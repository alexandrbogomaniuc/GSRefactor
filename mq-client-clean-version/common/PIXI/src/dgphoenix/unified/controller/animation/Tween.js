import EventDispatcher from '../events/EventDispatcher';
import Ticker from '../time/Ticker';
import { easeIn as defaultEasing } from '../../model/display/animation/easing/linear';

/**
 * Instances registry
 * @type {Set}
 * @ignore
 */
let TWEENS = new Set();

/**
 * New created instances. Flushes in TWEENS after the tick.
 * This is to avoid circular ticks when Tween created inside another Tween callback.
 * @type {Set}
 * @ignore
 */
let NEW_TWEENS = new Set(); //

/**
 * @class
 * @extends EventDispatcher
 * @classdesc Animation class
 * @memberOf module:animation
 */
 let IDD_COUNTER = 0;
class Tween extends EventDispatcher
{
	static get EVENT_ON_FINISHED() {return "finish"};
	static get EVENT_ON_CHANGE() {return "change"};
	static get EVENT_ON_END_CICLE() {return "endcicle"};
	static get EVENT_ON_DESTROYING() {return "destroyed"};

	/**
	 * @constructor
	 * @param {Object} obj - an object with an animated property
	 * @param {string|number} prop - name of animated property or a number
	 * @param {number} start - start value
	 * @param {number} end - end value
	 * @param {number} duration
	 * @param {Function} - function of prop value update
	 * @param {boolean} autoRewind - auto repeat
	 * @param {number} delay - delay before Tween start (in ms)
	 * @augments EventDispatcher
	 */
	constructor(obj, prop, start, end, duration, easing = defaultEasing, autoRewind=false, delay=0) {
		super();

		if (typeof obj != 'object') obj = null;

		if (obj) {
			if (typeof obj[prop] == 'undefined') 
				{
					debugger;
					throw new Error('Trying to tween undefined property "' + prop + '"');
				}
			if (isNaN(obj[prop])) 
				{
					throw new Error('Tweened value can not be ' + ( typeof obj[prop]));
				}
		}
		else {
			if (isNaN(prop)) 
				{
					debugger;
					throw new Error('Tweened value can not be ' + ( typeof prop));
				}
		}

		if (typeof easing != 'function') easing = defaultEasing;

		/**
		 * Target object that has animated prop
		 * @type Object
		 * @ignore
		 */
		this.obj = obj;

		/**
		 * The name of animated property. Value of this property is a number.
		 * @type String
		 * @ignore
		 */
		this.prop = prop;

		/**
		 * Start value
		 * @type Number
		 * @ignore
		 */
		this.start = start;

		/**
		 * Final value
		 * @type Number
		 * @ignore
		 */
		this.end = end;

		/**
		 * Animation duration (in ms)
		 * @type Number
		 * @ignore
		 */
		this.duration = ~~duration;

		/**
		 * Callback function triggered on animation end
		 * @type Function
		 * @ignore
		 */
		this.callback = easing;

		/**
		 * Boolean value that indicates is animation playing or not
		 * @type Boolean
		 * @ignore
		 */
		this.playing = false;

		/**
		 * Current position of animation
		 * @ignore
		 * @type Number
		 * @ignore
		 */
		this._pos = -1 - delay;

		/**
		 * Indicates if its required to automatically repeat an animation after its end
		 * @type Boolean
		 * @ignore
		 */
		this.autoRewind = autoRewind;

		/** @ignore */
		this.destroy = false;

		this._destroyed = false;

		NEW_TWEENS.add(this);

		this.IDD = ++IDD_COUNTER;
	}

	/**
	 * Progress in percents (from 0 to 1)
	 * @returns {Number} From 0 to 1
	 */
	get progress()
	{
		if (this._destroyed)
		{
			return 0;
		}
		else
		{
			if (this._pos < 0)
			{
				return 0;
			}
			else if (this._pos > this.duration)
			{
				return 1;
			}
			else
			{
				return this._pos/this.duration;
			}
		}
	}

	destructor(aOptUseForceFinish_bl)
	{
		this.stop();
		
		if (aOptUseForceFinish_bl)
		{
			this.updateValue(this.end);
		}

		TWEENS.delete(this);
		NEW_TWEENS.delete(this);

		this.obj = null;
		this.callback = null;
		this.playing = false;

		this._destroyed = true;

		this.emit(Tween.EVENT_ON_DESTROYING);

		super.destructor();
	}

	/**
	 * Start animation play
	 */
	play(delta = 0) {
		this.playing = true;
		this.tick(delta);
		return this;
	}

	/**
	 * Pause animation
	 */
	pause() {
		this.playing = false;
		return this;
	}

	/**
	 * Resume animation
	 */
	unpause() {
		this.playing = true;
		return this;
	}

