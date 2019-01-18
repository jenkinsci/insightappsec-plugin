package io.jenkins.plugins.api;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientCache {

    public static final HttpClient SEARCH_API_HTTP_CLIENT = defaultHttpClient();
    public static final HttpClient APP_API_HTTP_CLIENT = defaultHttpClient();
    public static final HttpClient SCAN_API_HTTP_CLIENT = defaultHttpClient();

    private static HttpClient defaultHttpClient() {
        return HttpClientBuilder.create().build();
    }

}
