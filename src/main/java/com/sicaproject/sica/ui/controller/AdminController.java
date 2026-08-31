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

public class AdminController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblTotalVisitas;
    @FXML private Label lblVisitasDentro;
    @FXML private Label lblPendientes;
    @FXML private Label lblCerradasHoy;

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
    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolPermisos;
    @FXML private TableView<Empresa> tblEmpresas;
    @FXML private TableColumn<Empresa, String> colEId;
    @FXML private TableColumn<Empresa, String> colENombre;
    @FXML private TableColumn<Empresa, String> colENit;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final IncidenteService incidenteService = CompositionRoot.getInstance().incidenteService();
    private final PersonaService personaService = CompositionRoot.getInstance().personaService();
    private final RbacService rbacService = CompositionRoot.getInstance().rbacService();
    private final RolRepository rolRepository = CompositionRoot.getInstance().rolRepository();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
    private final EmpresaRepository empresaRepository = CompositionRoot.getInstance().empresaRepository();

    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Bienvenido, " + SceneManager.getCurrentUser().getUsername());
        }
        configurarColumnasEvacuacion();
        configurarColumnasIncidentes();
        configurarColumnasPersonas();
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
        tblEvacuacion.setItems(FXCollections.observableArrayList(activas));
        lblEvacuacionConteo.setText("Personal dentro para conteo de evacuacion: " + activas.size() + " personas");
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
        tblIncidentes.setItems(FXCollections.observableArrayList(incidenteService.listarTodos()));
    }

    @FXML
    private void handleNuevoIncidente(ActionEvent event) {
        Optional<DialogHelper.IncidenteDialogData> res = DialogHelper.mostrarDialogoReporteIncidente("Panel de Administracion");
        if (res.isPresent()) {
            DialogHelper.IncidenteDialogData data = res.get();
            try {
                incidenteService.reportarIncidente(data.titulo, data.descripcion, data.severidad, null, null, SceneManager.getCurrentUser());
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
            private final Button btnBloqueo = new Button();
            private final HBox box = new HBox(btnBloqueo);
            {
                box.setAlignment(Pos.CENTER);
                btnBloqueo.setOnAction(e -> {
                    Persona p = getTableView().getItems().get(getIndex());
                    toggleBloqueoPersona(p);
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
                        btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        btnBloqueo.setText("Bloquear");
                        btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); -fx-text-fill: white; -fx-font-weight: bold;");
                    }
                    setGraphic(box);
                }
            }
        });
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
        tblPersonas.setItems(FXCollections.observableArrayList(personaService.listarTodas()));
    }

    private void refreshRoles() {
        colRolNombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colRolPermisos.setCellValueFactory(data -> {
            String lista = data.getValue().getPermisos().stream()
                .map(p -> p.getCodigo())
                .sorted()
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(lista.isEmpty() ? "(sin permisos)" : lista);
        });
        tblRoles.setItems(FXCollections.observableArrayList(rolRepository.listarTodos()));
    }

    private void refreshUsuarios() {
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
        tblUsuarios.setItems(FXCollections.observableArrayList(usuarioRepository.findAll()));
    }

    private void refreshEmpresas() {
        colEId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colENombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colENit.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNit()));
        tblEmpresas.setItems(FXCollections.observableArrayList(empresaRepository.listar()));
    }
}