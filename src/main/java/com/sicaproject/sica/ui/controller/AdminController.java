package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblHora;
    @FXML private Label lblTotalVisitas;
    @FXML private Label lblVisitasDentro;
    @FXML private Label lblPendientes;
    @FXML private Label lblCerradasHoy;
    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolPermisos;

    private final VisitaService visitaService =
        CompositionRoot.getInstance().visitaService();
    private final RbacService rbacService =
        CompositionRoot.getInstance().rbacService();
    private final RolRepository rolRepository =
        CompositionRoot.getInstance().rolRepository();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Admin: " + SceneManager.getCurrentUser().getUsername());
        }


        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        refreshStats();
        refreshRoles();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshStats();
        refreshRoles();
    }

    @FXML
    private void handleBitacora(MouseEvent event) {
        SceneManager.loadScene("/com/sicaproject/sica/ui/bitacora.fxml", "SICA — Bitácora");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    private void refreshStats() {
        var todas = visitaService.listarTodas();
        lblTotalVisitas.setText(String.valueOf(todas.size()));
        lblVisitasDentro.setText(String.valueOf(
            todas.stream().filter(v -> v.getEstado() == EstadoVisita.DENTRO).count()));
        lblPendientes.setText(String.valueOf(
            todas.stream().filter(v -> v.getEstado() == EstadoVisita.PENDIENTE_APROBACION).count()));
        LocalDate hoy = LocalDate.now();
        lblCerradasHoy.setText(String.valueOf(todas.stream().filter(v ->
            (v.getEstado() == EstadoVisita.CERRADA || v.getEstado() == EstadoVisita.CERRADA_POR_SISTEMA)
            && v.getFechaHoraSalida() != null
            && v.getFechaHoraSalida().toLocalDate().equals(hoy)).count()));
    }

    private void refreshRoles() {
        colRolNombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colRolPermisos.setCellValueFactory(data -> {
            String lista = data.getValue().getPermisos().stream()
                .map(p -> p.getCodigo())
                .sorted()
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(lista.isEmpty() ? "(sin permisos asignados)" : lista);
        });
        tblRoles.setItems(FXCollections.observableArrayList(rolRepository.listarTodos()));
    }
}
