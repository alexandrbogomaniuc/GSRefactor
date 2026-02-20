import EventDispatcher from '../events/EventDispatcher';

var dispatcher = new EventDispatcher();
var _MAX_DELTA = 250;

export const BASE_FRAME_RATE = 16.7;
export const FRAME_RATE = 2*16.7;

/**
 * @typedef TickerInfo
 * @property {PIXI.Ticker} ticker - The shared ticker instance.
 * @property {Ticker} target - Ticker class.
 * @property {number} delta - Time elapsed in milliseconds from last frame to this frame capped by Ticker.MAX_DELTA.
 * @property {number} realDelta - Time elapsed in milliseconds from last frame to this frame.
 */

/**
 * @class
 * @inheritDoc
 * @classdesc Global ticker
 */
class Ticker  {
	static tick(e) {
		/**
		 * Global tick
		 * @event Ticker#tick
		 */
		dispatcher.emit("tick", Ticker.getInfo());
	}

	/**
	 * Maximum amount of milliseconds allowed to elapse between frames.
	 * This value is used to cap TickerInfo.delta.
	 */
	static get MAX_DELTA() {
		return _MAX_DELTA;
	}
	static set MAX_DELTA(val) {
		_MAX_DELTA = val;
	}

	/**
	 * Tick info at the moment.
	 * @returns {TickerInfo}
	 * @static
	 */
	static getInfo() {
		return {
			ticker: PIXI.Ticker.shared,
			target: Ticker,
			delta: Math.min(PIXI.Ticker.shared.elapsedMS, Ticker.MAX_DELTA),
			realDelta: PIXI.Ticker.shared.elapsedMS
		}
	}

	/** add event listener (tick, start, stop) */
	static on(event, fn, context) {
		dispatcher.on(event, fn, context);
	}

	/** add one-time event listener */
	static once(event, fn, context) {
		dispatcher.once(event, fn, context);
	}

	/** remove event listener */
	static removeListener(event, fn, context, once) {
		dispatcher.removeListener(event, fn, context, once);
	}

	/** remove event listener */
	static off(event, fn, context, once) {
		dispatcher.off(event, fn, context, once);
	}

	/** Stop ticker */
	static stop() {
		PIXI.Ticker.shared.stop();
		dispatcher.emit("stop", Ticker.getInfo());
	}

	/** Start ticker */
	static start() {
		PIXI.Ticker.shared.start();
		dispatcher.emit("start", Ticker.getInfo());
	}
}

Ticker.MAX_DELTA = 333;
PIXI.Ticker.shared.maxFPS = 60;
PIXI.Ticker.shared.add(Ticker.tick);
export default Ticker;