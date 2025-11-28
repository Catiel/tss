package com.simulation; // Paquete principal

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.output.ReportGenerator;
import com.simulation.processing.ProcessingRule;
import com.simulation.statistics.StatisticsCollector;

import java.util.ArrayList;
import java.util.List;

public class Main { // Clase principal
    public static void main(String[] args) { // Método principal
        System.out.println("=============================================================");
        System.out.println("  SIMULACIÓN ENGRANES DE ACERO SA");
        System.out.println("  Celda Flexible de Manufactura de Piezas Automotrices");
        System.out.println("=============================================================\n");

        // Configurar parámetros de simulación
        int numReplicas = 3; // Número de réplicas
        double simulationTime = 1000.0 * 60.0; // 1000 horas = 60,000 minutos por réplica

        // Lista para almacenar estadísticas de todas las réplicas
        List<StatisticsCollector> replicaStatistics = new ArrayList<>();

        // Ejecutar réplicas
        for (int replica = 1; replica <= numReplicas; replica++) {
            System.out.println("\n╔════════════════════════════════════════════════════════════╗");
            System.out.println("║  RÉPLICA " + replica + " de " + numReplicas);
            System.out.println("╚════════════════════════════════════════════════════════════╝\n");

            // Crear motor de simulación para esta réplica
            SimulationEngine engine = new SimulationEngine();

            // Configurar el modelo
            setupEntityTypes(engine);
            setupLocations(engine);
            setupResources(engine);
            setupProcessingRules(engine);
            setupArrivals(engine);

            // Ejecutar simulación
            System.out.println("Ejecutando simulación por " + (simulationTime / 60.0) + " horas (" + simulationTime
                    + " minutos)...\n");
            engine.run(simulationTime);

            // Almacenar estadísticas de esta réplica
            replicaStatistics.add(engine.getStatistics());

            // Generar reporte de esta réplica
            System.out.println("\n--- Resultados de Réplica " + replica + " ---");
            ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics());
            reportGenerator.generateConsoleReport();
            reportGenerator.generateFileReport("reporte_replica_" + replica + ".txt");
            reportGenerator.generateCSVReport("entidades_replica_" + replica + ".csv",
                    "locaciones_replica_" + replica + ".csv");
        }

        // Generar reporte consolidado
        System.out.println("\n\n╔════════════════════════════════════════════════════════════╗");
        System.out.println("║  REPORTE CONSOLIDADO - TODAS LAS RÉPLICAS");
        System.out.println("╚════════════════════════════════════════════════════════════╝\n");
        generateConsolidatedReport(replicaStatistics);

