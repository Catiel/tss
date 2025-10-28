package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.statistics.Statistics;
import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Gestor de réplicas de simulación
 * Ejecuta múltiples corridas con diferentes semillas aleatorias
 */
public class ReplicationManager {
    private SimulationParameters baseParameters;
    private int numReplications;
    private List<Statistics> replicationResults;
    private boolean running;
    private Thread executionThread;

    public ReplicationManager(SimulationParameters baseParameters, int numReplications) {
        this.baseParameters = baseParameters;
        this.numReplications = numReplications;
        this.replicationResults = new ArrayList<>();
        this.running = false;
    }

    /**
     * Ejecuta todas las réplicas de forma asíncrona
     * @param progressCallback Callback para actualizar progreso (replicación actual)
     * @param completionCallback Callback al completar todas las réplicas
     */
    public void runReplications(Consumer<Integer> progressCallback, Runnable completionCallback) {
        if (running) {
            System.out.println("Ya hay réplicas en ejecución");
            return;
        }

        running = true;
        replicationResults.clear();

        executionThread = new Thread(() -> {
            try {
                for (int i = 0; i < numReplications && running; i++) {
                    final int replicationNum = i + 1;

                    // Crear parámetros con semilla única para esta réplica
                    SimulationParameters repParams = createReplicationParameters(replicationNum);

                    // Crear y ejecutar engine para esta réplica
                    SimulationEngine engine = new SimulationEngine(repParams);
                    engine.setSimulationSpeed(999999); // Máxima velocidad para réplicas
                    engine.initialize();

                    // Ejecutar simulación
                    engine.run();

                    // Guardar resultados
                    Statistics stats = engine.getStatistics();
                    replicationResults.add(stats);

                    // Actualizar progreso en el hilo de JavaFX
                    Platform.runLater(() -> progressCallback.accept(replicationNum));

                    // Pequeña pausa para permitir actualizaciones de UI
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                running = false;

                // Notificar completación en el hilo de JavaFX
                Platform.runLater(completionCallback);

            } catch (Exception e) {
                e.printStackTrace();
                running = false;
                Platform.runLater(completionCallback);
            }
        });

        executionThread.setDaemon(true);
        executionThread.start();
    }

    /**
     * Crea parámetros específicos para una réplica
     */
    private SimulationParameters createReplicationParameters(int replicationNumber) {
        SimulationParameters params = new SimulationParameters();

        // Copiar parámetros base
        params.setSimulationDurationMinutes(baseParameters.getSimulationDurationMinutes());

        // Usar semilla diferente para cada réplica
        long baseSeed = baseParameters.getBaseRandomSeed();
        params.setBaseRandomSeed(baseSeed + replicationNumber * 1000);

        return params;
    }

    /**
     * Detiene la ejecución de réplicas
     */
    public void stop() {
        running = false;
        if (executionThread != null && executionThread.isAlive()) {
            executionThread.interrupt();
        }
    }

    /**
     * Obtiene los resultados de todas las réplicas
     */
    public List<Statistics> getReplicationResults() {
        return new ArrayList<>(replicationResults);
    }

    /**
     * Calcula estadísticas agregadas de todas las réplicas
     */
    public AggregatedStatistics getAggregatedStatistics() {
        if (replicationResults.isEmpty()) {
            return null;
        }

        return new AggregatedStatistics(replicationResults);
    }

    public boolean isRunning() {
        return running;
    }

    public int getNumReplications() {
        return numReplications;
    }

    public int getCompletedReplications() {
        return replicationResults.size();
    }

    /**
     * Clase interna para estadísticas agregadas
     */
    public static class AggregatedStatistics {
        private List<Statistics> allStats;

        // Promedios
        private double avgTotalArrivals;
        private double avgTotalExits;
        private double avgThroughput;
        private double avgAverageSystemTime;

        // Desviaciones estándar
        private double stdTotalExits;
        private double stdThroughput;
        private double stdAverageSystemTime;

        // Intervalos de confianza (95%)
        private double[] ciTotalExits;
        private double[] ciThroughput;
        private double[] ciAverageSystemTime;

        public AggregatedStatistics(List<Statistics> stats) {
            this.allStats = stats;
            calculateStatistics();
        }

        private void calculateStatistics() {
            int n = allStats.size();
            if (n == 0) return;

            // Calcular promedios
            double sumArrivals = 0;
            double sumExits = 0;
            double sumThroughput = 0;
            double sumSystemTime = 0;

            for (Statistics stat : allStats) {
                sumArrivals += stat.getTotalArrivals();
                sumExits += stat.getTotalExits();
                sumThroughput += stat.getThroughput();
                sumSystemTime += stat.getAverageSystemTime();
            }

            avgTotalArrivals = sumArrivals / n;
            avgTotalExits = sumExits / n;
            avgThroughput = sumThroughput / n;
            avgAverageSystemTime = sumSystemTime / n;

            // Calcular desviaciones estándar
            double sumSqExits = 0;
            double sumSqThroughput = 0;
            double sumSqSystemTime = 0;

            for (Statistics stat : allStats) {
                sumSqExits += Math.pow(stat.getTotalExits() - avgTotalExits, 2);
                sumSqThroughput += Math.pow(stat.getThroughput() - avgThroughput, 2);
                sumSqSystemTime += Math.pow(stat.getAverageSystemTime() - avgAverageSystemTime, 2);
            }

            stdTotalExits = Math.sqrt(sumSqExits / n);
            stdThroughput = Math.sqrt(sumSqThroughput / n);
            stdAverageSystemTime = Math.sqrt(sumSqSystemTime / n);

            // Calcular intervalos de confianza del 95% (t-student para n pequeño)
            double tValue = getTValue(n);
            double marginExits = tValue * stdTotalExits / Math.sqrt(n);
            double marginThroughput = tValue * stdThroughput / Math.sqrt(n);
            double marginSystemTime = tValue * stdAverageSystemTime / Math.sqrt(n);

            ciTotalExits = new double[]{avgTotalExits - marginExits, avgTotalExits + marginExits};
            ciThroughput = new double[]{avgThroughput - marginThroughput, avgThroughput + marginThroughput};
            ciAverageSystemTime = new double[]{avgAverageSystemTime - marginSystemTime, avgAverageSystemTime + marginSystemTime};
        }

        private double getTValue(int n) {
            // Aproximación de valores t-student para 95% de confianza
            if (n <= 2) return 12.706;
            if (n <= 5) return 2.776;
            if (n <= 10) return 2.228;
            if (n <= 20) return 2.086;
            if (n <= 30) return 2.042;
            return 1.96; // Para n > 30, aproximar con distribución normal
        }

        // Getters
        public double getAvgTotalArrivals() {
            return avgTotalArrivals;
        }

        public double getAvgTotalExits() {
            return avgTotalExits;
        }

        public double getAvgThroughput() {
            return avgThroughput;
        }

        public double getAvgAverageSystemTime() {
            return avgAverageSystemTime;
        }

        public double getStdTotalExits() {
            return stdTotalExits;
        }

        public double getStdThroughput() {
            return stdThroughput;
        }

        public double getStdAverageSystemTime() {
            return stdAverageSystemTime;
        }

        public double[] getCiTotalExits() {
            return ciTotalExits;
        }

        public double[] getCiThroughput() {
            return ciThroughput;
        }

        public double[] getCiAverageSystemTime() {
            return ciAverageSystemTime;
        }

        public List<Statistics> getAllStatistics() {
            return allStats;
        }

        @Override
        public String toString() {
            return String.format(
                "Estadísticas Agregadas (%d réplicas):\n" +
                "  Arribos promedio: %.2f\n" +
                "  Salidas promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f]\n" +
                "  Throughput promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f] piezas/hora\n" +
                "  Tiempo en sistema promedio: %.2f ± %.2f [IC 95%%: %.2f - %.2f] min",
                allStats.size(),
                avgTotalArrivals,
                avgTotalExits, stdTotalExits, ciTotalExits[0], ciTotalExits[1],
                avgThroughput, stdThroughput, ciThroughput[0], ciThroughput[1],
                avgAverageSystemTime, stdAverageSystemTime, ciAverageSystemTime[0], ciAverageSystemTime[1]
            );
        }
    }
}
