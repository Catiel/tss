package com.simulation.processing; // Declaración del paquete de procesamiento de la simulación

import com.simulation.core.SimulationEngine; // Importa el motor principal de simulación
import com.simulation.entities.Entity; // Importa la clase que representa entidades individuales

public class BatchProcessingRule extends ProcessingRule { // Define la clase de regla de procesamiento por lotes que extiende ProcessingRule
    private final int batchSize; // Tamaño del lote que define cuántas entidades se procesan juntas

    public BatchProcessingRule(String locationName, String entityTypeName, double processingTime, int batchSize) { // Constructor que recibe ubicación, tipo de entidad, tiempo de procesamiento y tamaño de lote
        super(locationName, entityTypeName, processingTime); // Llama al constructor de la clase padre con los tres primeros parámetros
        this.batchSize = batchSize; // Asigna el tamaño de lote recibido a la variable de instancia
    }

    @Override // Anotación que indica sobrescritura del método process de la clase padre
    public void process(Entity entity, SimulationEngine engine) { // Define el método de procesamiento para entidades individuales
        // La lógica de acumulación se maneja en OperationHandler.handleAccumulate()
        // Esta regla solo define los parámetros
    }

    public int getBatchSize() { // Método getter para obtener el tamaño del lote
        return batchSize; // Retorna el tamaño de lote configurado
    }
}
