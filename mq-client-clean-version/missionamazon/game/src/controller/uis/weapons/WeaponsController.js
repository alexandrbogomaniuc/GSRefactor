import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import WeaponsInfo from '../../../model/uis/weapons/WeaponsInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import PlayerInfo from '../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GamePlayerController from '../../custom/GamePlayerController';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import { ROUND_STATE } from '../../../model/state/GameStateInfo';
import GameStateController from '../../state/GameStateController';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';
import BonusInfo from './../../../../../../common/PIXI/src/dgphoenix/unified/model/custom/bonus/BonusInfo';
import GameExternalCommunicator, { LOBBY_MESSAGES, GAME_MESSAGES } from '../../../external/GameExternalCommunicator';

class WeaponsController extends SimpleController
{
	static get EVENT_ON_WEAPONS_UPDATED()	{return "onWeaponsUpdated";}
	static get EVENT_ON_FRB_AMMO_UPDATED()	{return "onFrbAmmoUpdated";}
	static get EVENT_ON_REAL_AMMO_VALUE_UPDATED()	{return "EVENT_ON_REAL_AMMO_VALUE_UPDATED";}
	static get EVENT_ON_QUALIFY_WIN_TRANSFERRED_TO_REAL_AMMO() { return "EVENT_ON_QUALIFY_WIN_TRANSFERRED_TO_REAL_AMMO"; }
	static get EVENT_ON_AMMO_UPDATED() 		{return "onAmmoUpdated";}

	constructor()
	{
		super(new WeaponsInfo());
	}

	__init()
	{
		super.__init();

		this._fAwardingInProgressCound_num = 0;
		this._fListeningForEndShow_bln = false;

		this._gameScreen = APP.currentWindow;
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_WEAPON_SHOTS_UPDATED, this._onRemainingShotsUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onWeaponUpdated, this);
		this._gameScreen.on(GameScreen.EVENT_ON_REVERT_AMMO_BACK, this._onRevertAmmoBack, this);
		this._gameScreen.on(GameScreen.EVENT_DECREASE_AMMO, this._decreaseAmmo, this);
		this._gameScreen.on(GameScreen.EVENT_ON_WEAPON_AWARDED, this._onWeaponAwarded, this);
		this._gameScreen.on(GameScreen.EVENT_ON_TIME_TO_HANDLE_AWARDED_WEAPON, this._onTimeToHandleAwardedWeapon, this);
		this._gameScreen.on(GameScreen.EVENT_ON_WEAPON_BOUGHT, this._onWeaponBought, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED, this._onRoomRestoringOnLagsStarted, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this._onBackToLobby, this);
		this._gameScreen.on(GameScreen.EVENT_ON_WIN_TO_AMMO_TRANSFERED, this._onWinTransfered, this);
		this._gameScreen.on(GameScreen.EVENT_ON_BET_MULTIPLIER_UPDATED, this._onPlayerBetMultiplierUpdate, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenActivated, this);

		this._fPlayerController_pc = APP.playerController;
		this._fPlayerInfo_pi = this._fPlayerController_pc.i_getInfo();
		this._fPlayerController_pc.on(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED, this._onPlayerWeaponUpdated, this);
		this._fPlayerController_pc.on(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);

		this._fGameStateController_gsc = this._gameScreen.gameStateController;
		this._fGameStateInfo_gsi = this._fGameStateController_gsc.info;
		this._fGameStateController_gsc.on(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		APP.externalCommunicator.on(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);
	}

	//PUBLIC...
	i_clearAll()
	{
		this.info.currentWeaponId = undefined;
	}

