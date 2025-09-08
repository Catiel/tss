package actividad_4.ejercicio_1; // Paquete donde reside la clase

import java.util.ArrayList; // Implementación concreta de lista dinámica
import java.util.List;       // Interfaz List para los observadores

/**
 * Controlador de parámetros para el EJERCICIO 1 (versión francesa) — solo variables del enunciado.
 * Contiene los parámetros usados en las hojas Excel e imágenes suministradas.
 * Aplica patrón Singleton + Observer para acceso y notificación centralizada.
 */
public class ControladorParametros { // Declaración de la clase principal del controlador
    private static ControladorParametros instancia; // Instancia única (Singleton, lazy)

    // Parámetros (nombres alineados con el Excel / imágenes) -------------------
    private int tamanoActualMercado = 300_000;           // Tamaño actual del mercado (unidades)
    private double crecimientoPrimeros5 = 0.10;         // Crecimiento anual 10% para años 1-5
    private double crecimientoProximos5 = 0.05;         // Crecimiento anual 5% para años 6-10
    private double precioVentaUnitario = 50.0;          // Precio de venta por unidad ($)
    private double costeVariableUnitario = 10.0;        // Coste variable por unidad ($)
    private double cuotaMercadoVersionIngles = 0.30;    // Cuota de mercado actual (versión inglés)
    private double cuotaMercadoConNuevaVersion = 0.40;  // Cuota de mercado estimada con versión francesa
    private double costeFijoCrearVersion = 6_000_000.0; // Coste fijo de desarrollo ($)
    private int horizonteAnios = 10;                    // Horizonte de análisis (años)
    private double tasaDescuento = 0.10;                // Tasa de descuento (decimal, 10%)

    // Sistema de observadores para notificar cambios --------------------------
    private final List<ParametrosChangeListener> listeners = new ArrayList<>(); // Colección de listeners registrados

    /** Interfaz funcional para recibir notificación de cambios */
    public interface ParametrosChangeListener { void onParametrosChanged(); }

    /** Registra un nuevo listener (solo si no estaba ya) */
    public void addChangeListener(ParametrosChangeListener listener) {
        if (!listeners.contains(listener)) { // Evita duplicados
            listeners.add(listener);         // Añade a la lista de observadores
        }
    }

    /** Elimina un listener previamente registrado */
    public void removeChangeListener(ParametrosChangeListener listener) {
        listeners.remove(listener); // Lo retira si existe
    }

    /** Recorre todos los listeners y dispara su callback de cambio */
    private void notifyListeners() {
        for (ParametrosChangeListener listener : listeners) { // Itera lista actual
            listener.onParametrosChanged();                   // Invoca método de notificación
        }
    }

    private ControladorParametros() { } // Constructor privado: impide instanciación externa

    /** Devuelve la instancia única; si no existe la crea (lazy initialization) */
    public static ControladorParametros getInstancia() {
        if (instancia == null) instancia = new ControladorParametros(); // Crea primera vez
        return instancia; // Retorna referencia única
    }

    // Getters y setters — cada setter actualiza y notifica --------------------
    public int getTamanoActualMercado() { return tamanoActualMercado; } // Obtiene tamaño mercado
    public void setTamanoActualMercado(int tamanoActualMercado) {
        this.tamanoActualMercado = tamanoActualMercado; // Asigna nuevo valor
        notifyListeners();                              // Notifica cambio
    }

    public double getCrecimientoPrimeros5() { return crecimientoPrimeros5; } // Obtiene crecimiento 1-5
    public void setCrecimientoPrimeros5(double crecimientoPrimeros5) {
        this.crecimientoPrimeros5 = crecimientoPrimeros5; // Actualiza valor
        notifyListeners();                                // Notifica
    }

    public double getCrecimientoProximos5() { return crecimientoProximos5; } // Obtiene crecimiento 6-10
    public void setCrecimientoProximos5(double crecimientoProximos5) {
        this.crecimientoProximos5 = crecimientoProximos5; // Actualiza valor
        notifyListeners();                                 // Notifica
    }

    public double getPrecioVentaUnitario() { return precioVentaUnitario; } // Obtiene PVP unitario
    public void setPrecioVentaUnitario(double precioVentaUnitario) {
        this.precioVentaUnitario = precioVentaUnitario; // Actualiza
        notifyListeners();                              // Notifica
    }

    public double getCosteVariableUnitario() { return costeVariableUnitario; } // Obtiene coste variable
    public void setCosteVariableUnitario(double costeVariableUnitario) {
        this.costeVariableUnitario = costeVariableUnitario; // Actualiza
        notifyListeners();                                   // Notifica
    }

    public double getCuotaMercadoVersionIngles() { return cuotaMercadoVersionIngles; } // Obtiene cuota actual
    public void setCuotaMercadoVersionIngles(double cuotaMercadoVersionIngles) {
        this.cuotaMercadoVersionIngles = cuotaMercadoVersionIngles; // Actualiza
        notifyListeners();                                          // Notifica
    }

    public double getCuotaMercadoConNuevaVersion() { return cuotaMercadoConNuevaVersion; } // Obtiene cuota estimada con nueva versión
    public void setCuotaMercadoConNuevaVersion(double cuotaMercadoConNuevaVersion) {
        this.cuotaMercadoConNuevaVersion = cuotaMercadoConNuevaVersion; // Actualiza
        notifyListeners();                                             // Notifica
    }

    public double getCosteFijoCrearVersion() { return costeFijoCrearVersion; } // Obtiene coste fijo
    public void setCosteFijoCrearVersion(double costeFijoCrearVersion) {
        this.costeFijoCrearVersion = costeFijoCrearVersion; // Actualiza
        notifyListeners();                                  // Notifica
    }

    public int getHorizonteAnios() { return horizonteAnios; } // Obtiene horizonte
    public void setHorizonteAnios(int horizonteAnios) {
        this.horizonteAnios = horizonteAnios; // Actualiza
        notifyListeners();                    // Notifica
    }

    public double getTasaDescuento() { return tasaDescuento; } // Obtiene tasa descuento
    public void setTasaDescuento(double tasaDescuento) {
        this.tasaDescuento = tasaDescuento; // Actualiza
        notifyListeners();                  // Notifica
    }
}