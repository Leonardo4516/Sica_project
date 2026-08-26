package com.sicaproject.sica.iam.domain;

public class Usuario {
    private Long id;
    private String username;
    private String passwordHash;
    private String activo; // "true"/"false"

    private com.sicaproject.sica.personas.domain.Persona persona;
    private com.sicaproject.sica.iam.domain.Rol rol;

    public Usuario() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPasswordHash() { return passwordHash; }
    public void setPasswordHash(String passwordHash) { this.passwordHash = passwordHash; }

    public String getActivo() { return activo; }
    public void setActivo(String activo) { this.activo = activo; }

    public com.sicaproject.sica.iam.domain.Rol getRol() { return rol; }
    public void setRol(com.sicaproject.sica.iam.domain.Rol rol) { this.rol = rol; }

    public com.sicaproject.sica.personas.domain.Persona getPersona() { return persona; }
    public void setPersona(com.sicaproject.sica.personas.domain.Persona persona) { this.persona = persona; }
}