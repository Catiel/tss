package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.*;
import com.simulation.statistics.Statistics;
import com.simulation.core.EventTypes.*;

import java.util.*;

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

    private Location recepcion;
    private ProcessingLocation lavadora;
    private BufferLocation almacenPintura;
    private ProcessingLocation pintura;
    private BufferLocation almacenHorno;
    private ProcessingLocation horno;
    private InspectionStation inspeccion;

    private Set<Entity> entitiesInTransport;
    private List<Entity> allActiveEntities;

    // Tiempo mínimo para animación visual SOLAMENTE (no afecta estadísticas)
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

        statistics.registerLocation(recepcion);
        statistics.registerLocation(lavadora);
        statistics.registerLocation(almacenPintura);
        statistics.registerLocation(pintura);
        statistics.registerLocation(almacenHorno);
        statistics.registerLocation(horno);
        statistics.registerLocation(inspeccion);
    }

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

    public void initialize() {
        reset();
        lastRealTime = System.currentTimeMillis();
        double firstArrival = randomGen.nextArrivalTime();
        scheduleEvent(new ArrivalEvent(firstArrival));
    }

    public void setSimulationSpeed(double minutesPerSecond) {
        this.simulationSpeed = minutesPerSecond;
    }

    public void run() {
        running = true;
        double endTime = params.getSimulationDurationMinutes();

        while (running && !eventQueue.isEmpty()) {
            while (paused && running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (!running) break;

            Event nextEvent = eventQueue.peek();
            if (nextEvent == null || nextEvent.getTime() >= endTime) {
                break;
            }

            double targetSimTime = nextEvent.getTime();

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

            lastRealTime = System.currentTimeMillis();
            eventQueue.poll();
            currentTime = targetSimTime;

            if (currentTime >= endTime) {
                break;
            }

            nextEvent.execute(this);
        }

        statistics.finalizeStatistics(currentTime);
        running = false;
    }

    public void handleArrival(double time) {
        Entity entity = new Entity(time);
        statistics.recordArrival();

        allActiveEntities.add(entity);

        recepcion.enter(entity, time);
        entity.setCurrentLocation("RECEPCION");

        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            scheduleEvent(new ArrivalEvent(nextArrival));
        }

        tryMoveToLavadora(entity, time);
    }

    private void tryMoveToLavadora(Entity entity, double time) {
        if (lavadora.canEnter()) {
            recepcion.exit(entity, time);

            double transportTime = randomGen.nextTransportRecepcionLavadora();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "LAVADORA");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "LAVADORA"));
        } else {
            lavadora.addToQueue(entity);
            entity.addWaitTime(0);
        }
    }

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
            case "PINTURA_VISUAL":
                // Llegada visual a PINTURA
                arriveAtPintura(entity, time);
                break;
            case "ALMACEN_HORNO":
                arriveAtAlmacenHorno(entity, time);
                break;
            case "HORNO_VISUAL":
                // Llegada visual a HORNO
                arriveAtHorno(entity, time);
                break;
            case "INSPECCION":
                arriveAtInspeccion(entity, time);
                break;
        }
    }

    private void arriveAtLavadora(Entity entity, double time) {
        lavadora.enter(entity, time);
        entity.setCurrentLocation("LAVADORA");

        double processTime = randomGen.nextLavadoraProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "LAVADORA"));
    }

    private void arriveAtPintura(Entity entity, double time) {
        pintura.enter(entity, time);
        entity.setCurrentLocation("PINTURA");

        double processTime = randomGen.nextPinturaProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "PINTURA"));
    }

    private void arriveAtHorno(Entity entity, double time) {
        horno.enter(entity, time);
        entity.setCurrentLocation("HORNO");

        double processTime = randomGen.nextHornoProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "HORNO"));
    }

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

    private void finishLavadora(Entity entity, double time) {
        lavadora.exit(entity, time);

        if (lavadora.hasQueuedEntities() && lavadora.canEnter()) {
            Entity nextEntity = lavadora.pollFromQueue();
            tryMoveToLavadora(nextEntity, time);
        }

        tryMoveToAlmacenPintura(entity, time);
    }

    private void tryMoveToAlmacenPintura(Entity entity, double time) {
        if (almacenPintura.canEnter()) {
            double transportTime = randomGen.nextTransportLavadoraAlmacen();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN_PINTURA");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_PINTURA"));
        } else {
            almacenPintura.addToQueue(entity);
            entity.setBlocked(true, time);
        }
    }

    private void arriveAtAlmacenPintura(Entity entity, double time) {
        almacenPintura.enter(entity, time);
        entity.setCurrentLocation("ALMACEN_PINTURA");
        tryMoveToPintura(entity, time);
    }

    private void tryMoveToPintura(Entity entity, double time) {
        if (pintura.canEnter()) {
            almacenPintura.exit(entity, time);

            // Movimiento instantáneo CON animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "PINTURA_VISUAL");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "PINTURA_VISUAL"));
        } else {
            pintura.addToQueue(entity);
        }
    }

    private void finishPintura(Entity entity, double time) {
        pintura.exit(entity, time);

        if (pintura.hasQueuedEntities() && pintura.canEnter()) {
            Entity nextEntity = pintura.pollFromQueue();
            tryMoveToPintura(nextEntity, time);
        }

        if (almacenPintura.hasQueuedEntities() && almacenPintura.canEnter()) {
            Entity blockedEntity = almacenPintura.pollFromQueue();
            blockedEntity.setBlocked(false, time);
            tryMoveToAlmacenPintura(blockedEntity, time);
        }

        tryMoveToAlmacenHorno(entity, time);
    }

    private void tryMoveToAlmacenHorno(Entity entity, double time) {
        if (almacenHorno.canEnter()) {
            double transportTime = randomGen.nextTransportPinturaAlmacen();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN_HORNO");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_HORNO"));
        } else {
            almacenHorno.addToQueue(entity);
            entity.setBlocked(true, time);
        }
    }

    private void arriveAtAlmacenHorno(Entity entity, double time) {
        almacenHorno.enter(entity, time);
        entity.setCurrentLocation("ALMACEN_HORNO");
        tryMoveToHorno(entity, time);
    }

    private void tryMoveToHorno(Entity entity, double time) {
        if (horno.canEnter()) {
            almacenHorno.exit(entity, time);

            // Movimiento instantáneo CON animación visual
            entity.startTransit(time, VISUAL_TRANSIT_TIME, "HORNO_VISUAL");
            entitiesInTransport.add(entity);

            scheduleEvent(new TransportEndEvent(time + VISUAL_TRANSIT_TIME, entity, "HORNO_VISUAL"));
        } else {
            horno.addToQueue(entity);
        }
    }

    private void finishHorno(Entity entity, double time) {
        horno.exit(entity, time);

        if (horno.hasQueuedEntities() && horno.canEnter()) {
            Entity nextEntity = horno.pollFromQueue();
            tryMoveToHorno(nextEntity, time);
        }

        if (almacenHorno.hasQueuedEntities() && almacenHorno.canEnter()) {
            Entity blockedEntity = almacenHorno.pollFromQueue();
            blockedEntity.setBlocked(false, time);
            tryMoveToAlmacenHorno(blockedEntity, time);
        }

        tryMoveToInspeccion(entity, time);
    }

    private void tryMoveToInspeccion(Entity entity, double time) {
        double transportTime = randomGen.nextTransportHornoInspeccion();
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "INSPECCION");
        entitiesInTransport.add(entity);

        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION"));
    }

    private void arriveAtInspeccion(Entity entity, double time) {
        if (inspeccion.canEnter()) {
            inspeccion.enter(entity, time);
            entity.setCurrentLocation("INSPECCION");

            double operationTime = randomGen.nextInspeccionOperation();
            entity.addProcessTime(operationTime);
            scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity));
        } else {
            inspeccion.addToQueue(entity);
        }
    }

    public void handleInspectionOperationEnd(Entity entity, double time) {
        inspeccion.incrementOperationCount(entity);

        if (inspeccion.hasCompletedAllOperations(entity)) {
            inspeccion.exit(entity, time);

            if (inspeccion.hasQueuedEntities() && inspeccion.canEnter()) {
                Entity nextEntity = inspeccion.pollFromQueue();
                arriveAtInspeccion(nextEntity, time);
            }

            allActiveEntities.remove(entity);
            statistics.recordExit(entity, time);
        } else {
            double operationTime = randomGen.nextInspeccionOperation();
            entity.addProcessTime(operationTime);
            scheduleEvent(new InspectionOperationEndEvent(time + operationTime, entity));
        }
    }

    private void scheduleEvent(Event event) {
        eventQueue.add(event);
    }

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
