package actividad_6.ejercicio2; // Declaración del paquete donde se encuentra la clase

// Importación de clases necesarias para la interfaz gráfica y funcionalidad
import java.awt.BorderLayout; // Para el layout de distribución en bordes (North, South, East, West, Center)
import java.awt.Component; // Para el manejo de componentes GUI
import java.awt.FlowLayout; // Para el layout de distribución en flujo secuencial
import java.awt.event.ActionEvent; // Para el manejo de eventos de acciones (clicks de botones)
import javax.swing.JButton; // Para crear botones clickeables
import javax.swing.JFrame; // Para crear la ventana principal de la aplicación
import javax.swing.JLabel; // Para crear etiquetas de texto estáticas
import javax.swing.JPanel; // Para crear paneles contenedores de componentes
import javax.swing.JScrollPane; // Para crear paneles con capacidad de scroll
import javax.swing.JTable; // Para crear tablas de datos
import javax.swing.JTextField; // Para crear campos de texto editables
import javax.swing.SwingUtilities; // Para utilities de threading de Swing
import javax.swing.table.DefaultTableModel; // Para el modelo de datos de la tabla

import org.apache.commons.math3.distribution.NormalDistribution; // Para cálculos de distribución normal estadística

public class ejercicio2Manual extends JFrame { // Declaración de la clase que extiende JFrame (ventana principal)
    private final JTextField txtNumPiezas; // Campo de texto para el número de piezas (editable por el usuario)
    private final JTextField txtMediaExponencial; // Campo de texto para la media exponencial (solo lectura)
    private final JTextField txtMediaNormal; // Campo de texto para la media normal (solo lectura)
    private final JTextField txtDesvNormal; // Campo de texto para la desviación estándar normal (solo lectura)
    private final DefaultTableModel model; // Modelo de datos que maneja el contenido de la tabla
    private final JTable tabla; // Tabla que muestra los datos de la simulación
    private final JButton btnCrearFilas; // Botón para crear las filas de la tabla según el número de piezas
    private final JButton btnCalcular; // Botón para ejecutar los cálculos de simulación

