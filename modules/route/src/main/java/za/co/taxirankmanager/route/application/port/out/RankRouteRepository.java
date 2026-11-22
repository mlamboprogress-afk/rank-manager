package za.co.taxirankmanager.route.application.port.out;

import za.co.taxirankmanager.rank.domain.RankId;
import za.co.taxirankmanager.route.domain.RankRoute;
import za.co.taxirankmanager.route.domain.RankRouteId;

import java.util.List;
import java.util.Optional;

public interface RankRouteRepository {

    void save(RankRoute rankRoute);

    Optional<RankRoute> findById(RankRouteId id);

    List<RankRoute> findByRankId(RankId rankId);

    List<RankRoute> findAllActive();
}
