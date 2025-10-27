package com.simulacion.ui;

import com.simulacion.estadisticas.EstadisticasSimulacion;
import com.simulacion.motor.SimulacionListener;

import javax.swing.*;
import java.awt.*;
import java.awt.geom.*;

/**
 * Panel que muestra la animacion visual del proceso de produccion
 * Dibuja las estaciones y las piezas moviendose entre ellas
 */
public class PanelAnimacion extends JPanel implements SimulacionListener {

    // Contadores de piezas en cada estacion (para visualizar)
    private int piezasEnRecepcion = 0;
    private int piezasEnLavado = 0;
    private int piezasEnAlmacenPintura = 0;
    private int piezasEnPintura = 0;
    private int piezasEnAlmacenHorno = 0;
    private int piezasEnHorno = 0;
    private int piezasEnInspeccion = 0;
    private int piezasCompletadas = 0;

    // Colores para las estaciones
    private static final Color COLOR_RECEPCION = new Color(200, 200, 200);
    private static final Color COLOR_LAVADO = new Color(135, 206, 250);
    private static final Color COLOR_ALMACEN_PINTURA = new Color(255, 228, 181);
    private static final Color COLOR_PINTURA = new Color(255, 165, 0);
    private static final Color COLOR_ALMACEN_HORNO = new Color(255, 218, 185);
    private static final Color COLOR_HORNO = new Color(255, 69, 0);
    private static final Color COLOR_INSPECCION = new Color(144, 238, 144);
    private static final Color COLOR_PIEZA = new Color(139, 69, 19);

    /**
     * Constructor del panel de animacion
     */
    public PanelAnimacion() {
        setBackground(Color.WHITE);
        setPreferredSize(new Dimension(800, 400));
        setBorder(BorderFactory.createLineBorder(Color.GRAY, 2));
    }

    /**
     * Limpia la animacion
     */
    public void limpiar() {
        piezasEnRecepcion = 0;
        piezasEnLavado = 0;
        piezasEnAlmacenPintura = 0;
        piezasEnPintura = 0;
        piezasEnAlmacenHorno = 0;
        piezasEnHorno = 0;
        piezasEnInspeccion = 0;
        piezasCompletadas = 0;
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;

        // Activar antialiasing para mejor calidad
        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = getWidth();
        int height = getHeight();

        // Dimensiones de las estaciones
        int stationWidth = 120;
        int stationHeight = 80;
        int smallStationWidth = 80;
        int smallStationHeight = 60;

        // Fila 1: Recepcion -> Lavado -> Almacen Pintura
        int y1 = 50;
        int x1 = 50;
        int x2 = x1 + stationWidth + 80;
        int x3 = x2 + stationWidth + 80;

        // Fila 2: Pintura -> Almacen Horno -> Horno
        int y2 = y1 + stationHeight + 60;
        int x4 = 50;
        int x5 = x4 + stationWidth + 80;
        int x6 = x5 + stationWidth + 80;

        // Fila 3: Inspeccion -> Salida
        int y3 = y2 + stationHeight + 60;
        int x7 = 200;
        int x8 = x7 + stationWidth + 100;

        // Dibujar estaciones y conexiones

        // Recepcion
        dibujarEstacion(g2d, x1, y1, stationWidth, stationHeight, "RECEPCIÓN",
            COLOR_RECEPCION, piezasEnRecepcion);

        // Flecha a Lavado
        dibujarFlecha(g2d, x1 + stationWidth, y1 + stationHeight/2,
            x2, y1 + stationHeight/2);

        // Lavado
        dibujarEstacion(g2d, x2, y1, stationWidth, stationHeight, "LAVADO",
            COLOR_LAVADO, piezasEnLavado);

        // Flecha a Almacen Pintura
        dibujarFlecha(g2d, x2 + stationWidth, y1 + stationHeight/2,
            x3, y1 + stationHeight/2);

        // Almacen Pintura
        dibujarEstacion(g2d, x3, y1, stationWidth, stationHeight, "ALM. PINTURA",
            COLOR_ALMACEN_PINTURA, piezasEnAlmacenPintura);

        // Flecha curva hacia Pintura
        dibujarFlechaCurva(g2d, x3 + stationWidth/2, y1 + stationHeight,
            x4 + stationWidth/2, y2);

        // Pintura
        dibujarEstacion(g2d, x4, y2, stationWidth, stationHeight, "PINTURA",
            COLOR_PINTURA, piezasEnPintura);

        // Flecha a Almacen Horno
        dibujarFlecha(g2d, x4 + stationWidth, y2 + stationHeight/2,
            x5, y2 + stationHeight/2);

        // Almacen Horno
        dibujarEstacion(g2d, x5, y2, stationWidth, stationHeight, "ALM. HORNO",
            COLOR_ALMACEN_HORNO, piezasEnAlmacenHorno);

        // Flecha a Horno
        dibujarFlecha(g2d, x5 + stationWidth, y2 + stationHeight/2,
            x6, y2 + stationHeight/2);

        // Horno
        dibujarEstacion(g2d, x6, y2, stationWidth, stationHeight, "HORNO",
            COLOR_HORNO, piezasEnHorno);

        // Flecha curva hacia Inspeccion
        dibujarFlechaCurva(g2d, x6 + stationWidth/2, y2 + stationHeight,
            x7 + stationWidth/2, y3);

        // Inspeccion
        dibujarEstacion(g2d, x7, y3, stationWidth, stationHeight, "INSPECCIÓN",
            COLOR_INSPECCION, piezasEnInspeccion);

        // Flecha a Salida
        dibujarFlecha(g2d, x7 + stationWidth, y3 + stationHeight/2,
            x8, y3 + stationHeight/2);

        // Salida (contador)
        dibujarSalida(g2d, x8, y3, smallStationWidth, smallStationHeight, piezasCompletadas);

        // Leyenda
        dibujarLeyenda(g2d, width - 200, 20);
    }

