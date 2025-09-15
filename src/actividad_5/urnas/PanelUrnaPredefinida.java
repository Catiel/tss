package actividad_5.urnas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel que muestra la simulación con los 10 números pseudoaleatorios proporcionados por la práctica. */
public class PanelUrnaPredefinida extends JPanel {
    private final DefaultTableModel modelo;

    // Números dados en el enunciado (coma -> punto)
    private static final double[] NUMEROS = {0.81,0.95,0.79,0.24,0.26,0.34,0.51,0.72,0.08,0.94};

    public PanelUrnaPredefinida(){
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Simulación (números proporcionados por la práctica)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(titulo, BorderLayout.NORTH);

        modelo = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tabla = new JTable(modelo);
        EstilosUI.aplicarEstiloTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(getBackground());
        area.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        area.setText("Se extraen 10 pelotas (con reemplazo)." +
                "\nProbabilidades: 10% verdes, 40% rojas, 50% amarillas." +
                "\nRangos: [0,0.10)->verdes | [0.10,0.50)->rojas | [0.50,1.00]->amarillas.");
        add(area, BorderLayout.SOUTH);

        llenar();
    }

    private void llenar(){
        modelo.setRowCount(0);
        for(int i=0;i<NUMEROS.length;i++){
            double r = NUMEROS[i];
            String color = UrnaModelo.colorPara(r);
            modelo.addRow(new Object[]{i+1, UtilFormatoUrnas.fmt(r), color});
        }
    }
}
