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

@Component
public class ProxyService {
    /*
    MAX_PROXIES -> maximum account of proxies to be read from the proxies.txt file, if MAX_PROXIES = 1 the application only uses one thread for all requests.
    AMOUNT_OF_PROXIES -> The actual amount of used proxies in case there are fewer proxies than specified by MAX_PROXIES. Put MAX_PROXIES to Integer.MAX_VALUE
    if you want all proxies of your file to be used.
    */

    @Value("${user-properties.max-proxies}")
    private int MAX_PROXIES;
    private List<String[]> proxyParams;

    private List<Thread> currentThreads = new ArrayList<>();
    private int last_index;

    // initialize in post construct, since the properties are only inserted post creation
    @PostConstruct
    private void init() {
        this.proxyParams = getProxies(MAX_PROXIES);
    }

    public void addRestTemplateConsumerThreads(int amountOfThreads, int amountOfProxies, Consumer<RestTemplate[]> consumer) {
        if (amountOfProxies < amountOfThreads) {
            throw new IllegalArgumentException("Proxy amount must exceed/equal thread amount");
        }
        // spread the proxies evenly over all threads
        int[] amounts = new int[amountOfThreads];
        for (int i = 0; i < amountOfProxies; i++) {
            amounts[i % amounts.length]++;
        }
        for (int i = 0; i < amountOfThreads; i++) {
            addRestTemplateConsumerThread(consumer, amounts[i]);
        }
    }

    public void addEmptyThread(Runnable r) {
        Thread t = new Thread(r);
        currentThreads.add(t);
        t.start();
    }

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

        Thread thread = new Thread(
            () -> consumer.accept(templates.toArray(new RestTemplate[0]))
        );

        currentThreads.add(thread);

        thread.start();
    }

    public void await() {
        for (Thread t : currentThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        currentThreads = new ArrayList<>();
    }

    public int maxThreads() {
        return MAX_PROXIES;
    }

    /**
     * Gets an array of the proxies [ip, port]. Reads up to MAX_PROXIES proxies from the file.
     *
     * @param MAX_PROXIES Max amount to be read from the file
     * @return List of proxies.
     */
    private List<String[]> getProxies(int MAX_PROXIES) {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(CSGOAccountMapper.class.getClassLoader().getResourceAsStream("proxies.txt")));
        BufferedReader reader = new BufferedReader(r);
        List<String[]> lines = reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).map(line -> line.split(":")).collect(Collectors.toList());
        Collections.shuffle(lines); // shuffle so different proxies are selected each time for rate limiting purposes
        List<String[]> proxies = lines.stream().limit(MAX_PROXIES).toList();
        if (proxies.size() != MAX_PROXIES) {
            throw new IllegalStateException("Proxy amount specified in the properties is higher than the amount of proxies in the file.");
        }
        return proxies;
    }
}
