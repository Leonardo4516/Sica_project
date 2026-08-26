package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BitacoraController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label lblHora;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<Object> tblBitacora;

    private final AuditoriaService auditoriaService =
        CompositionRoot.getInstance().auditoriaService();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
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

        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(7));

        refreshTabla();
    }

    @FXML
    private void handleFiltrar(ActionEvent event) {
        refreshTabla();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshTabla();
    }

    @FXML
    private void handleAdmin(MouseEvent event) {
        SceneManager.loadScene("/com/sicaproject/sica/ui/admin.fxml", "SICA — Administración");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    private void refreshTabla() {
        var rows = FXCollections.<Object>observableArrayList();
        for (BitacoraAuditoria b : auditoriaService.listarTodas()) {
            rows.add(new Object[]{
                b.getFechaHora() != null ? b.getFechaHora().format(dtFmt) : "—",
                b.getUsuarioId(),
                b.getAccion(),
                b.getRecurso(),
                b.getDetalle(),
                b.getResultado()
            });
        }
        tblBitacora.setItems(rows);
    }
}
