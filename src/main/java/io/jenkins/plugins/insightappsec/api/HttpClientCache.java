package io.jenkins.plugins.insightappsec.api;

import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.HttpClientBuilder;

public class HttpClientCache {

    public static final HttpClient SEARCH_API_HTTP_CLIENT = defaultHttpClient();
    public static final HttpClient APP_API_HTTP_CLIENT = defaultHttpClient();
    public static final HttpClient SCAN_API_HTTP_CLIENT = defaultHttpClient();

    private static HttpClient defaultHttpClient() {
        HttpClientBuilder builder = HttpClientBuilder.create();
        ProxyUtil.configureProxy(builder);
        return builder.build();
    }

}
