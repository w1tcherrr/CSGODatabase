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

    public void wait(int i) {
        LOGGER.info("Locking for " + i + " minutes.");
        locked_until = LocalDateTime.now().plusMinutes(i);
        waitForLock();
    }

    public void waitForLock() {
        while (isLocked()) {
            try {
                LOGGER.info("Still locked due to 429.");
                Thread.sleep(15000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLocked() {
        return locked_until.isAfter(LocalDateTime.now());
    }
}
