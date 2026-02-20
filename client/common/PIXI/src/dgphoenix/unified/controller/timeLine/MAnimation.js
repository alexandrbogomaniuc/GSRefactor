import MTimeLine from "./MTimeLine";

const ANIMATION_TARGET 			= "ANIMATION_TARGET";
const KEY_FRAME 				= "KEY_FRAME";
const EASING 					= "EASING";
const SET_X 					= "SET_X";
const SET_Y 					= "SET_Y";
const SET_SCALE 				= "SET_SCALE";
const SET_SCALE_X 				= "SET_SCALE_X";
const SET_SCALE_Y 				= "SET_SCALE_Y";
const SET_WIDTH 				= "SET_WIDTH";
const SET_HEIGHT 				= "SET_HEIGHT";
const SET_ALPHA 				= "SET_ALPHA";
const SET_ROTATION_IN_DEGREES 	= "SET_ROTATION_IN_DEGREES";
const EXECUTE_METHOD			= "EXECUTE_METHOD";

/**
 * @class
 * @classdesc Animation class
 */
class MAnimation
{
	static get ANIMATION_TARGET()			{ return ANIMATION_TARGET }
	static get KEY_FRAME()					{ return KEY_FRAME }
	static get SET_X()						{ return SET_X }
	static get SET_Y()						{ return SET_Y }
	static get SET_SCALE() 					{ return SET_SCALE }
	static get SET_SCALE_X() 				{ return SET_SCALE_X }
	static get SET_SCALE_Y() 				{ return SET_SCALE_Y }
	static get SET_WIDTH()					{ return SET_WIDTH }
	static get SET_HEIGHT()					{ return SET_HEIGHT }
	static get SET_ALPHA()					{ return SET_ALPHA }
	static get SET_ROTATION_IN_DEGREES() 	{ return SET_ROTATION_IN_DEGREES }
	static get EXECUTE_METHOD()				{ return EXECUTE_METHOD }

	static get ANIMATION_PLAY_MODE_ID_FORWARD() 				{ return 0 }
	static get ANIMATION_PLAY_MODE_ID_BACK() 					{ return 1 }
	static get ANIMATION_PLAY_MODE_ID_LOOP() 					{ return 2 }
	static get ANIMATION_PLAY_MODE_ID_REPEAT_SEVERAL_TIMES() 	{ return 3 }


	static get LINEAR() 		{ return 0 }
	static get EASE() 			{ return 1 }
	static get EASE_IN() 		{ return 2 }
	static get EASE_OUT() 		{ return 3 }
	static get EASE_IN_OUT()	{ return 4 }


	static getCubicBezierY(aProgress_num, aX0_num, aX1_num, aX2_num, aX3_num)
	{
		let lReversedPorogress_num = 1 - aProgress_num;

		return (
			Math.pow(lReversedPorogress_num, 3) * aX0_num +
			3 * Math.pow(lReversedPorogress_num, 2) * aProgress_num * aX1_num +
			3 * lReversedPorogress_num * Math.pow(aProgress_num, 2) * aX2_num +
			Math.pow(aProgress_num, 3) * aX3_num);
	}

	static getEasingMultiplier(aEeasingId_int, aProgress_num)
	{
		switch(aEeasingId_int)
		{
			case MAnimation.EASE:
				return MAnimation.getCubicBezierY(aProgress_num, 0, 0.75, 0.9, 1);
			case MAnimation.EASE_IN:
				return MAnimation.getCubicBezierY(aProgress_num, 0, 0, 0.58, 1);
			case MAnimation.EASE_OUT:
				return MAnimation.getCubicBezierY(aProgress_num, 0, 0.42, 1, 1);
			case MAnimation.EASE_IN_OUT:
				return MAnimation.getCubicBezierY(aProgress_num, 0, 0.1, 1, 1);
		}

		return aProgress_num;
	}

