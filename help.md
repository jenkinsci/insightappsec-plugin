# Description

The InsightAppSec Jenkins plugin provides an easy way to integrate your build process with the InsightAppSec REST API.
Using this plugin, Jenkins can automatically run a scan against your web apps and make a decision about the pass/fail
status of the build based on the scan result. This decision can be made by using different rules, for example, the
presence of a vulnerability based on a search query.

# Key Features

* Trigger scans from Jenkins
* Pass or fail builds based on scan results

# Requirements

* A user account on the Insight Platform with `Read Write` or `Admin` access.
* Access to InsightAppSec. Note that free trial users are unable to start a scan via the API and so may not use the plugin.

# Documentation

## Setup

To get started, ensure an InsightAppSec API key has been generated and added to `Credential Manager`:

1. From the Jenkins homepage, select "Credentials".
2. Select the desired scope, global is appropriate.
3. Click Add Credentials.
4. Select "Insight API Key" as the kind, then provide:
    1. Name (A friendly name to refer to these credentials. For example, "Bob's API Key")
    2. Insight API Key (the API Key to connect to the Insight Platform). Instructions for creating an API Key can be found here.

## Technical Details

Additional documentation for installing and configuring the Jenkins plugin can be found on the [Rapid7 help pages](https://insightappsec.help.rapid7.com/docs/jenkins-integration).

### Installation

The simplest and most common way of installing the InsightAppSec plugin is through Jenkin's Manage Plugins functionality:

1. From the Jenkins homepage, select "Manage Jenkins"
2. Select "Manage Plugins"; only available to Jenkins administrators
3. Under the "Available" tab, search for `InsightAppSec` and select the checkbox to select its
4. Select "Install without restart"

Additionally, the plugin can be installed by manually building the hpi file and uploading to your Jenkins installation.
For more information, see https://plugins.jenkins.io/insightappsec

### Contributing

Rapid7 welcomes contributions to the InsightAppSec Jenkins Plugin and has designated its repository as open source. For
a full guide on configuring a development environment, as well as deploying, packaging, and testing the plugin, please
refer to the [project README](https://github.com/jenkinsci/insightappsec-plugin/blob/master/README.md).

## Troubleshooting

_This plugin does not contain any troubleshooting information._

# Version History

### 1.0.1
- Add support for proxy configuration

### 1.0.0 
- Initial integration

# Links

## References

* [InsightAppSec API documentation](https://help.rapid7.com/insightappsec/en-us/api/v1/docs.html)
* [Create a Rapid7 Platform API Key](https://insightappsec.help.rapid7.com/docs/get-started-with-the-insightappsec-api)
