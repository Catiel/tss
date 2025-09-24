package actividad_6.ejercicio4; // Declaración del paquete donde se encuentra la clase de versión con ingreso manual

import javax.swing.*; // Importa clases para crear interfaces gráficas con componentes Swing como botones, tablas y ventanas
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla editable que permite personalizar qué celdas son modificables
import java.awt.*; // Importa clases para manejo de layouts, eventos y componentes gráficos básicos de AWT
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acciones como clicks de botones y activaciones de menús

public class ejercicio4_manual extends JFrame { // Declaración de la clase que extiende JFrame para crear ventana con ingreso manual de valores Rn

    private JTextField txtDias; // Campo de texto editable para ingresar el número de días que se van a simular en el sistema
    private JTextField txtCapacidadBodega; // Campo de texto para ingresar la capacidad máxima de almacenamiento de la bodega en kilogramos
    private JTextField txtCostoOrdenar; // Campo de texto para ingresar el costo fijo en pesos por realizar un pedido de reabastecimiento al proveedor
    private JTextField txtCostoFaltante; // Campo de texto para ingresar el costo unitario en pesos por cada kilogramo de inventario faltante
    private JTextField txtCostoMantenimiento; // Campo de texto para ingresar el costo unitario en pesos por cada kilogramo de inventario almacenado
    private JTextField txtMediaDemanda; // Campo de texto para ingresar la media de la distribución exponencial que modela la demanda diaria
    private DefaultTableModel model; // Modelo de datos personalizado para la tabla que permite edición selectiva de celdas específicas
    private JTable tabla; // Componente visual de tabla que muestra los datos de simulación y permite edición manual de valores Rn
    private JButton btnCrearFilas; // Botón de acción que genera filas vacías en la tabla según el número de días especificado
    private JButton btnSimular; // Botón de acción que ejecuta la simulación usando los valores Rn ingresados manualmente por el usuario

    public ejercicio4_manual() { // Constructor de la clase que inicializa la ventana principal y todos sus componentes gráficos
        setTitle("Simulacion Inventario Azucar - Ingreso Manual"); // Establece el título específico de la ventana para la versión de ingreso manual
        setSize(1400, 600); // Define las dimensiones de la ventana (1400 píxeles de ancho por 600 píxeles de alto) para acomodar tabla ancha
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que toda la aplicación termine cuando se cierre esta ventana principal
        setLocationRelativeTo(null); // Centra automáticamente la ventana en el centro de la pantalla del usuario

        initComponents(); // Llama al método que inicializa y configura todos los componentes de la interfaz gráfica
    }

    private void initComponents() { // Método privado que crea e inicializa todos los componentes de la interfaz de usuario
        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout()); // Crea panel contenedor con disposición de flujo horizontal para organizar controles de entrada

        panelSuperior.add(new JLabel("Número de Dias:")); // Agrega etiqueta descriptiva para identificar el campo de entrada del número de días
        txtDias = new JTextField("14", 5); // Crea campo de texto editable con valor inicial "14" días y ancho visual de 5 caracteres
        panelSuperior.add(txtDias); // Agrega el campo de días al panel superior para que el usuario pueda modificar la duración

        panelSuperior.add(new JLabel("Capacidad Bodega (Kg):")); // Agrega etiqueta para identificar el campo de capacidad máxima de almacenamiento
        txtCapacidadBodega = new JTextField("700", 5); // Crea campo con valor por defecto de 700 kilogramos y ancho de 5 caracteres
        panelSuperior.add(txtCapacidadBodega); // Agrega el campo de capacidad al panel de controles de entrada

        panelSuperior.add(new JLabel("Costo de ordenar ($):")); // Agrega etiqueta para identificar el costo fijo de realizar pedidos al proveedor
        txtCostoOrdenar = new JTextField("1000", 5); // Crea campo con valor por defecto de $1000 pesos por pedido y ancho de 5 caracteres
        panelSuperior.add(txtCostoOrdenar); // Agrega el campo de costo de pedidos al panel de parámetros

        panelSuperior.add(new JLabel("Costo de faltante ($ por Kg):")); // Agrega etiqueta para el costo unitario cuando hay escasez de inventario
        txtCostoFaltante = new JTextField("6", 5); // Crea campo con valor por defecto de $6 pesos por kilogramo faltante y ancho de 5 caracteres
        panelSuperior.add(txtCostoFaltante); // Agrega el campo de costo por faltante al panel de configuración

