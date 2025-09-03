package actividad_3.ejercicio_1;

/**
 * Controlador de los parámetros del modelo de precios.
 * Implementa el patrón Singleton para que los parámetros sean accesibles y modificables desde cualquier parte de la aplicación.
 * Permite obtener y modificar los valores de coste unitario, tipo de cambio, constante de demanda y elasticidad.
 */
public class ControladorParametros {
    private static ControladorParametros instancia;
    private double costeUnitario = 50;
    private double tipoCambio = 1.22;
    private double constanteDemanda = 27556759;
    private double elasticidad = -2.4;

    /**
     * Constructor privado para el patrón Singleton.
     */
    private ControladorParametros() {
    }

    /**
     * Obtiene la instancia única del controlador de parámetros.
     * @return Instancia única de ControladorParametros.
     */
    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros();
        return instancia;
    }

    /**
     * Obtiene el coste unitario de fabricación en dólares.
     * @return Coste unitario ($).
     */
    public double getCosteUnitario() {
        return costeUnitario;
    }

    /**
     * Establece el coste unitario de fabricación en dólares.
     * @param v Nuevo coste unitario ($).
     */
    public void setCosteUnitario(double v) {
        costeUnitario = v;
    }

    /**
     * Obtiene el tipo de cambio actual entre dólar y libra.
     * @return Tipo de cambio ($/£).
     */
    public double getTipoCambio() {
        return tipoCambio;
    }

    /**
     * Establece el tipo de cambio actual entre dólar y libra.
     * @param v Nuevo tipo de cambio ($/£).
     */
    public void setTipoCambio(double v) {
        tipoCambio = v;
    }

    /**
     * Obtiene la constante de la función de demanda.
     * @return Constante de demanda.
     */
    public double getConstanteDemanda() {
        return constanteDemanda;
    }

    /**
     * Establece la constante de la función de demanda.
     * @param v Nueva constante de demanda.
     */
    public void setConstanteDemanda(double v) {
        constanteDemanda = v;
    }

    /**
     * Obtiene la elasticidad de la función de demanda.
     * @return Elasticidad.
     */
    public double getElasticidad() {
        return elasticidad;
    }

    /**
     * Establece la elasticidad de la función de demanda.
     * @param v Nueva elasticidad.
     */
    public void setElasticidad(double v) {
        elasticidad = v;
    }

}
