package actividad_6.ejercicio4; // Declaración del paquete donde se encuentra la clase de versión aleatoria

import javax.swing.*; // Importa clases para crear interfaces gráficas con componentes Swing
import javax.swing.table.DefaultTableModel; // Importa el modelo de tabla por defecto para manejo dinámico de datos tabulares
import java.awt.*; // Importa clases para manejo de layouts, colores y componentes gráficos de AWT
import java.awt.event.ActionEvent; // Importa la clase para manejar eventos de acciones como clicks de botones
import java.util.Random; // Importa la clase Random para generar números aleatorios

public class ejercicio4_aleatorio extends JFrame { // Declaración de la clase que extiende JFrame para crear ventana con números aleatorios

    private final JTextField txtDias; // Campo de texto editable para ingresar el número de días a simular (variable en esta versión)
    private final JTextField txtCapacidadBodega; // Campo de texto para ingresar la capacidad máxima de almacenamiento de la bodega en Kg
    private final JTextField txtCostoOrdenar; // Campo de texto para ingresar el costo fijo por realizar un pedido de reabastecimiento
    private final JTextField txtCostoFaltante; // Campo de texto para ingresar el costo unitario por Kg cuando hay escasez de inventario
    private final JTextField txtCostoMantenimiento; // Campo de texto para ingresar el costo unitario por Kg de mantener inventario almacenado
    private final JTextField txtMediaDemanda; // Campo de texto para ingresar la media de la distribución exponencial que modela la demanda
    private final DefaultTableModel model; // Modelo de datos dinámico para la tabla que almacena y muestra los resultados de simulación

    public ejercicio4_aleatorio() { // Constructor de la clase que inicializa todos los componentes de la interfaz gráfica
        setTitle("Simulacion Inventario Azucar - Version Aleatoria"); // Establece el título específico para la versión con números aleatorios
        setSize(1200, 500); // Define las dimensiones de la ventana (1200 píxeles de ancho por 500 píxeles de alto)
        setDefaultCloseOperation(EXIT_ON_CLOSE); // Configura que la aplicación termine completamente al cerrar esta ventana
        setLocationRelativeTo(null); // Centra automáticamente la ventana en el centro de la pantalla del usuario

        JPanel panelInput = new JPanel(new FlowLayout()); // Crea un panel contenedor con disposición de flujo horizontal para los controles

        panelInput.add(new JLabel("Número de Días:")); // Agrega etiqueta descriptiva para identificar el campo de entrada de días
        txtDias = new JTextField("14", 5); // Crea campo de texto con valor inicial "14" días y ancho visual de 5 caracteres
        // Ahora es editable para poder cambiar la cantidad de días
        panelInput.add(txtDias); // Agrega el campo de días editable al panel de controles

        panelInput.add(new JLabel("Capacidad Bodega (Kg):")); // Agrega etiqueta para identificar el campo de capacidad de almacenamiento
        txtCapacidadBodega = new JTextField("700", 5); // Crea campo con valor por defecto de 700 Kg y ancho de 5 caracteres
        panelInput.add(txtCapacidadBodega); // Agrega el campo de capacidad al panel de controles

        panelInput.add(new JLabel("Costo de ordenar ($):")); // Agrega etiqueta para identificar el costo fijo de realizar pedidos
        txtCostoOrdenar = new JTextField("1000", 5); // Crea campo con valor por defecto de $1000 pesos y ancho de 5 caracteres
        panelInput.add(txtCostoOrdenar); // Agrega el campo de costo de pedidos al panel

        panelInput.add(new JLabel("Costo de faltante ($ por Kg):")); // Agrega etiqueta para el costo unitario por faltante de inventario
        txtCostoFaltante = new JTextField("6", 5); // Crea campo con valor por defecto de $6 pesos por Kg faltante y ancho de 5 caracteres
        panelInput.add(txtCostoFaltante); // Agrega el campo de costo de faltante al panel

        panelInput.add(new JLabel("Costo de mantenimiento ($ por Kg):")); // Agrega etiqueta para el costo unitario de almacenamiento
        txtCostoMantenimiento = new JTextField("1", 5); // Crea campo con valor por defecto de $1 peso por Kg almacenado y ancho de 5 caracteres
        panelInput.add(txtCostoMantenimiento); // Agrega el campo de costo de almacenamiento al panel

        panelInput.add(new JLabel("Media Demanda (Kg/dia):")); // Agrega etiqueta para la media de la distribución exponencial de demanda diaria
        txtMediaDemanda = new JTextField("100", 5); // Crea campo con valor por defecto de 100 Kg por día y ancho de 5 caracteres
        panelInput.add(txtMediaDemanda); // Agrega el campo de media de demanda al panel

        JButton btnSimular = new JButton("Simular"); // Crea botón de acción para ejecutar la simulación con números aleatorios
        panelInput.add(btnSimular); // Agrega el botón de simulación al panel de controles

        String[] columnas = { // Define el array de strings con los nombres de las columnas para la tabla de resultados
                "Día", "Inventario Inicial (Kg)", "Entrega del Proveedor (Kg)", "Inventario Total (Kg)", // Primeras 4 columnas: identificador de día, inventario al inicio, cantidad recibida e inventario disponible
                "Rn Demanda", "Demanda (Kg)", "Venta (Kg)", "Inventario Final (Kg)", // Siguientes 4 columnas: número aleatorio generado, demanda calculada, venta efectiva e inventario restante
                "Ventas Perdidas (Kg)", "Costo de ordenar ($)", "Costo de faltante ($)", // Siguientes 3 columnas: ventas no realizadas por falta de stock, costo de pedidos y costo por faltantes
                "Costo de mantenimiento ($)", "Costo total ($)" // Últimas 2 columnas: costo de almacenar inventario y suma total de todos los costos del día
        };
        model = new DefaultTableModel(columnas, 0); // Crea el modelo de tabla usando las columnas definidas y comenzando con 0 filas de datos
        JTable tabla = new JTable(model); // Crea el componente visual de tabla utilizando el modelo de datos creado
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con barras de desplazamiento automáticas

        add(panelInput, BorderLayout.NORTH); // Posiciona el panel de controles en la región norte (superior) de la ventana
        add(scrollPane, BorderLayout.CENTER); // Posiciona la tabla con scroll en la región central de la ventana

        btnSimular.addActionListener(this::simular); // Asocia el método simular() al evento de click del botón usando referencia de método
    }

