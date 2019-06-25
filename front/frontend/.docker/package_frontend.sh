#!/bin/sh

#
MODULE_NAME=frontend
PROJECT_VERSION=$(cat "package.json" | grep '"version"' | cut -d'"' -f4 | sed -e "s/\.0-SNAPSHOT/-SNAPSHOT/g" | sed -e "s/\.0$//g")

# Image name for a branch
if [ "$CI_BUILD_REF_NAME" != "master" ]; then
    # Use the Gitlab issue id
    ISSUE_ID=$(echo "$CI_BUILD_REF_NAME" | grep -Eo '^\d+-' | cut -d- -f1)
    if [ "$ISSUE_ID" != "" ]; then
        IMAGE_NAME="$CI_REGISTRY_IMAGE/$MODULE_NAME:b$ISSUE_ID"
    # Use branch name otherwise
    else
        IMAGE_NAME="$CI_REGISTRY_IMAGE/$MODULE_NAME:b$CI_BUILD_REF_NAME"
    fi
# Image name for master : same build for snapshot and release (simply use current project version)
else
    IMAGE_NAME="$CI_REGISTRY_IMAGE/$MODULE_NAME:$PROJECT_VERSION"
fi

# Copy Dockerfile and move to webapp directory
cp .docker/Dockerfile.frontend Dockerfile
cp .docker/deploy-front.sh .
cp .docker/root.conf .
cp .docker/root_location.conf .

# Build Docker image
echo "Building image $IMAGE_NAME..."
docker build -t $IMAGE_NAME .

# Login to remote registry
echo "Login to remote registry..."
docker login -u gitlab-ci-token -p $CI_BUILD_TOKEN $CI_REGISTRY

# Push image to remote repo
echo "Pushing image $IMAGE_NAME..."
docker push $IMAGE_NAME
