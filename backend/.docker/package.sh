#!/bin/sh

# Login to remote registry
echo "Login to remote registry..."
docker login -u gitlab-ci-token -p $CI_JOB_TOKEN $CI_REGISTRY

build_docker(){
    local FILE_PATH=$1
    local FILE_NAME=$2
    local IMAGE_NAME=$3

    echo "Docker context path $FILE_PATH - artifact $FILE_NAME"
    cp .docker/Dockerfile.springboot $FILE_PATH
    cp .docker/start-springboot.sh $FILE_PATH

    # Build Docker image
    echo "Building image $IMAGE_NAME..."
    docker build --build-arg JAR_FILE=$FILE_NAME -t $IMAGE_NAME -f $FILE_PATH/Dockerfile.springboot $FILE_PATH

        # Push image to remote repo
    echo "Pushing image $IMAGE_NAME..."
    docker push $IMAGE_NAME
}


pids=""
RESULT=0

# For each final artifact
for file in $(find . -name "*.jar" | grep -v "gradle-wrapper")
do

    # Extract module info
    FILENAME=$(echo "$file" | grep -Eo '/[^/]+$' | cut -d/ -f2 | sed -n -e 's/\.jar//p')
    PROJECT_VERSION=$(echo $FILENAME | grep -Eo '\-\d.+' | cut -d- -f2-3)
    MODULE_NAME=$(echo $FILENAME | sed -n -e "s/-$PROJECT_VERSION//p")

    FILE_PATH=$(dirname "${file}")
    FILE_NAME=$(basename "${file}")

    # Compute docker $IMAGE_NAME
    . ./.docker/version.sh

    build_docker $FILE_PATH $FILE_NAME $IMAGE_NAME &
    pids="$pids $!"
done

for pid in $pids; do
    wait $pid || let "RESULT=1"
done

if [ "$RESULT" == "1" ];
    then
       exit 1
fi
