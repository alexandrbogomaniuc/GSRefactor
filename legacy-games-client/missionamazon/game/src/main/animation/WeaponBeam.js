import Sprite from '../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';

class WeaponBeam extends Sprite {

	static get EVENT_ON_TARGET_ACHIEVED() 		{ return 'EVENT_ON_TARGET_ACHIEVED' }
	static get EVENT_ON_ANIMATION_COMPLETED() 	{ return 'EVENT_ON_ANIMATION_COMPLETED' }

	constructor(aShotData_obj)
	{
		super();

		this._fShotData_obj = aShotData_obj;

		this._fStartPoint_pt = null;
		this._fEndPoint_pt = null;
		this._fTargetDistance_num = undefined;
	}

	get shotData()
	{
		return this._fShotData_obj;
	}

	get endPoint()
	{
		return this._fEndPoint_pt;
	}

	set endPoint(aValue_pt)
	{
		this._fEndPoint_pt = aValue_pt;
		this.__invalidateLocation();
	}

	get startPoint()
	{
		return this._fStartPoint_pt;
	}

	set startPoint(aValue_pt)
	{
		this._fStartPoint_pt = aValue_pt;
		this.__invalidateLocation();
	}

	//abstract
	get _baseBeamLength()
	{
		throw new Error ('_baseBeamLength is an abstract, should be overriden');
	}

	//abstract
	get _minimumBeamLength()
	{
		throw new Error ('_minimumBeamLength is an abstract, should be overriden');	
	}

	get _targetDistance()
	{
		if (!this._fStartPoint_pt || !this._fEndPoint_pt)
		{
			throw new Error ('target distance can\'t be calculated, because start or end point is unknown');
		}
		return this._fTargetDistance_num || (this._fTargetDistance_num = Utils.getDistance(this._fStartPoint_pt, this._fEndPoint_pt));
	}

	get _beamLength()
	{
		return Math.max(this._minimumBeamLength, this._targetDistance);
	}

	get shotData()
	{
		return this._fShotData_obj;
	}

	i_shoot(aStartPoint_pt, aEndPoint_pt)
	{
		this.__shoot(aStartPoint_pt, aEndPoint_pt);
	}

	__shoot(aStartPoint_pt, aEndPoint_pt)
	{
		this._fStartPoint_pt = aStartPoint_pt;
		this._fEndPoint_pt = aEndPoint_pt;

		this.__invalidateLocation();
	}

	__invalidateLocation()
	{
		this._fTargetDistance_num = undefined;

		this._updatePosition();
		this._updateRotation();
		this._updateScale();
	}

	_updatePosition()
	{
		this.position.set(this._fStartPoint_pt.x, this._fStartPoint_pt.y);
	}

	_updateRotation()
	{
		this.rotation = Math.PI - Utils.getAngle(this._fStartPoint_pt, this._fEndPoint_pt) - Math.PI/2;	
	}

	_updateScale()
	{
		this.scale.x = this._beamLength / this._baseBeamLength;
	}

	destroy()
	{
		this._fShotData_obj = null;
		this._fStartPoint_pt = null;
		this._fEndPoint_pt = null;

		this.removeAllListeners();
		super.destroy();
	}
}

export default WeaponBeam;