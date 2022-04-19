########
lfDocker
########

Parameters
==========

:Required Parameters:

    :mvnSettings: Jenkins ID of maven settings file to be used by this job.
    :project: Name to be given to the built container.

:Optional Parameters:

    :containerPushRegistry: Override default registry to push built image.

:Environment Variables:

    :DOCKER_BUILD_ARGS: Build-time arguments to pass to Docker.
    :VERSION: If version is not being set through another means, it can be
        passed in via this env var.
    :DOCKER_CUSTOM_TAGS: Space-separated string of tags to push.

Usage
=====

Calling lfDocker will log into Docker registries (via the global-jjb shell
script docker-login.sh), and then build the container. If the branch is "master"
or "main", the container will then be pushed to the containerPushRegistry (this
should be defined in lfDefaults, but can also optionally be passed in).

There are also environment variables that can be used to further tune the build
or push process, but these are always optional.
