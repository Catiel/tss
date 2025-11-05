package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.*;
import com.simulation.statistics.Statistics;
import com.simulation.core.EventTypes.*;

import java.util.*;

/**
 * Motor de simulación Multi-Engrane - Fabricación de engranes con 12 locaciones
 * CORREGIDO: Las entidades permanecen en locaciones hasta que hay capacidad en destino
 * CORREGIDO: Routing probabilístico se decide UNA VEZ y se mantiene en reintentos
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

    // 12 Locaciones del sistema Multi-Engrane (según ProModel)
    private BufferLocation conveyor1;
    private BufferLocation almacen;
    private ProcessingLocation cortadora;
    private ProcessingLocation torno;
    private BufferLocation conveyor2;
    private ProcessingLocation fresadora;
    private BufferLocation almacen2;
    private ProcessingLocation pintura;
    private ProcessingLocation inspeccion1;
    private ProcessingLocation inspeccion2;
    private ProcessingLocation empaque;
    private ProcessingLocation embarque;

    private Set<Entity> entitiesInTransport;
    private List<Entity> allActiveEntities;

    private TransportResource trabajador1;
    private TransportResource trabajador2;
    private TransportResource trabajador3;
    private TransportResource montacargas;
    private TransportResource montacargasSec;

    private static final double VISUAL_TRANSIT_TIME = 0.05;
    private static final double R1_TRAVEL_TIME = 54.28 / 150.0;
    private static final double R2_TRAVEL_TIME = 36.83 / 150.0;
    private static final double R3_TRAVEL_TIME = 45.15 / 150.0;
    private static final double R4_TRAVEL_TIME = 33.13 / 150.0;
    private static final double R5_TRAVEL_TIME = 56.86 / 150.0;

    public SimulationEngine(SimulationParameters params) {
        this.params = params;
        this.statistics = new Statistics();
        this.eventQueue = new PriorityQueue<>();
        this.entitiesInTransport = new HashSet<>();
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>());
        initializeTransportResources();
        this.currentTime = 0;
        this.running = false;
        this.paused = false;
        initializeLocations();
        initializeRandomGenerators();
    }

    private void initializeLocations() {
        conveyor1 = new BufferLocation("CONVEYOR_1", params.getConveyor1Capacity());
        almacen = new BufferLocation("ALMACEN", params.getAlmacenCapacity());
        cortadora = new ProcessingLocation("CORTADORA", params.getCortadoraCapacity());
        torno = new ProcessingLocation("TORNO", params.getTornoCapacity());
        conveyor2 = new BufferLocation("CONVEYOR_2", params.getConveyor2Capacity());
        fresadora = new ProcessingLocation("FRESADORA", params.getFresadoraCapacity());
        almacen2 = new BufferLocation("ALMACEN_2", params.getAlmacen2Capacity());
        pintura = new ProcessingLocation("PINTURA", params.getPinturaCapacity());
        inspeccion1 = new ProcessingLocation("INSPECCION_1", params.getInspeccion1Capacity());
        inspeccion2 = new ProcessingLocation("INSPECCION_2", params.getInspeccion2Capacity());
        empaque = new ProcessingLocation("EMPAQUE", params.getEmpaqueCapacity());
        embarque = new ProcessingLocation("EMBARQUE", params.getEmbarqueCapacity());

        statistics.registerLocation(conveyor1);
        statistics.registerLocation(almacen);
        statistics.registerLocation(cortadora);
        statistics.registerLocation(torno);
        statistics.registerLocation(conveyor2);
        statistics.registerLocation(fresadora);
        statistics.registerLocation(almacen2);
        statistics.registerLocation(pintura);
        statistics.registerLocation(inspeccion1);
        statistics.registerLocation(inspeccion2);
        statistics.registerLocation(empaque);
        statistics.registerLocation(embarque);
    }

    private void initializeTransportResources() {
        trabajador1 = new TransportResource("TRABAJADOR_1");
        trabajador2 = new TransportResource("TRABAJADOR_2");
        trabajador3 = new TransportResource("TRABAJADOR_3");
        montacargas = new TransportResource("MONTACARGAS");
        montacargasSec = new TransportResource("MONTACARGAS_");
    }

    private void initializeRandomGenerators() {
        randomGen = new RandomGenerators(params.getBaseRandomSeed());
        randomGen.initialize(
                params.getArrivalMeanTime(),
                params.getConveyor1Time(),
                params.getConveyor2Time(),
                params.getTransportWorkerTime(),
                params.getAlmacenProcessMean(),
                params.getAlmacenProcessStdDev(),
                params.getCortadoraProcessMean(),
                params.getTornoProcessMean(),
                params.getTornoProcessStdDev(),
                params.getFresadoraProcessMean(),
                params.getAlmacen2ProcessMean(),
                params.getAlmacen2ProcessStdDev(),
                params.getPinturaProcessMean(),
                params.getInspeccion1ProcessMean(),
                params.getInspeccion1ProcessStdDev(),
                params.getInspeccion2ProcessMean(),
                params.getEmpaqueProcessMean(),
                params.getEmpaqueProcessStdDev(),
                params.getEmbarqueProcessMean(),
                params.getInspeccion1ToEmpaqueProb()
        );
    }

    public void reset() {
        eventQueue.clear();
        entitiesInTransport.clear();
        allActiveEntities.clear();
        resetTransportResources();
        currentTime = 0;
        running = false;
        paused = false;
        lastRealTime = 0;
        Entity.resetIdCounter();
        statistics.reset();

        conveyor1.resetState();
        almacen.resetState();
        cortadora.resetState();
        torno.resetState();
        conveyor2.resetState();
        fresadora.resetState();
        almacen2.resetState();
        pintura.resetState();
        inspeccion1.resetState();
        inspeccion2.resetState();
        empaque.resetState();
        embarque.resetState();

        initializeRandomGenerators();
    }

    private void resetTransportResources() {
        trabajador1.reset();
        trabajador2.reset();
        trabajador3.reset();
        montacargas.reset();
        montacargasSec.reset();
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

    // === MANEJO DE EVENTOS PRINCIPALES ===

    public void handleArrival(double time) {
        Entity entity = new Entity(time);
        statistics.recordArrival();
        allActiveEntities.add(entity);

        conveyor1.enter(entity, time);
        entity.setCurrentLocation("CONVEYOR_1");

        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            scheduleEvent(new ArrivalEvent(nextArrival));
        }

        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "CONVEYOR_1"));
    }

    public void handleTransportEnd(Entity entity, String destinationName, double time) {
        entitiesInTransport.remove(entity);
        entity.endTransit();

        switch (destinationName) {
            case "ALMACEN": arriveAtAlmacen(entity, time); break;
            case "CORTADORA": arriveAtCortadora(entity, time); break;
            case "TORNO": arriveAtTorno(entity, time); break;
            case "CONVEYOR_2": arriveAtConveyor2(entity, time); break;
            case "FRESADORA": arriveAtFresadora(entity, time); break;
            case "ALMACEN_2": arriveAtAlmacen2(entity, time); break;
            case "PINTURA": arriveAtPintura(entity, time); break;
            case "INSPECCION_1": arriveAtInspeccion1(entity, time); break;
            case "INSPECCION_2": arriveAtInspeccion2(entity, time); break;
            case "EMPAQUE": arriveAtEmpaque(entity, time); break;
            case "EMBARQUE": arriveAtEmbarque(entity, time); break;
        }
    }

    public void handleTransportResourceAfterArrival(TransportResource resource, double arrivalTime, double returnTime) {
        if (resource == null) return;

        if (returnTime <= 0) {
            handleTransportResourceAvailable(resource, arrivalTime);
        } else {
            scheduleEvent(new ResourceReleaseEvent(arrivalTime + returnTime, resource));
        }
    }

    public void handleTransportResourceAvailable(TransportResource resource, double time) {
        resource.release();

        // Intentar procesar colas cuando un recurso se libera
        if (resource == trabajador1) {
            processTornoQueue(time);
        } else if (resource == trabajador2) {
            processFresadoraExitQueue(time);
        } else if (resource == montacargas) {
            processAlmacen2ExitQueue(time);
        } else if (resource == montacargasSec) {
            processPinturaExitQueue(time);
        } else if (resource == trabajador3) {
            processEmpaqueExitQueue(time);
        }
    }

    public void handleProcessEnd(Entity entity, String locationName, double time) {
        switch (locationName) {
            case "CONVEYOR_1": finishConveyor1(entity, time); break;
            case "ALMACEN": finishAlmacen(entity, time); break;
            case "CORTADORA": finishCortadora(entity, time); break;
            case "TORNO": finishTorno(entity, time); break;
            case "CONVEYOR_2": finishConveyor2(entity, time); break;
            case "FRESADORA": finishFresadora(entity, time); break;
            case "ALMACEN_2": finishAlmacen2(entity, time); break;
            case "PINTURA": finishPintura(entity, time); break;
            case "INSPECCION_1": finishInspeccion1(entity, time); break;
            case "INSPECCION_2": finishInspeccion2(entity, time); break;
            case "EMPAQUE": finishEmpaque(entity, time); break;
            case "EMBARQUE": finishEmbarque(entity, time); break;
        }
    }

    // === MÉTODOS FINISH (12 métodos) - CORREGIDOS ===

    private void finishConveyor1(Entity entity, double time) {
        // Entidad NO sale hasta que ALMACEN tenga capacidad
        if (almacen.canEnter()) {
            almacen.reserveCapacity();
            conveyor1.exit(entity, time);
            double transportTime = 4.0;
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN");
            entitiesInTransport.add(entity);
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN"));
        } else {
            // Reintentar más tarde - la entidad permanece en CONVEYOR_1
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CONVEYOR_1"));
        }
    }

    private void finishAlmacen(Entity entity, double time) {
        // Entidad NO sale hasta que CORTADORA tenga capacidad
        if (cortadora.canEnter()) {
            cortadora.reserveCapacity();
            almacen.exit(entity, time);
            double transportTime = 3.0;
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "CORTADORA");
            entitiesInTransport.add(entity);
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CORTADORA"));
        } else {
            // Reintentar más tarde - la entidad permanece en ALMACEN (BLOQUEADA)
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "ALMACEN"));
        }
    }

    private void finishCortadora(Entity entity, double time) {
        // SPLIT: 1 barra → 2 piezas
        // La barra original NO sale hasta que ambas piezas puedan moverse

        // Verificar si TORNO tiene capacidad para al menos 1 pieza
        if (torno.canEnter() && trabajador1.isAvailable()) {
            // Hacer SPLIT
            cortadora.exit(entity, time);

            Entity pieza1 = entity;
            Entity pieza2 = new Entity(time);
            allActiveEntities.add(pieza2);
            statistics.recordArrival();

            // Mover pieza1
            torno.reserveCapacity();
            trabajador1.occupy();
            pieza1.setBlocked(false, time);
            startTransportWithResource(pieza1, time, R1_TRAVEL_TIME, R1_TRAVEL_TIME, "TORNO", trabajador1);

            // Agregar pieza2 a la cola de TORNO
            torno.addToQueue(pieza2);
            pieza2.setBlocked(true, time);

            // Procesar cola de CORTADORA
            processCortadoraQueue(time);
        } else {
            // La barra permanece en CORTADORA bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CORTADORA"));
        }
    }

    private void finishTorno(Entity entity, double time) {
        // Entidad NO sale hasta que pueda moverse a CONVEYOR_2
        torno.exit(entity, time);
        double transportTime = 3.0;
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "CONVEYOR_2");
        entitiesInTransport.add(entity);
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CONVEYOR_2"));

        // Procesar cola de TORNO
        processTornoQueue(time);
    }

    private void finishConveyor2(Entity entity, double time) {
        // Entidad NO sale hasta que FRESADORA tenga capacidad
        if (fresadora.canEnter()) {
            fresadora.reserveCapacity();
            conveyor2.exit(entity, time);
            double transportTime = 4.0;
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "FRESADORA");
            entitiesInTransport.add(entity);
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "FRESADORA"));
        } else {
            // Reintentar más tarde - la entidad permanece en CONVEYOR_2
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CONVEYOR_2"));
        }
    }

    private void finishFresadora(Entity entity, double time) {
        // Entidad NO sale hasta que ALMACEN_2 tenga capacidad Y trabajador2 esté disponible
        if (almacen2.canEnter() && trabajador2.isAvailable()) {
            almacen2.reserveCapacity();
            fresadora.exit(entity, time);
            entity.setBlocked(false, time);
            startTransportWithResource(entity, time, R2_TRAVEL_TIME, R2_TRAVEL_TIME, "ALMACEN_2", trabajador2);

            // Procesar cola de FRESADORA
            processFresadoraQueue(time);
        } else {
            // La entidad permanece en FRESADORA bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "FRESADORA"));
        }
    }

    private void finishAlmacen2(Entity entity, double time) {
        // Entidad NO sale hasta que PINTURA tenga capacidad Y montacargas esté disponible
        if (pintura.canEnter() && montacargas.isAvailable()) {
            pintura.reserveCapacity();
            almacen2.exit(entity, time);
            entity.setBlocked(false, time);
            startTransportWithResource(entity, time, R3_TRAVEL_TIME, R3_TRAVEL_TIME, "PINTURA", montacargas);

            // Procesar cola de ALMACEN_2
            processAlmacen2Queue(time);
        } else {
            // La entidad permanece en ALMACEN_2 bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "ALMACEN_2"));
        }
    }

    private void finishPintura(Entity entity, double time) {
        // Entidad NO sale hasta que INSPECCION_1 tenga capacidad Y montacargasSec esté disponible
        if (inspeccion1.canEnter() && montacargasSec.isAvailable()) {
            inspeccion1.reserveCapacity();
            pintura.exit(entity, time);
            entity.setBlocked(false, time);
            startTransportWithResource(entity, time, R4_TRAVEL_TIME, R4_TRAVEL_TIME, "INSPECCION_1", montacargasSec);

            // Procesar cola de PINTURA
            processPinturaQueue(time);
        } else {
            // La entidad permanece en PINTURA bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "PINTURA"));
        }
    }

    private void finishInspeccion1(Entity entity, double time) {
        // CRÍTICO: Decidir el routing UNA SOLA VEZ y almacenarlo en la entidad
        // Esto evita que se generen múltiples números aleatorios en reintentos
        if (entity.getRoutingDestination() == null) {
            boolean goToEmpaque = randomGen.routeToEmpaqueFromInspeccion1();
            String destination = goToEmpaque ? "EMPAQUE" : "INSPECCION_2";
            entity.setRoutingDestination(destination);
        }

        String destination = entity.getRoutingDestination();

        if ("EMPAQUE".equals(destination)) {
            // 80%: INSPECCION_1 → EMPAQUE
            if (empaque.canEnter()) {
                empaque.reserveCapacity();
                inspeccion1.exit(entity, time);
                entity.setBlocked(false, time);
                entity.setRoutingDestination(null); // Limpiar para futuros usos
                double transportTime = 4.0;
                entity.addTransportTime(transportTime);
                entity.startTransit(time, transportTime, "EMPAQUE");
                entitiesInTransport.add(entity);
                scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE"));

                // Procesar cola de INSPECCION_1
                processInspeccion1Queue(time);
            } else {
                // La entidad permanece en INSPECCION_1 bloqueada
                // NO limpiamos routingDestination para mantener la decisión
                entity.setBlocked(true, time);
                entity.addWaitTime(0.5);
                scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_1"));
            }
        } else {
            // 20%: INSPECCION_1 → INSPECCION_2
            if (inspeccion2.canEnter()) {
                inspeccion2.reserveCapacity();
                inspeccion1.exit(entity, time);
                entity.setBlocked(false, time);
                entity.setRoutingDestination(null); // Limpiar para futuros usos
                double transportTime = 4.0;
                entity.addTransportTime(transportTime);
                entity.startTransit(time, transportTime, "INSPECCION_2");
                entitiesInTransport.add(entity);
                scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION_2"));

                // Procesar cola de INSPECCION_1
                processInspeccion1Queue(time);
            } else {
                // La entidad permanece en INSPECCION_1 bloqueada
                // NO limpiamos routingDestination para mantener la decisión
                entity.setBlocked(true, time);
                entity.addWaitTime(0.5);
                scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_1"));
            }
        }
    }

    private void finishInspeccion2(Entity entity, double time) {
        // INSPECCION_2 → EMPAQUE
        if (empaque.canEnter()) {
            empaque.reserveCapacity();
            inspeccion2.exit(entity, time);
            entity.setBlocked(false, time);
            double transportTime = 3.0;
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "EMPAQUE");
            entitiesInTransport.add(entity);
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE"));

            // Procesar cola de INSPECCION_2
            processInspeccion2Queue(time);
        } else {
            // La entidad permanece en INSPECCION_2 bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "INSPECCION_2"));
        }
    }

    private void finishEmpaque(Entity entity, double time) {
        // Entidad NO sale hasta que EMBARQUE tenga capacidad Y trabajador3 esté disponible
        if (embarque.canEnter() && trabajador3.isAvailable()) {
            embarque.reserveCapacity();
            empaque.exit(entity, time);
            entity.setBlocked(false, time);
            startTransportWithResource(entity, time, R5_TRAVEL_TIME, R5_TRAVEL_TIME, "EMBARQUE", trabajador3);

            // Procesar cola de EMPAQUE
            processEmpaqueQueue(time);
        } else {
            // La entidad permanece en EMPAQUE bloqueada
            entity.setBlocked(true, time);
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "EMPAQUE"));
        }
    }

    private void finishEmbarque(Entity entity, double time) {
        embarque.exit(entity, time);
        double transportTime = 3.0;
        entity.addTransportTime(transportTime);
        double exitTime = time + transportTime;
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, exitTime);

        // Procesar cola de EMBARQUE
        processEmbarqueQueue(time);
    }

    // === MÉTODOS ARRIVE AT (11 métodos) ===

    private void arriveAtAlmacen(Entity entity, double time) {
        almacen.commitReservedCapacity();
        almacen.enter(entity, time);
        entity.setCurrentLocation("ALMACEN");
        double processTime = randomGen.nextAlmacenProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "ALMACEN"));
    }

    private void arriveAtCortadora(Entity entity, double time) {
        cortadora.commitReservedCapacity();
        cortadora.enter(entity, time);
        entity.setCurrentLocation("CORTADORA");
        double processTime = randomGen.nextCortadoraProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "CORTADORA"));
    }

    private void arriveAtTorno(Entity entity, double time) {
        torno.commitReservedCapacity();
        torno.enter(entity, time);
        entity.setCurrentLocation("TORNO");
        double processTime = randomGen.nextTornoProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "TORNO"));
    }

    private void arriveAtConveyor2(Entity entity, double time) {
        conveyor2.enter(entity, time);
        entity.setCurrentLocation("CONVEYOR_2");
        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "CONVEYOR_2"));
    }

    private void arriveAtFresadora(Entity entity, double time) {
        fresadora.commitReservedCapacity();
        fresadora.enter(entity, time);
        entity.setCurrentLocation("FRESADORA");
        double processTime = randomGen.nextFresadoraProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "FRESADORA"));
    }

    private void arriveAtAlmacen2(Entity entity, double time) {
        almacen2.commitReservedCapacity();
        almacen2.enter(entity, time);
        entity.setCurrentLocation("ALMACEN_2");
        double processTime = randomGen.nextAlmacen2Process();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "ALMACEN_2"));
    }

    private void arriveAtPintura(Entity entity, double time) {
        pintura.commitReservedCapacity();
        pintura.enter(entity, time);
        entity.setCurrentLocation("PINTURA");
        double processTime = randomGen.nextPinturaProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "PINTURA"));
    }

    private void arriveAtInspeccion1(Entity entity, double time) {
        inspeccion1.commitReservedCapacity();
        inspeccion1.enter(entity, time);
        entity.setCurrentLocation("INSPECCION_1");
        double processTime = randomGen.nextInspeccion1Process();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "INSPECCION_1"));
    }

    private void arriveAtInspeccion2(Entity entity, double time) {
        inspeccion2.commitReservedCapacity();
        inspeccion2.enter(entity, time);
        entity.setCurrentLocation("INSPECCION_2");
        double processTime = randomGen.nextInspeccion2Process();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "INSPECCION_2"));
    }

    private void arriveAtEmpaque(Entity entity, double time) {
        empaque.commitReservedCapacity();
        empaque.enter(entity, time);
        entity.setCurrentLocation("EMPAQUE");
        double processTime = randomGen.nextEmpaqueProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "EMPAQUE"));
    }

    private void arriveAtEmbarque(Entity entity, double time) {
        embarque.commitReservedCapacity();
        embarque.enter(entity, time);
        entity.setCurrentLocation("EMBARQUE");
        double processTime = randomGen.nextEmbarqueProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "EMBARQUE"));
    }

    // === MÉTODOS PROCESS QUEUE (corregidos) ===

    private void processCortadoraQueue(double time) {
        while (cortadora.canEnter() && cortadora.hasQueuedEntities()) {
            Entity nextEntity = cortadora.pollFromQueue();
            if (nextEntity != null) {
                cortadora.reserveCapacity();
                double transportTime = 3.0;
                nextEntity.addTransportTime(transportTime);
                nextEntity.setBlocked(false, time);
                nextEntity.startTransit(time, transportTime, "CORTADORA");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "CORTADORA"));
            }
        }
    }

    private void processTornoQueue(double time) {
        while (torno.canEnter() && torno.hasQueuedEntities() && trabajador1.isAvailable()) {
            Entity nextEntity = torno.pollFromQueue();
            if (nextEntity != null) {
                torno.reserveCapacity();
                trabajador1.occupy();
                nextEntity.setBlocked(false, time);
                startTransportWithResource(nextEntity, time, R1_TRAVEL_TIME, R1_TRAVEL_TIME, "TORNO", trabajador1);
            }
        }
    }

    private void processFresadoraQueue(double time) {
        while (fresadora.canEnter() && fresadora.hasQueuedEntities()) {
            Entity nextEntity = fresadora.pollFromQueue();
            if (nextEntity != null) {
                fresadora.reserveCapacity();
                double transportTime = 4.0;
                nextEntity.addTransportTime(transportTime);
                nextEntity.setBlocked(false, time);
                nextEntity.startTransit(time, transportTime, "FRESADORA");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "FRESADORA"));
            }
        }
    }

    private void processFresadoraExitQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en FRESADORA
    }

    private void processAlmacen2Queue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de ALMACEN_2
    }

    private void processAlmacen2ExitQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en ALMACEN_2
    }

    private void processPinturaQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de PINTURA
    }

    private void processPinturaExitQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en PINTURA
    }

    private void processInspeccion1Queue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de INSPECCION_1
    }

    private void processInspeccion2Queue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de INSPECCION_2
    }

    private void processEmpaqueQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de EMPAQUE
    }

    private void processEmpaqueExitQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades bloqueadas en EMPAQUE
    }

    private void processEmbarqueQueue(double time) {
        // Este método se puede implementar si es necesario procesar entidades en cola de EMBARQUE
    }

    // === MÉTODOS UTILITARIOS ===
    
    private void scheduleEvent(Event event) {
        eventQueue.add(event);
    }

    private void startTransport(Entity entity, double time, double travelTime, String destination) {
        entity.addTransportTime(travelTime);
        entity.startTransit(time, travelTime, destination);
        entitiesInTransport.add(entity);
        scheduleEvent(new TransportEndEvent(time + travelTime, entity, destination));
    }

    private void startTransportWithResource(Entity entity, double time, double travelTime, double returnTime, String destination, TransportResource resource) {
        resource.occupy();
        entity.addTransportTime(travelTime);
        entity.startTransit(time, travelTime, destination);
        entitiesInTransport.add(entity);
        scheduleEvent(new TransportEndEvent(time + travelTime, entity, destination, resource, returnTime));
    }

    // === GETTERS ===
    
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
            case "CONVEYOR_1": return conveyor1;
            case "ALMACEN": return almacen;
            case "CORTADORA": return cortadora;
            case "TORNO": return torno;
            case "CONVEYOR_2": return conveyor2;
            case "FRESADORA": return fresadora;
            case "ALMACEN_2": return almacen2;
            case "PINTURA": return pintura;
            case "INSPECCION_1": return inspeccion1;
            case "INSPECCION_2": return inspeccion2;
            case "EMPAQUE": return empaque;
            case "EMBARQUE": return embarque;
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
