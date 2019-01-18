package io.jenkins.plugins.api.search;

public class SearchRequestModels {

    public static SearchRequest.SearchRequestBuilder aSearchRequest() {
        return SearchRequest.builder();
    }

    public static SearchRequest.SearchRequestBuilder aVulnerabilitySearchRequest() {
        return aSearchRequest().type(SearchRequest.SearchType.VULNERABILITY);
    }

    public static SearchRequest.SearchRequestBuilder aScanConfigSearchRequest() {
        return aSearchRequest().type(SearchRequest.SearchType.SCAN_CONFIG);
    }
}
