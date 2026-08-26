package com.sicaproject.sica.empresas.application.service;

import com.sicaproject.sica.empresas.domain.Empresa;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class EmpresaService {

    private static Map<Long, Empresa> empresas = new HashMap<>();
    private static long nextEmpresaId = 1;

    // Empresas de prueba
    public EmpresaService() {
        if (empresas.isEmpty()) {
            // Empresa Acme (la principal)
            var acme = new Empresa();
            acme.setId(nextEmpresaId++);
            acme.setNombre("Zona Acme");
            acme.setNit("900123456-7");
            empresas.put(acme.getId(), acme);

            // Empresa Tecnobytes
            var tech = new Empresa();
            tech.setId(nextEmpresaId++);
            tech.setNombre("Tecnobytes SAS");
            tech.setNit("900987654-3");
            empresas.put(tech.getId(), tech);

            // Empresa InnoVA
            var inno = new Empresa();
            inno.setId(nextEmpresaId++);
            inno.setNombre("InnoVA Consulting");
            inno.setNit("900555555-1");
            empresas.put(inno.getId(), inno);
        }
    }

    public Optional<Empresa> porId(long id) {
        return Optional.ofNullable(empresas.get(id));
    }

    public Optional<Empresa> porNit(String nit) {
        return empresas.values().stream()
                .filter(e -> e.getNit().equals(nit))
                .findFirst();
    }

    public Empresa crear(Empresa empresa) {
        empresa.setId(nextEmpresaId++);
        empresas.put(empresa.getId(), empresa);
        return empresa;
    }

    public void actualizar(Empresa empresa) {
        empresas.put(empresa.getId(), empresa);
    }

    public java.util.List<Empresa> listar() {
        return java.util.List.copyOf(empresas.values());
    }
}