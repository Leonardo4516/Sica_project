package com.sicaproject.sica.iam.domain;

public class Permiso {
    private long id;
    private String codigo;
    private String descripcion;

    public Permiso() {}

    public Permiso(long id, String codigo, String descripcion) {
        this.id = id;
        this.codigo = codigo;
        this.descripcion = descripcion;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getCodigo() { return codigo; }
    public void setCodigo(String codigo) { this.codigo = codigo; }

    public String getDescripcion() { return descripcion; }
    public void setDescripcion(String descripcion) { this.descripcion = descripcion; }
}
