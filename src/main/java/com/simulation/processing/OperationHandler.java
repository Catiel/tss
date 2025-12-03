package com.simulation.processing; // Declaración del paquete de procesamiento de la simulación

import com.simulation.core.Event; // Importa la clase Event para crear eventos de simulación
import com.simulation.core.SimulationEngine; // Importa el motor principal de simulación
import com.simulation.entities.Entity; // Importa la clase que representa entidades individuales
import com.simulation.entities.Entity; // Importación duplicada de Entity (redundante)
import com.simulation.gui.AnimationController; // Importa el controlador de animación gráfica
import com.simulation.locations.Location; // Importa la clase que representa ubicaciones
import com.simulation.resources.Resource; // Importa la clase que representa recursos

import java.util.*; // Importa todas las clases del paquete de utilidades de Java

public class OperationHandler { // Define la clase manejadora de operaciones de entidades
    private final SimulationEngine engine; // Referencia al motor de simulación
    private final Random random; // Generador de números aleatorios para decisiones probabilísticas
    private final Map<String, Queue<Entity>> joinQueues; // Mapa de colas para operaciones de unión y acumulación
    private final Map<String, Integer> joinRequirements; // Mapa de requisitos numéricos para operaciones de unión
    private final Map<String, Queue<Entity>> blockedEntities; // Mapa de entidades bloqueadas esperando espacio en destino
    private final Map<Entity, Double> blockStartTime; // Mapa que registra el tiempo de inicio de bloqueo
    private AnimationController animationController; // Controlador opcional de animación gráfica

    public OperationHandler(SimulationEngine engine) { // Constructor que recibe el motor de simulación
        this.engine = engine; // Asigna la referencia del motor
        this.random = new Random(); // Inicializa el generador de números aleatorios
        this.joinQueues = new HashMap<>(); // Inicializa el mapa de colas de unión
        this.joinRequirements = new HashMap<>(); // Inicializa el mapa de requisitos de unión
        this.blockedEntities = new HashMap<>(); // Inicializa el mapa de entidades bloqueadas
        this.blockStartTime = new HashMap<>(); // Inicializa el mapa de tiempos de inicio de bloqueo
        this.animationController = null; // Inicializa el controlador de animación como nulo

        initializeJoinRequirements(); // Llama al método que configura los requisitos de unión
    }

    public void setAnimationController(AnimationController controller) { // Método setter para establecer el controlador de animación
        this.animationController = controller; // Asigna el controlador recibido
    }

    private void initializeJoinRequirements() { // Método que inicializa las colas de acumulación
        joinQueues.put("HORNO_ACCUM", new LinkedList<>()); // Crea cola de acumulación para el horno
        // El tamaño del lote se obtiene dinámicamente del BatchProcessingRule
        // joinRequirements ya no se usa - ver getBatchSizeForLocation()
    }

    /**
     * Obtiene el tamaño del lote configurado para una locación.
     * Si no hay BatchProcessingRule, retorna 1 (sin acumulación).
     */
    private int getBatchSizeForLocation(String locationName) { // Método que obtiene el tamaño de lote dinámicamente
        ProcessingRule rule = engine.getProcessingRule(locationName); // Obtiene la regla de procesamiento de la ubicación
        if (rule instanceof BatchProcessingRule processingRule) { // Verifica si la regla es de tipo procesamiento por lotes
            return processingRule.getBatchSize(); // Retorna el tamaño de lote configurado
        }
        return 1; // Retorna 1 por defecto si no hay procesamiento por lotes
    }

    private void notifyLocationAvailable(String locationName) { // Método que notifica cuando una ubicación tiene espacio disponible
        if (blockedEntities.containsKey(locationName)) { // Verifica si hay entidades bloqueadas esperando esta ubicación
            Queue<Entity> blockedQueue = blockedEntities.get(locationName); // Obtiene la cola de entidades bloqueadas
            if (!blockedQueue.isEmpty()) { // Verifica si la cola no está vacía
                Location destination = engine.getLocation(locationName); // Obtiene la ubicación destino
                if (destination.canAccept()) { // Verifica si la ubicación puede aceptar una nueva entidad
                    Entity entity = blockedQueue.poll(); // Extrae la primera entidad de la cola

                    // Calculate and record blocking time
                    if (entity != null && blockStartTime.containsKey(entity)) { // Verifica si la entidad existe y tiene tiempo de inicio de bloqueo
                        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación
                        double blockedTime = currentTime - blockStartTime.get(entity); // Calcula el tiempo total de bloqueo
                        entity.addBlockingTime(blockedTime); // Añade el tiempo de bloqueo a las estadísticas de la entidad
                        blockStartTime.remove(entity); // Elimina el registro de tiempo de inicio de bloqueo
                    }

                    if (entity != null && entity.getCurrentLocation() != null) { // Verifica si la entidad y su ubicación actual existen
                        routeEntity(entity, entity.getCurrentLocation().getName()); // Rutea la entidad desde su ubicación actual
                    }
                }
            }
        }
    }

