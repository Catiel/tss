package com.simulation.gui;

import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.Stop;

/**
 * Paleta de colores profesional para la GUI del simulador Steel Gears
 * Proporciona colores consistentes y gradientes de alta calidad
 */
public class ColorPalette {

    // ===== COLORES PRINCIPALES =====
    public static final Color BACKGROUND_DARK = Color.web("#1a1a2e");
    public static final Color BACKGROUND_MEDIUM = Color.web("#16213e");
    public static final Color BACKGROUND_LIGHT = Color.web("#0f3460");

    public static final Color PRIMARY = Color.web("#0f3460");
    public static final Color ACCENT_RED = Color.web("#e94560");
    public static final Color ACCENT_CYAN = Color.web("#00d9ff");
    public static final Color ACCENT_ORANGE = Color.web("#ffa31a");
    public static final Color ACCENT_GREEN = Color.web("#00ff88");

    // ===== COLORES DE ESTACIONES =====
    public static final Color HORNO_BASE = Color.web("#ff6b35");
    public static final Color HORNO_GLOW = Color.web("#ff8c42");

    public static final Color TORNEADO_BASE = Color.web("#4a7c99");
    public static final Color TORNEADO_GLOW = Color.web("#6ba3c4");

    public static final Color FRESADO_BASE = Color.web("#7b68ee");
    public static final Color FRESADO_GLOW = Color.web("#9b88ff");

    public static final Color TALADRO_BASE = Color.web("#48a9a6");
    public static final Color TALADRO_GLOW = Color.web("#68c9c6");

    public static final Color RECTIFICADO_BASE = Color.web("#6c88c4");
    public static final Color RECTIFICADO_GLOW = Color.web("#8ca8e4");

    public static final Color INSPECCION_BASE = Color.web("#52b788");
    public static final Color INSPECCION_GLOW = Color.web("#72d7a8");

    public static final Color BANDA_BASE = Color.web("#6c757d");
    public static final Color BANDA_GLOW = Color.web("#8c959d");

    public static final Color ALMACEN_BASE = Color.web("#8b4513");
    public static final Color ALMACEN_GLOW = Color.web("#ab6533");

    public static final Color CARGA_DESCARGA_BASE = Color.web("#95a3b3");
    public static final Color CARGA_DESCARGA_GLOW = Color.web("#b5c3d3");

    // ===== COLORES DE RECURSOS =====
    public static final Color GRUA_BASE = Color.web("#ff9500");
    public static final Color GRUA_SHADOW = Color.web("#cc7700");

    public static final Color ROBOT_BASE = Color.web("#ff3b30");
    public static final Color ROBOT_SHADOW = Color.web("#cc2f26");

    // ===== COLORES DE ENTIDADES =====
    public static final Color ENTITY_PRIMARY = Color.web("#00d9ff");
    public static final Color ENTITY_SHADOW = Color.web("#0099bb");

    // ===== COLORES DE UI =====
    public static final Color UI_GLASS_FILL = Color.web("#ffffff", 0.1);
    public static final Color UI_GLASS_STROKE = Color.web("#ffffff", 0.3);
    public static final Color UI_TEXT_PRIMARY = Color.web("#ffffff");
    public static final Color UI_TEXT_SECONDARY = Color.web("#b0b0b0");

    // ===== COLORES DE ESTADO =====
    public static final Color STATUS_IDLE = Color.web("#6c757d");
    public static final Color STATUS_WORKING = Color.web("#00ff88");
    public static final Color STATUS_WAITING = Color.web("#ffa31a");
    public static final Color STATUS_BLOCKED = Color.web("#e94560");

    /**
     * Crea un gradiente lineal vertical para fondos
     */
    public static LinearGradient createBackgroundGradient() {
        return new LinearGradient(
                0, 0, 0, 1, true, CycleMethod.NO_CYCLE,
                new Stop(0, BACKGROUND_DARK),
                new Stop(1, BACKGROUND_MEDIUM));
    }

    /**
     * Crea un gradiente radial para efectos de resplandor
     */
    public static RadialGradient createGlowGradient(Color centerColor, double opacity) {
        Color transparent = new Color(
                centerColor.getRed(),
                centerColor.getGreen(),
                centerColor.getBlue(),
                0.0);
        Color opaque = new Color(
                centerColor.getRed(),
                centerColor.getGreen(),
                centerColor.getBlue(),
                opacity);

        return new RadialGradient(
                0, 0, 0.5, 0.5, 0.5, true, CycleMethod.NO_CYCLE,
                new Stop(0, opaque),
                new Stop(1, transparent));
    }

    /**
     * Crea un gradiente lineal para objetos con profundidad
     */
    public static LinearGradient createDepthGradient(Color baseColor, double angle) {
        Color lighter = baseColor.brighter();
        Color darker = baseColor.darker();

        double rad = Math.toRadians(angle);
        double x1 = 0.5 - Math.cos(rad) * 0.5;
        double y1 = 0.5 - Math.sin(rad) * 0.5;
        double x2 = 0.5 + Math.cos(rad) * 0.5;
        double y2 = 0.5 + Math.sin(rad) * 0.5;

        return new LinearGradient(
                x1, y1, x2, y2, true, CycleMethod.NO_CYCLE,
                new Stop(0, lighter),
                new Stop(0.5, baseColor),
                new Stop(1, darker));
    }

    /**
     * Obtiene color base según el nombre de la ubicación
     */
    public static Color getLocationBaseColor(String locationName) {
        switch (locationName.toUpperCase()) {
            case "HORNO":
                return HORNO_BASE;
            case "TORNEADO":
                return TORNEADO_BASE;
            case "FRESADO":
                return FRESADO_BASE;
            case "TALADRO":
                return TALADRO_BASE;
            case "RECTIFICADO":
                return RECTIFICADO_BASE;
            case "INSPECCION":
                return INSPECCION_BASE;
            case "BANDA_1":
            case "BANDA_2":
                return BANDA_BASE;
            case "ALMACEN_MP":
                return ALMACEN_BASE;
            case "CARGA":
            case "DESCARGA":
                return CARGA_DESCARGA_BASE;
            case "SALIDA":
                return STATUS_IDLE;
            default:
                return PRIMARY;
        }
    }

    /**
     * Obtiene color de resplandor según el nombre de la ubicación
     */
    public static Color getLocationGlowColor(String locationName) {
        switch (locationName.toUpperCase()) {
            case "HORNO":
                return HORNO_GLOW;
            case "TORNEADO":
                return TORNEADO_GLOW;
            case "FRESADO":
                return FRESADO_GLOW;
            case "TALADRO":
                return TALADRO_GLOW;
            case "RECTIFICADO":
                return RECTIFICADO_GLOW;
            case "INSPECCION":
                return INSPECCION_GLOW;
            case "BANDA_1":
            case "BANDA_2":
                return BANDA_GLOW;
            case "ALMACEN_MP":
                return ALMACEN_GLOW;
            case "CARGA":
            case "DESCARGA":
                return CARGA_DESCARGA_GLOW;
            default:
                return ACCENT_CYAN;
        }
    }

    /**
     * Interpola entre dos colores
     */
    public static Color interpolate(Color from, Color to, double progress) {
        progress = Math.max(0.0, Math.min(1.0, progress));
        return new Color(
                from.getRed() + (to.getRed() - from.getRed()) * progress,
                from.getGreen() + (to.getGreen() - from.getGreen()) * progress,
                from.getBlue() + (to.getBlue() - from.getBlue()) * progress,
                from.getOpacity() + (to.getOpacity() - from.getOpacity()) * progress);
    }
}
