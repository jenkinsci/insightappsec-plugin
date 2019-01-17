# InsightAppSec Jenkins Plugin

Welcome to the InsightAppSec Jenkins Plugin. 

More information about InsightAppSec can be found here: https://www.rapid7.com/products/insightappsec/

## Prerequisites
To use the plugin you will need
- A user account on the Insight Platform with `Read Write` or `Admin` access.
    - Note that free trial users are unable to start a scan via the API and so may not use the plugin.
- Access to InsightAppSec.

## Installation
The plugin may be installed using the jenkins plugin marketplace.
Additionally the plugin can be installed by manually building the `hpi` file and uploading to your Jenkins installation.

## Usage

### Freestyle Project
The plugin may be used as a build step of a freestyle project. 

To enabled the plugin: 
- Using the `Add build step` dropdown of a freestyle project, select `Scan using InsightAppSec`.


You will then be presented with the plugin configuration pane. The configuration options are as follows:
- **Data Storage Region** [required]
   - The data storage region of the target InsightAppSec instance.
   
- **Insight API Key** [required]
   - The Insight API Key you wish to use for scanning.
   - More info on Jenkins managed Insight API Keys can be found below.
   
- **App** [required]
   - The app containing the Scan Config you wish to scan.
   
   - Provided the region and api key are compatible, a list of Apps that the api key has access to will pre-populate in the drop-down.
   
      - This may take some seconds to load.
   
- **Scan Config** [required]
   - The existing Scan Config you wish to scan.
   
   - Provided the region and api key are compatible, as well as having previously selected an App, the Scan Configs belonging to the selected App will pre-populate in the drop-down.
   
      - This may take some seconds to load.
      
- **Advance build when** [required]
   - This configuration option can be used to augment how the build advances based on the status of the scan submitted.
   
   - There are four options to choose from:
      - **Scan has been submitted** 
         - Advance the build when the scan has been _submitted_ successfully.
      - **Scan has been started**
         - Advance the build when the scan has been _started_ successfully.
      - **Scan has been completed** 
         - Advance the build when the scan has been _completed_ successfully.
      - **Vulnerability results query has returned no vulnerabilities**
         - Advance the build when the scan has been _completed_ _and_ the vulnerability search query has returned _no vulnerabilities_.
         
- **Vulnerability query** [optional]
   - An InsightAppSec search query may be supplied to search vulnerabilities found by the scan.
   
   - For example, if you wish to fail the build when high severity vulnerabilities have been found, use: `vulnerability.severity='HIGH'`.

   - The query supplied will automatically be scoped to the scan.
   
   - For more information on vulnerability search queries, consult the InsightAppSec API search documentation [here](https://help.rapid7.com/insightappsec/en-us/api/v1/docs.html#tag/Search).
   
   - If left blank, the build will fail when **any** vulnerabilities have been found in the scan.
  
   - :warning: Ignored unless `Vulnerability results query has returned no vulnerabilities` has been selected as build advance option.
 
- **Max scan pending duration** [optional]
    - A max scan pending duration may be provided so that the length of time the CI process takes to provide feedback can be controlled.
    
        - The duration will take affect when the scan has been submitted.
        
        - Upon reaching the duration, the scan will be cancelled and the build will fail.
        
        - The following format must be used for defining a duration:
        - ``` 
          0d 5h 30m 
          ```
            - (d) - Days
            - (h) - Hours
            - (m) - Minutes
        - A quantity must be supplied for each of the above. e.g.
            - 1 day: 1d 0h 0m
            - 5 hours: 0d 5h 0m
            - 3 hours, 30 minutes: 0d 3h 30m

    - :warning: Ignored if `Scan has been submitted` has been selected as build advance option.
    
- **Max scan execution duration** [optional]
    - A max scan execution duration may be provided so that the length of time the CI process takes to provide feedback can be controlled.
        
        - The duration will take affect when the scan moves into scanning state.
        
        - Upon reaching the duration, the in-progress scan will be stopped and the build will advance as normal.
        
        - The format is same as above.
        
    - :warning: Ignored if `Scan has been submitted` or `Scan has been started` has been selected as build advance option.

- **Enable scan results** [optional]
   - Disabled by default
   
   - Flag to indicate if scan results should be viewable when a build has finished.
    
   - When enabled, a new action will be provided to view scan results, labeled 'InsightAppSec Scan Results'.
   
   - **Note: All users with access to view the build job history will be able to view InsightAppSec scan results**.
   
   - :warning: Ignored if `Scan has been submitted` or `Scan has been started` has been selected as build advance option.

### Pipeline
The plugin may be used as part of a pipeline. 

The following configuration options may be used


| Field    | Valid Values                 | Required|                                                        
|----------|------------------------------|---------|
| `region` | `US` // united states <br> `EU` // europe <br> `AU` // australia <br> `CA` // canada <br> `AP` // japan  | true |  
| `insightCredentialsId`         |     < your credentials id >                         | true |
| `scanConfigId`         | < your scan config id>                              | true |
| `buildAdvanceIndictor`         | `SCAN_SUBMITTED` <br> `SCAN_STARTED` <br> `SCAN_COMPLETED` <br> `VULNERABILITY_RESULTS` | true |
| `vulnerabilityQuery`         | A valid vulnerability search query| false |
| `maxScanPendingDuration`         | A duration string in the format described above | false |
| `maxScanExecutionDuration`         | A duration string in the format described above | false |
| `enableScanResults`         | `true` <br> `false` | false |

#### Example

Minimal configuration:
```groovy
insightAppSec region: 'US', insightCredentialsId: 'My ID', scanConfigId: 'f5984f53-2399-47e2-a6b9-010933cbc440', buildAdvanceIndicator: 'VULNERABILITY_RESULTS'
```

Full configuration:
```groovy
insightAppSec region: 'US', insightCredentialsId: 'My ID', scanConfigId: 'f5984f53-2399-47e2-a6b9-010933cbc440', buildAdvanceIndicator: 'VULNERABILITY_RESULTS', vulnerabilityQuery: 'vulnerability.severity=\'HIGH\'', maxScanPendingDuration: '0d 0h 10m', maxScanExecutionDuration: '0d 10h 0m', enableScanResults: true
```

### Using Jenkins managed Insight API Key

This plugin provides a new type of managed jenkins credential; `Insight API Key`.
A credential can be added in two ways:
- **During build configuration**
    - Use the `Add` button next to `Insight API Key` field to open the credentials modal.
    
- **Using the credentials manager**
    - From the Jenkins homepage, select `Credentials`.
    - Select the desired scope, global is appropriate.
    - Click `Add Credentials`.
    
 Select `Insight API Key` as the kind, then provide:
 - Name (A friendly name to refer to these credentials. For example, "Bob's API Key").
 - Insight API Key (API Key to connect to the Insight Platform).
    - Instructions for creating one can be found [here](https://insight.help.rapid7.com/docs/managing-platform-api-keys).
    
_Note: When using the credentials manager there is a known UI issue that blocks the API Key field with OK button, resize the page to fix the rendering._

## Development
To run the plugin locally, `cd` to the root directory and invoke:
```
mvn hpi:run
```
When the output shows `INFO: Jenkins is fully up and running` navigate to `http://localhost:8080/jenkins/` and you will see the sandbox jenkins homepage.

## See also
- InsightAppSec API documentation: https://help.rapid7.com/insightappsec/en-us/api/v1/docs.html
- Jenkins plugin tutorial: https://wiki.jenkins.io/display/JENKINS/Plugin+tutorial

