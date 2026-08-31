package com.sicaproject.sica.empresas.application.service;

import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.iam.domain.Usuario;
import java.util.List;
import java.util.Optional;

public class EmpresaService {

    private final EmpresaRepository empresaRepository;
    private final RbacService rbacService;
    private final AuditoriaService auditoriaService;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this(empresaRepository, null, null);
    }

    public EmpresaService(EmpresaRepository empresaRepository, RbacService rbacService, AuditoriaService auditoriaService) {
        this.empresaRepository = empresaRepository;
        this.rbacService = rbacService;
        this.auditoriaService = auditoriaService;
    }

    public Optional<Empresa> porId(long id) {
        return empresaRepository.porId(id);
    }

    public Optional<Empresa> porNit(String nit) {
        return empresaRepository.porNit(nit);
    }

    public Empresa crear(Empresa empresa) {
        return crear(empresa, null);
    }

    public Empresa crear(Empresa empresa, Usuario usuarioSolicitante) {
        if (usuarioSolicitante != null && rbacService != null) {
            rbacService.verificarPermiso(usuarioSolicitante, "gestionar_empresas");
        }
        Empresa guardada = empresaRepository.guardar(empresa);
        if (auditoriaService != null) {
            Long usuarioId = usuarioSolicitante != null ? usuarioSolicitante.getId() : null;
            auditoriaService.registrar(usuarioId, "EMPRESA_CREADA", "EMPRESA", guardada.getId(),
                    "Empresa registrada: " + guardada.getNombre() + " (NIT: " + guardada.getNit() + ")", "EXITOSO");
        }
        return guardada;
    }

    public List<Empresa> listar() {
        return empresaRepository.listar();
    }
}
