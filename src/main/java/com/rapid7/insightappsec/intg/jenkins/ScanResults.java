package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.api.scan.ScanExecutionDetails;
import com.rapid7.insightappsec.intg.jenkins.api.vulnerability.Vulnerability;
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
