package com.simulation.resources; // Declaración del paquete que contiene las clases que representan los recursos y locaciones del sistema de simulación

public class TransportResource { // Declaración de la clase pública TransportResource que representa un recurso de transporte que puede estar ocupado o disponible
    private final String name; // Variable privada final que almacena el nombre identificador único del recurso de transporte
    private boolean busy; // Variable privada booleana que almacena el estado del recurso (true si está ocupado, false si está disponible)

    public TransportResource(String name) { // Constructor público que inicializa un recurso de transporte recibiendo el nombre como parámetro
        this.name = name; // Asigna el nombre recibido a la variable de instancia final
        this.busy = false; // Inicializa el estado del recurso como disponible (no ocupado)
    } // Cierre del constructor TransportResource

    public String getName() { // Método público que retorna el nombre del recurso de transporte de tipo String sin recibir parámetros
        return name; // Retorna el nombre identificador del recurso de transporte
    } // Cierre del método getName

    public boolean isBusy() { // Método público que verifica si el recurso de transporte está ocupado retornando boolean sin recibir parámetros
        return busy; // Retorna el estado actual del recurso (true si está ocupado, false si está disponible)
    } // Cierre del método isBusy

    public boolean isAvailable() { // Método público que verifica si el recurso de transporte está disponible retornando boolean sin recibir parámetros
        return !busy; // Retorna true si el recurso no está ocupado (está disponible), false si está ocupado
    } // Cierre del método isAvailable

    public void occupy() { // Método público que marca el recurso de transporte como ocupado sin recibir parámetros y sin retorno
        busy = true; // Establece el estado del recurso como ocupado
    } // Cierre del método occupy

    public void release() { // Método público que libera el recurso de transporte marcándolo como disponible sin recibir parámetros y sin retorno
        busy = false; // Establece el estado del recurso como disponible
    } // Cierre del método release

    public void reset() { // Método público que reinicia el estado del recurso de transporte a su estado inicial sin recibir parámetros y sin retorno
        busy = false; // Reinicia el estado del recurso como disponible (no ocupado)
    } // Cierre del método reset
} // Cierre de la clase TransportResource
