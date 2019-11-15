# Description

The InsightAppSec Jenkins plugin provides an easy way to integrate your build process with the InsightAppSec REST API. Using this plugin, Jenkins can automatically run a scan against your web apps and make a decision about the pass/fail status of the build based on the scan result. This decision can be made by using different rules, for example, the maximum number of vulnerabilities found or thresholds of vulnerability severity.

# Key Features

* Trigger scans from Jenkins
* Pass or fail builds based on scan results

# Requirements

* A user account on the Insight Platform with `Read Write` or `Admin` access.
* Access to InsightAppSec. Note that free trial users are unable to start a scan via the API and so may not use the plugin.

# Documentation

## Setup

The plugin may be installed using the plugin manager. For more information see https://plugins.jenkins.io/insightappsec

Additionally the plugin can be installed by manually building the hpi file and uploading to your Jenkins installation.

## Technical Details

### Development

To run the plugin locally, cd to the root directory and invoke:

```
mvn hpi:run
```

When the output shows INFO: Jenkins is fully up and running navigate to http://localhost:8080/jenkins/ and you will see the sandbox Jenkins homepage.

### Contributing

Rapid7 welcomes contributions to the InsightAppSec Jenkins Plugin and has designated its repository as open source. For a full guide on configuring a development environment, as well as deploying, packaging, and testing the plugin, please refer to the [project README](https://github.com/jenkinsci/insightappsec-plugin/blob/master/README.md).

## Troubleshooting

_This plugin does not contain any troubleshooting information._

# Version History

* 1.0.0 - Initial integration

# Links

## References

* [InsightAppSec API documentation](https://help.rapid7.com/insightappsec/en-us/api/v1/docs.html)
* [Jenkins plugin tutorial](https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial)