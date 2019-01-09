package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.Identifiable;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanExecutionDetails;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequest;
import com.rapid7.insightappsec.intg.jenkins.api.vulnerability.Vulnerability;
import com.rapid7.insightappsec.intg.jenkins.exception.APIException;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanFailureException;
import org.apache.commons.lang.StringUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus.CANCELING;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus.COMPLETE;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus.FAILED;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus.PENDING;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.Scan.ScanStatus.RUNNING;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.ScanExecutionDetailsModels.aCompleteScanExecutionDetails;
import static com.rapid7.insightappsec.intg.jenkins.api.scan.ScanModels.aScan;
import static com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequestModels.aVulnerabilitySearchRequest;
import static com.rapid7.insightappsec.intg.jenkins.api.vulnerability.VulnerabilityModels.aCompleteVulnerability;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class InsightAppSecScanStepRunnerTest {

    @Mock
    private ScanApi scanApi;

    @Mock
    private SearchApi searchApi;

    @Mock
    private InsightAppSecLogger logger;

    @Mock
    private ThreadHelper threadHelper;

    @Mock
    private ScanDurationHandler scanDurationHandler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private InsightAppSecScanStepRunner runner;

    private String scanConfigId = UUID.randomUUID().toString();
    private String scanId = UUID.randomUUID().toString();

    private Scan.ScanBuilder scanBuilder = aScan().scanConfig(new Identifiable(scanConfigId));

    // ADVANCE ON SUBMISSION

    @Test
    public void run_advanceWhenSubmitted() throws InterruptedException {
        // given
        mockSubmitScan();

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_SUBMITTED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        assertFalse(results.isPresent());
    }

    // ADVANCE ON START

    @Test
    public void run_advanceWhenStarted() throws InterruptedException {
        // given
        mockSubmitScan();
        
        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenReturn(scanBuilder.status(RUNNING).build());

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_STARTED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(1)).sleep(TimeUnit.SECONDS.toMillis(15));

        assertFalse(results.isPresent());
    }

    // ADVANCE ON COMPLETE

    @Test
    public void run_advanceWhenCompleted() throws InterruptedException {
        // given
        mockSubmitScan();
        
        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenReturn(scanBuilder.status(COMPLETE).build());

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities();
        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(2)).sleep(TimeUnit.SECONDS.toMillis(15));

        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWhenCompleted_scanFailingStatus_canceling() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenReturn(scanBuilder.status(CANCELING).build());

        exception.expect(ScanFailureException.class);
        exception.expectMessage(String.format("Scan has failed. Status: %s", CANCELING));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        // expected exception
    }

    @Test
    public void run_advanceWhenCompleted_scanFailingStatus_failed() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenReturn(scanBuilder.status(FAILED).build());

        exception.expect(ScanFailureException.class);
        exception.expectMessage(String.format("Scan has failed. Status: %s", FAILED));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        // expected exception
    }

    /**
     * Ensures that throwing an exception on initial poll does not break the application.
     * Ensures the logging tweak that occurs when initial poll fails, i.e can't log initial status.
     */
    @Test
    public void run_advanceWhenCompleted_initialPollFails() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenReturn(scanBuilder.status(COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(2)).sleep(TimeUnit.SECONDS.toMillis(15));
    }

    /**
     * Ensures that throwing an exception on first subsequent poll does not break the application.
     */
    @Test
    public void run_advanceWhenCompleted_firstSubsequentPollFails() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenReturn(scanBuilder.status(COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(3)).sleep(TimeUnit.SECONDS.toMillis(15));
    }

    /**
     * Ensure an exception is thrown when total failures in sequence are greater than failure threshold.
     * Scenario:
     * - First 21 polls fail
     */
    @Test
    public void run_advanceWhenCompleted_subsequentPollsFailAboveThreshold() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException());

        exception.expect(RuntimeException.class);
        exception.expectMessage("Scan polling has failed 21 times, aborting");

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        // expected exception
    }

    /**
     * Ensure that a successful poll will reset the total failure count.
     * Scenario:
     * - 20 polls fail
     * - Then success
     *  - Then next 2 polls fail
     */
    @Test
    public void run_advanceWhenSubmitted_successResetsFailureCount() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(PENDING).build())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(RUNNING).build())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(24)).sleep(TimeUnit.SECONDS.toMillis(15));
    }

    // ADVANCE ON VULNERABILITY QUERY

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_zeroResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_someResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 10);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_zeroResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_someResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 10);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    // TEST HELPERS

    private void mockSubmitScan() {
        given(scanApi.submitScan(scanConfigId)).willReturn(scanId);
    }

    private ScanExecutionDetails mockGetScanExecutionDetails() {
        ScanExecutionDetails details = aCompleteScanExecutionDetails().build();

        when(scanApi.getScanExecutionDetails(scanId)).thenReturn(details);

        return details;
    }

    private List<Vulnerability> mockGetVulnerabilities() {
        return mockGetVulnerabilities(null, 10);
    }

    private List<Vulnerability> mockGetVulnerabilities(String query,
                                                       int size) {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("vulnerability.scans.id='%s'", scanId));

        if (!StringUtils.isEmpty(query)) {
            sb.append(String.format(" && %s", query));
        }

        SearchRequest searchRequest = aVulnerabilitySearchRequest().query(sb.toString()).build();

        List<Vulnerability> vulnerabilities = Stream.generate(() -> aCompleteVulnerability().build()).limit(size).collect(toList());
        when(searchApi.searchAll(searchRequest, Vulnerability.class)).thenReturn(vulnerabilities);

        return vulnerabilities;
    }

}