package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con los datos predefinidos y simulación de 10 días usando números aleatorios dados. */
public class PanelVentasPredefinido extends JPanel {
    private final DefaultTableModel modeloDistrib;
    private final DefaultTableModel modeloSim;
    private final JLabel lblResumen;

    // Números aleatorios predefinidos (10 días) según imagen (0.07,0.60,...)
    private static final double[] RAND = {0.07,0.60,0.77,0.49,0.76,0.95,0.51,0.16,0.14,0.85};

    public PanelVentasPredefinido(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Ventas - Ejemplo predefinido (10 días)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Tabla distribución
        modeloDistrib = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribucion();
        JTable tDist = new JTable(modeloDistrib); EstilosUI.aplicarEstiloTabla(tDist); tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,55,70,70}; for(int i=0;i<anchos.length;i++) tDist.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Simulación
        modeloSim = new DefaultTableModel(new Object[]{"Día","r","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tSim); tSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tSim.getColumnModel().getColumn(0).setPreferredWidth(40);
        tSim.getColumnModel().getColumn(1).setPreferredWidth(55);
        tSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tSim.getColumnModel().getColumn(3).setPreferredWidth(70);
        simular();

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tDist), new JScrollPane(tSim));
        split.setResizeWeight(0.45); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblResumen, BorderLayout.SOUTH);
        actualizarResumen();
    }

    private void llenarDistribucion(){
        modeloDistrib.setRowCount(0);
        double inicio=0, acum=0;
        for(int i=0;i<VentasModelo.DEMANDA.length;i++){
            double p = VentasModelo.PROBS[i];
            acum += p; if(i==VentasModelo.DEMANDA.length-1) acum=1.0; double fin=acum;
            modeloDistrib.addRow(new Object[]{UtilFormatoVentas.f2(p), UtilFormatoVentas.f2(acum), UtilFormatoVentas.f2(inicio), UtilFormatoVentas.f2(fin), VentasModelo.DEMANDA[i], VentasModelo.GANANCIA[i]});
            inicio=fin;
        }
    }

    private void simular(){
        modeloSim.setRowCount(0);
        for(int i=0;i<RAND.length;i++){
            double r = RAND[i];
            int demanda = VentasModelo.demandaPara(r);
            int gan = VentasModelo.gananciaParaDemanda(demanda);
            modeloSim.addRow(new Object[]{i+1, UtilFormatoVentas.f2(r), demanda, gan});
        }
        modeloSim.addRow(new Object[]{"Total","", totalDemanda(), totalGanancia()});
    }

    private int totalDemanda(){ int s=0; for(double r: RAND) s += VentasModelo.demandaPara(r); return s; }
    private int totalGanancia(){ int s=0; for(double r: RAND) s += VentasModelo.gananciaParaDemanda(VentasModelo.demandaPara(r)); return s; }

    private void actualizarResumen(){
        double promGan = totalGanancia()/ (double) RAND.length;
        lblResumen.setText("Promedio ganancia (10 días) = " + UtilFormatoVentas.f2(promGan) + " | Esperado demanda = " + UtilFormatoVentas.f2(VentasModelo.esperadoDemanda()) + " | Esperado ganancia = " + UtilFormatoVentas.f2(VentasModelo.esperadoGanancia()));
    }
}

