package com.sicaproject.sica.personas;

import com.sicaproject.sica.TestDatabaseCleaner;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.domain.BitacoraAuditoria;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.domain.Persona;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class PersonaCrudAdminTest {

    private final CompositionRoot root = CompositionRoot.getInstance();
    private final PersonaService personaService = root.personaService();
    private final AuthService authService = root.authService();
    private final AuditoriaService auditoriaService = root.auditoriaService();

    private Usuario admin;
    private Empresa empresa;

    @BeforeEach
    void setUp() {
        TestDatabaseCleaner.limpiarDatosDePrueba();
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
    void admin_puede_crear_editar_y_eliminar_persona_con_auditoria() {
        // 1. Crear persona
        Persona nueva = new Persona();
        nueva.setNombre("Persona CRUD Admin Test");
        nueva.setTipoDocumento("CC");
        nueva.setDocumento("CRUD-TEST-" + System.currentTimeMillis());
        nueva.setTipo("TRABAJADOR");
        nueva.setEmpresaId(empresa.getId());
        nueva.setFotoUrl("photos/test_crud.jpg");

        Persona creada = personaService.guardar(nueva, admin);
        assertNotNull(creada);
        assertTrue(creada.getId() > 0);
        assertEquals("Persona CRUD Admin Test", creada.getNombre());
        assertEquals("photos/test_crud.jpg", creada.getFotoUrl());

        long personaId = creada.getId();

        // Verificar auditoría de creación
        List<BitacoraAuditoria> bitacora = auditoriaService.listarTodas();
        boolean auditCreada = bitacora.stream().anyMatch(b ->
                "PERSONA_CREADA".equals(b.getAccion()) &&
                b.getEntidadId() == personaId);
        assertTrue(auditCreada, "Debe auditar la creación de la persona");

        // 2. Editar persona
        creada.setNombre("Persona CRUD Editada");
        creada.setFotoUrl("photos/test_crud_updated.jpg");
        personaService.guardar(creada, admin);

        Persona actualizada = personaService.porId(personaId).orElseThrow();
        assertEquals("Persona CRUD Editada", actualizada.getNombre());
        assertEquals("photos/test_crud_updated.jpg", actualizada.getFotoUrl());

        // 3. Eliminar persona
        personaService.eliminarPersona(personaId, admin);
        Optional<Persona> eliminadaOpt = personaService.porId(personaId);
        assertTrue(eliminadaOpt.isEmpty(), "La persona debe haber sido eliminada");

        // Verificar auditoría de eliminación
        List<BitacoraAuditoria> bitacoraFinal = auditoriaService.listarTodas();
        boolean auditEliminada = bitacoraFinal.stream().anyMatch(b ->
                "PERSONA_ELIMINADA".equals(b.getAccion()) &&
                b.getEntidadId() == personaId);
        assertTrue(auditEliminada, "Debe auditar la eliminación de la persona");
    }
}
