package at.emielregis.backend.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class RestTemplateProvider {

    @Bean
    public RestTemplate provide() {
        return new RestTemplate(new HttpComponentsClientHttpRequestFactory());
    }
}
