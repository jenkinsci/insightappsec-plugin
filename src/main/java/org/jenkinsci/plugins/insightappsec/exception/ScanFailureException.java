package org.jenkinsci.plugins.insightappsec.exception;

import org.jenkinsci.plugins.insightappsec.api.scan.Scan;

public class ScanFailureException extends RuntimeException {

    private static final long serialVersionUID = -9011530665026990784L;

    public ScanFailureException(Scan.ScanStatus scanStatus) {
        super(String.format("Scan has failed. Status: %s", scanStatus));
    }

}
