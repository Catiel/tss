package com.simulation.core; // Declaración del paquete donde se encuentra esta clase

import com.simulation.arrivals.ArrivalGenerator; // Importa el generador de llegadas de entidades
import com.simulation.entities.Entity; // Importa la clase que representa entidades individuales
import com.simulation.entities.EntityType; // Importa la clase que define tipos de entidades
import com.simulation.locations.Location; // Importa la clase que representa ubicaciones en la simulación
import com.simulation.locations.LocationType; // Importa la clase que define tipos de ubicaciones
import com.simulation.processing.ProcessingRule; // Importa las reglas de procesamiento para ubicaciones
import com.simulation.resources.Resource; // Importa la clase que representa recursos compartidos
import com.simulation.resources.ResourceType; // Importa la clase que define tipos de recursos
import com.simulation.statistics.StatisticsCollector; // Importa el recolector de estadísticas de la simulación

import java.util.ArrayList; // Importa la clase ArrayList para listas dinámicas
import java.util.HashMap; // Importa la clase HashMap para mapas clave-valor
import java.util.List; // Importa la interfaz List
import java.util.Map; // Importa la interfaz Map

public class SimulationEngine { // Clase principal que coordina todos los componentes de la simulación
    private final SimulationClock clock; // Reloj que mantiene el tiempo actual de la simulación
    private final EventScheduler scheduler; // Planificador que gestiona la cola de eventos
    private final StatisticsCollector statistics; // Recolector que calcula y almacena estadísticas
    private final Map<String, EntityType> entityTypes; // Mapa que almacena todos los tipos de entidades definidos
    private final Map<String, Location> locations; // Mapa que almacena todas las ubicaciones de la simulación
    private final Map<String, Resource> resources; // Mapa que almacena todos los recursos compartidos
    private final Map<String, ProcessingRule> processingRules; // Mapa que almacena las reglas de procesamiento por ubicación
    private final ArrivalGenerator arrivalGenerator; // Generador que programa llegadas de entidades
    private double simulationEndTime; // Tiempo en el que debe finalizar la simulación
    private final List<SimulationListener> listeners = new ArrayList<>(); // Lista de observadores que escuchan eventos de la simulación

    public SimulationEngine() { // Constructor que inicializa todos los componentes del motor de simulación
        this.clock = new SimulationClock(); // Crea un nuevo reloj de simulación inicializado en tiempo cero
        this.scheduler = new EventScheduler(clock); // Crea el planificador de eventos asociado al reloj
        this.statistics = new StatisticsCollector(); // Crea el recolector de estadísticas
        this.entityTypes = new HashMap<>(); // Inicializa el mapa de tipos de entidades vacío
        this.locations = new HashMap<>(); // Inicializa el mapa de ubicaciones vacío
        this.resources = new HashMap<>(); // Inicializa el mapa de recursos vacío
        this.processingRules = new HashMap<>(); // Inicializa el mapa de reglas de procesamiento vacío
        this.arrivalGenerator = new ArrivalGenerator(this); // Crea el generador de llegadas asociado a este motor
    }

    public void addEntityType(String name, double speed) { // Método para agregar un nuevo tipo de entidad al modelo
        entityTypes.put(name, new EntityType(name, speed)); // Crea y almacena el tipo de entidad con su nombre y velocidad
    }

    public void addLocation(String name, int capacity, int units) { // Método para agregar una nueva ubicación al modelo
        locations.put(name, new Location(new LocationType(name, capacity, units))); // Crea y almacena la ubicación con su nombre, capacidad y unidades
    }

    public void addResource(String name, int units, double speed) { // Método para agregar un nuevo recurso al modelo
        resources.put(name, new Resource(new ResourceType(name, units, speed))); // Crea y almacena el recurso con su nombre, unidades disponibles y velocidad
    }

    public void addProcessingRule(ProcessingRule rule) { // Método para agregar una regla de procesamiento a una ubicación
        processingRules.put(rule.getLocationName(), rule); // Almacena la regla usando el nombre de la ubicación como clave
    }

    public void scheduleArrival(String entityTypeName, String locationName, // Método para programar llegadas de entidades
                               double firstTime, int occurrences, double frequency) {
        arrivalGenerator.scheduleArrivals(entityTypeName, locationName, // Delega al generador de llegadas la programación de eventos de arribo
                                         firstTime, occurrences, frequency);
    }

