package com.simulacion.modelo;

/**
 * Clase que representa una pieza que fluye a traves del sistema de produccion
 * Contiene informacion sobre tiempos de entrada, procesamiento y salida
 */
public class Pieza {

    // Contador estatico para generar IDs unicos
    private static int contadorId = 0;

    // Identificador unico de la pieza
    private final int id;

    // Tiempos de registro (en minutos desde el inicio de la simulacion)
    private double tiempoLlegada;
    private double tiempoInicioRecepcion;
    private double tiempoInicioLavado;
    private double tiempoFinLavado;
    private double tiempoInicioEsperaPintura;  // CORREGIDO: era "tiempoInicioEsperaP intura" con espacio
    private double tiempoInicioPintura;
    private double tiempoFinPintura;
    private double tiempoInicioEsperaHorno;
    private double tiempoInicioHorno;
    private double tiempoFinHorno;
    private double tiempoInicioInspeccion;
    private double tiempoFinInspeccion;
    private double tiempoSalida;

    /**
     * Constructor que crea una nueva pieza con ID unico
     * @param tiempoLlegada tiempo de llegada al sistema en minutos
     */
    public Pieza(double tiempoLlegada) {
        this.id = ++contadorId;
        this.tiempoLlegada = tiempoLlegada;
        this.tiempoInicioRecepcion = tiempoLlegada;
    }

    /**
     * Reinicia el contador de IDs (util para nuevas replicas)
     */
    public static void reiniciarContador() {
        contadorId = 0;
    }

    /**
     * Calcula el tiempo total que la pieza estuvo en el sistema
     * @return tiempo total en el sistema en minutos
     */
    public double getTiempoEnSistema() {
        return tiempoSalida - tiempoLlegada;
    }

    /**
     * Calcula el tiempo de espera en colas
     * @return tiempo de espera total en minutos
     */
    public double getTiempoEspera() {
        double esperaLavado = tiempoInicioLavado - tiempoInicioRecepcion;
        double esperaPintura = tiempoInicioPintura - tiempoInicioEsperaPintura;
        double esperaHorno = tiempoInicioHorno - tiempoInicioEsperaHorno;
        return esperaLavado + esperaPintura + esperaHorno;
    }

    // Getters y Setters

    public int getId() {
        return id;
    }

    public double getTiempoLlegada() {
        return tiempoLlegada;
    }

    public void setTiempoLlegada(double tiempoLlegada) {
        this.tiempoLlegada = tiempoLlegada;
    }

    public double getTiempoInicioRecepcion() {
        return tiempoInicioRecepcion;
    }

    public void setTiempoInicioRecepcion(double tiempoInicioRecepcion) {
        this.tiempoInicioRecepcion = tiempoInicioRecepcion;
    }

    public double getTiempoInicioLavado() {
        return tiempoInicioLavado;
    }

    public void setTiempoInicioLavado(double tiempoInicioLavado) {
        this.tiempoInicioLavado = tiempoInicioLavado;
    }

    public double getTiempoFinLavado() {
        return tiempoFinLavado;
    }

    public void setTiempoFinLavado(double tiempoFinLavado) {
        this.tiempoFinLavado = tiempoFinLavado;
    }

    public double getTiempoInicioEsperaPintura() {
        return tiempoInicioEsperaPintura;
    }

    public void setTiempoInicioEsperaPintura(double tiempoInicioEsperaPintura) {
        this.tiempoInicioEsperaPintura = tiempoInicioEsperaPintura;
    }

    public double getTiempoInicioPintura() {
        return tiempoInicioPintura;
    }

    public void setTiempoInicioPintura(double tiempoInicioPintura) {
        this.tiempoInicioPintura = tiempoInicioPintura;
    }

    public double getTiempoFinPintura() {
        return tiempoFinPintura;
    }

    public void setTiempoFinPintura(double tiempoFinPintura) {
        this.tiempoFinPintura = tiempoFinPintura;
    }

    public double getTiempoInicioEsperaHorno() {
        return tiempoInicioEsperaHorno;
    }

    public void setTiempoInicioEsperaHorno(double tiempoInicioEsperaHorno) {
        this.tiempoInicioEsperaHorno = tiempoInicioEsperaHorno;
    }

    public double getTiempoInicioHorno() {
        return tiempoInicioHorno;
    }

    public void setTiempoInicioHorno(double tiempoInicioHorno) {
        this.tiempoInicioHorno = tiempoInicioHorno;
    }

    public double getTiempoFinHorno() {
        return tiempoFinHorno;
    }

    public void setTiempoFinHorno(double tiempoFinHorno) {
        this.tiempoFinHorno = tiempoFinHorno;
    }

    public double getTiempoInicioInspeccion() {
        return tiempoInicioInspeccion;
    }

    public void setTiempoInicioInspeccion(double tiempoInicioInspeccion) {
        this.tiempoInicioInspeccion = tiempoInicioInspeccion;
    }

    public double getTiempoFinInspeccion() {
        return tiempoFinInspeccion;
    }

    public void setTiempoFinInspeccion(double tiempoFinInspeccion) {
        this.tiempoFinInspeccion = tiempoFinInspeccion;
    }

    public double getTiempoSalida() {
        return tiempoSalida;
    }

    public void setTiempoSalida(double tiempoSalida) {
        this.tiempoSalida = tiempoSalida;
    }

    @Override
    public String toString() {
        return "Pieza #" + id;
    }
}
