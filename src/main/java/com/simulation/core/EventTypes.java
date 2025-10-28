package com.simulation.core;

public class EventTypes {

    // Evento de arribo de pieza
    public static class ArrivalEvent extends Event {
        public ArrivalEvent(double time) {
            super(time, null);
        }

        @Override
        public void execute(SimulationEngine engine) {
            engine.handleArrival(time);
        }
    }

    // Evento de fin de transporte
    public static class TransportEndEvent extends Event {
        private String destinationName;

        public TransportEndEvent(double time, Entity entity, String destination) {
            super(time, entity);
            this.destinationName = destination;
        }

        @Override
        public void execute(SimulationEngine engine) {
            engine.handleTransportEnd(entity, destinationName, time);
        }
    }

    // Evento de fin de proceso
    public static class ProcessEndEvent extends Event {
        private String locationName;

        public ProcessEndEvent(double time, Entity entity, String location) {
            super(time, entity);
            this.locationName = location;
        }

        @Override
        public void execute(SimulationEngine engine) {
            engine.handleProcessEnd(entity, locationName, time);
        }
    }

    // Evento de fin de operación de inspección
    public static class InspectionOperationEndEvent extends Event {
        public InspectionOperationEndEvent(double time, Entity entity) {
            super(time, entity);
        }

        @Override
        public void execute(SimulationEngine engine) {
            engine.handleInspectionOperationEnd(entity, time);
        }
    }
}
