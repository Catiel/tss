package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Representa una estación/locación con visualización profesional mejorada
 */
public class LocationNode {

    private final String name;
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final Color baseColor;
    private final Color glowColor;

    private int currentOccupancy;
    private int capacity;
    private int totalEntries = 0; // Total de entradas histórico

    // Estado de animación
    private double animationTime = 0;
    private double glowIntensity = 0;
    private boolean isWorking = false;
    private boolean isWaiting = false;

    // Sistema de partículas propio
    private final ParticleSystem particleSystem;

    public LocationNode(String name, double x, double y, double width, double height, Color color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.baseColor = ColorPalette.getLocationBaseColor(name);
        this.glowColor = ColorPalette.getLocationGlowColor(name);
        this.currentOccupancy = 0;
        this.capacity = 10;
        this.particleSystem = new ParticleSystem(200);
    }

    /**
     * Actualiza el estado de animación
     */
    public void update(double deltaTime) {
        animationTime += deltaTime;

        // Actualizar intensidad de resplandor
        if (isWorking) {
            glowIntensity = Math.min(1.0, glowIntensity + deltaTime * 2);
        } else {
            glowIntensity = Math.max(0.0, glowIntensity - deltaTime * 1.5);
        }

        if (isAlmacenMp()) {
            particleSystem.clear();
            return;
        }

        // Actualizar partículas
        particleSystem.update(deltaTime);

        // Emitir partículas SOLO si está trabajando (optimización y feedback)
        if (isWorking) {
            emitStationSpecificParticles();
        }
    }

    public void draw(GraphicsContext gc) {
        // Dibujar sombra
        VisualEffects.drawSoftShadow(gc, x, y, width, height, 8);

        // Dibujar resplandor si está activo
        if (glowIntensity > 0.1) {
            VisualEffects.drawGlow(gc, x, y, width, height, glowColor, glowIntensity * 0.8);
        }

        // Dibujar el cuerpo principal de la estación según su tipo
        drawStationBody(gc);

        // Dibujar contador de ocupación/capacidad VISIBLE
        drawOccupancyCounter(gc);

        // Dibujar barra de capacidad circular
        drawCapacityBar(gc);

        // Dibujar nombre con estilo
        drawStationName(gc);

        // Dibujar indicador de estado
        drawStatusIndicator(gc);

        // Dibujar badge de total de entradas
        if (totalEntries > 0) {
            drawTotalEntriesBadge(gc);
        }

        if (!isAlmacenMp()) {
            // Renderizar partículas (solo si hay activas)
            particleSystem.render(gc);
        } else {
            particleSystem.clear();
        }
    }

    /**
     * Dibuja el cuerpo de la estación según su tipo
     */
    private void drawStationBody(GraphicsContext gc) {
        switch (name.toUpperCase()) {
            case "HORNO":
                drawHornoStation(gc);
                break;
            case "TORNEADO":
                drawTorneadoStation(gc);
                break;
            case "FRESADO":
                drawFresadoStation(gc);
                break;
            case "TALADRO":
                drawTaladroStation(gc);
                break;
            case "RECTIFICADO":
                drawRectificadoStation(gc);
                break;
            case "INSPECCION":
                drawInspeccionStation(gc);
                break;
            case "BANDA_1":
            case "BANDA_2":
                drawBandaStation(gc);
                break;
            case "ALMACEN_MP":
                drawAlmacenStation(gc);
                break;
            case "CARGA":
            case "DESCARGA":
                drawCargaDescargaStation(gc);
                break;
            default:
                drawGenericStation(gc);
                break;
        }
    }

    /**
     * Estación HORNO - con efecto de calor
     */
    private void drawHornoStation(GraphicsContext gc) {
        // Cuerpo principal con gradiente
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 15, 15);

        // Ventana del horno (oscura con resplandor)
        double windowX = x + width * 0.25;
        double windowY = y + height * 0.3;
        double windowW = width * 0.5;
        double windowH = height * 0.4;

        gc.setFill(Color.color(0.1, 0.05, 0.0));
        gc.fillRoundRect(windowX, windowY, windowW, windowH, 8, 8);

