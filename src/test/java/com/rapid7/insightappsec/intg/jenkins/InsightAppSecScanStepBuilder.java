package com.rapid7.insightappsec.intg.jenkins;

public class InsightAppSecScanStepBuilder {

    private String appId;
    private String scanConfigId;
    private String buildAdvanceSelector;
    private String vulnerabilityQuery;
    private String region;
    private String credentialsId;
    private boolean storeScanResults;
    private String maxScanPendingDuration;
    private String maxScanExecutionDuration;

    public static InsightAppSecScanStepBuilder builder() {
        return new InsightAppSecScanStepBuilder();
    }

    public InsightAppSecScanStepBuilder withAppId(String appId) {
        this.appId = appId;
        return this;
    }

    public InsightAppSecScanStepBuilder withScanConfigId(String scanConfigId) {
        this.scanConfigId = scanConfigId;
        return this;
    }

    public InsightAppSecScanStepBuilder withBuildAdvanceSelector(String buildAdvanceSelector) {
        this.buildAdvanceSelector = buildAdvanceSelector;
        return this;
    }

    public InsightAppSecScanStepBuilder withVulnerabiltyQuery(String vulnerabilityQuery) {
        this.vulnerabilityQuery = vulnerabilityQuery;
        return this;
    }

    public InsightAppSecScanStepBuilder withRegion(String region) {
        this.region = region;
        return this;
    }

    public InsightAppSecScanStepBuilder withCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
        return this;
    }

    public InsightAppSecScanStepBuilder withStoreScanResults(boolean storeScanResults) {
        this.storeScanResults = storeScanResults;
        return this;
    }

    public InsightAppSecScanStepBuilder setMaxScanPendingDuration(String maxScanPendingDuration) {
        this.maxScanPendingDuration = maxScanPendingDuration;
        return this;
    }

    public InsightAppSecScanStepBuilder setMaxScanExecutionDuration(String maxScanExecutionDuration) {
        this.maxScanExecutionDuration = maxScanExecutionDuration;
        return this;
    }

    public InsightAppSecScanStep build() {
        return new InsightAppSecScanStep(region,
                                         credentialsId,
                                         appId,
                                         scanConfigId,
                                         buildAdvanceSelector,
                                         vulnerabilityQuery,
                                         storeScanResults,
                                         maxScanPendingDuration,
                                         maxScanExecutionDuration);
    }

}
