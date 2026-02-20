import FlyingDebris from './FlyingDebris';
import { APP } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import { Utils } from '../../../../../common/PIXI/src/dgphoenix/unified/model/Utils';
import * as Easing from '../../../../../common/PIXI/src/dgphoenix/unified/model/display/animation/easing';
import { Sequence } from '../../../../../common/PIXI/src/dgphoenix/unified/controller/animation';
import { WEAPONS } from '../../../../shared/src/CommonConstants';

class CharcoalFlyingDebris extends FlyingDebris {

	constructor(params, points, callback) {
		super(params, points, callback);

		this.shadow = null;
		this._fRandomAssetIndex_int = undefined;
		this._fRedSprite_sprt = null;

	}

	//override
	getSpeed(){
		switch (this.typeId)
		{
			case WEAPONS.FLAMETHROWER_POWER_UP:
			case WEAPONS.FLAMETHROWER:
				return Utils.random(0.7, 1.5, true);
		}
		return Utils.random(0.8, 2, true);
	}

	//override
	addFire(){
			
		super.addFire();

		let n = this._getRandomAssetIndex();
		this._fRedSprite_sprt = APP.library.getSpriteFromAtlas('weapons/MineLauncher/debris/charcoal_red_' + n);
		this.fire.addChild(this._fRedSprite_sprt);
		this._fRedSprite_sprt.zIndex = 3;

		let sequence = [
			{	tweens: [],
				duration: 6*2*16.7 },
			{
				tweens: [
					{prop: 'alpha', to: 0}
				],
				duration: 15*2*16.7,
				onfinish: (e) => {
					this._fRedSprite_sprt.destroy();
					this._fRedSprite_sprt = null;
				}
			}
		]
		Sequence.start(this._fRedSprite_sprt, sequence);
	}

	//override
	_initBaseSprite()
	{
		let n = this._getRandomAssetIndex();
		let lDebrisSprite_sprt = APP.library.getSpriteFromAtlas('weapons/MineLauncher/debris/charcoal_' + n);
		//lDebrisSprite_sprt.tint = 0xCD4535; //0x4F4F4F;
		return lDebrisSprite_sprt;
	}

	//override
	_getBaseScale()
	{
		return 0.74;
	}

	_getRandomScale()
	{
		return Utils.random(0.6, 1, true);
	}

	_getRandomAssetIndex()
	{
		if (this._fRandomAssetIndex_int === undefined)
		{
			this._fRandomAssetIndex_int = Utils.random(0, 2);
		}
		return this._fRandomAssetIndex_int;
	}

	destroy()
	{
		Sequence.destroy(Sequence.findByTarget(this._fRedSprite_sprt));
		this._fRedSprite_sprt = null;
		super.destroy();
	}
}

export default CharcoalFlyingDebris;