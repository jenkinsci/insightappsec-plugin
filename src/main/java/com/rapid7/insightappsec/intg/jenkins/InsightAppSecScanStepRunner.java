package com.rapid7.insightappsec.intg.jenkins;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanRetrievalFailedException;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanSubmissionFailedException;
import org.apache.commons.io.IOUtils;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

import java.util.concurrent.TimeUnit;

import static java.lang.String.format;

public class InsightAppSecScanStepRunner {

    private final ScanApi scanApi;
    private final ObjectMapper mapper;

    private InsightAppSecLogger logger;

    InsightAppSecScanStepRunner(ScanApi scanApi) {
        this.scanApi = scanApi;
        this.mapper = new ObjectMapper();
    }

    public void setLogger(InsightAppSecLogger logger) {
        this.logger = logger;
    }

    public void run(String scanConfigId,
                    BuildAdvanceIndicator buildAdvanceIndicator) throws InterruptedException {
        String scanId = submitScan(scanConfigId);

        logger.log("Using build advance indicator: '%s'", buildAdvanceIndicator.getDisplayName());

        switch (buildAdvanceIndicator) {
            case SCAN_SUBMITTED:
                // non-blocking
                break;
            case SCAN_STARTED:
                blockUntilStatus(scanId, Scan.ScanStatus.RUNNING);
                break;
            case SCAN_COMPLETED:
                blockUntilStatus(scanId, Scan.ScanStatus.COMPLETE);
                break;
            case VULNERABILITY_RESULTS:
                blockUntilStatus(scanId, Scan.ScanStatus.COMPLETE);
                // TODO: Integrate vuln results
                break;
        }
    }

    private void blockUntilStatus(String scanId,
                                  Scan.ScanStatus desiredStatus) throws InterruptedException {
        logger.log("Beginning polling for scan with id: %s", scanId);

        // TODO: Currently this will fail the build if the get scan request fails once, should we introduce a tolerance threshold?
        //       e.g. Only fail if this request fails more than x times
        Scan scan = getScan(scanId);
        Scan.ScanStatus cachedStatus = scan.getStatus();

        logger.log("Scan status: %s", cachedStatus);

        while (true) {

            if (!cachedStatus.equals(scan.getStatus())) {
                logger.log("Scan status has been updated from %s to %s", cachedStatus, scan.getStatus());
                cachedStatus = scan.getStatus();
            }

            if (scan.getStatus().equals(desiredStatus)) {
                logger.log("Desired scan status has been reached");
                break;
            } else {
                Thread.sleep(TimeUnit.SECONDS.toMillis(30));
            }

            // TODO: Same as above
            scan = getScan(scanId);
        }
    }

    private String submitScan(String scanConfigId) {
        logger.log("Submitting scan for scan config with id: %s", scanConfigId);

        try {
            HttpResponse response = scanApi.submitScan(scanConfigId);

            if (response.getStatusLine().getStatusCode() == 201) {
                logger.log("Scan submitted successfully");

                String locationHeader = response.getHeaders(HttpHeaders.LOCATION)[0].getValue();
                String scanId = locationHeader.substring(locationHeader.lastIndexOf('/') + 1);

                logger.log("Scan id: %s", scanId);

                return scanId;
            } else {
                throw new ScanSubmissionFailedException(format("Error occurred submitting scan. Response %n %s", response));
            }

        } catch (Exception e) {
            throw new ScanSubmissionFailedException("Error occurred submitting scan", e);
        }
    }

    private Scan getScan(String scanId) {
        try {
            HttpResponse response = scanApi.getScan(scanId);

            if (response.getStatusLine().getStatusCode() == 200) {
                String content = IOUtils.toString(response.getEntity().getContent());

                return mapper.readValue(content, Scan.class);
            } else {
                throw new ScanRetrievalFailedException(format("Error occurred retrieving scan with id %s. Response %n %s", scanId, response));
            }

        } catch (Exception e) {
            throw new ScanRetrievalFailedException(format("Error occurred retrieving scan with id %s", scanId), e);
        }
    }
}