        System.out.println("\n¡Simulación completada exitosamente!");
        System.out.println("Total de réplicas ejecutadas: " + numReplicas);
        System.out.println("Tiempo simulado por réplica: " + (simulationTime / 60.0) + " horas");
        System.out.println("Tiempo total simulado: " + (simulationTime * numReplicas / 60.0) + " horas\n");
    }

    private static void setupEntityTypes(SimulationEngine engine) {
        // Única entidad: piezas automotrices que fluyen por el sistema
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0); // Velocidad estándar
    }

    private static void setupLocations(SimulationEngine engine) {
        // Secuencia de locaciones en la celda de manufactura
        engine.addLocation("ALMACEN_MP", Integer.MAX_VALUE, 1); // Almacén de materia prima (capacidad infinita)
        engine.addLocation("HORNO", 10, 1); // Capacidad de 10 piezas (procesamiento por lotes)
        engine.addLocation("BANDA_1", Integer.MAX_VALUE, 1); // Banda transportadora 1 (capacidad infinita)
        engine.addLocation("CARGA", Integer.MAX_VALUE, 1); // Estación de carga (capacidad infinita)
        engine.addLocation("TORNEADO", 1, 1); // 1 máquina de torneado
        engine.addLocation("FRESADO", 1, 1); // 1 máquina de fresado
        engine.addLocation("TALADRO", 1, 1); // 1 máquina de taladrado (TALADRO en ProModel)
        engine.addLocation("RECTIFICADO", 1, 1); // 1 máquina de rectificado
        engine.addLocation("DESCARGA", Integer.MAX_VALUE, 1); // Estación de descarga (capacidad infinita)
        engine.addLocation("BANDA_2", Integer.MAX_VALUE, 1); // Banda transportadora 2 (capacidad infinita)
        engine.addLocation("INSPECCION", 1, 1); // 1 estación de inspección
        engine.addLocation("SALIDA", Integer.MAX_VALUE, 1); // Punto de salida del sistema
    }

    private static void setupResources(SimulationEngine engine) {
        // Recursos de manejo de materiales
        engine.addResource("GRUA_VIAJERA", 1, 25.0); // 1 grúa viajera a 25 pies/min
        engine.addResource("ROBOT", 1, 45.0); // 1 robot a 45 pies/min
        // Las bandas transportadoras se modelan como locaciones, no como recursos
    }

    private static void setupProcessingRules(SimulationEngine engine) {
        // Flujo de proceso para PIEZA_AUTOMOTRIZ

        // 1. ALMACEN_MP: Sin procesamiento, solo recepción
        engine.addProcessingRule(new SimpleProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0));

        // 2. HORNO: Procesamiento por lotes (ACCUM 10), 100 minutos
        engine.addProcessingRule(new BatchProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 100.0, 10));

        // 3. BANDA_1: Transporte se maneja en el routing (MOVE FOR 1 min)
        engine.addProcessingRule(new SimpleProcessingRule("BANDA_1", "PIEZA_AUTOMOTRIZ", 0.0));

        // 4. CARGA: 0.5 minutos
        engine.addProcessingRule(new SimpleProcessingRule("CARGA", "PIEZA_AUTOMOTRIZ", 0.5));

        // 5. TORNEADO: 9.37 minutos (Ajustado a ProModel)
        engine.addProcessingRule(new SimpleProcessingRule("TORNEADO", "PIEZA_AUTOMOTRIZ", 9.37));

        // 6. FRESADO: 10.18 minutos (Ajustado a ProModel)
        engine.addProcessingRule(new SimpleProcessingRule("FRESADO", "PIEZA_AUTOMOTRIZ", 10.18));

        // 7. TALADRO: 2.66 minutos (Ajustado a ProModel)
        engine.addProcessingRule(new SimpleProcessingRule("TALADRO", "PIEZA_AUTOMOTRIZ", 2.66));

        // 8. RECTIFICADO: 3.02 minutos (Ajustado a ProModel)
        engine.addProcessingRule(new SimpleProcessingRule("RECTIFICADO", "PIEZA_AUTOMOTRIZ", 3.02));

        // 9. DESCARGA: 0.5 minutos
        engine.addProcessingRule(new SimpleProcessingRule("DESCARGA", "PIEZA_AUTOMOTRIZ", 0.5));

        // 10. BANDA_2: Transporte se maneja en el routing (MOVE FOR 1 min)
        engine.addProcessingRule(new SimpleProcessingRule("BANDA_2", "PIEZA_AUTOMOTRIZ", 0.0));

        // 11. INSPECCION: Exponencial(3) minutos
        engine.addProcessingRule(new ExponentialProcessingRule("INSPECCION", "PIEZA_AUTOMOTRIZ", 3.0));

        // 12. SALIDA: Sin procesamiento (EXIT)
        engine.addProcessingRule(new SimpleProcessingRule("SALIDA", "PIEZA_AUTOMOTRIZ", 0));
    }

    private static void setupArrivals(SimulationEngine engine) {
        // PIEZA_AUTOMOTRIZ: Poisson con tasa de 12 piezas/hora
        // Tiempo entre arribos: Exponencial(5) minutos
        // Para 1000 horas = 60,000 minutos
        // Número esperado de arribos: 12,000 piezas
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 0, 12000, 5.0);
    }

    private static void generateConsolidatedReport(List<StatisticsCollector> replicaStatistics) {
        System.out.println("Análisis de " + replicaStatistics.size() + " réplicas:\n");

        // Aquí se puede implementar análisis estadístico más avanzado
        // Por ahora, mostraremos un resumen básico

        System.out.println("Nota: Los reportes individuales de cada réplica se han guardado en archivos separados.");
        System.out.println("Para un análisis detallado, consulte:");
        for (int i = 1; i <= replicaStatistics.size(); i++) {
            System.out.println("  - reporte_replica_" + i + ".txt");
            System.out.println("  - entidades_replica_" + i + ".csv");
            System.out.println("  - locaciones_replica_" + i + ".csv");
        }

        System.out.println("\nOBJETIVOS DEL ANÁLISIS:");
        System.out.println("a) Utilización de equipos - revisar % de utilización en locaciones_replica_X.csv");
        System.out.println("b) Producción de la celda - revisar entidades completadas en los reportes");
        System.out.println("c) Cuello de botella - identificar la estación con mayor utilización");
        System.out.println("\nCuello de botella esperado: FRESADO (9.17 min/pieza, ~6.5 piezas/hora)");
    }

    // ========== CLASES INTERNAS PARA REGLAS DE PROCESAMIENTO ==========

    // Regla simple con tiempo fijo
    private static class SimpleProcessingRule extends ProcessingRule {
        public SimpleProcessingRule(String locationName, String entityTypeName, double processingTime) {
            super(locationName, entityTypeName, processingTime);
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            // Implementación sencilla, se puede personalizar
        }
    }

    // Regla con procesamiento por lotes (ACCUM)
    private static class BatchProcessingRule extends ProcessingRule {
        private final int batchSize;

        public BatchProcessingRule(String locationName, String entityTypeName, double processingTime, int batchSize) {
            super(locationName, entityTypeName, processingTime);
            this.batchSize = batchSize;
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            // La lógica de acumulación se maneja en OperationHandler.handleAccumulate()
            // Esta regla solo define los parámetros
        }

        public int getBatchSize() {
            return batchSize;
        }
    }

    // Regla con tiempo exponencial
    private static class ExponentialProcessingRule extends ProcessingRule {
        public ExponentialProcessingRule(String locationName, String entityTypeName, double mean) {
            super(locationName, entityTypeName, mean);
        }

        @Override
        public void process(Entity entity, SimulationEngine engine) {
            // El tiempo de procesamiento ya se genera con distribución exponencial
            // en el OperationHandler cuando se programa el procesamiento
        }
    }
}
