package org.jenkinsci.plugins.insightappsec.api.scan;

import com.fasterxml.jackson.annotation.JsonCreator;
import org.jenkinsci.plugins.insightappsec.api.Identifiable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Arrays;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Scan {

    private Identifiable scanConfig;

    private ScanStatus status;

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
        AUTHENTICATING,
        FAILED,
        AWAITING_AUTHENTICATION,
        AUTHENTICATED,

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
