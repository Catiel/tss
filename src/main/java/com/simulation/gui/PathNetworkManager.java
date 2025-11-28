package com.simulation.gui;

import javafx.geometry.Point2D;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestor de Redes de Ruta (Path Networks) basado en el modelo ProModel.
 * Define los nodos y segmentos de ruta para el movimiento realista de recursos.
 */
public class PathNetworkManager {

    // Redes definidas en el modelo
    public static final String RED_RECEPCION = "RED_RECEPCION";
    public static final String RED_LUPULO = "RED_LUPULO";
    public static final String RED_LEVADURA = "RED_LEVADURA";
    public static final String RED_EMPACADO = "RED_EMPACADO";

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
        // --- RED_RECEPCION ---
        // N1 (MALTEADO) -> N2 (SECADO) -> N3 (MOLIENDA)
        Map<String, Point2D> recepNodes = new HashMap<>();
        recepNodes.put("N1", new Point2D(100, 250)); // MALTEADO
        recepNodes.put("N2", new Point2D(100, 400)); // SECADO
        recepNodes.put("N3", new Point2D(300, 400)); // MOLIENDA
        networks.put(RED_RECEPCION, recepNodes);

        List<PathSegment> recepSegments = new ArrayList<>();
        recepSegments.add(new PathSegment("N1", "N2"));
        recepSegments.add(new PathSegment("N2", "N3"));
        networkSegments.put(RED_RECEPCION, recepSegments);

        // --- RED_LUPULO ---
        // N1 (SILO_LUPULO) -> N2 (COCCION)
        Map<String, Point2D> lupuloNodes = new HashMap<>();
        lupuloNodes.put("N1", new Point2D(900, 100)); // SILO_LUPULO
        lupuloNodes.put("N2", new Point2D(900, 400)); // COCCION
        networks.put(RED_LUPULO, lupuloNodes);

        List<PathSegment> lupuloSegments = new ArrayList<>();
        lupuloSegments.add(new PathSegment("N1", "N2"));
        networkSegments.put(RED_LUPULO, lupuloSegments);

        // --- RED_LEVADURA ---
        // N1 (SILO_LEVADURA) -> N2 (FERMENTACION)
        Map<String, Point2D> levaduraNodes = new HashMap<>();
        levaduraNodes.put("N1", new Point2D(100, 600)); // SILO_LEVADURA
        levaduraNodes.put("N2", new Point2D(700, 600)); // FERMENTACION
        networks.put(RED_LEVADURA, levaduraNodes);

        List<PathSegment> levaduraSegments = new ArrayList<>();
        levaduraSegments.add(new PathSegment("N1", "N2"));
        networkSegments.put(RED_LEVADURA, levaduraSegments);

        // --- RED_EMPACADO ---
        // N1 (EMPACADO) -> N2 (ALMACENAJE) -> N3 (MERCADO)
        Map<String, Point2D> empacadoNodes = new HashMap<>();
        empacadoNodes.put("N1", new Point2D(1700, 400)); // EMPACADO
        empacadoNodes.put("N2", new Point2D(1700, 800)); // ALMACENAJE
        empacadoNodes.put("N3", new Point2D(1700, 1000)); // MERCADO
        networks.put(RED_EMPACADO, empacadoNodes);

