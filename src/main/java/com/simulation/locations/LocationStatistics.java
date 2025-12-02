package com.simulation.locations; // Declaración del paquete de ubicaciones de la simulación

public class LocationStatistics { // Define la clase que almacena y calcula estadísticas de una ubicación
    private final String locationName; // Nombre identificador de la ubicación para estas estadísticas
    private double scheduledTime; // Tiempo total programado de ejecución de la simulación
    private int capacity; // Capacidad máxima de entidades que puede contener la ubicación
    private int totalEntries; // Contador total de entradas de entidades a la ubicación
    private double averageTimePerEntry; // Tiempo promedio que cada entidad permanece en la ubicación
    private double averageContents; // Promedio de entidades presentes en la ubicación durante la simulación
    private double maxContents; // Contenido máximo permitido igual a la capacidad de la ubicación
    private double currentContents; // Cantidad actual de entidades presentes en la ubicación
    private double utilizationPercent; // Porcentaje de utilización calculado como contenido promedio sobre capacidad
    private double busyUtilizationPercent; // Porcentaje de tiempo que la ubicación estuvo ocupada

    public LocationStatistics(String locationName) { // Constructor que recibe el nombre de la ubicación
        this.locationName = locationName; // Asigna el nombre recibido a la variable de instancia
    }

    public void calculate(Location location, double totalSimulationTime, int entries, double totalProcessingTime) { // Método que calcula todas las estadísticas de la ubicación
        this.scheduledTime = totalSimulationTime; // Asigna el tiempo total de simulación
        this.capacity = location.getType().capacity(); // Obtiene y asigna la capacidad del tipo de ubicación
        this.totalEntries = entries; // Asigna el número total de entradas recibido

        if (entries > 0) { // Verifica si hubo al menos una entrada para evitar división por cero
            this.averageTimePerEntry = location.getTotalOccupancyTime() / entries; // Calcula tiempo promedio dividiendo ocupación total entre entradas
        } else { // Si no hubo entradas
            this.averageTimePerEntry = 0.0; // Establece tiempo promedio en cero
        }

        this.averageContents = location.getTotalOccupancyTime() / totalSimulationTime; // Calcula contenido promedio dividiendo ocupación entre tiempo total
        this.maxContents = capacity; // Asigna el contenido máximo igual a la capacidad
        this.currentContents = location.getCurrentOccupancy(); // Obtiene y asigna la ocupación actual de la ubicación

        if (capacity > 0) { // Verifica si la capacidad es mayor a cero para evitar división por cero
            this.utilizationPercent = (averageContents / capacity) * 100.0; // Calcula porcentaje de utilización como razón de contenido promedio sobre capacidad
        } else { // Si la capacidad es cero o infinita
            this.utilizationPercent = 0.0; // Establece utilización en cero
        }

        if (totalSimulationTime > 0) { // Verifica si el tiempo de simulación es mayor a cero
            busyUtilizationPercent = (location.getBusyTime() / totalSimulationTime) * 100.0; // Calcula porcentaje de tiempo ocupado sobre tiempo total
        } else { // Si no hay tiempo de simulación
            busyUtilizationPercent = 0.0; // Establece utilización ocupada en cero
        }
    }

    public String getLocationName() { // Método getter para obtener el nombre de la ubicación
        return locationName; // Retorna el nombre identificador de la ubicación
    }

    public double getScheduledTime() { // Método getter para obtener el tiempo programado
        return scheduledTime; // Retorna el tiempo total de simulación programado
    }

    public int getCapacity() { // Método getter para obtener la capacidad
        return capacity; // Retorna la capacidad máxima de la ubicación
    }

    public int getTotalEntries() { // Método getter para obtener el total de entradas
        return totalEntries; // Retorna el número total de entradas registradas
    }

    public double getAverageTimePerEntry() { // Método getter para obtener tiempo promedio por entrada
        return averageTimePerEntry; // Retorna el tiempo promedio que cada entidad pasó en la ubicación
    }

    public double getAverageContents() { // Método getter para obtener contenido promedio
        return averageContents; // Retorna el promedio de entidades presentes durante la simulación
    }

    public double getMaxContents() { // Método getter para obtener contenido máximo
        return maxContents; // Retorna el contenido máximo permitido en la ubicación
    }

    public double getCurrentContents() { // Método getter para obtener contenido actual
        return currentContents; // Retorna la cantidad actual de entidades en la ubicación
    }

    public double getUtilizationPercent() { // Método getter para obtener porcentaje de utilización
        return utilizationPercent; // Retorna el porcentaje de utilización basado en capacidad
    }

    public double getBusyUtilizationPercent() { // Método getter para obtener porcentaje de utilización ocupada
        return busyUtilizationPercent; // Retorna el porcentaje de tiempo que la ubicación estuvo ocupada
    }
}
