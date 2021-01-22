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

The calling pipeline must manually unstash the "stack-cost" file. For LF
Pipelines, this is handled by the lfInfraShipLogs function.
