package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.core.Entity; // Importa la clase Entity para acceder a las entidades que fluyen por el sistema
import com.simulation.core.SimulationEngine; // Importa la clase SimulationEngine para acceder al motor de simulación y sus datos
import com.simulation.resources.InspectionStation; // Importa la clase InspectionStation para acceder a estaciones de inspección especializadas
import com.simulation.resources.Location; // Importa la clase Location para acceder a las locaciones del sistema
import javafx.scene.canvas.Canvas; // Importa la clase Canvas de JavaFX para dibujar gráficos 2D
import javafx.scene.canvas.GraphicsContext; // Importa la clase GraphicsContext de JavaFX para realizar operaciones de dibujo en el canvas
import javafx.scene.layout.Pane; // Importa la clase Pane de JavaFX para crear un contenedor de layout
import javafx.scene.paint.Color; // Importa la clase Color de JavaFX para definir colores
import javafx.scene.text.Font; // Importa la clase Font de JavaFX para definir fuentes de texto
import javafx.scene.text.FontWeight; // Importa la enumeración FontWeight de JavaFX para especificar el grosor de la fuente
import javafx.scene.text.TextAlignment; // Importa la enumeración TextAlignment de JavaFX para especificar la alineación del texto

import java.util.*; // Importa todas las clases del paquete util de Java (List, Map, ArrayList, HashMap, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Panel MEJORADO - Muestra TODAS las locaciones aunque no existan en el motor // Descripción de la clase indicando que dibuja todas las locaciones configuradas
 */ // Fin del comentario Javadoc
public class AnimationPanel extends Pane { // Declaración de la clase pública AnimationPanel que extiende Pane para ser un panel que muestra la animación visual del sistema
    private Canvas canvas; // Variable privada que almacena el lienzo (canvas) sobre el cual se dibuja la animación
    private SimulationEngine engine; // Variable privada que almacena la referencia al motor de simulación para acceder a sus datos

    private static final double WIDTH = 1600; // Constante estática final que define el ancho del canvas en píxeles
    private static final double HEIGHT = 1250; // Constante estática final que define la altura del canvas en píxeles
    private static final double BOX_SIZE = 120; // Constante estática final que define el tamaño de las cajas que representan locaciones en píxeles
    private static final double COUNTER_WIDTH = 210; // Constante estática final que define el ancho de los contadores de estadísticas en píxeles
    private static final double COUNTER_HEIGHT = 86; // Constante estática final que define la altura de los contadores de estadísticas en píxeles
    private static final double COUNTER_START_X = 1300; // Constante estática final que define la posición X inicial de los contadores en píxeles
    private static final double COUNTER_START_Y = 80; // Constante estática final que define la posición Y inicial de los contadores en píxeles

    private Map<String, double[]> locationPositions; // Variable privada que almacena un mapa de nombres de locaciones a sus posiciones [x, y] en el canvas
    private Map<String, Color> locationColors; // Variable privada que almacena un mapa de nombres de locaciones a sus colores representativos
    private Map<String, String> locationIcons; // Variable privada que almacena un mapa de nombres de locaciones a sus iconos emoji

    private List<VirtualTransit> virtualTransits; // Variable privada que almacena una lista de tránsitos virtuales para animaciones suaves
    private double gearRotation = 0; // Variable privada que almacena el ángulo de rotación actual de los engranes para animación, inicializada en 0

    public AnimationPanel(SimulationEngine engine) { // Constructor público que inicializa el panel de animación recibiendo el motor de simulación como parámetro
        this.engine = engine; // Asigna el motor de simulación recibido a la variable de instancia
        this.canvas = new Canvas(WIDTH, HEIGHT); // Crea un nuevo canvas con el ancho y altura definidos
        this.locationPositions = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar las posiciones de las locaciones
        this.locationColors = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar los colores de las locaciones
        this.locationIcons = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar los iconos de las locaciones
        this.virtualTransits = new ArrayList<>(); // Crea una nueva ArrayList vacía para almacenar los tránsitos virtuales

        initializePositions(); // Llama al método para inicializar las posiciones de todas las locaciones en el canvas
        initializeColors(); // Llama al método para inicializar los colores de todas las locaciones
        initializeIcons(); // Llama al método para inicializar los iconos de todas las locaciones

        getChildren().add(canvas); // Agrega el canvas como hijo de este Pane para que sea visible
        setMinSize(WIDTH, HEIGHT); // Establece el tamaño mínimo del panel con el ancho y altura definidos
        setPrefSize(WIDTH, HEIGHT); // Establece el tamaño preferido del panel con el ancho y altura definidos
        setMaxSize(WIDTH, HEIGHT); // Establece el tamaño máximo del panel con el ancho y altura definidos
    } // Cierre del constructor AnimationPanel

