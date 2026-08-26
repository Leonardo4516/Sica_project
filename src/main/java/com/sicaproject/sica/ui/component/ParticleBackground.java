package com.sicaproject.sica.ui.component;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Fondo animado con partículas flotantes para las pantallas SICA.
 * Se dibuja en un Canvas superpuesto detrás del contenido.
 * Implementa una red suave de puntos conectados estilo "data network".
 */
public class ParticleBackground extends Pane {

    private static final int PARTICLE_COUNT = 60;
    private static final double CONNECTION_DISTANCE = 140.0;
    private static final double PARTICLE_SPEED = 0.4;

    private final Canvas canvas;
    private final List<Particle> particles = new ArrayList<>();
    private final Timeline timeline;
    private final Random random = new Random();
    private int width = 800;
    private int height = 600;

    public ParticleBackground() {
        canvas = new Canvas();
        canvas.widthProperty().bind(widthProperty());
        canvas.heightProperty().bind(heightProperty());
        getChildren().add(canvas);

        widthProperty().addListener((obs, o, n) -> {
            width = n.intValue();
            canvas.setWidth(width);
        });
        heightProperty().addListener((obs, o, n) -> {
            height = n.intValue();
            canvas.setHeight(height);
        });

        for (int i = 0; i < PARTICLE_COUNT; i++) {
            particles.add(new Particle(random));
        }

        timeline = new Timeline(new KeyFrame(Duration.millis(33), e -> animate()));
        timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        timeline.play();
    }

    public void stop() {
        timeline.stop();
    }

    private void animate() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.clearRect(0, 0, width, height);

        for (Particle p : particles) {
            p.update(width, height);
            p.draw(gc);
        }

        gc.setStroke(Color.rgb(107, 138, 253, 0.18));
        gc.setLineWidth(0.6);
        for (int i = 0; i < particles.size(); i++) {
            Particle a = particles.get(i);
            for (int j = i + 1; j < particles.size(); j++) {
                Particle b = particles.get(j);
                double dx = a.x - b.x;
                double dy = a.y - b.y;
                double dist = Math.sqrt(dx * dx + dy * dy);
                if (dist < CONNECTION_DISTANCE) {
                    double alpha = 0.22 * (1.0 - dist / CONNECTION_DISTANCE);
                    gc.setStroke(Color.rgb(107, 138, 253, alpha));
                    gc.strokeLine(a.x, a.y, b.x, b.y);
                }
            }
        }
    }

    private static class Particle {
        double x, y, vx, vy, radius;

        Particle(Random r) {
            x = r.nextDouble() * 1200;
            y = r.nextDouble() * 800;
            vx = (r.nextDouble() - 0.5) * PARTICLE_SPEED;
            vy = (r.nextDouble() - 0.5) * PARTICLE_SPEED;
            radius = 1.2 + r.nextDouble() * 1.8;
        }

        void update(int w, int h) {
            x += vx;
            y += vy;
            if (x < 0 || x > w) vx = -vx;
            if (y < 0 || y > h) vy = -vy;
        }

        void draw(GraphicsContext gc) {
            gc.setFill(Color.rgb(107, 138, 253, 0.7));
            gc.fillOval(x - radius, y - radius, radius * 2, radius * 2);
        }
    }
}
