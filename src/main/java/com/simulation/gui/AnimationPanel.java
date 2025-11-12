package com.simulation.gui;

import com.simulation.core.Entity;
import com.simulation.core.DigemicEngine; // Motor DIGEMIC
import com.simulation.resources.Location;
import javafx.scene.canvas.Canvas; // Importa la clase Canvas de JavaFX para dibujar gráficos 2D
import javafx.scene.canvas.GraphicsContext; // Importa la clase GraphicsContext de JavaFX para realizar operaciones de dibujo en el canvas
import javafx.scene.image.Image; // Permite utilizar imágenes personalizadas para las locaciones
import javafx.scene.layout.Pane; // Importa la clase Pane de JavaFX para crear un contenedor de layout
import javafx.scene.paint.Color; // Importa la clase Color de JavaFX para definir colores
import javafx.scene.text.Font; // Importa la clase Font de JavaFX para definir fuentes de texto
import javafx.scene.text.FontWeight; // Importa la enumeración FontWeight de JavaFX para especificar el grosor de la fuente
import javafx.scene.text.TextAlignment; // Importa la enumeración TextAlignment de JavaFX para especificar la alineación del texto

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*; // Importa todas las clases del paquete util de Java (List, Map, ArrayList, HashMap, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Panel MEJORADO - Muestra TODAS las locaciones aunque no existan en el motor // Descripción de la clase indicando que dibuja todas las locaciones configuradas
 */ // Fin del comentario Javadoc
public class AnimationPanel extends Pane {
    private Canvas canvas;
    private DigemicEngine engine; // Motor DIGEMIC

    private static final double WIDTH = 1200; // MODIFICADO: Reducido de 1600 a 1200
    private static final double HEIGHT = 900; // MODIFICADO: Reducido de 1250 a 900
    private static final double BOX_SIZE = 140; // MODIFICADO: Aumentado de 120 a 140 para mejor visibilidad
    private static final double COUNTER_WIDTH = 210;
    private static final double COUNTER_HEIGHT = 86;
    private static final double COUNTER_START_X = 950; // MODIFICADO: Ajustado a nuevo ancho
    private static final double COUNTER_START_Y = 80;

    private Map<String, double[]> locationPositions;
    private Map<String, Color> locationColors;
    private Map<String, String> locationIcons;
    private Map<String, Image> locationImages; // Permite usar imágenes personalizadas si existen

    private List<VirtualTransit> virtualTransits;
    private Map<Integer, String> visualLocations; // Locaciones visibles (pueden diferir de la real durante tránsito)
    private Set<Integer> activeTransitEntities; // Entidades actualmente en animación de tránsito
    private double gearRotation = 0;
    
    // NUEVO: Variables para control de zoom
    private double zoomLevel = 1.0;
    private static final double MIN_ZOOM = 0.5;
    private static final double MAX_ZOOM = 2.0;
    private static final double ZOOM_STEP = 0.1;

    public AnimationPanel(DigemicEngine engine) { // Constructor recibe DigemicEngine
        this.engine = engine;
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.locationPositions = new HashMap<>();
        this.locationColors = new HashMap<>();
        this.locationIcons = new HashMap<>();
        this.locationImages = new HashMap<>();
    this.virtualTransits = new ArrayList<>();
    this.visualLocations = new HashMap<>(); // Inicializar tracking de locaciones visibles
    this.activeTransitEntities = new HashSet<>(); // Inicializar set de tránsito

        initializePositions();
        initializeColors();
        initializeIcons();
        initializeImages();

        getChildren().add(canvas);
        setMinSize(WIDTH, HEIGHT); // Establece el tamaño mínimo del panel con el ancho y altura definidos
        setPrefSize(WIDTH, HEIGHT); // Establece el tamaño preferido del panel con el ancho y altura definidos
        
        // NUEVO: Configurar zoom con scroll del mouse
        setupZoomControls();
    } // Cierre del constructor AnimationPanel
    
    // NUEVO: Método para configurar controles de zoom
    private void setupZoomControls() {
        canvas.setOnScroll(event -> {
            if (event.isControlDown()) {
                double delta = event.getDeltaY();
                if (delta > 0) {
                    zoomIn();
                } else {
                    zoomOut();
                }
                event.consume();
            }
        });
    }
    
    // NUEVO: Métodos de zoom
    public void zoomIn() {
        if (zoomLevel < MAX_ZOOM) {
            zoomLevel += ZOOM_STEP;
            canvas.setScaleX(zoomLevel);
            canvas.setScaleY(zoomLevel);
        }
    }
    
    public void zoomOut() {
        if (zoomLevel > MIN_ZOOM) {
            zoomLevel -= ZOOM_STEP;
            canvas.setScaleX(zoomLevel);
            canvas.setScaleY(zoomLevel);
        }
    }
    
    public void resetZoom() {
        zoomLevel = 1.0;
        canvas.setScaleX(1.0);
        canvas.setScaleY(1.0);
    }

    // === MÉTODOS HELPER PARA ACCESO AL MOTOR ===
    
    private Location getLocationFromEngine(String name) {
        return engine.getLocation(name);
    }

    private double getCurrentTimeFromEngine() {
        return engine.getCurrentTime();
    }

    private List<Entity> getAllActiveEntitiesFromEngine() {
        return engine.getAllActiveEntities();
    }

    private com.simulation.statistics.Statistics getStatisticsFromEngine() {
        return engine.getStatistics();
    }


    private void initializePositions() { // Método privado que inicializa las posiciones X-Y de cada locación DIGEMIC en el canvas
        // Layout de oficina de pasaportes optimizado para mejor visualización
        
        // Columna izquierda: Entrada y Zona de Formas
        locationPositions.put("ENTRADA", new double[]{60, 120}); // Puerta de entrada (arriba izquierda)
        locationPositions.put("ZONA_FORMAS", new double[]{60, 340}); // Área de llenado de formularios (abajo de entrada)
        
        // Columna central: Áreas de espera
        locationPositions.put("SALA_SILLAS", new double[]{340, 120}); // Sala con sillas (centro arriba) - 40 capacidad
        locationPositions.put("SALA_DE_PIE", new double[]{340, 340}); // Área de pie (centro abajo) - sin límite
        
        // Columna derecha: Ventanillas de servicio
        locationPositions.put("SERVIDOR_1", new double[]{620, 160}); // Primera ventanilla (derecha arriba)
        locationPositions.put("SERVIDOR_2", new double[]{620, 340}); // Segunda ventanilla (derecha abajo)
    } // Cierre del método initializePositions

