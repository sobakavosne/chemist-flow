#!/bin/sh

# Run scalafmt check
echo "Running scalafmt check..."
sbt scalafmtCheckAll

# Capture the exit code of scalafmtCheckAll
EXIT_CODE=$?

if [ $EXIT_CODE -ne 0 ]; then
  echo "Code format violations detected. Please run 'sbt scalafmtAll' to format your code."
  exit 1
fi

echo "Code format check passed. Proceeding with push."
exit 0
