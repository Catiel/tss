package actividad_5.ventas; // Define el paquete donde se encuentra esta clase para el módulo de simulación de ventas

import javax.swing.*; // Importa todas las clases de Swing para crear la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de tablas
import java.awt.*; // Importa clases para manejo de componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel que genera números aleatorios para simular demanda diaria. */ // Comentario de documentación que explica el propósito del panel
public class PanelVentasAleatorio extends JPanel { // Declara la clase que extiende JPanel para crear un panel personalizado de simulación aleatoria de ventas
    private final JSpinner spDias; // Declara spinner para seleccionar el número de días a simular
    private final JButton btnSimular; // Declara el botón para ejecutar la simulación
    private final DefaultTableModel modeloDist; // Declara el modelo de datos para la tabla de distribución de probabilidades
    private final DefaultTableModel modeloSim; // Declara el modelo de datos para la tabla de resultados de simulación
    private final JTextArea resumen; // Declara el área de texto para mostrar el resumen de resultados

    public PanelVentasAleatorio() { // Constructor de la clase
        setLayout(new BorderLayout(8, 8)); // Establece el layout del panel con separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual al panel

        JLabel titulo = new JLabel("Simulación Ventas de Programas de Fútbol - Simulación aleatoria"); // Crea la etiqueta del título descriptivo
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo del título
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea el panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica el estilo al panel de controles
        controles.add(new JLabel("Número de días:")); // Agrega la etiqueta para el número de días
        spDias = new JSpinner(new SpinnerNumberModel(10, 1, 500, 1)); // Inicializa el spinner con valor inicial 10, mínimo 1, máximo 500, incremento 1
        controles.add(spDias); // Agrega el spinner al panel de controles
        btnSimular = new JButton("Ejecutar Simulación"); // Crea el botón para ejecutar la simulación
        EstilosUI.aplicarEstiloBoton(btnSimular); // Aplica el estilo al botón
        controles.add(btnSimular); // Agrega el botón al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior antes del contenido principal

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8)); // Crea el panel principal con layout BorderLayout y separación de 8 píxeles
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica el estilo al panel principal

        // Panel izquierdo: tabla de distribución (referencia)
        modeloDist = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de distribución con las columnas
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Programas vendidos" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
        };
        llenarDistribucion(); // Llama al método para llenar la tabla de distribución con datos

        JTable tDist = new JTable(modeloDist); // Crea la tabla de distribución usando el modelo
        EstilosUI.aplicarEstiloTabla(tDist); // Aplica el estilo a la tabla
        tDist.getTableHeader().setBackground(new Color(200, 240, 255)); // Establece el color de fondo del encabezado (azul claro)
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tDist.getTableHeader().setForeground(Color.BLACK); // Establece el color del texto del encabezado en negro
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece el ancho preferido de la primera columna (Probabilidad)
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece el ancho preferido de la segunda columna (Distribución acumulada)
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece el ancho preferido de la tercera columna (Rangos)
        tDist.getColumnModel().getColumn(3).setPreferredWidth(120); // Establece el ancho preferido de la cuarta columna (Programas vendidos)

        JScrollPane spDist = new JScrollPane(tDist); // Crea un panel de desplazamiento para la tabla de distribución
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de programas vendidos (referencia)")); // Establece un borde con título
        spDist.setPreferredSize(new Dimension(480, 280)); // Establece el tamaño preferido del panel de desplazamiento
        panelPrincipal.add(spDist, BorderLayout.WEST); // Agrega el panel de desplazamiento al lado oeste del panel principal

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{ // Inicializa el modelo de la tabla de simulación con las columnas
            "Día", "# Aleatorio", "Demanda", "Ganancia" // Define los nombres de las columnas
        }, 0) { // Establece 0 filas iniciales
            @Override
            public boolean isCellEditable(int r, int c) { // Sobrescribe el método para determinar si una celda es editable
                return false; // Hace que todas las celdas sean no editables
            }
            @Override
            public Class<?> getColumnClass(int c) { // Sobrescribe el método para definir el tipo de datos de cada columna
                if (c == 0 || c == 2 || c == 3) return Integer.class; // Primera, tercera y cuarta columna son Integer
                return String.class; // La segunda columna (números aleatorios) es String
            }
        };

        JTable tablaSim = new JTable(modeloSim) { // Crea la tabla de simulación con renderizado personalizado
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) { // Sobrescribe el método para personalizar la apariencia de las celdas
                Component c = super.prepareRenderer(renderer, row, column); // Obtiene el componente renderizado por defecto
                if (row >= getRowCount() - 2) { // Si es una de las últimas dos filas (totales y promedio)
                    c.setBackground(new Color(220, 220, 220)); // Establece color de fondo gris para las filas de resumen
                } else if (row % 2 == 0) { // Si es una fila par
                    c.setBackground(Color.WHITE); // Establece color de fondo blanco
                } else { // Si es una fila impar
                    c.setBackground(new Color(245, 245, 245)); // Establece color de fondo gris claro
                }
                return c; // Retorna el componente con el color aplicado
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica el estilo a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionamiento automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(50); // Establece el ancho preferido de la primera columna (Día)
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80); // Establece el ancho preferido de la segunda columna (# Aleatorio)
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(80); // Establece el ancho preferido de la tercera columna (Demanda)
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(80); // Establece el ancho preferido de la cuarta columna (Ganancia)

        JScrollPane spSim = new JScrollPane(tablaSim); // Crea un panel de desplazamiento para la tabla de simulación
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación")); // Establece un borde con título
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el panel de desplazamiento al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel actual

        // Área de resumen
        resumen = new JTextArea(); // Crea el área de texto para el resumen
        resumen.setEditable(false); // Hace el área de texto no editable
        resumen.setBackground(getBackground()); // Establece el mismo color de fondo que el panel
        resumen.setLineWrap(true); // Habilita el ajuste automático de línea
        resumen.setWrapStyleWord(true); // Habilita el ajuste de línea por palabra
        resumen.setBorder(BorderFactory.createTitledBorder("Análisis de resultados")); // Establece un borde con título
        resumen.setFont(new Font("Arial", Font.PLAIN, 12)); // Establece la fuente en Arial, normal, tamaño 12
        resumen.setPreferredSize(new Dimension(0, 120)); // Establece el tamaño preferido del área de resumen
        add(resumen, BorderLayout.SOUTH); // Agrega el área de resumen en la parte inferior

        btnSimular.addActionListener(this::simular); // Agrega un listener al botón que ejecuta el método simular

        // Ejecutar simulación inicial
        simular(null); // Ejecuta una simulación inicial al cargar el panel
    }

    private void llenarDistribucion() { // Método para llenar la tabla de distribución con datos del modelo
        modeloDist.setRowCount(0); // Limpia todas las filas de la tabla
        double[][] rangos = VentasModelo.getRangos(); // Obtiene los rangos de números aleatorios del modelo

        for (int i = 0; i < VentasModelo.DEMANDA.length; i++) { // Itera sobre cada valor de demanda en el modelo
            String rango = UtilFormatoVentas.f2(rangos[i][0]) + " - " + UtilFormatoVentas.f2(rangos[i][1]); // Formatea el rango como string con 2 decimales
            modeloDist.addRow(new Object[]{ // Agrega una nueva fila a la tabla
                UtilFormatoVentas.f2(VentasModelo.PROBS[i]), // Probabilidad formateada con 2 decimales
                UtilFormatoVentas.f2(getSumaAcumulada(VentasModelo.PROBS, i)), // Distribución acumulada formateada
                rango, // Rango de números aleatorios
                VentasModelo.DEMANDA[i] // Valor de la demanda (programas vendidos)
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

    private void simular(ActionEvent e) { // Método que se ejecuta cuando se presiona el botón simular
        int dias = (int) spDias.getValue(); // Obtiene el número de días seleccionado del spinner
        modeloSim.setRowCount(0); // Limpia todas las filas de la tabla de simulación

        int totalDemanda = 0; // Inicializa el acumulador de demanda total
        int totalGanancia = 0; // Inicializa el acumulador de ganancia total

        for (int d = 1; d <= dias; d++) { // Itera sobre cada día de la simulación
            double r = Math.random(); // Genera un número aleatorio entre 0.0 y 1.0
            int demanda = VentasModelo.demandaPara(r); // Convierte el número aleatorio en demanda usando el modelo
            int ganancia = VentasModelo.gananciaParaDemanda(demanda); // Calcula la ganancia correspondiente a la demanda

            totalDemanda += demanda; // Acumula la demanda diaria al total
            totalGanancia += ganancia; // Acumula la ganancia diaria al total

            modeloSim.addRow(new Object[]{ // Agrega una nueva fila con los resultados del día
                d, // Número del día
                UtilFormatoVentas.f2(r), // Número aleatorio formateado con 2 decimales
                demanda, // Demanda calculada (programas vendidos)
                ganancia // Ganancia calculada
            });
        }

        // Fila de totales
        modeloSim.addRow(new Object[]{ // Agrega la fila de totales
            "TOTALES", // Etiqueta para la fila de totales
            "", // Columna vacía
            totalDemanda, // Total de demanda acumulada
            totalGanancia // Total de ganancia acumulada
        });

        // Fila de promedio
        double promDemanda = totalDemanda / (double) dias; // Calcula el promedio de demanda dividiendo total entre días
        double promGanancia = totalGanancia / (double) dias; // Calcula el promedio de ganancia dividiendo total entre días
        modeloSim.addRow(new Object[]{ // Agrega la fila de promedios
            "PROMEDIO", // Etiqueta para la fila de promedio
            "", // Columna vacía
            UtilFormatoVentas.f2(promDemanda), // Promedio de demanda formateado con 2 decimales
            UtilFormatoVentas.f2(promGanancia) // Promedio de ganancia formateado con 2 decimales
        });

        // Actualizar resumen
        actualizarResumen(dias, totalDemanda, totalGanancia, promDemanda, promGanancia); // Llama al método para actualizar el área de resumen con los resultados
    }

    private void actualizarResumen(int dias, int totalDemanda, int totalGanancia, double promDemanda, double promGanancia) { // Método para actualizar el área de resumen con estadísticas de la simulación
        StringBuilder sb = new StringBuilder(); // Crea un constructor de cadenas para armar el texto del resumen
        sb.append("RESULTADOS DE LA SIMULACIÓN ALEATORIA (").append(dias).append(" días):\n\n"); // Agrega el encabezado con el número de días simulados

        sb.append("DEMANDA:\n"); // Agrega la sección de demanda
        sb.append("• Total programas vendidos: ").append(totalDemanda).append(" programas\n"); // Agrega el total de programas vendidos
        sb.append("• Promedio diario simulado: ").append(UtilFormatoVentas.f2(promDemanda)).append(" programas\n"); // Agrega el promedio diario de programas

        sb.append("GANANCIAS:\n"); // Agrega la sección de ganancias
        sb.append("• Total ganancia: $").append(totalGanancia).append("\n"); // Agrega la ganancia total con símbolo de dólar
        sb.append("• Ganancia promedio diaria: $").append(UtilFormatoVentas.f2(promGanancia)).append("\n"); // Agrega el promedio de ganancia diaria

        resumen.setText(sb.toString()); // Establece el texto del área de resumen con el contenido construido
    }
}