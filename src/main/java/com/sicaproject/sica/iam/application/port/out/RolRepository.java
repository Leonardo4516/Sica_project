package com.sicaproject.sica.iam.application.port.out;

import com.sicaproject.sica.iam.domain.Rol;
import java.util.List;

public interface RolRepository {
    List<Rol> listarTodos();
}
