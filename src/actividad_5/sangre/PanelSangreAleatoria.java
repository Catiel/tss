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
        JLabel titulo = new JLabel("Simulación aleatoria de plasma (números generados automáticamente)");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
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

        // Panel izquierdo con tablas de distribuciones
        JPanel panelIzq = new JPanel(new GridLayout(3,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelIzq);

        modeloSupply = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        modeloPacientes = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","#Pac"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        modeloDemanda = new DefaultTableModel(new Object[]{"Prob","Acum","Ini","Fin","Pintas"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };

        llenarDerivadas();

        JTable tSup = new JTable(modeloSupply);
        EstilosUI.aplicarEstiloTabla(tSup);
        tSup.getTableHeader().setBackground(new Color(200, 255, 200));
        panelIzq.add(new JScrollPane(tSup));

        JTable tPac = new JTable(modeloPacientes);
        EstilosUI.aplicarEstiloTabla(tPac);
        tPac.getTableHeader().setBackground(new Color(200, 220, 255));
        panelIzq.add(new JScrollPane(tPac));

        JTable tDem = new JTable(modeloDemanda);
        EstilosUI.aplicarEstiloTabla(tDem);
        tDem.getTableHeader().setBackground(new Color(255, 200, 255));
        panelIzq.add(new JScrollPane(tDem));

        add(panelIzq, BorderLayout.WEST);

        // Tabla de simulación principal
        modeloSim = new DefaultTableModel(new Object[]{
                "Semana","Inventario Inicial","#Aleatorio","Pintas","Sangre Disponible Total",
                "#Aleatorio","#Pacientes","Nro de Paciente","#Aleatorio","Pintas","#Pintas Restantes"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){
                if(c == 0 || c == 1 || c == 3 || c == 4 || c == 6 || c == 7 || c == 9 || c == 10) return Integer.class;
                return String.class;
            }
        };
        JTable tabla = new JTable(modeloSim);
        EstilosUI.aplicarEstiloTabla(tabla);
        JScrollPane scrollSim = new JScrollPane(tabla);
        scrollSim.setPreferredSize(new Dimension(900, 400));
        add(scrollSim, BorderLayout.CENTER);

        btnSimular.addActionListener(this::simular);

        JTextArea descripcion = new JTextArea("Configure el número de semanas e inventario inicial, luego presione 'Simular'. " +
            "Los números aleatorios se generan automáticamente usando Math.random(). Cada paciente tiene su propia fila.");
        descripcion.setWrapStyleWord(true);
        descripcion.setLineWrap(true);
        descripcion.setEditable(false);
        descripcion.setBackground(getBackground());
        add(descripcion, BorderLayout.SOUTH);
    }

    private void llenarDerivadas(){
        // Tabla de suministro con rangos acumulados
        modeloSupply.setRowCount(0);
        double acum = 0, ini = 0;
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
        modeloPacientes.setRowCount(0);
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
        modeloDemanda.setRowCount(0);
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

    private double rand(){
        return Math.random();
    }

    private void simular(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        int inventario = (int) spInvInicial.getValue();
        modeloSim.setRowCount(0);

        for(int sem = 1; sem <= semanas; sem++){
            // Calcular suministro de la semana (ALEATORIO)
            double rSup = rand();
            int suministro = SangreModelo.suministro(rSup);
            int sangreTotal = inventario + suministro;

            // Calcular número de pacientes de la semana (ALEATORIO)
            double rPac = rand();
            int numPacientes = SangreModelo.pacientes(rPac);

            int sangreRestante = sangreTotal;

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) {
                modeloSim.addRow(new Object[]{
                    sem,                               // Semana
                    inventario,                        // Inventario inicial
                    UtilFormatoSangre.fmt2(rSup),     // #Aleatorio suministro
                    suministro,                        // Pintas suministradas
                    sangreTotal,                       // Sangre disponible total
                    UtilFormatoSangre.fmt2(rPac),     // #Aleatorio pacientes
                    numPacientes,                      // Número de pacientes (0)
                    "",                                // Nro de paciente (vacío)
                    "",                                // #Aleatorio demanda (vacío)
                    "",                                // Pintas demanda (vacío)
                    sangreRestante                     // Pintas restantes
                });
            } else {
                // Agregar una fila por cada paciente
                for(int p = 0; p < numPacientes && p < 4; p++){
                    // Generar número aleatorio para la demanda de este paciente
                    double rDem = rand();
                    int demanda = SangreModelo.demandaPaciente(rDem);

                    sangreRestante -= demanda;
                    if(sangreRestante < 0) sangreRestante = 0;

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? sem : "";
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
                        p + 1,                             // Nro de paciente (1, 2, 3, 4)
                        UtilFormatoSangre.fmt2(rDem),     // #Aleatorio demanda (GENERADO)
                        demanda,                           // Pintas demanda (CALCULADO)
                        sangreRestante                     // Pintas restantes (actualizado progresivamente)
                    });
                }
            }

            inventario = sangreRestante; // Inventario para siguiente semana
        }
    }
}