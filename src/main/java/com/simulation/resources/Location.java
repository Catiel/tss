package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.*;

/**
 * Clase base para todas las locaciones
 * CORREGIDO: Cálculo correcto de utilización considerando capacidad
 */
public abstract class Location {

    protected String name;
    protected int capacity;
    protected int currentContent;
    protected int pendingArrivals;
    protected Queue<Entity> queue;
    protected int totalEntries;
    protected int totalExits;

    // Integrales de tiempo para métricas
    protected double contentTimeIntegral;
    protected double busyTimeIntegral;
    protected double lastContentUpdateTime;

    public Location(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.currentContent = 0;
        this.pendingArrivals = 0;
        this.queue = new LinkedList<>();
        this.totalEntries = 0;
        this.totalExits = 0;
        this.contentTimeIntegral = 0;
        this.busyTimeIntegral = 0;
        this.lastContentUpdateTime = 0;
    }

    /**
     * Verifica si la locación puede recibir más entidades
     */
    public boolean canEnter() {
        if (capacity == Integer.MAX_VALUE) {
            return true;
        }
        return currentContent + pendingArrivals < capacity;
    }

    /**
     * Una entidad entra a la locación
     */
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            updateTimeIntegrals(currentTime);
            currentContent++;
            if (capacity != Integer.MAX_VALUE && pendingArrivals > 0) {
                pendingArrivals--;
            }
            totalEntries++;
        }
    }

    /**
     * Una entidad sale de la locación
     */
    public void exit(Entity entity, double currentTime) {
        if (currentContent > 0) {
            updateTimeIntegrals(currentTime);
            currentContent--;
            totalExits++;
        }
    }

    private void updateTimeIntegrals(double currentTime) {
        if (currentTime > lastContentUpdateTime) {
            double delta = currentTime - lastContentUpdateTime;
            contentTimeIntegral += currentContent * delta;
            if (currentContent > 0) {
                busyTimeIntegral += delta;
            }
            lastContentUpdateTime = currentTime;
        }
    }

    /**
     * Agrega una entidad a la cola de espera
     */
    public void addToQueue(Entity entity) {
        queue.add(entity);
    }

    /**
     * Remueve y retorna la siguiente entidad de la cola
     */
    public Entity pollFromQueue() {
        return queue.poll();
    }

    /**
     * Verifica si hay entidades en cola
     */
    public boolean hasQueuedEntities() {
        return !queue.isEmpty();
    }

    // Getters básicos
    public String getName() {
        return name;
    }

    public int getCapacity() {
        return capacity;
    }

    public int getCurrentContent() {
        return currentContent;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public int getTotalExits() {
        return totalExits;
    }

    /**
     * Calcula el porcentaje de utilización de la locación
     * CORREGIDO: Para capacidad > 1, usa contentTimeIntegral / (capacity * currentTime)
     */
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }

        updateTimeIntegrals(currentTime);

        // Para capacidad 1: usar busyTimeIntegral
        if (capacity == 1) {
            return (busyTimeIntegral / currentTime) * 100.0;
        }

        // Para capacidad > 1: usar contentTimeIntegral / (capacity * time)
        return (contentTimeIntegral / (capacity * currentTime)) * 100.0;
    }

    /**
     * Calcula el tiempo promedio por entrada
     */
    public double getAverageTimePerEntry(double currentTime) {
        if (totalEntries == 0) return 0;
        return currentTime / totalEntries;
    }

    /**
     * Calcula el contenido promedio ponderado por tiempo
     * FÓRMULA: Σ(contenido_i * tiempo_i) / tiempo_total
     */
    public double getAverageContent(double currentTime) {
        if (currentTime <= 0) return 0;
        updateTimeIntegrals(currentTime);
        return contentTimeIntegral / currentTime;
    }

    /**
     * Reserva capacidad para una llegada en tránsito
     */
    public void reserveCapacity() {
        if (capacity == Integer.MAX_VALUE) {
            return;
        }
        pendingArrivals++;
    }

    /**
     * Confirma una reserva previamente hecha
     */
    public void commitReservedCapacity() {
        if (capacity == Integer.MAX_VALUE) {
            return;
        }
        if (pendingArrivals > 0) {
            pendingArrivals--;
        }
    }

    public int getPendingArrivals() {
        return pendingArrivals;
    }

    /**
     * Reinicia el estado interno de la locación
     */
    public void resetState() {
        currentContent = 0;
        pendingArrivals = 0;
        queue.clear();
        totalEntries = 0;
        totalExits = 0;
        contentTimeIntegral = 0;
        busyTimeIntegral = 0;
        lastContentUpdateTime = 0;
    }
}
