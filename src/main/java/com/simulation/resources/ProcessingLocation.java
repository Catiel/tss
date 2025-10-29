package com.simulation.resources; // Declaración del paquete que contiene las clases de recursos del sistema de simulación

/** // Inicio del comentario Javadoc de la clase
 * Locación de procesamiento (LAVADORA, PINTURA, HORNO) // Descripción de la clase indicando que representa locaciones que procesan entidades
 * Realiza operaciones sobre las entidades // Nota sobre la funcionalidad de transformar o procesar las entidades que pasan por ella
 */ // Fin del comentario Javadoc
public class ProcessingLocation extends Location { // Declaración de la clase pública ProcessingLocation que extiende Location para representar locaciones que realizan operaciones de procesamiento sobre las entidades

    public ProcessingLocation(String name, int capacity) { // Constructor público que inicializa una locación de procesamiento recibiendo el nombre y capacidad como parámetros
        super(name, capacity); // Llama al constructor de la clase padre Location pasando el nombre y capacidad para inicializar las variables base
    } // Cierre del constructor ProcessingLocation

    @Override // Anotación que indica que este método sobrescribe el método getUtilization de la clase Location
    public double getUtilization(double currentTime) { // Método público que calcula el porcentaje de utilización de la locación de procesamiento recibiendo el tiempo actual como parámetro y retornando un double
        if (capacity == Integer.MAX_VALUE || currentTime <= 0) { // Condición que verifica si la capacidad es infinita (Integer.MAX_VALUE) o si el tiempo actual es menor o igual a 0
            return 0.0; // Retorna 0.0 si la capacidad es infinita o no hay tiempo transcurrido porque no se puede calcular utilización
        } // Cierre del bloque condicional if
        double averageBusyUnits = getAverageContent(currentTime); // Obtiene el contenido promedio de la locación (número promedio de unidades ocupadas procesando) llamando al método de la clase padre
        return (averageBusyUnits / capacity) * 100.0; // Retorna el porcentaje de utilización dividiendo las unidades ocupadas promedio entre la capacidad total y multiplicando por 100 para obtener porcentaje
    } // Cierre del método getUtilization
} // Cierre de la clase ProcessingLocation
