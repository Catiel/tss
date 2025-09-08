package actividad_4.ejercicio_2;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

/**
 * Panel de sensibilidad 2D manual editable EN LA MISMA TABLA.
 * Estructura de la tabla:
 *  - Fila 0: encabezado de flujos (columna 0 = payback actual calculado con params trial, columnas 1..N = valores de Flujo Año 1 editables)
 *  - Filas 1..M: cada fila representa una tasa de crecimiento (columna 0 = tasa % editable). El resto de celdas son resultados calculados (payback) no editables.
 * Flujo y tasa se introducen en bruto (flujo en millones, tasa en %). Botón "Calcular" genera los paybacks.
 * Si no recupera dentro del horizonte, muestra horizonte + 1 (ej: 11 si horizonte=10) en lugar de -1.
 */
public class PanelPaybackSensibilidad2DManual extends JPanel implements ControladorParametros.ParametrosChangeListener {
    private JTable tabla;
    private DefaultTableModel modelo;
    private JSpinner spCols; // número de flujos
    private JSpinner spFilas; // número de tasas
    private JButton btnGenerar;
    private JButton btnCalcular;
    private JLabel lblInfo;

    public PanelPaybackSensibilidad2DManual(){
        ControladorParametros.getInstancia().addChangeListener(this);
        EstilosUI.aplicarEstiloPanel(this);
        setLayout(new BorderLayout(10,10));

        // Barra superior de configuración
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5));
        EstilosUI.aplicarEstiloPanel(top);
        top.add(new JLabel("Columnas (Flujos):"));
        spCols = new JSpinner(new SpinnerNumberModel(5,1,30,1));
        top.add(spCols);
        top.add(new JLabel("Filas (Tasas):"));
        spFilas = new JSpinner(new SpinnerNumberModel(5,1,30,1));
        top.add(spFilas);
        btnGenerar = new JButton("Generar tabla");
        EstilosUI.aplicarEstiloBoton(btnGenerar);
        btnGenerar.addActionListener(e->generarTabla());
        top.add(btnGenerar);
        btnCalcular = new JButton("Calcular");
        EstilosUI.aplicarEstiloBoton(btnCalcular);
        btnCalcular.addActionListener(e->calcularResultados());
        top.add(btnCalcular);
        add(top, BorderLayout.NORTH);

        // Modelo inicial vacío
        crearTablaBase(5,5);
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        add(scroll, BorderLayout.CENTER);

        lblInfo = new JLabel("Edite flujos (fila superior) y tasas % (columna izquierda). Luego pulse Calcular.");
        EstilosUI.aplicarEstiloLabel(lblInfo);
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT));
        EstilosUI.aplicarEstiloPanel(south);
        south.add(lblInfo);
        add(south, BorderLayout.SOUTH);

        // Inicializar esquina superior con payback actual
        onParametrosChanged();
    }

    private void crearTablaBase(int numFlujos, int numTasas){
        modelo = Payback2DUtil.crearModeloBase(numFlujos, numTasas, true, true); // manual: flujos y tasas editables
        Payback2DUtil.inicializarFilas(modelo, numFlujos, numTasas);
        if(tabla==null){
            tabla = new JTable(modelo){
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){
                    Component comp = super.prepareRenderer(renderer,row,column);
                    if(row==0 || column==0) comp.setBackground(new Color(240,240,255)); else comp.setBackground(Color.WHITE);
                    return comp;
                }
            };
            EstilosUI.aplicarEstiloTabla(tabla);
            tabla.setFillsViewportHeight(true);
        } else {
            tabla.setModel(modelo);
        }
    }

    private void generarTabla(){
        int cols = (Integer) spCols.getValue();
        int filas = (Integer) spFilas.getValue();
        crearTablaBase(cols, filas);
        // Actualizar esquina con payback actual después de generar
        onParametrosChanged();
    }

    private void calcularResultados(){
        ControladorParametros p = ControladorParametros.getInstancia();
        boolean ok = Payback2DUtil.calcularYVolcar(modelo, p.getInversionOriginal(), p.getHorizonteAnios());
        if(!ok){
            lblInfo.setText("Errores: verifique flujos (>0) y tasas (>=0)");
            return;
        }
        onParametrosChanged();
        lblInfo.setText("Cálculo completado. Valores = " + (p.getHorizonteAnios() + 1) + " indican no recupera en horizonte=" + p.getHorizonteAnios());
    }

    @Override public void onParametrosChanged(){
        SwingUtilities.invokeLater(() -> {
            ControladorParametros p = ControladorParametros.getInstancia();
            int payback = PaybackCalculo.calcularPeriodo(p.getInversionOriginal(), p.getFlujoAnio1(), p.getTasaCrecimientoAnual(), p.getHorizonteAnios());
            int valorMostrar = (payback == -1) ? (p.getHorizonteAnios() + 1) : payback;
            if (modelo != null && modelo.getRowCount() > 0) {
                modelo.setValueAt(valorMostrar, 0, 0);
            }
        });
    }

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); }
}