package com.sicaproject.sica.personas.domain;

public class Persona {
    private long id;
    private String tipo = "INVITADO"; // Valor por defecto
    private String tipoDocumento = "CC"; // Valor por defecto
    private String nombre;
    private String documento;
    private String fotoUrl;
    private Long empresaId;
    private boolean bloqueado;
    private String motivoBloqueo;
    private int totalVisitas;

    public Persona() {}

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getTipo() { return tipo; }
    public void setTipo(String tipo) { this.tipo = (tipo != null && !tipo.isEmpty()) ? tipo : "INVITADO"; }

    public String getTipoDocumento() { return tipoDocumento; }
    public void setTipoDocumento(String tipoDocumento) { 
        this.tipoDocumento = (tipoDocumento != null && !tipoDocumento.isEmpty()) ? tipoDocumento : "CC"; 
    }

    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getDocumento() { return documento; }
    public void setDocumento(String documento) { this.documento = documento; }

    public String getFotoUrl() { return fotoUrl; }
    public void setFotoUrl(String fotoUrl) { this.fotoUrl = fotoUrl; }

    public Long getEmpresaId() { return empresaId; }
    public void setEmpresaId(Long empresaId) { this.empresaId = empresaId; }

    public boolean isBloqueado() { return bloqueado; }
    public void setBloqueado(boolean bloqueado) { this.bloqueado = bloqueado; }

    public String getMotivoBloqueo() { return motivoBloqueo; }
    public void setMotivoBloqueo(String motivoBloqueo) { this.motivoBloqueo = motivoBloqueo; }

    public int getTotalVisitas() { return totalVisitas; }
    public void setTotalVisitas(int totalVisitas) { this.totalVisitas = totalVisitas; }
}