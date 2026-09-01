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
    @FXML private TextField txtBuscarPendientes;
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

    private final ObservableList<Visita> masterPendientes = FXCollections.observableArrayList();
    private FilteredList<Visita> filteredPendientes;

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

        ToggleGroup tgDoc = new ToggleGroup();
        rbCC.setToggleGroup(tgDoc);
        rbCE.setToggleGroup(tgDoc);
        rbPasaporte.setToggleGroup(tgDoc);
        rbCC.setSelected(true);

        ToggleGroup tgPersona = new ToggleGroup();
        rbTrabajador.setToggleGroup(tgPersona);
        rbInvitado.setToggleGroup(tgPersona);
        rbInvitado.setSelected(true);

        configurarColumnas();

        filteredPendientes = new FilteredList<>(masterPendientes, p -> true);
        SortedList<Visita> sortedPendientes = new SortedList<>(filteredPendientes);
        sortedPendientes.comparatorProperty().bind(tblPendientes.comparatorProperty());
        tblPendientes.setItems(sortedPendientes);

        if (txtBuscarPendientes != null) {
            txtBuscarPendientes.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltro(newVal));
        }

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
        colPersona.setCellValueFactory(data -> {
            Persona p = data.getValue().getPersona();
            String nom = p != null ? p.getNombre() : "—";
            return new SimpleStringProperty(p != null && p.isBloqueado() ? nom + " [BLOQUEADO]" : nom);
        });
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
                btnAprobar.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white; -fx-font-weight: bold;");
                btnRechazar.setStyle("-fx-background-color: linear-gradient(to right, #ef4444, #dc2626); -fx-text-fill: white; -fx-font-weight: bold;");
                btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); -fx-text-fill: white; -fx-font-weight: bold;");

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
                
                if (persona.isBloqueado()) {
                    btnBloqueo.setText("Desbloquear");
                    btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #10b981, #059669); -fx-text-fill: white;");
                } else {
                    btnBloqueo.setText("Bloquear");
                    btnBloqueo.setStyle("-fx-background-color: linear-gradient(to right, #f59e0b, #d97706); -fx-text-fill: white;");
                }
                
                setGraphic(contenedor);
            }
        });
    }

    private void aprobar(Visita visita) {
        try {
            visitaService.aprobarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("Visita #" + visita.getId() + " aprobada. El guarda ya puede hacer el check-in.");
            lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void rechazar(Visita visita) {
        try {
            visitaService.rechazarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("Visita #" + visita.getId() + " rechazada.");
            lblMsg.setStyle("-fx-text-fill: #f59e0b;");
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    private void toggleBloqueo(Visita visita) {
        try {
            Persona persona = visita.getPersona();
            if (persona.isBloqueado()) {
                personaService.desbloquearPersona(persona, SceneManager.getCurrentUser());
                lblMsg.setText("Persona desbloqueada: " + persona.getNombre());
                lblMsg.setStyle("-fx-text-fill: #10b981;");
            } else {
                Optional<String> motivo = DialogHelper.pedirTexto(
                        "Bloquear Persona",
                        "Restriccion de Acceso",
                        "Motivo de seguridad para bloquear a " + persona.getNombre() + ":",
                        "Ej. Incumplimiento de normas perimetrales"
                );
                if (motivo.isPresent() && !motivo.get().trim().isEmpty()) {
                    String motivoTxt = motivo.get().trim();
                    personaService.bloquearPersona(persona, motivoTxt, SceneManager.getCurrentUser());
                    lblMsg.setText("Persona bloqueada: " + persona.getNombre() + " (Motivo: " + motivoTxt + ")");
                    lblMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                } else {
                    return;
                }
            }
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            Empresa empresa = cmbEmpresa.getValue();
            String documento = txtDocumento.getText().trim();

            if (documento.isEmpty()) {
                lblMsg.setText("Ingrese el numero de documento");
                lblMsg.setStyle("-fx-text-fill: #f59e0b;");
                return;
            }
            if (empresa == null) {
                lblMsg.setText("Seleccione empresa destino");
                lblMsg.setStyle("-fx-text-fill: #f59e0b;");
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
                personaService.guardar(persona, SceneManager.getCurrentUser());
                txtNombre.setText(persona.getNombre());
            } else {
                if (nombre.isEmpty()) {
                    lblMsg.setText("Ingrese el nombre del visitante");
                    lblMsg.setStyle("-fx-text-fill: #f59e0b;");
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

            UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
            List<Usuario> guardas = usuarioRepository.findByRol("GUARDA");
            Usuario guard = guardas != null && !guardas.isEmpty() ? guardas.get(0) : SceneManager.getCurrentUser();

            Visita v = visitaService.registrarVisitaPreaprobada(
                persona, empresa, SceneManager.getCurrentUser(), guard
            );

            lblMsg.setText("Visita #" + v.getId() + " pre-aprobada para " + persona.getNombre() + " (" + persona.getTotalVisitas() + " visitas)");
            lblMsg.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
            txtDocumento.clear();
            txtNombre.clear();
            rbCC.setSelected(true);
            rbInvitado.setSelected(true);
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("Error: " + e.getMessage());
            lblMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTabla();
    }

    @FXML
    public void handleLogout(Event event) {
        SceneManager.logout();
    }

    @Override
    public void refreshData() {
        refreshTabla();
    }

    private boolean coincideVisita(Visita v, String filtro) {
        if (filtro == null || filtro.trim().isEmpty()) {
            return true;
        }
        String term = filtro.trim().toLowerCase();
        if (String.valueOf(v.getId()).contains(term)) return true;
        if (v.getPersona() != null) {
            if (v.getPersona().getNombre() != null && v.getPersona().getNombre().toLowerCase().contains(term)) return true;
            if (v.getPersona().getDocumento() != null && v.getPersona().getDocumento().toLowerCase().contains(term)) return true;
            if (v.getPersona().getTipo() != null && v.getPersona().getTipo().toLowerCase().contains(term)) return true;
        }
        if (v.getEmpresaDestino() != null && v.getEmpresaDestino().getNombre() != null) {
            if (v.getEmpresaDestino().getNombre().toLowerCase().contains(term)) return true;
        }
        if (v.getObservaciones() != null && v.getObservaciones().toLowerCase().contains(term)) return true;
        return false;
    }

    private void aplicarFiltro(String filtro) {
        if (filteredPendientes != null) {
            filteredPendientes.setPredicate(v -> coincideVisita(v, filtro));
        }
    }

    private void refreshTabla() {
        var pendientes = visitaService.listarTodas().stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        masterPendientes.setAll(pendientes);

        if (txtBuscarPendientes != null) {
            aplicarFiltro(txtBuscarPendientes.getText());
        }
    }
}
