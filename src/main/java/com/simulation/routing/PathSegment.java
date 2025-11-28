package com.simulation.routing; // Declaración del paquete

public class PathSegment { // Clase que representa un segmento o arco de la red de caminos
    private final PathNode fromNode; // Nodo de inicio del segmento
    private final PathNode toNode; // Nodo de fin del segmento
    private final double distance; // Distancia entre nodos
    private final boolean bidirectional; // Indica si el segmento es bidireccional
    private final double speedFactor; // Factor multiplicativo para la velocidad en este segmento

    public PathSegment(PathNode fromNode, PathNode toNode, double distance,
                       boolean bidirectional, double speedFactor) { // Constructor completo
        this.fromNode = fromNode; // Asigna nodo de inicio
        this.toNode = toNode; // Asigna nodo de fin
        this.distance = distance; // Asigna distancia
        this.bidirectional = bidirectional; // Asigna si es bidireccional
        this.speedFactor = speedFactor; // Asigna factor de velocidad
    }

    public PathSegment(PathNode fromNode, PathNode toNode, boolean bidirectional) { // Constructor con distancia calculada y factor 1.0
        this.fromNode = fromNode; // Asigna nodo de inicio
        this.toNode = toNode; // Asigna nodo de fin
        this.distance = fromNode.distanceTo(toNode); // Calcula distancia usando coordenadas
        this.bidirectional = bidirectional; // Asigna si es bidireccional
        this.speedFactor = 1.0; // Usa factor de velocidad unitario
    }

    public double calculateTravelTime(double baseSpeed) { // Calcula tiempo de viaje usando la velocidad base
        if (baseSpeed <= 0) { // Verifica que la velocidad sea positiva
            throw new IllegalArgumentException("La velocidad debe ser positiva"); // Lanza excepción si no es válida
        }
        double effectiveSpeed = baseSpeed * speedFactor; // Aplica el factor multiplicativo al baseSpeed
        return distance / effectiveSpeed; // Devuelve el tiempo distancia sobre velocidad efectiva
    }

    public PathNode getFromNode() { // Devuelve el nodo de inicio
        return fromNode; // Retorna fromNode
    }

    public PathNode getToNode() { // Devuelve el nodo de fin
        return toNode; // Retorna toNode
    }

    public double getDistance() { // Devuelve la distancia del segmento
        return distance; // Retorna distancia
    }

    public boolean isBidirectional() { // Indica si el segmento es bidireccional
        return bidirectional; // Retorna true o false
    }

    public double getSpeedFactor() { // Devuelve el factor de velocidad del segmento
        return speedFactor; // Retorna factor
    }

    public boolean connectsNodes(PathNode node1, PathNode node2) { // Indica si el segmento conecta ambos nodos
        if (fromNode.equals(node1) && toNode.equals(node2)) { // Si es (from, to)
            return true; // Retorna true
        }
        return bidirectional && fromNode.equals(node2) && toNode.equals(node1); // Si es bidireccional y (to, from)
    }

    @Override
    public String toString() { // Devuelve representación tipo texto del segmento
        return "PathSegment{" +
                "from=" + fromNode.getNodeId() +
                ", to=" + toNode.getNodeId() +
                ", distance=" + distance +
                ", bidirectional=" + bidirectional +
                ", speedFactor=" + speedFactor +
                '}'; // Ensambla información clave en el string
    }
}
