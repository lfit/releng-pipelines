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

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class LFJavaSpec extends JenkinsPipelineSpecification {

    def lfJava = null
    def defaults = [
        javaVersion: "openjdk11",
        mvnGoals: "clean install",
        mvnVersion: "mvn35",
        mvnGlobalSettings: "testGlobalConfig",
        mvnSettings: "testConfig",
    ]

    def setup() {
        lfJava = loadPipelineScriptForTest('vars/lfJava.groovy')
        explicitlyMockPipelineVariable('lfCommon')
        explicitlyMockPipelineVariable('lfDefaults')
    }

    def "Test lfJava [Should] build maven [When] called" () {
        setup:
            explicitlyMockPipelineStep('withMaven')
            getPipelineMock('lfDefaults.call')() >> {
                return defaults
            }
            getPipelineMock("libraryResource")('shell/python-tools-install.sh') >> {
                return 'python-tools-install'
            }
            getPipelineMock("lfCommon.installPythonTools").call(_) >> null
            getPipelineMock("lfCommon.jacocoNojava").call(_) >> null
        when: 'Variables are properly set'
            lfJava()
        then:
            1 * getPipelineMock('withMaven').call(_) >> { _arguments ->
                // Keys are different, but all config values in withMaven should
                // match what we passed.
                _arguments[0][0].values().each { i ->
                    assert defaults.values().contains(i)
                }
            }
            1 * getPipelineMock('sh').call("mvn clean install")
    }

    def "Test lfJava [Should] build & deploy maven [When] branch == 'master'" () {
        setup:
            explicitlyMockPipelineStep("withMaven")
            getPipelineMock('lfDefaults.call')() >> {
                return defaults
            }
            getPipelineMock("libraryResource")('shell/python-tools-install.sh') >> {
                return 'python-tools-install'
            }
            getPipelineMock("lfCommon.installPythonTools").call(_) >> null
            getPipelineMock("lfCommon.jacocoNojava").call(_) >> null

            explicitlyMockPipelineStep("mavenDeploy")
            lfJava.getBinding().setVariable("env", ["GIT_BRANCH": "master"])
        when: 'Variables are properly set'
            lfJava()
        then:
            2 * getPipelineMock('withMaven').call(_) >> { _arguments ->
                // Keys are different, but all config values in withMaven should
                // match what we passed.
                _arguments[0][0].values().each { i ->
                    assert defaults.values().contains(i)
                }
            }
            1 * getPipelineMock('sh').call("mvn clean install")
    }
}
