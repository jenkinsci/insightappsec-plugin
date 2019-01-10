package com.rapid7.insightappsec.intg.jenkins;

import com.cloudbees.plugins.credentials.common.StandardListBoxModel;
import com.rapid7.insightappsec.intg.jenkins.api.app.App;
import com.rapid7.insightappsec.intg.jenkins.api.app.AppApi;
import com.rapid7.insightappsec.intg.jenkins.api.scanconfig.ScanConfig;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequest;
import com.rapid7.insightappsec.intg.jenkins.credentials.InsightCredentialsHelper;
import com.rapid7.insightappsec.intg.jenkins.exception.APIException;
import hudson.model.Item;
import hudson.util.FormValidation;
import hudson.util.ListBoxModel;
import jenkins.model.Jenkins;
import org.apache.commons.lang.StringUtils;
import org.apache.http.HttpStatus;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toCollection;

public class DescriptorHelper {

    private final InsightCredentialsHelper credentialsHelper;
    private final DurationStringParser durationStringParser;

    private AppApi appApi;
    private SearchApi searchApi;

    /**
     * No arg constructor - default
     */
    public DescriptorHelper() {
        this.credentialsHelper = InsightCredentialsHelper.INSTANCE;
        this.durationStringParser = DurationStringParser.INSTANCE;
    }

    /**
     * All arg constructor - for unit tests only
     */
    public DescriptorHelper(InsightCredentialsHelper credentialsHelper,
                            DurationStringParser durationStringParser,
                            AppApi appApi,
                            SearchApi searchApi) {
        this.credentialsHelper = credentialsHelper;
        this.durationStringParser = durationStringParser;
        this.appApi = appApi;
        this.searchApi = searchApi;
    }

    ListBoxModel getRegionItems() {
        ListBoxModel items = new ListBoxModel();

        items.add(new ListBoxModel.Option("- Select region -", ""));
        Stream.of(Region.values()).forEach(r -> items.add(r.getDisplayName(), r.name()));

        return items;
    }

    ListBoxModel getInsightCredentialsIdItems(Jenkins context) {
        if (context == null || !context.hasPermission(Item.CONFIGURE)) {
            return new StandardListBoxModel();
        }

        StandardListBoxModel items = new StandardListBoxModel();

        items.withAll(credentialsHelper.lookupAllInsightCredentials(context))
             .add(0, new ListBoxModel.Option("- Select API key -", ""));

        return items;
    }

    ListBoxModel getAppIdItems(String region,
                               String insightCredentialsId) {
        if (!StringUtils.isEmpty(region)
            && !StringUtils.isEmpty(insightCredentialsId)) {

            try {
                initAppApi(region, insightCredentialsId);

                // collect apps
                List<App> apps = appApi.getApps();
                apps.sort(Comparator.comparing(App::getName));

                // populate items
                ListBoxModel items = new ListBoxModel();
                items.add("- Select app -", "");
                apps.forEach(a -> items.add(String.format("%s (%s)", a.getName(), a.getId()), a.getId()));

                return items;
            } catch (Exception e) {
                return handleDoFillException(e, "apps");
            }
        }

        ListBoxModel options = new ListBoxModel();
        options.add("- First select region and API key -", "");

        return options;
    }

    ListBoxModel getScanConfigIdItems(String region,
                                      String insightCredentialsId,
                                      String appId) {
        if (!StringUtils.isEmpty(region)
            && !StringUtils.isEmpty(insightCredentialsId)
            && !StringUtils.isEmpty(appId)) {

            try {
                initSearchApi(region, insightCredentialsId);

                // collect scan configs
                SearchRequest searchRequest = new SearchRequest(SearchRequest.SearchType.SCAN_CONFIG,
                                                                String.format("scanconfig.app.id='%s'", appId));
                List<ScanConfig> scanConfigs = searchApi.searchAll(searchRequest, ScanConfig.class);
                scanConfigs.sort(Comparator.comparing(ScanConfig::getName));

                // populate items
                ListBoxModel items = new ListBoxModel();
                items.add("- Select scan config -", "");
                scanConfigs.forEach(sc -> items.add(String.format("%s (%s)", sc.getName(), sc.getId()), sc.getId()));

                return items;
            } catch (Exception e) {
                return handleDoFillException(e, "scan configs");
            }
        }

        ListBoxModel items = new ListBoxModel();
        items.add("- First select app -", "");

        return items;
    }

    ListBoxModel getBuildAdvanceIndicatorItems() {
        return Stream.of(BuildAdvanceIndicator.values())
                     .map(bai -> new ListBoxModel.Option(bai.getDisplayName(), bai.name()))
                     .collect(toCollection(ListBoxModel::new));
    }

    FormValidation doCheckVulnerabilityQuery() {
        return FormValidation.okWithMarkup(String.format(Messages.validation_markup_vulnerabilityQuery(),
                                                         Messages.selectors_vulnerabilityQuery()));
    }

    FormValidation doCheckMaxScanPendingDuration(String maxScanPendingDuration) {
        return doCheckDurationString(maxScanPendingDuration,
                                     String.format(Messages.validation_markup_maxScanPendingDuration(),
                                                   Messages.selectors_scanSubmitted()));
    }

    FormValidation doCheckMaxScanExecutionDuration(String maxScanExecutionDuration) {
        return doCheckDurationString(maxScanExecutionDuration,
                                     String.format(Messages.validation_markup_maxScanExecutionDuration(),
                                                   Messages.selectors_scanSubmitted(),
                                                   Messages.selectors_scanStarted()));
    }

    // HELPERS

    private void initAppApi(String region,
                            String insightCredentialsId) {
        String host = Region.fromString(region).getAPIHost();
        String apiKey = credentialsHelper.lookupInsightCredentialsById(insightCredentialsId)
                                         .getApiKey()
                                         .getPlainText();
        if (appApi == null) {
            appApi = new AppApi(host, apiKey);
        } else {
            // always use latest host / key as subject to change during configuration
            appApi.setHost(host);
            appApi.setApiKey(apiKey);
        }
    }

    private void initSearchApi(String region,
                               String insightCredentialsId) {
        String host = Region.fromString(region).getAPIHost();
        String apiKey = credentialsHelper.lookupInsightCredentialsById(insightCredentialsId)
                                         .getApiKey()
                                         .getPlainText();
        if (searchApi == null) {
            searchApi = new SearchApi(host, apiKey);
        } else {
            // always use latest host / key as subject to change during configuration
            searchApi.setHost(host);
            searchApi.setApiKey(apiKey);
        }
    }

    private ListBoxModel handleDoFillException(Exception e,
                                               String entity) {
        if (e instanceof APIException
            && ((APIException)e).getResponse() != null &&
            ((APIException)e).getResponse().getStatusLine().getStatusCode() == HttpStatus.SC_UNAUTHORIZED) {

            ListBoxModel items = new ListBoxModel();
            items.add("- Invalid API key: Forbidden -", "");

            return items;
        } else {
            ListBoxModel items = new ListBoxModel();
            items.add(String.format("- Error loading %s -", entity), "");

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

}
