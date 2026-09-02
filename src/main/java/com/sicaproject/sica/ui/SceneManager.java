package com.sicaproject.sica.ui;

import com.sicaproject.sica.iam.domain.Usuario;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Administrador global de escenas y enrutamiento dinámico de la interfaz gráfica JavaFX.
 * Centraliza la carga de plantillas FXML, aplicación de estilos CSS globales y redirección
 * contextual de pantallas según el rol del usuario autenticado (RBAC Navigation).
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

    /**
     * Determina la ruta FXML de la consola operativa correspondiente al rol del usuario.
     */
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

    /**
     * Provee el título formal de ventana correspondiente al panel asignado al usuario.
     */
    public static String titleForRole(Usuario usuario) {
        return switch (usuario.getRol().getNombre().toUpperCase()) {
            case "GUARDA" -> "SICA — Panel Guardia";
            case "FUNCIONARIO" -> "SICA — Panel Funcionario";
            case "ADMIN" -> "SICA — Administración";
            default -> "SICA";
        };
    }

    /**
     * Carga y renderiza un nuevo archivo FXML aplicando la hoja de estilos compartida styles.css.
     */
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

    /**
     * Enruta al usuario autenticado hacia su consola especializada tras un login exitoso.
     */
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

    /**
     * Cierra la sesión activa, desuscribe observadores de refresco y retorna a la pantalla de login.
     */
    public static void logout() {
        try {
            RefreshScheduler.getInstance().clear();
        } catch (Exception ignored) {
        }
        currentUser = null;
        loadScene("/com/sicaproject/sica/ui/login.fxml", "SICA — Login");
    }
}
