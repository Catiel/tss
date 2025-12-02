package com.simulation.config; // Declaración del paquete de configuración de la simulación

import java.util.HashMap; // Importa la clase HashMap para mapas clave-valor
import java.util.Map; // Importa la interfaz Map para manejo de mapas

/**
 * Clase que almacena toda la configuración parametrizable de la simulación
 */
public class SimulationConfig { // Define la clase de configuración de la simulación

    // ===== PARÁMETROS GENERALES =====
    private double simulationTime = 1000.05; // Tiempo total de ejecución de la simulación en horas
    private int numberOfReplicas = 3; // Número de réplicas de la simulación para análisis estadístico
    private double warmUpTime = 0.0; // Tiempo de calentamiento antes de recolectar estadísticas
    private long randomSeed = System.currentTimeMillis(); // Semilla para generador de números aleatorios basada en tiempo actual

    // ===== PARÁMETROS DE LOCACIONES =====
    private Map<String, Integer> locationCapacities = new HashMap<>(); // Mapa que almacena la capacidad máxima de cada ubicación
    private Map<String, Double> processingTimes = new HashMap<>(); // Mapa que almacena el tiempo de procesamiento de cada ubicación

    // HORNO específico
    private int hornoBatchSize = 10; // Tamaño del lote de procesamiento del horno
    private double hornoProcessingTime = 100.0; // Tiempo de procesamiento del horno en minutos

    // ===== PARÁMETROS DE RECURSOS =====
    private int gruaQuantity = 1; // Cantidad de grúas disponibles en la simulación
    private double gruaEmptySpeed = 25.0; // Velocidad de la grúa vacía en pies por minuto
    private double gruaLoadedSpeed = 25.0; // Velocidad de la grúa cargada en pies por minuto

    private int robotQuantity = 1; // Cantidad de robots disponibles en la simulación
    private double robotEmptySpeed = 45.0; // Velocidad del robot vacío en pies por minuto
    private double robotLoadedSpeed = 45.0; // Velocidad del robot cargado en pies por minuto

    // ===== PARÁMETROS DE ARRIBOS =====
    private String arrivalDistribution = "EXPONENTIAL"; // Tipo de distribución de llegadas: EXPONENTIAL o CONSTANT
    private double arrivalMeanTime = 5.0; // Tiempo promedio entre llegadas en minutos
    private double arrivalFirstTime = 0.0; // Tiempo de la primera llegada en la simulación
    private int arrivalMaxEntities = 12000; // Número máximo de entidades que llegarán

    // ===== PARÁMETROS DE INSPECCIÓN =====
    private boolean inspeccionExponential = true; // Flag que indica si la inspección usa distribución exponencial (true) o tiempo fijo (false)

    public SimulationConfig() { // Constructor de la clase de configuración
        initializeDefaults(); // Llama al método que inicializa valores por defecto
    }

    private void initializeDefaults() { // Método que establece los valores predeterminados de configuración
        // Capacidades por defecto
        locationCapacities.put("ALMACEN_MP", Integer.MAX_VALUE); // Establece capacidad infinita para almacén de materia prima
        locationCapacities.put("HORNO", 10); // Establece capacidad de 10 unidades para el horno
        locationCapacities.put("BANDA_1", Integer.MAX_VALUE); // Establece capacidad infinita para banda transportadora 1
        locationCapacities.put("CARGA", Integer.MAX_VALUE); // Establece capacidad infinita para área de carga
        locationCapacities.put("TORNEADO", 1); // Establece capacidad de 1 unidad para estación de torneado
        locationCapacities.put("FRESADO", 1); // Establece capacidad de 1 unidad para estación de fresado
        locationCapacities.put("TALADRO", 1); // Establece capacidad de 1 unidad para estación de taladro
        locationCapacities.put("RECTIFICADO", 1); // Establece capacidad de 1 unidad para estación de rectificado
        locationCapacities.put("DESCARGA", Integer.MAX_VALUE); // Establece capacidad infinita para área de descarga
        locationCapacities.put("BANDA_2", Integer.MAX_VALUE); // Establece capacidad infinita para banda transportadora 2
        locationCapacities.put("INSPECCION", 1); // Establece capacidad de 1 unidad para estación de inspección
        locationCapacities.put("SALIDA", Integer.MAX_VALUE); // Establece capacidad infinita para área de salida

        // Tiempos de procesamiento por defecto
        processingTimes.put("ALMACEN_MP", 0.0); // Tiempo de procesamiento cero para almacén de materia prima
        processingTimes.put("BANDA_1", 0.94); // Tiempo de procesamiento de 0.94 minutos en banda 1
        processingTimes.put("CARGA", 0.5); // Tiempo de procesamiento de 0.5 minutos en área de carga
        processingTimes.put("TORNEADO", 5.2); // Tiempo de procesamiento de 5.2 minutos en torneado
        processingTimes.put("FRESADO", 9.17); // Tiempo de procesamiento de 9.17 minutos en fresado
        processingTimes.put("TALADRO", 1.6); // Tiempo de procesamiento de 1.6 minutos en taladro
        processingTimes.put("RECTIFICADO", 2.85); // Tiempo de procesamiento de 2.85 minutos en rectificado
        processingTimes.put("DESCARGA", 0.5); // Tiempo de procesamiento de 0.5 minutos en área de descarga
        processingTimes.put("BANDA_2", 1.02); // Tiempo de procesamiento de 1.02 minutos en banda 2
        processingTimes.put("INSPECCION", 3.0); // Tiempo de procesamiento de 3.0 minutos en inspección
        processingTimes.put("SALIDA", 0.0); // Tiempo de procesamiento cero para área de salida
    }

