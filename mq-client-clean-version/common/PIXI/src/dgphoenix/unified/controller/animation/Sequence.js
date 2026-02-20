import EventDispatcher from '../events/EventDispatcher';
import Tween from './Tween';
import Timer from '../time/Timer';
import { APP } from '../main/globals';

let SEQ = new Set();
let NEW_SEQ = new Set();

/**
 * @class
 * @classdesc Class of animation sequences
 * @extends EventDispatcher
 * @inheritDoc
 * @memberof module:animation
 * @example let sequence = [
 * {
	 *     tweens: [
	 *         {prop: "x", to: 100},
	 *         {prop: "y", to: 100},
	 *         {prop: "rotation", to: Math.PI*2, duration: 1000}
	 *     ],
	 *     duration: 800,
	 *     ease: easing.cubic.easeIn
	 * },
	 * {
	 *     tweens: [
	 *         {prop: "scale.x", from: 0.5, to: 2},
	 *         {prop: "scale.y", from: 0.5, to: 2},
	 *         {prop: "rotation", to: 0}
	 *     ],
	 *     duration: 1000
	 * }
 * ];
 */

let IDD_COUNTER = 0;
class Sequence extends EventDispatcher 
{
	static get EVENT_ON_SEQUENCE_PLAYING_COMPLETED() {return "finish"};
	/**
	 * @constructor
	 * @param {Object} obj - Target object to which animation sequences are applied
	 * @param {Object} sequence - sequence configuration
	 */
	constructor(obj, sequence, aOptIsReusable_bl = false)
	{
		super();

		this.obj = obj;
		this.sequence = sequence;
		this.currentAnimation = -1;

		this.currentTweens = [];

		this.ended = false;

		this.startTimer = null;
		this.endTimer = null;

		this._destroyed = false;
		this.IDD = ++IDD_COUNTER;
		this._fIsReusable_bl = aOptIsReusable_bl;

		this._callStack = null;
		this._currentTarget= null;

		if (APP.isDebugMode)
		{
			this.saveTargetData(obj);
		}

		NEW_SEQ.add(this);
	}

	saveTargetData(obj)
	{
		try
		{
			this._callStack = new Error().stack.toString();

			if (!obj.target)
			{
				this._currentTarget = {description: 'Object', target: Object.assign({}, obj)};
			}
			else
			{
				if (!obj.target.asset || !obj.target.asset.name)
				{
					let children = [];
					if (obj.target.children)
					{
						for (let i = 0; i < obj.target.children.length; i++)
						{
							if (!obj.target.children[i].asset || !obj.target.children[i].asset.name)
							{
								children.push(obj.target.children[i].target);
							}
							else
							{
								children.push(obj.target.children[i].asset.name);
							}
						}
					}

					this._currentTarget = {description: 'Container Sprite. Target contains children', target: children};
				}
				else
				{
					this._currentTarget = {description: 'Img Sprite', target: obj.target.asset.name};
				}
			}
		}
		catch(e)
		{
			if (APP.isDebugMode)
			{
				console.error(e);
			}
		}
	}

	start(delay = undefined)
	{
		this.currentAnimation = -1;

		if (delay !== undefined && delay > 0)
		{
			this._triggerStartDelay(delay);
		}
		else 
		{
			this._startPlaying();
		}
	}

	_triggerStartDelay(delay)
	{
		this.startTimer = new Timer(() => this._onStartTimeoutCompleted(), delay);
	}

	_onStartTimeoutCompleted()
	{
		this.startTimer.destroy = true;
		this._startPlaying();
	}

	_startPlaying() 
	{
		if (!(this.sequence || this.sequence.length))
		{
			return;
		}

		this._playNext();
	}

	/**
	 * Start the next animation
	 */
	_playNext(aOverTime_num=0) 
	{
		if (this._destroyed)
		{
			throw new Error ("Can't continue destroyed sequence.");
		}
		
		this.currentAnimation++;

		try
		{
			var animation = this.sequence[this.currentAnimation];
			var masterDuration, duration, tweens, tweenDescription, from, to, tween, ease;

			tweens = animation.tweens;
			if (!Array.isArray(tweens))
			{
				tweens = [tweens];
			}

			masterDuration = animation.duration || 0;
			if (aOverTime_num > 0)
			{
				masterDuration -= aOverTime_num; // Tween will be started even if masterDuration is negative value to complete Tween in the next tick (to keep the order of tweens completion between sequenses)
			}

			this.currentTweens = [];

			for (var n=0; n < tweens.length; n++) 
			{
				tweenDescription = tweens[n];

				let obj = this.obj;
				let prop = tweenDescription.prop;
				let parts = prop.split(".");
				
				while (parts.length > 1) obj = obj[parts.shift()];
				prop = parts[0];

				duration = tweenDescription.duration;
				if (typeof duration == 'undefined' || duration > masterDuration) duration = masterDuration;

				from = tweenDescription.from;
				if (typeof from == 'undefined') from = obj[prop];

				to = tweenDescription.to;
				if (typeof to == 'undefined') to = obj[prop];

				ease = tweenDescription.ease || animation.ease || null;

				tween = new Tween(obj, prop, from, to, duration, ease);

				if(typeof tweenDescription.onchange != "undefined") tween.on('change', tweenDescription.onchange);
				if(typeof tweenDescription.onfinish != "undefined") tween.on('finish', tweenDescription.onfinish);

				tween.on(Tween.EVENT_ON_DESTROYING, this._onTweenDestroying, this);

				tween.play();

				this.currentTweens.push(tween);
			}
		}
		catch(e)
		{
			console.error({stack: this._callStack, target: this._currentTarget, tweenParam: tweenDescription});
			throw new Error(e);
		}

		this.endTimer = new Timer(() => this._onEndTimeoutCompleted(), masterDuration);
	}

