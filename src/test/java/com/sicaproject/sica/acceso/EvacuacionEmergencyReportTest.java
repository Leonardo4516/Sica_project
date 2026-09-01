package com.sicaproject.sica.acceso;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.domain.EstadoVisita;
import com.sicaproject.sica.acceso.domain.Visita;
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

class EvacuacionEmergencyReportTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final VisitaService visitaService = root.visitaService();
    private final PersonaService personaService = root.personaService();
    private final AuthService authService = root.authService();

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
    void recuento_de_personal_dentro_para_evacuacion_de_emergencia() {
        // Crear 2 personas e ingresar al complejo
        Persona p1 = new Persona();
        p1.setDocumento("EVAC-1-" + System.currentTimeMillis());
        p1.setNombre("Trabajador Evacuacion 1");
        p1.setTipo("TRABAJADOR");
        p1 = personaService.guardar(p1, funcionario);

        Persona p2 = new Persona();
        p2.setDocumento("EVAC-2-" + System.currentTimeMillis());
        p2.setNombre("Invitado Evacuacion 2");
        p2.setTipo("INVITADO");
        p2 = personaService.guardar(p2, funcionario);

        Visita v1 = visitaService.registrarVisitaPreaprobada(p1, empresa, funcionario, null);
        Visita v2 = visitaService.registrarVisitaPreaprobada(p2, empresa, funcionario, null);

        // Check-in de ambas personas
        visitaService.checkIn(v1.getId(), guarda);
        visitaService.checkIn(v2.getId(), guarda);

        // Consultar personal actualmente DENTRO (para evacuación)
        List<Visita> personalDentro = visitaService.listarTodas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
                .toList();

        assertTrue(personalDentro.size() >= 2, "Debe haber al menos 2 personas dentro para evacuación");

        final long p1Id = p1.getId();
        final long p2Id = p2.getId();

        boolean p1Dentro = personalDentro.stream().anyMatch(v -> v.getPersona().getId() == p1Id);
        boolean p2Dentro = personalDentro.stream().anyMatch(v -> v.getPersona().getId() == p2Id);

        assertTrue(p1Dentro, "Trabajador 1 debe figurar en la lista de evacuación");
        assertTrue(p2Dentro, "Invitado 2 debe figurar en la lista de evacuación");

        // Al hacer check-out de p1, sale de la lista de evacuación activa
        visitaService.checkOut(v1.getId(), guarda);

        List<Visita> despuesSalida = visitaService.listarTodas().stream()
                .filter(v -> v.getEstado() == EstadoVisita.DENTRO)
                .toList();

        boolean p1SigueDentro = despuesSalida.stream().anyMatch(v -> v.getPersona().getId() == p1Id);
        assertFalse(p1SigueDentro, "Trabajador 1 ya no debe estar en la lista de evacuación tras check-out");
    }
}
