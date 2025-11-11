package com.simulation.resources; // Declaración del paquete que contiene las clases que representan los recursos y locaciones del sistema de simulación

import com.simulation.core.Entity; // Importa la clase Entity para poder trabajar con entidades que entran y salen de las locaciones
import java.util.*; // Importa todas las clases del paquete util de Java (List, Set, Map, Queue, LinkedList, etc.)

/** // Inicio del comentario Javadoc de la clase
 * Clase base para todas las locaciones // Descripción de la clase
 * CORREGIDO: Cálculo correcto de utilización considerando capacidad // Nota sobre corrección de cálculo de utilización
 */ // Fin del comentario Javadoc
public abstract class Location { // Declaración de la clase abstracta pública Location que sirve como clase base para todas las locaciones del sistema

    protected String name; // Variable protegida que almacena el nombre identificador de la locación
    protected int capacity; // Variable protegida que almacena la capacidad máxima de entidades que puede contener la locación
    protected int currentContent; // Variable protegida que almacena el número actual de entidades en la locación
    protected int pendingArrivals; // Variable protegida que almacena el número de entidades que han sido reservadas pero aún no han llegado
    protected Queue<Entity> queue; // Variable protegida que almacena una cola de entidades esperando para entrar a la locación
    protected int totalEntries; // Variable protegida que almacena el total acumulado de entidades que han entrado a la locación
    protected int totalExits; // Variable protegida que almacena el total acumulado de entidades que han salido de la locación

    // Integrales de tiempo para métricas
    protected double contentTimeIntegral; // Variable protegida que almacena la integral de tiempo del contenido para calcular promedios ponderados por tiempo
    protected double busyTimeIntegral; // Variable protegida que almacena la integral de tiempo ocupado (cuando hay al menos una entidad) para calcular utilización
    protected double lastContentUpdateTime; // Variable protegida que almacena el último tiempo en que se actualizaron las integrales de tiempo

    public Location(String name, int capacity) { // Constructor público que inicializa una locación recibiendo el nombre y capacidad como parámetros
        this.name = name; // Asigna el nombre recibido a la variable de instancia
        this.capacity = capacity; // Asigna la capacidad recibida a la variable de instancia
        this.currentContent = 0; // Inicializa el contenido actual en 0 (locación vacía)
        this.pendingArrivals = 0; // Inicializa las llegadas pendientes en 0
        this.queue = new LinkedList<>(); // Crea una nueva cola vinculada vacía para las entidades esperando
        this.totalEntries = 0; // Inicializa el total de entradas en 0
        this.totalExits = 0; // Inicializa el total de salidas en 0
        this.contentTimeIntegral = 0; // Inicializa la integral de contenido en 0
        this.busyTimeIntegral = 0; // Inicializa la integral de tiempo ocupado en 0
        this.lastContentUpdateTime = 0; // Inicializa el último tiempo de actualización en 0
    } // Cierre del constructor Location

