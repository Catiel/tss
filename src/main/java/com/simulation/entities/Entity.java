package com.simulation.entities; // Declaración del paquete de entidades de la simulación

import com.simulation.locations.Location; // Importa la clase Location para referencias de ubicación

public class Entity { // Define la clase que representa una entidad individual en la simulación
    private static int nextId = 1; // Contador estático para generar IDs únicos secuenciales

    private final int id; // Identificador único e inmutable de la entidad
    private final EntityType type; // Tipo de entidad inmutable asignado en construcción
    private Location currentLocation; // Ubicación actual donde se encuentra la entidad
    private double entryTime; // Tiempo de entrada a la ubicación actual
    private double totalSystemTime; // Tiempo total acumulado que la entidad ha estado en el sistema
    private double totalValueAddedTime; // Tiempo total acumulado en actividades que agregan valor
    private double totalNonValueAddedTime; // Tiempo total acumulado en actividades sin valor agregado
    private double totalWaitTime; // Tiempo total acumulado esperando en colas
    private double totalBlockingTime; // Tiempo total acumulado bloqueada por capacidad
    private boolean inSystem; // Flag que indica si la entidad está actualmente en el sistema
    private boolean isTransformed; // Flag que indica si la entidad es resultado de transformación
    private EntityState state; // Estado visual de procesamiento de la entidad

    private final double creationTime; // Tiempo de simulación cuando fue creada la entidad

    public Entity(EntityType type) { // Constructor simple que recibe solo el tipo de entidad
        this(type, false, 0.0); // Delega al constructor completo con valores por defecto
    }

    // Constructor con parámetro para indicar si es transformada
    public Entity(EntityType type, boolean isTransformed) { // Constructor que recibe tipo y flag de transformación
        this(type, isTransformed, 0.0); // Delega al constructor completo con tiempo de creación cero
    }

    public Entity(EntityType type, boolean isTransformed, double creationTime) { // Constructor completo con todos los parámetros
        this.id = nextId++; // Asigna ID único y post-incrementa el contador estático
        this.type = type; // Asigna el tipo de entidad recibido
        this.totalSystemTime = 0; // Inicializa tiempo total en sistema a cero
        this.totalValueAddedTime = 0; // Inicializa tiempo de valor agregado a cero
        this.totalNonValueAddedTime = 0; // Inicializa tiempo sin valor agregado a cero
        this.totalWaitTime = 0; // Inicializa tiempo de espera a cero
        this.totalBlockingTime = 0; // Inicializa tiempo de bloqueo a cero
        this.inSystem = true; // Establece flag de presencia en sistema como verdadero
        this.isTransformed = isTransformed; // Asigna el flag de transformación recibido
        this.creationTime = creationTime; // Asigna el tiempo de creación recibido
        this.state = EntityState.RAW; // Inicializa estado como materia prima sin procesar
    }

    public int getId() { // Método getter para obtener el ID de la entidad
        return id; // Retorna el identificador único
    }

    public EntityType getType() { // Método getter para obtener el tipo de entidad
        return type; // Retorna el tipo asignado
    }

    public Location getCurrentLocation() { // Método getter para obtener la ubicación actual
        return currentLocation; // Retorna la referencia a la ubicación donde está
    }

    public void setCurrentLocation(Location location) { // Método setter para establecer la ubicación actual
        this.currentLocation = location; // Asigna la nueva ubicación recibida
    }

    public double getCreationTime() { // Método getter para obtener el tiempo de creación
        return creationTime; // Retorna el tiempo cuando fue creada
    }

    public double getEntryTime() { // Método getter para obtener el tiempo de entrada
        return entryTime; // Retorna el tiempo de entrada a ubicación actual
    }

    public void setEntryTime(double time) { // Método setter para establecer el tiempo de entrada
        this.entryTime = time; // Asigna el nuevo tiempo de entrada
    }

    public void addSystemTime(double time) { // Método para acumular tiempo en el sistema
        this.totalSystemTime += time; // Incrementa el tiempo total en sistema
    }

    public void addValueAddedTime(double time) { // Método para acumular tiempo de valor agregado
        this.totalValueAddedTime += time; // Incrementa el tiempo de actividades con valor
    }

    public void addNonValueAddedTime(double time) { // Método para acumular tiempo sin valor agregado
        this.totalNonValueAddedTime += time; // Incrementa el tiempo de actividades sin valor
    }

    public void addWaitTime(double time) { // Método para acumular tiempo de espera
        this.totalWaitTime += time; // Incrementa el tiempo total esperando
    }

    public double getTotalSystemTime() { // Método getter para obtener tiempo total en sistema
        return totalSystemTime; // Retorna el tiempo acumulado en sistema
    }

    public double getTotalValueAddedTime() { // Método getter para obtener tiempo de valor agregado
        return totalValueAddedTime; // Retorna el tiempo acumulado con valor
    }

    public double getTotalNonValueAddedTime() { // Método getter para obtener tiempo sin valor agregado
        return totalNonValueAddedTime; // Retorna el tiempo acumulado sin valor
    }

    public double getTotalWaitTime() { // Método getter para obtener tiempo de espera
        return totalWaitTime; // Retorna el tiempo acumulado esperando
    }

    public void addBlockingTime(double time) { // Método para acumular tiempo de bloqueo
        this.totalBlockingTime += time; // Incrementa el tiempo total bloqueada
    }

    public double getTotalBlockingTime() { // Método getter para obtener tiempo de bloqueo
        return totalBlockingTime; // Retorna el tiempo acumulado bloqueada
    }

    public boolean isInSystem() { // Método getter para verificar si está en el sistema
        return inSystem; // Retorna el flag de presencia en sistema
    }

    public void setInSystem(boolean inSystem) { // Método setter para establecer presencia en sistema
        this.inSystem = inSystem; // Asigna el nuevo valor del flag
    }

    public boolean isTransformed() { // Método getter para verificar si es transformada
        return isTransformed; // Retorna el flag de transformación
    }

    public void setTransformed(boolean transformed) { // Método setter para establecer flag de transformación
        isTransformed = transformed; // Asigna el nuevo valor del flag de transformación
    }

    private String pendingDestination; // Destino pendiente almacenado para movimiento futuro

    public String getPendingDestination() { // Método getter para obtener destino pendiente
        return pendingDestination; // Retorna el destino almacenado
    }

    public void setPendingDestination(String destination) { // Método setter para establecer destino pendiente
        this.pendingDestination = destination; // Asigna el nuevo destino a donde se moverá
    }

    public EntityState getState() { // Método getter para obtener el estado de procesamiento
        return state; // Retorna el estado visual actual
    }

    public void setState(EntityState state) { // Método setter para establecer el estado de procesamiento
        this.state = state; // Asigna el nuevo estado visual
    }
}
