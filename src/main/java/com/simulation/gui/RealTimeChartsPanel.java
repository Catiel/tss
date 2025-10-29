package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.resources.Location; // Importa la clase Location para acceder a las locaciones del sistema
import com.simulation.statistics.Statistics; // Importa la clase Statistics para acceder a las estadísticas de la simulación
import javafx.collections.FXCollections; // Importa la clase FXCollections de JavaFX para crear colecciones observables
import javafx.collections.ObservableList; // Importa la interfaz ObservableList de JavaFX para listas que notifican cambios
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para definir márgenes y espaciado interno
import javafx.geometry.Side; // Importa la enumeración Side de JavaFX para especificar la posición de elementos (Top, Bottom, Left, Right)
import javafx.scene.chart.*; // Importa todas las clases de gráficas de JavaFX (LineChart, BarChart, etc.)
import javafx.scene.control.Label; // Importa la clase Label de JavaFX para etiquetas de texto
import javafx.scene.control.Tab; // Importa la clase Tab de JavaFX para crear pestañas
import javafx.scene.control.TabPane; // Importa la clase TabPane de JavaFX para crear un contenedor con pestañas
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para crear un contenedor de layout vertical

import java.util.HashMap; // Importa la clase HashMap de Java para crear mapas clave-valor
import java.util.Map; // Importa la interfaz Map de Java para trabajar con mapas

/** // Inicio del comentario Javadoc de la clase
 * Panel con múltiples gráficas que se actualizan en tiempo real // Descripción de la clase
 */ // Fin del comentario Javadoc
public class RealTimeChartsPanel extends VBox { // Declaración de la clase pública RealTimeChartsPanel que extiende VBox para ser un contenedor vertical de gráficas en tiempo real

    private TabPane tabPane; // Variable privada que almacena el panel de pestañas que contiene todas las gráficas

    // Gráfica 2: Piezas en el sistema
    private LineChart<Number, Number> systemPiecesChart; // Variable privada que almacena la gráfica de líneas de piezas en el sistema
    private XYChart.Series<Number, Number> arrivalsSeriesSystem; // Variable privada que almacena la serie de datos de arribos acumulados
    private XYChart.Series<Number, Number> exitsSeriesSystem; // Variable privada que almacena la serie de datos de salidas acumuladas
    private XYChart.Series<Number, Number> inSystemSeries; // Variable privada que almacena la serie de datos de piezas en el sistema

    // Gráfica 3: Utilización de locaciones
    private BarChart<String, Number> utilizationChart; // Variable privada que almacena la gráfica de barras de utilización de locaciones

    // Gráfica 4: Contenido de locaciones en tiempo real
    private LineChart<Number, Number> locationContentChart; // Variable privada que almacena la gráfica de líneas de contenido de locaciones
    private Map<String, XYChart.Series<Number, Number>> contentSeriesMap; // Variable privada que almacena un mapa de nombres de locaciones a sus series de datos de contenido

    // Gráfica 5: Tiempo promedio en sistema
    private LineChart<Number, Number> avgSystemTimeChart; // Variable privada que almacena la gráfica de líneas de tiempo promedio en sistema
    private XYChart.Series<Number, Number> avgTimeSeries; // Variable privada que almacena la serie de datos de tiempo promedio en sistema

    private static final int MAX_DATA_POINTS = 5000; // Constante estática final que define el número máximo de puntos de datos antes de aplicar downsampling
    private int updateCounter = 0; // Variable privada que cuenta el número de actualizaciones para controlar la frecuencia de actualización, inicializada en 0

    public RealTimeChartsPanel() { // Constructor público que inicializa el panel de gráficas en tiempo real sin recibir parámetros
        setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor
        setSpacing(10); // Establece un espaciado de 10 píxeles entre los elementos hijos del VBox

        contentSeriesMap = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar las series de contenido de cada locación

        initializeCharts(); // Llama al método para inicializar todas las gráficas y pestañas
    } // Cierre del constructor RealTimeChartsPanel

