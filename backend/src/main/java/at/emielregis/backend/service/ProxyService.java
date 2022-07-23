package at.emielregis.backend.service;

import at.emielregis.backend.runners.httpmapper.CSGOAccountMapper;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

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
    private final int AMOUNT_OF_PROXIES = getProxies().size();
    private final List<String[]> proxyParams = getProxies();
    private List<Thread> currentThreads = new ArrayList<>();
    private int last_index;

    public void addThreads(int amount, Consumer<RestTemplate> consumer) {
        for (int i = 0; i < amount; i++) {
            addThread(consumer);
        }
    }

    public void addThread(Consumer<RestTemplate> consumer) {
        last_index = (last_index + 1) % maxThreads();
        String[] currentParams = proxyParams.get(last_index);

        Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(currentParams[0], Integer.parseInt(currentParams[1])));
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setProxy(proxy);
        RestTemplate template = new RestTemplate(requestFactory);

        Thread thread = new Thread(
            () -> consumer.accept(template)
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
        return AMOUNT_OF_PROXIES;
    }

    /**
     * Gets an array of the proxies [ip, port]. Reads up to MAX_PROXIES proxies from the file.
     *
     * @return List of proxies.
     */
    private static List<String[]> getProxies() {
        InputStreamReader r = new InputStreamReader(Objects.requireNonNull(CSGOAccountMapper.class.getClassLoader().getResourceAsStream("proxies.txt")));
        BufferedReader reader = new BufferedReader(r);
        List<String[]> lines = reader.lines().filter(line -> !line.isEmpty()).filter(line -> !line.startsWith("#")).map(String::trim).map(line -> line.split(":")).collect(Collectors.toList());
        Collections.shuffle(lines); // shuffle so different proxies are selected each time for rate limiting purposes
        return lines.stream().toList();
    }
}
