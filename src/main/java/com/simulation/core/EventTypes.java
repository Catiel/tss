package com.simulation.core; // Declaración del paquete que contiene las clases principales (core) de la simulación

import com.simulation.resources.TransportResource;

public class EventTypes { // Declaración de la clase pública EventTypes que agrupa todos los tipos específicos de eventos de la simulación

    // Evento de arribo de pieza
    public static class ArrivalEvent extends Event { // Declaración de clase estática pública anidada ArrivalEvent que extiende Event y representa el arribo de una nueva pieza al sistema
        public ArrivalEvent(double time) { // Constructor público que inicializa un evento de arribo recibiendo solo el tiempo como parámetro
            super(time, null); // Llamada al constructor de la clase padre Event pasando el tiempo y null como entidad porque aún no se ha creado la pieza
        } // Cierre del constructor ArrivalEvent

        @Override // Anotación que indica que este método sobrescribe el método abstracto execute de la clase padre Event
        public void execute(SimulationEngine engine) { // Método público que ejecuta la lógica del evento de arribo recibiendo el motor de simulación como parámetro
            engine.handleArrival(time); // Invoca el método handleArrival del motor de simulación pasando el tiempo del arribo para procesar la llegada de una nueva pieza
        } // Cierre del método execute
    } // Cierre de la clase ArrivalEvent

    // Evento de fin de transporte
    public static class TransportEndEvent extends Event { // Declaración de clase estática pública anidada TransportEndEvent que extiende Event y representa la finalización de un transporte
        private String destinationName; // Variable privada que almacena el nombre de la ubicación de destino donde terminó el transporte
        private TransportResource resource;
        private double resourceReturnTime;

        public TransportEndEvent(double time, Entity entity, String destination) {
            this(time, entity, destination, null, 0.0);
        }

        public TransportEndEvent(double time, Entity entity, String destination, TransportResource resource, double resourceReturnTime) { // Constructor público que inicializa un evento de fin de transporte recibiendo tiempo, entidad y destino como parámetros
            super(time, entity); // Llamada al constructor de la clase padre Event pasando el tiempo y la entidad que está siendo transportada
            this.destinationName = destination; // Asigna el nombre del destino recibido como parámetro a la variable de instancia destinationName
            this.resource = resource;
            this.resourceReturnTime = resourceReturnTime;
        } // Cierre del constructor TransportEndEvent

        @Override // Anotación que indica que este método sobrescribe el método abstracto execute de la clase padre Event
        public void execute(SimulationEngine engine) { // Método público que ejecuta la lógica del evento de fin de transporte recibiendo el motor de simulación como parámetro
            engine.handleTransportEnd(entity, destinationName, time); // Invoca el método handleTransportEnd del motor pasando la entidad, nombre del destino y tiempo para procesar la llegada de la entidad a su destino
            if (resource != null) {
                engine.handleTransportResourceAfterArrival(resource, time, resourceReturnTime);
            }
        } // Cierre del método execute
    } // Cierre de la clase TransportEndEvent

    // Evento de fin de proceso
    public static class ProcessEndEvent extends Event { // Declaración de clase estática pública anidada ProcessEndEvent que extiende Event y representa la finalización de un proceso en una estación
        private String locationName; // Variable privada que almacena el nombre de la ubicación donde finalizó el procesamiento

        public ProcessEndEvent(double time, Entity entity, String location) { // Constructor público que inicializa un evento de fin de proceso recibiendo tiempo, entidad y ubicación como parámetros
            super(time, entity); // Llamada al constructor de la clase padre Event pasando el tiempo y la entidad que está siendo procesada
            this.locationName = location; // Asigna el nombre de la ubicación recibido como parámetro a la variable de instancia locationName
        } // Cierre del constructor ProcessEndEvent

        @Override // Anotación que indica que este método sobrescribe el método abstracto execute de la clase padre Event
        public void execute(SimulationEngine engine) { // Método público que ejecuta la lógica del evento de fin de proceso recibiendo el motor de simulación como parámetro
            engine.handleProcessEnd(entity, locationName, time); // Invoca el método handleProcessEnd del motor pasando la entidad, nombre de ubicación y tiempo para procesar la finalización del procesamiento
        } // Cierre del método execute
    }

    public static class ResourceReleaseEvent extends Event {
        private final TransportResource resource;

        public ResourceReleaseEvent(double time, TransportResource resource) {
            super(time, null);
            this.resource = resource;
        }

        @Override
        public void execute(SimulationEngine engine) {
            engine.handleTransportResourceAvailable(resource, time);
        }
    }

    // NOTA: InspectionOperationEndEvent del sistema viejo fue removido
    // En Multi-Engrane, INSPECCION_1 e INSPECCION_2 son procesos simples sin operaciones múltiples
}

