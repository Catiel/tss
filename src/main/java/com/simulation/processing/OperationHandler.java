package com.simulation.processing;

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.gui.AnimationController;
import com.simulation.locations.Location;
import com.simulation.resources.Resource;

import java.util.*;

public class OperationHandler {
    private final SimulationEngine engine;
    private final Random random;
    private AnimationController animationController;

    // Colas para JOIN operations
    private final Map<String, Queue<Entity>> joinQueues;
    private final Map<String, Integer> joinRequirements;

    public OperationHandler(SimulationEngine engine) {
        this.engine = engine;
        this.random = new Random();
        this.joinQueues = new HashMap<>();
        this.joinRequirements = new HashMap<>();
        this.animationController = null;

        initializeJoinRequirements();
    }

    public void setAnimationController(AnimationController controller) {
        this.animationController = controller;
    }

    private void initializeJoinRequirements() {
        // COCCION: 1 GRANOS_DE_CEBADA + 4 LUPULO
        joinQueues.put("COCCION_GRANOS_DE_CEBADA", new LinkedList<>());
        joinQueues.put("COCCION_LUPULO", new LinkedList<>());
        joinRequirements.put("COCCION_LUPULO", 4);

        // FERMENTACION: 1 MOSTO + 2 LEVADURA
        joinQueues.put("FERMENTACION_MOSTO", new LinkedList<>());
        joinQueues.put("FERMENTACION_LEVADURA", new LinkedList<>());
        joinRequirements.put("FERMENTACION_LEVADURA", 2);

        // EMPACADO: 1 CAJA_VACIA + 6 BOTELLA_CON_CERVEZA
        joinQueues.put("EMPACADO_CAJA_VACIA", new LinkedList<>());
        joinQueues.put("EMPACADO_BOTELLA_CON_CERVEZA", new LinkedList<>());
        joinRequirements.put("EMPACADO_BOTELLA_CON_CERVEZA", 6);
        
        // ACCUM para ALMACENAJE: acumular 6 cajas
        joinQueues.put("ALMACENAJE_ACCUM", new LinkedList<>());
        joinRequirements.put("ALMACENAJE_ACCUM", 6);
    }
    
    /**
     * Maneja la lógica ACCUM - acumula entidades hasta alcanzar cantidad requerida
     */
    private void handleAccumulate(Entity entity, String fromLocation, String destination, 
                                  int quantity, String resourceName) {
        String accumKey = fromLocation + "_ACCUM";
        Queue<Entity> accumQueue = joinQueues.get(accumKey);
        
        if (accumQueue != null) {
            accumQueue.add(entity);
            
            // Cuando se acumulan suficientes, liberar UNA entidad (no todas)
            // Las demás siguen esperando en cola para acumularse de nuevo
            if (accumQueue.size() >= quantity) {
                // Solo enviar la primera entidad del batch
                Entity firstEntity = accumQueue.poll();
                
                if (resourceName != null && !resourceName.isEmpty()) {
                    moveWithResource(firstEntity, destination, resourceName);
                } else {
                    handleArrival(firstEntity, destination);
                }
            }
        }
    }

