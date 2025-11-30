package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Representa una partícula individual del sistema
 */
class Particle {
    double x, y; // Posición
    double vx, vy; // Velocidad
    double life; // Vida restante (0-1)
    double maxLife; // Vida máxima
    double size; // Tamaño
    Color color; // Color
    double alpha; // Opacidad
    double rotation; // Rotación
    double rotationSpeed; // Velocidad de rotación
    ParticleType type; // Tipo de partícula

    public Particle(double x, double y, double vx, double vy, double life,
            double size, Color color, ParticleType type) {
        this.x = x;
        this.y = y;
        this.vx = vx;
        this.vy = vy;
        this.life = life;
        this.maxLife = life;
        this.size = size;
        this.color = color;
        this.alpha = 1.0;
        this.rotation = Math.random() * 360;
        this.rotationSpeed = (Math.random() - 0.5) * 180;
        this.type = type;
    }

    public void update(double deltaTime) {
        // Actualizar posición
        x += vx * deltaTime;
        y += vy * deltaTime;

        // Actualizar rotación
        rotation += rotationSpeed * deltaTime;

        // Decrementar vida
        life -= deltaTime;

        // Actualizar opacidad basada en vida
        alpha = Math.max(0, life / maxLife);
    }

    public boolean isDead() {
        return life <= 0;
    }

    public void draw(GraphicsContext gc) {
        gc.save();
        gc.setGlobalAlpha(alpha);

        switch (type) {
            case SMOKE:
                drawSmoke(gc);
                break;
            case SPARK:
                drawSpark(gc);
                break;
            case GLOW:
                drawGlow(gc);
                break;
            case TRAIL:
                drawTrail(gc);
                break;
            case EMBER:
                drawEmber(gc);
                break;
        }

        gc.restore();
    }

    private void drawSmoke(GraphicsContext gc) {
        // Humo con gradiente radial
        double currentSize = size * (1.5 - life / maxLife);
        gc.setFill(color);

        // Múltiples círculos para efecto de difusión
        for (int i = 0; i < 3; i++) {
            double offset = i * 2;
            double layerAlpha = alpha * (1.0 - i * 0.3);
            gc.setGlobalAlpha(layerAlpha);
            gc.fillOval(x - currentSize / 2 - offset, y - currentSize / 2 - offset,
                    currentSize + offset * 2, currentSize + offset * 2);
        }
    }

    private void drawSpark(GraphicsContext gc) {
        // Chispa como línea brillante
        double length = size * (life / maxLife);
        gc.setStroke(color.brighter());
        gc.setLineWidth(2);

        // Línea en dirección de movimiento
        double angle = Math.atan2(vy, vx);
        double x2 = x - Math.cos(angle) * length;
        double y2 = y - Math.sin(angle) * length;

        gc.strokeLine(x, y, x2, y2);

        // Punto brillante al final
        gc.setFill(Color.WHITE);
        gc.fillOval(x - 2, y - 2, 4, 4);
    }

    private void drawGlow(GraphicsContext gc) {
        // Resplandor con capas
        for (int i = 3; i > 0; i--) {
            double scale = 1.0 + (i * 0.3);
            double layerAlpha = alpha * (1.0 - i / 4.0);
            gc.setGlobalAlpha(layerAlpha);
            gc.setFill(color);

            double s = size * scale;
            gc.fillOval(x - s / 2, y - s / 2, s, s);
        }
    }

    private void drawTrail(GraphicsContext gc) {
        // Estela alargada
        double w = size;
        double h = size * 3;
        gc.setFill(color);
        gc.fillOval(x - w / 2, y - h / 2, w, h);
    }

    private void drawEmber(GraphicsContext gc) {
        // Brasa con núcleo brillante
        gc.setFill(color.brighter());
        gc.fillOval(x - size / 2, y - size / 2, size, size);

        // Resplandor naranja exterior
        gc.setGlobalAlpha(alpha * 0.5);
        gc.setFill(Color.ORANGE);
        gc.fillOval(x - size, y - size, size * 2, size * 2);
    }
}

/**
 * Tipos de partículas disponibles
 */
enum ParticleType {
    SMOKE, // Humo
    SPARK, // Chispa
    GLOW, // Resplandor
    TRAIL, // Estela
    EMBER // Brasa
}

/**
 * Sistema de partículas profesional para efectos visuales
 */
