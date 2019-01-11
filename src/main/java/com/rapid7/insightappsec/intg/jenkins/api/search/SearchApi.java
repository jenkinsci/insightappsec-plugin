package com.rapid7.insightappsec.intg.jenkins.api.search;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import org.apache.http.client.HttpClient;

import java.util.List;

public class SearchApi extends AbstractApi {

    // PATHS

    private static final String SEARCH = "/search";

    public SearchApi(HttpClient client,
                     String host,
                     String apiKey) {
        super(client, host, apiKey);
    }

    // API OPERATIONS

    public <T> List<T> searchAll(SearchRequest searchRequest,
                                 Class<T> clazz) {
        return postForAll(SEARCH, clazz, searchRequest);
    }

}
