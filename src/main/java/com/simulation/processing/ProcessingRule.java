package com.simulation.processing; // Declaración del paquete donde se encuentra esta clase

import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;

public abstract class ProcessingRule { // Clase abstracta que define la regla base para procesamiento de entidades
    protected final String locationName; // Nombre de la ubicación donde aplica esta regla (inmutable, accesible a subclases)
    protected final String entityTypeName; // Nombre del tipo de entidad que procesa esta regla (inmutable, accesible a subclases)
    protected final double processingTime; // Tiempo de procesamiento en minutos (inmutable, accesible a subclases)

    public ProcessingRule(String locationName, String entityTypeName, double processingTime) { // Constructor que inicializa la regla de procesamiento
        this.locationName = locationName; // Asigna el nombre de la ubicación
        this.entityTypeName = entityTypeName; // Asigna el tipo de entidad
        this.processingTime = processingTime; // Asigna el tiempo de procesamiento
    }

    public abstract void process(Entity entity, SimulationEngine engine); // Método abstracto que las subclases deben implementar para definir el procesamiento específico

    public String getLocationName() { // Método getter para obtener el nombre de la ubicación
        return locationName; // Retorna el nombre de la ubicación
    }

    public String getEntityTypeName() { // Método getter para obtener el tipo de entidad
        return entityTypeName; // Retorna el nombre del tipo de entidad
    }

    public double getProcessingTime() { // Método getter para obtener el tiempo de procesamiento
        return processingTime; // Retorna el tiempo de procesamiento en minutos
    }
}
