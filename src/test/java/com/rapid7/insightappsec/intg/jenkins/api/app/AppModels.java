package com.rapid7.insightappsec.intg.jenkins.api.app;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public class AppModels {

    public static App.AppBuilder anApp() {
        return App.builder();
    }

    public static App.AppBuilder aCompleteApp() {
        return anApp().id(UUID.randomUUID().toString())
                      .name(RandomStringUtils.randomAlphanumeric(8));
    }

}
