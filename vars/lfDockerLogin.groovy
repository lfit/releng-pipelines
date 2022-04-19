// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2019 Intel Corporation
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

/**
 * Method to login to all needed Docker registries.
 *   Required body values:
 *     * settingsFile: Maven settings config file ID
 *   Optional body values (should be defined in global defaults):
 *     * dockerRegistry
 *     * dockerRegistryPorts
 *     * dockerHubRegistry
 *     * dockerHubEmail
 *
 * @param body Config values to be provided in the form "key = value".
 */

def call(config = [:]) {
    // The LF Global JJB Docker Login script looks for information in the following variables:
    // $SETTINGS_FILE, $DOCKER_REGISTRY, $REGISTRY_PORTS, $DOCKERHUB_REGISTRY, $DOCKERHUB_EMAIL
    // Please refer to the shell script in global-jjb/shell for the usage.
    // Most parameters are listed as optional, but without any of them set the script has no operation.
    def _dockerRegistry = config.dockerRegistry ?: ''
    def _dockerRegistryPorts = config.dockerRegistryPorts ?: ''
    def _dockerHubRegistry = config.dockerHubRegistry ?: ''
    def _dockerHubEmail = config.dockerHubEmail ?: ''

    def _settingsFile = config.settingsFile
    if (!_settingsFile) {
        error('Project Settings File id (settingsFile) is required for the docker login script.')
    }

    if (_dockerRegistry && !_dockerRegistryPorts) {
        error('Docker registry ports (dockerRegistryPorts) are required when docker registry is set (dockerRegistry).')
    }

    if (_dockerRegistryPorts && !_dockerRegistry) {
        error('Docker registry (dockerRegistry) is required when docker registry ports are set (dockerRegistryPorts).')
    }

    def envVars = []
    if (_dockerRegistry)      { envVars << "DOCKER_REGISTRY=${_dockerRegistry}" }
    if (_dockerRegistryPorts) { envVars << "REGISTRY_PORTS=${_dockerRegistryPorts}" }
    if (_dockerHubRegistry)   { envVars << "DOCKERHUB_REGISTRY=${_dockerHubRegistry}" }
    if (_dockerHubEmail)      { envVars << "DOCKERHUB_EMAIL=${_dockerHubEmail}" }

    withEnv(envVars){
        configFileProvider([configFile(fileId: _settingsFile, variable: 'SETTINGS_FILE')]) {
            sh(script: libraryResource('global-jjb-shell/docker-login.sh'))
        }
    }
}
