package actividad_6.ejercicio3; // Declaración del paquete donde se encuentra la clase

// Importación de clases necesarias para la interfaz gráfica y funcionalidad
import javax.swing.*; // Para componentes de interfaz gráfica como JFrame, JButton, JTextField, etc.
import javax.swing.table.DefaultTableModel; // Para el modelo de datos de la tabla que muestra los resultados
import java.awt.*; // Para layouts y componentes de AWT como BorderLayout, FlowLayout
import java.awt.event.ActionEvent; // Para el manejo de eventos de acciones (clicks de botones)

public class ejercicio3 extends JFrame { // Declaración de la clase que extiende JFrame para crear una ventana

    // Declaración de campos de texto para los parámetros de entrada (todos final e inmutables)
    private final JTextField txtNumMuestras; // Campo para mostrar el número de ensambles a simular
    private final JTextField txtMinBarraA; // Campo para mostrar el valor mínimo de la Barra A en cm
    private final JTextField txtMaxBarraA; // Campo para mostrar el valor máximo de la Barra A en cm
    private final JTextField txtValorEsperadoErlang; // Campo para mostrar el valor esperado de la distribución Erlang
    private final JTextField txtFormaErlang; // Campo para mostrar el parámetro de forma k de la distribución Erlang
    private final JTextField txtEspecInf; // Campo para mostrar la especificación inferior de longitud total
    private final JTextField txtEspecSup; // Campo para mostrar la especificación superior de longitud total
    private final DefaultTableModel model; // Modelo de datos que maneja el contenido de la tabla de resultados

    // Valores predeterminados para Rn (Barra A - Distribución Uniforme)
    private final double[] valoresRn = { // Array con 15 números aleatorios predefinidos para la distribución uniforme
        0.6367, 0.0640, 0.6685, 0.2177, 0.6229, 0.6813, 0.1551, 0.7678, // Primeros 8 valores Rn
        0.8208, 0.4394, 0.9858, 0.6969, 0.4822, 0.9188, 0.7084 // Últimos 7 valores Rn
    };

    // Valores predeterminados para Rn1 (Barra B - Distribución Erlang)
    private final double[] valoresRn1 = { // Array con 15 números aleatorios para el primer parámetro Erlang
        0.0887, 0.2574, 0.2031, 0.1525, 0.9888, 0.7149, 0.2019, 0.2213, // Primeros 8 valores Rn1
        0.9547, 0.9271, 0.6493, 0.3526, 0.7490, 0.6757, 0.8639 // Últimos 7 valores Rn1
    };

    // Valores predeterminados para Rn2 (Barra B - Distribución Erlang)
    private final double[] valoresRn2 = { // Array con 15 números aleatorios para el segundo parámetro Erlang
        0.3345, 0.2086, 0.2513, 0.0631, 0.9721, 0.4351, 0.6910, 0.1118, // Primeros 8 valores Rn2
        0.2845, 0.8603, 0.4857, 0.3081, 0.1997, 0.6390, 0.3940 // Últimos 7 valores Rn2
    };

    // Valores predeterminados para Rn3 (Barra B - Distribución Erlang)
    private final double[] valoresRn3 = { // Array con 15 números aleatorios para el tercer parámetro Erlang
        0.6019, 0.5317, 0.0923, 0.2564, 0.0830, 0.7227, 0.3506, 0.6067, // Primeros 8 valores Rn3
        0.0808, 0.3498, 0.9285, 0.6747, 0.0057, 0.7344, 0.0645 // Últimos 7 valores Rn3
    };

    // Valores predeterminados para Rn4 (Barra B - Distribución Erlang)
    private final double[] valoresRn4 = { // Array con 15 números aleatorios para el cuarto parámetro Erlang
        0.5768, 0.8775, 0.7669, 0.8342, 0.4201, 0.6741, 0.9184, 0.7222, // Primeros 8 valores Rn4
        0.9865, 0.9188, 0.0485, 0.5950, 0.4104, 0.9229, 0.6055 // Últimos 7 valores Rn4
    };

