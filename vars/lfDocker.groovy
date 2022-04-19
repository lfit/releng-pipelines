// SPDX-License-Identifier: Apache-2.0
//
// Copyright (c) 2022 The Linux Foundation
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
 * Method to run Docker builds. Requires "Docker Pipeline" Jenkins plugin.
 *   Required body values:
 *     * mvnSettings: Maven settings config file ID
 *     * project: Name to be given to container
 *
 *   Optional body values (should be defined in lfDefaults):
 *     * containerPushRegistry: Override default registry to push built image.
 *
 * @param body Config values to be provided in the form "key = value".
 */
def call(body) {
    // Evaluate the body block and collect configuration into the object
    def defaults = lfDefaults()
    def config = [:]

    if (body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    // For duplicate keys, Groovy will use the right hand map's values.
    config = defaults + config

    if (!config.mvnSettings || !config.project) {
        throw new Exception("Maven settings file id (mvnSettings) and " +
            "project ID (project) are required for lfDocker function.")
    }
    if (config.containerPushRegistry) {
        env.CONTAINER_PUSH_REGISTRY = config.containerPushRegistry
    } else {
        config.containerPushRegistry = ""
    }

    lfCommon.containerRegistryLogin(config.mvnSettings)

    ///////////////////////////////////
    // Build/Verify Docker Container //
    ///////////////////////////////////
    dockerBuild(config.project)

    /////////////////////////////
    // Push container on merge //
    /////////////////////////////
    if (env.GIT_BRANCH == "main" || env.GIT_BRANCH == "master") {
        dockerPush(config.project, config.containerPushRegistry)
    }
}

/**
 * Function to build a docker image.
 *   Optional env variables:
 *     * DOCKER_BUILD_ARGS: Build-time arguments to pass to Docker.
 *     * DOCKER_BUILD_CONTEXT: Override default build context of "." (present working directory).
 *     * DOCKER_FILE_PATH: Override default path to Dockerfile.
 *
 * @param dockerImageName Image name to build
 */
def dockerBuild(dockerImageName) {
    def buildArgString = ""
    def dockerfile = "Dockerfile"
    def buildContext = "."

    if (env.DOCKER_BUILD_ARGS) {
        def buildArgs = ['']  // Start with blank entry
        env.DOCKER_BUILD_ARGS.split(',').each { buildArgs << it }
        buildArgString = buildArgs.join(' --build-arg ')
    }

    if (env.DOCKER_FILE_PATH) {
        dockerfile = env.DOCKER_FILE_PATH
    }

    if (env.DOCKER_BUILD_CONTEXT) {
        buildContext = env.DOCKER_BUILD_CONTEXT
    }

    docker.build(finalImageName(dockerImageName), "-f ${dockerfile} ${buildArgString} ${buildContext}")
}

/**
 * Function to push a docker image to a registry.
 *   Optional env variables:
 *     * CONTAINER_PUSH_REGISTRY: The registry that the image is being pushed to.
 *     * DOCKER_CUSTOM_TAGS: Space-separated string of additional tags.
 *     * GIT_COMMIT: Git commit SHA. Should always be part of the build env.
 *     * VERSION: If version is not being set through another means, it can be passed in.
 *
 * @param dockerImage Image name to push
 * @param registry Registry to push to
 * @param latest Boolean indicating if this push should be tagged "latest"
 * @param tags List of tags. Used in lieu of env.DOCKER_CUSTOM_TAGS
 */
def dockerPush(dockerImage, registry, latest = true, tags = null) {
    if (tags == null) {
        tags = getDockerTags(latest)
    }
    def image = docker.image(finalImageName(dockerImage))

    docker.withRegistry(registry) {
        tags.each {
            image.push(it)
        }
    }
}

/**
 * This function polls multiple possible sources for Docker tags, compiling them
 * and returning all tags in a single list.
 *   Optional env variables:
 *     * DOCKER_CUSTOM_TAGS: Space-separated string of additional tags.
 *     * GIT_COMMIT: Git commit SHA. Should always be part of the build env.
 *     * VERSION: If version is not being set through another means, it can be passed in.
 *
 * @param latest Boolean to indicate if this should be tagged "latest"
 * @param customTags Space-separated string of additional tags
 */
def getDockerTags(latest = true, customTags = env.DOCKER_CUSTOM_TAGS) {
    def allTags = []

    if (env.GIT_COMMIT) {
        allTags << "${env.GIT_COMMIT}"
    }

    if (latest) {
        allTags << "latest"
    }

    if (env.VERSION) {
        allTags << env.VERSION
    }

    if (env.GIT_COMMIT && env.VERSION) {
        allTags << "${env.GIT_COMMIT}-${env.VERSION}"
    }

    if (customTags) {
        customTags.split(' ').each {
            allTags << it
        }
    }

    return allTags
}

/**
 * Function to prepend registry to image name (generally needed when using
 * multiple registries).
 *   Optional env variables:
 *     * CONTAINER_PUSH_REGISTRY: The registry that the image is being pushed to.
 *
 * @param imageName Base image name
 */
def finalImageName(imageName) {
    def finalDockerImageName = imageName

    // prepend with registry "namespace" if not empty
    if (env.CONTAINER_PUSH_REGISTRY && env.CONTAINER_PUSH_REGISTRY != '/') {
        finalDockerImageName = "${env.CONTAINER_PUSH_REGISTRY}/${finalDockerImageName}"
    }

    finalDockerImageName
}
