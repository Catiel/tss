package actividad_5.dulce; // Define el paquete donde se encuentra esta clase

import javax.swing.*; // Importa todas las clases de Swing para componentes de interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de datos para tablas
import java.awt.*; // Importa clases de AWT para layouts y colores
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

/** Panel que genera números aleatorios para simular demanda */
public class PanelDulceAleatorio extends JPanel { // Clase que extiende JPanel para crear un panel de simulación aleatoria de maximización de ganancias de Dulce Ada
    private final JSpinner spDias; // Control spinner para seleccionar el número de réplicas/simulaciones a ejecutar
    private final JButton btnSimular; // Botón que inicia el proceso de simulación aleatoria
    private final DefaultTableModel modeloDist; // Modelo para la tabla que muestra la distribución de probabilidades de demanda (referencia)
    private final DefaultTableModel modeloSim; // Modelo para la tabla que muestra los resultados detallados de la simulación
    private final DefaultTableModel modeloComp; // Modelo para la tabla que compara diferentes valores de Q y sus ganancias promedio

    private double[] randoms; // números aleatorios generados - Array que almacena los números aleatorios generados para la simulación actual

    private final JTextField txtValoresQ; // Campo de texto donde el usuario puede especificar valores de Q a comparar (separados por coma)
    private JLabel lblPromedio; // Para mostrar el promedio en el panel inferior - Etiqueta que muestra la ganancia promedio para Q=60

