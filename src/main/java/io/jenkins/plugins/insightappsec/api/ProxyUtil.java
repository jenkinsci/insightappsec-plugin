package io.jenkins.plugins.insightappsec.api;

import hudson.ProxyConfiguration;
import jenkins.model.Jenkins;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.HttpClientBuilder;

public class ProxyUtil {

    public static void configureProxy(HttpClientBuilder builder) {
        Jenkins jenkins = Jenkins.getInstance();
        ProxyConfiguration config = jenkins.proxy;

        if (config != null) {
            // connection
            builder.setProxy(getProxyHost(config));

            // authentication
            if (config.getUserName() != null && !config.getUserName().isEmpty()) {
                builder.setDefaultCredentialsProvider(getProxyCredentialsProvider(config));
            }
        }
    }

    private static HttpHost getProxyHost(ProxyConfiguration config) {
        return new HttpHost(config.name, config.port);
    }

    private static CredentialsProvider getProxyCredentialsProvider(ProxyConfiguration config) {
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        final AuthScope authScope = new AuthScope(config.name, config.port);
        final Credentials credentials = new UsernamePasswordCredentials(config.getUserName(), config.getPassword());

        credentialsProvider.setCredentials(authScope, credentials);
        return credentialsProvider;
    }
    
}
