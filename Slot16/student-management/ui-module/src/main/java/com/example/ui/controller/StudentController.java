package com.example.ui.controller;

import com.example.data.pojo.Student;
import com.example.data.service.StudentService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.springframework.stereotype.Component;

import java.util.List;

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
        // TODO 5.2: tableView binding, selection listener, disable bindings, initial load
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

    // TODO 5.3: loadData
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

    // TODO 5.4: handleSearch, handleRefresh
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

    // TODO 5.5: fillForm, showInfo, showError
    private void fillForm(Student s) {
        if (s != null) {
            idField.setText(s.getId() != null ? String.valueOf(s.getId()) : "");
            nameField.setText(s.getName() != null ? s.getName() : "");
            emailField.setText(s.getEmail() != null ? s.getEmail() : "");
            ageField.setText(s.getAge() != null ? String.valueOf(s.getAge()) : "");
            clearMessage();
        }
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

    // Stubs for TODO #6
    @FXML private void handleAdd()     { }
    @FXML private void handleUpdate()  { }
    @FXML private void handleDelete()  { }
    @FXML private void handleClear()   { }
}
