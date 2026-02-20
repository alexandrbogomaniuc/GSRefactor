import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundFulfillTilesetBaseClassView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super();

		this._fOffsetX_num = 0;
		this._fOffsetY_num = 0;

		this._fPrevGameplayZoneWidth = undefined;
		this._fPrevGameplayZoneHegiht = undefined;

		this._fTiles_rcdc_arr_arr = [];
	}

	getTileWidth()
	{
		return 123;
	}

	getTileHeight()
	{
		return 123;
	}

	//override
	expandIfRequired(aWidthInPixels_num, aHeightInPixels_num)
	{
		let lTileWidth_num = this.getTileWidth();
		let lTileHeight_num = this.getTileHeight();

		let lColumnsCount_int = Math.trunc(aWidthInPixels_num / lTileWidth_num) + 1;
		let lRowsCount_int = Math.trunc(aHeightInPixels_num / lTileHeight_num) + 1;
		let l_rcdc_arr_arr = this._fTiles_rcdc_arr_arr;

		for( let  y = 0; y <= lRowsCount_int; y++ )
		{
			if(!l_rcdc_arr_arr[y])
			{
				l_rcdc_arr_arr[y] = [];
			}

			for( let  x = 0; x <= lColumnsCount_int; x++ )
			{
				let l_rcdc = l_rcdc_arr_arr[y][x];

				if(!l_rcdc)
				{
					let l_rcdc = this.generateTileView();
					l_rcdc.anchor.set(0, 1);
					l_rcdc_arr_arr[y][x] = this.addChild(l_rcdc);
				}
				
				l_rcdc_arr_arr[y][x].visible = true;
			}
		}
	}

	//override
	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;

		// if (this._fCurAdjustedMultiplierValue_num === l_gpi.multiplierValue)
		// {
		// 	return;
		// }
		
		let lMultiplierDelta_num = (l_gpi.multiplierValue - l_gpi.minMultiplierValue) * BackgroundTilesetBaseClassView.getParalaxSpeedMultiplier();

		let lPreLaunchTimeDeltaMult_num = 0;
		if (lRoundInfo_ri.isRoundStartTimeDefined && l_gpi.isPreLaunchFlightRequired)
		{
			let lRestTime_num = l_gpi.multiplierChangeFlightRestTime;
			let lPreLaunchTimeDelta_num = lRestTime_num > l_gpi.preLaunchFlightDuration ? l_gpi.preLaunchFlightDuration : Math.max(lRestTime_num, 0);
			if (!(lRestTime_num > l_gpi.preLaunchFlightDuration))
			{
				lPreLaunchTimeDelta_num = Math.max(lRestTime_num, 0);
				lPreLaunchTimeDeltaMult_num = 1-lPreLaunchTimeDelta_num/l_gpi.preLaunchFlightDuration;
			}
		}

		let lPrevOffsetX_num = this._fOffsetX_num;
		let lPrevOffsetY_num = this._fOffsetY_num;
		
		this._fOffsetX_num = this.getOffsetPerPreLaunchX() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierX() * lMultiplierDelta_num;
		this._fOffsetY_num = this.getOffsetPerPreLaunchY() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierY() * lMultiplierDelta_num;

		// console.log("** this._fOffsetX_num:", this._fOffsetX_num, "; lRestTimeToRound_num:", (lRoundInfo_ri.roundStartTime - lCurGameplayTime_num), "; lPreLaunchTimeDeltaMult_num:", lPreLaunchTimeDeltaMult_num)
	
		if (Math.abs(this._fOffsetX_num) > this.getTileWidth())
		{
			this._fOffsetX_num = this._fOffsetX_num % this.getTileWidth();
		}

		if (Math.abs(this._fOffsetY_num) > this.getTileHeight())
		{
			this._fOffsetY_num = this._fOffsetY_num % this.getTileHeight();
		}
		
		this.expandIfRequired(
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width,
			GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height);

		if (lPrevOffsetX_num !== this._fOffsetX_num 
			|| lPrevOffsetY_num !== this._fOffsetY_num
			|| this._fPrevGameplayZoneWidth !== GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width
			|| this._fPrevGameplayZoneHegiht !== GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height)
		{
			let l_rcdc_arr_arr = this._fTiles_rcdc_arr_arr;
			
			for( let y = 0; y < l_rcdc_arr_arr.length; y++ )
			{
				for( let x = 0; x < l_rcdc_arr_arr[y].length; x++ )
				{
					let l_rcdc = l_rcdc_arr_arr[y][x];
	
					l_rcdc.position.set(
						x * this.getTileWidth() + this._fOffsetX_num,
						-y * this.getTileHeight() + this._fOffsetY_num);

					if (l_rcdc.position.x < -this.getTileWidth() 
						|| l_rcdc.position.y > GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height
						|| l_rcdc.position.y < -2*this.getTileHeight()
						|| l_rcdc.position.x > GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width)
					{
						l_rcdc.visible = false;
					}
					else
					{
						l_rcdc.visible = true;
					}
				}
			}

			this._fPrevGameplayZoneWidth = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width;
			this._fPrevGameplayZoneHegiht = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
		}

		this.position.y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;

		this._fCurAdjustedMultiplierValue_num = l_gpi.multiplierValue;
	}
}
export default BackgroundFulfillTilesetBaseClassView;