    // ===== GETTERS Y SETTERS =====

    public double getSimulationTime() { // Método getter para obtener el tiempo de simulación
        return simulationTime; // Retorna el tiempo de simulación en horas
    }

    public void setSimulationTime(double simulationTime) { // Método setter para establecer el tiempo de simulación
        this.simulationTime = simulationTime; // Asigna el nuevo valor de tiempo de simulación
    }

    public int getNumberOfReplicas() { // Método getter para obtener el número de réplicas
        return numberOfReplicas; // Retorna la cantidad de réplicas configuradas
    }

    public void setNumberOfReplicas(int numberOfReplicas) { // Método setter para establecer el número de réplicas
        this.numberOfReplicas = numberOfReplicas; // Asigna el nuevo número de réplicas
    }

    public double getWarmUpTime() { // Método getter para obtener el tiempo de calentamiento
        return warmUpTime; // Retorna el tiempo de warm-up en horas
    }

    public void setWarmUpTime(double warmUpTime) { // Método setter para establecer el tiempo de calentamiento
        this.warmUpTime = warmUpTime; // Asigna el nuevo tiempo de warm-up
    }

    public long getRandomSeed() { // Método getter para obtener la semilla aleatoria
        return randomSeed; // Retorna la semilla del generador de números aleatorios
    }

    public void setRandomSeed(long randomSeed) { // Método setter para establecer la semilla aleatoria
        this.randomSeed = randomSeed; // Asigna la nueva semilla para reproducibilidad
    }

    public Map<String, Integer> getLocationCapacities() { // Método getter para obtener el mapa de capacidades
        return locationCapacities; // Retorna el mapa con todas las capacidades de ubicaciones
    }

    public Map<String, Double> getProcessingTimes() { // Método getter para obtener el mapa de tiempos de procesamiento
        return processingTimes; // Retorna el mapa con todos los tiempos de procesamiento
    }

    public int getHornoBatchSize() { // Método getter para obtener el tamaño de lote del horno
        return hornoBatchSize; // Retorna el tamaño del batch del horno
    }

    public void setHornoBatchSize(int hornoBatchSize) { // Método setter para establecer el tamaño de lote del horno
        this.hornoBatchSize = hornoBatchSize; // Asigna el nuevo tamaño de batch
    }

    public double getHornoProcessingTime() { // Método getter para obtener el tiempo de procesamiento del horno
        return hornoProcessingTime; // Retorna el tiempo de procesamiento en minutos
    }

    public void setHornoProcessingTime(double hornoProcessingTime) { // Método setter para establecer el tiempo de procesamiento del horno
        this.hornoProcessingTime = hornoProcessingTime; // Asigna el nuevo tiempo de procesamiento
    }

    public int getGruaQuantity() { // Método getter para obtener la cantidad de grúas
        return gruaQuantity; // Retorna el número de grúas disponibles
    }

    public void setGruaQuantity(int gruaQuantity) { // Método setter para establecer la cantidad de grúas
        this.gruaQuantity = gruaQuantity; // Asigna la nueva cantidad de grúas
    }

    public double getGruaEmptySpeed() { // Método getter para obtener la velocidad de grúa vacía
        return gruaEmptySpeed; // Retorna la velocidad en pies por minuto sin carga
    }

