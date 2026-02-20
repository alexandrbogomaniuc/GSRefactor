import WeaponBeam from '../WeaponBeam';
import Enemy from '../../enemies/Enemy';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import ShotResultsUtil from '../../ShotResultsUtil';
import PlayerSpot from '../../playerSpots/PlayerSpot';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import CommonEffectsManager from '../../CommonEffectsManager';
import FlameThrowerBeamFlameView from './FlameThrowerBeamFlameView';
import FlameThrowerBeamBackSmokeView from './FlameThrowerBeamBackSmokeView';
import FlameThrowerBeamHitBoom from './FlameThrowerBeamHitBoom';
import { MUZZLE_DISTANCE, MUZZLE_LENGTH } from './FlameThrowerGun';
import { FRAME_RATE } from '../../../../../shared/src/CommonConstants';

class FlameThrowerBeam extends WeaponBeam
{
	static get EVENT_ON_TARGET_ACHIEVED()			{ return WeaponBeam.EVENT_ON_TARGET_ACHIEVED; }
	static get EVENT_ON_ANIMATION_COMPLETED()		{ return WeaponBeam.EVENT_ON_ANIMATION_COMPLETED; }
	static get EVENT_ON_BASIC_ANIMATION_COMPLETED()	{ return 'EVENT_ON_BASIC_ANIMATION_COMPLETED'; }
	static get EVENT_ON_ROTATION_UPDATED()			{ return 'EVENT_ON_ROTATION_UPDATED'; }

	//override
	get _baseBeamLength()
	{
		return FlameThrowerBeamFlameView.BASE_BEAM_LENGTH;
	}

	//override
	get _minimumBeamLength()
	{
		return 50;
	}

	get targetEnemy()
	{
		return this._fTargetEnemy_enm;
	}
	
	constructor(aShotData_obj, callback, weaponScale)
	{
		super(aShotData_obj);

		this._fTargetAchievingCallback_func = callback;
		this._fTargetEnemy_enm = null;
		this._fScaleableBase_sprt = null;
		this._fPlayerSeat_ps = null;
		this._fSkomeSequences_arr = [];
		this._fWeaponScale = weaponScale ? weaponScale: 1;

		this._fScaleableBase_sprt = this.addChild(new Sprite);
		this._fScaleableBase_sprt.pivot.set(0, 0);

		this._fDieSmokesAmount_num = 0;
	}

	//override
	__shoot(aStartPoint_pt, aEndPoint_pt)
	{
		super.__shoot(aStartPoint_pt, aEndPoint_pt);

		this._fDieSmokesAmount_num = 0;

		let lTargetEnemyId_int = ShotResultsUtil.extractTargetEnemyId(this.shotData);
		this._startFollowingEnemy(lTargetEnemyId_int);

		this._fPlayerSeat_ps = APP.currentWindow.gameField.getSeat(this.shotData.seatId, true);

		if (APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater)
		{
			this._addFlameBackWhiteSmoke();
			this._addFlameBackBlackSmoke();
		}

		let lFlame_sprt = this._fScaleableBase_sprt.addChild(new FlameThrowerBeamFlameView());
		lFlame_sprt.once(FlameThrowerBeamFlameView.EVENT_ON_FLAME_TARGET_ACHIEVED, this._onTargetAchieved, this);
		lFlame_sprt.once(FlameThrowerBeamFlameView.EVENT_ON_FLAME_TARGET_ANIMATION_COMPLETED, this._onBasicAnimationCompleted, this);
		lFlame_sprt.startAnimation();

		// this._addHitBoom();

		APP.on('tick', this._onTick, this);
	}

	//FXs...
	_addFlameBackBlackSmoke()
	{
		let lFlameBackSmoke_sprt = this._fScaleableBase_sprt.addChild(new FlameThrowerBeamBackSmokeView());
		lFlameBackSmoke_sprt.startAnimation();

		let maskGr = this.addChild(APP.library.getSpriteFromAtlas('weapons/FlameThrower/flame_back_smoke_mask'));
		maskGr.anchor.set(0, 0.5);
		maskGr.scale.set(4);
		
		lFlameBackSmoke_sprt.mask = maskGr;
	}

