package fu.de200475.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.stage.Stage;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class WelcomeController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Label infoLabel;

    public void setUser(String username, boolean remember) {
        welcomeLabel.setText("Xin chào, " + username + "!");
        String time = LocalDateTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss - dd/MM/yyyy"));
        String extra = remember ? " (Đã bật ghi nhớ đăng nhập)" : "";
        infoLabel.setText("Thời gian đăng nhập: " + time + extra);
    }

    @FXML
    protected void handleLogout(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("login-view.fxml"));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root, 800, 500));
            stage.setTitle("Đăng nhập");
            stage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
