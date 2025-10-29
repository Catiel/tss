package com.simulation.resources;

import com.simulation.core.Entity;
import java.util.HashMap;
import java.util.Map;

/**
 * Estación de inspección con múltiples mesas
 * CORREGIDO: Manejo adecuado de 2 mesas con 3 operaciones cada una
 */
public class InspectionStation extends Location {
    private int operationsPerPiece;
    private Map<Entity, Integer> operationCounts;

    public InspectionStation(String name, int numStations, int operationsPerPiece) {
        super(name, numStations);  // capacity = 2 (dos mesas)
        this.operationsPerPiece = operationsPerPiece;
        this.operationCounts = new HashMap<>();
    }

    @Override
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            // Actualizar contenido promedio
            updateWeightedContent(currentTime);

            currentContent++;
            totalEntries++;
            operationCounts.put(entity, 0);  // Iniciar contador de operaciones

            // Actualizar estado de ocupación
            if (currentContent == 1 && !isBusy) {
                isBusy = true;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    @Override
    public void exit(Entity entity, double currentTime) {
        if (currentContent > 0) {
            // Actualizar contenido promedio
            updateWeightedContent(currentTime);

            currentContent--;
            totalExits++;
            operationCounts.remove(entity);  // Limpiar contador

            // Actualizar estado de ocupación
            if (currentContent == 0 && isBusy) {
                totalBusyTime += (currentTime - lastStatusChangeTime);
                isBusy = false;
                lastStatusChangeTime = currentTime;
            }
        }
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

    /**
     * Método auxiliar para actualizar contenido promedio
     */
    private void updateWeightedContent(double currentTime) {
        if (currentTime > lastContentChangeTime) {
            double timeDelta = currentTime - lastContentChangeTime;
            weightedContentSum += lastContent * timeDelta;
            lastContent = currentContent;
            lastContentChangeTime = currentTime;
        }
    }

    @Override
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }

        double actualBusyTime = totalBusyTime;

        // Si actualmente está procesando, agregar tiempo desde último cambio
        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        // Para 2 mesas: utilización = tiempo ocupado / (tiempo total * 2)
        // Esto da el % promedio de utilización de ambas mesas
        return (actualBusyTime / (currentTime * capacity)) * 100.0;
    }

    @Override
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