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
 * Common functions
 */

// Replaces lf-infra-pre-build
def installPythonTools() {
    sh(script: libraryResource('shell/python-tools-install.sh'))
}

// Replaces lf-jacoco-nojava-workaround
def jacocoNojava() {
    sh("mkdir -p $WORKSPACE/target/classes $WORKSPACE/jacoco/classes")
}

def updateJavaAlternatives(javaVersion) {
    bashScript = [
        "SET_JDK_VERSION=$javaVersion",
        libraryResource('shell/update-java-alternatives.sh')
    ].join("\n")
    sh(script: bashScript)
    // TODO: Inject /tmp/java.env
}

def sigulSignDir(signDir, signMode) {
    if (signMode == "") {
        signMode = "serial"
    }
    configFileProvider([
        configFile(fileId: 'sigul-config', variable: 'SIGUL_CONFIG'),
        configFile(fileId: 'sigul-password', variable: 'SIGUL_PASSWORD'),
        configFile(fileId: 'sigul-pki', variable: 'SIGUL_PKI')
    ]) {
        configAndInstall = [
            libraryResource('shell/sigul-configuration.sh'),
            libraryResource('shell/sigul-install.sh')
        ].join("\n")
        withEnv([
            "SIGN_DIR=$signDir",
            "SIGN_MODE=$signMode"
        ]) {
            sh(script: configAndInstall)
        }
        sh(script: libraryResource('shell/sigul-configuration-cleanup.sh'))
    }
}

/**
 * Function to login to all needed container registries.
 *
 * @param settingsFile Maven settings config file ID.
 * @param containerRegistry Local container registry.
 * @param containerRegistryPorts Ports to use for local docker registry.
 * @param externalContainerRegistry Path to external registry (generally Docker Hub).
 * @param dockerHubEmail Email for logging into external registry (Docker <17.06.0 only).
 */

def containerRegistryLogin(settingsFile, containerRegistry="", containerRegistryPorts="",
         externalContainerRegistry="", dockerHubEmail="") {
    // The LF Global JJB Docker Login script looks for information in the following variables:
    // $SETTINGS_FILE, $DOCKER_REGISTRY, $REGISTRY_PORTS, $DOCKERHUB_REGISTRY, $DOCKERHUB_EMAIL
    // Please refer to the shell script in global-jjb/shell for the usage.
    // Most parameters are listed as optional, but without any of them set the script has no operation.
    if (!settingsFile) {
        error('Project Settings File id (settingsFile) is required for the container registry login script.')
    }

    if (containerRegistry && !containerRegistryPorts) {
        error('Container registry ports (containerRegistryPorts) are required when registry (containerRegistry) is set.')
    }

    if (containerRegistryPorts && !containerRegistry) {
        error('Container registry (containerRegistry) is required when registry ports (containerRegistryPorts) are set.')
    }

    if (dockerHubEmail && !externalContainerRegistry) {
        error('External registry (externalContainerRegistry) is required when Docker Hub Email (dockerHubEmail) is set.')
    }

    def envVars = []
    if (containerRegistry)      { envVars << "DOCKER_REGISTRY=${containerRegistry}" }
    if (containerRegistryPorts) { envVars << "REGISTRY_PORTS=${containerRegistryPorts}" }
    if (externalContainerRegistry)   { envVars << "DOCKERHUB_REGISTRY=${externalContainerRegistry}" }
    if (dockerHubEmail)      { envVars << "DOCKERHUB_EMAIL=${dockerHubEmail}" }

    withEnv(envVars){
        configFileProvider([configFile(fileId: settingsFile, variable: 'SETTINGS_FILE')]) {
            sh(script: libraryResource('global-jjb-shell/docker-login.sh'))
        }
    }
}
