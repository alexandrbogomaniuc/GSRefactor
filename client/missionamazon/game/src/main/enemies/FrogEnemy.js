import BabyFrogEnemy from './BabyFrogEnemy';
import Enemy, { DIRECTION, STATE_WALK, STATE_TURN } from './Enemy';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { GlowFilter } from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import AtlasSprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import AtlasConfig from '../../config/AtlasConfig';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import DeathFxAnimation from '../animation/death/DeathFxAnimation';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { FRAME_RATE } from '../../../../shared/src/CommonConstants';

const HIT_SPINE_SPEED = 0.9;
const EXPLODE_SPINE_SPEED = 1.2;

const PUFFS = [
	{id: 1, x: 17, y: 17, scale: {x: 0.79, y: -0.59}, angle: 23, blendMode: PIXI.BLEND_MODES.SCREEN},
	{id: 2, x: -11, y: -14, scale: {x: 1.09, y: 1.09}, angle: -13, blendMode: PIXI.BLEND_MODES.SCREEN},
	{id: 3, x: -6, y: -2, scale: {x: 0.89, y: -0.89}, angle: -13, blendMode: PIXI.BLEND_MODES.SCREEN}
]


class FrogEnemy extends BabyFrogEnemy {

	get isBabyFrog()
	{
		return false;
	}

	constructor(aParams_obj)
	{
		super(aParams_obj);

		this._fPuffs_sprt_arr = [];
		this._fHighlightedSprite_sprt = null;
		this._fTremblingSequence_seq = null;
		this._fBlueSmokes_spr_arr = [];
	}

	// the diference between current time and closest point of trajectory
	// it needs for tiny toads' trajectory correction
	i_getTrajectoryPointsDiff()
	{
		if (!this.trajectory || !this.trajectory.points || !this.trajectory.points.length) return 0;

		let lCurrentTime_num = APP.gameScreen.currentTime;
		let lPoints_arr = this.trajectory.points;

		for (let i = 1; i < lPoints_arr.length; i++)
		{
			if (lPoints_arr[i].time >= lCurrentTime_num && lPoints_arr[i+1])
			{
				return lCurrentTime_num - lPoints_arr[i].time;
			}
		}
		return null;
	}

	//override
	get _maxJumpDistance()
	{
		return 138+20;
	}

	//override
	getScaleCoefficient()
	{
		return 0.33;
	}

	_getSpineViewOffset()
	{
		return {x:0, y: -10};
	}

	getLocalCenterOffset()
	{
		if (!this._fLocalCenterOffset_obj)
		{
			this._fLocalCenterOffset_obj = this._resetLocalCenterOffset();
		}
		switch (this.state)
		{
			case STATE_WALK:
				this._fLocalCenterOffset_obj = this._calcLocalCenterOffset();
				break;
			case STATE_TURN:
				this._fLocalCenterOffset_obj = this._resetLocalCenterOffset();
				break;
		}
		return this._fLocalCenterOffset_obj;
	}

	_resetLocalCenterOffset()
	{
		return {x: 0, y: -20};
	}

	_calcLocalCenterOffset()
	{
		if (!this.spineView) return {x: 0, y: 0};

		//update current offset point
		let lIsYOffsetNeeded_bln = Boolean(this.direction == DIRECTION.RIGHT_UP || this.direction == DIRECTION.LEFT_UP);
		let lYOffset_num = this._resetLocalCenterOffset().y;

		let lXOffset_num = 0;
		switch (this.direction)
		{
			case DIRECTION.RIGHT_UP:
				lXOffset_num = 25;
				break;
			case DIRECTION.RIGHT_DOWN:
				lXOffset_num = 30;
				break;
			case DIRECTION.LEFT_UP:
				lXOffset_num = -15;
				break;
		}

		if (lIsYOffsetNeeded_bln)
		{
			lYOffset_num = -40;
		}

		return {x: lXOffset_num, y: lYOffset_num};
	}

	_getHitRectWidth()
	{
		return 90;
	}

	_getHitRectHeight()
	{
		return 80;
	}

	setDeath(aIsInstantKill_bl = false)
	{
		if (this.state == STATE_TURN)
		{
			this.spineView.removeAllListeners();
			this.spineView.clearStateListeners();
			this.spineView.view.state.onComplete = null;
			this.stateListener = null;

			this.spineView.view.state.onComplete = () => {
				this.setWalk();
				super.setDeath(aIsInstantKill_bl);
			}
		}
		else
		{
			super.setDeath(aIsInstantKill_bl);
		}
	}

