package com.jenkindex.repository;

import com.jenkindex.entity.Jenkins;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Mono;

public interface JenkinsRepository extends R2dbcRepository<Jenkins, String> {
}
