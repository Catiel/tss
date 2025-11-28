package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.resources.Resource;
import javafx.geometry.Point2D;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.effect.DropShadow;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestor visual para recursos (operadores y camión)
 */
public class VisualResourceManager {

    private static final double RESOURCE_SIZE = 30;
    private final SimulationEngine engine;
    private final Map<String, ResourcePosition> resourcePositions = new HashMap<>();
    private final PathNetworkManager pathManager;

    public VisualResourceManager(SimulationEngine engine) {
        this.engine = engine;
        this.pathManager = new PathNetworkManager();
        initializePositions();
    }

    private void initializePositions() {
        // Posiciones iniciales basadas en el nodo Home (N1) de cada red
        for (String resourceName : engine.getAllResources().keySet()) {
            Point2D homePos = pathManager.getHomePosition(resourceName);
            if (homePos != null) {
                resourcePositions.put(resourceName, new ResourcePosition(homePos.getX(), homePos.getY()));
            } else {
                // Fallback si no tiene red definida
                resourcePositions.put(resourceName, new ResourcePosition(100, 100));
            }
        }
    }

    public void render(GraphicsContext gc, VisualEntityManager entityManager) {
        // 1. Actualizar recursos que están transportando entidades
        if (entityManager != null) {
            for (VisualEntityManager.ResourceTransport transport : entityManager.getTransportsInProgress().values()) {
                if (transport.resourceName == null)
                    continue;

                ResourcePosition pos = resourcePositions.get(transport.resourceName);
                if (pos != null) {
                    // El recurso se mueve EXACTAMENTE con la entidad
                    pos.x = transport.currentX;
                    pos.y = transport.currentY - 20; // Offset visual
                    pos.isBusy = true; // Marcar como ocupado visualmente
                }
            }
        }

        // 2. Mover recursos libres de vuelta a Home (si no están ocupados)
        for (Map.Entry<String, ResourcePosition> entry : resourcePositions.entrySet()) {
            String name = entry.getKey();
            ResourcePosition pos = entry.getValue();

            // Verificar si el recurso está realmente libre en el motor
            boolean isEngineFree = engine.getResource(name).isAvailable();

            // Si está libre y no está en Home, moverlo hacia Home
            if (isEngineFree) {
                pos.isBusy = false;
                Point2D home = pathManager.getHomePosition(name);
                if (home != null) {
                    // Movimiento simple hacia home (lerp)
                    double dx = home.getX() - pos.x;
                    double dy = home.getY() - pos.y;
                    if (Math.abs(dx) > 1 || Math.abs(dy) > 1) {
                        pos.x += dx * 0.05;
                        pos.y += dy * 0.05;
                    }
                }
            }
        }

        for (Resource resource : engine.getAllResources().values()) {
            renderResource(gc, resource);
        }
    }

    private void renderResource(GraphicsContext gc, Resource resource) {
        ResourcePosition pos = resourcePositions.get(resource.getName());
        if (pos == null)
            return;

        double x = pos.x;
        double y = pos.y;

        // Efecto sombra
        DropShadow shadow = new DropShadow();
        shadow.setRadius(5);
        shadow.setOffsetY(2);
        shadow.setColor(Color.rgb(0, 0, 0, 0.3));
        gc.setEffect(shadow);

        // Color según disponibilidad
        boolean isAvailable = resource.isAvailable();
        Color bgColor = isAvailable ? Color.web("#4CAF50") : Color.web("#F44336");

        // Fondo circular
        gc.setFill(bgColor);
        gc.fillOval(x - RESOURCE_SIZE / 2, y - RESOURCE_SIZE / 2, RESOURCE_SIZE, RESOURCE_SIZE);

        // Borde
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(3);
        gc.strokeOval(x - RESOURCE_SIZE / 2, y - RESOURCE_SIZE / 2, RESOURCE_SIZE, RESOURCE_SIZE);

        gc.setEffect(null);

        // Icono del recurso
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        String icon = getResourceIcon(resource.getName());
        gc.fillText(icon, x - 10, y + 7);

        // Etiqueta del recurso
        gc.setFill(Color.web("#263238"));
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        String label = formatResourceName(resource.getName());
        gc.fillText(label, x - 30, y + RESOURCE_SIZE);

        // Indicador de estado
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 9));
        String status = isAvailable ? "Disponible" : "Ocupado";
        gc.setFill(isAvailable ? Color.web("#388E3C") : Color.web("#D32F2F"));
        gc.fillText(status, x - 25, y + RESOURCE_SIZE + 12);

        // Contador de usos
        gc.setFill(Color.web("#757575"));
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 8));
        String usage = String.format("Usos: %d", resource.getStatistics().getTotalUsages());
        gc.fillText(usage, x - 20, y + RESOURCE_SIZE + 22);

        // Si está ocupado, dibujar línea al área de trabajo
        if (!isAvailable) {
            drawWorkingIndicator(gc, x, y, resource.getName());
        }
    }

    private void drawWorkingIndicator(GraphicsContext gc, double x, double y, String resourceName) {
        // Efecto de pulso para recurso ocupado
        gc.setGlobalAlpha(0.5);
        gc.setStroke(Color.web("#FF5722"));
        gc.setLineWidth(2);
        gc.setLineDashes(5, 5);

        // Línea hacia área de trabajo (simplificado)
        double targetX = x;
        double targetY = y - 50;
        gc.strokeLine(x, y - RESOURCE_SIZE / 2, targetX, targetY);

        gc.setLineDashes(0);
        gc.setGlobalAlpha(1.0);
    }

    private String getResourceIcon(String name) {
        if (name.contains("GRUA")) {
            return "🏗️";
        } else if (name.contains("ROBOT")) {
            return "🤖";
        } else {
            return "🔧";
        }
    }

    private String formatResourceName(String name) {
        String[] parts = name.split("_");
        StringBuilder formatted = new StringBuilder();
        for (String part : parts) {
            if (formatted.length() > 0)
                formatted.append(" ");
            formatted.append(part.charAt(0))
                    .append(part.substring(1).toLowerCase());
        }
        return formatted.toString();
    }

    /**
     * Clase interna para posición de recurso
     */
    private static class ResourcePosition {
        double x, y;
        boolean isBusy = false;

        ResourcePosition(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }
}
