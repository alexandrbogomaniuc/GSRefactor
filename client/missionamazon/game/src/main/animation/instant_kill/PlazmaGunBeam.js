import WeaponBeam from "../WeaponBeam";
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sequence from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import ShotResultsUtil from '../../ShotResultsUtil';
import Enemy from '../../enemies/Enemy';
import AtlasSprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/AtlasSprite';
import InstantKillProjectile from './InstantKillProjectile';

class PlazmaGunBeam extends WeaponBeam {

	static get EVENT_ON_TARGET_PRE_ACHIEVED() 		{ return 'EVENT_ON_TARGET_PRE_ACHIEVED'; }
	static get EVENT_ON_TARGET_ACHIEVED() 			{ return WeaponBeam.EVENT_ON_TARGET_ACHIEVED; }
	static get EVENT_ON_ANIMATION_COMPLETED() 		{ return WeaponBeam.EVENT_ON_ANIMATION_COMPLETED; }
	static get EVENT_ON_BASIC_ANIMATION_COMPLETED()	{ return 'EVENT_ON_BASIC_ANIMATION_COMPLETED'; }

	constructor(aShotData_obj, aTargetEnemyId_int, callback)
	{
		super(aShotData_obj);

		this._fTargetAchievingCallback_func = callback;
		this._fTargetEnemyId_int = aTargetEnemyId_int;
	}

	get _baseBeamLength()
	{
		return 280;
	}

	get _minimumBeamLength()
	{
		return 10;
	}

	get targetEnemyId()
	{
		return this._fTargetEnemyId_int;
	}

	//override
	__shoot(aStartPoint_pt, aEndPoint_pt)
	{
		super.__shoot(aStartPoint_pt, aEndPoint_pt);

		let projectile = this.addChild(new InstantKillProjectile());
		projectile.pivot.set(-20, 0);

		projectile.once(InstantKillProjectile.SCALE_COMPLETED, this._onProjectileScaleCompleted, this); 
		projectile.once(InstantKillProjectile.ANIMATION_COMPLETED, this._onProjectileAnimationCompleted, this); 
		projectile.shoot(Utils.getDistance(aStartPoint_pt, aEndPoint_pt));
	}

	_onProjectileScaleCompleted(event)
	{
		this.emit(PlazmaGunBeam.EVENT_ON_TARGET_PRE_ACHIEVED);
	}

	_onProjectileAnimationCompleted(event)
	{
		this._onTargetAchieved();
	}

	_onTargetAchieved()
	{
		let angle = Math.PI - this.rotation;
		this._fTargetAchievingCallback_func && this._fTargetAchievingCallback_func.call(null, this.endPoint, angle);
		this.emit(PlazmaGunBeam.EVENT_ON_TARGET_ACHIEVED);

		this._onBasicAnimationCompleted();
	}

	_onBasicAnimationCompleted()
	{
		this.emit(PlazmaGunBeam.EVENT_ON_BASIC_ANIMATION_COMPLETED);

		this._onAnimationCompleted();
	}

	_onAnimationCompleted()
	{
		this.emit(PlazmaGunBeam.EVENT_ON_ANIMATION_COMPLETED);
		this.destroy();
	}

	destroy()
	{
		this._fTargetAchievingCallback_func = null;
		this._fTargetEnemyId_int = undefined;

		this.removeAllListeners();

		super.destroy();
	}

}

export default PlazmaGunBeam;

