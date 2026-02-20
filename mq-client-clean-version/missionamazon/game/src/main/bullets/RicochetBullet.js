import TextShadowDescriptorParser from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/text/shadow/TextShadowDescriptorParser';
import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { COOP_FIRE_FX_ALPHA } from '../../../../shared/src/CommonConstants';

const FLY_SPEED = 1.25;
let localId = 0;

const DEBUG_RICOCHETS_LIMIT = -1; // -1 for unlimited

class RicochetBullet extends Sprite
{
	get bulletId()
	{
		return this._bulletId;
	}

	get weaponId()
	{
		return this._weaponId;
	}

	get seatId()
	{
		return +this._bulletId.slice(0, 1);
	}

	get isMasterBullet()
	{
		let currentSeatId = APP.gameScreen.gameField.seatId;
		return this.seatId === currentSeatId;
	}

	get directionAngle()
	{
		return Math.PI-this._bulletAngle;
	}

	get bulletBetLevel()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		return lPlayerInfo_pi.getBetLevelByTurretSkinId(this._bulletViewType);
	}

	get bulletInitialBetLevel()
	{
		let lPlayerInfo_pi = APP.playerController.info;
		return lPlayerInfo_pi.getBetLevelByTurretSkinId(this._initialDefaultBulletId);
	}

	get lasthand()
	{
		return this._lasthand;
	}

	updateSkin(aNewSkinId)
	{
		this.defaultBulletId = aNewSkinId;
	}

	disableRicochet()
	{
		this._disableRicochet();
	}

	constructor(aWeapon_obj, startPos, endPos, bulletId = null, startTime = 0, lasthand = false)
	{
		super();

		this.localId = localId++;
		if (bulletId === null || bulletId === undefined)
		{
			let seatId = APP.gameScreen.gameField.seatId;
			this._bulletId = seatId + "_" + this.localId;
		}
		else
		{
			this._bulletId = bulletId;
		}

		this._fTicksCount_num = 0;
		this._fCollisionEnemy_e = null;
		this._startTime = startTime;
		this.currTime = this._startTime;
		this._fFire_spr = null;
		this._wallsCollisionPoints = null;
		this.defaultBulletId = aWeapon_obj.defaultWeaponId;
		this._initialDefaultBulletId = aWeapon_obj.defaultWeaponId;
		this._weaponId = aWeapon_obj.weaponId;
		this.startPos = startPos;
		this.endPos = endPos;
		this._bulletAngle = Math.atan2(this.endPos.y - this.startPos.y, this.endPos.x - this.startPos.x);
		this._bulletAngle = +this._bulletAngle.toFixed(2);
		this.fireRotation = this._bulletAngle + Math.PI/2;
		this._isRicochetAllowed = true;
		this._debugRicochetsLimit = DEBUG_RICOCHETS_LIMIT;
		this._lasthand = lasthand;

		this._fIsConfirmedByServer_bl = !this.isMasterBullet;//if is co-player's bullet, no need to wait for server approve 
		this._fIsDeniedByServer_bl = false;

		this._fCollisionInfo_obj = null;
		this._fIsRequired_bl = true;
		this._fIsShotCompletionExpected_bl = !!this.isMasterBullet;
		this._fIsViewCreated_bl = false;

		this.once('added', ()=>this.createView());
	}

	isRequired()
	{
		return this._fIsRequired_bl;
	}

	setIsRequired(aIsRequired_bl)
	{
		this.visible = aIsRequired_bl;
		this._fIsRequired_bl = aIsRequired_bl;
	}

	onBulletResponse()
	{
		//FLY OUT FROM GUN CONFIRMED BY SERVER
		this._fIsConfirmedByServer_bl = true;
	}

	isConfirmedOrDeniedByServer()
	{
		return this._fIsConfirmedByServer_bl || this._fIsDeniedByServer_bl;
	}


	isConfirmedByServer()
	{
		return this._fIsConfirmedByServer_bl;
	}

	setDeniedByServer()
	{
		this._fIsDeniedByServer_bl = true;
	}

	isDeniedByServer()
	{
		return this._fIsDeniedByServer_bl;
	}

	/**
	 * active are bullets that still can proceed Shot to the server
	 */
	get isActive()
	{
		return this._fIsShotCompletionExpected_bl;
	}

	get wallsCollisionPoints()
	{
		if (!this._wallsCollisionPoints)
		{
			let lBulletType = this._bulletViewType;
			switch(lBulletType)
			{
				case 5:
					this._wallsCollisionPoints = [new PIXI.Point(0, -40)];
					break;
				case 4:
					this._wallsCollisionPoints = [new PIXI.Point(0, -20)];
					break;
				case 3:
				case 2:
					this._wallsCollisionPoints = [new PIXI.Point(0, -18)];
					break;
				case 1:
				default:
					this._wallsCollisionPoints = [new PIXI.Point(0, -15)];
					break;
			}
		}

		return this._wallsCollisionPoints;
	}

	get _bulletViewType()
	{
		return this.defaultBulletId;
	}

	get _getTime()
	{
		return APP.gameScreen.accurateCurrentTime;
	}

	get localWallsCollisionPoints()
	{
		if (this._fFire_spr)
		{
			let points = [];
			for (let point of this.wallsCollisionPoints)
			{
				let newPoint = this._fFire_spr.localToLocal(point.x, point.y, this);
				points.push(new PIXI.Point(newPoint.x, newPoint.y));
			}
			return points;
		}

		return this.wallsCollisionPoints;
	}

	get isChangeDirectionAllowed()
	{
		return this._isRicochetAllowed;
	}

	_disableRicochet()
	{
		this._isRicochetAllowed = false;
		this._fIsShotCompletionExpected_bl = false;
	}

	createView()
	{
		this.addFire();
		this.startBulletFly(this.startPos, this.endPos, true);
		this._fIsViewCreated_bl = true;
	}

	startBulletFly(startPos, endPos, start = false)
	{
		if (this._debugRicochetsLimit === 0)
		{
			this._disableRicochet();
			return;
		}

		if (this._debugRicochetsLimit !== -1) --this._debugRicochetsLimit;

		this.startPos = startPos;
		this.endPos = endPos;
		if (!start)
		{
			this.startPos.x = +(this.startPos.x.toFixed(1));
			this.startPos.y = +(this.startPos.y.toFixed(1));
			this.endPos.x = +(this.endPos.x.toFixed(1));
			this.endPos.y = +(this.endPos.y.toFixed(1));
		}

		if (this.startPos.x == this.endPos.x) this.endPos.x+=1;
		if (this.startPos.y == this.endPos.y) this.endPos.y+=1;

		this.position.set(this.startPos.x, this.startPos.y);

		this._bulletAngle = Math.atan2(this.endPos.y - this.startPos.y, this.endPos.x - this.startPos.x);
		this._bulletAngle = +this._bulletAngle.toFixed(2);
		this.angleCos = Math.cos(this._bulletAngle);
		this.angleSin = Math.sin(this._bulletAngle);
		this.fireRotation = this._bulletAngle + Math.PI/2;

		if (this._fFire_spr)
		{
			this._fFire_spr.rotation = this.fireRotation;

			// Debug walls collision shape...
			// this._debugWallsCollisionShape();
			// ...Debug walls collision shape
		}
	}

	_debugWallsCollisionShape()
	{
		this._debugHitLine && this._debugHitLine.destroy();

		let points = this.localWallsCollisionPoints;
		this._debugHitLine = this.addChild(new PIXI.Graphics());
		this._debugHitLine.lineStyle(4, 0xff00ff, 0.8).drawPolygon(points);
	}

	onTick(aDelta_num)
	{
		this._fTicksCount_num += 1 * aDelta_num;

		if (!this.endPos)
		{
			return;
		}

		if (this._fFire_spr && this._fFire_spr.bulletType !== this._bulletViewType)
		{
			this._updateSkinView();
		}

		let step = aDelta_num * FLY_SPEED;
		this.x += this.angleCos*step;
		this.y += this.angleSin*step;

		let timeDiff = this._getTime - this.currTime - aDelta_num;

		if (timeDiff > 0)
		{
			let delta = aDelta_num / 2;
			if (this._lasthand)
			{
				delta *= 10; // Speed up x10 times
			}
			if (timeDiff - delta < 0)
			{
				delta = timeDiff;
			}
			this.currTime += delta;

			let step = delta * FLY_SPEED;
			this.x += this.angleCos*step;
			this.y += this.angleSin*step;
		}
		else
		{
			this._lasthand = false;

		}

		let lAlphaMultiplier_num = 1;// this.isMasterBullet ? 1 : 0.25;

		if(
			this._fFlash_s &&
			this._fIsViewCreated_bl
			)
		{

			if(this._fTicksCount_num < 100)
			{
				this._fFlash_s.alpha = 0;
			}
			else if(this._fFlash_s.alpha < 1 * lAlphaMultiplier_num)
			{
				let lAlpha_num = this._fFlash_s.alpha +  0.5 * lAlphaMultiplier_num;

				if(lAlpha_num > 1 * lAlphaMultiplier_num)
				{
					lAlpha_num = 1 * lAlphaMultiplier_num;
				}

				this._fFlash_s.alpha = lAlpha_num;
			}
		}
	}

	addFire()
	{
		this._fFire_spr = this.addChild(new Sprite());

		if (!this.isMasterBullet)
		{
			this._fFire_spr.alpha = COOP_FIRE_FX_ALPHA;
		}
		
		const lNipple_spr = this._fFire_spr.addChild(APP.library.getSpriteFromAtlas(this.nippleAssetName));

		let lFlash_s = this._fFire_spr.addChild(APP.library.getSpriteFromAtlas(this.bulletAssetName));
		lFlash_s.blendMode = PIXI.BLEND_MODES.ADD;
		lFlash_s.alpha = 0;

		const lBulletType = this._bulletViewType;
		switch (lBulletType)
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

		this._fFlash_s = lFlash_s;
		this._fFire_spr.bulletType = lBulletType;
	}

	get bulletAssetName()
	{
		let lBulletType = this._bulletViewType;

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
		switch (this._bulletViewType)
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

	_updateSkinView()
	{
		let lPrevRotation = this._fFire_spr.rotation;
		let lPrevVisible = this._fFire_spr.visible;
		this._fFire_spr.destroy();

		this.addFire();
		this._fFire_spr.rotation = lPrevRotation;
		this._fFire_spr.visible = lPrevVisible;
	}

	onCollisionOccurred(aEnemy_e)
	{
		this._fCollisionEnemy_e = aEnemy_e;
		this.setIsRequired(false);

		if(
			this.isMasterBullet &&
			this._fIsConfirmedByServer_bl
			)
		{
			this._tryToCompleteShot();
		}
	}

	isRicochetBullet()
	{
		return true;
	}

	_tryToCompleteShot()
	{
		this.setIsRequired(false);

		if(!this._fIsConfirmedByServer_bl)
		{
			return;
		}

		APP.gameScreen.gameField.onRicochetBulletShotOccurred(this, this._fCollisionEnemy_e);
		this._fCollisionEnemy_e = null;

		this._fIsShotCompletionExpected_bl = false;
	}

	tryToCompleteShotIfCollisionHappenedBeforeBulletWasApprovedByServer()
	{
		if(
			this.isMasterBullet &&
			!this._fIsRequired_bl &&
			this._fIsConfirmedByServer_bl &&
			this._fCollisionEnemy_e
			)
		{
			this._tryToCompleteShot();
		}
	}

	tick(delta, realDelta)
	{
		this.onTick(realDelta);
	}

	destroy()
	{
		super.destroy();

		this.startPos = undefined;
		this.endPos = undefined;
		this._bulletAngle = undefined;
		this.fireRotation = undefined;
		this.defaultBulletId = undefined;
		this._initialDefaultBulletId = undefined;
		this._fFire_spr = undefined;
		this._wallsCollisionPoints = undefined;
		this._isRicochetAllowed = undefined;
		this._debugRicochetsLimit = undefined;
		this._lasthand = undefined;
		this._fCollisionInfo_obj = null;
		this._fCollisionEnemy_e = null;
		this._fIsShotCompletionExpected_bl = undefined;
		this._fIsConfirmedByServer_bl = undefined;
		this._fIsDeniedByServer_bl = undefined;
	}
}

export default RicochetBullet;