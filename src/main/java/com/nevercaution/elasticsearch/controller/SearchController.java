package com.nevercaution.elasticsearch.controller;

import com.nevercaution.elasticsearch.model.User;
import com.nevercaution.elasticsearch.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RestController
@Slf4j
public class SearchController {

    @Autowired
    private ElasticsearchService elasticsearchService;

    @GetMapping("/match_all/{index}")
    public Mono<List<User>> matchAll(@PathVariable("index") String index) {

        return elasticsearchService.matchAll(index).onErrorResume((Throwable error) -> {
            log.error("err", error);
            List<User> userList = new ArrayList<>();
            User user = new User();
            user.setPostDate(new Date());
            user.setUser("default User");
            user.setMessage("default message");
            userList.add(user);
            return Mono.just(userList);
        });
    }

    @GetMapping("/index/{index}/{type}")
    public Mono<Void> index(@PathVariable("index") String index,
                            @PathVariable("type") String type,
                            @RequestParam(value = "user_name") String userName,
                            @RequestParam(value = "message") String message) {
        return elasticsearchService.index(index, type, userName, message)
                .onErrorResume(error -> {
                    log.error("index error ", error);
            return Mono.empty();
        });
    }
}
