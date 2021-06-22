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

def call() {
    try {
        sh(script: libraryResource('shell/job-cost.sh'))
        cost_str = sh(script: "cat $WORKSPACE/archives/cost.csv | cut -d, -f6", returnStdout: true)
    } catch(Exception e) {
        // Failure in job-cost.sh should not affect the rest of the run.
        println("Exception caught while running job-cost.sh.")
        return
    }

    lock("${BUILD_TAG}-stack-cost") {
        try {
            unstash "stack-cost"
            stack_cost = sh(script: "cat stack-cost | awk '{print \$2}", returnStdout: true)
        } catch(Exception e) {
            stack_cost = 0
        }

        cost = (cost_str as float) + (stack_cost as float)
        sh("echo total: ${cost} > stack-cost")
        stash includes: "**/stack-cost", name: "stack-cost"
    }
}
