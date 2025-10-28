package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.*;

/**
 * Clase base para todas las locaciones
 */
public abstract class Location {
    protected String name;
    protected int capacity;
    protected int currentContent;
    protected Queue<Entity> queue;
    protected int totalEntries;
    protected int totalExits;

    // Para cálculo de utilización
    protected double totalBusyTime;
    protected double lastStatusChangeTime;
    protected boolean isBusy;

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
    }

    public boolean canEnter() {
        return currentContent < capacity;
    }

    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            currentContent++;
            totalEntries++;

            if (currentContent == 1 && !isBusy) {
                isBusy = true;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    public void exit(Entity entity, double currentTime) {
        if (currentContent > 0) {
            currentContent--;
            totalExits++;

            if (currentContent == 0 && isBusy) {
                totalBusyTime += (currentTime - lastStatusChangeTime);
                isBusy = false;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    public void addToQueue(Entity entity) {
        queue.add(entity);
    }

    public Entity pollFromQueue() {
        return queue.poll();
    }

    public boolean hasQueuedEntities() {
        return !queue.isEmpty();
    }

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

    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE) {
            return 0.0;
        }

        if (currentTime <= 0) {
            return 0.0;
        }

        double actualBusyTime = totalBusyTime;

        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        return (actualBusyTime / currentTime) * 100.0;
    }

    public double getAverageTimePerEntry(double currentTime) {
        if (totalEntries == 0) return 0;
        return currentTime / totalEntries;
    }

    public double getAverageContent(double currentTime) {
        if (currentTime == 0) return 0;
        return (totalBusyTime * capacity) / currentTime;
    }
}
