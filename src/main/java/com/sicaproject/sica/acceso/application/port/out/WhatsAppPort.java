package com.sicaproject.sica.acceso.application.port.out;

import com.sicaproject.sica.acceso.domain.Visita;

public interface WhatsAppPort {
    void enviarRecordatorioAnfitrion(Visita visita);
}
