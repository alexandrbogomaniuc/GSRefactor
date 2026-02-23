function parseBankFlags(raw) {
  const out = {};
  String(raw || '')
    .split(',')
    .map((v) => v.trim())
    .filter((v) => v.length > 0)
    .forEach((pair) => {
      const chunks = pair.split(':');
      if (chunks.length !== 2) {
        return;
      }
      const bankId = chunks[0].trim();
      const flag = chunks[1].trim().toLowerCase();
      if (!bankId) {
        return;
      }
      out[bankId] = flag === 'true' || flag === '1' || flag === 'yes';
    });
  return out;
}

function resolveBankMultiplayer(bankFlags, bankId) {
  if (!bankId) {
    return true;
  }
  if (Object.prototype.hasOwnProperty.call(bankFlags || {}, bankId)) {
    return Boolean(bankFlags[bankId]);
  }
  return true;
}

function routeDecision({ bankId, isMultiplayer, routeEnabled, canaryBanks, bankFlags }) {
  const requestedMultiplayer = Boolean(isMultiplayer);
  const normalizedBankId = String(bankId || '').trim();
  const canary = Array.isArray(canaryBanks) ? canaryBanks : [];
  const bankAllowsMultiplayer = resolveBankMultiplayer(bankFlags || {}, normalizedBankId);

  if (!requestedMultiplayer) {
    return {
      routeToMultiplayerService: false,
      reason: 'non_multiplayer_game',
      requestedMultiplayer,
      bankAllowsMultiplayer
    };
  }
  if (!bankAllowsMultiplayer) {
    return {
      routeToMultiplayerService: false,
      reason: 'bank_multiplayer_disabled',
      requestedMultiplayer,
      bankAllowsMultiplayer
    };
  }
  if (!routeEnabled) {
    return {
      routeToMultiplayerService: false,
      reason: 'route_disabled',
      requestedMultiplayer,
      bankAllowsMultiplayer
    };
  }
  if (!canary.includes(normalizedBankId)) {
    return {
      routeToMultiplayerService: false,
      reason: 'bank_not_in_canary',
      requestedMultiplayer,
      bankAllowsMultiplayer
    };
  }
  return {
    routeToMultiplayerService: true,
    reason: 'eligible',
    requestedMultiplayer,
    bankAllowsMultiplayer
  };
}

module.exports = {
  parseBankFlags,
  resolveBankMultiplayer,
  routeDecision
};
