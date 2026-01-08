#!/bin/bash

# Colors
CYAN='\033[1;36m'
GREEN='\033[1;32m'
RED='\033[1;31m'
YELLOW='\033[1;33m'
BLUE='\033[1;34m'
RESET='\033[0m'

# Print header
print_header() {
    echo -e "\n${CYAN}╔════════════════════════════════════════════╗${RESET}"
    echo -e "${CYAN}║${RESET}  🔄  ${BLUE}BTC Node Dashboard Update${RESET}  ${CYAN}║${RESET}"
    echo -e "${CYAN}╚════════════════════════════════════════════╝${RESET}\n"
}

# Print section
print_section() {
    echo -e "\n${BLUE}┌─────────────────────────────────────────┐${RESET}"
    echo -e "${BLUE}│${RESET}  $1"
    echo -e "${BLUE}└─────────────────────────────────────────┘${RESET}"
}

# Print success
print_success() {
    echo -e "${GREEN}  ✓${RESET} $1"
}

# Print error
print_error() {
    echo -e "${RED}  ✗${RESET} $1"
}

# Print warning
print_warning() {
    echo -e "${YELLOW}  ⚠${RESET} $1"
}

print_header

# Update Maven properties (Quarkus, plugins, etc.)
print_section "🔧 Maven Properties & Plugins Update"

# Run Maven versions update for properties
props_output=$(mvn -U -Dmaven.version.ignore='(?i).*-(alpha|beta|m|rc)([-.]?\d+)?' -DgenerateBackupPoms=false versions:update-properties 2>&1)

# Display property updates
echo "$props_output" | grep '\[INFO\] Property' | while read -r line; do
    if [[ $line == *"Leaving unchanged"* ]]; then
        version=$(echo "$line" | sed -n 's/.*as \([0-9.]*\).*/\1/p')
        prop=$(echo "$line" | sed -n 's/.*Property \(\${[^}]*}\).*/\1/p')
        echo -e "  ${CYAN}→${RESET} $prop: ${GREEN}$version${RESET}"
    elif [[ $line == *"Updating"* ]]; then
        old_version=$(echo "$line" | sed -n 's/.*from \([0-9.]*\).*/\1/p')
        new_version=$(echo "$line" | sed -n 's/.*to \([0-9.]*\).*/\1/p')
        prop=$(echo "$line" | sed -n 's/.*Property \(\${[^}]*}\).*/\1/p')
        echo -e "  ${GREEN}✓${RESET} $prop: ${YELLOW}$old_version${RESET} → ${GREEN}$new_version${RESET}"
    fi
done

if [ ${PIPESTATUS[0]} -ne 0 ]; then
    print_error "Maven properties update failed"
    exit 1
fi


# Update Node.js LTS version (using jq for robust JSON parsing)

print_section "🟢 Node.js LTS Version Update"
latest_node=$(curl -s https://nodejs.org/dist/index.json | jq -r '.[] | select(.lts != false) | .version' | head -n 1)
if [ -n "$latest_node" ]; then
    # Update pom.xml with 'v' prefix
    sed -i '' "s|<node.lts.version>v[0-9.]*</node.lts.version>|<node.lts.version>${latest_node}</node.lts.version>|" pom.xml
    sed -i '' "s|<node.lts.version></node.lts.version>|<node.lts.version>${latest_node}</node.lts.version>|" pom.xml
    print_success "Node.js LTS: ${GREEN}${latest_node}${RESET}"
else
    print_error "Could not fetch latest Node.js LTS version"
fi

# Update pnpm version (latest, using jq for robust JSON parsing)

print_section "📦 pnpm Version Update"
latest_pnpm=$(curl -s https://registry.npmjs.org/pnpm/latest | jq -r .version)
if [ -n "$latest_pnpm" ]; then
    sed -i '' "s|<pnpm.version>[0-9.]*</pnpm.version>|<pnpm.version>${latest_pnpm}</pnpm.version>|" pom.xml
    print_success "pnpm: ${GREEN}${latest_pnpm}${RESET}"
else
    print_error "Could not fetch latest pnpm version"
fi

# Synchronize documentation and workflows with pom.xml versions
print_section "📝 Documentation Synchronization"
if [ -f "update-docs.mjs" ]; then
    node update-docs.mjs 2>&1 | while IFS= read -r line; do
        if [[ $line == *"✅"* ]]; then
            echo -e "  ${GREEN}✓${RESET} ${line#*✅ }"
        elif [[ $line == *"📦"* ]] || [[ $line == *"Backend:"* ]] || [[ $line == *"Frontend:"* ]]; then
            # Skip verbose output
            :
        elif [[ $line == *"✨"* ]]; then
            echo -e "\n  ${GREEN}✨${RESET} ${line#*✨ }"
        else
            echo "$line"
        fi
    done
    
    if [ ${PIPESTATUS[0]} -eq 0 ]; then
        print_success "All documentation synchronized"
    else
        print_error "Failed to synchronize documentation"
    fi
else
    print_warning "update-docs.mjs not found, skipping documentation sync"
fi

# Update frontend dependencies with npm-check-updates
print_section "📦 Frontend Dependencies Update (npm-check-updates)"
if [ -d "src/main/web" ]; then
    cd src/main/web
    if command -v ncu &> /dev/null; then
        ncu_output=$(ncu -u --loglevel warn 2>&1)
        if [ -n "$ncu_output" ]; then
            echo "$ncu_output" | while IFS= read -r line; do
                if [[ $line == *"→"* ]]; then
                    echo -e "  ${GREEN}✓${RESET} $line"
                else
                    echo -e "  ${CYAN}→${RESET} $line"
                fi
            done
        else
            echo -e "  ${CYAN}→${RESET} All frontend dependencies are up to date"
        fi
    else
        print_warning "ncu (npm-check-updates) not found, skipping frontend updates"
        print_warning "Install with: npm install -g npm-check-updates"
    fi
    cd ../../../
else
    print_warning "src/main/web directory not found"
fi

echo -e "\n${CYAN}╔════════════════════════════════════════════╗${RESET}"
echo -e "${CYAN}║${RESET}  ${GREEN}✓${RESET} ${GREEN}Update completed successfully!${RESET}       ${CYAN}║${RESET}"
echo -e "${CYAN}╚════════════════════════════════════════════╝${RESET}\n"

