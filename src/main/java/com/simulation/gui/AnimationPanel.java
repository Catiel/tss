package com.simulation.gui;

import com.simulation.core.Entity;
import com.simulation.core.SimulationEngine;
import com.simulation.resources.InspectionStation;
import com.simulation.resources.Location;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.*;

/**
 * Panel de animación con contadores en tiempo real
 */
public class AnimationPanel extends Pane {
    private Canvas canvas;
    private SimulationEngine engine;

    private static final double WIDTH = 1600;
    private static final double HEIGHT = 700;
    private static final double BOX_SIZE = 100;
    private static final double VERTICAL_SPACING = 200;
    private static final double COUNTER_WIDTH = 120;
    private static final double COUNTER_HEIGHT = 80;

    private Map<String, double[]> locationPositions;
    private Map<String, Color> locationColors;
    private Map<String, String> locationIcons;

    private List<VirtualTransit> virtualTransits;
    private double gearRotation = 0;

    public AnimationPanel(SimulationEngine engine) {
        this.engine = engine;
        this.canvas = new Canvas(WIDTH, HEIGHT);
        this.locationPositions = new HashMap<>();
        this.locationColors = new HashMap<>();
        this.locationIcons = new HashMap<>();
        this.virtualTransits = new ArrayList<>();

        initializePositions();
        initializeColors();
        initializeIcons();

        getChildren().add(canvas);
        setMinSize(WIDTH, HEIGHT);
        setPrefSize(WIDTH, HEIGHT);
        setMaxSize(WIDTH, HEIGHT);
    }

    private void initializePositions() {
        double topY = 150;
        double bottomY = topY + VERTICAL_SPACING;

        locationPositions.put("RECEPCION", new double[]{50, topY});
        locationPositions.put("LAVADORA", new double[]{280, topY});
        locationPositions.put("ALMACEN_PINTURA", new double[]{510, topY});
        locationPositions.put("PINTURA", new double[]{740, topY});

        locationPositions.put("ALMACEN_HORNO", new double[]{280, bottomY});
        locationPositions.put("HORNO", new double[]{510, bottomY});
        locationPositions.put("INSPECCION_1", new double[]{780, bottomY});
        locationPositions.put("INSPECCION_2", new double[]{930, bottomY});
    }

    private void initializeColors() {
        locationColors.put("RECEPCION", Color.rgb(100, 181, 246));
        locationColors.put("LAVADORA", Color.rgb(129, 199, 132));
        locationColors.put("ALMACEN_PINTURA", Color.rgb(255, 245, 157));
        locationColors.put("PINTURA", Color.rgb(255, 167, 38));
        locationColors.put("ALMACEN_HORNO", Color.rgb(255, 224, 178));
        locationColors.put("HORNO", Color.rgb(239, 83, 80));
        locationColors.put("INSPECCION_1", Color.rgb(189, 189, 189));
        locationColors.put("INSPECCION_2", Color.rgb(189, 189, 189));
    }

    private void initializeIcons() {
        locationIcons.put("RECEPCION", "📦");
        locationIcons.put("LAVADORA", "🧼");
        locationIcons.put("ALMACEN_PINTURA", "📦");
        locationIcons.put("PINTURA", "🎨");
        locationIcons.put("ALMACEN_HORNO", "📦");
        locationIcons.put("HORNO", "🔥");
        locationIcons.put("INSPECCION_1", "🔍");
        locationIcons.put("INSPECCION_2", "🔍");
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        // Fondo
        gc.setFill(Color.rgb(240, 242, 245));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitle(gc);
        drawConnections(gc);
        drawAllLocations(gc);
        drawCounters(gc); // NUEVO: Contadores en tiempo real

        detectVirtualTransits();
        drawTransitEntities(gc);
        drawVirtualTransitEntities(gc);

        drawGlobalInfo(gc);

        gearRotation += 0.05;
        if (gearRotation > 2 * Math.PI) {
            gearRotation = 0;
        }
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("🏭 SIMULACIÓN DE LÍNEA DE PRODUCCIÓN", WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.rgb(100, 100, 100));
        gc.fillText("Modelo ProModel - Java Implementation", WIDTH / 2, 60);
    }

