package com.simulation.gui;

/**
 * Funciones de easing para animaciones suaves y profesionales
 * Todas las funciones reciben un valor t entre 0 y 1 y devuelven el valor
 * interpolado
 */
public class AnimationEasing {

    /**
     * Sin easing - lineal
     */
    public static double linear(double t) {
        return t;
    }

    /**
     * Ease In Quadrático - Aceleración suave
     */
    public static double easeInQuad(double t) {
        return t * t;
    }

    /**
     * Ease Out Quadrático - Desaceleración suave
     */
    public static double easeOutQuad(double t) {
        return t * (2 - t);
    }

    /**
     * Ease In-Out Quadrático - Aceleración y desaceleración suaves
     */
    public static double easeInOutQuad(double t) {
        return t < 0.5 ? 2 * t * t : -1 + (4 - 2 * t) * t;
    }

    /**
     * Ease In Cúbico - Aceleración más pronunciada
     */
    public static double easeInCubic(double t) {
        return t * t * t;
    }

    /**
     * Ease Out Cúbico - Desaceleración más pronunciada
     */
    public static double easeOutCubic(double t) {
        return (--t) * t * t + 1;
    }

    /**
     * Ease In-Out Cúbico - Suave en ambos extremos
     */
    public static double easeInOutCubic(double t) {
        return t < 0.5 ? 4 * t * t * t : (t - 1) * (2 * t - 2) * (2 * t - 2) + 1;
    }

    /**
     * Ease In Quártico
     */
    public static double easeInQuart(double t) {
        return t * t * t * t;
    }

    /**
     * Ease Out Quártico
     */
    public static double easeOutQuart(double t) {
        return 1 - (--t) * t * t * t;
    }

    /**
     * Ease In-Out Quártico
     */
    public static double easeInOutQuart(double t) {
        return t < 0.5 ? 8 * t * t * t * t : 1 - 8 * (--t) * t * t * t;
    }

    /**
     * Ease In Quíntico
     */
    public static double easeInQuint(double t) {
        return t * t * t * t * t;
    }

    /**
     * Ease Out Quíntico
     */
    public static double easeOutQuint(double t) {
        return 1 + (--t) * t * t * t * t;
    }

    /**
     * Ease In-Out Quíntico
     */
    public static double easeInOutQuint(double t) {
        return t < 0.5 ? 16 * t * t * t * t * t : 1 + 16 * (--t) * t * t * t * t;
    }

    /**
     * Ease In Sinusoidal - Muy suave
     */
    public static double easeInSine(double t) {
        return 1 - Math.cos(t * Math.PI / 2);
    }

    /**
     * Ease Out Sinusoidal - Muy suave
     */
    public static double easeOutSine(double t) {
        return Math.sin(t * Math.PI / 2);
    }

    /**
     * Ease In-Out Sinusoidal - Muy suave en ambos extremos
     */
    public static double easeInOutSine(double t) {
        return -(Math.cos(Math.PI * t) - 1) / 2;
    }

    /**
     * Ease In Exponencial - Muy lento al inicio
     */
    public static double easeInExpo(double t) {
        return t == 0 ? 0 : Math.pow(2, 10 * t - 10);
    }

    /**
     * Ease Out Exponencial - Muy rápido al inicio
     */
    public static double easeOutExpo(double t) {
        return t == 1 ? 1 : 1 - Math.pow(2, -10 * t);
    }

    /**
     * Ease In-Out Exponencial
     */
    public static double easeInOutExpo(double t) {
        return t == 0 ? 0
                : t == 1 ? 1
                        : t < 0.5
                                ? Math.pow(2, 20 * t - 10) / 2
                                : (2 - Math.pow(2, -20 * t + 10)) / 2;
    }

    /**
     * Ease In Circular
     */
    public static double easeInCirc(double t) {
        return 1 - Math.sqrt(1 - Math.pow(t, 2));
    }

    /**
     * Ease Out Circular
     */
    public static double easeOutCirc(double t) {
        return Math.sqrt(1 - Math.pow(t - 1, 2));
    }

    /**
     * Ease In-Out Circular
     */
    public static double easeInOutCirc(double t) {
        return t < 0.5
                ? (1 - Math.sqrt(1 - Math.pow(2 * t, 2))) / 2
                : (Math.sqrt(1 - Math.pow(-2 * t + 2, 2)) + 1) / 2;
    }

    /**
     * Elastic - Rebote elástico al final
     */
    public static double easeOutElastic(double t) {
        double c4 = (2 * Math.PI) / 3;
        return t == 0 ? 0
                : t == 1 ? 1
                        : Math.pow(2, -10 * t) * Math.sin((t * 10 - 0.75) * c4) + 1;
    }

    /**
     * Bounce - Rebote al final
     */
    public static double easeOutBounce(double t) {
        double n1 = 7.5625;
        double d1 = 2.75;

        if (t < 1 / d1) {
            return n1 * t * t;
        } else if (t < 2 / d1) {
            return n1 * (t -= 1.5 / d1) * t + 0.75;
        } else if (t < 2.5 / d1) {
            return n1 * (t -= 2.25 / d1) * t + 0.9375;
        } else {
            return n1 * (t -= 2.625 / d1) * t + 0.984375;
        }
    }

    /**
     * Back - Sobrepasa ligeramente el objetivo
     */
    public static double easeOutBack(double t) {
        double c1 = 1.70158;
        double c3 = c1 + 1;
        return 1 + c3 * Math.pow(t - 1, 3) + c1 * Math.pow(t - 1, 2);
    }

    /**
     * Curva de Bezier cúbica personalizada
     */
    public static double cubicBezier(double t, double p1, double p2) {
        // Bezier simplificado para control points (p1, p2)
        return 3 * (1 - t) * (1 - t) * t * p1 +
                3 * (1 - t) * t * t * p2 +
                t * t * t;
    }

    /**
     * Smooth step - Transición muy suave
     */
    public static double smoothStep(double t) {
        return t * t * (3 - 2 * t);
    }

    /**
     * Smoother step - Aún más suave que smoothStep
     */
    public static double smootherStep(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }
}
