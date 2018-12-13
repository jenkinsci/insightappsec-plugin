package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.InsightAppSecLogger;
import com.rapid7.insightappsec.intg.jenkins.api.scan.Scan;
import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchApi;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchRequest;
import com.rapid7.insightappsec.intg.jenkins.api.search.SearchResult;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanFailureException;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanRetrievalFailedException;
import com.rapid7.insightappsec.intg.jenkins.exception.ScanSubmissionFailedException;
import com.rapid7.insightappsec.intg.jenkins.exception.VulnerabilitySearchException;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.mutable.MutableInt;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;

import java.io.IOException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

import static com.rapid7.insightappsec.intg.jenkins.MappingConfiguration.OBJECT_MAPPER_INSTANCE;
import static java.lang.String.format;

public class InsightAppSecScanStepRunner {

    private final ScanApi scanApi;
    private final SearchApi searchApi;

    private final ThreadHelper threadHelper;
    private final InsightAppSecLogger logger;

    InsightAppSecScanStepRunner(ScanApi scanApi,
                                SearchApi searchApi,
                                ThreadHelper threadHelper,
                                InsightAppSecLogger logger) {
        this.scanApi = scanApi;
        this.searchApi = searchApi;
        this.threadHelper = threadHelper;
        this.logger = logger;
    }

    public void run(String scanConfigId,
                    BuildAdvanceIndicator buildAdvanceIndicator,
                    Optional<String> vulnerabilityQuery) throws InterruptedException {
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
                SearchResult result = searchVulnerabilities(scanId, vulnerabilityQuery);
                handleVulnerabilityResult(result);
                break;
        }
    }

    private void handleVulnerabilityResult(SearchResult result) {
        if (result.getMetadata().getTotalData() != 0) {
            // TODO: collect all the vulns
            // TODO: persist all the vulns

            logger.log(String.format("Failing build due to %s non-filtered vulnerabilities", result.getMetadata().getTotalData()));

            throw new VulnerabilitySearchException("Non-filtered vulnerabilities were found");
        }
    }

    private void blockUntilStatus(String scanId,
                                  Scan.ScanStatus desiredStatus) throws InterruptedException {
        logger.log("Beginning polling for scan with id: %s", scanId);

        int pollIntervalSeconds = 15;
        int failureThreshold = 20; // let fail up to 20 times, i.e. 5 minutes of failed polling = failed build
        MutableInt failedCount = new MutableInt(0);

        // perform initial poll and log / cache initial status
        Optional<Scan> scanOpt = tryGetScan(scanId, failureThreshold, failedCount);
        Optional<Scan.ScanStatus> cachedStatusOpt = Optional.empty();

        if (scanOpt.isPresent()) {
            cachedStatusOpt = Optional.of(scanOpt.get().getStatus());
            logger.log("Scan status: %s", cachedStatusOpt.get());
        }

        while (true) {

            if (scanOpt.isPresent()) {
                // failed to set cached status on initial poll, set here in this case
                if (!cachedStatusOpt.isPresent()) {
                    cachedStatusOpt = Optional.of(scanOpt.get().getStatus());
                }

                // log and update cached status upon change
                if (!cachedStatusOpt.get().equals(scanOpt.get().getStatus())) {
                    logger.log("Scan status has been updated from %s to %s", cachedStatusOpt.get(),
                                                                                      scanOpt.get().getStatus());
                    cachedStatusOpt = Optional.of(scanOpt.get().getStatus());
                }

                if (scanOpt.get().getStatus().equals(Scan.ScanStatus.CANCELING) ||
                    scanOpt.get().getStatus().equals(Scan.ScanStatus.FAILED)) {
                    logger.log("Failing build due to scan status: %s", scanOpt.get().getStatus());

                    throw new ScanFailureException(String.format("Scan has failed. Status: %s", scanOpt.get().getStatus()));
                }

                // log and exit upon reaching desired state
                if (scanOpt.get().getStatus().equals(desiredStatus)) {
                    logger.log("Desired scan status has been reached");
                    break;
                }
            }

            threadHelper.sleep(TimeUnit.SECONDS.toMillis(pollIntervalSeconds));
            scanOpt = tryGetScan(scanId, failureThreshold, failedCount);
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

        } catch (IOException e) {
            throw new ScanSubmissionFailedException("Error occurred submitting scan", e);
        }
    }

    private Optional<Scan> tryGetScan(String scanId,
                                      int failureThreshold,
                                      MutableInt failedCount) {
        try {
            Scan scan = getScan(scanId);

            failedCount.setValue(0); // reset the failure count

            return Optional.of(scan);
        } catch (Exception e) {
            failedCount.add(1);

            if (failedCount.toInteger() > failureThreshold) {
                throw new RuntimeException(String.format("Scan polling has failed %s times, aborting", failedCount.toString()), e);
            } else {
                return Optional.empty();
            }
        }
    }

    private Scan getScan(String scanId) {
        try {
            HttpResponse response = scanApi.getScan(scanId);

            if (response.getStatusLine().getStatusCode() == 200) {
                String content = IOUtils.toString(response.getEntity().getContent());

                return OBJECT_MAPPER_INSTANCE.readValue(content, Scan.class);
            } else {
                throw new ScanRetrievalFailedException(format("Error occurred retrieving scan with id %s. Response %n %s", scanId, response));
            }

        } catch (IOException e) {
            throw new ScanRetrievalFailedException(format("Error occurred retrieving scan with id %s", scanId), e);
        }
    }

    private SearchResult searchVulnerabilities(String scanId,
                                               Optional<String> vulnerabilityQuery) {
        SearchRequest searchRequest = vulnSearchRequest(scanId, vulnerabilityQuery);
        logger.log("Searching for vulnerabilities using query [%s]", searchRequest.getQuery());

        try {
            HttpResponse response = searchApi.search(searchRequest);

            if (response.getStatusLine().getStatusCode() == 200) {
                String content = IOUtils.toString(response.getEntity().getContent());

                return OBJECT_MAPPER_INSTANCE.readValue(content, SearchResult.class);
            } else {
                throw new VulnerabilitySearchException(format("Error occurred retrieving vulnerabilities for query [%s]. Response %n %s", searchRequest.getQuery(), response));
            }
        } catch (IOException e) {
            throw new VulnerabilitySearchException(String.format("Error occurred retrieving vulnerabilities for query [%s]", searchRequest.getQuery()), e);
        }
    }

    private SearchRequest vulnSearchRequest(String scanId,
                                            Optional<String> vulnerabilityQuery) {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("vulnerability.scans.id='%s'", scanId));

        vulnerabilityQuery.ifPresent(s -> sb.append(String.format(" && %s", s)));

        return new SearchRequest(SearchRequest.SearchType.VULNERABILITY, sb.toString());
    }

}
