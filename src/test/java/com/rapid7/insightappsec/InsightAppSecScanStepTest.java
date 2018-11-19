package com.rapid7.insightappsec;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.rapid7.insightappsec.InsightAppSecScanStepModels.aCompleteInsightAppSecScanStep;

public class InsightAppSecScanStepTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testConfigRoundTrip() throws Exception {
        // given
        FreeStyleProject project = jenkins.createFreeStyleProject();
        InsightAppSecScanStepBuilder scanStep = aCompleteInsightAppSecScanStep();
        project.getBuildersList().add(scanStep.build());

        // when
        project = jenkins.configRoundtrip(project);

        // then
        jenkins.assertEqualDataBoundBeans(scanStep.build(),
                                          project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        // given
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(aCompleteInsightAppSecScanStep().build());

        // when
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        // then
        jenkins.assertLogContains("InsightAppSec step executed", build);
    }

}