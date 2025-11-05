package com.simulation.core;

public class Entity {

    private static int nextId = 1;
    private final int id;
    private double creationTime;
    private double systemEntryTime;
    private double currentLocationEntryTime;
    private double totalWaitTime;
    private double totalProcessTime;
    private double totalTransportTime;
    private double totalBlockTime;
    private String currentLocation;
    private boolean inTransit;
    private double transitStartTime;
    private double transitDuration;
    private String destinationLocation;
    private boolean blocked;
    private double blockStartTime;
    private String routingDestination; // NUEVO: Para almacenar decisiones de routing

    public Entity(double creationTime) {
        this.id = nextId++;
        this.creationTime = creationTime;
        this.systemEntryTime = creationTime;
        this.currentLocationEntryTime = creationTime;
        this.totalWaitTime = 0;
        this.totalProcessTime = 0;
        this.totalTransportTime = 0;
        this.totalBlockTime = 0;
        this.currentLocation = "";
        this.inTransit = false;
        this.transitStartTime = 0;
        this.transitDuration = 0;
        this.destinationLocation = "";
        this.blocked = false;
        this.blockStartTime = 0;
        this.routingDestination = null; // NUEVO
    }

    public static void resetIdCounter() {
        nextId = 1;
    }

    // === MÉTODOS DE TIEMPO ===

    public void addProcessTime(double time) {
        this.totalProcessTime += time;
    }

    public void addTransportTime(double time) {
        this.totalTransportTime += time;
    }

    public void addWaitTime(double time) {
        this.totalWaitTime += time;
    }

    public void addBlockTime(double time) {
        this.totalBlockTime += time;
    }

    public void setBlocked(boolean blocked, double currentTime) {
        if (blocked && !this.blocked) {
            // Inicia bloqueo
            this.blocked = true;
            this.blockStartTime = currentTime;
        } else if (!blocked && this.blocked) {
            // Termina bloqueo
            this.blocked = false;
            if (blockStartTime > 0) {
                this.totalBlockTime += (currentTime - blockStartTime);
            }
            this.blockStartTime = 0;
        }
    }

    // === TRÁNSITO ===

    public void startTransit(double startTime, double duration, String destination) {
        this.inTransit = true;
        this.transitStartTime = startTime;
        this.transitDuration = duration;
        this.destinationLocation = destination;
    }

    public void endTransit() {
        this.inTransit = false;
        this.transitStartTime = 0;
        this.transitDuration = 0;
        this.destinationLocation = "";
    }

    public double getTransitProgress(double currentTime) {
        if (!inTransit || transitDuration <= 0) {
            return 1.0;
        }
        double elapsed = currentTime - transitStartTime;
        return Math.min(1.0, elapsed / transitDuration);
    }

    // === GETTERS Y SETTERS ===

    public int getId() {
        return id;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public double getSystemEntryTime() {
        return systemEntryTime;
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public double getTotalProcessTime() {
        return totalProcessTime;
    }

    public double getTotalTransportTime() {
        return totalTransportTime;
    }

    public double getTotalBlockTime() {
        return totalBlockTime;
    }

    /**
     * Calcula el tiempo total que la entidad ha estado en el sistema
     * IMPORTANTE: Nombre debe ser getTotalSystemTime para compatibilidad con Statistics.java
     */
    public double getTotalSystemTime(double currentTime) {
        return currentTime - systemEntryTime;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String location) {
        this.currentLocation = location;
    }

    public boolean isInTransit() {
        return inTransit;
    }

    public double getTransitStartTime() {
        return transitStartTime;
    }

    public double getTransitDuration() {
        return transitDuration;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public boolean isBlocked() {
        return blocked;
    }

    // NUEVO: Métodos para routing probabilístico
    public String getRoutingDestination() {
        return routingDestination;
    }

    public void setRoutingDestination(String destination) {
        this.routingDestination = destination;
    }

    @Override
    public String toString() {
        return String.format("Entity-%d [Location: %s, Transit: %s]",
                id, currentLocation, inTransit ? destinationLocation : "None");
    }
}
