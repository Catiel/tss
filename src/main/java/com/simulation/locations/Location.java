package com.simulation.locations;

import com.simulation.entities.Entity;

import java.util.LinkedList;
import java.util.Queue;

public class Location {
    private final LocationType type;
    private final Queue<Entity> queue;
    private final Queue<Entity> contentQueue;
    private int currentOccupancy;
    private double totalOccupancyTime;
    private double lastUpdateTime;
    // Tiempo acumulado con ocupación > 0 (utilización de la estación)
    private double busyTime;
    // Última ocupación registrada para cálculo de busyTime
    private int lastOccupancyForBusy;

    public Location(LocationType type) {
        this.type = type;
        this.queue = new LinkedList<>();
        this.contentQueue = new LinkedList<>();
        this.currentOccupancy = 0;
        this.totalOccupancyTime = 0;
        this.lastUpdateTime = 0;
        this.busyTime = 0;
        this.lastOccupancyForBusy = 0;
    }

    public boolean canAccept() {
        return currentOccupancy < type.capacity();
    }

    public void enter(Entity entity, double currentTime) {
        updateOccupancyTime(currentTime);
        if (canAccept()) {
            contentQueue.add(entity);
            currentOccupancy++;
            entity.setCurrentLocation(this);
        } else {
            queue.add(entity);
        }
    }

    public Entity exit(double currentTime) {
        updateOccupancyTime(currentTime);
        Entity entity = contentQueue.poll();
        if (entity != null) {
            currentOccupancy--;
            
            // Procesar siguiente en cola si hay espacio
            if (!queue.isEmpty() && canAccept()) {
                Entity nextEntity = queue.poll();
                contentQueue.add(nextEntity);
                currentOccupancy++;
                nextEntity.setCurrentLocation(this);
            }
        }
        return entity;
    }

    public void addToQueue(Entity entity) {
        queue.add(entity);
    }

    public Entity removeFromQueue() {
        return queue.poll();
    }

    private void updateOccupancyTime(double currentTime) {
        double timeDelta = currentTime - lastUpdateTime;
        // Acumular tiempo ponderado por ocupación para promedio
        totalOccupancyTime += currentOccupancy * timeDelta;
        // Si había ocupación > 0 en el intervalo, sumar a busyTime
        if (lastOccupancyForBusy > 0) {
            busyTime += timeDelta;
        }
        lastOccupancyForBusy = currentOccupancy;
        lastUpdateTime = currentTime;
    }

    public LocationType getType() {
        return type;
    }

    public int getCurrentOccupancy() {
        return currentOccupancy;
    }

    public int getQueueSize() {
        return queue.size();
    }

    public double getTotalOccupancyTime() {
        return totalOccupancyTime;
    }

    public double getBusyTime() {
        return busyTime;
    }

    public double getLastUpdateTime() {
        return lastUpdateTime;
    }
    
    public String getName() {
        return type.name();
    }
    
    public Queue<Entity> getQueue() {
        return queue;
    }
}
