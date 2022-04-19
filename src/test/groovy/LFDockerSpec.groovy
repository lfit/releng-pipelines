// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2022 The Linux Foundation
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

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class LFDockerSpec extends JenkinsPipelineSpecification {

    def lfDocker = null
    def defaults = [
        containerPublicRegistry: "docker.io",
        containerPushRegistry: "nexus3.example.org",
        mvnSettings: "testConfig",
        project: "testProject",
    ]

    def setup() {
        lfDocker = loadPipelineScriptForTest('vars/lfDocker.groovy')
        explicitlyMockPipelineVariable('lfCommon')
        explicitlyMockPipelineVariable('lfDefaults')
    }

    def "Test lfDocker [Should] throw exception [When] mvnSettings is null" () {
        setup:
        when:
            lfDocker({mvnSettings = null})
        then:
            thrown Exception
    }

    def "Test lfDocker [Should] throw exception [When] project is null" () {
        setup:
        when:
            lfDocker()
        then:
            thrown Exception
    }

    def "Test lfDocker [Should] build docker image [When] called" () {
        setup:
            def environmentVariables = [
                "DOCKER_FILE_PATH": "",
                "DOCKER_BUILD_ARGS": "",
                "DOCKER_BUILD_CONTEXT": ""
            ]
            lfDocker.getBinding().setVariable("env", environmentVariables)
            getPipelineMock("lfDefaults.call")() >> {
                return defaults
            }
            explicitlyMockPipelineStep("dockerBuild")
            explicitlyMockPipelineStep("docker.build")
        when:
            lfDocker()
        then:
            1 * getPipelineMock("lfCommon.containerRegistryLogin").call(_) >> null
            1 * getPipelineMock("docker.build").call([
                "nexus3.example.org/testProject",
                "-f Dockerfile  ."
            ])
    }

    def "Test lfDocker [Should] build & deploy container [When] branch == 'master'" () {
        setup:
            def environmentVariables = [
                "DOCKER_FILE_PATH": "",
                "DOCKER_BUILD_ARGS": "",
                "DOCKER_BUILD_CONTEXT": ""
            ]
            lfDocker.getBinding().setVariable("env", environmentVariables)
            getPipelineMock("lfDefaults.call")() >> {
                return defaults
            }
            explicitlyMockPipelineStep("dockerBuild")
            explicitlyMockPipelineStep("docker.build")
        when:
            lfDocker()
        then:
            1 * getPipelineMock("lfCommon.containerRegistryLogin").call(_) >> null
            1 * getPipelineMock("docker.build").call([
                "nexus3.example.org/testProject",
                "-f Dockerfile  ."
            ])
    }

    def "Test lfDocker [Should] build & deploy container [When] branch == 'master'" () {
        setup:
            def environmentVariables = [
                "DOCKER_FILE_PATH": "",
                "DOCKER_BUILD_ARGS": "",
                "DOCKER_BUILD_CONTEXT": ""
            ]
            lfDocker.getBinding().setVariable("env", environmentVariables)
            getPipelineMock("lfDefaults.call")() >> {
                return defaults
            }
            explicitlyMockPipelineStep("dockerBuild")
            explicitlyMockPipelineStep("docker.build")
        when:
            lfDocker()
        then:
            1 * getPipelineMock("lfCommon.containerRegistryLogin").call(_) >> null
            1 * getPipelineMock("docker.build").call([
                "nexus3.example.org/testProject",
                "-f Dockerfile  ."
            ])
    }
}
