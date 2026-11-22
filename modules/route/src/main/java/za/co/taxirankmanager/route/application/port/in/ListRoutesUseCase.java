package za.co.taxirankmanager.route.application.port.in;

import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.domain.RankRoute;
import za.co.taxirankmanager.route.domain.Route;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.Optional;

public interface ListRoutesUseCase {

    List<Route> listActiveRoutes();

    Optional<Route> findRouteById(RouteId id);

    List<RankRoute> findRankRoutesByRank(RankId rankId);
}
