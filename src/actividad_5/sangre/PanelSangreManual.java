package actividad_5.sangre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/** Panel para ingresar manualmente números aleatorios y ejecutar la simulación. */
public class PanelSangreManual extends JPanel {
    private final JSpinner spSemanas; // número de semanas
    private final JSpinner spInvInicial; // inventario inicial
    private final JButton btnCrear; // crea filas
    private final JButton btnCalcular; // calcula resultados
    private final DefaultTableModel modeloSupply;
    private final DefaultTableModel modeloPacientes;
    private final DefaultTableModel modeloDemanda;
    private final DefaultTableModel modeloInput; // tabla de entrada de números aleatorios
    private final DefaultTableModel modeloSim; // tabla de simulación final

    public PanelSangreManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación manual (ingrese números aleatorios en [0,1))");
        EstilosUI.aplicarEstiloTitulo(titulo);
        add(titulo, BorderLayout.NORTH);

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(controles);
        controles.add(new JLabel("Semanas:"));
        spSemanas = new JSpinner(new SpinnerNumberModel(6,1,20,1));
        controles.add(spSemanas);
        controles.add(new JLabel("Inventario inicial:"));
        spInvInicial = new JSpinner(new SpinnerNumberModel(0,0,1000,1));
        controles.add(spInvInicial);
        btnCrear = new JButton("Crear tabla");
        EstilosUI.aplicarEstiloBoton(btnCrear);
        controles.add(btnCrear);
        btnCalcular = new JButton("Calcular");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(5,5));
        EstilosUI.aplicarEstiloPanel(panelPrincipal);

        // Panel izquierdo con distribuciones
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

        panelPrincipal.add(panelIzq, BorderLayout.WEST);

        // Panel central con tabla de entrada y simulación
        JPanel panelCentro = new JPanel(new GridLayout(2,1,5,5));
        EstilosUI.aplicarEstiloPanel(panelCentro);

        // Tabla de entrada de números aleatorios (editable)
        modeloInput = new DefaultTableModel(new Object[]{
                "Semana","#Alea.Suministro","#Alea.Pacientes"},0){
            @Override public boolean isCellEditable(int r,int c){
                // Solo columnas de suministro y pacientes son editables (1-2)
                return c == 1 || c == 2;
            }
            @Override public Class<?> getColumnClass(int c){
                return c == 0 ? Integer.class : String.class;
            }
        };
        JTable tablaInput = new JTable(modeloInput);
        EstilosUI.aplicarEstiloTabla(tablaInput);
        tablaInput.getTableHeader().setBackground(new Color(255, 255, 200));
        JScrollPane scrollInput = new JScrollPane(tablaInput);
        scrollInput.setPreferredSize(new Dimension(600, 200));
        scrollInput.setBorder(BorderFactory.createTitledBorder("1. Ingrese números aleatorios [0,1)"));
        panelCentro.add(scrollInput);

        // Tabla de simulación final (solo lectura)
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
        JScrollPane scrollSim = new JScrollPane(tablaSim);
        scrollSim.setPreferredSize(new Dimension(900, 300));
        scrollSim.setBorder(BorderFactory.createTitledBorder("2. Resultados de la simulación"));
        panelCentro.add(scrollSim);

        panelPrincipal.add(panelCentro, BorderLayout.CENTER);
        add(panelPrincipal, BorderLayout.CENTER);

        btnCrear.addActionListener(this::crearTabla);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);

        JTextArea ayuda = new JTextArea("PASOS:\n" +
            "1. Configure semanas e inventario inicial\n" +
            "2. Presione 'Crear tabla' para generar filas\n" +
            "3. Ingrese números aleatorios (0 ≤ r < 1) para SUMINISTRO y PACIENTES únicamente\n" +
            "4. Presione 'Calcular' para obtener los resultados\n" +
            "NOTA: Los números aleatorios para la demanda de cada paciente se generan automáticamente.");
        ayuda.setWrapStyleWord(true);
        ayuda.setLineWrap(true);
        ayuda.setEditable(false);
        ayuda.setBackground(getBackground());
        ayuda.setFont(new Font("Arial", Font.PLAIN, 11));
        add(ayuda, BorderLayout.SOUTH);
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

    private void crearTabla(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        modeloInput.setRowCount(0);
        modeloSim.setRowCount(0);

        for(int s = 1; s <= semanas; s++){
            modeloInput.addRow(new Object[]{
                s,          // Semana
                "",         // #Alea.Suministro
                ""          // #Alea.Pacientes
            });
        }
        btnCalcular.setEnabled(true);
    }

    private Double parseRand(Object obj){
        if(obj == null) return null;
        String t = obj.toString().trim();
        if(t.isEmpty()) return null;
        try{
            double v = Double.parseDouble(t.replace(',','.'));
            if(v < 0 || v >= 1){
                return null;
            }
            return v;
        }catch(Exception ex){
            return null;
        }
    }

    private void calcular(ActionEvent e){
        int inventario = (int) spInvInicial.getValue();
        modeloSim.setRowCount(0);

        for(int r = 0; r < modeloInput.getRowCount(); r++){
            int semana = r + 1;

            // VALIDAR Y OBTENER SUMINISTRO
            Double rSup = parseRand(modeloInput.getValueAt(r, 1));
            if(rSup == null){
                mensajeError(semana, "Suministro");
                return;
            }
            int suministro = SangreModelo.suministro(rSup);
            int sangreTotal = inventario + suministro;

            // VALIDAR Y OBTENER PACIENTES
            Double rPac = parseRand(modeloInput.getValueAt(r, 2));
            if(rPac == null){
                mensajeError(semana, "Pacientes");
                return;
            }
            int numPacientes = SangreModelo.pacientes(rPac);

            int sangreRestante = sangreTotal;

            // Si no hay pacientes, agregar una sola fila
            if(numPacientes == 0) {
                modeloSim.addRow(new Object[]{
                    semana,                            // Semana
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
                // Procesar cada paciente individual (NÚMEROS ALEATORIOS AUTOMÁTICOS)
                for(int p = 0; p < numPacientes && p < 4; p++){
                    // GENERAR AUTOMÁTICAMENTE el número aleatorio para la demanda
                    double rDem = Math.random(); // AUTOMÁTICO
                    int demanda = SangreModelo.demandaPaciente(rDem);

                    sangreRestante -= demanda;
                    if(sangreRestante < 0) sangreRestante = 0;

                    // Solo mostrar datos de semana, suministro y pacientes en la primera fila
                    Object colSemana = (p == 0) ? semana : "";
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
                        UtilFormatoSangre.fmt2(rDem),     // #Aleatorio demanda (GENERADO AUTOMÁTICAMENTE)
                        demanda,                           // Pintas demanda
                        sangreRestante                     // Pintas restantes (actualizado progresivamente)
                    });
                }
            }

            inventario = sangreRestante; // Inventario para siguiente semana
        }

        JOptionPane.showMessageDialog(this,
            "Simulación completada exitosamente!\n" +
            "Los números aleatorios para la demanda de pacientes se generaron automáticamente.",
            "Cálculo terminado",
            JOptionPane.INFORMATION_MESSAGE);
    }

    private void mensajeError(int semana, String campo){
        JOptionPane.showMessageDialog(this,
            "Semana " + semana + ": número aleatorio inválido para " + campo +
            "\n(debe estar en [0,1) y no estar vacío)",
            "Dato inválido",
            JOptionPane.ERROR_MESSAGE);
    }
}