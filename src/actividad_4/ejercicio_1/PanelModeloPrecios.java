package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Componentes Swing
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Layouts y utilidades AWT

public class PanelModeloPrecios extends JPanel implements ControladorParametros.ParametrosChangeListener { // Panel principal que escucha cambios de parámetros
    private final JLabel lblVANCon;               // Etiqueta VAN escenario con nueva versión
    private final JLabel lblVANSin;               // Etiqueta VAN escenario sin nueva versión
    private final JLabel lblDiferenciaVAN;        // Etiqueta diferencia de VAN
    private final JLabel lblTasaDescuentoValor;   // Etiqueta valor tasa de descuento actual
    private final DefaultTableModel modeloTabla;  // Modelo de datos de la tabla de resultados
    private final JTable tablaResultados;         // Tabla visual de resultados

    /**
     * Constructor. Inicializa UI, registra listener y dispara primer cálculo.
     */
    public PanelModeloPrecios() { // Inicio constructor
        ControladorParametros.getInstancia().addChangeListener(this); // Se registra para recibir notificaciones

        EstilosUI.aplicarEstiloPanel(this);          // Aplica estilo fondo
        setLayout(new GridBagLayout());              // Usa GridBagLayout para disposición flexible
        GridBagConstraints gbc = new GridBagConstraints(); // Restricciones de layout
        gbc.insets = new Insets(12, 18, 12, 18);     // Márgenes entre componentes
        gbc.anchor = GridBagConstraints.WEST;        // Alineación izquierda
        gbc.gridy = 0;                               // Fila inicial

        // Título principal ---------------------------------------------
        JLabel titulo = new JLabel("Modelo de beneficios"); // Título
        EstilosUI.aplicarEstiloTitulo(titulo);       // Estilo título
        gbc.gridx = 0; gbc.gridwidth = 8;            // Ocupa 8 columnas lógicas
        add(titulo, gbc);                            // Añade título

        // Fila de VANs -------------------------------------------------
        gbc.gridy++;                                 // Siguiente fila
        gbc.gridwidth = 1;                           // Restablece ancho por defecto
        gbc.gridx = 0;                               // Columna 0
        JLabel lblVANConTitulo = new JLabel("VAN con versión francesa ($):"); // Etiqueta título VAN con
        EstilosUI.aplicarEstiloLabel(lblVANConTitulo); // Estilo label
        add(lblVANConTitulo, gbc);                   // Añade label
        gbc.gridx = 1;                               // Columna valor
        lblVANCon = new JLabel("-");                // Valor inicial
        EstilosUI.aplicarEstiloLabel(lblVANCon);     // Estilo valor
        add(lblVANCon, gbc);                         // Añade valor

        gbc.gridx = 2;                               // Columna título VAN sin
        JLabel lblVANSinTitulo = new JLabel("VAN sin versión francesa ($):"); // Etiqueta VAN sin
        EstilosUI.aplicarEstiloLabel(lblVANSinTitulo); // Estilo
        add(lblVANSinTitulo, gbc);                   // Añade etiqueta
        gbc.gridx = 3;                               // Columna valor
        lblVANSin = new JLabel("-");                // Valor inicial
        EstilosUI.aplicarEstiloLabel(lblVANSin);     // Estilo valor
        add(lblVANSin, gbc);                         // Añade valor

        gbc.gridx = 4;                               // Columna título diferencia
        JLabel lblDiferenciaVANTitulo = new JLabel("Diferencia VAN ($):"); // Etiqueta diferencia
        EstilosUI.aplicarEstiloLabel(lblDiferenciaVANTitulo); // Estilo
        add(lblDiferenciaVANTitulo, gbc);            // Añade etiqueta
        gbc.gridx = 5;                               // Columna valor diferencia
        lblDiferenciaVAN = new JLabel("-");         // Valor inicial
        EstilosUI.aplicarEstiloLabel(lblDiferenciaVAN); // Estilo valor
        add(lblDiferenciaVAN, gbc);                  // Añade valor

        // Definición de columnas (estructura espejo SIN / CON) ---------
        String[] columnas = {
            "Año",                 // Col 0
            "Tamaño de mercado",   // Col 1
            "Unidades vendidas",   // Col 2 (sin)
            "Costo variable",      // Col 3 (sin)
            "Ingresos",            // Col 4 (sin)
            "Unidades vendidas",   // Col 5 (con)
            "Costo variable",      // Col 6 (con)
            "Ingresos"             // Col 7 (con)
        };

        // Modelo de tabla no editable ----------------------------------
        modeloTabla = new DefaultTableModel(columnas, 0) { // Crea modelo sin filas
            @Override public boolean isCellEditable(int row, int col) { return false; } // Bloquea edición
        };

        // Tabla ---------------------------------------------------------
        tablaResultados = new JTable(modeloTabla); // Instancia JTable
        EstilosUI.aplicarEstiloTabla(tablaResultados); // Estilo tabla

        // Ajuste de anchos de columnas ---------------------------------
        int[] anchos = {60,120,120,120,120,120,120,120}; // Anchos preferidos
        for (int i = 0; i < anchos.length; i++) {        // Itera columnas
            tablaResultados.getColumnModel().getColumn(i).setPreferredWidth(anchos[i]); // Aplica ancho
        }

        // (headerPanel no utilizado directamente; podría retirarse o reutilizarse) -----
        JPanel headerPanel = new JPanel(new BorderLayout()); // Panel placeholder encabezados agrupados

        // Panel de encabezados agrupados (sin / con) --------------------
        JPanel groupHeaderPanel = new JPanel(new GridBagLayout()); // Panel grid para títulos agrupados
        GridBagConstraints gbcHeader = new GridBagConstraints();   // Restricciones header
        gbcHeader.fill = GridBagConstraints.HORIZONTAL;            // Expandir horizontal
        gbcHeader.insets = new Insets(2,0,2,0);                    // Márgenes

        // Encabezado vacío cubre columnas 0-1 (Año + Tamaño) -----------
        JLabel espacioLabel = new JLabel("");                       // Label vacío
        espacioLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.LIGHT_GRAY)); // Borde
        espacioLabel.setOpaque(true);                                 // Fondo visible

        // Encabezado escenario SIN nueva versión ----------------------
        JLabel sinVersionLabel = new JLabel("Sin la versión francesa", SwingConstants.CENTER); // Texto
        sinVersionLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.LIGHT_GRAY)); // Borde
        sinVersionLabel.setOpaque(true);                              // Fondo visible
        sinVersionLabel.setBackground(new Color(230,230,255));        // Color diferenciador
        EstilosUI.aplicarEstiloLabel(sinVersionLabel);                // Estilo tipográfico

        // Encabezado escenario CON nueva versión ----------------------
        JLabel conVersionLabel = new JLabel("Con versión francesa", SwingConstants.CENTER); // Texto
        conVersionLabel.setBorder(BorderFactory.createMatteBorder(1,1,1,1, Color.LIGHT_GRAY)); // Borde
        conVersionLabel.setOpaque(true);                              // Fondo visible
        conVersionLabel.setBackground(new Color(230,255,230));        // Color diferenciador
        EstilosUI.aplicarEstiloLabel(conVersionLabel);                // Estilo tipográfico

        // Colocación de encabezados agrupados -------------------------
        gbcHeader.gridx = 0; gbcHeader.gridy = 0; gbcHeader.gridwidth = 2; gbcHeader.weightx = 0.25; // Segmento inicial
        groupHeaderPanel.add(espacioLabel, gbcHeader);                // Añade placeholder
        gbcHeader.gridx = 2; gbcHeader.gridwidth = 3; gbcHeader.weightx = 0.375; // Segmento SIN
        groupHeaderPanel.add(sinVersionLabel, gbcHeader);             // Añade encabezado SIN
        gbcHeader.gridx = 5; gbcHeader.gridwidth = 3; gbcHeader.weightx = 0.375; // Segmento CON
        groupHeaderPanel.add(conVersionLabel, gbcHeader);             // Añade encabezado CON

        // Panel contenedor de tabla + encabezados ---------------------
        JPanel panelTabla = new JPanel(new BorderLayout()); // Panel tabla
        panelTabla.add(groupHeaderPanel, BorderLayout.NORTH); // Inserta encabezados agrupados
        JScrollPane scrollPane = new JScrollPane(tablaResultados);   // Scroll para tabla
        panelTabla.add(scrollPane, BorderLayout.CENTER);              // Añade tabla en centro

        // Inserta panel de tabla en layout principal ------------------
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 8;                // Nueva fila ocupa ancho completo
        gbc.weightx = 1.0; gbc.weighty = 1.0;                         // Prioridad de expansión
        gbc.fill = GridBagConstraints.BOTH;                           // Expandir ambos ejes
        add(panelTabla, gbc);                                         // Añade panelTabla

        // Panel info tasa de descuento --------------------------------
        gbc.gridy++; gbc.gridx = 0; gbc.gridwidth = 8;                // Fila siguiente
        gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weighty = 0.0;  // Sólo horizontal
        JPanel panelTasaDescuento = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Panel izquierda
        JLabel lblTasaDescuentoTitulo = new JLabel("Tasa de descuento: "); // Etiqueta titulo tasa
        EstilosUI.aplicarEstiloLabel(lblTasaDescuentoTitulo);        // Estilo
        lblTasaDescuentoValor = new JLabel("10%");                  // Valor inicial placeholder
        EstilosUI.aplicarEstiloLabel(lblTasaDescuentoValor);         // Estilo
        panelTasaDescuento.add(lblTasaDescuentoTitulo);              // Añade titulo
        panelTasaDescuento.add(lblTasaDescuentoValor);               // Añade valor
        add(panelTasaDescuento, gbc);                                // Añade panel tasa

        actualizarResultados(); // Primer cálculo y llenado de tabla
    }

    // Formatea un número como moneda usando utilidades centralizadas ----
    private String formatearMoneda(double valor) { return UtilidadesFormato.formatearMoneda(valor); }

    // Actualiza las etiquetas de VAN ------------------------------------
    private void actualizarEtiquetasVAN(double vanCon, double vanSin, double diferenciaVAN) {
        lblVANCon.setText(formatearMoneda(vanCon));       // VAN con
        lblVANSin.setText(formatearMoneda(vanSin));       // VAN sin
        lblDiferenciaVAN.setText(formatearMoneda(diferenciaVAN)); // Diferencia
    }

    // Limpia resultados (en caso de error) -------------------------------
    private void limpiarResultados() {
        lblVANCon.setText("-");  // Limpia VAN con
        lblVANSin.setText("-");  // Limpia VAN sin
        lblDiferenciaVAN.setText("-"); // Limpia diferencia
        modeloTabla.setRowCount(0); // Vacía tabla
    }

    // Recalcula y pinta la tabla completa -------------------------------
    private void actualizarResultados() {
        try { // Bloque protegido
            ControladorParametros params = ControladorParametros.getInstancia(); // Parámetros globales
            double tasaDescuento = params.getTasaDescuento(); // Tasa de descuento actual
            ModeloSoftwareCalculo.ResultadoComparativo resultados = ModeloSoftwareCalculo.calcularComparativo(params, tasaDescuento); // Cálculo

            modeloTabla.setRowCount(0); // Limpia filas previas
            for (int i = 0; i < resultados.resultadosCon.length; i++) { // Itera años
                ModeloSoftwareCalculo.ResultadoAnual rCon = resultados.resultadosCon[i]; // Registro escenario con
                ModeloSoftwareCalculo.ResultadoAnual rSin = resultados.resultadosSin[i]; // Registro escenario sin

                modeloTabla.addRow(new Object[]{ // Añade fila combinada
                    rCon.anio,                         // Año
                    rCon.demanda,                      // Tamaño mercado
                    rSin.unidadesProducidas,           // Unidades sin
                    formatearMoneda(rSin.costoVariableProduccion), // Coste variable sin
                    formatearMoneda(rSin.ingresosVentas),           // Ingresos sin
                    rCon.unidadesProducidas,           // Unidades con
                    formatearMoneda(rCon.costoVariableProduccion), // Coste variable con
                    formatearMoneda(rCon.ingresosVentas)           // Ingresos con
                });
            }

            actualizarEtiquetasVAN(resultados.vanCon, resultados.vanSin, resultados.diferenciaVAN); // Actualiza VANs
            lblTasaDescuentoValor.setText(String.format("%.0f%%", tasaDescuento * 100)); // Refresca tasa
        } catch (Exception ex) { // Cualquier error
            ex.printStackTrace(); // Traza (depuración)
            limpiarResultados();  // Limpia UI
        }
    }

    @Override public void onParametrosChanged() { // Notificación de cambios globales
        SwingUtilities.invokeLater(this::actualizarResultados); // Recalcula en EDT
    }

    @Override public void removeNotify() { // Al sacar el panel del contenedor
        ControladorParametros.getInstancia().removeChangeListener(this); // Se des-registra
        super.removeNotify(); // Llama a super
    }
}
