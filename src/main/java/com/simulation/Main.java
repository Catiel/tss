package com.simulation; // Declaración del paquete principal de la aplicación

import com.simulation.core.SimulationEngine; // Importa el motor principal de simulación
import com.simulation.entities.Entity; // Importa la clase Entity para manejo de entidades
import com.simulation.output.ReportGenerator; // Importa el generador de reportes de salida
import com.simulation.processing.BatchProcessingRule; // Importa la regla de procesamiento por lotes
import com.simulation.processing.ProcessingRule; // Importa la clase base de reglas de procesamiento
import com.simulation.statistics.StatisticsCollector; // Importa el recolector de estadísticas

import java.util.ArrayList; // Importa ArrayList para listas dinámicas
import java.util.List; // Importa la interfaz List para manejo de listas
import java.util.Random; // Importa Random para generación de números aleatorios

public class Main { // Define la clase principal de la aplicación
    public static void main(String[] args) { // Método principal de ejecución del programa
        System.out.println("============================================================="); // Imprime línea superior del encabezado
        System.out.println("  SIMULACIÓN ENGRANES DE ACERO SA"); // Imprime título de la empresa
        System.out.println("  Celda Flexible de Manufactura de Piezas Automotrices"); // Imprime subtítulo descriptivo
        System.out.println("=============================================================\n"); // Imprime línea inferior del encabezado con salto de línea
        System.out.println( // Imprime mensaje de debug con parámetros de configuración
                "DEBUG: Main with ProModel parameters (Horno 100.0, Torneado 5.2, Fresado 9.17, Taladro 1.6, Rectificado 2.85)"); // Muestra tiempos de procesamiento configurados

        // Configurar parámetros de simulación
        int numReplicas = 3; // Define el número de réplicas de simulación a ejecutar
        double simulationTime = 1000.0 * 60.0; // Calcula tiempo total de simulación en minutos (1000 horas)

        List<StatisticsCollector> replicaStatistics = new ArrayList<>(); // Crea lista para almacenar estadísticas de cada réplica

        for (int replica = 1; replica <= numReplicas; replica++) { // Itera por cada réplica de simulación
            System.out.println("\n╔════════════════════════════════════════════════════════════╗"); // Imprime línea superior del encabezado de réplica
            System.out.println("║  RÉPLICA " + replica + " de " + numReplicas); // Imprime número de réplica actual
            System.out.println("╚════════════════════════════════════════════════════════════╝\n"); // Imprime línea inferior del encabezado con salto de línea

            SimulationEngine engine = new SimulationEngine(); // Crea nueva instancia del motor de simulación
            setupEntityTypes(engine); // Configura los tipos de entidades en el motor
            setupLocations(engine); // Configura las ubicaciones en el motor
            setupResources(engine); // Configura los recursos en el motor
            setupProcessingRules(engine); // Configura las reglas de procesamiento en el motor
            setupArrivals(engine); // Configura las llegadas de entidades en el motor

            System.out.println("Ejecutando simulación por " + (simulationTime / 60.0) + " horas (" + simulationTime // Imprime mensaje de inicio con tiempo en horas
                    + " minutos)...\n"); // Continúa el mensaje con tiempo en minutos
            engine.run(simulationTime); // Ejecuta la simulación por el tiempo especificado

            replicaStatistics.add(engine.getStatistics()); // Añade las estadísticas de esta réplica a la lista

            System.out.println("\n--- Resultados de Réplica " + replica + " ---"); // Imprime encabezado de resultados de la réplica
            ReportGenerator reportGenerator = new ReportGenerator(engine.getStatistics()); // Crea generador de reportes con las estadísticas
            reportGenerator.generateConsoleReport(); // Genera y muestra el reporte en consola
            reportGenerator.generateFileReport("reporte_replica_" + replica + ".txt"); // Genera reporte en archivo de texto
        }

        generateConsolidatedReport(replicaStatistics); // Genera reporte consolidado de todas las réplicas
    }

    private static void setupEntityTypes(SimulationEngine engine) { // Método privado que configura los tipos de entidades
        engine.addEntityType("PIEZA_AUTOMOTRIZ", 150.0); // Añade tipo de entidad pieza automotriz con velocidad 150.0
    }

    private static void setupLocations(SimulationEngine engine) { // Método privado que configura todas las ubicaciones del sistema
        engine.addLocation("ALMACEN_MP", 999999, 1); // Añade almacén de materia prima con capacidad casi infinita
        engine.addLocation("HORNO", 10, 1); // Añade horno con capacidad de 10 unidades
        engine.addLocation("BANDA_1", 999999, 1); // Añade banda transportadora 1 con capacidad casi infinita
        engine.addLocation("CARGA", 999999, 1); // Añade área de carga con capacidad casi infinita
        engine.addLocation("TORNEADO", 1, 1); // Añade estación de torneado con capacidad de 1 unidad
        engine.addLocation("FRESADO", 1, 1); // Añade estación de fresado con capacidad de 1 unidad
        engine.addLocation("TALADRO", 1, 1); // Añade estación de taladro con capacidad de 1 unidad
        engine.addLocation("RECTIFICADO", 1, 1); // Añade estación de rectificado con capacidad de 1 unidad
        engine.addLocation("DESCARGA", 999999, 1); // Añade área de descarga con capacidad casi infinita
        engine.addLocation("BANDA_2", 999999, 1); // Añade banda transportadora 2 con capacidad casi infinita
        engine.addLocation("INSPECCION", 1, 1); // Añade estación de inspección con capacidad de 1 unidad
        engine.addLocation("SALIDA", 999999, 1); // Añade área de salida con capacidad casi infinita
    }

