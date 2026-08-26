package com.sicaproject.sica.acceso.domain;

public enum EstadoVisita {
    APROBADA,
    PENDIENTE_APROBACION,
    DENTRO,
    RECHAZADA,
    CERRADA,
    CERRADA_POR_SISTEMA;

    public boolean puedeTransicionarA(EstadoVisita destino) {
        return switch (this) {
            case APROBADA -> destino == DENTRO;
            case PENDIENTE_APROBACION -> destino == APROBADA || destino == RECHAZADA;
            case DENTRO -> destino == CERRADA || destino == CERRADA_POR_SISTEMA;
            default -> false;
        };
    }
}