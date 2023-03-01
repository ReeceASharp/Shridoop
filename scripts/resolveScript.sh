#!/bin/bash
      ## -*-Shell-Script-*-
CmdName=${0##*/}
Usage="usage: $CmdName runs envsubst, but allows '\$' to  keep variables from
    being expanded.
  With option   -sl   '\$' keeps the back-slash.
  Default is to replace  '\$' with '$'
"

if [[ $1 = -h ]]  ;then echo -e >&2  "$Usage" ; exit 1 ;fi
if [[ $1 = -sl ]] ;then  sl='\'  ; shift ;fi

sed 's/\\\$/\${EnVsUbDolR}/g' |  EnVsUbDolR=$sl\$  envsubst  "$@"
