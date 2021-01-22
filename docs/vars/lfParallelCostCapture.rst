#####################
lfParallelCostCapture
#####################

Parameters
==========

None.

Usage
=====

This function is designed to run at the end of a parallel stage that has spun
up a temporary node. It will pull in the cost data, and stash it in a file
titled "stack-cost". This can then be picked up by the job-cost.sh script.

.. warning::
    Calling this function requires that the Lockable Resources Plugin is
    installed.

An example of the intended implementation:

.. code-block:: groovy

    parallel {
        stage('1') {
            node {newNode1}  // Only add post stage if a new node is being used
            stages {<all stages>}
            post {
                always {
                    lfParallelCostCapture()
                }
            }
        }
        stage('2') {
            node {newNode2}
            stages {<all stages>}
            post {
                always {
                    lfParallelCostCapture()
                }
            }
        }
    }

The calling pipeline must manually unstash the "stack-cost" file. For LF
Pipelines, this is handled by the lfInfraShipLogs function.
