package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) del motor de simulación

public class Entity { // Declaración de la clase pública Entity que representa una pieza o unidad que fluye a través del sistema de simulación

    private static int nextId = 1; // Variable estática privada que almacena el siguiente ID disponible para asignar a nuevas entidades, inicializada en 1
    private final int id; // Variable final privada que almacena el identificador único e inmutable de esta entidad
    private double creationTime; // Variable privada que almacena el tiempo en que se creó esta entidad en minutos
    private double systemEntryTime; // Variable privada que almacena el tiempo en que esta entidad entró al sistema de simulación
    private double totalWaitTime; // Variable privada que almacena el tiempo total acumulado que la entidad ha pasado esperando en colas
    private double totalProcessTime; // Variable privada que almacena el tiempo total acumulado que la entidad ha pasado siendo procesada
    private double totalTransportTime; // Variable privada que almacena el tiempo total acumulado que la entidad ha pasado en transporte entre locaciones
    private double totalBlockTime; // Variable privada que almacena el tiempo total acumulado que la entidad ha estado bloqueada
    private String currentLocation; // Variable privada que almacena el nombre de la locación actual donde se encuentra la entidad
    private boolean inTransit; // Variable privada booleana que indica si la entidad está actualmente en tránsito entre locaciones
    private double transitStartTime; // Variable privada que almacena el tiempo en que comenzó el tránsito actual
    private double transitDuration; // Variable privada que almacena la duración total del tránsito actual en minutos
    private String destinationLocation; // Variable privada que almacena el nombre de la locación de destino cuando está en tránsito
    private boolean blocked; // Variable privada booleana que indica si la entidad está actualmente bloqueada
    private double blockStartTime; // Variable privada que almacena el tiempo en que comenzó el bloqueo actual
    private String routingDestination; // NUEVO: Para almacenar decisiones de routing // Variable privada que almacena la decisión de routing probabilístico (a qué locación debe ir después)

    public Entity(double creationTime) { // Constructor público que inicializa una nueva entidad recibiendo el tiempo de creación como parámetro
        this.id = nextId++; // Asigna el siguiente ID disponible a esta entidad e incrementa el contador estático para la próxima entidad
        this.creationTime = creationTime; // Asigna el tiempo de creación recibido a la variable de instancia
        this.systemEntryTime = creationTime; // Establece el tiempo de entrada al sistema igual al tiempo de creación
        this.totalWaitTime = 0; // Inicializa el tiempo total de espera en 0
        this.totalProcessTime = 0; // Inicializa el tiempo total de proceso en 0
        this.totalTransportTime = 0; // Inicializa el tiempo total de transporte en 0
        this.totalBlockTime = 0; // Inicializa el tiempo total de bloqueo en 0
        this.currentLocation = ""; // Inicializa la locación actual como cadena vacía
        this.inTransit = false; // Inicializa el estado de tránsito como falso (no está en tránsito)
        this.transitStartTime = 0; // Inicializa el tiempo de inicio de tránsito en 0
        this.transitDuration = 0; // Inicializa la duración del tránsito en 0
        this.destinationLocation = ""; // Inicializa la locación de destino como cadena vacía
        this.blocked = false; // Inicializa el estado de bloqueo como falso (no está bloqueada)
        this.blockStartTime = 0; // Inicializa el tiempo de inicio de bloqueo en 0
        this.routingDestination = null; // NUEVO // Inicializa el destino de routing como null (sin decisión de routing aún)
    } // Cierre del constructor Entity

    public static void resetIdCounter() { // Método estático público que reinicia el contador de IDs a 1 sin recibir parámetros
        nextId = 1; // Restablece el contador estático de IDs a 1 para comenzar una nueva simulación
    } // Cierre del método resetIdCounter

    // === MÉTODOS DE TIEMPO ===

    public void addProcessTime(double time) { // Método público que acumula tiempo de proceso a la entidad recibiendo el tiempo a agregar como parámetro
        this.totalProcessTime += time; // Suma el tiempo recibido al tiempo total de proceso acumulado
    } // Cierre del método addProcessTime

    public void addTransportTime(double time) { // Método público que acumula tiempo de transporte a la entidad recibiendo el tiempo a agregar como parámetro
        this.totalTransportTime += time; // Suma el tiempo recibido al tiempo total de transporte acumulado
    } // Cierre del método addTransportTime

