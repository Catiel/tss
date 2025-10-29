package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.core.ReplicationManager; // Importa la clase ReplicationManager para acceder al gestor de réplicas y sus estadísticas agregadas
import com.simulation.resources.Location; // Importa la clase Location para acceder a las locaciones del sistema
import com.simulation.statistics.Statistics; // Importa la clase Statistics para acceder a las estadísticas de la simulación
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para definir márgenes y espaciado interno
import javafx.scene.chart.*; // Importa todas las clases de gráficas de JavaFX (BarChart, CategoryAxis, NumberAxis, etc.)
import javafx.scene.control.Label; // Importa la clase Label de JavaFX para etiquetas de texto
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para crear un contenedor de layout vertical
import javafx.scene.paint.Color; // Importa la clase Color de JavaFX para definir colores (aunque no se usa actualmente en el código)

import java.util.List; // Importa la interfaz List de Java para trabajar con listas de estadísticas

/** // Inicio del comentario Javadoc de la clase
 * Panel con gráfica de barras de utilización por locación // Descripción de la clase
 * Similar a la gráfica de ProModel // Nota indicando que está diseñada para replicar la apariencia de ProModel
 */ // Fin del comentario Javadoc
public class UtilizationChartPanel extends VBox { // Declaración de la clase pública UtilizationChartPanel que extiende VBox para ser un contenedor vertical conteniendo una gráfica de utilización

    private BarChart<String, Number> utilizationChart; // Variable privada que almacena la gráfica de barras de utilización parametrizada con String para categorías y Number para valores
    private Label titleLabel; // Variable privada que almacena la etiqueta del título principal de la gráfica
    private Label subtitleLabel; // Variable privada que almacena la etiqueta del subtítulo de la gráfica

    public UtilizationChartPanel() { // Constructor público que inicializa el panel de gráfica de utilización sin recibir parámetros
        setPadding(new Insets(15)); // Establece un margen interno de 15 píxeles en todos los lados del contenedor
        setSpacing(10); // Establece un espaciado de 10 píxeles entre los elementos hijos del VBox
        initializeChart(); // Llama al método para inicializar la gráfica y sus componentes
    } // Cierre del constructor UtilizationChartPanel

    private void initializeChart() { // Método privado que inicializa la gráfica de barras de utilización y sus componentes visuales
        // Título principal
        titleLabel = new Label("Locación Resumen - % Utilización (Prom. Reps)"); // Crea una nueva etiqueta con el título principal indicando que muestra el promedio de réplicas
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #333;"); // Establece el estilo de la etiqueta con fuente de 18 píxeles, negrita y color de texto gris oscuro usando CSS

        // Subtítulo
        subtitleLabel = new Label("Baseline"); // Crea una nueva etiqueta con el subtítulo "Baseline"
        subtitleLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #666;"); // Establece el estilo de la etiqueta con fuente de 14 píxeles y color de texto gris medio usando CSS

        // Configurar ejes
        CategoryAxis xAxis = new CategoryAxis(); // Crea un nuevo eje X categórico para mostrar nombres de locaciones
        xAxis.setLabel("Locación"); // Establece la etiqueta del eje X como "Locación"
        xAxis.setTickLabelRotation(0); // Establece la rotación de las etiquetas de marcas en 0 grados (horizontal)

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para mostrar porcentajes de utilización
        yAxis.setLabel("% Utilización"); // Establece la etiqueta del eje Y como "% Utilización"
        yAxis.setAutoRanging(false); // Desactiva el auto-ajuste del rango del eje Y para establecer valores fijos
        yAxis.setLowerBound(0); // Establece el límite inferior del eje Y en 0
        yAxis.setUpperBound(105); // Establece el límite superior del eje Y en 105 (para dar espacio extra arriba del 100%)
        yAxis.setTickUnit(10); // Establece el intervalo entre marcas del eje Y en 10 unidades

        // Crear gráfica
        utilizationChart = new BarChart<>(xAxis, yAxis); // Crea una nueva gráfica de barras con los ejes X e Y definidos y la asigna a la variable de instancia
        utilizationChart.setLegendVisible(true); // Establece que la leyenda debe ser visible en la gráfica
        utilizationChart.setAnimated(true); // Habilita las animaciones de la gráfica para transiciones suaves
        utilizationChart.setTitle(""); // Establece el título de la gráfica como cadena vacía (el título está en la etiqueta separada)

        // Estilo
        utilizationChart.setStyle( // Establece el estilo CSS de la gráfica con múltiples propiedades
            "-fx-background-color: white;" + // Establece el color de fondo de la gráfica como blanco
            "-fx-border-color: #cccccc;" + // Establece el color del borde como gris claro
            "-fx-border-width: 1;" + // Establece el grosor del borde en 1 píxel
            "-fx-border-radius: 5;" // Establece el radio de redondeo de las esquinas del borde en 5 píxeles
        ); // Cierre del paréntesis de setStyle

        VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible

        getChildren().addAll(titleLabel, subtitleLabel, utilizationChart); // Agrega las dos etiquetas y la gráfica como hijos de este VBox en el orden especificado
    } // Cierre del método initializeChart

