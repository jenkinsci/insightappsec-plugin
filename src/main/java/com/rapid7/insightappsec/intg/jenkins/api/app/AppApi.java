package com.rapid7.insightappsec.intg.jenkins.api.app;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;

import java.util.List;

public class AppApi extends AbstractApi {

    // PATHS

    private static final String APPS = "/apps";

    public AppApi(String host,
                  String apiKey) {
        super(host, apiKey);
    }

    // API OPERATIONS

    public List<App> getApps() {
        return getForAll(APPS, App.class);
    }

}
