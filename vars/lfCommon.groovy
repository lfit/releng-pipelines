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
        sh(script: configAndInstall)
        withEnv([
            "SIGN_DIR=signDir",
            "SIGN_MODE=signMode"
        ]) {
            sh(script: libraryResource('shell/sigul-install.sh'))
        }
        sh(script: libraryResource('shell/sigul-configuration-cleanup.sh'))
    }
}