	/**
	 * @constructor
	 * @param {MTimeLine} aWrapper_mt - Timeline to which the animation is attached.
	 */
	constructor(aWrapper_mt)
	{
		this._fFunctions_func_arr = [];
		this._fFunctionsCallFrameIndexes_int_arr = [];
		this._fAnimationFragments_obj_arr = [];
		this._fCurrentScaleX_num = 1;
		this._fCurrentScaleY_num = 1;
		this._fCurrentTranslateX_num = 0;
		this._fCurrentTranslateY_num = 0;
		this._fCurrentAlpha_num = 1;
		this._fCurrentAngle_num = 0;
		this._fMethodArgumentValue_num = 0;
		this._fCurrentFrame_num = -1;
		this._fTotalFramesCount_int = 0;
		this._fExecutableMethodContext_ctx = this;
		this._fWrapper_mt = aWrapper_mt;

		this._fNextFrameCallbackFunction_func = this.onNextFrames.bind(this);
		this._fPlayModeId_int = MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD;
		this._fIsPlaying_bl = false;
	}

	/**
	 * Returns total animation frames
	 * @returns {number}
	 */
	getTotalFramesCount()
	{
		return this._fTotalFramesCount_int;
	}

	/**
	 * Gets previous property value.
	 * @param {string} aKey_str - Property name
	 * @param {number} aAnimationFragmentIndex_int - Start frame number 
	 * @returns {number}
	 */
	getPreviousValue(aKey_str, aAnimationFragmentIndex_int)
	{
		for( let i = aAnimationFragmentIndex_int - 1; i > 0; i-- )
		{
			let lValue_num = this._fAnimationFragments_obj_arr[i][aKey_str];

			if(lValue_num !== undefined)
			{
				return lValue_num;
			}
		}

		return undefined;
	}

	/**
	 * Sets a Function to be called when animation reaches specific frame.
	 * @param {Function} aFunction_func - Function to call
	 * @param {number} aFrameIndex_num  - Target frame number
	 * @param {*} aContext_ctx - Function context
	 * @param {*} aOptArgument - Function argument
	 * @param {number} [aOptFrameRateMultiplier_num=1] - Framerate multiplier
	 */
	callFunctionAtFrame(aFunction_func, aFrameIndex_num, aContext_ctx, aOptArgument, aOptFrameRateMultiplier_num = 1)
	{
		let lFrameIndex_int = Math.round(aFrameIndex_num * aOptFrameRateMultiplier_num);

		this._fFunctions_func_arr.push(aFunction_func.bind(aContext_ctx, aOptArgument));
		this._fFunctionsCallFrameIndexes_int_arr.push(lFrameIndex_int);

		if(lFrameIndex_int > this._fTotalFramesCount_int)
		{
			this._fTotalFramesCount_int = lFrameIndex_int;
		}
	}

