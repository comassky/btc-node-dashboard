#!/bin/bash

set -e

echo -e "\033[1;34m==============================\033[0m"
echo -e "\033[1;34mDependency update\033[0m"
echo -e "\033[1;34m==============================\033[0m"

# Le filtre ultime :
# 1. 2>&1 fusionne la sortie standard et les erreurs
# 2. sed -e 's/\r//g' enlève les retours chariot
# 3. sed -E "/\[=+.*\]/d" supprime les lignes avec [=======]
FILTER="2>&1 | sed -e 's/\r//g' | sed -E '/\[=+.*\]/d' | grep -v 'All dependencies match'"

echo -e "\033[1;36m[INFO]\033[0m Updating all dependencies except tailwindcss..."
eval "ncu -u --reject tailwindcss --loglevel warn $FILTER" && \
    echo -e "\033[1;32m[SUCCESS]\033[0m Dependencies (except tailwindcss) updated." || \
    echo -e "\033[1;31m[ERROR]\033[0m Failed to update dependencies (except tailwindcss)."

echo -e "\033[1;34m------------------------------\033[0m"

echo -e "\033[1;36m[INFO]\033[0m Updating tailwindcss (minor only, v3)..."
eval "ncu -u --target minor --filter tailwindcss --loglevel warn $FILTER" && \
    echo -e "\033[1;32m[SUCCESS]\033[0m tailwindcss updated (minor)." || \
    echo -e "\033[1;31m[ERROR]\033[0m Failed to update tailwindcss."