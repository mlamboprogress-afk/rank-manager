package za.co.taxirankmanager.adapters.out.postgres.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

import java.util.UUID;

@Entity
@Table(name = "taxi")
public class TaxiJpaEntity {

    @Id
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "registration_display", nullable = false)
    private String registrationDisplay;

    @Column(name = "registration_normalised", nullable = false, unique = true)
    private String registrationNormalised;

    @Column(name = "active", nullable = false)
    private boolean active;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    public TaxiJpaEntity() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getRegistrationDisplay() { return registrationDisplay; }
    public void setRegistrationDisplay(String registrationDisplay) { this.registrationDisplay = registrationDisplay; }
    public String getRegistrationNormalised() { return registrationNormalised; }
    public void setRegistrationNormalised(String registrationNormalised) { this.registrationNormalised = registrationNormalised; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public long getVersion() { return version; }
    public void setVersion(long version) { this.version = version; }
}
