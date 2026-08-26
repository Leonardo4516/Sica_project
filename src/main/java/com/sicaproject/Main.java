package com.sicaproject;

import com.sicaproject.sica.empresas.domain.Empresa;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;

/**
 * Smoke test temporal de la capa de persistencia.
 * Se reemplaza por una Application de JavaFX cuando empecemos la UI (siguiente fase).
 * Requiere que ya hayas corrido schema.sql y data.sql contra tu PostgreSQL local.
 */
public class Main {
    public static void main(String[] args) {
        try {
            var empresas = CompositionRoot.getInstance().empresaService().listar();
            System.out.println("Conexión a BD exitosa. Empresas encontradas: " + empresas.size());
            for (Empresa e : empresas) {
                System.out.println(" - " + e.getNombre() + " (" + e.getNit() + ")");
            }
        } catch (Exception e) {
            System.err.println("Error conectando a la base de datos: " + e.getMessage());
            e.printStackTrace();
        } finally {
            JpaConfig.close();
        }
    }
}
