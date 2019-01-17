package org.jenkinsci.plugins.insightappsec;

import org.jenkinsci.plugins.insightappsec.api.scan.ScanExecutionDetails;
import org.jenkinsci.plugins.insightappsec.api.vulnerability.Vulnerability;
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
