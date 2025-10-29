package com.simulation.resources;

/**
 * Locación de procesamiento (LAVADORA, PINTURA, HORNO)
 * Realiza operaciones sobre las entidades
 */
public class ProcessingLocation extends Location {

    public ProcessingLocation(String name, int capacity) {
        super(name, capacity);
    }

    @Override
    public double getUtilization(double currentTime) {
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) {
            return 0.0;
        }
        double averageBusyUnits = getAverageContent(currentTime);
        return (averageBusyUnits / capacity) * 100.0;
    }
}
