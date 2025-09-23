package actividad_6.ejercicio1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio1 extends JFrame {

    private final JTextField txtMediaLlegada;
    private final JTextField txtMediaInspeccion;
    private final JTextField txtDesvEstInspeccion;
    private final JTextField txtNumPiezas;
    private final DefaultTableModel model;

    public ejercicio1() {
        setTitle("Simulación Inspección con Parámetros");
        setSize(1200, 550);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel inputPanel = new JPanel(new FlowLayout());

        inputPanel.add(new JLabel("Media - Tiempo entreg llegadas"));
        txtMediaLlegada = new JTextField("5", 5);
        txtMediaLlegada.setEditable(false);
        inputPanel.add(txtMediaLlegada);

        inputPanel.add(new JLabel("Media - Tiempo inspección"));
        txtMediaInspeccion = new JTextField("4", 5);
        txtMediaInspeccion.setEditable(false);
        inputPanel.add(txtMediaInspeccion);

        inputPanel.add(new JLabel("Desv Est - Tiempo inspección"));
        txtDesvEstInspeccion = new JTextField("0.5", 5);
        txtDesvEstInspeccion.setEditable(false);
        inputPanel.add(txtDesvEstInspeccion);

        inputPanel.add(new JLabel("Número de piezas"));
        txtNumPiezas = new JTextField("9", 5);
        txtNumPiezas.setEditable(false);
        inputPanel.add(txtNumPiezas);

        JButton btnSimular = new JButton("Simular");
        inputPanel.add(btnSimular);

        String[] columnas = {
                "Piezas", "Tiempo entreg llegadas", "Tiempo de llegada", "Inicio de inspección",
                "Tiempo de inspección", "Fin de la inspección", "Duración de la inspección",
                "Tiempo en espera", "Tiempo pro1/2 en inspeccion"
        };
        model = new DefaultTableModel(columnas, 0);

        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSimular.addActionListener(this::simular);
    }

    private void simular(ActionEvent e) {
        model.setRowCount(0);

        // Valores predeterminados fijos
        int n = 9;

        // Valores predeterminados para tiempo entreg llegadas
        double[] tiempoEntregLlegadas = {
                1.327607, 6.5310326, 5.1946396, 7.0961155, 7.31768001,
                2.0867485, 1.4688669, 4.6554184, 0.4156928
        };

        // Valores predeterminados para tiempo de inspección
        double[] tiempoInspeccionPred = {
                3.0058359, 4.13466238, 3.9414644, 4.2697994, 4.1262137,
                3.9923443, 4.1267457, 4.4108686, 2.7754816
        };

        double[] tiempoLlegada = new double[n];
        double[] inicioInspeccion = new double[n];
        double[] finInspeccion = new double[n];
        double[] duracionInspeccion = new double[n];
        double[] tiempoEspera = new double[n];

        for (int i = 0; i < n; i++) {
            // Calcular tiempo de llegada
            if (i == 0) {
                tiempoLlegada[i] = tiempoEntregLlegadas[i];
            } else {
                tiempoLlegada[i] = tiempoLlegada[i - 1] + tiempoEntregLlegadas[i];
            }

            // Calcular inicio de inspección
            if (i == 0) {
                inicioInspeccion[i] = tiempoLlegada[i];
            } else {
                inicioInspeccion[i] = Math.max(tiempoLlegada[i], finInspeccion[i - 1]);
            }

            // Calcular fin de inspección = inicio de inspección + tiempo de inspección
            finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccionPred[i];

            // Calcular duración de la inspección = fin de la inspección - tiempo de llegada
            duracionInspeccion[i] = finInspeccion[i] - tiempoLlegada[i];

            // Calcular tiempo en espera
            tiempoEspera[i] = Math.max(0, inicioInspeccion[i] - tiempoLlegada[i]);

            // Calcular tiempo promedio en inspección (promedio entre la primera fila y la actual)
            double tiempoPromInspeccion;
            if (i == 0) {
                tiempoPromInspeccion = duracionInspeccion[0];
            } else {
                tiempoPromInspeccion = (duracionInspeccion[0] + duracionInspeccion[i]) / 2.0;
            }

            model.addRow(new Object[]{
                    i + 1,
                    String.format("%.7f", tiempoEntregLlegadas[i]),
                    String.format("%.6f", tiempoLlegada[i]),
                    String.format("%.6f", inicioInspeccion[i]),
                    String.format("%.7f", tiempoInspeccionPred[i]),
                    String.format("%.6f", finInspeccion[i]),
                    String.format("%.6f", duracionInspeccion[i]),
                    String.format("%.6f", tiempoEspera[i]),
                    String.format("%.6f", tiempoPromInspeccion)
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio1().setVisible(true));
    }
}
