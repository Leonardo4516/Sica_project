package com.sicaproject.sica.ui;

import com.sicaproject.sica.iam.domain.Usuario;
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

    public static String fxmlForRole(Usuario usuario) {
        if (usuario == null || usuario.getRol() == null || usuario.getRol().getNombre() == null) {
            throw new IllegalStateException("El usuario autenticado no tiene rol cargado");
        }
        return switch (usuario.getRol().getNombre().toUpperCase()) {
            case "GUARDA" -> "/com/sicaproject/sica/ui/guarda.fxml";
            case "FUNCIONARIO" -> "/com/sicaproject/sica/ui/funcionario.fxml";
            case "ADMIN" -> "/com/sicaproject/sica/ui/admin.fxml";
            default -> throw new IllegalStateException("Rol no reconocido: " + usuario.getRol().getNombre());
        };
    }

    public static String titleForRole(Usuario usuario) {
        return switch (usuario.getRol().getNombre().toUpperCase()) {
            case "GUARDA" -> "SICA — Panel Guardía";
            case "FUNCIONARIO" -> "SICA — Panel Funcionario";
            case "ADMIN" -> "SICA — Administración";
            default -> "SICA";
        };
    }

    public static void loadScene(String fxmlPath, String title) {
        try {
            var resource = SceneManager.class.getResource(fxmlPath);
            if (resource == null) {
                throw new IOException("No se encontró el FXML en el classpath: " + fxmlPath);
            }
            FXMLLoader loader = new FXMLLoader(resource);
            Parent root = loader.load();
            Scene scene = new Scene(root, 1280, 720);
            var css = SceneManager.class.getResource("styles.css");
            if (css != null) {
                scene.getStylesheets().add(css.toExternalForm());
            }
            primaryStage.setScene(scene);
            primaryStage.setTitle(title);
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            throw new RuntimeException("Error cargando " + fxmlPath + ": " + rootCause(e).getMessage(), e);
        }
    }

    public static void navigateAfterLogin(Usuario usuario) {
        currentUser = usuario;
        loadScene(fxmlForRole(usuario), titleForRole(usuario));
    }

    static Throwable rootCause(Throwable t) {
        Throwable current = t;
        while (current.getCause() != null && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }

    public static void logout() {
        currentUser = null;
        loadScene("/com/sicaproject/sica/ui/login.fxml", "SICA — Login");
    }
}
