import PlayerController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/custom/PlayerController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Game from '../../Game';
import PlayerInfo from '../../../../../common/PIXI/src/dgphoenix/unified/model/custom/PlayerInfo';
import GameScreen from '../../main/GameScreen';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import { CLIENT_MESSAGES, SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';
import RoundResultScreenController from '../uis/roundresult/RoundResultScreenController';
import FireSettingsController from '../../controller/uis/fire_settings/FireSettingsController';
import GameField from './../../main/GameField';
import WeaponsController from '../../controller/uis/weapons/WeaponsController';
import BattlegroundTutorialController from '../../controller/uis/custom/tutorial/BattlegroundTutorialController';
import GamePlayerInfo from '../../model/custom/GamePlayerInfo';
import { ROUND_STATE } from '../../model/state/GameStateInfo';

class GamePlayerController extends PlayerController
{
	static get EVENT_ON_PLAYER_INFO_UPDATED() 			{return PlayerController.EVENT_ON_PLAYER_INFO_UPDATED;}
	static get EVENT_ON_PLAYER_WEAPON_UPDATED() 		{return "onPlayerWeaponIdUpdated";}
	static get EVENT_ON_WEAPON_SURPLUS_UPDATED()		{return "onWEaponSurplusUpdated";}

	static get EVENT_ON_QUALIFY_WIN_SYNC_BY_SERVER_STARTING()	{return "EVENT_ON_QUALIFY_WIN_SYNC_BY_SERVER_STARTING";}

	//INIT...
	constructor(aOptInfo_ussi)
	{
		super(aOptInfo_ussi ? aOptInfo_ussi : new GamePlayerInfo());
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fAlreadySitIn_bln = false;

		APP.on(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		APP.on("onGameStarted", this._onGameStarted, this);
	}
	//...INIT

	_onGameStarted()
	{
		APP.currentWindow.on(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onPlayerSpotWeaponUpdated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_WIN_TO_AMMO_TRANSFERED, this._onWinToAmmoTransfered, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_ACTIVATED, this._onRoundResultScreenActivated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_ROUND_RESULT_SCREEN_DEACTIVATED, this._onRoundResultScreenDeactivated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_BET_MULTIPLIER_UPDATED, this._onBetMultiplierUpdated, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_FRB_ENDED_COMPLETED, this._onFRBEndedCompleted, this);
		APP.currentWindow.on(GameScreen.EVENT_ON_CURRENT_POWER_UP_MULTIPLIER_UPDATED, this._onCurrentPowerUpMultiplierUpdated, this);

		let roundResultScreenController = APP.gameScreen.gameField.roundResultScreenController;
		roundResultScreenController.on(RoundResultScreenController.EVENT_ON_ROUND_RESULT_SCREEN_SKIPPED, this._onRoundResultScreenActivationSkipped, this);

		let lFireSettingsController_fsc = APP.currentWindow.fireSettingsController;
		lFireSettingsController_fsc.on(FireSettingsController.EVENT_ON_FIRE_SETTINGS_CHANGED, this._onFireSettingsChanged, this);

		APP.currentWindow.gameField.on(GameField.EVENT_ON_WEAPON_TO_UNPRESENTED_TRANSFER_REQUIRED, this._onWeaponToUnpresentedRequired, this);
		APP.currentWindow.gameField.on(GameField.EVENT_ON_TIME_TO_CONVERT_BALANCE_TO_AMMO, this._onTimeToConvertBalanceToAmmo, this);
		APP.currentWindow.gameField.on(GameField.EVENT_ON_RICOCHET_BULLETS_UPDATED, this._onRicochetBulletsUpdated, this);

		let lWeaponsController_wsc = this._fWeaponsController_wsc = APP.currentWindow.weaponsController;
		lWeaponsController_wsc.on(WeaponsController.EVENT_ON_QUALIFY_WIN_TRANSFERRED_TO_REAL_AMMO, this._onQualifyWinTransferredToRealAmmo, this);
		lWeaponsController_wsc.on(WeaponsController.EVENT_ON_REAL_AMMO_VALUE_UPDATED, this._onRealAmmoValueUpdated, this);
		
		let lBattlegroundTutorialController_btc = this._fBattlegroundTutorialController_btc = APP.gameScreen.gameField.battlegroundTutorialController;
		lBattlegroundTutorialController_btc.on(BattlegroundTutorialController.EVENT_ON_SETTINGS_TUTORIAL_STATE_CHANGED, this._onSettingsTutorialStateChanged, this);
	}

	_onRicochetBulletsUpdated(aEvent_obj)
	{
		let currentBullets = aEvent_obj.currentBullets;
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_RICOCHET_BULLETS] = {value: currentBullets};
		this.info.setPlayerInfo(PlayerInfo.KEY_RICOCHET_BULLETS, lUpdatedData_obj[PlayerInfo.KEY_RICOCHET_BULLETS]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onFRBEndedCompleted()
	{
		if (APP.currentWindow.gameFrbController.info.frbEnded)
		{
			let lUpdatedData_obj = {};
			lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0, time: Date.now()};

			this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);
			this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
		}
	}

	_onCurrentPowerUpMultiplierUpdated(aEvent_obj = {})
	{
		const lPowerUpMultiplier_num = aEvent_obj && aEvent_obj.currentPowerUpMultiplier;
		const lUpdatedData_obj = {};

		lUpdatedData_obj[PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER] = {value: lPowerUpMultiplier_num};
		this.info.setPlayerInfo(PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER, lUpdatedData_obj[PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER]);
		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data:lUpdatedData_obj});
	}

	_onBetMultiplierUpdated(aEvent_obj)
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL] = {value: aEvent_obj.multiplier};
		this.info.setPlayerInfo(PlayerInfo.KEY_BET_LEVEL, lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onFireSettingsChanged()
	{
		let lFireSettingsInfo_fsi = APP.currentWindow.fireSettingsController.info;

		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET] = {value:lFireSettingsInfo_fsi.lockOnTarget};
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY] = {value:lFireSettingsInfo_fsi.targetPriority - 1};
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE] = {value:lFireSettingsInfo_fsi.autoFire};
		lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED] = {value:lFireSettingsInfo_fsi.fireSpeed - 1};

		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_LOCK_ON_TARGET]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_TARGET_PRIORITY]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_AUTO_FIRE]);
		this.info.setPlayerInfo(PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED, lUpdatedData_obj[PlayerInfo.KEY_FIRE_SETTINGS_FIRE_SPEED]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data:lUpdatedData_obj});
	}

	_onPlayerSpotWeaponUpdated(event)
	{
		this._updatePlayerWeapon({value: event.weaponId});
	}

	_onWinToAmmoTransfered(event)
	{
		let lTransferedWin_num = event.winAmount;
		if (lTransferedWin_num === undefined)
		{
			APP.logger.i_pushWarning(`GamePlayerController. Value for win transfer is not defined. ${JSON.stringify(event)}`);
			console.log(`Invalid value for win transfer.`);
		}
		else if (lTransferedWin_num > this.info.qualifyWin)
		{
			console.error(`Invalid qualify win amount to transfer presented win: ${this.info.qualifyWin}<${lTransferedWin_num}`);
			lTransferedWin_num = this.info.qualifyWin;
		}

		let lResultState_bl = this.roundResultActivationInProgress || this.roundResultActive;

		let lUpdatedData_obj = {};
		let lUpdatedQualifyWin_num = this.info.qualifyWin - (lResultState_bl ? 0 : lTransferedWin_num);
		lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: lUpdatedQualifyWin_num, complex: true};
		lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: this.info.unpresentedWin > lTransferedWin_num ? this.info.unpresentedWin - lTransferedWin_num : 0, complex: true};

		this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);
		this.info.setPlayerInfo(PlayerInfo.KEY_UNPRESENTED_WIN, lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onTimeToConvertBalanceToAmmo(event)
	{
		let lUpdatedData_obj = {};
		let lPrevBalance = this.info.balance;
		let lAmmoAmount_num = event.ammoAmount;
		let lAmmoCost_num = lAmmoAmount_num * this.info.currentStake;
		let lNewBalance = lPrevBalance - lAmmoCost_num;
		lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: lNewBalance};

		this.info.setPlayerInfo(PlayerInfo.KEY_BALANCE, lUpdatedData_obj[PlayerInfo.KEY_BALANCE]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onWeaponToUnpresentedRequired(aEvent_obj)
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: this.info.unpresentedWin + aEvent_obj.winValue, complex: true};
		this.info.setPlayerInfo(PlayerInfo.KEY_UNPRESENTED_WIN, lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN]);
	}

	_onQualifyWinTransferredToRealAmmo()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0, complex: true};
		this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);
		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data:lUpdatedData_obj});
	}

	_onRealAmmoValueUpdated()
	{
		let lUpdatedData_obj = {};

		let lRealAmmo_num = this._fWeaponsController_wsc.info.realAmmo;
		lUpdatedData_obj[PlayerInfo.KEY_REAL_AMMO] = {value: lRealAmmo_num, complex: true};
		this.info.setPlayerInfo(PlayerInfo.KEY_REAL_AMMO, lUpdatedData_obj[PlayerInfo.KEY_REAL_AMMO]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data:lUpdatedData_obj});
	}

	_onSettingsTutorialStateChanged(event)
	{
		let lUpdatedData_obj = {};

		let lState_bln = this._fBattlegroundTutorialController_btc.showAgain;
		lUpdatedData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED] = {value: lState_bln};
		this.info.setPlayerInfo(PlayerInfo.KEY_TOOL_TIP_ENABLED, lUpdatedData_obj[PlayerInfo.KEY_TOOL_TIP_ENABLED]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data:lUpdatedData_obj});
	}

	get roundResultActivationInProgress()
	{
		if (!APP.gameScreen.gameField) return false;
		return APP.gameScreen.gameField.roundResultScreenController.info.roundResultResponseRecieved;
	}

	get roundResultActive()
	{
		if (!APP.gameScreen.gameField) return false;
		return APP.gameScreen.gameField.roundResultScreenController.isActive;
	}

	get _playerSeatId()
	{
		return APP.currentWindow ? APP.currentWindow.player.seatId : -1;
	}

	get _isGameInProgress()
	{
		return APP.currentWindow ? APP.currentWindow.gameStateController.info.isGameInProgress : false;
	}

	get _roundResultActivated()
	{
		return APP.currentWindow ? APP.currentWindow.gameField.roundResultActive : false;
	}

	get _roundResultActivating()
	{
		return APP.currentWindow ? APP.currentWindow.gameField.roundResultActivationInProgress : false;
	}

	_onPlayerInfoUpdated(event)
	{
		let data = event.data;

		if (data.balance != undefined)
		{
			if (APP.isWebSocketInteractionInitiated && APP.webSocketInteractionController.isSitoutRequestInProgress)
			{
				delete event.data.balance;
			}
		}

		if (Object.keys(data).length)
		{
			for (let key in data)
			{
				this.info.setPlayerInfo(key, data[key]);
			}
		}
	}

	_updatePlayerWeapon(aData_obj)
	{
		if (!aData_obj || aData_obj.value == undefined)
		{
			return;
		}

		this.info.setPlayerInfo(PlayerInfo.KEY_WEAPON_ID, aData_obj);
		this.emit(GamePlayerController.EVENT_ON_PLAYER_WEAPON_UPDATED);
	}

	_onRoundResultScreenActivated()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: 0};
		
		this.info.setPlayerInfo(PlayerInfo.KEY_UNPRESENTED_WIN, lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN]);
		
		if (APP.isBattlegroundGame)
		{
			lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL] = {value: this.info.possibleBetLevels[0]};
			this.info.setPlayerInfo(PlayerInfo.KEY_BET_LEVEL, lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL]);
		}

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onRoundResultScreenDeactivated()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0};

		this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);
		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onRoundResultScreenActivationSkipped()
	{
		let lUpdatedData_obj = {};
		lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: 0};
		this.info.setPlayerInfo(PlayerInfo.KEY_UNPRESENTED_WIN, lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN]);

		lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0};
		this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);

		this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
	}

	_onGameFieldCleared()
	{
		if (this.roundResultActivationInProgress)
		{
			let lUpdatedData_obj = {};
			lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: 0};
			this.info.setPlayerInfo(PlayerInfo.KEY_UNPRESENTED_WIN, lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN]);

			lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0};
			this.info.setPlayerInfo(PlayerInfo.KEY_QUALIFY_WIN, lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN]);

			lUpdatedData_obj[PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER] = {value: 1};
			this.info.setPlayerInfo(PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER, lUpdatedData_obj[PlayerInfo.KEY_CURRENT_POWER_UP_MULTIPLIER]);

			this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
		}
	}

	updateMasterPlayerSeatId(aSeatIdValue_num)
	{
		let lPrevSeatIdValue_num = this.info.seatId;
		let lUpdatedData_obj = {};
		
		lUpdatedData_obj[PlayerInfo.KEY_SEATID] = {value: aSeatIdValue_num};
		this.info.setPlayerInfo(PlayerInfo.KEY_SEATID, lUpdatedData_obj[PlayerInfo.KEY_SEATID]);

		if (lPrevSeatIdValue_num !== this.info.seatId)
		{
			this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj});
		}
	}

	_onServerMessage(event)
	{
		let data = event.messageData;
		let requestData = event.requestData;
		let lUpdatedData_obj = {};

		let lSeaters_obj_arr;
		let lObserversList_obj_arr;
		let lRoomConfirmedBuyInBattlgroundSeats_int_arr;

		switch(data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				if (data.playerStake && data.playerStake !== -1)
				{
					lUpdatedData_obj[PlayerInfo.KEY_STAKE] = {value: data.playerStake, time: data.date};
				}
				else if (data.stake && data.stake !== -1)
				{
					lUpdatedData_obj[PlayerInfo.KEY_STAKE] = {value: data.stake, time: data.date};
				}
				else
				{
					lUpdatedData_obj[PlayerInfo.KEY_STAKE] = {value: -1, time: data.date};
					throw new Error("Wrong stake error:", -1);
				}

				if (data.battlegroundInfo)
				{
					if (APP.isCAFMode && data.isOwner !== undefined)
					{
						let lIsRoomOwner_bl = data.isOwner || false;
						lUpdatedData_obj[PlayerInfo.KEY_IS_CAF_ROOM_MANAGER] = {value: lIsRoomOwner_bl, time: data.date};
					}
					else
					{
						lUpdatedData_obj[PlayerInfo.KEY_IS_CAF_ROOM_MANAGER] = {value: false, time: data.date};
					}

					if (APP.isCAFMode && data.isKicked !== undefined)
					{
						let lIsKicked_bl = data.isKicked || false;
						lUpdatedData_obj[PlayerInfo.KEY_IS_KICKED] = {value: lIsKicked_bl, time: data.date};
					}
					else
					{
						lUpdatedData_obj[PlayerInfo.KEY_IS_KICKED] = {value: false, time: data.date};
					}

					/*TODO [os]: debug...*/
					// lUpdatedData_obj[PlayerInfo.KEY_IS_CAF_ROOM_MANAGER] = {value: true, time: data.date};
					
					// data.battlegroundInfo.observers = 
					// [
					// 	{
					// 		nickname: this.info.nickname,
					// 		isKicked: false
					// 	},
					// 	{
					// 		nickname: "Killer12",
					// 		isKicked: false
					// 	},
					// 	{
					// 		nickname: "TESTPLAYER2",
					// 		isKicked: true
					// 	},
					// 	{
					// 		nickname: "Killer17",
					// 		isKicked: false
					// 	},
					// 	{
					// 		nickname: "Killer18",
					// 		isKicked: false
					// 	},
					// ]
					/*TODO [os]: ...debug*/

					if (data.battlegroundInfo.observers !== undefined)
					{
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: data.battlegroundInfo.observers, time: data.date};
					}
					else
					{
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: [ { nickname: this.info.nickname, isKicked: false } ], time: data.date};
					}

					if (data.battlegroundInfo.confirmedSeatsId !== undefined)
					{
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS] = {value: data.battlegroundInfo.confirmedSeatsId, time: data.date};
					}

					if (data.battlegroundInfo.buyInConfirmed !== undefined)
					{
						lUpdatedData_obj[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] = {value: !!data.battlegroundInfo.buyInConfirmed, time: data.date};
					}
				}

				if (data.seats !== undefined)
				{
					lSeaters_obj_arr = [];
					for (let i=0; i<data.seats.length; i++)
					{
						lSeaters_obj_arr.push( { seatId: data.seats[i].id, nickname: data.seats[i].nickname} );
					}
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_SERVER_SEATERS] = {value: lSeaters_obj_arr, time: data.date};
				}

				!data.reels && (data.reels = APP.playerController.info.paytable.reels);

				lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL] = {value: data.betLevel};
				lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: data.alreadySitInWin, time: data.date};
				lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: 0, time: data.date, complex: true};
				lUpdatedData_obj[PlayerInfo.KEY_REELS] = {value: data.reels};

				this._fAlreadySitIn_bln = Boolean(data.alreadySitInNumber != -1);
				lUpdatedData_obj[PlayerInfo.KEY_MASTER_SERVER_SEATID] = this._fAlreadySitIn_bln ? {value: data.alreadySitInNumber} : {value: undefined};
				break;

			case SERVER_MESSAGES.FULL_GAME_INFO:
				if (data.friends !== undefined)
				{
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_FRIENDS] = {value: data.friends, time: data.date};
				}
				else
				{	
					// mock feature
					/*const mockRaw = [{"nickname":"zeljko55","online":true},{"nickname":"frontend3","online":false},{"nickname":"vladimirv","online":true},{"nickname":"zeljko33","online":false},{"nickname":"zeljko44","online":true},{"nickname":"frontend1","online":true},{"nickname":"frontend55","online":false},{"nickname":"frontend65","online":true},{"nickname":"frontend71","online":true},{"nickname":"frontend22","online":true},{"nickname":"frontend333","online":false},{"nickname":"frontend44","online":true}];
					//const size = Math.floor(Math.random() * mockRaw.length);
					const size = 7;
					const mock = [];
					console.log("seting new friend list size " + size);
					for(let m = 0; m<size; m++){
						mock[m] = mockRaw[m]
					}

					lUpdatedData_obj[PlayerInfo.KEY_ROOM_FRIENDS] = {value:mock, time: data.date};*/
					
				}
				let lMasterServerSeatId_num = undefined;
				if (data.seats && data.seats.length)
				{
					for (let seat of data.seats)
					{
						if (seat.id === this.info.masterServerSeatId)
						{
							lMasterServerSeatId_num = this.info.masterServerSeatId
						}
					}
				}
				lUpdatedData_obj[PlayerInfo.KEY_MASTER_SERVER_SEATID] = {value: lMasterServerSeatId_num};

				!data.reels && (data.reels = APP.playerController.info.paytable.reels);
				if (!this._isFRBMode)
				{
					lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: 0, time: data.date, complex: true};
					lUpdatedData_obj[PlayerInfo.KEY_BET_LEVEL] = {value: data.betLevel};
				}
				lUpdatedData_obj[PlayerInfo.KEY_REELS] = {value: data.reels};
			if (data.observers !== undefined)
				{
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: data.observers, time: data.date};

					for (let i=0; i<data.observers.length; i++)
					{
						if (data.observers[i].nickname == this.info.nickname)
						{
							lUpdatedData_obj[PlayerInfo.KEY_IS_KICKED] = {value: data.observers[i].isKicked, time: data.date};
						}
					}
				}

				if (data.seats !== undefined)
				{
					lSeaters_obj_arr = [];
					for (let i=0; i<data.seats.length; i++)
					{
						lSeaters_obj_arr.push( { seatId: data.seats[i].id, nickname: data.seats[i].nickname} );
					}
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_SERVER_SEATERS] = {value: lSeaters_obj_arr, time: data.date};
				}
			break;
			case SERVER_MESSAGES.GAME_STATE_CHANGED:
				if (data.state === ROUND_STATE.QUALIFY && APP.isBattlegroundGame)
				{
					lUpdatedData_obj[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] = {value: false, time: data.date};
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS] = {value: [], time: data.date};

					lObserversList_obj_arr = this.info.roomObservers;
					if (!!lObserversList_obj_arr && !!lObserversList_obj_arr.length)
					{
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lObserversList_obj_arr, time: data.date};
					}
				}
			break;

			case SERVER_MESSAGES.SERVER_BATTLEGROUND_BUY_IN_CONFIRMED_SEATS:
				lUpdatedData_obj[PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS] = {value: data.confirmedSeatsId, time: data.date};
			break;

			case SERVER_MESSAGES.ROOM_WAS_OPENED:
				let lCurObserversList_obj_arr = this.info.roomObservers || [];
				let lRecievedObserver_obj = { nickname: data.nickname, isKicked: data.isKicked }
				let lIsRecievedObserverAlreadyExists_bl = false;
				for (let i=0; i<lCurObserversList_obj_arr.length; i++)
				{
					let lCurObserver_obj = lCurObserversList_obj_arr[i];
					if (lCurObserver_obj.nickname == lRecievedObserver_obj.nickname)
					{
						lCurObserver_obj.isKicked = lRecievedObserver_obj.isKicked;
						lIsRecievedObserverAlreadyExists_bl = true;
						break;
					}
				}
				
				if (lIsRecievedObserverAlreadyExists_bl)
				{
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lCurObserversList_obj_arr, time: data.date};
				}
				else
				{
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lCurObserversList_obj_arr.concat(lRecievedObserver_obj), time: data.date};
				}
			break;

			case SERVER_MESSAGES.OBSERVER_REMOVED:
				lObserversList_obj_arr = this._preRemoveObserver(data.nickname);
				lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lObserversList_obj_arr, time: data.date};
			break;

			case SERVER_MESSAGES.BATTLEGROUND_CAF_KICK_REPSONSE:
				if (data.rid === -1)
				{
					lUpdatedData_obj[PlayerInfo.KEY_IS_KICKED] = {value: true, time: data.date};
					break;
				}
				
				lObserversList_obj_arr = this.info.roomObservers || [];
				let lKickedPlayerNickname_str = requestData.nickname;
				for (let i=0; i<lObserversList_obj_arr.length; i++)
				{
					let lCurObserver_obj = lObserversList_obj_arr[i];
					if (lCurObserver_obj.nickname == lKickedPlayerNickname_str)
					{
						lCurObserver_obj.isKicked = true;
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lObserversList_obj_arr, time: data.date};
						break;
					}
				}
			break;

			case SERVER_MESSAGES.OK:
				if (!!requestData && requestData.class == CLIENT_MESSAGES.CONFIRM_BATTLEGROUND_BUY_IN)
				{
					lUpdatedData_obj[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] = {value: true, time: data.date};
				}
			break;

			case SERVER_MESSAGES.RE_BUY_RESPONSE:
				if (APP.isBattlegroundGame)
				{
					lUpdatedData_obj[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] = {value: true, time: data.date};
				}
			case SERVER_MESSAGES.BUY_IN_RESPONSE:
				lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: data.balance, time: data.date};
				break;

			case SERVER_MESSAGES.SIT_IN_RESPONSE:
				lSeaters_obj_arr = this.info.roomServerSeaters || [];
				lUpdatedData_obj[PlayerInfo.KEY_ROOM_SERVER_SEATERS] = {value: lSeaters_obj_arr.concat( [ { seatId: data.id, nickname: data.nickname} ] ), time: data.date};

				if (APP.isBattlegroundGame)
				{
					lRoomConfirmedBuyInBattlgroundSeats_int_arr = this.info.roomConfirmedBuyInBattlgroundSeats || [];
					if (lRoomConfirmedBuyInBattlgroundSeats_int_arr.indexOf(data.id) < 0)
					{
						lRoomConfirmedBuyInBattlgroundSeats_int_arr.push(data.id);
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS] = {value: lRoomConfirmedBuyInBattlgroundSeats_int_arr, time: data.date};
					}
				}
				
				if (data.rid !== -1)
				{
					lUpdatedData_obj[PlayerInfo.KEY_MASTER_SERVER_SEATID] = {value: data.id};
					lUpdatedData_obj[PlayerInfo.KEY_WEAPON_ID] ={value:  data.specialWeaponId, time: data.date};
					lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: data.balance, time: data.date};
					lUpdatedData_obj[PlayerInfo.KEY_REFRESH_BALANCE] = {value: data.showRefreshBalanceButton || false, time: data.date};
					lUpdatedData_obj[PlayerInfo.KEY_WEAPONS] = {value: data.weapons, time: data.date};

					if (!this._fAlreadySitIn_bln)
					{
						lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: 0, time: data.date};
					}

					this._fAlreadySitIn_bln = false;
				}
				break;

			case SERVER_MESSAGES.SIT_OUT_RESPONSE:
				lSeaters_obj_arr = this.info.roomServerSeaters || [];
				for (let i=0; i<lSeaters_obj_arr.length; i++)
				{
					if (lSeaters_obj_arr[i].nickname == data.nickname)
					{
						lSeaters_obj_arr.splice(i, 1);
						lUpdatedData_obj[PlayerInfo.KEY_ROOM_SERVER_SEATERS] = {value: lSeaters_obj_arr, time: data.date};
						break;
					}
				}

				if (APP.isBattlegroundGame)
				{
					lRoomConfirmedBuyInBattlgroundSeats_int_arr = this.info.roomConfirmedBuyInBattlgroundSeats;
					if (lRoomConfirmedBuyInBattlgroundSeats_int_arr && !!lRoomConfirmedBuyInBattlgroundSeats_int_arr.length)
					{
						let lRemIndex_int = lRoomConfirmedBuyInBattlgroundSeats_int_arr.indexOf(data.id);
						if (lRemIndex_int >= 0)
						{
							lRoomConfirmedBuyInBattlgroundSeats_int_arr.splice(lRemIndex_int, 1);
							lUpdatedData_obj[PlayerInfo.KEY_ROOM_CONFIRMED_BUYIN_BATTLGROUND_SEATS] = {value: lRoomConfirmedBuyInBattlgroundSeats_int_arr, time: data.date};
						}
					}
				}

				if (data.rid !== -1 || data.id === this.info.seatId || data.id === this.info.masterServerSeatId)
				{
					lUpdatedData_obj[PlayerInfo.KEY_MASTER_SERVER_SEATID] = {value: undefined};
					APP.logger.i_pushDebug(`GamePlayerController. SitOut response. ${JSON.stringify(event.messagedata)}`);

					if (APP.isBattlegroundGame)
					{
						lUpdatedData_obj[PlayerInfo.IS_BATTLGROUND_BUYIN_CONFIRMED] = {value: false, time: data.date};
					}
				}
				break;

			case SERVER_MESSAGES.MISS:
			case SERVER_MESSAGES.HIT:
				if (APP.currentWindow.player && this._playerSeatId >= 0)
				{
					let lAddWinValue_num = 0;
					if (!isNaN(data.win))
					{
						let killBonusPay = 0;
						if(data.seatId == APP.currentWindow.player.seatId)
						{
							if (data.killBonusPay && data.killBonusPay > 0)
							{
								killBonusPay = data.killBonusPay;
							}
						}

						let gemsPayout = 0;
						if(data.seatId == APP.currentWindow.player.seatId)
						{
							if (data.gemsPayout && data.gemsPayout > 0)
							{
								gemsPayout = data.gemsPayout;
							}
						}
						lAddWinValue_num += killBonusPay + gemsPayout + data.win;
					}

					if (lAddWinValue_num > 0)
					{
						lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: this.info.qualifyWin + lAddWinValue_num, time: data.date, complex: true};
						lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: this.info.unpresentedWin + lAddWinValue_num, time: data.date, complex: true};
					}
				}
				break;

			case SERVER_MESSAGES.ROUND_RESULT:
				lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: data.balance, time: data.date};

				let lAmmoCost_num = 0;
				if (data.unusedBulletsMoney)
				{
					lAmmoCost_num = data.unusedBulletsMoney;
				}

				let lWinAmount_int = Math.floor(data.winAmount) || 0;

				let lUnpresentedWin_num = this.info.unpresentedWin;

				if (!this._isFRBMode)
				{
					lWinAmount_int += lAmmoCost_num;
					if (data.winRebuyAmount && lWinAmount_int >= data.winRebuyAmount)
					{
						lWinAmount_int -= data.winRebuyAmount;
					}

					if (data.weaponSurplus)
					{
						let lWeaponSurplusSum_num = 0;

						for (let weapon of data.weaponSurplus)
						{
							lWeaponSurplusSum_num += weapon.winBonus;
						}

						if (lWeaponSurplusSum_num)
						{
							lUnpresentedWin_num += lWeaponSurplusSum_num;
						}
					}
				}
				else
				{
					lWinAmount_int = this.info.qualifyWin;
				}

				lUpdatedData_obj[PlayerInfo.KEY_QUALIFY_WIN] = {value: lWinAmount_int, time: data.date};
				lUpdatedData_obj[PlayerInfo.KEY_UNPRESENTED_WIN] = {value: lUnpresentedWin_num, time: data.date};
				break;

			case SERVER_MESSAGES.BALANCE_UPDATED:
				lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: data.balance, time: data.date};
				break;

			case SERVER_MESSAGES.WEAPONS:
				if (data.ammoAmount > 0)
				{
					if (this._isBonusMode || APP.tournamentModeController.info.isTournamentMode)
					{
						let lCurBalance_num = this.info.balance;
						let lAmmoCost_num = data.ammoAmount*this.info.currentStake;
						let lNewBalance_num = lCurBalance_num - lAmmoCost_num;
						if (lNewBalance_num >= 0)
						{
							lUpdatedData_obj[PlayerInfo.KEY_BALANCE] = {value: lNewBalance_num, time: data.date};
						}						
					}
				}
				// [Y]TODO ignore so far, check later; currently server sends wrong list
				//console.log('[Y] %c SERVER_MESSAGES.WEAPONS >> ', 'background: #222; color: #bada55', data.weapons);
				// lUpdatedData_obj[PlayerInfo.KEY_WEAPONS] = {value: data.weapons, time: data.date};
				break;
			case SERVER_MESSAGES.WEAPON_PAID_MULTIPLIER_UPDATED:
				lUpdatedData_obj[PlayerInfo.KEY_WEAPON_COST_MULTIPLIERS] = {value: data.weaponPaidMultiplier, time: data.date};
				break;

			case SERVER_MESSAGES.ERROR:
				if (
						data.code === GameWebSocketInteractionController.ERROR_CODES.OBSERVER_DOESNT_EXIST
						&& requestData
					)
				{
					lObserversList_obj_arr = this._preRemoveObserver(requestData.nickname);
					lUpdatedData_obj[PlayerInfo.KEY_ROOM_OBSERVERS] = {value: lObserversList_obj_arr, time: data.date};
				}
				break;
		}

		if (Object.keys(lUpdatedData_obj).length)
		{
			for (let lKey_str in lUpdatedData_obj)
			{
				if (lKey_str === PlayerInfo.KEY_WEAPON_ID)
				{
					this._updatePlayerWeapon(lUpdatedData_obj[lKey_str]);
				}
				else
				{
					this.info.setPlayerInfo(lKey_str, lUpdatedData_obj[lKey_str]);
				}
			}

			this.emit(GamePlayerController.EVENT_ON_PLAYER_INFO_UPDATED, {data: lUpdatedData_obj, class: data.class});
		}
	}

	_preRemoveObserver(aNickName_str)
	{
		// method updates list but does not apply setPlayerInfo for KEY_ROOM_OBSERVERS
		let lObserversList_obj_arr = this.info.roomObservers;
		if (!!lObserversList_obj_arr && !!lObserversList_obj_arr.length)
		{
			for (let i=0; i<lObserversList_obj_arr.length; i++)
			{
				let lCurObserver_obj = lObserversList_obj_arr[i];
				if (lCurObserver_obj.nickname == aNickName_str)
				{
					lObserversList_obj_arr.splice(i, 1);
					break;
				}
			}
		}

		return lObserversList_obj_arr;
	}

	get _isFRBMode()
	{
		return APP.currentWindow.gameFrbController.info.frbMode;
	}

	get _isBonusMode()
	{
		return APP.currentWindow.gameBonusController.info.isActivated;
	}

	destroy()
	{
		APP.off(Game.EVENT_ON_PLAYER_INFO_UPDATED, this._onPlayerInfoUpdated, this);
		APP.webSocketInteractionController.off(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);

		APP.off("onGameStarted", this._onGameStarted, this);

		APP.currentWindow.off(GameScreen.EVENT_ON_WEAPON_UPDATED, this._onPlayerSpotWeaponUpdated, this);
		APP.currentWindow.off(GameScreen.EVENT_ON_FRB_ENDED_COMPLETED, this._onFRBEndedCompleted, this);

		super.destroy()

		this._fAlreadySitIn_bln = undefined;
	}
}

export default GamePlayerController;