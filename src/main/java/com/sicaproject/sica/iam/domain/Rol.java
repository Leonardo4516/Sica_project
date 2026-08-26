package com.sicaproject.sica.iam.domain;

import java.util.HashSet;
import java.util.Set;

public class Rol {
    private long id;
    private String nombre; // GUARDA, FUNCIONARIO, ADMIN
    private Set<Permiso> permisos = new HashSet<>();

    public Rol() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }

    public boolean tienePermiso(String codigo) {
        return permisos.stream().anyMatch(p -> p.getCodigo().equals(codigo));
    }
}