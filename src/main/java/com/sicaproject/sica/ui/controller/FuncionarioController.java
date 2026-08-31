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
import com.sicaproject.sica.ui.RefreshScheduler;
import com.sicaproject.sica.ui.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class FuncionarioController implements Initializable, RefreshScheduler.Refreshable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private TableView<Visita> tblPendientes;
    @FXML private TableColumn<Visita, String> colId;
    @FXML private TableColumn<Visita, String> colDocumento;
    @FXML private TableColumn<Visita, String> colPersona;
    @FXML private TableColumn<Visita, String> colEmpresa;
    @FXML private TableColumn<Visita, String> colFecha;
    @FXML private TableColumn<Visita, String> colTipo;
    @FXML private TableColumn<Visita, String> colVisitas;
    @FXML private TableColumn<Visita, Void> colAcciones;
    @FXML private TextField txtNombre;
    @FXML private RadioButton rbCC;
    @FXML private RadioButton rbCE;
    @FXML private RadioButton rbPasaporte;
    @FXML private RadioButton rbTrabajador;
    @FXML private RadioButton rbInvitado;
    @FXML private TextField txtDocumento;
    @FXML private ComboBox<Empresa> cmbEmpresa;
    @FXML private CheckBox chkPaseTemporal;
    @FXML private Label lblMsg;

    private final VisitaService visitaService =
        CompositionRoot.getInstance().visitaService();
    private final PersonaService personaService =
        CompositionRoot.getInstance().personaService();
    private final EmpresaService empresaService =
        CompositionRoot.getInstance().empresaService();

    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Bienvenido, " + SceneManager.getCurrentUser().getUsername());
        }

        cmbEmpresa.setItems(FXCollections.observableArrayList(empresaService.listar()));
        cmbEmpresa.setCellFactory(cb -> new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre() + " (" + e.getNit() + ")");
            }
        });
        cmbEmpresa.setButtonCell(new ListCell<>() {
            @Override protected void updateItem(Empresa e, boolean empty) {
                super.updateItem(e, empty);
                setText(empty || e == null ? null : e.getNombre());
            }
        });

        // Configurar ToggleGroup para tipos de documento
        ToggleGroup tgDoc = new ToggleGroup();
        rbCC.setToggleGroup(tgDoc);
        rbCE.setToggleGroup(tgDoc);
        rbPasaporte.setToggleGroup(tgDoc);
        rbCC.setSelected(true);

        // Configurar ToggleGroup para tipo de persona
        ToggleGroup tgPersona = new ToggleGroup();
        rbTrabajador.setToggleGroup(tgPersona);
        rbInvitado.setToggleGroup(tgPersona);
        rbInvitado.setSelected(true);

        configurarColumnas();
        refreshTabla();
        RefreshScheduler.getInstance().register(this);
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colDocumento.setCellValueFactory(data -> {
            String tipo = data.getValue().getPersona().getTipoDocumento();
            String doc = data.getValue().getPersona().getDocumento();
            return new SimpleStringProperty((tipo != null ? tipo : "") + " " + (doc != null ? doc : ""));
        });
        colPersona.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        colEmpresa.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colFecha.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colTipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getTipo()));
        colVisitas.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getPersona().getTotalVisitas())));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnAprobar = new Button("Aprobar");
            private final Button btnRechazar = new Button("Rechazar");
            private final Button btnBloqueo = new Button();
            private final HBox contenedor = new HBox(6, btnAprobar, btnRechazar, btnBloqueo);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnAprobar.getStyleClass().add("btn-aprobar");
                btnRechazar.getStyleClass().add("btn-rechazar");
                btnBloqueo.getStyleClass().add("btn-bloqueo");
                btnAprobar.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    aprobar(v);
                });
                btnRechazar.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    rechazar(v);
                });
                btnBloqueo.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    toggleBloqueo(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                    return;
                }
                
                Visita visita = getTableView().getItems().get(getIndex());
                Persona persona = visita.getPersona();
                
                // Actualizar texto e icono del botón según estado de bloqueo
                if (persona.isBloqueado()) {
                    btnBloqueo.setText("Desbloquear");
                    btnBloqueo.setTooltip(new Tooltip("Desbloquear persona"));
                } else {
                    btnBloqueo.setText("Bloquear");
                    btnBloqueo.setTooltip(new Tooltip("Bloquear persona"));
                }
                
                setGraphic(contenedor);
            }
        });
    }

    private void aprobar(Visita visita) {
        try {
            visitaService.aprobarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("✓ Visita #" + visita.getId() + " aprobada. El guarda ya puede hacer el check-in.");
            refreshTabla();
        RefreshScheduler.getInstance().register(this);
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }

    private void rechazar(Visita visita) {
        try {
            visitaService.rechazarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("✓ Visita #" + visita.getId() + " rechazada.");
            refreshTabla();
        RefreshScheduler.getInstance().register(this);
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }

    private void toggleBloqueo(Visita visita) {
        try {
            Persona persona = visita.getPersona();
            if (persona.isBloqueado()) {
                personaService.desbloquearPersona(persona);
                lblMsg.setText("✓ Persona desbloqueada: " + persona.getNombre());
            } else {
                TextInputDialog dialog = new TextInputDialog();
                dialog.setTitle("Bloquear persona");
                dialog.setHeaderText("Bloquear a: " + persona.getNombre());
                dialog.setContentText("Motivo del bloqueo:");
                
                Optional<String> resultado = dialog.showAndWait();
                if (resultado.isPresent() && !resultado.get().trim().isEmpty()) {
                    String motivo = resultado.get().trim();
                    personaService.bloquearPersona(persona, motivo);
                    lblMsg.setText("✓ Persona bloqueada: " + persona.getNombre() + " (Motivo: " + motivo + ")");
                } else {
                    return;
                }
            }
            refreshTabla();
        RefreshScheduler.getInstance().register(this);
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }
@FXML
    private void handleRegistrar(ActionEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            Empresa empresa = cmbEmpresa.getValue();
            String documento = txtDocumento.getText().trim();

            if (documento.isEmpty()) {
                lblMsg.setText("✗ Ingrese el número de documento");
                return;
            }
            if (empresa == null) {
                lblMsg.setText("✗ Seleccione empresa destino");
                return;
            }

            String tipoDocumento = "CC";
            if (rbCE.isSelected()) tipoDocumento = "CE";
            else if (rbPasaporte.isSelected()) tipoDocumento = "PASAPORTE";

            String tipoPersona = rbTrabajador.isSelected() ? "TRABAJADOR" : "INVITADO";

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
                personaService.guardar(persona);
                txtNombre.setText(persona.getNombre());
                lblMsg.setText("✓ Persona existente: " + persona.getNombre() + " (Visitas: " + persona.getTotalVisitas() + ")");
            } else {
                if (nombre.isEmpty()) {
                    lblMsg.setText("✗ Ingrese el nombre del visitante (nueva persona)");
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
                persona = personaService.guardar(persona);
            }

            personaService.incrementarVisitas(persona);

            // Get a guard user (any available)
            UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
            List<Usuario> guardas = usuarioRepository.findByRol("GUARDA");
            Usuario guard = guardas != null && !guardas.isEmpty() ? guardas.get(0) : SceneManager.getCurrentUser();

            // Create pre-approved visit directly (no need for guard approval)
            Visita v = visitaService.registrarVisitaPreaprobada(
                persona, empresa, SceneManager.getCurrentUser(), guard
            );

            lblMsg.setText("✓ Visita #" + v.getId() + " pre-aprobada para " + persona.getNombre() + " (" + persona.getTotalVisitas() + " visitas)");
            txtDocumento.clear();
            rbCC.setSelected(true);
            rbInvitado.setSelected(true);
            refreshTabla();
        RefreshScheduler.getInstance().register(this);
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTabla();
        RefreshScheduler.getInstance().register(this);
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    @Override
    public void refreshData() {
        refreshTabla();
    }

    private void refreshTabla() {
        var pendientes = visitaService.listarTodas().stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        tblPendientes.getItems().setAll(pendientes);
    }
}
