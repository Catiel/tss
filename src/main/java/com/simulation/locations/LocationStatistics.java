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

    public void calculate(Location location, double totalSimulationTime, int entries, double totalProcessingTime) {
        this.scheduledTime = totalSimulationTime;
        this.capacity = location.getType().capacity();
        this.totalEntries = entries;

        if (entries > 0) {
            this.averageTimePerEntry = location.getTotalOccupancyTime() / entries;
        } else {
            this.averageTimePerEntry = 0.0;
        }

        this.averageContents = location.getTotalOccupancyTime() / totalSimulationTime;
        this.maxContents = capacity;
        this.currentContents = location.getCurrentOccupancy();

        if (capacity > 0) {
            this.utilizationPercent = (averageContents / capacity) * 100.0;
        } else {
            this.utilizationPercent = 0.0;
        }

        if (totalSimulationTime > 0) {
            busyUtilizationPercent = (location.getBusyTime() / totalSimulationTime) * 100.0;
        } else {
            busyUtilizationPercent = 0.0;
        }
    }

    public String getLocationName() {
        return locationName;
    } // Retorna el nombre de la ubicación

    public double getScheduledTime() {
        return scheduledTime;
    } // Retorna el tiempo programado total de la simulación

    public int getCapacity() {
        return capacity;
    } // Retorna la capacidad de la ubicación

    public int getTotalEntries() {
        return totalEntries;
    } // Retorna el número total de entradas

    public double getAverageTimePerEntry() {
        return averageTimePerEntry;
    } // Retorna el tiempo promedio por entrada

    public double getAverageContents() {
        return averageContents;
    } // Retorna el contenido promedio de entidades

    public double getMaxContents() {
        return maxContents;
    } // Retorna el contenido máximo (capacidad)

    public double getCurrentContents() {
        return currentContents;
    } // Retorna el contenido actual de entidades

    public double getUtilizationPercent() {
        return utilizationPercent;
    } // Retorna el porcentaje de utilización principal

    public double getBusyUtilizationPercent() {
        return busyUtilizationPercent;
    } // Retorna el porcentaje de utilización basado en tiempo ocupado
}
