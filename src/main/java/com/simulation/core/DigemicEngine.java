package com.simulation.core;
import com.simulation.config.SimulationParameters;
import com.simulation.core.EventTypes.*;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.BufferLocation;
import com.simulation.resources.Location;
import com.simulation.resources.ProcessingLocation;
import com.simulation.statistics.Statistics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Motor de simulación DIGEMIC - Sistema de expedición de pasaportes.
 * Modelo con 6 locaciones: Entrada, Zona_Formas, Sala_Sillas, Sala_De_Pie, Servidor_1, Servidor_2.
 */
public class DigemicEngine {

    private final SimulationParameters params;
    private RandomGenerators randomGen;
    private final Statistics statistics;
    private final PriorityQueue<Event> eventQueue;
    private volatile double currentTime;
    private volatile boolean running;
    private volatile boolean paused;
    private volatile double simulationSpeed = 100.0;
    private volatile long lastRealTime = 0L;

    // 6 Locaciones del sistema DIGEMIC
    private BufferLocation entrada;
    private BufferLocation zonaFormas;
    private BufferLocation salaSillas;
    private BufferLocation salaDePie;
    private ProcessingLocation servidor1;
    private ProcessingLocation servidor2;

    private final Set<Entity> entitiesInTransport;
    private final List<Entity> allActiveEntities;

    // Contadores de pasaportes por servidor (cada 10 → pausa)
    private int pasaportesAtendidosServidor1 = 0;
    private int pasaportesAtendidosServidor2 = 0;
    private boolean servidor1Paused = false;
    private boolean servidor2Paused = false;

