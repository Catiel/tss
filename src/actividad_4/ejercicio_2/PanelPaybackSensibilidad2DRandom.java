package actividad_4.ejercicio_2; // Paquete del ejercicio 2

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Clases gráficas y layouts
import java.util.Locale; // Locale para formateo numérico consistente
import java.util.concurrent.ThreadLocalRandom; // Generador de números aleatorios eficiente

/**
 * Panel de sensibilidad 2D (payback) con generación aleatoria de flujos (fila 0) y tasas (col 0).
 * Celda (0,0) muestra el payback usando parámetros globales. Valor horizonte+1 = no recupera.
 */
public class PanelPaybackSensibilidad2DRandom extends JPanel implements ControladorParametros.ParametrosChangeListener { // Implementa listener de cambios
    private JTable tabla;                       // Tabla visual
    private DefaultTableModel modelo;           // Modelo de datos de la tabla
    private JSpinner spCols, spFilas;            // Spinners cantidad de columnas (flujos) y filas (tasas)
    private JSpinner spFlujoMin, spFlujoMax;     // Rango mínimo/máximo flujos año 1
    private JSpinner spTasaMin, spTasaMax;       // Rango mínimo/máximo tasas (%)
    private JButton btnGenerar;                  // Botón para generar datos aleatorios
    private JLabel lblInfo;                      // Mensajes informativos / errores
    private boolean datosGenerados = false;      // Indica si hay datos válidos para recalcular

    public PanelPaybackSensibilidad2DRandom(){ // Constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra como observador
        EstilosUI.aplicarEstiloPanel(this); // Estilo base fondo
        setLayout(new BorderLayout(10,10)); // Layout principal con márgenes
        construirBarraSuperior();            // Construye controles configurables
        crearTablaBase(5,5);                 // Crea tabla inicial 5x5
        JScrollPane scroll = new JScrollPane(tabla); // Scroll para la tabla
        scroll.setBorder(BorderFactory.createEmptyBorder(10,10,10,10)); // Margen interior
        add(scroll, BorderLayout.CENTER);    // Añade scroll al centro
        lblInfo = new JLabel("Configure y pulse Generar Aleatorio."); // Mensaje inicial
        EstilosUI.aplicarEstiloLabel(lblInfo); // Estilo label
        JPanel south = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel inferior
        EstilosUI.aplicarEstiloPanel(south);  // Fondo blanco
        south.add(lblInfo);                   // Inserta label
        add(south, BorderLayout.SOUTH);       // Añade panel inferior
        onParametrosChanged();                // Inicializa (0,0) con payback actual
    }

