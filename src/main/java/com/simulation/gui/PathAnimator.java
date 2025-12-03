package com.simulation.gui;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Animador de trayectorias mejorado con curvas Bezier y easing functions
 */
public class PathAnimator {

    private final EntitySprite entity;
    private final ResourceSprite resource;
    private final LocationNode from;
    private final LocationNode to;
    private final double duration;
    private final Runnable onComplete;
    private final boolean withResource;

    // Posiciones iniciales y finales
    private final double startX;
    private final double startY;
    private final double endX;
    private final double endY;

    // Punto de control para curva Bezier
    private final double controlX;
    private final double controlY;

    private double elapsed = 0;
    private boolean finished = false;

    // Configuración de animación
    private final boolean useBezierCurve;
    private final String easingType;

    /**
     * Constructor simple sin recurso
     */
    public PathAnimator(EntitySprite entity, LocationNode from, LocationNode to,
            double duration, Runnable onComplete) {
        this(entity, null, from, to, duration, onComplete);
    }

    /**
     * Constructor completo con recurso
     */
    public PathAnimator(EntitySprite entity, ResourceSprite resource,
            LocationNode from, LocationNode to,
            double duration, Runnable onComplete) {
        this.entity = entity;
        this.resource = resource;
        this.from = from;
        this.to = to;
        this.duration = duration;
        this.onComplete = onComplete;
        this.withResource = (resource != null);

        this.startX = from.getCenterX();
        this.startY = from.getCenterY();
        this.endX = to.getCenterX();
        this.endY = to.getCenterY();

        // Calcular punto de control para curva Bezier (punto medio elevado/desplazado)
        double midX = (startX + endX) / 2;
        double midY = (startY + endY) / 2;

        // Calcular perpendicular para hacer la curva
        double dx = endX - startX;
        double dy = endY - startY;
        double distance = Math.sqrt(dx * dx + dy * dy);

        // Elevar el punto de control proporcionalmente a la distancia
        double elevation = Math.min(distance * 0.2, 50); // Máximo 50 píxeles
        double perpX = -dy / distance; // Perpendicular normalizado
        double perpY = dx / distance;

        this.controlX = midX + perpX * elevation;
        this.controlY = midY + perpY * elevation;

        // Usar Bezier si la distancia es significativa
        this.useBezierCurve = distance > 100;

        // Elegir easing según si hay recurso
        this.easingType = withResource ? "easeInOutCubic" : "easeInOutQuad";

        if (withResource) {
            resource.setMoving(true);
        }
    }

    public void update(double deltaTime) {
        if (finished)
            return;

        elapsed += deltaTime;
        double rawProgress = Math.min(elapsed / duration, 1.0);

        // Aplicar función de easing para movimiento suave
        double easedProgress = applyEasing(rawProgress);

        // Calcular posición con Bezier o lineal
        double currentX, currentY;

        if (useBezierCurve) {
            // Curva Bezier cuadrática
            double t = easedProgress;
            double mt = 1 - t;

            currentX = mt * mt * startX + 2 * mt * t * controlX + t * t * endX;
            currentY = mt * mt * startY + 2 * mt * t * controlY + t * t * endY;
        } else {
            // Interpolación lineal suavizada
            currentX = startX + (endX - startX) * easedProgress;
            currentY = startY + (endY - startY) * easedProgress;
        }

        // Actualizar posición de la entidad
        entity.setPosition(currentX, currentY);

        // Actualizar entidad sprite para animación interna
        entity.update(deltaTime);

        // Si hay recurso, moverlo junto con la entidad
        if (withResource && resource != null) {
            // Posicionar el recurso ligeramente adelante en la trayectoria
            double resourceOffset = 20;
            double angle = Math.atan2(endY - startY, endX - startX);
            double resourceX = currentX - Math.cos(angle) * resourceOffset;
            double resourceY = currentY - Math.sin(angle) * resourceOffset;

            resource.setPosition(resourceX, resourceY);
            resource.update(deltaTime);
        }

        // Emitir trail particles durante el movimiento
        if (easedProgress > 0.1 && easedProgress < 0.9) {
            // Solo trail en la parte media del movimiento
            if (ThreadLocalRandom.current().nextDouble() < 0.7) {
                entity.getTrailSystem().emitTrail(
                        currentX, currentY,
                        0, 0,
                        ColorPalette.ENTITY_PRIMARY);
            }
        }

        // Verificar si terminó
        if (rawProgress >= 1.0) {
            finished = true;

            // Asegurar posición final exacta
            entity.setPosition(endX, endY);

            // Devolver recurso a home
            if (withResource && resource != null) {
                resource.returnHome();
            }

            // Ejecutar callback
            if (onComplete != null) {
                onComplete.run();
            }
        }
    }

    /**
     * Aplica la función de easing seleccionada
     */
    private double applyEasing(double t) {
        switch (easingType) {
            case "easeInOutQuad":
                return AnimationEasing.easeInOutQuad(t);
            case "easeInOutCubic":
                return AnimationEasing.easeInOutCubic(t);
            case "easeInOutQuart":
                return AnimationEasing.easeInOutQuart(t);
            case "easeInOutSine":
                return AnimationEasing.easeInOutSine(t);
            case "easeOutBack":
                return AnimationEasing.easeOutBack(t);
            default:
                return AnimationEasing.smootherStep(t);
        }
    }

    /**
     * Calcula la velocidad actual basada en el progreso
     */
    public double getCurrentSpeed() {
        if (finished || elapsed == 0)
            return 0;

        double rawProgress = Math.min(elapsed / duration, 1.0);

        // Velocidad máxima en el medio, mínima al inicio/final
        return Math.sin(rawProgress * Math.PI); // 0 -> 1 -> 0
    }

    public boolean isFinished() {
        return finished;
    }

    public EntitySprite getEntity() {
        return entity;
    }

    public double getProgress() {
        return Math.min(elapsed / duration, 1.0);
    }

    public LocationNode getFrom() {
        return from;
    }

    public LocationNode getTo() {
        return to;
    }
}