    public DigemicEngine(SimulationParameters params) {
        this.params = params;
        this.statistics = new Statistics();
        this.eventQueue = new PriorityQueue<>();
        this.entitiesInTransport = new HashSet<>();
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>());
        this.currentTime = 0.0;
        this.running = false;
        this.paused = false;
        initializeLocations();
        initializeRandomGenerators();
    }

    private void initializeLocations() {
        entrada = new BufferLocation("ENTRADA", params.getEntradaCapacity());
        zonaFormas = new BufferLocation("ZONA_FORMAS", params.getZonaFormasCapacity());
        salaSillas = new BufferLocation("SALA_SILLAS", params.getSalaSillasCapacity());
        salaDePie = new BufferLocation("SALA_DE_PIE", params.getSalaDePieCapacity());
        servidor1 = new ProcessingLocation("SERVIDOR_1", params.getServidor1Capacity());
        servidor2 = new ProcessingLocation("SERVIDOR_2", params.getServidor2Capacity());

        statistics.registerLocation(entrada);
        statistics.registerLocation(zonaFormas);
        statistics.registerLocation(salaSillas);
        statistics.registerLocation(salaDePie);
        statistics.registerLocation(servidor1);
        statistics.registerLocation(servidor2);
    }

    private void initializeRandomGenerators() {
        randomGen = new RandomGenerators(params.getBaseRandomSeed());
        randomGen.initialize(
                params.getArrivalMeanTime(),
                params.getZonaFormasMin(),
                params.getZonaFormasMax(),
                params.getServicioMean(),
                params.getPausaServidorMean(),
                params.getDirectoASalaProb()
        );
    }

    public void reset() {
        eventQueue.clear();
        entitiesInTransport.clear();
        allActiveEntities.clear();
        currentTime = 0.0;
        running = false;
        paused = false;
        lastRealTime = 0L;
        pasaportesAtendidosServidor1 = 0;
        pasaportesAtendidosServidor2 = 0;
        servidor1Paused = false;
        servidor2Paused = false;
        Entity.resetIdCounter();
        statistics.reset();

        entrada.resetState();
        zonaFormas.resetState();
        salaSillas.resetState();
        salaDePie.resetState();
        servidor1.resetState();
        servidor2.resetState();

        statistics.updateWaitingAreaSnapshot(
                salaSillas.getCurrentContent(),
                salaDePie.getCurrentContent()
        );

        initializeRandomGenerators();
    }

    public void initialize() {
        reset();
        lastRealTime = System.currentTimeMillis();
        scheduleEvent(new ArrivalEvent(0.0));
    }

    public void setSimulationSpeed(double minutesPerSecond) {
        if (minutesPerSecond <= 0) {
            throw new IllegalArgumentException("Simulation speed must be positive");
        }
        this.simulationSpeed = minutesPerSecond;
        lastRealTime = System.currentTimeMillis();
    }

    public void run() {
        running = true;
        double endTime = params.getSimulationDurationMinutes();

        while (running) {
            if (eventQueue.isEmpty()) {
                break;
            }

            while (paused && running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    return;
                }
            }

            if (!running) {
                break;
            }

            Event nextEvent = eventQueue.peek();
            if (nextEvent == null) {
                break;
            }

            double targetSimTime = nextEvent.getTime();
            if (targetSimTime > endTime) {
                currentTime = endTime;
                break;
            }

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

            nextEvent.execute(this);
        }

        statistics.finalizeStatistics(endTime);
        currentTime = Math.min(currentTime, endTime);
        running = false;
        paused = false;
    }

    // === MANEJO DE EVENTOS PRINCIPALES ===

    public void handleArrival(double time) {
        Entity entity = new Entity(time);
        statistics.recordArrival();
        allActiveEntities.add(entity);

        entrada.enter(entity, time);
        entity.setCurrentLocation("ENTRADA");

        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            if (nextArrival <= params.getSimulationDurationMinutes()) {
                scheduleEvent(new ArrivalEvent(nextArrival));
            }
        }

        if (entity.getRoutingDestination() == null) {
            boolean directo = randomGen.goDirectoASala();
            entity.setRoutingDestination(directo ? "SALA" : "FORMAS");
        }

        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "ENTRADA"));
    }

    public void handleProcessEnd(Entity entity, String locationName, double time) {
        switch (locationName) {
            case "ENTRADA":
                finishEntrada(entity, time);
                break;
            case "ZONA_FORMAS":
                finishZonaFormas(entity, time);
                break;
            case "SALA_DE_PIE":
                finishSalaDePie(entity, time);
                break;
            case "SALA_SILLAS":
                finishSalaSillas(entity, time);
                break;
            case "SERVIDOR_1":
                finishServidor1(entity, time);
                break;
            case "SERVIDOR_2":
                finishServidor2(entity, time);
                break;
            default:
                break;
        }
    }

    // === MÉTODOS FINISH ===

    private void finishEntrada(Entity entity, double time) {
        String destination = entity.getRoutingDestination();
        entrada.exit(entity, time);

        if ("FORMAS".equals(destination)) {
            zonaFormas.enter(entity, time);
            entity.setCurrentLocation("ZONA_FORMAS");
            double formasTime = randomGen.nextZonaFormasTime();
            entity.addProcessTime(formasTime);
            scheduleEvent(new ProcessEndEvent(time + formasTime, entity, "ZONA_FORMAS"));
        } else {
            tryEnterSala(entity, time);
        }
        updateWaitingAreaSnapshot();
    }

    private void finishZonaFormas(Entity entity, double time) {
        zonaFormas.exit(entity, time);
        entity.setRoutingDestination(null);
        tryEnterSala(entity, time);
    }

    private void tryEnterSala(Entity entity, double time) {
        if (salaSillas.canEnter()) {
            salaSillas.enter(entity, time);
            entity.setCurrentLocation("SALA_SILLAS");
            entity.setBlocked(false, time);
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS"));
        } else {
            salaDePie.enter(entity, time);
            entity.setCurrentLocation("SALA_DE_PIE");
            entity.setBlocked(true, time);
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_DE_PIE"));
        }
        updateWaitingAreaSnapshot();
    }

    private void finishSalaDePie(Entity entity, double time) {
        if (!"SALA_DE_PIE".equals(entity.getCurrentLocation())) {
            return;
        }

        if (salaSillas.canEnter()) {
            salaDePie.exit(entity, time);
            salaSillas.enter(entity, time);
            entity.setCurrentLocation("SALA_SILLAS");
            entity.setBlocked(false, time);
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS"));
            updateWaitingAreaSnapshot();
        }
    }

    private void finishSalaSillas(Entity entity, double time) {
        if (!"SALA_SILLAS".equals(entity.getCurrentLocation())) {
            return;
        }

        if (!servidor1Paused && servidor1.canEnter()) {
            servidor1.reserveCapacity();
            salaSillas.exit(entity, time);
            wakeUpStandingRoom(time);
            arriveAtServidor1(entity, time);
            updateWaitingAreaSnapshot();
        } else if (!servidor2Paused && servidor2.canEnter()) {
            servidor2.reserveCapacity();
            salaSillas.exit(entity, time);
            wakeUpStandingRoom(time);
            arriveAtServidor2(entity, time);
            updateWaitingAreaSnapshot();
        }
    }

    private void arriveAtServidor1(Entity entity, double time) {
        servidor1.commitReservedCapacity();
        servidor1.enter(entity, time);
        entity.setCurrentLocation("SERVIDOR_1");
        double servicioTime = randomGen.nextServicioTime();
        entity.addProcessTime(servicioTime);
        scheduleEvent(new ProcessEndEvent(time + servicioTime, entity, "SERVIDOR_1"));
    }

    private void finishServidor1(Entity entity, double time) {
        pasaportesAtendidosServidor1++;

        if (pasaportesAtendidosServidor1 >= params.getPasaportesPorPausa()) {
            double pausaTime = randomGen.nextPausaServidorTime();
            pasaportesAtendidosServidor1 = 0;
            servidor1Paused = true;
            entity.addProcessTime(pausaTime);
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, entity, "SERVIDOR_1"));
        } else {
            completeServerExit(servidor1, entity, time);
        }
    }

    private void arriveAtServidor2(Entity entity, double time) {
        servidor2.commitReservedCapacity();
        servidor2.enter(entity, time);
        entity.setCurrentLocation("SERVIDOR_2");
        double servicioTime = randomGen.nextServicioTime();
        entity.addProcessTime(servicioTime);
        scheduleEvent(new ProcessEndEvent(time + servicioTime, entity, "SERVIDOR_2"));
    }

    private void finishServidor2(Entity entity, double time) {
        pasaportesAtendidosServidor2++;

        if (pasaportesAtendidosServidor2 >= params.getPasaportesPorPausa()) {
            double pausaTime = randomGen.nextPausaServidorTime();
            pasaportesAtendidosServidor2 = 0;
            servidor2Paused = true;
            entity.addProcessTime(pausaTime);
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, entity, "SERVIDOR_2"));
        } else {
            completeServerExit(servidor2, entity, time);
        }
    }

    public void handleServerPauseEnd(String serverName, Entity entity, double time) {
        if ("SERVIDOR_1".equals(serverName)) {
            servidor1Paused = false;
            completeServerExit(servidor1, entity, time);
        } else if ("SERVIDOR_2".equals(serverName)) {
            servidor2Paused = false;
            completeServerExit(servidor2, entity, time);
        }
    }

    // === MÉTODOS UTILITARIOS ===

    private void scheduleEvent(Event event) {
        eventQueue.add(event);
    }

    private void updateWaitingAreaSnapshot() {
        statistics.updateWaitingAreaSnapshot(
                salaSillas.getCurrentContent(),
                salaDePie.getCurrentContent()
        );
    }

    public double getCurrentTime() {
        return currentTime;
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        running = false;
        paused = false;
    }

    public void pause() {
        paused = true;
    }

    public void resume() {
        lastRealTime = System.currentTimeMillis();
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
            case "ENTRADA":
                return entrada;
            case "ZONA_FORMAS":
                return zonaFormas;
            case "SALA_SILLAS":
                return salaSillas;
            case "SALA_DE_PIE":
                return salaDePie;
            case "SERVIDOR_1":
                return servidor1;
            case "SERVIDOR_2":
                return servidor2;
            default:
                return null;
        }
    }

    public Set<Entity> getEntitiesInTransport() {
        return new HashSet<>(entitiesInTransport);
    }

    public List<Entity> getAllActiveEntities() {
        synchronized (allActiveEntities) {
            return new ArrayList<>(allActiveEntities);
        }
    }

    private void wakeUpWaitingChairs(double time) {
        for (Entity entity : getAllActiveEntities()) {
            if ("SALA_SILLAS".equals(entity.getCurrentLocation())) {
                scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS"));
            }
        }
    }

    private void wakeUpStandingRoom(double time) {
        for (Entity entity : getAllActiveEntities()) {
            if ("SALA_DE_PIE".equals(entity.getCurrentLocation())) {
                scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_DE_PIE"));
            }
        }
    }

    private void completeServerExit(ProcessingLocation servidor, Entity entity, double time) {
        servidor.exit(entity, time);
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, time);
        wakeUpWaitingChairs(time);
        updateWaitingAreaSnapshot();
    }
}