    private void handleAccumulate(Entity entity, String fromLocation, String destination, int quantity, // Método que maneja la acumulación de entidades para procesamiento en lotes
            String resourceName) { // Parámetro del nombre del recurso para el transporte
        String accumKey = fromLocation + "_ACCUM"; // Construye la clave de acumulación concatenando ubicación y sufijo
        Queue<Entity> accumQueue = joinQueues.get(accumKey); // Obtiene la cola de acumulación correspondiente

        if (accumQueue != null) { // Verifica si la cola de acumulación existe
            accumQueue.add(entity); // Añade la entidad a la cola de acumulación

            if (accumQueue.size() >= quantity) { // Verifica si se alcanzó la cantidad requerida para procesar el lote
                for (int i = 0; i < quantity; i++) { // Itera por la cantidad de entidades del lote
                    Entity batchEntity = accumQueue.poll(); // Extrae una entidad de la cola
                    if (batchEntity != null) { // Verifica si la entidad extraída no es nula
                        if (resourceName != null && !resourceName.isEmpty()) { // Verifica si se requiere un recurso para mover
                            moveWithResource(batchEntity, destination, resourceName); // Mueve la entidad usando el recurso especificado
                        } else { // Si no se requiere recurso
                            handleArrival(batchEntity, destination); // Maneja la llegada directa al destino
                        }
                    }
                }
            }
        }
    }

    public void handleArrival(Entity entity, String locationName) { // Método público que maneja la llegada de una entidad a una ubicación
        Location location = engine.getLocation(locationName); // Obtiene la ubicación destino por nombre
        if (location == null) { // Verifica si la ubicación no existe
            System.err.println("CRITICAL ERROR: Location '" + locationName + "' not found!"); // Imprime error crítico de ubicación no encontrada
            System.err.println("Available locations: " + engine.getAllLocations().keySet()); // Imprime las ubicaciones disponibles
            throw new RuntimeException("Location not found: " + locationName); // Lanza excepción de tiempo de ejecución
        }
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación

        if (entity.getEntryTime() == 0) { // Verifica si es la primera entrada de la entidad al sistema
            entity.setEntryTime(currentTime); // Establece el tiempo de entrada como el tiempo actual
            if (!entity.isTransformed()) { // Verifica si la entidad no es resultado de transformación
                engine.getStatistics().recordEntityEntry(entity); // Registra la entrada de la entidad en estadísticas
                engine.notifyEntityCreated(entity, location); // Notifica a los listeners sobre la creación de la entidad
            }
        }
        if (location.enter(entity, currentTime)) { // Intenta que la entidad entre a la ubicación
            engine.notifyEntityArrival(entity, location); // Notifica a los listeners sobre la llegada

            String entityType = entity.getType().getName(); // Obtiene el nombre del tipo de entidad
            boolean isSecondaryJoinEntity = (locationName.equals("COCCION") && entityType.equals("LUPULO")) || // Verifica si es entidad secundaria de unión en cocción
                    (locationName.equals("FERMENTACION") && entityType.equals("LEVADURA")) || // Verifica si es entidad secundaria de unión en fermentación
                    (locationName.equals("EMPACADO") && entityType.equals("BOTELLA_CON_CERVEZA")); // Verifica si es entidad secundaria de unión en empacado

            if (!isSecondaryJoinEntity) { // Si no es entidad secundaria de unión
                engine.getStatistics().recordLocationEntry(locationName); // Registra la entrada en estadísticas de ubicación
            }

            scheduleProcessing(entity, locationName); // Programa el procesamiento de la entidad en la ubicación
        } else { // Si la ubicación no puede aceptar la entidad
            // Entity is queued at the location, waiting for space
        }
        // location.exit(currentTime); // Removed to prevent premature exit
        // Routing is handled in completeProcessing or specific scheduleProcessing logic

        // if (isJoinLocation(locationName, entityType)) {
        // handleJoinLogic(entity, locationName);
        // } else {
        // routeEntity(entity, locationName);
        // }

        if (animationController != null) { // Verifica si hay controlador de animación activo
            updateGUILocationOccupancy(locationName); // Actualiza la ocupación de la ubicación en la GUI
        }
    }

