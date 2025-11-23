package com.simulation; // Paquete principal

import com.simulation.core.SimulationEngine; // Importa la clase SimulationEngine
import com.simulation.entities.Entity; // Importa la clase Entity
import com.simulation.output.ReportGenerator; // Importa la clase para generar reportes
import com.simulation.processing.ProcessingRule; // Importa la clase ProcessingRule

public class Main { // Clase principal
    public static void main(String[] args) { // Método principal
        System.out.println("Iniciando simulación del modelo de producción de cerveza...\n"); // Mensaje inicial

        // Crear motor de simulación
        SimulationEngine engine = new SimulationEngine(); // Instancia del motor de simulación

        // Configurar tipos de entidades
        setupEntityTypes(engine); // Configura tipos de entidad

        // Configurar locaciones
        setupLocations(engine); // Configura locaciones

        // Configurar recursos
        setupResources(engine); // Configura recursos

        // Configurar reglas de procesamiento
        setupProcessingRules(engine); // Configura reglas de procesamiento

        // Configurar arribos
        setupArrivals(engine); // Configura las llegadas de entidades

        // Ejecutar simulación: 70 horas = 4200 minutos
        double simulationTime = 70.0 * 60.0; // Tiempo total de simulación en minutos

        System.out.println("Ejecutando simulación por " + simulationTime + " minutos (70 horas)...\n"); // Mensaje de inicio de ejecución
        engine.run(simulationTime); // Ejecuta la simulación

        // Generar reportes
        ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics()); // Instancia de generador
        reportGenerator.generateConsoleReport(); // Reporte en consola
        reportGenerator.generateFileReport("reporte_simulacion.txt"); // Archivo de texto
        reportGenerator.generateCSVReport("entidades_reporte.csv", "locaciones_reporte.csv"); // Reportes CSV

