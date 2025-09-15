package actividad_5.colas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios de llegada y servicio. */
public class PanelColaManual extends JPanel {
    private final JSpinner spClientes; // número de clientes
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final DefaultTableModel modeloServ;
    private final DefaultTableModel modeloLleg;
    private final DefaultTableModel modeloSim;
    private final JLabel lblResumen;

    public PanelColaManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Cola de banco - simulación manual");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Clientes:"));
        spClientes = new JSpinner(new SpinnerNumberModel(10,1,500,1));
        controles.add(spClientes);
        btnCrear = new JButton("Crear filas"); EstilosUI.aplicarEstiloBoton(btnCrear); controles.add(btnCrear);
        btnCalcular = new JButton("Calcular" ); EstilosUI.aplicarEstiloBoton(btnCalcular); controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Distribuciones de referencia
        modeloServ = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tServ"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloLleg = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tInter"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribuciones();
        JPanel panelDist = new JPanel(); panelDist.setLayout(new BoxLayout(panelDist, BoxLayout.Y_AXIS)); EstilosUI.aplicarEstiloPanel(panelDist);
        panelDist.add(bloque("Distribución tiempo servicio", modeloServ, new int[]{55,55,55,55,55})); panelDist.add(Box.createVerticalStrut(6));
        panelDist.add(bloque("Distribución intervalo llegadas", modeloLleg, new int[]{55,55,55,55,55}));

        // Tabla editable de simulación: columnas rLleg y rServ editables
        modeloSim = new DefaultTableModel(new Object[]{"Cliente","rLleg","IntArr","HoraLleg","rServ","tServ","InicioServ","FinServ","Espera","Ocioso"},0){
            @Override public boolean isCellEditable(int r,int c){ return c==1 || c==4; }
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,75,55,55,80,80,55,55}; for(int i=0;i<anchos.length;i++) tablaSim.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(panelDist), new JScrollPane(tablaSim));
        split.setResizeWeight(0.40); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblResumen, BorderLayout.SOUTH);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);
    }

    private JPanel bloque(String titulo, DefaultTableModel modelo, int[] anchos){ JTable t = new JTable(modelo); EstilosUI.aplicarEstiloTabla(t); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); for(int i=0;i<anchos.length;i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]); return rotulado(titulo,new JScrollPane(t)); }
    private JPanel rotulado(String titulo, JComponent c){ JPanel p=new JPanel(new BorderLayout()); EstilosUI.aplicarEstiloPanel(p); JLabel l=new JLabel(titulo); l.setFont(l.getFont().deriveFont(Font.BOLD,11f)); p.add(l,BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); return p; }

    private void llenarDistribuciones(){
        double acum=0, ini=0; modeloServ.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.SERVICIO_VALORES.length;i++){ double p=ColaBancoModelo.SERVICIO_PROBS[i]; acum+=p; if(i==ColaBancoModelo.SERVICIO_VALORES.length-1)acum=1.0; double fin=acum; modeloServ.addRow(new Object[]{UtilFormatoColas.f2(p),UtilFormatoColas.f2(acum),UtilFormatoColas.f2(ini),UtilFormatoColas.f2(fin),ColaBancoModelo.SERVICIO_VALORES[i]}); ini=fin; }
        acum=0; ini=0; modeloLleg.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.LLEGADA_VALORES.length;i++){ double p=ColaBancoModelo.LLEGADA_PROBS[i]; acum+=p; if(i==ColaBancoModelo.LLEGADA_VALORES.length-1)acum=1.0; double fin=acum; modeloLleg.addRow(new Object[]{UtilFormatoColas.f2(p),UtilFormatoColas.f2(acum),UtilFormatoColas.f2(ini),UtilFormatoColas.f2(fin),ColaBancoModelo.LLEGADA_VALORES[i]}); ini=fin; }
    }

    private void crearFilas(ActionEvent e){
        int clientes = (int) spClientes.getValue(); modeloSim.setRowCount(0);
        for(int i=1;i<=clientes;i++) modeloSim.addRow(new Object[]{i, "", "", "", "", "", "", "", "", ""});
        btnCalcular.setEnabled(true);
    }

    private Double parseRand(Object v){ if(v==null) return null; String t=v.toString().trim().replace(',', '.'); if(t.isEmpty()) return null; try{ double d=Double.parseDouble(t); if(d<0||d>=1) return null; return d; }catch(Exception ex){ return null; } }

    private void calcular(ActionEvent e){
        if(modeloSim.getRowCount()==0) return;
        int relojLleg=0; int finPrev=0; int totalEspera=0; int totalOcio=0; int clientes=modeloSim.getRowCount();
        for(int row=0; row<clientes; row++){
            Double rLleg = parseRand(modeloSim.getValueAt(row,1)); if(rLleg==null){ error(row,"rLleg"); return; }
            int interArr = ColaBancoModelo.tiempoInterLlegada(rLleg);
            relojLleg += interArr; String horaLleg = UtilFormatoColas.horaDesdeBase(relojLleg);
            Double rServ = parseRand(modeloSim.getValueAt(row,4)); if(rServ==null){ error(row,"rServ"); return; }
            int tServ = ColaBancoModelo.tiempoServicio(rServ);
            int inicioServ = Math.max(relojLleg, finPrev); int espera = inicioServ - relojLleg; int ocio = (inicioServ>finPrev)?inicioServ - finPrev:0; int finServ = inicioServ + tServ;
            totalEspera += espera; totalOcio += ocio; finPrev = finServ;
            modeloSim.setValueAt(interArr,row,2); modeloSim.setValueAt(horaLleg,row,3); modeloSim.setValueAt(tServ,row,5);
            modeloSim.setValueAt(UtilFormatoColas.horaDesdeBase(inicioServ),row,6); modeloSim.setValueAt(UtilFormatoColas.horaDesdeBase(finServ),row,7);
            modeloSim.setValueAt(espera,row,8); modeloSim.setValueAt(ocio,row,9);
            modeloSim.setValueAt(UtilFormatoColas.f2(rLleg),row,1); modeloSim.setValueAt(UtilFormatoColas.f2(rServ),row,4);
        }
        double promEspera = totalEspera/(double) clientes; lblResumen.setText("Promedio espera = "+UtilFormatoColas.f2(promEspera)+" min | Ocioso total = "+totalOcio+" min | Objetivo (<=2): "+(promEspera<=2?"SI":"NO"));
    }

    private void error(int fila, String campo){ JOptionPane.showMessageDialog(this, "Fila "+(fila+1)+": valor inválido en "+campo+" (0<=r<1)", "Error", JOptionPane.ERROR_MESSAGE); }
}

