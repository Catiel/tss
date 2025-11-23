package com.simulation.core;

import com.simulation.entities.Entity;
import com.simulation.locations.Location;
import com.simulation.resources.Resource;

/**
 * Interfaz para escuchar eventos de la simulación
 */
public interface SimulationListener {
    
    /**
     * Llamado cuando una entidad llega a una locación
     */
    void onEntityArrival(Entity entity, Location location, double time);
    
    /**
     * Llamado cuando una entidad se mueve de una locación a otra
     */
    void onEntityMove(Entity entity, Location from, Location to, double time);
    
    /**
     * Llamado cuando una entidad sale del sistema
     */
    void onEntityExit(Entity entity, Location from, double time);
    
    /**
     * Llamado cuando un recurso es adquirido
     */
    void onResourceAcquired(Resource resource, Entity entity, double time);
    
    /**
     * Llamado cuando un recurso es liberado
     */
    void onResourceReleased(Resource resource, Entity entity, double time);
    
    /**
     * Llamado cuando una entidad es creada
     */
    void onEntityCreated(Entity entity, Location location, double time);
}
