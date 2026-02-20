import MAnimation from './MAnimation';
import { APP } from '../main/globals';

/*
Usage example:

//DEMO SPRITES...
let l_s = this.addChild(APP.library.getSprite("some_sprite"));
let l2_s = this.addChild(APP.library.getSprite("some_sprite"));
l2_s.position.y = 50
//...DEMO SPRITES

//TIME LINE...
let l_mt = new MTimeLine();

l_mt.callFunctionOnStart(
	console.log,
	console,
	"START");

l_mt.callFunctionAtFrame(
	console.log, 		//function
	5, 					//frame number 
	console,			//context
	"5TH FRAME PASSED"	//argument
	);

l_mt.addAnimation(
	l_s,
	MTimeLine.SET_X,
	-300,
	[
		[300, 200],
		5,
		[-300, 200],
	]);

l_mt.addAnimation(
	l2_s,
	MTimeLine.SET_X,
	-300,
	[
		[300, 200, MTimeLine.EASE_IN_OUT],
		5,
		[-300, 200, MTimeLine.EASE_IN_OUT],
	]);

l_mt.addAnimation(
	this._yourFunction, //ex: _yourFunction(aValue){ console.log(aValue);}
	MTimeLine.EXECUTE_METHOD,
	0,
	[
		[1, 125, MTimeLine.EASE_IN_OUT],
	],
	this //context
	);


l_mt.callFunctionOnFinish(
	console.log,
	console,
	"FINISH");

//l_mt.play();
//l_mt.playSeveralTimes(3);
//l_mt.playFromFrame(20);
//l_mt.playLoop();
//l_mt.playBack();//play backward

//l_mt.stop();
//l_mt.pause();
//l_mt.resume();

//l_mt.isPaused();
//l_mt.isPlaying();

//l_mt.wind(21);//move to frame (21)
//l_mt.windToEnd();//move to the end

//l_mt.getProgress(); //get progress in percents (from 0 to 1)
//l_mt.getTotalFramesCount();
//l_mt.getCurrentFrameIndex();

//l_mt.destroy(); //destroy

//...TIME LINE
*/

/**
 * @class
 * @classdesc Timeline class
 */
class MTimeLine
{
																	// > = speed
	static get LINEAR() 		{ return MAnimation.LINEAR } 		// [-]	 [-]	[-]
	static get EASE() 			{ return MAnimation.EASE }			// [>>]	 [>>] 	[>]
	static get EASE_IN() 		{ return MAnimation.EASE_IN }		// [>]	 [>>] 	[>>>]
	static get EASE_OUT() 		{ return MAnimation.EASE_OUT }		// [>>>] [>]	[>]
	static get EASE_IN_OUT()	{ return MAnimation.EASE_IN_OUT }	// [>]	 [>>>] 	[>>]

	static get TARGET()						{ return MAnimation.TARGET }
	static get KEY_FRAME()					{ return MAnimation.KEY_FRAME }
	static get SET_X()						{ return MAnimation.SET_X }
	static get SET_Y()						{ return MAnimation.SET_Y }
	static get SET_WIDTH()					{ return MAnimation.SET_WIDTH }
	static get SET_HEIGHT()					{ return MAnimation.SET_HEIGHT }
	static get SET_SCALE() 					{ return MAnimation.SET_SCALE }
	static get SET_SCALE_X() 				{ return MAnimation.SET_SCALE_X }
	static get SET_SCALE_Y() 				{ return MAnimation.SET_SCALE_Y }
	static get SET_ALPHA()					{ return MAnimation.SET_ALPHA }
	static get SET_ROTATION_IN_DEGREES() 	{ return MAnimation.SET_ROTATION_IN_DEGREES }
	static get EXECUTE_METHOD()				{ return MAnimation.EXECUTE_METHOD }

	static get PLAY_MODE_ID_FORWARD() 		{ return MAnimation.ANIMATION_PLAY_MODE_ID_FORWARD }
	static get PLAY_MODE_ID_BACK() 			{ return MAnimation.ANIMATION_PLAY_MODE_ID_BACK }
	static get PLAY_MODE_ID_LOOP() 			{ return MAnimation.ANIMATION_PLAY_MODE_ID_LOOP }
	static get PLAY_MODE_ID_REPEAT_SEVERAL_TIMES() { return MAnimation.ANIMATION_PLAY_MODE_ID_REPEAT_SEVERAL_TIMES }

