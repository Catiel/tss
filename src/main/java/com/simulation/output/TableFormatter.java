package com.simulation.output; // Declaración del paquete donde se encuentra esta clase

public class TableFormatter { // Clase utilitaria para formatear tablas de estadísticas en texto

    public static String formatDouble(double value, int decimals) { // Método estático para formatear números decimales con cantidad específica de decimales
        return String.format("%." + decimals + "f", value); // Retorna el número formateado como string con el número de decimales especificado
    }

    public static String formatEntityTable(java.util.Map<String, com.simulation.entities.EntityStatistics> stats) { // Método estático para formatear la tabla de estadísticas de entidades
        StringBuilder sb = new StringBuilder(); // Crea un StringBuilder para construir eficientemente la tabla como string

        sb.append("\n=== ENTIDAD RESUMEN ===\n\n"); // Agrega el título de la sección de entidades con líneas en blanco
        sb.append(String.format("%-25s %15s %25s %35s %40s %30s %30s %30s\n", // Agrega la fila de encabezados con formato de columnas alineadas a la izquierda y derecha
            "Nombre", // Encabezado de la columna de nombre
            "Total Salida", // Encabezado de total de salidas
            "Cantidad actual en Sistema", // Encabezado de cantidad actual en sistema
            "Tiempo En Sistema Promedio (Min)", // Encabezado de tiempo promedio en sistema
            "Tiempo En lógica de movimiento Promedio (Min)", // Encabezado de tiempo promedio de movimiento
            "Tiempo Espera Promedio (Min)", // Encabezado de tiempo promedio de espera
            "Tiempo En Operación Promedio (Min)", // Encabezado de tiempo promedio de operación
            "Tiempo de Bloqueo Promedio (Min)")); // Encabezado de tiempo promedio de bloqueo
        sb.append("-".repeat(230)).append("\n"); // Agrega una línea separadora de 230 guiones seguida de salto de línea

        for (com.simulation.entities.EntityStatistics stat : stats.values()) { // Itera sobre todas las estadísticas de entidades en el mapa
            sb.append(String.format("%-25s %15d %25d %35s %40s %30s %30s %30s\n", // Agrega una fila de datos con formato de columnas
                stat.getEntityName(), // Nombre de la entidad alineado a la izquierda
                stat.getTotalExits(), // Total de salidas alineado a la derecha
                stat.getCurrentInSystem(), // ACTUALIZADO para mostrar cantidad actual en sistema
                formatDouble(stat.getAverageSystemTime(), 2), // Tiempo promedio en sistema formateado con 2 decimales
                formatDouble(stat.getAverageNonValueAddedTime(), 2), // Tiempo promedio sin valor agregado formateado con 2 decimales
                formatDouble(stat.getAverageWaitTime(), 2), // Tiempo promedio de espera formateado con 2 decimales
                formatDouble(stat.getAverageValueAddedTime(), 2), // Tiempo promedio de operación formateado con 2 decimales
                "0.00" // Tiempo de bloqueo fijo en 0.00 (no implementado)
            ));
        }

        return sb.toString(); // Retorna la tabla completa como string
    }

    public static String formatLocationTable(java.util.Map<String, com.simulation.locations.LocationStatistics> stats) { // Método estático para formatear la tabla de estadísticas de ubicaciones
        StringBuilder sb = new StringBuilder(); // Crea un StringBuilder para construir la tabla

        sb.append("\n=== LOCACIÓN RESUMEN ===\n\n"); // Agrega el título de la sección de ubicaciones con líneas en blanco
        sb.append(String.format("%-25s %20s %15s %20s %30s %25s %20s %20s %20s\n", // Agrega la fila de encabezados con formato de columnas
            "Nombre", "Tiempo Programado (Hr)", "Capacidad", "Total Entradas", // Primeros cuatro encabezados
            "Tiempo Por entrada Promedio (Min)", "Contenido Promedio", "Contenido Máximo", // Siguientes tres encabezados
            "Contenido Actual", "% Utilización")); // Últimos dos encabezados
        sb.append("-".repeat(200)).append("\n"); // Agrega una línea separadora de 200 guiones seguida de salto de línea

        for (com.simulation.locations.LocationStatistics stat : stats.values()) { // Itera sobre todas las estadísticas de ubicaciones en el mapa
            sb.append(String.format("%-25s %20s %15d %20d %30s %25s %20s %20s %20s\n", // Agrega una fila de datos con formato de columnas
                stat.getLocationName(), // Nombre de la ubicación alineado a la izquierda
                formatDouble(stat.getScheduledTime() / 60.0, 2), // Tiempo programado convertido de minutos a horas con 2 decimales
                stat.getCapacity(), // Capacidad de la ubicación
                stat.getTotalEntries(), // Total de entradas a la ubicación
                formatDouble(stat.getAverageTimePerEntry(), 2), // Tiempo promedio por entrada formateado con 2 decimales
                formatDouble(stat.getAverageContents(), 2), // Contenido promedio formateado con 2 decimales
                formatDouble(stat.getMaxContents(), 2), // Contenido máximo formateado con 2 decimales
                formatDouble(stat.getCurrentContents(), 2), // Contenido actual formateado con 2 decimales
                formatDouble(stat.getUtilizationPercent(), 2) // Porcentaje de utilización formateado con 2 decimales
            ));
        }

        return sb.toString(); // Retorna la tabla completa como string
    }
}
