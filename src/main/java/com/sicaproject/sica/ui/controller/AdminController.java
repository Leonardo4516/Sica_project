package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.incidentes.application.service.IncidenteService;
import com.sicaproject.sica.incidentes.domain.Incidente;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import com.sicaproject.sica.ui.util.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import com.sicaproject.sica.ui.RefreshScheduler;

public class AdminController implements Initializable, RefreshScheduler.Refreshable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblTotalVisitas;
    @FXML private Label lblVisitasDentro;
    @FXML private Label lblPendientes;
    @FXML private Label lblCerradasHoy;
    @FXML private TextField txtBuscarAdmin;
    @FXML private TabPane tabPaneAdmin;

    // Evacuación de Emergencia
    @FXML private Label lblEvacuacionConteo;
    @FXML private TableView<Visita> tblEvacuacion;
    @FXML private TableColumn<Visita, String> colEvacId;
    @FXML private TableColumn<Visita, String> colEvacDoc;
    @FXML private TableColumn<Visita, String> colEvacPersona;
    @FXML private TableColumn<Visita, String> colEvacTipo;
    @FXML private TableColumn<Visita, String> colEvacEmpresa;
    @FXML private TableColumn<Visita, String> colEvacHora;
    @FXML private TableColumn<Visita, String> colEvacAnfitrion;

    // Incidentes
    @FXML private TableView<Incidente> tblIncidentes;
    @FXML private TableColumn<Incidente, String> colIncId;
    @FXML private TableColumn<Incidente, String> colIncTitulo;
    @FXML private TableColumn<Incidente, String> colIncSeveridad;
    @FXML private TableColumn<Incidente, String> colIncEstado;
    @FXML private TableColumn<Incidente, String> colIncPersona;
    @FXML private TableColumn<Incidente, String> colIncReportadoPor;
    @FXML private TableColumn<Incidente, String> colIncFecha;
    @FXML private TableColumn<Incidente, Void> colIncAccion;

    // Personas
    @FXML private TableView<Persona> tblPersonas;
    @FXML private TableColumn<Persona, String> colPerId;
    @FXML private TableColumn<Persona, String> colPerDoc;
    @FXML private TableColumn<Persona, String> colPerNombre;
    @FXML private TableColumn<Persona, String> colPerTipo;
    @FXML private TableColumn<Persona, String> colPerEmpresa;
    @FXML private TableColumn<Persona, String> colPerEstado;
    @FXML private TableColumn<Persona, String> colPerVisitas;
    @FXML private TableColumn<Persona, Void> colPerAccion;

    // Usuarios, Roles, Empresas
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUId;
    @FXML private TableColumn<Usuario, String> colUUsername;
    @FXML private TableColumn<Usuario, String> colURol;
    @FXML private TableColumn<Usuario, String> colUPersona;
    @FXML private TableColumn<Usuario, String> colUEstado;
    @FXML private TableColumn<Usuario, Void> colUAccion;
    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolPermisos;
    @FXML private TableView<Empresa> tblEmpresas;
    @FXML private TableColumn<Empresa, String> colEId;
    @FXML private TableColumn<Empresa, String> colENombre;
    @FXML private TableColumn<Empresa, String> colENit;

    private final ObservableList<Visita> masterEvacuacion = FXCollections.observableArrayList();
    private final ObservableList<Incidente> masterIncidentes = FXCollections.observableArrayList();
    private final ObservableList<Persona> masterPersonas = FXCollections.observableArrayList();
    private final ObservableList<Usuario> masterUsuarios = FXCollections.observableArrayList();
    private final ObservableList<Rol> masterRoles = FXCollections.observableArrayList();
    private final ObservableList<Empresa> masterEmpresas = FXCollections.observableArrayList();

    private FilteredList<Visita> filteredEvacuacion;
    private FilteredList<Incidente> filteredIncidentes;
    private FilteredList<Persona> filteredPersonas;
    private FilteredList<Usuario> filteredUsuarios;
    private FilteredList<Rol> filteredRoles;
    private FilteredList<Empresa> filteredEmpresas;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final IncidenteService incidenteService = CompositionRoot.getInstance().incidenteService();
    private final PersonaService personaService = CompositionRoot.getInstance().personaService();
    private final RbacService rbacService = CompositionRoot.getInstance().rbacService();
    private final RolRepository rolRepository = CompositionRoot.getInstance().rolRepository();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
    private final EmpresaRepository empresaRepository = CompositionRoot.getInstance().empresaRepository();
    private final com.sicaproject.sica.iam.application.service.UsuarioService usuarioService = CompositionRoot.getInstance().usuarioService();

    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Bienvenido, " + SceneManager.getCurrentUser().getUsername());
        }
        configurarColumnasEvacuacion();
        configurarColumnasIncidentes();
        configurarColumnasPersonas();
        configurarColumnasUsuarios();
        configurarColumnasRoles();
        configurarColumnasEmpresas();

        filteredEvacuacion = new FilteredList<>(masterEvacuacion, p -> true);
        SortedList<Visita> sortedEvac = new SortedList<>(filteredEvacuacion);
        sortedEvac.comparatorProperty().bind(tblEvacuacion.comparatorProperty());
        tblEvacuacion.setItems(sortedEvac);

        filteredIncidentes = new FilteredList<>(masterIncidentes, p -> true);
        SortedList<Incidente> sortedInc = new SortedList<>(filteredIncidentes);
        sortedInc.comparatorProperty().bind(tblIncidentes.comparatorProperty());
        tblIncidentes.setItems(sortedInc);

        filteredPersonas = new FilteredList<>(masterPersonas, p -> true);
        SortedList<Persona> sortedPer = new SortedList<>(filteredPersonas);
        sortedPer.comparatorProperty().bind(tblPersonas.comparatorProperty());
        tblPersonas.setItems(sortedPer);

        filteredUsuarios = new FilteredList<>(masterUsuarios, p -> true);
        SortedList<Usuario> sortedUsu = new SortedList<>(filteredUsuarios);
        sortedUsu.comparatorProperty().bind(tblUsuarios.comparatorProperty());
        tblUsuarios.setItems(sortedUsu);

        filteredRoles = new FilteredList<>(masterRoles, p -> true);
        SortedList<Rol> sortedRol = new SortedList<>(filteredRoles);
        sortedRol.comparatorProperty().bind(tblRoles.comparatorProperty());
        tblRoles.setItems(sortedRol);

        filteredEmpresas = new FilteredList<>(masterEmpresas, p -> true);
        SortedList<Empresa> sortedEmp = new SortedList<>(filteredEmpresas);
        sortedEmp.comparatorProperty().bind(tblEmpresas.comparatorProperty());
        tblEmpresas.setItems(sortedEmp);

        if (txtBuscarAdmin != null) {
            txtBuscarAdmin.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltrosAdmin(newVal));
        }

        RefreshScheduler.getInstance().register(this);
        refreshAll();
    }

    @Override
    public void refreshData() {
        refreshAll();
    }

    private void refreshAll() {
        refreshStats();
        refreshEvacuacion();
        refreshIncidentes();
        refreshPersonas();
        refreshRoles();
        refreshUsuarios();
        refreshEmpresas();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshAll();
    }

    @FXML
    public void handleBitacora(Event event) {
        SceneManager.loadScene("/com/sicaproject/sica/ui/bitacora.fxml", "SICA — Bitácora");
    }

    @FXML
    public void handleLogout(Event event) {
        SceneManager.logout();
    }

    private void refreshStats() {
        var todas = visitaService.listarTodas();
        lblTotalVisitas.setText(String.valueOf(todas.size()));
        long dentro = todas.stream().filter(v -> v.getEstado() == EstadoVisita.DENTRO).count();
        lblVisitasDentro.setText(String.valueOf(dentro));
        lblPendientes.setText(String.valueOf(
            todas.stream().filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION).count()));
        LocalDate hoy = LocalDate.now();
        lblCerradasHoy.setText(String.valueOf(todas.stream().filter(v ->
            (v.getEstado() == EstadoVisita.CERRADA || v.getEstado() == EstadoVisita.CERRADA_POR_SISTEMA)
            && v.getFechaHoraSalida() != null
            && v.getFechaHoraSalida().toLocalDate().equals(hoy)).count()));
    }

    private void configurarColumnasEvacuacion() {
        colEvacId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colEvacDoc.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPersona().getTipoDocumento() + " " + data.getValue().getPersona().getDocumento()));
        colEvacPersona.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        colEvacTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getTipo()));
        colEvacEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colEvacHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colEvacAnfitrion.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getFuncionarioAnfitrion() != null ? data.getValue().getFuncionarioAnfitrion().getUsername() : "—"));
    }

    private void refreshEvacuacion() {
        List<Visita> activas = visitaService.listarTodas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
                .toList();
        masterEvacuacion.setAll(activas);
        lblEvacuacionConteo.setText("Personal dentro para conteo de evacuacion: " + activas.size() + " personas");
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    private void configurarColumnasIncidentes() {
        colIncId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colIncTitulo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitulo()));
        colIncSeveridad.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSeveridad().name()));
        colIncEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado()));
        colIncPersona.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getPersona() != null ? data.getValue().getPersona().getNombre() : "N/A"));
        colIncReportadoPor.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getReportadoPor() != null ? data.getValue().getReportadoPor().getUsername() : "—"));
        colIncFecha.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHora();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });

        colIncSeveridad.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("CRITICA".equals(item) || "ALTA".equals(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if ("MEDIA".equals(item)) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #38bdf8; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colIncAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnResolver = new Button("Resolver");
            private final HBox box = new HBox(btnResolver);
            {
                box.setAlignment(Pos.CENTER);
                btnResolver.setOnAction(e -> {
                    Incidente inc = getTableView().getItems().get(getIndex());
                    try {
                        incidenteService.cambiarEstado(inc.getId(), "RESUELTO", SceneManager.getCurrentUser());
                        refreshIncidentes();
                    } catch (Exception ex) {
                        DialogHelper.mostrarError("Error", ex.getMessage());
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Incidente inc = getTableView().getItems().get(getIndex());
                    if ("RESUELTO".equals(inc.getEstado()) || "CERRADO".equals(inc.getEstado())) {
                        btnResolver.setDisable(true);
                        btnResolver.setText("Cerrado");
                        btnResolver.setStyle("-fx-background-color: rgba(148,163,184,0.2); -fx-text-fill: #94a3b8;");
                    } else {
                        btnResolver.setDisable(false);
                        btnResolver.setText("Resolver");
                        btnResolver.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    setGraphic(box);
                }
            }
        });
    }

    private void refreshIncidentes() {
        masterIncidentes.setAll(incidenteService.listarTodos());
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    @FXML
    private void handleNuevoIncidente(ActionEvent event) {
        Optional<DialogHelper.IncidenteDialogData> res = DialogHelper.mostrarDialogoReporteIncidente("Panel de Administracion", personaService.listarTodas());
        if (res.isPresent()) {
            DialogHelper.IncidenteDialogData data = res.get();
            try {
                incidenteService.reportarIncidente(data.titulo, data.descripcion, data.severidad, data.persona, null, SceneManager.getCurrentUser());
                refreshIncidentes();
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al registrar incidente", e.getMessage());
            }
        }
    }

    private void configurarColumnasPersonas() {
        colPerId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colPerDoc.setCellValueFactory(data -> new SimpleStringProperty(
                (data.getValue().getTipoDocumento() != null ? data.getValue().getTipoDocumento() : "") + " " +
                        (data.getValue().getDocumento() != null ? data.getValue().getDocumento() : "")));
        colPerNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colPerTipo.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTipo()));
        colPerEmpresa.setCellValueFactory(data -> {
            Long empId = data.getValue().getEmpresaId();
            if (empId == null) return new SimpleStringProperty("—");
            return new SimpleStringProperty(empresaRepository.porId(empId).map(Empresa::getNombre).orElse("—"));
        });
        colPerEstado.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().isBloqueado() ? "[BLOQUEADO] (" + data.getValue().getMotivoBloqueo() + ")" : "[AUTORIZADO]"));
        colPerVisitas.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getTotalVisitas())));

        colPerEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("BLOQUEADO")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    }
                }
            }
        });

        colPerAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final Button btnBloqueo = new Button();
            private final Button btnEliminar = new Button("Eliminar");
            private final HBox box = new HBox(6, btnEditar, btnBloqueo, btnEliminar);
            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 8;");
                btnEliminar.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #b91c1c); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 8;");

                btnEditar.setOnAction(e -> {
                    Persona p = getTableView().getItems().get(getIndex());
                    editarPersona(p);
                });

                btnBloqueo.setOnAction(e -> {
                    Persona p = getTableView().getItems().get(getIndex());
                    toggleBloqueoPersona(p);
                });

                btnEliminar.setOnAction(e -> {
                    Persona p = getTableView().getItems().get(getIndex());
                    eliminarPersona(p);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Persona p = getTableView().getItems().get(getIndex());
                    if (p.isBloqueado()) {
                        btnBloqueo.setText("Desbloquear");
                        btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 8;");
                    } else {
                        btnBloqueo.setText("Bloquear");
                        btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11px; -fx-padding: 4 8;");
                    }
                    setGraphic(box);
                }
            }
        });
    }

    // =========================================================================
    // GESTIÓN DE PADRÓN DE PERSONAS: CREACIÓN, EDICIÓN Y ELIMINACIÓN
    // =========================================================================
    /**
     * Despliega el diálogo modal de registro para incorporar una nueva persona al padrón.
     * Gestiona la carga y persistencia física de su fotografía en el directorio local 'photos/'.
     */
    @FXML
    private void handleNuevaPersona(ActionEvent event) {
        Optional<DialogHelper.PersonaDialogData> res = DialogHelper.mostrarDialogoPersona(null, empresaRepository.listar());
        if (res.isPresent()) {
            DialogHelper.PersonaDialogData data = res.get();
            try {
                // Copiar fotografía al directorio local y obtener ruta relativa sanitizada
                String fotoFinal = procesarArchivoFoto(data.documento, data.fotoArchivo, data.fotoUrl);
                Persona nueva = new Persona();
                nueva.setNombre(data.nombre);
                nueva.setTipoDocumento(data.tipoDocumento);
                nueva.setDocumento(data.documento);
                nueva.setTipo(data.tipo);
                nueva.setEmpresaId(data.empresa != null ? data.empresa.getId() : null);
                nueva.setFotoUrl(fotoFinal);

                // Persistir nueva persona y actualizar vista
                personaService.guardar(nueva, SceneManager.getCurrentUser());
                refreshPersonas();
                DialogHelper.mostrarExito("Persona Creada", "La persona '" + nueva.getNombre() + "' fue registrada con éxito.");
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al registrar persona", e.getMessage());
            }
        }
    }

    /**
     * Permite modificar la información y la fotografía de una persona existente.
     */
    private void editarPersona(Persona persona) {
        Optional<DialogHelper.PersonaDialogData> res = DialogHelper.mostrarDialogoPersona(persona, empresaRepository.listar());
        if (res.isPresent()) {
            DialogHelper.PersonaDialogData data = res.get();
            try {
                // Actualizar fotografía si fue seleccionada una nueva
                String fotoFinal = procesarArchivoFoto(data.documento, data.fotoArchivo, data.fotoUrl);
                persona.setNombre(data.nombre);
                persona.setTipoDocumento(data.tipoDocumento);
                persona.setDocumento(data.documento);
                persona.setTipo(data.tipo);
                persona.setEmpresaId(data.empresa != null ? data.empresa.getId() : null);
                persona.setFotoUrl(fotoFinal);

                // Guardar cambios en persistencia y refrescar tabla
                personaService.guardar(persona, SceneManager.getCurrentUser());
                refreshPersonas();
                DialogHelper.mostrarExito("Persona Actualizada", "Los datos y fotografía de '" + persona.getNombre() + "' fueron actualizados.");
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al editar persona", e.getMessage());
            }
        }
    }

    /**
     * Solicita confirmación y ejecuta la eliminación de una persona en base de datos.
     */
    private void eliminarPersona(Persona persona) {
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION);
        confirmacion.setTitle("Eliminar Persona");
        confirmacion.setHeaderText("¿Confirmar eliminación de persona?");
        confirmacion.setContentText("¿Está seguro de que desea eliminar a '" + persona.getNombre() +
                "' (Documento: " + persona.getDocumento() + ") del sistema SICA?\n\n" +
                "Esta acción eliminará su registro y desvinculará sus registros asociados. Es irreversible y quedará registrada en la bitácora de auditoría.");
        DialogHelper.aplicarEstilo(confirmacion);

        Optional<ButtonType> respuesta = confirmacion.showAndWait();
        if (respuesta.isPresent() && respuesta.get() == ButtonType.OK) {
            try {
                personaService.eliminarPersona(persona.getId(), SceneManager.getCurrentUser());
                refreshPersonas();
                DialogHelper.mostrarExito("Persona Eliminada", "La persona '" + persona.getNombre() + "' ha sido eliminada exitosamente del sistema.");
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al Eliminar Persona",
                        "No se pudo completar la eliminación.\nDetalle: " + e.getMessage());
            }
        }
    }

    private String procesarArchivoFoto(String documento, java.io.File archivoFoto, String fotoUrlActual) {
        if (archivoFoto != null) {
            try {
                java.io.File photosDir = new java.io.File("photos");
                if (!photosDir.exists()) {
                    photosDir.mkdirs();
                }
                String ext = "jpg";
                String name = archivoFoto.getName().toLowerCase();
                if (name.endsWith(".png")) ext = "png";
                else if (name.endsWith(".jpeg")) ext = "jpeg";

                String docSanitized = documento.replaceAll("[^a-zA-Z0-9_-]", "_");
                String nuevoNombre = "persona_" + docSanitized + "_" + System.currentTimeMillis() + "." + ext;
                java.io.File destino = new java.io.File(photosDir, nuevoNombre);
                java.nio.file.Files.copy(archivoFoto.toPath(), destino.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                return "photos/" + nuevoNombre;
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al Guardar Foto", "No se pudo copiar el archivo de foto: " + e.getMessage());
            }
        }
        return fotoUrlActual;
    }

    private void toggleBloqueoPersona(Persona persona) {
        try {
            if (persona.isBloqueado()) {
                personaService.desbloquearPersona(persona, SceneManager.getCurrentUser());
                refreshPersonas();
            } else {
                Optional<String> motivo = DialogHelper.pedirTexto(
                        "Bloquear Persona",
                        "Restriccion de Acceso Inmediata",
                        "Ingrese el motivo para bloquear a " + persona.getNombre() + ":",
                        "Ej. Incumplimiento de normas de seguridad"
                );
                if (motivo.isPresent() && !motivo.get().trim().isEmpty()) {
                    personaService.bloquearPersona(persona, motivo.get().trim(), SceneManager.getCurrentUser());
                    refreshPersonas();
                }
            }
        } catch (Exception e) {
            DialogHelper.mostrarError("Error en Bloqueo/Desbloqueo", e.getMessage());
        }
    }

    private void refreshPersonas() {
        masterPersonas.setAll(personaService.listarTodas());
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    private void configurarColumnasRoles() {
        colRolNombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colRolPermisos.setCellValueFactory(data -> {
            String lista = data.getValue().getPermisos().stream()
                .map(p -> p.getCodigo())
                .sorted()
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(lista.isEmpty() ? "(sin permisos)" : lista);
        });
    }

    private void refreshRoles() {
        masterRoles.setAll(rolRepository.listarTodos());
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    private void configurarColumnasUsuarios() {
        colUId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colUUsername.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getUsername()));
        colURol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getRol().getNombre()));
        colUPersona.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona() != null
                ? data.getValue().getPersona().getNombre() : "—"));
        colUEstado.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));

        colUAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btnEditar = new Button("Editar");
            private final HBox box = new HBox(btnEditar);
            {
                box.setAlignment(Pos.CENTER);
                btnEditar.setStyle("-fx-background-color: linear-gradient(to right, #3b82f6, #2563eb); -fx-text-fill: white; -fx-font-weight: bold;");
                btnEditar.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    Optional<DialogHelper.UsuarioDialogData> res = DialogHelper.mostrarDialogoEditarUsuario(u, rolRepository.listarTodos(), personaService.listarTodas());
                    if (res.isPresent()) {
                        DialogHelper.UsuarioDialogData data = res.get();
                        try {
                            usuarioService.actualizarUsuario(u.getId(), data.nombreRol, data.persona, SceneManager.getCurrentUser());
                            refreshUsuarios();
                        } catch (Exception ex) {
                            DialogHelper.mostrarError("Error al editar usuario", ex.getMessage());
                        }
                    }
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });
    }

    private void refreshUsuarios() {
        masterUsuarios.setAll(usuarioRepository.findAll());
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    private void configurarColumnasEmpresas() {
        colEId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colENombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colENit.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNit()));
    }

    private void refreshEmpresas() {
        masterEmpresas.setAll(empresaRepository.listar());
        if (txtBuscarAdmin != null) aplicarFiltrosAdmin(txtBuscarAdmin.getText());
    }

    private void aplicarFiltrosAdmin(String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            if (filteredEvacuacion != null) filteredEvacuacion.setPredicate(v -> true);
            if (filteredIncidentes != null) filteredIncidentes.setPredicate(i -> true);
            if (filteredPersonas != null) filteredPersonas.setPredicate(p -> true);
            if (filteredUsuarios != null) filteredUsuarios.setPredicate(u -> true);
            if (filteredRoles != null) filteredRoles.setPredicate(r -> true);
            if (filteredEmpresas != null) filteredEmpresas.setPredicate(e -> true);
            return;
        }
        String term = filtro.trim().toLowerCase();

        if (filteredEvacuacion != null) {
            filteredEvacuacion.setPredicate(v -> {
                if (String.valueOf(v.getId()).contains(term)) return true;
                if (v.getPersona() != null) {
                    if (v.getPersona().getNombre() != null && v.getPersona().getNombre().toLowerCase().contains(term)) return true;
                    if (v.getPersona().getDocumento() != null && v.getPersona().getDocumento().toLowerCase().contains(term)) return true;
                    if (v.getPersona().getTipo() != null && v.getPersona().getTipo().toLowerCase().contains(term)) return true;
                }
                if (v.getEmpresaDestino() != null && v.getEmpresaDestino().getNombre() != null) {
                    if (v.getEmpresaDestino().getNombre().toLowerCase().contains(term)) return true;
                }
                if (v.getFuncionarioAnfitrion() != null && v.getFuncionarioAnfitrion().getUsername() != null) {
                    if (v.getFuncionarioAnfitrion().getUsername().toLowerCase().contains(term)) return true;
                }
                return false;
            });
        }

        if (filteredIncidentes != null) {
            filteredIncidentes.setPredicate(i -> {
                if (String.valueOf(i.getId()).contains(term)) return true;
                if (i.getTitulo() != null && i.getTitulo().toLowerCase().contains(term)) return true;
                if (i.getDescripcion() != null && i.getDescripcion().toLowerCase().contains(term)) return true;
                if (i.getSeveridad() != null && i.getSeveridad().name().toLowerCase().contains(term)) return true;
                if (i.getEstado() != null && i.getEstado().toLowerCase().contains(term)) return true;
                if (i.getPersona() != null && i.getPersona().getNombre() != null) {
                    if (i.getPersona().getNombre().toLowerCase().contains(term)) return true;
                }
                if (i.getReportadoPor() != null && i.getReportadoPor().getUsername() != null) {
                    if (i.getReportadoPor().getUsername().toLowerCase().contains(term)) return true;
                }
                return false;
            });
        }

        if (filteredPersonas != null) {
            filteredPersonas.setPredicate(p -> {
                if (String.valueOf(p.getId()).contains(term)) return true;
                if (p.getNombre() != null && p.getNombre().toLowerCase().contains(term)) return true;
                if (p.getDocumento() != null && p.getDocumento().toLowerCase().contains(term)) return true;
                if (p.getTipo() != null && p.getTipo().toLowerCase().contains(term)) return true;
                if (p.getMotivoBloqueo() != null && p.getMotivoBloqueo().toLowerCase().contains(term)) return true;
                return false;
            });
        }

        if (filteredUsuarios != null) {
            filteredUsuarios.setPredicate(u -> {
                if (String.valueOf(u.getId()).contains(term)) return true;
                if (u.getUsername() != null && u.getUsername().toLowerCase().contains(term)) return true;
                if (u.getRol() != null && u.getRol().getNombre() != null) {
                    if (u.getRol().getNombre().toLowerCase().contains(term)) return true;
                }
                if (u.getPersona() != null && u.getPersona().getNombre() != null) {
                    if (u.getPersona().getNombre().toLowerCase().contains(term)) return true;
                }
                return false;
            });
        }

        if (filteredRoles != null) {
            filteredRoles.setPredicate(r -> {
                if (r.getNombre() != null && r.getNombre().toLowerCase().contains(term)) return true;
                return false;
            });
        }

        if (filteredEmpresas != null) {
            filteredEmpresas.setPredicate(e -> {
                if (String.valueOf(e.getId()).contains(term)) return true;
                if (e.getNombre() != null && e.getNombre().toLowerCase().contains(term)) return true;
                if (e.getNit() != null && e.getNit().toLowerCase().contains(term)) return true;
                return false;
            });
        }
    }
}