        List<PathSegment> empacadoSegments = new ArrayList<>();
        empacadoSegments.add(new PathSegment("N1", "N2"));
        empacadoSegments.add(new PathSegment("N2", "N3"));
        networkSegments.put(RED_EMPACADO, empacadoSegments);
    }

    private void initializeMappings() {
        // Mapear Locaciones a Nodos de Red específicos

        // RED_RECEPCION
        locationToNodeMap.put("MALTEADO", new NetworkNodeRef(RED_RECEPCION, "N1"));
        locationToNodeMap.put("SECADO", new NetworkNodeRef(RED_RECEPCION, "N2"));
        locationToNodeMap.put("MOLIENDA", new NetworkNodeRef(RED_RECEPCION, "N3"));

        // RED_LUPULO
        locationToNodeMap.put("SILO_LUPULO", new NetworkNodeRef(RED_LUPULO, "N1"));
        locationToNodeMap.put("COCCION", new NetworkNodeRef(RED_LUPULO, "N2"));

        // RED_LEVADURA
        locationToNodeMap.put("SILO_LEVADURA", new NetworkNodeRef(RED_LEVADURA, "N1"));
        locationToNodeMap.put("FERMENTACION", new NetworkNodeRef(RED_LEVADURA, "N2"));

        // RED_EMPACADO
        locationToNodeMap.put("EMPACADO", new NetworkNodeRef(RED_EMPACADO, "N1"));
        locationToNodeMap.put("ALMACENAJE", new NetworkNodeRef(RED_EMPACADO, "N2"));
        locationToNodeMap.put("MERCADO", new NetworkNodeRef(RED_EMPACADO, "N3"));
    }

    /**
     * Obtiene la ruta (lista de puntos) para ir de una locación a otra usando una
     * red específica.
     * Si no hay red, retorna una ruta directa (línea recta).
     */
    public List<Point2D> getPath(String fromLocation, String toLocation, String resourceName) {
        List<Point2D> path = new ArrayList<>();

        // Determinar qué red usa este recurso
        String networkName = getNetworkForResource(resourceName);

        if (networkName != null) {
            NetworkNodeRef startNode = locationToNodeMap.get(fromLocation);
            NetworkNodeRef endNode = locationToNodeMap.get(toLocation);

            // Verificar si ambas locaciones están en la red del recurso
            if (startNode != null && endNode != null &&
                    startNode.network.equals(networkName) && endNode.network.equals(networkName)) {

                // Construir ruta nodo a nodo
                return buildNetworkPath(networkName, startNode.nodeName, endNode.nodeName);
            }
        }

        // Fallback: Si no hay red definida o no coincide, usar ruta directa (pero esto
        // no debería pasar si todo está bien configurado)
        // Ojo: Para movimientos sin recurso, esto se llamará con resourceName=null
        return path; // Retorna vacío, el caller deberá manejarlo (usando coordenadas directas de
        // locaciones)
    }

    /**
     * Obtiene la posición "Home" para un recurso.
     */
    public Point2D getHomePosition(String resourceName) {
        String networkName = getNetworkForResource(resourceName);
        if (networkName != null) {
            // Por defecto, Home es N1 en todas las redes según el modelo
            return networks.get(networkName).get("N1");
        }
        return null;
    }

    private String getNetworkForResource(String resourceName) {
        if (resourceName == null)
            return null;
        switch (resourceName) {
            case "OPERADOR_RECEPCION":
                return RED_RECEPCION;
            case "OPERADOR_LUPULO":
                return RED_LUPULO;
            case "OPERADOR_LEVADURA":
                return RED_LEVADURA;
            case "OPERADOR_EMPACADO":
                return RED_EMPACADO;
            case "CAMION":
                return RED_EMPACADO;
            default:
                return null;
        }
    }

    private List<Point2D> buildNetworkPath(String networkName, String startNode, String endNode) {
        List<Point2D> path = new ArrayList<>();
        Map<String, Point2D> nodes = networks.get(networkName);

        // Lógica simple para redes lineales/árboles pequeños
        // En este caso, todas las redes son lineales o muy simples.
        // Implementación específica para las redes conocidas:

        path.add(nodes.get(startNode));

        if (startNode.equals(endNode))
            return path;

        if (networkName.equals(RED_RECEPCION)) {
            // N1 <-> N2 <-> N3
            if (startNode.equals("N1") && endNode.equals("N3")) {
                path.add(nodes.get("N2"));
            } else if (startNode.equals("N3") && endNode.equals("N1")) {
                path.add(nodes.get("N2"));
            }
        } else if (networkName.equals(RED_EMPACADO)) {
            // N1 <-> N2 <-> N3
            if (startNode.equals("N1") && endNode.equals("N3")) {
                path.add(nodes.get("N2"));
            } else if (startNode.equals("N3") && endNode.equals("N1")) {
                path.add(nodes.get("N2"));
            }
        }

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
