import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import TreasuresInfo from '../../../model/uis/treasures/TreasuresInfo';
import GameWebSocketInteractionController from '../../../controller/interaction/server/GameWebSocketInteractionController';
import { SERVER_MESSAGES } from '../../../model/interaction/server/GameWebSocketInteractionInfo';
import GameScreen from '../../../main/GameScreen';
import GameField from '../../../main/GameField';
import TreasuresView from '../../../view/uis/treasures/TreasuresView';
import { BTG_TOTAL_QUEST_COMPLETE_GEMS, TOTAL_QUEST_COMPLETE_GEMS } from '../../../../../shared/src/CommonConstants';
import WinTierUtil from '../../../main/WinTierUtil';
import TreasuresSidebarController from '../../../controller/uis/treasures/TreasuresSidebarController';

const QUEST_AWARD_ID_PREFIX = "q_";

class TreasuresController extends SimpleUIController
{
	static get TREASURE_AMOUNT_UPDATE()					{ return "TREASURE_AMOUNT_UPDATE"; }
	static get TREASURES_PRICES_UPDATE()				{ return "TREASURES_PRICES_UPDATE"; }

	static get EVENT_ON_QUEST_WIN_REGISTER()			{ return "onQuestWinRegister"; }
	static get EVENT_ON_QUEST_WIN_REQUIRED()			{ return "onQuestWinRequired"; }

	static get TREASURE_AWARD_ANIMATION_COMPLETED()		{ return TreasuresView.TREASURE_AWARD_ANIMATION_COMPLETED; }
	static get EVENT_ON_TREASURE_GEM_DROP()				{ return TreasuresView.EVENT_ON_TREASURE_GEM_DROP; }

	getTreasureAmount()
	{
		return this.info.currentGemsAmount.slice(0);
	}

