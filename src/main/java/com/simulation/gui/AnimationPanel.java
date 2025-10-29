package com.simulation.gui; // Declaración del paquete que contiene las clases de interfaz gráfica de usuario (GUI) de la simulación

import com.simulation.core.Entity; // Importa la clase Entity que representa las piezas que fluyen por el sistema
import com.simulation.core.SimulationEngine; // Importa la clase SimulationEngine que es el motor principal de la simulación
import com.simulation.resources.InspectionStation; // Importa la clase InspectionStation que representa la estación de inspección específica
import com.simulation.resources.Location; // Importa la clase Location que representa las locaciones generales del sistema
import javafx.scene.canvas.Canvas; // Importa la clase Canvas de JavaFX para dibujar gráficos 2D
import javafx.scene.canvas.GraphicsContext; // Importa la clase GraphicsContext para realizar operaciones de dibujo en el canvas
import javafx.scene.layout.Pane; // Importa la clase Pane de JavaFX que es un contenedor de layout
import javafx.scene.paint.Color; // Importa la clase Color de JavaFX para definir colores en los gráficos
import javafx.scene.text.Font; // Importa la clase Font de JavaFX para definir fuentes de texto
import javafx.scene.text.FontWeight; // Importa la clase FontWeight de JavaFX para definir el peso de las fuentes (negrita, normal, etc.)
import javafx.scene.text.TextAlignment; // Importa la clase TextAlignment de JavaFX para alinear texto (centro, izquierda, derecha)

import java.util.*; // Importa todas las clases del paquete util de Java para usar colecciones, listas, mapas, etc.

/** // Inicio del comentario Javadoc de la clase
 * Panel de animación con contadores en tiempo real // Descripción de la clase como panel de animación
 */ // Fin del comentario Javadoc
public class AnimationPanel extends Pane { // Declaración de la clase pública AnimationPanel que extiende Pane para ser un panel de JavaFX con capacidad de dibujo
    private Canvas canvas; // Variable privada que almacena el canvas donde se dibuja toda la animación
    private SimulationEngine engine; // Variable privada que almacena la referencia al motor de simulación para acceder a datos en tiempo real

    private static final double WIDTH = 1600; // Constante estática final que define el ancho del canvas en píxeles
    private static final double HEIGHT = 700; // Constante estática final que define la altura del canvas en píxeles
    private static final double BOX_SIZE = 100; // Constante estática final que define el tamaño de cada caja de locación en píxeles
    private static final double VERTICAL_SPACING = 200; // Constante estática final que define el espaciado vertical entre filas de locaciones en píxeles
    private static final double COUNTER_WIDTH = 120; // Constante estática final que define el ancho de los contadores de estadísticas en píxeles
    private static final double COUNTER_HEIGHT = 80; // Constante estática final que define la altura de los contadores de estadísticas en píxeles
    private static final double COUNTER_START_X = 1150; // Constante estática final que define la coordenada X inicial de los contadores en píxeles
    private static final double COUNTER_START_Y = 90; // Constante estática final que define la coordenada Y inicial de los contadores en píxeles

    private Map<String, double[]> locationPositions; // Variable privada que almacena un mapa de nombres de locaciones a sus coordenadas [x, y] en el canvas
    private Map<String, Color> locationColors; // Variable privada que almacena un mapa de nombres de locaciones a sus colores representativos
    private Map<String, String> locationIcons; // Variable privada que almacena un mapa de nombres de locaciones a sus iconos emoji

    private List<VirtualTransit> virtualTransits; // Variable privada que almacena una lista de tránsitos virtuales para animar movimientos instantáneos
    private double gearRotation = 0; // Variable privada que almacena el ángulo de rotación actual de los engranajes para animación, inicializada en 0

    public AnimationPanel(SimulationEngine engine) { // Constructor público que inicializa el panel de animación recibiendo el motor de simulación como parámetro
        this.engine = engine; // Asigna el motor de simulación recibido a la variable de instancia engine
        this.canvas = new Canvas(WIDTH, HEIGHT); // Crea un nuevo canvas con el ancho y altura definidos por las constantes
        this.locationPositions = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar las posiciones de las locaciones
        this.locationColors = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar los colores de las locaciones
        this.locationIcons = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar los iconos de las locaciones
        this.virtualTransits = new ArrayList<>(); // Crea una nueva lista ArrayList vacía para almacenar los tránsitos virtuales

        initializePositions(); // Llama al método para inicializar las posiciones de todas las locaciones en el canvas
        initializeColors(); // Llama al método para inicializar los colores asignados a cada locación
        initializeIcons(); // Llama al método para inicializar los iconos emoji asignados a cada locación

        getChildren().add(canvas); // Agrega el canvas como hijo de este panel para que sea visible
        setMinSize(WIDTH, HEIGHT); // Establece el tamaño mínimo del panel con el ancho y altura definidos
        setPrefSize(WIDTH, HEIGHT); // Establece el tamaño preferido del panel con el ancho y altura definidos
        setMaxSize(WIDTH, HEIGHT); // Establece el tamaño máximo del panel con el ancho y altura definidos
    } // Cierre del constructor AnimationPanel

    private void initializePositions() { // Método privado que inicializa las coordenadas de todas las locaciones en el canvas
        double topY = 150; // Define la coordenada Y de la fila superior de locaciones
        double bottomY = topY + VERTICAL_SPACING; // Calcula la coordenada Y de la fila inferior sumando el espaciado vertical a la Y superior

        locationPositions.put("RECEPCION", new double[]{50, topY}); // Asigna la posición [50, 150] a la locación RECEPCION en la esquina superior izquierda
        locationPositions.put("LAVADORA", new double[]{280, topY}); // Asigna la posición [280, 150] a la locación LAVADORA en la fila superior
        locationPositions.put("ALMACEN_PINTURA", new double[]{510, topY}); // Asigna la posición [510, 150] a la locación ALMACEN_PINTURA en la fila superior
        locationPositions.put("PINTURA", new double[]{740, topY}); // Asigna la posición [740, 150] a la locación PINTURA en la fila superior

        locationPositions.put("ALMACEN_HORNO", new double[]{280, bottomY}); // Asigna la posición [280, 350] a la locación ALMACEN_HORNO en la fila inferior
        locationPositions.put("HORNO", new double[]{510, bottomY}); // Asigna la posición [510, 350] a la locación HORNO en la fila inferior
        locationPositions.put("INSPECCION_1", new double[]{780, bottomY}); // Asigna la posición [780, 350] a la primera mesa de inspección en la fila inferior
        locationPositions.put("INSPECCION_2", new double[]{930, bottomY}); // Asigna la posición [930, 350] a la segunda mesa de inspección en la fila inferior
    } // Cierre del método initializePositions

