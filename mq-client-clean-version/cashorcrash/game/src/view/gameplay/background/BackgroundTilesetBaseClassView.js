import GameplayBackgroundBaseClassView from './GameplayBackgroundBaseClassView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import BackgroundTileBaseClassView from './BackgroundTileBaseClassView';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundTilesetBaseClassView extends Sprite
{
	static getParalaxSpeedMultiplier()
	{
		return 4;
	}

	constructor(aTilesQueueLength_int)
	{
		super();

		this._fTemplateTilesQueueLength_int = aTilesQueueLength_int;
		this._fCurrentTemplateTileIndexInQueue_int = 0;

		this._fTiles_rcv_arr = [];
		this._fExpansionWidth_num = 0;
		this._fExpansionHeight_num = 0;

		this._fCurAdjustedMultiplierValue_num = undefined;

		GameplayBackgroundBaseClassView.registerTileset(this);
	}

	generateTileView(aTemplateTileIndex_int)
	{
		switch(aTemplateTileIndex_int)
		{
			case 0:
				return new BackgroundTileBaseClassView();
		}

		return null;
	}

	getInitialPositionX()
	{
		return 0;
	}

	getInitialPositionY()
	{
		return 0;
	}

	getOffsetPerMultiplierX()
	{
		return 0;
	}

	getOffsetPerMultiplierY()
	{
		return 0;
	}

	getOffsetPerPreLaunchX()
	{
		return 0;
	}

	getOffsetPerPreLaunchY()
	{
		return 0;
	}

	getTilesetHeight()
	{
		return 0;
	}

	getTilesetWidth()
	{
		return 0;
	}

	expandIfRequired(aWidthInPixels_num)
	{
		if(this._fTemplateTilesQueueLength_int === 0)
		{
			return;
		}

		while( this._fExpansionWidth_num + this.x < aWidthInPixels_num )
		{
			let l_rcdc = this.generateTileView(this._fCurrentTemplateTileIndexInQueue_int);
			l_rcdc.position.x = this._fExpansionWidth_num;

			this.addChild(l_rcdc);
			this._fTiles_rcv_arr.push(l_rcdc);
			this._fExpansionWidth_num += l_rcdc.getTileWidth();

			this._fCurrentTemplateTileIndexInQueue_int++;

			if( this._fCurrentTemplateTileIndexInQueue_int > this._fTemplateTilesQueueLength_int - 1 )
			{
				this._fCurrentTemplateTileIndexInQueue_int = 0;
			}
		}
	}

	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lCurGameplayMultiplierValue_num = l_gpi.multiplierValue;
		let lRoundInfo_ri = l_gpi.roundInfo;

		// if (this._fCurAdjustedMultiplierValue_num === lCurGameplayMultiplierValue_num)
		// {
		// 	return;
		// }
		
		let lMultiplierDelta_num = null;
		if (lRoundInfo_ri.isRoundWaitState)
		{
			lMultiplierDelta_num = 0;
		}
		else
		{
			lMultiplierDelta_num = (lCurGameplayMultiplierValue_num - l_gpi.minMultiplierValue) * BackgroundTilesetBaseClassView.getParalaxSpeedMultiplier();
		}
		
		let lHeight_num = this.getTilesetHeight();

		let lPreLaunchTimeDeltaMult_num = 0;
		if (lRoundInfo_ri.isRoundStartTimeDefined && l_gpi.isPreLaunchFlightRequired)
		{
			let lRestTime_num = l_gpi.multiplierChangeFlightRestTime;
			let lPreLaunchTimeDelta_num = lRestTime_num > l_gpi.preLaunchFlightDuration ? l_gpi.preLaunchFlightDuration : Math.max(lRestTime_num, 0);
			lPreLaunchTimeDeltaMult_num = 1-lPreLaunchTimeDelta_num/l_gpi.preLaunchFlightDuration;
		}

		this.position.set(
			this.getInitialPositionX() + this.getOffsetPerPreLaunchX() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierX() * lMultiplierDelta_num,
			this.getInitialPositionY() + this.getOffsetPerPreLaunchY() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierY() * lMultiplierDelta_num
			);

		if(
			lHeight_num === 0 ||
			this.position.y < GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height + lHeight_num
			)
		{
			this.expandIfRequired(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width);
		}

		this._fCurAdjustedMultiplierValue_num = lCurGameplayMultiplierValue_num;
	}

	updateArea()
	{
		this._fCurAdjustedMultiplierValue_num = undefined;
	}
}
export default BackgroundTilesetBaseClassView;