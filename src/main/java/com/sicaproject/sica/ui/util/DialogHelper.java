package com.sicaproject.sica.ui.util;

import com.sicaproject.sica.incidentes.domain.SeveridadIncidente;
import com.sicaproject.sica.ui.SceneManager;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;

import java.util.Optional;

public final class DialogHelper {

    private DialogHelper() {}

    public static void aplicarEstilo(Dialog<?> dialog) {
        DialogPane pane = dialog.getDialogPane();
        String css = SceneManager.class.getResource("styles.css") != null
                ? SceneManager.class.getResource("styles.css").toExternalForm()
                : null;
        if (css != null && !pane.getStylesheets().contains(css)) {
            pane.getStylesheets().add(css);
        }
        pane.getStyleClass().add("custom-dialog-pane");

        for (ButtonType bt : pane.getButtonTypes()) {
            Button btn = (Button) pane.lookupButton(bt);
            if (btn != null) {
                btn.getStyleClass().add("btn");
                if (bt.getButtonData() == ButtonBar.ButtonData.OK_DONE || bt == ButtonType.OK) {
                    btn.getStyleClass().add("btn-primary");
                } else if (bt == ButtonType.CANCEL) {
                    btn.getStyleClass().add("btn-secondary");
                } else if (bt == ButtonType.YES) {
                    btn.getStyleClass().add("btn-danger");
                }
            }
        }
    }

    public static void mostrarInfo(String titulo, String encabezado, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static void mostrarExito(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titulo);
        alert.setHeaderText(titulo);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static void mostrarError(String titulo, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(titulo);
        alert.setHeaderText("Error en la Operacion");
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static void mostrarAdvertencia(String titulo, String encabezado, String mensaje) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(titulo);
        alert.setHeaderText(encabezado);
        alert.setContentText(mensaje);
        aplicarEstilo(alert);
        alert.showAndWait();
    }

    public static Optional<String> pedirTexto(String titulo, String encabezado, String labelTexto, String prompt) {
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle(titulo);
        dialog.setHeaderText(encabezado);

        VBox content = new VBox(10);
        content.setPadding(new Insets(14));
        Label lbl = new Label(labelTexto);
        lbl.getStyleClass().add("label-text");
        lbl.setStyle("-fx-font-weight: bold; -fx-text-fill: #cbd5e1;");

        TextField txtInput = new TextField();
        txtInput.setPromptText(prompt);
        txtInput.setPrefWidth(360);
        content.getChildren().addAll(lbl, txtInput);

        dialog.getDialogPane().setContent(content);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aplicarEstilo(dialog);

        dialog.setResultConverter(bt -> bt == ButtonType.OK ? txtInput.getText().trim() : null);

        return dialog.showAndWait();
    }

    public static class IncidenteDialogData {
        public final String titulo;
        public final String descripcion;
        public final SeveridadIncidente severidad;
        public final com.sicaproject.sica.personas.domain.Persona persona;

        public IncidenteDialogData(String titulo, String descripcion, SeveridadIncidente severidad, com.sicaproject.sica.personas.domain.Persona persona) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.severidad = severidad;
            this.persona = persona;
        }
    }

