package com.simulation.core; // Declaración del paquete principal del motor de simulación
import com.simulation.config.SimulationParameters; // Importa la clase de parámetros de configuración
import com.simulation.core.EventTypes.*; // Importa todos los tipos de eventos del sistema
import com.simulation.random.RandomGenerators; // Importa el generador de números aleatorios
import com.simulation.resources.BufferLocation; // Importa la clase para locaciones de tipo buffer
import com.simulation.resources.Location; // Importa la clase base abstracta de locaciones
import com.simulation.resources.ProcessingLocation; // Importa la clase para locaciones de procesamiento
import com.simulation.statistics.Statistics; // Importa la clase de estadísticas

import java.util.ArrayList; // Importa ArrayList para listas dinámicas
import java.util.Collections; // Importa utilidades para colecciones sincronizadas
import java.util.HashSet; // Importa HashSet para conjuntos sin duplicados
import java.util.List; // Importa la interfaz List
import java.util.PriorityQueue; // Importa cola de prioridad para eventos ordenados por tiempo
import java.util.Set; // Importa la interfaz Set

/**
 * Motor de simulación DIGEMIC - Sistema de expedición de pasaportes.
 * Modelo con 6 locaciones: Entrada, Zona_Formas, Sala_Sillas, Sala_De_Pie, Servidor_1, Servidor_2.
 */
public class DigemicEngine { // Declaración de la clase principal del motor de simulación

    private final SimulationParameters params; // Almacena los parámetros de configuración de la simulación
    private RandomGenerators randomGen; // Generador de números aleatorios para eventos estocásticos
    private final Statistics statistics; // Objeto que recopila y calcula estadísticas de la simulación
    private final PriorityQueue<Event> eventQueue; // Cola de eventos ordenados por tiempo de ocurrencia
    private volatile double currentTime; // Tiempo actual de la simulación en minutos (volatile para thread-safety)
    private volatile boolean running; // Bandera que indica si la simulación está en ejecución
    private volatile boolean paused; // Bandera que indica si la simulación está pausada
    private volatile double simulationSpeed = 2.0; // Velocidad de simulación: 2 minutos simulados por segundo real
    private volatile long lastRealTime = 0L; // Marca de tiempo real del último evento procesado

    // 6 Locaciones del sistema DIGEMIC
    private BufferLocation entrada; // Locación de entrada donde llegan los clientes
    private BufferLocation zonaFormas; // Zona donde los clientes llenan formularios
    private BufferLocation salaSillas; // Sala de espera con 40 sillas (capacidad limitada)
    private BufferLocation salaDePie; // Sala de espera de pie (capacidad ilimitada)
    private ProcessingLocation servidor1; // Primer servidor de atención (prioridad FIRST)
    private ProcessingLocation servidor2; // Segundo servidor de atención

    private final Set<Entity> entitiesInTransport; // Conjunto de entidades en tránsito entre locaciones
    private final List<Entity> allActiveEntities; // Lista de todas las entidades activas en el sistema

    // Contadores de pasaportes por servidor (cada 10 → pausa)
    private volatile int pasaportesAtendidosServidor1 = 0; // Contador de pasaportes procesados por servidor 1
    private volatile int pasaportesAtendidosServidor2 = 0; // Contador de pasaportes procesados por servidor 2
    private volatile boolean servidor1Paused = false; // Indica si servidor 1 está en pausa
    private volatile boolean servidor2Paused = false; // Indica si servidor 2 está en pausa

    public DigemicEngine(SimulationParameters params) { // Constructor que recibe los parámetros de configuración
        this.params = params; // Asigna los parámetros recibidos al atributo de la clase
        this.statistics = new Statistics(); // Crea una nueva instancia del objeto de estadísticas
        this.eventQueue = new PriorityQueue<>(); // Inicializa la cola de eventos vacía
        this.entitiesInTransport = new HashSet<>(); // Inicializa el conjunto de entidades en tránsito
        this.allActiveEntities = Collections.synchronizedList(new ArrayList<>()); // Crea lista sincronizada de entidades activas
        this.currentTime = 0.0; // Inicializa el tiempo de simulación en cero
        this.running = false; // Marca la simulación como no iniciada
        this.paused = false; // Marca la simulación como no pausada
        initializeLocations(); // Llama al método que inicializa las 6 locaciones del sistema
        initializeRandomGenerators(); // Llama al método que configura los generadores aleatorios
    }

