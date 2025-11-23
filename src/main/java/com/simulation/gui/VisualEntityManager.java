package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import javafx.geometry.Point2D;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Gestor visual para entidades con animación suave de movimiento
 */
public class VisualEntityManager {

    private final ConcurrentLinkedQueue<AnimatedEntity> entities = new ConcurrentLinkedQueue<>();
    private final Map<Integer, ResourceTransport> transportsInProgress = new ConcurrentHashMap<>();

    private static final double ENTITY_SIZE = 16; // Más grande para mejor visibilidad
    private static final double ANIMATION_SPEED = 4.0; // Más rápido para mejor fluidez

    public void addEntity(String type, double startX, double startY, double endX, double endY) {
        entities.add(new AnimatedEntity(type, startX, startY, endX, endY));
    }

    /**
     * Iniciar transporte de entidad CON recurso usando una ruta compleja
     */
    public void startResourceTransport(int entityId, String entityType, String resourceName,
            List<Point2D> path) {
        ResourceTransport transport = new ResourceTransport(
                entityId, entityType, resourceName, path);
        transportsInProgress.put(entityId, transport);
    }

    /**
     * Iniciar transporte de entidad SIN recurso (auto-transporte)
     */
    public void startEntityTransport(int entityId, String entityType, List<Point2D> path) {
        // Usamos "SELF" como nombre de recurso para indicar movimiento propio
        ResourceTransport transport = new ResourceTransport(
                entityId, entityType, "SELF", path);
        transportsInProgress.put(entityId, transport);
    }

    /**
     * Completar transporte de entidad
     */
    public void completeResourceTransport(int entityId) {
        transportsInProgress.remove(entityId);
    }

    /**
     * Obtener transportes en progreso para sincronizar con recursos
     */
    public Map<Integer, ResourceTransport> getTransportsInProgress() {
        return transportsInProgress;
    }

    public boolean isBeingTransported(int entityId) {
        return transportsInProgress.containsKey(entityId);
    }

    public void render(GraphicsContext gc) {
        // Renderizar entidades que se mueven solas
        Iterator<AnimatedEntity> iterator = entities.iterator();
        while (iterator.hasNext()) {
            AnimatedEntity entity = iterator.next();

            // Actualizar posición
            entity.update();

            // Remover si llegó al destino
            if (entity.hasArrived()) {
                iterator.remove();
                continue;
            }

            // Dibujar entidad
            renderEntity(gc, entity.type, entity.currentX, entity.currentY, entity.dx, entity.dy);
        }

        // Renderizar entidades siendo transportadas por recursos
        Iterator<Map.Entry<Integer, ResourceTransport>> transportIterator = transportsInProgress.entrySet().iterator();
        while (transportIterator.hasNext()) {
            Map.Entry<Integer, ResourceTransport> entry = transportIterator.next();
            ResourceTransport transport = entry.getValue();

            transport.update();

            if (transport.hasArrived()) {
                transportIterator.remove();
                continue;
            }

            // La entidad se dibuja junto al recurso
            renderEntity(gc, transport.entityType,
                    transport.currentX, transport.currentY,
                    transport.dx, transport.dy);
        }
    }

    // Cache de imágenes
    private final Map<String, javafx.scene.image.Image> imageCache = new ConcurrentHashMap<>();

    private void renderEntity(GraphicsContext gc, String type, double x, double y, double dx, double dy) {
        // Intentar cargar imagen
        javafx.scene.image.Image img = getImageForType(type);

        boolean isMoving = Math.abs(dx) > 0.1 || Math.abs(dy) > 0.1;

        if (img != null) {
            // Renderizar IMAGEN
            gc.save();
            gc.translate(x, y);

            // Rotación si se mueve
            if (isMoving) {
                double angle = Math.toDegrees(Math.atan2(dy, dx));
                gc.rotate(angle);
            }

            // Dibujar imagen centrada
            double imgSize = ENTITY_SIZE * 1.5;
            gc.drawImage(img, -imgSize / 2, -imgSize / 2, imgSize, imgSize);
            gc.restore();

            // Etiqueta (siempre visible para identificar)
            renderLabel(gc, type, x, y);

        } else {
            // Renderizar FORMA (Fallback)
            renderShape(gc, type, x, y, dx, dy, isMoving);
        }
    }

