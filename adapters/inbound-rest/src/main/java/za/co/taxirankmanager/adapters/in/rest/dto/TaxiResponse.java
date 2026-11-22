package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.fleet.domain.Taxi;

public record TaxiResponse(String id, String registration, boolean active) {

    public static TaxiResponse from(Taxi taxi) {
        return new TaxiResponse(
                taxi.getId().value().toString(),
                taxi.getRegistration().getDisplayValue(),
                taxi.isActive());
    }
}
