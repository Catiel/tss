package com.simulation.output; // Declaración del paquete donde se encuentra esta clase

import com.simulation.entities.EntityStatistics;
import com.simulation.locations.LocationStatistics;
import com.simulation.statistics.StatisticsCollector;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

public class ReportGenerator { // Clase que genera reportes de simulación en diferentes formatos
    private final StatisticsCollector statistics; // Recolector de estadísticas que contiene los datos a reportar

    public ReportGenerator(StatisticsCollector statistics) { // Constructor que inicializa el generador con un
                                                             // recolector de estadísticas
        this.statistics = statistics; // Asigna el recolector de estadísticas recibido
    }

    public void generateConsoleReport() { // Método para generar y mostrar el reporte en la consola
        System.out.println("\n" + "=".repeat(100)); // Imprime una línea separadora de 100 signos igual con salto de
                                                    // línea inicial
        System.out.println("REPORTE DE SIMULACION - MODELO DE PRODUCCION DE CERVEZA"); // Imprime el título del reporte
        System.out.println("=".repeat(100)); // Imprime otra línea separadora

        Map<String, EntityStatistics> entityStats = statistics.getEntityStats(); // Obtiene el mapa de estadísticas de
                                                                                 // entidades
        System.out.println(TableFormatter.formatEntityTable(entityStats)); // Formatea e imprime la tabla de
                                                                           // estadísticas de entidades

        Map<String, LocationStatistics> locationStats = statistics.getLocationStats();
        System.out.println(TableFormatter.formatLocationTable(locationStats));

        Map<String, com.simulation.resources.ResourceStatistics> resourceStats = statistics.getResourceStats();
        System.out.println(TableFormatter.formatResourceTable(resourceStats));
    }

    public void generateFileReport(String filename) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) {
            writer.println("REPORTE DE SIMULACIÓN - MODELO DE PRODUCCIÓN DE CERVEZA");
            writer.println("=".repeat(100));

            Map<String, EntityStatistics> entityStats = statistics.getEntityStats();
            writer.println(TableFormatter.formatEntityTable(entityStats));

            Map<String, LocationStatistics> locationStats = statistics.getLocationStats();
            writer.println(TableFormatter.formatLocationTable(locationStats));

            Map<String, com.simulation.resources.ResourceStatistics> resourceStats = statistics.getResourceStats();
            writer.println(TableFormatter.formatResourceTable(resourceStats));

            System.out.println("Reporte generado: " + filename); // Imprime mensaje de confirmación con el nombre del
                                                                 // archivo
        } catch (IOException e) { // Captura excepciones de entrada/salida
            System.err.println("Error al generar reporte: " + e.getMessage()); // Imprime mensaje de error con la
                                                                               // descripción de la excepción
        }
    }

    public void generateCSVReport(String entityFile, String locationFile) { // Método para generar reportes en formato
                                                                            // CSV (dos archivos separados)
        generateEntityCSV(entityFile); // Genera el archivo CSV de entidades
        generateLocationCSV(locationFile); // Genera el archivo CSV de ubicaciones
    }

    private void generateEntityCSV(String filename) { // Método privado para generar el CSV de estadísticas de entidades
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea un PrintWriter con
                                                                               // try-with-resources
            writer.println("Nombre,Total Salida,Tiempo En Sistema Promedio (Min)," + // Escribe la línea de encabezado
                                                                                     // del CSV con los nombres de las
                                                                                     // columnas
                    "Tiempo En lógica de movimiento Promedio (Min)," +
                    "Tiempo Espera Promedio (Min)," +
                    "Tiempo En Operación Promedio (Min)");

            for (EntityStatistics stat : statistics.getEntityStats().values()) { // Itera sobre todas las estadísticas
                                                                                 // de entidades
                writer.printf("%s,%d,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea con los datos de cada entidad en
                                                             // formato CSV
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

    private void generateLocationCSV(String filename) { // Método privado para generar el CSV de estadísticas de
                                                        // ubicaciones
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea un PrintWriter con
                                                                               // try-with-resources
            writer.println("Nombre,Tiempo Programado (Hr),Capacidad,Total Entradas," + // Escribe la línea de encabezado
                                                                                       // del CSV
                    "Tiempo Por entrada Promedio (Min),Contenido Promedio," +
                    "Contenido Máximo,Contenido Actual,% Utilización");

            for (LocationStatistics stat : statistics.getLocationStats().values()) { // Itera sobre todas las
                                                                                     // estadísticas de ubicaciones
                writer.printf("%s,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea con los datos de cada
                                                                          // ubicación
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
