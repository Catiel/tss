package com.simulation.statistics; // Declaración del paquete

import com.simulation.entities.Entity; // Importa la clase Entity
import com.simulation.entities.EntityStatistics; // Importa estadísticas de entidades
import com.simulation.locations.Location; // Importa la clase Location
import com.simulation.locations.LocationStatistics; // Importa estadísticas de locaciones

import java.util.HashMap; // Importa HashMap para mapas
import java.util.Map; // Importa Map

public class StatisticsCollector { // Clase que recolecta y administra estadísticas de la simulación
    private final Map<String, EntityStatistics> entityStats; // Mapa de estadísticas por tipo de entidad
    private final Map<String, LocationStatistics> locationStats; // Mapa de estadísticas por locación
    private final Map<String, Integer> locationEntries; // Conteo de entradas por locación
    private final Map<String, Double> locationTotalTime; // Tiempo total de procesamiento por locación

    public StatisticsCollector() { // Constructor que inicializa las colecciones
        this.entityStats = new HashMap<>(); // Inicializa mapa de estadísticas de entidades
        this.locationStats = new HashMap<>(); // Inicializa estadísticas de locaciones
        this.locationEntries = new HashMap<>(); // Inicializa conteo de entradas
        this.locationTotalTime = new HashMap<>(); // Inicializa tiempos de procesamiento
    }

    public void recordEntityEntry(Entity entity) { // Registra la entrada de una entidad
        String entityName = entity.getType().getName(); // Obtiene el tipo de entidad
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName)); // Crea estadísticas si no existen
        entityStats.get(entityName).recordEntry(); // Incrementa contador de entradas
    }

    public void recordEntityExit(Entity entity) { // Registra la salida de una entidad
        String entityName = entity.getType().getName(); // Obtiene tipo de entidad
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName)); // Crea estadística si no existe
        entity.setInSystem(false); // Marca entidad como fuera del sistema
        entityStats.get(entityName).recordExit(entity); // Actualiza estadísticas con salida
    }

    public void recordLocationEntry(String locationName) { // Registra entrada a una locación
        locationEntries.put(locationName, locationEntries.getOrDefault(locationName, 0) + 1); // Incrementa contador entrada
    }

    public void recordLocationProcessingTime(String locationName, double time) { // Registra tiempo de procesamiento para locación
        locationTotalTime.put(locationName, locationTotalTime.getOrDefault(locationName, 0.0) + time); // Acumula tiempo procesado
    }

    public void calculateLocationStatistics(Map<String, Location> locations, double totalSimulationTime) { // Calcula estadísticas totales para locaciones
        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Para cada ubicación en el mapa
            String name = entry.getKey(); // Obtiene nombre de la locación
            Location location = entry.getValue(); // Obtiene objeto Location

            LocationStatistics stats = new LocationStatistics(name); // Crea objeto de estadísticas
            int entries = locationEntries.getOrDefault(name, 0); // Obtiene total entradas o 0
            double totalTime = locationTotalTime.getOrDefault(name, 0.0); // Obtiene tiempo total o 0

            stats.calculate(location, totalSimulationTime, entries, totalTime); // Calcula estadísticas con datos actuales
            locationStats.put(name, stats); // Almacena en mapa de estadísticas por locación
        }
    }

    public EntityReport generateEntityReport(double simulationTime) { // Genera reporte de entidades
        return new EntityReport(entityStats, simulationTime); // Retorna nuevo reporte de entidades
    }

    public LocationReport generateLocationReport(double simulationTime) { // Genera reporte de locaciones
        return new LocationReport(locationStats, simulationTime); // Retorna nuevo reporte de locaciones
    }

    public Map<String, EntityStatistics> getEntityStats() { // Devuelve mapa de estadísticas de entidades
        return entityStats;
    }

    public Map<String, LocationStatistics> getLocationStats() { // Devuelve mapa de estadísticas de locaciones
        return locationStats;
    }

    public void reset() { // Limpia todas las estadísticas y conteos
        entityStats.clear();
        locationStats.clear();
        locationEntries.clear();
        locationTotalTime.clear();
    }
}
