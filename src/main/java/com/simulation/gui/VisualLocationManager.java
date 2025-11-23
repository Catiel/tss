package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.locations.Location;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

import java.util.Map;

/**
 * Gestor visual para renderizar locaciones con diseño atractivo
 */
public class VisualLocationManager {

    private final Map<String, javafx.geometry.Point2D> positions;
    private static final double LOCATION_SIZE = 80;

    public VisualLocationManager(Map<String, javafx.geometry.Point2D> positions) {
        this.positions = positions;
    }

    public void render(GraphicsContext gc, SimulationEngine engine) {
        for (Map.Entry<String, javafx.geometry.Point2D> entry : positions.entrySet()) {
            String locationName = entry.getKey();
            javafx.geometry.Point2D pos = entry.getValue();

            Location location = engine.getLocation(locationName);
            if (location != null) {
                renderLocation(gc, locationName, pos, location);
            }
        }
    }

    private final Map<String, Integer> totalEntries = new java.util.concurrent.ConcurrentHashMap<>();

    public void setTotalEntries(String locationName, int count) {
        totalEntries.put(locationName, count);
    }

    private void renderLocation(GraphicsContext gc, String name, javafx.geometry.Point2D pos, Location location) {
        int currentOccupancy = location.getCurrentOccupancy();
        int capacity = location.getType().capacity();
        int queueSize = location.getQueueSize();
        int totalIn = totalEntries.getOrDefault(name, 0);

        // Calcular color basado en utilización
        double utilization = capacity > 0 ? (double) currentOccupancy / capacity : 0;
        Color baseColor = getColorByUtilization(utilization, queueSize > 0);

        // Sombra suave
        gc.setEffect(new DropShadow(8, 3, 3, Color.color(0, 0, 0, 0.4)));

        // Cuerpo principal con gradiente simple
        LinearGradient gradient = new LinearGradient(
                pos.getX(), pos.getY(),
                pos.getX(), pos.getY() + LOCATION_SIZE,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, baseColor.brighter()),
                new Stop(1, baseColor.darker()));

        gc.setFill(gradient);
        gc.fillRoundRect(pos.getX(), pos.getY(), LOCATION_SIZE, LOCATION_SIZE, 12, 12);

        // Borde
        gc.setEffect(null);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeRoundRect(pos.getX(), pos.getY(), LOCATION_SIZE, LOCATION_SIZE, 12, 12);