	constructor()
	{
		super(new TreasuresInfo(), new TreasuresView());

		this._fWebSocketInteractionController_wsic = null;
		this._fPandingAward_obj_arr = [];
		this._fDelayedUpdate_obj = null;
		this._fPendingAward_int = 0;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fWebSocketInteractionController_wsic = APP.webSocketInteractionController;
		this._fWebSocketInteractionController_wsic.on(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
		this._fWebSocketInteractionController_wsic.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		this._fGameScreen_spr = APP.gameScreen;
		this._fGameScreen_spr.gameField.on('showEnemyHit', this._onEnemyImpacted, this);
		this._fGameScreen_spr.on(GameScreen.EVENT_ON_CLOSE_ROOM, this._onCloseRoom, this);
		this._fGameScreen_spr.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		this._fGameScreen_spr.on(GameScreen.EVENT_ON_HIT_AWARD_EXPECTED, this._onHitAwardExpected, this);
		this._fGameScreen_spr.once(GameScreen.EVENT_ON_READY, this._onGameScreenReady, this);

		//DEBUG...
		//window.addEventListener("keydown", this.keyDownHandler.bind(this), false);
		//...DEBUG
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(TreasuresView.TREASURE_AWARD_ANIMATION_COMPLETED, this._onTreasureAwardAnimationCompleted, this);
		this.view.on(TreasuresView.EVENT_ON_TREASURE_GEM_DROP, this.emit, this);
	}

	_onGameScreenReady()
	{
		this._fGameScreen_spr.gameField.once(GameField.EVENT_ON_GAME_FIELD_SCREEN_CREATED, this._onGameFieldScreenCreated, this);
	}

	_onGameFieldScreenCreated()
	{
		APP.currentWindow.gameField.treasuresSidebarController.on(TreasuresSidebarController.EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH, this._onQuestCompleteAnimationFinish, this);
	}

	_onQuestCompleteAnimationFinish(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.gemId;
		const lHitData_obj = this._getPandingHitDataById(lGemId_int);
		lHitData_obj && this._onQuestAnimationCompleted(lHitData_obj, lGemId_int);
		this._fPendingAward_int--;
		if (this._fPendingAward_int <= 0 && this._fDelayedUpdate_obj && !APP.isBattlegroundGame)
		{
			this._updateAmount(this._fDelayedUpdate_obj);
			this._fDelayedUpdate_obj = null;
			this._fPendingAward_int = 0;
		}
	}

	_getPandingHitDataById(lGemId_int)
	{
		const lIndex_int = this._fPandingAward_obj_arr.findIndex(l_obj => l_obj.gems[lGemId_int] != 0);
		if (lIndex_int !== -1)
		{
			return this._fPandingAward_obj_arr.splice(lIndex_int, 1)[0];
		}
	}

	_onTreasureAwardAnimationCompleted(aEvent_obj)
	{
		this.emit(TreasuresController.TREASURE_AWARD_ANIMATION_COMPLETED, { id: aEvent_obj.id })
	}

	//DEBUG...
	keyDownHandler(keyCode)
	{
		if (keyCode.keyCode == 107)
		{
			const msg = {
				data: {
					"seatId": 1,
					"damage": 0.0,
					"win": 100.0,
					"awardedWeaponId": -1,
					"usedSpecialWeapon": 4,
					"remainingSWShots": 1,
					"score": 0.0,
					"enemy": {
						"id": 214758,
						"typeId": 11,
						"speed": 4.0,
						"awardedPrizes": "",
						"awardedSum": 0.0,
						"energy": 1.0,
						"fullEnergy": 1.0,
						"skin": 0,
						"parentEnemyId": -1,
						"members": [],
						"swarmId": 706,
						"swarmType": 7
					},
					"hit": true,
					"awardedWeaponShots": 0,
					"killed": true,
					"lastResult": false,
					"multiplierPay": 0,
					"killBonusPay": 0.0,
					"serverAmmo": 303,
					"bossNumberShots": 0,
					"currentWin": 100.0,
					"hvEnemyId": -1,
					"x": 553.90137,
					"y": 265.9405,
					"mineId": "",
					"newFreeShots": 0,
					"newFreeShotsSeatId": 1,
					"hitResultBySeats": {
						"1": [
							{
								"id": 0,
								"value": "100.0"
							}
						]
					},
					"instanceKill": false,
					"chMult": 1,
					"awardedWeapons": [],
					"needExplode": false,
					"isExplode": false,
					"gems": [
						1,
						0,
						0,
						0
					],
					"enemyId": 214758,
					"shotEnemyId": 214703,
					"betLevel": 1,
					"isPaidSpecialShot": false,
					"moneyWheelWin": 0.0,
					"enemiesInstantKilled": {},
					"enemiesWithUpdatedMode": [],
					"bulletId": "",
					"nextBetLevel": -1,
					"date": 1638509043158,
					"rid": 7,
					"gemsPayout": 100,
					"class": "Hit"
				}
			};
			this._handleMainPlayerEnemyImpacted(msg);
		}
	}
	//...DEBUG

	_onServerMessage(event)
	{
		let data = event.messageData;
		switch (data.class)
		{
			case SERVER_MESSAGES.GET_ROOM_INFO_RESPONSE:
				this._updateAmount(data.seatGems);
				if (APP.isBattlegroundGame && data.gemPrizes)
				{
					this._updatePrices(data.gemPrizes);
				}
				break;
			case SERVER_MESSAGES.FULL_GAME_INFO:
				this._updateAmount(data.seatGems);
				if (APP.isBattlegroundGame && data.gemPrizes)
				{
					this._updatePrices(data.gemPrizes);
				}
				break;
		}
	}

	_updateAmount(aSeatGems_obj)
	{
		if (this._fPendingAward_int > 0)
		{
			this._fDelayedUpdate_obj = aSeatGems_obj;
		}

		for (let gemId in aSeatGems_obj)
		{
			this.info.currentGemsAmount[gemId] = aSeatGems_obj[gemId];
		}

		if (Object.keys(aSeatGems_obj).length == 0)
		{
			this.info.clear();
		}

		this._fPandingAward_obj_arr = [];

		this.emit(TreasuresController.TREASURE_AMOUNT_UPDATE, { treasureAmount: this.info.currentGemsAmount.slice(0) });
	}

	_updatePrices(aEvent_obj)
	{
		this.emit(TreasuresController.TREASURES_PRICES_UPDATE, { prices: aEvent_obj });
	}

	_onHitAwardExpected(aEvent_obj)
	{
		if (aEvent_obj.hitData.seatId !== aEvent_obj.masterSeatId) return;

		if (!aEvent_obj.hitData.gemsPayout) return;

		const lHitData_obj = aEvent_obj.hitData;
		this._fPandingAward_obj_arr.push(lHitData_obj);
		this._fPendingAward_int++;

		this.emit(TreasuresController.EVENT_ON_QUEST_WIN_REGISTER, { hitData: lHitData_obj, awardedWin: lHitData_obj.gemsPayout });
	}

	_getQuestPayoutById(lGemId_int)
	{
		switch (lGemId_int)
		{
			case 0:
				return { x: 820, y: 270 - 48 - 48 };
			case 1:
				return { x: 820, y: 270 - 48 };
			case 2:
				return { x: 820, y: 270};
			case 3:
				return { x: 820, y: 270 + 48 };
			case 4:
				return { x: 820, y: 270 + 48 + 48 };
		}

		return { x: 0, y: 0 };
	}

	_onQuestAnimationCompleted(hitData, lGemId_int)
	{
		let lAwardingController_ac = APP.currentWindow.awardingController;
		let lContainer_sprt = lAwardingController_ac.awardingContainerInfo.container;
		let lSpot = APP.gameScreen.gameField.spot;
		let lHitData_obj = hitData;
		if (lAwardingController_ac && lSpot && lContainer_sprt && lHitData_obj)
		{
			let lCurrentStake_num = APP.playerController.info.currentStake;
			let lStartPos_obj = this._getQuestPayoutById(lGemId_int);
			let lFinalPos_obj = null;

			if (APP.isBattlegroundGame)
			{
				lFinalPos_obj = lSpot.scoreFieldPosition;
				lFinalPos_obj = lSpot.localToGlobal(lFinalPos_obj.x, lFinalPos_obj.y);
			}
			else
			{
				lFinalPos_obj = lSpot.spotVisualCenterPoint;
			}

			let hitEnemyIdSuff = "";
			if (lHitData_obj.enemyId !== undefined)
			{
				hitEnemyIdSuff = lHitData_obj.enemyId;
			}

			const awardId = QUEST_AWARD_ID_PREFIX + lHitData_obj.rid + hitEnemyIdSuff;

			let data = [
				lHitData_obj.gemsPayout,
				lCurrentStake_num,
				{
					start: lStartPos_obj,
					startOffset: { x: 0, y: 0 },
					winPoint: lFinalPos_obj,
					specifiedWinSoundTier: WinTierUtil.WIN_TIERS.TIER_BIG,
					isQualifyWinDevalued: false,
					awardId: awardId,
					seatId: APP.playerController.info.seatId,
					isQuestCompleateAward: true
				}
			];

			this.emit(TreasuresController.EVENT_ON_QUEST_WIN_REQUIRED, { data: data });
		}
	}

	_onEnemyImpacted(aEvent_obj)
	{
		const lShotResponseInfo_obj = aEvent_obj.data;
		if (lShotResponseInfo_obj.class != "Hit")
		{
			return;
		}

		if (lShotResponseInfo_obj.rid != -1)
		{
			this._handleMainPlayerEnemyImpacted(aEvent_obj);
		}
	}

	_handleMainPlayerEnemyImpacted(aHitData_obj)
	{
		const lShotResponseInfo_obj = aHitData_obj.data;
		const lGemsInfo_arr = lShotResponseInfo_obj.gems;
		
		if (APP.isBattlegroundGame)
		{
			lGemsInfo_arr.reverse();
		}

		let lTotalNeededGemsAmount_int = APP.isBattlegroundGame ? BTG_TOTAL_QUEST_COMPLETE_GEMS : TOTAL_QUEST_COMPLETE_GEMS;
		lGemsInfo_arr && lGemsInfo_arr.forEach((aGemAmount_num, aGemId_num) =>
		{
			for (let i = 0; i < aGemAmount_num; i++)
			{
				this.info.increaseGemsAmountById(aGemId_num);
				if (this.info.getGemAmountById(aGemId_num) == lTotalNeededGemsAmount_int)
				{
					this.info.clearGemAmountById(aGemId_num);
				}
				this._showTreasureById(aGemId_num, aHitData_obj);
			}
		});
	}

	_showTreasureById(aGemId_num, aHitData_obj)
	{
		this.view.addToContainerIfRequired(this._treasuresContainer);
		const lFinalPosition_obj = this._generateFinalPosition(aGemId_num);
		const lStartPosition_obj = this._correctStartPositionIfRequired(this._generateStartPosition(aHitData_obj));
		this.view.showTreasureById(aGemId_num, lStartPosition_obj, lFinalPosition_obj);
	}

	_generateStartPosition(aHitData_obj)
	{
		return this._fGameScreen_spr.gameField.getEnemyPosition(aHitData_obj.data.enemyId) || { x: 0, y: 0 };
	}

	_correctStartPositionIfRequired(aStartPosition_obj = { x: 0, y: 0 })
	{
		if (aStartPosition_obj.x < 50)
		{
			aStartPosition_obj.x = 50;
		}
		if (aStartPosition_obj.x > 910)
		{
			aStartPosition_obj.x = 910;
		}
		if (aStartPosition_obj.y < 50)
		{
			aStartPosition_obj.y = 50;
		}
		if (aStartPosition_obj.y > 490)
		{
			aStartPosition_obj.y = 490;
		}

		return aStartPosition_obj;
	}

	_generateFinalPosition(aGemId_num)
	{
		return this._fGameScreen_spr.gameField.treasuresSidebarController.getTreasureLandingGlobalPosition(aGemId_num);
	}

	get _treasuresContainer()
	{
		return APP.currentWindow.gameField.treasuresAnimationContainer;
	}

	_onCloseRoom()
	{
		this._clearAllTreasures();
	}

	_onGameFieldCleared()
	{
		this._clearAllTreasures();
	}

	_onGameServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._clearAllTreasures();
	}

	_clearAllTreasures()
	{
		this.view.clearAllTreasures();
		this._fPandingAward_obj_arr = [];
	}

	destroy()
	{
		this._clearAllTreasures();

		if (this._fGameScreen_spr)
		{
			this._fGameScreen_spr.off(GameScreen.EVENT_ON_CLOSE_ROOM, this._onCloseRoom, this);
			this._fGameScreen_spr.off(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		}

		if (this._fWebSocketInteractionController_wsic)
		{
			this._fWebSocketInteractionController_wsic.off(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
			this._fWebSocketInteractionController_wsic.off(GameWebSocketInteractionController.EVENT_ON_SERVER_MESSAGE, this._onServerMessage, this);
			this._fWebSocketInteractionController_wsic = null;
		}

		this._fPandingAward_obj_arr = [];

		super.destroy();
	}
}

export default TreasuresController