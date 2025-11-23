package com.simulation.locations; // Declaración del paquete donde se encuentra esta clase

public class LocationStatistics { // Clase que recopila y calcula estadísticas para una ubicación específica
    private final String locationName; // Nombre de la ubicación para la cual se calculan estadísticas
    private double scheduledTime; // Tiempo total programado de la simulación
    private int capacity; // Capacidad máxima de la ubicación (número de entidades que puede contener)
    private int totalEntries; // Número total de entradas de entidades a esta ubicación
    private double averageTimePerEntry; // Tiempo promedio que cada entidad pasa en la ubicación
    private double averageContents; // Contenido promedio de entidades en la ubicación durante la simulación
    private double maxContents; // Contenido máximo posible (igual a la capacidad)
    private double currentContents; // Número actual de entidades en la ubicación
    private double utilizationPercent; // Porcentaje de utilización basado en el contenido promedio vs capacidad
    private double busyUtilizationPercent; // Tiempo ocupado / tiempo total * 100

    public LocationStatistics(String locationName) { // Constructor que inicializa las estadísticas para una ubicación
        this.locationName = locationName; // Asigna el nombre de la ubicación
    }

    public void calculate(Location location, double totalSimulationTime, int entries, double totalProcessingTime) { // Método que calcula todas las estadísticas de la ubicación
        this.scheduledTime = totalSimulationTime; // Asigna el tiempo total de la simulación
        this.capacity = location.getType().capacity(); // Obtiene la capacidad de la ubicación desde su tipo
        this.totalEntries = entries; // Asigna el número total de entradas registradas

        if (entries > 0) { // Verifica que haya habido al menos una entrada
            this.averageTimePerEntry = totalProcessingTime / entries; // Calcula el tiempo promedio dividiendo el tiempo total entre el número de entradas
        }

        this.averageContents = location.getTotalOccupancyTime() / totalSimulationTime; // Calcula el contenido promedio dividiendo el tiempo de ocupación acumulado entre el tiempo total
        this.maxContents = capacity; // El contenido máximo es igual a la capacidad
        this.currentContents = location.getCurrentOccupancy(); // Obtiene el número actual de entidades en la ubicación
        if (capacity > 0) { // Verifica que la capacidad sea mayor que cero para evitar división por cero
            this.utilizationPercent = (averageContents / capacity) * 100.0; // Calcula el porcentaje de utilización basado en contenido promedio (estilo ProModel)
        } else { // Si la capacidad es cero
            this.utilizationPercent = 0.0; // Establece la utilización en cero
        }

        if (totalSimulationTime > 0) { // Verifica que el tiempo de simulación sea mayor que cero
            busyUtilizationPercent = (location.getBusyTime() / totalSimulationTime) * 100.0; // Calcula el porcentaje de utilización basado en tiempo ocupado (alternativa)
        } else { // Si el tiempo de simulación es cero
            busyUtilizationPercent = 0.0; // Establece la utilización ocupada en cero
        }

        if (utilizationPercent < 0.01 && busyUtilizationPercent > 0.1) { // Si la utilización por contenido es casi cero pero hubo tiempo ocupado significativo (fallback)
            utilizationPercent = busyUtilizationPercent; // Usa la utilización ocupada como respaldo para obtener un valor más representativo
        }
    }

    public String getLocationName() { return locationName; } // Retorna el nombre de la ubicación
    public double getScheduledTime() { return scheduledTime; } // Retorna el tiempo programado total de la simulación
    public int getCapacity() { return capacity; } // Retorna la capacidad de la ubicación
    public int getTotalEntries() { return totalEntries; } // Retorna el número total de entradas
    public double getAverageTimePerEntry() { return averageTimePerEntry; } // Retorna el tiempo promedio por entrada
    public double getAverageContents() { return averageContents; } // Retorna el contenido promedio de entidades
    public double getMaxContents() { return maxContents; } // Retorna el contenido máximo (capacidad)
    public double getCurrentContents() { return currentContents; } // Retorna el contenido actual de entidades
    public double getUtilizationPercent() { return utilizationPercent; } // Retorna el porcentaje de utilización principal
    public double getBusyUtilizationPercent() { return busyUtilizationPercent; } // Retorna el porcentaje de utilización basado en tiempo ocupado
}
