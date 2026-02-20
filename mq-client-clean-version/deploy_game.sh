#!/bin/bash

# Define an array of valid directory names
valid_dirs=("cashorcrash" "dragonstone" "missionamazon" "revengeofra" "sectorx" "sectorx_btg")

# Check if the directory name is provided and is valid
if [ "$#" -ne 1 ] || [[ ! " ${valid_dirs[@]} " =~ " $1 " ]]; then
    echo "Usage: $0 [directory_name]"
    echo "directory_name must be one of: ${valid_dirs[*]}"
    exit 1
fi

dir_name=$1

echo "Start deployment"

# Change to the /home/users/bitbucket/$dir_name directory
cd /home/users/bitbucket/"$dir_name" || { echo "Failed to change directory to $dir_name"; exit 1; }

# Check if game/ and lobby/ directories exist before removing
for sub_dir in game lobby; do
    if [ -d "$sub_dir/" ]; then
        echo "Remove $sub_dir directory"
        rm -r "$sub_dir"/ || { echo "Failed to remove $sub_dir directory"; exit 1; }
    else
        echo "$sub_dir/ directory does not exist"
    fi
done

echo "Untar $dir_name.tar.gz"
tar -xzvf "$dir_name".tar.gz || { echo "Failed to extract archive"; exit 1; }

# Check if the target deployment directories exist before removing files
for sub_dir in game lobby; do
    if [ -d "/www/html/repo/html5pc/actiongames/$dir_name/$sub_dir/" ]; then
        echo "Remove files from $sub_dir deployed directory"
        sudo rm -r /www/html/repo/html5pc/actiongames/"$dir_name"/"$sub_dir"/* || { echo "Failed to remove $sub_dir files"; exit 1; }
    else
        echo "Deploy directory /www/html/repo/html5pc/actiongames/$dir_name/$sub_dir/ does not exist"
    fi
done

echo "Copy files to deploy directories"

# Check if the game build directory exists before copying
if [ -d "./game/dist/build/" ]; then
    sudo cp -r ./game/dist/build/* /www/html/repo/html5pc/actiongames/"$dir_name"/game/ || { echo "Failed to copy game files"; exit 1; }
else
    echo "Game build directory does not exist"
fi

# Check if the lobby build directory exists before copying
if [ -d "./lobby/dist/build/" ]; then
    sudo cp -r ./lobby/dist/build/* /www/html/repo/html5pc/actiongames/"$dir_name"/lobby/ || { echo "Failed to copy lobby files"; exit 1; }
else
    echo "Lobby build directory does not exist"
fi

echo "Restart Apache"
sudo systemctl restart apache2 || { echo "Failed to restart Apache"; exit 1; }

echo "Done!"