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
//

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

    if(body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    def _logSettingsFile = config.logSettingsFile ?: 'jenkins-log-archives-settings'
    if (!_logSettingsFile) {
        throw new Exception('Log settings file id (logSettingsFile) is required for LF log deploy script.')
    }

    withEnv(["SERVER_ID=logs"]){
        configFileProvider([configFile(fileId: _logSettingsFile, variable: 'SETTINGS_FILE')]) {
            echo 'Running shell/create-netrc.sh'
            sh(script: libraryResource('shell/create-netrc.sh'))
        }

        echo 'Running shell/python-tools-install.sh'
        sh(script: libraryResource('shell/python-tools-install.sh'))
        echo 'Running shell/sudo-logs.sh'
        sh(script: libraryResource('shell/sudo-logs.sh'))
        echo 'Running shell/job-cost.sh'
        sh(script: libraryResource('shell/job-cost.sh'))

        if ("$S3_BUCKET" =~ /.*logs-s3.*/) {
            configFileProvider([configFile(fileId: "jenkins-s3-log-ship",
                                           targetLocation: '$HOME/.aws/credentials')]) {
                echo 'Running shell/logs-deploy.sh'
                sh(script: libraryResource('shell/logs-deploy.sh'))
            }
        } else {
            echo 'Running shell/logs-deploy.sh'
            sh(script: libraryResource('shell/logs-deploy.sh'))
        }

        echo 'Running shell/logs-clear-credentials.sh'
        sh(script: libraryResource('shell/logs-clear-credentials.sh'))
    }

    // Set build description with build logs and PR info if applicable
    if (!currentBuild.description) {currentBuild.description = ''}

    // the old GHPRB plugin updated the description to contain the PR # with a link to the PR.
    // If the build description contains a link to the PR then add a br
    if (currentBuild.description.contains('PR #')) {
        currentBuild.description += "<br>"
    }
    currentBuild.description += "Build logs: <a href=\"$LOGS_SERVER/$SILO/$JENKINS_HOSTNAME/$JOB_NAME/$BUILD_NUMBER\">$LOGS_SERVER/$SILO/$JENKINS_HOSTNAME/$JOB_NAME/$BUILD_NUMBER</a>"
}
