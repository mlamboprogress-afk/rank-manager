package za.co.taxirankmanager.route.application.port.in;

import za.co.taxirankmanager.route.application.command.CreateRouteCommand;
import za.co.taxirankmanager.route.domain.RouteId;

public interface CreateRouteUseCase {

    RouteId createRoute(CreateRouteCommand command);
}
