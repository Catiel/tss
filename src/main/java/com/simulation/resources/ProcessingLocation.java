package com.simulation.resources;

import com.simulation.core.Entity;

/**
 * Locación de procesamiento (LAVADORA, PINTURA, HORNO)
 * Realiza operaciones sobre las entidades
 */
public class ProcessingLocation extends Location {

    public ProcessingLocation(String name, int capacity) {
        super(name, capacity);
    }

    @Override
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            currentContent++;
            totalEntries++;

            // Marca como ocupado cuando entra la primera pieza
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

            // Si queda vacío, registrar tiempo ocupado
            if (currentContent == 0 && isBusy) {
                totalBusyTime += (currentTime - lastStatusChangeTime);
                isBusy = false;
                lastStatusChangeTime = currentTime;
            }
        }
    }

    @Override
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }

        double actualBusyTime = totalBusyTime;

        // Si actualmente está procesando, agregar tiempo actual
        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        // Para estaciones de procesamiento:
        // Utilización = (tiempo ocupado / tiempo total) * 100
        return (actualBusyTime / currentTime) * 100.0;
    }
}
