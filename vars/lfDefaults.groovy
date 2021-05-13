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
 * Provides default values for common variables within the library.
 *
 */
def call(body) {
    def defaults = [
        lftoolsVersion: "<1.0.0",
        packerVersion: "1.2.3",
        jenkinsSshCredential: "edgex-jenkins-ssh",
        buildDaysToKeep: 30,
        buildTimeout: 360,
        archiveArtifacts: "",
        javaVersion: "openjdk11",

        mvnGoals: "clean install",
        mvnVersion: "mvn35",
        mvnGlobalSettings: "global-settings",
    ]
    return defaults
}
