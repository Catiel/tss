package com.simulation.resources; // Declaración del paquete que contiene las clases de recursos del sistema de simulación

import com.simulation.core.Entity; // Importa la clase Entity que representa las piezas que fluyen por el sistema
import java.util.HashMap; // Importa la clase HashMap de Java para crear mapas clave-valor
import java.util.HashSet; // Importa la clase HashSet de Java para crear conjuntos sin duplicados
import java.util.Map; // Importa la interfaz Map de Java para trabajar con mapas
import java.util.Set; // Importa la interfaz Set de Java para trabajar con conjuntos

/** // Inicio del comentario Javadoc de la clase
 * Estación de inspección con múltiples mesas // Descripción de la clase
 * CORREGIDO: Manejo adecuado de 2 mesas con 3 operaciones cada una // Nota indicando que está corregida para manejar correctamente múltiples mesas y operaciones
 */ // Fin del comentario Javadoc
public class InspectionStation extends Location { // Declaración de la clase pública InspectionStation que extiende Location para representar una estación de inspección con múltiples mesas y operaciones por pieza
    private final int operationsPerPiece; // Variable final privada que almacena el número de operaciones que debe completar cada pieza en inspección (típicamente 3)
    private final Map<Entity, Integer> operationCounts; // Variable final privada que almacena un mapa de entidades a su contador de operaciones completadas
    private final Set<Entity> reservedEntities; // Variable final privada que almacena un conjunto de entidades que tienen una mesa reservada pero aún no han entrado

    public InspectionStation(String name, int numStations, int operationsPerPiece) { // Constructor público que inicializa la estación de inspección recibiendo el nombre, número de estaciones (mesas) y operaciones por pieza como parámetros
        super(name, numStations);  // capacity = 2 (dos mesas) // Llama al constructor de la clase padre Location con el nombre y número de estaciones (que se convierte en la capacidad)
        this.operationsPerPiece = operationsPerPiece; // Asigna el número de operaciones por pieza recibido a la variable de instancia
        this.operationCounts = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar los contadores de operaciones de cada entidad
        this.reservedEntities = new HashSet<>(); // Crea un nuevo HashSet vacío para almacenar las entidades con mesas reservadas
    } // Cierre del constructor InspectionStation

    @Override // Anotación que indica que este método sobrescribe el método enter de la clase Location
    public void enter(Entity entity, double currentTime) { // Método público que procesa la entrada de una entidad a la estación de inspección recibiendo la entidad y el tiempo actual como parámetros
        commitReservationFor(entity); // Confirma la reservación de mesa para esta entidad removiéndola del conjunto de reservadas
        super.enter(entity, currentTime); // Llama al método enter de la clase padre para registrar la entrada en las estadísticas
        operationCounts.put(entity, 0); // Inicializa el contador de operaciones completadas para esta entidad en 0
    } // Cierre del método enter

    @Override // Anotación que indica que este método sobrescribe el método exit de la clase Location
    public void exit(Entity entity, double currentTime) { // Método público que procesa la salida de una entidad de la estación de inspección recibiendo la entidad y el tiempo actual como parámetros
        operationCounts.remove(entity); // Remueve el contador de operaciones de esta entidad del mapa
        reservedEntities.remove(entity); // Remueve la entidad del conjunto de reservadas por si acaso aún estaba allí
        super.exit(entity, currentTime); // Llama al método exit de la clase padre para registrar la salida en las estadísticas
    } // Cierre del método exit

    /** // Inicio del comentario Javadoc del método
     * Incrementa el contador de operaciones completadas para una entidad // Descripción del método
     */ // Fin del comentario Javadoc
    public void incrementOperationCount(Entity entity) { // Método público que incrementa el contador de operaciones completadas de una entidad recibiendo la entidad como parámetro
        if (operationCounts.containsKey(entity)) { // Condición que verifica si la entidad existe en el mapa de contadores
            operationCounts.put(entity, operationCounts.get(entity) + 1); // Incrementa en 1 el contador de operaciones de esta entidad obteniendo el valor actual, sumando 1 y guardándolo nuevamente
        } // Cierre del bloque condicional if
    } // Cierre del método incrementOperationCount