	/**
	 * Adds animation to an object
	 * @param {*} aTargetObject_mdc - Target object with an animated property.
	 * @param {string} aKey_str - Animated property name.
	 * @param {number} aInitialValue_num - Initial property value.
	 * @param {*} aDescriptors_num_arr_arr - Animation descriptor.
	 * @param {*} [aOptContext_ctx] - Optional Function context (used for property name EXECUTE_METHOD).
	 * @param {number} [aOptFrameRateMultiplier_num=1] - Framerate multiplier.
	 */
	setAnimation(aTargetObject_mdc, aKey_str, aInitialValue_num, aDescriptors_num_arr_arr, aOptContext_ctx, aOptFrameRateMultiplier_num = 1)
	{
		let lTargetView_mdc = aTargetObject_mdc;
		let aAnimationDescriptor_obj_arr = [];

		/*
		local format looks like this:
		[
			{
				KEY_FRAME: 0,
				SET_X: 200,
				SET_SCALE_X: 0.5,
			},
			{
				KEY_FRAME: 200,
				SET_X: 100,
				SET_SCALE_X: 1,
			},
			{
				KEY_FRAME: 400,
				SET_X: 0,
				SET_SCALE_X: 0.5,
			},
		],

		*/

		//CONVERTING GIVEN DESCRIPTORS FROM UE TO LOCAL FORMAT...
		let lFameIndex_int = 0;

		aAnimationDescriptor_obj_arr[0] = {	KEY_FRAME : 0 };
		aAnimationDescriptor_obj_arr[0][aKey_str] = aInitialValue_num;

		for( let i = 0; i < aDescriptors_num_arr_arr.length; i++ )
		{
			let lDescriptor_num_arr = aDescriptors_num_arr_arr[i];
			let lDescriptor_obj = {};
			let lDurationInFrames_int = undefined;
			let lValue_num = undefined;
			let lEasingId_int = undefined;

			if(Array.isArray(lDescriptor_num_arr))
			{
				lDurationInFrames_int = Math.round(lDescriptor_num_arr[1] * aOptFrameRateMultiplier_num);
				
				if(lDescriptor_num_arr[1] === 1)
				{
					lDurationInFrames_int = 0.01;
				}

				lValue_num = lDescriptor_num_arr[0];
				lEasingId_int = lDescriptor_num_arr[2];
			}
			else
			{
				//TAKE SINGLE INT AS PAUSE...
				/*
					...
					[0.75, 13],
					25, <------- like this
					[0.5, 10],
					...
				*/
				lDurationInFrames_int = Math.round(lDescriptor_num_arr * aOptFrameRateMultiplier_num);

				//USE PREVIOUS VALUE...
				if(i === 0)
				{
					lValue_num = aInitialValue_num;
				}
				else
				{
					lValue_num = aDescriptors_num_arr_arr[i - 1][0];
				}
				//...USE PREVIOUS VALUE
				//...TAKE SINGLE INT AS PAUSE
			}


			lFameIndex_int += lDurationInFrames_int;
			lDescriptor_obj[KEY_FRAME] = lFameIndex_int;
			lDescriptor_obj[aKey_str] = lValue_num;
			lDescriptor_obj[EASING] = lEasingId_int === undefined ? MAnimation.LINEAR : lEasingId_int;

			aAnimationDescriptor_obj_arr.push(lDescriptor_obj);
		}
		//...CONVERTING GIVEN DESCRIPTORS FROM UE TO LOCAL FORMAT



		this._fAnimationFragments_obj_arr = aAnimationDescriptor_obj_arr;

		for( let i = 0; i < aAnimationDescriptor_obj_arr.length; i++ )
		{
			let lAnimation_obj = aAnimationDescriptor_obj_arr[i];

			if(lAnimation_obj.KEY_FRAME > this._fTotalFramesCount_int)
			{
				this._fTotalFramesCount_int = lAnimation_obj.KEY_FRAME;
			}

			lAnimation_obj.ANIMATION_TARGET = lTargetView_mdc;

			if(lAnimation_obj.SET_SCALE !== undefined)
			{
				lAnimation_obj.SET_SCALE_X = lAnimation_obj.SET_SCALE;
				lAnimation_obj.SET_SCALE_Y = lAnimation_obj.SET_SCALE;
			}
		}

		if(!!aOptContext_ctx)
		{
			this._fExecutableMethodContext_ctx = aOptContext_ctx;
		}
	}

	/**
	 * Gets animation state of previous frame.
	 * @param {number} aAnimationFragmentIndex_int - Frame number.
	 * @param {string} aKey_str - Animated property.
	 * @returns {object}
	 */
	getPreviousFragmentForKey(aAnimationFragmentIndex_int, aKey_str)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		
		for( let i = aAnimationFragmentIndex_int - 1; i >= 0; i-- )
		{
			if(lAnimationFragments_obj_arr[i][aKey_str] !== undefined)
			{
				return lAnimationFragments_obj_arr[i];
			}
		}

