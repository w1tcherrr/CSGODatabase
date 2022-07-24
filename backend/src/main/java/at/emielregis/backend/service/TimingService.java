package at.emielregis.backend.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.lang.invoke.MethodHandles;

@Component
public class TimingService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    public void time(Runnable runnable, String message) {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        runnable.run();
        stopWatch.stop();
        double seconds = stopWatch.getTotalTimeSeconds();
        System.out.println(message.replace("{}", String.format("%.2f", seconds)));
    }
}
