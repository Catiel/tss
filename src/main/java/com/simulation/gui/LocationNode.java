package com.simulation.gui;

import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;

public class LocationNode {

    private final String name;
    private double x, y;
    private double width, height;
    private Color color;
    private int currentOccupancy;
    private int capacity;

    public LocationNode(String name, double x, double y, double width, double height, Color color) {
        this.name = name;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.color = color;
        this.currentOccupancy = 0;
        this.capacity = 10;
    }

    public void draw(GraphicsContext gc) {
        // Dibujar rectángulo de la locación
        gc.setFill(color);
        gc.fillRoundRect(x, y, width, height, 10, 10);

        // Borde
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(2);
        gc.strokeRoundRect(x, y, width, height, 10, 10);

        // Nombre de la locación
        gc.setFill(Color.WHITE);
        gc.setFont(new Font("Arial", 10));
        gc.setTextAlign(TextAlignment.CENTER);
        gc.fillText(name, x + width / 2, y + height / 2);

        // Mostrar ocupación
        gc.setFill(Color.YELLOW);
        gc.fillText(currentOccupancy + "/" + capacity, x + width / 2, y + height / 2 + 15);
    }

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

    // Getters
    public String getName() { return name; }
    public double getX() { return x; }
    public double getY() { return y; }
    public double getWidth() { return width; }
    public double getHeight() { return height; }
}