    private void initializeColors() { // Método privado que inicializa los colores representativos de cada locación usando valores RGB
        locationColors.put("RECEPCION", Color.rgb(100, 181, 246)); // Asigna un color azul claro a la locación RECEPCION
        locationColors.put("LAVADORA", Color.rgb(129, 199, 132)); // Asigna un color verde claro a la locación LAVADORA
        locationColors.put("ALMACEN_PINTURA", Color.rgb(255, 245, 157)); // Asigna un color amarillo claro a la locación ALMACEN_PINTURA
        locationColors.put("PINTURA", Color.rgb(255, 167, 38)); // Asigna un color naranja a la locación PINTURA
        locationColors.put("ALMACEN_HORNO", Color.rgb(255, 224, 178)); // Asigna un color durazno claro a la locación ALMACEN_HORNO
        locationColors.put("HORNO", Color.rgb(239, 83, 80)); // Asigna un color rojo a la locación HORNO
        locationColors.put("INSPECCION_1", Color.rgb(189, 189, 189)); // Asigna un color gris a la primera mesa de inspección
        locationColors.put("INSPECCION_2", Color.rgb(189, 189, 189)); // Asigna un color gris a la segunda mesa de inspección
    } // Cierre del método initializeColors

    private void initializeIcons() { // Método privado que inicializa los iconos emoji que representan cada locación
        locationIcons.put("RECEPCION", "📦"); // Asigna el emoji de caja 📦 a la locación RECEPCION
        locationIcons.put("LAVADORA", "🧼"); // Asigna el emoji de jabón 🧼 a la locación LAVADORA
        locationIcons.put("ALMACEN_PINTURA", "📦"); // Asigna el emoji de caja 📦 a la locación ALMACEN_PINTURA
        locationIcons.put("PINTURA", "🎨"); // Asigna el emoji de paleta de pintura 🎨 a la locación PINTURA
        locationIcons.put("ALMACEN_HORNO", "📦"); // Asigna el emoji de caja 📦 a la locación ALMACEN_HORNO
        locationIcons.put("HORNO", "🔥"); // Asigna el emoji de fuego 🔥 a la locación HORNO
        locationIcons.put("INSPECCION_1", "🔍"); // Asigna el emoji de lupa 🔍 a la primera mesa de inspección
        locationIcons.put("INSPECCION_2", "🔍"); // Asigna el emoji de lupa 🔍 a la segunda mesa de inspección
    } // Cierre del método initializeIcons

    public void render() { // Método público que renderiza toda la animación dibujando todos los elementos en el canvas
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Obtiene el contexto gráfico 2D del canvas para realizar operaciones de dibujo

        // Fondo
        gc.setFill(Color.rgb(240, 242, 245)); // Establece el color de relleno a un gris muy claro para el fondo
        gc.fillRect(0, 0, WIDTH, HEIGHT); // Dibuja un rectángulo relleno que cubre todo el canvas como fondo

        drawTitle(gc); // Llama al método para dibujar el título principal de la simulación
        drawConnections(gc); // Llama al método para dibujar las conexiones entre locaciones
        drawAllLocations(gc); // Llama al método para dibujar todas las cajas de locaciones
        drawCounters(gc); // NUEVO: Contadores en tiempo real // Llama al método para dibujar los contadores de estadísticas en tiempo real

        detectVirtualTransits(); // Llama al método para detectar y crear tránsitos virtuales para movimientos instantáneos
        drawTransitEntities(gc); // Llama al método para dibujar las entidades que están en tránsito real
        drawVirtualTransitEntities(gc); // Llama al método para dibujar las entidades que están en tránsito virtual

        drawGlobalInfo(gc); // Llama al método para dibujar el panel de información global con estadísticas generales

        gearRotation += 0.05; // Incrementa el ángulo de rotación de los engranajes en 0.05 radianes para animar su movimiento
        if (gearRotation > 2 * Math.PI) { // Condición que verifica si el ángulo de rotación superó una vuelta completa (2π radianes)
            gearRotation = 0; // Reinicia el ángulo de rotación a 0 para evitar valores muy grandes
        } // Cierre del bloque condicional if
    } // Cierre del método render

