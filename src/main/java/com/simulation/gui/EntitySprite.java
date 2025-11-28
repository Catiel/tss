package com.simulation.gui;

import com.simulation.entities.Entity;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;

public class EntitySprite {

    private final Entity entity;
    private final double size = 8;
    private final Color color;
    private double x, y;

    public EntitySprite(Entity entity, double x, double y) {
        this.entity = entity;
        this.x = x;
        this.y = y;
        this.color = getColorForEntityType(entity.getType().getName());
    }

    private Color getColorForEntityType(String type) {
        switch (type) {
            case "GRANOS_DE_CEBADA":
                return Color.WHEAT;
            case "LUPULO":
                return Color.DARKGREEN;
            case "LEVADURA":
                return Color.YELLOW;
            case "MOSTO":
                return Color.BROWN;
            case "CERVEZA":
                return Color.GOLDENROD;
            case "BOTELLA_CON_CERVEZA":
                return Color.DARKGOLDENROD;
            case "CAJA_VACIA":
                return Color.BURLYWOOD;
            case "CAJA_CON_CERVEZAS":
                return Color.DARKORANGE;
            default:
                return Color.GRAY;
        }
    }

    public void draw(GraphicsContext gc) {
        gc.setFill(color);
        gc.fillOval(x - size / 2, y - size / 2, size, size);

        // Borde
        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);
        gc.strokeOval(x - size / 2, y - size / 2, size, size);
    }

    public void setPosition(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Entity getEntity() {
        return entity;
    }
}

