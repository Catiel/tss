package actividad_5.ventas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios y simular ventas. */
public class PanelVentasManual extends JPanel {
    private final JSpinner spDias; private final JButton btnCrear; private final JButton btnCalcular;
    private final DefaultTableModel modeloDist; private final DefaultTableModel modeloSim; private final JLabel lblResumen;

    public PanelVentasManual(){
        setLayout(new BorderLayout(8,8)); EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Ventas - Simulación manual"); EstilosUI.aplicarEstiloTitulo(titulo); add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Días:")); spDias = new JSpinner(new SpinnerNumberModel(10,1,500,1)); controles.add(spDias);
        btnCrear = new JButton("Crear filas"); EstilosUI.aplicarEstiloBoton(btnCrear); controles.add(btnCrear);
        btnCalcular = new JButton("Calcular"); EstilosUI.aplicarEstiloBoton(btnCalcular); controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        modeloDist = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Demanda","Ganancia"},0){@Override public boolean isCellEditable(int r,int c){return false;}}; llenarDistribucion();
        JTable tDist = new JTable(modeloDist); EstilosUI.aplicarEstiloTabla(tDist); tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); int[] anchos={55,55,55,55,70,70}; for(int i=0;i<anchos.length;i++) tDist.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        modeloSim = new DefaultTableModel(new Object[]{"Día","r","Demanda","Ganancia"},0){ @Override public boolean isCellEditable(int r,int c){ return c==1; } @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; } };
        JTable tSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tSim); tSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); tSim.getColumnModel().getColumn(0).setPreferredWidth(40); tSim.getColumnModel().getColumn(1).setPreferredWidth(55); tSim.getColumnModel().getColumn(2).setPreferredWidth(70); tSim.getColumnModel().getColumn(3).setPreferredWidth(70);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tDist), new JScrollPane(tSim)); split.setResizeWeight(0.42); split.setOneTouchExpandable(true); add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8)); add(lblResumen, BorderLayout.SOUTH);

        btnCrear.addActionListener(this::crearFilas); btnCalcular.addActionListener(this::calcular); btnCalcular.setEnabled(false);
    }

    private void llenarDistribucion(){ modeloDist.setRowCount(0); double ini=0,acum=0; for(int i=0;i<VentasModelo.DEMANDA.length;i++){ double p=VentasModelo.PROBS[i]; acum+=p; if(i==VentasModelo.DEMANDA.length-1)acum=1.0; double fin=acum; modeloDist.addRow(new Object[]{UtilFormatoVentas.f2(p),UtilFormatoVentas.f2(acum),UtilFormatoVentas.f2(ini),UtilFormatoVentas.f2(fin),VentasModelo.DEMANDA[i],VentasModelo.GANANCIA[i]}); ini=fin; } }

    private void crearFilas(ActionEvent e){ int dias=(int)spDias.getValue(); modeloSim.setRowCount(0); for(int d=1; d<=dias; d++) modeloSim.addRow(new Object[]{d, "", "", ""}); modeloSim.addRow(new Object[]{"Total","","",""}); btnCalcular.setEnabled(true); }

    private Double parse(Object v){ if(v==null) return null; String t=v.toString().trim().replace(',', '.'); if(t.isEmpty()) return null; try{ double d=Double.parseDouble(t); if(d<0||d>=1) return null; return d; }catch(Exception ex){ return null;} }

    private void calcular(ActionEvent e){ if(modeloSim.getRowCount()==0) return; // quitar fila total y recalcular
        int last = modeloSim.getRowCount()-1; if("Total".equals(String.valueOf(modeloSim.getValueAt(last,0)))) modeloSim.removeRow(last);
        int totalDem=0,totalGan=0; for(int i=0;i<modeloSim.getRowCount();i++){ Double r=parse(modeloSim.getValueAt(i,1)); if(r==null){ JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": número aleatorio inválido (0<=r<1)","Error",JOptionPane.ERROR_MESSAGE); return; } int demanda=VentasModelo.demandaPara(r); int gan=VentasModelo.gananciaParaDemanda(demanda); totalDem+=demanda; totalGan+=gan; modeloSim.setValueAt(UtilFormatoVentas.f2(r), i,1); modeloSim.setValueAt(demanda, i,2); modeloSim.setValueAt(gan, i,3); }
        modeloSim.addRow(new Object[]{"Total","", totalDem, totalGan}); double promGan = totalGan/(double)modeloSim.getRowCount()-1; lblResumen.setText("Promedio ganancia = "+UtilFormatoVentas.f2(promGan)+" | Esperado ganancia = "+UtilFormatoVentas.f2(VentasModelo.esperadoGanancia())); }
}

