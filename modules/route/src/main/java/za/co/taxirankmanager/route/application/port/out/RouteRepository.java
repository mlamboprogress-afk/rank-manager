package za.co.taxirankmanager.route.application.port.out;

import za.co.taxirankmanager.route.domain.Route;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.Optional;

public interface RouteRepository {

    void save(Route route);

    Optional<Route> findById(RouteId id);

    List<Route> findAllActive();
}
