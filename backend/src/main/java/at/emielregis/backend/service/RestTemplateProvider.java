package at.emielregis.backend.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * Provides a configured {@link RestTemplate} bean for making HTTP requests.
 * Configured with {@link HttpComponentsClientHttpRequestFactory} for advanced HTTP capabilities.
 */
@Configuration
public class RestTemplateProvider {

    /**
     * Configures and provides a {@link RestTemplate} bean.
     *
     * @return The configured {@link RestTemplate}.
     */
    @Bean
    public RestTemplate provide() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
}