        // Resplandor interior animado
        if (isWorking) {
            double pulseIntensity = 0.7 + Math.sin(animationTime * 3) * 0.3;
            gc.setGlobalAlpha(pulseIntensity * glowIntensity);
            gc.setFill(Color.ORANGERED);
            gc.fillRoundRect(windowX + 2, windowY + 2, windowW - 4, windowH - 4, 6, 6);
            gc.setGlobalAlpha(1.0);
        }

        // Borde metálico
        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(3);
        gc.strokeRoundRect(x, y, width, height, 15, 15);

        // Detalles metálicos
        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x + 3, y + 3, width - 6, height - 6, 12, 12);
    }

    /**
     * Estación TORNEADO - con pieza rotando
     */
    private void drawTorneadoStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        // Torno (horizontal)
        double tornoY = y + height * 0.5;
        gc.setStroke(baseColor.darker());
        gc.setLineWidth(4);
        gc.strokeLine(x + 10, tornoY, x + width - 10, tornoY);

        // Pieza rotando (si está trabajando)
        if (isWorking) {
            double pieceX = x + width * 0.5;
            double pieceY = tornoY;
            double rotation = animationTime * 360; // Grados

            gc.save();
            gc.translate(pieceX, pieceY);
            gc.rotate(rotation);
            gc.setFill(ColorPalette.ACCENT_CYAN);
            gc.fillRect(-15, -4, 30, 8);
            gc.restore();
        }

        // Borde
        VisualEffects.drawNeonBorder(gc, x, y, width, height, glowColor, glowIntensity * 0.5);
    }

    /**
     * Estación FRESADO
     */
    private void drawFresadoStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        // Cabezal de fresado
        double headX = x + width * 0.5;
        double headY = y + height * 0.3;

        // Movimiento vertical si está trabajando
        if (isWorking) {
            headY += Math.sin(animationTime * 5) * 5;
        }

        gc.setFill(baseColor.darker());
        gc.fillRect(headX - 8, headY, 16, height * 0.4);

        // Fresa (herramienta)
        gc.setFill(Color.SILVER);
        gc.fillOval(headX - 6, headY + height * 0.3, 12, 12);

        // Borde con efecto
        VisualEffects.drawNeonBorder(gc, x, y, width, height, glowColor, glowIntensity * 0.5);
    }

    /**
     * Estación TALADRO
     */
    private void drawTaladroStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        // Columna del taladro
        double columnX = x + width * 0.5;
        gc.setFill(baseColor.darker());
        gc.fillRect(columnX - 5, y + 10, 10, height - 20);

        // Broca con movimiento
        double drillY = y + height * 0.4;
        if (isWorking) {
            drillY += Math.sin(animationTime * 8) * 3;
        }

        gc.setFill(Color.GRAY);
        gc.fillPolygon(
                new double[] { columnX, columnX - 4, columnX + 4 },
                new double[] { drillY + 15, drillY, drillY },
                3);

        // Borde
        VisualEffects.drawNeonBorder(gc, x, y, width, height, glowColor, glowIntensity * 0.5);
    }

    /**
     * Estación RECTIFICADO - con muela girando
     */
    private void drawRectificadoStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        // Muela
        double wheelX = x + width * 0.6;
        double wheelY = y + height * 0.5;
        double wheelRadius = 15;

        gc.setFill(Color.DARKGRAY);
        gc.fillOval(wheelX - wheelRadius, wheelY - wheelRadius, wheelRadius * 2, wheelRadius * 2);

        // Marcas radiales si está girando
        if (isWorking) {
            gc.save();
            gc.translate(wheelX, wheelY);
            gc.rotate(animationTime * 720); // Gira rápido
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(2);
            for (int i = 0; i < 8; i++) {
                gc.rotate(45);
                gc.strokeLine(0, 0, wheelRadius - 2, 0);
            }
            gc.restore();
        }

        // Borde
        VisualEffects.drawNeonBorder(gc, x, y, width, height, glowColor, glowIntensity * 0.5);
    }

    /**
     * Estación INSPECCION - con lupa/scanner
     */
    private void drawInspeccionStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        // Mesa de inspección
        gc.setFill(baseColor.brighter());
        gc.fillRect(x + width * 0.2, y + height * 0.6, width * 0.6, height * 0.3);

        // Lupa/Scanner con efecto de escaneo
        double scanX = x + width * 0.5;
        double scanY = y + height * 0.4;

        gc.setFill(Color.TRANSPARENT);
        gc.setStroke(ColorPalette.ACCENT_CYAN);
        gc.setLineWidth(3);
        gc.strokeOval(scanX - 15, scanY - 15, 30, 30);

        // Rayo de escaneo animado
        if (isWorking) {
            gc.setGlobalAlpha(0.3 + Math.sin(animationTime * 4) * 0.3);
            gc.setFill(ColorPalette.ACCENT_CYAN);
            gc.fillRect(x + width * 0.2, scanY + 15, width * 0.6, 2);
            gc.setGlobalAlpha(1.0);
        }

        // Borde
        VisualEffects.drawNeonBorder(gc, x, y, width, height, glowColor, glowIntensity * 0.5);
    }

    /**
     * Estación BANDA - transportadora animada
     */
    private void drawBandaStation(GraphicsContext gc) {
        // Base
        gc.setFill(baseColor);
        gc.fillRoundRect(x, y, width, height, 8, 8);

        // Banda transportadora
        double bandaY = y + height * 0.5 - 8;
        gc.setFill(baseColor.darker());
        gc.fillRect(x + 5, bandaY, width - 10, 16);

        // Líneas indicadoras de movimiento
        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(2);

        int numLines = 8;
        double spacing = (width - 10) / numLines;
        double offset = (animationTime * 50) % spacing;

        for (int i = 0; i < numLines + 1; i++) {
            double lineX = x + 5 + (i * spacing) - offset;
            gc.strokeLine(lineX, bandaY, lineX + 10, bandaY + 16);
        }

        // Borde
        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 8, 8);
    }

    /**
     * Estación ALMACEN
     */
    private void drawAlmacenStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 10, 10);

        // Estantes (rack)
        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(2);
        for (int i = 1; i <= 3; i++) {
            double shelfY = y + (height / 4) * i;
            gc.strokeLine(x + 10, shelfY, x + width - 10, shelfY);
        }

        // Columnas
        gc.strokeLine(x + width * 0.3, y + 10, x + width * 0.3, y + height - 10);
        gc.strokeLine(x + width * 0.7, y + 10, x + width * 0.7, y + height - 10);

        // Cajas (representación simple)
        gc.setFill(ColorPalette.ACCENT_ORANGE);
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 2; col++) {
                double boxX = x + 15 + col * (width * 0.4);
                double boxY = y + 15 + row * (height / 4);
                gc.fillRect(boxX, boxY, 12, 12);
            }
        }

        // Borde
        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
    }

    /**
     * Estaciones CARGA/DESCARGA
     */
    private void drawCargaDescargaStation(GraphicsContext gc) {
        // Base
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 10, 10);

        // Plataforma
        gc.setFill(baseColor.darker());
        gc.fillRect(x + 10, y + height * 0.6, width - 20, height * 0.3);

        // Flechas indicadoras
        Color arrowColor = name.equals("CARGA") ? ColorPalette.ACCENT_GREEN : ColorPalette.ACCENT_ORANGE;
        gc.setFill(arrowColor);

        double arrowX = x + width * 0.5;
        double arrowY = y + height * 0.35;
        double arrowSize = 12;

        if (name.equals("CARGA")) {
            // Flecha hacia abajo
            gc.fillPolygon(
                    new double[] { arrowX, arrowX - arrowSize, arrowX + arrowSize },
                    new double[] { arrowY + arrowSize, arrowY, arrowY },
                    3);
        } else {
            // Flecha hacia arriba
            gc.fillPolygon(
                    new double[] { arrowX, arrowX - arrowSize, arrowX + arrowSize },
                    new double[] { arrowY, arrowY + arrowSize, arrowY + arrowSize },
                    3);
        }

        // Borde
        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x, y, width, height, 10, 10);
    }

    /**
     * Estación genérica para casos no especificados
     */
    private void drawGenericStation(GraphicsContext gc) {
        LinearGradient gradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(gradient);
        gc.fillRoundRect(x, y, width, height, 12, 12);

        gc.setStroke(baseColor.darker().darker());
        gc.setLineWidth(2.5);
        gc.strokeRoundRect(x, y, width, height, 12, 12);
    }

    /**
     * Dibuja contador de ocupación/capacidad GRANDE Y VISIBLE
     */
    private void drawOccupancyCounter(GraphicsContext gc) {
        // Panel oscuro en la parte inferior
        double panelX = x;
        double panelY = y + height + 5;
        double panelWidth = width;
        double panelHeight = 22;

        // Fondo semitransparente
        gc.setFill(Color.color(0, 0, 0, 0.8));
        gc.fillRoundRect(panelX, panelY, panelWidth, panelHeight, 4, 4);

        // Texto de ocupación
        String occupancyText = currentOccupancy + "/" +
                (capacity == Integer.MAX_VALUE ? "∞" : String.valueOf(capacity));

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        gc.setTextAlign(TextAlignment.CENTER);

        // Color según ocupación
        double occupancyRatio = capacity > 0 ? (double) currentOccupancy / capacity : 0;
        Color textColor;
        if (occupancyRatio < 0.5) {
            textColor = ColorPalette.STATUS_WORKING; // Verde
        } else if (occupancyRatio < 0.8) {
            textColor = Color.YELLOW; // Amarillo
        } else {
            textColor = ColorPalette.ACCENT_RED; // Rojo
        }

        gc.setFill(textColor);
        gc.fillText(occupancyText, panelX + panelWidth / 2, panelY + 16);

        // Borde
        gc.setStroke(ColorPalette.UI_GLASS_STROKE);
        gc.setLineWidth(1);
        gc.strokeRoundRect(panelX, panelY, panelWidth, panelHeight, 4, 4);
    }

    /**
     * Dibuja la barra de capacidad circular mejorada
     */
    private void drawCapacityBar(GraphicsContext gc) {
        double radius = 12;
        double centerX = x + width - radius - 8;
        double centerY = y + radius + 8;
        double progress = capacity > 0 ? (double) currentOccupancy / capacity : 0;

        // Fondo
        gc.setFill(Color.color(0, 0, 0, 0.5));
        gc.fillOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Progreso circular
        Color progressColor = progress < 0.7 ? ColorPalette.STATUS_WORKING
                : progress < 0.9 ? ColorPalette.STATUS_WAITING : ColorPalette.STATUS_BLOCKED;

        VisualEffects.drawCircularProgress(gc, centerX, centerY, radius - 2, progress,
                progressColor, Color.color(1, 1, 1, 0.2));

        // Texto de capacidad
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(currentOccupancy + "", centerX, centerY + 4);
    }

    /**
     * Dibuja el nombre de la estación con estilo
     */
    private void drawStationName(GraphicsContext gc) {
        String displayName = formatStationName(name);

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 11));
        gc.setTextAlign(TextAlignment.CENTER);

        double textX = x + width / 2;
        double textY = y - 8;

        // Sombra del texto
        gc.setFill(Color.BLACK);
        gc.fillText(displayName, textX + 1, textY + 1);

        // Texto principal con resplandor sutil
        if (glowIntensity > 0.3) {
            VisualEffects.drawGlowingText(gc, displayName, textX, textY,
                    ColorPalette.UI_TEXT_PRIMARY, glowColor,
                    glowIntensity * 0.5);
        } else {
            gc.setFill(ColorPalette.UI_TEXT_PRIMARY);
            gc.fillText(displayName, textX, textY);
        }
    }

    /**
     * Dibuja indicador LED de estado
     */
    private void drawStatusIndicator(GraphicsContext gc) {
        double ledX = x + 8;
        double ledY = y + 8;
        double ledSize = 6;

        Color statusColor;
        if (isWorking) {
            statusColor = ColorPalette.STATUS_WORKING;
        } else if (isWaiting) {
            statusColor = ColorPalette.STATUS_WAITING;
        } else {
            statusColor = ColorPalette.STATUS_IDLE;
        }

        // Resplandor del LED
        if (isWorking || isWaiting) {
            gc.setGlobalAlpha(0.6 + Math.sin(animationTime * 3) * 0.4);
            gc.setFill(statusColor);
            gc.fillOval(ledX - ledSize, ledY - ledSize, ledSize * 3, ledSize * 3);
            gc.setGlobalAlpha(1.0);
        }

        // LED
        gc.setFill(statusColor);
        gc.fillOval(ledX, ledY, ledSize, ledSize);

        // Brillo
        gc.setFill(Color.WHITE);
        gc.fillOval(ledX + 1, ledY + 1, ledSize * 0.4, ledSize * 0.4);
    }

    /**
     * Emite partículas específicas según el tipo de estación
     * OPTIMIZADO: Reduce frecuencia de emisión
     */
    private void emitStationSpecificParticles() {
        double centerX = x + width / 2;
        double centerY = y + height / 2;

        // Emitir con probabilidad reducida para optimizar rendimiento
        if (Math.random() < 0.15) { // Reducido de 0.3 a 0.15
            switch (name.toUpperCase()) {
                case "HORNO":
                    particleSystem.emitSmoke(centerX, y + height * 0.2, 1,
                            Color.color(0.5, 0.5, 0.5, 0.6));
                    if (Math.random() < 0.05) { // Reducido de 0.1
                        particleSystem.emitEmbers(centerX, y + height * 0.3, 1); // Reducido a 1
                    }
                    break;

                case "TORNEADO":
                case "FRESADO":
                case "TALADRO":
                case "RECTIFICADO":
                    // Solo cuando está procesando
                    if (Math.random() < 0.1) { // Reducido de 0.2
                        particleSystem.emitSparks(centerX, centerY, 2, ColorPalette.ACCENT_CYAN);
                    }
                    break;

                case "INSPECCION":
                    // Resplandor muy ocasional
                    if (Math.random() < 0.05) {
                        particleSystem.emitGlow(centerX, centerY, ColorPalette.ACCENT_CYAN, 0.6);
                    }
                    break;
            }
        }
    }

    /**
     * Formatea el nombre de la estación para mostrar
     */
    /**
     * Dibuja badge con el total de entradas en la esquina superior derecha
     */
    private void drawTotalEntriesBadge(GraphicsContext gc) {
        double badgeSize = 20;
        double badgeX = x + width - badgeSize / 2 - 3;
        double badgeY = y - badgeSize / 2 + 3;

        // Círculo de fondo
        gc.setFill(ColorPalette.ACCENT_CYAN);
        gc.fillOval(badgeX, badgeY, badgeSize, badgeSize);

        // Borde blanco
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(2);
        gc.strokeOval(badgeX, badgeY, badgeSize, badgeSize);

        // Número
        gc.setFill(Color.WHITE);
        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);
        String displayText = String.valueOf(totalEntries); // Siempre mostrar número real
        gc.fillText(displayText, badgeX + badgeSize / 2, badgeY + badgeSize / 2 + 3);
    }

    private String formatStationName(String name) {
        return name.replace("_", " ");
    }

    private boolean isAlmacenMp() {
        return "ALMACEN_MP".equalsIgnoreCase(name);
    }

    // ===== GETTERS Y SETTERS =====

    public double getCenterX() {
        return x + width / 2;
    }

    public double getCenterY() {
        return y + height / 2;
    }

    public void setOccupancy(int occupancy) {
        this.currentOccupancy = occupancy;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public void setWorking(boolean working) {
        this.isWorking = working;
    }

    public void setWaiting(boolean waiting) {
        this.isWaiting = waiting;
    }

    public void setTotalEntries(int totalEntries) {
        this.totalEntries = totalEntries;
    }

    public int getTotalEntries() {
        return totalEntries;
    }

    public String getName() {
        return name;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getWidth() {
        return width;
    }

    public double getHeight() {
        return height;
    }

    public ParticleSystem getParticleSystem() {
        return particleSystem;
    }
}
