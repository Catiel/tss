package com.simulation.core;

import com.simulation.arrivals.ArrivalGenerator;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.locations.Location;
import com.simulation.locations.LocationType;
import com.simulation.processing.ProcessingRule;
import com.simulation.resources.Resource;
import com.simulation.resources.ResourceType;
import com.simulation.statistics.StatisticsCollector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimulationEngine {
    private final SimulationClock clock;
    private final EventScheduler scheduler;
    private final StatisticsCollector statistics;
    private final Map<String, EntityType> entityTypes;
    private final Map<String, Location> locations;
    private final Map<String, Resource> resources;
    private final Map<String, ProcessingRule> processingRules;
    private final ArrivalGenerator arrivalGenerator;
    private final List<SimulationListener> listeners = new ArrayList<>();
    private double simulationEndTime;

    public SimulationEngine() {
        this.clock = new SimulationClock();
        this.scheduler = new EventScheduler(clock);
        this.statistics = new StatisticsCollector();
        this.entityTypes = new HashMap<>();
        this.locations = new HashMap<>();
        this.resources = new HashMap<>();
        this.processingRules = new HashMap<>();
        this.arrivalGenerator = new ArrivalGenerator(this);
    }

    public void addEntityType(String name, double speed) {
        entityTypes.put(name, new EntityType(name, speed));
    }

    public void addLocation(String name, int capacity, int units) {
        locations.put(name, new Location(new LocationType(name, capacity, units)));
    }

    public void addResource(String name, int units, double speed) {
        resources.put(name, new Resource(new ResourceType(name, units, speed)));
    }

    public void addProcessingRule(ProcessingRule rule) {
        processingRules.put(rule.getLocationName(), rule);
    }

    public void scheduleArrival(String entityTypeName, String locationName,
            double firstTime, int occurrences, double frequency) {
        arrivalGenerator.scheduleArrivals(entityTypeName, locationName,
                firstTime, occurrences, frequency);
    }

    public void run(double endTime) {
        this.simulationEndTime = endTime;

        while (scheduler.hasEvents() && clock.getCurrentTime() < endTime) {
            Event event = scheduler.getNextEvent();
            clock.advanceTo(event.getScheduledTime());
            event.execute();
        }

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime());
        statistics.calculateResourceStatistics(resources, clock.getCurrentTime());
    }

    public void setEndTime(double endTime) {
        this.simulationEndTime = endTime;
    }

    public boolean step(double speedMultiplier) {
        if (!scheduler.hasEvents() || clock.getCurrentTime() >= simulationEndTime) {
            return false;
        }

        Event event = scheduler.getNextEvent();
        clock.advanceTo(event.getScheduledTime());
        event.execute();

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime());
        statistics.calculateResourceStatistics(resources, clock.getCurrentTime());

        return scheduler.hasEvents() && clock.getCurrentTime() < simulationEndTime;
    }

    public void addListener(SimulationListener listener) {
        listeners.add(listener);
    }

    public void removeListener(SimulationListener listener) {
        listeners.remove(listener);
    }

    public void notifyEntityArrival(Entity entity, Location location) {
        for (SimulationListener listener : listeners) {
            listener.onEntityArrival(entity, location, clock.getCurrentTime());
        }
    }

    public void notifyEntityMove(Entity entity, Location from, Location to) {
        for (SimulationListener listener : listeners) {
            listener.onEntityMove(entity, from, to, clock.getCurrentTime());
        }
    }

    public void notifyEntityExit(Entity entity, Location from) {
        for (SimulationListener listener : listeners) {
            listener.onEntityExit(entity, from, clock.getCurrentTime());
        }
    }

    public void notifyResourceAcquired(Resource resource, Entity entity) {
        for (SimulationListener listener : listeners) {
            listener.onResourceAcquired(resource, entity, clock.getCurrentTime());
        }
    }

    public void notifyResourceReleased(Resource resource, Entity entity) {
        for (SimulationListener listener : listeners) {
            listener.onResourceReleased(resource, entity, clock.getCurrentTime());
        }
    }

    public void notifyEntityCreated(Entity entity, Location location) {
        for (SimulationListener listener : listeners) {
            listener.onEntityCreated(entity, location, clock.getCurrentTime());
        }
    }

    public SimulationClock getClock() {
        return clock;
    }

    public EventScheduler getScheduler() {
        return scheduler;
    }

    public StatisticsCollector getStatistics() {
        return statistics;
    }

    public EntityType getEntityType(String name) {
        return entityTypes.get(name);
    }

    public Location getLocation(String name) {
        return locations.get(name);
    }

    public Resource getResource(String name) {
        return resources.get(name);
    }

    public ProcessingRule getProcessingRule(String location) {
        return processingRules.get(location);
    }

    public Map<String, Location> getAllLocations() {
        return locations;
    }

    public Map<String, EntityType> getAllEntityTypes() {
        return entityTypes;
    }

    public Map<String, Resource> getAllResources() {
        return resources;
    }
}
