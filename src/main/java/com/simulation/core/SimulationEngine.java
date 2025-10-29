package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.*;
import com.simulation.statistics.Statistics;
import com.simulation.core.EventTypes.*;

import java.util.*;

/**
 * Motor de simulación de eventos discretos
 * CORREGIDO para representar fielmente el modelo ProModel
 */
public class SimulationEngine {
    private SimulationParameters params;
    private RandomGenerators randomGen;
    private Statistics statistics;
    private PriorityQueue<Event> eventQueue;

    private double currentTime;
    private boolean running;
    private boolean paused;

    private volatile double simulationSpeed = 100.0;
    private long lastRealTime = 0;

    // Locaciones del sistema
    private Location recepcion;
    private ProcessingLocation lavadora;
    private BufferLocation almacenPintura;
    private ProcessingLocation pintura;
    private BufferLocation almacenHorno;
    private ProcessingLocation horno;
    private InspectionStation inspeccion;

    private Set<Entity> entitiesInTransport;
    private List<Entity> allActiveEntities;

    // Tiempo mínimo para animación visual (NO afecta estadísticas)
    private static final double VISUAL_TRANSIT_TIME = 0.05;

    public SimulationEngine(SimulationParameters params) {
        this.params = params;
        this.statistics = new Statistics();
        this.eventQueue = new PriorityQueue<>();
        this.entitiesInTransport = new HashSet<>();
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>());
        this.currentTime = 0;
        this.running = false;
        this.paused = false;

