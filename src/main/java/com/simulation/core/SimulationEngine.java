package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) del motor de simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters para acceder a los parámetros de configuración
import com.simulation.random.RandomGenerators; // Importa la clase RandomGenerators para generar números aleatorios con diferentes distribuciones
import com.simulation.resources.*; // Importa todas las clases del paquete resources (Location, ProcessingLocation, BufferLocation, etc.)
import com.simulation.statistics.Statistics; // Importa la clase Statistics para recopilar estadísticas de la simulación
import com.simulation.core.EventTypes.*; // Importa todas las clases de tipos de eventos del paquete EventTypes

import java.util.*; // Importa todas las clases del paquete util de Java (List, Set, Map, PriorityQueue, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Motor de simulación Multi-Engrane - Fabricación de engranes con 12 locaciones // Descripción del motor de simulación
 * CORREGIDO: Las entidades permanecen en locaciones hasta que hay capacidad en destino // Nota sobre corrección de bloqueo
 * CORREGIDO: Routing probabilístico se decide UNA VEZ y se mantiene en reintentos // Nota sobre corrección de decisiones de routing
 */ // Fin del comentario Javadoc
public class SimulationEngine { // Declaración de la clase pública SimulationEngine que es el motor principal que ejecuta toda la simulación de eventos discretos

    private SimulationParameters params; // Variable privada que almacena los parámetros de configuración de la simulación
    private RandomGenerators randomGen; // Variable privada que almacena el generador de números aleatorios para todas las distribuciones
    private Statistics statistics; // Variable privada que almacena el objeto de estadísticas para recopilar métricas de la simulación
    private PriorityQueue<Event> eventQueue; // Variable privada que almacena la cola de eventos ordenada por tiempo de ocurrencia
    private double currentTime; // Variable privada que almacena el tiempo actual de la simulación en minutos
    private boolean running; // Variable privada booleana que indica si la simulación está en ejecución
    private boolean paused; // Variable privada booleana que indica si la simulación está pausada
    private volatile double simulationSpeed = 100.0; // Variable privada volátil que almacena la velocidad de simulación en minutos simulados por segundo real, inicializada en 100.0
    private long lastRealTime = 0; // Variable privada que almacena el último tiempo real en milisegundos para control de velocidad, inicializada en 0

    // 12 Locaciones del sistema Multi-Engrane (según ProModel)
    private BufferLocation conveyor1; // Variable privada que almacena la primera locación conveyor (buffer con capacidad infinita)
    private BufferLocation almacen; // Variable privada que almacena la locación de almacén (buffer con capacidad limitada)
    private ProcessingLocation cortadora; // Variable privada que almacena la locación de cortadora (procesa y divide barras en piezas)
    private ProcessingLocation torno; // Variable privada que almacena la locación de torno (procesa piezas)
    private BufferLocation conveyor2; // Variable privada que almacena la segunda locación conveyor (buffer con capacidad infinita)
    private ProcessingLocation fresadora; // Variable privada que almacena la locación de fresadora (procesa piezas)
    private BufferLocation almacen2; // Variable privada que almacena el segundo almacén (buffer con capacidad limitada)
    private ProcessingLocation pintura; // Variable privada que almacena la locación de pintura (procesa piezas)
    private ProcessingLocation inspeccion1; // Variable privada que almacena la primera locación de inspección (procesa piezas)
    private ProcessingLocation inspeccion2; // Variable privada que almacena la segunda locación de inspección (procesa piezas)
    private ProcessingLocation empaque; // Variable privada que almacena la locación de empaque (procesa piezas)
    private ProcessingLocation embarque; // Variable privada que almacena la locación de embarque (última estación antes de salir)

    private Set<Entity> entitiesInTransport; // Variable privada que almacena un conjunto de entidades que están actualmente en tránsito entre locaciones
    private List<Entity> allActiveEntities; // Variable privada que almacena una lista de todas las entidades activas en el sistema

    private TransportResource trabajador1; // Variable privada que almacena el recurso de transporte trabajador 1
    private TransportResource trabajador2; // Variable privada que almacena el recurso de transporte trabajador 2
    private TransportResource trabajador3; // Variable privada que almacena el recurso de transporte trabajador 3
    private TransportResource montacargas; // Variable privada que almacena el recurso de transporte montacargas principal
    private TransportResource montacargasSec; // Variable privada que almacena el recurso de transporte montacargas secundario

    private static final double VISUAL_TRANSIT_TIME = 0.05; // Constante estática final que define el tiempo de tránsito visual mínimo en minutos para propósitos de animación
    private static final double R1_TRAVEL_TIME = 54.28 / 150.0; // Constante estática final que define el tiempo de viaje de la ruta R1 en minutos calculado como distancia dividida entre velocidad
    private static final double R2_TRAVEL_TIME = 36.83 / 150.0; // Constante estática final que define el tiempo de viaje de la ruta R2 en minutos
    private static final double R3_TRAVEL_TIME = 45.15 / 150.0; // Constante estática final que define el tiempo de viaje de la ruta R3 en minutos
    private static final double R4_TRAVEL_TIME = 33.13 / 150.0; // Constante estática final que define el tiempo de viaje de la ruta R4 en minutos
    private static final double R5_TRAVEL_TIME = 56.86 / 150.0; // Constante estática final que define el tiempo de viaje de la ruta R5 en minutos