    /**
     * Dibuja una estacion con su nombre y contador de piezas
     */
    private void dibujarEstacion(Graphics2D g2d, int x, int y, int width, int height,
                                  String nombre, Color color, int piezas) {
        // Sombra
        g2d.setColor(new Color(0, 0, 0, 50));
        g2d.fillRoundRect(x + 3, y + 3, width, height, 15, 15);

        // Rectangulo de la estacion
        g2d.setColor(color);
        g2d.fillRoundRect(x, y, width, height, 15, 15);

        // Borde
        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawRoundRect(x, y, width, height, 15, 15);

        // Nombre de la estacion
        g2d.setFont(new Font("Arial", Font.BOLD, 12));
        FontMetrics fm = g2d.getFontMetrics();
        int textWidth = fm.stringWidth(nombre);
        g2d.drawString(nombre, x + (width - textWidth) / 2, y + 25);

        // Contador de piezas
        String contador = String.valueOf(piezas);
        g2d.setFont(new Font("Arial", Font.BOLD, 24));
        fm = g2d.getFontMetrics();
        textWidth = fm.stringWidth(contador);
        g2d.setColor(new Color(0, 0, 0, 200));
        g2d.drawString(contador, x + (width - textWidth) / 2, y + 55);

        // Dibujar iconos de piezas (pequeños cuadrados)
        if (piezas > 0) {
            int maxIconos = Math.min(piezas, 5);
            int iconSize = 8;
            int spacing = 3;
            int totalWidth = maxIconos * iconSize + (maxIconos - 1) * spacing;
            int startX = x + (width - totalWidth) / 2;
            int iconY = y + height - 15;

            g2d.setColor(COLOR_PIEZA);
            for (int i = 0; i < maxIconos; i++) {
                g2d.fillRect(startX + i * (iconSize + spacing), iconY, iconSize, iconSize);
            }
        }
    }

