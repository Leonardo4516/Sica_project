package com.sicaproject.sica.shared.infrastructure.config;

import com.sicaproject.sica.acceso.application.port.out.VisitaRepository;
import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.acceso.infrastructure.adapter.out.persistence.VisitaRepositoryJpaAdapter;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.auditoria.application.port.out.BitacoraAuditoriaRepository;
import com.sicaproject.sica.auditoria.infrastructure.adapter.out.persistence.BitacoraAuditoriaRepositoryJpaAdapter;
import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence.EmpresaRepositoryJpaAdapter;
import com.sicaproject.sica.iam.application.port.out.RolRepository;
import com.sicaproject.sica.iam.application.port.out.UsuarioRepository;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.RolRepositoryJpaAdapter;
import com.sicaproject.sica.iam.infrastructure.adapter.out.persistence.UsuarioRepositoryJpaAdapter;
import com.sicaproject.sica.incidentes.application.port.out.IncidenteRepository;
import com.sicaproject.sica.incidentes.application.service.IncidenteService;
import com.sicaproject.sica.incidentes.infrastructure.adapter.out.persistence.IncidenteRepositoryJpaAdapter;
import com.sicaproject.sica.personas.application.port.out.PersonaRepository;
import com.sicaproject.sica.personas.application.service.PersonaService;
import com.sicaproject.sica.personas.infrastructure.adapter.out.persistence.PersonaRepositoryJpaAdapter;

/**
 * Único lugar del proyecto donde se conectan los puertos con sus adaptadores
 * concretos ("cableado" manual, sin framework de inyección de dependencias).
 * Los controladores de la futura UI JavaFX deben pedir sus servicios AQUÍ,
 * nunca instanciar un *RepositoryJpaAdapter directamente.
 */
public final class CompositionRoot {

    private static CompositionRoot instance;

    private final EmpresaRepository empresaRepository = new EmpresaRepositoryJpaAdapter();
    private final PersonaRepository personaRepository = new PersonaRepositoryJpaAdapter();
    private final UsuarioRepository usuarioRepository = new UsuarioRepositoryJpaAdapter();
    private final VisitaRepository visitaRepository = new VisitaRepositoryJpaAdapter();
    private final BitacoraAuditoriaRepository bitacoraRepository = new BitacoraAuditoriaRepositoryJpaAdapter();
    private final RolRepository rolRepository = new RolRepositoryJpaAdapter();
    private final IncidenteRepository incidenteRepository = new IncidenteRepositoryJpaAdapter();

    private final AuditoriaService auditoriaService = new AuditoriaService(bitacoraRepository);
    private final RbacService rbacService = new RbacService();
    private final AuthService authService = new AuthService(usuarioRepository, auditoriaService);
    private final EmpresaService empresaService = new EmpresaService(empresaRepository, rbacService, auditoriaService);
    private final PersonaService personaService = new PersonaService(personaRepository, rbacService, auditoriaService);
    private final VisitaService visitaService = new VisitaService(visitaRepository, rbacService, auditoriaService);
    private final IncidenteService incidenteService = new IncidenteService(incidenteRepository, rbacService, auditoriaService);
    private final com.sicaproject.sica.iam.application.service.UsuarioService usuarioService =
            new com.sicaproject.sica.iam.application.service.UsuarioService(usuarioRepository, rolRepository, rbacService, auditoriaService);

    private CompositionRoot() {}

    public static synchronized CompositionRoot getInstance() {
        if (instance == null) {
            instance = new CompositionRoot();
        }
        return instance;
    }

    public AuthService authService() { return authService; }
    public UsuarioRepository usuarioRepository() { return usuarioRepository; }
    public RolRepository rolRepository() { return rolRepository; }
    public RbacService rbacService() { return rbacService; }
    public EmpresaRepository empresaRepository() { return empresaRepository; }
    public EmpresaService empresaService() { return empresaService; }
    public PersonaService personaService() { return personaService; }
    public VisitaService visitaService() { return visitaService; }
    public AuditoriaService auditoriaService() { return auditoriaService; }
    public IncidenteRepository incidenteRepository() { return incidenteRepository; }
    public IncidenteService incidenteService() { return incidenteService; }
    public com.sicaproject.sica.iam.application.service.UsuarioService usuarioService() { return usuarioService; }
}
