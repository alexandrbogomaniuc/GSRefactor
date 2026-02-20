import PlaceBetsBaseController from './PlaceBetsBaseController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import RoundController from '../../../gameplay/RoundController';
import BetsController from '../../../gameplay/bets/BetsController';
import { EDIT_AUTO_EJECT_REJECT_REASONS } from '../../../gameplay/bets/BaseBetsController';
import GamePlayersController from '../../../gameplay/players/GamePlayersController';
import GameWebSocketInteractionController from '../../../interaction/server/GameWebSocketInteractionController';
import BetInfo from '../../../../model/gameplay/bets/BetInfo';
import { CLIENT_MESSAGES } from '../../../../model/interaction/server/GameWebSocketInteractionInfo';

class PlaceBetsController extends PlaceBetsBaseController
{
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

		let wsInteractionController = APP.webSocketInteractionController;
		wsInteractionController.on(GameWebSocketInteractionController.EVENT_ON_SERVER_ERROR_MESSAGE, this._onServerErrorMessage, this);

		this._fGamePlayersController_gpsc = APP.gameController.gameplayController.gamePlayersController;
		this._fGamePlayersController_gpsc.once(GamePlayersController.EVENT_ON_MASTER_PLAYER_IN, this._onMasterPlayerIn, this);

		this._fBetsController_bc = this._fGamePlayersController_gpsc.betsController;
		this._fBetsController_bc.on(BetsController.EVENT_ON_BET_LIMITS_UPDATED, this._onBetLimitsUpdated, this);
		this._fBetsController_bc.on(BetsController.EVENT_ON_CRASH_CANCEL_AUTOEJECT_CONFIRMED, this._onAutoEjectCancelConfirmed, this);
		this._fBetsController_bc.on(BetsController.EVENT_ON_EDIT_AUTOEJECT_CONFIRMED, this._onAutoEjectEditConfirmed, this);
		this._fBetsController_bc.on(BetsController.EVENT_ON_EDIT_AUTOEJECT_REJECTED, this._onAutoEjectEditRejected, this);

		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fRoundController_rc.on(RoundController.EVENT_ON_ROUND_STATE_CHANGED, this._onRoundStateChanged, this);
	}

	__initViewLevel()
	{
		super.__initViewLevel();
	}
	//...INIT

	_onBetLimitsUpdated()
	{
		this.view.updateBetLimits();
	}

	_onAutoEjectCancelConfirmed(event)
	{
		this.view.confirmCancelAutoEject(event.astronautIndex, event.cancelType);
	}

	_onAutoEjectEditConfirmed(event)
	{
		this.view.confirmEditAutoEject(event.astronautIndex);
	}

	_onAutoEjectEditRejected(event)
	{
		if (event.rejectReason === EDIT_AUTO_EJECT_REJECT_REASONS.WRONG_ROUND_STATE)
		{
			let l_ri = this._fRoundController_rc.info;
			let l_bi = this._fBetsController_bc.info.getBetInfo(event.betId);

			if ((l_ri.isRoundPlayState || l_ri.isRoundBuyInState || l_ri.isRoundPauseState) && l_bi && !l_bi.isEjected)
			{
				this.view.updateMasterBet(event.betIndex);
			}
		}
	}

	_onMasterPlayerIn()
	{
		this.view.updateMasterBets();
	}

	_onRoundStateChanged()
	{
		let l_ri = this._fRoundController_rc.info;
		if (l_ri.isRoundPlayState)
		{
			this.view && this.view.updateMasterBets();
		}
	}

	_onServerErrorMessage(event)
	{
		let serverData = event.messageData;
		let requestData = event.requestData;
		let lAstronautIndex_int;

		switch (serverData.code)
		{
			case GameWebSocketInteractionController.ERROR_CODES.BAD_MULTIPLIER:
				lAstronautIndex_int = BetInfo.extractBetIndex(requestData.betId);
				let lAutoEjectMultValue_num = requestData.multiplier;

				this.view.handleDeniedAutoEject(lAstronautIndex_int, lAutoEjectMultValue_num);
				break;
			case GameWebSocketInteractionController.ERROR_CODES.CHANGE_BET_NOT_ALLOWED:
			case GameWebSocketInteractionController.ERROR_CODES.BET_NOT_FOUND:
				if (requestData.class === CLIENT_MESSAGES.CRASH_CHANGE_AUTOEJECT)
				{
					lAstronautIndex_int = BetInfo.extractBetIndex(requestData.betId);

					this.view.applyActualAutoEjectMultiplier(lAstronautIndex_int);
				}
				break;
		}
	}
}

export default PlaceBetsController