    /**
     * Dibuja una flecha horizontal
     */
    private void dibujarFlecha(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));

        // Linea
        g2d.drawLine(x1, y1, x2, y2);

        // Punta de flecha
        int arrowSize = 10;
        int[] xPoints = {x2, x2 - arrowSize, x2 - arrowSize};
        int[] yPoints = {y2, y2 - arrowSize/2, y2 + arrowSize/2};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    /**
     * Dibuja una flecha curva vertical
     */
    private void dibujarFlechaCurva(Graphics2D g2d, int x1, int y1, int x2, int y2) {
        g2d.setColor(new Color(100, 100, 100));
        g2d.setStroke(new BasicStroke(2));

        // Curva bezier
        Path2D path = new Path2D.Double();
        path.moveTo(x1, y1);

        int ctrlX = (x1 + x2) / 2;
        int ctrlY = (y1 + y2) / 2;
        path.quadTo(ctrlX, y1 + 20, x2, y2);

        g2d.draw(path);

        // Punta de flecha
        int arrowSize = 10;
        int[] xPoints = {x2, x2 - arrowSize/2, x2 + arrowSize/2};
        int[] yPoints = {y2, y2 - arrowSize, y2 - arrowSize};
        g2d.fillPolygon(xPoints, yPoints, 3);
    }

    /**
     * Dibuja el contador de salida
     */
    private void dibujarSalida(Graphics2D g2d, int x, int y, int width, int height, int piezas) {
        // Circulo
        g2d.setColor(new Color(50, 205, 50));
        g2d.fillOval(x, y, width, height);

        g2d.setColor(Color.BLACK);
        g2d.setStroke(new BasicStroke(2));
        g2d.drawOval(x, y, width, height);

        // Texto "SALIDA"
        g2d.setFont(new Font("Arial", Font.BOLD, 10));
        FontMetrics fm = g2d.getFontMetrics();
        String texto = "SALIDA";
        int textWidth = fm.stringWidth(texto);
        g2d.drawString(texto, x + (width - textWidth) / 2, y + height/2 - 5);

        // Contador
        g2d.setFont(new Font("Arial", Font.BOLD, 18));
        fm = g2d.getFontMetrics();
        String contador = String.valueOf(piezas);
        textWidth = fm.stringWidth(contador);
        g2d.drawString(contador, x + (width - textWidth) / 2, y + height/2 + 15);
    }

    /**
     * Dibuja la leyenda explicativa
     */
    private void dibujarLeyenda(Graphics2D g2d, int x, int y) {
        g2d.setFont(new Font("Arial", Font.PLAIN, 10));
        g2d.setColor(Color.BLACK);
        g2d.drawString("Los números indican", x, y);
        g2d.drawString("cantidad de piezas", x, y + 15);
        g2d.drawString("en cada estación", x, y + 30);
    }

    @Override
    public void onActualizacion(double tiempoActual, EstadisticasSimulacion estadisticas) {
        // Actualizar contadores
        this.piezasEnRecepcion = estadisticas.getPiezasEnRecepcion();
        this.piezasEnLavado = estadisticas.getPiezasEnLavado();
        this.piezasEnAlmacenPintura = estadisticas.getPiezasEnAlmacenPintura();
        this.piezasEnPintura = estadisticas.getPiezasEnPintura();
        this.piezasEnAlmacenHorno = estadisticas.getPiezasEnAlmacenHorno();
        this.piezasEnHorno = estadisticas.getPiezasEnHorno();
        this.piezasEnInspeccion = estadisticas.getPiezasEnInspeccion();
        this.piezasCompletadas = estadisticas.getPiezasCompletadas();

        // Redibujar
        SwingUtilities.invokeLater(this::repaint);
    }

    @Override
    public void onFinalizacion(EstadisticasSimulacion estadisticas) {
        // Actualizacion final
        onActualizacion(0, estadisticas);
    }
}
