package com.simulation.resources;

/**
 * Locación tipo Buffer (Almacén)
 * Representa almacenes intermedios como ALMACEN_PINTURA y ALMACEN_HORNO
 * No procesa, solo almacena
 */
public class BufferLocation extends Location {

    public BufferLocation(String name, int capacity) {
        super(name, capacity);
    }
}