    /** // Inicio del comentario Javadoc del método
     * Verifica si una entidad completó todas sus operaciones // Descripción del método
     */ // Fin del comentario Javadoc
    public boolean hasCompletedAllOperations(Entity entity) { // Método público que verifica si una entidad completó todas sus operaciones recibiendo la entidad como parámetro y retornando un boolean
        return operationCounts.getOrDefault(entity, 0) >= operationsPerPiece; // Retorna true si el contador de operaciones de la entidad (o 0 si no existe) es mayor o igual al número requerido de operaciones por pieza
    } // Cierre del método hasCompletedAllOperations

    /** // Inicio del comentario Javadoc del método
     * Obtiene el número de operaciones completadas por una entidad // Descripción del método
     */ // Fin del comentario Javadoc
    public int getOperationCount(Entity entity) { // Método público que obtiene el número de operaciones completadas por una entidad recibiendo la entidad como parámetro y retornando un int
        return operationCounts.getOrDefault(entity, 0); // Retorna el contador de operaciones de la entidad desde el mapa, o 0 si la entidad no existe en el mapa
    } // Cierre del método getOperationCount

    @Override // Anotación que indica que este método sobrescribe el método getUtilization de la clase Location
    public double getUtilization(double currentTime) { // Método público que calcula el porcentaje de utilización de la estación de inspección recibiendo el tiempo actual como parámetro y retornando un double
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) { // Condición que verifica si la capacidad es infinita o si el tiempo actual es menor o igual a 0
            return 0.0; // Retorna 0.0 si la capacidad es infinita o no hay tiempo transcurrido
        } // Cierre del bloque condicional if
        double averageBusyStations = getAverageContent(currentTime); // Obtiene el contenido promedio (número promedio de mesas ocupadas) durante el tiempo transcurrido
        return (averageBusyStations / capacity) * 100.0; // Retorna el porcentaje de utilización dividiendo las mesas ocupadas promedio entre la capacidad total y multiplicando por 100
    } // Cierre del método getUtilization

    public boolean hasAvailableStation() { // Método público que verifica si hay una mesa de inspección disponible sin recibir parámetros y retornando un boolean
        return canEnter(); // Retorna el resultado de llamar al método canEnter() de la clase padre que verifica si hay capacidad disponible
    } // Cierre del método hasAvailableStation

    public void reserveStation(Entity entity) { // Método público que reserva una mesa de inspección para una entidad recibiendo la entidad como parámetro
        if (!hasAvailableStation()) { // Condición que verifica si no hay mesas disponibles
            throw new IllegalStateException("No hay mesas de inspección disponibles"); // Lanza una excepción IllegalStateException si se intenta reservar cuando no hay mesas disponibles
        } // Cierre del bloque condicional if
        if (reservedEntities.contains(entity)) { // Condición que verifica si la entidad ya tiene una reservación
            return; // Sale del método prematuramente si la entidad ya tiene una mesa reservada para evitar reservaciones duplicadas
        } // Cierre del bloque condicional if
        reserveCapacity(); // Reserva una unidad de capacidad (una mesa) llamando al método de la clase padre
        reservedEntities.add(entity); // Agrega la entidad al conjunto de entidades con mesas reservadas
    } // Cierre del método reserveStation

    public void commitReservationFor(Entity entity) { // Método público que confirma la reservación de una entidad cuando realmente entra a la estación recibiendo la entidad como parámetro
        if (reservedEntities.remove(entity)) { // Condición que intenta remover la entidad del conjunto de reservadas, retornando true si estaba presente
            commitReservedCapacity(); // Si la entidad estaba reservada, confirma la capacidad reservada llamando al método de la clase padre
        } // Cierre del bloque condicional if
    } // Cierre del método commitReservationFor

    @Override // Anotación que indica que este método sobrescribe el método resetState de la clase Location
    public void resetState() { // Método público que reinicia el estado de la estación de inspección a su estado inicial sin recibir parámetros
        super.resetState(); // Llama al método resetState de la clase padre para reiniciar el estado base de la locación
        operationCounts.clear(); // Limpia el mapa de contadores de operaciones eliminando todas las entradas
        reservedEntities.clear(); // Limpia el conjunto de entidades reservadas eliminando todas las entidades
    } // Cierre del método resetState
} // Cierre de la clase InspectionStation
