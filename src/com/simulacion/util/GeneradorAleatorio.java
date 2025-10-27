package com.simulacion.util;

import org.apache.commons.math3.distribution.ExponentialDistribution;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.apache.commons.math3.distribution.UniformRealDistribution;
import org.apache.commons.math3.distribution.TriangularDistribution;

/**
 * Generador de numeros aleatorios con diferentes distribuciones estadisticas
 * Utiliza Apache Commons Math para garantizar generacion correcta
 */
public class GeneradorAleatorio {

    /**
     * Genera un tiempo con distribucion exponencial
     * @param media media de la distribucion
     * @return valor aleatorio exponencial
     */
    public static double exponencial(double media) {
        ExponentialDistribution dist = new ExponentialDistribution(media);
        return dist.sample();
    }

    /**
     * Genera un tiempo con distribucion normal
     * @param media media de la distribucion
     * @param desviacion desviacion estandar
     * @return valor aleatorio normal
     */
    public static double normal(double media, double desviacion) {
        NormalDistribution dist = new NormalDistribution(media, desviacion);
        double valor = dist.sample();
        // Asegurar que no sea negativo
        return Math.max(0.01, valor);
    }

    /**
     * Genera un tiempo con distribucion triangular
     * @param min valor minimo
     * @param moda valor mas probable (moda)
     * @param max valor maximo
     * @return valor aleatorio triangular
     */
    public static double triangular(double min, double moda, double max) {
        TriangularDistribution dist = new TriangularDistribution(min, moda, max);
        return dist.sample();
    }

    /**
     * Genera un tiempo con distribucion uniforme dado centro y amplitud
     * Formula: U(centro, amplitud) = Uniforme(centro - amplitud, centro + amplitud)
     * @param centro punto central
     * @param amplitud semi-rango
     * @return valor aleatorio uniforme
     */
    public static double uniformeCentroAmplitud(double centro, double amplitud) {
        double min = centro - amplitud;
        double max = centro + amplitud;
        UniformRealDistribution dist = new UniformRealDistribution(min, max);
        return dist.sample();
    }

    /**
     * Genera un tiempo con distribucion uniforme dado minimo y maximo
     * @param min valor minimo
     * @param max valor maximo
     * @return valor aleatorio uniforme
     */
    public static double uniforme(double min, double max) {
        UniformRealDistribution dist = new UniformRealDistribution(min, max);
        return dist.sample();
    }
}
