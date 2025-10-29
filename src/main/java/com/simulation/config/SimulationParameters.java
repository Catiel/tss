package com.simulation.config; // Declaración del paquete que contiene las clases de configuración de la simulación

public class SimulationParameters { // Declaración de la clase pública que almacena todos los parámetros de la simulación
    // Duración de la simulación: 90 días x 24 horas x 60 minutos = 129,600 minutos
    private double simulationDurationMinutes = 129600.0; // CORREGIDO: 90 días completos // Variable privada que define la duración total de la simulación en minutos (90 días)

    // Semilla aleatoria base
    private long baseRandomSeed = 12345L; // Variable privada que almacena la semilla para el generador de números aleatorios, garantizando reproducibilidad

    // Capacidades de locaciones
    private int lavadoraCapacity = 5; // Variable privada que define la capacidad máxima de piezas que puede procesar la lavadora simultáneamente
    private int almacenPinturaCapacity = 10; // Variable privada que define la capacidad máxima de piezas que puede almacenar el almacén de pintura
    private int pinturaCapacity = 3; // Variable privada que define la capacidad máxima de piezas que puede procesar la estación de pintura simultáneamente
    private int almacenHornoCapacity = 10; // Variable privada que define la capacidad máxima de piezas que puede almacenar el almacén del horno
    private int hornoCapacity = 1; // Variable privada que define la capacidad del horno (procesa una pieza a la vez)
    private int inspeccionNumStations = 2; // Variable privada que define el número de estaciones de inspección disponibles
    private int inspeccionOperationsPerPiece = 3; // Variable privada que define el número de operaciones de inspección requeridas por cada pieza

    // Parámetros de arribos: E(2) = Exponencial con media 2
    private double arrivalMeanTime = 2.0; // Variable privada que define el tiempo medio entre arribos de piezas siguiendo distribución exponencial (en minutos)

    // Transporte RECEPCION -> LAVADORA: MOVE FOR E(3)
    private double transportRecepcionLavadoraMean = 3.0; // Variable privada que define el tiempo medio de transporte desde recepción hasta lavadora con distribución exponencial (en minutos)

    // Transporte LAVADORA -> ALMACEN_PINTURA: MOVE FOR E(2)
    private double transportLavadoraAlmacenMean = 2.0; // Variable privada que define el tiempo medio de transporte desde lavadora hasta almacén de pintura con distribución exponencial (en minutos)

    // Proceso LAVADORA: WAIT N(10, 2)min = Normal(media=10, desv=2)
    private double lavadoraProcessMean = 10.0; // Variable privada que define la media del tiempo de procesamiento en la lavadora con distribución normal (en minutos)
    private double lavadoraProcessStdDev = 2.0; // Variable privada que define la desviación estándar del tiempo de procesamiento en la lavadora (en minutos)

    // Proceso PINTURA: WAIT T(4, 8, 10)min = Triangular(min=4, mode=8, max=10)
    private double pinturaProcessMin = 4.0; // Variable privada que define el tiempo mínimo de procesamiento en pintura con distribución triangular (en minutos)
    private double pinturaProcessMode = 8.0; // Variable privada que define el tiempo más frecuente (moda) de procesamiento en pintura con distribución triangular (en minutos)
    private double pinturaProcessMax = 10.0; // Variable privada que define el tiempo máximo de procesamiento en pintura con distribución triangular (en minutos)

    // Transporte PINTURA -> ALMACEN_HORNO: MOVE FOR U(3.5, 1.5)
    // U(3.5, 1.5) = Uniforme con media 3.5 y half-width 1.5
    // Rango: [3.5-1.5, 3.5+1.5] = [2.0, 5.0]
    private double transportPinturaAlmacenMin = 2.0; // Variable privada que define el tiempo mínimo de transporte desde pintura hasta almacén del horno con distribución uniforme (en minutos)
    private double transportPinturaAlmacenMax = 5.0; // Variable privada que define el tiempo máximo de transporte desde pintura hasta almacén del horno con distribución uniforme (en minutos)

