package com.simulacion.ui;

import com.simulacion.estadisticas.EstadisticasSimulacion;
import com.simulacion.motor.SimulacionListener;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import javax.swing.*;
import java.awt.*;

public class PanelGraficas extends JPanel implements SimulacionListener {

    private XYSeries seriePiezasEnSistema;
    private XYSeries seriePiezasCompletadas;
    private XYSeries serieColaRecepcion;
    private XYSeries serieColaAlmacenPintura;
    private XYSeries serieColaAlmacenHorno;

    private ChartPanel chartPanelPiezas;
    private ChartPanel chartPanelColas;

    // Control de actualizacion
    private double ultimoTiempoGraficado = -100;
    private static final double INTERVALO_GRAFICO = 50; // Graficar cada 50 minutos

    public PanelGraficas() {
        setLayout(new GridLayout(1, 2, 10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        crearGraficas();
    }

    private void crearGraficas() {
        seriePiezasEnSistema = new XYSeries("Piezas en Sistema");
        seriePiezasCompletadas = new XYSeries("Piezas Completadas");
        serieColaRecepcion = new XYSeries("Cola Recepción");
        serieColaAlmacenPintura = new XYSeries("Cola Alm. Pintura");
        serieColaAlmacenHorno = new XYSeries("Cola Alm. Horno");

        // Grafica 1: Piezas
        XYSeriesCollection datasetPiezas = new XYSeriesCollection();
        datasetPiezas.addSeries(seriePiezasEnSistema);
        datasetPiezas.addSeries(seriePiezasCompletadas);

        JFreeChart chartPiezas = ChartFactory.createXYLineChart(
            "Piezas en el Sistema",
            "Tiempo (minutos)",
            "Cantidad de Piezas",
            datasetPiezas,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        personalizarGrafica(chartPiezas);
        chartPanelPiezas = new ChartPanel(chartPiezas);
        chartPanelPiezas.setPreferredSize(new Dimension(400, 300));

        // Grafica 2: Colas
        XYSeriesCollection datasetColas = new XYSeriesCollection();
        datasetColas.addSeries(serieColaRecepcion);
        datasetColas.addSeries(serieColaAlmacenPintura);
        datasetColas.addSeries(serieColaAlmacenHorno);

        JFreeChart chartColas = ChartFactory.createXYLineChart(
            "Longitud de Colas",
            "Tiempo (minutos)",
            "Número de Piezas en Cola",
            datasetColas,
            PlotOrientation.VERTICAL,
            true,
            true,
            false
        );

        personalizarGrafica(chartColas);
        chartPanelColas = new ChartPanel(chartColas);
        chartPanelColas.setPreferredSize(new Dimension(400, 300));

        add(chartPanelPiezas);
        add(chartPanelColas);
    }

    private void personalizarGrafica(JFreeChart chart) {
        XYPlot plot = chart.getXYPlot();
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.LIGHT_GRAY);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);

        // Mejorar la legibilidad
        chart.getLegend().setItemFont(new Font("Arial", Font.PLAIN, 10));
        plot.getDomainAxis().setLabelFont(new Font("Arial", Font.PLAIN, 11));
        plot.getRangeAxis().setLabelFont(new Font("Arial", Font.PLAIN, 11));
    }

    public void limpiar() {
        seriePiezasEnSistema.clear();
        seriePiezasCompletadas.clear();
        serieColaRecepcion.clear();
        serieColaAlmacenPintura.clear();
        serieColaAlmacenHorno.clear();
        ultimoTiempoGraficado = -100;
    }

    @Override
    public void onActualizacion(double tiempoActual, EstadisticasSimulacion estadisticas) {
        // Actualizar solo si ha pasado suficiente tiempo O es el primer punto
        if (tiempoActual - ultimoTiempoGraficado >= INTERVALO_GRAFICO ||
            seriePiezasEnSistema.getItemCount() == 0) {

            SwingUtilities.invokeLater(() -> {
                try {
                    // Agregar datos a las series
                    seriePiezasEnSistema.add(tiempoActual, estadisticas.getPiezasEnSistema());
                    seriePiezasCompletadas.add(tiempoActual, estadisticas.getPiezasCompletadas());
                    serieColaRecepcion.add(tiempoActual, estadisticas.getPiezasEnRecepcion());
                    serieColaAlmacenPintura.add(tiempoActual, estadisticas.getPiezasEnAlmacenPintura());
                    serieColaAlmacenHorno.add(tiempoActual, estadisticas.getPiezasEnAlmacenHorno());

                    // Limitar el numero de puntos para mantener rendimiento
                    limitarPuntos(seriePiezasEnSistema, 2000);
                    limitarPuntos(seriePiezasCompletadas, 2000);
                    limitarPuntos(serieColaRecepcion, 2000);
                    limitarPuntos(serieColaAlmacenPintura, 2000);
                    limitarPuntos(serieColaAlmacenHorno, 2000);

                } catch (Exception e) {
                    System.err.println("Error actualizando gráficas: " + e.getMessage());
                }
            });

            ultimoTiempoGraficado = tiempoActual;
        }
    }

    @Override
    public void onFinalizacion(EstadisticasSimulacion estadisticas) {
        // Agregar punto final si no está ya agregado
        SwingUtilities.invokeLater(() -> {
            if (seriePiezasEnSistema.getItemCount() > 0) {
                double ultimoTiempo = seriePiezasEnSistema.getX(
                    seriePiezasEnSistema.getItemCount() - 1).doubleValue();

                // Solo agregar si el último punto no es el final
                if (Math.abs(ultimoTiempo - ultimoTiempoGraficado) > INTERVALO_GRAFICO / 2) {
                    onActualizacion(ultimoTiempoGraficado, estadisticas);
                }
            }
        });
    }

    /**
     * Limita el numero de puntos en una serie para mantener rendimiento
     * Elimina puntos intermedios manteniendo inicio y fin
     */
    private void limitarPuntos(XYSeries serie, int maxPuntos) {
        if (serie.getItemCount() > maxPuntos) {
            // Calcular cuantos puntos eliminar
            int puntosAEliminar = serie.getItemCount() - maxPuntos;

            // Eliminar puntos del medio (mantener inicio y fin)
            for (int i = 0; i < puntosAEliminar; i++) {
                if (serie.getItemCount() > maxPuntos) {
                    // Eliminar punto en posicion intermedia
                    int posicionEliminar = serie.getItemCount() / 2;
                    serie.remove(posicionEliminar);
                }
            }
        }
    }
}
