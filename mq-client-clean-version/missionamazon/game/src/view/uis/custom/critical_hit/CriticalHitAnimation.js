import { Sprite, AtlasSprite } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display';
import I18 from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/translations/I18';
import AtlasConfig from './../../../../config/AtlasConfig';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import Sequence from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import { FRAME_RATE, ENEMIES } from './../../../../../../shared/src/CommonConstants';
import CriticalHitMultiplierView from './CriticalHitMultiplierView';
import PrizesController from './../../../../controller/uis/prizes/PrizesController';
import { ENEMY_DIRECTION } from '../../../../config/Constants';
import { GlowFilter, OutlineFilter } from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Filters';

let _criticalExplosionTextures = null;

function _initExplosionTextures()
{
	if (_criticalExplosionTextures) return;

	_criticalExplosionTextures = AtlasSprite.getFrames(APP.library.getAsset("critical_hit/critical_explosion"), AtlasConfig.CriticalExplosion, "");
}

const HIT_POSITIONS = {
	[ENEMIES.BlueOrbArtillerystrike]:		{x: 25, y: -24},
	[ENEMIES.BlueOrbFlamethrower]:			{x: 25, y: -24},
	[ENEMIES.BlueOrbCryogun]:				{x: 25, y: -24},
	[ENEMIES.BlueOrbLaser]:					{x: 25, y: -24},
	[ENEMIES.BlueOrbPlasma]:				{x: 25, y: -24},

	[ENEMIES.Wasp]:							{x: 25, y: -40},
	[ENEMIES.YellowWasp]:					{x: 25, y: -40},
	[ENEMIES.VioletWasp]:					{x: 25, y: -40},
	[ENEMIES.Firefly]:						{x: 25, y: -40},
	[ENEMIES.RedAnt]:						{x: 25*1.1, y: -24*1.1},
	[ENEMIES.BlackAnt]:						{x: 25, y: -24},
	[ENEMIES.BabyFrog]:						{x: 25, y: -24},
	[ENEMIES.Scorpion]:						{x: 25, y: -36},
	[ENEMIES.SkullboneOrb]:					{x: 25, y: -48},
	[ENEMIES.SnakeStraight]:				{x: 25*0.85, y: -24*0.85},
	[ENEMIES.Jaguar]:						{x: 25*1.3*1.1, y: -48*1.3*1.1},
	[ENEMIES.Skullbreaker]:					{x: 25, y: -48},
	[ENEMIES.Witch]:						{x: 25, y: -48},
	[ENEMIES.Jumper]:						{x: 25, y: -48},
	[ENEMIES.Exploder]:						{x: 25, y: -48},
	[ENEMIES.SkullboneRunner]:				{x: 25, y: -48},
	[ENEMIES.SpiderBoss]: 					{x: 25, y: -48},
	[ENEMIES.GolemBoss]: 					{x: 25*1.15, y: -48*1.15},
	[ENEMIES.ApeBoss]: 						{x: 25, y: -48},

	[ENEMIES.PoisonPlantMaroonViolet]:			{x: 50, y: -30},
	[ENEMIES.PoisonPlantYellowPurple]:		{x: 50, y: -30},
	[ENEMIES.CarnivorePlantRed]:			{x: 50, y: -30},
	[ENEMIES.CarnivorePlantGreen]:			{x: 50, y: -30},
	[ENEMIES.Goblin]:						{x: 25, y: -48},
	[ENEMIES.HobGoblin]:					{x: 25, y: -48},
	[ENEMIES.GreenImp]:						{x: 50, y: -30},
	[ENEMIES.Ogre]:							{x: 50, y: -30},
	[null]:									{x: 50, y: -30}
}

class CriticalHitAnimation extends Sprite
{
	static get EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED()		{return "onCriticalHitAnimationEnded";}

	startAnimation()
	{
		this._startAnimation();
	}

	getCaptionBounds()
	{
		let lBounds_obj = this._fCaption_ta ? this._fCaption_ta.getBounds() : {width: 0, height: 0, x: 0, y: 0};

		lBounds_obj.x = 0;
		lBounds_obj.y = 0;
		if (this._fCaptionContainer_sprt)
		{
			lBounds_obj.x += this._fCaptionContainer_sprt.position.x;
			lBounds_obj.y += this._fCaptionContainer_sprt.position.y;
		}
		if (this._fContainer_sprt)
		{
			lBounds_obj.x += this._fContainer_sprt.position.x;
			lBounds_obj.y += this._fContainer_sprt.position.y;
		}

		return lBounds_obj;
	}

