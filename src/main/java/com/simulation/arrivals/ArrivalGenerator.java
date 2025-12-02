package com.simulation.arrivals; // Declaración del paquete donde se encuentra esta clase

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.processing.OperationHandler;
import java.util.Random;

public class ArrivalGenerator { // Clase encargada de generar llegadas de entidades en la simulación
    private final SimulationEngine engine; // Referencia al motor de simulación
    private final OperationHandler operationHandler; // Manejador de operaciones para procesar las llegadas de entidades
    private static final Random random = new Random(); // Generador de números aleatorios

    public ArrivalGenerator(SimulationEngine engine) { // Constructor que inicializa el generador de llegadas
        this.engine = engine; // Asigna el motor de simulación recibido como parámetro
        this.operationHandler = new OperationHandler(engine); // Crea una nueva instancia del manejador de operaciones
    }

    public void scheduleArrivals(String entityTypeName, String locationName, // Método que programa múltiples llegadas
                                                                             // de entidades
            double firstTime, int occurrences, double frequency) {
        scheduleArrivals(entityTypeName, locationName, firstTime, occurrences, frequency, false);
    }

    public void scheduleArrivals(String entityTypeName, String locationName, // Método que programa múltiples llegadas
                                                                             // de entidades con distribución
            double firstTime, int occurrences, double frequency, boolean useExponential) {
        EntityType entityType = engine.getEntityType(entityTypeName); // Obtiene el tipo de entidad desde el motor
                                                                      // usando su nombre

        if (entityType == null) { // Verifica si el tipo de entidad existe
            System.err.println("Tipo de entidad no encontrado: " + entityTypeName); // Imprime mensaje de error si no se
                                                                                    // encuentra el tipo
            return; // Sale del método si no existe el tipo de entidad
        }

        double currentArrivalTime = firstTime;
        for (int i = 0; i < occurrences; i++) { // Ciclo que genera cada una de las llegadas programadas
            final double arrivalTime = currentArrivalTime; // Variable final para usar en clase anónima

            Event arrivalEvent = new Event(arrivalTime, 0, // Crea un nuevo evento de llegada con tiempo, prioridad y
                                                           // descripción
                    "Arrival of " + entityTypeName + " at " + locationName) {
                @Override // Define el método que se ejecutará cuando ocurra este evento
                public void execute() {
                    Entity entity = new Entity(entityType, false, engine.getClock().getCurrentTime()); // Crea una nueva
                                                                                                       // instancia de
                                                                                                       // la entidad del
                                                                                                       // tipo
                                                                                                       // especificado
                    operationHandler.handleArrival(entity, locationName); // Procesa la llegada de la entidad en la
                                                                          // ubicación indicada
                }
            };

            engine.getScheduler().scheduleEvent(arrivalEvent); // Programa el evento de llegada en el planificador del
                                                               // motor

            // Calcula el tiempo hasta el próximo arribo
            if (useExponential) {
                // Distribución exponencial: -mean * ln(1-U)
                double interarrivalTime = -frequency * Math.log(1.0 - random.nextDouble());
                currentArrivalTime += interarrivalTime;
            } else {
                // Frecuencia fija
                currentArrivalTime += frequency;
            }
        }
    }
}
