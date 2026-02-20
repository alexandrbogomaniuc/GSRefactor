import Sprite from '../../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import MissEffect from '../../../../../main/missEffects/MissEffect';
import CommonEffectsManager from '../../../../../main/CommonEffectsManager';
import Timer from '../../../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer';
import { FRAME_RATE } from '../../../../../../../shared/src/CommonConstants';
import CoinsExplosionAnimation from './../coins_explosion/CoinsExplosionAnimation';
import { ENEMIES } from './../../../../../../../shared/src/CommonConstants';

class BossModeDisappearanceFxView extends Sprite
{
	static get EVENT_ANIMATION_COMPLETED() {return "onBossDisappearenceFxAnimationCompleted"};

	startAnimation()
	{
		this._startAnimation();
	}

	startCoinsExplodeAnimation()
	{
		this._startCoinsExplodeAnimation();
	}

	constructor(aBossType_str, aIsCoPlayerWin_bln)
	{
		super();

		this._fIsCoPlayerWin_bln = aIsCoPlayerWin_bln;
		this._fBossType_str = aBossType_str;

		this._fCoinsExplosionAnimation_cea = null;

		this._fDeathFlash_sprt = null;
		this._fDeathSplat_sprt = null;
		this._fDeathSmokeBurst_sprt = null;
		this._fDeathGroundSmoke_sprt = null;

		this._fCompleteTimer_t = null;
		this._fFlashAnimationTimer_t = null;

		this._fExplosionCompleted_bln = null;
		this._fMainAnimationCompleted_bln = null;
		this._fTimers_arr = [];
	}

	_startCoinsExplodeAnimation()
	{
		this._fExplosionCompleted_bln = false;

		this._fCoinsExplosionAnimation_cea = this.addChild(new CoinsExplosionAnimation(this._fBossType_str, this._fIsCoPlayerWin_bln));
		let lPos_obj = this._coinsExplodePosition;
		this._fCoinsExplosionAnimation_cea.position.set(lPos_obj.x, lPos_obj.y);

		if (this._fIsCoPlayerWin_bln)
		{
			this._fCoinsExplosionAnimation_cea.scale.set(0.7);
		}

		this._fCoinsExplosionAnimation_cea.once(CoinsExplosionAnimation.EVENT_ON_ANIMATION_COMPLETED, this._onExplosionCompleted, this);

		this._fTimers_arr.push(new Timer(()=>{this._fCoinsExplosionAnimation_cea && this._fCoinsExplosionAnimation_cea.startAnimation()}, 50*FRAME_RATE));

	}

	get _coinsExplodePosition()
	{
		switch (this._fBossType_str)
		{
			default: return {x: 0, y: -122};
		}
	}

	_onExplosionCompleted()
	{
		this._fExplosionCompleted_bln = true;

		this._validateCompletion();
	}

	_startAnimation()
	{
		this._fMainAnimationCompleted_bln = false;

		this._startSplatAnimation();
		this._startFlashAnimation();

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this._startGroundSmokeAnimation();
			this._startSmokeBurstAnimation();
		}

