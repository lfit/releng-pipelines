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
    println "Printing body1:  $body"
    println "Printing body.mvnSettings:  $body.mvnSettings"
    // Evaluate the body block and collect configuration into the object
    def defaults = lfDefaults()
    println "Printing defaults:  $defaults"
    def config = [:] //creating an empty map

    // Set default archiveArtifacts for Maven builds.
    defaults.archiveArtifacts = """**/*.log
**/hs_err_*.log
**/target/**/feature.xml
**/target/failsafe-reports/failsafe-summary.xml
**/target/surefire-reports/*-output.txt"""
    defaults.mvnSettings = "$body.mvnSettings"

    // if (body) {
    //     println "body.resolveStrategy :  $body.resolveStrategy "
    //     body.resolveStrategy = Closure.DELEGATE_FIRST
    //     println "body.delegate :  $body.delegate "
    //     body.delegate = config
    //     body()
    // }

    // For duplicate keys, Groovy will use the right hand map's values.
    config = defaults + config
    println "Printing final config:  $config"

    if (!config.mvnSettings) {
        throw new Exception("Maven settings file id (mvnSettings) is " +
            "required for lfJava function.")
    }

    // sh "echo starting pythontools"
    // lfCommon.installPythonTools()
    // lfCommon.jacocoNojava()
    // lfCommon.updateJavaAlternatives("jdk17")
    // sh 'rpm -aq | grep -i jdk'    
    // sh "java -version"
    // sh 'printenv'
    // // sh 'echo JAVA_HOME="$JAVA_HOME"'
    // // sh 'ls -la $JAVA_HOME'
    // sh 'which java'
    sh "echo JAVA_HOME=$JAVA_HOME"

    withMaven(
        maven: config.mvnVersion,
        jdk: config.javaVersion,
        mavenSettingsConfig: config.mvnSettings,
        globalMavenSettingsConfig: config.mvnGlobalSettings,
    ) {
        sh 'ls -la /w/tools/hudson.model.JDK/jdk17/jdk-17.0.4/'
        sh 'echo JAVA_HOME="/w/tools/hudson.model.JDK/jdk17/jdk-17.0.4/" > env.JAVA_HOME'
        environment {
               JAVA_HOME = sh(script: "/w/tools/hudson.model.JDK/jdk17/jdk-17.0.4/", , returnStdout: true).trim()
           }
        sh "echo JAVA_HOME=$JAVA_HOME"
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
