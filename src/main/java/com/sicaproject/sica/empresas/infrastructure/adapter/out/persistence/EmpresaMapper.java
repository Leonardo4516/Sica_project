package com.sicaproject.sica.empresas.infrastructure.adapter.out.persistence;

import com.sicaproject.sica.empresas.domain.Empresa;

public final class EmpresaMapper {

    private EmpresaMapper() {}

    public static Empresa toDomain(EmpresaEntity entity) {
        if (entity == null) return null;
        Empresa empresa = new Empresa();
        empresa.setId(entity.getId());
        empresa.setNombre(entity.getNombre());
        empresa.setNit(entity.getNit());
        return empresa;
    }

    public static void copyToEntity(Empresa empresa, EmpresaEntity entity) {
        entity.setNombre(empresa.getNombre());
        entity.setNit(empresa.getNit());
    }
}
