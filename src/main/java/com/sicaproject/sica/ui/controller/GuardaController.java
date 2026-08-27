package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
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
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import javafx.util.Duration;

import java.net.URL;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class GuardaController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblHora;
    @FXML private TextField txtVisitaIdIn;
    @FXML private TextField txtVisitaIdOut;
    @FXML private Button btnCheckIn;
    @FXML private Button btnCheckOut;
    @FXML private Label lblInMsg;
    @FXML private Label lblOutMsg;
    @FXML private TableView<Visita> tblActivas;
    @FXML private TableColumn<Visita, String> colGId;
    @FXML private TableColumn<Visita, String> colGPersona;
    @FXML private TableColumn<Visita, String> colGEmpresa;
    @FXML private TableColumn<Visita, String> colGHora;
    @FXML private TableColumn<Visita, String> colGEstado;

    private final VisitaService visitaService =
        CompositionRoot.getInstance().visitaService();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Guarda: " + SceneManager.getCurrentUser().getUsername());
        }

        ParticleBackground particles = new ParticleBackground();
        particles.setManaged(false);
        particles.setMouseTransparent(true);
        rootPane.getChildren().add(0, particles);
        particles.start();

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        colGId.setCellValueFactory(data -> new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colGPersona.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getPersona().getNombre()));
        colGEmpresa.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEmpresaDestino().getNombre()));
        colGHora.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHoraEntrada();
            return new SimpleStringProperty(f != null ? f.format(dateTimeFmt) : "—");
        });
        colGEstado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getEstado().name()));

        refreshTabla();
    }

    @FXML
    private void handleCheckIn(ActionEvent event) {
        try {
            long id = Long.parseLong(txtVisitaIdIn.getText().trim());
            Visita v = visitaService.checkIn(id, SceneManager.getCurrentUser());
            lblInMsg.setText("✓ Check-in OK. Estado: " + v.getEstado());
            refreshTabla();
        } catch (NumberFormatException e) {
            lblInMsg.setText("ID inválido");
        } catch (Exception e) {
            lblInMsg.setText("✗ " + e.getMessage());
        }
    }

    @FXML
    private void handleCheckOut(ActionEvent event) {
        try {
            long id = Long.parseLong(txtVisitaIdOut.getText().trim());
            Visita v = visitaService.checkOut(id, SceneManager.getCurrentUser());
            lblOutMsg.setText("✓ Check-out OK. Estado: " + v.getEstado());
            refreshTabla();
        } catch (NumberFormatException e) {
            lblOutMsg.setText("ID inválido");
        } catch (Exception e) {
            lblOutMsg.setText("✗ " + e.getMessage());
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
        var activas = visitaService.listarTodas().stream()
            .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
            .toList();
        tblActivas.setItems(FXCollections.observableArrayList(activas));
    }
}