    public PanelDulceAleatorio() { // Constructor que inicializa todo el panel de simulación aleatoria de Dulce Ada
        setLayout(new BorderLayout(8, 8)); // Establece el layout BorderLayout con espaciado de 8 píxeles horizontal y vertical
        EstilosUI.aplicarEstiloPanel(this); // Aplica el estilo visual predefinido al panel

        JLabel titulo = new JLabel("Dulce Ada - Simulación aleatoria"); // Crea la etiqueta del título específica para simulación aleatoria de Dulce Ada
        EstilosUI.aplicarEstiloTitulo(titulo); // Aplica el estilo específico para títulos
        add(titulo, BorderLayout.NORTH); // Agrega el título en la parte superior del panel

        // Controles superiores
        JPanel controles = new JPanel(new FlowLayout(FlowLayout.LEFT)); // Crea panel de controles con layout de flujo alineado a la izquierda
        EstilosUI.aplicarEstiloPanel(controles); // Aplica estilo al panel de controles
        controles.add(new JLabel("Número de réplicas:")); // Agrega etiqueta para el control del número de simulaciones
        spDias = new JSpinner(new SpinnerNumberModel(100, 10, 500, 10)); // Inicializa spinner con valor inicial 100, mínimo 10, máximo 500, incremento 10
        controles.add(spDias); // Agrega el spinner de réplicas al panel de controles
        controles.add(new JLabel("Valores Q a comparar (separados por coma):")); // Agrega etiqueta para el campo de valores Q a comparar
        txtValoresQ = new JTextField("40,50,60,70,80,90", 15); // Inicializa campo de texto con valores predeterminados de demanda y ancho de 15 caracteres
        controles.add(txtValoresQ); // Agrega el campo de texto al panel de controles
        btnSimular = new JButton("Ejecutar Simulación"); // Crea el botón para ejecutar la simulación aleatoria
        EstilosUI.aplicarEstiloBoton(btnSimular); // Aplica estilo predefinido al botón
        controles.add(btnSimular); // Agrega el botón al panel de controles
        add(controles, BorderLayout.BEFORE_FIRST_LINE); // Agrega el panel de controles en la parte superior

        // Panel principal dividido
        JPanel panelPrincipal = new JPanel(new BorderLayout(8, 8)); // Crea el panel principal con BorderLayout y espaciado
        EstilosUI.aplicarEstiloPanel(panelPrincipal); // Aplica estilo al panel principal

        // Panel izquierdo: distribución y comparación
        JPanel panelIzq = new JPanel(); // Crea panel izquierdo que contendrá dos tablas apiladas verticalmente
        panelIzq.setLayout(new BoxLayout(panelIzq, BoxLayout.Y_AXIS)); // Establece layout vertical (Y_AXIS) para apilar componentes de arriba abajo
        EstilosUI.aplicarEstiloPanel(panelIzq); // Aplica estilo al panel izquierdo

        // Tabla de distribución (referencia)
        modeloDist = new DefaultTableModel(new Object[]{ // Crea modelo de tabla no editable para mostrar distribución de probabilidades
            "Probabilidad", "Distribución acumulada", "Rangos de #s aleatorios", "Demanda"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { // Override para hacer toda la tabla de solo lectura
                return false;
            }
        };
        llenarDistribucion(); // Llama al método para poblar la tabla de distribución con datos del modelo

        JTable tDist = new JTable(modeloDist); // Crea la tabla de distribución usando el modelo
        EstilosUI.aplicarEstiloTabla(tDist); // Aplica estilo predefinido a la tabla de distribución
        tDist.getTableHeader().setBackground(new Color(200, 240, 255)); // Establece color azul claro para el encabezado de la tabla de distribución
        tDist.getTableHeader().setForeground(Color.BLACK); // Establece color negro para el texto del encabezado
        tDist.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva el redimensionado automático de columnas
        tDist.getColumnModel().getColumn(0).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Probabilidad"
        tDist.getColumnModel().getColumn(1).setPreferredWidth(120); // Establece ancho de 120 píxeles para columna "Distribución acumulada"
        tDist.getColumnModel().getColumn(2).setPreferredWidth(140); // Establece ancho de 140 píxeles para columna "Rangos de #s aleatorios"
        tDist.getColumnModel().getColumn(3).setPreferredWidth(70); // Establece ancho de 70 píxeles para columna "Demanda"

        JScrollPane spDist = new JScrollPane(tDist); // Envuelve la tabla de distribución en un scroll pane
        spDist.setBorder(BorderFactory.createTitledBorder("Distribución de probabilidades (referencia)")); // Agrega borde con título explicativo
        spDist.setPreferredSize(new Dimension(430, 220)); // Establece tamaño preferido del scroll pane de distribución
        panelIzq.add(spDist); // Agrega el scroll pane de distribución a la parte superior del panel izquierdo

        panelIzq.add(Box.createVerticalStrut(8)); // Agrega un espacio vertical de 8 píxeles entre las dos tablas del panel izquierdo

        // Tabla de comparación de decisiones
        modeloComp = new DefaultTableModel(new Object[]{ // Crea modelo de tabla no editable para comparar diferentes valores de Q
            "Compra", "Ganancia promedio"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { // Override para hacer toda la tabla de solo lectura
                return false;
            }
        };

        JTable tablaComp = new JTable(modeloComp); // Crea la tabla de comparación usando el modelo
        EstilosUI.aplicarEstiloTabla(tablaComp); // Aplica estilo predefinido a la tabla de comparación
        tablaComp.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaComp.getColumnModel().getColumn(0).setPreferredWidth(70); // Establece ancho de 70 píxeles para columna "Compra"
        tablaComp.getColumnModel().getColumn(1).setPreferredWidth(130); // Establece ancho de 130 píxeles para columna "Ganancia promedio"
        JScrollPane spComp = new JScrollPane(tablaComp); // Envuelve la tabla de comparación en un scroll pane
        spComp.setBorder(BorderFactory.createTitledBorder("Comparación de decisiones")); // Agrega borde con título
        spComp.setPreferredSize(new Dimension(210, 220)); // Establece tamaño preferido del scroll pane de comparación
        panelIzq.add(spComp); // Agrega el scroll pane de comparación a la parte inferior del panel izquierdo

        panelPrincipal.add(panelIzq, BorderLayout.WEST); // Agrega el panel izquierdo (con las 2 tablas apiladas) al lado oeste del panel principal

        // Panel derecho: tabla de simulación
        modeloSim = new DefaultTableModel(new Object[]{ // Crea modelo para la tabla de simulación con 4 columnas para mostrar resultados detallados
            "Replica", "# Aleatorio", "Demanda", "Ganancia"
        }, 0) {
            @Override
            public boolean isCellEditable(int r, int c) { // Override para hacer toda la tabla de solo lectura
                return false;
            }
            @Override
            public Class<?> getColumnClass(int c) { // Override para especificar el tipo de datos de cada columna
                if (c == 0 || c == 2) return Integer.class; // Columnas 0 (Replica) y 2 (Demanda) son de tipo Integer
                return String.class; // Las columnas 1 (# Aleatorio) y 3 (Ganancia) son de tipo String (formateadas)
            }
        };

        JTable tablaSim = new JTable(modeloSim) { // Crea la tabla de simulación con comportamiento personalizado de renderizado
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) { // Override para personalizar el renderizado de celdas
                Component c = super.prepareRenderer(renderer, row, column); // Obtiene el componente renderizado por defecto
                if (row == getRowCount() - 1) { // fila de promedio - Verifica si es la última fila (fila de totales/promedio)
                    c.setBackground(new Color(220, 220, 220)); // Establece fondo gris para fila de promedio
                } else if (row % 2 == 0) { // Si es fila par de datos normales
                    c.setBackground(Color.WHITE); // Establece fondo blanco para filas pares
                } else { // Si es fila impar de datos normales
                    c.setBackground(new Color(245, 245, 245)); // Establece fondo gris claro para filas impares (patrón zebra)
                }
                return c; // Retorna el componente con el color de fondo apropiado
            }
        };

        EstilosUI.aplicarEstiloTabla(tablaSim); // Aplica estilo predefinido a la tabla de simulación
        tablaSim.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); // Desactiva redimensionado automático de columnas
        tablaSim.getColumnModel().getColumn(0).setPreferredWidth(60); // Establece ancho de 60 píxeles para columna "Replica"
        tablaSim.getColumnModel().getColumn(1).setPreferredWidth(80); // Establece ancho de 80 píxeles para columna "# Aleatorio"
        tablaSim.getColumnModel().getColumn(2).setPreferredWidth(70); // Establece ancho de 70 píxeles para columna "Demanda"
        tablaSim.getColumnModel().getColumn(3).setPreferredWidth(90); // Establece ancho de 90 píxeles para columna "Ganancia"

        JScrollPane spSim = new JScrollPane(tablaSim); // Envuelve la tabla de simulación en un scroll pane
        spSim.setBorder(BorderFactory.createTitledBorder("Resultados de la simulación (Cantidad comprada = 60)")); // Agrega borde con título que indica Q=60 por defecto
        panelPrincipal.add(spSim, BorderLayout.CENTER); // Agrega el scroll pane de simulación al centro del panel principal

        add(panelPrincipal, BorderLayout.CENTER); // Agrega el panel principal al centro del panel general

        // Panel inferior para mostrar el promedio
        JPanel panelInferior = new JPanel(new FlowLayout(FlowLayout.CENTER)); // Crea panel inferior con layout de flujo centrado
        EstilosUI.aplicarEstiloPanel(panelInferior); // Aplica estilo al panel inferior
        lblPromedio = new JLabel(); // Crea la etiqueta para mostrar el promedio de ganancias
        EstilosUI.aplicarEstiloLabel(lblPromedio); // Aplica estilo predefinido a la etiqueta
        lblPromedio.setFont(new Font("Arial", Font.BOLD, 14)); // Establece fuente Arial, negrita, tamaño 14 para destacar el promedio
        lblPromedio.setForeground(new Color(0, 100, 0)); // Verde oscuro - Establece color verde oscuro para el texto del promedio
        panelInferior.add(lblPromedio); // Agrega la etiqueta del promedio al panel inferior
        add(panelInferior, BorderLayout.SOUTH); // Agrega el panel inferior en la parte inferior del panel general

        btnSimular.addActionListener(this::simular); // Asocia el método simular como listener del evento de clic del botón

        // Ejecutar simulación inicial
        simular(null); // Ejecuta una simulación inicial al crear el panel para mostrar datos inmediatamente
    }