    private void construirBarraSuperior(){ // Construye panel con controles de rango
        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT,8,5)); // Flow layout compacto
        EstilosUI.aplicarEstiloPanel(top); // Fondo
        top.add(new JLabel("Cols:")); spCols = new JSpinner(new SpinnerNumberModel(5,1,50,1)); top.add(spCols); // Spinner columnas
        top.add(new JLabel("Filas:")); spFilas = new JSpinner(new SpinnerNumberModel(5,1,50,1)); top.add(spFilas); // Spinner filas
        top.add(new JLabel("Flujo Min:")); spFlujoMin = new JSpinner(new SpinnerNumberModel(30.0,0.01,1e6,1.0)); top.add(spFlujoMin); // Flujo mínimo
        top.add(new JLabel("Flujo Max:")); spFlujoMax = new JSpinner(new SpinnerNumberModel(100.0,0.01,1e6,1.0)); top.add(spFlujoMax); // Flujo máximo
        top.add(new JLabel("Tasa% Min:")); spTasaMin = new JSpinner(new SpinnerNumberModel(5.0,0.0,1000.0,0.5)); top.add(spTasaMin); // Tasa mínima
        top.add(new JLabel("Tasa% Max:")); spTasaMax = new JSpinner(new SpinnerNumberModel(25.0,0.0,1000.0,0.5)); top.add(spTasaMax); // Tasa máxima
        btnGenerar = new JButton("Generar Aleatorio"); // Botón generar
        EstilosUI.aplicarEstiloBoton(btnGenerar); // Estilo botón
        btnGenerar.addActionListener(e->generarAleatorio()); // Acción clic -> generar
        top.add(btnGenerar); // Añade botón
        add(top, BorderLayout.NORTH); // Coloca barra arriba
    }

    private void crearTablaBase(int numFlujos, int numTasas){ // Crea modelo y estructura inicial
        modelo = Payback2DUtil.crearModeloBase(numFlujos, numTasas, false, false); // No editable (aleatorio)
        Payback2DUtil.inicializarFilas(modelo, numFlujos, numTasas); // Filas vacías
        if(tabla==null){ // Si tabla aún no creada
            tabla = new JTable(modelo){ // Sobrescribe para colorear cabeceras lógicas
                @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer,int row,int column){ // Render personalizado
                    Component comp = super.prepareRenderer(renderer,row,column); // Componente base
                    if(row==0||column==0) comp.setBackground(new Color(240,240,255)); else comp.setBackground(Color.WHITE); // Fondo distinto cabeceras
                    return comp; // Devuelve componente
                }
            };
            EstilosUI.aplicarEstiloTabla(tabla); // Aplica estilo (fuentes, header, etc.)
            tabla.setFillsViewportHeight(true);  // Ajusta altura
        } else tabla.setModel(modelo);           // Reutiliza instancia existente
        datosGenerados=false; // Marca que aún no hay datos válidos
    }

    private void generarAleatorio(){ // Genera flujos y tasas aleatorias según rangos
        int cols=(Integer)spCols.getValue();      // Número de flujos
        int filas=(Integer)spFilas.getValue();    // Número de tasas
        double fMin=((Number)spFlujoMin.getValue()).doubleValue(); // Flujo mínimo
        double fMax=((Number)spFlujoMax.getValue()).doubleValue(); // Flujo máximo
        double tMin=((Number)spTasaMin.getValue()).doubleValue();  // Tasa mínima (%)
        double tMax=((Number)spTasaMax.getValue()).doubleValue();  // Tasa máxima (%)
        if(fMax<=fMin || tMax<tMin || fMin<=0){  // Validación de rangos
            lblInfo.setText("Rangos inválidos"); // Mensaje error
            return;                               // Aborta
        }
        crearTablaBase(cols,filas);               // Reinicia estructura
        ThreadLocalRandom rnd=ThreadLocalRandom.current(); // RNG thread-safe y rápido
        for(int c=1;c<=cols;c++){                 // Genera flujos para fila 0
            double v=fMin+rnd.nextDouble()*(fMax-fMin); // Valor aleatorio en rango
            modelo.setValueAt(Payback2DUtil.formatear(v),0,c); // Escribe flujo formateado
        }
        for(int r=1;r<=filas;r++){                // Genera tasas para columna 0
            double t=tMin+rnd.nextDouble()*(tMax-tMin); // Tasa aleatoria
            modelo.setValueAt(String.format(Locale.US,"%.2f",t),r,0); // Guarda tasa con 2 decimales
        }
        recalcular();                             // Calcula paybacks
        lblInfo.setText("Generado (h+1 = no recupera)"); // Mensaje de estado
    }

    private void recalcular(){ // Recalcula paybacks para la matriz actual
        ControladorParametros p=ControladorParametros.getInstancia(); // Parámetros globales
        boolean ok = Payback2DUtil.calcularYVolcar(modelo, p.getInversionOriginal(), p.getHorizonteAnios()); // Calcula y vuelca
        datosGenerados = ok; // Actualiza bandera
        actualizarCeldaEsquina(); // Refresca (0,0)
        if(!ok) lblInfo.setText("Datos inválidos"); // Mensaje si falla validación
    }

    private void actualizarCeldaEsquina(){ // Actualiza celda (0,0) con payback global
        ControladorParametros p=ControladorParametros.getInstancia(); // Obtiene controlador
        int pay=PaybackCalculo.calcularPeriodo(p.getInversionOriginal(), p.getFlujoAnio1(), p.getTasaCrecimientoAnual(), p.getHorizonteAnios()); // Calcula periodo
        if(modelo!=null && modelo.getRowCount()>0) // Verifica modelo listo
            modelo.setValueAt(pay==-1? p.getHorizonteAnios()+1: pay,0,0); // Escribe payback normalizado
    }

    @Override public void onParametrosChanged(){ // Callback cuando cambian parámetros globales
        SwingUtilities.invokeLater(() -> {        // Ejecuta en EDT
            if(datosGenerados) recalcular();      // Recalcula si hay datos
            else actualizarCeldaEsquina();        // Sólo refresca esquina
        });
    }

    @Override public void removeNotify(){ // Cuando el panel deja el árbol Swing
        ControladorParametros.getInstancia().removeChangeListener(this); // De-registra listener
        super.removeNotify(); // Llama a super
    }
}
