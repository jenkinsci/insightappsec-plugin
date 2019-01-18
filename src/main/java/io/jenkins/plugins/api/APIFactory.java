package io.jenkins.plugins.api;

import io.jenkins.plugins.Region;
import io.jenkins.plugins.api.app.AppApi;
import io.jenkins.plugins.api.scan.ScanApi;
import io.jenkins.plugins.api.search.SearchApi;
import io.jenkins.plugins.credentials.InsightCredentialsHelper;
import org.apache.http.client.HttpClient;

public class APIFactory {

    private final InsightCredentialsHelper insightCredentialsHelper;

    private final HttpClient searchApiHttpClient;
    private final HttpClient appApiHttpClient;
    private final HttpClient scanApiHttpClient;

    public APIFactory(InsightCredentialsHelper insightCredentialsHelper,
                      HttpClient searchApiHttpClient,
                      HttpClient appApiHttpClient,
                      HttpClient scanApiHttpClient) {
        this.insightCredentialsHelper = insightCredentialsHelper;
        this.searchApiHttpClient = searchApiHttpClient;
        this.appApiHttpClient = appApiHttpClient;
        this.scanApiHttpClient = scanApiHttpClient;
    }

    public SearchApi newSearchApi(String regionString,
                                  String insightCredentialsId) {
        return new SearchApi(searchApiHttpClient, getHost(regionString), getApiKey(insightCredentialsId));
    }

    public AppApi newAppApi(String regionString,
                            String insightCredentialsId) {
        return new AppApi(appApiHttpClient, getHost(regionString), getApiKey(insightCredentialsId));
    }

    public ScanApi newScanApi(String regionString,
                              String insightCredentialsId) {
        return new ScanApi(scanApiHttpClient, getHost(regionString), getApiKey(insightCredentialsId));
    }

    // HELPERS

    private String getHost(String regionString) {
        return Region.fromString(regionString).getAPIHost();
    }

    private String getApiKey(String insightCredentialsId) {
        return insightCredentialsHelper.lookupInsightCredentialsById(insightCredentialsId)
                .getApiKey()
                .getPlainText();
    }

}
