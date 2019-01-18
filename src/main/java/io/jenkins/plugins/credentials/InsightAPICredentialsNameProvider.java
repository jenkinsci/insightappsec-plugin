package io.jenkins.plugins.credentials;

import com.cloudbees.plugins.credentials.CredentialsNameProvider;

import javax.annotation.Nonnull;

public class InsightAPICredentialsNameProvider extends CredentialsNameProvider<InsightAPICredentialsImpl> {

    @Nonnull
    @Override
    public String getName(@Nonnull InsightAPICredentialsImpl insightCredentials) {
        return insightCredentials.getId();
    }
}
