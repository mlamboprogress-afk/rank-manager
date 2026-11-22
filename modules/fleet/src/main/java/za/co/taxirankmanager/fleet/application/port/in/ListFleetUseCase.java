package za.co.taxirankmanager.fleet.application.port.in;

import za.co.taxirankmanager.fleet.domain.Driver;
import za.co.taxirankmanager.fleet.domain.DriverId;
import za.co.taxirankmanager.fleet.domain.Taxi;
import za.co.taxirankmanager.fleet.domain.TaxiId;

import java.util.List;
import java.util.Optional;

public interface ListFleetUseCase {

    List<Taxi> listActiveTaxis();

    Optional<Taxi> findTaxiById(TaxiId id);

    List<Driver> listActiveDrivers();

    Optional<Driver> findDriverById(DriverId id);
}
