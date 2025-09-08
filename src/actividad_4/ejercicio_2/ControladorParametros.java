package actividad_4.ejercicio_2; // Paquete donde reside la clase

import java.util.ArrayList; // Importa implementación de lista dinámica
import java.util.List;      // Importa la interfaz List

/**
 * Controlador de parámetros (Singleton) para el EJERCICIO 2 (payback).
 * Mantiene valores globales y notifica a los paneles cuando cambian.
 */
public class ControladorParametros { // Clase principal del controlador
    private static ControladorParametros instancia; // Referencia estática única (patrón Singleton)

    // --------------------------- Parámetros ejercicio 2 ---------------------------
    private double inversionOriginal = 300.0;      // Inversión inicial (millones)
    private double flujoAnio1 = 50.0;              // Flujo de caja del año 1 (millones)
    private double tasaCrecimientoAnual = 0.15;    // Tasa de crecimiento anual (decimal, 0.15 = 15%)
    private int horizonteAnios = 10;               // Horizonte de años para cálculo del payback

    // Observadores
    private final List<ParametrosChangeListener> listeners = new ArrayList<>(); // Lista de listeners registrados
    public interface ParametrosChangeListener { void onParametrosChanged(); }   // Interfaz para recibir aviso de cambios
    public void addChangeListener(ParametrosChangeListener l){                  // Agrega listener si no está
        if(!listeners.contains(l)) listeners.add(l);
    }
    public void removeChangeListener(ParametrosChangeListener l){ listeners.remove(l); } // Elimina listener
    private void notifyListeners(){                                             // Notifica a todos los listeners
        for(ParametrosChangeListener l: listeners) l.onParametrosChanged();
    }

    private ControladorParametros(){} // Constructor privado evita instanciación externa

    public static ControladorParametros getInstancia(){ // Método de acceso Singleton
        if(instancia==null) instancia = new ControladorParametros(); // Crea instancia única si no existe
        return instancia; // Devuelve la instancia única
    }

    // Getters / Setters (cada setter notifica cambios)
    public double getInversionOriginal(){ return inversionOriginal; } // Devuelve inversión
    public void setInversionOriginal(double v){ this.inversionOriginal = v; notifyListeners(); } // Actualiza inversión

    public double getFlujoAnio1(){ return flujoAnio1; } // Devuelve flujo año 1
    public void setFlujoAnio1(double v){ this.flujoAnio1 = v; notifyListeners(); } // Actualiza flujo año 1

    public double getTasaCrecimientoAnual(){ return tasaCrecimientoAnual; } // Devuelve tasa crecimiento
    public void setTasaCrecimientoAnual(double v){ this.tasaCrecimientoAnual = v; notifyListeners(); } // Actualiza tasa

    public int getHorizonteAnios(){ return horizonteAnios; } // Devuelve horizonte
    public void setHorizonteAnios(int h){ this.horizonteAnios = h; notifyListeners(); } // Actualiza horizonte
}