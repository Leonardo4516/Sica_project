package com.sicaproject.sica.iam.domain;

/**
 * Entidad de Dominio que modela una cuenta de acceso al software SICA.
 * Almacena el nombre de usuario único, el hash criptográfico BCrypt de su contraseña,
 * el estado de activación de la cuenta, su rol RBAC asignado y su vínculo con Persona.
 */
public class Usuario {
    private Long id;
    private String username;
    private String passwordHash; // Hash BCrypt con salt generado por jBCrypt
    private boolean activo; // Estado de habilitación para inicio de sesión

    private com.sicaproject.sica.personas.domain.Persona persona; // Persona física asociada a la cuenta (opcional)
    private com.sicaproject.sica.iam.domain.Rol rol; // Rol RBAC con su colección dinámica de permisos

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public boolean isActivo() { return activo; }
    public void setActivo(boolean activo) { this.activo = activo; }

    public com.sicaproject.sica.iam.domain.Rol getRol() { return rol; }
    public void setRol(com.sicaproject.sica.iam.domain.Rol rol) { this.rol = rol; }

    public com.sicaproject.sica.personas.domain.Persona getPersona() { return persona; }
    public void setPersona(com.sicaproject.sica.personas.domain.Persona persona) { this.persona = persona; }
}