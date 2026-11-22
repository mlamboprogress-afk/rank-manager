package za.co.taxirankmanager.route.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.application.command.AssignTaxiToRankRouteCommand;
import za.co.taxirankmanager.route.application.command.CreateRouteCommand;
import za.co.taxirankmanager.route.application.port.in.AssignTaxiToRankRouteUseCase;
import za.co.taxirankmanager.route.application.port.in.CreateRouteUseCase;
import za.co.taxirankmanager.route.application.port.in.ListRoutesUseCase;
import za.co.taxirankmanager.route.application.port.out.RankRouteRepository;
import za.co.taxirankmanager.route.application.port.out.RouteRepository;
import za.co.taxirankmanager.route.domain.RankRoute;
import za.co.taxirankmanager.route.domain.RankRouteId;
import za.co.taxirankmanager.route.domain.Route;
import za.co.taxirankmanager.route.domain.RouteId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RouteService implements CreateRouteUseCase, AssignTaxiToRankRouteUseCase, ListRoutesUseCase {

    private final RouteRepository routeRepository;
    private final RankRouteRepository rankRouteRepository;

    @Inject
    public RouteService(RouteRepository routeRepository, RankRouteRepository rankRouteRepository) {
        this.routeRepository = routeRepository;
        this.rankRouteRepository = rankRouteRepository;
    }

    @Override
    public RouteId createRoute(CreateRouteCommand command) {
        RouteId id = RouteId.generate();
        Route route = Route.create(id, command.name(), command.origin(), command.destination());
        routeRepository.save(route);
        return id;
    }

    @Override
    public RankRouteId assignTaxi(AssignTaxiToRankRouteCommand command) {
        RankRouteId id = RankRouteId.generate();
        RankRoute rankRoute = RankRoute.create(id, command.rankId(), command.routeId());
        rankRouteRepository.save(rankRoute);
        return id;
    }

    @Override
    public List<Route> listActiveRoutes() {
        return routeRepository.findAllActive();
    }

    @Override
    public Optional<Route> findRouteById(RouteId id) {
        return routeRepository.findById(id);
    }

    @Override
    public List<RankRoute> findRankRoutesByRank(RankId rankId) {
        return rankRouteRepository.findByRankId(rankId);
    }
}
