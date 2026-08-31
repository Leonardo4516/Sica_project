package com.sicaproject.sica.iam.application.port.out;

import com.sicaproject.sica.iam.domain.Rol;
import java.util.List;
import java.util.Optional;

public interface RolRepository {
    List<Rol> listarTodos();
    Optional<Rol> porNombre(String nombre);
    Optional<Rol> porId(long id);
}