    private void initializeLocations() { // Método que crea e inicializa las 6 locaciones del sistema
        entrada = new BufferLocation("ENTRADA", params.getEntradaCapacity()); // Crea la entrada con capacidad infinita
        zonaFormas = new BufferLocation("ZONA_FORMAS", params.getZonaFormasCapacity()); // Crea zona de formas con capacidad infinita
        salaSillas = new BufferLocation("SALA_SILLAS", params.getSalaSillasCapacity()); // Crea sala de sillas con capacidad de 40
        salaDePie = new BufferLocation("SALA_DE_PIE", params.getSalaDePieCapacity()); // Crea sala de pie con capacidad infinita
        servidor1 = new ProcessingLocation("SERVIDOR_1", params.getServidor1Capacity()); // Crea servidor 1 con capacidad de 1
        servidor2 = new ProcessingLocation("SERVIDOR_2", params.getServidor2Capacity()); // Crea servidor 2 con capacidad de 1

        statistics.registerLocation(entrada); // Registra la entrada en el módulo de estadísticas
        statistics.registerLocation(zonaFormas); // Registra zona de formas en el módulo de estadísticas
        statistics.registerLocation(salaSillas); // Registra sala de sillas en el módulo de estadísticas
        statistics.registerLocation(salaDePie); // Registra sala de pie en el módulo de estadísticas
        statistics.registerLocation(servidor1); // Registra servidor 1 en el módulo de estadísticas
        statistics.registerLocation(servidor2); // Registra servidor 2 en el módulo de estadísticas
    }

    private void initializeRandomGenerators() { // Método que configura los generadores de números aleatorios
        randomGen = new RandomGenerators(params.getBaseRandomSeed()); // Crea generador con semilla base para reproducibilidad
        randomGen.initialize( // Inicializa los generadores con los parámetros específicos
                params.getArrivalMeanTime(), // Media de tiempo entre arribos (3.33 min)
                params.getZonaFormasMin(), // Tiempo mínimo en zona de formas (4 min)
                params.getZonaFormasMax(), // Tiempo máximo en zona de formas (8 min)
                params.getServicioMean(), // Tiempo promedio de servicio (6 min)
                params.getPausaServidorMean(), // Tiempo promedio de pausa del servidor (5 min)
                params.getDirectoASalaProb() // Probabilidad de ir directo a sala (90%)
        );
    }

    public void reset() { // Método que reinicia completamente la simulación a su estado inicial
        eventQueue.clear(); // Elimina todos los eventos pendientes de la cola
        entitiesInTransport.clear(); // Elimina todas las entidades en tránsito
        allActiveEntities.clear(); // Elimina todas las entidades activas del sistema
        currentTime = 0.0; // Reinicia el tiempo de simulación a cero
        running = false; // Marca la simulación como detenida
        paused = false; // Marca la simulación como no pausada
        lastRealTime = 0L; // Reinicia la marca de tiempo real
        pasaportesAtendidosServidor1 = 0; // Reinicia el contador de pasaportes del servidor 1
        pasaportesAtendidosServidor2 = 0; // Reinicia el contador de pasaportes del servidor 2
        servidor1Paused = false; // Marca servidor 1 como no pausado
        servidor2Paused = false; // Marca servidor 2 como no pausado
        Entity.resetIdCounter(); // Reinicia el contador de IDs de entidades a cero
        statistics.reset(); // Reinicia todas las estadísticas acumuladas

        entrada.resetState(); // Reinicia el estado de la entrada
        zonaFormas.resetState(); // Reinicia el estado de zona de formas
        salaSillas.resetState(); // Reinicia el estado de sala de sillas
        salaDePie.resetState(); // Reinicia el estado de sala de pie
        servidor1.resetState(); // Reinicia el estado del servidor 1
        servidor2.resetState(); // Reinicia el estado del servidor 2

        statistics.updateWaitingAreaSnapshot( // Actualiza la instantánea de las áreas de espera
                salaSillas.getCurrentContent(), // Contenido actual de sala de sillas (0)
                salaDePie.getCurrentContent() // Contenido actual de sala de pie (0)
        );

        initializeRandomGenerators(); // Re-inicializa los generadores aleatorios con la semilla base
    }

