import SimpleInfo from '../base/SimpleInfo';
import { APP } from '../../controller/main/globals';

/**
 * @typedef Avatar
 * @type {Object}
 * @property {number} border - Avatar border id
 * @property {number} hero - Avatar head id
 * @property {number} background - Avatar background id
 */

/**
 * @typedef RoomObserver
 * @type {Object}
 * @property {String} nickname - Player nickname
 * @property {Boolean} isKicked - Player isKicked state
 */

/**
 * @typedef RoomSeater
 * @type {Object}
 * @property {String} nickname - Player nickname
 * @property {Number} seatId - Player seat id
 */

/**
 * @class
 * @classdesc Stores player fata
 */
class PlayerInfo extends SimpleInfo
{
	static get DEFAULT_NICK_GLYPHS()				{return "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789"};

	static get KEY_UNPRESENTED_WIN()				{return "unpresentedWin"};
	static get KEY_QUALIFY_WIN() 					{return "qualifyWin"};
	static get KEY_LEVEL() 							{return "level"};
	static get KEY_CURRENT_XP()						{return "xp"};
	static get KEY_LEVEL_MIN_XP()					{return "xpNext"};
	static get KEY_LEVEL_MAX_XP()					{return "xpPrev"};
	static get KEY_CURRENCY_SYMBOL() 				{return "currencySymbol"};
	static get KEY_CURRENCY_CODE() 					{return "currencyCode"};
	static get KEY_REAL_CURRENCY_SYMBOL() 			{return "realPlayerCurrencySymbol"};	
	static get KEY_WEAPON_PRICES() 					{return "weaponPrices"};
	static get KEY_WEAPONS() 						{return "weapons"};
	static get KEY_WEAPON_ID() 						{return "weaponId"};
	static get KEY_BALANCE() 						{return "balance"};
	static get KEY_REFRESH_BALANCE() 				{return "refreshBalanceAvailable"};
	static get KEY_SEATID() 						{return "seatId"};
	static get KEY_MASTER_SERVER_SEATID() 			{return "masterServerSeatId"};
	static get KEY_STAKES() 						{return "stakes"};
	static get KEY_STAKES_LIMIT()	 				{return "stakesLimit"};
	static get KEY_STAKE()							{return "stake"};
	static get KEY_ENTERING_ROOM_STAKE()			{return "enteringRoomStake"};
	static get KEY_AVATAR()							{return "avatar"};
	static get KEY_NICKNAME()						{return "nickname"};
	static get KEY_NICKNAME_GLYPHS()				{return "nicknameGlyphs"};	
	static get KEY_NICKNAME_EDIT_ENABLED()			{return "nicknameEditEnabled"};	
	static get KEY_TOOL_TIP_ENABLED()				{return "toolTipEnabled"};
	static get KEY_TEST_SYSTEM()					{return "testSystem"};
	static get KEY_BRAND_ENABLE()					{return "brandEnable"};
	static get KEY_HEALTH_BAR_ENABLED()				{return "healthBarEnabled"};
	static get KEY_IS_NEWBIE()						{return "isNewbie"};
	static get KEY_DID_THE_PLAYER_WIN_SW_ALREADY()	{return "didThePlayerWinSWAlready"};
	static get KEY_FIRE_SETTINGS_LOCK_ON_TARGET()	{return "lockOnTarget"};
	static get KEY_FIRE_SETTINGS_TARGET_PRIORITY()	{return "targetPriority"};
	static get KEY_FIRE_SETTINGS_AUTO_FIRE()		{return "autoFire"};
	static get KEY_FIRE_SETTINGS_FIRE_SPEED()		{return "fireSpeed"};
	static get KEY_FIRE_SETTINGS_AUTO_EQUIP()		{return "autoEquip"};
	static get KEY_WEAPON_MODE()					{return "weaponMode"};
	static get KEY_WEAPON_COST_MULTIPLIERS() 		{return "weaponCostMultipliers"};
	static get KEY_QUEST_MULTIPLIERS()				{return "questMultypliers"};
	static get KEY_DISABLE_MQ_AUTOFIRING()			{return "disableAutofiring"};
	static get KEY_REAL_AMMO()						{return "realAmmo"};
	static get KEY_MONEY_WHEEL_PAYOUTS()			{return "moneyWheelPayouts"};
	static get KEY_POSSIBLE_BET_LEVELS()			{return "possibleBetLevels"};
	static get KEY_BET_LEVEL()						{return "betLevel"};
	static get KEY_BULLETS_LIMIT()					{return "bulletsLimit"};
	static get KEY_RICOCHET_BULLETS()				{return "ricochetsBullets"};
	static get KEY_REELS()							{return "reels"};
	static get KEY_CURRENT_POWER_UP_MULTIPLIER()	{return "keyCurrentPowerUpMultiplier"};
	static get KEY_IS_CAF_ROOM_MANAGER()			{return "isCAFRoomManager"};
	static get KEY_IS_KICKED()						{return "isKicked"};
	static get KEY_ROOM_OBSERVERS()					{return "roomObservers"};
	static get KEY_ROOM_FRIENDS()					{return "friends"};
	static get KEY_ROOM_SERVER_SEATERS()			{return "roomServerSeaters"};
	static get KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS()			{return "roomConfirmedBuyInBattlgroundSeats"};
	static get IS_BATTLGROUND_BUYIN_CONFIRMED()		{return "isBattlgroundBuyInConfirmed"};
	
	
	
