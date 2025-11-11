package com.simulation.config; // Declaración del paquete que contiene las clases de configuración de la simulación

/** // Inicio del comentario Javadoc de la clase
 * Parámetros de configuración para el sistema Multi-Engrane // Descripción de la clase
 * Sistema de fabricación de engranes con 12 locaciones // Detalle indicando que es un sistema con 12 locaciones diferentes
 */ // Fin del comentario Javadoc
public class SimulationParameters { // Declaración de la clase pública SimulationParameters que almacena todos los parámetros de configuración del sistema de fabricación de engranes
    // Duración: 60 horas = 3,600 minutos
    private double simulationDurationMinutes = 3600.0; // Variable privada que almacena la duración total de la simulación en minutos, inicializada en 3600 minutos (60 horas)

    // Semilla aleatoria
    private long baseRandomSeed = 12345L; // Variable privada que almacena la semilla base para el generador de números aleatorios, inicializada en 12345 para reproducibilidad

    // === CAPACIDADES DE LAS 12 LOCACIONES ===
    private int conveyor1Capacity = Integer.MAX_VALUE;  // CONVEYOR_1 - capacidad infinita // Variable privada que almacena la capacidad del primer conveyor, inicializada en Integer.MAX_VALUE para representar capacidad infinita
    private int almacenCapacity = 10; // Variable privada que almacena la capacidad del almacén, inicializada en 10 unidades
    private int cortadoraCapacity = 1; // Variable privada que almacena la capacidad de la cortadora, inicializada en 1 unidad (solo puede procesar una pieza a la vez)
    private int tornoCapacity = 2; // Variable privada que almacena la capacidad del torno, inicializada en 2 unidades (puede procesar dos piezas simultáneamente)
    private int conveyor2Capacity = Integer.MAX_VALUE;  // CONVEYOR_2 - capacidad infinita // Variable privada que almacena la capacidad del segundo conveyor, inicializada en Integer.MAX_VALUE para representar capacidad infinita
    private int fresadoraCapacity = 2; // Variable privada que almacena la capacidad de la fresadora, inicializada en 2 unidades (puede procesar dos piezas simultáneamente)
    private int almacen2Capacity = 10; // Variable privada que almacena la capacidad del segundo almacén, inicializada en 10 unidades
    private int pinturaCapacity = 4; // Variable privada que almacena la capacidad de la estación de pintura, inicializada en 4 unidades (puede procesar cuatro piezas simultáneamente)
    private int inspeccion1Capacity = 2; // Variable privada que almacena la capacidad de la primera estación de inspección, inicializada en 2 unidades
    private int inspeccion2Capacity = 1; // Variable privada que almacena la capacidad de la segunda estación de inspección, inicializada en 1 unidad
    private int empaqueCapacity = 1; // Variable privada que almacena la capacidad de la estación de empaque, inicializada en 1 unidad
    private int embarqueCapacity = 3; // Variable privada que almacena la capacidad de la estación de embarque, inicializada en 3 unidades

    // === PARÁMETROS DE ARRIBOS Y TRANSPORTES ===
    private double arrivalMeanTime = 15.0;  // Tiempo fijo entre arribos // Variable privada que almacena el tiempo promedio entre arribos de nuevas piezas, inicializada en 15.0 minutos
    private double conveyor1Time = 4.0;      // Tiempo fijo CONVEYOR_1 // Variable privada que almacena el tiempo fijo de transporte en el primer conveyor, inicializada en 4.0 minutos
    private double conveyor2Time = 4.0;      // Tiempo fijo CONVEYOR_2 // Variable privada que almacena el tiempo fijo de transporte en el segundo conveyor, inicializada en 4.0 minutos
    private double transportWorkerTime = 0.1; // Tiempo fijo transporte trabajador // Variable privada que almacena el tiempo fijo de transporte cuando un trabajador mueve una pieza, inicializada en 0.1 minutos

