package io.jenkins.plugins.insightappsec.api.search;

import io.jenkins.plugins.insightappsec.api.Page;

import java.util.function.Supplier;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

public class PageModels {

    public static Page.PageBuilder aPage() {
        return Page.builder();
    }

    public static <T> Page.PageBuilder aPageOf(Supplier<T> supplier, int size) {
        return aPage().data(Stream.generate(supplier).limit(size).collect(toList()));

    }

    public static Page.Metadata.MetadataBuilder aMetadata() {
        return Page.Metadata.builder();
    }

}
