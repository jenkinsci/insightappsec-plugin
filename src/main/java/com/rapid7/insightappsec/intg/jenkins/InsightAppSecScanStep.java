package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightCredentialsHelper;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.AncestorInPath;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.Optional;

public class InsightAppSecScanStep extends Builder implements SimpleBuildStep {

    private final String region;
    private final String insightCredentialsId;
    private final String appId;
    private final String scanConfigId;
    private final String buildAdvanceIndicator;
    private final String vulnerabilityQuery;
    private final boolean enableScanResults;
    private final String maxScanPendingDuration;
    private final String maxScanExecutionDuration;

    @DataBoundConstructor
    public InsightAppSecScanStep(String region,
                                 String insightCredentialsId,
                                 String appId,
                                 String scanConfigId,
                                 String buildAdvanceIndicator,
                                 String vulnerabilityQuery,
                                 boolean enableScanResults,
                                 String maxScanPendingDuration,
                                 String maxScanExecutionDuration) {
        this.region = Region.fromString(region).name();
        this.insightCredentialsId = Util.fixEmptyAndTrim(insightCredentialsId);
        this.appId = Util.fixEmptyAndTrim(appId);
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.buildAdvanceIndicator = BuildAdvanceIndicator.fromString(buildAdvanceIndicator).name();
        this.vulnerabilityQuery = Util.fixEmptyAndTrim(vulnerabilityQuery);
        this.enableScanResults = enableScanResults;
        this.maxScanPendingDuration = Util.fixEmptyAndTrim(maxScanPendingDuration);
        this.maxScanExecutionDuration = Util.fixEmptyAndTrim(maxScanExecutionDuration);
    }

    public String getRegion() {
        return region;
    }

    public String getInsightCredentialsId() {
        return insightCredentialsId;
    }

    public String getAppId() {
        return appId;
    }

    public String getScanConfigId() {
        return scanConfigId;
    }

    public String getBuildAdvanceIndicator() {
        return buildAdvanceIndicator;
    }

    public String getVulnerabilityQuery() {
        return vulnerabilityQuery;
    }

    public boolean isEnableScanResults() {
        return enableScanResults;
    }

    public String getMaxScanPendingDuration() {
        return maxScanPendingDuration;
    }

    public String getMaxScanExecutionDuration() {
        return maxScanExecutionDuration;
    }


    @Override
    public void perform(Run<?, ?> run,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) throws InterruptedException {
        InsightAppSecLogger logger = new InsightAppSecLogger(listener.getLogger());

        BuildAdvanceIndicator bai = BuildAdvanceIndicator.fromString(buildAdvanceIndicator);

        Optional<ScanResults> scanResults = newRunner(logger).run(scanConfigId,
                                                                  bai,
                                                                  vulnerabilityQuery);

        scanResults.ifPresent(sc -> ScanResultHandler.INSTANCE.handleScanResults(run, logger, bai, sc, enableScanResults));

    }

    // HELPERS

    private InsightAppSecScanStepRunner newRunner(InsightAppSecLogger logger) {
        String apiKey = InsightCredentialsHelper.INSTANCE.lookupInsightCredentialsById(insightCredentialsId).getApiKey().getPlainText();

        Region reg = Region.fromString(region);

        ScanApi scanApi = new ScanApi(reg.getAPIHost(), apiKey);
        SearchApi searchApi = new SearchApi(reg.getAPIHost(), apiKey);

        return new InsightAppSecScanStepRunner(scanApi,
                                               searchApi,
                                               ThreadHelper.INSTANCE,
                                               logger,
                                               newScanDurationHandler(scanApi, logger));
    }

    private ScanDurationHandler newScanDurationHandler(ScanApi scanApi,
                                                       InsightAppSecLogger logger) {
        long maxScanPendingDuration = DurationStringParser.INSTANCE.parseDurationString(this.maxScanPendingDuration);
        long maxScanExecutionDuration = DurationStringParser.INSTANCE.parseDurationString(this.maxScanExecutionDuration);

        return new ScanDurationHandler(BuildAdvanceIndicator.fromString(buildAdvanceIndicator),
                                       scanApi,
                                       logger,
                                       System.currentTimeMillis(),
                                       maxScanPendingDuration,
                                       maxScanExecutionDuration);
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        private DescriptorHelper descriptorHelper = new DescriptorHelper();

        public ListBoxModel doFillRegionItems() {
            return descriptorHelper.getRegionItems();
        }

        public ListBoxModel doFillInsightCredentialsIdItems(@AncestorInPath Jenkins context) {
            return descriptorHelper.getInsightCredentialsIdItems(context);
        }

        public ListBoxModel doFillAppIdItems(@QueryParameter String region,
                                             @QueryParameter String insightCredentialsId) {
            return descriptorHelper.getAppIdItems(region, insightCredentialsId);
        }

        public ListBoxModel doFillScanConfigIdItems(@QueryParameter String region,
                                                    @QueryParameter String insightCredentialsId,
                                                    @QueryParameter String appId) {
            return descriptorHelper.getScanConfigIdItems(region, insightCredentialsId, appId);
        }

        public ListBoxModel doFillBuildAdvanceIndicatorItems() {
            return descriptorHelper.getBuildAdvanceIndicatorItems();
        }

        public FormValidation doCheckVulnerabilityQuery() {
            // no actual validation, just return markup message
            return descriptorHelper.doCheckVulnerabilityQuery();
        }

        public FormValidation doCheckMaxScanPendingDuration(@QueryParameter String maxScanPendingDuration) {
            return descriptorHelper.doCheckMaxScanPendingDuration(maxScanPendingDuration);
        }

        public FormValidation doCheckMaxScanExecutionDuration(@QueryParameter String maxScanExecutionDuration) {
            return descriptorHelper.doCheckMaxScanExecutionDuration(maxScanExecutionDuration);
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return Messages.displayName();
        }
    }

}
