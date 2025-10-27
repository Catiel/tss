package com.simulacion.motor;

import com.simulacion.estadisticas.EstadisticasSimulacion;
import com.simulacion.modelo.Pieza;
import com.simulacion.modelo.SimulacionParametros;
import com.simulacion.util.GeneradorAleatorio;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Queue;

public class MotorSimulacion {

    private final SimulacionParametros parametros;
    private PriorityQueue<EventoSimulacion> colaEventos;
    private double tiempoActual;
    private EstadisticasSimulacion estadisticas;

    private Queue<Pieza> colaRecepcion;
    private Queue<Pieza> colaAlmacenPintura;
    private Queue<Pieza> colaAlmacenHorno;
    private Queue<Pieza> colaInspeccion;

    private int capacidadDisponibleLavado;
    private int capacidadDisponiblePintura;
    private int capacidadDisponibleHorno;
    private int inspectoresDisponibles;

    private List<Pieza> piezasEnLavado;
    private List<Pieza> piezasEnPintura;
    private List<Pieza> piezasEnHorno;
    private List<Pieza> piezasEnInspeccion;

    private List<Double> tiemposInicioLavado;
    private List<Double> tiemposInicioPintura;
    private List<Double> tiemposInicioHorno;
    private List<Double> tiemposInicioInspeccion;

    private boolean simulacionActiva;
    private boolean pausada;
    private List<SimulacionListener> listeners;

    public MotorSimulacion(SimulacionParametros parametros) {
        this.parametros = parametros;
        this.listeners = new ArrayList<>();
        inicializar();
    }

    public void inicializar() {
        tiempoActual = 0;
        colaEventos = new PriorityQueue<>();
        estadisticas = new EstadisticasSimulacion();

        colaRecepcion = new LinkedList<>();
        colaAlmacenPintura = new LinkedList<>();
        colaAlmacenHorno = new LinkedList<>();
        colaInspeccion = new LinkedList<>();

        capacidadDisponibleLavado = parametros.getCapacidadLavadora();
        capacidadDisponiblePintura = parametros.getCapacidadPintura();
        capacidadDisponibleHorno = parametros.getCapacidadHorno();
        inspectoresDisponibles = parametros.getNumeroInspectores();

        piezasEnLavado = new ArrayList<>();
        piezasEnPintura = new ArrayList<>();
        piezasEnHorno = new ArrayList<>();
        piezasEnInspeccion = new ArrayList<>();

        tiemposInicioLavado = new ArrayList<>();
        tiemposInicioPintura = new ArrayList<>();
        tiemposInicioHorno = new ArrayList<>();
        tiemposInicioInspeccion = new ArrayList<>();

        simulacionActiva = false;
        pausada = false;

        Pieza.reiniciarContador();

        programarProximoArribo(0);

        double tiempoFin = parametros.getTiempoTotalSimulacion();
        colaEventos.add(new EventoSimulacion(tiempoFin, EventoSimulacion.TipoEvento.FIN_SIMULACION));
    }

    private void programarProximoArribo(double tiempoBase) {
        double tiempoEntreArribos = GeneradorAleatorio.exponencial(parametros.getMediaArribos());
        double tiempoArribo = tiempoBase + tiempoEntreArribos;

        if (tiempoArribo < parametros.getTiempoTotalSimulacion()) {
            Pieza nuevaPieza = new Pieza(tiempoArribo);
            colaEventos.add(new EventoSimulacion(tiempoArribo,
                EventoSimulacion.TipoEvento.LLEGADA_PIEZA, nuevaPieza));
        }
    }

    public void ejecutarSimulacion() {
    simulacionActiva = true;
    int contador = 0;

    // CAMBIO: Frecuencia basada en tiempo, no en eventos
    double tiempoUltimaNotificacion = 0;
    double intervaloNotificacion = parametros.isModoRapido() ?
        parametros.getTiempoTotalSimulacion() / 100 : // 100 actualizaciones en modo rapido
        1.0; // Cada minuto en modo visual

    while (!colaEventos.isEmpty() && simulacionActiva) {
        while (pausada && simulacionActiva) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }

        if (!simulacionActiva) break;

        EventoSimulacion evento = colaEventos.poll();
        if (evento == null) break;

        tiempoActual = evento.getTiempo();
        estadisticas.actualizarIntegrales(tiempoActual);
        procesarEvento(evento);

        contador++;

        // Notificar basado en tiempo transcurrido, no en eventos
        if (tiempoActual - tiempoUltimaNotificacion >= intervaloNotificacion) {
            notificarActualizacion();
            tiempoUltimaNotificacion = tiempoActual;
        }

        // Pausa para visualizacion solo en modo visual
        if (!parametros.isModoRapido() && parametros.getVelocidadSimulacion() > 0) {
            try {
                Thread.sleep(parametros.getVelocidadSimulacion());
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            }
        }
    }