    private void initializeColors() { // Método privado que inicializa los colores representativos de todas las locaciones DIGEMIC
        locationColors.put("ENTRADA", Color.rgb(76, 175, 80)); // Verde para entrada
        locationColors.put("ZONA_FORMAS", Color.rgb(255, 193, 7)); // Amarillo para zona de formularios
        locationColors.put("SALA_SILLAS", Color.rgb(33, 150, 243)); // Azul para sala con sillas
        locationColors.put("SALA_DE_PIE", Color.rgb(156, 39, 176)); // Morado para área de pie
        locationColors.put("SERVIDOR_1", Color.rgb(244, 67, 54)); // Rojo para servidor 1
        locationColors.put("SERVIDOR_2", Color.rgb(244, 67, 54)); // Rojo para servidor 2
    } // Cierre del método initializeColors

    private void initializeIcons() { // Método privado que inicializa los iconos emoji de todas las locaciones DIGEMIC
        locationIcons.put("ENTRADA", "🚪"); // Puerta de entrada
        locationIcons.put("ZONA_FORMAS", "📝"); // Formularios
        locationIcons.put("SALA_SILLAS", "�"); // Sillas
        locationIcons.put("SALA_DE_PIE", "🧍"); // Personas de pie
        locationIcons.put("SERVIDOR_1", "�"); // Servidor/ventanilla 1
        locationIcons.put("SERVIDOR_2", "�"); // Servidor/ventanilla 2
    } // Cierre del método initializeIcons

    public void render() { // Método público que renderiza (dibuja) toda la animación en el canvas
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Obtiene el contexto gráfico 2D del canvas para realizar operaciones de dibujo

        gc.setFill(Color.rgb(240, 242, 245)); // Establece el color de relleno como gris muy claro para el fondo
        gc.fillRect(0, 0, WIDTH, HEIGHT); // Dibuja un rectángulo de fondo que cubre todo el canvas

        drawTitle(gc); // Llama al método para dibujar el título del sistema
        drawConnections(gc); // Llama al método para dibujar las conexiones (flechas) entre locaciones
        drawAllLocations(gc); // Llama al método para dibujar todas las 6 locaciones DIGEMIC
        drawCounters(gc); // Llama al método para dibujar los contadores de estadísticas de cada locación

    detectVirtualTransits(); // Actualiza las animaciones de tránsito en curso
    drawStationaryEntities(gc); // Dibuja las entidades que están esperando en cada locación
    drawVirtualTransitEntities(gc); // Dibuja las entidades que se están moviendo entre locaciones

        drawGlobalInfo(gc); // Llama al método para dibujar el panel de información global del sistema

        gearRotation += 0.05; // Incrementa el ángulo de rotación para animaciones
        if (gearRotation > 2 * Math.PI) { // Condición que verifica si el ángulo de rotación excede 2π (360 grados)
            gearRotation = 0; // Reinicia el ángulo de rotación a 0 para comenzar de nuevo
        } // Cierre del bloque condicional if
    } // Cierre del método render

