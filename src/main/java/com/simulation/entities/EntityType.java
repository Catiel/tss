package com.simulation.entities; // Declaración del paquete donde se encuentra esta clase

public class EntityType { // Clase que define un tipo de entidad con sus características y estadísticas asociadas
    private final String name; // Nombre identificador del tipo de entidad (inmutable)
    private final double speedMetersPerMinute; // Velocidad de desplazamiento de este tipo de entidad en metros por minuto
    private EntityStatistics statistics; // Objeto que recopila estadísticas de todas las entidades de este tipo

    public EntityType(String name, double speedMetersPerMinute) { // Constructor que inicializa un nuevo tipo de entidad
        this.name = name; // Asigna el nombre del tipo de entidad
        this.speedMetersPerMinute = speedMetersPerMinute; // Asigna la velocidad de desplazamiento
        this.statistics = new EntityStatistics(name); // Crea un nuevo recopilador de estadísticas asociado a este tipo
    }

    public String getName() { // Método getter para obtener el nombre del tipo de entidad
        return name; // Retorna el nombre identificador
    }

    public double getSpeedMetersPerMinute() { // Método getter para obtener la velocidad de desplazamiento
        return speedMetersPerMinute; // Retorna la velocidad en metros por minuto
    }

    public EntityStatistics getStatistics() { // Método getter para obtener el objeto de estadísticas
        return statistics; // Retorna el recopilador de estadísticas asociado
    }

    public void setStatistics(EntityStatistics statistics) { // Método setter para reemplazar el objeto de estadísticas
        this.statistics = statistics; // Actualiza la referencia al recopilador de estadísticas
    }
}
