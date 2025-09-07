package actividad_4.ejercicio_1;

/**
 * Controlador de los parámetros del modelo Wozac.
 * Implementa el patrón Singleton para que los parámetros sean accesibles y modificables desde cualquier parte de la aplicación.
 * Permite obtener y modificar los valores de demanda, crecimiento, costos, precios y tasa de descuento.
 */
public class ControladorParametros {
    private static ControladorParametros instancia;

    // Parámetros del modelo de Wozac
    private int demandaInicial = 50000;
    private double crecimientoAnual = 0.05;
    private double costoCapacidadUnitaria = 16.0;
    private double precioVentaUnitario = 3.0;
    private double costoVariableUnitario = 0.20;
    private double costoOperativoUnitario = 0.40;
    private double tasaDescuento = 0.10; // Por defecto 10%

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
     * Obtiene la demanda inicial del modelo.
     * @return Demanda inicial (unidades).
     */
    public int getDemandaInicial() {
        return demandaInicial;
    }

    /**
     * Establece la demanda inicial del modelo.
     * @param v Nueva demanda inicial (unidades).
     */
    public void setDemandaInicial(int v) {
        demandaInicial = v;
    }

    /**
     * Obtiene el crecimiento anual de la demanda.
     * @return Crecimiento anual (proporción, ej: 0.05 para 5%).
     */
    public double getCrecimientoAnual() {
        return crecimientoAnual;
    }

    /**
     * Establece el crecimiento anual de la demanda.
     * @param v Nuevo crecimiento anual (proporción).
     */
    public void setCrecimientoAnual(double v) {
        crecimientoAnual = v;
    }

    /**
     * Obtiene el costo de capacidad unitaria.
     * @return Costo de capacidad unitaria ($ por unidad).
     */
    public double getCostoCapacidadUnitaria() {
        return costoCapacidadUnitaria;
    }

    /**
     * Establece el costo de capacidad unitaria.
     * @param v Nuevo costo de capacidad unitaria ($ por unidad).
     */
    public void setCostoCapacidadUnitaria(double v) {
        costoCapacidadUnitaria = v;
    }

    /**
     * Obtiene el precio de venta unitario.
     * @return Precio de venta unitario ($ por unidad).
     */
    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    /**
     * Establece el precio de venta unitario.
     * @param v Nuevo precio de venta unitario ($ por unidad).
     */
    public void setPrecioVentaUnitario(double v) {
        precioVentaUnitario = v;
    }

    /**
     * Obtiene el costo variable unitario.
     * @return Costo variable unitario ($ por unidad).
     */
    public double getCostoVariableUnitario() {
        return costoVariableUnitario;
    }

    /**
     * Establece el costo variable unitario.
     * @param v Nuevo costo variable unitario ($ por unidad).
     */
    public void setCostoVariableUnitario(double v) {
        costoVariableUnitario = v;
    }

    /**
     * Obtiene el costo operativo unitario.
     * @return Costo operativo unitario anual ($ por unidad).
     */
    public double getCostoOperativoUnitario() {
        return costoOperativoUnitario;
    }

    /**
     * Establece el costo operativo unitario.
     * @param v Nuevo costo operativo unitario anual ($ por unidad).
     */
    public void setCostoOperativoUnitario(double v) {
        costoOperativoUnitario = v;
    }

    /**
     * Obtiene la tasa de descuento para el cálculo de VAN.
     * @return Tasa de descuento anual (proporción, ej: 0.10 para 10%).
     */
    public double getTasaDescuento() {
        return tasaDescuento;
    }

    /**
     * Establece la tasa de descuento para el cálculo de VAN.
     * @param v Nueva tasa de descuento anual (proporción).
     */
    public void setTasaDescuento(double v) {
        tasaDescuento = v;
    }
}
