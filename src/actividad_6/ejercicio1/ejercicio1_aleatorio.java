package actividad_6.ejercicio1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class ejercicio1_aleatorio extends JFrame {

    private final JTextField txtMediaLlegada;
    private final JTextField txtMediaInspeccion;
    private final JTextField txtDesvEstInspeccion;
    private final JTextField txtNumPiezas;
    private final DefaultTableModel model;

    public ejercicio1_aleatorio() {
        setTitle("Simulación Inspección - Valores Aleatorios");
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
        txtNumPiezas.setEditable(true); // Permite modificar el número de piezas
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

        // Obtener número de piezas del campo de texto
        int n = Integer.parseInt(txtNumPiezas.getText());

        // Parámetros según el enunciado
        double mediaLlegada = 5.0;    // Media para distribución exponencial
        double mediaInspeccion = 4.0;  // Media para distribución normal
        double desvInspeccion = 0.5;   // Desviación estándar para distribución normal

        Random random = new Random();

        double[] tiempoLlegada = new double[n];
        double[] inicioInspeccion = new double[n];
        double[] finInspeccion = new double[n];
        double[] duracionInspeccion = new double[n];
        double[] tiempoEspera = new double[n];
        double[] tiempoEntregLlegadas = new double[n];
        double[] tiempoInspeccionArray = new double[n];

        for (int i = 0; i < n; i++) {
            // Generar tiempo entreg llegadas usando distribución exponencial
            // Fórmula: -LN(ALEATORIO()) * media
            double randomValue1 = random.nextDouble();
            tiempoEntregLlegadas[i] = -Math.log(randomValue1) * mediaLlegada;

            // Generar tiempo de inspección usando distribución normal
            // Fórmula equivalente a DISTR.NORM.INV(ALEATORIO();4;0.5)
            tiempoInspeccionArray[i] = mediaInspeccion + desvInspeccion * random.nextGaussian();
            // Asegurar que no sea negativo
            if (tiempoInspeccionArray[i] < 0) {
                tiempoInspeccionArray[i] = 0;
            }

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
            finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccionArray[i];

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
                    String.format("%.7f", tiempoInspeccionArray[i]),
                    String.format("%.6f", finInspeccion[i]),
                    String.format("%.6f", duracionInspeccion[i]),
                    String.format("%.6f", tiempoEspera[i]),
                    String.format("%.6f", tiempoPromInspeccion)
            });
        }

        // Calcular y mostrar estadísticas adicionales
        double sumaDuracionInspeccion = 0;
        for (int i = 0; i < n; i++) {
            sumaDuracionInspeccion += duracionInspeccion[i];
        }
        double tiempoPromedioTotal = sumaDuracionInspeccion / n;

        JOptionPane.showMessageDialog(this,
            String.format("Tiempo promedio de permanencia en el proceso: %.4f minutos", tiempoPromedioTotal),
            "Resultado de la Simulación",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio1_aleatorio().setVisible(true));
    }
}
