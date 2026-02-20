import { Sprite } from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display";
import { APP } from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals";
import I18 from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import TextField from "../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField";
import BattlegroundRoundResultInformationOneWinnerScreenView from './BattlegroundRoundResultInformationOneWinnerScreenView';
import BattlegroundRoundResultInformationTieWinnersScreenView from './BattlegroundRoundResultInformationTieWinnersScreenView';
import BattlegroundRoundResultInformationNoWinnersScreenView from './BattlegroundRoundResultInformationNoWinnersScreenView';

class BattlegroundRoundResultInformationScreenView extends Sprite
{
	static get TIE_MODE_TWO () { return 1 };
	static get TIE_MODE_MANY () { return 2 };
	static get EJECT_MODE_NO_REFUND () { return 1 };
	static get EJECT_MODE_REFUND () { return 2 };
	
	constructor()
	{
		super();

		this._fMainContainer_spr = null;
		this._fOneWinnerScreenView_brriowsv = null;
		this._fTieWinnerScreenView_brritwssv = null;
		this._fNoWinnersScreenView_brrinwssv = null;

		this.init();
	}


	init()
	{
		this._fMainContainer_spr = this.addChild(new Sprite());

		this._fOneWinnerScreenView_brriowsv = this._fMainContainer_spr.addChild(new BattlegroundRoundResultInformationOneWinnerScreenView);
		this._fTieWinnerScreenView_brritwssv = this._fMainContainer_spr.addChild(new BattlegroundRoundResultInformationTieWinnersScreenView);
		this._fNoWinnersScreenView_brrinwssv = this._fMainContainer_spr.addChild(new BattlegroundRoundResultInformationNoWinnersScreenView);
	}

	adjust()
	{
		let l_gpc = APP.gameController.gameplayController;
		let l_gpi = l_gpc.info;
		let l_gpv = l_gpc.view;
		let l_ri = l_gpi.roundInfo;
		if (
				(l_ri.isRoundQualifyState || l_ri.isRoundWaitState)
				&& l_ri.hasActualPreviousRoundResults
				&& !l_gpv.battlegroundYouWonView.isAnimationInProgress
			)
		{
			this.visible = true;

			this._fOneWinnerScreenView_brriowsv.adjust();
			this._fTieWinnerScreenView_brritwssv.adjust();
			this._fNoWinnersScreenView_brrinwssv.adjust();

			this._fMainContainer_spr.position.y = this._fNoWinnersScreenView_brrinwssv.isRefundMode ? 52 : 124;
		}
		else
		{
			this.visible = false;
		}
	}

	updateArea()
	{
		this._fMainContainer_spr.position.x = APP.layout.isPortraitOrientation ? 254 : 554;
	}

	destroy()
	{
		super.destroy();
	}
}
export default BattlegroundRoundResultInformationScreenView;