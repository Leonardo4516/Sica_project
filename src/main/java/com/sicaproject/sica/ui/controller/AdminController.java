package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Rol;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.text.Text;
import java.net.URL;
import java.time.LocalDate;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class AdminController implements Initializable {

    @FXML private StackPane rootPane;
    @FXML private Text txtUsuario;
    @FXML private Label lblTotalVisitas;
    @FXML private Label lblVisitasDentro;
    @FXML private Label lblPendientes;
    @FXML private Label lblCerradasHoy;
    @FXML private TableView<Usuario> tblUsuarios;
    @FXML private TableColumn<Usuario, String> colUId;
    @FXML private TableColumn<Usuario, String> colUUsername;
    @FXML private TableColumn<Usuario, String> colURol;
    @FXML private TableColumn<Usuario, String> colUPersona;
    @FXML private TableColumn<Usuario, String> colUEstado;
    @FXML private TableColumn<Usuario, Void> colUAccion;
    @FXML private TableView<Rol> tblRoles;
    @FXML private TableColumn<Rol, String> colRolNombre;
    @FXML private TableColumn<Rol, String> colRolPermisos;
    @FXML private TableView<Empresa> tblEmpresas;
    @FXML private TableColumn<Empresa, String> colEId;
    @FXML private TableColumn<Empresa, String> colENombre;
    @FXML private TableColumn<Empresa, String> colENit;

    private final VisitaService visitaService = CompositionRoot.getInstance().visitaService();
    private final RbacService rbacService = CompositionRoot.getInstance().rbacService();
    private final RolRepository rolRepository = CompositionRoot.getInstance().rolRepository();
    private final UsuarioRepository usuarioRepository = CompositionRoot.getInstance().usuarioRepository();
    private final EmpresaRepository empresaRepository = CompositionRoot.getInstance().empresaRepository();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        if (SceneManager.getCurrentUser() != null) {
            txtUsuario.setText("Bienvenido, " + SceneManager.getCurrentUser().getUsername());
        }
        refreshStats();
        refreshRoles();
        refreshUsuarios();
        refreshEmpresas();
    }

    @FXML
    private void handleRefresh(ActionEvent event) {
        refreshStats();
        refreshRoles();
        refreshUsuarios();
        refreshEmpresas();
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
            return new SimpleStringProperty(lista.isEmpty() ? "(sin permisos)" : lista);
        });
        tblRoles.setItems(FXCollections.observableArrayList(rolRepository.listarTodos()));
    }

    private void refreshUsuarios() {
        colUId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colUUsername.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getUsername()));
        colURol.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getRol().getNombre()));
        colUPersona.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getPersona() != null
                ? data.getValue().getPersona().getNombre() : "—"));
        colUEstado.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().isActivo() ? "Activo" : "Inactivo"));
        tblUsuarios.setItems(FXCollections.observableArrayList(usuarioRepository.findAll()));
    }

    private void refreshEmpresas() {
        colEId.setCellValueFactory(data ->
            new SimpleStringProperty(String.valueOf(data.getValue().getId())));
        colENombre.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNombre()));
        colENit.setCellValueFactory(data ->
            new SimpleStringProperty(data.getValue().getNit()));
        tblEmpresas.setItems(FXCollections.observableArrayList(empresaRepository.listar()));
    }
}