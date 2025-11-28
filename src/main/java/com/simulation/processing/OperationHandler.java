package com.simulation.processing; // Declaración del paquete

import com.simulation.core.Event;
import com.simulation.core.SimulationEngine;
import com.simulation.entities.Entity;
import com.simulation.entities.EntityType;
import com.simulation.gui.AnimationController;
import com.simulation.locations.Location;
import com.simulation.resources.Resource;

import java.util.*;

public class OperationHandler { // Clase que maneja operaciones de la simulación
    private final SimulationEngine engine; // Motor de simulación
    private final Random random; // Generador de números aleatorios
    // Colas para JOIN operations
    private final Map<String, Queue<Entity>> joinQueues; // Mapa de colas para operaciones JOIN
    private final Map<String, Integer> joinRequirements; // Mapa de requisitos numéricos para JOIN
    private AnimationController animationController; // Controlador de animación

    public OperationHandler(SimulationEngine engine) { // Constructor del manejador de operaciones
        this.engine = engine; // Asigna el motor
        this.random = new Random(); // Inicializa el generador aleatorio
        this.joinQueues = new HashMap<>(); // Inicializa mapa de colas JOIN
        this.joinRequirements = new HashMap<>(); // Inicializa mapa de requisitos JOIN
        this.animationController = null; // Inicializa controlador como null

        initializeJoinRequirements(); // Configura los requisitos JOIN
    }

    public void setAnimationController(AnimationController controller) { // Establece el controlador de animación
        this.animationController = controller; // Asigna el controlador recibido
    }

    private void initializeJoinRequirements() { // Inicializa los requisitos para operaciones JOIN
        // COCCION: 1 GRANOS_DE_CEBADA + 4 LUPULO
        joinQueues.put("COCCION_GRANOS_DE_CEBADA", new LinkedList<>()); // Crea cola para granos en COCCION
        joinQueues.put("COCCION_LUPULO", new LinkedList<>()); // Crea cola para lúpulo en COCCION
        joinRequirements.put("COCCION_LUPULO", 4); // Establece que se requieren 4 lúpulos

        // FERMENTACION: 1 MOSTO + 2 LEVADURA
        joinQueues.put("FERMENTACION_MOSTO", new LinkedList<>()); // Crea cola para mosto en FERMENTACION
        joinQueues.put("FERMENTACION_LEVADURA", new LinkedList<>()); // Crea cola para levadura en FERMENTACION
        joinRequirements.put("FERMENTACION_LEVADURA", 2); // Establece que se requieren 2 levaduras

        // EMPACADO: 1 CAJA_VACIA + 6 BOTELLA_CON_CERVEZA
        joinQueues.put("EMPACADO_CAJA_VACIA", new LinkedList<>()); // Crea cola para cajas vacías en EMPACADO
        joinQueues.put("EMPACADO_BOTELLA_CON_CERVEZA", new LinkedList<>()); // Crea cola para botellas en EMPACADO
        joinRequirements.put("EMPACADO_BOTELLA_CON_CERVEZA", 6); // Establece que se requieren 6 botellas

        // ACCUM para ALMACENAJE: acumular 6 cajas
        joinQueues.put("ALMACENAJE_ACCUM", new LinkedList<>()); // Crea cola para acumulación en ALMACENAJE
        joinRequirements.put("ALMACENAJE_ACCUM", 6); // Establece que se acumulan 6 cajas
    }

