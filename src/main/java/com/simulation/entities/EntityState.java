package com.simulation.entities;

/**
 * Estados de procesamiento de una entidad en la manufactura
 */
public enum EntityState {
    RAW, // Materia prima sin procesar (gris)
    HEAT_TREATED, // Después del horno (rojo)
    MACHINED // Después del torneado (engrane)
}
