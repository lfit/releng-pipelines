#! /bin/bash
# SPDX-License-Identifier: Apache-2.0
##############################################################################
# Copyright (c) 2019 Intel Corporation
# Copyright (c) 2020 The Linux Foundation
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#      http:#www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
##############################################################################
if [ "$#" -ne 1 ]; then
    echo "Usage: $0 <Jenkins URL>"
    exit
fi

echo "---> pipeline-linter.sh"

set -u

JENKINS_URL=$1
JENKINS_CRUMB=`curl --silent "$JENKINS_URL/crumbIssuer/api/xml?xpath=concat(//crumbRequestField,\":\",//crumb)"`
JENKINS_VAL="$JENKINS_URL/pipeline-model-converter/validate"
JENKINS_FILE_LIST=(`grep -lr "pipeline\s*{" vars src Jenkinsfile`)
nonZeroexit=false

for JENKINS_FILE in "${JENKINS_FILE_LIST[@]}"
do
    ret=$(curl --silent -X POST -H $JENKINS_CRUMB -F "jenkinsfile=<$JENKINS_FILE" $JENKINS_VAL)
    if [[ $ret == *"Errors"* ]];then
        echo "Linting error for $JENKINS_FILE"
        echo $ret
        nonZeroexit=true
    else
        echo "$JENKINS_FILE successfully validated"
    fi
done

# Set non-zero exit if linter reports any errors
if [ $nonZeroexit ]
then
    exit 1
fi
