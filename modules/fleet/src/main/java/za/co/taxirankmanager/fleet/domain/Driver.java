package za.co.taxirankmanager.fleet.domain;

import java.util.Objects;

public class Driver {

    private final DriverId id;
    private final SouthAfricanIdNumber idNumber;
    private String firstName;
    private String lastName;
    private String mobileNumber;
    private boolean active;
    private long version;

    private Driver(DriverId id, SouthAfricanIdNumber idNumber, String firstName,
                   String lastName, String mobileNumber, boolean active, long version) {
        this.id = Objects.requireNonNull(id);
        this.idNumber = Objects.requireNonNull(idNumber);
        this.firstName = Objects.requireNonNull(firstName);
        this.lastName = Objects.requireNonNull(lastName);
        this.mobileNumber = mobileNumber;
        this.active = active;
        this.version = version;
    }

    public static Driver register(DriverId id, SouthAfricanIdNumber idNumber,
                                  String firstName, String lastName, String mobileNumber) {
        return new Driver(id, idNumber, firstName, lastName, mobileNumber, true, 0L);
    }

    public static Driver reconstitute(DriverId id, SouthAfricanIdNumber idNumber, String firstName,
                                      String lastName, String mobileNumber, boolean active, long version) {
        return new Driver(id, idNumber, firstName, lastName, mobileNumber, active, version);
    }

    public void updateContactDetails(String mobileNumber) {
        this.mobileNumber = mobileNumber;
    }

    public void deactivate() {
        this.active = false;
    }

    public DriverId getId() { return id; }
    public SouthAfricanIdNumber getIdNumber() { return idNumber; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getMobileNumber() { return mobileNumber; }
    public boolean isActive() { return active; }
    public long getVersion() { return version; }

    public String getDisplayName() {
        return firstName + " " + lastName;
    }
}
