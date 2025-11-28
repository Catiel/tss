package com.simulation.core; // Declaración del paquete donde se encuentra esta clase

import java.util.PriorityQueue;

public class EventScheduler { // Clase que administra el calendario de eventos de la simulación
    private final PriorityQueue<Event> eventList; // Cola de prioridad que mantiene los eventos ordenados por tiempo y prioridad
    private final SimulationClock clock; // Referencia al reloj de la simulación

    public EventScheduler(SimulationClock clock) { // Constructor que inicializa el planificador de eventos
        this.eventList = new PriorityQueue<>(); // Crea una nueva cola de prioridad vacía para almacenar eventos
        this.clock = clock; // Asigna el reloj de simulación recibido como parámetro
    }

    public void scheduleEvent(Event event) { // Método para programar un nuevo evento en la cola
        eventList.add(event); // Agrega el evento a la cola de prioridad, ordenándolo automáticamente
    }

    public Event getNextEvent() { // Método para obtener y remover el próximo evento a ejecutar
        return eventList.poll(); // Recupera y elimina el evento con menor tiempo programado (y mayor prioridad si hay empate)
    }

    public boolean hasEvents() { // Método para verificar si hay eventos pendientes
        return !eventList.isEmpty(); // Retorna verdadero si la cola no está vacía, falso en caso contrario
    }

    public void clear() { // Método para limpiar todos los eventos de la cola
        eventList.clear(); // Elimina todos los eventos de la cola de prioridad
    }

    public int getEventCount() { // Método para obtener la cantidad de eventos pendientes
        return eventList.size(); // Retorna el número de eventos actualmente en la cola
    }
}