    // Proceso HORNO: WAIT U(3, 1)min
    // U(3, 1) = Uniforme con media 3 y half-width 1
    // Rango: [3-1, 3+1] = [2.0, 4.0]
    private double hornoProcessMin = 2.0; // Variable privada que define el tiempo mínimo de procesamiento en el horno con distribución uniforme (en minutos)
    private double hornoProcessMax = 4.0; // Variable privada que define el tiempo máximo de procesamiento en el horno con distribución uniforme (en minutos)

    // Transporte HORNO -> INSPECCION: MOVE FOR U(2, 1)
    // U(2, 1) = Uniforme con media 2 y half-width 1
    // Rango: [2-1, 2+1] = [1.0, 3.0]
    private double transportHornoInspeccionMin = 1.0; // Variable privada que define el tiempo mínimo de transporte desde horno hasta inspección con distribución uniforme (en minutos)
    private double transportHornoInspeccionMax = 3.0; // Variable privada que define el tiempo máximo de transporte desde horno hasta inspección con distribución uniforme (en minutos)

    // Operaciones de INSPECCION: WAIT E(2) por cada operación
    private double inspeccionOperationMean = 2.0; // Variable privada que define el tiempo medio por cada operación de inspección con distribución exponencial (en minutos)

    // Getters y Setters
    public double getSimulationDurationMinutes() { // Método público getter que retorna la duración de la simulación en minutos
        return simulationDurationMinutes; // Retorna el valor de la variable simulationDurationMinutes
    } // Cierre del método getSimulationDurationMinutes

    public void setSimulationDurationMinutes(double simulationDurationMinutes) { // Método público setter que permite modificar la duración de la simulación recibiendo un parámetro double
        this.simulationDurationMinutes = simulationDurationMinutes; // Asigna el valor del parámetro recibido a la variable de instancia simulationDurationMinutes
    } // Cierre del método setSimulationDurationMinutes

    public long getBaseRandomSeed() { // Método público getter que retorna la semilla aleatoria base de tipo long
        return baseRandomSeed; // Retorna el valor de la variable baseRandomSeed
    } // Cierre del método getBaseRandomSeed

    public void setBaseRandomSeed(long baseRandomSeed) { // Método público setter que permite modificar la semilla aleatoria base recibiendo un parámetro long
        this.baseRandomSeed = baseRandomSeed; // Asigna el valor del parámetro recibido a la variable de instancia baseRandomSeed
    } // Cierre del método setBaseRandomSeed

    public int getLavadoraCapacity() { // Método público getter que retorna la capacidad de la lavadora de tipo int
        return lavadoraCapacity; // Retorna el valor de la variable lavadoraCapacity
    } // Cierre del método getLavadoraCapacity

    public int getAlmacenPinturaCapacity() { // Método público getter que retorna la capacidad del almacén de pintura de tipo int
        return almacenPinturaCapacity; // Retorna el valor de la variable almacenPinturaCapacity
    } // Cierre del método getAlmacenPinturaCapacity

    public int getPinturaCapacity() { // Método público getter que retorna la capacidad de la estación de pintura de tipo int
        return pinturaCapacity; // Retorna el valor de la variable pinturaCapacity
    } // Cierre del método getPinturaCapacity

    public int getAlmacenHornoCapacity() { // Método público getter que retorna la capacidad del almacén del horno de tipo int
        return almacenHornoCapacity; // Retorna el valor de la variable almacenHornoCapacity
    } // Cierre del método getAlmacenHornoCapacity

    public int getHornoCapacity() { // Método público getter que retorna la capacidad del horno de tipo int
        return hornoCapacity; // Retorna el valor de la variable hornoCapacity
    } // Cierre del método getHornoCapacity

    public int getInspeccionNumStations() { // Método público getter que retorna el número de estaciones de inspección de tipo int
        return inspeccionNumStations; // Retorna el valor de la variable inspeccionNumStations
    } // Cierre del método getInspeccionNumStations

    public int getInspeccionOperationsPerPiece() { // Método público getter que retorna el número de operaciones de inspección por pieza de tipo int
        return inspeccionOperationsPerPiece; // Retorna el valor de la variable inspeccionOperationsPerPiece
    } // Cierre del método getInspeccionOperationsPerPiece

