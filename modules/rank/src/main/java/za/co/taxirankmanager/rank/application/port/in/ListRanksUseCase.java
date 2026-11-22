package za.co.taxirankmanager.rank.application.port.in;

import za.co.taxirankmanager.rank.domain.Rank;
import za.co.taxirankmanager.rank.domain.RankId;

import java.util.List;
import java.util.Optional;

public interface ListRanksUseCase {

    List<Rank> listActive();

    Optional<Rank> findById(RankId id);
}
