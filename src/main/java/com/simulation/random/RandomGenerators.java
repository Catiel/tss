package com.simulation.random; // Declaración del paquete que contiene las clases generadoras de números aleatorios para la simulación

import java.util.Random; // Importa la clase Random de Java para generar números aleatorios

public class RandomGenerators { // Declaración de la clase pública RandomGenerators que genera números aleatorios con diferentes distribuciones según los parámetros de ProModel
    private Random random; // Variable privada que almacena la instancia de Random para generar números aleatorios

    // Parámetros de la simulación según ProModel
    private double arrivalMeanTime; // Variable privada que almacena el tiempo promedio entre arribos de entidades
    private double conveyor1Time; // Variable privada que almacena el tiempo fijo de transporte en el primer conveyor
    private double conveyor2Time; // Variable privada que almacena el tiempo fijo de transporte en el segundo conveyor
    private double transportWorkerTime; // Variable privada que almacena el tiempo de transporte promedio de los trabajadores
    private double almacenProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en el almacén
    private double almacenProcessStdDev; // Variable privada que almacena la desviación estándar del tiempo de procesamiento en el almacén
    private double cortadoraProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en la cortadora
    private double tornoProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en el torno
    private double tornoProcessStdDev; // Variable privada que almacena la desviación estándar del tiempo de procesamiento en el torno
    private double fresadoraProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en la fresadora
    private double almacen2ProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en el segundo almacén
    private double almacen2ProcessStdDev; // Variable privada que almacena la desviación estándar del tiempo de procesamiento en el segundo almacén
    private double pinturaProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en pintura
    private double inspeccion1ProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en la primera inspección
    private double inspeccion1ProcessStdDev; // Variable privada que almacena la desviación estándar del tiempo de procesamiento en la primera inspección
    private double inspeccion2ProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en la segunda inspección
    private double empaqueProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en empaque
    private double empaqueProcessStdDev; // Variable privada que almacena la desviación estándar del tiempo de procesamiento en empaque
    private double embarqueProcessMean; // Variable privada que almacena la media del tiempo de procesamiento en embarque
    private double inspeccion1ToEmpaqueProb; // Variable privada que almacena la probabilidad de routing de inspección 1 a empaque

    public RandomGenerators(long seed) { // Constructor público que inicializa el generador de números aleatorios recibiendo una semilla como parámetro
        this.random = new Random(seed); // Crea una nueva instancia de Random con la semilla especificada para reproducibilidad
    } // Cierre del constructor RandomGenerators

