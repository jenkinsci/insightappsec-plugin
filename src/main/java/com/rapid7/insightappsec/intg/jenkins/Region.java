package com.rapid7.insightappsec.intg.jenkins;

import com.rapid7.insightappsec.intg.jenkins.exception.UnrecognizedRegionException;

import java.util.Arrays;

public enum Region {

    US(Messages.selectors_us()),
    CA(Messages.selectors_ca()),
    EU(Messages.selectors_eu()),
    AU(Messages.selectors_au());

    String displayName;

    Region(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAPIHost() {
        return String.format("%s.api.insight.rapid7.com", this.name().toLowerCase());
    }

    static Region fromString(String value) {
        return Arrays.stream(Region.values())
                     .filter(e -> e.name().equalsIgnoreCase(value))
                     .findAny()
                     .orElseThrow(() -> new UnrecognizedRegionException("The region provided is not recognized"));
    }
}
