package com.rapid7.insightappsec;

import com.rapid7.insightappsec.api.SafeLogger;
import com.rapid7.insightappsec.api.scan.ScanApi;
import com.rapid7.insightappsec.exception.ScanSubmissionFailedException;
import org.apache.http.HttpResponse;

import java.io.IOException;

import static java.lang.String.format;

public class InsightAppSecScanStepRunner {

    private final ScanApi scanApi;

    private SafeLogger logger;

    InsightAppSecScanStepRunner(ScanApi scanApi) {
        this.scanApi = scanApi;
    }

    public void setLogger(SafeLogger logger) {
        this.logger = logger;
    }

    public void run(String scanConfigId) {
        submitScan(scanConfigId);
    }

    private void submitScan(String scanConfigId) {
        logger.log("Submitting scan for scan config: %s", scanConfigId);

        try {
            HttpResponse response = scanApi.submitScan(scanConfigId);

            if (response.getStatusLine().getStatusCode() == 201) {
                logger.log("Scan submitted successfully");
                logger.log("Response: %n %s", response);
            } else {
                throw new ScanSubmissionFailedException(format("Error occurred submitting scan. Response %n %s", response));
            }

        } catch (IOException e) {
            throw new ScanSubmissionFailedException("Error occurred submitting scan", e);
        }

    }
}
