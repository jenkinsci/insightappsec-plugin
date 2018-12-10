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

    // TEMP - will be replaced by user configuration

    private static final String API_KEY = "70ccbe5d-f081-4e62-9480-4fc925cf2552";
    private static final String URL = "https://us.api.insight.rapid7.com";

    // FIELDS

    private final HttpClient client;

    protected AbstractApi() {
        this.client = HttpClientBuilder.create().build();
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

        get.addHeader(X_API_KEY_HEADER, API_KEY);

        return get;
    }

    private HttpPost createPost(Object body, String path) throws JsonProcessingException {
        String json = OBJECT_MAPPER_INSTANCE.writeValueAsString(body);

        StringEntity requestEntity = new StringEntity(json, ContentType.APPLICATION_JSON);

        HttpPost post = new HttpPost(buildUri(path));
        post.addHeader(X_API_KEY_HEADER, API_KEY);
        post.setEntity(requestEntity);

        return post;
    }

    private URI buildUri(String path) {
        try {
            return new URI(URL + "/ias/v1" + path);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

}
