package com.simulacion.motor;

import com.simulacion.estadisticas.EstadisticasSimulacion;

/**
 * Interface para recibir notificaciones de eventos de la simulacion
 * Permite actualizar la UI en tiempo real
 */
public interface SimulacionListener {

    /**
     * Se invoca cuando hay una actualizacion en la simulacion
     * @param tiempoActual tiempo actual de la simulacion
     * @param estadisticas estadisticas actuales
     */
    void onActualizacion(double tiempoActual, EstadisticasSimulacion estadisticas);

    /**
     * Se invoca cuando la simulacion finaliza
     * @param estadisticas estadisticas finales
     */
    void onFinalizacion(EstadisticasSimulacion estadisticas);
}
