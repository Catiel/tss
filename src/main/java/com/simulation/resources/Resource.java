package com.simulation.resources;

import com.simulation.entities.Entity;

import java.util.LinkedList;
import java.util.Queue;

public class Resource {
    private final ResourceType type;
    private final Queue<Entity> waitingQueue;
    private int availableUnits;
    private double totalBusyTime;
    private double lastUpdateTime;
    private String currentLocation;
    private boolean isReturningHome = false;

    public Resource(ResourceType type) {
        this.type = type;
        this.availableUnits = type.units();
        this.waitingQueue = new LinkedList<>();
        this.totalBusyTime = 0;
        this.lastUpdateTime = 0;

        // Initialize default location
        if (type.name().equals("GRUA_VIAJERA")) {
            this.currentLocation = "ALMACEN_MP";
        } else if (type.name().equals("ROBOT")) {
            this.currentLocation = "CARGA";
        } else {
            this.currentLocation = "UNKNOWN";
        }
    }

    public boolean isAvailable() {
        return availableUnits > 0;
    }

    public void acquire(double currentTime) {
        if (availableUnits > 0) {
            updateBusyTime(currentTime);
            availableUnits--;
        }
    }

    public void release(double currentTime) {
        updateBusyTime(currentTime);
        availableUnits++;
    }

    public void addToQueue(Entity entity) {
        waitingQueue.add(entity);
    }

    public Entity removeFromQueue() {
        return waitingQueue.poll();
    }

    private void updateBusyTime(double currentTime) {
        double timeDelta = currentTime - lastUpdateTime;
        int busyUnits = type.units() - availableUnits;
        totalBusyTime += busyUnits * timeDelta;
        lastUpdateTime = currentTime;
    }

    public ResourceType getType() {
        return type;
    }

    public int getAvailableUnits() {
        return availableUnits;
    }

    public Queue<Entity> getQueue() {
        return waitingQueue;
    }

    public void removeEntity(Entity entity) {
        waitingQueue.remove(entity);
    }

    public int getQueueSize() {
        return waitingQueue.size();
    }

    public double getTotalBusyTime() {
        return totalBusyTime;
    }

    public double getUtilization(double totalTime) {
        if (totalTime == 0)
            return 0.0;
        return (totalBusyTime / (totalTime * type.units())) * 100.0;
    }

    public String getName() {
        return type.name();
    }

    public ResourceStatistics getStatistics() {
        ResourceStatistics stats = new ResourceStatistics(getName());
        stats.calculate(this, lastUpdateTime, type.units() - availableUnits, totalBusyTime);
        return stats;
    }

    public String getCurrentLocation() {
        return currentLocation;
    }

    public void setCurrentLocation(String currentLocation) {
        this.currentLocation = currentLocation;
    }

    public boolean isReturningHome() {
        return isReturningHome;
    }

    public void setReturningHome(boolean returningHome) {
        isReturningHome = returningHome;
    }

    private long returnHomeId = 0;

    public long getReturnHomeId() {
        return returnHomeId;
    }

    public long incrementReturnHomeId() {
        return ++returnHomeId;
    }
}
