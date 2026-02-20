import
{
	DIALOG_ID_INSUFFICIENT_FUNDS,
	DIALOG_ID_FRB,
	DIALOG_ID_BONUS,
	DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN, DIALOG_ID_MID_ROUND_EXIT } from '../../../shared/src/CommonConstants';

import { APP } from '../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import GameField from './GameField';
import Game from '../Game';
import { WEAPONS, ENEMIES, ENEMY_TYPES, ENEMY_BOSS_SKINS, ENEMY_ANT_SKINS, ENEMY_WASP_SKINS, POWER_UP_WEAPONS } from '../../../shared/src/CommonConstants';
import AwardingController from '../controller/uis/awarding/AwardingController';
import GameStateController from '../controller/state/GameStateController';
import MapsController from '../controller/uis/maps/MapsController';
import TreasuresController from '../controller/uis/treasures/TreasuresController';
import BossModeController from '../controller/uis/custom/bossmode/BossModeController';
import BossModeView from '../view/uis/custom/bossmode/BossModeView';
import BossModeInfo from '../model/custom/bossmode/BossModeInfo';
import SubloadingController from '../controller/subloading/SubloadingController';
import GameWebSocketInteractionController from '../controller/interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../model/interaction/server/GameWebSocketInteractionInfo';
import I18 from '../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import PlayerInfo from '../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import HvEnemiesController from '../controller/uis/hv_enemies/HvEnemiesController';
import { Sequence } from '../../../../common/PIXI/src/dgphoenix/unified/controller/animation/index';
import * as Easing from '../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import GameExternalCommunicator from '../external/GameExternalCommunicator';
import {LOBBY_MESSAGES, GAME_MESSAGES} from '../external/GameExternalCommunicator';
import {ROUND_STATE} from '../model/state/GameStateInfo';
import WeaponsController from '../controller/uis/weapons/WeaponsController';
import FireSettingsController from '../controller/uis/fire_settings/FireSettingsController';
import InfoPanelController from '../controller/uis/info_panel/InfoPanelController';
import InfoPanelView from '../view/uis/info_panel/InfoPanelView';
import TargetingController from '../controller/uis/targeting/TargetingController';
import RoundResultScreenController from '../controller/uis/roundresult/RoundResultScreenController';
import Timer from "../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import AutoTargetingSwitcherController from '../controller/uis/targeting/AutoTargetingSwitcherController';
import { Utils } from '../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import GamePlayerController from '../controller/custom/GamePlayerController';
import GameOptimizationController from '../controller/custom/GameOptimizationController';
import ShotResponsesController from '../controller/custom/ShotResponsesController';
import GameBuyAmmoRetryingController from '../controller/custom/GameBuyAmmoRetryingController';
import CryogunsController from '../controller/uis/weapons/cryogun/CryogunsController';
import FlameThrowersController from '../controller/uis/weapons/flamethrower/FlameThrowersController';
import ArtilleryStrikesController from '../controller/uis/weapons/artillerystrike/ArtilleryStrikesController';
import PlazmaGunsController from '../controller/uis/weapons/plazmagun/PlazmaGunsController';
import RicochetLasersController from '../controller/uis/weapons/ricochet_laser/RicochetLasersController';
import TrajectoryUtils from './TrajectoryUtils';
import CursorController from '../controller/interaction/cursor/CursorController';
import PrizesController, { HIT_RESULT_SINGLE_CASH_ID } from '../controller/uis/prizes/PrizesController';
import SpecialWeaponPrizesController from '../controller/uis/prizes/SpecialWeaponPrizesController';
import GameBonusController from '../controller/uis/custom/bonus/GameBonusController';
import GameFRBController from './../controller/uis/custom/frb/GameFRBController';
import BigWinsController from '../controller/uis/awarding/big_win/BigWinsController';
import CursorAnimation from './animation/CursorAnimation';
import TransitionViewController from '../controller/uis/transition/TransitionViewController';
import Enemy from './enemies/Enemy';
import BattlegroundGameController from '../controller/uis/battleground/BattlegroundGameController';
import BattlegroundCountDownDialogController from '../controller/uis/custom/gameplaydialogs/custom/BattlegroundCountDownDialogController';
import GameplayDialogsController from './../controller/uis/custom/gameplaydialogs/GameplayDialogsController';
import GamePleaseWaitDialogController from '../controller/uis/custom/GamePleaseWaitDialogController';
import GamePendingOperationController from '../controller/custom/GamePendingOperationController';
import CafPrivateRoundCountDownDialogController from '../controller/uis/custom/gameplaydialogs/custom/CafPrivateRoundCountDownDialogController';

//Last server date response
var serverTime = Date.now();
var accurateServerTime = Date.now();
var serverTimeInitiated = false;
var clientTimeDiff = 0;
var accurateClientTimeDiff = 0;
var serverLastMessageRecieveTime = undefined;
var accurateServerLastMessageRecieveTime = undefined;

var lastTickOccurTime = undefined;

var testMode = false;
var testAngleId = -1;

const MAX_TICK_DELAY = 3000;

class GameScreen extends Sprite
{
	static get EVENT_BATTLEGROUND_REQUEST_TIME_TO_START()					{return "EVENT_BATTLEGROUND_REQUEST_TIME_TO_START";}
	static get EVENT_BATTLEGROUND_CONFIRM_BUY_IN_REQUIRED()					{return "EVENT_BATTLEGROUND_CONFIRM_BUY_IN_REQUIRED";}
	static get EVENT_ON_READY()												{return "onGameScreenReady";}
	static get EVENT_ON_FORCE_SIT_OUT_REQUIRED()							{return "onForceSitOutRequired";}
	static get EVENT_ON_WIN_TO_AMMO_TRANSFERED()							{return "onWinToAmmoTransfered";}
	static get EVENT_ON_CLOSE_ROOM()										{return "onCloseRoom";}
	static get EVENT_ON_NEW_ROUND_STATE()									{return "onNewRoundState";}
	static get EVENT_ON_WAITING_NEW_ROUND()									{return "onWaitingNewRound";}
	static get EVENT_ON_ROOM_INFO_UPDATED()									{return "onRoomInfoUpdated";}
	static get EVENT_ON_NEXT_MAP_UPDATED()									{return "onNextMapUpdated";}
	static get EVENT_ON_HEALTH_BAR_STATE_UPDATED()							{return "onHealthBarStateUpdated";}
	static get EVENT_ON_CHOOSE_WEAPON_SCREEN_CHANGED()						{return "onWeaponsChooseScreenActivated";}
	static get EVENT_MID_ROUND_COMPENSATE_SW_REQUIRED()						{return "onMidRoundCompensateSWExitRequired";}
	static get EVENT_MID_ROUND_EXIT_REQUIRED()								{return "onMidRoundExitRequired";}
	static get EVENT_ON_BET_LEVEL_CHANGE_CONFIRMED()						{return "onBetLevelChangeConfirmed";}
	static get EVENT_ON_BET_LEVEL_CHANGE_NOT_CONFIRMED()					{return "onBetLevelChangeNotConfirmed";}
	static get EVENT_ON_FRB_ENDED_COMPLETED() 								{return "onFRBEndedCompleted";}
	static get EVENT_ROUND_RESULT_RETURN_SW_RESPONSE()						{return "onRoundResultReturnSWResponse";}
	static get EVENT_ROUND_RESULT_RETURN_SW_DIALOG_SHOW_VALIDATE()			{return "onRoundResultReturnSWDialogShowValidate";}
	static get EVENT_ON_BULLET()											{return "onBullet";}
	static get EVENT_ON_BULLET_RESPONSE()									{return "onBulletResponse";}
	static get EVENT_ON_BULLET_CLEAR_RESPONSE()								{return "onBulletClearResponse";}
	static get EVENT_ON_CO_PLAYER_COLLISION_OCCURED()						{return "onCoPlayerCollisionOccured";}
	static get EVENT_ON_LASTHAND_BULLETS()									{return "onLasthandBullets";}
	static get EVENT_ON_BULLET_PLACE_NOT_ALLOWED()							{return "onBulletPlaceNotAllowed";}
	static get EVENT_ON_SHOT_RESPONSE_OCCURED()								{return "onShotResponseOccured";}

	static get EVENT_ON_GAME_SOCKET_URL_UPDATED()							{return 'EVENT_ON_GAME_SOCKET_URL_UPDATED';}
	static get EVENT_ON_ROOM_ID_CHANGED()									{return 'EVENT_ON_ROOM_ID_CHANGED';}
	static get EVENT_ON_GAME_RESTORED_AFTER_UNSEASONABLE_REQUEST()			{return 'EVENT_ON_GAME_RESTORED_AFTER_UNSEASONABLE_REQUEST';}
	static get EVENT_ON_GAME_RESTORE_AFTER_UNSEASONABLE_REQUEST_CANCELED()	{return 'EVENT_ON_GAME_RESTORE_AFTER_UNSEASONABLE_REQUEST_CANCELED';}
	static get EVENT_ON_URL_BASED_PLAYER_STAKE_CHANGED()					{return 'EVENT_ON_URL_BASED_PLAYER_STAKE_CHANGED';}
	static get EVENT_ON_FULL_GAME_INFO_REQUIRED()							{return 'EVENT_ON_FULL_GAME_INFO_REQUIRED';}
	static get EVENT_ON_CLOSE_ROOM_REQUIRED()								{return 'EVENT_ON_CLOSE_ROOM_REQUIRED';}
	static get EVENT_ON_SIT_IN_REQUIRED()									{return 'EVENT_ON_SIT_IN_REQUIRED';}
	static get EVENT_ON_SIT_OUT_REQUIRED()									{return 'EVENT_ON_SIT_OUT_REQUIRED';}
	static get EVENT_ON_SHOT_TRIGGERED()									{return 'EVENT_ON_SHOT_TRIGGERED';}
	static get EVENT_ON_BUY_IN_REQUIRED()									{return 'EVENT_ON_BUY_IN_REQUIRED';}
	static get EVENT_ON_RE_BUY_REQUIRED()									{return 'EVENT_ON_RE_BUY_REQUIRED';}
	static get EVENT_ON_CHANGE_STAKE_REQUIRED()								{return 'EVENT_ON_CHANGE_STAKE_REQUIRED';}
	static get EVENT_ON_GAME_FIELD_CLEARED()								{return 'EVENT_ON_GAME_FIELD_CLEARED';}
	static get EVENT_ON_NEW_HV_ENEMY()										{return 'EVENT_ON_NEW_HV_ENEMY';}
	static get EVENT_ON_HV_ENEMY_PARENT_HIT()								{return 'EVENT_ON_HV_ENEMY_PARENT_HIT';}
	static get EVENT_DESTROY_ENEMY()										{return 'EVENT_DESTROY_ENEMY';}
	static get EVENT_ON_WEAPON_SHOTS_UPDATED()								{return 'EVENT_ON_WEAPON_SHOTS_UPDATED';}
	static get EVENT_ON_REVERT_AMMO_BACK()									{return 'EVENT_ON_REVERT_AMMO_BACK';}
	static get EVENT_ON_KILLED_MISS_ENEMY()									{return "EVENT_ON_KILLED_MISS_ENEMY";}
	static get EVENT_ON_INVULNERABLE_ENEMY()								{return "EVENT_ON_INVULNERABLE_ENEMY";}
	static get EVENT_ON_WEAPON_AWARDED()									{return 'EVENT_ON_WEAPON_AWARDED';}
	static get EVENT_ON_TIME_TO_HANDLE_AWARDED_WEAPON() 					{return 'EVENT_ON_TIME_TO_HANDLE_AWARDED_WEAPON';}
	static get EVENT_ON_WEAPON_BOUGHT()										{return 'EVENT_ON_WEAPON_BOUGHT';}
	static get EVENT_ON_ROOM_PAUSED()										{return 'EVENT_ON_ROOM_PAUSED';}
	static get EVENT_ON_ROOM_UNPAUSED()										{return 'EVENT_ON_ROOM_UNPAUSED';}
	static get EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED()					{return 'EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED';}
	static get EVENT_ON_DIALOG_ACTIVATED()									{return 'EVENT_ON_DIALOG_ACTIVATED';}
	static get EVENT_ON_DIALOG_DEACTIVATED()								{return 'EVENT_ON_DIALOG_DEACTIVATED';}
	static get EVENT_ON_TRAJECTORIES_UPDATED()								{return 'EVENT_ON_TRAJECTORIES_UPDATED';}
	static get EVENT_ON_PLAYER_REMOVED()									{return 'EVENT_ON_PLAYER_REMOVED';}
	static get EVENT_ON_PLAYER_ADDED()										{return 'EVENT_ON_PLAYER_ADDED';}
	static get EVENT_ON_ROUND_ID_UPDATED()									{return 'EVENT_ON_ROUND_ID_UPDATED';}
	static get EVENT_ON_LOBBY_BACKGROUND_FADE_START()						{return 'EVENT_ON_LOBBY_BACKGROUND_FADE_START';}
	static get EVENT_ON_BOMB_ENEMY_KILLED()									{return 'EVENT_ON_BOMB_ENEMY_KILLED';}
	static get EVENT_ON_TICK_OCCURRED()										{return 'EVENT_ON_TICK_OCCURRED';}
	static get EVENT_ON_SHOT_RESPONSE_PARSED()								{return 'EVENT_ON_SHOT_RESPONSE_PARSED';}

	static get EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED()						{return GameField.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED;}
	static get EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED()			{return GameField.EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED;}
	static get EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED()						{return GameField.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED;}
	static get EVENT_SOUND_ON_BUTTON_CLICKED()								{return GameField.EVENT_SOUND_ON_BUTTON_CLICKED;}
	static get EVENT_SOUND_OFF_BUTTON_CLICKED()								{return GameField.EVENT_SOUND_OFF_BUTTON_CLICKED;}
	static get EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED()					{return GameField.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED;}
	static get EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED()						{return GameField.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED;}
	static get EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED()			{return GameField.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED;}
	static get EVENT_ON_GAME_ROUND_STATE_CHANGED()							{return GameStateController.EVENT_ON_GAME_ROUND_STATE_CHANGED;}
	static get EVENT_ON_DRAW_MAP()											{return GameField.EVENT_ON_DRAW_MAP;}
	static get EVENT_ON_WEAPON_UPDATED()									{return GameField.EVENT_ON_WEAPON_UPDATED;}
	static get EVENT_REFRESH_COMMON_PANEL_REQUIRED()						{return GameField.EVENT_REFRESH_COMMON_PANEL_REQUIRED;}
	static get EVENT_ON_NEW_BOSS_CREATED()									{return GameField.EVENT_ON_NEW_BOSS_CREATED;}
	static get EVENT_ON_BOSS_DESTROYING()									{return GameField.EVENT_ON_BOSS_DESTROYING;}
	static get EVENT_ON_BOSS_DESTROYED()									{return GameField.EVENT_ON_BOSS_DESTROYED;}
	static get EVENT_ON_BOSS_DEATH_FLARE()									{return GameField.EVENT_ON_BOSS_DEATH_FLARE;}
	static get EVENT_ON_BOSS_DEATH_CRACK()									{return GameField.EVENT_ON_BOSS_DEATH_CRACK;}
	static get EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME()						{return GameField.EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME;}
	static get EVENT_ON_TIME_TO_EXPLODE_COINS()								{return GameField.EVENT_ON_TIME_TO_EXPLODE_COINS;}
	static get EVENT_DECREASE_AMMO()										{return GameField.EVENT_DECREASE_AMMO;}
	static get EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING()			{return GameField.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING;}
	static get EVENT_ON_TARGETING()											{return GameField.EVENT_ON_TARGETING;}
	static get EVENT_ON_RESET_TARGET()										{return GameField.EVENT_ON_RESET_TARGET;}
	static get EVENT_ON_GAME_FIELD_SCREEN_CREATED()							{return GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED;}
	static get EVENT_ON_ROOM_FIELD_CLEARED()								{return GameField.EVENT_ON_ROOM_FIELD_CLEARED;}
	static get EVENT_ON_TARGET_ENEMY_IS_DEAD()								{return GameField.EVENT_ON_TARGET_ENEMY_IS_DEAD;}
	static get EVENT_ON_HIT_AWARD_EXPECTED()								{return GameField.EVENT_ON_HIT_AWARD_EXPECTED;}
	static get EVENT_ON_SHOT_SHOW_FIRE_START_TIME()							{return GameField.EVENT_ON_SHOT_SHOW_FIRE_START_TIME;}
	static get EVENT_ON_GUN_UNLOCKED()										{return GameField.EVENT_ON_GUN_UNLOCKED;}
	static get EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME()				{return GameField.EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME;}
	static get EVENT_ON_BULLET_TARGET_TIME()								{return GameField.EVENT_ON_BULLET_TARGET_TIME;}
	static get EVENT_ON_BET_MULTIPLIER_UPDATED()							{return GameField.EVENT_ON_BET_MULTIPLIER_UPDATED;}
	static get EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED()					{return GameField.EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED;}
	static get EVENT_ON_TIME_TO_RESTART_BONUS_ROOM() 						{return "EVENT_ON_TIME_TO_RESTART_BONUS_ROOM";}
	static get EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS() 				{return GameBonusController.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS;}
	static get EVENT_ON_BULLET_CLEAR()										{return GameField.EVENT_ON_BULLET_CLEAR;}
	static get EVENT_ON_GREEN_CLOUD_APPEARED()								{return GameField.EVENT_ON_GREEN_CLOUD_APPEARED;}
	static get EVENT_ON_EMPTY_ARMOR_GLOWED()								{return GameField.EVENT_ON_EMPTY_ARMOR_GLOWED;}
	static get EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED()					{return GameField.EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED;}

	static get EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED()			{return "EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED"; }
	static get EVENT_BATTLEGROUND_TIME_TO_START_UPDATED()					{return "EVENT_BATTLEGROUND_TIME_TO_START_UPDATED"; }
	static get EVENT_ON_SERVER_MESSAGE_GAME_STATE_CHANGED()					{return "EVENT_ON_SERVER_MESSAGE_GAME_STATE_CHANGED"; }
	static get EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED() 						{return "EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED"; }
	static get EVENT_ON_BATTLEGROUND_ROUND_CANCELED() 						{return "EVENT_ON_BATTLEGROUND_ROUND_CANCELED"; }

	static get EVENT_ON_UPDATE_PLAYER_WIN_CAPTION()							{return GameField.EVENT_ON_UPDATE_PLAYER_WIN_CAPTION;}
	static get EVENT_ON_CURRENT_POWER_UP_MULTIPLIER_UPDATED() 				{return "EVENT_ON_CURRENT_POWER_UP_MULTIPLIER_UPDATED";}

	static get EVENT_ON_DEATH_COIN_AWARD()									{return GameField.EVENT_ON_DEATH_COIN_AWARD;}

	static get EVENT_ON_AUTOFIRE_BUTTON_ENABLED()							{ return GameField.EVENT_ON_AUTOFIRE_BUTTON_ENABLED; }
	static get EVENT_ON_AUTOFIRE_BUTTON_DISABLED()							{ return GameField.EVENT_ON_AUTOFIRE_BUTTON_DISABLED; }
	static get EVENT_ON_NO_EMPTY_SEATS()									{ return "EVENT_ON_NO_EMPTY_SEATS"; }
	static get EVENT_ON_PLAY_AGAIN_REQUIRED()								{ return "EVENT_ON_PLAY_AGAIN_REQUIRED"; }
	static get EVENT_ON_BTG_ROUND_OBSERVER_DENIED()							{ return "EVENT_ON_BTG_ROUND_OBSERVER_DENIED"; }

	static calculateEnemyName(typeId, skin)
	{
		let lName_str;
		switch(typeId)
		{
			case ENEMY_TYPES.WITCH:				lName_str = ENEMIES.Witch;				break;
			case ENEMY_TYPES.SNAKE: 				lName_str = ENEMIES.SnakeStraight;		break;
			case ENEMY_TYPES.JUMPER:				lName_str = ENEMIES.Jumper;				break;
			case ENEMY_TYPES.EXPLODER:				lName_str = ENEMIES.Exploder;			break;
			case ENEMY_TYPES.SKULLBONE_RUNNER:		lName_str = ENEMIES.SkullboneRunner;	break;
			case ENEMY_TYPES.JAGUAR:				lName_str = ENEMIES.Jaguar;				break;
			case ENEMY_TYPES.SKULLBREAKER:			lName_str = ENEMIES.Skullbreaker;		break;
			case ENEMY_TYPES.SCORPION:				lName_str = ENEMIES.Scorpion;			break;
			case ENEMY_TYPES.TINY_TOAD:				lName_str = ENEMIES.BabyFrog;			break;
			case ENEMY_TYPES.SKULLBONE_ORB:			lName_str = ENEMIES.SkullboneOrb;		break;

			case ENEMY_TYPES.WASP:
				switch (skin)
				{
					case ENEMY_WASP_SKINS.WASP:			lName_str = ENEMIES.Wasp;			break;
					case ENEMY_WASP_SKINS.VIOLET_WASP:	lName_str = ENEMIES.VioletWasp;		break;
					case ENEMY_WASP_SKINS.YELLOW_WASP:	lName_str = ENEMIES.YellowWasp;		break;
					case ENEMY_WASP_SKINS.FIREFLY:		lName_str = ENEMIES.Firefly;		break;
					default: throw new Error ("Unexpected wasp skin: " + skin);
				}
				break;

			case ENEMY_TYPES.SPIDERLING:
				switch (skin)
				{
					case ENEMY_ANT_SKINS.RED_ANT:		lName_str = ENEMIES.RedAnt;			break;
					case ENEMY_ANT_SKINS.BLACK_ANT:		lName_str = ENEMIES.BlackAnt;		break;
					default: throw new Error ("Unexpected ant skin: " + skin);
				}
				break;
			case ENEMY_TYPES.POISON_PLANT_YELLOW_PURPLE:
				lName_str = ENEMIES.PoisonPlantYellowPurple;
				break;
			case ENEMY_TYPES.POISON_PLANT_GREEN_PINK:
				lName_str = ENEMIES.PoisonPlantMaroonViolet; //ENEMIES.EmptyArmorSilver;
				break;
			case ENEMY_TYPES.CARNIVORE_PLANT_RED:
				lName_str = ENEMIES.CarnivorePlantRed; //ENEMIES.EmptyArmorBlue;
				break;
			case ENEMY_TYPES.CARNIVORE_PLANT_GREEN:
				lName_str = ENEMIES.CarnivorePlantGreen; //ENEMIES.EmptyArmorGold;
				break;
			case ENEMY_TYPES.GREEN_IMP:
				lName_str = ENEMIES.SnakeStraight; //ENEMIES.GreenImp;
				break;
			case ENEMY_TYPES.OGRE:
				lName_str = ENEMIES.Ogre;
				break;
			case ENEMY_TYPES.BOSS:
				switch (skin)
				{
					case ENEMY_BOSS_SKINS.SPIDER: 	lName_str = ENEMIES.SpiderBoss; break;
					case ENEMY_BOSS_SKINS.GOLEM: 	lName_str = ENEMIES.GolemBoss; break;
					case ENEMY_BOSS_SKINS.APE: 		lName_str = ENEMIES.ApeBoss; break;
					default: throw new Error ("Unexpected Boss skin: " + skin);
				}
				break;
			case ENEMY_TYPES.HOBGOBLIN:
				lName_str = ENEMIES.SnakeStraight; //ENEMIES.EmptyArmorGold;
				break;
			case ENEMY_TYPES.BLUE_ORB_ARTILLERYSTRIKE:
				lName_str = ENEMIES.BlueOrbArtillerystrike;
				break;
			case ENEMY_TYPES.BLUE_ORB_FLAMETHROWER:
				lName_str = ENEMIES.BlueOrbFlamethrower;
				break;
			case ENEMY_TYPES.BLUE_ORB_CRYOGUN:
				lName_str = ENEMIES.BlueOrbCryogun;
				break;
			case ENEMY_TYPES.BLUE_ORB_LASER:
				lName_str = ENEMIES.BlueOrbLaser;
				break;
			case ENEMY_TYPES.BLUE_ORB_INSTAKILL:
				lName_str = ENEMIES.BlueOrbPlasma;
				break;
			default: throw new Error ("Unexpected enemy typeId " + typeId);
		}

		return lName_str;
	}

	constructor()
	{
		super();
		this._fIsBattlegroundMode_bl = false;
		this.enemies = [];

		this.getRoomParams = null;

		this.room = null;
		this.player = {
			currency: "UAH",
			nickname: "Alex",
			stake: 1
		};

		this.massDamageAffectedEnemies = [];

		this._fTreasureController_tc = null;
		this._fGameStateController_gsc = null;
		this._fBossModeController_bmc = null;
		this._fMapsController_msc = null;
		this._fSubloadingController_sc = null;
		this._fHvEnemiesController_hesc = null;
		this._fWeaponsController_fwsc = null;
		this._fFireSettingsController_fsc = null;
		this._fInfoPanelController_ipc = null;
		this._fInfoPanelView_ipv = null;
		this._fTargetingController_tc = null;
		this._fAutoTargetingSwitcherController_atsc = null;
		this._fGameOptimizationController_goc = null;
		this._fCryogunsController_csc = null;
		this._fFlameThrowersController_ftsc = null;
		this._fArtilleryStrikesController_assc = null;
		this._fPlazmaGunsController_pgsc = null;
		this._fRicochetLasersController_rlsc = null;
		this._fCursorController_cc = null;
		this._fPrizesController_psc = null;
		this._fShotResponsesController_srsc = null;
		this._fSpecialWeaponPrizesController_swpsc = null;
		this._fBuyAmmoRetryingController_barc = null;
		this._fCompensationDialogActive_bln = false;
		this._fGameBonusController_gbc = null;
		this._fBigWinsController_bwsc = null;
		this._fTransitionViewController_tvc = null;

		this._fStartGameURL_str = null;
		this._fIsPaused_bl = false;
		this._fResumeInProcess_bl = true;
		this._fGameFieldCapture_sprt = null;
		this._fBackToLobbyRequired_bl = false;
		this._fNeedUpdateStartGameUrl_bl = true;

		this._restoreAfterUnseasonableRequest = false;
		this._restoreAfterLagsInProgress = false;

		this._roomUnpauseTimer = null;
		this._screenShowTimer = null;

		this._ready = false;

		this._fForseSitOutInProgress = false;

		this._fHealthBarEnabled_bln = null;

		this._fEnemyIdToRemoveFurther_num = null;

		this._fRoundFinishSoon_bl = false;

		this._fBonusEndedPostponedData_obj = null;
		this._fFrbEndedPostponedData_obj = null;
		this._fBonusRoundEndedPostponedData_obj = null;
		this._fPostponedSitOutData_obj = null;

		this._fTournamentModeInfo = null;

		this._fIsExpectedBuyIn_bln = null;
		this._fIsSitOutOnNotEnoughPlayersExpected_bl = null;
		this._fIsCountDownCanceled = null;

		this._fBattlegroundGameController_bgc = null;

		this._fIsFullGameInfoUpdateExpected_bl = null;

		this._fMasterSWAwardsExpected_obj_arr = [];
	}

