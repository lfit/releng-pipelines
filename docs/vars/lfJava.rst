######
lfJava
######

Parameters
==========

:Required Parameters:

    :mvnSettings: Jenkins ID of maven settings file to be used by this job

:Optional Parameters:

    :javaVersion: Java version to use for Maven build
    :mvnGlobalSettings: Override default global-settings filename
    :mvnGoals: String with maven goals to execute
    :mvnVersion: Maven version to use in build
    :archiveArtifacts: Newline-separated list of paths to archive.

Usage
=====

Calling lfJava will prep the agent and then execute a maven build, using the
mvnGoals specified. If the branch is "master" or "main", the maven-deploy script will
be called, deploying artifacts to maven. If triggered by a comment containing
the keyword "stage", the maven-stage script will be run to sign and stage
artifacts for release.
