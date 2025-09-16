package actividad_5.dulce; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts

/** Panel con los datos predefinidos del Excel - 100 números aleatorios fijos */ // Comentario de documentación de la clase
public class PanelDulcePredefinido extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado
    private final DefaultTableModel modeloDistrib; // Declara el modelo de datos para la tabla de distribución de probabilidades
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla de simulación
    private final DefaultTableModel modeloComparacion; // Declara el modelo de datos para la tabla de comparación de ganancias

    public PanelDulcePredefinido() { // Constructor de la clase
        setLayout(new BorderLayout(8, 8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel

        JLabel titulo = new JLabel("Dulce Ada - Ejemplo predefinido (100 réplicas)"); // Crea la etiqueta del título
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8)); // Crea el panel principal con layout BorderLayout y separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica el estilo al panel principal

        // Panel izquierdo: distribución y comparación
        JPanel panelIzq = new JPanel(); // Crea el panel izquierdo
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS)); // Establece layout vertical (BoxLayout en eje Y)
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica el estilo al panel izquierdo

        // Tabla de distribución
        modeloDistrib = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de distribución con las columnas
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Demanda" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
        };
        llenarDistribucion(); // Llama al método para llenar la tabla de distribución con datos

        JTable tDist = new JTable(modeloDistrib); // Crea la tabla de distribución usando el modelo
        EstilosUI.aplicarEstiloTabla(tDist); // Aplica el estilo a la tabla
        tDist.getTableHeader().setBackground(new Color(200, 240, 255)); // Establece el color de fondo del encabezado (azul claro)
        tDist.getTableHeader().setForeground(Color.BLACK); // Establece el color del texto del encabezado en negro
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece el ancho preferido de la primera columna (Probabilidad)
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece el ancho preferido de la segunda columna (Distribución acumulada)
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece el ancho preferido de la tercera columna (Rangos)
        tDist.getColumnModel().getColumn(3).setPreferredWidth(70); // Establece el ancho preferido de la cuarta columna (Demanda)

        JScrollPane spDist = new JScrollPane(tDist); // Crea un panel de desplazamiento para la tabla de distribución
        spDist.setBorder(BorderFactory.createTitledBorder("Tabla de distribución de probabilidades de la demanda")); // Establece un borde con título
        spDist.setPreferredSize(new Dimension(430, 220)); // Establece el tamaño preferido del panel de desplazamiento
        panelIzq.add(spDist); // Agrega el panel de desplazamiento al panel izquierdo

        panelIzq.add(Box.createVerticalStrut(8)); // Agrega un espaciador vertical de 8 píxeles

        // Tabla de comparación de ganancias generales
        modeloComparacion = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de comparación
            "Compra", "Ganancia promedio" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
        };
        llenarComparacion(); // Llama al método para llenar la tabla de comparación con datos

        JTable tablaComp = new JTable(modeloComparacion); // Crea la tabla de comparación usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaComp); // Aplica el estilo a la tabla
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70); // Establece el ancho preferido de la primera columna (Compra)
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130); // Establece el ancho preferido de la segunda columna (Ganancia promedio)
        JScrollPane spComp = new JScrollPane(tablaComp); // Crea un panel de desplazamiento para la tabla de comparación
        spComp.setBorder(BorderFactory.createTitledBorder("Ganancias generales de la simulación dulce hada")); // Establece un borde con título
        spComp.setPreferredSize(new Dimension(210, 220)); // Establece el tamaño preferido del panel de desplazamiento
        panelIzq.add(spComp); // Agrega el panel de desplazamiento al panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo al lado oeste del panel principal

        // Panel derecho: tabla de simulación (con Q=60 por defecto)
        modeloSim = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de simulación
            "Replica", "# Aleatorio", "Demanda", "Ganancia" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
            @Override
            public Class<?> getColumnClass(int c) { // Sobrescribe el método para definir el tipo de datos de cada columna
                if (c == 0 || c == 2) return Integer.class; // Primera y tercera columna son Integer
                return String.class; // Las demás columnas son String
            }
        };

        JTable tSim = new JTable(modeloSim) { // Crea la tabla de simulación con renderizado personalizado
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) { // Sobrescribe el método para personalizar la apariencia de las celdas
                Component c = super.prepareRenderer(renderer, row, column); // Obtiene el componente renderizado por defecto
                if (row == getRowCount() - 1) { // Si es la última fila (fila de promedio)
                    c.setBackground(new Color(220, 220, 220)); // Establece color de fondo gris para la fila de promedio
                } else if (row % 2 == 0) { // Si es una fila par
                    c.setBackground(Color.WHITE); // Establece color de fondo blanco
                } else { // Si es una fila impar
                    c.setBackground(new Color(245, 245, 245)); // Establece color de fondo gris claro
                }
                return c; // Retorna el componente con el color aplicado
            }
        };

        EstilosUI.aplicarEstiloTabla(tSim); // Aplica el estilo a la tabla de simulación
        tSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tSim.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece el ancho preferido de la primera columna (Replica)
        tSim.getColumnModel().getColumn(1).setPreferredWidth(80); // Establece el ancho preferido de la segunda columna (# Aleatorio)
        tSim.getColumnModel().getColumn(2).setPreferredWidth(70); // Establece el ancho preferido de la tercera columna (Demanda)
        tSim.getColumnModel().getColumn(3).setPreferredWidth(90); // Establece el ancho preferido de la cuarta columna (Ganancia)

        simular(); // Llama al método para ejecutar la simulación y llenar la tabla
        JScrollPane spSim = new JScrollPane(tSim); // Crea un panel de desplazamiento para la tabla de simulación
        spSim.setBorder(BorderFactory.createTitledBorder("Tabla de simulación (Variable de decisión: Cantidad comprada = 60)")); // Establece un borde con título descriptivo
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el panel de desplazamiento al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel actual

        // Panel inferior para mostrar el promedio
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Crea el panel inferior con layout de flujo centrado
        EstilosUI.aplicarEstiloPanel(panelInferior); // Aplica el estilo al panel inferior
        JLabel lblPromedio = new JLabel(); // Crea la etiqueta para mostrar el promedio
        EstilosUI.aplicarEstiloLabel(lblPromedio); // Aplica el estilo a la etiqueta
        lblPromedio.setFont(new Font("Arial", Font.BOLD, 14)); // Establece la fuente en Arial, negrita, tamaño 14
        lblPromedio.setForeground(new Color(0, 100, 0)); // Establece el color del texto en verde oscuro

        // Calcular y mostrar el promedio
        double[] ganancias = DulceModelo.simularGanancias(60, DulceModelo.RAND_FIJOS); // Simula las ganancias para Q=60 usando números aleatorios predefinidos
        double promedio = DulceModelo.promedio(ganancias); // Calcula el promedio de las ganancias
        lblPromedio.setText("Ganancia promedio para Q=60: " + UtilFormatoDulce.m2(promedio)); // Establece el texto de la etiqueta con el promedio formateado

        panelInferior.add(lblPromedio); // Agrega la etiqueta al panel inferior
        add(panelInferior, BorderLayout.SOUTH); // Agrega el panel inferior en la parte sur del panel actual
    }

    private void llenarDistribucion() { // Método para llenar la tabla de distribución con datos del modelo
        modeloDistrib.setRowCount(0); // Limpia todas las filas de la tabla
        double[][] rangos = DulceModelo.getRangos(); // Obtiene los rangos de números aleatorios del modelo

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) { // Itera sobre cada demanda en el modelo
            String rango = UtilFormatoDulce.p4(rangos[i][0]) + " - " + UtilFormatoDulce.p4(rangos[i][1]); // Formatea el rango como string con 4 decimales
            modeloDistrib.addRow(new Object[]{ // Agrega una nueva fila a la tabla
                UtilFormatoDulce.p4(DulceModelo.PROB[i]), // Probabilidad formateada con 4 decimales
                UtilFormatoDulce.p4(getSumaAcumulada(DulceModelo.PROB, i)), // Distribución acumulada formateada
                rango, // Rango de números aleatorios
                DulceModelo.DEMANDAS[i] // Valor de la demanda
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta) { // Método para calcular la suma acumulada de probabilidades hasta un índice dado
        double suma = 0; // Inicializa la suma en 0
        for (int i = 0; i <= hasta; i++) { // Itera desde 0 hasta el índice especificado (inclusive)
            suma += probs[i]; // Suma la probabilidad del índice actual
        }
        return suma; // Retorna la suma acumulada
    }

    private void llenarComparacion() { // Método para llenar la tabla de comparación con diferentes valores de Q
        modeloComparacion.setRowCount(0); // Limpia todas las filas de la tabla comparativa
        int[] decisiones = {40, 50, 60, 70, 80, 90}; // Define los valores de decisión Q a comparar

        for (int Q : decisiones) { // Itera sobre cada decisión Q
            double[] ganancias = DulceModelo.simularGanancias(Q, DulceModelo.RAND_FIJOS); // Simula las ganancias para el valor Q actual usando números predefinidos
            double promedio = DulceModelo.promedio(ganancias); // Calcula el promedio de las ganancias
            modeloComparacion.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)}); // Agrega fila con el valor Q y su promedio de ganancia formateado
        }
    }

    private void simular() { // Método para ejecutar la simulación y llenar la tabla de resultados
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación
        int Q = 60; // Establece la cantidad fija para la tabla de simulación
        double suma = 0; // Inicializa la suma de ganancias en 0

        for (int i = 0; i < DulceModelo.RAND_FIJOS.length; i++) { // Itera sobre cada número aleatorio predefinido
            double r = DulceModelo.RAND_FIJOS[i]; // Obtiene el número aleatorio actual
            int demanda = DulceModelo.demandaPara(r); // Calcula la demanda correspondiente al número aleatorio
            double ganancia = DulceModelo.ganancia(Q, demanda); // Calcula la ganancia para la cantidad Q y demanda
            suma += ganancia; // Acumula la ganancia en la suma total

            modeloSim.addRow(new Object[]{ // Agrega una nueva fila a la tabla de simulación
                i + 1, // Número de réplica
                UtilFormatoDulce.p4(r), // Número aleatorio formateado con 4 decimales
                demanda, // Valor de la demanda
                UtilFormatoDulce.m2(ganancia) // Ganancia formateada como moneda con 2 decimales
            });
        }

        // Fila de promedio con mejor formato
        double promedio = suma / DulceModelo.RAND_FIJOS.length; // Calcula el promedio de ganancias dividiendo la suma total entre el número de réplicas
        modeloSim.addRow(new Object[]{ // Agrega la fila de totales/promedio
            "TOTAL/PROMEDIO", // Etiqueta para la fila de resumen
            "", // Columna vacía
            "", // Columna vacía
            UtilFormatoDulce.m2(promedio) // Promedio formateado como moneda
        });
    }
}