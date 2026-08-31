package com.sicaproject.sica.ui.controller;

import com.sicaproject.sica.iam.application.service.AuthService;
import com.sicaproject.sica.iam.domain.Usuario;
import com.sicaproject.sica.shared.infrastructure.config.CompositionRoot;
import com.sicaproject.sica.ui.SceneManager;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;

public class LoginController implements Initializable {

    @FXML private TextField txtUsuario;
    @FXML private PasswordField txtPassword;
    @FXML private CheckBox chkRecordar;
    @FXML private Label lblError;
    @FXML private Button btnLogin;
    @FXML private StackPane rootPane;
    @FXML private Canvas canvasParticles;

    private final AuthService authService = CompositionRoot.getInstance().authService();
    private AnimationTimer particleTimer;
    private final List<Particle> particles = new ArrayList<>();
    private final Random random = new Random();

    private static class Particle {
        double x, y;
        double vx, vy;
        double radius;
        double alpha;

        Particle(double width, double height, Random rand) {
            this.x = rand.nextDouble() * width;
            this.y = rand.nextDouble() * height;
            this.vx = (rand.nextDouble() - 0.5) * 0.7;
            this.vy = (rand.nextDouble() - 0.5) * 0.7;
            this.radius = 2.0 + rand.nextDouble() * 2.5;
            this.alpha = 0.2 + rand.nextDouble() * 0.45;
        }

        void update(double width, double height) {
            x += vx;
            y += vy;
            if (x < 0) { x = width; }
            else if (x > width) { x = 0; }
            if (y < 0) { y = height; }
            else if (y > height) { y = 0; }
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initParticles();
    }

    private void initParticles() {
        if (canvasParticles == null) return;

        canvasParticles.widthProperty().bind(rootPane.widthProperty());
        canvasParticles.heightProperty().bind(rootPane.heightProperty());

        GraphicsContext gc = canvasParticles.getGraphicsContext2D();

        // Inicializar 35 particulas flotantes
        for (int i = 0; i < 35; i++) {
            particles.add(new Particle(1280, 720, random));
        }

        particleTimer = new AnimationTimer() {
            @Override
            public void handle(long now) {
                double w = canvasParticles.getWidth();
                double h = canvasParticles.getHeight();
                if (w <= 0 || h <= 0) return;

                gc.clearRect(0, 0, w, h);

                // Dibujar lineas de conexion cercanas
                for (int i = 0; i < particles.size(); i++) {
                    Particle p1 = particles.get(i);
                    for (int j = i + 1; j < particles.size(); j++) {
                        Particle p2 = particles.get(j);
                        double dx = p1.x - p2.x;
                        double dy = p1.y - p2.y;
                        double dist = Math.sqrt(dx * dx + dy * dy);
                        if (dist < 110) {
                            double lineAlpha = (1.0 - dist / 110.0) * 0.15;
                            gc.setStroke(Color.rgb(99, 102, 241, lineAlpha));
                            gc.setLineWidth(0.8);
                            gc.strokeLine(p1.x, p1.y, p2.x, p2.y);
                        }
                    }
                }

                // Dibujar particulas
                for (Particle p : particles) {
                    p.update(w, h);
                    gc.setFill(Color.rgb(129, 140, 248, p.alpha));
                    gc.fillOval(p.x - p.radius, p.y - p.radius, p.radius * 2, p.radius * 2);
                }
            }
        };

        particleTimer.start();
    }

    public void stopParticleAnimation() {
        if (particleTimer != null) {
            particleTimer.stop();
            particleTimer = null;
        }
    }

    @FXML
    private void handleLogin(ActionEvent event) {
        String usuario = txtUsuario.getText().trim();
        String password = txtPassword.getText();

        if (usuario.isEmpty() || password.isEmpty()) {
            showError("Por favor ingrese usuario y contraseña");
            return;
        }

        btnLogin.setDisable(true);
        try {
            Optional<Usuario> result = authService.login(usuario, password);
            if (result.isPresent()) {
                Usuario u = result.get();
                stopParticleAnimation();
                SceneManager.navigateAfterLogin(u);
            } else {
                showError("Credenciales inválidas");
            }
        } catch (Exception e) {
            showError("Error de autenticación: " + e.getMessage());
        } finally {
            btnLogin.setDisable(false);
        }
    }

    private void showError(String msg) {
        lblError.setText(msg);
        lblError.setOpacity(1.0);
        FadeTransition ft = new FadeTransition(Duration.seconds(4), lblError);
        ft.setFromValue(1.0);
        ft.setToValue(0.0);
        ft.setDelay(Duration.seconds(2));
        ft.play();
    }
}
