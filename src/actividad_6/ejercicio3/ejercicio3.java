package actividad_6.ejercicio3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

public class ejercicio3 extends JFrame {

    private final JTextField txtNumMuestras;
    private final JTextField txtMinBarraA;
    private final JTextField txtMaxBarraA;
    private final JTextField txtValorEsperadoErlang;
    private final JTextField txtFormaErlang;
    private final JTextField txtEspecInf;
    private final JTextField txtEspecSup;
    private final DefaultTableModel model;

    public ejercicio3() {
        setTitle("Simulación Barras Defectuosas");
        setSize(1100, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelInput = new JPanel(new FlowLayout());

        panelInput.add(new JLabel("Número de Ensambles:"));
        txtNumMuestras = new JTextField("15", 5);
        panelInput.add(txtNumMuestras);

        panelInput.add(new JLabel("Mínimo Barra A (cm):"));
        txtMinBarraA = new JTextField("45", 5);
        panelInput.add(txtMinBarraA);

        panelInput.add(new JLabel("Máximo Barra A (cm):"));
        txtMaxBarraA = new JTextField("55", 5);
        panelInput.add(txtMaxBarraA);

        panelInput.add(new JLabel("Valor Esperado Erlang (cm):"));
        txtValorEsperadoErlang = new JTextField("30", 5);
        panelInput.add(txtValorEsperadoErlang);

        panelInput.add(new JLabel("Parámetro forma Erlang k:"));
        txtFormaErlang = new JTextField("4", 5);
        panelInput.add(txtFormaErlang);

        panelInput.add(new JLabel("Especificación inferior (cm):"));
        txtEspecInf = new JTextField("70", 5);
        panelInput.add(txtEspecInf);

        panelInput.add(new JLabel("Especificación superior (cm):"));
        txtEspecSup = new JTextField("90", 5);
        panelInput.add(txtEspecSup);

        JButton btnSimular = new JButton("Simular");
        panelInput.add(btnSimular);

        String[] columnas = {
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1",
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)",
            "Longitud total (cm)", "Especificación inferior (cm)",
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No",
            "Piezas defectuosas acumuladas", "% piezas defectuosas"
        };
        model = new DefaultTableModel(columnas, 0);
        JTable tabla = new JTable(model);
        JScrollPane scrollPane = new JScrollPane(tabla);

        add(panelInput, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);

        btnSimular.addActionListener(this::simular);
    }

    private void simular(ActionEvent e) {
        model.setRowCount(0);

        int n = Integer.parseInt(txtNumMuestras.getText());
        double minA = Double.parseDouble(txtMinBarraA.getText());
        double maxA = Double.parseDouble(txtMaxBarraA.getText());
        double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText());
        int kErlang = Integer.parseInt(txtFormaErlang.getText());
        double especInf = Double.parseDouble(txtEspecInf.getText());
        double especSup = Double.parseDouble(txtEspecSup.getText());

        Random random = new Random();
        int acumDefectuosas = 0;

        for (int i = 1; i <= n; i++) {
            double rnA = random.nextDouble();
            double dimBarraA = minA + (maxA - minA) * rnA;

            // 4 aleatorios uniformes para Erlang
            double rn1 = random.nextDouble();
            double rn2 = random.nextDouble();
            double rn3 = random.nextDouble();
            double rn4 = random.nextDouble();

            // Sumar ln(1 - ri) para Erlang
            double lnProduct = Math.log(1 - rn1) + Math.log(1 - rn2) + Math.log(1 - rn3) + Math.log(1 - rn4);
            // Erlang Er = -(1/(k*lambda)) * ln(product(1-ri)), lambda = 1/valorEsperado
            double dimBarraB = -(valorEsperado / kErlang) * lnProduct;

            double longitudTotal = dimBarraA + dimBarraB;

            boolean defectuosa = (longitudTotal < especInf) || (longitudTotal > especSup);
            int defectInt = defectuosa ? 1 : 0;
            acumDefectuosas += defectInt;
            double porcentaje = (double) acumDefectuosas / i;

            model.addRow(new Object[] {
                i,
                String.format("%.4f", rnA),
                String.format("%.4f", dimBarraA),
                String.format("%.4f", rn1),
                String.format("%.4f", rn2),
                String.format("%.4f", rn3),
                String.format("%.4f", rn4),
                String.format("%.4f", dimBarraB),
                String.format("%.4f", longitudTotal),
                String.format("%.2f", especInf),
                String.format("%.2f", especSup),
                defectInt,
                acumDefectuosas,
                String.format("%.2f%%", porcentaje*100)
            });
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ejercicio3().setVisible(true);
        });
    }
}