    public void initializeState(Statistics stats) { // Método público que inicializa el estado de las gráficas con estadísticas iniciales recibiendo el objeto de estadísticas como parámetro
        reset(); // Llama al método reset para limpiar todas las gráficas
        double initialTime = Math.max(0, stats.getSimulationDuration()); // Calcula el tiempo inicial como el máximo entre 0 y la duración de la simulación
        updateSystemPiecesChart(stats, initialTime); // Actualiza la gráfica de piezas en el sistema con el tiempo inicial
        updateUtilizationChart(stats, initialTime); // Actualiza la gráfica de utilización con el tiempo inicial
        updateLocationContentChart(stats, initialTime); // Actualiza la gráfica de contenido de locaciones con el tiempo inicial
        updateAvgSystemTimeChart(stats, initialTime); // Actualiza la gráfica de tiempo promedio en sistema con el tiempo inicial
    } // Cierre del método initializeState

    private void initializeCharts() { // Método privado que inicializa todas las gráficas y crea las pestañas
        tabPane = new TabPane(); // Crea una nueva instancia de TabPane para contener las pestañas de gráficas
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Establece que las pestañas no puedan cerrarse por el usuario
    tabPane.setStyle("-fx-font-size: 14px;"); // Establece el tamaño de fuente del TabPane a 14 píxeles usando CSS

        // Tab 1: Throughput
    // Tab 1: Piezas en Sistema
    Tab systemPiecesTab = new Tab("📦 Piezas en el Sistema"); // Crea una nueva pestaña con el título y emoji de piezas en el sistema
        systemPiecesTab.setContent(createSystemPiecesChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de piezas en el sistema

    // Tab 2: Utilización de Locaciones
    Tab utilizationTab = new Tab("📊 Utilización de Locaciones"); // Crea una nueva pestaña con el título y emoji de utilización de locaciones
        utilizationTab.setContent(createUtilizationChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de utilización

    // Tab 3: Contenido de Locaciones
    Tab contentTab = new Tab("📍 Contenido por Locación"); // Crea una nueva pestaña con el título y emoji de contenido por locación
        contentTab.setContent(createLocationContentChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de contenido de locaciones

    // Tab 4: Tiempo Promedio en Sistema
    Tab avgTimeTab = new Tab("⏱ Tiempo Promedio en Sistema"); // Crea una nueva pestaña con el título y emoji de tiempo promedio en sistema
        avgTimeTab.setContent(createAvgSystemTimeChart()); // Establece el contenido de la pestaña llamando al método que crea la gráfica de tiempo promedio

    tabPane.getTabs().addAll(systemPiecesTab, utilizationTab, contentTab, avgTimeTab); // Agrega las cuatro pestañas al TabPane en el orden especificado

        VBox.setVgrow(tabPane, javafx.scene.layout.Priority.ALWAYS); // Establece que el TabPane debe expandirse verticalmente para ocupar todo el espacio disponible
        getChildren().add(tabPane); // Agrega el TabPane como hijo de este VBox para que sea visible
    } // Cierre del método initializeCharts

    // ========== GRÁFICA 1: PIEZAS EN SISTEMA ==========
    private VBox createSystemPiecesChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de piezas en el sistema
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        Label title = new Label("Arribos, Salidas y Piezas en Sistema"); // Crea una nueva etiqueta con el título de la gráfica
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"); // Establece el estilo de la etiqueta con fuente de 16 píxeles y negrita usando CSS

        NumberAxis xAxis = new NumberAxis(); // Crea un nuevo eje X numérico para la gráfica
        xAxis.setLabel("Tiempo (minutos)"); // Establece la etiqueta del eje X como "Tiempo (minutos)"
        xAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje X según los datos

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para la gráfica
        yAxis.setLabel("Cantidad de Piezas"); // Establece la etiqueta del eje Y como "Cantidad de Piezas"
        yAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje Y según los datos

        systemPiecesChart = new LineChart<>(xAxis, yAxis); // Crea una nueva gráfica de líneas con los ejes X e Y definidos y la asigna a la variable de instancia
        systemPiecesChart.setTitle("Estado del Sistema"); // Establece el título de la gráfica como "Estado del Sistema"
        systemPiecesChart.setCreateSymbols(false); // Desactiva la creación de símbolos (puntos) en cada punto de datos para mejorar el rendimiento
        systemPiecesChart.setAnimated(false); // Desactiva las animaciones de la gráfica para mejorar el rendimiento

        arrivalsSeriesSystem = new XYChart.Series<>(); // Crea una nueva serie de datos para los arribos
        arrivalsSeriesSystem.setName("Arribos Acumulados"); // Establece el nombre de la serie como "Arribos Acumulados"

        exitsSeriesSystem = new XYChart.Series<>(); // Crea una nueva serie de datos para las salidas
        exitsSeriesSystem.setName("Salidas Acumuladas"); // Establece el nombre de la serie como "Salidas Acumuladas"

        inSystemSeries = new XYChart.Series<>(); // Crea una nueva serie de datos para las piezas en el sistema
        inSystemSeries.setName("En Sistema"); // Establece el nombre de la serie como "En Sistema"

    systemPiecesChart.getData().add(arrivalsSeriesSystem); // Agrega la serie de arribos a la gráfica
    systemPiecesChart.getData().add(exitsSeriesSystem); // Agrega la serie de salidas a la gráfica
    systemPiecesChart.getData().add(inSystemSeries); // Agrega la serie de piezas en sistema a la gráfica

    applyLargeFont(systemPiecesChart); // Aplica un tamaño de fuente grande a la gráfica
    VBox.setVgrow(systemPiecesChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, systemPiecesChart); // Agrega la etiqueta de título y la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica configurada
    } // Cierre del método createSystemPiecesChart

    // ========== GRÁFICA 3: UTILIZACIÓN ==========
    private VBox createUtilizationChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de utilización
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        Label title = new Label("Utilización Actual de Locaciones (%)"); // Crea una nueva etiqueta con el título de la gráfica
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"); // Establece el estilo de la etiqueta con fuente de 16 píxeles y negrita usando CSS

        CategoryAxis xAxis = new CategoryAxis(); // Crea un nuevo eje X categórico para mostrar nombres de locaciones
        xAxis.setLabel("Locación"); // Establece la etiqueta del eje X como "Locación"

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para mostrar porcentajes
        yAxis.setLabel("% Utilización"); // Establece la etiqueta del eje Y como "% Utilización"
        yAxis.setAutoRanging(false); // Desactiva el auto-ajuste del rango del eje Y para establecer valores fijos
        yAxis.setLowerBound(0); // Establece el límite inferior del eje Y en 0
        yAxis.setUpperBound(105); // Establece el límite superior del eje Y en 105 (para dar espacio extra arriba del 100%)

        utilizationChart = new BarChart<>(xAxis, yAxis); // Crea una nueva gráfica de barras con los ejes X e Y definidos y la asigna a la variable de instancia
        utilizationChart.setTitle("Utilización en Tiempo Real"); // Establece el título de la gráfica como "Utilización en Tiempo Real"
        utilizationChart.setLegendVisible(false); // Oculta la leyenda porque solo hay una serie de datos
        utilizationChart.setAnimated(false); // Desactiva las animaciones de la gráfica para mejorar el rendimiento

    applyLargeFont(utilizationChart); // Aplica un tamaño de fuente grande a la gráfica
    VBox.setVgrow(utilizationChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, utilizationChart); // Agrega la etiqueta de título y la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica configurada
    } // Cierre del método createUtilizationChart

    // ========== GRÁFICA 4: CONTENIDO DE LOCACIONES ==========
    private VBox createLocationContentChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de contenido de locaciones
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        Label title = new Label("Contenido de Locaciones en Tiempo Real"); // Crea una nueva etiqueta con el título de la gráfica
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"); // Establece el estilo de la etiqueta con fuente de 16 píxeles y negrita usando CSS

        NumberAxis xAxis = new NumberAxis(); // Crea un nuevo eje X numérico para el tiempo
        xAxis.setLabel("Tiempo (minutos)"); // Establece la etiqueta del eje X como "Tiempo (minutos)"
        xAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje X según los datos

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para la cantidad de piezas
        yAxis.setLabel("Cantidad de Piezas"); // Establece la etiqueta del eje Y como "Cantidad de Piezas"
        yAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje Y según los datos

        locationContentChart = new LineChart<>(xAxis, yAxis); // Crea una nueva gráfica de líneas con los ejes X e Y definidos y la asigna a la variable de instancia
        locationContentChart.setTitle("Piezas por Locación"); // Establece el título de la gráfica como "Piezas por Locación"
        locationContentChart.setCreateSymbols(false); // Desactiva la creación de símbolos (puntos) en cada punto de datos para mejorar el rendimiento
        locationContentChart.setAnimated(false); // Desactiva las animaciones de la gráfica para mejorar el rendimiento
        locationContentChart.setLegendSide(Side.RIGHT); // Establece que la leyenda debe mostrarse en el lado derecho de la gráfica

        // Crear series para cada locación
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA", // Define un array con los nombres de las locaciones para las cuales se mostrarán series
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"}; // Continuación del array de nombres de locaciones

        for (String loc : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            XYChart.Series<Number, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para esta locación
            series.setName(loc); // Establece el nombre de la serie como el nombre de la locación
            contentSeriesMap.put(loc, series); // Agrega la serie al mapa de series usando el nombre de la locación como clave
            locationContentChart.getData().add(series); // Agrega la serie a la gráfica
        } // Cierre del bucle for-each

    applyLargeFont(locationContentChart); // Aplica un tamaño de fuente grande a la gráfica
    VBox.setVgrow(locationContentChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, locationContentChart); // Agrega la etiqueta de título y la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica configurada
    } // Cierre del método createLocationContentChart

