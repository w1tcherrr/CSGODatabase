package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;

/**
 * Service for handling busy-waiting logic.
 * Ensures that threads lock for a specified duration and wait until the lock is lifted.
 */
@Component
public class BusyWaitingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final ThreadLocal<LocalDateTime> locked_until = ThreadLocal.withInitial(() -> LocalDateTime.MIN);

    /**
     * Locks the current thread for the specified amount of seconds.
     *
     * @param seconds The duration of the lock in seconds.
     */
    public void wait(int seconds) {
        LOGGER.info("Thread {} locking for {} seconds.", Thread.currentThread().getName(), seconds);
        locked_until.set(LocalDateTime.now().plusSeconds(seconds));
        waitForLock();
    }

    /**
     * Waits until the lock is lifted by continuously checking the lock status.
     */
    private void waitForLock() {
        while (isLocked()) {
            try {
                long millisToWait = locked_until.get().minusNanos(System.nanoTime()).getNano() / 1000000L;
                if (millisToWait > 0) {
                    Thread.sleep(millisToWait); // Sleep for the remaining duration
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status
                break;
            }
        }
    }

    /**
     * Checks if the current thread is still locked.
     *
     * @return True if the lock is active, false otherwise.
     */
    public boolean isLocked() {
        return locked_until.get().isAfter(LocalDateTime.now());
    }
}
