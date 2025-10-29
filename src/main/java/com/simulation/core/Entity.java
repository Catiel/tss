package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) de la simulación

public class Entity { // Declaración de la clase pública Entity que representa cada pieza/entidad que fluye a través del sistema de simulación
    private static int nextId = 1; // Variable de clase estática que almacena el próximo ID a asignar, comienza en 1 y se incrementa con cada nueva entidad

    private final int id; // Variable de instancia final que almacena el identificador único e inmutable de cada entidad
    private double creationTime; // Variable privada que almacena el tiempo en que fue creada la entidad en la simulación (en minutos)
    private double systemEntryTime; // Variable privada que almacena el tiempo en que la entidad ingresó al sistema de simulación (en minutos)
    private double currentLocationEntryTime; // Variable privada que almacena el tiempo en que la entidad ingresó a su ubicación actual (en minutos)

    private double totalWaitTime; // Variable privada que acumula el tiempo total que la entidad ha esperado en colas (en minutos)
    private double totalProcessTime; // Variable privada que acumula el tiempo total que la entidad ha sido procesada en las estaciones (en minutos)
    private double totalTransportTime; // Variable privada que acumula el tiempo total que la entidad ha estado en transporte entre ubicaciones (en minutos)
    private double totalBlockTime; // Variable privada que acumula el tiempo total que la entidad ha estado bloqueada esperando capacidad disponible (en minutos)

    private String currentLocation; // Variable privada que almacena el nombre de la ubicación actual donde se encuentra la entidad
    private String previousLocation; // Variable privada que almacena el nombre de la ubicación anterior donde estuvo la entidad
    private String destinationLocation; // Variable privada que almacena el nombre de la ubicación de destino hacia donde se dirige la entidad
    private boolean isInTransit; // Variable privada booleana que indica si la entidad está actualmente en tránsito entre ubicaciones
    private double transitStartTime; // Variable privada que almacena el tiempo en que comenzó el tránsito actual (en minutos)
    private double transitDuration; // Variable privada que almacena la duración total del tránsito actual (en minutos)

    private boolean isBlocked; // Variable privada booleana que indica si la entidad está actualmente bloqueada por falta de capacidad en el destino
    private double blockStartTime; // Variable privada que almacena el tiempo en que comenzó el bloqueo actual (en minutos)

    public Entity(double creationTime) { // Constructor público que inicializa una nueva entidad recibiendo como parámetro el tiempo de creación
        this.id = nextId++; // Asigna el ID actual a esta entidad y luego incrementa nextId para la próxima entidad (post-incremento)
        this.creationTime = creationTime; // Asigna el tiempo de creación recibido como parámetro a la variable de instancia
        this.systemEntryTime = creationTime; // Inicializa el tiempo de entrada al sistema con el mismo valor del tiempo de creación
        this.currentLocationEntryTime = creationTime; // Inicializa el tiempo de entrada a la ubicación actual con el tiempo de creación
        this.totalWaitTime = 0; // Inicializa el tiempo total de espera en cero al crear la entidad
        this.totalProcessTime = 0; // Inicializa el tiempo total de procesamiento en cero al crear la entidad
        this.totalTransportTime = 0; // Inicializa el tiempo total de transporte en cero al crear la entidad
        this.totalBlockTime = 0; // Inicializa el tiempo total de bloqueo en cero al crear la entidad
        this.isBlocked = false; // Inicializa el estado de bloqueo como falso (no bloqueada) al crear la entidad
        this.isInTransit = false; // Inicializa el estado de tránsito como falso (no en tránsito) al crear la entidad
        this.transitStartTime = 0; // Inicializa el tiempo de inicio de tránsito en cero al crear la entidad
        this.transitDuration = 0; // Inicializa la duración de tránsito en cero al crear la entidad
    } // Cierre del constructor Entity

    public static void resetIdCounter() { // Método estático público que reinicia el contador de IDs a su valor inicial
        nextId = 1; // Restablece el contador de IDs al valor 1 para comenzar una nueva secuencia de identificadores
    } // Cierre del método resetIdCounter

    public int getId() { // Método público getter que retorna el identificador único de la entidad de tipo int
        return id; // Retorna el valor de la variable id
    } // Cierre del método getId

