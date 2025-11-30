package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.LinearGradient;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 * Sprite mejorado de recurso (Grúa/Robot) con diseño industrial profesional
 */
public class ResourceSprite {

    private final String name;
    private final double homeX;
    private final double homeY;
    private final Color baseColor;
    private final Color shadowColor;
    private double x, y;
    private boolean isMoving = false;
    private boolean isLoaded = false;

    // Estado de animación
    private double animationTime = 0;
    private double hookSwing = 0; // Para grúa
    private double armRotation = 0; // Para robot

    public ResourceSprite(String name, double x, double y, Color color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;

        if (name.equals("GRUA_VIAJERA")) {
            this.baseColor = ColorPalette.GRUA_BASE;
            this.shadowColor = ColorPalette.GRUA_SHADOW;
        } else {
            this.baseColor = ColorPalette.ROBOT_BASE;
            this.shadowColor = ColorPalette.ROBOT_SHADOW;
        }
    }

    /**
     * Actualiza el estado de animación
     */
    public void update(double deltaTime) {
        animationTime += deltaTime;

        // Animación de gancho oscilante para grúa
        if (name.equals("GRUA_VIAJERA") && isMoving) {
            hookSwing = Math.sin(animationTime * 5) * 5;
        } else {
            hookSwing *= 0.9; // Amortiguación
        }

        // Rotación del brazo para robot
        if (name.equals("ROBOT") && isMoving) {
            armRotation += deltaTime * 180; // Gira
        }
    }

    public void draw(GraphicsContext gc) {
        // Actualizar animación (en caso de que no se llame update externamente)

        if (name.equals("GRUA_VIAJERA")) {
            drawGrua(gc);
        } else if (name.equals("ROBOT")) {
            drawRobot(gc);
        }
    }

    /**
     * Dibuja la grúa viajera con detalle industrial
     */
    private void drawGrua(GraphicsContext gc) {
        double width = 50;
        double height = 40;

        // Sombra dinámica
        VisualEffects.drawSoftShadow(gc, x - width / 2, y + height - 5, width, 10, 6);

        // Puente de la grúa (viga horizontal)
        LinearGradient bridgeGradient = ColorPalette.createDepthGradient(baseColor, 90);
        gc.setFill(bridgeGradient);
        gc.fillRect(x - width / 2, y, width, 12);

        // Bordes metálicos del puente
        gc.setStroke(shadowColor);
        gc.setLineWidth(2);
        gc.strokeRect(x - width / 2, y, width, 12);

        // Detalles metálicos
        gc.setStroke(baseColor.brighter());
        gc.setLineWidth(1);
        gc.strokeLine(x - width / 2 + 2, y + 2, x + width / 2 - 2, y + 2);
        gc.strokeLine(x - width / 2 + 2, y + 10, x + width / 2 - 2, y + 10);

        // Carro (trolley) que se mueve en el puente
        double trolleyX = x;
        gc.setFill(shadowColor);
        gc.fillRect(trolleyX - 10, y + 8, 20, 8);

        // Cable del gancho
        gc.setStroke(Color.DARKGRAY);
        gc.setLineWidth(2);
        double hookTopY = y + 16;
        double hookBottomY = y + height + hookSwing;
        gc.strokeLine(trolleyX, hookTopY, trolleyX, hookBottomY);

        // Gancho
        gc.setFill(Color.GRAY);
        gc.fillOval(trolleyX - 6, hookBottomY - 3, 12, 12);

        // Forma del gancho (triángulo invertido)
        gc.setFill(Color.DARKGRAY);
        gc.fillPolygon(
                new double[] { trolleyX, trolleyX - 4, trolleyX + 4 },
                new double[] { hookBottomY + 9, hookBottomY + 3, hookBottomY + 3 },
                3);

        // Indicador de carga
        if (isLoaded) {
            gc.setFill(ColorPalette.ACCENT_CYAN);
            gc.fillOval(trolleyX - 8, hookBottomY + 10, 16, 16);
        }

        // LED de estado
        drawStatusLED(gc, x + width / 2 - 8, y + 2);

        // Etiqueta
        drawLabel(gc, x, y - 8);
    }

