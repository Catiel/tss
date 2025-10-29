package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.core.ReplicationManager; // Importa la clase ReplicationManager para acceder al gestor de réplicas y sus estadísticas agregadas
import com.simulation.statistics.Statistics; // Importa la clase Statistics para acceder a las estadísticas individuales de cada réplica
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para definir márgenes y espaciado interno
import javafx.scene.chart.*; // Importa todas las clases de gráficas de JavaFX (BarChart, LineChart, etc.)
import javafx.scene.control.Label; // Importa la clase Label de JavaFX para etiquetas de texto (aunque no se usa actualmente en el código)
import javafx.scene.control.Tab; // Importa la clase Tab de JavaFX para crear pestañas en el TabPane
import javafx.scene.control.TabPane; // Importa la clase TabPane de JavaFX para crear un contenedor con pestañas
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para crear un contenedor de layout vertical

import java.util.List; // Importa la interfaz List de Java para trabajar con listas de estadísticas

public class ChartPanel extends VBox { // Declaración de la clase pública ChartPanel que extiende VBox para ser un contenedor vertical de gráficas

    private BarChart<String, Number> utilizationChart; // Variable privada que almacena la gráfica de barras de utilización por locación
    private BarChart<String, Number> throughputChart; // Variable privada que almacena la gráfica de barras de throughput por réplica
    private LineChart<String, Number> systemTimeChart; // Variable privada que almacena la gráfica de líneas de tiempo en sistema por réplica

    public ChartPanel() { // Constructor público que inicializa el panel de gráficas sin recibir parámetros
        setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor
        setSpacing(10); // Establece un espaciado de 10 píxeles entre los elementos hijos del VBox
        initializeCharts(); // Llama al método para inicializar todas las gráficas y pestañas
    } // Cierre del constructor ChartPanel

