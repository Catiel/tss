package actividad_4.ejercicio_2;

import java.util.ArrayList;
import java.util.List;

/**
 * Controlador de parámetros (Singleton) para el EJERCICIO 2 (payback).
 * Sólo mantiene los valores necesarios:
 *  - Inversión original (millones)
 *  - Flujo de caja del año 1 (millones)
 *  - Tasa de crecimiento anual (decimal)
 *  - Horizonte en años (por defecto 10)
 */
public class ControladorParametros {
    private static ControladorParametros instancia;

    // --------------------------- Parámetros ejercicio 2 ---------------------------
    private double inversionOriginal = 300.0;      // millones
    private double flujoAnio1 = 50.0;              // millones
    private double tasaCrecimientoAnual = 0.15;    // 15% (decimal)
    private int horizonteAnios = 10;               // años a simular

    // Observadores
    private final List<ParametrosChangeListener> listeners = new ArrayList<>();
    public interface ParametrosChangeListener { void onParametrosChanged(); }
    public void addChangeListener(ParametrosChangeListener l){ if(!listeners.contains(l)) listeners.add(l);}
    public void removeChangeListener(ParametrosChangeListener l){ listeners.remove(l);}
    private void notifyListeners(){ for(ParametrosChangeListener l: listeners) l.onParametrosChanged(); }

    private ControladorParametros(){}
    public static ControladorParametros getInstancia(){ if(instancia==null) instancia = new ControladorParametros(); return instancia; }

    // Getters / Setters
    public double getInversionOriginal(){ return inversionOriginal; }
    public void setInversionOriginal(double v){ this.inversionOriginal = v; notifyListeners(); }

    public double getFlujoAnio1(){ return flujoAnio1; }
    public void setFlujoAnio1(double v){ this.flujoAnio1 = v; notifyListeners(); }

    public double getTasaCrecimientoAnual(){ return tasaCrecimientoAnual; }
    public void setTasaCrecimientoAnual(double v){ this.tasaCrecimientoAnual = v; notifyListeners(); }

    public int getHorizonteAnios(){ return horizonteAnios; }
    public void setHorizonteAnios(int h){ this.horizonteAnios = h; notifyListeners(); }
}