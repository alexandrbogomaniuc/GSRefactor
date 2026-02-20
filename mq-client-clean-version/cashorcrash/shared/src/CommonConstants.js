export const BASE_FRAME_RATE = 16.7;
export const FRAME_RATE = 2*16.7;

export const DEFAULT_WEAPON_NAME = "Pistol";


//DIALOGS IDS...
let dialogIdCounter = 0;

export const DIALOG_ID_NETWORK_ERROR 						= dialogIdCounter++; //0
export const DIALOG_ID_GAME_NETWORK_ERROR 					= dialogIdCounter++; //1
export const DIALOG_ID_CRITICAL_ERROR 						= dialogIdCounter++; //2
export const DIALOG_ID_GAME_CRITICAL_ERROR 					= dialogIdCounter++; //3
export const DIALOG_ID_RECONNECT 							= dialogIdCounter++; //4
export const DIALOG_ID_GAME_RECONNECT 						= dialogIdCounter++; //5
export const DIALOG_ID_GAME_ROOM_REOPEN 					= dialogIdCounter++; //6
export const DIALOG_ID_ROOM_NOT_FOUND 						= dialogIdCounter++; //7
export const DIALOG_ID_GAME_NEM 							= dialogIdCounter++; //8 	//not enough money
export const DIALOG_ID_REDIRECTION 							= dialogIdCounter++; //9
export const DIALOG_ID_RETURN_TO_GAME 						= dialogIdCounter++; //10
export const DIALOG_ID_FORCE_SIT_OUT						= dialogIdCounter++; //11
export const DIALOG_ID_MID_ROUND_COMPENSATE_SW 				= dialogIdCounter++; //12
export const DIALOG_ID_MID_ROUND_EXIT 						= dialogIdCounter++; //13
export const DIALOG_ID_GAME_BUY_AMMO_FAILED	 				= dialogIdCounter++; //14
export const DIALOG_ID_WEBGL_CONTEXT_LOST					= dialogIdCounter++; //15
export const DIALOG_ID_RUNTIME_ERROR 						= dialogIdCounter++; //16
export const DIALOG_ID_BONUS 								= dialogIdCounter++; //17
export const DIALOG_ID_FRB									= dialogIdCounter++; //18
export const DIALOG_ID_TOURNAMENT_STATE						= dialogIdCounter++; //19
export const DIALOG_ID_GAME_REBUY							= dialogIdCounter++; //20
export const DIALOG_ID_GAME_NEM_FOR_ROOM					= dialogIdCounter++; //21
export const DIALOG_ID_LOBBY_REBUY							= dialogIdCounter++; //22
export const DIALOG_ID_LOBBY_NEM							= dialogIdCounter++; //23
export const DIALOG_ID_LOBBY_REBUY_FAILED					= dialogIdCounter++; //24
export const DIALOG_ID_GAME_SW_PURCHASE_LIMIT_EXCEEDED		= dialogIdCounter++; //25
export const DIALOG_ID_ROUND_TRANSITION_SW_COMPENSATION		= dialogIdCounter++; //26

export const DIALOG_ID_INSUFFICIENT_FUNDS					= dialogIdCounter++; //27
export const DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS		= dialogIdCounter++; //28
export const DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION		= dialogIdCounter++; //29
export const DIALOG_ID_BATTLEGROUND_RULES					= dialogIdCounter++; //30
export const DIALOG_ID_NO_WEAPONS_FIRED						= dialogIdCounter++; //31
export const DIALOG_ID_BATTLEGROUND_CONTINUE_READING		= dialogIdCounter++; //32

export const DIALOG_ID_PENDING_OPERATION_FAILED				= dialogIdCounter++; //33
export const DIALOG_ID_ROUND_ALREADY_FINISHED				= dialogIdCounter++; //34
export const DIALOG_ID_ROOM_MOVED_ERROR_REQUESTS_LIMIT_REACHED = dialogIdCounter++; //35
export const DIALOG_ID_PLEASE_WAIT							= dialogIdCounter++; //36
export const DIALOG_ID_WAIT_PENDING_OPERATION				= dialogIdCounter++; //37

export const DIALOG_ID_SERVER_REBOOT						= dialogIdCounter++; //37
export const DIALOG_ID_GAME_SERVER_REBOOT					= dialogIdCounter++; //38

export const DIALOG_ID_CAF_PLAYER_KICKED 					= dialogIdCounter++; //40
export const DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER		= dialogIdCounter++; //41
export const DIALOG_ID_CAF_ROOM_WAS_DEACTIVATED 			= dialogIdCounter++; //42

export const DIALOGS_IDS_COUNT								= dialogIdCounter;
//...DIALOGS IDS

//GAMEPLAY DIALOGS IDS...
let gameplayDialogIdCounter = 0;
export const DIALOG_ID_BATTLEGROUND_COUNT_DOWN				= gameplayDialogIdCounter++;
export const DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN			= gameplayDialogIdCounter++;
export const GAMEPLAY_DIALOGS_IDS_COUNT						= gameplayDialogIdCounter;
//...GAMEPLAY DIALOGS IDS

export const BATTLEGROUND_STAKE_MULTIPLIER = 100;

export const TOURNAMENT_STATES = {
	READY: "READY",
	ACTIVE:	"STARTED",
	FINISHED:	"FINISHED",
	CANCELLED:	"CANCELLED"
};

export const TREASURES_NAMES = {
	0: "sapphire",	//blue
	1: "yellow",	//yellow
	2: "emerald",	//green
	3: "ruby",		//red
	4: "diamond",	//white
};


