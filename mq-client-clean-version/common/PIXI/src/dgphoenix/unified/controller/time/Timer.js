import EventDispatcher from '../events/EventDispatcher';
import Ticker from './Ticker';

let TIMERS = new Set();
let NEW_TIMERS = new Set();

let TIMERS_COUNTER = 0;

/**
 * @class
 * @inheritDoc
 * @extends EventDispatcher
 * @classdesc Timer
 */
class Timer extends EventDispatcher {

	/**
	 * @constructor
	 * @param {Function} onend - function to be called after timer end
	 * @param {Number} timeout - timeout in ms
	 * @param {Boolean} [repeat=false] indicates if it's required to automatically repeat timer after end
	 */
	constructor(onend, timeout = 0, repeat = false) {
		super();

		/** @ignore */
		this.repeat = repeat;

		/** @ignore */
		this.initialTimeout = timeout;

		/** @ignore */
		this.timeout = timeout;
		this.overtime = 0;

		/** @ignore */
		this.destroy = false;

		/** @ignore */
		this.paused = false;

		this._onComplete = onend || null;

		this._destroyed = false;

		this.TIMER_ID = ++TIMERS_COUNTER;

		this.on('end', this._onTimerCompleted, this);

		NEW_TIMERS.add(this);
	}

	reset(timeout)
	{
		this.initialTimeout = timeout;
		this.timeout = timeout;
	}

	_onTimerCompleted()
	{
		if (this._onComplete)
		{
			if ('string' == typeof this._onComplete) 
			{
				eval(this._onComplete);
			}
			else
			{
				this._onComplete();
			}
		}
	}

	setTimeout(val = 0){
		this.initialTimeout = this.timeout = val && val > 0 ? val : 0;
	}

	destructor()
	{
		if (this._destroyed)
		{
			return;
		}
		
		super.destructor();

		TIMERS.delete(this);
		NEW_TIMERS.delete(this);

		this.off('end', this._onTimerCompleted, this);

		this._onComplete = null;
		this._destroyed = true;
	}

	/**
	 * Updates timer state
	 * @param {number} delta ms
	 * @ignore
	 */
	tick(delta) {
		if (this.destroy || this.paused) return;

		if (this._destroyed)
		{
			throw new Error("Can't continue destroyed timer.");
		}

		this.timeout -= delta;
		
		this.emit("tick", {delta: delta, left: this.timeout});

		if (this.timeout <= 0) {

			let overtime = this.overtime = -this.timeout;
			
			this.emit("end", {overtime: overtime});

			if (this.repeat) {
				this.rewind();
				this.timeout -= overtime;
			}
			else {
				this.destroy = true;
			}
		}
	}

	/**
	 * Rewinds timer to the beginning
	 */
	rewind() {
		this.timeout = this.initialTimeout;
	}

	/**
	 * Resume timer
	 */
	resume() {
		this.paused = false;
	}

	/**
	 * Pause timer
	 */
	pause() {
		this.paused = true;
	}
	
	/**
	 * Finish timer immediately
	 */
	finish() {
		this.paused = false;
		this.tick(this.timeout);
	}

	start(){
		this.destroy = false;
		this.rewind();
		this.resume();
	}

	isInProgress()
	{
		return !this.destroy && !this.paused;
	}

	getElapsedTime()
	{
		return this.initialTimeout - this.timeout;
	}

	/**
	 * Tick all timers
	 * @param {Number} delta
	 * @param {Iterable} [items] All timers by default
	 * @ignore
	 */
	static tick(delta, items = TIMERS) {
		this.flush();
		for (let t of items) {
			t.tick(delta);
		}
		this.cleanup();
	}

	/**
	 * Register new created Timers
	 * @ignore
	 */
	static flush() {
		for (let tween of NEW_TIMERS) {
			TIMERS.add(tween);
		}
		NEW_TIMERS.clear();
	}

	/**
	 * Cleanup timer marked as destroyed
	 * @ignore
	 */
	static cleanup() {
		let destroyed = this.filter('destroy', true);
		this.destroy(destroyed);
	}

	/**
	 * Destroy timers
	 * @param {Iterable} [items] All by default
	 * @ignore
	 */
	static destroy(items = [...TIMERS, ...NEW_TIMERS]) {
		for (let t of items) {
			t.destructor();
		}
	}

	/**
	 * Mark timers to be destroyed next tick
	 * @param {Iterable} [items] All by default
	 * @ignore
	 */
	static softDestroy(items = [...TIMERS, ...NEW_TIMERS]) {
		for (let t of items) {
			t.destroy = true;
		}
	}

	/**
	 * Find all Timers by property
	 * @param {String} prop Filter property
	 * @param {...*} values List of values
	 * @return {Tween[]} All Timers matching property value
	 * @ignore
	 */
	static filter(prop, ...values) {
		let result = [...TIMERS, ...NEW_TIMERS];
		if (undefined !== prop && values.length) {
			for (let val of values) {
				result = result.filter(
					t => t[prop] === val
				);
			}
		}
		return result;
	}

	/** ignore */
	static get debugInfo() {
		return `TIMERS: ${TIMERS.size} +${NEW_TIMERS.size}`;
	}

	static clear(timer) {
		timer.destroy = true;
	}
}

Ticker.on("tick", (e) => {
	Timer.tick(e.delta);
});

export default Timer;