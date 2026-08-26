package com.sicaproject.sica.empresas.domain;

public class Empresa {
    private long id;
    private String nombre;
    private String nit;

    public Empresa() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }
}