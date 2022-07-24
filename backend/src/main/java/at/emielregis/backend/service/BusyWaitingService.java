package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

@Component
public class BusyWaitingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private LocalDateTime locked_until = LocalDateTime.MIN;

    /**
     * Busy waits for the specified amount of time.
     *
     * @param seconds The amount of seconds to wait.
     */
    public void wait(int seconds) {
        LOGGER.info("Locking for " + seconds + " seconds.");
        locked_until = LocalDateTime.now().plusMinutes(seconds);
        waitForLock();
    }

    public void waitForLock() {
        while (isLocked()) {
            Thread.onSpinWait();
        }
    }

    public boolean isLocked() {
        return locked_until.isAfter(LocalDateTime.now());
    }
}
