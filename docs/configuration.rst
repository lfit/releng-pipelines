#############
Configuration
#############

In order to use the LF RelEng Pipelines Library, it simply needs to be imported
by adding to the Pipeline Libraries in Jenkins. This can be done in the global
Jenkins settings under Global Pipeline Libraries, in individual job settings,
or in the GitHub Organization config, if using the GitHub plugin for an entire
org.

.. warning::

   Global Pipeline Libraries imported in the global Settings do not run inside
   of Jenkins' groovy sandbox. This can lead to security risks, and it is
   recommended that the library be imported at a more specific level.

With the library imported, all functions in the ``vars`` directory can be called
directly in Jenkinsfile, without the need for further imports. For a more
detailed explanation of pipeline libraries, please see the
`Jenkins Shared Library docs
<https://www.jenkins.io/doc/book/pipeline/shared-libraries/#using-libraries>`_.
