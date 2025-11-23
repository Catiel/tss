package com.simulation.core; // Declaración del paquete donde se encuentra esta interfaz

import com.simulation.entities.Entity; // Importa la clase Entity que representa entidades en la simulación
import com.simulation.locations.Location; // Importa la clase Location que representa ubicaciones
import com.simulation.resources.Resource; // Importa la clase Resource que representa recursos compartidos

/** // Comentario JavaDoc que documenta la interfaz
 * Interfaz para escuchar eventos de la simulación // Describe el propósito de la interfaz
 */ // Fin del comentario JavaDoc
public interface SimulationListener { // Define la interfaz para implementar el patrón Observer en la simulación

    /** // Comentario JavaDoc para el método onEntityArrival
     * Llamado cuando una entidad llega a una locación // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onEntityArrival(Entity entity, Location location, double time); // Método callback que se ejecuta cuando una entidad arriba a una ubicación

    /** // Comentario JavaDoc para el método onEntityMove
     * Llamado cuando una entidad se mueve de una locación a otra // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onEntityMove(Entity entity, Location from, Location to, double time); // Método callback que se ejecuta cuando una entidad se mueve entre dos ubicaciones

    /** // Comentario JavaDoc para el método onEntityExit
     * Llamado cuando una entidad sale del sistema // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onEntityExit(Entity entity, Location from, double time); // Método callback que se ejecuta cuando una entidad abandona el sistema de simulación

    /** // Comentario JavaDoc para el método onResourceAcquired
     * Llamado cuando un recurso es adquirido // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onResourceAcquired(Resource resource, Entity entity, double time); // Método callback que se ejecuta cuando una entidad toma posesión de un recurso

    /** // Comentario JavaDoc para el método onResourceReleased
     * Llamado cuando un recurso es liberado // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onResourceReleased(Resource resource, Entity entity, double time); // Método callback que se ejecuta cuando una entidad libera un recurso previamente adquirido

    /** // Comentario JavaDoc para el método onEntityCreated
     * Llamado cuando una entidad es creada // Explica cuándo se invoca este método
     */ // Fin del comentario JavaDoc
    void onEntityCreated(Entity entity, Location location, double time); // Método callback que se ejecuta cuando se instancia una nueva entidad en una ubicación
} // Fin de la interfaz SimulationListener
