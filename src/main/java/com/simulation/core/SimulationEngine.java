package com.simulation.core; // Declaración del paquete principal de simulación

import com.simulation.arrivals.ArrivalGenerator; // Importa la clase que genera llegadas de entidades
import com.simulation.entities.Entity; // Importa la clase que representa entidades individuales
import com.simulation.entities.EntityType; // Importa la clase que define tipos de entidades
import com.simulation.locations.Location; // Importa la clase que representa ubicaciones en la simulación
import com.simulation.locations.LocationType; // Importa la clase que define tipos de ubicaciones
import com.simulation.processing.ProcessingRule; // Importa la clase que define reglas de procesamiento
import com.simulation.resources.Resource; // Importa la clase que representa recursos disponibles
import com.simulation.resources.ResourceType; // Importa la clase que define tipos de recursos
import com.simulation.routing.Route; // Importa la clase que define rutas entre ubicaciones
import com.simulation.statistics.StatisticsCollector; // Importa el recolector de estadísticas de la simulación

import java.util.ArrayList; // Importa la clase ArrayList para listas dinámicas
import java.util.HashMap; // Importa la clase HashMap para mapas clave-valor
import java.util.List; // Importa la interfaz List para manejo de listas
import java.util.Map; // Importa la interfaz Map para manejo de mapas

public class SimulationEngine { // Define la clase principal del motor de simulación
    private final SimulationClock clock; // Reloj de la simulación para llevar el tiempo actual
    private final EventScheduler scheduler; // Programador de eventos para gestionar la cola de eventos
    private final StatisticsCollector statistics; // Colector de estadísticas para métricas de simulación
    private final Map<String, EntityType> entityTypes; // Mapa de tipos de entidades indexados por nombre
    private final Map<String, Location> locations; // Mapa de ubicaciones indexadas por nombre
    private final Map<String, Resource> resources; // Mapa de recursos indexados por nombre
    private final Map<String, ProcessingRule> processingRules; // Mapa de reglas de procesamiento por ubicación
    private final Map<String, Route> routes; // Mapa de rutas usando clave fromLocation_toLocation
    private final ArrivalGenerator arrivalGenerator; // Generador para crear llegadas de entidades programadas
    private final List<SimulationListener> listeners = new ArrayList<>(); // Lista de listeners para notificar eventos
    private double simulationEndTime; // Tiempo de finalización de la simulación

    public SimulationEngine() { // Constructor del motor de simulación
        this.clock = new SimulationClock(); // Inicializa el reloj de simulación
        this.scheduler = new EventScheduler(clock); // Inicializa el programador de eventos con el reloj
        this.statistics = new StatisticsCollector(); // Inicializa el colector de estadísticas
        this.entityTypes = new HashMap<>(); // Inicializa el mapa de tipos de entidades vacío
        this.locations = new HashMap<>(); // Inicializa el mapa de ubicaciones vacío
        this.resources = new HashMap<>(); // Inicializa el mapa de recursos vacío
        this.processingRules = new HashMap<>(); // Inicializa el mapa de reglas de procesamiento vacío
        this.routes = new HashMap<>(); // Inicializa el mapa de rutas vacío
        this.arrivalGenerator = new ArrivalGenerator(this); // Inicializa el generador de llegadas con referencia al motor
    }

    public void addEntityType(String name, double speed) { // Método para agregar un nuevo tipo de entidad
        entityTypes.put(name, new EntityType(name, speed)); // Crea y almacena el tipo de entidad con nombre y velocidad
    }

    public void addLocation(String name, int capacity, int units) { // Método para agregar una nueva ubicación
        locations.put(name, new Location(new LocationType(name, capacity, units))); // Crea y almacena la ubicación con capacidad y unidades
    }

    public void addResource(String name, int units, double speed) { // Método para agregar un nuevo recurso
        resources.put(name, new Resource(new ResourceType(name, units, speed))); // Crea y almacena el recurso con unidades y velocidad
    }

    public void addProcessingRule(ProcessingRule rule) { // Método para agregar una regla de procesamiento
        processingRules.put(rule.getLocationName(), rule); // Almacena la regla indexada por el nombre de ubicación
    }

    public void addRoute(String from, String to, String resourceName, double moveTime) { // Método para agregar una ruta entre ubicaciones
        String key = from + "_" + to; // Crea la clave concatenando origen y destino con guión bajo
        routes.put(key, new Route(from, to, resourceName, moveTime)); // Almacena la ruta con recurso y tiempo de movimiento
    }

    public Route getRoute(String from, String to) { // Método para obtener una ruta entre dos ubicaciones
        String key = from + "_" + to; // Construye la clave concatenando origen y destino
        return routes.get(key); // Retorna la ruta correspondiente o null si no existe
    }

    public void scheduleArrival(String entityTypeName, String locationName, // Método para programar llegadas de entidades con distribución uniforme
            double firstTime, int occurrences, double frequency) { // Parámetros: tipo, ubicación, tiempo inicial, ocurrencias y frecuencia
        arrivalGenerator.scheduleArrivals(entityTypeName, locationName, // Delega al generador de llegadas para programar las llegadas
                firstTime, occurrences, frequency); // Pasa todos los parámetros al generador
    }

    public void scheduleArrival(String entityTypeName, String locationName, // Sobrecarga del método para programar llegadas con opción exponencial
            double firstTime, int occurrences, double frequency, boolean useExponential) { // Parámetros adicionales incluyen flag de distribución exponencial
        arrivalGenerator.scheduleArrivals(entityTypeName, locationName, // Delega al generador de llegadas con distribución seleccionable
                firstTime, occurrences, frequency, useExponential); // Pasa todos los parámetros incluyendo el flag exponencial
    }