		return lAnimationFragments_obj_arr[aAnimationFragmentIndex_int];
	}

	/**
	 * Gets animation state of next frame.
	 * @param {number} aAnimationFragmentIndex_int - Frame number.
	 * @param {string} aKey_str - Animated property.
	 * @returns {object}
	 */
	getNextFragmentForKey(aAnimationFragmentIndex_int, aKey_str)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		
		for( let i = aAnimationFragmentIndex_int; i < lAnimationFragments_obj_arr.length; i++ )
		{
			if(lAnimationFragments_obj_arr[i][aKey_str] !== undefined)
			{
				return lAnimationFragments_obj_arr[i];
			}
		}

		return lAnimationFragments_obj_arr[lAnimationFragments_obj_arr.length - 1];
	}

	/**
	 * Gets animation state of last frame.
	 * @param {string} aKey_str - Animated property.
	 * @returns {object}
	 */
	getLastFragmentForKey(aKey_str)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		let lLastIndex_int = lAnimationFragments_obj_arr.length-1;
		
		if(lAnimationFragments_obj_arr[lLastIndex_int][aKey_str] !== undefined)
		{
			return lAnimationFragments_obj_arr[lLastIndex_int];
		}

		return null;
	}

	/**
	 * Set animation to a specific frame.
	 * @param {number} aFrameIndex_num - Target frame number.
	 * @param {boolean} [aSkipFunctions_bl=false] - If true - callbacks for jumped frames will be omitted
	 * @private
	 */
	_wind(aFrameIndex_num, aSkipFunctions_bl = false)
	{
		let lAnimationFragments_obj_arr = this._fAnimationFragments_obj_arr;
		let lAnimationFragmentsCount_int = lAnimationFragments_obj_arr.length;
		let lCurrentAnimationFragmentIndex_int = 0;
		let lKey_int = undefined;
		let lPreviousAnimationFragment_obj = null;
		let lNextAnimationFragment_obj = null;
		let lLastAnimationFragment_obj = null;
		let lFramesDelta_num = 0;
		let lProgress_num = 0;
		let lDelta_num = 0;
		let lEasingId_int = 0;
		let lFrameIndex_num = aFrameIndex_num;

		if (!aSkipFunctions_bl)
		{
			let lFunctions_func_arr = this.getFunctionsToCall(aFrameIndex_num, this._fCurrentFrame_num);
			for (let i = 0; i < lFunctions_func_arr.length; i++)
			{
				lFunctions_func_arr[i].function.call();
			}
		}

		//INITIAL VALUES...
		if(lFrameIndex_num <= 0)
		{
			let lAnimationFragment_obj = this._fAnimationFragments_obj_arr[0];


			//TRANSLATE X...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_X] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].position.x = lAnimationFragment_obj[SET_X];
			}
			//...TRANSLATE X

			//TRANSLATE Y...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_Y] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].position.y = lAnimationFragment_obj[SET_Y];
			}
			//...TRANSLATE Y

			//SET WIDTH...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_WIDTH] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].width = lAnimationFragment_obj[SET_WIDTH];
			}
			//...SET WIDTH

			//SET HEIGHT...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_HEIGHT] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].height = lAnimationFragment_obj[SET_HEIGHT];
			}
			//...SET HEIGHT

			//SCALE X...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_SCALE_X] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].scale.x = lAnimationFragment_obj[SET_SCALE_X];
			}
			//...SCALE X

			//SCALE Y...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_SCALE_Y] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].scale.y = lAnimationFragment_obj[SET_SCALE_Y];
			}
			//...SCALE Y

			//ROTATION IN DEGREES...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_ROTATION_IN_DEGREES] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].rotation = lAnimationFragment_obj[SET_ROTATION_IN_DEGREES] * (Math.PI / 180);
			}
			//...ROTATION IN DEGREES

			//ALPHA...
			if(
				lAnimationFragment_obj &&
				lAnimationFragment_obj[SET_ALPHA] !== undefined
				)
			{
				lAnimationFragment_obj[ANIMATION_TARGET].alpha = lAnimationFragment_obj[SET_ALPHA];
			}
			//...ALPHA

		}

		//...INITIAL VALUES

		for( let i = 0; i < this._fAnimationFragments_obj_arr.length; i++ )
		{
			let lAnimationFragment_obj = this._fAnimationFragments_obj_arr[i];

			if(lFrameIndex_num <= lAnimationFragment_obj.KEY_FRAME)
			{
				lCurrentAnimationFragmentIndex_int = i;
				break;
			}
		}

		//TRANSLATE X...
		lKey_int = SET_X;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);

		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentTranslateX_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentTranslateX_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].position.x = this._fCurrentTranslateX_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentTranslateX_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].position.x = this._fCurrentTranslateX_num;
			}
		}
		//...TRANSLATE X

		//TRANSLATE Y...
		lKey_int = SET_Y;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentTranslateY_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentTranslateY_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].position.y = this._fCurrentTranslateY_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentTranslateY_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].position.y = this._fCurrentTranslateY_num;
			}
			
		}
		//...TRANSLATE Y

		//SET WIDTH...
		lKey_int = SET_WIDTH;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);

		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj[KEY_FRAME] - lPreviousAnimationFragment_obj[KEY_FRAME];
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj[KEY_FRAME]) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentWidth_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentWidth_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].width = this._fCurrentWidth_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentWidth_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].width = this._fCurrentWidth_num;
			}
		}
		//...SET WIDTH

		//SET HEIGHT...
		lKey_int = SET_HEIGHT;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);

		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj[KEY_FRAME] - lPreviousAnimationFragment_obj[KEY_FRAME];
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj[KEY_FRAME]) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentHeight_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentHeight_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].height = this._fCurrentHeight_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentHeight_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].height = this._fCurrentHeight_num;
			}
		}
		//...SET HEIHGT

		//SCALE X...
		lKey_int = SET_SCALE_X;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentScaleX_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentScaleX_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].scale.x = this._fCurrentScaleX_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentScaleX_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].scale.x = this._fCurrentScaleX_num;
			}
		}
		//...SCALE X

		//SCALE Y...
		lKey_int = SET_SCALE_Y;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentScaleY_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentScaleY_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].scale.y = this._fCurrentScaleY_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentScaleY_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].scale.y = this._fCurrentScaleY_num;
			}
		}
		//...SCALE Y

		//ROTATION_IN_DEGREES...
		lKey_int = SET_ROTATION_IN_DEGREES;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentAngle_num = lPreviousAnimationFragment_obj[lKey_int];
			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentAngle_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].rotation = this._fCurrentAngle_num * (Math.PI / 180);
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentAngle_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].rotation = this._fCurrentAngle_num * (Math.PI / 180);
			}
		}
		//...ROTATION_IN_DEGREES

		//ALPHA...
		lKey_int = SET_ALPHA;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.		KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fCurrentAlpha_num = lPreviousAnimationFragment_obj[lKey_int];

			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fCurrentAlpha_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;
				lPreviousAnimationFragment_obj[ANIMATION_TARGET].alpha = this._fCurrentAlpha_num;
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fCurrentAlpha_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].alpha = this._fCurrentAlpha_num;
			}
		}
		//...ALPHA


		//EXECUTE_METHOD...
		lKey_int = EXECUTE_METHOD;
		lPreviousAnimationFragment_obj = this.getPreviousFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int);
		
		if(lPreviousAnimationFragment_obj)
		{
			lNextAnimationFragment_obj =  this.getNextFragmentForKey(lCurrentAnimationFragmentIndex_int, lKey_int) || lPreviousAnimationFragment_obj;
			lLastAnimationFragment_obj = this.getLastFragmentForKey(lKey_int);
			lFramesDelta_num = lNextAnimationFragment_obj.KEY_FRAME - lPreviousAnimationFragment_obj.KEY_FRAME;
			lProgress_num = (lFrameIndex_num - lPreviousAnimationFragment_obj.KEY_FRAME) / lFramesDelta_num;
			lDelta_num = lNextAnimationFragment_obj[lKey_int] - lPreviousAnimationFragment_obj[lKey_int];
			lEasingId_int = lNextAnimationFragment_obj.EASING;

			this._fMethodArgumentValue_num = lPreviousAnimationFragment_obj[lKey_int];
			if(
				!Number.isNaN(lDelta_num) &&
				lPreviousAnimationFragment_obj !== lNextAnimationFragment_obj
				)
			{
				this._fMethodArgumentValue_num += lDelta_num * MAnimation.getEasingMultiplier(lEasingId_int, lProgress_num);;

				lPreviousAnimationFragment_obj[ANIMATION_TARGET].call(this._fExecutableMethodContext_ctx, this._fMethodArgumentValue_num);
			}
			else if (lLastAnimationFragment_obj && lFrameIndex_num > lLastAnimationFragment_obj.KEY_FRAME)
			{
				this._fMethodArgumentValue_num = lLastAnimationFragment_obj[lKey_int];
				lLastAnimationFragment_obj[ANIMATION_TARGET].call(this._fExecutableMethodContext_ctx, this._fMethodArgumentValue_num);
			}
		}
		//...EXECUTE_METHOD
		
		this._fCurrentFrame_num = aFrameIndex_num;
	}

	/**
	 * Start animation playing from a specific frame.
	 * @param {number} aFrameIndex_num - Start frame number.
	 * @param {number} [aOptModeId_int=MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD] - Animation play mode.
	 */
	playFromFrame(aFrameIndex_num, aOptModeId_int = MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD)
	{
		this.play(aOptModeId_int);
		this._fCurrentFrame_num = aFrameIndex_num;
	}

	/**
	 * Start animation playing from the first frame.
	 * @param {number} [aOptModeId_int=MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD]  - Animation play mode.
	 */
	play(aOptModeId_int = MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD)
	{
		this._fCurrentFrame_num = -1;
		this._fPlayModeId_int = aOptModeId_int;
		this._fIsPlaying_bl = true;

		this._wind(this._fCurrentFrame_num);
	}

	/**
	 * Stop animation playing.
	 */
	stop()
	{
		this._fIsPlaying_bl = false;
	}

	/**
	 * Current animation frame.
	 * @returns {number}
	 */
	getCurrentFrameIndex()
	{
		return this._fCurrentFrame_num;
	}

	/**
	 * Returns functions to be called between specific frames.
	 * @param {number} aFrameIndex_num - Start frame of search interval.
	 * @param {number} aCurrentFrame_num - End frame of search interval.
	 * @returns {Object[]} - Array of descriptors.
	 */
	getFunctionsToCall(aFrameIndex_num, aCurrentFrame_num)
	{
		let lFunctions_func_arr = [];
		
		let lFramesDelta_num = aFrameIndex_num - aCurrentFrame_num;
		let lPrevFrame_num = aCurrentFrame_num;
		let lNextFrame_num = Math.max(aFrameIndex_num, aCurrentFrame_num);
		
		if (this._fPlayModeId_int === MAnimation.ANIMATION_PLAY_MODE_ID_BACK && lFramesDelta_num >= 0)
		{
			lNextFrame_num = this._fWrapper_mt.getTotalFramesCount() - this._fWrapper_mt.getCurrentFrameIndex();
			lPrevFrame_num = lNextFrame_num - lFramesDelta_num;
		}
		
		for( let i = 0; i < this._fFunctions_func_arr.length; i++ )
		{
		let lFunctionFrameIndex_int = this._fFunctionsCallFrameIndexes_int_arr[i];

		if(
			lFunctionFrameIndex_int > lPrevFrame_num &&
			lFunctionFrameIndex_int <= lNextFrame_num
		)
		{
			lFunctions_func_arr.push({ function: this._fFunctions_func_arr[i], frame: lFunctionFrameIndex_int });
		}
		}
		
		lFunctions_func_arr.sort((a, b) => {
			let val = (this._fPlayModeId_int === MAnimation.ANIMATION_PLAY_MODE_ID_BACK != lFramesDelta_num < 0)? -1 : 1;
			if (a.frame > b.frame)
			{
				return val;
			}
			else
			{
				return -val;
			}
		});
			
		return lFunctions_func_arr;
	}

	/**
	 * Step of animation through specific amount of frames.
	 * @param {number} aFramesCount_num - Frames amount for animation step.
	 */
	onNextFrames(aFramesCount_num)
	{
		let lFramesDelta_num = aFramesCount_num;

		let lCurrentFrame_num = this._fCurrentFrame_num + lFramesDelta_num;

		if(lCurrentFrame_num > this._fTotalFramesCount_int)
		{
			switch(this._fPlayModeId_int)
			{
				case MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD:
				case MAnimation.ANIMATION_PLAY_MODE_ID_BACK:
				case MAnimation.ANIMATION_PLAY_MODE_ID_LOOP:
				case MAnimation.ANIMATION_PLAY_MODE_ID_REPEAT_SEVERAL_TIMES:
				{
					lCurrentFrame_num = this._fTotalFramesCount_int;
					this._fIsPlaying_bl = false;
				}
				break;
			}
		}
		
		this._wind(lCurrentFrame_num);
		
	}

	/**
	 * Set animation to a specific frame.
	 * @param {number} aFrameIndex_num - Target frame number.
	 * @param {boolean} [aSkipFunctions_bl=false] - If true - callbacks for jumped frames will be omitted.
	 */
	wind(aFrameIndex_num, aSkipFunctions_bl = false)
	{
		this._wind(aFrameIndex_num, aSkipFunctions_bl);
	}

	/**
	 * Set animation to the first frame.
	 * @param {boolean} [aSkipFunctions_bl=false] - If true - callbacks for jumped frames will be omitted.
	 */
	windToStart(aSkipFunctions_bl = false)
	{
		this._wind(-1, aSkipFunctions_bl);
	}

	/**
	 * Set animation to the final frame.
	 * @param {boolean} [aSkipFunctions_bl=false] - If true - callbacks for jumped frames will be omitted. 
	 */
	windToEnd(aSkipFunctions_bl = false)
	{
		this._wind(this._fTotalFramesCount_int, aSkipFunctions_bl);
	}

	windToEndAndExecuteAllRemainingFunctions()
	{
		let l_func_arr = this._fFunctions_func_arr;
		let l_int_arr = this._fFunctionsCallFrameIndexes_int_arr;

		for( let i = 0; i < l_func_arr.length; i++ )
		{
			if( l_int_arr[i] > this._fCurrentFrame_num )
			{
				l_func_arr[i].call();
			}
		}

		this.windToEnd();
	}

	/**
	 * Indicates whether animation is currently playing or not.
	 * @returns {boolean}
	 */
	isPlaying()
	{
		return this._fIsPlaying_bl;
	}

	/**
	 * Destroy animation instance.
	 */
	destroy()
	{
		this._fFunctions_func_arr = null;
		this._fFunctionsCallFrameIndexes_int_arr = null;
		this._fAnimationFragments_obj_arr = null;
		this._fCurrentScaleX_num = undefined;
		this._fCurrentScaleY_num = undefined;
		this._fCurrentTranslateX_num = undefined;
		this._fCurrentTranslateY_num = undefined;
		this._fCurrentAlpha_num = undefined;
		this._fCurrentAngle_num = undefined;
		this._fMethodArgumentValue_num = undefined;
		this._fCurrentFrame_num = undefined;
		this._fTotalFramesCount_int = undefined;
		this._fExecutableMethodContext_ctx = null;
		this._fWrapper_mt = null;

		this._fNextFrameCallbackFunction_func = null;
		this._fPlayModeId_int = undefined;
		this._fIsPlaying_bl = undefined;
	}
}


export default MAnimation;