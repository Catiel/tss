package com.simulation.gui; // Declaración del paquete de la interfaz gráfica

import com.simulation.core.Entity; // Importa la clase Entity que representa clientes en el sistema
import com.simulation.core.DigemicEngine; // Importa el motor de simulación DIGEMIC
import com.simulation.resources.Location; // Importa la clase base de locaciones
import javafx.scene.canvas.Canvas; // Importa la clase Canvas de JavaFX para dibujar gráficos 2D
import javafx.scene.canvas.GraphicsContext; // Importa la clase GraphicsContext de JavaFX para realizar operaciones de dibujo en el canvas
import javafx.scene.image.Image; // Permite utilizar imágenes personalizadas para las locaciones
import javafx.scene.layout.Pane; // Importa la clase Pane de JavaFX para crear un contenedor de layout
import javafx.scene.paint.Color; // Importa la clase Color de JavaFX para definir colores
import javafx.scene.text.Font; // Importa la clase Font de JavaFX para definir fuentes de texto
import javafx.scene.text.FontWeight; // Importa la enumeración FontWeight de JavaFX para especificar el grosor de la fuente
import javafx.scene.text.TextAlignment; // Importa la enumeración TextAlignment de JavaFX para especificar la alineación del texto

import java.io.File; // Importa la clase File para operaciones con archivos
import java.io.FileInputStream; // Importa FileInputStream para leer archivos desde el sistema
import java.io.IOException; // Importa IOException para manejar errores de entrada/salida
import java.io.InputStream; // Importa InputStream para leer streams de datos
import java.util.*; // Importa todas las clases del paquete util de Java (List, Map, ArrayList, HashMap, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Panel MEJORADO - Muestra TODAS las locaciones aunque no existan en el motor // Descripción de la clase indicando que dibuja todas las locaciones configuradas
 */ // Fin del comentario Javadoc
public class AnimationPanel extends Pane { // Declaración de la clase pública que extiende Pane de JavaFX
    private Canvas canvas; // Canvas donde se dibuja toda la animación
    private DigemicEngine engine; // Referencia al motor de simulación DIGEMIC

    private static final double WIDTH = 1200; // Ancho del canvas reducido de 1600 a 1200 píxeles
    private static final double HEIGHT = 900; // Alto del canvas reducido de 1250 a 900 píxeles
    private static final double BOX_SIZE = 140; // Tamaño de cada caja de locación aumentado de 120 a 140 píxeles
    private static final double COUNTER_WIDTH = 210; // Ancho de los contadores de estadísticas en píxeles
    private static final double COUNTER_HEIGHT = 86; // Alto de los contadores de estadísticas en píxeles
    private static final double COUNTER_START_X = 950; // Posición X inicial de los contadores ajustada al nuevo ancho
    private static final double COUNTER_START_Y = 80; // Posición Y inicial de los contadores desde la parte superior

    private Map<String, double[]> locationPositions; // Mapa que almacena las posiciones [x, y] de cada locación
    private Map<String, Color> locationColors; // Mapa que almacena el color representativo de cada locación
    private Map<String, String> locationIcons; // Mapa que almacena el emoji/icono de cada locación
    private Map<String, Image> locationImages; // Mapa que almacena imágenes personalizadas opcionales para locaciones
    private Map<String, Integer> lastVisualCounts; // Mapa que almacena los conteos visibles sincronizados con la animación

    private List<VirtualTransit> virtualTransits; // Lista de transiciones activas de entidades entre locaciones
    private Map<Integer, String> visualLocations; // Mapa que almacena locaciones visibles que pueden diferir de la real durante tránsito
    private Set<Integer> activeTransitEntities; // Conjunto de IDs de entidades actualmente en animación de tránsito
    private double gearRotation = 0; // Variable para controlar la rotación de animaciones (aumenta continuamente)
    
    // NUEVO: Variables para control de zoom
    private double zoomLevel = 1.0; // Nivel de zoom actual (1.0 = tamaño normal, 100%)
    private static final double MIN_ZOOM = 0.5; // Nivel mínimo de zoom permitido (50%)
    private static final double MAX_ZOOM = 2.0; // Nivel máximo de zoom permitido (200%)
    private static final double ZOOM_STEP = 0.1; // Incremento/decremento del zoom por cada operación (10%)

    public AnimationPanel(DigemicEngine engine) { // Constructor que recibe el motor de simulación DIGEMIC
        this.engine = engine; // Asigna el motor recibido al atributo de la clase
        this.canvas = new Canvas(WIDTH, HEIGHT); // Crea un nuevo canvas con el ancho y alto definidos
        this.locationPositions = new HashMap<>(); // Inicializa el mapa de posiciones vacío
        this.locationColors = new HashMap<>(); // Inicializa el mapa de colores vacío
        this.locationIcons = new HashMap<>(); // Inicializa el mapa de iconos vacío
        this.locationImages = new HashMap<>(); // Inicializa el mapa de imágenes vacío
        this.lastVisualCounts = new HashMap<>(); // Inicializa el mapa de conteos visuales vacío
        this.virtualTransits = new ArrayList<>(); // Inicializa la lista de tránsitos virtuales vacía
        this.visualLocations = new HashMap<>(); // Inicializa el mapa de locaciones visuales para tracking
        this.activeTransitEntities = new HashSet<>(); // Inicializa el conjunto de entidades en tránsito vacío

        initializePositions(); // Llama al método que configura las posiciones de todas las locaciones
        initializeColors(); // Llama al método que configura los colores de todas las locaciones
        initializeIcons(); // Llama al método que configura los iconos de todas las locaciones
        initializeImages(); // Llama al método que intenta cargar imágenes personalizadas

        getChildren().add(canvas); // Agrega el canvas como hijo del Pane para que sea visible
        setMinSize(WIDTH, HEIGHT); // Establece el tamaño mínimo del panel con el ancho y altura definidos
        setPrefSize(WIDTH, HEIGHT); // Establece el tamaño preferido del panel con el ancho y altura definidos
        
        setupZoomControls(); // Configura los controles de zoom con scroll del mouse
    } // Cierre del constructor AnimationPanel
    
    private void setupZoomControls() { // Método privado que configura los controles de zoom
        canvas.setOnScroll(event -> { // Establece un manejador de eventos para el scroll del mouse
            if (event.isControlDown()) { // Verifica si la tecla Control está presionada
                double delta = event.getDeltaY(); // Obtiene la dirección del scroll (positivo=arriba, negativo=abajo)
                if (delta > 0) { // Si el scroll es hacia arriba
                    zoomIn(); // Aumenta el zoom
                } else { // Si el scroll es hacia abajo
                    zoomOut(); // Disminuye el zoom
                }
                event.consume(); // Consume el evento para que no se propague
            }
        });
    }
    
    public void zoomIn() { // Método público que aumenta el nivel de zoom
        if (zoomLevel < MAX_ZOOM) { // Verifica que no se exceda el zoom máximo
            zoomLevel += ZOOM_STEP; // Incrementa el nivel de zoom en 0.1
            canvas.setScaleX(zoomLevel); // Aplica el nuevo zoom en el eje X
            canvas.setScaleY(zoomLevel); // Aplica el nuevo zoom en el eje Y
        }
    }
    
    public void zoomOut() { // Método público que disminuye el nivel de zoom
        if (zoomLevel > MIN_ZOOM) { // Verifica que no se baje del zoom mínimo
            zoomLevel -= ZOOM_STEP; // Decrementa el nivel de zoom en 0.1
            canvas.setScaleX(zoomLevel); // Aplica el nuevo zoom en el eje X
            canvas.setScaleY(zoomLevel); // Aplica el nuevo zoom en el eje Y
        }
    }
    
    public void resetZoom() { // Método público que reinicia el zoom al nivel normal (100%)
        zoomLevel = 1.0; // Establece el nivel de zoom en 1.0 (100%)
        canvas.setScaleX(1.0); // Aplica el zoom normal en el eje X
        canvas.setScaleY(1.0); // Aplica el zoom normal en el eje Y
    }

    // === MÉTODOS HELPER PARA ACCESO AL MOTOR ===
    
    private Location getLocationFromEngine(String name) { // Método helper que obtiene una locación del motor
        return engine.getLocation(name); // Retorna la locación solicitada desde el motor
    }

    private double getCurrentTimeFromEngine() { // Método helper que obtiene el tiempo actual de simulación
        return engine.getCurrentTime(); // Retorna el tiempo actual desde el motor
    }

    private List<Entity> getAllActiveEntitiesFromEngine() { // Método helper que obtiene todas las entidades activas
        return engine.getAllActiveEntities(); // Retorna la lista de entidades activas desde el motor
    }

