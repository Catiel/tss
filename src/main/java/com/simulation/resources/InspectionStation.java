package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Estación de inspección con múltiples mesas
 * CORREGIDO: Manejo adecuado de 2 mesas con 3 operaciones cada una
 */
public class InspectionStation extends Location {
    private final int operationsPerPiece;
    private final Map<Entity, Integer> operationCounts;
    private final Set<Entity> reservedEntities;

    public InspectionStation(String name, int numStations, int operationsPerPiece) {
        super(name, numStations);  // capacity = 2 (dos mesas)
        this.operationsPerPiece = operationsPerPiece;
        this.operationCounts = new HashMap<>();
        this.reservedEntities = new HashSet<>();
    }

    @Override
    public void enter(Entity entity, double currentTime) {
        commitReservationFor(entity);
        super.enter(entity, currentTime);
        operationCounts.put(entity, 0);
    }

    @Override
    public void exit(Entity entity, double currentTime) {
        operationCounts.remove(entity);
        reservedEntities.remove(entity);
        super.exit(entity, currentTime);
    }

    /**
     * Incrementa el contador de operaciones completadas para una entidad
     */
    public void incrementOperationCount(Entity entity) {
        if (operationCounts.containsKey(entity)) {
            operationCounts.put(entity, operationCounts.get(entity) + 1);
        }
    }

    /**
     * Verifica si una entidad completó todas sus operaciones
     */
    public boolean hasCompletedAllOperations(Entity entity) {
        return operationCounts.getOrDefault(entity, 0) >= operationsPerPiece;
    }

    /**
     * Obtiene el número de operaciones completadas por una entidad
     */
    public int getOperationCount(Entity entity) {
        return operationCounts.getOrDefault(entity, 0);
    }

    @Override
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }
        double averageBusyStations = getAverageContent(currentTime);
        return (averageBusyStations / capacity) * 100.0;
    }

    public boolean hasAvailableStation() {
        return canEnter();
    }

    public void reserveStation(Entity entity) {
        if (!hasAvailableStation()) {
            throw new IllegalStateException("No hay mesas de inspección disponibles");
        }
        if (reservedEntities.contains(entity)) {
            return;
        }
        reserveCapacity();
        reservedEntities.add(entity);
    }

    public void commitReservationFor(Entity entity) {
        if (reservedEntities.remove(entity)) {
            commitReservedCapacity();
        }
    }

    @Override
    public void resetState() {
        super.resetState();
        operationCounts.clear();
        reservedEntities.clear();
    }
}