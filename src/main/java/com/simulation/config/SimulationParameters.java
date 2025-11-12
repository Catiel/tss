package com.simulation.config;

/**
 * Parámetros de configuración para el sistema DIGEMIC
 * Sistema de expedición de pasaportes con 6 locaciones
 */
public class SimulationParameters {
    // Duración: 8 horas = 480 minutos
    private double simulationDurationMinutes = 480.0;

    // Semilla aleatoria
    private long baseRandomSeed = 12345L;

    // === CAPACIDADES DE LAS 6 LOCACIONES ===
    private int entradaCapacity = Integer.MAX_VALUE;  // ENTRADA - capacidad infinita
    private int zonaFormasCapacity = Integer.MAX_VALUE; // ZONA_FORMAS - capacidad infinita
    private int salaSillasCapacity = 40; // SALA_SILLAS - 40 sillas
    private int salaDePieCapacity = Integer.MAX_VALUE; // SALA_DE_PIE - capacidad infinita
    private int servidor1Capacity = 1; // SERVIDOR_1 - 1 servidor
    private int servidor2Capacity = 1; // SERVIDOR_2 - 1 servidor

    // === PARÁMETROS DE ARRIBOS ===
    // Poisson con 18 clientes/h → Exponencial con media 3.33 min entre arribos
    private double arrivalMeanTime = 3.33;  // E(3.33) minutos entre arribos

    // === PROCESOS según ProModel DIGEMIC ===
    // ZONA_FORMAS: WAIT U(6, 2) min → Uniforme entre 4 y 8 minutos
    private double zonaFormasMin = 4.0; // Mínimo de la distribución uniforme
    private double zonaFormasMax = 8.0; // Máximo de la distribución uniforme

    // SERVIDORES: WAIT E(6) min
    private double servicioMean = 6.0; // Tiempo de atención exponencial con media 6 minutos

    // TIEMPO DE PAUSA: WAIT E(5) min (cada 10 pasaportes)
    private double pausaServidorMean = 5.0; // Tiempo exponencial de pausa con media 5 minutos
    private int pasaportesPorPausa = 10; // Cada 10 pasaportes se hace una pausa

    // === PROBABILIDADES DE ROUTING ===
    private double directoASalaProb = 0.90;   // 90% directo a sala
    private double aFormasProb = 0.10; // 10% a llenar formas

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

    // Capacidades (6 getters)
    public int getEntradaCapacity() { return entradaCapacity; }
    public int getZonaFormasCapacity() { return zonaFormasCapacity; }
    public int getSalaSillasCapacity() { return salaSillasCapacity; }
    public int getSalaDePieCapacity() { return salaDePieCapacity; }
    public int getServidor1Capacity() { return servidor1Capacity; }
    public int getServidor2Capacity() { return servidor2Capacity; }

    // Arribos
    public double getArrivalMeanTime() { return arrivalMeanTime; }

    // Procesos
    public double getZonaFormasMin() { return zonaFormasMin; }
    public double getZonaFormasMax() { return zonaFormasMax; }
    public double getServicioMean() { return servicioMean; }
    public double getPausaServidorMean() { return pausaServidorMean; }
    public int getPasaportesPorPausa() { return pasaportesPorPausa; }

    // Probabilidades de routing
    public double getDirectoASalaProb() { return directoASalaProb; }
    public double getAFormasProb() { return aFormasProb; }

    // === SETTERS ===

    public void setEntradaCapacity(int value) { this.entradaCapacity = value; }
    public void setZonaFormasCapacity(int value) { this.zonaFormasCapacity = value; }
    public void setSalaSillasCapacity(int value) { this.salaSillasCapacity = value; }
    public void setSalaDePieCapacity(int value) { this.salaDePieCapacity = value; }
    public void setServidor1Capacity(int value) { this.servidor1Capacity = value; }
    public void setServidor2Capacity(int value) { this.servidor2Capacity = value; }

    public void setArrivalMeanTime(double value) { this.arrivalMeanTime = value; }
    public void setZonaFormasMin(double value) { this.zonaFormasMin = value; }
    public void setZonaFormasMax(double value) { this.zonaFormasMax = value; }
    public void setServicioMean(double value) { this.servicioMean = value; }
    public void setPausaServidorMean(double value) { this.pausaServidorMean = value; }
    public void setPasaportesPorPausa(int value) { this.pasaportesPorPausa = value; }

    public void setDirectoASalaProb(double value) { this.directoASalaProb = value; }
    public void setAFormasProb(double value) { this.aFormasProb = value; }
}
