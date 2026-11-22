package za.co.taxirankmanager.route.application.port.in;

import za.co.taxirankmanager.route.application.command.AssignTaxiToRankRouteCommand;
import za.co.taxirankmanager.route.domain.RankRouteId;

public interface AssignTaxiToRankRouteUseCase {

    RankRouteId assignTaxi(AssignTaxiToRankRouteCommand command);
}
