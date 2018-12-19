package com.rapid7.insightappsec.intg.jenkins;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightCredentialsHelper;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractProject;
import hudson.model.Item;
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

import java.io.PrintStream;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class InsightAppSecPlugin extends Builder implements SimpleBuildStep {

    private final String scanConfigId;
    private final BuildAdvanceIndicator buildAdvanceIndicator;
    private final String vulnerabilityQuery;
    private final Region region;
    private final String credentialsId;

    @DataBoundConstructor
    public InsightAppSecPlugin(String scanConfigId,
                               String buildAdvanceIndicator,
                               String vulnerabilityQuery,
                               String region,
                               String credentialsId) {
        this.scanConfigId = Util.fixEmptyAndTrim(scanConfigId);
        this.buildAdvanceIndicator = BuildAdvanceIndicator.fromString(buildAdvanceIndicator);
        this.vulnerabilityQuery = Util.fixEmptyAndTrim(vulnerabilityQuery);
        this.region = Region.fromString(region);
        this.credentialsId = Util.fixEmptyAndTrim(credentialsId);
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

    public Region getRegion() {
        return region;
    }

    public String getCredentialsId() {
        return credentialsId;
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
        String apiKey = InsightCredentialsHelper.lookupInsightCredentialsById(credentialsId).getApiKey().getPlainText();

        return new InsightAppSecScanStepRunner(new ScanApi(region.getAPIHost(), apiKey),
                                               new SearchApi(region.getAPIHost(), apiKey),
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

        public ListBoxModel doFillRegionItems() {
            return Stream.of(Region.values())
                         .map(bi -> new ListBoxModel.Option(bi.getDisplayName(), bi.name()))
                         .collect(toCollection(ListBoxModel::new));
        }

        public ListBoxModel doFillCredentialsIdItems(@AncestorInPath Jenkins context) {
            if (context == null || !context.hasPermission(Item.CONFIGURE)) {
                return new StandardListBoxModel();
            }

            return new StandardListBoxModel().withAll(InsightCredentialsHelper.lookupAllInsightCredentials(context));
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