	init(aOptRoomUrl_str=undefined)
	{
		//init controllers...
		this.mapsController.i_init();
		this.subloadingController.i_init();
		this.hvEnemiesController.i_init();
		this.weaponsController.i_init();
		this.fireSettingsController.i_init();
		this.infoPanelController.i_init();
		this.targetingController.i_init();
		if (APP.isMobile)
		{
			this.autoTargetingSwitcherController.i_init();
		}
		this.bossModeController.i_init();
		this.gameOptimizationController.i_init();
		this.cryogunsController.i_init();
		this.flameThrowersController.i_init();
		this.artilleryStrikesController.i_init();
		this.plazmaGunsController.i_init();
		this.ricochetLasersController.i_init();
		this.bigWinsController.i_init();
		//...init controllers

		this._initShotResponsesController();

		this.createGameField();
		this.infoPanelController.initView(this.infoPanelView);

		this.start(aOptRoomUrl_str);

		this._initPrizesController();
		this._initSpecialWeaponPrizesController();
		this._initAwardingController();
		this._initTreasuresController();
		this._initGameStateController();
		this._initCursorController();
		this._initGameBonusController();
		this._initGameFrbController();
		this._initTransitionViewController();
		this._initBattlegroundGameController();

		this._startHandleLobbyExternalMessages();
		this._startHandleShotResponseMessages();
		this._startHandleWebSocketMessages();

		this._initBuyAmmoRetryingController();

		this._initGamePleaseWaitDialogController();

		this._fHealthBarEnabled_bln = APP.playerController.info.healthBarEnabled;
		this._onHealthBarStateUpdated();
		APP.on(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onLobbyPlayerInfoUpdated, this);
		APP.on(Game.EVENT_ON_ASSETS_LOADING_ERROR, this._onAssetsLoadingError, this);
		APP.on(Game.EVENT_ON_WEBGL_CONTEXT_LOST, this._onWebglContextLost, this);

		APP.playerController.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		this._ready = true;
		this.emit(GameScreen.EVENT_ON_READY);

		this.on(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);

		this._fGameBonusController_gbc.on(GameBonusController.EVENT_ON_BONUS_STATE_CHANGED, this._onBonusStateChanged, this);
		this._fGameBonusController_gbc.on(GameBonusController.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, this._onTimeToOpenRealRoomAfterBonus, this);
		this._fGameFrbController_gfrbc.on(GameFRBController.EVENT_ON_FRB_STATUS_CHANGED, this.onFRBEnded, this);
		this._fGameFrbController_gfrbc.on(GameFRBController.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS, this._onTimeToOpenRealRoomAfterBonus, this);

		APP.pendingOperationController.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_STARTTED, this._onPendingOperationStarted, this);
		APP.pendingOperationController.on(GamePendingOperationController.EVENT_ON_PENDING_OPERATION_COMPLETED, this._onPendingOperationCompleted, this);

