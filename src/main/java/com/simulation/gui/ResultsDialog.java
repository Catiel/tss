package com.simulation.gui; // Declaración del paquete de la interfaz gráfica de usuario

import com.simulation.resources.Location; // Importa la clase Location que representa las locaciones del sistema
import com.simulation.statistics.Statistics; // Importa la clase Statistics que almacena las estadísticas de la simulación
import javafx.geometry.Insets; // Importa Insets para definir márgenes y espaciados internos
import javafx.geometry.Pos; // Importa Pos para definir posiciones y alineaciones de elementos
import javafx.scene.Scene; // Importa Scene que representa el contenedor de elementos visuales
import javafx.scene.control.Button; // Importa Button para crear botones
import javafx.scene.control.Label; // Importa Label para crear etiquetas de texto
import javafx.scene.layout.GridPane; // Importa GridPane para organizar elementos en cuadrícula
import javafx.scene.layout.VBox; // Importa VBox para organizar elementos verticalmente
import javafx.stage.Modality; // Importa Modality para definir el comportamiento modal de ventanas
import javafx.stage.Stage; // Importa Stage que representa una ventana de la aplicación

/**
 * Diálogo que muestra los resultados finales de la simulación DIGEMIC
 * según los incisos requeridos (a-e)
 */
public class ResultsDialog { // Declaración de la clase pública ResultsDialog que muestra resultados finales
    private final Statistics stats; // Variable final que almacena las estadísticas de la simulación
    private final double currentTime; // Variable final que almacena el tiempo actual de la simulación en minutos

    public ResultsDialog(Statistics stats, double currentTime) { // Constructor que recibe las estadísticas y el tiempo actual
        this.stats = stats; // Asigna las estadísticas recibidas a la variable de instancia
        this.currentTime = currentTime; // Asigna el tiempo actual recibido a la variable de instancia
    }

