package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

public class ResourceSprite {

    private final String name;
    private double x, y;
    private final double homeX;
    private final double homeY;
    private final double size = 15;
    private final Color color;
    private boolean isMoving = false;

    public ResourceSprite(String name, double x, double y, Color color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.homeX = x;
        this.homeY = y;
        this.color = color;
    }

    public void draw(GraphicsContext gc) {
        // Dibujar como cuadrado (operador/camión)
        gc.setFill(color);
        gc.fillRect(x - size / 2, y - size / 2, size, size);

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRect(x - size / 2, y - size / 2, size, size);

        // Etiqueta
        gc.setFill(Color.BLACK);
        gc.setFont(new Font("Arial", 8));
        gc.fillText(name.substring(0, Math.min(3, name.length())), x - 10, y + size + 10);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void returnHome() {
        this.x = homeX;
        this.y = homeY;
        this.isMoving = false;
    }

    public double getX() { return x; }
    public double getY() { return y; }
    public String getName() { return name; }
    public void setMoving(boolean moving) { isMoving = moving; }
    public boolean isMoving() { return isMoving; }
}

