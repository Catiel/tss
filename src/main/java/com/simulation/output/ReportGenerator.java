package com.simulation.output; // Declaración del paquete donde se encuentra esta clase

import com.simulation.entities.EntityStatistics; // Importa la clase de estadísticas de entidades
import com.simulation.locations.LocationStatistics; // Importa la clase de estadísticas de ubicaciones
import com.simulation.statistics.StatisticsCollector; // Importa el recolector de estadísticas

import java.io.FileWriter; // Importa FileWriter para escribir archivos de texto
import java.io.IOException; // Importa la excepción de entrada/salida
import java.io.PrintWriter; // Importa PrintWriter para escribir texto formateado a archivos
import java.util.Map; // Importa la interfaz Map para colecciones clave-valor

public class ReportGenerator { // Clase que genera reportes de simulación en diferentes formatos
    private final StatisticsCollector statistics; // Recolector de estadísticas que contiene los datos a reportar

    public ReportGenerator(StatisticsCollector statistics) { // Constructor que inicializa el generador con un recolector de estadísticas
        this.statistics = statistics; // Asigna el recolector de estadísticas recibido
    }

    public void generateConsoleReport() { // Método para generar y mostrar el reporte en la consola
        System.out.println("\n" + "=".repeat(100)); // Imprime una línea separadora de 100 signos igual con salto de línea inicial
        System.out.println("REPORTE DE SIMULACIÓN - MODELO DE PRODUCCIÓN DE CERVEZA"); // Imprime el título del reporte
        System.out.println("=".repeat(100)); // Imprime otra línea separadora

        Map<String, EntityStatistics> entityStats = statistics.getEntityStats(); // Obtiene el mapa de estadísticas de entidades
        System.out.println(TableFormatter.formatEntityTable(entityStats)); // Formatea e imprime la tabla de estadísticas de entidades

        Map<String, LocationStatistics> locationStats = statistics.getLocationStats(); // Obtiene el mapa de estadísticas de ubicaciones
        System.out.println(TableFormatter.formatLocationTable(locationStats)); // Formatea e imprime la tabla de estadísticas de ubicaciones
    }

    public void generateFileReport(String filename) { // Método para generar el reporte y guardarlo en un archivo de texto
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea un PrintWriter con try-with-resources para cerrar automáticamente
            writer.println("REPORTE DE SIMULACIÓN - MODELO DE PRODUCCIÓN DE CERVEZA"); // Escribe el título del reporte en el archivo
            writer.println("=".repeat(100)); // Escribe una línea separadora en el archivo

            Map<String, EntityStatistics> entityStats = statistics.getEntityStats(); // Obtiene el mapa de estadísticas de entidades
            writer.println(TableFormatter.formatEntityTable(entityStats)); // Formatea y escribe la tabla de entidades en el archivo

            Map<String, LocationStatistics> locationStats = statistics.getLocationStats(); // Obtiene el mapa de estadísticas de ubicaciones
            writer.println(TableFormatter.formatLocationTable(locationStats)); // Formatea y escribe la tabla de ubicaciones en el archivo

            System.out.println("Reporte generado: " + filename); // Imprime mensaje de confirmación con el nombre del archivo
        } catch (IOException e) { // Captura excepciones de entrada/salida
            System.err.println("Error al generar reporte: " + e.getMessage()); // Imprime mensaje de error con la descripción de la excepción
        }
    }

    public void generateCSVReport(String entityFile, String locationFile) { // Método para generar reportes en formato CSV (dos archivos separados)
        generateEntityCSV(entityFile); // Genera el archivo CSV de entidades
        generateLocationCSV(locationFile); // Genera el archivo CSV de ubicaciones
    }

    private void generateEntityCSV(String filename) { // Método privado para generar el CSV de estadísticas de entidades
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea un PrintWriter con try-with-resources
            writer.println("Nombre,Total Salida,Tiempo En Sistema Promedio (Min)," + // Escribe la línea de encabezado del CSV con los nombres de las columnas
                         "Tiempo En lógica de movimiento Promedio (Min)," +
                         "Tiempo Espera Promedio (Min)," +
                         "Tiempo En Operación Promedio (Min)");

            for (EntityStatistics stat : statistics.getEntityStats().values()) { // Itera sobre todas las estadísticas de entidades
                writer.printf("%s,%d,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea con los datos de cada entidad en formato CSV
                    stat.getEntityName(), // Nombre de la entidad
                    stat.getTotalExits(), // Total de salidas
                    stat.getAverageSystemTime(), // Tiempo promedio en sistema
                    stat.getAverageNonValueAddedTime(), // Tiempo promedio sin valor agregado (movimiento)
                    stat.getAverageWaitTime(), // Tiempo promedio de espera
                    stat.getAverageValueAddedTime() // Tiempo promedio con valor agregado (operación)
                );
            }

            System.out.println("CSV de entidades generado: " + filename); // Imprime mensaje de confirmación
        } catch (IOException e) { // Captura excepciones de entrada/salida
            System.err.println("Error al generar CSV: " + e.getMessage()); // Imprime mensaje de error
        }
    }

    private void generateLocationCSV(String filename) { // Método privado para generar el CSV de estadísticas de ubicaciones
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea un PrintWriter con try-with-resources
            writer.println("Nombre,Tiempo Programado (Hr),Capacidad,Total Entradas," + // Escribe la línea de encabezado del CSV
                         "Tiempo Por entrada Promedio (Min),Contenido Promedio," +
                         "Contenido Máximo,Contenido Actual,% Utilización");

            for (LocationStatistics stat : statistics.getLocationStats().values()) { // Itera sobre todas las estadísticas de ubicaciones
                writer.printf("%s,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea con los datos de cada ubicación
                    stat.getLocationName(), // Nombre de la ubicación
                    stat.getScheduledTime() / 60.0, // Tiempo programado convertido de minutos a horas
                    stat.getCapacity(), // Capacidad de la ubicación
                    stat.getTotalEntries(), // Total de entradas
                    stat.getAverageTimePerEntry(), // Tiempo promedio por entrada
                    stat.getAverageContents(), // Contenido promedio
                    stat.getMaxContents(), // Contenido máximo
                    stat.getCurrentContents(), // Contenido actual
                    stat.getUtilizationPercent() // Porcentaje de utilización
                );
            }

            System.out.println("CSV de locaciones generado: " + filename); // Imprime mensaje de confirmación
        } catch (IOException e) { // Captura excepciones de entrada/salida
            System.err.println("Error al generar CSV: " + e.getMessage()); // Imprime mensaje de error
        }
    }
}
