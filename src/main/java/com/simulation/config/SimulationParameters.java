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
    private int conveyor1Capacity = Integer.MAX_VALUE;  // CONVEYOR_1 - capacidad infinita
    private int almacenCapacity = 10;
    private int cortadoraCapacity = 1;
    private int tornoCapacity = 2;
    private int conveyor2Capacity = Integer.MAX_VALUE;  // CONVEYOR_2 - capacidad infinita
    private int fresadoraCapacity = 2;
    private int almacen2Capacity = 10;
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
    
    // === PROCESOS según ProModel ===
    // ALMACEN: WAIT N(5, 0.5) min
    private double almacenProcessMean = 5.0;
    private double almacenProcessStdDev = 0.5;
    
    // CORTADORA: WAIT E(3) min
    private double cortadoraProcessMean = 3.0;
    
    // TORNO: WAIT N(5, 0.5) min
    private double tornoProcessMean = 5.0;
    private double tornoProcessStdDev = 0.5;
    
    // FRESADORA: WAIT E(3) min
    private double fresadoraProcessMean = 3.0;
    
    // ALMACEN_2: WAIT N(5, 0.5) min
    private double almacen2ProcessMean = 5.0;
    private double almacen2ProcessStdDev = 0.5;
    
    // PINTURA: WAIT E(3) min
    private double pinturaProcessMean = 3.0;
    
    // INSPECCION_1: WAIT N(5, 0.5) min
    private double inspeccion1ProcessMean = 5.0;
    private double inspeccion1ProcessStdDev = 0.5;
    
    // INSPECCION_2: WAIT E(3) min
    private double inspeccion2ProcessMean = 3.0;
    
    // EMPAQUE: WAIT N(5, 0.5) min
    private double empaqueProcessMean = 5.0;
    private double empaqueProcessStdDev = 0.5;
    
    // EMBARQUE: WAIT E(3) min
    private double embarqueProcessMean = 3.0;
    
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
    
    // Capacidades (12 getters)
    public int getConveyor1Capacity() { return conveyor1Capacity; }
    public int getAlmacenCapacity() { return almacenCapacity; }
    public int getCortadoraCapacity() { return cortadoraCapacity; }
    public int getTornoCapacity() { return tornoCapacity; }
    public int getConveyor2Capacity() { return conveyor2Capacity; }
    public int getFresadoraCapacity() { return fresadoraCapacity; }
    public int getAlmacen2Capacity() { return almacen2Capacity; }
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
    
    // Procesos - getters según ProModel
    public double getAlmacenProcessMean() { return almacenProcessMean; }
    public double getAlmacenProcessStdDev() { return almacenProcessStdDev; }
    public double getCortadoraProcessMean() { return cortadoraProcessMean; }
    public double getTornoProcessMean() { return tornoProcessMean; }
    public double getTornoProcessStdDev() { return tornoProcessStdDev; }
    public double getFresadoraProcessMean() { return fresadoraProcessMean; }
    public double getAlmacen2ProcessMean() { return almacen2ProcessMean; }
    public double getAlmacen2ProcessStdDev() { return almacen2ProcessStdDev; }
    public double getPinturaProcessMean() { return pinturaProcessMean; }
    public double getInspeccion1ProcessMean() { return inspeccion1ProcessMean; }
    public double getInspeccion1ProcessStdDev() { return inspeccion1ProcessStdDev; }
    public double getInspeccion2ProcessMean() { return inspeccion2ProcessMean; }
    public double getEmpaqueProcessMean() { return empaqueProcessMean; }
    public double getEmpaqueProcessStdDev() { return empaqueProcessStdDev; }
    public double getEmbarqueProcessMean() { return embarqueProcessMean; }
    
    // Probabilidades de routing (2 getters)
    public double getInspeccion1ToEmpaqueProb() { return inspeccion1ToEmpaqueProb; }
    public double getInspeccion1ToInspeccion2Prob() { return inspeccion1ToInspeccion2Prob; }

    // === SETTERS ===

    public void setConveyor1Capacity(int value) { this.conveyor1Capacity = value; }
    public void setAlmacenCapacity(int value) { this.almacenCapacity = value; }
    public void setCortadoraCapacity(int value) { this.cortadoraCapacity = value; }
    public void setTornoCapacity(int value) { this.tornoCapacity = value; }
    public void setConveyor2Capacity(int value) { this.conveyor2Capacity = value; }
    public void setFresadoraCapacity(int value) { this.fresadoraCapacity = value; }
    public void setAlmacen2Capacity(int value) { this.almacen2Capacity = value; }
    public void setPinturaCapacity(int value) { this.pinturaCapacity = value; }
    public void setInspeccion1Capacity(int value) { this.inspeccion1Capacity = value; }
    public void setInspeccion2Capacity(int value) { this.inspeccion2Capacity = value; }
    public void setEmpaqueCapacity(int value) { this.empaqueCapacity = value; }
    public void setEmbarqueCapacity(int value) { this.embarqueCapacity = value; }

    public void setArrivalMeanTime(double value) { this.arrivalMeanTime = value; }
    public void setConveyor1Time(double value) { this.conveyor1Time = value; }
    public void setConveyor2Time(double value) { this.conveyor2Time = value; }
    public void setTransportWorkerTime(double value) { this.transportWorkerTime = value; }

    public void setAlmacenProcessMean(double value) { this.almacenProcessMean = value; }
    public void setAlmacenProcessStdDev(double value) { this.almacenProcessStdDev = value; }
    public void setCortadoraProcessMean(double value) { this.cortadoraProcessMean = value; }
    public void setTornoProcessMean(double value) { this.tornoProcessMean = value; }
    public void setTornoProcessStdDev(double value) { this.tornoProcessStdDev = value; }
    public void setFresadoraProcessMean(double value) { this.fresadoraProcessMean = value; }
    public void setAlmacen2ProcessMean(double value) { this.almacen2ProcessMean = value; }
    public void setAlmacen2ProcessStdDev(double value) { this.almacen2ProcessStdDev = value; }
    public void setPinturaProcessMean(double value) { this.pinturaProcessMean = value; }
    public void setInspeccion1ProcessMean(double value) { this.inspeccion1ProcessMean = value; }
    public void setInspeccion1ProcessStdDev(double value) { this.inspeccion1ProcessStdDev = value; }
    public void setInspeccion2ProcessMean(double value) { this.inspeccion2ProcessMean = value; }
    public void setEmpaqueProcessMean(double value) { this.empaqueProcessMean = value; }
    public void setEmpaqueProcessStdDev(double value) { this.empaqueProcessStdDev = value; }
    public void setEmbarqueProcessMean(double value) { this.embarqueProcessMean = value; }

    public void setInspeccion1ToEmpaqueProb(double value) { this.inspeccion1ToEmpaqueProb = value; }
    public void setInspeccion1ToInspeccion2Prob(double value) { this.inspeccion1ToInspeccion2Prob = value; }
}

