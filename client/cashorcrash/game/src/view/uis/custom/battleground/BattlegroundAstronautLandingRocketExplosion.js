import MTimeLine from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import Sprite from '../../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class BattlegroundAstronautLandingRocketExplosion extends Sprite
{	
	constructor()
	{
		super();

		this._fTimeline_mtl = null;

		let lContainer_sprt = this._fContainer_sprt = this.addChild(new Sprite);
		lContainer_sprt.x = 635;
		lContainer_sprt.y = 190;

		let lExplosionContainer_sprt = this._fExplosionContainer_sprt = lContainer_sprt.addChild(new Sprite);
		lExplosionContainer_sprt.scale.set(1.42);
		lExplosionContainer_sprt.x = -120 - (34 - 20) * 8;
		lExplosionContainer_sprt.y = (5/288)*(lExplosionContainer_sprt.x+120)*(lExplosionContainer_sprt.x+120) - 100;
		
		this._fDust1_spr = lExplosionContainer_sprt.addChild(APP.library.getSprite("game/battleground/dust"));
		this._fDust2_spr = lExplosionContainer_sprt.addChild(APP.library.getSprite("game/battleground/dust"));
		this._fSmokePurple_spr = lExplosionContainer_sprt.addChild(APP.library.getSprite("game/battleground/smoke_purple"));
		this._fMiddleLightParticle_spr = lExplosionContainer_sprt.addChild(APP.library.getSprite("game/battleground/light_particle"));
		this._fStartGlow_spr = lExplosionContainer_sprt.addChild(APP.library.getSprite("game/battleground/star_glow"));

		this._addAnimation();
        
		this.visible = false;
	}

	_addAnimation()
	{
		let l_mtl = new MTimeLine();

		l_mtl.addAnimation(
			this._fDust1_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				3,
				[0.28, 0.01],
				[0.41, 4],
				[0, 27]
			],
			this);

		l_mtl.addAnimation(
			this._fDust1_spr,
			MTimeLine.SET_SCALE,
			0.232 * 2,
			[
				3,
				[0.461 * 2, 31]
			],
			this);

		l_mtl.addAnimation(
			this._fDust1_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				3,
				[31.8, 31]
			],
			this);

		l_mtl.addAnimation(
			this._fDust2_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				3,
				[0.28, 0.01],
				[0.41, 4],
				[0, 33]
			],
			this);

		l_mtl.addAnimation(
			this._fDust2_spr,
			MTimeLine.SET_SCALE,
			0.172 * 2,
			[
				3,
				[0.401 * 2, 38]
			],
			this);

		l_mtl.addAnimation(
			this._fDust2_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			-79,
			[
				3,
				[-110.8, 38]
			],
			this);

		l_mtl.addAnimation(
			this._fSmokePurple_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				1,
				[0.28, 0.01],
				[0.64, 4],
				[0, 23]
			],
			this);

		l_mtl.addAnimation(
			this._fSmokePurple_spr,
			MTimeLine.SET_SCALE,
			0.132 * 2,
			[
				1,
				[0.361 * 2, 27]
			],
			this);

		l_mtl.addAnimation(
			this._fSmokePurple_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				1,
				[-31.8, 27]
			],
			this);

		l_mtl.addAnimation(
			this._fMiddleLightParticle_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 3],
				[0, 23]
			],
			this);

		l_mtl.addAnimation(
			this._fMiddleLightParticle_spr,
			MTimeLine.SET_SCALE,
			1.5,
			[
				[2.76, 3],
				[1.58, 23]
			],
			this);
		
		l_mtl.addAnimation(
			this._fStartGlow_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				1,
				[1, 3],
				[0, 15]
			],
			this);

		l_mtl.addAnimation(
			this._fStartGlow_spr,
			MTimeLine.SET_SCALE,
			-0.3,
			[
				1,
				[0.96, 3],
				[-0.22, 15]
			],
			this);

		l_mtl.addAnimation(
			this._fStartGlow_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			30,
			[
				1,
				[48, 18]
			],
			this);

		l_mtl.callFunctionOnFinish(this._onAnimationCompleted, this);

		this._fTimeline_mtl = l_mtl;
	}

	getProgress()
	{
		return this._fTimeline_mtl.getProgress();
	}

   drop()
	{
		this.visible = false;

		this._fTimeline_mtl.stop();
		this._fTimeline_mtl.windToMillisecond(0);
	}

	get isDropped()
	{
		return !this.visible;
	}

	updateArea()
	{
		let lIsPortraitOrientation_bl = APP.layout.isPortraitOrientation;

		this._fContainer_sprt.x = lIsPortraitOrientation_bl ? 376 : 635;
	}

	startExplosionAnimation()
	{
		this._configureView();

		let l_mtl = this._fTimeline_mtl;
		l_mtl.play();

		this.visible = true;
	}

    startAnimationOut(aSeatId_str)
    {
        this._configureView(false, aSeatId_str);

        let l_mtl = this._fTimelineOut_mtl;
        l_mtl.play();

        this.visible = true;
    }

	_configureView()
	{
		this.updateArea();
	}

	_onAnimationCompleted()
	{
		this.drop();
	}

    destroy()
	{
		if (this._fTimeline_mtl)
		{
			this._fTimeline_mtl.stop();
			this._fTimeline_mtl.destroy();
		}
		this._fTimeline_mtl = null;

		this._fStartGlow_spr = null;
		this._fMiddleLightParticle_spr = null;
		this._fSmokePurple_spr = null;
		this._fDust2_spr = null;
		this._fDust1_spr = null;
		this._fContainer_sprt = null;

		super.destroy();
	}

}

export default BattlegroundAstronautLandingRocketExplosion;