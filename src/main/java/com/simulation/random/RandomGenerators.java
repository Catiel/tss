package com.simulation.random; // Declaración del paquete que contiene las clases relacionadas con la generación de números aleatorios

import java.util.Random; // Importa la clase Random de Java para generar números pseudoaleatorios

public class RandomGenerators { // Declaración de la clase pública RandomGenerators que encapsula todos los generadores de números aleatorios con diferentes distribuciones para la simulación
    private Random random; // Variable privada que almacena la instancia del generador de números aleatorios de Java

    // Parámetros de distribuciones
    private double arrivalMean; // Variable privada que almacena la media de la distribución exponencial de tiempo entre arribos
    private double transportRecepcionLavadoraMean; // Variable privada que almacena la media de la distribución exponencial de transporte de recepción a lavadora
    private double transportLavadoraAlmacenMean; // Variable privada que almacena la media de la distribución exponencial de transporte de lavadora a almacén
    private double lavadoraProcessMean; // Variable privada que almacena la media de la distribución normal de tiempo de proceso en lavadora
    private double lavadoraProcessStdDev; // Variable privada que almacena la desviación estándar de la distribución normal de tiempo de proceso en lavadora
    private double pinturaProcessMin; // Variable privada que almacena el valor mínimo de la distribución triangular de tiempo de proceso en pintura
    private double pinturaProcessMode; // Variable privada que almacena la moda (valor más probable) de la distribución triangular de tiempo de proceso en pintura
    private double pinturaProcessMax; // Variable privada que almacena el valor máximo de la distribución triangular de tiempo de proceso en pintura
    private double transportPinturaAlmacenMin; // Variable privada que almacena el valor mínimo de la distribución uniforme de transporte de pintura a almacén
    private double transportPinturaAlmacenMax; // Variable privada que almacena el valor máximo de la distribución uniforme de transporte de pintura a almacén
    private double hornoProcessMin; // Variable privada que almacena el valor mínimo de la distribución uniforme de tiempo de proceso en horno
    private double hornoProcessMax; // Variable privada que almacena el valor máximo de la distribución uniforme de tiempo de proceso en horno
    private double transportHornoInspeccionMin; // Variable privada que almacena el valor mínimo de la distribución uniforme de transporte de horno a inspección
    private double transportHornoInspeccionMax; // Variable privada que almacena el valor máximo de la distribución uniforme de transporte de horno a inspección
    private double inspeccionOperationMean; // Variable privada que almacena la media de la distribución exponencial de tiempo de operación de inspección

    public RandomGenerators(long seed) { // Constructor público que inicializa el generador de números aleatorios recibiendo una semilla (seed) de tipo long como parámetro
        this.random = new Random(seed); // Crea una nueva instancia de Random con la semilla especificada para garantizar reproducibilidad de los resultados
    } // Cierre del constructor RandomGenerators

    public void initialize( // Método público que inicializa todos los parámetros de las distribuciones recibiendo 15 parámetros double
            double arrivalMean, // Primer parámetro: media de la distribución de arribos
            double transportRecepcionLavadoraMean, // Segundo parámetro: media de transporte recepción-lavadora
            double transportLavadoraAlmacenMean, // Tercer parámetro: media de transporte lavadora-almacén
            double lavadoraProcessMean, // Cuarto parámetro: media de proceso en lavadora
            double lavadoraProcessStdDev, // Quinto parámetro: desviación estándar de proceso en lavadora
            double pinturaProcessMin, // Sexto parámetro: mínimo de proceso en pintura
            double pinturaProcessMode, // Séptimo parámetro: moda de proceso en pintura
            double pinturaProcessMax, // Octavo parámetro: máximo de proceso en pintura
            double transportPinturaAlmacenMin, // Noveno parámetro: mínimo de transporte pintura-almacén
            double transportPinturaAlmacenMax, // Décimo parámetro: máximo de transporte pintura-almacén
            double hornoProcessMin, // Undécimo parámetro: mínimo de proceso en horno
            double hornoProcessMax, // Duodécimo parámetro: máximo de proceso en horno
            double transportHornoInspeccionMin, // Decimotercero parámetro: mínimo de transporte horno-inspección
            double transportHornoInspeccionMax, // Decimocuarto parámetro: máximo de transporte horno-inspección
            double inspeccionOperationMean) { // Decimoquinto parámetro: media de operación de inspección

        this.arrivalMean = arrivalMean; // Asigna el valor de media de arribos recibido a la variable de instancia
        this.transportRecepcionLavadoraMean = transportRecepcionLavadoraMean; // Asigna el valor de media de transporte recepción-lavadora recibido a la variable de instancia
        this.transportLavadoraAlmacenMean = transportLavadoraAlmacenMean; // Asigna el valor de media de transporte lavadora-almacén recibido a la variable de instancia
        this.lavadoraProcessMean = lavadoraProcessMean; // Asigna el valor de media de proceso en lavadora recibido a la variable de instancia
        this.lavadoraProcessStdDev = lavadoraProcessStdDev; // Asigna el valor de desviación estándar de proceso en lavadora recibido a la variable de instancia
        this.pinturaProcessMin = pinturaProcessMin; // Asigna el valor mínimo de proceso en pintura recibido a la variable de instancia
        this.pinturaProcessMode = pinturaProcessMode; // Asigna el valor de moda de proceso en pintura recibido a la variable de instancia
        this.pinturaProcessMax = pinturaProcessMax; // Asigna el valor máximo de proceso en pintura recibido a la variable de instancia
        this.transportPinturaAlmacenMin = transportPinturaAlmacenMin; // Asigna el valor mínimo de transporte pintura-almacén recibido a la variable de instancia
        this.transportPinturaAlmacenMax = transportPinturaAlmacenMax; // Asigna el valor máximo de transporte pintura-almacén recibido a la variable de instancia
        this.hornoProcessMin = hornoProcessMin; // Asigna el valor mínimo de proceso en horno recibido a la variable de instancia
        this.hornoProcessMax = hornoProcessMax; // Asigna el valor máximo de proceso en horno recibido a la variable de instancia
        this.transportHornoInspeccionMin = transportHornoInspeccionMin; // Asigna el valor mínimo de transporte horno-inspección recibido a la variable de instancia
        this.transportHornoInspeccionMax = transportHornoInspeccionMax; // Asigna el valor máximo de transporte horno-inspección recibido a la variable de instancia
        this.inspeccionOperationMean = inspeccionOperationMean; // Asigna el valor de media de operación de inspección recibido a la variable de instancia
    } // Cierre del método initialize

