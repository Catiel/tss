package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.effect.BlendMode;

/**
 * Utilidades para efectos visuales avanzados
 */
public class VisualEffects {

    /**
     * Dibuja una sombra suave debajo de un objeto
     */
    public static void drawSoftShadow(GraphicsContext gc, double x, double y,
            double width, double height, double blur) {
        gc.save();
        gc.setGlobalAlpha(0.3);

        // Dibuja múltiples óvalos con opacidad decreciente para simular blur
        for (int i = 0; i < blur; i++) {
            double alpha = 0.3 * (1.0 - (i / blur));
            gc.setGlobalAlpha(alpha);
            gc.setFill(Color.BLACK);

            double offset = i * 0.5;
            gc.fillOval(
                    x - offset,
                    y + height * 0.7 - offset,
                    width + offset * 2,
                    height * 0.3 + offset);
        }

        gc.restore();
    }

    /**
     * Dibuja un efecto de resplandor alrededor de un objeto
     */
    public static void drawGlow(GraphicsContext gc, double x, double y,
            double width, double height, Color glowColor,
            double intensity) {
        gc.save();

        int layers = 5;
        for (int i = layers; i > 0; i--) {
            double scale = 1.0 + (i * 0.1);
            double alpha = intensity * (1.0 - (i / (double) layers)) * 0.6;

            gc.setGlobalAlpha(alpha);
            gc.setFill(glowColor);

            double scaledWidth = width * scale;
            double scaledHeight = height * scale;
            double offsetX = (scaledWidth - width) / 2;
            double offsetY = (scaledHeight - height) / 2;

            gc.fillRoundRect(
                    x - offsetX,
                    y - offsetY,
                    scaledWidth,
                    scaledHeight,
                    15, 15);
        }

        gc.restore();
    }

    /**
     * Dibuja un borde con efecto de neón
     */
    public static void drawNeonBorder(GraphicsContext gc, double x, double y,
            double width, double height, Color neonColor,
            double intensity) {
        gc.save();

        // Resplandor exterior
        for (int i = 3; i > 0; i--) {
            gc.setGlobalAlpha(intensity * 0.3 * (1.0 - i / 4.0));
            gc.setStroke(neonColor);
            gc.setLineWidth(4 + i * 2);
            gc.strokeRoundRect(x, y, width, height, 10, 10);
        }

        // Línea principal brillante
        gc.setGlobalAlpha(intensity);
        gc.setStroke(neonColor.brighter());
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);