	/**
	 * Rewinds playback to the beginning
	 */
	rewind() {
		this._pos = -1;
		return this;
	}

	/**
	 * Jumps directly to the last position
	 */
	forward() {
		this._pos = this.duration;
		return this;
	}

	/**
	 * Stops playback and rewinds to the beginning
	 */
	stop() {
		this.pause();
		this.rewind();
		return this;
	}

	/**
	 * Sets the value of animated property
	 * @param {number} val New value of animated prop
	 * @ignore
	 */
	updateValue(val) {
		if (this.obj) {
			this.obj[this.prop] = val;
		}
		else {
			this.prop = val;
		}

		/**
		 * @event Tween#change
		 * @type {Object}
		 * @property {number} position Position between start and end (from 0 to 1)
		 * @property {number} value Current value
		 */
		this.emit(Tween.EVENT_ON_CHANGE, {
			value: val,
			position: ((val - this.start) / (this.end - this.start))
		});

		return this;
	}

	/**
	 * Play animation in reverse order
	 */
	reverse() {
		[this.start, this.end] = [this.end, this.start];
		this.rewind();
		this.play();
	}

	/**
	 * Do step of animation
	 * @param {number} delta
	 * @ignore
	 */
	tick(delta = 0) {
		if (!this.playing) return false;

		if (this._destroyed)
		{
			throw new Error("Can't continue destroyed tween.");
		}

		this._pos += delta;

		// it's possible to use this behavior to create delay before animation
		if (this._pos < 0) return false;

		if (this._pos > this.duration)
		{
			if (this.autoRewind)
			{
				this._pos -= this.duration;
				this.emit(Tween.EVENT_ON_END_CICLE);
			}
			else {
				return this.finish();
			}
		}

		var val = this.start == this.end
			? this.start * 1
			: this.callback(this._pos, this.start, this.end - this.start, this.duration);

		this.updateValue(val);

		return false;
	}

	/**
	 * Finish an animation
	 */
	finish() {

		this.stop();
		this.updateValue(this.end);

		/**
		 * @event Tween#finish
		 * @property {number} position Position between start and end (from 0 to 1)
		 * @property {number} value Current value
		 * @property {boolean} cancelDestroy Specifies if it is required to cancel Tween destroying
		 */
		let e = {
			position: 1,
			value: this.end,
			cancelDestroy: false
		};

		this.emit(Tween.EVENT_ON_FINISHED, e);

		if(!e.cancelDestroy) this.destroy = true;
	}

	/**
	 * Tick all registered Tweens
	 * @param {Number} delta
	 * @param {Iterable} [items] All tweens by default
	 * @ignore
	 */
	static tick(delta, items = TWEENS) {
		this.cleanup();
		this.flush();
		for (let t of items) {
			t.tick(delta);
		}
	}

	/**
	 * Register new created Tweens
	 * @ignore
	 */
	static flush() {
		for (let tween of NEW_TWEENS) {
			TWEENS.add(tween);
		}
		NEW_TWEENS.clear();
	}

	/**
	 * Cleanup tweens marked as destroyed
	 * @ignore
	 */
	static cleanup() {
		let destroyed = this.filter('destroy', true);
		this.destroy(destroyed);
	}

	/**
	 * Destroy tweens
	 * @param {Iterable} [items] All tweens by default
	 * @ignore
	 */
	static destroy(items = [...TWEENS, ...NEW_TWEENS]) {
		for (let t of items) {
			t.destructor();
		}
	}

	/**
	 * Mark tweens to be destroyed next tick
	 * @param {Iterable} [items] All tweens by default
	 * @ignore
	 */
	static softDestroy(items = [...TWEENS, ...NEW_TWEENS]) {
		for (let t of items) {
			t.destroy = true;
		}
	}

	/**
	 * Find all Tweens by property
	 * @param {String} prop Filter property
	 * @param {...*} values List of values
	 * @return {Tween[]} All Tweens matching property value
	 * @ignore
	 */
	static filter(prop, ...values) {
		let result = [...TWEENS, ...NEW_TWEENS];
		if (undefined !== prop && values.length) {
			for (let val of values) {
				result = result.filter(
					tween => tween[prop] === val
				);
			}
		}
		return result;
	}

	/**
	 * Find all Tweens with target object matching any of targets
	 * @method module:animation.Tween.findByTarget
	 * @param {...*} targets List of objects
	 * @return {Tween[]} All matching tweens
	 */
	static findByTarget(...targets) {
		return this.filter('obj', ...targets);
	}

	/** @ignore */
	static get debugInfo(){
		return `TWEENS: ${TWEENS.size} +${NEW_TWEENS.size}`;
	}

}

Ticker.on("tick", (e) => {
	Tween.tick(e.delta);
});

export default Tween;