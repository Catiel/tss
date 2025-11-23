package com.simulation.routing; // Declaración del paquete

import java.util.*; // Importa utilidades de Java

public class PathNetwork { // Clase que representa una red de caminos/conexiones entre nodos
    private final String networkName; // Nombre de la red
    private final String networkType; // "Sobrepasar", "No Sobrepasar", etc.
    private final Map<String, PathNode> nodes; // Mapa de nodos por su ID
    private final List<PathSegment> segments; // Lista de segmentos entre nodos
    private final PathNode homeNode; // Nodo de inicio o 'base' de la red

    public PathNetwork(String networkName, String networkType, PathNode homeNode) { // Constructor
        this.networkName = networkName; // Asigna el nombre de la red
        this.networkType = networkType; // Asigna el tipo de red
        this.nodes = new HashMap<>(); // Inicializa el mapa de nodos
        this.segments = new ArrayList<>(); // Inicializa la lista de segmentos
        this.homeNode = homeNode; // Asigna el nodo base
    }

    public void addNode(PathNode node) { // Agrega un nodo a la red
        nodes.put(node.getNodeId(), node); // Inserta en el mapa por su ID
    }

    public void addSegment(PathSegment segment) { // Agrega un segmento a la red
        segments.add(segment); // Inserta en la lista de segmentos

        // Asegurar que los nodos estén en la red
        if (!nodes.containsKey(segment.getFromNode().getNodeId())) { // Si el nodo origen no está en la red
            addNode(segment.getFromNode()); // Lo agrega
        }
        if (!nodes.containsKey(segment.getToNode().getNodeId())) { // Si el nodo destino no está en la red
            addNode(segment.getToNode()); // Lo agrega
        }
    }

    public PathNode getNode(String nodeId) { // Obtiene un nodo por su ID
        return nodes.get(nodeId); // Devuelve el nodo del mapa
    }

    public List<PathSegment> getSegmentsFromNode(PathNode node) { // Segmentos que salen de un nodo (o entran si bidireccional)
        List<PathSegment> result = new ArrayList<>(); // Lista de resultado
        for (PathSegment segment : segments) { // Recorre todos los segmentos
            if (segment.getFromNode().equals(node)) { // Si el nodo es origen
                result.add(segment); // Agrega el segmento al resultado
            } else if (segment.isBidirectional() && segment.getToNode().equals(node)) { // Si segmento es bidireccional y el nodo es destino
                result.add(segment); // También lo agrega
            }
        }
        return result; // Devuelve la lista de segmentos aplicables
    }

    public PathSegment findSegment(PathNode from, PathNode to) { // Busca un segmento específico entre dos nodos
        for (PathSegment segment : segments) { // Recorre todos los segmentos
            if (segment.connectsNodes(from, to)) { // Si conecta ambos nodos
                return segment; // Devuelve el segmento
            }
        }
        return null; // Si no lo encuentra
    }

    public List<PathNode> findShortestPath(PathNode start, PathNode end) { // Algoritmo de Dijkstra para hallar la ruta más corta
        if (start == null || end == null) { // Si alguno es null
            return new ArrayList<>(); // Devuelve lista vacía
        }

        Map<PathNode, Double> distances = new HashMap<>(); // Distancias mínimas a nodos
        Map<PathNode, PathNode> previous = new HashMap<>(); // Anterior en la ruta óptima
        PriorityQueue<NodeDistancePair> queue = new PriorityQueue<>(); // Cola de prioridad según distancia

        // Inicializar distancias
        for (PathNode node : nodes.values()) { // Para todos los nodos
            distances.put(node, Double.MAX_VALUE); // Distancia infinita
        }
        distances.put(start, 0.0); // Distancia a sí mismo es 0
        queue.add(new NodeDistancePair(start, 0.0)); // Nodo inicial a la cola

        while (!queue.isEmpty()) { // Mientras haya nodos por explorar
            NodeDistancePair current = queue.poll(); // Toma el de menor distancia
            PathNode currentNode = current.node; // Nodo actual

            if (currentNode.equals(end)) { // Si es destino, detiene la búsqueda
                break;
            }

            for (PathSegment segment : getSegmentsFromNode(currentNode)) { // Segmentar todos los adyacentes
                PathNode neighbor = segment.getFromNode().equals(currentNode)
                    ? segment.getToNode() // Si soy el origen, el otro es destino
                    : segment.getFromNode(); // Si soy destino, el otro es origen

                double newDistance = distances.get(currentNode) + segment.getDistance(); // Nueva distancia potencial

                if (newDistance < distances.get(neighbor)) { // Si mejora el mínimo
                    distances.put(neighbor, newDistance); // Actualiza distancia
                    previous.put(neighbor, currentNode); // Guarda predecesor
                    queue.add(new NodeDistancePair(neighbor, newDistance)); // Agrega a la cola con nueva distancia
                }
            }
        }

        // Reconstruir camino
        List<PathNode> path = new ArrayList<>(); // Camino a devolver
        PathNode current = end; // Comienza por el destino

        while (current != null) { // Mientras haya predecesores
            path.add(0, current); // Inserta al principio del camino
            current = previous.get(current); // Mueve al anterior
        }

        return path.isEmpty() || !path.get(0).equals(start) ? new ArrayList<>() : path; // Si no llega al inicio, retorna vacío
    }

    public double calculatePathDistance(List<PathNode> path) { // Suma las distancias de una secuencia de nodos (camino)
        double totalDistance = 0.0; // Acumulador

        for (int i = 0; i < path.size() - 1; i++) { // Recorre de nodo a nodo siguiente
            PathSegment segment = findSegment(path.get(i), path.get(i + 1)); // Busca el segmento que los conecta
            if (segment != null) { // Si existe
                totalDistance += segment.getDistance(); // Suma su distancia
            }
        }

        return totalDistance; // Devuelve la distancia total
    }

    public String getNetworkName() { return networkName; } // Devuelve el nombre de la red

    public String getNetworkType() { return networkType; } // Devuelve el tipo de la red

    public PathNode getHomeNode() { return homeNode; } // Devuelve el nodo base

    public Map<String, PathNode> getNodes() { return nodes; } // Devuelve el mapa de nodos

    public List<PathSegment> getSegments() { return segments; } // Devuelve la lista de segmentos

    // Clase auxiliar para el algoritmo de Dijkstra
    private static class NodeDistancePair implements Comparable<NodeDistancePair> { // Par nodo-distancia, para la cola de prioridad
        PathNode node; // Nodo
        double distance; // Distancia acumulada

        NodeDistancePair(PathNode node, double distance) { // Constructor
            this.node = node; // Asigna nodo
            this.distance = distance; // Asigna distancia
        }

        @Override
        public int compareTo(NodeDistancePair other) { // Comparación por distancia (para ordenar la cola)
            return Double.compare(this.distance, other.distance); // Compara distancias
        }
    }

    @Override
    public String toString() { // Formatea información de la red como string
        return "PathNetwork{" +
                "name='" + networkName + '\'' +
                ", type='" + networkType + '\'' +
                ", nodes=" + nodes.size() +
                ", segments=" + segments.size() +
                ", home=" + (homeNode != null ? homeNode.getNodeId() : "null") +
                '}'; // Devuelve texto descriptivo
    }
}
