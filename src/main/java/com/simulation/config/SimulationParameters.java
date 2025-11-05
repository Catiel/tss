package com.simulation.config;

/**
 * Parámetros de configuración para el sistema Multi-Engrane
 * Sistema de fabricación de engranes con 12 locaciones
 */
public class SimulationParameters {
    // Duración: 60 horas = 3,600 minutos
    private double simulationDurationMinutes = 3600.0;
    
    // Semilla aleatoria
    private long baseRandomSeed = 12345L;
    
    // === CAPACIDADES DE LAS 12 LOCACIONES ===
    private int almacenCapacity = 10;
    private int cortadoraCapacity = 1;
    private int tornoCapacity = 2;
    private int almacen2Capacity = 10;
    private int fresadoraCapacity = 2;
    private int pinturaCapacity = 4;
    private int inspeccion1Capacity = 2;
    private int inspeccion2Capacity = 1;
    private int empaqueCapacity = 1;
    private int embarqueCapacity = 3;
    
    // === PARÁMETROS DE ARRIBOS Y TRANSPORTES ===
    private double arrivalMeanTime = 15.0;  // Tiempo fijo entre arribos
    private double conveyor1Time = 4.0;      // Tiempo fijo CONVEYOR_1
    private double conveyor2Time = 4.0;      // Tiempo fijo CONVEYOR_2
    private double transportWorkerTime = 0.1; // Tiempo fijo transporte trabajador
    
    // === PROCESOS (19 parámetros) ===
    // ALMACEN: E(2)
    private double almacenProcessMean = 2.0;
    
    // CORTADORA: N(17, 1.5)
    private double cortadoraProcessMean = 17.0;
    private double cortadoraProcessStdDev = 1.5;
    
    // TORNO: N(15, 2)
    private double tornoProcessMean = 15.0;
    private double tornoProcessStdDev = 2.0;
    
    // FRESADORA: N(18, 2)
    private double fresadoraProcessMean = 18.0;
    private double fresadoraProcessStdDev = 2.0;
    
    // ALMACEN_2: E(2)
    private double almacen2ProcessMean = 2.0;
    
    // PINTURA: E(5)
    private double pinturaProcessMean = 5.0;
    
    // INSPECCION_1: N(6, 1)
    private double inspeccion1ProcessMean = 6.0;
    private double inspeccion1ProcessStdDev = 1.0;
    
    // INSPECCION_2: N(10, 1.5)
    private double inspeccion2ProcessMean = 10.0;
    private double inspeccion2ProcessStdDev = 1.5;
    
    // EMPAQUE: E(3)
    private double empaqueProcessMean = 3.0;
    
    // EMBARQUE: E(2)
    private double embarqueProcessMean = 2.0;
    
    // === PROBABILIDADES DE ROUTING ===
    private double inspeccion1ToEmpaqueProb = 0.80;   // 80% a EMPAQUE
    private double inspeccion1ToInspeccion2Prob = 0.20; // 20% a INSPECCION_2
    
    // === GETTERS ===
    
    public double getSimulationDurationMinutes() {
        return simulationDurationMinutes;
    }
    
    public void setSimulationDurationMinutes(double simulationDurationMinutes) {
        this.simulationDurationMinutes = simulationDurationMinutes;
    }
    
    public long getBaseRandomSeed() {
        return baseRandomSeed;
    }
    
    public void setBaseRandomSeed(long baseRandomSeed) {
        this.baseRandomSeed = baseRandomSeed;
    }
    
    // Capacidades (10 getters)
    public int getAlmacenCapacity() { return almacenCapacity; }
    public int getCortadoraCapacity() { return cortadoraCapacity; }
    public int getTornoCapacity() { return tornoCapacity; }
    public int getAlmacen2Capacity() { return almacen2Capacity; }
    public int getFresadoraCapacity() { return fresadoraCapacity; }
    public int getPinturaCapacity() { return pinturaCapacity; }
    public int getInspeccion1Capacity() { return inspeccion1Capacity; }
    public int getInspeccion2Capacity() { return inspeccion2Capacity; }
    public int getEmpaqueCapacity() { return empaqueCapacity; }
    public int getEmbarqueCapacity() { return embarqueCapacity; }
    
    // Arribos y transportes (4 getters)
    public double getArrivalMeanTime() { return arrivalMeanTime; }
    public double getConveyor1Time() { return conveyor1Time; }
    public double getConveyor2Time() { return conveyor2Time; }
    public double getTransportWorkerTime() { return transportWorkerTime; }
    
    // Procesos (19 getters)
    public double getAlmacenProcessMean() { return almacenProcessMean; }
    public double getCortadoraProcessMean() { return cortadoraProcessMean; }
    public double getCortadoraProcessStdDev() { return cortadoraProcessStdDev; }
    public double getTornoProcessMean() { return tornoProcessMean; }
    public double getTornoProcessStdDev() { return tornoProcessStdDev; }
    public double getFresadoraProcessMean() { return fresadoraProcessMean; }
    public double getFresadoraProcessStdDev() { return fresadoraProcessStdDev; }
    public double getAlmacen2ProcessMean() { return almacen2ProcessMean; }
    public double getPinturaProcessMean() { return pinturaProcessMean; }
    public double getInspeccion1ProcessMean() { return inspeccion1ProcessMean; }
    public double getInspeccion1ProcessStdDev() { return inspeccion1ProcessStdDev; }
    public double getInspeccion2ProcessMean() { return inspeccion2ProcessMean; }
    public double getInspeccion2ProcessStdDev() { return inspeccion2ProcessStdDev; }
    public double getEmpaqueProcessMean() { return empaqueProcessMean; }
    public double getEmbarqueProcessMean() { return embarqueProcessMean; }
    
    // Probabilidades de routing (2 getters)
    public double getInspeccion1ToEmpaqueProb() { return inspeccion1ToEmpaqueProb; }
    public double getInspeccion1ToInspeccion2Prob() { return inspeccion1ToInspeccion2Prob; }
}

