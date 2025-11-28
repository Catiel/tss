package com.simulation.resources; // Declaración del paquete

import com.simulation.entities.Entity;

import java.util.LinkedList;
import java.util.Queue;

public class Resource { // Clase que representa un recurso compartido en la simulación
    private final ResourceType type; // Tipo del recurso (definición de propiedades del recurso)
    private final Queue<Entity> waitingQueue; // Cola de entidades esperando para adquirir el recurso
    private int availableUnits; // Unidades disponibles actualmente del recurso
    private double totalBusyTime; // Tiempo total acumulado en el que el recurso estuvo ocupado
    private double lastUpdateTime; // Último tiempo en que se registró el estado del recurso

    public Resource(ResourceType type) { // Constructor del recurso
        this.type = type; // Asigna el tipo recibido
        this.availableUnits = type.units(); // Inicializa unidades disponibles según el tipo
        this.waitingQueue = new LinkedList<>(); // Inicializa la cola de espera
        this.totalBusyTime = 0; // Inicializa tiempo ocupado en cero
        this.lastUpdateTime = 0; // Inicializa el tiempo de última actualización en cero
    }

    public boolean isAvailable() { // Indica si hay unidades disponibles para ser adquiridas
        return availableUnits > 0; // Retorna verdadero si hay unidades libres
    }

    public void acquire(double currentTime) { // Intentar adquirir una unidad del recurso
        if (availableUnits > 0) { // Si hay unidades disponibles
            updateBusyTime(currentTime); // Actualiza estadísticas de ocupación
            availableUnits--; // Disminuye una unidad disponible
        }
    }

    public void release(double currentTime) { // Libera una unidad del recurso
        updateBusyTime(currentTime); // Actualiza estadísticas de ocupación
        availableUnits++; // Incrementa unidades disponibles
    }

    public void addToQueue(Entity entity) { // Agrega una entidad a la cola de espera del recurso
        waitingQueue.add(entity); // Añade al final de la cola
    }

    public Entity removeFromQueue() { // Remueve la siguiente entidad de la cola de espera
        return waitingQueue.poll(); // Quita y devuelve la siguiente entidad, o null si no hay
    }

    private void updateBusyTime(double currentTime) { // Actualiza el tiempo ocupado por el recurso
        double timeDelta = currentTime - lastUpdateTime; // Calcula tiempo transcurrido desde la última actualización
        int busyUnits = type.units() - availableUnits; // Calcula cuántas unidades estuvieron ocupadas en el intervalo
        totalBusyTime += busyUnits * timeDelta; // Suma ese tiempo al acumulador
        lastUpdateTime = currentTime; // Actualiza el tiempo de referencia
    }

    public ResourceType getType() { // Devuelve el tipo de recurso
        return type; // Retorna el objeto tipo
    }

    public int getAvailableUnits() { // Devuelve cuántas unidades hay actualmente disponibles
        return availableUnits; // Retorna unidades disponibles
    }

    public int getQueueSize() { // Devuelve cuántas entidades esperan el recurso
        return waitingQueue.size(); // Retorna tamaño de la cola de espera
    }

    public double getTotalBusyTime() { // Devuelve el tiempo total ocupado
        return totalBusyTime; // Retorna acumulador de tiempo ocupado
    }

    public double getUtilization(double totalTime) { // Calcula el porcentaje de utilización del recurso
        return (totalBusyTime / (totalTime * type.units())) * 100.0; // Utilización en porcentaje
    }

    public String getName() { // Devuelve el nombre del recurso
        return type.name(); // Retorna el nombre desde el tipo de recurso
    }

    public ResourceStatistics getStatistics() { // Devuelve estadísticas básicas del recurso
        ResourceStatistics stats = new ResourceStatistics(getName()); // Crea un objeto de estadísticas
        stats.calculate(this, lastUpdateTime, type.units() - availableUnits, totalBusyTime); // Calcula estadísticas actuales
        return stats; // Retorna el objeto de estadísticas
    }
}
