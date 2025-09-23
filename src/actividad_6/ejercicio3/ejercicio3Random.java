package actividad_6.ejercicio3; // Declaración del paquete donde se encuentra la clase

// Importación de clases necesarias para la interfaz gráfica y funcionalidad
import javax.swing.*; // Para componentes de interfaz gráfica como JFrame, JButton, JTextField, JTable, etc.
import javax.swing.table.DefaultTableModel; // Para el modelo de datos de la tabla que muestra los resultados de simulación
import java.awt.*; // Para layouts y componentes de AWT como BorderLayout, FlowLayout
import java.awt.event.ActionEvent; // Para el manejo de eventos de acciones (clicks de botones)
import java.util.Random; // Para la generación de números aleatorios en tiempo real durante la simulación

public class ejercicio3Random extends JFrame { // Declaración de la clase que extiende JFrame para crear una ventana con generación aleatoria

    // Declaración de campos de texto para los parámetros de entrada (todos final e inmutables excepto el número de muestras)
    private final JTextField txtNumMuestras; // Campo editable para ingresar el número de ensambles a simular dinámicamente
    private final JTextField txtMinBarraA; // Campo de solo lectura para mostrar el valor mínimo de la Barra A en cm
    private final JTextField txtMaxBarraA; // Campo de solo lectura para mostrar el valor máximo de la Barra A en cm
    private final JTextField txtValorEsperadoErlang; // Campo de solo lectura para mostrar el valor esperado de la distribución Erlang
    private final JTextField txtFormaErlang; // Campo de solo lectura para mostrar el parámetro de forma k de la distribución Erlang
    private final JTextField txtEspecInf; // Campo de solo lectura para mostrar la especificación inferior de longitud total
    private final JTextField txtEspecSup; // Campo de solo lectura para mostrar la especificación superior de longitud total
    private final DefaultTableModel model; // Modelo de datos que maneja el contenido de la tabla de resultados con generación aleatoria

