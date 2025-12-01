package com.simulation.processing;

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.Entity;
import com.simulation.gui.AnimationController;
import com.simulation.locations.Location;
import com.simulation.resources.Resource;

import java.util.*;

public class OperationHandler {
    private final SimulationEngine engine;
    private final Random random;
    private final Map<String, Queue<Entity>> joinQueues;
    private final Map<String, Integer> joinRequirements;
    private final Map<String, Queue<Entity>> blockedEntities; // Entities waiting for a destination
    private final Map<Entity, Double> blockStartTime; // Track when entity became blocked
    private AnimationController animationController;

    public OperationHandler(SimulationEngine engine) {
        this.engine = engine;
        this.random = new Random();
        this.joinQueues = new HashMap<>();
        this.joinRequirements = new HashMap<>();
        this.blockedEntities = new HashMap<>();
        this.blockStartTime = new HashMap<>();
        this.animationController = null;

        initializeJoinRequirements();
    }

    public void setAnimationController(AnimationController controller) {
        this.animationController = controller;
    }

    private void initializeJoinRequirements() {
        joinQueues.put("HORNO_ACCUM", new LinkedList<>());
        // El tamaño del lote se obtiene dinámicamente del BatchProcessingRule
        // joinRequirements ya no se usa - ver getBatchSizeForLocation()
    }

    /**
     * Obtiene el tamaño del lote configurado para una locación.
     * Si no hay BatchProcessingRule, retorna 1 (sin acumulación).
     */
    private int getBatchSizeForLocation(String locationName) {
        ProcessingRule rule = engine.getProcessingRule(locationName);
        if (rule instanceof BatchProcessingRule) {
            return ((BatchProcessingRule) rule).getBatchSize();
        }
        return 1; // Default: no batch
    }

    private void notifyLocationAvailable(String locationName) {
        if (blockedEntities.containsKey(locationName)) {
            Queue<Entity> blockedQueue = blockedEntities.get(locationName);
            if (!blockedQueue.isEmpty()) {
                Location destination = engine.getLocation(locationName);
                if (destination.canAccept()) {
                    Entity entity = blockedQueue.poll();

                    // Calculate and record blocking time
                    if (entity != null && blockStartTime.containsKey(entity)) {
                        double currentTime = engine.getClock().getCurrentTime();
                        double blockedTime = currentTime - blockStartTime.get(entity);
                        entity.addBlockingTime(blockedTime);
                        blockStartTime.remove(entity); // Clean up
                    }

                    if (entity != null && entity.getCurrentLocation() != null) {
                        routeEntity(entity, entity.getCurrentLocation().getName());
                    }
                }
            }
        }
    }

    private void handleAccumulate(Entity entity, String fromLocation, String destination, int quantity,
            String resourceName) {
        String accumKey = fromLocation + "_ACCUM";
        Queue<Entity> accumQueue = joinQueues.get(accumKey);

        if (accumQueue != null) {
            accumQueue.add(entity);

            if (accumQueue.size() >= quantity) {
                for (int i = 0; i < quantity; i++) {
                    Entity batchEntity = accumQueue.poll();
                    if (batchEntity != null) {
                        if (resourceName != null && !resourceName.isEmpty()) {
                            moveWithResource(batchEntity, destination, resourceName);
                        } else {
                            handleArrival(batchEntity, destination);
                        }
                    }
                }
            }
        }
    }