        // Icono GRANDE y claro (emoji)
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 42));
        gc.setFill(Color.WHITE);
        String icon = getLocationIcon(name);
        gc.fillText(icon, pos.getX() + LOCATION_SIZE / 2, pos.getY() + LOCATION_SIZE / 2 + 15);

        // Dibujar nombre de la locación con fondo semitransparente
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillRoundRect(pos.getX() - 5, pos.getY() - 25, LOCATION_SIZE + 10, 20, 5, 5);
        gc.setFill(Color.web("#ECF0F1"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(TextAlignment.CENTER);
        String displayName = formatLocationName(name);
        gc.fillText(displayName, pos.getX() + LOCATION_SIZE / 2, pos.getY() - 10);

        // Visualizar entidades acumuladas CLARAMENTE en el centro
        if (currentOccupancy > 0 && currentOccupancy <= 10) {
            // Mostrar círculos individuales para pocas entidades
            renderAccumulatedEntitiesCircles(gc, pos, currentOccupancy, utilization);
        } else if (currentOccupancy > 0) {
            // Mostrar número grande para muchas entidades
            renderAccumulatedEntitiesCount(gc, pos, currentOccupancy);
        }

        // Nombre de la locación ARRIBA
        gc.setFill(Color.color(0, 0, 0, 0.85));
        gc.fillRoundRect(pos.getX() - 5, pos.getY() - 28, LOCATION_SIZE + 10, 22, 6, 6);
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        String locName = formatLocationName(name);
        gc.fillText(locName, pos.getX() + LOCATION_SIZE / 2, pos.getY() - 12);

        // Indicador de ocupación ABAJO con fondo oscuro - MÁS GRANDE Y VISIBLE
        gc.setFill(Color.color(0, 0, 0, 0.90));
        gc.fillRoundRect(pos.getX(), pos.getY() + LOCATION_SIZE + 6, LOCATION_SIZE, 28, 6, 6);

        // Etiqueta "PROCESANDO:" en tamaño pequeño
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setFill(Color.web("#FFA726")); // Naranja para llamar la atención
        gc.fillText("PROCESANDO:", pos.getX() + LOCATION_SIZE / 2, pos.getY() + LOCATION_SIZE + 18);

        // Número grande y claro
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(Color.YELLOW);
        String occupancyText = currentOccupancy + "/" + (capacity == Integer.MAX_VALUE ? "∞" : capacity);
        gc.fillText(occupancyText, pos.getX() + LOCATION_SIZE / 2, pos.getY() + LOCATION_SIZE + 30);

        // Contador TOTAL ENTRIES (Badge Azul en esquina superior derecha)
        if (totalIn > 0) {
            double badgeSize = 24;
            double badgeX = pos.getX() + LOCATION_SIZE - badgeSize / 2;
            double badgeY = pos.getY() - badgeSize / 2;

            gc.setFill(Color.web("#3498DB")); // Azul
            gc.fillOval(badgeX, badgeY, badgeSize, badgeSize);

            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize);

            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
            gc.fillText(String.valueOf(totalIn), badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 4);
        }

        // Dibujar cola si existe (badge animado)
        if (queueSize > 0) {
            gc.setFill(new RadialGradient(
                    0, 0,
                    pos.getX() + LOCATION_SIZE - 7.5, pos.getY() - 7.5,
                    15,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#FF6B6B")),
                    new Stop(1, Color.web("#E74C3C"))));
            gc.fillOval(pos.getX() + LOCATION_SIZE - 20, pos.getY() - 10, 30, 30);
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(pos.getX() + LOCATION_SIZE - 20, pos.getY() - 10, 30, 30);
            gc.setFill(Color.WHITE);
            gc.setFont(Font.font("Arial", FontWeight.BOLD, 13));
            gc.fillText(String.valueOf(queueSize), pos.getX() + LOCATION_SIZE - 5, pos.getY() + 8);
        }

        // Dibujar barra de capacidad 3D
        if (capacity < Integer.MAX_VALUE) {
            double barWidth = 65;
            double barHeight = 8;
            double barX = pos.getX() + (LOCATION_SIZE - barWidth) / 2;
            double barY = pos.getY() + LOCATION_SIZE + 30;

            // Sombra de la barra
            gc.setFill(Color.color(0, 0, 0, 0.3));
            gc.fillRoundRect(barX + 2, barY + 2, barWidth, barHeight, 4, 4);

            // Fondo de la barra
            gc.setFill(new LinearGradient(
                    barX, barY,
                    barX, barY + barHeight,
                    false,
                    CycleMethod.NO_CYCLE,
                    new Stop(0, Color.web("#2C3E50")),
                    new Stop(1, Color.web("#1A252F"))));
            gc.fillRoundRect(barX, barY, barWidth, barHeight, 4, 4);

            // Barra de progreso con gradiente
            double fillWidth = barWidth * utilization;
            if (fillWidth > 0) {
                Color barColor = getBarColor(utilization);
                gc.setFill(new LinearGradient(
                        barX, barY,
                        barX, barY + barHeight,
                        false,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, barColor.brighter()),
                        new Stop(1, barColor)));
                gc.fillRoundRect(barX, barY, fillWidth, barHeight, 4, 4);

                // Brillo en la barra
                gc.setFill(new LinearGradient(
                        barX, barY,
                        barX, barY + barHeight / 2,
                        false,
                        CycleMethod.NO_CYCLE,
                        new Stop(0, Color.color(1, 1, 1, 0.4)),
                        new Stop(1, Color.color(1, 1, 1, 0))));
                gc.fillRoundRect(barX, barY, fillWidth, barHeight / 2, 4, 4);
            }

            // Borde de la barra
            gc.setStroke(Color.web("#ECF0F1"));
            gc.setLineWidth(1);
            gc.strokeRoundRect(barX, barY, barWidth, barHeight, 4, 4);
        }
    }

    private Color getColorByUtilization(double utilization, boolean hasQueue) {
        if (hasQueue) {
            return Color.web("#E67E22"); // Naranja si hay cola
        } else if (utilization == 0) {
            return Color.web("#95A5A6"); // Gris si vacío
        } else if (utilization < 0.5) {
            return Color.web("#27AE60"); // Verde si baja utilización
        } else if (utilization < 0.8) {
            return Color.web("#F39C12"); // Amarillo si media utilización
        } else {
            return Color.web("#E74C3C"); // Rojo si alta utilización
        }
    }

    private Color getBarColor(double utilization) {
        if (utilization < 0.5) {
            return Color.web("#2ECC71");
        } else if (utilization < 0.8) {
            return Color.web("#F1C40F");
        } else {
            return Color.web("#E74C3C");
        }
    }

    private String getLocationIcon(String name) {
        switch (name) {
            case "SILO_GRANDE":
            case "SILO_LUPULO":
            case "SILO_LEVADURA":
                return "🏭";
            case "MALTEADO":
            case "SECADO":
            case "MOLIENDA":
                return "⚙️";
            case "MACERADO":
            case "FILTRADO":
                return "🌾";
            case "COCCION":
                return "🔥";
            case "ENFRIAMIENTO":
                return "❄️";
            case "FERMENTACION":
                return "🫧";
            case "MADURACION":
                return "⏱️";
            case "INSPECCION":
                return "🔍";
            case "EMBOTELLADO":
                return "🍾";
            case "ETIQUETADO":
                return "🏷️";
            case "EMPACADO":
                return "📦";
            case "ALMACEN_CAJAS":
                return "🗃️";
            case "ALMACENAJE":
                return "🏪";
            case "MERCADO":
                return "🛒";
            default:
                return "📍";
        }
    }

    /**
     * Renderizar círculos individuales para pocas entidades (1-10)
     */
    private void renderAccumulatedEntitiesCircles(GraphicsContext gc, javafx.geometry.Point2D pos,
            int occupancy, double utilization) {
        double dotSize = 10;
        double spacing = 4;
        int maxPerRow = 3;

        // Color según utilización
        Color dotColor;
        if (utilization < 0.5) {
            dotColor = Color.web("#3498DB"); // Azul
        } else if (utilization < 0.8) {
            dotColor = Color.web("#FFD700"); // Dorado
        } else {
            dotColor = Color.web("#FF6B6B"); // Rojo
        }

        // Calcular posición inicial centrada
        int rows = (int) Math.ceil((double) occupancy / maxPerRow);
        double totalHeight = rows * (dotSize + spacing) - spacing;
        double startY = pos.getY() + (LOCATION_SIZE - totalHeight) / 2;

        for (int i = 0; i < occupancy; i++) {
            int row = i / maxPerRow;
            int col = i % maxPerRow;
            int itemsInRow = Math.min(maxPerRow, occupancy - row * maxPerRow);
            double totalWidth = itemsInRow * (dotSize + spacing) - spacing;
            double startX = pos.getX() + (LOCATION_SIZE - totalWidth) / 2;

            double dotX = startX + col * (dotSize + spacing);
            double dotY = startY + row * (dotSize + spacing);

            // Sombra
            gc.setFill(Color.color(0, 0, 0, 0.3));
            gc.fillOval(dotX + 1, dotY + 1, dotSize, dotSize);

            // Círculo con gradiente
            RadialGradient gradient = new RadialGradient(
                    0, 0, dotX + dotSize / 3, dotY + dotSize / 3, dotSize,
                    false, CycleMethod.NO_CYCLE,
                    new Stop(0, dotColor.brighter()),
                    new Stop(1, dotColor));
            gc.setFill(gradient);
            gc.fillOval(dotX, dotY, dotSize, dotSize);

            // Borde blanco
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(2);
            gc.strokeOval(dotX, dotY, dotSize, dotSize);
        }
    }

    /**
     * Renderizar número grande para muchas entidades (>10)
     */
    private void renderAccumulatedEntitiesCount(GraphicsContext gc, javafx.geometry.Point2D pos,
            int occupancy) {
        double centerX = pos.getX() + LOCATION_SIZE / 2;
        double centerY = pos.getY() + LOCATION_SIZE / 2;

        // Círculo de fondo semitransparente
        gc.setFill(Color.color(0, 0, 0, 0.7));
        gc.fillOval(centerX - 22, centerY - 22, 44, 44);

        // Borde
        gc.setStroke(Color.YELLOW);
        gc.setLineWidth(3);
        gc.strokeOval(centerX - 22, centerY - 22, 44, 44);

        // Número grande
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 24));
        gc.setFill(Color.YELLOW);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(String.valueOf(occupancy), centerX, centerY + 8);
    }

    private String formatLocationName(String name) {
        return name.replace("_", " ");
    }

    public javafx.geometry.Point2D getLocationPosition(String name) {
        return positions.get(name);
    }
}
