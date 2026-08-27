package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import com.sicaproject.sica.ui.component.ParticleBackground;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.util.Optional;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRecordar;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private StackPane rootPane;

    private final AuthService authService = CompositionRoot.getInstance().authService();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ParticleBackground.attachTo(rootPane);
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        btnLogin.setDisable(true);
        try {
            Optional<Usuario> result = authService.login(usuario, password);
            if (result.isPresent()) {
                SceneManager.navigateAfterLogin(result.get());
            } else {
                showError("Credenciales inválidas");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Throwable cause = e;
            while (cause.getCause() != null && cause.getCause() != cause) {
                cause = cause.getCause();
            }
            showError("No se pudo abrir el panel: " + cause.getClass().getSimpleName()
                    + " - " + cause.getMessage());
        } finally {
            btnLogin.setDisable(false);
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setOpacity(1.0);
        FadeTransition ft = new FadeTransition(Duration.seconds(4), lblError);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setDelay(Duration.seconds(2));
        ft.play();
    }
}
