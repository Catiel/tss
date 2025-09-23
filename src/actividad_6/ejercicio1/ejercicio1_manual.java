package actividad_6.ejercicio1;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio1_manual extends JFrame {

    private final JTextField txtMediaLlegada;
    private final JTextField txtMediaInspeccion;
    private final JTextField txtDesvEstInspeccion;
    private final JTextField txtNumPiezas;
    private final DefaultTableModel model;
    private JTable tabla;

    public ejercicio1_manual() {
        setTitle("Simulación Inspección - Ingreso Manual");
        setSize(1400, 600);
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
        txtNumPiezas.setEditable(true);
        inputPanel.add(txtNumPiezas);

        JButton btnCrearFilas = new JButton("Crear Filas");
        JButton btnCalcular = new JButton("Calcular");
        JButton btnLimpiar = new JButton("Limpiar");

        inputPanel.add(btnCrearFilas);
        inputPanel.add(btnCalcular);
        inputPanel.add(btnLimpiar);

        String[] columnas = {
                "Piezas", "Tiempo entreg llegadas", "Tiempo de llegada", "Inicio de inspección",
                "Tiempo de inspección", "Fin de la inspección", "Duración de la inspección",
                "Tiempo en espera", "Tiempo pro1/2 en inspeccion"
        };
        model = new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Solo permitir editar las columnas 1 (Tiempo entreg llegadas) y 4 (Tiempo de inspección)
                return column == 1 || column == 4;
            }
        };

        tabla = new JTable(model);

        // Configurar el ancho de las columnas para mejor visualización
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);  // Piezas
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Tiempo entreg llegadas
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100); // Tiempo de llegada
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Inicio de inspección
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Tiempo de inspección
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120); // Fin de la inspección
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120); // Duración de la inspección
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100); // Tiempo en espera
        tabla.getColumnModel().getColumn(8).setPreferredWidth(140); // Tiempo pro1/2 en inspeccion

        JScrollPane scrollPane = new JScrollPane(tabla);

        setLayout(new BorderLayout());
        add(inputPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnCrearFilas.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnLimpiar.addActionListener(this::limpiar);
    }

    private void crearFilas(ActionEvent e) {
        model.setRowCount(0);

        try {
            int n = Integer.parseInt(txtNumPiezas.getText());

            for (int i = 0; i < n; i++) {
                model.addRow(new Object[]{
                        i + 1,                    // Número de pieza
                        "",                       // Tiempo entreg llegadas (editable)
                        "",                       // Tiempo de llegada (calculado)
                        "",                       // Inicio de inspección (calculado)
                        "",                       // Tiempo de inspección (editable)
                        "",                       // Fin de la inspección (calculado)
                        "",                       // Duración de la inspección (calculado)
                        "",                       // Tiempo en espera (calculado)
                        ""                        // Tiempo pro1/2 en inspeccion (calculado)
                });
            }

            JOptionPane.showMessageDialog(this,
                "Filas creadas. Ahora ingrese los valores en las columnas:\n" +
                "- 'Tiempo entreg llegadas' (columna 2)\n" +
                "- 'Tiempo de inspección' (columna 5)\n\n" +
                "Luego presione 'Calcular' para completar los cálculos.",
                "Instrucciones",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Por favor, ingrese un número válido para las piezas.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void calcular(ActionEvent e) {
        int n = model.getRowCount();
        if (n == 0) {
            JOptionPane.showMessageDialog(this,
                "Primero debe crear las filas.",
                "Error",
                JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            double[] tiempoEntregLlegadas = new double[n];
            double[] tiempoInspeccion = new double[n];
            double[] tiempoLlegada = new double[n];
            double[] inicioInspeccion = new double[n];
            double[] finInspeccion = new double[n];
            double[] duracionInspeccion = new double[n];
            double[] tiempoEspera = new double[n];

            // Leer los valores ingresados manualmente
            for (int i = 0; i < n; i++) {
                Object valorTiempoLlegada = model.getValueAt(i, 1);
                Object valorTiempoInspeccion = model.getValueAt(i, 4);

                if (valorTiempoLlegada == null || valorTiempoLlegada.toString().trim().isEmpty()) {
                    throw new NumberFormatException("Falta el valor de 'Tiempo entreg llegadas' en la fila " + (i + 1));
                }
                if (valorTiempoInspeccion == null || valorTiempoInspeccion.toString().trim().isEmpty()) {
                    throw new NumberFormatException("Falta el valor de 'Tiempo de inspección' en la fila " + (i + 1));
                }

                tiempoEntregLlegadas[i] = Double.parseDouble(valorTiempoLlegada.toString().trim());
                tiempoInspeccion[i] = Double.parseDouble(valorTiempoInspeccion.toString().trim());
            }

            // Realizar los cálculos
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
                finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccion[i];

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

                // Actualizar la tabla con los valores calculados
                model.setValueAt(String.format("%.6f", tiempoLlegada[i]), i, 2);
                model.setValueAt(String.format("%.6f", inicioInspeccion[i]), i, 3);
                model.setValueAt(String.format("%.6f", finInspeccion[i]), i, 5);
                model.setValueAt(String.format("%.6f", duracionInspeccion[i]), i, 6);
                model.setValueAt(String.format("%.6f", tiempoEspera[i]), i, 7);
                model.setValueAt(String.format("%.6f", tiempoPromInspeccion), i, 8);
            }

            // Calcular y mostrar estadísticas adicionales
            double sumaDuracionInspeccion = 0;
            for (int i = 0; i < n; i++) {
                sumaDuracionInspeccion += duracionInspeccion[i];
            }
            double tiempoPromedioTotal = sumaDuracionInspeccion / n;

            JOptionPane.showMessageDialog(this,
                String.format("Cálculos completados exitosamente.\n\nTiempo promedio de permanencia en el proceso: %.4f minutos", tiempoPromedioTotal),
                "Resultado de la Simulación",
                JOptionPane.INFORMATION_MESSAGE);

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Error en los datos ingresados: " + ex.getMessage() + "\n\nAsegúrese de que todos los valores sean números válidos.",
                "Error de Datos",
                JOptionPane.ERROR_MESSAGE);
        }
    }

    private void limpiar(ActionEvent e) {
        model.setRowCount(0);
        JOptionPane.showMessageDialog(this,
            "Tabla limpiada. Puede crear nuevas filas.",
            "Información",
            JOptionPane.INFORMATION_MESSAGE);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new ejercicio1_manual().setVisible(true));
    }
}
