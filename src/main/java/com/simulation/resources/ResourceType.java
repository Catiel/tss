package com.simulation.resources; // Declaración del paquete donde se encuentra este record

public record ResourceType(String name, int units, double speedMetersPerMinute) { // Record que define el tipo de recurso con nombre, cantidad de unidades y velocidad en metros por minuto
} // El record genera automáticamente constructor, getters, equals, hashCode, y toString para sus campos[web:147][web:151][web:90]