    /** // Inicio del comentario Javadoc del método
     * Distribución Exponencial: E(mean) // Descripción de la distribución utilizada
     * Genera tiempo entre arribos // Propósito del método
     */ // Fin del comentario Javadoc
    public double nextArrivalTime() { // Método público que genera el siguiente tiempo entre arribos usando distribución exponencial sin recibir parámetros y retornando un double
        return -arrivalMean * Math.log(1.0 - random.nextDouble()); // Retorna un valor generado con la fórmula de inversión de distribución exponencial: -λ * ln(1-U) donde U es uniforme(0,1) y λ es la media
    } // Cierre del método nextArrivalTime

    /** // Inicio del comentario Javadoc del método
     * Transporte RECEPCION -> LAVADORA: E(3) // Descripción de la distribución y su parámetro por defecto
     */ // Fin del comentario Javadoc
    public double nextTransportRecepcionLavadora() { // Método público que genera el siguiente tiempo de transporte de recepción a lavadora usando distribución exponencial sin recibir parámetros y retornando un double
        return -transportRecepcionLavadoraMean * Math.log(1.0 - random.nextDouble()); // Retorna un valor generado con la fórmula de inversión de distribución exponencial usando la media configurada
    } // Cierre del método nextTransportRecepcionLavadora

    /** // Inicio del comentario Javadoc del método
     * Transporte LAVADORA -> ALMACEN_PINTURA: E(2) // Descripción de la distribución y su parámetro por defecto
     */ // Fin del comentario Javadoc
    public double nextTransportLavadoraAlmacen() { // Método público que genera el siguiente tiempo de transporte de lavadora a almacén usando distribución exponencial sin recibir parámetros y retornando un double
        return -transportLavadoraAlmacenMean * Math.log(1.0 - random.nextDouble()); // Retorna un valor generado con la fórmula de inversión de distribución exponencial usando la media configurada
    } // Cierre del método nextTransportLavadoraAlmacen

    /** // Inicio del comentario Javadoc del método
     * Proceso LAVADORA: N(10, 2) = Normal(media=10, desv=2) // Descripción de la distribución normal con sus parámetros por defecto
     */ // Fin del comentario Javadoc
    public double nextLavadoraProcess() { // Método público que genera el siguiente tiempo de proceso en lavadora usando distribución normal sin recibir parámetros y retornando un double
        double value = random.nextGaussian() * lavadoraProcessStdDev + lavadoraProcessMean; // Genera un valor normal multiplicando una gaussiana estándar N(0,1) por la desviación estándar y sumando la media
        // Asegurar que no sea negativo
        return Math.max(0.1, value); // Retorna el máximo entre 0.1 y el valor generado para asegurar que el tiempo de proceso sea siempre positivo (mínimo 0.1 minutos)
    } // Cierre del método nextLavadoraProcess

