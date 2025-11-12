package com.simulation.statistics; // Declaración del paquete que contiene las clases relacionadas con la recopilación y cálculo de estadísticas de la simulación

import com.simulation.core.Entity; // Importa la clase Entity para acceder a los datos de las entidades que salen del sistema
import com.simulation.resources.Location; // Importa la clase Location para acceder a las locaciones y sus estadísticas

import java.util.*; // Importa todas las clases del paquete util de Java (Map, List, HashMap, ArrayList, Collections, etc.)

public class Statistics { // Declaración de la clase pública Statistics que recopila y calcula todas las estadísticas de la simulación
    private int totalArrivals; // Variable privada que almacena el número total de arribos al sistema
    private int totalExits; // Variable privada que almacena el número total de salidas del sistema (piezas completadas)
    private double totalSystemTime; // Variable privada que almacena la suma acumulada de todos los tiempos en sistema de las entidades que salieron
    private double simulationDuration; // Variable privada que almacena la duración total de la simulación en minutos
    private double totalWaitTime; // Variable privada que almacena la suma de tiempos de espera de las entidades completadas
    private double totalProcessTime; // Variable privada que almacena la suma de tiempos de operación de las entidades completadas
    private double totalTransportTime; // Variable privada que almacena la suma de tiempos de movimiento de las entidades completadas
    private double totalBlockTime; // Variable privada que almacena la suma de tiempos de bloqueo de las entidades completadas

    private Map<String, Location> locations; // Variable privada que almacena un mapa de nombres de locaciones a sus objetos Location
    private List<Double> entitySystemTimes; // Variable privada que almacena una lista de todos los tiempos en sistema individuales para cálculos estadísticos

    public Statistics() { // Constructor público que inicializa el objeto de estadísticas sin recibir parámetros
        this.totalArrivals = 0; // Inicializa el contador de arribos totales en 0
        this.totalExits = 0; // Inicializa el contador de salidas totales en 0
        this.totalSystemTime = 0; // Inicializa el acumulador de tiempo total en sistema en 0
        this.simulationDuration = 0; // Inicializa la duración de la simulación en 0
        this.locations = new HashMap<>(); // Crea un nuevo HashMap vacío para almacenar las locaciones
        this.entitySystemTimes = new ArrayList<>(); // Crea una nueva ArrayList vacía para almacenar los tiempos en sistema individuales
    this.totalWaitTime = 0; // Inicializa el acumulador de tiempo de espera en 0
    this.totalProcessTime = 0; // Inicializa el acumulador de tiempo de operación en 0
    this.totalTransportTime = 0; // Inicializa el acumulador de tiempo de transporte en 0
    this.totalBlockTime = 0; // Inicializa el acumulador de tiempo de bloqueo en 0
    } // Cierre del constructor Statistics

    public void registerLocation(Location location) { // Método público que registra una locación en las estadísticas recibiendo el objeto Location como parámetro
        locations.put(location.getName(), location); // Agrega la locación al mapa usando su nombre como clave y el objeto Location como valor
    } // Cierre del método registerLocation

    public void recordArrival() { // Método público que registra el arribo de una nueva entidad al sistema sin recibir parámetros
        totalArrivals++; // Incrementa el contador de arribos totales en 1
    } // Cierre del método recordArrival

    public void recordExit(Entity entity, double currentTime) { // Método público que registra la salida de una entidad del sistema recibiendo la entidad y el tiempo actual como parámetros
        totalExits++; // Incrementa el contador de salidas totales en 1
        double systemTime = entity.getTotalSystemTime(currentTime); // Obtiene el tiempo total que la entidad pasó en el sistema llamando al método de la entidad
        totalSystemTime += systemTime; // Acumula el tiempo en sistema de esta entidad al total acumulado
        entitySystemTimes.add(systemTime); // Agrega el tiempo en sistema de esta entidad a la lista de tiempos individuales
    totalWaitTime += entity.getTotalWaitTime(); // Acumula el tiempo total de espera de la entidad
    totalProcessTime += entity.getTotalProcessTime(); // Acumula el tiempo total de operación de la entidad
    totalTransportTime += entity.getTotalTransportTime(); // Acumula el tiempo total de transporte de la entidad
    totalBlockTime += entity.getTotalBlockTime(); // Acumula el tiempo total de bloqueo de la entidad
    } // Cierre del método recordExit

