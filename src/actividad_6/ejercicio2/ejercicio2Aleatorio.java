package actividad_6.ejercicio2; // Declaración del paquete donde se encuentra la clase

// Importación de clases necesarias para la interfaz gráfica
import java.awt.Component; // Para el manejo de componentes GUI
import java.awt.FlowLayout; // Para el layout de distribución de componentes
import java.awt.event.ActionEvent; // Para el manejo de eventos de botones
import java.util.Random; // Para la generación de números aleatorios
import javax.swing.JButton; // Para crear botones
import javax.swing.JFrame; // Para crear la ventana principal
import javax.swing.JLabel; // Para crear etiquetas de texto
import javax.swing.JPanel; // Para crear paneles contenedores
import javax.swing.JScrollPane; // Para crear paneles con scroll
import javax.swing.JTable; // Para crear tablas
import javax.swing.JTextField; // Para crear campos de texto
import javax.swing.SwingUtilities; // Para utilities de Swing
import javax.swing.table.DefaultTableModel; // Para el modelo de datos de la tabla

import org.apache.commons.math3.distribution.NormalDistribution; // Para cálculos de distribución normal

public class ejercicio2Aleatorio extends JFrame { // Declaración de la clase que extiende JFrame
    private final JTextField txtNumPiezas; // Campo de texto para el número de piezas (editable)
    private final JTextField txtMediaExponencial; // Campo de texto para la media exponencial (no editable)
    private final JTextField txtMediaNormal; // Campo de texto para la media normal (no editable)
    private final JTextField txtDesvNormal; // Campo de texto para la desviación normal (no editable)
    private final DefaultTableModel model; // Modelo de datos para la tabla

    public ejercicio2Aleatorio() { // Constructor de la clase
        this.setTitle("Simulación de Inspección - Números Aleatorios"); // Establece el título de la ventana
        this.setSize(1000, 450); // Define el tamaño de la ventana (ancho, alto)
        this.setDefaultCloseOperation(3); // Define la operación al cerrar la ventana (EXIT_ON_CLOSE)
        this.setLocationRelativeTo((Component)null); // Centra la ventana en la pantalla
        JPanel var1 = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo

        var1.add(new JLabel("Número de Piezas:")); // Agrega etiqueta para número de piezas
        this.txtNumPiezas = new JTextField("18", 5); // Crea campo de texto editable con valor inicial 18
        var1.add(this.txtNumPiezas); // Agrega el campo al panel

        var1.add(new JLabel("Media Exponencial (tiempo entre llegadas):")); // Agrega etiqueta para media exponencial
        this.txtMediaExponencial = new JTextField("5", 5); // Crea campo de texto con valor inicial 5
        this.txtMediaExponencial.setEnabled(false); // No editable - solo para visualizar - deshabilita la edición
        var1.add(this.txtMediaExponencial); // Agrega el campo al panel

        var1.add(new JLabel("Media Normal (tiempo inspección):")); // Agrega etiqueta para media normal
        this.txtMediaNormal = new JTextField("4", 5); // Crea campo de texto con valor inicial 4
        this.txtMediaNormal.setEnabled(false); // No editable - solo para visualizar - deshabilita la edición
        var1.add(this.txtMediaNormal); // Agrega el campo al panel

        var1.add(new JLabel("Desviación Normal:")); // Agrega etiqueta para desviación normal
        this.txtDesvNormal = new JTextField("0.5", 5); // Crea campo de texto con valor inicial 0.5
        this.txtDesvNormal.setEnabled(false); // No editable - solo para visualizar - deshabilita la edición
        var1.add(this.txtDesvNormal); // Agrega el campo al panel

        JButton var2 = new JButton("Simular"); // Crea el botón de simulación
        var1.add(var2); // Agrega el botón al panel

        String[] var3 = new String[]{"Pieza", "Rn Llegada", "Tiempo entre llegadas", "Minuto en que llega", "Minuto en que inicia inspección", "Rn Inspección", "Tiempo de inspección", "Minuto en que finaliza inspección", "Tiempo total inspección", "Tiempo en espera"}; // Define los nombres de las columnas de la tabla
        this.model = new DefaultTableModel(var3, 0); // Crea el modelo de la tabla con las columnas y 0 filas
        JTable var4 = new JTable(this.model); // Crea la tabla usando el modelo
        JScrollPane var5 = new JScrollPane(var4); // Crea un panel con scroll para la tabla
        this.add(var1, "North"); // Agrega el panel de controles en la parte superior
        this.add(var5, "Center"); // Agrega la tabla con scroll en el centro
        var2.addActionListener(this::simular); // Asocia el método simular al evento click del botón
    }

