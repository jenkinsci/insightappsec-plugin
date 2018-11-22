package com.rapid7.insightappsec.intg.jenkins.api.scan;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import com.rapid7.insightappsec.intg.jenkins.api.Id;
import org.apache.http.HttpResponse;

import java.io.IOException;

public class ScanApi extends AbstractApi {

    // PATHS

    private static final String SCANS = "/scans";

    // API OPERATIONS

    public HttpResponse submitScan(String scanConfigId) throws IOException {
        Scan scan = new Scan(new Id(scanConfigId));
        return post(scan, SCANS);
    }

    public HttpResponse getScan(String scanId) throws IOException {
        return get(SCANS + "/" + scanId);
    }

}
