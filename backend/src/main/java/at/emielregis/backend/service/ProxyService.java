package at.emielregis.backend.service;

import at.emielregis.backend.runners.httpmapper.CSGOAccountMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Service class for managing HTTP proxies and handling threads for REST template consumers.
 * This class reads proxy information from a file and distributes proxies evenly among threads
 * to handle requests in a rate-limited and distributed manner.
 */
@Component
public class ProxyService {

    /**
     * Maximum number of proxies to read from the `proxies.txt` file.
     * If set to a very high value, all proxies in the file will be used.
     */
    @Value("${user-properties.max-proxies}")
    private int MAX_PROXIES;

    private List<String[]> proxyParams; // Stores proxy details in [ip, port] format.
    private List<Thread> currentThreads = new ArrayList<>(); // Active threads managed by this service.
    private int last_index = 0; // Index for round-robin proxy assignment.

    /**
     * Initializes the proxy parameters by reading the proxy list from the file.
     * This method is called automatically after dependency injection is complete.
     */
    @PostConstruct
    private void init() {
        proxyParams = getProxies(MAX_PROXIES);
    }

    /**
     * Creates multiple threads for handling REST template consumers.
     * Each thread is assigned a portion of the available proxies.
     *
     * @param amountOfThreads The number of threads to create.
     * @param amountOfProxies The number of proxies to distribute across threads.
     * @param consumer        The consumer function to execute on the assigned REST templates.
     */
    public void addRestTemplateConsumerThreads(int amountOfThreads, int amountOfProxies, Consumer<RestTemplate[]> consumer) {
        if (amountOfProxies < amountOfThreads) {
            throw new IllegalArgumentException("Proxy amount must exceed or equal thread amount");
        }

        int[] amounts = new int[amountOfThreads];
        for (int i = 0; i < amountOfProxies; i++) {
            amounts[i % amounts.length]++;
        }

        for (int i = 0; i < amountOfThreads; i++) {
            addRestTemplateConsumerThread(consumer, amounts[i]);
        }
    }

    /**
     * Adds a simple thread that runs a given {@link Runnable}.
     *
     * @param r The runnable to execute.
     */
    public void addEmptyThread(Runnable r) {
        Thread t = new Thread(r);
        currentThreads.add(t);
        t.start();
    }

    /**
     * Creates a thread with a specific number of REST templates assigned to it.
     *
     * @param consumer The consumer function to execute.
     * @param amount   The number of REST templates (and proxies) to assign to the thread.
     */
    public void addRestTemplateConsumerThread(Consumer<RestTemplate[]> consumer, int amount) {
        List<RestTemplate> templates = new ArrayList<>();

        for (int i = 0; i < amount; i++) {
            last_index = (last_index + 1) % maxThreads();
            String[] currentParams = proxyParams.get(last_index);

            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(currentParams[0], Integer.parseInt(currentParams[1])));
            SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
            requestFactory.setProxy(proxy);
            RestTemplate template = new RestTemplate(requestFactory);

            templates.add(template);
        }

        Thread thread = new Thread(() -> consumer.accept(templates.toArray(new RestTemplate[0])));
        currentThreads.add(thread);
        thread.start();
    }

    /**
     * Waits for all active threads to finish execution.
     * This method blocks the current thread until all threads in the `currentThreads` list are complete.
     */
    public void await() {
        for (Thread t : currentThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore the interrupted status.
                throw new RuntimeException("Thread interrupted while waiting", e);
            }
        }
        currentThreads = new ArrayList<>();
    }

    /**
     * Gets the maximum number of threads allowed based on the proxy limit.
     *
     * @return The maximum number of threads.
     */
    public int maxThreads() {
        return MAX_PROXIES;
    }

    /**
     * Reads proxies from the `proxies.txt` file and limits the number of proxies based on `MAX_PROXIES`.
     * The file should contain proxy entries in `ip:port` format, one per line.
     *
     * @param MAX_PROXIES The maximum number of proxies to read.
     * @return A list of proxies in [ip, port] format.
     */
    private List<String[]> getProxies(int MAX_PROXIES) {
        InputStreamReader reader = new InputStreamReader(Objects.requireNonNull(
            CSGOAccountMapper.class.getClassLoader().getResourceAsStream("proxies.txt")
        ));
        BufferedReader bufferedReader = new BufferedReader(reader);

        List<String[]> lines = bufferedReader.lines()
            .filter(line -> !line.isEmpty() && !line.startsWith("#")) // Ignore empty lines and comments.
            .map(String::trim)
            .map(line -> line.split(":"))
            .collect(Collectors.toList());

        Collections.shuffle(lines); // Shuffle to distribute load evenly among proxies.
        List<String[]> proxies = lines.stream().limit(MAX_PROXIES).toList();

        if (proxies.size() < MAX_PROXIES) {
            throw new IllegalStateException("Insufficient proxies in the file for the specified MAX_PROXIES.");
        }

        return proxies;
    }
}