    private void drawTitle(GraphicsContext gc) { // Método privado que dibuja el título principal de la simulación recibiendo el contexto gráfico como parámetro
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno a un gris muy oscuro para el texto del título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26)); // Establece la fuente a Arial negrita de tamaño 26 puntos
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText("🏭 SIMULACIÓN DE LÍNEA DE PRODUCCIÓN", WIDTH / 2, 35); // Dibuja el texto del título centrado horizontalmente en la coordenada Y 35

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece la fuente a Arial normal de tamaño 14 puntos para el subtítulo
        gc.setFill(Color.rgb(100, 100, 100)); // Establece el color de relleno a un gris medio para el subtítulo
        gc.fillText("Modelo ProModel - Java Implementation", WIDTH / 2, 60); // Dibuja el subtítulo centrado horizontalmente en la coordenada Y 60
    } // Cierre del método drawTitle

    private void drawConnections(GraphicsContext gc) { // Método privado que dibuja las líneas de conexión entre locaciones recibiendo el contexto gráfico como parámetro
        gc.setStroke(Color.rgb(120, 120, 140)); // Establece el color de trazo a un gris azulado para las líneas de conexión
        gc.setLineWidth(3); // Establece el grosor de la línea a 3 píxeles
        gc.setLineDashes(5, 5); // Establece el patrón de línea discontinua con segmentos de 5 píxeles seguidos de espacios de 5 píxeles

        drawConnection(gc, "RECEPCION", "LAVADORA"); // Dibuja la conexión desde RECEPCION a LAVADORA
        drawConnection(gc, "LAVADORA", "ALMACEN_PINTURA"); // Dibuja la conexión desde LAVADORA a ALMACEN_PINTURA
        drawConnection(gc, "ALMACEN_PINTURA", "PINTURA"); // Dibuja la conexión desde ALMACEN_PINTURA a PINTURA
        drawConnectionCurved(gc, "PINTURA", "ALMACEN_HORNO"); // Dibuja la conexión curva desde PINTURA a ALMACEN_HORNO
        drawConnection(gc, "ALMACEN_HORNO", "HORNO"); // Dibuja la conexión desde ALMACEN_HORNO a HORNO
        drawConnectionToInspection(gc, "HORNO", "INSPECCION_1"); // Dibuja las conexiones desde HORNO a ambas mesas de inspección

        gc.setLineDashes(null); // Restablece el patrón de línea a sólido (sin discontinuidades)
    } // Cierre del método drawConnections

    private void drawConnection(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión recta entre dos locaciones recibiendo el contexto gráfico y nombres de locaciones como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene las coordenadas de la locación de origen
        double[] pos2 = locationPositions.get(to); // Obtiene las coordenadas de la locación de destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE; // Calcula la coordenada X del punto de salida (lado derecho de la caja de origen)
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula la coordenada Y del punto de salida (centro vertical de la caja de origen)
        double x2 = pos2[0]; // Coordenada X del punto de entrada (lado izquierdo de la caja de destino)
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula la coordenada Y del punto de entrada (centro vertical de la caja de destino)

        gc.strokeLine(x1, y1, x2, y2); // Dibuja una línea recta desde el punto de salida al punto de entrada
        drawArrow(gc, x1, y1, x2, y2); // Dibuja una flecha al final de la línea para indicar la dirección del flujo
    } // Cierre del método drawConnection

    private void drawConnectionCurved(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión curva (Bézier) entre dos locaciones recibiendo el contexto gráfico y nombres como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene las coordenadas de la locación de origen
        double[] pos2 = locationPositions.get(to); // Obtiene las coordenadas de la locación de destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE / 2; // Calcula la coordenada X del punto de salida (centro horizontal de la caja de origen)
        double y1 = pos1[1] + BOX_SIZE; // Calcula la coordenada Y del punto de salida (lado inferior de la caja de origen)
        double x2 = pos2[0] + BOX_SIZE / 2; // Calcula la coordenada X del punto de entrada (centro horizontal de la caja de destino)
        double y2 = pos2[1]; // Coordenada Y del punto de entrada (lado superior de la caja de destino)

        gc.beginPath(); // Inicia un nuevo path para dibujar la curva
        gc.moveTo(x1, y1); // Mueve el lápiz al punto de inicio de la curva
        gc.bezierCurveTo(x1, y1 + 50, x2, y2 - 50, x2, y2); // Dibuja una curva de Bézier cúbica con puntos de control para crear una curva suave
        gc.stroke(); // Dibuja el path de la curva con el color y grosor actuales
        drawArrow(gc, x2, y2 - 20, x2, y2); // Dibuja una flecha al final de la curva para indicar la dirección del flujo
    } // Cierre del método drawConnectionCurved

    private void drawConnectionToInspection(GraphicsContext gc, String from, String to) { // Método privado que dibuja conexiones desde una locación a ambas mesas de inspección recibiendo el contexto gráfico y nombres como parámetros
        double[] pos1 = locationPositions.get(from); // Obtiene las coordenadas de la locación de origen (HORNO)
        double[] pos2 = locationPositions.get(to); // Obtiene las coordenadas de la primera mesa de inspección
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método prematuramente

        double x1 = pos1[0] + BOX_SIZE; // Calcula la coordenada X del punto de salida (lado derecho de la caja de origen)
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula la coordenada Y del punto de salida (centro vertical de la caja de origen)
        double x2 = pos2[0]; // Coordenada X del punto de entrada a la primera mesa de inspección
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula la coordenada Y del punto de entrada (centro vertical de la primera mesa)

        gc.strokeLine(x1, y1, x2, y2); // Dibuja una línea desde HORNO a la primera mesa de inspección

        double[] pos3 = locationPositions.get("INSPECCION_2"); // Obtiene las coordenadas de la segunda mesa de inspección
        if (pos3 != null) { // Condición que verifica si existe la posición de la segunda mesa
            double x3 = pos3[0]; // Coordenada X del punto de entrada a la segunda mesa de inspección
            double y3 = pos3[1] + BOX_SIZE / 2; // Calcula la coordenada Y del punto de entrada (centro vertical de la segunda mesa)
            gc.strokeLine(x1, y1, x3, y3); // Dibuja una línea desde HORNO a la segunda mesa de inspección
        } // Cierre del bloque condicional if
    } // Cierre del método drawConnectionToInspection

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) { // Método privado que dibuja una flecha al final de una línea recibiendo el contexto gráfico y coordenadas de inicio y fin como parámetros
        double arrowLength = 12; // Define la longitud de las líneas de la flecha en píxeles
        double angle = Math.atan2(y2 - y1, x2 - x1); // Calcula el ángulo de la línea usando arcotangente para determinar la dirección

        double x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6); // Calcula la coordenada X del primer punto de la punta de la flecha (30 grados hacia arriba)
        double y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6); // Calcula la coordenada Y del primer punto de la punta de la flecha
        double x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6); // Calcula la coordenada X del segundo punto de la punta de la flecha (30 grados hacia abajo)
        double y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6); // Calcula la coordenada Y del segundo punto de la punta de la flecha

        gc.setFill(Color.rgb(120, 120, 140)); // Establece el color de relleno para la flecha (mismo que las líneas)
        gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3); // Dibuja un triángulo relleno que forma la punta de la flecha con tres vértices
    } // Cierre del método drawArrow

    private void drawAllLocations(GraphicsContext gc) { // Método privado que dibuja todas las locaciones del sistema recibiendo el contexto gráfico como parámetro
        drawLocation(gc, "RECEPCION", engine.getLocation("RECEPCION")); // Dibuja la caja de la locación RECEPCION
        drawLocation(gc, "LAVADORA", engine.getLocation("LAVADORA")); // Dibuja la caja de la locación LAVADORA
        drawLocation(gc, "ALMACEN_PINTURA", engine.getLocation("ALMACEN_PINTURA")); // Dibuja la caja de la locación ALMACEN_PINTURA
        drawLocation(gc, "PINTURA", engine.getLocation("PINTURA")); // Dibuja la caja de la locación PINTURA
        drawLocation(gc, "ALMACEN_HORNO", engine.getLocation("ALMACEN_HORNO")); // Dibuja la caja de la locación ALMACEN_HORNO
        drawLocation(gc, "HORNO", engine.getLocation("HORNO")); // Dibuja la caja de la locación HORNO
        drawInspectionStations(gc); // Dibuja las dos mesas de inspección con manejo especial
    } // Cierre del método drawAllLocations

    private void drawLocation(GraphicsContext gc, String name, Location location) { // Método privado que dibuja una locación individual recibiendo el contexto gráfico, nombre y objeto de locación como parámetros
        if (location == null) return; // Si la locación es null, sale del método prematuramente

        double[] pos = locationPositions.get(name); // Obtiene las coordenadas de la locación desde el mapa de posiciones
        if (pos == null) return; // Si la posición es null, sale del método prematuramente

        Color color = locationColors.get(name); // Obtiene el color asignado a esta locación desde el mapa de colores
        String icon = locationIcons.get(name); // Obtiene el icono emoji asignado a esta locación desde el mapa de iconos

        int currentContent = location.getCurrentContent(); // Obtiene el número actual de entidades en esta locación
        int capacity = location.getCapacity(); // Obtiene la capacidad máxima de la locación
        int queueSize = location.getQueueSize(); // Obtiene el tamaño de la cola de espera de esta locación

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.15)); // Establece el color de relleno a negro semi-transparente para la sombra
        gc.fillRoundRect(pos[0] + 4, pos[1] + 4, BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja un rectángulo redondeado desplazado para crear efecto de sombra

        // Caja principal
        gc.setFill(color); // Establece el color de relleno al color específico de esta locación
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el rectángulo redondeado principal de la caja de la locación

        gc.setStroke(color.darker()); // Establece el color de trazo a una versión más oscura del color de la locación para el borde
        gc.setLineWidth(3); // Establece el grosor del borde a 3 píxeles
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el borde redondeado de la caja

        // Icono o engranaje
        if (currentContent > 0 && !name.contains("ALMACEN")) { // Condición que verifica si hay entidades en la locación y no es un almacén
            drawGear(gc, pos[0] + BOX_SIZE / 2, pos[1] + 35, 18); // Dibuja un engranaje animado en el centro superior de la caja para indicar procesamiento activo
        } else { // Bloque else que se ejecuta si la locación está vacía o es un almacén
            gc.setFont(Font.font("Segoe UI Emoji", 28)); // Establece la fuente a Segoe UI Emoji de tamaño 28 para mostrar el emoji
            gc.setFill(Color.WHITE); // Establece el color de relleno a blanco para el icono
            gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + 40); // Dibuja el icono emoji centrado en la parte superior de la caja
        } // Cierre del bloque else

        // Nombre
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno a gris oscuro para el texto del nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Establece la fuente a Arial negrita de tamaño 11 para el nombre
        String displayName = name.replace("_", " "); // Reemplaza los guiones bajos con espacios para hacer el nombre más legible
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 25); // Dibuja el nombre de la locación centrado arriba de la caja

        // Capacidad actual
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22)); // Establece la fuente a Arial negrita de tamaño 22 para el texto de capacidad
        gc.setFill(Color.WHITE); // Establece el color de relleno a blanco para el texto de capacidad
        String contentText = currentContent + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity); // Crea el texto mostrando contenido actual / capacidad, usando símbolo infinito si la capacidad es máxima
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + 75); // Dibuja el texto de capacidad centrado en la parte inferior de la caja

        // Cola de espera
        if (queueSize > 0) { // Condición que verifica si hay entidades esperando en la cola
            drawQueueIndicator(gc, pos[0], pos[1], queueSize); // Dibuja un indicador visual de la cola con el tamaño de la cola
        } // Cierre del bloque condicional if

        // Barra de utilización
        double utilization = location.getUtilization(engine.getCurrentTime()); // Obtiene el porcentaje de utilización de la locación en el tiempo actual
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization); // Dibuja una barra de progreso debajo de la caja mostrando el nivel de utilización

        // Piezas en la locación
        drawEntitiesInLocation(gc, pos[0], pos[1], currentContent, capacity); // Dibuja representaciones visuales de las entidades dentro de la caja
    } // Cierre del método drawLocation

    private void drawGear(GraphicsContext gc, double centerX, double centerY, double radius) { // Método privado que dibuja un engranaje animado recibiendo el contexto gráfico, coordenadas del centro y radio como parámetros
        int teeth = 8; // Define el número de dientes del engranaje
        double innerRadius = radius * 0.6; // Calcula el radio interior del engranaje como 60% del radio exterior
        double toothHeight = radius * 0.3; // Calcula la altura de cada diente como 30% del radio

        gc.save(); // Guarda el estado actual del contexto gráfico para restaurarlo después
        gc.translate(centerX, centerY); // Traslada el origen de coordenadas al centro del engranaje
        gc.rotate(Math.toDegrees(gearRotation)); // Rota el contexto gráfico según el ángulo actual de rotación (convertido a grados)

        gc.setFill(Color.rgb(120, 120, 120)); // Establece el color de relleno a gris medio para el engranaje
        gc.setStroke(Color.rgb(80, 80, 80)); // Establece el color de trazo a gris oscuro para el borde del engranaje
        gc.setLineWidth(2); // Establece el grosor del borde a 2 píxeles

        gc.beginPath(); // Inicia un nuevo path para dibujar la forma del engranaje
        for (int i = 0; i < teeth * 2; i++) { // Bucle que itera sobre cada punto del contorno (2 puntos por diente: punta y valle)
            double angle = (2 * Math.PI / (teeth * 2)) * i; // Calcula el ángulo de este punto dividiendo el círculo completo entre el número total de puntos
            double r = (i % 2 == 0) ? radius + toothHeight : radius; // Alterna entre radio exterior (punta del diente) y radio base (valle entre dientes)
            double x = r * Math.cos(angle); // Calcula la coordenada X del punto usando coordenadas polares
            double y = r * Math.sin(angle); // Calcula la coordenada Y del punto usando coordenadas polares

            if (i == 0) gc.moveTo(x, y); // Si es el primer punto, mueve el lápiz sin dibujar
            else gc.lineTo(x, y); // Para los demás puntos, dibuja una línea desde el punto anterior
        } // Cierre del bucle for
        gc.closePath(); // Cierra el path conectando el último punto con el primero
        gc.fill(); // Rellena la forma del engranaje con el color establecido
        gc.stroke(); // Dibuja el borde del engranaje con el color de trazo establecido

        gc.setFill(Color.rgb(200, 200, 200)); // Establece el color de relleno a gris claro para el centro del engranaje
        gc.fillOval(-innerRadius / 2, -innerRadius / 2, innerRadius, innerRadius); // Dibuja un círculo relleno en el centro del engranaje

        gc.restore(); // Restaura el estado del contexto gráfico al estado guardado anteriormente
    } // Cierre del método drawGear

    private void drawInspectionStations(GraphicsContext gc) { // Método privado que dibuja las dos mesas de inspección con lógica especial recibiendo el contexto gráfico como parámetro
        InspectionStation inspeccion = (InspectionStation) engine.getLocation("INSPECCION"); // Obtiene la estación de inspección del motor y la convierte a tipo InspectionStation
        if (inspeccion == null) return; // Si la estación es null, sale del método prematuramente

        int totalContent = inspeccion.getCurrentContent(); // Obtiene el número total de entidades en inspección (ambas mesas combinadas)
        int totalQueue = inspeccion.getQueueSize(); // Obtiene el tamaño total de la cola de espera
        int totalEntries = inspeccion.getTotalEntries(); // Obtiene el número total de entradas históricas a inspección

        int content1 = Math.min(1, totalContent); // Asigna máximo 1 entidad a la primera mesa (0 o 1)
        int content2 = Math.max(0, totalContent - 1); // Asigna las entidades restantes a la segunda mesa (0 o 1)

        drawInspectionStation(gc, "INSPECCION_1", content1, 1, totalQueue, totalEntries, inspeccion); // Dibuja la primera mesa de inspección con su contenido y la cola completa
        drawInspectionStation(gc, "INSPECCION_2", content2, 1, 0, 0, inspeccion); // Dibuja la segunda mesa de inspección con su contenido pero sin mostrar cola ni entradas
    } // Cierre del método drawInspectionStations

    private void drawInspectionStation(GraphicsContext gc, String name, int content, // Método privado que dibuja una mesa de inspección individual recibiendo múltiples parámetros
                                      int capacity, int queueSize, int totalEntries, // Continuación de los parámetros del método
                                      InspectionStation inspeccion) { // Último parámetro: referencia a la estación de inspección completa
        double[] pos = locationPositions.get(name); // Obtiene las coordenadas de esta mesa de inspección desde el mapa de posiciones
        if (pos == null) return; // Si la posición es null, sale del método prematuramente

        Color color = locationColors.get(name); // Obtiene el color asignado a esta mesa de inspección
        String icon = locationIcons.get(name); // Obtiene el icono emoji asignado a esta mesa de inspección

        gc.setFill(Color.rgb(0, 0, 0, 0.15)); // Establece el color de relleno a negro semi-transparente para la sombra
        gc.fillRoundRect(pos[0] + 4, pos[1] + 4, BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja un rectángulo redondeado desplazado para crear efecto de sombra

        gc.setFill(color); // Establece el color de relleno al color específico de esta mesa
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el rectángulo redondeado principal de la caja de la mesa

        gc.setStroke(color.darker()); // Establece el color de trazo a una versión más oscura del color para el borde
        gc.setLineWidth(3); // Establece el grosor del borde a 3 píxeles
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el borde redondeado de la caja

        if (content > 0) { // Condición que verifica si hay una entidad siendo procesada en esta mesa
            drawGear(gc, pos[0] + BOX_SIZE / 2, pos[1] + 35, 18); // Dibuja un engranaje animado indicando procesamiento activo
        } else { // Bloque else que se ejecuta si la mesa está vacía
            gc.setFont(Font.font("Segoe UI Emoji", 28)); // Establece la fuente a Segoe UI Emoji de tamaño 28
            gc.setFill(Color.WHITE); // Establece el color de relleno a blanco para el icono
            gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + 40); // Dibuja el icono de lupa centrado en la parte superior
        } // Cierre del bloque else

        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno a gris oscuro para el texto del nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Establece la fuente a Arial negrita de tamaño 11
        String displayName = name.equals("INSPECCION_1") ? "INSPECCIÓN Mesa 1" : "INSPECCIÓN Mesa 2"; // Define el nombre a mostrar dependiendo de qué mesa sea
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 25); // Dibuja el nombre de la mesa centrado arriba de la caja

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22)); // Establece la fuente a Arial negrita de tamaño 22 para el texto de capacidad
        gc.setFill(Color.WHITE); // Establece el color de relleno a blanco para el texto de capacidad
        String contentText = content + "/" + capacity; // Crea el texto mostrando contenido actual / capacidad
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + 75); // Dibuja el texto de capacidad centrado en la parte inferior

        if (queueSize > 0 && name.equals("INSPECCION_1")) { // Condición que verifica si hay cola y es la primera mesa (solo mostrar cola en la primera)
            drawQueueIndicator(gc, pos[0], pos[1], queueSize); // Dibuja el indicador visual de la cola
        } // Cierre del bloque condicional if

        double utilization = inspeccion.getUtilization(engine.getCurrentTime()) / 2; // Calcula la utilización de esta mesa dividiendo la utilización total entre 2 (porque son 2 mesas)
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization); // Dibuja la barra de utilización debajo de la caja

        if (content > 0) { // Condición que verifica si hay entidades en esta mesa
            drawEntitiesInLocation(gc, pos[0], pos[1], content, capacity); // Dibuja representaciones visuales de las entidades en la caja
        } // Cierre del bloque condicional if
    } // Cierre del método drawInspectionStation

    private void drawQueueIndicator(GraphicsContext gc, double x, double y, int queueSize) { // Método privado que dibuja un indicador circular de cola recibiendo el contexto gráfico, coordenadas y tamaño de cola como parámetros
        double badgeX = x + BOX_SIZE - 35; // Calcula la coordenada X del indicador posicionándolo en la esquina superior derecha
        double badgeY = y - 10; // Calcula la coordenada Y del indicador posicionándolo ligeramente arriba de la caja
        double badgeSize = 30; // Define el tamaño del círculo del indicador en píxeles

        gc.setFill(Color.rgb(244, 67, 54)); // Establece el color de relleno a rojo para el indicador de cola
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja un círculo rojo para el fondo del indicador

        gc.setStroke(Color.WHITE); // Establece el color de trazo a blanco para el borde del círculo
        gc.setLineWidth(2); // Establece el grosor del borde a 2 píxeles
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja el borde blanco del círculo

        gc.setFill(Color.WHITE); // Establece el color de relleno a blanco para el número
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // Establece la fuente a Arial negrita de tamaño 14
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText(String.valueOf(queueSize), badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 5); // Dibuja el número de entidades en cola centrado en el círculo

        gc.setFill(Color.rgb(244, 67, 54)); // Establece el color de relleno a rojo para la etiqueta
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10)); // Establece la fuente a Arial negrita de tamaño 10
        gc.fillText("COLA", x + BOX_SIZE / 2, y + BOX_SIZE + 40); // Dibuja la etiqueta "COLA" centrada debajo de la caja
    } // Cierre del método drawQueueIndicator

    private void drawUtilizationBar(GraphicsContext gc, double x, double y, double width, double utilization) { // Método privado que dibuja una barra de utilización recibiendo el contexto gráfico, coordenadas, ancho y porcentaje de utilización como parámetros
        double barHeight = 10; // Define la altura de la barra en píxeles

        gc.setFill(Color.rgb(220, 220, 220)); // Establece el color de relleno a gris claro para el fondo de la barra
        gc.fillRoundRect(x, y, width, barHeight, 5, 5); // Dibuja un rectángulo redondeado que representa el fondo completo de la barra

        double fillWidth = width * (utilization / 100.0); // Calcula el ancho del relleno de la barra según el porcentaje de utilización

        Color fillColor; // Declara una variable para almacenar el color del relleno según el nivel de utilización
        if (utilization < 50) fillColor = Color.rgb(76, 175, 80); // Si la utilización es menor a 50%, usa verde (bajo uso)
        else if (utilization < 80) fillColor = Color.rgb(255, 193, 7); // Si la utilización está entre 50-80%, usa amarillo (uso medio)
        else fillColor = Color.rgb(244, 67, 54); // Si la utilización es mayor a 80%, usa rojo (alto uso)

        gc.setFill(fillColor); // Establece el color de relleno al color determinado según el nivel de utilización
        gc.fillRoundRect(x, y, fillWidth, barHeight, 5, 5); // Dibuja el rectángulo redondeado del relleno con el ancho calculado

        gc.setFill(Color.rgb(60, 60, 60)); // Establece el color de relleno a gris oscuro para el texto del porcentaje
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Establece la fuente a Arial negrita de tamaño 11
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText(String.format("%.0f%%", utilization), x + width / 2, y + barHeight + 15); // Dibuja el porcentaje de utilización centrado debajo de la barra
    } // Cierre del método drawUtilizationBar

    private void drawEntitiesInLocation(GraphicsContext gc, double x, double y, int count, int capacity) { // Método privado que dibuja círculos representando entidades dentro de una locación recibiendo el contexto gráfico, coordenadas, cantidad y capacidad como parámetros
        if (count == 0 || capacity == Integer.MAX_VALUE) return; // Si no hay entidades o la capacidad es infinita, sale del método prematuramente

        int maxDisplay = Math.min(count, capacity); // Determina el número máximo de entidades a dibujar (el menor entre count y capacity)
        int cols = (int) Math.ceil(Math.sqrt(capacity)); // Calcula el número de columnas usando la raíz cuadrada de la capacidad redondeada hacia arriba
        int rows = (int) Math.ceil((double) capacity / cols); // Calcula el número de filas dividiendo la capacidad entre columnas redondeando hacia arriba

        double pieceSize = Math.min((BOX_SIZE - 20) / cols, (BOX_SIZE - 20) / rows) * 0.65; // Calcula el tamaño de cada círculo basándose en el espacio disponible
        double offsetX = x + (BOX_SIZE - cols * pieceSize) / 2; // Calcula el desplazamiento X para centrar la cuadrícula de círculos horizontalmente
        double offsetY = y + (BOX_SIZE - rows * pieceSize) / 2 + 10; // Calcula el desplazamiento Y para centrar la cuadrícula de círculos verticalmente

        int drawn = 0; // Inicializa el contador de entidades dibujadas en 0
        for (int i = 0; i < rows && drawn < maxDisplay; i++) { // Bucle externo que itera sobre las filas mientras no se hayan dibujado todas las entidades
            for (int j = 0; j < cols && drawn < maxDisplay; j++) { // Bucle interno que itera sobre las columnas mientras no se hayan dibujado todas las entidades
                double px = offsetX + j * pieceSize + pieceSize / 4; // Calcula la coordenada X de este círculo
                double py = offsetY + i * pieceSize + pieceSize / 4; // Calcula la coordenada Y de este círculo

                gc.setFill(Color.rgb(0, 0, 0, 0.3)); // Establece el color de relleno a negro semi-transparente para la sombra
                gc.fillOval(px + 2, py + 2, pieceSize / 2, pieceSize / 2); // Dibuja un círculo desplazado para crear efecto de sombra

                gc.setFill(Color.rgb(33, 150, 243)); // Establece el color de relleno a azul para el círculo de la entidad
                gc.fillOval(px, py, pieceSize / 2, pieceSize / 2); // Dibuja el círculo que representa la entidad

                gc.setStroke(Color.rgb(25, 118, 210)); // Establece el color de trazo a azul oscuro para el borde del círculo
                gc.setLineWidth(1.5); // Establece el grosor del borde a 1.5 píxeles
                gc.strokeOval(px, py, pieceSize / 2, pieceSize / 2); // Dibuja el borde del círculo

                drawn++; // Incrementa el contador de entidades dibujadas
            } // Cierre del bucle interno for
        } // Cierre del bucle externo for
    } // Cierre del método drawEntitiesInLocation

    /** // Inicio del comentario Javadoc del método
     * NUEVO: Dibuja contadores de estadísticas en tiempo real // Descripción del método nuevo
     */ // Fin del comentario Javadoc
    private void drawCounters(GraphicsContext gc) { // Método privado que dibuja los contadores de estadísticas para cada locación recibiendo el contexto gráfico como parámetro
    double startX = COUNTER_START_X; // Asigna la coordenada X inicial de los contadores desde la constante
    double startY = COUNTER_START_Y; // Asigna la coordenada Y inicial de los contadores desde la constante
        double spacing = COUNTER_HEIGHT + 15; // Calcula el espaciado vertical entre contadores sumando la altura más 15 píxeles

        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA", // Define un array con los nombres de las locaciones para las cuales se mostrarán contadores
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"}; // Continuación del array de nombres de locaciones

        for (int i = 0; i < locations.length; i++) { // Bucle for que itera sobre cada locación en el array
            Location loc = engine.getLocation(locations[i]); // Obtiene el objeto Location correspondiente a esta locación del motor
            if (loc != null) { // Condición que verifica si la locación existe (no es null)
                drawCounter(gc, startX, startY + i * spacing, locations[i], loc); // Dibuja el contador para esta locación en la posición calculada
            } // Cierre del bloque condicional if
        } // Cierre del bucle for
    } // Cierre del método drawCounters

    /** // Inicio del comentario Javadoc del método
     * Dibuja un contador individual para una locación // Descripción del método
     */ // Fin del comentario Javadoc
    private void drawCounter(GraphicsContext gc, double x, double y, String name, Location location) { // Método privado que dibuja un contador individual recibiendo el contexto gráfico, coordenadas, nombre y objeto de locación como parámetros
        // Fondo del contador
        gc.setFill(Color.rgb(255, 255, 255, 0.95)); // Establece el color de relleno a blanco casi opaco para el fondo del contador
        gc.fillRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 8, 8); // Dibuja un rectángulo redondeado que forma el fondo del contador

        gc.setStroke(locationColors.get(name)); // Establece el color de trazo al color característico de esta locación
        gc.setLineWidth(2); // Establece el grosor del borde a 2 píxeles
        gc.strokeRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 8, 8); // Dibuja el borde redondeado del contador con el color de la locación

        // Nombre de la locación
        gc.setFill(Color.rgb(50, 50, 50)); // Establece el color de relleno a gris oscuro para el texto del nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10)); // Establece la fuente a Arial negrita de tamaño 10
        gc.setTextAlign(TextAlignment.LEFT); // Establece la alineación del texto a la izquierda
        String displayName = name.replace("_", " "); // Reemplaza los guiones bajos con espacios para hacer el nombre más legible
        gc.fillText(displayName, x + 5, y + 15); // Dibuja el nombre de la locación en la parte superior izquierda del contador

        // Estadísticas
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 9)); // Establece la fuente a Arial normal de tamaño 9 para las estadísticas

        int entries = location.getTotalEntries(); // Obtiene el número total de entradas históricas a esta locación
        gc.fillText("Entradas: " + entries, x + 5, y + 30); // Dibuja el texto mostrando el número total de entradas

        double util = location.getUtilization(engine.getCurrentTime()); // Obtiene el porcentaje de utilización de la locación en el tiempo actual
        gc.fillText(String.format("Util: %.1f%%", util), x + 5, y + 45); // Dibuja el texto mostrando la utilización con un decimal

        int queue = location.getQueueSize(); // Obtiene el tamaño actual de la cola de espera
        gc.fillText("Cola: " + queue, x + 5, y + 60); // Dibuja el texto mostrando el tamaño de la cola

        double avgContent = location.getAverageContent(engine.getCurrentTime()); // Obtiene el contenido promedio de la locación en el tiempo actual
        gc.fillText(String.format("Prom: %.1f", avgContent), x + 5, y + 75); // Dibuja el texto mostrando el contenido promedio con un decimal
    } // Cierre del método drawCounter

    private void detectVirtualTransits() { // Método privado que detecta y gestiona tránsitos virtuales para movimientos instantáneos entre almacenes y estaciones
        virtualTransits.removeIf(vt -> { // Usa removeIf con un predicado para eliminar tránsitos virtuales que hayan completado su animación
            vt.progress += 0.08; // Incrementa el progreso del tránsito virtual en 0.08 (8%)
            return vt.progress >= 1.0; // Retorna true para eliminar este tránsito si su progreso alcanzó o superó 1.0 (100%)
        }); // Cierre del paréntesis de removeIf

        List<Entity> allEntities = engine.getAllActiveEntities(); // Obtiene la lista de todas las entidades activas en el sistema
        for (Entity entity : allEntities) { // Bucle for-each que itera sobre cada entidad activa
            if (entity == null || entity.isInTransit()) continue; // Si la entidad es null o ya está en tránsito real, salta a la siguiente iteración

            String currentLoc = entity.getCurrentLocation(); // Obtiene la ubicación actual de la entidad
            if (currentLoc == null) continue; // Si la ubicación actual es null, salta a la siguiente iteración

            if (currentLoc.equals("ALMACEN_PINTURA") && // Condición que verifica si la entidad está en almacén de pintura
                engine.getLocation("PINTURA").getCurrentContent() < engine.getLocation("PINTURA").getCapacity()) { // Y si hay espacio disponible en la estación de pintura
                boolean exists = virtualTransits.stream().anyMatch(vt -> vt.entityId == entity.getId()); // Verifica si ya existe un tránsito virtual para esta entidad usando stream y anyMatch
                if (!exists) { // Si no existe un tránsito virtual para esta entidad
                    virtualTransits.add(new VirtualTransit(entity.getId(), "ALMACEN_PINTURA", "PINTURA")); // Crea y agrega un nuevo tránsito virtual del almacén a pintura
                } // Cierre del bloque condicional if interno
            } // Cierre del bloque condicional if
            else if (currentLoc.equals("ALMACEN_HORNO") && // Condición alternativa que verifica si la entidad está en almacén del horno
                     engine.getLocation("HORNO").getCurrentContent() < engine.getLocation("HORNO").getCapacity()) { // Y si hay espacio disponible en el horno
                boolean exists = virtualTransits.stream().anyMatch(vt -> vt.entityId == entity.getId()); // Verifica si ya existe un tránsito virtual para esta entidad
                if (!exists) { // Si no existe un tránsito virtual para esta entidad
                    virtualTransits.add(new VirtualTransit(entity.getId(), "ALMACEN_HORNO", "HORNO")); // Crea y agrega un nuevo tránsito virtual del almacén al horno
                } // Cierre del bloque condicional if interno
            } // Cierre del bloque else if
        } // Cierre del bucle for-each
    } // Cierre del método detectVirtualTransits

    private void drawTransitEntities(GraphicsContext gc) { // Método privado que dibuja las entidades que están en tránsito real recibiendo el contexto gráfico como parámetro
        List<Entity> allEntities = engine.getAllActiveEntities(); // Obtiene la lista de todas las entidades activas en el sistema
        if (allEntities == null) return; // Si la lista es null, sale del método prematuramente

        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación

        for (Entity entity : allEntities) { // Bucle for-each que itera sobre cada entidad activa
            if (entity == null || !entity.isInTransit()) continue; // Si la entidad es null o no está en tránsito, salta a la siguiente iteración

            String from = entity.getCurrentLocation(); // Obtiene la ubicación de origen del tránsito
            String to = entity.getDestinationLocation(); // Obtiene la ubicación de destino del tránsito
            if (from == null || to == null) continue; // Si alguna ubicación es null, salta a la siguiente iteración

            double progress = entity.getTransitProgress(currentTime); // Obtiene el progreso del tránsito (0.0 a 1.0) basado en el tiempo actual

            double[] fromPos = getLocationExitPoint(from); // Obtiene las coordenadas del punto de salida de la locación de origen
            double[] toPos = getLocationEntryPoint(to); // Obtiene las coordenadas del punto de entrada de la locación de destino

            if (fromPos != null && toPos != null) { // Condición que verifica si ambas posiciones son válidas (no null)
                double x, y; // Declara variables para almacenar las coordenadas calculadas de la entidad

                if (from.equals("PINTURA") && to.equals("ALMACEN_HORNO")) { // Condición especial para el movimiento curvo de pintura a almacén del horno
                    double[] curvePos = getCurvePosition(fromPos, toPos, progress); // Calcula la posición en la curva Bézier según el progreso
                    x = curvePos[0]; // Asigna la coordenada X de la posición en la curva
                    y = curvePos[1]; // Asigna la coordenada Y de la posición en la curva
                } else if (to.equals("INSPECCION")) { // Condición para movimientos hacia inspección
                    toPos = getLocationEntryPoint("INSPECCION_1"); // Redirige al punto de entrada de la primera mesa de inspección
                    if (toPos == null) continue; // Si la posición es null, salta a la siguiente iteración
                    x = fromPos[0] + (toPos[0] - fromPos[0]) * progress; // Calcula la coordenada X interpolando linealmente entre origen y destino
                    y = fromPos[1] + (toPos[1] - fromPos[1]) * progress; // Calcula la coordenada Y interpolando linealmente entre origen y destino
                } else { // Bloque else para todos los demás movimientos rectos
                    x = fromPos[0] + (toPos[0] - fromPos[0]) * progress; // Calcula la coordenada X interpolando linealmente entre origen y destino
                    y = fromPos[1] + (toPos[1] - fromPos[1]) * progress; // Calcula la coordenada Y interpolando linealmente entre origen y destino
                } // Cierre del bloque else

                drawMovingPiece(gc, x, y, entity.getId()); // Dibuja la pieza en movimiento en las coordenadas calculadas
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each
    } // Cierre del método drawTransitEntities

    private void drawVirtualTransitEntities(GraphicsContext gc) { // Método privado que dibuja las entidades en tránsito virtual (movimientos instantáneos animados) recibiendo el contexto gráfico como parámetro
        for (VirtualTransit vt : virtualTransits) { // Bucle for-each que itera sobre cada tránsito virtual en la lista
            double[] fromPos = getLocationExitPoint(vt.from); // Obtiene las coordenadas del punto de salida de la locación de origen
            double[] toPos = getLocationEntryPoint(vt.to); // Obtiene las coordenadas del punto de entrada de la locación de destino

            if (fromPos != null && toPos != null) { // Condición que verifica si ambas posiciones son válidas (no null)
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * vt.progress; // Calcula la coordenada X interpolando linealmente según el progreso del tránsito virtual
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * vt.progress; // Calcula la coordenada Y interpolando linealmente según el progreso del tránsito virtual
                drawMovingPiece(gc, x, y, vt.entityId); // Dibuja la pieza en movimiento en las coordenadas calculadas
            } // Cierre del bloque condicional if
        } // Cierre del bucle for-each
    } // Cierre del método drawVirtualTransitEntities

    private double[] getLocationExitPoint(String location) { // Método privado que calcula el punto de salida de una locación recibiendo el nombre de la locación como parámetro y retornando un array de coordenadas
        if (location.equals("PINTURA")) { // Condición especial para la locación PINTURA que sale por abajo
            double[] pos = locationPositions.get(location); // Obtiene las coordenadas de la esquina superior izquierda de PINTURA
            if (pos == null) return null; // Si la posición es null, retorna null
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE}; // Retorna las coordenadas del punto de salida en el centro inferior de la caja
        } // Cierre del bloque condicional if

        double[] pos = locationPositions.get(location); // Obtiene las coordenadas de la esquina superior izquierda de la locación
        if (pos == null) return null; // Si la posición es null, retorna null
        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Retorna las coordenadas del punto de salida en el centro derecho de la caja (salida estándar)
    } // Cierre del método getLocationExitPoint

    private double[] getLocationEntryPoint(String location) { // Método privado que calcula el punto de entrada de una locación recibiendo el nombre de la locación como parámetro y retornando un array de coordenadas
        if (location.equals("ALMACEN_HORNO")) { // Condición especial para ALMACEN_HORNO que entra por arriba
            double[] pos = locationPositions.get(location); // Obtiene las coordenadas de la esquina superior izquierda de ALMACEN_HORNO
            if (pos == null) return null; // Si la posición es null, retorna null
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Retorna las coordenadas del punto de entrada en el centro superior de la caja
        } // Cierre del bloque condicional if

        if (location.equals("INSPECCION")) { // Condición especial para INSPECCION que redirige a la primera mesa
            location = "INSPECCION_1"; // Cambia la locación a INSPECCION_1 para obtener su posición
        } // Cierre del bloque condicional if

        double[] pos = locationPositions.get(location); // Obtiene las coordenadas de la esquina superior izquierda de la locación
        if (pos == null) return null; // Si la posición es null, retorna null
        return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Retorna las coordenadas del punto de entrada en el centro izquierdo de la caja (entrada estándar)
    } // Cierre del método getLocationEntryPoint

    private double[] getCurvePosition(double[] from, double[] to, double t) { // Método privado que calcula una posición en una curva Bézier cuadrática recibiendo puntos de inicio, fin y parámetro t como parámetros
        double midX = (from[0] + to[0]) / 2; // Calcula la coordenada X del punto medio entre origen y destino
        double controlY = from[1] + 80; // Calcula la coordenada Y del punto de control desplazándolo 80 píxeles abajo del origen para crear la curva

        double x = (1 - t) * (1 - t) * from[0] + // Calcula la coordenada X usando la fórmula de Bézier cuadrática: término del punto inicial
                   2 * (1 - t) * t * midX + // Término del punto de control (solo X, Y viene de controlY)
                   t * t * to[0]; // Término del punto final

        double y = (1 - t) * (1 - t) * from[1] + // Calcula la coordenada Y usando la fórmula de Bézier cuadrática: término del punto inicial
                   2 * (1 - t) * t * controlY + // Término del punto de control con la Y desplazada hacia abajo
                   t * t * to[1]; // Término del punto final

        return new double[]{x, y}; // Retorna un array con las coordenadas calculadas en la curva
    } // Cierre del método getCurvePosition

    private void drawMovingPiece(GraphicsContext gc, double x, double y, int entityId) { // Método privado que dibuja una pieza en movimiento recibiendo el contexto gráfico, coordenadas e ID de entidad como parámetros
        double pieceSize = 16; // Define el tamaño del círculo que representa la pieza en píxeles

        gc.setFill(Color.rgb(0, 0, 0, 0.3)); // Establece el color de relleno a negro semi-transparente para la sombra
        gc.fillOval(x - pieceSize/2 + 2, y - pieceSize/2 + 2, pieceSize, pieceSize); // Dibuja un círculo desplazado para crear efecto de sombra

        gc.setFill(Color.rgb(33, 150, 243)); // Establece el color de relleno a azul para el círculo de la pieza
        gc.fillOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize); // Dibuja el círculo principal centrado en las coordenadas dadas

        gc.setStroke(Color.rgb(25, 118, 210)); // Establece el color de trazo a azul oscuro para el borde
        gc.setLineWidth(2); // Establece el grosor del borde a 2 píxeles
        gc.strokeOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize); // Dibuja el borde del círculo
    } // Cierre del método drawMovingPiece

    private void drawGlobalInfo(GraphicsContext gc) { // Método privado que dibuja el panel de información global con estadísticas generales recibiendo el contexto gráfico como parámetro
        double infoX = WIDTH - 320; // Calcula la coordenada X del panel posicionándolo en la esquina superior derecha
        double infoY = 90; // Define la coordenada Y del panel
        double infoWidth = 300; // Define el ancho del panel en píxeles
        double infoHeight = 180; // Define la altura del panel en píxeles

        gc.setFill(Color.rgb(255, 255, 255, 0.98)); // Establece el color de relleno a blanco casi opaco para el fondo del panel
        gc.fillRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja un rectángulo redondeado que forma el fondo del panel

        gc.setStroke(Color.rgb(200, 200, 200)); // Establece el color de trazo a gris claro para el borde del panel
        gc.setLineWidth(2); // Establece el grosor del borde a 2 píxeles
        gc.strokeRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja el borde redondeado del panel

        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno a gris oscuro para el texto del título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Establece la fuente a Arial negrita de tamaño 16
        gc.setTextAlign(TextAlignment.LEFT); // Establece la alineación del texto a la izquierda
        gc.fillText("📊 Estadísticas en Tiempo Real", infoX + 15, infoY + 30); // Dibuja el título del panel

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13)); // Establece la fuente a Arial normal de tamaño 13 para las estadísticas

        double currentTime = engine.getCurrentTime(); // Obtiene el tiempo actual de la simulación en minutos
        int days = (int) (currentTime / (24 * 60)); // Calcula el número de días dividiendo el tiempo total entre minutos por día
        int hours = (int) ((currentTime % (24 * 60)) / 60); // Calcula las horas del día actual usando el módulo de minutos por día dividido entre 60
        int minutes = (int) (currentTime % 60); // Calcula los minutos de la hora actual usando el módulo de 60

        gc.fillText(String.format("⏱ Tiempo: %d días %02d:%02d", days, hours, minutes), // Dibuja el tiempo transcurrido formateado como días, horas y minutos
                   infoX + 15, infoY + 60); // Coordenadas del texto

        int totalArrivals = engine.getStatistics().getTotalArrivals(); // Obtiene el número total de arribos desde las estadísticas
        gc.fillText("📥 Arribos: " + totalArrivals, infoX + 15, infoY + 85); // Dibuja el texto mostrando el total de arribos

        int totalExits = engine.getStatistics().getTotalExits(); // Obtiene el número total de salidas desde las estadísticas
        gc.fillText("📤 Completadas: " + totalExits, infoX + 15, infoY + 110); // Dibuja el texto mostrando el total de piezas completadas

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0; // Calcula el throughput en piezas por hora dividiendo salidas entre tiempo y multiplicando por 60, o 0 si no hay tiempo transcurrido
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput), // Dibuja el texto mostrando el throughput con dos decimales
                   infoX + 15, infoY + 135); // Coordenadas del texto

        int inSystem = totalArrivals - totalExits; // Calcula el número de piezas actualmente en el sistema restando salidas de arribos
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 15, infoY + 160); // Dibuja el texto mostrando las piezas actuales en el sistema
    } // Cierre del método drawGlobalInfo

    public void reset() { // Método público que reinicia el panel de animación a su estado inicial
        virtualTransits.clear(); // Limpia la lista de tránsitos virtuales eliminando todos los elementos
        gearRotation = 0; // Reinicia el ángulo de rotación de los engranajes a 0
        render(); // Llama al método render para redibujar el canvas con el estado inicial
    } // Cierre del método reset

    private static class VirtualTransit { // Declaración de clase estática privada interna VirtualTransit para representar tránsitos virtuales animados
        int entityId; // Variable de instancia que almacena el ID de la entidad en tránsito virtual
        String from; // Variable de instancia que almacena el nombre de la locación de origen
        String to; // Variable de instancia que almacena el nombre de la locación de destino
        double progress; // Variable de instancia que almacena el progreso del tránsito (0.0 a 1.0)

        VirtualTransit(int entityId, String from, String to) { // Constructor que inicializa un tránsito virtual recibiendo ID de entidad, origen y destino como parámetros
            this.entityId = entityId; // Asigna el ID de entidad recibido a la variable de instancia
            this.from = from; // Asigna la locación de origen recibida a la variable de instancia
            this.to = to; // Asigna la locación de destino recibida a la variable de instancia
            this.progress = 0; // Inicializa el progreso en 0 (inicio del tránsito)
        } // Cierre del constructor VirtualTransit
    } // Cierre de la clase VirtualTransit
} // Cierre de la clase AnimationPanel