    simulacionActiva = false;
    notificarActualizacion(); // Notificacion final
    notificarFinalizacion();
}


    private void procesarEvento(EventoSimulacion evento) {
        switch (evento.getTipo()) {
            case LLEGADA_PIEZA:
                procesarLlegadaPieza(evento.getPieza());
                break;
            case INICIO_LAVADO:
                procesarInicioLavado(evento.getPieza());
                break;
            case FIN_LAVADO:
                procesarFinLavado(evento.getPieza());
                break;
            case LLEGADA_ALMACEN_PINTURA:
                procesarLlegadaAlmacenPintura(evento.getPieza());
                break;
            case INICIO_PINTURA:
                procesarInicioPintura(evento.getPieza());
                break;
            case FIN_PINTURA:
                procesarFinPintura(evento.getPieza());
                break;
            case LLEGADA_ALMACEN_HORNO:
                procesarLlegadaAlmacenHorno(evento.getPieza());
                break;
            case INICIO_HORNO:
                procesarInicioHorno(evento.getPieza());
                break;
            case FIN_HORNO:
                procesarFinHorno(evento.getPieza());
                break;
            case LLEGADA_INSPECCION:
                procesarLlegadaInspeccion(evento.getPieza());
                break;
            case INICIO_INSPECCION:
                procesarInicioInspeccion(evento.getPieza());
                break;
            case FIN_INSPECCION:
                procesarFinInspeccion(evento.getPieza());
                break;
            case FIN_SIMULACION:
                simulacionActiva = false;
                break;
        }
    }

    // RECEPCION -> LAVADO
    private void procesarLlegadaPieza(Pieza pieza) {
        estadisticas.registrarNuevaPieza();
        colaRecepcion.add(pieza);
        actualizarContadores();
        programarProximoArribo(tiempoActual);
        intentarIniciarLavado();
    }

    private void intentarIniciarLavado() {
        while (!colaRecepcion.isEmpty() && capacidadDisponibleLavado > 0) {
            Pieza pieza = colaRecepcion.poll();
            capacidadDisponibleLavado--;

            double tiempoTraslado = GeneradorAleatorio.exponencial(
                parametros.getMediaTrasladoRecepcionLavado());
            double tiempoLlegada = tiempoActual + tiempoTraslado;

            colaEventos.add(new EventoSimulacion(tiempoLlegada,
                EventoSimulacion.TipoEvento.INICIO_LAVADO, pieza));

            actualizarContadores();
        }
    }

    private void procesarInicioLavado(Pieza pieza) {
        pieza.setTiempoInicioLavado(tiempoActual);
        piezasEnLavado.add(pieza);
        tiemposInicioLavado.add(tiempoActual);

        double tiempoLavado = GeneradorAleatorio.normal(
            parametros.getMediaTiempoLavado(),
            parametros.getDesviacionTiempoLavado());

        double tiempoFin = tiempoActual + tiempoLavado;
        pieza.setTiempoFinLavado(tiempoFin);

        colaEventos.add(new EventoSimulacion(tiempoFin,
            EventoSimulacion.TipoEvento.FIN_LAVADO, pieza));

        actualizarContadores();
    }

    private void procesarFinLavado(Pieza pieza) {
        int index = piezasEnLavado.indexOf(pieza);
        if (index >= 0) {
            piezasEnLavado.remove(index);
            double tiempoInicio = tiemposInicioLavado.remove(index);
            estadisticas.agregarTiempoOcupadoLavado(tiempoActual - tiempoInicio);
        }
        capacidadDisponibleLavado++;

        double tiempoTraslado = GeneradorAleatorio.exponencial(
            parametros.getMediaTrasladoLavadoPintura());
        double tiempoLlegada = tiempoActual + tiempoTraslado;

        colaEventos.add(new EventoSimulacion(tiempoLlegada,
            EventoSimulacion.TipoEvento.LLEGADA_ALMACEN_PINTURA, pieza));

        intentarIniciarLavado();
        actualizarContadores();
    }

    // ALMACEN PINTURA -> PINTURA
    private void procesarLlegadaAlmacenPintura(Pieza pieza) {
        pieza.setTiempoInicioEsperaPintura(tiempoActual);
        colaAlmacenPintura.add(pieza);
        actualizarContadores();
        intentarIniciarPintura();
    }

    private void intentarIniciarPintura() {
        while (!colaAlmacenPintura.isEmpty() && capacidadDisponiblePintura > 0) {
            Pieza pieza = colaAlmacenPintura.poll();
            capacidadDisponiblePintura--;

            colaEventos.add(new EventoSimulacion(tiempoActual,
                EventoSimulacion.TipoEvento.INICIO_PINTURA, pieza));

            actualizarContadores();
        }
    }

    private void procesarInicioPintura(Pieza pieza) {
        pieza.setTiempoInicioPintura(tiempoActual);
        piezasEnPintura.add(pieza);
        tiemposInicioPintura.add(tiempoActual);

        double tiempoPintura = GeneradorAleatorio.triangular(
            parametros.getMinPintura(),
            parametros.getModaPintura(),
            parametros.getMaxPintura());

        double tiempoFin = tiempoActual + tiempoPintura;
        pieza.setTiempoFinPintura(tiempoFin);

        colaEventos.add(new EventoSimulacion(tiempoFin,
            EventoSimulacion.TipoEvento.FIN_PINTURA, pieza));

        actualizarContadores();
    }

    private void procesarFinPintura(Pieza pieza) {
        int index = piezasEnPintura.indexOf(pieza);
        if (index >= 0) {
            piezasEnPintura.remove(index);
            double tiempoInicio = tiemposInicioPintura.remove(index);
            estadisticas.agregarTiempoOcupadoPintura(tiempoActual - tiempoInicio);
        }
        capacidadDisponiblePintura++;

        double tiempoTraslado = GeneradorAleatorio.uniformeCentroAmplitud(
            parametros.getCentroTrasladoPinturaHorno(),
            parametros.getAmplitudTrasladoPinturaHorno());
        double tiempoLlegada = tiempoActual + tiempoTraslado;

        colaEventos.add(new EventoSimulacion(tiempoLlegada,
            EventoSimulacion.TipoEvento.LLEGADA_ALMACEN_HORNO, pieza));

        intentarIniciarPintura();
        actualizarContadores();
    }

    // ALMACEN HORNO -> HORNO
    private void procesarLlegadaAlmacenHorno(Pieza pieza) {
        pieza.setTiempoInicioEsperaHorno(tiempoActual);
        colaAlmacenHorno.add(pieza);
        actualizarContadores();
        intentarIniciarHorno();
    }

    private void intentarIniciarHorno() {
        while (!colaAlmacenHorno.isEmpty() && capacidadDisponibleHorno > 0) {
            Pieza pieza = colaAlmacenHorno.poll();
            capacidadDisponibleHorno--;

            colaEventos.add(new EventoSimulacion(tiempoActual,
                EventoSimulacion.TipoEvento.INICIO_HORNO, pieza));

            actualizarContadores();
        }
    }

    private void procesarInicioHorno(Pieza pieza) {
        pieza.setTiempoInicioHorno(tiempoActual);
        piezasEnHorno.add(pieza);
        tiemposInicioHorno.add(tiempoActual);

        double tiempoHorno = GeneradorAleatorio.uniformeCentroAmplitud(
            parametros.getCentroHorno(),
            parametros.getAmplitudHorno());

        double tiempoFin = tiempoActual + tiempoHorno;
        pieza.setTiempoFinHorno(tiempoFin);

        colaEventos.add(new EventoSimulacion(tiempoFin,
            EventoSimulacion.TipoEvento.FIN_HORNO, pieza));

        actualizarContadores();
    }

    private void procesarFinHorno(Pieza pieza) {
        int index = piezasEnHorno.indexOf(pieza);
        if (index >= 0) {
            piezasEnHorno.remove(index);
            double tiempoInicio = tiemposInicioHorno.remove(index);
            estadisticas.agregarTiempoOcupadoHorno(tiempoActual - tiempoInicio);
        }
        capacidadDisponibleHorno++;

        double tiempoTraslado = GeneradorAleatorio.uniformeCentroAmplitud(
            parametros.getCentroTrasladoHornoInspeccion(),
            parametros.getAmplitudTrasladoHornoInspeccion());
        double tiempoLlegada = tiempoActual + tiempoTraslado;

        colaEventos.add(new EventoSimulacion(tiempoLlegada,
            EventoSimulacion.TipoEvento.LLEGADA_INSPECCION, pieza));

        intentarIniciarHorno();
        actualizarContadores();
    }

    // INSPECCION -> SALIDA
    private void procesarLlegadaInspeccion(Pieza pieza) {
        colaInspeccion.add(pieza);
        actualizarContadores();
        intentarIniciarInspeccion();
    }

    private void intentarIniciarInspeccion() {
        while (!colaInspeccion.isEmpty() && inspectoresDisponibles > 0) {
            Pieza pieza = colaInspeccion.poll();
            inspectoresDisponibles--;

            colaEventos.add(new EventoSimulacion(tiempoActual,
                EventoSimulacion.TipoEvento.INICIO_INSPECCION, pieza));

            actualizarContadores();
        }
    }

    private void procesarInicioInspeccion(Pieza pieza) {
        pieza.setTiempoInicioInspeccion(tiempoActual);
        piezasEnInspeccion.add(pieza);
        tiemposInicioInspeccion.add(tiempoActual);

        double tiempoTotal = 0;
        for (int i = 0; i < parametros.getElementosPorPieza(); i++) {
            tiempoTotal += GeneradorAleatorio.exponencial(
                parametros.getMediaTiempoInspeccionElemento());
        }

        double tiempoFin = tiempoActual + tiempoTotal;
        pieza.setTiempoFinInspeccion(tiempoFin);

        colaEventos.add(new EventoSimulacion(tiempoFin,
            EventoSimulacion.TipoEvento.FIN_INSPECCION, pieza));

        actualizarContadores();
    }

    private void procesarFinInspeccion(Pieza pieza) {
        int index = piezasEnInspeccion.indexOf(pieza);
        if (index >= 0) {
            piezasEnInspeccion.remove(index);
            double tiempoInicio = tiemposInicioInspeccion.remove(index);
            estadisticas.agregarTiempoOcupadoInspeccion(tiempoActual - tiempoInicio);
        }
        inspectoresDisponibles++;
        pieza.setTiempoSalida(tiempoActual);

        estadisticas.registrarPiezaCompletada(pieza);
        intentarIniciarInspeccion();
        actualizarContadores();
    }

    private void actualizarContadores() {
        estadisticas.setPiezasEnRecepcion(colaRecepcion.size());
        estadisticas.setPiezasEnLavado(piezasEnLavado.size());
        estadisticas.setPiezasEnAlmacenPintura(colaAlmacenPintura.size());
        estadisticas.setPiezasEnPintura(piezasEnPintura.size());
        estadisticas.setPiezasEnAlmacenHorno(colaAlmacenHorno.size());
        estadisticas.setPiezasEnHorno(piezasEnHorno.size());
        estadisticas.setPiezasEnInspeccion(piezasEnInspeccion.size());
    }

    private void notificarActualizacion() {
        for (SimulacionListener listener : listeners) {
            listener.onActualizacion(tiempoActual, estadisticas);
        }
    }

    private void notificarFinalizacion() {
        for (SimulacionListener listener : listeners) {
            listener.onFinalizacion(estadisticas);
        }
    }

    public void pausar() {
        pausada = true;
    }

    public void reanudar() {
        pausada = false;
    }

    public void detener() {
        simulacionActiva = false;
    }

    public boolean isSimulacionActiva() {
        return simulacionActiva;
    }

    public boolean isPausada() {
        return pausada;
    }

    public void agregarListener(SimulacionListener listener) {
        listeners.add(listener);
    }

    public void removerListener(SimulacionListener listener) {
        listeners.remove(listener);
    }

    public double getTiempoActual() {
        return tiempoActual;
    }

    public EstadisticasSimulacion getEstadisticas() {
        return estadisticas;
    }

    public SimulacionParametros getParametros() {
        return parametros;
    }
}
