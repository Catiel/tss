package com.simulation.statistics;

import com.simulation.locations.LocationStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record LocationReport(Map<String, LocationStatistics> locationStats, double simulationTime) {

    public List<LocationReportRow> generateRows() {
        List<LocationReportRow> rows = new ArrayList<>();

        for (LocationStatistics stats : locationStats.values()) {
            LocationReportRow row = new LocationReportRow(
                    stats.getLocationName(),
                    stats.getScheduledTime() / 60.0, // Convertir a horas
                    stats.getCapacity(),
                    stats.getTotalEntries(),
                    stats.getAverageTimePerEntry(),
                    stats.getAverageContents(),
                    stats.getMaxContents(),
                    stats.getCurrentContents(),
                    stats.getUtilizationPercent()
            );
            rows.add(row);
        }

        return rows;
    }

    public String generateTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                                           LOCACIÓN RESUMEN                                                            ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n\n");

        List<LocationReportRow> rows = generateRows();

        if (rows.isEmpty()) {
            sb.append("No hay datos de locaciones para reportar.\n");
            return sb.toString();
        }

        // Encabezados
        sb.append(String.format("%-25s | %18s | %12s | %15s | %25s | %20s | %18s | %18s | %15s\n",
                "Nombre",
                "T. Programado (Hr)",
                "Capacidad",
                "Total Entradas",
                "T. Por Entrada (Min)",
                "Contenido Prom.",
                "Contenido Máx.",
                "Contenido Actual",
                "% Utilización"));
        sb.append("-".repeat(200)).append("\n");

        // Datos
        for (LocationReportRow row : rows) {
            sb.append(String.format("%-25s | %18.2f | %12d | %15d | %25.2f | %20.2f | %18.2f | %18.2f | %15.2f\n",
                    row.locationName,
                    row.scheduledTimeHours,
                    row.capacity,
                    row.totalEntries,
                    row.avgTimePerEntry,
                    row.avgContents,
                    row.maxContents,
                    row.currentContents,
                    row.utilizationPercent
            ));
        }

        return sb.toString();
    }

    public record LocationReportRow(String locationName, double scheduledTimeHours, int capacity, int totalEntries,
                                    double avgTimePerEntry, double avgContents, double maxContents,
                                    double currentContents, double utilizationPercent) {
    }
}
