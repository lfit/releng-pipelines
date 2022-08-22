// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2019 Intel Corporation
// Copyright (c) 2020 The Linux Foundation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Method to ship logs. It is meant to be called directly, not
 * instantiated, e.g.
 * lfInfraShipLogs{}
 * or
 * lfInfraShipLogs{ logSettingsFile = myLogSettingsFileName }
 *
 * @param body Config values to be provided in the form "key = value".
 */
def call(body) {
    // Evaluate the body block and collect configuration into the object
    def config = [:]

    if (body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    def _logSettingsFile = config.logSettingsFile ?: "jenkins-log-archives-settings"
    if (!_logSettingsFile) {
        throw new Exception("Log settings file id (logSettingsFile) is " +
            "required for LF log deploy script.")
    }

    if (!("$S3_BUCKET" =~ /.*logs-s3.*/) && "$LOGS_SERVER" == "") {
        echo "No LOGS_SERVER or valid S3_BUCKET defined. Skipping log shipping."
    } else {
        // SERVER_ID should always be "logs" when running create-netrc for log shipping
        withEnv(["SERVER_ID=logs"]){
            configFileProvider([configFile(fileId: _logSettingsFile,
                                           variable: 'SETTINGS_FILE')]) {
                echo 'Running shell/create-netrc.sh'
                sh(script: libraryResource('shell/create-netrc.sh'))
            }

            lfCommon.installPythonTools()
            echo 'Running shell/sudo-logs.sh'
            sh(script: libraryResource('shell/sudo-logs.sh'))

            // Check for stashed "stack-cost" file, used to get cost data from
            // parallel builds.
            try {
                unstash "stack-cost"
            } catch(Exception e) {}

            echo 'Running shell/job-cost.sh'
            sh(script: libraryResource('shell/job-cost.sh'))

            buildDesc = ""

            if ("$S3_BUCKET" =~ /.*logs-s3.*/) {
                // If S3_BUCKET is defined, we need the config file
                configFileProvider([configFile(fileId: "jenkins-s3-log-ship",
                    targetLocation: "$HOME/.aws/credentials")]) {
                    echo 'Running shell/logs-deploy.sh'
                    sh(script: libraryResource('shell/logs-deploy.sh'))
                }
                s3_path = "logs/${SILO}/${JENKINS_HOSTNAME}/${JOB_NAME}/${BUILD_NUMBER}/"
                buildDesc += "S3 build logs: <a href=\"https://$CDN_URL/$s3_path\">https://$CDN_URL/$s3_path</a>\n"
                // If LOGS_SERVER is also defined, logs-deploy.sh will deploy to both
                if ("$LOGS_SERVER" != "") {
                    nexus_path = "${SILO}/${JENKINS_HOSTNAME}/${JOB_NAME}/${BUILD_NUMBER}"
                    buildDesc += "Nexus build logs: <a href=\"$LOGS_SERVER/" +
                        "$nexus_path\">$LOGS_SERVER/$nexus_path</a>\n"
                }
            } else {  // Only LOGS_SERVER is defined
                echo 'Running shell/logs-deploy.sh'
                sh(script: libraryResource('shell/logs-deploy.sh'))
                nexus_path = "${SILO}/${JENKINS_HOSTNAME}/${JOB_NAME}/${BUILD_NUMBER}"
                buildDesc += "Nexus build logs: <a href=\"$LOGS_SERVER/" +
                    "$nexus_path\">$LOGS_SERVER/$nexus_path</a>\n"
            }

            echo 'Running shell/logs-clear-credentials.sh'
            sh(script: libraryResource('shell/logs-clear-credentials.sh'))
        }

        if (!currentBuild.description) {currentBuild.description = ''}
        // The old GHPRB plugin updated the description to contain the PR #
        // with a link to the PR. If the build description contains a link to
        // the PR, then add a br.
        if (currentBuild.description.contains('PR #')) {
            currentBuild.description += "<br>"
        }
        currentBuild.description += buildDesc
    }
}