    /** // Inicio del comentario Javadoc del método
     * Actualiza la gráfica con estadísticas agregadas de réplicas // Descripción del método
     */ // Fin del comentario Javadoc
    public void updateChart(ReplicationManager.AggregatedStatistics aggStats) { // Método público que actualiza la gráfica con estadísticas agregadas de múltiples réplicas recibiendo las estadísticas agregadas como parámetro
        if (aggStats == null) return; // Si las estadísticas agregadas son null, sale del método prematuramente

        List<Statistics> allStats = aggStats.getAllStatistics(); // Obtiene la lista de todas las estadísticas individuales de cada réplica
        if (allStats.isEmpty()) return; // Si la lista de estadísticas está vacía, sale del método prematuramente

        utilizationChart.getData().clear(); // Limpia todos los datos existentes de la gráfica

        // Serie de datos
        XYChart.Series<String, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para la gráfica
        series.setName("Baseline"); // Establece el nombre de la serie como "Baseline"

        // Locaciones a mostrar (mismo orden que ProModel)
        String[] locations = { // Define un array con los nombres de las locaciones en el mismo orden que ProModel
            "RECEPCION", // Locación de recepción
            "LAVADORA", // Locación de lavadora
            "ALMACEN_PINTURA", // Locación de almacén de pintura
            "PINTURA", // Locación de pintura
            "ALMACEN_HORNO", // Locación de almacén del horno
            "HORNO", // Locación del horno
            "INSPECCION.1", // Primera mesa de inspección
            "INSPECCION.2", // Segunda mesa de inspección
            "INSPECCION" // Inspección total (ambas mesas)
        }; // Cierre de la declaración del array

        // Calcular utilizaciones promedio
        for (String locName : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            double avgUtil = calculateAverageUtilization(allStats, locName); // Calcula la utilización promedio de esta locación a través de todas las réplicas

            // Agregar datos
            XYChart.Data<String, Number> data = new XYChart.Data<>(locName, avgUtil); // Crea un nuevo punto de datos con el nombre de la locación y su utilización promedio
            series.getData().add(data); // Agrega el punto de datos a la serie
        } // Cierre del bucle for-each

        utilizationChart.getData().add(series); // Agrega la serie completa de datos a la gráfica

        // Aplicar colores a las barras después de que se rendericen
        applyBarColors(); // Llama al método para aplicar colores a las barras según sus valores
    } // Cierre del método updateChart con parámetro AggregatedStatistics