    public double getArrivalMeanTime() { // Método público getter que retorna el tiempo medio entre arribos de tipo double
        return arrivalMeanTime; // Retorna el valor de la variable arrivalMeanTime
    } // Cierre del método getArrivalMeanTime

    public double getTransportRecepcionLavadoraMean() { // Método público getter que retorna el tiempo medio de transporte de recepción a lavadora de tipo double
        return transportRecepcionLavadoraMean; // Retorna el valor de la variable transportRecepcionLavadoraMean
    } // Cierre del método getTransportRecepcionLavadoraMean

    public double getTransportLavadoraAlmacenMean() { // Método público getter que retorna el tiempo medio de transporte de lavadora a almacén de tipo double
        return transportLavadoraAlmacenMean; // Retorna el valor de la variable transportLavadoraAlmacenMean
    } // Cierre del método getTransportLavadoraAlmacenMean

    public double getLavadoraProcessMean() { // Método público getter que retorna la media del tiempo de proceso en lavadora de tipo double
        return lavadoraProcessMean; // Retorna el valor de la variable lavadoraProcessMean
    } // Cierre del método getLavadoraProcessMean

    public double getLavadoraProcessStdDev() { // Método público getter que retorna la desviación estándar del tiempo de proceso en lavadora de tipo double
        return lavadoraProcessStdDev; // Retorna el valor de la variable lavadoraProcessStdDev
    } // Cierre del método getLavadoraProcessStdDev

    public double getPinturaProcessMin() { // Método público getter que retorna el tiempo mínimo de proceso en pintura de tipo double
        return pinturaProcessMin; // Retorna el valor de la variable pinturaProcessMin
    } // Cierre del método getPinturaProcessMin

    public double getPinturaProcessMode() { // Método público getter que retorna la moda del tiempo de proceso en pintura de tipo double
        return pinturaProcessMode; // Retorna el valor de la variable pinturaProcessMode
    } // Cierre del método getPinturaProcessMode

    public double getPinturaProcessMax() { // Método público getter que retorna el tiempo máximo de proceso en pintura de tipo double
        return pinturaProcessMax; // Retorna el valor de la variable pinturaProcessMax
    } // Cierre del método getPinturaProcessMax

    public double getTransportPinturaAlmacenMin() { // Método público getter que retorna el tiempo mínimo de transporte de pintura a almacén de tipo double
        return transportPinturaAlmacenMin; // Retorna el valor de la variable transportPinturaAlmacenMin
    } // Cierre del método getTransportPinturaAlmacenMin

    public double getTransportPinturaAlmacenMax() { // Método público getter que retorna el tiempo máximo de transporte de pintura a almacén de tipo double
        return transportPinturaAlmacenMax; // Retorna el valor de la variable transportPinturaAlmacenMax
    } // Cierre del método getTransportPinturaAlmacenMax

    public double getHornoProcessMin() { // Método público getter que retorna el tiempo mínimo de proceso en horno de tipo double
        return hornoProcessMin; // Retorna el valor de la variable hornoProcessMin
    } // Cierre del método getHornoProcessMin

    public double getHornoProcessMax() { // Método público getter que retorna el tiempo máximo de proceso en horno de tipo double
        return hornoProcessMax; // Retorna el valor de la variable hornoProcessMax
    } // Cierre del método getHornoProcessMax

    public double getTransportHornoInspeccionMin() { // Método público getter que retorna el tiempo mínimo de transporte de horno a inspección de tipo double
        return transportHornoInspeccionMin; // Retorna el valor de la variable transportHornoInspeccionMin
    } // Cierre del método getTransportHornoInspeccionMin

    public double getTransportHornoInspeccionMax() { // Método público getter que retorna el tiempo máximo de transporte de horno a inspección de tipo double
        return transportHornoInspeccionMax; // Retorna el valor de la variable transportHornoInspeccionMax
    } // Cierre del método getTransportHornoInspeccionMax

    public double getInspeccionOperationMean() { // Método público getter que retorna el tiempo medio de cada operación de inspección de tipo double
        return inspeccionOperationMean; // Retorna el valor de la variable inspeccionOperationMean
    } // Cierre del método getInspeccionOperationMean
} // Cierre de la clase SimulationParameters
