package com.simulation.random;

import java.util.Random;

public class RandomGenerators {
    private Random random;
    
    // Parámetros de la simulación
    private double arrivalMeanTime;
    private double conveyor1Time;
    private double conveyor2Time;
    private double transportWorkerTime;
    private double almacenProcessMean;
    private double cortadoraProcessMean;
    private double cortadoraProcessStdDev;
    private double tornoProcessMean;
    private double tornoProcessStdDev;
    private double fresadoraProcessMean;
    private double fresadoraProcessStdDev;
    private double almacen2ProcessMean;
    private double pinturaProcessMean;
    private double inspeccion1ProcessMean;
    private double inspeccion1ProcessStdDev;
    private double inspeccion2ProcessMean;
    private double inspeccion2ProcessStdDev;
    private double empaqueProcessMean;
    private double embarqueProcessMean;
    private double inspeccion1ToEmpaqueProb;
    
    public RandomGenerators(long seed) {
        this.random = new Random(seed);
    }
    
    public void initialize(
            double arrivalMeanTime,
            double conveyor1Time,
            double conveyor2Time,
            double transportWorkerTime,
            double almacenProcessMean,
            double cortadoraProcessMean,
            double cortadoraProcessStdDev,
            double tornoProcessMean,
            double tornoProcessStdDev,
            double fresadoraProcessMean,
            double fresadoraProcessStdDev,
            double almacen2ProcessMean,
            double pinturaProcessMean,
            double inspeccion1ProcessMean,
            double inspeccion1ProcessStdDev,
            double inspeccion2ProcessMean,
            double inspeccion2ProcessStdDev,
            double empaqueProcessMean,
            double embarqueProcessMean,
            double inspeccion1ToEmpaqueProb) {
        this.arrivalMeanTime = arrivalMeanTime;
        this.conveyor1Time = conveyor1Time;
        this.conveyor2Time = conveyor2Time;
        this.transportWorkerTime = transportWorkerTime;
        this.almacenProcessMean = almacenProcessMean;
        this.cortadoraProcessMean = cortadoraProcessMean;
        this.cortadoraProcessStdDev = cortadoraProcessStdDev;
        this.tornoProcessMean = tornoProcessMean;
        this.tornoProcessStdDev = tornoProcessStdDev;
        this.fresadoraProcessMean = fresadoraProcessMean;
        this.fresadoraProcessStdDev = fresadoraProcessStdDev;
        this.almacen2ProcessMean = almacen2ProcessMean;
        this.pinturaProcessMean = pinturaProcessMean;
        this.inspeccion1ProcessMean = inspeccion1ProcessMean;
        this.inspeccion1ProcessStdDev = inspeccion1ProcessStdDev;
        this.inspeccion2ProcessMean = inspeccion2ProcessMean;
        this.inspeccion2ProcessStdDev = inspeccion2ProcessStdDev;
        this.empaqueProcessMean = empaqueProcessMean;
        this.embarqueProcessMean = embarqueProcessMean;
        this.inspeccion1ToEmpaqueProb = inspeccion1ToEmpaqueProb;
    }
    
    // FIXED TIME: 15 minutos entre arribos
    public double nextArrivalTime() {
        return arrivalMeanTime;
    }
    
    // FIXED TIME: 4 minutos para CONVEYOR_1
    public double nextConveyor1Time() {
        return conveyor1Time;
    }
    
    // FIXED TIME: 4 minutos para CONVEYOR_2
    public double nextConveyor2Time() {
        return conveyor2Time;
    }
    
    // FIXED TIME: 0.1 minutos para transporte por trabajador
    public double nextTransportWorkerTime() {
        return transportWorkerTime;
    }
    
    // ALMACEN: E(2)
    public double nextAlmacenProcess() {
        return nextExponential(almacenProcessMean);
    }
    
    // CORTADORA: N(17, 1.5)
    public double nextCortadoraProcess() {
        return nextNormal(cortadoraProcessMean, cortadoraProcessStdDev);
    }
    
    // TORNO: N(15, 2)
    public double nextTornoProcess() {
        return nextNormal(tornoProcessMean, tornoProcessStdDev);
    }
    
    // FRESADORA: N(18, 2)
    public double nextFresadoraProcess() {
        return nextNormal(fresadoraProcessMean, fresadoraProcessStdDev);
    }
    
    // ALMACEN_2: E(2)
    public double nextAlmacen2Process() {
        return nextExponential(almacen2ProcessMean);
    }
    
    // PINTURA: E(5)
    public double nextPinturaProcess() {
        return nextExponential(pinturaProcessMean);
    }
    
    // INSPECCION_1: N(6, 1)
    public double nextInspeccion1Process() {
        return nextNormal(inspeccion1ProcessMean, inspeccion1ProcessStdDev);
    }
    
    // INSPECCION_2: N(10, 1.5)
    public double nextInspeccion2Process() {
        return nextNormal(inspeccion2ProcessMean, inspeccion2ProcessStdDev);
    }
    
    // EMPAQUE: E(3)
    public double nextEmpaqueProcess() {
        return nextExponential(empaqueProcessMean);
    }
    
    // EMBARQUE: E(2)
    public double nextEmbarqueProcess() {
        return nextExponential(embarqueProcessMean);
    }
    
    // ROUTING: 80% a EMPAQUE, 20% a INSPECCION_2
    public boolean routeToEmpaqueFromInspeccion1() {
        return random.nextDouble() < inspeccion1ToEmpaqueProb;
    }
    
    // === PRIVATE HELPER METHODS ===
    
    private double nextExponential(double mean) {
        if (mean <= 0) {
            throw new IllegalArgumentException("Mean must be positive");
        }
        return -mean * Math.log(1.0 - random.nextDouble());
    }
    
    private double nextNormal(double mean, double stdDev) {
        return mean + stdDev * random.nextGaussian();
    }
}
