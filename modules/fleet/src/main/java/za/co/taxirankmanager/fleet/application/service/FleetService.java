package za.co.taxirankmanager.fleet.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.fleet.application.command.RegisterDriverCommand;
import za.co.taxirankmanager.fleet.application.command.RegisterTaxiCommand;
import za.co.taxirankmanager.fleet.application.port.in.ListFleetUseCase;
import za.co.taxirankmanager.fleet.application.port.in.RegisterDriverUseCase;
import za.co.taxirankmanager.fleet.application.port.in.RegisterTaxiUseCase;
import za.co.taxirankmanager.fleet.application.port.out.DriverRepository;
import za.co.taxirankmanager.fleet.application.port.out.TaxiRepository;
import za.co.taxirankmanager.fleet.domain.Driver;
import za.co.taxirankmanager.fleet.domain.DriverId;
import za.co.taxirankmanager.fleet.domain.SouthAfricanIdNumber;
import za.co.taxirankmanager.fleet.domain.Taxi;
import za.co.taxirankmanager.fleet.domain.TaxiId;
import za.co.taxirankmanager.fleet.domain.VehicleRegistration;
import za.co.taxirankmanager.shared.domain.DomainException;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FleetService implements RegisterTaxiUseCase, RegisterDriverUseCase, ListFleetUseCase {

    private final TaxiRepository taxiRepository;
    private final DriverRepository driverRepository;

    @Inject
    public FleetService(TaxiRepository taxiRepository, DriverRepository driverRepository) {
        this.taxiRepository = taxiRepository;
        this.driverRepository = driverRepository;
    }

    @Override
    public TaxiId registerTaxi(RegisterTaxiCommand command) {
        VehicleRegistration reg = VehicleRegistration.of(command.registration());
        if (taxiRepository.existsByNormalisedRegistration(reg.getNormalisedValue())) {
            throw new DuplicateRegistrationException("Taxi already registered: " + reg);
        }
        TaxiId id = TaxiId.generate();
        Taxi taxi = Taxi.register(id, reg);
        taxiRepository.save(taxi);
        return id;
    }

    @Override
    public DriverId registerDriver(RegisterDriverCommand command) {
        SouthAfricanIdNumber idNumber = SouthAfricanIdNumber.of(command.idNumber());
        String hash = idNumber.hash();
        if (driverRepository.findByIdNumberHash(hash).isPresent()) {
            throw new DuplicateRegistrationException("Driver already registered with this ID number");
        }
        DriverId id = DriverId.generate();
        Driver driver = Driver.register(id, idNumber, command.firstName(), command.lastName(), command.mobileNumber());
        driverRepository.save(driver);
        return id;
    }

    @Override
    public List<Taxi> listActiveTaxis() {
        return taxiRepository.findAllActive();
    }

    @Override
    public Optional<Taxi> findTaxiById(TaxiId id) {
        return taxiRepository.findById(id);
    }

    @Override
    public List<Driver> listActiveDrivers() {
        return driverRepository.findAllActive();
    }

    @Override
    public Optional<Driver> findDriverById(DriverId id) {
        return driverRepository.findById(id);
    }

    public static final class DuplicateRegistrationException extends DomainException {
        public DuplicateRegistrationException(String message) {
            super(message);
        }
    }
}
