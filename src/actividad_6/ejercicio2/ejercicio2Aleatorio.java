package actividad_6.ejercicio2;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.util.Random;
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

public class ejercicio2Aleatorio extends JFrame {
    private final JTextField txtNumPiezas;
    private final JTextField txtMediaExponencial;
    private final JTextField txtMediaNormal;
    private final JTextField txtDesvNormal;
    private final DefaultTableModel model;

    public ejercicio2Aleatorio() {
        this.setTitle("Simulación de Inspección - Números Aleatorios");
        this.setSize(1000, 450);
        this.setDefaultCloseOperation(3);
        this.setLocationRelativeTo((Component)null);
        JPanel var1 = new JPanel(new FlowLayout());

        var1.add(new JLabel("Número de Piezas:"));
        this.txtNumPiezas = new JTextField("18", 5);
        var1.add(this.txtNumPiezas);

        var1.add(new JLabel("Media Exponencial (tiempo entre llegadas):"));
        this.txtMediaExponencial = new JTextField("5", 5);
        var1.add(this.txtMediaExponencial);

        var1.add(new JLabel("Media Normal (tiempo inspección):"));
        this.txtMediaNormal = new JTextField("4", 5);
        var1.add(this.txtMediaNormal);

        var1.add(new JLabel("Desviación Normal:"));
        this.txtDesvNormal = new JTextField("0.5", 5);
        var1.add(this.txtDesvNormal);

        JButton var2 = new JButton("Simular");
        var1.add(var2);

        String[] var3 = new String[]{"Pieza", "Rn Llegada", "Tiempo entre llegadas", "Minuto en que llega", "Minuto en que inicia inspección", "Rn Inspección", "Tiempo de inspección", "Minuto en que finaliza inspección", "Tiempo total inspección", "Tiempo en espera"};
        this.model = new DefaultTableModel(var3, 0);
        JTable var4 = new JTable(this.model);
        JScrollPane var5 = new JScrollPane(var4);
        this.add(var1, "North");
        this.add(var5, "Center");
        var2.addActionListener(this::simular);
    }

    // Función para calcular NORM.INV(p, mu, sigma) usando Apache Commons Math3
    private double normInv(double p, double mu, double sigma) {
        // Usamos directamente la distribución normal con parámetros específicos
        NormalDistribution normal = new NormalDistribution(mu, sigma);
        return normal.inverseCumulativeProbability(p);
    }

    private void simular(ActionEvent var1) {
        this.model.setRowCount(0);
        int numPiezas = Integer.parseInt(this.txtNumPiezas.getText());
        double mediaExponencial = Double.parseDouble(this.txtMediaExponencial.getText());
        double mediaNormal = Double.parseDouble(this.txtMediaNormal.getText());
        double desviacionNormal = Double.parseDouble(this.txtDesvNormal.getText());

        // Arrays para almacenar los datos
        double[] rnLlegada = new double[numPiezas];
        double[] tiempoEntreLlegadas = new double[numPiezas];
        double[] minutoLlegada = new double[numPiezas];
        double[] minutoInicioInspeccion = new double[numPiezas];
        double[] rnInspeccion = new double[numPiezas];
        double[] tiempoInspeccion = new double[numPiezas];
        double[] minutoFinInspeccion = new double[numPiezas];
        double[] tiempoTotalInspeccion = new double[numPiezas];
        double[] tiempoEspera = new double[numPiezas];

        Random random = new Random();

        // Generar números aleatorios para ambas columnas
        for(int i = 0; i < numPiezas; i++) {
            rnLlegada[i] = random.nextDouble();
            rnInspeccion[i] = random.nextDouble();
        }

        // Calcular tiempo entre llegadas usando la distribución exponencial
        for(int i = 0; i < numPiezas; i++) {
            tiempoEntreLlegadas[i] = -Math.log(1.0 - rnLlegada[i]) * mediaExponencial;
        }

        // Calcular tiempo de llegada acumulado
        minutoLlegada[0] = tiempoEntreLlegadas[0];
        for(int i = 1; i < numPiezas; i++) {
            minutoLlegada[i] = minutoLlegada[i - 1] + tiempoEntreLlegadas[i];
        }

        // Calcular tiempo de inspección usando distribución normal
        for(int i = 0; i < numPiezas; i++) {
            tiempoInspeccion[i] = normInv(rnInspeccion[i], mediaNormal, desviacionNormal);
            if (tiempoInspeccion[i] < 0.0) {
                tiempoInspeccion[i] = 0.0;
            }
        }

        // Calcular tiempos de inicio, fin, duración total y espera
        minutoInicioInspeccion[0] = minutoLlegada[0];
        minutoFinInspeccion[0] = minutoInicioInspeccion[0] + tiempoInspeccion[0];
        tiempoEspera[0] = 0.0;
        tiempoTotalInspeccion[0] = tiempoInspeccion[0];

        for(int i = 1; i < numPiezas; i++) {
            minutoInicioInspeccion[i] = Math.max(minutoLlegada[i], minutoFinInspeccion[i - 1]);
            minutoFinInspeccion[i] = minutoInicioInspeccion[i] + tiempoInspeccion[i];
            tiempoEspera[i] = Math.max(0.0, minutoInicioInspeccion[i] - minutoLlegada[i]);
            tiempoTotalInspeccion[i] = minutoFinInspeccion[i] - minutoLlegada[i];
        }

        // Agregar filas a la tabla
        for(int i = 0; i < numPiezas; i++) {
            this.model.addRow(new Object[]{
                i + 1,
                String.format("%.4f", rnLlegada[i]),
                String.format("%.4f", tiempoEntreLlegadas[i]),
                String.format("%.4f", minutoLlegada[i]),
                String.format("%.4f", minutoInicioInspeccion[i]),
                String.format("%.4f", rnInspeccion[i]),
                String.format("%.4f", tiempoInspeccion[i]),
                String.format("%.4f", minutoFinInspeccion[i]),
                String.format("%.4f", tiempoTotalInspeccion[i]),
                String.format("%.4f", tiempoEspera[i])
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> (new ejercicio2Aleatorio()).setVisible(true));
    }
}
