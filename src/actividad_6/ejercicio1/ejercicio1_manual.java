package actividad_6.ejercicio1; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa todas las clases de Swing para la interfaz gráfica
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejar datos de la tabla
import java.awt.*; // Importa las clases de AWT para componentes gráficos y layouts
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acción

public class ejercicio1_manual extends JFrame { // Declara la clase que extiende JFrame para crear una ventana con ingreso manual

    private final JTextField txtMediaLlegada; // Campo de texto para mostrar la media de tiempo entre llegadas (solo referencia)
    private final JTextField txtMediaInspeccion; // Campo de texto para mostrar la media de tiempo de inspección (solo referencia)
    private final JTextField txtDesvEstInspeccion; // Campo de texto para mostrar la desviación estándar de inspección (solo referencia)
    private final JTextField txtNumPiezas; // Campo de texto para ingresar el número de piezas a simular (editable)
    private final DefaultTableModel model; // Modelo de tabla para manejar los datos de la simulación
    private JTable tabla; // Referencia a la tabla para configurar propiedades específicas

    public ejercicio1_manual() { // Constructor de la clase
        setTitle("Simulación Inspección - Ingreso Manual"); // Establece el título de la ventana indicando que permite ingreso manual
        setSize(1400, 600); // Define el tamaño de la ventana más grande para acomodar mejor la tabla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que al cerrar la ventana termine la aplicación
        setLocationRelativeTo(null); // Centra la ventana en la pantalla

        JPanel inputPanel = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para los controles de entrada

        inputPanel.add(new JLabel("Media - Tiempo entreg llegadas")); // Agrega etiqueta para el campo de media de llegadas
        txtMediaLlegada = new JTextField("5", 5); // Crea campo de texto con valor "5" como referencia y ancho de 5 caracteres
        txtMediaLlegada.setEditable(false); // Hace el campo no editable ya que es solo informativo/referencia
        inputPanel.add(txtMediaLlegada); // Agrega el campo al panel

        inputPanel.add(new JLabel("Media - Tiempo inspección")); // Agrega etiqueta para el campo de media de inspección
        txtMediaInspeccion = new JTextField("4", 5); // Crea campo de texto con valor "4" como referencia y ancho de 5 caracteres
        txtMediaInspeccion.setEditable(false); // Hace el campo no editable ya que es solo informativo/referencia
        inputPanel.add(txtMediaInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Desv Est - Tiempo inspección")); // Agrega etiqueta para el campo de desviación estándar
        txtDesvEstInspeccion = new JTextField("0.5", 5); // Crea campo de texto con valor "0.5" como referencia y ancho de 5 caracteres
        txtDesvEstInspeccion.setEditable(false); // Hace el campo no editable ya que es solo informativo/referencia
        inputPanel.add(txtDesvEstInspeccion); // Agrega el campo al panel

        inputPanel.add(new JLabel("Número de piezas")); // Agrega etiqueta para el campo de número de piezas
        txtNumPiezas = new JTextField("9", 5); // Crea campo de texto con valor por defecto "9" y ancho de 5 caracteres
        txtNumPiezas.setEditable(true); // Hace el campo editable para que el usuario pueda cambiar la cantidad de piezas
        inputPanel.add(txtNumPiezas); // Agrega el campo al panel

        JButton btnCrearFilas = new JButton("Crear Filas"); // Crea botón para generar las filas vacías en la tabla
        JButton btnCalcular = new JButton("Calcular"); // Crea botón para realizar los cálculos después del ingreso manual
        JButton btnLimpiar = new JButton("Limpiar"); // Crea botón para limpiar toda la tabla

        inputPanel.add(btnCrearFilas); // Agrega el botón de crear filas al panel
        inputPanel.add(btnCalcular); // Agrega el botón de calcular al panel
        inputPanel.add(btnLimpiar); // Agrega el botón de limpiar al panel

        String[] columnas = { // Define un arreglo con los nombres de las columnas de la tabla
                "Piezas", "Tiempo entreg llegadas", "Tiempo de llegada", "Inicio de inspección",
                "Tiempo de inspección", "Fin de la inspección", "Duración de la inspección",
                "Tiempo en espera", "Tiempo pro1/2 en inspeccion"
        };
        model = new DefaultTableModel(columnas, 0) { // Crea el modelo de tabla con las columnas definidas y 0 filas iniciales
            @Override
            public boolean isCellEditable(int row, int column) { // Sobrescribe el método para controlar qué celdas son editables
                // Solo permitir editar las columnas 1 (Tiempo entreg llegadas) y 4 (Tiempo de inspección)
                return column == 1 || column == 4; // Retorna true solo para las columnas que el usuario puede editar manualmente
            }
        };

        tabla = new JTable(model); // Crea la tabla usando el modelo definido

        // Configurar el ancho de las columnas para mejor visualización
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);  // Piezas // Establece ancho preferido para la columna de número de piezas
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120); // Tiempo entreg llegadas // Establece ancho para columna editable de tiempo entre llegadas
        tabla.getColumnModel().getColumn(2).setPreferredWidth(100); // Tiempo de llegada // Establece ancho para columna calculada de tiempo de llegada
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120); // Inicio de inspección // Establece ancho para columna calculada de inicio de inspección
        tabla.getColumnModel().getColumn(4).setPreferredWidth(120); // Tiempo de inspección // Establece ancho para columna editable de tiempo de inspección
        tabla.getColumnModel().getColumn(5).setPreferredWidth(120); // Fin de la inspección // Establece ancho para columna calculada de fin de inspección
        tabla.getColumnModel().getColumn(6).setPreferredWidth(120); // Duración de la inspección // Establece ancho para columna calculada de duración total
        tabla.getColumnModel().getColumn(7).setPreferredWidth(100); // Tiempo en espera // Establece ancho para columna calculada de tiempo de espera
        tabla.getColumnModel().getColumn(8).setPreferredWidth(140); // Tiempo pro1/2 en inspeccion // Establece ancho para columna calculada de tiempo promedio

        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con barras de desplazamiento

        setLayout(new BorderLayout()); // Establece el layout de la ventana como BorderLayout
        add(inputPanel, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro

        btnCrearFilas.addActionListener(this::crearFilas); // Asocia el método crearFilas como listener del botón crear filas
        btnCalcular.addActionListener(this::calcular); // Asocia el método calcular como listener del botón calcular
        btnLimpiar.addActionListener(this::limpiar); // Asocia el método limpiar como listener del botón limpiar
    }

    private void crearFilas(ActionEvent e) { // Método que se ejecuta al presionar el botón "Crear Filas"
        model.setRowCount(0); // Limpia todas las filas existentes de la tabla

        try { // Inicia bloque try para manejar posibles errores de conversión
            int n = Integer.parseInt(txtNumPiezas.getText()); // Lee y convierte a entero el número de piezas ingresado

            for (int i = 0; i < n; i++) { // Itera para crear el número especificado de filas
                model.addRow(new Object[] { // Agrega una nueva fila con valores iniciales
                    i + 1,                    // Número de pieza // Número de pieza comenzando en 1
                    "",                       // Tiempo entreg llegadas (editable) // Campo vacío para ingreso manual
                    "",                       // Tiempo de llegada (calculado) // Campo vacío que se calculará después
                    "",                       // Inicio de inspección (calculado) // Campo vacío que se calculará después
                    "",                       // Tiempo de inspección (editable) // Campo vacío para ingreso manual
                    "",                       // Fin de la inspección (calculado) // Campo vacío que se calculará después
                    "",                       // Duración de la inspección (calculado) // Campo vacío que se calculará después
                    "",                       // Tiempo en espera (calculado) // Campo vacío que se calculará después
                    ""                        // Tiempo pro1/2 en inspeccion (calculado) // Campo vacío que se calculará después
                });
            }

            JOptionPane.showMessageDialog(this, // Muestra un cuadro de diálogo con instrucciones
                "Filas creadas. Ahora ingrese los valores en las columnas:\n" + // Mensaje explicativo de los pasos a seguir
                "- 'Tiempo entreg llegadas' (columna 2)\n" + // Indica qué columna debe llenar el usuario
                "- 'Tiempo de inspección' (columna 5)\n\n" + // Indica la segunda columna que debe llenar
                "Luego presione 'Calcular' para completar los cálculos.", // Instrucción final
                "Instrucciones", // Título del cuadro de diálogo
                JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo

        } catch (NumberFormatException ex) { // Captura errores si el texto no es un número válido
            JOptionPane.showMessageDialog(this, // Muestra cuadro de diálogo de error
                "Por favor, ingrese un número válido para las piezas.", // Mensaje de error claro
                "Error", // Título del cuadro de error
                JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
        }
    }

    private void calcular(ActionEvent e) { // Método que se ejecuta al presionar el botón "Calcular"
        int n = model.getRowCount(); // Obtiene el número de filas en la tabla
        if (n == 0) { // Verifica si hay filas en la tabla
            JOptionPane.showMessageDialog(this, // Muestra error si no hay filas
                "Primero debe crear las filas.", // Mensaje indicando que debe crear filas primero
                "Error", // Título del error
                JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
            return; // Sale del método si no hay filas
        }

        try { // Inicia bloque try para manejar errores de datos
            double[] tiempoEntregLlegadas = new double[n]; // Arreglo para almacenar tiempos entre llegadas ingresados manualmente
            double[] tiempoInspeccion = new double[n]; // Arreglo para almacenar tiempos de inspección ingresados manualmente
            double[] tiempoLlegada = new double[n]; // Arreglo para almacenar el tiempo acumulado de llegada de cada pieza
            double[] inicioInspeccion = new double[n]; // Arreglo para almacenar el tiempo de inicio de inspección de cada pieza
            double[] finInspeccion = new double[n]; // Arreglo para almacenar el tiempo de fin de inspección de cada pieza
            double[] duracionInspeccion = new double[n]; // Arreglo para almacenar la duración total en el proceso de cada pieza
            double[] tiempoEspera = new double[n]; // Arreglo para almacenar el tiempo de espera de cada pieza

            // Leer los valores ingresados manualmente
            for (int i = 0; i < n; i++) { // Itera a través de todas las filas para leer los valores ingresados
                Object valorTiempoLlegada = model.getValueAt(i, 1); // Obtiene el valor de tiempo entre llegadas de la fila i
                Object valorTiempoInspeccion = model.getValueAt(i, 4); // Obtiene el valor de tiempo de inspección de la fila i

                if (valorTiempoLlegada == null || valorTiempoLlegada.toString().trim().isEmpty()) { // Verifica si el campo está vacío
                    throw new NumberFormatException("Falta el valor de 'Tiempo entreg llegadas' en la fila " + (i + 1)); // Lanza excepción con mensaje específico
                }
                if (valorTiempoInspeccion == null || valorTiempoInspeccion.toString().trim().isEmpty()) { // Verifica si el campo está vacío
                    throw new NumberFormatException("Falta el valor de 'Tiempo de inspección' en la fila " + (i + 1)); // Lanza excepción con mensaje específico
                }

                tiempoEntregLlegadas[i] = Double.parseDouble(valorTiempoLlegada.toString().trim()); // Convierte el texto a double y almacena en el arreglo
                tiempoInspeccion[i] = Double.parseDouble(valorTiempoInspeccion.toString().trim()); // Convierte el texto a double y almacena en el arreglo
            }

            // Realizar los cálculos
            for (int i = 0; i < n; i++) { // Itera a través de cada pieza para realizar todos los cálculos
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
                finInspeccion[i] = inicioInspeccion[i] + tiempoInspeccion[i]; // Suma el tiempo de inicio más la duración de la inspección

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

                // Actualizar la tabla con los valores calculados
                model.setValueAt(String.format("%.6f", tiempoLlegada[i]), i, 2); // Actualiza la celda con el tiempo de llegada formateado a 6 decimales
                model.setValueAt(String.format("%.6f", inicioInspeccion[i]), i, 3); // Actualiza la celda con el inicio de inspección formateado
                model.setValueAt(String.format("%.6f", finInspeccion[i]), i, 5); // Actualiza la celda con el fin de inspección formateado
                model.setValueAt(String.format("%.6f", duracionInspeccion[i]), i, 6); // Actualiza la celda con la duración total formateada
                model.setValueAt(String.format("%.6f", tiempoEspera[i]), i, 7); // Actualiza la celda con el tiempo de espera formateado
                model.setValueAt(String.format("%.6f", tiempoPromInspeccion), i, 8); // Actualiza la celda con el tiempo promedio formateado
            }

            // Calcular y mostrar estadísticas adicionales
            double sumaDuracionInspeccion = 0; // Inicializa la suma de todas las duraciones
            for (int i = 0; i < n; i++) { // Itera a través de todas las piezas
                sumaDuracionInspeccion += duracionInspeccion[i]; // Suma la duración de inspección de cada pieza
            }
            double tiempoPromedioTotal = sumaDuracionInspeccion / n; // Calcula el tiempo promedio total dividiendo la suma entre el número de piezas

            JOptionPane.showMessageDialog(this, // Muestra cuadro de diálogo con el resultado final
                String.format("Cálculos completados exitosamente.\n\nTiempo promedio de permanencia en el proceso: %.4f minutos", tiempoPromedioTotal), // Mensaje con el resultado formateado
                "Resultado de la Simulación", // Título del cuadro de resultado
                JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo

        } catch (NumberFormatException ex) { // Captura errores de conversión de datos
            JOptionPane.showMessageDialog(this, // Muestra cuadro de diálogo de error
                "Error en los datos ingresados: " + ex.getMessage() + "\n\nAsegúrese de que todos los valores sean números válidos.", // Mensaje detallado del error
                "Error de Datos", // Título del error
                JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
        }
    }

    private void limpiar(ActionEvent e) { // Método que se ejecuta al presionar el botón "Limpiar"
        model.setRowCount(0); // Elimina todas las filas de la tabla
        JOptionPane.showMessageDialog(this, // Muestra confirmación de la acción
            "Tabla limpiada. Puede crear nuevas filas.", // Mensaje confirmando que la tabla fue limpiada
            "Información", // Título del mensaje
            JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación
        SwingUtilities.invokeLater(() -> new ejercicio1_manual().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing
    }
}