    public void setGruaEmptySpeed(double gruaEmptySpeed) { // Método setter para establecer la velocidad de grúa vacía
        this.gruaEmptySpeed = gruaEmptySpeed; // Asigna la nueva velocidad vacía
    }

    public double getGruaLoadedSpeed() { // Método getter para obtener la velocidad de grúa cargada
        return gruaLoadedSpeed; // Retorna la velocidad en pies por minuto con carga
    }

    public void setGruaLoadedSpeed(double gruaLoadedSpeed) { // Método setter para establecer la velocidad de grúa cargada
        this.gruaLoadedSpeed = gruaLoadedSpeed; // Asigna la nueva velocidad cargada
    }

    public int getRobotQuantity() { // Método getter para obtener la cantidad de robots
        return robotQuantity; // Retorna el número de robots disponibles
    }

    public void setRobotQuantity(int robotQuantity) { // Método setter para establecer la cantidad de robots
        this.robotQuantity = robotQuantity; // Asigna la nueva cantidad de robots
    }

    public double getRobotEmptySpeed() { // Método getter para obtener la velocidad de robot vacío
        return robotEmptySpeed; // Retorna la velocidad en pies por minuto sin carga
    }

    public void setRobotEmptySpeed(double robotEmptySpeed) { // Método setter para establecer la velocidad de robot vacío
        this.robotEmptySpeed = robotEmptySpeed; // Asigna la nueva velocidad vacía del robot
    }

    public double getRobotLoadedSpeed() { // Método getter para obtener la velocidad de robot cargado
        return robotLoadedSpeed; // Retorna la velocidad en pies por minuto con carga
    }

    public void setRobotLoadedSpeed(double robotLoadedSpeed) { // Método setter para establecer la velocidad de robot cargado
        this.robotLoadedSpeed = robotLoadedSpeed; // Asigna la nueva velocidad cargada del robot
    }

    public String getArrivalDistribution() { // Método getter para obtener el tipo de distribución de llegadas
        return arrivalDistribution; // Retorna el nombre de la distribución configurada
    }

    public void setArrivalDistribution(String arrivalDistribution) { // Método setter para establecer el tipo de distribución de llegadas
        this.arrivalDistribution = arrivalDistribution; // Asigna el nuevo tipo de distribución
    }

    public double getArrivalMeanTime() { // Método getter para obtener el tiempo promedio entre llegadas
        return arrivalMeanTime; // Retorna el tiempo medio de inter-arribo en minutos
    }

    public void setArrivalMeanTime(double arrivalMeanTime) { // Método setter para establecer el tiempo promedio entre llegadas
        this.arrivalMeanTime = arrivalMeanTime; // Asigna el nuevo tiempo medio de inter-arribo
    }

    public double getArrivalFirstTime() { // Método getter para obtener el tiempo de primera llegada
        return arrivalFirstTime; // Retorna el tiempo de la primera entidad en minutos
    }

    public void setArrivalFirstTime(double arrivalFirstTime) { // Método setter para establecer el tiempo de primera llegada
        this.arrivalFirstTime = arrivalFirstTime; // Asigna el nuevo tiempo de primera llegada
    }

    public int getArrivalMaxEntities() { // Método getter para obtener el máximo de entidades
        return arrivalMaxEntities; // Retorna el número máximo de entidades a generar
    }

    public void setArrivalMaxEntities(int arrivalMaxEntities) { // Método setter para establecer el máximo de entidades
        this.arrivalMaxEntities = arrivalMaxEntities; // Asigna el nuevo límite máximo de entidades
    }

    public boolean isInspeccionExponential() { // Método getter para verificar si inspección es exponencial
        return inspeccionExponential; // Retorna true si usa distribución exponencial
    }

    public void setInspeccionExponential(boolean inspeccionExponential) { // Método setter para establecer tipo de distribución de inspección
        this.inspeccionExponential = inspeccionExponential; // Asigna el flag de distribución exponencial
    }

    // ===== MÉTODOS DE UTILIDAD =====

    /**
     * Obtiene el tiempo de simulación en minutos
     */
    public double getSimulationTimeInMinutes() { // Método para convertir tiempo de simulación a minutos
        return simulationTime * 60.0; // Multiplica horas por 60 para obtener minutos
    }