    /** // Inicio del comentario Javadoc del método
     * Actualiza la gráfica con estadísticas de una sola simulación // Descripción del método
     */ // Fin del comentario Javadoc
    public void updateChart(Statistics stats, double currentTime) { // Método público sobrecargado que actualiza la gráfica con estadísticas de una sola simulación recibiendo las estadísticas y el tiempo actual como parámetros
        if (stats == null) return; // Si las estadísticas son null, sale del método prematuramente

        utilizationChart.getData().clear(); // Limpia todos los datos existentes de la gráfica

        XYChart.Series<String, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para la gráfica
        series.setName("Baseline"); // Establece el nombre de la serie como "Baseline"

        String[] locations = { // Define un array con los nombres de las locaciones en el mismo orden que ProModel
            "RECEPCION", // Locación de recepción
            "LAVADORA", // Locación de lavadora
            "ALMACEN_PINTURA", // Locación de almacén de pintura
            "PINTURA", // Locación de pintura
            "ALMACEN_HORNO", // Locación de almacén del horno
            "HORNO", // Locación del horno
            "INSPECCION.1", // Primera mesa de inspección
            "INSPECCION.2", // Segunda mesa de inspección
            "INSPECCION" // Inspección total (ambas mesas)
        }; // Cierre de la declaración del array

        for (String locName : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            double util = getLocationUtilization(stats, locName, currentTime); // Obtiene la utilización de esta locación en el tiempo actual
            series.getData().add(new XYChart.Data<>(locName, util)); // Crea y agrega un nuevo punto de datos con el nombre y la utilización
        } // Cierre del bucle for-each

        utilizationChart.getData().add(series); // Agrega la serie completa de datos a la gráfica
        applyBarColors(); // Llama al método para aplicar colores a las barras según sus valores
    } // Cierre del método updateChart con parámetros Statistics y double

