package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con el ejemplo de 20 semanas usando números aleatorios proporcionados. */
public class PanelCalentadoresPredefinido extends JPanel {
    private final DefaultTableModel modeloFrecuencia; // tabla original (ventas, frecuencia)
    private final DefaultTableModel modeloProb;       // tabla con probabilidades
    private final DefaultTableModel modeloRangos;     // tabla con acumulada y rangos
    private final DefaultTableModel modeloSim;        // simulación 20 semanas

    private static final int[] FRECUENCIAS = {6,5,9,12,8,7,3};
    private static final double[] RAND = {0.10,0.24,0.03,0.32,0.23,0.59,0.95,0.34,0.34,0.51,0.08,0.48,0.66,0.97,0.03,0.96,0.46,0.74,0.77,0.44};

    public PanelCalentadoresPredefinido(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación de ventas de calentadores (ejemplo 20 semanas)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel panelTablas = new JPanel();
        panelTablas.setLayout(new BoxLayout(panelTablas, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(panelTablas);

        // 1) Frecuencias
        modeloFrecuencia = new DefaultTableModel(new Object[]{"Ventas/semana","# semanas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarFrecuencias();
        panelTablas.add(bloque("Frecuencias observadas (50 semanas)", modeloFrecuencia, new int[]{110,90}));
        panelTablas.add(Box.createVerticalStrut(6));

        // 2) Probabilidades
        modeloProb = new DefaultTableModel(new Object[]{"Ventas","# semanas","Prob"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarProbabilidades();
        panelTablas.add(bloque("Probabilidades empíricas", modeloProb, new int[]{70,80,70}));
        panelTablas.add(Box.createVerticalStrut(6));

        // 3) Rangos
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarRangos();
        panelTablas.add(bloque("Distribución acumulada y rangos", modeloRangos, new int[]{55,55,55,55,55}));
        panelTablas.add(Box.createVerticalStrut(8));

        // 4) Simulación
        modeloSim = new DefaultTableModel(new Object[]{"Semana","Rand","Ventas","Faltante"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(70);
        simular();
        panelTablas.add(rotulado("Simulación (inventario constante 8)", new JScrollPane(tablaSim)));

        JScrollPane spTop = new JScrollPane(panelTablas);

        // Resumen
        JTextArea resumen = new JTextArea();
        resumen.setEditable(false);
        resumen.setWrapStyleWord(true); resumen.setLineWrap(true);
        resumen.setBackground(getBackground());
        resumen.setBorder(BorderFactory.createTitledBorder("Resultados a), b), c)"));
        double promedioSim = promedioSimulado();
        StringBuilder sb = new StringBuilder();
        sb.append("a) Faltantes (semanas con demanda > 8): ").append(contarFaltantes()).append(" (semana(s) ");
        sb.append(listarSemanasFaltantes()).append(")\n");
        sb.append("b) Promedio de ventas en 20 semanas = ").append(UtilFormatoCalent.f2(promedioSim)).append(" calentadores/semana\n");
        sb.append("c) Valor esperado analítico E(ventas) = ").append(UtilFormatoCalent.f2(CalentadoresModelo.esperado()))
          .append(" calentadores/semana\nCon más semanas la media simulada se acerca al valor esperado.");
        resumen.setText(sb.toString());

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spTop, new JScrollPane(resumen));
        split.setResizeWeight(0.82);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);
    }

    private JPanel bloque(String titulo, DefaultTableModel modelo, int[] anchos){
        JTable t = new JTable(modelo); EstilosUI.aplicarEstiloTabla(t); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        for(int i=0;i<anchos.length && i<t.getColumnCount();i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);
        return rotulado(titulo,new JScrollPane(t));
    }
    private JPanel rotulado(String titulo, JComponent comp){
        JPanel p = new JPanel(new BorderLayout()); EstilosUI.aplicarEstiloPanel(p);
        JLabel l = new JLabel(titulo); l.setFont(l.getFont().deriveFont(Font.BOLD,11f));
        p.add(l, BorderLayout.NORTH); p.add(comp, BorderLayout.CENTER); return p; }

    private void llenarFrecuencias(){
        modeloFrecuencia.setRowCount(0);
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            modeloFrecuencia.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i]});
        }
        modeloFrecuencia.addRow(new Object[]{"Total",50});
    }
    private void llenarProbabilidades(){
        modeloProb.setRowCount(0);
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            modeloProb.addRow(new Object[]{CalentadoresModelo.VENTAS[i], FRECUENCIAS[i], UtilFormatoCalent.f2(CalentadoresModelo.PROBS[i])});
        }
        modeloProb.addRow(new Object[]{"Total",50,UtilFormatoCalent.f2(1.0)});
    }
    private void llenarRangos(){
        modeloRangos.setRowCount(0);
        double inicio=0, acum=0;
        for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){
            double p = CalentadoresModelo.PROBS[i];
            acum += p; if(i==CalentadoresModelo.VENTAS.length-1) acum = 1.0; double fin = acum;
            modeloRangos.addRow(new Object[]{UtilFormatoCalent.f2(p), UtilFormatoCalent.f2(acum), UtilFormatoCalent.f2(inicio), UtilFormatoCalent.f2(fin), CalentadoresModelo.VENTAS[i]});
            inicio = fin;
        }
    }
    private void simular(){
        modeloSim.setRowCount(0);
        int faltantes = 0;
        for(int s=0;s<RAND.length;s++){
            double r = RAND[s];
            int ventas = CalentadoresModelo.ventasPara(r);
            int falta = (ventas>CalentadoresModelo.INVENTARIO_FIJO)?1:0;
            if(falta==1) faltantes++;
            modeloSim.addRow(new Object[]{s+1, UtilFormatoCalent.f2(r), ventas, falta});
        }
        modeloSim.addRow(new Object[]{"Total","", totalVentas(), faltantes});
    }
    private int totalVentas(){ int t=0; for(int i=0;i<RAND.length;i++){ t+= CalentadoresModelo.ventasPara(RAND[i]); } return t; }
    private double promedioSimulado(){ return totalVentas() / (double) RAND.length; }
    private int contarFaltantes(){ int c=0; for(double r: RAND) if(CalentadoresModelo.ventasPara(r)>CalentadoresModelo.INVENTARIO_FIJO) c++; return c; }
    private String listarSemanasFaltantes(){ StringBuilder sb=new StringBuilder(); boolean first=true; for(int i=0;i<RAND.length;i++){ if(CalentadoresModelo.ventasPara(RAND[i])>CalentadoresModelo.INVENTARIO_FIJO){ if(!first) sb.append(", "); sb.append(i+1); first=false; } } return sb.toString(); }
}

