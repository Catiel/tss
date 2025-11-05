package com.simulation.resources;

public class TransportResource {
    private final String name;
    private boolean busy;

    public TransportResource(String name) {
        this.name = name;
        this.busy = false;
    }

    public String getName() {
        return name;
    }

    public boolean isBusy() {
        return busy;
    }

    public boolean isAvailable() {
        return !busy;
    }

    public void occupy() {
        busy = true;
    }

    public void release() {
        busy = false;
    }

    public void reset() {
        busy = false;
    }
}