    public void finalizeStatistics(double currentTime) { // Método público que finaliza la recopilación de estadísticas recibiendo el tiempo actual como parámetro
        this.simulationDuration = currentTime; // Establece la duración total de la simulación con el tiempo actual final
    } // Cierre del método finalizeStatistics

    public int getTotalArrivals() { // Método público getter que retorna el número total de arribos de tipo int
        return totalArrivals; // Retorna el valor de la variable totalArrivals
    } // Cierre del método getTotalArrivals

    public int getTotalExits() { // Método público getter que retorna el número total de salidas de tipo int
        return totalExits; // Retorna el valor de la variable totalExits
    } // Cierre del método getTotalExits

    public double getAverageSystemTime() { // Método público que calcula el tiempo promedio que las entidades pasan en el sistema sin recibir parámetros y retornando un double
        if (totalExits == 0) return 0; // Si no hay salidas, retorna 0 para evitar división por cero
        return totalSystemTime / totalExits; // Retorna el tiempo promedio dividiendo la suma total de tiempos entre el número de salidas
    } // Cierre del método getAverageSystemTime

    public double getThroughput() { // Método público que calcula el throughput (piezas completadas por hora) sin recibir parámetros y retornando un double
        // Throughput = piezas completadas / tiempo total (en horas)
        if (simulationDuration <= 0) return 0; // Si la duración es menor o igual a 0, retorna 0 para evitar división por cero
        return (totalExits / simulationDuration) * 60.0; // Convertir a piezas/hora // Retorna el throughput dividiendo salidas entre duración en minutos y multiplicando por 60 para convertir a piezas por hora
    } // Cierre del método getThroughput

    public double getSimulationDuration() { // Método público getter que retorna la duración de la simulación de tipo double
        return simulationDuration; // Retorna el valor de la variable simulationDuration
    } // Cierre del método getSimulationDuration

    public double getAverageWaitTime() { // Método público que retorna el tiempo promedio de espera
        if (totalExits == 0) return 0; // Evita división por cero cuando no hay salidas
        return totalWaitTime / totalExits; // Retorna el promedio de tiempos de espera
    }

    public double getAverageProcessTime() { // Método público que retorna el tiempo promedio en operación
        if (totalExits == 0) return 0; // Evita división por cero cuando no hay salidas
        return totalProcessTime / totalExits; // Retorna el promedio de tiempos de operación
    }

    public double getAverageTransportTime() { // Método público que retorna el tiempo promedio en lógica de movimiento
        if (totalExits == 0) return 0; // Evita división por cero cuando no hay salidas
        return totalTransportTime / totalExits; // Retorna el promedio de tiempos de transporte
    }

    public double getAverageBlockTime() { // Método público que retorna el tiempo promedio en bloqueo
        if (totalExits == 0) return 0; // Evita división por cero cuando no hay salidas
        return totalBlockTime / totalExits; // Retorna el promedio de tiempos de bloqueo
    }

    public Map<String, Location> getLocations() { // Método público getter que retorna el mapa de locaciones de tipo Map<String, Location>
        return locations; // Retorna la referencia al mapa de locaciones
    } // Cierre del método getLocations

    public Location getLocation(String name) { // Método público que obtiene una locación específica por su nombre recibiendo el nombre como parámetro y retornando un objeto Location
        return locations.get(name); // Retorna la locación correspondiente al nombre desde el mapa, o null si no existe
    } // Cierre del método getLocation

    public double getLocationUtilization(String locationName, double currentTime) { // Método público que obtiene la utilización de una locación específica recibiendo el nombre de la locación y el tiempo actual como parámetros y retornando un double
        Location loc = locations.get(locationName); // Obtiene el objeto Location correspondiente al nombre desde el mapa
        if (loc == null) return 0; // Si la locación no existe, retorna 0
        return loc.getUtilization(currentTime); // Retorna la utilización de la locación llamando a su método getUtilization con el tiempo actual
    } // Cierre del método getLocationUtilization

