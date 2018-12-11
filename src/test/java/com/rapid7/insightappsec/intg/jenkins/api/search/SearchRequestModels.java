package com.rapid7.insightappsec.intg.jenkins.api.search;

public class SearchRequestModels {

    public static SearchRequest.SearchRequestBuilder aSearchRequest() {
        return SearchRequest.builder();
    }

    public static SearchRequest.SearchRequestBuilder aVulnerabilitySearchRequest() {
        return aSearchRequest().type(SearchRequest.SearchType.VULNERABILITY);
    }
}
