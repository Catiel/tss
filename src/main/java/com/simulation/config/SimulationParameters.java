// Declaración del paquete donde se encuentra la clase de configuración
package com.simulation.config;


/**
 * Parámetros de configuración para el sistema DIGEMIC
 * Sistema de expedición de pasaportes con 6 locaciones
 */
// Declaración de la clase pública que almacena todos los parámetros de la simulación
public class SimulationParameters {
    // Variable que define la duración total de la simulación: 8 horas convertidas a 480 minutos
    private double simulationDurationMinutes = 480.0;


    // Semilla inicial para el generador de números aleatorios, asegura reproducibilidad de resultados
    private long baseRandomSeed = 12345L;


    // === CAPACIDADES DE LAS 6 LOCACIONES ===
    // Capacidad de la entrada: valor máximo de entero para simular capacidad ilimitada
    private int entradaCapacity = Integer.MAX_VALUE;  // ENTRADA - capacidad infinita
    // Capacidad de la zona de formas: valor máximo de entero para simular capacidad ilimitada
    private int zonaFormasCapacity = Integer.MAX_VALUE; // ZONA_FORMAS - capacidad infinita
    // Capacidad de la sala de sillas: limitada a exactamente 40 personas sentadas
    private int salaSillasCapacity = 40; // SALA_SILLAS - 40 sillas
    // Capacidad de la sala de pie: valor máximo de entero para simular capacidad ilimitada
    private int salaDePieCapacity = Integer.MAX_VALUE; // SALA_DE_PIE - capacidad infinita
    // Capacidad del primer servidor: limitada a 1 persona siendo atendida simultáneamente
    private int servidor1Capacity = 1; // SERVIDOR_1 - 1 servidor
    // Capacidad del segundo servidor: limitada a 1 persona siendo atendida simultáneamente
    private int servidor2Capacity = 1; // SERVIDOR_2 - 1 servidor


    // === PARÁMETROS DE ARRIBOS ===
    // Tiempo promedio entre arribos según distribución exponencial (Poisson 18 clientes/hora)
    // Calculo: 60 min/hora ÷ 18 clientes/hora = 3.33 min/cliente
    private double arrivalMeanTime = 3.33;  // E(3.33) minutos entre arribos


    // === PROCESOS según ProModel DIGEMIC ===
    // Límite inferior de la distribución uniforme para tiempo en zona de formas (mínimo 4 minutos)
    private double zonaFormasMin = 4.0; // Mínimo de la distribución uniforme
    // Límite superior de la distribución uniforme para tiempo en zona de formas (máximo 8 minutos)
    private double zonaFormasMax = 8.0; // Máximo de la distribución uniforme


    // Tiempo promedio de servicio en servidores según distribución exponencial (media de 6 minutos)
    private double servicioMean = 6.0; // Tiempo de atención exponencial con media 6 minutos


    // Tiempo promedio de pausa del servidor según distribución exponencial (media de 5 minutos)
    private double pausaServidorMean = 5.0; // Tiempo exponencial de pausa con media 5 minutos
    // Frecuencia de pausas: cada 10 pasaportes procesados el servidor toma un descanso
    private int pasaportesPorPausa = 10; // Cada 10 pasaportes se hace una pausa


    // === PROBABILIDADES DE ROUTING ===
    // Probabilidad de que un cliente vaya directo a la sala sin llenar formas (90%)
    private double directoASalaProb = 0.90;   // 90% directo a sala
    // Probabilidad de que un cliente necesite llenar formas antes de pasar a la sala (10%)
    private double aFormasProb = 0.10; // 10% a llenar formas


    // === GETTERS ===


    // Método getter que retorna la duración total de la simulación en minutos
    public double getSimulationDurationMinutes() {
        return simulationDurationMinutes; // Devuelve el valor de la duración de la simulación
    }


    // Método setter que permite modificar la duración de la simulación
    public void setSimulationDurationMinutes(double simulationDurationMinutes) {
        this.simulationDurationMinutes = simulationDurationMinutes; // Asigna el nuevo valor al atributo
    }


    // Método getter que retorna la semilla base para el generador aleatorio
    public long getBaseRandomSeed() {
        return baseRandomSeed; // Devuelve el valor de la semilla aleatoria
    }