    public ejercicio3() { // Constructor de la clase que inicializa todos los componentes de la interfaz
        setTitle("Simulación Barras Defectuosas"); // Establece el título de la ventana principal
        setSize(1100, 450); // Define las dimensiones de la ventana (ancho 1100px, alto 450px)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla del usuario

        JPanel panelInput = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para organizar componentes horizontalmente

        panelInput.add(new JLabel("Número de Ensambles:")); // Agrega etiqueta descriptiva para el número de ensambles
        txtNumMuestras = new JTextField("15", 5); // Crea campo de texto con valor fijo "15" y ancho de 5 caracteres
        txtNumMuestras.setEditable(false); // Hace el campo no editable para solo mostrar información
        panelInput.add(txtNumMuestras); // Agrega el campo de texto al panel de entrada

        panelInput.add(new JLabel("Mínimo Barra A (cm):")); // Agrega etiqueta para el valor mínimo de Barra A
        txtMinBarraA = new JTextField("45", 5); // Crea campo con valor fijo "45" cm y ancho de 5 caracteres
        txtMinBarraA.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtMinBarraA); // Agrega el campo al panel de entrada

        panelInput.add(new JLabel("Máximo Barra A (cm):")); // Agrega etiqueta para el valor máximo de Barra A
        txtMaxBarraA = new JTextField("55", 5); // Crea campo con valor fijo "55" cm y ancho de 5 caracteres
        txtMaxBarraA.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtMaxBarraA); // Agrega el campo al panel de entrada

        panelInput.add(new JLabel("Valor Esperado Erlang (cm):")); // Agrega etiqueta para la media de la distribución Erlang
        txtValorEsperadoErlang = new JTextField("30", 5); // Crea campo con valor fijo "30" cm y ancho de 5 caracteres
        txtValorEsperadoErlang.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtValorEsperadoErlang); // Agrega el campo al panel de entrada

        panelInput.add(new JLabel("Parámetro forma Erlang k:")); // Agrega etiqueta para el parámetro de forma de Erlang
        txtFormaErlang = new JTextField("4", 5); // Crea campo con valor fijo "4" y ancho de 5 caracteres
        txtFormaErlang.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtFormaErlang); // Agrega el campo al panel de entrada

        panelInput.add(new JLabel("Especificación inferior (cm):")); // Agrega etiqueta para el límite inferior de aceptación
        txtEspecInf = new JTextField("70", 5); // Crea campo con valor fijo "70" cm y ancho de 5 caracteres
        txtEspecInf.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtEspecInf); // Agrega el campo al panel de entrada

        panelInput.add(new JLabel("Especificación superior (cm):")); // Agrega etiqueta para el límite superior de aceptación
        txtEspecSup = new JTextField("90", 5); // Crea campo con valor fijo "90" cm y ancho de 5 caracteres
        txtEspecSup.setEditable(false); // Campo de solo lectura para mostrar parámetro fijo
        panelInput.add(txtEspecSup); // Agrega el campo al panel de entrada

        JButton btnSimular = new JButton("Simular"); // Crea botón para ejecutar la simulación
        panelInput.add(btnSimular); // Agrega el botón al panel de entrada

        String[] columnas = { // Define los nombres de las columnas para la tabla de resultados
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1", // Primeras 4 columnas: número, Rn para Barra A, dimensión A, primer Rn Erlang
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)", // Siguientes 4 columnas: Rn2-4 para Erlang, dimensión B calculada
            "Longitud total (cm)", "Especificación inferior (cm)", // Columnas para longitud total y límite inferior
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No", // Columnas para límite superior y estado defectuoso
            "Piezas defectuosas acumuladas", "% piezas defectuosas" // Columnas para conteo acumulado y porcentaje
        };
        model = new DefaultTableModel(columnas, 0); // Crea modelo de tabla con las columnas definidas y 0 filas iniciales
        JTable tabla = new JTable(model); // Crea la tabla usando el modelo de datos
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con scroll para manejar muchas filas

        add(panelInput, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior de la ventana
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro de la ventana

        btnSimular.addActionListener(this::simular); // Asocia el método simular al evento click del botón
    }

    private void simular(ActionEvent e) { // Método que ejecuta la simulación cuando se presiona el botón
        model.setRowCount(0); // Limpia todas las filas existentes de la tabla antes de la nueva simulación

        int n = Integer.parseInt(txtNumMuestras.getText()); // Obtiene el número de ensambles a simular desde el campo de texto
        double minA = Double.parseDouble(txtMinBarraA.getText()); // Obtiene el valor mínimo para la distribución uniforme de Barra A
        double maxA = Double.parseDouble(txtMaxBarraA.getText()); // Obtiene el valor máximo para la distribución uniforme de Barra A
        double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText()); // Obtiene la media para la distribución Erlang
        int kErlang = Integer.parseInt(txtFormaErlang.getText()); // Obtiene el parámetro de forma k para la distribución Erlang
        double especInf = Double.parseDouble(txtEspecInf.getText()); // Obtiene la especificación inferior para determinar defectos
        double especSup = Double.parseDouble(txtEspecSup.getText()); // Obtiene la especificación superior para determinar defectos

        int acumDefectuosas = 0; // Inicializa contador acumulado de piezas defectuosas

        for (int i = 1; i <= n; i++) { // Ciclo principal que procesa cada ensamble desde 1 hasta n
            // Usar valores predeterminados en lugar de números aleatorios
            double rnA = valoresRn[i-1];  // Obtiene el valor Rn predeterminado para Barra A (i-1 porque el array empieza en índice 0)
            double dimBarraA = minA + (maxA - minA) * rnA; // Calcula dimensión de Barra A usando distribución uniforme inversa

            // Usar valores predeterminados para Erlang
            double rn1 = valoresRn1[i-1]; // Obtiene el primer valor Rn predeterminado para la distribución Erlang
            double rn2 = valoresRn2[i-1]; // Obtiene el segundo valor Rn predeterminado para la distribución Erlang
            double rn3 = valoresRn3[i-1]; // Obtiene el tercer valor Rn predeterminado para la distribución Erlang
            double rn4 = valoresRn4[i-1]; // Obtiene el cuarto valor Rn predeterminado para la distribución Erlang

            // Sumar ln(1 - ri) para Erlang
            double lnProduct = Math.log(1 - rn1) + Math.log(1 - rn2) + Math.log(1 - rn3) + Math.log(1 - rn4); // Calcula la suma de logaritmos naturales para Erlang
            // Erlang Er = -(1/(k*lambda)) * ln(product(1-ri)), lambda = 1/valorEsperado
            double dimBarraB = -(valorEsperado / kErlang) * lnProduct; // Aplica la fórmula de distribución Erlang inversa para calcular dimensión de Barra B

            double longitudTotal = dimBarraA + dimBarraB; // Calcula la longitud total sumando dimensiones de ambas barras

            boolean defectuosa = (longitudTotal < especInf) || (longitudTotal > especSup); // Determina si la pieza es defectuosa comparando con especificaciones
            int defectInt = defectuosa ? 1 : 0; // Convierte el booleano a entero (1 = defectuosa, 0 = aceptable)
            acumDefectuosas += defectInt; // Suma al contador acumulado si la pieza es defectuosa
            double porcentaje = (double) acumDefectuosas / i; // Calcula el porcentaje de piezas defectuosas hasta el momento

            model.addRow(new Object[] { // Agrega una nueva fila a la tabla con todos los resultados calculados
                i, // Número de ensamble actual
                String.format("%.4f", rnA), // Valor Rn para Barra A formateado con 4 decimales
                String.format("%.2f", dimBarraA), // Dimensión calculada de Barra A formateada con 2 decimales
                String.format("%.4f", rn1), // Primer valor Rn para Erlang formateado con 4 decimales
                String.format("%.4f", rn2), // Segundo valor Rn para Erlang formateado con 4 decimales
                String.format("%.4f", rn3), // Tercer valor Rn para Erlang formateado con 4 decimales
                String.format("%.4f", rn4), // Cuarto valor Rn para Erlang formateado con 4 decimales
                String.format("%.2f", dimBarraB), // Dimensión calculada de Barra B formateada con 2 decimales
                String.format("%.2f", longitudTotal), // Longitud total formateada con 2 decimales
                String.format("%.2f", especInf), // Especificación inferior formateada con 2 decimales
                String.format("%.2f", especSup), // Especificación superior formateada con 2 decimales
                defectInt, // Estado defectuoso como entero (1 o 0)
                acumDefectuosas, // Contador acumulado de piezas defectuosas
                String.format("%.2f%%", porcentaje*100) // Porcentaje de defectuosas formateado con 2 decimales y símbolo %
            });
        }
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación de forma independiente
        SwingUtilities.invokeLater(() -> { // Ejecuta la creación de la interfaz en el hilo de eventos de Swing para thread safety
            new ejercicio3().setVisible(true); // Crea una nueva instancia de la clase y hace visible la ventana
        });
    }
}
