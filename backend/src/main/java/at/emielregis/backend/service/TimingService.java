package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.invoke.MethodHandles;

/**
 * Service for timing the execution of operations and logging their durations.
 * Can be used to measure performance of specific blocks of code.
 */
@Component
public class TimingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    /**
     * Times the execution of a {@link Runnable} and logs the duration with a custom message.
     *
     * @param runnable The block of code to time.
     * @param message  The log message template with a placeholder `{}` for the duration in seconds.
     */
    public void time(Runnable runnable, String message) {
        LOGGER.info("TimingService#time() - Timing operation: {}", message);

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            runnable.run();
        } catch (Exception e) {
            LOGGER.error("TimingService#time() - Exception occurred during timed operation: {}", e.getMessage());
            throw e;
        } finally {
            stopWatch.stop();
            double seconds = stopWatch.getTotalTimeSeconds();
            String formattedMessage = message.replace("{}", String.format("%.2f", seconds));
            LOGGER.info("TimingService#time() - Completed operation: {}", formattedMessage);
        }
    }
}
