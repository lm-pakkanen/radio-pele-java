#!/bin/bash

sudo apt update && sudo apt upgrade -y

cd "${HOME}/radio-pele-java" &&
  git pull &&
  ./run-artifactory.sh