    public void handleArrival(Entity entity, String locationName) {
        Location location = engine.getLocation(locationName);
        if (location == null) {
            System.err.println("CRITICAL ERROR: Location '" + locationName + "' not found!");
            System.err.println("Available locations: " + engine.getAllLocations().keySet());
            throw new RuntimeException("Location not found: " + locationName);
        }
        double currentTime = engine.getClock().getCurrentTime();

        if (entity.getEntryTime() == 0) {
            entity.setEntryTime(currentTime);
            if (!entity.isTransformed()) {
                engine.getStatistics().recordEntityEntry(entity);
                engine.notifyEntityCreated(entity, location);
            }
        }
        if (location.enter(entity, currentTime)) {
            engine.notifyEntityArrival(entity, location);

            String entityType = entity.getType().getName();
            boolean isSecondaryJoinEntity = (locationName.equals("COCCION") && entityType.equals("LUPULO")) ||
                    (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) ||
                    (locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA"));

            if (!isSecondaryJoinEntity) {
                engine.getStatistics().recordLocationEntry(locationName);
            }

            scheduleProcessing(entity, locationName);
        } else {
            // Entity is queued at the location, waiting for space
        }
        // location.exit(currentTime); // Removed to prevent premature exit
        // Routing is handled in completeProcessing or specific scheduleProcessing logic

        // if (isJoinLocation(locationName, entityType)) {
        // handleJoinLogic(entity, locationName);
        // } else {
        // routeEntity(entity, locationName);
        // }

        if (animationController != null) {
            updateGUILocationOccupancy(locationName);
        }
    }

    public void scheduleProcessing(Entity entity, String locationName) {
        if (locationName.equals("HORNO")) {
            String accumKey = "HORNO_ACCUM";
            Queue<Entity> accumQueue = joinQueues.get(accumKey);
            
            // Obtener tamaño de lote dinámico desde la configuración
            int batchSize = getBatchSizeForLocation("HORNO");

            if (accumQueue != null) {
                accumQueue.add(entity);
                
                System.out.println("[HORNO] Piezas acumuladas: " + accumQueue.size() + " / " + batchSize);

                if (accumQueue.size() >= batchSize) {
                    List<Entity> batchEntities = new ArrayList<>();
                    for (int i = 0; i < batchSize; i++) {
                        Entity batchEntity = accumQueue.poll();
                        if (batchEntity != null) {
                            batchEntities.add(batchEntity);
                        }
                    }

                    ProcessingRule rule = engine.getProcessingRule(locationName);
                    double processingTime = rule.getProcessingTime();
                    double currentTime = engine.getClock().getCurrentTime();

                    for (Entity batchEntity : batchEntities) {
                        batchEntity.addValueAddedTime(processingTime);
                    }
                    engine.getStatistics().recordLocationProcessingTime(locationName, processingTime);
                    
                    System.out.println("[HORNO] Procesando lote de " + batchEntities.size() + " piezas por " + processingTime + " min");

                    final int finalBatchSize = batchEntities.size();
                    Event processingEvent = new Event(currentTime + processingTime, 0, "Process batch of " + finalBatchSize + " in HORNO") {
                        @Override
                        public void execute() {
                            Location hornoLocation = engine.getLocation("HORNO");
                            double baseCompletionTime = engine.getClock().getCurrentTime();

                            for (int i = 0; i < batchEntities.size(); i++) {
                                Entity batchEntity = batchEntities.get(i);
                                double exitDelay = i * 0.01; // Salidas casi simultáneas para maximizar bloqueo

                                Event exitEvent = new Event(baseCompletionTime + exitDelay, 0,
                                        "Exit piece " + (i + 1) + " from HORNO") {
                                    @Override
                                    public void execute() {
                                        // hornoLocation.exit() removed to prevent premature release
                                        routeEntity(batchEntity, "HORNO");
                                    }
                                };
                                engine.getScheduler().scheduleEvent(exitEvent);
                            }
                        }
                    };
                    engine.getScheduler().scheduleEvent(processingEvent);
                }
            }
            return;
        }

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
        // Location exit is now handled in routeEntity or moveWithResource
        // to ensure utilization stats include waiting time for resources.

        routeEntity(entity, locationName);
        if (animationController != null) {
            updateGUILocationOccupancy(locationName);
        }
    }

    private void checkAndPromoteFromQueue(Location location) {
        while (location.canAccept() && location.getQueueSize() > 0) {
            Entity nextEntity = location.removeFromQueue();
            if (nextEntity != null) {
                if (location.enter(nextEntity, engine.getClock().getCurrentTime())) {
                    // Notify arrival for the promoted entity to trigger stats and processing
                    engine.notifyEntityArrival(nextEntity, location);
                    engine.getStatistics().recordLocationEntry(location.getName());
                    scheduleProcessing(nextEntity, location.getName());
                }
            }
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
                Location to = engine.getLocation(destination);
                if (to.canAccept()) {
                    if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                        moveWithResource(entity, destination, route.resourceName());
                    } else {
                        Location from = engine.getLocation(fromLocation);
                        engine.notifyEntityMove(entity, from, to);
                        handleArrival(entity, destination);
                    }
                } else {
                    blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity);
                    blockStartTime.put(entity, engine.getClock().getCurrentTime());
                }
            } else {
                double probability = route.probability();
                if (random.nextDouble() <= probability) {
                    int quantity = route.quantity();

                    if ("ACCUM".equals(route.moveLogic()) && quantity > 1) {
                        handleAccumulate(entity, fromLocation, destination, quantity, route.resourceName());
                    } else if (quantity > 1) {
                        double currentTime = engine.getClock().getCurrentTime();
                        entity.addSystemTime(currentTime - entity.getEntryTime());

                        for (int i = 0; i < quantity; i++) {
                            Entity newEntity = createTransformedEntity(entity, destination);
                            engine.getStatistics().recordEntityEntry(newEntity);

                            Location to = engine.getLocation(destination);
                            if (to.canAccept()) {
                                if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                                    moveWithResource(newEntity, destination, route.resourceName());
                                } else {
                                    Location from = engine.getLocation(fromLocation);
                                    engine.notifyEntityMove(newEntity, from, to);
                                    handleArrival(newEntity, destination);
                                }
                            } else {
                                blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(newEntity);
                                blockStartTime.put(newEntity, engine.getClock().getCurrentTime());
                            }
                        }
                    } else {
                        Location to = engine.getLocation(destination);
                        if (to.canAccept()) {
                            if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                                moveWithResource(entity, destination, route.resourceName());
                            } else {
                                Location from = engine.getLocation(fromLocation);
                                from.exit(engine.getClock().getCurrentTime());
                                checkAndPromoteFromQueue(from);
                                notifyLocationAvailable(fromLocation); // Notify that space is free

                                engine.notifyEntityMove(entity, from, to);

                                if ("DESCARGA".equals(fromLocation) && "BANDA_2".equals(destination)) {

                                    double currentTime = engine.getClock().getCurrentTime();
                                    Event moveEvent = new Event(currentTime + 1.0, 0,
                                            "Move through " + fromLocation + " to " + destination) {
                                        @Override
                                        public void execute() {
                                            handleArrival(entity, destination);
                                        }
                                    };
                                    engine.getScheduler().scheduleEvent(moveEvent);
                                    entity.addNonValueAddedTime(1.0);
                                } else {
                                    handleArrival(entity, destination);
                                }
                            }
                        } else {
                            blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity);
                            blockStartTime.put(entity, engine.getClock().getCurrentTime());
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
        return original;
    }