	getMaxMultiplierBounds()
	{
		let lPrevScale_num = this._fMultiplierContainer_sprt.scale.x;
		this._fMultiplierContainer_sprt.scale.set(2.6);

		let lBounds_obj = this._fMultiplierContainer_sprt ? this._fMultiplierContainer_sprt.getBounds() : {width: 0, height: 0, x: 0, y: 0};

		lBounds_obj.x = 0;
		lBounds_obj.y = 0;
		if (this._fContainer_sprt)
		{
			lBounds_obj.x += this._fContainer_sprt.position.x;
			lBounds_obj.y += this._fContainer_sprt.position.y;
		}

		this._fMultiplierContainer_sprt.scale.set(lPrevScale_num);

		return lBounds_obj;
	}

	get rid()
	{
		return this._fRid_num;
	}

	get enemyId()
	{
		return this._fEnemyId_num;
	}

	constructor(aWin_num, aMult_num, aEnemyId_num, aEnemyName_str, aEnemyDirection_str, aRid_num)
	{
		super();

		_initExplosionTextures();

		this._fRid_num = aRid_num;
		this._fWin_num = aWin_num;
		this._fMult_num = aMult_num;
		this._fEnemyId_num = aEnemyId_num;
		this._fEnemyName_str = aEnemyName_str;
		this._fContainer_sprt = null;
		this._fCaption_ta = null;
		this._fCaptionContainer_sprt = null;
		this._fExplosion_sprt = null;
		this._fAwardPosition_obj = {x: 0, y: 0};
		this._fMultiplierContainer_sprt = null;
		this._fMultiplier_chmv = null;
		this._fSweep_sprt = null;
		this._fFlare_sprt = null;
		this._fIsLeftDir_bln = !!(aEnemyDirection_str == ENEMY_DIRECTION.LEFT_UP || aEnemyDirection_str == ENEMY_DIRECTION.LEFT_DOWN);
		this._fMultiplierInitiated_bln = false;
		this._fCashAppeared_bln = false;

		APP.gameScreen.prizesController.on(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);

		this._init();
	}

	_init()
	{
		this._initContainer();
		this._initExplosion();
		this._initFlare();

		this._fCaptionContainer_sprt = this._fContainer_sprt.addChild(new Sprite());
		this._fCaptionContainer_sprt.position.set(0, -10);
		this._fMultiplierContainer_sprt = this._fContainer_sprt.addChild(new Sprite());
		this._fMultiplierContainer_sprt.position.set(0, -10);

		this._initCaption();
		this._initMultiplier();
		this._initSweep();
	}

	_initCaption()
	{
		this._fCaption_ta = I18.generateNewCTranslatableAsset("TACriticalHitLabel");

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._fCaptionContainer_sprt.addChild(this._createGlowCopy(this._fCaption_ta, 6));
		}

