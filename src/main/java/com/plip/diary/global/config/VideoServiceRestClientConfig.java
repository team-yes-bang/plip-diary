package com.plip.diary.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties(VideoServiceProperties.class)
public class VideoServiceRestClientConfig {

    @Bean
    @LoadBalanced
    RestClient.Builder loadBalancedRestClientBuilder() {
        return RestClient.builder();
    }

    @Bean
    RestClient videoServiceRestClient(
            @LoadBalanced RestClient.Builder restClientBuilder,
            VideoServiceProperties properties
    ) {
        return restClientBuilder
                .baseUrl(properties.getBaseUrl())
                .build();
    }
}
