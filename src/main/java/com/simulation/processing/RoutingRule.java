package com.simulation.processing;

public record RoutingRule(String destinationLocation, double probability, int quantity, String moveLogic,
                          String resourceName) {
}
