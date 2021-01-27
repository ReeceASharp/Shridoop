# Reads the file, and given a port and name to register with, will start a ChunkServer
# listening on that port. Used by the Controller
# Note: The name is just an alias used for easier reference
# TODO: Eventually add the ability to not run a terminal for the ChunkServers, and just a process instead

while IFS=' ' read -ra CHUNK;
do
  #ignore comments in file
  [[ "$CHUNK" =~ ^#.*$ ]] && continue

  # output data for
  echo GO: "$1", CONNECTION: "${CHUNK[0]}", NAME: "${CHUNK[1]}"

  #start up a server
  /git-bash.exe -li -c "java -cp target/Distributed_File_System-1.0.jar fileSystem.node.server.ChunkServer ${CHUNK[0]} $1 ${CHUNK[1]}" &
done < ./chunkServers