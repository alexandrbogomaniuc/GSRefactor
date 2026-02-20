import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import StarshipsPoolView from './StarshipsPoolView';
import StarshipTakeOffExplosionView from './starshipEffects/StarshipTakeOffExplosionView';
import StarshipTakeOffSmokeView from './starshipEffects/StarshipTakeOffSmokeView';
import StarshipCrashExplosionView from './starshipEffects/StarshipCrashExplosionView';
import BattlegroundStarshipCrashExplosionView from './starshipEffects/battleground/BattlegroundStarshipCrashExplosionView';
import StarshipHeatPreFlightSmokeAnimation from './starshipEffects/StarshipHeatPreFlightSmokeAnimation';
import GameplayInfo from '../../../model/gameplay/GameplayInfo';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../../config/AtlasConfig';
import MTimeLine from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import GraphView from '../graph/GraphView';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import MAnimation from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';

class StarshipBaseView extends Sprite
{
	static get EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED ()			{ return StarshipTakeOffExplosionView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED; }
	static get EVENT_ON_STARSHIP_CRASH_EXPLOSION_STARTED ()				{ return StarshipCrashExplosionView.EVENT_ON_EXPLOSION_STARTED; }

	constructor()
	{
		super();

		let lGameplayController_gpc = APP.gameController.gameplayController;
		this._fRoundController_rc = APP.gameController.gameplayController.roundController;
		this._fGameplayInfo_gpi = lGameplayController_gpc.info;
		this._fGameplayView_gpv = lGameplayController_gpc.view;
		this._fGraphView_rgv = this._fGameplayView_gpv.graphView;
		this._fContentContainer_sprt = null;
		this._fShakeContainer_sprt = null;
		this._fTakeOffExplosionView_rcstoev = null;
		this._fTakeOffSmokeView_stosv = null;
		this._fFalloutView_rcsfv = null;
		this._fCrashExplosionView_rcscev = null;
		this._fShipFire_sprt = null;

		//CONTENT CONTAINER...
		let l_sprt = new Sprite();
		this._fContentContainer_sprt = this.addChild(l_sprt);
		this._fShakeContainer_sprt = this._fContentContainer_sprt.addChild(new Sprite);
		//...CONTENT CONTAINER

		//ENGINE...
		this._fShipFire_sprt = this._addFire();
		//...ENGINE

		//EFFECTS...
		//FALLOUT EFFECT...
		let l_rcsfv = this.generateFalloutEffect();
		this._fFalloutView_rcsfv = l_rcsfv;
		this.addChild(l_rcsfv);
		//...FALLOUT EFFECT

		//CRASH EXPLOSION...
		let l_rcscev = this.generateCrashExplosion();
		this._fCrashExplosionView_rcscev = l_rcscev;
		this.addChild(l_rcscev);
		//...CRASH EXPLOSION

		//TAKE OFF EXPLOSION...
		let l_rcstoev = this.generateTakeOffExplosion();
		this._fTakeOffExplosionView_rcstoev = l_rcstoev;
		this._fGameplayView_gpv.starshipForegroundEffectsContainer.addChild(l_rcstoev);
		//...TAKE OFF EXPLOSION
		//...EFFECTS

		//BODY...
		this._fBody_sprt = this._generateStarshipBody();
		this._fBody_sprt.scale.set(0.4);
		this._fShakeContainer_sprt.addChild(this._fBody_sprt);

		//BTG SPEED UP BEFORE EXPLOSION...
		if(APP.isBattlegroundGame)
		{
			let l_rcsuav = this._generateBTGSpeedUpAnimationView();
			this._fBTGSpeedUpView_rcsuav = l_rcsuav;
			this._fShakeContainer_sprt.addChild(l_rcsuav);
		}
		//...BTG SPEED UP BEFORE EXPLOSION

		this._addHeat();

		if (this._fGameplayInfo_gpi.isPreLaunchFlightRequired)
		{
			//TAKE OFF SMOKE...
			let l_stosv = this.generateTakeOffSmoke();
			this._fTakeOffSmokeView_stosv = l_stosv;
			this._fGameplayView_gpv.starshipForegroundEffectsContainer.addChild(l_stosv);
			//...TAKE OFF SMOKE

			this._fHeatPreFlightAnimation_mtl = this._generateHeatPreFlightAnimation();

			this._fStarshipHeatPreFlightSmokeAnimation_hpfsa = this._generateStarshipHeatPreFlightSmokeAnimation();

			this._fShakeContainer_sprt.addChild(this._fStarshipHeatPreFlightSmokeAnimation_hpfsa);

			this._fShakeAnim_mtl = this._generateShipShakeAnimation();
		}

		StarshipsPoolView.registerStarship(this);
	}

