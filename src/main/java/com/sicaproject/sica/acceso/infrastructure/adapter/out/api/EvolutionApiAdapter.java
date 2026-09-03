package com.sicaproject.sica.acceso.infrastructure.adapter.out.api;

import com.sicaproject.sica.acceso.application.port.out.WhatsAppPort;
import com.sicaproject.sica.acceso.domain.Visita;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

public class EvolutionApiAdapter implements WhatsAppPort {

    // =========================================================================
    // CONFIGURACIÓN DE EVOLUTION API
    // =========================================================================
    private static final String API_URL = "http://localhost:8080";
    private static final String API_KEY = "sica_secret_key_123";
    private static final String INSTANCE_NAME = "SicaBot";
    
    // TODO: Reemplaza con tu número real incluyendo código de país sin el símbolo + (Ej: 573001234567)
    private static final String NUMERO_DESTINO = "573181365555";
    // =========================================================================

    private final HttpClient httpClient;

    public EvolutionApiAdapter() {
        this.httpClient = HttpClient.newHttpClient();
    }

    @Override
    public void enviarRecordatorioAnfitrion(Visita visita) {
        if ("TU_NUMERO_AQUI".equals(NUMERO_DESTINO)) {
            System.err.println("[EvolutionAPI] AVISO: Configura tu número en EvolutionApiAdapter para enviar el WhatsApp.");
            return;
        }

        try {
            String nombreVisitante = visita.getPersona() != null ? visita.getPersona().getNombre() : "Desconocido";
            String documento = visita.getPersona() != null ? visita.getPersona().getDocumento() : "N/A";
            
            String mensaje = "🚨 *SICA ALERTA* 🚨\\n\\n" +
                             "Tienes un visitante esperando aprobación en recepción:\\n" +
                             "👤 *" + nombreVisitante + "* (C.C. " + documento + ")\\n\\n" +
                             "Ingresa al sistema para gestionar el acceso.";

            String url = API_URL + "/message/sendText/" + INSTANCE_NAME;

            // Construir JSON a mano para no añadir librerías como Jackson/Gson
            String jsonBody = "{"
                    + "\"number\": \"" + NUMERO_DESTINO + "\","
                    + "\"options\": {\"delay\": 1200, \"presence\": \"composing\"},"
                    + "\"text\": \"" + mensaje + "\""
                    + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Content-Type", "application/json")
                    .header("apikey", API_KEY)
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody, StandardCharsets.UTF_8))
                    .build();

            httpClient.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                    .thenAccept(response -> {
                        if (response.statusCode() >= 200 && response.statusCode() < 300) {
                            System.out.println("[EvolutionAPI] ✅ WhatsApp enviado a " + NUMERO_DESTINO);
                        } else {
                            System.err.println("[EvolutionAPI] ❌ Error HTTP " + response.statusCode() + ": " + response.body());
                        }
                    })
                    .exceptionally(ex -> {
                        System.err.println("[EvolutionAPI] Falló la conexión: ¿Está el contenedor corriendo? " + ex.getMessage());
                        return null;
                    });

        } catch (Exception e) {
            System.err.println("[EvolutionAPI] Error inesperado: " + e.getMessage());
        }
    }
}