    /** // Inicio del comentario Javadoc del método
     * Calcula la utilización promedio de una locación a través de réplicas // Descripción del método
     */ // Fin del comentario Javadoc
    private double calculateAverageUtilization(List<Statistics> allStats, String locName) { // Método privado que calcula la utilización promedio de una locación recibiendo la lista de estadísticas y el nombre de la locación como parámetros y retornando un double
        double sum = 0; // Inicializa el acumulador de suma de utilizaciones en 0
        int count = 0; // Inicializa el contador de réplicas válidas en 0

        for (Statistics stats : allStats) { // Bucle for-each que itera sobre cada estadística de réplica
            double util = getLocationUtilization(stats, locName, stats.getSimulationDuration()); // Obtiene la utilización de esta locación en esta réplica
            if (util >= 0) { // Condición que verifica si la utilización es válida (mayor o igual a 0)
                sum += util; // Acumula la utilización a la suma total
                count++; // Incrementa el contador de réplicas válidas
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each

        return count > 0 ? sum / count : 0; // Retorna el promedio dividiendo la suma entre el contador, o 0 si no hay réplicas válidas usando operador ternario
    } // Cierre del método calculateAverageUtilization

    /** // Inicio del comentario Javadoc del método
     * Obtiene la utilización de una locación específica // Descripción del método
     */ // Fin del comentario Javadoc
    private double getLocationUtilization(Statistics stats, String locName, double currentTime) { // Método privado que obtiene la utilización de una locación específica recibiendo las estadísticas, nombre de locación y tiempo actual como parámetros y retornando un double
        Location loc = null; // Inicializa la variable loc como null para almacenar la locación

        // Manejo especial para INSPECCION
        if (locName.equals("INSPECCION.1") || locName.equals("INSPECCION.2")) { // Condición que verifica si la locación es una de las mesas individuales de inspección
            loc = stats.getLocation("INSPECCION"); // Obtiene la locación de inspección completa desde las estadísticas
            if (loc != null) { // Condición que verifica si la locación existe (no es null)
                // Para las mesas individuales, dividir la utilización total entre 2
                return loc.getUtilization(currentTime) / 2.0; // Retorna la mitad de la utilización total porque hay dos mesas
            } // Cierre del bloque condicional if interno
        } else if (locName.equals("INSPECCION")) { // Condición que verifica si la locación es INSPECCION (total)
            loc = stats.getLocation("INSPECCION"); // Obtiene la locación de inspección completa desde las estadísticas
            if (loc != null) { // Condición que verifica si la locación existe (no es null)
                // Utilización total de ambas mesas
                return loc.getUtilization(currentTime); // Retorna la utilización total de ambas mesas de inspección
            } // Cierre del bloque condicional if interno
        } else { // Bloque else que se ejecuta para todas las demás locaciones
            loc = stats.getLocation(locName); // Obtiene la locación especificada desde las estadísticas
        } // Cierre del bloque else

        return loc != null ? loc.getUtilization(currentTime) : 0; // Retorna la utilización de la locación si existe, o 0 si es null usando operador ternario
    } // Cierre del método getLocationUtilization

    /** // Inicio del comentario Javadoc del método
     * Aplica colores a las barras según su valor // Descripción del método
     */ // Fin del comentario Javadoc
    private void applyBarColors() { // Método privado que aplica colores a las barras de la gráfica según sus valores de utilización
        // Esperar a que se renderice la gráfica
        utilizationChart.applyCss(); // Aplica los estilos CSS a la gráfica para asegurar que los nodos estén creados
        utilizationChart.layout(); // Ejecuta el layout de la gráfica para asegurar que los nodos estén posicionados

        for (XYChart.Series<String, Number> series : utilizationChart.getData()) { // Bucle for-each que itera sobre cada serie de la gráfica
            for (XYChart.Data<String, Number> data : series.getData()) { // Bucle for-each interno que itera sobre cada punto de datos de la serie
                if (data.getNode() != null) { // Condición que verifica si el nodo visual del punto de datos existe (no es null)
                    double value = data.getYValue().doubleValue(); // Obtiene el valor Y del punto de datos (porcentaje de utilización) y lo convierte a double
                    String color = getColorForUtilization(value); // Obtiene el color apropiado para este valor de utilización llamando al método auxiliar
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";"); // Establece el estilo del nodo (barra) para colorearla con el color obtenido usando CSS
                } // Cierre del bloque condicional if
            } // Cierre del bucle for-each interno
        } // Cierre del bucle for-each externo
    } // Cierre del método applyBarColors

    /** // Inicio del comentario Javadoc del método
     * Retorna el color según el nivel de utilización // Descripción del método
     */ // Fin del comentario Javadoc
    private String getColorForUtilization(double utilization) { // Método privado que retorna un color hexadecimal apropiado según el valor de utilización recibiendo el valor como parámetro y retornando un String
        if (utilization < 50) { // Condición que verifica si la utilización es menor al 50%
            return "#4CAF50"; // Verde // Retorna verde para utilización baja
        } else if (utilization < 80) { // Condición que verifica si la utilización está entre 50% y 80%
            return "#FFC107"; // Amarillo // Retorna amarillo para utilización media
        } else { // Bloque else que se ejecuta si la utilización es mayor o igual al 80%
            return "#2196F3"; // Azul (similar a ProModel) // Retorna azul para utilización alta, similar al color usado en ProModel
        } // Cierre del bloque else
    } // Cierre del método getColorForUtilization

    /** // Inicio del comentario Javadoc del método
     * Limpia la gráfica // Descripción del método
     */ // Fin del comentario Javadoc
    public void clear() { // Método público que limpia la gráfica eliminando todos sus datos
        utilizationChart.getData().clear(); // Limpia todos los datos de la gráfica de utilización
    } // Cierre del método clear

    /** // Inicio del comentario Javadoc del método
     * Actualiza el subtítulo // Descripción del método
     */ // Fin del comentario Javadoc
    public void setSubtitle(String subtitle) { // Método público que actualiza el texto del subtítulo recibiendo el nuevo subtítulo como parámetro
        subtitleLabel.setText(subtitle); // Establece el texto de la etiqueta de subtítulo con el valor recibido
    } // Cierre del método setSubtitle
} // Cierre de la clase UtilizationChartPanel
