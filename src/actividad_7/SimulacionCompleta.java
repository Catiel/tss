package actividad_7;

import com.formdev.flatlaf.FlatLightLaf;
import org.apache.commons.math3.distribution.NormalDistribution;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.title.TextTitle;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.*;
import java.awt.*;
import java.util.Random;

public class SimulacionCompleta extends JFrame {

    private JTable tabla;
    private DefaultTableModel model;

    public SimulacionCompleta() {
        super("Simulación de Inventario - Análisis Completo");
        Font fuenteGeneral = new Font("Segoe UI", Font.PLAIN, 14);
        Font fuenteTitulo = new Font("Segoe UI Semibold", Font.BOLD, 18);
        Font fuenteHeader = new Font("Segoe UI", Font.BOLD, 15);

        int dias = 365;
        int politicaProduccion = 60;
        double mediaDemanda = 80;
        double desviacionDemanda = 10;
        double costoFaltanteUnitario = 800;
        double costoInventarioUnitario = 500;

        String[] columnas = {
                "Día", "Inventario inicial (Uds)", "Política de producción (Uds)", "Total disponible (Uds)",
                "Rn", "Demanda (uds)", "Ventas (Uds)", "Ventas perdidas (uds)", "Inventario final (Uds)",
                "Costo faltante ($)", "Costo de inventarios ($)", "Costo total ($)"
        };

        model = new DefaultTableModel(columnas, 0);
        tabla = new JTable(model);
        tabla.setFont(fuenteGeneral);
        tabla.setRowHeight(28);
        tabla.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tabla.setFillsViewportHeight(true);
        tabla.getTableHeader().setReorderingAllowed(false);

        JTableHeader header = tabla.getTableHeader();
        header.setBackground(new Color(30, 144, 255));
        header.setForeground(Color.WHITE);
        header.setFont(fuenteHeader);
        header.setPreferredSize(new Dimension(header.getWidth(), 32));

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);

        DefaultTableCellRenderer moneyRenderer = new DefaultTableCellRenderer() {
            @Override
            public void setValue(Object value) {
                if (value instanceof Number) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(String.format("$%,.2f", value));
                } else if (value instanceof String) {
                    setHorizontalAlignment(SwingConstants.RIGHT);
                    setText(value.toString());
                } else {
                    super.setValue(value);
                }
            }
        };

        int[] anchos = {50, 140, 150, 140, 70, 100, 100, 120, 130, 150, 170, 150};
        for (int i = 0; i < columnas.length; i++) {
            TableColumn col = tabla.getColumnModel().getColumn(i);
            col.setPreferredWidth(anchos[i]);
            if (i >= 9) col.setCellRenderer(moneyRenderer);
            else col.setCellRenderer(centerRenderer);
        }

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable tbl, Object value, boolean isSelected,
                                                           boolean hasFocus, int row, int column) {
                Component comp = super.getTableCellRendererComponent(tbl, value, isSelected, hasFocus, row, column);
                if (!isSelected) comp.setBackground(row % 2 == 0 ? Color.WHITE : new Color(235, 245, 255));
                return comp;
            }
        });

        JScrollPane scrollPaneTabla = new JScrollPane(tabla);
        scrollPaneTabla.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        int inventarioFinal = 0;
        NormalDistribution dist = new NormalDistribution(mediaDemanda, desviacionDemanda);
        Random random = new Random();

        double sumaCostoTotal = 0;
        double sumaCuadrados = 0;
        double minCosto = Double.MAX_VALUE;
        double maxCosto = Double.MIN_VALUE;

        DefaultCategoryDataset dataset = new DefaultCategoryDataset();

        for (int dia = 1; dia <= dias; dia++) {
            int inventarioInicial = inventarioFinal;
            int totalDisponible = inventarioInicial + politicaProduccion;
            double rn = random.nextDouble();
            int demanda = (int) Math.round(dist.inverseCumulativeProbability(rn));
            int ventas = Math.min(demanda, totalDisponible);
            int ventasPerdidas = Math.max(0, demanda - ventas);
            inventarioFinal = totalDisponible - ventas;
            double costoFaltante = ventasPerdidas * costoFaltanteUnitario;
            double costoInventario = inventarioFinal * costoInventarioUnitario;
            double costoTotal = costoFaltante + costoInventario;

            sumaCostoTotal += costoTotal;
            sumaCuadrados += costoTotal * costoTotal;
            if (costoTotal < minCosto) minCosto = costoTotal;
            if (costoTotal > maxCosto) maxCosto = costoTotal;

            Object[] fila = {
                    dia, inventarioInicial, politicaProduccion, totalDisponible,
                    String.format("%.4f", rn), demanda, ventas, ventasPerdidas, inventarioFinal,
                    costoFaltante, costoInventario, costoTotal
            };
            model.addRow(fila);

            dataset.addValue(costoTotal, "Costo total", Integer.toString(dia));
        }

        double promedio = sumaCostoTotal / dias;
        double varianza = (sumaCuadrados / dias) - (promedio * promedio);
        double desviacion = Math.sqrt(varianza);

        JPanel resumenPanel = new JPanel(new GridLayout(1, 4, 25, 10));
        resumenPanel.setBorder(BorderFactory.createTitledBorder(null, "Resumen Estadístico",
                javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.TOP,
                new Font("Segoe UI Semibold", Font.BOLD, 16), new Color(30, 144, 255)));
        resumenPanel.setBackground(Color.WHITE);

        resumenPanel.add(createSummaryLabel(String.format("Promedio: $%,.2f", promedio)));
        resumenPanel.add(createSummaryLabel(String.format("Desviación: $%,.2f", desviacion)));
        resumenPanel.add(createSummaryLabel(String.format("Mínimo: $%,.2f", minCosto)));
        resumenPanel.add(createSummaryLabel(String.format("Máximo: $%,.2f", maxCosto)));

        // Scroll panel para resumen estadístico
        JScrollPane scrollPaneResumen = new JScrollPane(resumenPanel);
        scrollPaneResumen.setPreferredSize(new Dimension(1000, 60));
        scrollPaneResumen.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));

        JFreeChart chart = ChartFactory.createLineChart(
                "Evolución del Costo total ($) por Día",
                "Día",
                "Costo total ($)",
                dataset,
                PlotOrientation.VERTICAL,
                false,
                true,
                false
        );
        chart.setBackgroundPaint(Color.white);
        TextTitle textTitle = new TextTitle("Evolución del Costo total ($) por Día",
                new Font("Segoe UI Semibold", Font.BOLD, 18));
        textTitle.setPaint(new Color(30, 144, 255));
        chart.setTitle(textTitle);

        ChartPanel chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(1200, 350));
        chartPanel.setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));
        chartPanel.setBackground(Color.WHITE);

        // Layout con BorderLayout para dividir en 3 zonas verticales
        JPanel mainPanel = new JPanel(new BorderLayout(20, 20));
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        mainPanel.add(chartPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPaneTabla, BorderLayout.CENTER);
        mainPanel.add(scrollPaneResumen, BorderLayout.SOUTH);

        setContentPane(mainPanel);

        setSize(1400, 900);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
    }

    private JLabel createSummaryLabel(String texto) {
        JLabel label = new JLabel(texto, SwingConstants.CENTER);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        label.setForeground(new Color(50, 50, 50));
        return label;
    }

    public static void main(String[] args) {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
        SwingUtilities.invokeLater(() -> new SimulacionCompleta().setVisible(true));
    }
}




