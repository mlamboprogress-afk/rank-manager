package za.co.taxirankmanager.identity.domain;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

public class User {

    private final UserId id;
    private final String externalSubject;
    private final Set<Role> roles;

    private User(UserId id, String externalSubject, Set<Role> roles) {
        this.id = Objects.requireNonNull(id);
        this.externalSubject = Objects.requireNonNull(externalSubject);
        this.roles = roles.isEmpty()
                ? Collections.emptySet()
                : Collections.unmodifiableSet(EnumSet.copyOf(roles));
    }

    public static User of(UserId id, String externalSubject, Set<Role> roles) {
        return new User(id, externalSubject, roles);
    }

    public boolean hasRole(Role role) {
        return roles.contains(role);
    }

    public UserId getId() { return id; }
    public String getExternalSubject() { return externalSubject; }
    public Set<Role> getRoles() { return roles; }
}
