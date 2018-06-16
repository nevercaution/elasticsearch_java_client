package com.nevercaution.elasticsearch.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.http.HttpHost;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Setter
@ConfigurationProperties(prefix = "elasticsearch")
public class ElasticsearchProperties {

    private List<String> hosts;

    public HttpHost[] hosts() {
        return hosts.stream().map(HttpHost::create).toArray(HttpHost[]::new);
    }
}
