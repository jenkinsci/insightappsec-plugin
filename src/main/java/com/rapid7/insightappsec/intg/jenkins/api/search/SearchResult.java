package com.rapid7.insightappsec.intg.jenkins.api.search;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchResult<T> {

    private Metadata metadata;
    private List<T> data;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class Metadata {

        private int index;
        private int totalPages;

    }

}
