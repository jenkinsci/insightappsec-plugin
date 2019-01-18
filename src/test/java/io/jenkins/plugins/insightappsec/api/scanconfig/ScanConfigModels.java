package io.jenkins.plugins.insightappsec.api.scanconfig;

import org.apache.commons.lang3.RandomStringUtils;

import java.util.UUID;

public class ScanConfigModels {

    public static ScanConfig.ScanConfigBuilder anScanConfig() {
        return ScanConfig.builder();
    }

    public static ScanConfig.ScanConfigBuilder aCompleteScanConfig() {
        return anScanConfig().id(UUID.randomUUID().toString())
                             .name(RandomStringUtils.randomAlphanumeric(8));
    }

}
