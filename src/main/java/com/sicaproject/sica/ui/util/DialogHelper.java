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

/**
 * Utilidad transversal para la construcción, personalización y despliegue de cuadros de diálogo modales.
 * Aplica de forma automática la paleta de colores oscuros del tema glassmorphism de SICA,
 * e incluye builders para diálogos de alerta, confirmación, reporte de incidentes y CRUD de personas con fotografía.
 */
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

    public static class PersonaDialogData {
        public final String nombre;
        public final String tipoDocumento;
        public final String documento;
        public final String tipo;
        public final com.sicaproject.sica.empresas.domain.Empresa empresa;
        public final String fotoUrl;
        public final java.io.File fotoArchivo;

        public PersonaDialogData(String nombre, String tipoDocumento, String documento, String tipo,
                                 com.sicaproject.sica.empresas.domain.Empresa empresa, String fotoUrl,
                                 java.io.File fotoArchivo) {
            this.nombre = nombre;
            this.tipoDocumento = tipoDocumento;
            this.documento = documento;
            this.tipo = tipo;
            this.empresa = empresa;
            this.fotoUrl = fotoUrl;
            this.fotoArchivo = fotoArchivo;
        }
    }

    public static Optional<PersonaDialogData> mostrarDialogoPersona(
            com.sicaproject.sica.personas.domain.Persona existente,
            java.util.List<com.sicaproject.sica.empresas.domain.Empresa> empresas) {
        Dialog<PersonaDialogData> dialog = new Dialog<>();
        dialog.setTitle(existente == null ? "Nueva Persona" : "Editar Persona");
        dialog.setHeaderText(existente == null ? "Registrar nueva persona en el sistema" : "Editando datos de: " + existente.getNombre());

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(14);
        grid.setPadding(new Insets(16, 20, 16, 20));

        // Nombre
        Label lblNom = new Label("Nombre completo:");
        lblNom.getStyleClass().add("label-text");
        TextField txtNombre = new TextField(existente != null ? existente.getNombre() : "");
        txtNombre.setPromptText("Ej. Carlos Andrés Pérez");
        txtNombre.setPrefWidth(360);

        // Tipo Documento
        Label lblTipoDoc = new Label("Tipo de documento:");
        lblTipoDoc.getStyleClass().add("label-text");
        ComboBox<String> cmbTipoDoc = new ComboBox<>(FXCollections.observableArrayList("CC", "CE", "PASAPORTE"));
        cmbTipoDoc.setValue(existente != null && existente.getTipoDocumento() != null ? existente.getTipoDocumento() : "CC");
        cmbTipoDoc.setPrefWidth(360);

        // Documento
        Label lblDoc = new Label("Número de documento:");
        lblDoc.getStyleClass().add("label-text");
        TextField txtDoc = new TextField(existente != null ? existente.getDocumento() : "");
        txtDoc.setPromptText("Ej. 1020304050");
        txtDoc.setPrefWidth(360);

        // Tipo Persona
        Label lblTipo = new Label("Tipo de persona:");
        lblTipo.getStyleClass().add("label-text");
        ComboBox<String> cmbTipo = new ComboBox<>(FXCollections.observableArrayList("TRABAJADOR", "INVITADO"));
        cmbTipo.setValue(existente != null && existente.getTipo() != null ? existente.getTipo() : "TRABAJADOR");
        cmbTipo.setPrefWidth(360);

        // Empresa
        Label lblEmp = new Label("Empresa vinculada (opcional):");
        lblEmp.getStyleClass().add("label-text");
        ComboBox<com.sicaproject.sica.empresas.domain.Empresa> cmbEmp = new ComboBox<>(FXCollections.observableArrayList(empresas));
        cmbEmp.setPrefWidth(360);
        cmbEmp.setPromptText("Seleccione empresa...");
        cmbEmp.setConverter(new javafx.util.StringConverter<>() {
            @Override
            public String toString(com.sicaproject.sica.empresas.domain.Empresa e) {
                return e == null ? "" : e.getNombre() + " (" + e.getNit() + ")";
            }
            @Override
            public com.sicaproject.sica.empresas.domain.Empresa fromString(String string) {
                return null;
            }
        });
        if (existente != null && existente.getEmpresaId() != null) {
            for (com.sicaproject.sica.empresas.domain.Empresa emp : empresas) {
                if (emp.getId() == existente.getEmpresaId()) {
                    cmbEmp.setValue(emp);
                    break;
                }
            }
        }

        // Foto y Preview
        Label lblFoto = new Label("Fotografía:");
        lblFoto.getStyleClass().add("label-text");

        javafx.scene.image.ImageView imgPreview = new javafx.scene.image.ImageView();
        imgPreview.setFitWidth(70);
        imgPreview.setFitHeight(70);
        imgPreview.setPreserveRatio(false);
        imgPreview.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 6, 0, 0, 0);");

        final java.util.concurrent.atomic.AtomicReference<java.io.File> fotoSeleccionadaRef = new java.util.concurrent.atomic.AtomicReference<>(null);
        final java.util.concurrent.atomic.AtomicReference<String> fotoUrlRef = new java.util.concurrent.atomic.AtomicReference<>(existente != null ? existente.getFotoUrl() : null);

        if (existente != null && existente.getFotoUrl() != null && !existente.getFotoUrl().isBlank()) {
            java.io.File f = new java.io.File(existente.getFotoUrl());
            if (f.exists()) {
                try {
                    imgPreview.setImage(new javafx.scene.image.Image(f.toURI().toString()));
                } catch (Exception ignored) {}
            }
        }

        Label lblFotoRuta = new Label(fotoUrlRef.get() != null ? fotoUrlRef.get() : "Sin foto");
        lblFotoRuta.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");

        Button btnSeleccionarFoto = new Button("Elegir Foto...");
        btnSeleccionarFoto.getStyleClass().addAll("btn", "btn-secondary");
        btnSeleccionarFoto.setStyle("-fx-font-size: 11px; -fx-padding: 4 10;");
        btnSeleccionarFoto.setOnAction(e -> {
            javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
            fileChooser.setTitle("Seleccionar Fotografía");
            fileChooser.getExtensionFilters().addAll(
                    new javafx.stage.FileChooser.ExtensionFilter("Imágenes (*.jpg, *.png, *.jpeg)", "*.jpg", "*.png", "*.jpeg")
            );
            java.io.File file = fileChooser.showOpenDialog(dialog.getDialogPane().getScene().getWindow());
            if (file != null) {
                fotoSeleccionadaRef.set(file);
                try {
                    imgPreview.setImage(new javafx.scene.image.Image(file.toURI().toString()));
                    lblFotoRuta.setText(file.getName());
                } catch (Exception ex) {
                    mostrarError("Error de Imagen", "No se pudo cargar la imagen: " + ex.getMessage());
                }
            }
        });

        Button btnQuitarFoto = new Button("Quitar");
        btnQuitarFoto.getStyleClass().addAll("btn", "btn-secondary");
        btnQuitarFoto.setStyle("-fx-font-size: 11px; -fx-padding: 4 10; -fx-text-fill: #ef4444;");
        btnQuitarFoto.setOnAction(e -> {
            fotoSeleccionadaRef.set(null);
            fotoUrlRef.set(null);
            imgPreview.setImage(null);
            lblFotoRuta.setText("Sin foto");
        });

        javafx.scene.layout.HBox boxFoto = new javafx.scene.layout.HBox(10, imgPreview, new VBox(6, btnSeleccionarFoto, btnQuitarFoto, lblFotoRuta));
        boxFoto.setAlignment(Pos.CENTER_LEFT);

        grid.add(lblNom, 0, 0);
        grid.add(txtNombre, 1, 0);
        grid.add(lblTipoDoc, 0, 1);
        grid.add(cmbTipoDoc, 1, 1);
        grid.add(lblDoc, 0, 2);
        grid.add(txtDoc, 1, 2);
        grid.add(lblTipo, 0, 3);
        grid.add(cmbTipo, 1, 3);
        grid.add(lblEmp, 0, 4);
        grid.add(cmbEmp, 1, 4);
        grid.add(lblFoto, 0, 5);
        grid.add(boxFoto, 1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        aplicarEstilo(dialog);

        dialog.setResultConverter(bt -> {
            if (bt == ButtonType.OK) {
                String nom = txtNombre.getText().trim();
                String doc = txtDoc.getText().trim();
                if (nom.isEmpty() || doc.isEmpty()) {
                    mostrarError("Campos obligatorios", "El nombre y el documento no pueden estar vacíos.");
                    return null;
                }
                return new PersonaDialogData(nom, cmbTipoDoc.getValue(), doc, cmbTipo.getValue(),
                        cmbEmp.getValue(), fotoUrlRef.get(), fotoSeleccionadaRef.get());
            }
            return null;
        });

        return dialog.showAndWait();
    }
}
