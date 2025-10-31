package io.jenkins.plugins.insightappsec;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import io.jenkins.plugins.insightappsec.api.APIFactory;
import io.jenkins.plugins.insightappsec.api.app.App;
import io.jenkins.plugins.insightappsec.api.app.AppApi;
import io.jenkins.plugins.insightappsec.api.scanconfig.ScanConfig;
import io.jenkins.plugins.insightappsec.api.search.SearchApi;
import io.jenkins.plugins.insightappsec.api.search.SearchRequest;
import io.jenkins.plugins.insightappsec.credentials.InsightCredentialsHelper;
import io.jenkins.plugins.insightappsec.exception.APIException;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class DescriptorHelper {

    private static final String EMPTY_VALUE = "";
    
    private final APIFactory apiFactory;
    private final InsightCredentialsHelper credentialsHelper;
    private final DurationStringParser durationStringParser;

    private AppApi appApi;
    private SearchApi searchApi;

    public DescriptorHelper(APIFactory apiFactory,
                            InsightCredentialsHelper credentialsHelper,
                            DurationStringParser durationStringParser) {
        this.apiFactory = apiFactory;
        this.credentialsHelper = credentialsHelper;
        this.durationStringParser = durationStringParser;
    }

    ListBoxModel getRegionItems() {
        ListBoxModel items = new ListBoxModel();

        items.add(new ListBoxModel.Option(withHyphens(Messages.selectors_prompts_region()), EMPTY_VALUE));
        Stream.of(Region.values()).forEach(r -> items.add(r.getDisplayName(), r.name()));

        return items;
    }

    ListBoxModel getInsightCredentialsIdItems(Jenkins context) {
        if (context == null || !context.hasPermission(Item.CONFIGURE)) {
            return new StandardListBoxModel();
        }

        StandardListBoxModel items = new StandardListBoxModel();

        items.withAll(credentialsHelper.lookupAllInsightCredentials(context))
             .add(0, new ListBoxModel.Option(withHyphens(Messages.selectors_prompts_apiKey()), EMPTY_VALUE));

        return items;
    }

    ListBoxModel getAppIdItems(String region,
                               String insightCredentialsId) {
        if (!StringUtils.isEmpty(region)
            && !StringUtils.isEmpty(insightCredentialsId)) {

            try {
                refreshAppApi(region, insightCredentialsId);

                // collect apps
                List<App> apps = appApi.getApps();
                apps.sort(Comparator.comparing(App::getName));

                // populate items
                ListBoxModel items = new ListBoxModel();
                items.add(withHyphens(Messages.selectors_prompts_app()), EMPTY_VALUE);
                apps.forEach(a -> items.add(nameWithId(a.getName(), a.getId()), a.getId()));

                return items;
            } catch (Exception e) {
                return handleDoFillException(e, withHyphens(Messages.selectors_errors_app()));
            }
        }

        ListBoxModel options = new ListBoxModel();
        options.add(withHyphens(Messages.selectors_dependency_app()), EMPTY_VALUE);

        return options;
    }

    ListBoxModel getScanConfigIdItems(String region,
                                      String insightCredentialsId,
                                      String appId) {
        if (!StringUtils.isEmpty(region)
            && !StringUtils.isEmpty(insightCredentialsId)
            && !StringUtils.isEmpty(appId)) {

            try {
                refreshSearchApi(region, insightCredentialsId);

                // collect scan configs
                SearchRequest searchRequest = new SearchRequest(SearchRequest.SearchType.SCAN_CONFIG,
                                                                String.format("scanconfig.app.id='%s'", appId));
                List<ScanConfig> scanConfigs = searchApi.searchAll(searchRequest, ScanConfig.class);
                scanConfigs.sort(Comparator.comparing(ScanConfig::getName));

                // populate items
                ListBoxModel items = new ListBoxModel();
                items.add(withHyphens(Messages.selectors_prompts_scanConfig()), EMPTY_VALUE);
                scanConfigs.forEach(sc -> items.add(nameWithId(sc.getName(), sc.getId()), sc.getId()));

                return items;
            } catch (Exception e) {
                return handleDoFillException(e, withHyphens(Messages.selectors_errors_scanConfigs()));
            }
        }

        ListBoxModel items = new ListBoxModel();
        items.add(withHyphens(Messages.selectors_dependency_scanConfig()), EMPTY_VALUE);

        return items;
    }

    ListBoxModel getBuildAdvanceIndicatorItems() {
        return Stream.of(BuildAdvanceIndicator.values())
                     .map(bai -> new ListBoxModel.Option(bai.getDisplayName(), bai.name()))
                     .collect(toCollection(ListBoxModel::new));
    }

    FormValidation doCheckVulnerabilityQuery() {
        return FormValidation.okWithMarkup(String.format(Messages.validation_markup_ignoredUnless(),
                                                         Messages.selectors_vulnerabilityQuery()));
    }

    FormValidation doCheckMaxScanPendingDuration(String maxScanPendingDuration) {
        return doCheckDurationString(maxScanPendingDuration,
                                     String.format(Messages.validation_markup_ignoredIf(),
                                                   Messages.selectors_scanSubmitted()));
    }

    FormValidation doCheckMaxScanExecutionDuration(String maxScanExecutionDuration) {
        return doCheckDurationString(maxScanExecutionDuration,
                                     String.format(Messages.validation_markup_ignoredIfComposite(),
                                                   Messages.selectors_scanSubmitted(),
                                                   Messages.selectors_scanStarted()));
    }

    FormValidation doCheckEnableScanResults() {
        return FormValidation.okWithMarkup(String.format(Messages.validation_markup_ignoredIfComposite(),
                                                         Messages.selectors_scanSubmitted(),
                                                         Messages.selectors_scanStarted()));
    }

    FormValidation doCheckRequiredField(String notNullOrEmpty) {
        return StringUtils.isEmpty(notNullOrEmpty) ?
                           FormValidation.error(Messages.validation_errors_required()) :
                           FormValidation.ok();
    }

    // HELPERS

    private void refreshAppApi(String region,
                               String insightCredentialsId) {
        appApi = apiFactory.newAppApi(region, insightCredentialsId);
    }

    private void refreshSearchApi(String region,
                                  String insightCredentialsId) {
        searchApi = apiFactory.newSearchApi(region, insightCredentialsId);
    }

    private ListBoxModel handleDoFillException(Exception e,
                                               String entity) {
        if (e instanceof APIException
            && ((APIException)e).getResponse() != null &&
            ((APIException)e).getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

            ListBoxModel items = new ListBoxModel();
            items.add(withHyphens(Messages.selectors_errors_forbidden()), EMPTY_VALUE);

            return items;
        } else {
            ListBoxModel items = new ListBoxModel();
            items.add(entity, EMPTY_VALUE);

            return items;
        }
    }

    private FormValidation doCheckDurationString(String durationString,
                                                 String defaultMarkup) {
        try {
            durationStringParser.parseDurationString(durationString);

            return FormValidation.okWithMarkup(defaultMarkup);
        } catch (Exception e) {
            return FormValidation.error(Messages.validation_errors_invalidDuration());
        }
    }

    private String nameWithId(String name,
                              String id) {
        return String.format("%s (%s)", name, id);
    }

    private String withHyphens(String string) {
        return String.format("- %s -", string);
    }

}