    private void llenarDistribucion() { // Método que llena la tabla de distribución de probabilidades con datos del modelo
        modeloDist.setRowCount(0); // Limpia todas las filas existentes en el modelo de distribución
        double[][] rangos = DulceModelo.getRangos(); // Obtiene los rangos de números aleatorios para cada valor de demanda del modelo

        for (int i = 0; i < DulceModelo.DEMANDAS.length; i++) { // Itera sobre todos los valores de demanda del modelo
            String rango = UtilFormatoDulce.p4(rangos[i][0]) + " - " + UtilFormatoDulce.p4(rangos[i][1]); // Construye string del rango formateado (inicio - fin) con 4 decimales
            modeloDist.addRow(new Object[]{ // Agrega una fila a la tabla de distribución con los datos calculados
                UtilFormatoDulce.p4(DulceModelo.PROB[i]), // Probabilidad individual formateada a 4 decimales
                UtilFormatoDulce.p4(getSumaAcumulada(DulceModelo.PROB, i)), // Probabilidad acumulada hasta este punto, formateada a 4 decimales
                rango, // Rango de números aleatorios como string formateado
                DulceModelo.DEMANDAS[i] // Valor de demanda correspondiente
            });
        }
    }

    private double getSumaAcumulada(double[] probs, int hasta) { // Método utilitario que calcula la suma acumulada de probabilidades hasta un índice dado
        double suma = 0; // Inicializa acumulador de suma
        for (int i = 0; i <= hasta; i++) { // Itera desde 0 hasta el índice especificado (inclusive)
            suma += probs[i]; // Suma la probabilidad del índice actual al acumulado
        }
        return suma; // Retorna la suma acumulada
    }

