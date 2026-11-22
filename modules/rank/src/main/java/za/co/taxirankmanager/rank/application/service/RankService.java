package za.co.taxirankmanager.rank.application.service;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.rank.application.command.CreateRankCommand;
import za.co.taxirankmanager.rank.application.port.in.CreateRankUseCase;
import za.co.taxirankmanager.rank.application.port.in.ListRanksUseCase;
import za.co.taxirankmanager.rank.application.port.out.RankRepository;
import za.co.taxirankmanager.rank.domain.Rank;
import za.co.taxirankmanager.rank.domain.RankId;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class RankService implements CreateRankUseCase, ListRanksUseCase {

    private final RankRepository rankRepository;

    @Inject
    public RankService(RankRepository rankRepository) {
        this.rankRepository = rankRepository;
    }

    @Override
    public RankId createRank(CreateRankCommand command) {
        RankId id = RankId.generate();
        Rank rank = Rank.create(id, command.name(), command.locationDescription());
        rankRepository.save(rank);
        return id;
    }

    @Override
    public List<Rank> listActive() {
        return rankRepository.findAllActive();
    }

    @Override
    public Optional<Rank> findById(RankId id) {
        return rankRepository.findById(id);
    }
}
