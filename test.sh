#!/usr/bin/env bash

# Note: this is only for Windows, assuming git is installed
# This script simply opens up a few terminals and simulates a distributed environment
# This is also being ran through a Unix-style terminal
# I don't have access to a cluster, so this will simulate it locally

# Controller
/git-bash.exe -li -c "java -cp target/*.jar fileSystem.node.controller.Controller" &

# Chunk Servers
CHUNK_SERVERS=3
for (( i = 0; i < CHUNK_SERVERS; i++ )); do
    /git-bash.exe -li -c "java -cp target/*.jar fileSystem.node.server.ChunkServer" &
done

# Client(s)
CLIENTS=1
for (( i = 0; i < CLIENTS; i++ )); do
    /git-bash.exe -li -c "java -cp target/*.jar fileSystem.node.client.Client" &
done