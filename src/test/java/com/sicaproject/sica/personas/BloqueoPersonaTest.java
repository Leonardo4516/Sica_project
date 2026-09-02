package com.sicaproject.sica.personas;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.TestDatabaseCleaner;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BloqueoPersonaTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final PersonaService personaService = root.personaService();
    private final VisitaService visitaService = root.visitaService();
    private final AuthService authService = root.authService();
    private final AuditoriaService auditoriaService = root.auditoriaService();

    private Usuario funcionario;
    private Usuario guarda;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
        funcionario = authService.login("funcionario1", "123456").orElseThrow();
        guarda = authService.login("guarda1", "123456").orElseThrow();
        empresa = root.empresaService().listar().get(0);
    }

    @AfterEach
    void tearDown() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
    }

    @AfterAll
    static void tearDownAll() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
    }

    @Test
    void bloquear_y_desbloquear_persona_con_auditoria() {
        Persona p = new Persona();
        p.setTipoDocumento("CC");
        p.setDocumento(String.valueOf(100000000L + (System.currentTimeMillis() % 899999999L)));
        p.setNombre("Persona Bloqueo Test");
        p.setTipo("INVITADO");
        Persona persona = personaService.guardar(p, funcionario);
        final long personaId = persona.getId();

        assertFalse(persona.isBloqueado());

        // 1. Bloquear persona
        String motivo = "Incidente de conducta en recepción";
        personaService.bloquearPersona(persona, motivo, funcionario);

        Persona bloqueada = personaService.porId(personaId).orElseThrow();
        assertTrue(bloqueada.isBloqueado());
        assertEquals(motivo, bloqueada.getMotivoBloqueo());

        List<BitacoraAuditoria> bitacora = auditoriaService.listarTodas();
        boolean auditBloqueo = bitacora.stream().anyMatch(b ->
                "PERSONA_BLOQUEADA".equals(b.getAccion()) &&
                b.getEntidadId() == personaId &&
                b.getDetalle().contains(motivo));
        assertTrue(auditBloqueo, "Debe existir auditoría de PERSONA_BLOQUEADA con el motivo");

        // 2. Desbloquear persona
        personaService.desbloquearPersona(bloqueada, funcionario);
        Persona desbloqueada = personaService.porId(personaId).orElseThrow();
        assertFalse(desbloqueada.isBloqueado());
        assertNull(desbloqueada.getMotivoBloqueo());

        boolean auditDesbloqueo = auditoriaService.listarTodas().stream().anyMatch(b ->
                "PERSONA_DESBLOQUEADA".equals(b.getAccion()) &&
                b.getEntidadId() == personaId);
        assertTrue(auditDesbloqueo, "Debe existir auditoría de PERSONA_DESBLOQUEADA");
    }

    @Test
    void persona_bloqueada_no_puede_hacer_checkin_y_se_audita() {
        Persona p = new Persona();
        p.setTipoDocumento("CC");
        p.setDocumento(String.valueOf(200000000L + (System.currentTimeMillis() % 799999999L)));
        p.setNombre("Persona Bloqueada Checkin");
        p.setTipo("INVITADO");
        Persona persona = personaService.guardar(p, funcionario);
        final long personaId = persona.getId();

        // Pre-aprobar visita antes del bloqueo
        Visita visita = visitaService.registrarVisitaPreaprobada(persona, empresa, funcionario, null);
        assertEquals(EstadoVisita.APROBADA, visita.getEstado());

        // Bloquear a la persona
        personaService.bloquearPersona(persona, "Alerta de seguridad perimetral", funcionario);

        // Intentar check-in: DEBE fallar con IllegalStateException
        assertThrows(IllegalStateException.class, () -> visitaService.checkIn(visita.getId(), guarda));

        // Debe registrarse auditoría de intento denegado
        List<BitacoraAuditoria> bitacora = auditoriaService.listarTodas();
        boolean auditDenegado = bitacora.stream().anyMatch(b ->
                "ACCESO_DENEGADO_BLOQUEO".equals(b.getAccion()) &&
                "DENEGADO".equals(b.getResultado()) &&
                b.getEntidadId() == personaId);
        assertTrue(auditDenegado, "Debe existir registro en auditoría con ACCESO_DENEGADO_BLOQUEO");
    }
}
