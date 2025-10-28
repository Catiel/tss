package com.simulation.statistics;

import com.simulation.core.Entity;
import com.simulation.resources.Location;

import java.util.*;

public class Statistics {
    private int totalArrivals;
    private int totalExits;
    private double totalSystemTime;
    private double simulationDuration;

    private Map<String, Location> locations;
    private List<Double> entitySystemTimes;

    public Statistics() {
        this.totalArrivals = 0;
        this.totalExits = 0;
        this.totalSystemTime = 0;
        this.simulationDuration = 0;
        this.locations = new HashMap<>();
        this.entitySystemTimes = new ArrayList<>();
    }

    public void registerLocation(Location location) {
        locations.put(location.getName(), location);
    }

    public void recordArrival() {
        totalArrivals++;
    }

    public void recordExit(Entity entity, double currentTime) {
        totalExits++;
        double systemTime = entity.getTotalSystemTime(currentTime);
        totalSystemTime += systemTime;
        entitySystemTimes.add(systemTime);
    }

    public void finalizeStatistics(double currentTime) {
        this.simulationDuration = currentTime;
    }

    public int getTotalArrivals() {
        return totalArrivals;
    }

    public int getTotalExits() {
        return totalExits;
    }

    public double getAverageSystemTime() {
        if (totalExits == 0) return 0;
        return totalSystemTime / totalExits;
    }

    public double getThroughput() {
        // Throughput = piezas completadas / tiempo total (en horas)
        if (simulationDuration <= 0) return 0;
        return (totalExits / simulationDuration) * 60.0; // Convertir a piezas/hora
    }

    public double getSimulationDuration() {
        return simulationDuration;
    }

    public Map<String, Location> getLocations() {
        return locations;
    }

    public Location getLocation(String name) {
        return locations.get(name);
    }

    public double getLocationUtilization(String locationName, double currentTime) {
        Location loc = locations.get(locationName);
        if (loc == null) return 0;
        return loc.getUtilization(currentTime);
    }

    public int getLocationCurrentContent(String locationName) {
        Location loc = locations.get(locationName);
        if (loc == null) return 0;
        return loc.getCurrentContent();
    }

    public int getLocationQueueSize(String locationName) {
        Location loc = locations.get(locationName);
        if (loc == null) return 0;
        return loc.getQueueSize();
    }

    public int getLocationTotalEntries(String locationName) {
        Location loc = locations.get(locationName);
        if (loc == null) return 0;
        return loc.getTotalEntries();
    }

    public List<Double> getEntitySystemTimes() {
        return new ArrayList<>(entitySystemTimes);
    }

    public double getMinSystemTime() {
        if (entitySystemTimes.isEmpty()) return 0;
        return Collections.min(entitySystemTimes);
    }

    public double getMaxSystemTime() {
        if (entitySystemTimes.isEmpty()) return 0;
        return Collections.max(entitySystemTimes);
    }

    public double getStdDevSystemTime() {
        if (entitySystemTimes.size() < 2) return 0;

        double mean = getAverageSystemTime();
        double sumSquares = 0;

        for (double time : entitySystemTimes) {
            sumSquares += Math.pow(time - mean, 2);
        }

        return Math.sqrt(sumSquares / entitySystemTimes.size());
    }

    public void reset() {
        totalArrivals = 0;
        totalExits = 0;
        totalSystemTime = 0;
        simulationDuration = 0;
        entitySystemTimes.clear();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== ESTADÍSTICAS DE SIMULACIÓN ===\n");
        sb.append(String.format("Duración: %.2f minutos\n", simulationDuration));
        sb.append(String.format("Total de Arribos: %d\n", totalArrivals));
        sb.append(String.format("Total de Salidas: %d\n", totalExits));
        sb.append(String.format("Throughput: %.2f piezas/hora\n", getThroughput()));
        sb.append(String.format("Tiempo Promedio en Sistema: %.2f minutos\n", getAverageSystemTime()));
        sb.append(String.format("Desv. Estándar Tiempo en Sistema: %.2f minutos\n", getStdDevSystemTime()));
        sb.append(String.format("Tiempo Mínimo en Sistema: %.2f minutos\n", getMinSystemTime()));
        sb.append(String.format("Tiempo Máximo en Sistema: %.2f minutos\n", getMaxSystemTime()));

        sb.append("\n=== ESTADÍSTICAS POR LOCACIÓN ===\n");
        for (Map.Entry<String, Location> entry : locations.entrySet()) {
            Location loc = entry.getValue();
            sb.append(String.format("\n%s:\n", loc.getName()));
            sb.append(String.format("  Capacidad: %d\n",
                loc.getCapacity() == Integer.MAX_VALUE ? -1 : loc.getCapacity()));
            sb.append(String.format("  Contenido Actual: %d\n", loc.getCurrentContent()));
            sb.append(String.format("  Cola Actual: %d\n", loc.getQueueSize()));
            sb.append(String.format("  Total Entradas: %d\n", loc.getTotalEntries()));
            sb.append(String.format("  Total Salidas: %d\n", loc.getTotalExits()));
            sb.append(String.format("  Utilización: %.2f%%\n", loc.getUtilization(simulationDuration)));
        }

        return sb.toString();
    }
}
