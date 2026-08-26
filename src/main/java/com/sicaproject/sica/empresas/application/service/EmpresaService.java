package com.sicaproject.sica.empresas.application.service;

import com.sicaproject.sica.empresas.application.port.out.EmpresaRepository;
import com.sicaproject.sica.empresas.domain.Empresa;
import java.util.List;
import java.util.Optional;

public class EmpresaService {

    private final EmpresaRepository empresaRepository;

    public EmpresaService(EmpresaRepository empresaRepository) {
        this.empresaRepository = empresaRepository;
    }

    public Optional<Empresa> porId(long id) {
        return empresaRepository.porId(id);
    }

    public Optional<Empresa> porNit(String nit) {
        return empresaRepository.porNit(nit);
    }

    public Empresa crear(Empresa empresa) {
        return empresaRepository.guardar(empresa);
    }

    public List<Empresa> listar() {
        return empresaRepository.listar();
    }
}