    private void simular(ActionEvent e) { // Método privado que ejecuta la lógica completa de simulación cuando se activa el evento
        model.setRowCount(0); // Elimina todas las filas existentes en la tabla para comenzar una nueva simulación limpia

        int dias = Integer.parseInt(txtDias.getText()); // Convierte el texto del campo días a número entero para el bucle de simulación
        double capacidadBodega = Double.parseDouble(txtCapacidadBodega.getText()); // Convierte el texto de capacidad a número decimal para cálculos
        double costoOrdenar = Double.parseDouble(txtCostoOrdenar.getText()); // Convierte el texto de costo de pedidos a número decimal
        double costoFaltante = Double.parseDouble(txtCostoFaltante.getText()); // Convierte el texto de costo de faltante a número decimal
        double costoMantenimiento = Double.parseDouble(txtCostoMantenimiento.getText()); // Convierte el texto de costo de almacenamiento a número decimal
        double mediaDemanda = Double.parseDouble(txtMediaDemanda.getText()); // Convierte el texto de media de demanda a número decimal

        // Generador de números aleatorios para Rn
        Random random = new Random(); // Crea una instancia del generador de números pseudoaleatorios para valores Rn variables

        double inventarioInicial = 0; // Variable para rastrear el inventario disponible al comenzar cada día (inicia en cero)
        double inventarioFinalAnterior = 0; // Para el cálculo de entregas del proveedor

        for (int dia = 1; dia <= dias; dia++) { // Bucle principal que itera a través de cada día desde 1 hasta el número especificado
            // Fórmula de entregas del proveedor según las especificaciones de Excel
            double entregaProveedor; // Variable local para almacenar la cantidad que entrega el proveedor en el día actual
            if (dia == 1) { // Condición especial para manejar la lógica del primer día de la simulación
                // Primer día: =SI(RESIDUO(A7;7)=1;700;0)
                entregaProveedor = (dia % 7 == 1) ? capacidadBodega : 0; // Si el resto de dividir día entre 7 es 1, entrega capacidad completa, sino cero
            } else { // Condición para todos los días posteriores al primero en la simulación
                // Días siguientes: =SI(RESIDUO(A8;7)=0;700-H7;0) donde H7 es inventario final anterior
                entregaProveedor = (dia % 7 == 0) ? Math.max(0, capacidadBodega - inventarioFinalAnterior) : 0; // Si día módulo 7 es cero, entrega hasta completar capacidad máxima
            }

            double inventarioTotal = inventarioInicial + entregaProveedor; // Calcula el inventario total disponible sumando inicial más entrega del proveedor

            // Generar valor aleatorio de Rn entre 0 y 1
            double rnDemanda = random.nextDouble(); // Genera número aleatorio uniforme entre 0.0 y 1.0 para calcular demanda estocástica

            // Demanda usando la fórmula de Excel: =-100*LN(1-E7)
            double demanda = -mediaDemanda * Math.log(1 - rnDemanda); // Aplica transformación logarítmica inversa para generar demanda con distribución exponencial

            // Venta usando la fórmula de Excel: =MIN(F7;D7) - mínimo entre demanda e inventario total
            double venta = Math.min(demanda, inventarioTotal); // La venta real es el menor valor entre la demanda del cliente y el inventario disponible

            // Inventario final: inventario total - venta
            double inventarioFinal = inventarioTotal - venta; // Calcula el inventario que permanece al final del día después de satisfacer ventas

            // Ventas perdidas usando la fórmula de Excel: =MAX(0;F7-D7) - demanda menos inventario total
            double ventasPerdidas = Math.max(0, demanda - inventarioTotal); // Calcula las ventas no realizadas cuando la demanda excede el inventario (cero si hay suficiente stock)

            // Costo de ordenar usando la fórmula de Excel: =SI(C7>0;1000;0) - si hay entrega del proveedor
            double costoOrden = (entregaProveedor > 0) ? costoOrdenar : 0; // Aplica el costo fijo de pedido solo cuando efectivamente hubo entrega del proveedor
            double costoFalt = ventasPerdidas * costoFaltante; // Calcula el costo total por ventas perdidas multiplicando cantidad por costo unitario de faltante
            double costoMant = inventarioFinal * costoMantenimiento; // Calcula el costo total de almacenamiento multiplicando inventario final por costo unitario de mantenimiento
            double costoTotal = costoOrden + costoFalt + costoMant; // Suma todos los componentes de costo para obtener el costo total del día

            model.addRow(new Object[]{ // Agrega una nueva fila a la tabla con todos los valores calculados y formateados para el día actual
                    dia, // Número del día actual en la secuencia de simulación
                    String.format("%.0f", inventarioInicial), // Inventario inicial del día formateado como número entero sin decimales
                    String.format("%.0f", entregaProveedor), // Cantidad entregada por el proveedor formateada como entero
                    String.format("%.0f", inventarioTotal), // Inventario total disponible formateado como entero
                    String.format("%.4f", rnDemanda), // Número aleatorio generado formateado con exactamente 4 decimales para precisión
                    String.format("%.0f", demanda), // Demanda calculada del día formateada como entero
                    String.format("%.0f", venta), // Venta realizada formateada como entero
                    String.format("%.0f", inventarioFinal), // Inventario restante al final formateado como entero
                    String.format("%.0f", ventasPerdidas), // Ventas no realizadas formateadas como entero
                    String.format("$%.0f", costoOrden), // Costo de pedidos formateado como moneda entera con símbolo de pesos
                    String.format("$%.0f", costoFalt), // Costo por faltantes formateado como moneda entera
                    String.format("$%.0f", costoMant), // Costo de mantenimiento formateado como moneda entera
                    String.format("$%.0f", costoTotal) // Costo total del día formateado como moneda entera
            });

            // Actualizar valores para la siguiente iteración
            inventarioInicial = inventarioFinal; // El inventario final del día actual se convierte en el inventario inicial del día siguiente
            inventarioFinalAnterior = inventarioFinal; // Guarda el inventario final actual para usarlo en cálculos de entrega del proveedor en futuros ciclos semanales
        }
    }

    public static void main(String[] args) { // Método principal estático que sirve como punto de entrada para ejecutar la aplicación independientemente
        SwingUtilities.invokeLater(() -> new ejercicio4_aleatorio().setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing para thread safety y hace visible la ventana
    }
}