        initializeLocations();
        initializeRandomGenerators();
    }

    /**
     * Inicializa todas las locaciones del sistema
     */
    private void initializeLocations() {
        recepcion = new Location("RECEPCION", Integer.MAX_VALUE) {};
        lavadora = new ProcessingLocation("LAVADORA", params.getLavadoraCapacity());
        almacenPintura = new BufferLocation("ALMACEN_PINTURA", params.getAlmacenPinturaCapacity());
        pintura = new ProcessingLocation("PINTURA", params.getPinturaCapacity());
        almacenHorno = new BufferLocation("ALMACEN_HORNO", params.getAlmacenHornoCapacity());
        horno = new ProcessingLocation("HORNO", params.getHornoCapacity());
        inspeccion = new InspectionStation("INSPECCION",
                params.getInspeccionNumStations(),
                params.getInspeccionOperationsPerPiece());

        // Registrar locaciones para estadísticas
        statistics.registerLocation(recepcion);
        statistics.registerLocation(lavadora);
        statistics.registerLocation(almacenPintura);
        statistics.registerLocation(pintura);
        statistics.registerLocation(almacenHorno);
        statistics.registerLocation(horno);
        statistics.registerLocation(inspeccion);
    }

    /**
     * Inicializa generadores de números aleatorios
     */
    private void initializeRandomGenerators() {
        randomGen = new RandomGenerators(params.getBaseRandomSeed());
        randomGen.initialize(
                params.getArrivalMeanTime(),
                params.getTransportRecepcionLavadoraMean(),
                params.getTransportLavadoraAlmacenMean(),
                params.getLavadoraProcessMean(),
                params.getLavadoraProcessStdDev(),
                params.getPinturaProcessMin(),
                params.getPinturaProcessMode(),
                params.getPinturaProcessMax(),
                params.getTransportPinturaAlmacenMin(),
                params.getTransportPinturaAlmacenMax(),
                params.getHornoProcessMin(),
                params.getHornoProcessMax(),
                params.getTransportHornoInspeccionMin(),
                params.getTransportHornoInspeccionMax(),
                params.getInspeccionOperationMean()
        );
    }

    /**
     * Reinicia completamente la simulación
     */
    public void reset() {
        eventQueue.clear();
        entitiesInTransport.clear();
        allActiveEntities.clear();
        currentTime = 0;
        running = false;
        paused = false;
        lastRealTime = 0;
        Entity.resetIdCounter();
        statistics.reset();
        initializeRandomGenerators();
    }

    /**
     * Inicializa la simulación programando el primer arribo
     */
    public void initialize() {
        reset();
        lastRealTime = System.currentTimeMillis();
        double firstArrival = randomGen.nextArrivalTime();
        scheduleEvent(new ArrivalEvent(firstArrival));
    }

    /**
     * Establece la velocidad de simulación
     * @param minutesPerSecond Minutos simulados por segundo real
     */
    public void setSimulationSpeed(double minutesPerSecond) {
        this.simulationSpeed = minutesPerSecond;
    }

    /**
     * Ejecuta la simulación hasta completar o hasta que se detenga
     */
    public void run() {
        running = true;
        double endTime = params.getSimulationDurationMinutes();

        while (running && !eventQueue.isEmpty()) {
            // Manejo de pausa
            while (paused && running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (!running) break;

            // Obtener siguiente evento
            Event nextEvent = eventQueue.peek();
            if (nextEvent == null || nextEvent.getTime() >= endTime) {
                break;
            }

            double targetSimTime = nextEvent.getTime();

            // Control de velocidad de simulación (para visualización)
            long currentRealTime = System.currentTimeMillis();
            double elapsedRealSeconds = (currentRealTime - lastRealTime) / 1000.0;
            double simulatedMinutes = elapsedRealSeconds * simulationSpeed;

            double timeUntilEvent = targetSimTime - currentTime;
            if (timeUntilEvent > simulatedMinutes && simulationSpeed < 10000) {
                long waitTimeMs = (long) ((timeUntilEvent / simulationSpeed) * 1000);
                waitTimeMs = Math.min(waitTimeMs, 50);

                if (waitTimeMs > 0) {
                    try {
                        Thread.sleep(waitTimeMs);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        return;
                    }
                }
            }

            // Procesar evento
            lastRealTime = System.currentTimeMillis();
            eventQueue.poll();
            currentTime = targetSimTime;

            if (currentTime >= endTime) {
                break;
            }

            nextEvent.execute(this);
        }

        // Finalizar estadísticas
        statistics.finalizeStatistics(currentTime);
        running = false;
    }

    /**
     * Maneja el arribo de una nueva pieza al sistema
     */
    public void handleArrival(double time) {
        // Crear nueva entidad
        Entity entity = new Entity(time);
        statistics.recordArrival();
        allActiveEntities.add(entity);

        // Entrar a RECEPCION
        recepcion.enter(entity, time);
        entity.setCurrentLocation("RECEPCION");

        // Programar siguiente arribo si aún no termina la simulación
        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            scheduleEvent(new ArrivalEvent(nextArrival));
        }

        // Intentar mover inmediatamente a LAVADORA
        tryMoveToLavadora(entity, time);
    }

    /**
     * Intenta mover una entidad de RECEPCION a LAVADORA
     */
    private void tryMoveToLavadora(Entity entity, double time) {
        if (lavadora.canEnter()) {
            // Salir de recepción
            recepcion.exit(entity, time);

            // Generar tiempo de transporte E(3)
            double transportTime = randomGen.nextTransportRecepcionLavadora();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "LAVADORA");
            entitiesInTransport.add(entity);

            // Programar llegada a LAVADORA
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "LAVADORA"));
        } else {
            // No hay espacio, agregar a cola de espera
            lavadora.addToQueue(entity);
            entity.addWaitTime(0); // Marca inicio de espera
        }
    }

    /**
     * Maneja el fin de un transporte
     */
    public void handleTransportEnd(Entity entity, String destinationName, double time) {
        entitiesInTransport.remove(entity);
        entity.endTransit();

        switch (destinationName) {
            case "LAVADORA":
                arriveAtLavadora(entity, time);
                break;
            case "ALMACEN_PINTURA":
                arriveAtAlmacenPintura(entity, time);
                break;
            case "PINTURA":
                arriveAtPintura(entity, time);
                break;
            case "ALMACEN_HORNO":
                arriveAtAlmacenHorno(entity, time);
                break;
            case "HORNO":
                arriveAtHorno(entity, time);
                break;
            case "INSPECCION":
                arriveAtInspeccion(entity, time);
                break;
        }
    }

    /**
     * Entidad llega a LAVADORA
     */
    private void arriveAtLavadora(Entity entity, double time) {
        lavadora.enter(entity, time);
        entity.setCurrentLocation("LAVADORA");

        // Generar tiempo de proceso N(10, 2)
        double processTime = randomGen.nextLavadoraProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "LAVADORA"));
    }

    /**
     * Entidad llega a PINTURA
     */
    private void arriveAtPintura(Entity entity, double time) {
        pintura.enter(entity, time);
        entity.setCurrentLocation("PINTURA");

        // Generar tiempo de proceso T(4, 8, 10)
        double processTime = randomGen.nextPinturaProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "PINTURA"));
    }

    /**
     * Entidad llega a HORNO
     */
    private void arriveAtHorno(Entity entity, double time) {
        horno.enter(entity, time);
        entity.setCurrentLocation("HORNO");

        // Generar tiempo de proceso U(3, 1)
        double processTime = randomGen.nextHornoProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "HORNO"));
    }

    /**
     * Maneja el fin de un proceso
     */
    public void handleProcessEnd(Entity entity, String locationName, double time) {
        switch (locationName) {
            case "LAVADORA":
                finishLavadora(entity, time);
                break;
            case "PINTURA":
                finishPintura(entity, time);
                break;
            case "HORNO":
                finishHorno(entity, time);
                break;
        }
    }

    /**
     * Termina proceso en LAVADORA
     */
    private void finishLavadora(Entity entity, double time) {
        // Salir de LAVADORA
        lavadora.exit(entity, time);

        // Procesar siguiente en cola de LAVADORA
        if (lavadora.hasQueuedEntities() && lavadora.canEnter()) {
            Entity nextEntity = lavadora.pollFromQueue();
            tryMoveToLavadora(nextEntity, time);
        }

        // Mover a ALMACEN_PINTURA
        tryMoveToAlmacenPintura(entity, time);
    }

    /**
     * Intenta mover entidad a ALMACEN_PINTURA
     */
    private void tryMoveToAlmacenPintura(Entity entity, double time) {
        if (almacenPintura.canEnter()) {
            // Generar tiempo de transporte E(2)
            double transportTime = randomGen.nextTransportLavadoraAlmacen();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN_PINTURA");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_PINTURA"));
        } else {
            // Almacén lleno - BLOQUEO
            almacenPintura.addToQueue(entity);
            entity.setBlocked(true, time);
        }
    }

    /**
     * Entidad llega a ALMACEN_PINTURA
     */
    private void arriveAtAlmacenPintura(Entity entity, double time) {
        almacenPintura.enter(entity, time);
        entity.setCurrentLocation("ALMACEN_PINTURA");

        // Intentar mover inmediatamente a PINTURA (movimiento instantáneo)
        tryMoveToPintura(entity, time);
    }

    /**
     * Intenta mover entidad de ALMACEN_PINTURA a PINTURA
     * MOVIMIENTO INSTANTÁNEO (sin tiempo de transporte en estadísticas)
     */
    private void tryMoveToPintura(Entity entity, double time) {
        if (pintura.canEnter()) {
            almacenPintura.exit(entity, time);

            // Movimiento instantáneo con animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "PINTURA");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "PINTURA"));
        } else {
            // No hay espacio en PINTURA, esperar en almacén
            pintura.addToQueue(entity);
        }
    }

    /**
     * Termina proceso en PINTURA
     */
    private void finishPintura(Entity entity, double time) {
        // Salir de PINTURA
        pintura.exit(entity, time);

        // Procesar siguiente en cola de PINTURA
        if (pintura.hasQueuedEntities() && pintura.canEnter()) {
            Entity nextEntity = pintura.pollFromQueue();
            tryMoveToPintura(nextEntity, time);
        }

        // Desbloquear entidades en cola de ALMACEN_PINTURA
        if (almacenPintura.hasQueuedEntities() && almacenPintura.canEnter()) {
            Entity blockedEntity = almacenPintura.pollFromQueue();
            blockedEntity.setBlocked(false, time);
            tryMoveToAlmacenPintura(blockedEntity, time);
        }

        // Mover a ALMACEN_HORNO
        tryMoveToAlmacenHorno(entity, time);
    }

    /**
     * Intenta mover entidad a ALMACEN_HORNO
     */
    private void tryMoveToAlmacenHorno(Entity entity, double time) {
        if (almacenHorno.canEnter()) {
            // Generar tiempo de transporte U(3.5, 1.5) = [2, 5]
            double transportTime = randomGen.nextTransportPinturaAlmacen();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN_HORNO");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_HORNO"));
        } else {
            // Almacén lleno - BLOQUEO
            almacenHorno.addToQueue(entity);
            entity.setBlocked(true, time);
        }
    }

    /**
     * Entidad llega a ALMACEN_HORNO
     */
    private void arriveAtAlmacenHorno(Entity entity, double time) {
        almacenHorno.enter(entity, time);
        entity.setCurrentLocation("ALMACEN_HORNO");

        // Intentar mover inmediatamente a HORNO (movimiento instantáneo)
        tryMoveToHorno(entity, time);
    }

    /**
     * Intenta mover entidad de ALMACEN_HORNO a HORNO
     * MOVIMIENTO INSTANTÁNEO (sin tiempo de transporte en estadísticas)
     */
    private void tryMoveToHorno(Entity entity, double time) {
        if (horno.canEnter()) {
            almacenHorno.exit(entity, time);

            // Movimiento instantáneo con animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "HORNO");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "HORNO"));
        } else {
            // No hay espacio en HORNO, esperar en almacén
            horno.addToQueue(entity);
        }
    }

    /**
     * Termina proceso en HORNO
     */
    private void finishHorno(Entity entity, double time) {
        // Salir de HORNO
        horno.exit(entity, time);

        // Procesar siguiente en cola de HORNO
        if (horno.hasQueuedEntities() && horno.canEnter()) {
            Entity nextEntity = horno.pollFromQueue();
            tryMoveToHorno(nextEntity, time);
        }

        // Desbloquear entidades en cola de ALMACEN_HORNO
        if (almacenHorno.hasQueuedEntities() && almacenHorno.canEnter()) {
            Entity blockedEntity = almacenHorno.pollFromQueue();
            blockedEntity.setBlocked(false, time);
            tryMoveToAlmacenHorno(blockedEntity, time);
        }

        // Mover a INSPECCION
        tryMoveToInspeccion(entity, time);
    }

    /**
     * Intenta mover entidad a INSPECCION
     */
    private void tryMoveToInspeccion(Entity entity, double time) {
        // Generar tiempo de transporte U(2, 1) = [1, 3]
        double transportTime = randomGen.nextTransportHornoInspeccion();
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "INSPECCION");
        entitiesInTransport.add(entity);

        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION"));
    }

    /**
     * Entidad llega a INSPECCION
     */
    private void arriveAtInspeccion(Entity entity, double time) {
        if (inspeccion.canEnter()) {
            inspeccion.enter(entity, time);
            entity.setCurrentLocation("INSPECCION");

            // Iniciar primera operación de inspección E(2)
            double operationTime = randomGen.nextInspeccionOperation();
            entity.addProcessTime(operationTime);
            scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity));
        } else {
            // No hay espacio en ninguna mesa, agregar a cola
            inspeccion.addToQueue(entity);
        }
    }

    /**
     * Maneja el fin de una operación de inspección
     */
    public void handleInspectionOperationEnd(Entity entity, double time) {
        // Incrementar contador de operaciones
        inspeccion.incrementOperationCount(entity);

        // Verificar si completó todas las operaciones (3 total)
        if (inspeccion.hasCompletedAllOperations(entity)) {
            // Salir del sistema
            inspeccion.exit(entity, time);

            // Procesar siguiente en cola
            if (inspeccion.hasQueuedEntities() && inspeccion.canEnter()) {
                Entity nextEntity = inspeccion.pollFromQueue();
                arriveAtInspeccion(nextEntity, time);
            }

            // Registrar salida del sistema
            allActiveEntities.remove(entity);
            statistics.recordExit(entity, time);
        } else {
            // Continuar con siguiente operación E(2)
            double operationTime = randomGen.nextInspeccionOperation();
            entity.addProcessTime(operationTime);
            scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity));
        }
    }

    /**
     * Programa un evento en la cola de eventos
     */
    private void scheduleEvent(Event event) {
        eventQueue.add(event);
    }

    // Getters para acceso externo
    public double getCurrentTime() {
        return currentTime;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        paused = false;
    }

    public boolean isPaused() {
        return paused;
    }

    public Statistics getStatistics() {
        return statistics;
    }

    public Location getLocation(String name) {
        switch (name) {
            case "RECEPCION": return recepcion;
            case "LAVADORA": return lavadora;
            case "ALMACEN_PINTURA": return almacenPintura;
            case "PINTURA": return pintura;
            case "ALMACEN_HORNO": return almacenHorno;
            case "HORNO": return horno;
            case "INSPECCION": return inspeccion;
            default: return null;
        }
    }

    public Set<Entity> getEntitiesInTransport() {
        return new HashSet<>(entitiesInTransport);
    }

    public List<Entity> getAllActiveEntities() {
        synchronized (allActiveEntities) {
            List<Entity> safeCopy = new ArrayList<>();
            for (Entity entity : allActiveEntities) {
                if (entity != null) {
                    safeCopy.add(entity);
                }
            }
            return safeCopy;
        }
    }
}