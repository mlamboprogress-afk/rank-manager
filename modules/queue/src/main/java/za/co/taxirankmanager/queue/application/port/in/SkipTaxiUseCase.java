package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.application.command.SkipTaxiCommand;

public interface SkipTaxiUseCase {

    void skipTaxi(SkipTaxiCommand command);
}
