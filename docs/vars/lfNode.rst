######
lfNode
######

Parameters
==========

:Optional Parameters:

    :nodeDir: Root of NodeJS project.
    :nodeVersion: The version of NodeJS to use. This must be installed in
        Jenkins' global tools.

Usage
=====

Calling lfNode will execute a simple ``npm install`` followed by ``npm test`` in
order to validate the code.
