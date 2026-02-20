import FlyingDebris from './FlyingDebris';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';

class RockFlyingDebris extends FlyingDebris {

	constructor(params, points, callback) {
		super(params, points, callback);

		this.index = -1;
	}

	//override
	getSpeed(){
		return Utils.random(1, 2.5, true);
	}

	_getRandomAssetIndex()
	{
		if (this._fRandomAssetIndex_int === undefined)
		{
			this._fRandomAssetIndex_int = Utils.random(1, 3);
		}
		return this._fRandomAssetIndex_int;
	}

	//override
	_initBaseSprite()
	{
		let lDebrisSprite_sprt;
		let n;
		if (this._fParams_obj.index !== undefined)
		{
			n = this._fParams_obj.index;
		}
		else
		{
			n = this._getRandomAssetIndex();
		}
		this.index = n;

		lDebrisSprite_sprt = APP.library.getSpriteFromAtlas('boss_mode/golem/rocks/rock_' + n);

		return lDebrisSprite_sprt;
	}

	addFire()
	{
		super.addFire();

		let lScale_obj = this._getAdditionalScale();
		lScale_obj.x *= 0.26 * this._getBaseScale() * this._getRandomScale() * 1.3;
		lScale_obj.y *= 0.33 * this._getBaseScale() * this._getRandomScale() * 1.3;
		this.fire.scale.set(lScale_obj.x, lScale_obj.y);

		this.shadow.view.scale.x = this.fire.scale.x * 0.7;
		this.shadow.view.scale.y = this.fire.scale.y * 0.5;
		this.shadow.view.alpha = 0.3;
	}

	_getAdditionalScale()
	{
		let lScale_obj = {x: 1, y: 1};
		switch (this.index)
		{
			case 1:
				if (Math.random() > 0.5)
				{
					lScale_obj.x = 1;
					lScale_obj.y = 1.4;
				}
				else
				{
					lScale_obj.x = 0.9;
					lScale_obj.y = 1.1;
				};
				break;
			case 2:
				if (Math.random() > 0.5)
				{
					lScale_obj.x = 0.7;
					lScale_obj.y = 1.1;
				}
				else
				{
					lScale_obj.x = 0.7;
					lScale_obj.y = 0.6;
				};
				break;
			case 3:
				if (Math.random() > 0.5)
				{
					lScale_obj.x = 0.8;
					lScale_obj.y = 1;
				}
				else
				{
					lScale_obj.x = 0.8;
					lScale_obj.y = 0.7;
				};
				break;
		}
		if (this._fParams_obj.additionalScale != null)
		{
			lScale_obj.x *= this._fParams_obj.additionalScale.x;
			lScale_obj.y *= this._fParams_obj.additionalScale.y;
		}
		return lScale_obj;
	}

	_getRandomScale()
	{
		return Math.random() > 0.5 ? 1 : -1;
	}
}

export default RockFlyingDebris;