package com.sicaproject.sica.iam;

import com.sicaproject.sica.TestDatabaseCleaner;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthServiceAuditTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final AuthService authService = root.authService();
    private final AuditoriaService auditoriaService = root.auditoriaService();

    @AfterAll
    static void tearDownAll() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
    }

    @Test
    void login_exitoso_registra_auditoria() {
        Optional<Usuario> userOpt = authService.login("admin1", "123456");
        assertTrue(userOpt.isPresent());

        List<BitacoraAuditoria> bitacora = auditoriaService.listarTodas();
        boolean auditado = bitacora.stream().anyMatch(b ->
                "LOGIN_EXITOSO".equals(b.getAccion()) &&
                "EXITOSO".equals(b.getResultado()) &&
                b.getUsuarioId() != null &&
                b.getUsuarioId().equals(userOpt.get().getId()));

        assertTrue(auditado, "Debe existir un registro de auditoría para LOGIN_EXITOSO");
    }

    @Test
    void login_fallido_registra_auditoria() {
        String randomUser = "usuario_fake_" + System.currentTimeMillis();
        Optional<Usuario> userOpt = authService.login(randomUser, "badpass");
        assertTrue(userOpt.isEmpty());

        List<BitacoraAuditoria> bitacora = auditoriaService.listarTodas();
        boolean auditado = bitacora.stream().anyMatch(b ->
                "LOGIN_FALLIDO".equals(b.getAccion()) &&
                "FALLIDO".equals(b.getResultado()) &&
                b.getDetalle() != null &&
                b.getDetalle().contains(randomUser));

        assertTrue(auditado, "Debe existir un registro de auditoría para LOGIN_FALLIDO");
    }
}
