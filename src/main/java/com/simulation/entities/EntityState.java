package com.simulation.entities; // Declaración del paquete de entidades de la simulación

/**
 * Estados de procesamiento de una entidad en la manufactura
 */
public enum EntityState { // Define la enumeración de estados de procesamiento de entidades
    RAW, // Estado de materia prima sin procesar representado con color gris
    HEAT_TREATED, // Estado después del tratamiento térmico en horno representado con color rojo
    MACHINED // Estado después del proceso de torneado representado con ícono de engrane
}