    public void run(double endTime) { // Método principal para ejecutar la simulación completa hasta un tiempo final
        this.simulationEndTime = endTime; // Establece el tiempo de finalización de la simulación

        while (scheduler.hasEvents() && clock.getCurrentTime() < endTime) { // Bucle principal mientras haya eventos y no se alcance el tiempo final
            Event event = scheduler.getNextEvent(); // Obtiene el próximo evento de la cola
            clock.advanceTo(event.getScheduledTime()); // Avanza el reloj al tiempo del evento
            event.execute(); // Ejecuta la acción asociada al evento
        }

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime()); // Calcula las estadísticas finales de todas las ubicaciones
    }

    public void setEndTime(double endTime) { // Método para establecer el tiempo de finalización de la simulación
        this.simulationEndTime = endTime; // Asigna el tiempo final de simulación
    }

    public boolean step(double speedMultiplier) { // Método para ejecutar la simulación paso a paso (para GUI)
        if (!scheduler.hasEvents() || clock.getCurrentTime() >= simulationEndTime) { // Verifica si quedan eventos o si se alcanzó el tiempo final
            return false; // Retorna falso indicando que la simulación ha terminado
        }

        Event event = scheduler.getNextEvent(); // Obtiene el siguiente evento a ejecutar
        clock.advanceTo(event.getScheduledTime()); // Avanza el reloj al tiempo de ese evento
        event.execute(); // Ejecuta la acción del evento

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime()); // Actualiza las estadísticas parciales después del evento

        return scheduler.hasEvents() && clock.getCurrentTime() < simulationEndTime; // Retorna verdadero si aún hay más eventos pendientes
    }

    public void addListener(SimulationListener listener) { // Método para registrar un observador de eventos
        listeners.add(listener); // Agrega el observador a la lista de listeners
    }

    public void removeListener(SimulationListener listener) { // Método para desregistrar un observador
        listeners.remove(listener); // Elimina el observador de la lista
    }

    public void notifyEntityArrival(Entity entity, Location location) { // Método para notificar a los observadores sobre la llegada de una entidad
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores registrados
            listener.onEntityArrival(entity, location, clock.getCurrentTime()); // Notifica al observador con los detalles de la llegada
        }
    }

    public void notifyEntityMove(Entity entity, Location from, Location to) { // Método para notificar sobre el movimiento de una entidad entre ubicaciones
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores
            listener.onEntityMove(entity, from, to, clock.getCurrentTime()); // Notifica al observador con origen, destino y tiempo
        }
    }

    public void notifyEntityExit(Entity entity, Location from) { // Método para notificar sobre la salida de una entidad del sistema
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores
            listener.onEntityExit(entity, from, clock.getCurrentTime()); // Notifica al observador sobre la salida de la entidad
        }
    }

    public void notifyResourceAcquired(Resource resource, Entity entity) { // Método para notificar cuando una entidad adquiere un recurso
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores
            listener.onResourceAcquired(resource, entity, clock.getCurrentTime()); // Notifica sobre la adquisición del recurso
        }
    }

    public void notifyResourceReleased(Resource resource, Entity entity) { // Método para notificar cuando una entidad libera un recurso
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores
            listener.onResourceReleased(resource, entity, clock.getCurrentTime()); // Notifica sobre la liberación del recurso
        }
    }

    public void notifyEntityCreated(Entity entity, Location location) { // Método para notificar sobre la creación de una nueva entidad
        for (SimulationListener listener : listeners) { // Itera sobre todos los observadores
            listener.onEntityCreated(entity, location, clock.getCurrentTime()); // Notifica sobre la creación de la entidad en la ubicación especificada
        }
    }

    public SimulationClock getClock() { return clock; } // Retorna la referencia al reloj de simulación
    public EventScheduler getScheduler() { return scheduler; } // Retorna la referencia al planificador de eventos
    public StatisticsCollector getStatistics() { return statistics; } // Retorna la referencia al recolector de estadísticas
    public EntityType getEntityType(String name) { return entityTypes.get(name); } // Busca y retorna un tipo de entidad por su nombre
    public Location getLocation(String name) { return locations.get(name); } // Busca y retorna una ubicación por su nombre
    public Resource getResource(String name) { return resources.get(name); } // Busca y retorna un recurso por su nombre
    public ProcessingRule getProcessingRule(String location) { return processingRules.get(location); } // Busca y retorna la regla de procesamiento de una ubicación
    public Map<String, Location> getAllLocations() { return locations; } // Retorna el mapa completo de todas las ubicaciones
    public Map<String, EntityType> getAllEntityTypes() { return entityTypes; } // Retorna el mapa completo de todos los tipos de entidades
    public Map<String, Resource> getAllResources() { return resources; } // Retorna el mapa completo de todos los recursos
}