	/**
	 * Register new timeline.
	 * @param {MTimeLine} aTimeLine_mtl - Target timeline.
	 * @returns {number} - Index of registered timeline.
	 */
	static registerNewTimeLine(aTimeLine_mtl)
	{
		if(!MTimeLine.timeLines_mtl_arr)
		{
			MTimeLine.timeLines_mtl_arr = [];

			if(APP.gameScreen)
			{
				APP.gameScreen.on('EVENT_ON_TICK_OCCURRED', MTimeLine.tick, this);
			}
			
		}

		MTimeLine.timeLines_mtl_arr.push(aTimeLine_mtl);

		return MTimeLine.timeLines_mtl_arr.length - 1;
	}

	/**
	 * Unregister timeline by index.
	 * @param {number} aIndex_int - Target timeline index.
	 */
	static unregisterTimeLine(aIndex_int)
	{
		for( let i = aIndex_int + 1; i < MTimeLine.timeLines_mtl_arr.length; i++ )
		{
			MTimeLine.timeLines_mtl_arr[i].setIndex(i - 1);
		}

		MTimeLine.timeLines_mtl_arr.splice(aIndex_int, 1);
	}

	/**
	 * Tick timeline immediately.
	 * @param {number} aDelta_num 
	 */
	static forceTick(aDelta_num)
	{
		let lFramesCount_num = aDelta_num / 16;

		if(!MTimeLine.timeLines_mtl_arr)
		{
			return;
		}

		if(lFramesCount_num > 10)
		{
			lFramesCount_num = 10;
		}

		for( let i = 0; i < MTimeLine.timeLines_mtl_arr.length; i++ )
		{
			let l_mtl = MTimeLine.timeLines_mtl_arr[i];

			if(l_mtl.isPlaying())
			{
				l_mtl.tick(lFramesCount_num);
			}
		}
	}

	/**
	 * Tick registered timelines on EVENT_ON_TICK_OCCURRED event.
	 * @param {*} aEvent_obj - EVENT_ON_TICK_OCCURRED event.
	 */
	static tick(aEvent_obj)
	{
		let lFramesCount_num = aEvent_obj.delta / 16;

		if(lFramesCount_num > 10)
		{
			lFramesCount_num = 10;
		}

		for( let i = 0; i < MTimeLine.timeLines_mtl_arr.length; i++ )
		{
			let l_mtl = MTimeLine.timeLines_mtl_arr[i];

			if(l_mtl.isPlaying())
			{
				l_mtl.tick(lFramesCount_num);
			}
		}
	}


	constructor( aOptInputTargetFrameRate_int = 30 )
	{
		this._fAnimations_ma_arr = [];
		this._fFunctionsOnStart_func_arr = [];
		this._fFunctionsOnFinish_func_arr = [];
		this._fIsCompleted_bl = true;
		this._fIsPlaying_bl = false;
		this._fIsPaused_bl = false;
		this._fIndex_int = MTimeLine.registerNewTimeLine(this);
		this._fRemainingReplaysCount_int = 0;
		this._fPlayModeId_int = MTimeLine.PLAY_MODE_ID_FORWARD;
		this._fInputFrameRateMultiplier_num = 60 / aOptInputTargetFrameRate_int;
		this._fTotalFramesCount_num = 0;
		this._fCurrentFrame_num = 0;
	}

	/**
	 * Current timeline position (frame number).
	 * @returns {number}
	 */
	getCurrentFrameIndex()
	{
		return this._fCurrentFrame_num;
	}

	/**
	 * Returns timeline progress in percents (from 0 to 1)
	 * @returns {number}
	 */
	getProgress()
	{
		if(!this._fIsPlaying_bl)
		{
			return 0;
		}

		let lProgress_num = this._fCurrentFrame_num / this._fTotalFramesCount_num;

		if(lProgress_num < 0)
		{
			lProgress_num = 1;
		}

		return lProgress_num;
	}

	/**
	 * Update timeline registration index.
	 * @param {number} aIndex_int
	 */
	setIndex(aIndex_int)
	{
		this._fIndex_int = aIndex_int;
	}

	/**
	 * Add animations to timeline.
	 * @param {Object[]} aDescriptors_obj_arr - Array of animations descriptors.
	 */
	setAnimations(aDescriptors_obj_arr)
	{
		for( let i = 0; i < aDescriptors_obj_arr.length; i++ )
		{
			this.addAnimation(aDescriptors_obj_arr[i]);
		}
	}

