package com.simulation.resources; // Declaración del paquete de recursos de la simulación

import com.simulation.entities.Entity; // Importa la clase Entity para manejo de entidades en cola

import java.util.LinkedList; // Importa LinkedList para implementación de cola
import java.util.Queue; // Importa la interfaz Queue para manejo de colas

public class Resource { // Define la clase que representa un recurso en la simulación
    private final ResourceType type; // Tipo de recurso inmutable que define características
    private final Queue<Entity> waitingQueue; // Cola de entidades esperando por este recurso
    private int availableUnits; // Número actual de unidades disponibles del recurso
    private double totalBusyTime; // Tiempo total acumulado que el recurso ha estado ocupado
    private double lastUpdateTime; // Último tiempo registrado para cálculo de estadísticas
    private String currentLocation; // Ubicación actual del recurso en el sistema
    private boolean isReturningHome = false; // Flag que indica si el recurso está regresando a su ubicación base

    public Resource(ResourceType type) { // Constructor que recibe el tipo de recurso
        this.type = type; // Asigna el tipo de recurso recibido
        this.availableUnits = type.units(); // Inicializa unidades disponibles con las unidades totales del tipo
        this.waitingQueue = new LinkedList<>(); // Inicializa la cola de espera como lista enlazada vacía
        this.totalBusyTime = 0; // Inicializa tiempo ocupado total en cero
        this.lastUpdateTime = 0; // Inicializa último tiempo de actualización en cero

        // Initialize default location
        if (type.name().equals("GRUA_VIAJERA")) { // Verifica si el recurso es la grúa viajera
            this.currentLocation = "ALMACEN_MP"; // Establece almacén de materia prima como ubicación inicial
        } else if (type.name().equals("ROBOT")) { // Verifica si el recurso es el robot
            this.currentLocation = "CARGA"; // Establece carga como ubicación inicial
        } else { // Para cualquier otro tipo de recurso
            this.currentLocation = "UNKNOWN"; // Establece ubicación desconocida como valor por defecto
        }
    }

    public boolean isAvailable() { // Método que verifica si el recurso tiene unidades disponibles
        return availableUnits > 0; // Retorna true si hay al menos una unidad disponible
    }

    public void acquire(double currentTime) { // Método para adquirir una unidad del recurso
        if (availableUnits > 0) { // Verifica si hay unidades disponibles para adquirir
            updateBusyTime(currentTime); // Actualiza el tiempo ocupado antes de adquirir
            availableUnits--; // Decrementa el número de unidades disponibles
        }
    }

    public void release(double currentTime) { // Método para liberar una unidad del recurso
        updateBusyTime(currentTime); // Actualiza el tiempo ocupado antes de liberar
        availableUnits++; // Incrementa el número de unidades disponibles
    }

    public void addToQueue(Entity entity) { // Método para añadir una entidad a la cola de espera
        waitingQueue.add(entity); // Añade la entidad al final de la cola
    }

    public Entity removeFromQueue() { // Método para remover y retornar la primera entidad de la cola
        return waitingQueue.poll(); // Extrae y retorna la primera entidad de la cola o null si está vacía
    }

    private void updateBusyTime(double currentTime) { // Método privado que actualiza el tiempo total de ocupación
        double timeDelta = currentTime - lastUpdateTime; // Calcula el tiempo transcurrido desde la última actualización
        int busyUnits = type.units() - availableUnits; // Calcula el número de unidades que están ocupadas
        totalBusyTime += busyUnits * timeDelta; // Acumula el tiempo ocupado multiplicando unidades ocupadas por tiempo transcurrido
        lastUpdateTime = currentTime; // Actualiza el último tiempo de registro al tiempo actual
    }

    public ResourceType getType() { // Método getter para obtener el tipo de recurso
        return type; // Retorna el tipo de recurso inmutable
    }

    public int getAvailableUnits() { // Método getter para obtener el número de unidades disponibles
        return availableUnits; // Retorna el número actual de unidades disponibles
    }

    public Queue<Entity> getQueue() { // Método getter para obtener la cola de entidades esperando
        return waitingQueue; // Retorna la referencia a la cola de espera
    }

    public void removeEntity(Entity entity) { // Método para remover una entidad específica de la cola
        waitingQueue.remove(entity); // Busca y remueve la entidad de la cola
    }

    public int getQueueSize() { // Método getter para obtener el tamaño de la cola de espera
        return waitingQueue.size(); // Retorna el número de entidades en la cola
    }

    public double getTotalBusyTime() { // Método getter para obtener el tiempo total de ocupación
        return totalBusyTime; // Retorna el tiempo acumulado que el recurso ha estado ocupado
    }

    public double getUtilization(double totalTime) { // Método que calcula el porcentaje de utilización del recurso
        if (totalTime == 0) // Verifica si el tiempo total es cero para evitar división por cero
            return 0.0; // Retorna utilización cero si no hay tiempo transcurrido
        return (totalBusyTime / (totalTime * type.units())) * 100.0; // Calcula y retorna utilización como porcentaje dividiendo tiempo ocupado entre tiempo total disponible
    }

    public String getName() { // Método getter para obtener el nombre del recurso
        return type.name(); // Retorna el nombre del tipo de recurso
    }

    public ResourceStatistics getStatistics() { // Método que genera y retorna las estadísticas del recurso
        ResourceStatistics stats = new ResourceStatistics(getName()); // Crea nuevo objeto de estadísticas con el nombre del recurso
        stats.calculate(this, lastUpdateTime, type.units() - availableUnits, totalBusyTime); // Calcula las estadísticas con datos actuales del recurso
        return stats; // Retorna el objeto de estadísticas calculadas
    }

    public String getCurrentLocation() { // Método getter para obtener la ubicación actual del recurso
        return currentLocation; // Retorna la ubicación actual donde se encuentra el recurso
    }

    public void setCurrentLocation(String currentLocation) { // Método setter para establecer la ubicación actual del recurso
        this.currentLocation = currentLocation; // Asigna la nueva ubicación recibida
    }

    public boolean isReturningHome() { // Método getter para verificar si el recurso está regresando a casa
        return isReturningHome; // Retorna el valor del flag de retorno a casa
    }

    public void setReturningHome(boolean returningHome) { // Método setter para establecer el estado de retorno a casa
        isReturningHome = returningHome; // Asigna el nuevo valor del flag de retorno a casa
    }

    private long returnHomeId = 0; // Identificador único para eventos de retorno a casa, inicializado en cero

    public long getReturnHomeId() { // Método getter para obtener el ID actual de retorno a casa
        return returnHomeId; // Retorna el identificador actual de retorno
    }

    public long incrementReturnHomeId() { // Método que incrementa y retorna el ID de retorno a casa
        return ++returnHomeId; // Pre-incrementa el ID y retorna el nuevo valor
    }
}
