package com.simulation.output; // Declaración del paquete de salida y reportes de la simulación

import com.simulation.entities.EntityStatistics; // Importa la clase de estadísticas de entidades
import com.simulation.locations.LocationStatistics; // Importa la clase de estadísticas de ubicaciones
import com.simulation.statistics.StatisticsCollector; // Importa el recolector principal de estadísticas

import java.io.FileWriter; // Importa la clase para escribir archivos de texto
import java.io.IOException; // Importa la clase de excepciones de entrada/salida
import java.io.PrintWriter; // Importa la clase para escribir texto formateado en archivos
import java.util.Map; // Importa la interfaz Map para manejo de mapas clave-valor

public class ReportGenerator { // Define la clase generadora de reportes de simulación
    private final StatisticsCollector statistics; // Recolector de estadísticas que provee los datos para los reportes

    public ReportGenerator(StatisticsCollector statistics) { // Constructor que recibe el recolector de estadísticas
        this.statistics = statistics; // Asigna el recolector recibido a la variable de instancia
    }

    public void generateConsoleReport() { // Método que genera y muestra el reporte completo en consola
        System.out.println("\n" + "=".repeat(100)); // Imprime salto de línea seguido de separador de 100 caracteres
        System.out.println("REPORTE DE SIMULACION - MODELO DE PRODUCCION DE CERVEZA"); // Imprime el título principal del reporte
        System.out.println("=".repeat(100)); // Imprime separador inferior del título

        Map<String, EntityStatistics> entityStats = statistics.getEntityStats(); // Obtiene el mapa de estadísticas de entidades del recolector
        System.out.println(TableFormatter.formatEntityTable(entityStats)); // Formatea e imprime la tabla de estadísticas de entidades

        Map<String, LocationStatistics> locationStats = statistics.getLocationStats(); // Obtiene el mapa de estadísticas de ubicaciones del recolector
        System.out.println(TableFormatter.formatLocationTable(locationStats)); // Formatea e imprime la tabla de estadísticas de ubicaciones

        Map<String, com.simulation.resources.ResourceStatistics> resourceStats = statistics.getResourceStats(); // Obtiene el mapa de estadísticas de recursos del recolector
        System.out.println(TableFormatter.formatResourceTable(resourceStats)); // Formatea e imprime la tabla de estadísticas de recursos
    }