    /** // Inicio del comentario Javadoc del método
     * Verifica si la locación puede recibir más entidades // Descripción del método
     */ // Fin del comentario Javadoc
    public boolean canEnter() { // Método público que verifica si la locación tiene capacidad disponible retornando boolean sin recibir parámetros
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita
            return true; // Retorna true si la capacidad es infinita (siempre puede entrar)
        } // Cierre del bloque condicional if
        return currentContent + pendingArrivals < capacity; // Retorna true si el contenido actual más las llegadas pendientes es menor a la capacidad
    } // Cierre del método canEnter

    /** // Inicio del comentario Javadoc del método
     * Una entidad entra a la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void enter(Entity entity, double currentTime) { // Método público que maneja la entrada de una entidad a la locación recibiendo la entidad y el tiempo actual como parámetros sin retorno
        if (currentContent < capacity) { // Condición que verifica si hay espacio disponible en la locación
            updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo antes de agregar la entidad
            currentContent++; // Incrementa el contenido actual de la locación
            if (capacity != Integer.MAX_VALUE && pendingArrivals > 0) { // Condición que verifica si hay llegadas pendientes y capacidad no es infinita
                pendingArrivals--; // Decrementa las llegadas pendientes porque una entidad ha llegado
            } // Cierre del bloque condicional if
            totalEntries++; // Incrementa el total de entradas
        } // Cierre del bloque condicional if
    } // Cierre del método enter

    /** // Inicio del comentario Javadoc del método
     * Una entidad sale de la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void exit(Entity entity, double currentTime) { // Método público que maneja la salida de una entidad de la locación recibiendo la entidad y el tiempo actual como parámetros sin retorno
        if (currentContent > 0) { // Condición que verifica si hay al menos una entidad en la locación
            updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo antes de remover la entidad
            currentContent--; // Decrementa el contenido actual de la locación
            totalExits++; // Incrementa el total de salidas
        } // Cierre del bloque condicional if
    } // Cierre del método exit

    private void updateTimeIntegrals(double currentTime) { // Método privado que actualiza las integrales de tiempo acumuladas recibiendo el tiempo actual como parámetro sin retorno
        if (currentTime > lastContentUpdateTime) { // Condición que verifica si ha pasado tiempo desde la última actualización
            double delta = currentTime - lastContentUpdateTime; // Calcula el intervalo de tiempo transcurrido
            contentTimeIntegral += currentContent * delta; // Acumula el contenido multiplicado por el tiempo transcurrido
            if (currentContent > 0) { // Condición que verifica si hay al menos una entidad en la locación
                busyTimeIntegral += delta; // Acumula el tiempo en que la locación estuvo ocupada
            } // Cierre del bloque condicional if
            lastContentUpdateTime = currentTime; // Actualiza el último tiempo de actualización al tiempo actual
        } // Cierre del bloque condicional if
    } // Cierre del método updateTimeIntegrals

    /** // Inicio del comentario Javadoc del método
     * Agrega una entidad a la cola de espera // Descripción del método
     */ // Fin del comentario Javadoc
    public void addToQueue(Entity entity) { // Método público que agrega una entidad a la cola de espera recibiendo la entidad como parámetro sin retorno
        queue.add(entity); // Agrega la entidad al final de la cola
    } // Cierre del método addToQueue

    /** // Inicio del comentario Javadoc del método
     * Remueve y retorna la siguiente entidad de la cola // Descripción del método
     */ // Fin del comentario Javadoc
    public Entity pollFromQueue() { // Método público que remueve y retorna la primera entidad de la cola retornando Entity sin recibir parámetros
        return queue.poll(); // Retorna y remueve el primer elemento de la cola (o null si está vacía)
    } // Cierre del método pollFromQueue

    /** // Inicio del comentario Javadoc del método
     * Verifica si hay entidades en cola // Descripción del método
     */ // Fin del comentario Javadoc
    public boolean hasQueuedEntities() { // Método público que verifica si hay entidades esperando en la cola retornando boolean sin recibir parámetros
        return !queue.isEmpty(); // Retorna true si la cola no está vacía (hay entidades esperando)
    } // Cierre del método hasQueuedEntities

    // Getters básicos
    public String getName() { // Método público que retorna el nombre de la locación de tipo String sin recibir parámetros
        return name; // Retorna el nombre de la locación
    } // Cierre del método getName

    public int getCapacity() { // Método público que retorna la capacidad de la locación de tipo int sin recibir parámetros
        return capacity; // Retorna la capacidad máxima de la locación
    } // Cierre del método getCapacity

    public int getCurrentContent() { // Método público que retorna el contenido actual de la locación de tipo int sin recibir parámetros
        return currentContent; // Retorna el número de entidades actualmente en la locación
    } // Cierre del método getCurrentContent

    public int getQueueSize() { // Método público que retorna el tamaño de la cola de espera de tipo int sin recibir parámetros
        return queue.size(); // Retorna el número de entidades esperando en la cola
    } // Cierre del método getQueueSize

    public int getTotalEntries() { // Método público que retorna el total acumulado de entradas de tipo int sin recibir parámetros
        return totalEntries; // Retorna el total de entidades que han entrado a la locación
    } // Cierre del método getTotalEntries

    public int getTotalExits() { // Método público que retorna el total acumulado de salidas de tipo int sin recibir parámetros
        return totalExits; // Retorna el total de entidades que han salido de la locación
    } // Cierre del método getTotalExits

    /** // Inicio del comentario Javadoc del método
     * Calcula el porcentaje de utilización de la locación // Descripción del método
     * CORREGIDO: Para capacidad > 1, usa contentTimeIntegral / (capacity * currentTime) // Nota sobre la fórmula corregida
     */ // Fin del comentario Javadoc
    public double getUtilization(double currentTime) { // Método público que calcula el porcentaje de utilización de la locación recibiendo el tiempo actual como parámetro y retornando double
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) { // Condición que verifica si la capacidad es infinita o el tiempo es cero o negativo
            return 0.0; // Retorna 0.0 si no se puede calcular la utilización
        } // Cierre del bloque condicional if

        updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo hasta el tiempo actual

        // Para capacidad 1: usar busyTimeIntegral
        if (capacity == 1) { // Condición que verifica si la capacidad es 1 (servidor único)
            return (busyTimeIntegral / currentTime) * 100.0; // Retorna el porcentaje de tiempo ocupado
        } // Cierre del bloque condicional if

        // Para capacidad > 1: usar contentTimeIntegral / (capacity * time)
        return (contentTimeIntegral / (capacity * currentTime)) * 100.0; // Retorna el porcentaje de utilización promedio ponderada
    } // Cierre del método getUtilization

    /** // Inicio del comentario Javadoc del método
     * Calcula el tiempo promedio por entrada // Descripción del método
     */ // Fin del comentario Javadoc
    public double getAverageTimePerEntry(double currentTime) { // Método público que calcula el tiempo promedio por entrada recibiendo el tiempo actual como parámetro y retornando double
        if (totalEntries == 0) return 0; // Si no hay entradas, retorna 0
        return currentTime / totalEntries; // Retorna el tiempo total dividido entre el número de entradas
    } // Cierre del método getAverageTimePerEntry

    /** // Inicio del comentario Javadoc del método
     * Calcula el contenido promedio ponderado por tiempo // Descripción del método
     * FÓRMULA: Σ(contenido_i * tiempo_i) / tiempo_total // Descripción de la fórmula utilizada
     */ // Fin del comentario Javadoc
    public double getAverageContent(double currentTime) { // Método público que calcula el contenido promedio ponderado por tiempo recibiendo el tiempo actual como parámetro y retornando double
        if (currentTime <= 0) return 0; // Si el tiempo es cero o negativo, retorna 0
        updateTimeIntegrals(currentTime); // Actualiza las integrales de tiempo hasta el tiempo actual
        return contentTimeIntegral / currentTime; // Retorna la integral de contenido dividida entre el tiempo total
    } // Cierre del método getAverageContent

    /** // Inicio del comentario Javadoc del método
     * Reserva capacidad para una llegada en tránsito // Descripción del método
     */ // Fin del comentario Javadoc
    public void reserveCapacity() { // Método público que reserva capacidad para una entidad en tránsito sin recibir parámetros y sin retorno
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita
            return; // Sale del método si la capacidad es infinita (no es necesario reservar)
        } // Cierre del bloque condicional if
        pendingArrivals++; // Incrementa el contador de llegadas pendientes
    } // Cierre del método reserveCapacity

    /** // Inicio del comentario Javadoc del método
     * Confirma una reserva previamente hecha // Descripción del método
     */ // Fin del comentario Javadoc
    public void commitReservedCapacity() { // Método público que confirma una reserva previamente hecha sin recibir parámetros y sin retorno
        if (capacity == Integer.MAX_VALUE) { // Condición que verifica si la capacidad es infinita
            return; // Sale del método si la capacidad es infinita (no hay nada que confirmar)
        } // Cierre del bloque condicional if
        if (pendingArrivals > 0) { // Condición que verifica si hay llegadas pendientes
            pendingArrivals--; // Decrementa el contador de llegadas pendientes
        } // Cierre del bloque condicional if
    } // Cierre del método commitReservedCapacity

    public int getPendingArrivals() { // Método público que retorna el número de llegadas pendientes de tipo int sin recibir parámetros
        return pendingArrivals; // Retorna el contador de entidades reservadas pero aún no llegadas
    } // Cierre del método getPendingArrivals

    /** // Inicio del comentario Javadoc del método
     * Reinicia el estado interno de la locación // Descripción del método
     */ // Fin del comentario Javadoc
    public void resetState() { // Método público que reinicia el estado interno de la locación a su estado inicial sin recibir parámetros y sin retorno
        currentContent = 0; // Reinicia el contenido actual a 0
        pendingArrivals = 0; // Reinicia las llegadas pendientes a 0
        queue.clear(); // Limpia la cola de todas las entidades esperando
        totalEntries = 0; // Reinicia el total de entradas a 0
        totalExits = 0; // Reinicia el total de salidas a 0
        contentTimeIntegral = 0; // Reinicia la integral de contenido a 0
        busyTimeIntegral = 0; // Reinicia la integral de tiempo ocupado a 0
        lastContentUpdateTime = 0; // Reinicia el último tiempo de actualización a 0
    } // Cierre del método resetState
} // Cierre de la clase Location
