package com.rapid7.insightappsec.intg.jenkins.api.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchRequest {

    private SearchType type;
    private String query;

    public enum SearchType {

        VULNERABILITY

    }

}
