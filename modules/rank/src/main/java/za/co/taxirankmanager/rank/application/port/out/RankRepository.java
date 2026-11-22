package za.co.taxirankmanager.rank.application.port.out;

import za.co.taxirankmanager.rank.domain.Rank;
import za.co.taxirankmanager.rank.domain.RankId;

import java.util.List;
import java.util.Optional;

public interface RankRepository {

    void save(Rank rank);

    Optional<Rank> findById(RankId id);

    List<Rank> findAllActive();
}