    public ejercicio3Random() { // Constructor de la clase que inicializa todos los componentes de la interfaz aleatoria
        setTitle("Simulación Barras Defectuosas - Versión Aleatoria"); // Establece el título específico para la versión con números aleatorios
        setSize(1100, 450); // Define las dimensiones de la ventana (ancho 1100px, alto 450px) apropiadas para mostrar todos los datos
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que la aplicación termine completamente al cerrar la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla del usuario para mejor accesibilidad

        JPanel panelInput = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para organizar componentes horizontalmente

        panelInput.add(new JLabel("Número de Ensambles:")); // Agrega etiqueta descriptiva para el campo de número de ensambles
        txtNumMuestras = new JTextField("15", 5); // Crea campo de texto editable con valor inicial "15" y ancho de 5 caracteres
        // Solo este campo es editable
        panelInput.add(txtNumMuestras); // Agrega el único campo editable al panel de entrada

        panelInput.add(new JLabel("Mínimo Barra A (cm):")); // Agrega etiqueta para el parámetro mínimo de la distribución uniforme de Barra A
        txtMinBarraA = new JTextField("45", 5); // Crea campo con valor fijo "45" cm y ancho de 5 caracteres
        txtMinBarraA.setEditable(false); // Deshabilita la edición para mantener parámetro fijo en la simulación
        panelInput.add(txtMinBarraA); // Agrega el campo de solo lectura al panel

        panelInput.add(new JLabel("Máximo Barra A (cm):")); // Agrega etiqueta para el parámetro máximo de la distribución uniforme de Barra A
        txtMaxBarraA = new JTextField("55", 5); // Crea campo con valor fijo "55" cm y ancho de 5 caracteres
        txtMaxBarraA.setEditable(false); // Deshabilita la edición para mantener parámetro fijo en la simulación
        panelInput.add(txtMaxBarraA); // Agrega el campo de solo lectura al panel

        panelInput.add(new JLabel("Valor Esperado Erlang (cm):")); // Agrega etiqueta para la media de la distribución Erlang de Barra B
        txtValorEsperadoErlang = new JTextField("30", 5); // Crea campo con valor fijo "30" cm y ancho de 5 caracteres
        txtValorEsperadoErlang.setEditable(false); // Deshabilita la edición para mantener parámetro estadístico fijo
        panelInput.add(txtValorEsperadoErlang); // Agrega el campo de solo lectura al panel

        panelInput.add(new JLabel("Parámetro forma Erlang k:")); // Agrega etiqueta para el parámetro de forma de la distribución Erlang
        txtFormaErlang = new JTextField("4", 5); // Crea campo con valor fijo "4" y ancho de 5 caracteres (4 números aleatorios para Erlang)
        txtFormaErlang.setEditable(false); // Deshabilita la edición para mantener forma estadística fija
        panelInput.add(txtFormaErlang); // Agrega el campo de solo lectura al panel

        panelInput.add(new JLabel("Especificación inferior (cm):")); // Agrega etiqueta para el límite inferior de aceptación de calidad
        txtEspecInf = new JTextField("70", 5); // Crea campo con valor fijo "70" cm y ancho de 5 caracteres
        txtEspecInf.setEditable(false); // Deshabilita la edición para mantener criterio de calidad fijo
        panelInput.add(txtEspecInf); // Agrega el campo de solo lectura al panel

        panelInput.add(new JLabel("Especificación superior (cm):")); // Agrega etiqueta para el límite superior de aceptación de calidad
        txtEspecSup = new JTextField("90", 5); // Crea campo con valor fijo "90" cm y ancho de 5 caracteres
        txtEspecSup.setEditable(false); // Deshabilita la edición para mantener criterio de calidad fijo
        panelInput.add(txtEspecSup); // Agrega el campo de solo lectura al panel

        JButton btnSimular = new JButton("Simular"); // Crea botón para ejecutar la simulación con números aleatorios
        panelInput.add(btnSimular); // Agrega el botón de simulación al panel de entrada

        String[] columnas = { // Define los nombres de las columnas para la tabla de resultados de simulación aleatoria
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1", // Primeras 4 columnas: número consecutivo, Rn generado para uniforme, dimensión A calculada, primer Rn para Erlang
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)", // Siguientes 4 columnas: segundo a cuarto Rn para Erlang, dimensión B calculada
            "Longitud total (cm)", "Especificación inferior (cm)", // Columnas para longitud total calculada y límite inferior de especificación
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No", // Columnas para límite superior y determinación binaria de estado defectuoso
            "Piezas defectuosas acumuladas", "% piezas defectuosas" // Columnas para conteo acumulativo y porcentaje estadístico de defectos
        };
        model = new DefaultTableModel(columnas, 0); // Crea modelo de tabla con las columnas definidas y 0 filas iniciales para llenado dinámico
        JTable tabla = new JTable(model); // Crea la tabla usando el modelo de datos que se llenará con resultados aleatorios
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con scroll para manejar cualquier cantidad de filas

        add(panelInput, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior de la ventana
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro de la ventana para máxima visibilidad

        btnSimular.addActionListener(this::simular); // Asocia el método simular al evento click del botón para ejecutar simulación aleatoria
    }

    private void simular(ActionEvent e) { // Método que ejecuta la simulación completa con generación de números aleatorios en tiempo real
        model.setRowCount(0); // Limpia todas las filas existentes de la tabla antes de generar nueva simulación aleatoria

        int n = Integer.parseInt(txtNumMuestras.getText()); // Obtiene el número de ensambles a simular desde el campo editable
        double minA = Double.parseDouble(txtMinBarraA.getText()); // Obtiene el valor mínimo para la distribución uniforme de Barra A
        double maxA = Double.parseDouble(txtMaxBarraA.getText()); // Obtiene el valor máximo para la distribución uniforme de Barra A
        double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText()); // Obtiene la media para la distribución Erlang de Barra B
        int kErlang = Integer.parseInt(txtFormaErlang.getText()); // Obtiene el parámetro de forma k para la distribución Erlang (número de variables aleatorias)
        double especInf = Double.parseDouble(txtEspecInf.getText()); // Obtiene la especificación inferior para determinar piezas defectuosas
        double especSup = Double.parseDouble(txtEspecSup.getText()); // Obtiene la especificación superior para determinar piezas defectuosas

