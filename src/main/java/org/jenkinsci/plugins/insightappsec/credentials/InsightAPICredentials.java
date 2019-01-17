package org.jenkinsci.plugins.insightappsec.credentials;

import com.cloudbees.plugins.credentials.common.StandardCredentials;
import hudson.util.Secret;

public interface InsightAPICredentials extends StandardCredentials {

    Secret getApiKey();

}
