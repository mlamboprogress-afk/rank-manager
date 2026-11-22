package za.co.taxirankmanager.adapters.in.rest.dto;

import za.co.taxirankmanager.fleet.domain.Driver;

public record DriverResponse(String id, String displayName, String maskedIdNumber, String mobileNumber, boolean active) {

    public static DriverResponse from(Driver driver) {
        return new DriverResponse(
                driver.getId().value().toString(),
                driver.getDisplayName(),
                driver.getIdNumber().masked(),
                driver.getMobileNumber(),
                driver.isActive());
    }
}
