package actividad_3.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public abstract class TablaEstilizadaPanel extends JPanel {
    protected JTable tabla;
    protected DefaultTableModel modeloTabla;
    protected JLabel lblOptimo;
    protected int filaOptima = -1;

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
                if (row == filaOptima) {
                    c.setBackground(new Color(180, 255, 180));
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
        lblOptimo = new JLabel("Mejor capacidad: - | Ganancia máxima: -");
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

    protected void actualizarOptimo(double mejorPrecio, double mejorGanancia, int filaOptima) {
        this.filaOptima = filaOptima;
        lblOptimo.setText("Mejor capacidad: " + (filaOptima >= 0 ? String.format("%.2f", mejorPrecio) : "-") + " | Ganancia máxima: " + (filaOptima >= 0 ? String.format("%.2f", mejorGanancia) : "-"));
        tabla.repaint();
    }
}

