package actividad_4.ejercicio_2; // Paquete del ejercicio 2

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla estándar
import java.awt.*; // Clases de layout y gráficos

/**
 * Panel de sensibilidad 2D manual editable EN LA MISMA TABLA.
 * Estructura de la tabla:
 *  - Fila 0: encabezado de flujos (columna 0 = payback actual con parámetros globales, columnas 1..N = flujos año 1 editables)
 *  - Filas 1..M: cada fila representa una tasa de crecimiento (col 0 = tasa % editable). Las celdas resto son paybacks calculados.
 *  Si no recupera dentro del horizonte => muestra horizonte+1 (en vez de -1).
 */
public class PanelPaybackSensibilidad2DManual extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel que escucha cambios
    private JTable tabla;                 // Referencia a la JTable
    private DefaultTableModel modelo;     // Modelo de datos de la tabla
    private JSpinner spCols;              // Spinner cantidad de flujos (columnas editables)
    private JSpinner spFilas;             // Spinner cantidad de tasas (filas)
    private JButton btnGenerar;           // Botón para regenerar estructura
    private JButton btnCalcular;          // Botón para calcular paybacks
    private JLabel lblInfo;               // Mensajes de estado / validación

    public PanelPaybackSensibilidad2DManual(){ // Constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como listener de parámetros
        EstilosUI.aplicarEstiloPanel(this); // Fondo y estilo base
        setLayout(new BorderLayout(10,10)); // BorderLayout con espacios

        // Barra superior de configuración
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,10,5)); // Panel superior con FlowLayout
        EstilosUI.aplicarEstiloPanel(top); // Estilo panel superior
        top.add(new JLabel("Columnas (Flujos):")); // Etiqueta spinner columnas
        spCols = new JSpinner(new SpinnerNumberModel(5,1,30,1)); // Spinner columnas inicial=5
        top.add(spCols); // Añade spinner columnas
        top.add(new JLabel("Filas (Tasas):")); // Etiqueta spinner filas
        spFilas = new JSpinner(new SpinnerNumberModel(5,1,30,1)); // Spinner filas inicial=5
        top.add(spFilas); // Añade spinner filas
        btnGenerar = new JButton("Generar tabla"); // Botón generar estructura
        EstilosUI.aplicarEstiloBoton(btnGenerar); // Estilo botón
        btnGenerar.addActionListener(e->generarTabla()); // Acción generar
        top.add(btnGenerar); // Añade botón
        btnCalcular = new JButton("Calcular"); // Botón calcular resultados
        EstilosUI.aplicarEstiloBoton(btnCalcular); // Estilo botón
        btnCalcular.addActionListener(e->calcularResultados()); // Acción calcular
        top.add(btnCalcular); // Añade botón
        add(top, BorderLayout.NORTH); // Inserta barra superior

        // Modelo inicial vacío
        crearTablaBase(5,5); // Construye tabla por defecto 5x5
        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Margen del scroll
        add(scroll, BorderLayout.CENTER); // Añade tabla al centro

        lblInfo = new JLabel("Edite flujos (fila superior) y tasas % (columna izquierda). Luego pulse Calcular."); // Mensaje inicial
        EstilosUI.aplicarEstiloLabel(lblInfo); // Estilo label
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel inferior
        EstilosUI.aplicarEstiloPanel(south); // Estilo panel inferior
        south.add(lblInfo); // Añade label
        add(south, BorderLayout.SOUTH); // Añade panel inferior

        // Inicializar esquina superior con payback actual
        onParametrosChanged(); // Calcula y muestra el payback actual en (0,0)
    }

    private void crearTablaBase(int numFlujos, int numTasas){ // Crea modelo y tabla base
        modelo = Payback2DUtil.crearModeloBase(numFlujos, numTasas, true, true); // flujos y tasas editables
        Payback2DUtil.inicializarFilas(modelo, numFlujos, numTasas); // Agrega filas vacías
        if(tabla==null){ // Si tabla aún no existe
            tabla = new JTable(modelo){ // Crea JTable anónima para colorear
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Personaliza celdas
                    Component comp = super.prepareRenderer(renderer,row,column); // Componente base
                    if(row==0 || column==0) comp.setBackground(new Color(240,240,255)); else comp.setBackground(Color.WHITE); // Colorea cabeceras lógicas
                    return comp; // Devuelve componente configurado
                }
            };
            EstilosUI.aplicarEstiloTabla(tabla); // Estilo tabla (fuente, header)
            tabla.setFillsViewportHeight(true); // Rellenar alto viewport
        } else { // Ya existe tabla
            tabla.setModel(modelo); // Solo actualizar modelo
        }
    }

    private void generarTabla(){ // Re-crea estructura según spinners
        int cols = (Integer) spCols.getValue(); // Número de columnas (flujos)
        int filas = (Integer) spFilas.getValue(); // Número de filas (tasas)
        crearTablaBase(cols, filas); // Reconstruye modelo
        onParametrosChanged(); // Actualiza celda (0,0) con payback actual
    }

    private void calcularResultados(){ // Calcula paybacks para combinaciones introducidas
        ControladorParametros p = ControladorParametros.getInstancia(); // Parámetros globales
        boolean ok = Payback2DUtil.calcularYVolcar(modelo, p.getInversionOriginal(), p.getHorizonteAnios()); // Ejecuta cálculo
        if(!ok){ // Si validación falla
            lblInfo.setText("Errores: verifique flujos (>0) y tasas (>=0)"); // Mensaje error
            return; // Sale sin actualizar esquina
        }
        onParametrosChanged(); // Refresca (0,0) con payback actualizado global
        lblInfo.setText("Cálculo completado. Valores = " + (p.getHorizonteAnios() + 1) + " indican no recupera en horizonte=" + p.getHorizonteAnios()); // Mensaje éxito
    }

    @Override public void onParametrosChanged(){ // Callback cambio parámetros globales
        SwingUtilities.invokeLater(() -> { // Asegura ejecución en EDT
            ControladorParametros p = ControladorParametros.getInstancia(); // Obtiene parámetros
            int payback = PaybackCalculo.calcularPeriodo(p.getInversionOriginal(), p.getFlujoAnio1(), p.getTasaCrecimientoAnual(), p.getHorizonteAnios()); // Calcula periodo
            int valorMostrar = (payback == -1) ? (p.getHorizonteAnios() + 1) : payback; // Normaliza valor (-1 -> horizonte+1)
            if (modelo != null && modelo.getRowCount() > 0) { // Verifica que exista modelo
                modelo.setValueAt(valorMostrar, 0, 0); // Actualiza celda (0,0)
            }
        });
    }

    @Override public void removeNotify(){ // Al remover panel del contenedor
        ControladorParametros.getInstancia().removeChangeListener(this); // Se des-registra listener
        super.removeNotify(); // Llama a implementación padre
    }
}