    // === PROCESOS según ProModel ===
    // ALMACEN: WAIT N(5, 0.5) min
    private double almacenProcessMean = 5.0; // Variable privada que almacena la media del tiempo de proceso en el almacén usando distribución normal, inicializada en 5.0 minutos
    private double almacenProcessStdDev = 0.5; // Variable privada que almacena la desviación estándar del tiempo de proceso en el almacén, inicializada en 0.5 minutos

    // CORTADORA: WAIT E(3) min
    private double cortadoraProcessMean = 3.0; // Variable privada que almacena la media del tiempo de proceso en la cortadora usando distribución exponencial, inicializada en 3.0 minutos

    // TORNO: WAIT N(5, 0.5) min
    private double tornoProcessMean = 5.0; // Variable privada que almacena la media del tiempo de proceso en el torno usando distribución normal, inicializada en 5.0 minutos
    private double tornoProcessStdDev = 0.5; // Variable privada que almacena la desviación estándar del tiempo de proceso en el torno, inicializada en 0.5 minutos

    // FRESADORA: WAIT E(3) min
    private double fresadoraProcessMean = 3.0; // Variable privada que almacena la media del tiempo de proceso en la fresadora usando distribución exponencial, inicializada en 3.0 minutos

    // ALMACEN_2: WAIT N(5, 0.5) min
    private double almacen2ProcessMean = 5.0; // Variable privada que almacena la media del tiempo de proceso en el segundo almacén usando distribución normal, inicializada en 5.0 minutos
    private double almacen2ProcessStdDev = 0.5; // Variable privada que almacena la desviación estándar del tiempo de proceso en el segundo almacén, inicializada en 0.5 minutos

    // PINTURA: WAIT E(3) min
    private double pinturaProcessMean = 3.0; // Variable privada que almacena la media del tiempo de proceso en pintura usando distribución exponencial, inicializada en 3.0 minutos

    // INSPECCION_1: WAIT N(5, 0.5) min
    private double inspeccion1ProcessMean = 5.0; // Variable privada que almacena la media del tiempo de proceso en la primera inspección usando distribución normal, inicializada en 5.0 minutos
    private double inspeccion1ProcessStdDev = 0.5; // Variable privada que almacena la desviación estándar del tiempo de proceso en la primera inspección, inicializada en 0.5 minutos

    // INSPECCION_2: WAIT E(3) min
    private double inspeccion2ProcessMean = 3.0; // Variable privada que almacena la media del tiempo de proceso en la segunda inspección usando distribución exponencial, inicializada en 3.0 minutos

    // EMPAQUE: WAIT N(5, 0.5) min
    private double empaqueProcessMean = 5.0; // Variable privada que almacena la media del tiempo de proceso en empaque usando distribución normal, inicializada en 5.0 minutos
    private double empaqueProcessStdDev = 0.5; // Variable privada que almacena la desviación estándar del tiempo de proceso en empaque, inicializada en 0.5 minutos

    // EMBARQUE: WAIT E(3) min
    private double embarqueProcessMean = 3.0; // Variable privada que almacena la media del tiempo de proceso en embarque usando distribución exponencial, inicializada en 3.0 minutos

    // === PROBABILIDADES DE ROUTING ===
    private double inspeccion1ToEmpaqueProb = 0.80;   // 80% a EMPAQUE // Variable privada que almacena la probabilidad de que una pieza vaya de INSPECCION_1 a EMPAQUE, inicializada en 0.80 (80%)
    private double inspeccion1ToInspeccion2Prob = 0.20; // 20% a INSPECCION_2 // Variable privada que almacena la probabilidad de que una pieza vaya de INSPECCION_1 a INSPECCION_2, inicializada en 0.20 (20%)

    // === GETTERS ===

    public double getSimulationDurationMinutes() { // Método público getter que retorna la duración de la simulación en minutos de tipo double
        return simulationDurationMinutes; // Retorna el valor de la variable simulationDurationMinutes
    } // Cierre del método getSimulationDurationMinutes

