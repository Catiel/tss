package com.simulation.arrivals; // Declaración del paquete donde se encuentra esta clase

import com.simulation.core.Event; // Importación de la clase Event del núcleo de la simulación
import com.simulation.core.SimulationEngine; // Importación del motor principal de simulación
import com.simulation.entities.Entity; // Importación de la clase Entity que representa entidades en la simulación
import com.simulation.entities.EntityType; // Importación de la clase EntityType que define tipos de entidades
import com.simulation.processing.OperationHandler; // Importación del manejador de operaciones para procesar llegadas

public class ArrivalGenerator { // Clase encargada de generar llegadas de entidades en la simulación
    private final SimulationEngine engine; // Referencia al motor de simulación
    private final OperationHandler operationHandler; // Manejador de operaciones para procesar las llegadas de entidades

    public ArrivalGenerator(SimulationEngine engine) { // Constructor que inicializa el generador de llegadas
        this.engine = engine; // Asigna el motor de simulación recibido como parámetro
        this.operationHandler = new OperationHandler(engine); // Crea una nueva instancia del manejador de operaciones
    }

    public void scheduleArrivals(String entityTypeName, String locationName, // Método que programa múltiples llegadas de entidades
                                double firstTime, int occurrences, double frequency) {
        EntityType entityType = engine.getEntityType(entityTypeName); // Obtiene el tipo de entidad desde el motor usando su nombre

        if (entityType == null) { // Verifica si el tipo de entidad existe
            System.err.println("Tipo de entidad no encontrado: " + entityTypeName); // Imprime mensaje de error si no se encuentra el tipo
            return; // Sale del método si no existe el tipo de entidad
        }

        for (int i = 0; i < occurrences; i++) { // Ciclo que genera cada una de las llegadas programadas
            double arrivalTime = firstTime + (i * frequency); // Calcula el tiempo de llegada: tiempo inicial más frecuencia multiplicada por índice

            Event arrivalEvent = new Event(arrivalTime, 0, // Crea un nuevo evento de llegada con tiempo, prioridad y descripción
                "Arrival of " + entityTypeName + " at " + locationName) {
                @Override // Define el método que se ejecutará cuando ocurra este evento
                public void execute() {
                    Entity entity = new Entity(entityType); // Crea una nueva instancia de la entidad del tipo especificado
                    operationHandler.handleArrival(entity, locationName); // Procesa la llegada de la entidad en la ubicación indicada
                }
            };

            engine.getScheduler().scheduleEvent(arrivalEvent); // Programa el evento de llegada en el planificador del motor
        }
    }
}
