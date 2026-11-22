package za.co.taxirankmanager.adapters.in.rest.dto;

public record RegisterDriverRequest(
        String idNumber,
        String firstName,
        String lastName,
        String mobileNumber
) {}