    /**
     * Obtiene el tiempo de procesamiento para una locación
     */
    public double getProcessingTime(String locationName) { // Método para obtener tiempo de procesamiento de una ubicación específica
        return processingTimes.getOrDefault(locationName, 0.0); // Retorna el tiempo o 0.0 si no existe
    }

    /**
     * Establece el tiempo de procesamiento para una locación
     */
    public void setProcessingTime(String locationName, double time) { // Método para establecer tiempo de procesamiento de una ubicación
        processingTimes.put(locationName, time); // Almacena el tiempo en el mapa con la clave de ubicación
    }

    /**
     * Obtiene la capacidad de una locación
     */
    public int getLocationCapacity(String locationName) { // Método para obtener capacidad de una ubicación específica
        return locationCapacities.getOrDefault(locationName, 1); // Retorna la capacidad o 1 si no existe
    }

    /**
     * Establece la capacidad de una locación
     */
    public void setLocationCapacity(String locationName, int capacity) { // Método para establecer capacidad de una ubicación
        locationCapacities.put(locationName, capacity); // Almacena la capacidad en el mapa con la clave de ubicación
    }

    /**
     * Verifica si los arribos usan distribución exponencial
     */
    public boolean isArrivalExponential() { // Método para verificar si las llegadas usan distribución exponencial
        return "EXPONENTIAL".equalsIgnoreCase(arrivalDistribution); // Compara ignorando mayúsculas con el valor EXPONENTIAL
    }

    /**
     * Copia la configuración actual
     */
    public SimulationConfig copy() { // Método para crear una copia profunda de la configuración
        SimulationConfig copy = new SimulationConfig(); // Crea una nueva instancia de configuración
        copy.simulationTime = this.simulationTime; // Copia el tiempo de simulación
        copy.numberOfReplicas = this.numberOfReplicas; // Copia el número de réplicas
        copy.warmUpTime = this.warmUpTime; // Copia el tiempo de calentamiento
        copy.randomSeed = this.randomSeed; // Copia la semilla aleatoria
        copy.hornoBatchSize = this.hornoBatchSize; // Copia el tamaño de lote del horno
        copy.hornoProcessingTime = this.hornoProcessingTime; // Copia el tiempo de procesamiento del horno
        copy.gruaQuantity = this.gruaQuantity; // Copia la cantidad de grúas
        copy.gruaEmptySpeed = this.gruaEmptySpeed; // Copia la velocidad de grúa vacía
        copy.gruaLoadedSpeed = this.gruaLoadedSpeed; // Copia la velocidad de grúa cargada
        copy.robotQuantity = this.robotQuantity; // Copia la cantidad de robots
        copy.robotEmptySpeed = this.robotEmptySpeed; // Copia la velocidad de robot vacío
        copy.robotLoadedSpeed = this.robotLoadedSpeed; // Copia la velocidad de robot cargado
        copy.arrivalDistribution = this.arrivalDistribution; // Copia el tipo de distribución de llegadas
        copy.arrivalMeanTime = this.arrivalMeanTime; // Copia el tiempo promedio entre llegadas
        copy.arrivalFirstTime = this.arrivalFirstTime; // Copia el tiempo de primera llegada
        copy.arrivalMaxEntities = this.arrivalMaxEntities; // Copia el máximo de entidades
        copy.inspeccionExponential = this.inspeccionExponential; // Copia el flag de inspección exponencial
        copy.locationCapacities.putAll(this.locationCapacities); // Copia todos los pares clave-valor de capacidades
        copy.processingTimes.putAll(this.processingTimes); // Copia todos los pares clave-valor de tiempos de procesamiento
        return copy; // Retorna la nueva instancia con todos los valores copiados
    }

    @Override
    public String toString() { // Sobrescribe el método toString para representación en texto
        return "SimulationConfig{" + // Inicia la cadena con el nombre de la clase
                "simulationTime=" + simulationTime + " hrs" + // Concatena tiempo de simulación con unidad
                ", replicas=" + numberOfReplicas + // Concatena número de réplicas
                ", arrivals=" + arrivalDistribution + "(" + arrivalMeanTime + " min)" + // Concatena distribución y tiempo medio de llegadas
                ", maxEntities=" + arrivalMaxEntities + // Concatena máximo de entidades
                ", hornoBatch=" + hornoBatchSize + // Concatena tamaño de lote del horno
                ", hornoTime=" + hornoProcessingTime + // Concatena tiempo de procesamiento del horno
                "}"; // Cierra la cadena de representación
    }
}
