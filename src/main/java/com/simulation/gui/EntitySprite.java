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
    private double x, y;

    // Estado de animación
    private double animationTime = 0;
    private double spawnProgress = 0; // 0 a 1
    private boolean isSpawning = true;
    private double pulsePhase = Math.random() * Math.PI * 2;

    // Estado de progreso visual
    private ProcessingState processingState = ProcessingState.RAW;

    // Sistema de estela (reducido para optimización)
    private final ParticleSystem trailSystem;

    // Estados de procesamiento
    public enum ProcessingState {
        RAW, // Cubo gris - estado inicial
        HEAT_TREATED, // Cubo rojo - después del HORNO
        MACHINED // Engrane - después de mecanizado
    }

    public EntitySprite(Entity entity, double x, double y) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.trailSystem = new ParticleSystem(50); // Reducido de 100
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
        // Trail reducido para optimización
        if (!isSpawning && spawnProgress >= 1.0 && Math.random() < 0.3) {
            trailSystem.emitTrail(x, y, 0, 0, getStateColor().deriveColor(0, 1, 1, 0.4));
        }

        // Renderizar estela primero
        trailSystem.render(gc);

        // Calcular tamaño con efecto de spawn
        double currentSize = baseSize * AnimationEasing.easeOutBack(spawnProgress);

        // Efecto de pulso más sutil
        double pulseSize = currentSize + Math.sin(animationTime * 2 + pulsePhase) * 0.3;

        // Dibujar sombra
        VisualEffects.drawSoftShadow(gc, x - currentSize / 2, y - currentSize / 2,
                currentSize, currentSize, 3);

        // Dibujar resplandor exterior (reducido)
        double glowIntensity = 0.5 + Math.sin(animationTime * 2 + pulsePhase) * 0.3;
        gc.setGlobalAlpha(glowIntensity * 0.4);
        gc.setFill(getStateColor().brighter());
        gc.fillOval(x - pulseSize, y - pulseSize, pulseSize * 2, pulseSize * 2);
        gc.setGlobalAlpha(1.0);

        // Dibujar forma según estado
        drawStateShape(gc, currentSize);

        // Indicador de ID pequeño
        gc.setFill(Color.WHITE);
        gc.setFont(javafx.scene.text.Font.font("Arial", 6));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.fillText(String.valueOf(entity.getId()), x, y + currentSize + 6);
    }

    /**
     * Dibuja la forma según el estado de procesamiento
     */
    private void drawStateShape(GraphicsContext gc, double size) {
        Color stateColor = getStateColor();

        switch (processingState) {
            case RAW:
                // Cubo gris simple
                drawCube(gc, size, stateColor);
                break;

            case HEAT_TREATED:
                // Cubo rojo con efecto de calor
                drawCube(gc, size, stateColor);
                // Efecto de brillo rojo pulsante
                gc.setGlobalAlpha(0.3 + Math.sin(animationTime * 4) * 0.2);
                gc.setFill(Color.ORANGERED);
                gc.fillRect(x - size / 2, y - size / 2, size, size);
                gc.setGlobalAlpha(1.0);
                break;

            case MACHINED:
                // Engrane complejo
                drawGear(gc, size, stateColor);
                break;
        }
    }

    /**
     * Dibuja un cubo con efecto 3D
     */
    private void drawCube(GraphicsContext gc, double size, Color color) {
        // Cuerpo principal
        RadialGradient gradient = ColorPalette.createGlowGradient(color, 0.8);
        gc.setFill(gradient);
        gc.fillRect(x - size / 2, y - size / 2, size, size);

        // Borde
        gc.setStroke(color.brighter());
        gc.setLineWidth(1.5);
        gc.strokeRect(x - size / 2, y - size / 2, size, size);

        // Highlight superior
        gc.setFill(Color.color(1, 1, 1, 0.3));
        gc.fillRect(x - size / 2, y - size / 2, size, size * 0.3);
    }

    /**
     * Dibuja un engrane con dientes
     */
    private void drawGear(GraphicsContext gc, double size, Color color) {
        double centerX = x;
        double centerY = y;
        double outerRadius = size / 2;
        double innerRadius = size * 0.35;
        int teethCount = 8;

        // Rotar el engrane lentamente
        double rotation = animationTime * 30;

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(rotation);

        // Dibujar dientes del engrane
        gc.setFill(color);
        for (int i = 0; i < teethCount; i++) {
            double angle = (360.0 / teethCount) * i;
            double rad = Math.toRadians(angle);

            double x1 = Math.cos(rad) * innerRadius;
            double y1 = Math.sin(rad) * innerRadius;
            double x2 = Math.cos(rad) * outerRadius;
            double y2 = Math.sin(rad) * outerRadius;

            double toothWidth = Math.toRadians(360.0 / teethCount / 3);
            double x3 = Math.cos(rad + toothWidth) * outerRadius;
            double y3 = Math.sin(rad + toothWidth) * outerRadius;
            double x4 = Math.cos(rad + toothWidth) * innerRadius;
            double y4 = Math.sin(rad + toothWidth) * innerRadius;

            gc.fillPolygon(
                    new double[] { x1, x2, x3, x4 },
                    new double[] { y1, y2, y3, y4 },
                    4);
        }

        // Círculo interior
        RadialGradient gradient = ColorPalette.createGlowGradient(color, 1.0);
        gc.setFill(gradient);
        gc.fillOval(-innerRadius, -innerRadius, innerRadius * 2, innerRadius * 2);

        // Borde brillante
        gc.setStroke(color.brighter());
        gc.setLineWidth(1);
        gc.strokeOval(-outerRadius, -outerRadius, outerRadius * 2, outerRadius * 2);

        // Centro
        gc.setFill(Color.color(0.3, 0.3, 0.3));
        double holeRadius = innerRadius * 0.4;
        gc.fillOval(-holeRadius, -holeRadius, holeRadius * 2, holeRadius * 2);

        gc.restore();
    }

    /**
     * Obtiene el color según el estado
     */
    private Color getStateColor() {
        switch (processingState) {
            case RAW:
                return Color.GRAY; // Gris
            case HEAT_TREATED:
                return Color.web("#e74c3c"); // Rojo
            case MACHINED:
                return ColorPalette.ACCENT_CYAN; // Cyan metálico
            default:
                return Color.GRAY;
        }
    }

    /**
     * Actualiza el estado de procesamiento
     */
    public void setProcessingState(ProcessingState state) {
        if (this.processingState != state) {
            this.processingState = state;
            // Pequeña explosión visual al cambiar de estado
            trailSystem.emitExplosion(x, y, 8, getStateColor());
        }
    }

    public ProcessingState getProcessingState() {
        return processingState;
    }

    /**
     * Inicia animación de spawn
     */
    public void spawn() {
        isSpawning = true;
        spawnProgress = 0;

        // Efecto de explosión al aparecer (reducido)
        trailSystem.emitExplosion(x, y, 8, getStateColor());
    }

    /**
     * Efecto de despawn (desaparición)
     */
    public void despawn() {
        trailSystem.emitExplosion(x, y, 15, getStateColor());
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