    public int getLocationCurrentContent(String locationName) { // Método público que obtiene el contenido actual de una locación específica recibiendo el nombre de la locación como parámetro y retornando un int
        Location loc = locations.get(locationName); // Obtiene el objeto Location correspondiente al nombre desde el mapa
        if (loc == null) return 0; // Si la locación no existe, retorna 0
        return loc.getCurrentContent(); // Retorna el contenido actual de la locación llamando a su método getCurrentContent
    } // Cierre del método getLocationCurrentContent

    public int getLocationQueueSize(String locationName) { // Método público que obtiene el tamaño de la cola de una locación específica recibiendo el nombre de la locación como parámetro y retornando un int
        Location loc = locations.get(locationName); // Obtiene el objeto Location correspondiente al nombre desde el mapa
        if (loc == null) return 0; // Si la locación no existe, retorna 0
        return loc.getQueueSize(); // Retorna el tamaño de la cola de la locación llamando a su método getQueueSize
    } // Cierre del método getLocationQueueSize

    public int getLocationTotalEntries(String locationName) { // Método público que obtiene el total de entradas de una locación específica recibiendo el nombre de la locación como parámetro y retornando un int
        Location loc = locations.get(locationName); // Obtiene el objeto Location correspondiente al nombre desde el mapa
        if (loc == null) return 0; // Si la locación no existe, retorna 0
        return loc.getTotalEntries(); // Retorna el total de entradas de la locación llamando a su método getTotalEntries
    } // Cierre del método getLocationTotalEntries

    public List<Double> getEntitySystemTimes() { // Método público que retorna una copia de la lista de tiempos en sistema sin recibir parámetros y retornando una List<Double>
        return new ArrayList<>(entitySystemTimes); // Retorna una nueva ArrayList con una copia de los tiempos en sistema para evitar modificaciones externas
    } // Cierre del método getEntitySystemTimes

    public double getMinSystemTime() { // Método público que calcula el tiempo mínimo en sistema sin recibir parámetros y retornando un double
        if (entitySystemTimes.isEmpty()) return 0; // Si la lista está vacía, retorna 0
        return Collections.min(entitySystemTimes); // Retorna el valor mínimo de la lista usando el método min de Collections
    } // Cierre del método getMinSystemTime

    public double getMaxSystemTime() { // Método público que calcula el tiempo máximo en sistema sin recibir parámetros y retornando un double
        if (entitySystemTimes.isEmpty()) return 0; // Si la lista está vacía, retorna 0
        return Collections.max(entitySystemTimes); // Retorna el valor máximo de la lista usando el método max de Collections
    } // Cierre del método getMaxSystemTime

    public double getStdDevSystemTime() { // Método público que calcula la desviación estándar de los tiempos en sistema sin recibir parámetros y retornando un double
        if (entitySystemTimes.size() < 2) return 0; // Si hay menos de 2 valores, retorna 0 porque no se puede calcular desviación estándar

        double mean = getAverageSystemTime(); // Obtiene la media de los tiempos en sistema llamando al método getAverageSystemTime
        double sumSquares = 0; // Inicializa el acumulador de suma de cuadrados en 0

        for (double time : entitySystemTimes) { // Bucle for-each que itera sobre cada tiempo en sistema en la lista
            sumSquares += Math.pow(time - mean, 2); // Acumula el cuadrado de la diferencia entre cada tiempo y la media
        } // Cierre del bucle for-each

        return Math.sqrt(sumSquares / entitySystemTimes.size()); // Retorna la desviación estándar calculada como la raíz cuadrada de la suma de cuadrados dividida entre el número de valores
    } // Cierre del método getStdDevSystemTime