    public void initialize( // Método público que inicializa todos los parámetros de distribuciones de probabilidad recibiendo todos los parámetros como argumentos sin retorno
            double arrivalMeanTime, // Parámetro que contiene el tiempo promedio entre arribos
            double conveyor1Time, // Parámetro que contiene el tiempo fijo de conveyor 1
            double conveyor2Time, // Parámetro que contiene el tiempo fijo de conveyor 2
            double transportWorkerTime, // Parámetro que contiene el tiempo de transporte de trabajadores
            double almacenProcessMean, // Parámetro que contiene la media del proceso de almacén
            double almacenProcessStdDev, // Parámetro que contiene la desviación estándar del proceso de almacén
            double cortadoraProcessMean, // Parámetro que contiene la media del proceso de cortadora
            double tornoProcessMean, // Parámetro que contiene la media del proceso de torno
            double tornoProcessStdDev, // Parámetro que contiene la desviación estándar del proceso de torno
            double fresadoraProcessMean, // Parámetro que contiene la media del proceso de fresadora
            double almacen2ProcessMean, // Parámetro que contiene la media del proceso de almacén 2
            double almacen2ProcessStdDev, // Parámetro que contiene la desviación estándar del proceso de almacén 2
            double pinturaProcessMean, // Parámetro que contiene la media del proceso de pintura
            double inspeccion1ProcessMean, // Parámetro que contiene la media del proceso de inspección 1
            double inspeccion1ProcessStdDev, // Parámetro que contiene la desviación estándar del proceso de inspección 1
            double inspeccion2ProcessMean, // Parámetro que contiene la media del proceso de inspección 2
            double empaqueProcessMean, // Parámetro que contiene la media del proceso de empaque
            double empaqueProcessStdDev, // Parámetro que contiene la desviación estándar del proceso de empaque
            double embarqueProcessMean, // Parámetro que contiene la media del proceso de embarque
            double inspeccion1ToEmpaqueProb) { // Parámetro que contiene la probabilidad de routing a empaque
        this.arrivalMeanTime = arrivalMeanTime; // Asigna el tiempo promedio de arribos a la variable de instancia
        this.conveyor1Time = conveyor1Time; // Asigna el tiempo de conveyor 1 a la variable de instancia
        this.conveyor2Time = conveyor2Time; // Asigna el tiempo de conveyor 2 a la variable de instancia
        this.transportWorkerTime = transportWorkerTime; // Asigna el tiempo de transporte de trabajadores a la variable de instancia
        this.almacenProcessMean = almacenProcessMean; // Asigna la media del proceso de almacén a la variable de instancia
        this.almacenProcessStdDev = almacenProcessStdDev; // Asigna la desviación estándar del proceso de almacén a la variable de instancia
        this.cortadoraProcessMean = cortadoraProcessMean; // Asigna la media del proceso de cortadora a la variable de instancia
        this.tornoProcessMean = tornoProcessMean; // Asigna la media del proceso de torno a la variable de instancia
        this.tornoProcessStdDev = tornoProcessStdDev; // Asigna la desviación estándar del proceso de torno a la variable de instancia
        this.fresadoraProcessMean = fresadoraProcessMean; // Asigna la media del proceso de fresadora a la variable de instancia
        this.almacen2ProcessMean = almacen2ProcessMean; // Asigna la media del proceso de almacén 2 a la variable de instancia
        this.almacen2ProcessStdDev = almacen2ProcessStdDev; // Asigna la desviación estándar del proceso de almacén 2 a la variable de instancia
        this.pinturaProcessMean = pinturaProcessMean; // Asigna la media del proceso de pintura a la variable de instancia
        this.inspeccion1ProcessMean = inspeccion1ProcessMean; // Asigna la media del proceso de inspección 1 a la variable de instancia
        this.inspeccion1ProcessStdDev = inspeccion1ProcessStdDev; // Asigna la desviación estándar del proceso de inspección 1 a la variable de instancia
        this.inspeccion2ProcessMean = inspeccion2ProcessMean; // Asigna la media del proceso de inspección 2 a la variable de instancia
        this.empaqueProcessMean = empaqueProcessMean; // Asigna la media del proceso de empaque a la variable de instancia
        this.empaqueProcessStdDev = empaqueProcessStdDev; // Asigna la desviación estándar del proceso de empaque a la variable de instancia
        this.embarqueProcessMean = embarqueProcessMean; // Asigna la media del proceso de embarque a la variable de instancia
        this.inspeccion1ToEmpaqueProb = inspeccion1ToEmpaqueProb; // Asigna la probabilidad de routing a empaque a la variable de instancia
    } // Cierre del método initialize

    // FIXED TIME: 15 minutos entre arribos
    public double nextArrivalTime() { // Método público que retorna el tiempo hasta el próximo arribo de tipo double sin recibir parámetros
        return arrivalMeanTime; // Retorna el tiempo fijo de arribo almacenado
    } // Cierre del método nextArrivalTime

    // FIXED TIME: 4 minutos para CONVEYOR_1
    public double nextConveyor1Time() { // Método público que retorna el tiempo fijo para el primer conveyor de tipo double sin recibir parámetros
        return conveyor1Time; // Retorna el tiempo fijo del primer conveyor almacenado
    } // Cierre del método nextConveyor1Time

    // FIXED TIME: 4 minutos para CONVEYOR_2
    public double nextConveyor2Time() { // Método público que retorna el tiempo fijo para el segundo conveyor de tipo double sin recibir parámetros
        return conveyor2Time; // Retorna el tiempo fijo del segundo conveyor almacenado
    } // Cierre del método nextConveyor2Time

    // FIXED TIME: 0.1 minutos para transporte por trabajador
    public double nextTransportWorkerTime() { // Método público que retorna el tiempo de transporte para trabajadores de tipo double sin recibir parámetros
        return transportWorkerTime; // Retorna el tiempo fijo de transporte de trabajadores almacenado
    } // Cierre del método nextTransportWorkerTime

    // ALMACEN: N(5, 0.5) según ProModel
    public double nextAlmacenProcess() { // Método público que genera un tiempo de proceso normal para el almacén de tipo double sin recibir parámetros
        return nextNormal(almacenProcessMean, almacenProcessStdDev); // Retorna un valor de distribución normal con la media y desviación del almacén
    } // Cierre del método nextAlmacenProcess

    // CORTADORA: E(3) según ProModel
    public double nextCortadoraProcess() { // Método público que genera un tiempo de proceso exponencial para la cortadora de tipo double sin recibir parámetros
        return nextExponential(cortadoraProcessMean); // Retorna un valor de distribución exponencial con la media de la cortadora
    } // Cierre del método nextCortadoraProcess

