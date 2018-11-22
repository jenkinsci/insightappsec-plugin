package com.rapid7.insightappsec.intg.jenkins.api.scan;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.rapid7.insightappsec.intg.jenkins.api.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Scan {

    @JsonProperty("scan_config")
    private Id scanConfig;

    private ScanStatus status;

    public Scan(Id scanConfig) {
        this.scanConfig = scanConfig;
    }

    public enum ScanStatus {

        PENDING,
        RUNNING,
        SCANNED,
        PROCESSED,
        COMPLETE,
        PAUSED,
        BLACKED_OUT,
        PAUSING,
        RESUMING,
        STOPPING,
        CANCELING,
        FAILED

    }
}
