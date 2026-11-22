package za.co.taxirankmanager.adapters.out.clock;

import jakarta.enterprise.context.ApplicationScoped;
import za.co.taxirankmanager.shared.application.port.out.ClockPort;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;

@ApplicationScoped
public class SystemClockAdapter implements ClockPort {

    private static final ZoneId SAST = ZoneId.of("Africa/Johannesburg");

    @Override
    public Instant now() {
        return Instant.now();
    }

    @Override
    public LocalDate today() {
        return LocalDate.now(SAST);
    }
}
