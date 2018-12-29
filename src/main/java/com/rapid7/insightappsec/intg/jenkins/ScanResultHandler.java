package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.exception.VulnerabilitySearchException;
import hudson.model.Run;
import org.apache.commons.collections.CollectionUtils;

public class ScanResultHandler {

    public void handleScanResult(Run<?,?> run,
                                 InsightAppSecLogger logger,
                                 BuildAdvanceIndicator buildAdvanceIndicator,
                                 ScanResults scanResults) {
        // persist scan results
        run.addAction(new InsightAppSecScanStepAction(scanResults));

        if (buildAdvanceIndicator.equals(BuildAdvanceIndicator.VULNERABILITY_RESULTS) &&
            !CollectionUtils.isEmpty(scanResults.getVulnerabilities())) {
            logger.log(String.format("Failing build due to %s non-filtered vulnerabilities", scanResults.getVulnerabilities().size()));

            throw new VulnerabilitySearchException("Non-filtered vulnerabilities were found");
        }
    }
}
