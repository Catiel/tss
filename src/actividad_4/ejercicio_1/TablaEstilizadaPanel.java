package actividad_4.ejercicio_1; // Paquete del ejercicio 1

import javax.swing.*; // Componentes Swing (JPanel, JTable, JScrollPane, JLabel)
import javax.swing.table.DefaultTableModel; // Modelo de tabla por defecto
import java.awt.*; // Layouts, Color, Font y demás utilidades AWT

public abstract class TablaEstilizadaPanel extends JPanel { // Clase abstracta base para paneles con tabla estilizada
    protected JTable tabla;                 // Referencia a la JTable que muestra los datos
    protected DefaultTableModel modeloTabla; // Modelo de datos de la tabla
    protected JLabel lblOptimo;             // Etiqueta que muestra métricas óptimas calculadas
    protected int filaOptima = -1;          // Índice de la fila con mejor ganancia (si no existe => -1)
    protected int filaOptimaVan = -1;       // Índice de la fila con mejor VAN (si no existe => -1)
    protected double mejorCapacidadVan = -1; // Capacidad asociada al mejor VAN
    protected double mejorVan = Double.NEGATIVE_INFINITY; // Valor máximo de VAN encontrado (inicializado muy bajo)

    /**
     * Constructor del panel de tabla estilizada.
     * Configura la interfaz, el estilo y los componentes para mostrar la tabla de resultados.
     * Permite agregar paneles superiores/inferiores personalizados.
     * @param titulo Título del panel.
     * @param modeloTabla Modelo de datos de la tabla.
     * @param panelSuperior Panel adicional en la parte superior (puede ser null).
     * @param panelInferior Panel adicional en la parte inferior (puede ser null).
     */
    public TablaEstilizadaPanel(String titulo, DefaultTableModel modeloTabla, JPanel panelSuperior, JPanel panelInferior) { // Constructor
        EstilosUI.aplicarEstiloPanel(this);        // Aplica estilo común de fondo
        setLayout(new BorderLayout(10, 10));       // Usa BorderLayout con márgenes entre regiones
        JLabel lblTitulo = new JLabel(titulo);     // Crea etiqueta de título
        EstilosUI.aplicarEstiloTitulo(lblTitulo);  // Aplica estilo de título (fuente, tamaño, etc.)
        lblTitulo.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen interno del título
        add(lblTitulo, BorderLayout.NORTH);        // Añade el título en la parte superior
        if (panelSuperior != null) {               // Si se proporciona panel superior adicional
            add(panelSuperior, BorderLayout.BEFORE_FIRST_LINE); // Lo añade antes de la primera línea (zona superior)
        }
        this.modeloTabla = modeloTabla;            // Asigna el modelo recibido a la variable de instancia
        tabla = new JTable(modeloTabla) {          // Crea JTable anónima para personalizar renderizado
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) { // Sobrescribe renderizado de celdas
                Component c = super.prepareRenderer(renderer, row, column); // Obtiene componente base
                if (row == filaOptima && filaOptima >= 0) {            // Si es la fila óptima de ganancia
                    c.setBackground(new Color(180, 255, 180));         // Fondo verde claro
                } else if (row == filaOptimaVan && filaOptimaVan >= 0) { // Si es la fila óptima de VAN
                    c.setBackground(new Color(180, 220, 255));         // Fondo azul claro
                } else if (column == 0) {                              // Primera columna (categorías / capacidad)
                    c.setBackground(new Color(255, 255, 230));         // Amarillo muy suave
                } else if (column == 1) {                              // Segunda columna (puede ser cabecera de subgrupo)
                    c.setBackground(new Color(235, 245, 255));         // Azul muy suave
                    if (c instanceof JLabel) {                         // Si el renderer devuelve JLabel
                        c.setFont(new Font("Segoe UI", Font.BOLD, 13)); // Aplica negrita ligera
                    }
                } else {                                               // Resto de celdas
                    c.setBackground(Color.WHITE);                      // Fondo blanco estándar
                }
                return c;                                              // Retorna componente estilizado
            }
        };
        EstilosUI.aplicarEstiloTabla(tabla);       // Aplica estilo general (fuentes, cabecera)
        tabla.setBorder(BorderFactory.createLineBorder(new Color(200, 220, 240))); // Borde exterior de la tabla
        JScrollPane scroll = new JScrollPane(tabla); // Scroll para manejar muchas filas
        scroll.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10)); // Margen del scroll pane
        add(scroll, BorderLayout.CENTER);          // Añade la tabla (con scroll) al centro
        lblOptimo = new JLabel("Mejor capacidad: - | Ganancia máxima: - | Mejor VAN: - | Capacidad VAN: -"); // Texto por defecto
        EstilosUI.aplicarEstiloLabel(lblOptimo);   // Aplica estilo a la etiqueta
        lblOptimo.setFont(new Font("Segoe UI", Font.BOLD, 16)); // Ajusta fuente a negrita y mayor tamaño
        if (panelInferior == null) {               // Si no se proporciona panel inferior personalizado
            JPanel panelInferiorDefault = new JPanel(new BorderLayout()); // Crea panel contenedor inferior
            EstilosUI.aplicarEstiloPanel(panelInferiorDefault); // Aplica estilo fondo
            panelInferiorDefault.setBorder(BorderFactory.createCompoundBorder( // Borde compuesto (línea + margen interno)
                BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(200, 220, 240)), // Línea superior fina
                BorderFactory.createEmptyBorder(10, 10, 10, 10)));                     // Margen interno
            panelInferiorDefault.add(lblOptimo, BorderLayout.CENTER); // Coloca etiqueta de métricas
            add(panelInferiorDefault, BorderLayout.SOUTH);            // Añade al sur
        } else {                                                     // Si hay panel personalizado
            add(panelInferior, BorderLayout.SOUTH);                  // Se añade directamente
        }
    }

    /**
     * Actualiza los indicadores óptimos y el resaltado de filas en la tabla.
     * Muestra la mejor capacidad y ganancia máxima, así como el mejor VAN y su capacidad.
     * @param mejorCapacidad Capacidad con mayor ganancia.
     * @param mejorGanancia Valor de la ganancia máxima.
     * @param filaOptima Índice de la fila óptima de ganancia.
     * @param mejorCapacidadVan Capacidad con mayor VAN.
     * @param mejorVan Valor máximo de VAN.
     * @param filaOptimaVan Índice de la fila óptima de VAN.
     */
    public void actualizarOptimo(double mejorCapacidad, double mejorGanancia, int filaOptima, double mejorCapacidadVan, double mejorVan, int filaOptimaVan) { // Actualiza métricas
        this.filaOptima = filaOptima;                 // Guarda índice fila óptima ganancia
        this.filaOptimaVan = filaOptimaVan;           // Guarda índice fila óptima VAN
        this.mejorCapacidadVan = mejorCapacidadVan;   // Guarda capacidad asociada a VAN máximo
        this.mejorVan = mejorVan;                     // Guarda valor máximo de VAN
        lblOptimo.setText(                            // Construye texto descriptivo
            "Mejor capacidad: " + (filaOptima >= 0 ? String.format("%.2f", mejorCapacidad) : "-") +
            " | Ganancia máxima: " + (filaOptima >= 0 ? String.format("$%,.0f", mejorGanancia) : "-") +
            " | Mejor VAN: " + (filaOptimaVan >= 0 ? String.format("$%,.0f", mejorVan) : "-") +
            " | Capacidad VAN: " + (filaOptimaVan >= 0 ? String.format("%.2f", mejorCapacidadVan) : "-")
        );
        tabla.repaint(); // Solicita repintado para aplicar nuevos resaltados de filas
    }
}
