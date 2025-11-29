package com.simulation.routing;

/**
 * Representa una ruta entre dos locaciones con opción de usar recurso
 */
public class Route {
    private final String fromLocation;
    private final String toLocation;
    private final String resourceName; // null si no usa recurso
    private final double moveTime; // tiempo de movimiento en minutos

    public Route(String from, String to, String resource, double moveTime) {
        this.fromLocation = from;
        this.toLocation = to;
        this.resourceName = resource;
        this.moveTime = moveTime;
    }

    public String getFromLocation() {
        return fromLocation;
    }

    public String getToLocation() {
        return toLocation;
    }

    public String getResourceName() {
        return resourceName;
    }

    public double getMoveTime() {
        return moveTime;
    }

    public boolean requiresResource() {
        return resourceName != null && !resourceName.isEmpty();
    }
}
