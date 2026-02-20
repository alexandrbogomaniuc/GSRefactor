const fs = require('fs');
const path = require('path');

const KNOWN_PROFILES = {
  legacy: 'pixi.js-legacy',
  latest: 'pixi.js'
};

function resolvePixiPackage(projectDir) {
  const profile = (process.env.PIXI_PROFILE || 'legacy').trim().toLowerCase();
  const explicitPackage = (process.env.PIXI_PACKAGE || '').trim();
  const packageName = explicitPackage || KNOWN_PROFILES[profile] || profile;

  const packageDir = path.resolve(projectDir, `../../common/PIXI/node_modules/${packageName}`);
  if (!fs.existsSync(packageDir)) {
    throw new Error(
      `[PIXI] Requested package '${packageName}' was not found at ${packageDir}. ` +
      `Run 'cd common/PIXI && npm install ${packageName}@latest --no-save' ` +
      `or use PIXI_PROFILE=legacy.`
    );
  }

  return packageDir;
}

module.exports = {
  resolvePixiPackage,
  KNOWN_PROFILES
};
