package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public abstract class TablaEstilizadaPanel extends JPanel {
    protected JTable tabla;
    protected DefaultTableModel modeloTabla;
    protected JLabel lblOptimo;
    protected int filaOptima = -1;
    protected int filaOptimaVan = -1;
    protected double mejorCapacidadVan = -1;
    protected double mejorVan = Double.NEGATIVE_INFINITY;

    public TablaEstilizadaPanel(String titulo, DefaultTableModel modeloTabla, JPanel panelSuperior, JPanel panelInferior) {
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10, 10));
        JLabel lblTitulo = new JLabel(titulo);
        EstilosUI.aplicarEstiloTitulo(lblTitulo);
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(lblTitulo, BorderLayout.NORTH);
        if (panelSuperior != null) {
            add(panelSuperior, BorderLayout.BEFORE_FIRST_LINE);
        }
        this.modeloTabla = modeloTabla;
        tabla = new JTable(modeloTabla) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (row == filaOptima && filaOptima >= 0) {
                    c.setBackground(new Color(180, 255, 180)); // verde para ganancia
                } else if (row == filaOptimaVan && filaOptimaVan >= 0) {
                    c.setBackground(new Color(180, 220, 255)); // azul claro para VAN
                } else if (column == 0) {
                    c.setBackground(new Color(255, 255, 230));
                } else if (column == 1) {
                    c.setBackground(new Color(235, 245, 255));
                    if (c instanceof JLabel) {
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13));
                    }
                } else {
                    c.setBackground(Color.WHITE);
                }
                return c;
            }
        };
        EstilosUI.aplicarEstiloTabla(tabla);
        tabla.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240)));
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(scroll, BorderLayout.CENTER);
        lblOptimo = new JLabel("Mejor capacidad: - | Ganancia máxima: - | Mejor VAN: - | Capacidad VAN: -");
        EstilosUI.aplicarEstiloLabel(lblOptimo);
        lblOptimo.setFont(new Font("Segoe UI", Font.BOLD, 16));
        if (panelInferior == null) {
            JPanel panelInferiorDefault = new JPanel(new BorderLayout());
            EstilosUI.aplicarEstiloPanel(panelInferiorDefault);
            panelInferiorDefault.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 220, 240)),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));
            panelInferiorDefault.add(lblOptimo, BorderLayout.CENTER);
            add(panelInferiorDefault, BorderLayout.SOUTH);
        } else {
            add(panelInferior, BorderLayout.SOUTH);
        }
    }

    public void actualizarOptimo(double mejorCapacidad, double mejorGanancia, int filaOptima, double mejorCapacidadVan, double mejorVan, int filaOptimaVan) {
        this.filaOptima = filaOptima;
        this.filaOptimaVan = filaOptimaVan;
        this.mejorCapacidadVan = mejorCapacidadVan;
        this.mejorVan = mejorVan;
        lblOptimo.setText(
            "Mejor capacidad: " + (filaOptima >= 0 ? String.format("%.2f", mejorCapacidad) : "-") +
            " | Ganancia máxima: " + (filaOptima >= 0 ? String.format("$%,.0f", mejorGanancia) : "-") +
            " | Mejor VAN: " + (filaOptimaVan >= 0 ? String.format("$%,.0f", mejorVan) : "-") +
            " | Capacidad VAN: " + (filaOptimaVan >= 0 ? String.format("%.2f", mejorCapacidadVan) : "-")
        );
        tabla.repaint();
    }
}
