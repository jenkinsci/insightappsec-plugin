package com.rapid7.insightappsec.intg.jenkins.api.search;

public class SearchResultModels {

    public static SearchResult.SearchResultBuilder aSearchResult() {
        return SearchResult.builder();
    }

    public static SearchResult.Metadata.MetadataBuilder aMetadata() {
        return SearchResult.Metadata.builder();
    }

}