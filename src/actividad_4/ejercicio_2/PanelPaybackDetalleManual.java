package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Detalle de amortización (payback) para una combinación manual seleccionada
 * desde la tabla de sensibilidad manual.
 * No escucha automáticamente parámetros globales; se actualiza vía método publico.
 */
public class PanelPaybackDetalleManual extends JPanel {
    private final DefaultTableModel modelo;
    private final JLabel lblTituloCombo;
    private final JLabel lblPeriodo;

    public PanelPaybackDetalleManual(){
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        JPanel cabecera = new JPanel(new BorderLayout());
        EstilosUI.aplicarEstiloPanel(cabecera);
        lblTituloCombo = new JLabel("Detalle manual - seleccione una celda en la tabla 2D manual");
        EstilosUI.aplicarEstiloTitulo(lblTituloCombo);
        lblTituloCombo.setBorder(BorderFactory.createEmptyBorder(10,10,5,10));
        cabecera.add(lblTituloCombo, BorderLayout.CENTER);
        add(cabecera, BorderLayout.NORTH);

        String[] cols = {"Año","Flujo de caja","Acumulado","¿Menos que inv.?"};
        modelo = new DefaultTableModel(cols,0){
            @Override public boolean isCellEditable(int r,int c){return false;}
            @Override public Class<?> getColumnClass(int ci){return ci==0?Integer.class:(ci==3?Integer.class:Double.class);} }
        ;
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
    }

    /**
     * Actualiza el detalle para una combinación.
     * @param inversion inversión inicial
     * @param flujoAnio1 flujo año 1
     * @param crecimiento tasa crecimiento (decimal)
     * @param horizonte horizonte años
     */
    public void actualizar(double inversion, double flujoAnio1, double crecimiento, int horizonte){
        PaybackCalculo.ResultadoPayback res = PaybackCalculo.calcular(inversion, flujoAnio1, crecimiento, horizonte);
        modelo.setRowCount(0);
        for (PaybackCalculo.ResultadoAnual r: res.resultados){
            modelo.addRow(new Object[]{r.anio, r.flujo, r.acumulado, r.menorQueInversion});
        }
        if(res.periodoRecuperacion==-1){
            lblPeriodo.setText("Amortización: No recupera en "+ horizonte +" años");
        } else {
            lblPeriodo.setText("Amortización: " + res.periodoRecuperacion);
        }
        lblTituloCombo.setText(String.format("Detalle - Flujo Año1=%.2f | Crec=%.2f%%", flujoAnio1, crecimiento*100));
    }
}

