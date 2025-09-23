package actividad_6.ejercicio3; // Declaración del paquete donde se encuentra la clase

// Importación de clases necesarias para la interfaz gráfica y funcionalidad
import javax.swing.*; // Para componentes de interfaz gráfica como JFrame, JButton, JTextField, JTable, etc.
import javax.swing.table.DefaultTableModel; // Para el modelo de datos de la tabla que permite edición personalizada
import java.awt.*; // Para layouts y componentes de AWT como BorderLayout, FlowLayout
import java.awt.event.ActionEvent; // Para el manejo de eventos de acciones (clicks de botones)

public class ejercicio3Manual extends JFrame { // Declaración de la clase que extiende JFrame para crear una ventana con ingreso manual

    // Declaración de campos de texto para los parámetros de entrada
    private JTextField txtNumEnsambles; // Campo editable para ingresar el número de ensambles a simular
    private JTextField txtMinBarraA; // Campo de solo lectura para mostrar el valor mínimo de la Barra A
    private JTextField txtMaxBarraA; // Campo de solo lectura para mostrar el valor máximo de la Barra A
    private JTextField txtValorEsperadoErlang; // Campo de solo lectura para mostrar el valor esperado de la distribución Erlang
    private JTextField txtFormaErlang; // Campo de solo lectura para mostrar el parámetro de forma k de la distribución Erlang
    private JTextField txtEspecInf; // Campo de solo lectura para mostrar la especificación inferior de longitud total
    private JTextField txtEspecSup; // Campo de solo lectura para mostrar la especificación superior de longitud total
    private DefaultTableModel model; // Modelo de datos personalizado que maneja el contenido y editabilidad de la tabla
    private JTable tabla; // Tabla que muestra los datos y permite ingreso manual en columnas específicas
    private JButton btnCrearFilas; // Botón para generar las filas vacías de la tabla según el número de ensambles
    private JButton btnSimular; // Botón para ejecutar la simulación con los datos ingresados manualmente

    public ejercicio3Manual() { // Constructor de la clase que inicializa todos los componentes de la interfaz
        setTitle("Simulación Barras Defectuosas - Ingreso Manual"); // Establece el título específico para la versión manual
        setSize(1400, 600); // Define dimensiones más amplias (1400x600) para acomodar todas las columnas de la tabla
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla del usuario

        initComponents(); // Llama al método que inicializa y configura todos los componentes de la interfaz
    }

    private void initComponents() { // Método que organiza e inicializa todos los componentes de la interfaz gráfica
        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout()); // Crea panel superior con layout de flujo horizontal para controles

        panelSuperior.add(new JLabel("Número de Ensambles:")); // Agrega etiqueta descriptiva para el campo editable
        txtNumEnsambles = new JTextField("15", 5); // Crea campo de texto editable con valor inicial 15 y ancho de 5 caracteres
        panelSuperior.add(txtNumEnsambles); // Agrega el campo editable al panel superior

        panelSuperior.add(new JLabel("Mín Barra A (cm):")); // Agrega etiqueta para el parámetro mínimo de Barra A
        txtMinBarraA = new JTextField("45", 5); // Crea campo con valor fijo "45" cm y ancho de 5 caracteres
        txtMinBarraA.setEnabled(false); // Deshabilita la edición para mostrar solo información fija
        panelSuperior.add(txtMinBarraA); // Agrega el campo de solo lectura al panel

