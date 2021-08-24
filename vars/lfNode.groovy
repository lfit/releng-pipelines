// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2021 The Linux Foundation
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
 * Method to run Node jobs.
 *   Optional body values (should be defined in lfDefaults):
 *     * nodeDir
 *     * nodeVersion
 *
 * @param body Config values to be provided in the form "key = value".
 */
def call(body) {
    // Evaluate the body block and collect configuration into the object
    def defaults = lfDefaults()
    def config = [:]

    if (body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    // For duplicate keys, Groovy will use the right hand map's values.
    config = defaults + config

    ////////////////////////
    // Default parameters //
    ////////////////////////
    archiveArtifacts = """**/*.log
**/hs_err_*.log
**/target/**/feature.xml
**/target/failsafe-reports/failsafe-summary.xml
**/target/surefire-reports/*-output.txt"""

    ///////////////////
    // Verify NodeJS //
    ///////////////////
    dir(config.nodeDir) {
        // The nodeJS installation configured in Jenkins' Global Tools should
        // be named simply with the version.
        nodejs(nodeJSInstallationName: config.nodeVersion, configId: "npmrc") {
            sh "npm install"
            sh "npm test"
        }
    }
}
