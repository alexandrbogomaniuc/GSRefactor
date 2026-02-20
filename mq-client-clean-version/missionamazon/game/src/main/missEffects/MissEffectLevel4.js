import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import MissEffect from './MissEffect';
import MTimeLine from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MTimeLine';
import MAnimation from '../../../../../common/PIXI/src/dgphoenix/unified/controller/timeLine/MAnimation';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CommonEffectsManager from './../CommonEffectsManager';

class MissEffectLevel4 extends MissEffect
{
	constructor()
	{
		super();
	}

	//override
	init()
	{
		this.__fTimeLine_mt = new MTimeLine();

		this._addHitflash();
		this._addDieSmoke();
		this._addSphere();

		this._addBigSmoke();

		this._addRedSmoke1();
		this._addRay1();
		this._addRay2();
		this._addRay3();
		this._addRedSmoke2();
		this._addRedSmoke3();
		this._addRedSmoke4();
		this._addRedSmoke5();
		this.scale.set(0.75);
	}

	_addHitflash()
	{
		const lHitflash_spr = this.addChild(APP.library.getSprite("hitflash"));
		lHitflash_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lHitflash_spr.scale.set(0.19);

		this.__fTimeLine_mt.addAnimation(
			lHitflash_spr,
			MTimeLine.SET_ALPHA,
			0,
			[
				[1, 1],
				1,
				[0, 2],
			]);
		
		this.__fTimeLine_mt.callFunctionAtFrame(
			lHitflash_spr.show,
			1,
			lHitflash_spr);

		this.__fTimeLine_mt.callFunctionAtFrame(
			lHitflash_spr.hide,
			4,
			lHitflash_spr);
		//...FLASH
	}

	_addDieSmoke()
	{
		const lDieSmoke = this.addChild(new Sprite());
		lDieSmoke.textures = CommonEffectsManager.getDieSmokeUnmultTextures();
		lDieSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
		lDieSmoke.animationSpeed = 0.5;
		lDieSmoke.anchor.set(0.57, 0.81);
		lDieSmoke.scale.set(2);
		lDieSmoke.on("animationend", () =>
		{
			lDieSmoke.stop();
		});

		this.__fTimeLine_mt.callFunctionAtFrame(
			lDieSmoke.play,
			11,
			lDieSmoke);
	}

	_addSphere()
	{
		const lSphere_spr = this.addChild(APP.library.getSprite("enemy_impact/circle_blast/circle_blast_4"));
		lSphere_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lSphere_spr.scale.set(0);

		this.__fTimeLine_mt.addAnimation(
			lSphere_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				[0.96 * 2.74, 13]
			]);

		this.__fTimeLine_mt.addAnimation(
			lSphere_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				6,
				[0, 6],
			]);
	}

	_addBigSmoke()
	{
		const lBigSmoke_spr = this.addChild(APP.library.getSprite("enemy_impact/default_impact_smoke"));
		lBigSmoke_spr.blendMode = PIXI.BLEND_MODES.SCREEN;
		lBigSmoke_spr.scale.set(0);

		this.__fTimeLine_mt.addAnimation(
			lBigSmoke_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				4,
				[1.66, 16, MAnimation.EASE_OUT]
			]);

		this.__fTimeLine_mt.addAnimation(
			lBigSmoke_spr,
			MTimeLine.SET_ALPHA,
			0.7,
			[
				9,
				[0, 7]
			]);
	}

	_addRedSmoke1()
	{
		const lRedSmoke1_spr = this.addChild(APP.library.getSprite("enemy_impact/explosion/explosion_4_5"));
		lRedSmoke1_spr.scale.set(0);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke1_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				13,
				[2.265 * 0.75, 20]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke1_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				23,
				[0, 10]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke1_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 35]
			]);
	}

	_addRay1()
	{
		const lRedRay1_spr = this.addChild(APP.library.getSprite("enemy_impact/circle_blast/sparkle_red_4"));
		lRedRay1_spr.scale.set(0);
		lRedRay1_spr.blendMode = PIXI.BLEND_MODES.ADD;
		lRedRay1_spr.position.set(0, 0);

		this.__fTimeLine_mt.addAnimation(
			lRedRay1_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				6,
				[0.37 * 0.75, 16, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay1_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				17,
				[0, 7]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay1_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 32]
			]);
	}

	_addRay2()
	{
		const lRedRay2_spr = this.addChild(APP.library.getSprite("enemy_impact/circle_blast/sparkle_red_4"));
		lRedRay2_spr.scale.set(0);
		lRedRay2_spr.position.set(-26, -19.5);
		lRedRay2_spr.blendMode = PIXI.BLEND_MODES.ADD;

		this.__fTimeLine_mt.addAnimation(
			lRedRay2_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				10,
				[0.37 * 0.75, 16, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT],
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay2_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				30,
				[0, 3]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay2_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 32]
			]);
	}

	_addRay3()
	{
		const lRedRay3_spr = this.addChild(APP.library.getSprite("enemy_impact/circle_blast/sparkle_red_4"));
		lRedRay3_spr.scale.set(0);
		lRedRay3_spr.position.set(-15, -34);
		lRedRay3_spr.blendMode = PIXI.BLEND_MODES.ADD;

		this.__fTimeLine_mt.addAnimation(
			lRedRay3_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				15,
				[0.37 * 0.75, 16, MAnimation.EASE_IN],
				[0, 7, MAnimation.EASE_OUT]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay3_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				35,
				[0, 3]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedRay3_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 32]
			]);
	}

	_addRedSmoke2()
	{
		const lRedSmoke2_spr = this.addChild(APP.library.getSprite("enemy_impact/explosion/explosion_4_5"));
		lRedSmoke2_spr.scale.set(0);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke2_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				6,
				[0.48 * 2.265 * 0.75, 20]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke2_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				16,
				[0, 10]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke2_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 35]
			]);
	}

	_addRedSmoke3()
	{
		const lRedSmoke3_spr = this.addChild(APP.library.getSprite("enemy_impact/explosion/explosion_4_5"));
		lRedSmoke3_spr.scale.set(0);
		lRedSmoke3_spr.position.set(14, -38);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke3_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				11,
				[0.48 * 2.265 * 0.75, 20]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke3_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				21,
				[0, 10]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke3_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, -35]
			]);
	}

	_addRedSmoke4()
	{
		const lRedSmoke4_spr = this.addChild(APP.library.getSprite("enemy_impact/explosion/explosion_4_5"));
		lRedSmoke4_spr.scale.set(0);
		lRedSmoke4_spr.position.set(18, -21);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke4_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				12,
				[0.48 * 2.265 * 0.75, 20]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke4_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				22,
				[0, 10]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke4_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, 35]
			]);
	}

	_addRedSmoke5()
	{
		const lRedSmoke5_spr = this.addChild(APP.library.getSprite("enemy_impact/explosion/explosion_4_5"));
		lRedSmoke5_spr.scale.set(0);
		lRedSmoke5_spr.position.set(-33, 2);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke5_spr,
			MTimeLine.SET_SCALE,
			0,
			[
				8,
				[0.48 * 2.265 * 0.75, 20]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke5_spr,
			MTimeLine.SET_ALPHA,
			1,
			[
				20,
				[0, 10]
			]);

		this.__fTimeLine_mt.addAnimation(
			lRedSmoke5_spr,
			MTimeLine.SET_ROTATION_IN_DEGREES,
			0,
			[
				4,
				[22, -35]
			]);
	}
}

export default MissEffectLevel4;