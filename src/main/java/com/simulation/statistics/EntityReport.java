package com.simulation.statistics; // Declaración del paquete

import com.simulation.entities.EntityStatistics; // Importa EntityStatistics

import java.util.ArrayList; // Importa ArrayList para manejo de listas
import java.util.List; // Importa interfaz List
import java.util.Map; // Importa interfaz Map

public record EntityReport(Map<String, EntityStatistics> entityStats, double simulationTime) { // Record que contiene estadísticas y tiempo total para generar reportes

    public List<EntityReportRow> generateRows() { // Genera filas de reporte basadas en las estadísticas de entidades
        List<EntityReportRow> rows = new ArrayList<>(); // Lista para filas de reporte

        for (EntityStatistics stats : entityStats.values()) { // Itera sobre cada conjunto de estadísticas
            EntityReportRow row = new EntityReportRow( // Crea fila de reporte por cada tipo de entidad
                    stats.getEntityName(), // Nombre de la entidad
                    stats.getTotalExits(), // Total de salidas
                    0, // Cantidad actual en sistema (se calcula dinámicamente)
                    stats.getAverageSystemTime(), // Tiempo promedio en sistema
                    stats.getAverageNonValueAddedTime(), // Tiempo promedio en movimiento (sin valor)
                    stats.getAverageWaitTime(), // Tiempo promedio de espera
                    stats.getAverageValueAddedTime(), // Tiempo promedio en operación
                    0.0, // Tiempo de bloqueo promedio
                    stats.getMinSystemTime(), // Tiempo mínimo en sistema
                    stats.getMaxSystemTime() // Tiempo máximo en sistema
            );
            rows.add(row); // Agrega fila a la lista
        }

        return rows; // Retorna lista de filas
    }

    public String generateTextReport() { // Genera un reporte en formato texto
        StringBuilder sb = new StringBuilder(); // Crea un StringBuilder para construir el texto
        sb.append("\n╔════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╗\n"); // Encabezado decorativo
        sb.append("║                                            ENTIDAD RESUMEN                                                            ║\n"); // Título centrado
        sb.append("╚════════════════════════════════════════════════════════════════════════════════════════════════════════════════════════╝\n\n"); // Encabezado decorativo final

        List<EntityReportRow> rows = generateRows(); // Obtiene filas de reporte

        if (rows.isEmpty()) { // Si no hay datos para mostrar
            sb.append("No hay datos de entidades para reportar.\n"); // Mensaje de vacío
            return sb.toString(); // Retorna texto completo
        }

        // Encabezados de las columnas
        sb.append(String.format("%-25s | %12s | %15s | %20s | %25s | %20s | %25s | %20s\n",
                "Nombre",
                "Total Salida",
                "Cant. Actual",
                "T. Sistema (Min)",
                "T. Movimiento (Min)",
                "T. Espera (Min)",
                "T. Operación (Min)",
                "T. Bloqueo (Min)"));
        sb.append("-".repeat(200)).append("\n"); // Línea divisoria

        // Datos fila por fila
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

        return sb.toString(); // Retorna el reporte completo como texto
    }

    public record EntityReportRow(String entityName, int totalExits, int currentInSystem, double avgSystemTime,
                                  double avgMoveTime, double avgWaitTime, double avgOperationTime, double avgBlockTime,
                                  double minSystemTime, double maxSystemTime) { // Record que representa una fila detallada del reporte
    }
}
