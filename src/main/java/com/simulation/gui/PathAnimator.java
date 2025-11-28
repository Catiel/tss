package com.simulation.gui;

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
    private double elapsed = 0;
    private boolean finished = false;

    public PathAnimator(EntitySprite entity, LocationNode from, LocationNode to,
                        double duration, Runnable onComplete) {
        this(entity, null, from, to, duration, onComplete);
    }

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

        if (withResource) {
            resource.setMoving(true);
        }
    }

    public void update(double deltaTime) {
        if (finished) return;

        elapsed += deltaTime;
        double progress = Math.min(elapsed / duration, 1.0);

        // Interpolación lineal
        double currentX = startX + (endX - startX) * progress;
        double currentY = startY + (endY - startY) * progress;

        // Actualizar posición de la entidad
        entity.setPosition(currentX, currentY);

        // Si hay recurso, moverlo junto con la entidad
        if (withResource && resource != null) {
            resource.setPosition(currentX - 15, currentY); // Offset para que se vea al lado
        }

        // Verificar si terminó
        if (progress >= 1.0) {
            finished = true;

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

    public boolean isFinished() {
        return finished;
    }

    public EntitySprite getEntity() {
        return entity;
    }
}
