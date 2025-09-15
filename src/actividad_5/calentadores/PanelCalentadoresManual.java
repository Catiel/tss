package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios U(0,1) y simular ventas semanales. */
public class PanelCalentadoresManual extends JPanel {
    private final JSpinner spSemanas;
    private final JSpinner spInventario;
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final DefaultTableModel modeloRangos;
    private final DefaultTableModel modeloSim;
    private final JTextArea resumen;

    public PanelCalentadoresManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación manual de ventas de calentadores");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Semanas:"));
        spSemanas = new JSpinner(new SpinnerNumberModel(20,1,200,1));
        controles.add(spSemanas);
        controles.add(new JLabel("Inventario fijo:"));
        spInventario = new JSpinner(new SpinnerNumberModel(CalentadoresModelo.INVENTARIO_FIJO,1,500,1));
        controles.add(spInventario);
        btnCrear = new JButton("Crear filas"); EstilosUI.aplicarEstiloBoton(btnCrear); controles.add(btnCrear);
        btnCalcular = new JButton("Calcular"); EstilosUI.aplicarEstiloBoton(btnCalcular); controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Tabla de rangos (referencia)
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarRangos();
        JTable tRangos = new JTable(modeloRangos); EstilosUI.aplicarEstiloTabla(tRangos); tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,55,55};
        for(int i=0;i<anchos.length;i++) tRangos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Tabla de simulación (Rand editable)
        modeloSim = new DefaultTableModel(new Object[]{"Semana","Rand","Ventas","Faltante"},0){
            @Override public boolean isCellEditable(int r,int c){ return c==1; }
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tablaSim = new JTable(modeloSim){
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                Component c = super.prepareRenderer(renderer,row,column);
                if(row < getRowCount()-1){
                    Object falt = getValueAt(row,3);
                    if("1".equals(String.valueOf(falt))) c.setBackground(new Color(0xFFCCCC));
                    else c.setBackground(Color.white);
                } else c.setBackground(new Color(0xEFEFEF));
                return c;
            }
        }; EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(70);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(tRangos), new JScrollPane(tablaSim));
        split.setResizeWeight(0.35); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        resumen = new JTextArea(); resumen.setEditable(false); resumen.setBackground(getBackground()); resumen.setLineWrap(true); resumen.setWrapStyleWord(true);
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen")); add(resumen, BorderLayout.SOUTH);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);
    }

    private void llenarRangos(){
        modeloRangos.setRowCount(0); double inicio=0, acum=0; for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ double p=CalentadoresModelo.PROBS[i]; acum+=p; if(i==CalentadoresModelo.VENTAS.length-1)acum=1.0; double fin=acum; modeloRangos.addRow(new Object[]{UtilFormatoCalent.f2(p),UtilFormatoCalent.f2(acum),UtilFormatoCalent.f2(inicio),UtilFormatoCalent.f2(fin),CalentadoresModelo.VENTAS[i]}); inicio=fin; } }

    private void crearFilas(ActionEvent e){
        int semanas = (int) spSemanas.getValue(); modeloSim.setRowCount(0); for(int s=1;s<=semanas;s++) modeloSim.addRow(new Object[]{s, "", "", ""}); modeloSim.addRow(new Object[]{"Total","","",""}); btnCalcular.setEnabled(true); }

    private Double parse(Object v){ if(v==null) return null; String t=v.toString().trim().replace(',', '.'); if(t.isEmpty()) return null; try{ double d=Double.parseDouble(t); if(d<0||d>=1) return null; return d;}catch(Exception ex){ return null;} }

    private void calcular(ActionEvent e){
        if(modeloSim.getRowCount()==0) return; int inventario=(int) spInventario.getValue();
        // quitar fila Total si existe al final
        int lastIndex = modeloSim.getRowCount()-1;
        if("Total".equals(String.valueOf(modeloSim.getValueAt(lastIndex,0)))) modeloSim.removeRow(lastIndex);
        int totalVentas=0; int faltantes=0; StringBuilder faltSem=new StringBuilder(); boolean first=true;
        for(int i=0;i<modeloSim.getRowCount();i++){
            Double r = parse(modeloSim.getValueAt(i,1)); if(r==null){ JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": Rand inválido (0<=r<1)","Error",JOptionPane.ERROR_MESSAGE); return; }
            int ventas = CalentadoresModelo.ventasPara(r);
            int falt = ventas>inventario?1:0; if(falt==1){ faltantes++; if(!first) faltSem.append(", "); faltSem.append(i+1); first=false; }
            totalVentas += ventas;
            modeloSim.setValueAt(UtilFormatoCalent.f2(r), i,1);
            modeloSim.setValueAt(ventas, i,2);
            modeloSim.setValueAt(falt, i,3);
        }
        modeloSim.addRow(new Object[]{"Total","", totalVentas, faltantes});
        double prom = totalVentas/(double)(modeloSim.getRowCount()-1);
        resumen.setText("Faltantes: "+faltantes+(faltantes>0?" (semanas "+faltSem+")":"")+"\nPromedio ventas simulado: "+UtilFormatoCalent.f2(prom)+"\nValor esperado analítico: "+UtilFormatoCalent.f2(CalentadoresModelo.esperado()));
    }
}