    // Función para calcular NORM.INV(p, mu, sigma) usando Apache Commons Math3
    private double normInv(double p, double mu, double sigma) { // Método para calcular la inversa de distribución normal
        // Usamos directamente la distribución normal con parámetros específicos
        NormalDistribution normal = new NormalDistribution(mu, sigma); // Crea una instancia de distribución normal con media y desviación
        return normal.inverseCumulativeProbability(p); // Retorna la probabilidad acumulativa inversa (valor Z)
    }

    private void simular(ActionEvent var1) { // Método que ejecuta la simulación al presionar el botón
        this.model.setRowCount(0); // Limpia todas las filas existentes en la tabla
        int numPiezas = Integer.parseInt(this.txtNumPiezas.getText()); // Obtiene el número de piezas del campo editable
        double mediaExponencial = Double.parseDouble(this.txtMediaExponencial.getText()); // Obtiene la media exponencial del campo no editable
        double mediaNormal = Double.parseDouble(this.txtMediaNormal.getText()); // Obtiene la media normal del campo no editable
        double desviacionNormal = Double.parseDouble(this.txtDesvNormal.getText()); // Obtiene la desviación normal del campo no editable

        // Arrays para almacenar los datos
        double[] rnLlegada = new double[numPiezas]; // Array para almacenar números aleatorios de llegada
        double[] tiempoEntreLlegadas = new double[numPiezas]; // Array para almacenar tiempo entre llegadas calculado
        double[] minutoLlegada = new double[numPiezas]; // Array para almacenar minuto de llegada acumulado
        double[] minutoInicioInspeccion = new double[numPiezas]; // Array para almacenar minuto de inicio de inspección
        double[] rnInspeccion = new double[numPiezas]; // Array para almacenar números aleatorios de inspección
        double[] tiempoInspeccion = new double[numPiezas]; // Array para almacenar tiempo de inspección calculado
        double[] minutoFinInspeccion = new double[numPiezas]; // Array para almacenar minuto de fin de inspección
        double[] tiempoTotalInspeccion = new double[numPiezas]; // Array para almacenar tiempo total de inspección
        double[] tiempoEspera = new double[numPiezas]; // Array para almacenar tiempo de espera

        Random random = new Random(); // Crea un generador de números aleatorios

        // Generar números aleatorios para ambas columnas
        for(int i = 0; i < numPiezas; i++) { // Ciclo para generar números aleatorios para cada pieza
            rnLlegada[i] = random.nextDouble(); // Genera número aleatorio entre 0 y 1 para llegada
            rnInspeccion[i] = random.nextDouble(); // Genera número aleatorio entre 0 y 1 para inspección
        }

        // Calcular tiempo entre llegadas usando la distribución exponencial
        for(int i = 0; i < numPiezas; i++) { // Ciclo para calcular tiempo entre llegadas
            tiempoEntreLlegadas[i] = -Math.log(1.0 - rnLlegada[i]) * mediaExponencial; // Aplica fórmula de distribución exponencial inversa
        }

        // Calcular tiempo de llegada acumulado
        minutoLlegada[0] = tiempoEntreLlegadas[0]; // La primera pieza llega en su tiempo entre llegadas
        for(int i = 1; i < numPiezas; i++) { // Ciclo para calcular llegadas acumuladas de las piezas restantes
            minutoLlegada[i] = minutoLlegada[i - 1] + tiempoEntreLlegadas[i]; // Suma el tiempo anterior más el tiempo entre llegadas actual
        }

        // Calcular tiempo de inspección usando distribución normal
        for(int i = 0; i < numPiezas; i++) { // Ciclo para calcular tiempo de inspección de cada pieza
            tiempoInspeccion[i] = normInv(rnInspeccion[i], mediaNormal, desviacionNormal); // Aplica distribución normal inversa con parámetros dados
            if (tiempoInspeccion[i] < 0.0) { // Si el tiempo calculado es negativo (puede ocurrir con distribución normal)
                tiempoInspeccion[i] = 0.0; // Lo establece en cero (no puede haber tiempo negativo)
            }
        }

        // Calcular tiempos de inicio, fin, duración total y espera
        minutoInicioInspeccion[0] = minutoLlegada[0]; // La primera pieza inicia inspección cuando llega
        minutoFinInspeccion[0] = minutoInicioInspeccion[0] + tiempoInspeccion[0]; // El fin de inspección es inicio más duración
        tiempoEspera[0] = 0.0; // La primera pieza no espera (llega y se inspecciona inmediatamente)
        tiempoTotalInspeccion[0] = tiempoInspeccion[0]; // El tiempo total es igual al tiempo de inspección para la primera pieza

        for(int i = 1; i < numPiezas; i++) { // Ciclo para calcular tiempos de las piezas restantes
            minutoInicioInspeccion[i] = Math.max(minutoLlegada[i], minutoFinInspeccion[i - 1]); // Inicio es el máximo entre su llegada y fin de inspección anterior
            minutoFinInspeccion[i] = minutoInicioInspeccion[i] + tiempoInspeccion[i]; // Fin es inicio más duración de inspección
            tiempoEspera[i] = Math.max(0.0, minutoInicioInspeccion[i] - minutoLlegada[i]); // Espera es la diferencia entre inicio y llegada (si es positiva)
            tiempoTotalInspeccion[i] = minutoFinInspeccion[i] - minutoLlegada[i]; // Tiempo total es desde llegada hasta fin de inspección
        }

        // Agregar filas a la tabla
        for(int i = 0; i < numPiezas; i++) { // Ciclo para agregar cada fila a la tabla con los resultados
            this.model.addRow(new Object[]{ // Agrega una nueva fila con todos los valores calculados
                i + 1, // Número de pieza (índice + 1)
                String.format("%.4f", rnLlegada[i]), // Número aleatorio de llegada formateado a 4 decimales
                String.format("%.4f", tiempoEntreLlegadas[i]), // Tiempo entre llegadas formateado a 4 decimales
                String.format("%.4f", minutoLlegada[i]), // Minuto de llegada formateado a 4 decimales
                String.format("%.4f", minutoInicioInspeccion[i]), // Minuto de inicio de inspección formateado a 4 decimales
                String.format("%.4f", rnInspeccion[i]), // Número aleatorio de inspección formateado a 4 decimales
                String.format("%.4f", tiempoInspeccion[i]), // Tiempo de inspección formateado a 4 decimales
                String.format("%.4f", minutoFinInspeccion[i]), // Minuto de fin de inspección formateado a 4 decimales
                String.format("%.4f", tiempoTotalInspeccion[i]), // Tiempo total de inspección formateado a 4 decimales
                String.format("%.4f", tiempoEspera[i]) // Tiempo de espera formateado a 4 decimales
            });
        }

        // Calcular y mostrar el promedio de tiempo total de inspección
        double sumaTiempoTotal = 0.0; // Inicializa la suma de todos los tiempos totales de inspección
        for (int i = 0; i < numPiezas; i++) { // Itera a través de todas las piezas
            sumaTiempoTotal += tiempoTotalInspeccion[i]; // Suma el tiempo total de inspección de cada pieza
        }
        double promedioTiempoTotal = sumaTiempoTotal / numPiezas; // Calcula el promedio dividiendo la suma entre el número de piezas

        // Mostrar el resultado en un cuadro de diálogo
        javax.swing.JOptionPane.showMessageDialog(this, // Muestra un cuadro de diálogo con el resultado
            String.format("Promedio de Tiempo Total de Inspección: %.4f minutos por pieza\n" +
                         "Basado en %d piezas simuladas",
                         promedioTiempoTotal, numPiezas), // Formatea el mensaje con el promedio y número de piezas
            "Estadísticas de la Simulación", // Título del cuadro de diálogo
            javax.swing.JOptionPane.INFORMATION_MESSAGE); // Tipo de mensaje informativo
    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación
        SwingUtilities.invokeLater(() -> (new ejercicio2Aleatorio()).setVisible(true)); // Ejecuta la interfaz en el hilo de eventos de Swing
    }
}
