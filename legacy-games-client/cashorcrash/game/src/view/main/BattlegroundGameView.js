import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { ORIENTATION } from '../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import GameplayView from '../gameplay/GameplayView';
import BattlegroundRoundDetailsView from '../uis/custom/battleground/BattlegroundRoundDetailsView';
import TopPanelView from '../uis/custom/top_status_panel/TopPanelView';
import BattlegroundPlaceBetsView from '../uis/custom/placebets/battleground/BattlegroundPlaceBetsView';
import MiddlePanelView from '../uis/custom/middle_panel/MiddlePanelView';
import GameBaseView, { GAME_VIEW_SETTINGS } from './GameBaseView';

class BattlegroundGameView extends GameBaseView
{
	get topPanelView()
	{
		return this._fTopPanelView_tpv || (this._fTopPanelView_tpv = this._generateTopPanelView());
	}

	constructor()
	{
		super();
		this._fMiddlePanelView_mpv = null;
		this._fTopPanelView_tpv = null;
	}

	//override
	__init()
	{
		super.__init();

		this._updateArea();
	}

	//override
	_calculateGameZones()
	{
		GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT = APP.isMobile ? 36 : 26;
		GAME_VIEW_SETTINGS.BAP_HEIGHT = {
						[ORIENTATION.LANDSCAPE] : GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT,
						[ORIENTATION.PORTRAIT] : GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT*2
					}

		if (APP.layout.isPortraitOrientation)
		{
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE = {x: 0, y: 0, width: 540, height: 488};
		}
		else
		{
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE = {x: 256, y: 0, width: 704, height: 503};
		}
	}

	//override
	_updateArea()
	{
		super._updateArea();

		let lIsPortraitMode_bi = APP.layout.isPortraitOrientation;
		let lScreenWidth_num = APP.screenWidth;
		let lScreenHeight_num = APP.screenHeight;
		let lTopBapAreaPoint_num = lScreenHeight_num - GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT;
		let lBottomGameplayZonePoint_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;

		this.position.set(-lScreenWidth_num/2, -lScreenHeight_num/2);

		let lRoundDetailsView_rdsv = this.roundDetailsView;
		let lGameplayMiddlePoint_num = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width/2;
		lRoundDetailsView_rdsv.position.set(lGameplayMiddlePoint_num - BattlegroundRoundDetailsView.ROUND_DETAILS_WIDTH/2, (lIsPortraitMode_bi ? lBottomGameplayZonePoint_num : lTopBapAreaPoint_num) - BattlegroundRoundDetailsView.ROUND_DETAILS_HEIGHT);
		
		let lPlaceBetsWidth_num = lIsPortraitMode_bi ? lScreenWidth_num/2 : 256;
		let lDefPlaceBetsHeight_num = 155;

		let lPlaceBetsTopPoint_num = lIsPortraitMode_bi ? (GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT + 4) : 100;		
		let lPlaceBetsLeftPoint_num = 0;

		let lPlaceBetsLayout_rt = new PIXI.Rectangle(
			lPlaceBetsLeftPoint_num, 
			lPlaceBetsTopPoint_num, 
			lPlaceBetsWidth_num, 
			lDefPlaceBetsHeight_num
		);
		this.placeBetsView.updateLayout(lPlaceBetsLayout_rt, lIsPortraitMode_bi);

		if (this._fTopPanelView_tpv)
		{
			let llTopPanelLayout_rt = new PIXI.Rectangle(
				lIsPortraitMode_bi ? 9 : 0,
				lIsPortraitMode_bi ? ((GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT + 4) + 17) : 0,
				lIsPortraitMode_bi ? lScreenWidth_num : lPlaceBetsWidth_num,
				100 
			);
			this._fTopPanelView_tpv.updateLayout(llTopPanelLayout_rt, lIsPortraitMode_bi);
		}

		if (this._fMiddlePanelView_mpv)
		{
			let lMiddlePanelLayout_rt = new PIXI.Rectangle(
				lIsPortraitMode_bi ? 270 : 0, //x
				lIsPortraitMode_bi ? 609 : 250, //y
				lIsPortraitMode_bi ? lScreenWidth_num/2 : lPlaceBetsWidth_num, //width
				lIsPortraitMode_bi ? 500 : 100 //height
			);

			this._fMiddlePanelView_mpv.updateLayout(lMiddlePanelLayout_rt, lIsPortraitMode_bi);
		}
	}

	//override
	_generateGameplayView()
	{
		let lGameplayView_gpv = this.addChild(new GameplayView);

		return lGameplayView_gpv;
	}

	//override
	_generateRoundDetailsView()
	{
		let lRoundDetailsView_rdsv = this.addChild(new BattlegroundRoundDetailsView());
		return lRoundDetailsView_rdsv;
	}

	//override
	_generateBetsListView()
	{
		let lMiddlePanelView_mpv = this._fMiddlePanelView_mpv = this.addChild(new MiddlePanelView());
		return lMiddlePanelView_mpv;
	}

	//override
	_generatePlaceBetsView()
	{
		let lPlaceBetsView_pbsv = this.addChild(new BattlegroundPlaceBetsView());
		return lPlaceBetsView_pbsv;
	}

	_generateTopPanelView()
	{
		let lTopPanelView_tpv = this.addChild(new TopPanelView());
		return lTopPanelView_tpv;
	}
}

export default BattlegroundGameView;