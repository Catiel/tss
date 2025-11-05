package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.*;
import com.simulation.statistics.Statistics;
import com.simulation.core.EventTypes.*;
import java.util.*;

/**
 * Motor de simulación Multi-Engrane - Fabricación de engranes con 12 locaciones
 * CONVEYOR_1 → ALMACEN → CORTADORA (SPLIT) → TORNO → CONVEYOR_2 → FRESADORA → 
 * ALMACEN_2 → PINTURA → INSPECCION_1 (routing) → EMPAQUE/INSPECCION_2 → EMBARQUE → EXIT
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

    // 12 Locaciones del sistema Multi-Engrane
    private Location recepcion;
    private BufferLocation almacen;
    private ProcessingLocation cortadora;
    private ProcessingLocation torno;
    private BufferLocation almacen2;
    private ProcessingLocation fresadora;
    private ProcessingLocation pintura;
    private ProcessingLocation inspeccion1;
    private ProcessingLocation inspeccion2;
    private ProcessingLocation empaque;
    private ProcessingLocation embarque;

    private Set<Entity> entitiesInTransport;
    private List<Entity> allActiveEntities;
    
    // 9 Colas de bloqueo
    private Deque<Entity> blockedAfterAlmacen;
    private Deque<Entity> blockedAfterCortadora;
    private Deque<Entity> blockedAfterTorno;
    private Deque<Entity> blockedAfterFresadora;
    private Deque<Entity> blockedAfterAlmacen2;
    private Deque<Entity> blockedAfterPintura;
    private Deque<Entity> blockedAfterInspeccion1;
    private Deque<Entity> blockedAfterInspeccion2;
    private Deque<Entity> blockedAfterEmpaque;

    private static final double VISUAL_TRANSIT_TIME = 0.05;

    public SimulationEngine(SimulationParameters params) {
        this.params = params;
        this.statistics = new Statistics();
        this.eventQueue = new PriorityQueue<>();
        this.entitiesInTransport = new HashSet<>();
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>());
        
        this.blockedAfterAlmacen = new ArrayDeque<>();
        this.blockedAfterCortadora = new ArrayDeque<>();
        this.blockedAfterTorno = new ArrayDeque<>();
        this.blockedAfterFresadora = new ArrayDeque<>();
        this.blockedAfterAlmacen2 = new ArrayDeque<>();
        this.blockedAfterPintura = new ArrayDeque<>();
        this.blockedAfterInspeccion1 = new ArrayDeque<>();
        this.blockedAfterInspeccion2 = new ArrayDeque<>();
        this.blockedAfterEmpaque = new ArrayDeque<>();
        
        this.currentTime = 0;
        this.running = false;
        this.paused = false;

        initializeLocations();
        initializeRandomGenerators();
    }

    private void initializeLocations() {
        recepcion = new Location("RECEPCION", Integer.MAX_VALUE) {};
        almacen = new BufferLocation("ALMACEN", params.getAlmacenCapacity());
        cortadora = new ProcessingLocation("CORTADORA", params.getCortadoraCapacity());
        torno = new ProcessingLocation("TORNO", params.getTornoCapacity());
        almacen2 = new BufferLocation("ALMACEN_2", params.getAlmacen2Capacity());
        fresadora = new ProcessingLocation("FRESADORA", params.getFresadoraCapacity());
        pintura = new ProcessingLocation("PINTURA", params.getPinturaCapacity());
        inspeccion1 = new ProcessingLocation("INSPECCION_1", params.getInspeccion1Capacity());
        inspeccion2 = new ProcessingLocation("INSPECCION_2", params.getInspeccion2Capacity());
        empaque = new ProcessingLocation("EMPAQUE", params.getEmpaqueCapacity());
        embarque = new ProcessingLocation("EMBARQUE", params.getEmbarqueCapacity());

        statistics.registerLocation(recepcion);
        statistics.registerLocation(almacen);
        statistics.registerLocation(cortadora);
        statistics.registerLocation(torno);
        statistics.registerLocation(almacen2);
        statistics.registerLocation(fresadora);
        statistics.registerLocation(pintura);
        statistics.registerLocation(inspeccion1);
        statistics.registerLocation(inspeccion2);
        statistics.registerLocation(empaque);
        statistics.registerLocation(embarque);
    }

    private void initializeRandomGenerators() {
        randomGen = new RandomGenerators(params.getBaseRandomSeed());
        randomGen.initialize(
                params.getArrivalMeanTime(),
                params.getConveyor1Time(),
                params.getConveyor2Time(),
                params.getTransportWorkerTime(),
                params.getAlmacenProcessMean(),
                params.getCortadoraProcessMean(),
                params.getCortadoraProcessStdDev(),
                params.getTornoProcessMean(),
                params.getTornoProcessStdDev(),
                params.getFresadoraProcessMean(),
                params.getFresadoraProcessStdDev(),
                params.getAlmacen2ProcessMean(),
                params.getPinturaProcessMean(),
                params.getInspeccion1ProcessMean(),
                params.getInspeccion1ProcessStdDev(),
                params.getInspeccion2ProcessMean(),
                params.getInspeccion2ProcessStdDev(),
                params.getEmpaqueProcessMean(),
                params.getEmbarqueProcessMean(),
                params.getInspeccion1ToEmpaqueProb()
        );
    }

    public void reset() {
        eventQueue.clear();
        entitiesInTransport.clear();
        allActiveEntities.clear();
        
        blockedAfterAlmacen.clear();
        blockedAfterCortadora.clear();
        blockedAfterTorno.clear();
        blockedAfterFresadora.clear();
        blockedAfterAlmacen2.clear();
        blockedAfterPintura.clear();
        blockedAfterInspeccion1.clear();
        blockedAfterInspeccion2.clear();
        blockedAfterEmpaque.clear();
        
        currentTime = 0;
        running = false;
        paused = false;
        lastRealTime = 0;
        Entity.resetIdCounter();
        statistics.reset();
        
        recepcion.resetState();
        almacen.resetState();
        cortadora.resetState();
        torno.resetState();
        almacen2.resetState();
        fresadora.resetState();
        pintura.resetState();
        inspeccion1.resetState();
        inspeccion2.resetState();
        empaque.resetState();
        embarque.resetState();
        
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
    // === MANEJO DE EVENTOS PRINCIPALES ===
    
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
        
        moveToConveyor1(entity, time);
    }

    private void moveToConveyor1(Entity entity, double time) {
        recepcion.exit(entity, time);
        
        double transportTime = randomGen.nextConveyor1Time();
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "ALMACEN");
        entitiesInTransport.add(entity);
        
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN"));
    }

    public void handleTransportEnd(Entity entity, String destinationName, double time) {
        entitiesInTransport.remove(entity);
        entity.endTransit();

        switch (destinationName) {
            case "ALMACEN": arriveAtAlmacen(entity, time); break;
            case "CORTADORA": arriveAtCortadora(entity, time); break;
            case "TORNO": arriveAtTorno(entity, time); break;
            case "CONVEYOR_2": arriveAtConveyor2End(entity, time); break;
            case "FRESADORA": arriveAtFresadora(entity, time); break;
            case "ALMACEN_2": arriveAtAlmacen2(entity, time); break;
            case "PINTURA": arriveAtPintura(entity, time); break;
            case "INSPECCION_1": arriveAtInspeccion1(entity, time); break;
            case "INSPECCION_2": arriveAtInspeccion2(entity, time); break;
            case "EMPAQUE": arriveAtEmpaque(entity, time); break;
            case "EMBARQUE": arriveAtEmbarque(entity, time); break;
        }
    }

    public void handleProcessEnd(Entity entity, String locationName, double time) {
        switch (locationName) {
            case "ALMACEN": finishAlmacen(entity, time); break;
            case "CORTADORA": finishCortadora(entity, time); break;
            case "TORNO": finishTorno(entity, time); break;
            case "FRESADORA": finishFresadora(entity, time); break;
            case "ALMACEN_2": finishAlmacen2(entity, time); break;
            case "PINTURA": finishPintura(entity, time); break;
            case "INSPECCION_1": finishInspeccion1(entity, time); break;
            case "INSPECCION_2": finishInspeccion2(entity, time); break;
            case "EMPAQUE": finishEmpaque(entity, time); break;
            case "EMBARQUE": finishEmbarque(entity, time); break;
        }
    }

    // === MÉTODOS ARRIVE AT (12 métodos) ===
    
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

    private void arriveAtConveyor2End(Entity entity, double time) {
        if (fresadora.canEnter()) {
            fresadora.reserveCapacity();
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "FRESADORA");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "FRESADORA"));
        } else {
            fresadora.addToQueue(entity);
        }
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

    // === MÉTODOS FINISH (10 métodos) ===
    
    private void finishAlmacen(Entity entity, double time) {
        if (cortadora.canEnter()) {
            cortadora.reserveCapacity();
            almacen.exit(entity, time);
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "CORTADORA");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CORTADORA"));
            processAlmacenQueue(time);
        } else {
            blockedAfterAlmacen.addLast(entity);
            entity.setBlocked(true, time);
        }
    }

    private void finishCortadora(Entity entity, double time) {
        cortadora.exit(entity, time);
        
        // SPLIT: 1 barra → 2 piezas
        Entity pieza1 = entity;
        Entity pieza2 = new Entity(time);
        allActiveEntities.add(pieza2);
        statistics.recordArrival();
        
        tryMoveToTorno(pieza1, time);
        tryMoveToTorno(pieza2, time);
        
        processCortadoraQueue(time);
        releaseBlockedAfterCortadora(time);
    }

    private void finishTorno(Entity entity, double time) {
        torno.exit(entity, time);
        
        double transportTime = randomGen.nextConveyor2Time();
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "CONVEYOR_2");
        entitiesInTransport.add(entity);
        
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CONVEYOR_2"));
        processTornoQueue(time);
        releaseBlockedAfterTorno(time);
    }

    private void finishFresadora(Entity entity, double time) {
        if (almacen2.canEnter()) {
            almacen2.reserveCapacity();
            fresadora.exit(entity, time);
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN_2");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN_2"));
            processFresadoraQueue(time);
        } else {
            blockedAfterFresadora.addLast(entity);
            entity.setBlocked(true, time);
        }
    }

    private void finishAlmacen2(Entity entity, double time) {
        if (pintura.canEnter()) {
            pintura.reserveCapacity();
            almacen2.exit(entity, time);
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "PINTURA");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "PINTURA"));
            processAlmacen2Queue(time);
        } else {
            blockedAfterAlmacen2.addLast(entity);
            entity.setBlocked(true, time);
        }
    }

    private void finishPintura(Entity entity, double time) {
        if (inspeccion1.canEnter()) {
            inspeccion1.reserveCapacity();
            pintura.exit(entity, time);
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "INSPECCION_1");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION_1"));
            processPinturaQueue(time);
        } else {
            blockedAfterPintura.addLast(entity);
            entity.setBlocked(true, time);
        }
    }

    private void finishInspeccion1(Entity entity, double time) {
        inspeccion1.exit(entity, time);
        
        boolean goToEmpaque = randomGen.routeToEmpaqueFromInspeccion1();
        
        if (goToEmpaque) {
            tryMoveToEmpaque(entity, time);
        } else {
            tryMoveToInspeccion2(entity, time);
        }
        
        processInspeccion1Queue(time);
        releaseBlockedAfterInspeccion1(time);
    }

    private void finishInspeccion2(Entity entity, double time) {
        inspeccion2.exit(entity, time);
        tryMoveToEmpaque(entity, time);
        processInspeccion2Queue(time);
        releaseBlockedAfterInspeccion2(time);
    }

    private void finishEmpaque(Entity entity, double time) {
        if (embarque.canEnter()) {
            embarque.reserveCapacity();
            empaque.exit(entity, time);
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "EMBARQUE");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMBARQUE"));
            processEmpaqueQueue(time);
        } else {
            blockedAfterEmpaque.addLast(entity);
            entity.setBlocked(true, time);
        }
    }

    private void finishEmbarque(Entity entity, double time) {
        embarque.exit(entity, time);
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, time);
        processEmbarqueQueue(time);
        releaseBlockedAfterEmbarque(time);
    }

    // === MÉTODOS TRY MOVE ===
    
    private void tryMoveToCortadora(Entity entity, double time) {
        if (cortadora.canEnter()) {
            cortadora.reserveCapacity();
            
            String currentLoc = entity.getCurrentLocation();
            if ("ALMACEN".equals(currentLoc)) {
                almacen.exit(entity, time);
            }
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "CORTADORA");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CORTADORA"));
        } else {
            cortadora.addToQueue(entity);
        }
    }

    private void tryMoveToTorno(Entity entity, double time) {
        if (torno.canEnter()) {
            torno.reserveCapacity();
            
            String currentLoc = entity.getCurrentLocation();
            if ("CORTADORA".equals(currentLoc)) {
                cortadora.exit(entity, time);
            }
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "TORNO");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "TORNO"));
        } else {
            torno.addToQueue(entity);
        }
    }

    private void tryMoveToEmpaque(Entity entity, double time) {
        if (empaque.canEnter()) {
            empaque.reserveCapacity();
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "EMPAQUE");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE"));
        } else {
            empaque.addToQueue(entity);
        }
    }

    private void tryMoveToInspeccion2(Entity entity, double time) {
        if (inspeccion2.canEnter()) {
            inspeccion2.reserveCapacity();
            
            double transportTime = randomGen.nextTransportWorkerTime();
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "INSPECCION_2");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION_2"));
        } else {
            inspeccion2.addToQueue(entity);
        }
    }

    // === MÉTODOS PROCESS QUEUE ===
    
    private void processAlmacenQueue(double time) {
        while (almacen.canEnter() && almacen.hasQueuedEntities()) {
            Entity nextEntity = almacen.pollFromQueue();
            if (nextEntity != null) {
                tryMoveToCortadora(nextEntity, time);
            }
        }
    }

    private void processCortadoraQueue(double time) {
        while (cortadora.canEnter() && cortadora.hasQueuedEntities()) {
            Entity nextEntity = cortadora.pollFromQueue();
            if (nextEntity != null) {
                tryMoveToCortadora(nextEntity, time);
            }
        }
    }

    private void processTornoQueue(double time) {
        while (torno.canEnter() && torno.hasQueuedEntities()) {
            Entity nextEntity = torno.pollFromQueue();
            if (nextEntity != null) {
                tryMoveToTorno(nextEntity, time);
            }
        }
    }

    private void processFresadoraQueue(double time) {
        while (fresadora.canEnter() && fresadora.hasQueuedEntities()) {
            Entity nextEntity = fresadora.pollFromQueue();
            if (nextEntity != null) {
                fresadora.reserveCapacity();
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "FRESADORA");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "FRESADORA"));
            }
        }
    }

    private void processAlmacen2Queue(double time) {
        while (pintura.canEnter() && almacen2.hasQueuedEntities()) {
            Entity nextEntity = almacen2.pollFromQueue();
            if (nextEntity != null) {
                pintura.reserveCapacity();
                almacen2.exit(nextEntity, time);
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "PINTURA");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "PINTURA"));
            }
        }
    }

    private void processPinturaQueue(double time) {
        while (inspeccion1.canEnter() && pintura.hasQueuedEntities()) {
            Entity nextEntity = pintura.pollFromQueue();
            if (nextEntity != null) {
                inspeccion1.reserveCapacity();
                pintura.exit(nextEntity, time);
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "INSPECCION_1");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "INSPECCION_1"));
            }
        }
    }

    private void processInspeccion1Queue(double time) {
        while (inspeccion1.canEnter() && inspeccion1.hasQueuedEntities()) {
            Entity nextEntity = inspeccion1.pollFromQueue();
            if (nextEntity != null) {
                inspeccion1.reserveCapacity();
                pintura.exit(nextEntity, time);
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "INSPECCION_1");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "INSPECCION_1"));
            }
        }
    }

    private void processInspeccion2Queue(double time) {
        while (inspeccion2.canEnter() && inspeccion2.hasQueuedEntities()) {
            Entity nextEntity = inspeccion2.pollFromQueue();
            if (nextEntity != null) {
                inspeccion2.reserveCapacity();
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "INSPECCION_2");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "INSPECCION_2"));
            }
        }
    }

    private void processEmpaqueQueue(double time) {
        while (empaque.canEnter() && empaque.hasQueuedEntities()) {
            Entity nextEntity = empaque.pollFromQueue();
            if (nextEntity != null) {
                tryMoveToEmpaque(nextEntity, time);
            }
        }
    }

    private void processEmbarqueQueue(double time) {
        while (embarque.canEnter() && embarque.hasQueuedEntities()) {
            Entity nextEntity = embarque.pollFromQueue();
            if (nextEntity != null) {
                embarque.reserveCapacity();
                empaque.exit(nextEntity, time);
                double transportTime = randomGen.nextTransportWorkerTime();
                nextEntity.addTransportTime(transportTime);
                nextEntity.startTransit(time, transportTime, "EMBARQUE");
                entitiesInTransport.add(nextEntity);
                scheduleEvent(new TransportEndEvent(time + transportTime, nextEntity, "EMBARQUE"));
            }
        }
    }

    // === MÉTODOS RELEASE BLOCKED ===
    
    private void releaseBlockedAfterCortadora(double time) {
        while (!blockedAfterCortadora.isEmpty() && torno.canEnter()) {
            Entity blocked = blockedAfterCortadora.removeFirst();
            tryMoveToTorno(blocked, time);
        }
    }

    private void releaseBlockedAfterTorno(double time) {
        // No hay bloqueo directo después de TORNO (va a conveyor)
    }

    private void releaseBlockedAfterInspeccion1(double time) {
        // No hay bloqueo directo (routing decide destino)
    }

    private void releaseBlockedAfterInspeccion2(double time) {
        while (!blockedAfterInspeccion2.isEmpty() && empaque.canEnter()) {
            Entity blocked = blockedAfterInspeccion2.removeFirst();
            tryMoveToEmpaque(blocked, time);
        }
    }

    private void releaseBlockedAfterEmbarque(double time) {
        // No hay bloqueo después de EMBARQUE (exit)
    }

    // === MÉTODOS UTILITARIOS ===
    
    private void scheduleEvent(Event event) {
        eventQueue.add(event);
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
            case "RECEPCION": return recepcion;
            case "ALMACEN": return almacen;
            case "CORTADORA": return cortadora;
            case "TORNO": return torno;
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
