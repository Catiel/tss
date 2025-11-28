package com.simulation;

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.output.ReportGenerator;
import com.simulation.processing.ProcessingRule;

public class MainTest {
    public static void main(String[] args) {
        System.out.println("============== PRUEBA CORTA ==============");
        System.out.println("Simulación de 100 minutos para depuración\n");

        SimulationEngine engine = new SimulationEngine();

        // Configurar
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0);

        engine.addLocation("ALMACEN_MP", Integer.MAX_VALUE, 1);
        engine.addLocation("HORNO", 10, 1);
        engine.addLocation("SALIDA", Integer.MAX_VALUE, 1);

        engine.addResource("GRUA_VIAJERA", 1, 25.0);

        // Reglas simples
        engine.addProcessingRule(new SimpleProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0));
        engine.addProcessingRule(new SimpleProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 10)); // Reducido a 10 min
        engine.addProcessingRule(new SimpleProcessingRule("SALIDA", "PIEZA_AUTOMOTRIZ", 0));

        // Arribos: 10 piezas, cada 5 minutos
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 0, 20, 5.0);

        // Ejecutar 100 minutos
        System.out.println("Ejecutando simulación por 100 minutos...\n");
        engine.run(100.0);

        // Reporte
        ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics());
        reportGenerator.generateConsoleReport();

        System.out.println("\n¡Prueba completada!");
    }

    private static class SimpleProcessingRule extends ProcessingRule {
        public SimpleProcessingRule(String locationName, String entityTypeName, double processingTime) {
            super(locationName, entityTypeName, processingTime);
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
        }
    }
}
