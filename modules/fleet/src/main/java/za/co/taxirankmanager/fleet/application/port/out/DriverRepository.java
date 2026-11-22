package za.co.taxirankmanager.fleet.application.port.out;

import za.co.taxirankmanager.fleet.domain.Driver;
import za.co.taxirankmanager.fleet.domain.DriverId;

import java.util.List;
import java.util.Optional;

public interface DriverRepository {

    void save(Driver driver);

    Optional<Driver> findById(DriverId id);

    Optional<Driver> findByIdNumberHash(String idNumberHash);

    List<Driver> findAllActive();
}
