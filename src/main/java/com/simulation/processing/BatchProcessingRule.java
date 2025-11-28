package com.simulation.processing;

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;

public class BatchProcessingRule extends ProcessingRule {
    private final int batchSize;

    public BatchProcessingRule(String locationName, String entityTypeName, double processingTime, int batchSize) {
        super(locationName, entityTypeName, processingTime);
        this.batchSize = batchSize;
    }

    @Override
    public void process(Entity entity, SimulationEngine engine) {
        // La lógica de acumulación se maneja en OperationHandler.handleAccumulate()
        // Esta regla solo define los parámetros
    }

    public int getBatchSize() {
        return batchSize;
    }
}
