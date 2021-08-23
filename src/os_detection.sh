#!/usr/bin/env bash

UNAME=$( command -v uname)

case $( "${UNAME}" | tr '[:upper:]' '[:lower:]') in
  linux*)
    printf 'linux\n'
    ;;
  darwin*)
    printf 'darwin\n'
    ;;
  msys*|cygwin*|mingw*)
    # or possible 'bash on windows'
    printf 'windows\n'

    ;;
  nt|win*)
    printf 'windows\n'
    ;;
  *)
    printf 'unknown\n'
    ;;
esac