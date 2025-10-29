package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) de la simulación

public abstract class Event implements Comparable<Event> { // Declaración de clase abstracta pública Event que implementa la interfaz Comparable para ordenar eventos por tiempo
    protected double time; // Variable protegida que almacena el tiempo en que ocurrirá el evento en la simulación (en minutos)
    protected Entity entity; // Variable protegida que almacena la referencia a la entidad asociada con este evento

    public Event(double time, Entity entity) { // Constructor público que inicializa un evento recibiendo el tiempo de ocurrencia y la entidad asociada como parámetros
        this.time = time; // Asigna el tiempo recibido como parámetro a la variable de instancia time
        this.entity = entity; // Asigna la entidad recibida como parámetro a la variable de instancia entity
    } // Cierre del constructor Event

    public double getTime() { // Método público getter que retorna el tiempo en que ocurrirá el evento de tipo double
        return time; // Retorna el valor de la variable time
    } // Cierre del método getTime

    public Entity getEntity() { // Método público getter que retorna la entidad asociada al evento de tipo Entity
        return entity; // Retorna el valor de la variable entity
    } // Cierre del método getEntity

    public abstract void execute(SimulationEngine engine); // Método abstracto público que debe ser implementado por las subclases para ejecutar la lógica específica del evento recibiendo el motor de simulación como parámetro

    @Override // Anotación que indica que este método sobrescribe el método compareTo de la interfaz Comparable
    public int compareTo(Event other) { // Método público que compara este evento con otro para determinar el orden de ejecución recibiendo otro evento como parámetro y retornando un entero
        int timeComparison = Double.compare(this.time, other.time); // Compara los tiempos de ambos eventos usando Double.compare y almacena el resultado (-1 si este es menor, 0 si son iguales, 1 si este es mayor)
        if (timeComparison != 0) { // Condición que verifica si los tiempos son diferentes (timeComparison no es cero)
            return timeComparison; // Retorna el resultado de la comparación de tiempos para ordenar eventos por tiempo de ocurrencia
        } // Cierre del bloque condicional if
        // Si los tiempos son iguales, mantener orden de llegada
        return Integer.compare( // Retorna la comparación de los IDs de las entidades para desempatar eventos con el mismo tiempo usando Integer.compare
            this.entity != null ? this.entity.getId() : 0, // Expresión ternaria que obtiene el ID de la entidad de este evento si no es null, o retorna 0 si es null
            other.entity != null ? other.entity.getId() : 0 // Expresión ternaria que obtiene el ID de la entidad del otro evento si no es null, o retorna 0 si es null
        ); // Cierre del paréntesis de Integer.compare
    } // Cierre del método compareTo
} // Cierre de la clase Event
