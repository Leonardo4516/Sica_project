package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
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
        // Sin partículas: JavaFX obliga a que todo dibujo en Canvas corra en el hilo de UI,
        // así que animarlas no se beneficia de hilos y consumía recursos sin necesidad real.
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();
        System.out.println("[LoginController] Intento login: " + usuario);

        if (usuario.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        btnLogin.setDisable(true);
        try {
            Optional<Usuario> result = authService.login(usuario, password);
            if (result.isPresent()) {
                Usuario u = result.get();
                System.out.println("[LoginController] Login OK: " + u.getUsername() + " / " + u.getRol().getNombre());
                SceneManager.navigateAfterLogin(u);
            } else {
                System.out.println("[LoginController] Login FAIL: credenciales inválidas para " + usuario);
                showError("Credenciales inválidas");
            }
        } catch (Exception e) {
            System.err.println("[LoginController] Login EXCEPTION: " + e.getClass().getName() + ": " + e.getMessage());
            e.printStackTrace();
            showError("Error de autenticación: " + e.getMessage());
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
