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
import com.sicaproject.sica.ui.component.ParticleBackground;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class FuncionarioController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblHora;
    @FXML private TableView<Object> tblPendientes;
    @FXML private TextField txtNombre;
    @FXML private ComboBox<Empresa> cmbEmpresa;
    @FXML private CheckBox chkPaseTemporal;
    @FXML private Label lblMsg;

    private final VisitaService visitaService =
        CompositionRoot.getInstance().visitaService();
    private final PersonaService personaService =
        CompositionRoot.getInstance().personaService();
    private final EmpresaService empresaService =
        CompositionRoot.getInstance().empresaService();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Funcionario: " + SceneManager.getCurrentUser().getUsername());
        }

        ParticleBackground particles = new ParticleBackground();
        particles.setManaged(false);
        particles.setMouseTransparent(true);
        rootPane.getChildren().add(0, particles);
        particles.resize(1280, 720);
        particles.start();

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

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

        refreshTabla();
    }

    @FXML
    private void handleRegistrar(ActionEvent event) {
        try {
            String nombre = txtNombre.getText().trim();
            Empresa empresa = cmbEmpresa.getValue();
            if (nombre.isEmpty() || empresa == null) {
                lblMsg.setText("Complete nombre y empresa");
                return;
            }

            Persona persona = new Persona();
            persona.setNombre(nombre);
            persona.setDocumento("PENDIENTE-" + System.currentTimeMillis());
            personaService.guardar(persona);

            Visita v = visitaService.solicitarAcceso(
                persona, empresa,
                SceneManager.getCurrentUser(),
                SceneManager.getCurrentUser(),
                chkPaseTemporal.isSelected()
            );
            lblMsg.setText("✓ Solicitud creada con ID " + v.getId());
            txtNombre.clear();
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
        var rows = FXCollections.<Object>observableArrayList();
        for (Visita v : visitaService.listarTodas()) {
            if (v.getEstado() == EstadoVisita.PENDIENTE_APROBACION) {
                rows.add(new Object[]{
                    v.getId(),
                    v.getPersona().getNombre(),
                    v.getEmpresaDestino().getNombre(),
                    v.getFechaHoraRegistro() != null
                        ? v.getFechaHoraRegistro().format(dateTimeFmt) : "—",
                    v.isPaseTemporal() ? "Pase Temporal" : "Normal"
                });
            }
        }
        tblPendientes.setItems(rows);
    }
}