    public void handleArrival(Entity entity, String locationName) {
        Location location = engine.getLocation(locationName);
        double currentTime = engine.getClock().getCurrentTime();

        // Solo registrar entrada si NO es transformada y es primera vez
        if (entity.getEntryTime() == 0) {
            entity.setEntryTime(currentTime);
            if (!entity.isTransformed()) {
                engine.getStatistics().recordEntityEntry(entity);
                // Notificar creación de entidad
                engine.notifyEntityCreated(entity, location);
            }
        }
        location.enter(entity, currentTime);
        
        // Notificar llegada a locación
        engine.notifyEntityArrival(entity, location);

        // Solo contar entrada a locación si NO es entidad secundaria de JOIN
        // Entidades secundarias: LUPULO (a COCCION), LEVADURA (a FERMENTACION), BOTELLA (a EMPACADO)
        String entityType = entity.getType().getName();
        boolean isSecondaryJoinEntity = 
            (locationName.equals("COCCION") && entityType.equals("LUPULO")) ||
            (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) ||
            (locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA"));
        
        if (!isSecondaryJoinEntity) {
            engine.getStatistics().recordLocationEntry(locationName);
        }
        
        scheduleProcessing(entity, locationName);

        // Actualizar GUI si está disponible
        if (animationController != null) {
            updateGUILocationOccupancy(locationName);
        }
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

        // Actualizar GUI si está disponible
        if (animationController != null) {
            updateGUILocationOccupancy(locationName);
        }
    }

    private boolean isJoinLocation(String locationName, String entityType) {
        // Solo las entidades PRINCIPALES que inician el JOIN son manejadas aquí
        if (locationName.equals("COCCION") && entityType.equals("GRANOS_DE_CEBADA")) {
            return true;
        }
        if (locationName.equals("FERMENTACION") && entityType.equals("MOSTO")) {
            return true;
        }
        if (locationName.equals("EMPACADO") && entityType.equals("CAJA_VACIA")) {
            return true;
        }
        // LUPULO, LEVADURA y BOTELLA simplemente se encolan (manejado en handleJoinLogic)
        if (locationName.equals("COCCION") && entityType.equals("LUPULO")) {
            return true;
        }
        if (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) {
            return true;
        }
        return locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA");
    }

    private void handleJoinLogic(Entity entity, String locationName) {
        String entityType = entity.getType().getName();
        String queueKey = locationName + "_" + entityType;

        joinQueues.get(queueKey).add(entity);

        // Solo intentar procesar el JOIN si es la entidad PRINCIPAL
        if (locationName.equals("COCCION") && entityType.equals("GRANOS_DE_CEBADA")) {
            processJoinCoccion();
        } else if (locationName.equals("FERMENTACION") && entityType.equals("MOSTO")) {
            processJoinFermentacion();
        } else if (locationName.equals("EMPACADO") && entityType.equals("CAJA_VACIA")) {
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
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 4; i++) {
                Entity lupuloEntity = lupuloQueue.poll();
                totalValueAdded += lupuloEntity.getTotalValueAddedTime();
                totalNonValueAdded += lupuloEntity.getTotalNonValueAddedTime();

                lupuloEntity.addSystemTime(currentTime - lupuloEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(lupuloEntity);
            }

            // Crear MOSTO como transformación
            EntityType mostoType = engine.getEntityType("MOSTO");
            Entity mosto = new Entity(mostoType, true);
            mosto.setEntryTime(entryTime);
            mosto.addValueAddedTime(totalValueAdded);
            mosto.addNonValueAddedTime(totalNonValueAdded);

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
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 2; i++) {
                Entity levaduraEntity = levaduraQueue.poll();
                totalValueAdded += levaduraEntity.getTotalValueAddedTime();
                totalNonValueAdded += levaduraEntity.getTotalNonValueAddedTime();

                levaduraEntity.addSystemTime(currentTime - levaduraEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(levaduraEntity);
            }

            // Crear CERVEZA como transformación
            EntityType cervezaType = engine.getEntityType("CERVEZA");
            Entity cerveza = new Entity(cervezaType, true);
            cerveza.setEntryTime(entryTime);
            cerveza.addValueAddedTime(totalValueAdded);
            cerveza.addNonValueAddedTime(totalNonValueAdded);

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
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 6; i++) {
                Entity botellaEntity = botellaQueue.poll();
                totalValueAdded += botellaEntity.getTotalValueAddedTime();
                totalNonValueAdded += botellaEntity.getTotalNonValueAddedTime();

                botellaEntity.addSystemTime(currentTime - botellaEntity.getEntryTime());
                engine.getStatistics().recordEntityExit(botellaEntity);
            }

            // Crear CAJA_CON_CERVEZAS como transformación
            EntityType cajaLlenaType = engine.getEntityType("CAJA_CON_CERVEZAS");
            Entity cajaLlena = new Entity(cajaLlenaType, true);
            cajaLlena.setEntryTime(entryTime);
            cajaLlena.addValueAddedTime(totalValueAdded);
            cajaLlena.addNonValueAddedTime(totalNonValueAdded);

            // Registrar como nueva entidad al sistema (producto final)
            engine.getStatistics().recordEntityEntry(cajaLlena);
            
            // WAIT 10 min en EMPACADO (procesamiento)
            cajaLlena.addValueAddedTime(10);
            engine.getStatistics().recordLocationProcessingTime("EMPACADO", 10);
            
            // Programar movimiento a ALMACENAJE después de 10 minutos
            double currentTimeAfterJoin = engine.getClock().getCurrentTime();
            final Entity finalCajaLlena = cajaLlena; // Referencia final para closure
            
            Event packingEvent = new Event(currentTimeAfterJoin + 10, 0,
                "Pack caja at EMPACADO") {
                @Override
                public void execute() {
                    moveWithResource(finalCajaLlena, "ALMACENAJE", "OPERADOR_EMPACADO");
                }
            };
            engine.getScheduler().scheduleEvent(packingEvent);
        }
    }

