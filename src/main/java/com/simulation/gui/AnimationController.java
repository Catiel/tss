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
        // Fila superior - Recepción y procesamiento inicial
        locationNodes.put("SILO_GRANDE", new LocationNode("SILO_GRANDE", 50, 50, 80, 60, Color.SADDLEBROWN));
        locationNodes.put("MALTEADO", new LocationNode("MALTEADO", 160, 50, 80, 60, Color.ORANGE));
        locationNodes.put("SECADO", new LocationNode("SECADO", 270, 50, 80, 60, Color.ORANGERED));
        locationNodes.put("MOLIENDA", new LocationNode("MOLIENDA", 380, 50, 80, 60, Color.BROWN));

        // Fila media - Macerado y cocción
        locationNodes.put("MACERADO", new LocationNode("MACERADO", 50, 150, 80, 60, Color.CHOCOLATE));
        locationNodes.put("FILTRADO", new LocationNode("FILTRADO", 160, 150, 80, 60, Color.DARKGOLDENROD));
        locationNodes.put("COCCION", new LocationNode("COCCION", 270, 150, 100, 80, Color.DARKRED));

        // Silos auxiliares
        locationNodes.put("SILO_LUPULO", new LocationNode("SILO_LUPULO", 400, 150, 80, 60, Color.DARKGREEN));
        locationNodes.put("SILO_LEVADURA", new LocationNode("SILO_LEVADURA", 520, 150, 80, 60, Color.YELLOW));

        // Fila inferior - Fermentación y maduración
        locationNodes.put("ENFRIAMIENTO", new LocationNode("ENFRIAMIENTO", 50, 270, 90, 60, Color.LIGHTBLUE));
        locationNodes.put("FERMENTACION", new LocationNode("FERMENTACION", 170, 270, 100, 80, Color.PURPLE));
        locationNodes.put("MADURACION", new LocationNode("MADURACION", 300, 270, 100, 80, Color.DARKVIOLET));

        // Fila embotellado
        locationNodes.put("INSPECCION", new LocationNode("INSPECCION", 50, 390, 80, 60, Color.LIGHTGREEN));
        locationNodes.put("EMBOTELLADO", new LocationNode("EMBOTELLADO", 160, 390, 90, 60, Color.CYAN));
        locationNodes.put("ETIQUETADO", new LocationNode("ETIQUETADO", 280, 390, 90, 60, Color.LIGHTCORAL));

        // Fila empacado
        locationNodes.put("ALMACEN_CAJAS", new LocationNode("ALMACEN_CAJAS", 50, 510, 90, 60, Color.BURLYWOOD));
        locationNodes.put("EMPACADO", new LocationNode("EMPACADO", 170, 510, 80, 60, Color.CORAL));
        locationNodes.put("ALMACENAJE", new LocationNode("ALMACENAJE", 280, 510, 90, 60, Color.DARKKHAKI));
        locationNodes.put("MERCADO", new LocationNode("MERCADO", 400, 510, 100, 80, Color.GOLD));

        // Recursos (operadores y camión)
        resourceSprites.put("OPERADOR_RECEPCION", new ResourceSprite("OPERADOR_RECEPCION", 50, 600, Color.BLUE));
        resourceSprites.put("OPERADOR_LUPULO", new ResourceSprite("OPERADOR_LUPULO", 120, 600, Color.GREEN));
        resourceSprites.put("OPERADOR_LEVADURA", new ResourceSprite("OPERADOR_LEVADURA", 190, 600, Color.YELLOW));
        resourceSprites.put("OPERADOR_EMPACADO", new ResourceSprite("OPERADOR_EMPACADO", 260, 600, Color.ORANGE));
        resourceSprites.put("CAMION", new ResourceSprite("CAMION", 330, 600, Color.RED));
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
        drawLine("SILO_GRANDE", "MALTEADO");
        drawLine("MALTEADO", "SECADO");
        drawLine("SECADO", "MOLIENDA");
        drawLine("MOLIENDA", "MACERADO");
        drawLine("MACERADO", "FILTRADO");
        drawLine("FILTRADO", "COCCION");
        drawLine("SILO_LUPULO", "COCCION");
        drawLine("COCCION", "ENFRIAMIENTO");
        drawLine("ENFRIAMIENTO", "FERMENTACION");
        drawLine("SILO_LEVADURA", "FERMENTACION");
        drawLine("FERMENTACION", "MADURACION");
        drawLine("MADURACION", "INSPECCION");
        drawLine("INSPECCION", "EMBOTELLADO");
        drawLine("EMBOTELLADO", "ETIQUETADO");
        drawLine("ETIQUETADO", "EMPACADO");
        drawLine("ALMACEN_CAJAS", "EMPACADO");
        drawLine("EMPACADO", "ALMACENAJE");
        drawLine("ALMACENAJE", "MERCADO");
    }

    private void drawLine(String from, String to) {
        LocationNode fromNode = locationNodes.get(from);
        LocationNode toNode = locationNodes.get(to);

        if (fromNode != null && toNode != null) {
            gc.strokeLine(
                    fromNode.getCenterX(), fromNode.getCenterY(),
                    toNode.getCenterX(), toNode.getCenterY()
            );
        }
    }

    public void animateEntityMovement(Entity entity, String fromLocation, String toLocation,
                                      String resourceName, Runnable onComplete) {
        LocationNode from = locationNodes.get(fromLocation);
        LocationNode to = locationNodes.get(toLocation);

        if (from == null || to == null) return;

        EntitySprite sprite = entitySprites.computeIfAbsent(
                entity.getId(),
                id -> new EntitySprite(entity, from.getCenterX(), from.getCenterY())
        );

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

