// SPDX-License-Identifier: Apache-2.0
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

import com.homeaway.devtools.jenkins.testing.JenkinsPipelineSpecification

public class LFCommonSpec extends JenkinsPipelineSpecification {

    def lfCommon = null

    def setup() {
        lfCommon = loadPipelineScriptForTest("vars/lfCommon.groovy")
    }

    def "Test lfCommon [Should] call expected script [When] methods are called" () {
        setup:
            lfCommon.getBinding().setVariable('WORKSPACE', '/w/test/job/1')
            getPipelineMock("libraryResource")("shell/python-tools-install.sh") >> {
                return "python-tools-install"
            }
            getPipelineMock("libraryResource")("shell/update-java-alternatives.sh") >> {
                return "update-java-alternatives"
            }
        when:
            lfCommon.installPythonTools()
        then:
            1 * getPipelineMock("sh").call([script:"python-tools-install"])
        when:
            lfCommon.jacocoNojava()
        then:
            1 * getPipelineMock("sh").call("mkdir -p /w/test/job/1/target/classes /w/test/job/1/jacoco/classes")
        when:
            lfCommon.updateJavaAlternatives("openjdk11")
        then:
            1 * getPipelineMock("sh").call([script:
                "SET_JDK_VERSION=openjdk11" + "\n" +
                "update-java-alternatives"
            ])
    }
}