    public void addWaitTime(double time) { // Método público que acumula tiempo de espera a la entidad recibiendo el tiempo a agregar como parámetro
        this.totalWaitTime += time; // Suma el tiempo recibido al tiempo total de espera acumulado
    } // Cierre del método addWaitTime

    public void addBlockTime(double time) { // Método público que acumula tiempo de bloqueo a la entidad recibiendo el tiempo a agregar como parámetro
        this.totalBlockTime += time; // Suma el tiempo recibido al tiempo total de bloqueo acumulado
    } // Cierre del método addBlockTime

    public void setBlocked(boolean blocked, double currentTime) { // Método público que establece o quita el estado de bloqueo de la entidad recibiendo el estado de bloqueo y el tiempo actual como parámetros
        if (blocked && !this.blocked) { // Condición que verifica si se está activando el bloqueo (blocked es true) y la entidad no estaba bloqueada previamente
            // Inicia bloqueo
            this.blocked = true; // Establece el estado de bloqueo como verdadero
            this.blockStartTime = currentTime; // Registra el tiempo actual como el inicio del bloqueo
        } else if (!blocked && this.blocked) { // Condición que verifica si se está desactivando el bloqueo (blocked es false) y la entidad estaba bloqueada previamente
            // Termina bloqueo
            this.blocked = false; // Establece el estado de bloqueo como falso
            if (blockStartTime > 0) { // Condición que verifica si hay un tiempo de inicio de bloqueo registrado
                this.totalBlockTime += (currentTime - blockStartTime); // Calcula la duración del bloqueo restando el tiempo de inicio del tiempo actual y lo acumula al tiempo total de bloqueo
            } // Cierre del bloque condicional if interno
            this.blockStartTime = 0; // Reinicia el tiempo de inicio de bloqueo a 0
        } // Cierre del bloque else if
    } // Cierre del método setBlocked

    // === TRÁNSITO ===

    public void startTransit(double startTime, double duration, String destination) { // Método público que inicia el tránsito de la entidad hacia una nueva locación recibiendo el tiempo de inicio, duración del tránsito y locación de destino como parámetros
        this.inTransit = true; // Establece el estado de tránsito como verdadero indicando que la entidad está en movimiento
        this.transitStartTime = startTime; // Registra el tiempo de inicio del tránsito
        this.transitDuration = duration; // Registra la duración total del tránsito en minutos
        this.destinationLocation = destination; // Registra el nombre de la locación de destino
    } // Cierre del método startTransit

    public void endTransit() { // Método público que finaliza el tránsito de la entidad cuando llega a su destino sin recibir parámetros
        this.inTransit = false; // Establece el estado de tránsito como falso indicando que la entidad ya no está en movimiento
        this.transitStartTime = 0; // Reinicia el tiempo de inicio de tránsito a 0
        this.transitDuration = 0; // Reinicia la duración del tránsito a 0
        this.destinationLocation = ""; // Reinicia la locación de destino como cadena vacía
    } // Cierre del método endTransit

    public double getTransitProgress(double currentTime) { // Método público que calcula el progreso del tránsito actual como un porcentaje entre 0.0 y 1.0 recibiendo el tiempo actual como parámetro y retornando un double
        if (!inTransit || transitDuration <= 0) { // Condición que verifica si la entidad no está en tránsito o si la duración del tránsito es cero o negativa
            return 1.0; // Retorna 1.0 (100% completo) si no hay tránsito activo
        } // Cierre del bloque condicional if
        double elapsed = currentTime - transitStartTime; // Calcula el tiempo transcurrido desde el inicio del tránsito hasta el tiempo actual
        return Math.min(1.0, elapsed / transitDuration); // Retorna el mínimo entre 1.0 y la proporción del tiempo transcurrido dividido entre la duración total para evitar valores mayores a 1.0
    } // Cierre del método getTransitProgress

    // === GETTERS Y SETTERS ===

    public int getId() { // Método público getter que retorna el ID único de la entidad de tipo int
        return id; // Retorna el valor de la variable id
    } // Cierre del método getId

    public double getCreationTime() { // Método público getter que retorna el tiempo de creación de la entidad de tipo double
        return creationTime; // Retorna el valor de la variable creationTime
    } // Cierre del método getCreationTime

    public double getSystemEntryTime() { // Método público getter que retorna el tiempo de entrada al sistema de tipo double
        return systemEntryTime; // Retorna el valor de la variable systemEntryTime
    } // Cierre del método getSystemEntryTime

