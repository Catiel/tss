package com.simulacion.ui;

import com.simulacion.estadisticas.EstadisticasSimulacion;
import com.simulacion.estadisticas.ResultadoReplica;
import com.simulacion.modelo.SimulacionParametros;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PanelEstadisticas extends JPanel {

    private JTextArea textArea;
    private JScrollPane scrollPane;
    private SimulacionParametros parametrosActuales;

    public PanelEstadisticas() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("Courier New", Font.PLAIN, 10));

        scrollPane = new JScrollPane(textArea);
        add(scrollPane, BorderLayout.CENTER);

        limpiar();
    }

    public void limpiar() {
        textArea.setText("Esperando inicio de simulación...\n");
        parametrosActuales = null;
    }

    public void setParametros(SimulacionParametros parametros) {
        this.parametrosActuales = parametros;
    }

    public void actualizar(double tiempoActual, EstadisticasSimulacion stats, SimulacionParametros params) {
        this.parametrosActuales = params;
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════\n");
        sb.append(" ESTADÍSTICAS TIEMPO REAL\n");
        sb.append("═══════════════════════════\n\n");

        sb.append(String.format("Tiempo: %.0f min\n\n", tiempoActual));

        sb.append("CONTADORES\n");
        sb.append("───────────────────────────\n");
        sb.append(String.format("Recepción:      %3d\n", stats.getPiezasEnRecepcion()));
        sb.append(String.format("Lavado:         %3d\n", stats.getPiezasEnLavado()));
        sb.append(String.format("Alm. Pintura:   %3d\n", stats.getPiezasEnAlmacenPintura()));
        sb.append(String.format("Pintura:        %3d\n", stats.getPiezasEnPintura()));
        sb.append(String.format("Alm. Horno:     %3d\n", stats.getPiezasEnAlmacenHorno()));
        sb.append(String.format("Horno:          %3d\n", stats.getPiezasEnHorno()));
        sb.append(String.format("Inspección:     %3d\n", stats.getPiezasEnInspeccion()));
        sb.append(String.format("Completadas:    %3d\n\n", stats.getPiezasCompletadas()));

        if (tiempoActual > 0 && params != null) {
            sb.append("UTILIZACIÓN\n");
            sb.append("───────────────────────────\n");
            sb.append(String.format("Lavado:      %5.2f%%\n",
                stats.getUtilizacionLavado(tiempoActual, params.getCapacidadLavadora())));
            sb.append(String.format("Pintura:     %5.2f%%\n",
                stats.getUtilizacionPintura(tiempoActual, params.getCapacidadPintura())));
            sb.append(String.format("Horno:       %5.2f%%\n",
                stats.getUtilizacionHorno(tiempoActual, params.getCapacidadHorno())));
            sb.append(String.format("Inspección:  %5.2f%%\n\n",
                stats.getUtilizacionInspeccion(tiempoActual, params.getNumeroInspectores())));
        }

        sb.append("═══════════════════════════\n");

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
    }

    public void mostrarResumenReplicas(List<ResultadoReplica> replicas, SimulacionParametros params) {
        StringBuilder sb = new StringBuilder();

        sb.append("═══════════════════════════════════════════════\n");
        sb.append("      RESUMEN CONSOLIDADO - PROMEDIO\n");
        sb.append("═══════════════════════════════════════════════\n\n");

        sb.append("CONFIGURACIÓN:\n");
        sb.append(String.format("Réplicas ejecutadas: %d\n", replicas.size()));
        sb.append(String.format("Días por réplica: %d\n", params.getDiasSimulacion()));
        sb.append(String.format("Horas por día: %d\n", params.getHorasPorDia()));
        sb.append(String.format("Tiempo total: %.0f min\n\n", params.getTiempoTotalSimulacion()));

        // Calcular promedios
        double avgPiezasCompletadas = replicas.stream().mapToInt(ResultadoReplica::getPiezasCompletadas).average().orElse(0);
        double avgPiezasEnSistema = replicas.stream().mapToInt(ResultadoReplica::getPiezasEnSistema).average().orElse(0);
        double avgTiempoEnSistema = replicas.stream().mapToDouble(ResultadoReplica::getTiempoPromedioEnSistema).average().orElse(0);
        double avgTiempoEspera = replicas.stream().mapToDouble(ResultadoReplica::getTiempoPromedioEspera).average().orElse(0);

        double avgUtilLavado = replicas.stream().mapToDouble(ResultadoReplica::getUtilizacionLavado).average().orElse(0);
        double avgUtilPintura = replicas.stream().mapToDouble(ResultadoReplica::getUtilizacionPintura).average().orElse(0);
        double avgUtilHorno = replicas.stream().mapToDouble(ResultadoReplica::getUtilizacionHorno).average().orElse(0);
        double avgUtilInspeccion = replicas.stream().mapToDouble(ResultadoReplica::getUtilizacionInspeccion).average().orElse(0);

        int maxColaRecepcion = replicas.stream().mapToInt(ResultadoReplica::getMaxColaRecepcion).max().orElse(0);
        int maxColaAlmPintura = replicas.stream().mapToInt(ResultadoReplica::getMaxColaAlmacenPintura).max().orElse(0);
        int maxColaAlmHorno = replicas.stream().mapToInt(ResultadoReplica::getMaxColaAlmacenHorno).max().orElse(0);
        int maxColaInspeccion = replicas.stream().mapToInt(ResultadoReplica::getMaxColaInspeccion).max().orElse(0);

        sb.append("RESULTADOS PROMEDIO:\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("Piezas Completadas:     %.2f\n", avgPiezasCompletadas));
        sb.append(String.format("Piezas en Sistema:      %.2f\n\n", avgPiezasEnSistema));

        sb.append(String.format("Tiempo Prom. en Sistema:  %.2f min\n", avgTiempoEnSistema));
        sb.append(String.format("Tiempo Prom. de Espera:   %.2f min\n\n", avgTiempoEspera));

        sb.append("UTILIZACIÓN PROMEDIO:\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("Lavado:       %.2f%%\n", avgUtilLavado));
        sb.append(String.format("Pintura:      %.2f%%\n", avgUtilPintura));
        sb.append(String.format("Horno:        %.2f%%\n", avgUtilHorno));
        sb.append(String.format("Inspección:   %.2f%%\n\n", avgUtilInspeccion));

        sb.append("COLAS MÁXIMAS OBSERVADAS:\n");
        sb.append("───────────────────────────────────────────────\n");
        sb.append(String.format("Recepción:      %d piezas\n", maxColaRecepcion));
        sb.append(String.format("Alm. Pintura:   %d piezas\n", maxColaAlmPintura));
        sb.append(String.format("Alm. Horno:     %d piezas\n", maxColaAlmHorno));
        sb.append(String.format("Inspección:     %d piezas\n\n", maxColaInspeccion));

        sb.append("ANÁLISIS ECONÓMICO:\n");
        sb.append("───────────────────────────────────────────────\n");
        double utilidadTotal = avgPiezasCompletadas * params.getUtilidadPorPieza();
        sb.append(String.format("Utilidad Total (prom):  $%.2f\n", utilidadTotal));
        sb.append(String.format("Utilidad/Pieza:         $%.2f\n", params.getUtilidadPorPieza()));
        sb.append(String.format("Throughput:             %.2f piezas/día\n\n",
            avgPiezasCompletadas / params.getDiasSimulacion()));

        sb.append("═══════════════════════════════════════════════\n");
        sb.append("         SIMULACIÓN COMPLETADA\n");
        sb.append("═══════════════════════════════════════════════\n");

        textArea.setText(sb.toString());
        textArea.setCaretPosition(0);
    }
}
