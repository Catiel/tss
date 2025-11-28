package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class AnimationController {

    private final Canvas canvas;
    private final GraphicsContext gc;
    private final Map<String, LocationNode> locationNodes;
    private final Map<Integer, EntitySprite> entitySprites;
    private final Map<String, ResourceSprite> resourceSprites;
    private final List<PathAnimator> activeAnimations;
    private SimulationEngine engine;
    private AnimationTimer timer;
    private double simulationSpeed = 1.0;
    private boolean isRunning = false;
    private boolean isPaused = false;

    private double currentTime = 0;

    public AnimationController(double width, double height) {
        this.canvas = new Canvas(width, height);
        this.gc = canvas.getGraphicsContext2D();

        this.locationNodes = new ConcurrentHashMap<>();
        this.entitySprites = new ConcurrentHashMap<>();
        this.resourceSprites = new ConcurrentHashMap<>();
        this.activeAnimations = Collections.synchronizedList(new ArrayList<>());

        initializeLayout();
        setupAnimationTimer();
    }

    private void initializeLayout() {
        // Coordenadas de las locaciones en el canvas
        // Flujo principal
        locationNodes.put("ALMACEN_MP", new LocationNode("ALMACEN_MP", 50, 300, 80, 60, Color.SADDLEBROWN));
        locationNodes.put("HORNO", new LocationNode("HORNO", 250, 300, 100, 80, Color.ORANGERED));
        locationNodes.put("BANDA_1", new LocationNode("BANDA_1", 450, 300, 120, 40, Color.GRAY));
        locationNodes.put("CARGA", new LocationNode("CARGA", 650, 300, 80, 60, Color.LIGHTGRAY));

        // Celda de manufactura
        locationNodes.put("TORNEADO", new LocationNode("TORNEADO", 850, 150, 80, 60, Color.BLUE));
        locationNodes.put("FRESADO", new LocationNode("FRESADO", 1050, 150, 80, 60, Color.BLUEVIOLET));
        locationNodes.put("TALADRO", new LocationNode("TALADRO", 1050, 450, 80, 60, Color.CADETBLUE));
        locationNodes.put("RECTIFICADO", new LocationNode("RECTIFICADO", 850, 450, 80, 60, Color.CORNFLOWERBLUE));

        // Salida
        locationNodes.put("DESCARGA", new LocationNode("DESCARGA", 650, 500, 80, 60, Color.LIGHTGRAY));
        locationNodes.put("BANDA_2", new LocationNode("BANDA_2", 450, 500, 120, 40, Color.GRAY));
        locationNodes.put("INSPECCION", new LocationNode("INSPECCION", 250, 500, 80, 60, Color.GREEN));
        locationNodes.put("SALIDA", new LocationNode("SALIDA", 50, 500, 80, 60, Color.DARKGREEN));

        // Recursos
        resourceSprites.put("GRUA_VIAJERA", new ResourceSprite("GRUA_VIAJERA", 150, 100, Color.ORANGE));
        resourceSprites.put("ROBOT", new ResourceSprite("ROBOT", 850, 300, Color.RED));
    }

    private void setupAnimationTimer() {
        timer = new AnimationTimer() {
            private long lastUpdate = 0;

            @Override
            public void handle(long now) {
                if (lastUpdate == 0) {
                    lastUpdate = now;
                    return;
                }

                double deltaTime = (now - lastUpdate) / 1_000_000_000.0; // Convertir a segundos
                lastUpdate = now;

                if (!isPaused) {
                    update(deltaTime * simulationSpeed);
                }

                render();
            }
        };
    }

    private void update(double deltaTime) {
        currentTime += deltaTime * 60; // Convertir a minutos de simulación

        // Actualizar animaciones activas
        synchronized (activeAnimations) {
            Iterator<PathAnimator> iterator = activeAnimations.iterator();
            while (iterator.hasNext()) {
                PathAnimator animator = iterator.next();
                animator.update(deltaTime);

                if (animator.isFinished()) {
                    iterator.remove();
                }
            }
        }
    }

    private void render() {
        // Limpiar canvas
        gc.setFill(Color.LIGHTGRAY);
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());

        // Dibujar conexiones entre locaciones
        drawConnections();

        // Dibujar locaciones
        for (LocationNode node : locationNodes.values()) {
            node.draw(gc);
        }

        // Dibujar recursos
        for (ResourceSprite sprite : resourceSprites.values()) {
            sprite.draw(gc);
        }

        // Dibujar entidades en movimiento
        synchronized (entitySprites) {
            for (EntitySprite sprite : entitySprites.values()) {
                sprite.draw(gc);
            }
        }

        // Dibujar información de tiempo
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 14));
        gc.fillText(String.format("Tiempo de simulación: %.2f minutos", currentTime), 10, canvas.getHeight() - 10);
    }

    private void drawConnections() {
        gc.setStroke(Color.GRAY);
        gc.setLineWidth(2);

        // Dibujar líneas entre locaciones conectadas
        drawLine("ALMACEN_MP", "HORNO");
        drawLine("HORNO", "BANDA_1");
        drawLine("BANDA_1", "CARGA");

        // Flujo Robot
        drawLine("CARGA", "TORNEADO");
        drawLine("TORNEADO", "FRESADO");
        drawLine("FRESADO", "TALADRO");
        drawLine("TALADRO", "RECTIFICADO");
        drawLine("RECTIFICADO", "DESCARGA");

        drawLine("DESCARGA", "BANDA_2");
        drawLine("BANDA_2", "INSPECCION");
        drawLine("INSPECCION", "SALIDA");
    }

    private void drawLine(String from, String to) {
        LocationNode fromNode = locationNodes.get(from);
        LocationNode toNode = locationNodes.get(to);

        if (fromNode != null && toNode != null) {
            gc.strokeLine(
                    fromNode.getCenterX(), fromNode.getCenterY(),
                    toNode.getCenterX(), toNode.getCenterY());
        }
    }

    public void animateEntityMovement(Entity entity, String fromLocation, String toLocation,
            String resourceName, Runnable onComplete) {
        LocationNode from = locationNodes.get(fromLocation);
        LocationNode to = locationNodes.get(toLocation);

        if (from == null || to == null)
            return;

        EntitySprite sprite = entitySprites.computeIfAbsent(
                entity.getId(),
                id -> new EntitySprite(entity, from.getCenterX(), from.getCenterY()));

        PathAnimator animator;

        if (resourceName != null && !resourceName.isEmpty()) {
            // Movimiento con recurso
            ResourceSprite resource = resourceSprites.get(resourceName);
            animator = new PathAnimator(sprite, resource, from, to, 2.0, onComplete);
        } else {
            // Movimiento directo
            animator = new PathAnimator(sprite, from, to, 1.0, onComplete);
        }

        activeAnimations.add(animator);
    }

    public void start() {
        isRunning = true;
        isPaused = false;
        timer.start();
    }

    public void pause() {
        isPaused = true;
    }

    public void resume() {
        isPaused = false;
    }

    public void stop() {
        isRunning = false;
        timer.stop();
    }

    public void setSimulationSpeed(double speed) {
        this.simulationSpeed = speed;
    }

    public Canvas getCanvas() {
        return canvas;
    }

    public void setEngine(SimulationEngine engine) {
        this.engine = engine;
    }

    public Map<String, LocationNode> getLocationNodes() {
        return locationNodes;
    }
}
