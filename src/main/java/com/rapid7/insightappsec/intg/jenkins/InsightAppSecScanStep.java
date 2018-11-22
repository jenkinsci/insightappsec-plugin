package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
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
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.UUID;
import java.util.stream.Stream;

public class InsightAppSecScanStep extends Builder implements SimpleBuildStep {

    private final InsightAppSecScanStepRunner runner;

    private final String scanConfigId;
    private final String buildAdvanceIndicator;

    @DataBoundConstructor
    public InsightAppSecScanStep(String scanConfigId,
                                 String buildAdvanceIndicator) {
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.buildAdvanceIndicator = buildAdvanceIndicator;
        this.runner = createRunner();
    }

    public String getScanConfigId() {
        return scanConfigId;
    }

    public String getBuildAdvanceIndicator() {
        return buildAdvanceIndicator;
    }

    @Override
    public void perform(Run<?, ?> run,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) throws InterruptedException {
        runner.setLogger(new InsightAppSecLogger(listener.getLogger()));
        runner.run(scanConfigId, BuildAdvanceIndicator.fromString(buildAdvanceIndicator));
    }

    // HELPERS

    private InsightAppSecScanStepRunner createRunner() {
        return new InsightAppSecScanStepRunner(new ScanApi(), new ThreadHelper());
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public ListBoxModel doFillBuildAdvanceIndicatorItems() {
            ListBoxModel items = new ListBoxModel();

            Stream.of(BuildAdvanceIndicator.values()).forEach(item -> items.add(item.getDisplayName(), item.name()));

            return items;
        }

        public FormValidation doCheckVulnerabilityQuery(@QueryParameter String vulnerabilityQuery) {
            return FormValidation.okWithMarkup(String.format(Messages.validation_markup_vulnerabilityQuery(),
                                                             Messages.selectors_vulnerabilityQuery()));
        }

        public FormValidation doCheckScanConfigId(@QueryParameter String scanConfigId) {
            return doCheckId(scanConfigId);
        }

        private FormValidation doCheckId(String id) {
            id = Util.fixEmptyAndTrim(id);

            if (id == null) {
                return FormValidation.error(Messages.validation_errors_requiredId());
            }

            try {
                UUID.fromString(id);
            } catch (Exception e) {
                return FormValidation.error(Messages.validation_errors_invalidId());
            }

            return FormValidation.ok();
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
