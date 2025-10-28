package com.simulation.resources;

import com.simulation.core.Entity;

/**
 * Locación tipo Buffer (Almacén)
 * Representa almacenes intermedios como ALMACEN_PINTURA y ALMACEN_HORNO
 * No procesa, solo almacena
 */
public class BufferLocation extends Location {

    public BufferLocation(String name, int capacity) {
        super(name, capacity);
    }

    @Override
    public void enter(Entity entity, double currentTime) {
        if (currentContent < capacity) {
            currentContent++;
            totalEntries++;

            // Si el almacén pasa de vacío a tener contenido, marcar como ocupado
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

            // Si el almacén queda vacío, actualizar tiempo ocupado
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

        // Si actualmente tiene contenido, agregar tiempo desde último cambio
        if (isBusy && currentContent > 0) {
            actualBusyTime += (currentTime - lastStatusChangeTime);
        }

        // Utilización basada en promedio de piezas en el buffer
        // Para buffers, calculamos: tiempo que estuvo ocupado / tiempo total
        return (actualBusyTime / currentTime) * 100.0;
    }
}