    public ejercicio2Manual() { // Constructor de la clase que inicializa todos los componentes
        this.setTitle("Simulación de Inspección - Entrada Manual"); // Establece el título de la ventana
        this.setSize(1200, 500); // Define las dimensiones de la ventana (ancho 1200px, alto 500px)
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Define que la aplicación se cierre al cerrar la ventana
        this.setLocationRelativeTo(null); // Centra la ventana en la pantalla

        // Panel superior con parámetros y botones
        JPanel panelSuperior = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo para organizar componentes horizontalmente

        panelSuperior.add(new JLabel("Número de Piezas:")); // Agrega etiqueta descriptiva para el campo de número de piezas
        this.txtNumPiezas = new JTextField("18", 5); // Crea campo de texto editable con valor inicial 18 y ancho de 5 caracteres
        panelSuperior.add(this.txtNumPiezas); // Agrega el campo de texto al panel superior

        panelSuperior.add(new JLabel("Media Exponencial:")); // Agrega etiqueta para el parámetro de media exponencial
        this.txtMediaExponencial = new JTextField("5", 5); // Crea campo con valor fijo 5 y ancho de 5 caracteres
        this.txtMediaExponencial.setEnabled(false); // No editable - solo para visualizar - deshabilita la edición del usuario
        panelSuperior.add(this.txtMediaExponencial); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Media Normal:")); // Agrega etiqueta para el parámetro de media de distribución normal
        this.txtMediaNormal = new JTextField("4", 5); // Crea campo con valor fijo 4 y ancho de 5 caracteres
        this.txtMediaNormal.setEnabled(false); // No editable - solo para visualizar - campo de solo lectura
        panelSuperior.add(this.txtMediaNormal); // Agrega el campo al panel superior

        panelSuperior.add(new JLabel("Desviación Normal:")); // Agrega etiqueta para el parámetro de desviación estándar
        this.txtDesvNormal = new JTextField("0.5", 5); // Crea campo con valor fijo 0.5 y ancho de 5 caracteres
        this.txtDesvNormal.setEnabled(false); // No editable - solo para visualizar - previene modificación accidental
        panelSuperior.add(this.txtDesvNormal); // Agrega el campo al panel superior

        // Botones
        this.btnCrearFilas = new JButton("Crear Filas"); // Crea botón para generar filas en la tabla
        this.btnCalcular = new JButton("Calcular"); // Crea botón para ejecutar los cálculos de simulación
        this.btnCalcular.setEnabled(false); // Deshabilitado hasta crear filas - inicialmente no se puede usar

        panelSuperior.add(btnCrearFilas); // Agrega el botón de crear filas al panel superior
        panelSuperior.add(btnCalcular); // Agrega el botón de calcular al panel superior

        // Configurar tabla
        String[] columnas = new String[]{ // Define los nombres de las columnas de la tabla de simulación
            "Pieza", "Rn Llegada", "Tiempo entre llegadas", "Minuto en que llega",
            "Minuto en que inicia inspección", "Rn Inspección", "Tiempo de inspección",
            "Minuto en que finaliza inspección", "Tiempo total inspección", "Tiempo en espera"
        };

        this.model = new DefaultTableModel(columnas, 0) { // Crea el modelo de tabla con las columnas definidas y 0 filas iniciales
            @Override
            public boolean isCellEditable(int row, int column) { // Método que determina qué celdas son editables
                // Solo permitir editar las columnas "Rn Llegada" (1) y "Rn Inspección" (5)
                return column == 1 || column == 5; // Retorna true solo para las columnas de números aleatorios
            }
        };

        this.tabla = new JTable(this.model); // Crea la tabla usando el modelo de datos configurado
        JScrollPane scrollPane = new JScrollPane(tabla); // Envuelve la tabla en un panel con scroll para manejar muchas filas

        // Layout
        this.add(panelSuperior, BorderLayout.NORTH); // Agrega el panel de controles en la parte superior de la ventana
        this.add(scrollPane, BorderLayout.CENTER); // Agrega la tabla con scroll en el centro de la ventana

        // Event listeners
        btnCrearFilas.addActionListener(this::crearFilas); // Asocia el método crearFilas al evento click del botón
        btnCalcular.addActionListener(this::calcular); // Asocia el método calcular al evento click del botón

        // Listener para el campo de número de piezas para permitir recrear filas
        txtNumPiezas.addActionListener(e -> habilitarCrearFilas()); // Detecta cuando se presiona Enter en el campo
        txtNumPiezas.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() { // Detecta cambios en tiempo real
            public void insertUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al insertar texto
            public void removeUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al eliminar texto
            public void changedUpdate(javax.swing.event.DocumentEvent e) { habilitarCrearFilas(); } // Se ejecuta al cambiar atributos
        });
    }

    private void habilitarCrearFilas() { // Método que se ejecuta cuando el usuario modifica el número de piezas
        // Habilitar solo el botón crear filas cuando se modifica el número de piezas
        btnCrearFilas.setEnabled(true); // Permite al usuario crear nuevas filas
        btnCalcular.setEnabled(false); // Deshabilita el cálculo hasta que se creen nuevas filas
    }

    private void crearFilas(ActionEvent e) { // Método que genera las filas de la tabla según el número de piezas especificado
        try { // Manejo de errores por entrada inválida
            int numPiezas = Integer.parseInt(this.txtNumPiezas.getText()); // Convierte el texto a número entero

            // Limpiar tabla
            this.model.setRowCount(0); // Elimina todas las filas existentes de la tabla

            // Crear filas con valores por defecto
            for(int i = 0; i < numPiezas; i++) { // Ciclo para crear una fila por cada pieza
                Object[] fila = new Object[10]; // Array que representa una fila con 10 columnas
                fila[0] = i + 1; // Número de pieza (índice + 1 para empezar en 1)
                fila[1] = ""; // Rn Llegada - editable, inicialmente vacío para entrada manual
                fila[2] = ""; // Tiempo entre llegadas - calculado automáticamente
                fila[3] = ""; // Minuto en que llega - calculado automáticamente
                fila[4] = ""; // Minuto en que inicia inspección - calculado automáticamente
                fila[5] = ""; // Rn Inspección - editable, inicialmente vacío para entrada manual
                fila[6] = ""; // Tiempo de inspección - calculado automáticamente
                fila[7] = ""; // Minuto en que finaliza inspección - calculado automáticamente
                fila[8] = ""; // Tiempo total inspección - calculado automáticamente
                fila[9] = ""; // Tiempo en espera - calculado automáticamente

                this.model.addRow(fila); // Agrega la fila completa al modelo de la tabla
            }

            // Habilitar el botón calcular y deshabilitar crear filas
            this.btnCalcular.setEnabled(true); // Permite ejecutar cálculos una vez que las filas están creadas
            this.btnCrearFilas.setEnabled(false); // Deshabilita la creación de filas hasta el próximo cambio

        } catch (NumberFormatException ex) { // Captura errores si el texto no es un número válido
            javax.swing.JOptionPane.showMessageDialog(this, // Muestra diálogo de error al usuario
                "Por favor ingrese un número válido de piezas", // Mensaje explicativo del error
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE); // Tipo de mensaje de error
        }
    }

    // Función para calcular NORM.INV(p, mu, sigma) usando Apache Commons Math3
    private double normInv(double p, double mu, double sigma) { // Método para calcular la función inversa de distribución normal
        NormalDistribution normal = new NormalDistribution(mu, sigma); // Crea instancia de distribución normal con media y desviación específicas
        return normal.inverseCumulativeProbability(p); // Retorna el valor correspondiente a la probabilidad acumulativa inversa
    }

    private void calcular(ActionEvent e) { // Método principal que ejecuta todos los cálculos de la simulación
        try { // Manejo de errores durante los cálculos
            double mediaExponencial = Double.parseDouble(this.txtMediaExponencial.getText()); // Obtiene la media para distribución exponencial
            double mediaNormal = Double.parseDouble(this.txtMediaNormal.getText()); // Obtiene la media para distribución normal
            double desviacionNormal = Double.parseDouble(this.txtDesvNormal.getText()); // Obtiene la desviación estándar para distribución normal

            int numFilas = this.model.getRowCount(); // Obtiene el número actual de filas en la tabla

            // Arrays para los cálculos
            double[] rnLlegada = new double[numFilas]; // Array para almacenar números aleatorios de llegada ingresados manualmente
            double[] rnInspeccion = new double[numFilas]; // Array para almacenar números aleatorios de inspección ingresados manualmente
            double[] tiempoEntreLlegadas = new double[numFilas]; // Array para almacenar tiempos calculados entre llegadas
            double[] minutoLlegada = new double[numFilas]; // Array para almacenar minutos acumulados de llegada
            double[] minutoInicioInspeccion = new double[numFilas]; // Array para almacenar minutos de inicio de inspección
            double[] tiempoInspeccion = new double[numFilas]; // Array para almacenar tiempos calculados de inspección
            double[] minutoFinInspeccion = new double[numFilas]; // Array para almacenar minutos de finalización de inspección
            double[] tiempoTotalInspeccion = new double[numFilas]; // Array para almacenar tiempo total desde llegada hasta fin
            double[] tiempoEspera = new double[numFilas]; // Array para almacenar tiempo de espera antes de iniciar inspección

            // Leer valores de Rn desde la tabla
            for(int i = 0; i < numFilas; i++) { // Ciclo para leer los valores ingresados manualmente por el usuario
                try { // Manejo de errores por celda individual
                    String rnLlegadaStr = this.model.getValueAt(i, 1).toString(); // Obtiene el valor de Rn Llegada como string
                    String rnInspeccionStr = this.model.getValueAt(i, 5).toString(); // Obtiene el valor de Rn Inspección como string

                    rnLlegada[i] = Double.parseDouble(rnLlegadaStr); // Convierte string a número decimal
                    rnInspeccion[i] = Double.parseDouble(rnInspeccionStr); // Convierte string a número decimal

                    // Validar que estén entre 0 y 1
                    if(rnLlegada[i] < 0 || rnLlegada[i] > 1 || rnInspeccion[i] < 0 || rnInspeccion[i] > 1) { // Verifica que los números aleatorios estén en el rango válido
                        throw new IllegalArgumentException("Los valores Rn deben estar entre 0 y 1"); // Lanza excepción si están fuera del rango
                    }
                } catch (NumberFormatException ex) { // Captura errores de conversión de texto a número
                    javax.swing.JOptionPane.showMessageDialog(this, // Muestra mensaje de error específico por fila
                        "Error en fila " + (i+1) + ": Por favor ingrese valores numéricos válidos para Rn", // Mensaje con número de fila específico
                        "Error", javax.swing.JOptionPane.ERROR_MESSAGE); // Tipo de diálogo de error
                    return; // Termina la ejecución del método si hay error
                }
            }

            // Calcular tiempo entre llegadas
            for(int i = 0; i < numFilas; i++) { // Ciclo para calcular tiempo entre llegadas usando distribución exponencial
                if(rnLlegada[i] == 1.0) rnLlegada[i] = 0.9999; // Evitar log(0) - previene división por cero en logaritmo
                tiempoEntreLlegadas[i] = -Math.log(1.0 - rnLlegada[i]) * mediaExponencial; // Aplica fórmula de distribución exponencial inversa
            }

            // Calcular tiempo de llegada acumulado
            minutoLlegada[0] = tiempoEntreLlegadas[0]; // La primera pieza llega exactamente en su tiempo entre llegadas
            for(int i = 1; i < numFilas; i++) { // Ciclo para calcular llegadas acumulativas
                minutoLlegada[i] = minutoLlegada[i - 1] + tiempoEntreLlegadas[i]; // Suma tiempo anterior más el intervalo actual
            }

            // Calcular tiempo de inspección
            for(int i = 0; i < numFilas; i++) { // Ciclo para calcular tiempo de inspección usando distribución normal
                tiempoInspeccion[i] = normInv(rnInspeccion[i], mediaNormal, desviacionNormal); // Aplica función inversa de distribución normal
                if(tiempoInspeccion[i] < 0.0) { // Verifica si el tiempo calculado es negativo (posible con distribución normal)
                    tiempoInspeccion[i] = 0.0; // Establece tiempo mínimo en cero (no puede haber tiempo negativo)
                }
            }

            // Calcular tiempos de inicio, fin, duración total y espera
            minutoInicioInspeccion[0] = minutoLlegada[0]; // La primera pieza inicia inspección inmediatamente al llegar
            minutoFinInspeccion[0] = minutoInicioInspeccion[0] + tiempoInspeccion[0]; // Fin = inicio + duración de inspección
            tiempoEspera[0] = 0.0; // La primera pieza no tiene espera
            tiempoTotalInspeccion[0] = tiempoInspeccion[0]; // Tiempo total igual a tiempo de inspección para la primera

            for(int i = 1; i < numFilas; i++) { // Ciclo para calcular tiempos de las piezas restantes
                minutoInicioInspeccion[i] = Math.max(minutoLlegada[i], minutoFinInspeccion[i - 1]); // Inicio es el mayor entre su llegada y fin de inspección anterior
                minutoFinInspeccion[i] = minutoInicioInspeccion[i] + tiempoInspeccion[i]; // Fin = inicio + duración
                tiempoEspera[i] = Math.max(0.0, minutoInicioInspeccion[i] - minutoLlegada[i]); // Espera = diferencia entre inicio y llegada (si es positiva)
                tiempoTotalInspeccion[i] = minutoFinInspeccion[i] - minutoLlegada[i]; // Tiempo total = desde llegada hasta finalización
            }

            // Actualizar la tabla con los resultados
            for(int i = 0; i < numFilas; i++) { // Ciclo para actualizar cada fila de la tabla con los resultados calculados
                this.model.setValueAt(String.format("%.4f", tiempoEntreLlegadas[i]), i, 2); // Actualiza columna de tiempo entre llegadas con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", minutoLlegada[i]), i, 3); // Actualiza columna de minuto de llegada con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", minutoInicioInspeccion[i]), i, 4); // Actualiza columna de inicio de inspección con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", tiempoInspeccion[i]), i, 6); // Actualiza columna de tiempo de inspección con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", minutoFinInspeccion[i]), i, 7); // Actualiza columna de fin de inspección con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", tiempoTotalInspeccion[i]), i, 8); // Actualiza columna de tiempo total con formato 4 decimales
                this.model.setValueAt(String.format("%.4f", tiempoEspera[i]), i, 9); // Actualiza columna de tiempo de espera con formato 4 decimales
            }

        } catch (NumberFormatException ex) { // Captura errores de conversión de parámetros
            javax.swing.JOptionPane.showMessageDialog(this, // Muestra diálogo de error para parámetros inválidos
                "Por favor ingrese valores numéricos válidos en los parámetros", // Mensaje de error genérico
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE); // Tipo de diálogo de error
        } catch (Exception ex) { // Captura cualquier otro error inesperado durante los cálculos
            javax.swing.JOptionPane.showMessageDialog(this, // Muestra diálogo de error genérico
                "Error en el cálculo: " + ex.getMessage(), // Mensaje con detalles del error específico
                "Error", javax.swing.JOptionPane.ERROR_MESSAGE); // Tipo de diálogo de error
        }
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación de forma independiente
        SwingUtilities.invokeLater(() -> (new ejercicio2Manual()).setVisible(true)); // Ejecuta la creación de la interfaz en el hilo de eventos de Swing
    }
}
