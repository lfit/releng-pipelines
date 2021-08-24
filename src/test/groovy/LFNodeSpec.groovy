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

public class LFNodeSpec extends JenkinsPipelineSpecification {

    def lfNode = null
    def defaults = [
        nodeDir: "",
        nodeVersion: "14.17.5",
    ]

    def setup() {
        lfNode = loadPipelineScriptForTest("vars/lfNode.groovy")
        explicitlyMockPipelineVariable("lfDefaults")
    }

    def "Test lfNode [Should] build NodeJS [When] called" () {
        setup:
            explicitlyMockPipelineStep("dir")
            explicitlyMockPipelineStep("nodejs")
            getPipelineMock("lfDefaults.call")() >> {
                return defaults
            }
        when:
            lfNode()
        then:
            1 * getPipelineMock("dir").call(_)
            1 * getPipelineMock("nodejs").call(_) >> { _arguments ->
                assert _arguments[0][0]["nodeJSInstallationName"] == defaults["nodeVersion"]
                assert _arguments[0][0]["configId"] == "npmrc"
            }
            1 * getPipelineMock("sh").call("npm install")
            1 * getPipelineMock("sh").call("npm test")
    }
}