    public double getTotalWaitTime() { // Método público getter que retorna el tiempo total de espera de tipo double
        return totalWaitTime; // Retorna el valor de la variable totalWaitTime
    } // Cierre del método getTotalWaitTime

    public double getTotalProcessTime() { // Método público getter que retorna el tiempo total de proceso de tipo double
        return totalProcessTime; // Retorna el valor de la variable totalProcessTime
    } // Cierre del método getTotalProcessTime

    public double getTotalTransportTime() { // Método público getter que retorna el tiempo total de transporte de tipo double
        return totalTransportTime; // Retorna el valor de la variable totalTransportTime
    } // Cierre del método getTotalTransportTime

    public double getTotalBlockTime() { // Método público getter que retorna el tiempo total de bloqueo de tipo double
        return totalBlockTime; // Retorna el valor de la variable totalBlockTime
    } // Cierre del método getTotalBlockTime

    /** // Inicio del comentario Javadoc del método
     * Calcula el tiempo total que la entidad ha estado en el sistema // Descripción del método
     * IMPORTANTE: Nombre debe ser getTotalSystemTime para compatibilidad con Statistics.java // Nota importante sobre el nombre del método para mantener compatibilidad
     */ // Fin del comentario Javadoc
    public double getTotalSystemTime(double currentTime) { // Método público que calcula el tiempo total que la entidad ha permanecido en el sistema recibiendo el tiempo actual como parámetro y retornando un double
        return currentTime - systemEntryTime; // Retorna la diferencia entre el tiempo actual y el tiempo de entrada al sistema
    } // Cierre del método getTotalSystemTime

    public String getCurrentLocation() { // Método público getter que retorna el nombre de la locación actual de tipo String
        return currentLocation; // Retorna el valor de la variable currentLocation
    } // Cierre del método getCurrentLocation

    public void setCurrentLocation(String location) { // Método público setter que establece la locación actual de la entidad recibiendo el nombre de la locación como parámetro
        this.currentLocation = location; // Asigna el nombre de locación recibido a la variable de instancia currentLocation
    } // Cierre del método setCurrentLocation

    public boolean isInTransit() { // Método público getter que retorna si la entidad está en tránsito de tipo boolean
        return inTransit; // Retorna el valor de la variable inTransit
    } // Cierre del método isInTransit

    public double getTransitStartTime() { // Método público getter que retorna el tiempo de inicio del tránsito de tipo double
        return transitStartTime; // Retorna el valor de la variable transitStartTime
    } // Cierre del método getTransitStartTime

    public double getTransitDuration() { // Método público getter que retorna la duración del tránsito de tipo double
        return transitDuration; // Retorna el valor de la variable transitDuration
    } // Cierre del método getTransitDuration

    public String getDestinationLocation() { // Método público getter que retorna el nombre de la locación de destino de tipo String
        return destinationLocation; // Retorna el valor de la variable destinationLocation
    } // Cierre del método getDestinationLocation

    public boolean isBlocked() { // Método público getter que retorna si la entidad está bloqueada de tipo boolean
        return blocked; // Retorna el valor de la variable blocked
    } // Cierre del método isBlocked

    // NUEVO: Métodos para routing probabilístico
    public String getRoutingDestination() { // Método público getter que retorna el destino de routing probabilístico previamente asignado de tipo String
        return routingDestination; // Retorna el valor de la variable routingDestination
    } // Cierre del método getRoutingDestination

    public void setRoutingDestination(String destination) { // Método público setter que establece el destino de routing probabilístico recibiendo el nombre de la locación destino como parámetro
        this.routingDestination = destination; // Asigna el destino recibido a la variable de instancia routingDestination
    } // Cierre del método setRoutingDestination

    @Override // Anotación que indica que este método sobrescribe el método toString de la clase Object
    public String toString() { // Método público que retorna una representación en texto de la entidad sin recibir parámetros y retornando un String
        return String.format("Entity-%d [Location: %s, Transit: %s]", // Retorna una cadena formateada con el ID de la entidad, su locación actual y su estado de tránsito
                id, currentLocation, inTransit ? destinationLocation : "None"); // Parámetros del formato: id, locación actual, y destino si está en tránsito o "None" si no lo está usando operador ternario
    } // Cierre del método toString
} // Cierre de la clase Entity
