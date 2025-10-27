package com.simulacion.motor;

import com.simulacion.modelo.Pieza;

public class EventoSimulacion implements Comparable<EventoSimulacion> {

    public enum TipoEvento {
        LLEGADA_PIEZA,
        INICIO_LAVADO,
        FIN_LAVADO,
        LLEGADA_ALMACEN_PINTURA,
        INICIO_PINTURA,
        FIN_PINTURA,
        LLEGADA_ALMACEN_HORNO,
        INICIO_HORNO,
        FIN_HORNO,
        LLEGADA_INSPECCION,
        INICIO_INSPECCION,
        FIN_INSPECCION,
        FIN_SIMULACION
    }

    private final double tiempo;
    private final TipoEvento tipo;
    private final Pieza pieza;

    public EventoSimulacion(double tiempo, TipoEvento tipo, Pieza pieza) {
        this.tiempo = tiempo;
        this.tipo = tipo;
        this.pieza = pieza;
    }

    public EventoSimulacion(double tiempo, TipoEvento tipo) {
        this(tiempo, tipo, null);
    }

    public double getTiempo() {
        return tiempo;
    }

    public TipoEvento getTipo() {
        return tipo;
    }

    public Pieza getPieza() {
        return pieza;
    }

    @Override
    public int compareTo(EventoSimulacion otro) {
        return Double.compare(this.tiempo, otro.tiempo);
    }

    @Override
    public String toString() {
        return String.format("Evento[t=%.2f, tipo=%s, pieza=%s]",
            tiempo, tipo, pieza != null ? pieza.toString() : "N/A");
    }
}
