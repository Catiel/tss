package com.simulation.entities;

import com.simulation.locations.Location;

public class Entity {
    private static int nextId = 1;

    private final int id;
    private final EntityType type;
    private Location currentLocation;
    private double entryTime;
    private double totalSystemTime;
    private double totalValueAddedTime;
    private double totalNonValueAddedTime;
    private double totalWaitTime;
    private boolean inSystem;
    private boolean isTransformed; // NUEVO: indica si es resultado de transformación

    private final double creationTime; // Time when entity was created

    public Entity(EntityType type) {
        this(type, false, 0.0);
    }

    // Constructor con parámetro para indicar si es transformada
    public Entity(EntityType type, boolean isTransformed) {
        this(type, isTransformed, 0.0);
    }

    public Entity(EntityType type, boolean isTransformed, double creationTime) {
        this.id = nextId++;
        this.type = type;
        this.totalSystemTime = 0;
        this.totalValueAddedTime = 0;
        this.totalNonValueAddedTime = 0;
        this.totalWaitTime = 0;
        this.inSystem = true;
        this.isTransformed = isTransformed;
        this.creationTime = creationTime;
    }

    public int getId() {
        return id;
    }

    public EntityType getType() {
        return type;
    }

    public Location getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(Location location) {
        this.currentLocation = location;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public double getEntryTime() {
        return entryTime;
    }

    public void setEntryTime(double time) {
        this.entryTime = time;
    }

    public void addSystemTime(double time) {
        this.totalSystemTime += time;
    }

    public void addValueAddedTime(double time) {
        this.totalValueAddedTime += time;
    }

    public void addNonValueAddedTime(double time) {
        this.totalNonValueAddedTime += time;
    }

    public void addWaitTime(double time) {
        this.totalWaitTime += time;
    }

    public double getTotalSystemTime() {
        return totalSystemTime;
    }

    public double getTotalValueAddedTime() {
        return totalValueAddedTime;
    }

    public double getTotalNonValueAddedTime() {
        return totalNonValueAddedTime;
    }

    public double getTotalWaitTime() {
        return totalWaitTime;
    }

    public boolean isInSystem() {
        return inSystem;
    }

    public void setInSystem(boolean inSystem) {
        this.inSystem = inSystem;
    }

    public boolean isTransformed() {
        return isTransformed;
    }

    public void setTransformed(boolean transformed) {
        isTransformed = transformed;
    }

    private String pendingDestination;

    public String getPendingDestination() {
        return pendingDestination;
    }

    public void setPendingDestination(String destination) {
        this.pendingDestination = destination;
    }
}
