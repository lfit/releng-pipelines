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
 * Method to run Java jobs.
 *   Required body values:
 *     * mvnSettings: Maven settings config file ID
 *   Optional body values:
 *     * javaVersion (default: "openjdk11")
 *     * mvnGlobalSettings (default: "global-settings")
 *     * mvnGoals (default: "clean install")
 *     * mvnVersion (default: "mvn35")
 *     * archiveArtifacts (default:
 *           """**\/*.log
 *           **\/hs_err_*.log
 *           **\/target/**\/feature.xml
 *           **\/target/failsafe-reports/failsafe-summary.xml
 *           *\/target/surefire-reports/*-output.txt"""
 *       )
 *
 * @param body Config values to be provided in the form "key = value".
 */
def call(body) {
    // Evaluate the body block and collect configuration into the object
    cat "begin lfJava() defaults"
    def defaults = lfDefaults()
    cat "begin lfJava() config"
    def config = [:]
    // Set default archiveArtifacts for Maven builds.
    defaults.archiveArtifacts = """**/*.log
**/hs_err_*.log
**/target/**/feature.xml
**/target/failsafe-reports/failsafe-summary.xml
**/target/surefire-reports/*-output.txt"""
    ls -la
    if (body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    // For duplicate keys, Groovy will use the right hand map's values.
    config = defaults + config

    if (!config.mvnSettings) {
        throw new Exception("Maven settings file id (mvnSettings) is " +
            "required for lfJava function.")
    }

    lfCommon.installPythonTools()
    lfCommon.jacocoNojava()

    withMaven(
        maven: config.mvnVersion,
        jdk: config.javaVersion,
        mavenSettingsConfig: config.mvnSettings,
        globalMavenSettingsConfig: config.mvnGlobalSettings,
    ) {
        sh "mvn ${config.mvnGoals}"
    }

    if (env.GIT_BRANCH == "main" || env.GIT_BRANCH == "master") {
        mavenDeploy(config)
    }

    // TODO: Make this generic, rather than Github-specific. A function in
    // lfCommon that will take comments from different providers and check them
    // would be ideal.
    if (env.GITHUB_COMMENT =~ /stage/) {
        mavenStage(config)
    }
}

def mavenDeploy(config) {
    deployScript = [
        libraryResource('shell/common-variables.sh'),
        libraryResource('shell/maven-deploy.sh')
    ].join("\n")

    withMaven(
        maven: config.mvnVersion,
        jdk: config.javaVersion,
        mavenSettingsConfig: config.mvnSettings,
        globalMavenSettingsConfig: config.mvnGlobalSettings,
    ) {
        sh(script: deployScript)
    }
}

def mavenStage(config) {
    lfCommon.sigulSignDir(".", "")
    stageScript = [
        libraryResource('shell/common-variables.sh'),
        libraryResource('shell/maven-stage.sh')
    ].join("\n")

    withMaven(
        maven: config.mvnVersion,
        jdk: config.javaVersion,
        mavenSettingsConfig: config.mvnSettings,
        globalMavenSettingsConfig: config.mvnGlobalSettings,
    ) {
        sh(script: stageScript)
    }
}
