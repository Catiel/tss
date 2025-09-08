package actividad_4.ejercicio_2; // Paquete del ejercicio 2 actividad 4

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Layouts y utilidades de AWT

/**
 * Panel que muestra el detalle de flujos de caja anuales y el periodo de recuperación (payback).
 */
public class PanelPaybackDetalle extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios de parámetros
    private final DefaultTableModel modelo; // Modelo de datos de la tabla (filas: años)
    private final JLabel lblPeriodo;        // Etiqueta que muestra el periodo de recuperación calculado

    public PanelPaybackDetalle() { // Constructor del panel
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como listener para actualizar al cambiar parámetros
        EstilosUI.aplicarEstiloPanel(this); // Aplica estilo visual común al panel
        setLayout(new BorderLayout(10,10)); // Usa BorderLayout con separación

        JLabel titulo = new JLabel("Flujos de caja anuales"); // Título del panel
        EstilosUI.aplicarEstiloTitulo(titulo); // Estilo de título consistente
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,5,10)); // Margen alrededor del título
        add(titulo, BorderLayout.NORTH); // Coloca el título en la parte superior

        String[] cols = {"Año","Flujo de caja","Acumulativo","¿Menos que original?"}; // Encabezados de columnas
        modelo = new DefaultTableModel(cols,0){ // Crea modelo de tabla sin filas iniciales
            @Override public boolean isCellEditable(int r,int c){return false;} // Tabla sólo lectura
            @Override public Class<?> getColumnClass(int columnIndex){ // Define tipos para mejor renderizado / orden
                return columnIndex==0?Integer.class: (columnIndex==3?Integer.class:Double.class); // Año e indicador como Integer, otros Double
            }
        };
        JTable tabla = new JTable(modelo); // Crea la tabla con el modelo
        EstilosUI.aplicarEstiloTabla(tabla); // Aplica estilo a la tabla
        tabla.setFillsViewportHeight(true); // Rellena todo el alto del viewport
        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Margen interno del scroll
        add(scroll, BorderLayout.CENTER); // Añade la tabla al centro

        // Solo valor numérico (etiqueta inferior con el periodo de recuperación)
        lblPeriodo = new JLabel("-"); // Inicialmente sin valor
        EstilosUI.aplicarEstiloLabel(lblPeriodo); // Estilo de etiqueta
        lblPeriodo.setBorder(BorderFactory.createEmptyBorder(0,15,10,10)); // Margen interno
        add(lblPeriodo, BorderLayout.SOUTH); // Coloca la etiqueta abajo

        actualizar(); // Realiza primera carga de datos
    }

    private void actualizar(){ // Recalcula y refresca la tabla y el periodo
        ControladorParametros p = ControladorParametros.getInstancia(); // Obtiene parámetros actuales
        PaybackCalculo.ResultadoPayback res = PaybackCalculo.calcular(p); // Calcula el payback completo
        modelo.setRowCount(0); // Limpia filas previas
        for (PaybackCalculo.ResultadoAnual r: res.resultados){ // Itera resultados anuales
            modelo.addRow(new Object[]{r.anio, r.flujo, r.acumulado, r.menorQueInversion}); // Añade fila
        }
        int periodo = res.periodoRecuperacion; // Año en que se recupera (o -1)
        int valorMostrar = (periodo == -1) ? (p.getHorizonteAnios() + 1) : periodo; // Si no recupera muestra horizonte+1
        lblPeriodo.setText(String.valueOf(valorMostrar)); // Actualiza etiqueta
    }

    @Override public void onParametrosChanged(){ SwingUtilities.invokeLater(this::actualizar); } // Al cambiar parámetros recalcula en EDT

    @Override public void removeNotify(){ // Al eliminar el panel de la jerarquía
        ControladorParametros.getInstancia().removeChangeListener(this); // Se desregistra para evitar fugas de memoria
        super.removeNotify(); // Llama a implementación base
    }
}