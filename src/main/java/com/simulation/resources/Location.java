package com.simulation.resources; // Declaración del paquete que contiene las clases de recursos del sistema de simulación

import com.simulation.core.Entity; // Importa la clase Entity que representa las piezas que fluyen por el sistema
import java.util.*; // Importa todas las clases del paquete util de Java (Queue, LinkedList, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Clase base para todas las locaciones // Descripción de la clase como clase base abstracta
 * CORREGIDO: Cálculo preciso de utilización y tiempos // Nota indicando que los cálculos están corregidos para precisión
 */ // Fin del comentario Javadoc
public abstract class Location { // Declaración de la clase abstracta pública Location que sirve como clase base para todas las locaciones del sistema
    protected String name; // Variable protegida que almacena el nombre de la locación
    protected int capacity; // Variable protegida que almacena la capacidad máxima de la locación (número máximo de entidades que puede contener simultáneamente)
    protected int currentContent; // Variable protegida que almacena el contenido actual de la locación (número de entidades presentes actualmente)
    protected int pendingArrivals; // Variable protegida que almacena el número de llegadas pendientes (entidades que tienen capacidad reservada pero aún no han llegado)
    protected Queue<Entity> queue; // Variable protegida que almacena la cola de entidades esperando para entrar a la locación
    protected int totalEntries; // Variable protegida que almacena el número total de entradas históricas a la locación
    protected int totalExits; // Variable protegida que almacena el número total de salidas históricas de la locación

    // Integrales de tiempo para métricas
    protected double contentTimeIntegral; // Variable protegida que almacena la integral del contenido por tiempo (Σ contenido * tiempo) para calcular contenido promedio
    protected double busyTimeIntegral; // Variable protegida que almacena el tiempo total que la locación ha estado ocupada (con al menos una entidad)
    protected double lastContentUpdateTime; // Variable protegida que almacena el último tiempo en que se actualizaron las integrales para evitar cálculos duplicados

    public Location(String name, int capacity) { // Constructor público que inicializa una locación recibiendo el nombre y capacidad como parámetros
        this.name = name; // Asigna el nombre recibido a la variable de instancia name
        this.capacity = capacity; // Asigna la capacidad recibida a la variable de instancia capacity
        this.currentContent = 0; // Inicializa el contenido actual en 0 (locación vacía)
        this.pendingArrivals = 0; // Inicializa las llegadas pendientes en 0
        this.queue = new LinkedList<>(); // Crea una nueva LinkedList vacía para la cola de espera
        this.totalEntries = 0; // Inicializa el total de entradas en 0
        this.totalExits = 0; // Inicializa el total de salidas en 0
        this.contentTimeIntegral = 0; // Inicializa la integral de contenido-tiempo en 0
        this.busyTimeIntegral = 0; // Inicializa la integral de tiempo ocupado en 0
        this.lastContentUpdateTime = 0; // Inicializa el último tiempo de actualización en 0
    } // Cierre del constructor Location