export const TOTAL_QUEST_COMPLETE_GEMS = 3;
export const BTG_TOTAL_QUEST_COMPLETE_GEMS = 1;

export const WEAPONS = {
	DEFAULT:					-1,
	INSTAKILL:					4,
	RICOCHET:					3,
	ARTILLERYSTRIKE:			7,
	FLAMETHROWER:				9,
	CRYOGUN:					10,
	HIGH_LEVEL: 				16,
	ARTILLERYSTRIKE_POWER_UP:	17,
	FLAMETHROWER_POWER_UP:		18,
	CRYOGUN_POWER_UP:			19,
	RICOCHET_POWER_UP:			20,
	INSTAKILL_POWER_UP:			21
};

export const POWER_UP_WEAPONS = {
	ARTILLERYSTRIKE_POWER_UP:	17,
	FLAMETHROWER_POWER_UP:		18,
	CRYOGUN_POWER_UP:			19,
	RICOCHET_POWER_UP:			20,
	INSTAKILL_POWER_UP:			21
};

export const RICOCHET_WEAPONS = [WEAPONS.DEFAULT, WEAPONS.HIGH_LEVEL];

export const INDICATORS_CONSTANT_VALUES =
{
	SPECIAL_WEAPONS_MAX_TARGETS_COUNTS:
	[
		12, //plasma rifle
		12,//laser
		14,//cryogun
		16,//flamethrower
		25 //artillery strike
	],
	SPECIAL_WEAPONS_MAX_TARGETS_COUNTS_BTG:
	[
		10, //plasma rifle
		10,//laser
		14,//cryogun
		15,//flamethrower
		20 //artillery strike
	],
	MAX_SHOTS_PER_POWERUP: 20
};

export const COOP_FIRE_FX_ALPHA = 0.3;

export const GAME_ROUND_STATE = {
	WAIT: 		"WAIT",
	PLAY: 		"PLAY",
	QUALIFY: 	"QUALIFY",
	CLOSED: 	"CLOSED"
};

export const ENEMIES =
{
	Jaguar:						'Jaguar',
	Skullbreaker:				'Skullbreaker',
	Wasp:						'Wasp',
	YellowWasp:					'YellowWasp',
	VioletWasp:					'VioletWasp',
	Firefly:					'Firefly',
	Witch:						'Witch',
	SnakeStraight:				'SnakeStraight',
	RedAnt:						'RedAnt',
	BlackAnt:					'BlackAnt',
	BabyFrog: 					'BabyFrog',
	Scorpion:					'Scorpion',
	Jumper:						'Jumper',
	Exploder:					'Exploder',
	SkullboneRunner:			'SkullboneRunner',
	SkullboneOrb:				'SkullboneOrb',
	SpiderBoss: 				'SpiderBoss',
	GolemBoss: 					'GolemBoss',
	ApeBoss: 					'ApeBoss',
	BlueOrbArtillerystrike:		'BlueOrbArtillerystrike',
	BlueOrbFlamethrower:		'BlueOrbFlamethrower',
	BlueOrbCryogun:				'BlueOrbCryogun',
	BlueOrbLaser:				'BlueOrbLaser',
	BlueOrbPlasma:				'BlueOrbPlasma',
	PoisonPlantMaroonViolet:	'PoisonPlantMaroonViolet',
	PoisonPlantYellowPurple:	'PoisonPlantYellowPurple',
	CarnivorePlantRed:			'CarnivorePlantRed',
	CarnivorePlantGreen:		'CarnivorePlantGreen',
};

export const ENEMY_TYPES =
{
	SKULLBREAKER:					0,
	WITCH:							1,
	JUMPER: 						2,
	SKULLBONE_RUNNER:				3,
	JAGUAR:							4,
	SNAKE:							5,
	SPIDERLING:						6,
	WASP:							7,
	SKULLBONE_ORB:					8,
	EXPLODER:						9,
	SCORPION:						10,
	TINY_TOAD:						11,
	POISON_PLANT_GREEN_PINK:		12,
	POISON_PLANT_YELLOW_PURPLE:		13,
	CARNIVORE_PLANT_RED:			14,
	CARNIVORE_PLANT_GREEN:			15,
	BLUE_ORB_ARTILLERYSTRIKE:		16,
	BLUE_ORB_FLAMETHROWER:			17,
	BLUE_ORB_CRYOGUN:				18,
	BLUE_ORB_LASER:					19,
	BLUE_ORB_INSTAKILL:				20,
	BOSS:							21,
};

export const ENEMY_BOSS_SKINS =
{
	SPIDER: 	1,
	GOLEM: 		2,
	APE: 		3
}

export const ENEMY_ANT_SKINS =
{
	RED_ANT:			1,
	BLACK_ANT:			2
}

export const ENEMY_WASP_SKINS =
{
	WASP:				1,
	VIOLET_WASP: 		2,
	FIREFLY:			3,
	YELLOW_WASP: 		4
}

export const ENEMIES_EFFECTS_LIST = {
	LIGHTNING: "LIGHTNING",
	FIRE: "FIRE"
};

export const BACKGROUND_SOUNDS_FADING_TIME = 1000;

export const IS_SPECIAL_WEAPON_SHOT_PAID = false;

export function isBossEnemy(aEnemyTypeId)
{
	return aEnemyTypeId === ENEMY_TYPES.BOSS;
}

export function isPowerUpWeapons(aWeaponIds)
{
	if (!Array.isArray(aWeaponIds))
	{
		aWeaponIds = [aWeaponIds];
	}

	return aWeaponIds.some(el => Object.values(POWER_UP_WEAPONS).includes(el));
}

export const DISABLE_TIP_AUTOFIRE = 0;