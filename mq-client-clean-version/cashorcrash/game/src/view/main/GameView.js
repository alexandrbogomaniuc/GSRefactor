import SimpleUIView from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/SimpleUIView';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { ORIENTATION } from '../../../../../common/PIXI/src/dgphoenix/unified/view/layout/DOMLayout';
import GameplayView from '../gameplay/GameplayView';
import RoundDetailsView from '../uis/custom/RoundDetailsView';
import BetsListView from '../uis/custom/betslist/BetsListView';
import PlaceBetsView from '../uis/custom/placebets/PlaceBetsView';
import ScreenKeyboard from '../uis/custom/placebets/ScreenKeyboard';
import GameBaseView, { GAME_VIEW_SETTINGS } from './GameBaseView';

class GameView extends GameBaseView
{
	constructor()
	{
		super();
	}

	//override
	__init()
	{
		super.__init();

		if (APP.isMobile)
		{
			this.addChild(ScreenKeyboard.instance);
			ScreenKeyboard.instance.hide();
		}
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
		lRoundDetailsView_rdsv.position.set(lGameplayMiddlePoint_num - RoundDetailsView.ROUND_DETAILS_WIDTH/2, (lIsPortraitMode_bi ? lBottomGameplayZonePoint_num : lTopBapAreaPoint_num) - RoundDetailsView.ROUND_DETAILS_HEIGHT);
		
		let lPlaceBetsWidth_num = lIsPortraitMode_bi ? lScreenWidth_num-4 : 256;
		let lDefPlaceBetsHeight_num = 355;

		let lPlaceBetsTopPoint_num = lIsPortraitMode_bi ? (GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y + GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT + 4) : GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.y;
		let lPlaceBetsLeftPoint_num = lIsPortraitMode_bi ? 2 : 0;
		let lPlaceBetsLayout_rt = new PIXI.Rectangle(lPlaceBetsLeftPoint_num, lPlaceBetsTopPoint_num, lPlaceBetsWidth_num, lDefPlaceBetsHeight_num);
		this.placeBetsView.updateLayout(lPlaceBetsLayout_rt, lIsPortraitMode_bi);

		let lBetsListHeight_num = lIsPortraitMode_bi ? 125 : GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height - lDefPlaceBetsHeight_num;
		let lBetsListTopPoint_num = lDefPlaceBetsHeight_num;
		if (lIsPortraitMode_bi)
		{
			lBetsListTopPoint_num = lScreenHeight_num - GAME_VIEW_SETTINGS.BAP_ROW_HEIGHT - lBetsListHeight_num;
			if (APP.isMobile)
			{
				lBetsListTopPoint_num += 5;
			}
		}

		let lBetsListLayout_rt = new PIXI.Rectangle(
			0,
			lBetsListTopPoint_num,
			lIsPortraitMode_bi ? lPlaceBetsWidth_num/2 : lPlaceBetsWidth_num,
			lBetsListHeight_num 
		);
		this.betsListView.updateLayout(lBetsListLayout_rt, lIsPortraitMode_bi)

		if (APP.isMobile)
		{
			let lKeyBoard_sk = ScreenKeyboard.instance;
			let lOffset_num = 1;
			if (lIsPortraitMode_bi)
			{
				lKeyBoard_sk.position.set(278, lPlaceBetsTopPoint_num+lOffset_num);
			}
			else
			{
				lKeyBoard_sk.position.set(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.x+lOffset_num, lPlaceBetsTopPoint_num+lOffset_num);
			}
			
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
		let lRoundDetailsView_rdsv = this.addChild(new RoundDetailsView());
		return lRoundDetailsView_rdsv;
	}

	//override
	_generateBetsListView()
	{
		let lBetsListView_bslv = this.addChild(new BetsListView());
		return lBetsListView_bslv;
	}

	//override
	_generatePlaceBetsView()
	{
		let lPlaceBetsView_pbsv = this.addChild(new PlaceBetsView());
		return lPlaceBetsView_pbsv;
	}
}

export default GameView;