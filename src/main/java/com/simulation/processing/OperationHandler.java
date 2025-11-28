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
    private AnimationController animationController;

    public OperationHandler(SimulationEngine engine) {
        this.engine = engine;
        this.random = new Random();
        this.joinQueues = new HashMap<>();
        this.joinRequirements = new HashMap<>();
        this.blockedEntities = new HashMap<>();
        this.animationController = null;

        initializeJoinRequirements();
    }

    public void setAnimationController(AnimationController controller) {
        this.animationController = controller;
    }

    private void initializeJoinRequirements() {
        joinQueues.put("HORNO_ACCUM", new LinkedList<>());
        joinRequirements.put("HORNO_ACCUM", 10);
    }

    private void notifyLocationAvailable(String locationName) {
        if (blockedEntities.containsKey(locationName)) {
            Queue<Entity> blockedQueue = blockedEntities.get(locationName);
            if (!blockedQueue.isEmpty()) {
                Location destination = engine.getLocation(locationName);
                if (destination.canAccept()) {
                    Entity entity = blockedQueue.poll();
                    // Retry routing for the unblocked entity
                    // We need to know where it came from to re-trigger the route logic correctly
                    // For simplicity in this specific model, we can infer or store the source.
                    // Better approach: Store a wrapper or just retry the move logic.
                    // Since routeEntity handles the move, we just need to call it again.
                    // But routeEntity requires 'fromLocation'.
                    // Let's store a "BlockedEntity" object or just use the entity's current
                    // location.

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

            if (accumQueue != null) {
                accumQueue.add(entity);

                if (accumQueue.size() >= 10) {
                    List<Entity> batchEntities = new ArrayList<>();
                    for (int i = 0; i < 10; i++) {
                        Entity batchEntity = accumQueue.poll();
                        if (batchEntity != null) {
                            batchEntities.add(batchEntity);
                        }
                    }

                    double processingTime = 100.0;
                    double currentTime = engine.getClock().getCurrentTime();

                    for (Entity batchEntity : batchEntities) {
                        batchEntity.addValueAddedTime(processingTime);
                    }
                    engine.getStatistics().recordLocationProcessingTime(locationName, processingTime);

                    Event processingEvent = new Event(currentTime + processingTime, 0, "Process batch of 10 in HORNO") {
                        @Override
                        public void execute() {
                            Location hornoLocation = engine.getLocation("HORNO");
                            double baseCompletionTime = engine.getClock().getCurrentTime();

                            for (int i = 0; i < batchEntities.size(); i++) {
                                Entity batchEntity = batchEntities.get(i);
                                double exitDelay = i * 0.1;

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

                                if (("BANDA_1".equals(fromLocation) && "CARGA".equals(destination)) ||
                                        ("DESCARGA".equals(fromLocation) && "BANDA_2".equals(destination))) {

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
        if (resource.isAvailable() && resource.getQueueSize() > 0) {
            Entity nextEntity = resource.removeFromQueue();
            if (nextEntity != null) {
                String destination = nextEntity.getPendingDestination();
                moveWithResource(nextEntity, destination, resource.getName());
            }
        }
    }

    private void moveWithResource(Entity entity, String destination, String resourceName) {
        Resource resource = engine.getResource(resourceName);
        double currentTime = engine.getClock().getCurrentTime();

        if (resource != null && resource.isAvailable()) {
            resource.acquire(currentTime);
            engine.notifyResourceAcquired(resource, entity);

            // 1. Calculate Empty Travel Time
            String resourceLoc = resource.getCurrentLocation();
            String entityLoc = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName() : "UNKNOWN";
            double emptyTravelTime = calculateMoveTime(resourceLoc, entityLoc, resourceName);

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
            if (resource != null) {
                entity.setPendingDestination(destination);
                resource.addToQueue(entity);
                entity.addWaitTime(1.0);
            }
        }
    }

    private void performLoadedMove(Entity entity, String destination, String resourceName, Resource resource) {
        double currentTime = engine.getClock().getCurrentTime();

        // Entity leaves the location now that resource has arrived and picked it up
        Location from = entity.getCurrentLocation();
        if (from != null) {
            from.exit(currentTime);
            checkAndPromoteFromQueue(from);
            notifyLocationAvailable(from.getName()); // Notify that space is free
        }

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
                engine.notifyResourceReleased(resource, entity);
                Location from = entity.getCurrentLocation();
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
        entity.addSystemTime(currentTime - entity.getEntryTime());
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
        double speed = 0.0;

        if ("GRUA_VIAJERA".equals(resourceName)) {
            speed = 25.0;
        } else if ("ROBOT".equals(resourceName)) {
            speed = 45.0;
        }

        if (speed > 0)
            return distance / speed;
        return 1.0;
    }

    private double calculateDistance(String from, String to) {
        if (from.equals(to))
            return 0.0;

        // Red_Grua
        if (isPath(from, to, "ALMACEN_MP", "HORNO"))
            return 10.0;
        if (isPath(from, to, "HORNO", "BANDA_1"))
            return 15.0;

        // Red_Robot
        if (isPath(from, to, "CARGA", "TORNEADO"))
            return 20.0;
        if (isPath(from, to, "TORNEADO", "FRESADO"))
            return 15.0;
        if (isPath(from, to, "FRESADO", "TALADRO"))
            return 15.0;
        if (isPath(from, to, "TALADRO", "RECTIFICADO"))
            return 15.0;
        if (isPath(from, to, "RECTIFICADO", "DESCARGA"))
            return 20.0;

        // Default fallback (should not happen if paths are correct)
        return 0.0;
    }

    private boolean isPath(String from, String to, String loc1, String loc2) {
        return (from.equals(loc1) && to.equals(loc2)) || (from.equals(loc2) && to.equals(loc1));
    }
}
