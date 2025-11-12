package com.simulation.core;

/**
 * Clase abstracta base para todos los eventos del sistema de simulación DIGEMIC
 */
public abstract class Event implements Comparable<Event> {
    protected double time;
    protected Entity entity;

    public Event(double time, Entity entity) {
        this.time = time;
        this.entity = entity;
    }

    public double getTime() {
        return time;
    }

    public Entity getEntity() {
        return entity;
    }

    // Método abstracto compatible con SimulationEngine y DigemicEngine
    public abstract void execute(Object engine);

    @Override
    public int compareTo(Event other) {
        int timeComparison = Double.compare(this.time, other.time);
        if (timeComparison != 0) {
            return timeComparison;
        }
        // Si los tiempos son iguales, mantener orden de llegada
        return Integer.compare(
            this.entity != null ? this.entity.getId() : 0,
            other.entity != null ? other.entity.getId() : 0
        );
    }
}
