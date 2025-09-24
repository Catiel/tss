package actividad_6.ejercicio4; // Declaración del paquete donde se encuentra la clase

import javax.swing.*; // Importa clases para crear interfaces gráficas con Swing
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejo de datos tabulares
import java.awt.*; // Importa clases para manejo de layouts y componentes gráficos
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acciones (clicks de botones)

public class ejercicio4 extends JFrame { // Declaración de la clase que extiende JFrame para crear una ventana

    private final JTextField txtDias; // Campo de texto para mostrar el número de días (no editable)
    private final JTextField txtCapacidadBodega; // Campo de texto para ingresar la capacidad máxima de la bodega en Kg
    private final JTextField txtCostoOrdenar; // Campo de texto para ingresar el costo fijo por realizar un pedido al proveedor
    private final JTextField txtCostoFaltante; // Campo de texto para ingresar el costo por Kg cuando hay faltante de inventario
    private final JTextField txtCostoMantenimiento; // Campo de texto para ingresar el costo por Kg de mantener inventario
    private final JTextField txtMediaDemanda; // Campo de texto para ingresar la media de la distribución exponencial de demanda
    private final DefaultTableModel model; // Modelo de datos para la tabla que mostrará los resultados de la simulación

    public ejercicio4() { // Constructor de la clase que inicializa la interfaz gráfica
        setTitle("Simulación Inventario Azúcar"); // Establece el título de la ventana principal
        setSize(1200, 500); // Define el tamaño inicial de la ventana (1200 píxeles de ancho, 500 de alto)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que la aplicación termine al cerrar la ventana
        setLocationRelativeTo(null); // Centra la ventana en la pantalla del usuario

        JPanel panelInput = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para organizar los controles de entrada

        panelInput.add(new JLabel("Numero de Días:")); // Agrega etiqueta descriptiva para el campo de días
        txtDias = new JTextField("14", 5); // Crea campo de texto con valor fijo "14" y ancho de 5 caracteres
        txtDias.setEditable(false); // Deshabilita la edición para mantener fijo en 14 días
        panelInput.add(txtDias); // Agrega el campo de días al panel de controles

        panelInput.add(new JLabel("Capacidad Bodega (Kg):")); // Agrega etiqueta para el campo de capacidad de bodega
        txtCapacidadBodega = new JTextField("700", 5); // Crea campo con valor por defecto de 700 Kg y ancho de 5 caracteres
        panelInput.add(txtCapacidadBodega); // Agrega el campo de capacidad al panel

        panelInput.add(new JLabel("Costo de ordenar ($):")); // Agrega etiqueta para el costo fijo de realizar pedidos
        txtCostoOrdenar = new JTextField("1000", 5); // Crea campo con valor por defecto de $1000 y ancho de 5 caracteres
        panelInput.add(txtCostoOrdenar); // Agrega el campo de costo de ordenar al panel

        panelInput.add(new JLabel("Costo de faltante ($ por Kg):")); // Agrega etiqueta para el costo por faltante de inventario
        txtCostoFaltante = new JTextField("6", 5); // Crea campo con valor por defecto de $6 por Kg y ancho de 5 caracteres
        panelInput.add(txtCostoFaltante); // Agrega el campo de costo de faltante al panel

        panelInput.add(new JLabel("Costo de mantenimiento ($ por Kg):")); // Agrega etiqueta para el costo de mantener inventario
        txtCostoMantenimiento = new JTextField("1", 5); // Crea campo con valor por defecto de $1 por Kg y ancho de 5 caracteres
        panelInput.add(txtCostoMantenimiento); // Agrega el campo de costo de mantenimiento al panel

        panelInput.add(new JLabel("Media Demanda (Kg/día):")); // Agrega etiqueta para la media de la distribución exponencial
        txtMediaDemanda = new JTextField("100", 5); // Crea campo con valor por defecto de 100 Kg/día y ancho de 5 caracteres
        panelInput.add(txtMediaDemanda); // Agrega el campo de media de demanda al panel

        JButton btnSimular = new JButton("Simular"); // Crea botón para ejecutar la simulación de inventario
        panelInput.add(btnSimular); // Agrega el botón de simular al panel de controles

        String[] columnas = { // Define los nombres de las columnas para la tabla de resultados
                "Dia", "Inventario Inicial (Kg)", "Entrega del Proveedor (Kg)", "Inventario Total (Kg)", // Primeras 4 columnas: día, inventario inicial, entrega y total
                "Rn Demanda", "Demanda (Kg)", "Venta (Kg)", "Inventario Final (Kg)", // Siguientes 4 columnas: número aleatorio, demanda calculada, venta realizada e inventario final
                "Ventas Perdidas (Kg)", "Costo de ordenar ($)", "Costo de faltante ($)", // Siguientes 3 columnas: ventas no realizadas por falta de inventario y costos de ordenar y faltante
                "Costo de mantenimiento ($)", "Costo total ($)" // Últimas 2 columnas: costo de mantener inventario y costo total del día
        };
        model = new DefaultTableModel(columnas, 0); // Crea el modelo de tabla con las columnas definidas y 0 filas iniciales
        JTable tabla = new JTable(model); // Crea la tabla visual usando el modelo de datos
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con barras de desplazamiento

        add(panelInput, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior de la ventana
        add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro de la ventana

        btnSimular.addActionListener(this::simular); // Asocia el método simular() al evento click del botón simular
    }

    private void simular(ActionEvent e) { // Método que ejecuta la simulación de inventario cuando se presiona el botón
        model.setRowCount(0); // Limpia todas las filas existentes en la tabla para nueva simulación

        int dias = Integer.parseInt(txtDias.getText()); // Convierte el texto del campo días a número entero
        double capacidadBodega = Double.parseDouble(txtCapacidadBodega.getText()); // Convierte el texto de capacidad a número decimal
        double costoOrdenar = Double.parseDouble(txtCostoOrdenar.getText()); // Convierte el texto de costo de ordenar a número decimal
        double costoFaltante = Double.parseDouble(txtCostoFaltante.getText()); // Convierte el texto de costo de faltante a número decimal
        double costoMantenimiento = Double.parseDouble(txtCostoMantenimiento.getText()); // Convierte el texto de costo de mantenimiento a número decimal
        double mediaDemanda = Double.parseDouble(txtMediaDemanda.getText()); // Convierte el texto de media de demanda a número decimal

        // Valores predefinidos de Rn que proporcionaste
        double[] valoresRn = { // Array con números aleatorios predefinidos para generar demanda consistente
            0.9350, 0.1307, 0.8557, 0.4987, 0.2534, // Primeros 5 valores aleatorios entre 0 y 1
            0.8885, 0.2714, 0.5620, 0.2332, 0.9056, // Siguientes 5 valores aleatorios entre 0 y 1
            0.4334, 0.4872, 0.3084, 0.0584 // Últimos 4 valores aleatorios para completar 14 días
        };

        double inventarioInicial = 0; // Variable para almacenar el inventario al inicio de cada día (comienza en 0)
        double inventarioFinalAnterior = 0; // Para el cálculo de entregas del proveedor

        for (int dia = 1; dia <= dias; dia++) { // Bucle que simula cada día desde el 1 hasta el número especificado
            // Fórmula de entregas del proveedor según tus especificaciones de Excel
            double entregaProveedor; // Variable para almacenar la cantidad entregada por el proveedor cada día
            if (dia == 1) { // Condición especial para el primer día de simulación
                // Primer día: =SI(RESIDUO(A7;7)=1;700;0)
                entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0; // Si el día módulo 7 es 1, entrega capacidad completa, sino 0
            } else { // Para todos los días posteriores al primero
                // Días siguientes: =SI(RESIDUO(A8;7)=0;700-H7;0) donde H7 es inventario final anterior
                entregaProveedor = (dia % 7 == 0) ? Math.max(0, capacidadBodega - inventarioFinalAnterior) : 0; // Si día módulo 7 es 0, entrega hasta completar capacidad
            }

            double inventarioTotal = inventarioInicial + entregaProveedor; // Suma el inventario inicial más lo entregado por el proveedor

            // Usar valor predefinido de Rn
            double rnDemanda = valoresRn[(dia - 1) % valoresRn.length]; // Obtiene el número aleatorio correspondiente al día (cicla si hay más de 14 días)

            // Demanda usando tu fórmula de Excel: =-100*LN(1-E7)
            double demanda = -mediaDemanda * Math.log(1 - rnDemanda); // Calcula demanda usando distribución exponencial inversa

            // Venta usando tu fórmula de Excel: =MIN(F7;D7) - mínimo entre demanda e inventario total
            double venta = Math.min(demanda, inventarioTotal); // La venta es el menor valor entre lo que se demanda y lo que hay disponible

            // Inventario final: inventario total - venta
            double inventarioFinal = inventarioTotal - venta; // Calcula inventario restante después de las ventas del día

            // Ventas perdidas usando tu fórmula de Excel: =MAX(0;F7-D7) - demanda menos inventario total
            double ventasPerdidas = Math.max(0, demanda - inventarioTotal); // Calcula ventas no realizadas por falta de inventario (0 si hay suficiente)

            // Costo de ordenar usando tu fórmula de Excel: =SI(C7>0;1000;0) - si hay entrega del proveedor
            double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0; // Aplica costo de ordenar solo si hubo entrega del proveedor
            double costoFalt = ventasPerdidas * costoFaltante; // Calcula costo total por ventas perdidas multiplicando cantidad por costo unitario
            double costoMant = inventarioFinal * costoMantenimiento; // Calcula costo de mantener inventario multiplicando cantidad por costo unitario
            double costoTotal = costoOrden + costoFalt + costoMant; // Suma todos los costos del día para obtener costo total

            model.addRow(new Object[]{ // Agrega nueva fila a la tabla con todos los valores calculados del día
                    dia, // Número del día actual de la simulación
                    String.format("%.0f", inventarioInicial), // Inventario inicial formateado como entero
                    String.format("%.0f", entregaProveedor), // Entrega del proveedor formateada como entero
                    String.format("%.0f", inventarioTotal), // Inventario total formateado como entero
                    String.format("%.4f", rnDemanda), // Número aleatorio formateado con 4 decimales
                    String.format("%.0f", demanda), // Demanda calculada formateada como entero
                    String.format("%.0f", venta), // Venta realizada formateada como entero
                    String.format("%.0f", inventarioFinal), // Inventario final formateado como entero
                    String.format("%.0f", ventasPerdidas), // Ventas perdidas formateadas como entero
                    String.format("$%.0f", costoOrden), // Costo de ordenar formateado como moneda entera
                    String.format("$%.0f", costoFalt), // Costo de faltante formateado como moneda entera
                    String.format("$%.0f", costoMant), // Costo de mantenimiento formateado como moneda entera
                    String.format("$%.0f", costoTotal) // Costo total formateado como moneda entera
            });

            // Actualizar valores para la siguiente iteración
            inventarioInicial = inventarioFinal; // El inventario final de hoy se convierte en inventario inicial de mañana
            inventarioFinalAnterior = inventarioFinal; // Guarda el inventario final para cálculos de entrega del próximo ciclo semanal
        }
    }

    public static void main(String[] args) { // Método principal que inicia la aplicación
        SwingUtilities.invokeLater(() -> new ejercicio4().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing y hace visible la ventana
    }
}