    private void initializeCharts() { // Método privado que inicializa el TabPane con las tres gráficas en pestañas separadas
        TabPane tabPane = new TabPane(); // Crea una nueva instancia de TabPane para contener las pestañas de gráficas
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Establece que las pestañas no puedan cerrarse por el usuario

        // Gráfica de Utilización
        Tab utilizationTab = new Tab("📊 Utilización por Locación"); // Crea una nueva pestaña con el título y emoji de utilización
        utilizationTab.setContent(createUtilizationChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de utilización

        // Gráfica de Throughput
        Tab throughputTab = new Tab("📈 Throughput por Réplica"); // Crea una nueva pestaña con el título y emoji de throughput
        throughputTab.setContent(createThroughputChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de throughput

        // Gráfica de Tiempo en Sistema
        Tab systemTimeTab = new Tab("⏱ Tiempo en Sistema"); // Crea una nueva pestaña con el título y emoji de tiempo en sistema
        systemTimeTab.setContent(createSystemTimeChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de tiempo en sistema

        tabPane.getTabs().addAll(utilizationTab, throughputTab, systemTimeTab); // Agrega las tres pestañas al TabPane en el orden especificado
        getChildren().add(tabPane); // Agrega el TabPane como hijo de este VBox para que sea visible
        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS); // Establece que el TabPane debe expandirse verticalmente para ocupar todo el espacio disponible
    } // Cierre del método initializeCharts

    private VBox createUtilizationChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de utilización
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        CategoryAxis xAxis = new CategoryAxis(); // Crea un nuevo eje X categórico para mostrar nombres de locaciones
        xAxis.setLabel("Locación"); // Establece la etiqueta del eje X como "Locación"

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para mostrar porcentajes
        yAxis.setLabel("% Utilización"); // Establece la etiqueta del eje Y como "% Utilización"
        yAxis.setAutoRanging(false); // Desactiva el auto-ajuste del rango del eje Y para establecer valores fijos
        yAxis.setLowerBound(0); // Establece el límite inferior del eje Y en 0
        yAxis.setUpperBound(100); // Establece el límite superior del eje Y en 100 (porque es porcentaje)

        utilizationChart = new BarChart<>(xAxis, yAxis); // Crea una nueva gráfica de barras con los ejes X e Y definidos y la asigna a la variable de instancia
        utilizationChart.setTitle("Utilización de Locaciones - Promedio de 3 Réplicas"); // Establece el título de la gráfica
        utilizationChart.setLegendVisible(false); // Oculta la leyenda porque solo hay una serie de datos

        VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().add(utilizationChart); // Agrega la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica
    } // Cierre del método createUtilizationChart

    private VBox createThroughputChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de throughput
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        CategoryAxis xAxis = new CategoryAxis(); // Crea un nuevo eje X categórico para mostrar números de réplicas
        xAxis.setLabel("Réplica"); // Establece la etiqueta del eje X como "Réplica"

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para mostrar valores de throughput
        yAxis.setLabel("Piezas por Hora"); // Establece la etiqueta del eje Y como "Piezas por Hora"

        throughputChart = new BarChart<>(xAxis, yAxis); // Crea una nueva gráfica de barras con los ejes X e Y definidos y la asigna a la variable de instancia
        throughputChart.setTitle("Throughput del Sistema por Réplica"); // Establece el título de la gráfica
        throughputChart.setLegendVisible(false); // Oculta la leyenda porque solo hay una serie de datos

        VBox.setVgrow(throughputChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().add(throughputChart); // Agrega la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica
    } // Cierre del método createThroughputChart

    private VBox createSystemTimeChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de tiempo en sistema
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        CategoryAxis xAxis = new CategoryAxis(); // Crea un nuevo eje X categórico para mostrar números de réplicas
        xAxis.setLabel("Réplica"); // Establece la etiqueta del eje X como "Réplica"

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para mostrar valores de tiempo en minutos
        yAxis.setLabel("Minutos"); // Establece la etiqueta del eje Y como "Minutos"

        systemTimeChart = new LineChart<>(xAxis, yAxis); // Crea una nueva gráfica de líneas con los ejes X e Y definidos y la asigna a la variable de instancia
        systemTimeChart.setTitle("Tiempo Promedio en Sistema por Réplica"); // Establece el título de la gráfica
        systemTimeChart.setCreateSymbols(true); // Habilita la creación de símbolos (puntos) en cada punto de datos de la línea
        systemTimeChart.setLegendVisible(true); // Muestra la leyenda para identificar las series de datos

        VBox.setVgrow(systemTimeChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().add(systemTimeChart); // Agrega la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica
    } // Cierre del método createSystemTimeChart

    public void updateCharts(ReplicationManager.AggregatedStatistics aggStats) { // Método público que actualiza todas las gráficas con nuevas estadísticas agregadas recibiendo las estadísticas como parámetro
        if (aggStats == null) return; // Si las estadísticas agregadas son null, sale del método prematuramente

        List<Statistics> allStats = aggStats.getAllStatistics(); // Obtiene la lista de todas las estadísticas individuales de cada réplica
        if (allStats.isEmpty()) return; // Si la lista de estadísticas está vacía, sale del método prematuramente

        updateUtilizationChart(allStats); // Llama al método para actualizar la gráfica de utilización con las estadísticas
        updateThroughputChart(allStats); // Llama al método para actualizar la gráfica de throughput con las estadísticas
        updateSystemTimeChart(allStats); // Llama al método para actualizar la gráfica de tiempo en sistema con las estadísticas
    } // Cierre del método updateCharts

    private void updateUtilizationChart(List<Statistics> allStats) { // Método privado que actualiza la gráfica de utilización recibiendo la lista de estadísticas como parámetro
        utilizationChart.getData().clear(); // Limpia todos los datos existentes de la gráfica de utilización

        XYChart.Series<String, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para la gráfica
        series.setName("% Utilización Promedio"); // Establece el nombre de la serie como "% Utilización Promedio"

        // Calcular promedios por locación
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA", // Define un array con los nombres de las locaciones para las cuales se calculará la utilización
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"}; // Continuación del array de nombres de locaciones

        for (String locName : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            double avgUtil = 0; // Inicializa el acumulador de utilización promedio en 0
            int count = 0; // Inicializa el contador de réplicas válidas en 0
            for (Statistics stats : allStats) { // Bucle for-each interno que itera sobre cada estadística de réplica
                if (stats.getLocation(locName) != null) { // Condición que verifica si la locación existe en esta estadística
                    avgUtil += stats.getLocation(locName).getUtilization(stats.getSimulationDuration()); // Acumula el porcentaje de utilización de esta locación en esta réplica
                    count++; // Incrementa el contador de réplicas válidas
                } // Cierre del bloque condicional if
            } // Cierre del bucle for-each interno
            if (count > 0) { // Condición que verifica si se encontró al menos una réplica válida para esta locación
                avgUtil /= count; // Calcula el promedio de utilización dividiendo la suma acumulada entre el número de réplicas
                series.getData().add(new XYChart.Data<>(locName, avgUtil)); // Agrega un punto de datos a la serie con el nombre de la locación y su utilización promedio
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each externo

        utilizationChart.getData().add(series); // Agrega la serie completa de datos a la gráfica de utilización
    } // Cierre del método updateUtilizationChart

    private void updateThroughputChart(List<Statistics> allStats) { // Método privado que actualiza la gráfica de throughput recibiendo la lista de estadísticas como parámetro
        throughputChart.getData().clear(); // Limpia todos los datos existentes de la gráfica de throughput

        XYChart.Series<String, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para la gráfica
        series.setName("Throughput"); // Establece el nombre de la serie como "Throughput"

        for (int i = 0; i < allStats.size(); i++) { // Bucle for que itera sobre cada estadística en la lista usando índice
            Statistics stats = allStats.get(i); // Obtiene la estadística de la réplica en el índice actual
            series.getData().add(new XYChart.Data<>("Réplica " + (i + 1), stats.getThroughput())); // Agrega un punto de datos con el nombre de la réplica (base 1) y su throughput
        } // Cierre del bucle for

        throughputChart.getData().add(series); // Agrega la serie completa de datos a la gráfica de throughput
    } // Cierre del método updateThroughputChart

    private void updateSystemTimeChart(List<Statistics> allStats) { // Método privado que actualiza la gráfica de tiempo en sistema recibiendo la lista de estadísticas como parámetro
        systemTimeChart.getData().clear(); // Limpia todos los datos existentes de la gráfica de tiempo en sistema

        XYChart.Series<String, Number> avgSeries = new XYChart.Series<>(); // Crea una nueva serie de datos para la gráfica
        avgSeries.setName("Tiempo Promedio"); // Establece el nombre de la serie como "Tiempo Promedio"

        for (int i = 0; i < allStats.size(); i++) { // Bucle for que itera sobre cada estadística en la lista usando índice
            Statistics stats = allStats.get(i); // Obtiene la estadística de la réplica en el índice actual
            avgSeries.getData().add(new XYChart.Data<>("Réplica " + (i + 1), stats.getAverageSystemTime())); // Agrega un punto de datos con el nombre de la réplica (base 1) y su tiempo promedio en sistema
        } // Cierre del bucle for

        systemTimeChart.getData().add(avgSeries); // Agrega la serie completa de datos a la gráfica de tiempo en sistema
    } // Cierre del método updateSystemTimeChart

    public void clear() { // Método público que limpia todas las gráficas eliminando todos sus datos
        utilizationChart.getData().clear(); // Limpia todos los datos de la gráfica de utilización
        throughputChart.getData().clear(); // Limpia todos los datos de la gráfica de throughput
        systemTimeChart.getData().clear(); // Limpia todos los datos de la gráfica de tiempo en sistema
    } // Cierre del método clear
} // Cierre de la clase ChartPanel