    /** // Inicio del comentario Javadoc del método
     * Proceso PINTURA: T(4, 8, 10) = Triangular(min=4, mode=8, max=10) // Descripción de la distribución triangular con sus parámetros por defecto
     */ // Fin del comentario Javadoc
    public double nextPinturaProcess() { // Método público que genera el siguiente tiempo de proceso en pintura usando distribución triangular sin recibir parámetros y retornando un double
        double u = random.nextDouble(); // Genera un número aleatorio uniforme entre 0 y 1
        double f = (pinturaProcessMode - pinturaProcessMin) / (pinturaProcessMax - pinturaProcessMin); // Calcula el factor f que representa la posición relativa de la moda dentro del rango [min, max]

        if (u < f) { // Condición que verifica si el número aleatorio está en la primera mitad de la distribución triangular (antes de la moda)
            return pinturaProcessMin + Math.sqrt(u * (pinturaProcessMax - pinturaProcessMin) * // Retorna un valor calculado con la fórmula de inversión para la parte ascendente de la triangular: min + sqrt(U * (max-min) * (mode-min))
                                                     (pinturaProcessMode - pinturaProcessMin)); // Continuación de la fórmula: multiplicando por (mode-min)
        } else { // Bloque else que se ejecuta si el número aleatorio está en la segunda mitad de la distribución (después de la moda)
            return pinturaProcessMax - Math.sqrt((1 - u) * (pinturaProcessMax - pinturaProcessMin) * // Retorna un valor calculado con la fórmula de inversión para la parte descendente de la triangular: max - sqrt((1-U) * (max-min) * (max-mode))
                                                           (pinturaProcessMax - pinturaProcessMode)); // Continuación de la fórmula: multiplicando por (max-mode)
        } // Cierre del bloque else
    } // Cierre del método nextPinturaProcess

    /** // Inicio del comentario Javadoc del método
     * Transporte PINTURA -> ALMACEN_HORNO: U(3.5, 1.5) // Descripción de la distribución uniforme con media y half-width
     * Uniforme en rango [2.0, 5.0] // Rango real de la distribución uniforme
     */ // Fin del comentario Javadoc
    public double nextTransportPinturaAlmacen() { // Método público que genera el siguiente tiempo de transporte de pintura a almacén usando distribución uniforme sin recibir parámetros y retornando un double
        return transportPinturaAlmacenMin + // Retorna un valor calculado como el mínimo más
               (transportPinturaAlmacenMax - transportPinturaAlmacenMin) * random.nextDouble(); // el rango (max-min) multiplicado por un número aleatorio uniforme entre 0 y 1, generando así una distribución uniforme en [min, max]
    } // Cierre del método nextTransportPinturaAlmacen

    /** // Inicio del comentario Javadoc del método
     * Proceso HORNO: U(3, 1) // Descripción de la distribución uniforme con media y half-width
     * Uniforme en rango [2.0, 4.0] // Rango real de la distribución uniforme
     */ // Fin del comentario Javadoc
    public double nextHornoProcess() { // Método público que genera el siguiente tiempo de proceso en horno usando distribución uniforme sin recibir parámetros y retornando un double
        return hornoProcessMin + // Retorna un valor calculado como el mínimo más
               (hornoProcessMax - hornoProcessMin) * random.nextDouble(); // el rango (max-min) multiplicado por un número aleatorio uniforme entre 0 y 1, generando así una distribución uniforme en [min, max]
    } // Cierre del método nextHornoProcess

    /** // Inicio del comentario Javadoc del método
     * Transporte HORNO -> INSPECCION: U(2, 1) // Descripción de la distribución uniforme con media y half-width
     * Uniforme en rango [1.0, 3.0] // Rango real de la distribución uniforme
     */ // Fin del comentario Javadoc
    public double nextTransportHornoInspeccion() { // Método público que genera el siguiente tiempo de transporte de horno a inspección usando distribución uniforme sin recibir parámetros y retornando un double
        return transportHornoInspeccionMin + // Retorna un valor calculado como el mínimo más
               (transportHornoInspeccionMax - transportHornoInspeccionMin) * random.nextDouble(); // el rango (max-min) multiplicado por un número aleatorio uniforme entre 0 y 1, generando así una distribución uniforme en [min, max]
    } // Cierre del método nextTransportHornoInspeccion

    /** // Inicio del comentario Javadoc del método
     * Operación de INSPECCION: E(2) // Descripción de la distribución exponencial y su parámetro por defecto
     */ // Fin del comentario Javadoc
    public double nextInspeccionOperation() { // Método público que genera el siguiente tiempo de operación de inspección usando distribución exponencial sin recibir parámetros y retornando un double
        return -inspeccionOperationMean * Math.log(1.0 - random.nextDouble()); // Retorna un valor generado con la fórmula de inversión de distribución exponencial usando la media configurada
    } // Cierre del método nextInspeccionOperation
} // Cierre de la clase RandomGenerators
