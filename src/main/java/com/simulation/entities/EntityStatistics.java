package com.simulation.entities; // Declaración del paquete donde se encuentra esta clase

public class EntityStatistics { // Clase que recopila y calcula estadísticas para un tipo específico de entidad
    private final String entityName; // Nombre del tipo de entidad para el cual se recopilan estadísticas
    private int totalExits; // Contador de entidades que han salido del sistema
    private int totalCreated; // NUEVO: total de entidades creadas
    private double totalSystemTime; // Suma acumulada del tiempo total de todas las entidades en el sistema
    private double totalValueAddedTime; // Suma acumulada del tiempo con valor agregado de todas las entidades
    private double totalNonValueAddedTime; // Suma acumulada del tiempo sin valor agregado de todas las entidades
    private double totalWaitTime; // Suma acumulada del tiempo de espera de todas las entidades
    private double totalBlockingTime; // Suma acumulada del tiempo de bloqueo de todas las entidades
    private double minSystemTime; // Tiempo mínimo que una entidad ha permanecido en el sistema
    private double maxSystemTime; // Tiempo máximo que una entidad ha permanecido en el sistema

    public EntityStatistics(String entityName) { // Constructor que inicializa las estadísticas para un tipo de entidad
        this.entityName = entityName; // Asigna el nombre del tipo de entidad
        this.totalExits = 0; // Inicializa el contador de salidas en cero
        this.totalCreated = 0; // Inicializa el contador de creaciones en cero
        this.totalSystemTime = 0; // Inicializa el tiempo total acumulado en cero
        this.totalValueAddedTime = 0; // Inicializa el tiempo con valor acumulado en cero
        this.totalNonValueAddedTime = 0; // Inicializa el tiempo sin valor acumulado en cero
        this.totalWaitTime = 0; // Inicializa el tiempo de espera acumulado en cero
        this.totalBlockingTime = 0; // Inicializa el tiempo de bloqueo acumulado en cero
        this.minSystemTime = Double.MAX_VALUE; // Inicializa el mínimo con el valor máximo posible para que cualquier
                                               // tiempo sea menor
        this.maxSystemTime = 0; // Inicializa el máximo en cero
    }

    public void recordEntry() { // Método para registrar la creación de una nueva entidad
        totalCreated++; // Incrementa el contador de entidades creadas
    }

    public void recordExit(Entity entity) { // Método para registrar la salida de una entidad y actualizar estadísticas
        totalExits++; // Incrementa el contador de entidades que han salido

        double systemTime = entity.getTotalSystemTime(); // Obtiene el tiempo total que la entidad estuvo en el sistema
        if (systemTime > 0) { // Verifica que el tiempo sea válido (mayor que cero)
            totalSystemTime += systemTime; // Acumula el tiempo en sistema
            totalValueAddedTime += entity.getTotalValueAddedTime(); // Acumula el tiempo con valor agregado
            totalNonValueAddedTime += entity.getTotalNonValueAddedTime(); // Acumula el tiempo sin valor agregado
            totalWaitTime += entity.getTotalWaitTime(); // Acumula el tiempo de espera
            totalBlockingTime += entity.getTotalBlockingTime(); // Acumula el tiempo de bloqueo

            if (systemTime < minSystemTime) { // Verifica si este tiempo es menor al mínimo actual
                minSystemTime = systemTime; // Actualiza el tiempo mínimo
            }
            if (systemTime > maxSystemTime) { // Verifica si este tiempo es mayor al máximo actual
                maxSystemTime = systemTime; // Actualiza el tiempo máximo
            }
        }
    }

    public String getEntityName() { // Método getter para obtener el nombre del tipo de entidad
        return entityName; // Retorna el nombre de la entidad
    }

    public int getTotalExits() { // Método getter para obtener el total de salidas
        return totalExits; // Retorna el número de entidades que han salido
    }

    public int getCurrentInSystem() { // Método para calcular cuántas entidades están actualmente en el sistema
        return totalCreated - totalExits; // Resta las salidas del total creado para obtener las que permanecen
    }

    public double getAverageSystemTime() { // Método para calcular el tiempo promedio en el sistema
        return totalExits > 0 ? totalSystemTime / totalExits : 0; // Divide el tiempo total entre las salidas, o cero si
                                                                  // no hay salidas
    }

    public double getAverageValueAddedTime() { // Método para calcular el tiempo promedio con valor agregado
        return totalExits > 0 ? totalValueAddedTime / totalExits : 0; // Divide el tiempo con valor entre las salidas, o
                                                                      // cero si no hay salidas
    }

    public double getAverageNonValueAddedTime() { // Método para calcular el tiempo promedio sin valor agregado
        return totalExits > 0 ? totalNonValueAddedTime / totalExits : 0; // Divide el tiempo sin valor entre las
                                                                         // salidas, o cero si no hay salidas
    }

    public double getAverageWaitTime() { // Método para calcular el tiempo promedio de espera
        return totalExits > 0 ? totalWaitTime / totalExits : 0; // Divide el tiempo de espera entre las salidas, o cero
                                                                // si no hay salidas
    }

    public double getAverageBlockingTime() { // Método para calcular el tiempo promedio de bloqueo
        return totalExits > 0 ? totalBlockingTime / totalExits : 0; // Divide el tiempo de bloqueo entre las salidas, o
                                                                    // cero si no hay salidas
    }

    public double getMinSystemTime() { // Método para obtener el tiempo mínimo en el sistema
        return minSystemTime == Double.MAX_VALUE ? 0 : minSystemTime; // Retorna cero si no se ha registrado ninguna
                                                                      // salida, o el mínimo registrado
    }

    public double getMaxSystemTime() { // Método getter para obtener el tiempo máximo en el sistema
        return maxSystemTime; // Retorna el tiempo máximo registrado
    }
}
