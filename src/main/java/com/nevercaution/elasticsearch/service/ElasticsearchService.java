package com.nevercaution.elasticsearch.service;

import com.google.gson.Gson;
import com.nevercaution.elasticsearch.model.User;
import com.nevercaution.elasticsearch.util.GsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;

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

    public Flux<User> matchAll(String index) {
        final Gson gson = GsonUtil.gson();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        return Flux.create((FluxSink<User> sink) -> {
            restHighLevelClient.searchAsync(searchRequest, new ActionListener<SearchResponse>() {
                @Override
                public void onResponse(SearchResponse searchResponse) {
                    searchResponse.getHits().forEach(item -> {
                        User user = gson.fromJson(item.getSourceAsString(), User.class);
                        sink.next(user);
                    });
                    sink.complete();
                }

                @Override
                public void onFailure(Exception e) {
                    log.error("matchAll error ", e);
                    sink.error(e);
                }
            });
        });
    }

    public Mono<List<User>> matchAllSync(String index) {
        final Gson gson = GsonUtil.gson();

        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.matchAllQuery());

        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);

        List<User> resultList = new ArrayList<>();
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest);

            searchResponse.getHits().forEach(item -> {
                User user = gson.fromJson(item.getSourceAsString(), User.class);
                resultList.add(user);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }

        return Mono.just(resultList);
    }

    public Mono<User> getUser(String index, String type, String id) {
        final Gson gson = GsonUtil.gson();
        GetRequest getRequest = new GetRequest(index, type, id);
        return Mono.create(sink -> {
            restHighLevelClient.getAsync(getRequest, new ActionListener<GetResponse>() {
                @Override
                public void onResponse(GetResponse documentFields) {
                    User user = gson.fromJson(documentFields.getSourceAsString(), User.class);
                    sink.success(user);
                }

                @Override
                public void onFailure(Exception e) {
                    e.printStackTrace();
                    sink.error(e);
                }
            });
        });
    }
}
