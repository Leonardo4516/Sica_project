package com.sicaproject.sica;

import com.sicaproject.sica.shared.infrastructure.persistence.JpaConfig;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

public final class TestDatabaseCleaner {

    private TestDatabaseCleaner() {}

    public static void limpiarDatosDePrueba() {
        EntityManager em = JpaConfig.newEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Eliminar registros de prueba creados durante los tests
            em.createNativeQuery("DELETE FROM bitacora_auditoria WHERE id > 5 OR detalle LIKE '%TEST%' OR detalle LIKE '%Test%'").executeUpdate();
            em.createNativeQuery("DELETE FROM incidente WHERE id > 2 OR titulo LIKE '%TEST%' OR titulo LIKE '%Test%'").executeUpdate();
            em.createNativeQuery("DELETE FROM visita WHERE id > 6 OR observaciones LIKE '%TEST%' OR observaciones LIKE '%Test%' OR observaciones LIKE '%F1%' OR observaciones LIKE '%F2%' OR observaciones LIKE '%F3%' OR observaciones LIKE '%F4%'").executeUpdate();
            em.createNativeQuery("DELETE FROM persona WHERE id > 10 OR documento LIKE '%TEST%' OR documento LIKE '%EVAC%' OR documento LIKE '%DOC-%' OR documento LIKE '%INC-%' OR documento LIKE '%F1-%' OR documento LIKE '%F2-%' OR documento LIKE '%F3-%' OR documento LIKE '%F4-%' OR documento LIKE '%RBAC-%' OR nombre LIKE '%Test%'").executeUpdate();
            
            // Restaurar estado base de personas maestras
            em.createNativeQuery("UPDATE persona SET bloqueado = TRUE, motivo_bloqueo = 'Incumplimiento reiterado de normas de seguridad industrial en planta' WHERE id = 10").executeUpdate();
            em.createNativeQuery("UPDATE persona SET bloqueado = FALSE, motivo_bloqueo = NULL WHERE id < 10").executeUpdate();
            
            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
