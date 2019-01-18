package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.api.scan.ScanExecutionDetails;
import io.jenkins.plugins.insightappsec.api.vulnerability.Vulnerability;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScanResults {

    private List<Vulnerability> vulnerabilities;
    private ScanExecutionDetails scanExecutionDetails;

}
