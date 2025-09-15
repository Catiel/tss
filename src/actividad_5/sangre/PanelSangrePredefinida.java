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

    // Números aleatorios del ejemplo por semana
    private final double[] RAND_SUPPLY = {0.59,0.22,0.39,0.06,0.85,0.08};
    private final double[] RAND_PACIENTES = {0.27,0.51,0.67,0.91,0.56,0.27};
    // Matriz de números de demanda por paciente por semana (max 4)
    private final double[][] RAND_DEM = {
            {0.79, Double.NaN, Double.NaN, Double.NaN}, // semana1: 1 paciente
            {0.42,0.30,Double.NaN,Double.NaN}, // semana2: 2 pacientes
            {0.71,0.36,Double.NaN,Double.NaN}, // semana3: 2 pacientes
            {0.72,0.86,0.33,Double.NaN}, // semana4: 3 pacientes
            {0.63,0.93,Double.NaN,Double.NaN}, // semana5: 2 pacientes
            {0.60,Double.NaN,Double.NaN,Double.NaN} // semana6: 1 paciente
    };

    public PanelSangrePredefinida(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);

        JLabel titulo = new JLabel("Simulación de plasma (números proporcionados del ejemplo)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Panel izquierdo con todas las tablas de distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(4,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        // 1. Tabla combinada original (solo para referencia)
        modeloDistribuciones = new DefaultTableModel(new Object[]{
                "Pintas/Entrega", "Probabilidad", "Pacientes/Semana", "Probabilidad", "Pintas/Paciente", "Probabilidad"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        llenarTablaCombinada();
        JTable tablaComb = new JTable(modeloDistribuciones);
        EstilosUI.aplicarEstiloTabla(tablaComb);
        panelIzq.add(new JScrollPane(tablaComb));

        // 2. Tabla de suministro con rangos
        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tSup = new JTable(modeloSupply);
        EstilosUI.aplicarEstiloTabla(tSup);
        tSup.getTableHeader().setBackground(new Color(200, 255, 200));
        panelIzq.add(new JScrollPane(tSup));

        // 3. Tabla de pacientes con rangos
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","#Pac"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tPac = new JTable(modeloPacientes);
        EstilosUI.aplicarEstiloTabla(tPac);
        tPac.getTableHeader().setBackground(new Color(200, 220, 255));
        panelIzq.add(new JScrollPane(tPac));

        // 4. Tabla de demanda por paciente con rangos
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        JTable tDem = new JTable(modeloDemanda);
        EstilosUI.aplicarEstiloTabla(tDem);
        tDem.getTableHeader().setBackground(new Color(255, 200, 255));
        panelIzq.add(new JScrollPane(tDem));

        llenarDerivadas();
        add(panelIzq, BorderLayout.WEST);

        // Tabla de simulación principal (lado derecho)
        modeloSim = new DefaultTableModel(new Object[]{
                "Semana","Inventario Inicial","#Aleatorio","Pintas","Sangre Disponible Total",
                "#Aleatorio","#Pacientes","Nro de Paciente","#Aleatorio","Pintas","#Pintas Restantes"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){
                if(c == 0 || c == 1 || c == 3 || c == 4 || c == 6 || c == 7 || c == 9 || c == 10) return Integer.class;
                return String.class;
            }
        };
        JTable tablaSim = new JTable(modeloSim);
        EstilosUI.aplicarEstiloTabla(tablaSim);

        JScrollPane spSim = new JScrollPane(tablaSim);
        spSim.setPreferredSize(new Dimension(900, 400));
        add(spSim, BorderLayout.CENTER);

        simular();

        JTextArea descripcion = new JTextArea("Simulación con datos del ejemplo: 6 semanas con inventario inicial 0. " +
            "Los números aleatorios están predefinidos según el ejercicio. Cada paciente tiene su propia fila.");
        descripcion.setWrapStyleWord(true);
        descripcion.setLineWrap(true);
        descripcion.setEditable(false);
        descripcion.setBackground(getBackground());
        add(descripcion, BorderLayout.SOUTH);
    }

    private void llenarTablaCombinada(){
        modeloDistribuciones.setRowCount(0);
        int filas = Math.max(SangreModelo.SUPPLY_VALUES.length,
                    Math.max(SangreModelo.PACIENTES_VALUES.length, SangreModelo.DEMANDA_VALUES.length));

        for(int i = 0; i < filas; i++){
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
        // Tabla de suministro con rangos acumulados
        double acum = 0;
        double ini = 0;
        for(int i = 0; i < SangreModelo.SUPPLY_VALUES.length; i++){
            double p = SangreModelo.SUPPLY_PROBS[i];
            acum += p;
            if(i == SangreModelo.SUPPLY_VALUES.length-1) acum = 1.0;
            double fin = acum;
            modeloSupply.addRow(new Object[]{
                UtilFormatoSangre.fmt2(p),
                UtilFormatoSangre.fmt2(acum),
                UtilFormatoSangre.fmt2(ini),
                UtilFormatoSangre.fmt2(fin),
                SangreModelo.SUPPLY_VALUES[i]
            });
            ini = fin;
        }

        // Tabla de pacientes con rangos acumulados
        acum = 0;
        ini = 0;
        for(int i = 0; i < SangreModelo.PACIENTES_VALUES.length; i++){
            double p = SangreModelo.PACIENTES_PROBS[i];
            acum += p;
            if(i == SangreModelo.PACIENTES_VALUES.length-1) acum = 1.0;
            double fin = acum;
            modeloPacientes.addRow(new Object[]{
                UtilFormatoSangre.fmt2(p),
                UtilFormatoSangre.fmt2(acum),
                UtilFormatoSangre.fmt2(ini),
                UtilFormatoSangre.fmt2(fin),
                SangreModelo.PACIENTES_VALUES[i]
            });
            ini = fin;
        }

        // Tabla de demanda por paciente con rangos acumulados
        acum = 0;
        ini = 0;
        for(int i = 0; i < SangreModelo.DEMANDA_VALUES.length; i++){
            double p = SangreModelo.DEMANDA_PROBS[i];
            acum += p;
            if(i == SangreModelo.DEMANDA_VALUES.length-1) acum = 1.0;
            double fin = acum;
            modeloDemanda.addRow(new Object[]{
                UtilFormatoSangre.fmt2(p),
                UtilFormatoSangre.fmt2(acum),
                UtilFormatoSangre.fmt2(ini),
                UtilFormatoSangre.fmt2(fin),
                SangreModelo.DEMANDA_VALUES[i]
            });
            ini = fin;
        }
    }

    private void simular(){
        modeloSim.setRowCount(0);
        int inventario = 0; // inventario inicial semana 1

        for(int semana = 0; semana < RAND_SUPPLY.length; semana++){
            // Calcular suministro de la semana
            double rSup = RAND_SUPPLY[semana];
            int suministro = SangreModelo.suministro(rSup);
            int sangreTotal = inventario + suministro;

            // Calcular número de pacientes de la semana
            double rPac = RAND_PACIENTES[semana];
            int numPacientes = SangreModelo.pacientes(rPac);

            int sangreRestante = sangreTotal;

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) {
                modeloSim.addRow(new Object[]{
                    semana + 1,                        // Semana
                    inventario,                        // Inventario inicial
                    UtilFormatoSangre.fmt2(rSup),     // #Aleatorio suministro
                    suministro,                        // Pintas suministradas
                    sangreTotal,                       // Sangre disponible total
                    UtilFormatoSangre.fmt2(rPac),     // #Aleatorio pacientes
                    numPacientes,                      // Número de pacientes
                    "",                                // Nro de paciente (vacío si no hay)
                    "",                                // #Aleatorio demanda (vacío si no hay)
                    "",                                // Pintas demanda (vacío si no hay)
                    sangreRestante                     // Pintas restantes
                });
            } else {
                // Agregar una fila por cada paciente
                for(int p = 0; p < numPacientes && p < 4; p++){
                    double rDem = RAND_DEM[semana][p];
                    if(Double.isNaN(rDem)) break;

                    int demanda = SangreModelo.demandaPaciente(rDem);
                    sangreRestante -= demanda;
                    if(sangreRestante < 0) sangreRestante = 0;

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? (semana + 1) : "";
                    Object colInventario = (p == 0) ? inventario : "";
                    Object colRandSup = (p == 0) ? UtilFormatoSangre.fmt2(rSup) : "";
                    Object colSuministro = (p == 0) ? suministro : "";
                    Object colSangreTotal = (p == 0) ? sangreTotal : "";
                    Object colRandPac = (p == 0) ? UtilFormatoSangre.fmt2(rPac) : "";
                    Object colNumPac = (p == 0) ? numPacientes : "";

                    modeloSim.addRow(new Object[]{
                        colSemana,                         // Semana (solo en primera fila del grupo)
                        colInventario,                     // Inventario inicial (solo en primera fila)
                        colRandSup,                        // #Aleatorio suministro (solo en primera fila)
                        colSuministro,                     // Pintas suministradas (solo en primera fila)
                        colSangreTotal,                    // Sangre disponible total (solo en primera fila)
                        colRandPac,                        // #Aleatorio pacientes (solo en primera fila)
                        colNumPac,                         // Número de pacientes (solo en primera fila)
                        p + 1,                             // Nro de paciente
                        UtilFormatoSangre.fmt2(rDem),     // #Aleatorio demanda
                        demanda,                           // Pintas demanda
                        sangreRestante                     // Pintas restantes (actualizado progresivamente)
                    });
                }
            }

            inventario = sangreRestante; // Inventario para siguiente semana
        }
    }
}