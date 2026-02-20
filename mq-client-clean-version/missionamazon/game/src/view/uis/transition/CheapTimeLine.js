export const KEY_FRAME 					= "KEY_FRAME";
export const SET_SCALE_X 				= "SET_SCALE_X";
export const SET_SCALE_Y 				= "SET_SCALE_Y";
export const SET_SCALE 					= "SET_SCALE";
export const SET_X 						= "SET_X";
export const SET_Y 						= "SET_Y";
export const SET_ALPHA 					= "SET_ALPHA";
export const SET_ROTATION_IN_DEGREES 	= "SET_ROTATION_IN_DEGREES";

/*
	Use this class if a lot of animations should be triggered at the same time.
	Easing is only linear.

	This is an alternative for usage of huge amount of Tweens.
*/

class CheapTimeLine
{
	constructor(aOptAnimationsDescriptor_obj_arr)
	{
		this._fAnimationFragments_obj_arr = [];

		this._fCurrentScaleX_num = 1;
		this._fCurrentScaleY_num = 1;
		this._fCurrentTranslateX_num = 0;
		this._fCurrentTranslateY_num = 0;
		this._fCurrentAlpha_num = 1;
		this._fCurrentAngle_num = 0;

		if(aOptAnimationsDescriptor_obj_arr)
		{
			this.setAnimations(aOptAnimationsDescriptor_obj_arr);
		}
	}

	getPreviousFragmentForKey(aAnimationFragmentIndex_int, aKey_int)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		
		for( let i = aAnimationFragmentIndex_int - 1; i >= 0; i-- )
		{
			if(lAnimationFragments_obj_arr[i][aKey_int] !== undefined)
			{
				return lAnimationFragments_obj_arr[i];
			}
		}

