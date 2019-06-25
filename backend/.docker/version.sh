#!/bin/sh

# Image name for a branch
if [ "$CI_BUILD_REF_NAME" != "master" ]; then
    # Use the Gitlab issue id
    ISSUE_ID=$(echo "$CI_BUILD_REF_NAME" | grep -Eo '^\d+-' | cut -d- -f1)
    if [ "$ISSUE_ID" != "" ]; then
        IMAGE_NAME="$CI_REGISTRY_IMAGE:$MODULE_NAME-b$ISSUE_ID"
    # Use branch name otherwise
    else
        IMAGE_NAME="$CI_REGISTRY_IMAGE:$MODULE_NAME-b$CI_BUILD_REF_NAME"
    fi
# Image name for master : same build for snapshot and release (simply use current project version)
else
    IMAGE_NAME="$CI_REGISTRY_IMAGE:$MODULE_NAME-$PROJECT_VERSION"
fi
