package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que genera números aleatorios para simular demanda diaria. */
public class PanelVentasAleatorio extends JPanel {
    private final JSpinner spDias;
    private final JButton btnSimular;
    private final DefaultTableModel modeloDist;
    private final DefaultTableModel modeloSim;
    private final JLabel lblResumen;

    public PanelVentasAleatorio(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Ventas - Simulación aleatoria");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Días:")); spDias = new JSpinner(new SpinnerNumberModel(10,1,500,1)); controles.add(spDias);
        btnSimular = new JButton("Simular"); EstilosUI.aplicarEstiloBoton(btnSimular); controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        modeloDist = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribucion();
        JTable tDist = new JTable(modeloDist); EstilosUI.aplicarEstiloTabla(tDist); tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,55,70,70}; for(int i=0;i<anchos.length;i++) tDist.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        modeloSim = new DefaultTableModel(new Object[]{"Día","r","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tSim); tSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tSim.getColumnModel().getColumn(0).setPreferredWidth(40);
        tSim.getColumnModel().getColumn(1).setPreferredWidth(55);
        tSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tSim.getColumnModel().getColumn(3).setPreferredWidth(70);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tDist), new JScrollPane(tSim));
        split.setResizeWeight(0.42); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblResumen, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);
        simular(null); // primera corrida
    }

    private void llenarDistribucion(){
        modeloDist.setRowCount(0); double ini=0, acum=0; for(int i=0;i<VentasModelo.DEMANDA.length;i++){ double p=VentasModelo.PROBS[i]; acum+=p; if(i==VentasModelo.DEMANDA.length-1)acum=1.0; double fin=acum; modeloDist.addRow(new Object[]{UtilFormatoVentas.f2(p),UtilFormatoVentas.f2(acum),UtilFormatoVentas.f2(ini),UtilFormatoVentas.f2(fin),VentasModelo.DEMANDA[i],VentasModelo.GANANCIA[i]}); ini=fin; } }

    private void simular(ActionEvent e){
        int dias = (int) spDias.getValue(); modeloSim.setRowCount(0); int totalDem=0, totalGan=0; for(int d=1; d<=dias; d++){ double r=Math.random(); int demanda=VentasModelo.demandaPara(r); int gan=VentasModelo.gananciaParaDemanda(demanda); totalDem+=demanda; totalGan+=gan; modeloSim.addRow(new Object[]{d, UtilFormatoVentas.f2(r), demanda, gan}); } modeloSim.addRow(new Object[]{"Total","", totalDem, totalGan}); double promGan = totalGan/(double)dias; lblResumen.setText("Promedio ganancia = "+UtilFormatoVentas.f2(promGan)+" | Esperado ganancia = "+UtilFormatoVentas.f2(VentasModelo.esperadoGanancia())+" | Esperado demanda = "+UtilFormatoVentas.f2(VentasModelo.esperadoDemanda()));
    }
}

