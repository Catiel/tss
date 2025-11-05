package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.resources.Location; // Importa la clase Location para acceder a las locaciones del sistema
import com.simulation.statistics.Statistics; // Importa la clase Statistics para acceder a las estadísticas de la simulación
import javafx.beans.property.*; // Importa todas las clases de propiedades de JavaFX (StringProperty, IntegerProperty, DoubleProperty, etc.)
import javafx.collections.FXCollections; // Importa la clase FXCollections de JavaFX para crear colecciones observables
import javafx.collections.ObservableList; // Importa la interfaz ObservableList de JavaFX para listas que notifican cambios
import javafx.geometry.Insets; // Importa la clase Insets de JavaFX para definir márgenes y espaciado interno
import javafx.scene.control.*; // Importa todas las clases de controles de JavaFX (TableView, TableColumn, Label, etc.)
import javafx.scene.control.cell.PropertyValueFactory; // Importa la clase PropertyValueFactory para vincular propiedades de objetos con columnas de tablas
import javafx.scene.layout.BorderPane; // Importa la clase BorderPane de JavaFX para el layout principal
import javafx.scene.layout.VBox; // Importa la clase VBox de JavaFX para layout vertical

import java.util.Arrays; // Importa la clase Arrays para trabajar con colecciones utilitarias
import java.util.Map; // Importa la interfaz Map de Java para trabajar con mapas clave-valor

/** // Inicio del comentario Javadoc de la clase
 * Panel de resultados con tablas de estadísticas // Descripción de la clase
 */ // Fin del comentario Javadoc
public class ResultsTablePanel extends BorderPane { // Declaración de la clase pública ResultsTablePanel que extiende BorderPane para ser un panel con layout BorderPane conteniendo tablas de resultados

    private TableView<LocationRow> locationTable; // Variable privada que almacena la tabla de estadísticas de locaciones
    private TableView<EntityRow> entityTable; // Variable privada que almacena la tabla de estadísticas de entidades
    private TabPane tabPane; // Variable privada que almacena el panel de pestañas que contiene ambas tablas

    public ResultsTablePanel() { // Constructor público que inicializa el panel de resultados sin recibir parámetros
        initializeUI(); // Llama al método para inicializar la interfaz de usuario
    } // Cierre del constructor ResultsTablePanel

    private void initializeUI() { // Método privado que inicializa la interfaz de usuario creando las pestañas y tablas
        tabPane = new TabPane(); // Crea una nueva instancia de TabPane para contener las pestañas de tablas
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE); // Establece que las pestañas no puedan cerrarse por el usuario

        // Pestaña de Locaciones
        Tab locationTab = new Tab("📍 Estadísticas de Locaciones"); // Crea una nueva pestaña con el título y emoji de estadísticas de locaciones
        locationTab.setContent(createLocationTable()); // Establece el contenido de la pestaña llamando al método que crea la tabla de locaciones

        // Pestaña de Entidades
        Tab entityTab = new Tab("📦 Estadísticas de Entidades"); // Crea una nueva pestaña con el título y emoji de estadísticas de entidades
        entityTab.setContent(createEntityTable()); // Establece el contenido de la pestaña llamando al método que crea la tabla de entidades

        tabPane.getTabs().addAll(locationTab, entityTab); // Agrega las dos pestañas al TabPane en el orden especificado

