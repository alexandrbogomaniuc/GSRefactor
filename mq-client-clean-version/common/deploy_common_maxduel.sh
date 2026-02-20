#!/bin/bash
echo "Start deployment"

# Check if /home/bitbucket/common directory exists
if [ ! -d "/home/bitbucket/common" ]; then
    echo "Directory /home/bitbucket/common does not exist"
    exit 1
fi

cd /home/bitbucket/common || { echo "Failed to change directory"; exit 1; }

# Check if assets/ directory exists before removing
if [ -d "assets/" ]; then
    echo "Remove game and lobby directories"
    rm -r assets/ || { echo "Failed to remove directories"; exit 1; }
else
    echo "assets/ directory does not exist"
fi

echo "Untar common.tar.gz"
tar -xzvf common.tar.gz || { echo "Failed to extract archive"; exit 1; }

# Check if target deployment directory exists before removing files
if [ -d "/www/html/repo/html5pc/actiongames/common/assets/" ]; then
    echo "Remove files from deployed directories"
    sudo rm -rf /www/html/repo/html5pc/actiongames/common/assets/* || { echo "Failed to remove files"; exit 1; }
else
    echo "Deploy directory /www/html/repo/html5pc/actiongames/common/assets/ does not exist"
    exit 1
fi

echo "Copy files to deploy directories"
sudo cp -r ./assets/* /www/html/repo/html5pc/actiongames/common/assets/ || { echo "Failed to copy files"; exit 1; }

echo "Restart Apache"
sudo systemctl restart apache2 || { echo "Failed to restart Apache"; exit 1; }

echo "Done!"
