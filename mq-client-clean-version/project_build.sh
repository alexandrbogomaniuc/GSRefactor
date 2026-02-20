#!/bin/bash

# Function to list all modules
list_modules() {
    echo "Available modules:"
    for module in "${module_list[@]}"; do
        echo "- $module"
    done
}

# Function to update the version.js file with the current commit hash
update_version_file() {
    local module=$1
    local commit_hash=$(git rev-parse --verify HEAD)
    echo "Updating version.json for module: $module"
    echo $commit_hash
    for version_file in $(find "./$module" -name "version.json"); do
        jq '. += { "commitHash": "'"$commit_hash"'" }' "$version_file" > tmpfile && mv tmpfile "$version_file"
    echo "$(cat "$version_file")"
    done
}

# Function to display help
display_help() {
    echo "Usage: $0 [operation] [module_name|all]"
    echo ""
    echo "Operations:"
    echo "  install       Install dependencies for a module or all modules."
    echo "  build         Build a module or all modules."
    echo "  compress      Compress a module or all modules."
    echo "  list_modules  List all available modules."
    echo "  help          Display this help message."
    echo ""
    echo "Modules:"
    list_modules
    echo "- all (for all modules)"
    echo ""
    echo "PIXI selection (optional env vars):"
    echo "  PIXI_PROFILE=legacy|latest   (default: legacy)"
    echo "  PIXI_PACKAGE=<npm-package>   (overrides PIXI_PROFILE)"
    echo "  PIXI_VERSION=<version-tag>   (default: latest)"
}

# Define an array of module names
module_list=(
    "common"
    "cashorcrash"
    "dragonstone"
    "missionamazon"
    "sectorx"
    "sectorx_btg"
    "revengeofra"
)

PIXI_PROFILE=${PIXI_PROFILE:-legacy}
PIXI_PACKAGE=${PIXI_PACKAGE:-}
PIXI_VERSION=${PIXI_VERSION:-latest}

# Check if operation is 'help' or 'list_modules'
if [ "$1" == "help" ]; then
    display_help
    exit 0
elif [ "$1" == "list_modules" ]; then
    list_modules
    exit 0
fi

# Check for correct number of arguments
if [ "$#" -ne 2 ]; then
    display_help
    exit 1
fi

operation=$1
module=$2

install_selected_pixi_package() {
    local package_name=""
    local package_version="$PIXI_VERSION"

    if [ -n "$PIXI_PACKAGE" ]; then
        package_name="$PIXI_PACKAGE"
    elif [ "$PIXI_PROFILE" == "latest" ]; then
        package_name="pixi.js"
    else
        package_name="pixi.js-legacy"
    fi

    if [ "$package_name" == "pixi.js-legacy" ]; then
        echo "PIXI profile: legacy (using pixi.js-legacy from package.json)"
        return
    fi

    local target="$package_name@$package_version"
    echo "PIXI profile: $PIXI_PROFILE"
    echo "Installing selected PIXI package in ./common/PIXI: $target"
    npm install "$target" --no-save
}

# Function to install a module
install_module() {
    local module=$1
    echo "Installing module: $module"
    echo "------------------"
    echo "--> $module"
    echo "------------------"

    echo "Installing dependencies in ./common/PIXI"
    cd ./common/PIXI
    npm install
    install_selected_pixi_package
    cd ../..
     if [[ $module != *"common"* ]]; then
        pwd
        echo "Installing dependencies in $module/game"
        cd "./$module"/game || exit 1 # Move to the directory or exit if it fails
        pwd
        npm install
        if [[ $module != *"cashorcrash"* ]]; then
            echo "Installing dependencies in ./$module/lobby"
            cd ../lobby || exit 1 # Move to the lobby directory or exit if it fails
            npm install
            echo "Installing dependencies in ./$module/shared"
            cd ../shared || exit 1 # Move to the shared directory or exit if it fails
            npm install
        fi
        cd ../../ # Move back to the parent directory
    fi
}

# Function to build a module
build_module() {
    local module=$1
    echo "Building module: $module"
    echo "------------------"
    echo "--> $module"
    echo "------------------"

    if [[ $module != "common" ]]; then # Exclude common module from build
        echo "Building $module/game"
        cd "./$module"/game || exit 1
        if ! npm run build; then
            echo "Failed to build $module/game"
            exit 1
        fi
        if [[ $module != "cashorcrash" ]]; then
            echo "Building $module/lobby"
            cd ../lobby || exit 1
            if ! npm run build; then
                echo "Failed to build $module/lobby"
                exit 1
            fi
        fi
        cd ../../ # Move back to the parent directory
    fi
}

# Function to install a module
compress_module() {
    local module=$1
    echo "Compressing module: $module"
    echo "------------------"
    echo "--> $module"
    echo "------------------"
    if [[ $module == *"common"* ]]; then
        echo "Compressing dependencies in ./common/PIXI"
        cd ./common
        tar -czvf $(basename "$PWD").tar.gz ./assets/
        cd ..
    elif [[ $module == *"cashorcrash"* ]]; then
        pwd
        echo "Compressing dependencies in $module"
        cd "./$module"/ || exit 1 # Move to the directory or exit if it fails
        tar -czvf $(basename "$PWD").tar.gz ./game/dist/build/
        cd ..
    else
        echo "Compressing dependencies in ./$module"
        cd "./$module"/ || exit 1 # Move to the directory or exit if it fails
        tar -czvf $(basename "$PWD").tar.gz ./game/dist/build/ ./lobby/dist/build/
        cd ..
    fi
}

# Process a single module
process_module() {
    local module=$1
    if [ "$operation" == "install" ]; then
        install_module "$module"
    elif [ "$operation" == "build" ]; then
        build_module "$module"
    elif [ "$operation" == "compress" ]; then
        compress_module "$module"
    else
        echo "Unknown operation: $operation"
        exit 1
    fi
}


# Main logic
if [ "$module" == "all" ]; then
    for mod in "${module_list[@]}"; do
        process_module "$mod"
        update_version_file "$mod" 
    done
elif [[ " ${module_list[*]} " == *" $module "* ]]; then
    process_module "$module"
    update_version_file "$module" 
else
    echo "Unknown module: $module"
    exit 1
fi

echo "Completed $operation for $module"
