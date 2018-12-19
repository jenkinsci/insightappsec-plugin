package com.rapid7.insightappsec.intg.jenkins.api.search;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class SearchApi extends AbstractApi {

    // PATHS

    private static final String SEARCH = "/search";

    public SearchApi(String host,
                     String apiKey) {
        super(host, apiKey);
    }

    // API OPERATIONS

    public HttpResponse search(SearchRequest searchRequest, int index) throws IOException {
        Map<String, String> params = new HashMap<>();
        params.put("index", String.valueOf(index));

        return post(searchRequest, SEARCH, params);
    }

}
