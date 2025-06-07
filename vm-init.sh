#!/bin/bash

sudo apt update && sudo apt upgrade -y
exit

cd "${HOME}/radio-pele-java" &&
  git pull &&
  ./run.sh
