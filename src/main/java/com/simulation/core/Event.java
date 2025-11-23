package com.simulation.core; // Declaración del paquete donde se encuentra esta clase

public abstract class Event implements Comparable<Event> { // Clase abstracta Event que implementa la interfaz Comparable para ordenar eventos
    protected double scheduledTime; // Tiempo programado en el que se ejecutará el evento
    protected int priority; // Prioridad del evento para ordenamiento cuando ocurren al mismo tiempo
    protected String description; // Descripción textual del evento

    public Event(double scheduledTime, int priority, String description) { // Constructor que inicializa un evento con sus parámetros
        this.scheduledTime = scheduledTime; // Asigna el tiempo programado al atributo de la clase
        this.priority = priority; // Asigna la prioridad al atributo de la clase
        this.description = description; // Asigna la descripción al atributo de la clase
    }

    public abstract void execute(); // Método abstracto que las subclases deben implementar para definir la acción del evento

    public double getScheduledTime() { // Método getter para obtener el tiempo programado
        return scheduledTime; // Retorna el tiempo programado del evento
    }

    public int getPriority() { // Método getter para obtener la prioridad
        return priority; // Retorna la prioridad del evento
    }

    @Override // Anotación que indica que este método sobrescribe el de la interfaz Comparable
    public int compareTo(Event other) { // Método que compara dos eventos para ordenarlos
        int timeComparison = Double.compare(this.scheduledTime, other.scheduledTime); // Compara los tiempos programados de ambos eventos
        if (timeComparison != 0) { // Si los tiempos son diferentes
            return timeComparison; // Retorna el resultado de la comparación por tiempo
        }
        return Integer.compare(this.priority, other.priority); // Si los tiempos son iguales, compara por prioridad
    }
}
