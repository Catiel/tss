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
 * Panel MEJORADO - Muestra TODAS las locaciones aunque no existan en el motor
 */
public class AnimationPanel extends Pane {
    private Canvas canvas;
    private SimulationEngine engine;

    private static final double WIDTH = 1600;
    private static final double HEIGHT = 950;
    private static final double BOX_SIZE = 90;
    private static final double COUNTER_WIDTH = 150;
    private static final double COUNTER_HEIGHT = 48;
    private static final double COUNTER_START_X = 1330;
    private static final double COUNTER_START_Y = 80;

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
        double y1 = 100;
        double spacing = 220;

        locationPositions.put("CONVEYOR_1", new double[]{60, y1});
        locationPositions.put("ALMACEN", new double[]{60 + spacing, y1});
        locationPositions.put("CORTADORA", new double[]{60 + spacing * 2, y1});
        locationPositions.put("TORNO", new double[]{60 + spacing * 3, y1});

        double y2 = 320;
        locationPositions.put("CONVEYOR_2", new double[]{60 + spacing * 3, y2});
        locationPositions.put("FRESADORA", new double[]{60 + spacing * 2, y2});
        locationPositions.put("ALMACEN_2", new double[]{60 + spacing, y2});
        locationPositions.put("PINTURA", new double[]{60, y2});

        double y3 = 540;
        locationPositions.put("INSPECCION_1", new double[]{60, y3});

        double y4 = 680;
        locationPositions.put("INSPECCION_2", new double[]{60, y4});

