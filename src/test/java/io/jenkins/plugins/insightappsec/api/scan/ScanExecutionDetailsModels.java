package io.jenkins.plugins.insightappsec.api.scan;

public class ScanExecutionDetailsModels {

    public static ScanExecutionDetails.ScanExecutionDetailsBuilder aScanExecutionDetails() {
        return ScanExecutionDetails.builder();
    }

    public static ScanExecutionDetails.ScanExecutionDetailsBuilder aCompleteScanExecutionDetails() {
        return aScanExecutionDetails().attacked(10)
                                      .dripDelay(10)
                                      .failedRequests(10)
                                      .linksCrawled(10)
                                      .networkSpeed(10)
                                      .requests(10);
    }

}