    public void run(double endTime) { // Método para ejecutar la simulación completa hasta el tiempo final
        this.simulationEndTime = endTime; // Establece el tiempo de finalización de la simulación

        while (scheduler.hasEvents() && clock.getCurrentTime() < endTime) { // Bucle mientras haya eventos y no se alcance el tiempo final
            Event event = scheduler.getNextEvent(); // Obtiene el siguiente evento de la cola del programador
            clock.advanceTo(event.getScheduledTime()); // Avanza el reloj al tiempo programado del evento
            event.execute(); // Ejecuta el evento actual
        }

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime()); // Calcula estadísticas finales de todas las ubicaciones
        statistics.calculateResourceStatistics(resources, clock.getCurrentTime()); // Calcula estadísticas finales de todos los recursos
    }

    public void setEndTime(double endTime) { // Método para establecer el tiempo de finalización
        this.simulationEndTime = endTime; // Asigna el tiempo final de simulación
    }

    public boolean step(double speedMultiplier) { // Método para ejecutar un paso de simulación con multiplicador de velocidad
        if (!scheduler.hasEvents() || clock.getCurrentTime() >= simulationEndTime) { // Verifica si no hay eventos o se alcanzó el tiempo final
            return false; // Retorna false indicando que no se puede avanzar más
        }

        Event event = scheduler.getNextEvent(); // Obtiene el siguiente evento de la cola
        clock.advanceTo(event.getScheduledTime()); // Avanza el reloj al tiempo del evento
        event.execute(); // Ejecuta el evento

        statistics.calculateLocationStatistics(locations, clock.getCurrentTime()); // Actualiza estadísticas de ubicaciones después del paso
        statistics.calculateResourceStatistics(resources, clock.getCurrentTime()); // Actualiza estadísticas de recursos después del paso

        return scheduler.hasEvents() && clock.getCurrentTime() < simulationEndTime; // Retorna true si hay más eventos y no se alcanzó el tiempo final
    }

    public void addListener(SimulationListener listener) { // Método para agregar un listener de eventos de simulación
        listeners.add(listener); // Agrega el listener a la lista de observadores
    }

    public void removeListener(SimulationListener listener) { // Método para remover un listener de eventos
        listeners.remove(listener); // Elimina el listener de la lista de observadores
    }

    public void notifyEntityArrival(Entity entity, Location location) { // Método para notificar la llegada de una entidad a una ubicación
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onEntityArrival(entity, location, clock.getCurrentTime()); // Notifica a cada listener el evento de llegada con tiempo actual
        }
    }

    public void notifyEntityMove(Entity entity, Location from, Location to) { // Método para notificar el movimiento de una entidad entre ubicaciones
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onEntityMove(entity, from, to, clock.getCurrentTime()); // Notifica a cada listener el movimiento con origen, destino y tiempo
        }
    }

    public void notifyEntityExit(Entity entity, Location from) { // Método para notificar la salida de una entidad de una ubicación
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onEntityExit(entity, from, clock.getCurrentTime()); // Notifica a cada listener la salida con ubicación y tiempo
        }
    }

    public void notifyResourceAcquired(Resource resource, Entity entity) { // Método para notificar la adquisición de un recurso por una entidad
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onResourceAcquired(resource, entity, clock.getCurrentTime()); // Notifica a cada listener la adquisición con tiempo actual
        }
    }

    public void notifyResourceReleased(Resource resource, Entity entity) { // Método para notificar la liberación de un recurso por una entidad
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onResourceReleased(resource, entity, clock.getCurrentTime()); // Notifica a cada listener la liberación con tiempo actual
        }
    }

    public void notifyEntityCreated(Entity entity, Location location) { // Método para notificar la creación de una nueva entidad
        for (SimulationListener listener : listeners) { // Itera sobre todos los listeners registrados
            listener.onEntityCreated(entity, location, clock.getCurrentTime()); // Notifica a cada listener la creación con ubicación y tiempo
        }
    }

    public SimulationClock getClock() { // Método getter para obtener el reloj de simulación
        return clock; // Retorna la referencia al objeto reloj
    }

    public EventScheduler getScheduler() { // Método getter para obtener el programador de eventos
        return scheduler; // Retorna la referencia al objeto programador
    }

    public StatisticsCollector getStatistics() { // Método getter para obtener el colector de estadísticas
        return statistics; // Retorna la referencia al colector de estadísticas
    }

    public EntityType getEntityType(String name) { // Método para obtener un tipo de entidad por nombre
        return entityTypes.get(name); // Retorna el tipo de entidad o null si no existe
    }

    public Location getLocation(String name) { // Método para obtener una ubicación por nombre
        return locations.get(name); // Retorna la ubicación o null si no existe
    }

    public Resource getResource(String name) { // Método para obtener un recurso por nombre
        return resources.get(name); // Retorna el recurso o null si no existe
    }

    public ProcessingRule getProcessingRule(String location) { // Método para obtener la regla de procesamiento de una ubicación
        return processingRules.get(location); // Retorna la regla de procesamiento o null si no existe
    }

    public Map<String, Location> getAllLocations() { // Método para obtener el mapa completo de ubicaciones
        return locations; // Retorna el mapa con todas las ubicaciones registradas
    }

    public Map<String, EntityType> getAllEntityTypes() { // Método para obtener el mapa completo de tipos de entidades
        return entityTypes; // Retorna el mapa con todos los tipos de entidades registrados
    }

    public Map<String, Resource> getAllResources() { // Método para obtener el mapa completo de recursos
        return resources; // Retorna el mapa con todos los recursos registrados
    }
}
