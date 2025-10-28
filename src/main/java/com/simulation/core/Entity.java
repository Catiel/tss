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
    private String previousLocation;
    private String destinationLocation;
    private boolean isInTransit;
    private double transitStartTime;
    private double transitDuration;

    private boolean isBlocked;
    private double blockStartTime;

    public Entity(double creationTime) {
        this.id = nextId++;
        this.creationTime = creationTime;
        this.systemEntryTime = creationTime;
        this.currentLocationEntryTime = creationTime;
        this.totalWaitTime = 0;
        this.totalProcessTime = 0;
        this.totalTransportTime = 0;
        this.totalBlockTime = 0;
        this.isBlocked = false;
        this.isInTransit = false;
        this.transitStartTime = 0;
        this.transitDuration = 0;
    }

    public static void resetIdCounter() {
        nextId = 1;
    }

    public int getId() {
        return id;
    }

    public double getCreationTime() {
        return creationTime;
    }

    public double getSystemEntryTime() {
        return systemEntryTime;
    }

    public void setSystemEntryTime(double time) {
        this.systemEntryTime = time;
    }

    public double getCurrentLocationEntryTime() {
        return currentLocationEntryTime;
    }

    public void setCurrentLocationEntryTime(double time) {
        this.currentLocationEntryTime = time;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String location) {
        this.previousLocation = this.currentLocation;
        this.currentLocation = location;
    }

    public String getPreviousLocation() {
        return previousLocation;
    }

    public String getDestinationLocation() {
        return destinationLocation;
    }

    public void setDestinationLocation(String location) {
        this.destinationLocation = location;
    }

    public boolean isInTransit() {
        return isInTransit;
    }

    public void startTransit(double currentTime, double duration, String destination) {
        this.isInTransit = true;
        this.transitStartTime = currentTime;
        this.transitDuration = duration;
        this.destinationLocation = destination;
    }

    public void endTransit() {
        this.isInTransit = false;
        this.transitStartTime = 0;
        this.transitDuration = 0;
        this.destinationLocation = null;
    }

    public double getTransitProgress(double currentTime) {
        if (!isInTransit || transitDuration <= 0) {
            return 0;
        }
        double elapsed = currentTime - transitStartTime;
        return Math.min(1.0, elapsed / transitDuration);
    }

    public void addWaitTime(double time) {
        this.totalWaitTime += time;
    }

    public void addProcessTime(double time) {
        this.totalProcessTime += time;
    }

    public void addTransportTime(double time) {
        this.totalTransportTime += time;
    }

    public void addBlockTime(double time) {
        this.totalBlockTime += time;
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

    public double getTotalSystemTime(double currentTime) {
        return currentTime - systemEntryTime;
    }

    public boolean isBlocked() {
        return isBlocked;
    }

    public void setBlocked(boolean blocked, double currentTime) {
        if (blocked && !isBlocked) {
            blockStartTime = currentTime;
        } else if (!blocked && isBlocked) {
            addBlockTime(currentTime - blockStartTime);
        }
        this.isBlocked = blocked;
    }
}
