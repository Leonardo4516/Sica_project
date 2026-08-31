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

        public IncidenteDialogData(String titulo, String descripcion, SeveridadIncidente severidad) {
            this.titulo = titulo;
            this.descripcion = descripcion;
            this.severidad = severidad;
        }
    }

    public static Optional<IncidenteDialogData> mostrarDialogoReporteIncidente(String origen) {
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

        Label lblDesc = new Label("Descripcion detallada:");
        lblDesc.getStyleClass().add("label-text");
        TextArea txtDesc = new TextArea();
        txtDesc.setPromptText("Detalles de lo ocurrido, lugar exacto, personas o vehiculos involucrados...");
        txtDesc.setPrefRowCount(4);
        txtDesc.setPrefWidth(380);
        txtDesc.setWrapText(true);

        grid.add(lblTit, 0, 0);
        grid.add(txtTitulo, 1, 0);
        grid.add(lblSev, 0, 1);
        grid.add(cmbSev, 1, 1);
        grid.add(lblDesc, 0, 2);
        grid.add(txtDesc, 1, 2);

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
                return new IncidenteDialogData(tit, desc, cmbSev.getValue());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
