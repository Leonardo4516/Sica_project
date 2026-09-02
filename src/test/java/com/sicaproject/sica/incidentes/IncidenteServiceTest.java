package com.sicaproject.sica.incidentes;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.incidentes.application.service.IncidenteService;
import com.sicaproject.sica.incidentes.domain.Incidente;
import com.sicaproject.sica.incidentes.domain.SeveridadIncidente;
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

class IncidenteServiceTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final IncidenteService incidenteService = root.incidenteService();
    private final PersonaService personaService = root.personaService();
    private final AuthService authService = root.authService();
    private final AuditoriaService auditoriaService = root.auditoriaService();

    private Usuario guarda;
    private Usuario admin;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
        guarda = authService.login("guarda1", "123456").orElseThrow();
        admin = authService.login("admin1", "123456").orElseThrow();
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
    void reportar_incidente_crea_registro_y_audita() {
        Persona persona = new Persona();
        persona.setTipoDocumento("CC");
        persona.setDocumento(String.valueOf(100000000L + (System.currentTimeMillis() % 899999999L)));
        persona.setNombre("Persona Incidente Test");
        persona.setTipo("INVITADO");
        persona = personaService.guardar(persona, guarda);

        String titulo = "Ingreso sin credencial a zona restringida";
        String descripcion = "Se detectó intento de acceso a sala de servidores sin autorización previa";
        SeveridadIncidente severidad = SeveridadIncidente.ALTA;

        Incidente inc = incidenteService.reportarIncidente(
                titulo, descripcion, severidad, persona, empresa, guarda
        );

        assertNotNull(inc.getId());
        assertEquals(titulo, inc.getTitulo());
        assertEquals(severidad, inc.getSeveridad());
        assertEquals("ABIERTO", inc.getEstado());
        assertEquals(guarda.getId(), inc.getReportadoPor().getId());

        final long incId = inc.getId();
        List<BitacoraAuditoria> auditoria = auditoriaService.listarTodas();
        boolean auditado = auditoria.stream().anyMatch(b ->
                "REGISTRO_INCIDENTE".equals(b.getAccion()) &&
                b.getEntidadId() == incId &&
                b.getDetalle().contains(titulo));

        assertTrue(auditado, "Debe existir un registro de auditoría con la acción REGISTRO_INCIDENTE");
    }

    @Test
    void admin_puede_cambiar_estado_incidente_y_se_audita() {
        Incidente inc = incidenteService.reportarIncidente(
                "Falsa alarma de sensor", "Sensor de puerta principal activado accidentalmente",
                SeveridadIncidente.BAJA, null, empresa, guarda
        );

        final long incId = inc.getId();
        Incidente actualizado = incidenteService.cambiarEstado(incId, "RESUELTO", admin);
        assertEquals("RESUELTO", actualizado.getEstado());

        boolean auditado = auditoriaService.listarTodas().stream().anyMatch(b ->
                "INCIDENTE_ESTADO_CAMBIADO".equals(b.getAccion()) &&
                b.getEntidadId() == incId &&
                b.getDetalle().contains("RESUELTO"));

        assertTrue(auditado, "Debe existir auditoría de cambio de estado del incidente");
    }
}
