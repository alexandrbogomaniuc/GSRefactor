import { APP } from '../../../../../../common/PIXI/src/dgphoenix/unified/controller/main/globals';
import Sprite from '../../../../../../common/PIXI/src/dgphoenix/unified/view/base/display/Sprite';
import CheapTimeLine from './CheapTimeLine';

const ANIMATIONS =
[
	//CLOUD 1...
	new CheapTimeLine(
	[
		{
			KEY_FRAME: 0,
			SET_ALPHA: 0,
			SET_ROTATION_DEGREES: 0,
			SET_Y: 0,
			SET_SCALE: 4
		},
		{
			KEY_FRAME: 31,
			SET_ALPHA: 0.3,
		},
		{
			KEY_FRAME: 67,
			SET_ALPHA: 0,
			SET_ROTATION_DEGREES: -9,
			SET_Y: -200,
			SET_SCALE: 4
		}
	]),
	//...CLOUD 1

	//CLOUD 2...
	new CheapTimeLine(
	[
		{
			KEY_FRAME: 31,
			SET_ALPHA: 0,
			SET_ROTATION_DEGREES: 4,
			SET_Y: 0,
			SET_SCALE: 4
		},
		{
			KEY_FRAME: 67,
			SET_ALPHA: 0.3,
		},
		{
			KEY_FRAME: 102,
			SET_ALPHA: 0,
			SET_ROTATION_DEGREES: 13,
			SET_Y: -200,
			SET_SCALE: 4
		}
	]),
	//...CLOUD 2
];

const CLOUDS_COUNT = ANIMATIONS.length;

class CloudsPool extends Sprite
{
	constructor()
	{
		super();
		this._fSprites_s_arr = [];
		this._fSprites2_s_arr = [];

		for( let i = 0; i < CLOUDS_COUNT; i++ )
		{
			let l_s = this.addChild(APP.library.getSprite("transition/fog"));
			l_s.scale.x = 0;
			this._fSprites_s_arr[i] = l_s;

			let l2_s = this.addChild(APP.library.getSprite("transition/fog"));
			l2_s.scale.x = 0;
			this._fSprites2_s_arr[i] = l2_s;
		}

		this._fProgress_num = 0;
		this._fProgress2_num = -0.5;
		this._fIsLoop_bl = false;

	}

	update(aTimeMultiplier_num = 1)
	{
		this._fProgress_num += 0.01 * aTimeMultiplier_num;
		this._fProgress2_num += 0.01 * aTimeMultiplier_num;


		if(this._fProgress_num > 1)
		{
			this._fProgress_num = this._fIsLoop_bl ? this._fProgress_num % 1 : 1;
		}

		if(this._fProgress2_num > 1)
		{
			this._fProgress2_num = this._fIsLoop_bl ? this._fProgress2_num % 1 : 1;
		}


		for( let i = 0; i < CLOUDS_COUNT; i++ )
		{
			ANIMATIONS[i].wind(this._fProgress_num);
			ANIMATIONS[i].adjustSprite(this._fSprites_s_arr[i]);
		}

		if(this._fProgress2_num > 0)
		{
			for( let i = 0; i < CLOUDS_COUNT; i++ )
			{
				ANIMATIONS[i].wind(this._fProgress2_num);
				ANIMATIONS[i].adjustSprite(this._fSprites2_s_arr[i]);
			}
		}
	}

	setLoopMode(aIsloopMode_bl, aOptIsInstant_bl = false)
	{
		this._fIsLoop_bl = aIsloopMode_bl;

		if(aOptIsInstant_bl)
		{
			if(aIsloopMode_bl)
			{
				this._fProgress_num = 0;
				this._fProgress2_num = 0.5;
			}
			else
			{
				this.drop();
			}
		}
	}


	drop()
	{
		this._fProgress_num = 0;
		this._fProgress2_num = -0.5;
		this._fIsLoop_bl = false;

		for( let i = 0; i < this._fSprites_s_arr.length; i++ )
		{
			this._fSprites_s_arr[i].scale.x = 0;
			this._fSprites2_s_arr[i].scale.x = 0;
		}
	}

	destroy()
	{
		for( let i = 0; i < this._fSprites_s_arr.length; i++ )
		{
			this._fSprites_s_arr[i].destroy();
			this._fSprites2_s_arr[i].destroy();
		}

		this._fSprites_s_arr = null;
		this._fSprites2_s_arr = null;

		super.destroy();
	}

	isVisible()
	{
		for( let i = 0; i < this._fSprites_s_arr.length; i++ )
		{
			if(
				this._fSprites_s_arr[i].alpha > 0 ||
				this._fSprites2_s_arr[i].alpha > 0
				)
			{
				return true;
			}
		}

		return false;
	}
}

export default CloudsPool;