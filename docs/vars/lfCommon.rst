########
lfCommon
########

Common functions to be used by other scripts.

Functions
=========

installPythonTools
------------------

This should be run before most jobs, in order to install commonly-used python tools.

jacocoNojava
------------

Workaround for Jenkins not being able to find Java in JaCoCo runs.

updateJavaAlternatives
----------------------

Runs a script to ensure that the preferred version of Java is installed and is
the default when java commands are executed.

:Required parameters:

    :javaVersion: The version of java to set as the default, e.g. "openjdk11".

sigulSignDir
------------

Signs the specified directory as a single artifact.

:Required parameters:

    :signDir: Path to directory to be signed (absolute path, or relative to
        the current working directory).
    :signMode: Serial or parallel. If left blank, the default (serial) is used.

dockerLogin
-----------

Function to login to all needed Docker registries.

:Required parameters:

    :settingsFile: Maven settings config file ID.

:Optional parameters:

    :dockerRegistry: Local docker registry.
    :dockerRegistryPorts: Ports to use for local docker registry.
    :dockerHubRegistry: Path to external registry (generally Docker Hub).
    :dockerHubEmail: Email for logging into external registry (Docker <17.06.0
        only).
