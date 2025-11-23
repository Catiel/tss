package com.simulation.processing;

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.locations.Location;
import com.simulation.resources.Resource;

import java.util.*;

public class OperationHandler {
    private final SimulationEngine engine;
    private final Random random;

    // Colas para JOIN operations
    private final Map<String, Queue<Entity>> joinQueues;
    private final Map<String, Integer> joinRequirements;

    public OperationHandler(SimulationEngine engine) {
        this.engine = engine;
        this.random = new Random();
        this.joinQueues = new HashMap<>();
        this.joinRequirements = new HashMap<>();

        initializeJoinRequirements();
    }

    private void initializeJoinRequirements() {
        joinQueues.put("COCCION_GRANOS_DE_CEBADA", new LinkedList<>());
        joinQueues.put("COCCION_LUPULO", new LinkedList<>());
        joinRequirements.put("COCCION_LUPULO", 4);

        joinQueues.put("FERMENTACION_MOSTO", new LinkedList<>());
        joinQueues.put("FERMENTACION_LEVADURA", new LinkedList<>());
        joinRequirements.put("FERMENTACION_LEVADURA", 2);

        joinQueues.put("EMPACADO_CAJA_VACIA", new LinkedList<>());
        joinQueues.put("EMPACADO_BOTELLA_CON_CERVEZA", new LinkedList<>());
        joinRequirements.put("EMPACADO_BOTELLA_CON_CERVEZA", 6);
    }

    public void handleArrival(Entity entity, String locationName) {
        Location location = engine.getLocation(locationName);
        double currentTime = engine.getClock().getCurrentTime();

        // Solo registrar entrada si NO es transformada y es primera vez
        if (entity.getEntryTime() == 0) {
            entity.setEntryTime(currentTime);
            if (!entity.isTransformed()) {
                engine.getStatistics().recordEntityEntry(entity);
            }
        }
        location.enter(entity, currentTime);

        engine.getStatistics().recordLocationEntry(locationName);
        scheduleProcessing(entity, locationName);
    }

    public void scheduleProcessing(Entity entity, String locationName) {
        ProcessingRule rule = engine.getProcessingRule(locationName);
        if (rule != null) {
            double processingTime = rule.getProcessingTime();
            double currentTime = engine.getClock().getCurrentTime();

            Event processingEvent = new Event(currentTime + processingTime, 0,
                "Process " + entity.getType().getName() + " at " + locationName) {
                @Override
                public void execute() {
                    completeProcessing(entity, locationName);
                }
            };

            engine.getScheduler().scheduleEvent(processingEvent);

            if (processingTime > 0) {
                entity.addValueAddedTime(processingTime);
                engine.getStatistics().recordLocationProcessingTime(locationName, processingTime);
            }
        }
    }

    public void completeProcessing(Entity entity, String locationName) {
        Location location = engine.getLocation(locationName);
        double currentTime = engine.getClock().getCurrentTime();

        location.exit(currentTime);

        String entityType = entity.getType().getName();

        if (isJoinLocation(locationName, entityType)) {
            handleJoinLogic(entity, locationName);
        } else {
            routeEntity(entity, locationName);
        }
    }

    private boolean isJoinLocation(String locationName, String entityType) {
        if (locationName.equals("COCCION") && (entityType.equals("GRANOS_DE_CEBADA") || entityType.equals("LUPULO"))) {
            return true;
        }
        if (locationName.equals("FERMENTACION") && (entityType.equals("MOSTO") || entityType.equals("LEVADURA"))) {
            return true;
        }
        if (locationName.equals("EMPACADO") && (entityType.equals("CAJA_VACIA") || entityType.equals("BOTELLA_CON_CERVEZA"))) {
            return true;
        }
        return false;
    }

    private void handleJoinLogic(Entity entity, String locationName) {
        String entityType = entity.getType().getName();
        String queueKey = locationName + "_" + entityType;

        joinQueues.get(queueKey).add(entity);

        if (locationName.equals("COCCION")) {
            processJoinCoccion();
        } else if (locationName.equals("FERMENTACION")) {
            processJoinFermentacion();
        } else if (locationName.equals("EMPACADO")) {
            processJoinEmpacado();
        }
    }

