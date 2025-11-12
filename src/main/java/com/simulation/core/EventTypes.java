package com.simulation.core; // Declaración del paquete donde residen los tipos de eventos

/**
 * Tipos de eventos para DIGEMIC (Sistema de expedición de pasaportes)
 */
public class EventTypes { // Clase contenedora para todos los tipos de eventos del sistema DIGEMIC

    // ========= EVENTOS DIGEMIC =========
    
    /**
     * Evento de arribo de cliente al sistema DIGEMIC
     */
    public static class ArrivalEvent extends Event { // Clase estática interna que representa el arribo de un cliente
        public ArrivalEvent(double time) { // Constructor que recibe solo el tiempo de arribo
            super(time, null); // Llama al constructor de Event con tiempo y entidad null (se crea en handleArrival)
        }

        @Override // Anotación que indica sobrescritura del método abstracto de Event
        public void execute(Object engineObj) { // Método que ejecuta la lógica del evento de arribo
            DigemicEngine engine = (DigemicEngine) engineObj; // Convierte el objeto genérico a DigemicEngine
            engine.handleArrival(time); // Delega al motor de simulación el manejo del arribo en el tiempo especificado
        }
    }

    /**
     * Evento de fin de proceso en una locación
     */
    public static class ProcessEndEvent extends Event { // Clase estática interna que representa el fin de un proceso
        private String locationName; // Nombre de la locación donde terminó el proceso

        public ProcessEndEvent(double time, Entity entity, String location) { // Constructor con tiempo, entidad y locación
            super(time, entity); // Llama al constructor de Event con tiempo y entidad
            this.locationName = location; // Asigna el nombre de la locación al atributo de la clase
        }

        @Override // Anotación que indica sobrescritura del método abstracto de Event
        public void execute(Object engineObj) { // Método que ejecuta la lógica del fin de proceso
            DigemicEngine engine = (DigemicEngine) engineObj; // Convierte el objeto genérico a DigemicEngine
            engine.handleProcessEnd(entity, locationName, time); // Delega al motor el manejo del fin de proceso con entidad, locación y tiempo
        }
    }

    /**
     * Evento de fin de pausa de servidor (cada 10 pasaportes)
     */
    public static class ServerPauseEndEvent extends Event { // Clase estática interna que representa el fin de pausa de un servidor
        private final String serverName; // Nombre del servidor que termina su pausa (SERVIDOR_1 o SERVIDOR_2)

        public ServerPauseEndEvent(double time, Entity entity, String serverName) { // Constructor con tiempo, entidad y nombre del servidor
            super(time, entity); // Llama al constructor de Event con tiempo y entidad
            this.serverName = serverName; // Asigna el nombre del servidor al atributo final de la clase
        }

        @Override // Anotación que indica sobrescritura del método abstracto de Event
        public void execute(Object engineObj) { // Método que ejecuta la lógica del fin de pausa del servidor
            DigemicEngine engine = (DigemicEngine) engineObj; // Convierte el objeto genérico a DigemicEngine
            engine.handleServerPauseEnd(serverName, entity, time); // Delega al motor el manejo del fin de pausa con servidor, entidad y tiempo
        }
    }
}