    // Método setter que permite modificar la semilla aleatoria
    public void setBaseRandomSeed(long baseRandomSeed) {
        this.baseRandomSeed = baseRandomSeed; // Asigna el nuevo valor al atributo
    }


    // Capacidades (6 getters)
    // Getter que retorna la capacidad de la entrada (ilimitada)
    public int getEntradaCapacity() { return entradaCapacity; }
    // Getter que retorna la capacidad de la zona de formas (ilimitada)
    public int getZonaFormasCapacity() { return zonaFormasCapacity; }
    // Getter que retorna la capacidad de la sala de sillas (40 personas)
    public int getSalaSillasCapacity() { return salaSillasCapacity; }
    // Getter que retorna la capacidad de la sala de pie (ilimitada)
    public int getSalaDePieCapacity() { return salaDePieCapacity; }
    // Getter que retorna la capacidad del servidor 1 (1 persona a la vez)
    public int getServidor1Capacity() { return servidor1Capacity; }
    // Getter que retorna la capacidad del servidor 2 (1 persona a la vez)
    public int getServidor2Capacity() { return servidor2Capacity; }


    // Arribos
    // Getter que retorna el tiempo promedio entre arribos de clientes
    public double getArrivalMeanTime() { return arrivalMeanTime; }


    // Procesos
    // Getter que retorna el tiempo mínimo en la zona de formas (distribución uniforme)
    public double getZonaFormasMin() { return zonaFormasMin; }
    // Getter que retorna el tiempo máximo en la zona de formas (distribución uniforme)
    public double getZonaFormasMax() { return zonaFormasMax; }
    // Getter que retorna el tiempo promedio de servicio en los servidores
    public double getServicioMean() { return servicioMean; }
    // Getter que retorna el tiempo promedio de pausa de los servidores
    public double getPausaServidorMean() { return pausaServidorMean; }
    // Getter que retorna la cantidad de pasaportes procesados antes de cada pausa
    public int getPasaportesPorPausa() { return pasaportesPorPausa; }


    // Probabilidades de routing
    // Getter que retorna la probabilidad de ir directo a la sala (90%)
    public double getDirectoASalaProb() { return directoASalaProb; }
    // Getter que retorna la probabilidad de ir a llenar formas (10%)
    public double getAFormasProb() { return aFormasProb; }


    // === SETTERS ===


    // Setter que permite modificar la capacidad de la entrada
    public void setEntradaCapacity(int value) { this.entradaCapacity = value; }
    // Setter que permite modificar la capacidad de la zona de formas
    public void setZonaFormasCapacity(int value) { this.zonaFormasCapacity = value; }
    // Setter que permite modificar la capacidad de la sala de sillas
    public void setSalaSillasCapacity(int value) { this.salaSillasCapacity = value; }
    // Setter que permite modificar la capacidad de la sala de pie
    public void setSalaDePieCapacity(int value) { this.salaDePieCapacity = value; }
    // Setter que permite modificar la capacidad del servidor 1
    public void setServidor1Capacity(int value) { this.servidor1Capacity = value; }
    // Setter que permite modificar la capacidad del servidor 2
    public void setServidor2Capacity(int value) { this.servidor2Capacity = value; }


    // Setter que permite modificar el tiempo promedio entre arribos
    public void setArrivalMeanTime(double value) { this.arrivalMeanTime = value; }
    // Setter que permite modificar el tiempo mínimo en zona de formas
    public void setZonaFormasMin(double value) { this.zonaFormasMin = value; }
    // Setter que permite modificar el tiempo máximo en zona de formas
    public void setZonaFormasMax(double value) { this.zonaFormasMax = value; }
    // Setter que permite modificar el tiempo promedio de servicio
    public void setServicioMean(double value) { this.servicioMean = value; }
    // Setter que permite modificar el tiempo promedio de pausa del servidor
    public void setPausaServidorMean(double value) { this.pausaServidorMean = value; }
    // Setter que permite modificar la cantidad de pasaportes antes de cada pausa
    public void setPasaportesPorPausa(int value) { this.pasaportesPorPausa = value; }


    // Setter que permite modificar la probabilidad de ir directo a la sala
    public void setDirectoASalaProb(double value) { this.directoASalaProb = value; }
    // Setter que permite modificar la probabilidad de ir a llenar formas
    public void setAFormasProb(double value) { this.aFormasProb = value; }
}
