package com.simulation.statistics; // Declaración del paquete que contiene las clases relacionadas con la recopilación y cálculo de estadísticas de la simulación

public class TimeWeightedStatistic { // Declaración de la clase pública TimeWeightedStatistic que calcula promedios ponderados por tiempo (útil para métricas como contenido promedio de una locación)
    private double sum; // Variable privada que almacena la suma acumulada del producto de valores por sus duraciones (Σ valor * tiempo)
    private double lastValue; // Variable privada que almacena el último valor registrado antes de la actualización actual
    private double lastTime; // Variable privada que almacena el último tiempo en que se actualizó la estadística
    private double totalTime; // Variable privada que almacena el tiempo total transcurrido desde el inicio hasta la última actualización

    public TimeWeightedStatistic() { // Constructor público que inicializa la estadística ponderada por tiempo sin recibir parámetros
        this.sum = 0; // Inicializa la suma acumulada en 0
        this.lastValue = 0; // Inicializa el último valor en 0
        this.lastTime = 0; // Inicializa el último tiempo en 0
        this.totalTime = 0; // Inicializa el tiempo total en 0
    } // Cierre del constructor TimeWeightedStatistic

    public void update(double value, double time) { // Método público que actualiza la estadística con un nuevo valor en un tiempo específico recibiendo el valor y el tiempo como parámetros
        if (time > lastTime) { // Condición que verifica si el nuevo tiempo es mayor al último tiempo registrado para evitar actualizaciones con tiempos pasados
            sum += lastValue * (time - lastTime); // Acumula el producto del último valor por el tiempo transcurrido desde la última actualización a la suma total
            totalTime = time - 0; // desde el inicio // Actualiza el tiempo total como el tiempo actual menos 0 (desde el inicio de la simulación)
            lastValue = value; // Actualiza el último valor con el nuevo valor recibido
            lastTime = time; // Actualiza el último tiempo con el nuevo tiempo recibido
        } // Cierre del bloque condicional if
    } // Cierre del método update

    public double getAverage() { // Método público que calcula y retorna el promedio ponderado por tiempo sin recibir parámetros y retornando un double
        return totalTime > 0 ? sum / totalTime : 0; // Retorna el promedio dividiendo la suma acumulada entre el tiempo total si el tiempo es positivo, o 0 si no hay tiempo transcurrido usando operador ternario
    } // Cierre del método getAverage

    public void reset() { // Método público que reinicia la estadística a su estado inicial sin recibir parámetros
        sum = 0; // Reinicia la suma acumulada a 0
        lastValue = 0; // Reinicia el último valor a 0
        lastTime = 0; // Reinicia el último tiempo a 0
        totalTime = 0; // Reinicia el tiempo total a 0
    } // Cierre del método reset
} // Cierre de la clase TimeWeightedStatistic
