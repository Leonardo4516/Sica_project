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
import javafx.beans.property.SimpleStringProperty;
    import javafx.collections.FXCollections;
    import javafx.event.ActionEvent;
    import javafx.fxml.FXML;
    import javafx.fxml.Initializable;
    import javafx.scene.control.*;
    import javafx.scene.input.MouseEvent;
    import javafx.scene.layout.StackPane;
    import javafx.scene.text.Text;
    import javafx.scene.layout.HBox;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class GuardaController implements Initializable {

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
    @FXML private TextField txtDocSolicitud;
    @FXML private TextField txtNombreSolicitud;
    @FXML private ComboBox<Empresa> cmbEmpresaSolicitud;
    @FXML private ComboBox<Usuario> cmbFuncionarioSolicitud;
    @FXML private CheckBox chkPaseTemporalSolicitud;
    @FXML private Button btnSolicitar;
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

        // Configurar ToggleGroup para tipos de documento
        ToggleGroup tgDoc = new ToggleGroup();
        rbCC.setToggleGroup(tgDoc);
        rbCE.setToggleGroup(tgDoc);
        rbPasaporte.setToggleGroup(tgDoc);
        rbCC.setSelected(true); // Por defecto CC seleccionado

        configurarColumnas(tblActivas, colGId, colGPersona, colGEmpresa, colGHora, colGEstado);
        configurarColumnasAprobadas();
        configurarColumnasPendientes();

        refreshTablas();
    }

    private void configurarColumnas(TableView<Visita> tabla, TableColumn<Visita, String> colId,
            TableColumn<Visita, String> colPersona, TableColumn<Visita, String> colEmpresa,
            TableColumn<Visita, String> colHora, TableColumn<Visita, String> colEstado) {
        colId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colPersona.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        colEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado().name()));
    }

    private void configurarColumnasAprobadas() {
        tblAprobadas.getColumns().clear();
        TableColumn<Visita, String> colAId = new TableColumn<>("ID");
        colAId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<Visita, String> colAPersona = new TableColumn<>("Persona");
        colAPersona.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        TableColumn<Visita, String> colAEmpresa = new TableColumn<>("Empresa");
        colAEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        TableColumn<Visita, String> colAHora = new TableColumn<>("Hora registro");
        colAHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colATipo = new TableColumn<>("Tipo");
        colATipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isPaseTemporal() ? "Temporal" : "Normal"));
        tblAprobadas.getColumns().addAll(colAId, colAPersona, colAEmpresa, colAHora, colATipo);
    }

    private void configurarColumnasPendientes() {
        tblPendientes.getColumns().clear();
        TableColumn<Visita, String> colPId = new TableColumn<>("ID");
        colPId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        TableColumn<Visita, String> colPPersona = new TableColumn<>("Persona");
        colPPersona.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        TableColumn<Visita, String> colPEmpresa = new TableColumn<>("Empresa");
        colPEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        TableColumn<Visita, String> colPHora = new TableColumn<>("Hora registro");
        colPHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colPTipo = new TableColumn<>("Tipo");
        colPTipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isPaseTemporal() ? "Temporal" : "Normal"));
        tblPendientes.getColumns().addAll(colPId, colPPersona, colPEmpresa, colPHora, colPTipo);
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        try {
            String txt = txtVisitaIdIn.getText().trim();
            if (txt.isEmpty()) { lblInMsg.setText("Ingrese un ID"); return; }
            long id = Long.parseLong(txt);
            Visita v = visitaService.checkIn(id, SceneManager.getCurrentUser());
            lblInMsg.setText("Check-in OK. Estado: " + v.getEstado());
            txtVisitaIdIn.clear();
            refreshTablas();
        } catch (NumberFormatException e) {
            lblInMsg.setText("ID inválido");
        } catch (Exception e) {
            lblInMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        try {
            String txt = txtVisitaIdOut.getText().trim();
            if (txt.isEmpty()) { lblOutMsg.setText("Ingrese un ID"); return; }
            long id = Long.parseLong(txt);
            Visita v = visitaService.checkOut(id, SceneManager.getCurrentUser());
            lblOutMsg.setText("Check-out OK. Estado: " + v.getEstado());
            txtVisitaIdOut.clear();
            refreshTablas();
        } catch (NumberFormatException e) {
            lblOutMsg.setText("ID inválido");
        } catch (Exception e) {
            lblOutMsg.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleSolicitarAcceso(ActionEvent event) {
        try {
            String nombre = txtNombreSolicitud.getText().trim();
            Empresa empresa = cmbEmpresaSolicitud.getValue();
            Usuario anfitrion = cmbFuncionarioSolicitud.getValue();

            if (nombre.isEmpty()) { lblSolicitudMsg.setText("Ingrese nombre del visitante"); return; }
            if (empresa == null) { lblSolicitudMsg.setText("Seleccione empresa destino"); return; }

            String tipoDocumento = "CC";
            if (rbCE.isSelected()) tipoDocumento = "CE";
            else if (rbPasaporte.isSelected()) tipoDocumento = "PASAPORTE";

            String documento = txtDocSolicitud.getText().trim();

            if (documento.isEmpty()) { lblSolicitudMsg.setText("Ingrese número de documento"); return; }

            Optional<Persona> personaOpt = personaService.porDocumento(documento);
            Persona persona;
            if (personaOpt.isPresent()) {
                persona = personaOpt.get();
                persona.setTipoDocumento(tipoDocumento);
                personaService.guardar(persona);
            } else {
                persona = new Persona();
                persona.setNombre(nombre);
                persona.setDocumento(documento);
                persona.setTipoDocumento(tipoDocumento);
                persona = personaService.guardar(persona);
            }

            boolean esTemporal = chkPaseTemporalSolicitud.isSelected();
            Visita v = visitaService.solicitarAcceso(
                persona, empresa, anfitrion, SceneManager.getCurrentUser(), esTemporal
            );

            lblSolicitudMsg.setText("Solicitud creada #" + v.getId() + " (" + v.getEstado() + ")");
            txtDocSolicitud.clear();
            txtNombreSolicitud.clear();
            chkPaseTemporalSolicitud.setSelected(false);
            rbCC.setSelected(true);
            refreshTablas();
        } catch (Exception e) {
            lblSolicitudMsg.setText("Error: " + e.getMessage());
        }
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
        var todas = visitaService.listarTodas();

        var activas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
            .toList();
        tblActivas.setItems(FXCollections.observableArrayList(activas));

        var aprobadas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.APROBADA)
            .toList();
        tblAprobadas.setItems(FXCollections.observableArrayList(aprobadas));

        var pendientes = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        tblPendientes.setItems(FXCollections.observableArrayList(pendientes));
    }
}