    public void generateFileReport(String filename) { // Método que genera el reporte completo en un archivo de texto
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea PrintWriter con try-with-resources para cerrar automáticamente
            writer.println("REPORTE DE SIMULACIÓN - MODELO DE PRODUCCIÓN DE CERVEZA"); // Escribe el título del reporte en el archivo
            writer.println("=".repeat(100)); // Escribe línea separadora en el archivo

            Map<String, EntityStatistics> entityStats = statistics.getEntityStats(); // Obtiene el mapa de estadísticas de entidades
            writer.println(TableFormatter.formatEntityTable(entityStats)); // Escribe la tabla formateada de entidades en el archivo

            Map<String, LocationStatistics> locationStats = statistics.getLocationStats(); // Obtiene el mapa de estadísticas de ubicaciones
            writer.println(TableFormatter.formatLocationTable(locationStats)); // Escribe la tabla formateada de ubicaciones en el archivo

            Map<String, com.simulation.resources.ResourceStatistics> resourceStats = statistics.getResourceStats(); // Obtiene el mapa de estadísticas de recursos
            writer.println(TableFormatter.formatResourceTable(resourceStats)); // Escribe la tabla formateada de recursos en el archivo

            System.out.println("Reporte generado: " + filename); // Imprime mensaje de confirmación con el nombre del archivo creado
        } catch (IOException e) { // Captura excepciones de entrada/salida durante la escritura del archivo
            System.err.println("Error al generar reporte: " + e.getMessage()); // Imprime mensaje de error en la salida de errores estándar
        }
    }

    public void generateCSVReport(String entityFile, String locationFile) { // Método público para generar dos archivos CSV separados
        generateEntityCSV(entityFile); // Llama al método que genera el CSV de entidades
        generateLocationCSV(locationFile); // Llama al método que genera el CSV de ubicaciones
    }

    private void generateEntityCSV(String filename) { // Método privado que genera archivo CSV con estadísticas de entidades
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea PrintWriter con try-with-resources para el archivo CSV
            writer.println("Nombre,Total Salida,Tiempo En Sistema Promedio (Min)," + // Escribe la primera parte del encabezado CSV
                    "Tiempo En lógica de movimiento Promedio (Min)," + // Escribe la segunda parte del encabezado
                    "Tiempo Espera Promedio (Min)," + // Escribe la tercera parte del encabezado
                    "Tiempo En Operación Promedio (Min)"); // Escribe la última parte del encabezado

            for (EntityStatistics stat : statistics.getEntityStats().values()) { // Itera sobre cada estadística de entidad en la colección
                writer.printf("%s,%d,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea CSV con formato de 6 columnas
                        stat.getEntityName(), // Columna 1: Nombre de la entidad
                        stat.getTotalExits(), // Columna 2: Total de salidas del sistema
                        stat.getAverageSystemTime(), // Columna 3: Tiempo promedio total en sistema
                        stat.getAverageNonValueAddedTime(), // Columna 4: Tiempo promedio en movimiento sin valor
                        stat.getAverageWaitTime(), // Columna 5: Tiempo promedio esperando
                        stat.getAverageValueAddedTime() // Columna 6: Tiempo promedio en operaciones productivas
                );
            }

            System.out.println("CSV de entidades generado: " + filename); // Imprime confirmación con nombre del archivo CSV creado
        } catch (IOException e) { // Captura excepciones de entrada/salida durante escritura CSV
            System.err.println("Error al generar CSV: " + e.getMessage()); // Imprime mensaje de error con descripción de la excepción
        }
    }

    private void generateLocationCSV(String filename) { // Método privado que genera archivo CSV con estadísticas de ubicaciones
        try (PrintWriter writer = new PrintWriter(new FileWriter(filename))) { // Crea PrintWriter con try-with-resources para el archivo CSV
            writer.println("Nombre,Tiempo Programado (Hr),Capacidad,Total Entradas," + // Escribe la primera parte del encabezado CSV
                    "Tiempo Por entrada Promedio (Min),Contenido Promedio," + // Escribe la segunda parte del encabezado
                    "Contenido Máximo,Contenido Actual,% Utilización"); // Escribe la última parte del encabezado

            for (LocationStatistics stat : statistics.getLocationStats().values()) { // Itera sobre cada estadística de ubicación en la colección
                writer.printf("%s,%.2f,%d,%d,%.2f,%.2f,%.2f,%.2f,%.2f\n", // Escribe una línea CSV con formato de 9 columnas
                        stat.getLocationName(), // Columna 1: Nombre de la ubicación
                        stat.getScheduledTime() / 60.0, // Columna 2: Tiempo programado convertido a horas
                        stat.getCapacity(), // Columna 3: Capacidad máxima de la ubicación
                        stat.getTotalEntries(), // Columna 4: Número total de entradas registradas
                        stat.getAverageTimePerEntry(), // Columna 5: Tiempo promedio de permanencia por entrada
                        stat.getAverageContents(), // Columna 6: Contenido promedio de entidades
                        stat.getMaxContents(), // Columna 7: Contenido máximo permitido
                        stat.getCurrentContents(), // Columna 8: Contenido actual al final de la simulación
                        stat.getUtilizationPercent() // Columna 9: Porcentaje de utilización de la capacidad
                );
            }

            System.out.println("CSV de locaciones generado: " + filename); // Imprime confirmación con nombre del archivo CSV de ubicaciones
        } catch (IOException e) { // Captura excepciones de entrada/salida durante escritura CSV
            System.err.println("Error al generar CSV: " + e.getMessage()); // Imprime mensaje de error con descripción de la excepción
        }
    }
}