		this._fCaptionContainer_sprt.addChild(this._fCaption_ta);
	}

	_initMultiplier()
	{
		this._fMultiplier_chmv = new CriticalHitMultiplierView();
		this._fMultiplier_chmv.value = "x" + this._fMult_num;

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._fMultiplierContainer_sprt.addChild(this._createGlowCopy(this._fMultiplier_chmv, 8));
		}

		this._fMultiplierContainer_sprt.addChild(this._fMultiplier_chmv);
		this._fMultiplierContainer_sprt.scale.set(0);
	}

	_createGlowCopy(aSprite_sprt, aExtraSize_num)
	{
		let remProps = {
			x: aSprite_sprt.x,
			y: aSprite_sprt.y
		}

		APP.isDeprecetedInternetExplorer
			? aSprite_sprt.filters = [new OutlineFilter(2.5, 0xffffe6)]
			: aSprite_sprt.filters = [new GlowFilter({distance: 8, outerStrength: 2.5, innerStrength: 2, color: 0xffffe6, quality: 2})];

		let lGlowBounds_obj = aSprite_sprt.getBounds();
		lGlowBounds_obj.height += aExtraSize_num;
		lGlowBounds_obj.y -= aExtraSize_num/2;
		lGlowBounds_obj.width += aExtraSize_num;
		lGlowBounds_obj.x -= aExtraSize_num/2;
		
		aSprite_sprt.x = lGlowBounds_obj.width/2;
		aSprite_sprt.y = lGlowBounds_obj.height/2;
		var lGlowTexture_txt = PIXI.RenderTexture.create({ width: lGlowBounds_obj.width, height: lGlowBounds_obj.height, scaleMode: PIXI.SCALE_MODES.NEAREST, resolution: 2 });
		APP.stage.renderer.render(aSprite_sprt, { renderTexture: lGlowTexture_txt });
		aSprite_sprt.x = remProps.x;
		aSprite_sprt.y = remProps.y;
		aSprite_sprt.filters = [];

		let lGlowSprite_sprt = new Sprite();
		lGlowSprite_sprt.texture = lGlowTexture_txt;
		lGlowSprite_sprt.scale.set(1.04);
		lGlowSprite_sprt.tint = 0xffffe6;
		lGlowSprite_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lGlowSprite_sprt.alpha = 0.9;

		return lGlowSprite_sprt;
	}

	_initContainer()
	{
		this._fContainer_sprt = this.addChild(new Sprite());

		let lHitPosition_pt = HIT_POSITIONS[this._fEnemyName_str];
		if (!lHitPosition_pt)
		{
			throw new Error("CriticalHitAnimation :: no HIT POSITION for enemy " + this._fEnemyName_str);
		}
		this._fContainer_sprt.position.x = lHitPosition_pt.x;
		this._fContainer_sprt.position.y = lHitPosition_pt.y;
		this._fContainer_sprt.visible = false;

		if (this._fIsLeftDir_bln)
		{
			this._fContainer_sprt.position.x -= 16;
		}
	}

	_initExplosion()
	{
		this._fExplosion_sprt = this._fContainer_sprt.addChild(new Sprite());
		this._fExplosion_sprt.scale.set(2);
		this._fExplosion_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		this._fExplosion_sprt.textures = _criticalExplosionTextures;
		this._fExplosion_sprt.position.set(11, -70);
		this._fExplosion_sprt.rotation = Utils.gradToRad(65);
		this._fExplosion_sprt.animationSpeed = 0.5;
		this._fExplosion_sprt.on('animationend', () => {
			this._fExplosion_sprt && this._fExplosion_sprt.destroy();
			this._fExplosion_sprt = null;
		});
	}

	_initFlare()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._fFlare_sprt = this._fContainer_sprt.addChild(APP.library.getSprite("critical_hit/flare"));
			this._fFlare_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		}
	}

	_initSweep()
	{
		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			let lTextWidthDescr = I18.getTranslatableAssetDescriptor("TACriticalHitTextWidthDescriptor");
			this._fTextWidth_num = !!lTextWidthDescr ? lTextWidthDescr.areaInnerContentDescriptor.areaDescriptor.width : this._fCaption_ta.assetContent.getBounds().width;

			this._fSweep_sprt = this._fCaption_ta.addChild(APP.library.getSprite("light_sweep"));
			this._fSweep_sprt.scale.set(0.5);
			let lMask_ta = this._fCaption_ta.addChild(I18.generateNewCTranslatableAsset("TACriticalHitLabel"));
			this._fSweep_sprt.mask = lMask_ta.assetContent;

			let lStartSweepX_num = -this._fTextWidth_num/2 - this._fSweep_sprt.width/2;
			this._fSweep_sprt.position.set(lStartSweepX_num, 0);
		}
	}

	_onTimeToShowCash(aEvent_obj)
	{
		let lData_arr = aEvent_obj.data;

		for (let lData_obj of lData_arr)
		{
			if (lData_obj.hitData.enemy && this._fEnemyId_num === lData_obj.hitData.enemy.id)
			{
				let lX_num = lData_obj.awardStartPosition.x;
				let lY_num = lData_obj.awardStartPosition.y;
				this._fAwardPosition_obj = {x: lX_num, y: lY_num};

				if (this._fIsLeftDir_bln)
				{
					this._fAwardPosition_obj.x += 16;
				}

				if (APP.gameScreen.prizesController)
				{
					APP.gameScreen.prizesController.off(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);
				}

				this._fCashAppeared_bln = true;
				this._tryStartMultiplier();
				return;
			}
		}
	}

	_startAnimation()
	{
		this._fContainer_sprt.visible = true;

		this._fExplosion_sprt.play();

		this._startCaptionAnimation();
		this._startFlareAnimation();
	}

	_startFlareAnimation()
	{
		if (!this._fFlare_sprt) return;

		this._fFlare_sprt.scale.set(0);
		let lFlareSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 0.4},	{prop: 'scale.y', to: 0.4}],	duration: 1*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1},		{prop: 'scale.y', to: 1}],		duration: 1*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 0},		{prop: 'scale.y', to: 0}],		duration: 12*FRAME_RATE, onfinish: () => {
				this._fFlare_sprt && this._fFlare_sprt.destroy();
				this._fFlare_sprt = null;
			}}
		];

		Sequence.start(this._fFlare_sprt, lFlareSeq_arr);
	}

	_startCaptionAnimation()
	{
		this._fCaptionContainer_sprt.scale.set(0);
		let lCaptionSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 1.6},	{prop: 'scale.y', to: 1.6}],	duration: 5*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.4},	{prop: 'scale.y', to: 1.4}],	duration: 3*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.45},	{prop: 'scale.y', to: 1.45}],	duration: 2*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1},		{prop: 'scale.y', to: 1}],		duration: 9*FRAME_RATE, onfinish: () => {
				this._startSweep();
				this._fMultiplierInitiated_bln = true;
				this._tryStartMultiplier();
			}},
			{tweens: [],	duration: 5*FRAME_RATE},
			{tweens: [{prop: 'alpha', to: 0}],	duration: 2*FRAME_RATE, onfinish: () => {
				this._fCaptionContainer_sprt && this._fCaptionContainer_sprt.destroy();
				this._fCaptionContainer_sprt = null;
			}},
		];

		Sequence.start(this._fCaptionContainer_sprt, lCaptionSeq_arr);
	}

	_startSweep()
	{
		if (!this._fSweep_sprt) return;

		let l_seq = [{tweens: [{prop: 'position.x', to: this._fTextWidth_num/2},],	duration: 8*FRAME_RATE}];

		Sequence.start(this._fSweep_sprt, l_seq);
	}

	_tryStartMultiplier()
	{
		if (this._fMultiplierInitiated_bln && this._fCashAppeared_bln)
		{
			this._startMultiplier();
		}
	}

	_startMultiplier()
	{
		let lAward_ca = APP.currentWindow.awardingController.getAwardByRid(this._fRid_num, this._fEnemyId_num);
		if (lAward_ca)
		{
			let lOffset_obj = lAward_ca.offscreenOffset;
			if (lOffset_obj)
			{
				this._fAwardPosition_obj.x += lOffset_obj.x;
				this._fAwardPosition_obj.y += lOffset_obj.y;
			}
		}

		let lFinalPos_obj = this.globalToLocal(this._fAwardPosition_obj.x, this._fAwardPosition_obj.y);
		lFinalPos_obj.x -= 24;
		lFinalPos_obj.y -= 61;

		let lMultSeq_arr = [
			{tweens: [{prop: 'scale.x', to: 2.6}, {prop: 'scale.y', to: 2.6}], duration: 4*FRAME_RATE},
			{tweens: [{prop: 'scale.x', to: 1.17}, {prop: 'scale.y', to: 1.17},	{prop: 'position.y', to: -6}], duration: 8*FRAME_RATE},
			{tweens: [{prop: 'position.x', to: lFinalPos_obj.x}, {prop: 'position.y', to: lFinalPos_obj.y}], duration: 5*FRAME_RATE},
			{tweens: [],	duration: 1*FRAME_RATE, onfinish: () => {
				this._fMultiplierContainer_sprt && this._fMultiplierContainer_sprt.destroy();
				this._fMultiplierContainer_sprt = null;
				this._onAnimationEnded();
			}},
		];

		Sequence.start(this._fMultiplierContainer_sprt, lMultSeq_arr);
	}

	_onAnimationEnded()
	{
		this.emit(CriticalHitAnimation.EVENT_ON_CRITICAL_HIT_ANIMATION_ENDED);
	}

	destroy()
	{
		if (APP.gameScreen.prizesController)
		{
			APP.gameScreen.prizesController.off(PrizesController.i_EVENT_ON_TIME_TO_SHOW_CASH_PRIZES, this._onTimeToShowCash, this);
		}

		this._fFlare_sprt && Sequence.destroy(Sequence.findByTarget(this._fFlare_sprt));
		this._fCaptionContainer_sprt && Sequence.destroy(Sequence.findByTarget(this._fCaptionContainer_sprt));
		this._fSweep_sprt && Sequence.destroy(Sequence.findByTarget(this._fSweep_sprt));
		this._fMultiplierContainer_sprt && Sequence.destroy(Sequence.findByTarget(this._fMultiplierContainer_sprt));

		super.destroy();

		this._fWin_num = null;
		this._fMult_num = null;
		this._fEnemyId_num = null;
		this._fEnemyName_str = null;
		this._fContainer_sprt = null;
		this._fCaption_ta = null;
		this._fCaptionContainer_sprt = null;
		this._fExplosion_sprt = null;
		this._fAwardPosition_obj = null;
		this._fMultiplierContainer_sprt = null;
		this._fMultiplier_chmv = null;
		this._fSweep_sprt = null;
		this._fFlare_sprt = null;
		this._fRid_num = null;
		this._fMultiplierInitiated_bln = null;
		this._fCashAppeared_bln = null;
	}
}

export default CriticalHitAnimation;