    private static void setupResources(SimulationEngine engine) { // Método privado que configura los recursos del sistema
        engine.addResource("GRUA_VIAJERA", 1, 25.0); // Añade grúa viajera con 1 unidad y velocidad 25.0 pies/min
        engine.addResource("ROBOT", 1, 45.0); // Añade robot con 1 unidad y velocidad 45.0 pies/min
    }

    private static void setupProcessingRules(SimulationEngine engine) { // Método privado que configura todas las reglas de procesamiento
        // 1. ALMACEN_MP
        engine.addProcessingRule(new ProcessingRule("ALMACEN_MP", "PIEZA_AUTOMOTRIZ", 0) { // Añade regla para almacén sin tiempo de procesamiento
            @Override // Anotación de sobrescritura del método abstracto
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío para almacén
            }
        });

        // 2. HORNO: 100.0 min, Batch 10 (ProModel: WAIT 100 min)
        engine.addProcessingRule(new BatchProcessingRule("HORNO", "PIEZA_AUTOMOTRIZ", 100.0, 10)); // Añade regla de procesamiento por lotes para horno con tiempo 100 min y tamaño de lote 10

        // 3. BANDA_1: 0.94 min
        engine.addProcessingRule(new ProcessingRule("BANDA_1", "PIEZA_AUTOMOTRIZ", 0.94) { // Añade regla para banda 1 con tiempo 0.94 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 4. CARGA: 0.5 min
        engine.addProcessingRule(new ProcessingRule("CARGA", "PIEZA_AUTOMOTRIZ", 0.5) { // Añade regla para carga con tiempo 0.5 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 5. TORNEADO: 5.2 min (ProModel: WAIT 5.2 min)
        engine.addProcessingRule(new ProcessingRule("TORNEADO", "PIEZA_AUTOMOTRIZ", 5.2) { // Añade regla para torneado con tiempo 5.2 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 6. FRESADO: 9.17 min (ProModel: WAIT 9.17 min)
        engine.addProcessingRule(new ProcessingRule("FRESADO", "PIEZA_AUTOMOTRIZ", 9.17) { // Añade regla para fresado con tiempo 9.17 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 7. TALADRO: 1.6 min (ProModel: WAIT 1.6 min)
        engine.addProcessingRule(new ProcessingRule("TALADRO", "PIEZA_AUTOMOTRIZ", 1.6) { // Añade regla para taladro con tiempo 1.6 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 8. RECTIFICADO: 2.85 min (ProModel: WAIT 2.85 min)
        engine.addProcessingRule(new ProcessingRule("RECTIFICADO", "PIEZA_AUTOMOTRIZ", 2.85) { // Añade regla para rectificado con tiempo 2.85 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 9. DESCARGA: 0.5 min
        engine.addProcessingRule(new ProcessingRule("DESCARGA", "PIEZA_AUTOMOTRIZ", 0.5) { // Añade regla para descarga con tiempo 0.5 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 10. BANDA_2: 1.02 min
        engine.addProcessingRule(new ProcessingRule("BANDA_2", "PIEZA_AUTOMOTRIZ", 1.02) { // Añade regla para banda 2 con tiempo 1.02 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 11. INSPECCION: E(3) min (ProModel: WAIT E(3) min - distribución exponencial)
        engine.addProcessingRule(new ProcessingRule("INSPECCION", "PIEZA_AUTOMOTRIZ", 3.0, true) { // Añade regla para inspección con tiempo exponencial media 3.0 min
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });

        // 12. SALIDA
        engine.addProcessingRule(new ProcessingRule("SALIDA", "PIEZA_AUTOMOTRIZ", 0) { // Añade regla para salida sin tiempo de procesamiento
            @Override // Anotación de sobrescritura
            public void process(Entity e, SimulationEngine en) { // Define método de procesamiento vacío
            }
        });
    }

    private static void setupArrivals(SimulationEngine engine) { // Método privado que configura las llegadas de entidades
        // ProModel: Frecuencia E(5) min - Distribución exponencial con media 5
        engine.scheduleArrival("PIEZA_AUTOMOTRIZ", "ALMACEN_MP", 0, 12000, 5.0, true); // Programa llegadas con distribución exponencial: tipo, ubicación, tiempo inicial 0, cantidad 12000, frecuencia media 5 min, exponencial activado
    }

    private static void generateConsolidatedReport(List<StatisticsCollector> replicaStatistics) { // Método que genera reporte consolidado de todas las réplicas
        System.out.println("Análisis de " + replicaStatistics.size() + " réplicas completado."); // Imprime mensaje de finalización con número de réplicas analizadas
    }
}
