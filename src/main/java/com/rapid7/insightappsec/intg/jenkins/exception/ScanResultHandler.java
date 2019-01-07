package com.rapid7.insightappsec.intg.jenkins.exception;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStepAction;
import com.rapid7.insightappsec.intg.jenkins.ScanResults;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import hudson.model.Run;
import org.apache.commons.collections.CollectionUtils;

public class ScanResultHandler {

    public static final ScanResultHandler INSTANCE = new ScanResultHandler();

    private ScanResultHandler() {
        // private constructor
    }

    public void handleScanResults(Run<?,?> run,
                                  InsightAppSecLogger logger,
                                  BuildAdvanceIndicator buildAdvanceIndicator,
                                  ScanResults scanResults,
                                  boolean storeScanResults) {
        if (storeScanResults) {
            run.addAction(new InsightAppSecScanStepAction(scanResults));
        }

        if (buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS) &&
            !CollectionUtils.isEmpty(scanResults.getVulnerabilities())) {
            logger.log(String.format("Failing build due to %s non-filtered vulnerabilities", scanResults.getVulnerabilities().size()));

            throw new VulnerabilitySearchException("Non-filtered vulnerabilities were found");
        }
    }
}
