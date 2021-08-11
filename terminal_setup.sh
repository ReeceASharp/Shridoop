#!/bin/bash
#Useful Links
# https://man7.org/linux/man-pages/man1/tmux.1.html
# https://ryan.himmelwright.net/post/scripting-tmux-workspaces/


SESSION="Distributed"

SCRIPT_DIR=$(dirname "$(readlink -f "$0")")
COMMAND_TOP_LEFT="java -cp target/*.jar fileSystem.node.Controller"
COMMAND_TOP_RIGHT="java -cp target/*.jar fileSystem.node.Client"
COMMAND_MATRIX="java -cp target/*.jar fileSystem.node.ChunkServer"

# Dimensions of Grid of terminals, and percentage of entire terminal
MATRIX_WIDTH=3
MATRIX_HEIGHT=2
MATRIX_PERCENT=70



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


  # Send Commands
  # Top Row
  tmux send-keys -t 0 "$COMMAND_TOP_LEFT" Enter

  tmux send-keys -t 1 "$COMMAND_TOP_RIGHT" Enter

  tmux select-pane -t 2
  for (( i = 0; i < MATRIX_HEIGHT*MATRIX_WIDTH; i++ ))
  do
    tmux send-keys "$COMMAND_MATRIX $(( 6000 + i ))" Enter
    tmux select-pane -t +1
  done
fi

# Start up a client, and focus on it
tmux attach-session -t $SESSION