    private void initializePositions() { // Método privado que inicializa las posiciones [x, y] de todas las 12 locaciones en el canvas
        double y1 = 100; // Define la coordenada Y de la primera fila de locaciones
        double spacing = 220; // Define el espaciado horizontal entre locaciones en píxeles

        locationPositions.put("CONVEYOR_1", new double[]{60, y1}); // Establece la posición del primer conveyor en [60, 100]
        locationPositions.put("ALMACEN", new double[]{60 + spacing, y1}); // Establece la posición del almacén en [280, 100]
        locationPositions.put("CORTADORA", new double[]{60 + spacing * 2, y1}); // Establece la posición de la cortadora en [500, 100]
        locationPositions.put("TORNO", new double[]{60 + spacing * 3, y1}); // Establece la posición del torno en [720, 100]

        double y2 = 320; // Define la coordenada Y de la segunda fila de locaciones
        locationPositions.put("CONVEYOR_2", new double[]{60 + spacing * 3, y2}); // Establece la posición del segundo conveyor en [720, 320]
        locationPositions.put("FRESADORA", new double[]{60 + spacing * 2, y2}); // Establece la posición de la fresadora en [500, 320]
        locationPositions.put("ALMACEN_2", new double[]{60 + spacing, y2}); // Establece la posición del segundo almacén en [280, 320]
        locationPositions.put("PINTURA", new double[]{60, y2}); // Establece la posición de pintura en [60, 320]

        double y3 = 540; // Define la coordenada Y de la tercera fila de locaciones
        locationPositions.put("INSPECCION_1", new double[]{60, y3}); // Establece la posición de la primera inspección en [60, 540]

        double y4 = 680; // Define la coordenada Y de la cuarta fila de locaciones
        locationPositions.put("INSPECCION_2", new double[]{60, y4}); // Establece la posición de la segunda inspección en [60, 680]

        locationPositions.put("EMPAQUE", new double[]{330, 610}); // Establece la posición de empaque en [330, 610]
        locationPositions.put("EMBARQUE", new double[]{600, 610}); // Establece la posición de embarque en [600, 610]
    } // Cierre del método initializePositions

    private void initializeColors() { // Método privado que inicializa los colores representativos de todas las locaciones
        locationColors.put("CONVEYOR_1", Color.rgb(96, 125, 139)); // Establece el color del primer conveyor como gris azulado
        locationColors.put("ALMACEN", Color.rgb(255, 241, 118)); // Establece el color del almacén como amarillo claro
        locationColors.put("CORTADORA", Color.rgb(239, 83, 80)); // Establece el color de la cortadora como rojo
        locationColors.put("TORNO", Color.rgb(129, 199, 132)); // Establece el color del torno como verde
        locationColors.put("CONVEYOR_2", Color.rgb(96, 125, 139)); // Establece el color del segundo conveyor como gris azulado
        locationColors.put("FRESADORA", Color.rgb(156, 39, 176)); // Establece el color de la fresadora como morado
        locationColors.put("ALMACEN_2", Color.rgb(255, 241, 118)); // Establece el color del segundo almacén como amarillo claro
        locationColors.put("PINTURA", Color.rgb(255, 167, 38)); // Establece el color de pintura como naranja
        locationColors.put("INSPECCION_1", Color.rgb(189, 189, 189)); // Establece el color de la primera inspección como gris
        locationColors.put("INSPECCION_2", Color.rgb(189, 189, 189)); // Establece el color de la segunda inspección como gris
        locationColors.put("EMPAQUE", Color.rgb(121, 85, 72)); // Establece el color de empaque como marrón
        locationColors.put("EMBARQUE", Color.rgb(33, 150, 243)); // Establece el color de embarque como azul
    } // Cierre del método initializeColors

