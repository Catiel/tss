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

public class ejercicio2 extends JFrame {
    private final JTextField txtNumPiezas;
    private final JTextField txtMediaExponencial;
    private final JTextField txtMediaNormal;
    private final JTextField txtDesvNormal;
    private final DefaultTableModel model;

    public ejercicio2() {
        this.setTitle("Simulación Completa de Inspección");
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

    // Implementación de la función inversa de la distribución normal estándar
    // Equivalente a NORM.INV en Excel
    private double normInv(double p) {
        // Constantes para la aproximación de Beasley-Springer-Moro
        double a0 = -3.969683028665376e+01;
        double a1 =  2.209460984245205e+02;
        double a2 = -2.759285104469687e+02;
        double a3 =  1.383577518672690e+02;
        double a4 = -3.066479806614716e+01;
        double a5 =  2.506628277459239e+00;

        double b1 = -5.447609879822406e+01;
        double b2 =  1.615858368580409e+02;
        double b3 = -1.556989798598866e+02;
        double b4 =  6.680131188771972e+01;
        double b5 = -1.328068155288572e+01;

        double c0 = -7.784894002430293e-03;
        double c1 = -3.223964580411365e-01;
        double c2 = -2.400758277161838e+00;
        double c3 = -2.549732539343734e+00;
        double c4 =  4.374664141464968e+00;
        double c5 =  2.938163982698783e+00;

        double d1 =  7.784695709041462e-03;
        double d2 =  3.224671290700398e-01;
        double d3 =  2.445134137142996e+00;
        double d4 =  3.754408661907416e+00;

        double pLow = 0.02425;
        double pHigh = 1 - pLow;
        double q, r;

        if (p < 0 || p > 1) {
            throw new IllegalArgumentException("p debe estar entre 0 y 1");
        } else if (p == 0) {
            return Double.NEGATIVE_INFINITY;
        } else if (p == 1) {
            return Double.POSITIVE_INFINITY;
        } else if (p < pLow) {
            q = Math.sqrt(-2 * Math.log(p));
            return (((((c0*q+c1)*q+c2)*q+c3)*q+c4)*q+c5) / ((((d1*q+d2)*q+d3)*q+d4)*q+1);
        } else if (p <= pHigh) {
            q = p - 0.5;
            r = q*q;
            return (((((a0*r+a1)*r+a2)*r+a3)*r+a4)*r+a5)*q / (((((b1*r+b2)*r+b3)*r+b4)*r+b5)*r+1);
        } else {
            q = Math.sqrt(-2 * Math.log(1-p));
            return -(((((c0*q+c1)*q+c2)*q+c3)*q+c4)*q+c5) / ((((d1*q+d2)*q+d3)*q+d4)*q+1);
        }
    }

    // Función para calcular NORM.INV(p, mu, sigma) = mu + sigma * NORM.INV(p, 0, 1)
    private double normInv(double p, double mu, double sigma) {
        return mu + sigma * normInv(p);
    }

    private void simular(ActionEvent var1) {
        this.model.setRowCount(0);
        int var2 = Integer.parseInt(this.txtNumPiezas.getText());
        double var3 = Double.parseDouble(this.txtMediaExponencial.getText());
        double var5 = Double.parseDouble(this.txtMediaNormal.getText());
        double var7 = Double.parseDouble(this.txtDesvNormal.getText());

        // Valores aleatorios predeterminados para Rn Llegada (columna 2)
        double[] valoresRnLlegada = {
            0.2962, 0.2883, 0.7287, 0.5568, 0.9641, 0.3651, 0.1524, 0.9198, 0.7633,
            0.3989, 0.2594, 0.4217, 0.9523, 0.7420, 0.4152, 0.8417, 0.6656, 0.1064
        };

        // Valores aleatorios predeterminados para Rn Inspección (columna 6)
        double[] valoresRnInspeccion = {
            0.7831, 0.6601, 0.5286, 0.7129, 0.0880, 0.8815, 0.0356, 0.4289, 0.7293,
            0.8502, 0.4793, 0.0455, 0.3672, 0.7548, 0.1636, 0.3114, 0.9976, 0.9619
        };

        double[] var9 = new double[var2];
        double[] var10 = new double[var2];
        double[] var11 = new double[var2];
        double[] var12 = new double[var2];
        double[] var13 = new double[var2];
        double[] var14 = new double[var2];
        double[] var15 = new double[var2];
        double[] var16 = new double[var2];
        double[] var17 = new double[var2];
        Random var18 = new Random();

        // Usar valores predeterminados para Rn Llegada y Rn Inspección
        for(int var19 = 0; var19 < var2; ++var19) {
            if (var19 < valoresRnLlegada.length) {
                var9[var19] = valoresRnLlegada[var19];
            } else {
                var9[var19] = var18.nextDouble(); // Si se excede el array, generar aleatorio
            }

            if (var19 < valoresRnInspeccion.length) {
                var13[var19] = valoresRnInspeccion[var19];
            } else {
                var13[var19] = var18.nextDouble(); // Si se excede el array, generar aleatorio
            }
        }

        // Calcular tiempo entre llegadas usando media fija de 5
        for(int var20 = 0; var20 < var2; ++var20) {
            var10[var20] = -Math.log(1.0 - var9[var20]) * 5.0; // Media fija de 5
        }

        // Calcular tiempo de llegada acumulado
        var11[0] = var10[0];
        for(int var21 = 1; var21 < var2; ++var21) {
            var11[var21] = var11[var21 - 1] + var10[var21];
        }

        // Calcular tiempo de inspección usando NORM.INV con valores fijos (media=4, sigma=0.5)
        for(int var22 = 0; var22 < var2; ++var22) {
            var14[var22] = normInv(var13[var22], 4.0, 0.5); // Equivalente a DISTR.NORM.INV(segundo rn;4;0.5)
            if (var14[var22] < 0.0) {
                var14[var22] = 0.0;
            }
        }

        // Calcular tiempos de inicio, fin, duración y espera
        var12[0] = var11[0];
        var15[0] = var12[0] + var14[0];
        var17[0] = 0.0;
        var16[0] = var14[0];

        for(int var23 = 1; var23 < var2; ++var23) {
            var12[var23] = Math.max(var11[var23], var15[var23 - 1]);
            var15[var23] = var12[var23] + var14[var23];
            var17[var23] = Math.max(0.0, var12[var23] - var11[var23]);
            var16[var23] = var15[var23] - var11[var23];
        }

        // Agregar filas a la tabla
        for(int var24 = 0; var24 < var2; ++var24) {
            this.model.addRow(new Object[]{var24 + 1, String.format("%.4f", var9[var24]), String.format("%.4f", var10[var24]), String.format("%.4f", var11[var24]), String.format("%.4f", var12[var24]), String.format("%.4f", var13[var24]), String.format("%.4f", var14[var24]), String.format("%.4f", var15[var24]), String.format("%.4f", var16[var24]), String.format("%.4f", var17[var24])});
        }

    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> (new ejercicio2()).setVisible(true));
    }
}
