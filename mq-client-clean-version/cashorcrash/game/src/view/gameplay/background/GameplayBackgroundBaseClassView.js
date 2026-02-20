import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class GameplayBackgroundBaseClassView extends Sprite
{
	static registerTileset( aTilesetView_rtbcv )
	{
		if(!GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr)
		{
			GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr = [];
		}

		GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr.push(aTilesetView_rtbcv);
	}

	constructor()
	{
		super();

		this._fGameplayView_gpv = null;
		this._fGameplayInfo_gpi = null;

		//TILESETS...
		let l_rtbcv_arr = GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr;

		for( let i = 0; i < l_rtbcv_arr.length; i++ )
		{
			this.addChild(l_rtbcv_arr[i]);
		}
		//...TILESETS

		this.adjust();
	}


	adjust()
	{
		let l_gpv = this._fGameplayView_gpv || (this._fGameplayView_gpv = APP.gameController.gameplayController.view);
		let l_gpi = this._fGameplayInfo_gpi || (this._fGameplayInfo_gpi = APP.gameController.gameplayController.info);
		
		let l_rtbcv_arr = GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr;
		
		let lRoundMillisecondIndex_int = l_gpi.isPreLaunchTimePeriod ? -l_gpi.multiplierChangeFlightRestTime : l_gpi.multiplierRoundDuration;
		if (l_gpi.isPreLaunchFlightRequired && l_gpi.multiplierChangeFlightRestTime > l_gpi.preLaunchFlightDuration)
		{
			lRoundMillisecondIndex_int = -l_gpi.preLaunchFlightDuration;
		}

		let lScale_num = l_gpv.getCorrespondentZoomOutScale(lRoundMillisecondIndex_int);

		for( let i = 0; i < l_rtbcv_arr.length; i++ )
		{
			l_rtbcv_arr[i].adjust();
			l_rtbcv_arr[i].scale.set(lScale_num);
		}
	}

	updateArea()
	{
		let l_rtbcv_arr = GameplayBackgroundBaseClassView.tilesetViews_rtbcv_arr;

		for( let i = 0; i < l_rtbcv_arr.length; i++ )
		{
			l_rtbcv_arr[i].updateArea();
		}
	}
}
export default GameplayBackgroundBaseClassView;