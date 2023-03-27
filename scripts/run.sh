#!/bin/bash

# Note this is ran through the make-file. It is not meant to be ran directly

# Check how many arguments were given, if more than 2 exit
if [ $# -ne 2 ]; then
    echo "Usage: ./run.sh <env file path> <controller|chunk_holder|client|infra>"
    echo "Given arguments: $@"
    exit 1
fi

# Read in the first two arguments, sanitize the NODE_TYPE and save them as variables
ENV_FILE=$1
NODE_TYPE=$(echo $2 | tr '[:upper:]' '[:lower:]')
EXECUTABLE_CLASS="fileSystem.node.$NODE_TYPE"
NAME_PREPEND="shridoop" # used to name the docker containers

# Note: The order is specific, the controller must be started first as the chunk_holders connect to it

# CONTROLLER / Infrastructure
if [ $NODE_TYPE = "controller" ] || [ $NODE_TYPE = "infra" ]; then
    # Run the controller
    docker run \
        -d \
        --name "${NAME_PREPEND}-controller" \
        --network=$DOCKER_NETWORK_NAME \
        --ip=$CONTROLLER_HOST \
        --env IP_ADDRESS=$CONTROLLER_HOST \
        --env-file=$ENV_FILE \
        $DOCKER_IMAGE \
        filesystem.node.Controller
fi

# CHUNK_HOLDER / Infrastructure
if [ $NODE_TYPE = "chunk_holder" ] || [ $NODE_TYPE = "infra" ]; then

    # read in CHUNK_CONFIG_FILE, and for each line, start a chunk_holder
    while IFS=' ' read -ra CHUNK; do
        #ignore comments in file
        [[ "$CHUNK" =~ ^#.*$ ]] && continue
        #start up a server
        docker run \
            -d \
            --name "${NAME_PREPEND}-chunk_holder-${CHUNK[1]}" \
            --network=$DOCKER_NETWORK_NAME \
            --ip ${CHUNK[0]} \
            --env IP_ADDRESS=${CHUNK[0]} \
            --env-file=$ENV_FILE \
            $DOCKER_IMAGE \
            filesystem.node.ChunkHolder
    done < $CHUNK_CONFIG_FILE
fi

# CLIENT
if [ $NODE_TYPE = "client" ]; then
    # The chances of this overlapping are super low, so I'm just going to leave it
    _number=$(shuf -i 0-1000000 -n 1)
    # generate a random ip for the client to using the second half of the subnet, hardcoded ip subnet for right now
    _ip=$(printf "10.0.1.%d" "$((RANDOM % 126 + 126 ))")

    # run the client
    docker run \
        --name "${NAME_PREPEND}-client-${random_number}" \
        --network=$DOCKER_NETWORK_NAME \
        --ip $_ip \
        $DOCKER_IMAGE \
        filesystem.node.Client

fi