		this._fCompleteTimer_t && this._fCompleteTimer_t.destructor();
		this._fCompleteTimer_t = new Timer(this._onAnimationCompleted.bind(this), 91*FRAME_RATE);
	}

	//SPLAT...
	_startSplatAnimation()
	{
		this._fDeathSplat_sprt = this.addChild(this._createDeathSplat());

		let lSplatSeq_arr = [
			{ tweens:[], duration:1*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:0}], duration:7*FRAME_RATE },
			{ tweens:[], duration:3*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:1}], duration:1*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:0}], duration:7*FRAME_RATE }
		];

		Sequence.start(this._fDeathSplat_sprt, lSplatSeq_arr);
	}

	_createDeathSplat()
	{
		let lDeathSplat_sprt = APP.library.getSpriteFromAtlas(this._deathSplatTextureName);
		let pos = this._deathSplatPosition;
		lDeathSplat_sprt.anchor.set(0.5, 0.5);
		lDeathSplat_sprt.position.set(pos.x, pos.y);
		lDeathSplat_sprt.scale.set(12);
		lDeathSplat_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		return lDeathSplat_sprt;
	}

	get _deathSplatPosition()
	{
		return new PIXI.Point(-20, -50);
	}

	get _deathSplatTextureName()
	{
		return 'boss_mode/death_splat';
	}
	//...SPLAT

	//SMOKE BURST...
	_startSmokeBurstAnimation()
	{
		this._fDeathSmokeBurst_sprt = this.addChild(this._createDeathSmokeBurst());
		this._fDeathSmokeBurst_sprt.once('animationend', (e) => {
			e.target.destroy();
		});
		this._fDeathSmokeBurst_sprt.play();
	}

	_createDeathSmokeBurst()
	{
		let lDeathSmokeBurst_sprt = new Sprite;
		let pos = this._deathSmokeBurstPosition;
		lDeathSmokeBurst_sprt.textures = this._deathSmokeBurstTextures;
		lDeathSmokeBurst_sprt.blendMode = PIXI.BLEND_MODES.SCREEN;
		lDeathSmokeBurst_sprt.position.set(pos.x, pos.y);
		lDeathSmokeBurst_sprt.scale.set(6);
		lDeathSmokeBurst_sprt.animationSpeed = 48/60;

		return lDeathSmokeBurst_sprt;
	}

	get _deathSmokeBurstPosition()
	{
		return new PIXI.Point(-80, -135);
	}

	get _deathSmokeBurstTextures()
	{
		return MissEffect.getSmokeTextures();
	}
	//...SMOKE BURST

	//GROUND SMOKE...
	_startGroundSmokeAnimation()
	{
		this._fDeathGroundSmoke_sprt = this.addChild(this._createDeathGroundSmoke());
		this._fDeathGroundSmoke_sprt.once('animationend', (e) => { e.target.destroy() });
		this._fDeathGroundSmoke_sprt.play();
	}

	_createDeathGroundSmoke()
	{
		let groundSmoke = new Sprite;
		let pos = this._deathGroundSmokePosition;
		groundSmoke.textures = [PIXI.Texture.EMPTY, PIXI.Texture.EMPTY, PIXI.Texture.EMPTY, PIXI.Texture.EMPTY].concat(this._deathGroundSmokeTextures);
		groundSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		groundSmoke.position.set(pos.x, pos.y);
		groundSmoke.scale.set(3.5);
		groundSmoke.animationSpeed = 48/60;

		return groundSmoke;
	}

	get _deathGroundSmokePosition()
	{
		return new PIXI.Point(-80, -55);
	}

	get _deathGroundSmokeTextures()
	{
		CommonEffectsManager.getGroundSmokeTextures();
		return CommonEffectsManager.textures['groundSmoke'];
	}
	//...GROUND SMOKE

	//FLASH...
	_startFlashAnimation()
	{
		this._fDeathFlash_sprt = this.addChild(this._createDeathFlash());
		this._fDeathFlash_sprt.visible = false;

		this._startFlashSequence();
	}

	_startFlashSequence()
	{
		this._fDeathFlash_sprt.visible = true;

		let lFlashSeq_arr = [
			{ tweens:[], duration:12*FRAME_RATE },
			{ tweens:[{prop:"alpha", to:0}], duration:5*FRAME_RATE }
		];

		Sequence.start(this._fDeathFlash_sprt, lFlashSeq_arr);
	}

	_createDeathFlash()
	{
		let lDeathFlash_sprt = APP.library.getSpriteFromAtlas(this._deathFlashTextureName);
		let pos = this._deathFlashPosition;
		lDeathFlash_sprt.blendMode = PIXI.BLEND_MODES.ADD;
		lDeathFlash_sprt.position.set(pos.x, pos.y);
		lDeathFlash_sprt.scale.set(5);

		return lDeathFlash_sprt;
	}

	get _deathFlashPosition()
	{
		return new PIXI.Point(0, 0);
	}

	get _deathFlashTextureName()
	{
		return 'boss_mode/death_flash';
	}
	//...FLASH

	_onAnimationCompleted()
	{
		this._fMainAnimationCompleted_bln = true;

		this._fCompleteTimer_t && this._fCompleteTimer_t.destructor();
		this._fCompleteTimer_t = null;

		this._validateCompletion();
	}

	_validateCompletion()
	{
		if (this._fMainAnimationCompleted_bln && this._fExplosionCompleted_bln)
		{
			this.emit(BossModeDisappearanceFxView.EVENT_ANIMATION_COMPLETED);
		}
	}

	destroy()
	{
		this._fCompleteTimer_t && this._fCompleteTimer_t.destructor();
		this._fCompleteTimer_t = null;

		this._fFlashAnimationTimer_t && this._fFlashAnimationTimer_t.destructor();
		this._fFlashAnimationTimer_t = null;

		if (this._fDeathFlash_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fDeathFlash_sprt));
			this._fDeathFlash_sprt.destroy();
			this._fDeathFlash_sprt = null;
		}

		if (this._fDeathSplat_sprt)
		{
			Sequence.destroy(Sequence.findByTarget(this._fDeathSplat_sprt));
			this._fDeathSplat_sprt.destroy();
			this._fDeathSplat_sprt = null;
		}

		this._fDeathSmokeBurst_sprt = null;
		this._fDeathGroundSmoke_sprt = null;

		super.destroy();

		this._fCoinsExplosionAnimation_cea = null;
	}
}

export default BossModeDisappearanceFxView