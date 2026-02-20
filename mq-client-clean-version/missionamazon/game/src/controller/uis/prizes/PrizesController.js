import SimpleController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import ShotResponsesController from '../../../controller/custom/ShotResponsesController';
import { WEAPONS } from '../../../../../shared/src/CommonConstants';

export const HIT_RESULT_SINGLE_CASH_ID 	= 0;
export const HIT_RESULT_TREASURE_ID		= 1;
export const HIT_RESULT_SPECIAL_WEAPON_ID 	= 2;
export const HIT_RESULT_ADDITIONAL_CASH_ID = 3; // in Pirates it is Final Win for Boss

class PrizesController extends SimpleController {

	static get i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES() 				{ return 'i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES';}
	static get i_EVENT_ON_TIME_TO_SHOW_MASTER_SCORE_PRIZE() 		{ return 'i_EVENT_ON_TIME_TO_SHOW_MASTER_SCORE_PRIZE';}
	static get i_EVENT_ON_TIME_TO_SHOW_TREASURE_PRIZE() 			{ return 'EVENT_ON_TIME_TO_SHOW_TREASURE_PRIZE';}
	static get i_EVENT_ON_TIME_TO_SHOW_SPECIAL_WEAPON_PRIZE() 		{ return 'EVENT_ON_TIME_TO_SHOW_SPECIAL_WEAPON_PRIZE';}

	static isAnyCashAwardAnimationRequired(aHitData_obj)
	{
		if (!!aHitData_obj.hitResultBySeats && !Utils.isEmptyObject(aHitData_obj.hitResultBySeats))
		{
			for (let lSeatId_int in aHitData_obj.hitResultBySeats)
			{
				let lPrizesForSeat_obj_arr = aHitData_obj.hitResultBySeats[lSeatId_int];
				let lWinCash_num = 0;

				for (let lPrize_obj of lPrizesForSeat_obj_arr)
				{
					switch (lPrize_obj.id)
					{
						case HIT_RESULT_SINGLE_CASH_ID:
						case HIT_RESULT_ADDITIONAL_CASH_ID:
							lWinCash_num += +lPrize_obj.value;
							if (lWinCash_num > 0) return true;
							break;
					}
				}
			}
		}
		return false;
	}

