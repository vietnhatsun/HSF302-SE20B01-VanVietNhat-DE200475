package com.fu.ch08.t4;

import com.fu.ch08.common.AlertUtil;
import javafx.application.Application;
import javafx.beans.binding.Bindings;
import javafx.beans.property.DoubleProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ScoreDashboardApp extends Application {

    // TODO 2: ObservableList có EXTRACTOR → sửa điểm 1 dòng cũng phát sự kiện thay đổi
    private final ObservableList<ScoreRow> scores = FXCollections.observableArrayList(ScoreRow::observables);

    private final PieChart pie = new PieChart();
    private final CategoryAxis barX = new CategoryAxis();
    private final NumberAxis barY = new NumberAxis(0, 10, 1);
    private final BarChart<String, Number> bar = new BarChart<>(barX, barY);
    private final StackedBarChart<String, Number> stacked = new StackedBarChart<>(new CategoryAxis(), new NumberAxis(0, 30, 5));
    private final LineChart<String, Number> line = new LineChart<>(new CategoryAxis(), new NumberAxis(0, 10, 1));
    private final StackPane barHolder = new StackPane(bar);
    private final Map<String, CheckBox> seriesToggles = new LinkedHashMap<>();

    @Override
    public void start(Stage stage) {
        scores.addAll(
                new ScoreRow("An", 8, 9, 7.5),
                new ScoreRow("Bình", 6, 5.5, 7),
                new ScoreRow("Cường", 9.5, 9, 9),
                new ScoreRow("Dung", 4, 5, 4.5)
        );

        // TODO 3: Bảng điểm editable
        TableView<ScoreRow> table = new TableView<>(scores);
        table.setEditable(true);
        TableColumn<ScoreRow, String> colName = new TableColumn<>("Họ tên");
        colName.setCellValueFactory(c -> c.getValue().nameProperty());
        table.getColumns().add(colName);
        addScoreColumn(table, "Java", ScoreRow::javaProperty);
        addScoreColumn(table, "Spring", ScoreRow::springProperty);
        addScoreColumn(table, "JavaFX", ScoreRow::javafxProperty);
        TableColumn<ScoreRow, String> colAvg = new TableColumn<>("TB");
        colAvg.setCellValueFactory(c -> Bindings.createStringBinding(
                () -> String.valueOf(c.getValue().average()), c.getValue().observables()));
        table.getColumns().add(colAvg);

        // TODO 4: Form thêm
        TextField tfName = new TextField();
        TextField tfJava = new TextField();
        TextField tfSpring = new TextField();
        TextField tfFx = new TextField();
        tfName.setPromptText("Họ tên");
        tfJava.setPromptText("Java");
        tfSpring.setPromptText("Spring");
        tfFx.setPromptText("JavaFX");
        Button btnAdd = new Button("Thêm");
        Button btnDel = new Button("Xóa dòng chọn");

        btnAdd.setOnAction(e -> {
            try {
                double j = score(tfJava);
                double s = score(tfSpring);
                double f = score(tfFx);
                String n = tfName.getText().trim();
                if (n.isEmpty()) throw new IllegalArgumentException("Nhập họ tên");
                if (scores.stream().anyMatch(r -> r.getName().equalsIgnoreCase(n))) {
                    throw new IllegalArgumentException("Tên đã tồn tại");
                }
                scores.add(new ScoreRow(n, j, s, f));
                List.of(tfName, tfJava, tfSpring, tfFx).forEach(TextField::clear);
            } catch (IllegalArgumentException ex) {
                AlertUtil.warn(ex.getMessage());
            }
        });
        btnDel.setOnAction(e -> scores.remove(table.getSelectionModel().getSelectedItem()));
        btnDel.disableProperty().bind(table.getSelectionModel().selectedItemProperty().isNull());

        // TODO 5: Chart config
        pie.setTitle("Phân bố xếp loại");
        bar.setTitle("Điểm trung bình theo môn");
        bar.setAnimated(false);
        bar.setLegendVisible(false);
        stacked.setTitle("Tổng điểm từng sinh viên");
        stacked.setAnimated(false);
        line.setTitle("Điểm từng sinh viên");
        line.setAnimated(false);

        ComboBox<String> cbBarType = new ComboBox<>(FXCollections.observableArrayList("Bar - TB môn", "Stacked - từng SV"));
        cbBarType.setValue("Bar - TB môn");
        cbBarType.valueProperty().addListener((o, a, v) -> barHolder.getChildren().setAll(v.startsWith("Bar") ? bar : stacked));

        HBox toggles = new HBox(10);
        for (String subject : List.of("Java", "Spring", "JavaFX")) {
            CheckBox cb = new CheckBox(subject);
            cb.setSelected(true);
            cb.selectedProperty().addListener((o, a, b) -> refreshCharts());
            seriesToggles.put(subject, cb);
            toggles.getChildren().add(cb);
        }

        // TODO 6: Lắng nghe thay đổi dữ liệu (thêm/xóa/sửa)
        scores.addListener((ListChangeListener<ScoreRow>) change -> refreshCharts());
        refreshCharts();

        TabPane tabs = new TabPane(
                new Tab("Xếp loại", pie),
                new Tab("Theo môn", new VBox(6, cbBarType, barHolder)),
                new Tab("Từng SV", new VBox(6, toggles, line))
        );
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);

        VBox left = new VBox(8, new HBox(5, tfName, tfJava, tfSpring, tfFx), new HBox(5, btnAdd, btnDel), table);
        left.setPadding(new Insets(10));
        left.setPrefWidth(470);

        stage.setScene(new Scene(new SplitPane(left, tabs), 1150, 600));
        stage.setTitle("T4 - Charts Dashboard (double-click ô điểm để sửa)");
        stage.show();
    }

    private void addScoreColumn(TableView<ScoreRow> t, String title, Function<ScoreRow, DoubleProperty> prop) {
        TableColumn<ScoreRow, Double> col = new TableColumn<>(title);
        col.setCellValueFactory(c -> prop.apply(c.getValue()).asObject());
        // DoubleStringConverter ném NumberFormatException khi gõ chữ → bọc lại, trả null để báo lỗi
        col.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter() {
            @Override
            public Double fromString(String s) {
                try {
                    return super.fromString(s == null ? null : s.trim().replace(',', '.'));
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        }));
        col.setOnEditCommit(e -> {
            Double v = e.getNewValue();
            if (v == null || v < 0 || v > 10) {
                AlertUtil.warn("Điểm từ 0 đến 10");
                t.refresh();
                return;
            }
            prop.apply(e.getRowValue()).set(v);
        });
        t.getColumns().add(col);
    }

    private static double score(TextField tf) {
        try {
            double v = Double.parseDouble(tf.getText().trim());
            if (v < 0 || v > 10) throw new IllegalArgumentException(tf.getPromptText() + ": điểm từ 0 đến 10");
            return v;
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(tf.getPromptText() + ": phải là số");
        }
    }

    // TODO 7: Dựng lại dữ liệu cho mọi chart
    private void refreshCharts() {
        if (scores.isEmpty()) {
            pie.getData().clear();
            bar.getData().clear();
            stacked.getData().clear();
            line.getData().clear();
            return;
        }

        // Pie
        Map<String, List<String>> byGrade = scores.stream().collect(Collectors.groupingBy(ScoreRow::grade,
                TreeMap::new, Collectors.mapping(ScoreRow::getName, Collectors.toList())));
        pie.getData().setAll(byGrade.entrySet().stream()
                .map(en -> new PieChart.Data(en.getKey() + " (" + en.getValue().size() + ")", en.getValue().size())).toList());
        for (PieChart.Data d : pie.getData()) {          // node đã tồn tại sau khi add vào chart
            String grade = d.getName().substring(0, d.getName().indexOf(" ("));
            double pct = d.getPieValue() * 100 / scores.size();
            Tooltip.install(d.getNode(), new Tooltip("%s: %.1f%%".formatted(d.getName(), pct)));
            d.getNode().setOnMouseClicked(e -> AlertUtil.info(grade + ": " + String.join(", ", byGrade.get(grade))));
        }

        // Bar — TB từng môn
        XYChart.Series<String, Number> avg = new XYChart.Series<>();
        avg.getData().add(new XYChart.Data<>("Java", scores.stream().mapToDouble(r -> r.javaProperty().get()).average().orElse(0)));
        avg.getData().add(new XYChart.Data<>("Spring", scores.stream().mapToDouble(r -> r.springProperty().get()).average().orElse(0)));
        avg.getData().add(new XYChart.Data<>("JavaFX", scores.stream().mapToDouble(r -> r.javafxProperty().get()).average().orElse(0)));
        bar.getData().setAll(List.of(avg));

        // Stacked & Line — mỗi môn 1 series, trục X = tên SV
        stacked.getData().clear();
        line.getData().clear();
        Map<String, Function<ScoreRow, DoubleProperty>> subjects = new LinkedHashMap<>();
        subjects.put("Java", ScoreRow::javaProperty);
        subjects.put("Spring", ScoreRow::springProperty);
        subjects.put("JavaFX", ScoreRow::javafxProperty);
        subjects.forEach((subject, prop) -> {
            XYChart.Series<String, Number> s1 = new XYChart.Series<>();
            XYChart.Series<String, Number> s2 = new XYChart.Series<>();
            s1.setName(subject);
            s2.setName(subject);
            scores.forEach(r -> {
                s1.getData().add(new XYChart.Data<>(r.getName(), prop.apply(r).get()));
                s2.getData().add(new XYChart.Data<>(r.getName(), prop.apply(r).get()));
            });
            stacked.getData().add(s1);
            if (seriesToggles.isEmpty() || seriesToggles.get(subject).isSelected()) {
                line.getData().add(s2);
            }
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}