	_addFlameBackWhiteSmoke()
	{
		/*AE layers 61-74*/
		this._addDieSmoke(new PIXI.Point(0, 0), new PIXI.Point(0.6, 0.6), 90, PIXI.BLEND_MODES.SCREEN, -1);
		this._addDieSmoke(new PIXI.Point(0+12, 0-1.5+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -3);
		this._addDieSmoke(new PIXI.Point(0+32, 0-3.5+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -4);
		this._addDieSmoke(new PIXI.Point(0+58, 0-6+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -5);
		this._addDieSmoke(new PIXI.Point(0+84, 0-7+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -6);
		this._addDieSmoke(new PIXI.Point(0+103, 0-11+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -7);
		this._addDieSmoke(new PIXI.Point(0+123, 0-14+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -8);
		
		this._addDieSmoke(new PIXI.Point(0, 0), new PIXI.Point(0.6, 0.6), 90, PIXI.BLEND_MODES.SCREEN, -13);
		this._addDieSmoke(new PIXI.Point(0+12, 0-1.5+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -14);
		this._addDieSmoke(new PIXI.Point(0+32, 0-3.5+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -15);
		this._addDieSmoke(new PIXI.Point(0+58, 0-6+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -16);
		this._addDieSmoke(new PIXI.Point(0+84, 0-7+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -17);
		this._addDieSmoke(new PIXI.Point(0+103, 0-11+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -18);
		this._addDieSmoke(new PIXI.Point(0+123, 0-14+10), new PIXI.Point(0.4, 0.4), 90, PIXI.BLEND_MODES.SCREEN, -19);
	}

	_addHitBoom()
	{
		let lFlameHitBoom_sprt = this._fScaleableBase_sprt.addChild(new FlameThrowerBeamHitBoom());
		lFlameHitBoom_sprt.startAnimation();
	}

	_addFinalSmokes()
	{
		this._addDieSmoke(new PIXI.Point(0, 0), new PIXI.Point(0.6, 0.6), 80, PIXI.BLEND_MODES.SCREEN, -2);  //AE layer 44

		this._addDieBlackSmoke(new PIXI.Point(0, 0), new PIXI.Point(0.6, 0.6), 0.4); //AE layer 30
		this._addDieBlackSmoke(new PIXI.Point(0+22, 0-3.5), new PIXI.Point(0.6, 0.6), 0.4, 0.75); //AE layer 29
		this._addDieBlackSmoke(new PIXI.Point(0+53, 0-6.5), new PIXI.Point(0.6, 0.6), 0.7, 1.5); //AE layer 28
		this._addDieBlackSmoke(new PIXI.Point(0+80, 0-8), new PIXI.Point(0.6, 0.6), 0.7, 2.25); //AE layer 27
		this._addDieBlackSmoke(new PIXI.Point(0+108, 0-10), new PIXI.Point(0.6, 0.6), 0.7, 2.25); //AE layer 26
	}

	_addDieBlackSmoke(position, scaleMultiplier, alphaBaseValue, delay=0)
	{
		let lSmoke_sprt = this._addDieSmoke(position, scaleMultiplier, 90, PIXI.BLEND_MODES.MULTIPLY, 9);
		if (!lSmoke_sprt) return;

		lSmoke_sprt.alpha = 0;
		lSmoke_sprt.tint = 0x000000;
		
		let seq = [{tweens: [{ prop: 'alpha', to: Utils.getRandomWiggledValue(alphaBaseValue, 0.05) }], duration: 3*FRAME_RATE}];

		if (delay > 0)
		{
			seq.unshift({tweens: [], duration: delay*FRAME_RATE});
		}

		let blackSmokeSeq = Sequence.start(lSmoke_sprt, seq);

		this._fSkomeSequences_arr = this._fSkomeSequences_arr || [];
		this._fSkomeSequences_arr.push(blackSmokeSeq);
	}

	_addDieSmoke(position, scaleMultiplier, rotationInGrad, blendMode=PIXI.BLEND_MODES.NORMAL, startFrame=0, totalFrames=undefined)
	{
		if (!APP.profilingController.info.isVfxDynamicProfileValueMediumOrGreater) return null;

		let lSmoke_sprt = Sprite.createMultiframesSprite(CommonEffectsManager.getDieSmokeUnmultTextures(), startFrame, totalFrames);
		this._fScaleableBase_sprt.addChild(lSmoke_sprt);

		lSmoke_sprt.animationSpeed = 30/60;
		lSmoke_sprt.blendMode = blendMode;

		lSmoke_sprt.anchor.set(0.57, 0.81);
		lSmoke_sprt.scale.set(2 * scaleMultiplier.x, 2 * scaleMultiplier.y);

		if (position !== undefined)
		{
			lSmoke_sprt.position.set(position.x, position.y);
		}

		if (rotationInGrad !== undefined)
		{
			lSmoke_sprt.rotation = Utils.gradToRad(rotationInGrad);
		}

		lSmoke_sprt.on('animationend', () => {
			lSmoke_sprt.destroy();

			--this._fDieSmokesAmount_num;
			this._onAnimationCompletedSuspision();
		});

		lSmoke_sprt.play();
		++this._fDieSmokesAmount_num;

		return lSmoke_sprt;
	}
	//...FXs

	_startFollowingEnemy(aTargetEnemyId_int)
	{
		let lTargetEnemy_enm = APP.currentWindow.gameField.getExistEnemy(aTargetEnemyId_int);
		if (lTargetEnemy_enm && lTargetEnemy_enm.parent)
		{
			this._fTargetEnemy_enm = lTargetEnemy_enm;
			this._fTargetEnemy_enm.on(Enemy.EVENT_ON_ENEMY_DESTROY, this._stopFollowingEnemy, this);
		}
	}

	_stopFollowingEnemy()
	{
		this._fTargetEnemy_enm && this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_DESTROY, this._stopFollowingEnemy, this);
		this._fTargetEnemy_enm = null;
	}

	_onTick(e)
	{
		//follow target enemy = update beam's end position
		if (this._fTargetEnemy_enm)
		{
			if (!this._fTargetEnemy_enm.parent)
			{
				this._stopFollowingEnemy();
			}
			else
			{
				let lEndPoint_pt = this._fTargetEnemy_enm.getCenterPosition();
				this.endPoint = lEndPoint_pt;
			}
		}

		//update beam start position
		if (this._fPlayerSeat_ps && this._fPlayerSeat_ps.parent)
		{
			let startPos = new PIXI.Point();
			let startAngle = this._fPlayerSeat_ps.weaponSpotView.rotation;
			let sign = this._fPlayerSeat_ps.isBottom ? 1 : -1;
			startPos.x = this._fPlayerSeat_ps.position.x + this._fPlayerSeat_ps.weaponSpotView.x * sign;
			startPos.y = this._fPlayerSeat_ps.position.y + this._fPlayerSeat_ps.weaponSpotView.y * sign;
			let gunPos = this._fPlayerSeat_ps.gunCenter;
			startPos.x += gunPos.x * this._fPlayerSeat_ps.scale.x;
			startPos.y += gunPos.y * this._fPlayerSeat_ps.scale.y;
			let lGunPushEffectCurrentDistance_num = this._fPlayerSeat_ps.weaponSpotView.gun.y; /*gun push effect might be in progress*/
			let dist = ( (MUZZLE_DISTANCE  *this._fWeaponScale + MUZZLE_LENGTH) - lGunPushEffectCurrentDistance_num) * this._fPlayerSeat_ps.scale.x * sign * PlayerSpot.WEAPON_SCALE;

			startPos.x -= Math.cos(startAngle + Math.PI/2)*dist;
			startPos.y -= Math.sin(startAngle + Math.PI/2)*dist;

			this.startPoint = this.parent.globalToLocal(startPos.x, startPos.y);
		}
	}

	_onTargetAchieved()
	{
		let seq = [
			{	tweens: [],
				duration: 8*FRAME_RATE,
				onfinish: () => {
					this._stopFollowingEnemy();
					let angle = Math.PI - this.rotation;
					this._fTargetAchievingCallback_func && this._fTargetAchievingCallback_func.call(null, this.endPoint, angle);
				}
			}
		];

		Sequence.start(this, seq);

		this.emit(FlameThrowerBeam.EVENT_ON_TARGET_ACHIEVED);
	}

	_onBasicAnimationCompleted()
	{
		// needs for unlocking weapon after the enemy
		this._addFinalSmokes();
		APP.off('tick', this._onTick, this);

		this.emit(FlameThrowerBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED);
	}

	_onAnimationCompletedSuspision()
	{
		if (this._fDieSmokesAmount_num <= 0)
		{
			this._onAnimationCompleted();
		}
	}

	_onAnimationCompleted()
	{
		this.emit(FlameThrowerBeam.EVENT_ON_ANIMATION_COMPLETED);
		this.destroy();
	}

	//override
	_updateScale()
	{
		let lScaleX_num = (this._beamLength / this._baseBeamLength);
		this._fScaleableBase_sprt.scale.x = lScaleX_num;
	}

	//override
	_updateRotation()
	{
		super._updateRotation();
		this.emit(FlameThrowerBeam.EVENT_ON_ROTATION_UPDATED, {endPoint: this.endPoint, seatId: this.shotData.seatId});
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this));

		while (this._fSkomeSequences_arr && this._fSkomeSequences_arr.length)
		{
			let seq = this._fSkomeSequences_arr.pop();
			seq && seq.destructor();
		}

		if (this._fTargetEnemy_enm)
		{
			this._fTargetEnemy_enm.off(Enemy.EVENT_ON_ENEMY_DESTROY, this._stopFollowingEnemy, this);
		}

		APP.off('tick', this._onTick, this);
		this.removeAllListeners();

		super.destroy();

		this._fScaleableBase_sprt = null;
		this._fSkomeSequences_arr = null;
		this._fTargetAchievingCallback_func = null;
		this._fTargetEnemy_enm = null;
		this._fPlayerSeat_ps = null;
		this._fDieSmokesAmount_num = undefined;
		this._fWeaponScale = null;
	}
}

export default FlameThrowerBeam;