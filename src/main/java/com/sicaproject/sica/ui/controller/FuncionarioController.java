package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
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
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FuncionarioController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private TableView<Visita> tblPendientes;
    @FXML private TableColumn<Visita, String> colId;
    @FXML private TableColumn<Visita, String> colPersona;
    @FXML private TableColumn<Visita, String> colEmpresa;
    @FXML private TableColumn<Visita, String> colFecha;
    @FXML private TableColumn<Visita, String> colTipo;
    @FXML private TableColumn<Visita, Void> colAcciones;
    @FXML private TextField txtNombre;
    @FXML private RadioButton rbCC;
    @FXML private RadioButton rbCE;
    @FXML private RadioButton rbPasaporte;
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

        configurarColumnas();
        refreshTabla();
    }

    private void configurarColumnas() {
        colId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colPersona.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        colEmpresa.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colFecha.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraRegistro();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colTipo.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isPaseTemporal() ? "Pase Temporal" : "Normal"));

        colAcciones.setCellFactory(col -> new TableCell<>() {
            private final Button btnAprobar = new Button("Aprobar");
            private final Button btnRechazar = new Button("Rechazar");
            private final HBox contenedor = new HBox(8, btnAprobar, btnRechazar);

            {
                contenedor.setAlignment(Pos.CENTER);
                btnAprobar.getStyleClass().add("btn-aprobar");
                btnRechazar.getStyleClass().add("btn-rechazar");
                btnAprobar.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    aprobar(v);
                });
                btnRechazar.setOnAction(e -> {
                    Visita v = getTableView().getItems().get(getIndex());
                    rechazar(v);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : contenedor);
            }
        });
    }

    private void aprobar(Visita visita) {
        try {
            visitaService.aprobarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("✓ Visita #" + visita.getId() + " aprobada. El guarda ya puede hacer el check-in.");
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }

    private void rechazar(Visita visita) {
        try {
            visitaService.rechazarVisita(visita.getId(), SceneManager.getCurrentUser());
            lblMsg.setText("✓ Visita #" + visita.getId() + " rechazada.");
            refreshTabla();
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

            if (nombre.isEmpty()) {
                lblMsg.setText("✗ Ingrese el nombre del visitante");
                return;
            }
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

            Persona persona = new Persona();
            persona.setNombre(nombre);
            persona.setDocumento(documento);
            persona.setTipoDocumento(tipoDocumento);
            persona = personaService.guardar(persona);

            Visita v = visitaService.solicitarAcceso(
                persona, empresa,
                SceneManager.getCurrentUser(),
                SceneManager.getCurrentUser(),
                chkPaseTemporal.isSelected()
            );
            lblMsg.setText("✓ Solicitud creada con ID " + v.getId());
            txtNombre.clear();
            txtDocumento.clear();
            rbCC.setSelected(true);
            refreshTabla();
        } catch (Exception e) {
            lblMsg.setText("✗ " + e.getMessage());
        }
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTabla();
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    private void refreshTabla() {
        var pendientes = visitaService.listarTodas().stream()
            .filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION)
            .toList();
        tblPendientes.setItems(FXCollections.observableArrayList(pendientes));
    }
}
