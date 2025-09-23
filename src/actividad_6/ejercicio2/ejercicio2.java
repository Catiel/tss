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

public class ejercicio2 extends JFrame { // Declaración de la clase que extiende JFrame
    private final JTextField txtNumPiezas; // Campo de texto para el número de piezas (oculto)
    private final JTextField txtMediaExponencial; // Campo de texto para la media exponencial
    private final JTextField txtMediaNormal; // Campo de texto para la media normal
    private final JTextField txtDesvNormal; // Campo de texto para la desviación normal
    private final DefaultTableModel model; // Modelo de datos para la tabla

    public ejercicio2() { // Constructor de la clase
        this.setTitle("Simulación Completa de Inspección"); // Establece el título de la ventana
        this.setSize(1000, 450); // Define el tamaño de la ventana (ancho, alto)
        this.setDefaultCloseOperation(3); // Define la operación al cerrar la ventana (EXIT_ON_CLOSE)
        this.setLocationRelativeTo((Component)null); // Centra la ventana en la pantalla
        JPanel var1 = new JPanel(new FlowLayout()); // Crea un panel con layout de flujo

        // Campo oculto - no se muestra en la interfaz pero se usa internamente
        this.txtNumPiezas = new JTextField("18", 5); // Inicializa el campo de número de piezas con valor 18
        // No agregamos este campo al panel para que no sea visible

        var1.add(new JLabel("Media Exponencial (tiempo entre llegadas):")); // Agrega etiqueta para media exponencial
        this.txtMediaExponencial = new JTextField("5", 5); // Crea campo de texto con valor inicial 5
        this.txtMediaExponencial.setEditable(false); // Solo lectura - hace el campo no editable
        var1.add(this.txtMediaExponencial); // Agrega el campo al panel

        var1.add(new JLabel("Media Normal (tiempo inspección):")); // Agrega etiqueta para media normal
        this.txtMediaNormal = new JTextField("4", 5); // Crea campo de texto con valor inicial 4
        this.txtMediaNormal.setEditable(false); // Solo lectura - hace el campo no editable
        var1.add(this.txtMediaNormal); // Agrega el campo al panel

        var1.add(new JLabel("Desviación Normal:")); // Agrega etiqueta para desviación normal
        this.txtDesvNormal = new JTextField("0.5", 5); // Crea campo de texto con valor inicial 0.5
        this.txtDesvNormal.setEditable(false); // Solo lectura - hace el campo no editable
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
        NormalDistribution normal = new NormalDistribution(mu, sigma); // Crea una instancia de distribución normal
        return normal.inverseCumulativeProbability(p); // Retorna la probabilidad acumulativa inversa
    }

