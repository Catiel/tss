package com.simulation.core;

import com.simulation.config.SimulationParameters;
import com.simulation.random.RandomGenerators;
import com.simulation.resources.*;
import com.simulation.statistics.Statistics;
import com.simulation.core.EventTypes.*;

import java.util.*;

/**
 * Motor de simulación DIGEMIC - Sistema de expedición de pasaportes
 * Modelo con 6 locaciones: Entrada, Zona_Formas, Sala_Sillas, Sala_De_Pie, Servidor_1, Servidor_2
 */
public class DigemicEngine {
    
    private SimulationParameters params;
    private RandomGenerators randomGen;
    private Statistics statistics;
    private PriorityQueue<Event> eventQueue;
    private double currentTime;
    private boolean running;
    private boolean paused;
    private volatile double simulationSpeed = 100.0;
    private long lastRealTime = 0;

    // 6 Locaciones del sistema DIGEMIC
    private BufferLocation entrada;
    private BufferLocation zonaFormas;
    private BufferLocation salaSillas;     // Capacidad 40
    private BufferLocation salaDePie;      // Capacidad infinita
    private ProcessingLocation servidor1;
    private ProcessingLocation servidor2;

    private Set<Entity> entitiesInTransport;
    private List<Entity> allActiveEntities;

    // Contadores de pasaportes por servidor (cada 10 → pausa)
    private int pasaportesAtendidosServidor1 = 0;
    private int pasaportesAtendidosServidor2 = 0;

    public DigemicEngine(SimulationParameters params) {
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
        currentTime = 0;
        running = false;
        paused = false;
        lastRealTime = 0;
        pasaportesAtendidosServidor1 = 0;
        pasaportesAtendidosServidor2 = 0;
        Entity.resetIdCounter();
        statistics.reset();

        entrada.resetState();
        zonaFormas.resetState();
        salaSillas.resetState();
        salaDePie.resetState();
        servidor1.resetState();
        servidor2.resetState();

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

            if (!running) break;

            Event nextEvent = eventQueue.peek();
            if (nextEvent == null) {
                break;
            }

            if (nextEvent.getTime() >= endTime) {
                currentTime = endTime;
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
                currentTime = endTime;
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

        entrada.enter(entity, time);
        entity.setCurrentLocation("ENTRADA");

        // Programar siguiente arribo
        if (time < params.getSimulationDurationMinutes()) {
            double nextArrival = time + randomGen.nextArrivalTime();
            scheduleEvent(new ArrivalEvent(nextArrival));
        }

        // Decisión de routing: 90% directo a sala, 10% a formas
        if (entity.getRoutingDestination() == null) {
            boolean directo = randomGen.goDirectoASala();
            entity.setRoutingDestination(directo ? "SALA" : "FORMAS");
        }

        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "ENTRADA"));
    }

    public void handleProcessEnd(Entity entity, String locationName, double time) {
        switch (locationName) {
            case "ENTRADA": finishEntrada(entity, time); break;
            case "ZONA_FORMAS": finishZonaFormas(entity, time); break;
            case "SALA_DE_PIE": finishSalaDePie(entity, time); break;
            case "SALA_SILLAS": finishSalaSillas(entity, time); break;
            case "SERVIDOR_1": finishServidor1(entity, time); break;
            case "SERVIDOR_2": finishServidor2(entity, time); break;
        }
    }

    // === MÉTODOS FINISH ===

    private void finishEntrada(Entity entity, double time) {
        String destination = entity.getRoutingDestination();
        entrada.exit(entity, time);

        if ("FORMAS".equals(destination)) {
            // 10% va a llenar formas
            zonaFormas.enter(entity, time);
            entity.setCurrentLocation("ZONA_FORMAS");
            double formasTime = randomGen.nextZonaFormasTime();
            entity.addProcessTime(formasTime);
            scheduleEvent(new ProcessEndEvent(time + formasTime, entity, "ZONA_FORMAS"));
        } else {
            // 90% va directo a sala
            tryEnterSala(entity, time);
        }
    }

    private void finishZonaFormas(Entity entity, double time) {
        zonaFormas.exit(entity, time);
        entity.setRoutingDestination(null); // Limpiar
        tryEnterSala(entity, time);
    }

    private void tryEnterSala(Entity entity, double time) {
        // Intentar entrar a Sala_Sillas (40 sillas)
        if (salaSillas.canEnter()) {
            salaSillas.enter(entity, time);
            entity.setCurrentLocation("SALA_SILLAS");
            entity.setBlocked(false, time);
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS"));
        } else {
            // No hay sillas, va a Sala_De_Pie
            salaDePie.enter(entity, time);
            entity.setCurrentLocation("SALA_DE_PIE");
            entity.setBlocked(true, time);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "SALA_DE_PIE"));
        }
    }

    private void finishSalaDePie(Entity entity, double time) {
        // Intentar sentarse si hay silla disponible
        if (salaSillas.canEnter()) {
            salaDePie.exit(entity, time);
            salaSillas.enter(entity, time);
            entity.setCurrentLocation("SALA_SILLAS");
            entity.setBlocked(false, time);
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS"));
        } else {
            // Sigue de pie, reintentar
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "SALA_DE_PIE"));
        }
    }

    private void finishSalaSillas(Entity entity, double time) {
        // Intentar ir a servidor disponible (FIRST)
        if (servidor1.canEnter()) {
            servidor1.reserveCapacity();
            salaSillas.exit(entity, time);
            arriveAtServidor1(entity, time);
        } else if (servidor2.canEnter()) {
            servidor2.reserveCapacity();
            salaSillas.exit(entity, time);
            arriveAtServidor2(entity, time);
        } else {
            // Esperar sentado
            entity.addWaitTime(0.5);
            scheduleEvent(new ProcessEndEvent(time + 0.5, entity, "SALA_SILLAS"));
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
        servidor1.exit(entity, time);
        pasaportesAtendidosServidor1++;
        
        // Salir del sistema
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, time);

        // Verificar si necesita pausa (cada 10 pasaportes)
        if (pasaportesAtendidosServidor1 >= params.getPasaportesPorPausa()) {
            double pausaTime = randomGen.nextPausaServidorTime();
            pasaportesAtendidosServidor1 = 0;
            // Durante la pausa, el servidor no puede atender
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, "SERVIDOR_1"));
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
        servidor2.exit(entity, time);
        pasaportesAtendidosServidor2++;
        
        // Salir del sistema
        allActiveEntities.remove(entity);
        statistics.recordExit(entity, time);

        // Verificar si necesita pausa (cada 10 pasaportes)
        if (pasaportesAtendidosServidor2 >= params.getPasaportesPorPausa()) {
            double pausaTime = randomGen.nextPausaServidorTime();
            pasaportesAtendidosServidor2 = 0;
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, "SERVIDOR_2"));
        }
    }

    public void handleServerPauseEnd(String serverName, double time) {
        // La pausa terminó, el servidor puede volver a atender
        // No se necesita acción especial, el servidor ya está disponible
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
            case "ENTRADA": return entrada;
            case "ZONA_FORMAS": return zonaFormas;
            case "SALA_SILLAS": return salaSillas;
            case "SALA_DE_PIE": return salaDePie;
            case "SERVIDOR_1": return servidor1;
            case "SERVIDOR_2": return servidor2;
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