    public SimulationEngine(SimulationParameters params) { // Constructor público que inicializa el motor de simulación recibiendo los parámetros de configuración como parámetro
        this.params = params; // Asigna los parámetros recibidos a la variable de instancia
        this.statistics = new Statistics(); // Crea una nueva instancia de Statistics para recopilar estadísticas
        this.eventQueue = new PriorityQueue<>(); // Crea una nueva PriorityQueue vacía para almacenar eventos ordenados por tiempo
        this.entitiesInTransport = new HashSet<>(); // Crea un nuevo HashSet vacío para almacenar entidades en tránsito
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>()); // Crea una lista sincronizada (thread-safe) de entidades activas
        initializeTransportResources(); // Llama al método para inicializar los recursos de transporte
        this.currentTime = 0; // Inicializa el tiempo actual en 0
        this.running = false; // Inicializa el estado de ejecución como falso
        this.paused = false; // Inicializa el estado de pausa como falso
        initializeLocations(); // Llama al método para inicializar las 12 locaciones del sistema
        initializeRandomGenerators(); // Llama al método para inicializar los generadores de números aleatorios
    } // Cierre del constructor SimulationEngine

    private void initializeLocations() { // Método privado que inicializa las 12 locaciones del sistema de fabricación de engranes
        conveyor1 = new BufferLocation("CONVEYOR_1", params.getConveyor1Capacity()); // Crea la instancia de conveyor1 con su nombre y capacidad obtenida de los parámetros
        almacen = new BufferLocation("ALMACEN", params.getAlmacenCapacity()); // Crea la instancia del almacén con su nombre y capacidad obtenida de los parámetros
        cortadora = new ProcessingLocation("CORTADORA", params.getCortadoraCapacity()); // Crea la instancia de la cortadora con su nombre y capacidad obtenida de los parámetros
        torno = new ProcessingLocation("TORNO", params.getTornoCapacity()); // Crea la instancia del torno con su nombre y capacidad obtenida de los parámetros
        conveyor2 = new BufferLocation("CONVEYOR_2", params.getConveyor2Capacity()); // Crea la instancia de conveyor2 con su nombre y capacidad obtenida de los parámetros
        fresadora = new ProcessingLocation("FRESADORA", params.getFresadoraCapacity()); // Crea la instancia de la fresadora con su nombre y capacidad obtenida de los parámetros
        almacen2 = new BufferLocation("ALMACEN_2", params.getAlmacen2Capacity()); // Crea la instancia del almacén 2 con su nombre y capacidad obtenida de los parámetros
        pintura = new ProcessingLocation("PINTURA", params.getPinturaCapacity()); // Crea la instancia de pintura con su nombre y capacidad obtenida de los parámetros
        inspeccion1 = new ProcessingLocation("INSPECCION_1", params.getInspeccion1Capacity()); // Crea la instancia de inspección 1 con su nombre y capacidad obtenida de los parámetros
        inspeccion2 = new ProcessingLocation("INSPECCION_2", params.getInspeccion2Capacity()); // Crea la instancia de inspección 2 con su nombre y capacidad obtenida de los parámetros
        empaque = new ProcessingLocation("EMPAQUE", params.getEmpaqueCapacity()); // Crea la instancia de empaque con su nombre y capacidad obtenida de los parámetros
        embarque = new ProcessingLocation("EMBARQUE", params.getEmbarqueCapacity()); // Crea la instancia de embarque con su nombre y capacidad obtenida de los parámetros

        statistics.registerLocation(conveyor1); // Registra conveyor1 en el objeto de estadísticas para recopilar métricas
        statistics.registerLocation(almacen); // Registra el almacén en el objeto de estadísticas
        statistics.registerLocation(cortadora); // Registra la cortadora en el objeto de estadísticas
        statistics.registerLocation(torno); // Registra el torno en el objeto de estadísticas
        statistics.registerLocation(conveyor2); // Registra conveyor2 en el objeto de estadísticas
        statistics.registerLocation(fresadora); // Registra la fresadora en el objeto de estadísticas
        statistics.registerLocation(almacen2); // Registra el almacén 2 en el objeto de estadísticas
        statistics.registerLocation(pintura); // Registra pintura en el objeto de estadísticas
        statistics.registerLocation(inspeccion1); // Registra inspección 1 en el objeto de estadísticas
        statistics.registerLocation(inspeccion2); // Registra inspección 2 en el objeto de estadísticas
        statistics.registerLocation(empaque); // Registra empaque en el objeto de estadísticas
        statistics.registerLocation(embarque); // Registra embarque en el objeto de estadísticas
    } // Cierre del método initializeLocations

    private void initializeTransportResources() { // Método privado que inicializa los 5 recursos de transporte del sistema
        trabajador1 = new TransportResource("TRABAJADOR_1"); // Crea una nueva instancia del recurso de transporte trabajador 1
        trabajador2 = new TransportResource("TRABAJADOR_2"); // Crea una nueva instancia del recurso de transporte trabajador 2
        trabajador3 = new TransportResource("TRABAJADOR_3"); // Crea una nueva instancia del recurso de transporte trabajador 3
        montacargas = new TransportResource("MONTACARGAS"); // Crea una nueva instancia del recurso de transporte montacargas principal
        montacargasSec = new TransportResource("MONTACARGAS_"); // Crea una nueva instancia del recurso de transporte montacargas secundario
    } // Cierre del método initializeTransportResources

    private void initializeRandomGenerators() { // Método privado que inicializa los generadores de números aleatorios con todos los parámetros de distribución
        randomGen = new RandomGenerators(params.getBaseRandomSeed()); // Crea una nueva instancia de RandomGenerators con la semilla base de los parámetros
        randomGen.initialize( // Llama al método initialize del generador de números aleatorios pasando todos los parámetros de distribución
                params.getArrivalMeanTime(), // Parámetro 1: tiempo promedio entre arribos
                params.getConveyor1Time(), // Parámetro 2: tiempo de transporte en conveyor 1
                params.getConveyor2Time(), // Parámetro 3: tiempo de transporte en conveyor 2
                params.getTransportWorkerTime(), // Parámetro 4: tiempo de transporte de trabajadores
                params.getAlmacenProcessMean(), // Parámetro 5: media del proceso en almacén
                params.getAlmacenProcessStdDev(), // Parámetro 6: desviación estándar del proceso en almacén
                params.getCortadoraProcessMean(), // Parámetro 7: media del proceso en cortadora
                params.getTornoProcessMean(), // Parámetro 8: media del proceso en torno
                params.getTornoProcessStdDev(), // Parámetro 9: desviación estándar del proceso en torno
                params.getFresadoraProcessMean(), // Parámetro 10: media del proceso en fresadora
                params.getAlmacen2ProcessMean(), // Parámetro 11: media del proceso en almacén 2
                params.getAlmacen2ProcessStdDev(), // Parámetro 12: desviación estándar del proceso en almacén 2
                params.getPinturaProcessMean(), // Parámetro 13: media del proceso en pintura
                params.getInspeccion1ProcessMean(), // Parámetro 14: media del proceso en inspección 1
                params.getInspeccion1ProcessStdDev(), // Parámetro 15: desviación estándar del proceso en inspección 1
                params.getInspeccion2ProcessMean(), // Parámetro 16: media del proceso en inspección 2
                params.getEmpaqueProcessMean(), // Parámetro 17: media del proceso en empaque
                params.getEmpaqueProcessStdDev(), // Parámetro 18: desviación estándar del proceso en empaque
                params.getEmbarqueProcessMean(), // Parámetro 19: media del proceso en embarque
                params.getInspeccion1ToEmpaqueProb() // Parámetro 20: probabilidad de ir de inspección 1 a empaque
        ); // Cierre del paréntesis de initialize
    } // Cierre del método initializeRandomGenerators

    public void reset() { // Método público que reinicia completamente el motor de simulación a su estado inicial
        eventQueue.clear(); // Limpia la cola de eventos eliminando todos los eventos pendientes
        entitiesInTransport.clear(); // Limpia el conjunto de entidades en tránsito
        allActiveEntities.clear(); // Limpia la lista de entidades activas
        resetTransportResources(); // Llama al método para reiniciar todos los recursos de transporte
        currentTime = 0; // Reinicia el tiempo actual a 0
        running = false; // Establece el estado de ejecución como falso
        paused = false; // Establece el estado de pausa como falso
        lastRealTime = 0; // Reinicia el último tiempo real a 0
        Entity.resetIdCounter(); // Llama al método estático para reiniciar el contador de IDs de entidades a 1
        statistics.reset(); // Llama al método para reiniciar las estadísticas

        conveyor1.resetState(); // Reinicia el estado interno de conveyor1
        almacen.resetState(); // Reinicia el estado interno del almacén
        cortadora.resetState(); // Reinicia el estado interno de la cortadora
        torno.resetState(); // Reinicia el estado interno del torno
        conveyor2.resetState(); // Reinicia el estado interno de conveyor2
        fresadora.resetState(); // Reinicia el estado interno de la fresadora
        almacen2.resetState(); // Reinicia el estado interno del almacén 2
        pintura.resetState(); // Reinicia el estado interno de pintura
        inspeccion1.resetState(); // Reinicia el estado interno de inspección 1
        inspeccion2.resetState(); // Reinicia el estado interno de inspección 2
        empaque.resetState(); // Reinicia el estado interno de empaque
        embarque.resetState(); // Reinicia el estado interno de embarque

        initializeRandomGenerators(); // Reinicializa los generadores de números aleatorios con la semilla base
    } // Cierre del método reset

    private void resetTransportResources() { // Método privado que reinicia el estado de todos los recursos de transporte
        trabajador1.reset(); // Reinicia el estado del trabajador 1 (lo marca como disponible)
        trabajador2.reset(); // Reinicia el estado del trabajador 2
        trabajador3.reset(); // Reinicia el estado del trabajador 3
        montacargas.reset(); // Reinicia el estado del montacargas principal
        montacargasSec.reset(); // Reinicia el estado del montacargas secundario
    } // Cierre del método resetTransportResources

    public void initialize() { // Método público que inicializa la simulación y programa el primer evento de arribo
        reset(); // Llama al método reset para asegurar que el motor está en estado inicial
        lastRealTime = System.currentTimeMillis(); // Registra el tiempo real actual en milisegundos para control de velocidad
        double firstArrival = randomGen.nextArrivalTime(); // Genera el tiempo del primer arribo usando la distribución exponencial
        scheduleEvent(new ArrivalEvent(firstArrival)); // Programa el primer evento de arribo en la cola de eventos
    } // Cierre del método initialize

    public void setSimulationSpeed(double minutesPerSecond) { // Método público que establece la velocidad de simulación recibiendo los minutos simulados por segundo real como parámetro
        this.simulationSpeed = minutesPerSecond; // Asigna el valor recibido a la variable de velocidad de simulación
    } // Cierre del método setSimulationSpeed

    public void run() { // Método público que ejecuta el loop principal de la simulación procesando eventos secuencialmente
        running = true; // Establece el estado de ejecución como verdadero
        double endTime = params.getSimulationDurationMinutes(); // Obtiene la duración total de la simulación en minutos desde los parámetros

        while (running) { // Bucle while que se ejecuta mientras la simulación esté corriendo
            if (eventQueue.isEmpty()) { // Condición que verifica si la cola de eventos está vacía
                break; // Sale del bucle si no hay más eventos
            } // Cierre del bloque condicional if

            while (paused && running) { // Bucle while interno que se ejecuta mientras la simulación esté pausada y corriendo
                try { // Bloque try para manejar la interrupción del sleep
                    Thread.sleep(100); // Pausa el hilo por 100 milisegundos mientras está pausado
                } catch (InterruptedException e) { // Captura la excepción si el hilo es interrumpido
                    Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo
                    return; // Sale del método prematuramente
                } // Cierre del bloque catch
            } // Cierre del bucle while interno

            if (!running) break; // Si la simulación ya no está corriendo, sale del bucle

            Event nextEvent = eventQueue.peek(); // Obtiene el siguiente evento de la cola sin removerlo
            if (nextEvent == null) { // Condición que verifica si el evento es null
                break; // Sale del bucle si no hay evento
            } // Cierre del bloque condicional if

            if (nextEvent.getTime() >= endTime) { // Condición que verifica si el tiempo del próximo evento excede la duración de la simulación
                currentTime = endTime; // Establece el tiempo actual al tiempo final de la simulación
                break; // Sale del bucle porque la simulación terminó
            } // Cierre del bloque condicional if

            double targetSimTime = nextEvent.getTime(); // Obtiene el tiempo del próximo evento
            long currentRealTime = System.currentTimeMillis(); // Obtiene el tiempo real actual en milisegundos
            double elapsedRealSeconds = (currentRealTime - lastRealTime) / 1000.0; // Calcula los segundos reales transcurridos desde la última actualización
            double simulatedMinutes = elapsedRealSeconds * simulationSpeed; // Calcula los minutos simulados basados en el tiempo real transcurrido y la velocidad de simulación
            double timeUntilEvent = targetSimTime - currentTime; // Calcula el tiempo simulado hasta el próximo evento

            if (timeUntilEvent > simulatedMinutes && simulationSpeed < 10000) { // Condición que verifica si se debe esperar antes de procesar el evento (para controlar la velocidad de simulación)
                long waitTimeMs = (long) ((timeUntilEvent / simulationSpeed) * 1000); // Calcula el tiempo de espera en milisegundos para sincronizar con la velocidad deseada
                waitTimeMs = Math.min(waitTimeMs, 50); // Limita el tiempo de espera a máximo 50 milisegundos para mantener responsividad
                if (waitTimeMs > 0) { // Condición que verifica si hay tiempo de espera positivo
                    try { // Bloque try para manejar la interrupción del sleep
                        Thread.sleep(waitTimeMs); // Pausa el hilo por el tiempo calculado
                    } catch (InterruptedException e) { // Captura la excepción si el hilo es interrumpido
                        Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo
                        return; // Sale del método prematuramente
                    } // Cierre del bloque catch
                } // Cierre del bloque condicional if interno
            } // Cierre del bloque condicional if externo

            lastRealTime = System.currentTimeMillis(); // Actualiza el último tiempo real con el tiempo actual
            eventQueue.poll(); // Remueve el próximo evento de la cola
            currentTime = targetSimTime; // Actualiza el tiempo actual de la simulación al tiempo del evento

            if (currentTime >= endTime) { // Condición que verifica nuevamente si se excedió el tiempo final
                currentTime = endTime; // Establece el tiempo actual al tiempo final
                break; // Sale del bucle
            } // Cierre del bloque condicional if

            nextEvent.execute(this); // Ejecuta el evento llamando a su método execute y pasando este motor como contexto
        } // Cierre del bucle while principal

        statistics.finalizeStatistics(currentTime); // Finaliza las estadísticas con el tiempo final de la simulación
        running = false; // Establece el estado de ejecución como falso
    } // Cierre del método run

    // === MANEJO DE EVENTOS PRINCIPALES ===

    public void handleArrival(double time) { // Método público que maneja el arribo de una nueva entidad al sistema recibiendo el tiempo de arribo como parámetro
        Entity entity = new Entity(time); // Crea una nueva entidad con el tiempo de arribo como tiempo de creación
        statistics.recordArrival(); // Registra el arribo en las estadísticas
        allActiveEntities.add(entity); // Agrega la nueva entidad a la lista de entidades activas

        conveyor1.enter(entity, time); // La entidad entra al primer conveyor
        entity.setCurrentLocation("CONVEYOR_1"); // Establece la locación actual de la entidad como CONVEYOR_1

        if (time < params.getSimulationDurationMinutes()) { // Condición que verifica si aún no se ha llegado al final de la simulación
            double nextArrival = time + randomGen.nextArrivalTime(); // Calcula el tiempo del próximo arribo sumando un tiempo aleatorio
            scheduleEvent(new ArrivalEvent(nextArrival)); // Programa el próximo evento de arribo
        } // Cierre del bloque condicional if

        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "CONVEYOR_1")); // Programa el fin del proceso en CONVEYOR_1 casi inmediatamente (0.01 minutos después)
    } // Cierre del método handleArrival

    public void handleTransportEnd(Entity entity, String destinationName, double time) { // Método público que maneja la finalización del transporte de una entidad recibiendo la entidad, nombre del destino y tiempo como parámetros
        entitiesInTransport.remove(entity); // Remueve la entidad del conjunto de entidades en tránsito
        entity.endTransit(); // Finaliza el estado de tránsito de la entidad

        switch (destinationName) { // Switch que determina qué método de arribo llamar basado en el nombre del destino
            case "ALMACEN": arriveAtAlmacen(entity, time); break; // Si el destino es ALMACEN, llama al método correspondiente
            case "CORTADORA": arriveAtCortadora(entity, time); break; // Si el destino es CORTADORA, llama al método correspondiente
            case "TORNO": arriveAtTorno(entity, time); break; // Si el destino es TORNO, llama al método correspondiente
            case "CONVEYOR_2": arriveAtConveyor2(entity, time); break; // Si el destino es CONVEYOR_2, llama al método correspondiente
            case "FRESADORA": arriveAtFresadora(entity, time); break; // Si el destino es FRESADORA, llama al método correspondiente
            case "ALMACEN_2": arriveAtAlmacen2(entity, time); break; // Si el destino es ALMACEN_2, llama al método correspondiente
            case "PINTURA": arriveAtPintura(entity, time); break; // Si el destino es PINTURA, llama al método correspondiente
            case "INSPECCION_1": arriveAtInspeccion1(entity, time); break; // Si el destino es INSPECCION_1, llama al método correspondiente
            case "INSPECCION_2": arriveAtInspeccion2(entity, time); break; // Si el destino es INSPECCION_2, llama al método correspondiente
            case "EMPAQUE": arriveAtEmpaque(entity, time); break; // Si el destino es EMPAQUE, llama al método correspondiente
            case "EMBARQUE": arriveAtEmbarque(entity, time); break; // Si el destino es EMBARQUE, llama al método correspondiente
        } // Cierre del switch
    } // Cierre del método handleTransportEnd

    public void handleTransportResourceAfterArrival(TransportResource resource, double arrivalTime, double returnTime) { // Método público que maneja la liberación de un recurso de transporte después de una llegada recibiendo el recurso, tiempo de llegada y tiempo de retorno como parámetros
        if (resource == null) return; // Si el recurso es null, sale del método prematuramente

        if (returnTime <= 0) { // Condición que verifica si no hay tiempo de retorno
            handleTransportResourceAvailable(resource, arrivalTime); // Libera el recurso inmediatamente
        } else { // Bloque else que se ejecuta si hay tiempo de retorno
            scheduleEvent(new ResourceReleaseEvent(arrivalTime + returnTime, resource)); // Programa un evento de liberación del recurso después del tiempo de retorno
        } // Cierre del bloque else
    } // Cierre del método handleTransportResourceAfterArrival

    public void handleTransportResourceAvailable(TransportResource resource, double time) { // Método público que maneja la disponibilidad de un recurso de transporte recibiendo el recurso y el tiempo como parámetros
        resource.release(); // Libera el recurso (lo marca como disponible)

        // Intentar procesar colas cuando un recurso se libera
        if (resource == trabajador1) { // Condición que verifica si el recurso liberado es el trabajador 1
            processTornoQueue(time); // Intenta procesar la cola del torno
        } else if (resource == trabajador2) { // Condición que verifica si el recurso es el trabajador 2
            processFresadoraExitQueue(time); // Intenta procesar entidades bloqueadas en la salida de fresadora
        } else if (resource == montacargas) { // Condición que verifica si el recurso es el montacargas
            processAlmacen2ExitQueue(time); // Intenta procesar entidades bloqueadas en la salida de almacén 2
        } else if (resource == montacargasSec) { // Condición que verifica si el recurso es el montacargas secundario
            processPinturaExitQueue(time); // Intenta procesar entidades bloqueadas en la salida de pintura
        } else if (resource == trabajador3) { // Condición que verifica si el recurso es el trabajador 3
            processEmpaqueExitQueue(time); // Intenta procesar entidades bloqueadas en la salida de empaque
        } // Cierre de las condiciones if-else
    } // Cierre del método handleTransportResourceAvailable

    public void handleProcessEnd(Entity entity, String locationName, double time) { // Método público que maneja la finalización de un proceso en una locación recibiendo la entidad, nombre de la locación y tiempo como parámetros
        switch (locationName) { // Switch que determina qué método de finalización llamar basado en el nombre de la locación
            case "CONVEYOR_1": finishConveyor1(entity, time); break; // Si la locación es CONVEYOR_1, llama al método de finalización correspondiente
            case "ALMACEN": finishAlmacen(entity, time); break; // Si la locación es ALMACEN, llama al método de finalización correspondiente
            case "CORTADORA": finishCortadora(entity, time); break; // Si la locación es CORTADORA, llama al método de finalización correspondiente
            case "TORNO": finishTorno(entity, time); break; // Si la locación es TORNO, llama al método de finalización correspondiente
            case "CONVEYOR_2": finishConveyor2(entity, time); break; // Si la locación es CONVEYOR_2, llama al método de finalización correspondiente
            case "FRESADORA": finishFresadora(entity, time); break; // Si la locación es FRESADORA, llama al método de finalización correspondiente
            case "ALMACEN_2": finishAlmacen2(entity, time); break; // Si la locación es ALMACEN_2, llama al método de finalización correspondiente
            case "PINTURA": finishPintura(entity, time); break; // Si la locación es PINTURA, llama al método de finalización correspondiente
            case "INSPECCION_1": finishInspeccion1(entity, time); break; // Si la locación es INSPECCION_1, llama al método de finalización correspondiente
            case "INSPECCION_2": finishInspeccion2(entity, time); break; // Si la locación es INSPECCION_2, llama al método de finalización correspondiente
            case "EMPAQUE": finishEmpaque(entity, time); break; // Si la locación es EMPAQUE, llama al método de finalización correspondiente
            case "EMBARQUE": finishEmbarque(entity, time); break; // Si la locación es EMBARQUE, llama al método de finalización correspondiente
        } // Cierre del switch
    } // Cierre del método handleProcessEnd

    // === MÉTODOS FINISH (12 métodos) - CORREGIDOS ===

    private void finishConveyor1(Entity entity, double time) { // Método privado que maneja la finalización del proceso en CONVEYOR_1 recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que ALMACEN tenga capacidad
        if (almacen.canEnter()) { // Condición que verifica si el almacén tiene capacidad disponible
            almacen.reserveCapacity(); // Reserva capacidad en el almacén para esta entidad
            conveyor1.exit(entity, time); // La entidad sale de conveyor1
            double transportTime = 4.0; // Define el tiempo de transporte como 4.0 minutos
            entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
            entity.startTransit(time, transportTime, "ALMACEN"); // Inicia el tránsito de la entidad hacia ALMACEN
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN")); // Programa el evento de fin de transporte
        } else { // Bloque else que se ejecuta si el almacén no tiene capacidad
            // Reintentar más tarde - la entidad permanece en CONVEYOR_1
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CONVEYOR_1")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishConveyor1
    private void finishAlmacen(Entity entity, double time) { // Método privado que maneja la finalización del proceso en ALMACEN recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que CORTADORA tenga capacidad
        if (cortadora.canEnter()) { // Condición que verifica si la cortadora tiene capacidad disponible
            cortadora.reserveCapacity(); // Reserva capacidad en la cortadora para esta entidad
            almacen.exit(entity, time); // La entidad sale del almacén
            double transportTime = 3.0; // Define el tiempo de transporte como 3.0 minutos
            entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
            entity.startTransit(time, transportTime, "CORTADORA"); // Inicia el tránsito de la entidad hacia CORTADORA
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CORTADORA")); // Programa el evento de fin de transporte
        } else { // Bloque else que se ejecuta si la cortadora no tiene capacidad
            // Reintentar más tarde - la entidad permanece en ALMACEN (BLOQUEADA)
            entity.setBlocked(true, time); // Marca la entidad como bloqueada y registra el tiempo de inicio del bloqueo
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "ALMACEN")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishAlmacen

    private void finishCortadora(Entity entity, double time) { // Método privado que maneja la finalización del proceso en CORTADORA recibiendo la entidad y el tiempo como parámetros
        // SPLIT: 1 barra → 2 piezas
        // La barra original NO sale hasta que ambas piezas puedan moverse

        // Verificar si TORNO tiene capacidad para al menos 1 pieza
        if (torno.canEnter() && trabajador1.isAvailable()) { // Condición que verifica si el torno tiene capacidad Y el trabajador 1 está disponible
            // Hacer SPLIT
            cortadora.exit(entity, time); // La barra sale de la cortadora

            Entity pieza1 = entity; // La entidad original se convierte en la primera pieza
            Entity pieza2 = new Entity(time); // Crea una nueva entidad para representar la segunda pieza resultante del split
            allActiveEntities.add(pieza2); // Agrega la segunda pieza a la lista de entidades activas
            statistics.recordArrival(); // Registra la segunda pieza como un nuevo arribo al sistema

            // Mover pieza1
            torno.reserveCapacity(); // Reserva capacidad en el torno para la primera pieza
            trabajador1.occupy(); // Marca el trabajador 1 como ocupado
            pieza1.setBlocked(false, time); // Desbloquea la primera pieza
            startTransportWithResource(pieza1, time, R1_TRAVEL_TIME, R1_TRAVEL_TIME, "TORNO", trabajador1); // Inicia el transporte de la primera pieza al torno usando el trabajador 1

            // Agregar pieza2 a la cola de TORNO
            torno.addToQueue(pieza2); // Agrega la segunda pieza a la cola de espera del torno
            pieza2.setBlocked(true, time); // Marca la segunda pieza como bloqueada porque está esperando en cola

            // Procesar cola de CORTADORA
            processCortadoraQueue(time); // Intenta procesar la cola de la cortadora para ver si hay más entidades esperando
        } else { // Bloque else que se ejecuta si el torno no tiene capacidad o el trabajador no está disponible
            // La barra permanece en CORTADORA bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CORTADORA")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishCortadora

    private void finishTorno(Entity entity, double time) { // Método privado que maneja la finalización del proceso en TORNO recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que pueda moverse a CONVEYOR_2
        torno.exit(entity, time); // La entidad sale del torno
        double transportTime = 3.0; // Define el tiempo de transporte como 3.0 minutos
        entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, transportTime, "CONVEYOR_2"); // Inicia el tránsito de la entidad hacia CONVEYOR_2
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CONVEYOR_2")); // Programa el evento de fin de transporte

        // Procesar cola de TORNO
        processTornoQueue(time); // Intenta procesar la cola del torno para mover la siguiente entidad esperando
    } // Cierre del método finishTorno

    private void finishConveyor2(Entity entity, double time) { // Método privado que maneja la finalización del proceso en CONVEYOR_2 recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que FRESADORA tenga capacidad
        if (fresadora.canEnter()) { // Condición que verifica si la fresadora tiene capacidad disponible
            fresadora.reserveCapacity(); // Reserva capacidad en la fresadora para esta entidad
            conveyor2.exit(entity, time); // La entidad sale de conveyor2
            double transportTime = 4.0; // Define el tiempo de transporte como 4.0 minutos
            entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
            entity.startTransit(time, transportTime, "FRESADORA"); // Inicia el tránsito de la entidad hacia FRESADORA
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "FRESADORA")); // Programa el evento de fin de transporte
        } else { // Bloque else que se ejecuta si la fresadora no tiene capacidad
            // Reintentar más tarde - la entidad permanece en CONVEYOR_2
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CONVEYOR_2")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishConveyor2

    private void finishFresadora(Entity entity, double time) { // Método privado que maneja la finalización del proceso en FRESADORA recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que ALMACEN_2 tenga capacidad Y trabajador2 esté disponible
        if (almacen2.canEnter() && trabajador2.isAvailable()) { // Condición que verifica si el almacén 2 tiene capacidad Y el trabajador 2 está disponible
            almacen2.reserveCapacity(); // Reserva capacidad en el almacén 2 para esta entidad
            fresadora.exit(entity, time); // La entidad sale de la fresadora
            entity.setBlocked(false, time); // Desbloquea la entidad
            startTransportWithResource(entity, time, R2_TRAVEL_TIME, R2_TRAVEL_TIME, "ALMACEN_2", trabajador2); // Inicia el transporte hacia ALMACEN_2 usando el trabajador 2

            // Procesar cola de FRESADORA
            processFresadoraQueue(time); // Intenta procesar la cola de la fresadora
        } else { // Bloque else que se ejecuta si no hay capacidad o recurso disponible
            // La entidad permanece en FRESADORA bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "FRESADORA")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishFresadora

    private void finishAlmacen2(Entity entity, double time) { // Método privado que maneja la finalización del proceso en ALMACEN_2 recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que PINTURA tenga capacidad Y montacargas esté disponible
        if (pintura.canEnter() && montacargas.isAvailable()) { // Condición que verifica si pintura tiene capacidad Y el montacargas está disponible
            pintura.reserveCapacity(); // Reserva capacidad en pintura para esta entidad
            almacen2.exit(entity, time); // La entidad sale del almacén 2
            entity.setBlocked(false, time); // Desbloquea la entidad
            startTransportWithResource(entity, time, R3_TRAVEL_TIME, R3_TRAVEL_TIME, "PINTURA", montacargas); // Inicia el transporte hacia PINTURA usando el montacargas

            // Procesar cola de ALMACEN_2
            processAlmacen2Queue(time); // Intenta procesar la cola del almacén 2
        } else { // Bloque else que se ejecuta si no hay capacidad o recurso disponible
            // La entidad permanece en ALMACEN_2 bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "ALMACEN_2")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishAlmacen2

    private void finishPintura(Entity entity, double time) { // Método privado que maneja la finalización del proceso en PINTURA recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que INSPECCION_1 tenga capacidad Y montacargasSec esté disponible
        if (inspeccion1.canEnter() && montacargasSec.isAvailable()) { // Condición que verifica si inspección 1 tiene capacidad Y el montacargas secundario está disponible
            inspeccion1.reserveCapacity(); // Reserva capacidad en inspección 1 para esta entidad
            pintura.exit(entity, time); // La entidad sale de pintura
            entity.setBlocked(false, time); // Desbloquea la entidad
            startTransportWithResource(entity, time, R4_TRAVEL_TIME, R4_TRAVEL_TIME, "INSPECCION_1", montacargasSec); // Inicia el transporte hacia INSPECCION_1 usando el montacargas secundario

            // Procesar cola de PINTURA
            processPinturaQueue(time); // Intenta procesar la cola de pintura
        } else { // Bloque else que se ejecuta si no hay capacidad o recurso disponible
            // La entidad permanece en PINTURA bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "PINTURA")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishPintura

    private void finishInspeccion1(Entity entity, double time) { // Método privado que maneja la finalización del proceso en INSPECCION_1 con routing probabilístico recibiendo la entidad y el tiempo como parámetros
        // CRÍTICO: Decidir el routing UNA SOLA VEZ y almacenarlo en la entidad
        // Esto evita que se generen múltiples números aleatorios en reintentos
        if (entity.getRoutingDestination() == null) { // Condición que verifica si la entidad aún no tiene una decisión de routing asignada
            boolean goToEmpaque = randomGen.routeToEmpaqueFromInspeccion1(); // Genera una decisión aleatoria: true para ir a EMPAQUE (80%), false para ir a INSPECCION_2 (20%)
            String destination = goToEmpaque ? "EMPAQUE" : "INSPECCION_2"; // Establece el destino basado en la decisión aleatoria usando operador ternario
            entity.setRoutingDestination(destination); // Almacena la decisión en la entidad para mantenerla en futuros reintentos
        } // Cierre del bloque condicional if

        String destination = entity.getRoutingDestination(); // Obtiene el destino previamente decidido de la entidad

        if ("EMPAQUE".equals(destination)) { // Condición que verifica si el destino decidido es EMPAQUE
            // 80%: INSPECCION_1 → EMPAQUE
            if (empaque.canEnter()) { // Condición que verifica si empaque tiene capacidad disponible
                empaque.reserveCapacity(); // Reserva capacidad en empaque para esta entidad
                inspeccion1.exit(entity, time); // La entidad sale de inspección 1
                entity.setBlocked(false, time); // Desbloquea la entidad
                entity.setRoutingDestination(null); // Limpiar para futuros usos // Limpia la decisión de routing de la entidad para que pueda recibir nuevas decisiones en el futuro si es necesario
                double transportTime = 4.0; // Define el tiempo de transporte como 4.0 minutos
                entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
                entity.startTransit(time, transportTime, "EMPAQUE"); // Inicia el tránsito de la entidad hacia EMPAQUE
                entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
                scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE")); // Programa el evento de fin de transporte

                // Procesar cola de INSPECCION_1
                processInspeccion1Queue(time); // Intenta procesar la cola de inspección 1
            } else { // Bloque else que se ejecuta si empaque no tiene capacidad
                // La entidad permanece en INSPECCION_1 bloqueada
                // NO limpiamos routingDestination para mantener la decisión
                entity.setBlocked(true, time); // Marca la entidad como bloqueada
                entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
                scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_1")); // Programa un reintento después de 0.5 minutos manteniendo la misma decisión de routing
            } // Cierre del bloque else interno
        } else { // Bloque else que se ejecuta si el destino decidido es INSPECCION_2
            // 20%: INSPECCION_1 → INSPECCION_2
            if (inspeccion2.canEnter()) { // Condición que verifica si inspección 2 tiene capacidad disponible
                inspeccion2.reserveCapacity(); // Reserva capacidad en inspección 2 para esta entidad
                inspeccion1.exit(entity, time); // La entidad sale de inspección 1
                entity.setBlocked(false, time); // Desbloquea la entidad
                entity.setRoutingDestination(null); // Limpiar para futuros usos // Limpia la decisión de routing de la entidad
                double transportTime = 4.0; // Define el tiempo de transporte como 4.0 minutos
                entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
                entity.startTransit(time, transportTime, "INSPECCION_2"); // Inicia el tránsito de la entidad hacia INSPECCION_2
                entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
                scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION_2")); // Programa el evento de fin de transporte

                // Procesar cola de INSPECCION_1
                processInspeccion1Queue(time); // Intenta procesar la cola de inspección 1
            } else { // Bloque else que se ejecuta si inspección 2 no tiene capacidad
                // La entidad permanece en INSPECCION_1 bloqueada
                // NO limpiamos routingDestination para mantener la decisión
                entity.setBlocked(true, time); // Marca la entidad como bloqueada
                entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
                scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_1")); // Programa un reintento después de 0.5 minutos manteniendo la misma decisión de routing
            } // Cierre del bloque else interno
        } // Cierre del bloque else externo
    } // Cierre del método finishInspeccion1

    private void finishInspeccion2(Entity entity, double time) { // Método privado que maneja la finalización del proceso en INSPECCION_2 recibiendo la entidad y el tiempo como parámetros
        // INSPECCION_2 → EMPAQUE
        if (empaque.canEnter()) { // Condición que verifica si empaque tiene capacidad disponible
            empaque.reserveCapacity(); // Reserva capacidad en empaque para esta entidad
            inspeccion2.exit(entity, time); // La entidad sale de inspección 2
            entity.setBlocked(false, time); // Desbloquea la entidad
            double transportTime = 3.0; // Define el tiempo de transporte como 3.0 minutos
            entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
            entity.startTransit(time, transportTime, "EMPAQUE"); // Inicia el tránsito de la entidad hacia EMPAQUE
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE")); // Programa el evento de fin de transporte

            // Procesar cola de INSPECCION_2
            processInspeccion2Queue(time); // Intenta procesar la cola de inspección 2
        } else { // Bloque else que se ejecuta si empaque no tiene capacidad
            // La entidad permanece en INSPECCION_2 bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_2")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishInspeccion2

    private void finishEmpaque(Entity entity, double time) { // Método privado que maneja la finalización del proceso en EMPAQUE recibiendo la entidad y el tiempo como parámetros
        // Entidad NO sale hasta que EMBARQUE tenga capacidad Y trabajador3 esté disponible
        if (embarque.canEnter() && trabajador3.isAvailable()) { // Condición que verifica si embarque tiene capacidad Y el trabajador 3 está disponible
            embarque.reserveCapacity(); // Reserva capacidad en embarque para esta entidad
            empaque.exit(entity, time); // La entidad sale de empaque
            entity.setBlocked(false, time); // Desbloquea la entidad
            startTransportWithResource(entity, time, R5_TRAVEL_TIME, R5_TRAVEL_TIME, "EMBARQUE", trabajador3); // Inicia el transporte hacia EMBARQUE usando el trabajador 3

            // Procesar cola de EMPAQUE
            processEmpaqueQueue(time); // Intenta procesar la cola de empaque
        } else { // Bloque else que se ejecuta si no hay capacidad o recurso disponible
            // La entidad permanece en EMPAQUE bloqueada
            entity.setBlocked(true, time); // Marca la entidad como bloqueada
            entity.addWaitTime(0.5); // Acumula 0.5 minutos de tiempo de espera
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "EMPAQUE")); // Programa un reintento después de 0.5 minutos
        } // Cierre del bloque else
    } // Cierre del método finishEmpaque

    private void finishEmbarque(Entity entity, double time) { // Método privado que maneja la finalización del proceso en EMBARQUE (última estación) recibiendo la entidad y el tiempo como parámetros
        embarque.exit(entity, time); // La entidad sale de embarque
        double transportTime = 3.0; // Define el tiempo de transporte como 3.0 minutos
        entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        double exitTime = time + transportTime; // Calcula el tiempo de salida del sistema sumando el tiempo de transporte
        allActiveEntities.remove(entity); // Remueve la entidad de la lista de entidades activas porque ya salió del sistema
        statistics.recordExit(entity, exitTime); // Registra la salida de la entidad en las estadísticas con su tiempo total en el sistema

        // Procesar cola de EMBARQUE
        processEmbarqueQueue(time); // Intenta procesar la cola de embarque
    } // Cierre del método finishEmbarque

    // === MÉTODOS ARRIVE AT (11 métodos) ===

    private void arriveAtAlmacen(Entity entity, double time) { // Método privado que maneja la llegada de una entidad al ALMACEN recibiendo la entidad y el tiempo como parámetros
        almacen.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el almacén
        almacen.enter(entity, time); // La entidad entra al almacén
        entity.setCurrentLocation("ALMACEN"); // Establece la locación actual de la entidad como ALMACEN
        double processTime = randomGen.nextAlmacenProcess(); // Genera un tiempo de proceso aleatorio usando distribución normal
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "ALMACEN")); // Programa el evento de fin de proceso en el almacén
    } // Cierre del método arriveAtAlmacen

    private void arriveAtCortadora(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a la CORTADORA recibiendo la entidad y el tiempo como parámetros
        cortadora.commitReservedCapacity(); // Confirma la capacidad previamente reservada en la cortadora
        cortadora.enter(entity, time); // La entidad entra a la cortadora
        entity.setCurrentLocation("CORTADORA"); // Establece la locación actual de la entidad como CORTADORA
        double processTime = randomGen.nextCortadoraProcess(); // Genera un tiempo de proceso aleatorio usando distribución exponencial
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "CORTADORA")); // Programa el evento de fin de proceso en la cortadora
    } // Cierre del método arriveAtCortadora

    private void arriveAtTorno(Entity entity, double time) { // Método privado que maneja la llegada de una entidad al TORNO recibiendo la entidad y el tiempo como parámetros
        torno.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el torno
        torno.enter(entity, time); // La entidad entra al torno
        entity.setCurrentLocation("TORNO"); // Establece la locación actual de la entidad como TORNO
        double processTime = randomGen.nextTornoProcess(); // Genera un tiempo de proceso aleatorio usando distribución normal
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "TORNO")); // Programa el evento de fin de proceso en el torno
    } // Cierre del método arriveAtTorno

    private void arriveAtConveyor2(Entity entity, double time) { // Método privado que maneja la llegada de una entidad al CONVEYOR_2 recibiendo la entidad y el tiempo como parámetros
        conveyor2.enter(entity, time); // La entidad entra a conveyor2 (no requiere confirmación de capacidad porque es infinita)
        entity.setCurrentLocation("CONVEYOR_2"); // Establece la locación actual de la entidad como CONVEYOR_2
        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "CONVEYOR_2")); // Programa el fin del proceso casi inmediatamente (0.01 minutos después)
    } // Cierre del método arriveAtConveyor2

    private void arriveAtFresadora(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a la FRESADORA recibiendo la entidad y el tiempo como parámetros
        fresadora.commitReservedCapacity(); // Confirma la capacidad previamente reservada en la fresadora
        fresadora.enter(entity, time); // La entidad entra a la fresadora
        entity.setCurrentLocation("FRESADORA"); // Establece la locación actual de la entidad como FRESADORA
        double processTime = randomGen.nextFresadoraProcess(); // Genera un tiempo de proceso aleatorio usando distribución exponencial
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "FRESADORA")); // Programa el evento de fin de proceso en la fresadora
    } // Cierre del método arriveAtFresadora

    private void arriveAtAlmacen2(Entity entity, double time) { // Método privado que maneja la llegada de una entidad al ALMACEN_2 recibiendo la entidad y el tiempo como parámetros
        almacen2.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el almacén 2
        almacen2.enter(entity, time); // La entidad entra al almacén 2
        entity.setCurrentLocation("ALMACEN_2"); // Establece la locación actual de la entidad como ALMACEN_2
        double processTime = randomGen.nextAlmacen2Process(); // Genera un tiempo de proceso aleatorio usando distribución normal
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "ALMACEN_2")); // Programa el evento de fin de proceso en el almacén 2
    } // Cierre del método arriveAtAlmacen2

    private void arriveAtPintura(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a PINTURA recibiendo la entidad y el tiempo como parámetros
        pintura.commitReservedCapacity(); // Confirma la capacidad previamente reservada en pintura
        pintura.enter(entity, time); // La entidad entra a pintura
        entity.setCurrentLocation("PINTURA"); // Establece la locación actual de la entidad como PINTURA
        double processTime = randomGen.nextPinturaProcess(); // Genera un tiempo de proceso aleatorio usando distribución exponencial
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "PINTURA")); // Programa el evento de fin de proceso en pintura
    } // Cierre del método arriveAtPintura

    private void arriveAtInspeccion1(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a INSPECCION_1 recibiendo la entidad y el tiempo como parámetros
        inspeccion1.commitReservedCapacity(); // Confirma la capacidad previamente reservada en inspección 1
        inspeccion1.enter(entity, time); // La entidad entra a inspección 1
        entity.setCurrentLocation("INSPECCION_1"); // Establece la locación actual de la entidad como INSPECCION_1
        double processTime = randomGen.nextInspeccion1Process(); // Genera un tiempo de proceso aleatorio usando distribución normal
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "INSPECCION_1")); // Programa el evento de fin de proceso en inspección 1
    } // Cierre del método arriveAtInspeccion1

    private void arriveAtInspeccion2(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a INSPECCION_2 recibiendo la entidad y el tiempo como parámetros
        inspeccion2.commitReservedCapacity(); // Confirma la capacidad previamente reservada en inspección 2
        inspeccion2.enter(entity, time); // La entidad entra a inspección 2
        entity.setCurrentLocation("INSPECCION_2"); // Establece la locación actual de la entidad como INSPECCION_2
        double processTime = randomGen.nextInspeccion2Process(); // Genera un tiempo de proceso aleatorio usando distribución exponencial
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "INSPECCION_2")); // Programa el evento de fin de proceso en inspección 2
    } // Cierre del método arriveAtInspeccion2

    private void arriveAtEmpaque(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a EMPAQUE recibiendo la entidad y el tiempo como parámetros
        empaque.commitReservedCapacity(); // Confirma la capacidad previamente reservada en empaque
        empaque.enter(entity, time); // La entidad entra a empaque
        entity.setCurrentLocation("EMPAQUE"); // Establece la locación actual de la entidad como EMPAQUE
        double processTime = randomGen.nextEmpaqueProcess(); // Genera un tiempo de proceso aleatorio usando distribución normal
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "EMPAQUE")); // Programa el evento de fin de proceso en empaque
    } // Cierre del método arriveAtEmpaque

    private void arriveAtEmbarque(Entity entity, double time) { // Método privado que maneja la llegada de una entidad a EMBARQUE recibiendo la entidad y el tiempo como parámetros
        embarque.commitReservedCapacity(); // Confirma la capacidad previamente reservada en embarque
        embarque.enter(entity, time); // La entidad entra a embarque
        entity.setCurrentLocation("EMBARQUE"); // Establece la locación actual de la entidad como EMBARQUE
        double processTime = randomGen.nextEmbarqueProcess(); // Genera un tiempo de proceso aleatorio usando distribución exponencial
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "EMBARQUE")); // Programa el evento de fin de proceso en embarque
    } // Cierre del método arriveAtEmbarque

    // === MÉTODOS PROCESS QUEUE (corregidos) ===

    private void processCortadoraQueue(double time) { // Método privado que intenta procesar entidades en cola de la CORTADORA recibiendo el tiempo actual como parámetro
        while (cortadora.canEnter() && cortadora.hasQueuedEntities()) { // Bucle while que se ejecuta mientras haya capacidad en cortadora Y haya entidades en cola
            Entity nextEntity = cortadora.pollFromQueue(); // Remueve y obtiene la primera entidad de la cola de cortadora
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida
                cortadora.reserveCapacity(); // Reserva capacidad en cortadora para esta entidad
                double transportTime = 3.0; // Define el tiempo de transporte como 3.0 minutos
                nextEntity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
                nextEntity.setBlocked(false, time); // Desbloquea la entidad
                nextEntity.startTransit(time, transportTime, "CORTADORA"); // Inicia el tránsito de la entidad hacia CORTADORA
                entitiesInTransport.add(nextEntity); // Agrega la entidad al conjunto de entidades en tránsito
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "CORTADORA")); // Programa el evento de fin de transporte
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processCortadoraQueue

    private void processTornoQueue(double time) { // Método privado que intenta procesar entidades en cola del TORNO recibiendo el tiempo actual como parámetro
        while (torno.canEnter() && torno.hasQueuedEntities() && trabajador1.isAvailable()) { // Bucle while que se ejecuta mientras haya capacidad en torno Y haya entidades en cola Y trabajador 1 esté disponible
            Entity nextEntity = torno.pollFromQueue(); // Remueve y obtiene la primera entidad de la cola del torno
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida
                torno.reserveCapacity(); // Reserva capacidad en torno para esta entidad
                trabajador1.occupy(); // Marca el trabajador 1 como ocupado
                nextEntity.setBlocked(false, time); // Desbloquea la entidad
                startTransportWithResource(nextEntity, time, R1_TRAVEL_TIME, R1_TRAVEL_TIME, "TORNO", trabajador1); // Inicia el transporte hacia TORNO usando el trabajador 1
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processTornoQueue

    private void processFresadoraQueue(double time) { // Método privado que intenta procesar entidades en cola de la FRESADORA recibiendo el tiempo actual como parámetro
        while (fresadora.canEnter() && fresadora.hasQueuedEntities()) { // Bucle while que se ejecuta mientras haya capacidad en fresadora Y haya entidades en cola
            Entity nextEntity = fresadora.pollFromQueue(); // Remueve y obtiene la primera entidad de la cola de fresadora
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida
                fresadora.reserveCapacity(); // Reserva capacidad en fresadora para esta entidad
                double transportTime = 4.0; // Define el tiempo de transporte como 4.0 minutos
                nextEntity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
                nextEntity.setBlocked(false, time); // Desbloquea la entidad
                nextEntity.startTransit(time, transportTime, "FRESADORA"); // Inicia el tránsito de la entidad hacia FRESADORA
                entitiesInTransport.add(nextEntity); // Agrega la entidad al conjunto de entidades en tránsito
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "FRESADORA")); // Programa el evento de fin de transporte
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processFresadoraQueue

    private void processFresadoraExitQueue(double time) { // Método privado placeholder para procesar entidades bloqueadas en la salida de FRESADORA recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en FRESADORA
    } // Cierre del método processFresadoraExitQueue

    private void processAlmacen2Queue(double time) { // Método privado placeholder para procesar entidades en cola de ALMACEN_2 recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de ALMACEN_2
    } // Cierre del método processAlmacen2Queue

    private void processAlmacen2ExitQueue(double time) { // Método privado placeholder para procesar entidades bloqueadas en la salida de ALMACEN_2 recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en ALMACEN_2
    } // Cierre del método processAlmacen2ExitQueue

    private void processPinturaQueue(double time) { // Método privado placeholder para procesar entidades en cola de PINTURA recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de PINTURA
    } // Cierre del método processPinturaQueue

    private void processPinturaExitQueue(double time) { // Método privado placeholder para procesar entidades bloqueadas en la salida de PINTURA recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en PINTURA
    } // Cierre del método processPinturaExitQueue

    private void processInspeccion1Queue(double time) { // Método privado placeholder para procesar entidades en cola de INSPECCION_1 recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de INSPECCION_1
    } // Cierre del método processInspeccion1Queue

    private void processInspeccion2Queue(double time) { // Método privado placeholder para procesar entidades en cola de INSPECCION_2 recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de INSPECCION_2
    } // Cierre del método processInspeccion2Queue

    private void processEmpaqueQueue(double time) { // Método privado placeholder para procesar entidades en cola de EMPAQUE recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de EMPAQUE
    } // Cierre del método processEmpaqueQueue

    private void processEmpaqueExitQueue(double time) { // Método privado placeholder para procesar entidades bloqueadas en la salida de EMPAQUE recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en EMPAQUE
    } // Cierre del método processEmpaqueExitQueue

    private void processEmbarqueQueue(double time) { // Método privado placeholder para procesar entidades en cola de EMBARQUE recibiendo el tiempo actual como parámetro
        // Este método se puede implementar si es necesario procesar entidades en cola de EMBARQUE
    } // Cierre del método processEmbarqueQueue

    // === MÉTODOS UTILITARIOS ===

    private void scheduleEvent(Event event) { // Método privado utilitario que programa un evento en la cola de eventos recibiendo el evento como parámetro
        eventQueue.add(event); // Agrega el evento a la cola de prioridad que automáticamente lo ordena por tiempo
    } // Cierre del método scheduleEvent

    private void startTransport(Entity entity, double time, double travelTime, String destination) { // Método privado utilitario que inicia el transporte de una entidad sin recurso recibiendo la entidad, tiempo, tiempo de viaje y destino como parámetros
        entity.addTransportTime(travelTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, travelTime, destination); // Inicia el estado de tránsito de la entidad
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
        scheduleEvent(new TransportEndEvent(time + travelTime, entity, destination)); // Programa el evento de fin de transporte
    } // Cierre del método startTransport

    private void startTransportWithResource(Entity entity, double time, double travelTime, double returnTime, String destination, TransportResource resource) { // Método privado utilitario que inicia el transporte de una entidad con un recurso recibiendo la entidad, tiempo, tiempo de viaje, tiempo de retorno, destino y recurso como parámetros
        resource.occupy(); // Marca el recurso de transporte como ocupado
        entity.addTransportTime(travelTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, travelTime, destination); // Inicia el estado de tránsito de la entidad
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito
        scheduleEvent(new TransportEndEvent(time + travelTime, entity, destination, resource, returnTime)); // Programa el evento de fin de transporte que incluye información del recurso y tiempo de retorno
    } // Cierre del método startTransportWithResource

    // === GETTERS ===

    public double getCurrentTime() { // Método público getter que retorna el tiempo actual de la simulación de tipo double
        return currentTime; // Retorna el valor de la variable currentTime
    } // Cierre del método getCurrentTime

    public boolean isRunning() { // Método público getter que retorna si la simulación está en ejecución de tipo boolean
        return running; // Retorna el valor de la variable running
    } // Cierre del método isRunning

    public void stop() { // Método público que detiene la simulación sin recibir parámetros
        running = false; // Establece el estado de ejecución como falso para detener el loop principal
    } // Cierre del método stop

    public void pause() { // Método público que pausa la simulación sin recibir parámetros
        paused = true; // Establece el estado de pausa como verdadero
    } // Cierre del método pause

    public void resume() { // Método público que reanuda la simulación pausada sin recibir parámetros
        paused = false; // Establece el estado de pausa como falso para continuar el loop principal
    } // Cierre del método resume

    public boolean isPaused() { // Método público getter que retorna si la simulación está pausada de tipo boolean
        return paused; // Retorna el valor de la variable paused
    } // Cierre del método isPaused

    public Statistics getStatistics() { // Método público getter que retorna el objeto de estadísticas de tipo Statistics
        return statistics; // Retorna la referencia al objeto statistics
    } // Cierre del método getStatistics

    public Location getLocation(String name) { // Método público que obtiene una locación por su nombre recibiendo el nombre como parámetro y retornando un objeto Location
        switch (name) { // Switch que busca la locación correspondiente al nombre recibido
            case "CONVEYOR_1": return conveyor1; // Si el nombre es CONVEYOR_1, retorna la referencia a conveyor1
            case "ALMACEN": return almacen; // Si el nombre es ALMACEN, retorna la referencia a almacen
            case "CORTADORA": return cortadora; // Si el nombre es CORTADORA, retorna la referencia a cortadora
            case "TORNO": return torno; // Si el nombre es TORNO, retorna la referencia a torno
            case "CONVEYOR_2": return conveyor2; // Si el nombre es CONVEYOR_2, retorna la referencia a conveyor2
            case "FRESADORA": return fresadora; // Si el nombre es FRESADORA, retorna la referencia a fresadora
            case "ALMACEN_2": return almacen2; // Si el nombre es ALMACEN_2, retorna la referencia a almacen2
            case "PINTURA": return pintura; // Si el nombre es PINTURA, retorna la referencia a pintura
            case "INSPECCION_1": return inspeccion1; // Si el nombre es INSPECCION_1, retorna la referencia a inspeccion1
            case "INSPECCION_2": return inspeccion2; // Si el nombre es INSPECCION_2, retorna la referencia a inspeccion2
            case "EMPAQUE": return empaque; // Si el nombre es EMPAQUE, retorna la referencia a empaque
            case "EMBARQUE": return embarque; // Si el nombre es EMBARQUE, retorna la referencia a embarque
            default: return null; // Si el nombre no coincide con ninguna locación, retorna null
        } // Cierre del switch
    } // Cierre del método getLocation

    public Set<Entity> getEntitiesInTransport() { // Método público que retorna un conjunto de entidades en tránsito de tipo Set<Entity>
        return new HashSet<>(entitiesInTransport); // Retorna una copia del conjunto de entidades en tránsito para evitar modificaciones externas
    } // Cierre del método getEntitiesInTransport

    public List<Entity> getAllActiveEntities() { // Método público thread-safe que retorna una lista de todas las entidades activas de tipo List<Entity>
        synchronized (allActiveEntities) { // Bloque sincronizado para acceso thread-safe a la lista de entidades activas
            List<Entity> safeCopy = new ArrayList<>(); // Crea una nueva lista vacía para almacenar la copia segura
            for (Entity entity : allActiveEntities) { // Bucle for-each que itera sobre cada entidad en la lista de entidades activas
                if (entity != null) { // Condición que verifica si la entidad no es null
                    safeCopy.add(entity); // Agrega la entidad a la copia segura
                } // Cierre del bloque condicional if
            } // Cierre del bucle for-each
            return safeCopy; // Retorna la copia segura de la lista de entidades activas
        } // Cierre del bloque sincronizado
    } // Cierre del método getAllActiveEntities
} // Cierre de la clase SimulationEngine

