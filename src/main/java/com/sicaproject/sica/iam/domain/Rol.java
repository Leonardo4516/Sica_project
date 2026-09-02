package com.sicaproject.sica.iam.domain;

import java.util.HashSet;
import java.util.Set;

/**
 * Entidad de Dominio que representa un Rol en la arquitectura de Control de Acceso Basado en Roles (RBAC).
 * Agrupa una colección dinámica de permisos cargados desde la base de datos (tabla rol_permiso).
 * Permite la validación de capacidades sin código hardcodeado en las capas superiores.
 */
public class Rol {
    private long id;
    private String nombre; // Identificador del rol: GUARDA, FUNCIONARIO, ADMIN
    private Set<Permiso> permisos = new HashSet<>(); // Conjunto de permisos asignados en base de datos

    public Rol() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public Set<Permiso> getPermisos() { return permisos; }
    public void setPermisos(Set<Permiso> permisos) { this.permisos = permisos; }

    /**
     * Evalúa si este rol contiene un permiso específico mediante evaluación funcional con Streams.
     * 
     * @param codigo Código único del permiso solicitado (ej. 'registrar_visita').
     * @return true si el conjunto contiene al menos un Permiso con dicho código coincidente.
     */
    public boolean tienePermiso(String codigo) {
        return permisos.stream().anyMatch(p -> p.getCodigo().equals(codigo));
    }
}