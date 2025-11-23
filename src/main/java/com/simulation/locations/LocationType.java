package com.simulation.locations; // Declaración del paquete donde se encuentra este record

public record LocationType(String name, int capacity, int units) { // Record que define un tipo de ubicación con nombre, capacidad y número de unidades (genera automáticamente constructor, getters, equals, hashCode y toString)
} // Fin del record (los records son clases inmutables que encapsulan datos de forma compacta)
