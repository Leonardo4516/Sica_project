package com.sicaproject.sica.ui;

import com.sicaproject.sica.iam.domain.Usuario;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Administrador global de escenas para la UI JavaFX.
 * Maneja la carga de FXML y navegación entre pantallas según el rol del usuario.
 */
public class SceneManager {

    private static Stage primaryStage;
    private static Usuario currentUser;

    public static void setPrimaryStage(Stage stage) {
        primaryStage = stage;
    }

    public static Stage getPrimaryStage() {
        return primaryStage;
    }

    public static Usuario getCurrentUser() {
        return currentUser;
    }

    public static void loadScene(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(
                SceneManager.class.getResource(fxmlPath)
            );
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            scene.getStylesheets().add(
                SceneManager.class.getResource("styles.css").toExternalForm()
            );
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Error cargando " + fxmlPath, e);
        }
    }

    public static void navigateAfterLogin(Usuario usuario) {
        currentUser = usuario;
        String rol = usuario.getRol().getNombre().toUpperCase();
        switch (rol) {
            case "GUARDA":
                loadScene("/com/sicaproject/sica/ui/guarda.fxml", "SICA — Panel Guardía");
                break;
            case "FUNCIONARIO":
                loadScene("/com/sicaproject/sica/ui/funcionario.fxml", "SICA — Panel Funcionario");
                break;
            case "ADMIN":
                loadScene("/com/sicaproject/sica/ui/admin.fxml", "SICA — Administración");
                break;
            default:
                loadScene("/com/sicaproject/sica/ui/login.fxml", "SICA — Login");
        }
    }

    public static void logout() {
        currentUser = null;
        loadScene("/com/sicaproject/sica/ui/login.fxml", "SICA — Login");
    }
}
