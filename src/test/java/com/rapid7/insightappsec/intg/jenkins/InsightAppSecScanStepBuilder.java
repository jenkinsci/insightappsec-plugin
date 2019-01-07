package com.rapid7.insightappsec.intg.jenkins;

public class InsightAppSecScanStepBuilder {
    
    private String scanConfigId;
    private String buildAdvanceSelector;
    private String vulnerabilityQuery;
    private String region;
    private String credentialsId;
    private boolean storeScanResults;

    public static InsightAppSecScanStepBuilder builder() {
        return new InsightAppSecScanStepBuilder();
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

    public InsightAppSecScanStep build() {
        return new InsightAppSecScanStep(scanConfigId, buildAdvanceSelector, vulnerabilityQuery, region, credentialsId, storeScanResults);
    }

}
