import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { WEAPONS } from '../../../../shared/src/CommonConstants';
import Sequence from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation/Sequence';
import {Z_INDEXES} from '../GameField';
import { COOP_FIRE_FX_ALPHA } from '../../../../shared/src/CommonConstants';

const DISTANCE_EPS = 10;//px

class Bullet extends Sprite
{
	static get EVENT_ON_SHOW_RICOCHET_EFFECT()	{ return 'EVENT_ON_SHOW_RICOCHET_EFFECT';}
	static get EVENT_ON_DESTROYED()				{ return 'EVENT_ON_DESTROYED';}
	static get EVENT_ON_DISAPPEAR()				{ return 'EVENT_ON_DISAPPEAR';}

	constructor(params, points, callback, aIsMasterBullet_bl)
	{
		super();
		
		this._fParams_obj = params;

		this.pointsArray = points.reverse();
		this._fCallback_func = callback;
		this._fIsMasterBullet_bl = aIsMasterBullet_bl;

		let startPos = this.pointsArray.pop();
		let endPos = this.pointsArray.pop();

		this.id = params.id || 0;
		this.typeId = params.typeId;
		this.defaultWeaponBulletId = params.defaultWeaponBulletId;
		this.radius = params.radius || 4;

		this.startPos = startPos;

		this.endPos = endPos;
		this.targetEnemy = params.targetEnemy;
		this.randomOffset = params.randomOffset;
		this._bulletAngle = Math.atan2(endPos.x - startPos.x, endPos.y - startPos.y) + Math.PI/2;
		this.preEndProps = (params.preEndPos === undefined) ? null : params.preEndPos;

		this._fFire_spr = null;
		this._fCircles_sprt_arr = null;
		this.pathCount = undefined;
		this._fWeaponScale = params.weaponScale ? params.weaponScale: 1;

		this.fireRotation = -this._bulletAngle - Math.PI/2;

		this.speed = this.getSpeed();

		this.trailTime = 0;

		this._fDisappeared_bl = false;
		this._fIsRequired_bl = true;

		this.targetEnemy && this.targetEnemy.once(Sprite.EVENT_ON_DESTROYING, this._onTargetEnemyDestroyed, this);

		this._fIsCallbackExecuted_bl = false;
		this._fFlash_s = null;

		this.once('added', () => {
			this.createView(callback);
		})
	}

	_onTargetEnemyDestroyed()
	{
		this.targetEnemy = null;
	}

	setIsRequired(aIsRequired_bl)
	{
		this._fIsRequired_bl = aIsRequired_bl;
	}

	isMissEffectRequired()
	{
		return true;
	}

	isRequired()
	{
		return this._fIsRequired_bl;
	}

	isConfirmedOrDeniedByServer()
	{
		return true;
	}

	getSpeed()
	{
		switch (this.typeId)
		{
			case WEAPONS.DEFAULT: 
				return 4;
			default: 
				return 3;
		}
	}

	createView(callback)
	{
		this.addFire();
		this.setPos(callback);
	}

	setPos(callback)
	{
		this.position.set(this.startPos.x, this.startPos.y);
		this.pathCount = 0;

		this.showDefaultTrajectory(callback);
		
		this.zIndex = Z_INDEXES.BULLET;
	}

	showDefaultTrajectory(callback)
	{
		let len = Math.sqrt(Math.pow(this.x - this.endPos.x, 2) + Math.pow(this.y - this.endPos.y, 2));
		let time = len / this.speed;

		if(this.pointsArray.length == 0)
		{
			//this.on('tick', this.onTick, this);
		}
		else
		{
			this.moveTo(this.endPos.x, this.endPos.y, time, null, () => {
				this.startPos.x = this.endPos.x;
				this.startPos.y = this.endPos.y;
				this.endPos = this.pointsArray.pop();

				this._bulletAngle = Math.atan2(this.endPos.x - this.startPos.x, this.endPos.y - this.startPos.y) + Math.PI/2;
				this.fireRotation = -this._bulletAngle - Math.PI/2;
				if (this._fFire_spr)
				{
					this._fFire_spr.rotation = this.fireRotation;
				}

				this.emit(Bullet.EVENT_ON_SHOW_RICOCHET_EFFECT, {x: this.startPos.x, y: this.startPos.y});
				this.showDefaultTrajectory(callback);
			});
		}
	}

	onTick(aDelta_num)
	{
		if (!this.endPos)
		{
			return;
		}
		
		let dist = Math.sqrt(Math.pow(this.x - this.endPos.x, 2) + Math.pow(this.y - this.endPos.y, 2));
		let passtDist = Math.sqrt(Math.pow(this.x - this.startPos.x, 2) + Math.pow(this.y - this.startPos.y, 2));
		
		let lFlashAlpha_num = passtDist / 5;

		if(this._fFlash_s)
		{
			if(lFlashAlpha_num > 1)
			{
				lFlashAlpha_num = 1;
			}

			this._fFlash_s.alpha = lFlashAlpha_num;
		}

		if (this.targetEnemy)
		{
			this.endPos = (this.targetEnemy && this.targetEnemy.transform) ? this.targetEnemy.getCenterPosition() : this.endPos;
			if (this.randomOffset) 
			{
				this.endPos.x += this.randomOffset.x;
				this.endPos.y += this.randomOffset.y;
			}
		}

		let step = aDelta_num * this.speed;
		if (
			!this._fIsCallbackExecuted_bl &&
			(
				dist < DISTANCE_EPS ||
				step > dist
			)
		)
		{

			if(this.isMissEffectRequired())
			{
				APP.gameScreen.gameField.showMissEffect(
					this.endPos.x,
					this.endPos.y,
					-1,
					null,
					this.defaultWeaponBulletId,
					this._fIsMasterBullet_bl);
			}	

			//this.off('tick', this.onTick, this);
			this.position.set(this.endPos.x, this.endPos.y);
			this._fCallback_func(this, this.endPos, this._bulletAngle);
			this._fIsCallbackExecuted_bl = true;
			this.disappear();
			return;
		}

		this._bulletAngle = Math.atan2(this.endPos.x - this.x, this.endPos.y - this.y) + Math.PI/2;
		this.fireRotation = -this._bulletAngle - Math.PI/2;
		if (this._fFire_spr)
		{
			this._fFire_spr.rotation = this.fireRotation;
		}

		let angle = this._bulletAngle - Math.PI;
		this.x = this.x + Math.cos(angle)*step;
		this.y = this.y - Math.sin(angle)*step;
	}


