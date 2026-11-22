package za.co.taxirankmanager.fleet.application.port.in;

import za.co.taxirankmanager.fleet.application.command.RegisterTaxiCommand;
import za.co.taxirankmanager.fleet.domain.TaxiId;

public interface RegisterTaxiUseCase {

    TaxiId registerTaxi(RegisterTaxiCommand command);
}