	startWiggleStarshipSpeedUp()
	{
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fShakeContainer_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				3,
				[1.8, 4],
				[-1.2, 4],
				[0.4, 4],
				[-0.9, 4],
				[0, 5],
			]);
		l_mtl.play();
	}

	startWiggleStarship()
	{
		let l_mtl = new MTimeLine();
		l_mtl.addAnimation(
			this._fShakeContainer_sprt,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				[-0.9, 1],
				[2.3, 2],
				[-0.4, 1],
				[1.1, 2],
				[-0.5, 2],
				[0, 2],
			]);
		l_mtl.play();
	}

	_generateStarshipBody()
	{
		return new Sprite;
	}

	get _preLaunchOffsetPoint()
	{
		return {x: 0, y: 0};
	}

	_addFire()
	{
		let lFire_sprt = this._generateFireView();
		lFire_sprt.position.set(-4, 65);
		
		this._fShakeContainer_sprt.addChild(lFire_sprt);

		return lFire_sprt;
	}

	_generateFireView()
	{
		let lFire_sprt = Sprite.createMultiframesSprite(StarshipBaseView.getFireTextures());
		lFire_sprt.anchor.set(0.49, 0.15);
		lFire_sprt.scale.set(2);
		lFire_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		lFire_sprt.play();

		return lFire_sprt;
	}

	//CONTENT CONTAINER...
	getContentContainer()
	{
		return this._fContentContainer_sprt;
	}
	//...CONTENT CONTAINER

	//TAKE OFF EXPLOSION...
	generateTakeOffExplosion()
	{
		let l_stoev = new StarshipTakeOffExplosionView();
		l_stoev.on(StarshipTakeOffExplosionView.EVENT_ON_STARSHIP_TAKE_OFF_EXPLOSION_STARTED, this.emit, this);

		return l_stoev;
	}
	//...TAKE OFF EXPLOSION

	//TAKE OFF EXPLOSION...
	generateTakeOffSmoke()
	{
		let l_stosv = new StarshipTakeOffSmokeView();

		return l_stosv;
	}
	//...TAKE OFF EXPLOSION

	//FALLOUT EFFECT...
	generateFalloutEffect()
	{
		return new Sprite();
	}

	getFalloutEffect()
	{
		return this._fFalloutView_rcsfv;
	}
	//...FALLOUT EFFECT

	//CRASH EXPLOSION...
	generateCrashExplosion()
	{
		if (APP.isBattlegroundGame)
		{
			let l_scev = new BattlegroundStarshipCrashExplosionView();
			l_scev.on(BattlegroundStarshipCrashExplosionView.EVENT_ON_EXPLOSION_STARTED, this.emit, this);
			return l_scev;
		}
		else
		{
			let l_scev = new StarshipCrashExplosionView();
			l_scev.on(StarshipCrashExplosionView.EVENT_ON_EXPLOSION_STARTED, this.emit, this);
			return l_scev;
		}
	}

	getCrashExplosion()
	{
		return this._fCrashExplosionView_rcscev;
	}
	//...CRASH EXPLOSION

	//BTG SPEED UP BEFORE EXPLOSION...
	_generateBTGSpeedUpAnimationView()
	{
		//override
		return new Sprite;
	}
	//...BTG SPEED UP BEFORE EXPLOSION

	getTakeOffSpeedMultiplier()
	{
		let l_gpi = this._fGameplayInfo_gpi;

		return l_gpi.isPreLaunchFlightRequired ? 1 : 3;
	}

	_addHeat()
	{
		this._fHeatLoopAnimation_mtl = null;

		//HEAT...
		let lHeat_sprt = this._fHeat_sprt = this._generateHeat();
		this._fShakeContainer_sprt.addChild(lHeat_sprt);
		//...HEAT

		//ANIMATION...
		let l_mtl = this._generateHeatIdleAnimation();
		this._fHeatLoopAnimation_mtl = l_mtl;
		//...ANIMATION
	}

	_generateHeat()
	{
		return new Sprite;
	}

	_generateHeatIdleAnimation()
	{
		return new MTimeLine;
	}

	_generateHeatPreFlightAnimation()
	{
		let l_mtl = new MTimeLine;
		let lMult_num = APP.isBattlegroundGame ? 1 : 2;

		let lHeatAnim_arr = [
				[1, 2*lMult_num],
				[0, 6*lMult_num],
				12*lMult_num,
				[1, 1*lMult_num],
				[0, 3*lMult_num],
				[1, 1*lMult_num],
				[0, 6*lMult_num],
				7*lMult_num,
				[1, 2*lMult_num]
				
			];

		if (APP.isBattlegroundGame)
		{
			lHeatAnim_arr.splice(0, 3);
		}

		l_mtl.addAnimation(
			this._fHeat_sprt,
			MTimeLine.SET_ALPHA,
			0,
			lHeatAnim_arr);

		let lShipFireAnim_arr = [
			20*lMult_num,
			[1, 1*lMult_num],
			[0, 3*lMult_num],
			[1, 1*lMult_num],
			[0, 6*lMult_num],
			7*lMult_num,
			[1, 2*lMult_num]
		]

		if (APP.isBattlegroundGame)
		{
			lShipFireAnim_arr.splice(0, 1);
		}

		l_mtl.addAnimation(
			this._fShipFire_sprt,
			MTimeLine.SET_ALPHA,
			0,
			lShipFireAnim_arr);

		return l_mtl;
	}

	_generateStarshipHeatPreFlightSmokeAnimation()
	{
		return new StarshipHeatPreFlightSmokeAnimation();
	}

	_generateShipShakeAnimation()
	{
		let l_mtl = new MTimeLine;

		l_mtl.addAnimation(
			this._fShakeContainer_sprt,
			MTimeLine.SET_X,
			0,
			[
				[-1.5, 1*2],
				[1.5, 1*2]
			]);

		return l_mtl;
	}

	adjust()
	{
		let l_gpi = this._fGameplayInfo_gpi;
		let l_gpv = this._fGameplayView_gpv;
		let lRoundInfo_ri = l_gpi.roundInfo;
		let lCurGameplayTime_num = l_gpi.gameplayTime;
		let lIsPreLaunchFlightMode_bl = l_gpi.isPreLaunchFlightRequired;

		this._fContentContainer_sprt.visible = (lIsPreLaunchFlightMode_bl || l_gpi.multiplierRoundDuration > 0)
												&& (lRoundInfo_ri.isRoundWaitState || lRoundInfo_ri.isRoundPlayActive)
												&& (!APP.isBattlegroundGame || !l_gpv.battlegroundYouWonView.isAnimationInProgress);

		if(APP.forcedState == "WAIT" ||  APP.forcedState == "PLAY")
		{
			this._fContentContainer_sprt.visible = true;
		}										

		
		let lRoundMillisecondIndex_int = l_gpi.isPreLaunchTimePeriod ? -l_gpi.multiplierChangeFlightRestTime : l_gpi.multiplierRoundDuration;
		let lCurrentMultiplier_num = l_gpi.isPreLaunchTimePeriod ? l_gpi.calculateMultiplier(lRoundMillisecondIndex_int) : l_gpi.multiplierValue;
		let lAngle_num = 0;

		if (lIsPreLaunchFlightMode_bl)
		{
			if (l_gpi.multiplierChangeFlightRestTime > l_gpi.preLaunchFlightDuration)
			{
				lCurrentMultiplier_num = 0;
				lRoundMillisecondIndex_int = -l_gpi.preLaunchFlightDuration;
				lAngle_num = Utils.gradToRad(0);
			}
			else
			{
				const STRIGHT_DURATION_MS = 1500;
				const PRELAUNCH_MAX_ANGLE = 40;
				const MAX_ANGLE = 52;
				const lPrelaunchTurnDuration_num = l_gpi.preLaunchFlightDuration-STRIGHT_DURATION_MS;
				const FLIGHT_FULL_TURN_DURATION = 1500;
				let lPercent_num = 0;
				let lAngleGrad_num = 0;

				if (lRoundMillisecondIndex_int > 0)
				{
					lPercent_num = Math.min(lRoundMillisecondIndex_int/FLIGHT_FULL_TURN_DURATION, 1);
					lAngleGrad_num = PRELAUNCH_MAX_ANGLE + MAnimation.getEasingMultiplier(MAnimation.EASE_OUT, lPercent_num)*(MAX_ANGLE-PRELAUNCH_MAX_ANGLE);
				}
				else if (l_gpi.multiplierChangeFlightRestTime < lPrelaunchTurnDuration_num)
				{
					lPercent_num = Math.min((lPrelaunchTurnDuration_num-l_gpi.multiplierChangeFlightRestTime)/lPrelaunchTurnDuration_num, 1);
					lAngleGrad_num = MAnimation.getEasingMultiplier(MAnimation.EASE_IN, lPercent_num)*PRELAUNCH_MAX_ANGLE;
				}
				
				lAngle_num = Utils.gradToRad(lAngleGrad_num);
			}
		}
		else
		{
			let lX_num = this._fGraphView_rgv.getCorrespondentCoordinateX(lRoundMillisecondIndex_int);
			let lY_num = this._fGraphView_rgv.getCorrespondentCoordinateY(lCurrentMultiplier_num);

			let lRoundPreviousMillisecondIndex_int = lRoundMillisecondIndex_int - 1000;
			let lRoundPreviousMillisecondMultiplier_num = l_gpi.calculateMultiplier(lRoundPreviousMillisecondIndex_int);
			let lPreviousMillisecondMatchingCoordinateX_num = this._fGraphView_rgv.getCorrespondentCoordinateX(lRoundPreviousMillisecondIndex_int);
			let lPreviousMillisecondMatchingCoordinateY_num = this._fGraphView_rgv.getCorrespondentCoordinateY(lRoundPreviousMillisecondMultiplier_num);

			let lDeltaX_num = lPreviousMillisecondMatchingCoordinateX_num - lX_num;
			let lDeltaY_num = lPreviousMillisecondMatchingCoordinateY_num - lY_num;

			lAngle_num = -Math.atan2(lDeltaX_num, lDeltaY_num) || Utils.gradToRad(90);
		}
		this.rotation = lAngle_num;

		if (APP.isBattlegroundGame)
		{
			this._fCrashExplosionView_rcscev.rotation = - lAngle_num;
		}
		let lShipX_num = this._fGraphView_rgv.getCorrespondentCoordinateX(lRoundMillisecondIndex_int * this.getTakeOffSpeedMultiplier());
		let lShipY_num = this._fGraphView_rgv.getMatchingCoordinateYAccordingMockapInitialSettings(lCurrentMultiplier_num);

		//console.log("current multi value " + lCurrentMultiplier_num);
		if (lShipX_num > 180  && lCurrentMultiplier_num > 0)
		{
			lShipX_num = 180; 
		}

		if (lShipY_num < 380  && lCurrentMultiplier_num > 0)
		{
			lShipY_num = 380;
		}
		this.position.set(lShipX_num, lShipY_num);

		let lZoomScale_num = l_gpv.getCorrespondentZoomOutScale(lRoundMillisecondIndex_int);
		this._fContentContainer_sprt.scale.set(lZoomScale_num);

		let lZoomScaleIn_num = 1;

		if (APP.isBattlegroundGame)
		{
			lZoomScaleIn_num = l_gpv.getCorrespondentZoomInScale(lRoundMillisecondIndex_int);
			this._fShakeContainer_sprt.scale.set(lZoomScaleIn_num);
		}

		let lOffsetPoint_p = this._preLaunchOffsetPoint;
		let dx_num = Math.abs(lOffsetPoint_p.x)*(lZoomScale_num-1);
		let dy_num = -Math.abs(lOffsetPoint_p.y)*(lZoomScale_num-1);
		this._fContentContainer_sprt.position.set(dx_num, dy_num);

		this.activateShip();

		this._fFalloutView_rcsfv.adjust();
		this._fCrashExplosionView_rcscev.adjust();
		if (APP.isBattlegroundGame)
		{
			this._fBTGSpeedUpView_rcsuav.adjust();
		}
		if (lZoomScale_num > 1)
		{
			this._fTakeOffExplosionView_rcstoev.adjust(lZoomScale_num, lOffsetPoint_p.x, lOffsetPoint_p.y);
		}
		else
		{
			this._fTakeOffExplosionView_rcstoev.adjust(lZoomScaleIn_num);
		}

		this._fTakeOffSmokeView_stosv && this._fTakeOffSmokeView_stosv.adjust();

		if (this._fHeatPreFlightAnimation_mtl && l_gpi.multiplierChangeFlightRestTime > l_gpi.preLaunchFlightDuration)
		{
			this._fHeatLoopAnimation_mtl.stop();
			
			let lHeatPreFlightAnimDuration_num = this._fHeatPreFlightAnimation_mtl.getTotalDurationInMilliseconds();
			let lPreFlightAnimStartTime_int = l_gpi.multiplierChangeFlightStartTime-(l_gpi.preLaunchFlightDuration+lHeatPreFlightAnimDuration_num);
			this._fHeatPreFlightAnimation_mtl.windToMillisecond(lCurGameplayTime_num, lPreFlightAnimStartTime_int);

			if (this._fStarshipHeatPreFlightSmokeAnimation_hpfsa)
			{
				let lSmokePreflightAnimDuration_num = this._fStarshipHeatPreFlightSmokeAnimation_hpfsa.duration*2;
				let lSmokePreFlightAnimStartTime_int = l_gpi.multiplierChangeFlightStartTime-(l_gpi.preLaunchFlightDuration+lSmokePreflightAnimDuration_num);
				let lHeatAnimDuration_num = lCurGameplayTime_num - lSmokePreFlightAnimStartTime_int;
				let lHeatSmokeAnimDuration_num = lHeatAnimDuration_num;
				if (lHeatAnimDuration_num > 1400)
				{
					lHeatSmokeAnimDuration_num -= 1400;
				}
				this._fStarshipHeatPreFlightSmokeAnimation_hpfsa.adjust(lHeatSmokeAnimDuration_num);
				this._fStarshipHeatPreFlightSmokeAnimation_hpfsa.visible = true;
			}
		}
		else
		{
			if (!this._fHeatLoopAnimation_mtl.isPlaying())
			{
				this._fHeatPreFlightAnimation_mtl && this._fHeatPreFlightAnimation_mtl.windToMillisecond(0);
				this._fHeatLoopAnimation_mtl.playLoop();
			}

			this._fShipFire_sprt.alpha = 1;

			if (this._fStarshipHeatPreFlightSmokeAnimation_hpfsa)
			{
				this._fStarshipHeatPreFlightSmokeAnimation_hpfsa.visible = false;
			}
		}

		if (this._fShakeAnim_mtl)
		{
			if (l_gpi.multiplierChangeFlightRestTime > 0 && l_gpi.multiplierChangeFlightRestTime < l_gpi.preLaunchFlightDuration)
			{
				if (!this._fShakeAnim_mtl.isPlaying())
				{
					this._fShakeAnim_mtl.playLoop();
				}
			}
			else
			{
				if (this._fShakeAnim_mtl.isPlaying())
				{
					this._fShakeAnim_mtl.stop();
				}

				this._fShakeContainer_sprt.x = 0;
			}
		}
	}

	activateShip()
	{
		this.visible = true;
		this._fTakeOffExplosionView_rcstoev.visible = this._fContentContainer_sprt.visible;

		if (this._fTakeOffSmokeView_stosv)
		{
			this._fTakeOffSmokeView_stosv.visible = this._fContentContainer_sprt.visible;
		}

		if(APP.isBattlegroundGame)
		{
			this._fBTGSpeedUpView_rcsuav.activateListeners();
		}
	}

	deactivateShip()
	{
		this.visible = false;
		this._fTakeOffExplosionView_rcstoev.visible = false;

		this._fFalloutView_rcsfv.deactivate();
		this._fCrashExplosionView_rcscev.deactivate();
		this._fTakeOffExplosionView_rcstoev.deactivate();

		if (this._fTakeOffSmokeView_stosv)
		{
			this._fTakeOffSmokeView_stosv.visible = false;
			this._fTakeOffSmokeView_stosv.deactivate();
		}

		if(APP.isBattlegroundGame)
		{
			this._fBTGSpeedUpView_rcsuav && this._fBTGSpeedUpView_rcsuav.deactivate();
		}
	}
}

