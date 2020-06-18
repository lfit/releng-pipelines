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
import spock.lang.Ignore

public class LFInfraShipLogsSpec extends JenkinsPipelineSpecification {

    def lfInfraShipLogs = null

    def setup() {
        lfInfraShipLogs = loadPipelineScriptForTest('vars/lfInfraShipLogs.groovy')
        explicitlyMockPipelineVariable('out')
    }

    def "Test lfInfraShipLogs [Should] throw exception [When] logSettingsFile is null" () {
        setup:
        when:
            lfInfraShipLogs({logSettingsFile = null})
        then:
            thrown Exception
    }

    def "Test lfInfraShipLogs [Should] call expected shell scripts with expected arguments [When] called" () {
        setup:
            def environmentVariables = [
                'DOCKER_REGISTRY': 'MyDockerRegistry',
                'ghprbPullId': true
            ]
            lfInfraShipLogs.getBinding().setVariable('env', environmentVariables)
            explicitlyMockPipelineStep('echo')
            explicitlyMockPipelineStep('withEnv')
            getPipelineMock("libraryResource")('shell/create-netrc.sh') >> {
                return 'create-netrc'
            }
            getPipelineMock("libraryResource")('shell/python-tools-install.sh') >> {
                return 'python-tools-install'
            }
            getPipelineMock("libraryResource")('shell/sudo-logs.sh') >> {
                return 'sudo-logs'
            }
            getPipelineMock("libraryResource")('shell/job-cost.sh') >> {
                return 'job-cost'
            }
            getPipelineMock("libraryResource")('shell/logs-deploy.sh') >> {
                return 'logs-deploy'
            }
            getPipelineMock("libraryResource")('shell/logs-clear-credentials.sh') >> {
                return 'logs-clear-credentials'
            }
            lfInfraShipLogs.getBinding().setVariable('currentBuild', [:])
            lfInfraShipLogs.getBinding().setVariable('LOGS_SERVER', 'MyLogServer')
            lfInfraShipLogs.getBinding().setVariable('S3_BUCKET', 'my-logs-s3')
            lfInfraShipLogs.getBinding().setVariable('SILO', 'MySilo')
            lfInfraShipLogs.getBinding().setVariable('JENKINS_HOSTNAME', 'MyJenkinsHostname')
            lfInfraShipLogs.getBinding().setVariable('JOB_NAME', 'MyJobName')
            lfInfraShipLogs.getBinding().setVariable('BUILD_NUMBER', 'MyBuildNumber')
        when:
            lfInfraShipLogs()
        then:
            1 * getPipelineMock('withEnv').call(_) >> { _arguments ->
                def envArgs = [
                    'SERVER_ID=logs'
                ]
                assert envArgs == _arguments[0][0]
            }
            1 * getPipelineMock('sh').call([script:'create-netrc'])
            1 * getPipelineMock('sh').call([script:'python-tools-install'])
            1 * getPipelineMock('sh').call([script:'sudo-logs'])
            1 * getPipelineMock('sh').call([script:'job-cost'])
            // TODO: logs-deploy should test both possible conditions
            1 * getPipelineMock('sh').call([script:'logs-deploy'])
            1 * getPipelineMock('sh').call([script:'logs-clear-credentials'])
            // TODO: Check value of currentBuild.description mock variable if possible
    }
}
