package com.simulation.routing; // Declaración del paquete

import com.simulation.locations.Location; // Importa la clase Location

public class PathNode { // Clase que representa un nodo en la red de caminos
    private final String nodeId; // Identificador único del nodo
    private final Location associatedLocation; // Ubicación asociada a este nodo
    private double xCoordinate; // Coordenada X del nodo (en el plano)
    private double yCoordinate; // Coordenada Y del nodo (en el plano)

    public PathNode(String nodeId, Location associatedLocation) { // Constructor con coordenadas por defecto 0.0
        this.nodeId = nodeId; // Asigna el ID recibido
        this.associatedLocation = associatedLocation; // Asocia la ubicación recibida
        this.xCoordinate = 0.0; // Inicializa X en cero
        this.yCoordinate = 0.0; // Inicializa Y en cero
    }

    public PathNode(String nodeId, Location associatedLocation, double x, double y) { // Constructor con coordenadas X, Y específicas
        this.nodeId = nodeId; // Asigna el ID recibido
        this.associatedLocation = associatedLocation; // Asocia la ubicación recibida
        this.xCoordinate = x; // Inicializa coordenada X
        this.yCoordinate = y; // Inicializa coordenada Y
    }

    public String getNodeId() { // Devuelve el ID del nodo
        return nodeId; // Retorna el identificador
    }

    public Location getAssociatedLocation() { // Devuelve la ubicación asociada
        return associatedLocation; // Retorna el objeto Location
    }

    public double getXCoordinate() { // Devuelve la coordenada X
        return xCoordinate; // Retorna X
    }

    public void setXCoordinate(double xCoordinate) { // Modifica la coordenada X
        this.xCoordinate = xCoordinate; // Actualiza X
    }

    public double getYCoordinate() { // Devuelve la coordenada Y
        return yCoordinate; // Retorna Y
    }

    public void setYCoordinate(double yCoordinate) { // Modifica la coordenada Y
        this.yCoordinate = yCoordinate; // Actualiza Y
    }

    public double distanceTo(PathNode other) { // Calcula la distancia euclidiana a otro nodo
        double dx = this.xCoordinate - other.xCoordinate; // Diferencia X
        double dy = this.yCoordinate - other.yCoordinate; // Diferencia Y
        return Math.sqrt(dx * dx + dy * dy); // Retorna distancia euclidiana
    }

    @Override
    public String toString() { // Representa el nodo como texto
        return "PathNode{" +
                "nodeId='" + nodeId + '\'' +
                ", location=" + (associatedLocation != null ? associatedLocation.getType().name() : "null") +
                ", coordinates=(" + xCoordinate + ", " + yCoordinate + ")" +
                '}'; // Arma la cadena con los datos principales
    }
}
