package com.simulation.gui;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de Redes de Ruta (Path Networks) para Steel Gears.
 * Define los nodos y segmentos de ruta para el movimiento realista de recursos.
 */
public class PathNetworkManager {

    // Redes definidas en el modelo
    public static final String RED_GRUA = "RED_GRUA";
    public static final String RED_ROBOT = "RED_ROBOT";

    // Mapa de redes: NombreRed -> Mapa de Nodos (NombreNodo -> Coordenada)
    private final Map<String, Map<String, Point2D>> networks = new HashMap<>();

    // Mapa de conexiones: NombreRed -> Lista de Segmentos
    private final Map<String, List<PathSegment>> networkSegments = new HashMap<>();

    // Mapa de Locación -> Nodo de Red (para saber dónde conectar)
    private final Map<String, NetworkNodeRef> locationToNodeMap = new HashMap<>();

    public PathNetworkManager() {
        initializeNetworks();
        initializeMappings();
    }

    private void initializeNetworks() {
        // --- RED_GRUA ---
        // N1 (ALMACEN_MP) -> N2 (HORNO) -> N3 (BANDA_1)
        Map<String, Point2D> gruaNodes = new HashMap<>();
        gruaNodes.put("N1", new Point2D(50, 300)); // ALMACEN_MP
        gruaNodes.put("N2", new Point2D(250, 300)); // HORNO
        gruaNodes.put("N3", new Point2D(450, 300)); // BANDA_1
        networks.put(RED_GRUA, gruaNodes);

        List<PathSegment> gruaSegments = new ArrayList<>();
        gruaSegments.add(new PathSegment("N1", "N2"));
        gruaSegments.add(new PathSegment("N2", "N3"));
        networkSegments.put(RED_GRUA, gruaSegments);

        // --- RED_ROBOT ---
        // N1 (CARGA) -> N2 (TORNEADO) -> N3 (FRESADO) -> N4 (TALADRO) -> N5
        // (RECTIFICADO) -> N6 (DESCARGA)
        Map<String, Point2D> robotNodes = new HashMap<>();
        robotNodes.put("N1", new Point2D(650, 300)); // CARGA
        robotNodes.put("N2", new Point2D(850, 150)); // TORNEADO
        robotNodes.put("N3", new Point2D(1050, 150)); // FRESADO
        robotNodes.put("N4", new Point2D(1050, 450)); // TALADRO
        robotNodes.put("N5", new Point2D(850, 450)); // RECTIFICADO
        robotNodes.put("N6", new Point2D(650, 500)); // DESCARGA
        networks.put(RED_ROBOT, robotNodes);

        List<PathSegment> robotSegments = new ArrayList<>();
        robotSegments.add(new PathSegment("N1", "N2"));
        robotSegments.add(new PathSegment("N2", "N3"));
        robotSegments.add(new PathSegment("N3", "N4"));
        robotSegments.add(new PathSegment("N4", "N5"));
        robotSegments.add(new PathSegment("N5", "N6"));
        networkSegments.put(RED_ROBOT, robotSegments);
    }

    private void initializeMappings() {
        // Mapear Locaciones a Nodos de Red específicos

        // RED_GRUA
        locationToNodeMap.put("ALMACEN_MP", new NetworkNodeRef(RED_GRUA, "N1"));
        locationToNodeMap.put("HORNO", new NetworkNodeRef(RED_GRUA, "N2"));
        locationToNodeMap.put("BANDA_1", new NetworkNodeRef(RED_GRUA, "N3"));

        // RED_ROBOT
        locationToNodeMap.put("CARGA", new NetworkNodeRef(RED_ROBOT, "N1"));
        locationToNodeMap.put("TORNEADO", new NetworkNodeRef(RED_ROBOT, "N2"));
        locationToNodeMap.put("FRESADO", new NetworkNodeRef(RED_ROBOT, "N3"));
        locationToNodeMap.put("TALADRO", new NetworkNodeRef(RED_ROBOT, "N4"));
        locationToNodeMap.put("RECTIFICADO", new NetworkNodeRef(RED_ROBOT, "N5"));
        locationToNodeMap.put("DESCARGA", new NetworkNodeRef(RED_ROBOT, "N6"));
    }

    /**
     * Obtiene la ruta (lista de puntos) para ir de una locación a otra usando una
     * red específica.
     */
    public List<Point2D> getPath(String fromLocation, String toLocation, String resourceName) {
        List<Point2D> path = new ArrayList<>();

        // Determinar qué red usa este recurso
        String networkName = getNetworkForResource(resourceName);

        if (networkName != null) {
            NetworkNodeRef startNode = locationToNodeMap.get(fromLocation);
            NetworkNodeRef endNode = locationToNodeMap.get(toLocation);

            // Manejo especial para puntos compartidos o si no están en el mapa principal
            if (startNode == null && fromLocation.equals("CARGA") && networkName.equals(RED_ROBOT)) {
                startNode = new NetworkNodeRef(RED_ROBOT, "N2");
            }
            if (endNode == null && toLocation.equals("CARGA") && networkName.equals(RED_ROBOT)) {
                endNode = new NetworkNodeRef(RED_ROBOT, "N2");
            }

            if (startNode != null && endNode != null &&
                    startNode.network.equals(networkName) && endNode.network.equals(networkName)) {

                // Construir ruta nodo a nodo
                return buildNetworkPath(networkName, startNode.nodeName, endNode.nodeName);
            }
        }

        return path;
    }

    /**
     * Obtiene la posición "Home" para un recurso.
     */
    public Point2D getHomePosition(String resourceName) {
        String networkName = getNetworkForResource(resourceName);
        if (networkName != null) {
            // Home es N1
            return networks.get(networkName).get("N1");
        }
        return null;
    }

    private String getNetworkForResource(String resourceName) {
        if (resourceName == null)
            return null;
        switch (resourceName) {
            case "GRUA_VIAJERA":
                return RED_GRUA;
            case "ROBOT":
                return RED_ROBOT;
            default:
                return null;
        }
    }

    private List<Point2D> buildNetworkPath(String networkName, String startNode, String endNode) {
        List<Point2D> path = new ArrayList<>();
        Map<String, Point2D> nodes = networks.get(networkName);

        path.add(nodes.get(startNode));

        if (startNode.equals(endNode))
            return path;

        path.add(nodes.get(endNode));
        return path;
    }

    // Clases auxiliares
    private static class PathSegment {
        String fromNode;
        String toNode;

        PathSegment(String from, String to) {
            this.fromNode = from;
            this.toNode = to;
        }
    }

    private static class NetworkNodeRef {
        String network;
        String nodeName;

        NetworkNodeRef(String network, String nodeName) {
            this.network = network;
            this.nodeName = nodeName;
        }
    }
}