    // ========== GRÁFICA 5: TIEMPO PROMEDIO EN SISTEMA ==========
    private VBox createAvgSystemTimeChart() { // Método privado que crea y retorna un VBox conteniendo la gráfica de tiempo promedio en sistema
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

        Label title = new Label("Tiempo Promedio en Sistema (minutos)"); // Crea una nueva etiqueta con el título de la gráfica
        title.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;"); // Establece el estilo de la etiqueta con fuente de 16 píxeles y negrita usando CSS

        NumberAxis xAxis = new NumberAxis(); // Crea un nuevo eje X numérico para el tiempo
        xAxis.setLabel("Tiempo (minutos)"); // Establece la etiqueta del eje X como "Tiempo (minutos)"
        xAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje X según los datos

        NumberAxis yAxis = new NumberAxis(); // Crea un nuevo eje Y numérico para el tiempo promedio
        yAxis.setLabel("Tiempo Promedio (min)"); // Establece la etiqueta del eje Y como "Tiempo Promedio (min)"
        yAxis.setAutoRanging(true); // Habilita el auto-ajuste del rango del eje Y según los datos

        avgSystemTimeChart = new LineChart<>(xAxis, yAxis); // Crea una nueva gráfica de líneas con los ejes X e Y definidos y la asigna a la variable de instancia
        avgSystemTimeChart.setTitle("Evolución del Tiempo Promedio"); // Establece el título de la gráfica como "Evolución del Tiempo Promedio"
        avgSystemTimeChart.setCreateSymbols(false); // Desactiva la creación de símbolos (puntos) en cada punto de datos para mejorar el rendimiento
        avgSystemTimeChart.setAnimated(false); // Desactiva las animaciones de la gráfica para mejorar el rendimiento

        avgTimeSeries = new XYChart.Series<>(); // Crea una nueva serie de datos para el tiempo promedio
        avgTimeSeries.setName("Tiempo Promedio"); // Establece el nombre de la serie como "Tiempo Promedio"
        avgSystemTimeChart.getData().add(avgTimeSeries); // Agrega la serie a la gráfica

    applyLargeFont(avgSystemTimeChart); // Aplica un tamaño de fuente grande a la gráfica
    VBox.setVgrow(avgSystemTimeChart, javafx.scene.layout.Priority.ALWAYS); // Establece que la gráfica debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, avgSystemTimeChart); // Agrega la etiqueta de título y la gráfica al contenedor VBox

        return container; // Retorna el contenedor VBox con la gráfica configurada
    } // Cierre del método createAvgSystemTimeChart

