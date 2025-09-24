package actividad_6.ejercicio1; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de Swing para la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de la tabla
import java.awt.*; // Importa las clases de AWT para componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción
import java.util.Random; // Importa la clase Random para generar números aleatorios

public class ejercicio1_aleatorio extends JFrame { // Declara la clase que extiende JFrame para crear una ventana con valores aleatorios

    private final JTextField txtMediaLlegada; // Campo de texto para mostrar la media de tiempo entre llegadas
    private final JTextField txtMediaInspeccion; // Campo de texto para mostrar la media de tiempo de inspección
    private final JTextField txtDesvEstInspeccion; // Campo de texto para mostrar la desviación estándar de inspección
    private final JTextField txtNumPiezas; // Campo de texto para ingresar el número de piezas (editable)
    private final DefaultTableModel model; // Modelo de tabla para manejar los datos de la simulación

    public ejercicio1_aleatorio() { // Constructor de la clase
        setTitle("Simulación Inspección - Valores Aleatorios"); // Establece el título de la ventana indicando que usa valores aleatorios
        setSize(1200, 550); // Define el tamaño de la ventana (ancho x alto)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que al cerrar la ventana termine la aplicación
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JPanel inputPanel = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para los controles de entrada

        inputPanel.add(new JLabel("Media - Tiempo entreg llegadas")); // Agrega etiqueta para el campo de media de llegadas
        txtMediaLlegada = new JTextField("5", 5); // Crea campo de texto con valor "5" (media para distribución exponencial) y ancho de 5 caracteres
        txtMediaLlegada.setEditable(true); // Hace el campo EDITABLE para que el usuario pueda modificar la media de llegadas
        inputPanel.add(txtMediaLlegada); // Agrega el campo al panel

        inputPanel.add(new JLabel("Media - Tiempo inspección")); // Agrega etiqueta para el campo de media de inspección
        txtMediaInspeccion = new JTextField("4", 5); // Crea campo de texto con valor "4" (media para distribución normal) y ancho de 5 caracteres
        txtMediaInspeccion.setEditable(true); // Hace el campo EDITABLE para que el usuario pueda modificar la media de inspección
        inputPanel.add(txtMediaInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Desv Est - Tiempo inspección")); // Agrega etiqueta para el campo de desviación estándar
        txtDesvEstInspeccion = new JTextField("0.5", 5); // Crea campo de texto con valor "0.5" (desviación estándar para distribución normal) y ancho de 5 caracteres
        txtDesvEstInspeccion.setEditable(true); // Hace el campo EDITABLE para que el usuario pueda modificar la desviación estándar
        inputPanel.add(txtDesvEstInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Número de piezas")); // Agrega etiqueta para el campo de número de piezas
        txtNumPiezas = new JTextField("9", 5); // Crea campo de texto con valor por defecto "9" y ancho de 5 caracteres
        txtNumPiezas.setEditable(true); // Permite modificar el número de piezas // Hace el campo editable para que el usuario pueda cambiar la cantidad de piezas a simular
        inputPanel.add(txtNumPiezas); // Agrega el campo al panel

        JButton btnSimular = new JButton("Simular"); // Crea un botón con el texto "Simular" para iniciar la simulación
        inputPanel.add(btnSimular); // Agrega el botón al panel de controles

        String[] columnas = { // Define un arreglo con los nombres de las columnas de la tabla de resultados
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
        model.setRowCount(0); // Limpia todas las filas de la tabla antes de comenzar nueva simulación

        // Obtener número de piezas del campo de texto
        int n = Integer.parseInt(txtNumPiezas.getText()); // Lee y convierte a entero el número de piezas ingresado por el usuario

        // Parámetros según el enunciado
        double mediaLlegada = 5.0;    // Media para distribución exponencial // Define la media de la distribución exponencial para tiempos entre llegadas
        double mediaInspeccion = 4.0;  // Media para distribución normal // Define la media de la distribución normal para tiempos de inspección
        double desvInspeccion = 0.5;   // Desviación estándar para distribución normal // Define la desviación estándar de la distribución normal

        // Obtener parámetros desde los campos de texto
        try {
            mediaLlegada = Double.parseDouble(txtMediaLlegada.getText()); // Intenta obtener y parsear la media de llegadas
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido para la media de tiempo entre llegadas.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si hay un error en el valor ingresado
        }

        try {
            mediaInspeccion = Double.parseDouble(txtMediaInspeccion.getText()); // Intenta obtener y parsear la media de inspección
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido para la media de tiempo de inspección.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si hay un error en el valor ingresado
        }

        try {
            desvInspeccion = Double.parseDouble(txtDesvEstInspeccion.getText()); // Intenta obtener y parsear la desviación estándar
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Valor inválido para la desviación estándar de inspección.", "Error", JOptionPane.ERROR_MESSAGE);
            return; // Sale del método si hay un error en el valor ingresado
        }

        Random random = new Random(); // Crea una instancia del generador de números aleatorios

        double[] tiempoLlegada = new double[n]; // Arreglo para almacenar el tiempo acumulado de llegada de cada pieza
        double[] inicioInspeccion = new double[n]; // Arreglo para almacenar el tiempo de inicio de inspección de cada pieza
        double[] finInspeccion = new double[n]; // Arreglo para almacenar el tiempo de fin de inspección de cada pieza
        double[] duracionInspeccion = new double[n]; // Arreglo para almacenar la duración total en el proceso de cada pieza
        double[] tiempoEspera = new double[n]; // Arreglo para almacenar el tiempo de espera de cada pieza
        double[] tiempoEntregLlegadas = new double[n]; // Arreglo para almacenar los tiempos entre llegadas generados aleatoriamente
        double[] tiempoInspeccionArray = new double[n]; // Arreglo para almacenar los tiempos de inspección generados aleatoriamente

        for (int i = 0; i < n; i++) { // Itera a través de cada pieza para generar valores aleatorios y realizar cálculos
            // Generar tiempo entreg llegadas usando distribución exponencial
            // Fórmula: -LN(ALEATORIO()) * media
            double randomValue1 = random.nextDouble(); // Genera un número aleatorio entre 0 y 1
            tiempoEntregLlegadas[i] = -Math.log(randomValue1) * mediaLlegada; // Aplica la fórmula de distribución exponencial: -LN(aleatorio) * media

            // Generar tiempo de inspección usando distribución normal
            // Fórmula equivalente a DISTR.NORM.INV(ALEATORIO();4;0.5)
            tiempoInspeccionArray[i] = mediaInspeccion + desvInspeccion * random.nextGaussian(); // Genera valor usando distribución normal con la media y desviación especificadas
            // Asegurar que no sea negativo
            if (tiempoInspeccionArray[i] < 0) { // Verifica si el tiempo de inspección generado es negativo
                tiempoInspeccionArray[i] = 0; // Establece el tiempo mínimo en 0 si el valor generado es negativo
            }

            // Calcular tiempo de llegada
            if (i == 0) { // Para la primera pieza
                tiempoLlegada[i] = tiempoEntregLlegadas[i]; // El tiempo de llegada es igual al tiempo entre llegadas
            } else { // Para las demás piezas
                tiempoLlegada[i] = tiempoLlegada[i - 1] + tiempoEntregLlegadas[i]; // Acumula el tiempo de llegada anterior más el tiempo entre llegadas actual
            }

            // Calcular inicio de inspección
            if (i == 0) { // Para la primera pieza
                inicioInspeccion[i] = tiempoLlegada[i]; // Inicia inspección inmediatamente al llegar
            } else { // Para las demás piezas
                inicioInspeccion[i] = Math.max(tiempoLlegada[i], finInspeccion[i - 1]); // Inicia cuando llega o cuando termina la inspección anterior, lo que sea mayor
            }

            // Calcular fin de inspección = inicio de inspección + tiempo de inspección
            finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccionArray[i]; // Suma el tiempo de inicio más la duración de la inspección generada aleatoriamente

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
                    String.format("%.7f", tiempoEntregLlegadas[i]), // Tiempo entre llegadas generado aleatoriamente con 7 decimales
                    String.format("%.6f", tiempoLlegada[i]), // Tiempo de llegada acumulado con 6 decimales
                    String.format("%.6f", inicioInspeccion[i]), // Inicio de inspección con 6 decimales
                    String.format("%.7f", tiempoInspeccionArray[i]), // Tiempo de inspección generado aleatoriamente con 7 decimales
                    String.format("%.6f", finInspeccion[i]), // Fin de inspección con 6 decimales
                    String.format("%.6f", duracionInspeccion[i]), // Duración total en el sistema con 6 decimales
                    String.format("%.6f", tiempoEspera[i]), // Tiempo de espera con 6 decimales
                    String.format("%.6f", tiempoPromInspeccion) // Tiempo promedio con 6 decimales
            });
        }

        // Calcular y mostrar estadísticas adicionales
        double sumaDuracionInspeccion = 0; // Inicializa la suma de todas las duraciones de inspección
        for (int i = 0; i < n; i++) { // Itera a través de todas las piezas
            sumaDuracionInspeccion += duracionInspeccion[i]; // Suma la duración de inspección de cada pieza
        }
        double tiempoPromedioTotal = sumaDuracionInspeccion / n; // Calcula el tiempo promedio total dividiendo la suma entre el número de piezas

        JOptionPane.showMessageDialog(this, // Muestra un cuadro de diálogo con el resultado
            String.format("Tiempo promedio de permanencia en el proceso: %.4f minutos", tiempoPromedioTotal), // Formatea el mensaje con el tiempo promedio con 4 decimales
            "Resultado de la Simulación", // Título del cuadro de diálogo
            JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación
        SwingUtilities.invokeLater(() -> new ejercicio1_aleatorio().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing
    }
}