    private void drawConnections(GraphicsContext gc) {
        gc.setStroke(Color.rgb(120, 120, 140));
        gc.setLineWidth(3);
        gc.setLineDashes(5, 5);

        drawConnection(gc, "RECEPCION", "LAVADORA");
        drawConnection(gc, "LAVADORA", "ALMACEN_PINTURA");
        drawConnection(gc, "ALMACEN_PINTURA", "PINTURA");
        drawConnectionCurved(gc, "PINTURA", "ALMACEN_HORNO");
        drawConnection(gc, "ALMACEN_HORNO", "HORNO");
        drawConnectionToInspection(gc, "HORNO", "INSPECCION_1");

        gc.setLineDashes(null);
    }

    private void drawConnection(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0] + BOX_SIZE;
        double y1 = pos1[1] + BOX_SIZE / 2;
        double x2 = pos2[0];
        double y2 = pos2[1] + BOX_SIZE / 2;

        gc.strokeLine(x1, y1, x2, y2);
        drawArrow(gc, x1, y1, x2, y2);
    }

    private void drawConnectionCurved(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0] + BOX_SIZE / 2;
        double y1 = pos1[1] + BOX_SIZE;
        double x2 = pos2[0] + BOX_SIZE / 2;
        double y2 = pos2[1];

        gc.beginPath();
        gc.moveTo(x1, y1);
        gc.bezierCurveTo(x1, y1 + 50, x2, y2 - 50, x2, y2);
        gc.stroke();
        drawArrow(gc, x2, y2 - 20, x2, y2);
    }

    private void drawConnectionToInspection(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0] + BOX_SIZE;
        double y1 = pos1[1] + BOX_SIZE / 2;
        double x2 = pos2[0];
        double y2 = pos2[1] + BOX_SIZE / 2;

        gc.strokeLine(x1, y1, x2, y2);

        double[] pos3 = locationPositions.get("INSPECCION_2");
        if (pos3 != null) {
            double x3 = pos3[0];
            double y3 = pos3[1] + BOX_SIZE / 2;
            gc.strokeLine(x1, y1, x3, y3);
        }
    }

    private void drawArrow(GraphicsContext gc, double x1, double y1, double x2, double y2) {
        double arrowLength = 12;
        double angle = Math.atan2(y2 - y1, x2 - x1);

        double x3 = x2 - arrowLength * Math.cos(angle - Math.PI / 6);
        double y3 = y2 - arrowLength * Math.sin(angle - Math.PI / 6);
        double x4 = x2 - arrowLength * Math.cos(angle + Math.PI / 6);
        double y4 = y2 - arrowLength * Math.sin(angle + Math.PI / 6);

        gc.setFill(Color.rgb(120, 120, 140));
        gc.fillPolygon(new double[]{x2, x3, x4}, new double[]{y2, y3, y4}, 3);
    }

    private void drawAllLocations(GraphicsContext gc) {
        drawLocation(gc, "RECEPCION", engine.getLocation("RECEPCION"));
        drawLocation(gc, "LAVADORA", engine.getLocation("LAVADORA"));
        drawLocation(gc, "ALMACEN_PINTURA", engine.getLocation("ALMACEN_PINTURA"));
        drawLocation(gc, "PINTURA", engine.getLocation("PINTURA"));
        drawLocation(gc, "ALMACEN_HORNO", engine.getLocation("ALMACEN_HORNO"));
        drawLocation(gc, "HORNO", engine.getLocation("HORNO"));
        drawInspectionStations(gc);
    }

    private void drawLocation(GraphicsContext gc, String name, Location location) {
        if (location == null) return;

        double[] pos = locationPositions.get(name);
        if (pos == null) return;

        Color color = locationColors.get(name);
        String icon = locationIcons.get(name);

        int currentContent = location.getCurrentContent();
        int capacity = location.getCapacity();
        int queueSize = location.getQueueSize();

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.15));
        gc.fillRoundRect(pos[0] + 4, pos[1] + 4, BOX_SIZE, BOX_SIZE, 12, 12);

        // Caja principal
        gc.setFill(color);
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        gc.setStroke(color.darker());
        gc.setLineWidth(3);
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        // Icono o engranaje
        if (currentContent > 0 && !name.contains("ALMACEN")) {
            drawGear(gc, pos[0] + BOX_SIZE / 2, pos[1] + 35, 18);
        } else {
            gc.setFont(Font.font("Segoe UI Emoji", 28));
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + 40);
        }

        // Nombre
        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        String displayName = name.replace("_", " ");
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 25);

        // Capacidad actual
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setFill(Color.WHITE);
        String contentText = currentContent + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity);
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + 75);

        // Cola de espera
        if (queueSize > 0) {
            drawQueueIndicator(gc, pos[0], pos[1], queueSize);
        }

        // Barra de utilización
        double utilization = location.getUtilization(engine.getCurrentTime());
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization);

        // Piezas en la locación
        drawEntitiesInLocation(gc, pos[0], pos[1], currentContent, capacity);
    }

    private void drawGear(GraphicsContext gc, double centerX, double centerY, double radius) {
        int teeth = 8;
        double innerRadius = radius * 0.6;
        double toothHeight = radius * 0.3;

        gc.save();
        gc.translate(centerX, centerY);
        gc.rotate(Math.toDegrees(gearRotation));

        gc.setFill(Color.rgb(120, 120, 120));
        gc.setStroke(Color.rgb(80, 80, 80));
        gc.setLineWidth(2);

        gc.beginPath();
        for (int i = 0; i < teeth * 2; i++) {
            double angle = (2 * Math.PI / (teeth * 2)) * i;
            double r = (i % 2 == 0) ? radius + toothHeight : radius;
            double x = r * Math.cos(angle);
            double y = r * Math.sin(angle);

            if (i == 0) gc.moveTo(x, y);
            else gc.lineTo(x, y);
        }
        gc.closePath();
        gc.fill();
        gc.stroke();

        gc.setFill(Color.rgb(200, 200, 200));
        gc.fillOval(-innerRadius / 2, -innerRadius / 2, innerRadius, innerRadius);

        gc.restore();
    }

    private void drawInspectionStations(GraphicsContext gc) {
        InspectionStation inspeccion = (InspectionStation) engine.getLocation("INSPECCION");
        if (inspeccion == null) return;

        int totalContent = inspeccion.getCurrentContent();
        int totalQueue = inspeccion.getQueueSize();
        int totalEntries = inspeccion.getTotalEntries();

        int content1 = Math.min(1, totalContent);
        int content2 = Math.max(0, totalContent - 1);

        drawInspectionStation(gc, "INSPECCION_1", content1, 1, totalQueue, totalEntries, inspeccion);
        drawInspectionStation(gc, "INSPECCION_2", content2, 1, 0, 0, inspeccion);
    }

    private void drawInspectionStation(GraphicsContext gc, String name, int content,
                                      int capacity, int queueSize, int totalEntries,
                                      InspectionStation inspeccion) {
        double[] pos = locationPositions.get(name);
        if (pos == null) return;

        Color color = locationColors.get(name);
        String icon = locationIcons.get(name);

        gc.setFill(Color.rgb(0, 0, 0, 0.15));
        gc.fillRoundRect(pos[0] + 4, pos[1] + 4, BOX_SIZE, BOX_SIZE, 12, 12);

        gc.setFill(color);
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        gc.setStroke(color.darker());
        gc.setLineWidth(3);
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        if (content > 0) {
            drawGear(gc, pos[0] + BOX_SIZE / 2, pos[1] + 35, 18);
        } else {
            gc.setFont(Font.font("Segoe UI Emoji", 28));
            gc.setFill(Color.WHITE);
            gc.setTextAlign(TextAlignment.CENTER);
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + 40);
        }

        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        String displayName = name.equals("INSPECCION_1") ? "INSPECCIÓN Mesa 1" : "INSPECCIÓN Mesa 2";
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 25);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        gc.setFill(Color.WHITE);
        String contentText = content + "/" + capacity;
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + 75);

        if (queueSize > 0 && name.equals("INSPECCION_1")) {
            drawQueueIndicator(gc, pos[0], pos[1], queueSize);
        }

        double utilization = inspeccion.getUtilization(engine.getCurrentTime()) / 2;
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization);

        if (content > 0) {
            drawEntitiesInLocation(gc, pos[0], pos[1], content, capacity);
        }
    }

    private void drawQueueIndicator(GraphicsContext gc, double x, double y, int queueSize) {
        double badgeX = x + BOX_SIZE - 35;
        double badgeY = y - 10;
        double badgeSize = 30;

        gc.setFill(Color.rgb(244, 67, 54));
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(queueSize), badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 5);

        gc.setFill(Color.rgb(244, 67, 54));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.fillText("COLA", x + BOX_SIZE / 2, y + BOX_SIZE + 40);
    }

    private void drawUtilizationBar(GraphicsContext gc, double x, double y, double width, double utilization) {
        double barHeight = 10;

        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillRoundRect(x, y, width, barHeight, 5, 5);

        double fillWidth = width * (utilization / 100.0);

        Color fillColor;
        if (utilization < 50) fillColor = Color.rgb(76, 175, 80);
        else if (utilization < 80) fillColor = Color.rgb(255, 193, 7);
        else fillColor = Color.rgb(244, 67, 54);

        gc.setFill(fillColor);
        gc.fillRoundRect(x, y, fillWidth, barHeight, 5, 5);

        gc.setFill(Color.rgb(60, 60, 60));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.format("%.0f%%", utilization), x + width / 2, y + barHeight + 15);
    }

    private void drawEntitiesInLocation(GraphicsContext gc, double x, double y, int count, int capacity) {
        if (count == 0 || capacity == Integer.MAX_VALUE) return;

        int maxDisplay = Math.min(count, capacity);
        int cols = (int) Math.ceil(Math.sqrt(capacity));
        int rows = (int) Math.ceil((double) capacity / cols);

        double pieceSize = Math.min((BOX_SIZE - 20) / cols, (BOX_SIZE - 20) / rows) * 0.65;
        double offsetX = x + (BOX_SIZE - cols * pieceSize) / 2;
        double offsetY = y + (BOX_SIZE - rows * pieceSize) / 2 + 10;

        int drawn = 0;
        for (int i = 0; i < rows && drawn < maxDisplay; i++) {
            for (int j = 0; j < cols && drawn < maxDisplay; j++) {
                double px = offsetX + j * pieceSize + pieceSize / 4;
                double py = offsetY + i * pieceSize + pieceSize / 4;

                gc.setFill(Color.rgb(0, 0, 0, 0.3));
                gc.fillOval(px + 2, py + 2, pieceSize / 2, pieceSize / 2);

                gc.setFill(Color.rgb(33, 150, 243));
                gc.fillOval(px, py, pieceSize / 2, pieceSize / 2);

                gc.setStroke(Color.rgb(25, 118, 210));
                gc.setLineWidth(1.5);
                gc.strokeOval(px, py, pieceSize / 2, pieceSize / 2);

                drawn++;
            }
        }
    }

    /**
     * NUEVO: Dibuja contadores de estadísticas en tiempo real
     */
    private void drawCounters(GraphicsContext gc) {
        double startX = 1150;
        double startY = 130;
        double spacing = COUNTER_HEIGHT + 15;

        String[] locations = {"LAVADORA", "ALMACEN_PINTURA", "PINTURA",
                             "ALMACEN_HORNO", "HORNO", "INSPECCION"};

        for (int i = 0; i < locations.length; i++) {
            Location loc = engine.getLocation(locations[i]);
            if (loc != null) {
                drawCounter(gc, startX, startY + i * spacing, locations[i], loc);
            }
        }
    }

    /**
     * Dibuja un contador individual para una locación
     */
    private void drawCounter(GraphicsContext gc, double x, double y, String name, Location location) {
        // Fondo del contador
        gc.setFill(Color.rgb(255, 255, 255, 0.95));
        gc.fillRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 8, 8);

        gc.setStroke(locationColors.get(name));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 8, 8);

        // Nombre de la locación
        gc.setFill(Color.rgb(50, 50, 50));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.LEFT);
        String displayName = name.replace("_", " ");
        gc.fillText(displayName, x + 5, y + 15);

        // Estadísticas
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 9));

        int entries = location.getTotalEntries();
        gc.fillText("Entradas: " + entries, x + 5, y + 30);

        double util = location.getUtilization(engine.getCurrentTime());
        gc.fillText(String.format("Util: %.1f%%", util), x + 5, y + 45);

        int queue = location.getQueueSize();
        gc.fillText("Cola: " + queue, x + 5, y + 60);

        double avgContent = location.getAverageContent(engine.getCurrentTime());
        gc.fillText(String.format("Prom: %.1f", avgContent), x + 5, y + 75);
    }

    private void detectVirtualTransits() {
        virtualTransits.removeIf(vt -> {
            vt.progress += 0.08;
            return vt.progress >= 1.0;
        });

        List<Entity> allEntities = engine.getAllActiveEntities();
        for (Entity entity : allEntities) {
            if (entity == null || entity.isInTransit()) continue;

            String currentLoc = entity.getCurrentLocation();
            if (currentLoc == null) continue;

            if (currentLoc.equals("ALMACEN_PINTURA") &&
                engine.getLocation("PINTURA").getCurrentContent() < engine.getLocation("PINTURA").getCapacity()) {
                boolean exists = virtualTransits.stream().anyMatch(vt -> vt.entityId == entity.getId());
                if (!exists) {
                    virtualTransits.add(new VirtualTransit(entity.getId(), "ALMACEN_PINTURA", "PINTURA"));
                }
            }
            else if (currentLoc.equals("ALMACEN_HORNO") &&
                     engine.getLocation("HORNO").getCurrentContent() < engine.getLocation("HORNO").getCapacity()) {
                boolean exists = virtualTransits.stream().anyMatch(vt -> vt.entityId == entity.getId());
                if (!exists) {
                    virtualTransits.add(new VirtualTransit(entity.getId(), "ALMACEN_HORNO", "HORNO"));
                }
            }
        }
    }

    private void drawTransitEntities(GraphicsContext gc) {
        List<Entity> allEntities = engine.getAllActiveEntities();
        if (allEntities == null) return;

        double currentTime = engine.getCurrentTime();

        for (Entity entity : allEntities) {
            if (entity == null || !entity.isInTransit()) continue;

            String from = entity.getCurrentLocation();
            String to = entity.getDestinationLocation();
            if (from == null || to == null) continue;

            double progress = entity.getTransitProgress(currentTime);

            double[] fromPos = getLocationExitPoint(from);
            double[] toPos = getLocationEntryPoint(to);

            if (fromPos != null && toPos != null) {
                double x, y;

                if (from.equals("PINTURA") && to.equals("ALMACEN_HORNO")) {
                    double[] curvePos = getCurvePosition(fromPos, toPos, progress);
                    x = curvePos[0];
                    y = curvePos[1];
                } else if (to.equals("INSPECCION")) {
                    toPos = getLocationEntryPoint("INSPECCION_1");
                    if (toPos == null) continue;
                    x = fromPos[0] + (toPos[0] - fromPos[0]) * progress;
                    y = fromPos[1] + (toPos[1] - fromPos[1]) * progress;
                } else {
                    x = fromPos[0] + (toPos[0] - fromPos[0]) * progress;
                    y = fromPos[1] + (toPos[1] - fromPos[1]) * progress;
                }

                drawMovingPiece(gc, x, y, entity.getId());
            }
        }
    }

    private void drawVirtualTransitEntities(GraphicsContext gc) {
        for (VirtualTransit vt : virtualTransits) {
            double[] fromPos = getLocationExitPoint(vt.from);
            double[] toPos = getLocationEntryPoint(vt.to);

            if (fromPos != null && toPos != null) {
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * vt.progress;
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * vt.progress;
                drawMovingPiece(gc, x, y, vt.entityId);
            }
        }
    }

    private double[] getLocationExitPoint(String location) {
        if (location.equals("PINTURA")) {
            double[] pos = locationPositions.get(location);
            if (pos == null) return null;
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE};
        }

        double[] pos = locationPositions.get(location);
        if (pos == null) return null;
        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2};
    }

    private double[] getLocationEntryPoint(String location) {
        if (location.equals("ALMACEN_HORNO")) {
            double[] pos = locationPositions.get(location);
            if (pos == null) return null;
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]};
        }

        if (location.equals("INSPECCION")) {
            location = "INSPECCION_1";
        }

        double[] pos = locationPositions.get(location);
        if (pos == null) return null;
        return new double[]{pos[0], pos[1] + BOX_SIZE / 2};
    }

    private double[] getCurvePosition(double[] from, double[] to, double t) {
        double midX = (from[0] + to[0]) / 2;
        double controlY = from[1] + 80;

        double x = (1 - t) * (1 - t) * from[0] +
                   2 * (1 - t) * t * midX +
                   t * t * to[0];

        double y = (1 - t) * (1 - t) * from[1] +
                   2 * (1 - t) * t * controlY +
                   t * t * to[1];

        return new double[]{x, y};
    }

    private void drawMovingPiece(GraphicsContext gc, double x, double y, int entityId) {
        double pieceSize = 16;

        gc.setFill(Color.rgb(0, 0, 0, 0.3));
        gc.fillOval(x - pieceSize/2 + 2, y - pieceSize/2 + 2, pieceSize, pieceSize);

        gc.setFill(Color.rgb(33, 150, 243));
        gc.fillOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize);

        gc.setStroke(Color.rgb(25, 118, 210));
        gc.setLineWidth(2);
        gc.strokeOval(x - pieceSize/2, y - pieceSize/2, pieceSize, pieceSize);
    }

    private void drawGlobalInfo(GraphicsContext gc) {
        double infoX = WIDTH - 320;
        double infoY = 90;
        double infoWidth = 300;
        double infoHeight = 180;

        gc.setFill(Color.rgb(255, 255, 255, 0.98));
        gc.fillRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12);

        gc.setStroke(Color.rgb(200, 200, 200));
        gc.setLineWidth(2);
        gc.strokeRoundRect(infoX, infoY, infoWidth, infoHeight, 12, 12);

        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 16));
        gc.setTextAlign(TextAlignment.LEFT);
        gc.fillText("📊 Estadísticas en Tiempo Real", infoX + 15, infoY + 30);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 13));

        double currentTime = engine.getCurrentTime();
        int days = (int) (currentTime / (24 * 60));
        int hours = (int) ((currentTime % (24 * 60)) / 60);
        int minutes = (int) (currentTime % 60);

        gc.fillText(String.format("⏱ Tiempo: %d días %02d:%02d", days, hours, minutes),
                   infoX + 15, infoY + 60);

        int totalArrivals = engine.getStatistics().getTotalArrivals();
        gc.fillText("📥 Arribos: " + totalArrivals, infoX + 15, infoY + 85);

        int totalExits = engine.getStatistics().getTotalExits();
        gc.fillText("📤 Completadas: " + totalExits, infoX + 15, infoY + 110);

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0;
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput),
                   infoX + 15, infoY + 135);

        int inSystem = totalArrivals - totalExits;
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 15, infoY + 160);
    }

    public void reset() {
        virtualTransits.clear();
        gearRotation = 0;
        render();
    }

    private static class VirtualTransit {
        int entityId;
        String from;
        String to;
        double progress;

        VirtualTransit(int entityId, String from, String to) {
            this.entityId = entityId;
            this.from = from;
            this.to = to;
            this.progress = 0;
        }
    }
}