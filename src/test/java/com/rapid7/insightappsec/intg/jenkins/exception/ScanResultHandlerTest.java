package com.rapid7.insightappsec.intg.jenkins.exception;

import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStepAction;
import com.rapid7.insightappsec.intg.jenkins.ScanResultHandler;
import com.rapid7.insightappsec.intg.jenkins.ScanResults;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import hudson.model.Run;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import static com.rapid7.insightappsec.intg.jenkins.api.vulnerability.VulnerabilityModels.aCompleteVulnerability;
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

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private ScanResultHandler scanResultHandler;

    @Test
    public void handleScanResult_vulnerabilityResultsIndicator_vulnerabilitiesPresent() {
        // given
        ScanResults scanResults = ScanResults.builder()
                                             .vulnerabilities(singletonList(aCompleteVulnerability().build()))
                                             .build();

        exception.expect(VulnerabilitySearchException.class);
        exception.expectMessage("Non-filtered vulnerabilities were found");

        // when
        scanResultHandler.handleScanResults(run,
                                            logger,
                                            BuildAdvanceIndicator.VULNERABILITY_RESULTS,
                                            scanResults,
                                            true);
    }

    @Test
    public void handleScanResult_vulnerabilityResultsIndicator_noVulnerabilitiesPresent() {
        // given
        ScanResults scanResults = ScanResults.builder().build();

        // when
        scanResultHandler.handleScanResults(run,
                                            logger,
                                            BuildAdvanceIndicator.VULNERABILITY_RESULTS,
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
                                            BuildAdvanceIndicator.VULNERABILITY_RESULTS,
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