package com.simulation.core;

/**
 * Tipos de eventos para DIGEMIC (Sistema de expedición de pasaportes)
 */
public class EventTypes {

    // ========= EVENTOS DIGEMIC =========
    
    /**
     * Evento de arribo de cliente al sistema DIGEMIC
     */
    public static class ArrivalEvent extends Event {
        public ArrivalEvent(double time) {
            super(time, null);
        }

        @Override
        public void execute(Object engineObj) {
            DigemicEngine engine = (DigemicEngine) engineObj;
            engine.handleArrival(time);
        }
    }

    /**
     * Evento de fin de proceso en una locación
     */
    public static class ProcessEndEvent extends Event {
        private String locationName;

        public ProcessEndEvent(double time, Entity entity, String location) {
            super(time, entity);
            this.locationName = location;
        }

        @Override
        public void execute(Object engineObj) {
            DigemicEngine engine = (DigemicEngine) engineObj;
            engine.handleProcessEnd(entity, locationName, time);
        }
    }

    /**
     * Evento de fin de pausa de servidor (cada 10 pasaportes)
     */
    public static class ServerPauseEndEvent extends Event {
        private final String serverName;

        public ServerPauseEndEvent(double time, Entity entity, String serverName) {
            super(time, entity);
            this.serverName = serverName;
        }

        @Override
        public void execute(Object engineObj) {
            DigemicEngine engine = (DigemicEngine) engineObj;
            engine.handleServerPauseEnd(serverName, entity, time);
        }
    }

    /* ========= EVENTOS MULTI-ENGRANE (Deshabilitados) =========

    public static class TransportEndEvent extends Event {
        private String destinationName;
        private TransportResource resource;
        private double resourceReturnTime;

        public TransportEndEvent(double time, Entity entity, String destination) {
            this(time, entity, destination, null, 0.0);
        }

        public TransportEndEvent(double time, Entity entity, String destination, TransportResource resource, double resourceReturnTime) {
            super(time, entity);
            this.destinationName = destination;
            this.resource = resource;
            this.resourceReturnTime = resourceReturnTime;
        }

        @Override
        public void execute(Object engineObj) {
            if (engineObj instanceof SimulationEngine) {
                SimulationEngine engine = (SimulationEngine) engineObj;
                engine.handleTransportEnd(entity, destinationName, time);
                if (resource != null) {
                    engine.handleTransportResourceAfterArrival(resource, time, resourceReturnTime);
                }
            }
        }
    }

    public static class ResourceReleaseEvent extends Event {
        private final TransportResource resource;

        public ResourceReleaseEvent(double time, TransportResource resource) {
            super(time, null);
            this.resource = resource;
        }

        @Override
        public void execute(Object engineObj) {
            if (engineObj instanceof SimulationEngine) {
                SimulationEngine engine = (SimulationEngine) engineObj;
                engine.handleTransportResourceAvailable(resource, time);
            }
        }
    }
    */
}

