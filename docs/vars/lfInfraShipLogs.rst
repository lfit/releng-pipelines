###############
lfInfraShipLogs
###############

Parameters
==========

:Optional Parameters:

    :logSettingsFile: Override the file name where log shipping credentials can
        be found. Default: ``jenkins-log-archives-settings``

Usage
=====

lfInfraShipLogs looks for two environment variables: ``LOGS_SERVER`` and
``S3_BUCKET``. If ``LOGS_SERVER`` is present, logs will be pushed to the Nexus
server that the variable points to. If ``S3_BUCKET`` is present and contains a
name matching the regex ``.*logs-s3.*``, logs will be pushed to the indicated
S3 bucket.

If both ``LOGS_SERVER`` and ``S3_BUCKET`` are defined, lfInfraShipLogs will
attempt to push to both servers. If neither is defined, it will echo this fact
and return.