    private void initializeIcons() { // Método privado que inicializa los iconos emoji de todas las locaciones
        locationIcons.put("CONVEYOR_1", "→"); // Establece el icono del primer conveyor como flecha derecha
        locationIcons.put("ALMACEN", "📦"); // Establece el icono del almacén como caja
        locationIcons.put("CORTADORA", "✂"); // Establece el icono de la cortadora como tijeras
        locationIcons.put("TORNO", "⚙"); // Establece el icono del torno como engrane
        locationIcons.put("CONVEYOR_2", "→"); // Establece el icono del segundo conveyor como flecha derecha
        locationIcons.put("FRESADORA", "🔧"); // Establece el icono de la fresadora como llave inglesa
        locationIcons.put("ALMACEN_2", "📦"); // Establece el icono del segundo almacén como caja
        locationIcons.put("PINTURA", "🎨"); // Establece el icono de pintura como paleta de pintor
        locationIcons.put("INSPECCION_1", "🔍"); // Establece el icono de la primera inspección como lupa
        locationIcons.put("INSPECCION_2", "🔍"); // Establece el icono de la segunda inspección como lupa
        locationIcons.put("EMPAQUE", "📦"); // Establece el icono de empaque como caja
        locationIcons.put("EMBARQUE", "🚚"); // Establece el icono de embarque como camión
    } // Cierre del método initializeIcons

    public void render() { // Método público que renderiza (dibuja) toda la animación en el canvas
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Obtiene el contexto gráfico 2D del canvas para realizar operaciones de dibujo

        gc.setFill(Color.rgb(240, 242, 245)); // Establece el color de relleno como gris muy claro para el fondo
        gc.fillRect(0, 0, WIDTH, HEIGHT); // Dibuja un rectángulo de fondo que cubre todo el canvas

        drawTitle(gc); // Llama al método para dibujar el título del sistema
        drawConnections(gc); // Llama al método para dibujar las conexiones (flechas) entre locaciones
        drawAllLocations(gc); // Llama al método para dibujar todas las 12 locaciones
        drawCounters(gc); // Llama al método para dibujar los contadores de estadísticas de cada locación

        detectVirtualTransits(); // Llama al método para actualizar y limpiar los tránsitos virtuales
        drawTransitEntities(gc); // Llama al método para dibujar las entidades reales que están en tránsito
        drawVirtualTransitEntities(gc); // Llama al método para dibujar las entidades virtuales en tránsito

        drawGlobalInfo(gc); // Llama al método para dibujar el panel de información global del sistema
        drawExitLabel(gc); // Llama al método para dibujar la etiqueta de salida (EXIT)

        gearRotation += 0.05; // Incrementa el ángulo de rotación de los engranes en 0.05 radianes para animación
        if (gearRotation > 2 * Math.PI) { // Condición que verifica si el ángulo de rotación excede 2π (360 grados)
            gearRotation = 0; // Reinicia el ángulo de rotación a 0 para comenzar de nuevo
        } // Cierre del bloque condicional if
    } // Cierre del método render

