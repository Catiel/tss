package com.simulation.output; // Declaración del paquete de salida y formateo de reportes

public class TableFormatter { // Define la clase utilitaria estática para formatear tablas de estadísticas

    public static String formatDouble(double value, int decimals) { // Método estático que formatea un número decimal con precisión específica
        return String.format("%." + decimals + "f", value); // Construye formato dinámico y retorna el número formateado como string
    }

    public static String formatEntityTable(java.util.Map<String, com.simulation.entities.EntityStatistics> stats) { // Método estático que genera tabla formateada de estadísticas de entidades
        StringBuilder sb = new StringBuilder(); // Crea StringBuilder para construcción eficiente de strings

        sb.append("\n=== ENTIDAD RESUMEN ===\n\n"); // Agrega título de sección con saltos de línea antes y después
        sb.append(String.format("%-25s %15s %25s %35s %40s %30s %30s %30s\n", // Agrega encabezados con formato de columnas con anchos específicos
                "Nombre", // Columna 1: Nombre de entidad con 25 caracteres alineado izquierda
                "Total Salida", // Columna 2: Total de salidas con 15 caracteres alineado derecha
                "Cantidad actual en Sistema", // Columna 3: Cantidad actual con 25 caracteres alineado derecha
                "Tiempo En Sistema Promedio (Min)", // Columna 4: Tiempo en sistema con 35 caracteres alineado derecha
                "Tiempo En lógica de movimiento Promedio (Min)", // Columna 5: Tiempo de movimiento con 40 caracteres alineado derecha
                "Tiempo Espera Promedio (Min)", // Columna 6: Tiempo de espera con 30 caracteres alineado derecha
                "Tiempo En Operación Promedio (Min)", // Columna 7: Tiempo de operación con 30 caracteres alineado derecha
                "Tiempo de Bloqueo Promedio (Min)")); // Columna 8: Tiempo de bloqueo con 30 caracteres alineado derecha
        sb.append("-".repeat(230)).append("\n"); // Agrega línea separadora de 230 guiones y salto de línea

        for (com.simulation.entities.EntityStatistics stat : stats.values()) { // Itera sobre cada estadística de entidad en la colección de valores
            sb.append(String.format("%-25s %15d %25d %35s %40s %30s %30s %30s\n", // Agrega fila de datos con mismo formato de columnas que encabezados
                    stat.getEntityName(), // Obtiene y formatea nombre de entidad alineado izquierda
                    stat.getTotalExits(), // Obtiene y formatea total de salidas como entero
                    stat.getCurrentInSystem(), // Obtiene y formatea cantidad actual en sistema como entero
                    formatDouble(stat.getAverageSystemTime(), 2), // Obtiene tiempo promedio en sistema y formatea con 2 decimales
                    formatDouble(stat.getAverageNonValueAddedTime(), 2), // Obtiene tiempo sin valor agregado y formatea con 2 decimales
                    formatDouble(stat.getAverageWaitTime(), 2), // Obtiene tiempo de espera y formatea con 2 decimales
                    formatDouble(stat.getAverageValueAddedTime(), 2), // Obtiene tiempo de operación y formatea con 2 decimales
                    "0.00" // Valor fijo de tiempo de bloqueo ya que no está implementado
            ));
        }

        return sb.toString(); // Convierte StringBuilder a String y retorna la tabla completa
    }

    public static String formatLocationTable(java.util.Map<String, com.simulation.locations.LocationStatistics> stats) { // Método estático que genera tabla formateada de estadísticas de ubicaciones
        StringBuilder sb = new StringBuilder(); // Crea StringBuilder para construcción eficiente de la tabla

        sb.append("\n=== LOCACIÓN RESUMEN ===\n\n"); // Agrega título de sección de ubicaciones con saltos de línea
        sb.append(String.format("%-25s %20s %15s %20s %30s %25s %20s %20s %20s\n", // Agrega encabezados de columnas con anchos específicos
                "Nombre", "Tiempo Programado (Hr)", "Capacidad", "Total Entradas", // Primeros 4 encabezados: nombre, tiempo, capacidad y entradas
                "Tiempo Por entrada Promedio (Min)", "Contenido Promedio", "Contenido Máximo", // Siguientes 3 encabezados: tiempo por entrada y contenidos
                "Contenido Actual", "% Utilización")); // Últimos 2 encabezados: contenido actual y utilización
        sb.append("-".repeat(200)).append("\n"); // Agrega línea separadora de 200 guiones y salto de línea

        for (com.simulation.locations.LocationStatistics stat : stats.values()) { // Itera sobre cada estadística de ubicación en la colección
            sb.append(String.format("%-25s %20s %15d %20d %30s %25s %20s %20s %20s\n", // Agrega fila de datos con formato coincidente con encabezados
                    stat.getLocationName(), // Obtiene y formatea nombre de ubicación alineado izquierda
                    formatDouble(stat.getScheduledTime() / 60.0, 2), // Convierte tiempo de minutos a horas y formatea con 2 decimales
                    stat.getCapacity(), // Obtiene y formatea capacidad como entero
                    stat.getTotalEntries(), // Obtiene y formatea total de entradas como entero
                    formatDouble(stat.getAverageTimePerEntry(), 2), // Obtiene tiempo promedio por entrada y formatea con 2 decimales
                    formatDouble(stat.getAverageContents(), 2), // Obtiene contenido promedio y formatea con 2 decimales
                    formatDouble(stat.getMaxContents(), 2), // Obtiene contenido máximo y formatea con 2 decimales
                    formatDouble(stat.getCurrentContents(), 2), // Obtiene contenido actual y formatea con 2 decimales
                    formatDouble(stat.getUtilizationPercent(), 2) // Obtiene porcentaje de utilización y formatea con 2 decimales
            ));
        }

        return sb.toString(); // Convierte StringBuilder a String y retorna la tabla completa
    }

    public static String formatResourceTable(java.util.Map<String, com.simulation.resources.ResourceStatistics> stats) { // Método estático que genera tabla formateada de estadísticas de recursos
        StringBuilder sb = new StringBuilder(); // Crea StringBuilder para construcción eficiente de la tabla

        sb.append("\n=== RECURSO RESUMEN ===\n\n"); // Agrega título de sección de recursos con saltos de línea
        sb.append(String.format("%-25s %15s %20s %20s %20s\n", "Nombre", "Unidades", "% Utilización", "Tiempo Promedio/Viaje", "Total Viajes")); // Agrega encabezados de 5 columnas con anchos específicos
        sb.append("-".repeat(110)).append("\n"); // Agrega línea separadora de 110 guiones y salto de línea

        for (com.simulation.resources.ResourceStatistics stat : stats.values()) { // Itera sobre cada estadística de recurso en la colección
            sb.append(String.format("%-25s %15d %20s %20s %20d\n", stat.getResourceName(), stat.getUnits(), formatDouble(stat.getUtilizationPercent(), 2), formatDouble(stat.getAverageMinutesPerTrip(), 2), stat.getTotalTrips())); // Agrega fila con nombre, unidades, utilización formateada, tiempo por viaje formateado y total de viajes
        }

        return sb.toString(); // Convierte StringBuilder a String y retorna la tabla completa
    }
}