        locationPositions.put("EMPAQUE", new double[]{330, 610});
        locationPositions.put("EMBARQUE", new double[]{600, 610});
    }

    private void initializeColors() {
        locationColors.put("CONVEYOR_1", Color.rgb(96, 125, 139));
        locationColors.put("ALMACEN", Color.rgb(255, 241, 118));
        locationColors.put("CORTADORA", Color.rgb(239, 83, 80));
        locationColors.put("TORNO", Color.rgb(129, 199, 132));
        locationColors.put("CONVEYOR_2", Color.rgb(96, 125, 139));
        locationColors.put("FRESADORA", Color.rgb(156, 39, 176));
        locationColors.put("ALMACEN_2", Color.rgb(255, 241, 118));
        locationColors.put("PINTURA", Color.rgb(255, 167, 38));
        locationColors.put("INSPECCION_1", Color.rgb(189, 189, 189));
        locationColors.put("INSPECCION_2", Color.rgb(189, 189, 189));
        locationColors.put("EMPAQUE", Color.rgb(121, 85, 72));
        locationColors.put("EMBARQUE", Color.rgb(33, 150, 243));
    }

    private void initializeIcons() {
        locationIcons.put("CONVEYOR_1", "→");
        locationIcons.put("ALMACEN", "📦");
        locationIcons.put("CORTADORA", "✂");
        locationIcons.put("TORNO", "⚙");
        locationIcons.put("CONVEYOR_2", "→");
        locationIcons.put("FRESADORA", "🔧");
        locationIcons.put("ALMACEN_2", "📦");
        locationIcons.put("PINTURA", "🎨");
        locationIcons.put("INSPECCION_1", "🔍");
        locationIcons.put("INSPECCION_2", "🔍");
        locationIcons.put("EMPAQUE", "📦");
        locationIcons.put("EMBARQUE", "🚚");
    }

    public void render() {
        GraphicsContext gc = canvas.getGraphicsContext2D();

        gc.setFill(Color.rgb(240, 242, 245));
        gc.fillRect(0, 0, WIDTH, HEIGHT);

        drawTitle(gc);
        drawConnections(gc);
        drawAllLocations(gc);
        drawCounters(gc);

        detectVirtualTransits();
        drawTransitEntities(gc);
        drawVirtualTransitEntities(gc);

        drawGlobalInfo(gc);
        drawExitLabel(gc);

        gearRotation += 0.05;
        if (gearRotation > 2 * Math.PI) {
            gearRotation = 0;
        }
    }

    private void drawTitle(GraphicsContext gc) {
        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("⚙️ SISTEMA DE FABRICACIÓN DE ENGRANES - MULTI-ENGRANE", WIDTH / 2, 35);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
        gc.setFill(Color.rgb(100, 100, 100));
        gc.fillText("Flujo: Conveyor→Almacén→Corte→Torno→Conveyor→Fresado→Almacén→Pintura→Inspección→Empaque→Embarque", WIDTH / 2, 60);
    }

    private void drawConnections(GraphicsContext gc) {
        gc.setStroke(Color.rgb(120, 120, 140));
        gc.setLineWidth(3);
        gc.setLineDashes(5, 5);

        drawConnection(gc, "CONVEYOR_1", "ALMACEN");
        drawConnection(gc, "ALMACEN", "CORTADORA");
        drawConnection(gc, "CORTADORA", "TORNO");
        drawConnectionVertical(gc, "TORNO", "CONVEYOR_2");
        drawConnectionReverse(gc, "FRESADORA", "CONVEYOR_2");
        drawConnectionReverse(gc, "ALMACEN_2", "FRESADORA");
        drawConnectionReverse(gc, "PINTURA", "ALMACEN_2");
        drawConnectionVertical(gc, "PINTURA", "INSPECCION_1");
        drawConnectionVertical(gc, "INSPECCION_1", "INSPECCION_2");
        drawConnectionDiagonal(gc, "INSPECCION_1", "EMPAQUE");
        drawConnectionDiagonal(gc, "INSPECCION_2", "EMPAQUE");
        drawConnection(gc, "EMPAQUE", "EMBARQUE");
        drawExitArrow(gc);

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

    private void drawConnectionVertical(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0] + BOX_SIZE / 2;
        double y1 = pos1[1] + BOX_SIZE;
        double x2 = pos2[0] + BOX_SIZE / 2;
        double y2 = pos2[1];

        gc.strokeLine(x1, y1, x2, y2);
        drawArrow(gc, x2, y2 - 20, x2, y2);
    }

    private void drawConnectionReverse(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0];
        double y1 = pos1[1] + BOX_SIZE / 2;
        double x2 = pos2[0] + BOX_SIZE;
        double y2 = pos2[1] + BOX_SIZE / 2;

        gc.strokeLine(x1, y1, x2, y2);
        drawArrow(gc, x1 + 20, y1, x1, y1);
    }

    private void drawConnectionDiagonal(GraphicsContext gc, String from, String to) {
        double[] pos1 = locationPositions.get(from);
        double[] pos2 = locationPositions.get(to);
        if (pos1 == null || pos2 == null) return;

        double x1 = pos1[0] + BOX_SIZE;
        double y1 = pos1[1] + BOX_SIZE / 2;
        double x2 = pos2[0];
        double y2 = pos2[1] + BOX_SIZE / 2;

        gc.strokeLine(x1, y1, x2, y2);
        drawArrow(gc, x1 + (x2 - x1) * 0.8, y1 + (y2 - y1) * 0.8, x2, y2);
    }

    private void drawExitArrow(GraphicsContext gc) {
        double[] embarquePos = locationPositions.get("EMBARQUE");
        if (embarquePos == null) return;

        double x1 = embarquePos[0] + BOX_SIZE;
        double y1 = embarquePos[1] + BOX_SIZE / 2;
        double x2 = x1 + 100;
        double y2 = y1;

        gc.strokeLine(x1, y1, x2, y2);
        drawArrow(gc, x1, y1, x2, y2);
    }

    private void drawExitLabel(GraphicsContext gc) {
        double[] embarquePos = locationPositions.get("EMBARQUE");
        if (embarquePos == null) return;

        double x = embarquePos[0] + BOX_SIZE + 110;
        double y = embarquePos[1] + BOX_SIZE / 2;

        gc.setFill(Color.rgb(76, 175, 80));
        gc.fillRoundRect(x, y - 30, 100, 60, 12, 12);

        gc.setStroke(Color.rgb(56, 142, 60));
        gc.setLineWidth(3);
        gc.strokeRoundRect(x, y - 30, 100, 60, 12, 12);

        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText("EXIT", x + 50, y + 5);
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
        // DIBUJAR TODAS las locaciones, existan o no en el motor
        String[] allLocations = {
            "CONVEYOR_1", "ALMACEN", "CORTADORA", "TORNO",
            "CONVEYOR_2", "FRESADORA", "ALMACEN_2", "PINTURA",
            "INSPECCION_1", "INSPECCION_2", "EMPAQUE", "EMBARQUE"
        };

        for (String name : allLocations) {
            Location location = engine.getLocation(name);
            // SIEMPRE dibujar, aunque location sea null
            drawLocationSafe(gc, name, location);
        }
    }

    /**
     * Versión SEGURA que dibuja la locación aunque sea null
     */
    private void drawLocationSafe(GraphicsContext gc, String name, Location location) {
        double[] pos = locationPositions.get(name);
        if (pos == null) return;

        Color color = locationColors.get(name);
        String icon = locationIcons.get(name);

        // Si location es null, usar valores por defecto
        int currentContent = location != null ? location.getCurrentContent() : 0;
        int capacity = location != null ? location.getCapacity() : Integer.MAX_VALUE;
        int queueSize = location != null ? location.getQueueSize() : 0;

        // Sombra
        gc.setFill(Color.rgb(0, 0, 0, 0.2));
        gc.fillRoundRect(pos[0] + 5, pos[1] + 5, BOX_SIZE, BOX_SIZE, 12, 12);

        // Caja principal
        gc.setFill(color);
        gc.fillRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        gc.setStroke(color.darker());
        gc.setLineWidth(4);
        gc.strokeRoundRect(pos[0], pos[1], BOX_SIZE, BOX_SIZE, 12, 12);

        // ICONO - SIEMPRE visible
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFill(Color.WHITE);

        if (name.contains("CONVEYOR")) {
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 48));
            gc.fillText("→", pos[0] + BOX_SIZE / 2, pos[1] + 55);
        } else {
            gc.setFont(Font.font("Segoe UI Emoji", 32));
            gc.fillText(icon, pos[0] + BOX_SIZE / 2, pos[1] + 42);
        }

        // Nombre
        gc.setFill(Color.rgb(33, 33, 33));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        String displayName = getDisplayName(name);
        gc.fillText(displayName, pos[0] + BOX_SIZE / 2, pos[1] - 12);

        // Capacidad
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        gc.setFill(Color.WHITE);
        String contentText = currentContent + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity);
        gc.fillText(contentText, pos[0] + BOX_SIZE / 2, pos[1] + 78);

        // Cola
        if (queueSize > 0) {
            drawQueueIndicator(gc, pos[0], pos[1], queueSize);
        }

        // Barra de utilización
        double utilization = location != null ? location.getUtilization(engine.getCurrentTime()) : 0;
        drawUtilizationBar(gc, pos[0], pos[1] + BOX_SIZE + 8, BOX_SIZE, utilization);

        // Piezas (solo para no-conveyors)
        if (!name.contains("CONVEYOR") && location != null) {
            drawEntitiesInLocation(gc, pos[0], pos[1], currentContent, capacity);
        }
    }

    private String getDisplayName(String name) {
        switch (name) {
            case "ALMACEN": return "ALMACEN 1";
            case "ALMACEN_2": return "ALMACEN 2";
            case "CONVEYOR_1": return "CONVEYOR 1";
            case "CONVEYOR_2": return "CONVEYOR 2";
            case "INSPECCION_1": return "INSPECCION 1";
            case "INSPECCION_2": return "INSPECCION 2";
            default: return name;
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

    private void drawCounters(GraphicsContext gc) {
        double startX = COUNTER_START_X;
        double startY = COUNTER_START_Y;
        double spacing = COUNTER_HEIGHT + 6;

        String[] locations = {
            "CONVEYOR_1", "ALMACEN", "CORTADORA", "TORNO",
            "CONVEYOR_2", "FRESADORA", "ALMACEN_2", "PINTURA",
            "INSPECCION_1", "INSPECCION_2", "EMPAQUE", "EMBARQUE"
        };

        for (int i = 0; i < locations.length; i++) {
            Location loc = engine.getLocation(locations[i]);
            // SIEMPRE dibujar contador, aunque loc sea null
            drawCounterSafe(gc, startX, startY + i * spacing, locations[i], loc);
        }
    }

    private void drawCounterSafe(GraphicsContext gc, double x, double y, String name, Location location) {
        gc.setFill(Color.rgb(0, 0, 0, 0.08));
        gc.fillRoundRect(x + 1, y + 1, COUNTER_WIDTH, COUNTER_HEIGHT, 6, 6);

        gc.setFill(Color.rgb(255, 255, 255, 0.98));
        gc.fillRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 6, 6);

        gc.setStroke(locationColors.get(name));
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, COUNTER_WIDTH, COUNTER_HEIGHT, 6, 6);

        gc.setFill(Color.rgb(30, 30, 30));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 8.5));
        gc.setTextAlign(TextAlignment.LEFT);

        String displayName = getDisplayName(name);
        if (displayName.length() > 15) {
            displayName = displayName.substring(0, 13) + "..";
        }
        gc.fillText(displayName, x + 5, y + 11);

        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 7.5));
        gc.setFill(Color.rgb(50, 50, 50));

        int entries = location != null ? location.getTotalEntries() : 0;
        gc.fillText("E:" + entries, x + 5, y + 22);

        double util = location != null ? location.getUtilization(engine.getCurrentTime()) : 0;
        gc.fillText(String.format("U:%.0f%%", util), x + 5, y + 32);

        int queue = location != null ? location.getQueueSize() : 0;
        gc.fillText("C:" + queue, x + 80, y + 22);

        double avgContent = location != null ? location.getAverageContent(engine.getCurrentTime()) : 0;
        gc.fillText(String.format("P:%.1f", avgContent), x + 80, y + 32);

        double barWidth = COUNTER_WIDTH - 10;
        double barHeight = 3;
        double barY = y + COUNTER_HEIGHT - 8;

        gc.setFill(Color.rgb(220, 220, 220));
        gc.fillRoundRect(x + 5, barY, barWidth, barHeight, 1.5, 1.5);

        double fillWidth = barWidth * (util / 100.0);
        Color barColor;
        if (util < 40) barColor = Color.rgb(76, 175, 80);
        else if (util < 80) barColor = Color.rgb(255, 152, 0);
        else barColor = Color.rgb(244, 67, 54);

        gc.setFill(barColor);
        gc.fillRoundRect(x + 5, barY, fillWidth, barHeight, 1.5, 1.5);
    }

    private void detectVirtualTransits() {
        virtualTransits.removeIf(vt -> {
            vt.progress += 0.08;
            return vt.progress >= 1.0;
        });
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
                double x = fromPos[0] + (toPos[0] - fromPos[0]) * progress;
                double y = fromPos[1] + (toPos[1] - fromPos[1]) * progress;
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
        double[] pos = locationPositions.get(location);
        if (pos == null) return null;

        if (location.equals("TORNO") || location.equals("PINTURA") || location.equals("INSPECCION_1")) {
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1] + BOX_SIZE};
        }

        return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2};
    }

    private double[] getLocationEntryPoint(String location) {
        double[] pos = locationPositions.get(location);
        if (pos == null) return null;

        if (location.equals("CONVEYOR_2") || location.equals("INSPECCION_1") || location.equals("INSPECCION_2")) {
            return new double[]{pos[0] + BOX_SIZE / 2, pos[1]};
        }

        if (location.equals("FRESADORA") || location.equals("ALMACEN_2") || location.equals("PINTURA")) {
            return new double[]{pos[0] + BOX_SIZE, pos[1] + BOX_SIZE / 2};
        }

        return new double[]{pos[0], pos[1] + BOX_SIZE / 2};
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
        double infoX = 50;
        double infoY = 800;
        double infoWidth = 600;
        double infoHeight = 130;

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
        gc.fillText("📤 Completadas: " + totalExits, infoX + 250, infoY + 60);

        double throughput = currentTime > 0 ? (totalExits / currentTime) * 60 : 0;
        gc.fillText(String.format("⚡ Throughput: %.2f/hora", throughput),
                   infoX + 250, infoY + 85);

        int inSystem = totalArrivals - totalExits;
        gc.fillText("🔄 En sistema: " + inSystem, infoX + 250, infoY + 110);
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