    /**
     * Maneja la lógica ACCUM - acumula entidades hasta alcanzar cantidad requerida
     */
    private void handleAccumulate(Entity entity, String fromLocation, String destination, // Maneja acumulación de
                                  // entidades
                                  int quantity, String resourceName) {
        String accumKey = fromLocation + "_ACCUM"; // Crea clave para la cola de acumulación
        Queue<Entity> accumQueue = joinQueues.get(accumKey); // Obtiene la cola de acumulación

        if (accumQueue != null) { // Verifica que la cola existe
            accumQueue.add(entity); // Agrega entidad a la cola

            // Cuando se acumulan suficientes, liberar UNA entidad (no todas)
            // Las demás siguen esperando en cola para acumularse de nuevo
            if (accumQueue.size() >= quantity) { // Si se alcanzó la cantidad requerida
                // Solo enviar la primera entidad del batch
                Entity firstEntity = accumQueue.poll(); // Remueve la primera entidad

                if (resourceName != null && !resourceName.isEmpty()) { // Si requiere recurso
                    moveWithResource(firstEntity, destination, resourceName); // Mueve con recurso
                } else { // Si no requiere recurso
                    handleArrival(firstEntity, destination); // Maneja llegada directa
                }
            }
        }
    }

    public void handleArrival(Entity entity, String locationName) { // Maneja la llegada de una entidad a una ubicación
        Location location = engine.getLocation(locationName); // Obtiene la ubicación
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

        // Solo registrar entrada si NO es transformada y es primera vez
        if (entity.getEntryTime() == 0) { // Si es primera vez que entra al sistema
            entity.setEntryTime(currentTime); // Establece tiempo de entrada
            if (!entity.isTransformed()) { // Si NO es transformada
                engine.getStatistics().recordEntityEntry(entity); // Registra entrada en estadísticas
                // Notificar creación de entidad
                engine.notifyEntityCreated(entity, location); // Notifica creación a observadores
            }
        }
        location.enter(entity, currentTime); // Entidad entra a la ubicación

        // Notificar llegada a locación
        engine.notifyEntityArrival(entity, location); // Notifica llegada a observadores

        // Solo contar entrada a locación si NO es entidad secundaria de JOIN
        // Entidades secundarias: LUPULO (a COCCION), LEVADURA (a FERMENTACION), BOTELLA
        // (a EMPACADO)
        String entityType = entity.getType().getName(); // Obtiene tipo de entidad
        boolean isSecondaryJoinEntity = // Determina si es entidad secundaria en JOIN
                (locationName.equals("COCCION") && entityType.equals("LUPULO")) || // Lúpulo es secundario
                        (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) || // Levadura es
                        // secundaria
                        (locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA")); // Botella es
        // secundaria

        if (!isSecondaryJoinEntity) { // Si NO es secundaria
            engine.getStatistics().recordLocationEntry(locationName); // Registra entrada a ubicación
        }

        scheduleProcessing(entity, locationName); // Programa el procesamiento

        // Actualizar GUI si está disponible
        if (animationController != null) { // Si hay GUI
            updateGUILocationOccupancy(locationName); // Actualiza ocupación en GUI
        }
    }

    public void scheduleProcessing(Entity entity, String locationName) { // Programa el procesamiento de una entidad
        ProcessingRule rule = engine.getProcessingRule(locationName); // Obtiene regla de procesamiento
        if (rule != null) { // Si existe regla
            double processingTime = rule.getProcessingTime(); // Obtiene tiempo de procesamiento
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

            Event processingEvent = new Event(currentTime + processingTime, 0, // Crea evento de procesamiento
                    "Process " + entity.getType().getName() + " at " + locationName) {
                @Override // Sobrescribe execute
                public void execute() { // Método que se ejecuta cuando ocurre el evento
                    completeProcessing(entity, locationName); // Completa el procesamiento
                }
            };

            engine.getScheduler().scheduleEvent(processingEvent); // Programa el evento

            if (processingTime > 0) { // Si hay tiempo de procesamiento
                entity.addValueAddedTime(processingTime); // Agrega tiempo con valor
                engine.getStatistics().recordLocationProcessingTime(locationName, processingTime); // Registra tiempo en
                // estadísticas
            }
        }
    }

    public void completeProcessing(Entity entity, String locationName) { // Completa el procesamiento de una entidad
        Location location = engine.getLocation(locationName); // Obtiene la ubicación
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

        location.exit(currentTime); // Entidad sale de la ubicación

        String entityType = entity.getType().getName(); // Obtiene tipo de entidad

        if (isJoinLocation(locationName, entityType)) { // Si es ubicación JOIN
            handleJoinLogic(entity, locationName); // Maneja lógica JOIN
        } else { // Si no es JOIN
            routeEntity(entity, locationName); // Rutea la entidad
        }

        // Actualizar GUI si está disponible
        if (animationController != null) { // Si hay GUI
            updateGUILocationOccupancy(locationName); // Actualiza ocupación en GUI
        }
    }

    private boolean isJoinLocation(String locationName, String entityType) { // Determina si ubicación requiere JOIN
        // Solo las entidades PRINCIPALES que inician el JOIN son manejadas aquí
        if (locationName.equals("COCCION") && entityType.equals("GRANOS_DE_CEBADA")) { // Granos en COCCION
            return true; // Es JOIN
        }
        if (locationName.equals("FERMENTACION") && entityType.equals("MOSTO")) { // Mosto en FERMENTACION
            return true; // Es JOIN
        }
        if (locationName.equals("EMPACADO") && entityType.equals("CAJA_VACIA")) { // Caja en EMPACADO
            return true; // Es JOIN
        }
        // LUPULO, LEVADURA y BOTELLA simplemente se encolan (manejado en
        // handleJoinLogic)
        if (locationName.equals("COCCION") && entityType.equals("LUPULO")) { // Lúpulo en COCCION
            return true; // Es JOIN
        }
        if (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) { // Levadura en FERMENTACION
            return true; // Es JOIN
        }
        return locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA"); // Botella en EMPACADO es
        // JOIN
    }

    private void handleJoinLogic(Entity entity, String locationName) { // Maneja la lógica de operaciones JOIN
        String entityType = entity.getType().getName(); // Obtiene tipo de entidad
        String queueKey = locationName + "_" + entityType; // Crea clave para la cola

        joinQueues.get(queueKey).add(entity); // Agrega entidad a su cola JOIN

        // Solo intentar procesar el JOIN si es la entidad PRINCIPAL
        if (locationName.equals("COCCION") && entityType.equals("GRANOS_DE_CEBADA")) { // Si es grano en COCCION
            processJoinCoccion(); // Procesa JOIN de COCCION
        } else if (locationName.equals("FERMENTACION") && entityType.equals("MOSTO")) { // Si es mosto en FERMENTACION
            processJoinFermentacion(); // Procesa JOIN de FERMENTACION
        } else if (locationName.equals("EMPACADO") && entityType.equals("CAJA_VACIA")) { // Si es caja en EMPACADO
            processJoinEmpacado(); // Procesa JOIN de EMPACADO
        }
    }

    private void processJoinCoccion() { // Procesa JOIN en COCCION: 1 grano + 4 lúpulos = 1 mosto
        Queue<Entity> granosQueue = joinQueues.get("COCCION_GRANOS_DE_CEBADA"); // Obtiene cola de granos
        Queue<Entity> lupuloQueue = joinQueues.get("COCCION_LUPULO"); // Obtiene cola de lúpulos

        while (!granosQueue.isEmpty() && lupuloQueue.size() >= 4) { // Mientras haya 1 grano y 4 lúpulos
            Entity granosEntity = granosQueue.poll(); // Remueve grano

            double totalValueAdded = granosEntity.getTotalValueAddedTime(); // Obtiene tiempo con valor del grano
            double totalNonValueAdded = granosEntity.getTotalNonValueAddedTime(); // Obtiene tiempo sin valor del grano
            double entryTime = granosEntity.getEntryTime(); // Guarda tiempo de entrada
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

            granosEntity.addSystemTime(currentTime - granosEntity.getEntryTime()); // Calcula tiempo en sistema
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 4; i++) { // Procesa 4 lúpulos
                Entity lupuloEntity = lupuloQueue.poll(); // Remueve lúpulo
                totalValueAdded += lupuloEntity.getTotalValueAddedTime(); // Acumula tiempo con valor
                totalNonValueAdded += lupuloEntity.getTotalNonValueAddedTime(); // Acumula tiempo sin valor

                lupuloEntity.addSystemTime(currentTime - lupuloEntity.getEntryTime()); // Calcula tiempo en sistema
                engine.getStatistics().recordEntityExit(lupuloEntity); // Registra salida del lúpulo
            }

            // Crear MOSTO como transformación
            EntityType mostoType = engine.getEntityType("MOSTO"); // Obtiene tipo MOSTO
            Entity mosto = new Entity(mostoType, true); // Crea entidad MOSTO transformada
            mosto.setEntryTime(entryTime); // Hereda tiempo de entrada
            mosto.addValueAddedTime(totalValueAdded); // Hereda tiempo con valor
            mosto.addNonValueAddedTime(totalNonValueAdded); // Hereda tiempo sin valor

            engine.getStatistics().recordEntityEntry(mosto); // Registra entrada del MOSTO

            handleArrival(mosto, "ENFRIAMIENTO"); // Envía MOSTO a ENFRIAMIENTO
        }
    }

