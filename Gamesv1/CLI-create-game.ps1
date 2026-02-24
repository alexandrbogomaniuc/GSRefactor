param (
    [Parameter(Mandatory=$true)]
    [string]$GameName,

    [Parameter(Mandatory=$true)]
    [string]$GameId
)

$SourceTemplate = ".\slot-template"
$TargetDir = ".\$GameName"

Write-Host "🎰 GS Platform: Scaffolding new game '$GameName'..." -ForegroundColor Cyan

# 1. Check if Template exists
if (-Not (Test-Path $SourceTemplate)) {
    Write-Error "Could not find the 'slot-template' directory. Please run this script from the Gamesv1 folder."
    exit
}

# 2. Check if Target already exists
if (Test-Path $TargetDir) {
    Write-Error "A directory named '$GameName' already exists. Aborting."
    exit
}

# 3. Copy Directory
Write-Host "Copying boilerplate files..."
Copy-Item -Path $SourceTemplate -Destination $TargetDir -Recurse

# 4. Update Package.json (if exists)
$PackageJsonPath = "$TargetDir\package.json"
if (Test-Path $PackageJsonPath) {
    Write-Host "Updating local package.json..."
    $json = Get-Content $PackageJsonPath | ConvertFrom-Json
    $json.name = $GameName.ToLower()
    $json | ConvertTo-Json -Depth 10 | Set-Content $PackageJsonPath
}

# 5. Clean up node_modules in copied folder to ensure clean install (if it accidentally copied)
$NodeModules = "$TargetDir\node_modules"
if (Test-Path $NodeModules) {
    Write-Host "Cleaning up cached node_modules..."
    Remove-Item -Path $NodeModules -Recurse -Force
}

# 6. Optional: Overwrite Game ID in Configs (if master-game-config.json exists)
$ConfigPath = "$TargetDir\public\master-game-config.json"
if (Test-Path $ConfigPath) {
    Write-Host "Setting internal Game ID to '$GameId'..."
    $configJson = Get-Content $ConfigPath | ConvertFrom-Json
    $configJson.gameId = $GameId
    $configJson | ConvertTo-Json -Depth 10 | Set-Content $ConfigPath
}

Write-Host "✅ Success! New game project created at: $TargetDir" -ForegroundColor Green
Write-Host "Next steps:"
Write-Host "  cd $GameName"
Write-Host "  npm install"
Write-Host "  npm run dev"
