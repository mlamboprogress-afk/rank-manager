package za.co.taxirankmanager.fleet.application.port.out;

import za.co.taxirankmanager.fleet.domain.Taxi;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.fleet.domain.VehicleRegistration;

import java.util.List;
import java.util.Optional;

public interface TaxiRepository {

    void save(Taxi taxi);

    Optional<Taxi> findById(TaxiId id);

    Optional<Taxi> findByNormalisedRegistration(String normalisedRegistration);

    boolean existsByNormalisedRegistration(String normalisedRegistration);

    List<Taxi> findAllActive();
}
