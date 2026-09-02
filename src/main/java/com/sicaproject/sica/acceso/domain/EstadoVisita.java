package com.sicaproject.sica.acceso.domain;

/**
 * Máquina de estados finitos que modela el ciclo de vida de una visita o acceso en SICA.
 * Define las transiciones válidas para garantizar la integridad operativa del complejo Zona Acme.
 * 
 * Estados posibles:
 * - APROBADA: Pre-registrada por funcionario o aprobada tras solicitud; habilitada para Check-in.
 * - PENDIENTE_APROBACION: Solicitud originada en portería a la espera de visto bueno del anfitrión.
 * - DENTRO: La persona se encuentra físicamente dentro de las instalaciones del complejo.
 * - RECHAZADA: Solicitud denegada por el anfitrión.
 * - CERRADA: Salida física normal registrada por el guarda en portería (Check-out).
 * - CERRADA_POR_SISTEMA: Salida regularizada automáticamente al detectar reingreso sin salida previa (Flujo 4).
 */
public enum EstadoVisita {
    APROBADA,
    PENDIENTE_APROBACION,
    DENTRO,
    RECHAZADA,
    CERRADA,
    CERRADA_POR_SISTEMA;

    /**
     * Valida si una transición de estado es operacionalmente válida según las reglas de negocio.
     * 
     * Reglas de transición:
     * - APROBADA -> DENTRO (Únicamente mediante Check-in en portería).
     * - PENDIENTE_APROBACION -> APROBADA o RECHAZADA (Decisión exclusiva del anfitrión).
     * - DENTRO -> CERRADA (Check-out) o CERRADA_POR_SISTEMA (Salida olvidada / Flujo 4).
     * - Estados terminales (RECHAZADA, CERRADA, CERRADA_POR_SISTEMA) no admiten nuevas transiciones.
     * 
     * @param destino Estado objetivo propuesto.
     * @return true si la transición está permitida, false si es inconsistente.
     */
    public boolean puedeTransicionarA(EstadoVisita destino) {
        return switch (this) {
            case APROBADA -> destino == DENTRO;
            case PENDIENTE_APROBACION -> destino == APROBADA || destino == RECHAZADA;
            case DENTRO -> destino == CERRADA || destino == CERRADA_POR_SISTEMA;
            default -> false;
        };
    }
}