        Random random = new Random(); // Crea una instancia del generador de números aleatorios para toda la simulación
        int acumDefectuosas = 0; // Inicializa contador acumulado de piezas defectuosas encontradas

        for (int i = 1; i <= n; i++) { // Ciclo principal que procesa cada ensamble desde 1 hasta n con números completamente aleatorios
            // Generar números aleatorios para cada iteración
            double rnA = random.nextDouble(); // Genera número aleatorio uniforme [0,1) para la distribución de Barra A
            double dimBarraA = minA + (maxA - minA) * rnA; // Calcula dimensión de Barra A usando transformación de distribución uniforme inversa

            // 4 aleatorios uniformes para Erlang
            double rn1 = random.nextDouble(); // Genera primer número aleatorio uniforme [0,1) para la distribución Erlang
            double rn2 = random.nextDouble(); // Genera segundo número aleatorio uniforme [0,1) para la distribución Erlang
            double rn3 = random.nextDouble(); // Genera tercer número aleatorio uniforme [0,1) para la distribución Erlang
            double rn4 = random.nextDouble(); // Genera cuarto número aleatorio uniforme [0,1) para la distribución Erlang

            // Sumar ln(1 - ri) para Erlang
            double lnProduct = Math.log(1 - rn1) + Math.log(1 - rn2) + Math.log(1 - rn3) + Math.log(1 - rn4); // Calcula la suma de logaritmos naturales para transformación Erlang
            // Erlang Er = -(1/(k*lambda)) * ln(product(1-ri)), lambda = 1/valorEsperado
            double dimBarraB = -(valorEsperado / kErlang) * lnProduct; // Aplica la fórmula completa de distribución Erlang inversa para calcular dimensión de Barra B

            double longitudTotal = dimBarraA + dimBarraB; // Calcula la longitud total del ensamble sumando dimensiones de ambas barras

            boolean defectuosa = (longitudTotal < especInf) || (longitudTotal > especSup); // Determina si la pieza es defectuosa comparando longitud total con especificaciones
            int defectInt = defectuosa ? 1 : 0; // Convierte el resultado booleano a entero (1 = defectuosa, 0 = aceptable)
            acumDefectuosas += defectInt; // Suma al contador acumulado si la pieza actual es defectuosa
            double porcentaje = (double) acumDefectuosas / i; // Calcula el porcentaje acumulado de piezas defectuosas hasta el momento

            model.addRow(new Object[] { // Agrega una nueva fila a la tabla con todos los valores generados aleatoriamente y resultados calculados
                i, // Número de ensamble actual en la secuencia
                String.format("%.4f", rnA), // Valor Rn generado para Barra A formateado con 4 decimales para precisión
                String.format("%.2f", dimBarraA), // Dimensión calculada de Barra A formateada con 2 decimales para legibilidad
                String.format("%.4f", rn1), // Primer valor Rn generado para Erlang formateado con 4 decimales
                String.format("%.4f", rn2), // Segundo valor Rn generado para Erlang formateado con 4 decimales
                String.format("%.4f", rn3), // Tercer valor Rn generado para Erlang formateado con 4 decimales
                String.format("%.4f", rn4), // Cuarto valor Rn generado para Erlang formateado con 4 decimales
                String.format("%.2f", dimBarraB), // Dimensión calculada de Barra B formateada con 2 decimales
                String.format("%.2f", longitudTotal), // Longitud total calculada formateada con 2 decimales
                String.format("%.2f", especInf), // Especificación inferior mostrada con formato consistente de 2 decimales
                String.format("%.2f", especSup), // Especificación superior mostrada con formato consistente de 2 decimales
                defectInt, // Estado defectuoso como entero binario (1 o 0) para fácil interpretación
                acumDefectuosas, // Contador acumulado de todas las piezas defectuosas encontradas hasta ahora
                String.format("%.2f%%", porcentaje*100) // Porcentaje de defectuosas formateado con 2 decimales y símbolo de porcentaje
            });
        }
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación aleatoria de forma independiente
        SwingUtilities.invokeLater(() -> { // Ejecuta la creación de la interfaz en el hilo de eventos de Swing para thread safety
            new ejercicio3Random().setVisible(true); // Crea una nueva instancia de la clase aleatoria y hace visible la ventana
        });
    }
}
