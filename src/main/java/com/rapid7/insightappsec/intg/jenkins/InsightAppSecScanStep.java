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
    private final boolean storeScanResults;
    private final String maxScanStartWaitTime;
    private final String maxScanRuntime;

    @DataBoundConstructor
    public InsightAppSecScanStep(String region,
                                 String insightCredentialsId,
                                 String appId,
                                 String scanConfigId,
                                 String buildAdvanceIndicator,
                                 String vulnerabilityQuery,
                                 boolean storeScanResults,
                                 String maxScanStartWaitTime,
                                 String maxScanRuntime) {
        this.region = Region.fromString(region).name();
        this.insightCredentialsId = Util.fixEmptyAndTrim(insightCredentialsId);
        this.appId = Util.fixEmptyAndTrim(appId);
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.buildAdvanceIndicator = BuildAdvanceIndicator.fromString(buildAdvanceIndicator).name();
        this.vulnerabilityQuery = Util.fixEmptyAndTrim(vulnerabilityQuery);
        this.storeScanResults = storeScanResults;
        this.maxScanStartWaitTime = Util.fixEmptyAndTrim(maxScanStartWaitTime);
        this.maxScanRuntime = Util.fixEmptyAndTrim(maxScanRuntime);
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

    public boolean isStoreScanResults() {
        return storeScanResults;
    }

    public String getMaxScanStartWaitTime() {
        return maxScanStartWaitTime;
    }

    public String getMaxScanRuntime() {
        return maxScanRuntime;
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
        if (storeScanResults && scanResults.isPresent()) {
            new ScanResultHandler().handleScanResult(run, logger, bai, scanResults.get());
        }
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
                                               newWaitTimeHandler(scanApi, logger));
    }

    private WaitTimeHandler newWaitTimeHandler(ScanApi scanApi,
                                               InsightAppSecLogger logger) {
        long maxScanRuntimeDuration = WaitTimeParser.INSTANCE.parseWaitTimeString(maxScanRuntime);
        long maxScanStartWaitTimeDuration = WaitTimeParser.INSTANCE.parseWaitTimeString(maxScanStartWaitTime);

        return new WaitTimeHandler(BuildAdvanceIndicator.fromString(buildAdvanceIndicator),
                                   maxScanStartWaitTimeDuration,
                                   maxScanRuntimeDuration,
                                   scanApi,
                                   logger);
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

        public FormValidation doCheckMaxScanStartWaitTime(@QueryParameter String maxScanStartWaitTime) {
            return descriptorHelper.doCheckMaxScanStartWaitTime(maxScanStartWaitTime);
        }

        public FormValidation doCheckMaxScanRuntime(@QueryParameter String maxScanRuntime) {
            return descriptorHelper.doCheckMaxScanRuntime(maxScanRuntime);
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
