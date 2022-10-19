package com.jenkindex.service;

import com.jenkindex.dto.JenkinsStatus;
import com.jenkindex.entity.Jenkins;
import com.jenkindex.repository.JenkinsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class JenkinsService {
    private final JenkinsRepository jenkinsRepository;
    private final WebClient webClient;

    @Value("${jenkins.receiver}")
    private String defaultReceiver;

    // 젠킨스 잡 정보 리스트 조회
    @Transactional(readOnly = true)
    public Flux<JenkinsStatus> getJenkinsJobList() {
        return jenkinsRepository.findAll()
                .flatMap(this::getJenkinsJobStatus);

    }

    // 젠킨스 잡 정보 조회
    @Transactional(readOnly = true)
    public Mono<JenkinsStatus> getJenkinsJob(String region) {
        return jenkinsRepository.findById(region)
                .flatMap(this::getJenkinsJobStatus);
    }

    // 젠킨스 잡 실행
    public Mono<Jenkins> executeJenkinsJob(String region, String branch, String receiver) {
        return this.getJenkinsJob(region)
                .flatMap(status -> {
                    status.getJenkins().setBranch(branch); // Branch 업데이트 처리
                    log.info("Job result Status is {}", status.getResult());
                    if (status.getResult() == null) {
                        return Mono.empty();
                    } else {
                        return this.executeJenkinsJobRequest(status.getJenkins(), receiver);
                    }
                })
                .switchIfEmpty(Mono.defer(() -> {
                    log.info("Job is Running...");
                    return Mono.empty();
                }));
    }

    // 젠킨스 잡 실행 및 브랜치 정보 저장
    private Mono<Jenkins> executeJenkinsJobRequest(Jenkins jenkins, String receiver) {
        return webClient.post()
                .uri(jenkins.getJob_url() + "/buildWithParameters")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters
                        .fromFormData("token", jenkins.getJob_token())
                        .with("TARGET_BRANCH", jenkins.getBranch())
                        .with("receiver", (receiver == null) ? defaultReceiver : receiver))
                .exchangeToMono(response -> {
                    if (!response.statusCode().is2xxSuccessful()) {
                        return response.createException().flatMap(Mono::error);
                    } else {
                        // Body Return 후 작업하려 했는데, Empty Body 라서 Mono 동작안하여 그냥 여기서 SAVE 처리
                        return jenkinsRepository.save(jenkins);
                    }
                });
    }

    // 젠킨스 잡 상태 조회
    private Mono<JenkinsStatus> getJenkinsJobStatus(Jenkins jenkins) {
        return webClient.post()
                .uri(jenkins.getJob_url() + "/lastBuild/api/json")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("token", jenkins.getJob_token()))
                .retrieve()
                .onStatus(HttpStatus::is4xxClientError, clientResponse -> Mono.error(RuntimeException::new))
                .onStatus(HttpStatus::is5xxServerError, clientResponse -> Mono.error(RuntimeException::new))
                .bodyToMono(JenkinsStatus.class)
                .doOnSuccess(status -> status.setJenkins(jenkins))
                .onErrorReturn(JenkinsStatus.fallback(jenkins));
    }

    // 젠킨스잡 취소
    public Mono<ResponseEntity<Void>> stopJenkinsJob(String region, String id) {
        return this.getJenkinsJob(region)
                .flatMap(status -> (status.getResult() == null) ? this.stopJenkinsJobRequest(status.getJenkins(), id) : Mono.empty())
                .switchIfEmpty(Mono.defer(() -> Mono.just(ResponseEntity.notFound().build())));
    }

    private Mono<ResponseEntity<Void>> stopJenkinsJobRequest(Jenkins jenkins, String id) {
        return webClient.post()
                .uri(jenkins.getJob_url() + "/" + id + "/stop")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData("token", jenkins.getJob_token()))
                .exchangeToMono(response -> {
                    if (!response.statusCode().is3xxRedirection()) { // 응답이 302 리디렉션으로 온다.
                        return response.createException().flatMap(Mono::error);
                    } else {
                        return Mono.just(ResponseEntity.ok().build());
                    }
                });
    }
}
