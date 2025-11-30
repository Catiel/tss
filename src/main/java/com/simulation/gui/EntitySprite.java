package com.simulation.gui;

import com.simulation.entities.Entity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.RadialGradient;

/**
 * Sprite mejorado de entidad con efectos visuales profesionales
 */
public class EntitySprite {

    private final Entity entity;
    private final double baseSize = 10;
    private final Color baseColor;
    private final Color glowColor;
    private double x, y;

    // Estado de animación
    private double animationTime = 0;
    private double spawnProgress = 0; // 0 a 1
    private boolean isSpawning = true;
    private double pulsePhase = Math.random() * Math.PI * 2;

    // Sistema de estela
    private final ParticleSystem trailSystem;

    public EntitySprite(Entity entity, double x, double y) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.baseColor = ColorPalette.ENTITY_PRIMARY;
        this.glowColor = ColorPalette.ACCENT_CYAN;
        this.trailSystem = new ParticleSystem(100);
    }

    /**
     * Actualiza el estado de animación
     */
    public void update(double deltaTime) {
        animationTime += deltaTime;

        // Animación de spawn
        if (isSpawning) {
            spawnProgress = Math.min(1.0, spawnProgress + deltaTime * 3);
            if (spawnProgress >= 1.0) {
                isSpawning = false;
            }
        }

        // Actualizar sistema de estela
        trailSystem.update(deltaTime);
    }

    public void draw(GraphicsContext gc) {
        // Actualizar trail si está en movimiento (spawn completo)
        if (!isSpawning && spawnProgress >= 1.0) {
            // Agregar partículas de estela ocasionalmente
            if (Math.random() < 0.5) {
                trailSystem.emitTrail(x, y, 0, 0,
                        Color.color(baseColor.getRed(), baseColor.getGreen(),
                                baseColor.getBlue(), 0.6));
            }
        }

        // Renderizar estela primero
        trailSystem.render(gc);

        // Calcular tamaño con efecto de spawn
        double currentSize = baseSize * AnimationEasing.easeOutBack(spawnProgress);

        // Efecto de pulso sutil
        double pulseSize = currentSize + Math.sin(animationTime * 3 + pulsePhase) * 0.5;

        // Dibujar sombra suave
        VisualEffects.drawSoftShadow(gc, x - currentSize / 2, y - currentSize / 2,
                currentSize, currentSize, 4);

        // Dibujar resplandor exterior
        double glowIntensity = 0.6 + Math.sin(animationTime * 2 + pulsePhase) * 0.4;
        gc.setGlobalAlpha(glowIntensity * 0.5);
        gc.setFill(glowColor);
        gc.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
        gc.setGlobalAlpha(1.0);

        // Dibujar cuerpo principal con gradiente radial
        RadialGradient gradient = ColorPalette.createGlowGradient(baseColor, 1.0);
        gc.setFill(gradient);
        gc.fillOval(x - currentSize / 2, y - currentSize / 2, currentSize, currentSize);

        // Borde brillante
        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(2);
        gc.strokeOval(x - currentSize / 2, y - currentSize / 2, currentSize, currentSize);

        // Punto brillante central
        gc.setFill(Color.WHITE);
        double highlightSize = currentSize * 0.3;
        gc.fillOval(x - currentSize / 4, y - currentSize / 4, highlightSize, highlightSize);

        // Indicador de ID (opcional, pequeño)
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 7));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(String.valueOf(entity.getId()), x, y + currentSize + 8);
    }

    /**
     * Inicia animación de spawn
     */
    public void spawn() {
        isSpawning = true;
        spawnProgress = 0;

        // Efecto de explosión al aparecer
        trailSystem.emitExplosion(x, y, 12, baseColor);
    }

    /**
     * Efecto de despawn (desaparición)
     */
    public void despawn() {
        trailSystem.emitExplosion(x, y, 20, baseColor);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Entity getEntity() {
        return entity;
    }

    public ParticleSystem getTrailSystem() {
        return trailSystem;
    }
}
