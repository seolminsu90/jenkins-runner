package com.jenkindex.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebConfig implements WebFluxConfigurer {

    @Value("${jenkins.auth-token}")
    String authToken;

    @Value("${jenkins.url}")
    String url;

    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .baseUrl(url)
                .defaultHeader("Authorization", "Basic " + authToken)
                .build();
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        registry.addMapping("/api/**")
                .allowedOrigins("http://localhost", "http://localhost:8080", "http://localhost:3000") // ...
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                //.allowedHeaders("header1", "header2", "header3")
                //.exposedHeaders("header1", "header2")
                .allowCredentials(true).maxAge(3600);
    }
}