    public void initialize() { // Método que inicializa la simulación para comenzar su ejecución
        reset(); // Primero reinicia todo a estado inicial
        lastRealTime = System.currentTimeMillis(); // Captura el tiempo real actual del sistema
        scheduleEvent(new ArrivalEvent(0.0)); // Programa el primer evento de arribo en tiempo 0
    }

    public void setSimulationSpeed(double minutesPerSecond) { // Método que ajusta la velocidad de simulación
        if (Double.isNaN(minutesPerSecond) || Double.isInfinite(minutesPerSecond)) { // Verifica si el valor es NaN o infinito
            return; // Si es inválido, no hace nada y retorna
        }
        if (minutesPerSecond <= 0) { // Verifica si la velocidad es cero o negativa
            this.simulationSpeed = 0.0; // Establece velocidad en cero (pausa efectiva)
        } else { // Si la velocidad es válida y positiva
            this.simulationSpeed = minutesPerSecond; // Asigna la nueva velocidad de simulación
        }
        lastRealTime = System.currentTimeMillis(); // Actualiza la marca de tiempo real para el nuevo cálculo
    }

    public double getSimulationSpeed() { // Método getter que retorna la velocidad actual de simulación
        return simulationSpeed; // Devuelve el valor de minutos simulados por segundo real
    }

    public void run() { // Método principal que ejecuta el bucle de simulación
        running = true; // Marca la simulación como en ejecución
        double endTime = params.getSimulationDurationMinutes(); // Obtiene el tiempo final de simulación (480 min)

        while (running) { // Bucle principal que se ejecuta mientras la simulación esté activa
            if (eventQueue.isEmpty()) { // Verifica si la cola de eventos está vacía
                break; // Si no hay más eventos, termina el bucle
            }

            while (paused && running) { // Bucle interno que maneja el estado de pausa
                try { // Bloque try para manejar interrupciones del thread
                    Thread.sleep(100); // Espera 100 ms mientras está pausado
                } catch (InterruptedException e) { // Captura excepción de interrupción
                    Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                    return; // Sale del método run
                }
            }

            if (!running) { // Verifica nuevamente si se debe detener la simulación
                break; // Sale del bucle principal si running es false
            }

            Event nextEvent = eventQueue.peek(); // Obtiene el siguiente evento sin removerlo de la cola
            if (nextEvent == null) { // Verifica si el evento es nulo
                break; // Si es nulo, termina el bucle
            }

            double targetSimTime = nextEvent.getTime(); // Obtiene el tiempo del próximo evento a procesar
            if (targetSimTime > endTime) { // Verifica si el evento ocurre después del tiempo final
                currentTime = endTime; // Ajusta el tiempo actual al tiempo final
                break; // Termina la simulación
            }

            if (simulationSpeed <= 0.0) { // Verifica si la velocidad es cero o negativa
                try { // Bloque try para manejar la pausa
                    Thread.sleep(50); // Espera 50 ms cuando la velocidad es cero
                } catch (InterruptedException e) { // Captura excepción de interrupción
                    Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                    return; // Sale del método run
                }
                continue; // Continúa al siguiente ciclo del bucle sin procesar eventos
            }

            long currentRealTime = System.currentTimeMillis(); // Obtiene el tiempo real actual del sistema
            double elapsedRealSeconds = (currentRealTime - lastRealTime) / 1000.0; // Calcula segundos reales transcurridos
            double simulatedMinutes = elapsedRealSeconds * simulationSpeed; // Calcula minutos simulados basados en velocidad
            double timeUntilEvent = targetSimTime - currentTime; // Calcula tiempo restante hasta el próximo evento

            // MEJORADO: Control de velocidad más preciso
            if (timeUntilEvent > simulatedMinutes) { // Verifica si aún no es tiempo de procesar el evento
                // Calcular cuánto tiempo real esperar
                long waitTimeMs = (long) ((timeUntilEvent / simulationSpeed) * 1000); // Calcula milisegundos de espera necesarios
                
                // Para velocidades bajas, asegurar delays visibles
                if (simulationSpeed < 10) { // Si la velocidad es menor a 10 min/seg
                    waitTimeMs = Math.min(waitTimeMs, 100); // Limita la espera a máximo 100ms para suavidad visual
                } else if (simulationSpeed < 100) { // Si la velocidad está entre 10 y 100 min/seg
                    waitTimeMs = Math.min(waitTimeMs, 50); // Limita la espera a máximo 50ms
                } else { // Si la velocidad es mayor o igual a 100 min/seg
                    waitTimeMs = Math.min(waitTimeMs, 10); // Limita la espera a máximo 10ms para velocidades altas
                }
                
                if (waitTimeMs > 0) { // Verifica si hay tiempo de espera a aplicar
                    try { // Bloque try para manejar la espera
                        Thread.sleep(waitTimeMs); // Espera el tiempo calculado en milisegundos
                    } catch (InterruptedException e) { // Captura excepción de interrupción
                        Thread.currentThread().interrupt(); // Restaura el estado de interrupción
                        return; // Sale del método run
                    }
                }
            }

            lastRealTime = System.currentTimeMillis(); // Actualiza la marca de tiempo real después de la espera
            eventQueue.poll(); // Remueve el evento de la cola para procesarlo
            currentTime = targetSimTime; // Avanza el tiempo de simulación al tiempo del evento

            nextEvent.execute(this); // Ejecuta el evento pasando esta instancia del engine como parámetro
        }

        statistics.finalizeStatistics(endTime); // Finaliza y calcula las estadísticas finales de la simulación
        currentTime = Math.min(currentTime, endTime); // Asegura que el tiempo no exceda el tiempo final
        running = false; // Marca la simulación como detenida
        paused = false; // Marca la simulación como no pausada
    }

