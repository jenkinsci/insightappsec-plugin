package io.jenkins.plugins.insightappsec.exception;

import io.jenkins.plugins.insightappsec.BuildAdvanceIndicator;
import io.jenkins.plugins.insightappsec.InsightAppSecLogger;
import io.jenkins.plugins.insightappsec.InsightAppSecScanStepAction;
import io.jenkins.plugins.insightappsec.ScanResultHandler;
import io.jenkins.plugins.insightappsec.ScanResults;
import io.jenkins.plugins.insightappsec.api.vulnerability.VulnerabilityModels;
import hudson.model.Run;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static java.util.Collections.singletonList;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ScanResultHandlerTest {

    @Mock
    private Run<?,?> run;

    @Mock
    private InsightAppSecLogger logger;

    @InjectMocks
    private ScanResultHandler scanResultHandler;

    @Test
    public void handleScanResult_vulnerabilityResultsIndicator_vulnerabilitiesPresent() {
        // given
        ScanResults scanResults = ScanResults.builder()
                                             .vulnerabilities(singletonList(VulnerabilityModels.aCompleteVulnerability().build()))
                                             .build();

        // when
        VulnerabilitySearchException thrown = Assert.assertThrows(VulnerabilitySearchException.class, () ->
            scanResultHandler.handleScanResults(run,
                                                logger,
                                                BuildAdvanceIndicator.VULNERABILITY_QUERY,
                                                scanResults,
                                                true)
        );

        // then
        Assert.assertTrue(thrown.getMessage().contains("Non-filtered vulnerabilities were found"));
    }

    @Test
    public void handleScanResult_vulnerabilityResultsIndicator_noVulnerabilitiesPresent() {
        // given
        ScanResults scanResults = ScanResults.builder().build();

        // when
        scanResultHandler.handleScanResults(run,
                                            logger,
                                            BuildAdvanceIndicator.VULNERABILITY_QUERY,
                                            scanResults,
                             true);
        // then
        // no exception
        verify(run, times(1)).addAction(any(InsightAppSecScanStepAction.class));
    }

    @Test
    public void handleScanResult_vulnerabilityResultsIndicator_noVulnerabilitiesPresent_notStoreScanResults() {
        // given
        ScanResults scanResults = ScanResults.builder().build();

        // when
        scanResultHandler.handleScanResults(run,
                                            logger,
                                            BuildAdvanceIndicator.VULNERABILITY_QUERY,
                                            scanResults,
                                            false);
        // then
        // no exception
        verify(run, times(0)).addAction(any(InsightAppSecScanStepAction.class));
    }

    @Test
    public void handleScanResult_nonVulnerabilityResultsIndicator_noVulnerabilitiesPresent() {
        // given
        ScanResults scanResults = ScanResults.builder().build();

        // when
        scanResultHandler.handleScanResults(run,
                                            logger,
                                            BuildAdvanceIndicator.SCAN_COMPLETED,
                                            scanResults,
                             true);
        // then
        // no exception
        verify(run, times(1)).addAction(any(InsightAppSecScanStepAction.class));
    }

}