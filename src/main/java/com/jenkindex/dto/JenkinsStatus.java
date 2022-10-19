package com.jenkindex.dto;

import com.jenkindex.entity.Jenkins;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class JenkinsStatus {
    private Long queueId;
    private String result; // null, ABORTED, SUCCESS, FAILED? FAIL? 둘중 하나일듯.
    private Long timestamp;
    private Long estimatedDuration;
    private List<Map<String, Object>> parameters;
    private String fullDisplayName;
    private String id;

    // ... 기타 등등 많은데 필요없어서 패스

    private Jenkins jenkins;

    public static JenkinsStatus fallback(Jenkins jenkins) {
        JenkinsStatus status = new JenkinsStatus();
        status.setResult("Jenkins Serve Error");
        status.setJenkins(jenkins);
        return status;
    }
}