    /** // Inicio del comentario Javadoc del método
     * Verifica si la locación puede recibir más entidades // Descripción del método
     */ // Fin del comentario Javadoc
    public boolean canEnter() { // Método público que verifica si hay capacidad disponible para que entre una entidad sin recibir parámetros y retornando un boolean
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita (Integer.MAX_VALUE)
            return true; // Retorna true si la capacidad es infinita porque siempre hay espacio
        } // Cierre del bloque condicional if
        return currentContent + pendingArrivals < capacity; // Retorna true si la suma del contenido actual más las llegadas pendientes es menor que la capacidad total
    } // Cierre del método canEnter

    /** // Inicio del comentario Javadoc del método
     * Una entidad entra a la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void enter(Entity entity, double currentTime) { // Método público que procesa la entrada de una entidad a la locación recibiendo la entidad y el tiempo actual como parámetros
        if (currentContent < capacity) { // Condición que verifica si hay espacio disponible en la locación
            updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo antes de cambiar el contenido
            currentContent++; // Incrementa el contenido actual en 1 porque entró una nueva entidad
            if (capacity != Integer.MAX_VALUE && pendingArrivals > 0) { // Condición que verifica si la capacidad no es infinita y hay llegadas pendientes
                pendingArrivals--; // Decrementa las llegadas pendientes en 1 porque una llegada pendiente se materializó
            } // Cierre del bloque condicional if interno
            totalEntries++; // Incrementa el contador total de entradas en 1
        } // Cierre del bloque condicional if externo
    } // Cierre del método enter

    /** // Inicio del comentario Javadoc del método
     * Una entidad sale de la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void exit(Entity entity, double currentTime) { // Método público que procesa la salida de una entidad de la locación recibiendo la entidad y el tiempo actual como parámetros
        if (currentContent > 0) { // Condición que verifica si hay al menos una entidad en la locación
            updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo antes de cambiar el contenido
            currentContent--; // Decrementa el contenido actual en 1 porque salió una entidad
            totalExits++; // Incrementa el contador total de salidas en 1
        } // Cierre del bloque condicional if
    } // Cierre del método exit

    private void updateTimeIntegrals(double currentTime) { // Método privado que actualiza las integrales de tiempo usadas para calcular métricas recibiendo el tiempo actual como parámetro
        if (currentTime > lastContentUpdateTime) { // Condición que verifica si el tiempo actual es mayor al último tiempo de actualización para evitar cálculos duplicados
            double delta = currentTime - lastContentUpdateTime; // Calcula el intervalo de tiempo transcurrido desde la última actualización
            contentTimeIntegral += currentContent * delta; // Acumula el producto del contenido actual por el intervalo de tiempo a la integral de contenido-tiempo
            if (currentContent > 0) { // Condición que verifica si la locación está ocupada (tiene al menos una entidad)
                busyTimeIntegral += delta; // Acumula el intervalo de tiempo a la integral de tiempo ocupado
            } // Cierre del bloque condicional if interno
            lastContentUpdateTime = currentTime; // Actualiza el último tiempo de actualización al tiempo actual
        } // Cierre del bloque condicional if externo
    } // Cierre del método updateTimeIntegrals

    /** // Inicio del comentario Javadoc del método
     * Agrega una entidad a la cola de espera // Descripción del método
     */ // Fin del comentario Javadoc
    public void addToQueue(Entity entity) { // Método público que agrega una entidad a la cola de espera recibiendo la entidad como parámetro
        queue.add(entity); // Agrega la entidad al final de la cola usando el método add de Queue
    } // Cierre del método addToQueue

    /** // Inicio del comentario Javadoc del método
     * Remueve y retorna la siguiente entidad de la cola // Descripción del método
     */ // Fin del comentario Javadoc
    public Entity pollFromQueue() { // Método público que remueve y retorna la primera entidad de la cola sin recibir parámetros y retornando una Entity (o null si está vacía)
        return queue.poll(); // Remueve y retorna el primer elemento de la cola usando el método poll de Queue
    } // Cierre del método pollFromQueue

    /** // Inicio del comentario Javadoc del método
     * Verifica si hay entidades en cola // Descripción del método
     */ // Fin del comentario Javadoc
    public boolean hasQueuedEntities() { // Método público que verifica si hay entidades esperando en la cola sin recibir parámetros y retornando un boolean
        return !queue.isEmpty(); // Retorna true si la cola no está vacía (tiene al menos una entidad esperando)
    } // Cierre del método hasQueuedEntities

    // Getters básicos
    public String getName() { // Método público getter que retorna el nombre de la locación de tipo String
        return name; // Retorna el valor de la variable name
    } // Cierre del método getName

    public int getCapacity() { // Método público getter que retorna la capacidad de la locación de tipo int
        return capacity; // Retorna el valor de la variable capacity
    } // Cierre del método getCapacity

    public int getCurrentContent() { // Método público getter que retorna el contenido actual de la locación de tipo int
        return currentContent; // Retorna el valor de la variable currentContent
    } // Cierre del método getCurrentContent

    public int getQueueSize() { // Método público getter que retorna el tamaño de la cola de espera de tipo int
        return queue.size(); // Retorna el tamaño de la cola llamando al método size() de Queue
    } // Cierre del método getQueueSize

    public int getTotalEntries() { // Método público getter que retorna el total de entradas históricas de tipo int
        return totalEntries; // Retorna el valor de la variable totalEntries
    } // Cierre del método getTotalEntries

    public int getTotalExits() { // Método público getter que retorna el total de salidas históricas de tipo int
        return totalExits; // Retorna el valor de la variable totalExits
    } // Cierre del método getTotalExits

    /** // Inicio del comentario Javadoc del método
     * Calcula el porcentaje de utilización de la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public double getUtilization(double currentTime) { // Método público que calcula el porcentaje de utilización de la locación recibiendo el tiempo actual como parámetro y retornando un double
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) { // Condición que verifica si la capacidad es infinita o si el tiempo actual es menor o igual a 0
            return 0.0; // Retorna 0.0 si la capacidad es infinita o no hay tiempo transcurrido
        } // Cierre del bloque condicional if

        updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo al momento actual antes de calcular
        return (busyTimeIntegral / currentTime) * 100.0; // Retorna el porcentaje de utilización dividiendo el tiempo ocupado entre el tiempo total y multiplicando por 100
    } // Cierre del método getUtilization

    /** // Inicio del comentario Javadoc del método
     * Calcula el tiempo promedio por entrada // Descripción del método
     */ // Fin del comentario Javadoc
    public double getAverageTimePerEntry(double currentTime) { // Método público que calcula el tiempo promedio que transcurre entre cada entrada recibiendo el tiempo actual como parámetro y retornando un double
        if (totalEntries == 0) return 0; // Si no hay entradas, retorna 0 para evitar división por cero
        return currentTime / totalEntries; // Retorna el tiempo promedio por entrada dividiendo el tiempo total entre el número de entradas
    } // Cierre del método getAverageTimePerEntry

    /** // Inicio del comentario Javadoc del método
     * Calcula el contenido promedio ponderado por tiempo // Descripción del método
     * FÓRMULA: Σ(contenido_i * tiempo_i) / tiempo_total // Fórmula matemática utilizada
     */ // Fin del comentario Javadoc
    public double getAverageContent(double currentTime) { // Método público que calcula el contenido promedio de la locación ponderado por tiempo recibiendo el tiempo actual como parámetro y retornando un double
        if (currentTime <= 0) return 0; // Si el tiempo actual es menor o igual a 0, retorna 0 para evitar división por cero
        updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo al momento actual antes de calcular
        return contentTimeIntegral / currentTime; // Retorna el contenido promedio dividiendo la integral de contenido-tiempo entre el tiempo total
    } // Cierre del método getAverageContent

    /** // Inicio del comentario Javadoc del método
     * Reserva capacidad para una llegada en tránsito // Descripción del método
     */ // Fin del comentario Javadoc
    public void reserveCapacity() { // Método público que reserva una unidad de capacidad para una entidad que está en tránsito hacia esta locación
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita
            return; // Sale del método prematuramente si la capacidad es infinita porque no es necesario reservar
        } // Cierre del bloque condicional if
        pendingArrivals++; // Incrementa el contador de llegadas pendientes en 1 para reservar capacidad
    } // Cierre del método reserveCapacity

    /** // Inicio del comentario Javadoc del método
     * Cancela o confirma una reserva previamente hecha // Descripción del método
     */ // Fin del comentario Javadoc
    public void commitReservedCapacity() { // Método público que confirma una reserva de capacidad previamente hecha cuando la entidad realmente llega
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita
            return; // Sale del método prematuramente si la capacidad es infinita porque no hay reservas que confirmar
        } // Cierre del bloque condicional if
        if (pendingArrivals > 0) { // Condición que verifica si hay llegadas pendientes para confirmar
            pendingArrivals--; // Decrementa el contador de llegadas pendientes en 1 porque se confirmó una reserva
        } // Cierre del bloque condicional if interno
    } // Cierre del método commitReservedCapacity

    public int getPendingArrivals() { // Método público getter que retorna el número de llegadas pendientes de tipo int
        return pendingArrivals; // Retorna el valor de la variable pendingArrivals
    } // Cierre del método getPendingArrivals

    /** // Inicio del comentario Javadoc del método
     * Reinicia el estado interno de la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void resetState() { // Método público que reinicia el estado de la locación a su estado inicial sin recibir parámetros
        currentContent = 0; // Reinicia el contenido actual a 0
        pendingArrivals = 0; // Reinicia las llegadas pendientes a 0
        queue.clear(); // Limpia la cola de espera eliminando todas las entidades
        totalEntries = 0; // Reinicia el contador de entradas totales a 0
        totalExits = 0; // Reinicia el contador de salidas totales a 0
        contentTimeIntegral = 0; // Reinicia la integral de contenido-tiempo a 0
        busyTimeIntegral = 0; // Reinicia la integral de tiempo ocupado a 0
        lastContentUpdateTime = 0; // Reinicia el último tiempo de actualización a 0
    } // Cierre del método resetState
} // Cierre de la clase Location
