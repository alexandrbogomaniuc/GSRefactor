import BackgroundTilesetBaseClassView from './BackgroundTilesetBaseClassView';
import BackgroundTileSummonedAsteroidView from "./tiles/BackgroundTileSummonedAsteroidView";
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { GAME_VIEW_SETTINGS } from '../../main/GameBaseView';
import { Sprite } from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';

const MAX_ASTEROIDS_AMOUNT = 1;


let _fire_explosion_textures = null;

function _generateFireExplosionTextures()
{
	//here
	if (_fire_explosion_textures) return;
	_fire_explosion_textures = AtlasSprite.getFrames([APP.library.getAsset("game/battleground/fire_explosion/fire_explosion_texture")], [AtlasConfig.FireExplosionAnimation], "");
							   
}

class BackgroundSummonedAsteroidsView extends BackgroundTilesetBaseClassView
{
	constructor()
	{
		super();
	

		//[this.getTileWidth() - Math.round(Math.random() * (this.getTileWidth()/2.5)), this.getTileHeight()* 1.2]
		
		this._container = this.addChild(new Sprite());
		this._fAsteroids_arr = [];1
		this.to_summon_bg_elements = ["0","1","2"];
		this.to_summon_bg_elements = this.shuffleArray([...this.to_summon_bg_elements]);
		this.summoned_bg_elements = -1;

		this._fBasePathLen_num = Math.sqrt(this.getTileWidth()*this.getTileWidth() + this.getTileHeight()*this.getTileHeight());
		this._fBasePathDuration_num = 1000;
		this._fBaseAngle_num = 150;
		this._pathOfset = 100;
		this._smoothRoundDuration = {isActive:false, multiplierRoundDuration_int:0};

	}

