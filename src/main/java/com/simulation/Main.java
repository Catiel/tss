package com.simulation;

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.output.ReportGenerator;
import com.simulation.processing.BatchProcessingRule;
import com.simulation.processing.ProcessingRule;
import com.simulation.statistics.StatisticsCollector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class Main {
    public static void main(String[] args) {
        System.out.println("=============================================================");
        System.out.println("  SIMULACIÓN ENGRANES DE ACERO SA");
        System.out.println("  Celda Flexible de Manufactura de Piezas Automotrices");
        System.out.println("=============================================================\n");
        System.out.println("DEBUG: Main with calibrated parameters running (Horno 118.0, Rectificado 3.1)");

        // Configurar parámetros de simulación
        int numReplicas = 3;
        double simulationTime = 1000.0 * 60.0; // 60,000 minutos

        List<StatisticsCollector> replicaStatistics = new ArrayList<>();

        for (int replica = 1; replica <= numReplicas; replica++) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  RÉPLICA " + replica + " de " + numReplicas);
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            SimulationEngine engine = new SimulationEngine();
            setupEntityTypes(engine);
            setupLocations(engine);
            setupResources(engine);
            setupProcessingRules(engine);
            setupArrivals(engine);

            System.out.println("Ejecutando simulación por " + (simulationTime / 60.0) + " horas (" + simulationTime
                    + " minutos)...\n");
            engine.run(simulationTime);

            replicaStatistics.add(engine.getStatistics());

            System.out.println("\n--- Resultados de Réplica " + replica + " ---");
            ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics());
            reportGenerator.generateConsoleReport();
            reportGenerator.generateFileReport("reporte_replica_" + replica + ".txt");
        }

        generateConsolidatedReport(replicaStatistics);
    }

    private static void setupEntityTypes(SimulationEngine engine) {
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0);
    }

    private static void setupLocations(SimulationEngine engine) {
        engine.addLocation("ALMACEN_MP", 999999, 1);
        engine.addLocation("HORNO", 10, 1);
        engine.addLocation("BANDA_1", 999999, 1);
        engine.addLocation("CARGA", 999999, 1);
        engine.addLocation("TORNEADO", 1, 1);
        engine.addLocation("FRESADO", 1, 1);
        engine.addLocation("TALADRO", 1, 1);
        engine.addLocation("RECTIFICADO", 1, 1);
        engine.addLocation("DESCARGA", 999999, 1);
        engine.addLocation("BANDA_2", 999999, 1);
        engine.addLocation("INSPECCION", 1, 1);
        engine.addLocation("SALIDA", 999999, 1);
    }

    private static void setupResources(SimulationEngine engine) {
        engine.addResource("GRUA_VIAJERA", 1, 25.0);
        engine.addResource("ROBOT", 1, 45.0);
    }

    private static void setupProcessingRules(SimulationEngine engine) {
        // 1. ALMACEN_MP
        engine.addProcessingRule(new ProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 2. HORNO: 118.0 min, Batch 10
        engine.addProcessingRule(new BatchProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 118.0, 10));

        // 3. BANDA_1: 0.94 min
        engine.addProcessingRule(new ProcessingRule("BANDA_1", "PIEZA_AUTOMOTRIZ", 0.94) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 4. CARGA: 0.5 min
        engine.addProcessingRule(new ProcessingRule("CARGA", "PIEZA_AUTOMOTRIZ", 0.5) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 5. TORNEADO: 9.37 min
        engine.addProcessingRule(new ProcessingRule("TORNEADO", "PIEZA_AUTOMOTRIZ", 9.37) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 6. FRESADO: 10.18 min
        engine.addProcessingRule(new ProcessingRule("FRESADO", "PIEZA_AUTOMOTRIZ", 10.18) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 7. TALADRO: 2.66 min
        engine.addProcessingRule(new ProcessingRule("TALADRO", "PIEZA_AUTOMOTRIZ", 2.66) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 8. RECTIFICADO: 3.02 min
        engine.addProcessingRule(new ProcessingRule("RECTIFICADO", "PIEZA_AUTOMOTRIZ", 3.02) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 9. DESCARGA: 0.5 min
        engine.addProcessingRule(new ProcessingRule("DESCARGA", "PIEZA_AUTOMOTRIZ", 0.5) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 10. BANDA_2: 1.02 min
        engine.addProcessingRule(new ProcessingRule("BANDA_2", "PIEZA_AUTOMOTRIZ", 1.02) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 11. INSPECCION: 2.99 min
        engine.addProcessingRule(new ProcessingRule("INSPECCION", "PIEZA_AUTOMOTRIZ", 2.99) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });

        // 12. SALIDA
        engine.addProcessingRule(new ProcessingRule("SALIDA", "PIEZA_AUTOMOTRIZ", 0) {
            @Override
            public void process(Entity e, SimulationEngine en) {
            }
        });
    }

    private static void setupArrivals(SimulationEngine engine) {
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 0, 12000, 5.0);
    }

    private static void generateConsolidatedReport(List<StatisticsCollector> replicaStatistics) {
        System.out.println("Análisis de " + replicaStatistics.size() + " réplicas completado.");
    }
}
