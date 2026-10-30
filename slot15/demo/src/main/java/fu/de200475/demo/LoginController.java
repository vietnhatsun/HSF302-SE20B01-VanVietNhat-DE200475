package fu.de200475.demo;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;

public class LoginController {

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private CheckBox rememberCheckBox;

    @FXML
    private Hyperlink forgotPasswordLink;

    @FXML
    private Label errorLabel;

    @FXML
    private Button loginButton;

    @FXML
    private Hyperlink registerLink;

    @FXML
    protected void handleLogin(ActionEvent event) {
        String username = usernameField.getText();
        String password = passwordField.getText();

        // Xử lý logic đăng nhập tại đây
        // Kiểm tra thông tin đăng nhập
        if ("admin".equals(username) && "1234567".equals(password)) {
            showSuccessModal(username, rememberCheckBox.isSelected());
        } else {
            showErrorAlert();
        }
    }

    // Hiển thị Modal khi đăng nhập thành công
    private void showSuccessModal(String username, boolean remember) {
        Alert infoModal = new Alert(Alert.AlertType.INFORMATION);
        infoModal.setTitle("Thông báo");
        infoModal.setHeaderText("Đăng nhập thành công!");
        infoModal.setContentText("Tài khoản: " + username + "\n" +
                "Trạng thái ghi nhớ: " + (remember ? "Có" : "Không"));
        infoModal.showAndWait();
    }

    // Hiển thị Alert khi đăng nhập thất bại
    private void showErrorAlert() {
        Alert errorAlert = new Alert(Alert.AlertType.ERROR);
        errorAlert.setTitle("Lỗi");
        errorAlert.setHeaderText(null);
        errorAlert.setContentText("Đăng nhập không hợp lệ.");
        errorAlert.showAndWait();
    }

    @FXML
    protected void handleForgotPassword(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Quên mật khẩu");
        alert.setHeaderText(null);
        alert.setContentText("Vui lòng liên hệ quản trị viên để lấy lại mật khẩu.");
        alert.showAndWait();
    }

    @FXML
    protected void handleRegister(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Đăng ký tài khoản");
        alert.setHeaderText(null);
        alert.setContentText("Chức năng đăng ký tài khoản đang được phát triển.");
        alert.showAndWait();
    }
}