	get bulletAssetName()
	{
		let lBulletType = this.defaultWeaponBulletId;

		let lAssetName_str = "blend/bullet";

		switch (lBulletType)
		{
			case 1: lAssetName_str = 'weapons/DefaultGun/turret_1/bullet'; break;
			case 2: lAssetName_str = 'weapons/DefaultGun/turret_2/bullet'; break;
			case 3: lAssetName_str = 'weapons/DefaultGun/turret_3/bullet'; break;
			case 4: lAssetName_str = 'weapons/DefaultGun/turret_4/bullet'; break;
			case 5: lAssetName_str = 'weapons/DefaultGun/turret_5/bullet'; break;
		}

		return lAssetName_str;
	}

	get nippleAssetName()
	{
		let lAssetName_str;
		switch (this.defaultWeaponBulletId)
		{
			case 1: lAssetName_str = 'weapons/DefaultGun/turret_1/nipple'; break;
			case 2: lAssetName_str = 'weapons/DefaultGun/turret_2/nipple'; break;
			case 3: lAssetName_str = 'weapons/DefaultGun/turret_3/nipple'; break;
			case 4: lAssetName_str = 'weapons/DefaultGun/turret_4/nipple'; break;
			case 5: lAssetName_str = 'weapons/DefaultGun/turret_5/nipple'; break;
			default: lAssetName_str = 'weapons/DefaultGun/turret_1/nipple'; break;
		}

		return lAssetName_str;
	}

	addFire()
	{
		this._fFire_spr = this.addChild(new Sprite());

		if (!this._fIsMasterBullet_bl)
		{
			this._fFire_spr.alpha = COOP_FIRE_FX_ALPHA;
		}
		
		const lNipple_spr = this._fFire_spr.addChild(APP.library.getSpriteFromAtlas(this.nippleAssetName));

		let lFlash_s = this._fFire_spr.addChild(APP.library.getSpriteFromAtlas(this.bulletAssetName));
		lFlash_s.blendMode = PIXI.BLEND_MODES.ADD;
		lFlash_s.alpha = 0;

		switch (this.defaultWeaponBulletId)
		{
			case 1:
				lFlash_s.position.y = 35;
				lNipple_spr.position.set(0, 5);
				break;
			case 2:
				lFlash_s.position.y = 55;
				lNipple_spr.position.set(3, 13);
				break;
			case 3:
				lFlash_s.position.y = 55;
				lNipple_spr.position.set(3, 8);
				break;
			case 4:
				lFlash_s.position.y = 80;
				lNipple_spr.position.set(-4, 19);
				break;
			case 5:
				lFlash_s.position.y = 100;
				lNipple_spr.position.set(-5, 32);
				break;
		}

		this._fFire_spr.rotation = this.fireRotation;
		this._fFlash_s = lFlash_s;
	}

	disappear()
	{
		this.emit(Bullet.EVENT_ON_DISAPPEAR);
		//this.scaleTo(0.1, 50, null, () => this._onDisappeared());
		this._fDisappeared_bl = true;
		this.setIsRequired(false);
		//this.destroy();
	}

	get disappeared()
	{
		return this._fDisappeared_bl;
	}

	get isCompleted()
	{
		return this.disappeared;
	}

	_onDisappeared()
	{
		this._fDisappeared_bl = true;
		this.destroy();
	}

	destroy()
	{
		//this.off('tick', this.onTick, this);

		Sequence.destroy(Sequence.findByTarget(this));

		if (this._fFire_spr)
		{
			Sequence.destroy(Sequence.findByTarget(this._fFire_spr));
			this._fFire_spr = null;
		}

		if (this._fCircles_sprt_arr)
		{
			while (this._fCircles_sprt_arr.length)
			{
				this._fCircles_sprt_arr.pop().destroy();
			}
			this._fCircles_sprt_arr = null;
		}

		this.pointsArray = null;

		this.id = undefined;
		this.typeId = undefined;
		this.radius = undefined;
		this.startPos = null;
		this.endPos = null;
		
		this.targetEnemy && this.targetEnemy.off(Sprite.EVENT_ON_DESTROYING, this._onTargetEnemyDestroyed, this, true);
		this.targetEnemy = null;

		this.randomOffset = undefined;
		this._bulletAngle = undefined;
		this.preEndProps = null;
		this.pathCount = undefined;
		this.fireRotation = undefined;
		this.speed = undefined;

		this.trailTime = undefined;
		this._fDisappeared_bl = undefined;
		this._fIsCallbackExecuted_bl = false;

		this._fParams_obj = null;

		super.destroy();
	}

	isRicochetBullet()
	{
		return false;
	}

	tick(delta)
	{
		this.onTick(delta);
		//this.emit('tick', {delta: delta});
	}
}

export default Bullet;