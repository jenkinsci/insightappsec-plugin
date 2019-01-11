package com.rapid7.insightappsec.intg.jenkins.api.scan;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import com.rapid7.insightappsec.intg.jenkins.api.Identifiable;
import org.apache.http.client.HttpClient;

public class ScanApi extends AbstractApi {

    // PATHS

    private static final String SCANS = "/scans";
    private static final String EXECUTION_DETAILS = "/execution-details";
    private static final String ACTION = "/action";

    public ScanApi(HttpClient client,
                   String host,
                   String apiKey) {
        super(client, host, apiKey);
    }

    // API OPERATIONS

    public String submitScan(String scanConfigId) {
        return post(SCANS, new Scan(new Identifiable(scanConfigId), null));
    }

    public Scan getScan(String scanId) {
        return getById(SCANS + "/" + scanId, scanId, Scan.class);
    }

    public ScanExecutionDetails getScanExecutionDetails(String scanId) {
        return getById(SCANS + "/" + scanId + EXECUTION_DETAILS, scanId, ScanExecutionDetails.class);
    }

    public void submitScanAction(String scanId,
                                 ScanAction scanAction) {
        put(SCANS + "/" + scanId + ACTION, scanAction);
    }

}
