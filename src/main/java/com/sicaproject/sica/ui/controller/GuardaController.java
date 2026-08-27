package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import com.sicaproject.sica.ui.component.ParticleBackground;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GuardaController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblHora;

    // Sección Acción Rápida
    @FXML private TextField txtVisitaId;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Label lblAccionMsg;

    // Sección Solicitud
    @FXML private TextField txtDocSolicitud;
    @FXML private TextField txtNombreSolicitud;
    @FXML private ComboBox<Empresa> cmbEmpresaSolicitud;
    @FXML private ComboBox<Usuario> cmbFuncionarioSolicitud;
    @FXML private CheckBox chkPaseTemporalSolicitud;
    @FXML private Button btnSolicitar;
    @FXML private Label lblSolicitudMsg;

    // TabPane y Tablas
    @FXML private TabPane tabPaneVisitas;

    // Tabla Activas (DENTRO)
    @FXML private TableView<Visita> tblActivas;
    @FXML private TableColumn<Visita, String> colGId;
    @FXML private TableColumn<Visita, String> colGPersona;
    @FXML private TableColumn<Visita, String> colGDocumento;
    @FXML private TableColumn<Visita, String> colGEmpresa;
    @FXML private TableColumn<Visita, String> colGHora;
    @FXML private TableColumn<Visita, String> colGTipo;
    @FXML private TableColumn<Visita, Void> colGAccionOut;

    // Tabla Aprobadas
    @FXML private TableView<Visita> tblAprobadas;
    @FXML private TableColumn<Visita, String> colAId;
    @FXML private TableColumn<Visita, String> colAPersona;
    @FXML private TableColumn<Visita, String> colADocumento;
    @FXML private TableColumn<Visita, String> colAEmpresa;
    @FXML private TableColumn<Visita, String> colAAnfitrion;
    @FXML private TableColumn<Visita, String> colAFecha;
    @FXML private TableColumn<Visita, String> colATipo;
    @FXML private TableColumn<Visita, Void> colAAccionIn;

    // Tabla Pendientes
    @FXML private TableView<Visita> tblPendientes;
    @FXML private TableColumn<Visita, String> colPId;
    @FXML private TableColumn<Visita, String> colPPersona;
    @FXML private TableColumn<Visita, String> colPDocumento;
    @FXML private TableColumn<Visita, String> colPEmpresa;
    @FXML private TableColumn<Visita, String> colPAnfitrion;
    @FXML private TableColumn<Visita, String> colPFecha;
    @FXML private TableColumn<Visita, String> colPTipo;
    @FXML private TableColumn<Visita, String> colPEstado;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final PersonaService personaService = CompositionRoot.getInstance().personaService();
    private final EmpresaService empresaService = CompositionRoot.getInstance().empresaService();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Guarda: " + SceneManager.getCurrentUser().getUsername());
        }

        ParticleBackground.attachTo(rootPane);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        // Inicializar combos
        cmbEmpresaSolicitud.setItems(FXCollections.observableArrayList(empresaService.listar()));
        cmbEmpresaSolicitud.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre() + " (" + e.getNit() + ")");
            }
        });
        cmbEmpresaSolicitud.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre());
            }
        });

        cmbFuncionarioSolicitud.setItems(FXCollections.observableArrayList(usuarioRepository.findByRol("FUNCIONARIO")));
        cmbFuncionarioSolicitud.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername() + (u.getPersona() != null ? " (" + u.getPersona().getNombre() + ")" : ""));
            }
        });
        cmbFuncionarioSolicitud.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Usuario u, boolean empty) {
                super.updateItem(u, empty);
                setText(empty || u == null ? null : u.getUsername());
            }
        });

        configurarTablas();
        refreshTablas();
    }

    private void configurarTablas() {
        // 1. Tabla Activas (DENTRO)
        colGId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colGPersona.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getNombre() : "—"));
        colGDocumento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getDocumento() : "—"));
        colGEmpresa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpresaDestino() != null ? d.getValue().getEmpresaDestino().getNombre() : "—"));
        colGHora.setCellValueFactory(d -> {
            LocalDateTime f = d.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colGTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isPaseTemporal() ? "Pase Temporal" : "Normal"));
        colGAccionOut.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("🚪 Check-out");
            private final HBox box = new HBox(btn);
            {
                box.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("btn-rechazar");
                btn.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    ejecutarCheckOut(v.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // 2. Tabla Aprobadas (Listas para check-in)
        colAId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colAPersona.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getNombre() : "—"));
        colADocumento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getDocumento() : "—"));
        colAEmpresa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpresaDestino() != null ? d.getValue().getEmpresaDestino().getNombre() : "—"));
        colAAnfitrion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFuncionarioAnfitrion() != null ? d.getValue().getFuncionarioAnfitrion().getUsername() : "—"));
        colAFecha.setCellValueFactory(d -> {
            LocalDateTime f = d.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colATipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isPaseTemporal() ? "Pase Temporal" : "Normal"));
        colAAccionIn.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button("✅ Check-in");
            private final HBox box = new HBox(btn);
            {
                box.setAlignment(Pos.CENTER);
                btn.getStyleClass().add("btn-aprobar");
                btn.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    ejecutarCheckIn(v.getId());
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        // 3. Tabla Pendientes
        colPId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colPPersona.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getNombre() : "—"));
        colPDocumento.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getDocumento() : "—"));
        colPEmpresa.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEmpresaDestino() != null ? d.getValue().getEmpresaDestino().getNombre() : "—"));
        colPAnfitrion.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getFuncionarioAnfitrion() != null ? d.getValue().getFuncionarioAnfitrion().getUsername() : "—"));
        colPFecha.setCellValueFactory(d -> {
            LocalDateTime f = d.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colPTipo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isPaseTemporal() ? "Pase Temporal" : "Normal"));
        colPEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getEstado().name()));
    }

    private void ejecutarCheckIn(long visitaId) {
        try {
            Visita v = visitaService.checkIn(visitaId, SceneManager.getCurrentUser());
            lblAccionMsg.setText("✓ Check-in exitoso para visita #" + v.getId() + " (" + v.getPersona().getNombre() + ")");
            refreshTablas();
        } catch (Exception e) {
            lblAccionMsg.setText("✗ Error en Check-in: " + e.getMessage());
        }
    }

    private void ejecutarCheckOut(long visitaId) {
        try {
            Visita v = visitaService.checkOut(visitaId, SceneManager.getCurrentUser());
            lblAccionMsg.setText("✓ Check-out exitoso para visita #" + v.getId() + " (" + v.getPersona().getNombre() + ")");
            refreshTablas();
        } catch (Exception e) {
            lblAccionMsg.setText("✗ Error en Check-out: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        String input = txtVisitaId.getText().trim();
        if (input.isEmpty()) {
            lblAccionMsg.setText("Ingrese un ID o documento");
            return;
        }

        try {
            long id = Long.parseLong(input);
            ejecutarCheckIn(id);
            txtVisitaId.clear();
        } catch (NumberFormatException e) {
            // Intentar buscar por documento en visitas aprobadas
            List<Visita> todas = visitaService.listarTodas();
            Optional<Visita> encontrada = todas.stream()
                .filter(v -> v.getEstado() == EstadoVisita.APROBADA && v.getPersona() != null
                        && input.equalsIgnoreCase(v.getPersona().getDocumento()))
                .findFirst();
            if (encontrada.isPresent()) {
                ejecutarCheckIn(encontrada.get().getId());
                txtVisitaId.clear();
            } else {
                lblAccionMsg.setText("No hay visita APROBADA con documento: " + input);
            }
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        String input = txtVisitaId.getText().trim();
        if (input.isEmpty()) {
            lblAccionMsg.setText("Ingrese un ID o documento");
            return;
        }

        try {
            long id = Long.parseLong(input);
            ejecutarCheckOut(id);
            txtVisitaId.clear();
        } catch (NumberFormatException e) {
            // Intentar buscar por documento en visitas activas (DENTRO)
            List<Visita> todas = visitaService.listarTodas();
            Optional<Visita> encontrada = todas.stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO && v.getPersona() != null
                        && input.equalsIgnoreCase(v.getPersona().getDocumento()))
                .findFirst();
            if (encontrada.isPresent()) {
                ejecutarCheckOut(encontrada.get().getId());
                txtVisitaId.clear();
            } else {
                lblAccionMsg.setText("No hay visita DENTRO con documento: " + input);
            }
        }
    }

    @FXML
    private void handleSolicitarAcceso(ActionEvent event) {
        try {
            String doc = txtDocSolicitud.getText().trim();
            String nombre = txtNombreSolicitud.getText().trim();
            Empresa empresa = cmbEmpresaSolicitud.getValue();
            Usuario funcionario = cmbFuncionarioSolicitud.getValue();
            boolean paseTemporal = chkPaseTemporalSolicitud.isSelected();

            if (doc.isEmpty() || nombre.isEmpty() || empresa == null || funcionario == null) {
                lblSolicitudMsg.setText("Complete todos los campos de la solicitud.");
                return;
            }

            Persona persona = personaService.porDocumento(doc).orElseGet(() -> {
                Persona p = new Persona();
                p.setDocumento(doc);
                p.setNombre(nombre);
                p.setTipo(paseTemporal ? "TRABAJADOR" : "INVITADO");
                p.setEmpresaId(empresa.getId());
                return personaService.guardar(p);
            });

            Visita visita = visitaService.solicitarAcceso(
                persona, empresa, funcionario,
                SceneManager.getCurrentUser(), paseTemporal
            );

            lblSolicitudMsg.setText("✓ Solicitud #" + visita.getId() + " enviada al funcionario " + funcionario.getUsername());
            txtDocSolicitud.clear();
            txtNombreSolicitud.clear();
            chkPaseTemporalSolicitud.setSelected(false);
            refreshTablas();
        } catch (Exception e) {
            lblSolicitudMsg.setText("✗ " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTablas();
    }

    @FXML
    private void handleRefresh(MouseEvent event) {
        refreshTablas();
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    private void refreshTablas() {
        List<Visita> todas = visitaService.listarTodas();

        var activas = todas.stream().filter(v -> v.getEstado() == EstadoVisita.DENTRO).toList();
        tblActivas.setItems(FXCollections.observableArrayList(activas));

        var aprobadas = todas.stream().filter(v -> v.getEstado() == EstadoVisita.APROBADA).toList();
        tblAprobadas.setItems(FXCollections.observableArrayList(aprobadas));

        var pendientes = todas.stream().filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION).toList();
        tblPendientes.setItems(FXCollections.observableArrayList(pendientes));
    }
}

