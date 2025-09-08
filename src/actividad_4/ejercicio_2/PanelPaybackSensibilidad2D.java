package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Tabla de datos (sensibilidad 2D) del periodo de recuperación según:
 *  - Columnas: flujo de caja del año 1
 *  - Filas: tasa de crecimiento anual
 */
public class PanelPaybackSensibilidad2D extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final DefaultTableModel modelo;
    private final JTable tabla;

    // Valores fijos según enunciado (pueden ajustarse si se requiere)
    private final double[] flujosAnio1 = {30,40,50,60,70,80,90,100};
    private final double[] tasasCrec = {0.05,0.10,0.15,0.20,0.25};

    public PanelPaybackSensibilidad2D() {
        ControladorParametros.getInstancia().addChangeListener(this);
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Tabla de payback vs Flujo Año 1 y Crecimiento");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        add(titulo, BorderLayout.NORTH);

        // Columnas: primera vacía para encabezado de filas (tasas), luego cada flujo.
        String[] cols = new String[flujosAnio1.length + 1];
        cols[0] = "Tasa\\Flujo"; // cabecera combinada
        for (int i = 0; i < flujosAnio1.length; i++) cols[i+1] = String.valueOf((int)flujosAnio1[i]);

        modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
        };
        tabla = new JTable(modelo){
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                Component c = super.prepareRenderer(renderer,row,column);
                if (column==0){ c.setBackground(new Color(240,240,255)); }
                else { c.setBackground(Color.WHITE); }
                return c;
            }
        };
        EstilosUI.aplicarEstiloTabla(tabla);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scroll, BorderLayout.CENTER);

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(panelInfo);
        JLabel lbl = new JLabel("Valores > horizonte se muestran como 'NR' (No Recupera)");
        EstilosUI.aplicarEstiloLabel(lbl);
        panelInfo.add(lbl);
        add(panelInfo, BorderLayout.SOUTH);

        construirTabla();
    }

    private void construirTabla(){
        modelo.setRowCount(0);
        ControladorParametros p = ControladorParametros.getInstancia();
        double inversion = p.getInversionOriginal();
        int horizonte = p.getHorizonteAnios();
        for (double tasa : tasasCrec){
            Object[] fila = new Object[flujosAnio1.length + 1];
            fila[0] = String.format("%.0f%%", tasa*100);
            for (int i=0;i<flujosAnio1.length;i++){
                int payback = PaybackCalculo.calcularPeriodo(inversion, flujosAnio1[i], tasa, horizonte);
                fila[i+1] = payback == -1 ? "NR" : payback;
            }
            modelo.addRow(fila);
        }
    }

    @Override public void onParametrosChanged(){ SwingUtilities.invokeLater(this::construirTabla); }

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); }
}

