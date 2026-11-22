package za.co.taxirankmanager.rank.application.port.in;

import za.co.taxirankmanager.rank.application.command.CreateRankCommand;
import za.co.taxirankmanager.rank.domain.RankId;

public interface CreateRankUseCase {

    RankId createRank(CreateRankCommand command);
}
