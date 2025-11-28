package com.simulation.resources; // Declaración del paquete

public class ResourceStatistics { // Clase que encapsula estadísticas de uso de un recurso
    private final String resourceName; // Nombre del recurso
    private int units; // Número total de unidades del recurso
    private double utilizationPercent; // Porcentaje de utilización del recurso
    private double averageMinutesPerTrip; // Tiempo promedio por viaje (o uso)
    private int totalTrips; // Total de viajes (o usos del recurso)

    public ResourceStatistics(String resourceName) { // Constructor, recibe el nombre del recurso
        this.resourceName = resourceName; // Asigna nombre recibido
    }

    public void calculate(Resource resource, double totalTime, int trips, double totalTripTime) { // Calcula todas las estadísticas a partir del recurso y tiempos acumulados
        this.units = resource.getType().units(); // Obtiene las unidades desde el tipo de recurso
        this.utilizationPercent = resource.getUtilization(totalTime); // Calcula porcentaje de utilización
        this.totalTrips = trips; // Asigna cantidad de viajes/usos

        if (trips > 0) { // Si hubo al menos un viaje
            this.averageMinutesPerTrip = totalTripTime / trips; // Calcula el tiempo promedio por viaje
        }
    }

    // Getters
    public String getResourceName() {
        return resourceName;
    } // Devuelve el nombre del recurso

    public int getUnits() {
        return units;
    } // Devuelve el número de unidades

    public double getUtilizationPercent() {
        return utilizationPercent;
    } // Devuelve el porcentaje de utilización

    public double getAverageMinutesPerTrip() {
        return averageMinutesPerTrip;
    } // Devuelve el tiempo promedio por viaje

    public int getTotalTrips() {
        return totalTrips;
    } // Devuelve el total de viajes

    public int getTotalUsages() {
        return totalTrips;
    } // Devuelve total de usos (igual a totalTrips)
}