    private void simular(ActionEvent var1) { // Método que ejecuta la simulación al presionar el botón
        this.model.setRowCount(0); // Limpia todas las filas existentes en la tabla
        int var2 = Integer.parseInt(this.txtNumPiezas.getText()); // Obtiene el número de piezas del campo oculto
        double mediaExponencial = Double.parseDouble(this.txtMediaExponencial.getText()); // Obtiene la media exponencial
        double mediaNormal = Double.parseDouble(this.txtMediaNormal.getText()); // Obtiene la media normal
        double desviacionNormal = Double.parseDouble(this.txtDesvNormal.getText()); // Obtiene la desviación normal

        // Valores aleatorios predeterminados para Rn Llegada (columna 2)
        double[] valoresRnLlegada = { // Array con valores predefinidos para números aleatorios de llegada
            0.2962, 0.2883, 0.7287, 0.5568, 0.9641, 0.3651, 0.1524, 0.9198, 0.7633,
            0.3989, 0.2594, 0.4217, 0.9523, 0.7420, 0.4152, 0.8417, 0.6656, 0.1064
        };

        // Valores aleatorios predeterminados para Rn Inspección (columna 6)
        double[] valoresRnInspeccion = { // Array con valores predefinidos para números aleatorios de inspección
            0.7831, 0.6601, 0.5286, 0.7129, 0.0880, 0.8815, 0.0356, 0.4289, 0.7293,
            0.8502, 0.4793, 0.0455, 0.3672, 0.7548, 0.1636, 0.3114, 0.9976, 0.9619
        };

        double[] var9 = new double[var2]; // Array para almacenar números aleatorios de llegada
        double[] var10 = new double[var2]; // Array para almacenar tiempo entre llegadas
        double[] var11 = new double[var2]; // Array para almacenar minuto de llegada acumulado
        double[] var12 = new double[var2]; // Array para almacenar minuto de inicio de inspección
        double[] var13 = new double[var2]; // Array para almacenar números aleatorios de inspección
        double[] var14 = new double[var2]; // Array para almacenar tiempo de inspección
        double[] var15 = new double[var2]; // Array para almacenar minuto de fin de inspección
        double[] var16 = new double[var2]; // Array para almacenar tiempo total de inspección
        double[] var17 = new double[var2]; // Array para almacenar tiempo de espera
        Random var18 = new Random(); // Generador de números aleatorios para casos que excedan el array

        // Usar valores predeterminados para Rn Llegada y Rn Inspección
        for(int var19 = 0; var19 < var2; ++var19) { // Ciclo para asignar valores aleatorios a cada pieza
            if (var19 < valoresRnLlegada.length) { // Si el índice está dentro del array predefinido
                var9[var19] = valoresRnLlegada[var19]; // Usa el valor predefinido
            } else { // Si se excede el tamaño del array
                var9[var19] = var18.nextDouble(); // Si se excede el array, generar aleatorio
            }

            if (var19 < valoresRnInspeccion.length) { // Si el índice está dentro del array predefinido
                var13[var19] = valoresRnInspeccion[var19]; // Usa el valor predefinido
            } else { // Si se excede el tamaño del array
                var13[var19] = var18.nextDouble(); // Si se excede el array, generar aleatorio
            }
        }

        // Calcular tiempo entre llegadas usando la media exponencial del usuario
        for(int var20 = 0; var20 < var2; ++var20) { // Ciclo para calcular tiempo entre llegadas
            var10[var20] = -Math.log(1.0 - var9[var20]) * mediaExponencial; // Usar media exponencial ingresada - aplica fórmula de distribución exponencial
        }

        // Calcular tiempo de llegada acumulado
        var11[0] = var10[0]; // La primera pieza llega en su tiempo entre llegadas
        for(int var21 = 1; var21 < var2; ++var21) { // Ciclo para calcular llegadas acumuladas
            var11[var21] = var11[var21 - 1] + var10[var21]; // Suma el tiempo anterior más el tiempo entre llegadas actual
        }

        // Calcular tiempo de inspección usando los parámetros ingresados por el usuario
        for(int var22 = 0; var22 < var2; ++var22) { // Ciclo para calcular tiempo de inspección
            var14[var22] = normInv(var13[var22], mediaNormal, desviacionNormal); // Usar valores del usuario - aplica distribución normal inversa
            if (var14[var22] < 0.0) { // Si el tiempo calculado es negativo
                var14[var22] = 0.0; // Lo establece en cero (no puede haber tiempo negativo)
            }
        }

        // Calcular tiempos de inicio, fin, duración y espera
        var12[0] = var11[0]; // La primera pieza inicia inspección cuando llega
        var15[0] = var12[0] + var14[0]; // El fin de inspección es inicio más duración
        var17[0] = 0.0; // La primera pieza no espera
        var16[0] = var14[0]; // El tiempo total es igual al tiempo de inspección para la primera pieza

        for(int var23 = 1; var23 < var2; ++var23) { // Ciclo para las piezas restantes
            var12[var23] = Math.max(var11[var23], var15[var23 - 1]); // Inicio es el máximo entre llegada y fin de inspección anterior
            var15[var23] = var12[var23] + var14[var23]; // Fin es inicio más duración de inspección
            var17[var23] = Math.max(0.0, var12[var23] - var11[var23]); // Espera es la diferencia entre inicio e llegada (si es positiva)
            var16[var23] = var15[var23] - var11[var23]; // Tiempo total es desde llegada hasta fin de inspección
        }

        // Agregar filas a la tabla
        for(int var24 = 0; var24 < var2; ++var24) { // Ciclo para agregar cada fila a la tabla
            this.model.addRow(new Object[]{var24 + 1, String.format("%.4f", var9[var24]), String.format("%.4f", var10[var24]), String.format("%.4f", var11[var24]), String.format("%.4f", var12[var24]), String.format("%.4f", var13[var24]), String.format("%.4f", var14[var24]), String.format("%.4f", var15[var24]), String.format("%.4f", var16[var24]), String.format("%.4f", var17[var24])}); // Agrega una fila con todos los valores calculados formateados a 4 decimales
        }

    }

    public static void main(String[] args) { // Método principal para ejecutar la aplicación
        SwingUtilities.invokeLater(() -> (new ejercicio2()).setVisible(true)); // Ejecuta la interfaz en el hilo de eventos de Swing
    }
}
