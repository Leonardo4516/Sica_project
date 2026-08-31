package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.incidentes.application.service.IncidenteService;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.RefreshScheduler;
import com.sicaproject.sica.ui.SceneManager;
import com.sicaproject.sica.ui.util.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GuardaController implements Initializable, RefreshScheduler.Refreshable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private TextField txtVisitaIdIn;
    @FXML private TextField txtVisitaIdOut;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Label lblInMsg;
    @FXML private Label lblOutMsg;
    @FXML private RadioButton rbCC;
    @FXML private RadioButton rbCE;
    @FXML private RadioButton rbPasaporte;
    @FXML private RadioButton rbTrabajador;
    @FXML private RadioButton rbInvitado;
    @FXML private TextField txtDocSolicitud;
    @FXML private TextField txtNombreSolicitud;
    @FXML private ComboBox<Empresa> cmbEmpresaSolicitud;
    @FXML private ComboBox<Usuario> cmbFuncionarioSolicitud;
    @FXML private CheckBox chkPaseTemporalSolicitud;
    @FXML private Button btnSolicitar;
    @FXML private Button btnBuscar;
    @FXML private Label lblSolicitudMsg;
    @FXML private TableView<Visita> tblActivas;
    @FXML private TableColumn<Visita, String> colGId;
    @FXML private TableColumn<Visita, String> colGPersona;
    @FXML private TableColumn<Visita, String> colGEmpresa;
    @FXML private TableColumn<Visita, String> colGHora;
    @FXML private TableColumn<Visita, String> colGEstado;
    @FXML private TableView<Visita> tblAprobadas;
    @FXML private TableView<Visita> tblPendientes;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final EmpresaService empresaService = CompositionRoot.getInstance().empresaService();
    private final PersonaService personaService = CompositionRoot.getInstance().personaService();
    private final IncidenteService incidenteService = CompositionRoot.getInstance().incidenteService();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();

    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Bienvenido, " + SceneManager.getCurrentUser().getUsername());
        }

        cmbEmpresaSolicitud.setItems(FXCollections.observableArrayList(empresaService.listar()));
        cmbEmpresaSolicitud.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre());
            }
        });
        cmbEmpresaSolicitud.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre());
            }
        });

        List<Usuario> funcionarios = usuarioRepository.findByRol("FUNCIONARIO");
        cmbFuncionarioSolicitud.setItems(FXCollections.observableArrayList(funcionarios));
        cmbFuncionarioSolicitud.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername());
            }
        });
        cmbFuncionarioSolicitud.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername());
            }
        });

        ToggleGroup tgDoc = new ToggleGroup();
        rbCC.setToggleGroup(tgDoc);
        rbCE.setToggleGroup(tgDoc);
        rbPasaporte.setToggleGroup(tgDoc);
        rbCC.setSelected(true);

        ToggleGroup tgPersona = new ToggleGroup();
        rbTrabajador.setToggleGroup(tgPersona);
        rbInvitado.setToggleGroup(tgPersona);
        rbInvitado.setSelected(true);

        configurarColumnas(tblActivas, colGId, colGPersona, colGEmpresa, colGHora, colGEstado);
        configurarColumnasAprobadas();
        configurarColumnasPendientes();
        configurarSeleccionFilas();

        refreshTablas();

        RefreshScheduler.getInstance().register(this);
    }

    private void configurarSeleccionFilas() {
        tblAprobadas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtVisitaIdIn.setText(String.valueOf(newV.getId()));
                String foto = (newV.getPersona().getFotoUrl() != null && !newV.getPersona().getFotoUrl().isEmpty())
                        ? newV.getPersona().getFotoUrl() : "Sin foto registrada";
                String anfitrion = newV.getFuncionarioAnfitrion() != null ? newV.getFuncionarioAnfitrion().getUsername() : "—";
                String estadoBloqueo = newV.getPersona().isBloqueado()
                        ? "[BLOQUEADO] (" + newV.getPersona().getMotivoBloqueo() + ")"
                        : "[AUTORIZADO PARA INGRESO]";
                lblInMsg.setText("Visitante: " + newV.getPersona().getNombre() + " (" + newV.getPersona().getDocumento() + ")\n" +
                        "Empresa: " + newV.getEmpresaDestino().getNombre() + " | Anfitrion: " + anfitrion + "\n" +
                        "Foto: " + foto + "\n" +
                        "Estado: " + estadoBloqueo);
                if (newV.getPersona().isBloqueado()) {
                    lblInMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                } else {
                    lblInMsg.setStyle("-fx-text-fill: #10b981;");
                }
            }
        });

        tblActivas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtVisitaIdOut.setText(String.valueOf(newV.getId()));
                lblOutMsg.setText("Visita #" + newV.getId() + ": " + newV.getPersona().getNombre() + " (" + newV.getEmpresaDestino().getNombre() + ")");
                lblOutMsg.setStyle("-fx-text-fill: #38bdf8;");
            }
        });
    }

    @Override
    public void refreshData() {
        refreshTablas();
    }

    private void configurarColumnas(TableView<Visita> tabla, TableColumn<Visita, String> colId,
            TableColumn<Visita, String> colPersona, TableColumn<Visita, String> colEmpresa,
            TableColumn<Visita, String> colHora, TableColumn<Visita, String> colEstado) {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colPersona.setCellValueFactory(data -> {
            Persona p = data.getValue().getPersona();
            String nombre = p != null ? p.getNombre() : "—";
            return new SimpleStringProperty(p != null && p.isBloqueado() ? nombre + " [BLOQUEADO]" : nombre);
        });
        colEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado().name()));

        colEstado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("DENTRO".equals(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #cbd5e1;");
                    }
                }
            }
        });
    }

    private void configurarColumnasAprobadas() {
        tblAprobadas.getColumns().clear();
        TableColumn<Visita, String> colAId = new TableColumn<>("ID");
        colAId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<Visita, String> colAPersona = new TableColumn<>("Visitante");
        colAPersona.setCellValueFactory(data -> {
            Persona p = data.getValue().getPersona();
            String nombre = p != null ? p.getNombre() : "—";
            return new SimpleStringProperty(p != null && p.isBloqueado() ? nombre + " [BLOQUEADO]" : nombre);
        });
        TableColumn<Visita, String> colAEmpresa = new TableColumn<>("Empresa");
        colAEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        TableColumn<Visita, String> colAHora = new TableColumn<>("Hora Registro");
        colAHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colATipo = new TableColumn<>("Tipo");
        colATipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getTipo()));
        tblAprobadas.getColumns().addAll(colAId, colAPersona, colAEmpresa, colAHora, colATipo);
    }

    private void configurarColumnasPendientes() {
        tblPendientes.getColumns().clear();
        TableColumn<Visita, String> colPId = new TableColumn<>("ID");
        colPId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<Visita, String> colPPersona = new TableColumn<>("Visitante");
        colPPersona.setCellValueFactory(data -> {
            Persona p = data.getValue().getPersona();
            String nombre = p != null ? p.getNombre() : "—";
            return new SimpleStringProperty(p != null && p.isBloqueado() ? nombre + " [BLOQUEADO]" : nombre);
        });
        TableColumn<Visita, String> colPEmpresa = new TableColumn<>("Empresa");
        colPEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        TableColumn<Visita, String> colPHora = new TableColumn<>("Hora Registro");
        colPHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colPTipo = new TableColumn<>("Tipo");
        colPTipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getTipo()));
        tblPendientes.getColumns().addAll(colPId, colPPersona, colPEmpresa, colPHora, colPTipo);
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        try {
            String txt = txtVisitaIdIn.getText().trim();
            if (txt.isEmpty()) {
                lblInMsg.setText("Ingrese un ID de visita");
                lblInMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }
            long id = Long.parseLong(txt);
            Visita v = visitaService.checkIn(id, SceneManager.getCurrentUser());
            lblInMsg.setText("Check-in exitoso. Estado: " + v.getEstado() + " (" + v.getPersona().getNombre() + ")");
            lblInMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            txtVisitaIdIn.clear();
            refreshTablas();
        } catch (NumberFormatException e) {
            lblInMsg.setText("ID invalido");
            lblInMsg.setStyle("-fx-text-fill: #ef4444;");
        } catch (Exception e) {
            lblInMsg.setText("Error: " + e.getMessage());
            lblInMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
            DialogHelper.mostrarError("Acceso Denegado", e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        try {
            String txt = txtVisitaIdOut.getText().trim();
            if (txt.isEmpty()) {
                lblOutMsg.setText("Ingrese un ID de visita");
                lblOutMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }
            long id = Long.parseLong(txt);
            Visita v = visitaService.checkOut(id, SceneManager.getCurrentUser());
            lblOutMsg.setText("Check-out exitoso. Visita #" + v.getId() + " finalizada.");
            lblOutMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            txtVisitaIdOut.clear();
            refreshTablas();
        } catch (NumberFormatException e) {
            lblOutMsg.setText("ID invalido");
            lblOutMsg.setStyle("-fx-text-fill: #ef4444;");
        } catch (Exception e) {
            lblOutMsg.setText("Error: " + e.getMessage());
            lblOutMsg.setStyle("-fx-text-fill: #ef4444;");
            DialogHelper.mostrarError("Error en Check-out", e.getMessage());
        }
    }

    @FXML
    private void handleBuscarPersona(ActionEvent event) {
        try {
            String tipoDocumento = "CC";
            if (rbCE.isSelected()) tipoDocumento = "CE";
            else if (rbPasaporte.isSelected()) tipoDocumento = "PASAPORTE";

            String documento = txtDocSolicitud.getText().trim();
            if (documento.isEmpty()) {
                lblSolicitudMsg.setText("Ingrese numero de documento para buscar");
                lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }

            Optional<Persona> personaOpt = personaService.porTipoYDocumento(tipoDocumento, documento);
            if (personaOpt.isPresent()) {
                Persona persona = personaOpt.get();
                txtNombreSolicitud.setText(persona.getNombre());
                if ("TRABAJADOR".equals(persona.getTipo())) {
                    rbTrabajador.setSelected(true);
                } else {
                    rbInvitado.setSelected(true);
                }
                if (persona.isBloqueado()) {
                    lblSolicitudMsg.setText("[ALERTA] Persona BLOQUEADA. Motivo: " +
                            (persona.getMotivoBloqueo() != null ? persona.getMotivoBloqueo() : "Restriccion de acceso") +
                            " (" + persona.getTotalVisitas() + " visitas previas)");
                    lblSolicitudMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    DialogHelper.mostrarAdvertencia("Persona con Restriccion", "Acceso Bloqueado",
                            "Esta persona figura como BLOQUEADA en el sistema.\nMotivo: " + persona.getMotivoBloqueo());
                } else {
                    lblSolicitudMsg.setText("Persona encontrada: " + persona.getNombre() + " (" + persona.getTotalVisitas() + " visitas)");
                    lblSolicitudMsg.setStyle("-fx-text-fill: #10b981;");
                }
            } else {
                txtNombreSolicitud.clear();
                rbInvitado.setSelected(true);
                lblSolicitudMsg.setText("Persona no encontrada en el padron. Complete los datos para registrarla.");
                lblSolicitudMsg.setStyle("-fx-text-fill: #38bdf8;");
            }
        } catch (Exception e) {
            lblSolicitudMsg.setText("Error: " + e.getMessage());
            lblSolicitudMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleSolicitarAcceso(ActionEvent event) {
        try {
            String nombre = txtNombreSolicitud.getText().trim();
            Empresa empresa = cmbEmpresaSolicitud.getValue();
            Usuario anfitrion = cmbFuncionarioSolicitud.getValue();

            if (empresa == null) {
                lblSolicitudMsg.setText("Seleccione empresa destino");
                lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }
            if (anfitrion == null) {
                lblSolicitudMsg.setText("Seleccione funcionario anfitrion");
                lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }

            String tipoDocumento = "CC";
            if (rbCE.isSelected()) tipoDocumento = "CE";
            else if (rbPasaporte.isSelected()) tipoDocumento = "PASAPORTE";

            String tipoPersona = rbTrabajador.isSelected() ? "TRABAJADOR" : "INVITADO";

            String documento = txtDocSolicitud.getText().trim();
            if (documento.isEmpty()) {
                lblSolicitudMsg.setText("Ingrese numero de documento");
                lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }

            Optional<Persona> personaOpt = personaService.porTipoYDocumento(tipoDocumento, documento);
            Persona persona;
            if (personaOpt.isPresent()) {
                persona = personaOpt.get();
                if (!nombre.isEmpty()) {
                    persona.setNombre(nombre);
                }
                persona.setTipoDocumento(tipoDocumento);
                persona.setTipo(tipoPersona);
                if ("TRABAJADOR".equals(tipoPersona) && persona.getEmpresaId() == null) {
                    persona.setEmpresaId(empresa.getId());
                }
                personaService.guardar(persona, SceneManager.getCurrentUser());
                txtNombreSolicitud.setText(persona.getNombre());
            } else {
                if (nombre.isEmpty()) {
                    lblSolicitudMsg.setText("Ingrese nombre del visitante");
                    lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                    return;
                }
                persona = new Persona();
                persona.setNombre(nombre);
                persona.setDocumento(documento);
                persona.setTipoDocumento(tipoDocumento);
                persona.setTipo(tipoPersona);
                if ("TRABAJADOR".equals(tipoPersona)) {
                    persona.setEmpresaId(empresa.getId());
                }
                persona = personaService.guardar(persona, SceneManager.getCurrentUser());
            }

            personaService.incrementarVisitas(persona);

            boolean esTemporal = chkPaseTemporalSolicitud.isSelected();
            Visita v = visitaService.solicitarAcceso(
                persona, empresa, anfitrion, SceneManager.getCurrentUser(), esTemporal
            );

            lblSolicitudMsg.setText("Solicitud #" + v.getId() + " enviada para " + persona.getNombre() + " (" + persona.getTotalVisitas() + " visitas)");
            lblSolicitudMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            txtDocSolicitud.clear();
            txtNombreSolicitud.clear();
            chkPaseTemporalSolicitud.setSelected(false);
            rbCC.setSelected(true);
            rbInvitado.setSelected(true);
            refreshTablas();
        } catch (Exception e) {
            lblSolicitudMsg.setText("Error: " + e.getMessage());
            lblSolicitudMsg.setStyle("-fx-text-fill: #ef4444;");
            DialogHelper.mostrarError("Error en Solicitud", e.getMessage());
        }
    }

    @FXML
    private void handleReportarIncidente(ActionEvent event) {
        Optional<DialogHelper.IncidenteDialogData> res = DialogHelper.mostrarDialogoReporteIncidente("Porteria Principal");
        if (res.isPresent()) {
            DialogHelper.IncidenteDialogData data = res.get();
            try {
                incidenteService.reportarIncidente(data.titulo, data.descripcion, data.severidad, null, null, SceneManager.getCurrentUser());
                DialogHelper.mostrarExito("Incidente Reportado", "El incidente ha sido registrado en la bitacora central.");
            } catch (Exception e) {
                DialogHelper.mostrarError("Error al registrar", e.getMessage());
            }
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTablas();
    }

    @FXML
    public void handleLogout(Event event) {
        SceneManager.logout();
    }

    private void refreshTablas() {
        var todas = visitaService.listarTodas();

        var activas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
            .toList();
        tblActivas.getItems().setAll(activas);

        var aprobadas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.APROBADA)
            .toList();
        tblAprobadas.getItems().setAll(aprobadas);

        var pendientes = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        tblPendientes.getItems().setAll(pendientes);
    }
}
