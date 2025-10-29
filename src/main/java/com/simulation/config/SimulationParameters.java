package com.simulation.config;

public class SimulationParameters {
    // Duración de la simulación: 90 días x 24 horas x 60 minutos = 129,600 minutos
    private double simulationDurationMinutes = 129600.0; // CORREGIDO: 90 días completos

    // Semilla aleatoria base
    private long baseRandomSeed = 12345L;

    // Capacidades de locaciones
    private int lavadoraCapacity = 5;
    private int almacenPinturaCapacity = 10;
    private int pinturaCapacity = 3;
    private int almacenHornoCapacity = 10;
    private int hornoCapacity = 1;
    private int inspeccionNumStations = 2;
    private int inspeccionOperationsPerPiece = 3;

    // Parámetros de arribos: E(2) = Exponencial con media 2
    private double arrivalMeanTime = 2.0;

    // Transporte RECEPCION -> LAVADORA: MOVE FOR E(3)
    private double transportRecepcionLavadoraMean = 3.0;

    // Transporte LAVADORA -> ALMACEN_PINTURA: MOVE FOR E(2)
    private double transportLavadoraAlmacenMean = 2.0;

    // Proceso LAVADORA: WAIT N(10, 2)min = Normal(media=10, desv=2)
    private double lavadoraProcessMean = 10.0;
    private double lavadoraProcessStdDev = 2.0;

    // Proceso PINTURA: WAIT T(4, 8, 10)min = Triangular(min=4, mode=8, max=10)
    private double pinturaProcessMin = 4.0;
    private double pinturaProcessMode = 8.0;
    private double pinturaProcessMax = 10.0;

    // Transporte PINTURA -> ALMACEN_HORNO: MOVE FOR U(3.5, 1.5)
    // U(3.5, 1.5) = Uniforme con media 3.5 y half-width 1.5
    // Rango: [3.5-1.5, 3.5+1.5] = [2.0, 5.0]
    private double transportPinturaAlmacenMin = 2.0;
    private double transportPinturaAlmacenMax = 5.0;

    // Proceso HORNO: WAIT U(3, 1)min
    // U(3, 1) = Uniforme con media 3 y half-width 1
    // Rango: [3-1, 3+1] = [2.0, 4.0]
    private double hornoProcessMin = 2.0;
    private double hornoProcessMax = 4.0;

    // Transporte HORNO -> INSPECCION: MOVE FOR U(2, 1)
    // U(2, 1) = Uniforme con media 2 y half-width 1
    // Rango: [2-1, 2+1] = [1.0, 3.0]
    private double transportHornoInspeccionMin = 1.0;
    private double transportHornoInspeccionMax = 3.0;

    // Operaciones de INSPECCION: WAIT E(2) por cada operación
    private double inspeccionOperationMean = 2.0;

    // Getters y Setters
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

    public int getLavadoraCapacity() {
        return lavadoraCapacity;
    }

    public int getAlmacenPinturaCapacity() {
        return almacenPinturaCapacity;
    }

    public int getPinturaCapacity() {
        return pinturaCapacity;
    }

    public int getAlmacenHornoCapacity() {
        return almacenHornoCapacity;
    }

    public int getHornoCapacity() {
        return hornoCapacity;
    }

    public int getInspeccionNumStations() {
        return inspeccionNumStations;
    }

    public int getInspeccionOperationsPerPiece() {
        return inspeccionOperationsPerPiece;
    }

    public double getArrivalMeanTime() {
        return arrivalMeanTime;
    }

    public double getTransportRecepcionLavadoraMean() {
        return transportRecepcionLavadoraMean;
    }

    public double getTransportLavadoraAlmacenMean() {
        return transportLavadoraAlmacenMean;
    }

    public double getLavadoraProcessMean() {
        return lavadoraProcessMean;
    }

    public double getLavadoraProcessStdDev() {
        return lavadoraProcessStdDev;
    }

    public double getPinturaProcessMin() {
        return pinturaProcessMin;
    }

    public double getPinturaProcessMode() {
        return pinturaProcessMode;
    }

    public double getPinturaProcessMax() {
        return pinturaProcessMax;
    }

    public double getTransportPinturaAlmacenMin() {
        return transportPinturaAlmacenMin;
    }

    public double getTransportPinturaAlmacenMax() {
        return transportPinturaAlmacenMax;
    }

    public double getHornoProcessMin() {
        return hornoProcessMin;
    }

    public double getHornoProcessMax() {
        return hornoProcessMax;
    }

    public double getTransportHornoInspeccionMin() {
        return transportHornoInspeccionMin;
    }

    public double getTransportHornoInspeccionMax() {
        return transportHornoInspeccionMax;
    }

    public double getInspeccionOperationMean() {
        return inspeccionOperationMean;
    }
}