	getStartingPosition(){

		let topPattern = Math.round( Math.random() * 15000) % 2 == 0;
		
		if(topPattern){
			return [this.getTileWidth() - Math.round(Math.random() * (this.getTileWidth()/2.2)), this.getTileHeight() * 1.2];
		}else{
			return [this.getTileWidth() * 1.2,  this.getTileHeight() - Math.round(Math.random() * this.getTileHeight()/2.2)];
		}
	}

	
	getRandomSlowDownPoint(){

		const optionsArray = [[0.7,0.3],[0.6,0.4],[0.8,0.2],[0.5,0.5],[0.1,0.9],[0.2,0.8],[0.05,0.95],[0.04,0.96],[0.04,0.96],[0.04,0.96],[0.04,0.96],[0.04,0.96]];
		const randomIndex = Math.floor(Math.random() * optionsArray.length);
		//const randomIndex = 7;
		return optionsArray[randomIndex];
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
	generateTileView(aIndex_int, type, asset)
	{
		let l_btrav = new BackgroundTileSummonedAsteroidView(aIndex_int,type,asset);
		l_btrav.passedPathPercent = 0; 
		l_btrav.speed = this._fBasePathDuration_num
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
	expandIfRequired(aWidthInPixels_num, aHeightInPixels_num, lCurRoundDuration_num=0)
	{
        let l_gpi = APP.gameController.gameplayController.info;
        if(!l_gpi.newAsteroid) return;

		const multiplierDif = l_gpi.multiplierValue - l_gpi.newAsteroid.currentMult; 
		let timeDilation = 0;

		//console.log("MultilierDIf " + multiplierDif);
		if(multiplierDif< 1 && multiplierDif > 0.01)
		{
		
			timeDilation = (multiplierDif * this._multiplierUpdateRate);
			//console.log("TimeDilation " + timeDilation)
		}


		//console.log("new Asteroid type = " + l_gpi.newAsteroid.type + " currentMult :" + l_gpi.newAsteroid.currentMult + " multiplierUpdateRate:" + this._multiplierUpdateRate + " arrived at mult " + l_gpi.multiplierValue + " = delayed for " + multiplierDif + " time dilation " + timeDilation);
		

		//{type:data.asteroid.type, dateDif:dataDif, totalTime: data.asteroid.speed * 900, startXPercent:data.asteroid.x, startYPercent:data.asteroid.y, slowDownAt:data.asteroid.slow};
        let type = l_gpi.newAsteroid.type;
		const dateDif  = l_gpi.newAsteroid.dateDif;
		//console.log("new Asteroid type = " + l_gpi.newAsteroid.type + " time " + l_gpi.newAsteroid.totalTime );
		const totalTime = l_gpi.newAsteroid.totalTime - timeDilation;
		const startXPercent = l_gpi.newAsteroid.startXPercent;
		const startYPercent = l_gpi.newAsteroid.startYPercent;
		const slowDownAt = l_gpi.newAsteroid.slowDownAt;
		const roundId = l_gpi.newAsteroid.roundId;
        l_gpi.newAsteroid = false;
		if(roundId != APP.roundId)
		{
			return;
		}

		let asset = null;
		if(type != 10 && type != 11 && type != 12)
		{
			this.summoned_bg_elements++;
			asset = this.to_summon_bg_elements[this.summoned_bg_elements];
			if(!asset) return;
		}

        let l_arr = this._fAsteroids_arr;
		for (let i=l_arr.length; i<MAX_ASTEROIDS_AMOUNT; i++)
		{
            let lAsteroidView_btrav = this.generateTileView(i, type, asset);

            let lContainer_sprt = this._container;
            lContainer_sprt.addChild(lAsteroidView_btrav);
            let lAsteroidDescr_obj = {};
            lAsteroidDescr_obj.asteroid = lAsteroidView_btrav;
            lAsteroidDescr_obj.lastInitialXOffset = this.getTileWidth();
            lAsteroidDescr_obj.lastInitialYOffset = -this.getTileHeight();
            lAsteroidDescr_obj.lastStartDelay = lCurRoundDuration_num;
			lAsteroidDescr_obj.asset = asset;
			lAsteroidDescr_obj.totalTime = totalTime;
			lAsteroidDescr_obj.startXPercent = startXPercent;
			lAsteroidDescr_obj.startYPercent = startYPercent;
			lAsteroidDescr_obj.slowDownAt = slowDownAt;
            l_arr.push(lAsteroidDescr_obj);
		}
		
	}

	crash()
	{
		this._smoothRoundDuration.isActive = false;
	}
	//...OVERRIDE

	//OVERRIDE...
	adjust()
	{

		let l_gpi = APP.gameController.gameplayController.info;
		let l_gpv = APP.gameController.gameplayController.view;
		let lRoundInfo_ri = l_gpi.roundInfo;

		if(lRoundInfo_ri.isRoundPlayState)
		{
			if(this._previousDate == null)
			{
				
				this._previousDate = Date.now();
				this._previousMultiplier = l_gpi.multiplierValue;
			}

			if(l_gpi.multiplierValue - this._previousMultiplier >=0.01)
			{
				const nowDate = Date.now();				
				this._multiplierUpdateRate = nowDate - this._previousDate;
				this._previousMultiplier = l_gpi.multiplierValue;
				this._previousDate = nowDate;
			}
			this._container.visible = true; 
			this.visible = true;
		}else{
			this._previousDate = null;
			this._container.visible = false;
			this.visible = false; 
			if(this._l_fireExplosion)
			{
				this._l_fireExplosion.visible = false;
			}
		}
		
		let lCurRoundDuration_num = l_gpi.multiplierRoundDuration;
		let lCurRoundDuration_num_smooth = this._getSmoothRound(l_gpi.multiplierRoundDuration, this._fAsteroids_arr.length == 0);
		
		if (lRoundInfo_ri.isRoundStartTimeDefined && l_gpi.isPreLaunchFlightRequired)
		{
			let lRestTime_num = l_gpi.multiplierChangeFlightRestTime;
			const lPreLaunchAsteroidsDuration_int = 1000;
			if (lRestTime_num > 0)
			{
				if (lRestTime_num <= lPreLaunchAsteroidsDuration_int)
				{
					lCurRoundDuration_num += lPreLaunchAsteroidsDuration_int-lRestTime_num;
					lCurRoundDuration_num_smooth += lPreLaunchAsteroidsDuration_int-lRestTime_num;
				}
			}
			else
			{
				lCurRoundDuration_num += lPreLaunchAsteroidsDuration_int;
				lCurRoundDuration_num_smooth+= lPreLaunchAsteroidsDuration_int;
			}
		}
		
		let lMultiplierDelta_num = (l_gpi.multiplierValue - l_gpi.minMultiplierValue);
		

		this.expandIfRequired(GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.width, GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height,lCurRoundDuration_num);

		for (let i=0; i<this._fAsteroids_arr.length; i++)
		{
			let lAsteroidDescr_obj = this._fAsteroids_arr[i];
			if(lAsteroidDescr_obj.asset)
			{
				let l_btrav = lAsteroidDescr_obj.asteroid;
				let lParalaxSpeedMultiplier = l_btrav.paralaxMultiplier;

				if (l_gpv.foregroundContainer && i%2 === 0 && l_btrav.parent === this)
				{
					l_gpv.foregroundContainer.addChild(l_btrav);
				}

				let lAsteroidIndex_num = l_btrav.randomIndex;
				let lIndexRandomValue_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num);

				let lAddOffsetXDirection_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num+2) > 0.5 ? 10 : -10;
				lAsteroidDescr_obj.lastInitialXOffset = this.getTileWidth() + Math.trunc(lIndexRandomValue_num * this.getTileWidth())*lAddOffsetXDirection_num;
				lAsteroidDescr_obj.lastInitialYOffset = -this.getTileHeight();
				//lAsteroidDescr_obj.lastStartDelay = 300*i;

				let lAddOffsetYDirection_num = l_gpi.getPseudoRandomValue(lAsteroidIndex_num+1) > 0.5 ? 1 : -1;
				if (lAsteroidDescr_obj.lastInitialXOffset < this.getTileWidth())
				{
					lAddOffsetYDirection_num = -1;
				}
				lAsteroidDescr_obj.lastInitialYOffset += Math.min(lIndexRandomValue_num, 0.5)*this.getTileHeight()*lAddOffsetYDirection_num;

				let lPathXLen_num = Math.abs(lAsteroidDescr_obj.lastInitialXOffset);
				let lPathYLen_num = Math.abs(lAsteroidDescr_obj.lastInitialYOffset);
				let lPathLen_num = Math.sqrt(lPathXLen_num*lPathXLen_num + lPathYLen_num*lPathYLen_num)+this._pathOfset;

				let lCycleIndex_int = 0;
				let lPathPercent_num = 0;

				if (lCurRoundDuration_num_smooth > lAsteroidDescr_obj.lastStartDelay)
				{
					let lAsteroidMovePathDuration_num = Math.trunc(this._fBasePathDuration_num * (lPathLen_num/this._fBasePathLen_num)) * lParalaxSpeedMultiplier;
					let lAsteroidCurCyclePathDuration_num = (lCurRoundDuration_num_smooth-lAsteroidDescr_obj.lastStartDelay)%lAsteroidMovePathDuration_num;

					lPathPercent_num = lAsteroidCurCyclePathDuration_num/lAsteroidMovePathDuration_num;
					lCycleIndex_int = Math.trunc((lCurRoundDuration_num_smooth-lAsteroidDescr_obj.lastStartDelay)/lAsteroidMovePathDuration_num);
				}

				let lAngle_num = this._fBaseAngle_num + 10 * lIndexRandomValue_num;
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


				l_btrav.position.set(lTargetX_num, lTargetY_num);
				l_btrav.visible = true;

				if(lPathPercent_num>0.95)
				{
					l_btrav.parent.removeChild(l_btrav);
					this._fAsteroids_arr.splice(i,1);
					
				}
			}else{
				
				const slowDownFactor = 0.3; 

				if(!lAsteroidDescr_obj.startX)
				{
					const randomSlowDownPoint = this.getRandomSlowDownPoint();
					const firstPath = lAsteroidDescr_obj.slowDownAt; 
					const secondPath = 1 - lAsteroidDescr_obj.slowDownAt;
					const start_position = this.getStartingPosition();
					lAsteroidDescr_obj.startX = this.getTileWidth() * lAsteroidDescr_obj.startXPercent;
					lAsteroidDescr_obj.startY = this.getTileHeight() - this.getTileHeight() * lAsteroidDescr_obj.startYPercent;
					lAsteroidDescr_obj.endX = 150;
					lAsteroidDescr_obj.endY = 160;
					lAsteroidDescr_obj.deltaX = lAsteroidDescr_obj.endX - lAsteroidDescr_obj.startX;
					lAsteroidDescr_obj.deltaY = lAsteroidDescr_obj.endY - lAsteroidDescr_obj.startY;
					lAsteroidDescr_obj.totalTIme = lAsteroidDescr_obj.totalTime-200; 
					lAsteroidDescr_obj.T1 = firstPath * lAsteroidDescr_obj.totalTIme;
					lAsteroidDescr_obj.T2 = secondPath * lAsteroidDescr_obj.totalTIme;
					lAsteroidDescr_obj.denominator = lAsteroidDescr_obj.T1 + slowDownFactor * lAsteroidDescr_obj.T2;
					lAsteroidDescr_obj.VX = lAsteroidDescr_obj.deltaX / lAsteroidDescr_obj.denominator;
					lAsteroidDescr_obj.VY = lAsteroidDescr_obj.deltaY / lAsteroidDescr_obj.denominator;
					lAsteroidDescr_obj.startTime = Date.now();
				}

				let l_btrav = lAsteroidDescr_obj.asteroid;
				let newX, newY; 
				let elapsed = Date.now() - lAsteroidDescr_obj.startTime;
				
				if(elapsed < lAsteroidDescr_obj.T1)
				{
					newX = lAsteroidDescr_obj.startX + lAsteroidDescr_obj.VX * elapsed; 
					newY = lAsteroidDescr_obj.startY + lAsteroidDescr_obj.VY * elapsed; 

				}else if(elapsed <= lAsteroidDescr_obj.totalTIme)
				{
					let t = elapsed - lAsteroidDescr_obj.T1;
					newX = lAsteroidDescr_obj.startX + lAsteroidDescr_obj.VX * lAsteroidDescr_obj.T1 + lAsteroidDescr_obj.VX * slowDownFactor * t;
					newY = lAsteroidDescr_obj.startY + lAsteroidDescr_obj.VY * lAsteroidDescr_obj.T1 + lAsteroidDescr_obj.VY * slowDownFactor * t;

				}else
				{
					newX = lAsteroidDescr_obj.endX;
					newY = lAsteroidDescr_obj.endY;
					let _metheroData = {parent:l_btrav.parent, x:l_btrav.x, y:l_btrav.y}; 
					l_btrav.parent.removeChild(l_btrav);
					this._fAsteroids_arr.splice(i,1);
					_generateFireExplosionTextures();

					if(!this._l_fireExplosion)
					{
						let l_fireExplosion = this._l_fireExplosion = new Sprite();
						l_fireExplosion.textures = _fire_explosion_textures;
						l_fireExplosion.animationSpeed = 18/60;
						l_fireExplosion.blendMode = PIXI.BLEND_MODES.ADD;
						l_fireExplosion.scale.set(2);
						l_fireExplosion.play();
						this.addChild(l_fireExplosion);
						l_fireExplosion.x = _metheroData.x;
						l_fireExplosion.y = _metheroData.y;
	
						l_fireExplosion.once('animationend', (e) => {
							l_fireExplosion.stop();
							l_fireExplosion.parent.removeChild(l_fireExplosion);
							this._l_fireExplosion = null;
						});
					}
				}

				l_btrav.x = newX;
				l_btrav.y = newY * -1;
				l_btrav.scale.set(1);

				l_btrav.visible = true;
			}
		}

		this.position.y = GAME_VIEW_SETTINGS.GAMEPLAY_ZONE.height;
	}

