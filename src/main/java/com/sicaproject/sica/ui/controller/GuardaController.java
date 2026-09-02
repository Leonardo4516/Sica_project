package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Usuario;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import com.sicaproject.sica.incidentes.application.service.IncidenteService;
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
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Controlador de la consola operativa de portería principal (Panel Guardia).
 * Gestiona el ciclo de vida de los accesos físicos:
 * - Flujo 1: Check-in inmediato de visitas pre-aprobadas con visualización de foto.
 * - Flujo 2 y 3: Solicitud de acceso en tiempo real (invitados no anunciados o pase temporal).
 * - Salida física (Check-out) de personas en estado DENTRO.
 * - Registro de novedades e incidentes de seguridad perimetral.
 * - Filtrado en tiempo real con FilteredList y sincronización reactiva con RefreshScheduler.
 */
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
    @FXML private ImageView imgFotoVisitanteIn;
    @FXML private ImageView imgFotoVisitanteOut;
    @FXML private ImageView imgFotoPreview;
    private File fotoSeleccionada;
    @FXML private TextField txtBuscarVisita;
    @FXML private TableView<Visita> tblActivas;
    @FXML private TableColumn<Visita, String> colGId;
    @FXML private TableColumn<Visita, String> colGPersona;
    @FXML private TableColumn<Visita, String> colGEmpresa;
    @FXML private TableColumn<Visita, String> colGHora;
    @FXML private TableColumn<Visita, String> colGEstado;
    @FXML private TableView<Visita> tblAprobadas;
    @FXML private TableView<Visita> tblPendientes;

    private final ObservableList<Visita> masterActivas = FXCollections.observableArrayList();
    private final ObservableList<Visita> masterAprobadas = FXCollections.observableArrayList();
    private final ObservableList<Visita> masterPendientes = FXCollections.observableArrayList();

    private FilteredList<Visita> filteredActivas;
    private FilteredList<Visita> filteredAprobadas;
    private FilteredList<Visita> filteredPendientes;

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

        filteredActivas = new FilteredList<>(masterActivas, p -> true);
        filteredAprobadas = new FilteredList<>(masterAprobadas, p -> true);
        filteredPendientes = new FilteredList<>(masterPendientes, p -> true);

        SortedList<Visita> sortedActivas = new SortedList<>(filteredActivas);
        sortedActivas.comparatorProperty().bind(tblActivas.comparatorProperty());
        tblActivas.setItems(sortedActivas);

        SortedList<Visita> sortedAprobadas = new SortedList<>(filteredAprobadas);
        sortedAprobadas.comparatorProperty().bind(tblAprobadas.comparatorProperty());
        tblAprobadas.setItems(sortedAprobadas);

        SortedList<Visita> sortedPendientes = new SortedList<>(filteredPendientes);
        sortedPendientes.comparatorProperty().bind(tblPendientes.comparatorProperty());
        tblPendientes.setItems(sortedPendientes);

        if (txtBuscarVisita != null) {
            txtBuscarVisita.textProperty().addListener((obs, oldVal, newVal) -> aplicarFiltroVisitas(newVal));
        }

        refreshTablas();

        RefreshScheduler.getInstance().register(this);
    }

    private void configurarSeleccionFilas() {
        tblAprobadas.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            if (newV != null) {
                txtVisitaIdIn.setText(String.valueOf(newV.getId()));
                imgFotoVisitanteIn.setImage(cargarImagen(newV.getPersona().getFotoUrl()));
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
                String nom = newV.getPersona() != null ? newV.getPersona().getNombre() : "Sin persona";
                String emp = newV.getEmpresaDestino() != null ? newV.getEmpresaDestino().getNombre() : "Sin empresa";
                lblOutMsg.setText("Visita #" + newV.getId() + ": " + nom + " (" + emp + ")");
                lblOutMsg.setStyle("-fx-text-fill: #38bdf8;");
                imgFotoVisitanteOut.setImage(newV.getPersona() != null ? cargarImagen(newV.getPersona().getFotoUrl()) : null);
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
        colEmpresa.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmpresaDestino() != null ? data.getValue().getEmpresaDestino().getNombre() : "—"));
        colHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colEstado.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEstado() != null ? data.getValue().getEstado().name() : "—"));

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
        colAEmpresa.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmpresaDestino() != null ? data.getValue().getEmpresaDestino().getNombre() : "—"));
        TableColumn<Visita, String> colAHora = new TableColumn<>("Hora Registro");
        colAHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colATipo = new TableColumn<>("Tipo");
        colATipo.setCellValueFactory(data -> new SimpleStringProperty(
                (data.getValue().getPersona() != null && data.getValue().getPersona().getTipo() != null)
                        ? data.getValue().getPersona().getTipo() : "—"));
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
        colPEmpresa.setCellValueFactory(data -> new SimpleStringProperty(
                data.getValue().getEmpresaDestino() != null ? data.getValue().getEmpresaDestino().getNombre() : "—"));
        TableColumn<Visita, String> colPHora = new TableColumn<>("Hora Registro");
        colPHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        TableColumn<Visita, String> colPTipo = new TableColumn<>("Tipo");
        colPTipo.setCellValueFactory(data -> new SimpleStringProperty(
                (data.getValue().getPersona() != null && data.getValue().getPersona().getTipo() != null)
                        ? data.getValue().getPersona().getTipo() : "—"));
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

    private Image cargarImagen(String fotoUrl) {
        if (fotoUrl == null || fotoUrl.trim().isEmpty()) {
            return null;
        }
        try {
            File f = new File(fotoUrl);
            if (f.exists()) {
                return new Image(f.toURI().toString());
            }
            if (fotoUrl.startsWith("http://") || fotoUrl.startsWith("https://") || fotoUrl.startsWith("file:")) {
                return new Image(fotoUrl);
            }
            var res = getClass().getResource(fotoUrl);
            if (res != null) {
                return new Image(res.toExternalForm());
            }
        } catch (Exception ignored) {
        }
        return null;
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

            // Validar formato formal de documento
            com.sicaproject.sica.shared.util.InputValidator.validarDocumento(tipoDocumento, documento);

            Optional<Persona> personaOpt = personaService.porTipoYDocumento(tipoDocumento, documento);
            if (personaOpt.isPresent()) {
                Persona persona = personaOpt.get();
                txtNombreSolicitud.setText(persona.getNombre());
                if ("TRABAJADOR".equals(persona.getTipo())) {
                    rbTrabajador.setSelected(true);
                } else {
                    rbInvitado.setSelected(true);
                }
                
                // Cargar fotografía de la persona si está registrada
                imgFotoPreview.setImage(cargarImagen(persona.getFotoUrl()));
                fotoSeleccionada = null;

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
                imgFotoPreview.setImage(null);
                fotoSeleccionada = null;
                lblSolicitudMsg.setText("Persona no encontrada en el padron. Complete los datos para registrarla.");
                lblSolicitudMsg.setStyle("-fx-text-fill: #38bdf8;");
            }
        } catch (IllegalArgumentException e) {
            lblSolicitudMsg.setText(e.getMessage());
            lblSolicitudMsg.setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
        } catch (Exception e) {
            lblSolicitudMsg.setText("Error: " + e.getMessage());
            lblSolicitudMsg.setStyle("-fx-text-fill: #ef4444;");
        }
    }

    @FXML
    private void handleSeleccionarFoto(ActionEvent event) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Seleccionar Fotografia");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Imagenes", "*.png", "*.jpg", "*.jpeg")
        );
        File file = fileChooser.showOpenDialog(rootPane.getScene().getWindow());
        if (file != null) {
            fotoSeleccionada = file;
            imgFotoPreview.setImage(new Image(file.toURI().toString()));
        }
    }

    private void guardarFoto(com.sicaproject.sica.personas.domain.Persona persona) {
        if (fotoSeleccionada != null) {
            try {
                File uploadDir = new File("photos");
                if (!uploadDir.exists()) uploadDir.mkdirs();
                String ext = "";
                int i = fotoSeleccionada.getName().lastIndexOf(".");
                if (i > 0) ext = fotoSeleccionada.getName().substring(i);
                String nuevoNombre = persona.getTipoDocumento() + "_" + persona.getDocumento() + "_" + System.currentTimeMillis() + ext;
                File destino = new File(uploadDir, nuevoNombre);
                Files.copy(fotoSeleccionada.toPath(), destino.toPath(), StandardCopyOption.REPLACE_EXISTING);
                persona.setFotoUrl(destino.getAbsolutePath());
            } catch (Exception e) {
                System.err.println("Error al guardar la foto: " + e.getMessage());
            }
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

            // Validar formato de documento de identidad
            com.sicaproject.sica.shared.util.InputValidator.validarDocumento(tipoDocumento, documento);

            Optional<Persona> personaOpt = personaService.porTipoYDocumento(tipoDocumento, documento);
            Persona persona;
            if (personaOpt.isPresent()) {
                persona = personaOpt.get();
                if (!nombre.isEmpty()) {
                    com.sicaproject.sica.shared.util.InputValidator.validarNombrePersona(nombre);
                    persona.setNombre(nombre);
                }
                persona.setTipoDocumento(tipoDocumento);
                persona.setTipo(tipoPersona);
                if ("TRABAJADOR".equals(tipoPersona) && persona.getEmpresaId() == null) {
                    persona.setEmpresaId(empresa.getId());
                }
                guardarFoto(persona);
                persona = personaService.guardar(persona, SceneManager.getCurrentUser());
                txtNombreSolicitud.setText(persona.getNombre());
            } else {
                if (nombre.isEmpty()) {
                    lblSolicitudMsg.setText("Ingrese nombre del visitante");
                    lblSolicitudMsg.setStyle("-fx-text-fill: #f59e0b;");
                    return;
                }
                // Validar formato de nombre
                com.sicaproject.sica.shared.util.InputValidator.validarNombrePersona(nombre);

                persona = new Persona();
                persona.setNombre(nombre);
                persona.setDocumento(documento);
                persona.setTipoDocumento(tipoDocumento);
                persona.setTipo(tipoPersona);
                if ("TRABAJADOR".equals(tipoPersona)) {
                    persona.setEmpresaId(empresa.getId());
                }
                guardarFoto(persona);
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
            imgFotoPreview.setImage(null);
            fotoSeleccionada = null;
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
        Optional<DialogHelper.IncidenteDialogData> res = DialogHelper.mostrarDialogoReporteIncidente("Porteria Principal", personaService.listarTodas());
        if (res.isPresent()) {
            DialogHelper.IncidenteDialogData data = res.get();
            try {
                incidenteService.reportarIncidente(data.titulo, data.descripcion, data.severidad, data.persona, null, SceneManager.getCurrentUser());
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
        if (v.getFuncionarioAnfitrion() != null && v.getFuncionarioAnfitrion().getUsername() != null) {
            if (v.getFuncionarioAnfitrion().getUsername().toLowerCase().contains(term)) return true;
        }
        if (v.getObservaciones() != null && v.getObservaciones().toLowerCase().contains(term)) return true;
        return false;
    }

    private void aplicarFiltroVisitas(String filtro) {
        if (filteredActivas != null) filteredActivas.setPredicate(v -> coincideVisita(v, filtro));
        if (filteredAprobadas != null) filteredAprobadas.setPredicate(v -> coincideVisita(v, filtro));
        if (filteredPendientes != null) filteredPendientes.setPredicate(v -> coincideVisita(v, filtro));
    }

    private void refreshTablas() {
        var todas = visitaService.listarTodas();

        var activas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
            .toList();
        masterActivas.setAll(activas);

        var aprobadas = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.APROBADA)
            .toList();
        masterAprobadas.setAll(aprobadas);

        var pendientes = todas.stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        masterPendientes.setAll(pendientes);

        if (txtBuscarVisita != null) {
            aplicarFiltroVisitas(txtBuscarVisita.getText());
        }
    }
}
