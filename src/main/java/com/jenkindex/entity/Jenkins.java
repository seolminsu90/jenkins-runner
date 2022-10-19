package com.jenkindex.entity;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Getter
@Setter
@Table(name = "jenkins")
public class Jenkins {
    @Id
    private String region;

    private Integer order_num;
    private String title;
    private String branch;
    private String job_url;
    private String job_token;
}
