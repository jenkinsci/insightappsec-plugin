package com.rapid7.insightappsec.intg.jenkins.api.search;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class SearchApi extends AbstractApi {

    public static final SearchApi INSTANCE = new SearchApi();

    // PATHS

    private static final String SEARCH = "/search";

    private SearchApi() {
        // private constructor
    }

    // API OPERATIONS

    public HttpResponse search(SearchRequest searchRequest) throws IOException {
        return post(searchRequest, SEARCH);
    }

}
