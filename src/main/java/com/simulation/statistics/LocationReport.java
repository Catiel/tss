package com.simulation.statistics; // Declaración del paquete

import com.simulation.locations.LocationStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record LocationReport(Map<String, LocationStatistics> locationStats,
                             double simulationTime) { // Record que contiene estadísticas y tiempo para reporte

    public List<LocationReportRow> generateRows() { // Genera una lista de filas de reporte a partir de estadísticas
        List<LocationReportRow> rows = new ArrayList<>(); // Lista para filas

        for (LocationStatistics stats : locationStats.values()) { // Itera sobre las estadísticas de locaciones
            LocationReportRow row = new LocationReportRow( // Crea una fila por cada locación
                    stats.getLocationName(), // Nombre de la locación
                    stats.getScheduledTime() / 60.0, // Tiempo programado convertido a horas
                    stats.getCapacity(), // Capacidad de la locación
                    stats.getTotalEntries(), // Total de entradas a la locación
                    stats.getAverageTimePerEntry(), // Tiempo promedio por entrada
                    stats.getAverageContents(), // Contenido promedio
                    stats.getMaxContents(), // Contenido máximo
                    stats.getCurrentContents(), // Contenido actual
                    stats.getUtilizationPercent() // Porcentaje de utilización
            );
            rows.add(row); // Agrega fila a lista
        }

        return rows; // Retorna lista de filas
    }

    public String generateTextReport() { // Genera el reporte en formato texto
        StringBuilder sb = new StringBuilder(); // Crea StringBuilder para construcción de texto
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n"); // Encabezado decorativo
        sb.append("║                                           LOCACIÓN RESUMEN                                                            ║\n"); // Título centrado
        sb.append("╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n\n"); // Encabezado decorativo final

        List<LocationReportRow> rows = generateRows(); // Obtiene filas de reporte

        if (rows.isEmpty()) { // Si no hay datos
            sb.append("No hay datos de locaciones para reportar.\n"); // Mensaje de vacío
            return sb.toString(); // Retorna texto
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
        sb.append("-".repeat(200)).append("\n"); // Línea divisoria

        // Datos fila por fila
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

        return sb.toString(); // Retorna texto del reporte completo
    }

    public record LocationReportRow(String locationName, double scheduledTimeHours, int capacity, int totalEntries,
                                    double avgTimePerEntry, double avgContents, double maxContents,
                                    double currentContents,
                                    double utilizationPercent) { // Record que representa una fila del reporte de locaciones
    }
}