    private void drawTitle(GraphicsContext gc) { // Método privado que dibuja el título principal del sistema recibiendo el contexto gráfico como parámetro
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno como gris muy oscuro para el título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26)); // Establece la fuente como Arial, negrita, tamaño 26
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText("⚙️ SISTEMA DE FABRICACIÓN DE ENGRANES - MULTI-ENGRANE", WIDTH / 2, 35); // Dibuja el título centrado en la parte superior del canvas

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece la fuente como Arial, normal, tamaño 14
        gc.setFill(Color.rgb(100, 100, 100)); // Establece el color de relleno como gris medio para el subtítulo
        gc.fillText("Flujo: Conveyor→Almacén→Corte→Torno→Conveyor→Fresado→Almacén→Pintura→Inspección→Empaque→Embarque", WIDTH / 2, 60); // Dibuja el subtítulo describiendo el flujo del proceso
    } // Cierre del método drawTitle

    private void drawConnections(GraphicsContext gc) { // Método privado que dibuja todas las conexiones (líneas y flechas) entre locaciones recibiendo el contexto gráfico como parámetro
        gc.setStroke(Color.rgb(120, 120, 140)); // Establece el color de trazo como gris azulado para las líneas de conexión
        gc.setLineWidth(3); // Establece el grosor de la línea en 3 píxeles
        gc.setLineDashes(5, 5); // Establece un patrón de línea discontinua con segmentos de 5 píxeles

        drawConnection(gc, "CONVEYOR_1", "ALMACEN"); // Dibuja conexión horizontal de CONVEYOR_1 a ALMACEN
        drawConnection(gc, "ALMACEN", "CORTADORA"); // Dibuja conexión horizontal de ALMACEN a CORTADORA
        drawConnection(gc, "CORTADORA", "TORNO"); // Dibuja conexión horizontal de CORTADORA a TORNO
        drawConnectionVertical(gc, "TORNO", "CONVEYOR_2"); // Dibuja conexión vertical de TORNO a CONVEYOR_2
        drawConnectionReverse(gc, "FRESADORA", "CONVEYOR_2"); // Dibuja conexión horizontal inversa de FRESADORA a CONVEYOR_2
        drawConnectionReverse(gc, "ALMACEN_2", "FRESADORA"); // Dibuja conexión horizontal inversa de ALMACEN_2 a FRESADORA
        drawConnectionReverse(gc, "PINTURA", "ALMACEN_2"); // Dibuja conexión horizontal inversa de PINTURA a ALMACEN_2
        drawConnectionVertical(gc, "PINTURA", "INSPECCION_1"); // Dibuja conexión vertical de PINTURA a INSPECCION_1
        drawConnectionVertical(gc, "INSPECCION_1", "INSPECCION_2"); // Dibuja conexión vertical de INSPECCION_1 a INSPECCION_2
        drawConnectionDiagonal(gc, "INSPECCION_1", "EMPAQUE"); // Dibuja conexión diagonal de INSPECCION_1 a EMPAQUE
        drawConnectionDiagonal(gc, "INSPECCION_2", "EMPAQUE"); // Dibuja conexión diagonal de INSPECCION_2 a EMPAQUE
        drawConnection(gc, "EMPAQUE", "EMBARQUE"); // Dibuja conexión horizontal de EMPAQUE a EMBARQUE
        drawExitArrow(gc); // Dibuja la flecha de salida desde EMBARQUE

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

    private void drawConnectionReverse(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión horizontal inversa recibiendo el contexto gráfico y nombres de locaciones como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0]; // Calcula el punto X de salida en el borde izquierdo de la caja origen
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula el punto Y de salida en el centro vertical de la caja origen
        double x2 = pos2[0] + BOX_SIZE; // Calcula el punto X de llegada en el borde derecho de la caja destino
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula el punto Y de llegada en el centro vertical de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea de conexión entre los dos puntos
        drawArrow(gc, x1 + 20, y1, x1, y1); // Dibuja una flecha apuntando hacia la izquierda cerca del punto de salida
    } // Cierre del método drawConnectionReverse

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

    private void drawExitArrow(GraphicsContext gc) { // Método privado que dibuja la flecha de salida desde la locación de embarque recibiendo el contexto gráfico como parámetro
        double[] embarquePos = locationPositions.get("EMBARQUE"); // Obtiene la posición [x, y] de la locación de embarque
        if (embarquePos == null) return; // Si la posición es null, sale del método prematuramente

        double x1 = embarquePos[0] + BOX_SIZE; // Calcula el punto X inicial en el borde derecho de la caja de embarque
        double y1 = embarquePos[1] + BOX_SIZE / 2; // Calcula el punto Y inicial en el centro vertical de la caja de embarque
        double x2 = x1 + 100; // Calcula el punto X final extendiendo 100 píxeles hacia la derecha
        double y2 = y1; // El punto Y final es el mismo que el inicial (línea horizontal)

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea horizontal de salida
        drawArrow(gc, x1, y1, x2, y2); // Dibuja una flecha al final de la línea
    } // Cierre del método drawExitArrow

    private void drawExitLabel(GraphicsContext gc) { // Método privado que dibuja la etiqueta "EXIT" al final del flujo recibiendo el contexto gráfico como parámetro
        double[] embarquePos = locationPositions.get("EMBARQUE"); // Obtiene la posición [x, y] de la locación de embarque
        if (embarquePos == null) return; // Si la posición es null, sale del método prematuramente

        double x = embarquePos[0] + BOX_SIZE + 110; // Calcula la posición X de la etiqueta después de la flecha de salida
        double y = embarquePos[1] + BOX_SIZE / 2; // Calcula la posición Y de la etiqueta centrada verticalmente

        gc.setFill(Color.rgb(76, 175, 80)); // Establece el color de relleno como verde para el fondo de la etiqueta
        gc.fillRoundRect(x, y - 30, 100, 60, 12, 12); // Dibuja un rectángulo redondeado verde para la etiqueta EXIT

        gc.setStroke(Color.rgb(56, 142, 60)); // Establece el color de trazo como verde oscuro para el borde
        gc.setLineWidth(3); // Establece el grosor del borde en 3 píxeles
        gc.strokeRoundRect(x, y - 30, 100, 60, 12, 12); // Dibuja el borde del rectángulo redondeado

        gc.setFill(Color.WHITE); // Establece el color de relleno como blanco para el texto
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20)); // Establece la fuente como Arial, negrita, tamaño 20
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText("EXIT", x + 50, y + 5); // Dibuja el texto "EXIT" centrado en la etiqueta
    } // Cierre del método drawExitLabel

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

    private void drawAllLocations(GraphicsContext gc) { // Método privado que dibuja todas las 12 locaciones del sistema recibiendo el contexto gráfico como parámetro
        // DIBUJAR TODAS las locaciones, existan o no en el motor
        String[] allLocations = { // Define un array con los nombres de todas las 12 locaciones
            "CONVEYOR_1", "ALMACEN", "CORTADORA", "TORNO", // Primera línea de locaciones
            "CONVEYOR_2", "FRESADORA", "ALMACEN_2", "PINTURA", // Segunda línea de locaciones
            "INSPECCION_1", "INSPECCION_2", "EMPAQUE", "EMBARQUE" // Tercera línea de locaciones
        }; // Cierre de la declaración del array

        for (String name : allLocations) { // Bucle for-each que itera sobre cada nombre de locación en el array
            Location location = engine.getLocation(name); // Obtiene el objeto Location del motor (puede ser null si no existe)
            // SIEMPRE dibujar, aunque location sea null
            drawLocationSafe(gc, name, location); // Llama al método seguro que dibuja la locación incluso si es null
        } // Cierre del bucle for-each
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
        int currentContent = location != null ? location.getCurrentContent() : 0; // Obtiene el contenido actual de la locación, o 0 si es null
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

        // ICONO - SIEMPRE visible
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.setFill(Color.WHITE); // Establece el color de relleno como blanco para el icono

        if (name.contains("CONVEYOR")) { // Condición que verifica si el nombre contiene "CONVEYOR"
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 56)); // Establece la fuente como Arial, negrita, tamaño 56 para la flecha
            gc.fillText("→", pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE * 0.62); // Dibuja la flecha centrada en la caja
        } else { // Bloque else que se ejecuta para todas las demás locaciones
            gc.setFont(Font.font("Segoe UI Emoji", 40)); // Establece la fuente como Segoe UI Emoji, tamaño 40 para los emojis
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE * 0.58); // Dibuja el emoji centrado en la caja
        } // Cierre del bloque else

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
        double utilization = location != null ? location.getUtilization(engine.getCurrentTime()) : 0; // Obtiene el porcentaje de utilización de la locación, o 0 si es null
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization); // Dibuja la barra de utilización debajo de la caja

        // Piezas (solo para no-conveyors)
        if (!name.contains("CONVEYOR") && location != null) { // Condición que verifica si no es un conveyor Y la locación existe
            drawEntitiesInLocation(gc, pos[0], pos[1], currentContent, capacity); // Dibuja las piezas individuales dentro de la locación
        } // Cierre del bloque condicional if
    } // Cierre del método drawLocationSafe

    private String getDisplayName(String name) { // Método privado que retorna un nombre formateado para mostrar recibiendo el nombre interno como parámetro
        switch (name) { // Switch que determina qué nombre formateado retornar basado en el nombre interno
            case "ALMACEN": return "ALMACEN 1"; // Si es ALMACEN, retorna "ALMACEN 1"
            case "ALMACEN_2": return "ALMACEN 2"; // Si es ALMACEN_2, retorna "ALMACEN 2"
            case "CONVEYOR_1": return "CONVEYOR 1"; // Si es CONVEYOR_1, retorna "CONVEYOR 1"
            case "CONVEYOR_2": return "CONVEYOR 2"; // Si es CONVEYOR_2, retorna "CONVEYOR 2"
            case "INSPECCION_1": return "INSPECCION 1"; // Si es INSPECCION_1, retorna "INSPECCION 1"
            case "INSPECCION_2": return "INSPECCION 2"; // Si es INSPECCION_2, retorna "INSPECCION 2"
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

    private void drawEntitiesInLocation(GraphicsContext gc, double x, double y, int count, int capacity) { // Método privado que dibuja círculos representando piezas individuales dentro de una locación recibiendo el contexto gráfico, posición, cantidad y capacidad como parámetros
        if (count == 0 || capacity == Integer.MAX_VALUE) return; // Si no hay piezas o la capacidad es infinita, sale del método prematuramente

        int maxDisplay = Math.min(count, capacity); // Calcula el máximo de piezas a mostrar (el menor entre el conteo y la capacidad)
        int cols = (int) Math.ceil(Math.sqrt(capacity)); // Calcula el número de columnas como la raíz cuadrada de la capacidad redondeada arriba
        int rows = (int) Math.ceil((double) capacity / cols); // Calcula el número de filas dividiendo la capacidad entre las columnas

        double pieceSize = Math.min((BOX_SIZE - 20) / cols, (BOX_SIZE - 20) / rows) * 0.65; // Calcula el tamaño de cada pieza para que quepan todas
        double offsetX = x + (BOX_SIZE - cols * pieceSize) / 2; // Calcula el desplazamiento X para centrar la cuadrícula horizontalmente
        double offsetY = y + (BOX_SIZE - rows * pieceSize) / 2 + 10; // Calcula el desplazamiento Y para centrar la cuadrícula verticalmente

        int drawn = 0; // Inicializa el contador de piezas dibujadas en 0
        for (int i = 0; i < rows && drawn < maxDisplay; i++) { // Bucle for externo que itera sobre las filas
            for (int j = 0; j < cols && drawn < maxDisplay; j++) { // Bucle for interno que itera sobre las columnas
                double px = offsetX + j * pieceSize + pieceSize / 4; // Calcula la posición X de la pieza actual
                double py = offsetY + i * pieceSize + pieceSize / 4; // Calcula la posición Y de la pieza actual

                gc.setFill(Color.rgb(0, 0, 0, 0.3)); // Establece el color de relleno como negro semitransparente para la sombra
                gc.fillOval(px + 2, py + 2, pieceSize / 2, pieceSize / 2); // Dibuja un círculo desplazado como sombra de la pieza

                gc.setFill(Color.rgb(33, 150, 243)); // Establece el color de relleno como azul para la pieza
                gc.fillOval(px, py, pieceSize / 2, pieceSize / 2); // Dibuja el círculo que representa la pieza

                gc.setStroke(Color.rgb(25, 118, 210)); // Establece el color de trazo como azul oscuro para el borde de la pieza
                gc.setLineWidth(1.5); // Establece el grosor del borde en 1.5 píxeles
                gc.strokeOval(px, py, pieceSize / 2, pieceSize / 2); // Dibuja el borde del círculo

                drawn++; // Incrementa el contador de piezas dibujadas
            } // Cierre del bucle for interno
        } // Cierre del bucle for externo
    } // Cierre del método drawEntitiesInLocation

    private void drawCounters(GraphicsContext gc) { // Método privado que dibuja los contadores de estadísticas de todas las locaciones recibiendo el contexto gráfico como parámetro
        double startX = COUNTER_START_X; // Establece la posición X inicial usando la constante definida
        double startY = COUNTER_START_Y; // Establece la posición Y inicial usando la constante definida
        double spacing = COUNTER_HEIGHT + 6; // Calcula el espaciado vertical entre contadores sumando la altura más 6 píxeles

        String[] locations = { // Define un array con los nombres de todas las locaciones para las cuales se dibujarán contadores
            "CONVEYOR_1", "ALMACEN", "CORTADORA", "TORNO", // Primera línea de locaciones
            "CONVEYOR_2", "FRESADORA", "ALMACEN_2", "PINTURA", // Segunda línea de locaciones
            "INSPECCION_1", "INSPECCION_2", "EMPAQUE", "EMBARQUE" // Tercera línea de locaciones
        }; // Cierre de la declaración del array

        for (int i = 0; i < locations.length; i++) { // Bucle for que itera sobre cada índice del array de locaciones
            Location loc = engine.getLocation(locations[i]); // Obtiene el objeto Location del motor (puede ser null si no existe)
            // SIEMPRE dibujar contador, aunque loc sea null
            drawCounterSafe(gc, startX, startY + i * spacing, locations[i], loc); // Dibuja el contador en la posición calculada
        } // Cierre del bucle for
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

        int entries = location != null ? location.getTotalEntries() : 0; // Obtiene el total de entradas de la locación, o 0 si es null
        gc.fillText("Entradas: " + entries, x + 12, y + 46); // Dibuja el texto de entradas totales

        double util = location != null ? location.getUtilization(engine.getCurrentTime()) : 0; // Obtiene el porcentaje de utilización, o 0 si es null
        gc.fillText(String.format("Utilización: %.0f%%", util), x + 12, y + 65); // Dibuja el texto de utilización formateado sin decimales

        int queue = location != null ? location.getQueueSize() : 0; // Obtiene el tamaño de la cola, o 0 si es null
        gc.fillText("Cola: " + queue, x + 130, y + 46); // Dibuja el texto del tamaño de cola en la columna derecha

        double avgContent = location != null ? location.getAverageContent(engine.getCurrentTime()) : 0; // Obtiene el contenido promedio, o 0 si es null
        gc.fillText(String.format("Prom: %.1f", avgContent), x + 130, y + 65); // Dibuja el texto del contenido promedio con 1 decimal en la columna derecha

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

    private void detectVirtualTransits() { // Método privado que actualiza y limpia los tránsitos virtuales obsoletos sin recibir parámetros
        virtualTransits.removeIf(vt -> { // Usa removeIf para filtrar y remover tránsitos que cumplan la condición especificada
            vt.progress += 0.08; // Incrementa el progreso del tránsito virtual en 0.08
            return vt.progress >= 1.0; // Retorna true si el progreso alcanzó o superó 1.0 (100%) para removerlo de la lista
        }); // Cierre del paréntesis de removeIf
    } // Cierre del método detectVirtualTransits

    private void drawTransitEntities(GraphicsContext gc) { // Método privado que dibuja todas las entidades reales que están en tránsito recibiendo el contexto gráfico como parámetro
        List<Entity> allEntities = engine.getAllActiveEntities(); // Obtiene la lista de todas las entidades activas del motor
        if (allEntities == null) return; // Si la lista es null, sale del método prematuramente

        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación

        for (Entity entity : allEntities) { // Bucle for-each que itera sobre cada entidad activa
            if (entity == null || !entity.isInTransit()) continue; // Si la entidad es null o no está en tránsito, salta a la siguiente iteración

            String from = entity.getCurrentLocation(); // Obtiene la locación de origen de la entidad
            String to = entity.getDestinationLocation(); // Obtiene la locación de destino de la entidad
            if (from == null || to == null) continue; // Si alguna locación es null, salta a la siguiente iteración

            double progress = entity.getTransitProgress(currentTime); // Calcula el progreso del tránsito (0.0 a 1.0)

            double[] fromPos = getLocationExitPoint(from); // Obtiene el punto de salida de la locación origen
            double[] toPos = getLocationEntryPoint(to); // Obtiene el punto de entrada de la locación destino

            if (fromPos != null && toPos != null) { // Condición que verifica si ambas posiciones son válidas
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * progress; // Calcula la posición X interpolada según el progreso
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * progress; // Calcula la posición Y interpolada según el progreso
                Color baseColor = locationColors.getOrDefault(to, Color.rgb(33, 150, 243)); // Obtiene el color del destino, o azul por defecto
                drawMovingPiece(gc, x, y, entity.getId(), baseColor); // Dibuja la pieza en movimiento en la posición calculada
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each
    } // Cierre del método drawTransitEntities

    private void drawVirtualTransitEntities(GraphicsContext gc) { // Método privado que dibuja las entidades en tránsito virtual recibiendo el contexto gráfico como parámetro
        for (VirtualTransit vt : virtualTransits) { // Bucle for-each que itera sobre cada tránsito virtual en la lista
            double[] fromPos = getLocationExitPoint(vt.from); // Obtiene el punto de salida de la locación origen del tránsito virtual
            double[] toPos = getLocationEntryPoint(vt.to); // Obtiene el punto de entrada de la locación destino del tránsito virtual

            if (fromPos != null && toPos != null) { // Condición que verifica si ambas posiciones son válidas
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * vt.progress; // Calcula la posición X interpolada según el progreso del tránsito virtual
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * vt.progress; // Calcula la posición Y interpolada según el progreso del tránsito virtual
                Color baseColor = locationColors.getOrDefault(vt.to, Color.rgb(33, 150, 243)); // Obtiene el color del destino, o azul por defecto
                drawMovingPiece(gc, x, y, vt.entityId, baseColor); // Dibuja la pieza virtual en movimiento en la posición calculada
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each
    } // Cierre del método drawVirtualTransitEntities

    private double[] getLocationExitPoint(String location) { // Método privado que retorna el punto de salida [x, y] de una locación recibiendo el nombre de la locación como parámetro
        double[] pos = locationPositions.get(location); // Obtiene la posición base [x, y] de la locación
        if (pos == null) return null; // Si la posición es null, retorna null

        if (location.equals("TORNO") || location.equals("PINTURA") || location.equals("INSPECCION_1")) { // Condición que verifica si la locación tiene salida vertical
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE}; // Retorna el punto en el centro horizontal del borde inferior
        } // Cierre del bloque condicional if

        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Para todas las demás locaciones, retorna el punto en el borde derecho
    } // Cierre del método getLocationExitPoint

    private double[] getLocationEntryPoint(String location) { // Método privado que retorna el punto de entrada [x, y] de una locación recibiendo el nombre de la locación como parámetro
        double[] pos = locationPositions.get(location); // Obtiene la posición base [x, y] de la locación
        if (pos == null) return null; // Si la posición es null, retorna null

        if (location.equals("CONVEYOR_2") || location.equals("INSPECCION_1") || location.equals("INSPECCION_2")) { // Condición que verifica si la locación tiene entrada vertical
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Retorna el punto en el centro horizontal del borde superior
        } // Cierre del bloque condicional if

        if (location.equals("FRESADORA") || location.equals("ALMACEN_2") || location.equals("PINTURA")) { // Condición que verifica si la locación tiene entrada desde la derecha
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Retorna el punto en el centro vertical del borde derecho
        } // Cierre del bloque condicional if

        return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Para todas las demás locaciones, retorna el punto en el borde izquierdo
    } // Cierre del método getLocationEntryPoint

    private void drawMovingPiece(GraphicsContext gc, double x, double y, int entityId, Color baseColor) { // Método privado que dibuja una pieza en movimiento recibiendo el contexto gráfico, posición, ID de entidad y color como parámetros
        double pieceSize = 16; // Define el tamaño de la pieza en movimiento en píxeles

        Color shadowColor = baseColor.deriveColor(0, 1.0, 0.4, 0.35); // Crea un color de sombra derivando del color base
        gc.setFill(shadowColor); // Establece el color de relleno como el color de sombra
        gc.fillOval(x - pieceSize/2 + 2, y - pieceSize/2 + 2, pieceSize, pieceSize); // Dibuja un círculo desplazado como sombra de la pieza

        gc.setFill(baseColor); // Establece el color de relleno como el color base
        gc.fillOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize); // Dibuja el círculo principal de la pieza

        gc.setStroke(baseColor.darker()); // Establece el color de trazo como una versión más oscura del color base
        gc.setLineWidth(2); // Establece el grosor del borde en 2 píxeles
        gc.strokeOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize); // Dibuja el borde del círculo
    } // Cierre del método drawMovingPiece

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

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13)); // Establece la fuente como Arial, normal, tamaño 13 para las estadísticas

        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación en minutos
        int totalMinutes = (int) Math.floor(currentTime); // Convierte el tiempo a minutos enteros redondeando hacia abajo
        int hours = totalMinutes / 60; // Calcula las horas dividiendo los minutos entre 60
        int minutes = totalMinutes % 60; // Calcula los minutos restantes usando el operador módulo

        gc.fillText(String.format("⏱ Tiempo: %02d:%02d h", hours, minutes), // Dibuja el tiempo formateado en formato HH:MM
                infoX + 15, infoY + 60); // con la posición especificada

        int totalArrivals = engine.getStatistics().getTotalArrivals(); // Obtiene el total de arribos desde las estadísticas
        gc.fillText("📥 Arribos: " + totalArrivals, infoX + 15, infoY + 85); // Dibuja el texto de arribos totales con un emoji

        int totalExits = engine.getStatistics().getTotalExits(); // Obtiene el total de salidas (piezas completadas) desde las estadísticas
        gc.fillText("📤 Completadas: " + totalExits, infoX + 250, infoY + 60); // Dibuja el texto de piezas completadas en la columna derecha

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0; // Calcula el throughput en piezas por hora, o 0 si no hay tiempo
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput), // Dibuja el throughput formateado con 2 decimales
                infoX + 250, infoY + 85); // en la columna derecha

        int inSystem = totalArrivals - totalExits; // Calcula las piezas actualmente en el sistema restando salidas de arribos
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 250, infoY + 110); // Dibuja el texto de piezas en sistema en la columna derecha
    } // Cierre del método drawGlobalInfo

    public void reset() { // Método público que reinicia el panel de animación a su estado inicial sin recibir parámetros
        virtualTransits.clear(); // Limpia la lista de tránsitos virtuales
        gearRotation = 0; // Reinicia el ángulo de rotación de engranes a 0
        render(); // Llama al método render para redibujar el canvas limpio
    } // Cierre del método reset

    private static class VirtualTransit { // Declaración de clase estática privada interna VirtualTransit que representa un tránsito virtual
        int entityId; // Variable que almacena el ID de la entidad en tránsito virtual
        String from; // Variable que almacena el nombre de la locación de origen
        String to; // Variable que almacena el nombre de la locación de destino
        double progress; // Variable que almacena el progreso del tránsito (0.0 a 1.0)

        @SuppressWarnings("unused") // Anotación que suprime advertencias de variable no usada
        VirtualTransit(int entityId, String from, String to) { // Constructor que inicializa un tránsito virtual recibiendo el ID de entidad y locaciones como parámetros
            this.entityId = entityId; // Asigna el ID de entidad recibido a la variable de instancia
            this.from = from; // Asigna la locación origen recibida a la variable de instancia
            this.to = to; // Asigna la locación destino recibida a la variable de instancia
            this.progress = 0; // Inicializa el progreso en 0 (inicio del tránsito)
        } // Cierre del constructor VirtualTransit
    } // Cierre de la clase VirtualTransit
} // Cierre de la clase AnimationPanel