    private void processJoinFermentacion() { // Procesa JOIN en FERMENTACION: 1 mosto + 2 levaduras = 1 cerveza
        Queue<Entity> mostoQueue = joinQueues.get("FERMENTACION_MOSTO"); // Obtiene cola de mostos
        Queue<Entity> levaduraQueue = joinQueues.get("FERMENTACION_LEVADURA"); // Obtiene cola de levaduras

        while (!mostoQueue.isEmpty() && levaduraQueue.size() >= 2) { // Mientras haya 1 mosto y 2 levaduras
            Entity mostoEntity = mostoQueue.poll(); // Remueve mosto

            double totalValueAdded = mostoEntity.getTotalValueAddedTime(); // Obtiene tiempo con valor del mosto
            double totalNonValueAdded = mostoEntity.getTotalNonValueAddedTime(); // Obtiene tiempo sin valor del mosto
            double entryTime = mostoEntity.getEntryTime(); // Guarda tiempo de entrada
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

            mostoEntity.addSystemTime(currentTime - mostoEntity.getEntryTime()); // Calcula tiempo en sistema
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 2; i++) { // Procesa 2 levaduras
                Entity levaduraEntity = levaduraQueue.poll(); // Remueve levadura
                totalValueAdded += levaduraEntity.getTotalValueAddedTime(); // Acumula tiempo con valor
                totalNonValueAdded += levaduraEntity.getTotalNonValueAddedTime(); // Acumula tiempo sin valor

                levaduraEntity.addSystemTime(currentTime - levaduraEntity.getEntryTime()); // Calcula tiempo en sistema
                engine.getStatistics().recordEntityExit(levaduraEntity); // Registra salida de levadura
            }

            // Crear CERVEZA como transformación
            EntityType cervezaType = engine.getEntityType("CERVEZA"); // Obtiene tipo CERVEZA
            Entity cerveza = new Entity(cervezaType, true); // Crea entidad CERVEZA transformada
            cerveza.setEntryTime(entryTime); // Hereda tiempo de entrada
            cerveza.addValueAddedTime(totalValueAdded); // Hereda tiempo con valor
            cerveza.addNonValueAddedTime(totalNonValueAdded); // Hereda tiempo sin valor

            engine.getStatistics().recordEntityEntry(cerveza); // Registra entrada de CERVEZA

            handleArrival(cerveza, "MADURACION"); // Envía CERVEZA a MADURACION
        }
    }

    private void processJoinEmpacado() { // Procesa JOIN en EMPACADO: 1 caja + 6 botellas = 1 caja llena
        Queue<Entity> cajaQueue = joinQueues.get("EMPACADO_CAJA_VACIA"); // Obtiene cola de cajas vacías
        Queue<Entity> botellaQueue = joinQueues.get("EMPACADO_BOTELLA_CON_CERVEZA"); // Obtiene cola de botellas

        while (!cajaQueue.isEmpty() && botellaQueue.size() >= 6) { // Mientras haya 1 caja y 6 botellas
            Entity cajaEntity = cajaQueue.poll(); // Remueve caja

            double totalValueAdded = cajaEntity.getTotalValueAddedTime(); // Obtiene tiempo con valor de caja
            double totalNonValueAdded = cajaEntity.getTotalNonValueAddedTime(); // Obtiene tiempo sin valor de caja
            double entryTime = cajaEntity.getEntryTime(); // Guarda tiempo de entrada
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

            cajaEntity.addSystemTime(currentTime - cajaEntity.getEntryTime()); // Calcula tiempo en sistema
            // NO recordEntityExit - se consume en JOIN, no sale del sistema

            for (int i = 0; i < 6; i++) { // Procesa 6 botellas
                Entity botellaEntity = botellaQueue.poll(); // Remueve botella
                totalValueAdded += botellaEntity.getTotalValueAddedTime(); // Acumula tiempo con valor
                totalNonValueAdded += botellaEntity.getTotalNonValueAddedTime(); // Acumula tiempo sin valor

                botellaEntity.addSystemTime(currentTime - botellaEntity.getEntryTime()); // Calcula tiempo en sistema
                engine.getStatistics().recordEntityExit(botellaEntity); // Registra salida de botella
            }

            // Crear CAJA_CON_CERVEZAS como transformación
            EntityType cajaLlenaType = engine.getEntityType("CAJA_CON_CERVEZAS"); // Obtiene tipo CAJA_CON_CERVEZAS
            Entity cajaLlena = new Entity(cajaLlenaType, true); // Crea entidad CAJA_CON_CERVEZAS transformada
            cajaLlena.setEntryTime(entryTime); // Hereda tiempo de entrada
            cajaLlena.addValueAddedTime(totalValueAdded); // Hereda tiempo con valor
            cajaLlena.addNonValueAddedTime(totalNonValueAdded); // Hereda tiempo sin valor

            // Registrar como nueva entidad al sistema (producto final)
            engine.getStatistics().recordEntityEntry(cajaLlena); // Registra entrada de caja llena

            // WAIT 10 min en EMPACADO (procesamiento)
            cajaLlena.addValueAddedTime(10); // Agrega 10 minutos de procesamiento
            engine.getStatistics().recordLocationProcessingTime("EMPACADO", 10); // Registra tiempo en estadísticas

            // Programar movimiento a ALMACENAJE después de 10 minutos
            double currentTimeAfterJoin = engine.getClock().getCurrentTime(); // Obtiene tiempo actual
            final Entity finalCajaLlena = cajaLlena; // Referencia final para closure

            Event packingEvent = new Event(currentTimeAfterJoin + 10, 0, // Crea evento 10 minutos después
                    "Pack caja at EMPACADO") {
                @Override // Sobrescribe execute
                public void execute() { // Método que se ejecuta
                    moveWithResource(finalCajaLlena, "ALMACENAJE", "OPERADOR_EMPACADO"); // Mueve a ALMACENAJE con
                    // operador
                }
            };
            engine.getScheduler().scheduleEvent(packingEvent); // Programa el evento
        }
    }

    public void routeEntity(Entity entity, String fromLocation) { // Rutea entidad a su próximo destino
        String entityType = entity.getType().getName(); // Obtiene tipo de entidad
        RoutingRule route = getRoutingRule(fromLocation, entityType); // Obtiene regla de enrutamiento

        if (route != null) { // Si existe regla
            String destination = route.destinationLocation(); // Obtiene destino
            String moveLogic = route.moveLogic(); // Obtiene lógica de movimiento

            if ("EXIT".equals(destination)) { // Si destino es EXIT
                handleExit(entity); // Maneja salida
            } else if ("JOIN".equals(moveLogic)) { // Si lógica es JOIN
                // La entidad va a una locación para participar en un JOIN
                if (route.resourceName() != null && !route.resourceName().isEmpty()) {
                    moveWithResource(entity, destination, route.resourceName());
                } else {
                    Location from = engine.getLocation(fromLocation);
                    Location to = engine.getLocation(destination);
                    engine.notifyEntityMove(entity, from, to);
                    handleArrival(entity, destination);
                }
            } else { // Otros casos
                double probability = route.probability(); // Obtiene probabilidad
                if (random.nextDouble() <= probability) { // Si se cumple probabilidad
                    int quantity = route.quantity(); // Obtiene cantidad

                    if ("ACCUM".equals(route.moveLogic()) && quantity > 1) { // Si es ACCUM
                        // ACCUM: acumular entidades antes de mover en batch
                        handleAccumulate(entity, fromLocation, destination, quantity, route.resourceName()); // Acumula
                        // entidades
                    } else if (quantity > 1) { // Si genera múltiples entidades
                        // FIRST 6: NO registrar exit (es transformación interna), SÍ registrar entries
                        double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual
                        entity.addSystemTime(currentTime - entity.getEntryTime()); // Calcula tiempo en sistema
                        // NO recordEntityExit - la entidad se transforma, no sale del sistema

                        // Crear 6 entidades nuevas transformadas
                        for (int i = 0; i < quantity; i++) { // Itera por cantidad
                            Entity newEntity = createTransformedEntity(entity, destination); // Crea entidad
                            // transformada
                            engine.getStatistics().recordEntityEntry(newEntity); // Registra entrada

                            if (route.resourceName() != null && !route.resourceName().isEmpty()) { // Si requiere
                                // recurso
                                moveWithResource(newEntity, destination, route.resourceName()); // Mueve con recurso
                            } else { // Si no requiere recurso
                                Location from = engine.getLocation(fromLocation);
                                Location to = engine.getLocation(destination);
                                engine.notifyEntityMove(newEntity, from, to);
                                handleArrival(newEntity, destination); // Maneja llegada
                            }
                        }
                    } else { // Movimiento simple 1 a 1
                        if (route.resourceName() != null && !route.resourceName().isEmpty()) { // Si requiere recurso
                            moveWithResource(entity, destination, route.resourceName()); // Mueve con recurso
                        } else { // Si no requiere recurso
                            Location from = engine.getLocation(fromLocation);
                            Location to = engine.getLocation(destination);
                            engine.notifyEntityMove(entity, from, to);
                            handleArrival(entity, destination); // Maneja llegada
                        }
                    }
                } else { // Si no cumple probabilidad
                    handleExit(entity); // Entidad sale del sistema
                }
            }
        } else { // Si no hay regla
            handleExit(entity); // Entidad sale por defecto
        }
    }

    private Entity createTransformedEntity(Entity original, String destinationLocation) { // Crea entidad transformada
        EntityType newType = null; // Inicializa nuevo tipo como null

        if (destinationLocation.equals("ETIQUETADO")) { // Si destino es ETIQUETADO
            newType = engine.getEntityType("BOTELLA_CON_CERVEZA"); // Tipo es BOTELLA_CON_CERVEZA
        }

        if (newType != null) { // Si se determinó nuevo tipo
            Entity newEntity = new Entity(newType, true); // Crea entidad transformada
            newEntity.setEntryTime(original.getEntryTime()); // Hereda tiempo de entrada
            newEntity.addValueAddedTime(original.getTotalValueAddedTime()); // Hereda tiempo con valor
            newEntity.addNonValueAddedTime(original.getTotalNonValueAddedTime()); // Hereda tiempo sin valor
            newEntity.addWaitTime(original.getTotalWaitTime()); // Hereda tiempo de espera
            return newEntity; // Retorna nueva entidad
        }

        return original; // Retorna original si no hay transformación
    }

    private void moveWithResource(Entity entity, String destination, String resourceName) { // Mueve entidad usando
        // recurso
        Resource resource = engine.getResource(resourceName); // Obtiene el recurso
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual

        if (resource != null && resource.isAvailable()) { // Si recurso existe y está disponible
            resource.acquire(currentTime); // Adquiere el recurso

            // Notificar que el recurso fue adquirido
            engine.notifyResourceAcquired(resource, entity); // Notifica adquisición

            double moveTime = 2.0; // Tiempo de movimiento fijo

            // Animar movimiento si hay GUI disponible
            if (animationController != null) { // Si hay GUI
                String currentLocation = entity.getCurrentLocation() != null ? // Obtiene ubicación actual
                        entity.getCurrentLocation().getType().name() : "UNKNOWN";

                animationController.animateEntityMovement( // Inicia animación
                        entity,
                        currentLocation,
                        destination,
                        resourceName,
                        () -> { // Callback cuando termina animación
                            // Callback cuando termina la animación
                            resource.release(engine.getClock().getCurrentTime()); // Libera recurso
                            handleArrival(entity, destination); // Maneja llegada
                        });
            }

            Event moveEvent = new Event(currentTime + moveTime, 0, // Crea evento de movimiento
                    "Move " + entity.getType().getName() + " to " + destination) {
                @Override // Sobrescribe execute
                public void execute() { // Método que se ejecuta
                    // Notificar liberación de recurso
                    engine.notifyResourceReleased(resource, entity); // Notifica liberación

                    Location from = entity.getCurrentLocation(); // Obtiene ubicación origen
                    Location to = engine.getLocation(destination); // Obtiene ubicación destino
                    if (from != null && to != null) { // Si ambas existen
                        // Notificar movimiento de entidad
                        engine.notifyEntityMove(entity, from, to); // Notifica movimiento
                    }

                    // Solo ejecutar si NO hay GUI (modo consola)
                    if (animationController == null) { // Si no hay GUI
                        resource.release(engine.getClock().getCurrentTime()); // Libera recurso
                        handleArrival(entity, destination); // Maneja llegada
                    }
                }
            };

            engine.getScheduler().scheduleEvent(moveEvent); // Programa evento
            entity.addNonValueAddedTime(moveTime); // Agrega tiempo sin valor
        } else { // Si recurso no está disponible
            if (resource != null) { // Si recurso existe pero está ocupado
                resource.addToQueue(entity); // Agrega a cola del recurso
                entity.addWaitTime(1.0); // Agrega tiempo de espera
            }
        }
    }

    private void handleExit(Entity entity) { // Maneja salida de entidad del sistema
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual
        entity.addSystemTime(currentTime - entity.getEntryTime()); // Calcula tiempo total en sistema

        Location from = entity.getCurrentLocation(); // Obtiene ubicación actual
        if (from != null) { // Si tiene ubicación
            // Notificar salida del sistema
            engine.notifyEntityExit(entity, from); // Notifica salida
        }

        engine.getStatistics().recordEntityExit(entity); // Registra salida en estadísticas

        // Actualizar GUI si está disponible
        if (animationController != null) { // Si hay GUI
            updateGUIEntityCount(entity.getType().getName()); // Actualiza contador en GUI
        }
    }

    private RoutingRule getRoutingRule(String locationName, String entityType) { // Obtiene regla de enrutamiento
        return createRoutingRuleForLocation(locationName, entityType); // Delega a método de creación
    }

    private RoutingRule createRoutingRuleForLocation(String locationName, String entityType) { // Define reglas de
        // enrutamiento
        switch (locationName) { // Evalúa ubicación origen
            case "SILO_GRANDE": // Desde SILO_GRANDE
                return new RoutingRule("MALTEADO", 1.0, 1, "FIRST", null); // Va a MALTEADO
            case "MALTEADO": // Desde MALTEADO
                return new RoutingRule("SECADO", 1.0, 1, "FIRST", "OPERADOR_RECEPCION"); // Va a SECADO
            case "SECADO": // Desde SECADO
                return new RoutingRule("MOLIENDA", 1.0, 1, "FIRST", "OPERADOR_RECEPCION"); // Va a MOLIENDA
            case "MOLIENDA": // Desde MOLIENDA
                return new RoutingRule("MACERADO", 1.0, 1, "FIRST", null); // Va a MACERADO
            case "MACERADO": // Desde MACERADO
                return new RoutingRule("FILTRADO", 1.0, 1, "FIRST", null); // Va a FILTRADO
            case "FILTRADO": // Desde FILTRADO
                return new RoutingRule("COCCION", 1.0, 1, "FIRST", null); // Va a COCCION
            case "SILO_LUPULO": // Desde SILO_LUPULO
                return new RoutingRule("COCCION", 1.0, 1, "JOIN", "OPERADOR_LUPULO"); // Va a COCCION para JOIN
            case "ENFRIAMIENTO": // Desde ENFRIAMIENTO
                return new RoutingRule("FERMENTACION", 1.0, 1, "FIRST", null); // Va a FERMENTACION
            case "SILO_LEVADURA": // Desde SILO_LEVADURA
                return new RoutingRule("FERMENTACION", 1.0, 1, "JOIN", "OPERADOR_LEVADURA"); // Va a FERMENTACION para
            // JOIN
            case "MADURACION": // Desde MADURACION
                return new RoutingRule("INSPECCION", 1.0, 1, "FIRST", null); // Va a INSPECCION
            case "INSPECCION": // Desde INSPECCION
                return new RoutingRule("EMBOTELLADO", 0.9, 1, "FIRST", null); // Va a EMBOTELLADO con 90% probabilidad
            case "EMBOTELLADO": // Desde EMBOTELLADO
                return new RoutingRule("ETIQUETADO", 1.0, 6, "FIRST", null); // Va a ETIQUETADO generando 6 botellas
            case "ETIQUETADO": // Desde ETIQUETADO
                return new RoutingRule("EMPACADO", 1.0, 1, "JOIN", null); // Va a EMPACADO para JOIN
            case "ALMACEN_CAJAS": // Desde ALMACEN_CAJAS
                return new RoutingRule("EMPACADO", 1.0, 1, "FIRST", null); // Va a EMPACADO
            case "ALMACENAJE": // Desde ALMACENAJE
                return new RoutingRule("MERCADO", 1.0, 6, "ACCUM", "CAMION"); // Va a MERCADO acumulando 6
            case "MERCADO": // Desde MERCADO
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null); // Sale del sistema
            default: // Ubicación no especificada
                return new RoutingRule("EXIT", 1.0, 1, "FIRST", null); // Sale por defecto
        }
    }

    // Métodos auxiliares para actualizar la GUI
    private void updateGUILocationOccupancy(String locationName) { // Actualiza ocupación de ubicación en GUI
        if (animationController != null) { // Si hay controlador de animación
            Location location = engine.getLocation(locationName); // Obtiene ubicación
            if (location != null) { // Si ubicación existe
                int occupancy = location.getCurrentOccupancy(); // Obtiene ocupación actual
                int capacity = location.getType().capacity(); // Obtiene capacidad

                // Actualizar el nodo visual de la locación
                if (animationController.getLocationNodes().containsKey(locationName)) { // Si existe nodo visual
                    animationController.getLocationNodes().get(locationName).setOccupancy(occupancy); // Actualiza
                    // ocupación
                    animationController.getLocationNodes().get(locationName).setCapacity(capacity); // Actualiza
                    // capacidad
                }
            }
        }
    }

    private void updateGUIEntityCount(String entityType) { // Actualiza contador de entidades en GUI
        if (animationController != null) { // Si hay controlador de animación
            // Esta actualización se puede expandir para mostrar contadores en tiempo real
        }
    }
}
