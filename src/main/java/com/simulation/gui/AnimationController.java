package com.simulation.gui;

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import javafx.animation.AnimationTimer;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Controlador de animación mejorado con sistema multicapa profesional
 */
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
    private double animationTime = 0;

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
        animationTime += deltaTime;

        // Actualizar LocationNodes (animaciones, partículas, etc.)
        for (LocationNode node : locationNodes.values()) {
            node.update(deltaTime);
        }

        // Actualizar ResourceSprites (animaciones de grúa y robot)
        for (ResourceSprite sprite : resourceSprites.values()) {
            sprite.update(deltaTime);
        }

        // Actualizar EntitySprites que NO están en animación
        synchronized (entitySprites) {
            for (EntitySprite sprite : entitySprites.values()) {
                // Solo actualizar si no está siendo movido por un PathAnimator
                boolean isBeingAnimated = activeAnimations.stream()
                        .anyMatch(animator -> animator.getEntity() == sprite);

                if (!isBeingAnimated) {
                    sprite.update(deltaTime);
                }
            }
        }

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
        // ===== CAPA 1: FONDO =====
        renderBackground();

        // ===== CAPA 2: GRID/PATRON INDUSTRIAL =====
        renderIndustrialPattern();

        // ===== CAPA 3: CONEXIONES =====
        renderConnections();

        // ===== CAPA 4: LOCACIONES + SOMBRAS =====
        renderLocations();

        // ===== CAPA 5: RECURSOS =====
        renderResources();

        // ===== CAPA 6: ENTIDADES =====
        renderEntities();

        // ===== CAPA 7: EFECTOS UI OVERLAY =====
        renderUIOverlay();
    }

    /**
     * CAPA 1: Renderiza el fondo con gradiente
     */
    private void renderBackground() {
        gc.setFill(ColorPalette.createBackgroundGradient());
        gc.fillRect(0, 0, canvas.getWidth(), canvas.getHeight());
    }

    /**
     * CAPA 2: Renderiza patrón industrial (grid o circuitos)
     */
    private void renderIndustrialPattern() {
        // Grid industrial sutil
        VisualEffects.drawIndustrialGrid(gc, canvas.getWidth(), canvas.getHeight(),
                50, ColorPalette.UI_GLASS_STROKE);

        // Partículas flotantes ambientales
        VisualEffects.drawFloatingParticles(gc, canvas.getWidth(), canvas.getHeight(),
                ColorPalette.ACCENT_CYAN, animationTime, 30);
    }

    /**
     * CAPA 3: Renderiza conexiones entre locaciones con efectos de flujo
     */
    private void renderConnections() {
        // Dibujar líneas entre locaciones conectadas
        drawFlowingLine("ALMACEN_MP", "HORNO");
        drawFlowingLine("HORNO", "BANDA_1");
        drawFlowingLine("BANDA_1", "CARGA");

        // Flujo Robot
        drawFlowingLine("CARGA", "TORNEADO");
        drawFlowingLine("TORNEADO", "FRESADO");
        drawFlowingLine("FRESADO", "TALADRO");
        drawFlowingLine("TALADRO", "RECTIFICADO");
        drawFlowingLine("RECTIFICADO", "DESCARGA");

        drawFlowingLine("DESCARGA", "BANDA_2");
        drawFlowingLine("BANDA_2", "INSPECCION");
        drawFlowingLine("INSPECCION", "SALIDA");
    }

    /**
     * Dibuja línea con efecto de flujo animado
     */
    private void drawFlowingLine(String from, String to) {
        LocationNode fromNode = locationNodes.get(from);
        LocationNode toNode = locationNodes.get(to);

        if (fromNode != null && toNode != null) {
            double x1 = fromNode.getCenterX();
            double y1 = fromNode.getCenterY();
            double x2 = toNode.getCenterX();
            double y2 = toNode.getCenterY();

            // Línea base
            gc.setStroke(ColorPalette.UI_GLASS_STROKE);
            gc.setLineWidth(2);
            gc.setGlobalAlpha(0.3);
            gc.strokeLine(x1, y1, x2, y2);
            gc.setGlobalAlpha(1.0);

            // Partículas de flujo animadas
            double flowProgress = (animationTime * 0.3) % 1.0;
            double flowX = x1 + (x2 - x1) * flowProgress;
            double flowY = y1 + (y2 - y1) * flowProgress;

            gc.setFill(ColorPalette.ACCENT_CYAN);
            gc.setGlobalAlpha(0.6);
            gc.fillOval(flowX - 3, flowY - 3, 6, 6);
            gc.setGlobalAlpha(1.0);
        }
    }

    /**
     * CAPA 4: Renderiza locaciones con todos sus efectos
     */
    private void renderLocations() {
        for (LocationNode node : locationNodes.values()) {
            node.draw(gc);
        }
    }

    /**
     * CAPA 5: Renderiza recursos (grúa y robot)
     */
    private void renderResources() {
        for (ResourceSprite sprite : resourceSprites.values()) {
            sprite.draw(gc);
        }
    }

    /**
     * CAPA 6: Renderiza entidades en movimiento
     */
    private void renderEntities() {
        synchronized (entitySprites) {
            for (EntitySprite sprite : entitySprites.values()) {
                sprite.draw(gc);
            }
        }
    }

    /**
     * CAPA 7: Renderiza UI overlay (tiempo, stats, etc.)
     */
    private void renderUIOverlay() {
        // Panel semitransparente para información
        double panelX = 10;
        double panelY = canvas.getHeight() - 50;
        double panelWidth = 350;
        double panelHeight = 40;

        VisualEffects.drawGlassPanel(gc, panelX, panelY, panelWidth, panelHeight, 10);

        // Texto de información
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setFill(ColorPalette.UI_TEXT_PRIMARY);
        gc.fillText(String.format("⏱ Tiempo: %.2f min", currentTime), panelX + 15, panelY + 25);

        // Contador de entidades
        gc.fillText(String.format("📦 Entidades: %d", entitySprites.size()), panelX + 170, panelY + 25);

        // Indicador de velocidad
        gc.setFont(Font.font("Arial", FontWeight.NORMAL, 11));
        gc.setFill(ColorPalette.UI_TEXT_SECONDARY);
        gc.fillText(String.format("🚀 %.1fx", simulationSpeed), panelX + 290, panelY + 25);
    }

    /**
     * Inicia animación de movimiento de entidad
     */
    public void animateEntityMovement(Entity entity, String fromLocation, String toLocation,
            String resourceName, Runnable onComplete) {
        LocationNode from = locationNodes.get(fromLocation);
        LocationNode to = locationNodes.get(toLocation);

        if (from == null || to == null)
            return;

        EntitySprite sprite = entitySprites.computeIfAbsent(
                entity.getId(),
                id -> {
                    EntitySprite newSprite = new EntitySprite(entity, from.getCenterX(), from.getCenterY());
                    newSprite.spawn(); // Efecto de aparición
                    return newSprite;
                });

        PathAnimator animator;

        if (resourceName != null && !resourceName.isEmpty()) {
            // Movimiento con recurso
            ResourceSprite resource = resourceSprites.get(resourceName);
            if (resource != null) {
                resource.setLoaded(true);
            }
            animator = new PathAnimator(sprite, resource, from, to, 2.0, () -> {
                if (resource != null) {
                    resource.setLoaded(false);
                }
                if (onComplete != null) {
                    onComplete.run();
                }
            });
        } else {
            // Movimiento directo
            animator = new PathAnimator(sprite, from, to, 1.0, onComplete);
        }

        activeAnimations.add(animator);
    }

    /**
     * Actualiza el estado de una ubicación
     */
    public void updateLocationState(String locationName, boolean isWorking, boolean isWaiting) {
        LocationNode node = locationNodes.get(locationName);
        if (node != null) {
            node.setWorking(isWorking);
            node.setWaiting(isWaiting);
        }
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