    private void checkResourceQueue(Resource resource) {
        // Check if resource is available OR if it's returning home (interruptible)
        if ((resource.isAvailable() || resource.isReturningHome()) && resource.getQueueSize() > 0) {

            // If returning home, we interrupt it.
            // The resource is technically "available" (units > 0) but was moving.
            // We claim it now.
            if (resource.isReturningHome()) {
                resource.setReturningHome(false);
                // Note: We assume it's still at the start location of the return trip
                // because we haven't processed the arrival event yet.
                // This is the desired behavior: claim it before it goes far.
            }

            // Implement "Closest" rule (Más Cercano)
            // Find the entity in the queue that is closest to the resource's current
            // location
            Entity bestEntity = null;
            double minDistance = Double.MAX_VALUE;
            String resourceLoc = resource.getCurrentLocation();

            for (Entity entity : resource.getQueue()) {
                String entityLoc = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName()
                        : "UNKNOWN";
                double dist = calculateDistance(resourceLoc, entityLoc);
                if (dist < minDistance) {
                    minDistance = dist;
                    bestEntity = entity;
                }
            }

            if (bestEntity != null) {
                resource.removeEntity(bestEntity);
                String destination = bestEntity.getPendingDestination();
                moveWithResource(bestEntity, destination, resource.getName());
            }
        } else if (resource.isAvailable() && resource.getQueueSize() == 0) {
            returnToHome(resource);
        }
    }

