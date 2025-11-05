package com.simulation.random;

import java.util.Random;

public class RandomGenerators {
    private Random random;
    
    // Parámetros de la simulación según ProModel
    private double arrivalMeanTime;
    private double conveyor1Time;
    private double conveyor2Time;
    private double transportWorkerTime;
    private double almacenProcessMean;
    private double almacenProcessStdDev;
    private double cortadoraProcessMean;
    private double tornoProcessMean;
    private double tornoProcessStdDev;
    private double fresadoraProcessMean;
    private double almacen2ProcessMean;
    private double almacen2ProcessStdDev;
    private double pinturaProcessMean;
    private double inspeccion1ProcessMean;
    private double inspeccion1ProcessStdDev;
    private double inspeccion2ProcessMean;
    private double empaqueProcessMean;
    private double empaqueProcessStdDev;
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
            double almacenProcessStdDev,
            double cortadoraProcessMean,
            double tornoProcessMean,
            double tornoProcessStdDev,
            double fresadoraProcessMean,
            double almacen2ProcessMean,
            double almacen2ProcessStdDev,
            double pinturaProcessMean,
            double inspeccion1ProcessMean,
            double inspeccion1ProcessStdDev,
            double inspeccion2ProcessMean,
            double empaqueProcessMean,
            double empaqueProcessStdDev,
            double embarqueProcessMean,
            double inspeccion1ToEmpaqueProb) {
        this.arrivalMeanTime = arrivalMeanTime;
        this.conveyor1Time = conveyor1Time;
        this.conveyor2Time = conveyor2Time;
        this.transportWorkerTime = transportWorkerTime;
        this.almacenProcessMean = almacenProcessMean;
        this.almacenProcessStdDev = almacenProcessStdDev;
        this.cortadoraProcessMean = cortadoraProcessMean;
        this.tornoProcessMean = tornoProcessMean;
        this.tornoProcessStdDev = tornoProcessStdDev;
        this.fresadoraProcessMean = fresadoraProcessMean;
        this.almacen2ProcessMean = almacen2ProcessMean;
        this.almacen2ProcessStdDev = almacen2ProcessStdDev;
        this.pinturaProcessMean = pinturaProcessMean;
        this.inspeccion1ProcessMean = inspeccion1ProcessMean;
        this.inspeccion1ProcessStdDev = inspeccion1ProcessStdDev;
        this.inspeccion2ProcessMean = inspeccion2ProcessMean;
        this.empaqueProcessMean = empaqueProcessMean;
        this.empaqueProcessStdDev = empaqueProcessStdDev;
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
    
    // ALMACEN: N(5, 0.5) según ProModel
    public double nextAlmacenProcess() {
        return nextNormal(almacenProcessMean, almacenProcessStdDev);
    }
    
    // CORTADORA: E(3) según ProModel
    public double nextCortadoraProcess() {
        return nextExponential(cortadoraProcessMean);
    }
    
    // TORNO: N(5, 0.5) según ProModel
    public double nextTornoProcess() {
        return nextNormal(tornoProcessMean, tornoProcessStdDev);
    }
    
    // FRESADORA: E(3) según ProModel
    public double nextFresadoraProcess() {
        return nextExponential(fresadoraProcessMean);
    }
    
    // ALMACEN_2: N(5, 0.5) según ProModel
    public double nextAlmacen2Process() {
        return nextNormal(almacen2ProcessMean, almacen2ProcessStdDev);
    }
    
    // PINTURA: E(3) según ProModel
    public double nextPinturaProcess() {
        return nextExponential(pinturaProcessMean);
    }
    
    // INSPECCION_1: N(5, 0.5) según ProModel
    public double nextInspeccion1Process() {
        return nextNormal(inspeccion1ProcessMean, inspeccion1ProcessStdDev);
    }
    
    // INSPECCION_2: E(3) según ProModel
    public double nextInspeccion2Process() {
        return nextExponential(inspeccion2ProcessMean);
    }
    
    // EMPAQUE: N(5, 0.5) según ProModel
    public double nextEmpaqueProcess() {
        return nextNormal(empaqueProcessMean, empaqueProcessStdDev);
    }
    
    // EMBARQUE: E(3) según ProModel
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