    private javafx.scene.image.Image getImageForType(String type) {
        return imageCache.computeIfAbsent(type, t -> {
            try {
                // Normalizar nombre de archivo: "GRANOS_DE_CEBADA" -> "granos_de_cebada.png"
                String filename = "/images/" + t.toLowerCase() + ".png";
                java.net.URL url = getClass().getResource(filename);
                if (url != null) {
                    return new javafx.scene.image.Image(url.toExternalForm());
                }
            } catch (Exception e) {
                // Ignorar errores de carga, usar fallback
            }
            return null;
        });
    }

    private void renderShape(GraphicsContext gc, String type, double x, double y, double dx, double dy,
            boolean isMoving) {
        // Obtener color e icono según tipo de entidad
        Color color = getEntityColor(type);
        String icon = getEntityIcon(type);

        // Estela de movimiento SUAVE (solo si se está moviendo)
        if (isMoving) {
            gc.setGlobalAlpha(0.15);
            for (int i = 1; i <= 3; i++) {
                double trailX = x - dx * i * 4;
                double trailY = y - dy * i * 4;
                double trailSize = ENTITY_SIZE * (1 - i * 0.2);
                gc.setFill(color);
                gc.fillOval(trailX - trailSize / 2, trailY - trailSize / 2, trailSize, trailSize);
            }
            gc.setGlobalAlpha(1.0);
        }

        // Sombra
        gc.setFill(Color.color(0, 0, 0, 0.3));
        gc.fillOval(x - ENTITY_SIZE / 2 + 1.5, y - ENTITY_SIZE / 2 + 1.5, ENTITY_SIZE, ENTITY_SIZE);

        // Cuerpo de la entidad con gradiente simple
        RadialGradient gradient = new RadialGradient(
                0, 0,
                x - ENTITY_SIZE / 3, y - ENTITY_SIZE / 3,
                ENTITY_SIZE * 0.8,
                false,
                CycleMethod.NO_CYCLE,
                new Stop(0, color.brighter().brighter()),
                new Stop(1, color));
        gc.setFill(gradient);
        gc.fillOval(x - ENTITY_SIZE / 2, y - ENTITY_SIZE / 2, ENTITY_SIZE, ENTITY_SIZE);

        // Borde blanco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(x - ENTITY_SIZE / 2, y - ENTITY_SIZE / 2, ENTITY_SIZE, ENTITY_SIZE);

        // Dibujar ICONO rotado
        gc.save();
        gc.translate(x, y);
        if (isMoving) {
            double angle = Math.toDegrees(Math.atan2(dy, dx));
            gc.rotate(angle);
        }

        gc.setFont(Font.font("Segoe UI Emoji", FontWeight.BOLD, 12));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        gc.setFill(Color.WHITE);
        gc.fillText(icon, 0, 4);
        gc.restore();

        renderLabel(gc, type, x, y);
        renderArrow(gc, x, y, dx, dy, isMoving);
    }

