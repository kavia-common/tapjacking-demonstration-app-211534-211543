#!/bin/bash
cd /home/kavia/workspace/code-generation/tapjacking-demonstration-app-211534-211543/tapjacking_frontend
./gradlew lint
LINT_EXIT_CODE=$?
if [ $LINT_EXIT_CODE -ne 0 ]; then
   exit 1
fi