    /**
     * Dibuja el robot con diseño industrial
     */
    private void drawRobot(GraphicsContext gc) {
        double baseSize = 30;

        // Sombra dinámica circular
        gc.setGlobalAlpha(0.3);
        gc.setFill(Color.BLACK);
        gc.fillOval(x - baseSize / 2, y + baseSize - 5, baseSize, 10);
        gc.setGlobalAlpha(1.0);

        // Base del robot (círculo)
        LinearGradient baseGradient = ColorPalette.createDepthGradient(baseColor, 135);
        gc.setFill(baseGradient);
        gc.fillOval(x - baseSize / 2, y, baseSize, baseSize * 0.4);

        // Borde de la base
        gc.setStroke(shadowColor);
        gc.setLineWidth(2.5);
        gc.strokeOval(x - baseSize / 2, y, baseSize, baseSize * 0.4);

        // Columna central
        gc.setFill(shadowColor);
        gc.fillRect(x - 4, y + baseSize * 0.2, 8, baseSize * 0.4);

        // Brazo del robot (animado)
        gc.save();
        gc.translate(x, y + baseSize * 0.3);
        gc.rotate(armRotation);

        // Brazo principal
        gc.setFill(baseColor);
        gc.fillRect(-3, 0, 6, 25);

        // Articulación
        gc.setFill(shadowColor);
        gc.fillOval(-5, 20, 10, 10);

        // Efector final (pinza)
        gc.setFill(Color.SILVER);
        gc.fillRect(-6, 28, 4, 8);
        gc.fillRect(2, 28, 4, 8);

        gc.restore();

        // Indicador de carga
        if (isLoaded) {
            gc.setFill(ColorPalette.ACCENT_GREEN);
            gc.fillOval(x - 6, y + baseSize * 0.8, 12, 12);
        }

        // LED de estado
        drawStatusLED(gc, x, y + 5);

        // Anillo de luz en la base si está en movimiento
        if (isMoving) {
            double glowIntensity = 0.5 + Math.sin(animationTime * 4) * 0.5;
            gc.setGlobalAlpha(glowIntensity * 0.6);
            gc.setStroke(ColorPalette.ACCENT_CYAN);
            gc.setLineWidth(3);
            gc.strokeOval(x - baseSize / 2 - 2, y - 2, baseSize + 4, baseSize * 0.4 + 4);
            gc.setGlobalAlpha(1.0);
        }

        // Etiqueta
        drawLabel(gc, x, y - 12);
    }

    /**
     * Dibuja LED de estado
     */
    private void drawStatusLED(GraphicsContext gc, double ledX, double ledY) {
        Color ledColor = isMoving ? ColorPalette.STATUS_WORKING
                : isLoaded ? ColorPalette.STATUS_WAITING : ColorPalette.STATUS_IDLE;

        // Resplandor del LED si está activo
        if (isMoving || isLoaded) {
            double pulseIntensity = 0.6 + Math.sin(animationTime * 3) * 0.4;
            gc.setGlobalAlpha(pulseIntensity * 0.6);
            gc.setFill(ledColor);
            gc.fillOval(ledX - 6, ledY - 6, 12, 12);
            gc.setGlobalAlpha(1.0);
        }

        // LED
        gc.setFill(ledColor);
        gc.fillOval(ledX - 3, ledY - 3, 6, 6);

        // Brillo
        gc.setFill(Color.WHITE);
        gc.fillOval(ledX - 1, ledY - 1, 2, 2);
    }

    /**
     * Dibuja la etiqueta del recurso
     */
    private void drawLabel(GraphicsContext gc, double labelX, double labelY) {
        String displayName = name.replace("_", " ");

        gc.setFont(Font.font("Arial", FontWeight.BOLD, 9));
        gc.setTextAlign(TextAlignment.CENTER);

        // Sombra del texto
        gc.setFill(Color.BLACK);
        gc.fillText(displayName, labelX + 1, labelY + 1);

        // Texto principal
        gc.setFill(ColorPalette.UI_TEXT_PRIMARY);
        gc.fillText(displayName, labelX, labelY);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void returnHome() {
        this.x = homeX;
        this.y = homeY;
        this.isMoving = false;
        this.isLoaded = false;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getName() {
        return name;
    }

    public boolean isMoving() {
        return isMoving;
    }

    public void setMoving(boolean moving) {
        isMoving = moving;
    }

    public void setLoaded(boolean loaded) {
        this.isLoaded = loaded;
    }
}