    public void show() { // Método público que crea y muestra el diálogo de resultados
        Stage dialog = new Stage(); // Crea un nuevo Stage (ventana) para el diálogo
        dialog.initModality(Modality.APPLICATION_MODAL); // Establece el diálogo como modal bloqueando la ventana principal
        dialog.setTitle("Resultados de la Simulación DIGEMIC"); // Establece el título de la ventana del diálogo

        VBox mainLayout = new VBox(20); // Crea un VBox con espaciado vertical de 20 píxeles entre elementos
        mainLayout.setPadding(new Insets(25)); // Establece padding de 25 píxeles en todos los lados del VBox
        mainLayout.setAlignment(Pos.TOP_CENTER); // Establece la alineación de elementos al centro superior

        Label titleLabel = new Label("RESULTADOS FINALES - SISTEMA DIGEMIC"); // Crea etiqueta con el título principal
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"); // Aplica estilo: tamaño 18px, negrita, color gris oscuro

        int totalMinutes = (int) Math.floor(currentTime); // Convierte el tiempo actual a minutos enteros redondeando hacia abajo
        int hours = totalMinutes / 60; // Calcula las horas dividiendo minutos entre 60
        int minutes = totalMinutes % 60; // Calcula los minutos residuales usando módulo
        Label durationLabel = new Label(String.format("Duración: %02d:%02d horas (%d minutos)", hours, minutes, totalMinutes)); // Crea etiqueta con duración formateada
        durationLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #34495e;"); // Aplica estilo: tamaño 14px, color gris medio

        GridPane resultsGrid = new GridPane(); // Crea un GridPane para organizar los resultados principales
        resultsGrid.setHgap(15); // Establece espaciado horizontal de 15 píxeles entre columnas
        resultsGrid.setVgap(15); // Establece espaciado vertical de 15 píxeles entre filas
        resultsGrid.setPadding(new Insets(20, 0, 20, 0)); // Establece padding: 20 arriba, 0 lados, 20 abajo
        resultsGrid.setStyle("-fx-background-color: #ecf0f1; -fx-background-radius: 10px; -fx-padding: 20px;"); // Aplica estilo: fondo gris claro, bordes redondeados, padding interno

        double avgWaitTime = stats.getAverageWaitTime(); // Obtiene el tiempo promedio de espera desde las estadísticas
        double avgSystemTime = stats.getAverageSystemTime(); // Obtiene el tiempo promedio en sistema desde las estadísticas
        double avgProcessTime = stats.getAverageProcessTime(); // Obtiene el tiempo promedio de procesamiento desde las estadísticas
        
        double avgSeatTime = 0.0; // Inicializa variable para tiempo promedio en sillas en 0.0
        Location salaSillas = stats.getLocation("SALA_SILLAS"); // Obtiene la locación SALA_SILLAS desde las estadísticas
        if (salaSillas != null) { // Verifica si la locación existe (no es null)
            avgSeatTime = salaSillas.getAverageTimePerEntry(currentTime); // Obtiene el tiempo promedio por entrada en sala de sillas
        }

        double avgSeated = stats.getAverageSeated(currentTime); // Obtiene el número promedio de personas sentadas
        double avgStanding = stats.getAverageStanding(currentTime); // Obtiene el número promedio de personas de pie
        int maxWaitingArea = stats.getMaxWaitingArea(); // Obtiene el número máximo de personas en área de espera
        
        double utilizationServidor1 = stats.getLocation("SERVIDOR_1") != null  // Verifica si SERVIDOR_1 existe
            ? stats.getLocation("SERVIDOR_1").getUtilization(currentTime)  // Si existe, obtiene su utilización
            : 0.0; // Si no existe, asigna 0.0
        
        double utilizationServidor2 = stats.getLocation("SERVIDOR_2") != null  // Verifica si SERVIDOR_2 existe
            ? stats.getLocation("SERVIDOR_2").getUtilization(currentTime)  // Si existe, obtiene su utilización
            : 0.0; // Si no existe, asigna 0.0

        addResultRow(resultsGrid, 0, "a)", "Tiempo promedio de espera en la fila.",  // Agrega fila para inciso a) en posición 0
            String.format("%.2f minutos", avgSeatTime), "#3498db"); // Formatea tiempo con 2 decimales, color azul

        addResultRow(resultsGrid, 1, "b)", "Número promedio de personas sentadas.",  // Agrega fila para inciso b) en posición 1
            String.format("%.2f personas", avgSeated), "#27ae60"); // Formatea promedio con 2 decimales, color verde

        addResultRow(resultsGrid, 2, "c)", "Número promedio de personas de pie.",  // Agrega fila para inciso c) en posición 2
            String.format("%.2f personas", avgStanding), "#f39c12"); // Formatea promedio con 2 decimales, color naranja

        addResultRow(resultsGrid, 3, "d)", "Número máximo de personas en la sala de espera.",  // Agrega fila para inciso d) en posición 3
            String.format("%d personas", maxWaitingArea), "#e74c3c"); // Formatea máximo como entero, color rojo

        String utilizationText = String.format("Servidor 1: %.2f%%  |  Servidor 2: %.2f%%",  // Formatea utilizaciones de ambos servidores con 2 decimales
            utilizationServidor1, utilizationServidor2); // Inserta los valores de utilización calculados
        addResultRow(resultsGrid, 4, "e)", "Utilización de los servidores.",  // Agrega fila para inciso e) en posición 4
            utilizationText, "#9b59b6"); // Usa el texto formateado con ambas utilizaciones, color morado

        Label additionalLabel = new Label("Estadísticas Adicionales:"); // Crea etiqueta para sección de estadísticas adicionales
        additionalLabel.setStyle("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: #2c3e50; -fx-padding: 10px 0 5px 0;"); // Aplica estilo: tamaño 14px, negrita, color oscuro, padding vertical

        GridPane additionalGrid = new GridPane(); // Crea un GridPane para organizar las estadísticas adicionales
        additionalGrid.setHgap(15); // Establece espaciado horizontal de 15 píxeles entre columnas
        additionalGrid.setVgap(10); // Establece espaciado vertical de 10 píxeles entre filas
        additionalGrid.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8px; -fx-padding: 15px;"); // Aplica estilo: fondo gris muy claro, bordes redondeados, padding

        addSimpleRow(additionalGrid, 0, "Total de Arribos:", String.format("%d clientes", stats.getTotalArrivals())); // Agrega fila con total de arribos formateado
        addSimpleRow(additionalGrid, 1, "Total de Salidas:", String.format("%d clientes", stats.getTotalExits())); // Agrega fila con total de salidas formateado
        addSimpleRow(additionalGrid, 2, "Clientes en Sistema:", String.format("%d clientes",  // Agrega fila con clientes actuales en sistema
            stats.getTotalArrivals() - stats.getTotalExits())); // Calcula clientes en sistema restando salidas de arribos
        addSimpleRow(additionalGrid, 3, "Throughput:", String.format("%.2f clientes/hora", stats.getThroughput())); // Agrega fila con throughput formateado con 2 decimales
        addSimpleRow(additionalGrid, 4, "Tiempo promedio en sistema:", String.format("%.2f minutos", avgSystemTime)); // Agrega fila con tiempo promedio en sistema formateado
        addSimpleRow(additionalGrid, 5, "Tiempo promedio en operación:", String.format("%.2f minutos", avgProcessTime)); // Agrega fila con tiempo promedio de procesamiento formateado
        addSimpleRow(additionalGrid, 6, "Tiempo promedio esperando:", String.format("%.2f minutos", avgWaitTime)); // Agrega fila con tiempo promedio de espera formateado

        Button closeButton = new Button("Cerrar"); // Crea botón con texto "Cerrar"
        closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px;"); // Aplica estilo: tamaño 14px, padding, fondo azul, texto blanco, bordes redondeados
        closeButton.setOnAction(e -> dialog.close()); // Establece acción al hacer clic: cerrar el diálogo
        closeButton.setOnMouseEntered(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #2980b9; -fx-text-fill: white; -fx-background-radius: 5px; -fx-cursor: hand;")); // Aplica estilo hover: fondo azul más oscuro, cursor mano
        closeButton.setOnMouseExited(e -> closeButton.setStyle("-fx-font-size: 14px; -fx-padding: 10px 40px; -fx-background-color: #3498db; -fx-text-fill: white; -fx-background-radius: 5px;")); // Restaura estilo original cuando el mouse sale

        mainLayout.getChildren().addAll( // Agrega todos los elementos al layout principal en orden vertical
            titleLabel, // Agrega título principal
            durationLabel, // Agrega etiqueta de duración
            resultsGrid, // Agrega grid con resultados principales (incisos a-e)
            additionalLabel, // Agrega etiqueta de estadísticas adicionales
            additionalGrid, // Agrega grid con estadísticas adicionales
            closeButton // Agrega botón de cerrar
        );

        Scene scene = new Scene(mainLayout, 760, 660); // Crea una nueva escena con el layout principal y dimensiones 760x660 píxeles
        dialog.setScene(scene); // Establece la escena creada en el stage del diálogo
        dialog.setResizable(false); // Establece que el diálogo no pueda ser redimensionado
        dialog.show(); // Muestra el diálogo al usuario
    }

    private void addResultRow(GridPane grid, int row, String letter, String description, String value, String color) { // Método privado que agrega una fila de resultado con letra, descripción y valor coloreado
        Label letterLabel = new Label(letter); // Crea etiqueta con la letra del inciso (a, b, c, d, e)
        letterLabel.setStyle(String.format("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: %s;", color)); // Aplica estilo: tamaño 16px, negrita, color personalizado
        letterLabel.setMinWidth(32); // Establece ancho mínimo de 32 píxeles para la etiqueta
        letterLabel.setPrefWidth(32); // Establece ancho preferido de 32 píxeles para la etiqueta
        letterLabel.setAlignment(Pos.CENTER_RIGHT); // Establece alineación del texto a la derecha centrado verticalmente

        Label descLabel = new Label(description); // Crea etiqueta con la descripción del resultado
        descLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"); // Aplica estilo: tamaño 13px, negrita, color gris oscuro
        descLabel.setWrapText(true); // Habilita el ajuste automático de línea si el texto es muy largo
        descLabel.setMaxWidth(360); // Establece ancho máximo de 360 píxeles para la descripción
        GridPane.setHgrow(descLabel, javafx.scene.layout.Priority.ALWAYS); // Establece que la etiqueta crezca horizontalmente con prioridad ALWAYS

        Label valueLabel = new Label(value); // Crea etiqueta con el valor del resultado
        valueLabel.setStyle(String.format("-fx-font-size: 14px; -fx-font-weight: bold; -fx-text-fill: %s;", color)); // Aplica estilo: tamaño 14px, negrita, color personalizado
        valueLabel.setMinWidth(150); // Establece ancho mínimo de 150 píxeles para el valor
        valueLabel.setMaxWidth(Double.MAX_VALUE); // Establece ancho máximo ilimitado (puede crecer según necesidad)
        valueLabel.setWrapText(true); // Habilita el ajuste automático de línea si el valor es muy largo
        valueLabel.setAlignment(Pos.CENTER_RIGHT); // Establece alineación del texto a la derecha centrado verticalmente

        grid.add(letterLabel, 0, row); // Agrega la etiqueta de letra en la columna 0 de la fila especificada
        grid.add(descLabel, 1, row); // Agrega la etiqueta de descripción en la columna 1 de la fila especificada
        grid.add(valueLabel, 2, row); // Agrega la etiqueta de valor en la columna 2 de la fila especificada
    }

    private void addSimpleRow(GridPane grid, int row, String label, String value) { // Método privado que agrega una fila simple con etiqueta y valor para estadísticas adicionales
        Label labelNode = new Label(label); // Crea etiqueta con el texto de la etiqueta
        labelNode.setStyle("-fx-font-size: 12px; -fx-text-fill: #34495e;"); // Aplica estilo: tamaño 12px, color gris medio

        Label valueNode = new Label(value); // Crea etiqueta con el valor correspondiente
        valueNode.setStyle("-fx-font-size: 12px; -fx-font-weight: bold; -fx-text-fill: #2c3e50;"); // Aplica estilo: tamaño 12px, negrita, color gris oscuro

        grid.add(labelNode, 0, row); // Agrega la etiqueta en la columna 0 de la fila especificada
        grid.add(valueNode, 1, row); // Agrega el valor en la columna 1 de la fila especificada
    }
}