	static isAnyTreasureAwardAnimationRequired(aHitData_obj)
	{
		if (!!aHitData_obj.hitResultBySeats && !Utils.isEmptyObject(aHitData_obj.hitResultBySeats))
		{
			for (let lSeatId_int in aHitData_obj.hitResultBySeats)
			{
				let lPrizesForSeat_obj_arr = aHitData_obj.hitResultBySeats[lSeatId_int];
				for (let lPrize_obj of lPrizesForSeat_obj_arr)
				{
					if (lPrize_obj.id === HIT_RESULT_TREASURE_ID)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	static isAnySpecialWeaponAwardAnimationRequired(aHitData_obj)
	{
		if (!!aHitData_obj.hitResultBySeats && !Utils.isEmptyObject(aHitData_obj.hitResultBySeats))
		{
			for (let lSeatId_int in aHitData_obj.hitResultBySeats)
			{
				let lPrizesForSeat_obj_arr = aHitData_obj.hitResultBySeats[lSeatId_int];
				for (let lPrize_obj of lPrizesForSeat_obj_arr)
				{
					if (lPrize_obj.id === HIT_RESULT_SPECIAL_WEAPON_ID)
					{
						return true;
					}
				}
			}
		}
		return false;
	}

	constructor()
	{
		super();
		this._fPendingHitDataBySeats_obj = {};
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._gameScreen = APP.gameScreen;
		this._gameScreen.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);
		this._gameScreen.on(GameScreen.EVENT_ON_PLAYER_REMOVED, this._onPlayerSitOut, this);
		this._gameScreen.on(GameScreen.EVENT_ON_ROOM_FIELD_CLEARED, this._onRoomFieldCleared, this);

		let shotResponsesController = this._gameScreen.shotResponsesController;
		shotResponsesController.on(ShotResponsesController.EVENT_ON_SERVER_SHOT_RESPONSE, this._onServerShotResponse, this);
	}

	_onGameScreenReady()
	{
		this._gameField = this._gameScreen.gameField;
		this._gameField.on(GameField.EVENT_TIME_TO_SHOW_PRIZES, this._onTimeToShowPrizes, this);
	}

	_onRoomFieldCleared()
	{
		//clear pending hit data
		this._fPendingHitDataBySeats_obj = {};
	}

	_onPlayerSitOut(event)
	{
		let lSeatId_int = event.seatId;
		delete this._fPendingHitDataBySeats_obj[lSeatId_int];
	}

	_onServerShotResponse(event)
	{
		let lShotResponseInfo_sri = event.info;
		if (lShotResponseInfo_sri.isHit)
		{
			this._onServerHitMessage({messageData: lShotResponseInfo_sri.data});
		}
	}

	_onServerHitMessage(event)
	{
		let hitData = event.messageData;
		let id = hitData.id;
		for (let lSeatId_str in hitData.hitResultBySeats)
		{
			let lSeatId_int = +lSeatId_str;
			if (lSeatId_int == APP.playerController.info.seatId) continue; //for co-players only
			if (!this._fPendingHitDataBySeats_obj[lSeatId_int])
			{
				this._fPendingHitDataBySeats_obj[lSeatId_int] = {};
			}
			this._fPendingHitDataBySeats_obj[lSeatId_int][id] = hitData; //assuming [id] is unique and can be used as a unique identifier for hitData
		}
	}

	_onTimeToShowPrizes(event)
	{
		let prizesData 				= event.prizesData;
		let hitData 				= prizesData.hitData;
		let startPosition 			= prizesData.prizePosition;
		let isBoss 					= prizesData.isBoss;

		let lCashWins_obj_arr = [];
		let lMasterTotalCashWins_num = 0;

		let cashCounter = 0;
		let angles = [180, -60, 120, 60, -120];

		if (hitData.awardedWeapons && !!hitData.awardedWeapons.length)
		{
			for (var i = 0; i < hitData.awardedWeapons.length; i++)
			{
				hitData.hitResultBySeats[hitData.seatId].push({id: HIT_RESULT_SPECIAL_WEAPON_ID, value: hitData.awardedWeapons[i].id});
			}
		}

		for (let lSeatId_int in hitData.hitResultBySeats)
		{
			let lPrizesForSeat_obj_arr = hitData.hitResultBySeats[lSeatId_int];
			let lTreasureTypes_str_arr = [];
			let lSpecialWeaponIds_int_arr = [];
			let lIsMasterSeat_bl = (+lSeatId_int === APP.playerController.info.seatId)

			if (!lIsMasterSeat_bl && !hitData.ignorePending)
			{
				//check if player already sit out
				if (!this._fPendingHitDataBySeats_obj[lSeatId_int] || !this._fPendingHitDataBySeats_obj[lSeatId_int][hitData.id])
				{
					//console.log("[Y] player " + lSeatId_int + " already sit out ");
					continue;
				}
			}

			if (this._fPendingHitDataBySeats_obj[lSeatId_int])
			{
				// can be deleted as it's going to be proceed for animation; meanwhile appropriate animation controllers are listening for Player sit out themselves
				delete this._fPendingHitDataBySeats_obj[lSeatId_int][hitData.id];
			}

			for (let lPrize_obj of lPrizesForSeat_obj_arr)
			{
				switch (lPrize_obj.id)
				{
					case HIT_RESULT_SINGLE_CASH_ID:
					case HIT_RESULT_ADDITIONAL_CASH_ID:
						let lWinCash_num = +lPrize_obj.value;
						if (isBoss && hitData.killed && APP.isBattlegroundGame)
						{
							lWinCash_num -= hitData.killBonusPay;
						}

						if (hitData.gemsPayout)
						{
							lWinCash_num -= hitData.gemsPayout;
							if(!lIsMasterSeat_bl)
							{
								let awardStartPosition = new PIXI.Point(startPosition.x, startPosition.y);

								let angle = lIsMasterSeat_bl ? 0 : angles[cashCounter++ % angles.length];
								awardStartPosition.x -= Math.cos(-Utils.gradToRad(angle) + Math.PI/2)*50;
								awardStartPosition.y -= Math.sin(-Utils.gradToRad(angle) + Math.PI/2)*50;

								let lHitDataClone_obj = Object.assign({}, hitData);
								let lCashWinParams_obj = {
									seatId: +lSeatId_int,
									winCash: hitData.gemsPayout,
									hitData: lHitDataClone_obj,
									awardStartPosition: awardStartPosition,
									isBoss: false,
									isBossMasterFinalWin: false
								};
								lHitDataClone_obj.score = 0;

								lCashWinParams_obj.startOffset = {x: 0, y: -50};
								lCashWins_obj_arr.push(lCashWinParams_obj);
							}
						}

						if (lWinCash_num > 0)
						{
							let awardStartPosition = new PIXI.Point(startPosition.x, startPosition.y);

							let angle = lIsMasterSeat_bl ? 0 : angles[cashCounter++ % angles.length];
							awardStartPosition.x -= Math.cos(-Utils.gradToRad(angle) + Math.PI/2)*50;
							awardStartPosition.y -= Math.sin(-Utils.gradToRad(angle) + Math.PI/2)*50;

							let lHitDataClone_obj = Object.assign({}, hitData);
							let lCashWinParams_obj = {
								seatId: +lSeatId_int,
								winCash: lWinCash_num,
								hitData: lHitDataClone_obj,
								awardStartPosition: awardStartPosition,
								isBoss: isBoss,
								isBossMasterFinalWin: (isBoss && hitData.killed)
							};
							if (lIsMasterSeat_bl)
							{
								if (lMasterTotalCashWins_num > 0)
								{
									// to avoid multiple score award animations for a sigle hitData, due to one hitData can contain several cash wins for master player
									lHitDataClone_obj.score = 0;
								}
								lMasterTotalCashWins_num += lWinCash_num;

								lCashWins_obj_arr.unshift(lCashWinParams_obj)
							}
							else
							{
								lHitDataClone_obj.score = 0;

								lCashWinParams_obj.startOffset = {x: 0, y: -50};
								lCashWins_obj_arr.push(lCashWinParams_obj);
							}
						}
						break;
					case HIT_RESULT_TREASURE_ID:
						lTreasureTypes_str_arr.push(lPrize_obj.value);
						break;
					case HIT_RESULT_SPECIAL_WEAPON_ID:
						let lSWPrizeValue = lPrize_obj.value;
						if (isNaN(lSWPrizeValue) || lSWPrizeValue === null || (+lSWPrizeValue == WEAPONS.HIGH_LEVEL))
						{
						}
						else
						{
							lSpecialWeaponIds_int_arr.push(+lSWPrizeValue);
						}
						break;
				}
			}

			//SPECIAL WEAPONS...
			if (!lIsMasterSeat_bl && lSpecialWeaponIds_int_arr.length > 0)
			{
				let weaponsFlyPositions = [];

				switch (lSpecialWeaponIds_int_arr.length)
				{
					case 1:
						weaponsFlyPositions = [
							{x: 0, y: -70}
						];
						break;
					case 2:
						weaponsFlyPositions = [
							{x:  60, y: -30},
							{x: -60, y: -30}
						];
						break;
					case 3:
						weaponsFlyPositions = [
							{x:  60, y:  30},
							{x: -60, y:  30},
							{x:   0, y: -70}
						];
						break;
				}

				for (let i = 0; i < lSpecialWeaponIds_int_arr.length; i++)
				{
					let position = {};
					position.x = startPosition.x + weaponsFlyPositions[i].x;
					position.y = startPosition.y + weaponsFlyPositions[i].y;

					this.emit(PrizesController.i_EVENT_ON_TIME_TO_SHOW_SPECIAL_WEAPON_PRIZE, {specialWeaponId: lSpecialWeaponIds_int_arr[i], seatId: +lSeatId_int, startPosition: position});
				}
			}
			//...SPECIAL WEAPONS
		}

		if (lCashWins_obj_arr.length > 0)
		{
			this.emit(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, {data: lCashWins_obj_arr});
		}
	}
}

export default PrizesController;