    private void processJoinCoccion() {
        Queue<Entity> granosQueue = joinQueues.get("COCCION_GRANOS_DE_CEBADA");
        Queue<Entity> lupuloQueue = joinQueues.get("COCCION_LUPULO");

        while (!granosQueue.isEmpty() && lupuloQueue.size() >= 4) {
            Entity granosEntity = granosQueue.poll();

            double totalValueAdded = granosEntity.getTotalValueAddedTime();
            double totalNonValueAdded = granosEntity.getTotalNonValueAddedTime();
            double entryTime = granosEntity.getEntryTime();
            double currentTime = engine.getClock().getCurrentTime();

            granosEntity.addSystemTime(currentTime - granosEntity.getEntryTime());
            engine.getStatistics().recordEntityExit(granosEntity);

            for (int i = 0; i < 4; i++) {
                Entity lupuloEntity = lupuloQueue.poll();
                totalValueAdded += lupuloEntity.getTotalValueAddedTime();
                totalNonValueAdded += lupuloEntity.getTotalNonValueAddedTime();

                lupuloEntity.addSystemTime(currentTime - lupuloEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(lupuloEntity);
            }

            // Crear MOSTO como transformación
            EntityType mostoType = engine.getEntityType("MOSTO");
            Entity mosto = new Entity(mostoType, true); // Marcar como transformada
            mosto.setEntryTime(entryTime);
            mosto.addValueAddedTime(totalValueAdded);
            mosto.addNonValueAddedTime(totalNonValueAdded);

            // Registrar entrada de MOSTO
            engine.getStatistics().recordEntityEntry(mosto);

            handleArrival(mosto, "ENFRIAMIENTO");
        }
    }

    private void processJoinFermentacion() {
        Queue<Entity> mostoQueue = joinQueues.get("FERMENTACION_MOSTO");
        Queue<Entity> levaduraQueue = joinQueues.get("FERMENTACION_LEVADURA");

        while (!mostoQueue.isEmpty() && levaduraQueue.size() >= 2) {
            Entity mostoEntity = mostoQueue.poll();

            double totalValueAdded = mostoEntity.getTotalValueAddedTime();
            double totalNonValueAdded = mostoEntity.getTotalNonValueAddedTime();
            double entryTime = mostoEntity.getEntryTime();
            double currentTime = engine.getClock().getCurrentTime();

            mostoEntity.addSystemTime(currentTime - mostoEntity.getEntryTime());
            engine.getStatistics().recordEntityExit(mostoEntity);

            for (int i = 0; i < 2; i++) {
                Entity levaduraEntity = levaduraQueue.poll();
                totalValueAdded += levaduraEntity.getTotalValueAddedTime();
                totalNonValueAdded += levaduraEntity.getTotalNonValueAddedTime();

                levaduraEntity.addSystemTime(currentTime - levaduraEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(levaduraEntity);
            }

            // Crear CERVEZA como transformación
            EntityType cervezaType = engine.getEntityType("CERVEZA");
            Entity cerveza = new Entity(cervezaType, true); // Marcar como transformada
            cerveza.setEntryTime(entryTime);
            cerveza.addValueAddedTime(totalValueAdded);
            cerveza.addNonValueAddedTime(totalNonValueAdded);

            // Registrar entrada de CERVEZA
            engine.getStatistics().recordEntityEntry(cerveza);

            handleArrival(cerveza, "MADURACION");
        }
    }

    private void processJoinEmpacado() {
        Queue<Entity> cajaQueue = joinQueues.get("EMPACADO_CAJA_VACIA");
        Queue<Entity> botellaQueue = joinQueues.get("EMPACADO_BOTELLA_CON_CERVEZA");

        while (!cajaQueue.isEmpty() && botellaQueue.size() >= 6) {
            Entity cajaEntity = cajaQueue.poll();

            double totalValueAdded = cajaEntity.getTotalValueAddedTime();
            double totalNonValueAdded = cajaEntity.getTotalNonValueAddedTime();
            double entryTime = cajaEntity.getEntryTime();
            double currentTime = engine.getClock().getCurrentTime();

            cajaEntity.addSystemTime(currentTime - cajaEntity.getEntryTime());
            engine.getStatistics().recordEntityExit(cajaEntity);

            for (int i = 0; i < 6; i++) {
                Entity botellaEntity = botellaQueue.poll();
                totalValueAdded += botellaEntity.getTotalValueAddedTime();
                totalNonValueAdded += botellaEntity.getTotalNonValueAddedTime();

                botellaEntity.addSystemTime(currentTime - botellaEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(botellaEntity);
            }

            // Crear CAJA_CON_CERVEZAS como transformación
            EntityType cajaLlenaType = engine.getEntityType("CAJA_CON_CERVEZAS");
            Entity cajaLlena = new Entity(cajaLlenaType, true); // Marcar como transformada
            cajaLlena.setEntryTime(entryTime);
            cajaLlena.addValueAddedTime(totalValueAdded + 10);
            cajaLlena.addNonValueAddedTime(totalNonValueAdded);

            // Registrar entrada de CAJA_CON_CERVEZAS
            engine.getStatistics().recordEntityEntry(cajaLlena);

            moveWithResource(cajaLlena, "ALMACENAJE", "OPERADOR_EMPACADO");
        }
    }

    public void routeEntity(Entity entity, String fromLocation) {
        String entityType = entity.getType().getName();
        RoutingRule route = getRoutingRule(fromLocation, entityType);

        if (route != null) {
            String destination = route.getDestinationLocation();

            if ("EXIT".equals(destination)) {
                handleExit(entity);
            } else if ("JOIN".equals(destination)) {
                return;
            } else {
                double probability = route.getProbability();
                if (random.nextDouble() <= probability) {
                    int quantity = route.getQuantity();

                    if (quantity > 1) {
                        // FIRST 6: registrar salida de entidad original
                        double currentTime = engine.getClock().getCurrentTime();
                        entity.addSystemTime(currentTime - entity.getEntryTime());
                        engine.getStatistics().recordEntityExit(entity);

                        // Crear 6 entidades nuevas transformadas
                        for (int i = 0; i < quantity; i++) {
                            Entity newEntity = createTransformedEntity(entity, destination);
                            // Registrar entrada de cada botella
                            engine.getStatistics().recordEntityEntry(newEntity);

                            if (route.getResourceName() != null && !route.getResourceName().isEmpty()) {
                                moveWithResource(newEntity, destination, route.getResourceName());
                            } else {
                                handleArrival(newEntity, destination);
                            }
                        }
                    } else {
                        if (route.getResourceName() != null && !route.getResourceName().isEmpty()) {
                            moveWithResource(entity, destination, route.getResourceName());
                        } else {
                            handleArrival(entity, destination);
                        }
                    }
                } else {
                    handleExit(entity);
                }
            }
        } else {
            handleExit(entity);
        }
    }

    private Entity createTransformedEntity(Entity original, String destinationLocation) {
        EntityType newType = null;

        if (destinationLocation.equals("ETIQUETADO")) {
            newType = engine.getEntityType("BOTELLA_CON_CERVEZA");
        }

        if (newType != null) {
            Entity newEntity = new Entity(newType, true); // Marcar como transformada
            newEntity.setEntryTime(original.getEntryTime());
            newEntity.addValueAddedTime(original.getTotalValueAddedTime());
            newEntity.addNonValueAddedTime(original.getTotalNonValueAddedTime());
            newEntity.addWaitTime(original.getTotalWaitTime());
            return newEntity;
        }

        return original;
    }

    private void moveWithResource(Entity entity, String destination, String resourceName) {
        Resource resource = engine.getResource(resourceName);
        double currentTime = engine.getClock().getCurrentTime();

        if (resource != null && resource.isAvailable()) {
            resource.acquire(currentTime);

            double moveTime = 2.0;

            Event moveEvent = new Event(currentTime + moveTime, 0,
                "Move " + entity.getType().getName() + " to " + destination) {
                @Override
                public void execute() {
                    resource.release(engine.getClock().getCurrentTime());
                    handleArrival(entity, destination);
                }
            };

            engine.getScheduler().scheduleEvent(moveEvent);
            entity.addNonValueAddedTime(moveTime);
        } else {
            if (resource != null) {
                resource.addToQueue(entity);
                entity.addWaitTime(1.0);
            }
        }
    }

    private void handleExit(Entity entity) {
        double currentTime = engine.getClock().getCurrentTime();
        entity.addSystemTime(currentTime - entity.getEntryTime());
        engine.getStatistics().recordEntityExit(entity);
    }

    private RoutingRule getRoutingRule(String locationName, String entityType) {
        return createRoutingRuleForLocation(locationName, entityType);
    }

    private RoutingRule createRoutingRuleForLocation(String locationName, String entityType) {
        switch (locationName) {
            case "SILO_GRANDE":
                return new RoutingRule("MALTEADO", 1.0, 1, "FIRST", null);
            case "MALTEADO":
                return new RoutingRule("SECADO", 1.0, 1, "FIRST", "OPERADOR_RECEPCION");
            case "SECADO":
                return new RoutingRule("MOLIENDA", 1.0, 1, "FIRST", "OPERADOR_RECEPCION");
            case "MOLIENDA":
                return new RoutingRule("MACERADO", 1.0, 1, "FIRST", null);
            case "MACERADO":
                return new RoutingRule("FILTRADO", 1.0, 1, "FIRST", null);
            case "FILTRADO":
                return new RoutingRule("COCCION", 1.0, 1, "FIRST", null);
            case "SILO_LUPULO":
                return new RoutingRule("COCCION", 1.0, 1, "JOIN", "OPERADOR_LUPULO");
            case "ENFRIAMIENTO":
                return new RoutingRule("FERMENTACION", 1.0, 1, "FIRST", null);
            case "SILO_LEVADURA":
                return new RoutingRule("FERMENTACION", 1.0, 1, "JOIN", "OPERADOR_LEVADURA");
            case "MADURACION":
                return new RoutingRule("INSPECCION", 1.0, 1, "FIRST", null);
            case "INSPECCION":
                return new RoutingRule("EMBOTELLADO", 0.9, 1, "FIRST", null);
            case "EMBOTELLADO":
                return new RoutingRule("ETIQUETADO", 1.0, 6, "FIRST", null);
            case "ETIQUETADO":
                return new RoutingRule("EMPACADO", 1.0, 1, "JOIN", null);
            case "ALMACEN_CAJAS":
                return new RoutingRule("EMPACADO", 1.0, 1, "FIRST", null);
            case "ALMACENAJE":
                return new RoutingRule("MERCADO", 1.0, 1, "FIRST", "CAMION");
            case "MERCADO":
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null);
            default:
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null);
        }
    }
}