StarshipBaseView.getFireTextures = function()
{
	if (!StarshipBaseView.fire_textures)
	{
		StarshipBaseView.fire_textures = [];

		StarshipBaseView.fire_textures = AtlasSprite.getFrames([APP.library.getAsset('game/ship_fire')], [AtlasConfig.StarshipFire], '');
		StarshipBaseView.fire_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return StarshipBaseView.fire_textures;
}

StarshipBaseView.getShipBodyTextures = function()
{
	if (!StarshipBaseView.ship_textures)
	{
		StarshipBaseView.ship_textures = [];

		StarshipBaseView.ship_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'ship_body');
		StarshipBaseView.ship_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return StarshipBaseView.ship_textures;
}

StarshipBaseView.getShipHeatTextures = function()
{
	if (!StarshipBaseView.heat_textures)
	{
		StarshipBaseView.heat_textures = [];

		StarshipBaseView.heat_textures = AtlasSprite.getFrames([APP.library.getAsset('game/gameplay_assets')], [AtlasConfig.GameplayAssets], 'ship_heat');
		StarshipBaseView.heat_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return StarshipBaseView.heat_textures;
}

StarshipBaseView.getFlameExplosionTextures = function()
{
	if (!StarshipBaseView.flame_explosion_textures)
	{
		StarshipBaseView.flame_explosion_textures = [];

		StarshipBaseView.flame_explosion_textures = AtlasSprite.getFrames([APP.library.getAsset('game/battleground/ship_explosion/flame_explosion')], [AtlasConfig.FlameExplosion], '');
		StarshipBaseView.flame_explosion_textures.sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}

	return StarshipBaseView.flame_explosion_textures;
}

export default StarshipBaseView