package actividad_5.sangre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/** Panel con números aleatorios proporcionados por el ejemplo (6 semanas). */
public class PanelSangrePredefinida extends JPanel {
    private final DefaultTableModel modeloDistribuciones; // tabla combinada inicial (tres secciones)
    private final DefaultTableModel modeloSupply; // tabla derivada suministro
    private final DefaultTableModel modeloPacientes; // tabla derivada pacientes
    private final DefaultTableModel modeloDemanda; // tabla derivada demanda por paciente
    private final DefaultTableModel modeloSim; // tabla de simulación semanal

    // Números aleatorios (ejemplo) por semana
    private final double[] RAND_SUPPLY = {0.59,0.22,0.39,0.06,0.85,0.08};
    private final double[] RAND_PACIENTES = {0.27,0.51,0.67,0.91,0.56,0.27};
    // Matriz de números de demanda por paciente por semana (max 4) - usar Double.NaN para "no existe"
    private final double[][] RAND_DEM = {
            {0.79, Double.NaN, Double.NaN, Double.NaN}, // semana1 1 paciente
            {0.42,0.30,Double.NaN,Double.NaN}, // semana2 2 pacientes
            {0.71,0.36,Double.NaN,Double.NaN}, // semana3 2 pacientes
            {0.72,0.86,0.33,Double.NaN}, // semana4 3 pacientes
            {0.63,0.93,Double.NaN,Double.NaN}, // semana5 2 pacientes
            {0.60,Double.NaN,Double.NaN,Double.NaN} // semana6 1 paciente
    };

