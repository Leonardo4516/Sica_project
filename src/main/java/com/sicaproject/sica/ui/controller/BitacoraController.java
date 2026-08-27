package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
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
import javafx.util.Duration;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class BitacoraController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Label lblHora;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TextField txtFiltro;
    @FXML private Label lblMsg;
    @FXML private TableView<BitacoraAuditoria> tblBitacora;
    @FXML private TableColumn<BitacoraAuditoria, String> colBFecha;
    @FXML private TableColumn<BitacoraAuditoria, String> colBUsuario;
    @FXML private TableColumn<BitacoraAuditoria, String> colBAccion;
    @FXML private TableColumn<BitacoraAuditoria, String> colBRecurso;
    @FXML private TableColumn<BitacoraAuditoria, String> colBDetalle;
    @FXML private TableColumn<BitacoraAuditoria, String> colBResultado;

    private final AuditoriaService auditoriaService =
        CompositionRoot.getInstance().auditoriaService();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        ParticleBackground.attachTo(rootPane);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(30));

        colBFecha.setCellValueFactory(data -> {
            LocalDateTime f = data.getValue().getFechaHora();
            return new SimpleStringProperty(f != null ? f.format(dtFmt) : "—");
        });
        colBUsuario.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getUsuarioId() != null
                ? String.valueOf(data.getValue().getUsuarioId()) : "—"));
        colBAccion.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAccion()));
        colBRecurso.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getRecurso()));
        colBDetalle.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDetalle()));
        colBResultado.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getResultado()));

        refreshTabla();
    }

    @FXML
    private void handleFiltrar(ActionEvent event) {
        refreshTabla();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(30));
        if (txtFiltro != null) txtFiltro.clear();
        refreshTabla();
    }

    @FXML
    private void handleExportarCsv(ActionEvent event) {
        try {
            List<BitacoraAuditoria> items = tblBitacora.getItems();
            File csvFile = new File("bitacora_export.csv");
            try (PrintWriter writer = new PrintWriter(new FileWriter(csvFile))) {
                writer.println("Fecha,UsuarioID,Accion,Recurso,Detalle,Resultado");
                for (BitacoraAuditoria b : items) {
                    writer.printf("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"%n",
                        b.getFechaHora() != null ? b.getFechaHora().format(dtFmt) : "",
                        b.getUsuarioId() != null ? b.getUsuarioId() : "",
                        b.getAccion() != null ? b.getAccion() : "",
                        b.getRecurso() != null ? b.getRecurso() : "",
                        b.getDetalle() != null ? b.getDetalle().replace("\"", "\"\"") : "",
                        b.getResultado() != null ? b.getResultado() : ""
                    );
                }
            }
            if (lblMsg != null) lblMsg.setText("✓ " + items.size() + " registros exportados a bitacora_export.csv");
        } catch (Exception e) {
            if (lblMsg != null) lblMsg.setText("✗ Error al exportar: " + e.getMessage());
        }
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
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        String filtro = txtFiltro != null ? txtFiltro.getText().trim().toLowerCase() : "";

        var filtradas = auditoriaService.listarTodas().stream()
            .filter(b -> {
                if (b.getFechaHora() != null) {
                    LocalDate fecha = b.getFechaHora().toLocalDate();
                    if (desde != null && fecha.isBefore(desde)) return false;
                    if (hasta != null && fecha.isAfter(hasta)) return false;
                }
                if (!filtro.isEmpty()) {
                    String texto = (b.getAccion() + " " + b.getRecurso() + " " + b.getDetalle() + " " + b.getUsuarioId()).toLowerCase();
                    if (!texto.contains(filtro)) return false;
                }
                return true;
            })
            .toList();

        tblBitacora.setItems(FXCollections.observableArrayList(filtradas));
        if (lblMsg != null) {
            lblMsg.setText(filtradas.size() + " eventos encontrados");
        }
    }
}

