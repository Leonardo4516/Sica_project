package com.sicaproject.sica.empresas.domain;

/**
 * Entidad de Dominio que modela una Empresa residente en el Complejo Empresarial Zona Acme.
 * Identifica la razón social receptora de visitas y empleadora de trabajadores en el complejo.
 */
public class Empresa {
    private long id;
    private String nombre; // Razón social comercial de la empresa
    private String nit; // Número de Identificación Tributaria único

    public Empresa() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getNit() { return nit; }
    public void setNit(String nit) { this.nit = nit; }

    @Override
    public String toString() {
        return nombre;
    }
}