    public void routeEntity(Entity entity, String fromLocation) {
        String entityType = entity.getType().getName();
        RoutingRule route = getRoutingRule(fromLocation, entityType);

        if (route != null) {
            String destination = route.destinationLocation();
            String moveLogic = route.moveLogic();

            if ("EXIT".equals(destination)) {
                handleExit(entity);
            } else if ("JOIN".equals(moveLogic)) {
                // La entidad va a una locación para participar en un JOIN
                handleArrival(entity, destination);
            } else {
                double probability = route.probability();
                if (random.nextDouble() <= probability) {
                    int quantity = route.quantity();

                    if ("ACCUM".equals(route.moveLogic()) && quantity > 1) {
                        // ACCUM: acumular entidades antes de mover en batch
                        handleAccumulate(entity, fromLocation, destination, quantity, route.resourceName());
                    } else if (quantity > 1) {
                        // FIRST 6: NO registrar exit (es transformación interna), SÍ registrar entries
                        double currentTime = engine.getClock().getCurrentTime();
                        entity.addSystemTime(currentTime - entity.getEntryTime());
                        // NO recordEntityExit - la entidad se transforma, no sale del sistema

                        // Crear 6 entidades nuevas transformadas
                        for (int i = 0; i < quantity; i++) {
                            Entity newEntity = createTransformedEntity(entity, destination);
                            engine.getStatistics().recordEntityEntry(newEntity);

                            if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                                moveWithResource(newEntity, destination, route.resourceName());
                            } else {
                                handleArrival(newEntity, destination);
                            }
                        }
                    } else {
                        if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                            moveWithResource(entity, destination, route.resourceName());
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
            Entity newEntity = new Entity(newType, true);
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
            
            // Notificar que el recurso fue adquirido
            engine.notifyResourceAcquired(resource, entity);

            double moveTime = 2.0;

            // Animar movimiento si hay GUI disponible
            if (animationController != null) {
                String currentLocation = entity.getCurrentLocation() != null ?
                    entity.getCurrentLocation().getType().name() : "UNKNOWN";

                animationController.animateEntityMovement(
                    entity,
                    currentLocation,
                    destination,
                    resourceName,
                    () -> {
                        // Callback cuando termina la animación
                        resource.release(engine.getClock().getCurrentTime());
                        handleArrival(entity, destination);
                    }
                );
            }

            Event moveEvent = new Event(currentTime + moveTime, 0,
                "Move " + entity.getType().getName() + " to " + destination) {
                @Override
                public void execute() {
                    // Notificar liberación de recurso
                    engine.notifyResourceReleased(resource, entity);
                    
                    Location from = entity.getCurrentLocation();
                    Location to = engine.getLocation(destination);
                    if (from != null && to != null) {
                        // Notificar movimiento de entidad
                        engine.notifyEntityMove(entity, from, to);
                    }
                    
                    // Solo ejecutar si NO hay GUI (modo consola)
                    if (animationController == null) {
                        resource.release(engine.getClock().getCurrentTime());
                        handleArrival(entity, destination);
                    }
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
        
        Location from = entity.getCurrentLocation();
        if (from != null) {
            // Notificar salida del sistema
            engine.notifyEntityExit(entity, from);
        }
        
        engine.getStatistics().recordEntityExit(entity);

        // Actualizar GUI si está disponible
        if (animationController != null) {
            updateGUIEntityCount(entity.getType().getName());
        }
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
                return new RoutingRule("COCCION", 1.0, 1, "JOIN", "OPERADOR_LUPADURA");
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
                return new RoutingRule("MERCADO", 1.0, 6, "ACCUM", "CAMION");
            case "MERCADO":
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null);
            default:
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null);
        }
    }

    // Métodos auxiliares para actualizar la GUI
    private void updateGUILocationOccupancy(String locationName) {
        if (animationController != null) {
            Location location = engine.getLocation(locationName);
            if (location != null) {
                int occupancy = location.getCurrentOccupancy();
                int capacity = location.getType().capacity();

                // Actualizar el nodo visual de la locación
                if (animationController.getLocationNodes().containsKey(locationName)) {
                    animationController.getLocationNodes().get(locationName).setOccupancy(occupancy);
                    animationController.getLocationNodes().get(locationName).setCapacity(capacity);
                }
            }
        }
    }

    private void updateGUIEntityCount(String entityType) {
        if (animationController != null) {
            // Esta actualización se puede expandir para mostrar contadores en tiempo real
        }
    }
}