    public void setSimulationDurationMinutes(double simulationDurationMinutes) { // Método público setter que establece la duración de la simulación recibiendo el nuevo valor como parámetro
        this.simulationDurationMinutes = simulationDurationMinutes; // Asigna el valor recibido a la variable de instancia simulationDurationMinutes
    } // Cierre del método setSimulationDurationMinutes

    public long getBaseRandomSeed() { // Método público getter que retorna la semilla aleatoria base de tipo long
        return baseRandomSeed; // Retorna el valor de la variable baseRandomSeed
    } // Cierre del método getBaseRandomSeed

    public void setBaseRandomSeed(long baseRandomSeed) { // Método público setter que establece la semilla aleatoria base recibiendo el nuevo valor como parámetro
        this.baseRandomSeed = baseRandomSeed; // Asigna el valor recibido a la variable de instancia baseRandomSeed
    } // Cierre del método setBaseRandomSeed

    // Capacidades (12 getters)
    public int getConveyor1Capacity() { return conveyor1Capacity; } // Método público getter de una línea que retorna la capacidad del primer conveyor de tipo int
    public int getAlmacenCapacity() { return almacenCapacity; } // Método público getter de una línea que retorna la capacidad del almacén de tipo int
    public int getCortadoraCapacity() { return cortadoraCapacity; } // Método público getter de una línea que retorna la capacidad de la cortadora de tipo int
    public int getTornoCapacity() { return tornoCapacity; } // Método público getter de una línea que retorna la capacidad del torno de tipo int
    public int getConveyor2Capacity() { return conveyor2Capacity; } // Método público getter de una línea que retorna la capacidad del segundo conveyor de tipo int
    public int getFresadoraCapacity() { return fresadoraCapacity; } // Método público getter de una línea que retorna la capacidad de la fresadora de tipo int
    public int getAlmacen2Capacity() { return almacen2Capacity; } // Método público getter de una línea que retorna la capacidad del segundo almacén de tipo int
    public int getPinturaCapacity() { return pinturaCapacity; } // Método público getter de una línea que retorna la capacidad de la estación de pintura de tipo int
    public int getInspeccion1Capacity() { return inspeccion1Capacity; } // Método público getter de una línea que retorna la capacidad de la primera inspección de tipo int
    public int getInspeccion2Capacity() { return inspeccion2Capacity; } // Método público getter de una línea que retorna la capacidad de la segunda inspección de tipo int
    public int getEmpaqueCapacity() { return empaqueCapacity; } // Método público getter de una línea que retorna la capacidad de la estación de empaque de tipo int
    public int getEmbarqueCapacity() { return embarqueCapacity; } // Método público getter de una línea que retorna la capacidad de la estación de embarque de tipo int

    // Arribos y transportes (4 getters)
    public double getArrivalMeanTime() { return arrivalMeanTime; } // Método público getter de una línea que retorna el tiempo promedio entre arribos de tipo double
    public double getConveyor1Time() { return conveyor1Time; } // Método público getter de una línea que retorna el tiempo de transporte del primer conveyor de tipo double
    public double getConveyor2Time() { return conveyor2Time; } // Método público getter de una línea que retorna el tiempo de transporte del segundo conveyor de tipo double
    public double getTransportWorkerTime() { return transportWorkerTime; } // Método público getter de una línea que retorna el tiempo de transporte del trabajador de tipo double