	/**
	 * Add animation to timeline.
	 * @param {*} aTargetObject_mdc - Target object with an animated property.
	 * @param {string} aKey_str - Animated property name.
	 * @param {number} aInitialValue_num - Initial property value.
	 * @param {*} aDescriptors_num_arr_arr - Animation descriptor.
	 * @param {*} aOptContext_ctx - Optional Function context (used for property name EXECUTE_METHOD).
	 * @returns {MAnimation} - new added animation.
	 */
	addAnimation(aTargetObject_mdc, aKey_str, aInitialValue_num, aDescriptors_num_arr_arr, aOptContext_ctx)
	{
		let lAnimation_ma = new MAnimation(this);

		lAnimation_ma.setAnimation(aTargetObject_mdc, aKey_str, aInitialValue_num, aDescriptors_num_arr_arr, aOptContext_ctx, this._fInputFrameRateMultiplier_num);
		this._fAnimations_ma_arr.push(lAnimation_ma);

		let lFramesCount_num = lAnimation_ma.getTotalFramesCount();

		if(this._fTotalFramesCount_num < lFramesCount_num)
		{
			this._fTotalFramesCount_num = lFramesCount_num;
		}

		return lAnimation_ma;
	}

	/**
	 * Remove animation from timeline.
	 * @param {MAnimation} aAnimation_ma - Animation to be removed.
	 */
	removeAnimation(aAnimation_ma)
	{
		if (!aAnimation_ma)
		{
			return;
		}

		let lAnimations_ma_arr = this._fAnimations_ma_arr;
		let lAnimationIndex_int = lAnimations_ma_arr.indexOf(aAnimation_ma);
		if (lAnimationIndex_int >= 0)
		{
			lAnimations_ma_arr.splice(lAnimationIndex_int, 1);
			aAnimation_ma.destroy();
		}

		this._fTotalFramesCount_num = 0;
		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			let lFramesCount_num = lAnimations_ma_arr[i].getTotalFramesCount();
			if(this._fTotalFramesCount_num < lFramesCount_num)
			{
				this._fTotalFramesCount_num = lFramesCount_num;
			}
		}
	}

	/**
	 * Total frames amount on timeline.
	 * @returns {number}
	 */
	getTotalFramesCount()
	{
		return this._fTotalFramesCount_num;
	}

	/**
	 * Total duration of timeline in milliseconds.
	 * @returns {number}
	 */
	getTotalDurationInMilliseconds()
	{
		return this._fTotalFramesCount_num * 17;
	}

	/**
	 * Set timeline to provided time position. Loop if time exceeds total duration.
	 * @param {number} aMillisecondIndex_int - Target time position.
	 * @param {number} [aOptOffsetMillisecondIndex_int=0] - Additional target time offset.
	 */
	windLoopToMillisecond(aMillisecondIndex_int, aOptOffsetMillisecondIndex_int = 0)
	{
		this.windToMillisecond((aMillisecondIndex_int + aOptOffsetMillisecondIndex_int) % this.getTotalDurationInMilliseconds());
	}
	
	/**
	 * Set timeline to provided time position. If target time is out of total duration - timeline will be moved to final position.
	 * @param {number} aMillisecondIndex_int - Target time position.
	 * @param {number} aOptOffsetMillisecondIndex_int - Additional target time offset.
	 */
	windToMillisecond(aMillisecondIndex_int, aOptOffsetMillisecondIndex_int = 0)
	{
		this.wind((aMillisecondIndex_int - aOptOffsetMillisecondIndex_int) / 17 / this._fInputFrameRateMultiplier_num);
	}

	/**
	 * Get frame number by time offset.
	 * @param {number} aMillisecondIndex_int - Timeline time offset.
	 * @returns {number} Frame number.
	 */
	convertMillisecondToFrameIndex(aMillisecondIndex_int)
	{
		return aMillisecondIndex_int / 17 / this._fInputFrameRateMultiplier_num;
	}

	/**
	 * Set timeline to a specific frame.
	 * @param {number} aFrame_num - Target frame number.
	 */
	wind(aFrame_num)
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr
		let lFrameIndex_num = this._fInputFrameRateMultiplier_num * aFrame_num;

		if(lFrameIndex_num >= this._fTotalFramesCount_num - 1)
		{
			this.windToEnd();
			return;
		}

		if(lFrameIndex_num < 0)
		{
			this.windToStart();
			return;
		}

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].wind(lFrameIndex_num);
		}
	}

	/**
	 * Start timeline playing from the first frame.
	 * @param {number} [aOptModeId_int=MTimeLine.PLAY_MODE_ID_FORWARD] - Timeline play mode.
	 */
	play(aOptModeId_int = MTimeLine.PLAY_MODE_ID_FORWARD)
	{
		if(this._fIsPlaying_bl)
		{
			return;
		}

		if(this._fIsCompleted_bl)
		{
			let l_func_arr = this._fFunctionsOnStart_func_arr;

			if(aOptModeId_int === MTimeLine.PLAY_MODE_ID_BACK)
			{
				l_func_arr = this._fFunctionsOnFinish_func_arr;
			}

			for( let i = 0; i < l_func_arr.length; i++ )
			{
				l_func_arr[i].call();
			}

			this._fIsCompleted_bl = false;
		}

		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].play(aOptModeId_int);
		}

		this._fCurrentFrame_num = -1;
		this._fIsPlaying_bl = true;
		this._fIsPaused_bl = false;
		this._fPlayModeId_int = aOptModeId_int;
	}

	/**
	 * Start timeline playing in loop mode.
	 */
	playLoop()
	{
		this.play(MTimeLine.PLAY_MODE_ID_LOOP);
	}

	/**
	 * Start timeline playing in backward mode.
	 */
	playBack()
	{
		this.play(MTimeLine.PLAY_MODE_ID_BACK);
	}

	/**
	 * Start timeline playing from the first frame and play several times.
	 * @param {number} aTimesCount_int - Amount of timeline playing repeat.
	 */
	playSeveralTimes(aTimesCount_int)
	{
		this._fRemainingReplaysCount_int = aTimesCount_int;

		this.play(MTimeLine.PLAY_MODE_ID_REPEAT_SEVERAL_TIMES);
	}

	/**
	 * Start timeline playing from a specific frame.
	 * @param {number} aFrame_num - Start frame number.
	 * @param {number} [aOptModeId_int=MTimeLine.PLAY_MODE_ID_FORWARD] - Timeline play mode.
	 */
	playFromFrame(aFrame_num, aOptModeId_int = MTimeLine.PLAY_MODE_ID_FORWARD)
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].playFromFrame(aFrame_num, aOptModeId_int);
		}

		this._fCurrentFrame_num = aFrame_num;
		this._fIsPlaying_bl = true;
		this._fIsPaused_bl = false;
		this._fPlayModeId_int = aOptModeId_int;
		this._fIsCompleted_bl = false;
	}

	/**
	 * Start timeline playing from a specific time offset.
	 * @param {number} aMillisecondIndex_int - Start time offset.
	 * @param {number} [aOptModeId_int=MTimeLine.PLAY_MODE_ID_FORWARD] - Timeline play mode.
	 */
	playFromMillisecond(aMillisecondIndex_int, aOptModeId_int = MTimeLine.PLAY_MODE_ID_FORWARD)
	{
		let lFrameIndex_num = this.convertMillisecondToFrameIndex(aMillisecondIndex_int)*this._fInputFrameRateMultiplier_num;

		this.playFromFrame(lFrameIndex_num, aOptModeId_int);
	}
	
	/**
	 * Set timeline to the first frame.
	 */
	windToStart()
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].windToStart();
		}
	}

	/**
	 * Set timeline to final frame.
	 */
	windToEnd()
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].windToEnd();
		}
	}

	windToEndAndExecuteAllRemainingFunctions()
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].windToEndAndExecuteAllRemainingFunctions();
		}
	}

	/**
	 * Stop timeline playing.
	 */
	stop()
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			if(lAnimations_ma_arr[i].isPlaying())
			{
				lAnimations_ma_arr[i].stop();
			}
		}

		this._fIsPlaying_bl = false;
	}

	/**
	 * Stop timeline playing and reset to initial state.
	 */
	reset()
	{
		this.stop();

		this._fCurrentFrame_num = -1;
		this._fIsPaused_bl = false;
		this._fIsCompleted_bl = false;
		this._fPlayModeId_int = MTimeLine.PLAY_MODE_ID_FORWARD;
		this._fRemainingReplaysCount_int = 0;
	}

	/**
	 * Pause timeline playing.
	 */
	pause()
	{
		this._fIsPaused_bl = true;
	}

	/**
	 * Resume timeline playing.
	 */
	resume()
	{
		this._fIsPaused_bl = false;
	}

	/**
	 * Checks if any animation is playing on timeline.
	 * @returns {boolean}
	 * @private
	 */
	_isAnySubAnimationPlaying()
	{
		let lAnimations_ma_arr = this._fAnimations_ma_arr

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			if(lAnimations_ma_arr[i].isPlaying())
			{
				this._fCurrentFrame_num = lAnimations_ma_arr[i].getCurrentFrameIndex();
				return true;
			}
		}

		return false;
	}

	/**
	 * Indicates whether timeline is paused or not.
	 * @returns {boolean}
	 */
	isPaused()
	{
		return this._fIsPlaying_bl && this._fIsPaused_bl;
	}

	/**
	 * Indicates whether timeline is currently playing or not.
	 * @returns {boolean}
	 */
	isPlaying()
	{
		return this._fIsPlaying_bl && !this._fIsPaused_bl;
	}

	/**
	 * Indicates whether timeline playing completed or not.
	 * @returns {boolean}
	 */
	isCompleted()
	{
		return this._fIsCompleted_bl;
	}

	/**
	 * Sets a Function to be called when timeline reaches specific frame.
	 * @param {Function} aFunction_func - Function to call
	 * @param {number} aFrame_num - Target frame number
	 * @param {*} aContext_ctx - Function context
	 * @param {*} aOptArgument - Function argument
	 */
	callFunctionAtFrame(aFunction_func, aFrame_num, aContext_ctx, aOptArgument)
	{
		if(!this._fAnimations_ma_arr[0])
		{
			this._fAnimations_ma_arr[0] = new MAnimation(this);
		}

		this._fAnimations_ma_arr[0].callFunctionAtFrame(
										aFunction_func,
										aFrame_num,
										aContext_ctx,
										aOptArgument,
										this._fInputFrameRateMultiplier_num);
		

		if(aFrame_num > this._fTotalFramesCount_num)
		{
			this._fTotalFramesCount_num = aFrame_num;
		}
	}

	/**
	 * Add a Function to be called when timeline reaches first frame.
	 * @param {Function} aFunction_func - Function to call
	 * @param {*} aContext_ctx - Function context
	 * @param {*} aOptArgument - Function argument
	 */
	callFunctionOnStart(aFunction_func, aContext_ctx, aOptArgument)
	{
		this._fFunctionsOnStart_func_arr.push(aFunction_func.bind(aContext_ctx, aOptArgument));
	}

	/**
	 * Add a Function to be called when timeline reaches final frame.
	 * @param {Function} aFunction_func - Function to call 
	 * @param {*} aContext_ctx - Function context
	 * @param {*} aOptArgument - Function argument
	 */
	callFunctionOnFinish(aFunction_func, aContext_ctx, aOptArgument)
	{
		this._fFunctionsOnFinish_func_arr.push(aFunction_func.bind(aContext_ctx, aOptArgument));
	}

	/**
	 * Step of timeline through specific amount of frames.
	 * @param {number} aFramesCount_num - Frames amount for step.
	 */
	tick(aFramesCount_num)
	{
		if(this._fIsPaused_bl)
		{
			return;
		}

		if(!this._fIsPlaying_bl)
		{
			this.stop();
			return;
		}

		let lAnimations_ma_arr = this._fAnimations_ma_arr;

		for( let i = 0; i < lAnimations_ma_arr.length; i++ )
		{
			lAnimations_ma_arr[i].onNextFrames(aFramesCount_num);
		}

		if(!this._isAnySubAnimationPlaying())
		{
			let lIsCompleted_bl = false;

			switch(this._fPlayModeId_int)
			{
				case MTimeLine.PLAY_MODE_ID_FORWARD:
				case MTimeLine.PLAY_MODE_ID_BACK:
				{
					lIsCompleted_bl = true;
					this.stop();
				}
				break;
				case MTimeLine.PLAY_MODE_ID_LOOP:
				{
					this.stop();
					this.playLoop();
				}
				break;
				case MTimeLine.PLAY_MODE_ID_REPEAT_SEVERAL_TIMES:
				{
					this.stop();
					this._fRemainingReplaysCount_int--;
					if(this._fRemainingReplaysCount_int === 0)
					{
						lIsCompleted_bl = true;
					}
					else
					{
						this.playSeveralTimes(this._fRemainingReplaysCount_int);
					}
				}
				break;
			}

			if(lIsCompleted_bl)
			{
				let l_func_arr = this._fFunctionsOnFinish_func_arr;

				if(this._fPlayModeId_int === MTimeLine.PLAY_MODE_ID_BACK)
				{
					l_func_arr = this._fFunctionsOnStart_func_arr;
				}

				for( let i = 0; i < l_func_arr.length; i++ )
				{
					l_func_arr[i].call();
				}

				this._fIsCompleted_bl = true;
			}
		}
	}

	/**
	 * Destroy timeline instance.
	 */
	destroy()
	{
		MTimeLine.unregisterTimeLine(this._fIndex_int);
	}
}

export default MTimeLine;