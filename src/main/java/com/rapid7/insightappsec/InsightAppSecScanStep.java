package com.rapid7.insightappsec;

import com.rapid7.insightappsec.api.SafeLogger;
import com.rapid7.insightappsec.api.scan.ScanApi;
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
import jenkins.tasks.SimpleBuildStep;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.QueryParameter;

import java.util.UUID;

public class InsightAppSecScanStep extends Builder implements SimpleBuildStep {

    private final InsightAppSecScanStepRunner runner;

    private final String scanConfigId;

    @DataBoundConstructor
    public InsightAppSecScanStep(String scanConfigId) {
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.runner = createRunner();
    }

    public String getScanConfigId() {
        return scanConfigId;
    }

    @Override
    public void perform(Run<?, ?> run,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) {
        runner.setLogger(new SafeLogger(listener.getLogger()));
        runner.run(scanConfigId);
    }

    // HELPERS

    private InsightAppSecScanStepRunner createRunner() {
        return new InsightAppSecScanStepRunner(new ScanApi());
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public FormValidation doCheckScanConfigId(@QueryParameter String scanConfigId) {
            return doCheckId(scanConfigId);
        }

        private FormValidation doCheckId(String id) {
            id = Util.fixEmptyAndTrim(id);

            if (id == null) {
                return FormValidation.error(com.rapid7.insightappsec.Messages.errors_requiredId());
            }

            try {
                UUID.fromString(id);
            } catch (Exception e) {
                return FormValidation.error(com.rapid7.insightappsec.Messages.errors_invalidId());
            }

            return FormValidation.ok();
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return com.rapid7.insightappsec.Messages.displayName();
        }

    }

}
