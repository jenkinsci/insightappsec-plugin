package com.rapid7.insightappsec.intg.jenkins.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import static com.rapid7.insightappsec.intg.jenkins.MappingConfiguration.OBJECT_MAPPER_INSTANCE;

public abstract class AbstractApi {

    // HEADERS

    private static final String X_API_KEY_HEADER = "x-api-key";

    // FIELDS

    private final HttpClient client;
    private final String host;
    private final String apiKey;

    protected AbstractApi(String host,
                          String apiKey) {
        this.client = HttpClientBuilder.create().build();
        this.host = host;
        this.apiKey = apiKey;
    }

    // HELPERS

    protected HttpResponse get(String path) throws IOException {
        return client.execute(createGet(path));
    }

    protected HttpResponse post(Object body, String path) throws IOException {
        return client.execute(createPost(body, path));
    }

    private HttpGet createGet(String path) {
        HttpGet get = new HttpGet(buildUri(path));

        get.addHeader(X_API_KEY_HEADER, apiKey);

        return get;
    }

    private HttpPost createPost(Object body, String path) throws JsonProcessingException {
        String json = OBJECT_MAPPER_INSTANCE.writeValueAsString(body);

        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost post = new HttpPost(buildUri(path));
        post.addHeader(X_API_KEY_HEADER, apiKey);
        post.setEntity(requestEntity);

        return post;
    }

    private URI buildUri(String path) {
        try {
            return new URI(String.format("https://%s/ias/v1%s", host, path));
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
