import RoundDetailsController from '../RoundDetailsController';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import RoomController from '../../../gameplay/RoomController';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';

class BattlegroundRoundDetailsController extends RoundDetailsController
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

		let lRoomCVontroller_rc = APP.gameController.gameplayController.roomController;
		lRoomCVontroller_rc.on(RoomController.EVENT_ON_RAKE_DEFINED, this._onRakeDefined, this);
	}

    __initViewLevel()
    {
        super.__initViewLevel();
    }
	//...INIT

	__onRoundHistoryItemClicked(event)
	{
		let lRoundsHistoryInfo_rshi = this._fRoundsHistoryController_rshc.info;
		let lRoundIndex_int = event.id;

		let lWinners = lRoundsHistoryInfo_rshi.getWinnersById(lRoundIndex_int);

		let lMasterNickname_str = APP.gameController.gameplayController.info.gamePlayersInfo.observerId;
		let lWinnerMultValue_num  = 0;
		let lWinnerNames_str_arr  = [];
		let lWinnerNames_str = null;
		let lMasterMultValue_num  = null;
		let lMasterCurrentPlace = null;		
		
		for (let key in lWinners) {
			if (lWinnerMultValue_num < lWinners[key])
			{
				lWinnerMultValue_num = lWinners[key];
				lWinnerNames_str_arr = [];
			}

			if (key == lMasterNickname_str)
			{				
				lMasterMultValue_num = lWinners[key];
			}
			else if (lWinnerMultValue_num == lWinners[key])
			{
				lWinnerNames_str_arr.push(key);
			}
		}

		if (lMasterMultValue_num)
		{
			lMasterCurrentPlace = 1;

			for (let key in lWinners) {			

				if (lMasterMultValue_num < lWinners[key])
				{
					lMasterCurrentPlace++;
				}			
			}
		}

		let l_tad = null;
		if (lMasterCurrentPlace == 1)
		{
			if (lWinnerNames_str_arr.length > 1)
			{
				l_tad = I18.getTranslatableAssetDescriptor("TABattlegroundRoundHistoryYouAndXXWinners");
				lWinnerNames_str = l_tad.textDescriptor.text.replace('XX', lWinnerNames_str_arr.length);
			}
			else if (lWinnerNames_str_arr.length == 1)
			{
				l_tad = I18.getTranslatableAssetDescriptor("TABattlegroundRoundHistoryYouAndPlayer");
				lWinnerNames_str = l_tad.textDescriptor.text.replace('XX', lWinnerNames_str_arr[0]);
			}
			else
			{
				lWinnerNames_str = lMasterNickname_str;
			}
		}
		else
		{
			if (lWinnerNames_str_arr.length > 2)
			{
				l_tad = I18.getTranslatableAssetDescriptor("TABattlegroundRoundHistoryXXWinners");
				lWinnerNames_str = l_tad.textDescriptor.text.replace('XX', lWinnerNames_str_arr.length);
			}
			else if (lWinnerNames_str_arr.length == 2)
			{
				lWinnerNames_str = lWinnerNames_str_arr[0] + ", " + lWinnerNames_str_arr[1];
			}
			else if (lWinnerNames_str_arr.length == 1)
			{
				lWinnerNames_str = lWinnerNames_str_arr[0];
			}
		}

		if (lWinnerNames_str_arr.length != 0 || lMasterCurrentPlace == 1)
		{
			let lRakeCoof_num = (100 - this.info.rakePercent) * 0.01;
			let lTotalPot_num = lRakeCoof_num * lRoundsHistoryInfo_rshi.getBetsCountById(lRoundIndex_int) * APP.gameController.info.battlegroundBetValue;

			let lFormattedValue_str;
			// [OWL] TODO: apply changes for alll systems without any conditions
			if (APP.appParamsInfo.restrictCoinFractionLength !== undefined)
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(lTotalPot_num, true, APP.isBattlegroundGame, 2, undefined, false, true, true, APP.appParamsInfo.restrictCoinFractionLength);
			}
			else
			{
				lFormattedValue_str = APP.currencyInfo.i_formatNumber(lTotalPot_num, true, APP.isBattlegroundGame, 2, undefined, false);
			}

			this.info.totalPot = lFormattedValue_str;
			this.info.winnerPot = lWinnerNames_str;
		}
		else
		{
			this.info.totalPot = '-';
			this.info.winnerPot = '-';
		}
		this.info.currentPlayerPlace = lMasterCurrentPlace ? lMasterCurrentPlace : '-';		

		super.__onRoundHistoryItemClicked(event);
	}

	_onRakeDefined()
	{
		let l_rci = APP.gameController.gameplayController.roomController.info;
		this.info.rakePercent = l_rci.rakePercent;
	}
	
}

export default BattlegroundRoundDetailsController