    public double getCreationTime() { // Método público getter que retorna el tiempo de creación de la entidad de tipo double
        return creationTime; // Retorna el valor de la variable creationTime
    } // Cierre del método getCreationTime

    public double getSystemEntryTime() { // Método público getter que retorna el tiempo de entrada al sistema de tipo double
        return systemEntryTime; // Retorna el valor de la variable systemEntryTime
    } // Cierre del método getSystemEntryTime

    public void setSystemEntryTime(double time) { // Método público setter que permite modificar el tiempo de entrada al sistema recibiendo un parámetro double
        this.systemEntryTime = time; // Asigna el valor del parámetro recibido a la variable de instancia systemEntryTime
    } // Cierre del método setSystemEntryTime

    public double getCurrentLocationEntryTime() { // Método público getter que retorna el tiempo de entrada a la ubicación actual de tipo double
        return currentLocationEntryTime; // Retorna el valor de la variable currentLocationEntryTime
    } // Cierre del método getCurrentLocationEntryTime

    public void setCurrentLocationEntryTime(double time) { // Método público setter que permite modificar el tiempo de entrada a la ubicación actual recibiendo un parámetro double
        this.currentLocationEntryTime = time; // Asigna el valor del parámetro recibido a la variable de instancia currentLocationEntryTime
    } // Cierre del método setCurrentLocationEntryTime

    public String getCurrentLocation() { // Método público getter que retorna el nombre de la ubicación actual de tipo String
        return currentLocation; // Retorna el valor de la variable currentLocation
    } // Cierre del método getCurrentLocation

    public void setCurrentLocation(String location) { // Método público setter que actualiza la ubicación actual y guarda la anterior recibiendo un parámetro String
        this.previousLocation = this.currentLocation; // Guarda la ubicación actual en previousLocation antes de cambiarla
        this.currentLocation = location; // Asigna la nueva ubicación recibida como parámetro a currentLocation
    } // Cierre del método setCurrentLocation

    public String getPreviousLocation() { // Método público getter que retorna el nombre de la ubicación anterior de tipo String
        return previousLocation; // Retorna el valor de la variable previousLocation
    } // Cierre del método getPreviousLocation

    public String getDestinationLocation() { // Método público getter que retorna el nombre de la ubicación de destino de tipo String
        return destinationLocation; // Retorna el valor de la variable destinationLocation
    } // Cierre del método getDestinationLocation

    public void setDestinationLocation(String location) { // Método público setter que permite modificar la ubicación de destino recibiendo un parámetro String
        this.destinationLocation = location; // Asigna el valor del parámetro recibido a la variable de instancia destinationLocation
    } // Cierre del método setDestinationLocation

    public boolean isInTransit() { // Método público getter que retorna si la entidad está en tránsito de tipo boolean
        return isInTransit; // Retorna el valor de la variable isInTransit (true si está en tránsito, false si no)
    } // Cierre del método isInTransit

    public void startTransit(double currentTime, double duration, String destination) { // Método público que inicia el tránsito de una entidad recibiendo tiempo actual, duración y destino como parámetros
        this.isInTransit = true; // Establece el estado de tránsito como verdadero indicando que la entidad comenzó a moverse
        this.transitStartTime = currentTime; // Registra el tiempo actual como el momento de inicio del tránsito
        this.transitDuration = duration; // Almacena la duración total que tomará el tránsito
        this.destinationLocation = destination; // Establece la ubicación de destino hacia donde se dirige la entidad
    } // Cierre del método startTransit

    public void endTransit() { // Método público que finaliza el tránsito de una entidad sin recibir parámetros
        this.isInTransit = false; // Establece el estado de tránsito como falso indicando que la entidad llegó a su destino
        this.transitStartTime = 0; // Reinicia el tiempo de inicio de tránsito a cero
        this.transitDuration = 0; // Reinicia la duración de tránsito a cero
        this.destinationLocation = null; // Elimina la referencia a la ubicación de destino estableciéndola como null
    } // Cierre del método endTransit

