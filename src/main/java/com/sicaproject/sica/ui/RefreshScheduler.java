package com.sicaproject.sica.ui;

import com.sicaproject.sica.acceso.application.service.VisitaService;
import com.sicaproject.sica.auditoria.application.AuditoriaService;
import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.application.service.RbacService;
import com.sicaproject.sica.empresas.application.service.EmpresaService;
import com.sicaproject.sica.personas.application.service.PersonaService;
import javafx.application.Platform;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Componente central de sincronización y refresco automático de la interfaz de usuario.
 * Implementa el patrón Observer combinando un ScheduledExecutorService en segundo plano
 * con Platform.runLater() para sincronizar de manera segura los datos con el hilo UI de JavaFX (JavaFX Application Thread).
 */
public class RefreshScheduler {

    private static RefreshScheduler instance;

    private final ScheduledExecutorService scheduler;
    private final List<Refreshable> listeners = new ArrayList<>();
    private final VisitaService visitaService;
    private final PersonaService personaService;
    private final EmpresaService empresaService;
    private final RbacService rbacService;
    private final AuthService authService;
    private final AuditoriaService auditoriaService;

    public RefreshScheduler(VisitaService visitaService,
                            PersonaService personaService,
                            EmpresaService empresaService,
                            RbacService rbacService,
                            AuthService authService,
                            AuditoriaService auditoriaService) {
        this.visitaService = visitaService;
        this.personaService = personaService;
        this.empresaService = empresaService;
        this.rbacService = rbacService;
        this.authService = authService;
        this.auditoriaService = auditoriaService;
        // Hilo demonio en segundo plano para no bloquear el apagado de la aplicación
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "sica-refresh-scheduler");
            t.setDaemon(true);
            return t;
        });
    }

    public static RefreshScheduler getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RefreshScheduler no inicializado. Llamar a RefreshScheduler.init(...) desde SicaApplication");
        }
        return instance;
    }

    public static void init(VisitaService visitaService,
                            PersonaService personaService,
                            EmpresaService empresaService,
                            RbacService rbacService,
                            AuthService authService,
                            AuditoriaService auditoriaService) {
        if (instance == null) {
            instance = new RefreshScheduler(visitaService, personaService, empresaService, rbacService, authService, auditoriaService);
        }
    }

    /**
     * Registra un controlador de vista como observador para recibir notificaciones periódicas.
     */
    public synchronized void register(Refreshable listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Desregistra un controlador de vista al salir de pantalla.
     */
    public synchronized void unregister(Refreshable listener) {
        listeners.remove(listener);
    }

    public synchronized void clear() {
        listeners.clear();
    }

    /**
     * Inicia la tarea de temporización periódica en segundo plano.
     * Consulta y actualiza los controladores activos en el hilo de interfaz gráfica de JavaFX.
     * 
     * @param interval Intervalo de refresco (ej. Duration.seconds(5)).
     */
    public void startRefresh(Duration interval) {
        scheduler.scheduleWithFixedDelay(() -> {
            List<Refreshable> copy;
            synchronized (this) {
                if (listeners.isEmpty()) return;
                copy = new ArrayList<>(listeners);
            }
            // Delegar la actualización de los nodos visuales al JavaFX Application Thread
            Platform.runLater(() -> {
                for (Refreshable listener : copy) {
                    try {
                        listener.refreshData();
                    } catch (Exception e) {
                        // Ignorar excepciones transitorias de segundo plano
                    }
                }
            });
        }, (long) interval.toSeconds(), (long) interval.toSeconds(), TimeUnit.SECONDS);
    }

    /**
     * Detiene de manera ordenada el hilo ejecutor al cerrar la aplicación.
     */
    public void stop() {
        scheduler.shutdownNow();
    }

    /**
     * Interfaz observadora para controladores que requieren sincronización de datos en vivo.
     */
    public interface Refreshable {
        void refreshData();
    }
}