package actividad_6.ejercicio3;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

public class ejercicio3 extends JFrame {

    private final JTextField txtNumMuestras;
    private final JTextField txtMinBarraA;
    private final JTextField txtMaxBarraA;
    private final JTextField txtValorEsperadoErlang;
    private final JTextField txtFormaErlang;
    private final JTextField txtEspecInf;
    private final JTextField txtEspecSup;
    private final DefaultTableModel model;

    // Valores predeterminados para Rn (Barra A - Distribución Uniforme)
    private final double[] valoresRn = {
        0.6367, 0.0640, 0.6685, 0.2177, 0.6229, 0.6813, 0.1551, 0.7678,
        0.8208, 0.4394, 0.9858, 0.6969, 0.4822, 0.9188, 0.7084
    };

    // Valores predeterminados para Rn1 (Barra B - Distribución Erlang)
    private final double[] valoresRn1 = {
        0.0887, 0.2574, 0.2031, 0.1525, 0.9888, 0.7149, 0.2019, 0.2213,
        0.9547, 0.9271, 0.6493, 0.3526, 0.7490, 0.6757, 0.8639
    };

    // Valores predeterminados para Rn2 (Barra B - Distribución Erlang)
    private final double[] valoresRn2 = {
        0.3345, 0.2086, 0.2513, 0.0631, 0.9721, 0.4351, 0.6910, 0.1118,
        0.2845, 0.8603, 0.4857, 0.3081, 0.1997, 0.6390, 0.3940
    };

    // Valores predeterminados para Rn3 (Barra B - Distribución Erlang)
    private final double[] valoresRn3 = {
        0.6019, 0.5317, 0.0923, 0.2564, 0.0830, 0.7227, 0.3506, 0.6067,
        0.0808, 0.3498, 0.9285, 0.6747, 0.0057, 0.7344, 0.0645
    };

    // Valores predeterminados para Rn4 (Barra B - Distribución Erlang)
    private final double[] valoresRn4 = {
        0.5768, 0.8775, 0.7669, 0.8342, 0.4201, 0.6741, 0.9184, 0.7222,
        0.9865, 0.9188, 0.0485, 0.5950, 0.4104, 0.9229, 0.6055
    };

    public ejercicio3() {
        setTitle("Simulación Barras Defectuosas");
        setSize(1100, 450);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel panelInput = new JPanel(new FlowLayout());

        panelInput.add(new JLabel("Número de Ensambles:"));
        txtNumMuestras = new JTextField("15", 5);
        txtNumMuestras.setEditable(false);
        panelInput.add(txtNumMuestras);

        panelInput.add(new JLabel("Mínimo Barra A (cm):"));
        txtMinBarraA = new JTextField("45", 5);
        txtMinBarraA.setEditable(false);
        panelInput.add(txtMinBarraA);

        panelInput.add(new JLabel("Máximo Barra A (cm):"));
        txtMaxBarraA = new JTextField("55", 5);
        txtMaxBarraA.setEditable(false);
        panelInput.add(txtMaxBarraA);

        panelInput.add(new JLabel("Valor Esperado Erlang (cm):"));
        txtValorEsperadoErlang = new JTextField("30", 5);
        txtValorEsperadoErlang.setEditable(false);
        panelInput.add(txtValorEsperadoErlang);

        panelInput.add(new JLabel("Parámetro forma Erlang k:"));
        txtFormaErlang = new JTextField("4", 5);
        txtFormaErlang.setEditable(false);
        panelInput.add(txtFormaErlang);

        panelInput.add(new JLabel("Especificación inferior (cm):"));
        txtEspecInf = new JTextField("70", 5);
        txtEspecInf.setEditable(false);
        panelInput.add(txtEspecInf);

        panelInput.add(new JLabel("Especificación superior (cm):"));
        txtEspecSup = new JTextField("90", 5);
        txtEspecSup.setEditable(false);
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

        int acumDefectuosas = 0;

        for (int i = 1; i <= n; i++) {
            // Usar valores predeterminados en lugar de números aleatorios
            double rnA = valoresRn[i-1];  // i-1 porque el array empieza en índice 0
            double dimBarraA = minA + (maxA - minA) * rnA;

            // Usar valores predeterminados para Erlang
            double rn1 = valoresRn1[i-1];
            double rn2 = valoresRn2[i-1];
            double rn3 = valoresRn3[i-1];
            double rn4 = valoresRn4[i-1];

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
                String.format("%.2f", dimBarraA),
                String.format("%.4f", rn1),
                String.format("%.4f", rn2),
                String.format("%.4f", rn3),
                String.format("%.4f", rn4),
                String.format("%.2f", dimBarraB),
                String.format("%.2f", longitudTotal),
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
