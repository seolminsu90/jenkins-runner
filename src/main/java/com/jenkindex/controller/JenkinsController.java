package com.jenkindex.controller;

import com.jenkindex.dto.JenkinsStatus;
import com.jenkindex.entity.Jenkins;
import com.jenkindex.service.JenkinsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/jenkins")
public class JenkinsController {

    private final JenkinsService jenkinsService;

    @GetMapping
    public Flux<JenkinsStatus> getJenkinsJobList() {
        return jenkinsService.getJenkinsJobList();
    }

    @GetMapping("/{region}")
    public Mono<JenkinsStatus> getJenkinsJobList(@PathVariable String region) {
        return jenkinsService.getJenkinsJob(region);
    }

    @PostMapping("/{region}:execute")
    public Mono<Jenkins> executeJenkinsJob(@PathVariable String region, @RequestParam String branch, @RequestParam(required = false) String receiver) {
        if (receiver == null || receiver.equals("undefined") || receiver.equals("")) {
            receiver = null;
        }
        return jenkinsService.executeJenkinsJob(region, branch, receiver);
    }

    @PostMapping("/{region}:stop")
    public Mono<ResponseEntity<Void>> stopJenkinsJob(@PathVariable String region, @RequestParam String id) {
        return jenkinsService.stopJenkinsJob(region, id);
    }
}
