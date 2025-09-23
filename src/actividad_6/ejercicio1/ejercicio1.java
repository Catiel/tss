package actividad_6.ejercicio1; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de Swing para la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de la tabla
import java.awt.*; // Importa las clases de AWT para componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

public class ejercicio1 extends JFrame { // Declara la clase principal que extiende JFrame para crear una ventana

    private final JTextField txtMediaLlegada; // Campo de texto para mostrar la media de tiempo entre llegadas
    private final JTextField txtMediaInspeccion; // Campo de texto para mostrar la media de tiempo de inspección
    private final JTextField txtDesvEstInspeccion; // Campo de texto para mostrar la desviación estándar de inspección
    private final JTextField txtNumPiezas; // Campo de texto para mostrar el número de piezas
    private final DefaultTableModel model; // Modelo de tabla para manejar los datos de la simulación

    public ejercicio1() { // Constructor de la clase
        setTitle("Simulación Inspección con Parámetros"); // Establece el título de la ventana
        setSize(1200, 550); // Define el tamaño de la ventana (ancho x alto)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que al cerrar la ventana termine la aplicación
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JPanel inputPanel = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para los controles de entrada

        inputPanel.add(new JLabel("Media - Tiempo entreg llegadas")); // Agrega etiqueta para el campo de media de llegadas
        txtMediaLlegada = new JTextField("5", 5); // Crea campo de texto con valor "5" y ancho de 5 caracteres
        txtMediaLlegada.setEditable(false); // Hace el campo no editable por el usuario
        inputPanel.add(txtMediaLlegada); // Agrega el campo al panel

        inputPanel.add(new JLabel("Media - Tiempo inspección")); // Agrega etiqueta para el campo de media de inspección
        txtMediaInspeccion = new JTextField("4", 5); // Crea campo de texto con valor "4" y ancho de 5 caracteres
        txtMediaInspeccion.setEditable(false); // Hace el campo no editable por el usuario
        inputPanel.add(txtMediaInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Desv Est - Tiempo inspección")); // Agrega etiqueta para el campo de desviación estándar
        txtDesvEstInspeccion = new JTextField("0.5", 5); // Crea campo de texto con valor "0.5" y ancho de 5 caracteres
        txtDesvEstInspeccion.setEditable(false); // Hace el campo no editable por el usuario
        inputPanel.add(txtDesvEstInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Número de piezas")); // Agrega etiqueta para el campo de número de piezas
        txtNumPiezas = new JTextField("9", 5); // Crea campo de texto con valor "9" y ancho de 5 caracteres
        txtNumPiezas.setEditable(false); // Hace el campo no editable por el usuario
        inputPanel.add(txtNumPiezas); // Agrega el campo al panel

        JButton btnSimular = new JButton("Simular"); // Crea un botón con el texto "Simular"
        inputPanel.add(btnSimular); // Agrega el botón al panel de controles

        String[] columnas = { // Define un arreglo con los nombres de las columnas de la tabla
                "Piezas", "Tiempo entreg llegadas", "Tiempo de llegada", "Inicio de inspección",
                "Tiempo de inspección", "Fin de la inspección", "Duración de la inspección",
                "Tiempo en espera", "Tiempo pro1/2 en inspeccion"
        };
        model = new DefaultTableModel(columnas, 0); // Crea el modelo de tabla con las columnas definidas y 0 filas iniciales

        JTable tabla = new JTable(model); // Crea la tabla usando el modelo definido
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con barras de desplazamiento

        setLayout(new BorderLayout()); // Establece el layout de la ventana como BorderLayout
        add(inputPanel, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro

        btnSimular.addActionListener(this::simular); // Asocia el método simular como listener del botón
    }

    private void simular(ActionEvent e) { // Método que se ejecuta al presionar el botón simular
        model.setRowCount(0); // Limpia todas las filas de la tabla

        // Valores predeterminados fijos
        int n = 9; // Define el número fijo de piezas a simular

        // Valores predeterminados para tiempo entreg llegadas
        double[] tiempoEntregLlegadas = { // Arreglo con valores predefinidos de tiempo entre llegadas
                1.327607, 6.5310326, 5.1946396, 7.0961155, 7.31768001,
                2.0867485, 1.4688669, 4.6554184, 0.4156928
        };

        // Valores predeterminados para tiempo de inspección
        double[] tiempoInspeccionPred = { // Arreglo con valores predefinidos de tiempo de inspección
                3.0058359, 4.13466238, 3.9414644, 4.2697994, 4.1262137,
                3.9923443, 4.1267457, 4.4108686, 2.7754816
        };

        double[] tiempoLlegada = new double[n]; // Arreglo para almacenar el tiempo acumulado de llegada de cada pieza
        double[] inicioInspeccion = new double[n]; // Arreglo para almacenar el tiempo de inicio de inspección de cada pieza
        double[] finInspeccion = new double[n]; // Arreglo para almacenar el tiempo de fin de inspección de cada pieza
        double[] duracionInspeccion = new double[n]; // Arreglo para almacenar la duración total en el proceso de cada pieza
        double[] tiempoEspera = new double[n]; // Arreglo para almacenar el tiempo de espera de cada pieza

        for (int i = 0; i < n; i++) { // Itera a través de cada pieza para realizar los cálculos
            // Calcular tiempo de llegada
            if (i == 0) { // Para la primera pieza
                tiempoLlegada[i] = tiempoEntregLlegadas[i]; // El tiempo de llegada es igual al tiempo entre llegadas
            } else { // Para las demás piezas
                tiempoLlegada[i] = tiempoLlegada[i - 1] + tiempoEntregLlegadas[i]; // Acumula el tiempo de llegada anterior más el tiempo entre llegadas
            }

            // Calcular inicio de inspección
            if (i == 0) { // Para la primera pieza
                inicioInspeccion[i] = tiempoLlegada[i]; // Inicia inspección inmediatamente al llegar
            } else { // Para las demás piezas
                inicioInspeccion[i] = Math.max(tiempoLlegada[i], finInspeccion[i - 1]); // Inicia cuando llega o cuando termina la inspección anterior, lo que sea mayor
            }

            // Calcular fin de inspección = inicio de inspección + tiempo de inspección
            finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccionPred[i]; // Suma el tiempo de inicio más la duración de la inspección

            // Calcular duración de la inspección = fin de la inspección - tiempo de llegada
            duracionInspeccion[i] = finInspeccion[i] - tiempoLlegada[i]; // Calcula el tiempo total que la pieza permaneció en el sistema

            // Calcular tiempo en espera
            tiempoEspera[i] = Math.max(0, inicioInspeccion[i] - tiempoLlegada[i]); // Calcula cuánto tiempo esperó la pieza antes de ser inspeccionada

            // Calcular tiempo promedio en inspección (promedio entre la primera fila y la actual)
            double tiempoPromInspeccion; // Variable para almacenar el tiempo promedio
            if (i == 0) { // Para la primera pieza
                tiempoPromInspeccion = duracionInspeccion[0]; // El promedio es igual a su propia duración
            } else { // Para las demás piezas
                tiempoPromInspeccion = (duracionInspeccion[0] + duracionInspeccion[i]) / 2.0; // Calcula el promedio entre la primera pieza y la actual
            }

            model.addRow(new Object[]{ // Agrega una nueva fila a la tabla con todos los valores calculados
                    i + 1, // Número de pieza (empezando en 1)
                    String.format("%.7f", tiempoEntregLlegadas[i]), // Tiempo entre llegadas con 7 decimales
                    String.format("%.6f", tiempoLlegada[i]), // Tiempo de llegada con 6 decimales
                    String.format("%.6f", inicioInspeccion[i]), // Inicio de inspección con 6 decimales
                    String.format("%.7f", tiempoInspeccionPred[i]), // Tiempo de inspección con 7 decimales
                    String.format("%.6f", finInspeccion[i]), // Fin de inspección con 6 decimales
                    String.format("%.6f", duracionInspeccion[i]), // Duración en el sistema con 6 decimales
                    String.format("%.6f", tiempoEspera[i]), // Tiempo de espera con 6 decimales
                    String.format("%.6f", tiempoPromInspeccion) // Tiempo promedio con 6 decimales
            });
        }
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación
        SwingUtilities.invokeLater(() -> new ejercicio1().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing
    }
}