    public void scheduleProcessing(Entity entity, String locationName) { // Método que programa el procesamiento de una entidad en una ubicación
        if (locationName.equals("HORNO")) { // Verifica si la ubicación es el horno
            String accumKey = "HORNO_ACCUM"; // Define la clave de acumulación para el horno
            Queue<Entity> accumQueue = joinQueues.get(accumKey); // Obtiene la cola de acumulación del horno

            // Obtener tamaño de lote dinámico desde la configuración
            int batchSize = getBatchSizeForLocation("HORNO"); // Obtiene el tamaño de lote configurado dinámicamente

            if (accumQueue != null) { // Verifica si la cola de acumulación existe
                accumQueue.add(entity); // Añade la entidad a la cola de acumulación

                System.out.println("[HORNO] Piezas acumuladas: " + accumQueue.size() + " / " + batchSize); // Imprime el progreso de acumulación

                if (accumQueue.size() >= batchSize) { // Verifica si se alcanzó el tamaño de lote requerido
                    List<Entity> batchEntities = new ArrayList<>(); // Crea lista para almacenar las entidades del lote
                    for (int i = 0; i < batchSize; i++) { // Itera por el tamaño del lote
                        Entity batchEntity = accumQueue.poll(); // Extrae una entidad de la cola
                        if (batchEntity != null) { // Verifica si la entidad no es nula
                            batchEntities.add(batchEntity); // Añade la entidad a la lista del lote
                        }
                    }

                    ProcessingRule rule = engine.getProcessingRule(locationName); // Obtiene la regla de procesamiento del horno
                    double processingTime = rule.getProcessingTime(); // Obtiene el tiempo de procesamiento configurado
                    double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación

                    for (Entity batchEntity : batchEntities) { // Itera sobre cada entidad del lote
                        batchEntity.addValueAddedTime(processingTime); // Añade el tiempo de valor agregado a cada entidad
                    }
                    engine.getStatistics().recordLocationProcessingTime(locationName, processingTime); // Registra el tiempo de procesamiento en estadísticas

                    System.out.println("[HORNO] Procesando lote de " + batchEntities.size() + " piezas por " + processingTime + " min"); // Imprime información del lote procesándose

                    final int finalBatchSize = batchEntities.size(); // Crea variable final con el tamaño del lote para usar en clase anónima
                    Event processingEvent = new Event(currentTime + processingTime, 0, "Process batch of " + finalBatchSize + " in HORNO") { // Crea evento de procesamiento del lote
                        @Override // Anotación que indica sobrescritura del método
                        public void execute() { // Define la lógica a ejecutar cuando ocurra el evento
                            Location hornoLocation = engine.getLocation("HORNO"); // Obtiene la ubicación del horno
                            double baseCompletionTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo de completación base

                            for (int i = 0; i < batchEntities.size(); i++) { // Itera sobre cada entidad del lote procesado
                                Entity batchEntity = batchEntities.get(i); // Obtiene la entidad del índice actual
                                double exitDelay = i * 0.01; // Calcula pequeño retraso de salida para simular salidas casi simultáneas

                                Event exitEvent = new Event(baseCompletionTime + exitDelay, 0, // Crea evento de salida para cada entidad
                                        "Exit piece " + (i + 1) + " from HORNO") { // Descripción del evento de salida
                                    @Override // Anotación que indica sobrescritura
                                    public void execute() { // Define la lógica de ejecución del evento de salida
                                        // hornoLocation.exit() removed to prevent premature release
                                        routeEntity(batchEntity, "HORNO"); // Rutea la entidad desde el horno a su siguiente destino
                                    }
                                };
                                engine.getScheduler().scheduleEvent(exitEvent); // Programa el evento de salida en el planificador
                            }
                        }
                    };
                    engine.getScheduler().scheduleEvent(processingEvent); // Programa el evento de procesamiento del lote
                }
            }
            return; // Termina la ejecución del método para el caso del horno
        }

        ProcessingRule rule = engine.getProcessingRule(locationName); // Obtiene la regla de procesamiento de la ubicación
        if (rule != null) { // Verifica si existe una regla de procesamiento
            double processingTime = rule.getProcessingTime(); // Obtiene el tiempo de procesamiento configurado
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación

            Event processingEvent = new Event(currentTime + processingTime, 0, // Crea evento de procesamiento programado para el futuro
                    "Process " + entity.getType().getName() + " at " + locationName) { // Descripción del evento

                @Override // Anotación que indica sobrescritura
                public void execute() { // Define la lógica a ejecutar al completar el procesamiento
                    completeProcessing(entity, locationName); // Llama al método que completa el procesamiento
                }

            };

            engine.getScheduler().scheduleEvent(processingEvent); // Programa el evento en el planificador

            if (processingTime > 0) { // Verifica si hay tiempo de procesamiento positivo
                entity.addValueAddedTime(processingTime); // Añade el tiempo como valor agregado a la entidad
                engine.getStatistics().recordLocationProcessingTime(locationName, processingTime); // Registra el tiempo en estadísticas de ubicación
            }
        }
    }

    public void completeProcessing(Entity entity, String locationName) { // Método que completa el procesamiento de una entidad
        // Location exit is now handled in routeEntity or moveWithResource
        // to ensure utilization stats include waiting time for resources.

        routeEntity(entity, locationName); // Rutea la entidad a su siguiente destino
        if (animationController != null) { // Verifica si hay controlador de animación
            updateGUILocationOccupancy(locationName); // Actualiza la ocupación en la interfaz gráfica
        }
    }

    private void checkAndPromoteFromQueue(Location location) { // Método que promueve entidades de la cola a la ubicación cuando hay espacio
        while (location.canAccept() && location.getQueueSize() > 0) { // Mientras la ubicación pueda aceptar y haya entidades en cola
            Entity nextEntity = location.removeFromQueue(); // Extrae la siguiente entidad de la cola
            if (nextEntity != null) { // Verifica si la entidad no es nula
                if (location.enter(nextEntity, engine.getClock().getCurrentTime())) { // Intenta que la entidad entre a la ubicación
                    // Notify arrival for the promoted entity to trigger stats and processing
                    engine.notifyEntityArrival(nextEntity, location); // Notifica la llegada de la entidad promovida
                    engine.getStatistics().recordLocationEntry(location.getName()); // Registra la entrada en estadísticas
                    scheduleProcessing(nextEntity, location.getName()); // Programa el procesamiento de la entidad promovida
                }
            }
        }
    }

