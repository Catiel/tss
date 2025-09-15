package actividad_5.urnas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel que muestra la simulación con los 10 números pseudoaleatorios proporcionados por la práctica. */
public class PanelUrnaPredefinida extends JPanel {
    // Modelo para la tabla de simulación
    private final DefaultTableModel modeloSim;
    // Modelo para la tabla de distribución
    private final DefaultTableModel modeloDist;

    // Números dados en el enunciado (coma -> punto)
    private static final double[] NUMEROS = {0.81,0.95,0.79,0.24,0.26,0.34,0.51,0.72,0.08,0.94};

    public PanelUrnaPredefinida(){
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Simulación (números proporcionados por la práctica)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(titulo, BorderLayout.NORTH);

        // Panel central que contendrá ambas tablas
        JPanel centro = new JPanel();
        centro.setLayout(new BoxLayout(centro, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(centro);

        // ----- Tabla de distribución -----
        modeloDist = new DefaultTableModel(new Object[]{
                "Distribuciones de probabilidad",
                "Distribución acumulada",
                "Rango inicio",
                "Rango fin",
                "Color"},0){
            @Override public boolean isCellEditable(int r,int c){ return false; }
        };
        JTable tablaDist = new JTable(modeloDist);
        EstilosUI.aplicarEstiloTabla(tablaDist);
        JScrollPane spDist = new JScrollPane(tablaDist);
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades"));
        centro.add(spDist);
        centro.add(Box.createVerticalStrut(8));

        // ----- Tabla de simulación -----
        modeloSim = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tablaSim = new JTable(modeloSim);
        EstilosUI.aplicarEstiloTabla(tablaSim);
        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setBorder(BorderFactory.createTitledBorder("Simulación de extracciones"));
        centro.add(spSim);

        add(centro, BorderLayout.CENTER);

        // Texto explicativo
        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setBackground(getBackground());
        area.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        area.setText("Se extraen 10 pelotas (con reemplazo)." +
                "\nProbabilidades: 10% verdes, 40% rojas, 50% amarillas." +
                "\nRangos: [0,0.10)->verdes | [0.10,0.50)->rojas | [0.50,1.00]->amarillas.");
        add(area, BorderLayout.SOUTH);

        generarDistribucion();
        llenarSimulacion();
    }

    /** Genera dinámicamente la tabla de distribución a partir de las constantes del modelo. */
    private void generarDistribucion(){
        modeloDist.setRowCount(0);
        double[] probs = {UrnaModelo.P_VERDE, UrnaModelo.P_ROJA, UrnaModelo.P_AMARILLA};
        String[] colores = {"verde","rojas","amarillas"};
        double acumulada = 0.0;
        double inicio = 0.0;
        for(int i=0;i<probs.length;i++){
            double p = probs[i];
            acumulada += p;
            if(i==probs.length-1) acumulada = 1.0; // asegurar 1.00 exacto
            double fin = acumulada;
            String pTxt = UtilFormatoUrnas.fmt(p);          // prob individual
            String acTxt = UtilFormatoUrnas.fmt(acumulada); // acumulada
            String iniTxt = UtilFormatoUrnas.fmt(inicio);   // rango inicio
            String finTxt = UtilFormatoUrnas.fmt(fin);      // rango fin
            modeloDist.addRow(new Object[]{pTxt, acTxt, iniTxt, finTxt, colores[i]});
            inicio = fin;
        }
    }

    /** Llena la tabla de simulación con los números pseudoaleatorios dados. */
    private void llenarSimulacion(){
        modeloSim.setRowCount(0);
        for(int i=0;i<NUMEROS.length;i++){
            double r = NUMEROS[i];
            String color = UrnaModelo.colorPara(r);
            modeloSim.addRow(new Object[]{i+1, UtilFormatoUrnas.fmt(r), color});
        }
    }
}
