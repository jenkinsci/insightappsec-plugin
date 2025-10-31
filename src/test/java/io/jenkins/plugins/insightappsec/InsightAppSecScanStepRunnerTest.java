package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.api.Identifiable;
import io.jenkins.plugins.insightappsec.api.scan.Scan;
import io.jenkins.plugins.insightappsec.api.scan.ScanApi;
import io.jenkins.plugins.insightappsec.api.scan.ScanExecutionDetails;
import io.jenkins.plugins.insightappsec.api.search.SearchApi;
import io.jenkins.plugins.insightappsec.api.search.SearchRequest;
import io.jenkins.plugins.insightappsec.api.vulnerability.Vulnerability;
import io.jenkins.plugins.insightappsec.exception.APIException;
import io.jenkins.plugins.insightappsec.exception.ScanFailureException;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static io.jenkins.plugins.insightappsec.api.scan.ScanExecutionDetailsModels.aCompleteScanExecutionDetails;
import static io.jenkins.plugins.insightappsec.api.scan.ScanModels.aScan;
import static io.jenkins.plugins.insightappsec.api.search.SearchRequestModels.aVulnerabilitySearchRequest;
import static io.jenkins.plugins.insightappsec.api.vulnerability.VulnerabilityModels.aCompleteVulnerability;
import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mockStatic;
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
    private ScanDurationHandler scanDurationHandler;

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
        
        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build());

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_STARTED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", Scan.ScanStatus.PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.PENDING, Scan.ScanStatus.RUNNING);
        verify(logger, times(1)).log("Desired scan status has been reached");


        assertFalse(results.isPresent());
    }

    // ADVANCE ON COMPLETE

    @Test
    public void run_advanceWhenCompleted() throws InterruptedException {
        // given
        mockSubmitScan();
        
        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities();
        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", Scan.ScanStatus.PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.PENDING, Scan.ScanStatus.RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.RUNNING, Scan.ScanStatus.COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");


        assertTrue(results.isPresent());
        Assert.assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWhenCompleted_scanFailingStatus_canceling() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.CANCELING).build());

        // when
        ScanFailureException thrown = Assert.assertThrows(ScanFailureException.class, () ->
            runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null)
        );

        // then
        Assert.assertTrue(thrown.getMessage().contains(String.format("Scan has failed. Status: %s", Scan.ScanStatus.CANCELING)));
    }

    @Test
    public void run_advanceWhenCompleted_scanFailingStatus_failed() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.FAILED).build());

        // when
        ScanFailureException thrown = Assert.assertThrows(ScanFailureException.class, () ->
            runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null)
        );

        // then
        Assert.assertTrue(thrown.getMessage().contains(String.format("Scan has failed. Status: %s", Scan.ScanStatus.FAILED)));
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
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.RUNNING, Scan.ScanStatus.COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

    }

    /**
     * Ensures that throwing an exception on first subsequent poll does not break the application.
     */
    @Test
    public void run_advanceWhenCompleted_firstSubsequentPollFails() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
                                     .thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", Scan.ScanStatus.PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.PENDING, Scan.ScanStatus.RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.RUNNING, Scan.ScanStatus.COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

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

        // when
        RuntimeException thrown = Assert.assertThrows(RuntimeException.class, () ->
            runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null)
        );

        // then
        Assert.assertTrue(thrown.getMessage().contains("Scan polling has failed 21 times, aborting"));
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

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.PENDING).build())
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
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.RUNNING).build())
                                     .thenThrow(new APIException())
                                     .thenThrow(new APIException())
                                     .thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", Scan.ScanStatus.PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.PENDING, Scan.ScanStatus.RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", Scan.ScanStatus.RUNNING, Scan.ScanStatus.COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

    }

    // ADVANCE ON VULNERABILITY QUERY

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_zeroResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_QUERY, null);

        // then
        assertTrue(results.isPresent());
        Assert.assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_someResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 10);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_QUERY, null);

        // then
        assertTrue(results.isPresent());
        Assert.assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_zeroResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_QUERY, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        Assert.assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_someResults() throws InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(scanBuilder.status(Scan.ScanStatus.COMPLETE).build());

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 10);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_QUERY, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        Assert.assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
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
            if(query.startsWith("(") && query.endsWith(")")) {
                sb.append(String.format(" && %s", query));
            }
            else{
                sb.append(String.format(" && (%s)", query));
            }
        }

        SearchRequest searchRequest = aVulnerabilitySearchRequest().query(sb.toString()).build();

        List<Vulnerability> vulnerabilities = Stream.generate(() -> aCompleteVulnerability().build()).limit(size).collect(toList());
        when(searchApi.searchAll(searchRequest, Vulnerability.class)).thenReturn(vulnerabilities);

        return vulnerabilities;
    }

    // Note: Thread.sleep is no longer mocked globally. Tests that poll for scan status
    // will actually sleep briefly. For tests requiring verification, wrap in:
    // try (MockedStatic<Thread> threadMock = mockStatic(Thread.class)) { ... }

}