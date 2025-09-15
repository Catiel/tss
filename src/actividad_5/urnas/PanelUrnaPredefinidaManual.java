package actividad_5.urnas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;

/** Panel para que el usuario ingrese manualmente los números pseudoaleatorios (en [0,1)). */
public class PanelUrnaPredefinidaManual extends JPanel {
    private final DefaultTableModel modelo;
    private final JTextField txtFilas;
    private final JButton btnCrear;
    private final JButton btnCalcular;
    private final JButton btnLimpiar;
    private final JLabel lblResumen;

    public PanelUrnaPredefinidaManual(){
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Simulación con números ingresados manualmente");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        add(titulo, BorderLayout.NORTH);

        // Panel superior de controles
        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(panelTop);
        panelTop.add(new JLabel("Cantidad de filas:"));
        txtFilas = new JTextField("10",5);
        panelTop.add(txtFilas);
        btnCrear = new JButton("Crear filas");
        EstilosUI.aplicarEstiloBoton(btnCrear);
        panelTop.add(btnCrear);
        btnCalcular = new JButton("Calcular colores");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        panelTop.add(btnCalcular);
        btnLimpiar = new JButton("Limpiar");
        EstilosUI.aplicarEstiloBoton(btnLimpiar);
        panelTop.add(btnLimpiar);
        JLabel lblHint = new JLabel("Ingrese valores entre 0 y 1 (ej: 0.24, 0,72)");
        EstilosUI.aplicarEstiloLabel(lblHint);
        panelTop.add(lblHint);
        add(panelTop, BorderLayout.BEFORE_FIRST_LINE);

        modelo = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){
            @Override public boolean isCellEditable(int r,int c){ return c==1; } // solo número editable
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tabla = new JTable(modelo);
        EstilosUI.aplicarEstiloTabla(tabla);
        tabla.getColumnModel().getColumn(0).setPreferredWidth(90);
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140);
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        EstilosUI.aplicarEstiloLabel(lblResumen);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5,10,10,10));
        add(lblResumen, BorderLayout.SOUTH);

        btnCalcular.setEnabled(false);

        btnCrear.addActionListener(this::crearFilas);
        btnCalcular.addActionListener(this::calcular);
        btnLimpiar.addActionListener(e -> limpiar());
    }

    private void crearFilas(ActionEvent e){
        int n;
        try {
            n = Integer.parseInt(txtFilas.getText().trim());
            if(n<=0 || n>200){
                JOptionPane.showMessageDialog(this,"Ingrese un número entre 1 y 200","Cantidad inválida",JOptionPane.WARNING_MESSAGE);
                return; }
        } catch (NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"Valor no numérico","Error",JOptionPane.ERROR_MESSAGE); return; }
        modelo.setRowCount(0);
        for(int i=0;i<n;i++) modelo.addRow(new Object[]{i+1, "", ""});
        btnCalcular.setEnabled(true);
        lblResumen.setText(" ");
    }

    private void calcular(ActionEvent e){
        int verdes=0, rojas=0, amarillas=0; // contadores
        for(int i=0;i<modelo.getRowCount();i++){
            String txt = (modelo.getValueAt(i,1)==null)?"":modelo.getValueAt(i,1).toString();
            if(txt.trim().isEmpty()){
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": falta el número aleatorio","Dato faltante",JOptionPane.WARNING_MESSAGE); return; }
            double r;
            try { r = UtilFormatoUrnas.parse(txt); } catch (NumberFormatException ex){
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": formato inválido ('"+txt+"')","Error",JOptionPane.ERROR_MESSAGE); return; }
            if(r<0 || r>=1){
                JOptionPane.showMessageDialog(this,"Fila "+(i+1)+": el número debe estar en [0,1)","Fuera de rango",JOptionPane.WARNING_MESSAGE); return; }
            String color = UrnaModelo.colorPara(r);
            modelo.setValueAt(color, i, 2);
            if("verdes".equals(color)){
                verdes++;
            } else if("rojas".equals(color)){
                rojas++;
            } else if("amarillas".equals(color)){
                amarillas++;
            }
        }
        lblResumen.setText("Totales -> verdes: "+verdes+", rojas: "+rojas+", amarillas: "+amarillas);
    }

    private void limpiar(){
        modelo.setRowCount(0);
        btnCalcular.setEnabled(false);
        lblResumen.setText(" ");
    }
}
