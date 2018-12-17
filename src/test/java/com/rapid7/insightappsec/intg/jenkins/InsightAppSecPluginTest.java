package com.rapid7.insightappsec.intg.jenkins;

import com.google.common.base.Joiner;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

import static com.rapid7.insightappsec.intg.jenkins.InsightAppSecScanStepModels.aCompleteInsightAppSecPlugin;

public class InsightAppSecPluginTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void shouldStoreConfigurationForRecall() throws Exception {
        String[] keysToTest = {
                "scanConfigId",
                "buildAdvanceIndicator",
                "vulnerabilityQuery",
                "region"
        };

        FreeStyleProject p = jenkins.getInstance().createProject(FreeStyleProject.class, "testProject");
        InsightAppSecPlugin before = aCompleteInsightAppSecPlugin().build();

        p.getBuildersList().add(before);

        jenkins.submit(jenkins.createWebClient().getPage(p,"configure").getFormByName("config"));

        InsightAppSecPlugin after = p.getBuildersList().get(InsightAppSecPlugin.class);

        jenkins.assertEqualBeans(before, after, Joiner.on(',').join(keysToTest));
    }

}