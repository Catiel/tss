package com.simulation.statistics; // Declaración del paquete

import com.simulation.entities.Entity;
import com.simulation.entities.EntityStatistics;
import com.simulation.locations.Location;
import com.simulation.locations.LocationStatistics;

import com.simulation.resources.ResourceStatistics;

import java.util.HashMap;
import java.util.Map;

public class StatisticsCollector { // Clase que recolecta y administra estadísticas de la simulación
    private final Map<String, EntityStatistics> entityStats;
    private final Map<String, LocationStatistics> locationStats;
    private final Map<String, Integer> locationEntries;
    private final Map<String, Double> locationTotalTime;
    private final Map<String, ResourceStatistics> resourceStats;
    private final Map<String, Integer> resourceTrips;
    private final Map<String, Double> resourceTotalTripTime;

    public StatisticsCollector() {
        this.entityStats = new HashMap<>();
        this.locationStats = new HashMap<>();
        this.locationEntries = new HashMap<>();
        this.locationTotalTime = new HashMap<>();
        this.resourceStats = new HashMap<>();
        this.resourceTrips = new HashMap<>();
        this.resourceTotalTripTime = new HashMap<>();
    }

    public void recordEntityEntry(Entity entity) {
        String entityName = entity.getType().getName();
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName));
        entityStats.get(entityName).recordEntry();
    }

    public void recordEntityExit(Entity entity) {
        String entityName = entity.getType().getName();
        entityStats.putIfAbsent(entityName, new EntityStatistics(entityName));
        entity.setInSystem(false);
        entityStats.get(entityName).recordExit(entity);
    }

    public void recordLocationEntry(String locationName) {
        locationEntries.put(locationName, locationEntries.getOrDefault(locationName, 0) + 1);
    }

    public void recordLocationProcessingTime(String locationName, double time) {
        locationTotalTime.put(locationName, locationTotalTime.getOrDefault(locationName, 0.0) + time);
    }

    public void recordResourceTrip(String resourceName, double time) {
        resourceTrips.put(resourceName, resourceTrips.getOrDefault(resourceName, 0) + 1);
        resourceTotalTripTime.put(resourceName, resourceTotalTripTime.getOrDefault(resourceName, 0.0) + time);
    }

    public void calculateLocationStatistics(Map<String, Location> locations, double totalSimulationTime) {
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            String name = entry.getKey();
            Location location = entry.getValue();

            LocationStatistics stats = new LocationStatistics(name);
            int entries = locationEntries.getOrDefault(name, 0);
            double totalTime = locationTotalTime.getOrDefault(name, 0.0);

            stats.calculate(location, totalSimulationTime, entries, totalTime);
            locationStats.put(name, stats);
        }
    }

    public void calculateResourceStatistics(Map<String, com.simulation.resources.Resource> resources,
            double totalSimulationTime) {
        for (Map.Entry<String, com.simulation.resources.Resource> entry : resources.entrySet()) {
            String name = entry.getKey();
            com.simulation.resources.Resource resource = entry.getValue();

            com.simulation.resources.ResourceStatistics stats = new com.simulation.resources.ResourceStatistics(name);
            int trips = resourceTrips.getOrDefault(name, 0);
            double totalTripTime = resourceTotalTripTime.getOrDefault(name, 0.0);

            stats.calculate(resource, totalSimulationTime, trips, totalTripTime);
            resourceStats.put(name, stats);
        }
    }

    public EntityReport generateEntityReport(double simulationTime) {
        return new EntityReport(entityStats, simulationTime);
    }

    public LocationReport generateLocationReport(double simulationTime) {
        return new LocationReport(locationStats, simulationTime);
    }

    public Map<String, EntityStatistics> getEntityStats() {
        return entityStats;
    }

    public Map<String, LocationStatistics> getLocationStats() {
        return locationStats;
    }

    public Map<String, com.simulation.resources.ResourceStatistics> getResourceStats() {
        return resourceStats;
    }

    public void reset() {
        entityStats.clear();
        locationStats.clear();
        locationEntries.clear();
        locationTotalTime.clear();
        resourceStats.clear();
        resourceTrips.clear();
        resourceTotalTripTime.clear();
    }
}
