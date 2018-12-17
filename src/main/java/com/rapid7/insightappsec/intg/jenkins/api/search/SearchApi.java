package com.rapid7.insightappsec.intg.jenkins.api.search;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class SearchApi extends AbstractApi {

    // PATHS

    private static final String SEARCH = "/search";

    public SearchApi(String host,
                        String apiKey) {
        super(host, apiKey);
    }

    // API OPERATIONS

    public HttpResponse search(SearchRequest searchRequest) throws IOException {
        return post(searchRequest, SEARCH);
    }

}