        gc.restore();
    }

    /**
     * Dibuja un patrón de grid industrial en el fondo
     */
    public static void drawIndustrialGrid(GraphicsContext gc, double width, double height,
            double gridSize, Color gridColor) {
        gc.save();
        gc.setStroke(gridColor);
        gc.setLineWidth(0.5);
        gc.setGlobalAlpha(0.15);

        // Líneas verticales
        for (double x = 0; x < width; x += gridSize) {
            gc.strokeLine(x, 0, x, height);
        }

        // Líneas horizontales
        for (double y = 0; y < height; y += gridSize) {
            gc.strokeLine(0, y, width, y);
        }

        gc.restore();
    }

    /**
     * Dibuja un patrón de circuito tecnológico en el fondo
     */
    public static void drawCircuitPattern(GraphicsContext gc, double width, double height,
            Color circuitColor, double time) {
        gc.save();
        gc.setStroke(circuitColor);
        gc.setLineWidth(1);
        gc.setGlobalAlpha(0.1);

        double spacing = 80;

        for (double x = 0; x < width; x += spacing) {
            for (double y = 0; y < height; y += spacing) {
                // Líneas horizontales con variación
                double len1 = 20 + Math.sin(time + x * 0.01) * 10;
                gc.strokeLine(x, y, x + len1, y);

                // Líneas verticales
                double len2 = 15 + Math.cos(time + y * 0.01) * 8;
                gc.strokeLine(x, y, x, y + len2);

                // Nodos
                gc.fillOval(x - 2, y - 2, 4, 4);
            }
        }

        gc.restore();
    }

    /**
     * Dibuja un indicador de progreso circular
     */
    public static void drawCircularProgress(GraphicsContext gc, double centerX, double centerY,
            double radius, double progress,
            Color fillColor, Color bgColor) {
        gc.save();

        // Fondo
        gc.setStroke(bgColor);
        gc.setLineWidth(4);
        gc.strokeOval(centerX - radius, centerY - radius, radius * 2, radius * 2);

        // Progreso
        gc.setStroke(fillColor);
        gc.setLineWidth(4);

        double startAngle = -90;
        double extent = 360 * progress;

        // Usar strokeArc simulado con líneas
        int segments = 60;
        for (int i = 0; i < segments * progress; i++) {
            double angle1 = Math.toRadians(startAngle + (extent / segments) * i);
            double angle2 = Math.toRadians(startAngle + (extent / segments) * (i + 1));

            double x1 = centerX + Math.cos(angle1) * radius;
            double y1 = centerY + Math.sin(angle1) * radius;
            double x2 = centerX + Math.cos(angle2) * radius;
            double y2 = centerY + Math.sin(angle2) * radius;

            gc.strokeLine(x1, y1, x2, y2);
        }

        gc.restore();
    }

    /**
     * Dibuja texto con efecto de resplandor
     */
    public static void drawGlowingText(GraphicsContext gc, String text, double x, double y,
            Color textColor, Color glowColor, double glowIntensity) {
        gc.save();

        // Resplandor
        for (int i = 3; i > 0; i--) {
            gc.setGlobalAlpha(glowIntensity * 0.4 * (1.0 - i / 4.0));
            gc.setFill(glowColor);
            gc.fillText(text, x, y);
        }

        // Texto principal
        gc.setGlobalAlpha(1.0);
        gc.setFill(textColor);
        gc.fillText(text, x, y);

        gc.restore();
    }

    /**
     * Dibuja un rayo o destello de energía
     */
    public static void drawEnergyBolt(GraphicsContext gc, double x1, double y1,
            double x2, double y2, Color boltColor,
            double intensity, double time) {
        gc.save();

        // Calcular segmentos del rayo
        int segments = 8;
        double[] xPoints = new double[segments + 1];
        double[] yPoints = new double[segments + 1];

        xPoints[0] = x1;
        yPoints[0] = y1;
        xPoints[segments] = x2;
        yPoints[segments] = y2;

        // Puntos intermedios con offset aleatorio
        for (int i = 1; i < segments; i++) {
            double t = i / (double) segments;
            double baseX = x1 + (x2 - x1) * t;
            double baseY = y1 + (y2 - y1) * t;

            double offset = 15 * Math.sin(time * 10 + i);
            double perpX = -(y2 - y1) / Math.hypot(x2 - x1, y2 - y1);
            double perpY = (x2 - x1) / Math.hypot(x2 - x1, y2 - y1);

            xPoints[i] = baseX + perpX * offset;
            yPoints[i] = baseY + perpY * offset;
        }

        // Dibujar resplandor
        gc.setGlobalAlpha(intensity * 0.5);
        gc.setStroke(boltColor);
        gc.setLineWidth(6);
        for (int i = 0; i < segments; i++) {
            gc.strokeLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        // Dibujar rayo principal
        gc.setGlobalAlpha(intensity);
        gc.setStroke(boltColor.brighter());
        gc.setLineWidth(2);
        for (int i = 0; i < segments; i++) {
            gc.strokeLine(xPoints[i], yPoints[i], xPoints[i + 1], yPoints[i + 1]);
        }

        gc.restore();
    }

    /**
     * Dibuja un panel con efecto glassmorphism
     */
    public static void drawGlassPanel(GraphicsContext gc, double x, double y,
            double width, double height, double cornerRadius) {
        gc.save();

        // Relleno semitransparente
        gc.setGlobalAlpha(0.1);
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(x, y, width, height, cornerRadius, cornerRadius);

        // Borde brillante
        gc.setGlobalAlpha(0.3);
        gc.setStroke(Color.WHITE);
        gc.setLineWidth(1.5);
        gc.strokeRoundRect(x, y, width, height, cornerRadius, cornerRadius);

        // Highlight superior
        gc.setGlobalAlpha(0.2);
        gc.setFill(Color.WHITE);
        gc.fillRoundRect(x, y, width, height * 0.3, cornerRadius, cornerRadius);

        gc.restore();
    }

    /**
     * Dibuja partículas flotantes para ambiente
     */
    public static void drawFloatingParticles(GraphicsContext gc, double width, double height,
            Color particleColor, double time, int count) {
        gc.save();
        gc.setFill(particleColor);

        for (int i = 0; i < count; i++) {
            double seed = i * 1337;
            double x = ((seed * 17) % width + time * 10 * (1 + i % 3)) % width;
            double y = ((seed * 31) % height + time * 5 * (1 + i % 2)) % height;
            double size = 1 + (i % 3);
            double alpha = 0.3 + 0.4 * Math.sin(time + i);

            gc.setGlobalAlpha(alpha);
            gc.fillOval(x, y, size, size);
        }

        gc.restore();
    }
}