    public double getTransitProgress(double currentTime) { // Método público que calcula el progreso del tránsito (0.0 a 1.0) recibiendo el tiempo actual como parámetro
        if (!isInTransit || transitDuration <= 0) { // Condición que verifica si la entidad no está en tránsito o si la duración es cero o negativa
            return 0; // Retorna 0 indicando que no hay progreso de tránsito si no está en tránsito o la duración es inválida
        } // Cierre del bloque condicional if
        double elapsed = currentTime - transitStartTime; // Calcula el tiempo transcurrido desde el inicio del tránsito restando el tiempo de inicio del tiempo actual
        return Math.min(1.0, elapsed / transitDuration); // Retorna el progreso como porcentaje (elapsed/duration) limitado a un máximo de 1.0 usando Math.min
    } // Cierre del método getTransitProgress

    public void addWaitTime(double time) { // Método público que incrementa el tiempo total de espera recibiendo un valor double como parámetro
        this.totalWaitTime += time; // Suma el valor recibido al acumulador de tiempo total de espera
    } // Cierre del método addWaitTime

    public void addProcessTime(double time) { // Método público que incrementa el tiempo total de procesamiento recibiendo un valor double como parámetro
        this.totalProcessTime += time; // Suma el valor recibido al acumulador de tiempo total de procesamiento
    } // Cierre del método addProcessTime

    public void addTransportTime(double time) { // Método público que incrementa el tiempo total de transporte recibiendo un valor double como parámetro
        this.totalTransportTime += time; // Suma el valor recibido al acumulador de tiempo total de transporte
    } // Cierre del método addTransportTime

    public void addBlockTime(double time) { // Método público que incrementa el tiempo total de bloqueo recibiendo un valor double como parámetro
        this.totalBlockTime += time; // Suma el valor recibido al acumulador de tiempo total de bloqueo
    } // Cierre del método addBlockTime

    public double getTotalWaitTime() { // Método público getter que retorna el tiempo total de espera acumulado de tipo double
        return totalWaitTime; // Retorna el valor de la variable totalWaitTime
    } // Cierre del método getTotalWaitTime

    public double getTotalProcessTime() { // Método público getter que retorna el tiempo total de procesamiento acumulado de tipo double
        return totalProcessTime; // Retorna el valor de la variable totalProcessTime
    } // Cierre del método getTotalProcessTime

    public double getTotalTransportTime() { // Método público getter que retorna el tiempo total de transporte acumulado de tipo double
        return totalTransportTime; // Retorna el valor de la variable totalTransportTime
    } // Cierre del método getTotalTransportTime

    public double getTotalBlockTime() { // Método público getter que retorna el tiempo total de bloqueo acumulado de tipo double
        return totalBlockTime; // Retorna el valor de la variable totalBlockTime
    } // Cierre del método getTotalBlockTime

    public double getTotalSystemTime(double currentTime) { // Método público que calcula el tiempo total que la entidad ha estado en el sistema recibiendo el tiempo actual como parámetro
        return currentTime - systemEntryTime; // Retorna la diferencia entre el tiempo actual y el tiempo de entrada al sistema
    } // Cierre del método getTotalSystemTime

    public boolean isBlocked() { // Método público getter que retorna si la entidad está bloqueada de tipo boolean
        return isBlocked; // Retorna el valor de la variable isBlocked (true si está bloqueada, false si no)
    } // Cierre del método isBlocked

    public void setBlocked(boolean blocked, double currentTime) { // Método público setter que actualiza el estado de bloqueo y registra tiempos recibiendo un boolean y un double como parámetros
        if (blocked && !isBlocked) { // Condición que verifica si se está bloqueando la entidad (blocked es true) y no estaba bloqueada previamente (isBlocked es false)
            blockStartTime = currentTime; // Registra el tiempo actual como el momento de inicio del bloqueo
        } else if (!blocked && isBlocked) { // Condición alternativa que verifica si se está desbloqueando la entidad (blocked es false) y estaba bloqueada previamente (isBlocked es true)
            addBlockTime(currentTime - blockStartTime); // Calcula y acumula el tiempo que estuvo bloqueada restando el tiempo de inicio de bloqueo del tiempo actual
        } // Cierre del bloque condicional else if
        this.isBlocked = blocked; // Actualiza el estado de bloqueo con el valor recibido como parámetro
    } // Cierre del método setBlocked
} // Cierre de la clase Entity
