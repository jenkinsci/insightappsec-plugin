package com.rapid7.insightappsec.intg.jenkins.credentials;

import com.cloudbees.plugins.credentials.CredentialsMatchers;
import com.cloudbees.plugins.credentials.CredentialsProvider;
import hudson.security.ACL;
import jenkins.model.Jenkins;

import java.util.Collections;
import java.util.List;

public class InsightCredentialsHelper {

    public static List<InsightAPICredentials> lookupAllInsightCredentials(Jenkins context) {
        return CredentialsProvider.lookupCredentials(InsightAPICredentials.class,
                                                     context,
                                                     ACL.SYSTEM,
                                                     Collections.emptyList());
    }

    public static InsightAPICredentials lookupInsightCredentialsById(String credentialsId) {
        InsightAPICredentials credentials = CredentialsMatchers.firstOrNull(lookupAllInsightCredentials(Jenkins.getInstance()),
                                                                            CredentialsMatchers.withId(credentialsId));
        if (credentials == null) {
            throw new IllegalStateException(String.format("Insight credentials not found for ID %s", credentialsId));
        }

        return credentials;
    }

}
