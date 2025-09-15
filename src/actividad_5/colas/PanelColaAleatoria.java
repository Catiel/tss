package actividad_5.colas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que simula la cola con números aleatorios generados por el programa. */
public class PanelColaAleatoria extends JPanel {
    private final JSpinner spClientes; // número de clientes a simular
    private final JButton btnSimular;
    private final DefaultTableModel modeloServ;
    private final DefaultTableModel modeloLleg;
    private final DefaultTableModel modeloSim;
    private final JLabel lblResumen;

    public PanelColaAleatoria(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Cola de banco - simulación aleatoria");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Clientes:"));
        spClientes = new JSpinner(new SpinnerNumberModel(20,1,500,1));
        controles.add(spClientes);
        btnSimular = new JButton("Simular"); EstilosUI.aplicarEstiloBoton(btnSimular); controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Distribuciones
        modeloServ = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tServ"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloLleg = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tInter"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribuciones();
        JPanel panelDist = new JPanel(); panelDist.setLayout(new BoxLayout(panelDist, BoxLayout.Y_AXIS)); EstilosUI.aplicarEstiloPanel(panelDist);
        panelDist.add(bloque("Distribución tiempo servicio", modeloServ, new int[]{55,55,55,55,55})); panelDist.add(Box.createVerticalStrut(6));
        panelDist.add(bloque("Distribución intervalo entre llegadas", modeloLleg, new int[]{55,55,55,55,55}));

        // Simulación
        modeloSim = new DefaultTableModel(new Object[]{"Cliente","rLleg","IntArr","HoraLleg","rServ","tServ","InicioServ","FinServ","Espera","Ocioso"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,75,55,55,80,80,55,55}; for(int i=0;i<anchos.length;i++) tablaSim.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(panelDist), new JScrollPane(tablaSim));
        split.setResizeWeight(0.40); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8)); add(lblResumen, BorderLayout.SOUTH);

        btnSimular.addActionListener(this::simular);
        simular(null); // simulación inicial
    }

    private JPanel bloque(String titulo, DefaultTableModel modelo, int[] anchos){ JTable t = new JTable(modelo); EstilosUI.aplicarEstiloTabla(t); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); for(int i=0;i<anchos.length;i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]); return rotulado(titulo,new JScrollPane(t)); }
    private JPanel rotulado(String titulo, JComponent c){ JPanel p=new JPanel(new BorderLayout()); EstilosUI.aplicarEstiloPanel(p); JLabel l=new JLabel(titulo); l.setFont(l.getFont().deriveFont(Font.BOLD,11f)); p.add(l,BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); return p; }

    private void llenarDistribuciones(){
        double acum=0, ini=0; modeloServ.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.SERVICIO_VALORES.length;i++){ double p=ColaBancoModelo.SERVICIO_PROBS[i]; acum+=p; if(i==ColaBancoModelo.SERVICIO_VALORES.length-1)acum=1.0; double fin=acum; modeloServ.addRow(new Object[]{UtilFormatoColas.f2(p),UtilFormatoColas.f2(acum),UtilFormatoColas.f2(ini),UtilFormatoColas.f2(fin),ColaBancoModelo.SERVICIO_VALORES[i]}); ini=fin; }
        acum=0; ini=0; modeloLleg.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.LLEGADA_VALORES.length;i++){ double p=ColaBancoModelo.LLEGADA_PROBS[i]; acum+=p; if(i==ColaBancoModelo.LLEGADA_VALORES.length-1)acum=1.0; double fin=acum; modeloLleg.addRow(new Object[]{UtilFormatoColas.f2(p),UtilFormatoColas.f2(acum),UtilFormatoColas.f2(ini),UtilFormatoColas.f2(fin),ColaBancoModelo.LLEGADA_VALORES[i]}); ini=fin; }
    }

    private void simular(ActionEvent e){
        int clientes = (int) spClientes.getValue();
        modeloSim.setRowCount(0);
        int relojLleg=0; int finServPrev=0; int totalEspera=0; int totalOcio=0;
        for(int i=1;i<=clientes;i++){
            double rLleg = Math.random(); int interArr = ColaBancoModelo.tiempoInterLlegada(rLleg); relojLleg += interArr; String horaLleg = UtilFormatoColas.horaDesdeBase(relojLleg);
            double rServ = Math.random(); int tServ = ColaBancoModelo.tiempoServicio(rServ);
            int inicioServ = Math.max(relojLleg, finServPrev); int espera = inicioServ - relojLleg; int ocio = (inicioServ>finServPrev)? inicioServ - finServPrev:0; int finServ = inicioServ + tServ;
            totalEspera += espera; totalOcio += ocio; finServPrev = finServ;
            modeloSim.addRow(new Object[]{i, UtilFormatoColas.f2(rLleg), interArr, horaLleg, UtilFormatoColas.f2(rServ), tServ, UtilFormatoColas.horaDesdeBase(inicioServ), UtilFormatoColas.horaDesdeBase(finServ), espera, ocio});
        }
        double promEspera = totalEspera/(double) clientes;
        lblResumen.setText("Promedio espera = "+UtilFormatoColas.f2(promEspera)+" min | Ocioso total = "+totalOcio+" min | Objetivo (<=2): "+(promEspera<=2?"SI":"NO"));
    }
}

