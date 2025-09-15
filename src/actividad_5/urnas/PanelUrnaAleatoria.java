package actividad_5.urnas;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.Random;

/** Panel que genera números pseudoaleatorios y simula las extracciones. */
public class PanelUrnaAleatoria extends JPanel {
    private final DefaultTableModel modelo;
    private final JTextField txtCantidad;
    private final JButton btnGenerar;
    private final JLabel lblResumen;
    private final Random random = new Random();

    public PanelUrnaAleatoria(){
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Simulación con números generados por el programa");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,0,10));
        add(titulo, BorderLayout.NORTH);

        JPanel panelTop = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(panelTop);
        panelTop.add(new JLabel("Cantidad de extracciones:"));
        txtCantidad = new JTextField("10",5);
        panelTop.add(txtCantidad);
        btnGenerar = new JButton("Generar");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        panelTop.add(btnGenerar);
        add(panelTop, BorderLayout.BEFORE_FIRST_LINE);

        modelo = new DefaultTableModel(new Object[]{"# de pelota","Número aleatorio","Color"},0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int c){ return c==0?Integer.class:String.class; }
        };
        JTable tabla = new JTable(modelo);
        EstilosUI.aplicarEstiloTabla(tabla);
        add(new JScrollPane(tabla), BorderLayout.CENTER);

        lblResumen = new JLabel(" ");
        EstilosUI.aplicarEstiloLabel(lblResumen);
        lblResumen.setBorder(BorderFactory.createEmptyBorder(5,10,10,10));
        add(lblResumen, BorderLayout.SOUTH);

        btnGenerar.addActionListener(this::generar);
    }

    private void generar(ActionEvent e){
        int n;
        try{
            n = Integer.parseInt(txtCantidad.getText().trim());
            if(n<=0 || n>1000){
                JOptionPane.showMessageDialog(this,"Ingrese un valor entre 1 y 1000","Cantidad inválida",JOptionPane.WARNING_MESSAGE);
                return;
            }
        }catch(NumberFormatException ex){
            JOptionPane.showMessageDialog(this,"Valor no numérico","Error",JOptionPane.ERROR_MESSAGE);return;
        }
        modelo.setRowCount(0);
        int verdes=0, rojas=0, amarillas=0;
        for(int i=0;i<n;i++){
            double r = random.nextDouble();
            String color = UrnaModelo.colorPara(r);
            // Reemplazo de switch moderno por versión clásica
            if("verdes".equals(color)){
                verdes++;
            } else if("rojas".equals(color)){
                rojas++;
            } else if("amarillas".equals(color)){
                amarillas++;
            }
            modelo.addRow(new Object[]{i+1, UtilFormatoUrnas.fmt(r), color});
        }
        lblResumen.setText("Totales -> verdes: " + verdes + ", rojas: " + rojas + ", amarillas: " + amarillas);
    }
}
