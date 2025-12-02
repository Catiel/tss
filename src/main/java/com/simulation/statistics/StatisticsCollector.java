package com.simulation.statistics; // Declaración del paquete de estadísticas de la simulación

import com.simulation.entities.Entity; // Importa la clase Entity para manejo de entidades
import com.simulation.entities.EntityStatistics; // Importa la clase de estadísticas de entidades
import com.simulation.locations.Location; // Importa la clase Location para referencias de ubicaciones
import com.simulation.locations.LocationStatistics; // Importa la clase de estadísticas de ubicaciones

import com.simulation.resources.ResourceStatistics; // Importa la clase de estadísticas de recursos

import java.util.HashMap; // Importa HashMap para mapas clave-valor
import java.util.Map; // Importa la interfaz Map para manejo de mapas

public class StatisticsCollector { // Define la clase recolectora y administradora de todas las estadísticas de simulación
    private final Map<String, EntityStatistics> entityStats; // Mapa que almacena estadísticas de entidades indexadas por nombre de tipo
    private final Map<String, LocationStatistics> locationStats; // Mapa que almacena estadísticas de ubicaciones indexadas por nombre
    private final Map<String, Integer> locationEntries; // Mapa que cuenta el número de entradas a cada ubicación
    private final Map<String, Double> locationTotalTime; // Mapa que acumula el tiempo total de procesamiento por ubicación
    private final Map<String, ResourceStatistics> resourceStats; // Mapa que almacena estadísticas de recursos indexadas por nombre
    private final Map<String, Integer> resourceTrips; // Mapa que cuenta el número de viajes de cada recurso
    private final Map<String, Double> resourceTotalTripTime; // Mapa que acumula el tiempo total de viajes por recurso

    public StatisticsCollector() { // Constructor que inicializa el recolector de estadísticas
        this.entityStats = new HashMap<>(); // Inicializa el mapa de estadísticas de entidades vacío
        this.locationStats = new HashMap<>(); // Inicializa el mapa de estadísticas de ubicaciones vacío
        this.locationEntries = new HashMap<>(); // Inicializa el mapa de conteo de entradas vacío
        this.locationTotalTime = new HashMap<>(); // Inicializa el mapa de tiempos de ubicaciones vacío
        this.resourceStats = new HashMap<>(); // Inicializa el mapa de estadísticas de recursos vacío
        this.resourceTrips = new HashMap<>(); // Inicializa el mapa de conteo de viajes vacío
        this.resourceTotalTripTime = new HashMap<>(); // Inicializa el mapa de tiempos de viajes vacío
    }

