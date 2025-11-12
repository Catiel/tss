package com.simulation.core; // Declaración del paquete donde reside la clase Event

/**
 * Clase abstracta base para todos los eventos del sistema de simulación DIGEMIC
 */
public abstract class Event implements Comparable<Event> { // Clase abstracta que implementa Comparable para ordenar eventos por tiempo
    protected double time; // Tiempo en minutos cuando ocurre el evento (protected para acceso de subclases)
    protected Entity entity; // Entidad asociada al evento (cliente que experimenta el evento)

    public Event(double time, Entity entity) { // Constructor que inicializa un evento con tiempo y entidad
        this.time = time; // Asigna el tiempo del evento al atributo de la clase
        this.entity = entity; // Asigna la entidad asociada al atributo de la clase
    }

    public double getTime() { // Método getter que retorna el tiempo del evento
        return time; // Devuelve el valor del tiempo en minutos
    }

    public Entity getEntity() { // Método getter que retorna la entidad asociada
        return entity; // Devuelve la referencia a la entidad del evento
    }

    // Método abstracto compatible con SimulationEngine y DigemicEngine
    public abstract void execute(Object engine); // Método abstracto que cada tipo de evento debe implementar para ejecutar su lógica

    @Override // Anotación que indica sobrescritura del método de la interfaz Comparable
    public int compareTo(Event other) { // Método que compara dos eventos para ordenarlos en la cola de prioridad
        int timeComparison = Double.compare(this.time, other.time); // Compara los tiempos de ambos eventos (retorna -1, 0 o 1)
        if (timeComparison != 0) { // Verifica si los tiempos son diferentes
            return timeComparison; // Retorna el resultado de la comparación de tiempos
        }
        // Si los tiempos son iguales, mantener orden de llegada
        return Integer.compare( // Compara los IDs de las entidades para mantener orden FIFO cuando tiempos son iguales
            this.entity != null ? this.entity.getId() : 0, // Obtiene el ID de la entidad actual o 0 si es null
            other.entity != null ? other.entity.getId() : 0 // Obtiene el ID de la otra entidad o 0 si es null
        );
    }
}