	_onTweenDestroying(event)
	{
		if (!this.currentTweens || !this.currentTweens.length)
		{
			return;
		}

		let tween = event.target;
		let lId_num = this.currentTweens.indexOf(tween);
		if (~lId_num)
		{
			this.currentTweens.splice(lId_num, 1);
		}
	}

	_onEndTimeoutCompleted(aOptOverTime_num=0)
	{
		let lOverTime_num = aOptOverTime_num || this.endTimer.overtime || 0;

		if (this.endTimer)
		{
			this.endTimer.destroy = true;
		}

		this.clearTweens(true);

		if (this.currentAnimation >= 0) 
		{
			var lastPlayedAnimation = this.sequence[this.currentAnimation];
			if (lastPlayedAnimation.onfinish instanceof Function) 
			{
				lastPlayedAnimation.onfinish({target: this});
			}

			if (this._destroyed)
			{
				// this situation is possible when sequence destroy initiated in lastPlayedAnimation.onfinish handler called above
				//TODO avoid destroying Sequence in lastPlayedAnimation.onfinish handler
				return;
			}

			if (typeof lastPlayedAnimation.loop != "undefined" && lastPlayedAnimation.loop >= 0)
			{
				this.currentAnimation -= 1;
				lastPlayedAnimation.loop -= 1;
			}
		}

		if (this.currentAnimation >= this.sequence.length-1)
		{
			this._onSequencePlayingCompleted();
		}
		else
		{
			this._playNext(lOverTime_num);
		}
	}

	_onSequencePlayingCompleted()
	{
		this.ended = true;

		let e = {cancelDestroy: this._fIsReusable_bl};
		this.emit(Sequence.EVENT_ON_SEQUENCE_PLAYING_COMPLETED, e);

		if(!e.cancelDestroy) this.destructor();
	}
	/**
	 * @ignore
	 */
	executeTweensMethod(method) {
		for(let i=0; i<this.currentTweens.length; i++) {
			this.currentTweens[i][method]();
		}
	}

	/**
	 * @ignore
	 */
	clearTweens(aOptUseForceFinish_bl) {
		while (this.currentTweens.length)
		{
			let lCurTween = this.currentTweens.pop();
			lCurTween.destructor(aOptUseForceFinish_bl);
		}
		
		this.currentTweens = [];
	}

	/**
	 * Stop the sequence
	 */
	stop() {
		if(this.ended) return;

		this.executeTweensMethod("stop");
		if(this.startTimer) this.startTimer.destroy = true;
		if(this.endTimer) this.endTimer.destroy = true;
	}

	get paused() {
		return (this.startTimer && this.startTimer.paused()) || (this.endTimer && this.endTimer.paused);
	}

	/**
	 * Pause sequence playing
	 */
	pause() {
		if(this.ended) return;

		this.executeTweensMethod("pause");
		if(this.startTimer) this.startTimer.pause();
		if(this.endTimer) this.endTimer.pause();
	}

	/**
	 * Resume sequence playing
	 */
	resume() {
		if(this.ended) return;

		this.executeTweensMethod("play");
		if(this.startTimer) this.startTimer.resume();
		if(this.endTimer) this.endTimer.resume();
	}

	destructor() 
	{
		if (this._destroyed)
		{
			return;
		}
		
		if (this.startTimer) this.startTimer.destructor();
		if (this.endTimer) this.endTimer.destructor();

		SEQ.delete(this);
		NEW_SEQ.delete(this);

		this.obj = null;

		this.clearTweens();
		this.currentTweens = null;

		this.startTimer = null;
		this.endTimer = null;
		this.ended = true;
		this.sequence = null;
		this.currentAnimation = -1;

		this._callStack = null;
		this._currentTarget= null;

		this._destroyed = true;
	}

	/**
	 * Initialization and start of animations
	 * @param {Object} obj Target object to which animation sequences are applied
	 * @param {Object} sequence sequence configuration
	 * @param {Number} [delay=0] delay before Sequence start
	 * @returns {Sequence}
	 * @example Sequence.start(mySprite, [
	 * {
	 *     tweens: [
	 *         {prop: "x", to: 100},
	 *         {prop: "y", to: 100},
	 *         {prop: "rotation", to: Math.PI*2, duration: 1000}
	 *     ],
	 *     duration: 800,
	 *     ease: easing.cubic.easeIn
	 * },
	 * {
	 *     tweens: [
	 *         {prop: "scaleX", from: 0.5, to: 2},
	 *         {prop: "scaleY", from: 0.5, to: 2},
	 *         {prop: "rotation", to: 0}
	 *     ],
	 *     duration: 1000
	 * }
	 * ]);
	 */
	static start(obj, sequence, delay, aOptIsReusable_bl = false) 
	{
		if(!obj || !sequence) return;

		var sequenceInstance = new Sequence(obj, sequence, aOptIsReusable_bl);

		sequenceInstance.start(delay);

		return sequenceInstance;
	}

	static destroy(items = [...SEQ, ...NEW_SEQ]) 
	{
		for (let t of items) {
			t.destructor();
		}
	}

	static filter(prop, ...values) 
	{
		let result = [...SEQ, ...NEW_SEQ];
		if (undefined !== prop && values.length) {
			for (let val of values) {
				result = result.filter(
					tweenDescription => tweenDescription[prop] === val
				);
			}
		}
		return result;
	}

	static findByTarget(...targets) 
	{
		return this.filter('obj', ...targets);
	}
}

export default Sequence;