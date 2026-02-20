import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
//import MissEffect from '../../missEffects/MissEffect';
import RicochetEternalLaserFlare from './RicochetEternalLaserFlare';
import RicochetEternalSparks from './RicochetEternalSparks';
import DeathFxAnimation from '../death/DeathFxAnimation';
import * as Easing from '../../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Timer from "../../../../../../common/PIXI/src/dgphoenix/unified/controller/time/Timer";
import RicochetFireEffect from './RicochetFireEffect';
import PlayerSpot from '../../playerSpots/PlayerSpot';

const LASER_RAY_APPEARANCE_TIME = 20*2*16.7 * 3.5;

class RicochetLaserRay extends Sprite
{
	static get EVENT_ADDED () {return "EVENT_ADDED";}
	static get EVENT_READY_FOR_SAFE_REMOVE () {return "EVENT_READY_FOR_SAFE_REMOVE";}
	static get EVENT_DISAPPEARED () {return "EVENT_DISAPPEARED";}	

	static get FULL_APPEARANCE_TIME() { return LASER_RAY_APPEARANCE_TIME; }
	static get HALF_APPEARANCE_TIME() { return LASER_RAY_APPEARANCE_TIME/2; }

	get isReadyForSafeRemove()
	{
		return this._fIsReadyForSafeRemove_bl;
	}

	get shotData()
	{
		return this._fShotData_obj;
	}

	constructor(index, startDelay, appearanceDuration, startParams, endParams, aShotData_obj=false)
	{
		super();

		this.index = index;
		this.appearanceDuration = appearanceDuration;
		this.name = 'RicochetLaserRay ' + index;
		this.ray = null;
		this.rayOriginalHeight = undefined;
		this.rayEndContainer = null;
		this.rayBeginContainer = null;
		this.shadow = null;
		this.shadowOriginalHeight = null;
		this._fReadyForCompleteTimer_t = null;
		this._fCreateTimer_t = null;
		this.fire = null;

		this.startPos = null;
		this.startParams = startParams;
		this.endPos = null;
		this.endParams = endParams;
		this._fShotData_obj = aShotData_obj;

		if (startDelay > 0)
		{
			this._startCreateTimer(startDelay);
		}
		else
		{
			this.once('added', (e) => {
				this._onAdded();
			});
		}
	}

	get _gf()
	{
		return APP.currentWindow.gameField;
	}

	disappear()
	{
		this.fadeTo(0, 3*2*16.7, null, this._onDisappeared.bind(this));
	}

	_onDisappeared()
	{
		this.emit(RicochetLaserRay.EVENT_DISAPPEARED);
	}

	destroy()
	{
		this._destroyCreateTimer();
		this._destroyReadyForCompleteTimer();

		APP.off("tick", this._onTick, this);

		this.index = null;
		this.appearanceDuration = null;
		this.name = null;
		this.ray = null;
		this.rayOriginalHeight = null;
		this.rayEndContainer = null;
		this.rayBeginContainer = null;
		this.shadow = null;
		this.shadowOriginalHeight = null;
		this._fReadyForCompleteTimer_t = null;
		this._fCreateTimer_t = null;
		this.fire = null;
		this._fIsReadyForSafeRemove_bl = null;

		this.startPos = null;
		this.startParams = null;
		this.endPos = null;
		this.endParams = null;
		this._fShotData_obj = null;
		
		super.destroy();
	}

	_startCreateTimer(startDelay)
	{
		this._fCreateTimer_t = new Timer(this._onCreateTimeoutCompleted.bind(this), startDelay);
	}

	_destroyCreateTimer()
	{
		this._fCreateTimer_t && this._fCreateTimer_t.destructor();
		this._fCreateTimer_t = null;
	}

	_onCreateTimeoutCompleted()
	{
		this._destroyCreateTimer();

		this._createView();
	}

	_onAdded()
	{
		this._createView();
	}
	
	_createView()
	{
		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.shadow = this.addChild(APP.library.getSpriteFromAtlas('weapons/RicochetGun/laser_back_shadow'));
			this.shadow.anchor.set(0.5, 166/979);
			this.shadow.alpha = 0.8;
			this.shadowOriginalHeight = this.shadow.getBounds().height;
		}

		this.ray = this.addChild(APP.library.getSpriteFromAtlas("weapons/RicochetGun/laser_ray"));
		this.ray.anchor.set(0.5, 0);		
		this.rayOriginalHeight = this.ray.getBounds().height;
		this.ray.scale.x = 1.5;
		this.ray.alpha = 0.9;

		this.rayEndContainer = this.addChild(new Sprite);
		this.rayBeginContainer = this.addChild(new Sprite);
		
	
		let lensFlare = this.rayEndContainer.addChild(APP.library.getSpriteFromAtlas("common/lensflare"));			
		lensFlare.scale.set(0);
		lensFlare.blendMode = PIXI.BLEND_MODES.ADD;					
		lensFlare.scaleTo(0.75, 2*29*16.6, Easing.cubic.cubicOut);
		
