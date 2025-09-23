package actividad_6.ejercicio2;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import org.apache.commons.math3.distribution.NormalDistribution;

public class ejercicio2Manual extends JFrame {
    private final JTextField txtNumPiezas;
    private final JTextField txtMediaExponencial;
    private final JTextField txtMediaNormal;
    private final JTextField txtDesvNormal;
    private final DefaultTableModel model;
    private JTable tabla;
    private JButton btnCrearFilas;
    private JButton btnCalcular;

    public ejercicio2Manual() {
        this.setTitle("Simulación de Inspección - Entrada Manual");
        this.setSize(1200, 500);
        this.setDefaultCloseOperation(3);
        this.setLocationRelativeTo((Component)null);

        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout());

        panelSuperior.add(new JLabel("Número de Piezas:"));
        this.txtNumPiezas = new JTextField("18", 5);
        panelSuperior.add(this.txtNumPiezas);

        panelSuperior.add(new JLabel("Media Exponencial:"));
        this.txtMediaExponencial = new JTextField("5", 5);
        panelSuperior.add(this.txtMediaExponencial);

        panelSuperior.add(new JLabel("Media Normal:"));
        this.txtMediaNormal = new JTextField("4", 5);
        panelSuperior.add(this.txtMediaNormal);

        panelSuperior.add(new JLabel("Desviación Normal:"));
        this.txtDesvNormal = new JTextField("0.5", 5);
        panelSuperior.add(this.txtDesvNormal);

        // Botones
        this.btnCrearFilas = new JButton("Crear Filas");
        this.btnCalcular = new JButton("Calcular");
        this.btnCalcular.setEnabled(false); // Deshabilitado hasta crear filas

        panelSuperior.add(btnCrearFilas);
        panelSuperior.add(btnCalcular);

        // Configurar tabla
        String[] columnas = new String[]{
            "Pieza", "Rn Llegada", "Tiempo entre llegadas", "Minuto en que llega",
            "Minuto en que inicia inspección", "Rn Inspección", "Tiempo de inspección",
            "Minuto en que finaliza inspección", "Tiempo total inspección", "Tiempo en espera"
        };

