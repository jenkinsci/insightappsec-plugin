package io.jenkins.plugins.api.app;

import io.jenkins.plugins.api.AbstractApi;
import org.apache.http.client.HttpClient;

import java.util.List;

public class AppApi extends AbstractApi {

    // PATHS

    private static final String APPS = "/apps";

    public AppApi(HttpClient client,
                  String host,
                  String apiKey) {
        super(client, host, apiKey);
    }

    // API OPERATIONS

    public List<App> getApps() {
        return getForAll(APPS, App.class);
    }

}
