package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
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
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
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

    // TabPane
    @FXML private TabPane tabPaneAdmin;

    // Tabla Usuarios
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUId;
    @FXML private TableColumn<Usuario, String> colUUsername;
    @FXML private TableColumn<Usuario, String> colURol;
    @FXML private TableColumn<Usuario, String> colUPersona;
    @FXML private TableColumn<Usuario, String> colUEstado;
    @FXML private TableColumn<Usuario, Void> colUAccion;

    // Tabla Roles
    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolPermisos;

    // Tabla Empresas
    @FXML private TableView<Empresa> tblEmpresas;
    @FXML private TableColumn<Empresa, String> colEId;
    @FXML private TableColumn<Empresa, String> colENombre;
    @FXML private TableColumn<Empresa, String> colENit;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final RolRepository rolRepository = CompositionRoot.getInstance().rolRepository();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
    private final EmpresaService empresaService = CompositionRoot.getInstance().empresaService();

    private final DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm:ss");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Admin: " + SceneManager.getCurrentUser().getUsername());
        }

        ParticleBackground.attachTo(rootPane);

        Timeline clock = new Timeline(new KeyFrame(Duration.seconds(1),
            e -> lblHora.setText(LocalDateTime.now().format(timeFmt))));
        clock.setCycleCount(Timeline.INDEFINITE);
        clock.play();

        configurarTablas();
        refreshAll();
    }

    private void configurarTablas() {
        // Usuarios
        colUId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colUUsername.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getUsername()));
        colURol.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getRol() != null ? d.getValue().getRol().getNombre() : "—"));
        colUPersona.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getPersona() != null ? d.getValue().getPersona().getNombre() : "(ninguna)"));
        colUEstado.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().isActivo() ? "Activo" : "Inactivo"));
        colUAccion.setCellFactory(col -> new TableCell<>() {
            private final Button btn = new Button();
            private final HBox box = new HBox(btn);
            {
                box.setAlignment(Pos.CENTER);
                btn.setOnAction(e -> {
                    Usuario u = getTableView().getItems().get(getIndex());
                    toggleUsuario(u);
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Usuario u = getTableView().getItems().get(getIndex());
                    if (u.isActivo()) {
                        btn.setText("Desactivar");
                        btn.getStyleClass().setAll("btn", "btn-rechazar");
                    } else {
                        btn.setText("Activar");
                        btn.getStyleClass().setAll("btn", "btn-aprobar");
                    }
                    setGraphic(box);
                }
            }
        });

        // Roles
        colRolNombre.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getNombre()));
        colRolPermisos.setCellValueFactory(data -> {
            String lista = data.getValue().getPermisos().stream()
                .map(p -> p.getCodigo())
                .sorted()
                .collect(Collectors.joining(", "));
            return new SimpleStringProperty(lista.isEmpty() ? "(sin permisos asignados)" : lista);
        });

        // Empresas
        colEId.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getId())));
        colENombre.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNombre()));
        colENit.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getNit()));
    }

    private void toggleUsuario(Usuario u) {
        u.setActivo(!u.isActivo());
        usuarioRepository.save(u);
        refreshAll();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshAll();
    }

    @FXML
    private void handleBitacora(MouseEvent event) {
        SceneManager.loadScene("/com/sicaproject/sica/ui/bitacora.fxml", "SICA — Bitácora");
    }

    @FXML
    private void handleLogout(MouseEvent event) {
        SceneManager.logout();
    }

    private void refreshAll() {
        refreshStats();
        tblUsuarios.setItems(FXCollections.observableArrayList(usuarioRepository.findAll()));
        tblRoles.setItems(FXCollections.observableArrayList(rolRepository.listarTodos()));
        tblEmpresas.setItems(FXCollections.observableArrayList(empresaService.listar()));
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
}

