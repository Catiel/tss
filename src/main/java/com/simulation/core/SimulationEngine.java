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

    // 12 Locaciones del sistema Multi-Engrane (según ProModel)
    private BufferLocation conveyor1;      // CONVERYOR_1 - capacidad infinita
    private BufferLocation almacen;        // ALMACEN - capacidad 10
    private ProcessingLocation cortadora;  // CORTADORA - capacidad 1
    private ProcessingLocation torno;      // TORNO - capacidad 2
    private BufferLocation conveyor2;      // CONVERYOR_2 - capacidad infinita
    private ProcessingLocation fresadora;  // FRESADORA - capacidad 2
    private BufferLocation almacen2;       // ALMACEN_2 - capacidad 10
    private ProcessingLocation pintura;    // PINTURA - capacidad 4
    private ProcessingLocation inspeccion1;// INSPECCION_1 - capacidad 2
    private ProcessingLocation inspeccion2;// INSPECCION_2 - capacidad 1
    private ProcessingLocation empaque;    // EMPAQUE - capacidad 1
    private ProcessingLocation embarque;   // EMBARQUE - capacidad 3

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
    private Deque<Entity> blockedInConveyor2; // Entidades esperando en CONVEYOR_2

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
        this.blockedInConveyor2 = new ArrayDeque<>();
        
        this.currentTime = 0;
        this.running = false;
        this.paused = false;

        initializeLocations();
        initializeRandomGenerators();
    }

    private void initializeLocations() {
        // 12 Locaciones según ProModel
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
        
        blockedAfterAlmacen.clear();
        blockedAfterCortadora.clear();
        blockedAfterTorno.clear();
        blockedAfterFresadora.clear();
        blockedAfterAlmacen2.clear();
        blockedAfterPintura.clear();
        blockedAfterInspeccion1.clear();
        blockedAfterInspeccion2.clear();
        blockedAfterEmpaque.clear();
        blockedInConveyor2.clear();
        
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
        
        // Según ProModel: BARRA_DE_ACERO llega a CONVEYOR_1
        conveyor1.enter(entity, time);
        entity.setCurrentLocation("CONVEYOR_1");
        
        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            scheduleEvent(new ArrivalEvent(nextArrival));
        }
        
        // CONVEYOR_1 NO tiene tiempo de proceso - pasa inmediatamente a salir
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

    // === MÉTODOS FINISH (12 métodos) ===
    
    private void finishConveyor1(Entity entity, double time) {
        // ProModel: BARRA_DE_ACERO sale de CONVEYOR_1 hacia ALMACEN
        // Move For 4 min - este es el tiempo de TRANSPORTE, no de proceso
        if (almacen.canEnter()) {
            almacen.reserveCapacity();
            conveyor1.exit(entity, time);
            
            double transportTime = 4.0; // Según ProModel: Move For 4 min
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "ALMACEN");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "ALMACEN"));
        } else {
            // ALMACEN lleno: re-intentar después
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "CONVEYOR_1"));
        }
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
        // ProModel: PIEZA_TORNEADA entra a CONVEYOR_2
        conveyor2.enter(entity, time);
        entity.setCurrentLocation("CONVEYOR_2");
        
        // CONVEYOR_2 NO tiene tiempo de proceso - pasa inmediatamente a salir
        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "CONVEYOR_2"));
    }

    private void arriveAtFresadora(Entity entity, double time) {
        fresadora.commitReservedCapacity();
        fresadora.enter(entity, time);
        entity.setCurrentLocation("FRESADORA");
        
        double processTime = randomGen.nextFresadoraProcess();
        entity.addProcessTime(processTime);
        scheduleEvent(new ProcessEndEvent(time + processTime, entity, "FRESADORA"));
        
        // Intentar liberar entidades bloqueadas en CONVEYOR_2
        releaseBlockedInConveyor2(time);
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
            
            double transportTime = 3.0; // Según ProModel: MOVE FOR 3 min
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
        
        // ProModel: MOVE FOR 3 min hacia CONVEYOR_2
        double transportTime = 3.0; // Según ProModel: MOVE FOR 3 min
        entity.addTransportTime(transportTime);
        entity.startTransit(time, transportTime, "CONVEYOR_2");
        entitiesInTransport.add(entity);
        
        scheduleEvent(new TransportEndEvent(time + transportTime, entity, "CONVEYOR_2"));
        processTornoQueue(time);
        releaseBlockedAfterTorno(time);
    }
    
    private void finishConveyor2(Entity entity, double time) {
        // ProModel: PIEZA_TORNEADA sale de CONVEYOR_2 hacia FRESADORA
        // Move For 4 min - este es el tiempo de TRANSPORTE, no de proceso
        if (fresadora.canEnter()) {
            fresadora.reserveCapacity();
            conveyor2.exit(entity, time);
            
            double transportTime = 4.0; // Según ProModel: Move For 4 min
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "FRESADORA");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "FRESADORA"));
        } else {
            // FRESADORA llena: la entidad se queda esperando EN el conveyor
            // No hacemos exit() para que la entidad permanezca en CONVEYOR_2
            // Agregamos a una cola de bloqueados que se procesará cuando FRESADORA se libere
            blockedInConveyor2.addLast(entity);
            entity.setBlocked(true, time);
        }
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
            releaseBlockedInConveyor2(time); // Liberar entidades esperando en CONVEYOR_2
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
            // INSPECCION_1 → EMPAQUE: MOVE FOR 4 min (80%)
            tryMoveToEmpaqueFromInspeccion1(entity, time);
        } else {
            // INSPECCION_1 → INSPECCION_2: MOVE FOR 4 min (20%)
            tryMoveToInspeccion2FromInspeccion1(entity, time);
        }
        
        processInspeccion1Queue(time);
        releaseBlockedAfterInspeccion1(time);
    }

    private void finishInspeccion2(Entity entity, double time) {
        // INSPECCION_2 → EMPAQUE: MOVE FOR 3 min (según ProModel)
        inspeccion2.exit(entity, time);
        
        if (empaque.canEnter()) {
            empaque.reserveCapacity();
            
            double transportTime = 3.0; // Tiempo fijo según ProModel
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "EMPAQUE");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE"));
        } else {
            empaque.addToQueue(entity);
        }
        
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
        
        // ProModel: EMBARQUE → EXIT: MOVE FOR 3 min
        double transportTime = 3.0;
        entity.addTransportTime(transportTime);
        
        // Simular el transporte final y luego registrar salida
        double exitTime = time + transportTime;
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, exitTime);
        
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

    private void tryMoveToEmpaqueFromInspeccion1(Entity entity, double time) {
        // INSPECCION_1 → EMPAQUE: MOVE FOR 4 min (según ProModel)
        if (empaque.canEnter()) {
            empaque.reserveCapacity();
            
            double transportTime = 4.0; // Tiempo fijo según ProModel
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "EMPAQUE");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "EMPAQUE"));
        } else {
            empaque.addToQueue(entity);
        }
    }

    private void tryMoveToInspeccion2FromInspeccion1(Entity entity, double time) {
        // INSPECCION_1 → INSPECCION_2: MOVE FOR 4 min (según ProModel)
        if (inspeccion2.canEnter()) {
            inspeccion2.reserveCapacity();
            
            double transportTime = 4.0; // Tiempo fijo según ProModel
            entity.addTransportTime(transportTime);
            entity.startTransit(time, transportTime, "INSPECCION_2");
            entitiesInTransport.add(entity);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, entity, "INSPECCION_2"));
        } else {
            inspeccion2.addToQueue(entity);
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
        // Primero liberar entidades bloqueadas en CONVEYOR_2
        releaseBlockedInConveyor2(time);
        
        // Luego procesar cola normal de FRESADORA
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
    
    private void releaseBlockedInConveyor2(double time) {
        // Liberar entidades que estaban esperando en CONVEYOR_2 para ir a FRESADORA
        while (!blockedInConveyor2.isEmpty() && fresadora.canEnter()) {
            Entity blocked = blockedInConveyor2.removeFirst();
            blocked.setBlocked(false, time);
            
            fresadora.reserveCapacity();
            conveyor2.exit(blocked, time);
            
            double transportTime = 4.0; // Move For 4 min
            blocked.addTransportTime(transportTime);
            blocked.startTransit(time, transportTime, "FRESADORA");
            entitiesInTransport.add(blocked);
            
            scheduleEvent(new TransportEndEvent(time + transportTime, blocked, "FRESADORA"));
        }
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
