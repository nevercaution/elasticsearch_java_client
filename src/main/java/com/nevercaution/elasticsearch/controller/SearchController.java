package com.nevercaution.elasticsearch.controller;

import com.nevercaution.elasticsearch.model.User;
import com.nevercaution.elasticsearch.service.ElasticsearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
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
    public Flux<User> matchAll(@PathVariable("index") String index) {

        return elasticsearchService.matchAll(index).onErrorResume((Throwable error) -> {
            log.error("err", error);
            User user = new User();
            user.setPostDate(new Date());
            user.setUser("default User");
            user.setMessage("default message");
            return Flux.just(user);
        });
    }

    @GetMapping("/sync/match_all/{index}")
    public Mono<List<User>> matchAllSync(@PathVariable("index") String index) {
        return elasticsearchService.matchAllSync(index);
    }

    @GetMapping("get/{index}/{type}/{id}")
    public Mono<User> getAsync(@PathVariable("index") String index,
                               @PathVariable("type") String type,
                               @PathVariable("id") String id) {

        return elasticsearchService.getUser(index, type, id)
                .onErrorResume(error -> {
                    User defaultUser = new User();
                    defaultUser.setUser("default");
                    defaultUser.setPostDate(new Date());
                    defaultUser.setMessage("default message");
                    return Mono.just(defaultUser);
                })
                .defaultIfEmpty(new User());
    }

    @GetMapping("get2/{index}/{type}/{id}")
    public Mono<ResponseEntity<User>> getAsync2(@PathVariable("index") String index,
                                               @PathVariable("type") String type,
                                               @PathVariable("id") String id) {

        return elasticsearchService.getUser(index, type, id)
                .map(ResponseEntity::ok)
                .onErrorResume(error -> Mono.just(ResponseEntity.badRequest().build()))
                .defaultIfEmpty(ResponseEntity.status(HttpStatus.OK).body(new User()));
    }



    @RequestMapping(value = "/index/{index}/{type}", method = {RequestMethod.POST})
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