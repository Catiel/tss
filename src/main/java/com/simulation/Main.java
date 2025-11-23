package com.simulation;

import com.simulation.core.SimulationEngine;
import com.simulation.output.ReportGenerator;
import com.simulation.processing.ProcessingRule;
import com.simulation.entities.Entity;

public class Main {
    public static void main(String[] args) {
        System.out.println("Iniciando simulación del modelo de producción de cerveza...\n");

        // Crear motor de simulación
        SimulationEngine engine = new SimulationEngine();

        // Configurar tipos de entidades
        setupEntityTypes(engine);

        // Configurar locaciones
        setupLocations(engine);

        // Configurar recursos
        setupResources(engine);

        // Configurar reglas de procesamiento
        setupProcessingRules(engine);

        // Configurar arribos
        setupArrivals(engine);

        // Ejecutar simulación: 70 horas = 4,200 minutos
        double simulationTime = 70.0 * 60.0; // 4,200 minutos

        System.out.println("Ejecutando simulación por " + simulationTime + " minutos (70 horas)...\n");
        engine.run(simulationTime);

        // Generar reportes
        ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics());
        reportGenerator.generateConsoleReport();
        reportGenerator.generateFileReport("reporte_simulacion.txt");
        reportGenerator.generateCSVReport("entidades_reporte.csv", "locaciones_reporte.csv");

        System.out.println("\n¡Simulación completada exitosamente!");
    }

    private static void setupEntityTypes(SimulationEngine engine) {
        engine.addEntityType("GRANOS_DE_CEBADA", 150.0);
        engine.addEntityType("LUPULO", 150.0);
        engine.addEntityType("LEVADURA", 150.0);
        engine.addEntityType("MOSTO", 150.0);
        engine.addEntityType("CERVEZA", 150.0);
        engine.addEntityType("BOTELLA_CON_CERVEZA", 150.0);
        engine.addEntityType("CAJA_VACIA", 150.0);
        engine.addEntityType("CAJA_CON_CERVEZAS", 150.0);
    }

    private static void setupLocations(SimulationEngine engine) {
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

    private static void setupResources(SimulationEngine engine) {
        engine.addResource("OPERADOR_RECEPCION", 1, 90.0);
        engine.addResource("OPERADOR_LUPULO", 1, 100.0);
        engine.addResource("OPERADOR_LEVADURA", 1, 100.0);
        engine.addResource("OPERADOR_EMPACADO", 1, 100.0);
        engine.addResource("CAMION", 1, 100.0);
    }

    private static void setupProcessingRules(SimulationEngine engine) {
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

    private static void setupArrivals(SimulationEngine engine) {
        // 70 horas = 4200 minutos
        // Ajustado para coincidir con ProModel (arrivals target basado en entradas a locaciones)
        
        // GRANOS_DE_CEBADA: target ~124 arrivals (ProModel: SILO_GRANDE 124 entradas)
        // Frecuencia: 4200/124 = 33.87 min
        engine.scheduleArrival("GRANOS_DE_CEBADA", "SILO_GRANDE", 0, 124, 33.87);

        // LUPULO: target ~400 arrivals (ProModel: SILO_LUPULO 400 entradas)
        // Frecuencia: 4200/400 = 10.5 min
        engine.scheduleArrival("LUPULO", "SILO_LUPULO", 0, 400, 10.5);

        // LEVADURA: target ~190 arrivals (ProModel: SILO_LEVADURA 190 entradas)
        // Frecuencia: 4200/190 = 22.11 min
        engine.scheduleArrival("LEVADURA", "SILO_LEVADURA", 0, 190, 22.11);

        // CAJA_VACIA: target ~114 arrivals (ProModel: ALMACEN_CAJAS 114 entradas)
        // Frecuencia: 4200/114 = 36.84 min
        engine.scheduleArrival("CAJA_VACIA", "ALMACEN_CAJAS", 0, 114, 36.84);
    }

    // Clase interna para reglas de procesamiento simples
    private static class SimpleProcessingRule extends ProcessingRule {
        public SimpleProcessingRule(String locationName, String entityTypeName, double processingTime) {
            super(locationName, entityTypeName, processingTime);
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            // Implementación básica
        }
    }
}
