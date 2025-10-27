package com.simulacion.estadisticas;

import com.simulacion.modelo.Pieza;
import java.util.ArrayList;
import java.util.List;

public class EstadisticasSimulacion {

    private int piezasCompletadas = 0;
    private int piezasEnSistema = 0;

    private List<Double> tiemposEnSistema = new ArrayList<>();
    private List<Double> tiemposEspera = new ArrayList<>();

    // CAMBIO: Ahora guardamos tiempo ocupado por UNIDAD de recurso
    private double tiempoOcupadoLavado = 0;
    private double tiempoOcupadoPintura = 0;
    private double tiempoOcupadoHorno = 0;
    private double tiempoOcupadoInspeccion = 0;

    private int piezasEnRecepcion = 0;
    private int piezasEnLavado = 0;
    private int piezasEnAlmacenPintura = 0;
    private int piezasEnPintura = 0;
    private int piezasEnAlmacenHorno = 0;
    private int piezasEnHorno = 0;
    private int piezasEnInspeccion = 0;

    private double integralColaLavado = 0;
    private double integralColaAlmacenPintura = 0;
    private double integralColaAlmacenHorno = 0;
    private double integralColaInspeccion = 0;
    private double tiempoUltimaActualizacion = 0;

    private int maxColaLavado = 0;
    private int maxColaAlmacenPintura = 0;
    private int maxColaAlmacenHorno = 0;
    private int maxColaInspeccion = 0;

    public void registrarPiezaCompletada(Pieza pieza) {
        piezasCompletadas++;
        piezasEnSistema--;
        tiemposEnSistema.add(pieza.getTiempoEnSistema());
        tiemposEspera.add(pieza.getTiempoEspera());
    }

    public void registrarNuevaPieza() {
        piezasEnSistema++;
    }

    public void actualizarIntegrales(double tiempoActual) {
        double delta = tiempoActual - tiempoUltimaActualizacion;

        integralColaLavado += piezasEnRecepcion * delta;
        integralColaAlmacenPintura += piezasEnAlmacenPintura * delta;
        integralColaAlmacenHorno += piezasEnAlmacenHorno * delta;
        integralColaInspeccion += (piezasEnInspeccion > 2 ? piezasEnInspeccion - 2 : 0) * delta;

        tiempoUltimaActualizacion = tiempoActual;
    }

