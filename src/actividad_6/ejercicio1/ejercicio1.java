package actividad_6.ejercicio1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

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
        inputPanel.add(txtMediaLlegada);

        inputPanel.add(new JLabel("Media - Tiempo inspección"));
        txtMediaInspeccion = new JTextField("4", 5);
        inputPanel.add(txtMediaInspeccion);

        inputPanel.add(new JLabel("Desv Est - Tiempo inspección"));
        txtDesvEstInspeccion = new JTextField("0.5", 5);
        inputPanel.add(txtDesvEstInspeccion);

        inputPanel.add(new JLabel("Número de piezas"));
        txtNumPiezas = new JTextField("15", 5);
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

        int n = Integer.parseInt(txtNumPiezas.getText());
        double mediaLlegada = Double.parseDouble(txtMediaLlegada.getText());
        double mediaInspeccion = Double.parseDouble(txtMediaInspeccion.getText());
        double desvInspeccion = Double.parseDouble(txtDesvEstInspeccion.getText());

        Random random = new Random();

        double[] tiempoLlegada = new double[n];
        double[] inicioInspeccion = new double[n];
        double[] duracionInspeccion = new double[n];
        double[] finInspeccion = new double[n];
        double[] tiempoEspera = new double[n];

        for (int i = 0; i < n; i++) {
            double r = random.nextDouble();
            double tiempoEntregLlegadas = -Math.log(r) * mediaLlegada;

            if (i == 0) {
                tiempoLlegada[i] = tiempoEntregLlegadas;
            } else {
                tiempoLlegada[i] = tiempoLlegada[i - 1] + tiempoEntregLlegadas;
            }

            if (i == 0) {
                inicioInspeccion[i] = tiempoLlegada[i];
            } else {
                inicioInspeccion[i] = Math.max(tiempoLlegada[i], finInspeccion[i - 1]);
            }

            duracionInspeccion[i] = mediaInspeccion + desvInspeccion * random.nextGaussian();
            if (duracionInspeccion[i] < 0) duracionInspeccion[i] = 0;

            finInspeccion[i] = inicioInspeccion[i] + duracionInspeccion[i];

            tiempoEspera[i] = Math.max(0, inicioInspeccion[i] - tiempoLlegada[i]);

            double tiempoProm = finInspeccion[i] - tiempoLlegada[i];

            model.addRow(new Object[]{
                    i + 1,
                    String.format("%.6f", tiempoEntregLlegadas),
                    String.format("%.6f", tiempoLlegada[i]),
                    String.format("%.6f", inicioInspeccion[i]),
                    String.format("%.6f", duracionInspeccion[i]),
                    String.format("%.6f", finInspeccion[i]),
                    String.format("%.6f", duracionInspeccion[i]),
                    String.format("%.6f", tiempoEspera[i]),
                    String.format("%.6f", tiempoProm)
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio1().setVisible(true));
    }
}