	destroy()
	{
		//[Y]TODO remove event listeners
		if (this._fPlayerController_pc)
		{
			this._fPlayerController_pc.off(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED, this._onPlayerWeaponUpdated, this);
			this._fPlayerController_pc.off(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
			this._fPlayerController_pc = null;
		}

		this._fPlayerInfo_pi = null;

		if (this._fGameStateController_gsc)
		{
			this._fGameStateController_gsc.off(GameStateController.EVENT_ON_GAME_STATE_CHANGED, this._onGameStateChanged, this);
			this._fGameStateController_gsc = null;
		}
		this._fGameStateInfo_gsi = null;

		if (this._gameScreen)
		{
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_INFO_UPDATED, this._onRoomInfoUpdated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_WEAPON_SHOTS_UPDATED, this._onRemainingShotsUpdated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onWeaponUpdated, this);
			this._gameScreen.off(GameScreen.EVENT_ON_REVERT_AMMO_BACK, this._onRevertAmmoBack, this);
			this._gameScreen.off(GameScreen.EVENT_DECREASE_AMMO, this._decreaseAmmo, this);
			this._gameScreen.off(GameScreen.EVENT_ON_WEAPON_AWARDED, this._onWeaponAwarded, this);
			this._gameScreen.off(GameScreen.EVENT_ON_TIME_TO_HANDLE_AWARDED_WEAPON, this._onTimeToHandleAwardedWeapon, this);
			this._gameScreen.off(GameScreen.EVENT_ON_WEAPON_BOUGHT, this._onWeaponBought, this);
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_RESTORING_ON_LAGS_STARTED, this._onRoomRestoringOnLagsStarted, this);
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_PAUSED, this._onRoomPaused, this);
			this._gameScreen.off(GameScreen.EVENT_ON_ROOM_UNPAUSED, this._onRoomUnpaused, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BACK_TO_LOBBY_BUTTON_CLICKED, this._onBackToLobby, this);
			this._gameScreen.off(GameScreen.EVENT_ON_BET_MULTIPLIER_UPDATED, this._onPlayerBetMultiplierUpdate, this);
			this._gameScreen.off(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenActivated, this);