    public double getTiempoPromedioEnSistema() {
        if (tiemposEnSistema.isEmpty()) return 0;
        return tiemposEnSistema.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    public double getTiempoPromedioEspera() {
        if (tiemposEspera.isEmpty()) return 0;
        return tiemposEspera.stream().mapToDouble(Double::doubleValue).average().orElse(0);
    }

    /**
     * CORREGIDO: Calcula utilizacion considerando la capacidad del recurso
     * Utilizacion = TiempoOcupado / (TiempoTotal * Capacidad)
     */
    public double getUtilizacionLavado(double tiempoTotal, int capacidad) {
        if (tiempoTotal <= 0 || capacidad <= 0) return 0;
        return Math.min(100.0, (tiempoOcupadoLavado / (tiempoTotal * capacidad)) * 100);
    }

    public double getUtilizacionPintura(double tiempoTotal, int capacidad) {
        if (tiempoTotal <= 0 || capacidad <= 0) return 0;
        return Math.min(100.0, (tiempoOcupadoPintura / (tiempoTotal * capacidad)) * 100);
    }

    public double getUtilizacionHorno(double tiempoTotal, int capacidad) {
        if (tiempoTotal <= 0 || capacidad <= 0) return 0;
        return Math.min(100.0, (tiempoOcupadoHorno / (tiempoTotal * capacidad)) * 100);
    }

    public double getUtilizacionInspeccion(double tiempoTotal, int capacidad) {
        if (tiempoTotal <= 0 || capacidad <= 0) return 0;
        return Math.min(100.0, (tiempoOcupadoInspeccion / (tiempoTotal * capacidad)) * 100);
    }

    public double getColaPromedioLavado(double tiempoTotal) {
        return tiempoTotal > 0 ? integralColaLavado / tiempoTotal : 0;
    }

    public double getColaPromedioAlmacenPintura(double tiempoTotal) {
        return tiempoTotal > 0 ? integralColaAlmacenPintura / tiempoTotal : 0;
    }

    public double getColaPromedioAlmacenHorno(double tiempoTotal) {
        return tiempoTotal > 0 ? integralColaAlmacenHorno / tiempoTotal : 0;
    }

    public double getColaPromedioInspeccion(double tiempoTotal) {
        return tiempoTotal > 0 ? integralColaInspeccion / tiempoTotal : 0;
    }

    // Getters y Setters

    public int getPiezasCompletadas() {
        return piezasCompletadas;
    }

    public int getPiezasEnSistema() {
        return piezasEnSistema;
    }

    public int getPiezasEnRecepcion() {
        return piezasEnRecepcion;
    }

    public void setPiezasEnRecepcion(int piezasEnRecepcion) {
        this.piezasEnRecepcion = piezasEnRecepcion;
        maxColaLavado = Math.max(maxColaLavado, piezasEnRecepcion);
    }

    public int getPiezasEnLavado() {
        return piezasEnLavado;
    }

    public void setPiezasEnLavado(int piezasEnLavado) {
        this.piezasEnLavado = piezasEnLavado;
    }

    public int getPiezasEnAlmacenPintura() {
        return piezasEnAlmacenPintura;
    }

    public void setPiezasEnAlmacenPintura(int piezasEnAlmacenPintura) {
        this.piezasEnAlmacenPintura = piezasEnAlmacenPintura;
        maxColaAlmacenPintura = Math.max(maxColaAlmacenPintura, piezasEnAlmacenPintura);
    }

    public int getPiezasEnPintura() {
        return piezasEnPintura;
    }

    public void setPiezasEnPintura(int piezasEnPintura) {
        this.piezasEnPintura = piezasEnPintura;
    }

    public int getPiezasEnAlmacenHorno() {
        return piezasEnAlmacenHorno;
    }

    public void setPiezasEnAlmacenHorno(int piezasEnAlmacenHorno) {
        this.piezasEnAlmacenHorno = piezasEnAlmacenHorno;
        maxColaAlmacenHorno = Math.max(maxColaAlmacenHorno, piezasEnAlmacenHorno);
    }

    public int getPiezasEnHorno() {
        return piezasEnHorno;
    }

    public void setPiezasEnHorno(int piezasEnHorno) {
        this.piezasEnHorno = piezasEnHorno;
    }

    public int getPiezasEnInspeccion() {
        return piezasEnInspeccion;
    }

    public void setPiezasEnInspeccion(int piezasEnInspeccion) {
        this.piezasEnInspeccion = piezasEnInspeccion;
        maxColaInspeccion = Math.max(maxColaInspeccion,
            piezasEnInspeccion > 2 ? piezasEnInspeccion - 2 : 0);
    }

    public void agregarTiempoOcupadoLavado(double tiempo) {
        this.tiempoOcupadoLavado += tiempo;
    }

    public void agregarTiempoOcupadoPintura(double tiempo) {
        this.tiempoOcupadoPintura += tiempo;
    }

    public void agregarTiempoOcupadoHorno(double tiempo) {
        this.tiempoOcupadoHorno += tiempo;
    }

    public void agregarTiempoOcupadoInspeccion(double tiempo) {
        this.tiempoOcupadoInspeccion += tiempo;
    }

    public int getMaxColaLavado() {
        return maxColaLavado;
    }

    public int getMaxColaAlmacenPintura() {
        return maxColaAlmacenPintura;
    }

    public int getMaxColaAlmacenHorno() {
        return maxColaAlmacenHorno;
    }

    public int getMaxColaInspeccion() {
        return maxColaInspeccion;
    }

    public double getTiempoOcupadoLavado() {
        return tiempoOcupadoLavado;
    }

    public double getTiempoOcupadoPintura() {
        return tiempoOcupadoPintura;
    }

    public double getTiempoOcupadoHorno() {
        return tiempoOcupadoHorno;
    }

    public double getTiempoOcupadoInspeccion() {
        return tiempoOcupadoInspeccion;
    }
}
