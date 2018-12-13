package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
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

import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class InsightAppSecScanStep extends Builder implements SimpleBuildStep {

    private final String scanConfigId;
    private final BuildAdvanceIndicator buildAdvanceIndicator;
    private final String vulnerabilityQuery;

    @DataBoundConstructor
    public InsightAppSecScanStep(String scanConfigId,
                                 String buildAdvanceIndicator,
                                 String vulnerabilityQuery) {
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.buildAdvanceIndicator = BuildAdvanceIndicator.fromString(buildAdvanceIndicator);
        this.vulnerabilityQuery = Util.fixEmptyAndTrim(vulnerabilityQuery);
    }

    public String getScanConfigId() {
        return scanConfigId;
    }

    public BuildAdvanceIndicator getBuildAdvanceIndicator() {
        return buildAdvanceIndicator;
    }

    public String getVulnerabilityQuery() {
        return vulnerabilityQuery;
    }

    @Override
    public void perform(Run<?, ?> run,
                        FilePath workspace,
                        Launcher launcher,
                        TaskListener listener) throws InterruptedException {
        newRunner(listener.getLogger()).run(scanConfigId,
                                            buildAdvanceIndicator,
                                            Optional.ofNullable(vulnerabilityQuery));
    }

    // HELPERS

    private InsightAppSecScanStepRunner newRunner(PrintStream printStream) {
        return new InsightAppSecScanStepRunner(ScanApi.INSTANCE,
                                               SearchApi.INSTANCE,
                                               ThreadHelper.INSTANCE,
                                               new InsightAppSecLogger(printStream));
    }

    @Extension
    public static final class DescriptorImpl extends BuildStepDescriptor<Builder> {

        public ListBoxModel doFillBuildAdvanceIndicatorItems() {
            return Stream.of(BuildAdvanceIndicator.values())
                         .map(bi -> new ListBoxModel.Option(bi.getDisplayName(), bi.name()))
                         .collect(toCollection(ListBoxModel::new));
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