    private com.simulation.statistics.Statistics getStatisticsFromEngine() { // Método helper que obtiene el objeto de estadísticas
        return engine.getStatistics(); // Retorna el objeto Statistics desde el motor
    }

    private void initializePositions() { // Método privado que inicializa las posiciones X-Y de cada locación DIGEMIC en el canvas
        locationPositions.put("ENTRADA", new double[]{60, 120}); // Puerta de entrada en esquina superior izquierda
        locationPositions.put("ZONA_FORMAS", new double[]{60, 340}); // Área de formularios abajo de entrada
        locationPositions.put("SALA_SILLAS", new double[]{340, 120}); // Sala con sillas en centro arriba (40 capacidad)
        locationPositions.put("SALA_DE_PIE", new double[]{340, 340}); // Área de pie en centro abajo (sin límite)
        locationPositions.put("SERVIDOR_1", new double[]{620, 160}); // Primera ventanilla en derecha arriba
        locationPositions.put("SERVIDOR_2", new double[]{620, 340}); // Segunda ventanilla en derecha abajo
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
        locationIcons.put("ENTRADA", "🚪"); // Emoji de puerta para entrada
        locationIcons.put("ZONA_FORMAS", "📝"); // Emoji de formulario para zona de formas
        locationIcons.put("SALA_SILLAS", "💺"); // Emoji de silla para sala de sillas
        locationIcons.put("SALA_DE_PIE", "🧍"); // Emoji de persona de pie para sala de pie
        locationIcons.put("SERVIDOR_1", "🏢"); // Emoji de ventanilla para servidor 1
        locationIcons.put("SERVIDOR_2", "🏢"); // Emoji de ventanilla para servidor 2
    } // Cierre del método initializeIcons

    public void render() { // Método público que renderiza (dibuja) toda la animación en el canvas
        GraphicsContext gc = canvas.getGraphicsContext2D(); // Obtiene el contexto gráfico 2D del canvas para realizar operaciones de dibujo

        gc.setFill(Color.rgb(240, 242, 245)); // Establece el color de relleno como gris muy claro para el fondo
        gc.fillRect(0, 0, WIDTH, HEIGHT); // Dibuja un rectángulo de fondo que cubre todo el canvas

        List<Entity> allEntities = getAllActiveEntitiesFromEngine(); // Obtiene todas las entidades activas desde el motor
        detectVirtualTransits(allEntities); // Detecta y actualiza las transiciones en curso entre locaciones
        Map<String, List<Entity>> groupedEntities = groupEntitiesByVisualLocation(allEntities); // Agrupa entidades por su locación visual actual
        refreshVisualCounts(groupedEntities); // Actualiza los contadores visuales basados en las entidades agrupadas

        drawTitle(gc); // Dibuja el título del sistema en la parte superior
        drawConnections(gc); // Dibuja las conexiones (flechas) entre locaciones
        drawAllLocations(gc); // Dibuja todas las 6 locaciones DIGEMIC
        drawCounters(gc); // Dibuja los contadores de estadísticas de cada locación

        drawStationaryEntities(gc, groupedEntities); // Dibuja las entidades que están esperando en cada locación
        drawVirtualTransitEntities(gc); // Dibuja las entidades que se están moviendo entre locaciones

        drawGlobalInfo(gc); // Dibuja el panel de información global del sistema

        gearRotation += 0.05; // Incrementa el ángulo de rotación en 0.05 radianes para animaciones
        if (gearRotation > 2 * Math.PI) { // Verifica si el ángulo excede 2π (360 grados)
            gearRotation = 0; // Reinicia el ángulo a 0 para comenzar de nuevo
        }
    } // Cierre del método render

    private void drawTitle(GraphicsContext gc) { // Método privado que dibuja el título principal del sistema
        gc.setFill(Color.rgb(33, 33, 33)); // Establece el color de relleno como gris muy oscuro para el título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26)); // Establece la fuente como Arial, negrita, tamaño 26
        gc.setTextAlign(TextAlignment.CENTER); // Establece la alineación del texto al centro
        gc.fillText("🛂 DIGEMIC - SISTEMA DE EXPEDICIÓN DE PASAPORTES", WIDTH / 2, 35); // Dibuja el título centrado horizontalmente

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece la fuente como Arial, normal, tamaño 14
        gc.setFill(Color.rgb(100, 100, 100)); // Establece el color como gris medio para el subtítulo
        gc.fillText("Entrada(10%→Formas,90%→Sillas/Pie) | Formas→Sillas/Pie | Pie→Sillas→Servidor1/2", WIDTH / 2, 60); // Dibuja el subtítulo describiendo el flujo
    } // Cierre del método drawTitle

    private void drawConnections(GraphicsContext gc) { // Método privado que dibuja todas las conexiones (líneas y flechas) entre locaciones
        gc.setStroke(Color.rgb(100, 150, 200)); // Color azul suave para las líneas de conexión
        gc.setLineWidth(3); // Establece el grosor de la línea en 3 píxeles
        gc.setLineDashes(5, 5); // Establece un patrón de línea discontinua con segmentos de 5 píxeles

        drawConnectionVertical(gc, "ENTRADA", "ZONA_FORMAS"); // Dibuja conexión vertical: 10% van a llenar formularios
        drawConnection(gc, "ENTRADA", "SALA_SILLAS"); // Dibuja conexión horizontal: 90% intentan ir a sillas
        drawConnectionDiagonal(gc, "ENTRADA", "SALA_DE_PIE"); // Dibuja conexión diagonal: si sillas llena, van a pie
        
        drawConnection(gc, "ZONA_FORMAS", "SALA_SILLAS"); // Dibuja conexión desde zona de formas a sillas
        drawConnectionDiagonal(gc, "ZONA_FORMAS", "SALA_DE_PIE"); // Dibuja conexión desde formas a pie si sillas llena
        
        drawConnectionVertical(gc, "SALA_DE_PIE", "SALA_SILLAS"); // Dibuja conexión vertical: de pie a sillas cuando hay espacio
        
        drawConnection(gc, "SALA_SILLAS", "SERVIDOR_1"); // Dibuja conexión desde sillas a servidor 1 (FIRST)
        drawConnectionDiagonal(gc, "SALA_SILLAS", "SERVIDOR_2"); // Dibuja conexión desde sillas a servidor 2

        gc.setLineDashes(null); // Restablece el patrón de línea a sólida (sin discontinuidades)
    } // Cierre del método drawConnections

    private void drawConnection(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión horizontal entre dos locaciones
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método

        double x1 = pos1[0] + BOX_SIZE; // Calcula el punto X de salida en el borde derecho de la caja origen
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula el punto Y de salida en el centro vertical de la caja origen
        double x2 = pos2[0]; // Calcula el punto X de llegada en el borde izquierdo de la caja destino
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula el punto Y de llegada en el centro vertical de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea de conexión entre los dos puntos
        drawArrow(gc, x1, y1, x2, y2); // Dibuja una flecha en el extremo final de la línea
    } // Cierre del método drawConnection

    private void drawConnectionVertical(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión vertical entre dos locaciones
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método

        double x1 = pos1[0] + BOX_SIZE / 2; // Calcula el punto X de salida en el centro horizontal de la caja origen
        double y1 = pos1[1] + BOX_SIZE; // Calcula el punto Y de salida en el borde inferior de la caja origen
        double x2 = pos2[0] + BOX_SIZE / 2; // Calcula el punto X de llegada en el centro horizontal de la caja destino
        double y2 = pos2[1]; // Calcula el punto Y de llegada en el borde superior de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea de conexión vertical entre los dos puntos
        drawArrow(gc, x2, y2 - 20, x2, y2); // Dibuja una flecha vertical cerca del punto de llegada
    } // Cierre del método drawConnectionVertical

