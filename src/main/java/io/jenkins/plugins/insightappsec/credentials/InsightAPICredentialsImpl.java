package io.jenkins.plugins.insightappsec.credentials;

import com.cloudbees.plugins.credentials.CredentialsDescriptor;
import com.cloudbees.plugins.credentials.NameWith;
import com.cloudbees.plugins.credentials.impl.BaseStandardCredentials;
import hudson.Extension;
import hudson.util.Secret;
import org.kohsuke.stapler.DataBoundConstructor;

import edu.umd.cs.findbugs.annotations.CheckForNull;
import edu.umd.cs.findbugs.annotations.NonNull;

@NameWith(value = InsightAPICredentialsNameProvider.class)
public class InsightAPICredentialsImpl extends BaseStandardCredentials implements InsightAPICredentials {

    @NonNull
    private final Secret apiKey;

    @DataBoundConstructor
    public InsightAPICredentialsImpl(@CheckForNull String id,
                                     @CheckForNull String apiKey) {
        super(id, null);
        this.apiKey = Secret.fromString(apiKey);
    }

    public Secret getApiKey() {
        return this.apiKey;
    }

    @Extension
    public static class Descriptor extends CredentialsDescriptor {

        @Override
        public String getDisplayName() {
            return "Insight API Key";
        }
    }
}
