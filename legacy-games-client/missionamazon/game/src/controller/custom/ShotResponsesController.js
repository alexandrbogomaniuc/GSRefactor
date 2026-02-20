import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import ShotResponsesInfo from '../../model/custom/ShotResponsesInfo';
import ShotResponseInfo from '../../model/custom/shot/ShotResponseInfo';
import GameWebSocketInteractionController from '../../controller/interaction/server/GameWebSocketInteractionController';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { ENEMIES_EFFECTS_LIST } from '../../../../shared/src/CommonConstants';
import { HIT_RESULT_SINGLE_CASH_ID, HIT_RESULT_TREASURE_ID, HIT_RESULT_SPECIAL_WEAPON_ID, HIT_RESULT_ADDITIONAL_CASH_ID } from '../uis/prizes/PrizesController';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class ShotResponsesController extends SimpleController {

	static get EVENT_ON_SERVER_SHOT_RESPONSE() { return 'EVENT_ON_SERVER_SHOT_RESPONSE' }

	constructor()
	{
		super(new ShotResponsesInfo())
	}

	__init()
	{
		super.__init();

		this._FullGameInfoWait_bl = APP.isBattlegroundMode; //true only in BTG
		this._gameScreen = APP.currentWindow;
		this._startHandleWebSocketMessages();
	}

	_startHandleWebSocketMessages()
	{
		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_HIT_MESSAGE, this._onServerHitMessage, this);
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MISS_MESSAGE, this._onServerMissMessage, this);
		wsInteractionController.once(GameWebSocketInteractionController.EVENT_ON_SERVER_FULL_GAME_INFO_MESSAGE, this._onServerFullGameInfoMessage, this);
	}

	_onServerFullGameInfoMessage(event) 
	{
		this._FullGameInfoWait_bl = false;
		this.info.clear();
	}

	_onServerHitMessage(event) {
		let lShotResponseInfo_sri = new ShotResponseInfo(event.messageData, event.requestData);
		this.info.addShotResponse(lShotResponseInfo_sri);
		if (lShotResponseInfo_sri.lastResult && !this._FullGameInfoWait_bl)
		{
			this._onServerLastResultResponse();
		}
	}

	_onServerMissMessage(event) {
		let lShotResponseInfo_sri = new ShotResponseInfo(event.messageData, event.requestData);
		this.info.addShotResponse(lShotResponseInfo_sri);
		if (lShotResponseInfo_sri.lastResult && !this._FullGameInfoWait_bl)
		{
			this._onServerLastResultResponse();
		}
	}

	_onServerLastResultResponse()
	{
		let lShotResponses_sri_arr = this.info.currentShotResponses;

		let lHitGroup_obj = {};
		let lMissGroup_obj = {};

		for (let lShotResponse_sri of lShotResponses_sri_arr) {
			let lEnemyId_num = lShotResponse_sri.enemyId;
			if (lShotResponse_sri.isHit) {
				if (lHitGroup_obj[lEnemyId_num] == null) {
					lHitGroup_obj[lEnemyId_num] = [];
				}
				lHitGroup_obj[lEnemyId_num].push(lShotResponse_sri);
			}
			else if (lShotResponse_sri.isMiss) {
				if (lMissGroup_obj[lEnemyId_num] == null) {
					lMissGroup_obj[lEnemyId_num] = [];
				}
				lMissGroup_obj[lEnemyId_num].push(lShotResponse_sri);
			}
		}

		let lShotResponsesQueue_gsri_arr = [];

		// GROUP MISS...
		for (let enemyId in lMissGroup_obj)
		{
			let lShotResponses_sri_arr = lMissGroup_obj[enemyId];

			let lGroupedShotResponse_sri = lShotResponses_sri_arr[0];
			//group values
			for (let i=1; i<lShotResponses_sri_arr.length; i++)
			{
				let lAnotherShotResponse_sri = lShotResponses_sri_arr[i];
				lGroupedShotResponse_sri = this._groupResponses(lGroupedShotResponse_sri, lAnotherShotResponse_sri);
			}
			//lShotResponsesQueue_gsri_arr.push(...lShotResponses_sri_arr);
			if (lGroupedShotResponse_sri)
			{
				lShotResponsesQueue_gsri_arr.push(lGroupedShotResponse_sri);
			}
		}
		// ...GROUP MISS

		// GROUP HIT...
		for (let enemyId in lHitGroup_obj)
		{
			let lShotResponses_sri_arr = lHitGroup_obj[enemyId];

			//wins with chMult should be animated separatedly...
			for (let i=0; i<lShotResponses_sri_arr.length; i++)
			{
				let lShotResponse_sri = lShotResponses_sri_arr[i];
				if (lShotResponse_sri.chMult > 1)
				{
					lShotResponsesQueue_gsri_arr.push(lShotResponse_sri);
					lShotResponses_sri_arr.splice(i, 1);
					i--;
				}
			}
			//...wins with chMult should be animated separatedly

			let lGroupedShotResponse_sri = lShotResponses_sri_arr[0];
			// group values
			for (let i=1; i<lShotResponses_sri_arr.length; i++)
			{
				let lAnotherShotResponse_sri = lShotResponses_sri_arr[i];
				lGroupedShotResponse_sri = this._groupResponses(lGroupedShotResponse_sri, lAnotherShotResponse_sri);
			}
			//lShotResponsesQueue_gsri_arr.push(...lShotResponses_sri_arr);
			if (lGroupedShotResponse_sri)
			{
				lShotResponsesQueue_gsri_arr.push(lGroupedShotResponse_sri);
			}
		}
		// ...GROUP HIT

		for (let i=0; i<lShotResponsesQueue_gsri_arr.length; i++)
		{
			let lShotResponse_sri = lShotResponsesQueue_gsri_arr[i];
			lShotResponse_sri.lastResult = (i === lShotResponsesQueue_gsri_arr.length - 1);
			this.emit(ShotResponsesController.EVENT_ON_SERVER_SHOT_RESPONSE, {info: lShotResponse_sri});
			lShotResponse_sri.clear();
		}

		this.info.clear();
	}

	_groupResponses(aBaseResponse_sri, aAddResponse_sri)
	{
		let lBaseResponse_sri = aBaseResponse_sri;
		let lAddResponse_sri = aAddResponse_sri;

		//add win
		if (lAddResponse_sri.win)
		{
			lBaseResponse_sri.win = +lBaseResponse_sri.win + lAddResponse_sri.win;
		}

		//add damage
		if (lAddResponse_sri.damage)
		{
			lBaseResponse_sri.damage = +lBaseResponse_sri.damage + lAddResponse_sri.damage;
		}

		//add score
		if (lAddResponse_sri.score)
		{
			lBaseResponse_sri.score = +lBaseResponse_sri.score + lAddResponse_sri.score;
		}

		//add killBonusPay
		if (lAddResponse_sri.killBonusPay)
		{
			lBaseResponse_sri.killBonusPay = +lBaseResponse_sri.killBonusPay + lAddResponse_sri.killBonusPay;
		}

		//add prizes
		if (lAddResponse_sri.awardedPrizes)
		{
			if (lBaseResponse_sri.data.awardedPrizes == null)
			{
				lBaseResponse_sri.data.awardedPrizes = [];
			}
			lBaseResponse_sri.data.awardedPrizes.push(...lAddResponse_sri.awardedPrizes);
		}

		//add weapon
		lBaseResponse_sri = this._groupWeapons(lBaseResponse_sri, lAddResponse_sri);

		//update hitResultBySeats
		lBaseResponse_sri = this._updateHitResultBySeats(lBaseResponse_sri, lAddResponse_sri);

		//update killed=true|false?
		lBaseResponse_sri.killed = lBaseResponse_sri.killed || lAddResponse_sri.killed;

		//update enemiesInstantKilled if any
		if (lBaseResponse_sri.isHit)
		{
			if (lAddResponse_sri.enemiesInstantKilled && !Utils.isEmptyObject(lAddResponse_sri.enemiesInstantKilled))
			{
				if (lBaseResponse_sri.enemiesInstantKilled && !Utils.isEmptyObject(lBaseResponse_sri.enemiesInstantKilled))
				{
					throw new Error('We need to implement the merge of 2 enemiesInstantKilled objects'); // for future
				}
				lBaseResponse_sri.enemiesInstantKilled =  lAddResponse_sri.enemiesInstantKilled;
			}
		}

		return lBaseResponse_sri;
	}

	_groupWeapons(aBaseResponse_sri, aAddResponse_sri)
	{
		let lBaseResponse_sri = aBaseResponse_sri;
		let lAddResponse_sri = aAddResponse_sri;
		let lAddAwardedWeapons_arr = lAddResponse_sri.data.awardedWeapons;

		if (lAddAwardedWeapons_arr && lAddAwardedWeapons_arr.length > 0)
		{
			if (lBaseResponse_sri.data.awardedWeapons == null) {
				lBaseResponse_sri.data.awardedWeapons = [];
			}

			//search
			for (let i=0; i < lAddAwardedWeapons_arr.length; i++)
			{
				let lAwardedWeapon_obj = lAddAwardedWeapons_arr[i];
				let lAwardedWeaponId_int = lAwardedWeapon_obj.id;

				let lAddingWeaponExist_bl = false;
				for (let j=0; j < lBaseResponse_sri.data.awardedWeapons.length; j++)
				{
					if (lAwardedWeaponId_int == lBaseResponse_sri.data.awardedWeapons[j].id)
					{
						lBaseResponse_sri.data.awardedWeapons[j].shots += lAddAwardedWeapons_arr[i].shots;
						lAddingWeaponExist_bl = true;
					}
				}

				if (!lAddingWeaponExist_bl)
				{
					lBaseResponse_sri.data.awardedWeapons.push({id: lAwardedWeaponId_int, shots: lAddAwardedWeapons_arr[i].shots});
				}
			}
		}

		return lBaseResponse_sri;
	}

	_updateHitResultBySeats(aBaseResponse_sri, aAddResponse_sri)
	{
		let lBaseResponse_sri = aBaseResponse_sri;
		let lAddHitResultBySeats_obj = aAddResponse_sri.hitResultBySeats;

		for (let seatId in lAddHitResultBySeats_obj)
		{
			let lExistingHitResult_obj_arr = lBaseResponse_sri.hitResultBySeats[seatId];
			let lAddHitResultBySeats_obj_arr = lAddHitResultBySeats_obj[seatId];
			if (!lExistingHitResult_obj_arr)
			{
				lBaseResponse_sri.hitResultBySeats[seatId] = lAddHitResultBySeats_obj_arr;
			}
			else
			{

				for (let i=0; i<lAddHitResultBySeats_obj_arr.length; i++)
				{
					let lAddHitResult_obj = lAddHitResultBySeats_obj_arr[i];
					for (let j=0; j<lExistingHitResult_obj_arr.length; j++)
					{
						let lExistingHitResult_obj = lExistingHitResult_obj_arr[j];
						if (lExistingHitResult_obj.id == lAddHitResult_obj.id)
						{
							switch(+lExistingHitResult_obj.id)
							{
								case HIT_RESULT_SINGLE_CASH_ID:
									lExistingHitResult_obj.value = Number(lExistingHitResult_obj.value) + Number(lAddHitResult_obj.value);
									break;
								case HIT_RESULT_TREASURE_ID:
									lExistingHitResult_obj_arr.push(lAddHitResult_obj);
									break;
								case HIT_RESULT_SPECIAL_WEAPON_ID:
									let lFlag_bl = false;
									for (let obj of lExistingHitResult_obj_arr)
									{
										if (+obj.id === HIT_RESULT_SPECIAL_WEAPON_ID && obj.value === lAddHitResult_obj.value)
										{
											lFlag_bl = true;
											break;
										}
									}
									if (!lFlag_bl)
									{
										lExistingHitResult_obj_arr.push(lAddHitResult_obj);
									}
									break;
								case HIT_RESULT_ADDITIONAL_CASH_ID:
									lExistingHitResult_obj.value = Number(lExistingHitResult_obj.value) + Number(lAddHitResult_obj.value);
									break;
							}
							j = lExistingHitResult_obj_arr.length + 1;
						}
						else if (j === lExistingHitResult_obj_arr.length - 1)
						{
							lExistingHitResult_obj_arr.push(lAddHitResult_obj);
							break;
						}
					}
				}
			}
		}
		return lBaseResponse_sri;
	}
}

export default ShotResponsesController