        setCenter(tabPane); // Establece el TabPane en el centro del BorderPane
        setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del BorderPane
    } // Cierre del método initializeUI

    private VBox createLocationTable() { // Método privado que crea y retorna un VBox conteniendo la tabla de estadísticas de locaciones
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

    Label title = new Label("Resumen de Locaciones"); // Crea una nueva etiqueta con el título de la tabla
    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Presenta un título compacto fuera de la animación

    locationTable = new TableView<>(); // Crea una nueva instancia de TableView parametrizada con LocationRow para almacenar las filas de locaciones
    locationTable.setStyle("-fx-font-size: 13px;"); // Ajusta el tamaño del texto de la tabla para mantener la interfaz ligera

        // Columnas
        TableColumn<LocationRow, String> nameCol = new TableColumn<>("Locación"); // Crea una nueva columna de tabla parametrizada con LocationRow y String para mostrar nombres de locaciones con el encabezado "Locación"
        nameCol.setCellValueFactory(new PropertyValueFactory<>("name")); // Establece la fábrica de valores de celda para vincular con la propiedad "name" de LocationRow
        nameCol.setPrefWidth(150); // Establece el ancho preferido de la columna en 150 píxeles

        TableColumn<LocationRow, Integer> capacityCol = new TableColumn<>("Capacidad"); // Crea una nueva columna parametrizada con LocationRow e Integer para mostrar capacidades con el encabezado "Capacidad"
        capacityCol.setCellValueFactory(new PropertyValueFactory<>("capacity")); // Establece la fábrica de valores de celda para vincular con la propiedad "capacity" de LocationRow
        capacityCol.setPrefWidth(80); // Establece el ancho preferido de la columna en 80 píxeles
        capacityCol.setStyle("-fx-alignment: CENTER;"); // Establece la alineación del contenido de la columna al centro usando CSS

        TableColumn<LocationRow, Integer> entriesCol = new TableColumn<>("Total Entradas"); // Crea una nueva columna parametrizada con LocationRow e Integer para mostrar total de entradas con el encabezado "Total Entradas"
        entriesCol.setCellValueFactory(new PropertyValueFactory<>("totalEntries")); // Establece la fábrica de valores de celda para vincular con la propiedad "totalEntries" de LocationRow
        entriesCol.setPrefWidth(120); // Establece el ancho preferido de la columna en 120 píxeles
        entriesCol.setStyle("-fx-alignment: CENTER;"); // Establece la alineación del contenido de la columna al centro usando CSS

        TableColumn<LocationRow, Double> timePerEntryCol = new TableColumn<>("Tiempo/Entrada (min)"); // Crea una nueva columna parametrizada con LocationRow y Double para mostrar tiempo por entrada con el encabezado "Tiempo/Entrada (min)"
        timePerEntryCol.setCellValueFactory(new PropertyValueFactory<>("timePerEntry")); // Establece la fábrica de valores de celda para vincular con la propiedad "timePerEntry" de LocationRow
        timePerEntryCol.setPrefWidth(150); // Establece el ancho preferido de la columna en 150 píxeles
        timePerEntryCol.setStyle("-fx-alignment: CENTER-RIGHT;"); // Establece la alineación del contenido de la columna al centro-derecha usando CSS
        timePerEntryCol.setCellFactory(col -> new TableCell<LocationRow, Double>() { // Establece una fábrica de celdas personalizada para formatear los valores de tiempo por entrada
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                } // Cierre del bloque else
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory

        TableColumn<LocationRow, Double> avgContentCol = new TableColumn<>("Contenido Promedio"); // Crea una nueva columna parametrizada con LocationRow y Double para mostrar contenido promedio con el encabezado "Contenido Promedio"
        avgContentCol.setCellValueFactory(new PropertyValueFactory<>("avgContent")); // Establece la fábrica de valores de celda para vincular con la propiedad "avgContent" de LocationRow
        avgContentCol.setPrefWidth(140); // Establece el ancho preferido de la columna en 140 píxeles
        avgContentCol.setStyle("-fx-alignment: CENTER-RIGHT;"); // Establece la alineación del contenido de la columna al centro-derecha usando CSS
        avgContentCol.setCellFactory(col -> new TableCell<LocationRow, Double>() { // Establece una fábrica de celdas personalizada para formatear los valores de contenido promedio
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f", item)); // Formatea el valor como string con 2 decimales y lo establece como texto de la celda
                } // Cierre del bloque else
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory

        TableColumn<LocationRow, Integer> maxContentCol = new TableColumn<>("Contenido Máximo"); // Crea una nueva columna parametrizada con LocationRow e Integer para mostrar contenido máximo con el encabezado "Contenido Máximo"
        maxContentCol.setCellValueFactory(new PropertyValueFactory<>("maxContent")); // Establece la fábrica de valores de celda para vincular con la propiedad "maxContent" de LocationRow
        maxContentCol.setPrefWidth(130); // Establece el ancho preferido de la columna en 130 píxeles
        maxContentCol.setStyle("-fx-alignment: CENTER;"); // Establece la alineación del contenido de la columna al centro usando CSS

        TableColumn<LocationRow, Integer> currentContentCol = new TableColumn<>("Contenido Actual"); // Crea una nueva columna parametrizada con LocationRow e Integer para mostrar contenido actual con el encabezado "Contenido Actual"
        currentContentCol.setCellValueFactory(new PropertyValueFactory<>("currentContent")); // Establece la fábrica de valores de celda para vincular con la propiedad "currentContent" de LocationRow
        currentContentCol.setPrefWidth(130); // Establece el ancho preferido de la columna en 130 píxeles
        currentContentCol.setStyle("-fx-alignment: CENTER;"); // Establece la alineación del contenido de la columna al centro usando CSS

        TableColumn<LocationRow, Double> utilizationCol = new TableColumn<>("% Utilización"); // Crea una nueva columna parametrizada con LocationRow y Double para mostrar porcentaje de utilización con el encabezado "% Utilización"
        utilizationCol.setCellValueFactory(new PropertyValueFactory<>("utilization")); // Establece la fábrica de valores de celda para vincular con la propiedad "utilization" de LocationRow
        utilizationCol.setPrefWidth(120); // Establece el ancho preferido de la columna en 120 píxeles
        utilizationCol.setStyle("-fx-alignment: CENTER-RIGHT;"); // Establece la alineación del contenido de la columna al centro-derecha usando CSS
        utilizationCol.setCellFactory(col -> new TableCell<LocationRow, Double>() { // Establece una fábrica de celdas personalizada para formatear y colorear los valores de utilización
            @Override // Anotación que indica que este método sobrescribe el método updateItem de TableCell
            protected void updateItem(Double item, boolean empty) { // Método que actualiza el contenido de la celda recibiendo el valor y si está vacía como parámetros
                super.updateItem(item, empty); // Llama al método updateItem de la clase padre
                if (empty || item == null) { // Condición que verifica si la celda está vacía o el valor es null
                    setText(null); // Establece el texto de la celda como null (vacío)
                    setStyle(""); // Restablece el estilo de la celda a vacío
                } else { // Bloque else que se ejecuta si la celda tiene un valor válido
                    setText(String.format("%.2f%%", item)); // Formatea el valor como string con 2 decimales y símbolo de porcentaje

                    // Colorear según utilización
                    if (item < 50) { // Condición que verifica si la utilización es menor al 50%
                        setStyle("-fx-background-color: #C8E6C9;"); // Verde claro // Establece el fondo de la celda en verde claro para utilización baja
                    } else if (item < 80) { // Condición que verifica si la utilización está entre 50% y 80%
                        setStyle("-fx-background-color: #FFF9C4;"); // Amarillo claro // Establece el fondo de la celda en amarillo claro para utilización media
                    } else { // Bloque else que se ejecuta si la utilización es mayor o igual al 80%
                        setStyle("-fx-background-color: #FFCDD2;"); // Rojo claro // Establece el fondo de la celda en rojo claro para utilización alta
                    } // Cierre del bloque else
                } // Cierre del bloque else externo
            } // Cierre del método updateItem
        }); // Cierre del paréntesis de setCellFactory

        locationTable.getColumns().setAll( // Agrega todas las columnas a la tabla de locaciones en el orden especificado
            Arrays.asList(
                nameCol, capacityCol, entriesCol, timePerEntryCol,
                avgContentCol, maxContentCol, currentContentCol, utilizationCol
            )
        ); // Cierre del paréntesis de setAll

        VBox.setVgrow(locationTable, javafx.scene.layout.Priority.ALWAYS); // Establece que la tabla debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, locationTable); // Agrega la etiqueta de título y la tabla al contenedor VBox

        return container; // Retorna el contenedor VBox con la tabla configurada
    } // Cierre del método createLocationTable

    private VBox createEntityTable() { // Método privado que crea y retorna un VBox conteniendo la tabla de estadísticas de entidades
        VBox container = new VBox(10); // Crea un nuevo VBox con espaciado de 10 píxeles entre elementos
        container.setPadding(new Insets(10)); // Establece un margen interno de 10 píxeles en todos los lados del contenedor

    Label title = new Label("Resumen de Entidades (Piezas)"); // Crea una nueva etiqueta con el título de la tabla
    title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;"); // Presenta un título compacto fuera de la animación

    entityTable = new TableView<>(); // Crea una nueva instancia de TableView parametrizada con EntityRow para almacenar las filas de entidades
    entityTable.setStyle("-fx-font-size: 13px;"); // Ajusta el tamaño del texto de la tabla para mantener la interfaz ligera

        // Columnas
        TableColumn<EntityRow, String> metricCol = new TableColumn<>("Métrica"); // Crea una nueva columna parametrizada con EntityRow y String para mostrar nombres de métricas con el encabezado "Métrica"
        metricCol.setCellValueFactory(new PropertyValueFactory<>("metric")); // Establece la fábrica de valores de celda para vincular con la propiedad "metric" de EntityRow
        metricCol.setPrefWidth(250); // Establece el ancho preferido de la columna en 250 píxeles

        TableColumn<EntityRow, String> valueCol = new TableColumn<>("Valor"); // Crea una nueva columna parametrizada con EntityRow y String para mostrar valores con el encabezado "Valor"
        valueCol.setCellValueFactory(new PropertyValueFactory<>("value")); // Establece la fábrica de valores de celda para vincular con la propiedad "value" de EntityRow
        valueCol.setPrefWidth(200); // Establece el ancho preferido de la columna en 200 píxeles
        valueCol.setStyle("-fx-alignment: CENTER-RIGHT;"); // Establece la alineación del contenido de la columna al centro-derecha usando CSS

        TableColumn<EntityRow, String> unitCol = new TableColumn<>("Unidad"); // Crea una nueva columna parametrizada con EntityRow y String para mostrar unidades con el encabezado "Unidad"
        unitCol.setCellValueFactory(new PropertyValueFactory<>("unit")); // Establece la fábrica de valores de celda para vincular con la propiedad "unit" de EntityRow
        unitCol.setPrefWidth(150); // Establece el ancho preferido de la columna en 150 píxeles
        unitCol.setStyle("-fx-alignment: CENTER;"); // Establece la alineación del contenido de la columna al centro usando CSS

    entityTable.getColumns().setAll(Arrays.asList(metricCol, valueCol, unitCol)); // Agrega las tres columnas a la tabla de entidades en el orden especificado

        VBox.setVgrow(entityTable, javafx.scene.layout.Priority.ALWAYS); // Establece que la tabla debe expandirse verticalmente para ocupar todo el espacio disponible
        container.getChildren().addAll(title, entityTable); // Agrega la etiqueta de título y la tabla al contenedor VBox

        return container; // Retorna el contenedor VBox con la tabla configurada
    } // Cierre del método createEntityTable

    /** // Inicio del comentario Javadoc del método
     * Actualiza las tablas con estadísticas de la simulación // Descripción del método
     */ // Fin del comentario Javadoc
    public void updateStatistics(Statistics stats, double currentTime) { // Método público que actualiza ambas tablas con nuevas estadísticas recibiendo las estadísticas y el tiempo actual como parámetros
        updateLocationTable(stats, currentTime); // Llama al método para actualizar la tabla de locaciones
        updateEntityTable(stats); // Llama al método para actualizar la tabla de entidades
    } // Cierre del método updateStatistics

    private void updateLocationTable(Statistics stats, double currentTime) { // Método privado que actualiza la tabla de locaciones recibiendo las estadísticas y el tiempo actual como parámetros
        ObservableList<LocationRow> data = FXCollections.observableArrayList(); // Crea una nueva lista observable vacía para almacenar las filas de locaciones

        Map<String, Location> locations = stats.getLocations(); // Obtiene el mapa de locaciones desde las estadísticas

        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Bucle for-each que itera sobre cada entrada del mapa de locaciones
            Location loc = entry.getValue(); // Obtiene el objeto Location de la entrada actual

            data.add(new LocationRow( // Crea y agrega una nueva fila de locación a la lista con todos los datos
                loc.getName(), // Parámetro 1: nombre de la locación
                loc.getCapacity(), // Parámetro 2: capacidad de la locación
                loc.getTotalEntries(), // Parámetro 3: total de entradas a la locación
                loc.getAverageTimePerEntry(currentTime), // Parámetro 4: tiempo promedio por entrada
                loc.getAverageContent(currentTime), // Parámetro 5: contenido promedio de la locación
                loc.getCapacity(), // Parámetro 6: contenido máximo (mismo que capacidad)
                loc.getCurrentContent(), // Parámetro 7: contenido actual de la locación
                loc.getUtilization(currentTime) // Parámetro 8: porcentaje de utilización de la locación
            )); // Cierre del paréntesis de add y new LocationRow
        } // Cierre del bucle for-each

        locationTable.setItems(data); // Establece los items de la tabla con la lista de filas creada
    } // Cierre del método updateLocationTable

    private void updateEntityTable(Statistics stats) { // Método privado que actualiza la tabla de entidades recibiendo las estadísticas como parámetro
        ObservableList<EntityRow> data = FXCollections.observableArrayList(); // Crea una nueva lista observable vacía para almacenar las filas de entidades

        data.add(new EntityRow("Total de Arribos", // Agrega una nueva fila con la métrica "Total de Arribos"
            String.valueOf(stats.getTotalArrivals()), "piezas")); // Convierte el total de arribos a String y especifica "piezas" como unidad

        data.add(new EntityRow("Total de Salidas (Completadas)", // Agrega una nueva fila con la métrica "Total de Salidas (Completadas)"
            String.valueOf(stats.getTotalExits()), "piezas")); // Convierte el total de salidas a String y especifica "piezas" como unidad

        data.add(new EntityRow("En Sistema Actualmente", // Agrega una nueva fila con la métrica "En Sistema Actualmente"
            String.valueOf(stats.getTotalArrivals() - stats.getTotalExits()), "piezas")); // Calcula las piezas en sistema restando salidas de arribos y lo convierte a String

        data.add(new EntityRow("Throughput", // Agrega una nueva fila con la métrica "Throughput"
            String.format("%.2f", stats.getThroughput()), "piezas/hora")); // Formatea el throughput con 2 decimales y especifica "piezas/hora" como unidad

        data.add(new EntityRow("", "", "")); // Separador // Agrega una fila vacía como separador visual entre secciones

        data.add(new EntityRow("Tiempo en Sistema - Promedio", // Agrega una nueva fila con la métrica "Tiempo en Sistema - Promedio"
            String.format("%.2f", stats.getAverageSystemTime()), "minutos")); // Formatea el tiempo promedio con 2 decimales y especifica "minutos" como unidad

        data.add(new EntityRow("Tiempo en Sistema - Desviación Estándar", // Agrega una nueva fila con la métrica "Tiempo en Sistema - Desviación Estándar"
            String.format("%.2f", stats.getStdDevSystemTime()), "minutos")); // Formatea la desviación estándar con 2 decimales y especifica "minutos" como unidad

        data.add(new EntityRow("Tiempo en Sistema - Mínimo", // Agrega una nueva fila con la métrica "Tiempo en Sistema - Mínimo"
            String.format("%.2f", stats.getMinSystemTime()), "minutos")); // Formatea el tiempo mínimo con 2 decimales y especifica "minutos" como unidad

        data.add(new EntityRow("Tiempo en Sistema - Máximo", // Agrega una nueva fila con la métrica "Tiempo en Sistema - Máximo"
            String.format("%.2f", stats.getMaxSystemTime()), "minutos")); // Formatea el tiempo máximo con 2 decimales y especifica "minutos" como unidad

        entityTable.setItems(data); // Establece los items de la tabla con la lista de filas creada
    } // Cierre del método updateEntityTable

    /** // Inicio del comentario Javadoc del método
     * Limpia las tablas // Descripción del método
     */ // Fin del comentario Javadoc
    public void clear() { // Método público que limpia ambas tablas eliminando todos sus datos
        locationTable.getItems().clear(); // Limpia todos los items de la tabla de locaciones
        entityTable.getItems().clear(); // Limpia todos los items de la tabla de entidades
    } // Cierre del método clear

    // Clase interna para filas de locaciones
    public static class LocationRow { // Declaración de clase estática pública interna LocationRow que representa una fila de la tabla de locaciones usando JavaFX Properties
        private final StringProperty name; // Variable final que almacena la propiedad de nombre de la locación
        private final IntegerProperty capacity; // Variable final que almacena la propiedad de capacidad de la locación
        private final IntegerProperty totalEntries; // Variable final que almacena la propiedad de total de entradas
        private final DoubleProperty timePerEntry; // Variable final que almacena la propiedad de tiempo por entrada
        private final DoubleProperty avgContent; // Variable final que almacena la propiedad de contenido promedio
        private final IntegerProperty maxContent; // Variable final que almacena la propiedad de contenido máximo
        private final IntegerProperty currentContent; // Variable final que almacena la propiedad de contenido actual
        private final DoubleProperty utilization; // Variable final que almacena la propiedad de utilización

        public LocationRow(String name, int capacity, int totalEntries, // Constructor público que inicializa una fila de locación recibiendo nombre, capacidad y total de entradas como primeros parámetros
                          double timePerEntry, double avgContent, int maxContent, // Continuación de parámetros: tiempo por entrada, contenido promedio y contenido máximo
                          int currentContent, double utilization) { // Últimos parámetros: contenido actual y utilización
            this.name = new SimpleStringProperty(name); // Crea e inicializa una SimpleStringProperty con el nombre recibido
            this.capacity = new SimpleIntegerProperty(capacity == Integer.MAX_VALUE ? -1 : capacity); // Crea e inicializa una SimpleIntegerProperty con la capacidad, convirtiendo Integer.MAX_VALUE a -1
            this.totalEntries = new SimpleIntegerProperty(totalEntries); // Crea e inicializa una SimpleIntegerProperty con el total de entradas recibido
            this.timePerEntry = new SimpleDoubleProperty(timePerEntry); // Crea e inicializa una SimpleDoubleProperty con el tiempo por entrada recibido
            this.avgContent = new SimpleDoubleProperty(avgContent); // Crea e inicializa una SimpleDoubleProperty con el contenido promedio recibido
            this.maxContent = new SimpleIntegerProperty(maxContent == Integer.MAX_VALUE ? -1 : maxContent); // Crea e inicializa una SimpleIntegerProperty con el contenido máximo, convirtiendo Integer.MAX_VALUE a -1
            this.currentContent = new SimpleIntegerProperty(currentContent); // Crea e inicializa una SimpleIntegerProperty con el contenido actual recibido
            this.utilization = new SimpleDoubleProperty(utilization); // Crea e inicializa una SimpleDoubleProperty con la utilización recibida
        } // Cierre del constructor LocationRow

        // Getters para JavaFX Properties
        public String getName() { return name.get(); } // Método público getter que retorna el valor de la propiedad de nombre
        public StringProperty nameProperty() { return name; } // Método público que retorna la propiedad de nombre para binding en JavaFX

        public int getCapacity() { return capacity.get(); } // Método público getter que retorna el valor de la propiedad de capacidad
        public IntegerProperty capacityProperty() { return capacity; } // Método público que retorna la propiedad de capacidad para binding en JavaFX

        public int getTotalEntries() { return totalEntries.get(); } // Método público getter que retorna el valor de la propiedad de total de entradas
        public IntegerProperty totalEntriesProperty() { return totalEntries; } // Método público que retorna la propiedad de total de entradas para binding en JavaFX

        public double getTimePerEntry() { return timePerEntry.get(); } // Método público getter que retorna el valor de la propiedad de tiempo por entrada
        public DoubleProperty timePerEntryProperty() { return timePerEntry; } // Método público que retorna la propiedad de tiempo por entrada para binding en JavaFX

        public double getAvgContent() { return avgContent.get(); } // Método público getter que retorna el valor de la propiedad de contenido promedio
        public DoubleProperty avgContentProperty() { return avgContent; } // Método público que retorna la propiedad de contenido promedio para binding en JavaFX

        public int getMaxContent() { return maxContent.get(); } // Método público getter que retorna el valor de la propiedad de contenido máximo
        public IntegerProperty maxContentProperty() { return maxContent; } // Método público que retorna la propiedad de contenido máximo para binding en JavaFX

        public int getCurrentContent() { return currentContent.get(); } // Método público getter que retorna el valor de la propiedad de contenido actual
        public IntegerProperty currentContentProperty() { return currentContent; } // Método público que retorna la propiedad de contenido actual para binding en JavaFX

        public double getUtilization() { return utilization.get(); } // Método público getter que retorna el valor de la propiedad de utilización
        public DoubleProperty utilizationProperty() { return utilization; } // Método público que retorna la propiedad de utilización para binding en JavaFX
    } // Cierre de la clase LocationRow

    // Clase interna para filas de entidades
    public static class EntityRow { // Declaración de clase estática pública interna EntityRow que representa una fila de la tabla de entidades usando JavaFX Properties
        private final StringProperty metric; // Variable final que almacena la propiedad del nombre de la métrica
        private final StringProperty value; // Variable final que almacena la propiedad del valor de la métrica
        private final StringProperty unit; // Variable final que almacena la propiedad de la unidad de la métrica

        public EntityRow(String metric, String value, String unit) { // Constructor público que inicializa una fila de entidad recibiendo métrica, valor y unidad como parámetros
            this.metric = new SimpleStringProperty(metric); // Crea e inicializa una SimpleStringProperty con la métrica recibida
            this.value = new SimpleStringProperty(value); // Crea e inicializa una SimpleStringProperty con el valor recibido
            this.unit = new SimpleStringProperty(unit); // Crea e inicializa una SimpleStringProperty con la unidad recibida
        } // Cierre del constructor EntityRow

        public String getMetric() { return metric.get(); } // Método público getter que retorna el valor de la propiedad de métrica
        public StringProperty metricProperty() { return metric; } // Método público que retorna la propiedad de métrica para binding en JavaFX

        public String getValue() { return value.get(); } // Método público getter que retorna el valor de la propiedad de valor
        public StringProperty valueProperty() { return value; } // Método público que retorna la propiedad de valor para binding en JavaFX

        public String getUnit() { return unit.get(); } // Método público getter que retorna el valor de la propiedad de unidad
        public StringProperty unitProperty() { return unit; } // Método público que retorna la propiedad de unidad para binding en JavaFX
    } // Cierre de la clase EntityRow
} // Cierre de la clase ResultsTablePanel