    public void recordEntityEntry(Entity entity) { // Método que registra la entrada de una entidad al sistema
        String entityName = entity.getType().getName(); // Obtiene el nombre del tipo de entidad
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName)); // Crea estadísticas para este tipo si no existen
        entityStats.get(entityName).recordEntry(); // Registra una entrada en las estadísticas del tipo de entidad
    }

    public void recordEntityExit(Entity entity) { // Método que registra la salida de una entidad del sistema
        String entityName = entity.getType().getName(); // Obtiene el nombre del tipo de entidad
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName)); // Crea estadísticas para este tipo si no existen
        entity.setInSystem(false); // Marca la entidad como fuera del sistema
        entityStats.get(entityName).recordExit(entity); // Registra la salida y tiempos de la entidad en las estadísticas
    }

    public void recordLocationEntry(String locationName) { // Método que registra una entrada a una ubicación específica
        locationEntries.put(locationName, locationEntries.getOrDefault(locationName, 0) + 1); // Incrementa el contador de entradas para esta ubicación
    }

    public void recordLocationProcessingTime(String locationName, double time) { // Método que registra tiempo de procesamiento en una ubicación
        locationTotalTime.put(locationName, locationTotalTime.getOrDefault(locationName, 0.0) + time); // Acumula el tiempo de procesamiento a la ubicación
    }

    public void recordResourceTrip(String resourceName, double time) { // Método que registra un viaje de un recurso con su duración
        resourceTrips.put(resourceName, resourceTrips.getOrDefault(resourceName, 0) + 1); // Incrementa el contador de viajes del recurso
        resourceTotalTripTime.put(resourceName, resourceTotalTripTime.getOrDefault(resourceName, 0.0) + time); // Acumula el tiempo total de viajes del recurso
    }

    public void calculateLocationStatistics(Map<String, Location> locations, double totalSimulationTime) { // Método que calcula estadísticas finales de todas las ubicaciones
        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Itera sobre cada entrada del mapa de ubicaciones
            String name = entry.getKey(); // Obtiene el nombre de la ubicación
            Location location = entry.getValue(); // Obtiene el objeto ubicación
            location.updateOccupancyTime(totalSimulationTime); // Actualiza el tiempo de ocupación de la ubicación al tiempo final

            LocationStatistics stats = new LocationStatistics(name); // Crea nuevo objeto de estadísticas para la ubicación
            int entries = locationEntries.getOrDefault(name, 0); // Obtiene el número total de entradas o cero si no hay
            double totalTime = locationTotalTime.getOrDefault(name, 0.0); // Obtiene el tiempo total de procesamiento o cero si no hay

            stats.calculate(location, totalSimulationTime, entries, totalTime); // Calcula todas las estadísticas de la ubicación
            locationStats.put(name, stats); // Almacena las estadísticas calculadas en el mapa
        }
    }

    public void calculateResourceStatistics(Map<String, com.simulation.resources.Resource> resources, // Método que calcula estadísticas finales de todos los recursos
            double totalSimulationTime) { // Parámetro del tiempo total de simulación
        for (Map.Entry<String, com.simulation.resources.Resource> entry : resources.entrySet()) { // Itera sobre cada entrada del mapa de recursos
            String name = entry.getKey(); // Obtiene el nombre del recurso
            com.simulation.resources.Resource resource = entry.getValue(); // Obtiene el objeto recurso

            com.simulation.resources.ResourceStatistics stats = new com.simulation.resources.ResourceStatistics(name); // Crea nuevo objeto de estadísticas para el recurso
            int trips = resourceTrips.getOrDefault(name, 0); // Obtiene el número total de viajes o cero si no hay
            double totalTripTime = resourceTotalTripTime.getOrDefault(name, 0.0); // Obtiene el tiempo total de viajes o cero si no hay

            stats.calculate(resource, totalSimulationTime, trips, totalTripTime); // Calcula todas las estadísticas del recurso
            resourceStats.put(name, stats); // Almacena las estadísticas calculadas en el mapa
        }
    }

    public EntityReport generateEntityReport(double simulationTime) { // Método que genera un reporte de estadísticas de entidades
        return new EntityReport(entityStats, simulationTime); // Crea y retorna nuevo reporte con las estadísticas y tiempo de simulación
    }

    public LocationReport generateLocationReport(double simulationTime) { // Método que genera un reporte de estadísticas de ubicaciones
        return new LocationReport(locationStats, simulationTime); // Crea y retorna nuevo reporte con las estadísticas y tiempo de simulación
    }

    public Map<String, EntityStatistics> getEntityStats() { // Método getter para obtener el mapa de estadísticas de entidades
        return entityStats; // Retorna el mapa con todas las estadísticas de entidades
    }

    public Map<String, LocationStatistics> getLocationStats() { // Método getter para obtener el mapa de estadísticas de ubicaciones
        return locationStats; // Retorna el mapa con todas las estadísticas de ubicaciones
    }

    public Map<String, com.simulation.resources.ResourceStatistics> getResourceStats() { // Método getter para obtener el mapa de estadísticas de recursos
        return resourceStats; // Retorna el mapa con todas las estadísticas de recursos
    }

    public void reset() { // Método que reinicia todos los mapas de estadísticas a estado vacío
        entityStats.clear(); // Limpia el mapa de estadísticas de entidades
        locationStats.clear(); // Limpia el mapa de estadísticas de ubicaciones
        locationEntries.clear(); // Limpia el mapa de conteo de entradas a ubicaciones
        locationTotalTime.clear(); // Limpia el mapa de tiempos totales de ubicaciones
        resourceStats.clear(); // Limpia el mapa de estadísticas de recursos
        resourceTrips.clear(); // Limpia el mapa de conteo de viajes de recursos
        resourceTotalTripTime.clear(); // Limpia el mapa de tiempos totales de viajes de recursos
    }
}
