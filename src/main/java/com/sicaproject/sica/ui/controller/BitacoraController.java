package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import com.sicaproject.sica.ui.util.DialogHelper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class BitacoraController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private DatePicker dpDesde;
    @FXML private DatePicker dpHasta;
    @FXML private TableView<BitacoraAuditoria> tblBitacora;
    @FXML private TableColumn<BitacoraAuditoria, String> colBFecha;
    @FXML private TableColumn<BitacoraAuditoria, String> colBUsuario;
    @FXML private TableColumn<BitacoraAuditoria, String> colBAccion;
    @FXML private TableColumn<BitacoraAuditoria, String> colBRecurso;
    @FXML private TableColumn<BitacoraAuditoria, String> colBDetalle;
    @FXML private TableColumn<BitacoraAuditoria, String> colBResultado;

    private final AuditoriaService auditoriaService =
        CompositionRoot.getInstance().auditoriaService();

    private final DateTimeFormatter dtFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        dpHasta.setValue(LocalDate.now());
        dpDesde.setValue(LocalDate.now().minusDays(7));

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

        colBAccion.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.contains("BLOQUEO") || item.contains("FALLIDO") || item.contains("INCIDENTE")) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item.contains("SOLICITUD") || item.contains("SALIDA_OLVIDADA")) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #818cf8; -fx-font-weight: 500;");
                    }
                }
            }
        });

        colBResultado.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if ("EXITOSO".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                    } else if ("DENEGADO".equalsIgnoreCase(item) || "FALLIDO".equalsIgnoreCase(item)) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #cbd5e1;");
                    }
                }
            }
        });

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
    public void handleAdmin(Event event) {
        SceneManager.loadScene("/com/sicaproject/sica/ui/admin.fxml", "SICA — Administración");
    }

    @FXML
    public void handleLogout(Event event) {
        SceneManager.logout();
    }

    @FXML
    private void handleExportarCsv(ActionEvent event) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("Fecha,Usuario,Accion,Recurso,Detalle,Resultado\n");
            DateTimeFormatter csvFmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (var b : tblBitacora.getItems()) {
                String fecha = b.getFechaHora() != null ? b.getFechaHora().format(csvFmt) : "";
                String usuario = b.getUsuarioId() != null ? String.valueOf(b.getUsuarioId()) : "";
                sb.append(fecha).append(",")
                  .append(usuario).append(",")
                  .append(csvEscape(b.getAccion())).append(",")
                  .append(csvEscape(b.getRecurso())).append(",")
                  .append(csvEscape(b.getDetalle())).append(",")
                  .append(csvEscape(b.getResultado())).append("\n");
            }
            java.nio.file.Path out = java.nio.file.Paths.get(System.getProperty("user.home"), "sica_bitacora.csv");
            java.nio.file.Files.writeString(out, sb.toString());
            DialogHelper.mostrarExito("Exportacion Exitosa", "Bitacora exportada correctamente a:\n" + out.toAbsolutePath());
        } catch (Exception e) {
            DialogHelper.mostrarError("Error al exportar", e.getMessage());
        }
    }

    private String csvEscape(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private void refreshTabla() {
        LocalDate desde = dpDesde.getValue();
        LocalDate hasta = dpHasta.getValue();
        var filtradas = auditoriaService.listarTodas().stream()
            .filter(b -> {
                if (b.getFechaHora() == null) return true;
                LocalDate fecha = b.getFechaHora().toLocalDate();
                boolean despuesDeDesde = desde == null || !fecha.isBefore(desde);
                boolean antesDeHasta = hasta == null || !fecha.isAfter(hasta);
                return despuesDeDesde && antesDeHasta;
            })
            .toList();
        tblBitacora.setItems(FXCollections.observableArrayList(filtradas));
    }
}
