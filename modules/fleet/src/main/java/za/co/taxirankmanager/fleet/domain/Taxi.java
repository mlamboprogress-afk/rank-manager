package za.co.taxirankmanager.fleet.domain;

import java.util.Objects;

public class Taxi {

    private final TaxiId id;
    private final VehicleRegistration registration;
    private boolean active;
    private long version;

    private Taxi(TaxiId id, VehicleRegistration registration, boolean active, long version) {
        this.id = Objects.requireNonNull(id);
        this.registration = Objects.requireNonNull(registration);
        this.active = active;
        this.version = version;
    }

    public static Taxi register(TaxiId id, VehicleRegistration registration) {
        return new Taxi(id, registration, true, 0L);
    }

    public static Taxi reconstitute(TaxiId id, VehicleRegistration registration, boolean active, long version) {
        return new Taxi(id, registration, active, version);
    }

    public void deactivate() {
        this.active = false;
    }

    public TaxiId getId() { return id; }
    public VehicleRegistration getRegistration() { return registration; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }
}
