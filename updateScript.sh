#!/bin/bash

# Run the Maven properties update goal (show only property changes, with formatting)
echo -e "\033[1;34m==============================\033[0m"
echo -e "\033[1;34mMaven properties update\033[0m"
echo -e "\033[1;34m==============================\033[0m"

# Run Maven and format property lines
mvn -U -Dmaven.version.ignore='(?i).*-(alpha|beta|m|rc)([-.]?\d+)?' -DgenerateBackupPoms=false versions:update-properties 2>&1 | grep 'Property' | while read -r line; do
    echo -e "\033[1;32m[PROPERTY]\033[0m $line"
done

if [ ${PIPESTATUS[0]} -ne 0 ]; then
    echo -e "\033[1;31mERROR: Maven properties update failed.\033[0m"
    exit 1
fi


# Update Node.js LTS version (using jq for robust JSON parsing)

echo -e "\033[1;34m------------------------------\033[0m"
echo -e "\033[1;34mNode.js LTS version update\033[0m"
echo -e "\033[1;34m------------------------------\033[0m"
latest_node=$(curl -s https://nodejs.org/dist/index.json | jq -r '.[] | select(.lts != false) | .version' | head -n 1)
if [ -n "$latest_node" ]; then
    # Pour pom.xml, garder le 'v' devant
    sed -i '' "s|<node.lts.version>v[0-9.]*</node.lts.version>|<node.lts.version>${latest_node}</node.lts.version>|" pom.xml
    sed -i '' "s|<node.lts.version></node.lts.version>|<node.lts.version>${latest_node}</node.lts.version>|" pom.xml
    echo -e "\033[1;32m[UPDATED]\033[0m Node.js LTS version: ${latest_node} (pom.xml)"
    # Pour les workflows, toujours retirer le 'v' éventuel
    latest_node_nov=$(echo "$latest_node" | sed 's/^v//')
    for wf in .github/workflows/docker*.yml; do
        if grep -q 'node-version:' "$wf"; then
            sed -i '' "s/node-version: \"v[0-9.]*\"/node-version: \"${latest_node_nov}\"/g" "$wf"
            sed -i '' "s/node-version: \"[0-9.]*\"/node-version: \"${latest_node_nov}\"/g" "$wf"
            echo -e "\033[1;32m[UPDATED]\033[0m Node.js LTS version in $wf: ${latest_node_nov}"
        fi
    done
else
    echo -e "\033[1;31m[ERROR]\033[0m Could not fetch latest Node.js LTS version."
fi

# Update pnpm version (latest, using jq for robust JSON parsing)

echo -e "\033[1;34m------------------------------\033[0m"
echo -e "\033[1;34mpnpm version update\033[0m"
echo -e "\033[1;34m------------------------------\033[0m"
latest_pnpm=$(curl -s https://registry.npmjs.org/pnpm/latest | jq -r .version)
if [ -n "$latest_pnpm" ]; then
    sed -i '' "s|<pnpm.version>[0-9.]*</pnpm.version>|<pnpm.version>${latest_pnpm}</pnpm.version>|" pom.xml
    echo -e "\033[1;32m[UPDATED]\033[0m pnpm (LTS) version: ${latest_pnpm}"
    # Update pnpm version in GitHub Actions workflows
    for wf in .github/workflows/docker*.yml; do
        if grep -q 'npm install -g pnpm@' "$wf"; then
            sed -i '' "s/npm install -g pnpm@[0-9.]*/npm install -g pnpm@${latest_pnpm}/g" "$wf"
            echo -e "\033[1;32m[UPDATED]\033[0m pnpm version in $wf: ${latest_pnpm}"
        fi
    done
else
    echo -e "\033[1;31m[ERROR]\033[0m Could not fetch latest pnpm version."
fi

