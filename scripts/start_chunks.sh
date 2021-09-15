#!/bin/bash
# Reads the file, and given a port and name to register with, will start a ChunkServer
# listening on that port. Used by the Controller
# Note: The name is just an alias used for easier reference
# TODO: Eventually add the ability to not run a terminal for the ChunkServers, and just a process instead

while IFS=' ' read -ra CHUNK;
do
  #ignore comments in file
  [[ "$CHUNK" =~ ^#.*$ ]] && continue

  #start up a server
  /git-bash.exe -li -c "java -cp target/Distributed_File_System-1.0.jar fileSystem.node.ChunkServer $1 $2 ${CHUNK[0]} ${CHUNK[1]} ${CHUNK[2]}" &
done < ./chunkServers
