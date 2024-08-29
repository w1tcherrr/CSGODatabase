package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

@Component
public class BusyWaitingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ThreadLocal<LocalDateTime> locked_until = ThreadLocal.withInitial(() -> LocalDateTime.MIN);

    /**
     * Waits for the specified amount of time.
     *
     * @param seconds The amount of seconds to wait.
     */
    public void wait(int seconds) {
        LOGGER.info("Thread " + Thread.currentThread().getName() + " locking for " + seconds + " seconds.");
        locked_until.set(LocalDateTime.now().plusSeconds(seconds));
        waitForLock();
    }

    private void waitForLock() {
        while (isLocked()) {
            try {
                long millisToWait = locked_until.get().minusNanos(System.nanoTime()).getNano() / 1000000L;
                if (millisToWait > 0) {
                    Thread.sleep(millisToWait);  // Sleep only for the remaining time
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();  // Restore the interrupted status
                break;
            }
        }
    }

    public boolean isLocked() {
        return locked_until.get().isAfter(LocalDateTime.now());
    }
}
