package com.rapid7.insightappsec.api.scan;

import com.rapid7.insightappsec.api.AbstractApi;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class ScanApi extends AbstractApi {

    // PATHS

    private static final String SCANS = "/scans";

    // API OPERATIONS

    public HttpResponse submitScan(String scanConfigId) throws IOException {
        Scan scan = new Scan(new Scan.ScanConfig(scanConfigId));
        return post(scan, SCANS);
    }

}
