import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import TreasuresSidebarInfo from '../../../model/uis/treasures/TreasuresSidebarInfo';
import TreasuresController from './TreasuresController';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { BTG_TOTAL_QUEST_COMPLETE_GEMS, TOTAL_QUEST_COMPLETE_GEMS } from '../../../../../shared/src/CommonConstants';
import { TreasuresSidebarView } from '../../../view/uis/treasures/TreasuresSidebarView';
import GameWebSocketInteractionController from '../../../controller/interaction/server/GameWebSocketInteractionController';
import GameScreen from '../../../main/GameScreen';
import { GameStateInfo, ROUND_STATE } from '../../../model/state/GameStateInfo';

class TreasuresSidebarController extends SimpleUIController
{
	static get EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH() { return "EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH"; }
	static get EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH_AFTER_ROUND_END() { return "EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH_AFTER_ROUND_END"; }

	i_hideView()
	{
		this.view.visible = false;
	}

	i_showView()
	{
		this.view.visible = true;
		this._updateView();
	}

	getTreasureLandingGlobalPosition(aGemId_num)
	{
		let l_tsv = this.view;

		return l_tsv.getTreasureLandingGlobalPosition(aGemId_num);
	}

	constructor(aTresiresSidebarView_tsv)
	{
		super(new TreasuresSidebarInfo(), aTresiresSidebarView_tsv);

		this._fCompletedGemAvardAnimtionDelay_t = null;
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		APP.webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);

		APP.currentWindow.treasuresController.on(TreasuresController.TREASURE_AWARD_ANIMATION_COMPLETED, this._onTreasureAwardCompleted, this);
		APP.currentWindow.treasuresController.on(TreasuresController.TREASURE_AMOUNT_UPDATE, this._onTreasureAmountUpdate, this);
		APP.currentWindow.treasuresController.on(TreasuresController.TREASURES_PRICES_UPDATE, this._onTreasuresPricesUpdate, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_CLOSE_ROOM, this._onCloseRoom, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_GAME_FIELD_CLEARED, this._onGameFieldCleared, this);
		APP.gameScreen.on(GameScreen.EVENT_ON_BET_MULTIPLIER_UPDATED, this._onBetMultiplierUpdated, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();

		this.view.on(TreasuresSidebarView.AWARD_ANIMATION_FINISH, this._onTryCompleteQuest, this);
		this.view.on(TreasuresSidebarView.EVENT_ON_GEM_ANIMATION_START, this._onGemAnimationStart, this);
		this.view.on(TreasuresSidebarView.QUEST_COMPLETE_ANIMATION_FINISH, this._onQuestCompleteAnimationFinish, this);

		this.info.currentGemsAmount = APP.currentWindow.treasuresController.getTreasureAmount();

		this._updateView();
	}

	_onGemAnimationStart(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.gemId;
		this.info.decreaseGemsAmountById(lGemId_int);
		this._updateView();
	}

	_onQuestCompleteAnimationFinish(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.gemId;
		this.info.setQuestCompetedAnimationFreeByGemId(lGemId_int);

		this.emit(TreasuresSidebarController.EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH, { gemId: lGemId_int });
	}

	_onTreasureAwardCompleted(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.id;
		this.info.increaseGemsAmountById(lGemId_int);
		this._updateView();
		this.view.startGemAwardAnimationById(lGemId_int);
	}

	_onTreasureAmountUpdate(aEvent_obj)
	{
		this.info.currentGemsAmount = aEvent_obj.treasureAmount.slice(0);
		this._updateView();
	}

	_onTreasuresPricesUpdate(aEvent_obj)
	{
		this.info.prices = aEvent_obj.prices;
		this._updateView();
	}

	_onTryCompleteQuest(aEvent_obj)
	{
		const lGemId_int = aEvent_obj.gemId;
		let lGemsAmount_int = this.info.getGemAmountById(lGemId_int);
		if (lGemsAmount_int < 1 || APP.currentWindow.gameStateController.info.gameState === 'QUALIFY')
		{
			this.info.setQuestCompetedAnimationPlayByGemId(lGemId_int);
			this.view.startQuestCompleteAnimation(lGemId_int);
			this.emit(TreasuresSidebarController.EVENT_ON_QUEST_COMPLETE_ANIMATION_FINISH_AFTER_ROUND_END, {gemId: lGemId_int});
			return;
		}
		let lTotalNeededGemsAmount_int = APP.isBattlegroundGame ? BTG_TOTAL_QUEST_COMPLETE_GEMS : TOTAL_QUEST_COMPLETE_GEMS;
		if (lGemsAmount_int >= lTotalNeededGemsAmount_int && !this.info.doNowQuestCompetedAnimationPlayByGemId(lGemId_int))
		{
			this.info.setQuestCompetedAnimationPlayByGemId(lGemId_int);
			this.view.startQuestCompleteAnimation(lGemId_int);
		}
	}

	_onBetMultiplierUpdated()
	{
		this._updateView();
	}

	_updateView()
	{
		if (!this.view)
		{
			return
		}

		if (APP.isBattlegroundGame)
		{
			this.view.update();
		}
		else
		{
			let lLevel_num = APP.playerController.info.possibleBetLevels.indexOf(APP.playerController.info.betLevel);
			this.view.update(lLevel_num);
		}
	}

	_onCloseRoom()
	{
		this._clear();
	}

	_onGameFieldCleared()
	{
		this._clear();
	}

	_onGameServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._clear();
	}

	_clear()
	{
		this.view.clear();
		this.info.clear();
	}

	destroy()
	{
		super.destroy();

		this._fCompletedGemAvardAnimtionDelay_t = null;
	}
}

export default TreasuresSidebarController;