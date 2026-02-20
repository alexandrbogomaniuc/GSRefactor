import
{
	DIALOG_ID_INSUFFICIENT_FUNDS,
	DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS,
	DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION,
	DIALOG_ID_BATTLEGROUND_COUNT_DOWN,
	DIALOG_ID_BATTLEGROUND_RULES,
	DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER

} from '../../../shared/src/CommonConstants';

import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import Enemy from './enemies/Enemy';
import Bullet from './bullets/Bullet';
import RicochetEffect from './bullets/RicochetEffect';
import CharcoalFlyingDebris from './bullets/CharcoalFlyingDebris';
import Crate from '../ui/Crate';
import ContentItem from '../ui/content/ContentItem';
import ContentItemInfo from '../model/uis/content/ContentItemInfo';
import {WEAPONS, RICOCHET_WEAPONS, ENEMIES, FRAME_RATE, IS_SPECIAL_WEAPON_SHOT_PAID, ENEMY_TYPES, POWER_UP_WEAPONS} from '../../../shared/src/CommonConstants';
import Sequence from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import * as Easing from '../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import Timer from "../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import { Utils } from '../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import PlayerSpot from './playerSpots/PlayerSpot';
import BattlegroundMainPlayerSpot from './playerSpots/battleground/BattlegroundMainPlayerSpot';
import MainPlayerSpot from './playerSpots/MainPlayerSpot';
import Grenade from './Grenade';
import PreloadingSpinner from '../../../../common/PIXI/src/dgphoenix/gunified/view/custom/PreloadingSpinner';
import GameScreen from './GameScreen';
import AwardingController from '../controller/uis/awarding/AwardingController';
import RicochetLaserRay from './animation/ricochet_laser/RicochetLaserRay';
import GrenadeGroundBurn from './animation/grenade/GrenadeGroundBurn';
import CeilingDust from './animation/CeilingDust';
import RoundResultScreenView from '../view/uis/roundresult/RoundResultScreenView';
import BattlegroundResultScreenView from '../view/uis/roundresult/battleground/BattlegroundResultScreenView';
import RoundResultScreenInfo from '../model/uis/roundresult/RoundResultScreenInfo';
import RoundResultScreenController from '../controller/uis/roundresult/RoundResultScreenController';
import BossModeController from '../controller/uis/custom/bossmode/BossModeController';
import FireSettingsController from '../controller/uis/fire_settings/FireSettingsController';
import FireSettingsView from '../view/uis/fire_settings/FireSettingsView';
import CalloutsController from '../controller/uis/custom/callouts/CalloutsController';
import GameplayDialogsController from '../controller/uis/custom/gameplaydialogs/GameplayDialogsController';
import GamePlayerController from '../controller/custom/GamePlayerController';
import GameStateController from '../controller/state/GameStateController';
import { ROUND_STATE } from '../model/state/GameStateInfo';
import { SERVER_MESSAGES } from '../model/interaction/server/GameWebSocketInteractionInfo';
import Game from '../Game';
import GameFieldScreen from './gameField/GameFieldScreen';
import GameFieldBackContainer from './gameField/GameFieldBackContainer';
import GameFieldPlayersContainer from './gameField/GameFieldPlayersContainer';
import GameFieldPlayerSeatsBurstContainer from './gameField/GameFieldPlayerSeatsBurstContainer';
import TargetingController from '../controller/uis/targeting/TargetingController';
import GameSoundsController from '../controller/sounds/GameSoundsController';
import MapsController from '../controller/uis/maps/MapsController';
import SpineEnemy from './enemies/SpineEnemy';
import BossEnemy from './enemies/BossEnemy';
import BlueOrb from './enemies/BlueOrb';
import BlueOrbLaser from './enemies/BlueOrbLaser';
import BlueOrbAtrillery from './enemies/BlueOrbAtrillery';
import BlueOrbCryogun from './enemies/BlueOrbCryogun';
import BlueOrbFlamethrower from './enemies/BlueOrbFlamethrower';
import BlueOrbPlasma from './enemies/BlueOrbPlasma';

import CryogunFireEffect from './animation/cryogun/CryogunFireEffect';
import CryogunsController from '../controller/uis/weapons/cryogun/CryogunsController';
import Weapon from './playerSpots/Weapon';
import ShotResultsUtil from './ShotResultsUtil';
import FlameThrowersController from '../controller/uis/weapons/flamethrower/FlameThrowersController';
import ArtilleryStrikesController from '../controller/uis/weapons/artillerystrike/ArtilleryStrikesController';
import PlazmaGunsController from '../controller/uis/weapons/plazmagun/PlazmaGunsController';
import RicochetLasersController from '../controller/uis/weapons/ricochet_laser/RicochetLasersController';
import EightWayEnemy from './enemies/EightWayEnemy';
import DefaultGunFireEffect from './animation/default_gun/DefaultGunFireEffect';

import KeyboardControlProxy from '../../../../common/PIXI/src/dgphoenix/unified/controller/interaction/keyboard/KeyboardControlProxy';
import {GAME_MESSAGES, LOBBY_MESSAGES} from '../external/GameExternalCommunicator';
import GameExternalCommunicator from '../external/GameExternalCommunicator';
import PrizesController, { HIT_RESULT_SINGLE_CASH_ID, HIT_RESULT_ADDITIONAL_CASH_ID } from '../controller/uis/prizes/PrizesController';
import ProfilingInfo from '../../../../common/PIXI/src/dgphoenix/unified/model/profiling/ProfilingInfo';
import { MIN_SHOT_Y, MAX_SHOT_Y } from '../config/Constants';
import CriticalHitAnimation from '../view/uis/custom/critical_hit/CriticalHitAnimation';
import PowerUpMultiplierAnimation from '../view/uis/custom/power_up_multiplier/PowerUpMultiplierAnimation';
import ArtillerystrikeExplosion from './animation/artillery/ArtillerystrikeExplosion';
import WeaponsSidebarController from '../controller/uis/weapons/sidebar/WeaponsSidebarController';
import WeaponsSidebarView from '../view/uis/weapons/sidebar/WeaponsSidebarView';
import GameFRBController from './../controller/uis/custom/frb/GameFRBController';

import TreasuresSidebarController from '../controller/uis/treasures/TreasuresSidebarController';
import {TreasuresSidebarView, TreasuresSidebarBTGView} from '../view/uis/treasures/TreasuresSidebarView';

import TournamentModeController from '../controller/custom/tournament/TournamentModeController';
import BigWinsController from '../controller/uis/awarding/big_win/BigWinsController';

import GameWebSocketInteractionController from '../controller/interaction/server/GameWebSocketInteractionController';
import RicochetBullet from './bullets/RicochetBullet';
import RicochetController from './../controller/custom/RicochetController';

import BetLevelEmptyButton from './playerSpots/BetLevelEmptyButton';

import GroundFireTraceAnimation from './animation/GroundFireTraceAnimation';

import WaspEnemy from './enemies/WaspEnemy';
import FrogEnemy from './enemies/FrogEnemy';
import ScorpionEnemy from './enemies/ScorpionEnemy';
import BabyFrogEnemy from './enemies/BabyFrogEnemy';
import RunWithJumpEnemy from './enemies/RunWithJumpEnemy';
import WitchEnemy from './enemies/WitchEnemy';
import JumperEnemy from './enemies/JumperEnemy';
import SpiderBossEnemy from './enemies/SpiderBossEnemy';
import GolemBossEnemy from './enemies/GolemBossEnemy';
import ApeBossEnemy from './enemies/ApeBossEnemy';
import WitchEnemyTeleportAnimation from '../view/uis/enemies/WitchEnemyTeleportAnimation';

import { ENEMIES_EFFECTS_LIST, COOP_FIRE_FX_ALPHA } from './../../../shared/src/CommonConstants';
import WeaponSidebarIcon from './../view/uis/weapons/sidebar/WeaponSidebarIcon';

import FreeShotsCounterView from '../ui/content/FreeShotsCounterView';
import ScoreboardController from '../controller/uis/scoreboard/ScoreboardController';

import BattlegroundFinalCountingController from '../controller/uis/final_counting/BattlegroundFinalCountingController';
import BattlegroundFinalCountingView from '../view/uis/final_counting/BattlegroundFinalCountingView';
import BattlegroundFinalCountingInfo from '../model/uis/final_counting/BattlegroundFinalCountingInfo';

import BattlegroundCountDownDialogController from '../controller/uis/custom/gameplaydialogs/custom/BattlegroundCountDownDialogController';
import BattlegroundTutorialController from '../controller/uis/custom/tutorial/BattlegroundTutorialController';

import SpecialAreasMap from './specialAreas/SpecialAreasMap';
import SpecialAreasMapEditor from './specialAreas/SpecialAreasMapEditor';
import { BulgePinchFilter } from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

import MissEffect from './missEffects/MissEffect';
import MissEffectsPool from './missEffects/MissEffectsPool';
import PlantEnemy from './enemies/PlantEnemy';
import CarnivorePlantEnemy from './enemies/CarnivorePlantEnemy';
import PoisonPlantEnemy from './enemies/PoisonPlantEnemy';
import CriticalHitMultiplierView from '../view/uis/custom/critical_hit/CriticalHitMultiplierView';
import CriticalHitMultiplierGlowView from '../view/uis/custom/critical_hit/CriticalHitMultiplierGlowView';

import BossModeHPBarController from '../controller/uis/custom/bossmode/BossModeHPBarController';
import BossModeHPBarInfo from './../model/uis/custom/bossmode/BossModeHPBarInfo';
import BossModeHPBarView from './../view/uis/custom/bossmode/BossModeHPBarView';
import PowerUpMultiplierView from '../view/uis/custom/power_up_multiplier/PowerUpMultiplierView';
import AtlasConfig from '../config/AtlasConfig';
import { AtlasSprite } from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import GamePendingOperationController from '../controller/custom/GamePendingOperationController';

const AUTO_FIRE_TIMEOUTS = {
	MIN: {
		OTHER: 160,
		CRYOGUN: 978,
		FLAMETHROWER: 25*2*16.7,
		RICOCHET: 25*2*16.7,
		ARTILLERYSTRIKE: 1700,
		INSTAKILL: 1000,
		CRYOGUN_POWER_UP: 978,
		FLAMETHROWER_POWER_UP: 25*2*16.7,
		RICOCHET_POWER_UP: 25*2*16.7,
		ARTILLERYSTRIKE_POWER_UP: 1700,
		INSTAKILL_POWER_UP: 1000
	},
	MIDDLE: {
		OTHER: 290,
		CRYOGUN: 1027,
		FLAMETHROWER: 28*2*16.7,
		RICOCHET: 28*2*16.7,
		ARTILLERYSTRIKE: 1800,
		INSTAKILL: 1200,
		CRYOGUN_POWER_UP: 1027,
		FLAMETHROWER_POWER_UP: 28*2*16.7,
		RICOCHET_POWER_UP: 28*2*16.7,
		ARTILLERYSTRIKE_POWER_UP: 1800,
		INSTAKILL_POWER_UP: 1200
	},
	MAX: {
		OTHER: 550,
		CRYOGUN: 1202,
		FLAMETHROWER: 31*2*16.7,
		RICOCHET: 31*2*16.7,
		ARTILLERYSTRIKE: 1900,
		INSTAKILL: 1500,
		CRYOGUN_POWER_UP: 1202,
		FLAMETHROWER_POWER_UP: 31*2*16.7,
		RICOCHET_POWER_UP: 31*2*16.7,
		ARTILLERYSTRIKE_POWER_UP: 1900,
		INSTAKILL_POWER_UP: 1500
	}
}

const MIN_FIRE_REQUEST_TIMEOUT = 150; // minimum timeout between shots
const MIDDLE_FIRE_REQUEST_TIMEOUT = 230; // middle timeout between shots
const MAX_FIRE_REQUEST_TIMEOUT = 500; // maximum timeout between shots

const PLAYERS_POSITIONS = {
	"DESKTOP":[
		{x: 112, 		y: 493, 	direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 461.5-14, 	y: 493,		direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 811-28, 	y: 493,		direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 112,		y: 48+16,		direct: 1, masterOffset: {x: 0+36, y: 10-24}},
		{x: 461.5-14, 	y: 48+16,	 	direct: 1, masterOffset: {x: 0+36, y: 10-24}},
		{x: 811-28, 	y: 48+16, 		direct: 1, masterOffset: {x: 0+36, y: 10-24}}
	],
	"MOBILE": [
		{x: 112, 		y: 470, 	direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 461.5-14, 	y: 470,		direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 811-28, 	y: 470,		direct: 0, masterOffset: {x: 0+36, y: 2}},
		{x: 112,		y: 48+16,		direct: 1, masterOffset: {x: 0+36, y: 10-24}},
		{x: 461.5-14, 	y: 48+16, 		direct: 1, masterOffset: {x: 0+36, y: 10-24}},
		{x: 811-28, 	y: 48+16,	 	direct: 1, masterOffset: {x: 0+36, y: 10-24}}
	]
}

export const SEATS_POSITION_IDS = {
	0: 1,
	1: 4,
	2: 2,
	3: 5,
	4: 3,
	5: 0
}

const TURRET_TURN_UNIT = 0.047; // rad (~ 3 grad)
const CURSOR_MOVE_UNIT = 7; // px

//-----------------
// | this
// ----------------
//   | container
// ----------------
//     | screen
//-----------------
export const Z_INDEXES = {
	WAIT_SCREEN:						100000, /*this*/
	WAITING_CAPTION:					100000, /*this*/
	SUBLOADING:							100001, /*this*/
	TRANSITION_VIEW:					110002, /*this*/
	ROUND_RESULT:						110003, /*this*/
	COUNT_DOWN:							110004, /*this*/
	BTG_FINAL_COUNTING:					110005, /*this*/
	FIRE_SETTINGS:						110006, /*this*/

	CEILING_DUST:						40000, /*this.container*/

	GROUNDBURN:							-541, /*this.bottomScreen*/
	SMOKE_TRAIL:						2, /*this.bottomScreen*/
	STEPS_TRAIL: 						9, /*this.bottomScreen*/
	BOSS_APPEARING_CIRCLE_FX:			260, /*this.bottomScreen*/
	TELEPORT_FX_ANIMATION: 				1430, /*this.bottomScreen*/
	MAP_FX_ANIMATION:					8000, /*this.bottomScreen*/
	CHAIN_GUN_AMMO_CASE:				10000, /*this.bottomScreen*/
	GRADIENT:							19000, /*this.bottomScreen*/
	// bottomScreen - a part of map that will move away with boss appearing
	// topScreen - effects, elements and UI, that don't go away with boss appearing
	BOSS_RED_SCREEN:					480, /*this.topScreen*/
	BOSS_APPEARING_FX:					1440, /*this.topScreen*/
	LOGO:								19999, /*this.topScreen*/
	PLAYERS_CONTAINER:					20000, /*this.topScreen*/
	PLAYERS_CONTAINER_EFFECTS:			20001, /*this.topScreen*/
	MAIN_SPOT:							20002, /*this.topScreen*/
	BULLET:								20100, /*this.topScreen*/
	GUN_FIRE_EFFECT:					20101, /*this.topScreen*/
	MISS_EFFECT:						20102, /*this.topScreen*/
	PLAZMA_LENS_FLARE:					20110, /*this.topScreen*/
	MAIN_PLAYER_CONTAINER_EFFECTS:		20120, /*this.topScreen*/
	SIDEBAR: 							20130, /*this.topScreen*/
	PLAYER_REWARD:						21000, /*this.topScreen*/
	AWARDED_WEAPON_CONTENT:				26000, /*this.topScreen*/
	AMMO_COUNTER:						26001, /*this.topScreen*/
	TARGETING:							27000, /*this.topScreen*/
	AWARDED_WIN_CONTENT:				27010, /*this.topScreen*/
	CRITICAL_HIT:						27011, /*this.topScreen*/
	POWER_UP_MULTIPLIER:				27011, /*this.topScreen*/
	AUTO_TARGETING_SWITCHER:			27020, /*this.topScreen*/
	BET_LEVEL_BUTTON_HIT_AREA:			27021, /*this.topScreen should be greater than MAIN_SPOT*/
	WEAPON_ON_SPOT:						27022, /*this.topScreen should be greater than MAIN_SPOT*/
	BOSS_APPEARING_FLAMES_FX:			28003, /*this.topScreen*/
	BOSS_DISAPPEARING_FX:				28004, /*this.topScreen*/
	BOSS_DIE_RED_SCREEN:				28005, /*this.topScreen*/
	MINI_WINS_CONTENT: 					28007, /*this.topScreen*/
	BOSS_CAPTION:						28020, /*this.topScreen*/
	BOSS_YOU_WIN:						28021, /*this.topScreen*/
	BIG_WINS_CONTENT: 					30000, /*this.topScreen should be greater than boss content*/
}

class GameField extends Sprite
{
	static get EVENT_REFRESH_COMMON_PANEL_REQUIRED()				{return "onRefreshCommonPanelRequired";}
	static get EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED()				{return "onNotEnoughMoneyDialogRequired";}
	static get EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED()	{return 'onSWPurchaseLimitExceededDialogRequired';}
	static get EVENT_ON_BUY_AMMO_REQUIRED()							{return "onBuyAmmoRequired";}
	static get EVENT_ON_TIME_TO_CONVERT_BALANCE_TO_AMMO() 			{return "EVENT_ON_TIME_TO_CONVERT_BALANCE_TO_AMMO";}
	static get EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED()				{return "onBackToLobbyButtonClicked";}
	static get EVENT_ON_NEW_ENEMY_CREATED()							{return "newEnemyCreated";}
	static get EVENT_ON_NEW_BOSS_CREATED()							{return "newBossCreated";}
	static get EVENT_ON_BOSS_DESTROYING()							{return "bossDestroying";}
	static get EVENT_ON_BOSS_DESTROYED()							{return "bossDestroyed";}
	static get EVENT_ON_DRAGON_DISAPPEARED()						{return "EVENT_ON_DRAGON_DISAPPEARED";}
	static get EVENT_ON_END_SHOW_WEAPON()							{return "endShowWeapon";}
	static get EVENT_ON_DRAW_MAP()									{return "onDrawMap";}
	static get EVENT_ON_WEAPON_UPDATED()							{return "onWeaponUpdated";}
	static get EVENT_ON_GAME_FIELD_SCREEN_CREATED()					{return "onGameFieldScreenCreated";}
	static get EVENT_ON_ENEMY_HIT_ANIMATION()						{return "onEnemyHitAnimation";}
	static get EVENT_ON_ENEMY_MISS_ANIMATION()						{return "onEnemyMissAnimation";}
	static get EVENT_ON_ROOM_FIELD_CLEARED()						{return "roomFieldCleared";}
	static get EVENT_ON_GUN_UNLOCKED()								{return "gunUnlocked";}
	static get EVENT_ON_WEAPONS_INTERACTION_CHANGED()				{return "onWeaponsInteractionChanged";}
	static get EVENT_ON_ROUND_RESULT_FORCED()						{return "onRoundResultForced";}
	static get EVENT_ON_FREE_HIT_INTERRUPTED()						{return "onFreeHitInterrupted";}
	static get EVENT_ON_WEAPON_TO_UNPRESENTED_TRANSFER_REQUIRED()	{return "onWEaponToUnpresentedTransferRequired";}
	static get EVENT_ON_BET_MULTIPLIER_UPDATED()					{return "onBetMultiplierUpdate";}
	static get EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED()			{return "onBetMultiplierUpdateRequired";}
	static get EVENT_ON_EXPLODER_EXPLOSION_STARTED()				{return "onExploderExplosionStarted";}
	static get EVENT_ON_RICOCHET_BULLET_FLY_OUT()					{return "onRicochetBulletFlyOut";}
	static get EVENT_ON_RICOCHET_BULLET_REGISTER()					{return "onRicochetBulletRegister";}
	static get EVENT_ON_BULLET_CLEAR()								{return "onBulletClear";}
	static get EVENT_ON_CLEAR_BULLETS_BY_SEAT_ID()					{return "onClearBulletsBySeatId";}
	static get EVENT_ON_MASTER_SEAT_ADDED()							{return "onMasterSeatAdded";}
	static get EVENT_ON_CLEAR_ROOM_STARTED()						{return "onRoomClearStarted";}
	static get EVENT_ON_START_UPDATE_CURSOR_POSITION()				{return "onStartUpdateCursorPosition";}
	static get EVENT_ON_STOP_UPDATE_CURSOR_POSITION()				{return "onStopUpdateCursorPosition";}
	static get EVENT_ON_SET_SPECIFIC_CURSOR_POSITION()				{return "onSetSpecificCursorPosition";}

	static get EVENT_SHOW_FIRE()									{return 'EVENT_SHOW_FIRE';}
	static get EVENT_ON_BULLET_FLY_TIME()							{return 'EVENT_ON_BULLET_FLY_TIME';}
	static get EVENT_ON_BULLET_TARGET_TIME()						{return 'EVENT_ON_BULLET_TARGET_TIME';}
	static get EVENT_DECREASE_AMMO()								{return 'EVENT_DECREASE_AMMO';}
	static get EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING()	{return 'EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING';}
	static get EVENT_ON_TARGETING()									{return 'EVENT_ON_TARGETING';}
	static get EVENT_ON_RESET_TARGET()								{return 'EVENT_ON_RESET_TARGET';}
	static get EVENT_ON_ENEMY_KILLED_BY_PLAYER()					{return 'EVENT_ON_ENEMY_KILLED_BY_PLAYER';}
	static get EVENT_ON_CEILING_DUST_ANIMATION_STARTED()			{return 'EVENT_ON_CEILING_DUST_ANIMATION_STARTED';}
	static get EVENT_ON_CEILING_DUST_ANIMATION_COMPLETED()			{return 'EVENT_ON_CEILING_DUST_ANIMATION_COMPLETED';}
	static get EVENT_ON_GRENADE_EXPLOSION_ANIMATION_COMPLETED()		{return 'EVENT_ON_GRENADE_EXPLOSION_ANIMATION_COMPLETED';}
	static get EVENT_ON_CHANGE_WEAPON_FAILED()						{return 'EVENT_ON_CHANGE_WEAPON_FAILED';}
	static get EVENT_ON_TARGET_ENEMY_IS_DEAD()						{return 'EVENT_ON_TARGET_ENEMY_IS_DEAD';}
	static get EVENT_ON_HIT_AWARD_EXPECTED()						{return 'EVENT_ON_HIT_AWARD_EXPECTED';}
	static get EVENT_ON_SHOT_SHOW_FIRE_START_TIME()					{return 'EVENT_ON_SHOT_SHOW_FIRE_START_TIME';}
	static get EVENT_ON_FIRE_CANCELLED_WITH_NOT_ENOUGH_AMMO()		{return 'EVENT_ON_FIRE_CANCELLED_WITH_NOT_ENOUGH_AMMO';}

	static get EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME()		{return 'EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME';}
	static get EVENT_ON_TOTAL_WIN_UPDATED() 						{return "EVENT_ON_TOTAL_WIN_UPDATED";}

	static get EVENT_RELOAD_REQUIRED()								{return MainPlayerSpot.EVENT_RELOAD_REQUIRED;}
	static get EVENT_ON_ENEMY_VIEW_REMOVING()						{return Enemy.EVENT_ON_ENEMY_VIEW_REMOVING;}
	static get EVENT_ON_DEATH_ANIMATION_STARTED()					{return Enemy.EVENT_ON_DEATH_ANIMATION_STARTED;}
	static get EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT()				{return Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT;}
	static get EVENT_ON_ENEMY_PAUSE_WALKING()						{return Enemy.EVENT_ON_ENEMY_PAUSE_WALKING;}
	static get EVENT_ON_ENEMY_RESUME_WALKING()						{return Enemy.EVENT_ON_ENEMY_RESUME_WALKING;}
	static get EVENT_ON_ENEMY_FREEZE()								{return Enemy.EVENT_ON_ENEMY_FREEZE;}
	static get EVENT_ON_ENEMY_UNFREEZE()							{return Enemy.EVENT_ON_ENEMY_UNFREEZE;}
	static get EVENT_ON_BOSS_DEATH_FLARE()							{return Enemy.EVENT_ON_DEATH_ANIMATION_FLARE;}
	static get EVENT_ON_BOSS_DEATH_CRACK()							{return Enemy.EVENT_ON_DEATH_ANIMATION_CRACK;}
	static get EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME()				{return BossEnemy.EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME;}

	static get EVENT_ON_TIME_TO_EXPLODE_COINS()						{return Enemy.EVENT_ON_TIME_TO_EXPLODE_COINS;}
	static get EVENT_ON_NEW_ROUND_STATE()							{return GameScreen.EVENT_ON_NEW_ROUND_STATE;}
	static get EVENT_ON_WAITING_NEW_ROUND()							{return GameScreen.EVENT_ON_WAITING_NEW_ROUND;}
	static get EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED()	{return RoundResultScreenController.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED;}
	static get EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED()			{return RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED;}
	static get EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED()				{return RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED;}
	static get EVENT_ON_BATTLEGROUND_NEXT_ROUND_CLICKED() 			{return RoundResultScreenController.EVENT_ON_BATTLEGROUND_NEXT_ROUND_CLICKED; }
	static get EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATION_START()		{return RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATION_START;}
	static get EVENT_TIME_TO_VALIDATE_CURSOR() 						{return "EVENT_TIME_TO_VALIDATE_CURSOR";}
	static get EVENT_TIME_TO_SHOW_PRIZES() 							{return "EVENT_TIME_TO_SHOW_PRIZES";}
	static get EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED()				{return CriticalHitAnimation.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED;}
	static get EVENT_ON_PUWER_UP_ANIMATION_ENDED()					{return PowerUpMultiplierAnimation.EVENT_ON_ANIMATION_ENDED;}
	static get EVENT_ON_CRITICAL_HIT_ANIMATION_START() 				{return "EVENT_ON_CRITICAL_HIT_ANIMATION_START";}
	static get EVENT_ON_ENEMY_ENERGY_UPDATED()						{return Enemy.EVENT_ON_ENEMY_ENERGY_UPDATED;}
	static get EVENT_ON_TRY_TO_SKIP_BIG_WIN() 						{return "EVENT_ON_TRY_TO_SKIP_BIG_WIN";}
	static get DEFAULT_GUN_SHOW_FIRE() 								{return "DEFAULT_GUN_SHOW_FIRE";}
	static get EVENT_ON_RICOCHET_BULLET_DESTROY()					{return RicochetBullet.EVENT_ON_RICOCHET_BULLET_DESTROY;}
	static get EVENT_ON_RICOCHET_BULLETS_UPDATED()					{return RicochetController.EVENT_ON_RICOCHET_BULLETS_UPDATED;}
	static get EVENT_ON_BULLET_PLACE_NOT_ALLOWED()					{return GameScreen.EVENT_ON_BULLET_PLACE_NOT_ALLOWED;}
	static get EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED()			{return "EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED";}
	static get EVENT_BACK_TO_LOBBY_ON_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED()		{return "EVENT_BACK_TO_LOBBY_ON_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED";}
	static get EVENT_FULL_GAME_INFO_REQUIRED()						{return "EVENT_FULL_GAME_INFO_REQUIRED"}

	static get EVENT_ORB_CALLOUT_CREATED()							{return BlueOrb.EVENT_ORB_CALLOUT_CREATED; }

	static get EVENT_ON_UPDATE_PLAYER_WIN_CAPTION()					{return "EVENT_ON_UPDATE_PLAYER_WIN_CAPTION";}

	static get EVENT_ON_DEATH_COIN_AWARD()							{return Enemy.EVENT_ON_DEATH_COIN_AWARD;}

	static get EVENT_ON_AUTOFIRE_BUTTON_ENABLED()					{ return BattlegroundMainPlayerSpot.EVENT_ON_AUTOFIRE_BUTTON_ENABLED; }
	static get EVENT_ON_AUTOFIRE_BUTTON_DISABLED()					{ return BattlegroundMainPlayerSpot.EVENT_ON_AUTOFIRE_BUTTON_DISABLED; }

	get ricochetController()
	{
		return this._ricochetController;
	}

	get roundResultScreenController()
	{
		return this._roundResultScreenController;
	}

	get fireSettingsScreenActive()
	{
		return this._fFireSettingsScreenActive_bln;
	}

	get battlegroundFinalCountingController()
	{
		return this._battlegroundFinalCountingController;
	}

	get newWeapons()
	{
		let lNewWeapons_arr = [];

		for (let i = 0; i < this._fNewWeapons_arr.length; ++i)
		{
			let lAward_obj = this._fNewWeapons_arr[i];
			let lContentItems_arr = null;

			if (lAward_obj instanceof Crate)
			{
				lContentItems_arr = lAward_obj.contentItems;
			}
			else if (lAward_obj instanceof ContentItem)
			{
				lContentItems_arr = [lAward_obj];
			}

			for (let j = 0; j < lContentItems_arr.length; ++j)
			{
				let lNewWeapon_obj = lContentItems_arr[j].info ? lContentItems_arr[j].info.awardedWeapon : lContentItems_arr[j].awardedWeapon;
				if (lNewWeapon_obj)
				{
					lNewWeapons_arr.push(lNewWeapon_obj);
				}
			}
		}

		return lNewWeapons_arr;
	}

	get isGunLocked()
	{
		return this.gunLocked;
	}

	get totalWin()
	{
		return this._fTotalWin_num;
	}

	set totalWin(aValue_num)
	{
		this._fTotalWin_num = aValue_num;
		this.emit(GameField.EVENT_ON_TOTAL_WIN_UPDATED, {value: this._fTotalWin_num});
	}

	set isWinLimitExceeded(aValue_bl)
	{
		this._fIsWinLimitExceeded_bl = aValue_bl;
	}

	onSomeEnemySpawnSoundRequired(aEnemyTypeId_int)
	{
		this.emit(GameField.EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED, {enemyTypeId: aEnemyTypeId_int});
	}

	tryToBuyAmmo()
	{
		this._tryToBuyAmmo();
	}

	updateWeaponImmediately(aWeapon_obj, aIsNewAwardedLevelUp_bl=false)
	{
		this._updateWeaponImmediately(aWeapon_obj, aIsNewAwardedLevelUp_bl);
	}

	resetTargetIfRequired(aAnyway_bl = false)
	{
		this._resetTargetIfRequired(aAnyway_bl);
	}

	startTeleportAnimation(aEnemy_we, aEnemyPosition_obj, aEnemyScale_num, aOptDirection, aLabel)
	{
		this._startTeleportAnimation(aEnemy_we, aEnemyPosition_obj, aEnemyScale_num, aOptDirection, aLabel);
	}

	pauseTeleportAnimation()
	{
		this._pauseTeleportAnimation();
	}

	resumeTeleportAnimation()
	{
		this._resumeTeleportAnimation();
	}

	resetWaitBuyIn()
	{
		this.waitBuyIn = false;
		this.spot && (this.spot.reloadRequiredSent = false);
	}

	showPrizes(data, aPrizePosition_pt, aEnemyId_int, aIsBoss_bl)
	{
		this._showPrizes(data, aPrizePosition_pt, aEnemyId_int, aIsBoss_bl);
	}

	getBackgroundContainer()
	{
		return this.backContainer;
	}

	constructor()
	{
		super();

		this._fCurrentKingsOfTheHillSeatId_int_arr = [];
		this.waitScreen = null;

		this._graphicsBack = this.addChild(new PIXI.Graphics());
		this._graphicsBack.beginFill(0xb0aeaf).drawRect(-960/2, -540/2, 960, 540).endFill();

		this.container = this.addChild(new Sprite);
		this.backContainer = this.container.addChild(new GameFieldBackContainer);

		this._screenGradient = null;

		this._fCritAnims_arr = [];
		this._fPowerUpMultipliers_puma_arr = [];
		this._fFxAnims_arr = [];
		this._fNewWeapons_arr = [];
		this._defaultGunFireEffects_sprt_arr = [];
		this._fTeleportBulgeFilters_f_arr = [];
		this._fWitchEnemyTeleportAnimations_ceta_arr = [];
		this._fWitchSequences_arr = [];

		this._fRoundResultAnimsCount_num = 0;
		this._fRoundEndListenHandlers_arr = [];

		this._fRoundResultRestoreWeapon_num = null;

		this.enemies = [];
		this.deadEnemies = [];
		this.bullets = [];
		this.seatId = -1;
		this.nickname = undefined;
		this.currentScore = 0;
		this.currentWin = 0;
		this.currentWinTimeout = null;

		this._fWeaponToRestore_int = null;

		this.playersContainer = null;
		this.spot = null;

		this.waitBuyIn = false;

		this.gunLocked = false;

		this.indicatedEnemy = null;

		this._fLobbySecondaryScreenActive_bln = false;
		this._fWeaponSwitchTry_bln = false;
		this._fWeaponSwitchInProgress_bln = false;

		this._fIsWeaponAddingInProgress_bl = false; // starting from revealing weapon emblem from the crate
		this._fRoundResultBuyAmmoRequest_bln = false;
		this._fRoundResultActive_bln = false;
		this._fRoundResultResponseReceived_bl = false;
		this._fExternalBalanceUpdated_bl = false;
		this._fFireSettingsScreenActive_bln = false;
		this._fNeedExplodeFeatureInitiated_bln = false;
		this._fRoundResultOnLasthandWaitState_bln = false;
		this._isNeedRotate_bl = true;

		this._fFirstPicksUpWeapon_num = null;

		this.turretRotationTime = 0;
		this.cursorKeysMoveTime = 0;
		this.pointerPushed = false;
		this.autofireTime = 0;
		this._fLastFireTime_num = 0;
		this.lastPointerPos = {
			x: 0,
			y: 0
		};

		this.players = [];

		this.playerPosition = null;

		this._fIsWinLimitExceeded_bl = null;

		this.playerRewardContainer = null;

		this._fMidRoundExitDialogActive_bln = false;
		this._fBonusDialogActive_bln = false;
		this._fFRBDialogActive_bln = false;

		this._fRoundResultScreenView_bwsv = null;
		this._fRoundResultScreenController_bwsv = null;

		this._fFireSettingsView_fsv = null;

		this.shotRequestsAwaiting = 0;

		this.enemiesLastPositions = {};

		this._fWeaponsController_wsc = APP.currentWindow.weaponsController;
		this._fWeaponsInfo_wsi = this._fWeaponsController_wsc.i_getInfo();

		this._fGameStateController_gsc = APP.currentWindow.gameStateController;
		this._fGameStateInfo_gsi = this._fGameStateController_gsc.info;

		this._fTargetingController_tc = APP.currentWindow.targetingController;
		this._fTargetingInfo_tc = this._fTargetingController_tc.info;
		this._fTargetingController_tc.on(TargetingController.EVENT_ON_TARGET_RESET, this._tryToResumeAutofire, this);
		
		this._fAutofireButtonEnabled_bl = false;

		this._fAutoTargetingSwitcherController_atsc = APP.currentWindow.autoTargetingSwitcherController;
		this._fAutoTargetingSwitcherInfo_atsi = this._fAutoTargetingSwitcherController_atsc.info;

		this._fBossModeController_bmc = APP.currentWindow.bossModeController;

		this._fCurChangeWeaponTimerInfo_obj = null;

		this._fGameOptimizationController_goc = APP.currentWindow.gameOptimizationController;
		this._fGameOptimizationInfo_goi = this._fGameOptimizationController_goc.info;

		this._fCryogunsController_csc = APP.currentWindow.cryogunsController;
		this._fCryogunsInfo_csi = this._fCryogunsController_csc.i_getInfo();

		this._fFlameThrowersController_ftsc = APP.currentWindow.flameThrowersController;
		this._fFlameThrowersInfo_ftsi = this._fFlameThrowersController_ftsc.i_getInfo();
		this._fFlameThrowersController_ftsc.on(FlameThrowersController.EVENT_ON_BEAM_ROTATION_UPDATED, this._onFlameThrowerBeamRotationUpdated, this);

		this._fArtilleryStrikesController_assc = APP.currentWindow.artilleryStrikesController;
		this._fArtilleryStrikesInfo_assi = this._fArtilleryStrikesController_assc.i_getInfo();
		this._fArtilleryStrikesController_assc.on(ArtilleryStrikesController.EVENT_ON_STRIKE_MISSILE_HIT, this._onArtilleryStrikeMissileHit, this);
		this._fArtilleryStrikesController_assc.on(ArtilleryStrikesController.EVENT_ON_ARTILLERY_GRENADE_LANDED, this._artilleryGrenadeLanded, this);

		this._fPlazmaGunsController_pgsc = APP.currentWindow.plazmaGunsController;
		this._fPlazmaGunsInfo_pgsc = this._fPlazmaGunsController_pgsc.i_getInfo();
		this._fPlazmaGunsController_pgsc.on(PlazmaGunsController.EVENT_ON_BEAM_TARGET_PRE_ACHIEVED, this._plazmagunBeamTargetPreAchieved, this);

		this._fRicochetLasersController_rlsc = APP.currentWindow.ricochetLasersController;

		this._fConnectionClosed_bln = false;
		this._fConnectionHasBeenRecentlyClosed_bln = false;
		this._fUnpresentedWeaponSurplus_num = null;

		this._fWeaponsSidebarController_wssc = null;
		this._fTreasuresSidebarController_tsc = null;

		this._fTournamentModeInfo_tmi = null;

		this._fServerMessageWeaponSwitchExpected_bln = false;
		this._fLastRequestedWeaponId_int = undefined;
		this._fLastReceivedWeaponId_int = undefined;

		this._isWaitingResponseToBetLevelChangeRequest = false;

		this._fCommonPanelIndicatorsData_obj = null;

		this._fWeaponOffsetYFiringContinuously_int = null;
		this._fPushWeaponEffectPlaying_bl = null;
		this._fCurrentWeaponSpotGun = null;

		this._fBetLevelPlusButtonHitArea_b = null;
		this._fBetLevelMinusButtonHitArea_b = null;
		this._isPlusBetLevelChangeRequiredAfterFiring_bl = null;
		this._isMinusBetLevelChangeRequiredAfterFiring_bl = null;

		this._fRestoreAfterFreeShotsWeaponId_int = undefined;

		this._fDelayedStopOnBossSubroundTimerReference_ref = null;

		this._fBattlegroundFinalCountingController_fcc = null;

		this._fSpecialAreasMap_sam = new SpecialAreasMap();
		this._fMissEffectsPool_mep = null;

		this._fCurrentLevelUpTimeout = null;

		// DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		// ...DEBUG

		this._fGameStateWaitIsWatingBattlegroundRoundResult_bl = null;

		this._fEnemiesPlantWithPlayPreDeathAnimation_arr = [];
		this._fIsPlantEnemyOutroAnimationOnRoundResultWasInterrupted_bl = null;
	}

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 106)
	// 	{
	// 		window.SEIDs = !window.SEIDs;

	// 		for (const enemy of this.enemies)
	// 		{
	// 			enemy.enemyIndicatorsController.view.toggleIdsIndicator();
	// 		}

	// 	}
	// }
	//...DEBUG


	hasAnyBullets()
	{
		return this.bullets.length > 0;
	}

	get witchEnemyTeleportAnimationContainer()
	{
		return this.teleportFXContainer;
	}

	get isRoundStatePlay()
	{
		return this._fGameStateInfo_gsi ? (this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY) : false;
	}

	get lobbySecondaryScreenActive()
	{
		return this._fLobbySecondaryScreenActive_bln;
	}

	set lobbySecondaryScreenActive(aVal_bln)
	{
		this._fLobbySecondaryScreenActive_bln = aVal_bln;
		this._validateInteractivity();
	}

	get enemiesContainer()
	{
		return this.bottomScreen;
	}

	get specialAreasMap()
	{
		return this._fSpecialAreasMap_sam;
	}

	get isWaitingAnimationsEnd()
	{
		return this._fRoundResultAnimsCount_num > 0;
	}

	get awardingContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.AWARDED_WIN_CONTENT, stackContainer: this.topScreen, keySparklesContainer: this.topScreen}
	}

	get bigWinsContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BIG_WINS_CONTENT};
	}

	get mapContainers()
	{
		return {backContainer: this.backContainer, decorationsContainer: this.bottomScreen, torchesContainer: this.bottomScreen};
	}

	get cryogunEffectContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BULLET};
	}

	get flameThrowerEffectContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BULLET};
	}

	get artilleryStrikeEffectContainer()
	{
		return {grenadeContainer: this.bottomScreen, strikeContainer: this.topScreen, zIndex: Z_INDEXES.BULLET};
	}

	get plazmagunEffectContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BULLET};
	}

	get lasersContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BULLET};
	}

	get trollFxContainer()
	{
		return {container: this.bottomScreen};
	}

	get smokeTrailContainerInfo()
	{
		return {container: this.bottomScreen, zIndex: Z_INDEXES.SMOKE_TRAIL};
	}

	get stepsTrailContainer()
	{
		return this.screen ? this.screen.stepsTrailContainer : null;
	}

	get mapFXAnimationContainer()
	{
		return {container: this.bottomScreen, zIndex: Z_INDEXES.MAP_FX_ANIMATION};
	}

	get teleportFXContainer()
	{
		return {container: this.bottomScreen,  zIndex: Z_INDEXES.TELEPORT_FX_ANIMATION};
	}

	get transitionViewContainerInfo()
	{
		return {container: this, zIndex: Z_INDEXES.TRANSITION_VIEW};
	}

	get btgFinalCountingViewContainerInfo()
	{
		return {container: this, zIndex: Z_INDEXES.BTG_FINAL_COUNTING};
	}

	//TARGETING...
	get targetingContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.TARGETING };
	}

	get autoTargetingEnemy()
	{
		let lTargetEnemyId_int = APP.currentWindow.targetingController.info.targetEnemyId;
		return this.getExistEnemy(lTargetEnemyId_int);
	}

	get autoTargetingSwitcherContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.AUTO_TARGETING_SWITCHER };
	}
	//...TARGETING

	get subloadingContainerInfo()
	{
		return {container: this, zIndex: Z_INDEXES.SUBLOADING /*+1 to the WaitScreen*/};
	}

	get bossModeAppearingContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BOSS_APPEARING_FX, captionZIndex: Z_INDEXES.BOSS_CAPTION, circleZIndex: Z_INDEXES.BOSS_APPEARING_CIRCLE_FX };
	}

	get bossModeDisappearingContainerInfo()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.BOSS_DISAPPEARING_FX, dieRedScreenZIndex: Z_INDEXES.BOSS_DIE_RED_SCREEN, youWinZIndex: Z_INDEXES.BOSS_YOU_WIN};
	}

	get criticalHitAnimationContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.CRITICAL_HIT};
	}

	get powerUpMultiplierAnimationContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.POWER_UP_MULTIPLIER};
	}

	get treasuresAnimationContainer()
	{
		return {container: this.topScreen, zIndex: Z_INDEXES.AWARDED_WIN_CONTENT};
	}

	_getSpotPosition(positionId, isMasterSpot_bl)
	{
		let positions = APP.isMobile ? PLAYERS_POSITIONS["MOBILE"] : PLAYERS_POSITIONS["DESKTOP"]
		let spotPosDescr = positions[positionId];

		let spotPos = {
			x: 		spotPosDescr.x + (isMasterSpot_bl ? spotPosDescr.masterOffset.x : 0),
			y: 		spotPosDescr.y + (isMasterSpot_bl ? spotPosDescr.masterOffset.y : 0),
			direct: spotPosDescr.direct,
		}

		return spotPos;
	}

	getEnemyLastPosition(enemyId)
	{
		return this.enemiesLastPositions[enemyId];
	}

	extractUniqueEnemiesIds(aEnemies_obj_arr)
	{
		let uniqueEnemiesIds = [];
		for (let enemy of aEnemies_obj_arr)
		{
			let enemyId = enemy.enemyId;
			if (enemyId < 0 || uniqueEnemiesIds.indexOf(enemyId) > -1)
			{
				continue;
			}
			uniqueEnemiesIds.push(enemyId);
		}
		return uniqueEnemiesIds;
	}

	checkIfAutoTargetSwitchNeeded()
	{
		this._checkIfAutoTargetSwitchNeeded();
	}

	addRoomGradient()
	{
		if (!this._screenGradient)
		{
			var gradient = this.screen.addChild(APP.library.getSprite('gradient'));
			gradient.scale.x = 1.026; // to avoid gaps when ground shaking
			gradient.position.set(480, 525);
			gradient.zIndex = Z_INDEXES.GRADIENT;
			this._screenGradient = gradient;
		}
	}

	removeRoomGradient()
	{
		this._screenGradient && this._screenGradient.destroy();
		this._screenGradient = null;
	}

	redrawMap()
	{
		this.emit(GameField.EVENT_ON_DRAW_MAP);
	}

	_onCurrentMapUpdated()
	{
		this.validateState();
	}

	validateState()
	{
		if (this._fGameStateInfo_gsi.gameState == ROUND_STATE.WAIT)
		{
			APP.gameScreen.roundFinishSoon = false;
			this.changeState(this._fGameStateInfo_gsi.gameState);
		}
		else if (this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY)
		{
			APP.gameScreen.roundFinishSoon = false;
			this._fConnectionHasBeenRecentlyClosed_bln = false;
			this._fConnectionClosed_bln = false;
			if(!APP.isBattlegroundGame) this._onTimeToShowTutorial();
		}
	}

	resetRoomMap()
	{
		let backOffsetY_num = ((APP.config.margin.bottom || 0) - (APP.config.margin.top || 0)) / 2;
		let back = this.backContainer.addChild(APP.library.getSprite('preloader/back'));
		back.position.y = backOffsetY_num;
	}

	updateEnemyTrajectory(enemyId, trajectory)
	{
		let enemy = this.getExistEnemy(enemyId);
		if (enemy)
		{
			enemy.updateTrajectory(trajectory);
		}
	}

	isGamePlayInProgress()
	{
		return (
			this.waitScreen === null &&
			this.enemies &&
			this.enemies.length > 0);
	}

	removeWaitScreen()
	{
		if (this.waitScreen)
		{
			if (this.waitScreen.spinner)
			{
				this.waitScreen.spinner.destroy();
			}
			this.waitScreen.destroy();
			this.waitScreen = null;

			this.container.filters = null;
			this.container.interactiveChildren = true;

			this._checkBlur();
			this._validateCursor();
		}
	}

	showWaitScreen()
	{
		if (this.waitScreen)
		{
			return;
		}

		this.waitScreen = this.addChild(APP.library.getSprite('level_ui/wait'));
		this.waitScreen.position.set(0, 2);
		this.waitScreen.zIndex = Z_INDEXES.WAIT_SCREEN;

		let lBack_spr = this.waitScreen.addChild(APP.library.getSprite('preloader/loading_back'));

		let lCaption_cta = this.waitScreen.addChild(I18.generateNewCTranslatableAsset('TAWaitScreenGameInProgressCaption'));
		lCaption_cta.position.set(0, 17);

		this.waitScreen.spinner = this.addChild(new PreloadingSpinner(2100, 110));
		this.waitScreen.spinner.position.y = -55;
		this.waitScreen.spinner.startAnimation();

		this.container.filterArea = new PIXI.Rectangle(-2, -2, 964, 844);
		this.container.interactiveChildren = false;
	}

	showBlur()
	{
		this._showBlur();
	}

	checkBlur()
	{
		this._checkBlur();
	}

	hideBlur()
	{
		this._hideBlur();
	}

	onCloseRoom()
	{
		this._onCloseRoom();
	}

	init()
	{
		this.on('pointerdown', (e)=> this.pushPointer(e));
		this.on('pointermove', (e) => {
			this.emit(GameField.EVENT_ON_START_UPDATE_CURSOR_POSITION);

			this.tryRotateGun(e);
			if (!this.roundResultActive)
			{
				this._validateCursor();
			}
		});
		this.on('pointerup', (e) => this.unpushPointer(e));
		this.on('pointerupoutside', (e) => this.unpushPointer(e));
		window.addEventListener('mouseup', (e) => {
			if(e.button === 1){
				this.unpushPointer(e);
			}
		});
		this.on('rightclick', (e) => this.onPointerRightClick(e));
		this.on('pointerclick', (e) => this.onPointerClick(e));
		APP.on('onTickerPaused', (e) => this.unpushPointer(e));
		APP.on('onTickerResumed', (e) => this._onTickerResumed(e));

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
		APP.keyboardControlProxy.on(KeyboardControlProxy.i_EVENT_BUTTON_CLICKED, this._onKeyboardButtonClicked, this);

		this._roundResultScreenController.init();
		this._fireSettingsController.initView(this._fireSettingsView);

		APP.on(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.playerController.on(GamePlayerController.EVENT_ON_WEAPON_SURPLUS_UPDATED, this._onWeaponSurplusUpdated, this);

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_WEAPON_SWITCHED_MESSAGE, this._onServerWeaponSwitchedMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_REQUEST_WEAPON_UPDATE_SENDED, this._onWeaponUpdated, this);

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_DELAYED_RICOCHET_SHOT_REMOVED, this._onDelayedRicochetShotRemoved, this);

		this._roundResultScreenController.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this._onRoundResultDeactivatedOrSkipped, this);
		this._roundResultScreenController.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_SKIPPED, this._onRoundResultDeactivatedOrSkipped, this);

		this._roundResultScreenController.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenActivated, this);
		this._roundResultScreenController.on(RoundResultScreenController.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED, this._onBackToLobbyRoundResultButtonClicked, this);

		this._fireSettingsController.on(FireSettingsController.EVENT_ON_FIRE_SETTINGS_CHANGED, this._onFireSettingsChanged, this);
		this._fireSettingsController.on(FireSettingsController.EVENT_SCREEN_ACTIVATED, this._onFireSettingsActivated, this);
		this._fireSettingsController.on(FireSettingsController.EVENT_SCREEN_DEACTIVATED, this._onFireSettingsDeactivated, this);

		// this._fBossModeController_bmc.on(BossModeController.EVENT_ON_CAPTION_APPEARING_STARTED, this._onBossCaptionAppearStarted, this);
		// this._fBossModeController_bmc.on(BossModeController.EVENT_DISAPPEARING_PRESENTATION_STARTED, this._onBossDisappearPresentationStarted, this);
		this._fBossModeController_bmc.on(BossModeController.EVENT_SHAKE_THE_GROUND_REQUIRED, this._onBossModeAppearingShakeTheGroundRequired, this);

		this._fTargetingController_tc.on(TargetingController.EVENT_ON_PAUSE_STATE_UPDATED, this._onTargetingControllerPauseStateUpdate, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_NO_EMPTY_SEATS, this._onNoPlaceToSeatHandler, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_DIALOG_ACTIVATED, this._onDialogActivated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_DIALOG_DEACTIVATED, this._onDialogDectivated, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_FRB_ENDED_COMPLETED, this._onFRBEndedCompleted, this);

		APP.currentWindow.mapsController.on(MapsController.EVENT_CURRENT_MAP_ID_UPDATED, this._onCurrentMapUpdated, this);

		// this._fBossModeController_bmc.on(BossModeController.EVENT_ON_TIME_TO_SCALE_MAP, this._onBossScaleMapRequired, this);
		// this._fBossModeController_bmc.on(BossModeController.EVENT_ON_TIME_TO_BLUR_MAP, this._onBossBlurMapRequired, this);
		// this._fBossModeController_bmc.on(BossModeController.EVENT_ON_FADED_BACK_HIDE_REQUIRED, this._onBossModeFadedBackHiding, this);
		// this._fBossModeController_bmc.on(BossModeController.EVENT_ON_GROUND_BURN_REQUIRED, this._onBossGroundFireTraceRequired, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_BET_LEVEL_CHANGE_CONFIRMED, this._onBetLevelChangeConfirmed, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BET_LEVEL_CHANGE_NOT_CONFIRMED, this._onBetLevelChangeNotConfirmed, this);

		APP.currentWindow.gameFrbController.on(GameFRBController.EVENT_ON_FRB_MODE_CHANGED, this._onFRBModeChanged, this);

		let tournamentModeController = APP.tournamentModeController;
		this._fTournamentModeInfo_tmi = tournamentModeController.info;
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_SERVER_STATE_CHANGED, this._onTournamentModeServerStateChanged, this);
		tournamentModeController.on(TournamentModeController.EVENT_ON_TOURNAMENT_CLIENT_STATE_CHANGED, this._onTournamentModeClientStateChanged, this);

		APP.gameScreen.on(GameScreen.EVENT_ON_BULLET_RESPONSE, this._onBulletResponse, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BULLET_CLEAR_RESPONSE, this._onBulletClearResponse, this);

		this._fRicochetInfo_ri = this._ricochetController.info;

		APP.gameScreen.on(GameScreen.EVENT_ON_LASTHAND_BULLETS, this._onLasthandBullets, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, this._onBulletPlaceNotAllowed, this);

		APP.pendingOperationController.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
		APP.pendingOperationController.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);

		this._tryToProceedNewPlayer();

		this._validateCursor();

		//new SpecialAreasMapEditor();
	}

	_onNoPlaceToSeatHandler(event)
	{
		this.clearRoom(false, true, true);
	}

	_onLasthandBullets(aEvent_obj)
	{
		let bullets = aEvent_obj.bullets;
		let alreadySeatId = aEvent_obj.alreadySitInNumber;
		let isClearMasterBulletsRequired = false;

		for (let bullet of bullets)
		{
			let bulletId = bullet.bulletId;
			let seatId = +bulletId.slice(0, 1);
			let weaponId = bullet.weaponId;

			if (seatId === alreadySeatId)
			{
				isClearMasterBulletsRequired = true;
				continue;
			}

			if (this._isRicochetBulletExist(bulletId))
			{
				continue;
			}

			let startPos = {x: bullet.startPointX, y: bullet.startPointY};
			let distanationPos = {x: bullet.endPointX, y: bullet.endPointY};

			this.ricochetFire(distanationPos, seatId, bulletId, weaponId, bullet.bulletTime, startPos, true);
		}

		if (isClearMasterBulletsRequired)
		{
			this.clearRicochetBullets();
		}
	}

	_onWeaponUpdated(event)
	{
		this._fServerMessageWeaponSwitchExpected_bln = true;
		this._fLastRequestedWeaponId_int = event.weaponId;
	}

	_onResetSWSwitched()
	{
		this.tryToChangeWeapon(WEAPONS.DEFAULT);
		this._fServerMessageWeaponSwitchExpected_bln = false;
		this._fWeaponSwitchTry_bln = false;
		this._fWeaponSwitchInProgress_bln = false;
	}

	_onServerMessage(event)
	{
		let messageData = event.messageData;
		let messageClass = messageData.class;

		switch( messageClass )
		{
			case SERVER_MESSAGES.KING_OF_HILL_CHANGED:
				this.showKingsOfTheHill(messageData.newKings);
				break;
			case "Miss":
			case "Hit":
				if(messageData.seatId != this.seatId)
				{
					if(this._isNeedRotate_bl && !this._isRicochetBulletExist() && !this._fireSettingsInfo.autoFire)
					{
						this.rotatePlayerGun(messageData.seatId, messageData.x, messageData.y);
					}
					else
					{
						this._isNeedRotate_bl = true;
					}
				}
		}
	}

	_isRicochetBulletExist(aOptBulletId_str=undefined)
	{
		for(var i = 0; i < this.bullets.length; i++)
		{
			let lCurBullet = this.bullets[i];
			if ( lCurBullet instanceof RicochetBullet)
			{
				if (aOptBulletId_str === undefined || aOptBulletId_str === lCurBullet.bulletId)
				{
					return true;
				}
			}
		}
		return false;
	}

	i_getKingsOfTheHillSeatId()
	{
		return this._fCurrentKingsOfTheHillSeatId_int_arr;
	}

	showKingsOfTheHill(aSeatId_int_arr)
	{
		let lSeatId_int_arr = (aSeatId_int_arr.length > 0) ? aSeatId_int_arr : this._fCurrentKingsOfTheHillSeatId_int_arr;

		if( (lSeatId_int_arr === undefined) || (lSeatId_int_arr.length === 0) )
		{
			return;
		}


		for( let i = 0; i < PLAYERS_POSITIONS["DESKTOP"].length; i++ )
		{
			let lSeat_s = this.getSeat(i, true)

			if(lSeat_s)
			{
				lSeat_s.setBattlegroundCrownVisible(false);
			}
		}
		for ( let i = 0; i < lSeatId_int_arr.length; i++ )
		{
			let lSeat_s = this.getSeat(lSeatId_int_arr[i], true)
			if(lSeat_s)
			{
				lSeat_s.setBattlegroundCrownVisible(true);
			}
		}
		this._fCurrentKingsOfTheHillSeatId_int_arr = lSeatId_int_arr;
	}

	_onServerWeaponSwitchedMessage(event)
	{
		this._fLastReceivedWeaponId_int = event.messageData.weaponId;

		if(this._fLastReceivedWeaponId_int === this._fLastRequestedWeaponId_int)
		{
			this._fServerMessageWeaponSwitchExpected_bln = false;
		}
	}

	_onFRBModeChanged(e)
	{
		let lIsFRBMode_bln = e.value;
		if (lIsFRBMode_bln && APP.gameScreen.room)
		{
			this.resetPlayerWin();
			this.updatePlayerWin(APP.gameScreen.room.alreadySitInWin);
		}
	}

	_onBetLevelChangeConfirmed(aEvent_obj)
	{
		let lSeatId_int = aEvent_obj.seatId;
		let lNewBetLevel_int = aEvent_obj.betLevel;
		let l_pi = APP.playerController.info;

		if (this.spot && this.spot.player.seatId === lSeatId_int)
		{
			this.spot.onBetChangeConfirmed(lNewBetLevel_int);
		}
		else if (l_pi.seatId === lSeatId_int && APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			let lBetId_num = l_pi.possibleBetLevels.indexOf(lNewBetLevel_int) > -1 ? l_pi.possibleBetLevels.indexOf(lNewBetLevel_int) : 0;
			this._onPlayerBetMultiplierUpdated( { id: lBetId_num, multiplier: lNewBetLevel_int} );
		}
		else
		{
			let lPlayerInfo_obj = this.getPlayerBySeatId(lSeatId_int)
			let lPrevBetLevel_int = l_pi.roomDefaultBetLevel;
			let lSpot_ps = this.getSeat(lSeatId_int);
			let lCurrentWeaponId = lSpot_ps && lSpot_ps.currentWeaponId;

			if (lPlayerInfo_obj)
			{
				lPrevBetLevel_int = lPlayerInfo_obj.betLevel;
				lPlayerInfo_obj.betLevel = lNewBetLevel_int;
				lCurrentWeaponId = lPlayerInfo_obj.currentWeaponId
			}

			let lMult_int = l_pi.getTurretSkinId(lNewBetLevel_int); // from 1 to 5 usually

			if (!APP.isBattlegroundGame && lSpot_ps && lSpot_ps.currentWeaponId === WEAPONS.DEFAULT)
			{
				lSpot_ps && lSpot_ps.changeWeapon(lSpot_ps.currentWeaponId, lMult_int);
			}
		}

		this._isWaitingResponseToBetLevelChangeRequest = false;
		this._isPlusBetLevelChangeRequiredAfterFiring_bl = false;
		this._isMinusBetLevelChangeRequiredAfterFiring_bl = false;
	}

	_onBetLevelChangeNotConfirmed()
	{
		if (this.spot)
		{
			this.spot.onBetChangeNotConfirmed();
		}

		this._isWaitingResponseToBetLevelChangeRequest = false;
		this._isPlusBetLevelChangeRequiredAfterFiring_bl = false;
		this._isMinusBetLevelChangeRequiredAfterFiring_bl = false;
	}

	//CUSTOM CURSOR...
	_validateCursor()
	{
		this.emit(GameField.EVENT_TIME_TO_VALIDATE_CURSOR);
	}
	//...CUSTOM CURSOR

	_onBackToLobbyRoundResultButtonClicked()
	{
		this.emit(GameField.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED);
	}

	onBackToLobbyOccur()
	{
		this._fNewPlayerFlag_bln = false;

		this.screen && this.screen.hide();
	}

	_onTargetingControllerPauseStateUpdate()
	{
		this.fireImmediatelyIfRequired();
	}

	//RICOCHET CONTROLLER...
	get _ricochetController()
	{
		return this._fRicochetController_rc || (this._fRicochetController_rc = this._initRicochetController());
	}

	_initRicochetController()
	{
		let l_rc = new RicochetController();
		l_rc.init();
		l_rc.on(RicochetController.EVENT_ON_RICOCHET_BULLETS_UPDATED, this.emit, this);
		l_rc.on(RicochetController.EVENT_ON_RICOCHET_BULLETS_PAYBACK_REQUIRED, this._onRicochetBulletsPaybackRequired, this);

		return l_rc;
	}

	_onRicochetBulletsPaybackRequired(aEvent_obj)
	{
		APP.gameScreen.revertAmmoBack(aEvent_obj.weaponId, aEvent_obj.betLevel, false, false, aEvent_obj.currentBullets);
	}
	//...RICOCHET CONTROLLER

	//BOSS MAP SCALE...
	_onBossBlurMapRequired()
	{
		this._clearScale();

		this._mapScaleSequence = {strength: 0};
		let seq = [
			{tweens: [{prop: "strength", to: 0.4}], duration: 27*FRAME_RATE, ease: Easing.linear.easeIn}
		];
		this._mapScaleSequence.sequence = Sequence.start(this._mapScaleSequence, seq);

		this._fZoomFilter = new PIXI.filters.ZoomBlurFilter(0, new PIXI.Point(960/2, 540/2), 400);
		this.backContainer.filters = [this._fZoomFilter];
		this.bottomScreen.filters = [this._fZoomFilter];
	}

	_onBossScaleMapRequired()
	{
		this._clearScale();
	}

	_clearScale()
	{
		if (this._mapScaleSequence && this._mapScaleSequence.sequence)
		{
			this._mapScaleSequence.sequence.destructor();
		}
		this._mapScaleSequence = null;

		if (this.backContainer)
		{
			this.backContainer.filters = null;
		}
		if (this.bottomScreen)
		{
			this.bottomScreen.filters = null;
		}

		this._fZoomFilter = null;
	}

	_mapScaleTickUpdate()
	{
		if (this._mapScaleSequence && this._mapScaleSequence.sequence)
		{
			if (this._fZoomFilter)
			{
				this._fZoomFilter.strength = this._mapScaleSequence.strength;
			}
		}
	}

	_onBossGroundFireTraceRequired()
	{
		this._destroyGroundFireTraceAnimation();

		this._groundFireTraceAnimation = this.bottomScreen.addChild(new GroundFireTraceAnimation());
		this._groundFireTraceAnimation.zIndex = Z_INDEXES.GROUNDBURN;
		this._groundFireTraceAnimation.position.set(480, 270);
		this._groundFireTraceAnimation.on(GroundFireTraceAnimation.EVENT_ON_ANIMATION_FINISHED, this._destroyGroundFireTraceAnimation, this);
		this._groundFireTraceAnimation.startAnimation();
	}

	_destroyGroundFireTraceAnimation()
	{
		if (this._groundFireTraceAnimation)
		{
			this._groundFireTraceAnimation.off(GroundFireTraceAnimation.EVENT_ON_ANIMATION_FINISHED, this._destroyGroundFireTraceAnimation, this);
			this._groundFireTraceAnimation.destroy();
		}
		this._groundFireTraceAnimation = null;
	}

	_onBossModeFadedBackHiding()
	{
		this._clearScale();
	}
	//...BOSS MAP SCALE

	//BATTLEGROUND FINAL COUNTING...
	get isFinalCountingFireDenied()
	{
		return this._battlegroundFinalCountingController && this._battlegroundFinalCountingController.info.isFinalCountingFireDenied;
	}

	get _battlegroundFinalCountingController()
	{
		return this._fBattlegroundFinalCountingController_fcc || (this._fBattlegroundFinalCountingController_fcc = this._initBattlegroundFinalCountingController());
	}

	_initBattlegroundFinalCountingController()
	{
		let l_fsc = new BattlegroundFinalCountingController(new BattlegroundFinalCountingInfo(), this._battlegroundFinalCountingView);
		return l_fsc;
	}

	get _battlegroundFinalCountingView()
	{
		return this._fBattlegroundFinalCountingView_bfcv || (this._fBattlegroundFinalCountingView_bfcv = this._initBattlegroundFinalCountingView());
	}

	_initBattlegroundFinalCountingView()
	{
		const l_bfcv = this.topScreen.addChild(new BattlegroundFinalCountingView());
		l_bfcv.position.set(960/2, 540/2);
		l_bfcv.zIndex = Z_INDEXES.BTG_FINAL_COUNTING;

		return l_bfcv;
	}
	//...BATTLEGROUND FINAL COUNTING

	//BOSS HP BAR...
	_onBossCreated(aEvent_obj)
	{
		let lEnemy_e = this.getExistEnemy(aEvent_obj.enemyId);

		this._bossHealthBarController.updateBoss(lEnemy_e);

		this._bossHealthBarView.visible = true;

		if (!aEvent_obj.isLasthandBossView)
		{
			this._bossHealthBarView.position.x += 260;
			var lSequence_arr =
			[
				{
					tweens: [{ prop: "position.x", to: this._bossHealthBarView.position.x - 260 }],
					duration: 31 * FRAME_RATE
				}
			]

			Sequence.start(this._bossHealthBarView, lSequence_arr);

		}
	}

	//BOSS HP BAR CONTROLLER...
	get _bossHealthBarController()
	{
		return this._fBossHealthBarController_bmhpbc || (this._fBossHealthBarController_bmhpbc = this._initBossHealthBarController());
	}

	_initBossHealthBarController()
	{
		let l_bmhpbc = new BossModeHPBarController(new BossModeHPBarInfo());
		l_bmhpbc.i_init();
		l_bmhpbc.initView(this._bossHealthBarView);

		return l_bmhpbc;
	}
	//...BOSS HP BAR CONTROLLER

	//BOSS HP BAR VIEW...
	get _bossHealthBarView()
	{
		return this._fBossHealthBarView_bmhpbv || (this._fBossHealthBarView_bmhpbv = this._initBossHealthBarView());
	}

	_initBossHealthBarView()
	{
		let l_bmhpbv = this.middleScreen.addChild(new BossModeHPBarView());
		l_bmhpbv.visible = false;
		l_bmhpbv.zIndex = Z_INDEXES.BOSS_GUI_VIEW;
		l_bmhpbv.position.set(960/2+444, 540/2);

		return l_bmhpbv;
	}
	//...BOSS HP BAR VIEW

	_startRemovingBossHealthBar(aOptIsAnimationRequired = false)
	{
		if (this._fBossHealthBarView_bmhpbv && aOptIsAnimationRequired)
		{
			this._fBossHealthBarView_bmhpbv.once('animationend', this._destroyBossHealthBar, this);
			this._fBossHealthBarView_bmhpbv.onBossDeath();
			return
		}
		else
		{
			this._destroyBossHealthBar();
		}
	}

	_destroyBossHealthBar()
	{
		if (this._fBossHealthBarView_bmhpbv)
		{
			this._fBossHealthBarView_bmhpbv.off('animationend', this._destroyBossHealthBar, this, true);
			this._fBossHealthBarView_bmhpbv.visible = false;
			this.removeChild(this._fBossHealthBarView_bmhpbv);
			this._fBossHealthBarView_bmhpbv = null;
		}
		if (this._fBossHealthBarController_bmhpbc)
		{
			this._fBossHealthBarController_bmhpbc.destroy();
			this._fBossHealthBarController_bmhpbc = null;
		}
	}
	//...BOSS HP BAR

	get _isFrbMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _isBonusMode()
	{
		return APP.currentWindow.gameBonusController.info.isActivated;
	}

	_onPlayerInfoUpdated(event)
	{
		if (event.class == SERVER_MESSAGES.ROUND_RESULT)
		{
			this._fRoundResultResponseReceived_bl = true;
		}
		else if (event.class == SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE)
		{
			if (this._isFrbMode)
			{
				if (!APP.currentWindow.gameFrbController.info.frbEnded)
				{
					this._fExternalBalanceUpdated_bl = false;
				}
			}
		}
		else if ((event.data.balance !== undefined) && (this.roundResultActivationInProgress || this.roundResultActive || APP.currentWindow.gameFrbController.info.frbEnded))
		{
			this._fExternalBalanceUpdated_bl = true;

			if (this._isBonusMode)
			{
				let lBonusInfo_gbi = APP.currentWindow.gameBonusController.info;
				if (lBonusInfo_gbi.isWinLimitExceeded)
				{
					this._forceRoundResult();
				}
			}
		}
		!this._fConnectionClosed_bln && this.updatePlayerBalance();
	}

	_onFRBEndedCompleted()
	{
		this._fExternalBalanceUpdated_bl = false;
	}

	onFRBEnded()
	{
		this.updatePlayerBalance();
	}

	_onTickerResumed()
	{
		this.updatePlayerBalance();
	}

	isGameplayStarted()
	{
		return this.screen !== undefined;
	}

	startGamePlay()
	{
		if (!this.screen)
		{
			this.screen = new GameFieldScreen();

			this.bottomScreen = this.screen.addChild(new Sprite());
			this.middleScreen = this.screen.addChild(new Sprite());
			this.topScreen = this.screen.addChild(new Sprite());

			this.gameplayDialogScreen = this.addChild(new Sprite());

			this._calloutController.init();
			this._calloutController.initView(this.topScreen);

			this.battlegroundTutorialController.init();
			this._fBattlegroundTutorialController_btc.initView(this.gameplayDialogScreen);

			APP.gameScreen.gameplayDialogController.init();
			APP.gameScreen.gameplayDialogController.initView(this.gameplayDialogScreen);
			this.gameplayDialogScreen.zIndex = Z_INDEXES.COUNT_DOWN;

			this._fMissEffectsPool_mep = this.topScreen.addChild(new MissEffectsPool());
			this._fMissEffectsPool_mep.zIndex = Z_INDEXES.MISS_EFFECT;
			this._battlegroundFinalCountingController.init();

			this.emit(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED);
		}

		this.container.addChild(this.screen);
		this.screen.position.set(-480,-270);

		this._fLobbySecondaryScreenActive_bln = false;
		this._checkBlur();

		this.redrawMap();
		this.addRoomGradient();

		this._resetBuyButtonsValidator();
		this._initBuyButtonsValidator();

		this._initSidebarController();
		this._initTreasuresSidebarController();
	}

	_isDialogViewBlurForbidden(aDialogId_int)
	{
		switch(aDialogId_int)
		{
			case DIALOG_ID_INSUFFICIENT_FUNDS:
			case DIALOG_ID_BATTLEGROUND_NOT_ENOUGH_PLAYERS:
			case DIALOG_ID_BATTLEGROUND_BUY_IN_CONFIRMATION:
			case DIALOG_ID_BATTLEGROUND_COUNT_DOWN:
			case DIALOG_ID_BATTLEGROUND_RULES:
			case DIALOG_ID_BATTLEGROUND_CAF_ROOM_MANAGER:
				return true;
		}

		return false;
	}

	_onDialogActivated(e)
	{
		if(this._isDialogViewBlurForbidden(e.dialogId))
		{
			return;
		}

		this.showBlur();
	}

	_onDialogDectivated()
	{
		this._checkBlur();
		this._validateCursor();
	}

	_checkBlur()
	{
		if ( this.roundResultActive ||
			this.lobbySecondaryScreenActive ||
			APP.gameScreen.compensationDialogActive ||
			this._fMidRoundExitDialogActive_bln ||
			this._fBonusDialogActive_bln ||
			this._fFRBDialogActive_bln
		)
		{
			this._showBlur();
		}
		else
		{
			this._hideBlur();
		}
	}

	//BUY BUTTONS VALIDATOR...
	_resetBuyButtonsValidator()
	{
		this._fGameStateController_gsc.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameRoundStateChanged, this);
	}

	_initBuyButtonsValidator()
	{
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameRoundStateChanged, this);
	}

	_onGameRoundStateChanged(event)
	{
		if (event.value === ROUND_STATE.PLAY)
		{
			this._fCurrentKingsOfTheHillSeatId_int_arr = [];
			APP.gameScreen.roundFinishSoon = false;
			this._fIsPlantEnemyOutroAnimationOnRoundResultWasInterrupted_bl = false;
			this._fConnectionHasBeenRecentlyClosed_bln = false;
			this._fConnectionClosed_bln = false;
		}

		this._validateWeaponsBlockAvailability();
		this._tryToBuyAmmoFromRoundResult();
	}

	_validateWeaponsBlockAvailability()
	{
		let lNewState_str = this._fGameStateInfo_gsi.gameState;

		let lAllowWeaponsBlockInteraction_bl = true;//!this.roundResultActive && (lNewState_str === ROUND_STATE.PLAY);

		this._onInteractionChanged(lAllowWeaponsBlockInteraction_bl);
	}

	_onInteractionChanged(aAllow_bln)
	{
		this.emit(GameField.EVENT_ON_WEAPONS_INTERACTION_CHANGED, {allowed: aAllow_bln})
	}
	//...BUY BUTTONS VALIDATOR

	_initSidebarController()
	{
		if (APP.isBattlegroundGamePlayMode)
		{
			this.weaponsSidebarController.i_hideView();
			this.scoreboardController; // for init
		}
		else
		{
			this.weaponsSidebarController.i_showView();
		}
	}

	//BATTLEGROUND TUTORIAL...
	get battlegroundTutorialController()
	{
		return this._fBattlegroundTutorialController_btc || (this._fBattlegroundTutorialController_btc = this._initBattlegroundTutorialController());
	}

	_initBattlegroundTutorialController()
	{
		let l_btc = new BattlegroundTutorialController();
		l_btc.on(BattlegroundTutorialController.VIEW_APPEARING, this._onTutorialAppearing, this);
		l_btc.on(BattlegroundTutorialController.VIEW_HIDDEN, this._onTutorialHidden, this);
		return l_btc;
	}
	//...BATTLEGROUND TUTORIAL

	_showBlur()
	{
		let blurFilter = new PIXI.filters.BlurFilter();
		blurFilter.blur = 2;

		this.container.filters = [blurFilter];
	}

	_hideBlur()
	{
		this.container.filters = null;
	}

	_validateInteractivity()
	{
		this.interactive = !this._fFireSettingsScreenActive_bln;
		this._validateCursor();
	}

	deactivateGameScreens()
	{
		this._fireSettingsController.hideScreen();
		this._validateInteractivity();
	}
	//...BUY WEAPON SCREEN CONTROLLER

	//TREASURES SIDEBAR...
	get treasuresSidebarController()
	{
		return this._fTreasuresSidebarController_tsc || this._initTreasuresSidebarController();
	}

	_initTreasuresSidebarController()
	{
		if (this._fTreasuresSidebarController_tsc)
		{
			return this._fTreasuresSidebarController_tsc;
		}

		const l_tsv = this.topScreen.addChild(APP.isBattlegroundGame ? new TreasuresSidebarBTGView(): new TreasuresSidebarView());
		l_tsv.position.set(APP.isMobile ? 940 : 930, 295);
		l_tsv.visible = false;
		l_tsv.zIndex = Z_INDEXES.SIDEBAR;

		const l_tsc = this._fTreasuresSidebarController_tsc = new TreasuresSidebarController(l_tsv);
		l_tsc.i_init();
		this._fTreasuresSidebarController_tsc.on(TreasuresSidebarController.EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH_AFTER_ROUND_END, this._onGemGettedAfterRoundEnd, this);

		return this._fTreasuresSidebarController_tsc = l_tsc;
	}

	_onGemGettedAfterRoundEnd()
	{
		this._fGameStateController_gsc.updateRoundProgress();
		this.updatePlayerBalance();
		this.clearRoom(true);
	}
	//...TREASURES SIDEBAR

	//CALLOUT...
	get _calloutController()
	{
		return this._fCalloutController_fcc || (this._fCalloutController_fcc = this._initCalloutsController());
	}

	_initCalloutsController()
	{
		let l_cc = new CalloutsController();

		return l_cc;
	}
	//...CALLOUT

	//FIRE SETTINGS VIEW...
	get _fireSettingsView()
	{
		return this._fFireSettingsView_fsv || (this._fFireSettingsView_fsv = this._initFireSettingsView());
	}

	_initFireSettingsView()
	{
		let l_fsv = this.addChild(new FireSettingsView());
		l_fsv.zIndex = Z_INDEXES.FIRE_SETTINGS;

		return l_fsv;
	}
	//...FIRE SETTINGS VIEW

	//ROUND RESULT SCREEN CONTROLLER...
	get _roundResultScreenController()
	{
		return this._fRoundResultScreenController_bwsv || (this._fRoundResultScreenController_bwsv = this._initRoundResultScreenController());
	}

	_initRoundResultScreenController()
	{
		let l_rrsc = new RoundResultScreenController(new RoundResultScreenInfo(), this._roundResultScreenView, this);

		l_rrsc.on(RoundResultScreenController.EVENT_ON_NEXT_ROUND_CLICKED , this._onRoundResultNextRoundClicked, this);
		l_rrsc.on(RoundResultScreenController.EVENT_ON_BATTLEGROUND_NEXT_ROUND_CLICKED, this.emit, this);
		l_rrsc.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATION_START, this.emit, this);

		return l_rrsc;
	}

	_onRoundResultNextRoundClicked()
	{
		this.tryRoundResultBuyAmmoRequestAllowing();

		this._tryToBuyAmmoFromRoundResult(true);
	}

	tryRoundResultBuyAmmoRequestAllowing()
	{
		this._fRoundResultBuyAmmoRequest_bln = this._isFrbMode ? false : true;
	}

	_forceUsingSpecialWeaponInNextRound()
	{
		if (this._fGameStateInfo_gsi.gameState !== "PLAY") return;

		if(this.getNextWeaponFromTheQueue() != WEAPONS.DEFAULT && !IS_SPECIAL_WEAPON_SHOT_PAID)
		{
			this._fRoundResultRestoreWeapon_num = this.selectNextWeaponFromTheQueue();
		}
	}

	_tryToBuyAmmoFromRoundResult(aIgnoreRoundResultState_bln = false)
	{
		let lRoundResultActive_bln = (this.roundResultActivationInProgress || this.roundResultActive) && !aIgnoreRoundResultState_bln;
		if (this._fWeaponsInfo_wsi.ammo == 0 && this._fRoundResultBuyAmmoRequest_bln && !lRoundResultActive_bln)
		{
			//https://jira.dgphoenix.com/browse/MQPRT-347...
			if (APP.gameSettingsController.info.isBuyInNotMandatoryInRound)
			{
				let lIsAnySWExists_bl = this._fWeaponsController_wsc.i_getInfo().isAnyFreeSpecialWeaponExist;
				let lBalance_num = this.calcBalanceValue();
				let lCurrentStake_num = APP.playerController.info.currentStake;
				if (lBalance_num < lCurrentStake_num && lIsAnySWExists_bl && !IS_SPECIAL_WEAPON_SHOT_PAID)
				{
					let lIsDefaultWeaponSelected_bln = this._fWeaponsController_wsc.i_getInfo().currentWeaponId === WEAPONS.DEFAULT;
					if (lIsDefaultWeaponSelected_bln)
					{
						this._forceUsingSpecialWeaponInNextRound();
					}
					return;
				}
			}
			//...https://jira.dgphoenix.com/browse/MQPRT-347
			this._tryToBuyAmmo();
		}
	}

	//https://jira.dgphoenix.com/browse/MQPRT-343...
	_tryToBuyAmmoOnRoundStart()
	{
		let lRoundResultActive_bln = this.roundResultActivationInProgress || this.roundResultActive;
		if (
				this._fWeaponsInfo_wsi.ammo == 0
				&& (
						!lRoundResultActive_bln
						|| this._isBonusMode
						|| this._fTournamentModeInfo_tmi.isTournamentMode
					)
			)
		{
			let lPlayerInfo_pi = APP.playerController.info;
			let lBalance_num = lPlayerInfo_pi.balance;
			let lCurrentStake_num = APP.playerController.info.currentStake;
			if (lBalance_num >= lCurrentStake_num)
			{
				if (this._isBonusMode || this._fTournamentModeInfo_tmi.isTournamentMode)
				{
					//convert balance into ammo for Cash Bonus/tournaments mode
					this.emit(GameField.EVENT_ON_TIME_TO_CONVERT_BALANCE_TO_AMMO, {ammoAmount: ~~(lBalance_num/lCurrentStake_num)});
					this.updatePlayerBalance();

				}
				else
				{
					this._tryToBuyAmmo();
				}
			}
		}
	}
	//...https://jira.dgphoenix.com/browse/MQPRT-343

	_tryToBuyAmmo()
	{
		if (
				APP.currentWindow.isPaused || this._isFrbMode || this._isBonusMode || this._fTournamentModeInfo_tmi.isTournamentMode
				|| APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress
				|| APP.gameScreen.buyAmmoRetryingController.info.isRetryDialogActive
				|| APP.gameScreen.isForcedSitOutInProgress
			)
		{
			return;
		}

		if (this._fGameStateInfo_gsi.gameState == "PLAY"
			&& 
			(
				!APP.isBattlegroundGame	||
				APP.isBattlegroundGame && APP.gameScreen.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
			)
		)
		{
			this._fRoundResultBuyAmmoRequest_bln = false;
			this.emit(GameField.EVENT_ON_BUY_AMMO_REQUIRED);
		}
	}

	_tryToResumeAutofire()
	{
		//for mobile BTG: target is not reset by AutoTargetingSwitcher, shooting resumes
		if(APP.isBattlegroundGame && this._fAutofireButtonEnabled_bl) return;

		if(APP.isBattlegroundGame && !this._fAutoTargetingSwitcherInfo_atsi.isOn)
		{
			this._validateAutofireButton(true);
		}
		else
		{
			this._validateAutofireButton(false, null, true);
		}
	}

	_onRoundResultScreenActivated(e)
	{
		this._fRoundResultActive_bln = true;
		this._fCurrentKingsOfTheHillSeatId_int_arr = [];

		if (APP.currentWindow.isKeepSWModeActive)
		{
			for (let i = 0; i < this._fMinesController_msc.masterMinesOnFieldLen; ++i)
			{
				APP.gameScreen.revertAmmoBack(WEAPONS.MINELAUNCHER);
			}
		}

		this.deactivateGameScreens();

		this.emit(GameField.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED);
		this._validateCursor();

		if (!APP.currentWindow.gameStateController.info.isGameInProgress)
		{
			this.clearRoom(true);
		}

		if (!this._isFrbMode)
		{
			this.resetPlayerWin();
		}

		const l_wci = this._fWeaponsController_wsc.i_getInfo();
		if (APP.isBattlegroundGame)
		{
			this._fRestoreAfterFreeShotsWeaponId_int = undefined;
			if (this.spot)
			{
				this.spot.onBetChangeConfirmed(APP.playerController.info.betLevel, false);
			}


			this.changeWeapon(WEAPONS.DEFAULT);
			this._resetCoPlayersWeaponsIfRequired(true);
		}
		else
		{
			if (!APP.currentWindow.isKeepSWModeActive && l_wci.isFreeWeaponsQueueActivated)
			{
				this.selectNextWeaponFromTheQueue();
			}
		}

		this._validateUIBlur();
		this._showBlur();

		this.updatePlayerBalance();

		if (
				APP.currentWindow.isKeepSWModeActive
				&& !APP.currentWindow.isPaused  // don't remember currentWeaponId to restore if the game is paused because of https://jira.dgphoenix.com/browse/MQAMZN-119 and because for such cases this._fWeaponToRestore_int is used
			)
		{
			if (this._fWeaponsController_wsc && this._fRoundResultRestoreWeapon_num === null)
			{
				this._fRoundResultRestoreWeapon_num = this._fWeaponsController_wsc.i_getInfo().currentWeaponId;
			}
		}

		if (this._fGameStateWaitIsWatingBattlegroundRoundResult_bl)
		{
			this._fGameStateWaitIsWatingBattlegroundRoundResult_bl = false;
			this._completeActionsOnRoundStateWait();
		}

		if (APP.gameScreen.room)
		{
			APP.gameScreen.tryToProceedPostponedSitOut();
		}

		if (e.roundResultOnAwaitingGameUnpause)
		{
			this.emit(GameField.EVENT_FULL_GAME_INFO_REQUIRED);
		}

		APP.gameScreen.onRoundResultActivated(e);
	}

	_onRoundResultDeactivatedOrSkipped()
	{
		this._fRoundResultActive_bln = false;
		this._fRoundResultResponseReceived_bl = false;
		this._fExternalBalanceUpdated_bl = false;
		this._fServerMessageWeaponSwitchExpected_bln = false;
		this._fRoundResultOnLasthandWaitState_bln = false;

		if (!APP.currentWindow.isBackToLobbyRequired)
		{
			this.changeState(this._fGameStateInfo_gsi.gameState);

			if (!this._roundResultScreenController.info.isActiveScreenMode)
			{
				this.tryRoundResultBuyAmmoRequestAllowing();
			}
			this._tryToBuyAmmoFromRoundResult();
		}

		this.emit(GameField.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED);
		this._validateCursor();
		this._validateWeaponsBlockAvailability();
		this._validateUIBlur();

		this._checkBlur();
	}

	get roundResultActivationInProgress()
	{
		return this._roundResultScreenController.info.roundResultResponseRecieved || this._fRoundResultResponseReceived_bl;
	}

	get roundResultActive()
	{
		return this._fRoundResultActive_bln;
	}
	//...ROUND RESULT SCREEN CONTROLLER

	//ROUND RESULT SCREEN VIEW...
	get _roundResultScreenView()
	{
		return this._fRoundResultScreenView_bwsv || (this._fRoundResultScreenView_bwsv = this._initRoundResultScreenView());
	}

	_initRoundResultScreenView()
	{
		let l_rrsv = undefined;

		if(APP.isBattlegroundGame)
		{
			l_rrsv = new BattlegroundResultScreenView();
		}
		else
		{
			l_rrsv = new RoundResultScreenView();
		}

		this.addChild(l_rrsv);
		l_rrsv.zIndex = Z_INDEXES.ROUND_RESULT;

		return l_rrsv;
	}
	//...ROUND RESULT SCREEN VIEW

	//SCOREBOARD...
	get scoreboardController()
	{
		return this._fScoreboardController_sc || (this._fScoreboardController_sc = this._initScoreboardController());
	}

	_initScoreboardController()
	{
		let l_sc = new ScoreboardController();
		l_sc.i_init();
		l_sc.on(ScoreboardController.EVENT_ON_SCORE_BOARD_SCORES_UPDATED, this._onScoreboardScoresUpdated, this);

		let l_sv = l_sc.view;
		this._fScoreboardView_sv = this.topScreen.addChild(l_sv);
		l_sv.position.set(0, 80);
		l_sv.zIndex = Z_INDEXES.SIDEBAR;
		return l_sc;
	}

	_onScoreboardScoresUpdated(event)
	{
		if (event.seatId == APP.playerController.info.seatId)
		{
			this.updateCommonPanelIndicators({win: event.win}, null);
		}
	}

	get isScoreboardAnimationsPlaying()
	{
		return (this._fScoreboardController_sc && this._fScoreboardController_sc.view.isAnimationsPlaying) || false;
	}
	//...SCOREBOARD

	//WEAPONS SIDEBAR...
	get weaponsSidebarController()
	{
		return this._fWeaponsSidebarController_wssc || (this._fWeaponsSidebarController_wssc = this._initWeaponsSidebarController());
	}

	_initWeaponsSidebarController()
	{
		let l_wssv = this.topScreen.addChild(new WeaponsSidebarView());

		if(APP.isMobile)
		{
			l_wssv.scale.set(0.83);
			let lmobileY_num = l_wssv.position.y * 1.085;
			l_wssv.position.y = lmobileY_num;
		}

		l_wssv.zIndex = Z_INDEXES.SIDEBAR;

		let l_wssc = new WeaponsSidebarController(l_wssv);
		l_wssc.i_init();

		return l_wssc;
	}
	//...WEAPONS SIDEBAR

	_onCloseRoom()
	{
		this._fRoundResultRestoreWeapon_num = null;
		this._fWeaponToRestore_int = null;
		this._fRestoreAfterFreeShotsWeaponId_int = undefined;
	}

	showGroundBurn(x, y, aScale_num = 1, aInitialAlpha_num = 1, aIsArtilerryHit_bl = false)
	{
		var groundburn = this.bottomScreen.addChild(new GrenadeGroundBurn(aInitialAlpha_num, aIsArtilerryHit_bl));
		groundburn.scale.set(aScale_num);
		groundburn.once(GrenadeGroundBurn.EVENT_ON_ANIMATION_COMPLETED, this._onGroundBurnAnimationCompleted, this);
		groundburn.position.set(x, y);
		groundburn.zIndex = Z_INDEXES.GROUNDBURN;

		this._fFxAnims_arr.push(groundburn);
	}

	_onGroundBurnAnimationCompleted(event)
	{
		let groundburn = event.target;
		groundburn.off(GrenadeGroundBurn.EVENT_ON_ANIMATION_COMPLETED, this._onGroundBurnAnimationCompleted, this, true);

		let lIndex_int = this._fFxAnims_arr.indexOf(groundburn);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
		}

		groundburn.destroy();
	}

	_onGrenadeExplosionAnimationCompleted(event)
	{
		let lIndex_int = this._fFxAnims_arr.indexOf(event.target);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
		}
	}

	reboundAllEnemies(epicentrumPoint)
	{
		for (let enemy of this.enemies)
		{
			let enemyPos = enemy.getGlobalPosition();
			let enemyFeetPoint = enemy.getCurrentFootPointPosition();
			let pos = new PIXI.Point(0, 0);
			pos.x = enemyPos.x + enemyFeetPoint.x;
			pos.y = enemyPos.y + enemyFeetPoint.y;
			let angle = Utils.getAngle(pos, epicentrumPoint) - Math.PI/2;
			let dist = Utils.getDistance(pos, epicentrumPoint);
			enemy.showBombBounce(angle, dist, APP.currentWindow.mapsController.info.currentMapWalkingZone);
		}
	}

	showBombReboundSmoke(x, y)
	{
		let groundSmoke = this.bottomScreen.addChild(new Sprite);
		groundSmoke.position.set(x, y);
		groundSmoke.zIndex = y;

		Grenade.getTextures();
		groundSmoke.textures = Grenade.textures['groundSmoke'];
		groundSmoke.rotation = Utils.gradToRad(88);
		groundSmoke.scale.set(2*0.67);
		groundSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		groundSmoke.play();
		groundSmoke.once('animationend', (e) => {
			this._onGroundSmokeAnimationCompleted.bind(this, groundSmoke);
			e.target.destroy();
		});
		this._fFxAnims_arr.push(groundSmoke);
	}

	_onGroundSmokeAnimationCompleted(target)
	{
		var lIndex_int = this._fFxAnims_arr.indexOf(target);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
		}
	}

	getGunPosition(seatId)
	{
		let seat = this.getSeat(seatId, true);
		let gunPos = {x: 0, y: 0};
		let playerPos = this._getSpotPosition(SEATS_POSITION_IDS[seatId], (this.seatId === seatId));

		gunPos.x = playerPos.x + seat.gunCenter.x;
		gunPos.y = playerPos.y + seat.gunCenter.y;

		return gunPos;
	}

	_getShakeAmplitudeSequence(amplitudes_arr)
	{
		let sequence = [];
		for (let i = 0; i < amplitudes_arr.length; i++)
		{
			let curAmpl = amplitudes_arr[i];
			let obj = {
				tweens: [
					{prop: 'x', to: Utils.getRandomWiggledValue(0, curAmpl)},
					{prop: 'y', to: Utils.getRandomWiggledValue(0, curAmpl)}
				],
				duration: 1*FRAME_RATE
			};
			sequence.push(obj);
		}

		return sequence;
	}

	shakeTheGround(aShakingType_str = "", aOptResetExistingShaking_bl = false)
	{
		let sequence;
		let container = this.container;

		if (aOptResetExistingShaking_bl)
		{
			Sequence.destroy(Sequence.findByTarget(container));
		}

		switch (aShakingType_str)
		{
			case "quest_panel_puff":
				sequence = this._getShakeAmplitudeSequence([2, 5, 7, 5, 2, 0]);
				break;
			case "plasma":
				sequence = [];
				for (let i = 0; i < 10; i++)
				{
					sequence.push({tweens:[{prop:'x', to:(Utils.random(-35, 35)/10)}, {prop:'y', to:(Utils.random(-35, 35)/10)}], duration: FRAME_RATE});
				}
				break;

			case "bomb":
				sequence = [];
				for (let i = 0; i < 7; i++)
				{
					let obj = {
						tweens: [
							{prop: 'x', to: 12 - Utils.random(0, 24)},
							{prop: 'y', to: Utils.random(0, 20)}
						],
						duration: 40 + Utils.random(0, 40),
						ease: Easing.sine.easeOut
					};
					sequence.push(obj);
				}
				sequence.push({tweens: [{prop: "x", to: 0}, {prop: "y", to: 0}],
								duration: Utils.random(0, 30),
								ease: Easing.sine.easeOut,
								onfinish: () => {
									//dust from the ceiling
									if (aShakingType_str === "bomb")
									{
										this.pourDustFromTheCeiling();
									}
								}});
				break;

			case "artillery":
				sequence = [];
				for (let i = 0; i < 10; i++)
				{
					let obj = {
						tweens: [
							{prop: 'x', to: 12 - Utils.random(0, 24)},
							{prop: 'y', to: Utils.random(0, 20)}
						],
						duration: 40 + Utils.random(0, 40),
						ease: Easing.sine.easeOut
					};
					sequence.push(obj);
				}
				sequence.push(
								{	tweens: [{prop: "x", to: 0}, {prop: "y", to: 0}],
									duration: Utils.random(0, 30),
									ease: Easing.sine.easeOut,
									onfinish: () => {
										this.pourDustFromTheCeiling();
									}
								}
				);
				break;
			case "bossAppearing":
				sequence = [];
				for (let i = 0; i < 18; i++)
				{
					sequence.push({tweens:[{prop:'x', to:(Utils.random(-35, 35)/10)}, {prop:'y', to:(Utils.random(-35, 35)/10)}], duration: 2 * FRAME_RATE, ease:Easing.sine.easeInOut});
				}
				sequence.push(
						{
							tweens:
							[
								{ prop: 'scale.x', to: 1.05 },
								{ prop: 'scale.y', to: 1.05 }
							],
							duration: 3 * FRAME_RATE,
							ease: Easing.sine.easeIn
						},
						{
							tweens:
							[
								{ prop: 'scale.x', to: 1 },
								{ prop: 'scale.y', to: 1}
							],
							duration: 3 * FRAME_RATE,
							ease: Easing.sine.easeOut
						}
					);
					for (let i = 0; i < 25; i++)
					{
						sequence.push({tweens:[{prop:'x', to:(Utils.random(-35, 35)/10)}, {prop:'y', to:(Utils.random(-35, 35)/10)}], duration: 2 * FRAME_RATE, ease:Easing.sine.easeInOut});
					}
				break;
			case "bossShining":
				sequence = [];
				for (let i = 0; i < 50; i++)
				{
					sequence.push({tweens:[{prop:'x', to:(Utils.random(-35, 35)/10)}, {prop:'y', to:(Utils.random(-35, 35)/10)}], duration:100, ease:Easing.sine.easeOut});
				}
				break;

			case "bossExplosion":
				sequence = [];
				for (let i = 0; i < 3; i++)
				{
					sequence.push({tweens:[{prop:'x', to:Utils.random(-12, 12)}, {prop:'y', to:Utils.random(-7, 15)}], duration:50, ease:Easing.sine.easeOut});
				}
				for (let i = 0; i < 3; i++)
				{
					sequence.push({tweens:[{prop:'x', to:Utils.random(-10, 10)}, {prop:'y', to:Utils.random(-7, 10)}], duration:50, ease:Easing.sine.easeOut});
				}
				for (let i = 0; i < 3; i++)
				{
					sequence.push({tweens:[{prop:'x', to:Utils.random(-5, 5)}, {prop:'y', to:Utils.random(-5, 5)}], duration:50, ease:Easing.sine.easeOut});
				}
				sequence.push({tweens:[{prop:"x", to:0}, {prop:"y", to:0}], duration:50, ease:Easing.sine.easeOut});
				break;

			case "bombEnemy":
				sequence = [];

				sequence.push({tweens:[
					{prop:"scale.x", to: 1.02},
					{prop:"scale.y", to: 1.02},
					{prop:"position.x", to: 7},
					{prop:"position.y", to: -7}
				], duration: 50});
				sequence.push({tweens:[], duration: 50});
				sequence.push({tweens:[
					{prop:"scale.x", to: 1},
					{prop:"scale.y", to: 1},
					{prop:"position.x", to: 0},
					{prop:"position.y", to: 0}
				], duration: 50});
				break;

			case "bigWinEnd":
				sequence = [];
				for (let i = 0; i < 5; i++)
				{
					sequence.push({tweens:[{prop:'x', to:(Utils.random(-5, 5))}, {prop:'y', to:(Utils.random(-5, 5))}], duration:30, ease:Easing.sine.easeOut});
				}
				sequence.push({tweens:[{prop:"x", to:0}, {prop:"y", to:0}], duration:50, ease:Easing.sine.easeOut});
				break;

			default:
				sequence = [
					{tweens: [{prop: "y", to: 5}], duration: 30, ease: Easing.sine.easeOut},
					{tweens: [{prop: "y", to: 3}], duration: 20, ease: Easing.sine.easeOut},
					{tweens: [{prop: "y", to: 7}], duration: 50, ease: Easing.sine.easeOut},
					{tweens: [{prop: "y", to: 0}], duration: 25, ease: Easing.sine.easeOut},
				];

				if (this.playersContainer)
				{
					let oy = 0; //initial playersContainer position
					let playersContainerRevSeq = [
						{tweens: [{prop: "y", to: oy - 5}], duration: 30, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy - 3}], duration: 20, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy - 7}], duration: 50, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy}], duration: 25, ease: Easing.sine.easeOut},
					];
					Sequence.start(this.playersContainer, playersContainerRevSeq);
				}

				if (this.spot)
				{
					let oy = this.spot && this.spot.initialPosition.y;
					let spotContainerRevSeq = [
						{tweens: [{prop: "y", to: oy - 5}], duration: 30, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy - 3}], duration: 20, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy - 7}], duration: 50, ease: Easing.sine.easeOut},
						{tweens: [{prop: "y", to: oy}], duration: 25, ease: Easing.sine.easeOut},
					];
					Sequence.start(this.spot, spotContainerRevSeq);
				}
				break;
		}

		if (sequence && sequence.length && container)
		{
			Sequence.start(container, sequence);
		}
	}

	pourDustFromTheCeiling(aWithDebris_bl = true)
	{

		if (this._fGameOptimizationInfo_goi.i_isCeilingDustMaximumExceed())
		{
			return;
		}

		this.emit(GameField.EVENT_ON_CEILING_DUST_ANIMATION_STARTED);

		let ceilingDust = this.container.addChild(new CeilingDust(aWithDebris_bl));
		ceilingDust.once("animationFinish", this._onCeilingDustAnimationCompleted, this);
		ceilingDust.zIndex = Z_INDEXES.CEILING_DUST;
		this._fFxAnims_arr.push(ceilingDust);
	}

	_onCeilingDustAnimationCompleted(event)
	{
		this.emit(GameField.EVENT_ON_CEILING_DUST_ANIMATION_COMPLETED);
		let lIndex_int = this._fFxAnims_arr.indexOf(event.target);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
		}
	}

	_tryToProceedNewPlayer()
	{
		if (APP.playerController.info.isNewbie)
		{
			this._fNewPlayerFlag_bln = true;
		}
	}

	changeState(state)
	{
		if (typeof state != "string")
		{
			return;
		}

		switch (state)
		{
			case ROUND_STATE.PLAY:

				if (this._fGameStateWaitIsWatingBattlegroundRoundResult_bl)
				{
					this._fGameStateWaitIsWatingBattlegroundRoundResult_bl = false;
				}

				this._validateWeaponsBlockAvailability();
				this.redrawMap();
				this.addRoomGradient();

				this.emit(GameField.EVENT_ON_NEW_ROUND_STATE, {state: true});
				this.focusUI();
				this.showUI();
				this.removeTimeLeftText();

				if (this.spot) //I'm player, not observer
				{
					if (this._fNewPlayerFlag_bln) //Leave new player with default gun
					{
						this.tryToChangeWeapon(WEAPONS.DEFAULT);
					}
					else
					{
						if (this._fRoundResultRestoreWeapon_num != null || this._fWeaponToRestore_int != null)
						{
							let lWeaponToRestore_num = this._fRoundResultRestoreWeapon_num;
							if (this._fWeaponToRestore_int !== null) lWeaponToRestore_num = this._fWeaponToRestore_int;
							this._selectRememberedWeapon(lWeaponToRestore_num);
							this._fRoundResultRestoreWeapon_num = null;
							this._fWeaponToRestore_int = null;
						}
					}

					this.redrawAmmoText();
					this._tryToBuyAmmoOnRoundStart();
				}

				let awardingController = APP.currentWindow.awardingController;
				awardingController.removeAllAwardings();

				if(!APP.isBattlegroundGame) this._onTimeToShowTutorial();
				break;

			case ROUND_STATE.WAIT:
				if (!APP.currentWindow.isKeepSWModeActive)
				{
					this._fWeaponToRestore_int = null;
				}

				if (this._fGameStateWaitIsWatingBattlegroundRoundResult_bl)
				{
					break;
				}

				this._completeActionsOnRoundStateWait();
				break;

			case ROUND_STATE.QUALIFY:
				if (APP.isBattlegroundGamePlayMode)
				{
					this._fGameStateWaitIsWatingBattlegroundRoundResult_bl = true;
				}
				this._startRemovingBossHealthBar(true);

				if (!APP.currentWindow.isKeepSWModeActive)
				{
					this._fWeaponToRestore_int = null;
				}
				this._fRoundResultOnLasthandWaitState_bln = false;
				this._startListenForRoundEnd();
				if (this.seatId != -1)
				{
					this.removeWaitScreen();
				}
				break;
		}

		if(!this.isBossEnemyExist)
		{
			this._startRemovingBossHealthBar(true);
		}
		this._validateCursor();
	}

	_completeActionsOnRoundStateWait()
	{
		this._checkForIncompleteRoundAnimations();
		this._stopListenForRoundEnd();
		if (!this.roundResultActive && this.seatId != -1)
		{
			this.addTimeLeftText();
		}

		if (this.playersContainer)
		{
			for (let seat of this.playersContainer.children)
			{
				seat.currentScore = 0;
			}
		}
		this.redrawMap();
		this.updatePlayerBalance();
		this.emit(GameField.EVENT_ON_WAITING_NEW_ROUND);

		this._resetCoPlayersWeaponsIfRequired();
	}

	_resetCoPlayersWeaponsIfRequired(aIsIgnoreWaitState_bl = false)
	{
		let lPlayerInfo_pi = APP.playerController.info;
		let lGameStateInfo_gsi = APP.currentWindow.gameStateController.info;

		if (
				APP.isBattlegroundGame
				&& (lGameStateInfo_gsi.isWaitState || aIsIgnoreWaitState_bl)
				&& this.playersContainer
			)
		{
			// reset for the case if we still havn't got WeaponSwitched for the player in QUALIFY state (ex., if his socket was aborted)
			for (let seat of this.playersContainer.children)
			{
				let player = this.getPlayerBySeatId(seat.id);

				if (player.betLevel !== lPlayerInfo_pi.roomDefaultBetLevel)
				{
					player.betLevel = lPlayerInfo_pi.roomDefaultBetLevel;

					let lSpotCurrentDefaultWeaponId_int = lPlayerInfo_pi.getTurretSkinId(player.betLevel);
					seat.changeWeapon(WEAPONS.DEFAULT, lSpotCurrentDefaultWeaponId_int, true);
				}
			}
		}
	}

	_onTutorialAppearing()
	{
		this.hideUI(true);
	}

	_onTutorialHidden()
	{
		this.showUI();
	}

	_onTimeToShowTutorial(aEvent_obj, aOptAutoHide_bl=false)
	{
		if (this.spot)
		{
			if (!APP.isBattlegroundGame)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.TIME_TO_SHOW_TUTORIAL, {
					positionId: 		SEATS_POSITION_IDS[this.seatId],
					mainSpot: 			this.spot.getBounds(),
					isSpotAtBottom:		this.spot.isBottom,
					SWPanel: 			APP.isBattlegroundGamePlayMode ? this.scoreboardController.view.getBounds() : this.weaponsSidebarController.view.getBounds(),
					treasuresSidebarPanel: 	this.treasuresSidebarController.view.getBounds(),
					autoTargetSwitcher:	APP.gameScreen.autoTargetingSwitcherController.view.getBounds()
				});
			}
			else
			{
				this.emit(GameField.EVENT_TIME_TO_SHOW_TUTORIAL, {
					positionId: 		SEATS_POSITION_IDS[this.seatId],
					mainSpot: 			this.spot.getBounds(),
					isSpotAtBottom:		this.spot.isBottom,
					SWPanel: 			APP.isBattlegroundGamePlayMode ? this.scoreboardController.view.getBounds() : this.weaponsSidebarController.view.getBounds(),
					autoTargetSwitcher:	APP.gameScreen.autoTargetingSwitcherController.view.getBounds()
				});			
			}
		}
	}

	_startListenForRoundEnd()
	{
		this._stopListenForRoundEnd();

		this._fRoundResultAnimsCount_num = 0;

		for (let enemy of this.enemies)
		{
			++this._fRoundResultAnimsCount_num;

			let eventType = enemy.isBoss ? Enemy.EVENT_ON_DEATH_ANIMATION_COMPLETED : Enemy.EVENT_ON_ENEMY_DESTROY;
			enemy.once(eventType, this._onRREnemyDeathCompleted, this);

			this._fRoundEndListenHandlers_arr.push({
				obj: enemy,
				event: eventType,
				handler: this._onRREnemyDeathCompleted
			});
		}

		for (let enemy of this.deadEnemies)
		{
			++this._fRoundResultAnimsCount_num;
			enemy.once(Enemy.EVENT_ON_ENEMY_DESTROY, this._onRREnemyDestroyed, this);

			this._fRoundEndListenHandlers_arr.push({
				obj: enemy,
				event: Enemy.EVENT_ON_ENEMY_DESTROY,
				handler: this._onRREnemyDestroyed
			});
		}

		let awardingController = APP.currentWindow.awardingController;
		if (awardingController && awardingController.isAnyAwardingInProgress)
		{
			++this._fRoundResultAnimsCount_num;
			awardingController.once(AwardingController.EVENT_ON_ALL_ANIMATIONS_COMPLETED, this._onAwardsAnimationsCompleted, this);

			this._fRoundEndListenHandlers_arr.push({
				obj: awardingController,
				event: AwardingController.EVENT_ON_ALL_ANIMATIONS_COMPLETED,
				handler: this._onAwardsAnimationsCompleted
			});
		}

		let bigWinsController = APP.currentWindow.bigWinsController;
		if (bigWinsController && bigWinsController.isAnyBigWinInProgress)
		{
			++this._fRoundResultAnimsCount_num;
			bigWinsController.once(BigWinsController.EVENT_ON_ALL_ANIMATIONS_COMPLETED, this._onBigWinsAnimationsCompleted, this);
			this._fRoundEndListenHandlers_arr.push({
				obj: bigWinsController,
				event: BigWinsController.EVENT_ON_ALL_ANIMATIONS_COMPLETED,
				handler: this._onBigWinsAnimationsCompleted
			});

			bigWinsController.once(BigWinsController.EVENT_ON_ANIMATION_INTERRUPTED, this._onBigWinsAnimationsInterrupted, this);
			this._fRoundEndListenHandlers_arr.push({
				obj: bigWinsController,
				event: BigWinsController.EVENT_ON_ANIMATION_INTERRUPTED,
				handler: this._onBigWinsAnimationsInterrupted,
			});
		}

		let questsController = APP.currentWindow.questsController;
		if (questsController && questsController.isAnyCompletedQuestAnimationInProgress)
		{
			++this._fRoundResultAnimsCount_num;
			questsController.once(QuestsController.EVENT_ON_ALL_COMPLETED_QUESTS_ANIMATIONS_COMPLETED, this._onCompletedQuestsAnimationsCompleted, this);

			this._fRoundEndListenHandlers_arr.push({
				obj: questsController,
				event: QuestsController.EVENT_ON_ALL_COMPLETED_QUESTS_ANIMATIONS_COMPLETED,
				handler: this._onCompletedQuestsAnimationsCompleted
			});
		}

		if (this._fIsWeaponAddingInProgress_bl)
		{
			++this._fRoundResultAnimsCount_num;
			this.once(GameField.EVENT_ON_END_SHOW_WEAPON, this._onEndShowWeapon, this);

			this._fRoundEndListenHandlers_arr.push({
				obj: this,
				event: GameField.EVENT_ON_END_SHOW_WEAPON,
				handler: this._onEndShowWeapon
			});
		}

		if (this.gunLocked)
		{
			++this._fRoundResultAnimsCount_num;
			this.once(GameField.EVENT_ON_GUN_UNLOCKED, this._onGunUnlockedBeforeRoundEnd, this);
			this._fRoundEndListenHandlers_arr.push({
				obj: this,
				event: GameField.EVENT_ON_GUN_UNLOCKED,
				handler: this._onGunUnlockedBeforeRoundEnd
			});
		}

		if (this._fArtilleryStrikesInfo_assi.activeArtilleryStrikesCounter > 0)
		{
			++this._fRoundResultAnimsCount_num;
			this._fArtilleryStrikesController_assc.once(ArtilleryStrikesController.EVENT_ON_ALL_ARTILLERY_STRIKES_COMPLETED, this._onAllArtilleryStrikesCompleted, this);
			this._fRoundEndListenHandlers_arr.push({
				obj: this._fArtilleryStrikesController_assc,
				event: ArtilleryStrikesController.EVENT_ON_ALL_ARTILLERY_STRIKES_COMPLETED,
				handler: this._onAllArtilleryStrikesCompleted
			});
		}

		if (this._fRoundResultAnimsCount_num == 0)
		{
			this.onNextAnimationEnd();
		}
	}

	_onRREnemyDeathCompleted()
	{
		this.onNextAnimationEnd();
	}

	_onRREnemyDestroyed()
	{
		this.onNextAnimationEnd();
	}

	_onAwardsAnimationsCompleted()
	{
		this.onNextAnimationEnd();
	}

	_onBigWinsAnimationsCompleted()
	{
		this.onNextAnimationEnd();
	}

	_onBigWinsAnimationsInterrupted()
	{
		this.onNextAnimationEnd();
	}

	_onCompletedQuestsAnimationsCompleted()
	{
		this.onNextAnimationEnd();
	}

	_onBossModeWinAwardPresentationCompleted()
	{
		this.onNextAnimationEnd();
	}

	_onEndShowWeapon()
	{
		this.onNextAnimationEnd();
	}

	_onGunUnlockedBeforeRoundEnd()
	{
		this._stopListenForRoundEnd();
		this._startListenForRoundEnd(); //check from the beginning
	}

	_onAllArtilleryStrikesCompleted()
	{
		this._stopListenForRoundEnd();
		this._startListenForRoundEnd(); //check from the beginning
	}

	onNextAnimationEnd()
	{
		--this._fRoundResultAnimsCount_num;
		if (this._fRoundResultAnimsCount_num <= 0)
		{
			this._destroyAnimationsEndTimer();
			this._fAllAnimationsEndedTimer_t = new Timer(()=>this._onAllAnimationsEnd(), 100);
			this._stopListenForRoundEnd();
		}
	}

	_onAllAnimationsEnd()
	{
		this._destroyAnimationsEndTimer();
		this.emit(GameField.EVENT_ON_NEW_ROUND_STATE, {state: (this._fGameStateInfo_gsi.gameState === ROUND_STATE.PLAY)});
	}

	_destroyAnimationsEndTimer()
	{
		this._fAllAnimationsEndedTimer_t && this._fAllAnimationsEndedTimer_t.destructor();
		this._fAllAnimationsEndedTimer_t = null;
	}

	_checkForIncompleteRoundAnimations()
	{
		if (this._fRoundResultAnimsCount_num > 0)
		{
			this.clearRoom(true);

			this._forceRoundResult();
		}
	}

	_forceRoundResult()
	{
		this._stopListenForRoundEnd();
		this.emit(GameField.EVENT_ON_ROUND_RESULT_FORCED);
		this.emit(GameField.EVENT_ON_NEW_ROUND_STATE, {state: (this._fGameStateInfo_gsi.gameState === ROUND_STATE.PLAY)});
	}

	_stopListenForRoundEnd()
	{
		for (let i = 0; i < this._fRoundEndListenHandlers_arr.length; ++i)
		{
			let lObj = this._fRoundEndListenHandlers_arr[i].obj;
			let lEvent = this._fRoundEndListenHandlers_arr[i].event;
			let lHandler = this._fRoundEndListenHandlers_arr[i].handler;

			if (this._fRoundEndListenHandlers_arr[i].obj)
			{
				lObj.off(lEvent, lHandler, this);
			}
		}

		this._fRoundEndListenHandlers_arr = [];

		this._fRoundResultAnimsCount_num = 0;
	}

	_onTournamentModeServerStateChanged()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState)
		{
			if (this._fRoundResultAnimsCount_num > 0)
			{
				for (let enemy of this.enemies)
				{
					if (enemy.life != 0 || enemy.isDeathOutroAnimationStarted) continue;

					this.onNextAnimationEnd();
				}
			}
		}
	}

	_onTournamentModeClientStateChanged()
	{
		if (this._fTournamentModeInfo_tmi.isTournamentOnClientCompletedState)
		{
			this.removeWaitScreen();
		}
	}

	blurUI()
	{
		if (APP.isBattlegroundMode)
		{
			return;
		}

		this.spot && this.spot.blurSpot();

		for (var i = 0; i < this.players.length; ++i)
		{
			let curCoPlayer = this.players[i].spot;
			curCoPlayer && curCoPlayer.blurSpot();
		}
	}

	focusUI()
	{
		this.spot && this.spot.focusSpot()

		for (var i = 0; i < this.players.length; ++i)
		{
			let curCoPlayer = this.players[i].spot;
			curCoPlayer && curCoPlayer.focusSpot();
		}
	}

	_validateUIBlur()
	{
		switch (this._fGameStateInfo_gsi.gameState)
		{
			case ROUND_STATE.QUALIFY:
			case ROUND_STATE.WAIT:
				this.blurUI();
				break;
			case ROUND_STATE.PLAY:
				this.focusUI();
				break;
		}
	}

	hideUI(aOptShowOtherPlayers_bl=false)
	{
		if (this.spot && this.spot.weaponSpotView) this.spot.weaponSpotView.visible = false;
		if (this.spot) this.spot.visible = false;
		if (this.playersContainer) this.playersContainer.visible = aOptShowOtherPlayers_bl;
		if (this.treasuresSidebarController.view) this.treasuresSidebarController.view.visible = false;
	}

	showUI()
	{
		if (this.battlegroundTutorialController.isTutorialDisplayed)
		{
			return;
		}

		if (this.spot && this.spot.weaponSpotView) this.spot.weaponSpotView.visible = true;
		if (this.spot)
		{
			this.spot.visible = true;
		}
		if (this.playersContainer) this.playersContainer.visible = true;
		if (this.treasuresSidebarController.view) this.treasuresSidebarController.view.visible = true;
	}

	closeRoom()
	{
		this.clearRoom();
		this.resetPlayerWin();
		this._fMidRoundExitDialogActive_bln = false;
		this._fBonusDialogActive_bln = false;
		this._fFRBDialogActive_bln = false;
		this._checkBlur();
		this._roundResultScreenController.hideScreen();

		this.emit('roomFieldClosed');
	}


	_onBossModeAppearingShakeTheGroundRequired(event)
	{
		this.shakeTheGround("bossAppearing");
	}

	_addFilter(aContainer_sprt, aFilter_f)
	{
		if (!aContainer_sprt) return;

		aContainer_sprt.filters = aContainer_sprt.filters || [];

		let lId_num = aContainer_sprt.filters.indexOf(aFilter_f);
		if (!~lId_num)
		{
			let lAlphaFilter = new PIXI.filters.AlphaFilter();
			lAlphaFilter.resolution = APP.stage.renderer.resolution;
			aContainer_sprt.filters = aContainer_sprt.filters.concat([aFilter_f, lAlphaFilter]);
		}
	}

	_removeFilter(aContainer_sprt, aFilter_f)
	{
		if (!aContainer_sprt || !aContainer_sprt.filters || !aContainer_sprt.filters.length) return;

		let lId_num = aContainer_sprt.filters.indexOf(aFilter_f);
		if (~lId_num)
		{
			let lFilters_arr = [];
			for (let i = 0; i < aContainer_sprt.filters.length; ++i)
			{
				lFilters_arr.push(aContainer_sprt.filters[i]);
			}
			aContainer_sprt.filters = [];
			for (let i = 0; i < lFilters_arr.length; ++i)
			{
				if (i != lId_num)
				{
					aContainer_sprt.filters = aContainer_sprt.filters.concat([lFilters_arr[i]]);
				}
			}
		}
	}

	getSpotsCount()
	{
		let lSpotsCount_int = 0;

		for( let i = 0; i < PLAYERS_POSITIONS["DESKTOP"].length; i++ )
		{
			let lSeat_s = this.getSeat(i, true)

			if(lSeat_s)
			{
				lSpotsCount_int++;
			}
		}

		return lSpotsCount_int;
	}

	clearRoom (keepPlayersOnScreen = false, clearMasterPlayerInfo = true, aClearRicochetBullets_bl = false)
	{
		if (aClearRicochetBullets_bl)
		{
			this.clearRicochetBulletsIfRequired();
		}

		this.emit(GameField.EVENT_ON_CLEAR_ROOM_STARTED);

		this._destroyGroundFireTraceAnimation();
		this._clearScale();
		this.focusUI();
		this.removeWaitScreen();
		this.removeTimeLeftText();

		this.removeAllBullets();

		this.screen && this.screen.removeAllSteps();

		this.removeAllEnemies();
		this.removeAllFxAnimations();
		this._removeAllDefaultGunFireEffects();

		this.unlockGun();
		this._stopListenForRoundEnd();
		this.shotRequestsAwaiting = 0;
		this._fWeaponSwitchTry_bln = false;
		this._fWeaponSwitchInProgress_bln = false;
		this._fUnpresentedWeaponSurplus_num = null;
		this._fBossModeController_bmc && (this._fBossModeController_bmc.bossNumberShots = 0);
		this._fNeedExplodeFeatureInitiated_bln = false;

		this._isWaitingResponseToBetLevelChangeRequest = null;
		this._isPlusBetLevelChangeRequiredAfterFiring_bl = false;
		this._isMinusBetLevelChangeRequiredAfterFiring_bl = false;

		this._fFrogDeathDiffTime_num = null;

		this._resetTargetIfRequired(true);

		Sequence.destroy(Sequence.findByTarget(this.container));
		this.container.x = 0;
		this.container.y = 0;

		this._removeFilter(this.container, this._bulgePinchFilter);

		if (this._fCritAnims_arr)
		{
			for (let lAnim_sprt of this._fCritAnims_arr)
			{
				lAnim_sprt && lAnim_sprt.destroy();
				lAnim_sprt = null;
			}

			this._fCritAnims_arr = [];
		}

		if (this._fPowerUpMultipliers_puma_arr)
		{
			for (let lAnim_sprt of this._fPowerUpMultipliers_puma_arr)
			{
				lAnim_sprt && lAnim_sprt.destroy();
				lAnim_sprt = null;
			}

			this._fPowerUpMultipliers_puma_arr = [];
		}

		if (this._fWitchEnemyTeleportAnimations_ceta_arr)
		{
			for (let lAnim_sprt of this._fWitchEnemyTeleportAnimations_ceta_arr)
			{
				lAnim_sprt && lAnim_sprt.destroy();
				lAnim_sprt = null;
			}

			this._fWitchEnemyTeleportAnimations_ceta_arr = [];
		}

		if (this._fWitchSequences_arr)
		{
			for (let seq of this._fWitchSequences_arr)
			{
				seq && seq.destructor();
			}

			this._fWitchSequences_arr = [];
		}

		if (this.playersContainer)
		{
			Sequence.destroy(Sequence.findByTarget(this.playersContainer));
			this.playersContainer.position.set(0, 0);
		}

		if (this.spot)
		{
			Sequence.destroy(Sequence.findByTarget(this.spot));
			this.spot.position.set(this.playerPosition.x, this.playerPosition.y);
			this.validateBetLevelEmptyButtonPosition();
			this.spot.resetWaitingBetLevelChange();
		}

		if (this.spot && APP.isBattlegroundGame)
		{
			this.spot.forceLevelUps();
		}

		if (keepPlayersOnScreen)
		{
			this._resetPlayersSpotValues();

			if (this.playersContainer)
			{
				for (let seat of this.playersContainer.children)
				{
					let targetWeaponSpotView = seat.weaponSpotView;
					if (targetWeaponSpotView.pushSequence && !isNaN(targetWeaponSpotView.startPosX) && !isNaN(targetWeaponSpotView.startPosY))
					{
						targetWeaponSpotView.pushSequence.stop();
						targetWeaponSpotView.pushSequence.destructor();
						targetWeaponSpotView.pushSequence = null;

						targetWeaponSpotView.x = targetWeaponSpotView.startPosX;
						targetWeaponSpotView.y = targetWeaponSpotView.startPosY;
					}

					targetWeaponSpotView.gun.resetGun();
				}
			}
		}
		else
		{
			if (this.roundResultActive)
			{
				this._roundResultScreenController.showScreen(true);
			}

			this.resetBalanceFlags();
			this.endShowWeapon();

			if (this.spot)
			{
				this.spot.off(MainPlayerSpot.EVENT_ON_WEAPON_SELECTED, this._onWeaponSelected, this);
				this.spot.off(MainPlayerSpot.EVENT_RELOAD_REQUIRED, this._onReloadRequired, this);
				this.spot.off(MainPlayerSpot.EVENT_ON_BET_MULTIPLIER_CHANGED, this._onPlayerBetMultiplierUpdated, this);
				this.spot.off(MainPlayerSpot.EVENT_ON_BET_UPDATE_REQUIRED, this._onPlayerBetMultiplierUpdateRequired, this);
				this.spot.off(MainPlayerSpot.EVENT_ON_ROTATE_GUN_TO_ZERO, this._onPlayerSpotRotateToZeroWeaponRequired, this);
			}

			this._removeAllPlayersSpots(clearMasterPlayerInfo);
			this.redrawAmmoText();
		}

		this._destroyCurChangeWeaponTimerInfo();

		if (this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY && !keepPlayersOnScreen)
		{
			let l_wci = this._fWeaponsController_wsc.i_getInfo();
			let lIsLastFreeShot_bln = l_wci.isFreeWeaponsQueueActivated && l_wci.currentWeaponId != WEAPONS.DEFAULT && !l_wci.remainingSWShots;
			if (!lIsLastFreeShot_bln)
			{
				this.rememberWeaponToRestore();
			}
		}

		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
		this._validateInteractivity();

		// clear this.screen ...
		this.removeRoomGradient();
		// ... clear this.screen

		this.emit(GameField.EVENT_ON_ROOM_FIELD_CLEARED, {keepPlayersOnScreen: keepPlayersOnScreen});
		this.emit(GameField.EVENT_ON_START_UPDATE_CURSOR_POSITION);

		this._fCryogunsController_csc.off(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._cryogunMainSpotBeamAnimationCompletedForQueue, this, true);
		this._fCryogunsController_csc.off(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._cryogunMainSpotBeamAnimationCompletedForUnlock, this, true);

		this._fFlameThrowersController_ftsc.off(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._flameThrowerMainSpotBeamAnimationCompletedForQueue, this, true);
		this._fFlameThrowersController_ftsc.off(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._flameThrowerMainSpotBeamAnimationCompletedForUnlock, this, true);

		this._fArtilleryStrikesController_assc.off(ArtilleryStrikesController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED, this._artilleryStrikeMainStrikeAnimationCompletedForQueue, this, true);
		this._fArtilleryStrikesController_assc.off(ArtilleryStrikesController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED, this._artilleryStrikeMainStrikeAnimationCompletedForUnlock, this, true);

		this._fPlazmaGunsController_pgsc.off(PlazmaGunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._plazmagunMainSpotBeamAnimationCompletedForQueue, this, true);
		this._fPlazmaGunsController_pgsc.off(PlazmaGunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._plazmagunMainSpotBeamAnimationCompletedForUnlock, this, true);

		this._fRicochetLasersController_rlsc.off(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._ricochetLasersMainSpotBeamAnimationCompletedForQueue, this, true);
		this._fRicochetLasersController_rlsc.off(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._ricochetLasersMainSpotBeamAnimationCompletedForUnlock, this, true);

		if(!this.isBossEnemyExist)
		{
			this._startRemovingBossHealthBar();
		}

		this._fEnemiesPlantWithPlayPreDeathAnimation_arr = [];
	}

	changeWeaponIfRequired()
	{
		if(this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated && this._fWeaponsInfo_wsi.remainingSWShots <= 0)
		{
			this.selectNextWeaponFromTheQueue();
		}
	}

	clearRicochetBulletsIfRequired()
	{
		if (!this.ricochetController)
		{
			return;
		}

		let lDelayedRicochetShotsAmount = APP.webSocketInteractionController.delayedRicochetShotsAmount;
		let lHasDelayedRicochetShots = lDelayedRicochetShotsAmount > 0;
		if (
				this.ricochetController.info.isAnyBulletExist
				|| lHasDelayedRicochetShots
			)
		{
			this.clearRicochetBullets();
		}
	}

	clearRicochetBullets()
	{
		this.emit(GameField.EVENT_ON_BULLET_CLEAR);
	}

	_onDelayedRicochetShotRemoved(aEvent_obj)
	{
		if (this._fGameStateInfo_gsi.isPlayState || this._isFrbMode)
		{
			APP.gameScreen.revertAmmoBack(aEvent_obj.weaponId, aEvent_obj.betLevel, false, false, 1);
		}
	}

	onConnectionClosedHandled()
	{
		this._fWeaponSwitchInProgress_bln = false;

		this._destroyCurChangeWeaponTimerInfo();
		this._fConnectionClosed_bln = true;

		this._fConnectionHasBeenRecentlyClosed_bln = true;


		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
	}

	onConnectionOpenedHandled()
	{
		this._fConnectionClosed_bln = false;
		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
	}

	/*special for game pause: when player exits to lobby, open secondary screen or change active tab*/
	hideRoom()
	{
		this.changeWeaponIfRequired();
		this.clearRicochetBulletsIfRequired();
		this.clearRoom(false, false);
	}

	/*special for game unpause: when player return to game from lobby or another tab, or close secondary screen*/
	resetBalanceFlags()
	{
		this._fRoundResultResponseReceived_bl = false;

		if (!this.roundResultActive)
		{
			this._fExternalBalanceUpdated_bl = false;
		}
	}

	showRoom(state)
	{
		if (typeof state != "string")
		{
			return;
		}

		switch (state)
		{
			case ROUND_STATE.PLAY:
				this.focusUI();
				if (this.battlegroundTutorialController.isTutorialDisplayed)
				{
					this.hideUI(true);
				}
				else
				{
					this.showUI();
				}
				
				if (this.spot)
				{
					let lCurrentWeaponId_int = this._fWeaponsInfo_wsi.currentWeaponId;
					if (lCurrentWeaponId_int != WEAPONS.DEFAULT || this._fWeaponsInfo_wsi.ammo == 0)
					{
						if (this._fWeaponsInfo_wsi.remainingSWShots == 0 && this.shotRequestsAwaiting == 0 && this._fWeaponsInfo_wsi.isFreeWeaponsQueueActivated)
						{
							this.selectNextWeaponFromTheQueue();
						}
					}

					this.redrawAmmoText();
				}
				this._tryToBuyAmmoFromRoundResult();
				break;

			case ROUND_STATE.WAIT:
				this.blurUI();
				if (this._fRoundResultResponseReceived_bl || this.roundResultActive)
				{
					this._fRoundResultOnLasthandWaitState_bln = true;
				}
				this._fExternalBalanceUpdated_bl = false;
				this._fRoundResultResponseReceived_bl = false;

				if (!this.roundResultActive && this.seatId != -1)
				{
					this.addTimeLeftText();
				}
				break;

			case ROUND_STATE.QUALIFY:
				this.blurUI();

				if (this.seatId != -1)
				{
					if (!this.roundResultActive)
					{
						this._startListenForRoundEnd();
					}

					this.removeWaitScreen();
				}
				break;
		}
	}

	removeTimeLeftText()
	{
		if (this._fWaitingCaption_spr)
		{
			this._fWaitingCaption_spr.spinner && this._fWaitingCaption_spr.spinner.destroy();

			this._fWaitingCaption_spr.destroy();
			this._fWaitingCaption_spr = null;
		}
	}

	addTimeLeftText()
	{
		if (APP.gameScreen.transitionViewController.info.isFeatureActive)
		{
			return;
		}

		if (!this._fWaitingCaption_spr)
		{
			this._fWaitingCaption_spr = this.addChild(APP.library.getSprite('preloader/loading_back'));
			this._fWaitingCaption_spr.zIndex = Z_INDEXES.WAITING_CAPTION;

			let lCaption_cta = this._fWaitingCaption_spr.addChild(I18.generateNewCTranslatableAsset('TAWaitingForNewRoundCaption'));
			lCaption_cta.position.set(0, 17);

			this._fWaitingCaption_spr.spinner = this._fWaitingCaption_spr.addChild(new PreloadingSpinner(2100, 110));
			this._fWaitingCaption_spr.spinner.position.y = -55;
			this._fWaitingCaption_spr.spinner.startAnimation();
		}
	}

	onAvatartUpdate(data)
	{
		let seat;
		if (this.spot)
		{
			seat = this.spot;
		}

		seat && seat.updateAvatar(data);
	}

	updateCommonPanelIndicators(data, duration = 0)
	{
		let lData_obj = this._fCommonPanelIndicatorsData_obj || {};
		if (data.balance !== undefined)
		{
			lData_obj.balance = { value:data.balance };
		}
		if (data.win !== undefined)
		{
			if(APP.isBattlegroundGame)
			{
				this.spot && this.spot.setScore(data.win);
			}
			lData_obj.win = { value:data.win };
		}
		if (duration > 0)
		{
			lData_obj.duration = duration;
		}


		this._fCommonPanelIndicatorsData_obj = lData_obj;
	}

	calcBalanceValue()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		let lResultState_bl = this.roundResultActivationInProgress || this.roundResultActive;
		let lBalance_num = lPlayerInfo_pi.balance;

		if (APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress)
		{
			return lBalance_num;
		}

		if (!this._isFrbMode && !APP.currentWindow.gameFrbController.info.continousNextModeFrb)
		{
			if (
					this._fConnectionHasBeenRecentlyClosed_bln
					&& (
							this._fGameStateInfo_gsi.gameState == ROUND_STATE.WAIT
							|| this._fGameStateInfo_gsi.gameState == ROUND_STATE.QUALIFY
						)
				)
			{
				if (!APP.currentWindow.isKeepSWModeActive && this._fWeaponsController_wsc.i_getInfo().currentWeaponId !== WEAPONS.DEFAULT)
				{
					this.changeWeapon(WEAPONS.DEFAULT);
				}
			}
			else if ((!this._fExternalBalanceUpdated_bl || !lResultState_bl) && !this._fIsWinLimitExceeded_bl && !this._fRoundResultOnLasthandWaitState_bln)
			{
				let lRicochetBulletsAmnt_int = this.ricochetController.info.activeMasterBulletsAmount;
				let lRicochetBulletsCost_num = lRicochetBulletsAmnt_int*lPlayerInfo_pi.currentStake*lPlayerInfo_pi.betLevel;
				let lAmmoCost_num = !lResultState_bl ? this._fWeaponsInfo_wsi.realAmmo * lPlayerInfo_pi.currentStake : -lRicochetBulletsCost_num;
				let lAdditional_num = lPlayerInfo_pi.qualifyWin - lPlayerInfo_pi.unpresentedWin + lAmmoCost_num;
				if (this._fConnectionHasBeenRecentlyClosed_bln && !lResultState_bl)
				{
					lAdditional_num = lAmmoCost_num;
				}
				lBalance_num += lAdditional_num;
			}
			else if (this._fExternalBalanceUpdated_bl && !this.roundResultActive)
			{
				let lRicochetBulletsAmnt_int = this.ricochetController.info.activeMasterBulletsAmount;
				let lRicochetBulletsCost_num = lRicochetBulletsAmnt_int*lPlayerInfo_pi.currentStake*lPlayerInfo_pi.betLevel;
				lBalance_num -= lPlayerInfo_pi.unpresentedWin + lRicochetBulletsCost_num;

				if (lBalance_num < 0)
				{
					// possible when cash bonus win limit exceeded
					lBalance_num = 0;
				}
			}
			else if (
						(this._isBonusMode || this._fTournamentModeInfo_tmi.isTournamentMode)
						&& this._fExternalBalanceUpdated_bl && this.roundResultActive
						&& this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY
					)
			{
				let lAmmoCost_num = this._fWeaponsInfo_wsi.realAmmo * lPlayerInfo_pi.currentStake;
				lBalance_num += lAmmoCost_num;
			}
		}
		else
		{
			// calculations for frb mode
			// notes:
			// 1. total frb win amount should be added to balance value only at the end of frb (when frb end notification appears)
			// 2. external balance that we get from the server after "FRBEnded" message will already contain frb win, so we must deduct win till frb end notification appearing
			// 3. frb end notification can be delayed after "FRBEnded" message due to awaiting for win animation (coins flying)

			let lBalanceAdd_num = 0;
			if (APP.currentWindow.gameFrbController.info.frbEnded && APP.currentWindow.gameFrbController.info.isFrbCompleted)
			{
				if (!this.roundResultActive)
				{
					// "FRBEnded" already occured but notification still not presented
					lBalanceAdd_num = this._fExternalBalanceUpdated_bl ? -lPlayerInfo_pi.qualifyWin : 0;
				}
				else
				{
					// frb end notification already presented
					lBalanceAdd_num = this._fExternalBalanceUpdated_bl ? 0 : lPlayerInfo_pi.qualifyWin;
				}
			}
			lBalance_num += lBalanceAdd_num;
		}

		return lBalance_num;
	}

	updatePlayerBalance()
	{
		let lBalance_num = this.calcBalanceValue();
		this.updateCommonPanelIndicators({balance: lBalance_num});

		//JIRA... https://jira.dgphoenix.com/browse/MQAMZN-164
		if (lBalance_num < this.currentWin && !this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
		//...JIRA
	}

	updatePlayerWin(aValue_num = 0, aOptDuration_num = 0, aOptBounce_bln = false)
	{
		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			if (this._fRoundResultActive_bln)
			{
				return;
			}

			this.currentWinTimeout && this.currentWinTimeout.destructor();
			this.currentWinTimeout = new Timer(this.resetPlayerWin.bind(this), 3000);
		}

		if (aOptBounce_bln)
		{
			this.spotBounce();
		}

		if (this.currentWin === undefined) this.currentWin = 0;
		this.currentWin += aValue_num;

		//JIRA... https://jira.dgphoenix.com/browse/MQAMZN-164
		let lBalance_num = this.calcBalanceValue();
		if (lBalance_num < this.currentWin && !this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
		else
		{
			this.updateCommonPanelIndicators({balance:this.calcBalanceValue(), win:this.currentWin}, aOptDuration_num);
		}
		//...JIRA
	}

	resetPlayerWin()
	{
		this.currentWinTimeout && this.currentWinTimeout.destructor();
		this.currentWinTimeout = null;

		this.currentWin = 0;
		this.updateCommonPanelIndicators({win:this.currentWin});
	}

	//returns PlayerSpot ?
	getSeat(id, optAllowSpot)
	{
		optAllowSpot = Boolean(optAllowSpot);

		if (optAllowSpot && this.spot && this.spot.id == id)
		{
			return this.spot;
		}

		if (this.playersContainer)
		{
			for (let seat of this.playersContainer.children)
			{
				if (seat.id == id) return seat;
			}
		}

		return null;
	}

	getPlayerBySeatId(seatId)
	{
		for (let player of this.players)
		{
			if (player.seatId == seatId)
			{
				return player;
			}
		}
		return null;
	}

	_onWeaponSelected(event)
	{
		let weaponId = event.weaponId;

		let lSuccess_bl = this.tryToChangeWeapon(weaponId);
		if (!lSuccess_bl)
		{
			//change selected weapon back
			this.emit(GameField.EVENT_ON_CHANGE_WEAPON_FAILED);
		}
	}

	tryToChangeWeaponOnPlayerBetMultiplierUpdated()
	{
		let lCurrentWeaponId_int = this._fWeaponsController_wsc.i_getInfo().currentWeaponId;

		if (lCurrentWeaponId_int === WEAPONS.DEFAULT)
		{
			this.tryToChangeWeapon(WEAPONS.DEFAULT);
		}
	}

	get isWeaponChangeInProcess()
	{
		return !!this._fCurChangeWeaponTimerInfo_obj && !!this._fCurChangeWeaponTimerInfo_obj.timer;
	}

	tryToChangeWeapon(weaponId, aIgnoreRoundResult_bln, aIsNewAwardedLevelUp_bl=false)
	{
		if (this._fCurChangeWeaponTimerInfo_obj)
		{
			if (this._fCurChangeWeaponTimerInfo_obj.weaponId == WEAPONS.HIGH_LEVEL)
			{
				// we should not interrupt HIGH_LEVEL weapon switch
				return;
			}
			else
			{
				this._destroyCurChangeWeaponTimerInfo();
			}
		}

		let lCurrentTime_num = Date.now();
		let lTimeout_num = lCurrentTime_num - this._fLastFireTime_num;
		let lFireTimeout_num = this._fireTimeout;

		if (this.gunLocked || this.shotRequestsAwaiting > 0
			|| (lTimeout_num  < lFireTimeout_num && weaponId != WEAPONS.HIGH_LEVEL) /* after switching weapon fireImmediately might be initiated, we need to be sure there enough time passed*/
			|| this._fWeaponSwitchInProgress_bln)
		{
			//this.gunLocked - this means that weapon shooting animation in progress
			this._startChangeWeaponTimer(weaponId, aIsNewAwardedLevelUp_bl);

			return false;
		}

		this.changeWeapon(weaponId, aIgnoreRoundResult_bln, false, aIsNewAwardedLevelUp_bl);

		if (this.spot && this.spot.hasPendingHighLevelShots)
		{
			this._startChangeWeaponTimer(WEAPONS.HIGH_LEVEL, true);
		}

		return true;
	}

	_startChangeWeaponTimer(weaponId, aIsNewAwardedLevelUp_bl)
	{
		let lChangeWeaponTimer_tmr = new Timer( () => {
															this._destroyCurChangeWeaponTimerInfo();
															this.tryToChangeWeapon(weaponId, false, aIsNewAwardedLevelUp_bl, true);
														},
												150);

		this._fCurChangeWeaponTimerInfo_obj = {timer: lChangeWeaponTimer_tmr, weaponId:weaponId};
	}

	_destroyCurChangeWeaponTimerInfo()
	{
		if (!this._fCurChangeWeaponTimerInfo_obj)
		{
			return;
		}

		let lCurChangeWeaponTimerInfo_obj = this._fCurChangeWeaponTimerInfo_obj;
		lCurChangeWeaponTimerInfo_obj.timer.destructor();

		delete lCurChangeWeaponTimerInfo_obj.timer;
		delete lCurChangeWeaponTimerInfo_obj.weaponId;

		this._fCurChangeWeaponTimerInfo_obj = null;
	}

	changeWeapon(weaponId, aIgnoreRoundResult_bln = false, aIsSkipAnimation_bl = false, aIsNewAwardedLevelUp_bl=false)
	{
		if (APP.currentWindow.gameFrbController.info.frbEnded || !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			return;
		}

		//BATTLEGROUND...
		if(APP.isBattlegroundGame)
		{
			this.spot && this.spot.updateAmmo();
		}
		//...BATTLEGROUND

		if (this._fWeaponToRestore_int !== null) weaponId = this._fWeaponToRestore_int;

		let lWeaponsInfo_wsi = this._fWeaponsController_wsc.info;
		let lNextWeaponIdByWSI_num = lWeaponsInfo_wsi.i_getNextWeaponIdToShoot();

		if (lNextWeaponIdByWSI_num !== WEAPONS.DEFAULT)
		{
			let lShots_num = lWeaponsInfo_wsi.i_getWeaponFreeShots(lNextWeaponIdByWSI_num);
			if (aIsSkipAnimation_bl && lShots_num > 0 && this.spot)
			{
				weaponId = lNextWeaponIdByWSI_num;
			}
		}

		if (this._fCurChangeWeaponTimerInfo_obj)
		{
			if (this._fCurChangeWeaponTimerInfo_obj.weaponId == WEAPONS.HIGH_LEVEL)
			{
				return;
			}
			else
			{
				this._destroyCurChangeWeaponTimerInfo();
			}
		}

		if (
				!APP.currentWindow.player.sitIn
				&& !(
						this._fConnectionHasBeenRecentlyClosed_bln
						&& (
								this._fGameStateInfo_gsi.gameState == ROUND_STATE.WAIT
								|| this._fGameStateInfo_gsi.gameState == ROUND_STATE.QUALIFY
							)
					)
			)
		{
			return;
		}

		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;

		let lSpotCurrentDefaultWeaponId_int = lWeaponsInfo_wsi.currentDefaultWeaponId;

		if(
			lWeaponsInfo_wsi.isAnyFreeSpecialWeaponExist
			&& !lWeaponsInfo_wsi.isFreeWeaponsQueueActivated
			&& this._fRestoreAfterFreeShotsWeaponId_int === undefined
			)
		{
			if(lCurrentWeaponId_int === undefined)
			{
				lCurrentWeaponId_int = WEAPONS.DEFAULT;
			}

			this._fRestoreAfterFreeShotsWeaponId_int = lCurrentWeaponId_int;
		}

		if (lCurrentWeaponId_int === weaponId && !this._fWeaponSwitchTry_bln)
		{
			if (this.spot)
			{
				if (
						this._fWeaponToRestore_int === null
						&& lCurrentWeaponId_int !== WEAPONS.DEFAULT
						&& (
							(lWeaponsInfo_wsi.remainingSWShots > 0
							&& !lWeaponsInfo_wsi.isFreeWeaponsQueueActivated) //when current SW switches from paid to free
							|| (lWeaponsInfo_wsi.remainingSWShots <= 0
							&& lNextWeaponIdByWSI_num === WEAPONS.DEFAULT)) //when current SW switches from free to paid

					)
				{
					this.emit(GameField.EVENT_ON_WEAPON_UPDATED, {weaponId: weaponId});
				}

				this.redrawAmmoText();
				this.spot.on(MainPlayerSpot.EVENT_RELOAD_REQUIRED, this._onReloadRequired, this);

				this.spot.changeWeapon(lCurrentWeaponId_int, lSpotCurrentDefaultWeaponId_int, aIsSkipAnimation_bl, aIsNewAwardedLevelUp_bl);
			}

			this._fWeaponSwitchTry_bln = false;
			this._fWeaponSwitchInProgress_bln = false;

			if (this._fWeaponToRestore_int !== null)
			{
				this.emit(GameField.EVENT_ON_WEAPON_UPDATED, {weaponId: weaponId});
			}
			return;
		}

		if (lCurrentWeaponId_int == WEAPONS.DEFAULT && weaponId != WEAPONS.DEFAULT && weaponId != WEAPONS.HIGH_LEVEL)
		{
			APP.soundsController.play('mq_booster_end');
		}

		this.shotRequestsAwaiting = 0;

		if (!APP.currentWindow.isPaused)
		{
			this._fWeaponSwitchTry_bln = true;
			this._fWeaponSwitchInProgress_bln = true;
		}

		this.emit(GameField.EVENT_ON_WEAPON_UPDATED, {weaponId: weaponId});

		if (
				!APP.currentWindow.isKeepSWModeActive
				&& (
						this._fGameStateInfo_gsi.gameState == ROUND_STATE.WAIT
						|| this._fGameStateInfo_gsi.gameState == ROUND_STATE.QUALIFY
					)
			)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEAPON_SELECTED, {weaponId: weaponId});
		}

		this.redrawAmmoText();
		this.spot && this.spot.changeWeapon(weaponId, lSpotCurrentDefaultWeaponId_int, false, aIsNewAwardedLevelUp_bl);

		//this.autofireTime = 0;
	}

	changeWeaponToDefaultImmediatelyAfterSitOut()
	{
		this._destroyCurChangeWeaponTimerInfo();

		this.emit(GameField.EVENT_ON_WEAPON_UPDATED, {weaponId: WEAPONS.DEFAULT});
	}

	_selectRememberedWeapon(aWeapon_num)
	{
		this.changeWeapon(aWeapon_num, null, true);
	}

	selectNextWeaponFromTheQueue()
	{
		let lNextWeaponId_int = this.getNextWeaponFromTheQueue();
		this.changeWeapon(lNextWeaponId_int);
		return lNextWeaponId_int;
	}

	getNextWeaponFromTheQueue()
	{
		let lWeaponsInfo_wsi = this._fWeaponsController_wsc.i_getInfo();
		let lNextWeaponId_int = undefined;

		if(lWeaponsInfo_wsi.i_getWeaponFreeShots(WEAPONS.HIGH_LEVEL) > 0)
		{
			lNextWeaponId_int = WEAPONS.HIGH_LEVEL;
		}
		else if(lWeaponsInfo_wsi.autoEquipFreeSW)
		{
			lNextWeaponId_int = lWeaponsInfo_wsi.i_getNextWeaponIdToShoot();

			//WHEN NO FREE SHOTS LEFT, RESTORE WEAPON
			if(
				lNextWeaponId_int === WEAPONS.DEFAULT &&
				this._fRestoreAfterFreeShotsWeaponId_int !== undefined
				)
			{
				lNextWeaponId_int = this._fRestoreAfterFreeShotsWeaponId_int;
				this._fRestoreAfterFreeShotsWeaponId_int = undefined;
			}
			//...WHEN NO FREE SHOTS LEFT, RESTORE WEAPON
		}
		else
		{
			lNextWeaponId_int = WEAPONS.DEFAULT;
		}

		return lNextWeaponId_int;
	}

	onClearAmmo()
	{
		this.updatePlayerBalance();

		if (this._fUnpresentedWeaponSurplus_num)
		{
			this.emit(GameField.EVENT_ON_WEAPON_TO_UNPRESENTED_TRANSFER_REQUIRED, {winValue: this._fUnpresentedWeaponSurplus_num})
			this._fUnpresentedWeaponSurplus_num = null;
		}
	}

	onBuyAmmoResponse()
	{
		//this._fRoundResultBuyAmmoRequest_bln = false;

		this.resetBalanceFlags();
		// this.updatePlayerTotalWin();
		this.updatePlayerBalance();
		this.redrawAmmoText();
	}

	onReBuyAmmoResponse()
	{
		this.updatePlayerBalance();
		this.redrawAmmoText();
	}

	showAddWeapon(weapon, pos, enemyLife, optMultipleWeapons, optFinalPosition)
	{
		if (!pos)
		{
			throw new Error('GameField :: showAddWeapon >> pos is null');
		}
		this._fIsWeaponAddingInProgress_bl = true;
		if (enemyLife === 0)
		{
			if (optMultipleWeapons)
			{
				this.showWeapon(pos, weapon, optFinalPosition);
			}
			else
			{
				this.showCrateWithWeapon(pos.x, pos.y, this.spot, weapon);
			}
		}
		else
		{
			this.showWeapon(pos, weapon);
		}

		this._tryToShowPowerUpMultiplier(pos, weapon.id);
	}

	getWeaponEmblemAssetName(weaponId)
	{
		let assetName = '';

		switch (weaponId)
		{
			case WEAPONS.DEFAULT:
				assetName = 'weapons/DefaultGun/turret_1/turret';
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				assetName = 'weapons/emblems/plasma_emblem';
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				assetName = 'weapons/emblems/cryogun_emblem';
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				assetName = 'weapons/emblems/ricochet_emblem';
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				assetName = 'weapons/emblems/flamethrower_emblem';
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				assetName = 'weapons/emblems/artillerystrike_emblem';
				break;
			case WEAPONS.HIGH_LEVEL:
				assetName = 'battleground/powerup/background';
				break;
			default:
				throw new Error ('no assets for weapon id ' + weaponId);
		}
		return assetName;
	}

	getWeaponEmblemGlowAssetName(weaponId)
	{
		let assetName = '';
		switch (weaponId)
		{
			case WEAPONS.DEFAULT:
				assetName = 'weapons/DefaultGun/turret_1/turret';
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				assetName = 'weapons/emblems/plasma_emblem_glow';
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				assetName = 'weapons/emblems/cryogun_emblem_glow';
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				assetName = 'weapons/emblems/ricochet_emblem_glow';
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				assetName = 'weapons/emblems/flamethrower_emblem_glow';
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				assetName = 'weapons/emblems/artillerystrike_emblem_glow';
				break;
			case WEAPONS.HIGH_LEVEL:
				assetName = 'battleground/powerup/background';
				break;
			default:
				throw new Error ('no assets for weapon id ' + weaponId);
		}
		return assetName;
	}

	getWeaponEmblemAnchor()
	{
		return {x: 0.5, y: 0.5};
	}

	getWeaponEmblemGlowAnchor(weaponId)
	{
		let anchor = {x: 0.5, y: 0.5};
		switch (weaponId)
		{
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				anchor = { x: 123 / 256, y: 120 / 256 };
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				anchor = { x: 122 / 256, y: 121 / 256 };
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				anchor = { x: 128 / 256, y: 128 / 256 };
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				anchor = { x: 130 / 256, y: 128 / 256 };
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				anchor = { x: 0.51, y: 0.5 };
				break;
		}
		return anchor;
	}

	getWeaponContentLandingPosition(aOptWeaponId_int)
	{
		if (this.spot)
		{
			//BATTLEGROUND...
			if(APP.isBattlegroundGame)
			{
				let l_pt = this.spot.getAwardedWeaponLandingPosition(aOptWeaponId_int);

				return this.spot.localToGlobal(l_pt.x, l_pt.y);
			}
			//...BATTLEGROUND

			if (
				aOptWeaponId_int != null &&
				this._fWeaponsInfo_wsi.remainingSWShots > 0
				)
			{
				// move to sidebar
				return this.weaponsSidebarController.i_getWeaponLandingPosition(aOptWeaponId_int);
			}
			return this.spot.spotVisualCenterPoint;
		}
		return APP.isMobile ? {x: 502, y: 532} : {x: 534, y: 532};
	}

	getMasterCoinsLandingPosition()
	{
		if (this.spot)
		{
			return this.spot.spotVisualCenterPoint;
		}
		return null;
	}

	showWeapon(pos, aAwardedWeapon_obj, aOptFinalPosition_pt)
	{
		let lAwardedWeapon_obj = aAwardedWeapon_obj || this._fWeaponsInfo_wsi.i_extractAwardedWeapon();

		if (!this.spot)
		{
			return;
		}

		let lWeaponId_num = lAwardedWeapon_obj.id;
		let weaponAssetName = this.getWeaponEmblemAssetName(lWeaponId_num);
		let weaponAssetAnchor = this.getWeaponEmblemAnchor(lWeaponId_num);

		let weaponAssetGlowName = this.getWeaponEmblemGlowAssetName(lWeaponId_num);
		let weaponAssetGlowAnchor = this.getWeaponEmblemGlowAnchor(lWeaponId_num);
		let weaponAssetGlowScale = 2;
		let weaponAssetGlowDescriptor = {
			assetName: weaponAssetGlowName,
			anchor: weaponAssetGlowAnchor,
			scale: weaponAssetGlowScale
		};

		let contentItemInfo = new ContentItemInfo(ContentItemInfo.TYPE_WEAPON, this.spot.id, weaponAssetName, weaponAssetAnchor, null, lWeaponId_num, lAwardedWeapon_obj, null, null, weaponAssetGlowDescriptor);
		let contentItem = this.topScreen.addChild(new ContentItem(contentItemInfo));
		contentItem.zIndex = Z_INDEXES.AWARDED_WEAPON_CONTENT;
		let lWeaponItemHeight_num = null;
		let lWeaponItemWidth_num = null;

	
		lWeaponItemHeight_num = APP.library.getSpriteFromAtlas(weaponAssetName).height;
		lWeaponItemWidth_num = APP.library.getSpriteFromAtlas(weaponAssetName).width;
		
		
		let lWeaponBaseScale_num = contentItem.getBaseScale();
		let lWeaponMaxScale_num = contentItem.getMaxScale();

		let lFreeShotCounterOffset_num = contentItem.getAmmoCounterOffset();
		let lFreeShotCounterAreaDescriptor_fscad = I18.getTranslatableAssetDescriptor("TAAwardedFreeShotsCounterLabel").areaInnerContentDescriptor.areaDescriptor;
		let lFreeShotsCounterLabelWidth_num = FreeShotsCounterView.i_getWidth();
		let lFreeShotsCounterLabelHeigth_num = lFreeShotCounterAreaDescriptor_fscad.height;

		let lFinalPosition_pt = aOptFinalPosition_pt ? new PIXI.Point(aOptFinalPosition_pt.x, aOptFinalPosition_pt.y) : new PIXI.Point(0, -100);
		let lPosX_num = pos.x;
		let lPosY_num = pos.y;

		let lMaxWeaponWidth_num = lWeaponItemWidth_num * lWeaponBaseScale_num * lWeaponMaxScale_num;
		let lMaxWidth_num = Math.max(
			lMaxWeaponWidth_num,
			lFreeShotsCounterLabelWidth_num + lFreeShotCounterOffset_num
		);

		if (pos.x < ((lMaxWidth_num / 2) - lFinalPosition_pt.x))
		{
			lPosX_num = (lMaxWidth_num / 2);
		}
		else if (pos.x > (APP.config.size.width - (lMaxWidth_num / 2) + lFinalPosition_pt.x))
		{
			lPosX_num = APP.config.size.width - (lMaxWidth_num / 2);
		}

		let lMaxWeaponHeight_num = lWeaponItemHeight_num * lWeaponBaseScale_num * lWeaponMaxScale_num;
		let lMaxHeight_num = Math.max(
			lMaxWeaponHeight_num,
			lFreeShotCounterOffset_num + lFreeShotsCounterLabelHeigth_num
		);

		if (pos.y < ((lMaxHeight_num / 2) - lFinalPosition_pt.y))
		{
			lPosY_num = (lMaxHeight_num / 2) - lFinalPosition_pt.y;
		}
		else if (pos.y > (APP.config.size.height - (lMaxHeight_num / 2) + lFinalPosition_pt.y))
		{
			lPosY_num = APP.config.size.height - (lMaxHeight_num / 2);
		}

		contentItem.position.set(lPosX_num, lPosY_num);
		contentItem.once(ContentItem.ON_CONTENT_LANDED, this._onWeaponContentLanded.bind(this, lAwardedWeapon_obj));
		contentItem.startAnimation(lFinalPosition_pt, false, 2, false);
		this._fNewWeapons_arr.push(contentItem);

		lFreeShotCounterAreaDescriptor_fscad = null;
		lWeaponItemHeight_num= null;
		lWeaponItemWidth_num = null;
	}

	_tryToShowPowerUpMultiplier(aEnemyPosition_obj, aWeaponId_int)
	{
		const lPlayerInfo_pi = APP.playerController.info;
		const lCurrentPowerUpMultiplier_num = lPlayerInfo_pi.currentPowerUpMultiplier;
		if (lCurrentPowerUpMultiplier_num > 1 && this._isPowerUpWeapon(aWeaponId_int))
		{
			this._showPowerUpMultiplier(aEnemyPosition_obj, lCurrentPowerUpMultiplier_num)
		}
	}

	_isPowerUpWeapon(aWeaponId_int)
	{
		for (let lPowerUpWeapon in POWER_UP_WEAPONS)
		{
			if (POWER_UP_WEAPONS[lPowerUpWeapon] == aWeaponId_int)
			{
				return true;
			}
		}

		return false;
	}

	_showPowerUpMultiplier(aEnemyPosition_obj, aCurrentPowerUpMultiplier_num)
	{
		let lContainer_spr = this.powerUpMultiplierAnimationContainer.container.addChild(new Sprite());
		let lPowerUpMultiplier_pumv = lContainer_spr.addChild(new PowerUpMultiplierView());
		lContainer_spr.zIndex = this.powerUpMultiplierAnimationContainer.zIndex;
		lPowerUpMultiplier_pumv.value = aCurrentPowerUpMultiplier_num;

		let lSpot_ps = this.getSeat(this.seatId, true);
		let lCenterPoint_obj = lSpot_ps.globalGunCenter;

		lPowerUpMultiplier_pumv.on(PowerUpMultiplierView.EVENT_ON_APPEAR_ANIMATION_COMPLETED, ()=>{
			lPowerUpMultiplier_pumv && lPowerUpMultiplier_pumv.destroy();
			lPowerUpMultiplier_pumv = null;
		})
		lPowerUpMultiplier_pumv.i_startAppearAnimation(aEnemyPosition_obj, lCenterPoint_obj);
	}

	_onWeaponContentLanded(aLandedWeapon_obj, event)
	{
		let lLandedWeapon_obj = aLandedWeapon_obj;
		let contentItem = event ? event.target : null;

		if (contentItem)
		{
			var lIndex_int = this._fNewWeapons_arr.indexOf(contentItem);
			if (~lIndex_int)
			{
				this._fNewWeapons_arr.splice(lIndex_int, 1);
			}
			contentItem.destroy();
		}

		this.redrawAmmoText();

		this._onWeaponAddingAnimationCompleted(lLandedWeapon_obj);
		this.emit(GameField.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING);
		APP.gameScreen.awardWeaponLanded(lLandedWeapon_obj);
	}

	_updateWeaponImmediately(aWeapon_obj, aIsNewAwardedLevelUp_bl=false)
	{
		if (aIsNewAwardedLevelUp_bl && !!this.spot)
		{
			this.spot.addPendingHighLevelShots(aWeapon_obj.shots);
		}

		this.redrawAmmoText();

		if (APP.currentWindow.isPaused)
		{
			this.rememberWeaponToRestore();
		}

		this._onWeaponAddingAnimationCompleted(aWeapon_obj, aIsNewAwardedLevelUp_bl);
		this.emit(GameField.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING);
	}

	_onWeaponAddingAnimationCompleted(aLandedWeapon_obj, aIsNewAwardedLevelUp_bl=false)
	{
		this._fIsWeaponAddingInProgress_bl = false;

		if (
				this._fRoundResultActive_bln
				|| (
						APP.currentWindow.isPaused
						&& this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY
						&& !APP.currentWindow.isKeepSWModeActive
					)
			)
		{
			return;
		}

		this.emit(GameField.EVENT_ON_END_SHOW_WEAPON, {weapon: aLandedWeapon_obj});
		this.redrawAmmoText();

		if(!APP.playerController.info.didThePlayerWinSWAlready && aLandedWeapon_obj !== undefined && aLandedWeapon_obj.id !== WEAPONS.HIGH_LEVEL)
		{
			this._fFirstPicksUpWeapon_num = aLandedWeapon_obj.id;
			this.emit(GameField.EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME);
			return;
		}

		const lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;
		let lIsFreeShot_bl = lCurrentWeaponId_int !== WEAPONS.DEFAULT && lWeaponsInfo_wsi.remainingSWShots > 0 && lWeaponsInfo_wsi.isFreeWeaponsQueueActivated;

		if (
				(lWeaponsInfo_wsi.autoEquipFreeSW
				&& aLandedWeapon_obj !== undefined
				&& !lIsFreeShot_bl
				&& this._isProperWeaponLevelApplied(aLandedWeapon_obj.id))
				||
				(aLandedWeapon_obj !== undefined && aLandedWeapon_obj.id == WEAPONS.HIGH_LEVEL)
			)
		{
			this.tryToChangeWeapon(aLandedWeapon_obj.id, false, aIsNewAwardedLevelUp_bl);
		}
	}

	endShowWeapon()
	{
		while (this._fNewWeapons_arr.length)
		{
			let lAward_obj = this._fNewWeapons_arr.shift();

			let lContentItems_arr = null;

			if (lAward_obj instanceof Crate)
			{
				lContentItems_arr = lAward_obj.contentItems;
			}
			else if (lAward_obj instanceof ContentItem)
			{
				lContentItems_arr = [lAward_obj];
			}

			while (lContentItems_arr.length)
			{
				let lContentItem = lContentItems_arr.pop();
				let lAwardedWeapon_obj = lContentItem.info ? lContentItem.info.awardedWeapon : lContentItem.awardedWeapon;

				if (lAwardedWeapon_obj)
				{
					if (!APP.currentWindow.player.sitIn)
					{
						this._fIsWeaponAddingInProgress_bl = false;
						this.emit(GameField.EVENT_ON_END_SHOW_WEAPON, {weapon: lAwardedWeapon_obj});
					}
					else
					{
						this._onWeaponAddingAnimationCompleted(lAwardedWeapon_obj);
					}
				}
			}

			lAward_obj.destroy();
		}
	}

	showCrateWithWeapon(x, y, playerSeat, aAwardedWeapon_obj)
	{
		if (!this.spot)
		{
			return;
		}

		let cratePos = new PIXI.Point(x, y);

		let lAwardedWeapon_obj = aAwardedWeapon_obj || this._fWeaponsInfo_wsi.i_extractAwardedWeapon();

		let lWeaponId_num = lAwardedWeapon_obj.id;
		let assetName = this.getWeaponEmblemAssetName(lWeaponId_num);
		let assetAnchor = this.getWeaponEmblemAnchor(lWeaponId_num);

		let weaponAssetGlowName = this.getWeaponEmblemGlowAssetName(lWeaponId_num);
		let weaponAssetGlowAnchor = this.getWeaponEmblemGlowAnchor(lWeaponId_num);
		let weaponAssetGlowScale = 2;
		let weaponAssetGlowDescriptor = {
			assetName: weaponAssetGlowName,
			anchor: weaponAssetGlowAnchor,
			scale: weaponAssetGlowScale
		};

		let crateContentItemInfo = new ContentItemInfo(ContentItemInfo.TYPE_WEAPON, playerSeat.id, assetName, assetAnchor, null, lWeaponId_num, lAwardedWeapon_obj, null, null, weaponAssetGlowDescriptor);
		var crate = this.topScreen.addChild(new Crate(cratePos, [crateContentItemInfo]));
		crate.once(Crate.ON_CRATE_DISAPPEARED, this._onCrateDisappeared, this);
		crate.once(Crate.ON_CRATE_WEAPON_REVEAL, () => {
			let shift = crate.calculateCrateFinalPoint({x:x, y:y});
		});
		crate.zIndex = crate.y + crate.finalPoint.y + 65;

		crate.once(Crate.ON_CRATE_CONTENT_LANDED, this._onWeaponContentLanded.bind(this, lAwardedWeapon_obj));
		this._fNewWeapons_arr.push(crate);

		return crate;
	}

	_onCrateDisappeared(event)
	{
		let crate = event.target;
		var lIndex_int = this._fNewWeapons_arr.indexOf(crate);
		if (~lIndex_int)
		{
			this._fNewWeapons_arr.splice(lIndex_int, 1);
		}
		crate.destroy();
	}

	spotBounce()
	{
		this.spot && this.spot.bounceEffect();
	}

	playerSeatBounce(aSeatId_int)
	{
		let playerSeat = this.getSeat(aSeatId_int, true);
		playerSeat && playerSeat.bounceEffect();
	}

	// DRAW PLAYERS ...
	drawAllPlayers(players, masterSeatId = -1, aRedrawMasterPlayerScore_bl = false)
	{//GetRoomInfo, response field "seats"
		if (APP.currentWindow.isPaused)
		{
			return;
		}

		this._removeCoPlayers();
		this._initPlayersContainerIfRequired();
		this._initSeatsBurstContainerIfRequired();

		let lPlayerPos_int = -1;
		for (var i = 0; i < players.length; i ++)
		{
			if (masterSeatId>=0 && players[i].seatId == masterSeatId)
			{
				if (aRedrawMasterPlayerScore_bl)
				{
					this.redrawPlayerScore(players[i]);
				}
				continue;
			}
			else if (this.seatId>=0 && players[i].seatId == this.seatId)
			{
				continue;
			}

			lPlayerPos_int = SEATS_POSITION_IDS[players[i].seatId];

			var spot = this._drawCoPlayer(players[i], lPlayerPos_int);
			spot.currentScore = players[i].currentScore;

			this.players.push(players[i]);
		}

		if (	this.roundResultActive
			&&	this._fGameStateInfo_gsi
			&&	this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY
			)
		{
			this._resetPlayersSpotValues();
		}

		this.showKingsOfTheHill([]);
	}

	_drawCoPlayer(player, aId_int)
	{
		let pos = this._getSpotPosition(aId_int, false);
		player.master = false;
		player.positionId = aId_int;
		let playerContainer = player.spot = this.playersContainer.addChild(new PlayerSpot(player, pos));
		playerContainer.position.set(pos.x, pos.y);

		return playerContainer;
	}

	_removeCoPlayers()
	{
		while (this.players && this.players.length)
		{
			delete this.players.pop();
		}
		this.players = [];

		if (this.playersContainer)
		{
			this.playersContainer.destroyChildren();
		}
	}

	_onPlayerBetMultiplierUpdateRequired(aEvent_obj)
	{
		//this._resetTargetIfRequired(true);
		this._isWaitingResponseToBetLevelChangeRequest = true;
		this.emit(GameField.EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED, {id: aEvent_obj.id, multiplier: aEvent_obj.multiplier});
	}

	_onPlayerBetMultiplierUpdated(aEvent_obj)
	{
		this.emit(GameField.EVENT_ON_BET_MULTIPLIER_UPDATED, {id: aEvent_obj.id, multiplier: aEvent_obj.multiplier});

		if (aEvent_obj.weaponUpdateAllowed)
		{
			this.tryToChangeWeaponOnPlayerBetMultiplierUpdated();
		}
	}

	addMasterPlayerSpot(player, aOptCurrentWeaponId_int = -1)
	{
		this.removeMasterPlayerSpot();

		this.seatId = player.seatId;
		this.nickname = player.nickname;
		this.currentScore = player.currentScore;

		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
		else if (APP.isBattlegroundGame)
		{
			this.currentWin = APP.gameScreen.player.lastReceivedBattlegroundScore_int;
		}

		let id = player.seatId;
		let seat = this.getSeat(id);
		if (seat) seat.destroy();

		let lPlayerPosition_int = SEATS_POSITION_IDS[id];
		this.playerPosition = this._getSpotPosition(lPlayerPosition_int, true);

		player.master = true;
		player.positionId = lPlayerPosition_int;


		let lMainPlayerSpot_s;
		if (APP.isBattlegroundGame)
		{
			lMainPlayerSpot_s = new BattlegroundMainPlayerSpot(player, this.playerPosition, true);
			lMainPlayerSpot_s.on(BattlegroundMainPlayerSpot.EVENT_ON_AUTOFIRE_BUTTON_ENABLED, this._onAutoFireButtonEnabled, this);
			lMainPlayerSpot_s.on(BattlegroundMainPlayerSpot.EVENT_ON_AUTOFIRE_BUTTON_DISABLED, this._onAutoFireButtonDisabled, this);
		}
		else
		{
			lMainPlayerSpot_s = new MainPlayerSpot(player, this.playerPosition, true);
		}

		this.spot = this.topScreen.addChild(lMainPlayerSpot_s);

		this.spot.position.set(this.playerPosition.x, this.playerPosition.y);
		this.spot.zIndex = Z_INDEXES.MAIN_SPOT;
		this.spot.on(MainPlayerSpot.EVENT_ON_WEAPON_SELECTED, this._onWeaponSelected, this);
		this.spot.on(MainPlayerSpot.EVENT_ON_BET_MULTIPLIER_CHANGED, this._onPlayerBetMultiplierUpdated, this);
		this.spot.on(MainPlayerSpot.EVENT_ON_BET_UPDATE_REQUIRED, this._onPlayerBetMultiplierUpdateRequired, this);
		this.spot.on(MainPlayerSpot.EVENT_ON_CHANGE_WEAPON_TO_DEFAULT_REQUIRED, this._onPlayerChangeWeaponToDefaultRequired, this);
		this.spot.on(MainPlayerSpot.EVENT_ON_ROTATE_GUN_TO_ZERO, this._onPlayerSpotRotateToZeroWeaponRequired, this);

		if (!this._fBetLevelPlusButtonHitArea_b)
		{
			let lButtonParameters_obj = this.spot.betLevelPlusButtonParameters;
			this._fBetLevelPlusButtonHitArea_b = this.topScreen.addChild(new BetLevelEmptyButton(lButtonParameters_obj.width, lButtonParameters_obj.height));
			this._fBetLevelPlusButtonHitArea_b.zIndex = Z_INDEXES.BET_LEVEL_BUTTON_HIT_AREA;
			this._fBetLevelPlusButtonHitArea_b.on(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK, this._onBetLevelEmptyPlusButtonClicked, this);
			this._fBetLevelPlusButtonHitArea_b.on(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE, this._onBetLevelEmptyButtonRestrictedZone, this);
		}

		if (!this._fBetLevelMinusButtonHitArea_b)
		{
			let lButtonParameters_obj = this.spot.betLevelMinusButtonParameters;
			this._fBetLevelMinusButtonHitArea_b = this.topScreen.addChild(new BetLevelEmptyButton(lButtonParameters_obj.width, lButtonParameters_obj.height));
			this._fBetLevelMinusButtonHitArea_b.zIndex = Z_INDEXES.BET_LEVEL_BUTTON_HIT_AREA;
			this._fBetLevelMinusButtonHitArea_b.on(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK, this._onBetLevelEmptyMinusButtonClicked, this);
			this._fBetLevelMinusButtonHitArea_b.on(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE, this._onBetLevelEmptyButtonRestrictedZone, this);
		}

		this.validateBetLevelEmptyButtonPosition();

		if (this._fWeaponToRestore_int !== null && this._fWeaponsInfo_wsi.remainingSWShots)
		{
			this.changeWeapon(this._fWeaponToRestore_int);
			this.redrawAmmoText();

			this.spot.on(MainPlayerSpot.EVENT_RELOAD_REQUIRED, this._onReloadRequired, this);

			let lSpotCurrentDefaultWeaponId_int = this._fWeaponsController_wsc.i_getInfo().currentDefaultWeaponId;
			this.spot.changeWeapon(this._fWeaponToRestore_int, lSpotCurrentDefaultWeaponId_int);

			this._fWeaponToRestore_int = null;
		}
		else
		{
			let lCurrentWeaponId_int = aOptCurrentWeaponId_int !== undefined ? aOptCurrentWeaponId_int : WEAPONS.DEFAULT;

			const l_wci = this._fWeaponsController_wsc.i_getInfo();
			if (!l_wci.isFreeWeaponsQueueActivated && !isNaN(this._fRestoreAfterFreeShotsWeaponId_int))
			{
				lCurrentWeaponId_int = this._fRestoreAfterFreeShotsWeaponId_int;
				this._fRestoreAfterFreeShotsWeaponId_int = undefined;
			}

			if ((this._fRoundResultRestoreWeapon_num !== null || this._fWeaponToRestore_int != null) && this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY)
			{
				let lWeaponToRestore_num = this._fRoundResultRestoreWeapon_num;
				if (this._fWeaponToRestore_int !== null)
				{
					lWeaponToRestore_num = this._fWeaponToRestore_int;
				}
				this._selectRememberedWeapon(lWeaponToRestore_num);
				this._fRoundResultRestoreWeapon_num = null;
				this._fWeaponToRestore_int = null;
			}
			else if (
						(this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY || this.roundResultActivationInProgress)
						&& lCurrentWeaponId_int != WEAPONS.DEFAULT
						&& !this._fNewPlayerFlag_bln
					)
			{
				this.tryToChangeWeapon(lCurrentWeaponId_int);
			}
			else if (this._fGameStateInfo_gsi.gameState == ROUND_STATE.PLAY && !this._fNewPlayerFlag_bln)
			{
				this.selectNextWeaponFromTheQueue();
			}
			else if (this._fNewPlayerFlag_bln)
			{
				this.selectNextWeaponFromTheQueue();
			}
			else
			{
				this.changeWeapon(lCurrentWeaponId_int);
			}

			this.redrawAmmoText();
			this.spot.on(MainPlayerSpot.EVENT_RELOAD_REQUIRED, this._onReloadRequired, this);
		}

		this._validateCursor();

		this.emit(GameField.EVENT_ON_MASTER_SEAT_ADDED, {seatId: this.seatId});
	}

	validateBetLevelEmptyButtonPosition()
	{
		let lBottomOffset_num = this.spot.isBottom ? 0: 6;

		if (this._fBetLevelPlusButtonHitArea_b)
		{
			let lButtonParameters_obj = this.spot.betLevelPlusButtonParameters;
			this._fBetLevelPlusButtonHitArea_b.position.set(this.spot.x + lButtonParameters_obj.x, this.spot.y + lButtonParameters_obj.y + lBottomOffset_num);
		}

		if (this._fBetLevelMinusButtonHitArea_b)
		{
			let lButtonParameters_obj = this.spot.betLevelMinusButtonParameters;
			this._fBetLevelMinusButtonHitArea_b.position.set(this.spot.x + lButtonParameters_obj.x, this.spot.y + lButtonParameters_obj.y + lBottomOffset_num);
		}
	}

	_onBetLevelEmptyPlusButtonClicked()
	{
		if (this._isWaitingResponseToBetLevelChangeRequest || this.isMasterBulletExist() || this.shotRequestsAwaiting > 0)
		{
			if (this.spot && this.spot.isPlusButtonClickedAllowed())
			{
				if(APP.isAutoFireMode)
				{
					//this._resetTargetIfRequired(true);
				}
				this._isPlusBetLevelChangeRequiredAfterFiring_bl = true;
				this.spot.onPlusButtonClicked(true, true);
			}

			return;
		}
		else if (this.spot && this.spot.isPlusButtonClickedAllowed())
		{
			if(APP.isAutoFireMode)
			{
				//this._resetTargetIfRequired(true);
			}
			this.spot.onPlusButtonClicked(true, false);
		}
	}

	_onBetLevelEmptyMinusButtonClicked()
	{
		if (this._isWaitingResponseToBetLevelChangeRequest || this.isMasterBulletExist() || this.shotRequestsAwaiting > 0)
		{
			if (this.spot && this.spot.isMinusButtonClickedAllowed())
			{
				if(APP.isAutoFireEnabled)
				{
					//this._resetTargetIfRequired(true);
				}
				//this._resetTargetIfRequired(true);
				this._isMinusBetLevelChangeRequiredAfterFiring_bl = true;
				this.spot.onMinusButtonClicked(true, true);
			}

			return;
		}
		else if (this.spot && this.spot.isMinusButtonClickedAllowed())
		{
			if(APP.isAutoFireEnabled)
			{
				//this._resetTargetIfRequired(true);
			}
			//this._resetTargetIfRequired(true);
			this.spot.onMinusButtonClicked(true, false);
		}
	}

	_onAutoFireButtonEnabled(aEvent_obj)
	{
		this._fAutofireButtonEnabled_bl = true;

		if (aEvent_obj.enemy)
		{
			this.emit(GameField.EVENT_ON_AUTOFIRE_BUTTON_ENABLED);
			this._tryUpdateTargetAndFire(aEvent_obj.enemy);
		}
		else
		{
			let lTargetEnemy_enm = this.getNearestEnemy(this.spot.x, this.spot.y, 2);

			if (!lTargetEnemy_enm)
			{
				this._validateAutofireButton(false);
				return;
			}
			else
			{
				this.emit(GameField.EVENT_ON_AUTOFIRE_BUTTON_ENABLED);
				this._tryUpdateTargetAndFire(lTargetEnemy_enm);
			}
		}
	}

	_onAutoFireButtonDisabled()
	{
		this._fAutofireButtonEnabled_bl = false;

		this.emit(GameField.EVENT_ON_AUTOFIRE_BUTTON_DISABLED);
		this.resetTargetIfRequired(true);
	}

	checkingNeedChangeBetLevelAfterFiring()
	{
		if (!this.isMasterBulletExist() && this.shotRequestsAwaiting <= 0)
		{
			if (this._isPlusBetLevelChangeRequiredAfterFiring_bl)
			{
				this._isPlusBetLevelChangeRequiredAfterFiring_bl = false;
				this.spot.onPlusButtonClicked(false, false);
			}

			if (this._isMinusBetLevelChangeRequiredAfterFiring_bl)
			{
				this._isMinusBetLevelChangeRequiredAfterFiring_bl = false;
				this.spot.onMinusButtonClicked(false, false);
			}
		}
	}

	_onBetLevelEmptyButtonRestrictedZone(e)
	{
		if (this.spot)
		{
			this.spot.betLevelEmptyButtonRestrictedZone = e.isRestricted;
		}
	}

	_onPlayerChangeWeaponToDefaultRequired()
	{
		this.tryToChangeWeapon(WEAPONS.DEFAULT);
	}

	_onPlayerSpotRotateToZeroWeaponRequired()
	{
		this.rotateGun(APP.config.size.width/2, APP.config.size.height/2);
	}

	rememberWeaponToRestore()
	{
		let lPendingWeaponId_num = null;
		let lPendingMasterSWAwards = APP.gameScreen.pendingMasterSWAwards;
		if (lPendingMasterSWAwards && lPendingMasterSWAwards.length)
		{
			lPendingWeaponId_num = lPendingMasterSWAwards[0].id;
		}

		const l_wci = this._fWeaponsController_wsc.i_getInfo();

		if (lPendingWeaponId_num !== null)
		{
			l_wci.i_awardWeapon(lPendingWeaponId_num.id, lPendingWeaponId_num.shots);
		}
		if (
					l_wci
					&& l_wci.autoEquipFreeSW
					&& l_wci.isAnyFreeSpecialWeaponExist
					&& l_wci.currentWeaponId == WEAPONS.DEFAULT
				)
		{
			this._fWeaponToRestore_int = l_wci.i_getNextWeaponIdToShoot()
		}
		else if (l_wci && l_wci.currentWeaponId !== undefined && this._fGameStateInfo_gsi.gameState === ROUND_STATE.PLAY)
		{
			this._fWeaponToRestore_int = l_wci.currentWeaponId;
		}
		else
		{
			this._fWeaponToRestore_int = null;
		}
	}

	clearWeapons()
	{
		this._fWeaponToRestore_int = null;
		this._fWeaponsController_wsc.i_clearAll();
	}

	_onReloadRequired()
	{
		this._tryToBuyAmmo();
	}

	removeMasterPlayerSpot(clearMasterPlayerInfo = true)
	{
		this.spot && this.spot.destroy();
		this.spot = null;

		if (this._fBetLevelPlusButtonHitArea_b)
		{
			this._fBetLevelPlusButtonHitArea_b.off(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK, this._onBetLevelEmptyPlusButtonClicked, this);
			this._fBetLevelPlusButtonHitArea_b.off(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE, this._onBetLevelEmptyButtonRestrictedZone, this);
			this._fBetLevelPlusButtonHitArea_b.destroy();
			this._fBetLevelPlusButtonHitArea_b = null;
		}

		if (this._fBetLevelMinusButtonHitArea_b)
		{
			this._fBetLevelMinusButtonHitArea_b.off(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_CLICK, this._onBetLevelEmptyMinusButtonClicked, this);
			this._fBetLevelMinusButtonHitArea_b.off(BetLevelEmptyButton.EVENT_BET_LEVEL_EMPTY_BUTTON_RESTRICTED_ZONE, this._onBetLevelEmptyButtonRestrictedZone, this);
			this._fBetLevelMinusButtonHitArea_b.destroy();
			this._fBetLevelMinusButtonHitArea_b = null;
		}

		if (clearMasterPlayerInfo)
		{
			this.seatId = -1;
			this.playerPosition = null;
		}
	}

	_initPlayersContainerIfRequired()
	{
		if (this.playersContainer)
		{
			return;
		}

		this.playersContainer = this.topScreen.addChild(new GameFieldPlayersContainer);
		this.playersContainer.zIndex = Z_INDEXES.PLAYERS_CONTAINER;
	}

	_removeAllPlayersSpots(clearMasterPlayerInfo = true)
	{
		this._removeCoPlayers();
		this.removeMasterPlayerSpot(clearMasterPlayerInfo);
	}

	_resetPlayersSpotValues()
	{
		if (!this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.resetPlayerWin();
		}
	}


	_initSeatsBurstContainerIfRequired()
	{
		if (this.playerRewardContainer)
		{
			return;
		}

		this.playerRewardContainer = this.topScreen.addChild(new GameFieldPlayerSeatsBurstContainer);
		this.playerRewardContainer.zIndex = Z_INDEXES.PLAYER_REWARD;
	}
	// ... DRAW PLAYERS

	redrawPlayerScore(player)
	{
		if (!this.spot)
		{
			let playerInfo = APP.currentWindow.player;
			if (!playerInfo)
			{
				throw new Error("Cannot redraw master player spot - no player info.");
			}
			this.addMasterPlayerSpot(playerInfo, player.weapon.id);
		}
	}

	handleAmmoBackMessage()
	{
		this.redrawAmmoText();
		this.updatePlayerBalance();
		this.unlockGun()
	}

	onShotRequest()
	{
		this.shotRequestsAwaiting++;
	}

	onShotResponse()
	{
		this.shotRequestsAwaiting--;
		if (this.shotRequestsAwaiting < 0)
		{
			this.shotRequestsAwaiting = 0;
		}
		this.checkingNeedChangeBetLevelAfterFiring();
	}

	decreaseAmmo(aShotAmmoAmount_int, aIsPaidSWShot_bl = false)
	{
		if (isNaN(aShotAmmoAmount_int) || aShotAmmoAmount_int < 1)
		{
			throw new Error(`Incorrect decrease ammo amount: ${aShotAmmoAmount_int}`);
		}

		this.emit(GameField.EVENT_DECREASE_AMMO, {decreaseAmmoAmount: aShotAmmoAmount_int, isPaidSpecialShot: aIsPaidSWShot_bl});
		this.redrawAmmoText();
		this.updatePlayerBalance();
	}

	updateCurrentWeapon(aIsPaidSpecialShot_bl)
	{
		let lWeaponsInfo_wsi = this._fWeaponsController_wsc.i_getInfo();
		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;

		if (!(this.spot && lCurrentWeaponId_int !== undefined && this.shotRequestsAwaiting === 0) )return;

		if (lCurrentWeaponId_int !== WEAPONS.DEFAULT && lWeaponsInfo_wsi.remainingSWShots == 0 && !aIsPaidSpecialShot_bl)
		{

			if (this.isRicochetSW(lCurrentWeaponId_int) && this.ricochetController.info.getMasterBullets(true, lCurrentWeaponId_int).length > 0) return;

			switch (lCurrentWeaponId_int)
			{
				case WEAPONS.RICOCHET:
				case WEAPONS.RICOCHET_POWER_UP:
					this._fRicochetLasersController_rlsc.once(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._ricochetLasersMainSpotBeamAnimationCompletedForQueue, this);
					break;
				case WEAPONS.INSTAKILL:
				case WEAPONS.INSTAKILL_POWER_UP:
					this._fPlazmaGunsController_pgsc.once(PlazmaGunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._plazmagunMainSpotBeamAnimationCompletedForQueue, this);
					break;
				case WEAPONS.CRYOGUN:
				case WEAPONS.CRYOGUN_POWER_UP:
					this._fCryogunsController_csc.once(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._cryogunMainSpotBeamAnimationCompletedForQueue, this);
					break;
				case WEAPONS.FLAMETHROWER:
				case WEAPONS.FLAMETHROWER_POWER_UP:
					this._fFlameThrowersController_ftsc.once(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._flameThrowerMainSpotBeamAnimationCompletedForQueue, this);
					break;
				case WEAPONS.ARTILLERYSTRIKE:
				case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
					this._fArtilleryStrikesController_assc.once(ArtilleryStrikesController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED, this._artilleryStrikeMainStrikeAnimationCompletedForQueue, this);
					break;
				default:
					this.selectNextWeaponFromTheQueue();
					break;
			}

			this.redrawAmmoText();
		}
	}

	_cryogunMainSpotBeamAnimationCompletedForQueue()
	{
		this.selectNextWeaponFromTheQueue();
	}

	_cryogunMainSpotBeamAnimationCompletedForUnlock()
	{
		this.unlockGun();
	}

	_flameThrowerMainSpotBeamAnimationCompletedForQueue()
	{
		this.selectNextWeaponFromTheQueue();
	}

	_flameThrowerMainSpotBeamAnimationCompletedForUnlock()
	{
		this.unlockGun();
	}

	_artilleryStrikeMainStrikeAnimationCompletedForQueue()
	{
		this.selectNextWeaponFromTheQueue();
	}

	_artilleryStrikeMainStrikeAnimationCompletedForUnlock()
	{
		this.unlockGun();
	}

	_plazmagunMainSpotBeamAnimationCompletedForQueue()
	{
		this.selectNextWeaponFromTheQueue();
	}

	_plazmagunMainSpotBeamAnimationCompletedForUnlock()
	{
		this.unlockGun();
	}

	_ricochetLasersMainSpotBeamAnimationCompletedForQueue()
	{
		this.selectNextWeaponFromTheQueue();
	}

	_ricochetLasersMainSpotBeamAnimationCompletedForUnlock()
	{
		this.unlockGun();
	}

	_artilleryGrenadeLanded()
	{
		this.shakeTheGround();
	}

	_onWeaponSurplusUpdated(aEvent_obj)
	{
		this._fUnpresentedWeaponSurplus_num = aEvent_obj.winBonus;
	}

	redrawAmmoText()
	{
		if (this.spot)
		{
			let lAmmo_int = this._fWeaponsInfo_wsi.ammo;
			if (this._fWeaponsInfo_wsi.currentWeaponId != WEAPONS.DEFAULT/* && this._fWeaponsInfo_wsi.remainingSWShots > 0*/)
			{
				lAmmo_int = this._fWeaponsInfo_wsi.remainingSWShots;
			}

			this.spot.updateAmmo(lAmmo_int, this.isAmmoBuyingInProgress);
		}
	}

	get isAmmoBuyingInProgress()
	{
		return this.waitBuyIn || APP.webSocketInteractionController.isRebuyRequestInProgress;
	}

	get _fireSettingsController()
	{
		return APP.currentWindow.fireSettingsController;
	}

	get _fireSettingsInfo()
	{
		return this._fireSettingsController.info;
	}

	get _fireTimeout()
	{
		switch (this._fireSettingsInfo.fireSpeed)
		{
			case 1:
				return MAX_FIRE_REQUEST_TIMEOUT;
			case 2:
				return MIDDLE_FIRE_REQUEST_TIMEOUT;
			case 3:
			default:
				return MIN_FIRE_REQUEST_TIMEOUT;
		}
	}

	get _autoFireTimeout()
	{
		let timeoutObj;
		switch (this._fireSettingsInfo.fireSpeed)
		{
			case 1:
				timeoutObj = AUTO_FIRE_TIMEOUTS.MAX;
				break;
			case 2:
				timeoutObj = AUTO_FIRE_TIMEOUTS.MIDDLE;
				break;
			case 3:
			default:
				timeoutObj = AUTO_FIRE_TIMEOUTS.MIN;
				break;
		}

		let timeout = timeoutObj.OTHER;
		switch (this._fWeaponsInfo_wsi.currentWeaponId)
		{
			case WEAPONS.CRYOGUN:
				timeout = timeoutObj.CRYOGUN;
				break;
			case WEAPONS.RICOCHET:
				timeout = timeoutObj.RICOCHET;
				break;
			case WEAPONS.FLAMETHROWER:
				timeout = timeoutObj.FLAMETHROWER;
				break;
			case WEAPONS.ARTILLERYSTRIKE:
				timeout = timeoutObj.ARTILLERYSTRIKE;
				break;
			case WEAPONS.INSTAKILL:
				timeout = timeoutObj.INSTAKILL;
				break;
			case WEAPONS.CRYOGUN_POWER_UP:
				timeout = timeoutObj.CRYOGUN_POWER_UP;
				break;
			case WEAPONS.RICOCHET_POWER_UP:
				timeout = timeoutObj.RICOCHET_POWER_UP;
				break;
			case WEAPONS.FLAMETHROWER_POWER_UP:
				timeout = timeoutObj.FLAMETHROWER_POWER_UP;
				break;
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				timeout = timeoutObj.ARTILLERYSTRIKE_POWER_UP;
				break;
			case WEAPONS.INSTAKILL_POWER_UP:
				timeout = timeoutObj.INSTAKILL_POWER_UP;
				break;
		}

		return timeout;
	}

	get _turretRotationTimeout()
	{
		return 10;
	}

	get _turretKeysMoveTimeout()
	{
		return 10;
	}

	get _isFireDenied()
	{
		return	this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY
				|| !this.spot
				|| (this.spot.weaponSpotView && !this.spot.weaponSpotView.visible)
				|| this.gunLocked
				|| this.roundResultActive
				|| APP.currentWindow.subloadingController.isLoadingScreenShown /*map is loading*/
				|| this._fWeaponSwitchInProgress_bln
				|| this.isWeaponChangeInProcess
				|| this.lobbySecondaryScreenActive
				|| this._fTournamentModeInfo_tmi.isTournamentOnServerCompletedState
				|| APP.webSocketInteractionController.isSitoutRequestInProgress
				|| APP.isAnyDialogActive
				|| this._fFireSettingsScreenActive_bln
				|| this._fWeaponToRestore_int !== null
				|| this._fServerMessageWeaponSwitchExpected_bln
				|| !this._isFireAllowed()
				|| (this.spot.weaponSpotView && this.spot.weaponSpotView.isWeaponChangeInProgress)
				|| this.spot && this.spot.isWeaponChangeEffectsInProgress
				|| APP.currentWindow.isExpectedBuy
				|| this._isPlusBetLevelChangeRequiredAfterFiring_bl
				|| this._isMinusBetLevelChangeRequiredAfterFiring_bl
				|| !this._isProperWeaponLevelApplied()
				|| APP.isBattlegroundGame && this.isFinalCountingFireDenied
				|| APP.isBattlegroundGame && APP.gameScreen.roundFinishSoon
				|| APP.isBattlegroundGame && this.scoreboardController.info.isRoundTimeIsOver
				|| this._fTargetingInfo_tc.isActiveTargetPaused
	}

	_isProperWeaponLevelApplied(aOptWeaponId_int=undefined)
	{
		if (!APP.isBattlegroundGame)
		{
			return true;
		}

		let lCurrentWeaponId_int = this._fWeaponsInfo_wsi.currentWeaponId;
		let lPlayerInfo_pi = APP.playerController.info;
		let lCurrentBetLevel_int = lPlayerInfo_pi.betLevel;
		let lCompareWeaponId_int = aOptWeaponId_int !== undefined ? aOptWeaponId_int : lCurrentWeaponId_int;

		return (lCurrentBetLevel_int == lPlayerInfo_pi.possibleBetLevels[0] && lCompareWeaponId_int !== WEAPONS.HIGH_LEVEL)
				|| (lCurrentBetLevel_int > lPlayerInfo_pi.possibleBetLevels[0] && lCompareWeaponId_int == WEAPONS.HIGH_LEVEL)
	}

	_isEnoughMoneyForOneShot()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		let lRealAmmo_num = lPlayerInfo_pi.realAmmo;

		if (isNaN(lRealAmmo_num))
		{
			lRealAmmo_num = 0;
		}

		lRealAmmo_num = Number(lRealAmmo_num.toFixed(2));

		if (
			lPlayerInfo_pi.balance >= this._fWeaponsInfo_wsi.i_getCurrentWeaponShotPrice()
			|| ~~(lRealAmmo_num) > 0
		)
		{
			return true;
		}

		if (APP.isBattlegroundGame && this._fWeaponsInfo_wsi.currentWeaponId == WEAPONS.DEFAULT)
		{
			return true;
		}

		return false;
	}

	

	fire(e, isRicochet = false)
	{
		//debug info for QA...
		// if (this._fWeaponsInfo_wsi && this._fWeaponsInfo_wsi.currentWeaponId === WEAPONS.DEFAULT)
		// {
		// 	console.log("FIRE attempt, DEFAULT weapon ammo:" + this._fWeaponsInfo_wsi.ammo);
		// }
		//...debug info for QA

		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;
		let lIsPaidSWShot_bl = this._isPaidSWShot;
		let IsFreeSWShots_bl = lCurrentWeaponId_int !== WEAPONS.DEFAULT && !lIsPaidSWShot_bl;
		let lIsFreeShot_bl = IsFreeSWShots_bl || this._isFrbMode;

		//https://jira.dgphoenix.com/browse/DRAG-753
		if (
			this._isFrbMode
			&& !IsFreeSWShots_bl
			&& lWeaponsInfo_wsi.ammo <= 0
		)
		{
			return;
		}

		if (this._isFireDenied)
		{
			//return;

			if(!this._isProperWeaponLevelApplied())
			{		
				if(!this._reset_weapon_time_int || Date.now() - this._reset_weapon_time_int > 1500)
				{
					//console.log("FireController. Fire denied yatzieeee ");
					//console.log("FireController. Fire denied fix 1 ");
					this._reset_weapon_time_int = Date.now();
					this.selectNextWeaponFromTheQueue();
				}
				//else{
					//console.log("FireController. Fire denied  fix cooldown period ");
				//}
			}
			


			// old 
			if (lIsFreeShot_bl || this._isBonusMode || this._fTargetingInfo_tc.isActiveTargetPaused)
			{
				//console.log("FireController return one  lIsFreeShot_bl " + lIsFreeShot_bl , " ,this._isBonusMode: " +this._isBonusMode + " ,isActiveTargetPaused:" + this._fTargetingInfo_tc.isActiveTargetPaused);
				return;
			}
			else if (!this._isEnoughMoneyForOneShot() && !this.roundResultActive)
			{
				//PREVENT FOR BATTLEGROUND MODE ON ROUND RESULT ACTIVATION...
				//https://jira.dgphoenix.com/browse/MQBG-114
				if(
					APP.gameScreen.isBattlegroundMode
					&&
					(
						this.roundResultActivationInProgress ||
						this._fGameStateInfo_gsi.gameState === ROUND_STATE.WAIT
						)
					)
				{
					//console.log("FireController fire DENIED two");
					return;
				}
				//...PREVENT FOR BATTLEGROUND MODE ON ROUND RESULT ACTIVATION

				if (APP.playerController.info.unpresentedWin > 0 || this._fIsWeaponAddingInProgress_bl) //wait until the win is presented
				{
					//console.log("FireController fire DENIED tree");
					return;
				}
				if(!APP.isBattlegroundGame)
				{
					this.emit(
						GameField.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED,
						{
							hasUnrespondedShots: APP.webSocketInteractionController.hasUnrespondedShots,
							hasDelayedShots: APP.webSocketInteractionController.hasDelayedShots,
							hasUnparsedShotResponse: APP.webSocketInteractionController.isShotRequestParseInProgress,
							hasAwardedFreeSW: this._fWeaponsController_wsc.i_getInfo().isAnyFreeSpecialWeaponExist || APP.gameScreen.isAwardWeaponLandExpected
						}
					);
				}
			}
			return;
		}

		this.autofireTime = 0;

		let lTargetPoint_pt = e.data.global;

		if (!this._fLastFireTime_num)
		{
			this._fLastFireTime_num = Date.now();
		}
		else
		{
			if(	this._fWeaponsInfo_wsi.currentWeaponId !== WEAPONS.DEFAULT
				&& this._fWeaponsInfo_wsi.currentWeaponId !== WEAPONS.HIGH_LEVEL
				&& this.shotRequestsAwaiting > 0)
			{
				//console.log("FireController fire DENIED five");
				return;
			}

			let lCurrentTime_num = Date.now();
			let lTimeout_num = lCurrentTime_num - this._fLastFireTime_num;

			let lFireTimeout_num = this._fireTimeout;

			if (lTimeout_num  < lFireTimeout_num)
			{
				APP.logger.i_pushDebug(`GameField. Shot is refused because the previous shot was done ${lTimeout_num} ms ago. This is less then a minimum timeout of ${lFireTimeout_num} ms.`);
				//console.log(`Shot is refused because the previous shot was done ${lTimeout_num} ms ago. This is less then a minimum timeout of ${lFireTimeout_num} ms.`);
				//console.log("FireController fire DENIED six");
				return;
			}

			this._fLastFireTime_num = lCurrentTime_num;
		}

		let lPlayerInfo_pi = APP.playerController.info;
		let lShotMultiplier_int = lPlayerInfo_pi.betLevel * this._calcShotMultiplier();
		let lShotAmmoAmount_int = lShotMultiplier_int;

		if (lIsFreeShot_bl)
		{
			lShotAmmoAmount_int = 1; // for FREE SW
		}

		let lIsBattlegroundDefaultShot_bl = lCurrentWeaponId_int == WEAPONS.DEFAULT && APP.isBattlegroundGame;
		if ( !lIsFreeShot_bl && !lIsBattlegroundDefaultShot_bl && lWeaponsInfo_wsi.ammo < lShotAmmoAmount_int)
		{
			APP.logger.i_pushDebug(`GameField. Rest ammo (${lWeaponsInfo_wsi.ammo}) < shot required ammo (${lShotAmmoAmount_int}).`);
			//console.log(`FireController rest ammo (${lWeaponsInfo_wsi.ammo}) < shot required ammo (${lShotAmmoAmount_int})`);
			if (!this.isAmmoBuyingInProgress)
			{
				if (this._fIsWeaponAddingInProgress_bl)
				{
					//console.log("FireController fire DENIED seven");
					return;
				}

				this.redrawAmmoText();

				let lRicochetBullets_num = lPlayerInfo_pi.ricochetBullets;
				if ((lPlayerInfo_pi.betLevel > 1 || lCurrentWeaponId_int !== WEAPONS.DEFAULT) && (lWeaponsInfo_wsi.ammo >= 1))
				{
					lRicochetBullets_num = 0; // Ignore if bet > 1 or not default weapon
				}
				let lShotCost_num = this._fWeaponsInfo_wsi.i_getCurrentWeaponShotPrice();
				if (lPlayerInfo_pi.balance < lShotCost_num && !lPlayerInfo_pi.unpresentedWin)
				{
					if (
							this._fTournamentModeInfo_tmi.isTournamentMode
							&& this._fWeaponsController_wsc.i_getInfo().isAnyFreeSpecialWeaponExist
							&& !IS_SPECIAL_WEAPON_SHOT_PAID
						)
					{
						// in tournaments we should not trigger rebuy if we still have
						this._resetTargetIfRequired();
					}
					else if (lRicochetBullets_num)
					{
						//console.log("FireController fire DENIED return eight");
						return; // No actions needed if we don't have money, but have ricochet bullets on screen
					}
					else
					{
						if(!APP.isBattlegroundGame)
						{
							this.emit(GameField.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED, {
								hasUnrespondedShots: APP.webSocketInteractionController.hasUnrespondedShots,
								hasDelayedShots: APP.webSocketInteractionController.hasDelayedShots,
								hasUnparsedShotResponse: APP.webSocketInteractionController.isShotRequestParseInProgress,
								hasAwardedFreeSW: this._fWeaponsController_wsc.i_getInfo().isAnyFreeSpecialWeaponExist || APP.gameScreen.isAwardWeaponLandExpected
							}
);
						}
						
					}
				}
				else
				{
					let lTotalRealAmmoAmount_num = lWeaponsInfo_wsi.realAmmo + lPlayerInfo_pi.pendingAmmo;
					lTotalRealAmmoAmount_num = Number(lTotalRealAmmoAmount_num.toFixed(2));

					let lTotalAmmoAmount_num = Math.floor(lTotalRealAmmoAmount_num);
					if (lTotalAmmoAmount_num < lShotAmmoAmount_int && !APP.playerController.info.unpresentedWin) //wait until the win is presented
					{
						
						this.emit(GameField.EVENT_ON_FIRE_CANCELLED_WITH_NOT_ENOUGH_AMMO);
					}

					if (this._isBonusMode)
					{
						this.selectNextWeaponFromTheQueue();
					}
				}
			}
			//console.log("FireController fire DENIED  nine");
			return;
		}
		if (lWeaponsInfo_wsi.currentWeaponId != WEAPONS.DEFAULT)
		{
			if (lWeaponsInfo_wsi.remainingSWShots == 0 && !lIsPaidSWShot_bl)
			{
				APP.logger.i_pushDebug(`GameField. '0 special ammo left, this.shotRequestsAwaiting = ${this.shotRequestsAwaiting}.`);
				//console.log('0 special ammo left, this.shotRequestsAwaiting = ' + this.shotRequestsAwaiting);
				//console.log("FireController fire DENIED ten");
				return;
			}
		}

		let lEnemyX_num = this.lastPointerPos.x = e.data.global.x;
		let lEnemyY_num = this.lastPointerPos.y = e.data.global.y;

		//check autoTargetingEnemy, if it's already dead or fire dinied...
		if (this.autoTargetingEnemy)
		{
			let enemyId = this.autoTargetingEnemy.id;
			let enemyObj = APP.currentWindow.getExistEnemy(enemyId);
			let lEnemy_e = this.getExistEnemy(enemyId);

			if ((enemyObj && enemyObj.life === 0) || lEnemy_e.isFireDenied || lEnemy_e.invulnerable)
			{
				//select another target
				this.emit(GameField.EVENT_ON_TARGET_ENEMY_IS_DEAD, {enemyId: enemyId});
			}
		}
		//...check autoTargetingEnemy, if it's already dead or fire dinied

		let enemy = this.autoTargetingEnemy || this.indicatedEnemy || this.getNearestEnemy(lEnemyX_num, lEnemyY_num);


		if(
			enemy &&
			!isRicochet &&
			!enemy.isTargetable()
			)
		{
			APP.logger.i_pushDebug("GameField. Fire attempt: nearest enemy is not targetable.");
			//console.log('Fire attempt: nearest enemy is not targetable.');
			//console.log("FireController fire DENIED twelve");
			return;
		}

		if (!enemy && !isRicochet)
		{
			APP.logger.i_pushDebug("GameField. Fire attempt: nearest enemy not found.");
			//console.log('Fire attempt: nearest enemy not found.');
			//console.log("FireController fire DENIED 13");
			return;
		}

		if (enemy && enemy.isDestroyed)
		{
			if (APP.mobileValidator.ios())
			{
				//IOS fire on reconnect fix
				enemy = this.getNearestEnemy(lEnemyX_num, lEnemyY_num);
			}

			if (enemy.isDestroyed)
			{
				//console.log("FireController fire DENIED  14");
				return;
			}
			else
			{
				this.indicatedEnemy = enemy;
			}
		}

		if (
			lCurrentWeaponId_int === WEAPONS.CRYOGUN ||
			lCurrentWeaponId_int === WEAPONS.ARTILLERYSTRIKE ||
			lCurrentWeaponId_int === WEAPONS.CRYOGUN_POWER_UP ||
			lCurrentWeaponId_int === WEAPONS.ARTILLERYSTRIKE_POWER_UP)
		{
			this.indicatedEnemy = null;
		}

		if (!(lCurrentWeaponId_int == WEAPONS.INSTAKILL || lCurrentWeaponId_int == WEAPONS.INSTAKILL_POWER_UP))
		{
			let pushPos = this.lastPointerPos;

			if (this.autoTargetingEnemy)
			{
				if (lCurrentWeaponId_int === WEAPONS.ARTILLERYSTRIKE || lCurrentWeaponId_int === WEAPONS.ARTILLERYSTRIKE_POWER_UP)
				{
					let currentPos = this.autoTargetingEnemy.getGlobalPosition(); // enemie's feet

					let enemyId = this.autoTargetingEnemy.id;
					let lTime_num = 1500; //1,5 seconds in the future
					let futurePos = APP.currentWindow.getEnemyPositionInTheFuture(enemyId, lTime_num);
					pushPos = futurePos || currentPos;
				}
				else
				{
					pushPos = this.autoTargetingEnemy.getCenterPosition(); // enemie's center
				}
			}
			else
			{
				if (lCurrentWeaponId_int === WEAPONS.CRYOGUN || lCurrentWeaponId_int === WEAPONS.CRYOGUN_POWER_UP)
				{
					// if the nearest enemy is out of the CRYOGUN radius - snap it to aim
					if (enemy)
					{
						let lEnemyGlobalPosition_pt = enemy.getGlobalPosition();
						let distance = Utils.getDistance(pushPos, lEnemyGlobalPosition_pt);
						if (distance > CryogunsController.WEAPON_ACTION_RADIUS)
						{
							pushPos = lEnemyGlobalPosition_pt;
						}
					}

				}
				else if (this.indicatedEnemy)
				{
					pushPos = this.indicatedEnemy.getCenterPosition();
				}
			}

			if (this._isWeaponHavePushEffectInOwnShot(lCurrentWeaponId_int))
			{
				this.addPushEffect(pushPos.x, pushPos.y, this.spot);
			}
			this.rotateGun(pushPos.x, pushPos.y, true);
			lTargetPoint_pt = pushPos;
		}

		this.decreaseAmmo(lShotAmmoAmount_int, lIsPaidSWShot_bl);

		if (!isRicochet)
		{
			this.onShotRequest();
			this.emit(
				'fire',
				{
					id: enemy.id,
					weaponId: lCurrentWeaponId_int,
					x: lTargetPoint_pt.x,
					y: lTargetPoint_pt.y,
					isPaidSWShot: lIsPaidSWShot_bl,
					weaponPrice:  this._calcShotWeaponPrice(lCurrentWeaponId_int)
				});
		}
		else
		{
			//console.log("FireController: ricochetFire");
			this.ricochetFire(this._getPointOutsideWeaponRadius(this.lastPointerPos), this.seatId);
		}

		this.changeIndicator(e);
	}

	_isWeaponHavePushEffectInOwnShot(aWeaponId_int) //no push effect for FlameThrower && Laser - it will be a part of a Shot
	{
		return	!(aWeaponId_int === WEAPONS.RICOCHET ||
				aWeaponId_int === WEAPONS.FLAMETHROWER ||
				aWeaponId_int === WEAPONS.RICOCHET_POWER_UP ||
				aWeaponId_int === WEAPONS.FLAMETHROWER_POWER_UP ||
				aWeaponId_int === WEAPONS.INSTAKILL ||
				aWeaponId_int === WEAPONS.INSTAKILL_POWER_UP)
	}

	_getPointOutsideWeaponRadius(point)
	{
		let lSpot_ps = this.getSeat(this.seatId, true);
		let centerPoint = lSpot_ps.globalGunCenter;
		let gunRadius = lSpot_ps.weaponSpotView.gun.muzzleTipOffset;
		let gunPos = lSpot_ps.muzzleTipGlobalPoint;
		gunRadius -= lSpot_ps.weaponSpotView.gun.y;

		let distance = Utils.getDistance(point, centerPoint);

		if (distance <= gunRadius)
		{
			let moveDistX = gunPos.x - point.x;
			let moveDistY = gunPos.y - point.y;

			let newPoint = {x: point.x + moveDistX*2, y: point.y + moveDistY*2};
			return newPoint;
		}

		return point;
	}

	_onCollisionOccurred(aBullet_rb, aEnemy_e)
	{
		aEnemy_e.showHitBounce(aBullet_rb.directionAngle, aBullet_rb.weaponId);
		aEnemy_e.playHitHighlightAnimation(5 * FRAME_RATE);

		let lSeat_pt = this.getSeat(aBullet_rb.seatId, true);
		let lSpotCurrentDefaultWeaponId_int = lSeat_pt ? lSeat_pt.currentDefaultWeaponId : 1;

		lSeat_pt && this.showMissEffect(
			aBullet_rb.position.x,
			aBullet_rb.position.y,
			aBullet_rb.weaponId,
			aEnemy_e,
			lSpotCurrentDefaultWeaponId_int,
			lSeat_pt.isMaster);
	}


	onRicochetBulletShotOccurred(aBullet_rb, aEnemy_e)
	{
		this.onShotRequest();
		this.emit(
			'fire',
			{
				id: aEnemy_e.id,
				weaponId: aBullet_rb.weaponId,
				x: aBullet_rb.position.x,
				y: aBullet_rb.position.y,
				isPaidSWShot: false,
				bulletId: aBullet_rb.bulletId,
				weaponPrice: this._calcShotWeaponPrice(aBullet_rb.weaponId)
			});
	}

	ricochetFire(distanationPos, seatId, optResponseBulletId_str = null, optResponseWeaponId_int = null, optStartTime = null, optStartPos = null, lasthand = false)
	{
		if(APP.isCAFMode && !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			return;
		}

		let lSpot_ps = this.getSeat(seatId, true);
		if (!lSpot_ps)
		{
			APP.logger.i_pushWarning(`GameField. Cannot show ricochet fire, no target seat detected, seatId: ${seatId}.`);
			console.log(`Cannot show ricochet fire, no target seat detected, seatId: ${seatId}`);
			return;
		}

		this.emit(GameField.EVENT_ON_BULLET_FLY_TIME);
		this.playWeaponSound(WEAPONS.DEFAULT, seatId !== this.seatId, lSpot_ps.currentDefaultWeaponId);

		let endPos = {x: Math.round(distanationPos.x), y: Math.round(distanationPos.y)};
		let startPos = optStartPos ? optStartPos : lSpot_ps.muzzleTipGlobalPoint;
		startPos = {x: Math.round(startPos.x), y: Math.round(startPos.y)};
		let angle = Math.atan2(endPos.x - startPos.x, endPos.y - startPos.y);
		let len = Math.sqrt(Math.pow(startPos.x - endPos.x, 2) + Math.pow(startPos.y - endPos.y, 2));

		this.rotatePlayerGun(seatId, endPos.x, endPos.y);
		this.emit(GameField.DEFAULT_GUN_SHOW_FIRE, {seat: seatId});

		if(seatId != this.seatId)
		{
			this._addDefaultGunFireEffect(lSpot_ps.muzzleTipGlobalPoint.x, lSpot_ps.muzzleTipGlobalPoint.y, angle + Math.PI/2, len, lSpot_ps.currentDefaultWeaponId, lSpot_ps);
		}
		else
		{
			this._addDefaultGunFireEffect(startPos.x, startPos.y, angle + Math.PI/2, len, lSpot_ps.currentDefaultWeaponId, lSpot_ps);
		}

		this.addPushEffect(endPos.x, endPos.y, lSpot_ps);

		let startTime = 0;
		if (optStartTime !== null)
		{
			startTime = optStartTime;
		}

		let lWeaponId_num = optResponseWeaponId_int !== null ? optResponseWeaponId_int : lSpot_ps.currentWeaponId;

		var bullet = new RicochetBullet({weaponId: lWeaponId_num, defaultWeaponId: lSpot_ps.currentDefaultWeaponId}, startPos, endPos, optResponseBulletId_str, startTime, lasthand);
		bullet.zIndex = Z_INDEXES.BULLET;
		bullet.once(RicochetBullet.EVENT_ON_RICOCHET_BULLET_DESTROY, this.emit, this);

		if (!optResponseBulletId_str)
		{
			this.emit(GameField.EVENT_ON_RICOCHET_BULLET_REGISTER, {bullet: bullet});
		}

		this.emit(GameField.EVENT_ON_RICOCHET_BULLET_FLY_OUT, {bullet: bullet});

		this.topScreen.addChild(bullet);
		this.bullets.push(bullet);

		this.updatePlayerBalance();
	}

	_onBulletResponse(aEvent_obj)
	{
		let data = aEvent_obj.data;
		let isMasterSeat = (data.rid !== -1);

		for( let i = 0; i < this.bullets.length; i++ )
		{
			if(this.bullets[i].bulletId === data.bulletId)
			{
				this.bullets[i].onBulletResponse();
			}
		}


		if (!isMasterSeat)
		{
			this._isNeedRotate_bl = false;
			let bulletId = data.bulletId;
			let seatId = +bulletId.slice(0, 1);
			let weaponId = data.weaponId;

			let startPos = {x: data.startPointX, y: data.startPointY};
			let distanationPos = {x: data.endPointX, y: data.endPointY};

			this.ricochetFire(distanationPos, seatId, bulletId, weaponId, data.bulletTime, startPos);
		}
	}

	_onBulletPlaceNotAllowed(aEvent_obj)
	{
		let requestData = aEvent_obj.requestData;
		APP.gameScreen.revertAmmoBack(requestData.weaponId, undefined, requestData.isPaidSpecialShot, requestData.isRoundNotStartedError);

		this.emit(GameField.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, {bulletId: requestData.bulletId});
	}

	_onBulletClearResponse(aEvent_obj)
	{
		let seatId = aEvent_obj.data.seatNumber;
		this.emit(GameField.EVENT_ON_CLEAR_BULLETS_BY_SEAT_ID, {seatId: seatId});
	}

	_isFireAllowed()
	{
		return !this._isWaitingResponseToBetLevelChangeRequest;
	}

	_checkIfAutoTargetSwitchNeeded()
	{
		if (this.autoTargetingEnemy)
		{
			let enemyId = this.autoTargetingEnemy.id;
			let enemyObj = APP.currentWindow.getExistEnemy(enemyId);
			let lEnemy_e = this.getExistEnemy(enemyId);
			if ((enemyObj && enemyObj.life === 0) || lEnemy_e.isFireDenied || lEnemy_e.invulnerable)
			{
				//select another target
				if (this._isAutoTargetingEnemyAvailableForFire)
				{
				this.emit(GameField.EVENT_ON_TARGET_ENEMY_IS_DEAD, {enemyId: enemyId});
			}
		}
	}
	}

	_onLobbyExternalMessageReceived(event)
	{
		switch (event.type)
		{
			case LOBBY_MESSAGES.KEYBOARD_BUTTON_CLICKED:
				this._chooseKeyboardAction(event.data.code);
			break;
			case LOBBY_MESSAGES.MID_ROUND_EXIT_DIALOG_ACTIVATED:
				this._fMidRoundExitDialogActive_bln = true;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.MID_ROUND_EXIT_DIALOG_DEACTIVATED:
				this._fMidRoundExitDialogActive_bln = false;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.BONUS_DIALOG_ACTIVATED:
				this._fBonusDialogActive_bln = true;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.BONUS_DIALOG_DEACTIVATED:
				this._fBonusDialogActive_bln = false;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.FRB_DIALOG_ACTIVATED:
				this._fFRBDialogActive_bln = true;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.FRB_DIALOG_DEACTIVATED:
				this._fFRBDialogActive_bln = false;
				this._checkBlur();
			break;
			case LOBBY_MESSAGES.SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED:
				if (event.data.backToLobby)
				{
					this.emit(GameField.EVENT_BACK_TO_LOBBY_ON_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED);
				}
			break;
		}
	}

	_onKeyboardButtonClicked(event)
	{
		this._chooseKeyboardAction(event.code);
	}

	_chooseKeyboardAction(code)
	{
		switch (code)
		{
			case "Space":
				if (!APP.keyboardControlProxy.isSpaceDown)
				{
					this.chooseFireType();
				}
			break;
			case "ArrowLeft":
			case "KeyA":
				if (!APP.keyboardControlProxy.isLeftDown)
				{
					this.turretRotationTime = 0;
					this._tryRotateTurretLeft();
				}
			break;
			case "ArrowRight":
			case "KeyD":
				if (!APP.keyboardControlProxy.isRightDown)
				{
					this.turretRotationTime = 0;
					this._tryRotateTurretRight();
				}
			break;
			case "ArrowUp":
			case "KeyW":
				if (!APP.keyboardControlProxy.isUpDown)
				{
					this.cursorKeysMoveTime = 0;
					this._tryMoveCursorUp();
				}
			break;
			case "ArrowDown":
			case "KeyS":
				if (!APP.keyboardControlProxy.isDownDown)
				{
					this.cursorKeysMoveTime = 0;
					this._tryMoveCursorDown();
				}
			break;
		}
	}

	_calcShotMultiplier()
	{
		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;
		let lPlayerInfo_pi = APP.playerController.info;

		let lShotMultiplier_int = this._isPaidSWShot ? lPlayerInfo_pi.getWeaponPaidCostMultiplier(lCurrentWeaponId_int) : 1;

		return lShotMultiplier_int;
	}

	_calcShotWeaponPrice(aWeaponId_int=undefined)
	{
		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		let lCurrentWeaponId_int = aWeaponId_int !== undefined ? aWeaponId_int : lWeaponsInfo_wsi.currentWeaponId;
		let lPlayerInfo_pi = APP.playerController.info;

		let lShotMultiplier_int = lPlayerInfo_pi.getWeaponPaidCostMultiplier(lCurrentWeaponId_int);

		return lShotMultiplier_int;
	}

	get _isPaidSWShot()
	{
		let lWeaponsInfo_wsi = this._fWeaponsInfo_wsi;
		let lCurrentWeaponId_int = lWeaponsInfo_wsi.currentWeaponId;

		let lIsPaidSWShot_bl = lCurrentWeaponId_int !== WEAPONS.DEFAULT
								&& lWeaponsInfo_wsi.remainingSWShots <= 0
								&& !lWeaponsInfo_wsi.isFreeWeaponsQueueActivated;
		return lIsPaidSWShot_bl;
	}

	pushPointer(e)
	{
		this.emit(GameField.EVENT_ON_START_UPDATE_CURSOR_POSITION);

		if (APP.currentWindow.bigWinsController.isAnyBigWinAnimationInProgress)
		{
			this._tryToSkipBigWin();
			return; //prevent firing while any Big Win animations are playing
		}

		if (this._fAutoTargetingSwitcherInfo_atsi.isOn && APP.isAutoFireMode) return;

		this._resetTargetIfRequired();

		this.pointerPushed = true;

		var interaction = APP.stage.renderer.plugins.interaction;

		if (
				APP.isMobile
				|| (
						interaction.mouse.global.y > MIN_SHOT_Y
						&& interaction.mouse.global.y < MAX_SHOT_Y
					)
			)
		{
			this.tryRotateGun(e);
			this.changeIndicator(e);

			if(this._fAutoTargetingSwitcherInfo_atsi.isOn)
			{
				setTimeout(()=>{
					if(!this._fTargetingInfo_tc.targetEnemyId)
					{
						this.chooseFireType(e);
					}else{
						console.log("target defined ignore shooting");
					}
				},100);
			}else{
				this.chooseFireType(e);
			}
		}
	}

	chooseFireType(e)
	{
		if(APP.isCAFMode && !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			return;
		}

		if (!this.isRicochetAllowed)
		{
			if (e)
			{
				this.fire(e);
			}
			else
			{
				this._isAutoTargetingEnemyAvailableForFire && this.fireImmediately();
			}
		}
		else
		{
			if (this.isRicochetLimitReached)
			{
				let limit = this._ricochetController ? this._ricochetController.info.bulletsLimit : undefined;
				console.log(`Ricochet bullets has reached their limit: ${limit}, shot is refused.`);
			}
			else
			{
				let data = data || {data: {global:{x: this.lastPointerPos.x, y: this.lastPointerPos.y}}}
				this.fire(data, true);
			}
		}
	}

	get _isAutoTargetingEnemyAvailableForFire()
	{
		if(
			this.autoTargetingEnemy &&
			!this.autoTargetingEnemy.isTargetable()
			)
		{
			return false;
		}
			return true;
		}

	get isRicochetAllowed()
	{
		let lCurWeaponId_num = this._fWeaponsInfo_wsi ? this._fWeaponsInfo_wsi.currentWeaponId : undefined;
		let isAutotargetActive = this._fTargetingInfo_tc.isActive;

		return this.isRicochetWeapon(lCurWeaponId_num) && !isAutotargetActive;
	}

	isRicochetWeapon(aWeaponId)
	{
		return RICOCHET_WEAPONS.indexOf(aWeaponId) >= 0;
	}

	isRicochetSW(aWeaponId)
	{
		return this.isRicochetWeapon(aWeaponId) && aWeaponId !== WEAPONS.DEFAULT;
	}

	get isRicochetLimitReached()
	{
		let isRicochetsLimitReached = (this._ricochetController && this._ricochetController.info.isMasterBulletsLimitReached);

		return isRicochetsLimitReached;
	}

	get isPointerPushed()
	{
		return this.pointerPushed;
	}

	_tryToSkipBigWin()
	{
		this.emit(GameField.EVENT_ON_TRY_TO_SKIP_BIG_WIN);
	}

	unpushPointer()
	{
		this.emit(GameField.EVENT_ON_START_UPDATE_CURSOR_POSITION);

		this.pointerPushed = false;

		this._checkWeaponRecoilOffsetAfterEndShooting();
	}

	onPointerRightClick(e)
	{
		if (APP.currentWindow.bigWinsController.isAnyBigWinAnimationInProgress)
		{
			this._tryToSkipBigWin();
		}

		if (!this._fireSettingsInfo.lockOnTarget) return;

		if(!APP.isBattlegroundGame)
		{
			this.unpushPointer();
			this.emit(GameField.EVENT_ON_RESET_TARGET);
		}

		for( let i = 0; i < this.enemies.length; i++ )
		{
			if(this.enemies[i].onPointerRightClick(e.data.global.x, e.data.global.y))
			{
				if(APP.isBattlegroundGame)
				{
					this.unpushPointer();
					this.emit(GameField.EVENT_ON_RESET_TARGET);
				}

				this._fTargetingInfo_tc.targetEnemyId = this.enemies[i].id;
				this._onEnemyRightClick(this.enemies[i]);

				if (this._fireSettingsInfo.lockOnTarget)
				{
					this._validateAutofireButton(true, this.enemies[i]);
				}

				return;
			}
		}

		!APP.isBattlegroundGame && this._validateAutofireButton(false);
	}

	onPointerClick(e)
	{
		if (
			this._fAutoTargetingSwitcherInfo_atsi.isOn ||
			(
				this._fTargetingInfo_tc.isActive
				&& this._fireSettingsInfo.autoFire
				&& !(e.target instanceof WeaponSidebarIcon)
			)
		)
		{
			if(!APP.isBattlegroundGame) this._resetTargetIfRequired(true);

			for( let i = 0; i < this.enemies.length; i++ )
			{
				if(this.enemies[i].onPointerClick(e.data.global.x, e.data.global.y))
				{
					if(APP.isBattlegroundGame) this._resetTargetIfRequired(true);
					this._fTargetingInfo_tc.targetEnemyId = this.enemies[i].id;
					this._onEnemyClick(this.enemies[i]);

					if (this._fireSettingsInfo.lockOnTarget)
					{
						this._validateAutofireButton(true, this.enemies[i]);
					}

					return;
				}
			}

			!APP.isBattlegroundGame && this._validateAutofireButton(false);
		}
	}

	_onFireSettingsChanged()
	{
		if (!this._fireSettingsInfo.lockOnTarget)
		{
			this._resetTargetIfRequired(true);
			this._validateAutofireButton(false, null, true);
		}
	}

	_onFireSettingsActivated()
	{
		this.unpushPointer();
		this._fFireSettingsScreenActive_bln = true;

		this._validateInteractivity();
	}

	_onFireSettingsDeactivated()
	{
		this._fFireSettingsScreenActive_bln = false;

		this._validateInteractivity();
	}

	_resetTargetIfRequired(aAnyway_bl = false)
	{
		if (aAnyway_bl)
		{
			this.unpushPointer();
			this.emit(GameField.EVENT_ON_RESET_TARGET);
		}
	}

	_onEnemyRightClick(aEnemy_e)
	{
		//DEBUG...
		console.log("RIGHT CLICK ON ENEMY ID = " + aEnemy_e.id);
		//...DEBUG

		this._tryUpdateTargetAndFire(aEnemy_e);
	}

	_onEnemyClick(aEnemy_e)
	{
		let lId_int = aEnemy_e.id;

		if (this._fTargetingInfo_tc.isActive)
		{
			this._tryUpdateTargetAndFire(aEnemy_e);
		}
		else if (this._fAutoTargetingSwitcherInfo_atsi.isOn && this._fireSettingsInfo.lockOnTarget)
		{
			const lTargetEnemyId_int = lId_int;
			this.emit(GameField.EVENT_ON_TARGETING, {targetEnemyId: lTargetEnemyId_int});
			if (this._fireSettingsInfo.autoFire && !this._fTargetingInfo_tc.isPaused)
			{
				this.chooseFireType();
			}
			return;
		}
		this.unpushPointer();
	}

	_tryUpdateTargetAndFire(aEnemy_e)
	{
		// DEBUG... stub update trajectory
		// this.emit("stubUpdateTrajectory", {enemyId: e.enemyId});
		// return;
		// ...DEBUG
		let lId_int = aEnemy_e.id;

		if (!this._fireSettingsInfo.lockOnTarget || aEnemy_e.isEnemyLockedForTarget) return;

		this.unpushPointer();
		this.emit(GameField.EVENT_ON_TARGETING, {targetEnemyId: lId_int});
		if (this._fireSettingsInfo.autoFire && !this._fTargetingInfo_tc.isPaused)
		{
			this.fireImmediately();
			this._validateAutofireButton(true);
		}
	}

	onChooseWeaponsStateChanged(aVal_bln)
	{
		this._fChooseWeaponsScreenActive_bln = aVal_bln;
	}

	_validateAutofireButton(aState_bl, aEnemy_e, aOptIsQuietMode_bl=false)
	{
		if (APP.isBattlegroundGame && this.spot && this.spot.autofireButton.enabled != aState_bl && APP.isAutoFireEnabled)
		{
			this._fAutofireButtonEnabled_bl = aState_bl;
			this.spot.autofireButton.i_setEnable(aState_bl, aEnemy_e, aOptIsQuietMode_bl);
		}
	}

	_tryRotateTurretLeft()
	{
		this._tryRotateTurretOnAngle(1);
	}

	_tryRotateTurretRight()
	{
		this._tryRotateTurretOnAngle(-1);
	}

	_tryRotateTurretOnAngle(aDirect_num)
	{
		if (this.seatId === null || this.seatId === undefined) return;
		if (!APP.gameScreen.cursorController.isCursorRendering) return;

		let seatPosition = SEATS_POSITION_IDS[this.seatId];
		if (seatPosition > 2) aDirect_num *= -1;

		let gunPos = this.getGunPosition(this.seatId);
		let lastPointerPos = this.lastPointerPos;

		let angle = Utils.getAngle(gunPos, lastPointerPos);
		let dist = Utils.getDistance(gunPos, lastPointerPos);
		let distMax = 680;
		let coef = 1 - dist / distMax;
		if (coef > 1) coef = 1;
		if (coef < 0.3) coef = 0.3;
		let stepAngle = coef * TURRET_TURN_UNIT;

		let newAngle = (angle + aDirect_num * stepAngle - Math.PI/2);
		let newPosition = {x: gunPos.x + Math.cos(newAngle)*dist, y: gunPos.y - Math.sin(newAngle)*dist};

		let e = {data: {global: newPosition}};
		let isRotated = this.tryRotateGun(e);

		if (isRotated)
		{
			this.emit(GameField.EVENT_ON_STOP_UPDATE_CURSOR_POSITION);
			this.emit(GameField.EVENT_ON_SET_SPECIFIC_CURSOR_POSITION, {pos: newPosition});
		}
	}

	_tryMoveCursorUp()
	{
		this._tryMoveCursor(1);
	}

	_tryMoveCursorDown()
	{
		this._tryMoveCursor(-1);
	}

	_tryMoveCursor(aDirect_num)
	{
		if (this.seatId === null || this.seatId === undefined) return;
		if (!APP.gameScreen.cursorController.isCursorRendering) return;

		let seatPosition = SEATS_POSITION_IDS[this.seatId];
		if (seatPosition > 2) aDirect_num *= -1;

		let gunPos = this.getGunPosition(this.seatId);
		let lastPointerPos = this.lastPointerPos;

		let angle = Utils.getAngle(gunPos, lastPointerPos);
		let dist = Utils.getDistance(gunPos, lastPointerPos);

		let newDist = dist + aDirect_num * CURSOR_MOVE_UNIT;
		if (newDist < 60) newDist = 60;
		let newPosition = {x: gunPos.x + Math.cos(angle - Math.PI/2)*newDist, y: gunPos.y - Math.sin(angle - Math.PI/2)*newDist};

		let e = {data: {global: newPosition}};
		this.tryRotateGun(e);

		
		this.emit(GameField.EVENT_ON_STOP_UPDATE_CURSOR_POSITION);
		this.emit(GameField.EVENT_ON_SET_SPECIFIC_CURSOR_POSITION, {pos: newPosition});
		
	}

	tryRotateGun(e)
	{
		if (	!this.spot ||
				this._fTargetingInfo_tc.isActive ||
				this._fChooseWeaponsScreenActive_bln ||
				this.roundResultActive 
		)
		{
			return false;
		}

		var x = e.data.global.x;
	
		var y = e.data.global.y;



		let lKeyBoardRotation = e.data.isKeyboardRotation ? e.data.isKeyboardRotation : false;

		let lAPPSize_obj = APP.config.size;
		const margin  = 10;
		
		if(x>lAPPSize_obj.width -margin){
			x = lAPPSize_obj.width - margin;
		}

		if(x<margin){
			x = margin;
		}

		if(y>lAPPSize_obj.height - margin){
			y= lAPPSize_obj.height - margin;
		}

		if(y<margin){
			y= margin;
		}

		let A = this.spot.isBottom ? new PIXI.Point(0, lAPPSize_obj.height) : new PIXI.Point(0, 0); //left corner
		let B = this.spot.isBottom ? new PIXI.Point(lAPPSize_obj.width, lAPPSize_obj.height) : new PIXI.Point(lAPPSize_obj.width, 0); //right corner
		let C = this.getGunPosition(this.seatId); //spot point
		let D = new PIXI.Point(x, y); //cursor point
        let isRotationLimit = this._f(B,C,A,D) && this._f(C,A,B,D);

		if (isRotationLimit)
		{
			if (x > C.x && !lKeyBoardRotation)
			{
				this.rotateGun(B.x, B.y);
				this.lastPointerPos = B;
			}
			else if (!lKeyBoardRotation)
			{
				this.rotateGun(A.x, A.y);
				this.lastPointerPos = A;
			}
		}

		this.lastPointerPos = D;
		
		this.rotateGun(x, y);

		return true;
	}

	_g(a, b, d)
	{
		return (d.x - a.x) * (b.y - a.y) - (d.y - a.y) * (b.x - a.x);
	}

	_f(a, b, c, d)
	{
		return this._g(a, b, c) * this._g(a, b, d) >= 0;
	}

	changeIndicator(e)
	{
		if (this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY || !this.spot || this.gunLocked) return;

		let x = e.data.global.x;
		let y = e.data.global.y;

		let enemy = this.getNearestEnemy(x,y),
			dist = 100;
		let len = (enemy) ? Math.sqrt(Math.pow(enemy.x - x, 2) + Math.pow(enemy.y - y, 2)) : Infinity;

		if (!enemy || len >= dist)
		{
			this.indicatedEnemy = null;
		}
		else
		{
			if (enemy && enemy.life !== 0 &&
				((this.indicatedEnemy && this.indicatedEnemy != enemy) || !this.indicatedEnemy))
			{
				this.indicatedEnemy = enemy;
			}
		}
	}

	/*
	** /aOptForcedSelectionType_int:
	** 0 - no forced selection type, use by weapon type,
	** 1 - forced selection nearest toward point (x, y),
	** 2 - forced selection nearest to point (x, y)
	*/
	getNearestEnemy(x,y, aOptForcedSelectionType_int = 0, aOptFilteredEnemyIds_int_arr = null)
	{
		if (!this.enemies || !this.enemies.length || this.seatId < 0 || !this.spot)
		{
			//console.log("[GameField] getNearestEnemy: can't detect nearest enemy, no enemies on the screen.");
			return null;
		}

		let activeEnemiesPositions = [];
		for (let i = 0; i < this.enemies.length; i ++)
		{
			var enemy = this.enemies[i];

			if (aOptFilteredEnemyIds_int_arr && aOptFilteredEnemyIds_int_arr.indexOf(enemy.id) < 0)
			{
				continue;
			}

			if (enemy.life === 0) continue; //don't select killed enemies - the enemy might be dead on the server, but still walking on client, untill weapon killing animation is completed
			if ((enemy.isBoss) && enemy.isFireDenied) continue;
			if (enemy.isFireDenied) continue;
			if (enemy.invulnerable) continue;
			if (enemy.isDestroyed) continue; //[Y]TODO to figure out why the destroyed enemy wasn't removed from the array

			var enemyPos = enemy.getCenterPosition();
			activeEnemiesPositions.push({enemyPos: enemyPos, enemyIndex:i});
		}

		let nearestEnemyIndex = -1;
		if (aOptForcedSelectionType_int === 0)
		{
			nearestEnemyIndex = this._isGunTowardPointerEnemySelectionTypeAvailable(this._fWeaponsInfo_wsi.currentWeaponId)
									? this._getNearestTowardPointerEnemyIndex(new PIXI.Point(x, y), activeEnemiesPositions)
									: this._getNearestToPointerEnemyIndex(new PIXI.Point(x, y), activeEnemiesPositions);
		}
		else if (aOptForcedSelectionType_int === 1)
		{
			nearestEnemyIndex = this._getNearestTowardPointerEnemyIndex(new PIXI.Point(x, y), activeEnemiesPositions);
		}
		else if (aOptForcedSelectionType_int === 2)
		{
			nearestEnemyIndex = this._getNearestToPointerEnemyIndex(new PIXI.Point(x, y), activeEnemiesPositions);
		}

		return nearestEnemyIndex < 0 ? null : this.enemies[nearestEnemyIndex];
	}

	_isGunTowardPointerEnemySelectionTypeAvailable(weaponId)
	{
		if (weaponId === WEAPONS.DEFAULT) //|| weaponId === WEAPONS.RICOCHET
		{
			return true;
		}

		return false;
	}

	_getNearestTowardPointerEnemyIndex(pointerPosition, activeEnemiesPositions)
	{
		let gunPos = this.getGunPosition(this.seatId);
		var angles = [];
		var minAngle = 180;
		var minUnderPadAngle = 180;
		
		for (let i = 0; i < activeEnemiesPositions.length; i ++)
		{
			var enemyPosDescr = activeEnemiesPositions[i];
			var enemyPos = enemyPosDescr.enemyPos;
			var enemyIndex = enemyPosDescr.enemyIndex;
			let lIsUnderPadEnemy_bl = false;

			if (
				enemyPos.x > spotLeft &&
				enemyPos.x < spotRight &&
				enemyPos.y > spotTop &&
				enemyPos.y < spotBottom
			)
			{
				lIsUnderPadEnemy_bl = true;
			}

			let pointA = {x: pointerPosition.x, y: pointerPosition.y};
			let pointB = {x: gunPos.x, y: gunPos.y};
			let pointC = {x: enemyPos.x, y: enemyPos.y};

			let cosABC = Utils.cosABC(pointA, pointB, pointC);
			cosABC = Math.min(cosABC, 1);
			cosABC = Math.max(cosABC, -1);
			let angleABCInRad = Math.acos(cosABC);
			let angleABC = Utils.radToGrad(angleABCInRad)
			if (lIsUnderPadEnemy_bl && angleABC < minUnderPadAngle)
			{
				minUnderPadAngle = angleABC;
			}
			if (angleABC < minAngle && !lIsUnderPadEnemy_bl)
			{
				minAngle = angleABC;
			}

			angles.push({angle:angleABC, enemyPos:enemyPos, index: enemyIndex, isUnderPadEnemy: lIsUnderPadEnemy_bl});
		}

		if (angles.length == 0) return -1;

		var minDist = -1;
		var min = -1;
		let sortAngle = Math.max(10, minAngle);
		let edgeDist = 50; //50px to the edge of the standard weapon length
		let spotBounds = this.spot.getBounds();
		let spotLeft = this.spot.position.x - spotBounds.width/2;
		let spotRight = this.spot.position.x + spotBounds.width/2;
		let spotTop = this.spot.position.y - spotBounds.height/2;
		let spotBottom = this.spot.position.y + spotBounds.height/2;
		let underPadEnemies = [];
		for (let i = 0; i < angles.length; i ++)
		{
			let lEnemyAngleDescr = angles[i];
			
			if (lEnemyAngleDescr.angle > sortAngle) continue;

			if (lEnemyAngleDescr.isUnderPadEnemy)
			{
				if (lEnemyAngleDescr.angle <= minUnderPadAngle)
				{
					underPadEnemies.unshift(lEnemyAngleDescr);
				}
				else
				{
					underPadEnemies.push(lEnemyAngleDescr);
				}
				continue;
			}

			var curDistance = Math.sqrt(Math.pow(lEnemyAngleDescr.enemyPos.x - gunPos.x, 2) + Math.pow(lEnemyAngleDescr.enemyPos.y - gunPos.y, 2));
			if ((minDist == -1 || curDistance < minDist) && curDistance > edgeDist)
			{
				minDist = curDistance;
				min = i;
			}
		}

		if (min < 0)
		{
			if (!!underPadEnemies && !!underPadEnemies.length)
			{
				return underPadEnemies[0].index;
			}
			
			return angles[0].index;
		}

		return angles[min].index;
	}

	_getNearestToPointerEnemyIndex(pointerPosition, activeEnemiesPositions)
	{
		var distances = [];
		for (let i = 0; i < activeEnemiesPositions.length; i ++)
		{
			var enemyPosDescr = activeEnemiesPositions[i];
			var enemyPos = enemyPosDescr.enemyPos;
			var enemyIndex = enemyPosDescr.enemyIndex;

			var  len = Math.sqrt(Math.pow(enemyPos.x - pointerPosition.x, 2) + Math.pow(enemyPos.y - pointerPosition.y, 2));
			distances.push({len:len, index: enemyIndex});

		}

		if (distances.length == 0) return -1;

		var  min = 0;
		for (let i = 1; i < distances.length; i ++)
		{
			if (distances[i].len < distances[min].len)
			{
				min = i;
			}
		}

		return distances[min].index;
	}

	rotateGun(x, y, aOptFire = false)
	{
		if (!this.spot) return;
		if (!this.spot.weaponSpotView) return;
		if (this.spot.rotationBlocked && !aOptFire)
		{
			return;
		}

		let weaponSpotView = this.spot.weaponSpotView;
		let gunX = weaponSpotView.localToGlobal().x;
		let gunY = weaponSpotView.localToGlobal().y;
		let angle = Math.atan2(y - gunY, x - gunX) + Math.PI/2; //(x - gunX)/(y - gunY);

		if (!this.spot.isBottom)
		{
			angle += Math.PI;
		}

		this.spot.weaponSpotView.rotation = angle;
		return angle;
	}

	rotatePlayerGun(seatId, x, y)
	{
		if (seatId == this.seatId)
		{
			return this.rotateGun(x, y);
		}

		let player = this.getPlayerBySeatId(seatId);

		if (!player || !player.weaponSpotView)
		{
			return null;
		}

		let weaponSpotView = player.weaponSpotView;
		let gunX = weaponSpotView.localToGlobal().x;
		let gunY = weaponSpotView.localToGlobal().y;
		let angle = Math.atan2(y - gunY, x - gunX) + Math.PI/2;//(x - gunX)/(y - gunY);

		if (!player.spot.isBottom)
		{
			angle += Math.PI;
		}

		weaponSpotView.rotation = angle;

		return angle;
	}

	addPushEffect(x, y, seat)
	{
		let target = seat.weaponSpotView;
		let gun = seat.weaponSpotView.gun;

		this._fPushWeaponEffectPlaying_bl = true;
		this._fCurrentWeaponSpotGun = gun;

		var gunX = seat.gunCenter.x, gunY = seat.gunCenter.y;
		gunX = seat.localToGlobal(gunX, gunY).x;
		gunY = seat.localToGlobal(gunX, gunY).y;
		if (!target.pushSequence)
		{
			target.startPosX = target.x;
			target.startPosY = target.y;
		}
		else
		{
			target.pushSequence.stop();
			target.pushSequence.destructor();
			target.pushSequence = null;

			target.x = target.startPosX;
			target.y = target.startPosY;
		}

		gun.showShotlight();

		var endX = target.x;
		var endY = target.y;

		var dist = 8;
		let durations = [40, 0, 60, 5];
		switch (gun.id)
		{
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				durations = [200, 0, 60, 0];
				dist = 80;
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				durations = [2*2*16.7, 0, 4*2*16.7, 0];
				dist = 40;
				break;
			case WEAPONS.RICOCHET: // not sure
			case WEAPONS.RICOCHET_POWER_UP: // not sure
				durations = [2*2*16.7, 0, 2*2*16.7];
				dist = 30;
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				durations = [2*2*16.7, 26*2*16.7, 2*2*16.7, 0];
				dist = 25;
				break;
			default:
				break;
		}

		dist *= PlayerSpot.WEAPON_SCALE;

		let ang = -Math.PI/2;//Math.atan2(y - gunY, x - gunX);
		/*if (!target.isBottom)
		{
			ang += Math.PI;
		}*/

		let dx = dist*Math.cos(ang);
		let dy = dist*Math.sin(ang);

		var pushX = endX - dx;
		var pushY = endY - dy;

		let sequence = [
			{
				tweens: [
					{prop: "x", to: pushX},
					{prop: "y", to: pushY}
				],
				duration: durations[0],
				ease: Easing.quadratic.easeInOut
			},
			{
				tweens: [],
				duration: durations[1]
			},
			{
				tweens: [
					{prop: "x", to: endX},
					{prop: "y", to: endY}
				],
				duration: durations[2],
				ease: Easing.quadratic.easeInOut
			},
			{
				tweens: [],
				duration: durations[3],
				onfinish: () => {
					gun.hideShotlight();

					if (!this.pointerPushed && !this.isAutoFireEnabled)
					{
						gun.y = 0;
						this._fWeaponOffsetYFiringContinuously_int = 0;
					}

					this._fPushWeaponEffectPlaying_bl = false;
				}
			}
		];

		if (gun.id === WEAPONS.CRYOGUN || gun.id === WEAPONS.CRYOGUN_POWER_UP)
		{
			let additionalSequence = [
				{
					tweens: [
						{prop: "x", to: pushX + Math.cos(ang - Math.PI/7)*7},
						{prop: "y", to: pushY + Math.sin(ang - Math.PI/7)*7}
					],
					duration: 2*2*16.7
				},
				{
					tweens: [
						{prop: "x", to: pushX + Math.cos(ang + Math.PI/6)*7},
						{prop: "y", to: pushY + Math.sin(ang + Math.PI/6)*7}
					],
					duration: 3*2*16.7
				},
				{
					tweens: [
						{prop: "x", to: pushX + Math.cos(ang - Math.PI/5)*7},
						{prop: "y", to: pushY + Math.sin(ang - Math.PI/5)*7}
					],
					duration: 2*2*16.7
				},
				{
					tweens: [
						{prop: "x", to: pushX - Math.cos(ang)*10},
						{prop: "y", to: pushY - Math.sin(ang)*10}
					],
					duration: 2*2*16.7
				}
			];
			sequence = [sequence[0], ...additionalSequence, sequence[2]];
		}

		if (gun.id === WEAPONS.FLAMETHROWER || gun.id === WEAPONS.FLAMETHROWER_POWER_UP)
		{
			let addFlameThrowerStep = {
				tweens: [
					{prop: "x", to: pushX - Math.cos(ang)*4},
					{prop: "y", to: pushY - Math.sin(ang)*4}
				],
				duration: 2*2*16.7
			}
			sequence = [sequence[0], sequence[1], addFlameThrowerStep, sequence[2]];
		}

		target.pushSequence = Sequence.start(gun, sequence);
	}

	_checkWeaponRecoilOffsetAfterEndShooting()
	{
		if (!this._fPushWeaponEffectPlaying_bl
			&& this._fWeaponOffsetYFiringContinuously_int > 0
			&& this._fCurrentWeaponSpotGun.id === WEAPONS.DEFAULT)
		{
			this._fCurrentWeaponSpotGun.y = 0;
		}
	}

	_addDefaultGunFireEffect(x, y, angle, len, aOptSpotCurrentDefaultWeaponId_int = 1, aOptSpot_s)
	{
		if (aOptSpot_s && APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let lSpotCurrentDefaultWeaponId_int = aOptSpotCurrentDefaultWeaponId_int;
			let fireEffect = new DefaultGunFireEffect(lSpotCurrentDefaultWeaponId_int, this._fWeaponOffsetYFiringContinuously_int, aOptSpot_s.isMaster);

			let scale = 2;
			let hitboomScaleX = 1;
			let hitboomScaleY = 1;

			let lWeaponMultiplierValue_num;

			switch (lSpotCurrentDefaultWeaponId_int)
			{
				case 1:
					hitboomScaleX = 1.05;
					hitboomScaleY = 1.09;
					lWeaponMultiplierValue_num = 90;
					break;
				case 2:
					hitboomScaleX = 1.5;
					hitboomScaleY = 1.5;
					lWeaponMultiplierValue_num = 91;
					break;
				case 3:
					hitboomScaleX = 1.5;
					hitboomScaleY = 1.5;
					lWeaponMultiplierValue_num = 88;
					break;
				case 4:
					hitboomScaleX = 2.54;
					hitboomScaleY = 2.54;
					lWeaponMultiplierValue_num = 85;
					break;
				case 5:
					hitboomScaleX = 2.23;
					hitboomScaleY = 2.23;
					lWeaponMultiplierValue_num = 88;
					break;
				default:
					break;
			}

			let lPosMultiplier_num = lWeaponMultiplierValue_num * PlayerSpot.WEAPON_SCALE;

			// fireEffect.blendMode = PIXI.BLEND_MODES.ADD;
			fireEffect.rotation = - angle - Math.PI / 2;

			let dx = - Math.cos(angle) * lPosMultiplier_num * scale * hitboomScaleX;
			let dy = - Math.sin(angle) * lPosMultiplier_num * scale * hitboomScaleY;

			fireEffect.scale.set(hitboomScaleX * PlayerSpot.WEAPON_SCALE, hitboomScaleY * PlayerSpot.WEAPON_SCALE);
			fireEffect.position.set(x + dx, y - dy);

			fireEffect.once(DefaultGunFireEffect.EVENT_ON_ANIMATION_COMPLETED, this._onDefaultGunAnimationCompleted, this);
			fireEffect.once(Sprite.EVENT_ON_DESTROYING, this._onDefaultGunAnimationDestroying, this);

			this.topScreen.addChild(fireEffect);
			fireEffect.zIndex = Z_INDEXES.GUN_FIRE_EFFECT;

			this._defaultGunFireEffects_sprt_arr.push(fireEffect);
		}
	}

	_onDefaultGunAnimationCompleted(event)
	{
		this._destroyDefaultGunFireEffect(event.target);
	}

	_onDefaultGunAnimationDestroying(event)
	{
		if (!this._defaultGunFireEffects_sprt_arr || !this._defaultGunFireEffects_sprt_arr.length)
		{
			return;
		}

		let fireEffect = event.target;

		let lIndex_int = this._defaultGunFireEffects_sprt_arr.indexOf(fireEffect);
		if (~lIndex_int)
		{
			this._defaultGunFireEffects_sprt_arr.splice(lIndex_int, 1);
		}
	}

	_destroyDefaultGunFireEffect(fireEffect)
	{
		if (!fireEffect)
		{
			return;
		}

		let lIndex_int = this._defaultGunFireEffects_sprt_arr.indexOf(fireEffect);
		if (~lIndex_int)
		{
			this._defaultGunFireEffects_sprt_arr.splice(lIndex_int, 1);
		}

		fireEffect.off(DefaultGunFireEffect.EVENT_ON_ANIMATION_COMPLETED, this._onDefaultGunAnimationCompleted, this, true);
		fireEffect.destroy();
	}

	_removeAllDefaultGunFireEffects()
	{
		if (!this._defaultGunFireEffects_sprt_arr)
		{
			return;
		}

		while (this._defaultGunFireEffects_sprt_arr.length)
		{
			let fireEffect = this._defaultGunFireEffects_sprt_arr.pop();
			fireEffect.off(DefaultGunFireEffect.EVENT_ON_ANIMATION_COMPLETED, this._onDefaultGunAnimationCompleted, this, true);
			fireEffect.destroy();
		}
		this._defaultGunFireEffects_sprt_arr = [];
	}

	showAwards(scores_arr)
	{
		for (let i=0; i<scores_arr.length; i++)
		{
			let scoreData = scores_arr[i];

			let qualifyWin = scoreData.qualifyWin;
			if (qualifyWin > 0)
			{
				this.updatePlayerWin(qualifyWin/*, countTime*/);
			}
		}
	}

	showMiss(data, aIsPaidSpecialShot_bl)
	{
		if (data.rid != -1 && (!data.bulletId || this.isRicochetSW(data.usedSpecialWeapon)))
		{
			this.updateCurrentWeapon(aIsPaidSpecialShot_bl);
		}

		if (data.killedMiss || data.invulnerable)
		{
			//do not show animation of bullet flying nowhere
			return;
		}

		let missData = Object.assign({}, data);

		for (let obj of ShotResultsUtil.excludeFakeEnemies(missData.affectedEnemies))
		{
			if (
					this._isAnyCashAwardAnimationRequired(obj.data)
					|| this._isMasterScoreAwardRequired(obj.data)
				)
			{
				obj.data.rid = data.rid;
				this.emit(GameField.EVENT_ON_HIT_AWARD_EXPECTED, {hitData:obj.data, masterSeatId: this.seatId});
			}
		}

		this.showFire(missData, (e, endPos, angle) => this.proceedFireResult(e, endPos, angle, missData));
	}

	showHit(data, aIsPaidSpecialShot_bl)
	{
		if (data.rid != -1 && (!data.bulletId || this.isRicochetSW(data.usedSpecialWeapon)))
		{
			this.updateCurrentWeapon(aIsPaidSpecialShot_bl);
		}

		let hitData = Object.assign({}, data);

		for (let obj of ShotResultsUtil.excludeFakeEnemies(hitData.affectedEnemies))
		{
			if (
					this._isAnyCashAwardAnimationRequired(obj.data)
					|| this._isMasterScoreAwardRequired(obj.data)
				)
			{
				obj.data.rid = data.rid;
				this.emit(GameField.EVENT_ON_HIT_AWARD_EXPECTED, {hitData:obj.data, masterSeatId: this.seatId});
			}
		}

		this.showFire(hitData, (e, endPos, angle) => this.proceedFireResult(e, endPos, angle, hitData));
	}

	updatePlayerScore(aAddScore_num, aOptBounce_bln = false)
	{
		if (!this.spot) return;

		if (this.roundResultActive)
		{
			if (aOptBounce_bln)
			{
				this.spotBounce();
			}
		}
	}

	_showFireCryogun(data, callback)
	{
		this.playWeaponSound(data.usedSpecialWeapon, data.rid === -1);
		this.showPlayersWeaponEffect(data);
		APP.profilingController.info.isVfxProfileValueMediumOrGreater && this._addCryogunFireEffect(data);

		this.emit(GameField.EVENT_SHOW_FIRE, {data: data, callback: () => callback(null, null, 0, data)});

		let seat = this.getSeat(data.seatId, true);
		let gun = seat.weaponSpotView.gun;
		gun.shot();

		if (data.rid !== -1)
		{
			this.rotateGun(data.x, data.y);
			this.lockGunOnTarget(null);
			this._fCryogunsController_csc.once(CryogunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._cryogunMainSpotBeamAnimationCompletedForUnlock, this);
		}
	}

	_showFireFlameThrower(data, callback)
	{
		let enemyId = data.requestEnemyId;

		if (isNaN(enemyId))
		{
			throw new Error(`FlameThrower's target enemyId is ${enemyId}`);
		}

		this.playWeaponSound(data.usedSpecialWeapon, data.rid === -1);
		this.showPlayersWeaponEffect(data);
		let lResultProceeded = false;
		this.emit(GameField.EVENT_SHOW_FIRE, {data: data, callback: (endPos, angle) => {callback(null, endPos, angle); lResultProceeded = true;} } );

		let seat = this.getSeat(data.seatId, true);
		let gun = seat.weaponSpotView.gun;
		gun.shot();

		if (data.rid !== -1 && !lResultProceeded)
		{
			let enemy = this.getExistEnemy(enemyId);
			this.lockGunOnTarget(enemy);
			this._fFlameThrowersController_ftsc.once(FlameThrowersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._flameThrowerMainSpotBeamAnimationCompletedForUnlock, this);
		}
	}

	_showFireArtilleryStrike(data, callback)
	{
		let enemyId = ShotResultsUtil.getFirstNonFakeEnemy(data);

		enemyId = data.requestEnemyId;
		if (isNaN(enemyId))
		{
			throw new Error(`Artillery Strike's target enemyId is ${enemyId}`);
		}

		if (data.rid !== -1)
		{
			this.lockGunOnTarget(null);
			this._fArtilleryStrikesController_assc.once(ArtilleryStrikesController.EVENT_ON_MAIN_STRIKE_ANIMATION_COMPLETED, this._artilleryStrikeMainStrikeAnimationCompletedForUnlock, this);
		}

		this.playWeaponSound(data.usedSpecialWeapon, data.rid === -1);
		this.showPlayersWeaponEffect(data);
		this.emit(GameField.EVENT_SHOW_FIRE, {data: data, callback: () => callback(null)});
	}

	_showFirePlazmagun(data, callback)
	{
		let lIsMasterShot_bl = ShotResultsUtil.isMasterShot(data);

		let lTargetEnemyId_int = data.requestEnemyId;
		if (lTargetEnemyId_int < 0 || isNaN(lTargetEnemyId_int))
		{
			let lNonFakeEnemiesIds_int_arr = ShotResultsUtil.extractNonFakeEnemies(data);
			lTargetEnemyId_int = this._gameScreen.gameField.getFirstEverExistedEnemyIdFromTheList(lNonFakeEnemiesIds_int_arr);
		}
		let lTargetEnemy_enm = this.getExistEnemy(lTargetEnemyId_int);

		let seat = this.getSeat(data.seatId, true);
		let gun = seat.weaponSpotView.gun;
		gun.once(Weapon.EVENT_ON_GUN_RELOADED, () => {

			this.playWeaponSound(data.usedSpecialWeapon, !lIsMasterShot_bl);
			this.showPlayersWeaponEffect(data);
			this.shakeTheGround("plasma");

			this._fPlazmaGunsController_pgsc.relatedGun = gun;
			this.emit(GameField.EVENT_SHOW_FIRE, {data: data, targetEnemyId: lTargetEnemyId_int, callback: (endPos, angle) => callback(null, endPos, angle) } );
			gun.shot();

			if (lTargetEnemy_enm)
			{
				let lEnemyPos_pt = this.getEnemyPosition(lTargetEnemyId_int, true);
				this.showGroundBurn(lEnemyPos_pt.x, lEnemyPos_pt.y);
			}
		});
		gun.reload();

		if (lIsMasterShot_bl)
		{
			this.lockGunOnTarget(lTargetEnemy_enm);
			this.markEnemy(lTargetEnemy_enm, gun.isCharged);

			this._fPlazmaGunsController_pgsc.once(PlazmaGunsController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._plazmagunMainSpotBeamAnimationCompletedForUnlock, this);
		}
	}

	_plazmagunBeamTargetPreAchieved(aEvent_obj)
	{
		this.unmarkEnemy(aEvent_obj.targetEnemyId)
	}

	markEnemy(enemy, aIsFast_bl = false)
	{
		if (!enemy)
		{
			return;
		}

		enemy.addInstaMark(aIsFast_bl);
	}

	unmarkEnemy(aEnemyId_num)
	{
		let lEnemy_emn = this.getExistEnemy(aEnemyId_num);
		if (lEnemy_emn)
		{
			lEnemy_emn.removeInstaMark();
		}
	}

	// RICOCHET...
	_showFireRicochetLaser(data, callback)
	{
		let lIsMasterShot_bl = ShotResultsUtil.isMasterShot(data);
		let lTargetEnemyId_num = data.requestEnemyId;

		if (isNaN(lTargetEnemyId_num))
		{
			throw new Error(`Laser's target enemyId is ${lTargetEnemyId_num}`);
		}

		this.playWeaponSound(data.usedSpecialWeapon, !lIsMasterShot_bl);
		this.showPlayersWeaponEffect(data);

		let lResultProceeded = false;
		this.emit(GameField.EVENT_SHOW_FIRE, {data: data, callback: (endPos, angle) => {callback(null, endPos, angle); lResultProceeded = true;} } );

		let seat = this.getSeat(data.seatId, true);
		let gun = seat.weaponSpotView.gun;
		gun.shot();

		if (lIsMasterShot_bl && !lResultProceeded)
		{
			this.lockGunOnTarget(null);
			this._fRicochetLasersController_rlsc.once(RicochetLasersController.EVENT_ON_MAIN_SPOT_BEAM_BASIC_ANIMATION_COMPLETED, this._ricochetLasersMainSpotBeamAnimationCompletedForUnlock, this);
		}
	}

	get _ricochetLasersContainer()
	{
		return this.topScreen;
	}
	// ...RICOCHET

	_isMassWeapon(usedSpecialWeapon)
	{
		return	usedSpecialWeapon == WEAPONS.ARTILLERYSTRIKE ||
				usedSpecialWeapon == WEAPONS.FLAMETHROWER ||
				usedSpecialWeapon == WEAPONS.CRYOGUN ||
				usedSpecialWeapon == WEAPONS.RICOCHET ||
				usedSpecialWeapon == WEAPONS.INSTAKILL ||
				usedSpecialWeapon == WEAPONS.ARTILLERYSTRIKE_POWER_UP ||
				usedSpecialWeapon == WEAPONS.FLAMETHROWER_POWER_UP ||
				usedSpecialWeapon == WEAPONS.CRYOGUN_POWER_UP ||
				usedSpecialWeapon == WEAPONS.RICOCHET_POWER_UP ||
				usedSpecialWeapon == WEAPONS.INSTAKILL_POWER_UP;
	}

	showFire(data, callback)
	{
		if(APP.isCAFMode && !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			return;
		}

		this.emit(GameField.EVENT_ON_SHOT_SHOW_FIRE_START_TIME, {data:data, masterSeatId: this.seatId});

		if (!this.getSeat(data.seatId, true))
		{
			APP.logger.i_pushWarning(`GameField. Cannot show fire, no target seat detected, seatId: ${data.seatId}.`);
			console.log(`Cannot show fire, no target seat detected, seatId: ${data.seatId}`);
			return;
		}

		if (data.isExplode && !this._isMassWeapon(data.usedSpecialWeapon))
		{
			if (this._fNeedExplodeFeatureInitiated_bln)
			{
				this.once(GameField.EVENT_ON_EXPLODER_EXPLOSION_STARTED, ()=>callback(null, null, 0), this);
			}
			else
			{
				callback(null, null, 0);
			}

			return;
		}

		this.emit(GameField.EVENT_ON_BULLET_FLY_TIME);

		if (data.needExplode)
		{
			this._fNeedExplodeFeatureInitiated_bln = true;
		}

		//NEW WEAPONS...
		switch (data.usedSpecialWeapon)
		{
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				this._showFireCryogun(data, callback);
				return;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				this._showFireFlameThrower(data, callback);
				return;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				this._showFireArtilleryStrike(data, callback);
				return;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				this._showFirePlazmagun(data, callback);
				return;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				this._showFireRicochetLaser(data, callback);
				return;
		}
		//...NEW WEAPONS

		let startPos = {}, endPos = {x: 0, y: 0};
		let enemyId = (data.enemy) ? data.enemy.id : data.enemyId;
		let enemy, affectedEnemiesIds = [];

		for (let affectedEnemy of data.affectedEnemies)
		{
			affectedEnemiesIds.push(affectedEnemy.enemyId);
		}

		let uniqueAffectedEnemiesIds = affectedEnemiesIds.filter(function(value, index, self)
		{
			return self.indexOf(value) === index;
		});

		if (Array.isArray(data.affectedEnemies) && data.affectedEnemies.length > 1)
		{
			enemyId = data.requestEnemyId;
		}

		enemy = this.getExistEnemy(enemyId);
		if (!enemy)
		{
			APP.logger.i_pushWarning(`GameField. The affected enemy with enemyId=${enemyId} no longer exists in the game field!`);
			//console.log(`Attention! The affected enemy with enemyId=${enemyId} no longer exists in the game field!`);

			endPos = this.enemiesLastPositions[enemyId];
			if (!endPos)
			{
				callback(null, null, 0);
				return;
			}
		}
		else
		{
			endPos = enemy.getCenterPosition();
		}


		let lSpot_ps = this.getSeat(data.seatId, true);
		let lSpotCurrentDefaultWeaponId_int = lSpot_ps ? lSpot_ps.currentDefaultWeaponId : 1;
		let lIsRicochetBulletResultData_bl = this._isRicochetBulletResultData(data);

		if (!lIsRicochetBulletResultData_bl)
		{
			this.playWeaponSound(data.usedSpecialWeapon, data.rid === -1, lSpotCurrentDefaultWeaponId_int);
		}

		startPos = lSpot_ps.muzzleTipGlobalPoint;

		let seat = this.getSeat(data.seatId);
		let angle, len;

		let points = [startPos];

		if (data.rid != -1)
		{
			let lIsAutoTargetingEnemyInAffectedEnemies_bl = this.autoTargetingEnemy ? uniqueAffectedEnemiesIds.indexOf(this.autoTargetingEnemy.id) > -1 : false;
			let lIsIndicatedEnemyInAffectedEnemies_bl = this.indicatedEnemy ? uniqueAffectedEnemiesIds.indexOf(this.indicatedEnemy.id) > -1 : false;

			if (
				(enemy
				&& (!this.autoTargetingEnemy || !lIsAutoTargetingEnemyInAffectedEnemies_bl)
				&& (!this.indicatedEnemy || !lIsIndicatedEnemyInAffectedEnemies_bl))
				)
			{

				let dist = 0;
				let clickX = data.x || this.lastPointerPos.x;
				let clickY = data.y || this.lastPointerPos.y;

				let pointA = {x: clickX, y: clickY};
				let pointB = {x: points[0].x, y: points[0].y};
				let pointC = {x: endPos.x, y: endPos.y};

				dist = Math.sqrt(Math.pow(pointC.x - pointB.x, 2) + Math.pow(pointC.y - pointB.y, 2))/3;
				let pointerDist = Math.sqrt(Math.pow(clickX - pointB.x, 2) + Math.pow(clickY - pointB.y, 2));
				if (pointerDist < dist)
				{
					dist = pointerDist;
				}

				angle = Math.atan2(clickX - points[0].x, clickY - points[0].y) + Math.PI/2;
				let afterPoint = {
					x: pointB.x + Math.cos(angle-Math.PI)*dist,
					y: pointB.y - Math.sin(angle-Math.PI)*dist,
				}

				points.push(afterPoint);
			}
		}

		points.push(endPos);

		angle = Math.atan2(points[1].x - points[0].x, points[1].y - points[0].y) + Math.PI/2;
		len = Math.sqrt(Math.pow(points[0].x - points[1].x, 2) + Math.pow(points[0].y - points[1].y, 2));

		if (!seat)
		{
			if (!lIsRicochetBulletResultData_bl)
			{
				this.rotateGun(points[1].x, points[1].y);
			}
		}

		switch (data.usedSpecialWeapon)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				this.emit(GameField.DEFAULT_GUN_SHOW_FIRE, {seat: data.seatId});
				if (!lIsRicochetBulletResultData_bl)
				{
					this._addDefaultGunFireEffect(startPos.x, startPos.y, angle, len, lSpotCurrentDefaultWeaponId_int, lSpot_ps);
				}
				break;
		}

		if (!lIsRicochetBulletResultData_bl)
		{
			this.showPlayersWeaponEffect(data, angle, points[1]);
		}

		if (lIsRicochetBulletResultData_bl)
		{
			let lBulletPos = null;
			let lBulletAngle = 0;

			// ricochet bullet results
			let lIsMasterRicochetBullet_bl = this._isMasterRicochetBulletResultData(data);
			if (!lIsMasterRicochetBullet_bl)
			{
				if (!isNaN(data.x) && !isNaN(data.y))
				{
					lBulletPos = {x: data.x, y: data.y};
				}

				let lTargetRicochetBullet_rb = this.ricochetController.info.getBulletByBulletId(data.bulletId);
				if (lTargetRicochetBullet_rb)
				{
					if (!lBulletPos)
					{
						lBulletPos = {x: lTargetRicochetBullet_rb.x, y: lTargetRicochetBullet_rb.y};
					}

					lBulletAngle = lTargetRicochetBullet_rb.directionAngle;
				}
			}
			callback(null, lBulletPos, lBulletAngle);
			return;
		}

		let bulletProps = {
			radius: 4,
			typeId: data.usedSpecialWeapon
		};

		if (enemy)
		{
			bulletProps.targetEnemy = enemy;
		}

		switch (data.usedSpecialWeapon)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				bulletProps.defaultWeaponBulletId = lSpotCurrentDefaultWeaponId_int;
				break;
		}

		let lWeaponScale = seat ? seat.weaponSpotView.gun.i_getWeaponScale(this._fWeaponsController_wsc.i_getInfo().currentWeaponId): 1;
		bulletProps.weaponScale = lWeaponScale;

		let lIsMasterBullet_bl = (data.seatId === this.seatId);

		var projectile = this.generateBullet(data.usedSpecialWeapon, bulletProps, points, callback, data.rid, lIsMasterBullet_bl);

		this.topScreen.addChild(projectile);
		this.bullets.push(projectile);
	}

	_validateCoPlayerHighLevel(data)
	{
		let lSpot_ps = this.getSeat(data.seatId);

		if (data.rid !== -1 || !lSpot_ps || lSpot_ps.currentWeaponId !== WEAPONS.HIGH_LEVEL)
		{
			return;
		}

		let lExpectedMult_int = APP.playerController.info.getTurretSkinId(lSpot_ps.player.betLevel);
		if (lExpectedMult_int > lSpot_ps.currentDefaultWeaponId)
		{

			this._fCurrentLevelUpTimeout && this._fCurrentLevelUpTimeout.destructor();
			this._fCurrentLevelUpTimeout = new Timer(() => {
				this._fCurrentLevelUpTimeout && this._fCurrentLevelUpTimeout.destructor();
				this._fCurrentLevelUpTimeout = null;
				lSpot_ps.changeWeapon(lSpot_ps.currentWeaponId, lExpectedMult_int);
			}, 1000);
		}
	}

	_isRicochetBulletResultData(data)
	{
		return (data.bulletId !== undefined && data.bulletId.length > 0);
	}

	_isMasterRicochetBulletResultData(data)
	{
		return this._isRicochetBulletResultData(data) && data.seatId == this.seatId;
	}

	_startExploderExplosion()
	{
		this.emit(GameField.EVENT_ON_EXPLODER_EXPLOSION_STARTED);
	}

	_startTeleportAnimation(aEnemy_we, aEnemyPosition_obj, aEnemyScale_num, aOptDirection, aLabel)
	{
		let lAnimationOffsetX = 10;
		let lAnimationOffsetY = -75;

		let lIsFilterRequired_bln = APP.profilingController.info.isVfxProfileValueMediumOrGreater;
		let lBulgeFilter_f = null;

		let lWitchEnemyTeleportAnimation_ceta = this.witchEnemyTeleportAnimationContainer.container.addChild(new WitchEnemyTeleportAnimation());
		lWitchEnemyTeleportAnimation_ceta.scale.set(aEnemyScale_num);
		lWitchEnemyTeleportAnimation_ceta.zIndex = aEnemy_we.zIndex + 1;
		lWitchEnemyTeleportAnimation_ceta.position.set(aEnemyPosition_obj.x + lAnimationOffsetX, aEnemyPosition_obj.y + lAnimationOffsetY);
		lWitchEnemyTeleportAnimation_ceta.startAnimation();
		lWitchEnemyTeleportAnimation_ceta.once(WitchEnemyTeleportAnimation.EVENT_ON_ANIMATION_ENDED, ()=>{
			let lId_num = this._fWitchEnemyTeleportAnimations_ceta_arr.indexOf(lWitchEnemyTeleportAnimation_ceta);
			if (~lId_num)
			{

				this._fWitchEnemyTeleportAnimations_ceta_arr.splice(lId_num, 1);
			}

			lWitchEnemyTeleportAnimation_ceta && lWitchEnemyTeleportAnimation_ceta.destroy();
		});

		this._fWitchEnemyTeleportAnimations_ceta_arr.push(lWitchEnemyTeleportAnimation_ceta);

		if (lIsFilterRequired_bln)
		{
			lBulgeFilter_f = this._bulgePinchFilter;
			let lCenter_p = new PIXI.Point(aEnemyPosition_obj.x / APP.config.size.width, ( aEnemyPosition_obj.y + lAnimationOffsetY ) / APP.config.size.height);
			lBulgeFilter_f.uniforms.center = [lCenter_p.x, lCenter_p.y];
			lBulgeFilter_f.uniforms.radius = 150 * aEnemyScale_num;
			lBulgeFilter_f.uniforms.strength = 0;

			this._fTeleportBulgeFilters_f_arr.push({enemyId: aEnemy_we.id, filter: lBulgeFilter_f});

			this._addFilter(this.backContainer, lBulgeFilter_f);
			let lFilter_seq = [
				{tweens: [{prop: 'uniforms.strength', to: 0.5}],	duration: 9*FRAME_RATE},
				{tweens: [{prop: 'uniforms.strength', to: 0}],		duration: 3*FRAME_RATE}
			];
			let l_seq = Sequence.start(lBulgeFilter_f, lFilter_seq);
			this._fWitchSequences_arr.push(l_seq);
		}
	}
	_pauseTeleportAnimation()
	{
		if (this._fWitchEnemyTeleportAnimations_ceta_arr)
		{
			for (let lAnim_sprt of this._fWitchEnemyTeleportAnimations_ceta_arr)
			{
				lAnim_sprt && lAnim_sprt.pauseAnimation();
			}
		}

		if (this._fWitchSequences_arr)
		{
			for (let seq of this._fWitchSequences_arr)
			{
				seq && seq.pause();
			}
		}

	}

	_resumeTeleportAnimation()
	{
		if (this._fWitchEnemyTeleportAnimations_ceta_arr)
		{
			for (let lAnim_sprt of this._fWitchEnemyTeleportAnimations_ceta_arr)
			{
				lAnim_sprt && lAnim_sprt.resumeAnimation();
			}
		}

		if (this._fWitchSequences_arr)
		{
			for (let seq of this._fWitchSequences_arr)
			{
				seq && seq.resume();
			}
		}
	}

	_addCryogunFireEffect(data)
	{
		let startPos = this.getGunPosition(data.seatId);
		let endPos = new PIXI.Point(data.x, data.y);
		let points = [startPos, endPos];
		let angle = Math.atan2(points[1].x - points[0].x, points[1].y - points[0].y) + Math.PI/2;

		let fireEffect = this.topScreen.addChild(new CryogunFireEffect);
		fireEffect.rotation = -angle - Math.PI/2;

		let x = startPos.x;
		let y = startPos.y;
		x += Math.cos(angle)*(-40);
		y -= Math.sin(angle)*(-40);

		fireEffect.position.set(x, y);

		fireEffect.zIndex = Z_INDEXES.GUN_FIRE_EFFECT;
		this._fFxAnims_arr.push(fireEffect);

		fireEffect.once(CryogunFireEffect.EVENT_ON_ANIMATION_END, this._onSomeGunFireEffectAnimationCompleted, this);

		if (data.rid == -1)
		{
			fireEffect.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	_onSomeGunFireEffectAnimationCompleted(event)
	{
		let lIndex_int = this._fFxAnims_arr.indexOf(event.target);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
			event.target.destroy();
		}
	}

	generateBullet(weaponId, bulletProps, points, callback, rid, aIsMasterBullet_bl)
	{
		let bullet;
		switch (weaponId)
		{
			default:
				bullet = new Bullet(bulletProps, points, callback, aIsMasterBullet_bl);
				bullet.on(Bullet.EVENT_ON_SHOW_RICOCHET_EFFECT, (e) => {this.showRicochetEffect(e.x, e.y)});
		}
		return bullet;
	}

	playWeaponSound(id, aOptOtherPlayersShot_bl = false, aDefaultWeaponId_int = 1)
	{
		let lVolume_num = 1;
		let soundName = '';
		let randomSoundIndex = 1;
		switch (id)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:
				let lSpotCurrentDefaultWeaponId_int = aDefaultWeaponId_int; //this._fWeaponsController_wsc.i_getInfo().currentDefaultWeaponId;
				soundName = 'mq_turret_burst' + lSpotCurrentDefaultWeaponId_int;
				if(lSpotCurrentDefaultWeaponId_int === 5)
				{
					if(!APP.isMobile) lVolume_num *= 0.25;
					else lVolume_num *= 0.55;
				}
				break;
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				soundName = 'cryo_gun_shot';
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				soundName = 'mq_guns_splitshot';
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				soundName = 'flamethrower_shot';
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				soundName = 'bomb_flying';
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				soundName = 'plasma_shot';
				break;
			default:
				randomSoundIndex = APP.isMobile || APP.profilingController.info.isVfxProfileValueLessThan(ProfilingInfo.i_VFX_LEVEL_PROFILE_VALUES.MEDIUM) ? 6 : Utils.random(1, 8);
				soundName = 'mq_guns_bullets_flyby_' + randomSoundIndex;
				break;
		}

		if (soundName)
		{
			lVolume_num *= aOptOtherPlayersShot_bl ? GameSoundsController.OPPONENT_WEAPON_VOLUME : 1;
			APP.soundsController.play(soundName, false, lVolume_num, aOptOtherPlayersShot_bl);
		}
	}

	lockGunOnTarget(enemy)
	{
		this.gunLockTargetedEnemy = enemy;
		this.gunLocked = true;
	}

	unlockGun()
	{
		this.gunLocked = false;
		this.isWeaponChangeInProcess && this._fCurChangeWeaponTimerInfo_obj.timer.finish();
		this.emit(GameField.EVENT_ON_GUN_UNLOCKED);
	}

	showPlayersWeaponEffect(data, angle = undefined, endPos = undefined)
	{
		if (angle === undefined || endPos == undefined)
		{
			let startPos = this.getGunPosition(data.seatId);
			switch (data.usedSpecialWeapon)
			{
				case WEAPONS.CRYOGUN:
				case WEAPONS.CRYOGUN_POWER_UP:
				case WEAPONS.ARTILLERYSTRIKE:
				case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
					endPos = new PIXI.Point(data.x, data.y);
					break;
				case WEAPONS.RICOCHET:
				case WEAPONS.RICOCHET_POWER_UP:
				case WEAPONS.FLAMETHROWER:
				case WEAPONS.FLAMETHROWER_POWER_UP:
				case WEAPONS.INSTAKILL:
				case WEAPONS.INSTAKILL_POWER_UP:
					let enemyId = data.requestEnemyId;
					let enemy = this.getExistEnemy(enemyId);
					let enemyPos = enemy ? enemy.getCenterPosition() : this.getEnemyLastPosition(enemyId);

					endPos = enemyPos || new PIXI.Point(data.x, data.y);
					if (ShotResultsUtil.isMasterShot(data))
					{
						this.addPushEffect(endPos.x, endPos.y, this.spot);

						if (data.usedSpecialWeapon == WEAPONS.INSTAKILL || data.usedSpecialWeapon == WEAPONS.INSTAKILL_POWER_UP)
						{
							this.spot.showInstantKillRecoilEffect();
						}
					}
					break;
				default:
					throw new Error ('angle and/or endPos are undefined, weapons effect can\'t proceed');
			}
			angle = Math.atan2(endPos.x - startPos.x, endPos.y - startPos.y) + Math.PI/2;
		}
		for (var i = 0; i < this.players.length; ++i)
		{
			if (this.players[i].seatId == data.seatId)
			{
				if (this.players[i].weaponSpotView)
				{
					if (this.players[i].currentWeaponId != data.usedSpecialWeapon
							|| (data.betLevel && this.players[i].spot.currentDefaultWeaponId != this._getDefaultWeaponIdByBetLeavel(data.betLevel)))
					{
						this.players[i].currentWeaponId = this.players[i].specialWeaponId = data.usedSpecialWeapon;
						let lMult_int = APP.playerController.info.getTurretSkinId(data.betLevel);
						this.players[i].spot.changeWeapon(data.usedSpecialWeapon, lMult_int);
					}

					this.players[i].weaponSpotView.rotation = -angle - Math.PI/2;

					if (!this.players[i].spot.isBottom)
					{
						this.players[i].weaponSpotView.rotation += Math.PI;
					}

					let seat = this.getSeat(this.players[i].seatId, true);
					this.addPushEffect(endPos.x, endPos.y, seat);
				}

				break;
			}
		}
	}

	_getDefaultWeaponIdByBetLeavel(aBetLevel_num)
	{
		let lDefaultWeaponId_num = 1;
		switch(aBetLevel_num)
		{
			case 1:
				lDefaultWeaponId_num = 1;
				break;
			case 2:
				lDefaultWeaponId_num = 2;
				break;
			case 3:
				lDefaultWeaponId_num = 3;
				break;
			case 5:
				lDefaultWeaponId_num = 4;
				break;
			case 10:
				lDefaultWeaponId_num = 5;
				break;
			default:
				throw new Error(`Wrong bet leavel in BTG game ${aBetLevel_num} !`);
		}
		return lDefaultWeaponId_num;
	}

	showPlayersWeaponSwitched(data)
	{
		if (data.seatId == this.seatId)
		{
			if (data.weaponId != WEAPONS.DEFAULT &&
				this.spot &&
				data.weaponId != this._fWeaponsInfo_wsi.currentWeaponId &&
				!this._fRoundResultActive_bln)
			{
				this.changeWeapon(data.weaponId);
				this.redrawAmmoText();
				this.spot.on(MainPlayerSpot.EVENT_RELOAD_REQUIRED, this._onReloadRequired, this);

				let lSpotCurrentDefaultWeaponId_int = this._fWeaponsController_wsc.i_getInfo().currentDefaultWeaponId;
				this.spot.changeWeapon(data.weaponId, lSpotCurrentDefaultWeaponId_int);
			}

			this._fWeaponSwitchTry_bln = false;
			this._fWeaponSwitchInProgress_bln = false;
			this.fireImmediatelyIfRequired();
		}

		for (var i = 0; i < this.players.length; i ++)
		{
			let lPlayer_obj = this.players[i];
			if (lPlayer_obj.seatId == data.seatId && data.rid == -1)
			{
				lPlayer_obj.currentWeaponId = lPlayer_obj.specialWeaponId = data.weaponId;
				if (lPlayer_obj.spot)
				{
					let lSpotCurrentDefaultWeaponId_int = APP.playerController.info.getTurretSkinId(lPlayer_obj.betLevel);
					lPlayer_obj.spot.changeWeapon(data.weaponId, lSpotCurrentDefaultWeaponId_int);
				}
			}
		}
	}

	//FLAMETHROWER..
	_onFlameThrowerBeamRotationUpdated(aEvent_obj)
	{
		let seatId = aEvent_obj.seatId;
		let endPoint = aEvent_obj.endPoint;

		this.rotatePlayerGun(seatId, endPoint.x, endPoint.y);
	}
	//...FLAMETHROWER

	//ARTILLERY STRIKE...
	_onArtilleryStrikeMissileHit(aEvent_obj)
	{
		let isFinal = aEvent_obj.isFinal;
		let isFirst = aEvent_obj.isFirst;
		let pos = new PIXI.Point(aEvent_obj.x, aEvent_obj.y);
		let rid = aEvent_obj.rid;

		let lGroundBurnOffsetY_num = 20;
		//show explosion
		let strikeExplosion = this.topScreen.addChild(new ArtillerystrikeExplosion(isFinal));
		strikeExplosion.position.set(pos.x, pos.y);
		strikeExplosion.zIndex = pos.y + 200;
		if (this._fGameStateInfo_gsi.isBossSubround)
		{
			strikeExplosion.zIndex += 500;
		}
		strikeExplosion.start();
		strikeExplosion.once("animationFinish", this._onSomeGunFireEffectAnimationCompleted, this);
		this._fFxAnims_arr.push(strikeExplosion);

		let lGroundBurnScale_num = isFinal ? 1 : 0.4;
		this.showGroundBurn(pos.x, pos.y + lGroundBurnOffsetY_num, lGroundBurnScale_num, 0.7 /*initial alpha*/, true);

		let lIsMainPlayerShot_bl = rid >= 0;
		if (isFirst)
		{
			let lVolume_num = lIsMainPlayerShot_bl ? GameSoundsController.MAIN_PLAYER_VOLUME : GameSoundsController.OPPONENT_WEAPON_VOLUME;
			APP.soundsController.play('artillery_explosion', false, lVolume_num, !lIsMainPlayerShot_bl);
		}

		if (!lIsMainPlayerShot_bl)
		{
			strikeExplosion.alpha = 0.1;
		}
	}
	//...ARTILLERY STRIKE

	showRicochetEffect(x, y)
	{
		let ricochetEffect = this.topScreen.addChild(new RicochetEffect(x, y));
		ricochetEffect.once("animationFinish", this.onRicochetEffectFinished, this);

		this._fFxAnims_arr.push(ricochetEffect);
	}

	onRicochetEffectFinished(event)
	{
		let lIndex_int = this._fFxAnims_arr.indexOf(event.target);
		if (~lIndex_int)
		{
			this._fFxAnims_arr.splice(lIndex_int, 1);
		}
	}

	showMissEffect(x, y, weaponId, optTargetEnemy, aOptCurrentDefaultWeaponId_int = 1, aOptIsMasterEffect_bl = true)
	{
		switch (weaponId)
		{
			case WEAPONS.DEFAULT:
			case WEAPONS.HIGH_LEVEL:

				if(MissEffect.IS_MISS_EFFECT_REQUIRED)
				{
					this._fMissEffectsPool_mep.addMissEffect(
						aOptCurrentDefaultWeaponId_int,
						x,
						y,
						aOptIsMasterEffect_bl);
				}

				break;
			default:
				break;
		}
	}

	removeAllFxAnimations()
	{
		this._fMissEffectsPool_mep && this._fMissEffectsPool_mep.drop();

		while (this._fFxAnims_arr.length)
		{
			this._fFxAnims_arr.shift().destroy();
		}
		this._fFxAnims_arr = [];
	}

	proceedFireResult(e, endPos, angle, data)
	{
		this.emit(GameField.EVENT_ON_BULLET_TARGET_TIME, {data: data});

		switch (data.usedSpecialWeapon)
		{
			case WEAPONS.CRYOGUN:
			case WEAPONS.CRYOGUN_POWER_UP:
				this.proceedTypicalGrenadeResult(data, endPos, angle);
				break;
			case WEAPONS.RICOCHET:
			case WEAPONS.RICOCHET_POWER_UP:
				this.proceedLaserRicochetResult(data, endPos, angle);
				break;
			case WEAPONS.FLAMETHROWER:
			case WEAPONS.FLAMETHROWER_POWER_UP:
				this.proceedFlameThrowerResult(data, endPos, angle);
				break;
			case WEAPONS.ARTILLERYSTRIKE:
			case WEAPONS.ARTILLERYSTRIKE_POWER_UP:
				this.proceedArtilleryStrikeResult(data);
				break;
			case WEAPONS.INSTAKILL:
			case WEAPONS.INSTAKILL_POWER_UP:
				this.proceedTypicalGrenadeResult(data, endPos, angle);
				break;
			case WEAPONS.HIGH_LEVEL:
				this._validateCoPlayerHighLevel(data);
			default:
				for (let obj of data.affectedEnemies)
				{
					if (obj.data.class == 'Miss') this.showMissAnimation(e, endPos, angle, obj.data);
					if (obj.data.class == 'Hit') this.showHitAnimation(e, endPos, angle, obj.data);
				}
				break;
		}
	}

	proceedTypicalGrenadeResult(data, endPos, angle)
	{
		let realAffectedEnemies = ShotResultsUtil.excludeFakeEnemies(data.affectedEnemies);
		for (let obj of realAffectedEnemies)
		{
			if (obj.data.class == 'Miss') this.showMissAnimation(null, endPos, angle, obj.data);
			if (obj.data.class == 'Hit') this.showHitAnimation(null, endPos, angle, obj.data);
		}
	}

	proceedLaserRicochetResult(data, endPos, angle)
	{
		let uniqueAffectedEnemiesIds = this.extractUniqueEnemiesIds(data.affectedEnemies);
		let lHitDuration_num = uniqueAffectedEnemiesIds.length > 1 ? RicochetLaserRay.FULL_APPEARANCE_TIME : RicochetLaserRay.HALF_APPEARANCE_TIME;
		for (let obj of ShotResultsUtil.excludeFakeEnemies(data.affectedEnemies))
		{
			obj.data.hitDuration = lHitDuration_num;
			if (obj.data.class == 'Miss') this.showMissAnimation(null, endPos, angle, obj.data);
			if (obj.data.class == 'Hit') this.showHitAnimation(null, endPos, angle, obj.data);
		}
	}

	_getDistanceLineToPoint(p1, p2, p0)
	{
		let a = p1.y - p2.y;
		let b = p2.x - p1.x;
		let c = p2.y*p1.x - p1.y*p2.x;
		let h = (a*p0.x + b*p0.y + c) / Math.sqrt(a*a + b*b);
		return h;
	}

	_getDistanceBetweenTwoPoints(p1, p2)
	{
		var a = p1.x - p2.x;
		var b = p1.y - p2.y;

		return Math.sqrt(a*a + b*b);
	}

	_getPointOnTheLine(p1, p2, y0)
	{
		let k = (p1.y - p2.y) / (p1.x - p2.x);
		let b = p2.y - k * p2.x;
		let x0 = (y0 - b) / k;

		return {x: x0, y: y0};
	}

	proceedFlameThrowerResult(data, endPos, angle)
	{
		if (!endPos && data.rid != -1)
		{
			this.unlockGun();
		}

		// group enemies
		let lEnemies_obj = {}; // enemyId : [shotResultData, shotResultData]
		for (let obj of ShotResultsUtil.excludeFakeEnemies(data.affectedEnemies))
		{
			let lEnemyId_int = obj.enemyId;
			if (!lEnemies_obj[lEnemyId_int])
			{
				lEnemies_obj[lEnemyId_int] = [];
			}
			lEnemies_obj[lEnemyId_int].push(obj.data);
		}

		// create debris flying to affected enemies
		if (Object.keys(lEnemies_obj).length > 0)
		{
			for (let enemyId in lEnemies_obj)
			{
				this.startExplosionDebris(data.usedSpecialWeapon, enemyId, lEnemies_obj[enemyId], endPos, angle);
			}
		}
	}

	proceedArtilleryStrikeResult(data)
	{
		// group enemies
		let lEnemies_obj = {}; // enemyId : [shotResultData, shotResultData]
		for (let obj of ShotResultsUtil.excludeFakeEnemies(data.affectedEnemies))
		{
			let lEnemyId_int = obj.enemyId;
			if (!lEnemies_obj[lEnemyId_int])
			{
				lEnemies_obj[lEnemyId_int] = [];
			}
			lEnemies_obj[lEnemyId_int].push(obj.data);
		}

		// create artillery strikes flying to affected enemies
		if (Object.keys(lEnemies_obj).length > 0)
		{
			for (let enemyId in lEnemies_obj)
			{
				this.proceedExplosionDebrisResult(null/*e*/, null/*endPos*/, Math.PI/2/*angle*/, lEnemies_obj[enemyId]);
			}
		}
	}

	startExplosionDebris(weaponId, enemyId, data_arr, explosionCenterPosition, defaultAngle = 0)
	{
		let bulletProps = {
			typeId: weaponId
		};

		let lAffectedEnemy_enm = this.getExistEnemy(enemyId);
		if (lAffectedEnemy_enm)
		{
			let lEnemyPos_pt = lAffectedEnemy_enm.getGlobalPosition();
			let lDistance_num = Utils.getDistance(lEnemyPos_pt, explosionCenterPosition);
			if ( lDistance_num !== undefined && lDistance_num > 100)
			{
				bulletProps.targetEnemy = lAffectedEnemy_enm;
			}
		}

		if (!bulletProps.targetEnemy && data_arr)
		{
			this.proceedExplosionDebrisResult(null, explosionCenterPosition, defaultAngle, data_arr);
		}

		let points = [];
		points.push(explosionCenterPosition); // startPos
		if (bulletProps.targetEnemy)
		{
			let lEnemyPos_pt = bulletProps.targetEnemy.getGlobalPosition();
			let lEnemyCurrentFootPoint_pt = bulletProps.targetEnemy.getCurrentFootPointPosition();
			lEnemyPos_pt.y += lEnemyCurrentFootPoint_pt.y;

			//random offset...
			let hitRect = bulletProps.targetEnemy.getHitRectangle();
			let dx = lEnemyCurrentFootPoint_pt.x + hitRect.width - Utils.random(0, hitRect.width * 2, true);
			let dy = hitRect.height - Utils.random(0, hitRect.height * 2, true);
			bulletProps.randomOffset = {x: dx, y: dy};
			//...random offset

			points.push(lEnemyPos_pt);
			let DebrisClass = CharcoalFlyingDebris;
			let lFlyingDebris_fd = new DebrisClass(bulletProps, points, (e, endPos, angle) => this.proceedExplosionDebrisResult(e, endPos, angle, data_arr));
			this.topScreen.addChild(lFlyingDebris_fd);
			this.bullets.push(lFlyingDebris_fd);
		}
	}

	getFloorRandomPoint()
	{
		let lRandomEnemy_enm = this.getRandomEnemy();

		if (lRandomEnemy_enm)
		{
			let lPointsCount_int = lRandomEnemy_enm.trajectory.points.length;
			let lRandom_int = Utils.random(0, lPointsCount_int-1);
			let lRandomPoint_pt = lRandomEnemy_enm.trajectory.points[lRandom_int];
			return lRandomPoint_pt;
		}

		return {x: Utils.random(960/4, 960/2), y: Utils.random(540/4, 540/2)};
	}

	getRandomEnemy()
	{
		if (this.enemies && this.enemies.length > 0)
		{
			let n = Utils.random(0, this.enemies.length - 1);
			return this.enemies[n];
		}
		return null;
	}

	proceedExplosionDebrisResult(e, endPos, angle, data_arr)
	{
		for (let data of data_arr)
		{
			switch (data.class)
			{
				case 'Miss':
					this.showMissAnimation(null, endPos, angle, data);
					break;
				case 'Hit':
					this.showHitAnimation(null, endPos, angle, data);
					break;
			}
		}
	}

	showMissAnimation(e, endPos, angle, data)
	{
		let enemyId = (data.enemy) ? data.enemy.id : data.enemyId;
		let enemy = this.getExistEnemy(enemyId);
		let lIsMasterRicochetBullet_bl = this._isMasterRicochetBulletResultData(data);

		this.emit(GameField.EVENT_ON_ENEMY_MISS_ANIMATION, {data: data, enemyId: enemyId, rid: data.rid});

		if (enemy)
		{
			if (data.killedMiss)
			{
				let lPlayerWin_bln = false;
				let lCoPlayerWin_bln = false;

				if (data.hitResultBySeats)
				{
					for (let key in data.hitResultBySeats)
					{
						for (let prize of data.hitResultBySeats[key])
						{
							if ((prize.id === HIT_RESULT_SINGLE_CASH_ID || prize.id === HIT_RESULT_ADDITIONAL_CASH_ID) && +prize.value > 0)
							{
								lPlayerWin_bln = lPlayerWin_bln || !!(+key === +this.seatId);
								lCoPlayerWin_bln = lCoPlayerWin_bln || !!(+key !== +this.seatId);
							}
						}
					}
				}

				enemy.childHvEnemyId = data.hvEnemyId;
				enemy.setDeath(false, {playerWin: lPlayerWin_bln, coPlayerWin: lCoPlayerWin_bln});

				this.pushEnemyToDeadList(data.enemyId);
				this.emit("removeEnemy", {id: data.enemyId});
			}
			else
			{
				if (!lIsMasterRicochetBullet_bl)
				{
					enemy.showHitBounce(angle, data.usedSpecialWeapon, data.hitDuration);
					enemy.playHitHighlightAnimation(5*FRAME_RATE);
				}
			}
		}

		this.onEnemyImpacted(endPos, data, enemyId, enemy);
	}

	showHitAnimation(e, endPos, angle, data)
	{
		let enemyId = (data.enemy) ? data.enemy.id : data.enemyId;
		let enemy = this.getExistEnemy(enemyId);
		let lIsMasterRicochetBullet_bl = this._isMasterRicochetBulletResultData(data);

		this.emit(GameField.EVENT_ON_ENEMY_HIT_ANIMATION, {data: data, enemyId: enemyId, damage: data.damage, rid: data.rid});

		if (enemy)
		{
			if (!data.killed)
			{
				if (enemy.isBoss) enemy.energy = data.enemy.energy; // to trigger boss's weak state
				enemy.awardedPrizes = data.enemy.awardedPrizes;

				if (!lIsMasterRicochetBullet_bl)
				{
					enemy.showHitBounce(angle, data.usedSpecialWeapon, data.hitDuration);
					enemy.playHitHighlightAnimation(5*FRAME_RATE);
				}
			}
			else
			{
				let lPlayerWin_bln = false;
				let lCoPlayerWin_bln = false;

				if (data.hitResultBySeats)
				{
					for (let key in data.hitResultBySeats)
					{
						for (let prize of data.hitResultBySeats[key])
						{
							if ((prize.id === HIT_RESULT_SINGLE_CASH_ID || prize.id === HIT_RESULT_ADDITIONAL_CASH_ID) && +prize.value > 0)
							{
								if (prize.id === HIT_RESULT_SINGLE_CASH_ID && !!data.skipAwardedWin && data.enemy.typeId != ENEMY_TYPES.BOSS)
								{
									continue;
								}
								lPlayerWin_bln = lPlayerWin_bln || !!(+key === +this.seatId);
								lCoPlayerWin_bln = lCoPlayerWin_bln || !!(+key !== +this.seatId);
							}
						}
					}
				}

				if (enemy.isBoss && data.playerName)
				{
					this.emit(GameField.EVENT_ON_UPDATE_PLAYER_WIN_CAPTION, {playerName: data.playerName, seatId: data.seatId})
				}

				enemy.childHvEnemyId = data.hvEnemyId;
				enemy.setDeath(false, {playerWin: lPlayerWin_bln, coPlayerWin: lCoPlayerWin_bln});
				this.pushEnemyToDeadList(enemyId);
				this.emit("removeEnemy", {id: enemyId});

				this.emit(GameField.EVENT_ON_ENEMY_KILLED_BY_PLAYER, {
					playerName: data.playerName || "",
					enemyName: enemy.name
				});
			}
		}
		else
		{
			if (data && data.enemy && data.enemy.typeId)
			{
				const name = GameScreen.calculateEnemyName(~~data.enemy.typeId, data.enemy.skin);

				this.emit(GameField.EVENT_ON_ENEMY_KILLED_BY_PLAYER, {
					playerName: data.playerName || "",
					enemyName: name
				});
			}
		}

		this.onEnemyImpacted(endPos, data, enemyId, enemy, true);
	}

	hideEnemyEffectsBeforeDeathIfRequired(enemy)
	{
		for (let i = 0; i < this.enemies.length; i ++)
		{
			const enemyView = this.enemies[i];
			if (enemy.id == enemyView.id)
			{
				enemyView.hideEnemyEffectsBeforeDeathIfRequired();
				break;
			}
		}
	}

	onEnemyImpacted(endPos, data, enemyId, enemy)
	{
		let lIsMasterRicochetBullet_bl = this._isMasterRicochetBulletResultData(data);

		if (endPos && !lIsMasterRicochetBullet_bl)
		{
			let lSeat_pt = this.getSeat(data.seatId, true);
			let lSpotCurrentDefaultWeaponId_int = lSeat_pt ? lSeat_pt.currentDefaultWeaponId : 1;
			let lTargetRicochetBullet_rb = this.ricochetController.info.getBulletByBulletId(data.bulletId);
			if (lTargetRicochetBullet_rb && !lTargetRicochetBullet_rb.disappeared)
			{
				this.showMissEffect(
					endPos.x,
					endPos.y,
					data.usedSpecialWeapon,
					enemy,
					lSpotCurrentDefaultWeaponId_int,
					lSeat_pt.isMaster);
			}
		}

		let lCurrentEnemyPosition_pt = this.enemiesLastPositions[enemyId] || new PIXI.Point(data.x+Utils.random(-10, 10), data.y+Utils.random(-10, 10));
		let lPreviousEnemyPosition_pt = new PIXI.Point();
		let lEnemyFootPoint_pt = new PIXI.Point();
		if (enemy)
		{
			lCurrentEnemyPosition_pt = enemy.getGlobalPosition();
			lEnemyFootPoint_pt = enemy.footPoint;
			if (enemy.prevTurnPoint)
			{
				lPreviousEnemyPosition_pt.x = enemy.prevTurnPoint.x - lEnemyFootPoint_pt.x;
				lPreviousEnemyPosition_pt.y = enemy.prevTurnPoint.y - lEnemyFootPoint_pt.y;
			}
		}

		if (!endPos)
		{
			endPos = Utils.clone(lCurrentEnemyPosition_pt);
		}

		let lEnemyId_int = enemy ? enemy.id : null;
		let lPrizePosition_pt = lCurrentEnemyPosition_pt;
		let lIsBoss_bl = enemy && enemy.isBoss;
		if (enemy && enemy.isBoss)
		{
			lPrizePosition_pt.y -= 70;
		}

		let lIsMainPlayer_bln = !!(data.seatId == APP.playerController.info.seatId);
		if (!lIsMainPlayer_bln)
		{
			data.chMult = 0;
			if (data.killed && data.isKilledBossHit)
			{
				// keep killBonusPay for killed boss hits
			}
			else
			{
				data.win += (data.killBonusPay || 0);
				data.killBonusPay = 0;
			}
		}

		if (data.needExplode)
		{
			let lXShift_num = 0;
			if (!enemy || !enemy.direction)
			{
				lXShift_num = 0;
			}
			else if (enemy.direction == ENEMY_DIRECTION.LEFT_DOWN || enemy.direction == ENEMY_DIRECTION.LEFT_UP)
			{
				lXShift_num = -25;
			}
			else
			{
				lXShift_num = 25;
			}
			this._startExploderExplosion({x: lCurrentEnemyPosition_pt.x + lXShift_num, y: lCurrentEnemyPosition_pt.y - 40});
		}

		if(enemy && enemy.typeId == ENEMY_TYPES.EXPLODER && data.killed)
		{
			this._fFrogDeathDiffTime_num = APP.currentWindow.currentTime;
		}

		let lIsBigWin_bln = data.skipAwardedWin;

		if (!lIsBigWin_bln && data.currentPowerUpMultiplier > 1 && data.win != 0 && lIsMainPlayer_bln)
		{
			data.chMult = 0;
			this._startPowerUpMultiplierAnimation(data.currentPowerUpMultiplier, lPrizePosition_pt, enemyId, null, data.rid);
		}

		if (data.chMult > 1 && data.win != 0 && !lIsBigWin_bln)
		{
			this._startCriticalHitAnimation(data.win, data.chMult, lPrizePosition_pt, enemy ? enemy.id : null, enemy ? enemy.name : null, enemy ? enemy.direction : null, data.rid);
		}

		if (enemy && enemy.typeId == ENEMY_TYPES.EXPLODER)
		{
			new Timer(()=>this._showPrizes(data, lPrizePosition_pt, lEnemyId_int, lIsBoss_bl), 1000);
		} else this._showPrizes(data, lPrizePosition_pt, lEnemyId_int, lIsBoss_bl);
		
		this.emit('showEnemyHit', {id: enemyId, data: data, enemyView: enemy, position: endPos});
	}

	/*
	@aPrizePosition_pt - current dying enemy position
	*/
	_showPrizes(data, aPrizePosition_pt, aEnemyId_int, aIsBoss_bl)
	{
		this.emit(GameField.EVENT_TIME_TO_SHOW_PRIZES, {	prizesData:
															{
																hitData: data,
																prizePosition: aPrizePosition_pt,
																enemyId: aEnemyId_int,
																isBoss: aIsBoss_bl
															}
														}
		);
	}

	_getOffscreenOffsetY(aPosY_num, aAssetName_str)
	{
		let lOffscreenOffsetY_num = 0;
		let lMaxOverkillScaleValue_num = 1.9;
		let lHeigth_num = I18.generateNewCTranslatableAsset(aAssetName_str).getBounds().height;
		lHeigth_num *= lMaxOverkillScaleValue_num;

		if (aPosY_num - lHeigth_num / 2 < 10) /* 10 - not to be covered by top black bar (infobar)*/
		{
			lOffscreenOffsetY_num = 10 - (aPosY_num - lHeigth_num / 2) + Math.random() * 50;
		}
		else if (aPosY_num + lHeigth_num / 2 > 540)
		{
			lOffscreenOffsetY_num = 540 - (aPosY_num + lHeigth_num / 2) - Math.random() * 50;
		}

		return lOffscreenOffsetY_num;
	}

	_getOffscreenOffsetX(aPosX_num, aAssetName_str)
	{
		let lOffscreenOffsetX_num = 0;
		let lMaxOverkillScaleValue_num = 1.9;
		let lWidth_num = I18.generateNewCTranslatableAsset(aAssetName_str).getBounds().width;
		lWidth_num *= lMaxOverkillScaleValue_num;

		if (aPosX_num - lWidth_num / 2 < 0)
		{
			lOffscreenOffsetX_num = 0 - (aPosX_num - lWidth_num / 2) + Math.random() * 50;
		}
		else if (aPosX_num + lWidth_num / 2 > 960)
		{
			lOffscreenOffsetX_num = 960 - (aPosX_num + lWidth_num / 2) - Math.random() * 50;
		}

		return lOffscreenOffsetX_num;
	}

	get _bulgePinchFilter()
	{
		if (!this._fBulgePinchFilter_f)
		{
			this._fBulgePinchFilter_f = new BulgePinchFilter();
			this._fBulgePinchFilter_f.resolution = APP.stage.renderer.resolution;
		}

		return this._fBulgePinchFilter_f;
	}

	_startPowerUpMultiplierAnimation(aCurrentPowerUpMultiplier_num, aPos_obj, aEnemyId_num, aEnemyName_str, aRid_num)
	{
		const lPowerUpMultiplier_puma = this.powerUpMultiplierAnimationContainer.container.addChild(new PowerUpMultiplierAnimation(aCurrentPowerUpMultiplier_num, aEnemyId_num, aEnemyName_str, aRid_num, this.spot.isBottom));
		lPowerUpMultiplier_puma.zIndex = this.powerUpMultiplierAnimationContainer.zIndex;
		lPowerUpMultiplier_puma.position = this.spot.position;
		lPowerUpMultiplier_puma.once(PowerUpMultiplierAnimation.EVENT_ON_ANIMATION_ENDED, () => {
			let lId_num = this._fPowerUpMultipliers_puma_arr.indexOf(lPowerUpMultiplier_puma);
			if (~lId_num)
			{
				this._fPowerUpMultipliers_puma_arr.splice(lId_num, 1);
			}

			this.emit(GameField.EVENT_ON_PUWER_UP_ANIMATION_ENDED, {rid: lPowerUpMultiplier_puma.rid, enemyId: lPowerUpMultiplier_puma.enemyId});
			lPowerUpMultiplier_puma && lPowerUpMultiplier_puma.destroy();
		});

		this._fPowerUpMultipliers_puma_arr.push(lPowerUpMultiplier_puma);
	}

	_startCriticalHitAnimation(aWin_num, aMult_num, aPos_obj, aEnemyId_num, aEnemyName_str, aEnemyDirection_str, aRid_num)
	{
		let lOffsetX_num = this._getOffscreenOffsetX(aPos_obj.x, "TACriticalHitLabel");
		let lOffsetY_num = this._getOffscreenOffsetY(aPos_obj.y - 70, "TACriticalHitLabel");

		let lIsMasterPlayer_bl = (aRid_num >= 0);
		let lCrit_anim = this.criticalHitAnimationContainer.container.addChild(new CriticalHitAnimation(aWin_num, aMult_num, aEnemyId_num, aEnemyName_str, aEnemyDirection_str, aRid_num));
		lCrit_anim.zIndex = this.criticalHitAnimationContainer.zIndex;
		lCrit_anim.position.set((aPos_obj ? aPos_obj.x : 0) + lOffsetX_num, (aPos_obj ? aPos_obj.y : 0) + lOffsetY_num);

		let lBounds_obj = lCrit_anim.getCaptionBounds();
		lBounds_obj.height += 20;
		if (lCrit_anim.position.x > (APP.config.size.width - lBounds_obj.width)) lCrit_anim.position.x = APP.config.size.width - lBounds_obj.width;
		if (lCrit_anim.position.y > (APP.config.size.height - lBounds_obj.height)) lCrit_anim.position.y = APP.config.size.height - lBounds_obj.height;
		if (lCrit_anim.position.x < lBounds_obj.width) lCrit_anim.position.x = lBounds_obj.width;
		if (lCrit_anim.position.y < lBounds_obj.height)lCrit_anim.position.y = lBounds_obj.height;

		lCrit_anim.once(CriticalHitAnimation.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED, ()=>{
			let lId_num = this._fCritAnims_arr.indexOf(lCrit_anim);
			if (~lId_num)
			{
				this._fCritAnims_arr.splice(lId_num, 1);
			}

			this.emit(GameField.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED, {rid: lCrit_anim.rid, enemyId: lCrit_anim.enemyId});
			lCrit_anim && lCrit_anim.destroy();
		});

		this._fCritAnims_arr.push(lCrit_anim);
		lCrit_anim.startAnimation();

		if (lIsMasterPlayer_bl)
		{
			this.emit(GameField.EVENT_ON_CRITICAL_HIT_ANIMATION_START);
		}
		else
		{
			lCrit_anim.alpha = COOP_FIRE_FX_ALPHA;
		}
	}

	getCriticalAnimationById(aRid_num, aEnemyId_num)
	{
		for (let lCrit_anim of this._fCritAnims_arr)
		{
			if ((lCrit_anim.rid === aRid_num) && (lCrit_anim.enemyId === aEnemyId_num))
			{
				return lCrit_anim;
			}
		}

		return null;
	}

	_isAnyCashAwardAnimationRequired(hitData)
	{
		return PrizesController.isAnyCashAwardAnimationRequired(hitData);
	}

	_isMasterScoreAwardRequired(hitData)
	{
		return this.spot && (hitData.score > 0 && hitData.seatId === this.seatId);
	}

	checkExistEnemy(id)
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			if (this.enemies[i].id == id)
			{
				return true;
			}
		}

		return false;
	}

	getExistEnemy(id)
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			if (this.enemies[i].id == id)
			{
				return this.enemies[i];
			}
		}
	}

	get isBossEnemyExist()
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			if (this.enemies[i].isBoss)
			{
				return true;
			}
		}

		return false;
	}

	isBossEnemyExistExpect(aExpectEnemyId_num = -1)
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			if (this.enemies[i].isBoss && this.enemies[i].id != aExpectEnemyId_num)
			{
				return true;
			}
		}

		return false;
	}

	getFirstEverExistedEnemyIdFromTheList(aEnemiesIds_int_arr)
	{
		for (let lEnemyId_int of aEnemiesIds_int_arr)
		{
			if (this.getEnemyPosition(lEnemyId_int))
			{
				return lEnemyId_int;
			}
		}
		return null;
	}

	getEnemyPosition(enemyId, aFootPosition_bl = false)
	{
		let enemy = this.getExistEnemy(enemyId);
		if (enemy)
		{
			return aFootPosition_bl ? enemy.getGlobalPosition() : enemy.getCenterPosition();
		}
		return this.enemiesLastPositions[enemyId];
	}

	getDeadEnemy(id)
	{
		for (var i = 0; i < this.deadEnemies.length; i ++)
		{
			if (this.deadEnemies[i].id == id)
			{
				return this.deadEnemies[i];
			}
		}
	}

	checkExistBullet(id)
	{
		for (var i = 0; i < this.bullets.length; i ++)
		{
			if (this.bullets[i].id == id)
			{
				return true;
			}
		}

		return false;
	}

	getExistBullet(id)
	{
		for (var i = 0; i < this.bullets.length; i ++)
		{
			if (this.bullets[i].id == id)
			{
				return this.bullets[i];
			}
		}
	}

	isMasterBulletExist()
	{
		let lMasterSeatBuletCount_num = this.bullets.filter(function(item) {
			return item.isMasterBullet;
		}).length;

		return lMasterSeatBuletCount_num > 0;
	}

	createEnemy(aEnemy_obj)
	{
		let zombie;
		switch (aEnemy_obj.name)
		{
			case ENEMIES.RedAnt:
			case ENEMIES.BlackAnt:
			case ENEMIES.SnakeStraight:
				zombie = new EightWayEnemy(aEnemy_obj);
				break;
			case ENEMIES.PoisonPlantMaroonViolet:
			case ENEMIES.PoisonPlantYellowPurple:
				zombie = new PoisonPlantEnemy(aEnemy_obj, this);
				break;
			case ENEMIES.CarnivorePlantRed:
			case ENEMIES.CarnivorePlantGreen:
				zombie = new CarnivorePlantEnemy(aEnemy_obj, this);
				break;
			case ENEMIES.Scorpion:
				zombie = new ScorpionEnemy(aEnemy_obj);
				break;
			case ENEMIES.BabyFrog:
				zombie = new BabyFrogEnemy(aEnemy_obj);
				break;
			case ENEMIES.Jaguar:
			case ENEMIES.Skullbreaker:
			case ENEMIES.SkullboneOrb:
				zombie = new SpineEnemy(aEnemy_obj);
				break;
			case ENEMIES.Wasp:
			case ENEMIES.YellowWasp:
			case ENEMIES.VioletWasp:
			case ENEMIES.Firefly:
				zombie = new WaspEnemy(aEnemy_obj);
				break;
			case ENEMIES.Exploder:
				zombie = new FrogEnemy(aEnemy_obj);
				break;
			case ENEMIES.Jumper:
				zombie = new JumperEnemy(aEnemy_obj);
				break;
			case ENEMIES.Witch:
				zombie = new WitchEnemy(aEnemy_obj);
				break;
			case ENEMIES.SkullboneRunner:
				zombie = new RunWithJumpEnemy(aEnemy_obj); //RunWithJumpEnemy(aEnemy_obj);
				break;
			case ENEMIES.BlueOrbArtillerystrike:
				zombie = new BlueOrbAtrillery(aEnemy_obj);
				break;
			case ENEMIES.BlueOrbFlamethrower:
				zombie = new BlueOrbFlamethrower(aEnemy_obj);
				break;
			case ENEMIES.BlueOrbCryogun:
				zombie = new BlueOrbCryogun(aEnemy_obj);
				break;
			case ENEMIES.BlueOrbPlasma:
				zombie = new BlueOrbPlasma(aEnemy_obj);
				break;
			case ENEMIES.BlueOrbLaser:
				zombie = new BlueOrbLaser(aEnemy_obj);
				break;
			case ENEMIES.SpiderBoss:
				zombie = new SpiderBossEnemy(aEnemy_obj);
				break;
			case ENEMIES.ApeBoss:
				zombie = new ApeBossEnemy(aEnemy_obj);
				break;
			case ENEMIES.GolemBoss:
				zombie = new GolemBossEnemy(aEnemy_obj);
				break;
			default:
				zombie = new SpineEnemy(aEnemy_obj); //[Y]TODO to add exception - unknown enemy type
				break;
		}

		this.bottomScreen.addChild(zombie);
		this.enemies.push(zombie);

		this.emit(GameField.EVENT_ON_NEW_ENEMY_CREATED, {enemyId: aEnemy_obj.id});

		if (APP.isAutoFireEnabled && APP.isBattlegroundGame && this.spot && this.spot.autofireButton.enabled && !this._fTargetingInfo_tc.targetEnemyId && !this._fAutoTargetingSwitcherInfo_atsi.isOn)
		{
			this._onEnemyRightClick(zombie);
		}

		if (this._fGameStateInfo_gsi.subroundLasthand)
		{
			if (zombie.isBoss)
			{
				this.emit(GameField.EVENT_ON_NEW_BOSS_CREATED, {enemyId: aEnemy_obj.id, isLasthandBossView: true, bossName: aEnemy_obj.name});
				this._onBossCreated({enemyId: aEnemy_obj.id, isLasthandBossView: true, bossName: aEnemy_obj.name});
			}
		}
		else
		{
			if (zombie.isBoss)
			{
				zombie.prepareForBossAppearance();

				this.emit(GameField.EVENT_ON_NEW_BOSS_CREATED, {enemyId: aEnemy_obj.id, isLasthandBossView: false, bossName: aEnemy_obj.name});
				this._onBossCreated({enemyId: aEnemy_obj.id, isLasthandBossView: false, bossName: aEnemy_obj.name});
			}
		}

		zombie.on(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, this._onEnemyDeathAnimationStarted, this);
		if (zombie.isBoss)
		{
			zombie.on(BossEnemy.EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME, this.emit, this);
			zombie.on(Enemy.EVENT_ON_DEATH_ANIMATION_FLARE, this.emit, this);
			zombie.once(Enemy.EVENT_ON_DEATH_ANIMATION_CRACK, this.emit, this);
			zombie.on(Enemy.EVENT_ON_ENEMY_START_DYING, (e) => {
				this.emit(GameField.EVENT_ON_BOSS_DESTROYING, {bossName: e.bossName, enemy: e.enemy, isInstantKill: e.isInstantKill});

				if (e.isInstantKill)
				{
					Sequence.destroy(Sequence.findByTarget(this.container));
					this.container.x = 0;
					this.container.y = 0;
				}
			});
		}
		zombie.on(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING, this._onEnemyViewRemoving, this);
		//zombie.on(Enemy.EVENT_ON_ENEMY_RIGHT_CLICK, this._onEnemyRightClick, this);
		//zombie.on(Enemy.EVENT_ON_ENEMY_CLICK, this._onEnemyClick, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, this.emit, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_PAUSE_WALKING, this.emit, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_RESUME_WALKING, this.emit, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_FREEZE, this.emit, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_UNFREEZE, this.emit, this);
		zombie.on(Enemy.EVENT_ON_ENEMY_ENERGY_UPDATED, this.emit, this);
		zombie.once(Enemy.EVENT_ON_DEATH_COIN_AWARD, this.emit, this);
		zombie.once(Sprite.EVENT_ON_DESTROYING, this._onEnemyViewDestroying, this);

		if(!this._fGameStateInfo_gsi.isBossSubround)
		{
			zombie.on(BlueOrb.EVENT_ORB_CALLOUT_CREATED, (e) => {
				this.emit(BlueOrb.EVENT_ORB_CALLOUT_CREATED, {data: e.data})
			});
		}

		return zombie;
	}

	getEnemiesBySwarmId(aSwarmId_num)
	{
		let lEnemies_arr = [];
		for (let lEnemy_obj of this.enemies)
		{
			if (lEnemy_obj.swarmId !== null && lEnemy_obj.swarmId == aSwarmId_num)
			{
				lEnemies_arr.push(lEnemy_obj);
			}
		}

		return lEnemies_arr;
	}

	_onEnemyDeathAnimationStarted(event)
	{
		let zombie = event.target;
		if (zombie.isBoss)
		{
			this.shakeTheGround("bossShining");
			zombie.once(Enemy.EVENT_ON_DEATH_ANIMATION_OUTRO_STARTED, (e) => {
				this.emit(GameField.EVENT_ON_BOSS_DESTROYED, {enemyGlobalPoint: e.position});
				this.shakeTheGround("bossExplosion", true);
			});
			zombie.once(Enemy.EVENT_ON_TIME_TO_EXPLODE_COINS, (e) => {
				this._startRemovingBossHealthBar(true);
				this.emit(GameField.EVENT_ON_TIME_TO_EXPLODE_COINS, {enemyGlobalPoint: e.position, isCoPlayerWin: e.isCoPlayerWin});
			});
		}
		this.emit(GameField.EVENT_ON_DEATH_ANIMATION_STARTED, {enemyId:zombie.id});
	}

	_onEnemyViewRemoving(event)
	{
		let zombie = event.target;

		if (zombie.isBoss && Number.isNaN(Number.parseInt(zombie.deathReason)))
		{
			this.emit(GameField.EVENT_ON_BOSS_DESTROYED)
		}

		let enemyId = zombie.id;

		if (this.gunLockTargetedEnemy && this.gunLockTargetedEnemy.id === enemyId)
		{
			this.gunLockTargetedEnemy = null;
		}

		this.enemiesLastPositions[enemyId] = zombie.getGlobalPosition();
		this.pushEnemyToDeadList(enemyId);

		this.emit(GameField.EVENT_ON_ENEMY_VIEW_REMOVING, {enemyId:enemyId});
	}

	_onEnemyViewDestroying(event)
	{
		let zombie = event.target;
		let enemyId = zombie.id;

		if (this.gunLockTargetedEnemy && this.gunLockTargetedEnemy.id === enemyId)
		{
			this.gunLockTargetedEnemy = null;
		}

		let index = this.deadEnemies.indexOf(zombie);
		if (~index)
		{
			this.deadEnemies.splice(index, 1);
		}
	}

	getEnemiesByParentId(aId_num)
	{
		let lEnemies_arr = [];

		for (let enemy of this.enemies)
		{
			if (enemy.parentEnemyId == aId_num)
			{
				lEnemies_arr.push(enemy);
			}
		}

		return lEnemies_arr;
	}

	getEnemy(id)
	{
		for (var enemy of this.enemies)
		{
			if (enemy.id == id)
			{
				return enemy;
			}
		}
	}

	removeAllEnemies()
	{
		while (this.enemies.length)
		{
			try
			{
				this.enemies.pop().destroy(true);
			}
			catch (err)
			{
				console.log("GameField.js :: removeAllEnemies >> ", err);
			}
		}
		this.enemies = [];
		this.enemiesLastPositions = {};

		while (this.deadEnemies.length)
		{
			try
			{
				this.deadEnemies.pop().destroy(true);
			}
			catch (err)
			{
				console.log("GameField.js :: removeAllEnemies (dead) >> ", err);
			}
		}

		this.deadEnemies = [];
	}

	removeEnemy(enemy)
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			var enemyView = this.enemies[i];
			if (enemy.id == enemyView.id)
			{
				enemyView.i_playPreDeathAnimation(); //includes destroy instaMark
				break;
			}
		}
	}

	pushEnemyToDeadList(id)
	{
		if (id !== undefined)
		{
			if (this.indicatedEnemy && this.indicatedEnemy.id == id)
			{
				this.indicatedEnemy = null;
			}
			for (var i = 0; i < this.enemies.length; i ++)
			{
				let zombie = this.enemies[i];
				if (zombie.id == id)
				{
					this.enemiesLastPositions[id] = zombie.getGlobalPosition();
					this.enemies.splice(i, 1);

					this.deadEnemies.push(zombie);
					zombie.once(Enemy.EVENT_ON_DEATH_ANIMATION_COMPLETED, (e) => {
						let index = this.deadEnemies.indexOf(e.target);
						if (~index)
						{
							this.deadEnemies.splice(this.deadEnemies.indexOf(e.target), 1);
						}
					})
					return;
				}
			}
		}
	}

	removeOutsideEnemies()
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			let enemy = this.enemies[i];
			if (enemy.isEnded)
			{
				this.enemies.splice(i, 1);
				i--;
			}
		}
	}

	/**
	 * @param {Number} id
	 * @param {Number} reason:
	 *						0 - as a result of usual death;
	 *						1 - as a result of quick disappearing before/after boss
	 */
	setEnemyDestroy(id, reason = 0)
	{
		let enemy = this.getExistEnemy(id);
		if (enemy)
		{
			enemy.updateLife(0);
			enemy.deathReason = reason;
		}

		if (reason == 1) //immediately
		{
			if (APP.gameScreen.roundFinishSoon && !this._fIsPlantEnemyOutroAnimationOnRoundResultWasInterrupted_bl)
			{
				this._interruptPlantEnemyOutroAnimationOnRoundResultAndInstantDestroy();
			}

			if (enemy)
			{
				enemy.setDeath(true);
				this.pushEnemyToDeadList(id);
			}
			this.emit("removeEnemy", {id:id});
		}
	}

	_interruptPlantEnemyOutroAnimationOnRoundResultAndInstantDestroy()
	{
		this._fIsPlantEnemyOutroAnimationOnRoundResultWasInterrupted_bl = true;

		for (let i = 0; i < this._fEnemiesPlantWithPlayPreDeathAnimation_arr.length; i++)
		{
			let enemy = this.getExistEnemy(this._fEnemiesPlantWithPlayPreDeathAnimation_arr[i]);
			if (enemy)
			{
				enemy.i_interruptPreDeathAnimationAndInstantDestroy();
			}
		}
	}

	drawEnemies(enemies)
	{
		var zombie;

		for (var i = 0; i < enemies.length; i ++)
		{
			let enemyInfo = enemies[i];
			
			if (enemyInfo.angle === undefined)
			{
				continue;
			}

			if (!this.checkExistEnemy(enemyInfo.id))
			{
				if (enemyInfo.life === 0 || enemyInfo.isEnded)
				{
					if (enemyInfo.life === 0)
					{
						//this means that when it's time to add enemy to the screen it's already killed
						this.enemiesLastPositions[enemyInfo.id] = new PIXI.Point(enemyInfo.x, enemyInfo.y);
						this.emit("removeEnemy", {id:enemyInfo.id});
					}

					continue;
				}

				zombie = this.createEnemy(enemyInfo);
			}
			else
			{
				zombie = this.getExistEnemy(enemyInfo.id);

				// IF ENEMY WAS DESTROYED...
				if (enemyInfo.life !== undefined && enemyInfo.life == 0 && !zombie.isBoss)
				{
					if (zombie.life === 0 && zombie.deathReason !== null && !zombie.isDeathInProgress)
					{
						if (zombie.isPlantEnemy)
						{
							this._fEnemiesPlantWithPlayPreDeathAnimation_arr.push(zombie.id);
							zombie.once(Enemy.EVENT_ON_ENEMY_DESTROY, this._onPlantEnemyDestroyed, this);
						}


						if (!zombie.isPlantEnemy)
						{
							zombie.isEnded = true;
						}

						this.emit("removeEnemy", {id:zombie.id});

						zombie.setDeath(false);
						this.pushEnemyToDeadList(zombie.id);

						continue;
					}
				}
				// ...IF ENEMY WAS DESTROYED

				if (
					enemyInfo.prevTurnPoint &&
					enemyInfo.prevTurnPoint.x === enemyInfo.x &&
					enemyInfo.prevTurnPoint.y === enemyInfo.y
				)
				{
					enemyInfo.angle = zombie.angle;
				}

				if (!zombie.trajectoryPositionChangeInitiated)
				{
					zombie.trajectoryPositionChangeInitiated = (zombie.position.x !== (enemyInfo.x - zombie.footPoint.x) || zombie.position.y !== (enemyInfo.y - zombie.footPoint.y))
				}

				if (!zombie.isFrozen && zombie.isStayState &&
					zombie.trajectoryPositionChangeInitiated)
				{
					if (enemyInfo.allowUpdatePosition)
					{
						zombie.setWalk();
					}
					else
					{
						zombie.continueStayedStateAnim();
					}
				}
			}

			if (enemyInfo.typeId == ENEMY_TYPES.EXPLODER)
			{
				this._fFrogDeathDiffTime_num = null;
			}

			zombie.life = enemyInfo.life;
			zombie.invulnerable = enemyInfo.invulnerable;

			if (enemyInfo.life != 0)
			{
				if (enemyInfo.isEnded)
				{
					if (zombie.isPlantEnemy)
					{
						this._fEnemiesPlantWithPlayPreDeathAnimation_arr.push(zombie.id);
						zombie.once(Enemy.EVENT_ON_ENEMY_DESTROY, this._onPlantEnemyDestroyed, this);
					}

					if (zombie.isBoss && !this.isBossEnemyExistExpect(zombie.id))
					{
						this._startRemovingBossHealthBar(true);
					}
					zombie.i_playPreDeathAnimation();
					if (!zombie.isPlantEnemy)
					{
						zombie.visible = false;
						zombie.isEnded = true;
					}
					continue;
				}

				zombie.visible = !enemies[i].isHidden;
			}

			if (zombie.isBoss || (zombie.trajectory && zombie.trajectory.points && zombie.trajectory.points[0].portal))
			{
				zombie.isFireDenied = enemyInfo.isFirstStep;
			}

			zombie.upateCurrentNearbyTrajectoryPoints(enemyInfo.prevTurnPoint, enemyInfo.nextTurnPoint);

			zombie.angle = enemyInfo.angle;
			if (!zombie.appearancePositionUpdated)
			{
				zombie.position.set(enemyInfo.x - zombie.footPoint.x, enemyInfo.y - zombie.footPoint.y);

				let lOptJumpOffset_num = zombie.jumpOffset;
				if (lOptJumpOffset_num)
				{
					zombie.position.y += lOptJumpOffset_num;
				}
			}

			zombie.position.x += zombie.getPositionOffsetX();
			zombie.position.y += zombie.getPositionOffsetY();

			zombie.lastPointTimeInterval = enemyInfo.lastPointTimeInterval;
			zombie.changeView();
			zombie.changeZindex();
		}
	}

	_onPlantEnemyDestroyed(e)
	{
		let index = this._fEnemiesPlantWithPlayPreDeathAnimation_arr.indexOf(e.id);
		if (~index)
		{
			this._fEnemiesPlantWithPlayPreDeathAnimation_arr.splice(this._fEnemiesPlantWithPlayPreDeathAnimation_arr.indexOf(e.id), 1);
		}
	}

	fireImmediatelyIfRequired()
	{
		if (this._fireSettingsInfo.autoFire)
		{
			if (
					(this._fTargetingInfo_tc.isActive && !!this.autoTargetingEnemy)
					|| this.pointerPushed
				)
			{
				this._isAutoTargetingEnemyAvailableForFire && this.fireImmediately();
			}
		}

	}

	fireImmediately()
	{
		this.fire({
			data: {
				global:{
					x: this.lastPointerPos.x,
					y: this.lastPointerPos.y,
				}
			}
		});
	}

	get isAutoFireEnabled()
	{
		return (this._fTargetingInfo_tc.isActive || this._fTargetingInfo_tc.isActiveTargetPaused) && this._fireSettingsInfo.autoFire;
	}

	thisCursorMove(delta)
	{
		let isUpDown = APP.keyboardControlProxy.isUpDown;
		let isDownDown = APP.keyboardControlProxy.isDownDown;

		if (isUpDown || isDownDown)
		{
			let timeout = this._turretKeysMoveTimeout;
			this.cursorKeysMoveTime += delta;

			if (this.cursorKeysMoveTime >= timeout)
			{
				this.cursorKeysMoveTime = 0;

				if (isUpDown)
				{
					this._tryMoveCursorUp();
				}
				if (isDownDown)
				{
					this._tryMoveCursorDown();
				}
			}
		}
	}

	tickTurretRotation(delta)
	{
		let isLeftDown = APP.keyboardControlProxy.isLeftDown;
		let isRightDown = APP.keyboardControlProxy.isRightDown;

		if (isLeftDown || isRightDown)
		{
			let timeout = this._turretRotationTimeout;
			this.turretRotationTime += delta;

			if (this.turretRotationTime >= timeout)
			{
				this.turretRotationTime = 0;

				if (isRightDown)
				{
					this._tryRotateTurretRight();
				}
				if (isLeftDown)
				{
					this._tryRotateTurretLeft();
				}
			}
		}
	}

	tickAutoFire(delta)
	{
		if (
				(this.pointerPushed || this._fTargetingInfo_tc.isActive || APP.keyboardControlProxy.isSpaceDown) &&
				this._fireSettingsInfo.autoFire
			)
		{
			let timeout = this._autoFireTimeout;
			this.autofireTime += delta;

			if (this.autofireTime >= timeout)
			{
				this.autofireTime = 0;
				this.chooseFireType();
			}
		}
	}

	tickBullets(delta, realDelta)
	{
		let lDelta_num = realDelta;

		for (let bullet of this.bullets)
		{
			if (
				bullet.isConfirmedOrDeniedByServer() &&
				!bullet.isRequired()
				)
			{
				this.removeBullet(bullet);
			}

			//NON RICOCHET BULLETS...
			if(!bullet.isRicochetBullet())
			{
				bullet.tick(delta, realDelta);
			}
			//...NON RICOCHET BULLETS
		}


		//RICOCHET BULLETS...
		while(lDelta_num > 0)
		{
			let lStepDelta_num = 16;

			if(lDelta_num < lStepDelta_num)
			{
				lStepDelta_num = lDelta_num;
			}

			lDelta_num -= lStepDelta_num;

			for (let bullet of this.bullets)
			{

				if(
					bullet.isRicochetBullet() &&
					bullet.isRequired()
					)
				{
					if(bullet.lasthand)
					{
						bullet.tick(lStepDelta_num, lStepDelta_num);
						this.ricochetController.onBulletMove(bullet);
					}
					bullet.tick(lStepDelta_num, lStepDelta_num);
					this.ricochetController.onBulletMove(bullet);

					//CHECK COLLISIONS...
					if(bullet.isRequired() && !bullet.lasthand)
					{
						for( let i = 0; i < this.enemies.length; i++ )
						{
							let lEnemy_e = this.enemies[i];

							if(lEnemy_e.shadow)
							{
								if(
									bullet.isRequired() &&
									!lEnemy_e.isBehindWall() &&
									lEnemy_e.isTargetable() &&
									lEnemy_e.isCollision(
										bullet.position.x,
										bullet.position.y)
									)
								{
									bullet.onCollisionOccurred(lEnemy_e);
									this._onCollisionOccurred(bullet, lEnemy_e);
									bullet.setIsRequired(false);
									break;
								}
							}
						}
					}
					//...CHECK COLLISIONS
				}
			}
		}
		//...RICOCHET BULLETS
	}

	tickEmitters(delta)
	{
		if (this.emitter)
		{
			this.emitter.update(delta/1000);
		}
	}

	removeBullet(bullet)
	{
		if (!this.bullets)
		{
			return;
		}

		if(bullet.isRicochetBullet())
		{
			bullet.tryToCompleteShotIfCollisionHappenedBeforeBulletWasApprovedByServer();
		}

		let bulletIndex = this.bullets.indexOf(bullet);
		if (bulletIndex >= 0)
		{
			let bullet = this.bullets.splice(bulletIndex, 1)[0];
		}

		if(bullet.isRicochetBullet())
		{
			this.ricochetController.onBulletDestroy(bullet);
		}

		if (!this.isMasterBulletExist())
		{
			this.checkingNeedChangeBetLevelAfterFiring();
		}

		if(bullet)
		{
			bullet.destroy();
		}
	}

	removeAllBullets()
	{
		while (this.bullets.length)
		{
			try
			{
				this.bullets.shift().destroy();
			}
			catch (err)
			{
				console.log("GameField.js :: removeAllBullets >> ", err);
			}
		}
		this.bullets = [];
	}

	rotateLockedGun()
	{
		let enemyPos;

		if (this.gunLocked && this.gunLockTargetedEnemy && this.gunLockTargetedEnemy.parent)
		{
			enemyPos = this.gunLockTargetedEnemy.getCenterPosition();
		}

		if (enemyPos)
		{
			this.rotateGun(enemyPos.x, enemyPos.y);
		}
	}

	tickEnemies(delta)
	{
		for (let enemy of this.enemies)
		{
			enemy.tick(delta);
		}

		for (let enemy of this.deadEnemies)
		{
			enemy.i_hitHiglightUpdate(delta);
		}
	}

	tick(delta, realDelta)
	{
		this.tickAutoFire(delta);
		this.tickEmitters(delta);
		this.rotateLockedGun();
		this.tickEnemies(delta);

		this.tickBullets(delta, realDelta);

		this.tickTurretRotation(delta);
		this.thisCursorMove(delta);
		this._mapScaleTickUpdate();
		this.spot && this.spot.tick && this.spot.tick(delta);

		if (this._fCommonPanelIndicatorsData_obj)
		{
			let lCommonPanelIndicatorsData_obj = Object.assign({}, this._fCommonPanelIndicatorsData_obj);
			this._fCommonPanelIndicatorsData_obj = null;

			this.emit(GameField.EVENT_REFRESH_COMMON_PANEL_REQUIRED, { data:lCommonPanelIndicatorsData_obj } );
		}
	}

	//PENDING_OPERATION...
	_onPendingOperationStarted(event)
	{
		this.resetWaitBuyIn();
	}

	_onPendingOperationCompleted(event)
	{
		const lPlayerInfo_pi = APP.playerController.info;
		if (lPlayerInfo_pi.isMasterServerSeatIdDefined)
		{
			this._tryToBuyAmmoFromRoundResult();
		}
	}
	//...PENDING_OPERATION
}

export default GameField;