		let bendSmoother = this.rayEndContainer.addChild(APP.library.getSpriteFromAtlas("common/lensflare"));
		bendSmoother.scale.set(0.2);
		bendSmoother.blendMode = PIXI.BLEND_MODES.ADD;		

		let dieSmoke;
		if(APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			DeathFxAnimation.initTextures();
			dieSmoke = this.rayEndContainer.addChild(new Sprite);
			dieSmoke.textures = DeathFxAnimation.textures['smokeFx'];
			dieSmoke.anchor.set(0.5, 0.62);
			dieSmoke.scale.set(2);
			dieSmoke.blendMode = PIXI.BLEND_MODES.SCREEN;
			dieSmoke.play();
			dieSmoke.once('animationend', (e) => {
				e.target.destroy();
			})
		}

		let lEndEnemy_enm = this.endParams.enemyId !== undefined ? this._gf.getExistEnemy(this.endParams.enemyId) : null;
		if (lEndEnemy_enm)
		{
			let eternalLaserFlare = this.rayEndContainer.addChild(new RicochetEternalLaserFlare());
			eternalLaserFlare.scale.x = 1.2;
			eternalLaserFlare.scale.y = 2.302;

			let eternalSparks = this.rayEndContainer.addChild(new RicochetEternalSparks());
		}
		else 
		{
			lensFlare.renderable = false;
			APP.profilingController.info.isVfxProfileValueMediumOrGreater && (dieSmoke.renderable = false);
		}

		if (APP.profilingController.info.isVfxProfileValueMediumOrGreater)
		{
			this.fire = this.rayBeginContainer.addChild(new RicochetFireEffect())
		}

		this._updateView();

		APP.on("tick", this._onTick, this);

		this.emit(RicochetLaserRay.EVENT_ADDED, {finalPosition: this.endPos, angle: this.ray ? this.ray.rotation : 0});

		this._fReadyForCompleteTimer_t = new Timer(this._onReadyForCompleteTimeoutCompleted.bind(this), this.appearanceDuration);
	}

	_onTick(e)
	{
		this._updateView();
	}

	_onReadyForCompleteTimeoutCompleted()
	{
		this._destroyReadyForCompleteTimer();

		this._fIsReadyForSafeRemove_bl = true;
		this.emit(RicochetLaserRay.EVENT_READY_FOR_SAFE_REMOVE);
	}

	_destroyReadyForCompleteTimer()
	{
		this._fReadyForCompleteTimer_t && this._fReadyForCompleteTimer_t.destructor();
		this._fReadyForCompleteTimer_t = null;	
	}

	_updateView()
	{
		this._updateStartPosition();
		this._updateEndPosition();

		this._redraw();	
	}

	_updateStartPosition()
	{
		let lStartGlobalPos_obj = this.startParams.basePoint;
		let lSeatId_num = this.startParams.seatId;
		if (this.startParams.enemyId !== undefined)
		{
			let lEnemyPos_obj = this._gf.getEnemyPosition(this.startParams.enemyId) || this.startParams.basePoint;
			lStartGlobalPos_obj = lEnemyPos_obj;
		}
		else if (lSeatId_num !== undefined)
		{
			let playerSeat = this._gf.getSeat(lSeatId_num, true);

			if (playerSeat)
			{
				let lGunDirectionPoint_pt = this.endParams.basePoint;
				
				this._gf.rotatePlayerGun(lSeatId_num, lGunDirectionPoint_pt.x, lGunDirectionPoint_pt.y);

				lStartGlobalPos_obj = playerSeat.muzzleTipGlobalPoint;
			}
		}

		this.startPos = this.parent.globalToLocal(lStartGlobalPos_obj.x, lStartGlobalPos_obj.y);
	}

	_updateEndPosition()
	{
		let lEnemyPos_obj = this.endParams.basePoint;
		if (this.endParams.enemyId !== undefined)
		{
			lEnemyPos_obj = this._gf.getEnemyPosition(this.endParams.enemyId) || this.endParams.basePoint;
		}

		this.endPos = this.parent.globalToLocal(lEnemyPos_obj.x, lEnemyPos_obj.y);
	}

	_redraw()
	{
		if (!this.startPos || !this.endPos) {
			return;
		}

		var dist = Utils.getDistance(this.startPos, this.endPos);
		this.position.set(this.startPos.x, this.startPos.y);

		// ray...
		this.ray.scale.y = dist/this.rayOriginalHeight;
		this.ray.rotation = -Utils.getAngle(this.startPos, this.endPos);
		//...ray

		//shadow...
		if(this.shadow)
		{
			this.shadow.scale.y = dist/(this.shadowOriginalHeight - 160);
			this.shadow.rotation = this.ray.rotation;
		}
		//...shadow

		let fxPosition = { x: this.endPos.x - this.startPos.x, y: this.endPos.y - this.startPos.y };

		this.rayEndContainer.position.set(fxPosition.x, fxPosition.y);
		
		//laserFlare anchor 173/321, 90/180
		//laserSmoke anchor 86/171, 101/183
	}
}

export default RicochetLaserRay;