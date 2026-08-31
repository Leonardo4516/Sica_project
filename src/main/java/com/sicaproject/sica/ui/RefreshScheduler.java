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

    public synchronized void register(Refreshable listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public synchronized void unregister(Refreshable listener) {
        listeners.remove(listener);
    }

    public synchronized void clear() {
        listeners.clear();
    }

    public void startRefresh(Duration interval) {
        scheduler.scheduleWithFixedDelay(() -> {
            List<Refreshable> copy;
            synchronized (this) {
                if (listeners.isEmpty()) return;
                copy = new ArrayList<>(listeners);
            }
            Platform.runLater(() -> {
                for (Refreshable listener : copy) {
                    try {
                        listener.refreshData();
                    } catch (Exception e) {
                        // ignore background refresh errors
                    }
                }
            });
        }, (long) interval.toSeconds(), (long) interval.toSeconds(), TimeUnit.SECONDS);
    }

    public void stop() {
        scheduler.shutdownNow();
    }

    public interface Refreshable {
        void refreshData();
    }
}