    /** // Inicio del comentario Javadoc del método
     * Actualiza todas las gráficas con nuevos datos // Descripción del método
     */ // Fin del comentario Javadoc
    public void updateCharts(Statistics stats, double currentTime) { // Método público que actualiza todas las gráficas con nuevas estadísticas recibiendo las estadísticas y el tiempo actual como parámetros
        if (currentTime <= 0) return; // Si el tiempo actual es menor o igual a 0, sale del método prematuramente

        // Actualizar solo cada N llamadas para mejor rendimiento
        updateCounter++; // Incrementa el contador de actualizaciones
        if (updateCounter % 5 != 0 && currentTime < stats.getSimulationDuration()) { // Condición que verifica si el contador no es múltiplo de 5 y la simulación aún no ha terminado
            return; // Saltar algunas actualizaciones durante la simulación // Sale del método prematuramente para reducir la frecuencia de actualización y mejorar el rendimiento
        } // Cierre del bloque condicional if

        // 1. Actualizar Piezas en Sistema
        updateSystemPiecesChart(stats, currentTime); // Llama al método para actualizar la gráfica de piezas en el sistema

        // 2. Actualizar Utilización
        updateUtilizationChart(stats, currentTime); // Llama al método para actualizar la gráfica de utilización

        // 3. Actualizar Contenido de Locaciones
        updateLocationContentChart(stats, currentTime); // Llama al método para actualizar la gráfica de contenido de locaciones

        // 4. Actualizar Tiempo Promedio en Sistema
        updateAvgSystemTimeChart(stats, currentTime); // Llama al método para actualizar la gráfica de tiempo promedio en sistema
    } // Cierre del método updateCharts

