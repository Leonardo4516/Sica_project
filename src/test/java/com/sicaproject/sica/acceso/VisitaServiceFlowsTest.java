package com.sicaproject.sica.acceso;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.application.service.PermisoDenegadoException;
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

class VisitaServiceFlowsTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final VisitaService visitaService = root.visitaService();
    private final AuthService authService = root.authService();
    private final PersonaService personaService = root.personaService();
    private final EmpresaService empresaService = root.empresaService();
    private final AuditoriaService auditoriaService = root.auditoriaService();

    private Usuario guarda;
    private Usuario funcionario;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
        guarda = authService.login("guarda1", "123456").orElseThrow();
        funcionario = authService.login("funcionario1", "123456").orElseThrow();
        empresa = empresaService.listar().get(0);
    }

    @AfterEach
    void tearDown() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
    }

    @AfterAll
    static void tearDownAll() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
    }

    private Persona crearPersonaTest(String prefijo) {
        String doc = String.valueOf(100000000L + (long)(Math.random() * 899999999L));
        Persona p = new Persona();
        p.setTipoDocumento("CC");
        p.setDocumento(doc);
        p.setNombre("Persona Prueba Flujo");
        p.setTipo("INVITADO");
        p.setEmpresaId(empresa.getId());
        return personaService.guardar(p);
    }

    @Test
    void flujo1_invitado_preregistrado_completo() {
        Persona persona = crearPersonaTest("F1");

        // 1. Funcionario pre-registra la visita -> APROBADA
        Visita v = visitaService.registrarVisitaPreaprobada(persona, empresa, funcionario, null);
        assertNotNull(v.getId());
        assertEquals(EstadoVisita.APROBADA, v.getEstado());
        assertNotNull(v.getFechaHoraRegistro());
        assertNull(v.getFechaHoraEntrada());
        assertNull(v.getFechaHoraSalida());

        // 2. Guarda realiza check-in -> DENTRO
        Visita vEntrada = visitaService.checkIn(v.getId(), guarda);
        assertEquals(EstadoVisita.DENTRO, vEntrada.getEstado());
        assertNotNull(vEntrada.getFechaHoraEntrada());
        assertEquals(guarda.getId(), vEntrada.getGuarda().getId());

        // 3. Guarda realiza check-out -> CERRADA
        Visita vSalida = visitaService.checkOut(v.getId(), guarda);
        assertEquals(EstadoVisita.CERRADA, vSalida.getEstado());
        assertNotNull(vSalida.getFechaHoraSalida());
    }

    @Test
    void flujo2_invitado_no_anunciado_aprobado() {
        Persona persona = crearPersonaTest("F2");

        // 1. Guarda solicita acceso -> PENDIENTE_APROBACION
        Visita v = visitaService.solicitarAcceso(persona, empresa, funcionario, guarda, false);
        assertNotNull(v.getId());
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, v.getEstado());
        assertFalse(v.isPaseTemporal());

        // 2. Funcionario aprueba -> APROBADA
        Visita vAprobada = visitaService.aprobarVisita(v.getId(), funcionario);
        assertEquals(EstadoVisita.APROBADA, vAprobada.getEstado());

        // 3. Guarda realiza check-in -> DENTRO
        Visita vDentro = visitaService.checkIn(v.getId(), guarda);
        assertEquals(EstadoVisita.DENTRO, vDentro.getEstado());
        assertNotNull(vDentro.getFechaHoraEntrada());

        // 4. Guarda realiza check-out -> CERRADA
        Visita vCerrada = visitaService.checkOut(v.getId(), guarda);
        assertEquals(EstadoVisita.CERRADA, vCerrada.getEstado());
    }

    @Test
    void flujo2_invitado_no_anunciado_rechazado() {
        Persona persona = crearPersonaTest("F2-Rechazo");

        // 1. Guarda solicita acceso -> PENDIENTE_APROBACION
        Visita v = visitaService.solicitarAcceso(persona, empresa, funcionario, guarda, false);
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, v.getEstado());

        // 2. Funcionario rechaza -> RECHAZADA
        Visita vRechazada = visitaService.rechazarVisita(v.getId(), funcionario);
        assertEquals(EstadoVisita.RECHAZADA, vRechazada.getEstado());

        // 3. Check-in de visita rechazada debe fallar
        assertThrows(IllegalStateException.class, () -> visitaService.checkIn(v.getId(), guarda));
    }

    @Test
    void flujo3_carnet_olvidado_pase_temporal() {
        Persona persona = crearPersonaTest("F3-Carnet");
        persona.setTipo("TRABAJADOR");
        personaService.guardar(persona);

        // 1. Guarda solicita acceso con pase temporal
        Visita v = visitaService.solicitarAcceso(persona, empresa, funcionario, guarda, true);
        assertEquals(EstadoVisita.PENDIENTE_APROBACION, v.getEstado());
        assertTrue(v.isPaseTemporal());

        // 2. Funcionario aprueba
        Visita vAprobada = visitaService.aprobarVisita(v.getId(), funcionario);
        assertEquals(EstadoVisita.APROBADA, vAprobada.getEstado());
        assertTrue(vAprobada.isPaseTemporal());

        // 3. Guarda hace check-in
        Visita vDentro = visitaService.checkIn(v.getId(), guarda);
        assertEquals(EstadoVisita.DENTRO, vDentro.getEstado());
    }

    @Test
    void flujo4_salida_olvidada_regularizacion_automatica() {
        Persona persona = crearPersonaTest("F4-SalidaOlvidada");

        // 1. Primer ingreso normal de la persona
        Visita v1 = visitaService.registrarVisitaPreaprobada(persona, empresa, funcionario, null);
        visitaService.checkIn(v1.getId(), guarda);
        assertEquals(EstadoVisita.DENTRO, visitaService.obtenerVisita(v1.getId()).orElseThrow().getEstado());

        // 2. La persona regresa al complejo SIN haber hecho check-out de la visita 1
        // Nueva visita pre-registrada
        Visita v2 = visitaService.registrarVisitaPreaprobada(persona, empresa, funcionario, null);

        // 3. Al hacer check-in de la visita 2, el sistema debe regularizar automáticamente la visita 1 a CERRADA_POR_SISTEMA
        visitaService.checkIn(v2.getId(), guarda);

        Visita v1Regularizada = visitaService.obtenerVisita(v1.getId()).orElseThrow();
        assertEquals(EstadoVisita.CERRADA_POR_SISTEMA, v1Regularizada.getEstado());
        assertNotNull(v1Regularizada.getFechaHoraSalida());

        Visita v2Actual = visitaService.obtenerVisita(v2.getId()).orElseThrow();
        assertEquals(EstadoVisita.DENTRO, v2Actual.getEstado());

        // 4. Verificar que se registró el evento de auditoría SALIDA_OLVIDADA
        List<BitacoraAuditoria> auditoria = auditoriaService.listarTodas();
        boolean auditoriaEncontrada = auditoria.stream()
                .anyMatch(b -> "SALIDA_OLVIDADA".equals(b.getAccion()) && b.getEntidadId() == v1.getId());
        assertTrue(auditoriaEncontrada, "Debe existir un registro de auditoría con acción SALIDA_OLVIDADA");
    }

    @Test
    void validacion_rbac_permisos_denegados() {
        Persona persona = crearPersonaTest("RBAC-Test");
        Visita v = visitaService.solicitarAcceso(persona, empresa, funcionario, guarda, false);

        // 1. El Guarda NO puede aprobar ni rechazar visitas
        assertThrows(PermisoDenegadoException.class, () -> visitaService.aprobarVisita(v.getId(), guarda));
        assertThrows(PermisoDenegadoException.class, () -> visitaService.rechazarVisita(v.getId(), guarda));

        // 2. El Funcionario NO puede hacer check-out (no tiene permiso registrar_salida)
        // Primero aprueba la visita para poder hacer check-out
        Visita vAprobada = visitaService.aprobarVisita(v.getId(), funcionario);
        Visita vDentro = visitaService.checkIn(vAprobada.getId(), guarda);
        assertThrows(PermisoDenegadoException.class, () -> visitaService.checkOut(vDentro.getId(), funcionario));
    }
}
