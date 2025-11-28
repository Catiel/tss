package com.simulation.locations; // Declaración del paquete donde se encuentra esta clase

import com.simulation.entities.Entity;

import java.util.LinkedList;
import java.util.Queue;

public class Location { // Clase que representa una ubicación o estación en la simulación
    private final LocationType type; // Tipo de ubicación con sus características definidas
    private final Queue<Entity> queue; // Cola de espera para entidades que no pueden entrar por falta de capacidad
    private final Queue<Entity> contentQueue; // Cola de entidades que están actualmente siendo procesadas en la ubicación
    private int currentOccupancy; // Número actual de entidades ocupando la ubicación
    private double totalOccupancyTime; // Tiempo total acumulado ponderado por la ocupación (para promedio)
    private double lastUpdateTime; // Último tiempo en que se actualizaron las estadísticas de ocupación
    private double busyTime; // Tiempo acumulado con ocupación > 0 (utilización de la estación)
    private int lastOccupancyForBusy; // Última ocupación registrada para cálculo de busyTime

    public Location(LocationType type) { // Constructor que inicializa una ubicación
        this.type = type; // Asigna el tipo de ubicación
        this.queue = new LinkedList<>(); // Crea la cola de espera vacía
        this.contentQueue = new LinkedList<>(); // Crea la cola de contenido vacía
        this.currentOccupancy = 0; // Inicializa la ocupación actual en cero
        this.totalOccupancyTime = 0; // Inicializa el tiempo de ocupación acumulado en cero
        this.lastUpdateTime = 0; // Inicializa el tiempo de última actualización en cero
        this.busyTime = 0; // Inicializa el tiempo ocupado en cero
        this.lastOccupancyForBusy = 0; // Inicializa la última ocupación en cero
    }

    public boolean canAccept() { // Método que verifica si la ubicación puede aceptar más entidades
        return currentOccupancy < type.capacity(); // Retorna verdadero si la ocupación actual es menor que la capacidad
    }

    public void enter(Entity entity, double currentTime) { // Método para que una entidad entre a la ubicación
        updateOccupancyTime(currentTime); // Actualiza las estadísticas de ocupación antes del cambio
        if (canAccept()) { // Verifica si hay espacio disponible
            contentQueue.add(entity); // Agrega la entidad a la cola de procesamiento
            currentOccupancy++; // Incrementa el contador de ocupación
            entity.setCurrentLocation(this); // Establece esta ubicación como la actual para la entidad
        } else { // Si no hay espacio disponible
            queue.add(entity); // Agrega la entidad a la cola de espera
        }
    }

    public Entity exit(double currentTime) { // Método para que una entidad salga de la ubicación
        updateOccupancyTime(currentTime); // Actualiza las estadísticas de ocupación antes del cambio
        Entity entity = contentQueue.poll(); // Remueve y obtiene la primera entidad de la cola de procesamiento
        if (entity != null) { // Si se obtuvo una entidad (la cola no estaba vacía)
            currentOccupancy--; // Decrementa el contador de ocupación

            if (!queue.isEmpty() && canAccept()) { // Si hay entidades esperando y ahora hay espacio
                Entity nextEntity = queue.poll(); // Remueve la primera entidad de la cola de espera
                contentQueue.add(nextEntity); // La agrega a la cola de procesamiento
                currentOccupancy++; // Incrementa nuevamente la ocupación
                nextEntity.setCurrentLocation(this); // Establece esta ubicación como actual para la nueva entidad
            }
        }
        return entity; // Retorna la entidad que salió (o null si no había ninguna)
    }

    public void addToQueue(Entity entity) { // Método para agregar una entidad directamente a la cola de espera
        queue.add(entity); // Agrega la entidad al final de la cola de espera
    }

    public Entity removeFromQueue() { // Método para remover una entidad de la cola de espera
        return queue.poll(); // Remueve y retorna la primera entidad de la cola (o null si está vacía)
    }

    private void updateOccupancyTime(double currentTime) { // Método privado para actualizar estadísticas de ocupación
        double timeDelta = currentTime - lastUpdateTime; // Calcula el tiempo transcurrido desde la última actualización
        totalOccupancyTime += currentOccupancy * timeDelta; // Acumula tiempo ponderado por ocupación para cálculo de promedio
        if (lastOccupancyForBusy > 0) { // Si había al menos una entidad en el intervalo anterior
            busyTime += timeDelta; // Suma el intervalo al tiempo de utilización
        }
        lastOccupancyForBusy = currentOccupancy; // Guarda la ocupación actual para el próximo cálculo
        lastUpdateTime = currentTime; // Actualiza el marcador de tiempo de última actualización
    }

    public LocationType getType() { // Método getter para obtener el tipo de ubicación
        return type; // Retorna el objeto LocationType asociado
    }

    public int getCurrentOccupancy() { // Método getter para obtener la ocupación actual
        return currentOccupancy; // Retorna el número de entidades actualmente en la ubicación
    }

    public int getQueueSize() { // Método getter para obtener el tamaño de la cola de espera
        return queue.size(); // Retorna el número de entidades esperando
    }

    public double getTotalOccupancyTime() { // Método getter para obtener el tiempo de ocupación acumulado
        return totalOccupancyTime; // Retorna el tiempo total ponderado por ocupación
    }

    public double getBusyTime() { // Método getter para obtener el tiempo de utilización
        return busyTime; // Retorna el tiempo total en que la ubicación tuvo al menos una entidad
    }

    public double getLastUpdateTime() { // Método getter para obtener el tiempo de la última actualización
        return lastUpdateTime; // Retorna el marcador de tiempo de la última actualización de estadísticas
    }

    public String getName() { // Método getter para obtener el nombre de la ubicación
        return type.name(); // Retorna el nombre definido en el tipo de ubicación
    }

    public Queue<Entity> getQueue() { // Método getter para obtener la cola de espera
        return queue; // Retorna la referencia a la cola de entidades esperando
    }
}