		//DEBUG...
		// window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		//...DEBUG
		APP.currentWindow.gameplayDialogController.gameCafPrivateRoundCountDownDialogController.on(CafPrivateRoundCountDownDialogController.EVENT_ON_SCREEN_ACTIVATED, this._observerModeActivated, this);
    }

    _observerModeActivated(event)
    {
        console.log("observer problem: observer mode activated");
        APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.OBSERVER_MODE_ACTIVATED);
    }

	//DEBUG...
	// keyDownHandler(keyCode)
	// {
	// 	if (keyCode.keyCode == 32)
	// 	{
	// 	}
	// }
	//...DEBUG

	get _tournamentModeInfo()
	{
		return this._fTournamentModeInfo || (this._fTournamentModeInfo = APP.tournamentModeController.info);
	}

	hideBlur()
	{
		this._hideBlur();
	}

	showBlur()
	{
		this._showBlur();
	}

	get compensationDialogActive()
	{
		return this._fCompensationDialogActive_bln;
	}

	get isReady()
	{
		return this._ready;
	}

	get isPaused()
	{
		return this._fIsPaused_bl;
	}

	get currentTime()
	{
		return serverTime + clientTimeDiff;
	}

	get accurateCurrentTime()
	{
		return accurateServerTime + accurateClientTimeDiff;
	}

	get isExpectedBuyInOnRoomStarted()
	{
		if(this.isBattlegroundMode)
		{
			return false;
		}

		if(this._isBonusMode)
		{
			return false;
		}

		if(this._isFrbMode)
		{
			return false;
		}

		if(this._isTournamentMode)
		{
			return false;
		}

		return this._fIsExpectedBuyInOnRoomStarted_bl;
	}

	get isKeepSWModeActive()
	{
		// Keep SW mode is turned off on the server for Revenge of Ra. Even if we get MQ_WEAPONS_SAVING_ALLOWED true, it should be considered as turned off.
		let lIsKeepSW_bl = false; // APP.gameSettingsController.info.weaponsSavingAllowed;
		let lBonusInfo_bi = this.gameBonusController.info;
		if (lBonusInfo_bi.isActivated)
		{
			lIsKeepSW_bl = lBonusInfo_bi.keepBonusSW;
		}
		let lFrbInfo_bi = this.gameFrbController.info;
		if (lFrbInfo_bi.frbMode)
		{
			lIsKeepSW_bl = lFrbInfo_bi.keepBonusSW;
		}

		let lTournamentModeinfo_tmi = this._tournamentModeInfo;
		if (lTournamentModeinfo_tmi.isTournamentMode)
		{
			lIsKeepSW_bl = lTournamentModeinfo_tmi.isKeepSWMode;
		}

		return lIsKeepSW_bl;
	}

	clearAllButLobbyInfo()
	{
		var currency = this.player.currency,
			nickname = this.player.nickname;

		this.player = {
			currency: currency,
			nickname: nickname,
			stake: 1
		};
		this.room = null;

		APP.off(Game.EVENT_ON_AVATAR_UPDATED, this.onAvatarUpdate, this);
		APP.off(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED, this.onSecondaryScreenActivated, this);
		APP.off(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED, this.onSecondaryScreenDeactivated, this);

		this._restoreAfterLagsInProgress = false;
	}

	clear()
	{
		if (this.enemies && this.enemies.length)
		{
			while (this.enemies.length)
			{
				delete this.enemies.pop();
			}
			this.enemies = [];
		}

		if (this._fMasterSWAwardsQueue_obj_arr)
		{
			//implement postponed weapons awarding
			while (this._fMasterSWAwardsQueue_obj_arr.length)
			{
				let lAward_obj = this._fMasterSWAwardsQueue_obj_arr.shift();
				this.addWeapon({id: lAward_obj.id, shots: lAward_obj.shots, nextBetLevel: lAward_obj.nextBetLevel}, {x: 0, y: 0}, 0);
			}
		}

		this._fHvEnemiesController_hesc && this._fHvEnemiesController_hesc.i_clearAll();

		this._fMasterSWAwardsExpected_obj_arr = [];
		APP.webSocketInteractionController.clearShotResponseParsed();
	}

	get pendingMasterSWAwards()
	{
		return this._fMasterSWAwardsQueue_obj_arr;
	}

	awardWeaponLanded(aLandedWeapon_obj)
	{
		if (aLandedWeapon_obj.id && aLandedWeapon_obj.shots)
		{
			if (this._fMasterSWAwardsExpected_obj_arr[aLandedWeapon_obj.id])
			{
				this._fMasterSWAwardsExpected_obj_arr[aLandedWeapon_obj.id].shots -= aLandedWeapon_obj.shots;

				if (this._fMasterSWAwardsExpected_obj_arr[aLandedWeapon_obj.id].shots < 0)
				{
					this._fMasterSWAwardsExpected_obj_arr[aLandedWeapon_obj.id].shots = 0;
				}
			}
			else
			{
				APP.logger.i_pushWarning(`GameScreen. Invalid indexing of the MasterSWAwardsExpected array, perhaps the list is empty. ${JSON.stringify(this._fMasterSWAwardsExpected_obj_arr)}`);
			}
		}
		else
		{
			APP.logger.i_pushWarning(`GameScreen. Invalid object for deleting the weapon queue. ${JSON.stringify(aLandedWeapon_obj)}`);
			console.error("Invalid object for deleting the weapon queue.");
		}
	}

	get isAwardWeaponLandExpected()
	{
		for (var id in this._fMasterSWAwardsExpected_obj_arr)
		{
			if (this._fMasterSWAwardsExpected_obj_arr[id].shots && this._fMasterSWAwardsExpected_obj_arr[id].shots > 0)
			{
				return true;
			}
		}

		return false;
	}

	_awardMultipleMasterSW(aMasterSWAwards_obj_arr, aParams_obj)
	{
		if(aMasterSWAwards_obj_arr && aMasterSWAwards_obj_arr.length)
		{
			let weaponsFlyPositions = [];

			switch (aMasterSWAwards_obj_arr.length)
			{
				case 1:
					weaponsFlyPositions = [
						{x: 0, y: -100}
					];
					break;
				case 2:
					weaponsFlyPositions = [
						{x:  90, y: -45},
						{x: -90, y: -45}
					];
					break;
				case 3:
					weaponsFlyPositions = [
						{x:  90, y:   45},
						{x: -90, y:   45},
						{x:   0, y: -100}
					];
					break;
			}

			for (let i = 0; i < aMasterSWAwards_obj_arr.length; i++)
			{
				let lMasterSWAward_obj = aMasterSWAwards_obj_arr[i];
				let weapon = {id: lMasterSWAward_obj.id, shots: lMasterSWAward_obj.shots, nextBetLevel: aParams_obj.data.nextBetLevel};
				let enemyLife = (aParams_obj.enemyView) ? aParams_obj.enemyView.life : 0;

				this.addWeapon(weapon, aParams_obj.position, enemyLife, true, weaponsFlyPositions[i]);
			}

			this._fMasterSWAwardsQueue_obj_arr = null;
		}
	}

	createGameField()
	{
		this.gameField = this.addChild(new GameField);
		this.gameField.on('fire', (params) =>{
			var shotData =
			{
				enemyId: params.id,
				weaponId: params.weaponId,
				isPaidSWShot: params.isPaidSWShot,
				x: params.x,
				y: params.y,
				weaponPrice: params.weaponPrice
			};

			if (params.bulletId !== undefined)
			{
				shotData.bulletId = params.bulletId;
			}

			this.emit(GameScreen.EVENT_ON_SHOT_TRIGGERED, shotData);

		});
		this.gameField.on(GameField.EVENT_ON_RICOCHET_BULLET_REGISTER, (e) =>{
			var shotData =
			{
				bulletTime: this.accurateCurrentTime,
				bulletAngle: e.bullet.angle,
				bulletId: e.bullet.bulletId,
				weaponId: e.bullet.weaponId,
				startPoint: {x: e.bullet.startPos.x, y: e.bullet.startPos.y},
				endPoint: {x: e.bullet.endPos.x, y: e.bullet.endPos.y},
				startPointX: e.bullet.startPos.x,
				startPointY: e.bullet.startPos.y,
				endPointX: e.bullet.endPos.x,
				endPointY: e.bullet.endPos.y,
			};

			this.emit(GameScreen.EVENT_ON_BULLET, shotData);
		});
		this.gameField.on(GameField.EVENT_ON_BULLET_CLEAR, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_GREEN_CLOUD_APPEARED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_EMPTY_ARMOR_GLOWED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_SOME_ENEMY_SPAWN_SOUND_REQUIRED, this.emit, this);

		this.gameField.on(GameField.EVENT_ON_BUY_AMMO_REQUIRED, this._onBuyAmmoRequired, this);
		this.gameField.on(GameField.EVENT_ON_TIME_TO_CONVERT_BALANCE_TO_AMMO, this._onTimeToConvertBalanceToAmmo, this);
		this.gameField.on('showEnemyHit', (params) => {
			let enemy = this.getExistEnemy(params.id);
			let lDeadEnemy_enm = this.gameField.getDeadEnemy(params.id);

			if (params.data.awardedWeapons && !!params.data.awardedWeapons.length)
			{
				let lMasterSWAwards_obj_arr = params.data.awardedWeapons;

				if(params.data.seatId === this.player.seatId && lMasterSWAwards_obj_arr && lMasterSWAwards_obj_arr.length)
				{
					if (lDeadEnemy_enm && Enemy.i_isLongDeathAnimationSupported(params.data.enemy.typeId)) //if the shot killed the enemy and the enemy has long death animation
					{
						lDeadEnemy_enm.once(Enemy.EVENT_ON_ENEMY_IS_HIDDEN, () => {
							this._awardMultipleMasterSW(lMasterSWAwards_obj_arr, params);
						});
					}
					else //if the enemy is alive after the shot  or if the enemy doesnt have special death animation
					{
						this._awardMultipleMasterSW(lMasterSWAwards_obj_arr, params);
					}
				}
			}
			else
			{
				if (params.data.awardedWeaponId != WEAPONS.DEFAULT)
				{
					let weapon = {id: params.data.awardedWeaponId, shots: params.data.awardedWeaponShots, nextBetLevel:params.data.nextBetLevel};
					let enemyLife = (params.enemyView) ? params.enemyView.life : 0;
					this.addWeapon(weapon, params.position, enemyLife);

					this._fMasterSWAwardsQueue_obj_arr = null;
				}
			}

			if (enemy && params.enemyView)
			{
				this.updateEnemyHealth(enemy, params.enemyView)
			}

			if (enemy && params.data.killed)
			{
				this.removeEnemy(enemy);
			}
		});
		this.gameField.on('removeEnemy', (params) => {
			let enemy = this.getExistEnemy(params.id);
			if (enemy)
			{
				this.removeEnemy(enemy);
			}
		})
		this.gameField.on(GameField.EVENT_ON_DEATH_ANIMATION_STARTED, (params) => {
			let enemy = this.getExistEnemy(params.enemyId);
			if (enemy)
			{
				this.removeEnemy(enemy);
			}
		});
		this.gameField.on(GameField.EVENT_ON_ENEMY_VIEW_REMOVING, (params) => {
			let enemy = this.getExistEnemy(params.enemyId);
			if (enemy)
			{
				this.removeEnemy(enemy);
			}
		});

		this.gameField.on('roomFieldClosed', () => {
			this.sendCloseRoom();
		});

		this.gameField.on('roomFieldCleared', () => {
			this.emit(GameScreen.EVENT_ON_GAME_FIELD_CLEARED);
		});

		this.gameField.on(GameField.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this.onBackToLobbyButtonClicked, this);
		this.gameField.on(GameField.EVENT_ON_PROFILE_BUTTON_CLICKED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_ACHIEVEMENTS_BUTTON_CLICKED, this.emit, this);
		this.gameField.on(GameField.EVENT_SOUND_ON_BUTTON_CLICKED, this.emit, this);
		this.gameField.on(GameField.EVENT_SOUND_OFF_BUTTON_CLICKED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_NEW_ROUND_STATE, this._onNewRoundState, this);
		this.gameField.on(GameField.EVENT_ON_WAITING_NEW_ROUND, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_DRAW_MAP, this.emit, this);
		this.gameField.on(GameField.EVENT_REFRESH_COMMON_PANEL_REQUIRED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_WEAPON_UPDATED, this.emit, this);
		this.gameField.on(GameField.EVENT_DECREASE_AMMO, this.emit, this);
		this.gameField.on(GameField.EVENT_NEW_WEAPON_CONTENT_LANDED_WITHOUT_CHANGING, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_TARGETING, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_AUTOFIRE_BUTTON_ENABLED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_AUTOFIRE_BUTTON_DISABLED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_RESET_TARGET, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_ENEMY_ADD_TRAJECTORY_POINT, this._onEnemyAddTrajectoryPoint, this);
		this.gameField.on(GameField.EVENT_ON_ENEMY_PAUSE_WALKING, this._onEnemyPauseWalking, this);
		this.gameField.on(GameField.EVENT_ON_ENEMY_RESUME_WALKING, this._onEnemyResumeWalking, this);
		this.gameField.on(GameField.EVENT_ON_ENEMY_FREEZE, this._onEnemyFreeze, this);
		this.gameField.on(GameField.EVENT_ON_ENEMY_UNFREEZE, this._onEnemyUnfreeze, this);
		this.gameField.on(GameField.EVENT_ON_NEW_BOSS_CREATED, this._onBossCreated, this);
		this.gameField.on(GameField.EVENT_ON_DRAGON_DISAPPEARED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BOSS_DESTROYING, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BOSS_DESTROYED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BOSS_DEATH_FLARE, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BOSS_DEATH_CRACK, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BOSS_ENEMY_APPEARANCE_TIME, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_TIME_TO_EXPLODE_COINS, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this.emit, this);
		this.gameField.on(GameField.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED, this._onNotEnougMoneyDialogRequired, this);
		this.gameField.on(GameField.EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED, this._onSWPurchaseLimitExceededDialogRequired, this);
		this.gameField.on(GameField.EVENT_ON_FIRE_CANCELLED_WITH_NOT_ENOUGH_AMMO, this._onFireCancelledWithNotEnoughAmmo, this);
		this.gameField.on(GameField.EVENT_ON_PICKS_UP_SPECIAL_WEAPONS_FIRST_TIME, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BULLET_TARGET_TIME, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BET_MULTIPLIER_UPDATED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BET_MULTIPLIER_UPDATE_REQUIRED, this.emit, this);

		this.gameField.on(GameField.EVENT_ON_BACK_TO_LOBBY_ROUND_RESULT_BUTTON_CLICKED, this.onBackToLobbyRoundResultButtonClicked, this);
		this.gameField.on(GameField.EVENT_BACK_TO_LOBBY_ON_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_DEACTIVATED, this.backToLobbyOnSWPurchaseLimitExceededDialogDeactivated, this);

		this.gameField.on(GameField.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);
		this.gameField.on(GameField.EVENT_ON_TARGET_ENEMY_IS_DEAD, this.emit, this);

		this.gameField.on(GameField.EVENT_ON_HIT_AWARD_EXPECTED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_SHOT_SHOW_FIRE_START_TIME, this.emit, this);

		this.gameField.on(GameField.EVENT_ON_GUN_UNLOCKED, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_WEAPONS_INTERACTION_CHANGED, this._onInteractionChanged, this);

		this.gameField.on(GameField.EVENT_ON_DRAGON_FIRE_BREATH_STARTED, this.emit, this);

		this.gameField.on(GameField.EVENT_FULL_GAME_INFO_REQUIRED, this._requestFullGameInfo, this);

		this.gameField.on(GameField.EVENT_ON_UPDATE_PLAYER_WIN_CAPTION, this.emit, this);

		this.gameField.on(GameField.EVENT_ON_DEATH_COIN_AWARD, this.emit, this);
		this.gameField.on(GameField.EVENT_ON_BATTLEGROUND_NEXT_ROUND_CLICKED, this._onBattlegroundNextRoundClicked, this);
		this.gameField.on(GameField.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATION_START, this._onRoundResultActivationStart, this);



		//DEBUG...
		//this.gameField.on('stubUpdateTrajectory', this._onStubUpdateTrajectory, this);
		//...DEBUG

		this.gameField.init();

		//DEBUG...
		//setTimeout(this._debugUpdateTrajectories.bind(this), 10000);
		//...DEBUG

		//DEBUG RANDOM TRAJECTORIES...
		// document.addEventListener('keyup', (e)=>{
		// 	if (e.keyCode === 16) this._randomDirectionsTrajectoriesDebug();
		// });
		//...DEBUG RANDOM TRAJECTORIES
	}

	_onBossCreated(e)
	{
		this.emit(e);
	}

	_onRoomFieldCleared(event)
	{
		this.emit(event);
	}

	_onNewRoundState(event, data)
	{
		this.emit(event, data);

		if(event.state === true)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_IN_PROGRESS);
		}
	}

	onBackToLobbyRoundResultButtonClicked()
	{
		this.goBackToLobby();
	}

	backToLobbyOnSWPurchaseLimitExceededDialogDeactivated()
	{
		this.sitOut();
	}

	_onInteractionChanged(event)
	{
		let lAllowed_bl = event.allowed;

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.WEAPONS_INTERACTION_CHANGED, {allowed: lAllowed_bl});
	}

	get _isFrbMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _isBonusMode()
	{
		return this.gameBonusController.info.isActivated;
	}

	get isBattlegroundMode()
	{
		return this._fIsBattlegroundMode_bl;
	}

	get _isTournamentMode()
	{
		let lTournamentModeController_tmc = APP.tournamentModeController;

		if (
			lTournamentModeController_tmc &&
			lTournamentModeController_tmc.info &&
			lTournamentModeController_tmc.info.isTournamentMode
		)
		{
			return true;
		}

		return false;
	}

	get _awardedWeapons()
	{
		let lAwardingWeap_arr = APP.currentWindow.gameField.newWeapons;
		let lWeapons_arr = [];
		for (let i = 0; i < this.weaponsController.info.weapons.length; ++i)
		{
			lWeapons_arr[i] = {id: this.weaponsController.info.weapons[i].id, shots: this.weaponsController.info.weapons[i].shots};
		}

		for (let i = 0 ; i < lAwardingWeap_arr.length; ++i)
		{
			for (let j = 0; j < lWeapons_arr.length; ++j)
			{
				if (lWeapons_arr[j].id == lAwardingWeap_arr[i].id)
				{
					lWeapons_arr[j].shots += lAwardingWeap_arr[i].shots;
				}
			}
		}

		return lWeapons_arr;
	}

	isGamePlayInProgress()
	{
		return this.gameField.isGamePlayInProgress();
	}

	onBackToLobbyButtonClicked(aIsRequestedFromBattlegroundDialog_bl)
	{
		if(aIsRequestedFromBattlegroundDialog_bl)
		{
			this.sitOut();
			this.goBackToLobby();
			return;
		}

		if (this._fBackToLobbyRequired_bl)
		{
			return;
		}

		if (
				(
					this.gameStateController.info.gameState === ROUND_STATE.PLAY ||
					this.gameStateController.info.isGameInProgress
				) &&
				this.gameStateController.info.isPlayerSitIn &&
				(
					!this._isFrbMode
				)
			)
		{
			if (!APP.webSocketInteractionController.isSitoutRequestInProgress)
			{
				this.emit(GameScreen.EVENT_MID_ROUND_EXIT_REQUIRED, {roomId: this.room.id});
			}
		}
		else if (
					this._isFrbMode &&
					this.weaponsController.info.isAnyFreeSpecialWeaponExist
				)
		{
			this.sitOut();
		}
		else 
		{
			this.goBackToLobby();
		}
	}

	_isEmptyAmmoSpecialWeapons(aWeapons)
	{
		if (aWeapons)
		{
			for (let i = 0; i < aWeapons.length; i++)
			{
				if (aWeapons[i].shots != 0) return false;
			}
			return true;
		}
		else
		{
			return undefined;
		}
	}

	goBackToLobby(aOptErrorCode_num = null)
	{
		if (APP.isBattlegroundGamePlayMode)
		{
			return;
		}

		this._fRoundFinishSoon_bl = false;
		this._fCompensationDialogActive_bln = false;
		this._fBackToLobbyRequired_bl = true;
		this._restoreAfterLagsInProgress = false;
		
		this.gameField.roundResultScreenController.hideScreen();
		this.gameField.roundResultScreenController.resetScreenAppearing();

		if (this.gameStateController.info.isPlayerSitIn)
		{
			this.sitOut();
		}
		else
		{
			if (this.room && this.room.maxSeats !== undefined && this.room.maxSeats > 0)
			{
				this.closeRoom();
			}
			else
			{
				this.clearAllButLobbyInfo();
				this._emitBackToLobbyEvent(aOptErrorCode_num);
			}
		}
	}

	get isBackToLobbyRequired()
	{
		return this._fBackToLobbyRequired_bl;
	}

	_emitBackToLobbyEvent(aOptErrorCode_num)
	{
		this._fBackToLobbyRequired_bl = false;
		this.gameField && this.gameField.onBackToLobbyOccur();
		this.emit(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, {errorCodeReason: aOptErrorCode_num});
	}

	sendCloseRoom()
	{
		this.emit(GameScreen.EVENT_ON_CLOSE_ROOM_REQUIRED, {roomId: this._roomId});
	}

	start(aStartGameUrl_str = undefined)
	{
		let GET = this._applyUrlParams(aStartGameUrl_str);

		if (aStartGameUrl_str !== undefined)
		{
			let socketUrl = decodeURIComponent(GET.WEB_SOCKET_URL);
			this.emit(GameScreen.EVENT_ON_GAME_SOCKET_URL_UPDATED, {socketUrl: socketUrl});
		}
	}

	_applyUrlParams(aStartGameUrl_str = undefined)
	{
		let GET = parseGet(aStartGameUrl_str);
		window.GET = GET;

		if (GET.room)
		{
			this.getRoomParams = GET.room;
		}

		if (GET.roomId)
		{
			this.room = {id: GET.roomId};
			this._roomId = GET.roomId;
			this.emit(GameScreen.EVENT_ON_ROOM_ID_CHANGED, {id: this.room.id});
			APP.urlBasedParams.roomId = this.room.id;
		}

		if (GET.playerStake)
		{
			this.player.stake = +GET.playerStake;
		}

		if (GET.serverId !== undefined)
		{
			APP.urlBasedParams.serverId = GET.serverId;
		}

		this.sid = APP.urlBasedParams.SID = GET.SID || APP.urlBasedParams.SID;

		this.lang = I18.currentLocale;
		this.mode = APP.urlBasedParams.MODE;

		return GET;
	}

	//LOBBY EXTERNAL MESSAGES...
	_startHandleLobbyExternalMessages()
	{
		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	_onLobbyExternalMessageReceived(event)
	{
		let msgType = event.type;

		switch (msgType)
		{
			case LOBBY_MESSAGES.GAME_URL_UPDATED:
				if (APP.isBattlegroundGame)
				{
					this.clear();
					this.gameField && this.gameField.clearRoom();
				}

				let updatedGameUrl = event.data.gameUrl;
				let GET = parseGet(updatedGameUrl);

				this._applyUrlParams(updatedGameUrl);

				let socketUrl = decodeURIComponent(GET.WEB_SOCKET_URL);
				this.emit(GameScreen.EVENT_ON_GAME_SOCKET_URL_UPDATED, {socketUrl: socketUrl});
				break;
			case LOBBY_MESSAGES.BACK_TO_LOBBY:
			case LOBBY_MESSAGES.BUY_AMMO_FAILED_EXIT_ROOM_REQUESTED:
				this.onBackToLobbyButtonClicked(event.data && event.data.isRequestedFromBattleGroundDialog);
				break;
			case LOBBY_MESSAGES.DIALOG_ACTIVATED:
				let lDialogId_int = event.data? event.data.dialogId : undefined;
				this.emit(GameScreen.EVENT_ON_DIALOG_ACTIVATED, {dialogId: lDialogId_int});
				break;
			case LOBBY_MESSAGES.DIALOG_DEACTIVATED:
				this.emit(GameScreen.EVENT_ON_DIALOG_DEACTIVATED);
				//WHEN BATTLEGROUND REBUY NEM DIALOG CLOSED...
				if(
					APP.isBattlegroundGamePlayMode &&
					event.data.dialogId === DIALOG_ID_INSUFFICIENT_FUNDS &&
					this.gameStateController.info.isPlayerSitIn
					)
				{
					this.onBackToLobbyButtonClicked(true)
				}
				//...WHEN BATTLEGROUND REBUY NEM DIALOG CLOSED
				break;
			case LOBBY_MESSAGES.FORCE_SIT_OUT_EXIT_CONFIRMED:
				this._fForseSitOutInProgress = false;
				this.goBackToLobby();
				break;
			case LOBBY_MESSAGES.MID_ROUND_COMPENSATE_SW_EXIT_CONFIRMED:
				this.goBackToLobby();
				break;
			case LOBBY_MESSAGES.MID_ROUND_EXIT_CONFIRMED:
				this.goBackToLobby();
				break;
			case LOBBY_MESSAGES.CHOOSE_WEAPON_SCREEN_CHANGED:
				if (this.gameField)
				{
					this.gameField.onChooseWeaponsStateChanged(event.data.isActive);
				}
				this.emit(GameScreen.EVENT_ON_CHOOSE_WEAPON_SCREEN_CHANGED, {isActive: event.data.isActive});
				break;
			case LOBBY_MESSAGES.SERVER_ERROR_MESSAGE_RECIEVED:
				if (GameWebSocketInteractionController.isGeneralError(event.data.errorType))
				{
					let errorCode = event.data.errorCode;
					if (
							errorCode === GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND
							|| errorCode === GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN
						)
					{
						this.goBackToLobby();
					}
				}
				break;
			case LOBBY_MESSAGES.LOBBY_CONNECTION_STATE:
				let lLobbyConnectionOpened_bln = event.data.state;
				if (!lLobbyConnectionOpened_bln)
				{
					this.gameField.lobbySecondaryScreenActive = false;
					this.hideBlur();
				}
				break;
			case LOBBY_MESSAGES.LOBBY_LOADING_ERROR:
			case LOBBY_MESSAGES.WEBGL_CONTEXT_LOST:
				this.gameField && this.gameField.clearRoom();
				this.clear();
				this.clearAllButLobbyInfo();
				break;
			case LOBBY_MESSAGES.BACKGROUND_FADE_START:
				this.emit(GameScreen.EVENT_ON_LOBBY_BACKGROUND_FADE_START, {endVolume: event.data.endVolume});
				break;
			case LOBBY_MESSAGES.FRB_RESTART_REQUIRED:
			case LOBBY_MESSAGES.BONUS_RESTART_REQUIRED:
				if (event.data && event.data.restartRoom)
				{
					//restart room without going back to Lobby
					this.emit(GameScreen.EVENT_ON_TIME_TO_RESTART_BONUS_ROOM);
					this._fBackToLobbyRequired_bl = false;
					this.closeRoom();
				}
				else
				{
					this._fFrbEndedPostponedData_obj = null;
					this._fBonusRoundEndedPostponedData_obj = null;
					APP.webSocketInteractionController.clearOpenRoomSent();
					this.goBackToLobby();
				}
				break;
			case LOBBY_MESSAGES.TOURNAMENT_REBUY_CONFIRMED:
			case LOBBY_MESSAGES.BATTLEGROUND_PLAY_AGAIN_CONFIRMED:
				if (
					APP.isBattlegroundGamePlayMode && 
						(
							!this.gameStateController.info.isPlayerSitIn || 
							APP.webSocketInteractionController.isSitoutRequestInProgress
						)
					)
				{
					this._fNeedUpdateStartGameUrl_bl = true;
					this._gameUrlNeedToUpdate();
				}
				else
				{
					this.sendReBuy();
					this.gameField.showKingsOfTheHill([-1]);
				}
				break;
			case LOBBY_MESSAGES.GAME_NEM_FOR_ROOM_REQUEST_CONFIRMED:
				if (this._tournamentModeInfo.isTournamentMode
					&& this.weaponsController.info.isAnyFreeSpecialWeaponExist)
				{
					return;
				}
				this.goBackToLobby();
				break;
			case LOBBY_MESSAGES.TOURNAMENT_COMEPLETED_WITH_NO_REBUYS:
				this.goBackToLobby();
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_NEED_RETRY_REBUY:
				this._needRetryRebuy();
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_CHANGE_WORLD_BUTTON_CLICKED:
				this._onBattlegroundChangeWorldButtonClicked();
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_NEED_UPDATE_TIME_TO_START:
				this._requestFullGameInfo();
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_CONTINUE_READING_INFO_CLICKED:
				this._cancelRoundClicked();
				break;
			case LOBBY_MESSAGES.BATTLEGROUND_CANCEL_READY_CLICKED:
				this._cancelRoundClicked();
				break;
		}
	}

	//SHOT HANDLING...
	_startHandleShotResponseMessages()
	{
		this.shotResponsesController.on(ShotResponsesController.EVENT_ON_SERVER_SHOT_RESPONSE, this._onShotResponse, this);
	}

	_onShotResponse(data)
	{
		this.emit(GameScreen.EVENT_ON_SHOT_RESPONSE_OCCURED, data);

		let lShotResponseInfo_sri = data.info;
		if (lShotResponseInfo_sri.isHit) {
			this._onServerHitMessage({messageData: lShotResponseInfo_sri.data, requestData: lShotResponseInfo_sri.requestData});
		}
		else if (lShotResponseInfo_sri.isMiss) {
			this._onServerMissMessage({messageData: lShotResponseInfo_sri.data, requestData: lShotResponseInfo_sri.requestData});
		}
		else {
			throw new Error("Shot response is neither Hit nor Miss " + lShotResponseInfo_sri.toString());
		}

		this.emit(GameScreen.EVENT_ON_SHOT_RESPONSE_PARSED, lShotResponseInfo_sri.requestData);
	}
	//...SHOT HANDLING

	//SERVER INTERACTION...
	_startHandleWebSocketMessages()
	{
		let wsInteractionController = APP.webSocketInteractionController;

		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_OPENED, this._onGameServerConnectionOpened, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_GET_ROOM_INFO_RESPONSE_MESSAGE, this._onServerGetRoomInfoResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE, this._onServerFullGameInfoMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_IN_RESPONSE_MESSAGE, this._onServerSitInResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_SIT_OUT_RESPONSE_MESSAGE, this._onServerSitOutResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_NEW_ENEMY_MESSAGE, this._onServerNewEnemyMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_NEW_ENEMIES_MESSAGE, this._onServerNewEnemiesMessage, this);

		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CLIENTS_INFO_MESSAGE, this._onServerClientsInfoMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ROUND_RESULT_MESSAGE, this._onServerRoundResultMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_GAME_STATE_CHANGED_MESSAGE, this._onServerGameStateChangedMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BUY_IN_RESPONSE_MESSAGE, this._onServerBuyInResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_RE_BUY_RESPONSE_MESSAGE, this._onServerReBuyResponseMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ENEMY_DESTROYED_MESSAGE, this._onServerEnemyDestroyedMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CHANGE_MAP_MESSAGE, this._onServerChangeMapMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_WEAPON_SWITCHED_MESSAGE, this._onServerWeaponSwitchedMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_OK_MESSAGE, this._onServerOkMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_UPDATE_TRAJECTORIES_MESSAGE, this._onServerUpdateTrajectoriesMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ROUND_FINISH_SOON, this._onServerRoundFinishSoonMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BALANCE_UPDATED_MESSAGE, this._onServerBalanceUpdatedMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BET_LEVEL_CHANGED, this._onServerBetLevelChangeConfirmed, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_FRB_ENDED_MESSAGE, this._onServerFRBEndedMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BULLET_RESPONSE, this._onBulletResponse, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_BULLET_CLEAR_RESPONSE, this._onBulletClearResponse, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CANCEL_BATTLEGROUND_ROUND, this._onCancelBattlegroundRound, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_BATTLEGROUND_SCORE_BOARD, this._onBattlegroundScoreBoard, this);
	}

	_onBattlegroundScoreBoard(event)
	{
		let lPlayersScore_obj_arr = event.messageData.scoreBySeatId;
		let lMasterSeatId_num = APP.playerController.info.seatId;
		for (let i=0; i<lPlayersScore_obj_arr.length; i++)
		{
			let lPlayerScore_obj = lPlayersScore_obj_arr[i];
			if (lPlayerScore_obj.seatId == lMasterSeatId_num)
			{
				this.player.lastReceivedBattlegroundScore_int = lPlayerScore_obj.winAmount;
			}
		}
	}

	_onGameServerConnectionOpened()
	{
		this._restoreAfterUnseasonableRequest = false;
		this._restoreAfterLagsInProgress = false;

		if (this.gameField && this.gameField.isGameplayStarted())
		{
			this.gameField.screen.show();
			if (APP.isBattlegroundGamePlayMode)
			{
				this.gameField.battlegroundFinalCountingController.i_checkCountingComplete();
			}
		}
		this.gameField && this.gameField.onConnectionOpenedHandled();
	}

	_onGameServerConnectionClosed(event)
	{
		this._restoreAfterUnseasonableRequest = false;
		this._restoreAfterLagsInProgress = false;
		if(this.isBattlegroundMode)
		{
			this.battlegroundGameController.info.isNotFirstBattlegroundRoundAfterLoading = true;

			this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = false;
			this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo);
		}

		if (event.wasClean)
		{
			return;
		}

		this.gameField && this.gameField.clearRoom();
		this.gameField && this.gameField.onChooseWeaponsStateChanged(false);
		this.clear();
		this.clearAllButLobbyInfo();

		if (this._fStartGameURL_str)
		{
			this._applyUrlParams(this._fStartGameURL_str);
			this._fStartGameURL_str = null;
		}

		this.gameField && this.gameField.onConnectionClosedHandled();
	}

	_onServerMessage(event)
	{
		let messageData = event.messageData;
		let messageClass = messageData.class;

		//CATCHING RESPONSE TO BATTLEGROUND BUY IN CONFIRM HERE...
		if(
			APP.gameScreen.isBattlegroundMode &&
			APP.webSocketInteractionController.i_getLastItemClassInRequestList(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN) &&
			APP.webSocketInteractionController.i_getLastItemClassInRequestList(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN).rid === messageData.rid
			)
		{
			if(messageClass === SERVER_MESSAGES.OK)
			{
				this._onBattlegroundBuyInConfirmRequestApproved();
			}
			else
			{
				//DEBUG...
				APP.logger.i_pushDebug(`GameScreen. BTG buy-in confirm request was not approved by server. ${JSON.stringify(event.messageData)}`);
				console.error("battleground buy-in confirm request was not approved by server");
				//...DEBUG
			}
		}
		//....CATCHING RESPONSE TO BATTLEGROUND BUY IN CONFIRM HERE

		if (
				messageClass == SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE
				|| messageClass == SERVER_MESSAGES.FULL_GAME_INFO
				|| (messageClass == SERVER_MESSAGES.GAME_STATE_CHANGED && (messageData.state === ROUND_STATE.PLAY || messageData.state === ROUND_STATE.WAIT))
				|| (messageClass == SERVER_MESSAGES.UPDATE_TRAJECTORIES && messageData.freezeTime !== undefined && messageData.freezeTime > 0)
				|| messageClass == SERVER_MESSAGES.BALANCE_UPDATED
				|| messageClass == SERVER_MESSAGES.BATTLEGROUND_SCORE_BOARD
			)
		{
			let lServerNewTime_int = messageData.date;
			if (!serverTimeInitiated || serverTime < lServerNewTime_int)
			{
				serverTimeInitiated = true;
				serverTime = lServerNewTime_int;
				serverLastMessageRecieveTime = Date.now();
				clientTimeDiff = 0;
			}

			accurateServerTime = lServerNewTime_int;
			accurateClientTimeDiff = 0;
			accurateServerLastMessageRecieveTime = Date.now();
			if (APP.isBattlegroundGamePlayMode)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_TIME_SYNC, {serverTime: lServerNewTime_int});
			}
		}

		if (
			APP.isBattlegroundGame
			&& APP.isBattlegroundGamePlayMode
			&& (messageData.state === ROUND_STATE.PLAY)
			&& !this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
			&& !this.gameStateController.info.isPlayerSitIn
		)
		{
			
			this._fNeedUpdateStartGameUrl_bl = false;
			

			if (this._fNeedUpdateStartGameUrl_bl)
			{
				this.emit(GameScreen.EVENT_ON_BTG_ROUND_OBSERVER_DENIED);
			}

			this._gameUrlNeedToUpdate();
			this.clear();
			this.gameField && this.gameField.clearRoom();
		}

		if (APP.isBattlegroundGame
			&& APP.isBattlegroundGamePlayMode
			&& messageData.state
			)
		{
			this.emit(GameScreen.EVENT_ON_SERVER_MESSAGE_GAME_STATE_CHANGED, {state: messageData.state})
		}
	}

	_onServerGetRoomInfoResponseMessage(event)
	{
		this.getRoomInfoResponse(event.messageData);
	}

	_onServerFullGameInfoMessage(event)
	{
		let unrespondedSitIn = APP.webSocketInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_IN);
		if(unrespondedSitIn)
		{
			setTimeout(()=>{
				this._onServerFullGameInfoMessage(event);
			}, 30);
			return;
		}

		this.fullGameInfoResponse(event.messageData);

		if(APP.isBattlegroundGame)
		{
			let lTimeToStart_num = event.messageData.timeToStart;
			let lEndTime_num = event.messageData.endTime;

			if(lTimeToStart_num !== undefined)
			{
				if(	lEndTime_num 
					&& event.messageData.state === ROUND_STATE.PLAY
					&& !this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
					&& !this.gameStateController.info.isPlayerSitIn)
				{
					this.emit(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED,
					{
						timeToStart: lEndTime_num,
						isRoundResultDisplayInProgress: this.isRoundResultDisplayInProgress(),
						isPlayerClickedConfirmPlayForNextBattlegroundRound: this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
					});
				}
				else
				{
				APP.externalCommunicator.sendExternalMessage(
					GAME_MESSAGES.BATTLEGROUND_TIME_TO_START_UPDATED,
					{
						timeToStart: lTimeToStart_num,
						isRoundResultDisplayInProgress: this.isRoundResultDisplayInProgress(),
						isPlayerClickedConfirmPlayForNextBattlegroundRound: this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
					}
				);

				this.emit(GameScreen.EVENT_BATTLEGROUND_TIME_TO_START_UPDATED,
					{
						timeToStart: lTimeToStart_num,
						isRoundResultDisplayInProgress: this.isRoundResultDisplayInProgress(),
						isPlayerClickedConfirmPlayForNextBattlegroundRound: this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
					});
			}
		}
	}
	}

	isRoundResultDisplayInProgress()
	{
		return this.gameField.roundResultActive;
	}

	_onServerSitInResponseMessage(event)
	{
		this.sitInResponse(event.messageData);

		if(this.isBattlegroundMode)
		{
			this.emit(GameScreen.EVENT_BATTLEGROUND_REQUEST_TIME_TO_START);
		}
	}

	_onServerSitOutResponseMessage(event)
	{
		this.sitOutResponse(event.messageData);
	}

	_onServerNewEnemyMessage(event)
	{
		this.newZombieResponse(event.messageData);
	}

	_onServerNewEnemiesMessage(event)
	{
		this.newZombiesResponse(event.messageData);
	}

	_onServerMissMessage(event)
	{
		let lRequestEnemyId_int = event.requestData ? event.requestData.enemyId : undefined;
		if (!isNaN(event.messageData.shotEnemyId) && event.messageData.shotEnemyId >= 0)
		{
			lRequestEnemyId_int = event.messageData.shotEnemyId;
		}
		let lIsPaidSpecialShot_bl = event.requestData ? !!event.requestData.isPaidSpecialShot : false;

		this.missResponse(event.messageData, lIsPaidSpecialShot_bl, lRequestEnemyId_int);

		this._checkForCoPlayerCollision(event.messageData);
	}

	_onServerHitMessage(event)
	{
		//debug...
		// let pos = {};
		// pos.x = this.getExistEnemy(event.messageData.enemy.id).x;
		// pos.y = this.getExistEnemy(event.messageData.enemy.id).y;
		// this.newZombieResponse({"newEnemy":{"id":11643,"typeId":9,"speed":4.0,"awardedPrizes":"","awardedSum":0.0,"skin":1,"trajectory":{"speed":4.0,"points":[{"x":pos.x,"y":pos.y,"time":Date.now()}, {"x":0.0,"y":540.0,"time":(Date.now()+30000)}]}},"date":1560758228948,"rid":-1,"class":"NewEnemy"});
		// event.messageData.hvEnemyId = 11643;
		//...debug

		if (~~event.messageData.hvEnemyId > -1 || event.messageData.enemy.typeId == ENEMY_TYPES.EXPLODER)
		{
			this.emit(GameScreen.EVENT_ON_HV_ENEMY_PARENT_HIT, {hvEnemyId: event.messageData.hvEnemyId, parentEnemyId: event.messageData.enemy.id});
		}
		let lRequestEnemyId_int = event.requestData ? event.requestData.enemyId : undefined;
		if (!isNaN(event.messageData.shotEnemyId) && event.messageData.shotEnemyId >= 0)
		{
			lRequestEnemyId_int = event.messageData.shotEnemyId;
		}
		let lIsPaidSpecialShot_bl = event.requestData ? !!event.requestData.isPaidSpecialShot : false;

		this.hitResponse(event.messageData, lIsPaidSpecialShot_bl, lRequestEnemyId_int);

		if (event.messageData.rid != -1 && this._hasAwardedPowerUpWeapon(event.messageData))
		{
			this.emit(GameScreen.EVENT_ON_CURRENT_POWER_UP_MULTIPLIER_UPDATED, {currentPowerUpMultiplier: event.messageData.currentPowerUpMultiplier});
		}

		this._checkForCoPlayerCollision(event.messageData);
	}

	_hasAwardedPowerUpWeapon(aHitData_obj)
	{
		if (aHitData_obj && aHitData_obj.awardedWeapons && aHitData_obj.awardedWeapons[0])
		{
			for (let lPowerUpWeapon in POWER_UP_WEAPONS)
			{
				if (POWER_UP_WEAPONS[lPowerUpWeapon] == aHitData_obj.awardedWeapons[0].id)
				{
					return true;
				}
			}
		}
		return false;
	}

	_checkForCoPlayerCollision(data)
	{
		if (data.rid === -1 && data.bulletId !== undefined)
		{
			this.emit(GameScreen.EVENT_ON_CO_PLAYER_COLLISION_OCCURED, {bulletId: data.bulletId});
		}
	}

	_onServerClientsInfoMessage(event)
	{
		this.updatePlayersInfo(event.messageData);
	}

	_onServerRoundResultMessage(event)
	{
		if (this._isFRBEnded || this._isBonusRoundEnded)
		{
			return;
		}

		this.roundResultResponse(event.messageData);
	}

	_onServerGameStateChangedMessage(event)
	{
		this.gameStateChangedResponse(event.messageData);
	}

	_onServerBuyInResponseMessage(event)
	{
		this.onBuyInResponse(event.messageData);
	}

	_onServerReBuyResponseMessage(event)
	{
		this.onReBuyResponse(event.messageData);
	}

	_onServerEnemyDestroyedMessage(event)
	{
		this.destroyEnemy(event.messageData);

		if (!this.room)
		{
			this._fEnemyIdToRemoveFurther_num = aEnemyId_num;
		}
	}

	_onServerChangeMapMessage(event)
	{
		if (
				this._isFRBEnded
				|| this._isBonusRoundEnded
				|| (
						APP.isCAFMode
						&& this._fGameStateController_gsc
						&& this._fGameStateController_gsc.info
						&& !this._fGameStateController_gsc.info.isPlayerSitIn
					)
			)
		{
			return;
		}

		this.onChangeMap(event.messageData);
	}

	_onServerBetLevelChangeConfirmed(aEvent_obj)
	{
		let lBetLevel_num = aEvent_obj.messageData.betLevel;

		this.emit(GameScreen.EVENT_ON_BET_LEVEL_CHANGE_CONFIRMED, {betLevel: lBetLevel_num, seatId: aEvent_obj.messageData.seatId});
	}

	_onServerBetLevelChangeNotConfirmed()
	{
		this.emit(GameScreen.EVENT_ON_BET_LEVEL_CHANGE_NOT_CONFIRMED);
	}

	_onServerOkMessage(event)
	{
		let requestClass = undefined;
		let requestData = event.requestData;
		if (requestData && requestData.rid >= 0)
		{
			requestClass = requestData.class;
		}

		if (requestClass !== undefined)
		{
			switch (requestClass)
			{
				case CLIENT_MESSAGES.CLOSE_ROOM:
					this._onCloseRoomConfirmed();
					break;
				case CLIENT_MESSAGES.BATTLEGROUND_START_PRIVATE_ROOM:
					this._requestFullGameInfo();
					break;
			}
		}
	}

	_onServerErrorMessage(event)
	{
		this._handleErrorCode(event.messageData, event.requestData, event.errorType);
	}

	_onServerWeaponSwitchedMessage(event)
	{
		this.onWeaponSwitched(event.messageData);
	}

	_onServerUpdateTrajectoriesMessage(event)
	{
		this.onUpdateTrajectories(event.messageData);
	}

	_onServerRoundFinishSoonMessage()
	{
		this._fRoundFinishSoon_bl = true;
	}
	//...SERVER INTERACTION

	get awardingController()
	{
		return this._fAwardingController_ac || (this._fAwardingController_ac = this._initAwardingController());
	}

	get treasuresController()
	{
		return this._fTreasureController_tc || (this._fTreasureController_tc = this._initTreasuresController());
	}

	get gameStateController()
	{
		return this._fGameStateController_gsc || (this._fGameStateController_gsc = this._initGameStateController());
	}

	get gameFrbController()
	{
		return this._fGameFrbController_gfrbc || (this._fGameFrbController_gfrbc = this._initGameFrbController());
	}

	get gameBonusController()
	{
		return this._fGameBonusController_gbc || (this._fGameBonusController_gbc = this._initGameBonusController());
	}

	get bossModeController()
	{
		return this._fBossModeController_bmc || (this._fBossModeController_bmc = this._initBossModeController());
	}

	get mapsController()
	{
		return this._fMapsController_msc || (this._fMapsController_msc = this._initMapsController());
	}

	get subloadingController()
	{
		return this._fSubloadingController_sc || (this._fSubloadingController_sc = this._initSubloadingController());
	}

	get hvEnemiesController()
	{
		return this._fHvEnemiesController_hesc || (this._fHvEnemiesController_hesc = this._initHvEnemiesController());
	}

	get weaponsController()
	{
		return this._fWeaponsController_fwsc || (this._fWeaponsController_fwsc = this._initWeaponsController());
	}

	get bigWinsController()
	{
		return this._fBigWinsController_bwsc || (this._fBigWinsController_bwsc = this._initBigWinsController());
	}

	get fireSettingsController()
	{
		return this._fFireSettingsController_fsc || (this._fFireSettingsController_fsc = this._initFireSettingsController());
	}

	get infoPanelController()
	{
		return this._fInfoPanelController_ipc || (this._fInfoPanelController_ipc = this._initInfoPanelController());
	}

	get infoPanelView()
	{
		return this._fInfoPanelView_ipv || (this._fInfoPanelView_ipv = this._initInfoPanelView());
	}

	get targetingController()
	{
		return this._fTargetingController_tc || (this._fTargetingController_tc = this._initTargetingController());
	}

	get autoTargetingSwitcherController()
	{
		return this._fAutoTargetingSwitcherController_atsc || (this._fAutoTargetingSwitcherController_atsc = this._initAutoTargetingSwitcherController());
	}

	get gameOptimizationController()
	{
		return this._fGameOptimizationController_goc || (this._fGameOptimizationController_goc = this._initGameOptimizationController());
	}

	get cryogunsController()
	{
		return this._fCryogunsController_csc || (this._fCryogunsController_csc = this._initCryogunsController());
	}

	get flameThrowersController()
	{
		return this._fFlameThrowersController_ftsc || (this._fFlameThrowersController_ftsc = this._initFlameThrowersController());
	}

	get artilleryStrikesController()
	{
		return this._fArtilleryStrikesController_assc || (this._fArtilleryStrikesController_assc = this._initArtilleryStrikesController());
	}

	get plazmaGunsController()
	{
		return this._fPlazmaGunsController_pgsc || (this._fPlazmaGunsController_pgsc = this._initPlazmaGunsController());
	}

	get ricochetLasersController()
	{
		return this._fRicochetLasersController_rlsc || (this._fRicochetLasersController_rlsc = this._initRicochetLasersController());
	}

	get cursorController()
	{
		return this._fCursorController_cc || (this._fCursorController_cc = this._initCursorController());
	}

	get prizesController()
	{
		return this._fPrizesController_psc || (this._fPrizesController_psc = this._initPrizesController());
	}

	get shotResponsesController()
	{
		return this._fShotResponsesController_srsc || (this._fShotResponsesController_srsc = this._initShotResponsesController);
	}

	get specialWeaponPrizesController()
	{
		return this._fSpecialWeaponPrizesController_swpsc || (this._fSpecialWeaponPrizesController_swpsc = this._initSpecialWeaponPrizesController());
	}

	get buyAmmoRetryingController()
	{
		return this._fBuyAmmoRetryingController_barc || (this._fBuyAmmoRetryingController_barc = this._initBuyAmmoRetryingController());
	}

	get transitionViewController()
	{
		return this._fTransitionViewController_tvc || (this._fTransitionViewController_tvc = this._initTransitionViewController());
	}

	get battlegroundGameController()
	{
		return this._fBattlegroundGameController_bgc || (this._fBattlegroundGameController_bgc = this._initBattlegroundGameController());
	}

	get gameplayDialogController()
	{
		return this._fGameplayDialogController_gdsc || (this._fGameplayDialogController_gdsc = this._initGameplayDialogsController());
	}

	//AMMO BUY RETRY...
	_initBuyAmmoRetryingController()
	{
		if (this._fBuyAmmoRetryingController_barc)
		{
			return this._fBuyAmmoRetryingController_barc;
		}

		let l_gbarc = new GameBuyAmmoRetryingController();
		l_gbarc.on(GameBuyAmmoRetryingController.EVENT_ON_RETRY_BUY_IN_REQUIRED, this._onBuyAmmoRetryTime, this);
		l_gbarc.on(GameBuyAmmoRetryingController.EVENT_ON_RETRY_RE_BUY_REQUIRED, this._onReBuyAmmoRetryTime, this);
		l_gbarc.i_init();

		return l_gbarc;
	}

	_onBuyAmmoRetryTime()
	{
		this.sendBuyIn();
	}

	_onReBuyAmmoRetryTime()
	{
		this.sendReBuy();
	}

	_onFireCancelledWithNotEnoughAmmo()
	{
		if (
				!this._isFrbMode
				&& !this._isBonusMode
				&& !this._tournamentModeInfo.isTournamentMode
				&& !this.isBattlegroundMode
			)
		{
			this.sendBuyIn();
		}
	}
	//...AMMO BUY RETRY

	//SHOT RESPONSES WRAPPER...
	_initShotResponsesController()
	{
		if (this._fShotResponsesController_srsc)
		{
			return this._fShotResponsesController_srsc;
		}
		let l_srsc = new ShotResponsesController();
		l_srsc.i_init();
		this._fShotResponsesController_srsc = l_srsc;
		return l_srsc;
	}
	//...SHOT RESPONSES WRAPPER

	//PRIZES...
	_initPrizesController()
	{
		if (this._fPrizesController_psc)
		{
			return this._fPrizesController_psc;
		}
		let l_psc = new PrizesController();
		l_psc.i_init();
		this._fPrizesController_psc = l_psc;
		return l_psc;
	}

	_initSpecialWeaponPrizesController()
	{
		if (this._fSpecialWeaponPrizesController_swpsc)
		{
			return this._fSpecialWeaponPrizesController_swpsc;
		}
		let l_swpsc = new SpecialWeaponPrizesController();
		l_swpsc.i_init();
		l_swpsc.on(SpecialWeaponPrizesController.EVENT_ON_SPECIAL_WEAPON_PRIZE_LANDED, this._onSpecialWeaponPrizeLanded, this);
		this._fSpecialWeaponPrizesController_swpsc = l_swpsc;
		return l_swpsc;
	}

	_onSpecialWeaponPrizeLanded(event)
	{
		this.gameField.playerSeatBounce(event.seatId);
	}
	//...PRIZES

	//AWARDING...
	_initAwardingController()
	{
		if (this._fAwardingController_ac)
		{
			return this._fAwardingController_ac;
		}
		let l_ac = new AwardingController();
		l_ac.on(AwardingController.EVENT_ON_AWARD_COUNTED, this._onAwardCounted, this);
		l_ac.on(AwardingController.EVENT_ON_COIN_LANDED, this._onCoinLanded, this);
		l_ac.on(AwardingController.EVENT_ON_AWARD_ANIMATION_INTERRUPTED, this._onAwardAnimationInterrupted, this);
		l_ac.on(AwardingController.EVENT_ON_PENDING_AWARDS_DEVALUED, this._onPendingAwardsDevalued, this);
		l_ac.on(AwardingController.EVENT_ON_PENDING_AWARDS_SKIPPED, this._onPendingAwardsSkipped, this);

		l_ac.i_init();
		this._fAwardingController_ac = l_ac;
		return l_ac;
	}

	_onAwardCounted(event)
	{
		if (event.money)
		{
			if (!event.isQualifyWinDevalued && event.isMasterSeat)
			{
				this._transferWinToAmmo(event.money);
				if (event.rid === -1)
				{
					this._handleAwardFromCoPlayer();
				}
				else
				{
					this._sendBalanceUpdatedMessage(this.gameField.calcBalanceValue());
				}
			}
		}
		else if (event.score)
		{
			this.gameField.updatePlayerScore(event.score, true);
		}

		this.emit(AwardingController.EVENT_ON_AWARD_COUNTED, event);
	}

	_onCoinLanded(event)
	{
		if (event.money)
		{
			if (event.seatId === APP.playerController.info.seatId)
			{
				// do not bounce main player spot, only co-players'
				if(!APP.isBattlegroundGame)
				{
					this.gameField.updatePlayerWin(event.money, null, false /*aOptBounce_bl*/);
				}
			}
			else
			{
				this.gameField.playerSeatBounce(event.seatId);
			}
		}
	}

	_onAwardAnimationInterrupted(event)
	{
		if (event.money)
		{
			if (!event.isQualifyWinDevalued && event.isMasterSeat)
			{
				this._transferWinToAmmo(event.money);
				if (event.rid === -1)
				{
					this._handleAwardFromCoPlayer();
				}
			}

			this.gameField.updatePlayerWin(event.uncountedWin);
		}
		else if (event.score)
		{
			this.gameField.updatePlayerScore(event.score);
		}
	}

	_onPendingAwardsDevalued(event)
	{
		let devaluedPendingAwardsWin = event.devaluedPendingAwardsWin;
		if (devaluedPendingAwardsWin > 0)
		{
			this._transferWinToAmmo(devaluedPendingAwardsWin);
		}
	}

	_onPendingAwardsSkipped(event)
	{
		let skippedPendingAwardsWin = event.skippedPendingAwardsWin;
		if (skippedPendingAwardsWin > 0)
		{
			this._transferWinToAmmo(skippedPendingAwardsWin);
		}
	}
	//...AWARDING

	//TREASURES...
	_initTreasuresController()
	{
		if (this._fTreasureController_tc)
		{
			return this._fTreasureController_tc;
		}

		const l_tc = new TreasuresController();

		l_tc.i_init();
		this._fTreasureController_tc = l_tc
		return l_tc;
	}
	//...TREASURES

	//TRANSITION VIEW...
	_initTransitionViewController()
	{
		if (this._fTransitionViewController_tvc)
		{
			return;
		}

		let l_tvc = this._fTransitionViewController_tvc = new TransitionViewController();
		l_tvc.init();
		return l_tvc;
	}
	//...TRANSITION VIEW

	//BATTLEGROUND GAME CONTROLLER...
	_initBattlegroundGameController()
	{
		if (this._fBattlegroundGameController_bgc)
		{
			return;
		}

		let l_bgc = this._fBattlegroundGameController_bgc = new BattlegroundGameController();
		l_bgc.init();
		return l_bgc;
	}
	//...BATTLEGROUND GAME CONTROLLER

	//GAMEPLAY DIALOG...
	_initGameplayDialogsController()
	{
		if (this._fGameplayDialogController_gdsc)
		{
			return;
		}

		let l_gdsc = this._fGameplayDialogController_gdsc = new GameplayDialogsController();

		l_gdsc.gameBattlegroundCountDownDialogController.on(BattlegroundCountDownDialogController.EVENT_BATTLEGROUND_COUNTDOWN_CANCEL_CLICKED, this._cancelRoundClicked, this);
		return l_gdsc;
	}
	//...GAMEPLAY DIALOG

	_cancelRoundClicked()
	{
		this._fIsCountDownCanceled = true;

		if (this.gameStateController.info.isPlayerSitIn || this.player.sitInIsSent)
		{
			this.sitOut();
		}
		this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
		this.player.confirmIsSent = false;
		this.player.battlegroundBuyInConfirmIsApproved = false;
	}

	//CURSOR...
	_initCursorController()
	{
		if (this._fCursorController_cc)
		{
			return this._fCursorController_cc;
		}

		let lCursorAnimation = this.addChild(new CursorAnimation());

		this._fCursorController_cc = new CursorController(lCursorAnimation);
		this._fCursorController_cc.init();

		return this._fCursorController_cc;
	}
	//...CURSOR

	//BOSS MODE...
	_initBossModeController()
	{
		let l_bmc = new BossModeController(new BossModeInfo(), new BossModeView());

		l_bmc.on(BossModeController.EVENT_ON_COIN_LANDED, this._onCoinLanded, this);
		l_bmc.on(BossModeController.EVENT_ON_BOSS_WIN_AWARD_COUNTED, this._onBossWinAwardCounted, this);
		l_bmc.on(BossModeController.EVENT_ON_WIN_PRESENTATION_INTERRUPTED, this._onBossWinAnimationInterrupted, this);
		l_bmc.on(BossModeController.EVENT_ON_PENDING_BOSS_WINS_SKIPPED, this._onPendingBossWinsSkipped, this);

		return l_bmc;
	}

	_onBossWinAwardCounted(event)
	{
		if (event.money)
		{
			this._transferWinToAmmo(event.money);
			this._sendBalanceUpdatedMessage(this.gameField.calcBalanceValue());
		}
	}

	_onBossWinAnimationInterrupted(event)
	{
		if (event.uncountedWin)
		{
			this._transferWinToAmmo(event.uncountedWin);
		}

		if (event.notLandedWin)
		{
			this.gameField.updatePlayerWin(event.notLandedWin);
		}
	}

	_onPendingBossWinsSkipped(event)
	{
		let skippedPendingBossWinsValue = event.skippedPendingBossWinsValue;
		if (skippedPendingBossWinsValue > 0)
		{
			this._transferWinToAmmo(skippedPendingBossWinsValue);
			this.gameField.updatePlayerWin(skippedPendingBossWinsValue);
		}
	}
	//...BOSS MODE

	//MAPS...
	_initMapsController()
	{
		let l_msc = new MapsController();
		return l_msc;
	}
	//...MAPS

	//GAME OPTIMIZATION...
	_initGameOptimizationController()
	{
		let l_goc = new GameOptimizationController();
		return l_goc;
	}
	//...GAME OPTIMIZATION

	//CRYOGUN...
	_initCryogunsController()
	{
		let l_csc = new CryogunsController();
		return l_csc;
	}
	//...CRYOGUN

	//FLAMETHROWER...
	_initFlameThrowersController()
	{
		let l_ftc = new FlameThrowersController();
		return l_ftc;
	}
	//...FLAMETHROWER

	//ARTILLERYSTRIKE...
	_initArtilleryStrikesController()
	{
		let l_assc = new ArtilleryStrikesController();
		return l_assc;
	}
	//...ARTILLERYSTRIKE

	//PLAZMAGUN...
	_initPlazmaGunsController()
	{
		let l_pgsc = new PlazmaGunsController();
		return l_pgsc;
	}
	//...PLAZMAGUN

	//LASER...
	_initRicochetLasersController()
	{
		let l_rlsc = new RicochetLasersController();
		return l_rlsc;
	}
	//...LASER

	//SUBLOADING...
	_initSubloadingController()
	{
		let l_sc = new SubloadingController();

		return l_sc;
	}
	//...SUBLOADING

	//TIME OFFSET...
	_onSetEnemyTimeOffset(aEvent_obj)
	{
		let enemy = this.getExistEnemy(aEvent_obj.enemyId);

		if (!enemy)
		{
			APP.logger.i_pushWarning(`GameField. _onSetEnemyTimeOffset >> enemy with id ${aEvent_obj.enemyId} doesn't exist.`);
			console.log(`Error! _onSetEnemyTimeOffset >> enemy with id ${aEvent_obj.enemyId} doesn't exist`);
			return;
		}

		enemy.timeOffset = aEvent_obj.timeOffset;

		let lTrajectoryDuration_num = TrajectoryUtils.extractTrajectoryDuration(enemy.trajectory.points, 0);

		//limit duration maximum to make sure enemy will catch up his time offset until next UpdateTrajectory happens
		let lTimeRestoreDuration_num = Math.max(0, Math.min(lTrajectoryDuration_num - enemy.timeOffset, enemy.timeOffset * enemy.speed));

		let startDelay = aEvent_obj.startDelay || 0;


		let seq = [
			{
				tweens: [{prop: "timeOffset", to: 0}],
				duration: lTimeRestoreDuration_num,
				ease: Easing.sine.easeInOut
			}
		];
		let timeOffsetSequence = Sequence.start(enemy, seq, startDelay);
		enemy.timeOffsetSequence = timeOffsetSequence;

		enemy.allowUpdatePosition = true;
	}
	//...TIME OFFSET

	//HV ENEMIES...
	_initHvEnemiesController()
	{
		if (this._fHvEnemiesController_hesc)
		{
			return this._fHvEnemiesController_hesc;
		}

		let l_hesc = new HvEnemiesController();
		l_hesc.on(HvEnemiesController.EVENT_SET_HV_ENEMY_TIME_OFFSET, this._onSetHvEnemyTimeOffset, this);
		l_hesc.on(HvEnemiesController.EVENT_TIME_TO_CREATE_HV_ENEMY, this._onTimeToCreateHvEnemy, this);
		this._fHvEnemiesController_hesc = l_hesc;
		return l_hesc;
	}

	_onSetHvEnemyTimeOffset(aEvent_obj)
	{
		this._onSetEnemyTimeOffset(aEvent_obj);
	}

	_onTimeToCreateHvEnemy(aEvent_obj)
	{
		this.addEnemy(aEvent_obj.enemyData, null, true, false /*allowUpdatePosition*/, aEvent_obj.position, aEvent_obj.angle);
	}
	//..HV ENEMIES

	//WEAPONS...
	_initWeaponsController()
	{
		let l_wsc = new WeaponsController();
		l_wsc.on(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED, this._onFrbAmmoUpdated, this);
		return l_wsc;
	}

	_onFrbAmmoUpdated()
	{
		this._updateAmmo(0);
	}
	//...WEAPONS

	//BIG WINS...
	_initBigWinsController()
	{
		let l_bwsc = new BigWinsController();
		l_bwsc.on(BigWinsController.EVENT_ON_COIN_LANDED, this._onCoinLanded, this);
		l_bwsc.on(BigWinsController.EVENT_ON_BIG_WIN_AWARD_COUNTED, this._onBigWinAwardCounted, this);
		l_bwsc.on(BigWinsController.EVENT_ON_ANIMATION_INTERRUPTED, this._onBigWinAnimationInterrupted, this);
		l_bwsc.on(BigWinsController.EVENT_ON_PENDING_BIG_WINS_SKIPPED, this._onPendingBigWinsSkipped, this);
		return l_bwsc;
	}

	_onBigWinAwardCounted(event)
	{
		if (event.money)
		{
			this._transferWinToAmmo(event.money);
			this._sendBalanceUpdatedMessage(this.gameField.calcBalanceValue());
		}

		this.emit(BigWinsController.EVENT_ON_BIG_WIN_AWARD_COUNTED, event);
	}

	_onBigWinAnimationInterrupted(event)
	{
		if (event.uncountedWin)
		{
			this._transferWinToAmmo(event.uncountedWin);
		}

		if (event.notLandedWin)
		{
			this.gameField.updatePlayerWin(event.notLandedWin);
		}
	}

	_onPendingBigWinsSkipped(event)
	{
		let skippedPendingBigWinsValue = event.skippedPendingBigWinsValue;
		if (skippedPendingBigWinsValue > 0)
		{
			this._transferWinToAmmo(skippedPendingBigWinsValue);
			this.gameField.updatePlayerWin(skippedPendingBigWinsValue);
		}
	}
	//...BIG WINS

	//FIRE SETTINGS...
	_initFireSettingsController()
	{
		let l_fsc = new FireSettingsController();
		return l_fsc;
	}
	//...FIRE SETTINGS

	//INFO PANEL...
	_initInfoPanelController()
	{
		let l_ipc = new InfoPanelController();
		return l_ipc;
	}

	_initInfoPanelView()
	{
		let l_ipv = this.addChild(new InfoPanelView());
		return l_ipv;
	}
	//...INFO PANEL

	//TARGETING...
	_initTargetingController()
	{
		let l_tc = new TargetingController();
		return l_tc;
	}

	_initAutoTargetingSwitcherController()
	{
		let l_atsc = new AutoTargetingSwitcherController();
		return l_atsc;
	}
	//...TARGETING

	_initGameStateController()
	{
		if (this._fGameStateController_gsc)
		{
			return this._fGameStateController_gsc;
		}

		let l_gsc = new GameStateController();
		l_gsc.init();
		this._fGameStateController_gsc = l_gsc;
		return l_gsc;
	}

	_initGameFrbController()
	{
		if (this._fGameFrbController_gfrbc)
		{
			return this._fGameFrbController_gfrbc;
		}

		let l_gfrbc = new GameFRBController();
		l_gfrbc.init();
		this._fGameFrbController_gfrbc = l_gfrbc;
		return l_gfrbc;
	}

	_initGameBonusController()
	{
		if (this._fGameBonusController_gbc)
		{
			return this._fGameBonusController_gbc;
		}

		let l_gbc = new GameBonusController();
		l_gbc.init();
		this._fGameBonusController_gbc = l_gbc;

		l_gbc.on(GameBonusController.EVENT_ON_BONUS_SIT_OUT_REQUIRED, this._onBonusSitOutRequired, this);
		l_gbc.on(GameBonusController.EVENT_ON_BONUS_BACK_TO_LOBBY_REQUIRED, this._onBonusBackToLobbyRequired, this);
		l_gbc.on(GameBonusController.EVENT_ON_FRB_BACK_TO_LOBBY_REQUIRED, this._onFRBBackToLobbyRequired, this);

		return l_gbc;
	}

	_onBonusSitOutRequired()
	{
		this.sitOut();
	}

	_onBonusBackToLobbyRequired()
	{
		this.goBackToLobby();
	}

	_onFRBBackToLobbyRequired()
	{
		this.goBackToLobby();
	}

	_initGamePleaseWaitDialogController()
	{
		let l_gpwdc = new GamePleaseWaitDialogController();
		l_gpwdc.init();
	}

	gameStateChangedResponse(data)
	{
		this.room.state = data.state;
		this.room.ttnx = data.ttnx;
		this.room.roundId = data.roundId;

		if(APP.isCAFMode)
		{
			if (
				this._fGameplayDialogController_gdsc 
				&& this._fGameplayDialogController_gdsc.info
				&& this._fGameplayDialogController_gdsc.info.hasActiveDialogWithId(DIALOG_ID_CAF_PRIVATE_ROUND_COUNT_DOWN)
				&& this.room.state == ROUND_STATE.WAIT
				)
			{
				this._requestFullGameInfo();
			}
		}

		if (this.room.state == ROUND_STATE.PLAY)
		{
			this._requestFullGameInfo();
		}

		this.gameField.changeState(this.room.state);

		this.emit(GameScreen.EVENT_ON_ROUND_ID_UPDATED, {roundId: data.roundId});

		let l_gwsic = APP.webSocketInteractionController;
		if (
				this.isBattlegroundMode
				&& APP.isBattlegroundGamePlayMode
				&& !APP.playerController.info.isObserver
				&& !(this.player.seatId >= 0)
				&& this._isRoomSitItAllowedState
				&& !l_gwsic.isSitoutRequestInProgress
				&& !l_gwsic.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_IN)
				&& !l_gwsic.hasUnRespondedRequest(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
				&& !l_gwsic.hasDelayedRequests(CLIENT_MESSAGES.SIT_IN)
				&& !l_gwsic.hasDelayedRequests(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
			)
		{
			this.sendSitIn(true);
			return;
		}

		if (this._canJoinRoom() && this.player.seatId === undefined)
		{
			if (
				!APP.isBattlegroundGamePlayMode
				|| APP.isBattlegroundGamePlayMode && this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
				)
			{
				this.sendSitIn();
			}

		}
	}

	get isRoomGameActive()
	{
		return (this.room.state == ROUND_STATE.PLAY);
	}

	roundResultResponse(aData_obj)
	{
		let lWeaponsReturned_arr_num = aData_obj.weaponsReturned ? aData_obj.weaponsReturned : [];
		this.emit(GameScreen.EVENT_ROUND_RESULT_RETURN_SW_RESPONSE, {weaponsReturned: lWeaponsReturned_arr_num, weaponSurplus: aData_obj.weaponSurplus});

		this.gameField.roundResultScreenController.onRoundResultResponse(aData_obj);

		this.emit(GameScreen.EVENT_ON_NEXT_MAP_UPDATED, {nextMapId: aData_obj.nextMapId});
		this._clearAmmo();
		this.gameField.onClearAmmo();

		if (this.isPaused)
		{
			this.gameField && this.gameField.clearWeapons();
		}

		if (APP.isBattlegroundGamePlayMode)
		{
			this._requestFullGameInfo()
		}
	}

	_onBattlegroundChangeWorldButtonClicked()
	{
		if (this.gameStateController.info.isPlayerSitIn)
		{
			this.sitOut();
		}
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_BACK_TO_MQB_LOBBY);
	}

	onRoundResultActivated(event)
	{
		if (!this.gameStateController.info.isGameInProgress)
		{
			this.clear();
		}

		if (this.gameStateController.info.isWaitState)
		{
			this._requestFullGameInfo();
		}
		else if (APP.webSocketInteractionController.isConnectionOpened)
		{
			this._onNeedFullGameInfoUpdateOnGameStatePlayOrWait();
		}

		if (APP.webSocketInteractionController.info.isCriticalErrorDelayed(GameWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED))
		{
			if (!event.needStartSummary)
			{
				this.gameField.roundResultScreenController.once(RoundResultScreenController.ON_FULL_MAIN_PANEL_SEQUENCE_COMPLETED, this._onRoundResultScreenFullMainPanelSequenceComplited, this);
			}
			else
			{
				this.gameField.roundResultScreenController.off(RoundResultScreenController.ON_FULL_MAIN_PANEL_SEQUENCE_COMPLETED, this._onRoundResultScreenFullMainPanelSequenceComplited, this, true);
				this._forceDeactivatedRoomDelayedErrorIfRequired();
			}
		}
	}

	_onRoundResultScreenFullMainPanelSequenceComplited(event)
	{
		this._forceDeactivatedRoomDelayedErrorIfRequired();
	}

	_forceDeactivatedRoomDelayedErrorIfRequired()
	{
		if (!APP.webSocketInteractionController.info.isCriticalErrorDelayed(GameWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED))
		{
			return;
		}

		console.error("GameScreen _forceDeactivatedRoomDelayedErrorIfRequired");
		APP.logger.i_pushError(`GameScreen delayed error code ROOM_WAS_DEACTIVATED (${GameWebSocketInteractionController.ERROR_CODES.ROOM_WAS_DEACTIVATED}) forced.`)
		this.gameField && this.gameField.clearRoom();
		this.clear();
		this.clearAllButLobbyInfo();

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_CAF_DEACTIVATED_ROOM_DELAYED_ERROR_TIME);
	}

	_onRoundResultActivationStart()
	{
		this.emit(GameScreen.EVENT_ROUND_RESULT_RETURN_SW_DIALOG_SHOW_VALIDATE);
	}

	_onNeedFullGameInfoUpdateOnGameStatePlayOrWait()
	{
		if (!this.isBattlegroundMode)
		{
			return;
		}

		if (this.gameStateController.info.isPlayerSitIn)
		{
			this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = true;
			this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo, this);
			this.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo, this);
		}
		else
		{
			this._gameUrlNeedToUpdate();
		}
	}

	_onBattlegroundNextRoundClicked()
	{
		let roundState = this.gameStateController.info.gameState;
		if (roundState == ROUND_STATE.QUALIFY)
		{
			if (APP.webSocketInteractionController.isConnectionOpened)
			{
				this._onNeedFullGameInfoUpdateOnGameStatePlayOrWait();
			}
		}
		else
		{
			this._requestFullGameInfo();
		}

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.gameField.roundResultScreenController.view.visible});
		this.player.lastReceivedBattlegroundScore_int = 0;
		this.gameField.spot && this.gameField.spot.setScore();
	}

	_onCheckGameStateChangedForRequestFullGameInfo(e)
	{
		if (this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl && (e.value == ROUND_STATE.PLAY || e.value == ROUND_STATE.WAIT))
		{
			if (this.gameStateController.info.isPlayerSitIn)
			{
				this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = false;
				this._requestFullGameInfo();
			}
			else
			{
				if (
						this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
						|| APP.pendingOperationController.info.isPendingOperationInProgress
					)
				{
					this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = false;
				}
				else
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.gameField.roundResultScreenController.view.visible});
				}
			}

			this.gameStateController && this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo);
		}
		else if (e.value == ROUND_STATE.CLOSED)
		{
			this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = false;
			this.gameStateController && this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo);
		}
	}

	updatePlayersInfo(data, redraw=true)
	{
		for (let seat of data.seats)
		{
			seat.seatId = seat.id;
			seat.weapon = {
				id: seat.specialWeaponId
			}
		}

		this.room.seats = data.seats;

		if (this.player.seatId === undefined && !this._roomHasEmptySeats)
		{
			redraw = false;
		}

		if (redraw)
		{
			this.gameField.drawAllPlayers(this.room.seats, this.player ? this.player.seatId : -1, true);
		}
	}

	addWeapon(weapon, pos, enemyLife, optMultipleWeapons, optFinalPosition)
	{
		if (!this.isPaused)
		{
			this.emit(GameScreen.EVENT_ON_WEAPON_AWARDED, {weapon: weapon});
			this.gameField.showAddWeapon(weapon, pos, enemyLife, optMultipleWeapons, optFinalPosition);
		}
		else
		{
			this.emit(GameScreen.EVENT_ON_TIME_TO_HANDLE_AWARDED_WEAPON, {weapon: weapon});
		}
	}

	onWeaponSwitched(data)
	{
		if (this.room && this.room.seats)
		{
			for (let i = 0; i < this.room.seats.length; i++)
			{
				let lRoomSeat_obj = this.room.seats[i];
				if (lRoomSeat_obj.seatId == data.seatId && data.rid == -1)
				{
					if (lRoomSeat_obj.specialWeaponId != data.weaponId)
					{
						lRoomSeat_obj.specialWeaponId = data.weaponId;
					}

					if (
							APP.isBattlegroundGame
							&& data.weaponId == WEAPONS.DEFAULT
							&& (this.gameStateController.info.isQualifyState)
						)
					{
						let lPlayerInfo_pi = APP.playerController.info;
						lRoomSeat_obj.betLevel = lPlayerInfo_pi.roomDefaultBetLevel;
					}
				}
			}
		}

		this.gameField.showPlayersWeaponSwitched(data);
	}

	//BONUS...
	_onBonusStateChanged(e)
	{
		if (e.noWait)
		{
			return;
		}

		this._fBonusRoundEndedPostponedData_obj = {};
	}

	_onTimeToOpenRealRoomAfterBonus(data)
	{
		if (data.roomId && !isNaN(data.roomId) && data.roomId > -1)
		{
			APP.urlBasedParams.roomId = data.roomId;
		}

		//clearing...
		this._fRoundFinishSoon_bl = false;
		this._fCompensationDialogActive_bln = false;
		this._restoreAfterLagsInProgress = false;
		this.gameField.roundResultScreenController.hideScreen();
		this.gameField.roundResultScreenController.resetScreenAppearing();
		//...clearing

		this.emit(GameScreen.EVENT_ON_TIME_TO_OPEN_REAL_ROOM_AFTER_BONUS);
	}
	//...BONUS

	onFRBEnded()
	{
		this._fFrbEndedPostponedData_obj = {
			newStatus: APP.currentWindow.gameFrbController.info.frbEndReason,
			hasNextFrb: APP.currentWindow.gameFrbController.info.nextModeFrb,
			winSum: APP.currentWindow.gameFrbController.info.winSum,
			realWinSum: APP.currentWindow.gameFrbController.info.realWinSum
		};
	}

	_debugUpdateTrajectories()
	{
		let data = {};
		let trajectories = {};
		for (let i=0; i<this.enemies.length; i++)
		{
			let enemy = this.enemies[i];
			let trajectory = {};
			let points = [];
			let nStartTime = (new Date()).getTime();
			let nEndTime = nStartTime + 10000;
			let currentPoint = this.getEnemyPosition(enemy);

			points.push({x:currentPoint.x,y:currentPoint.y,time:nStartTime});
			points.push({x: 56, y: 93, time: nEndTime});
			trajectory.points = points;
			trajectory.speed = 4;
			trajectories[enemy.id] = trajectory;
		}

		data.trajectories = trajectories;
		this.onUpdateTrajectories(data);
	}

	//DEBUG...
	_onStubUpdateTrajectory(e)
	{
		let data = {};
		let trajectories = {};
		let currentPoint = null;

		serverTime = this.currentTime;
		clientTimeDiff = 0;

		let enemyId = e.enemyId;
		let enemyInfo = this.getExistEnemy(enemyId);
		if (!enemyInfo)
		{
			currentPoint = e.firstPoint;
		}
		else
		{
			currentPoint = this.getEnemyPosition(enemyInfo);
		}

		let nStartTime = this.currentTime;
		let nEndTime = nStartTime + 10000;

		let points = [];
		let trajectory = {};
		points.push({x:currentPoint.x, y:currentPoint.y, time:nStartTime});
		points.push({x: 0, y: 0, time: nEndTime});
		trajectory.points = points;
		trajectory.speed = 10;
		trajectories[enemyId] = trajectory;

		data.trajectories = trajectories;
		this.onUpdateTrajectories(data);
	}
	//...DEBUG

	_randomDirectionsTrajectoriesDebug()
	{
		let data = {};
		let trajectories = {};

		for (let i = 0; i < this.enemies.length; ++i)
		{
			let enemy = this.enemies[i];

			let trajectory = {};
			let points = [];
			if (enemy.name === ENEMIES.BlackAnt || enemy.name === ENEMIES.RedAnt)
			{
				let nStartTime = this.currentTime;
				let currentPoint = this.getEnemyPosition(enemy);

				points.push({x:currentPoint.x,y:currentPoint.y,time:nStartTime});

				let nEndTime = nStartTime;
				for (let j = 0; j < 40; ++j)
				{
					nEndTime += Utils.random(4000, 12000);
					points.push({x: Utils.random(10, 950), y: Utils.random(10, 530), time: nEndTime});
				}

				trajectory.points = points;
				trajectory.speed = 4;
				trajectories[enemy.id] = trajectory;
			}
			else
			{
				trajectory.points = [];
				trajectory.speed = 0;
				trajectories[enemy.id] = trajectory;
			}
		}

		data.trajectories = trajectories;

		this.onUpdateTrajectories(data);
	}

	onUpdateTrajectories(data)
	{
		this.emit(GameScreen.EVENT_ON_TRAJECTORIES_UPDATED, {data: data});

		for (let enemyId in data.trajectories)
		{
			//console.log("enemyId = " + enemyId)
			let trajectory = data.trajectories[enemyId];
			this._updateEnemyTrajectory(enemyId, trajectory, data.freezeTime);
			// if (this.checkExistEnemy(enemyId))
			// {

			// }
		}
	}

	_updateEnemyTrajectory(enemyId, trajectory, freezeTime = 0)
	{
		let enemy = this.getExistEnemy(enemyId);
		if (enemy)
		{
			let lIsBoss_lb = enemy.typeId == ENEMY_TYPES.BOSS;
			if (lIsBoss_lb)
			{
				//https://jira.dgphoenix.com/browse/MQ-1307
				//keep the original first point, to determine the very first step of the Boss for sure
				let lFirstPoint_pt = enemy.trajectory.points[0];
				let pt = {x: lFirstPoint_pt.x, y: lFirstPoint_pt.y, time: lFirstPoint_pt.time};
				trajectory.points.unshift(pt);
			}
			enemy.trajectory = trajectory;
			this._correctTrajectoryPointsIfRequired(enemy, freezeTime);
			enemy.speed = trajectory.speed;
			this.gameField.updateEnemyTrajectory(enemy.id, enemy.trajectory);
		}
		else
		{
			APP.logger.i_pushWarning(`GameField. No enemy with id ${enemyId} found for updating trajectory!`);
			console.log("No enemy with id " + enemyId + " found for updating trajectory!"); //this is probably HV enemy not yet added to the screen, awaiting for parent dying
		}
	}

	get roundFinishSoon()
	{
		return this._fRoundFinishSoon_bl;
	}

	set roundFinishSoon(value)
	{
		this._fRoundFinishSoon_bl = value;
	}

	getMassDamageEffectedEnemies(rid)
	{
		let res = [];
		for (let i = 0; i < this.massDamageAffectedEnemies.length; ++i)
		{
			if (this.massDamageAffectedEnemies[i].rid == rid)
			{
				res.push(this.massDamageAffectedEnemies[i]);
				this.massDamageAffectedEnemies.splice(i, 1);
				--i;
			}
		}

		return res;
	}

	missResponse(data, aIsPaidSpecialShot_bl, aRequestEnemyId_int = undefined)
	{
		if (!this.room)
		{
			return;
		}

		if (data.bossNumberShots)
		{
			this.bossModeController.bossNumberShots = data.bossNumberShots;
		}

		let lPlayerSeat_obj = this.gameField.getSeat(data.seatId, true);
		if (lPlayerSeat_obj)
		{
			data.playerName = lPlayerSeat_obj.nickname;
		}

		if (data.killedMiss)
		{
			let enemy = this.getExistEnemy(data.enemyId);
			if (enemy)
			{
				enemy.killedMiss = true;
				this.emit(GameScreen.EVENT_ON_KILLED_MISS_ENEMY, {enemyId: data.enemyId});
			}
		}

		if (this.isPaused)
		{
			if (data.rid != -1 && (data.killedMiss == true || data.invulnerable == true))
			{
				this.emit(GameScreen.EVENT_ON_REVERT_AMMO_BACK, {weaponId: data.usedSpecialWeapon,
					revertByRoundNotStartedError: false,
					revertAmmoAmount: data.betLevel,
					isPaidSpecialShot: aIsPaidSpecialShot_bl});
			}
			return;
		}

		this.massDamageAffectedEnemies.push({rid: data.rid, enemyId: data.enemyId, data: data});
		if (data.lastResult)
		{
			let enemies = this.getMassDamageEffectedEnemies(data.rid);
			data.affectedEnemies = enemies;
			data.requestEnemyId = aRequestEnemyId_int;
		}
		else
		{
			return;
		}

		if (data.rid != -1)
		{
			this.gameField.onShotResponse();

			if ((data.killedMiss == true || data.invulnerable == true))
			{
				this.revertAmmoBack(data.usedSpecialWeapon, data.betLevel, aIsPaidSpecialShot_bl);
			}
		}

		this.gameField.showMiss(data, aIsPaidSpecialShot_bl);
	}

	revertAmmoBack(weaponId, aRequestBetLevel_num, aIsPaidSpecialShot_bl, optIsRoundNotStarted, aOptSpecialAmmoAmount_num = 1)
	{
		let lBetLevel_num = aRequestBetLevel_num ? aRequestBetLevel_num : APP.playerController.info.betLevel;
		let lRevertAmmoAmount_int = (aIsPaidSpecialShot_bl ? this.weaponsController.info.i_getWeaponShotPriceConvertedIntoDefaultAmmo(weaponId) : lBetLevel_num) || 1;
		lRevertAmmoAmount_int *= aOptSpecialAmmoAmount_num;
		if (isNaN(lRevertAmmoAmount_int) || lRevertAmmoAmount_int < 1)
		{
			throw new Error(`Incorrect revert ammo amount: ${lRevertAmmoAmount_int}`);
		}

		this.emit(GameScreen.EVENT_ON_REVERT_AMMO_BACK, {weaponId: weaponId,
			revertByRoundNotStartedError: optIsRoundNotStarted,
			revertAmmoAmount:lRevertAmmoAmount_int,
			isPaidSpecialShot: aIsPaidSpecialShot_bl});
		this.gameField.handleAmmoBackMessage();
	}

	onShotResponse()
	{
		this.gameField.onShotResponse();
	}

	hitResponse(data, aIsPaidSpecialShot_bl, aRequestEnemyId_int = undefined)
	{
		if (!this.room)
		{
			return;
		}

		if (data.bossNumberShots)
		{
			this.bossModeController.bossNumberShots = data.bossNumberShots;
		}

		data.awardedWin = Number(data.win);

		let lPlayerSeat_obj = this.gameField.getSeat(data.seatId, true);
		if (lPlayerSeat_obj)
		{
			data.playerName = lPlayerSeat_obj.nickname;
		}

		if (+data.damage > 0)
		{
			this._damageEnemyEnergy(data.enemy.id, +data.damage);
		}

		if (data.killed)
		{
			this.destroyEnemy({enemyId: data.enemy.id, reason: 0});
		}

		let enemiesInstantKilled = data.enemiesInstantKilled;

		if (enemiesInstantKilled && !Utils.isEmptyObject(enemiesInstantKilled))
		{
			let instantKillsSum = 0;

			for (var key in enemiesInstantKilled)
			{
				let curInstantKillPrizes = enemiesInstantKilled[key];

				for (let i = 0; i < curInstantKillPrizes.length; i++)
				{
					if (curInstantKillPrizes[i].id == HIT_RESULT_SINGLE_CASH_ID)
					{
						instantKillsSum += Number(curInstantKillPrizes[i].value);
					}
				}

				this._removeEnemyFromRoomInfoById(key);
				this.emit(GameScreen.EVENT_DESTROY_ENEMY, {enemyId: key});
			}

			if (instantKillsSum > 0 && data.seatId === this.gameField.seatId)
			{
				data.win -= instantKillsSum;
				data.awardedWin -= instantKillsSum;
				data.currentWin -= instantKillsSum;
				data.hitResultBySeats[data.seatId][0].value = data.win.toString();
			}

			this.emit(GameScreen.EVENT_ON_BOMB_ENEMY_KILLED, data);
		}

		if (this.isPaused)
		{
			let lWin_num = data.awardedWin;

			if (lWin_num > 0)
			{
				this._transferWinToAmmo(lWin_num);
				this.gameField.updatePlayerWin(lWin_num);
			}

			if (data.killBonusPay > 0)
			{
				lWin_num = data.killBonusPay;

				this._transferWinToAmmo(lWin_num);
				this.gameField.updatePlayerWin(lWin_num);
			}

			if (data.awardedWeapons && data.awardedWeapons.length)
			{
				while (data.awardedWeapons.length)
				{
					let lSpecWeapon_obj = data.awardedWeapons.shift();
					let lWeapon_obj = {id: lSpecWeapon_obj.id, shots: lSpecWeapon_obj.shots};
					this.emit(GameScreen.EVENT_ON_WEAPON_AWARDED, {weapon: lWeapon_obj});
					this.gameField.updateWeaponImmediately(lWeapon_obj);
				}
			}
			else if (data.awardedWeaponId != WEAPONS.DEFAULT)
			{
				let lWeapon_obj = {id: data.awardedWeaponId, shots: data.awardedWeaponShots};
				this.emit(GameScreen.EVENT_ON_WEAPON_AWARDED, {weapon: lWeapon_obj});
				this.gameField.updateWeaponImmediately(lWeapon_obj);
			}

			return;
		}
		else
		{
			let lAwardedWeapons_arr = [];
			if (data.awardedWeapons && data.awardedWeapons.length)
			{
				for (let i = 0; i < data.awardedWeapons.length; ++i)
				{
					let lWeapon_obj = {id: data.awardedWeapons[i].id, shots: data.awardedWeapons[i].shots};
					lAwardedWeapons_arr.push(lWeapon_obj);
				}
			}
			else if (data.awardedWeaponId != WEAPONS.DEFAULT)
			{
				let lWeapon_obj = {id: data.awardedWeaponId, shots: data.awardedWeaponShots};
				lAwardedWeapons_arr.push(lWeapon_obj);
			}

			if (lAwardedWeapons_arr.length)
			{
				if (!this._fMasterSWAwardsQueue_obj_arr || !this._fMasterSWAwardsQueue_obj_arr.length)
				{
					this._fMasterSWAwardsQueue_obj_arr = lAwardedWeapons_arr;
				}

				for (let i = 0 ; i < lAwardedWeapons_arr.length; i++)
				{
					if (this._fMasterSWAwardsExpected_obj_arr[lAwardedWeapons_arr[i].id])
					{
						this._fMasterSWAwardsExpected_obj_arr[lAwardedWeapons_arr[i].id].shots += lAwardedWeapons_arr[i].shots;
					}
					else
					{
						this._fMasterSWAwardsExpected_obj_arr[lAwardedWeapons_arr[i].id] = {};
						this._fMasterSWAwardsExpected_obj_arr[lAwardedWeapons_arr[i].id].shots = lAwardedWeapons_arr[i].shots;
					}
				}
			}
		}

		if (!this.isPaused && !this.gameField.getSeat(data.seatId, true))
		{
			let lWin_num = data.awardedWin;
			if (lWin_num > 0)
			{
				this._transferWinToAmmo(lWin_num);
				this.gameField.updatePlayerWin(lWin_num);
			}
		}

		data = this._disableSpecialFeaturesForCoopPlayer(data);

		this.massDamageAffectedEnemies.push({rid: data.rid, enemyId: data.enemy.id, data: data});
		if (data.lastResult)
		{
			let enemies = this.getMassDamageEffectedEnemies(data.rid);
			data.affectedEnemies = enemies;
			data.requestEnemyId = aRequestEnemyId_int;
		}
		else
		{
			return;
		}

		if (data.rid != -1)
		{
			this.gameField.onShotResponse();
		}

		//check Big Win...
		 const lIsBigWin_bl = this.bigWinsController.i_isBigWin(data);
		let lIsSkipAwardedWinRequired_bl = lIsBigWin_bl; // skip if big win needed
		data.skipAwardedWin = lIsSkipAwardedWinRequired_bl;
		data.isKilledBossHit = this.bossModeController.i_isKilledBossWin(data);
		for (let affectedEnemy of data.affectedEnemies)
		{
			affectedEnemy.data.skipAwardedWin = lIsSkipAwardedWinRequired_bl;
			affectedEnemy.data.isKilledBossHit = data.isKilledBossHit;
		}
		//...check Big Win

		this.gameField.showHit(data, aIsPaidSpecialShot_bl);
	}

	_disableSpecialFeaturesForCoopPlayer(aData_obj)
	{
		let lSeatId = APP.playerController.info.seatId;

		if (aData_obj.seatId !== lSeatId)
		{
			aData_obj.chMult = 1; // Don't show critical hits to another players

			let lIsKilledBossWin_bl = aData_obj.killed && aData_obj.enemy.typeId == ENEMY_TYPES.BOSS;
			if (lIsKilledBossWin_bl)
			{
				// keep killBonusPay for killed boss hits
			}
			else
			{
				aData_obj.awardedWin += (aData_obj.killBonusPay || 0);
				aData_obj.killBonusPay = 0; // Don't show killBonusPay hits to another players
			}
		}

		//Critical hit debug...
		// aData_obj.chMult = 2;
		//...Critical hit debug

		return aData_obj;
	}

	_onNotEnougMoneyDialogRequired(event)
	{
		this.emit(GameScreen.EVENT_ON_RESET_TARGET);

		let lNEMData_obj = {};
		if (event.hasUnrespondedShots !== undefined)
		{
			lNEMData_obj.hasUnrespondedShots = event.hasUnrespondedShots;
		}

		if (event.hasDelayedShots !== undefined)
		{
			lNEMData_obj.hasDelayedShots = event.hasDelayedShots;
		}

		if (event.hasUnparsedShotResponse !== undefined)
		{
			lNEMData_obj.hasUnparsedShotResponse = event.hasUnparsedShotResponse;
		}

		if (event.hasAwardedFreeSW !== undefined)
		{
			lNEMData_obj.hasAwardedFreeSW = event.hasAwardedFreeSW;
		}

		this.emit(GameScreen.EVENT_NOT_ENOUGH_MONEY_DIALOG_REQUIRED, lNEMData_obj);
	}

	_onSWPurchaseLimitExceededDialogRequired()
	{
		this.emit(GameScreen.EVENT_SW_PURCHASE_LIMIT_EXCEEDED_DIALOG_REQUIRED);
	}

	_handleAwardFromCoPlayer()
	{
		this._sendBalanceUpdatedMessage(this.gameField.calcBalanceValue());
	}

	_onServerFRBEndedMessage()
	{
		let lFrbInfo_bi = this.gameFrbController.info;
		this.gameField.isWinLimitExceeded = lFrbInfo_bi.isWinLimitExceeded;
	}

	_onBulletResponse(aEvent_obj)
	{
		this.emit(GameScreen.EVENT_ON_BULLET_RESPONSE, {data: aEvent_obj.messageData});
	}

	_onBulletClearResponse(aEvent_obj)
	{
		this.emit(GameScreen.EVENT_ON_BULLET_CLEAR_RESPONSE, {data: aEvent_obj.messageData});
	}


	_onCancelBattlegroundRound(aEvent_obj)
	{
		if (APP.isCAFMode && APP.playerController.info.isKicked)
		{
			return;
		}

		let l_gsi = this._fGameStateController_gsc.info;
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_ROUND_CANCELED, {isGameInProgress: l_gsi.isGameInProgress, isWaitState: l_gsi.isWaitState, refundedAmount: aEvent_obj.messageData.refundedAmount });
		this.emit(GameScreen.EVENT_ON_BATTLEGROUND_ROUND_CANCELED, {isGameInProgress: l_gsi.isGameInProgress, isWaitState: l_gsi.isWaitState, refundedAmount: aEvent_obj.messageData.refundedAmount });

		if (l_gsi.isGameInProgress && l_gsi.isQualifyState && this.gameField.roundResultActive)
		{
			return;
		}

		if (!APP.webSocketInteractionController.isSitoutRequestInProgress)
		{
			this._fIsSitOutOnNotEnoughPlayersExpected_bl = true;
			this.sitOut();
			this.clear();
		}
	}

	_onServerBalanceUpdatedMessage()
	{
		let lBalance_num;
		if (this.gameField && this.gameStateController.info.isPlayerSitIn)
		{
			lBalance_num = this.gameField.calcBalanceValue();
			let lWeaponsInfo_wsi = this.weaponsController.info;
			let lCurrentShotCost_num = lWeaponsInfo_wsi.i_getCurrentWeaponShotPrice();
			
			this._fIsExpectedBuyIn_bln = lBalance_num >= lCurrentShotCost_num;

			if (lBalance_num >= lCurrentShotCost_num && this.weaponsController.i_getInfo().ammo < lWeaponsInfo_wsi.i_getCurrentWeaponShotPriceConvertedIntoDefaultAmmo()) //[Y]TODO if currently selected is Free SW? do we really need re-buy
			{
				let lRoundResultActive_bln = this.gameField.roundResultActivationInProgress || this.gameField.roundResultActive;
				let lIsDefaultWeaponSelected_bln = this.weaponsController.i_getInfo().currentWeaponId === WEAPONS.DEFAULT;

				if (!lRoundResultActive_bln && (lIsDefaultWeaponSelected_bln || !APP.gameSettingsController.info.isBuyInNotMandatoryInRound))
				{
					if (this._tournamentModeInfo.isTournamentMode)
					{
						// in tournament mode auto BuyIn should never occur
					}
					else if (this._isBonusMode)
					{
						// in cash bonus mode auto BuyIn should never occur
					}
					else if (this._isFrbMode)
					{
						// in frb mode auto BuyIn should never occur
					}
					else if (this.isBattlegroundMode)
					{
						// in isBattlegroundMode mode auto BuyIn should never occur
					}
					else
					{
						this.gameField.tryToBuyAmmo();
					}
				}
			}
		}

		this._sendBalanceUpdatedMessage(lBalance_num);

		if(this._fGameStateController_gsc.info.isPlayState)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_IN_PROGRESS);
		}
	}

	_sendBalanceUpdatedMessage(aSpecificBalance_num)
	{
		let lKeepNotEnoughMoneyDialog_bln = false;

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.SERVER_BALANCE_UPDATED_MESSAGE_RECIEVED, {keepDialog: lKeepNotEnoughMoneyDialog_bln, specBalance: aSpecificBalance_num});
	}

	updateEnemyHealth(enemy, params)
	{
		if (params.life === 0)
		{
			enemy.life = params.life;
		}

		enemy.awardedPrizes = params.awardedPrizes;
	}

	getRoomInfoResponse(data)
	{
		//Full room info(players, enemies, weapons etc)
		this.room = {
			id: data.roomId,
			maxSeats: data.maxSeats,
			minBuyIn: data.minBuyIn,
			playerStake: (data.playerStake && data.playerStake !== -1) ? data.playerStake : this.player.stake,
			name: data.name,
			state: data.state,
			ttnx: data.ttnx,
			zombies: data.enemies,
			roomEnemies: data.roomEnemies,
			weapons: data.weapons, // obsolete and no longer used
			seats: data.seats,
			width: data.width,
			height: data.height,
			mapId: data.mapId,
			alreadySitInNumber: data.alreadySitInNumber,
			alreadySitInAmmoCount: data.alreadySitInAmmoCount,
			alreadySitInWin: data.alreadySitInWin,
			freezeTime: data.freezeTime,
			roundId: data.roundId
		};
		this._roomId = data.roomId;

		this._fIsFullGameInfoUpdateExpected_bl = false;

		if (data.alreadySitInNumber === -1)
		{
			this.player.sitIn = false;
			this.player.seatId = undefined;
			APP.playerController.updateMasterPlayerSeatId(undefined);
			this._fIsSitOutOnNotEnoughPlayersExpected_bl = false;
		}

		//BATTLEGROUND...
		if(data.battlegroundInfo)
		{
			this.clearPendingRequestSitin();
			this._resetWaitingBattlegroundReBuyOnGameState();

			if (
				(this.room.state == ROUND_STATE.PLAY)
				&& APP.isBattlegroundGamePlayMode
				&& !this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
				&& !this.gameStateController.info.isPlayerSitIn)
			{
				this._fNeedUpdateStartGameUrl_bl = false;

				this._gameUrlNeedToUpdate();
				return;
			}

			if(this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
				&& data.state === ROUND_STATE.WAIT
				&& this.battlegroundGameController.info.isNotFirstBattlegroundRoundAfterLoading
				&& !data.battlegroundInfo.buyInConfirmed)
			{
				this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
			}

			let lBattlegroundInfo_obj = data.battlegroundInfo;
			this._fIsBattlegroundMode_bl = true;
			this.player.confirmIsSent = lBattlegroundInfo_obj.buyInConfirmed;
			this.player.battlegroundBuyInConfirmIsApproved = lBattlegroundInfo_obj.buyInConfirmed;
			this.player.battlegroundBuyInCost_num = data.battlegroundInfo.buyIn;
			APP.gameScreen.gameField.roundResultScreenController.info.battlegroundBuyIn = this._fbattlegroundBuyInCost_num;
			this.player.battlegroundPotTaxMultiplier_num = (1 - data.battlegroundInfo.potTaxPercent * 0.01);
			if (data.alreadySitInWin > 0)
			{
				this.player.lastReceivedBattlegroundScore_int = data.alreadySitInWin;
				this.player.currentWin = data.alreadySitInWin;
			}

			if(APP.isCAFMode)
			{
				if (data.battlegroundInfo.joinUrl !== undefined && data.battlegroundInfo.joinUrl !== this.room.joinUrl)
				{
					this.room.joinUrl = data.battlegroundInfo.joinUrl;
				}
			}

			if(data.battlegroundInfo.potTaxPercent === undefined)
			{
				this.player.battlegroundPotTaxMultiplier_num = 1;
			}

			let lKingOfTheHillId_int_arr = lBattlegroundInfo_obj.kingsOfHill;
			if(lKingOfTheHillId_int_arr !== undefined)
			{
				this.gameField.showKingsOfTheHill(lKingOfTheHillId_int_arr);
			}

			this.gameField.roundResultScreenController.view.alpha = 1;

			if (
				APP.isBattlegroundGamePlayMode &&
				this.gameField.roundResultActive &&
				(
					this.room.state == ROUND_STATE.QUALIFY ||
					this.room.state == ROUND_STATE.WAIT
				)
			) // in battleground in case of reconnect it is necessary to update the timers
			{
				this._requestFullGameInfo();
			}

			if (APP.isBattlegroundGamePlayMode && this.room.state == ROUND_STATE.QUALIFY)
			{
				this.gameField.roundResultScreenController.checkActivateScreenAfterConnectionOpen();

				if (!this.gameField.roundResultScreenController.isActiveOrActivationInProgress)
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.gameField.roundResultScreenController.view.visible, alreadySitInNumber: data.alreadySitInNumber});
					this._requestFullGameInfo();
				}
			}
		}
		else
		{
			this._fIsBattlegroundMode_bl = false;
			this.gameField.roundResultScreenController.view.alpha = 0;
			this.gameField.showKingsOfTheHill([-1]);
		}
		//...BATTLEGROUND


		if (this._fEnemyIdToRemoveFurther_num !== null)
		{
			this._removeEnemyFromRoomInfoById(this._fEnemyIdToRemoveFurther_num);
			this._fEnemyIdToRemoveFurther_num = null;
		}

		if (this.bossModeController.bossSummoned || this.bossModeController.bossSummonedCheckRequired)
		{
			this.bossModeController.bossNumberShots = data.bossNumberShots;
		}
		this.bossModeController.bossSummonedCheckRequired = true;

		this.emit(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this.room);

		this.emit(GameScreen.EVENT_ON_ROUND_ID_UPDATED, {roundId: data.roundId});
		this.emit(GameScreen.EVENT_ON_ROOM_ID_CHANGED, {id: this.room.id});

		this.player.stake = this.room.playerStake;

		this.updatePlayersInfo(data, false);

		this.gameField.startGamePlay();
		this.gameField.changeState(this.room.state);

		if (this.room.state == ROUND_STATE.CLOSED) return;

		if (data.alreadySitInNumber === -1 && !this._roomHasEmptySeats)
		{
			// all seats are taken by other players -> request new room from the server
			this._onNoPlaceToSeat();
			return;
		}

		if ((!this.player || !this.player.sitIn) && !this._fCompensationDialogActive_bln && !this._fBackToLobbyRequired_bl && !this.isForcedSitOutInProgress)
		//sitIn means that we got response to SitIn request (got response == sit in was successful)
		{
			let alreadySitInNumber = data.alreadySitInNumber;
			if (alreadySitInNumber != -1 || this._canJoinRoom())
			{
				if (!APP.isBattlegroundGamePlayMode)
				{
					this.sendSitIn();
				}
				else if (
					this.isBattlegroundMode
					&& APP.isBattlegroundGamePlayMode
					&& (this.gameStateController.info.isPlayerSitIn
						||
						this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
				)
				{
					if (this.room.state == ROUND_STATE.QUALIFY) //if you send "SitIn" on the state QUALIFY there will be an error
					{
						this.gameField.scoreboardController.resetAndHideScoreBoard();
					}
					else
					{
						this.sendSitIn(true);

						if(APP.isCAFMode && !this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
						{
							APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.isRoundResultDisplayInProgress()});
							this.emit(GameScreen.EVENT_ON_PLAY_AGAIN_REQUIRED);
							this._requestFullGameInfo();
						}
					}
				}
				else if (
					(
						this.isBattlegroundMode
						&& APP.isBattlegroundGamePlayMode
						&& APP.isConfirmBuyinDialogExpectedOnLastHand
					)
					||
					(
						this.isBattlegroundMode
						&& APP.isBattlegroundGamePlayMode
						&& !this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
						&& !APP.isConfirmBuyinDialogExpectedOnLastHand
						&& !this.gameField.roundResultActive
						&& !this.gameField.roundResultScreenController.isActiveOrActivationInProgress
					)
				)
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.gameField.roundResultScreenController.view.visible, alreadySitInNumber: data.alreadySitInNumber});
					this.emit(GameScreen.EVENT_ON_PLAY_AGAIN_REQUIRED);
					this._requestFullGameInfo();
				}

				if (
						this.room.seats
						&& (
								alreadySitInNumber != -1 ||
								this.room.state == ROUND_STATE.PLAY
							)
					)
				{
					this.gameField.drawAllPlayers(this.room.seats, alreadySitInNumber, false);
				}
			}
			else if (this.room.state == ROUND_STATE.PLAY)
			{
				if (this.room.seats)
				{
					this.gameField.drawAllPlayers(this.room.seats);
				}
				this.gameField.showWaitScreen();
			}
			else if (this.room.state == ROUND_STATE.QUALIFY)
			{
				if (!this.transitionViewController.info.isFeatureActive)
				{
					this.gameField.showWaitScreen();
				}

				if (this.isBattlegroundMode
					&& APP.isBattlegroundGamePlayMode)
				{
					this.gameField.scoreboardController.resetAndHideScoreBoard();
				}
			}
		}
		else if (this._fBackToLobbyRequired_bl && !this._fCompensationDialogActive_bln)
		{
			this.goBackToLobby();
		}

		this.addEnemies(this.room.roomEnemies, true /*isLasthand*/, data.enemiesModes);

		APP.on(Game.EVENT_ON_AVATAR_UPDATED, this.onAvatarUpdate, this);
		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_ACTIVATED, this.onSecondaryScreenActivated, this);
		APP.on(Game.EVENT_ON_GAME_SECONDARY_SCREEN_DEACTIVATED, this.onSecondaryScreenDeactivated, this);

		this.emit(GameScreen.EVENT_ON_LASTHAND_BULLETS, {bullets: data.allBullets, alreadySitInNumber: data.alreadySitInNumber});

		if (this._isFrbMode && !APP.isBattlegroundGame)
		{
			this.gameField && this.gameField.resetPlayerWin();
			this.gameField && this.gameField.updatePlayerWin(this.room.alreadySitInWin);
		}

		this._fIsExpectedBuyIn_bln = data.alreadySitInAmmoCount == 0;

		this.tryToProceedPostponedSitOut();


		if(this._fGameStateController_gsc.info.isPlayState)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_IN_PROGRESS);
		}

		this.emit(GameScreen.EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED, {roomId: this._roomId});
	}

	_onPlayerInfoUpdated()
	{
		this.player.stake = APP.playerController.info.currentStake;
	}

	_onLobbyPlayerInfoUpdated(aEvent_obj)
	{
		let lData_obj = aEvent_obj.data;

		if (lData_obj[PlayerInfo.KEY_HEALTH_BAR_ENABLED])
		{
			let lValue_bln = lData_obj[PlayerInfo.KEY_HEALTH_BAR_ENABLED].value;
			this._fHealthBarEnabled_bln = lValue_bln;
			this._onHealthBarStateUpdated();
		}
	}

	_needRetryRebuy()
	{
		this._onReBuyAmmoRetryTime();
	}

	get healthBarEnabled()
	{
		return this._fHealthBarEnabled_bln;
	}

	_onHealthBarStateUpdated()
	{
		this.emit(GameScreen.EVENT_ON_HEALTH_BAR_STATE_UPDATED, {value: this._fHealthBarEnabled_bln});
	}

	_canJoinRoom()
	{
		return this._roomHasEmptySeats
				&& this._isRoomSitItAllowedState
				&& !this._fCompensationDialogActive_bln
				&& !this._fBackToLobbyRequired_bl
				&& !this.isForcedSitOutInProgress;
	}

	get _roomHasEmptySeats()
	{
		return this.room.seats.length < this.room.maxSeats;
	}

	get _isRoomSitItAllowedState()
	{
		let state = this.gameStateController.info.gameState;
		return (state === ROUND_STATE.WAIT || state === ROUND_STATE.PLAY);
	}

	onAvatarUpdate(data)
	{
		this.gameField.onAvatartUpdate(data);

		let lAvatarData_obj = {
			border: data.borderStyle,
			hero: data.hero,
			back: data.background
		};
		APP.playerController.info.setPlayerInfo(PlayerInfo.KEY_AVATAR, {value: lAvatarData_obj});
	}

	_showBlur()
	{
		if (APP.isMobile) return;
		this.gameField.showBlur();
	}

	_hideBlur()
	{
		if (APP.isMobile) return;
		this.gameField.checkBlur();
	}

	onSecondaryScreenActivated()
	{
		if (this.gameField)
		{
			this.gameField.deactivateGameScreens();
			this.gameField.lobbySecondaryScreenActive = true;
			this.showBlur();
		}
	}

	onSecondaryScreenDeactivated(aEvent_obj)
	{
		if (this.gameField)
		{
			this.gameField.lobbySecondaryScreenActive = false;
			this.hideBlur();
			this.gameField.checkBlur();
		}
	}

	closeRoom()
	{
		if (this._fIsPaused_bl)
		{
			this.onRoomUnpaused();
			this._fIsPaused_bl = false;
		}

		if (this.gameField)
		{
			this.emit(GameScreen.EVENT_ON_CLOSE_ROOM);
			this.gameField.closeRoom();
			this.clear();
		}

		this._fCompensationDialogActive_bln = false;
		this._fFrbEndedPostponedData_obj = null;
		this._fBonusRoundEndedPostponedData_obj = null;

		this._fWeaponsController_fwsc.i_clearAll();
	}

	onRoomClosed()
	{
		this._onCloseRoomConfirmed();
	}

	_onCloseRoomConfirmed()
	{
		this.clearAllButLobbyInfo();

		let lIsSwitchToLobbyExpected_bl = this._fBackToLobbyRequired_bl && !this._fStartGameURL_str;
		
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ROOM_CLOSED, { switchToLobbyExpected: lIsSwitchToLobbyExpected_bl });

		if (APP.currentWindow.gameFrbController.info.frbEnded)
		{
			this.emit(GameScreen.EVENT_ON_FRB_ENDED_COMPLETED);
		}
		if (this.gameField)
		{
			this.gameField.onCloseRoom();
		}

		if (lIsSwitchToLobbyExpected_bl)
		{
			this._emitBackToLobbyEvent();
		}
		else if (this._fStartGameURL_str)
		{
			this.start(this._fStartGameURL_str);
			this._fStartGameURL_str = null;

			this._destroyScreenShowTimer();
			this._screenShowTimer = new Timer(this._onScreenShowTimerCompleted.bind(this), 10);
		}
	}

	_onScreenShowTimerCompleted()
	{
		this._destroyScreenShowTimer();
		this.gameField.screen.show();
		this.gameField.screen.hideBlur();
	}

	_destroyScreenShowTimer()
	{
		if (this._screenShowTimer)
		{
			this._screenShowTimer.destructor();
			this._screenShowTimer = null;
		}
	}

	_onCloseRoomDenied()
	{
		if (this.player.sitIn)
		{
			if (!APP.webSocketInteractionController.isSitoutRequestInProgress)
			{
				this.sitOut();
			}
		}
		else
		{
			this.sendCloseRoom();
		}
	}

	sitOut()
	{
		this.emit(GameScreen.EVENT_ON_RESET_TARGET);

		this.emit(GameScreen.EVENT_ON_SIT_OUT_REQUIRED);
	}

	updateRoomInfo(aData_obj)
	{
		if (this.gameField && this.gameField.isGameplayStarted())
		{
			this._fResumeInProcess_bl = true;

			if (aData_obj && aData_obj.startGameUrl)
			{
				this._fStartGameURL_str = aData_obj.startGameUrl;

				if (APP.webSocketInteractionController.reconnectInProgress)
				{
					this.player.sitIn = false;
					this.start(this._fStartGameURL_str);
					this._fStartGameURL_str = null;
					return;
				}

				if (this.gameStateController.info.isPlayerSitIn)
				{
					this.sitOut();
				}
				else
				{
					if (this.room)
					{
						this.closeRoom();
					}
					else
					{
						if (this._fIsPaused_bl)
						{
							this.onRoomUnpaused();
							this._fIsPaused_bl = false;
						}

						this.start(this._fStartGameURL_str);
						this._fStartGameURL_str = null;
					}
				}
			}
			else
			{
				if (this._fIsPaused_bl)
				{
					this.onRoomUnpaused();
					this._fIsPaused_bl = false;
				}

				this._requestFullGameInfo();
			}
		}
		else if (aData_obj && aData_obj.startGameUrl)
		{
			this.start(aData_obj.startGameUrl);
		}
	}

	clearPendingRequestSitin()
	{
		this.player.sitInIsSent = false;
	}

	tryToProceedPostponedSitOut(aOptForced_bl)
	{
		if (this._fPostponedSitOutData_obj)
		{
			this.sitOutResponse(this._fPostponedSitOutData_obj, aOptForced_bl);
			this.gameField.onFRBEnded();
		}
	}

	sitOutResponse(data, aOptForced_bl = false)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_SIT_OUT_RESPONSE, data);
		
		if (data.rid != -1 || APP.playerController.info.seatId == data.id || this.room.alreadySitInNumber == data.id)
		{
			this.room.alreadySitInNumber = -1;
			
			if (APP.gameScreen.isBattlegroundMode)
			{
				this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
				this.player.battlegroundBuyInConfirmIsApproved = false;
				
				if (this._fIsSitOutOnNotEnoughPlayersExpected_bl)
				{
					this._fIsSitOutOnNotEnoughPlayersExpected_bl = false;
					this.removePlayer(data.id);
					this.changeWeaponToDefaultImmediatelyAfterSitOut(); //we need to do this BEFORE main player spot to be removed
					this.gameField.removeMasterPlayerSpot();
					this._clearAmmo();
					this.player.sitIn = false;
					this.player.seatId = -1;
					APP.playerController.updateMasterPlayerSeatId(undefined);
					return;
				}
				if (this._fIsCountDownCanceled)
				{
					this._fIsCountDownCanceled = null;
					this.clear();
					this.removePlayer(data.id);
					this.changeWeaponToDefaultImmediatelyAfterSitOut(); //we need to do this BEFORE main player spot to be removed
					this.gameField.removeMasterPlayerSpot();
					this._clearAmmo();
					this.player.sitIn = false;
					this.player.seatId = -1;
					APP.playerController.updateMasterPlayerSeatId(undefined);
					return;
				}
			}

			if ((this._isFRBEnded || this._isBonusRoundEnded) && !this.gameField.roundResultActive && !aOptForced_bl)
			{
				this._fPostponedSitOutData_obj = data;
				return;
			}

			this._fPostponedSitOutData_obj = null;

			this.removePlayer(data.id);
			this.player.sitIn = false;

			this.changeWeaponToDefaultImmediatelyAfterSitOut(); //we need to do this BEFORE main player spot to be removed
			this.gameField.removeMasterPlayerSpot();
			if (!APP.isBattlegroundGamePlayMode) //we don't have RR for SP yet, but we don't need it in BTG now
			{
				this.gameField.roundResultScreenController.resetScreenAppearing();
			}

			if (this._fStartGameURL_str)
			{
				this.closeRoom();
			}

			let lSpecialWeaponsMoneyCompensation_num = data.compensateSpecialWeapons || 0;
			let lSpecialWeaponsMoneyTotalReturn_num = data.totalReturnedSpecialWeapons || 0;

			this._clearAmmo();

			let tournamentModeInfo = this._tournamentModeInfo;
			if (tournamentModeInfo.isTournamentMode && tournamentModeInfo.isTournamentOnServerCompletedState)
			{
				// actions (force sitout dlg, sw compensation dlg, back to lobby) not required, because tournament finished/cancelled message is already on screen
			}
			else if (this._isBonusMode && this.gameBonusController.info.bonusCompletionData != null)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.EVENT_ON_BONUS_STATUS_CHANGED_RESPONSE,
					{
						messageData: this.gameBonusController.info.bonusCompletionData,
						nextRoomId: this.gameBonusController.info.nextRoomId,
						nextModeFRB: data.hasNextFrb
					}
				);
			}
			else if (this._isFRBEnded && this._fGameFrbController_gfrbc.info.bonusCompletionData != null)
			{
				APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_FRB_ENDED_RESPONSE,
					{
						messageData: this._fGameFrbController_gfrbc.info.bonusCompletionData,
						nextRoomId: this._fGameFrbController_gfrbc.info.nextRoomId
					}
				);
			}
			else if	(this._isForceSitOut(data) && (!this.room || (this.room.state != ROUND_STATE.QUALIFY)))
			{
				if(
					(
						!this._isFRBEnded
						||
						(
							!this._isFrbMode
							&& (!this.room || (this.room.state != ROUND_STATE.QUALIFY))
						)
					)
					&&
					!APP.isBattlegroundGamePlayMode
				)
				{
					this.emit(GameScreen.EVENT_ON_FORCE_SIT_OUT_REQUIRED, {roomId: this.room.id});
					this._fForseSitOutInProgress = true;
				}
			}
			else if (lSpecialWeaponsMoneyCompensation_num > 0 || lSpecialWeaponsMoneyTotalReturn_num > 0)
			{
				this._fCompensationDialogActive_bln = true;
				this.emit(GameScreen.EVENT_MID_ROUND_COMPENSATE_SW_REQUIRED, {compensateSpecialWeapons: lSpecialWeaponsMoneyCompensation_num, totalReturnedSpecialWeapons: lSpecialWeaponsMoneyTotalReturn_num, roomId: this.room.id});
			}
			else
			{
				if (!APP.gameScreen.isBattlegroundMode) this.goBackToLobby();
			}

			APP.playerController.updateMasterPlayerSeatId(undefined);
			this.player.seatId = -1;
		}
		else
		{
			this.removePlayer(data.id);

			if (this._canJoinRoom() && this.player.seatId === undefined)
			{
				if (!APP.isBattlegroundGamePlayMode
					|| APP.isBattlegroundGamePlayMode && this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
				{
					this.sendSitIn();
				}
			}
		}
	}

	get isForcedSitOutInProgress()
	{
		return this._fForseSitOutInProgress;
	}

	get _isFRBEnded()
	{
		return this._fFrbEndedPostponedData_obj;
	}

	get _isBonusRoundEnded()
	{
		return this._fBonusRoundEndedPostponedData_obj;
	}

	_isForceSitOut(aData_obj)
	{
		return aData_obj.rid == -1 && APP.playerController.info.seatId == aData_obj.id;
	}

	sitInResponse(data)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_SIT_IN_RESPONSE, data);

		if (this._fGameStateController_gsc.info.isPlayState)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.GAME_IN_PROGRESS);
		}

		if (data.rid == -1)
		{
			var seat = {
				id: data.id,
				seatId: data.id,
				nickname: data.nickname,
				avatarId: data.avatarId,
				avatar: data.avatar,
				enterDate: data.enterDate,
				totalScore: 0,
				currentScore: 0,
				rankPonts: 0,
				weapon:{
					id: data.specialWeaponId
				}
			};
			this.addPlayer(seat);

			if (
					APP.playerController.info.seatId === undefined
					&& !this._roomHasEmptySeats
					&& !APP.webSocketInteractionController.hasDelayedRequests(CLIENT_MESSAGES.SIT_IN)
					&& !APP.webSocketInteractionController.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_IN)
				)
			{
				// all seats are taken by other players -> request new room from the server
				this._onNoPlaceToSeat();
				return;
			}

			this.emit(GameScreen.EVENT_ON_PLAYER_ADDED, {seatId: data.id});

			this.gameField.drawAllPlayers(this.room.seats);
		}
		else
		{
			APP.playerController.updateMasterPlayerSeatId(data.id);

			let lAvatarData_obj = {
				border: data.avatar.borderStyle,
				hero: data.avatar.hero,
				back: data.avatar.background
			};
			APP.playerController.info.setPlayerInfo(PlayerInfo.KEY_AVATAR, {value: lAvatarData_obj});
			APP.playerController.info.setPlayerInfo(PlayerInfo.KEY_NICKNAME, {value: data.nickname});

			this.player.seatId = data.id;
			this.player.nickname = data.nickname;
			this.player.currentScore = 0;
			this.player.totalScore = 0;
			this.player.totalWin = data.frbBalance || 0;
			this.player.avatarId = data.avatarId;
			this.player.avatar = data.avatar;
			this.player.enterDate = data.enterDate;
			this.player.sitIn = true;
			this.player.confirmIsSent = false;
			this.player.battlegroundBuyInConfirmIsApproved = false;
			this.player.sitInIsSent = false;
			if (!this.isPaused)
			{
				this.gameField.addMasterPlayerSpot(this.player);
				this.gameField.drawAllPlayers(this.room.seats, data.id /*master seat*/, true);
			}
			this.gameField.updatePlayerScore(this.player);

			this.gameField.resetWaitBuyIn();
			this.gameField.onBuyAmmoResponse();

			this.gameField.removeWaitScreen();
			this.gameField.validateState();

			this.gameField.isWinLimitExceeded = false;

			if (this.weaponsController.info.ammo == 0)
			{
				if (this.gameStateController.info.gameState == ROUND_STATE.PLAY)
				{
					this.gameField.tryToBuyAmmo();
				}
				else
				{
					this.gameField.tryRoundResultBuyAmmoRequestAllowing(); // to be able to send BuyIn after state change to PLAY
				}
			}
		}
	}

	removePlayer(id)
	{
		if (!this.room || !this.room.seats)
		{
			return;
		}
		let removed = false;
		for (let i = 0; i < this.room.seats.length; i ++)
		{
			if (this.room.seats[i].id == id)
			{
				this.room.seats.splice(i, 1);
				removed = true;
				break;
			}
		}

		if (removed)
		{
			this.emit(GameScreen.EVENT_ON_PLAYER_REMOVED, {seatId: id});
			this.gameField.drawAllPlayers(this.room.seats);
		}
	}

	addPlayer(seat)
	{
		var check = false;
		for (let i = 0; i < this.room.seats.length; i ++)
		{
			if (this.room.seats[i].id == seat.id)
			{
				this.room.seats[i] = seat;
				check = true;
				break;
			}
		}
		if (!check)
		{
			this.room.seats.push(seat);
		}

		this.infoPanelView.update();
	}

	newZombieResponse(data, aNeedShowSound_bl = true)
	{
		if (
			(data.newEnemy.parentEnemyId > -1)
			&& (data.newEnemy.typeId === ENEMY_TYPES.TINY_TOAD)
		)
		{
			this.emit(GameScreen.EVENT_ON_NEW_HV_ENEMY, {data: data.newEnemy});
		}
		else
		{
			this.addEnemy(data.newEnemy, null, aNeedShowSound_bl);
		}
	}

	newZombiesResponse(data)
	{
		if (!data.enemies) return;

		data = this.sortZombiesIfRequired(Utils.clone(data));

		for (let i = 0; i < data.enemies.length; ++i)
		{
			let lNeedShowSound_bl = i < 5;
			this.newZombieResponse({newEnemy: data.enemies[i]}, lNeedShowSound_bl);
		}
	}

	sortZombiesIfRequired(data) // used for scarabs and portal
	{
		if (data.enemies[0] && data.enemies[0].trajectory.points[0].portal && data.enemies[0].swarmType === 1)
		{
			var firstEnemy = Utils.clone(data.enemies[0]);
			data.enemies[0] = Utils.clone(data.enemies[data.enemies.length - 1]);
			data.enemies[data.enemies.length - 1] = firstEnemy;
		}

		return data;
	}

	get restoreAfterUnseasonableRequestInProgress()
	{
		return this._restoreAfterUnseasonableRequest;
	}

	get restoreAfterLagsInProgress()
	{
		return this._restoreAfterLagsInProgress;
	}

	_requestFullGameInfo()
	{
		if (APP.isBattlegroundGamePlayMode
			&& this._fIsFullGameInfoUpdateExpected_bl)
		{
			return;
		}

		if (!APP.pendingOperationController.info.isPendingOperationProgressStatusDefined)
		{
			return;
		}

		if (
			this.gameStateController.info.gameState !== ROUND_STATE.PLAY
			&&
				(
					APP.isDialogActive(DIALOG_ID_FRB) ||
					APP.isDialogActive(DIALOG_ID_BONUS)
				)
		)
		{
			return;
		}

		this._fIsFullGameInfoUpdateExpected_bl = true;
		this.emit(GameScreen.EVENT_ON_FULL_GAME_INFO_REQUIRED);
	}

	fullGameInfoResponse(data)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.FULL_GAME_INFO_UPDATED, data);
		this.emit(GameScreen.EVENT_ON_CURRENT_POWER_UP_MULTIPLIER_UPDATED, {currentPowerUpMultiplier: data.currentPowerUpMultiplier});

		this.room.roundId = data.roundId;
		this.emit(GameScreen.EVENT_ON_ROUND_ID_UPDATED, {roundId: data.roundId});

		let l_gwsic = APP.webSocketInteractionController;
		if (
				this.player.sitIn
				&& !this._fGameStateController_gsc.info.isPlayerSitIn
				&& !l_gwsic.isSitoutRequestInProgress
			)
		{
			// sitout on server occurred but SitOutResponse was not sent to game client https://jira.dgphoenix.com/browse/MQBG-541
			this.player.sitIn = false;
			this.player.seatId = undefined;
			APP.playerController.updateMasterPlayerSeatId(undefined);
		}

		this._fIsFullGameInfoUpdateExpected_bl = false;

		if (this._restoreAfterUnseasonableRequest)
		{
			this._restoreAfterUnseasonableRequest = false;
			this.restoreAfterUnseasonableRequest(data);
			return;
		}

		if (!this._fResumeInProcess_bl && !this._restoreAfterLagsInProgress) return;

		if (this.bossModeController.bossSummoned || this.bossModeController.bossSummonedCheckRequired)
		{
			this.bossModeController.bossNumberShots = data.bossNumberShots;
		}
		this.bossModeController.bossSummonedCheckRequired = true;
		this._fIsPaused_bl = false;
		this._restoreAfterLagsInProgress = false;

		if (data.mapId !== undefined && data.mapId !== this.room.mapId)
		{
			this.room.mapId = data.mapId;
		}
		if (data.state !== undefined && data.state !== this.room.state)
		{
			this.room.state = data.state;
			this.gameField.changeState(data.state);
			this.emit(GameScreen.EVENT_ON_NEW_ROUND_STATE, {state: (data.state == ROUND_STATE.PLAY)}); // (data.state == ROUND_STATE.PLAY) because GameField emits EVENT_ON_NEW_ROUND_STATE event with true/false value meaning is round in progress or not on client side (pending animations etc), and GameStateController expects Boolean in GameScreen.EVENT_ON_NEW_ROUND_STATE
		}

		this.updatePlayersInfo(data, true);

		if (APP.playerController.info.seatId === undefined && !this._roomHasEmptySeats)
		{
			// all seats are taken by other players -> request new room from the server
			this._onNoPlaceToSeat();
			return;
		}

		if (this._canJoinRoom() && this.player.seatId === undefined)
		{
			if (APP.isBattlegroundGamePlayMode)
			{
				if (this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
				{
					if (
							this._fGameStateController_gsc.info.isPlayState
							&& !this._fGameStateController_gsc.info.isPlayerSitIn
						)
					{
						this._fNeedUpdateStartGameUrl_bl = false;
						this._gameUrlNeedToUpdate();
						return;
					}
					else
					{
						this.sendSitIn();
					}
				}
			}
			else
			{
				this.sendSitIn();
			}
		}
		else if (!this.player.sitIn && this.player.seatId === undefined && !this._fForseSitOutInProgress)
		{
			if (!this.transitionViewController.info.isFeatureActive || this.room.state == ROUND_STATE.PLAY)
			{
				this.gameField.showWaitScreen();
			}
		}

		this.room.roomEnemies = data.roomEnemies;
		this.room.freezeTime = data.freezeTime;


		this.emit(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this.room);
		this.gameField.redrawMap();

		if (this.room.state === ROUND_STATE.PLAY)
		{
			this.gameField.addRoomGradient();
		}

		this.addEnemies(data.roomEnemies, true /*isLasthand*/, data.enemiesModes);

		// this.gameField.resetBalanceFlags();
		this.gameField.showRoom(this.room.state);
		
		this.emit(GameScreen.EVENT_ON_LASTHAND_BULLETS, {bullets: data.allBullets});
		
		this.onRoomUnpaused();

		if (data.seats && this._isFrbMode && !this._isFRBEnded && !APP.isBattlegroundGame)
		{
			let roundWin = 0;
			for (let seat of data.seats)
			{
				if (seat.id == this.player.seatId)
				{
					roundWin = seat.roundWin;
				}
			}
			this.gameField.resetPlayerWin();
			this.gameField.updatePlayerWin(roundWin);
		}

		this.emit(GameScreen.EVENT_ON_WEAPON_PAID_MULTIPLIER_UPDATE_REQUIRED, {roomId: this._roomId});
	}

	_destroyRoomUnpauseTimer()
	{
		if (this._roomUnpauseTimer)
		{
			this._roomUnpauseTimer.destructor();
			this._roomUnpauseTimer = null;
		}
	}

	restoreAfterUnseasonableRequest(data)
	{
		this.room.mapId = data.mapId;
		this.room.state = data.state;

		this.updatePlayersInfo(data, false);

		this.gameField.startGamePlay();
		this.gameField.changeState(this.room.state);

		if (this.room.state == ROUND_STATE.CLOSED) return;

		if (this._canJoinRoom() && !this.player.sitIn)
		{
			if (APP.isBattlegroundGamePlayMode)
			{
				if (this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
				{
					if (
							this._fGameStateController_gsc.info.isPlayState
							&& !this._fGameStateController_gsc.info.isPlayerSitIn
						)
					{
						this._fNeedUpdateStartGameUrl_bl = false;
						this._gameUrlNeedToUpdate();
						return;
					}
					else
					{
						this.sendSitIn();
					}
				}
			}
			else
			{
				this.sendSitIn();
			}
		}
		else
		{
			if (this.player.seatId === undefined)
			{
				if (!this.transitionViewController.info.isFeatureActive || this.room.state == ROUND_STATE.PLAY)
				{
					this.gameField.showWaitScreen();
				}
			}
			else
			{
				this.gameField.addMasterPlayerSpot(this.player);
			}
		}

		if (this.room.seats)
		{
			this.gameField.drawAllPlayers(this.room.seats);
		}

		if (
				this.room.state == ROUND_STATE.QUALIFY
				|| this.room.state == ROUND_STATE.WAIT
			)
		{
			this.gameField.blurUI();
		}

		this.room.roomEnemies = data.roomEnemies;
		this.addEnemies(data.roomEnemies, false, data.enemiesModes);

		this.emit(GameScreen.EVENT_ON_LASTHAND_BULLETS, {bullets: data.allBullets});

		this.gameField.resetBalanceFlags();

		this.emit(GameScreen.EVENT_ON_GAME_RESTORED_AFTER_UNSEASONABLE_REQUEST);
	}

	onRoomPaused()
	{
		if (this._fIsPaused_bl)
		{
			return;
		}

		if (this.gameField && this.gameField.isGameplayStarted())
		{
			this.emit(GameScreen.EVENT_ON_ROOM_PAUSED);
			this._fIsPaused_bl = true;
			this._fResumeInProcess_bl = false;
			this._restoreAfterLagsInProgress = false;

			//this.addGameFieldCapture();

			this.gameField.hideRoom();
			this.gameField.screen.hide();
			this.clear();
			
			this.subloadingController.i_showLoadingScreen();

			this._fMasterSWAwardsExpected_obj_arr = [];

			this._forceDeactivatedRoomDelayedErrorIfRequired();
		}
	}

	onRoomUnpaused()
	{
		if (!this._fIsPaused_bl)
		{
			return;
		}

		APP.forceRendering();

		this._destroyRoomUnpauseTimer();

		this.emit(GameScreen.EVENT_ON_ROOM_UNPAUSED);

		if (this.gameField && this.gameField.isGameplayStarted())
		{
			this.gameField.screen.show();
		}

		//this.removeGameFieldCapture();
	}

	_onRoomInfoUpdated()
	{
		this.subloadingController.i_hideLoadingScreen();
	}

	/*addGameFieldCapture()
	{
		this.removeGameFieldCapture();

		if (this.gameField)
		{
			var l_rt = this.gameField.getBounds();
			var lOrigX_num = this.gameField.position.x;
			var lOrigY_num = this.gameField.position.y;
			this.gameField.position.set(l_rt.width/2,l_rt.height/2);
			var l_txtr = PIXI.RenderTexture.create(l_rt.width, l_rt.height, PIXI.SCALE_MODES.NEAREST, 2);
			APP.stage.renderer.render(this.gameField, l_txtr);
			var l_sprt = new PIXI.Sprite(l_txtr);
			l_sprt.anchor.set(0.5);
			this.gameField.position.set(lOrigX_num, lOrigY_num);
			this.addChild(this._fGameFieldCapture_sprt = l_sprt);
		}
	}

	removeGameFieldCapture()
	{
		if (this._fGameFieldCapture_sprt)
		{
			this.contains(this._fGameFieldCapture_sprt) && this.removeChild(this._fGameFieldCapture_sprt);

			this._fGameFieldCapture_sprt.destroy(true);
			this._fGameFieldCapture_sprt = null;
		}
	}*/

	_onBattlegroundBuyInConfirmRequestApproved()
	{
		this.player.battlegroundBuyInConfirmIsApproved = true;
		this.sendSitIn();
	}

	sendSitIn(aBattlegroundPlayAgainIgnore_bl = false)
	{
		let l_poi = APP.pendingOperationController.info;
		if (
				this.player.sitInIsSent
				|| l_poi.isPendingOperationStatusCheckInProgress
			)
		{
			return;
		}

		//SEND CONFIRM BEFORE SIT IN IN BATTLEGROUND MODE...
		if (
			APP.gameScreen.isBattlegroundMode &&
			!this.player.battlegroundBuyInConfirmIsApproved
		)
		{
			if (!this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
			{
				if (!this.gameField.roundResultScreenController.info.isScreenActive && !aBattlegroundPlayAgainIgnore_bl)
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.gameField.roundResultScreenController.view.visible});
				}
			}
			else if(!this.player.confirmIsSent)
			{
				this.player.confirmIsSent = true;
				this.emit(GameScreen.EVENT_BATTLEGROUND_CONFIRM_BUY_IN_REQUIRED, {});
				return;
			}
		}
		//...SEND CONFIRM BEFORE SIT IN IN BATTLEGROUND MODE

		this.player.sitInIsSent = true;
		this.emit(GameScreen.EVENT_ON_SIT_IN_REQUIRED, {stake: this.player.stake});

		let lIsBattlegroundCountDownRequired_bl = (
			APP.isBattlegroundGamePlayMode &&
			!this.isRoundResultDisplayInProgress() &&
			this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound);

		this.emit(GameScreen.EVENT_ON_SIT_IN_COUNT_DOWN_REQUIRED, {isBattlegroundCountDownRequired: lIsBattlegroundCountDownRequired_bl});
		//APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.ON_SIT_IN_REQUIRED, {isBattlegroundCountDownRequired: lIsBattlegroundCountDownRequired_bl});
	}

	addEnemies(data, isLasthand, enemiesModes)
	{
		if (!data || !data.length) return;
		if (this.isPaused) return;

		for (var i=0; i<data.length; i++)
		{
			Object.assign(data[i], {isLasthand: isLasthand});
			let mode = null;
			if (enemiesModes)
			{
				if (enemiesModes[data[i].id])
				{
					mode = enemiesModes[data[i].id];
				}
			}
			this.addEnemy(data[i], null, false, true, null, undefined, mode);
		}
	}

	getEnemyIx(id)
	{
		for (var i = 0; i < this.enemies.length; i++)
		{
			if (this.enemies[i].id == id)
			{
				return i;
			}
		}

		return -1;
	}

	_onEnemyAddTrajectoryPoint(e)
	{
		let enemy = this.getExistEnemy(e.enemyId);
		let timeOffset = e.timeOffset ? e.timeOffset : 0;
		if (enemy)
		{
			let lEnemyTimeOffset_num = enemy.timeOffset || 0;
			let newPoint = {x: e.x, y: e.y, time: serverTime + clientTimeDiff - lEnemyTimeOffset_num + timeOffset};

			this._pushInRightPosition(enemy, newPoint);
		}
	}

	_pushInRightPosition(enemy, point)
	{
		let lPoints_arr = enemy.trajectory.points;
		for (let i = 1; i < lPoints_arr.length; ++i)
		{
			if (lPoints_arr[i].time > point.time)
			{
				if ( Utils.isEqualTrajectoryPoints(lPoints_arr[i], lPoints_arr[i-1]) ) // the enemy is probably frozen, we can update the points anyway
				{
					enemy.trajectory.points.splice(i-1, 2, point); //delete two points with approximately the same coords and put the new one
				}
				else
				{
					enemy.trajectory.points.splice(i, 0, point);
				}
				return;
			}
		}

		enemy.trajectory.points.push(point);
	}

	_onEnemyPauseWalking(e)
	{
		let enemy = this.getExistEnemy(e.enemyId)
		if (enemy)
		{
			enemy.allowUpdatePosition = false;
			enemy.timeOffset = e.timeOffset || 0;
		}
	}

	_onEnemyResumeWalking(e)
	{
		let enemy = this.getExistEnemy(e.enemyId)
		if (enemy)
		{
			if (e.timeOffset > 0)
			{
				this._onSetEnemyTimeOffset(e);
			}
			enemy.allowUpdatePosition = true;
		}
	}

	_onEnemyFreeze(e)
	{
		let enemy = this.getExistEnemy(e.enemyId)
		if (enemy)
		{
			enemy.frozen = true;

			let enemyInfoSequenses = Sequence.findByTarget(enemy);
			if (enemyInfoSequenses && enemyInfoSequenses.length)
			{
				for (let i=0; i<enemyInfoSequenses.length; i++)
				{
					let enemyInfoSequense = enemyInfoSequenses[i];
					if (!enemyInfoSequense.paused)
					{
						enemyInfoSequense.pause();
					}
				}
			}
		}
	}

	_onEnemyUnfreeze(e)
	{
		let enemy = this.getExistEnemy(e.enemyId)
		if (enemy)
		{

			enemy.frozen = false;

			let enemyInfoSequenses = Sequence.findByTarget(enemy);
			if (enemyInfoSequenses && enemyInfoSequenses.length)
			{
				for (let i=0; i<enemyInfoSequenses.length; i++)
				{
					let enemyInfoSequense = enemyInfoSequenses[i];
					if (enemyInfoSequense.paused)
					{
						enemyInfoSequense.resume();
					}
				}
			}
		}
	}

	addEnemy(enemy, testName, needShowSound = false, aAllowUpdatePosition_bl = true, aStartPosition_pt = null, angle = undefined, mode = 0)
	{
		if (APP.currentWindow.gameFrbController.info.frbEnded && !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			//don't add new enemies, because the room is cleared already
			console.log("[Y] Omit adding new enemies, because the room is cleared already!");
			return;
		}

		if(APP.isCAFMode && !APP.currentWindow.gameStateController.info.isPlayerSitIn)
		{
			return;
		}

		this._correctTrajectoryPointsIfRequired(enemy, needShowSound);

		if (this.checkExistEnemy(enemy.id))
		{
			let exenemy = this.getExistEnemy(enemy.id);
			exenemy.prevX = enemy.x;
			exenemy.prevY = enemy.y;
			this._updateEnemyTrajectory(enemy.id, enemy.trajectory); // in case previous enemy has not been destroyed yet by some reason, we need to update a trajectory for it
			return;
		}

		enemy.mode = mode;

		if (aStartPosition_pt)
		{
			enemy.x = aStartPosition_pt.x;
			enemy.y = aStartPosition_pt.y;
		}
		else
		{
			let firstPoint = enemy.trajectory && enemy.trajectory.points ? enemy.trajectory.points[0] : null;
			if (firstPoint)
			{
				aStartPosition_pt = {x:firstPoint.x, y:firstPoint.y};
			}
		}

		enemy.prevX = enemy.x;
		enemy.prevY = enemy.y;
		enemy.startPosition = aStartPosition_pt;

		enemy.angle = angle || 0;

		//[Y]DEBUG...
		// let startTime = enemy.trajectory.points[0].time;
		// let curTime = this.currentTime;
		// enemy.trajectory.points.sort((a, b) => {
		// 	return (a.time - b.time)
		// });
		// for (let point of enemy.trajectory.points)
		// {
		// 	point.time = point.time - startTime + curTime;
		// }
		//...[Y]DEBUG

		let name = GameScreen.calculateEnemyName(~~enemy.typeId, enemy.skin);

		enemy.name = name;
		enemy.needShowSound = needShowSound;

		enemy.allowUpdatePosition = aAllowUpdatePosition_bl;
		this.enemies.push(enemy);
		return enemy;
	}

	_correctTrajectoryPointsIfRequired(enemyInfo, freezeTime = 0)
	{

		//[Y]DEBUG...
		// enemyInfo.trajectory.points.sort((a, b) => {
		// 	return (a.time - b.time);
		// });
		// let startTime = enemyInfo.trajectory.points[0].time;
		// let curTime = this.currentTime;
		// for (let point of enemyInfo.trajectory.points)
		// {
		// 	point.time = point.time - startTime + this.currentTime;
		// }
		//...[Y]DEBUG

		if (enemyInfo.typeId === ENEMY_TYPES.JUMPER
			|| enemyInfo.typeId === ENEMY_TYPES.WITCH
			|| enemyInfo.typeId === ENEMY_TYPES.TINY_TOAD
			|| enemyInfo.typeId === ENEMY_TYPES.EXPLODER
			|| enemyInfo.typeId === ENEMY_TYPES.POISON_PLANT_GREEN_PINK
			|| enemyInfo.typeId === ENEMY_TYPES.POISON_PLANT_YELLOW_PURPLE
			|| enemyInfo.typeId === ENEMY_TYPES.CARNIVORE_PLANT_RED
			|| enemyInfo.typeId === ENEMY_TYPES.CARNIVORE_PLANT_PURPLE
		)
		{
			return;
		}

		if (isNaN(freezeTime) || freezeTime == 0)
		{
			this._resetTimeOffsetForEnemy(enemyInfo.id);
		}

		let enemy = this.gameField.getExistEnemy(enemyInfo.id);

		let lIsBoss_lb = enemy && (enemy.typeId == ENEMY_TYPES.BOSS);
		if (lIsBoss_lb) return; // nothing to correct for Boss

		if (enemy)
		{
			let currentPoint = enemy.getGlobalPosition();
			let currentTime = this.currentTime;
			let enemyTimeOffset = enemyInfo.timeOffset || 0;
			let pointTime = currentTime - enemyTimeOffset + freezeTime;
			let originalPoint = enemyInfo.trajectory.points[0];
			enemyInfo.trajectory.points[0] = ({
				x: currentPoint.x, y: currentPoint.y, time: pointTime,
				teleport: originalPoint.teleport,
				invulnerable: originalPoint.invulnerable,
				originalPoint: originalPoint
			});

			let secondOrigPoint = enemyInfo.trajectory.points[1];
			if (Utils.isEqualPoints(originalPoint, secondOrigPoint))
			{
				let secondNewPoint = enemyInfo.trajectory.points[1] = Object.assign({}, secondOrigPoint);
				secondNewPoint.x = currentPoint.x;
				secondNewPoint.y = currentPoint.y;
				secondNewPoint.time = secondNewPoint.time - enemyTimeOffset;
			}
		}

		//debug trajectory draw...
		// enemyInfo.gr && enemyInfo.gr.destroy();
		// let lTrajectoryPoints = enemyInfo.trajectory.points;
		// let gr = this.addChild(new PIXI.Graphics());
		// gr.x = -960/2;
		// gr.y = -540/2;
		// gr.lineStyle(2, 0x00ff00, 1);
		// for (let i=1; i<lTrajectoryPoints.length; i++)
		// {
		// 	let lppt = lTrajectoryPoints[i-1];
		// 	let lpt = lTrajectoryPoints[i];
		// 	gr.moveTo(lppt.x, lppt.y);
		// 	gr.lineTo(lpt.x, lpt.y);
		// }
		// enemyInfo.gr = gr;
		//...debug trajectory draw

	}

	_resetTimeOffsetForEnemy(enemyId)
	{
		let enemyInfo = this.getExistEnemy(enemyId);
		if (enemyInfo)
		{
			if (enemyInfo.timeOffsetSequence)
			{
				Sequence.destroy([enemyInfo.timeOffsetSequence]);
				enemyInfo.allowUpdatePosition = true;
				enemyInfo.timeOffsetSequence = null;
			}
			enemyInfo.timeOffset = 0;
		}
	}

	onChangeMap(data)
	{
		this.room.mapId = data.mapId;
		this.room.freezeTime = null;
		this.emit(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this.room);
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

	getEnemies()
	{
		return this.enemies;
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

	getBullets()
	{
		return this.gameField ? this.gameField.bullets : null;
	}

	changeWeaponToDefaultImmediatelyAfterSitOut()
	{
		this.gameField.changeWeaponToDefaultImmediatelyAfterSitOut();
	}

	_onBuyAmmoRequired()
	{
		if (this._fForseSitOutInProgress)
		{
			return;
		}

		if (!this.player.sitIn)
		{
			this.sendSitIn();
		}
		else
		{
			this.sendBuyIn();
		}
	}

	sendBuyIn()
	{
		if(this.isBattlegroundMode)
		{
			return;
		}

		if (
				!this.gameField.isAmmoBuyingInProgress
				&& !APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress
				&& !this.buyAmmoRetryingController.info.isRetryDialogActive
			)
		{
			this.emit(GameScreen.EVENT_ON_BUY_IN_REQUIRED);
			this.gameField.waitBuyIn = true;
		}
	}

	onBuyInResponse(data)
	{
		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BUY_IN_RESPONSE_RECIEVED);

		this._updateAmmo(data.ammoAmount);

		this._fIsExpectedBuyIn_bln = false;

		this.gameField.resetWaitBuyIn();
		this.gameField.onBuyAmmoResponse();
	}

	sendReBuy()
	{
		if (APP.gameScreen.isBattlegroundMode
			&& this.gameStateController.info.gameState == ROUND_STATE.QUALIFY)
		{
			if (!this._fBattlegroundIsNeedReBuyOnGameState_bl)
			{
				this._fBattlegroundIsNeedReBuyOnGameState_bl = true;
				this.gameStateController.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChangedForBattlegroundReBuy, this);
			}
			return;
		}

		let lWeaponsInfo_wsi = this.weaponsController.i_getInfo();
		let lPlayerInfo_pi = APP.playerController.info;
		let lUnpresentedWin_num = lPlayerInfo_pi.unpresentedWin;
		let lShotCost_num = lPlayerInfo_pi.currentStake;
		let lRealAmmoCost_num = lWeaponsInfo_wsi.realAmmo * lShotCost_num;

		if (
				!this.gameField.isAmmoBuyingInProgress
				&& !APP.pendingOperationController.info.isPendingOperationStatusCheckInProgress
				&& !this.buyAmmoRetryingController.info.isRetryDialogActive
				&& lWeaponsInfo_wsi.ammo == 0 // condition added due to https://jira.dgphoenix.com/browse/MQCMN-222
				&& (lUnpresentedWin_num+lRealAmmoCost_num) <= lShotCost_num
			)
		{
			this.emit(GameScreen.EVENT_ON_RE_BUY_REQUIRED);
		}
	}

	_onGameStateChangedForBattlegroundReBuy(e)
	{
		if (this._fBattlegroundIsNeedReBuyOnGameState_bl && e.value == ROUND_STATE.WAIT)
		{
			this._fBattlegroundIsNeedReBuyOnGameState_bl = false;

			if (this.player.sitIn)
			{
				this.sendReBuy();
			}
			else
			{
				this._gameUrlNeedToUpdate();
				this.gameField.scoreboardController.resetAndHideScoreBoard();
			}

			this.gameStateController && this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChangedForBattlegroundReBuy, this);
			return;
		}

		if (e.value == ROUND_STATE.PLAY || e.value == ROUND_STATE.CLOSED)
		{
			this._fBattlegroundIsNeedReBuyOnGameState_bl = false;
			this.gameStateController && this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChangedForBattlegroundReBuy, this);
		}
	}

	_resetWaitingBattlegroundReBuyOnGameState()
	{
		if (APP.isBattlegroundGamePlayMode && this._fBattlegroundIsNeedReBuyOnGameState_bl) //if we opened a new battleground room, then no need to send a rebuy
		{
			this._fBattlegroundIsNeedReBuyOnGameState_bl = false;
			this.gameStateController && this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChangedForBattlegroundReBuy, this);
		}
	}

	onReBuyResponse(data)
	{
		if(APP.isCAFMode) this._requestFullGameInfo();

		APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.RE_BUY_RESPONSE_RECIEVED);

		this._updateAmmo(data.ammoAmount);

		this.gameField.onReBuyAmmoResponse();
	}

	_onTimeToConvertBalanceToAmmo(event)
	{
		this._updateAmmo(event.ammoAmount);
	}

	_updateAmmo(ammoAmount)
	{
		let lDefaultAmmo_num = this.weaponsController.i_getInfo().realAmmo;
		let lNewDefaultAmmo_int = lDefaultAmmo_num + ammoAmount;
		this.emit(GameScreen.EVENT_ON_WEAPON_SHOTS_UPDATED, {weaponId: WEAPONS.DEFAULT, shots: lNewDefaultAmmo_int, updateAfterBuyIn: true});
		this.gameField && this.gameField.redrawAmmoText();
	}

	_transferWinToAmmo(winAmount)
	{
		if (this._isFrbMode) return;

		let lPlayerInfo_pi = APP.playerController.info;
		if (lPlayerInfo_pi.qualifyWin >= winAmount)
		{
			if (this.player.sitIn && !APP.isBattlegroundGame)
			{
				let lDefaultAmmo_num = this.weaponsController.i_getInfo().realAmmo;
				let lAddDefaultAmmo_num = winAmount / lPlayerInfo_pi.currentStake;
				let lNewDefaultAmmo_num = lDefaultAmmo_num + lAddDefaultAmmo_num;

				this.emit(GameScreen.EVENT_ON_WEAPON_SHOTS_UPDATED, {weaponId: WEAPONS.DEFAULT, shots: lNewDefaultAmmo_num});
				this.gameField.redrawAmmoText();
			}

			this.emit(GameScreen.EVENT_ON_WIN_TO_AMMO_TRANSFERED, {winAmount:winAmount});
		}
	}

	_clearAmmo()
	{
		if (this._isFrbMode) return;

		this.emit(GameScreen.EVENT_ON_WEAPON_SHOTS_UPDATED, {weaponId: WEAPONS.DEFAULT, shots: 0});
	}

	_setEnemyEnergy(aEnemy_obj)
	{
		let enemy = this.gameField.getExistEnemy(aEnemy_obj.id);
		if (enemy)
		{
			enemy.energy = aEnemy_obj.energy;
		}
	}

	_damageEnemyEnergy(aEnemyId_num, aEnemyDamage_num)
	{
		for (let enemy of this.enemies)
		{
			if (enemy.id == aEnemyId_num && !isNaN(enemy.energy))
			{
				enemy.energy = Math.max(0, enemy.energy-aEnemyDamage_num);
				break;
			}
		}
	}

	destroyEnemy(data)
	{
		for (let enemy of this.enemies)
		{
			if (enemy.id == data.enemyId)
			{
				enemy.life = 0;//death flag

				if (enemy.typeId == ENEMY_TYPES.BOSS)
				{
					// Handle boss death
				}

				this.gameField.setEnemyDestroy(enemy.id, data.reason, data.payout);
				break;
			}
		}

		this._removeEnemyFromRoomInfoById(data.enemyId);

		// look through hv enemies awaiting for to be created
		this.emit(GameScreen.EVENT_DESTROY_ENEMY, {enemyId: data.enemyId});
	}

	_removeEnemyFromRoomInfoById(aEnemyId_num)
	{
		if (this.room && this.room.roomEnemies)
		{
			let lEnemies_arr = this.room.roomEnemies;

			for (let enemy of lEnemies_arr)
			{
				if (enemy.id === aEnemyId_num)
				{
					let lEnemyIndex_num = lEnemies_arr.indexOf(enemy);
					if (lEnemyIndex_num >= 0)
					{
						lEnemies_arr.splice(lEnemyIndex_num, 1);
					}
				}
			}
		}
	}

	updateEnemyTrajectory(enemy, dx, dy)
	{
		for (let point of enemy.trajectory.points)
		{
			point.x += dx;
			point.y += dy;
		}
	}

	getEnemyPositionInTheFuture(aEnemyId_int, aFutureTimeOffset_num = 0)
	{
		let enemy = this.getExistEnemy(aEnemyId_int);
		if (enemy)
		{
			//https://jira.dgphoenix.com/projects/MQ/issues/MQ-1218...
			let lIgnoreEnemyTimeOffset_bl = false;
			if(enemy.typeId == ENEMY_TYPES.BOSS)
			{
				lIgnoreEnemyTimeOffset_bl = true;
			}
			//...MQ-1218

			return this.getEnemyPosition(enemy, aFutureTimeOffset_num, lIgnoreEnemyTimeOffset_bl);
		}
		return null;
	}

	_getFirstNonEqualTrajectoryPoint(baseTrajectoryPoint, trajectoryPoints)
	{
		if (!trajectoryPoints || !baseTrajectoryPoint)
		{
			return null;
		}

		let firstNonEqualPoint = null;

		trajectoryPoints = trajectoryPoints.slice();
		trajectoryPoints.sort(function (pointA, pointB)
								{
									return (pointA.time - pointB.time);
								}
		);

		for (let i=0; i<trajectoryPoints.length; i++)
		{
			let curPoint = trajectoryPoints[i];
			if (curPoint.time > baseTrajectoryPoint.time)
			{
				if (curPoint.x != baseTrajectoryPoint.x || curPoint.y != baseTrajectoryPoint.y)
				{
					firstNonEqualPoint = curPoint;
					break;
				}
			}
		}

		return firstNonEqualPoint;
	}

	getEnemyPosition(enemy, aFutureTimeOffset_int = 0, aIgnoreEnemyTimeOffset_bl = false)
	{
		let pMin = Number.MAX_VALUE;
		let nMin = Number.MAX_VALUE;

		let pp = null;
		let np = null;
		let nnp = null;

		let lEnemyTimeOffset_num = aIgnoreEnemyTimeOffset_bl ? 0 : (enemy.timeOffset || 0);
		let time = serverTime + clientTimeDiff - lEnemyTimeOffset_num + aFutureTimeOffset_int;
		let trajectoryPoints = enemy.trajectory.points;
		if (trajectoryPoints.length == 0) return;
		let lastTrajectoryPointTime = trajectoryPoints[trajectoryPoints.length-1].time;
		let counter = 0;
		for (let point of trajectoryPoints)
		{
			if (point.time < time)
			{
				let d = Math.abs(time - point.time);
				if (d < pMin)
				{
					pMin = d;
					pp = point;
				}
				else if (pp && pp.time === point.time && !!point.teleport && !!point.invulnerable)
				{
					pp.invulnerable = !!point.invulnerable;
				}
			}
			if (point.time >= time)
			{
				let d = Math.abs(time - point.time);
				if (d < nMin)
				{
					nMin = d;
					np = point;
					if (counter < trajectoryPoints.length - 1)
					{
						nnp = trajectoryPoints[counter + 1];
					}
				}
			}

			if (point.time > lastTrajectoryPointTime)
			{
				lastTrajectoryPointTime = point.time;
			}
			counter++;
		}

		let lastPointTimeInterval = lastTrajectoryPointTime - time;
		if (lastPointTimeInterval < 0)
		{
			lastPointTimeInterval = 0;
		}

		let isFirstStep = enemy.trajectory.points[0] === pp
								&& enemy.trajectory.points.length >=3
								&& (
										Utils.isEqualPoints(enemy.trajectory.points[0], enemy.trajectory.points[1], enemy.trajectory.points[2]) && !enemy.trajectory.points[2].teleport
										|| Utils.isEqualPoints(enemy.trajectory.points[0], enemy.trajectory.points[1]) && enemy.trajectory.points[0].invulnerable
										|| enemy.trajectory.points[0].portal
									)
								&& !enemy.frozen;

		if (testMode) //set enemy position in the middle of the scene
		{
			var  angles = [0, Math.PI, Math.PI*1.2, Math.PI*1.8];
			return {
				x: 480,
				y: 270,
				angle: (testAngleId == -1) ? angles[3] : angles[testAngleId],
				isEnded: false,
				isFirstStep: isFirstStep,
				lastPointTimeInterval: lastPointTimeInterval
			};
		}

		if (pp && np)
		{
			// Optimized code. Excluded sin and cos expensive operations.
			// Also calculate angle not per every frame, but once between two points. That means, we dont need to calculate atan2 at every frame.
			// Tests with performance.now() showed a performance boost for this function in about 10 times.
			// May be appropriate tested and included in the future, if performance optimizations would be needed.

			let xLen = np.x - pp.x;
			let yLen = np.y - pp.y;

			let len = Math.sqrt(xLen*xLen + yLen*yLen);

			let xOffset = (len == 0 ? 1 : xLen/len); //replacement for cos operation
			let yOffset = (len == 0 ? 0 : yLen/len); //replacement for sin operation
			let angle = 0;

			if (np.angle !== undefined)
			{
				// if angle was already calculated, use it
				angle = np.angle;
			}
			else if (Utils.isEqualPoints(pp, np) && nnp && this.cryogunsController.i_isEnemyFrozen(enemy.id))
			{
				// calculation for frozen enemies in lasthand state
				let nxLen = nnp.x - np.x;
				let nyLen = nnp.y - np.y;
				let nLen = Math.sqrt(nxLen*nxLen + nyLen*nyLen);
				angle = Math.atan2(nnp.y - np.y, nnp.x - np.x);
				xOffset = (nLen == 0 ? 1 : nxLen/nLen);
				yOffset = (nLen == 0 ? 0 : nyLen/nLen);
			}
			else
			{
				angle = Math.atan2(yLen, xLen);
			}

			np.angle = angle; //remember angle for the further steps

			let d = np.time - pp.time;
			let c = time - pp.time;
			len = len * (c/d);

			let x = pp.x + xOffset * len;
			let y = pp.y + yOffset * len;

			return {x: x, y: y, prevTurnPoint: pp, nextTurnPoint: np, angle: angle, isEnded: false, isFirstStep: isFirstStep, lastPointTimeInterval: lastPointTimeInterval};

		}
		else if (pp)
		{
			return {
				x: pp.x,
				y: pp.y,
				prevTurnPoint: pp,
				nextTurnPoint: np,
				angle: 0,
				isEnded: true,
				isFirstStep: isFirstStep,
				lastPointTimeInterval: lastPointTimeInterval
			};
		}
		else if (np)
		{
			return {
				x: np.x,
				y: np.y,
				prevTurnPoint: undefined,
				nextTurnPoint: np,
				angle: undefined,
				isHidden: true,
				isFirstStep: isFirstStep,
				lastPointTimeInterval: lastPointTimeInterval
			};
		}

		return null;
	}

	static normalizeAngle(angle)
	{
		let limit = Math.PI * 2;
		while (angle < 0) angle += limit;
		while (angle > limit) angle -= limit;
		return angle;
	}

	updateEnemies()
	{
		for (let enemy of this.enemies)
		{
			if (!enemy.allowUpdatePosition || enemy.frozen)
			{
				continue;
			}

			enemy.invulnerable = false;

			let p = this.getEnemyPosition(enemy);
			if (p)
			{
				enemy.x = p.x;
				enemy.y = p.y;
				enemy.angle = p.angle !== undefined ? GameScreen.normalizeAngle(p.angle) : undefined;
				enemy.isEnded = !!p.isEnded;
				enemy.isHidden = !!p.isHidden;
				enemy.isFirstStep = p.isFirstStep;
				enemy.prevTurnPoint = p.prevTurnPoint;
				enemy.nextTurnPoint = p.nextTurnPoint;
				enemy.lastPointTimeInterval = p.lastPointTimeInterval;
				enemy.invulnerable = enemy.prevTurnPoint ? !!enemy.prevTurnPoint.invulnerable : false;

				if (this.bossModeController.isIdleAnimating && enemy.typeId !== ENEMY_TYPES.BOSS)
				{
					enemy.invulnerable = true;
				}
			}
			else
			{
				//Just remove enemy
				this.gameField.removeEnemy(enemy);
				this.removeEnemy(enemy);
			}
		}
	}

	updateTimeToNextState(delta)
	{
		if (!this.room) return;

		if (this.room.ttnx > 0)
		{
			this.room.ttnx -= delta;
			if (this.room.ttnx < 0) this.room.ttnx = -1;
		}
	}

	drawEnemies()
	{
		this.gameField.drawEnemies(this.enemies.slice());
	}

	removeEnemy(enemy)
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			if (enemy.id == this.enemies[i].id)
			{
				this.gameField.hideEnemyEffectsBeforeDeathIfRequired(enemy);

				this.enemies.splice(i, 1);
				i--;
			}
		}

		//debug trajectory draw...
		// enemy.gr && enemy.gr.destroy();
		// enemy.gr = null;
		//...debug trajectory draw
	}

	removeOutsideEnemies()
	{
		for (var i = 0; i < this.enemies.length; i ++)
		{
			var enemy = this.enemies[i];
			if (enemy.isEnded && enemy.typeId !== ENEMY_TYPES.BOSS)
			{
				this.enemies.splice(i, 1);
				this.gameField.removeEnemy(enemy);
				i--;
			}
		}
		this.gameField.removeOutsideEnemies();
	}

	tickGamefield(delta, realDelta)
	{
		if (this.gameField)
		{
			this.gameField.tick(delta, realDelta);
		}
	}

	tick(delta, realDelta)
	{
		//STUBS MODE SYNC...
		let lWebSocketController_wsc = APP.webSocketInteractionController;

		if(
			lWebSocketController_wsc.isStubsMode &&
			lWebSocketController_wsc.isStubsMode()
			)
		{
			delta = 17;
			if(lWebSocketController_wsc._fNextStubIndex_int < 5) delta = 30;
			realDelta = delta;

			lastTickOccurTime = serverTime;
			serverTime+= delta
			accurateServerTime = serverTime;

			let lNextStubTime_num = APP.webSocketInteractionController.getNextSubTime();

			if(serverTime > lNextStubTime_num)
			{
				let lOffSet_num = serverTime - lNextStubTime_num;

				delta -= lOffSet_num;

				serverTime = lNextStubTime_num;
				lastTickOccurTime = serverTime;
				accurateServerTime = serverTime;
				realDelta = delta;
			}

			if(delta >= 0)
			{
				this.updateEnemies(delta);
				this.drawEnemies();
				this.removeOutsideEnemies();
				this.tickGamefield(delta, realDelta);
			}

			this.emit(GameScreen.EVENT_ON_TICK_OCCURRED, {delta: delta, realDelta: realDelta});
			return;
		}
		//...STUBS MODE SYNC



		let lCurTime_int = Date.now();

		let clientTimeDiffDelta = lastTickOccurTime !== undefined ? (lCurTime_int - lastTickOccurTime) : ~~realDelta;
		let serverTimeRecievedDelta;
		if (serverLastMessageRecieveTime !== undefined)
		{
			serverTimeRecievedDelta = lCurTime_int-serverLastMessageRecieveTime;
			clientTimeDiffDelta = serverTimeRecievedDelta;
		}
		serverLastMessageRecieveTime = undefined;
		clientTimeDiff += clientTimeDiffDelta;


		// accurate time...
		let accurateClientTimeDiffDelta = lastTickOccurTime !== undefined ? (lCurTime_int - lastTickOccurTime) : ~~realDelta;

		let accurateServerTimeRecievedDelta;
		if (accurateServerLastMessageRecieveTime !== undefined)
		{
			accurateServerTimeRecievedDelta = lCurTime_int-accurateServerLastMessageRecieveTime;
			accurateClientTimeDiffDelta = accurateServerTimeRecievedDelta;
		}
		accurateServerLastMessageRecieveTime = undefined;
		accurateClientTimeDiff += accurateClientTimeDiffDelta;
		// ...accurate time

		lastTickOccurTime = lCurTime_int;

		this.updateTimeToNextState(delta);

		let tournamentModeInfo = this._tournamentModeInfo;
		if (realDelta !== undefined && this._ready && realDelta > MAX_TICK_DELAY)
		{
			if (
					this.gameField
					&& this.gameField.isGameplayStarted()
					&& this.gameField.screen.visible
					&& !tournamentModeInfo.isTournamentOnServerCompletedState
				)
			{
				if (this.gameField)
				{
					this.gameField.clearRoom(false, false, true);
					this.gameField.deactivateGameScreens();
				}
				this.clear();

				this._restoreAfterLagsInProgress = true;

				this.emit(GameScreen.EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED);

				this._requestFullGameInfo();

				this._forceDeactivatedRoomDelayedErrorIfRequired()
			}
			return;
		}

		this.updateEnemies(delta);
		this.drawEnemies();
		this.removeOutsideEnemies();
		this.tickTarget();
		this.tickGamefield(delta, realDelta);

		this.emit(GameScreen.EVENT_ON_TICK_OCCURRED, {delta: delta, realDelta: realDelta});
	}

	tickTarget()
	{
		this._fTargetingController_tc && this._fTargetingController_tc.tick();
	}

	static getServerTime()
	{
		return serverTime;
	}

	_onAssetsLoadingError()
	{
		this.gameField && this.gameField.clearRoom();
		this.clear();
		this.clearAllButLobbyInfo();
	}

	_onWebglContextLost()
	{
		this.gameField && this.gameField.clearRoom();
		this.clear();
		this.clearAllButLobbyInfo();	
	}

	_handleErrorCode(serverData, requestData, errorType)
	{
		if (GameWebSocketInteractionController.isFatalError(errorType))
		{
			console.error("GameScreen. Error handle.", serverData);
			console.log("GameScreen ServerError " +  JSON.stringify(serverData));
			APP.logger.i_pushError(`GameScreen. ${errorType}: ${JSON.stringify(serverData)}`)
			this.gameField && this.gameField.clearRoom();
			this.clear();
			this.clearAllButLobbyInfo();
			return;
		}
		else
		{
			console.warn("GameScreen. Error handle.", serverData);
			console.log("GameScreen ServerError " +  JSON.stringify(serverData));
		}

		var isRoundNotStartedError = false;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_OPEN:
				if (APP.isBattlegroundGame)
				{
					if (requestData.class === CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
					{
						this.player.confirmIsSent = false;
					}

					this._fNeedUpdateStartGameUrl_bl = true;
					this._gameUrlNeedToUpdate();
				}
				else
				{
					this.goBackToLobby(serverData.code);
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.ROUND_NOT_STARTED:
				isRoundNotStartedError = true;
				requestData.isRoundNotStartedError = isRoundNotStartedError;
				if (requestData.class === CLIENT_MESSAGES.BULLET)
				{
					this.emit(GameScreen.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, {requestData: requestData});
				}
				if (requestData && requestData.rid >= 0)
				{
					switch (requestData.class)
					{
						case  CLIENT_MESSAGES.SHOT:
							let weaponId = requestData.weaponId;
							this.revertAmmoBack(weaponId, undefined, requestData.isPaidSpecialShot, isRoundNotStartedError);
							this.onShotResponse();
							break;
					}
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.WRONG_WEAPON:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_BULLETS:
				if (requestData && requestData.rid >= 0)
				{
					switch (requestData.class)
					{
						case  CLIENT_MESSAGES.SHOT:
							let weaponId = requestData.weaponId;
							this.revertAmmoBack(weaponId, undefined, requestData.isPaidSpecialShot, isRoundNotStartedError);
							this.onShotResponse();
							break;
					}
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.REQUEST_FREQ_LIMIT_EXCEEDED:
			case GameWebSocketInteractionController.ERROR_CODES.WRONG_COORDINATES:
				if (requestData && requestData.rid >= 0)
				{
					switch (requestData.class)
					{
						case  CLIENT_MESSAGES.SHOT:
							{
								let weaponId = requestData.weaponId;
								this.revertAmmoBack(weaponId, undefined, requestData.isPaidSpecialShot);
								this.onShotResponse();
							}
							break;
						default:
							//nothing to do
							break;
					}
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_OBSERVERS:
			case GameWebSocketInteractionController.ERROR_CODES.TOO_MANY_PLAYER:
				this.player.sitInIsSent = false;
			case GameWebSocketInteractionController.ERROR_CODES.ROOM_NOT_FOUND:
			case GameWebSocketInteractionController.ERROR_CODES.ROOM_MOVED:
			case GameWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this.gameField && this.gameField.clearRoom(false, true, true);
				this.clear();
				this.clearAllButLobbyInfo();
				break;

			case GameWebSocketInteractionController.ERROR_CODES.NOT_SEATER:
				if (requestData.class === CLIENT_MESSAGES.BULLET)
				{
					this.emit(GameScreen.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, {requestData: requestData});
				}

				if (
						APP.webSocketInteractionController.isSitoutRequestInProgress
						|| APP.webSocketInteractionController.isCloseRoomRequestInProgress
						|| !this.player.sitIn
						|| this._fForseSitOutInProgress
					)
				{
					// no need to request full game info due to room exit initiated by the player,
					// or sitout already occurred in game client
					this.emit(GameScreen.EVENT_ON_GAME_RESTORE_AFTER_UNSEASONABLE_REQUEST_CANCELED);

					if (
							APP.isBattlegroundGamePlayMode
							&& requestData.class === CLIENT_MESSAGES.RE_BUY
							&&
								(
									!this.gameStateController.info.isPlayerSitIn || 
									APP.webSocketInteractionController.isSitoutRequestInProgress
								)
						)
					{
						APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_GAME_URL_NEED_UPDATED_ON_PLAY_AGAIN);
					}
				}
				else
				{
					this.player.sitIn = false;
					this.player.seatId = undefined;
					this.gameField && this.gameField.clearRoom(false, true, true);
					this.clear();
					this._restoreAfterUnseasonableRequest = true;
					this._restoreAfterLagsInProgress = false;
					APP.playerController.updateMasterPlayerSeatId(undefined);
					this._fIsSitOutOnNotEnoughPlayersExpected_bl = false;

					this._requestFullGameInfo();
				}
				break;

			case GameWebSocketInteractionController.ERROR_CODES.NEED_SITOUT:
				if (requestData && requestData.rid >= 0)
				{
					switch (requestData.class)
					{
						case  CLIENT_MESSAGES.SIT_IN:
							this.player.sitInIsSent = false;

							if (this._canJoinRoom())
							{
								this.sendSitIn();
							}
							break;
						case  CLIENT_MESSAGES.CLOSE_ROOM:
							this._onCloseRoomDenied();
							break;
					}
				}
				break;

			case GameWebSocketInteractionController.ERROR_CODES.INSUFFICIENT_FUNDS:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ENOUGH_MONEY:
				this.gameField.resetWaitBuyIn();
				this.gameField.resetTargetIfRequired(true);

				if (
						requestData && requestData.rid >= 0 && requestData.class === CLIENT_MESSAGES.SIT_IN
						&& APP.tickerAllowed
					)

				{
					this.player.sitInIsSent = false;
					this.goBackToLobby();
				}
				break;

			case GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE:
			case GameWebSocketInteractionController.ERROR_CODES.NOT_FATAL_BAD_BUYIN:
			case GameWebSocketInteractionController.ERROR_CODES.PREV_OPERATION_IS_NOT_COMPLETE:
				this.gameField.resetWaitBuyIn();

				if (APP.isBattlegroundMode)
				{
					if (
							this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound
							&& this.gameStateController.info.isPlayerSitIn
							&& !this.gameField.roundResultScreenController.info.isScreenActive
							&& requestData
							&& (requestData.class === CLIENT_MESSAGES.RE_BUY || requestData.class === CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
						)
					{
						this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = false;
						this.player.confirmIsSent = false;

						this._fBattlegroundIsNeedFullGameInfoUpdateOnGameState_bl = false;
						this.gameStateController.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onCheckGameStateChangedForRequestFullGameInfo);
						break;
					}
					
					this.player.sitIn = false;

					if (serverData.code === GameWebSocketInteractionController.ERROR_CODES.BUYIN_NOT_ALLOWED_AT_CURRENT_GAME_STATE)
					{
						// no need to send _gameUrlNeedToUpdate, url will be requested from LobbyScreen for this error code
					}
					else
					{
						this._fNeedUpdateStartGameUrl_bl = true;
						this._gameUrlNeedToUpdate();
					}
					break;
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.CHANGE_BET_NOT_ALLOWED:
				this._onServerBetLevelChangeNotConfirmed();
				break;
			case GameWebSocketInteractionController.ERROR_CODES.NOT_ALLOWED_PLACE_BULLET:
				this.emit(GameScreen.EVENT_ON_BULLET_PLACE_NOT_ALLOWED, {requestData: requestData});
				break;
			case GameWebSocketInteractionController.ERROR_CODES.SW_PURCHASE_LIMIT_EXCEEDED:
				this.gameField._onResetSWSwitched();
				this.gameField.resetWaitBuyIn();
				break;
			case GameWebSocketInteractionController.ERROR_CODES.BAD_REQUEST:
				if (requestData && requestData.class === CLIENT_MESSAGES.SHOT && requestData.bulletId !== undefined)
				{
					// BAD_REQUEST for ricochet shots is not considered as fatal error due to https://jira.dgphoenix.com/browse/DRAG-986
					this.revertAmmoBack(requestData.weaponId, undefined, requestData.isPaidSpecialShot, isRoundNotStartedError);
					this.onShotResponse();
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.NOT_CONFIRM_BUYIN:
				this._fNeedUpdateStartGameUrl_bl = false;
				this.transitionViewController.info.blockChangeState = true;
				break;
			case GameWebSocketInteractionController.ERROR_CODES.FOUND_PENDING_OPERATION:
				if (requestData && requestData.rid >= 0)
				{
					switch (requestData.class)
					{
						case  CLIENT_MESSAGES.SIT_IN:
							this.player.sitInIsSent = false;
							break;
						case  CLIENT_MESSAGES.BUY_IN:
							this.gameField.resetWaitBuyIn();
							break;
						case  CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN:
							this.player.confirmIsSent = false;
							break;
						case  CLIENT_MESSAGES.BET_LEVEL:
							this._onServerBetLevelChangeNotConfirmed();
							break;
					}
				}
				break;
			case GameWebSocketInteractionController.ERROR_CODES.SIT_OUT_NOT_ALLOWED:
				if (this._fIsCountDownCanceled)
				{
					this._fIsCountDownCanceled = false;
					this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound = !APP.playerController.info.isObserver;
				}
				break;
		}
	}

	_onNoPlaceToSeat()
	{
		this.player.sitInIsSent = false;

		this.clear();
		this.clearAllButLobbyInfo();
		
		this.emit(GameScreen.EVENT_ON_NO_EMPTY_SEATS);
	}

	_gameUrlNeedToUpdate()
	{
		console.log("game url needs updating " + this._fNeedUpdateStartGameUrl_bl + " vs " + APP.webSocketInteractionController.info.isAnyCriticalErrorDelayed )

		if (this._fNeedUpdateStartGameUrl_bl && !APP.webSocketInteractionController.info.isAnyCriticalErrorDelayed)
		{
			APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_GAME_URL_NEED_UPDATED_ON_PLAY_AGAIN);
		}
	}

	//PENDING_OPERATION...
	_onPendingOperationStarted(event)
	{
		let l_ws = APP.webSocketInteractionController;
		if (this.player.sitInIsSent)
		{
			this.player.sitInIsSent = false;
		}

		if (this.gameField.waitBuyIn)
		{
			this.gameField.resetWaitBuyIn();
		}

		if (this.player.confirmIsSent)
		{
			this.player.confirmIsSent = false;
		}
	}

	_onPendingOperationCompleted(event)
	{
		if (APP.isBattlegroundGame)
		{
			let l_gwsic = APP.webSocketInteractionController;

			if (
					(APP.playerController.info.isObserver || !(this.player.seatId >= 0) /*for reconnect in PLAY state*/ )
					&& !l_gwsic.isSitoutRequestInProgress
					&& !l_gwsic.hasUnRespondedRequest(CLIENT_MESSAGES.SIT_IN)
					&& !l_gwsic.hasUnRespondedRequest(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
					&& !l_gwsic.hasDelayedRequests(CLIENT_MESSAGES.SIT_IN)
					&& !l_gwsic.hasDelayedRequests(CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
				)
			{
				if (this.battlegroundGameController.info.isPlayerClickedConfirmPlayForNextBattlegroundRound)
				{
					if (this.gameStateController.info.isWaitState)
					{
						this.sendSitIn();
					}
				}
				else if (
							APP.playerController.info.isObserver
							&& this.gameStateController.info.isWaitState
							&& !this.gameField.roundResultScreenController.info.isScreenActive
						)
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.BATTLEGROUND_PLAY_AGAIN_REQUEST, {isRRActive: this.isRoundResultDisplayInProgress()});
				}
			}

		}
	}
	//...PENDING_OPERATION
}



export default GameScreen;

function parseGet(aOptLocation_str = undefined)
{
	var get = {};

	var s = aOptLocation_str ? aOptLocation_str : window.location.toString();
	var p = s.indexOf("?");
	var tmp, params;
	if (p >= 0)
	{
		s = s.substr(p + 1, s.length);
		params = s.split("&");
		for (var i = 0; i < params.length; i++)
		{
			tmp = params[i].split("=");
			get[tmp[0]] = tmp[1];
		}
	}

	return get;
}