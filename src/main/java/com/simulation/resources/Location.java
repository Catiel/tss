package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.*;

/**
 * Clase base para todas las locaciones
 * CORREGIDO: Cálculo preciso de utilización y tiempos
 */
public abstract class Location {
    protected String name;
    protected int capacity;
    protected int currentContent;
    protected Queue<Entity> queue;
    protected int totalEntries;
    protected int totalExits;

    // Para cálculo de utilización basado en tiempo
    protected double totalBusyTime;
    protected double lastStatusChangeTime;
    protected boolean isBusy;

    // Para cálculo de contenido promedio
    protected double weightedContentSum;
    protected int lastContent;
    protected double lastContentChangeTime;

    public Location(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.currentContent = 0;
        this.queue = new LinkedList<>();
        this.totalEntries = 0;
        this.totalExits = 0;
        this.totalBusyTime = 0;
        this.lastStatusChangeTime = 0;
        this.isBusy = false;
        this.weightedContentSum = 0;
        this.lastContent = 0;
        this.lastContentChangeTime = 0;
    }

    /**
     * Verifica si la locación puede recibir más entidades
     */
    public boolean canEnter() {
        return currentContent < capacity;
    }

    /**
     * Una entidad entra a la locación
     */
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            // Actualizar contenido promedio
            updateWeightedContent(currentTime);

            currentContent++;
            totalEntries++;

            // Actualizar estado de ocupación
            if (currentContent == 1 && !isBusy) {
                isBusy = true;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    /**
     * Una entidad sale de la locación
     */
    public void exit(Entity entity, double currentTime) {
        if (currentContent > 0) {
            // Actualizar contenido promedio
            updateWeightedContent(currentTime);

            currentContent--;
            totalExits++;

            // Actualizar estado de ocupación
            if (currentContent == 0 && isBusy) {
                totalBusyTime += (currentTime - lastStatusChangeTime);
                isBusy = false;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    /**
     * Actualiza el cálculo de contenido promedio ponderado por tiempo
     */
    private void updateWeightedContent(double currentTime) {
        if (currentTime > lastContentChangeTime) {
            double timeDelta = currentTime - lastContentChangeTime;
            weightedContentSum += lastContent * timeDelta;
            lastContent = currentContent;
            lastContentChangeTime = currentTime;
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
     * FÓRMULA: (Tiempo ocupado / Tiempo total) * 100
     */
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }

        double actualBusyTime = totalBusyTime;

        // Si actualmente está ocupado, agregar tiempo desde último cambio
        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        return (actualBusyTime / currentTime) * 100.0;
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

        // Actualizar con el contenido actual
        double finalWeightedSum = weightedContentSum;
        if (currentTime > lastContentChangeTime) {
            finalWeightedSum += currentContent * (currentTime - lastContentChangeTime);
        }

        return finalWeightedSum / currentTime;
    }
}