    private void returnToHome(Resource resource) {
        String homeLocation = null;
        if ("GRUA_VIAJERA".equals(resource.getName())) {
            homeLocation = "ALMACEN_MP";
        } else if ("ROBOT".equals(resource.getName())) {
            homeLocation = "CARGA";
        }

        if (homeLocation != null && !homeLocation.equals(resource.getCurrentLocation())) {
            // Mark as returning home
            resource.setReturningHome(true);
            // Increment ID to invalidate previous return events
            final long currentId = resource.incrementReturnHomeId();

            double moveTime = calculateMoveTime(resource.getCurrentLocation(), homeLocation, resource.getName());
            // Add a delay before actually returning. This keeps the resource at the last
            // location
            // for a while, allowing nearby locations (like Rectificado) to claim it
            // quickly.
            // This matches ProModel behavior where resources don't return immediately.
            double delay = 10.0;
            double currentTime = engine.getClock().getCurrentTime();
            String finalHome = homeLocation;

            Event returnEvent = new Event(currentTime + moveTime + delay, 0,
                    "Return " + resource.getName() + " to Home") {
                @Override
                public void execute() {
                    // Only update if this specific return event is still valid
                    if (resource.isReturningHome() && resource.getReturnHomeId() == currentId) {
                        resource.setCurrentLocation(finalHome);
                        resource.setReturningHome(false);
                    }
                }
            };
            engine.getScheduler().scheduleEvent(returnEvent);
        }
    }

    private void moveWithResource(Entity entity, String destination, String resourceName) {
        Resource resource = engine.getResource(resourceName);
        Location destinationLoc = engine.getLocation(destination);
        double currentTime = engine.getClock().getCurrentTime();

        // CRITICAL: Try to RESERVE space in destination BEFORE moving
        if (destinationLoc != null) {
            if (!destinationLoc.reserve()) {
                // Could not reserve - destination is full (considering current + reserved)
                // Entity is BLOCKED at current location
                blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity);
                blockStartTime.put(entity, currentTime); // Record when blocking started
                // Entity stays at current location - blocking time will accumulate
                return;
            }
            // Successfully reserved space - proceed with movement
        }

