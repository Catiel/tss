package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * Estación de inspección con múltiples operaciones por pieza
 */
public class InspectionStation extends Location {
    private int operationsPerPiece;
    private Map<Entity, Integer> operationCounts;

    public InspectionStation(String name, int numStations, int operationsPerPiece) {
        super(name, numStations);
        this.operationsPerPiece = operationsPerPiece;
        this.operationCounts = new HashMap<>();
    }

    @Override
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            currentContent++;
            totalEntries++;
            operationCounts.put(entity, 0);

            if (currentContent == 1 && !isBusy) {
                isBusy = true;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    @Override
    public void exit(Entity entity, double currentTime) {
        if (currentContent > 0) {
            currentContent--;
            totalExits++;
            operationCounts.remove(entity);

            if (currentContent == 0 && isBusy) {
                totalBusyTime += (currentTime - lastStatusChangeTime);
                isBusy = false;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    public void incrementOperationCount(Entity entity) {
        if (operationCounts.containsKey(entity)) {
            operationCounts.put(entity, operationCounts.get(entity) + 1);
        }
    }

    public boolean hasCompletedAllOperations(Entity entity) {
        return operationCounts.getOrDefault(entity, 0) >= operationsPerPiece;
    }

    @Override
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }

        double actualBusyTime = totalBusyTime;

        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        // Para inspección con 2 estaciones, calculamos utilización promedio
        return (actualBusyTime / currentTime) * 100.0;
    }
}
