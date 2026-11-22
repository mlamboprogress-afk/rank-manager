package za.co.taxirankmanager.adapters.out.security;

import io.quarkus.security.identity.SecurityIdentity;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import za.co.taxirankmanager.identity.application.port.out.CurrentUserPort;
import za.co.taxirankmanager.identity.domain.Role;
import za.co.taxirankmanager.identity.domain.User;
import za.co.taxirankmanager.identity.domain.UserId;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RequestScoped
public class OidcCurrentUserAdapter implements CurrentUserPort {

    @Inject
    SecurityIdentity identity;

    @Override
    public UserId currentUserId() {
        String sub = identity.getPrincipal().getName();
        return UserId.of(UUID.fromString(sub));
    }

    @Override
    public User currentUser() {
        String sub = identity.getPrincipal().getName();
        Set<Role> roles = identity.getRoles().stream()
                .flatMap(r -> {
                    try {
                        return java.util.stream.Stream.of(Role.valueOf(r));
                    } catch (IllegalArgumentException e) {
                        return java.util.stream.Stream.empty();
                    }
                })
                .collect(Collectors.toSet());
        return User.of(UserId.of(UUID.fromString(sub)), sub, roles);
    }
}