        panelSuperior.add(new JLabel("Máx Barra A (cm):")); // Agrega etiqueta para el parámetro máximo de Barra A
        txtMaxBarraA = new JTextField("55", 5); // Crea campo con valor fijo "55" cm y ancho de 5 caracteres
        txtMaxBarraA.setEnabled(false); // Deshabilita la edición para parámetro fijo
        panelSuperior.add(txtMaxBarraA); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Valor Esperado Erlang:")); // Agrega etiqueta para la media de la distribución Erlang
        txtValorEsperadoErlang = new JTextField("30", 5); // Crea campo con valor fijo "30" cm y ancho de 5 caracteres
        txtValorEsperadoErlang.setEnabled(false); // Deshabilita la edición para parámetro fijo
        panelSuperior.add(txtValorEsperadoErlang); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Forma Erlang k:")); // Agrega etiqueta para el parámetro de forma de la distribución Erlang
        txtFormaErlang = new JTextField("4", 5); // Crea campo con valor fijo "4" y ancho de 5 caracteres
        txtFormaErlang.setEnabled(false); // Deshabilita la edición para parámetro fijo
        panelSuperior.add(txtFormaErlang); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Espec Inf:")); // Agrega etiqueta abreviada para especificación inferior
        txtEspecInf = new JTextField("70", 5); // Crea campo con valor fijo "70" cm y ancho de 5 caracteres
        txtEspecInf.setEnabled(false); // Deshabilita la edición para parámetro fijo
        panelSuperior.add(txtEspecInf); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Espec Sup:")); // Agrega etiqueta abreviada para especificación superior
        txtEspecSup = new JTextField("90", 5); // Crea campo con valor fijo "90" cm y ancho de 5 caracteres
        txtEspecSup.setEnabled(false); // Deshabilita la edición para parámetro fijo
        panelSuperior.add(txtEspecSup); // Agrega el campo al panel superior

        // Botones
        btnCrearFilas = new JButton("Crear Filas"); // Crea botón para generar filas vacías en la tabla
        btnSimular = new JButton("Simular"); // Crea botón para ejecutar la simulación
        btnSimular.setEnabled(false); // Deshabilita inicialmente hasta que se creen las filas

        panelSuperior.add(btnCrearFilas); // Agrega el botón de crear filas al panel superior
        panelSuperior.add(btnSimular); // Agrega el botón de simular al panel superior

        // Configurar tabla
        String[] columnas = { // Define los nombres de las columnas para la tabla de simulación
            "Ensambles", "Rn", "Dimensión Barra A (cm)", "Rn1", // Primeras 4 columnas: número, Rn para uniforme, dimensión A, primer Rn Erlang
            "Rn2", "Rn3", "Rn4", "Dimensión Barra B (cm)", // Siguientes 4 columnas: Rn2-4 para Erlang, dimensión B calculada
            "Longitud total (cm)", "Especificación inferior (cm)", // Columnas para longitud total y límite inferior
            "Especificación superior (cm)", "¿Defectuosa? 1=Si, 0=No", // Columnas para límite superior y estado defectuoso
            "Piezas defectuosas acumuladas", "% piezas defectuosas" // Columnas para conteo acumulado y porcentaje de defectos
        };

        model = new DefaultTableModel(columnas, 0) { // Crea modelo de tabla personalizado con las columnas y 0 filas iniciales
            @Override
            public boolean isCellEditable(int row, int column) { // Método que define qué celdas son editables
                // Solo permitir editar las columnas Rn (1), Rn1 (3), Rn2 (4), Rn3 (5), Rn4 (6)
                return column == 1 || column == 3 || column == 4 || column == 5 || column == 6; // Retorna true solo para columnas de números aleatorios
            }
        };

        tabla = new JTable(model); // Crea la tabla usando el modelo personalizado que permite edición selectiva

