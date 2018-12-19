package com.rapid7.insightappsec.intg.jenkins;

import hudson.model.Run;
import jenkins.model.RunAction2;

public class InsightAppSecScanStepAction implements RunAction2 {

    private static final String ICON_FILE_NAME = "clipboard.png";
    private static final String URL_NAME = "ias-scan-results";

    private transient Run run;

    private ScanResults scanResults;

    public InsightAppSecScanStepAction(ScanResults scanResults) {
        this.scanResults = scanResults;
    }

    public ScanResults getScanResults() {
        return scanResults;
    }

    @Override
    public void onAttached(Run<?, ?> run) {
        this.run = run;
    }

    @Override
    public void onLoad(Run<?, ?> run) {
        this.run = run;
    }

    public Run getRun() {
        return run;
    }

    @Override
    public String getIconFileName() {
        return ICON_FILE_NAME;
    }

    @Override
    public String getDisplayName() {
        return Messages.actions_scanResults();
    }

    @Override
    public String getUrlName() {
        return URL_NAME;
    }
}