	_getSmoothRound(multiplierRoundDuration_int, ignore)
	{
		if(multiplierRoundDuration_int == 0) {
			this._smoothRoundDuration.isActive = true;
			return multiplierRoundDuration_int;
		}

		if(!this._smoothRoundDuration.isActive )return multiplierRoundDuration_int;

		

		const dif = multiplierRoundDuration_int - this._smoothRoundDuration.multiplierRoundDuration_int;

		if( dif < 12 && this._smoothRoundDuration.isActive && !ignore)
		{
			const add = 12 - dif;
			this._smoothRoundDuration.multiplierRoundDuration_int = multiplierRoundDuration_int;
			console.log("AsteroidProblem - modified " + dif );
			return this._smoothRoundDuration.multiplierRoundDuration_int + add;
		}
		this._smoothRoundDuration.multiplierRoundDuration_int = multiplierRoundDuration_int;
		return this._smoothRoundDuration.multiplierRoundDuration_int;

	}

	
	//...OVERRIDE

	endOfGame()
	{
		this.summoned_bg_elements = -1;
		for (let i=0; i<this._fAsteroids_arr.length; i++)
		{
			let lAsteroidDescr_obj = this._fAsteroids_arr[i];
			let l_btrav = lAsteroidDescr_obj.asteroid;
			l_btrav.parent.removeChild(l_btrav);
			
		}
		this._fAsteroids_arr = [];
		this.to_summon_bg_elements = this.shuffleArray([...this.to_summon_bg_elements]);
		this.summoned_bg_elements = -1;
		
	}
	
	shuffleArray(array) {
		for (let i = array.length - 1; i > 0; i--) {
		  const j = Math.floor(Math.random() * (i + 1)); // random index od 0 do i
		  [array[i], array[j]] = [array[j], array[i]];   // zameni elemente
		}
		return array;
	  }

}
export default BackgroundSummonedAsteroidsView;