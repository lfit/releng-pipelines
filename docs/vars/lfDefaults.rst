##########
lfDefaults
##########

Usage
=====

These are default values for variables that are frequently used by functions
and scripts within the pipeline library. Calling lfDefaults() will return a map
of the default values. If combining with a user-specified config map, the
user-specified values should be on the right-hand side of the addition, as this
will be the values used in case of a collision.

.. code-block:: groovy

    def defaults = lfDefaults()
    def config = [:]

    if (body) {
        body.resolveStrategy = Closure.DELEGATE_FIRST
        body.delegate = config
        body()
    }

    config = defaults + config
