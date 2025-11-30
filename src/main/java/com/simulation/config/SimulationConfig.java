package com.simulation.config;

import java.util.HashMap;
import java.util.Map;

/**
 * Clase que almacena toda la configuración parametrizable de la simulación
 */
public class SimulationConfig {

    // ===== PARÁMETROS GENERALES =====
    private double simulationTime = 1000.05; // Tiempo de simulación en horas
    private int numberOfReplicas = 3;
    private double warmUpTime = 0.0;
    private long randomSeed = System.currentTimeMillis();

    // ===== PARÁMETROS DE LOCACIONES =====
    private Map<String, Integer> locationCapacities = new HashMap<>();
    private Map<String, Double> processingTimes = new HashMap<>();

    // HORNO específico
    private int hornoBatchSize = 10;
    private double hornoProcessingTime = 100.0;

    // ===== PARÁMETROS DE RECURSOS =====
    private int gruaQuantity = 1;
    private double gruaEmptySpeed = 25.0; // pies/min
    private double gruaLoadedSpeed = 25.0;

    private int robotQuantity = 1;
    private double robotEmptySpeed = 45.0;
    private double robotLoadedSpeed = 45.0;

    // ===== PARÁMETROS DE ARRIBOS =====
    private String arrivalDistribution = "EXPONENTIAL"; // EXPONENTIAL, CONSTANT
    private double arrivalMeanTime = 5.0; // minutos
    private double arrivalFirstTime = 0.0;
    private int arrivalMaxEntities = 12000;

    public SimulationConfig() {
        initializeDefaults();
    }

    private void initializeDefaults() {
        // Capacidades por defecto
        locationCapacities.put("ALMACEN_MP", Integer.MAX_VALUE);
        locationCapacities.put("HORNO", 10);
        locationCapacities.put("BANDA_1", Integer.MAX_VALUE);
        locationCapacities.put("CARGA", Integer.MAX_VALUE);
        locationCapacities.put("TORNEADO", 1);
        locationCapacities.put("FRESADO", 1);
        locationCapacities.put("TALADRO", 1);
        locationCapacities.put("RECTIFICADO", 1);
        locationCapacities.put("DESCARGA", Integer.MAX_VALUE);
        locationCapacities.put("BANDA_2", Integer.MAX_VALUE);
        locationCapacities.put("INSPECCION", 1);
        locationCapacities.put("SALIDA", Integer.MAX_VALUE);

        // Tiempos de procesamiento por defecto
        processingTimes.put("ALMACEN_MP", 0.0);
        processingTimes.put("BANDA_1", 0.94);
        processingTimes.put("CARGA", 0.5);
        processingTimes.put("TORNEADO", 5.2);
        processingTimes.put("FRESADO", 9.17);
        processingTimes.put("TALADRO", 1.6);
        processingTimes.put("RECTIFICADO", 2.85);
        processingTimes.put("DESCARGA", 0.5);
        processingTimes.put("BANDA_2", 1.02);
        processingTimes.put("INSPECCION", 3.0);
        processingTimes.put("SALIDA", 0.0);
    }

    // ===== GETTERS Y SETTERS =====

    public double getSimulationTime() {
        return simulationTime;
    }

    public void setSimulationTime(double simulationTime) {
        this.simulationTime = simulationTime;
    }

    public int getNumberOfReplicas() {
        return numberOfReplicas;
    }

    public void setNumberOfReplicas(int numberOfReplicas) {
        this.numberOfReplicas = numberOfReplicas;
    }

    public double getWarmUpTime() {
        return warmUpTime;
    }

    public void setWarmUpTime(double warmUpTime) {
        this.warmUpTime = warmUpTime;
    }

    public long getRandomSeed() {
        return randomSeed;
    }

    public void setRandomSeed(long randomSeed) {
        this.randomSeed = randomSeed;
    }

    public Map<String, Integer> getLocationCapacities() {
        return locationCapacities;
    }

    public Map<String, Double> getProcessingTimes() {
        return processingTimes;
    }

    public int getHornoBatchSize() {
        return hornoBatchSize;
    }

    public void setHornoBatchSize(int hornoBatchSize) {
        this.hornoBatchSize = hornoBatchSize;
    }

    public double getHornoProcessingTime() {
        return hornoProcessingTime;
    }

    public void setHornoProcessingTime(double hornoProcessingTime) {
        this.hornoProcessingTime = hornoProcessingTime;
    }

    public int getGruaQuantity() {
        return gruaQuantity;
    }

    public void setGruaQuantity(int gruaQuantity) {
        this.gruaQuantity = gruaQuantity;
    }

    public double getGruaEmptySpeed() {
        return gruaEmptySpeed;
    }

    public void setGruaEmptySpeed(double gruaEmptySpeed) {
        this.gruaEmptySpeed = gruaEmptySpeed;
    }

    public double getGruaLoadedSpeed() {
        return gruaLoadedSpeed;
    }

    public void setGruaLoadedSpeed(double gruaLoadedSpeed) {
        this.gruaLoadedSpeed = gruaLoadedSpeed;
    }

    public int getRobotQuantity() {
        return robotQuantity;
    }

    public void setRobotQuantity(int robotQuantity) {
        this.robotQuantity = robotQuantity;
    }

    public double getRobotEmptySpeed() {
        return robotEmptySpeed;
    }

    public void setRobotEmptySpeed(double robotEmptySpeed) {
        this.robotEmptySpeed = robotEmptySpeed;
    }

    public double getRobotLoadedSpeed() {
        return robotLoadedSpeed;
    }

    public void setRobotLoadedSpeed(double robotLoadedSpeed) {
        this.robotLoadedSpeed = robotLoadedSpeed;
    }

    public String getArrivalDistribution() {
        return arrivalDistribution;
    }

    public void setArrivalDistribution(String arrivalDistribution) {
        this.arrivalDistribution = arrivalDistribution;
    }

    public double getArrivalMeanTime() {
        return arrivalMeanTime;
    }

    public void setArrivalMeanTime(double arrivalMeanTime) {
        this.arrivalMeanTime = arrivalMeanTime;
    }

    public double getArrivalFirstTime() {
        return arrivalFirstTime;
    }

    public void setArrivalFirstTime(double arrivalFirstTime) {
        this.arrivalFirstTime = arrivalFirstTime;
    }

    public int getArrivalMaxEntities() {
        return arrivalMaxEntities;
    }

    public void setArrivalMaxEntities(int arrivalMaxEntities) {
        this.arrivalMaxEntities = arrivalMaxEntities;
    }
}