    // TORNO: N(5, 0.5) según ProModel
    public double nextTornoProcess() { // Método público que genera un tiempo de proceso normal para el torno de tipo double sin recibir parámetros
        return nextNormal(tornoProcessMean, tornoProcessStdDev); // Retorna un valor de distribución normal con la media y desviación del torno
    } // Cierre del método nextTornoProcess

    // FRESADORA: E(3) según ProModel
    public double nextFresadoraProcess() { // Método público que genera un tiempo de proceso exponencial para la fresadora de tipo double sin recibir parámetros
        return nextExponential(fresadoraProcessMean); // Retorna un valor de distribución exponencial con la media de la fresadora
    } // Cierre del método nextFresadoraProcess

    // ALMACEN_2: N(5, 0.5) según ProModel
    public double nextAlmacen2Process() { // Método público que genera un tiempo de proceso normal para el almacén 2 de tipo double sin recibir parámetros
        return nextNormal(almacen2ProcessMean, almacen2ProcessStdDev); // Retorna un valor de distribución normal con la media y desviación del almacén 2
    } // Cierre del método nextAlmacen2Process

    // PINTURA: E(3) según ProModel
    public double nextPinturaProcess() { // Método público que genera un tiempo de proceso exponencial para pintura de tipo double sin recibir parámetros
        return nextExponential(pinturaProcessMean); // Retorna un valor de distribución exponencial con la media de pintura
    } // Cierre del método nextPinturaProcess

    // INSPECCION_1: N(5, 0.5) según ProModel
    public double nextInspeccion1Process() { // Método público que genera un tiempo de proceso normal para inspección 1 de tipo double sin recibir parámetros
        return nextNormal(inspeccion1ProcessMean, inspeccion1ProcessStdDev); // Retorna un valor de distribución normal con la media y desviación de inspección 1
    } // Cierre del método nextInspeccion1Process

    // INSPECCION_2: E(3) según ProModel
    public double nextInspeccion2Process() { // Método público que genera un tiempo de proceso exponencial para inspección 2 de tipo double sin recibir parámetros
        return nextExponential(inspeccion2ProcessMean); // Retorna un valor de distribución exponencial con la media de inspección 2
    } // Cierre del método nextInspeccion2Process

    // EMPAQUE: N(5, 0.5) según ProModel
    public double nextEmpaqueProcess() { // Método público que genera un tiempo de proceso normal para empaque de tipo double sin recibir parámetros
        return nextNormal(empaqueProcessMean, empaqueProcessStdDev); // Retorna un valor de distribución normal con la media y desviación de empaque
    } // Cierre del método nextEmpaqueProcess

    // EMBARQUE: E(3) según ProModel
    public double nextEmbarqueProcess() { // Método público que genera un tiempo de proceso exponencial para embarque de tipo double sin recibir parámetros
        return nextExponential(embarqueProcessMean); // Retorna un valor de distribución exponencial con la media de embarque
    } // Cierre del método nextEmbarqueProcess

    // ROUTING: 80% a EMPAQUE, 20% a INSPECCION_2
    public boolean routeToEmpaqueFromInspeccion1() { // Método público que decide el routing de inspección 1 de tipo boolean sin recibir parámetros
        return random.nextDouble() < inspeccion1ToEmpaqueProb; // Retorna true si un número aleatorio es menor a la probabilidad de ir a empaque
    } // Cierre del método routeToEmpaqueFromInspeccion1

    // === PRIVATE HELPER METHODS ===

    private double nextExponential(double mean) { // Método privado que genera un número aleatorio con distribución exponencial recibiendo la media como parámetro y retornando double
        if (mean <= 0) { // Condición que verifica si la media es menor o igual a cero
            throw new IllegalArgumentException("Mean must be positive"); // Lanza excepción si la media no es positiva
        } // Cierre del bloque condicional if
        return -mean * Math.log(1.0 - random.nextDouble()); // Retorna un valor exponencial usando la fórmula: -media * ln(1 - U) donde U es uniforme(0,1)
    } // Cierre del método nextExponential

    private double nextNormal(double mean, double stdDev) { // Método privado que genera un número aleatorio con distribución normal recibiendo la media y desviación estándar como parámetros y retornando double
        return mean + stdDev * random.nextGaussian(); // Retorna un valor normal usando la fórmula: media + desviación * Z donde Z es estándar normal
    } // Cierre del método nextNormal
} // Cierre de la clase RandomGenerators
