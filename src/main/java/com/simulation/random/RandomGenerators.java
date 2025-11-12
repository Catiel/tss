package com.simulation.random;

import java.util.Random;

/**
 * Generador de números aleatorios para el sistema DIGEMIC
 * Implementa las distribuciones según el modelo ProModel de pasaportes
 */
public class RandomGenerators {
    private Random random;

    // Parámetros de la simulación según ProModel DIGEMIC
    private double arrivalMeanTime; // E(3.33) - Tiempo entre arribos (Poisson 18/h)
    private double zonaFormasMin; // U(6±2) min - Mínimo de distribución uniforme
    private double zonaFormasMax; // U(6±2) max - Máximo de distribución uniforme
    private double servicioMean; // E(6) - Tiempo de atención en servidores
    private double pausaServidorMean; // E(5) - Tiempo de pausa cada 10 pasaportes
    private double directoASalaProb; // 0.90 - Probabilidad de ir directo a sala

    public RandomGenerators(long seed) {
        this.random = new Random(seed);
    }

    public void initialize(
            double arrivalMeanTime,
            double zonaFormasMin,
            double zonaFormasMax,
            double servicioMean,
            double pausaServidorMean,
            double directoASalaProb) {
        this.arrivalMeanTime = arrivalMeanTime;
        this.zonaFormasMin = zonaFormasMin;
        this.zonaFormasMax = zonaFormasMax;
        this.servicioMean = servicioMean;
        this.pausaServidorMean = pausaServidorMean;
        this.directoASalaProb = directoASalaProb;
    }

    // ARRIBOS: E(3.33) - Distribución exponencial
    public double nextArrivalTime() {
        return nextExponential(arrivalMeanTime);
    }

    // ZONA_FORMAS: U(4, 8) - Distribución uniforme
    public double nextZonaFormasTime() {
        return nextUniform(zonaFormasMin, zonaFormasMax);
    }

    // SERVICIO: E(6) - Distribución exponencial
    public double nextServicioTime() {
        return nextExponential(servicioMean);
    }

    // PAUSA_SERVIDOR: E(5) - Distribución exponencial
    public double nextPausaServidorTime() {
        return nextExponential(pausaServidorMean);
    }

    // ROUTING: 90% directo a sala, 10% a formas
    public boolean goDirectoASala() {
        return random.nextDouble() < directoASalaProb;
    }

    // === PRIVATE HELPER METHODS ===

    private double nextExponential(double mean) {
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive");
        }
        return -mean * Math.log(1.0 - random.nextDouble());
    }

    private double nextUniform(double min, double max) {
        if (min >= max) {
            throw new IllegalArgumentException("Min must be less than max");
        }
        return min + (max - min) * random.nextDouble();
    }
}
