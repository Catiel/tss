package com.simulation.processing; // Declaración del paquete

public record RoutingRule(String destinationLocation, double probability, int quantity, String moveLogic,
                          // Record que define reglas de enrutamiento para entidades en la simulación
                          String resourceName) { // Parametro opcional para recurso empleado en el movimiento (ejemplo: operador, camión)
} // El record genera automáticamente constructor, getters, equals, hashCode y toString; todos sus campos son finales
