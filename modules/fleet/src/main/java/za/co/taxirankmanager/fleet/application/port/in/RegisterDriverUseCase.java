package za.co.taxirankmanager.fleet.application.port.in;

import za.co.taxirankmanager.fleet.application.command.RegisterDriverCommand;
import za.co.taxirankmanager.fleet.domain.DriverId;

public interface RegisterDriverUseCase {

    DriverId registerDriver(RegisterDriverCommand command);
}
