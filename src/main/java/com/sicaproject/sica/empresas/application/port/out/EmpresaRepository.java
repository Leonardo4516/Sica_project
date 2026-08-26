package com.sicaproject.sica.empresas.application.port.out;

import com.sicaproject.sica.empresas.domain.Empresa;
import java.util.List;
import java.util.Optional;

public interface EmpresaRepository {
    Optional<Empresa> porId(long id);
    Optional<Empresa> porNit(String nit);
    Empresa guardar(Empresa empresa);
    List<Empresa> listar();
}
