package io.jenkins.plugins.insightappsec.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;

import edu.umd.cs.findbugs.annotations.NonNull;

public class InsightAPICredentialsNameProvider extends CredentialsNameProvider<InsightAPICredentialsImpl> {

    @NonNull
    @Override
    public String getName(@NonNull InsightAPICredentialsImpl insightCredentials) {
        return insightCredentials.getId();
    }
}