    // Procesos - getters según ProModel
    public double getAlmacenProcessMean() { return almacenProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en el almacén de tipo double
    public double getAlmacenProcessStdDev() { return almacenProcessStdDev; } // Método público getter de una línea que retorna la desviación estándar del tiempo de proceso en el almacén de tipo double
    public double getCortadoraProcessMean() { return cortadoraProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en la cortadora de tipo double
    public double getTornoProcessMean() { return tornoProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en el torno de tipo double
    public double getTornoProcessStdDev() { return tornoProcessStdDev; } // Método público getter de una línea que retorna la desviación estándar del tiempo de proceso en el torno de tipo double
    public double getFresadoraProcessMean() { return fresadoraProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en la fresadora de tipo double
    public double getAlmacen2ProcessMean() { return almacen2ProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en el segundo almacén de tipo double
    public double getAlmacen2ProcessStdDev() { return almacen2ProcessStdDev; } // Método público getter de una línea que retorna la desviación estándar del tiempo de proceso en el segundo almacén de tipo double
    public double getPinturaProcessMean() { return pinturaProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en pintura de tipo double
    public double getInspeccion1ProcessMean() { return inspeccion1ProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en la primera inspección de tipo double
    public double getInspeccion1ProcessStdDev() { return inspeccion1ProcessStdDev; } // Método público getter de una línea que retorna la desviación estándar del tiempo de proceso en la primera inspección de tipo double
    public double getInspeccion2ProcessMean() { return inspeccion2ProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en la segunda inspección de tipo double
    public double getEmpaqueProcessMean() { return empaqueProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en empaque de tipo double
    public double getEmpaqueProcessStdDev() { return empaqueProcessStdDev; } // Método público getter de una línea que retorna la desviación estándar del tiempo de proceso en empaque de tipo double
    public double getEmbarqueProcessMean() { return embarqueProcessMean; } // Método público getter de una línea que retorna la media del tiempo de proceso en embarque de tipo double

    // Probabilidades de routing (2 getters)
    public double getInspeccion1ToEmpaqueProb() { return inspeccion1ToEmpaqueProb; } // Método público getter de una línea que retorna la probabilidad de ir de INSPECCION_1 a EMPAQUE de tipo double
    public double getInspeccion1ToInspeccion2Prob() { return inspeccion1ToInspeccion2Prob; } // Método público getter de una línea que retorna la probabilidad de ir de INSPECCION_1 a INSPECCION_2 de tipo double

    // === SETTERS ===

    public void setConveyor1Capacity(int value) { this.conveyor1Capacity = value; } // Método público setter de una línea que establece la capacidad del primer conveyor recibiendo el nuevo valor como parámetro
    public void setAlmacenCapacity(int value) { this.almacenCapacity = value; } // Método público setter de una línea que establece la capacidad del almacén recibiendo el nuevo valor como parámetro
    public void setCortadoraCapacity(int value) { this.cortadoraCapacity = value; } // Método público setter de una línea que establece la capacidad de la cortadora recibiendo el nuevo valor como parámetro
    public void setTornoCapacity(int value) { this.tornoCapacity = value; } // Método público setter de una línea que establece la capacidad del torno recibiendo el nuevo valor como parámetro
    public void setConveyor2Capacity(int value) { this.conveyor2Capacity = value; } // Método público setter de una línea que establece la capacidad del segundo conveyor recibiendo el nuevo valor como parámetro
    public void setFresadoraCapacity(int value) { this.fresadoraCapacity = value; } // Método público setter de una línea que establece la capacidad de la fresadora recibiendo el nuevo valor como parámetro
    public void setAlmacen2Capacity(int value) { this.almacen2Capacity = value; } // Método público setter de una línea que establece la capacidad del segundo almacén recibiendo el nuevo valor como parámetro
    public void setPinturaCapacity(int value) { this.pinturaCapacity = value; } // Método público setter de una línea que establece la capacidad de la estación de pintura recibiendo el nuevo valor como parámetro
    public void setInspeccion1Capacity(int value) { this.inspeccion1Capacity = value; } // Método público setter de una línea que establece la capacidad de la primera inspección recibiendo el nuevo valor como parámetro
    public void setInspeccion2Capacity(int value) { this.inspeccion2Capacity = value; } // Método público setter de una línea que establece la capacidad de la segunda inspección recibiendo el nuevo valor como parámetro
    public void setEmpaqueCapacity(int value) { this.empaqueCapacity = value; } // Método público setter de una línea que establece la capacidad de la estación de empaque recibiendo el nuevo valor como parámetro
    public void setEmbarqueCapacity(int value) { this.embarqueCapacity = value; } // Método público setter de una línea que establece la capacidad de la estación de embarque recibiendo el nuevo valor como parámetro

    public void setArrivalMeanTime(double value) { this.arrivalMeanTime = value; } // Método público setter de una línea que establece el tiempo promedio entre arribos recibiendo el nuevo valor como parámetro
    public void setConveyor1Time(double value) { this.conveyor1Time = value; } // Método público setter de una línea que establece el tiempo de transporte del primer conveyor recibiendo el nuevo valor como parámetro
    public void setConveyor2Time(double value) { this.conveyor2Time = value; } // Método público setter de una línea que establece el tiempo de transporte del segundo conveyor recibiendo el nuevo valor como parámetro
    public void setTransportWorkerTime(double value) { this.transportWorkerTime = value; } // Método público setter de una línea que establece el tiempo de transporte del trabajador recibiendo el nuevo valor como parámetro

    public void setAlmacenProcessMean(double value) { this.almacenProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en el almacén recibiendo el nuevo valor como parámetro
    public void setAlmacenProcessStdDev(double value) { this.almacenProcessStdDev = value; } // Método público setter de una línea que establece la desviación estándar del tiempo de proceso en el almacén recibiendo el nuevo valor como parámetro
    public void setCortadoraProcessMean(double value) { this.cortadoraProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en la cortadora recibiendo el nuevo valor como parámetro
    public void setTornoProcessMean(double value) { this.tornoProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en el torno recibiendo el nuevo valor como parámetro
    public void setTornoProcessStdDev(double value) { this.tornoProcessStdDev = value; } // Método público setter de una línea que establece la desviación estándar del tiempo de proceso en el torno recibiendo el nuevo valor como parámetro
    public void setFresadoraProcessMean(double value) { this.fresadoraProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en la fresadora recibiendo el nuevo valor como parámetro
    public void setAlmacen2ProcessMean(double value) { this.almacen2ProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en el segundo almacén recibiendo el nuevo valor como parámetro
    public void setAlmacen2ProcessStdDev(double value) { this.almacen2ProcessStdDev = value; } // Método público setter de una línea que establece la desviación estándar del tiempo de proceso en el segundo almacén recibiendo el nuevo valor como parámetro
    public void setPinturaProcessMean(double value) { this.pinturaProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en pintura recibiendo el nuevo valor como parámetro
    public void setInspeccion1ProcessMean(double value) { this.inspeccion1ProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en la primera inspección recibiendo el nuevo valor como parámetro
    public void setInspeccion1ProcessStdDev(double value) { this.inspeccion1ProcessStdDev = value; } // Método público setter de una línea que establece la desviación estándar del tiempo de proceso en la primera inspección recibiendo el nuevo valor como parámetro
    public void setInspeccion2ProcessMean(double value) { this.inspeccion2ProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en la segunda inspección recibiendo el nuevo valor como parámetro
    public void setEmpaqueProcessMean(double value) { this.empaqueProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en empaque recibiendo el nuevo valor como parámetro
    public void setEmpaqueProcessStdDev(double value) { this.empaqueProcessStdDev = value; } // Método público setter de una línea que establece la desviación estándar del tiempo de proceso en empaque recibiendo el nuevo valor como parámetro
    public void setEmbarqueProcessMean(double value) { this.embarqueProcessMean = value; } // Método público setter de una línea que establece la media del tiempo de proceso en embarque recibiendo el nuevo valor como parámetro

    public void setInspeccion1ToEmpaqueProb(double value) { this.inspeccion1ToEmpaqueProb = value; } // Método público setter de una línea que establece la probabilidad de ir de INSPECCION_1 a EMPAQUE recibiendo el nuevo valor como parámetro
    public void setInspeccion1ToInspeccion2Prob(double value) { this.inspeccion1ToInspeccion2Prob = value; } // Método público setter de una línea que establece la probabilidad de ir de INSPECCION_1 a INSPECCION_2 recibiendo el nuevo valor como parámetro
} // Cierre de la clase SimulationParameters
