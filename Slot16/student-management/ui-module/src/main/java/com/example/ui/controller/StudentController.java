package com.example.ui.controller;

import com.example.data.exception.StudentNotFoundException;
import com.example.data.exception.ValidationException;
import com.example.data.pojo.Student;
import com.example.data.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class StudentController {

    @FXML private TableView<Student> tableView;
    @FXML private TextField searchField, idField, nameField, emailField, ageField;
    @FXML private Button btnUpdate, btnDelete;
    @FXML private Label messageLabel, statusLabel;

    private final StudentService studentService;
    private final ObservableList<Student> data = FXCollections.observableArrayList();

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @FXML
    public void initialize() {
        tableView.setItems(data);

        tableView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                fillForm(newVal);
            }
        });

        btnUpdate.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());
        btnDelete.disableProperty().bind(tableView.getSelectionModel().selectedItemProperty().isNull());

        loadData();
    }

    private void loadData() {
        try {
            List<Student> list = studentService.findAll();
            data.setAll(list);
            statusLabel.setText("Loaded " + list.size() + " students");
            clearMessage();
        } catch (Exception e) {
            showError("Cannot load data: " + e.getMessage());
            statusLabel.setText("Database error");
        }
    }

    @FXML
    private void handleSearch() {
        try {
            String kw = searchField.getText();
            List<Student> list = studentService.search(kw);
            data.setAll(list);
            statusLabel.setText("Found " + list.size() + " students");
            clearMessage();
        } catch (Exception e) {
            showError("Search error: " + e.getMessage());
            statusLabel.setText("Database error");
        }
    }

    @FXML
    private void handleRefresh() {
        searchField.clear();
        loadData();
    }

    private void fillForm(Student s) {
        if (s != null) {
            idField.setText(s.getId() != null ? String.valueOf(s.getId()) : "");
            nameField.setText(s.getName() != null ? s.getName() : "");
            emailField.setText(s.getEmail() != null ? s.getEmail() : "");
            ageField.setText(s.getAge() != null ? String.valueOf(s.getAge()) : "");
            clearMessage();
        }
    }

    // TODO 6.1: readForm
    private Student readForm() {
        String name = nameField.getText();
        String email = emailField.getText();
        String ageText = ageField.getText();

        if (ageText == null || ageText.isBlank()) {
            throw new ValidationException("Age is required");
        }

        int age;
        try {
            age = Integer.parseInt(ageText.trim());
        } catch (NumberFormatException e) {
            throw new ValidationException("Age must be a number");
        }

        return new Student(name, email, age);
    }

    // TODO 6.2: handleAdd
    @FXML
    private void handleAdd() {
        try {
            Student s = readForm();
            Student created = studentService.create(s);
            showInfo("Added: " + created.getName());
            handleClear();
            loadData();
        } catch (ValidationException | StudentNotFoundException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("System error: " + e.getMessage());
        }
    }

    // TODO 6.3: handleUpdate
    @FXML
    private void handleUpdate() {
        try {
            Student selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }
            Student edit = readForm();
            edit.setId(selected.getId());
            Student updated = studentService.update(edit);
            showInfo("Updated: " + updated.getName());
            handleClear();
            loadData();
        } catch (ValidationException | StudentNotFoundException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("System error: " + e.getMessage());
        }
    }

    // TODO 6.4: handleDelete
    @FXML
    private void handleDelete() {
        try {
            Student selected = tableView.getSelectionModel().getSelectedItem();
            if (selected == null) {
                return;
            }

            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirm Delete");
            alert.setHeaderText(null);
            alert.setContentText("Delete student \"" + selected.getName() + "\"?");

            Optional<ButtonType> result = alert.showAndWait();
            if (result.isPresent() && result.get() == ButtonType.OK) {
                studentService.delete(selected.getId());
                showInfo("Deleted: " + selected.getName());
                handleClear();
                loadData();
            }
        } catch (ValidationException | StudentNotFoundException e) {
            showError(e.getMessage());
        } catch (Exception e) {
            showError("System error: " + e.getMessage());
        }
    }

    // TODO 6.5: handleClear
    @FXML
    private void handleClear() {
        idField.clear();
        nameField.clear();
        emailField.clear();
        ageField.clear();
        tableView.getSelectionModel().clearSelection();
        nameField.requestFocus();
    }

    private void showInfo(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: green;");
    }

    private void showError(String message) {
        messageLabel.setText(message);
        messageLabel.setStyle("-fx-text-fill: red;");
    }

    private void clearMessage() {
        messageLabel.setText("");
    }
}
