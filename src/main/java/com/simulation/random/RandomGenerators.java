package com.simulation.random;

import java.util.Random;

public class RandomGenerators {
    private Random random;

    // Parámetros de distribuciones
    private double arrivalMean;
    private double transportRecepcionLavadoraMean;
    private double transportLavadoraAlmacenMean;
    private double lavadoraProcessMean;
    private double lavadoraProcessStdDev;
    private double pinturaProcessMin;
    private double pinturaProcessMode;
    private double pinturaProcessMax;
    private double transportPinturaAlmacenMin;
    private double transportPinturaAlmacenMax;
    private double hornoProcessMin;
    private double hornoProcessMax;
    private double transportHornoInspeccionMin;
    private double transportHornoInspeccionMax;
    private double inspeccionOperationMean;

    public RandomGenerators(long seed) {
        this.random = new Random(seed);
    }

    public void initialize(
            double arrivalMean,
            double transportRecepcionLavadoraMean,
            double transportLavadoraAlmacenMean,
            double lavadoraProcessMean,
            double lavadoraProcessStdDev,
            double pinturaProcessMin,
            double pinturaProcessMode,
            double pinturaProcessMax,
            double transportPinturaAlmacenMin,
            double transportPinturaAlmacenMax,
            double hornoProcessMin,
            double hornoProcessMax,
            double transportHornoInspeccionMin,
            double transportHornoInspeccionMax,
            double inspeccionOperationMean) {

        this.arrivalMean = arrivalMean;
        this.transportRecepcionLavadoraMean = transportRecepcionLavadoraMean;
        this.transportLavadoraAlmacenMean = transportLavadoraAlmacenMean;
        this.lavadoraProcessMean = lavadoraProcessMean;
        this.lavadoraProcessStdDev = lavadoraProcessStdDev;
        this.pinturaProcessMin = pinturaProcessMin;
        this.pinturaProcessMode = pinturaProcessMode;
        this.pinturaProcessMax = pinturaProcessMax;
        this.transportPinturaAlmacenMin = transportPinturaAlmacenMin;
        this.transportPinturaAlmacenMax = transportPinturaAlmacenMax;
        this.hornoProcessMin = hornoProcessMin;
        this.hornoProcessMax = hornoProcessMax;
        this.transportHornoInspeccionMin = transportHornoInspeccionMin;
        this.transportHornoInspeccionMax = transportHornoInspeccionMax;
        this.inspeccionOperationMean = inspeccionOperationMean;
    }

    /**
     * Distribución Exponencial: E(mean)
     * Genera tiempo entre arribos
     */
    public double nextArrivalTime() {
        return -arrivalMean * Math.log(1.0 - random.nextDouble());
    }

    /**
     * Transporte RECEPCION -> LAVADORA: E(3)
     */
    public double nextTransportRecepcionLavadora() {
        return -transportRecepcionLavadoraMean * Math.log(1.0 - random.nextDouble());
    }

    /**
     * Transporte LAVADORA -> ALMACEN_PINTURA: E(2)
     */
    public double nextTransportLavadoraAlmacen() {
        return -transportLavadoraAlmacenMean * Math.log(1.0 - random.nextDouble());
    }

    /**
     * Proceso LAVADORA: N(10, 2) = Normal(media=10, desv=2)
     */
    public double nextLavadoraProcess() {
        double value = random.nextGaussian() * lavadoraProcessStdDev + lavadoraProcessMean;
        // Asegurar que no sea negativo
        return Math.max(0.1, value);
    }

    /**
     * Proceso PINTURA: T(4, 8, 10) = Triangular(min=4, mode=8, max=10)
     */
    public double nextPinturaProcess() {
        double u = random.nextDouble();
        double f = (pinturaProcessMode - pinturaProcessMin) / (pinturaProcessMax - pinturaProcessMin);

        if (u < f) {
            return pinturaProcessMin + Math.sqrt(u * (pinturaProcessMax - pinturaProcessMin) *
                                                     (pinturaProcessMode - pinturaProcessMin));
        } else {
            return pinturaProcessMax - Math.sqrt((1 - u) * (pinturaProcessMax - pinturaProcessMin) *
                                                           (pinturaProcessMax - pinturaProcessMode));
        }
    }

    /**
     * Transporte PINTURA -> ALMACEN_HORNO: U(3.5, 1.5)
     * Uniforme en rango [2.0, 5.0]
     */
    public double nextTransportPinturaAlmacen() {
        return transportPinturaAlmacenMin +
               (transportPinturaAlmacenMax - transportPinturaAlmacenMin) * random.nextDouble();
    }

    /**
     * Proceso HORNO: U(3, 1)
     * Uniforme en rango [2.0, 4.0]
     */
    public double nextHornoProcess() {
        return hornoProcessMin +
               (hornoProcessMax - hornoProcessMin) * random.nextDouble();
    }

    /**
     * Transporte HORNO -> INSPECCION: U(2, 1)
     * Uniforme en rango [1.0, 3.0]
     */
    public double nextTransportHornoInspeccion() {
        return transportHornoInspeccionMin +
               (transportHornoInspeccionMax - transportHornoInspeccionMin) * random.nextDouble();
    }

    /**
     * Operación de INSPECCION: E(2)
     */
    public double nextInspeccionOperation() {
        return -inspeccionOperationMean * Math.log(1.0 - random.nextDouble());
    }
}