    public void routeEntity(Entity entity, String fromLocation) { // Método público que rutea una entidad desde su ubicación actual
        String entityType = entity.getType().getName(); // Obtiene el nombre del tipo de entidad
        RoutingRule route = getRoutingRule(fromLocation, entityType); // Obtiene la regla de ruteo aplicable

        if (route != null) { // Verifica si existe una regla de ruteo
            String destination = route.destinationLocation(); // Obtiene la ubicación destino de la regla
            String moveLogic = route.moveLogic(); // Obtiene la lógica de movimiento de la regla

            if ("EXIT".equals(destination)) { // Verifica si el destino es salida del sistema
                handleExit(entity); // Maneja la salida de la entidad del sistema
            } else if ("JOIN".equals(moveLogic)) { // Verifica si la lógica es de tipo unión
                Location to = engine.getLocation(destination); // Obtiene la ubicación destino
                if (to.canAccept()) { // Verifica si la ubicación puede aceptar la entidad
                    if (route.resourceName() != null && !route.resourceName().isEmpty()) { // Verifica si se requiere un recurso
                        moveWithResource(entity, destination, route.resourceName()); // Mueve la entidad usando el recurso
                    } else { // Si no se requiere recurso
                        Location from = engine.getLocation(fromLocation); // Obtiene la ubicación origen
                        engine.notifyEntityMove(entity, from, to); // Notifica el movimiento de la entidad
                        handleArrival(entity, destination); // Maneja la llegada al destino
                    }
                } else { // Si la ubicación no puede aceptar
                    blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity); // Añade la entidad a la cola de bloqueadas
                    blockStartTime.put(entity, engine.getClock().getCurrentTime()); // Registra el tiempo de inicio de bloqueo
                }
            } else { // Para otras lógicas de movimiento
                double probability = route.probability(); // Obtiene la probabilidad de tomar esta ruta
                if (random.nextDouble() <= probability) { // Verifica si un número aleatorio cae dentro de la probabilidad
                    int quantity = route.quantity(); // Obtiene la cantidad de entidades a generar o mover

                    if ("ACCUM".equals(route.moveLogic()) && quantity > 1) { // Verifica si es lógica de acumulación con cantidad mayor a 1
                        handleAccumulate(entity, fromLocation, destination, quantity, route.resourceName()); // Maneja la acumulación de entidades
                    } else if (quantity > 1) { // Si la cantidad es mayor a 1 pero no es acumulación
                        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual
                        entity.addSystemTime(currentTime - entity.getEntryTime()); // Añade el tiempo en sistema a la entidad original

                        for (int i = 0; i < quantity; i++) { // Itera por la cantidad de entidades a crear
                            Entity newEntity = createTransformedEntity(entity, destination); // Crea una nueva entidad transformada
                            engine.getStatistics().recordEntityEntry(newEntity); // Registra la entrada de la nueva entidad

                            Location to = engine.getLocation(destination); // Obtiene la ubicación destino
                            if (to.canAccept()) { // Verifica si puede aceptar la entidad
                                if (route.resourceName() != null && !route.resourceName().isEmpty()) { // Verifica si requiere recurso
                                    moveWithResource(newEntity, destination, route.resourceName()); // Mueve con recurso
                                } else { // Si no requiere recurso
                                    Location from = engine.getLocation(fromLocation); // Obtiene ubicación origen
                                    engine.notifyEntityMove(newEntity, from, to); // Notifica el movimiento
                                    handleArrival(newEntity, destination); // Maneja la llegada
                                }
                            } else { // Si no puede aceptar
                                blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(newEntity); // Añade a cola de bloqueadas
                                blockStartTime.put(newEntity, engine.getClock().getCurrentTime()); // Registra tiempo de inicio de bloqueo
                            }
                        }
                    } else { // Si la cantidad es 1 (movimiento simple)
                        Location to = engine.getLocation(destination); // Obtiene la ubicación destino
                        if (to.canAccept()) { // Verifica si puede aceptar la entidad
                            if (route.resourceName() != null && !route.resourceName().isEmpty()) { // Verifica si requiere recurso
                                moveWithResource(entity, destination, route.resourceName()); // Mueve con recurso
                            } else { // Si no requiere recurso
                                Location from = engine.getLocation(fromLocation); // Obtiene ubicación origen
                                from.exit(engine.getClock().getCurrentTime()); // Ejecuta la salida de la ubicación origen
                                checkAndPromoteFromQueue(from); // Promueve entidades de la cola si hay espacio
                                notifyLocationAvailable(fromLocation); // Notifica que la ubicación tiene espacio disponible

                                engine.notifyEntityMove(entity, from, to); // Notifica el movimiento de la entidad

                                if ("DESCARGA".equals(fromLocation) && "BANDA_2".equals(destination)) { // Verifica caso especial descarga a banda 2

                                    double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual
                                    Event moveEvent = new Event(currentTime + 1.0, 0, // Crea evento de movimiento con delay de 1 minuto
                                            "Move through " + fromLocation + " to " + destination) { // Descripción del evento
                                        @Override // Anotación de sobrescritura
                                        public void execute() { // Define lógica de ejecución
                                            handleArrival(entity, destination); // Maneja la llegada al destino
                                        }
                                    };
                                    engine.getScheduler().scheduleEvent(moveEvent); // Programa el evento de movimiento
                                    entity.addNonValueAddedTime(1.0); // Añade 1 minuto como tiempo sin valor agregado
                                } else { // Para otros casos
                                    handleArrival(entity, destination); // Maneja la llegada inmediata al destino
                                }
                            }
                        } else { // Si el destino no puede aceptar
                            blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity); // Añade a cola de bloqueadas
                            blockStartTime.put(entity, engine.getClock().getCurrentTime()); // Registra tiempo de bloqueo
                        }
                    }
                } else { // Si el número aleatorio no cae dentro de la probabilidad
                    handleExit(entity); // Maneja la salida de la entidad del sistema
                }
            }
        } else { // Si no existe regla de ruteo
            handleExit(entity); // Maneja la salida de la entidad del sistema
        }
    }

    private Entity createTransformedEntity(Entity original, String destinationLocation) { // Método que crea una entidad transformada a partir de una original
        return original; // Retorna la entidad original sin transformación por ahora
    }

    private void checkResourceQueue(Resource resource) { // Método que verifica y procesa la cola de un recurso
        // Check if resource is available OR if it's returning home (interruptible)
        if ((resource.isAvailable() || resource.isReturningHome()) && resource.getQueueSize() > 0) { // Verifica si recurso está disponible o volviendo a casa y tiene cola

            // If returning home, we interrupt it.
            // The resource is technically "available" (units > 0) but was moving.
            // We claim it now.
            if (resource.isReturningHome()) { // Verifica si el recurso está volviendo a casa
                resource.setReturningHome(false); // Cancela el estado de retorno a casa
                // Note: We assume it's still at the start location of the return trip
                // because we haven't processed the arrival event yet.
                // This is the desired behavior: claim it before it goes far.
            }

            // Implement "Closest" rule (Más Cercano)
            // Find the entity in the queue that is closest to the resource's current
            // location
            Entity bestEntity = null; // Inicializa la mejor entidad como nula
            double minDistance = Double.MAX_VALUE; // Inicializa distancia mínima con valor máximo
            String resourceLoc = resource.getCurrentLocation(); // Obtiene la ubicación actual del recurso

            for (Entity entity : resource.getQueue()) { // Itera sobre cada entidad en la cola del recurso
                String entityLoc = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName() // Obtiene ubicación de la entidad o UNKNOWN si es nula
                        : "UNKNOWN"; // Valor por defecto si ubicación es nula
                double dist = calculateDistance(resourceLoc, entityLoc); // Calcula la distancia entre recurso y entidad
                if (dist < minDistance) { // Verifica si esta distancia es menor que la mínima encontrada
                    minDistance = dist; // Actualiza la distancia mínima
                    bestEntity = entity; // Actualiza la mejor entidad encontrada
                }
            }

            if (bestEntity != null) { // Verifica si se encontró una entidad más cercana
                resource.removeEntity(bestEntity); // Remueve la entidad de la cola del recurso
                String destination = bestEntity.getPendingDestination(); // Obtiene el destino pendiente de la entidad
                moveWithResource(bestEntity, destination, resource.getName()); // Mueve la entidad usando el recurso
            }
        } else if (resource.isAvailable() && resource.getQueueSize() == 0) { // Verifica si recurso está disponible y no tiene cola
            returnToHome(resource); // Inicia el retorno del recurso a su ubicación base
        }
    }

    private void returnToHome(Resource resource) { // Método que programa el retorno de un recurso a su ubicación base
        String homeLocation = null; // Inicializa ubicación base como nula
        if ("GRUA_VIAJERA".equals(resource.getName())) { // Verifica si el recurso es la grúa viajera
            homeLocation = "ALMACEN_MP"; // Asigna almacén de materia prima como ubicación base
        } else if ("ROBOT".equals(resource.getName())) { // Verifica si el recurso es el robot
            homeLocation = "CARGA"; // Asigna carga como ubicación base
        }

        if (homeLocation != null && !homeLocation.equals(resource.getCurrentLocation())) { // Verifica si hay ubicación base y no está ya ahí
            // Mark as returning home
            resource.setReturningHome(true); // Marca el recurso como volviendo a casa
            // Increment ID to invalidate previous return events
            final long currentId = resource.incrementReturnHomeId(); // Incrementa y obtiene ID de retorno para invalidar eventos previos

            double moveTime = calculateMoveTime(resource.getCurrentLocation(), homeLocation, resource.getName()); // Calcula tiempo de movimiento a casa
            // Add a delay before actually returning. This keeps the resource at the last
            // location
            // for a while, allowing nearby locations (like Rectificado) to claim it
            // quickly.
            // This matches ProModel behavior where resources don't return immediately.
            double delay = 10.0; // Define delay de 10 minutos antes de retornar
            double currentTime = engine.getClock().getCurrentTime(); // Obtiene tiempo actual
            String finalHome = homeLocation; // Crea variable final con ubicación base para usar en clase anónima

            Event returnEvent = new Event(currentTime + moveTime + delay, 0, // Crea evento de retorno con tiempo de viaje más delay
                    "Return " + resource.getName() + " to Home") { // Descripción del evento
                @Override // Anotación de sobrescritura
                public void execute() { // Define lógica de ejecución del evento
                    // Only update if this specific return event is still valid
                    if (resource.isReturningHome() && resource.getReturnHomeId() == currentId) { // Verifica si el evento de retorno sigue siendo válido
                        resource.setCurrentLocation(finalHome); // Actualiza la ubicación del recurso a la base
                        resource.setReturningHome(false); // Marca que ya no está volviendo a casa
                    }
                }
            };
            engine.getScheduler().scheduleEvent(returnEvent); // Programa el evento de retorno
        }
    }

    private void moveWithResource(Entity entity, String destination, String resourceName) { // Método que mueve una entidad a un destino usando un recurso específico
        Resource resource = engine.getResource(resourceName); // Obtiene el recurso por nombre
        Location destinationLoc = engine.getLocation(destination); // Obtiene la ubicación destino
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual

        // CRITICAL: Try to RESERVE space in destination BEFORE moving
        if (destinationLoc != null) { // Verifica si la ubicación destino existe
            if (!destinationLoc.reserve()) { // Intenta reservar espacio en el destino
                // Could not reserve - destination is full (considering current + reserved)
                // Entity is BLOCKED at current location
                blockedEntities.computeIfAbsent(destination, k -> new LinkedList<>()).add(entity); // Añade entidad a cola de bloqueadas
                blockStartTime.put(entity, currentTime); // Registra tiempo de inicio de bloqueo
                // Entity stays at current location - blocking time will accumulate
                return; // Termina la ejecución sin mover la entidad
            }
            // Successfully reserved space - proceed with movement
        }

        if (resource != null && resource.isAvailable()) { // Verifica si el recurso existe y está disponible
            // Invalidate any pending return events by incrementing the ID
            resource.incrementReturnHomeId(); // Incrementa ID de retorno para invalidar eventos previos

            // If the resource was returning home, cancel that status
            if (resource.isReturningHome()) { // Verifica si el recurso estaba volviendo a casa
                resource.setReturningHome(false); // Cancela el estado de retorno a casa
            }

            resource.acquire(currentTime); // Adquiere el recurso registrando el tiempo
            engine.notifyResourceAcquired(resource, entity); // Notifica la adquisición del recurso

            // 1. Calculate Empty Travel Time
            String resourceLoc = resource.getCurrentLocation(); // Obtiene ubicación actual del recurso
            String entityLoc = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName() : "UNKNOWN"; // Obtiene ubicación de la entidad o UNKNOWN
            double emptyTravelTime = calculateMoveTime(resourceLoc, entityLoc, resourceName); // Calcula tiempo de viaje vacío del recurso a la entidad

            if ("ROBOT".equals(resourceName) && "RECTIFICADO".equals(entityLoc)) { // Caso especial para debug (comentario removido)
                // Debug print removed
            }

            // 2. Schedule Pickup Event
            Event pickupEvent = new Event(currentTime + emptyTravelTime, 0, "Pickup " + entity.getType().getName()) { // Crea evento de recolección
                @Override // Anotación de sobrescritura
                public void execute() { // Define lógica de ejecución del pickup
                    // Resource arrives at entity location
                    resource.setCurrentLocation(entityLoc); // Actualiza ubicación del recurso a ubicación de entidad

                    // 3. Execute Loaded Move
                    performLoadedMove(entity, destination, resourceName, resource); // Ejecuta el movimiento cargado
                }
            };
            engine.getScheduler().scheduleEvent(pickupEvent); // Programa el evento de recolección

            // Add empty travel time to entity's non-value added time (waiting for
            // transport)
            entity.addNonValueAddedTime(emptyTravelTime); // Añade tiempo de viaje vacío como tiempo sin valor agregado

        } else { // Si el recurso no está disponible
            // Resource not available - release reservation and queue
            if (resource != null) { // Verifica si el recurso existe
                if (destinationLoc != null) { // Verifica si la ubicación destino existe
                    destinationLoc.releaseReservation(); // Libera la reservación del espacio
                }
                entity.setPendingDestination(destination); // Establece el destino pendiente en la entidad
                resource.addToQueue(entity); // Añade la entidad a la cola del recurso
                entity.addWaitTime(1.0); // Añade 1 minuto de tiempo de espera
            }
        }
    }

    private void performLoadedMove(Entity entity, String destination, String resourceName, Resource resource) { // Método que ejecuta el movimiento de entidad con recurso cargado
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación

        // ⚠️ CRÍTICO: NO liberar la ubicación ahora - implementando Blk=1 de ProModel
        // En ProModel, Blk=1 significa que la entidad bloquea la locación durante el
        // movimiento
        // Solo se libera después de "THEN FREE" (al completar el movimiento)
        Location from = entity.getCurrentLocation(); // Obtiene la ubicación origen de la entidad

        String currentLocation = entity.getCurrentLocation() != null ? entity.getCurrentLocation().getName() // Obtiene nombre de ubicación actual o UNKNOWN
                : "UNKNOWN"; // Valor por defecto si ubicación es nula
        double moveTime = calculateMoveTime(currentLocation, destination, resourceName); // Calcula el tiempo de movimiento cargado

        if (animationController != null) { // Verifica si hay controlador de animación activo
            animationController.animateEntityMovement(entity, currentLocation, destination, resourceName, () -> { // Inicia animación de movimiento
                // Animation callback (optional, logic handled in event)
            });
        }

        Event moveEvent = new Event(currentTime + moveTime, 0, // Crea evento de movimiento programado para el futuro
                "Move " + entity.getType().getName() + " to " + destination) { // Descripción del evento
            @Override // Anotación de sobrescritura
            public void execute() { // Define lógica de ejecución al completar el movimiento
                // ⚠️ AHORA SÍ - Liberar locación origen DESPUÉS del movimiento (THEN FREE)
                if (from != null) { // Verifica si la ubicación origen existe
                    from.exit(engine.getClock().getCurrentTime()); // Ejecuta la salida de la ubicación origen
                    checkAndPromoteFromQueue(from); // Promueve entidades de la cola de la ubicación origen
                    notifyLocationAvailable(from.getName()); // Notifica que la ubicación origen tiene espacio
                }

                engine.notifyResourceReleased(resource, entity); // Notifica la liberación del recurso

                // Record resource trip statistics
                engine.getStatistics().recordResourceTrip(resourceName, moveTime); // Registra el viaje del recurso en estadísticas

                Location to = engine.getLocation(destination); // Obtiene la ubicación destino
                if (from != null && to != null) { // Verifica si ambas ubicaciones existen
                    engine.notifyEntityMove(entity, from, to); // Notifica el movimiento de la entidad
                }

                // Update resource location to destination
                resource.setCurrentLocation(destination); // Actualiza la ubicación del recurso al destino

                if (animationController == null) { // Si no hay controlador de animación
                    resource.release(engine.getClock().getCurrentTime()); // Libera el recurso
                    checkResourceQueue(resource); // Verifica la cola del recurso para próxima asignación
                    handleArrival(entity, destination); // Maneja la llegada de la entidad al destino
                } else { // Si hay controlador de animación
                    // If animation is present, we still need to release logic here
                    // or ensure animation callback handles it.
                    // For safety/consistency with previous logic:
                    resource.release(engine.getClock().getCurrentTime()); // Libera el recurso
                    checkResourceQueue(resource); // Verifica la cola del recurso
                    handleArrival(entity, destination); // Maneja la llegada al destino
                }
            }
        };

        engine.getScheduler().scheduleEvent(moveEvent); // Programa el evento de movimiento
        entity.addNonValueAddedTime(moveTime); // Añade el tiempo de movimiento como tiempo sin valor agregado
    }

    private void handleExit(Entity entity) { // Método que maneja la salida de una entidad del sistema
        double currentTime = engine.getClock().getCurrentTime(); // Obtiene el tiempo actual de simulación
        entity.addSystemTime(currentTime - entity.getCreationTime()); // Añade el tiempo total en sistema a la entidad
        Location from = entity.getCurrentLocation(); // Obtiene la ubicación actual de la entidad
        if (from != null) { // Verifica si la ubicación existe
            from.exit(currentTime); // Ejecuta la salida de la ubicación
            checkAndPromoteFromQueue(from); // Promueve entidades de la cola si hay espacio
            notifyLocationAvailable(from.getName()); // Notifica que la ubicación tiene espacio disponible
            engine.notifyEntityExit(entity, from); // Notifica la salida de la entidad
        }
        engine.getStatistics().recordEntityExit(entity); // Registra la salida de la entidad en estadísticas
    }

    private void updateGUILocationOccupancy(String locationName) { // Método que actualiza la ocupación de una ubicación en la interfaz gráfica
        if (animationController != null) { // Verifica si hay controlador de animación activo
            Location location = engine.getLocation(locationName); // Obtiene la ubicación por nombre
            if (location != null) { // Verifica si la ubicación existe
                int occupancy = location.getCurrentOccupancy(); // Obtiene la ocupación actual de la ubicación
                int capacity = location.getType().capacity(); // Obtiene la capacidad de la ubicación
                if (animationController.getLocationNodes().containsKey(locationName)) { // Verifica si existe nodo gráfico para esta ubicación
                    animationController.getLocationNodes().get(locationName).setOccupancy(occupancy); // Actualiza la ocupación en el nodo gráfico
                    animationController.getLocationNodes().get(locationName).setCapacity(capacity); // Actualiza la capacidad en el nodo gráfico
                }
            }
        }
    }

    private RoutingRule getRoutingRule(String locationName, String entityType) { // Método que obtiene la regla de ruteo para una ubicación y tipo de entidad
        return createRoutingRuleForLocation(locationName, entityType); // Delega a método que crea la regla de ruteo
    }

    private RoutingRule createRoutingRuleForLocation(String locationName, String entityType) { // Método que crea dinámicamente la regla de ruteo según la ubicación
        switch (locationName) { // Evalúa el nombre de la ubicación
            case "ALMACEN_MP": // Caso almacén de materia prima
                return new RoutingRule("HORNO", 1.0, 1, "FIRST", "GRUA_VIAJERA"); // Retorna regla: destino horno, probabilidad 1.0, cantidad 1, lógica FIRST, recurso grúa
            case "HORNO": // Caso horno
                return new RoutingRule("BANDA_1", 1.0, 1, "FIRST", "GRUA_VIAJERA"); // Retorna regla: destino banda 1 con grúa
            case "BANDA_1": // Caso banda 1
                return new RoutingRule("CARGA", 1.0, 1, "FIRST", null); // Retorna regla: destino carga sin recurso
            case "CARGA": // Caso carga
                return new RoutingRule("TORNEADO", 1.0, 1, "FIRST", "ROBOT"); // Retorna regla: destino torneado con robot
            case "TORNEADO": // Caso torneado
                return new RoutingRule("FRESADO", 1.0, 1, "FIRST", "ROBOT"); // Retorna regla: destino fresado con robot
            case "FRESADO": // Caso fresado
                return new RoutingRule("TALADRO", 1.0, 1, "FIRST", "ROBOT"); // Retorna regla: destino taladro con robot
            case "TALADRO": // Caso taladro
                return new RoutingRule("RECTIFICADO", 1.0, 1, "FIRST", "ROBOT"); // Retorna regla: destino rectificado con robot
            case "RECTIFICADO": // Caso rectificado
                return new RoutingRule("DESCARGA", 1.0, 1, "FIRST", "ROBOT"); // Retorna regla: destino descarga con robot
            case "DESCARGA": // Caso descarga
                return new RoutingRule("BANDA_2", 1.0, 1, "FIRST", null); // Retorna regla: destino banda 2 sin recurso
            case "BANDA_2": // Caso banda 2
                return new RoutingRule("INSPECCION", 1.0, 1, "FIRST", null); // Retorna regla: destino inspección sin recurso
            case "INSPECCION": // Caso inspección
                return new RoutingRule("SALIDA", 1.0, 1, "FIRST", null); // Retorna regla: destino salida sin recurso
            case "SALIDA": // Caso salida
                return new RoutingRule("EXIT", 1.0, 1, "EXIT", null); // Retorna regla: destino EXIT del sistema
            default: // Caso por defecto para ubicaciones no reconocidas
                return null; // Retorna null si no hay regla definida
        }
    }

    private double calculateMoveTime(String from, String to, String resourceName) { // Método que calcula el tiempo de movimiento entre dos ubicaciones
        double distance = calculateDistance(from, to); // Calcula la distancia entre origen y destino
        Resource resource = engine.getResource(resourceName); // Obtiene el recurso por nombre
        double speed = resource != null ? resource.getType().speedMetersPerMinute() : 150.0; // Obtiene velocidad del recurso o usa 150.0 por defecto

        if (speed > 0) // Verifica si la velocidad es positiva
            return distance / speed; // Retorna tiempo calculado dividiendo distancia entre velocidad
        return 1.0; // Retorna 1.0 minuto por defecto si velocidad es cero
    }

    private double calculateDistance(String from, String to) { // Método que calcula la distancia entre dos ubicaciones
        if (from.equals(to)) // Verifica si origen y destino son iguales
            return 0.0; // Retorna distancia cero si son la misma ubicación

        // Map locations to node indices for Red_Robot (Linear:
        // Carga-Torneado-Fresado-Taladro-Rectificado-Descarga)
        List<String> robotPath = Arrays.asList("CARGA", "TORNEADO", "FRESADO", "TALADRO", "RECTIFICADO", "DESCARGA"); // Define el camino lineal del robot
        int idxFrom = robotPath.indexOf(from); // Obtiene índice de ubicación origen en el camino del robot
        int idxTo = robotPath.indexOf(to); // Obtiene índice de ubicación destino en el camino del robot

        if (idxFrom != -1 && idxTo != -1) { // Verifica si ambas ubicaciones están en el camino del robot
            // Calculate distance along the path
            double totalDist = 0; // Inicializa distancia total en cero
            int start = Math.min(idxFrom, idxTo); // Obtiene el índice menor como punto de inicio
            int end = Math.max(idxFrom, idxTo); // Obtiene el índice mayor como punto final

            // Distances between segments:
            // Carga-Torneado: 20
            // Torneado-Fresado: 15
            // Fresado-Taladro: 15
            // Taladro-Rectificado: 15
            // Rectificado-Descarga: 20
            double[] segmentDists = { 20.0, 15.0, 15.0, 15.0, 20.0 }; // Define distancias entre segmentos consecutivos del robot

            for (int i = start; i < end; i++) { // Itera sobre los segmentos entre inicio y fin
                totalDist += segmentDists[i]; // Suma la distancia de cada segmento
            }
            return totalDist; // Retorna la distancia total acumulada
        }

        // Map locations to node indices for Red_Grua (Linear: Almacen-Horno-Banda_1)
        List<String> gruaPath = Arrays.asList("ALMACEN_MP", "HORNO", "BANDA_1"); // Define el camino lineal de la grúa
        idxFrom = gruaPath.indexOf(from); // Obtiene índice de ubicación origen en el camino de la grúa
        idxTo = gruaPath.indexOf(to); // Obtiene índice de ubicación destino en el camino de la grúa

        if (idxFrom != -1 && idxTo != -1) { // Verifica si ambas ubicaciones están en el camino de la grúa
            double totalDist = 0; // Inicializa distancia total en cero
            int start = Math.min(idxFrom, idxTo); // Obtiene el índice menor como punto de inicio
            int end = Math.max(idxFrom, idxTo); // Obtiene el índice mayor como punto final

            // Almacen-Horno: 10
            // Horno-Banda_1: 15
            double[] segmentDists = { 10.0, 15.0 }; // Define distancias entre segmentos consecutivos de la grúa

            for (int i = start; i < end; i++) { // Itera sobre los segmentos entre inicio y fin
                totalDist += segmentDists[i]; // Suma la distancia de cada segmento
            }
            return totalDist; // Retorna la distancia total acumulada
        }

        // Conveyors (Bandas)
        if (from.equals("BANDA_1") || to.equals("BANDA_1")) // Verifica si alguna ubicación es banda 1
            return 30.0; // Retorna distancia fija de 30.0 para banda 1
        if (from.equals("BANDA_2") || to.equals("BANDA_2")) // Verifica si alguna ubicación es banda 2
            return 30.0; // Retorna distancia fija de 30.0 para banda 2

        return 0.0; // Retorna distancia cero por defecto si no se encuentra ruta
    }

    private boolean isPath(String from, String to, String loc1, String loc2) { // Método que verifica si dos ubicaciones forman un camino válido
        return (from.equals(loc1) && to.equals(loc2)) || (from.equals(loc2) && to.equals(loc1)); // Retorna true si el camino coincide en cualquier dirección
    }
}
