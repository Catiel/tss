package actividad_4.ejercicio_2; // Paquete del ejercicio 2

import javax.swing.*; // Importa componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla estándar
import java.awt.*; // Layouts y clases gráficas

/**
 * Tabla de datos (sensibilidad 2D) del periodo de recuperación según:
 *  - Columnas: flujo de caja del año 1
 *  - Filas: tasa de crecimiento anual
 */
public class PanelPaybackSensibilidad2D extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios de parámetros
    private final DefaultTableModel modelo; // Modelo de datos inmutable en referencia
    private final JTable tabla;             // Tabla que muestra los resultados

    // Valores fijos según enunciado (pueden ajustarse si se requiere)
    private final double[] flujosAnio1 = {30,40,50,60,70,80,90,100}; // Conjunto de flujos Año 1 a evaluar
    private final double[] tasasCrec = {0.05,0.10,0.15,0.20,0.25};   // Conjunto de tasas de crecimiento anual

    public PanelPaybackSensibilidad2D() { // Constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra para escuchar cambios globales
        EstilosUI.aplicarEstiloPanel(this); // Fondo y estilo base
        setLayout(new BorderLayout(10,10)); // Usa BorderLayout con gaps

        JLabel titulo = new JLabel("Tabla de payback vs Flujo Año 1 y Crecimiento"); // Título descriptivo
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica estilo de título
        titulo.setBorder(BorderFactory.createEmptyBorder(10,10,5,10)); // Margen interno
        add(titulo, BorderLayout.NORTH); // Añade título arriba

        // Columnas: primera vacía para encabezado de filas (tasas), luego cada flujo.
        String[] cols = new String[flujosAnio1.length + 1]; // Array de nombres de columnas
        cols[0] = "Tasa\\Flujo"; // cabecera combinada fila/columna
        for (int i = 0; i < flujosAnio1.length; i++) cols[i+1] = String.valueOf((int)flujosAnio1[i]); // Etiquetas de flujos

        modelo = new DefaultTableModel(cols,0){ // Crea modelo sin filas iniciales
            @Override public boolean isCellEditable(int r,int c){return false;} // Tabla sólo lectura
        };
        tabla = new JTable(modelo){ // Instancia la JTable
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Personaliza render
                Component c = super.prepareRenderer(renderer,row,column); // Obtiene componente base
                if (column==0){ c.setBackground(new Color(240,240,255)); } // Columna 0 (tasas) fondo diferenciado
                else { c.setBackground(Color.WHITE); } // Otras columnas fondo blanco
                return c; // Retorna componente configurado
            }
        };
        EstilosUI.aplicarEstiloTabla(tabla); // Estilos comunes (fuente, cabecera, etc.)
        JScrollPane scroll = new JScrollPane(tabla); // Scroll para tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Margen alrededor
        add(scroll, BorderLayout.CENTER); // Añade scroll al centro

        JPanel panelInfo = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel inferior info
        EstilosUI.aplicarEstiloPanel(panelInfo); // Fondo
        JLabel lbl = new JLabel("Valores = horizonte+1 indican no recupera en horizonte (ej: 11 para horizonte=10)"); // Leyenda explicativa
        EstilosUI.aplicarEstiloLabel(lbl); // Estilo label
        panelInfo.add(lbl); // Añade label a panel
        add(panelInfo, BorderLayout.SOUTH); // Ubica panel info abajo

        construirTabla(); // Genera primeras filas
    }

    private void construirTabla(){ // Llena / reconstruye la tabla según parámetros actuales
        modelo.setRowCount(0); // Limpia cualquier contenido previo
        ControladorParametros p = ControladorParametros.getInstancia(); // Obtiene parámetros globales
        double inversion = p.getInversionOriginal(); // Inversión inicial
        int horizonte = p.getHorizonteAnios(); // Horizonte en años
        for (double tasa : tasasCrec){ // Recorre cada tasa fija
            Object[] fila = new Object[flujosAnio1.length + 1]; // Nueva fila (col0 + flujos)
            fila[0] = String.format("%.0f%%", tasa*100); // Columna 0: tasa en % entero
            for (int i=0;i<flujosAnio1.length;i++){ // Recorre cada flujo Año 1
                int payback = PaybackCalculo.calcularPeriodo(inversion, flujosAnio1[i], tasa, horizonte); // Calcula payback
                int valorMostrar = (payback == -1) ? (horizonte + 1) : payback; // Si no recupera -> horizonte+1
                fila[i+1] = valorMostrar; // Asigna resultado a columna correspondiente
            }
            modelo.addRow(fila); // Añade fila completa al modelo
        }
    }

    @Override public void onParametrosChanged(){ SwingUtilities.invokeLater(this::construirTabla); } // Recalcula al cambiar parámetros (en EDT)

    @Override public void removeNotify(){ ControladorParametros.getInstancia().removeChangeListener(this); super.removeNotify(); } // Se desregistra al remover panel
}