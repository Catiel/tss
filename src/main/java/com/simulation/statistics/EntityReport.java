package com.simulation.statistics;

import com.simulation.entities.EntityStatistics;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public record EntityReport(Map<String, EntityStatistics> entityStats, double simulationTime) {

    public List<EntityReportRow> generateRows() {
        List<EntityReportRow> rows = new ArrayList<>();

        for (EntityStatistics stats : entityStats.values()) {
            EntityReportRow row = new EntityReportRow(
                    stats.getEntityName(),
                    stats.getTotalExits(),
                    0, // Cantidad actual en sistema (se calcula dinámicamente)
                    stats.getAverageSystemTime(),
                    stats.getAverageNonValueAddedTime(),
                    stats.getAverageWaitTime(),
                    stats.getAverageValueAddedTime(),
                    0.0, // Tiempo de bloqueo promedio
                    stats.getMinSystemTime(),
                    stats.getMaxSystemTime()
            );
            rows.add(row);
        }

        return rows;
    }

    public String generateTextReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n");
        sb.append("║                                            ENTIDAD RESUMEN                                                            ║\n");
        sb.append("╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n\n");

        List<EntityReportRow> rows = generateRows();

        if (rows.isEmpty()) {
            sb.append("No hay datos de entidades para reportar.\n");
            return sb.toString();
        }

        // Encabezados
        sb.append(String.format("%-25s | %12s | %15s | %20s | %25s | %20s | %25s | %20s\n",
                "Nombre",
                "Total Salida",
                "Cant. Actual",
                "T. Sistema (Min)",
                "T. Movimiento (Min)",
                "T. Espera (Min)",
                "T. Operación (Min)",
                "T. Bloqueo (Min)"));
        sb.append("-".repeat(200)).append("\n");

        // Datos
        for (EntityReportRow row : rows) {
            sb.append(String.format("%-25s | %12d | %15d | %20.2f | %25.2f | %20.2f | %25.2f | %20.2f\n",
                    row.entityName,
                    row.totalExits,
                    row.currentInSystem,
                    row.avgSystemTime,
                    row.avgMoveTime,
                    row.avgWaitTime,
                    row.avgOperationTime,
                    row.avgBlockTime
            ));
        }

        return sb.toString();
    }

    public record EntityReportRow(String entityName, int totalExits, int currentInSystem, double avgSystemTime,
                                  double avgMoveTime, double avgWaitTime, double avgOperationTime, double avgBlockTime,
                                  double minSystemTime, double maxSystemTime) {
    }
}
