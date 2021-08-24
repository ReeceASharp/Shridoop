#!/bin/bash
#Useful Links
# https://man7.org/linux/man-pages/man1/tmux.1.html
# https://ryan.himmelwright.net/post/scripting-tmux-workspaces/


SESSION="Distributed"

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
echo "SCRIPT DIR: $SCRIPT_DIR"

# Dimensions of Grid of terminals, and percentage of entire terminal
MATRIX_WIDTH=3
MATRIX_HEIGHT=3
MATRIX_PERCENT=70

# Contents can be edited, and these commands will be ran inside their respective terminal
top_left() {
  echo "${SCRIPT_DIR}"
  cd "${SCRIPT_DIR}"
  java -cp target/Distributed_File_System-*.jar fileSystem.node.Controller 7000
}

top_right() {
  echo "${SCRIPT_DIR}"
  cd "${SCRIPT_DIR}"
  clear
  sleep 5
  java -cp target/Distributed_File_System-*.jar fileSystem.node.Client localhost 7000
}

matrix() {
  echo "${SCRIPT_DIR}"
  cd "${SCRIPT_DIR}"
  echo 'Waiting 5 seconds for the Controller to start up'
  sleep 5
  clear
  java -cp target/Distributed_File_System-*.jar fileSystem.node.ChunkServer localhost 7000
}


# Converts the contents of a function to a string, which allows it to be sent to a tmux session
function_contents() {
  while [ "$1" ];  do
    type "$1" | sed  -n '/^    /{s/^    //p}' | sed '$s/.*/&;/' ; shift
  done
}

# Dump the functions above to a string with each command delimited by semicolons to be sent to a tmux
# Also: Remove ending semicolon so other parameters (like the matrix loop number) can be used as arguments
COMMAND_TOP_LEFT="$(function_contents top_left)"
COMMAND_TOP_LEFT=${COMMAND_TOP_LEFT:P:L-1}
COMMAND_TOP_RIGHT="$(function_contents top_right)"
COMMAND_TOP_RIGHT=${COMMAND_TOP_RIGHT:P:L-1}
COMMAND_MATRIX="$(function_contents matrix)"
COMMAND_MATRIX=${COMMAND_MATRIX:P:L-1}

if [ "$(tmux list-sessions | grep $SESSION)" == "" ]
then
  tmux new-session -d -s $SESSION -x "$(tput cols)" -y "$(tput lines)"
  tmux splitw -v -p $MATRIX_PERCENT

  # ROW SETUP
  # ***************************
  for (( x = 1; x < MATRIX_HEIGHT; x++ ))
  do
    height_percent=$(( (100 / (MATRIX_HEIGHT + 1 - x) ) * (MATRIX_HEIGHT - x) ))
    tmux splitw -v -p $height_percent
  done
  # ***************************

  # COLUMN SETUP
  # Build out each row, and execute the script for each pane
  # Note: This can't be cone inside of the row setup because of the recursive nature of tmux's pane-split functionality

  # Top Row
  tmux select-pane -t 0
  tmux splitw -h -p 50

 # Chunk Array
  for (( x = 0; x < MATRIX_HEIGHT; x++ ))
  do
    tmux select-pane -t '{down-of}'
    for (( y = 1; y < MATRIX_WIDTH; y++ ))
    do
      WIDTH_PERCENT=$(( (100 / ( MATRIX_WIDTH + 1 - y) ) * (MATRIX_WIDTH - y) ))
      tmux splitw -h -p $WIDTH_PERCENT
    done
  done


  # COMMAND DEPLOY
  # Top Row
  tmux send-keys -t 0 "$COMMAND_TOP_LEFT" Enter
  tmux send-keys -t 1 "$COMMAND_TOP_RIGHT" Enter

  tmux select-pane -t 2

  # MATRIX SETUP
  readarray array_dump < "$SCRIPT_DIR/chunkServers_linux"

  # Matrix
  for (( i = 0; i < MATRIX_HEIGHT*MATRIX_WIDTH; i++ ))
  do
    tmux send-keys "$COMMAND_MATRIX" " ${array_dump[i]}" Enter
    tmux select-pane -t +1
  done
fi

# Start up a client, and focus on it
tmux select-pane -t 1
tmux attach-session -t $SESSION


