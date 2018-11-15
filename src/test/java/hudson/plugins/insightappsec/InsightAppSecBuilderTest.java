package hudson.plugins.insightappsec;

import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

public class InsightAppSecBuilderTest {

    @Rule
    public JenkinsRule jenkins = new JenkinsRule();

    @Test
    public void testConfigRoundTrip() throws Exception {
        // given
        FreeStyleProject project = jenkins.createFreeStyleProject();
        project.getBuildersList().add(new InsightAppSecBuilder());

        // when
        project = jenkins.configRoundtrip(project);

        // then
        jenkins.assertEqualDataBoundBeans(new InsightAppSecBuilder(), project.getBuildersList().get(0));
    }

    @Test
    public void testBuild() throws Exception {
        // given
        FreeStyleProject project = jenkins.createFreeStyleProject();
        InsightAppSecBuilder builder = new InsightAppSecBuilder();
        project.getBuildersList().add(builder);

        // when
        FreeStyleBuild build = jenkins.buildAndAssertSuccess(project);

        // then
        jenkins.assertLogContains("InsightAppSec step executed", build);
    }

}