		return lAnimationFragments_obj_arr[aAnimationFragmentIndex_int];
	}

	getNextFragmentForKey(aAnimationFragmentIndex_int, aKey_int)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		
		for( let i = aAnimationFragmentIndex_int; i < lAnimationFragments_obj_arr.length; i++ )
		{
			if(lAnimationFragments_obj_arr[i][aKey_int] !== undefined)
			{
				return lAnimationFragments_obj_arr[i];
			}
		}

		return lAnimationFragments_obj_arr[lAnimationFragments_obj_arr.length - 1];
	}

	windToFrame(aFrameIndex_int)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		let lAnimationFragmentsCount_int = lAnimationFragments_obj_arr.length;
		let lTotalDurationInFrames_int = lAnimationFragments_obj_arr[lAnimationFragmentsCount_int - 1].KEY_FRAME;
		let lProgress_num = aFrameIndex_int / lTotalDurationInFrames_int;

		if(lProgress_num > 1)
		{
			lProgress_num = 1;
		}

		this.wind(lProgress_num);	
	}

	wind(aProgress_num)
	{
		let lInitialProgress_num = aProgress_num;
		if(lInitialProgress_num > 1)
		{
			lInitialProgress_num = lInitialProgress_num % 1;
		}

		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		let lAnimationFragmentsCount_int = lAnimationFragments_obj_arr.length;
		let lTotalDurationInFrames_int = lAnimationFragments_obj_arr[lAnimationFragmentsCount_int - 1].KEY_FRAME;
		let lCurrentFrameIndex_int = Math.trunc(lInitialProgress_num * lTotalDurationInFrames_int);
		let lCurrentAnimationFragmentIndex_int = 0;

		for( let i = 0; i < this._fAnimationFragments_obj_arr.length; i++ )
		{
			let lAnimationFragment_obj = this._fAnimationFragments_obj_arr[i];

			if(lCurrentFrameIndex_int <= lAnimationFragment_obj.KEY_FRAME)
			{
				lCurrentAnimationFragmentIndex_int = i;
				break;
			}
		}

		//TRANSLATE X...
		let lKey_int = SET_X;
		let lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		let lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		let lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		let lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		let lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];

		this._fCurrentTranslateX_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentTranslateX_num += lDelta_num * lProgress_num;
		}
		//...TRANSLATE X

		//TRANSLATE Y...
		lKey_int = SET_Y;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
		
		this._fCurrentTranslateY_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentTranslateY_num += lDelta_num * lProgress_num;
		}
		//...TRANSLATE Y

		//SCALE X...
		lKey_int = SET_SCALE_X;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
		
		this._fCurrentScaleX_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentScaleX_num += lDelta_num * lProgress_num;
		}
		//...SCALE X

		//SCALE Y...
		lKey_int = SET_SCALE_Y;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];

		this._fCurrentScaleY_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentScaleY_num += lDelta_num * lProgress_num;
		}
		//...SCALE Y

		//ANGLE...
		lKey_int = SET_ROTATION_IN_DEGREES;

		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
		
		this._fCurrentAngle_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentAngle_num += lDelta_num * lProgress_num;
		}
		//...ANGLE

		//ALPHA...
		lKey_int = SET_ALPHA;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		lFramesDelta_int = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
		lProgress_num = (lCurrentFrameIndex_int - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_int;
		lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
		this._fCurrentAlpha_num = lPreviousAnimationFragment_obj[lKey_int];

		if(lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj)
		{
			this._fCurrentAlpha_num += lDelta_num * lProgress_num;
		}
		//...ALPHA
	}

	getTranslateX()
	{
		return this._fCurrentTranslateX_num;
	}

	getTranslateY()
	{
		return this._fCurrentTranslateY_num;
	}

	getScaleX()
	{
		return this._fCurrentScaleX_num;
	}

	getScaleY()
	{
		return this._fCurrentScaleY_num;
	}

	getAngle()
	{
		return this._fCurrentAngle_num;
	}

	getAlpha()
	{
		return this._fCurrentAlpha_num;
	}


	setAnimations(aAnimationsDescriptor_obj_arr)
	{
		for( let i = 0; i < aAnimationsDescriptor_obj_arr.length; i++ )
		{
			let lAnimation_obj = aAnimationsDescriptor_obj_arr[i];

			if(
				i === 0 ||
				i === aAnimationsDescriptor_obj_arr.length - 1
				)
			{
				if(lAnimation_obj.SET_SCALE_X === undefined)
				{
					lAnimation_obj.SET_SCALE_X = 1;
				}
				
				if(lAnimation_obj.SET_SCALE_Y === undefined)
				{
					lAnimation_obj.SET_SCALE_Y = 1;
				}

				if(lAnimation_obj.SET_X === undefined)
				{
					lAnimation_obj.SET_X = 0;
				}

				if(lAnimation_obj.SET_Y === undefined)
				{
					lAnimation_obj.SET_Y = 0;
				}

				if(lAnimation_obj.SET_ALPHA === undefined)
				{
					lAnimation_obj.SET_ALPHA = 1;
				}

				if(lAnimation_obj.SET_ROTATION_IN_DEGREES === undefined)
				{
					lAnimation_obj.SET_ROTATION_IN_DEGREES =  0;
				}
			}

			if(lAnimation_obj.SET_SCALE !== undefined)
			{
				lAnimation_obj.SET_SCALE_X = lAnimation_obj.SET_SCALE;
				lAnimation_obj.SET_SCALE_Y = lAnimation_obj.SET_SCALE;
			}
		}

		this._fAnimationFragments_obj_arr = aAnimationsDescriptor_obj_arr;
	}

	adjustSprite(aSprite_s)
	{
		aSprite_s.position.set(
			this.getTranslateX(),
			this.getTranslateY());

		aSprite_s.alpha = this.getAlpha();
		aSprite_s.scale.x = this.getScaleX();
		aSprite_s.scale.y = this.getScaleY();
		aSprite_s.rotation = this.getAngle() * Math.PI / 180;
	}
}

export default CheapTimeLine;