    private void renderLabel(GraphicsContext gc, String type, double x, double y) {
        // Etiqueta GRANDE y CLARA
        String shortName = getShortEntityName(type);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 10));
        gc.setTextAlign(javafx.scene.text.TextAlignment.CENTER);
        double textWidth = shortName.length() * 6;

        // Fondo de etiqueta
        gc.setFill(Color.color(0, 0, 0, 0.9));
        gc.fillRoundRect(x - textWidth / 2 - 4, y + ENTITY_SIZE / 2 + 4, textWidth + 8, 16, 5, 5);

        // Texto
        gc.setFill(Color.YELLOW);
        gc.fillText(shortName, x, y + ENTITY_SIZE / 2 + 16);
    }

    private void renderArrow(GraphicsContext gc, double x, double y, double dx, double dy, boolean isMoving) {
        // Flecha de dirección CLARA (solo si se mueve)
        if (isMoving) {
            double angle = Math.atan2(dy, dx);
            double arrowDist = ENTITY_SIZE / 2 + 8;
            double arrowX = x + Math.cos(angle) * arrowDist;
            double arrowY = y + Math.sin(angle) * arrowDist;
            double arrowSize = 10;

            // Fondo de flecha
            gc.setFill(Color.color(0, 0, 0, 0.7));
            double[] xPointsBg = {
                    arrowX + Math.cos(angle) * (arrowSize + 2),
                    arrowX + Math.cos(angle + 2.6) * (arrowSize / 2 + 2),
                    arrowX + Math.cos(angle - 2.6) * (arrowSize / 2 + 2)
            };
            double[] yPointsBg = {
                    arrowY + Math.sin(angle) * (arrowSize + 2),
                    arrowY + Math.sin(angle + 2.6) * (arrowSize / 2 + 2),
                    arrowY + Math.sin(angle - 2.6) * (arrowSize / 2 + 2)
            };
            gc.fillPolygon(xPointsBg, yPointsBg, 3);

            // Flecha
            gc.setFill(Color.LIME);
            double[] xPoints = {
                    arrowX + Math.cos(angle) * arrowSize,
                    arrowX + Math.cos(angle + 2.6) * arrowSize / 2,
                    arrowX + Math.cos(angle - 2.6) * arrowSize / 2
            };
            double[] yPoints = {
                    arrowY + Math.sin(angle) * arrowSize,
                    arrowY + Math.sin(angle + 2.6) * arrowSize / 2,
                    arrowY + Math.sin(angle - 2.6) * arrowSize / 2
            };
            gc.fillPolygon(xPoints, yPoints, 3);

            // Borde
            gc.setStroke(Color.WHITE);
            gc.setLineWidth(1.5);
            gc.strokePolygon(xPoints, yPoints, 3);
        }
    }

    /**
     * Obtener nombre corto para etiqueta
     */
    private String getShortEntityName(String type) {
        switch (type) {
            case "GRANOS_DE_CEBADA":
                return "CEBADA";
            case "LUPULO":
                return "LÚPULO";
            case "LEVADURA":
                return "LEVADURA";
            case "MOSTO":
                return "MOSTO";
            case "CERVEZA":
                return "CERVEZA";
            case "BOTELLA_CON_CERVEZA":
                return "BOTELLA";
            case "CAJA_VACIA":
                return "CAJA-V";
            case "CAJA_CON_CERVEZAS":
                return "CAJA-C";
            default:
                return type.substring(0, Math.min(6, type.length()));
        }
    }

    private Color getEntityColor(String type) {
        switch (type) {
            case "GRANOS_DE_CEBADA":
                return Color.web("#D4A574"); // Marrón claro
            case "LUPULO":
                return Color.web("#7CB342"); // Verde
            case "LEVADURA":
                return Color.web("#FFA726"); // Naranja
            case "MOSTO":
                return Color.web("#8E24AA"); // Púrpura
            case "CERVEZA":
                return Color.web("#FFB300"); // Dorado
            case "BOTELLA_CON_CERVEZA":
                return Color.web("#00897B"); // Verde azulado
            case "CAJA_VACIA":
                return Color.web("#757575"); // Gris
            case "CAJA_CON_CERVEZAS":
                return Color.web("#5E35B1"); // Morado
            default:
                return Color.web("#3498DB"); // Azul por defecto
        }
    }

    private String getEntityIcon(String type) {
        switch (type) {
            case "GRANOS_DE_CEBADA":
                return "🌾";
            case "LUPULO":
                return "🌿";
            case "LEVADURA":
                return "🦠";
            case "MOSTO":
                return "🫗";
            case "CERVEZA":
                return "🍺";
            case "BOTELLA_CON_CERVEZA":
                return "🍾";
            case "CAJA_VACIA":
            case "CAJA_CON_CERVEZAS":
                return "📦";
            default:
                return "•";
        }
    }

    public void clear() {
        entities.clear();
        transportsInProgress.clear();
    }

    public int getEntityCount() {
        return entities.size() + transportsInProgress.size();
    }

    /**
     * Clase para transporte con recurso (Multi-segmento)
     */
    public static class ResourceTransport {
        public final int entityId;
        public final String entityType;
        public final String resourceName;

        private final Queue<Point2D> pathPoints;
        public double currentX, currentY;
        public double targetX, targetY;
        public double dx, dy;
        private boolean finished = false;

        public ResourceTransport(int entityId, String entityType, String resourceName, List<Point2D> path) {
            this.entityId = entityId;
            this.entityType = entityType;
            this.resourceName = resourceName;

            this.pathPoints = new LinkedList<>();
            if (path != null) {
                for (Point2D p : path) {
                    if (p != null)
                        this.pathPoints.add(p);
                }
            }

            // Inicializar posición en el primer punto
            if (!pathPoints.isEmpty()) {
                Point2D start = pathPoints.poll();
                this.currentX = start.getX();
                this.currentY = start.getY();

                // Configurar siguiente objetivo
                setupNextSegment();
            } else {
                // Si no hay ruta, marcar como terminado inmediatamente para evitar errores
                finished = true;
            }
        }

        private void setupNextSegment() {
            if (pathPoints.isEmpty()) {
                finished = true;
                return;
            }

            Point2D next = pathPoints.peek(); // Mirar el siguiente sin removerlo aún
            if (next == null) {
                pathPoints.poll();
                setupNextSegment();
                return;
            }

            this.targetX = next.getX();
            this.targetY = next.getY();

            double distance = Math.sqrt(Math.pow(targetX - currentX, 2) + Math.pow(targetY - currentY, 2));
            if (distance > 0) {
                this.dx = (targetX - currentX) / distance * ANIMATION_SPEED;
                this.dy = (targetY - currentY) / distance * ANIMATION_SPEED;
            } else {
                // Si la distancia es 0, pasar al siguiente inmediatamente
                pathPoints.poll();
                setupNextSegment();
            }
        }

        public void update() {
            if (finished)
                return;

            double distanceToTarget = Math.sqrt(
                    Math.pow(targetX - currentX, 2) + Math.pow(targetY - currentY, 2));

            if (distanceToTarget > ANIMATION_SPEED) {
                currentX += dx;
                currentY += dy;
            } else {
                // Llegó al nodo intermedio o final
                currentX = targetX;
                currentY = targetY;

                // Remover el punto alcanzado y configurar el siguiente
                if (!pathPoints.isEmpty()) {
                    pathPoints.poll();
                    setupNextSegment();
                } else {
                    finished = true;
                }
            }
        }

        public boolean hasArrived() {
            return finished;
        }
    }

    /**
     * Clase interna para entidad animada (sin recurso)
     */
    private static class AnimatedEntity {
        String type;
        double currentX, currentY;
        double targetX, targetY;
        double dx, dy;
        boolean arrived = false;

        AnimatedEntity(String type, double startX, double startY, double endX, double endY) {
            this.type = type;
            this.currentX = startX;
            this.currentY = startY;
            this.targetX = endX;
            this.targetY = endY;

            // Calcular vector de dirección
            double distance = Math.sqrt(Math.pow(endX - startX, 2) + Math.pow(endY - startY, 2));
            if (distance > 0) {
                this.dx = (endX - startX) / distance * ANIMATION_SPEED;
                this.dy = (endY - startY) / distance * ANIMATION_SPEED;
            }
        }

        void update() {
            if (arrived)
                return;

            // Calcular distancia al objetivo
            double distanceToTarget = Math.sqrt(
                    Math.pow(targetX - currentX, 2) + Math.pow(targetY - currentY, 2));

            // Si está cerca del objetivo, marcar como llegado
            if (distanceToTarget < ANIMATION_SPEED) {
                currentX = targetX;
                currentY = targetY;
                arrived = true;
            } else {
                // Mover hacia el objetivo
                currentX += dx;
                currentY += dy;
            }
        }

        boolean hasArrived() {
            return arrived;
        }
    }
}
