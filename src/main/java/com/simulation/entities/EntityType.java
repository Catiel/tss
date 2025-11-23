package com.simulation.entities;

public class EntityType {
    private final String name;
    private final double speedMetersPerMinute;
    private EntityStatistics statistics;

    public EntityType(String name, double speedMetersPerMinute) {
        this.name = name;
        this.speedMetersPerMinute = speedMetersPerMinute;
        this.statistics = new EntityStatistics(name);
    }

    public String getName() {
        return name;
    }

    public double getSpeedMetersPerMinute() {
        return speedMetersPerMinute;
    }
    
    public EntityStatistics getStatistics() {
        return statistics;
    }
    
    public void setStatistics(EntityStatistics statistics) {
        this.statistics = statistics;
    }
}