    // === MANEJO DE EVENTOS PRINCIPALES ===

    public void handleArrival(double time) { // Método que maneja el evento de arribo de un nuevo cliente
        Entity entity = new Entity(time); // Crea una nueva entidad con el tiempo de llegada
        statistics.recordArrival(); // Registra el arribo en las estadísticas
        allActiveEntities.add(entity); // Agrega la entidad a la lista de entidades activas

        entrada.enter(entity, time); // La entidad entra a la locación ENTRADA
        entity.setCurrentLocation("ENTRADA"); // Establece ENTRADA como la locación actual de la entidad

        if (time < params.getSimulationDurationMinutes()) { // Verifica si aún hay tiempo para más arribos
            double nextArrival = time + randomGen.nextArrivalTime(); // Genera el tiempo del próximo arribo usando distribución exponencial
            if (nextArrival <= params.getSimulationDurationMinutes()) { // Verifica que el próximo arribo ocurra antes del fin
                scheduleEvent(new ArrivalEvent(nextArrival)); // Programa el siguiente evento de arribo
            }
        }

        if (entity.getRoutingDestination() == null) { // Verifica si la entidad aún no tiene destino asignado
            boolean directo = randomGen.goDirectoASala(); // Genera decisión aleatoria: 90% directo, 10% a formas
            entity.setRoutingDestination(directo ? "SALA" : "FORMAS"); // Asigna el destino según la decisión aleatoria
        }

        scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "ENTRADA")); // Programa el fin de proceso en ENTRADA (casi inmediato)
    }

    public void handleProcessEnd(Entity entity, String locationName, double time) { // Método que maneja el fin de un proceso en una locación
        switch (locationName) { // Evalúa el nombre de la locación donde terminó el proceso
            case "ENTRADA": // Si terminó en ENTRADA
                finishEntrada(entity, time); // Llama al método específico para finalizar ENTRADA
                break; // Sale del switch
            case "ZONA_FORMAS": // Si terminó en ZONA_FORMAS
                finishZonaFormas(entity, time); // Llama al método específico para finalizar ZONA_FORMAS
                break; // Sale del switch
            case "SALA_DE_PIE": // Si terminó en SALA_DE_PIE
                finishSalaDePie(entity, time); // Llama al método específico para finalizar SALA_DE_PIE
                break; // Sale del switch
            case "SALA_SILLAS": // Si terminó en SALA_SILLAS
                finishSalaSillas(entity, time); // Llama al método específico para finalizar SALA_SILLAS
                break; // Sale del switch
            case "SERVIDOR_1": // Si terminó en SERVIDOR_1
                finishServidor1(entity, time); // Llama al método específico para finalizar SERVIDOR_1
                break; // Sale del switch
            case "SERVIDOR_2": // Si terminó en SERVIDOR_2
                finishServidor2(entity, time); // Llama al método específico para finalizar SERVIDOR_2
                break; // Sale del switch
            default: // Si no coincide con ninguna locación conocida
                break; // No hace nada
        }
    }

    // === MÉTODOS FINISH ===

    private void finishEntrada(Entity entity, double time) { // Método que procesa la salida de una entidad de ENTRADA
        String destination = entity.getRoutingDestination(); // Obtiene el destino asignado a la entidad (SALA o FORMAS)
        entrada.exit(entity, time); // Remueve la entidad de la locación ENTRADA

        if ("FORMAS".equals(destination)) { // Verifica si la entidad debe ir a llenar formas (10%)
            zonaFormas.enter(entity, time); // La entidad entra a ZONA_FORMAS
            entity.setCurrentLocation("ZONA_FORMAS"); // Actualiza la locación actual de la entidad
            double formasTime = randomGen.nextZonaFormasTime(); // Genera tiempo aleatorio uniforme entre 4 y 8 minutos
            entity.addProcessTime(formasTime); // Suma el tiempo de proceso a las estadísticas de la entidad
            scheduleEvent(new ProcessEndEvent(time + formasTime, entity, "ZONA_FORMAS")); // Programa el fin de llenado de formas
        } else { // Si la entidad va directo a la sala (90%)
            tryEnterSala(entity, time); // Intenta entrar a la sala de espera
        }
        updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera para estadísticas
    }

    private void finishZonaFormas(Entity entity, double time) { // Método que procesa la salida de ZONA_FORMAS
        zonaFormas.exit(entity, time); // Remueve la entidad de ZONA_FORMAS
        entity.setRoutingDestination(null); // Limpia el destino de ruteo ya completado
        tryEnterSala(entity, time); // Intenta entrar a la sala de espera
    }

    private void tryEnterSala(Entity entity, double time) { // Método que intenta asignar una entidad a sala de sillas o de pie
        if (salaSillas.canEnter()) { // Verifica si hay capacidad disponible en SALA_SILLAS (menos de 40)
            salaSillas.enter(entity, time); // La entidad entra a SALA_SILLAS
            entity.setCurrentLocation("SALA_SILLAS"); // Actualiza la locación actual
            entity.setBlocked(false, time); // Marca la entidad como no bloqueada (tiene silla)
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS")); // Programa el intento de ir a servidor casi inmediatamente
        } else { // Si SALA_SILLAS está llena (40 personas)
            salaDePie.enter(entity, time); // La entidad entra a SALA_DE_PIE
            entity.setCurrentLocation("SALA_DE_PIE"); // Actualiza la locación actual
            entity.setBlocked(true, time); // Marca la entidad como bloqueada (esperando de pie)
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_DE_PIE")); // Programa el intento de conseguir silla
        }
        updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera
    }

    private void finishSalaDePie(Entity entity, double time) { // Método que maneja intentos de conseguir silla desde sala de pie
        if (!"SALA_DE_PIE".equals(entity.getCurrentLocation())) { // Verifica que la entidad aún esté en SALA_DE_PIE
            return; // Si ya no está, no hace nada
        }

        if (salaSillas.canEnter()) { // Verifica si se liberó una silla en SALA_SILLAS
            salaDePie.exit(entity, time); // Remueve la entidad de SALA_DE_PIE
            salaSillas.enter(entity, time); // La entidad entra a SALA_SILLAS
            entity.setCurrentLocation("SALA_SILLAS"); // Actualiza la locación actual
            entity.setBlocked(false, time); // Marca la entidad como no bloqueada (ya tiene silla)
            scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS")); // Programa el intento de ir a servidor
            updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera
        }
    }

    private void finishSalaSillas(Entity entity, double time) { // Método que maneja el intento de ir a un servidor desde sala de sillas
        if (!"SALA_SILLAS".equals(entity.getCurrentLocation())) { // Verifica que la entidad aún esté en SALA_SILLAS
            return; // Si ya no está, no hace nada
        }

        // PRIORIDAD: Siempre intentar SERVIDOR_1 primero (FIRST)
        if (!servidor1Paused && servidor1.canEnter()) { // Verifica si SERVIDOR_1 está disponible y no pausado
            servidor1.reserveCapacity(); // Reserva la capacidad del servidor 1
            salaSillas.exit(entity, time); // Remueve la entidad de SALA_SILLAS
            wakeUpStandingRoom(time); // Despierta a personas de pie para que intenten conseguir la silla liberada
            updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera
            
            servidor1.commitReservedCapacity(); // Confirma la reserva de capacidad del servidor 1
            servidor1.enter(entity, time); // La entidad entra a SERVIDOR_1
            entity.setCurrentLocation("SERVIDOR_1"); // Actualiza la locación actual
            double servicioTime = randomGen.nextServicioTime(); // Genera tiempo de servicio exponencial con media 6 min
            entity.addProcessTime(servicioTime); // Suma el tiempo de proceso a las estadísticas de la entidad
            scheduleEvent(new ProcessEndEvent(time + servicioTime, entity, "SERVIDOR_1")); // Programa el fin de servicio
        } else if (!servidor2Paused && servidor2.canEnter()) { // Si SERVIDOR_1 no disponible, verifica SERVIDOR_2
            servidor2.reserveCapacity(); // Reserva la capacidad del servidor 2
            salaSillas.exit(entity, time); // Remueve la entidad de SALA_SILLAS
            wakeUpStandingRoom(time); // Despierta a personas de pie para que intenten conseguir la silla liberada
            updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera
            
            servidor2.commitReservedCapacity(); // Confirma la reserva de capacidad del servidor 2
            servidor2.enter(entity, time); // La entidad entra a SERVIDOR_2
            entity.setCurrentLocation("SERVIDOR_2"); // Actualiza la locación actual
            double servicioTime = randomGen.nextServicioTime(); // Genera tiempo de servicio exponencial con media 6 min
            entity.addProcessTime(servicioTime); // Suma el tiempo de proceso a las estadísticas de la entidad
            scheduleEvent(new ProcessEndEvent(time + servicioTime, entity, "SERVIDOR_2")); // Programa el fin de servicio
        }
        // Si ambos servidores están ocupados, el cliente espera en SALA_SILLAS
    }

    private void finishServidor1(Entity entity, double time) { // Método que maneja la finalización del servicio en SERVIDOR_1
        pasaportesAtendidosServidor1++; // Incrementa el contador de pasaportes procesados

        if (pasaportesAtendidosServidor1 >= params.getPasaportesPorPausa()) { // Verifica si alcanzó el límite de 10 pasaportes
            double pausaTime = randomGen.nextPausaServidorTime(); // Genera tiempo de pausa exponencial con media 5 min
            pasaportesAtendidosServidor1 = 0; // Reinicia el contador a cero para el próximo ciclo
            servidor1Paused = true; // Marca el servidor 1 como en pausa
            entity.addProcessTime(pausaTime); // Suma el tiempo de pausa a las estadísticas de la entidad
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, entity, "SERVIDOR_1")); // Programa el fin de la pausa
        } else { // Si aún no alcanza los 10 pasaportes
            completeServerExit(servidor1, entity, time); // Completa la salida normal del servidor sin pausa
        }
    }

    private void finishServidor2(Entity entity, double time) { // Método que maneja la finalización del servicio en SERVIDOR_2
        pasaportesAtendidosServidor2++; // Incrementa el contador de pasaportes procesados

        if (pasaportesAtendidosServidor2 >= params.getPasaportesPorPausa()) { // Verifica si alcanzó el límite de 10 pasaportes
            double pausaTime = randomGen.nextPausaServidorTime(); // Genera tiempo de pausa exponencial con media 5 min
            pasaportesAtendidosServidor2 = 0; // Reinicia el contador a cero para el próximo ciclo
            servidor2Paused = true; // Marca el servidor 2 como en pausa
            entity.addProcessTime(pausaTime); // Suma el tiempo de pausa a las estadísticas de la entidad
            scheduleEvent(new ServerPauseEndEvent(time + pausaTime, entity, "SERVIDOR_2")); // Programa el fin de la pausa
        } else { // Si aún no alcanza los 10 pasaportes
            completeServerExit(servidor2, entity, time); // Completa la salida normal del servidor sin pausa
        }
    }

    public void handleServerPauseEnd(String serverName, Entity entity, double time) { // Método que maneja el fin de la pausa de un servidor
        if ("SERVIDOR_1".equals(serverName)) { // Verifica si es el servidor 1
            servidor1Paused = false; // Marca el servidor 1 como activo nuevamente
            completeServerExit(servidor1, entity, time); // Completa la salida de la entidad del servidor
        } else if ("SERVIDOR_2".equals(serverName)) { // Verifica si es el servidor 2
            servidor2Paused = false; // Marca el servidor 2 como activo nuevamente
            completeServerExit(servidor2, entity, time); // Completa la salida de la entidad del servidor
        }
    }

    // === MÉTODOS UTILITARIOS ===

    private void scheduleEvent(Event event) { // Método que agrega un evento a la cola de eventos
        eventQueue.add(event); // Inserta el evento en la cola ordenada por tiempo
    }

    private void updateWaitingAreaSnapshot() { // Método que actualiza el snapshot de las áreas de espera
        statistics.updateWaitingAreaSnapshot( // Llama al método de estadísticas para registrar el estado actual
                salaSillas.getCurrentContent(), // Número actual de personas en SALA_SILLAS
                salaDePie.getCurrentContent() // Número actual de personas en SALA_DE_PIE
        );
    }

    public double getCurrentTime() { // Método getter que retorna el tiempo actual de simulación
        return currentTime; // Devuelve el tiempo simulado actual en minutos
    }

    public boolean isRunning() { // Método que verifica si la simulación está en ejecución
        return running; // Devuelve true si está corriendo, false si no
    }

    public void stop() { // Método que detiene completamente la simulación
        running = false; // Marca la simulación como detenida
        paused = false; // Marca la simulación como no pausada
    }

    public void pause() { // Método que pausa la simulación temporalmente
        paused = true; // Marca la simulación como pausada
    }

    public void resume() { // Método que reanuda la simulación después de una pausa
        lastRealTime = System.currentTimeMillis(); // Actualiza la marca de tiempo real para continuar correctamente
        paused = false; // Marca la simulación como no pausada (activa)
    }

    public boolean isPaused() { // Método que verifica si la simulación está pausada
        return paused; // Devuelve true si está pausada, false si no
    }

    public Statistics getStatistics() { // Método getter que retorna el objeto de estadísticas
        return statistics; // Devuelve la instancia de Statistics con todos los datos recopilados
    }

    public Location getLocation(String name) { // Método que obtiene una locación por su nombre
        switch (name) { // Evalúa el nombre de la locación solicitada
            case "ENTRADA": // Si solicita ENTRADA
                return entrada; // Retorna la instancia de entrada
            case "ZONA_FORMAS": // Si solicita ZONA_FORMAS
                return zonaFormas; // Retorna la instancia de zona de formas
            case "SALA_SILLAS": // Si solicita SALA_SILLAS
                return salaSillas; // Retorna la instancia de sala de sillas
            case "SALA_DE_PIE": // Si solicita SALA_DE_PIE
                return salaDePie; // Retorna la instancia de sala de pie
            case "SERVIDOR_1": // Si solicita SERVIDOR_1
                return servidor1; // Retorna la instancia del servidor 1
            case "SERVIDOR_2": // Si solicita SERVIDOR_2
                return servidor2; // Retorna la instancia del servidor 2
            default: // Si el nombre no coincide con ninguna locación
                return null; // Retorna null indicando que no existe
        }
    }

    public Set<Entity> getEntitiesInTransport() { // Método que retorna las entidades en tránsito
        return new HashSet<>(entitiesInTransport); // Retorna una copia del conjunto de entidades en tránsito
    }

    public List<Entity> getAllActiveEntities() { // Método que retorna todas las entidades activas
        synchronized (allActiveEntities) { // Sincroniza el acceso para thread-safety
            return new ArrayList<>(allActiveEntities); // Retorna una copia de la lista de entidades activas
        }
    }

    private void wakeUpWaitingChairs(double time) { // Método que despierta a todos los clientes en SALA_SILLAS
        // Despertar a TODOS los clientes en SALA_SILLAS para que intenten ir al servidor
        // Esto asegura que siempre se priorice SERVIDOR_1 (FIRST)
        for (Entity entity : getAllActiveEntities()) { // Itera sobre todas las entidades activas
            if ("SALA_SILLAS".equals(entity.getCurrentLocation())) { // Verifica si la entidad está en SALA_SILLAS
                scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_SILLAS")); // Programa un nuevo intento de ir a servidor
            }
        }
    }

    private void wakeUpStandingRoom(double time) { // Método que despierta a clientes en SALA_DE_PIE para intentar conseguir silla
        for (Entity entity : getAllActiveEntities()) { // Itera sobre todas las entidades activas
            if ("SALA_DE_PIE".equals(entity.getCurrentLocation())) { // Verifica si la entidad está en SALA_DE_PIE
                scheduleEvent(new ProcessEndEvent(time + 0.01, entity, "SALA_DE_PIE")); // Programa un intento de conseguir silla
            }
        }
    }

    private void completeServerExit(ProcessingLocation servidor, Entity entity, double time) { // Método que completa la salida de una entidad de un servidor
        servidor.exit(entity, time); // Remueve la entidad del servidor
        allActiveEntities.remove(entity); // Remueve la entidad de la lista de entidades activas
        statistics.recordExit(entity, time); // Registra la salida completa del sistema en las estadísticas
        wakeUpWaitingChairs(time); // Despierta a los clientes en sala de sillas para que intenten ir al servidor liberado
        updateWaitingAreaSnapshot(); // Actualiza la instantánea de las áreas de espera
    }

    public int getServerBatchProgress(String serverName) { // Método que retorna el progreso actual de pasaportes procesados
        if ("SERVIDOR_1".equals(serverName)) { // Verifica si es el servidor 1
            return pasaportesAtendidosServidor1; // Retorna el contador de pasaportes del servidor 1
        } else if ("SERVIDOR_2".equals(serverName)) { // Verifica si es el servidor 2
            return pasaportesAtendidosServidor2; // Retorna el contador de pasaportes del servidor 2
        }
        return 0; // Retorna 0 si el nombre del servidor no es válido
    }

    public int getServerBatchTarget() { // Método que retorna la meta de pasaportes antes de pausa
        return params.getPasaportesPorPausa(); // Retorna el parámetro configurado (10 pasaportes)
    }

    public boolean isServerPaused(String serverName) { // Método que verifica si un servidor está pausado
        if ("SERVIDOR_1".equals(serverName)) { // Verifica si es el servidor 1
            return servidor1Paused; // Retorna el estado de pausa del servidor 1
        } else if ("SERVIDOR_2".equals(serverName)) { // Verifica si es el servidor 2
            return servidor2Paused; // Retorna el estado de pausa del servidor 2
        }
        return false; // Retorna false si el nombre del servidor no es válido
    }
}
