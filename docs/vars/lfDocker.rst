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

Functions
=========

dockerBuild
-----------

Function to build a docker image.

:Required Parameters:

    :dockerImageName: Image name to build.

:Environment Variables:

    :DOCKER_BUILD_ARGS: Build-time arguments to pass to Docker.
    :DOCKER_BUILD_CONTEXT: Override default build context of "." (present
        working directory).
    :DOCKER_FILE_PATH: Override default path to Dockerfile.

dockerPush
----------

Function to push a docker image to a registry.

:Required Parameters:

    :dockerImage: Image name to push.
    :registry: Registry to push to.

:Optional Parameters:

    :latest: Boolean indicating if this push should be tagged "latest".
    :tags: List of tags. Used in lieu of env.DOCKER_CUSTOM_TAGS.

:Environment Variables:

    :CONTAINER_PUSH_REGISTRY: The registry that the image is being pushed to.
    :DOCKER_CUSTOM_TAGS: Space-separated string of additional tags.
    :GIT_COMMIT: Git commit SHA. Should always be part of the build env.
    :VERSION: If version is not being set through another means, it can be
        passed in.

getDockerTags
-------------

This function polls multiple possible sources for Docker tags, compiling them
and returning all tags in a single list.

:Optional Parameters:

    :latest: Boolean indicating if this should be tagged "latest".
    :customTags: Space-separated string of additional tags.

:Environment Variables:

    :DOCKER_CUSTOM_TAGS: Space-separated string of additional tags.
    :GIT_COMMIT: Git commit SHA. Should always be part of the build env.
    :VERSION: If version is not being set through another means, it can be
        passed in.

finalImageName
--------------

Function to prepend registry to image name (generally needed when using multiple
registries).

:Required Parameters:

    :imageName: Base image name.

:Environment Variables:

    :CONTAINER_PUSH_REGISTRY: The registry that the image is being pushed to.
