import SimpleController from '../../../../../common/PIXI/src/dgphoenix/unified/controller/base/SimpleController';
import GameWebSocketInteractionController from '../interaction/server/GameWebSocketInteractionController';
import RoundsHistoryInfo from '../../model/gameplay/RoundsHistoryInfo';
import BattlegroundRoundsHistoryInfo from '../../model/gameplay/BattlegroundRoundsHistoryInfo';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { SERVER_MESSAGES } from '../../model/interaction/server/GameWebSocketInteractionInfo';

/**
 * Keeps actual history data of previous rounds.
 */
class RoundsHistoryController extends SimpleController
{
	static get EVENT_ON_ROUNDS_HISTORY_UPDATED() 		{return "EVENT_ON_ROUNDS_HISTORY_UPDATED";}
	

	constructor(aOptInfo_rshi)
	{
		super(aOptInfo_rshi || APP.isBattlegroundGame ? new BattlegroundRoundsHistoryInfo() : new RoundsHistoryInfo());
	}

	init()
	{
		super.init();
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CRASH_GAME_INFO_MESSAGE, this._onServerCrashGameInfoMessage, this);
	}

	_onServerCrashGameInfoMessage(event)
	{
		let data = event.messageData;
		switch(data.class)
		{
			case SERVER_MESSAGES.CRASH_GAME_INFO:
				let lDataHistory = APP.isBattlegroundGame ? data.battleMultHistory : data.multHistory;
				let lIsHistoryUpdated_bl = this.info.tryToRefreshHistory(lDataHistory, data.date);
				if (lIsHistoryUpdated_bl)
				{
					this.emit(RoundsHistoryController.EVENT_ON_ROUNDS_HISTORY_UPDATED);
				}
				break;
		}
	}
}

export default RoundsHistoryController;