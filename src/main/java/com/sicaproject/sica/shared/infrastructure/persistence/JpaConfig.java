package com.sicaproject.sica.shared.infrastructure.persistence;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

/**
 * Punto único de acceso al EntityManagerFactory.
 * Lee la URL/usuario/password de variables de entorno si existen,
 * para no dejar credenciales quemadas en persistence.xml en producción.
 */
public final class JpaConfig {

    private static final EntityManagerFactory EMF = build();

    private JpaConfig() {}

    private static EntityManagerFactory build() {
        Map<String, String> overrides = new HashMap<>();
        putIfPresent(overrides, "jakarta.persistence.jdbc.url", "SICA_DB_URL");
        putIfPresent(overrides, "jakarta.persistence.jdbc.user", "SICA_DB_USER");
        putIfPresent(overrides, "jakarta.persistence.jdbc.password", "SICA_DB_PASSWORD");
        return Persistence.createEntityManagerFactory("sica-pu", overrides);
    }

    private static void putIfPresent(Map<String, String> map, String jpaKey, String envVar) {
        String value = System.getenv(envVar);
        if (value != null && !value.isBlank()) {
            map.put(jpaKey, value);
        }
    }

    public static EntityManager newEntityManager() {
        return EMF.createEntityManager();
    }

    public static void close() {
        if (EMF.isOpen()) {
            EMF.close();
        }
    }
}