	setDeathFramesAnimation(aIsInstantKill_bl = false)
	{
		if (aIsInstantKill_bl)
		{
			super.setDeathFramesAnimation(aIsInstantKill_bl);
		}
		else
		{
			this._deathInProgress = true;

			let lEnemyPosition_pt = this.getGlobalPosition();
			lEnemyPosition_pt.x += this.getCurrentFootPointPosition().x;
			lEnemyPosition_pt.y += this.getCurrentFootPointPosition().y;
			this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_STARTED, {position: lEnemyPosition_pt, angle: this.angle});

			try //[Y] for debug reasons
			{
				this.startHitDeathAnimation();
			}
			catch (e)
			{
				console.error(e);
			}
		}
	}

	startHitDeathAnimation()
	{
		if (this.spineView.view && this.spineView.view.state)
		{
			this.spineView.removeAllListeners();
			this.spineView.clearStateListeners();
			this.spineView.view.state.onComplete = null;
			this.stateListener = null;

			let animationName = this._calcAnimationName(this.direction, "hit");
			this.spineView.setAnimationByName(0, animationName, false);
			this.spineView.view.state.onComplete = () => {
				try //[Y] for debug reasons
				{
					this._setFinalDeathExplosion();
				}
				catch (e)
				{
					console.error(e);
				}
			}

			this.moveTo(this.x, this.y - this._fJumpOffsetY_num, 200, Easing.sine.easeIn, null, () => {
				this.changeZindex();
			});

			this.shadow.moveTo(this.shadow.x, this.shadow.y + this._fJumpOffsetY_num, 200, Easing.sine.easeIn);

			this.spineView.view.state.timeScale = HIT_SPINE_SPEED;
			this.spineView.play();
		}
	}

	_setFinalDeathExplosion()
	{
		//this.enemyIndicatorsController && this.enemyIndicatorsController.disableHPBar();

		if (this.spineView.view && this.spineView.view.state)
		{
			this.spineView.removeAllListeners();
			this.spineView.clearStateListeners();
			this.spineView.view.state.onComplete = null;
			this.stateListener = null;

			this._startTrembling();

			let animationName = this._calcAnimationName(this.direction, "explode");
			this.spineView.setAnimationByName(0, animationName, false);
			this.spineView.view.state.onComplete = () => {
				try //[Y] for debug reasons
				{
					this._startHighlighting();
				}
				catch (e)
				{
					console.error(e);
				}
			}
			this.spineView.view.state.timeScale = EXPLODE_SPINE_SPEED;
			APP.soundsController.play("frog_body_expand");
			APP.soundsController.play("frog_croak_long");
		}
	}

	_calcAnimationName(aDirection_str, aSuffix_str)
	{
		return this._calcAnimationAngle(aDirection_str) + "_" + aSuffix_str;
	}

	_calcAnimationAngle(aDirection_str)
	{
		switch (aDirection_str)
		{
			case DIRECTION.LEFT_UP:
				return '270';
			case DIRECTION.LEFT_DOWN:
				return '0';
			case DIRECTION.RIGHT_UP:
				return '180';
			case DIRECTION.RIGHT_DOWN:
				return '90';
		}
		throw new Error (aDirection_str + " is not supported direction.");
	}

	dissolveShadow()
	{
		if (this.shadow)
		{
			this.shadow.fadeTo(0, 100);
			this.shadow.scaleTo(0, 100, null, () => {
				this.shadow && this.shadow.destroy();
			})
		}
	}

	_showDeathExplosionSmokes()
	{
		this.dissolveShadow();

		this.container.scaleTo(0.8, 2 * FRAME_RATE, null, () => {
			this.spineView.alpha = 0;
			this._fHighlightedSprite_sprt.alpha = 0;
		})

		//this._stopTrembling();

		FrogEnemy.initExplosionTextures();

		/* for better contrast start double smoke */
		this._createSmoke(30);
		this._createSmoke(30, () => {
			this.spineView && this.spineView.destroy();
			this.spineView = null;
			this.onDeathFxAnimationCompleted();
		});

		this.emit(Enemy.EVENT_ON_ENEMY_VIEW_REMOVING);
		this._forceHVRisingUpIfRequired();

		DeathFxAnimation.initSmokePuffTextures();
		this._showPuffs();

		APP.soundsController.play("frog_explode");
	}

	_createSmoke(aZIndex_int, aCallback_func = null)
	{
		let lTextures_txtr_arr = FrogEnemy.textures['explosion'];

		let lExplosion_sprt = new Sprite();
		lExplosion_sprt.textures = lTextures_txtr_arr;
		lExplosion_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lExplosion_sprt.scale.set(2);
		lExplosion_sprt.anchor.set(0.5, 0.45);
		lExplosion_sprt.zIndex = this.zIndex + aZIndex_int;

		// adding to GameField for better view with the small frogs
		this._fBlueSmokes_spr_arr.push(this._gameField.addChild(lExplosion_sprt));
		let lPosition_pt = this._gameField.globalToLocal(this.position);
		let lCenter_pt = this._calcLocalCenterOffset()

		lPosition_pt.x += lCenter_pt.x;
		lPosition_pt.y += lCenter_pt.y;
		lExplosion_sprt.position.set(lPosition_pt.x, lPosition_pt.y);
		lExplosion_sprt.on('animationend', () => {
			this._gameField && this._gameField.removeChild(lExplosion_sprt);
			lExplosion_sprt && lExplosion_sprt.destroy();
			aCallback_func && aCallback_func();
		});
		lExplosion_sprt.play();

	}

	_showPuffs()
	{
		for (let lPuffParams_obj of PUFFS)
		{
			this._fPuffs_sprt_arr.push(this._showPuff(lPuffParams_obj));
		}
	}

	_showPuff(aParams_obj)
	{
		let lPuff_sprt = new Sprite;
		lPuff_sprt.zIndex = 99 - aParams_obj.id;

		let colorMatrix = new PIXI.filters.ColorMatrixFilter();
		lPuff_sprt.filters = [colorMatrix];
		colorMatrix.brightness(5);

		lPuff_sprt.blendMode = aParams_obj.blendMode;

		lPuff_sprt.position.set(aParams_obj.x, aParams_obj.y);
		lPuff_sprt.scale.set(aParams_obj.scale.x * 2, aParams_obj.scale.y * 2);
		lPuff_sprt.rotation = Utils.gradToRad(aParams_obj.angle);

		lPuff_sprt.once('animationend', () => {
			this._destroySprite(lPuff_sprt);
		});
		lPuff_sprt.gotoAndPlay(7);

		let seq = [
			{
				tweens: [],
				duration: 30 * FRAME_RATE
			},
			{
				tweens: [ { prop: "alpha", to: 0 } ],
				duration: 10 * FRAME_RATE,
				onfinish: () => { this._destroySprite(lPuff_sprt); }
			}
		];
		Sequence.start(lPuff_sprt, seq);

		return this.addChild(lPuff_sprt);
	}

	_destroySprite(aSprite_sprt)
	{
		Sequence.destroy(Sequence.findByTarget(aSprite_sprt));
		aSprite_sprt && aSprite_sprt.destroy();
	}

	_startTrembling()
	{
		let delta = 35;
		let startX = this.container.x;
		let startY = this.container.y;
		let sequence = [];
		let n = 50;
		for (let i = 0; i < n; i++)
		{
			sequence.push(
				{
					tweens:[{prop:'x', to:startX + (Utils.random(-delta, delta)/10)}, {prop:'y', to:startY + (Utils.random(-delta, delta)/10)}],
					duration: 1 * FRAME_RATE,
					ease:Easing.sine.easeInOut
				}
			)
		}

		this._fTremblingSequence_seq = Sequence.start(this.container, sequence);
	}

	_stopTrembling()
	{
		this._fTremblingSequence_seq && this._fTremblingSequence_seq.destructor();
		this._fTremblingSequence_seq = null;
	}

	_startHighlighting()
	{
		if (APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater && !(APP.isMobile && APP.mobileValidator.ios()) && !APP.isDeprecetedInternetExplorer)
		{
			this.spineView.view.filters = [new GlowFilter({distance: 12, outerStrength: 0, innerStrength: 4, color: 0xFFFFBE, quality: 1})];

			let l_txtr = APP.stage.renderer.generateTexture(this.spineView.view, PIXI.SCALE_MODES.LINER, APP.isMobile ? 0.25 : 0.5);
			let l_sprt = new PIXI.Sprite(l_txtr);

			this.spineView.view.filters = null;
			l_sprt.scale = this.spineView.scale;
			l_sprt.zIndex = this.spineView.zIndex + 1;
			this.container.addChild(l_sprt);

			let lLocalSpineBounds_obj = this.spineView.view.getLocalBounds();
			lLocalSpineBounds_obj.x *= this.spineView.scale.x;
			lLocalSpineBounds_obj.y *= this.spineView.scale.y;
			lLocalSpineBounds_obj.width *= this.spineView.scale.x;
			lLocalSpineBounds_obj.height *= this.spineView.scale.y;

			l_sprt.position.set(lLocalSpineBounds_obj.x + this.spineViewPos.x, lLocalSpineBounds_obj.y + this.spineViewPos.y);
			this._fHighlightedSprite_sprt = l_sprt;
		}
		else
		{
			this._fHighlightedSprite_sprt = new Sprite(); //stub
		}

		this._fHighlightedSprite_sprt.alpha = 0;
		let alphaSequence = [
			{
				tweens: [ { prop: "alpha", to: 0.6 } ],
				ease:Easing.sine.easeIn,
				duration: 350
			},
			{
				tweens: [],
				duration: 100,
				onfinish: () => { this._onReadyToExplode(); }
			}
		]
		Sequence.start(this._fHighlightedSprite_sprt, alphaSequence);
		this._fHighlightedSprite_sprt.blendMode = PIXI.BLEND_MODES.ADD;

		let scaleSequene = [
			{
				tweens: [
					{ prop: "scale.x", to: this.container.scale.x * 1.1 },
					{ prop: "scale.y", to: this.container.scale.x * 1.1 },
				],
				duration: 400
			}
		];
		Sequence.start(this.container, scaleSequene);
	}

	_onReadyToExplode()
	{

		let lEnemyPosition_pt = this.getGlobalPosition();
		lEnemyPosition_pt.x += this.getCurrentFootPointPosition().x;
		lEnemyPosition_pt.y += this.getCurrentFootPointPosition().y;
		this.emit(Enemy.EVENT_ON_DEATH_ANIMATION_EXPLOSION_STARTED, {position: lEnemyPosition_pt, angle: this.angle});

		this._showDeathExplosionSmokes();
	}

	//override
	playAppearSound()
	{
		let soundName = "frog_croak_" + (APP.isMobile ? 2 : Utils.random(1, 2));
		APP.soundsController.play(soundName);
	}

	//override
	_onJumpFinish()
	{
		this._fLandingCounter_int++;
		if (this._fLandingCounter_int === 1 ) // && this.params.needShowSound )
		{
			let soundName = "frog_land_" + (APP.isMobile ? 1 : Utils.random(1, 2));
			APP.soundsController.play(soundName);
		}
	}

	destroy(purely = false)
	{
		for (let lPuff_sprt of this._fPuffs_sprt_arr)
		{
			this._destroySprite(lPuff_sprt);
		}
		this._fPuffs_sprt_arr = null;

		for (let lSmoke_spr of this._fBlueSmokes_spr_arr)
		{
			this._gameField && this._gameField.removeChild(lSmoke_spr);
			lSmoke_spr && lSmoke_spr.destroy();
		}
		this._fBlueSmokes_spr_arr = null;

		this._fTremblingSequence_seq && this._fTremblingSequence_seq.destructor();
		this._fTremblingSequence_seq = null;

		Sequence.destroy(Sequence.findByTarget(this.container));
		Sequence.destroy(Sequence.findByTarget(this._fHighlightedSprite_sprt));

		super.destroy(purely);
	}
}

FrogEnemy.textures = {
	explosion: null
};

FrogEnemy.setTexture = function (name, imageNames, configs, path)
{
	if (!FrogEnemy.textures[name])
	{
		FrogEnemy.textures[name] = [];

		if(!Array.isArray(imageNames)) imageNames = [imageNames];
		if(!Array.isArray(configs)) configs = [configs];

		let assets = [];
		imageNames.forEach(function(item){assets.push(APP.library.getAsset(item))});

		FrogEnemy.textures[name] = AtlasSprite.getFrames(assets, configs, path);
		FrogEnemy.textures[name].sort(function(a, b){if(a._atlasName > b._atlasName) return 1; else return -1});
	}
};

FrogEnemy.initExplosionTextures = function ()
{
	FrogEnemy.setTexture('explosion', "enemies/frog/fx/frog_exploder_fx", AtlasConfig.FrogExploderFx, '');
}

export default FrogEnemy;