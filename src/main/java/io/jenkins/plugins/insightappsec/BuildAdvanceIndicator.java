package io.jenkins.plugins.insightappsec;

import io.jenkins.plugins.insightappsec.exception.UnrecognizedBuildAdvanceIndicatorException;

import java.util.Arrays;

public enum BuildAdvanceIndicator {

    SCAN_SUBMITTED(Messages.selectors_scanSubmitted()),
    SCAN_STARTED(Messages.selectors_scanStarted()),
    SCAN_COMPLETED(Messages.selectors_scanCompleted()),
    VULNERABILITY_QUERY(Messages.selectors_vulnerabilityQuery());

    String displayName;

    BuildAdvanceIndicator(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    static BuildAdvanceIndicator fromString(String value) {
        return Arrays.stream(BuildAdvanceIndicator.values())
                     .filter(e -> e.name().equalsIgnoreCase(value))
                     .findAny()
                     .orElseThrow(() -> new UnrecognizedBuildAdvanceIndicatorException(value));
    }

}