    public PanelSangrePredefinida(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación de plasma (números proporcionados)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel superior con tablas de distribuciones
        JPanel panelTop = new JPanel(new GridLayout(2,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelTop);

        // Tabla combinada (solo para mostrar origen)
        modeloDistribuciones = new DefaultTableModel(new Object[]{
                "Pintas/Prob (suministro)", "Prob", "Pacientes/sem", "Prob Pac", "Pintas x paciente", "Prob Dem"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        llenarTablaCombinada();
        JTable tablaComb = new JTable(modeloDistribuciones);
        EstilosUI.aplicarEstiloTabla(tablaComb);
        panelTop.add(new JScrollPane(tablaComb));

        // Panel con las tres tablas derivadas
        JPanel panelDerivadas = new JPanel(new GridLayout(1,3,5,5));
        EstilosUI.aplicarEstiloPanel(panelDerivadas);

        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","RangoIni","RangoFin","Pintas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","RangoIni","RangoFin","#Pac"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","RangoIni","RangoFin","Pintas"},0){@Override public boolean isCellEditable(int r,int c){return false;}};
        llenarDerivadas();
        JTable tSup = new JTable(modeloSupply); EstilosUI.aplicarEstiloTabla(tSup);
        JTable tPac = new JTable(modeloPacientes); EstilosUI.aplicarEstiloTabla(tPac);
        JTable tDem = new JTable(modeloDemanda); EstilosUI.aplicarEstiloTabla(tDem);
        panelDerivadas.add(new JScrollPane(tSup));
        panelDerivadas.add(new JScrollPane(tPac));
        panelDerivadas.add(new JScrollPane(tDem));

        panelTop.add(panelDerivadas);
        add(panelTop, BorderLayout.WEST);

        // Tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{
                "Semana","InvInicial","RandSup","Suministro","TotalDisp","RandPac","Pacientes",
                "RandDem1","Dem1","RandDem2","Dem2","RandDem3","Dem3","RandDem4","Dem4","Restante"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){ return (c==0)?Integer.class:String.class; }
        };
        JTable tablaSim = new JTable(modeloSim);
        EstilosUI.aplicarEstiloTabla(tablaSim);
        JScrollPane spSim = new JScrollPane(tablaSim);
        add(spSim, BorderLayout.CENTER);

        simular();

        JTextArea descripcion = new JTextArea("Una clínica rural recibe semanalmente un suministro variable. Se simulan 6 semanas con inventario inicial 0.");
        descripcion.setWrapStyleWord(true);
        descripcion.setLineWrap(true);
        descripcion.setEditable(false);
        descripcion.setBackground(getBackground());
        add(descripcion, BorderLayout.SOUTH);
    }

    private void llenarTablaCombinada(){
        modeloDistribuciones.setRowCount(0);
        int filas = Math.max(SangreModelo.SUPPLY_VALUES.length, Math.max(SangreModelo.PACIENTES_VALUES.length, SangreModelo.DEMANDA_VALUES.length));
        for(int i=0;i<filas;i++){
            String sVal = i < SangreModelo.SUPPLY_VALUES.length ? String.valueOf(SangreModelo.SUPPLY_VALUES[i]) : "";
            String sProb = i < SangreModelo.SUPPLY_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.SUPPLY_PROBS[i]) : "";
            String pVal = i < SangreModelo.PACIENTES_VALUES.length ? String.valueOf(SangreModelo.PACIENTES_VALUES[i]) : "";
            String pProb = i < SangreModelo.PACIENTES_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.PACIENTES_PROBS[i]) : "";
            String dVal = i < SangreModelo.DEMANDA_VALUES.length ? String.valueOf(SangreModelo.DEMANDA_VALUES[i]) : "";
            String dProb = i < SangreModelo.DEMANDA_PROBS.length ? UtilFormatoSangre.fmt2(SangreModelo.DEMANDA_PROBS[i]) : "";
            modeloDistribuciones.addRow(new Object[]{sVal,sProb,pVal,pProb,dVal,dProb});
        }
    }

    private void llenarDerivadas(){
        // supply
        double acum=0; double ini=0;
        for(int i=0;i<SangreModelo.SUPPLY_VALUES.length;i++){
            double p = SangreModelo.SUPPLY_PROBS[i];
            acum += p; if(i==SangreModelo.SUPPLY_VALUES.length-1) acum = 1.0;
            double fin = acum;
            modeloSupply.addRow(new Object[]{UtilFormatoSangre.fmt2(p), UtilFormatoSangre.fmt2(acum), UtilFormatoSangre.fmt2(ini), UtilFormatoSangre.fmt2(fin), SangreModelo.SUPPLY_VALUES[i]});
            ini = fin;
        }
        // pacientes
        acum=0; ini=0;
        for(int i=0;i<SangreModelo.PACIENTES_VALUES.length;i++){
            double p = SangreModelo.PACIENTES_PROBS[i];
            acum += p; if(i==SangreModelo.PACIENTES_VALUES.length-1) acum = 1.0; double fin = acum;
            modeloPacientes.addRow(new Object[]{UtilFormatoSangre.fmt2(p), UtilFormatoSangre.fmt2(acum), UtilFormatoSangre.fmt2(ini), UtilFormatoSangre.fmt2(fin), SangreModelo.PACIENTES_VALUES[i]});
            ini = fin;
        }
        // demanda por paciente
        acum=0; ini=0;
        for(int i=0;i<SangreModelo.DEMANDA_VALUES.length;i++){
            double p = SangreModelo.DEMANDA_PROBS[i];
            acum += p; if(i==SangreModelo.DEMANDA_VALUES.length-1) acum = 1.0; double fin = acum;
            modeloDemanda.addRow(new Object[]{UtilFormatoSangre.fmt2(p), UtilFormatoSangre.fmt2(acum), UtilFormatoSangre.fmt2(ini), UtilFormatoSangre.fmt2(fin), SangreModelo.DEMANDA_VALUES[i]});
            ini = fin;
        }
    }

    private void simular(){
        modeloSim.setRowCount(0);
        int inv = 0; // inventario inicial semana 1
        for(int semana=0; semana< RAND_SUPPLY.length; semana++){
            double rSup = RAND_SUPPLY[semana];
            int suministro = SangreModelo.suministro(rSup);
            int total = inv + suministro;
            double rPac = RAND_PACIENTES[semana];
            int pacientes = SangreModelo.pacientes(rPac);
            String[] randDemStr = {"","","",""};
            String[] demStr = {"","","",""};
            int restante = total;
            for(int p=0;p<pacientes && p<4;p++){
                double rDem = RAND_DEM[semana][p];
                if(Double.isNaN(rDem)) break; // seguridad
                int dem = SangreModelo.demandaPaciente(rDem);
                restante -= dem;
                randDemStr[p] = UtilFormatoSangre.fmt2(rDem);
                demStr[p] = String.valueOf(dem);
            }
            if(restante < 0) restante = 0; // no permitir negativo en inventario almacenado
            modeloSim.addRow(new Object[]{semana+1, inv, UtilFormatoSangre.fmt2(rSup), suministro, total, UtilFormatoSangre.fmt2(rPac), pacientes,
                    randDemStr[0], demStr[0], randDemStr[1], demStr[1], randDemStr[2], demStr[2], randDemStr[3], demStr[3], restante});
            inv = restante; // inventario para siguiente semana
        }
    }
}

