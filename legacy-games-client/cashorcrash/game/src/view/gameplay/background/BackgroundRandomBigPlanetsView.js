import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileRandomPlanetView from "./tiles/BackgroundTileRandomPlanetView";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayBackgroundView from './GameplayBackgroundView';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';

class BackgroundRandomBigPlanetsView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super();

		this._fOffsetX_num = 0;
		this._fOffsetY_num = 0;

		this._fBigPlanet_btrpv = null;

		this._fLastInitialXOffset_num = this.getTileWidth();
		this._fLastCiclesAmount_int = 0;
	}

	getTileWidth()
	{
		return 600;
	}

	getTileHeight()
	{
		return 550;
	}

	//OVERRIDE...
	getInitialPositionX()
	{
		return 150;
	}

	getInitialPositionY()
	{
		return 50;
	}

	getOffsetPerPreLaunchX()
	{
		return 370;
	}

	getOffsetPerPreLaunchY()
	{
		return 123;
	}
	//...OVERRIDE

	//OVERRIDE...
	generateTileView(aIndex_int)
	{
		let l_rcdc = new BackgroundTileRandomPlanetView(aIndex_int);

		return l_rcdc;
	}
	//...OVERRIDE

	//OVERRIDE...
	expandIfRequired(aWidthInPixels_num, aHeightInPixels_num)
	{
		let lTileWidth_num = this.getTileWidth();
		let lTileHeight_num = this.getTileHeight();

		let l_btrpv = this._fBigPlanet_btrpv;

		if (!l_btrpv)
		{
			l_btrpv = this.generateTileView(BackgroundTileRandomPlanetView.PLANET_VIEWS_AMOUNT-1);

			this.addChild(l_btrpv);

			this._fBigPlanet_btrpv = l_btrpv;
		}

	}
	//...OVERRIDE

	//OVERRIDE...
	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let lRoundInfo_ri = l_gpi.roundInfo;
		
		let lMultiplierDelta_num = (l_gpi.multiplierValue - l_gpi.minMultiplierValue) * BackgroundTilesetBaseClassView.getParalaxSpeedMultiplier();

		let lPreLaunchTimeDeltaMult_num = 0;
		if (lRoundInfo_ri.isRoundStartTimeDefined && l_gpi.isPreLaunchFlightRequired)
		{
			let lRestTime_num = l_gpi.multiplierChangeFlightRestTime;
			let lPreLaunchTimeDelta_num = lRestTime_num > l_gpi.preLaunchFlightDuration ? l_gpi.preLaunchFlightDuration : Math.max(lRestTime_num, 0);
			lPreLaunchTimeDeltaMult_num = 1-lPreLaunchTimeDelta_num/l_gpi.preLaunchFlightDuration;
		}
		
		this._fOffsetX_num = this.getOffsetPerPreLaunchX() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierX() * lMultiplierDelta_num;
		this._fOffsetY_num = this.getOffsetPerPreLaunchY() * lPreLaunchTimeDeltaMult_num + this.getOffsetPerMultiplierY() * lMultiplierDelta_num;

		let lTileCiclesX_int = Math.trunc(Math.abs(this._fOffsetX_num/this.getTileWidth()));
		let lTileCiclesY_int = Math.trunc(Math.abs(this._fOffsetY_num/this.getTileHeight()));	
		let lCurTileCyclesAmount_int = Math.max(lTileCiclesX_int, lTileCiclesY_int);

		let lIsTileCycleChanged_bl = false;
		if (lCurTileCyclesAmount_int !== this._fLastCiclesAmount_int)
		{
			this._fLastCiclesAmount_int = lCurTileCyclesAmount_int;
			lIsTileCycleChanged_bl = true;
		}
		
		if (Math.abs(this.getTileWidth()/this.getOffsetPerMultiplierX()) < Math.abs(this.getTileHeight()/this.getOffsetPerMultiplierY()))
		{
			this._fOffsetX_num = this._fOffsetX_num % this.getTileWidth();

			let lYOffsetMult_num = this._fOffsetX_num/this.getOffsetPerMultiplierX();
			this._fOffsetY_num = this.getOffsetPerMultiplierY()*lYOffsetMult_num;
		}
		else if (Math.abs(this._fOffsetY_num) > this.getTileHeight())
		{
			this._fOffsetY_num = this._fOffsetY_num % this.getTileHeight();

			let lXOffsetMult_num = this._fOffsetY_num/this.getOffsetPerMultiplierY();
			this._fOffsetX_num = this.getOffsetPerMultiplierX()*lXOffsetMult_num;
		}

		this.expandIfRequired(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height);
		
		if (lIsTileCycleChanged_bl)
		{
			this._fLastInitialXOffset_num = Math.trunc(l_gpi.getPseudoRandomValue(~~Math.abs(this._fOffsetX_num)) * this.getTileWidth());
			this.setRandomPlanets(lCurTileCyclesAmount_int, GameplayBackgroundView.TILESET_SMALL_PLANETS.currentPlanetIndex);
		}

		let l_rcdc = this._fBigPlanet_btrpv;

		let lBaseX_num = this._fLastInitialXOffset_num;
		let lBaseY_num = -this.getTileHeight();

		l_rcdc.position.set(
			this.getInitialPositionX() + lBaseX_num + this._fOffsetX_num,
			this.getInitialPositionY() + lBaseY_num + this._fOffsetY_num);

		l_rcdc.visible = this._fLastCiclesAmount_int > 0;

		this.position.y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierX()
	{
		return -60;
	}
	//...OVERRIDE

	//OVERRIDE...
	getOffsetPerMultiplierY()
	{
		return 100;
	}
	//...OVERRIDE

	setRandomPlanets(aOffset_num=0, aExcludePlanets_int_arr=[])
	{
		this._fBigPlanet_btrpv.randomize(aOffset_num, aExcludePlanets_int_arr);
	}

	get currentPlanetIndex()
	{
		return this._fBigPlanet_btrpv.currentPlanetIndex;
	}
}
export default BackgroundRandomBigPlanetsView;