    private void drawTitle(GraphicsContext gc) { // Método privado que dibuja el título principal del sistema recibiendo el contexto gráfico como parámetro
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno como gris muy oscuro para el título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26)); // Establece la fuente como Arial, negrita, tamaño 26
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText("🛂 DIGEMIC - SISTEMA DE EXPEDICIÓN DE PASAPORTES", WIDTH / 2, 35); // Dibuja el título centrado en la parte superior del canvas

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece la fuente como Arial, normal, tamaño 14
        gc.setFill(Color.rgb(100, 100, 100)); // Establece el color de relleno como gris medio para el subtítulo
        gc.fillText("Entrada(10%→Formas,90%→Sillas/Pie) | Formas→Sillas/Pie | Pie→Sillas→Servidor1/2", WIDTH / 2, 60); // Dibuja el subtítulo describiendo el flujo del proceso
    } // Cierre del método drawTitle

    private void drawConnections(GraphicsContext gc) { // Método privado que dibuja todas las conexiones (líneas y flechas) entre locaciones DIGEMIC
        gc.setStroke(Color.rgb(100, 150, 200)); // Color azul suave para las conexiones
        gc.setLineWidth(3); // Establece el grosor de la línea en 3 píxeles
        gc.setLineDashes(5, 5); // Establece un patrón de línea discontinua con segmentos de 5 píxeles

        // ENTRADA: 10% a ZONA_FORMAS, 90% intenta SALA_SILLAS (si llena → SALA_DE_PIE)
        drawConnectionVertical(gc, "ENTRADA", "ZONA_FORMAS"); // 10% van a llenar formularios
        drawConnection(gc, "ENTRADA", "SALA_SILLAS"); // 90% intentan ir a sillas primero
        drawConnectionDiagonal(gc, "ENTRADA", "SALA_DE_PIE"); // Si sillas llena, van a pie
        
        // ZONA_FORMAS: intenta SALA_SILLAS primero, si llena → SALA_DE_PIE
        drawConnection(gc, "ZONA_FORMAS", "SALA_SILLAS"); // Intenta sillas
        drawConnectionDiagonal(gc, "ZONA_FORMAS", "SALA_DE_PIE"); // Si llena, va a pie
        
        // SALA_DE_PIE: espera y se mueve a SALA_SILLAS cuando hay espacio
        drawConnectionVertical(gc, "SALA_DE_PIE", "SALA_SILLAS"); // Espera a que haya sillas disponibles
        
        // SALA_SILLAS: SOLO desde aquí van a servidores
        drawConnection(gc, "SALA_SILLAS", "SERVIDOR_1"); // A servidor 1 (FIRST disponible)
        drawConnectionDiagonal(gc, "SALA_SILLAS", "SERVIDOR_2"); // A servidor 2 (FIRST disponible)

        gc.setLineDashes(null); // Restablece el patrón de línea a sólida (sin discontinuidades)
    } // Cierre del método drawConnections

    private void drawConnection(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión horizontal entre dos locaciones recibiendo el contexto gráfico y nombres de locaciones como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE; // Calcula el punto X de salida en el borde derecho de la caja origen
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula el punto Y de salida en el centro vertical de la caja origen
        double x2 = pos2[0]; // Calcula el punto X de llegada en el borde izquierdo de la caja destino
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula el punto Y de llegada en el centro vertical de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea de conexión entre los dos puntos
        drawArrow(gc, x1, y1, x2, y2); // Dibuja una flecha en el extremo final de la línea
    } // Cierre del método drawConnection

    private void drawConnectionVertical(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión vertical entre dos locaciones recibiendo el contexto gráfico y nombres de locaciones como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE / 2; // Calcula el punto X de salida en el centro horizontal de la caja origen
        double y1 = pos1[1] + BOX_SIZE; // Calcula el punto Y de salida en el borde inferior de la caja origen
        double x2 = pos2[0] + BOX_SIZE / 2; // Calcula el punto X de llegada en el centro horizontal de la caja destino
        double y2 = pos2[1]; // Calcula el punto Y de llegada en el borde superior de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea de conexión vertical entre los dos puntos
        drawArrow(gc, x2, y2 - 20, x2, y2); // Dibuja una flecha vertical cerca del punto de llegada
    } // Cierre del método drawConnectionVertical

    private void drawConnectionDiagonal(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión diagonal entre dos locaciones recibiendo el contexto gráfico y nombres de locaciones como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE; // Calcula el punto X de salida en el borde derecho de la caja origen
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula el punto Y de salida en el centro vertical de la caja origen
        double x2 = pos2[0]; // Calcula el punto X de llegada en el borde izquierdo de la caja destino
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula el punto Y de llegada en el centro vertical de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea diagonal de conexión entre los dos puntos
        drawArrow(gc, x1 + (x2 - x1) * 0.8, y1 + (y2 - y1) * 0.8, x2, y2); // Dibuja una flecha en el 80% del recorrido hacia el destino
    } // Cierre del método drawConnectionDiagonal

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) { // Método privado que dibuja una punta de flecha recibiendo el contexto gráfico y coordenadas como parámetros
        double arrowLength = 12; // Define la longitud de la punta de flecha en píxeles
        double angle = Math.atan2(y2 - y1, x2 - x1); // Calcula el ángulo de la línea usando arcotangente

        double x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6); // Calcula la coordenada X del primer punto de la punta de flecha
        double y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6); // Calcula la coordenada Y del primer punto de la punta de flecha
        double x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6); // Calcula la coordenada X del segundo punto de la punta de flecha
        double y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6); // Calcula la coordenada Y del segundo punto de la punta de flecha

        gc.setFill(Color.rgb(120, 120, 140)); // Establece el color de relleno como gris azulado para la punta de flecha
        gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3); // Dibuja un triángulo relleno como punta de flecha
    } // Cierre del método drawArrow

    private void drawAllLocations(GraphicsContext gc) { // Método privado que dibuja todas las 6 locaciones DIGEMIC recibiendo el contexto gráfico como parámetro
        // DIBUJAR TODAS las locaciones DIGEMIC
        String[] allLocations = { // Define un array con los nombres de todas las 6 locaciones DIGEMIC
            "ENTRADA", "ZONA_FORMAS", // Entrada y zona de formularios
            "SALA_SILLAS", "SALA_DE_PIE", // Áreas de espera
            "SERVIDOR_1", "SERVIDOR_2" // Ventanillas de atención
        }; // Cierre de la declaración del array

        for (String name : allLocations) {
            Location location = getLocationFromEngine(name); // Usar método helper
            // SIEMPRE dibujar, aunque location sea null
            drawLocationSafe(gc, name, location);
        }
    } // Cierre del método drawAllLocations

    /** // Inicio del comentario Javadoc del método
     * Versión SEGURA que dibuja la locación aunque sea null // Descripción del método
     */ // Fin del comentario Javadoc
    private void drawLocationSafe(GraphicsContext gc, String name, Location location) { // Método privado que dibuja una locación de forma segura recibiendo el contexto gráfico, nombre de la locación y objeto Location como parámetros
        double[] pos = locationPositions.get(name); // Obtiene la posición [x, y] de la locación desde el mapa de posiciones
        if (pos == null) return; // Si la posición es null, sale del método prematuramente

        Color color = locationColors.get(name); // Obtiene el color de la locación desde el mapa de colores
        String icon = locationIcons.get(name); // Obtiene el icono de la locación desde el mapa de iconos

        // Si location es null, usar valores por defecto
    int currentContent = getVisualContent(name);
    int capacity = location != null ? location.getCapacity() : Integer.MAX_VALUE; // Obtiene la capacidad de la locación, o Integer.MAX_VALUE si es null
    int queueSize = location != null ? location.getQueueSize() : 0; // Obtiene el tamaño de la cola de la locación, o 0 si es null

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.2)); // Establece el color de relleno como negro semitransparente para la sombra
        gc.fillRoundRect(pos[0] + 5, pos[1] + 5, BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja un rectángulo redondeado desplazado como sombra

        // Caja principal
        gc.setFill(color); // Establece el color de relleno con el color específico de la locación
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el rectángulo redondeado principal de la locación

        gc.setStroke(color.darker()); // Establece el color de trazo como una versión más oscura del color de la locación
        gc.setLineWidth(4); // Establece el grosor del borde en 4 píxeles
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el borde del rectángulo redondeado

        Image locationImage = locationImages.get(name);
        if (locationImage != null) {
            drawLocationImage(gc, pos[0], pos[1], locationImage);
        } else {
            gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
            gc.setFill(Color.WHITE); // Establece el color de relleno como blanco para el icono
            gc.setFont(Font.font("Segoe UI Emoji", 40)); // Establece la fuente como Segoe UI Emoji, tamaño 40 para los emojis
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE * 0.58); // Dibuja el emoji centrado en la caja
        }

        // Nombre
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno como gris muy oscuro para el nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // Establece la fuente como Arial, negrita, tamaño 14

        String displayName = getDisplayName(name); // Obtiene el nombre a mostrar llamando al método que formatea nombres
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 12); // Dibuja el nombre centrado arriba de la caja

        // Capacidad
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24)); // Establece la fuente como Arial, negrita, tamaño 24 para el contador
        gc.setFill(Color.WHITE); // Establece el color de relleno como blanco para el contador
        String contentText = currentContent + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity); // Crea el texto del contador
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE - 12); // Dibuja el contador centrado en la parte inferior de la caja

        // Cola
        if (queueSize > 0) { // Condición que verifica si hay entidades en cola
            drawQueueIndicator(gc, pos[0], pos[1], queueSize); // Dibuja el indicador de cola con el tamaño
        } // Cierre del bloque condicional if

        // Barra de utilización
    double utilization = location != null ? location.getUtilization(getCurrentTimeFromEngine()) : 0; // Usar método helper
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization);

        if (name.startsWith("SERVIDOR")) {
            drawServerBatchProgress(gc, pos[0], pos[1], name);
        }

    } // Cierre del método drawLocationSafe

    private String getDisplayName(String name) { // Método privado que retorna un nombre formateado para mostrar recibiendo el nombre interno como parámetro
        switch (name) { // Switch que determina qué nombre formateado retornar basado en el nombre interno
            case "ENTRADA": return "ENTRADA"; // Puerta de entrada
            case "ZONA_FORMAS": return "ZONA FORMAS"; // Área de llenado de formularios
            case "SALA_SILLAS": return "SALA SILLAS"; // Sala de espera con sillas
            case "SALA_DE_PIE": return "SALA DE PIE"; // Área para esperar de pie
            case "SERVIDOR_1": return "SERVIDOR 1"; // Primera ventanilla
            case "SERVIDOR_2": return "SERVIDOR 2"; // Segunda ventanilla
            default: return name; // Para todos los demás casos, retorna el nombre sin modificar
        } // Cierre del switch
    } // Cierre del método getDisplayName

    private void drawQueueIndicator(GraphicsContext gc, double x, double y, int queueSize) { // Método privado que dibuja un indicador circular rojo mostrando el tamaño de la cola recibiendo el contexto gráfico, posición y tamaño de cola como parámetros
        double badgeX = x + BOX_SIZE - 48; // Calcula la posición X del badge en la esquina superior derecha de la caja
        double badgeY = y - 16; // Calcula la posición Y del badge ligeramente arriba de la caja
        double badgeSize = 44; // Define el tamaño del badge circular en píxeles

        gc.setFill(Color.rgb(244, 67, 54)); // Establece el color de relleno como rojo para el badge de cola
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja un círculo rojo como fondo del badge

        gc.setStroke(Color.WHITE); // Establece el color de trazo como blanco para el borde del badge
        gc.setLineWidth(3); // Establece el grosor del borde en 3 píxeles
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja el borde blanco del círculo

        gc.setFill(Color.WHITE); // Establece el color de relleno como blanco para el número
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Establece la fuente como Arial, negrita, tamaño 18
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText(String.valueOf(queueSize), badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 7); // Dibuja el número del tamaño de cola centrado en el badge
    } // Cierre del método drawQueueIndicator

    private void drawUtilizationBar(GraphicsContext gc, double x, double y, double width, double utilization) { // Método privado que dibuja una barra de progreso mostrando la utilización recibiendo el contexto gráfico, posición, ancho y porcentaje de utilización como parámetros
        double barHeight = 10; // Define la altura de la barra en píxeles

        gc.setFill(Color.rgb(220, 220, 220)); // Establece el color de relleno como gris claro para el fondo de la barra
        gc.fillRoundRect(x, y, width, barHeight, 5, 5); // Dibuja un rectángulo redondeado como fondo de la barra

        double fillWidth = width * (utilization / 100.0); // Calcula el ancho del relleno proporcional al porcentaje de utilización

        Color fillColor; // Declara la variable para el color del relleno
        if (utilization < 50) fillColor = Color.rgb(76, 175, 80); // Si la utilización es menor al 50%, usa verde
        else if (utilization < 80) fillColor = Color.rgb(255, 193, 7); // Si la utilización está entre 50% y 80%, usa amarillo
        else fillColor = Color.rgb(244, 67, 54); // Si la utilización es mayor o igual al 80%, usa rojo

        gc.setFill(fillColor); // Establece el color de relleno calculado
        gc.fillRoundRect(x, y, fillWidth, barHeight, 5, 5); // Dibuja el rectángulo redondeado de progreso con el ancho calculado

        gc.setFill(Color.rgb(60, 60, 60)); // Establece el color de relleno como gris oscuro para el texto del porcentaje
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Establece la fuente como Arial, negrita, tamaño 11
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText(String.format("%.0f%%", utilization), x + width / 2, y + barHeight + 15); // Dibuja el porcentaje centrado debajo de la barra
    } // Cierre del método drawUtilizationBar


    private void drawCounters(GraphicsContext gc) { // Método privado que dibuja los contadores de estadísticas de todas las locaciones DIGEMIC
        double startX = COUNTER_START_X; // Establece la posición X inicial usando la constante definida
        double startY = COUNTER_START_Y; // Establece la posición Y inicial usando la constante definida
        double spacing = COUNTER_HEIGHT + 6; // Calcula el espaciado vertical entre contadores sumando la altura más 6 píxeles

        String[] locations = { // Define un array con los nombres de todas las 6 locaciones DIGEMIC
            "ENTRADA", "ZONA_FORMAS", // Entrada y zona de formularios
            "SALA_SILLAS", "SALA_DE_PIE", // Áreas de espera
            "SERVIDOR_1", "SERVIDOR_2" // Ventanillas de atención
        }; // Cierre de la declaración del array

        for (int i = 0; i < locations.length; i++) {
            Location loc = getLocationFromEngine(locations[i]); // Usar método helper
            // SIEMPRE dibujar contador, aunque loc sea null
            drawCounterSafe(gc, startX, startY + i * spacing, locations[i], loc);
        }
    } // Cierre del método drawCounters

    private void drawCounterSafe(GraphicsContext gc, double x, double y, String name, Location location) { // Método privado que dibuja un contador de estadísticas de forma segura recibiendo el contexto gráfico, posición, nombre y objeto Location como parámetros
        gc.setFill(Color.rgb(0, 0, 0, 0.12)); // Establece el color de relleno como negro muy transparente para la sombra del contador
        gc.fillRoundRect(x + 2, y + 2, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja un rectángulo redondeado desplazado como sombra

        gc.setFill(Color.rgb(255, 255, 255, 0.98)); // Establece el color de relleno como blanco casi opaco para el fondo del contador
        gc.fillRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja el rectángulo redondeado principal del contador

        gc.setStroke(locationColors.get(name)); // Establece el color de trazo usando el color específico de la locación
        gc.setLineWidth(3); // Establece el grosor del borde en 3 píxeles
        gc.strokeRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja el borde del contador con el color de la locación

        gc.setFill(Color.rgb(30, 30, 30)); // Establece el color de relleno como gris muy oscuro para el texto del nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Establece la fuente como Arial, negrita, tamaño 16
        gc.setTextAlign(TextAlignment.LEFT); // Establece la alineación del texto a la izquierda

        String displayName = getDisplayName(name); // Obtiene el nombre formateado para mostrar
        if (displayName.length() > 15) { // Condición que verifica si el nombre es demasiado largo (más de 15 caracteres)
            displayName = displayName.substring(0, 13) + ".."; // Trunca el nombre a 13 caracteres y agrega ".." al final
        } // Cierre del bloque condicional if
        gc.fillText(displayName, x + 12, y + 24); // Dibuja el nombre con un margen de 12 píxeles desde el borde izquierdo

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece la fuente como Arial, normal, tamaño 14 para las estadísticas
        gc.setFill(Color.rgb(50, 50, 50)); // Establece el color de relleno como gris oscuro para el texto de estadísticas

        int entries = location != null ? location.getTotalEntries() : 0;
        gc.fillText("Entradas: " + entries, x + 12, y + 46);

        double util = location != null ? location.getUtilization(getCurrentTimeFromEngine()) : 0; // Usar método helper
        gc.fillText(String.format("Utilización: %.0f%%", util), x + 12, y + 65);

        int queue = location != null ? location.getQueueSize() : 0;
        gc.fillText("Cola: " + queue, x + 130, y + 46);

        double avgContent = location != null ? location.getAverageContent(getCurrentTimeFromEngine()) : 0; // Usar método helper
        gc.fillText(String.format("Prom: %.1f", avgContent), x + 130, y + 65);

        double barWidth = COUNTER_WIDTH - 24; // Calcula el ancho de la barra de utilización restando los márgenes
        double barHeight = 8; // Define la altura de la barra en píxeles
        double barY = y + COUNTER_HEIGHT - 17; // Calcula la posición Y de la barra en la parte inferior del contador

        gc.setFill(Color.rgb(220, 220, 220)); // Establece el color de relleno como gris claro para el fondo de la barra
        gc.fillRoundRect(x + 12, barY, barWidth, barHeight, 4, 4); // Dibuja el fondo de la barra

        double fillWidth = barWidth * (util / 100.0); // Calcula el ancho del relleno proporcional a la utilización
        Color barColor; // Declara la variable para el color de la barra
        if (util < 40) barColor = Color.rgb(76, 175, 80); // Si la utilización es menor al 40%, usa verde
        else if (util < 80) barColor = Color.rgb(255, 152, 0); // Si la utilización está entre 40% y 80%, usa naranja
        else barColor = Color.rgb(244, 67, 54); // Si la utilización es mayor o igual al 80%, usa rojo

        gc.setFill(barColor); // Establece el color de relleno calculado
        gc.fillRoundRect(x + 12, barY, fillWidth, barHeight, 4, 4); // Dibuja la barra de progreso con el ancho y color calculados
    } // Cierre del método drawCounterSafe

    private void drawLocationImage(GraphicsContext gc, double baseX, double baseY, Image image) {
        if (image == null || image.isError()) {
            return;
        }

        double availableWidth = BOX_SIZE * 0.7;
        double availableHeight = BOX_SIZE * 0.65;
        double scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight());
        scale = Math.min(scale, 1.2); // Evitar escalados exagerados

        double drawWidth = image.getWidth() * scale;
        double drawHeight = image.getHeight() * scale;
        double drawX = baseX + (BOX_SIZE - drawWidth) / 2.0;
        double drawY = baseY + (BOX_SIZE - drawHeight) / 2.0 - 6; // Ajuste ligero para dejar espacio al contador

        gc.drawImage(image, drawX, drawY, drawWidth, drawHeight);
    }

    private void drawServerBatchProgress(GraphicsContext gc, double baseX, double baseY, String serverName) {
        int target = engine.getServerBatchTarget();
        if (target <= 0) {
            return;
        }

        int progress = engine.getServerBatchProgress(serverName);
        boolean paused = engine.isServerPaused(serverName);

        int columns = 5;
        int rows = (int) Math.ceil((double) target / columns);
        double bubbleSize = 12;
        double bubbleSpacing = 5;
        double panelPadding = 6;

        double panelWidth = columns * bubbleSize + (columns - 1) * bubbleSpacing + panelPadding * 2;
        double panelHeight = rows * bubbleSize + (rows - 1) * bubbleSpacing + panelPadding * 2;

        double panelX = baseX + BOX_SIZE + 14;
        double panelY = baseY + (BOX_SIZE - panelHeight) / 2.0;

        Color baseColor = locationColors.getOrDefault(serverName, Color.rgb(244, 67, 54));

        gc.setFill(Color.rgb(255, 255, 255, 0.9));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        gc.setStroke(baseColor.darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10);

        for (int index = 0; index < target; index++) {
            int row = index / columns;
            int col = index % columns;

            double centerX = panelX + panelPadding + col * (bubbleSize + bubbleSpacing) + bubbleSize / 2.0;
            double centerY = panelY + panelPadding + row * (bubbleSize + bubbleSpacing) + bubbleSize / 2.0;

            boolean filled = index < progress;

            Color fillColor;
            if (filled) {
                fillColor = paused ? Color.rgb(255, 214, 102) : baseColor;
            } else {
                fillColor = Color.rgb(189, 189, 189, 0.6);
            }

            gc.setFill(fillColor);
            gc.fillOval(centerX - bubbleSize / 2.0, centerY - bubbleSize / 2.0, bubbleSize, bubbleSize);

            gc.setStroke(filled ? baseColor.darker() : Color.rgb(158, 158, 158));
            gc.setLineWidth(1.5);
            gc.strokeOval(centerX - bubbleSize / 2.0, centerY - bubbleSize / 2.0, bubbleSize, bubbleSize);
        }
    }

    private void initializeImages() {
        loadLocationImageFromResource("ENTRADA", "/images/entrada.png");
        loadLocationImageFromResource("ZONA_FORMAS", "/images/zona_formas.png");
        loadLocationImageFromResource("SALA_SILLAS", "/images/sala_sillas.png");
        loadLocationImageFromResource("SALA_DE_PIE", "/images/sala_de_pie.png");
        loadLocationImageFromResource("SERVIDOR_1", "/images/servidor_1.png");
        loadLocationImageFromResource("SERVIDOR_2", "/images/servidor_2.png");
    }

    private void loadLocationImageFromResource(String locationName, String resourcePath) {
        if (locationName == null || resourcePath == null) {
            return;
        }

        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return;
            }
            Image image = new Image(stream);
            if (!image.isError()) {
                locationImages.put(locationName, image);
            }
        } catch (IOException ignored) {
            // Silenciar: la imagen personalizada es opcional
        }
    }

    public boolean setLocationImage(String locationName, Image image) {
        if (locationName == null || image == null || image.isError()) {
            return false;
        }
        locationImages.put(locationName, image);
        return true;
    }

    public boolean setLocationImageFromResource(String locationName, String resourcePath) {
        if (locationName == null || resourcePath == null) {
            return false;
        }
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) {
            if (stream == null) {
                return false;
            }
            Image image = new Image(stream);
            if (image.isError()) {
                return false;
            }
            locationImages.put(locationName, image);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public boolean setLocationImageFromFile(String locationName, String filePath) {
        if (locationName == null || filePath == null) {
            return false;
        }
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            return false;
        }
        try (InputStream stream = new FileInputStream(file)) {
            Image image = new Image(stream);
            if (image.isError()) {
                return false;
            }
            locationImages.put(locationName, image);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public void clearLocationImage(String locationName) {
        if (locationName != null) {
            locationImages.remove(locationName);
        }
    }

    private int getVisualContent(String locationName) {
        int count = 0;
        for (Map.Entry<Integer, String> entry : visualLocations.entrySet()) {
            if (locationName.equals(entry.getValue()) && !activeTransitEntities.contains(entry.getKey())) {
                count++;
            }
        }
        return count;
    }

    private void detectVirtualTransits() {
        double currentSimTime = getCurrentTimeFromEngine();

        // Actualizar transiciones existentes en función del tiempo de simulación
        Iterator<VirtualTransit> iterator = virtualTransits.iterator();
        while (iterator.hasNext()) {
            VirtualTransit vt = iterator.next();
            double elapsed = currentSimTime - vt.startTime;
            double transitDuration = vt.estimatedDuration > 0 ? vt.estimatedDuration : 0.1;
            vt.progress = Math.min(1.0, elapsed / transitDuration);

            if (vt.progress >= 1.0) {
                iterator.remove();
                activeTransitEntities.remove(vt.entityId);
                visualLocations.put(vt.entityId, vt.to);
            }
        }

        List<Entity> allEntities = getAllActiveEntitiesFromEngine();
        if (allEntities == null) {
            return;
        }

        for (Entity entity : allEntities) {
            if (entity == null) {
                continue;
            }

            String currentLoc = entity.getCurrentLocation();
            if (currentLoc == null || currentLoc.isEmpty()) {
                visualLocations.remove(entity.getId());
                activeTransitEntities.remove(entity.getId());
                continue;
            }

            int entityId = entity.getId();
            String lastLoc = visualLocations.get(entityId);

            if (lastLoc != null && !lastLoc.equals(currentLoc)) {
                boolean alreadyTransiting = activeTransitEntities.contains(entityId);
                if (!alreadyTransiting
                        && locationPositions.containsKey(lastLoc)
                        && locationPositions.containsKey(currentLoc)) {
                    double distance = calculateDistance(lastLoc, currentLoc);
                    double duration = estimateTransitDuration(distance, lastLoc, currentLoc);

                    VirtualTransit vt = new VirtualTransit(entityId, lastLoc, currentLoc);
                    vt.startTime = currentSimTime;
                    vt.estimatedDuration = duration;
                    vt.progress = 0.0;

                    virtualTransits.add(vt);
                    activeTransitEntities.add(entityId);
                }
            }

            visualLocations.putIfAbsent(entityId, currentLoc);
            if (!activeTransitEntities.contains(entityId) && lastLoc == null) {
                visualLocations.put(entityId, currentLoc);
            }
        }
    }
    
    private double calculateDistance(String from, String to) {
        double[] fromPos = locationPositions.get(from);
        double[] toPos = locationPositions.get(to);
        
        if (fromPos == null || toPos == null) return 100;
        
        double dx = toPos[0] - fromPos[0];
        double dy = toPos[1] - fromPos[1];
        return Math.sqrt(dx * dx + dy * dy);
    }
    
    private double estimateTransitDuration(double distance, String from, String to) {
        double baseDuration;

        if (from.equals("ENTRADA") && to.equals("ZONA_FORMAS")) {
            baseDuration = 1.8;
        } else if (from.equals("ENTRADA") && (to.equals("SALA_SILLAS") || to.equals("SALA_DE_PIE"))) {
            baseDuration = 1.5;
        } else if (from.equals("ZONA_FORMAS")) {
            baseDuration = 1.3;
        } else if (from.equals("SALA_DE_PIE") && to.equals("SALA_SILLAS")) {
            baseDuration = 1.0;
        } else if (from.equals("SALA_SILLAS") && (to.equals("SERVIDOR_1") || to.equals("SERVIDOR_2"))) {
            baseDuration = 1.6;
        } else {
            baseDuration = 0.8 + (distance / 320.0);
        }

        double slowFactor = computeAnimationSlowFactor();
        return baseDuration * slowFactor;
    }

    private double computeAnimationSlowFactor() {
        double currentSpeed = engine.getSimulationSpeed();
        if (currentSpeed <= 0) {
            return 12.0;
        }
        double factor = 18.0 / currentSpeed;
        if (factor < 2.5) {
            factor = 2.5;
        } else if (factor > 12.0) {
            factor = 12.0;
        }
        return factor;
    }

    private void drawStationaryEntities(GraphicsContext gc) {
        List<Entity> allEntities = getAllActiveEntitiesFromEngine();
        if (allEntities == null) {
            return;
        }

        Map<String, List<Entity>> grouped = new HashMap<>();
        for (Entity entity : allEntities) {
            if (entity == null) {
                continue;
            }
            int entityId = entity.getId();
            if (activeTransitEntities.contains(entityId)) {
                continue; // Se está animando el tránsito, no dibujar como estacionario
            }

            String currentLoc = entity.getCurrentLocation();
            if (currentLoc == null || currentLoc.isEmpty()) {
                visualLocations.remove(entityId);
                continue;
            }

            String visualLoc = visualLocations.computeIfAbsent(entityId, id -> currentLoc);
            grouped.computeIfAbsent(visualLoc, key -> new ArrayList<>()).add(entity);
        }

        for (Map.Entry<String, List<Entity>> entry : grouped.entrySet()) {
            List<Entity> entities = entry.getValue();
            entities.sort(Comparator.comparingInt(Entity::getId));
            drawEntitiesForLocation(gc, entry.getKey(), entities);
        }
    }

    private void drawEntitiesForLocation(GraphicsContext gc, String location, List<Entity> entities) {
        double[] basePos = locationPositions.get(location);
        if (basePos == null || entities.isEmpty()) {
            return;
        }

        int columns = getColumnsForLocation(location);
        int rows = Math.max(1, (int) Math.ceil((double) entities.size() / columns));

        double padding = 18;
        double availableWidth = BOX_SIZE - padding * 2;
        double availableHeight = BOX_SIZE - padding * 2 - 16; // Dejar espacio para el contador inferior
        if (location.startsWith("SERVIDOR")) {
            availableHeight = BOX_SIZE - padding * 2;
        } else if ("SALA_DE_PIE".equals(location)) {
            availableHeight = BOX_SIZE - padding * 1.5;
        }

        double cellWidth = availableWidth / Math.max(1, columns);
        double cellHeight = availableHeight / Math.max(1, rows);

        for (int index = 0; index < entities.size(); index++) {
            int row = index / columns;
            int col = index % columns;

            double centerX = basePos[0] + padding + col * cellWidth + cellWidth / 2;
            double centerY = basePos[1] + padding + row * cellHeight + cellHeight / 2;

            drawStationaryEntity(gc, centerX, centerY, entities.get(index), location);
        }
    }

    private int getColumnsForLocation(String location) {
        switch (location) {
            case "ENTRADA":
                return 3;
            case "ZONA_FORMAS":
                return 4;
            case "SALA_SILLAS":
                return 8;
            case "SALA_DE_PIE":
                return 12;
            case "SERVIDOR_1":
            case "SERVIDOR_2":
                return 1;
            default:
                return 4;
        }
    }

    private void drawStationaryEntity(GraphicsContext gc, double centerX, double centerY, Entity entity, String location) {
        double baseSize;
        if (location.startsWith("SERVIDOR")) {
            baseSize = 26;
        } else if ("SALA_SILLAS".equals(location)) {
            baseSize = 18;
        } else if ("SALA_DE_PIE".equals(location)) {
            baseSize = 14;
        } else {
            baseSize = 16;
        }

        double size = baseSize;

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.25));
        gc.fillOval(centerX - size / 2 + 2, centerY - size / 2 + 2, size, size);

        boolean blocked = entity.isBlocked();
        Color fillColor;
        if (location.startsWith("SERVIDOR")) {
            fillColor = Color.rgb(255, 214, 102);
        } else if (blocked) {
            fillColor = Color.rgb(255, 111, 0);
        } else if ("ZONA_FORMAS".equals(location)) {
            fillColor = Color.rgb(255, 213, 79);
        } else if ("ENTRADA".equals(location)) {
            fillColor = Color.rgb(129, 199, 132);
        } else {
            fillColor = Color.rgb(33, 150, 243);
        }

        gc.setFill(fillColor);
        gc.fillOval(centerX - size / 2, centerY - size / 2, size, size);

        Color borderColor = blocked ? Color.rgb(183, 28, 28) : Color.rgb(25, 118, 210);
        gc.setStroke(borderColor);
        gc.setLineWidth(2);
        gc.strokeOval(centerX - size / 2, centerY - size / 2, size, size);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(entity.getId()), centerX, centerY + 3);
    }

    private void drawVirtualTransitEntities(GraphicsContext gc) {
        for (VirtualTransit vt : virtualTransits) {
            double[] fromPos = getLocationExitPoint(vt.from);
            double[] toPos = getLocationEntryPoint(vt.to);

            if (fromPos != null && toPos != null) {
                // Interpolación ease-in-out para movimiento más natural
                double t = vt.progress;
                double smoothProgress = t < 0.5 
                    ? 2 * t * t 
                    : 1 - Math.pow(-2 * t + 2, 2) / 2;
                
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * smoothProgress;
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * smoothProgress;
                
                Color baseColor = locationColors.getOrDefault(vt.to, Color.rgb(255, 215, 0));
                drawMovingPiece(gc, x, y, vt.entityId, baseColor);
            }
        }
    }

    private double[] getLocationExitPoint(String location) {
        double[] pos = locationPositions.get(location);
        if (pos == null) return null;

        // Puntos de salida para DIGEMIC
        if (location.equals("ENTRADA")) {
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE}; // Sale por abajo
        }
        if (location.equals("ZONA_FORMAS")) {
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Sale por la derecha
        }
        if (location.equals("SALA_DE_PIE")) {
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Sale por arriba
        }
        if (location.equals("SALA_SILLAS")) {
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Sale por la derecha
        }
        
        // Por defecto, sale por la derecha
        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2};
    }

    private double[] getLocationEntryPoint(String location) {
        double[] pos = locationPositions.get(location);
        if (pos == null) return null;

        // Puntos de entrada para DIGEMIC
        if (location.equals("ZONA_FORMAS")) {
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Entra por arriba
        }
        if (location.equals("SALA_SILLAS")) {
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entra por la izquierda
        }
        if (location.equals("SALA_DE_PIE")) {
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entra por la izquierda
        }
        if (location.equals("SERVIDOR_1") || location.equals("SERVIDOR_2")) {
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entran por la izquierda
        }
        
        // Por defecto, entra por la izquierda
        return new double[]{pos[0], pos[1] + BOX_SIZE / 2};
    }

    private void drawMovingPiece(GraphicsContext gc, double x, double y, int entityId, Color baseColor) {
        double pieceSize = 28; // Tamaño grande y visible
        
        // Efecto de pulsación suave
        double pulseEffect = Math.sin(gearRotation * 2) * 0.1 + 1.0;
        double actualSize = pieceSize * pulseEffect;

        // Triple halo para efecto de profundidad
        for (int i = 3; i >= 1; i--) {
            double haloSize = actualSize * (1.2 + i * 0.3);
            double alpha = 0.15 / i;
            gc.setFill(Color.rgb(255, 215, 0, alpha));
            gc.fillOval(x - haloSize/2, y - haloSize/2, haloSize, haloSize);
        }

        // Sombra profunda
        gc.setFill(Color.rgb(0, 0, 0, 0.5));
        gc.fillOval(x - actualSize/2 + 4, y - actualSize/2 + 4, actualSize, actualSize);

        // Cuerpo principal con gradiente simulado (círculo base)
        gc.setFill(Color.rgb(255, 215, 0)); // Dorado brillante
        gc.fillOval(x - actualSize/2, y - actualSize/2, actualSize, actualSize);
        
        // Highlight superior (simula luz)
        gc.setFill(Color.rgb(255, 255, 200, 0.6));
        gc.fillOval(x - actualSize/3, y - actualSize/3, actualSize/2, actualSize/2);

        // Borde exterior grueso y definido
        gc.setStroke(Color.rgb(204, 140, 0));
        gc.setLineWidth(3.5);
        gc.strokeOval(x - actualSize/2, y - actualSize/2, actualSize, actualSize);
        
        // Borde interior para más contraste
        gc.setStroke(Color.rgb(255, 240, 150));
        gc.setLineWidth(1.5);
        gc.strokeOval(x - actualSize/2 + 2, y - actualSize/2 + 2, actualSize - 4, actualSize - 4);
        
        // Trail effect (estela de movimiento)
        gc.setFill(Color.rgb(255, 215, 0, 0.2));
        gc.fillOval(x - actualSize, y - actualSize, actualSize * 2, actualSize * 2);
        
        // Centro indicador
        gc.setFill(Color.WHITE);
        double centerDot = actualSize / 5;
        gc.fillOval(x - centerDot/2, y - centerDot/2, centerDot, centerDot);
    }

    private void drawGlobalInfo(GraphicsContext gc) { // Método privado que dibuja el panel de información global del sistema recibiendo el contexto gráfico como parámetro
        double infoX = 50; // Define la posición X del panel de información
        double infoY = 800; // Define la posición Y del panel de información
        double infoWidth = 600; // Define el ancho del panel en píxeles
        double infoHeight = 130; // Define la altura del panel en píxeles

        gc.setFill(Color.rgb(255, 255, 255, 0.98)); // Establece el color de relleno como blanco casi opaco para el fondo del panel
        gc.fillRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja el rectángulo redondeado del panel

        gc.setStroke(Color.rgb(200, 200, 200)); // Establece el color de trazo como gris claro para el borde
        gc.setLineWidth(2); // Establece el grosor del borde en 2 píxeles
        gc.strokeRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja el borde del panel

        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno como gris muy oscuro para el texto del título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Establece la fuente como Arial, negrita, tamaño 16
        gc.setTextAlign(TextAlignment.LEFT); // Establece la alineación del texto a la izquierda
        gc.fillText("📊 Estadísticas en Tiempo Real", infoX + 15, infoY + 30); // Dibuja el título del panel con un emoji

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        double currentTime = getCurrentTimeFromEngine(); // Usar método helper
        int totalMinutes = (int) Math.floor(currentTime);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;

        gc.fillText(String.format("⏱ Tiempo: %02d:%02d h", hours, minutes),
                infoX + 15, infoY + 60);

        int totalArrivals = getStatisticsFromEngine().getTotalArrivals(); // Usar método helper
        gc.fillText("📥 Arribos: " + totalArrivals, infoX + 15, infoY + 85);

        int totalExits = getStatisticsFromEngine().getTotalExits(); // Usar método helper
        gc.fillText("📤 Completadas: " + totalExits, infoX + 250, infoY + 60);

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0; // Calcula el throughput en piezas por hora, o 0 si no hay tiempo
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput), // Dibuja el throughput formateado con 2 decimales
                infoX + 250, infoY + 85); // en la columna derecha

        int inSystem = totalArrivals - totalExits; // Calcula las piezas actualmente en el sistema restando salidas de arribos
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 250, infoY + 110); // Dibuja el texto de piezas en sistema en la columna derecha
    } // Cierre del método drawGlobalInfo

    public void reset() { // Método público que reinicia el panel de animación a su estado inicial sin recibir parámetros
        virtualTransits.clear(); // Limpia la lista de tránsitos virtuales
        visualLocations.clear();
        activeTransitEntities.clear();
        gearRotation = 0; // Reinicia el ángulo de rotación de engranes a 0
        resetZoom(); // NUEVO: Reinicia el zoom
        render(); // Llama al método render para redibujar el canvas limpio
    } // Cierre del método reset
    
    // NUEVO: Método público para agregar tránsito virtual
    public void addVirtualTransit(int entityId, String from, String to) {
        if (from != null && to != null && !from.equals(to)) {
            virtualTransits.add(new VirtualTransit(entityId, from, to));
        }
    }

    private static class VirtualTransit {
        int entityId;
        String from;
        String to;
        double progress;
        double startTime; // Tiempo de simulación cuando inició el tránsito
        double estimatedDuration; // Duración estimada en minutos de simulación

        VirtualTransit(int entityId, String from, String to) {
            this.entityId = entityId;
            this.from = from;
            this.to = to;
            this.progress = 0;
            this.startTime = 0;
            this.estimatedDuration = 0.2; // Valor por defecto
        }
    }
}