    private void drawConnectionDiagonal(GraphicsContext gc, String from, String to) { // Método privado que dibuja una conexión diagonal entre dos locaciones
        double[] pos1 = locationPositions.get(from); // Obtiene la posición [x, y] de la locación origen
        double[] pos2 = locationPositions.get(to); // Obtiene la posición [x, y] de la locación destino
        if (pos1 == null || pos2 == null) return; // Si alguna posición es null, sale del método

        double x1 = pos1[0] + BOX_SIZE; // Calcula el punto X de salida en el borde derecho de la caja origen
        double y1 = pos1[1] + BOX_SIZE / 2; // Calcula el punto Y de salida en el centro vertical de la caja origen
        double x2 = pos2[0]; // Calcula el punto X de llegada en el borde izquierdo de la caja destino
        double y2 = pos2[1] + BOX_SIZE / 2; // Calcula el punto Y de llegada en el centro vertical de la caja destino

        gc.strokeLine(x1, y1, x2, y2); // Dibuja la línea diagonal de conexión entre los dos puntos
        drawArrow(gc, x1 + (x2 - x1) * 0.8, y1 + (y2 - y1) * 0.8, x2, y2); // Dibuja una flecha en el 80% del recorrido
    } // Cierre del método drawConnectionDiagonal

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) { // Método privado que dibuja una punta de flecha
        double arrowLength = 12; // Define la longitud de la punta de flecha en píxeles
        double angle = Math.atan2(y2 - y1, x2 - x1); // Calcula el ángulo de la línea usando arcotangente

        double x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6); // Calcula la coordenada X del primer punto de la flecha
        double y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6); // Calcula la coordenada Y del primer punto de la flecha
        double x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6); // Calcula la coordenada X del segundo punto de la flecha
        double y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6); // Calcula la coordenada Y del segundo punto de la flecha

        gc.setFill(Color.rgb(120, 120, 140)); // Establece el color de relleno como gris azulado para la flecha
        gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3); // Dibuja un triángulo relleno como punta de flecha
    } // Cierre del método drawArrow

    private void drawAllLocations(GraphicsContext gc) { // Método privado que dibuja todas las 6 locaciones DIGEMIC
        String[] allLocations = {"ENTRADA", "ZONA_FORMAS", "SALA_SILLAS", "SALA_DE_PIE", "SERVIDOR_1", "SERVIDOR_2"}; // Array con nombres de todas las locaciones

        for (String name : allLocations) { // Itera sobre cada nombre de locación
            Location location = getLocationFromEngine(name); // Obtiene el objeto Location desde el motor
            drawLocationSafe(gc, name, location); // Dibuja la locación de forma segura (maneja nulls)
        }
    } // Cierre del método drawAllLocations

    private void drawLocationSafe(GraphicsContext gc, String name, Location location) { // Método privado que dibuja una locación de forma segura
        double[] pos = locationPositions.get(name); // Obtiene la posición [x, y] de la locación
        if (pos == null) return; // Si la posición es null, sale del método

        Color color = locationColors.get(name); // Obtiene el color de la locación
        String icon = locationIcons.get(name); // Obtiene el icono emoji de la locación

        int currentContent = lastVisualCounts.getOrDefault(name, 0); // Obtiene el conteo visual actual o 0 si no existe
        int capacity = location != null ? location.getCapacity() : Integer.MAX_VALUE; // Obtiene capacidad o infinito si location es null
        int queueSize = location != null ? location.getQueueSize() : 0; // Obtiene tamaño de cola o 0 si location es null

        gc.setFill(Color.rgb(0, 0, 0, 0.2)); // Establece color negro semitransparente para la sombra
        gc.fillRoundRect(pos[0] + 5, pos[1] + 5, BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja rectángulo redondeado desplazado como sombra

        gc.setFill(color); // Establece el color de relleno con el color de la locación
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el rectángulo redondeado principal de la locación

        gc.setStroke(color.darker()); // Establece el color de trazo como versión más oscura del color
        gc.setLineWidth(4); // Establece el grosor del borde en 4 píxeles
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12); // Dibuja el borde del rectángulo

        Image locationImage = locationImages.get(name); // Obtiene la imagen personalizada si existe
        if (locationImage != null) { // Si existe una imagen personalizada
            drawLocationImage(gc, pos[0], pos[1], locationImage); // Dibuja la imagen en lugar del emoji
        } else { // Si no existe imagen personalizada
            gc.setTextAlign(TextAlignment.CENTER); // Establece alineación del texto al centro
            gc.setFill(Color.WHITE); // Establece color blanco para el icono
            gc.setFont(Font.font("Segoe UI Emoji", 40)); // Establece fuente Segoe UI Emoji tamaño 40
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE * 0.58); // Dibuja el emoji centrado
        }

        gc.setFill(Color.rgb(33, 33, 33)); // Establece color gris muy oscuro para el nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14)); // Establece fuente Arial negrita tamaño 14

        String displayName = getDisplayName(name); // Obtiene el nombre formateado para mostrar
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 12); // Dibuja el nombre centrado arriba de la caja

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24)); // Establece fuente Arial negrita tamaño 24 para el contador
        gc.setFill(Color.WHITE); // Establece color blanco para el contador
        String contentText = currentContent + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity); // Crea texto del contador
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE - 12); // Dibuja el contador en la parte inferior

        if (queueSize > 0) { // Si hay entidades en cola
            drawQueueIndicator(gc, pos[0], pos[1], queueSize); // Dibuja el indicador de cola
        }

        double utilization = location != null ? location.getUtilization(getCurrentTimeFromEngine()) : 0; // Obtiene utilización o 0
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization); // Dibuja barra de utilización

        if (name.startsWith("SERVIDOR")) { // Si la locación es un servidor
            drawServerBatchProgress(gc, pos[0], pos[1], name); // Dibuja el progreso de pasaportes procesados
        }
    } // Cierre del método drawLocationSafe

    private String getDisplayName(String name) { // Método privado que retorna un nombre formateado para mostrar
        switch (name) { // Evalúa el nombre interno
            case "ENTRADA": return "ENTRADA"; // Retorna "ENTRADA"
            case "ZONA_FORMAS": return "ZONA FORMAS"; // Retorna "ZONA FORMAS" con espacio
            case "SALA_SILLAS": return "SALA SILLAS"; // Retorna "SALA SILLAS" con espacio
            case "SALA_DE_PIE": return "SALA DE PIE"; // Retorna "SALA DE PIE" con espacios
            case "SERVIDOR_1": return "SERVIDOR 1"; // Retorna "SERVIDOR 1" con espacio
            case "SERVIDOR_2": return "SERVIDOR 2"; // Retorna "SERVIDOR 2" con espacio
            default: return name; // Para otros casos retorna el nombre sin modificar
        }
    } // Cierre del método getDisplayName

    private void drawQueueIndicator(GraphicsContext gc, double x, double y, int queueSize) { // Método privado que dibuja un indicador circular rojo mostrando tamaño de cola
        double badgeX = x + BOX_SIZE - 48; // Calcula posición X del badge en esquina superior derecha
        double badgeY = y - 16; // Calcula posición Y del badge ligeramente arriba de la caja
        double badgeSize = 44; // Define el tamaño del badge circular en píxeles

        gc.setFill(Color.rgb(244, 67, 54)); // Establece color rojo para el badge
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja círculo rojo como fondo

        gc.setStroke(Color.WHITE); // Establece color blanco para el borde
        gc.setLineWidth(3); // Establece grosor del borde en 3 píxeles
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize); // Dibuja el borde blanco del círculo

        gc.setFill(Color.WHITE); // Establece color blanco para el número
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 18)); // Establece fuente Arial negrita tamaño 18
        gc.setTextAlign(TextAlignment.CENTER); // Establece alineación al centro
        gc.fillText(String.valueOf(queueSize), badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 7); // Dibuja el número centrado
    } // Cierre del método drawQueueIndicator

    private void drawUtilizationBar(GraphicsContext gc, double x, double y, double width, double utilization) { // Método privado que dibuja barra de progreso de utilización
        double barHeight = 10; // Define altura de la barra en píxeles

        gc.setFill(Color.rgb(220, 220, 220)); // Establece color gris claro para fondo de la barra
        gc.fillRoundRect(x, y, width, barHeight, 5, 5); // Dibuja rectángulo redondeado como fondo

        double fillWidth = width * (utilization / 100.0); // Calcula ancho del relleno proporcional a utilización

        Color fillColor; // Declara variable para el color del relleno
        if (utilization < 50) fillColor = Color.rgb(76, 175, 80); // Si utilización < 50%, usa verde
        else if (utilization < 80) fillColor = Color.rgb(255, 193, 7); // Si utilización 50-80%, usa amarillo
        else fillColor = Color.rgb(244, 67, 54); // Si utilización >= 80%, usa rojo

        gc.setFill(fillColor); // Establece el color de relleno calculado
        gc.fillRoundRect(x, y, fillWidth, barHeight, 5, 5); // Dibuja rectángulo de progreso con ancho calculado

        gc.setFill(Color.rgb(60, 60, 60)); // Establece color gris oscuro para el texto del porcentaje
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11)); // Establece fuente Arial negrita tamaño 11
        gc.setTextAlign(TextAlignment.CENTER); // Establece alineación al centro
        gc.fillText(String.format("%.0f%%", utilization), x + width / 2, y + barHeight + 15); // Dibuja el porcentaje centrado debajo
    } // Cierre del método drawUtilizationBar

    private void drawCounters(GraphicsContext gc) { // Método privado que dibuja los contadores de estadísticas
        double startX = COUNTER_START_X; // Establece posición X inicial de contadores
        double startY = COUNTER_START_Y; // Establece posición Y inicial de contadores
        double spacing = COUNTER_HEIGHT + 6; // Calcula espaciado vertical entre contadores

        String[] locations = {"ENTRADA", "ZONA_FORMAS", "SALA_SILLAS", "SALA_DE_PIE", "SERVIDOR_1", "SERVIDOR_2"}; // Array con nombres de locaciones

        for (int i = 0; i < locations.length; i++) { // Itera sobre cada locación
            Location loc = getLocationFromEngine(locations[i]); // Obtiene el objeto Location del motor
            drawCounterSafe(gc, startX, startY + i * spacing, locations[i], loc); // Dibuja el contador de forma segura
        }
    } // Cierre del método drawCounters

    private void drawCounterSafe(GraphicsContext gc, double x, double y, String name, Location location) { // Método privado que dibuja un contador de estadísticas de forma segura
        gc.setFill(Color.rgb(0, 0, 0, 0.12)); // Establece color negro muy transparente para sombra
        gc.fillRoundRect(x + 2, y + 2, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja rectángulo desplazado como sombra

        gc.setFill(Color.rgb(255, 255, 255, 0.98)); // Establece color blanco casi opaco para fondo
        gc.fillRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja rectángulo principal del contador

        gc.setStroke(locationColors.get(name)); // Establece color de trazo según color de la locación
        gc.setLineWidth(3); // Establece grosor del borde en 3 píxeles
        gc.strokeRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 10, 10); // Dibuja el borde del contador

        gc.setFill(Color.rgb(30, 30, 30)); // Establece color gris muy oscuro para texto del nombre
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Establece fuente Arial negrita tamaño 16
        gc.setTextAlign(TextAlignment.LEFT); // Establece alineación a la izquierda

        String displayName = getDisplayName(name); // Obtiene el nombre formateado
        if (displayName.length() > 15) { // Si el nombre es muy largo (más de 15 caracteres)
            displayName = displayName.substring(0, 13) + ".."; // Trunca a 13 caracteres y agrega ".."
        }
        gc.fillText(displayName, x + 12, y + 24); // Dibuja el nombre con margen de 12 píxeles

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14)); // Establece fuente Arial normal tamaño 14
        gc.setFill(Color.rgb(50, 50, 50)); // Establece color gris oscuro para estadísticas

        int entries = location != null ? location.getTotalEntries() : 0; // Obtiene total de entradas o 0 si null
        gc.fillText("Entradas: " + entries, x + 12, y + 46); // Dibuja el texto de entradas

        double util = location != null ? location.getUtilization(getCurrentTimeFromEngine()) : 0; // Obtiene utilización o 0
        gc.fillText(String.format("Utilización: %.0f%%", util), x + 12, y + 65); // Dibuja el porcentaje de utilización

        int queue = location != null ? location.getQueueSize() : 0; // Obtiene tamaño de cola o 0
        gc.fillText("Cola: " + queue, x + 130, y + 46); // Dibuja el tamaño de cola en columna derecha

        double avgContent = location != null ? location.getAverageContent(getCurrentTimeFromEngine()) : 0; // Obtiene promedio de contenido o 0
        gc.fillText(String.format("Prom: %.1f", avgContent), x + 130, y + 65); // Dibuja el promedio en columna derecha

        double barWidth = COUNTER_WIDTH - 24; // Calcula ancho de barra restando márgenes
        double barHeight = 8; // Define altura de barra en píxeles
        double barY = y + COUNTER_HEIGHT - 17; // Calcula posición Y de barra en parte inferior

        gc.setFill(Color.rgb(220, 220, 220)); // Establece color gris claro para fondo de barra
        gc.fillRoundRect(x + 12, barY, barWidth, barHeight, 4, 4); // Dibuja fondo de barra

        double fillWidth = barWidth * (util / 100.0); // Calcula ancho de relleno proporcional
        Color barColor; // Declara variable para color de barra
        if (util < 40) barColor = Color.rgb(76, 175, 80); // Si utilización < 40%, usa verde
        else if (util < 80) barColor = Color.rgb(255, 152, 0); // Si utilización 40-80%, usa naranja
        else barColor = Color.rgb(244, 67, 54); // Si utilización >= 80%, usa rojo

        gc.setFill(barColor); // Establece el color de relleno calculado
        gc.fillRoundRect(x + 12, barY, fillWidth, barHeight, 4, 4); // Dibuja barra de progreso
    } // Cierre del método drawCounterSafe

    private void drawLocationImage(GraphicsContext gc, double baseX, double baseY, Image image) { // Método privado que dibuja imagen personalizada de locación
        if (image == null || image.isError()) { // Si imagen es null o tiene error
            return; // Sale del método sin dibujar
        }

        double availableWidth = BOX_SIZE * 0.7; // Calcula ancho disponible (70% del tamaño de caja)
        double availableHeight = BOX_SIZE * 0.65; // Calcula alto disponible (65% del tamaño de caja)
        double scale = Math.min(availableWidth / image.getWidth(), availableHeight / image.getHeight()); // Calcula escala manteniendo aspecto
        scale = Math.min(scale, 1.2); // Limita escala máxima a 1.2 para evitar escalados exagerados

        double drawWidth = image.getWidth() * scale; // Calcula ancho final de dibujado
        double drawHeight = image.getHeight() * scale; // Calcula alto final de dibujado
        double drawX = baseX + (BOX_SIZE - drawWidth) / 2.0; // Calcula posición X para centrar imagen
        double drawY = baseY + (BOX_SIZE - drawHeight) / 2.0 - 6; // Calcula posición Y para centrar imagen con ajuste

        gc.drawImage(image, drawX, drawY, drawWidth, drawHeight); // Dibuja la imagen con dimensiones y posición calculadas
    }

    private void drawServerBatchProgress(GraphicsContext gc, double baseX, double baseY, String serverName) { // Método privado que dibuja progreso de pasaportes procesados por servidor
        int target = engine.getServerBatchTarget(); // Obtiene meta de pasaportes antes de pausa (10)
        if (target <= 0) { // Si la meta es 0 o negativa
            return; // Sale del método sin dibujar
        }

        int progress = engine.getServerBatchProgress(serverName); // Obtiene progreso actual del servidor
        boolean paused = engine.isServerPaused(serverName); // Verifica si el servidor está pausado

        int columns = 5; // Define 5 columnas para organizar los círculos
        int rows = (int) Math.ceil((double) target / columns); // Calcula filas necesarias redondeando hacia arriba
        double bubbleSize = 12; // Define tamaño de cada círculo indicador en píxeles
        double bubbleSpacing = 5; // Define espaciado entre círculos en píxeles
        double panelPadding = 6; // Define padding interno del panel en píxeles

        double panelWidth = columns * bubbleSize + (columns - 1) * bubbleSpacing + panelPadding * 2; // Calcula ancho del panel
        double panelHeight = rows * bubbleSize + (rows - 1) * bubbleSpacing + panelPadding * 2; // Calcula alto del panel

        double panelX = baseX + BOX_SIZE + 14; // Calcula posición X del panel (a la derecha de la caja)
        double panelY = baseY + (BOX_SIZE - panelHeight) / 2.0; // Calcula posición Y del panel (centrado verticalmente)

        Color baseColor = locationColors.getOrDefault(serverName, Color.rgb(244, 67, 54)); // Obtiene color del servidor o rojo por defecto

        gc.setFill(Color.rgb(255, 255, 255, 0.9)); // Establece color blanco semitransparente para fondo
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10); // Dibuja rectángulo redondeado como fondo

        gc.setStroke(baseColor.darker()); // Establece color de trazo oscurecido
        gc.setLineWidth(2); // Establece grosor del borde en 2 píxeles
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 10, 10); // Dibuja el borde del panel

        for (int index = 0; index < target; index++) { // Itera sobre cada posición de pasaporte (0 a 9)
            int row = index / columns; // Calcula el número de fila
            int col = index % columns; // Calcula el número de columna

            double centerX = panelX + panelPadding + col * (bubbleSize + bubbleSpacing) + bubbleSize / 2.0; // Calcula centro X del círculo
            double centerY = panelY + panelPadding + row * (bubbleSize + bubbleSpacing) + bubbleSize / 2.0; // Calcula centro Y del círculo

            boolean filled = index < progress; // Determina si este círculo debe estar lleno (pasaporte procesado)

            Color fillColor; // Declara variable para color de relleno
            if (filled) { // Si el pasaporte ya fue procesado
                fillColor = paused ? Color.rgb(255, 214, 102) : baseColor; // Amarillo si pausado, color base si activo
            } else { // Si el pasaporte aún no se procesa
                fillColor = Color.rgb(189, 189, 189, 0.6); // Gris transparente para vacío
            }

            gc.setFill(fillColor); // Establece el color de relleno calculado
            gc.fillOval(centerX - bubbleSize / 2.0, centerY - bubbleSize / 2.0, bubbleSize, bubbleSize); // Dibuja el círculo relleno

            gc.setStroke(filled ? baseColor.darker() : Color.rgb(158, 158, 158)); // Color de borde según si está lleno
            gc.setLineWidth(1.5); // Establece grosor del borde en 1.5 píxeles
            gc.strokeOval(centerX - bubbleSize / 2.0, centerY - bubbleSize / 2.0, bubbleSize, bubbleSize); // Dibuja el borde del círculo
        }
    }

    private void initializeImages() { // Método privado que intenta cargar imágenes personalizadas desde recursos
        loadLocationImageFromResource("ENTRADA", "/images/entrada.png"); // Intenta cargar imagen de entrada
        loadLocationImageFromResource("ZONA_FORMAS", "/images/zona_formas.png"); // Intenta cargar imagen de zona de formas
        loadLocationImageFromResource("SALA_SILLAS", "/images/sala_sillas.png"); // Intenta cargar imagen de sala de sillas
        loadLocationImageFromResource("SALA_DE_PIE", "/images/sala_de_pie.png"); // Intenta cargar imagen de sala de pie
        loadLocationImageFromResource("SERVIDOR_1", "/images/servidor_1.png"); // Intenta cargar imagen de servidor 1
        loadLocationImageFromResource("SERVIDOR_2", "/images/servidor_2.png"); // Intenta cargar imagen de servidor 2
    }

    private void loadLocationImageFromResource(String locationName, String resourcePath) { // Método privado que carga imagen desde recursos internos
        if (locationName == null || resourcePath == null) { // Si algún parámetro es null
            return; // Sale del método sin hacer nada
        }

        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) { // Intenta abrir stream del recurso
            if (stream == null) { // Si el recurso no existe
                return; // Sale del método sin hacer nada
            }
            Image image = new Image(stream); // Crea objeto Image desde el stream
            if (!image.isError()) { // Si la imagen se cargó correctamente
                locationImages.put(locationName, image); // Almacena la imagen en el mapa
            }
        } catch (IOException ignored) { // Captura excepciones de IO pero las ignora
            // Silenciar: la imagen personalizada es opcional
        }
    }

    public boolean setLocationImage(String locationName, Image image) { // Método público que asigna imagen personalizada a una locación
        if (locationName == null || image == null || image.isError()) { // Si algún parámetro es inválido
            return false; // Retorna false indicando fallo
        }
        locationImages.put(locationName, image); // Almacena la imagen en el mapa
        return true; // Retorna true indicando éxito
    }

    public boolean setLocationImageFromResource(String locationName, String resourcePath) { // Método público que carga imagen desde recurso interno
        if (locationName == null || resourcePath == null) { // Si algún parámetro es null
            return false; // Retorna false indicando fallo
        }
        try (InputStream stream = getClass().getResourceAsStream(resourcePath)) { // Intenta abrir stream del recurso
            if (stream == null) { // Si el recurso no existe
                return false; // Retorna false indicando fallo
            }
            Image image = new Image(stream); // Crea objeto Image desde el stream
            if (image.isError()) { // Si la imagen tiene error
                return false; // Retorna false indicando fallo
            }
            locationImages.put(locationName, image); // Almacena la imagen en el mapa
            return true; // Retorna true indicando éxito
        } catch (IOException e) { // Captura excepciones de IO
            return false; // Retorna false indicando fallo
        }
    }

    public boolean setLocationImageFromFile(String locationName, String filePath) { // Método público que carga imagen desde archivo del sistema
        if (locationName == null || filePath == null) { // Si algún parámetro es null
            return false; // Retorna false indicando fallo
        }
        File file = new File(filePath); // Crea objeto File con la ruta especificada
        if (!file.exists() || !file.isFile()) { // Si el archivo no existe o no es un archivo regular
            return false; // Retorna false indicando fallo
        }
        try (InputStream stream = new FileInputStream(file)) { // Intenta abrir stream del archivo
            Image image = new Image(stream); // Crea objeto Image desde el stream
            if (image.isError()) { // Si la imagen tiene error
                return false; // Retorna false indicando fallo
            }
            locationImages.put(locationName, image); // Almacena la imagen en el mapa
            return true; // Retorna true indicando éxito
        } catch (IOException e) { // Captura excepciones de IO
            return false; // Retorna false indicando fallo
        }
    }

    public void clearLocationImage(String locationName) { // Método público que elimina imagen personalizada de una locación
        if (locationName != null) { // Si el nombre no es null
            locationImages.remove(locationName); // Remueve la imagen del mapa (volverá a usar emoji)
        }
    }

    private void detectVirtualTransits(List<Entity> allEntities) { // Método privado que detecta y actualiza transiciones virtuales entre locaciones
        double currentSimTime = getCurrentTimeFromEngine(); // Obtiene el tiempo actual de simulación desde el motor

        Iterator<VirtualTransit> iterator = virtualTransits.iterator(); // Crea iterador para recorrer transiciones
        while (iterator.hasNext()) { // Mientras haya transiciones
            VirtualTransit vt = iterator.next(); // Obtiene la siguiente transición
            double elapsed = currentSimTime - vt.startTime; // Calcula tiempo transcurrido desde inicio de transición
            double transitDuration = vt.estimatedDuration > 0 ? vt.estimatedDuration : 0.1; // Obtiene duración estimada o 0.1 por defecto
            vt.progress = Math.min(1.0, elapsed / transitDuration); // Calcula progreso (0.0 a 1.0) limitado a máximo 1.0

            if (vt.progress >= 1.0) { // Si la transición se completó (llegó al destino)
                iterator.remove(); // Remueve la transición de la lista
                activeTransitEntities.remove(vt.entityId); // Remueve la entidad del conjunto de entidades en tránsito
                visualLocations.put(vt.entityId, vt.to); // Actualiza la locación visual al destino
            }
        }

        if (allEntities == null) { // Si la lista de entidades es null
            cleanupVisualState(Collections.emptySet()); // Limpia el estado visual con conjunto vacío
            return; // Sale del método
        }

        Set<Integer> activeIds = new HashSet<>(); // Crea conjunto para almacenar IDs de entidades activas

        for (Entity entity : allEntities) { // Itera sobre todas las entidades
            if (entity == null) { // Si la entidad es null
                continue; // Salta a la siguiente iteración
            }

            String currentLoc = entity.getCurrentLocation(); // Obtiene la locación actual de la entidad
            if (currentLoc == null || currentLoc.isEmpty()) { // Si la locación es null o vacía
                visualLocations.remove(entity.getId()); // Remueve la locación visual de la entidad
                activeTransitEntities.remove(entity.getId()); // Remueve la entidad de las entidades en tránsito
                continue; // Salta a la siguiente iteración
            }

            int entityId = entity.getId(); // Obtiene el ID de la entidad
            activeIds.add(entityId); // Agrega el ID al conjunto de IDs activos
            String lastLoc = visualLocations.get(entityId); // Obtiene la última locación visual conocida

            if (lastLoc != null && !lastLoc.equals(currentLoc)) { // Si cambió de locación
                boolean alreadyTransiting = activeTransitEntities.contains(entityId); // Verifica si ya está en tránsito
                if (!alreadyTransiting // Si no está en tránsito
                        && locationPositions.containsKey(lastLoc) // Y existe posición de origen
                        && locationPositions.containsKey(currentLoc)) { // Y existe posición de destino
                    double distance = calculateDistance(lastLoc, currentLoc); // Calcula distancia entre locaciones
                    double duration = estimateTransitDuration(distance, lastLoc, currentLoc); // Estima duración del tránsito

                    VirtualTransit vt = new VirtualTransit(entityId, lastLoc, currentLoc); // Crea nueva transición virtual
                    vt.startTime = currentSimTime; // Establece tiempo de inicio como tiempo actual
                    vt.estimatedDuration = duration; // Establece la duración estimada
                    vt.progress = 0.0; // Establece progreso inicial en 0

                    virtualTransits.add(vt); // Agrega la transición a la lista
                    activeTransitEntities.add(entityId); // Agrega la entidad al conjunto de entidades en tránsito
                }
            }

            visualLocations.putIfAbsent(entityId, currentLoc); // Establece locación visual si no existe
            if (!activeTransitEntities.contains(entityId) && lastLoc == null) { // Si no está en tránsito y no tiene locación previa
                visualLocations.put(entityId, currentLoc); // Establece la locación visual actual
            }
        }

        cleanupVisualState(activeIds); // Limpia el estado visual eliminando entidades inactivas
    }

    private Map<String, List<Entity>> groupEntitiesByVisualLocation(List<Entity> allEntities) { // Método privado que agrupa entidades por su locación visual
        Map<String, List<Entity>> grouped = new HashMap<>(); // Crea mapa para almacenar entidades agrupadas
        if (allEntities == null) { // Si la lista es null
            return grouped; // Retorna mapa vacío
        }

        for (Entity entity : allEntities) { // Itera sobre todas las entidades
            if (entity == null) { // Si la entidad es null
                continue; // Salta a la siguiente iteración
            }

            int entityId = entity.getId(); // Obtiene el ID de la entidad
            if (activeTransitEntities.contains(entityId)) { // Si está en tránsito
                continue; // Salta a la siguiente (se dibuja aparte)
            }

            String visualLocation = visualLocations.computeIfAbsent(entityId, id -> entity.getCurrentLocation()); // Obtiene locación visual o establece actual
            if (visualLocation == null || visualLocation.isEmpty()) { // Si la locación es null o vacía
                continue; // Salta a la siguiente iteración
            }

            grouped.computeIfAbsent(visualLocation, key -> new ArrayList<>()).add(entity); // Agrega entidad a la lista de su locación
        }

        return grouped; // Retorna el mapa de entidades agrupadas
    }

    private void refreshVisualCounts(Map<String, List<Entity>> grouped) { // Método privado que actualiza contadores visuales basados en entidades agrupadas
        lastVisualCounts.clear(); // Limpia los contadores previos
        String[] allLocations = {"ENTRADA", "ZONA_FORMAS", "SALA_SILLAS", "SALA_DE_PIE", "SERVIDOR_1", "SERVIDOR_2"}; // Array con todas las locaciones

        for (String location : allLocations) { // Itera sobre cada locación
            int count = 0; // Inicializa contador en 0
            List<Entity> list = grouped.get(location); // Obtiene lista de entidades en esta locación
            if (list != null) { // Si la lista existe
                count = list.size(); // Establece contador como tamaño de la lista
            }
            if (count == 0) { // Si el contador es 0
                lastVisualCounts.put(location, 0); // Establece contador visual en 0
            }
            if (location.startsWith("SERVIDOR")) { // Si es un servidor
                for (VirtualTransit vt : virtualTransits) { // Itera sobre transiciones
                    if (location.equals(vt.to)) { // Si el destino es este servidor
                        count++; // Incrementa el contador (entidad en camino)
                    }
                }
                List<Entity> zeroList = grouped.get(location); // Obtiene lista de entidades
                if (zeroList == null || zeroList.isEmpty()) { // Si no hay entidades en el servidor
                    for (VirtualTransit vt : virtualTransits) { // Itera sobre transiciones
                        if (location.equals(vt.from)) { // Si el origen es este servidor
                            count++; // Incrementa el contador
                        }
                    }
                }
            }
            lastVisualCounts.put(location, count); // Almacena el contador final para esta locación
        }
    }

    private void cleanupVisualState(Set<Integer> activeIds) { // Método privado que limpia el estado visual eliminando entidades inactivas
        if (visualLocations.isEmpty()) { // Si el mapa está vacío
            return; // Sale del método sin hacer nada
        }

        Iterator<Integer> cleanupIterator = visualLocations.keySet().iterator(); // Crea iterador de IDs visuales
        while (cleanupIterator.hasNext()) { // Mientras haya IDs
            int id = cleanupIterator.next(); // Obtiene el siguiente ID
            if (!activeIds.contains(id)) { // Si el ID no está en el conjunto de IDs activos
                cleanupIterator.remove(); // Remueve el ID del mapa de locaciones visuales
                activeTransitEntities.remove(id); // Remueve el ID de entidades en tránsito
            }
        }
    }
    
    private double calculateDistance(String from, String to) { // Método privado que calcula distancia euclidiana entre dos locaciones
        double[] fromPos = locationPositions.get(from); // Obtiene posición de origen
        double[] toPos = locationPositions.get(to); // Obtiene posición de destino
        
        if (fromPos == null || toPos == null) return 100; // Si alguna es null, retorna distancia por defecto
        
        double dx = toPos[0] - fromPos[0]; // Calcula diferencia en eje X
        double dy = toPos[1] - fromPos[1]; // Calcula diferencia en eje Y
        return Math.sqrt(dx * dx + dy * dy); // Retorna distancia usando teorema de Pitágoras
    }
    
    private double estimateTransitDuration(double distance, String from, String to) { // Método privado que estima duración de tránsito entre locaciones
        double baseDuration; // Declara variable para duración base

        if (from.equals("ENTRADA") && to.equals("ZONA_FORMAS")) { // Si va de entrada a zona de formas
            baseDuration = 1.8; // Duración base de 1.8 minutos simulados
        } else if (from.equals("ENTRADA") && (to.equals("SALA_SILLAS") || to.equals("SALA_DE_PIE"))) { // Si va de entrada a salas
            baseDuration = 1.5; // Duración base de 1.5 minutos simulados
        } else if (from.equals("ZONA_FORMAS")) { // Si viene de zona de formas
            baseDuration = 1.3; // Duración base de 1.3 minutos simulados
        } else if (from.equals("SALA_DE_PIE") && to.equals("SALA_SILLAS")) { // Si va de sala de pie a sillas
            baseDuration = 1.0; // Duración base de 1.0 minuto simulado
        } else if (from.equals("SALA_SILLAS") && (to.equals("SERVIDOR_1") || to.equals("SERVIDOR_2"))) { // Si va de sillas a servidor
            baseDuration = 1.6; // Duración base de 1.6 minutos simulados
        } else { // Para todos los demás casos
            baseDuration = 0.8 + (distance / 320.0); // Duración basada en distancia
        }

        double slowFactor = computeAnimationSlowFactor(); // Calcula factor de ralentización de animación
        return baseDuration * slowFactor; // Retorna duración base multiplicada por factor
    }

    private double computeAnimationSlowFactor() { // Método privado que calcula factor de ralentización basado en velocidad de simulación
        double currentSpeed = engine.getSimulationSpeed(); // Obtiene velocidad actual de simulación
        if (currentSpeed <= 0) { // Si la velocidad es 0 o negativa
            return 12.0; // Retorna factor máximo de ralentización
        }
        double factor = 18.0 / currentSpeed; // Calcula factor inversamente proporcional a velocidad
        if (factor < 2.5) { // Si el factor es muy bajo
            factor = 2.5; // Establece mínimo en 2.5
        } else if (factor > 12.0) { // Si el factor es muy alto
            factor = 12.0; // Establece máximo en 12.0
        }
        return factor; // Retorna el factor calculado
    }

    private void drawStationaryEntities(GraphicsContext gc, Map<String, List<Entity>> grouped) { // Método privado que dibuja entidades estacionarias (no en tránsito)
        if (grouped == null || grouped.isEmpty()) { // Si el mapa es null o está vacío
            return; // Sale del método sin hacer nada
        }

        for (Map.Entry<String, List<Entity>> entry : grouped.entrySet()) { // Itera sobre cada entrada del mapa
            List<Entity> entities = entry.getValue(); // Obtiene lista de entidades de esta locación
            if (entities == null || entities.isEmpty()) { // Si la lista es null o vacía
                continue; // Salta a la siguiente iteración
            }
            entities.sort(Comparator.comparingInt(Entity::getId)); // Ordena entidades por ID
            drawEntitiesForLocation(gc, entry.getKey(), entities); // Dibuja las entidades de esta locación
        }
    }

    private void drawEntitiesForLocation(GraphicsContext gc, String location, List<Entity> entities) { // Método privado que dibuja entidades en una locación específica
        double[] basePos = locationPositions.get(location); // Obtiene posición base de la locación
        if (basePos == null || entities.isEmpty()) { // Si la posición es null o no hay entidades
            return; // Sale del método sin hacer nada
        }

        if ("ZONA_FORMAS".equals(location)) { // Si la locación es zona de formas
            drawZonaFormasEntities(gc, basePos, entities); // Dibuja entidades con estilo especial de formularios
            return; // Sale del método
        }

        int columns = getColumnsForLocation(location); // Obtiene número de columnas para esta locación
        int rows = Math.max(1, (int) Math.ceil((double) entities.size() / columns)); // Calcula filas necesarias

        double padding = 18; // Define padding interno en píxeles
        double availableWidth = BOX_SIZE - padding * 2; // Calcula ancho disponible
        double availableHeight = BOX_SIZE - padding * 2 - 16; // Calcula alto disponible dejando espacio para contador
        if (location.startsWith("SERVIDOR")) { // Si es un servidor
            availableHeight = BOX_SIZE - padding * 2; // Usa todo el alto disponible
        } else if ("SALA_DE_PIE".equals(location)) { // Si es sala de pie
            availableHeight = BOX_SIZE - padding * 1.5; // Usa padding reducido
        }

        double cellWidth = availableWidth / Math.max(1, columns); // Calcula ancho de cada celda
        double cellHeight = availableHeight / Math.max(1, rows); // Calcula alto de cada celda

        for (int index = 0; index < entities.size(); index++) { // Itera sobre cada entidad
            int row = index / columns; // Calcula número de fila
            int col = index % columns; // Calcula número de columna

            double centerX = basePos[0] + padding + col * cellWidth + cellWidth / 2; // Calcula centro X
            double centerY = basePos[1] + padding + row * cellHeight + cellHeight / 2; // Calcula centro Y

            drawStationaryEntity(gc, centerX, centerY, entities.get(index), location); // Dibuja la entidad en esta posición
        }
    }

    private void drawZonaFormasEntities(GraphicsContext gc, double[] basePos, List<Entity> entities) { // Método privado que dibuja entidades en zona de formas como hojas de papel
        double paddingX = 12; // Padding horizontal en píxeles
        double paddingY = 14; // Padding vertical en píxeles
        double paperWidth = 16; // Ancho de cada hoja de papel
        double paperHeight = 20; // Alto de cada hoja de papel
        double spacingX = 6; // Espaciado horizontal entre hojas
        double spacingY = 6; // Espaciado vertical entre hojas
        int columns = 5; // Número de columnas de hojas

        double originX = basePos[0] + paddingX; // Calcula origen X
        double originY = basePos[1] + BOX_SIZE - paddingY - paperHeight; // Calcula origen Y (desde abajo)

        for (int index = 0; index < entities.size(); index++) { // Itera sobre cada entidad
            int row = index / columns; // Calcula número de fila
            int col = index % columns; // Calcula número de columna

            double x = originX + col * (paperWidth + spacingX); // Calcula posición X de la hoja
            double y = originY - row * (paperHeight + spacingY); // Calcula posición Y de la hoja (creciendo hacia arriba)

            gc.setFill(Color.rgb(255, 253, 231, 0.92)); // Establece color amarillo pálido para fondo de hoja
            gc.fillRoundRect(x, y, paperWidth, paperHeight, 4, 4); // Dibuja rectángulo redondeado como hoja

            gc.setStroke(Color.rgb(255, 213, 79)); // Establece color amarillo para borde
            gc.setLineWidth(1.5); // Establece grosor del borde
            gc.strokeRoundRect(x, y, paperWidth, paperHeight, 4, 4); // Dibuja borde de la hoja

            gc.setStroke(Color.rgb(158, 158, 158)); // Establece color gris para líneas de texto
            gc.setLineWidth(1); // Establece grosor de líneas
            gc.strokeLine(x + 3, y + 7, x + paperWidth - 3, y + 7); // Dibuja primera línea de texto simulada
            gc.strokeLine(x + 3, y + 11, x + paperWidth - 3, y + 11); // Dibuja segunda línea de texto simulada

            gc.setFill(Color.rgb(121, 85, 72)); // Establece color café para el ID
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 9)); // Establece fuente pequeña y negrita
            gc.setTextAlign(TextAlignment.CENTER); // Establece alineación al centro
            gc.fillText(String.valueOf(entities.get(index).getId()), x + paperWidth / 2.0, y + paperHeight - 5); // Dibuja el ID en la parte inferior
        }
    }

    private int getColumnsForLocation(String location) { // Método privado que retorna número de columnas para organizar entidades según locación
        switch (location) { // Evalúa el nombre de la locación
            case "ENTRADA": return 3; // 3 columnas para entrada
            case "ZONA_FORMAS": return 4; // 4 columnas para zona de formas
            case "SALA_SILLAS": return 8; // 8 columnas para sala de sillas
            case "SALA_DE_PIE": return 12; // 12 columnas para sala de pie
            case "SERVIDOR_1": // Para servidor 1
            case "SERVIDOR_2": return 1; // 1 columna (centrado)
            default: return 4; // 4 columnas por defecto
        }
    }

    private void drawStationaryEntity(GraphicsContext gc, double centerX, double centerY, Entity entity, String location) { // Método privado que dibuja una entidad estacionaria individual
        double baseSize; // Declara variable para tamaño base
        if (location.startsWith("SERVIDOR")) { // Si está en un servidor
            baseSize = 26; // Tamaño grande para visibilidad
        } else if ("SALA_SILLAS".equals(location)) { // Si está en sala de sillas
            baseSize = 18; // Tamaño mediano
        } else if ("SALA_DE_PIE".equals(location)) { // Si está en sala de pie
            baseSize = 14; // Tamaño pequeño (más entidades)
        } else { // Para otras locaciones
            baseSize = 16; // Tamaño estándar
        }

        double size = baseSize; // Asigna el tamaño calculado

        gc.setFill(Color.rgb(0, 0, 0, 0.25)); // Establece color negro semitransparente para sombra
        gc.fillOval(centerX - size / 2 + 2, centerY - size / 2 + 2, size, size); // Dibuja círculo desplazado como sombra

        boolean blocked = entity.isBlocked(); // Verifica si la entidad está bloqueada
        Color fillColor; // Declara variable para color de relleno
        if (location.startsWith("SERVIDOR")) { // Si está en servidor
            fillColor = Color.rgb(255, 214, 102); // Amarillo dorado
        } else if (blocked) { // Si está bloqueada
            fillColor = Color.rgb(255, 111, 0); // Naranja intenso
        } else if ("ZONA_FORMAS".equals(location)) { // Si está en zona de formas
            fillColor = Color.rgb(255, 213, 79); // Amarillo
        } else if ("ENTRADA".equals(location)) { // Si está en entrada
            fillColor = Color.rgb(129, 199, 132); // Verde claro
        } else { // Para otras locaciones
            fillColor = Color.rgb(33, 150, 243); // Azul
        }

        gc.setFill(fillColor); // Establece el color de relleno calculado
        gc.fillOval(centerX - size / 2, centerY - size / 2, size, size); // Dibuja círculo principal

        Color borderColor = blocked ? Color.rgb(183, 28, 28) : Color.rgb(25, 118, 210); // Color de borde según estado bloqueado
        gc.setStroke(borderColor); // Establece el color de trazo
        gc.setLineWidth(2); // Establece grosor del borde en 2 píxeles
        gc.strokeOval(centerX - size / 2, centerY - size / 2, size, size); // Dibuja el borde del círculo

        gc.setFill(Color.WHITE); // Establece color blanco para el ID
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10)); // Establece fuente pequeña y negrita
        gc.setTextAlign(TextAlignment.CENTER); // Establece alineación al centro
        gc.fillText(String.valueOf(entity.getId()), centerX, centerY + 3); // Dibuja el ID centrado en el círculo
    }

    private void drawVirtualTransitEntities(GraphicsContext gc) { // Método privado que dibuja entidades en tránsito entre locaciones
        for (VirtualTransit vt : virtualTransits) { // Itera sobre cada transición virtual
            double[] fromPos = getLocationExitPoint(vt.from); // Obtiene punto de salida de locación origen
            double[] toPos = getLocationEntryPoint(vt.to); // Obtiene punto de entrada de locación destino

            if (fromPos != null && toPos != null) { // Si ambos puntos existen
                double t = vt.progress; // Obtiene el progreso actual (0.0 a 1.0)
                double smoothProgress = t < 0.5  // Aplica interpolación ease-in-out para movimiento más natural
                    ? 2 * t * t  // Primera mitad: aceleración
                    : 1 - Math.pow(-2 * t + 2, 2) / 2; // Segunda mitad: desaceleración
                
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * smoothProgress; // Calcula posición X interpolada
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * smoothProgress; // Calcula posición Y interpolada
                
                Color baseColor = locationColors.getOrDefault(vt.to, Color.rgb(255, 215, 0)); // Obtiene color de destino o dorado
                drawMovingPiece(gc, x, y, vt.entityId, baseColor); // Dibuja la pieza en movimiento
            }
        }
    }

    private double[] getLocationExitPoint(String location) { // Método privado que retorna punto de salida de una locación
        double[] pos = locationPositions.get(location); // Obtiene posición de la locación
        if (pos == null) return null; // Si es null, retorna null

        if (location.equals("ENTRADA")) { // Si es entrada
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE}; // Sale por abajo (centro horizontal)
        }
        if (location.equals("ZONA_FORMAS")) { // Si es zona de formas
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Sale por la derecha (centro vertical)
        }
        if (location.equals("SALA_DE_PIE")) { // Si es sala de pie
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Sale por arriba (centro horizontal)
        }
        if (location.equals("SALA_SILLAS")) { // Si es sala de sillas
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Sale por la derecha (centro vertical)
        }
        
        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2}; // Por defecto, sale por la derecha
    }

    private double[] getLocationEntryPoint(String location) { // Método privado que retorna punto de entrada de una locación
        double[] pos = locationPositions.get(location); // Obtiene posición de la locación
        if (pos == null) return null; // Si es null, retorna null

        if (location.equals("ZONA_FORMAS")) { // Si es zona de formas
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]}; // Entra por arriba (centro horizontal)
        }
        if (location.equals("SALA_SILLAS")) { // Si es sala de sillas
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entra por la izquierda (centro vertical)
        }
        if (location.equals("SALA_DE_PIE")) { // Si es sala de pie
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entra por la izquierda (centro vertical)
        }
        if (location.equals("SERVIDOR_1") || location.equals("SERVIDOR_2")) { // Si es un servidor
            return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Entra por la izquierda (centro vertical)
        }
        
        return new double[]{pos[0], pos[1] + BOX_SIZE / 2}; // Por defecto, entra por la izquierda
    }

    private void drawMovingPiece(GraphicsContext gc, double x, double y, int entityId, Color baseColor) { // Método privado que dibuja una pieza en movimiento con efectos visuales
        double pieceSize = 28; // Tamaño grande y visible para entidades en movimiento
        
        double pulseEffect = Math.sin(gearRotation * 2) * 0.1 + 1.0; // Calcula efecto de pulsación usando rotación de engranaje
        double actualSize = pieceSize * pulseEffect; // Aplica efecto de pulsación al tamaño

        for (int i = 3; i >= 1; i--) { // Itera 3 veces para crear triple halo
            double haloSize = actualSize * (1.2 + i * 0.3); // Calcula tamaño de cada halo
            double alpha = 0.15 / i; // Calcula transparencia del halo
            gc.setFill(Color.rgb(255, 215, 0, alpha)); // Establece color dorado con transparencia
            gc.fillOval(x - haloSize/2, y - haloSize/2, haloSize, haloSize); // Dibuja círculo de halo
        }

        gc.setFill(Color.rgb(0, 0, 0, 0.5)); // Establece color negro semitransparente para sombra profunda
        gc.fillOval(x - actualSize/2 + 4, y - actualSize/2 + 4, actualSize, actualSize); // Dibuja círculo desplazado como sombra

        gc.setFill(Color.rgb(255, 215, 0)); // Establece color dorado brillante para cuerpo principal
        gc.fillOval(x - actualSize/2, y - actualSize/2, actualSize, actualSize); // Dibuja círculo principal dorado
        
        gc.setFill(Color.rgb(255, 255, 200, 0.6)); // Establece color amarillo claro semitransparente para highlight
        gc.fillOval(x - actualSize/3, y - actualSize/3, actualSize/2, actualSize/2); // Dibuja círculo de highlight (simula luz)

        gc.setStroke(Color.rgb(204, 140, 0)); // Establece color dorado oscuro para borde exterior
        gc.setLineWidth(3.5); // Establece grosor grueso del borde
        gc.strokeOval(x - actualSize/2, y - actualSize/2, actualSize, actualSize); // Dibuja borde exterior
        
        gc.setStroke(Color.rgb(255, 240, 150)); // Establece color amarillo claro para borde interior
        gc.setLineWidth(1.5); // Establece grosor fino del borde interior
        gc.strokeOval(x - actualSize/2 + 2, y - actualSize/2 + 2, actualSize - 4, actualSize - 4); // Dibuja borde interior
        
        gc.setFill(Color.rgb(255, 215, 0, 0.2)); // Establece color dorado muy transparente para estela
        gc.fillOval(x - actualSize, y - actualSize, actualSize * 2, actualSize * 2); // Dibuja círculo grande como estela de movimiento
        
        gc.setFill(Color.WHITE); // Establece color blanco para centro indicador
        double centerDot = actualSize / 5; // Calcula tamaño del punto central
        gc.fillOval(x - centerDot/2, y - centerDot/2, centerDot, centerDot); // Dibuja punto blanco central
    }

    private void drawGlobalInfo(GraphicsContext gc) { // Método privado que dibuja el panel de información global del sistema
        double infoX = 50; // Define posición X del panel (izquierda)
        double infoY = 800; // Define posición Y del panel (abajo)
        double infoWidth = 600; // Define ancho del panel en píxeles
        double infoHeight = 130; // Define alto del panel en píxeles

        gc.setFill(Color.rgb(255, 255, 255, 0.98)); // Establece color blanco casi opaco para fondo
        gc.fillRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja rectángulo redondeado como fondo

        gc.setStroke(Color.rgb(200, 200, 200)); // Establece color gris claro para borde
        gc.setLineWidth(2); // Establece grosor del borde en 2 píxeles
        gc.strokeRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12); // Dibuja el borde del panel

        gc.setFill(Color.rgb(33, 33, 33)); // Establece color gris muy oscuro para texto del título
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16)); // Establece fuente Arial negrita tamaño 16
        gc.setTextAlign(TextAlignment.LEFT); // Establece alineación a la izquierda
        gc.fillText("📊 Estadísticas en Tiempo Real", infoX + 15, infoY + 30); // Dibuja título del panel

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13)); // Establece fuente Arial normal tamaño 13

        double currentTime = getCurrentTimeFromEngine(); // Obtiene tiempo actual de simulación
        int totalMinutes = (int) Math.floor(currentTime); // Convierte a minutos enteros
        int hours = totalMinutes / 60; // Calcula horas dividiendo entre 60
        int minutes = totalMinutes % 60; // Calcula minutos restantes usando módulo

        gc.fillText(String.format("⏱ Tiempo: %02d:%02d h", hours, minutes), infoX + 15, infoY + 60); // Dibuja tiempo formateado

        int totalArrivals = getStatisticsFromEngine().getTotalArrivals(); // Obtiene total de arribos desde estadísticas
        gc.fillText("📥 Arribos: " + totalArrivals, infoX + 15, infoY + 85); // Dibuja total de arribos

        int totalExits = getStatisticsFromEngine().getTotalExits(); // Obtiene total de salidas desde estadísticas
        gc.fillText("📤 Completadas: " + totalExits, infoX + 250, infoY + 60); // Dibuja total de completadas

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0; // Calcula throughput en entidades por hora o 0
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput), infoX + 250, infoY + 85); // Dibuja throughput formateado

        int inSystem = totalArrivals - totalExits; // Calcula entidades actualmente en sistema
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 250, infoY + 110); // Dibuja entidades en sistema
    } // Cierre del método drawGlobalInfo

    public void reset() { // Método público que reinicia el panel de animación a su estado inicial
        virtualTransits.clear(); // Limpia la lista de transiciones virtuales
        visualLocations.clear(); // Limpia el mapa de locaciones visuales
        activeTransitEntities.clear(); // Limpia el conjunto de entidades en tránsito
        gearRotation = 0; // Reinicia el ángulo de rotación a 0
        resetZoom(); // Reinicia el nivel de zoom a normal (100%)
        render(); // Llama al método render para redibujar el canvas limpio
    } // Cierre del método reset
    
    public void addVirtualTransit(int entityId, String from, String to) { // Método público que agrega una transición virtual manualmente
        if (from != null && to != null && !from.equals(to)) { // Verifica que origen y destino sean válidos y diferentes
            virtualTransits.add(new VirtualTransit(entityId, from, to)); // Crea y agrega nueva transición virtual a la lista
        }
    }

    private static class VirtualTransit { // Clase estática interna que representa una transición virtual entre locaciones
        int entityId; // ID de la entidad que está en tránsito
        String from; // Nombre de la locación de origen
        String to; // Nombre de la locación de destino
        double progress; // Progreso del tránsito de 0.0 (inicio) a 1.0 (completado)
        double startTime; // Tiempo de simulación cuando inició el tránsito
        double estimatedDuration; // Duración estimada en minutos de simulación

        VirtualTransit(int entityId, String from, String to) { // Constructor de la transición virtual
            this.entityId = entityId; // Asigna el ID de la entidad
            this.from = from; // Asigna la locación de origen
            this.to = to; // Asigna la locación de destino
            this.progress = 0; // Inicializa el progreso en 0 (inicio)
            this.startTime = 0; // Inicializa el tiempo de inicio en 0
            this.estimatedDuration = 0.2; // Establece duración estimada por defecto en 0.2 minutos
        }
    }
}
