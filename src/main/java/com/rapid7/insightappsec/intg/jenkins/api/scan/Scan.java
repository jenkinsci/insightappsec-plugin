package com.rapid7.insightappsec.intg.jenkins.api.scan;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.rapid7.insightappsec.intg.jenkins.api.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Arrays;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
@Builder
public class Scan {

    private Identifiable scanConfig;

    private ScanStatus status;

    public Scan(Identifiable scanConfig) {
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
        FAILED,

        UNKNOWN;

        @JsonCreator
        public static ScanStatus fromString(String value) {
            return Arrays.stream(ScanStatus.values())
                         .filter(e -> e.name().equalsIgnoreCase(value))
                         .findAny()
                         .orElse(UNKNOWN);
        }

    }

}