			this._gameScreen = null;
		}

		APP.webSocketInteractionController && APP.webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		APP.externalCommunicator.off(GameExternalCommunicator.LOBBY_MESSAGE_RECEIVED, this._onLobbyExternalMessageReceived, this);

		super.destroy();
	}
	//...PUBLIC

	//PRIVATE...
	_onLobbyExternalMessageReceived(aEvent_obj)
	{
		switch (aEvent_obj.type)
		{
			case LOBBY_MESSAGES.FRB_SHOTS_UPDATED:
				let data = aEvent_obj.data;
				let lInfo_wsi = this.i_getInfo();

				lInfo_wsi.realAmmo = data.ammoAmount;
				this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
				this.emit(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED);
			break;
		}
	}

	_onRoomInfoUpdated()
	{
		let lInfo_wsi = this.i_getInfo();
		let lId_num = APP.playerController.info.getTurretSkinId(APP.playerController.info.betLevel);
		if (lId_num <= 0) lId_num = 1;
		lInfo_wsi.currentDefaultWeaponId = lId_num;
	}

	_onPlayerWeaponUpdated(aEvent_obj)
	{
		let lInfo_wsi = this.i_getInfo();
		let lWeaponId_int = this._fPlayerInfo_pi.weaponId;

		lInfo_wsi.currentWeaponId = lWeaponId_int;
	}

	_onPlayerInfoUpdated(aEvent_obj)
	{
		let lInfo_wsi = this.i_getInfo();

		let lUpdatedWeapons_obj = aEvent_obj.data[PlayerInfo.KEY_WEAPONS];
		if (lUpdatedWeapons_obj)
		{
			lInfo_wsi.weapons = lUpdatedWeapons_obj.value;

			this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
		}
	}

	_onPlayerBetMultiplierUpdate(event)
	{
		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.currentDefaultWeaponId = this._fPlayerInfo_pi.getTurretSkinId(event.multiplier);
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let lInfo_wsi = this.i_getInfo();

		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				if (APP.currentWindow.gameFrbController.info.frbMode)
				{
					APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.FRB_SHOTS_UPDATE_REQUIRED, {alreadySitInAmmo: data.alreadySitInAmmoCount});
				}
			break;
			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				if (data.rid !== -1)
				{
					let lIsFrb_bln = !!(APP.currentWindow.gameFrbController.info.frbMode || (data.mode == BonusInfo.TYPE_FRB));
					if (lIsFrb_bln)
					{
						APP.externalCommunicator.sendExternalMessage(GAME_MESSAGES.FRB_SHOTS_UPDATE_REQUIRED);
						if (!data.ammoAmount)
						{
							return;
						}
					}

					if(APP.isBattlegroundGame)
					{
						data.ammoAmount = 0;
					}
					lInfo_wsi.realAmmo = data.ammoAmount;
					if (	this._fPlayerInfo_pi.qualifyWin > 0 
							&& !APP.isBattlegroundGame
							&& !(APP.currentWindow.gameFrbController.info.frbMode 
									|| (data.mode == BonusInfo.TYPE_FRB))
						)
					{
						if (this._fPlayerInfo_pi.qualifyWin >= this._fPlayerInfo_pi.currentStake)
						{
							console.error("[Y] qualifyWin = " + this._fPlayerInfo_pi.qualifyWin + " > currentStake = " + this._fPlayerInfo_pi.currentStake);
						}
						lInfo_wsi.realAmmo = data.ammoAmount + this._fPlayerInfo_pi.qualifyWin/this._fPlayerInfo_pi.currentStake;
						// reset qualify win
						this.emit(WeaponsController.EVENT_ON_QUALIFY_WIN_TRANSFERRED_TO_REAL_AMMO);
					}

					this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);

					if (lIsFrb_bln)
					{
						this.emit(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED);
					}
				}
			break;

			case SERVER_MESSAGES.WEAPONS:
				if (data.ammoAmount > 0)
				{
					if (APP.currentWindow.gameFrbController.info.frbMode && !data.ammoAmount)
					{
						return;
					}

					lInfo_wsi.realAmmo = data.ammoAmount;

					this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);

					if (APP.currentWindow.gameFrbController.info.frbMode)
					{
						this.emit(WeaponsController.EVENT_ON_FRB_AMMO_UPDATED);
					}
				}

				if (data.weapons && data.weapons.length)
				{
					let lInfo_wsi = this.i_getInfo();
					lInfo_wsi.weapons = data.weapons;

					this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
				}
			break;
			case SERVER_MESSAGES.BALANCE_UPDATED:
				//debug: compare ammo amount on client/server...
				if (data.rid !== -1)
				{
					let requestData = event.requestData || {};
					let clientAmmo = requestData.clientAmmo || 0;
					let clientPendingAmmo = requestData.clientPendingAmmo || 0;
					let clientResultAmmo = requestData.clientResultAmmo || 0;
					let serverAmmo = data.serverAmmo;
					let lRicochetBullets_num = this._fPlayerInfo_pi.ricochetBullets;

					if (APP.isBattlegroundGame)
					{
						clientPendingAmmo = 0;
						lRicochetBullets_num = 0;
						serverAmmo = 0;
					}

					if (clientResultAmmo !== undefined && serverAmmo !== undefined && !APP.isBattlegroundGame)
					{
						if ((clientResultAmmo + lRicochetBullets_num*this._fPlayerInfo_pi.betLevel) != serverAmmo)
						{
							APP.logger.i_pushWarning(`Weapon Controller. Wrong Ammo. Client Result ${clientResultAmmo}; Server ${serverAmmo}; Client Real ${clientAmmo}; Client Pending ${clientPendingAmmo}; Ricochet Bullets ${lRicochetBullets_num}; Bet level ${this._fPlayerInfo_pi.betLevel}`);
							console.error("*** WRONG AMMO*** clientResultAmmo:", clientResultAmmo, "; serverAmmo:", serverAmmo, "; clientAmmo:", clientAmmo, "; clientPendingAmmo:", clientPendingAmmo, 
												"; ricochetBullets:", lRicochetBullets_num, "; bet level:", this._fPlayerInfo_pi.betLevel);
						}
					}
				}
				//...debug: compare ammo amount on client/server
			break;
			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				if (APP.tournamentModeController.info.resetBalanceAfterRebuy)
				{
					this._clearDefaultAmmo();
				}
			break;
		}
	}

	get _roundResultScreenInfo()
	{
		return APP.gameScreen.gameField.roundResultScreenController.info;
	}

	_onRemainingShotsUpdated(aEvent_obj)
	{
		let updateAfterBuyIn = !!aEvent_obj.updateAfterBuyIn;

		if (
				(
					this._fGameStateInfo_gsi.gameState === ROUND_STATE.QUALIFY
					&& this._roundResultScreenInfo.roundResultResponseRecieved /* condition added, because clear ammo occurs on RoundResult response only*/
					|| this._fGameStateInfo_gsi.gameState === ROUND_STATE.WAIT
				)
				&& !updateAfterBuyIn
				&& aEvent_obj.shots != 0
			)
		{
			APP.logger.i_pushDebug(`WeaponsController. Reject ammo updating (weaponId: ${aEvent_obj.weaponId}, shots: ${aEvent_obj.shots}) due to QUALIFY or WAIT state.`);
			console.log(`Reject ammo updating (weaponId: ${aEvent_obj.weaponId}, shots: ${aEvent_obj.shots}) due to QUALIFY or WAIT state.`);
			return;
		}

		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.i_updateWeaponShots(aEvent_obj.weaponId, aEvent_obj.shots);

		if (aEvent_obj.weaponId == WEAPONS.DEFAULT)
		{
			this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
		}
		
		this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
	}

	_onWeaponUpdated(aEvent_obj)
	{
		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.currentWeaponId = aEvent_obj.weaponId;
	}

	_onGameStateChanged(event)
	{
		switch (this._fGameStateInfo_gsi.gameState)
		{
			case ROUND_STATE.WAIT:
				if (!APP.currentWindow.gameFrbController.info.frbMode)
				{
					this._clearDefaultAmmo();
				}

				if (!this._isKeepSWModeActive)
				{
					this.info.weapons = [];
				}

				this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
			break;
		}
	}

	get _isKeepSWModeActive()
	{
		return APP.currentWindow.isKeepSWModeActive;
	}

	_onRoundResultScreenActivated()
	{
		switch (this._fGameStateInfo_gsi.gameState)
		{
			case ROUND_STATE.QUALIFY:
				if (!this._isKeepSWModeActive)
				{
					this.info.weapons = [];
				}

				this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
			break;
		}
	}

	_onWinTransfered()
	{
		let lAwardingController_ac = APP.currentWindow.awardingController;
		if(lAwardingController_ac && !lAwardingController_ac.hasUncountedAwards && this._fGameStateInfo_gsi.gameState === ROUND_STATE.QUALIFY && !this._isKeepSWModeActive)
		{
			this._clearDefaultAmmo();

			this.info.weapons = [];

			this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
		}
	}

	_clearDefaultAmmo()
	{
		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.i_zeroDefaultShots();

		this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
	}

	_onRevertAmmoBack(aEvent_obj)
	{
		let lWeapSave_bln = this._isKeepSWModeActive;
		let lIsRevertRejectState_bl = this._roundResultScreenInfo.roundResultResponseRecieved /* condition added, because clear ammo occurs on RoundResult response only*/
										|| aEvent_obj.revertByRoundNotStartedError;

		if (lIsRevertRejectState_bl && APP.currentWindow.gameFrbController.info.frbMode)
		{
			if (aEvent_obj.weaponId == WEAPONS.DEFAULT || lWeapSave_bln)
			{
				lIsRevertRejectState_bl = false;
			}
		}

		if (
				(!lWeapSave_bln || (lWeapSave_bln && (aEvent_obj.weaponId == WEAPONS.DEFAULT)))
				&& (
						this._fGameStateInfo_gsi.gameState === ROUND_STATE.QUALIFY
						&& 	lIsRevertRejectState_bl
						|| this._fGameStateInfo_gsi.gameState === ROUND_STATE.WAIT
					)
			)
		{
			APP.logger.i_pushDebug(`WeaponsController. Reject ammo reverting due to ${this._fGameStateInfo_gsi.gameState} state.`);
			console.log("Reject ammo reverting due to QUALIFY or WAIT state.");
			return;
		}
		
		let lRevertAmmoAmount_int = aEvent_obj.revertAmmoAmount || 1;
		let lInfo_wsi = this.i_getInfo();

		if (lInfo_wsi.currentWeaponId !== WEAPONS.DEFAULT && aEvent_obj.isPaidSpecialShot)
		{
			lInfo_wsi.i_revertAmmoBack(WEAPONS.DEFAULT, lRevertAmmoAmount_int);
		}
		else
		{
			lInfo_wsi.i_revertAmmoBack(aEvent_obj.weaponId, lRevertAmmoAmount_int);
		}
		this.emit(WeaponsController.EVENT_ON_AMMO_UPDATED);

		if (aEvent_obj.weaponId == WEAPONS.DEFAULT || aEvent_obj.isPaidSpecialShot)
		{
			this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
		}
	}

	_decreaseAmmo(aEvent_obj)
	{
		if (
				this._fGameStateInfo_gsi.gameState === ROUND_STATE.QUALIFY
				&& this._roundResultScreenInfo.roundResultResponseRecieved /* condition added, because clear ammo occurs on RoundResult response only*/
				|| this._fGameStateInfo_gsi.gameState === ROUND_STATE.WAIT
			)
		{
			APP.logger.i_pushDebug(`WeaponsController. Reject ammo decreasing due to ${this._fGameStateInfo_gsi.gameState} state.`);
			console.log("Reject ammo decreasing due to QUALIFY or WAIT state.");
			return;
		}
		
		let lDecreaseAmmoAmount_int = aEvent_obj.decreaseAmmoAmount || 1;
		let lInfo_wsi = this.i_getInfo();

		if (lInfo_wsi.currentWeaponId != WEAPONS.DEFAULT && aEvent_obj.isPaidSpecialShot)
		{
			lInfo_wsi.i_decreaseAmmo(WEAPONS.DEFAULT, lDecreaseAmmoAmount_int);
		}
		else
		{
			lInfo_wsi.i_decreaseAmmo(lInfo_wsi.currentWeaponId, lDecreaseAmmoAmount_int);
		}

		this.emit(WeaponsController.EVENT_ON_AMMO_UPDATED);

		if (lInfo_wsi.currentWeaponId == WEAPONS.DEFAULT || aEvent_obj.isPaidSpecialShot)
		{
			this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
		}

	}

	_onWeaponAwarded(aEvent_obj)
	{
		this._fAwardingInProgressCound_num++;
		if (this._gameScreen && !this._fListeningForEndShow_bln)
		{
			this._fListeningForEndShow_bln = true;
			this._gameScreen.gameField.once(GameField.EVENT_ON_END_SHOW_WEAPON, this._onWeaponShowed, this);
		}
	}

	_onTimeToHandleAwardedWeapon(aEvent_obj)
	{
		if (this._gameScreen)
		{
			this._proceedAwardedWeapon(aEvent_obj.weapon);
		}
	}

	_onWeaponBought(aEvent_obj)
	{
		this._fAwardingInProgressCound_num++;
		if (this._gameScreen && !this._fListeningForEndShow_bln)
		{
			this._fListeningForEndShow_bln = true;
			this._gameScreen.gameField.once(GameField.EVENT_ON_END_SHOW_WEAPON, this._onWeaponShowed, this);
		}
	}

	_onWeaponShowed(aEvent_obj)
	{
		if (!aEvent_obj.weapon) return;

		if (
				APP.currentWindow.isPaused
				&& this._fGameStateInfo_gsi.gameState != ROUND_STATE.PLAY
				&& !this._isKeepSWModeActive
			)
		return;

		let lWeapon_obj = aEvent_obj.weapon;
		this._proceedAwardedWeapon(lWeapon_obj);

		this._fAwardingInProgressCound_num--;

		if (this._fAwardingInProgressCound_num > 0)
		{
			this._gameScreen.gameField.once(GameField.EVENT_ON_END_SHOW_WEAPON, this._onWeaponShowed, this);
		}
		else
		{
			this._fListeningForEndShow_bln = false;
		}

	}

	_proceedAwardedWeapon(aWeapon_obj)
	{
		let lWeapon_obj = aWeapon_obj;

		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.i_addWeapon(lWeapon_obj.id, lWeapon_obj.shots);
		lInfo_wsi.i_awardWeapon(lWeapon_obj.id, lWeapon_obj.shots);

		this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
	}

	_onRoomRestoringOnLagsStarted()
	{
		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.i_clearAwardedWeapons();
	}

	_onRoomPaused()
	{
		let lInfo_wsi = this.i_getInfo();
		lInfo_wsi.i_clearAwardedWeapons();
	}

	_onRoomUnpaused()
	{
	}

	_onBackToLobby()
	{
		let lInfo_wsi = this.i_getInfo();

		lInfo_wsi.i_clearAwardedWeapons();
		lInfo_wsi.i_clearWeapons();
		lInfo_wsi.i_zeroDefaultShots();

		this.emit(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED);
		this.emit(WeaponsController.EVENT_ON_WEAPONS_UPDATED);
	}
	//...PRIVATE
}

export default WeaponsController;