        if (resource != null && resource.isAvailable()) {
            // Invalidate any pending return events by incrementing the ID
            resource.incrementReturnHomeId();

            // If the resource was returning home, cancel that status
            if (resource.isReturningHome()) {
                resource.setReturningHome(false);
            }

            resource.acquire(currentTime);
            engine.notifyResourceAcquired(resource, entity);

            // 1. Calculate Empty Travel Time
            String resourceLoc = resource.getCurrentLocation();
            String entityLoc = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName() : "UNKNOWN";
            double emptyTravelTime = calculateMoveTime(resourceLoc, entityLoc, resourceName);

            if ("ROBOT".equals(resourceName) && "RECTIFICADO".equals(entityLoc)) {
                // Debug print removed
            }

            // 2. Schedule Pickup Event
            Event pickupEvent = new Event(currentTime + emptyTravelTime, 0, "Pickup " + entity.getType().getName()) {
                @Override
                public void execute() {
                    // Resource arrives at entity location
                    resource.setCurrentLocation(entityLoc);

                    // 3. Execute Loaded Move
                    performLoadedMove(entity, destination, resourceName, resource);
                }
            };
            engine.getScheduler().scheduleEvent(pickupEvent);

            // Add empty travel time to entity's non-value added time (waiting for
            // transport)
            entity.addNonValueAddedTime(emptyTravelTime);

        } else {
            // Resource not available - release reservation and queue
            if (resource != null) {
                if (destinationLoc != null) {
                    destinationLoc.releaseReservation();
                }
                entity.setPendingDestination(destination);
                resource.addToQueue(entity);
                entity.addWaitTime(1.0);
            }
        }
    }

    private void performLoadedMove(Entity entity, String destination, String resourceName, Resource resource) {
        double currentTime = engine.getClock().getCurrentTime();

        // ⚠️ CRÍTICO: NO liberar la ubicación ahora - implementando Blk=1 de ProModel
        // En ProModel, Blk=1 significa que la entidad bloquea la locación durante el
        // movimiento
        // Solo se libera después de "THEN FREE" (al completar el movimiento)
        Location from = entity.getCurrentLocation();

        String currentLocation = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName()
                : "UNKNOWN";
        double moveTime = calculateMoveTime(currentLocation, destination, resourceName);

        if (animationController != null) {
            animationController.animateEntityMovement(entity, currentLocation, destination, resourceName, () -> {
                // Animation callback (optional, logic handled in event)
            });
        }

        Event moveEvent = new Event(currentTime + moveTime, 0,
                "Move " + entity.getType().getName() + " to " + destination) {
            @Override
            public void execute() {
                // ⚠️ AHORA SÍ - Liberar locación origen DESPUÉS del movimiento (THEN FREE)
                if (from != null) {
                    from.exit(engine.getClock().getCurrentTime());
                    checkAndPromoteFromQueue(from);
                    notifyLocationAvailable(from.getName());
                }

                engine.notifyResourceReleased(resource, entity);

                // Record resource trip statistics
                engine.getStatistics().recordResourceTrip(resourceName, moveTime);

                Location to = engine.getLocation(destination);
                if (from != null && to != null) {
                    engine.notifyEntityMove(entity, from, to);
                }

                // Update resource location to destination
                resource.setCurrentLocation(destination);

                if (animationController == null) {
                    resource.release(engine.getClock().getCurrentTime());
                    checkResourceQueue(resource);
                    handleArrival(entity, destination);
                } else {
                    // If animation is present, we still need to release logic here
                    // or ensure animation callback handles it.
                    // For safety/consistency with previous logic:
                    resource.release(engine.getClock().getCurrentTime());
                    checkResourceQueue(resource);
                    handleArrival(entity, destination);
                }
            }
        };

        engine.getScheduler().scheduleEvent(moveEvent);
        entity.addNonValueAddedTime(moveTime);
    }

    private void handleExit(Entity entity) {
        double currentTime = engine.getClock().getCurrentTime();
        entity.addSystemTime(currentTime - entity.getCreationTime());
        Location from = entity.getCurrentLocation();
        if (from != null) {
            from.exit(currentTime);
            checkAndPromoteFromQueue(from);
            notifyLocationAvailable(from.getName()); // Notify that space is free
            engine.notifyEntityExit(entity, from);
        }
        engine.getStatistics().recordEntityExit(entity);
    }

    private void updateGUILocationOccupancy(String locationName) {
        if (animationController != null) {
            Location location = engine.getLocation(locationName);
            if (location != null) {
                int occupancy = location.getCurrentOccupancy();
                int capacity = location.getType().capacity();
                if (animationController.getLocationNodes().containsKey(locationName)) {
                    animationController.getLocationNodes().get(locationName).setOccupancy(occupancy);
                    animationController.getLocationNodes().get(locationName).setCapacity(capacity);
                }
            }
        }
    }

    private RoutingRule getRoutingRule(String locationName, String entityType) {
        return createRoutingRuleForLocation(locationName, entityType);
    }

    private RoutingRule createRoutingRuleForLocation(String locationName, String entityType) {
        switch (locationName) {
            case "ALMACEN_MP":
                return new RoutingRule("HORNO", 1.0, 1, "FIRST", "GRUA_VIAJERA");
            case "HORNO":
                return new RoutingRule("BANDA_1", 1.0, 1, "FIRST", "GRUA_VIAJERA");
            case "BANDA_1":
                return new RoutingRule("CARGA", 1.0, 1, "FIRST", null);
            case "CARGA":
                return new RoutingRule("TORNEADO", 1.0, 1, "FIRST", "ROBOT");
            case "TORNEADO":
                return new RoutingRule("FRESADO", 1.0, 1, "FIRST", "ROBOT");
            case "FRESADO":
                return new RoutingRule("TALADRO", 1.0, 1, "FIRST", "ROBOT");
            case "TALADRO":
                return new RoutingRule("RECTIFICADO", 1.0, 1, "FIRST", "ROBOT");
            case "RECTIFICADO":
                return new RoutingRule("DESCARGA", 1.0, 1, "FIRST", "ROBOT");
            case "DESCARGA":
                return new RoutingRule("BANDA_2", 1.0, 1, "FIRST", null);
            case "BANDA_2":
                return new RoutingRule("INSPECCION", 1.0, 1, "FIRST", null);
            case "INSPECCION":
                return new RoutingRule("SALIDA", 1.0, 1, "FIRST", null);
            case "SALIDA":
                return new RoutingRule("EXIT", 1.0, 1, "EXIT", null);
            default:
                return null;
        }
    }

    private double calculateMoveTime(String from, String to, String resourceName) {
        double distance = calculateDistance(from, to);
        Resource resource = engine.getResource(resourceName);
        double speed = resource != null ? resource.getType().speedMetersPerMinute() : 150.0; // Default to entity speed
                                                                                             // if null

        if (speed > 0)
            return distance / speed;
        return 1.0;
    }

    private double calculateDistance(String from, String to) {
        if (from.equals(to))
            return 0.0;

        // Map locations to node indices for Red_Robot (Linear:
        // Carga-Torneado-Fresado-Taladro-Rectificado-Descarga)
        List<String> robotPath = Arrays.asList("CARGA", "TORNEADO", "FRESADO", "TALADRO", "RECTIFICADO", "DESCARGA");
        int idxFrom = robotPath.indexOf(from);
        int idxTo = robotPath.indexOf(to);

        if (idxFrom != -1 && idxTo != -1) {
            // Calculate distance along the path
            double totalDist = 0;
            int start = Math.min(idxFrom, idxTo);
            int end = Math.max(idxFrom, idxTo);

            // Distances between segments:
            // Carga-Torneado: 20
            // Torneado-Fresado: 15
            // Fresado-Taladro: 15
            // Taladro-Rectificado: 15
            // Rectificado-Descarga: 20
            double[] segmentDists = { 20.0, 15.0, 15.0, 15.0, 20.0 };

            for (int i = start; i < end; i++) {
                totalDist += segmentDists[i];
            }
            return totalDist;
        }

        // Map locations to node indices for Red_Grua (Linear: Almacen-Horno-Banda_1)
        List<String> gruaPath = Arrays.asList("ALMACEN_MP", "HORNO", "BANDA_1");
        idxFrom = gruaPath.indexOf(from);
        idxTo = gruaPath.indexOf(to);

        if (idxFrom != -1 && idxTo != -1) {
            double totalDist = 0;
            int start = Math.min(idxFrom, idxTo);
            int end = Math.max(idxFrom, idxTo);

            // Almacen-Horno: 10
            // Horno-Banda_1: 15
            double[] segmentDists = { 10.0, 15.0 };

            for (int i = start; i < end; i++) {
                totalDist += segmentDists[i];
            }
            return totalDist;
        }

        // Conveyors (Bandas)
        if (from.equals("BANDA_1") || to.equals("BANDA_1"))
            return 30.0;
        if (from.equals("BANDA_2") || to.equals("BANDA_2"))
            return 30.0;

        return 0.0;
    }

    private boolean isPath(String from, String to, String loc1, String loc2) {
        return (from.equals(loc1) && to.equals(loc2)) || (from.equals(loc2) && to.equals(loc1));
    }
}
