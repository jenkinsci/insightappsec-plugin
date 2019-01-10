package com.rapid7.insightappsec.intg.jenkins.api.scan;

import com.rapid7.insightappsec.intg.jenkins.api.AbstractApi;
import com.rapid7.insightappsec.intg.jenkins.api.Identifiable;

public class ScanApi extends AbstractApi {

    // PATHS

    private static final String SCANS = "/scans";
    private static final String EXECUTION_DETAILS = "/execution-details";
    private static final String ACTION = "/action";

    public ScanApi(String host,
                   String apiKey) {
        super(host, apiKey);
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
