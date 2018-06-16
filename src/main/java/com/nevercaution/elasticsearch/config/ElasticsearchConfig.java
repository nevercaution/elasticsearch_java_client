package com.nevercaution.elasticsearch.config;

import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ElasticsearchConfig {

    @Bean
    public RestHighLevelClient client(ElasticsearchProperties properties) {
        return new RestHighLevelClient(
                RestClient.builder(properties.hosts())
        );
    }
}
