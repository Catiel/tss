package actividad_5.colas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con simulación de cola (un cajero) usando números aleatorios proporcionados (8 clientes). */
public class PanelColaPredefinida extends JPanel {
    private final DefaultTableModel modeloServicio;
    private final DefaultTableModel modeloLlegadas;
    private final DefaultTableModel modeloSim;
    private final JLabel lblResumen;

    // Números aleatorios proporcionados (8 clientes)
    private static final double[] RAND_LLEGADA = {0.50,0.28,0.68,0.36,0.90,0.62,0.27,0.50};
    private static final double[] RAND_SERVICIO = {0.52,0.37,0.82,0.69,0.98,0.96,0.33,0.50};

    public PanelColaPredefinida(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Cola de banco - ejemplo predefinido (8 clientes)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel de parámetros arriba
        JPanel panelParams = new JPanel();
        panelParams.setLayout(new BoxLayout(panelParams, BoxLayout.Y_AXIS));
        EstilosUI.aplicarEstiloPanel(panelParams);

        modeloServicio = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tServ"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloLlegadas = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","tInter"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDistribuciones();
        panelParams.add(bloque("Distribución tiempo de servicio (min)", modeloServicio, new int[]{55,55,55,55,55}));
        panelParams.add(Box.createVerticalStrut(6));
        panelParams.add(bloque("Distribución intervalos entre llegadas (min)", modeloLlegadas, new int[]{55,55,55,55,55}));

        // Tabla simulación abajo
        modeloSim = new DefaultTableModel(new Object[]{"Cliente","rLleg","IntArr","HoraLleg","rServ","tServ","InicioServ","FinServ","Espera","Ocioso"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        JTable tablaSim = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tablaSim); tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        int[] anchos = {55,55,55,70,55,55,80,80,55,55};
        for(int i=0;i<anchos.length;i++) tablaSim.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]);

        JScrollPane spParams = new JScrollPane(panelParams);
        JScrollPane spSim = new JScrollPane(tablaSim);
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, spParams, spSim);
        split.setResizeWeight(0.45); split.setOneTouchExpandable(true);
        add(split, BorderLayout.CENTER);

        lblResumen = new JLabel(" "); EstilosUI.aplicarEstiloLabel(lblResumen); lblResumen.setBorder(BorderFactory.createEmptyBorder(4,8,4,8));
        add(lblResumen, BorderLayout.SOUTH);

        simular();
    }

    private JPanel bloque(String titulo, DefaultTableModel modelo, int[] anchos){ JTable t = new JTable(modelo); EstilosUI.aplicarEstiloTabla(t); t.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); for(int i=0;i<anchos.length;i++) t.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]); return rotulado(titulo,new JScrollPane(t)); }
    private JPanel rotulado(String titulo, JComponent c){ JPanel p=new JPanel(new BorderLayout()); EstilosUI.aplicarEstiloPanel(p); JLabel l=new JLabel(titulo); l.setFont(l.getFont().deriveFont(Font.BOLD,11f)); p.add(l,BorderLayout.NORTH); p.add(c,BorderLayout.CENTER); return p; }

    private void llenarDistribuciones(){
        // Servicio
        double acum=0, ini=0; modeloServicio.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.SERVICIO_VALORES.length;i++){
            double p = ColaBancoModelo.SERVICIO_PROBS[i];
            acum += p; if(i==ColaBancoModelo.SERVICIO_VALORES.length-1) acum = 1.0; double fin = acum;
            modeloServicio.addRow(new Object[]{UtilFormatoColas.f2(p), UtilFormatoColas.f2(acum), UtilFormatoColas.f2(ini), UtilFormatoColas.f2(fin), ColaBancoModelo.SERVICIO_VALORES[i]});
            ini = fin;
        }
        // Llegadas
        acum=0; ini=0; modeloLlegadas.setRowCount(0);
        for(int i=0;i<ColaBancoModelo.LLEGADA_VALORES.length;i++){
            double p = ColaBancoModelo.LLEGADA_PROBS[i];
            acum += p; if(i==ColaBancoModelo.LLEGADA_VALORES.length-1) acum = 1.0; double fin=acum;
            modeloLlegadas.addRow(new Object[]{UtilFormatoColas.f2(p), UtilFormatoColas.f2(acum), UtilFormatoColas.f2(ini), UtilFormatoColas.f2(fin), ColaBancoModelo.LLEGADA_VALORES[i]});
            ini = fin;
        }
    }

    private void simular(){
        modeloSim.setRowCount(0);
        int relojLlegadaAcumulado = 0; // offset minutos desde 09:00
        int finServicioAnterior = 0; // cuando termina el servicio previo (offset)
        int totalEspera = 0; int totalClientes = RAND_LLEGADA.length; int totalOcioso=0;
        for(int i=0;i<RAND_LLEGADA.length;i++){
            double rLleg = RAND_LLEGADA[i];
            int interArr = ColaBancoModelo.tiempoInterLlegada(rLleg);
            relojLlegadaAcumulado += interArr;
            String horaLleg = UtilFormatoColas.horaDesdeBase(relojLlegadaAcumulado);
            double rServ = RAND_SERVICIO[i];
            int tServ = ColaBancoModelo.tiempoServicio(rServ);
            int inicioServ = Math.max(relojLlegadaAcumulado, finServicioAnterior);
            int espera = inicioServ - relojLlegadaAcumulado;
            int ocio = (inicioServ > finServicioAnterior)? inicioServ - finServicioAnterior : 0;
            int finServ = inicioServ + tServ;
            totalEspera += espera; totalOcioso += ocio;
            modeloSim.addRow(new Object[]{i+1, UtilFormatoColas.f2(rLleg), interArr, horaLleg, UtilFormatoColas.f2(rServ), tServ,
                    UtilFormatoColas.horaDesdeBase(inicioServ), UtilFormatoColas.horaDesdeBase(finServ), espera, ocio});
            finServicioAnterior = finServ;
        }
        double promEspera = totalEspera/(double) totalClientes;
        lblResumen.setText("Promedio de espera = " + UtilFormatoColas.f2(promEspera) + " min | Total ocioso = " + totalOcioso + " min | Cumple objetivo (<=2)? " + (promEspera<=2?"SI":"NO"));
    }
}