    public void reset() { // Método público que reinicia todas las estadísticas a sus valores iniciales sin recibir parámetros
        totalArrivals = 0; // Reinicia el contador de arribos totales a 0
        totalExits = 0; // Reinicia el contador de salidas totales a 0
        totalSystemTime = 0; // Reinicia el acumulador de tiempo total en sistema a 0
        simulationDuration = 0; // Reinicia la duración de la simulación a 0
        entitySystemTimes.clear(); // Limpia la lista de tiempos en sistema eliminando todos los elementos
        totalWaitTime = 0; // Reinicia el acumulador de tiempos de espera a 0
        totalProcessTime = 0; // Reinicia el acumulador de tiempos de operación a 0
        totalTransportTime = 0; // Reinicia el acumulador de tiempos de transporte a 0
        totalBlockTime = 0; // Reinicia el acumulador de tiempos de bloqueo a 0
    } // Cierre del método reset

    @Override // Anotación que indica que este método sobrescribe el método toString de la clase Object
    public String toString() { // Método público que retorna una representación en texto de las estadísticas sin recibir parámetros y retornando un String
        StringBuilder sb = new StringBuilder(); // Crea un nuevo StringBuilder para construir el texto de forma eficiente
        sb.append("=== ESTADÍSTICAS DE SIMULACIÓN ===\n"); // Agrega el título principal de las estadísticas
        sb.append(String.format("Duración: %.2f minutos\n", simulationDuration)); // Agrega la duración de la simulación formateada con 2 decimales
        sb.append(String.format("Total de Arribos: %d\n", totalArrivals)); // Agrega el total de arribos
        sb.append(String.format("Total de Salidas: %d\n", totalExits)); // Agrega el total de salidas
        sb.append(String.format("Throughput: %.2f piezas/hora\n", getThroughput())); // Agrega el throughput formateado con 2 decimales
        sb.append(String.format("Tiempo Promedio en Sistema: %.2f minutos\n", getAverageSystemTime())); // Agrega el tiempo promedio en sistema formateado con 2 decimales
        sb.append(String.format("Desv. Estándar Tiempo en Sistema: %.2f minutos\n", getStdDevSystemTime())); // Agrega la desviación estándar formateada con 2 decimales
        sb.append(String.format("Tiempo Mínimo en Sistema: %.2f minutos\n", getMinSystemTime())); // Agrega el tiempo mínimo formateado con 2 decimales
        sb.append(String.format("Tiempo Máximo en Sistema: %.2f minutos\n", getMaxSystemTime())); // Agrega el tiempo máximo formateado con 2 decimales

        sb.append("\n=== ESTADÍSTICAS POR LOCACIÓN ===\n"); // Agrega el título de la sección de estadísticas por locación
        for (Map.Entry<String, Location> entry : locations.entrySet()) { // Bucle for-each que itera sobre cada entrada del mapa de locaciones
            Location loc = entry.getValue(); // Obtiene el objeto Location de la entrada actual
            sb.append(String.format("\n%s:\n", loc.getName())); // Agrega el nombre de la locación como subtítulo
            sb.append(String.format("  Capacidad: %d\n", // Agrega la capacidad de la locación
                loc.getCapacity() == Integer.MAX_VALUE ? -1 : loc.getCapacity())); // Convirtiendo Integer.MAX_VALUE a -1 para indicar capacidad infinita
            sb.append(String.format("  Contenido Actual: %d\n", loc.getCurrentContent())); // Agrega el contenido actual de la locación
            sb.append(String.format("  Cola Actual: %d\n", loc.getQueueSize())); // Agrega el tamaño actual de la cola
            sb.append(String.format("  Total Entradas: %d\n", loc.getTotalEntries())); // Agrega el total de entradas históricas
            sb.append(String.format("  Total Salidas: %d\n", loc.getTotalExits())); // Agrega el total de salidas históricas
            sb.append(String.format("  Utilización: %.2f%%\n", loc.getUtilization(simulationDuration))); // Agrega la utilización de la locación formateada con 2 decimales y símbolo de porcentaje
        } // Cierre del bucle for-each

        return sb.toString(); // Retorna el StringBuilder convertido a String con todas las estadísticas formateadas
    } // Cierre del método toString
} // Cierre de la clase Statistics
