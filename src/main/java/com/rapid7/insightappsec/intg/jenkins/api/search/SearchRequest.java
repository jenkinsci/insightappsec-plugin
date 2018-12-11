package com.rapid7.insightappsec.intg.jenkins.api.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class SearchRequest {

    private SearchType type;
    private String query;

    public enum SearchType {

        VULNERABILITY

    }

}