    public static Optional<IncidenteDialogData> mostrarDialogoReporteIncidente(String origen, java.util.List<com.sicaproject.sica.personas.domain.Persona> personas) {
        Dialog<IncidenteDialogData> dialog = new Dialog<>();
        dialog.setTitle("Reportar Incidente");
        dialog.setHeaderText("Registro de Incidente - " + origen);

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 20, 16, 20));

        Label lblTit = new Label("Titulo del incidente:");
        lblTit.getStyleClass().add("label-text");
        TextField txtTitulo = new TextField();
        txtTitulo.setPromptText("Ej. Intento de acceso no autorizado");
        txtTitulo.setPrefWidth(380);

        Label lblSev = new Label("Nivel de severidad:");
        lblSev.getStyleClass().add("label-text");
        ComboBox<SeveridadIncidente> cmbSev = new ComboBox<>(FXCollections.observableArrayList(SeveridadIncidente.values()));
        cmbSev.setValue(SeveridadIncidente.MEDIA);
        cmbSev.setPrefWidth(380);

        Label lblPer = new Label("Persona involucrada (opcional):");
        lblPer.getStyleClass().add("label-text");
        ComboBox<com.sicaproject.sica.personas.domain.Persona> cmbPer = new ComboBox<>(FXCollections.observableArrayList(personas));
        cmbPer.setPrefWidth(380);
        cmbPer.setPromptText("Seleccione una persona...");
        cmbPer.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.sicaproject.sica.personas.domain.Persona p) {
                return p == null ? "" : p.getNombre() + " (" + p.getDocumento() + ")";
            }
            @Override
            public com.sicaproject.sica.personas.domain.Persona fromString(String string) {
                return null;
            }
        });

        Label lblDesc = new Label("Descripcion detallada:");
        lblDesc.getStyleClass().add("label-text");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Detalles de lo ocurrido...");
        txtDesc.setPrefRowCount(4);
        txtDesc.setPrefWidth(380);
        txtDesc.setWrapText(true);

        grid.add(lblTit, 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(lblSev, 0, 1);
        grid.add(cmbSev, 1, 1);
        grid.add(lblPer, 0, 2);
        grid.add(cmbPer, 1, 2);
        grid.add(lblDesc, 0, 3);
        grid.add(txtDesc, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aplicarEstilo(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String tit = txtTitulo.getText().trim();
                String desc = txtDesc.getText().trim();
                if (tit.isEmpty() || desc.isEmpty()) {
                    return null;
                }
                return new IncidenteDialogData(tit, desc, cmbSev.getValue(), cmbPer.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }

    public static class UsuarioDialogData {
        public final String nombreRol;
        public final com.sicaproject.sica.personas.domain.Persona persona;

        public UsuarioDialogData(String nombreRol, com.sicaproject.sica.personas.domain.Persona persona) {
            this.nombreRol = nombreRol;
            this.persona = persona;
        }
    }

    public static Optional<UsuarioDialogData> mostrarDialogoEditarUsuario(com.sicaproject.sica.iam.domain.Usuario usuario, java.util.List<com.sicaproject.sica.iam.domain.Rol> roles, java.util.List<com.sicaproject.sica.personas.domain.Persona> personas) {
        Dialog<UsuarioDialogData> dialog = new Dialog<>();
        dialog.setTitle("Editar Usuario");
        dialog.setHeaderText("Usuario: " + usuario.getUsername());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 20, 16, 20));

        Label lblRol = new Label("Rol del usuario:");
        lblRol.getStyleClass().add("label-text");
        ComboBox<String> cmbRol = new ComboBox<>();
        for(com.sicaproject.sica.iam.domain.Rol r : roles) cmbRol.getItems().add(r.getNombre());
        cmbRol.setValue(usuario.getRol().getNombre());
        cmbRol.setPrefWidth(380);

        Label lblPer = new Label("Persona vinculada (opcional):");
        lblPer.getStyleClass().add("label-text");
        ComboBox<com.sicaproject.sica.personas.domain.Persona> cmbPer = new ComboBox<>(FXCollections.observableArrayList(personas));
        cmbPer.setPrefWidth(380);
        cmbPer.setPromptText("Seleccione una persona...");
        cmbPer.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.sicaproject.sica.personas.domain.Persona p) {
                return p == null ? "" : p.getNombre() + " (" + p.getDocumento() + ")";
            }
            @Override
            public com.sicaproject.sica.personas.domain.Persona fromString(String string) {
                return null;
            }
        });
        cmbPer.setValue(usuario.getPersona());

        grid.add(lblRol, 0, 0);
        grid.add(cmbRol, 1, 0);
        grid.add(lblPer, 0, 1);
        grid.add(cmbPer, 1, 1);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aplicarEstilo(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                return new UsuarioDialogData(cmbRol.getValue(), cmbPer.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
