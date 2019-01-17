package org.jenkinsci.plugins.insightappsec.api;

import org.jenkinsci.plugins.insightappsec.Region;
import org.jenkinsci.plugins.insightappsec.api.app.AppApi;
import org.jenkinsci.plugins.insightappsec.api.scan.ScanApi;
import org.jenkinsci.plugins.insightappsec.api.search.SearchApi;
import org.jenkinsci.plugins.insightappsec.credentials.InsightCredentialsHelper;
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
