package com.simulacion.estadisticas;

public class ResultadoReplica {

    private int numeroReplica;
    private int piezasCompletadas;
    private int piezasEnSistema;
    private double tiempoPromedioEnSistema;
    private double tiempoPromedioEspera;

    private double utilizacionLavado;
    private double utilizacionPintura;
    private double utilizacionHorno;
    private double utilizacionInspeccion;

    private int maxColaRecepcion;
    private int maxColaAlmacenPintura;
    private int maxColaAlmacenHorno;
    private int maxColaInspeccion;

    private double colaPromedioRecepcion;
    private double colaPromedioAlmacenPintura;
    private double colaPromedioAlmacenHorno;
    private double colaPromedioInspeccion;

    public ResultadoReplica(int numeroReplica) {
        this.numeroReplica = numeroReplica;
    }

    // Getters y Setters

    public int getNumeroReplica() {
        return numeroReplica;
    }

    public int getPiezasCompletadas() {
        return piezasCompletadas;
    }

    public void setPiezasCompletadas(int piezasCompletadas) {
        this.piezasCompletadas = piezasCompletadas;
    }

    public int getPiezasEnSistema() {
        return piezasEnSistema;
    }

    public void setPiezasEnSistema(int piezasEnSistema) {
        this.piezasEnSistema = piezasEnSistema;
    }

    public double getTiempoPromedioEnSistema() {
        return tiempoPromedioEnSistema;
    }

    public void setTiempoPromedioEnSistema(double tiempoPromedioEnSistema) {
        this.tiempoPromedioEnSistema = tiempoPromedioEnSistema;
    }

    public double getTiempoPromedioEspera() {
        return tiempoPromedioEspera;
    }

    public void setTiempoPromedioEspera(double tiempoPromedioEspera) {
        this.tiempoPromedioEspera = tiempoPromedioEspera;
    }

    public double getUtilizacionLavado() {
        return utilizacionLavado;
    }

    public void setUtilizacionLavado(double utilizacionLavado) {
        this.utilizacionLavado = utilizacionLavado;
    }

    public double getUtilizacionPintura() {
        return utilizacionPintura;
    }

    public void setUtilizacionPintura(double utilizacionPintura) {
        this.utilizacionPintura = utilizacionPintura;
    }

    public double getUtilizacionHorno() {
        return utilizacionHorno;
    }

    public void setUtilizacionHorno(double utilizacionHorno) {
        this.utilizacionHorno = utilizacionHorno;
    }

    public double getUtilizacionInspeccion() {
        return utilizacionInspeccion;
    }

    public void setUtilizacionInspeccion(double utilizacionInspeccion) {
        this.utilizacionInspeccion = utilizacionInspeccion;
    }

    public int getMaxColaRecepcion() {
        return maxColaRecepcion;
    }

    public void setMaxColaRecepcion(int maxColaRecepcion) {
        this.maxColaRecepcion = maxColaRecepcion;
    }

    public int getMaxColaAlmacenPintura() {
        return maxColaAlmacenPintura;
    }

    public void setMaxColaAlmacenPintura(int maxColaAlmacenPintura) {
        this.maxColaAlmacenPintura = maxColaAlmacenPintura;
    }

    public int getMaxColaAlmacenHorno() {
        return maxColaAlmacenHorno;
    }

    public void setMaxColaAlmacenHorno(int maxColaAlmacenHorno) {
        this.maxColaAlmacenHorno = maxColaAlmacenHorno;
    }

    public int getMaxColaInspeccion() {
        return maxColaInspeccion;
    }

    public void setMaxColaInspeccion(int maxColaInspeccion) {
        this.maxColaInspeccion = maxColaInspeccion;
    }

    public double getColaPromedioRecepcion() {
        return colaPromedioRecepcion;
    }

    public void setColaPromedioRecepcion(double colaPromedioRecepcion) {
        this.colaPromedioRecepcion = colaPromedioRecepcion;
    }

    public double getColaPromedioAlmacenPintura() {
        return colaPromedioAlmacenPintura;
    }

    public void setColaPromedioAlmacenPintura(double colaPromedioAlmacenPintura) {
        this.colaPromedioAlmacenPintura = colaPromedioAlmacenPintura;
    }

    public double getColaPromedioAlmacenHorno() {
        return colaPromedioAlmacenHorno;
    }

    public void setColaPromedioAlmacenHorno(double colaPromedioAlmacenHorno) {
        this.colaPromedioAlmacenHorno = colaPromedioAlmacenHorno;
    }

    public double getColaPromedioInspeccion() {
        return colaPromedioInspeccion;
    }

    public void setColaPromedioInspeccion(double colaPromedioInspeccion) {
        this.colaPromedioInspeccion = colaPromedioInspeccion;
    }
}
