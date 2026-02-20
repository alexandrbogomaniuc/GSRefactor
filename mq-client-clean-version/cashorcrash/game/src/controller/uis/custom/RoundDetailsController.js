import SimpleUIController from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/uis/base/SimpleUIController';
import RoundDetailsView from '../../../view/uis/custom/RoundDetailsView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameWebSocketInteractionController from '../../interaction/server/GameWebSocketInteractionController';
import BottomPanelController from './bottom_panel/BottomPanelController';

class RoundDetailsController extends SimpleUIController
{
	static get EVENT_ON_ROUND_DETAILD_OPENED() 		{ return RoundDetailsView.EVENT_ON_ROUND_DETAILD_OPENED; }
	static get EVENT_ON_ROUND_DETAILD_CLOSED() 		{ return RoundDetailsView.EVENT_ON_ROUND_DETAILD_CLOSED; }

	init()
	{
		super.init();
	}

	//INIT...
	constructor(...args)
	{
		super(...args);
	}

	__initControlLevel()
	{
		super.__initControlLevel();

		this._fRoundsHistoryController_rshc = APP.gameController.roundsHistoryController;

		let webSocketInteractionController = APP.webSocketInteractionController;
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_CONNECTION_CLOSED, this._onGameServerConnectionClosed, this);
		webSocketInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		let bottomPanelController = APP.gameController.bottomPanelController;
		bottomPanelController.on(BottomPanelController.EVENT_ROUND_HISTORY_ITEM_CLICKED, this.__onRoundHistoryItemClicked, this);
	}

    __initViewLevel()
    {
        super.__initViewLevel();

        this.view.on(RoundDetailsView.EVENT_ON_CLOSE_BTN_CLICKED, this._onCloseButtonClicked, this);
        this.view.on(RoundDetailsView.EVENT_ON_ROUND_DETAILD_OPENED, this.emit, this);
        this.view.on(RoundDetailsView.EVENT_ON_ROUND_DETAILD_CLOSED, this.emit, this);
    }
	//...INIT

	__onRoundHistoryItemClicked(event)
	{
		let lRoundsHistoryInfo_rshi = this._fRoundsHistoryController_rshc.info;
		let lRoundIndex_int = event.id;

        this.info.roundStartTime = lRoundsHistoryInfo_rshi.getStartTimeById(lRoundIndex_int);
        this.info.roundId = lRoundsHistoryInfo_rshi.getRoundIdById(lRoundIndex_int);
        this.info.betsCount = lRoundsHistoryInfo_rshi.getBetsCountById(lRoundIndex_int);
        this.info.multiplier = lRoundsHistoryInfo_rshi.getMultiplierById(lRoundIndex_int);
        this.info.uniqueToken = lRoundsHistoryInfo_rshi.getUniqueTokenById(lRoundIndex_int);

        this._showScreen();
	}

	_onCloseButtonClicked(event)
	{
		this._hideSceeen();
	}

	_onGameServerConnectionClosed(event)
	{
		if (event.wasClean)
		{
			return;
		}

		this._hideSceeen(true);
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.SERVER_SHUTDOWN:
				this._hideSceeen(true);
				break;
		}
	}

	_onGameScreenViewClicked(event)
	{
		this._hideSceeen();
	}

	_onRoundDetailsViewClicked(event)
	{
		event.stopPropagation();
	}

	_showScreen()
	{
        if (this.view)
		{
			this.info.currentRoundId = this.info.roundId;
			this.view.showScreen();
			this.view.on("pointerclick", this._onRoundDetailsViewClicked, this);
			APP.gameScreenView.gameViewContainer.on("pointerclick", this._onGameScreenViewClicked, this);
		}
	}

	_hideSceeen(aOptIsSkipAnimation_bl)
	{
        if (this.view)
		{
			this.info.currentRoundId = null;
			this.view.hideScreen(aOptIsSkipAnimation_bl);
			this.view.off("pointerclick", this._onRoundDetailsViewClicked, this);
			APP.gameScreenView.gameViewContainer.off("pointerclick", this._onGameScreenViewClicked, this);
		}
	}
}

export default RoundDetailsController