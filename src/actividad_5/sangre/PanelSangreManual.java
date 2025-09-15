package actividad_5.sangre;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para ingresar manualmente números aleatorios y ejecutar la simulación. */
public class PanelSangreManual extends JPanel {
    private final JSpinner spSemanas; // número de semanas
    private final JSpinner spInvInicial; // inventario inicial
    private final JButton btnCrear; // crea filas
    private final JButton btnCalcular; // calcula resultados
    private final DefaultTableModel modelo; // modelo de la tabla

    public PanelSangreManual(){
        setLayout(new BorderLayout(8,8));
        EstilosUI.aplicarEstiloPanel(this);
        JLabel titulo = new JLabel("Simulación manual (ingrese números aleatorios en [0,1))");
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
        btnCrear = new JButton("Crear filas"); EstilosUI.aplicarEstiloBoton(btnCrear); controles.add(btnCrear);
        btnCalcular = new JButton("Calcular"); EstilosUI.aplicarEstiloBoton(btnCalcular); controles.add(btnCalcular);
        add(controles, BorderLayout.BEFORE_FIRST_LINE);

        modelo = new DefaultTableModel(new Object[]{
                "Semana","InvInicial","RandSup","Suministro","TotalDisp","RandPac","Pacientes",
                "RandDem1","Dem1","RandDem2","Dem2","RandDem3","Dem3","RandDem4","Dem4","Restante"},0){
            @Override public boolean isCellEditable(int r,int c){
                // Editable solo las columnas de números aleatorios: RandSup(2), RandPac(5), RandDem1(7),9,11,13
                return c==2 || c==5 || c==7 || c==9 || c==11 || c==13;
            }
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tabla = new JTable(modelo); EstilosUI.aplicarEstiloTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnCalcular.setEnabled(false);

        JTextArea ayuda = new JTextArea("Ingrese los números aleatorios (0<=r<1). Deje en blanco los RandDem# que no apliquen (según # de pacientes).\nSe calcularán suministros, pacientes, demandas y pintas restantes.");
        ayuda.setWrapStyleWord(true); ayuda.setLineWrap(true); ayuda.setEditable(false); ayuda.setBackground(getBackground());
        add(ayuda, BorderLayout.SOUTH);
    }

    private void crearFilas(ActionEvent e){
        int semanas = (int) spSemanas.getValue();
        int inv = (int) spInvInicial.getValue();
        modelo.setRowCount(0);
        for(int s=1; s<=semanas; s++){
            modelo.addRow(new Object[]{s, (s==1?inv:""), "", "", "", "", "", "", "", "", "", "", "", "", "", ""});
        }
        btnCalcular.setEnabled(true);
    }

    private Double parseRand(Object obj){
        if(obj==null) return null;
        String t = obj.toString().trim();
        if(t.isEmpty()) return null;
        try{ double v = Double.parseDouble(t.replace(',','.')); if(v<0 || v>=1){ return null;} return v; }catch(Exception ex){ return null; }
    }

    private void calcular(ActionEvent e){
        int inv = 0; // se reemplazará por valor inicial de la primera fila si se proporcionó
        if(modelo.getRowCount()==0) return;
        // Inventario inicial primera fila
        Object invObj = modelo.getValueAt(0,1);
        try{ inv = Integer.parseInt(invObj==null||invObj.toString().trim().isEmpty()?"0":invObj.toString().trim()); }catch(Exception ex){ inv=0; }

        for(int r=0; r<modelo.getRowCount(); r++){
            // Semana y asignación de inventario inicial
            modelo.setValueAt(inv, r, 1);
            // Suministro
            Double rSup = parseRand(modelo.getValueAt(r,2));
            if(rSup==null){ mensajeError(r,"RandSup"); return; }
            int suministro = SangreModelo.suministro(rSup);
            int total = inv + suministro;
            modelo.setValueAt(String.valueOf(suministro), r, 3);
            modelo.setValueAt(String.valueOf(total), r, 4);
            modelo.setValueAt(UtilFormatoSangre.fmt2(rSup), r, 2);

            // Pacientes
            Double rPac = parseRand(modelo.getValueAt(r,5));
            if(rPac==null){ mensajeError(r,"RandPac"); return; }
            int pacientes = SangreModelo.pacientes(rPac);
            modelo.setValueAt(UtilFormatoSangre.fmt2(rPac), r, 5);
            modelo.setValueAt(String.valueOf(pacientes), r, 6);

            int restante = total;
            // Demandas por paciente
            int colBaseRand = 7; // RandDem1
            for(int p=0; p<4; p++){
                int colRand = colBaseRand + p*2; // 7,9,11,13
                int colDem = colRand + 1; // 8,10,12,14
                String randTxt = ""; String demTxt="";
                if(p < pacientes){
                    Double rDem = parseRand(modelo.getValueAt(r,colRand));
                    if(rDem==null){ mensajeError(r,"RandDem"+(p+1)); return; }
                    int dem = SangreModelo.demandaPaciente(rDem);
                    restante -= dem; if(restante<0) restante = 0;
                    randTxt = UtilFormatoSangre.fmt2(rDem);
                    demTxt = String.valueOf(dem);
                }
                modelo.setValueAt(randTxt, r, colRand);
                modelo.setValueAt(demTxt, r, colDem);
            }
            modelo.setValueAt(restante, r, 15); // restante
            inv = restante; // inventario para siguiente semana
        }
    }

    private void mensajeError(int fila, String campo){
        JOptionPane.showMessageDialog(this, "Fila "+(fila+1)+": número aleatorio inválido en " + campo + " (debe estar en [0,1) y no vacío)", "Dato inválido", JOptionPane.ERROR_MESSAGE);
    }
}

