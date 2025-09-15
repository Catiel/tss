package actividad_5.sangre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel que genera números aleatorios para simular varias semanas. */
public class PanelSangreAleatoria extends JPanel {
    private final JSpinner spSemanas; // número de semanas
    private final JSpinner spInvInicial; // inventario inicial
    private final JButton btnSimular; // botón de simulación

    private final DefaultTableModel modeloSupply;
    private final DefaultTableModel modeloPacientes;
    private final DefaultTableModel modeloDemanda;
    private final DefaultTableModel modeloSim;

    public PanelSangreAleatoria(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación aleatoria de plasma");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Semanas:"));
        spSemanas = new JSpinner(new SpinnerNumberModel(6,1,52,1));
        controles.add(spSemanas);
        controles.add(new JLabel("Inventario inicial:"));
        spInvInicial = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
        controles.add(spInvInicial);
        btnSimular = new JButton("Simular");
        EstilosUI.aplicarEstiloBoton(btnSimular);
        controles.add(btnSimular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel de distribuciones derivadas
        JPanel panelDer = new JPanel(new GridLayout(1,3,5,5));
        EstilosUI.aplicarEstiloPanel(panelDer);
        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","#Pac"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDerivadas();
        JTable tSup = new JTable(modeloSupply); EstilosUI.aplicarEstiloTabla(tSup); panelDer.add(new JScrollPane(tSup));
        JTable tPac = new JTable(modeloPacientes); EstilosUI.aplicarEstiloTabla(tPac); panelDer.add(new JScrollPane(tPac));
        JTable tDem = new JTable(modeloDemanda); EstilosUI.aplicarEstiloTabla(tDem); panelDer.add(new JScrollPane(tDem));
        add(panelDer, BorderLayout.WEST);

        modeloSim = new DefaultTableModel(new Object[]{
                "Semana","InvInicial","RandSup","Suministro","TotalDisp","RandPac","Pacientes",
                "RandDem1","Dem1","RandDem2","Dem2","RandDem3","Dem3","RandDem4","Dem4","Restante"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tabla = new JTable(modeloSim); EstilosUI.aplicarEstiloTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnSimular.addActionListener(this::simular);
    }

    private void llenarDerivadas(){
        modeloSupply.setRowCount(0); double acum=0, ini=0; for(int i=0;i<SangreModelo.SUPPLY_VALUES.length;i++){ double p=SangreModelo.SUPPLY_PROBS[i]; acum+=p; if(i==SangreModelo.SUPPLY_VALUES.length-1)acum=1.0; double fin=acum; modeloSupply.addRow(new Object[]{UtilFormatoSangre.fmt2(p),UtilFormatoSangre.fmt2(acum),UtilFormatoSangre.fmt2(ini),UtilFormatoSangre.fmt2(fin),SangreModelo.SUPPLY_VALUES[i]}); ini=fin; }
        modeloPacientes.setRowCount(0); acum=0; ini=0; for(int i=0;i<SangreModelo.PACIENTES_VALUES.length;i++){ double p=SangreModelo.PACIENTES_PROBS[i]; acum+=p; if(i==SangreModelo.PACIENTES_VALUES.length-1)acum=1.0; double fin=acum; modeloPacientes.addRow(new Object[]{UtilFormatoSangre.fmt2(p),UtilFormatoSangre.fmt2(acum),UtilFormatoSangre.fmt2(ini),UtilFormatoSangre.fmt2(fin),SangreModelo.PACIENTES_VALUES[i]}); ini=fin; }
        modeloDemanda.setRowCount(0); acum=0; ini=0; for(int i=0;i<SangreModelo.DEMANDA_VALUES.length;i++){ double p=SangreModelo.DEMANDA_PROBS[i]; acum+=p; if(i==SangreModelo.DEMANDA_VALUES.length-1)acum=1.0; double fin=acum; modeloDemanda.addRow(new Object[]{UtilFormatoSangre.fmt2(p),UtilFormatoSangre.fmt2(acum),UtilFormatoSangre.fmt2(ini),UtilFormatoSangre.fmt2(fin),SangreModelo.DEMANDA_VALUES[i]}); ini=fin; }
    }

    private double rand(){ return Math.random(); }

    private void simular(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        int inv = (int) spInvInicial.getValue();
        modeloSim.setRowCount(0);
        for(int sem=1; sem<=semanas; sem++){
            double rSup = rand(); int suministro = SangreModelo.suministro(rSup); int total = inv + suministro;
            double rPac = rand(); int pacientes = SangreModelo.pacientes(rPac);
            String[] rDem = {"","","",""}; String[] dem = {"","","",""}; int restante = total;
            for(int p=0;p<pacientes && p<4;p++){
                double r = rand(); int d = SangreModelo.demandaPaciente(r); restante -= d; if(restante<0) restante = 0; rDem[p]=UtilFormatoSangre.fmt2(r); dem[p]=String.valueOf(d);
            }
            modeloSim.addRow(new Object[]{sem, inv, UtilFormatoSangre.fmt2(rSup), suministro, total, UtilFormatoSangre.fmt2(rPac), pacientes,
                    rDem[0], dem[0], rDem[1], dem[1], rDem[2], dem[2], rDem[3], dem[3], restante});
            inv = restante;
        }
    }
}

