package com.rapid7.insightappsec.intg.jenkins;

public class InsightAppSecPluginBuilder {
    
    private String scanConfigId;
    private String buildAdvanceSelector;
    private String vulnerabilityQuery;
    private String region;
    private String credentialsId;

    public static InsightAppSecPluginBuilder builder() {
        return new InsightAppSecPluginBuilder();
    }

    public InsightAppSecPluginBuilder withScanConfigId(String scanConfigId) {
        this.scanConfigId = scanConfigId;
        return this;
    }

    public InsightAppSecPluginBuilder withBuildAdvanceSelector(String buildAdvanceSelector) {
        this.buildAdvanceSelector = buildAdvanceSelector;
        return this;
    }

    public InsightAppSecPluginBuilder withVulnerabiltyQuery(String vulnerabilityQuery) {
        this.vulnerabilityQuery = vulnerabilityQuery;
        return this;
    }

    public InsightAppSecPluginBuilder withRegion(String region) {
        this.region = region;
        return this;
    }

    public InsightAppSecPluginBuilder withCredentialsId(String credentialsId) {
        this.credentialsId = credentialsId;
        return this;
    }

    public InsightAppSecPlugin build() {
        return new InsightAppSecPlugin(scanConfigId, buildAdvanceSelector, vulnerabilityQuery, region, credentialsId);
    }

}
