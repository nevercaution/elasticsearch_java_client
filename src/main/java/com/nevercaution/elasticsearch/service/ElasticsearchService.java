package com.nevercaution.elasticsearch.service;

import com.google.gson.Gson;
import com.nevercaution.elasticsearch.model.User;
import com.nevercaution.elasticsearch.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.main.MainResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.publisher.MonoSink;

import java.io.IOError;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Slf4j
public class ElasticsearchService {

    private RestHighLevelClient restHighLevelClient;

    @Autowired
    public ElasticsearchService(RestHighLevelClient restHighLevelClient) {
        this.restHighLevelClient = restHighLevelClient;
    }

    public Mono<Void> index(String index, String type, String userName, String message) {
        Gson gson = GsonUtil.gson();

        User user = new User();
        user.setUser(userName);
        user.setMessage(message);
        user.setPostDate(new Date());

        IndexRequest indexRequest = new IndexRequest(index, type);
        indexRequest.source(gson.toJson(user), XContentType.JSON);


        return Mono.create(sink -> {
            restHighLevelClient.indexAsync(indexRequest, new ActionListener<IndexResponse>() {
                @Override
                public void onResponse(IndexResponse indexResponse) {
                    log.info("index success : "+indexResponse.toString());
                    sink.success();
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("index error ", e);
                    sink.error(e);
                }
            });
        });
    }

    public Mono<List<User>> matchAll(String index) {
        final Gson gson = GsonUtil.gson();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        return Mono.create((MonoSink<List<User>> sink) -> {
            restHighLevelClient.searchAsync(searchRequest, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    List<User> resultList = new ArrayList<>();
                    searchResponse.getHits().forEach(item -> {
                        User user = gson.fromJson(item.getSourceAsString(), User.class);
                        resultList.add(user);
                    });
                    sink.success(resultList);
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("matchAll error ", e);
                    sink.error(e);
                }
            });
        });
    }
}
