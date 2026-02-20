import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileRandomAsteroidView from "./tiles/BackgroundTileRandomAsteroidView";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import GameplayBackgroundView from './GameplayBackgroundView';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import TextField from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/TextField';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import { ROUND_STATES } from '../../../model/gameplay/RoundInfo';

const MAX_ASTEROIDS_AMOUNT = 30;

class BackgroundRandomAsteroidsView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super();

		this._fAsteroids_arr = [];

		this._fBasePathLen_num = Math.sqrt(this.getTileWidth()*this.getTileWidth() + this.getTileHeight()*this.getTileHeight());
		this._fBasePathDuration_num = 1000;
		this._fBaseAngle_num = 150;
		this._asteroidsContainer = this.addChild(new Sprite());
		this._smoothRoundDuration = {isActive:false, multiplierRoundDuration_int:0};
	}

	getTileWidth()
	{
		return 700;
	}

	getTileHeight()
	{
		return 550;
	}
	//...OVERRIDE

	//OVERRIDE...
	generateTileView(aIndex_int)
	{
		let l_btrav = new BackgroundTileRandomAsteroidView(aIndex_int);
		return l_btrav;
	}
	//...OVERRIDE

	get asteroids()
	{
		let lAsteroidViews_arr = [];
		for (let i=0; i<this._fAsteroids_arr.length; i++)
		{
			lAsteroidViews_arr.push(this._fAsteroids_arr[i].asteroid);
		}

		return lAsteroidViews_arr;
	}

	//OVERRIDE...
	expandIfRequired(aWidthInPixels_num, aHeightInPixels_num)
	{
		let l_arr = this._fAsteroids_arr;
		for (let i=l_arr.length; i<MAX_ASTEROIDS_AMOUNT; i++)
		{
			let lAsteroidView_btrav = this.generateTileView(i);

			let lContainer_sprt = this._asteroidsContainer;
			lContainer_sprt.addChild(lAsteroidView_btrav);

			let lAsteroidDescr_obj = {};
			lAsteroidDescr_obj.asteroid = lAsteroidView_btrav;
			lAsteroidDescr_obj.paralax  = 0.9 + (Math.random() * 1.9);
			lAsteroidDescr_obj.lastInitialXOffset = this.getTileWidth();
			lAsteroidDescr_obj.lastInitialYOffset = -this.getTileHeight();
			lAsteroidDescr_obj.lastStartDelay = 0;

			l_arr.push(lAsteroidDescr_obj);
		}
	}
	//...OVERRIDE

	crash()
	{
		this._smoothRoundDuration.isActive = false;
	}

	_getSmoothRound(multiplierRoundDuration_int)
	{
		if(multiplierRoundDuration_int == 0) {
			this._smoothRoundDuration.isActive = true;
			this._smoothRoundDuration.multiplierRoundDuration_int = multiplierRoundDuration_int;
			return multiplierRoundDuration_int;
			
		}
		if(!this._smoothRoundDuration.isActive || multiplierRoundDuration_int<1500)
		{
			return multiplierRoundDuration_int;
		}
		const dif = multiplierRoundDuration_int - this._smoothRoundDuration.multiplierRoundDuration_int;
		if( dif < 12 && this._smoothRoundDuration.isActive)
		{
			const add = 12 - dif;
			this._smoothRoundDuration.multiplierRoundDuration_int = multiplierRoundDuration_int + add;
			return this._smoothRoundDuration.multiplierRoundDuration_int ;			
		}
		this._smoothRoundDuration.multiplierRoundDuration_int = multiplierRoundDuration_int;
		return this._smoothRoundDuration.multiplierRoundDuration_int;

	}

	//OVERRIDE...
	adjust()
	{
		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let removeArray = [];


		let lCurRoundDuration_num = this._getSmoothRound(l_gpi.multiplierRoundDuration);
		
		if (lRoundInfo_ri.isRoundStartTimeDefined && l_gpi.isPreLaunchFlightRequired)
		{
			let lRestTime_num = l_gpi.multiplierChangeFlightRestTime;
			const lPreLaunchAsteroidsDuration_int = 1000;
			if (lRestTime_num > 0)
			{
				if (lRestTime_num <= lPreLaunchAsteroidsDuration_int)
				{
					lCurRoundDuration_num += lPreLaunchAsteroidsDuration_int-lRestTime_num;
				}
			}
			else
			{
				lCurRoundDuration_num += lPreLaunchAsteroidsDuration_int;
			}
		}
				

		this.expandIfRequired(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height);

		for (let i=0; i<this._fAsteroids_arr.length; i++)
		{
			let lAsteroidDescr_obj = this._fAsteroids_arr[i];

			let l_btrav = lAsteroidDescr_obj.asteroid;
			if(l_btrav.rotatable && lRoundInfo_ri.isRoundPlayActive)
			{
				if(l_btrav.rotationDirection == 1)
				{
					l_btrav.rotation+= 0.05;
				}else{
					l_btrav.rotation-= 0.05;
				}
				
			}
			let lParalaxSpeedMultiplier = lAsteroidDescr_obj.paralax;

			if (l_gpv.foregroundContainer && i%2 === 0 && l_btrav.parent === this)
			{
				l_gpv.foregroundContainer.addChild(l_btrav);
			}

			let lAsteroidIndex_num = l_btrav.randomIndex;

			let lIndexRandomValue_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num,true);

			let lAddOffsetXDirection_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num+2,true) > 0.5 ? 1 : -1;
			lAsteroidDescr_obj.lastInitialXOffset = this.getTileWidth() + Math.trunc(lIndexRandomValue_num * this.getTileWidth())*lAddOffsetXDirection_num;
			lAsteroidDescr_obj.lastInitialYOffset = -this.getTileHeight();
			lAsteroidDescr_obj.lastStartDelay = 300*i;

			let lAddOffsetYDirection_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num+1,true) > 0.5 ? 1 : -1;
			if (lAsteroidDescr_obj.lastInitialXOffset < this.getTileWidth())
			{
				lAddOffsetYDirection_num = -1;
			}
			lAsteroidDescr_obj.lastInitialYOffset += Math.min(lIndexRandomValue_num, 0.5)*this.getTileHeight()*lAddOffsetYDirection_num;

			let lPathXLen_num = Math.abs(lAsteroidDescr_obj.lastInitialXOffset);
			let lPathYLen_num = Math.abs(lAsteroidDescr_obj.lastInitialYOffset);
			let lPathLen_num = Math.sqrt(lPathXLen_num*lPathXLen_num + lPathYLen_num*lPathYLen_num)+100;

			let lCycleIndex_int = 0;
			let lPathPercent_num = 0;

			if (lCurRoundDuration_num > lAsteroidDescr_obj.lastStartDelay)
			{
				let lAsteroidMovePathDuration_num = Math.trunc(this._fBasePathDuration_num * (lPathLen_num/this._fBasePathLen_num)) * lParalaxSpeedMultiplier;
				let lAsteroidCurCyclePathDuration_num = (lCurRoundDuration_num-lAsteroidDescr_obj.lastStartDelay)%lAsteroidMovePathDuration_num;

				lPathPercent_num = lAsteroidCurCyclePathDuration_num/lAsteroidMovePathDuration_num;

				lCycleIndex_int = Math.trunc((lCurRoundDuration_num-lAsteroidDescr_obj.lastStartDelay)/lAsteroidMovePathDuration_num);
			}

			let lAngle_num = this._fBaseAngle_num + 10*lIndexRandomValue_num;
			let lPassedPath_num = lPathPercent_num * lPathLen_num;
			let lOffsetX_num = lPassedPath_num * Math.cos(Utils.gradToRad(lAngle_num));
			let lOffsetY_num = lPassedPath_num * Math.sin(Utils.gradToRad(lAngle_num));

			let lBaseX_num = lAsteroidDescr_obj.lastInitialXOffset;
			let lBaseY_num = lAsteroidDescr_obj.lastInitialYOffset;

			let lTargetX_num = lBaseX_num + lOffsetX_num;
			let lTargetY_num = lBaseY_num + lOffsetY_num;

			

			if (l_btrav.parent !== this)
			{
				let lPos_p = this.localToLocal(lTargetX_num, lTargetY_num, l_btrav.parent);
				lTargetX_num = lPos_p.x;
				lTargetY_num = lPos_p.y;
			}
			
			var previusPos = l_btrav.x; 
			l_btrav.position.set(lTargetX_num, lTargetY_num);
			l_btrav.visible = true;

			
			if(previusPos<l_btrav.x)
			{
				l_btrav.randomize(lAsteroidIndex_num+lCycleIndex_int);
			}
		}

		this.position.y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}
	//...OVERRIDE
}
export default BackgroundRandomAsteroidsView;