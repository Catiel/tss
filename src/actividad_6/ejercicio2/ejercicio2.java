package actividad_6.ejercicio2;

//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

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

    private void simular(ActionEvent var1) {
        this.model.setRowCount(0);
        int var2 = Integer.parseInt(this.txtNumPiezas.getText());
        double var3 = Double.parseDouble(this.txtMediaExponencial.getText());
        double var5 = Double.parseDouble(this.txtMediaNormal.getText());
        double var7 = Double.parseDouble(this.txtDesvNormal.getText());
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

        for(int var19 = 0; var19 < var2; ++var19) {
            var9[var19] = var18.nextDouble();
            var13[var19] = var18.nextDouble();
        }

        for(int var20 = 0; var20 < var2; ++var20) {
            var10[var20] = -Math.log((double)1.0F - var9[var20]) * var3;
        }

        var11[0] = var10[0];

        for(int var21 = 1; var21 < var2; ++var21) {
            var11[var21] = var11[var21 - 1] + var10[var21];
        }

        for(int var22 = 0; var22 < var2; ++var22) {
            var14[var22] = var5 + var7 * var18.nextGaussian();
            if (var14[var22] < (double)0.0F) {
                var14[var22] = (double)0.0F;
            }
        }

        var12[0] = var11[0];
        var15[0] = var12[0] + var14[0];
        var17[0] = (double)0.0F;
        var16[0] = var14[0];

        for(int var23 = 1; var23 < var2; ++var23) {
            var12[var23] = Math.max(var11[var23], var15[var23 - 1]);
            var15[var23] = var12[var23] + var14[var23];
            var17[var23] = Math.max((double)0.0F, var12[var23] - var11[var23]);
            var16[var23] = var15[var23] - var11[var23];
        }

        for(int var24 = 0; var24 < var2; ++var24) {
            this.model.addRow(new Object[]{var24 + 1, String.format("%.4f", var9[var24]), String.format("%.4f", var10[var24]), String.format("%.4f", var11[var24]), String.format("%.4f", var12[var24]), String.format("%.4f", var13[var24]), String.format("%.4f", var14[var24]), String.format("%.4f", var15[var24]), String.format("%.4f", var16[var24]), String.format("%.4f", var17[var24])});
        }

    }

    public static void main(String[] var0) {
        SwingUtilities.invokeLater(() -> (new ejercicio2()).setVisible(true));
    }
}

