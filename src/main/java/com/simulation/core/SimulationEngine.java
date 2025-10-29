package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) de la simulación

import com.simulation.config.SimulationParameters; // Importa la clase SimulationParameters para acceder a los parámetros de configuración de la simulación
import com.simulation.random.RandomGenerators; // Importa la clase RandomGenerators para generar números aleatorios con diferentes distribuciones
import com.simulation.resources.*; // Importa todas las clases del paquete resources que representan recursos del sistema (locaciones, buffers, etc.)
import com.simulation.statistics.Statistics; // Importa la clase Statistics para recolectar y calcular estadísticas de la simulación
import com.simulation.core.EventTypes.*; // Importa todos los tipos de eventos definidos en la clase EventTypes

import java.util.*; // Importa todas las clases del paquete util de Java para usar colecciones, listas, colas, etc.

/** // Inicio del comentario Javadoc de la clase
 * Motor de simulación de eventos discretos // Descripción de la clase como motor de simulación
 * CORREGIDO para representar fielmente el modelo ProModel // Nota indicando que está corregido para coincidir con ProModel
 */ // Fin del comentario Javadoc
public class SimulationEngine { // Declaración de la clase pública SimulationEngine que es el motor principal que ejecuta la simulación de eventos discretos
    private SimulationParameters params; // Variable privada que almacena los parámetros de configuración de la simulación
    private RandomGenerators randomGen; // Variable privada que almacena el generador de números aleatorios con diferentes distribuciones
    private Statistics statistics; // Variable privada que almacena el objeto que recopila todas las estadísticas de la simulación
    private PriorityQueue<Event> eventQueue; // Variable privada que almacena la cola de prioridad de eventos ordenados por tiempo de ocurrencia

    private double currentTime; // Variable privada que almacena el tiempo actual de la simulación en minutos
    private boolean running; // Variable privada booleana que indica si la simulación está actualmente en ejecución
    private boolean paused; // Variable privada booleana que indica si la simulación está pausada

    private volatile double simulationSpeed = 100.0; // Variable privada volátil que almacena la velocidad de simulación (minutos simulados por segundo real), inicializada en 100.0
    private long lastRealTime = 0; // Variable privada que almacena el último tiempo real en milisegundos para control de velocidad, inicializada en 0

    // Locaciones del sistema
    private Location recepcion; // Variable privada que almacena la locación de recepción donde llegan las piezas nuevas
    private ProcessingLocation lavadora; // Variable privada que almacena la locación de procesamiento de lavadora
    private BufferLocation almacenPintura; // Variable privada que almacena la locación buffer de almacén de pintura
    private ProcessingLocation pintura; // Variable privada que almacena la locación de procesamiento de pintura
    private BufferLocation almacenHorno; // Variable privada que almacena la locación buffer de almacén del horno
    private ProcessingLocation horno; // Variable privada que almacena la locación de procesamiento del horno
    private InspectionStation inspeccion; // Variable privada que almacena la estación de inspección con múltiples operaciones

    private Set<Entity> entitiesInTransport; // Variable privada que almacena un conjunto de entidades que están actualmente en tránsito entre locaciones
    private List<Entity> allActiveEntities; // Variable privada que almacena una lista sincronizada de todas las entidades activas en el sistema
    // Colas de piezas bloqueadas esperando espacio en la siguiente etapa
    private Deque<Entity> blockedAfterLavadora; // Variable privada que almacena una cola doblemente enlazada de entidades bloqueadas después de terminar en la lavadora esperando espacio en almacén de pintura
    private Deque<Entity> blockedAfterPintura; // Variable privada que almacena una cola doblemente enlazada de entidades bloqueadas después de terminar en pintura esperando espacio en almacén del horno
    private Deque<Entity> blockedAfterHorno; // Variable privada que almacena una cola doblemente enlazada de entidades bloqueadas después de terminar en el horno esperando espacio en inspección

    // Tiempo mínimo para animación visual (NO afecta estadísticas)
    private static final double VISUAL_TRANSIT_TIME = 0.05; // Constante estática final que define el tiempo mínimo de tránsito para animación visual (0.05 minutos) sin afectar estadísticas

    public SimulationEngine(SimulationParameters params) { // Constructor público que inicializa el motor de simulación recibiendo los parámetros de configuración como parámetro
        this.params = params; // Asigna los parámetros recibidos a la variable de instancia params
        this.statistics = new Statistics(); // Crea una nueva instancia de Statistics para recopilar estadísticas de la simulación
        this.eventQueue = new PriorityQueue<>(); // Crea una nueva cola de prioridad vacía para almacenar eventos ordenados por tiempo
        this.entitiesInTransport = new HashSet<>(); // Crea un nuevo conjunto HashSet vacío para almacenar entidades en tránsito
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>()); // Crea una nueva lista sincronizada vacía para almacenar todas las entidades activas de forma thread-safe
        this.blockedAfterLavadora = new ArrayDeque<>(); // Crea una nueva cola doblemente enlazada vacía para entidades bloqueadas después de lavadora
        this.blockedAfterPintura = new ArrayDeque<>(); // Crea una nueva cola doblemente enlazada vacía para entidades bloqueadas después de pintura
        this.blockedAfterHorno = new ArrayDeque<>(); // Crea una nueva cola doblemente enlazada vacía para entidades bloqueadas después del horno
        this.currentTime = 0; // Inicializa el tiempo actual de la simulación en 0
        this.running = false; // Inicializa el estado de ejecución como falso
        this.paused = false; // Inicializa el estado de pausa como falso

