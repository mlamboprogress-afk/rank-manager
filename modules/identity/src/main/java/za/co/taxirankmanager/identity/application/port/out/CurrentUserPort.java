package za.co.taxirankmanager.identity.application.port.out;

import za.co.taxirankmanager.identity.domain.User;
import za.co.taxirankmanager.identity.domain.UserId;

public interface CurrentUserPort {

    UserId currentUserId();

    User currentUser();
}
