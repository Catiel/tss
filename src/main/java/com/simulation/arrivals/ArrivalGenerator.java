package com.simulation.arrivals; // Declaración del paquete de generación de llegadas

import com.simulation.core.Event; // Importa la clase Event para crear eventos de simulación
import com.simulation.core.SimulationEngine; // Importa el motor principal de simulación
import com.simulation.entities.Entity; // Importa la clase que representa entidades individuales
import com.simulation.entities.EntityType; // Importa la clase que define tipos de entidades
import com.simulation.processing.OperationHandler; // Importa el manejador de operaciones de procesamiento
import java.util.Random; // Importa la clase Random para generación de números aleatorios

public class ArrivalGenerator { // Define la clase generadora de llegadas de entidades
    private final SimulationEngine engine; // Referencia al motor de simulación para acceder a sus componentes
    private final OperationHandler operationHandler; // Manejador que procesa las operaciones de llegada de entidades
    private static final Random random = new Random(); // Generador de números aleatorios compartido por todas las instancias

    public ArrivalGenerator(SimulationEngine engine) { // Constructor que recibe el motor de simulación
        this.engine = engine; // Asigna la referencia del motor a la variable de instancia
        this.operationHandler = new OperationHandler(engine); // Crea el manejador de operaciones asociado al motor
    }

    public void scheduleArrivals(String entityTypeName, String locationName, // Método para programar llegadas con frecuencia fija
            double firstTime, int occurrences, double frequency) { // Parámetros: tipo, ubicación, tiempo inicial, cantidad y frecuencia
        scheduleArrivals(entityTypeName, locationName, firstTime, occurrences, frequency, false); // Delega al método sobrecargado con distribución uniforme
    }

    public void scheduleArrivals(String entityTypeName, String locationName, // Método sobrecargado para programar llegadas con distribución seleccionable
            double firstTime, int occurrences, double frequency, boolean useExponential) { // Parámetros incluyen flag para distribución exponencial
        EntityType entityType = engine.getEntityType(entityTypeName); // Recupera el tipo de entidad desde el motor usando el nombre

        if (entityType == null) { // Verifica si el tipo de entidad fue encontrado en el motor
            System.err.println("Tipo de entidad no encontrado: " + entityTypeName); // Imprime error en salida de errores estándar
            return; // Termina la ejecución del método si el tipo no existe
        }

        double currentArrivalTime = firstTime; // Inicializa el tiempo de la primera llegada
        for (int i = 0; i < occurrences; i++) { // Itera para crear cada una de las llegadas programadas
            final double arrivalTime = currentArrivalTime; // Crea variable final con tiempo actual para usar en clase anónima

            Event arrivalEvent = new Event(arrivalTime, 0, // Instancia un nuevo evento de llegada con tiempo y prioridad cero
                    "Arrival of " + entityTypeName + " at " + locationName) { // Descripción del evento con tipo y ubicación
                @Override // Anotación que indica sobrescritura del método execute de Event
                public void execute() { // Define la lógica que se ejecutará cuando ocurra el evento
                    Entity entity = new Entity(entityType, false, engine.getClock().getCurrentTime()); // Crea nueva entidad con tipo, flag false y tiempo actual
                    operationHandler.handleArrival(entity, locationName); // Delega el manejo de la llegada al operationHandler
                }
            };

            engine.getScheduler().scheduleEvent(arrivalEvent); // Añade el evento al planificador del motor para su ejecución futura

            // Calcula el tiempo hasta el próximo arribo
            if (useExponential) { // Verifica si se debe usar distribución exponencial
                // Distribución exponencial: -mean * ln(1-U)
                double interarrivalTime = -frequency * Math.log(1.0 - random.nextDouble()); // Calcula tiempo inter-arribo con distribución exponencial usando transformación inversa
                currentArrivalTime += interarrivalTime; // Incrementa el tiempo actual con el inter-arribo calculado
            } else { // Si no se usa distribución exponencial
                // Frecuencia fija
                currentArrivalTime += frequency; // Incrementa el tiempo actual con la frecuencia constante
            }
        }
    }
}
