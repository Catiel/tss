package actividad_4.ejercicio_1;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de parámetros para el EJERCICIO 1 (versión francesa) — solo variables del enunciado.
 * Contiene los parámetros usados en las hojas Excel e imágenes que proporcionaste.
 * Se mantiene patrón Singleton para acceso centralizado.
 */
public class ControladorParametros {
    private static ControladorParametros instancia;

    // Parámetros (nombres alineados con el Excel / imágenes)
    private int tamanoActualMercado = 300_000;           // tamaño actual del mercado (unidades)
    private double crecimientoPrimeros5 = 0.10;         // 10% anual (años 1-5)
    private double crecimientoProximos5 = 0.05;        // 5% anual (años 6-10)
    private double precioVentaUnitario = 50.0;         // $50 por unidad
    private double costeVariableUnitario = 10.0;       // $10 por unidad
    private double cuotaMercadoVersionIngles = 0.30;   // 30% cuota actual
    private double cuotaMercadoConNuevaVersion = 0.40; // 40% con versión francesa
    private double costeFijoCrearVersion = 6_000_000.0; // coste fijo de crear la nueva versión ($)
    private int horizonteAnios = 10;                   // horizonte de planificación (años)
    private double tasaDescuento = 0.10;              // tasa de descuento por defecto (10%)

    // Sistema de observadores para notificar cambios
    private final List<ParametrosChangeListener> listeners = new ArrayList<>();

    /**
     * Interfaz para los escuchadores de cambios en parámetros
     */
    public interface ParametrosChangeListener {
        void onParametrosChanged();
    }

    /**
     * Añade un escuchador de cambios en parámetros
     */
    public void addChangeListener(ParametrosChangeListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    /**
     * Elimina un escuchador de cambios en parámetros
     */
    public void removeChangeListener(ParametrosChangeListener listener) {
        listeners.remove(listener);
    }

    /**
     * Notifica a todos los escuchadores registrados que hubo cambios
     */
    private void notifyListeners() {
        for (ParametrosChangeListener listener : listeners) {
            listener.onParametrosChanged();
        }
    }

    private ControladorParametros() { }

    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros();
        return instancia;
    }

    // Getters y setters — nombres claros según el Excel
    public int getTamanoActualMercado() {
        return tamanoActualMercado;
    }

    public void setTamanoActualMercado(int tamanoActualMercado) {
        this.tamanoActualMercado = tamanoActualMercado;
        notifyListeners();
    }

    public double getCrecimientoPrimeros5() {
        return crecimientoPrimeros5;
    }

    public void setCrecimientoPrimeros5(double crecimientoPrimeros5) {
        this.crecimientoPrimeros5 = crecimientoPrimeros5;
        notifyListeners();
    }

    public double getCrecimientoProximos5() {
        return crecimientoProximos5;
    }

    public void setCrecimientoProximos5(double crecimientoProximos5) {
        this.crecimientoProximos5 = crecimientoProximos5;
        notifyListeners();
    }

    public double getPrecioVentaUnitario() {
        return precioVentaUnitario;
    }

    public void setPrecioVentaUnitario(double precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario;
        notifyListeners();
    }

    public double getCosteVariableUnitario() {
        return costeVariableUnitario;
    }

    public void setCosteVariableUnitario(double costeVariableUnitario) {
        this.costeVariableUnitario = costeVariableUnitario;
        notifyListeners();
    }

    public double getCuotaMercadoVersionIngles() {
        return cuotaMercadoVersionIngles;
    }

    public void setCuotaMercadoVersionIngles(double cuotaMercadoVersionIngles) {
        this.cuotaMercadoVersionIngles = cuotaMercadoVersionIngles;
        notifyListeners();
    }

    public double getCuotaMercadoConNuevaVersion() {
        return cuotaMercadoConNuevaVersion;
    }

    public void setCuotaMercadoConNuevaVersion(double cuotaMercadoConNuevaVersion) {
        this.cuotaMercadoConNuevaVersion = cuotaMercadoConNuevaVersion;
        notifyListeners();
    }

    public double getCosteFijoCrearVersion() {
        return costeFijoCrearVersion;
    }

    public void setCosteFijoCrearVersion(double costeFijoCrearVersion) {
        this.costeFijoCrearVersion = costeFijoCrearVersion;
        notifyListeners();
    }

    public int getHorizonteAnios() {
        return horizonteAnios;
    }

    public void setHorizonteAnios(int horizonteAnios) {
        this.horizonteAnios = horizonteAnios;
        notifyListeners();
    }

    public double getTasaDescuento() {
        return tasaDescuento;
    }

    public void setTasaDescuento(double tasaDescuento) {
        this.tasaDescuento = tasaDescuento;
        notifyListeners();
    }
}