package com.fu.ch08.t1;

import com.fu.ch08.common.AlertUtil;
import javafx.application.Platform;
import javafx.beans.Observable;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class StudentController {
    @FXML private TreeView<String> tvFaculty;
    @FXML private TextField tfSearch, tfId, tfName, tfGpa;
    @FXML private TableView<Student> tblStudents;
    @FXML private TableColumn<Student, String> colId, colName, colClass, colGrade;
    @FXML private TableColumn<Student, Double> colGpa;
    @FXML private ComboBox<String> cbClass;
    @FXML private Label lblCount;
    @FXML private Button btnUpdate, btnDelete;

    // Khoa → danh sách lớp
    private static final Map<String, List<String>> FACULTIES = new LinkedHashMap<>();
    static {
        FACULTIES.put("Kỹ thuật phần mềm", List.of("SE1801", "SE1802", "SE1803"));
        FACULTIES.put("Trí tuệ nhân tạo", List.of("AI1801", "AI1802"));
    }

    // Extractor: sửa lớp/tên của 1 SV cũng làm FilteredList lọc lại
    private final ObservableList<Student> students = FXCollections.observableArrayList(
            s -> new Observable[]{s.studentIdProperty(), s.fullNameProperty(), s.classNameProperty()});
    {
        students.addAll(
                new Student("SE180001", "Nguyễn Văn An", "SE1801", 8.2),
                new Student("SE180002", "Trần Thị Bình", "SE1801", 9.1),
                new Student("SE180010", "Lê Văn Cường", "SE1802", 6.4),
                new Student("AI180003", "Phạm Thu Dung", "AI1801", 7.5));
    }
    private final FilteredList<Student> filtered = new FilteredList<>(students);

    @FXML
    public void initialize() {
        // TODO 3: Dựng cây
        TreeItem<String> root = new TreeItem<>("FPT University");
        FACULTIES.forEach((fac, classes) -> {
            TreeItem<String> facItem = new TreeItem<>(fac);
            classes.forEach(c -> facItem.getChildren().add(new TreeItem<>(c)));
            facItem.setExpanded(true);
            root.getChildren().add(facItem);
        });
        root.setExpanded(true);
        tvFaculty.setRoot(root);
        cbClass.getItems().setAll(FACULTIES.values().stream().flatMap(List::stream).toList());

        // TODO 4: Cột — PropertyValueFactory cần package được opens tới javafx.base
        colId.setCellValueFactory(new PropertyValueFactory<>("studentId"));
        colName.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        colClass.setCellValueFactory(new PropertyValueFactory<>("className"));
        colGpa.setCellValueFactory(new PropertyValueFactory<>("gpa"));
        colGrade.setCellValueFactory(new PropertyValueFactory<>("grade"));

        SortedList<Student> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblStudents.comparatorProperty());
        tblStudents.setItems(sorted);
        tblStudents.setPlaceholder(new Label("Không có sinh viên"));
        lblCount.textProperty().bind(Bindings.size(filtered).asString("%d sinh viên"));

        // TODO 5: Predicate kết hợp cây + tìm kiếm
        filtered.predicateProperty().bind(Bindings.createObjectBinding(() -> {
            TreeItem<String> node = tvFaculty.getSelectionModel().getSelectedItem();
            String kw = tfSearch.getText() == null ? "" : tfSearch.getText().trim().toLowerCase();
            Set<String> allowedClasses = classesOf(node);
            return (Predicate<Student>) s -> (allowedClasses == null || allowedClasses.contains(s.getClassName()))
                    && (kw.isEmpty() || s.getStudentId().toLowerCase().contains(kw) || s.getFullName().toLowerCase().contains(kw));
        }, tvFaculty.getSelectionModel().selectedItemProperty(), tfSearch.textProperty()));

        // TODO 6: Chọn dòng → điền form; nút disable khi chưa chọn
        tblStudents.getSelectionModel().selectedItemProperty().addListener((o, a, s) -> {
            if (s == null) return;
            tfId.setText(s.getStudentId());
            tfName.setText(s.getFullName());
            cbClass.setValue(s.getClassName());
            tfGpa.setText(String.valueOf(s.getGpa()));
        });
        BooleanBinding noSel = tblStudents.getSelectionModel().selectedItemProperty().isNull();
        btnUpdate.disableProperty().bind(noSel);
        btnDelete.disableProperty().bind(noSel);
    }

    /** null = không lọc (gốc hoặc chưa chọn) */
    private Set<String> classesOf(TreeItem<String> node) {
        if (node == null || node.getParent() == null) return null;      // gốc
        if (node.isLeaf()) return Set.of(node.getValue());             // lớp
        return node.getChildren().stream().map(TreeItem::getValue).collect(Collectors.toSet());   // khoa
    }

    // TODO 7: Validate form — trả về Student hoặc null
    private Student readForm(Student editing) {
        String id = tfId.getText().trim().toUpperCase();
        String name = tfName.getText().trim();
        String cls = cbClass.getValue();
        if (!id.matches("(SE|AI)\\d{6}")) {
            AlertUtil.warn("MSSV dạng SE/AI + 6 số");
            return null;
        }
        if (name.length() < 3) {
            AlertUtil.warn("Họ tên ≥ 3 ký tự");
            return null;
        }
        if (cls == null) {
            AlertUtil.warn("Chọn lớp");
            return null;
        }
        if (!id.startsWith(cls.substring(0, 2))) {
            AlertUtil.warn("MSSV và lớp không cùng ngành");
            return null;
        }
        double gpa;
        try {
            gpa = Double.parseDouble(tfGpa.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            AlertUtil.warn("GPA phải là số");
            return null;
        }
        if (gpa < 0 || gpa > 10) {
            AlertUtil.warn("GPA từ 0 đến 10");
            return null;
        }
        boolean dup = students.stream().anyMatch(s -> s != editing && s.getStudentId().equalsIgnoreCase(id));
        if (dup) {
            AlertUtil.warn("MSSV đã tồn tại");
            return null;
        }
        return new Student(id, name, cls, gpa);
    }

    // TODO 8: CRUD
    @FXML private void handleAdd() {
        Student s = readForm(null);
        if (s != null) {
            students.add(s);
            handleClear();
        }
    }

    @FXML private void handleUpdate() {
        Student sel = tblStudents.getSelectionModel().getSelectedItem();
        Student s = readForm(sel);
        if (sel == null || s == null) return;
        sel.setStudentId(s.getStudentId());
        sel.setFullName(s.getFullName());
        sel.setClassName(s.getClassName());
        sel.setGpa(s.getGpa());       // Property → bảng tự cập nhật
    }

    @FXML private void handleDelete() {
        Student sel = tblStudents.getSelectionModel().getSelectedItem();
        if (sel != null && AlertUtil.confirm("Xóa " + sel.getFullName() + "?")) {
            students.remove(sel);
        }
    }

    @FXML private void handleClear() {
        tfId.clear();
        tfName.clear();
        tfGpa.clear();
        cbClass.setValue(null);
        tblStudents.getSelectionModel().clearSelection();
    }

    // TODO 9: Import CSV: studentId,fullName,className,gpa
    @FXML private void handleImport() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Import sinh viên");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        File f = fc.showOpenDialog(tblStudents.getScene().getWindow());
        if (f == null) return;
        int ok = 0;
        List<String> errors = new ArrayList<>();
        try {
            List<String> lines = Files.readAllLines(f.toPath(), StandardCharsets.UTF_8);
            for (int i = 1; i < lines.size(); i++) {                      // bỏ dòng tiêu đề
                String line = lines.get(i).trim();
                if (line.isEmpty()) continue;
                String[] p = line.split(",");
                try {
                    if (p.length != 4) throw new IllegalArgumentException("thiếu cột");
                    String id = p[0].trim().toUpperCase();
                    double gpa = Double.parseDouble(p[3].trim());
                    if (!id.matches("(SE|AI)\\d{6}") || gpa < 0 || gpa > 10) throw new IllegalArgumentException("dữ liệu sai");
                    if (students.stream().anyMatch(s -> s.getStudentId().equals(id))) throw new IllegalArgumentException("trùng MSSV");
                    students.add(new Student(id, p[1].trim(), p[2].trim(), gpa));
                    ok++;
                } catch (IllegalArgumentException ex) {                    // NumberFormatException là con của nó
                    errors.add("Dòng " + (i + 1) + ": " + ex.getMessage());
                }
            }
            AlertUtil.info("Import %d dòng thành công, %d dòng lỗi%s".formatted(ok, errors.size(),
                    errors.isEmpty() ? "" : "\n" + String.join("\n", errors.subList(0, Math.min(10, errors.size())))));
        } catch (IOException e) {
            AlertUtil.error("Không đọc được file: " + e.getMessage());
        }
    }

    // TODO 10: Export CSV những SV đang hiển thị
    @FXML private void handleExport() {
        FileChooser fc = new FileChooser();
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV", "*.csv"));
        fc.setInitialFileName("students.csv");
        File f = fc.showSaveDialog(tblStudents.getScene().getWindow());
        if (f == null) return;
        List<String> lines = new ArrayList<>(List.of("studentId,fullName,className,gpa"));
        tblStudents.getItems().forEach(s -> lines.add(String.join(",", s.getStudentId(), s.getFullName(),
                s.getClassName(), String.valueOf(s.getGpa()))));
        try {
            Files.write(f.toPath(), lines, StandardCharsets.UTF_8);
            AlertUtil.info("Đã xuất " + (lines.size() - 1) + " sinh viên");
        } catch (IOException e) {
            AlertUtil.error(e.getMessage());
        }
    }

    @FXML private void handleExit() {
        Platform.exit();
    }
}