public class ParticleSystem {
    private final List<Particle> particles;
    private final int maxParticles;

    public ParticleSystem(int maxParticles) {
        this.particles = new ArrayList<>();
        this.maxParticles = maxParticles;
    }

    /**
     * Emite humo
     */
    public void emitSmoke(double x, double y, int count, Color smokeColor) {
        for (int i = 0; i < count && particles.size() < maxParticles; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 10 + Math.random() * 20;
            double vx = Math.cos(angle) * speed;
            double vy = -30 - Math.random() * 20; // Sube
            double life = 2.0 + Math.random() * 2.0;
            double size = 8 + Math.random() * 12;

            particles.add(new Particle(x, y, vx, vy, life, size, smokeColor, ParticleType.SMOKE));
        }
    }

    /**
     * Emite chispas
     */
    public void emitSparks(double x, double y, int count, Color sparkColor) {
        for (int i = 0; i < count && particles.size() < maxParticles; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 50 + Math.random() * 100;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed - 20; // Ligero sesgo hacia arriba
            double life = 0.3 + Math.random() * 0.5;
            double size = 4 + Math.random() * 6;

            particles.add(new Particle(x, y, vx, vy, life, size, sparkColor, ParticleType.SPARK));
        }
    }

    /**
     * Emite resplandor pulsante
     */
    public void emitGlow(double x, double y, Color glowColor, double intensity) {
        if (particles.size() < maxParticles) {
            double life = 0.5;
            double size = 20 * intensity;

            particles.add(new Particle(x, y, 0, 0, life, size, glowColor, ParticleType.GLOW));
        }
    }

    /**
     * Emite estela de movimiento
     */
    public void emitTrail(double x, double y, double vx, double vy, Color trailColor) {
        if (particles.size() < maxParticles) {
            double life = 0.3;
            double size = 4;

            particles.add(new Particle(x, y, vx * 0.5, vy * 0.5, life, size, trailColor, ParticleType.TRAIL));
        }
    }

    /**
     * Emite brasas/ascuas
     */
    public void emitEmbers(double x, double y, int count) {
        for (int i = 0; i < count && particles.size() < maxParticles; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 5 + Math.random() * 15;
            double vx = Math.cos(angle) * speed;
            double vy = -20 - Math.random() * 30; // Sube
            double life = 1.5 + Math.random() * 2.0;
            double size = 3 + Math.random() * 4;

            Color emberColor = Color.color(1.0, 0.3 + Math.random() * 0.3, 0);

            particles.add(new Particle(x, y, vx, vy, life, size, emberColor, ParticleType.EMBER));
        }
    }

    /**
     * Explosión de partículas
     */
    public void emitExplosion(double x, double y, int count, Color color) {
        for (int i = 0; i < count && particles.size() < maxParticles; i++) {
            double angle = Math.random() * Math.PI * 2;
            double speed = 80 + Math.random() * 120;
            double vx = Math.cos(angle) * speed;
            double vy = Math.sin(angle) * speed;
            double life = 0.5 + Math.random() * 1.0;
            double size = 5 + Math.random() * 10;

            ParticleType type = Math.random() < 0.7 ? ParticleType.SPARK : ParticleType.GLOW;

            particles.add(new Particle(x, y, vx, vy, life, size, color, type));
        }
    }

    /**
     * Actualiza todas las partículas
     */
    public void update(double deltaTime) {
        Iterator<Particle> iterator = particles.iterator();
        while (iterator.hasNext()) {
            Particle p = iterator.next();
            p.update(deltaTime);

            // Aplicar gravedad a ciertos tipos
            if (p.type == ParticleType.SPARK || p.type == ParticleType.EMBER) {
                p.vy += 98 * deltaTime; // Gravedad
            }

            // Aplicar fricción al humo
            if (p.type == ParticleType.SMOKE) {
                p.vx *= 0.98;
                p.vy *= 0.98;
            }

            // Eliminar partículas muertas
            if (p.isDead()) {
                iterator.remove();
            }
        }
    }

    /**
     * Renderiza todas las partículas
     */
    public void render(GraphicsContext gc) {
        for (Particle p : particles) {
            p.draw(gc);
        }
    }

    /**
     * Limpia todas las partículas
     */
    public void clear() {
        particles.clear();
    }

    /**
     * Obtiene el número actual de partículas
     */
    public int getParticleCount() {
        return particles.size();
    }
}