        initializeLocations(); // Llama al método para inicializar todas las locaciones del sistema
        initializeRandomGenerators(); // Llama al método para inicializar los generadores de números aleatorios
    } // Cierre del constructor SimulationEngine

    /** // Inicio del comentario Javadoc del método
     * Inicializa todas las locaciones del sistema // Descripción del método
     */ // Fin del comentario Javadoc
    private void initializeLocations() { // Método privado que inicializa todas las locaciones del sistema sin recibir parámetros
        recepcion = new Location("RECEPCION", Integer.MAX_VALUE) {}; // Crea una instancia anónima de Location para recepción con capacidad máxima ilimitada (Integer.MAX_VALUE)
        lavadora = new ProcessingLocation("LAVADORA", params.getLavadoraCapacity()); // Crea una instancia de ProcessingLocation para lavadora con la capacidad especificada en los parámetros
        almacenPintura = new BufferLocation("ALMACEN_PINTURA", params.getAlmacenPinturaCapacity()); // Crea una instancia de BufferLocation para almacén de pintura con la capacidad especificada en los parámetros
        pintura = new ProcessingLocation("PINTURA", params.getPinturaCapacity()); // Crea una instancia de ProcessingLocation para pintura con la capacidad especificada en los parámetros
        almacenHorno = new BufferLocation("ALMACEN_HORNO", params.getAlmacenHornoCapacity()); // Crea una instancia de BufferLocation para almacén del horno con la capacidad especificada en los parámetros
        horno = new ProcessingLocation("HORNO", params.getHornoCapacity()); // Crea una instancia de ProcessingLocation para horno con la capacidad especificada en los parámetros
        inspeccion = new InspectionStation("INSPECCION", // Crea una instancia de InspectionStation para inspección con el nombre "INSPECCION"
                params.getInspeccionNumStations(), // Segundo parámetro: número de estaciones de inspección obtenido de los parámetros
                params.getInspeccionOperationsPerPiece()); // Tercer parámetro: número de operaciones por pieza obtenido de los parámetros

        // Registrar locaciones para estadísticas
        statistics.registerLocation(recepcion); // Registra la locación de recepción en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(lavadora); // Registra la locación de lavadora en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(almacenPintura); // Registra la locación de almacén de pintura en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(pintura); // Registra la locación de pintura en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(almacenHorno); // Registra la locación de almacén del horno en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(horno); // Registra la locación del horno en el objeto de estadísticas para rastrear métricas
        statistics.registerLocation(inspeccion); // Registra la locación de inspección en el objeto de estadísticas para rastrear métricas
    } // Cierre del método initializeLocations

    /** // Inicio del comentario Javadoc del método
     * Inicializa generadores de números aleatorios // Descripción del método
     */ // Fin del comentario Javadoc
    private void initializeRandomGenerators() { // Método privado que inicializa los generadores de números aleatorios sin recibir parámetros
        randomGen = new RandomGenerators(params.getBaseRandomSeed()); // Crea una nueva instancia de RandomGenerators con la semilla aleatoria base obtenida de los parámetros
        randomGen.initialize( // Llama al método initialize del generador para configurar todos los generadores con los parámetros siguientes
                params.getArrivalMeanTime(), // Parámetro 1: tiempo medio entre arribos
                params.getTransportRecepcionLavadoraMean(), // Parámetro 2: tiempo medio de transporte de recepción a lavadora
                params.getTransportLavadoraAlmacenMean(), // Parámetro 3: tiempo medio de transporte de lavadora a almacén
                params.getLavadoraProcessMean(), // Parámetro 4: tiempo medio de proceso en lavadora
                params.getLavadoraProcessStdDev(), // Parámetro 5: desviación estándar del tiempo de proceso en lavadora
                params.getPinturaProcessMin(), // Parámetro 6: tiempo mínimo de proceso en pintura
                params.getPinturaProcessMode(), // Parámetro 7: moda del tiempo de proceso en pintura
                params.getPinturaProcessMax(), // Parámetro 8: tiempo máximo de proceso en pintura
                params.getTransportPinturaAlmacenMin(), // Parámetro 9: tiempo mínimo de transporte de pintura a almacén
                params.getTransportPinturaAlmacenMax(), // Parámetro 10: tiempo máximo de transporte de pintura a almacén
                params.getHornoProcessMin(), // Parámetro 11: tiempo mínimo de proceso en horno
                params.getHornoProcessMax(), // Parámetro 12: tiempo máximo de proceso en horno
                params.getTransportHornoInspeccionMin(), // Parámetro 13: tiempo mínimo de transporte de horno a inspección
                params.getTransportHornoInspeccionMax(), // Parámetro 14: tiempo máximo de transporte de horno a inspección
                params.getInspeccionOperationMean() // Parámetro 15: tiempo medio de cada operación de inspección
        ); // Cierre del paréntesis del método initialize
    } // Cierre del método initializeRandomGenerators

    /** // Inicio del comentario Javadoc del método
     * Reinicia completamente la simulación // Descripción del método
     */ // Fin del comentario Javadoc
    public void reset() { // Método público que reinicia completamente la simulación a su estado inicial sin recibir parámetros
        eventQueue.clear(); // Limpia la cola de eventos eliminando todos los eventos programados
        entitiesInTransport.clear(); // Limpia el conjunto de entidades en tránsito eliminando todas las entidades
        allActiveEntities.clear(); // Limpia la lista de entidades activas eliminando todas las entidades
        blockedAfterLavadora.clear(); // Limpia la cola de entidades bloqueadas después de lavadora
        blockedAfterPintura.clear(); // Limpia la cola de entidades bloqueadas después de pintura
        blockedAfterHorno.clear(); // Limpia la cola de entidades bloqueadas después del horno
        currentTime = 0; // Reinicia el tiempo actual de la simulación a 0
        running = false; // Establece el estado de ejecución como falso
        paused = false; // Establece el estado de pausa como falso
        lastRealTime = 0; // Reinicia el último tiempo real a 0
        Entity.resetIdCounter(); // Llama al método estático de Entity para reiniciar el contador de IDs a 1
        statistics.reset(); // Reinicia el objeto de estadísticas limpiando todos los datos recopilados
        recepcion.resetState(); // Reinicia el estado de la locación de recepción
        lavadora.resetState(); // Reinicia el estado de la locación de lavadora
        almacenPintura.resetState(); // Reinicia el estado de la locación de almacén de pintura
        pintura.resetState(); // Reinicia el estado de la locación de pintura
        almacenHorno.resetState(); // Reinicia el estado de la locación de almacén del horno
        horno.resetState(); // Reinicia el estado de la locación del horno
        inspeccion.resetState(); // Reinicia el estado de la estación de inspección
        initializeRandomGenerators(); // Reinicializa los generadores de números aleatorios con la semilla base
    } // Cierre del método reset

    /** // Inicio del comentario Javadoc del método
     * Inicializa la simulación programando el primer arribo // Descripción del método
     */ // Fin del comentario Javadoc
    public void initialize() { // Método público que inicializa la simulación preparándola para comenzar sin recibir parámetros
        reset(); // Llama al método reset para asegurar que todo está en estado inicial
        lastRealTime = System.currentTimeMillis(); // Captura el tiempo real actual en milisegundos para control de velocidad
        double firstArrival = randomGen.nextArrivalTime(); // Genera el tiempo del primer arribo usando distribución exponencial
        scheduleEvent(new ArrivalEvent(firstArrival)); // Programa el primer evento de arribo en la cola de eventos
    } // Cierre del método initialize

    /** // Inicio del comentario Javadoc del método
     * Establece la velocidad de simulación // Descripción del método
     * @param minutesPerSecond Minutos simulados por segundo real // Documentación del parámetro
     */ // Fin del comentario Javadoc
    public void setSimulationSpeed(double minutesPerSecond) { // Método público que establece la velocidad de simulación recibiendo los minutos simulados por segundo real como parámetro
        this.simulationSpeed = minutesPerSecond; // Asigna el valor recibido a la variable de instancia simulationSpeed
    } // Cierre del método setSimulationSpeed

    /** // Inicio del comentario Javadoc del método
     * Ejecuta la simulación hasta completar o hasta que se detenga // Descripción del método
     */ // Fin del comentario Javadoc
    public void run() { // Método público que ejecuta el loop principal de la simulación sin recibir parámetros
        running = true; // Establece el estado de ejecución como verdadero para comenzar la simulación
        double endTime = params.getSimulationDurationMinutes(); // Obtiene el tiempo final de la simulación desde los parámetros

        while (running && !eventQueue.isEmpty()) { // Bucle while que continúa mientras running sea verdadero y haya eventos en la cola
            // Manejo de pausa
            while (paused && running) { // Bucle interno while que mantiene la ejecución pausada mientras paused sea verdadero y running también
                try { // Bloque try para capturar interrupciones durante la pausa
                    Thread.sleep(100); // Pausa el hilo por 100 milisegundos mientras está pausado
                } catch (InterruptedException e) { // Captura la excepción si el hilo es interrumpido durante el sleep
                    Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo
                    return; // Sale del método prematuramente si el hilo fue interrumpido
                } // Cierre del bloque catch
            } // Cierre del bucle while de pausa

            if (!running) break; // Si running cambió a falso durante la pausa, sale del bucle principal

            // Obtener siguiente evento
            Event nextEvent = eventQueue.peek(); // Obtiene el siguiente evento de la cola sin removerlo para inspeccionar su tiempo
            if (nextEvent == null || nextEvent.getTime() >= endTime) { // Condición que verifica si no hay más eventos o si el próximo evento ocurre después del tiempo final
                break; // Sale del bucle si no hay más eventos válidos para procesar
            } // Cierre del bloque condicional if

            double targetSimTime = nextEvent.getTime(); // Obtiene el tiempo en que ocurrirá el próximo evento

            // Control de velocidad de simulación (para visualización)
            long currentRealTime = System.currentTimeMillis(); // Captura el tiempo real actual en milisegundos
            double elapsedRealSeconds = (currentRealTime - lastRealTime) / 1000.0; // Calcula los segundos reales transcurridos desde la última actualización dividiendo milisegundos entre 1000
            double simulatedMinutes = elapsedRealSeconds * simulationSpeed; // Calcula los minutos simulados que deberían haber transcurrido multiplicando segundos reales por la velocidad

            double timeUntilEvent = targetSimTime - currentTime; // Calcula el tiempo de simulación que falta hasta el próximo evento
            if (timeUntilEvent > simulatedMinutes && simulationSpeed < 10000) { // Condición que verifica si la simulación va más rápido que la velocidad configurada y la velocidad no es máxima
                long waitTimeMs = (long) ((timeUntilEvent / simulationSpeed) * 1000); // Calcula el tiempo en milisegundos que debe esperar para sincronizar con la velocidad deseada
                waitTimeMs = Math.min(waitTimeMs, 50); // Limita el tiempo de espera a un máximo de 50 milisegundos para mantener respuesta de la UI

                if (waitTimeMs > 0) { // Condición que verifica si hay tiempo de espera positivo
                    try { // Bloque try para capturar interrupciones durante la espera
                        Thread.sleep(waitTimeMs); // Pausa el hilo por el tiempo calculado para sincronizar la velocidad
                    } catch (InterruptedException e) { // Captura la excepción si el hilo es interrumpido durante el sleep
                        Thread.currentThread().interrupt(); // Restablece el estado de interrupción del hilo
                        return; // Sale del método prematuramente si el hilo fue interrumpido
                    } // Cierre del bloque catch
                } // Cierre del bloque condicional if
            } // Cierre del bloque condicional if de control de velocidad

            // Procesar evento
            lastRealTime = System.currentTimeMillis(); // Actualiza el último tiempo real para el próximo ciclo de control de velocidad
            eventQueue.poll(); // Remueve el evento de la cola después de inspeccionar su tiempo
            currentTime = targetSimTime; // Avanza el reloj de simulación al tiempo del evento actual

            if (currentTime >= endTime) { // Condición que verifica si se alcanzó o superó el tiempo final de simulación
                break; // Sale del bucle si se alcanzó el tiempo final
            } // Cierre del bloque condicional if

            nextEvent.execute(this); // Ejecuta el evento pasando esta instancia del motor como parámetro para que el evento pueda llamar métodos del motor
        } // Cierre del bucle while principal

        // Finalizar estadísticas
        statistics.finalizeStatistics(currentTime); // Finaliza la recopilación de estadísticas pasando el tiempo actual final
        running = false; // Establece el estado de ejecución como falso indicando que la simulación terminó
    } // Cierre del método run

    /** // Inicio del comentario Javadoc del método
     * Maneja el arribo de una nueva pieza al sistema // Descripción del método
     */ // Fin del comentario Javadoc
    public void handleArrival(double time) { // Método público que maneja el evento de arribo de una nueva pieza recibiendo el tiempo del arribo como parámetro
        // Crear nueva entidad
        Entity entity = new Entity(time); // Crea una nueva instancia de Entity con el tiempo de creación
        statistics.recordArrival(); // Registra el arribo en las estadísticas incrementando el contador de arribos
        allActiveEntities.add(entity); // Agrega la nueva entidad a la lista de entidades activas en el sistema

        // Entrar a RECEPCION
        recepcion.enter(entity, time); // Hace que la entidad entre a la locación de recepción en el tiempo especificado
        entity.setCurrentLocation("RECEPCION"); // Establece la ubicación actual de la entidad como "RECEPCION"

        // Programar siguiente arribo si aún no termina la simulación
        if (time < params.getSimulationDurationMinutes()) { // Condición que verifica si el tiempo actual es menor que la duración total de la simulación
            double nextArrival = time + randomGen.nextArrivalTime(); // Calcula el tiempo del próximo arribo sumando el tiempo actual y un tiempo inter-arribo aleatorio
            scheduleEvent(new ArrivalEvent(nextArrival)); // Programa el próximo evento de arribo en la cola de eventos
        } // Cierre del bloque condicional if

        // Intentar mover inmediatamente a LAVADORA
        tryMoveToLavadora(entity, time); // Intenta mover la entidad inmediatamente de recepción a lavadora
    } // Cierre del método handleArrival

    /** // Inicio del comentario Javadoc del método
     * Intenta mover una entidad de RECEPCION a LAVADORA // Descripción del método
     */ // Fin del comentario Javadoc
    private void tryMoveToLavadora(Entity entity, double time) { // Método privado que intenta mover una entidad a lavadora recibiendo la entidad y el tiempo como parámetros
        if (lavadora.canEnter()) { // Condición que verifica si hay capacidad disponible en la lavadora
            lavadora.reserveCapacity(); // Reserva una unidad de capacidad en la lavadora para esta entidad
            // Salir de recepción
            recepcion.exit(entity, time); // Hace que la entidad salga de la locación de recepción en el tiempo especificado

            // Generar tiempo de transporte E(3)
            double transportTime = randomGen.nextTransportRecepcionLavadora(); // Genera un tiempo de transporte aleatorio con distribución exponencial media 3
            entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
            entity.startTransit(time, transportTime, "LAVADORA"); // Inicia el tránsito de la entidad estableciendo tiempo de inicio, duración y destino
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito

            // Programar llegada a LAVADORA
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "LAVADORA")); // Programa un evento de fin de transporte para cuando la entidad llegue a lavadora
        } else { // Bloque else que se ejecuta si no hay capacidad disponible en la lavadora
            // No hay espacio, agregar a cola de espera
            lavadora.addToQueue(entity); // Agrega la entidad a la cola de espera de la lavadora
        } // Cierre del bloque else
    } // Cierre del método tryMoveToLavadora

    /** // Inicio del comentario Javadoc del método
     * Maneja el fin de un transporte // Descripción del método
     */ // Fin del comentario Javadoc
    public void handleTransportEnd(Entity entity, String destinationName, double time) { // Método público que maneja el fin de un transporte recibiendo la entidad, nombre del destino y tiempo como parámetros
        entitiesInTransport.remove(entity); // Remueve la entidad del conjunto de entidades en tránsito
        entity.endTransit(); // Finaliza el estado de tránsito de la entidad limpiando sus variables de tránsito

        switch (destinationName) { // Estructura switch que evalúa el nombre de la locación de destino
            case "LAVADORA": // Caso para destino lavadora
                arriveAtLavadora(entity, time); // Llama al método que procesa la llegada a lavadora
                break; // Sale del switch después de procesar este caso
            case "ALMACEN_PINTURA": // Caso para destino almacén de pintura
                arriveAtAlmacenPintura(entity, time); // Llama al método que procesa la llegada a almacén de pintura
                break; // Sale del switch después de procesar este caso
            case "PINTURA": // Caso para destino pintura
                arriveAtPintura(entity, time); // Llama al método que procesa la llegada a pintura
                break; // Sale del switch después de procesar este caso
            case "ALMACEN_HORNO": // Caso para destino almacén del horno
                arriveAtAlmacenHorno(entity, time); // Llama al método que procesa la llegada a almacén del horno
                break; // Sale del switch después de procesar este caso
            case "HORNO": // Caso para destino horno
                arriveAtHorno(entity, time); // Llama al método que procesa la llegada al horno
                break; // Sale del switch después de procesar este caso
            case "INSPECCION": // Caso para destino inspección
                arriveAtInspeccion(entity, time); // Llama al método que procesa la llegada a inspección
                break; // Sale del switch después de procesar este caso
        } // Cierre del switch
    } // Cierre del método handleTransportEnd

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a LAVADORA // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtLavadora(Entity entity, double time) { // Método privado que procesa la llegada de una entidad a lavadora recibiendo la entidad y el tiempo como parámetros
        lavadora.commitReservedCapacity(); // Confirma la capacidad previamente reservada en la lavadora
        lavadora.enter(entity, time); // Hace que la entidad entre a la locación de lavadora en el tiempo especificado
        entity.setCurrentLocation("LAVADORA"); // Establece la ubicación actual de la entidad como "LAVADORA"

        // Generar tiempo de proceso N(10, 2)
        double processTime = randomGen.nextLavadoraProcess(); // Genera un tiempo de proceso aleatorio con distribución normal media 10 y desviación estándar 2
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "LAVADORA")); // Programa un evento de fin de proceso para cuando termine el procesamiento en lavadora
    } // Cierre del método arriveAtLavadora

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a PINTURA // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtPintura(Entity entity, double time) { // Método privado que procesa la llegada de una entidad a pintura recibiendo la entidad y el tiempo como parámetros
        pintura.commitReservedCapacity(); // Confirma la capacidad previamente reservada en pintura
        pintura.enter(entity, time); // Hace que la entidad entre a la locación de pintura en el tiempo especificado
        entity.setCurrentLocation("PINTURA"); // Establece la ubicación actual de la entidad como "PINTURA"

        // Generar tiempo de proceso T(4, 8, 10)
        double processTime = randomGen.nextPinturaProcess(); // Genera un tiempo de proceso aleatorio con distribución triangular mínimo 4, moda 8, máximo 10
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "PINTURA")); // Programa un evento de fin de proceso para cuando termine el procesamiento en pintura
    } // Cierre del método arriveAtPintura

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a HORNO // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtHorno(Entity entity, double time) { // Método privado que procesa la llegada de una entidad al horno recibiendo la entidad y el tiempo como parámetros
        horno.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el horno
        horno.enter(entity, time); // Hace que la entidad entre a la locación del horno en el tiempo especificado
        entity.setCurrentLocation("HORNO"); // Establece la ubicación actual de la entidad como "HORNO"

        // Generar tiempo de proceso U(3, 1)
        double processTime = randomGen.nextHornoProcess(); // Genera un tiempo de proceso aleatorio con distribución uniforme entre 2 y 4 (media 3, half-width 1)
        entity.addProcessTime(processTime); // Acumula el tiempo de proceso a las estadísticas de la entidad
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "HORNO")); // Programa un evento de fin de proceso para cuando termine el procesamiento en el horno
    } // Cierre del método arriveAtHorno

    /** // Inicio del comentario Javadoc del método
     * Maneja el fin de un proceso // Descripción del método
     */ // Fin del comentario Javadoc
    public void handleProcessEnd(Entity entity, String locationName, double time) { // Método público que maneja el fin de un proceso recibiendo la entidad, nombre de la locación y tiempo como parámetros
        switch (locationName) { // Estructura switch que evalúa el nombre de la locación donde terminó el proceso
            case "LAVADORA": // Caso para proceso terminado en lavadora
                finishLavadora(entity, time); // Llama al método que procesa la finalización en lavadora
                break; // Sale del switch después de procesar este caso
            case "PINTURA": // Caso para proceso terminado en pintura
                finishPintura(entity, time); // Llama al método que procesa la finalización en pintura
                break; // Sale del switch después de procesar este caso
            case "HORNO": // Caso para proceso terminado en horno
                finishHorno(entity, time); // Llama al método que procesa la finalización en el horno
                break; // Sale del switch después de procesar este caso
        } // Cierre del switch
    } // Cierre del método handleProcessEnd

    /** // Inicio del comentario Javadoc del método
     * Termina proceso en LAVADORA // Descripción del método
     */ // Fin del comentario Javadoc
    private void finishLavadora(Entity entity, double time) { // Método privado que procesa la finalización de un proceso en lavadora recibiendo la entidad y el tiempo como parámetros
        if (!attemptLavadoraDeparture(entity, time)) { // Condición que verifica si el intento de salida de lavadora falló (no hay capacidad en el destino)
            blockedAfterLavadora.addLast(entity); // Agrega la entidad al final de la cola de entidades bloqueadas después de lavadora
            entity.setBlocked(true, time); // Marca la entidad como bloqueada estableciendo su estado y registrando el tiempo de inicio del bloqueo
        } // Cierre del bloque condicional if
    } // Cierre del método finishLavadora

    private boolean attemptLavadoraDeparture(Entity entity, double time) { // Método privado que intenta hacer salir una entidad de lavadora retornando true si tuvo éxito recibiendo la entidad y el tiempo como parámetros
        if (!almacenPintura.canEnter()) { // Condición que verifica si no hay capacidad disponible en el almacén de pintura
            return false; // Retorna falso indicando que la salida falló porque no hay espacio
        } // Cierre del bloque condicional if

        almacenPintura.reserveCapacity(); // Reserva una unidad de capacidad en el almacén de pintura para esta entidad
        entity.setBlocked(false, time); // Marca la entidad como no bloqueada y acumula el tiempo que estuvo bloqueada si aplica

        double transportTime = randomGen.nextTransportLavadoraAlmacen(); // Genera un tiempo de transporte aleatorio con distribución exponencial media 2
        entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, transportTime, "ALMACEN_PINTURA"); // Inicia el tránsito de la entidad estableciendo tiempo de inicio, duración y destino
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito

        lavadora.exit(entity, time); // Hace que la entidad salga de la locación de lavadora en el tiempo especificado
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_PINTURA")); // Programa un evento de fin de transporte para cuando la entidad llegue al almacén de pintura

        processLavadoraQueue(time); // Procesa la cola de espera de lavadora para ver si hay más entidades que puedan entrar
        return true; // Retorna verdadero indicando que la salida fue exitosa
    } // Cierre del método attemptLavadoraDeparture

    private void processLavadoraQueue(double time) { // Método privado que procesa la cola de espera de lavadora recibiendo el tiempo como parámetro
        while (lavadora.canEnter() && lavadora.hasQueuedEntities()) { // Bucle while que continúa mientras haya capacidad en lavadora y haya entidades en la cola
            Entity nextEntity = lavadora.pollFromQueue(); // Obtiene y remueve la siguiente entidad de la cola de espera de lavadora
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida de la cola
                tryMoveToLavadora(nextEntity, time); // Intenta mover la entidad a lavadora
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processLavadoraQueue

    private void releaseBlockedAfterLavadora(double time) { // Método privado que libera entidades bloqueadas después de lavadora recibiendo el tiempo como parámetro
        while (!blockedAfterLavadora.isEmpty() && almacenPintura.canEnter()) { // Bucle while que continúa mientras haya entidades bloqueadas y haya capacidad en almacén de pintura
            Entity blocked = blockedAfterLavadora.peekFirst(); // Obtiene la primera entidad bloqueada sin removerla de la cola para inspección
            if (attemptLavadoraDeparture(blocked, time)) { // Intenta hacer salir la entidad bloqueada de lavadora
                blockedAfterLavadora.removeFirst(); // Remueve la entidad de la cola de bloqueados si la salida fue exitosa
            } else { // Bloque else que se ejecuta si el intento de salida falló
                break; // Sale del bucle porque no hay más espacio disponible
            } // Cierre del bloque else
        } // Cierre del bucle while
    } // Cierre del método releaseBlockedAfterLavadora

    private boolean attemptPinturaDeparture(Entity entity, double time) { // Método privado que intenta hacer salir una entidad de pintura retornando true si tuvo éxito recibiendo la entidad y el tiempo como parámetros
        if (!almacenHorno.canEnter()) { // Condición que verifica si no hay capacidad disponible en el almacén del horno
            return false; // Retorna falso indicando que la salida falló porque no hay espacio
        } // Cierre del bloque condicional if

        almacenHorno.reserveCapacity(); // Reserva una unidad de capacidad en el almacén del horno para esta entidad
        entity.setBlocked(false, time); // Marca la entidad como no bloqueada y acumula el tiempo que estuvo bloqueada si aplica

        double transportTime = randomGen.nextTransportPinturaAlmacen(); // Genera un tiempo de transporte aleatorio con distribución uniforme entre 2 y 5
        entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, transportTime, "ALMACEN_HORNO"); // Inicia el tránsito de la entidad estableciendo tiempo de inicio, duración y destino
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito

        pintura.exit(entity, time); // Hace que la entidad salga de la locación de pintura en el tiempo especificado
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_HORNO")); // Programa un evento de fin de transporte para cuando la entidad llegue al almacén del horno

        processPinturaQueue(time); // Procesa la cola de espera de pintura para ver si hay más entidades que puedan entrar
        return true; // Retorna verdadero indicando que la salida fue exitosa
    } // Cierre del método attemptPinturaDeparture

    private void processPinturaQueue(double time) { // Método privado que procesa la cola de espera de pintura recibiendo el tiempo como parámetro
        while (pintura.canEnter() && pintura.hasQueuedEntities()) { // Bucle while que continúa mientras haya capacidad en pintura y haya entidades en la cola
            Entity nextEntity = pintura.pollFromQueue(); // Obtiene y remueve la siguiente entidad de la cola de espera de pintura
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida de la cola
                tryMoveToPintura(nextEntity, time); // Intenta mover la entidad a pintura
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processPinturaQueue

    private void releaseBlockedAfterPintura(double time) { // Método privado que libera entidades bloqueadas después de pintura recibiendo el tiempo como parámetro
        while (!blockedAfterPintura.isEmpty() && almacenHorno.canEnter()) { // Bucle while que continúa mientras haya entidades bloqueadas y haya capacidad en almacén del horno
            Entity blocked = blockedAfterPintura.peekFirst(); // Obtiene la primera entidad bloqueada sin removerla de la cola para inspección
            if (attemptPinturaDeparture(blocked, time)) { // Intenta hacer salir la entidad bloqueada de pintura
                blockedAfterPintura.removeFirst(); // Remueve la entidad de la cola de bloqueados si la salida fue exitosa
            } else { // Bloque else que se ejecuta si el intento de salida falló
                break; // Sale del bucle porque no hay más espacio disponible
            } // Cierre del bloque else
        } // Cierre del bucle while
    } // Cierre del método releaseBlockedAfterPintura

    private boolean attemptHornoDeparture(Entity entity, double time) { // Método privado que intenta hacer salir una entidad del horno retornando true si tuvo éxito recibiendo la entidad y el tiempo como parámetros
        if (!inspeccion.hasAvailableStation()) { // Condición que verifica si no hay estaciones de inspección disponibles
            return false; // Retorna falso indicando que la salida falló porque no hay estación disponible
        } // Cierre del bloque condicional if

        inspeccion.reserveStation(entity); // Reserva una estación de inspección para esta entidad
        entity.setBlocked(false, time); // Marca la entidad como no bloqueada y acumula el tiempo que estuvo bloqueada si aplica

        double transportTime = randomGen.nextTransportHornoInspeccion(); // Genera un tiempo de transporte aleatorio con distribución uniforme entre 1 y 3
        entity.addTransportTime(transportTime); // Acumula el tiempo de transporte a las estadísticas de la entidad
        entity.startTransit(time, transportTime, "INSPECCION"); // Inicia el tránsito de la entidad estableciendo tiempo de inicio, duración y destino
        entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito

        horno.exit(entity, time); // Hace que la entidad salga de la locación del horno en el tiempo especificado
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION")); // Programa un evento de fin de transporte para cuando la entidad llegue a inspección

        processHornoQueue(time); // Procesa la cola de espera del horno para ver si hay más entidades que puedan entrar
        return true; // Retorna verdadero indicando que la salida fue exitosa
    } // Cierre del método attemptHornoDeparture

    private void processHornoQueue(double time) { // Método privado que procesa la cola de espera del horno recibiendo el tiempo como parámetro
        while (horno.canEnter() && horno.hasQueuedEntities()) { // Bucle while que continúa mientras haya capacidad en el horno y haya entidades en la cola
            Entity nextEntity = horno.pollFromQueue(); // Obtiene y remueve la siguiente entidad de la cola de espera del horno
            if (nextEntity != null) { // Condición que verifica si se obtuvo una entidad válida de la cola
                tryMoveToHorno(nextEntity, time); // Intenta mover la entidad al horno
            } // Cierre del bloque condicional if
        } // Cierre del bucle while
    } // Cierre del método processHornoQueue

    private void releaseBlockedAfterHorno(double time) { // Método privado que libera entidades bloqueadas después del horno recibiendo el tiempo como parámetro
        while (!blockedAfterHorno.isEmpty() && inspeccion.hasAvailableStation()) { // Bucle while que continúa mientras haya entidades bloqueadas y haya estaciones de inspección disponibles
            Entity blocked = blockedAfterHorno.peekFirst(); // Obtiene la primera entidad bloqueada sin removerla de la cola para inspección
            if (attemptHornoDeparture(blocked, time)) { // Intenta hacer salir la entidad bloqueada del horno
                blockedAfterHorno.removeFirst(); // Remueve la entidad de la cola de bloqueados si la salida fue exitosa
            } else { // Bloque else que se ejecuta si el intento de salida falló
                break; // Sale del bucle porque no hay más estaciones disponibles
            } // Cierre del bloque else
        } // Cierre del bucle while
    } // Cierre del método releaseBlockedAfterHorno

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a ALMACEN_PINTURA // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtAlmacenPintura(Entity entity, double time) { // Método privado que procesa la llegada de una entidad al almacén de pintura recibiendo la entidad y el tiempo como parámetros
        almacenPintura.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el almacén de pintura
        almacenPintura.enter(entity, time); // Hace que la entidad entre a la locación del almacén de pintura en el tiempo especificado
        entity.setCurrentLocation("ALMACEN_PINTURA"); // Establece la ubicación actual de la entidad como "ALMACEN_PINTURA"

        // Intentar mover inmediatamente a PINTURA (movimiento instantáneo)
        tryMoveToPintura(entity, time); // Intenta mover la entidad inmediatamente del almacén de pintura a pintura sin tiempo de transporte
    } // Cierre del método arriveAtAlmacenPintura

    /** // Inicio del comentario Javadoc del método
     * Intenta mover entidad de ALMACEN_PINTURA a PINTURA // Descripción del método
     * MOVIMIENTO INSTANTÁNEO (sin tiempo de transporte en estadísticas) // Nota sobre la naturaleza instantánea del movimiento
     */ // Fin del comentario Javadoc
    private void tryMoveToPintura(Entity entity, double time) { // Método privado que intenta mover una entidad a pintura recibiendo la entidad y el tiempo como parámetros
        if (pintura.canEnter()) { // Condición que verifica si hay capacidad disponible en pintura
            pintura.reserveCapacity(); // Reserva una unidad de capacidad en pintura para esta entidad
            almacenPintura.exit(entity, time); // Hace que la entidad salga de la locación del almacén de pintura en el tiempo especificado
            releaseBlockedAfterLavadora(time); // Libera entidades bloqueadas después de lavadora ya que se liberó espacio en almacén de pintura

            // Movimiento instantáneo con animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "PINTURA"); // Inicia un tránsito visual mínimo para animación sin afectar estadísticas de transporte
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito para mostrarla en animación

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "PINTURA")); // Programa un evento de fin de transporte con el tiempo visual mínimo
        } else { // Bloque else que se ejecuta si no hay capacidad disponible en pintura
            // No hay espacio en PINTURA, esperar en almacén
            pintura.addToQueue(entity); // Agrega la entidad a la cola de espera de pintura
        } // Cierre del bloque else
    } // Cierre del método tryMoveToPintura

    /** // Inicio del comentario Javadoc del método
     * Termina proceso en PINTURA // Descripción del método
     */ // Fin del comentario Javadoc
    private void finishPintura(Entity entity, double time) { // Método privado que procesa la finalización de un proceso en pintura recibiendo la entidad y el tiempo como parámetros
        if (!attemptPinturaDeparture(entity, time)) { // Condición que verifica si el intento de salida de pintura falló (no hay capacidad en el destino)
            blockedAfterPintura.addLast(entity); // Agrega la entidad al final de la cola de entidades bloqueadas después de pintura
            entity.setBlocked(true, time); // Marca la entidad como bloqueada estableciendo su estado y registrando el tiempo de inicio del bloqueo
        } // Cierre del bloque condicional if
    } // Cierre del método finishPintura

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a ALMACEN_HORNO // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtAlmacenHorno(Entity entity, double time) { // Método privado que procesa la llegada de una entidad al almacén del horno recibiendo la entidad y el tiempo como parámetros
        almacenHorno.commitReservedCapacity(); // Confirma la capacidad previamente reservada en el almacén del horno
        almacenHorno.enter(entity, time); // Hace que la entidad entre a la locación del almacén del horno en el tiempo especificado
        entity.setCurrentLocation("ALMACEN_HORNO"); // Establece la ubicación actual de la entidad como "ALMACEN_HORNO"

        // Intentar mover inmediatamente a HORNO (movimiento instantáneo)
        tryMoveToHorno(entity, time); // Intenta mover la entidad inmediatamente del almacén del horno al horno sin tiempo de transporte
    } // Cierre del método arriveAtAlmacenHorno

    /** // Inicio del comentario Javadoc del método
     * Intenta mover entidad de ALMACEN_HORNO a HORNO // Descripción del método
     * MOVIMIENTO INSTANTÁNEO (sin tiempo de transporte en estadísticas) // Nota sobre la naturaleza instantánea del movimiento
     */ // Fin del comentario Javadoc
    private void tryMoveToHorno(Entity entity, double time) { // Método privado que intenta mover una entidad al horno recibiendo la entidad y el tiempo como parámetros
        if (horno.canEnter()) { // Condición que verifica si hay capacidad disponible en el horno
            horno.reserveCapacity(); // Reserva una unidad de capacidad en el horno para esta entidad
            almacenHorno.exit(entity, time); // Hace que la entidad salga de la locación del almacén del horno en el tiempo especificado
            releaseBlockedAfterPintura(time); // Libera entidades bloqueadas después de pintura ya que se liberó espacio en almacén del horno

            // Movimiento instantáneo con animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "HORNO"); // Inicia un tránsito visual mínimo para animación sin afectar estadísticas de transporte
            entitiesInTransport.add(entity); // Agrega la entidad al conjunto de entidades en tránsito para mostrarla en animación

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "HORNO")); // Programa un evento de fin de transporte con el tiempo visual mínimo
        } else { // Bloque else que se ejecuta si no hay capacidad disponible en el horno
            // No hay espacio en HORNO, esperar en almacén
            horno.addToQueue(entity); // Agrega la entidad a la cola de espera del horno
        } // Cierre del bloque else
    } // Cierre del método tryMoveToHorno

    /** // Inicio del comentario Javadoc del método
     * Termina proceso en HORNO // Descripción del método
     */ // Fin del comentario Javadoc
    private void finishHorno(Entity entity, double time) { // Método privado que procesa la finalización de un proceso en el horno recibiendo la entidad y el tiempo como parámetros
        if (!attemptHornoDeparture(entity, time)) { // Condición que verifica si el intento de salida del horno falló (no hay estación de inspección disponible)
            blockedAfterHorno.addLast(entity); // Agrega la entidad al final de la cola de entidades bloqueadas después del horno
            entity.setBlocked(true, time); // Marca la entidad como bloqueada estableciendo su estado y registrando el tiempo de inicio del bloqueo
        } // Cierre del bloque condicional if
    } // Cierre del método finishHorno

    /** // Inicio del comentario Javadoc del método
     * Entidad llega a INSPECCION // Descripción del método
     */ // Fin del comentario Javadoc
    private void arriveAtInspeccion(Entity entity, double time) { // Método privado que procesa la llegada de una entidad a inspección recibiendo la entidad y el tiempo como parámetros
        inspeccion.commitReservationFor(entity); // Confirma la reservación de estación previamente hecha para esta entidad
        inspeccion.enter(entity, time); // Hace que la entidad entre a la estación de inspección en el tiempo especificado
        entity.setCurrentLocation("INSPECCION"); // Establece la ubicación actual de la entidad como "INSPECCION"

        // Iniciar primera operación de inspección E(2)
        double operationTime = randomGen.nextInspeccionOperation(); // Genera un tiempo de operación aleatorio con distribución exponencial media 2
        entity.addProcessTime(operationTime); // Acumula el tiempo de operación a las estadísticas de tiempo de proceso de la entidad
        scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity)); // Programa un evento de fin de operación de inspección para cuando termine esta operación
    } // Cierre del método arriveAtInspeccion

    /** // Inicio del comentario Javadoc del método
     * Maneja el fin de una operación de inspección // Descripción del método
     */ // Fin del comentario Javadoc
    public void handleInspectionOperationEnd(Entity entity, double time) { // Método público que maneja el fin de una operación de inspección recibiendo la entidad y el tiempo como parámetros
        // Incrementar contador de operaciones
        inspeccion.incrementOperationCount(entity); // Incrementa el contador de operaciones completadas para esta entidad en la estación de inspección

        // Verificar si completó todas las operaciones (3 total)
        if (inspeccion.hasCompletedAllOperations(entity)) { // Condición que verifica si la entidad completó todas las operaciones de inspección requeridas
            // Salir del sistema
            inspeccion.exit(entity, time); // Hace que la entidad salga de la estación de inspección en el tiempo especificado
            releaseBlockedAfterHorno(time); // Libera entidades bloqueadas después del horno ya que se liberó una estación de inspección

            // Registrar salida del sistema
            allActiveEntities.remove(entity); // Remueve la entidad de la lista de entidades activas en el sistema
            statistics.recordExit(entity, time); // Registra la salida de la entidad en las estadísticas con todos sus tiempos acumulados
        } else { // Bloque else que se ejecuta si la entidad aún tiene operaciones pendientes
            // Continuar con siguiente operación E(2)
            double operationTime = randomGen.nextInspeccionOperation(); // Genera un tiempo de operación aleatorio con distribución exponencial media 2 para la siguiente operación
            entity.addProcessTime(operationTime); // Acumula el tiempo de operación a las estadísticas de tiempo de proceso de la entidad
            scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity)); // Programa un evento de fin de operación de inspección para cuando termine la siguiente operación
        } // Cierre del bloque else
    } // Cierre del método handleInspectionOperationEnd

    /** // Inicio del comentario Javadoc del método
     * Programa un evento en la cola de eventos // Descripción del método
     */ // Fin del comentario Javadoc
    private void scheduleEvent(Event event) { // Método privado que agrega un evento a la cola de eventos recibiendo el evento como parámetro
        eventQueue.add(event); // Agrega el evento a la cola de prioridad que automáticamente lo ordena por tiempo de ocurrencia
    } // Cierre del método scheduleEvent

    // Getters para acceso externo
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
        paused = true; // Establece el estado de pausa como verdadero para detener temporalmente el procesamiento de eventos
    } // Cierre del método pause

    public void resume() { // Método público que reanuda la simulación después de una pausa sin recibir parámetros
        paused = false; // Establece el estado de pausa como falso para reanudar el procesamiento de eventos
    } // Cierre del método resume

    public boolean isPaused() { // Método público getter que retorna si la simulación está pausada de tipo boolean
        return paused; // Retorna el valor de la variable paused
    } // Cierre del método isPaused

    public Statistics getStatistics() { // Método público getter que retorna el objeto de estadísticas de la simulación de tipo Statistics
        return statistics; // Retorna el valor de la variable statistics
    } // Cierre del método getStatistics

    public Location getLocation(String name) { // Método público que retorna una locación específica por su nombre recibiendo el nombre como parámetro de tipo String y retornando un objeto Location
        switch (name) { // Estructura switch que evalúa el nombre de la locación solicitada
            case "RECEPCION": return recepcion; // Caso para recepción: retorna la referencia a la locación de recepción
            case "LAVADORA": return lavadora; // Caso para lavadora: retorna la referencia a la locación de lavadora
            case "ALMACEN_PINTURA": return almacenPintura; // Caso para almacén de pintura: retorna la referencia a la locación del almacén de pintura
            case "PINTURA": return pintura; // Caso para pintura: retorna la referencia a la locación de pintura
            case "ALMACEN_HORNO": return almacenHorno; // Caso para almacén del horno: retorna la referencia a la locación del almacén del horno
            case "HORNO": return horno; // Caso para horno: retorna la referencia a la locación del horno
            case "INSPECCION": return inspeccion; // Caso para inspección: retorna la referencia a la estación de inspección
            default: return null; // Caso por defecto: retorna null si el nombre no coincide con ninguna locación conocida
        } // Cierre del switch
    } // Cierre del método getLocation

    public Set<Entity> getEntitiesInTransport() { // Método público que retorna una copia del conjunto de entidades en tránsito de tipo Set<Entity>
        return new HashSet<>(entitiesInTransport); // Retorna una nueva instancia de HashSet con una copia de las entidades en tránsito para evitar modificaciones externas
    } // Cierre del método getEntitiesInTransport

    public List<Entity> getAllActiveEntities() { // Método público que retorna una copia thread-safe de todas las entidades activas de tipo List<Entity>
        synchronized (allActiveEntities) { // Bloque sincronizado que asegura acceso thread-safe a la lista de entidades activas
            List<Entity> safeCopy = new ArrayList<>(); // Crea una nueva lista vacía para almacenar la copia segura de las entidades
            for (Entity entity : allActiveEntities) { // Bucle for-each que itera sobre cada entidad en la lista de entidades activas
                if (entity != null) { // Condición que verifica si la entidad no es null para evitar agregar referencias nulas
                    safeCopy.add(entity); // Agrega la entidad válida a la lista de copia segura
                } // Cierre del bloque condicional if
            } // Cierre del bucle for-each
            return safeCopy; // Retorna la lista de copia segura con todas las entidades activas válidas
        } // Cierre del bloque sincronizado
    } // Cierre del método getAllActiveEntities
} // Cierre de la clase SimulationEngine