    private void updateSystemPiecesChart(Statistics stats, double currentTime) { // Método privado que actualiza la gráfica de piezas en el sistema recibiendo las estadísticas y el tiempo actual como parámetros
        int arrivals = stats.getTotalArrivals(); // Obtiene el número total de arribos desde las estadísticas
        int exits = stats.getTotalExits(); // Obtiene el número total de salidas desde las estadísticas
        int inSystem = arrivals - exits; // Calcula el número de piezas en el sistema restando salidas de arribos

        arrivalsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, arrivals)); // Agrega un nuevo punto de datos a la serie de arribos con el tiempo actual y el número de arribos
        exitsSeriesSystem.getData().add(new XYChart.Data<>(currentTime, exits)); // Agrega un nuevo punto de datos a la serie de salidas con el tiempo actual y el número de salidas
        inSystemSeries.getData().add(new XYChart.Data<>(currentTime, inSystem)); // Agrega un nuevo punto de datos a la serie de piezas en sistema con el tiempo actual y el número calculado

        enforceSeriesLimit(arrivalsSeriesSystem); // Llama al método para aplicar downsampling a la serie de arribos si excede el límite de puntos
        enforceSeriesLimit(exitsSeriesSystem); // Llama al método para aplicar downsampling a la serie de salidas si excede el límite de puntos
        enforceSeriesLimit(inSystemSeries); // Llama al método para aplicar downsampling a la serie de piezas en sistema si excede el límite de puntos
    } // Cierre del método updateSystemPiecesChart

    private void updateUtilizationChart(Statistics stats, double currentTime) { // Método privado que actualiza la gráfica de utilización recibiendo las estadísticas y el tiempo actual como parámetros
        utilizationChart.getData().clear(); // Limpia todos los datos existentes de la gráfica de utilización

        XYChart.Series<String, Number> series = new XYChart.Series<>(); // Crea una nueva serie de datos para la utilización
        series.setName("Utilización"); // Establece el nombre de la serie como "Utilización"

        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA", // Define un array con los nombres de las locaciones para las cuales se calculará la utilización
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"}; // Continuación del array de nombres de locaciones

        for (String locName : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            Location loc = stats.getLocation(locName); // Obtiene el objeto Location correspondiente a esta locación desde las estadísticas
            if (loc != null) { // Condición que verifica si la locación existe (no es null)
                double util = loc.getUtilization(currentTime); // Obtiene el porcentaje de utilización de la locación en el tiempo actual
                XYChart.Data<String, Number> data = new XYChart.Data<>(locName, util); // Crea un nuevo punto de datos con el nombre de la locación y su utilización
                series.getData().add(data); // Agrega el punto de datos a la serie
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each

        utilizationChart.getData().add(series); // Agrega la serie completa de datos a la gráfica de utilización

        // Aplicar colores a las barras
        applyBarColors(); // Llama al método para aplicar colores a las barras según su valor de utilización
    } // Cierre del método updateUtilizationChart

    private void updateLocationContentChart(Statistics stats, double currentTime) { // Método privado que actualiza la gráfica de contenido de locaciones recibiendo las estadísticas y el tiempo actual como parámetros
        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA", // Define un array con los nombres de las locaciones para las cuales se actualizará el contenido
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"}; // Continuación del array de nombres de locaciones

        for (String locName : locations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            Location loc = stats.getLocation(locName); // Obtiene el objeto Location correspondiente a esta locación desde las estadísticas
            if (loc != null) { // Condición que verifica si la locación existe (no es null)
                XYChart.Series<Number, Number> series = contentSeriesMap.get(locName); // Obtiene la serie de datos correspondiente a esta locación desde el mapa de series
                if (series != null) { // Condición que verifica si la serie existe (no es null)
                    int content = loc.getCurrentContent(); // Obtiene el contenido actual de la locación (número de piezas)
                    series.getData().add(new XYChart.Data<>(currentTime, content)); // Agrega un nuevo punto de datos a la serie con el tiempo actual y el contenido
                    enforceSeriesLimit(series); // Llama al método para aplicar downsampling a la serie si excede el límite de puntos
                } // Cierre del bloque condicional if interno
            } // Cierre del bloque condicional if externo
        } // Cierre del bucle for-each
    } // Cierre del método updateLocationContentChart

    private void updateAvgSystemTimeChart(Statistics stats, double currentTime) { // Método privado que actualiza la gráfica de tiempo promedio en sistema recibiendo las estadísticas y el tiempo actual como parámetros
        if (stats.getTotalExits() > 0) { // Condición que verifica si hay al menos una salida (para evitar división por cero)
            double avgTime = stats.getAverageSystemTime(); // Obtiene el tiempo promedio en sistema desde las estadísticas
            avgTimeSeries.getData().add(new XYChart.Data<>(currentTime, avgTime)); // Agrega un nuevo punto de datos a la serie con el tiempo actual y el tiempo promedio
            enforceSeriesLimit(avgTimeSeries); // Llama al método para aplicar downsampling a la serie si excede el límite de puntos
        } // Cierre del bloque condicional if
    } // Cierre del método updateAvgSystemTimeChart

    private void enforceSeriesLimit(XYChart.Series<Number, Number> series) { // Método privado que aplica downsampling a una serie de datos si excede el límite máximo recibiendo la serie como parámetro
        ObservableList<XYChart.Data<Number, Number>> data = series.getData(); // Obtiene la lista observable de datos de la serie
        if (data.size() <= MAX_DATA_POINTS) { // Condición que verifica si el tamaño de los datos es menor o igual al límite máximo
            return; // Sale del método prematuramente si no se excede el límite
        } // Cierre del bloque condicional if

        ObservableList<XYChart.Data<Number, Number>> downsampled = FXCollections.observableArrayList(); // Crea una nueva lista observable vacía para almacenar los datos reducidos
        for (int i = 0; i < data.size(); i += 2) { // Bucle for que itera sobre los datos tomando cada segundo punto (reducción a la mitad)
            downsampled.add(data.get(i)); // Agrega el punto actual a la lista de datos reducidos
        } // Cierre del bucle for

        // Garantizar que el último punto siempre se conserve.
        XYChart.Data<Number, Number> lastPoint = data.get(data.size() - 1); // Obtiene el último punto de datos de la lista original
        if (downsampled.isEmpty() || downsampled.get(downsampled.size() - 1) != lastPoint) { // Condición que verifica si la lista reducida está vacía o si su último punto no es el último punto original
            downsampled.add(lastPoint); // Agrega el último punto original a la lista reducida para garantizar que siempre se muestre el punto más reciente
        } // Cierre del bloque condicional if

        data.setAll(downsampled); // Reemplaza todos los datos de la serie con los datos reducidos
    } // Cierre del método enforceSeriesLimit

    private void applyBarColors() { // Método privado que aplica colores a las barras de la gráfica de utilización según sus valores
        utilizationChart.applyCss(); // Aplica los estilos CSS a la gráfica para asegurar que los nodos estén creados
        utilizationChart.layout(); // Ejecuta el layout de la gráfica para asegurar que los nodos estén posicionados

        for (XYChart.Series<String, Number> series : utilizationChart.getData()) { // Bucle for-each que itera sobre cada serie de la gráfica de utilización
            for (XYChart.Data<String, Number> data : series.getData()) { // Bucle for-each interno que itera sobre cada punto de datos de la serie
                if (data.getNode() != null) { // Condición que verifica si el nodo visual del punto de datos existe (no es null)
                    double value = data.getYValue().doubleValue(); // Obtiene el valor Y del punto de datos (porcentaje de utilización) y lo convierte a double
                    String color = getColorForUtilization(value); // Obtiene el color apropiado para este valor de utilización llamando al método auxiliar
                    data.getNode().setStyle("-fx-bar-fill: " + color + ";"); // Establece el estilo del nodo (barra) para colorearla con el color obtenido usando CSS
                } // Cierre del bloque condicional if
            } // Cierre del bucle for-each interno
        } // Cierre del bucle for-each externo
    } // Cierre del método applyBarColors

    private String getColorForUtilization(double utilization) { // Método privado que retorna un color hexadecimal apropiado según el valor de utilización recibiendo el valor como parámetro
        if (utilization < 50) { // Condición que verifica si la utilización es menor al 50%
            return "#4CAF50"; // Verde // Retorna verde para utilización baja
        } else if (utilization < 80) { // Condición que verifica si la utilización está entre 50% y 80%
            return "#FFC107"; // Amarillo // Retorna amarillo para utilización media
        } else { // Bloque else que se ejecuta si la utilización es mayor o igual al 80%
            return "#2196F3"; // Azul // Retorna azul para utilización alta
        } // Cierre del bloque else
    } // Cierre del método getColorForUtilization

    private void applyLargeFont(Chart chart) { // Método privado que aplica un tamaño de fuente grande a una gráfica recibiendo la gráfica como parámetro
        chart.setStyle("-fx-font-size: 14px;"); // Establece el tamaño de fuente de la gráfica a 14 píxeles usando CSS
    } // Cierre del método applyLargeFont

    /** // Inicio del comentario Javadoc del método
     * Reinicia todas las gráficas // Descripción del método
     */ // Fin del comentario Javadoc
    public void reset() { // Método público que reinicia todas las gráficas limpiando sus datos
        arrivalsSeriesSystem.getData().clear(); // Limpia todos los datos de la serie de arribos
        exitsSeriesSystem.getData().clear(); // Limpia todos los datos de la serie de salidas
        inSystemSeries.getData().clear(); // Limpia todos los datos de la serie de piezas en sistema
        utilizationChart.getData().clear(); // Limpia todos los datos de la gráfica de utilización
        avgTimeSeries.getData().clear(); // Limpia todos los datos de la serie de tiempo promedio

        for (XYChart.Series<Number, Number> series : contentSeriesMap.values()) { // Bucle for-each que itera sobre cada serie en el mapa de series de contenido
            series.getData().clear(); // Limpia todos los datos de la serie actual
        } // Cierre del bucle for-each

        updateCounter = 0; // Reinicia el contador de actualizaciones a 0

    } // Cierre del método reset
} // Cierre de la clase RealTimeChartsPanel
