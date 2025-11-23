package com.simulation.arrivals;

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.processing.OperationHandler;

public class ArrivalGenerator {
    private final SimulationEngine engine;
    private final OperationHandler operationHandler;

    public ArrivalGenerator(SimulationEngine engine) {
        this.engine = engine;
        this.operationHandler = new OperationHandler(engine);
    }

    public void scheduleArrivals(String entityTypeName, String locationName, 
                                double firstTime, int occurrences, double frequency) {
        EntityType entityType = engine.getEntityType(entityTypeName);
        
        if (entityType == null) {
            System.err.println("Tipo de entidad no encontrado: " + entityTypeName);
            return;
        }

        for (int i = 0; i < occurrences; i++) {
            double arrivalTime = firstTime + (i * frequency);
            
            Event arrivalEvent = new Event(arrivalTime, 0, 
                "Arrival of " + entityTypeName + " at " + locationName) {
                @Override
                public void execute() {
                    Entity entity = new Entity(entityType);
                    operationHandler.handleArrival(entity, locationName);
                }
            };
            
            engine.getScheduler().scheduleEvent(arrivalEvent);
        }
    }
}