	// for Dragonstone...
	static get KEY_PAYTABLE() { return "paytable" };
	static get KEY_WEAPON_PAID_MULTIPLIER() { return "weaponPaidMultiplier" };
	// ...for Dragonstone

	/**
	 * No game currently supports LOOT_BOX mode
	 * @ignore
	 */
	static get SUPPORTED_WEAPON_MODES()
	{
		return {
			LOOT_BOX: 	"LOOT_BOX",
			PAID_SHOTS: "PAID_SHOTS"
		}
	};

	/**
	 * paytable info from the server
	 */
	get paytable() {
		return this._getPlayerInfoValue(PlayerInfo.KEY_PAYTABLE);
	}

	/**
	 * reels icons for minislot feature
	 */
	get reels() {
		return this._getPlayerInfoValue(PlayerInfo.KEY_REELS);
	}

	/**
	 * @deprecated
	 */
	get weaponPaidMultiplier() {
		return this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_PAID_MULTIPLIER);
	}

	/**
	 * player data storage
	 */
	get playerInfo()
	{
		return this._fPlayer_obj;
	}

	/**
	 * 
	 * @param {string} aKey_str Parameter name
	 * @param {*} aValue_obj Parameter value
	 */
	setPlayerInfo(aKey_str, aValue_obj)
	{
		if (aValue_obj.time === undefined)
		{
			aValue_obj.time = 0;
		}

		if (this._fPlayer_obj[aKey_str])
		{
			if (this._fPlayer_obj[aKey_str].time <= aValue_obj.time || aValue_obj.time === 0 || (this._fPlayer_obj[aKey_str].complex === true && aValue_obj.complex === true))
			{
				if (aValue_obj.time === 0)	
				{
					aValue_obj.time = this._fPlayer_obj[aKey_str].time;
				}
				
				this._fPlayer_obj[aKey_str] = aValue_obj;
			}
		}
		else
		{
			this._fPlayer_obj[aKey_str] = aValue_obj;
		}
	}

	/**
	 * Array of player's ricochet bullets on the screen
	 */
	get ricochetBullets()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_RICOCHET_BULLETS) || 0;
	}

	/**
	 * Is first time player starts game. If true - player receives a special weapon as a gift.
	 */
	get isNewbie()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_IS_NEWBIE) || false;
	}

	/**
	 * To be used for debug
	 */
	get testSystem()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_TEST_SYSTEM) || undefined;
	}

	/**
	 * Win that is received from the server but is still not counted (animation in progress or is not started yet) on client
	 */
	get unpresentedWin()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_UNPRESENTED_WIN) || 0;
	}

	/**
	 * Expected ammo amount to be counted when current unpresentedWin will be transferred to ammo
	 */
	get pendingAmmo()
	{
		return this.currentStake > 0 ? (this.unpresentedWin || 0) / this.currentStake : 0;
	}	

	/**
	 * Win amount
	 */
	get qualifyWin()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_QUALIFY_WIN) || 0;
	}

	/**
	 * Indicates whether tooltips are on or not
	 */
	get toolTipEnabled()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_TOOL_TIP_ENABLED) || undefined;
	}

	/**
	 * Player nickname
	 */
	get nickname()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_NICKNAME) || undefined;
	}

	/**
	 * Symbols allowed to enter for nickname
	 */
	get nicknameGlyphs()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_NICKNAME_GLYPHS) || undefined;
	}

	/**
	 * Indicates whether nickname change available in the game or not
	 */
	get isNicknameEditable()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_NICKNAME_EDIT_ENABLED) || undefined;
	}

	/**
	 * Player avatar info
	 * @type {Avatar}
	 */
	get avatar()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_AVATAR) || undefined;
	}

	/**
	 * Player level
	 * @deprecated
	 */
	get level()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_LEVEL) || undefined;
	}

	/**
	 * Collected round XP
	 * @deprecated
	 */
	get currentXP()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_CURRENT_XP) || undefined;
	}

	/**
	 * @deprecated
	 */
	get levelMinXP()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_LEVEL_MIN_XP) || undefined;
	}

	/**
	 * @deprecated
	 */
	get levelMaxXP()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_LEVEL_MAX_XP) || undefined;
	}

	/**
	 * @deprecated
	 */
	get isLevelDefined()
	{
		return this.level !== undefined;
	}

	/**
	 * Currency symbol
	 * @type {string}
	 */
	get currencySymbol()
	{
		//MODES THAT EXCLUDE CURRENCY SYMBOL...
		if(APP.isBattlegroundGame)
		{
			return "";
		}
		//...MODES THAT EXCLUDE CURRENCY SYMBOL

		return this._getPlayerInfoValue(PlayerInfo.KEY_CURRENCY_SYMBOL);
	}

	/**
	 * @deprecated
	 */
	get currencySymbolIgnoreModes()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_CURRENCY_SYMBOL);
	}

	/**
	 * Currency code
	 * @type {string}
	 */
	get currencyCode()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_CURRENCY_CODE);
	}

	/**
	 * @deprecated
	 */
	get realPlayerCurrencySymbol()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_REAL_CURRENCY_SYMBOL);
	}

	/**
	 * @deprecated
	 */
	get weaponPrices()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_PRICES);
	}

	/**
	 * Player current weapon id
	 */
	get weaponId()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_ID);
	}

	/**
	 * Weapons supported by the game
	 * @type {number[]}
	 */
	get weapons()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_WEAPONS);
	}

	/**
	 * @deprecated
	 */
	get weaponMode()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_MODE);
	}

	/**
	 * Autofire enable/disable mode
	 */
	get isDisableAutofiring()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_DISABLE_MQ_AUTOFIRING);
	}

	/**
	 * @deprecated
	 */
	get isWeaponModeDefined()
	{
		return this.weaponMode !== undefined;
	}

	/**
	 * @deprecated
	 */
	get isWeaponLootBoxMode()
	{
		return this.weaponMode === PlayerInfo.SUPPORTED_WEAPON_MODES.LOOT_BOX;
	}

	/**
	 * @deprecated
	 */
	get isWeaponPaidShotsMode()
	{
		return this.weaponMode === PlayerInfo.SUPPORTED_WEAPON_MODES.PAID_SHOTS;
	}

	/**
	 * @deprecated
	 */
	get isWeaponCostMultipliersDefined()
	{
		let lWeaponCostMultipliers_obj_arr = this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS);

		return !this.isWeaponPaidShotsMode || !!lWeaponCostMultipliers_obj_arr;
	}

	/**
	 * Multiplier to calculate weapons shot cost
	 * @param {number} aWeaponId_int 
	 * @returns {number} 
	 */
	getWeaponPaidCostMultiplier(aWeaponId_int)
	{
		let lWeaponCostMultipliers_obj_arr = this._getPlayerInfoValue(PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS);
		let lWeaponCostMultiplier_int = 1;

		if (!!lWeaponCostMultipliers_obj_arr)
		{
			for (let i=0; i<lWeaponCostMultipliers_obj_arr.length; i++)
			{
				let lWeaponCostMultiplier_obj = lWeaponCostMultipliers_obj_arr[i];
				if (+lWeaponCostMultiplier_obj.id === aWeaponId_int)
				{
					lWeaponCostMultiplier_int = +lWeaponCostMultiplier_obj.costMultiplier;
					break;
				}
			}
		}
		
		return lWeaponCostMultiplier_int;
	}

	get balance()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_BALANCE);
	}

	/**
	 * List of stakes (cost per shot) available to player
	 * @type {number[]}
	 */
	get stakes()
	{
		let stakes = this._getPlayerInfoValue(PlayerInfo.KEY_STAKES);

		if (!stakes) return [];

		stakes = stakes.slice();
		return stakes.sort(function(a, b) {return a - b});
	}

	/**
	 * Limit of stakes after which additional buyIn should be done (buy ammo in the background).
	 * @type {number}
	 */
	get stakesLimit()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_STAKES_LIMIT);	
	}

	/**
	 * Room stake (shot cost) in cents
	 * @type {number}
	 */
	get currentStake()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_STAKE);
	}

	/**
	 * Minimal of possible room stakes
	 * @type {number}
	 */
	get minRoomsStake()
	{
		let stakes = this._getPlayerInfoValue(PlayerInfo.KEY_STAKES);

		if (!stakes) return 0;

		stakes = stakes.slice();
		let sortedStakes = stakes.sort(function(a, b) {return a - b});

		return sortedStakes[0];
	}

	/**
	 * Player ammo amount. Can be float (win transferred to ammo leads to float type).
	 * @type {number}
	 */
	get realAmmo()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_REAL_AMMO);
	}

	/**
	 * Room stake to enter by player
	 * @type {number}
	 */
	get enterRoomStake()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_ENTERING_ROOM_STAKE);
	}

	/**
	 * safdsdf
	 * @type {boolean}
	 */
	get refreshBalanceAvailable()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_REFRESH_BALANCE);
	}

	/**
	 * Master player seatId confirmed on client (after sitin response, including re-sitin)
	 * @type {number}
	 */
	get seatId()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_SEATID);
	}

	/**
	 * Master player seatId on server (i.e. before re-sitin, when alreadySitInNumber!=-1 but re-sitin is not confirmed)
	 * @type {number}
	 */
	get masterServerSeatId()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_MASTER_SERVER_SEATID);
	}

	/**
	 * Indicates whether master player seater on server or not (his sit in can be not confirmed on client yet).
	 * @type {boolean}
	 */
	get isMasterServerSeatIdDefined()
	{
		return this.masterServerSeatId != undefined;
	}

	/**
	 * Master player has not confirmed his sit in on client
	 * @type {number}
	 */
	get isObserver()
	{
		return !(this.seatId >= 0);
	}

	/**
	 * Indicates whether brand should be displayed or not
	 * @type {boolean}
	 */
	get brandEnable()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_BRAND_ENABLE);
	}

	/**
	 * Indicates whether enemies health bar enabled or not
	 * @type {boolean}
	 */
	get healthBarEnabled()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_HEALTH_BAR_ENABLED);
	}

	/**
	 * @deprecated
	 */
	get didThePlayerWinSWAlready()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_DID_THE_PLAYER_WIN_SW_ALREADY);
	}

	/**
	 * Lock on target option is on/off
	 * @type {boolean}
	 */
	get lockOnTarget()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET);
	}

	/**
	 * Target selection priority type
	 * @type {number}
	 */
	get targetPriority()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY);
	}

	/**
	 * Auto fire option is on/off
	 * @type {boolean}
	 */
	get autoFire()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE);
	}

	/**
	 * fire speed option
	 * @type {number}
	 */
	get fireSpeed()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED);
	}

	/**
	 * @deprecated
	 */
	get autoEquip()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_FIRE_SETTINGS_AUTO_EQUIP);
	}

	/**
	 * @deprecated
	 */
	get questMultypliers()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_QUEST_MULTIPLIERS);
	}

	/**
	 * Amount of money player received for completing money wheel
	 */
	get moneyWheelPayouts()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_MONEY_WHEEL_PAYOUTS);
	}

	/**
	 * Possible bet levels
	 * @type {number[]}
	 */
	get possibleBetLevels()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_POSSIBLE_BET_LEVELS);
	}

	/**
	 * Gets turret skin id for current bet level
	 * @param {number} aBetLevel 
	 * @returns {number}
	 */
	getTurretSkinId(aBetLevel)
	{
		if (!aBetLevel)
		{
			aBetLevel = this.roomDefaultBetLevel;
		}

		let lPossibleBetLevels_arr = this.possibleBetLevels;
		const turretSkinsAmount = lPossibleBetLevels_arr[0] > 1 ? 5 : lPossibleBetLevels_arr.length; // DS/MA BTG have levels [3, 5, 10] but SectorX BTG has levels [1, 2, 3] 
		let lBetLevelIndex_int = lPossibleBetLevels_arr.indexOf(aBetLevel);
		if (lBetLevelIndex_int < 0)
		{
			return 1;
		}
		return lBetLevelIndex_int + 1 + (turretSkinsAmount - lPossibleBetLevels_arr.length);
	}

	/**
	 * Get bet level by turret skin id
	 * @param {number} aTurretSkinId_int 
	 * @returns {number}
	 */
	getBetLevelByTurretSkinId(aTurretSkinId_int)
	{
		let lPossibleBetLevels_arr = this.possibleBetLevels;
		const turretSkinsAmount = lPossibleBetLevels_arr[0] > 1 ? 5 : lPossibleBetLevels_arr.length; // DS/MA BTG have levels [3, 5, 10] but SectorX BTG has levels [1, 2, 3] 

		return lPossibleBetLevels_arr[aTurretSkinId_int - (turretSkinsAmount-lPossibleBetLevels_arr.length) - 1];
	}

	/**
	 * Current bet level
	 * @type {number}
	 */
	get betLevel()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_BET_LEVEL);
	}

	/**
	 * Room default bet level
	 * @type {number}
	 */
	get roomDefaultBetLevel()
	{
		return this.possibleBetLevels[0];
	}

	/**
	 * Current power up multiplier
	 * @type {number}
	 */
	get currentPowerUpMultiplier()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER) || 1;
	}

	/**
	 * Max ricochet bullets amount allowed to emit by the player
	 */
	get bulletsLimit()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_BULLETS_LIMIT);
	}

	/**
	 * Indicates whether player is CAF room manager or not
	 * @type {boolean}
	 */
	get isCAFRoomManager()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_IS_CAF_ROOM_MANAGER);
	}

	/**
	 *  Indicates whether room manager property defined or not
	 * @type {boolean}
	 */
	get isCAFRoomManagerDefined()
	{
		return this.isCAFRoomManager !== undefined;
	}

	/**
	 *  Indicates whether player is kicked by room manager or not
	 * @type {boolean}
	 */
	get isKicked()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_IS_KICKED);
	}

	/**
	 *  Indicates whether isKicked property defined or not
	 * @type {boolean}
	 */
	get isKickedDefined()
	{
		return this.isKicked !== undefined;
	}

	/**
	 * Room observers from the server
	 * @type {RoomObserver[]}
	 */
	get roomObservers()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_ROOM_OBSERVERS);
	}


	get friends()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_ROOM_FRIENDS);
	}

	/**
	 * Room seaters registered on the server (master player seat can be not confirmed on client still)
	 * @type {RoomSeater[]}
	 */
	get roomServerSeaters()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_ROOM_SERVER_SEATERS);
	}

	/**
	 * Room seats that confirmed battleground buy in.
	 * @type {number[]}
	 */
	get roomConfirmedBuyInBattlgroundSeats()
	{
		return this._getPlayerInfoValue(PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS);
	}

	/**
	 * Indicates whether buy-in of master player confirmed of not
	 * @type {Boolean}
	 */
	get isBattlgroundBuyInConfirmed()
	{
		return this._getPlayerInfoValue(PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED);
	}

	/**
	 * Checks if player with provided nickname is seater on server or not
	 * @param {string} aPlayerNickname_str 
	 * @returns {Boolean}
	 */
	isSeaterOnServer(aPlayerNickname_str)
	{
		let lRoomSeates_obj_arr = this.roomServerSeaters || null;
		
		if (!lRoomSeates_obj_arr || !lRoomSeates_obj_arr.length)
		{
			return false;
		}

		for (let i=0; i<lRoomSeates_obj_arr.length; i++)
		{
			if (lRoomSeates_obj_arr[i].nickname === aPlayerNickname_str)
			{
				return true;
			}
		}

		return false;
	}

	/**
	 * Gets player seatId by nickname
	 * @param {string} aPlayerNickname_str 
	 * @returns {number}
	 */
	getServerSeaterSeatId(aPlayerNickname_str)
	{
		let lRoomSeates_obj_arr = this.roomServerSeaters || null;
		
		if (!lRoomSeates_obj_arr || !lRoomSeates_obj_arr.length)
		{
			return undefined;
		}

		for (let i=0; i<lRoomSeates_obj_arr.length; i++)
		{
			if (lRoomSeates_obj_arr[i].nickname === aPlayerNickname_str)
			{
				return lRoomSeates_obj_arr[i].seatId;
			}
		}

		return undefined;
	}

	/**
	 * Checks if buyin of player with provided nickname is confirmed on server or not
	 * @param {string} aPlayerNickname_str 
	 * @returns {Boolean}
	 */
	isSeaterBattlegroundBuyInConfirmed(aPlayerNickname_str)
	{
		let lConfirmedSeatsId_int_arr = this.roomConfirmedBuyInBattlgroundSeats;
		if (!lConfirmedSeatsId_int_arr || !lConfirmedSeatsId_int_arr)
		{
			return false;
		}

		let lSeatId_int = this.getServerSeaterSeatId(aPlayerNickname_str);
		
		if (lSeatId_int === undefined)
		{
			return false;
		}

		return lConfirmedSeatsId_int_arr.indexOf(lSeatId_int) >= 0;
	}

	//INIT...
	constructor(aOptId_obj, aOptParentInfo_usi)
	{
		super(aOptId_obj, aOptParentInfo_usi);

		this._initPlayerInfo();
	}

	_initPlayerInfo()
	{
		this._fPlayer_obj = {};
		this._pendingInvite = false;
	}
	//...INIT

	/**
	 * Gets property value by the key
	 * @param {string} aKey_str - Property name
	 * @returns {*} - Property value
	 * @default undefined
	 */
	_getPlayerInfoValue(aKey_str)
	{
		if (this._fPlayer_obj[aKey_str])
		{
			return this._fPlayer_obj[aKey_str].value;
		}
		else
		{
			return undefined;
		}
	}

	set pendingInvite(aValue_obj)
	{
		this._pendingInvite = aValue_obj
	}

	get pendingInvite()
	{
		return this._pendingInvite;	
	}


	destroy()
	{
		this._fPlayer_obj = null;

		super.destroy();
	}
}

export default PlayerInfo;