    private void simular(ActionEvent e) { // Método que ejecuta la simulación aleatoria de ganancias
        int replicas = (int) spDias.getValue(); // Obtiene el número de réplicas/simulaciones seleccionado por el usuario

        // Generar números aleatorios
        randoms = new double[replicas]; // Crea array para almacenar los números aleatorios generados
        for (int i = 0; i < replicas; i++) { // Bucle para generar cada número aleatorio
            randoms[i] = Math.random(); // Genera número aleatorio entre 0 y 1 y lo almacena en el array
        }

        // Llenar tabla de simulación con Q=60
        llenarSimulacion(60); // Llama al método para llenar la tabla de simulación usando Q=60 como valor por defecto

        // Llenar tabla comparativa
        llenarComparativa(); // Llama al método para llenar la tabla comparativa con todos los valores de Q especificados
    }

    private void llenarSimulacion(int Q) { // Método que llena la tabla de simulación con los resultados para un valor específico de Q
        modeloSim.setRowCount(0); // Limpia todas las filas existentes en la tabla de simulación
        double suma = 0; // Inicializa acumulador para calcular el total de ganancias

        for (int i = 0; i < randoms.length; i++) { // Itera sobre cada número aleatorio generado
            double r = randoms[i]; // Obtiene el número aleatorio de la posición actual
            int demanda = DulceModelo.demandaPara(r); // Convierte el número aleatorio en demanda usando el modelo
            double ganancia = DulceModelo.ganancia(Q, demanda); // Calcula la ganancia para esta combinación de Q y demanda
            suma += ganancia; // Suma la ganancia de esta réplica al total acumulado

            modeloSim.addRow(new Object[]{ // Agrega una fila a la tabla de simulación con todos los datos de esta réplica
                i + 1, // Número de réplica (i+1 porque i empieza en 0)
                UtilFormatoDulce.p4(r), // Número aleatorio formateado a 4 decimales
                demanda, // Valor de demanda calculado
                UtilFormatoDulce.m2(ganancia) // Ganancia formateada como moneda con 2 decimales
            });
        }

        // Fila de promedio con mejor formato
        double promedio = suma / randoms.length; // Calcula el promedio de ganancias dividiendo la suma total entre el número de réplicas
        modeloSim.addRow(new Object[]{ // Agrega fila con el promedio al final de la tabla
            "TOTAL/PROMEDIO", // Etiqueta descriptiva en la primera columna
            "", // Columna de número aleatorio vacía
            "", // Columna de demanda vacía
            UtilFormatoDulce.m2(promedio) // Promedio de ganancias formateado como moneda
        });

        // Actualizar el label inferior
        lblPromedio.setText("Ganancia promedio para Q=60: " + UtilFormatoDulce.m2(promedio)); // Actualiza la etiqueta inferior con el promedio de ganancias para Q=60
    }

    private void llenarComparativa() { // Método que llena la tabla comparativa con ganancias promedio para diferentes valores de Q
        modeloComp.setRowCount(0); // Limpia todas las filas existentes en la tabla comparativa
        String[] partes = txtValoresQ.getText().split(","); // Divide el texto del campo por comas para obtener array de valores Q

        for (String parte : partes) { // Itera sobre cada valor Q especificado por el usuario
            parte = parte.trim(); // Elimina espacios en blanco al inicio y final
            if (parte.isEmpty()) continue; // Salta valores vacíos

            try {
                int Q = Integer.parseInt(parte); // Intenta convertir el string en número entero
                if (Q <= 0) continue; // Validar que sea un valor positivo - Salta valores no positivos

                double[] ganancias = DulceModelo.simularGanancias(Q, randoms); // Simula las ganancias para este Q usando los mismos números aleatorios
                double promedio = DulceModelo.promedio(ganancias); // Calcula el promedio de ganancias para este Q
                modeloComp.addRow(new Object[]{Q, UtilFormatoDulce.m2(promedio)}); // Agrega fila con Q y su ganancia promedio formateada
            } catch (NumberFormatException ex) {
                // Ignorar valores no numéricos - Si no se puede convertir a número, simplemente ignora este valor
            }
        }
    }
}