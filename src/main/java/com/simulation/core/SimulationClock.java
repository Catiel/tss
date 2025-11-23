package com.simulation.core; // Declaración del paquete donde se encuentra esta clase

public class SimulationClock { // Clase que representa el reloj de la simulación
    private double currentTime; // Variable que almacena el tiempo actual de la simulación

    public SimulationClock() { // Constructor que inicializa el reloj de simulación
        this.currentTime = 0.0; // Inicializa el tiempo actual en cero
    }

    public void advanceTo(double newTime) { // Método para avanzar el reloj a un nuevo tiempo
        if (newTime >= currentTime) { // Verifica que el nuevo tiempo sea mayor o igual al tiempo actual
            currentTime = newTime; // Actualiza el tiempo actual al nuevo tiempo
        } else { // Si el nuevo tiempo es menor al actual
            throw new IllegalArgumentException("No se puede retroceder el tiempo"); // Lanza excepción porque el tiempo no puede retroceder
        }
    }

    public double getCurrentTime() { // Método getter para obtener el tiempo actual
        return currentTime; // Retorna el valor del tiempo actual de la simulación
    }

    public void reset() { // Método para reiniciar el reloj de simulación
        currentTime = 0.0; // Reinicia el tiempo actual a cero
    }
}
