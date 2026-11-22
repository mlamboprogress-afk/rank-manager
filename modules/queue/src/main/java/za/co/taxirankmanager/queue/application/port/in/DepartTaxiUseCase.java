package za.co.taxirankmanager.queue.application.port.in;

import za.co.taxirankmanager.queue.application.command.DepartTaxiCommand;

public interface DepartTaxiUseCase {

    void departTaxi(DepartTaxiCommand command);
}
