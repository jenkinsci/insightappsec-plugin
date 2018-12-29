package com.rapid7.insightappsec.intg.jenkins;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStep.BuildAdvanceIndicator;
import com.rapid7.insightappsec.intg.jenkins.api.Identifiable;
import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanExecutionDetails;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequest;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchResult;
import com.rapid7.insightappsec.intg.jenkins.api.vulnerability.Vulnerability;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanFailureException;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanAPIFailureException;
import com.rapid7.insightappsec.intg.jenkins.exception.VulnerabilitySearchException;
import com.rapid7.insightappsec.intg.jenkins.mock.MockHttpResponse;
import org.apache.commons.lang.StringUtils;
import org.apache.http.Header;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.message.BasicHeader;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.IOException;
import java.util.ArrayList;
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
import static com.rapid7.insightappsec.intg.jenkins.api.search.SearchResultModels.aMetadata;
import static com.rapid7.insightappsec.intg.jenkins.api.search.SearchResultModels.aSearchResult;
import static com.rapid7.insightappsec.intg.jenkins.api.vulnerability.VulnerabilityModels.aCompleteVulnerability;
import static java.lang.String.format;
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
    private WaitTimeHandler waitTimeHandler;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @InjectMocks
    private InsightAppSecScanStepRunner runner;

    private String scanConfigId = UUID.randomUUID().toString();
    private String scanId = UUID.randomUUID().toString();

    // SCAN SUBMIT

    @Test
    public void run_scanSubmit_non201Response() throws IOException, InterruptedException {
        // given
        HttpResponse response = MockHttpResponse.create(400);

        given(scanApi.submitScan(scanConfigId)).willReturn(response);

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage(format("Error occurred submitting scan. Response %n %s", response));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_SUBMITTED, null);

        // then
        // exception expected
    }

    @Test
    public void run_scanSubmit_IOException() throws IOException, InterruptedException {
        // given
        given(scanApi.submitScan(scanConfigId)).willThrow(new IOException());

        exception.expect(ScanAPIFailureException.class);
        exception.expectMessage("Error occurred submitting scan");

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        // exception expected
    }

    // ADVANCE ON SUBMISSION

    @Test
    public void run_advanceWhenSubmitted() throws IOException, InterruptedException {
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
    public void run_advanceWhenStarted() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenReturn(aGetScanResponse(RUNNING));

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_STARTED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Using build advance indicator: '%s'", BuildAdvanceIndicator.SCAN_STARTED.getDisplayName());
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(1)).sleep(TimeUnit.SECONDS.toMillis(15));

        assertFalse(results.isPresent());
    }

    // ADVANCE ON COMPLETE

    @Test
    public void run_advanceWhenCompleted() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenReturn(aGetScanResponse(COMPLETE));

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities();
        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Using build advance indicator: '%s'", BuildAdvanceIndicator.SCAN_COMPLETED.getDisplayName());
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
    public void run_advanceWhenCompleted_scanFailingStatus_canceling() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenReturn(aGetScanResponse(CANCELING));

        exception.expect(ScanFailureException.class);
        exception.expectMessage(String.format("Scan has failed. Status: %s", CANCELING));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        // expected exception
    }

    @Test
    public void run_advanceWhenCompleted_scanFailingStatus_failed() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenReturn(aGetScanResponse(FAILED));

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
    public void run_advanceWhenCompleted_initialPollFails() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenThrow(new IOException())
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenReturn(aGetScanResponse(COMPLETE));

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Using build advance indicator: '%s'", BuildAdvanceIndicator.SCAN_COMPLETED.getDisplayName());
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(2)).sleep(TimeUnit.SECONDS.toMillis(15));
    }

    /**
     * Ensures that throwing an exception on first subsequent poll does not break the application.
     */
    @Test
    public void run_advanceWhenCompleted_firstSubsequentPollFails() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenThrow(new IOException())
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenReturn(aGetScanResponse(COMPLETE));

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Using build advance indicator: '%s'", BuildAdvanceIndicator.SCAN_COMPLETED.getDisplayName());
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
    public void run_advanceWhenCompleted_subsequentPollsFailAboveThreshold() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException());

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
    public void run_advanceWhenSubmitted_successResetsFailureCount() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(PENDING))
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenReturn(aGetScanResponse(RUNNING))
                                     .thenThrow(new IOException())
                                     .thenThrow(new IOException())
                                     .thenReturn(aGetScanResponse(COMPLETE));

        mockGetVulnerabilities();
        mockGetScanExecutionDetails();

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.SCAN_COMPLETED, null);

        // then
        verify(logger, times(1)).log("Scan submitted successfully");
        verify(logger, times(1)).log("Using build advance indicator: '%s'", BuildAdvanceIndicator.SCAN_COMPLETED.getDisplayName());
        verify(logger, times(1)).log("Beginning polling for scan with id: %s", scanId);
        verify(logger, times(1)).log("Scan status: %s", PENDING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", PENDING, RUNNING);
        verify(logger, times(1)).log("Scan status has been updated from %s to %s", RUNNING, COMPLETE);
        verify(logger, times(1)).log("Desired scan status has been reached");

        verify(threadHelper, times(24)).sleep(TimeUnit.SECONDS.toMillis(15));
    }

    // ADVANCE ON VULNERABILITY QUERY

    @Test
    public void run_advanceWithVulnerabilityQuery_non200() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        SearchRequest searchRequest = aVulnerabilitySearchRequest().query(String.format("vulnerability.scans.id='%s'", scanId)).build();
        HttpResponse response = MockHttpResponse.create(422);
        when(searchApi.search(searchRequest, 0)).thenReturn(response);

        exception.expect(VulnerabilitySearchException.class);
        exception.expectMessage(format("Error occurred retrieving vulnerabilities for query [%s]. Response %n %s", searchRequest.getQuery(), response));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        // expected exception
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_IOException() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        SearchRequest searchRequest = aVulnerabilitySearchRequest().query(String.format("vulnerability.scans.id='%s'", scanId)).build();
        when(searchApi.search(searchRequest, 0)).thenThrow(new IOException());

        exception.expect(VulnerabilitySearchException.class);
        exception.expectMessage(format("Error occurred retrieving vulnerabilities for query [%s]", searchRequest.getQuery()));

        // when
        runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        // expected exception
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_zeroResults() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 0, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_emptyQuery_someResults() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(null, 10, 3);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, null);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_zeroResults() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 0, 0);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }

    @Test
    public void run_advanceWithVulnerabilityQuery_queryPresent_someResults() throws IOException, InterruptedException {
        // given
        mockSubmitScan();

        when(scanApi.getScan(scanId)).thenReturn(aGetScanResponse(COMPLETE));

        ScanExecutionDetails scanExecutionDetails = mockGetScanExecutionDetails();
        String vulnerabilityQuery = "vulnerability.severity='HIGH'";

        List<Vulnerability> vulnerabilities = mockGetVulnerabilities(vulnerabilityQuery, 10, 3);

        // when
        Optional<ScanResults> results = runner.run(scanConfigId, BuildAdvanceIndicator.VULNERABILITY_RESULTS, vulnerabilityQuery);

        // then
        assertTrue(results.isPresent());
        assertEquals(results.get().getScanExecutionDetails(), scanExecutionDetails);
        assertEquals(results.get().getVulnerabilities(), vulnerabilities);
    }
    
    // TEST HELPERS

    private Header[] mockHeaders(String scanId) {
        Header[] headers = new Header[1];

        headers[0] = new BasicHeader(HttpHeaders.LOCATION, "http://test.com/" + scanId);

        return headers;
    }

    private void mockSubmitScan() throws IOException {
        HttpResponse response = MockHttpResponse.create(201, mockHeaders(scanId));
        given(scanApi.submitScan(scanConfigId)).willReturn(response);
    }

    private ScanExecutionDetails mockGetScanExecutionDetails() throws IOException {
        ScanExecutionDetails details = aCompleteScanExecutionDetails().build();

        HttpResponse response = MockHttpResponse.create(200, details);
        when(scanApi.getScanExecutionDetails(scanId)).thenReturn(response);

        return details;
    }

    private List<Vulnerability> mockGetVulnerabilities() throws IOException {
        return mockGetVulnerabilities(null, 10, 3);
    }

    private List<Vulnerability> mockGetVulnerabilities(String query,
                                                       int pageSize,
                                                       int totalPages) throws IOException {
        int index = 0;

        StringBuilder sb = new StringBuilder();
        sb.append(String.format("vulnerability.scans.id='%s'", scanId));

        if (!StringUtils.isEmpty(query)) {
            sb.append(String.format(" && %s", query));
        }

        SearchRequest searchRequest = aVulnerabilitySearchRequest().query(sb.toString()).build();

        SearchResult<Vulnerability> searchResult = aVulnerabilitySearchResult(index, totalPages, pageSize);

        List<Vulnerability> vulnerabilities = new ArrayList<>(searchResult.getData());
        when(searchApi.search(searchRequest, index)).thenReturn(MockHttpResponse.create(200, searchResult));

        index++;
        while(index < totalPages) {
            index++;

            searchResult = aVulnerabilitySearchResult(index, totalPages, pageSize);

            vulnerabilities.addAll(searchResult.getData());
            when(searchApi.search(searchRequest, index)).thenReturn(MockHttpResponse.create(200, searchResult));
        }

        return vulnerabilities;
    }

    private SearchResult<Vulnerability> aVulnerabilitySearchResult(int index,
                                                                   int totalPages,
                                                                   int pageSize) {
        return aSearchResult().metadata(aMetadata().totalPages(totalPages)
                                                   .index(index)
                                                   .build())
                                                   .data(Stream.generate(() -> aCompleteVulnerability().build())
                                                           .limit(pageSize)
                                                           .collect(toList()))
                                                   .build();
    }

    private HttpResponse aGetScanResponse(Scan.ScanStatus status) throws JsonProcessingException {
        return MockHttpResponse.create(200, aScan().scanConfig(new Identifiable(scanConfigId)).status(status).build());
    }

}