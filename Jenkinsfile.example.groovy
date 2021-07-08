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
//

loadGlobalLibrary()

pipeline {
    agent {
        // This label should match an agent available on the target system
        label "centos7-docker-4c-2g"
    }

    options {
        timestamps()
        timeout(360)
    }

    environment {
        // The settings file needs to exist on the target Jenkins system
        mvnSettings = "sandbox-settings"
    }

    stages {
        stage("Java Build") {
            steps {
                lfJava(mvnSettings=env.mvnSettings)
            }
        }
        stage("Parallel Testing") {
            parallel {
                stage("amd") {
                    // This label should match an agent available on the target system
                    node {"amdNode"}
                    steps {
                        sh "echo AMD tests"
                    }
                    post {
                        always {
                            lfParallelCostCapture()
                        }
                    }
                }
                stage("arm") {
                    // This label should match an agent available on the target system
                    node {"armNode"}
                    steps {
                        sh "echo ARM tests"
                    }
                    post {
                        always {
                            lfParallelCostCapture()
                        }
                    }
                }
            }
        }
    }

    post {
        always {
            // The default logSettingsFile is "jenkins-log-archives-settings".
            // If this file isn't present, a different value for logSettingsFile
            // will need to be passed to lfInfraShipLogs.
            lfInfraShipLogs()
        }
    }
}

// This loadGlobalLibrary call is only required if the library is not defined
// in the Jenkins global or job settings. Otherwise, a simple "@Library" call
// will suffice.
def loadGlobalLibrary(branch = "*/master") {
    library(identifier: "pipelines@master",
        retriever: legacySCM([
            $class: "GitSCM",
            userRemoteConfigs: [[url: "https://github.com/lfit/releng-pipelines"]],
            branches: [[name: branch]],
            doGenerateSubmoduleConfigurations: false,
            extensions: [[
                $class: "SubmoduleOption",
                recursiveSubmodules: true,
            ]]]
        )
    ) _
}
