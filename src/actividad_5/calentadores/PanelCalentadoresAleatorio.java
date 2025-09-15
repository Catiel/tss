package actividad_5.calentadores;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que simula semanas con números aleatorios generados internamente. */
public class PanelCalentadoresAleatorio extends JPanel {
    private final JSpinner spSemanas; // número de semanas
    private final JSpinner spInventario; // inventario fijo por semana
    private final JButton btnSimular; // ejecuta simulación

    private final DefaultTableModel modeloRangos; // tabla de distribución con rangos
    private final DefaultTableModel modeloSim;    // tabla de simulación
    private final JTextArea resumen;              // resumen de resultados

    public PanelCalentadoresAleatorio(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación aleatoria de ventas de calentadores");
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
        btnSimular = new JButton("Simular");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Tabla de rangos
        modeloRangos = new DefaultTableModel(new Object[]{"Prob","Acum","Inicio","Fin","Ventas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarRangos();
        JTable tRangos = new JTable(modeloRangos); EstilosUI.aplicarEstiloTabla(tRangos); tRangos.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,55,55};
        for(int i=0;i<anchos.length;i++) tRangos.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        // Tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{"Semana","Rand","Ventas","Faltante"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim){
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                Component c = super.prepareRenderer(renderer,row,column);
                if(row < getRowCount()-1){ // fila de datos
                    Object falt = getValueAt(row,3);
                    if("1".equals(String.valueOf(falt))) c.setBackground(new Color(0xFFCCCC));
                    else c.setBackground(Color.white);
                } else { c.setBackground(new Color(0xEFEFEF)); }
                return c;
            }
        }; EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60);
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70);
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(70);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                new JScrollPane(tRangos), new JScrollPane(tablaSim));
        split.setResizeWeight(0.35);
        split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        resumen = new JTextArea(); resumen.setEditable(false); resumen.setBackground(getBackground());
        resumen.setBorder(BorderFactory.createTitledBorder("Resumen")); resumen.setLineWrap(true); resumen.setWrapStyleWord(true);
        add(resumen, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);
    }

    private void llenarRangos(){
        modeloRangos.setRowCount(0); double inicio=0, acum=0; for(int i=0;i<CalentadoresModelo.VENTAS.length;i++){ double p=CalentadoresModelo.PROBS[i]; acum+=p; if(i==CalentadoresModelo.VENTAS.length-1)acum=1.0; double fin=acum; modeloRangos.addRow(new Object[]{UtilFormatoCalent.f2(p),UtilFormatoCalent.f2(acum),UtilFormatoCalent.f2(inicio),UtilFormatoCalent.f2(fin),CalentadoresModelo.VENTAS[i]}); inicio=fin; } }

    private void simular(ActionEvent e){
        int semanas = (int) spSemanas.getValue(); int inventario = (int) spInventario.getValue();
        modeloSim.setRowCount(0);
        int totalVentas=0; int faltantes=0; StringBuilder faltSemanas = new StringBuilder(); boolean first=true;
        for(int s=1;s<=semanas;s++){
            double r = Math.random(); int ventas = CalentadoresModelo.ventasPara(r);
            int falt = ventas>inventario?1:0; if(falt==1){ faltantes++; if(!first) faltSemanas.append(", "); faltSemanas.append(s); first=false; }
            totalVentas+=ventas;
            modeloSim.addRow(new Object[]{s, UtilFormatoCalent.f2(r), ventas, falt});
        }
        modeloSim.addRow(new Object[]{"Total","", totalVentas, faltantes});
        double prom = totalVentas/(double)semanas;
        resumen.setText("Faltantes: "+faltantes+ (faltantes>0?" (semanas "+faltSemanas+")":"") +"\nPromedio ventas simulado: "+UtilFormatoCalent.f2(prom)+"\nValor esperado analítico: "+UtilFormatoCalent.f2(CalentadoresModelo.esperado()));
    }
}

