package com.rapid7.insightappsec.intg.jenkins.api.scan;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanExecutionDetails {

    private int linksCrawled;
    private int attacked;
    private int requests;
    private int failedRequests;
    private int networkSpeed;
    private int dripDelay;

}