        System.out.println("\n¡Simulación completada exitosamente!"); // Fin
    }

    private static void setupEntityTypes(SimulationEngine engine) { // Configuración tipos de entidades
        engine.addEntityType("GRANOS_DE_CEBADA", 150.0); // Tipo granos de cebada
        engine.addEntityType("LUPULO", 150.0); // Tipo lúpulo
        engine.addEntityType("LEVADURA", 150.0); // Tipo levadura
        engine.addEntityType("MOSTO", 150.0); // Tipo mosto
        engine.addEntityType("CERVEZA", 150.0); // Tipo cerveza
        engine.addEntityType("BOTELLA_CON_CERVEZA", 150.0); // Tipo botella con cerveza
        engine.addEntityType("CAJA_VACIA", 150.0); // Tipo caja vacía
        engine.addEntityType("CAJA_CON_CERVEZAS", 150.0); // Tipo caja con cervezas
    }

    private static void setupLocations(SimulationEngine engine) { // Configuración locaciones
        engine.addLocation("SILO_GRANDE", 3, 1);
        engine.addLocation("MALTEADO", 3, 1);
        engine.addLocation("SECADO", 3, 1);
        engine.addLocation("MOLIENDA", 2, 1);
        engine.addLocation("MACERADO", 3, 1);
        engine.addLocation("FILTRADO", 2, 1);
        engine.addLocation("COCCION", 10, 1);
        engine.addLocation("ALMACEN_CAJAS", 30, 1);
        engine.addLocation("SILO_LUPULO", 10, 1);
        engine.addLocation("ENFRIAMIENTO", 10, 1);
        engine.addLocation("EMPACADO", 1, 1);
        engine.addLocation("ETIQUETADO", 6, 1);
        engine.addLocation("EMBOTELLADO", 6, 1);
        engine.addLocation("INSPECCION", 3, 1);
        engine.addLocation("MADURACION", 10, 1);
        engine.addLocation("FERMENTACION", 10, 1);
        engine.addLocation("SILO_LEVADURA", 10, 1);
        engine.addLocation("ALMACENAJE", 6, 1);
        engine.addLocation("MERCADO", Integer.MAX_VALUE, 1);
    }

    private static void setupResources(SimulationEngine engine) { // Configuración recursos
        engine.addResource("OPERADOR_RECEPCION", 1, 90.0);
        engine.addResource("OPERADOR_LUPULO", 1, 100.0);
        engine.addResource("OPERADOR_LEVADURA", 1, 100.0);
        engine.addResource("OPERADOR_EMPACADO", 1, 100.0);
        engine.addResource("CAMION", 1, 100.0);
    }

    private static void setupProcessingRules(SimulationEngine engine) { // Reglas de proceso
        // GRANOS_DE_CEBADA
        engine.addProcessingRule(new SimpleProcessingRule("SILO_GRANDE", "GRANOS_DE_CEBADA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("MALTEADO", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("SECADO", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("MOLIENDA", "GRANOS_DE_CEBADA", 60));
        engine.addProcessingRule(new SimpleProcessingRule("MACERADO", "GRANOS_DE_CEBADA", 90));
        engine.addProcessingRule(new SimpleProcessingRule("FILTRADO", "GRANOS_DE_CEBADA", 30));
        // LUPULO
        engine.addProcessingRule(new SimpleProcessingRule("SILO_LUPULO", "LUPULO", 0));
        engine.addProcessingRule(new SimpleProcessingRule("COCCION", "LUPULO", 0)); // JOIN
        // COCCION (transformación a MOSTO)
        engine.addProcessingRule(new SimpleProcessingRule("COCCION", "GRANOS_DE_CEBADA", 60));
        // LEVADURA
        engine.addProcessingRule(new SimpleProcessingRule("SILO_LEVADURA", "LEVADURA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("FERMENTACION", "LEVADURA", 0)); // JOIN
        // MOSTO
        engine.addProcessingRule(new SimpleProcessingRule("ENFRIAMIENTO", "MOSTO", 60));
        engine.addProcessingRule(new SimpleProcessingRule("FERMENTACION", "MOSTO", 120));
        // CERVEZA
        engine.addProcessingRule(new SimpleProcessingRule("MADURACION", "CERVEZA", 90));
        engine.addProcessingRule(new SimpleProcessingRule("INSPECCION", "CERVEZA", 30));
        engine.addProcessingRule(new SimpleProcessingRule("EMBOTELLADO", "CERVEZA", 3));
        // BOTELLAS
        engine.addProcessingRule(new SimpleProcessingRule("ETIQUETADO", "BOTELLA_CON_CERVEZA", 1));
        engine.addProcessingRule(new SimpleProcessingRule("EMPACADO", "BOTELLA_CON_CERVEZA", 0)); // JOIN 6
        // CAJAS
        engine.addProcessingRule(new SimpleProcessingRule("ALMACEN_CAJAS", "CAJA_VACIA", 0));
        engine.addProcessingRule(new SimpleProcessingRule("EMPACADO", "CAJA_VACIA", 0)); // WAIT 10 está después del JOIN
        engine.addProcessingRule(new SimpleProcessingRule("ALMACENAJE", "CAJA_CON_CERVEZAS", 5));
        engine.addProcessingRule(new SimpleProcessingRule("MERCADO", "CAJA_CON_CERVEZAS", 0));
    }

    private static void setupArrivals(SimulationEngine engine) { // Configuración de arribos
        // 70 horas = 4200 minutos
        // Target aproximado basado en entradas de ProModel
        engine.scheduleArrival("GRANOS_DE_CEBADA", "SILO_GRANDE", 0, 124, 33.87);
        engine.scheduleArrival("LUPULO", "SILO_LUPULO", 0, 400, 10.5);
        engine.scheduleArrival("LEVADURA", "SILO_LEVADURA", 0, 190, 22.11);
        engine.scheduleArrival("CAJA_VACIA", "ALMACEN_CAJAS", 0, 114, 36.84);
    }

    // Clase interna para reglas sencillas
    private static class SimpleProcessingRule extends ProcessingRule { // Extiende ProcessingRule
        public SimpleProcessingRule(String locationName, String entityTypeName, double processingTime) { // Constructor
            super(locationName, entityTypeName, processingTime); // Llama al constructor padre
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            // Implementación sencilla, se puede personalizar
        }
    }
}