        panelSuperior.add(new JLabel("Costo de mantenimiento ($ por Kg):")); // Agrega etiqueta para el costo unitario de mantener inventario en bodega
        txtCostoMantenimiento = new JTextField("1", 5); // Crea campo con valor por defecto de $1 peso por kilogramo almacenado y ancho de 5 caracteres
        panelSuperior.add(txtCostoMantenimiento); // Agrega el campo de costo de almacenamiento al panel de parámetros

        panelSuperior.add(new JLabel("Media Demanda (Kg/dia):")); // Agrega etiqueta para la media de la distribución exponencial de demanda diaria
        txtMediaDemanda = new JTextField("100", 5); // Crea campo con valor por defecto de 100 kilogramos por día y ancho de 5 caracteres
        panelSuperior.add(txtMediaDemanda); // Agrega el campo de media de demanda al panel de configuración

        // Botones
        btnCrearFilas = new JButton("Crear Filas"); // Crea botón de acción para generar filas vacías en la tabla según días especificados
        btnSimular = new JButton("Simular"); // Crea botón de acción para ejecutar la simulación con los valores Rn ingresados manualmente
        btnSimular.setEnabled(false); // Deshabilita inicialmente el botón de simular hasta que se creen las filas en la tabla

        panelSuperior.add(btnCrearFilas); // Agrega el botón de crear filas al panel superior de controles
        panelSuperior.add(btnSimular); // Agrega el botón de simular al panel superior de controles

        // Configurar tabla
        String[] columnas = { // Define el array de strings con los nombres de todas las columnas para la tabla de resultados
                "Dia", "Inventario Inicial (Kg)", "Entrega del Proveedor (Kg)", "Inventario Total (Kg)", // Primeras 4 columnas: identificador de día, inventario al inicio, cantidad recibida e inventario disponible
                "Rn Demanda", "Demanda (Kg)", "Venta (Kg)", "Inventario Final (Kg)", // Siguientes 4 columnas: número aleatorio ingresado manualmente, demanda calculada, venta efectiva e inventario restante
                "Ventas Perdidas (Kg)", "Costo de ordenar ($)", "Costo de faltante ($)", // Siguientes 3 columnas: ventas no realizadas por falta de stock, costo de pedidos y costo por faltantes
                "Costo de mantenimiento ($)", "Costo total ($)" // Últimas 2 columnas: costo de almacenar inventario restante y suma total de costos del día
        };

        model = new DefaultTableModel(columnas, 0) { // Crea modelo de tabla personalizado con las columnas definidas y 0 filas iniciales
            @Override
            public boolean isCellEditable(int row, int column) { // Método que define la política de editabilidad para cada celda de la tabla
                // Solo permitir editar la columna Rn Demanda (columna 4)
                return column == 4; // Retorna true únicamente para la columna 4 (Rn Demanda), haciendo editable solo esa columna
            }
        };

        tabla = new JTable(model); // Crea el componente visual de tabla utilizando el modelo personalizado de datos y editabilidad

