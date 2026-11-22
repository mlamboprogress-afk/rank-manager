package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.route.domain.Route;

public record RouteResponse(String id, String name, String origin, String destination, boolean active) {

    public static RouteResponse from(Route route) {
        return new RouteResponse(
                route.getId().value().toString(),
                route.getName(),
                route.getOrigin(),
                route.getDestination(),
                route.isActive());
    }
}