        // Ajustar ancho de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(80);  // Ensambles - ancho mínimo para números
        tabla.getColumnModel().getColumn(1).setPreferredWidth(80);  // Rn - ancho para números decimales
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120); // Dimensión Barra A - ancho para valores con decimales
        tabla.getColumnModel().getColumn(3).setPreferredWidth(80);  // Rn1 - ancho para números decimales
        tabla.getColumnModel().getColumn(4).setPreferredWidth(80);  // Rn2 - ancho para números decimales
        tabla.getColumnModel().getColumn(5).setPreferredWidth(80);  // Rn3 - ancho para números decimales
        tabla.getColumnModel().getColumn(6).setPreferredWidth(80);  // Rn4 - ancho para números decimales
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120); // Dimensión Barra B - ancho para valores con decimales
        tabla.getColumnModel().getColumn(8).setPreferredWidth(120); // Longitud total - ancho para valores con decimales

        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con scroll para manejar muchas filas

        // Layout
        add(panelSuperior, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior de la ventana
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro de la ventana

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas); // Asocia el método crearFilas al evento click del botón
        btnSimular.addActionListener(this::simular); // Asocia el método simular al evento click del botón

        // Listener para el campo de número de ensambles
        txtNumEnsambles.addActionListener(e -> habilitarCrearFilas()); // Detecta cuando se presiona Enter en el campo
        txtNumEnsambles.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { // Listener para detectar cambios en tiempo real
            public void insertUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al insertar texto
            public void removeUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al eliminar texto
            public void changedUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al cambiar atributos
        });
    }

    private void habilitarCrearFilas() { // Método que se ejecuta cuando el usuario modifica el número de ensambles
        btnCrearFilas.setEnabled(true); // Habilita el botón "Crear Filas" para permitir generar nueva tabla
        btnSimular.setEnabled(false); // Deshabilita el botón "Simular" hasta que se creen nuevas filas
    }

    private void crearFilas(ActionEvent e) { // Método que genera las filas vacías de la tabla según el número de ensambles
        try { // Manejo de errores por entrada inválida del usuario
            int numEnsambles = Integer.parseInt(txtNumEnsambles.getText()); // Convierte el texto ingresado a número entero
            if (numEnsambles <= 0) { // Valida que el número sea positivo
                JOptionPane.showMessageDialog(this, "El número de ensambles debe ser mayor a 0"); // Muestra mensaje de error
                return; // Termina la ejecución si el número es inválido
            }

            // Limpiar tabla
            model.setRowCount(0); // Elimina todas las filas existentes de la tabla

            // Crear filas con valores por defecto
            for (int i = 0; i < numEnsambles; i++) { // Ciclo para crear una fila por cada ensamble solicitado
                Object[] fila = new Object[14]; // Array que representa una fila con 14 columnas
                fila[0] = i + 1;    // Ensambles - número consecutivo empezando en 1
                fila[1] = "";       // Rn - editable, inicialmente vacío para ingreso manual
                fila[2] = "";       // Dimensión Barra A - calculado automáticamente, inicialmente vacío
                fila[3] = "";       // Rn1 - editable, inicialmente vacío para ingreso manual
                fila[4] = "";       // Rn2 - editable, inicialmente vacío para ingreso manual
                fila[5] = "";       // Rn3 - editable, inicialmente vacío para ingreso manual
                fila[6] = "";       // Rn4 - editable, inicialmente vacío para ingreso manual
                fila[7] = "";       // Dimensión Barra B - calculado automáticamente, inicialmente vacío
                fila[8] = "";       // Longitud total - calculado automáticamente, inicialmente vacío
                fila[9] = "";       // Especificación inferior - calculado automáticamente, inicialmente vacío
                fila[10] = "";      // Especificación superior - calculado automáticamente, inicialmente vacío
                fila[11] = "";      // ¿Defectuosa? - calculado automáticamente, inicialmente vacío
                fila[12] = "";      // Piezas defectuosas acumuladas - calculado automáticamente, inicialmente vacío
                fila[13] = "";      // % piezas defectuosas - calculado automáticamente, inicialmente vacío

                model.addRow(fila); // Agrega la fila completa al modelo de la tabla
            }

            // Habilitar el botón simular y deshabilitar crear filas
            btnSimular.setEnabled(true); // Permite ejecutar simulación una vez que las filas están creadas
            btnCrearFilas.setEnabled(false); // Deshabilita la creación de filas hasta el próximo cambio

            JOptionPane.showMessageDialog(this, "Tabla generada. Ahora ingrese los valores Rn manualmente en las columnas editables (Rn, Rn1, Rn2, Rn3, Rn4)."); // Mensaje informativo para el usuario

        } catch (NumberFormatException ex) { // Captura errores si el texto no es un número válido
            JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido de ensambles", "Error", JOptionPane.ERROR_MESSAGE); // Muestra diálogo de error específico
        }
    }

    private void simular(ActionEvent e) { // Método principal que ejecuta la simulación con los datos ingresados manualmente
        try { // Manejo de errores durante la simulación
            // Validar que todos los campos estén llenos
            if (!validarDatos()) { // Verifica que todos los valores Rn hayan sido ingresados
                return; // Termina si faltan datos
            }

            double minA = Double.parseDouble(txtMinBarraA.getText()); // Obtiene el valor mínimo para la distribución uniforme de Barra A
            double maxA = Double.parseDouble(txtMaxBarraA.getText()); // Obtiene el valor máximo para la distribución uniforme de Barra A
            double valorEsperado = Double.parseDouble(txtValorEsperadoErlang.getText()); // Obtiene la media para la distribución Erlang
            int kErlang = Integer.parseInt(txtFormaErlang.getText()); // Obtiene el parámetro de forma k para la distribución Erlang
            double especInf = Double.parseDouble(txtEspecInf.getText()); // Obtiene la especificación inferior para determinar defectos
            double especSup = Double.parseDouble(txtEspecSup.getText()); // Obtiene la especificación superior para determinar defectos

            int acumDefectuosas = 0; // Inicializa contador acumulado de piezas defectuosas
            int numFilas = model.getRowCount(); // Obtiene el número de filas (ensambles) en la tabla

            for (int i = 0; i < numFilas; i++) { // Ciclo que procesa cada fila de la tabla
                // Obtener valores ingresados manualmente
                double rnA = Double.parseDouble(model.getValueAt(i, 1).toString()); // Obtiene el valor Rn ingresado para Barra A
                double rn1 = Double.parseDouble(model.getValueAt(i, 3).toString()); // Obtiene el primer valor Rn ingresado para Erlang
                double rn2 = Double.parseDouble(model.getValueAt(i, 4).toString()); // Obtiene el segundo valor Rn ingresado para Erlang
                double rn3 = Double.parseDouble(model.getValueAt(i, 5).toString()); // Obtiene el tercer valor Rn ingresado para Erlang
                double rn4 = Double.parseDouble(model.getValueAt(i, 6).toString()); // Obtiene el cuarto valor Rn ingresado para Erlang

                // Validar que los valores estén entre 0 y 1
                if (!validarRango(rnA, rn1, rn2, rn3, rn4, i + 1)) { // Verifica que todos los valores Rn estén en el rango válido [0,1]
                    return; // Termina si hay valores fuera del rango
                }

                // Calcular dimensiones
                double dimBarraA = minA + (maxA - minA) * rnA; // Calcula dimensión de Barra A usando distribución uniforme inversa

                // Calcular Erlang
                double lnProduct = Math.log(1 - rn1) + Math.log(1 - rn2) + Math.log(1 - rn3) + Math.log(1 - rn4); // Calcula la suma de logaritmos naturales para Erlang
                double dimBarraB = -(valorEsperado / kErlang) * lnProduct; // Aplica la fórmula de distribución Erlang inversa

                double longitudTotal = dimBarraA + dimBarraB; // Calcula la longitud total sumando dimensiones de ambas barras

                boolean defectuosa = (longitudTotal < especInf) || (longitudTotal > especSup); // Determina si la pieza es defectuosa comparando con especificaciones
                int defectInt = defectuosa ? 1 : 0; // Convierte el booleano a entero (1 = defectuosa, 0 = aceptable)
                acumDefectuosas += defectInt; // Suma al contador acumulado si la pieza es defectuosa
                double porcentaje = (double) acumDefectuosas / (i + 1); // Calcula el porcentaje de piezas defectuosas hasta el momento

                // Actualizar fila con resultados calculados
                model.setValueAt(String.format("%.2f", dimBarraA), i, 2); // Actualiza dimensión de Barra A con formato de 2 decimales
                model.setValueAt(String.format("%.2f", dimBarraB), i, 7); // Actualiza dimensión de Barra B con formato de 2 decimales
                model.setValueAt(String.format("%.2f", longitudTotal), i, 8); // Actualiza longitud total con formato de 2 decimales
                model.setValueAt(String.format("%.2f", especInf), i, 9); // Actualiza especificación inferior con formato de 2 decimales
                model.setValueAt(String.format("%.2f", especSup), i, 10); // Actualiza especificación superior con formato de 2 decimales
                model.setValueAt(defectInt, i, 11); // Actualiza estado defectuoso como entero (1 o 0)
                model.setValueAt(acumDefectuosas, i, 12); // Actualiza contador acumulado de piezas defectuosas
                model.setValueAt(String.format("%.2f%%", porcentaje * 100), i, 13); // Actualiza porcentaje con formato de 2 decimales y símbolo %
            }

            JOptionPane.showMessageDialog(this, "Simulación completada exitosamente."); // Mensaje de confirmación de simulación exitosa

        } catch (NumberFormatException ex) { // Captura errores de conversión de texto a número
            JOptionPane.showMessageDialog(this, "Error en los datos ingresados: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Muestra error específico de conversión
        } catch (Exception ex) { // Captura cualquier otro error inesperado durante la simulación
            JOptionPane.showMessageDialog(this, "Error durante la simulación: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); // Muestra error genérico con detalles
        }
    }

    private boolean validarDatos() { // Método que verifica que todos los campos editables estén completos antes de simular
        int numFilas = model.getRowCount(); // Obtiene el número de filas en la tabla
        if (numFilas == 0) { // Verifica si la tabla está vacía
            JOptionPane.showMessageDialog(this, "Primero genere la tabla con 'Crear Filas'"); // Mensaje si no hay filas
            return false; // Retorna false si no hay datos para validar
        }

        // Validar columnas editables: Rn (1), Rn1 (3), Rn2 (4), Rn3 (5), Rn4 (6)
        int[] columnasEditables = {1, 3, 4, 5, 6}; // Array con los índices de las columnas que deben ser editadas por el usuario
        String[] nombresColumnas = {"Rn", "Rn1", "Rn2", "Rn3", "Rn4"}; // Array con los nombres legibles de las columnas editables

        for (int i = 0; i < numFilas; i++) { // Ciclo para verificar cada fila
            for (int j = 0; j < columnasEditables.length; j++) { // Ciclo para verificar cada columna editable
                int colIndex = columnasEditables[j]; // Obtiene el índice de la columna actual
                Object valor = model.getValueAt(i, colIndex); // Obtiene el valor de la celda específica
                if (valor == null || valor.toString().trim().isEmpty()) { // Verifica si la celda está vacía o es null
                    JOptionPane.showMessageDialog(this, // Muestra mensaje específico indicando qué celda falta
                        "Falta ingresar valor en fila " + (i + 1) + ", columna " + nombresColumnas[j]); // Mensaje con ubicación exacta del problema
                    return false; // Retorna false si encuentra algún campo vacío
                }
            }
        }
        return true; // Retorna true si todos los campos editables están completos
    }

    private boolean validarRango(double... valores) { // Método sobrecargado que valida un número variable de valores
        for (double valor : valores) { // Ciclo que verifica cada valor pasado como parámetro
            if (valor < 0 || valor > 1) { // Verifica que el valor esté en el rango válido [0,1] para números aleatorios
                return false; // Retorna false si encuentra algún valor fuera del rango
            }
        }
        return true; // Retorna true si todos los valores están en el rango válido
    }

    private boolean validarRango(double rnA, double rn1, double rn2, double rn3, double rn4, int fila) { // Método específico que valida los 5 valores Rn de una fila
        if (!validarRango(rnA, rn1, rn2, rn3, rn4)) { // Utiliza el método sobrecargado para validar todos los valores
            JOptionPane.showMessageDialog(this, // Muestra mensaje de error específico para valores fuera del rango
                "Los valores Rn deben estar entre 0 y 1 (fila " + fila + ")"); // Mensaje indicando la fila problemática
            return false; // Retorna false si hay valores inválidos
        }
        return true; // Retorna true si todos los valores son válidos
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación de forma independiente
        SwingUtilities.invokeLater(() -> new ejercicio3Manual().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing para thread safety
    }
}
