package com.sicaproject.sica.personas.domain;

/**
 * Entidad de Dominio que modela una persona en el sistema SICA (trabajador residente o invitado).
 * Almacena información de identificación (tipo y número de documento), pertenencia empresarial,
 * fotografía perimetral, contador de accesos y estado de restricción (Lista Negra).
 */
public class Persona {
    private long id;
    private String tipo = "INVITADO"; // Clasificación: TRABAJADOR o INVITADO
    private String tipoDocumento = "CC"; // Tipo de documento: CC, CE, PASAPORTE
    private String nombre;
    private String documento;
    private String fotoUrl; // Ruta local relativa o URL de la fotografía de seguridad
    private Long empresaId; // Identificador de la empresa empleadora (para trabajadores)
    private boolean bloqueado; // Indicador de restricción perimetral / lista negra
    private String motivoBloqueo; // Razón de seguridad que justifica el veto de acceso
    private int totalVisitas; // Contador histórico de visitas acumuladas

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