        this.model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar las columnas "Rn Llegada" (1) y "Rn Inspección" (5)
                return column == 1 || column == 5;
            }
        };

        this.tabla = new JTable(this.model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        // Layout
        this.add(panelSuperior, BorderLayout.NORTH);
        this.add(scrollPane, BorderLayout.CENTER);

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
    }

    private void crearFilas(ActionEvent e) {
        try {
            int numPiezas = Integer.parseInt(this.txtNumPiezas.getText());

            // Limpiar tabla
            this.model.setRowCount(0);

            // Crear filas con valores por defecto
            for(int i = 0; i < numPiezas; i++) {
                Object[] fila = new Object[10];
                fila[0] = i + 1; // Número de pieza
                fila[1] = "0.0000"; // Rn Llegada - editable
                fila[2] = ""; // Tiempo entre llegadas - calculado
                fila[3] = ""; // Minuto en que llega - calculado
                fila[4] = ""; // Minuto en que inicia inspección - calculado
                fila[5] = "0.0000"; // Rn Inspección - editable
                fila[6] = ""; // Tiempo de inspección - calculado
                fila[7] = ""; // Minuto en que finaliza inspección - calculado
                fila[8] = ""; // Tiempo total inspección - calculado
                fila[9] = ""; // Tiempo en espera - calculado

                this.model.addRow(fila);
            }

            // Habilitar el botón calcular y deshabilitar crear filas
            this.btnCalcular.setEnabled(true);
            this.btnCrearFilas.setEnabled(false);
            this.txtNumPiezas.setEnabled(false);

        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Por favor ingrese un número válido de piezas",
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    // Función para calcular NORM.INV(p, mu, sigma) usando Apache Commons Math3
    private double normInv(double p, double mu, double sigma) {
        NormalDistribution normal = new NormalDistribution(mu, sigma);
        return normal.inverseCumulativeProbability(p);
    }

    private void calcular(ActionEvent e) {
        try {
            double mediaExponencial = Double.parseDouble(this.txtMediaExponencial.getText());
            double mediaNormal = Double.parseDouble(this.txtMediaNormal.getText());
            double desviacionNormal = Double.parseDouble(this.txtDesvNormal.getText());

            int numFilas = this.model.getRowCount();

            // Arrays para los cálculos
            double[] rnLlegada = new double[numFilas];
            double[] rnInspeccion = new double[numFilas];
            double[] tiempoEntreLlegadas = new double[numFilas];
            double[] minutoLlegada = new double[numFilas];
            double[] minutoInicioInspeccion = new double[numFilas];
            double[] tiempoInspeccion = new double[numFilas];
            double[] minutoFinInspeccion = new double[numFilas];
            double[] tiempoTotalInspeccion = new double[numFilas];
            double[] tiempoEspera = new double[numFilas];

            // Leer valores de Rn desde la tabla
            for(int i = 0; i < numFilas; i++) {
                try {
                    String rnLlegadaStr = this.model.getValueAt(i, 1).toString();
                    String rnInspeccionStr = this.model.getValueAt(i, 5).toString();

                    rnLlegada[i] = Double.parseDouble(rnLlegadaStr);
                    rnInspeccion[i] = Double.parseDouble(rnInspeccionStr);

                    // Validar que estén entre 0 y 1
                    if(rnLlegada[i] < 0 || rnLlegada[i] > 1 || rnInspeccion[i] < 0 || rnInspeccion[i] > 1) {
                        throw new IllegalArgumentException("Los valores Rn deben estar entre 0 y 1");
                    }
                } catch (NumberFormatException ex) {
                    javax.swing.JOptionPane.showMessageDialog(this,
                        "Error en fila " + (i+1) + ": Por favor ingrese valores numéricos válidos para Rn",
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }

            // Calcular tiempo entre llegadas
            for(int i = 0; i < numFilas; i++) {
                if(rnLlegada[i] == 1.0) rnLlegada[i] = 0.9999; // Evitar log(0)
                tiempoEntreLlegadas[i] = -Math.log(1.0 - rnLlegada[i]) * mediaExponencial;
            }

            // Calcular tiempo de llegada acumulado
            minutoLlegada[0] = tiempoEntreLlegadas[0];
            for(int i = 1; i < numFilas; i++) {
                minutoLlegada[i] = minutoLlegada[i - 1] + tiempoEntreLlegadas[i];
            }

            // Calcular tiempo de inspección
            for(int i = 0; i < numFilas; i++) {
                tiempoInspeccion[i] = normInv(rnInspeccion[i], mediaNormal, desviacionNormal);
                if(tiempoInspeccion[i] < 0.0) {
                    tiempoInspeccion[i] = 0.0;
                }
            }

            // Calcular tiempos de inicio, fin, duración total y espera
            minutoInicioInspeccion[0] = minutoLlegada[0];
            minutoFinInspeccion[0] = minutoInicioInspeccion[0] + tiempoInspeccion[0];
            tiempoEspera[0] = 0.0;
            tiempoTotalInspeccion[0] = tiempoInspeccion[0];

            for(int i = 1; i < numFilas; i++) {
                minutoInicioInspeccion[i] = Math.max(minutoLlegada[i], minutoFinInspeccion[i - 1]);
                minutoFinInspeccion[i] = minutoInicioInspeccion[i] + tiempoInspeccion[i];
                tiempoEspera[i] = Math.max(0.0, minutoInicioInspeccion[i] - minutoLlegada[i]);
                tiempoTotalInspeccion[i] = minutoFinInspeccion[i] - minutoLlegada[i];
            }

            // Actualizar la tabla con los resultados
            for(int i = 0; i < numFilas; i++) {
                this.model.setValueAt(String.format("%.4f", tiempoEntreLlegadas[i]), i, 2);
                this.model.setValueAt(String.format("%.4f", minutoLlegada[i]), i, 3);
                this.model.setValueAt(String.format("%.4f", minutoInicioInspeccion[i]), i, 4);
                this.model.setValueAt(String.format("%.4f", tiempoInspeccion[i]), i, 6);
                this.model.setValueAt(String.format("%.4f", minutoFinInspeccion[i]), i, 7);
                this.model.setValueAt(String.format("%.4f", tiempoTotalInspeccion[i]), i, 8);
                this.model.setValueAt(String.format("%.4f", tiempoEspera[i]), i, 9);
            }

        } catch (NumberFormatException ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Por favor ingrese valores numéricos válidos en los parámetros",
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            javax.swing.JOptionPane.showMessageDialog(this,
                "Error en el cálculo: " + ex.getMessage(),
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> (new ejercicio2Manual()).setVisible(true));
    }
}

