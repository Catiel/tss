package com.simulation.random; // Declaración del paquete que contiene las clases de generación de números aleatorios

import java.util.Random; // Importa la clase Random de Java para generar números pseudoaleatorios

/**
 * Generador de números aleatorios para el sistema DIGEMIC
 * Implementa las distribuciones según el modelo ProModel de pasaportes
 */
public class RandomGenerators { // Declaración de la clase pública que genera números aleatorios según distribuciones específicas
    private Random random; // Variable privada que almacena el generador de números aleatorios base

    // Parámetros de la simulación según ProModel DIGEMIC
    private double arrivalMeanTime; // Variable privada que almacena el tiempo medio entre arribos según distribución exponencial E(3.33)
    private double zonaFormasMin; // Variable privada que almacena el tiempo mínimo en zona de formas según distribución uniforme U(6±2)
    private double zonaFormasMax; // Variable privada que almacena el tiempo máximo en zona de formas según distribución uniforme U(6±2)
    private double servicioMean; // Variable privada que almacena el tiempo medio de servicio según distribución exponencial E(6)
    private double pausaServidorMean; // Variable privada que almacena el tiempo medio de pausa según distribución exponencial E(5)
    private double directoASalaProb; // Variable privada que almacena la probabilidad de ir directo a sala (0.90 = 90%)

    public RandomGenerators(long seed) { // Constructor público que recibe una semilla para reproducibilidad de resultados
        this.random = new Random(seed); // Inicializa el generador Random con la semilla proporcionada para generar secuencias deterministas
    }

    public void initialize( // Método público que inicializa todos los parámetros de las distribuciones
            double arrivalMeanTime, // Parámetro 1: tiempo medio entre arribos en minutos
            double zonaFormasMin, // Parámetro 2: tiempo mínimo en zona de formas en minutos
            double zonaFormasMax, // Parámetro 3: tiempo máximo en zona de formas en minutos
            double servicioMean, // Parámetro 4: tiempo medio de servicio en minutos
            double pausaServidorMean, // Parámetro 5: tiempo medio de pausa del servidor en minutos
            double directoASalaProb) { // Parámetro 6: probabilidad de ir directo a sala (valor entre 0.0 y 1.0)
        this.arrivalMeanTime = arrivalMeanTime; // Asigna el tiempo medio de arribos a la variable de instancia
        this.zonaFormasMin = zonaFormasMin; // Asigna el tiempo mínimo en zona de formas a la variable de instancia
        this.zonaFormasMax = zonaFormasMax; // Asigna el tiempo máximo en zona de formas a la variable de instancia
        this.servicioMean = servicioMean; // Asigna el tiempo medio de servicio a la variable de instancia
        this.pausaServidorMean = pausaServidorMean; // Asigna el tiempo medio de pausa a la variable de instancia
        this.directoASalaProb = directoASalaProb; // Asigna la probabilidad de ir directo a sala a la variable de instancia
    }

    public double nextArrivalTime() { // Método público que genera el siguiente tiempo entre arribos usando distribución exponencial E(3.33)
        return nextExponential(arrivalMeanTime); // Retorna un valor aleatorio exponencial con media arrivalMeanTime (simula proceso de Poisson)
    }

    public double nextZonaFormasTime() { // Método público que genera el tiempo que una entidad pasa en zona de formas usando distribución uniforme U(4, 8)
        return nextUniform(zonaFormasMin, zonaFormasMax); // Retorna un valor aleatorio uniforme entre zonaFormasMin y zonaFormasMax
    }

    public double nextServicioTime() { // Método público que genera el tiempo de servicio en un servidor usando distribución exponencial E(6)
        return nextExponential(servicioMean); // Retorna un valor aleatorio exponencial con media servicioMean
    }

    public double nextPausaServidorTime() { // Método público que genera el tiempo de pausa del servidor usando distribución exponencial E(5)
        return nextExponential(pausaServidorMean); // Retorna un valor aleatorio exponencial con media pausaServidorMean
    }

    public boolean goDirectoASala() { // Método público que determina si una entidad va directo a sala (90%) o a llenar formas (10%)
        return random.nextDouble() < directoASalaProb; // Genera número aleatorio entre 0.0 y 1.0, retorna true si es menor que directoASalaProb (0.90)
    }

    // === PRIVATE HELPER METHODS ===

    private double nextExponential(double mean) { // Método privado que genera un número aleatorio según distribución exponencial con media especificada
        if (mean <= 0) { // Verifica si la media es menor o igual a cero (valor inválido para exponencial)
            throw new IllegalArgumentException("Mean must be positive"); // Lanza excepción indicando que la media debe ser positiva
        }
        return -mean * Math.log(1.0 - random.nextDouble()); // Aplica método de transformación inversa: -λ * ln(1-U) donde U es uniforme(0,1) y λ=1/mean
    }

    private double nextUniform(double min, double max) { // Método privado que genera un número aleatorio según distribución uniforme continua entre min y max
        if (min >= max) { // Verifica si el mínimo es mayor o igual que el máximo (rango inválido)
            throw new IllegalArgumentException("Min must be less than max"); // Lanza excepción indicando que min debe ser menor que max
        }
        return min + (max - min) * random.nextDouble(); // Aplica transformación lineal: min + (max-min)*U donde U es uniforme(0,1)
    }
}
