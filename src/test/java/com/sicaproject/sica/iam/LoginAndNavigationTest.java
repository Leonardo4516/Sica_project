package com.sicaproject.sica.iam;

import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginAndNavigationTest {

    private final AuthService auth = CompositionRoot.getInstance().authService();

    @Test
    void admin1_abre_panel_admin() {
        Usuario usuario = auth.login("admin1", "123456").orElseThrow();
        assertEquals("ADMIN", usuario.getRol().getNombre().toUpperCase());
        assertEquals("/com/sicaproject/sica/ui/admin.fxml", SceneManager.fxmlForRole(usuario));
    }

    @Test
    void guarda1_abre_panel_guarda() {
        Usuario usuario = auth.login("guarda1", "123456").orElseThrow();
        assertEquals("GUARDA", usuario.getRol().getNombre().toUpperCase());
        assertEquals("/com/sicaproject/sica/ui/guarda.fxml", SceneManager.fxmlForRole(usuario));
    }

    @Test
    void funcionario1_abre_panel_funcionario() {
        Usuario usuario = auth.login("funcionario1", "123456").orElseThrow();
        assertEquals("FUNCIONARIO", usuario.getRol().getNombre().toUpperCase());
        assertEquals("/com/sicaproject/sica/ui/funcionario.fxml", SceneManager.fxmlForRole(usuario));
    }

    @Test
    void credenciales_incorrectas_no_autentican() {
        assertTrue(auth.login("admin1", "wrong").isEmpty());
        assertTrue(auth.login("noexiste", "123456").isEmpty());
    }

    @Test
    void paneles_existen_en_classpath() {
        for (String user : new String[] {"admin1", "guarda1", "funcionario1"}) {
            Usuario usuario = auth.login(user, "123456").orElseThrow();
            String fxml = SceneManager.fxmlForRole(usuario);
            assertTrue(SceneManager.class.getResource(fxml) != null, "Falta " + fxml);
        }
    }

    @Test
    void dashboard_admin_puede_cargar_visitas_y_roles() {
        var root = CompositionRoot.getInstance();
        assertTrue(root.visitaService().listarTodas() != null);
        assertTrue(root.rolRepository().listarTodos().size() >= 3);
        assertTrue(root.empresaService().listar().size() >= 1);
        assertTrue(root.auditoriaService().listarTodas() != null);
    }
}