        // Ajustar ancho de columnas para mejor visualización
        tabla.getColumnModel().getColumn(0).setPreferredWidth(50);   // Establece ancho preferido de 50 píxeles para columna Día (números pequeños)
        tabla.getColumnModel().getColumn(1).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Inventario Inicial (números medianos)
        tabla.getColumnModel().getColumn(2).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Entrega Proveedor (números medianos)
        tabla.getColumnModel().getColumn(3).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Inventario Total (números medianos)
        tabla.getColumnModel().getColumn(4).setPreferredWidth(100);  // Establece ancho preferido de 100 píxeles para columna Rn Demanda EDITABLE (decimales con 4 dígitos)
        tabla.getColumnModel().getColumn(5).setPreferredWidth(100);  // Establece ancho preferido de 100 píxeles para columna Demanda (números medianos)
        tabla.getColumnModel().getColumn(6).setPreferredWidth(100);  // Establece ancho preferido de 100 píxeles para columna Venta (números medianos)
        tabla.getColumnModel().getColumn(7).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Inventario Final (números medianos)
        tabla.getColumnModel().getColumn(8).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Ventas Perdidas (números medianos)
        tabla.getColumnModel().getColumn(9).setPreferredWidth(120);  // Establece ancho preferido de 120 píxeles para columna Costo ordenar (valores monetarios)
        tabla.getColumnModel().getColumn(10).setPreferredWidth(120); // Establece ancho preferido de 120 píxeles para columna Costo faltante (valores monetarios)
        tabla.getColumnModel().getColumn(11).setPreferredWidth(120); // Establece ancho preferido de 120 píxeles para columna Costo mantenimiento (valores monetarios)
        tabla.getColumnModel().getColumn(12).setPreferredWidth(100); // Establece ancho preferido de 100 píxeles para columna Costo total (valores monetarios)

        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con barras de desplazamiento automáticas horizontal y vertical
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS); // Fuerza que siempre se muestre la barra de desplazamiento horizontal debido al ancho de la tabla

        // Layout principal
        add(panelSuperior, BorderLayout.NORTH); // Posiciona el panel de controles y parámetros en la región norte (superior) de la ventana principal
        add(scrollPane, BorderLayout.CENTER); // Posiciona la tabla con barras de desplazamiento en la región central de la ventana principal

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas); // Asocia el método crearFilas() al evento de click del botón crear filas usando referencia de método
        btnSimular.addActionListener(this::simular); // Asocia el método simular() al evento de click del botón simular usando referencia de método
    }

    private void crearFilas(ActionEvent e) { // Método privado que maneja el evento de creación de filas vacías en la tabla para ingreso manual
        try { // Bloque try-catch para manejar excepciones de conversión de tipos y validación de datos
            int dias = Integer.parseInt(txtDias.getText()); // Convierte el texto del campo días a número entero para determinar cantidad de filas a crear

            if (dias <= 0) { // Validación para asegurar que el número de días sea un valor positivo válido
                JOptionPane.showMessageDialog(this, "El numero de dias debe ser mayor a 0", // Muestra mensaje de error si el valor no es válido
                    "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de tipo error con icono correspondiente
                return; // Termina la ejecución del método sin crear filas si la validación falla
            }

            // Limpiar tabla
            model.setRowCount(0); // Elimina todas las filas existentes en la tabla para comenzar con una tabla completamente vacía

            // Crear filas vacías
            for (int dia = 1; dia <= dias; dia++) { // Bucle que itera desde día 1 hasta el número de días especificado por el usuario
                model.addRow(new Object[] { // Agrega una nueva fila al modelo de tabla con valores iniciales para cada columna
                    dia,           // Columna 0: Número del día actual (único valor pre-llenado)
                    "",           // Columna 1: Inventario Inicial (se calculará automáticamente después)
                    "",           // Columna 2: Entrega del Proveedor (se calculará automáticamente después)
                    "",           // Columna 3: Inventario Total (se calculará automáticamente después)
                    "",           // Columna 4: Rn Demanda - CAMPO EDITABLE (debe ser llenado manualmente por el usuario)
                    "",           // Columna 5: Demanda (se calculará automáticamente después usando Rn)
                    "",           // Columna 6: Venta (se calculará automáticamente después)
                    "",           // Columna 7: Inventario Final (se calculará automáticamente después)
                    "",           // Columna 8: Ventas Perdidas (se calculará automáticamente después)
                    "",           // Columna 9: Costo de ordenar (se calculará automáticamente después)
                    "",           // Columna 10: Costo de faltante (se calculará automáticamente después)
                    "",           // Columna 11: Costo de mantenimiento (se calculará automáticamente después)
                    ""            // Columna 12: Costo total (se calculará automáticamente después)
                });
            }

            btnSimular.setEnabled(true); // Habilita el botón de simular ahora que las filas han sido creadas exitosamente
            JOptionPane.showMessageDialog(this, // Muestra mensaje informativo al usuario sobre el siguiente paso a realizar
                "Se han creado " + dias + " filas.\n" + // Informa cuántas filas fueron creadas basado en los días especificados
                "Ahora ingrese manualmente los valores Rn en la columna 'Rn Demanda'.\n" + // Instruye al usuario sobre qué hacer a continuación
                "Los valores deben estar entre 0 y 1 (ejemplo: 0.9350, 0.1307, etc.)", // Proporciona ejemplos de valores válidos para Rn
                "Filas creadas", JOptionPane.INFORMATION_MESSAGE); // Especifica que es un mensaje informativo con icono correspondiente

        } catch (NumberFormatException ex) { // Captura excepciones cuando el texto no puede ser convertido a número entero
            JOptionPane.showMessageDialog(this, "Por favor ingrese un número válido para los dias", // Mensaje de error específico para formato incorrecto
                "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
        }
    }

    private void simular(ActionEvent e) { // Método privado que ejecuta la lógica completa de simulación usando valores Rn ingresados manualmente
        try { // Bloque try-catch principal para manejar todas las excepciones durante la simulación
            // Validar que todos los valores Rn estén ingresados
            int filas = model.getRowCount(); // Obtiene el número total de filas en la tabla para iterar sobre todas ellas
            for (int i = 0; i < filas; i++) { // Bucle que valida cada fila para asegurar que todos los valores Rn estén ingresados correctamente
                Object rnValue = model.getValueAt(i, 4); // Obtiene el valor de la celda Rn Demanda (columna 4) para la fila actual
                if (rnValue == null || rnValue.toString().trim().isEmpty()) { // Verifica si el valor está vacío, es nulo o contiene solo espacios en blanco
                    JOptionPane.showMessageDialog(this, // Muestra mensaje de error específico indicando qué valor falta
                        "Falta ingresar el valor Rn para el dia " + (i + 1), // Indica exactamente qué día necesita el valor Rn (i+1 porque i empieza en 0)
                        "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
                    return; // Termina la ejecución inmediatamente si encuentra un valor faltante
                }

                try { // Bloque try-catch anidado para validar el formato y rango de cada valor Rn individual
                    double rn = Double.parseDouble(rnValue.toString().trim()); // Convierte el texto a número decimal eliminando espacios en blanco
                    if (rn < 0 || rn >= 1) { // Valida que el valor esté en el rango correcto para números aleatorios (0 ≤ Rn < 1)
                        JOptionPane.showMessageDialog(this, // Muestra error si el valor está fuera del rango válido
                            "El valor Rn del dia " + (i + 1) + " debe estar entre 0 y 1 (exclusive)", // Especifica el rango válido y qué día tiene el error
                            "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
                        return; // Termina la ejecución si encuentra un valor fuera de rango
                    }
                } catch (NumberFormatException ex) { // Captura errores cuando el texto no puede ser convertido a número decimal
                    JOptionPane.showMessageDialog(this, // Muestra error específico de formato numérico
                        "El valor Rn del dia " + (i + 1) + " no es un número válido", // Indica exactamente qué día tiene formato incorrecto
                        "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
                    return; // Termina la ejecución si encuentra formato incorrecto
                }
            }

            // Obtener parámetros
            double capacidadBodega = Double.parseDouble(txtCapacidadBodega.getText()); // Convierte el texto de capacidad a número decimal para cálculos
            double costoOrdenar = Double.parseDouble(txtCostoOrdenar.getText()); // Convierte el texto de costo de pedidos a número decimal
            double costoFaltante = Double.parseDouble(txtCostoFaltante.getText()); // Convierte el texto de costo por faltante a número decimal
            double costoMantenimiento = Double.parseDouble(txtCostoMantenimiento.getText()); // Convierte el texto de costo de almacenamiento a número decimal
            double mediaDemanda = Double.parseDouble(txtMediaDemanda.getText()); // Convierte el texto de media de demanda a número decimal

            // Realizar simulación
            double inventarioInicial = 0; // Variable para rastrear el inventario disponible al comenzar cada día (inicia en cero el primer día)
            double inventarioFinalAnterior = 0; // Variable para almacenar el inventario final del día anterior (usado en cálculos de entrega del proveedor)

            for (int i = 0; i < filas; i++) { // Bucle principal que procesa cada día de la simulación usando los valores Rn ingresados
                int dia = i + 1; // Convierte el índice de fila (base 0) a número de día (base 1) para cálculos y presentación

                // Fórmula de entregas del proveedor
                double entregaProveedor; // Variable local para almacenar la cantidad que entrega el proveedor en el día actual
                if (dia == 1) { // Condición especial para manejar la lógica diferente del primer día de simulación
                    entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0; // Primer día: si día módulo 7 es 1, entrega capacidad completa, sino cero
                } else { // Condición para todos los días posteriores al primero en la simulación
                    entregaProveedor = (dia % 7 == 0) ? Math.max(0, capacidadBodega - inventarioFinalAnterior) : 0; // Días siguientes: si día módulo 7 es cero, entrega hasta completar capacidad máxima
                }

                double inventarioTotal = inventarioInicial + entregaProveedor; // Calcula el inventario total disponible sumando inventario inicial más entrega del proveedor

                // Obtener el valor Rn ingresado manualmente
                double rnDemanda = Double.parseDouble(model.getValueAt(i, 4).toString()); // Obtiene y convierte el valor Rn que el usuario ingresó manualmente en la tabla

                // Calcular demanda usando la fórmula: =-100*LN(1-E7)
                double demanda = -mediaDemanda * Math.log(1 - rnDemanda); // Aplica transformación logarítmica inversa usando el valor Rn manual para generar demanda con distribución exponencial

                // Venta: MIN(demanda, inventarioTotal)
                double venta = Math.min(demanda, inventarioTotal); // La venta real es el menor valor entre la demanda del cliente y el inventario total disponible

                // Inventario final
                double inventarioFinal = inventarioTotal - venta; // Calcula el inventario que permanece al final del día después de satisfacer todas las ventas posibles

                // Ventas perdidas: MAX(0, demanda - inventarioTotal)
                double ventasPerdidas = Math.max(0, demanda - inventarioTotal); // Calcula las ventas no realizadas cuando la demanda excede el inventario disponible (cero si hay suficiente stock)

                // Costos
                double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0; // Aplica el costo fijo de pedido únicamente cuando efectivamente hubo entrega del proveedor
                double costoFalt = ventasPerdidas * costoFaltante; // Calcula el costo total por ventas perdidas multiplicando cantidad perdida por costo unitario de faltante
                double costoMant = inventarioFinal * costoMantenimiento; // Calcula el costo total de almacenamiento multiplicando inventario final por costo unitario de mantenimiento
                double costoTotal = costoOrden + costoFalt + costoMant; // Suma todos los componentes de costo (pedido, faltante, mantenimiento) para obtener costo total del día

                // Actualizar fila con resultados (formato sin decimales excepto Rn)
                model.setValueAt(String.format("%.0f", inventarioInicial), i, 1); // Actualiza celda de inventario inicial con formato de número entero sin decimales
                model.setValueAt(String.format("%.0f", entregaProveedor), i, 2); // Actualiza celda de entrega del proveedor con formato de número entero
                model.setValueAt(String.format("%.0f", inventarioTotal), i, 3); // Actualiza celda de inventario total con formato de número entero
                // La columna 4 (Rn) ya tiene el valor ingresado manualmente
                model.setValueAt(String.format("%.0f", demanda), i, 5); // Actualiza celda de demanda calculada con formato de número entero
                model.setValueAt(String.format("%.0f", venta), i, 6); // Actualiza celda de venta realizada con formato de número entero
                model.setValueAt(String.format("%.0f", inventarioFinal), i, 7); // Actualiza celda de inventario final con formato de número entero
                model.setValueAt(String.format("%.0f", ventasPerdidas), i, 8); // Actualiza celda de ventas perdidas con formato de número entero
                model.setValueAt(String.format("$%.0f", costoOrden), i, 9); // Actualiza celda de costo de pedidos con formato monetario entero y símbolo de pesos
                model.setValueAt(String.format("$%.0f", costoFalt), i, 10); // Actualiza celda de costo por faltante con formato monetario entero
                model.setValueAt(String.format("$%.0f", costoMant), i, 11); // Actualiza celda de costo de mantenimiento con formato monetario entero
                model.setValueAt(String.format("$%.0f", costoTotal), i, 12); // Actualiza celda de costo total del día con formato monetario entero

                // Actualizar para siguiente iteración
                inventarioInicial = inventarioFinal; // El inventario final del día actual se convierte en el inventario inicial del día siguiente para continuidad
                inventarioFinalAnterior = inventarioFinal; // Guarda el inventario final actual para usarlo en cálculos de entrega del proveedor en futuros ciclos semanales
            }

            JOptionPane.showMessageDialog(this, "Simulacion completada exitosamente", // Muestra mensaje de confirmación cuando toda la simulación se ejecuta sin errores
                "Éxito", JOptionPane.INFORMATION_MESSAGE); // Especifica que es un mensaje informativo de éxito con icono correspondiente

        } catch (NumberFormatException ex) { // Captura excepciones de conversión de parámetros de entrada a números decimales
            JOptionPane.showMessageDialog(this, "Por favor verifique que todos los parámetros sean números válidos", // Mensaje de error genérico para parámetros con formato incorrecto
                "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
        } catch (Exception ex) { // Captura cualquier otra excepción no prevista durante la ejecución de la simulación
            JOptionPane.showMessageDialog(this, "Error durante la simulacion: " + ex.getMessage(), // Muestra el mensaje específico de la excepción para diagnóstico
                "Error", JOptionPane.ERROR_MESSAGE); // Especifica que es un mensaje de error con icono correspondiente
        }
    }

    public static void main(String[] args) { // Método principal estático que sirve como punto de entrada para ejecutar la aplicación de forma independiente
        SwingUtilities.invokeLater(() -> new ejercicio4_manual().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing para thread safety y hace visible la ventana inmediatamente
    }
}
