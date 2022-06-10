package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

@Component
public class BusyWaitingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final UrlProvider urlProvider;

    private LocalDateTime locked_until = LocalDateTime.MIN;

    public BusyWaitingService(UrlProvider urlProvider) {
        this.urlProvider = urlProvider;
    }

    public void waitAndCircleKey(int i) {
        LOGGER.info("Locking for 2 minutes.");
        locked_until = LocalDateTime.now().plusMinutes(2);
        urlProvider.circleKey();
        waitForLock();
    }

    public void waitForLock() {
        while (isLocked()) {
            try {
                LOGGER.info("Still locked due to 429.");
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean isLocked() {
        return locked_until.isAfter(LocalDateTime.now());
    }
}
