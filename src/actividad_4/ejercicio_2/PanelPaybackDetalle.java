package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel que muestra el detalle de flujos de caja anuales y el periodo de recuperación (payback).
 */
public class PanelPaybackDetalle extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private final DefaultTableModel modelo;
    private final JLabel lblPeriodo;

    public PanelPaybackDetalle() {
        ControladorParametros.getInstancia().addChangeListener(this);
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JLabel titulo = new JLabel("Flujos de caja anuales");
        EstilosUI.aplicarEstiloTitulo(titulo);
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        add(titulo, BorderLayout.NORTH);

        String[] cols = {"Año","Flujo de caja","Acumulativo","¿Menos que original?"};
        modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int columnIndex){
                return columnIndex==0?Integer.class: (columnIndex==3?Integer.class:Double.class);
            }
        };
        JTable tabla = new JTable(modelo);
        EstilosUI.aplicarEstiloTabla(tabla);
        tabla.setFillsViewportHeight(true);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scroll, BorderLayout.CENTER);

        lblPeriodo = new JLabel("Amortización: -");
        EstilosUI.aplicarEstiloLabel(lblPeriodo);
        lblPeriodo.setBorder(BorderFactory.createEmptyBorder(0,15,10,10));
        add(lblPeriodo, BorderLayout.SOUTH);

        actualizar();
    }

    private void actualizar(){
        ControladorParametros p = ControladorParametros.getInstancia();
        PaybackCalculo.ResultadoPayback res = PaybackCalculo.calcular(p);
        modelo.setRowCount(0);
        for (PaybackCalculo.ResultadoAnual r: res.resultados){
            modelo.addRow(new Object[]{r.anio, r.flujo, r.acumulado, r.menorQueInversion});
        }
        int periodo = res.periodoRecuperacion;
        if (periodo == -1) lblPeriodo.setText("Amortización: No recupera en " + p.getHorizonteAnios() + " años");
        else lblPeriodo.setText("Amortización: " + periodo);
    }

    @Override public void onParametrosChanged(){ SwingUtilities.invokeLater(this::actualizar); }

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); }
}
